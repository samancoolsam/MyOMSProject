package com.academy.ecommerce.sterling.bopis.sms.api;

/**#########################################################################################
*
* Project Name                : OMS_MAR_REL_3_2020
* Module                      : OMNI-4346 to OMNI-4349
* Author                      : Arun Reddy Bendram(C0015576)
* Author Group				  : CTS-POD
* Date                        : 27-Feb-2020 
* Description				  : This class publish SMS to ESB queue when BOPIS order is cancelled .
* 								 
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       		Remarks/Description                      
* ---------------------------------------------------------------------------------
* 27-Feb-2020		CTS  	 			  1.0           	Updated version
*
* #########################################################################################*/

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySendSMSOnOrderCancel implements YIFCustomApi {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademySendSMSOnOrderCancel.class);

	/**
	 * This method validates if hte Order is Full/Partial cancel and creates the SMS to be sent to Customer
	 * 
	 * @param inDoc
	 * @return docSMSMessage
	 * @throws Exception
	 */
	public Document sendSMSonOrderCancel(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("Start AcademySendSMSOnOrderCancel.sendSMSonOrderCancel() Input XML " + XMLUtil.getXMLString(inDoc));

		String strCancelType = "";
		String strPrimaryPhoneNo = "";
		String strAlternatePhoneNo = "";

		Element eleOrder = inDoc.getDocumentElement();
		eleOrder.removeAttribute(AcademyConstants.STR_SKIP_EMAIL);

		String strMaxStatus = eleOrder.getAttribute(AcademyConstants.ATTR_MAX_ORDER_STATUS);
		
		//Retrive the reason code to determine ASO cancellation or Customer cancellation
		String strReasonCode = XPathUtil.getString((Node) eleOrder, AcademyConstants.XPATH_SMS_ATTR_REASON_CODE);
		String strOrderNo = eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO);
		String strOrderHeaderKey = eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
		boolean partialCancellation = checkforpartialCancellations(env,strOrderNo,strOrderHeaderKey);
		if(YFCObject.isNull(strReasonCode)) {
			strReasonCode = XPathUtil.getString((Node) eleOrder,"/Order/OrderAudit/OrderAuditLevels/OrderAuditLevel[@ModificationLevel='ORDER_LINE']/OrderAuditDetails"
					+ "/OrderAuditDetail[@AuditType='Note']/Attributes/Attribute[@Name='ReasonCode']/@NewValue");
		}
		if(YFCObject.isNull(strReasonCode))
		 	strReasonCode = XPathUtil.getString((Node) eleOrder,"OrderLines/OrderLine[@MinLineStatus='9000']/Notes/Note/@ReasonCode");
		
		//Check if the Order line cancelled from Webstore as part oc Backroom Pick process
		String strIsBOPISCancelFromWebStore = (String) env.getTxnObject(AcademyConstants.ATTR_IS_WEB_STORE_FLAG);
		if (!YFCObject.isVoid(strIsBOPISCancelFromWebStore)
				&& (strIsBOPISCancelFromWebStore.equals(AcademyConstants.ATTR_Y))) {
			//if (!YFCObject.isVoid(strMaxStatus) && 
			//		!strMaxStatus.equals(AcademyConstants.VAL_CANCELLED_STATUS)) {
				log.verbose("::The sms for this line is ignored as it is a partial cancellation from WEBSOM::");
				inDoc.getDocumentElement().setAttribute(AcademyConstants.STR_SKIP_EMAIL, AcademyConstants.STR_TRUE);
				return inDoc;
			//}
		}

		log.verbose(" strMaxStatus :: "+ strMaxStatus +" :: strReasonCode ::"+strReasonCode);
		
		//Retrieveing the customer Infor or Phone No
		Element elePersonInfoBillToOut = SCXmlUtil.getChildElement(eleOrder, AcademyConstants.ELE_PERSON_INFO_BILL_TO);
		Element elePersonInfoMarkForOut = SCXmlUtil.getChildElement(eleOrder, AcademyConstants.ELEM_PERSON_INFO_SHIP_TO);
		
		if (!YFCObject.isVoid(elePersonInfoBillToOut)) {
			strPrimaryPhoneNo = elePersonInfoBillToOut.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
			//strZipCode = elePersonInfoBillToOut.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
		}
		if (!YFCObject.isVoid(elePersonInfoMarkForOut)) {
			strAlternatePhoneNo = elePersonInfoMarkForOut.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
		}
		
		if(inDoc.getDocumentElement().getTagName().equals(AcademyConstants.ELE_SHIPMENT)) {
			log.verbose("Shipment full Shortpick Cancel from Web SOM");
			strMaxStatus = AcademyConstants.VAL_CANCELLED_STATUS;
			strReasonCode = AcademyConstants.STR_MOD_REASON_VALUE;
			strPrimaryPhoneNo = XPathUtil.getString(inDoc,"/Shipment/ShipmentLines/ShipmentLine/OrderLine/Order/PersonInfoBillTo/@DayPhone");
			strAlternatePhoneNo = XPathUtil.getString(inDoc,"/Shipment/ShipmentLines/ShipmentLine/OrderLine/Order/PersonInfoShipTo/@DayPhone");
		}
		

		//Check if the cancellation is ASO or customer Cancellation by validating in Commoncode CodeType=Customer
		log.verbose("partialCancellations::" + partialCancellation + " Reason Code::" + strReasonCode + " Order Number::" + strOrderNo);
		//This is called for reasons AuthorizationFailure,Backgroud Check Failure
		String strIsBOPISEMailTemplateRequired = eleOrder.getAttribute("IsBOPISEMailTemplateRequired");
		strCancelType = getCancellationType(env, strReasonCode, partialCancellation,strIsBOPISEMailTemplateRequired);
		log.verbose("Common Code Type  :: " + strCancelType);
		
		//Retrieve the cancellation message format from Common codes 
		Document docCancelMsgCommonCodeOut = getCommonCodeList(env, AcademyConstants.STR_BOPIS_ORDER_CANCEL, null);
		log.verbose("output of getCommonCodeList " + XMLUtil.getXMLString(docCancelMsgCommonCodeOut));

		//Prepare cancellation message based on Reason code
		String strMessage = getCancellationMessage(docCancelMsgCommonCodeOut, strCancelType);
		if(YFCObject.isVoid(strMessage)){
			inDoc.getDocumentElement().setAttribute(AcademyConstants.STR_SKIP_EMAIL, AcademyConstants.STR_TRUE);
			return inDoc;
		}
		
		//Update missing variables in the SMS Message
		strMessage = strMessage.replace(AcademyConstants.ATT_$ORDER_NO, strOrderNo);
		//strOrderLink = strLink + "/"+ strOrderNo +"/" + strZipCode;
		
		Document docSMSMessage = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, AcademyConstants.STR_BOPIS_CANCELLATION);
		docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE, strMessage);
		
		if (!YFCObject.isVoid(strPrimaryPhoneNo)) {
			docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_DAY_PHONE, strPrimaryPhoneNo);
			//docSMSMessage.getDocumentElement().setAttribute("OrderLink", strOrderLink);
			log.verbose("primary customer SMS message : " + XMLUtil.getXMLString(docSMSMessage));
		}
		else if (!YFCObject.isVoid(strAlternatePhoneNo)) {
			docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_DAY_PHONE, strAlternatePhoneNo);
			//docSMSMessage.getDocumentElement().setAttribute("OrderLink", strOrderLink);
			log.verbose("alternate person SMS message : " + XMLUtil.getXMLString(docSMSMessage));
		}
		
		log.verbose("End AcademySendSMSOnOrderCancel.sendSMSonOrderCancel() Output XML " + XMLUtil.getXMLString(docSMSMessage));
		return docSMSMessage;
	}
	/**
	 * This method invokes getOrderList API and checks the partial cancellations
	 * 
	 * @param strCodeType
	 * @param strCodeValue
	 * @return outDocGetCommonCodeList
	 * @throws Exception
	 */
	
	private boolean checkforpartialCancellations(YFSEnvironment env, String strOrderNo,String strOrderHeaderKey) throws Exception {
		// TODO Auto-generated method stub
		log.verbose("Start AcademySendSMSOnOrderCancel.checkforpartialCancellations()");
		boolean partialCancellation = false;
		Document inDocGetOrderList = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		Element elemOrder = inDocGetOrderList.getDocumentElement(); 
		elemOrder.setAttribute(AcademyConstants.HISTORY_ORDER_FLAG, AcademyConstants.STR_NO);
		elemOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		if(!YFCObject.isVoid(strOrderHeaderKey))
			elemOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
		
		Document templateDocGetOrderList = XMLUtil.getDocument("<OrderList LastOrderHeaderKey='' LastRecordSet='' ReadFromHistory='' TotalOrderList=''>" + 
				"	<Order DocumentType='' EnterpriseCode='' OrderHeaderKey=''  OrderNo=''  Status=''>" + 
				"		<OrderLines>" + 
				"			<OrderLine DeliveryMethod='' MaxLineStatus='' MinLineStatus='' OrderHeaderKey='' OrderLineKey='' OrderedQty='' OriginalOrderedQty='' "+
			    "             PrimeLineNo='' Status=''/>" + 
				"		</OrderLines>" + 
				"	</Order>" + 
				"</OrderList>");
		log.verbose("AcademySendSMSOnOrderCancel.getOrderList() Input XML " + XMLUtil.getXMLString(inDocGetOrderList));
		log.verbose("AcademySendSMSOnOrderCancel.getOrderList() Template XML " + XMLUtil.getXMLString(templateDocGetOrderList));
		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, templateDocGetOrderList);
		Document outDocGetOrderList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST,
				inDocGetOrderList);
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
		
		log.verbose("AcademySendSMSOnOrderCancel.getOrderList() Output XML " + XMLUtil.getXMLString(outDocGetOrderList));
		
		Element elegetOrderlistOutput = outDocGetOrderList.getDocumentElement();
		log.beginTimer("Xpathcheck");
		Node nOrderLine =  XPathUtil.getNode(elegetOrderlistOutput,AcademyConstants.XPATH_SMS_GET_ORDER_LINE); 
		log.endTimer("Xpathcheck");
		
		if(!YFCObject.isVoid(nOrderLine))
			partialCancellation = true;
		log.verbose("Partial Cancellation :: "+partialCancellation);
		log.verbose("Start AcademySendSMSOnOrderCancel.checkforpartialCancellations()");
		return partialCancellation;
	}


	/**
	 * This method invokes getCommonCodeList API and provides the results
	 * 
	 * @param strCodeType
	 * @param strCodeValue
	 * @return outDocGetCommonCodeList
	 * @throws Exception
	 */
	private Document getCommonCodeList(YFSEnvironment env, String strCodeType, String strCodeValue ) throws Exception {
		
		log.verbose("Start AcademySendSMSOnOrderCancel.getCommonCodeList() strCodeType :: " + strCodeType + " :: strCodeValue :: "+strCodeValue);
		
		Document inDocGetCommonCodeList = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		if(!YFCObject.isVoid(strCodeType)) {
			inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, strCodeType);
		}
		
		if(!YFCObject.isVoid(strCodeValue)) {
			inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE, strCodeValue);
		}
		
		if(!YFCObject.isVoid(strCodeType) && strCodeType.equals("YCD_PICK_SHORT_RESOL")) {
			inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		}
		
		log.verbose("input to getCommonCodeList " + XMLUtil.getXMLString(inDocGetCommonCodeList));
		Document outDocGetCommonCodeList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST,
				inDocGetCommonCodeList);
		
		log.verbose("End AcademySendSMSOnOrderCancel.getCommonCodeList() Output XML " + XMLUtil.getXMLString(outDocGetCommonCodeList));
		return outDocGetCommonCodeList;

	}
	
	/**
	 * This method validates and provides the cancel Type
	 * If strIsBOPISEMailTemplateRequired is Y and reason code is Customer Abandoned it is Abandned Common code
	 * and then it was cancelled using the reason code STORE_ASSOCIATE_CANCEL then it is BOPIS CANCEL  
	 * 
	 * @param strReasonCode
	 * @param partialCancellation 
	 * @param strIsBOPISEMailTemplateRequired
	 * @return strCancelMessage
	 * @throws Exception
	 */
	private String getCancellationType(YFSEnvironment env, String strReasonCode, boolean partialCancellation, String strIsBOPISEMailTemplateRequired) throws Exception {
		
		log.verbose("Start AcademySendSMSOnOrderCancel.getCancellationType()  strReasonCode :: "+strReasonCode + " :: PartialCancellation :: " +partialCancellation +""
				+ "strIsBOPISEMailTemplateRequired :: "+strIsBOPISEMailTemplateRequired);
		boolean isFraudCancel = false;
		String strCancelType = null;
		if(!YFCObject.isVoid(strIsBOPISEMailTemplateRequired) && strIsBOPISEMailTemplateRequired.equals("Y")) {
			if(strReasonCode.equals(AcademyConstants.STR_CUSTOMER_ABANDONED))
				strCancelType = AcademyConstants.STR_CUST_ABANDONED_ORDER;
			else if (partialCancellation)
				strCancelType = AcademyConstants.STR_ASO_BOPIS_PART_CANCEL ;
			else
				strCancelType = AcademyConstants.STR_ASO_BOPIS_FULL_CANCEL ;
		}
		else {
			Document docCustomerCancelCommonCodeOut = getCommonCodeList(env,AcademyConstants.STR_CUSTOMER_CANCEL_CODE_TYPE, strReasonCode);

			if (partialCancellation) {
				if (docCustomerCancelCommonCodeOut.getDocumentElement().hasChildNodes())
					strCancelType = AcademyConstants.STR_CUSTOMER_PART_CANCEL_MSG;
				else  {
					isFraudCancel = checkForFraudCancellation(env, strReasonCode);
					if (isFraudCancel)
						strCancelType = AcademyConstants.STR_ASO_FRAUD_PART_CANCEL_MSG;
					else
						strCancelType = AcademyConstants.STR_ASO_PART_CANCEL_MSG;
				}
			} else {
				if (docCustomerCancelCommonCodeOut.getDocumentElement().hasChildNodes())
					strCancelType = AcademyConstants.STR_CUSTOMER_FULL_CANCEL_MSG;
				else {
					isFraudCancel = checkForFraudCancellation(env, strReasonCode);
					if (isFraudCancel)
						strCancelType = AcademyConstants.STR_ASO_FRAUD_FULL_CANCEL_MSG;
					else
					strCancelType = AcademyConstants.STR_ASO_FULL_CANCEL_MSG;
				}
			}
		}
		
		if(YFCObject.isVoid(strCancelType)) {
			strCancelType = AcademyConstants.STR_BOPIS_ORDER_CANCEL;
		}
		log.verbose("End AcademySendSMSOnOrderCancel.getCancellationType() Output message " + strCancelType);
		return strCancelType;

	}
	
	/**
	 * This method prepares the Cancellation message output based on CommonCode
	 * 
	 * @param docGetCommonCodeOut
	 * @param strCancelType
	 * @return strCancelMessage
	 * @throws Exception
	 */
	private String getCancellationMessage(Document docCancelMsgCommonCodeOut, String strCancelType) throws Exception {
		
		log.verbose("Start AcademySendSMSOnOrderCancel.getCancellationMessage()  strCancelType :: "+strCancelType);
		
		String strCancelMessage = "";
		int iMsgCount = 1;
		String strMessage = "";
		
		do {
			strMessage = XPathUtil.getString(docCancelMsgCommonCodeOut, "/CommonCodeList/CommonCode[@CodeValue='" 
					+ strCancelType + "_Msg" + iMsgCount + "']/@CodeLongDescription");
			if(YFCObject.isVoid(strMessage)) {
				iMsgCount=0;
			}
			else {
				strCancelMessage = strCancelMessage + strMessage;
				iMsgCount++;
			}
		}
		while (iMsgCount !=0);
		
		log.verbose("End AcademySendSMSOnOrderCancel.getCancellationMessage() Output message " + strCancelMessage);
		return strCancelMessage;

	}
	
	/** 
	 * This method checks whether it is fraud cancel or not
	 * @param env
	 * @param strReasonCode
	 * @return
	 * @throws Exception
	 */
	private boolean checkForFraudCancellation(YFSEnvironment env, String strReasonCode) throws Exception {
		Document outputCommomCodeList = null;
		Element eleCommonCode=null;
		NodeList nleleCommonCode=null;
		String strCodeType = "";
		Document inputTocommonCodeList = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		inputTocommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE, strReasonCode);
		inputTocommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC, strReasonCode);
		outputCommomCodeList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST, inputTocommonCodeList);
		nleleCommonCode = outputCommomCodeList.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
		if (nleleCommonCode.getLength() > 0) {
			for (int i = 0; i < nleleCommonCode.getLength(); i++) {
				eleCommonCode = (Element) nleleCommonCode.item(i);
				if (eleCommonCode != null && eleCommonCode.hasAttributes()) {
					strCodeType = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_TYPE);
					log.verbose("Code Type From Commom Code:" + strCodeType);

					if (strCodeType.equalsIgnoreCase("FraudCancel")) {
						return true;
					} 
					else {
						return false;
					}
					
				} 
			}
		} 
		else {
			return false;
		}
		return false;
	}
	
	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}

}