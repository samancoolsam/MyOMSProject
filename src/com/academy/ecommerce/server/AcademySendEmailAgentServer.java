package com.academy.ecommerce.server;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.academy.ecommerce.sterling.email.util.AcademyEmailUtil;
import com.academy.util.common.AcademyBOPISUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.ycp.core.YCPContext;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantriks.yih.adapter.util.YantriksConstants;


public class AcademySendEmailAgentServer extends YCPBaseTaskAgent {

	//SOF :: Start WN-1814 Ready for Customer Pickup Email
	boolean bIsSOFEmail = false;
	String strDelMethod = "";
	//SOF :: End WN-1814 Ready for Customer Pickup Email
	
	// OMNI-30146 - START :: Logic to send Consolidated Email if Save The Sale Lines
	// Exists in BOPIS Flow
	boolean doNotDelayEmail = true;
	int noOfSaveTheSaleLinesAllocated = 0;
	String strSOFDepartments = "";
	String strIsSTHSOFSMSRequired="";
	// OMNI-30146 - END
	// OMNI-92956 - END
	String strIsRFCPEmailConsolidation="";
	String strEmailType = "";
	private static YFCLogCategory log  = YFCLogCategory.instance(AcademySendEmailAgentServer.class.getName());
	//private static Logger log=Logger.getLogger(AcademySendEmailAgentServer.class.getName());
	@Override

	public Document executeTask(YFSEnvironment env, Document doc)
			throws Exception {
		if(log.isVerboseEnabled()){
			log.verbose("Entering class : AcademySendEmailAgentServer  methodname : executeTask "+XMLUtil.getXMLString(doc));
		}
		Element currElem=null;
		Document outXML1=null;
		Element orderElem=null;
		String orderHeaderKey="";
		String shipmentKey="";
		
		//SOF :: Start WN-1814 Ready for Customer Pickup Email
		NodeList nlSOFShipments = null;
		String strTranID = "";
		//SOF :: End WN-1814 Ready for Customer Pickup Email
		
		//String orderHeaderKey=doc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_KEY);
		String transactionkey=doc.getDocumentElement().getAttribute(AcademyConstants.ATTR_TRANS_KEY);
		String taskQKey=doc.getDocumentElement().getAttribute(AcademyConstants.ATTR_TASK_Q_KEY);
		strEmailType = System.getProperty(AcademyConstants.PROP_ACADEMY_SHIPCONF_EMAILTYPE);

		//Start WN-1560 Shipment Confirmation email - Wrong email IDs being passed
		String strLockId = doc.getDocumentElement().getAttribute(AcademyConstants.ATTR_LOCK_ID);
		Integer intLockId = Integer.parseInt(strLockId);
		Element eleTransactionFilters = (Element) doc.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_FILTERS).item(0);
		//Changes for OMNI-4017 Start
		strSOFDepartments = eleTransactionFilters.getAttribute("SOFItemDeps");
		//Changes for OMNI-4017 End
		//Changes for OMNI-43227 Start
		strIsSTHSOFSMSRequired=eleTransactionFilters.getAttribute("STHSOFSMSRequired");
		//Changes for OMNI-43227 Start
		String strLockIdLimit = eleTransactionFilters.getAttribute(AcademyConstants.STR_LOCK_ID_LIMIT);
		Integer intLockIdLimit = Integer.parseInt(strLockIdLimit);
		String strDelayedLinesPutAheadTime = eleTransactionFilters
				.getAttribute(AcademyConstants.ATTR_DELAYED_LINES_PUT_AHEAD_TIME);
		Integer intDelayedLinesPutAheadTime = Integer.parseInt(strDelayedLinesPutAheadTime);
		
		Document transactionListXML=callgetTransactionList(env,transactionkey);
		if(transactionListXML!=null){
		NodeList tranList=transactionListXML.getElementsByTagName(AcademyConstants.ELE_TRANSACTION);	
		currElem=(Element) tranList.item(0);
		}
		
		//OMNI-40848 Start partial fulfillment email changes
		String strDataType=doc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_TYPE);
		if(currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID).equals("SEND_EMAIL_ON_INVOICE.0001.ex")){
		if(!YFCCommon.isVoid(strDataType) && strDataType.equals(AcademyConstants.STR_ORDR_HDR_KEY)) {
			log.verbose("Partial fulfillment email changes");
			String strOrderHeaderKey= doc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_KEY);
			outXML1 = callgetCompleteOrderDetails(env, strOrderHeaderKey, false);
			//OMNI-108945 Start
		
			outXML1.getDocumentElement().setAttribute("EmailTemplateType", strEmailType);
			if(!YFCObject.isVoid(strEmailType) && AcademyConstants.STR_PACK.equals(strEmailType)){
				AcademyUtil.invokeService(env, "AcademyPostPartialFulfillmentEmailForConvey", outXML1);
			}
			else {
				AcademyUtil.invokeService(env, "AcademyPostPartialFulfillmentEmail", outXML1);
			}
			
			//OMNI-108945 End
			StringBuffer notetextPF=new StringBuffer();
			
			orderElem=outXML1.getDocumentElement();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			notetextPF.append("Sent Partial Fulfilment Email  on "+dateFormat.format(date)+" to "+orderElem.getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID));
			callChangeOrder(env,strOrderHeaderKey,notetextPF);
			callregisterProcessCompletionAPI(env,taskQKey); 
			return doc;
		}
		}
		//OMNI-40848 End partial fulfillment email changes
		
		if(intLockId < intLockIdLimit){
			log.verbose("Processing......");
			
		//End WN-1560 Shipment Confirmation email - Wrong email IDs being passed
				
		//SOF :: Start WN-1814 Ready for Customer Pickup Email
		if(AcademyConstants.SOF_RDYFORCUST_PICKUP_EMAIL_TRAN_ID.equals(currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID))){
			bIsSOFEmail = true;
		}
		//Changes for OMNI-68222 Start
		shipmentKey=doc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_KEY);
		Document outXML=getShipmentListCall(env,shipmentKey);
		log.verbose("Output Of getShipMentList->"+XMLUtil.getXMLString(outXML));
		//Changes for OMNI-68222 End
		if(bIsSOFEmail){
			log.verbose("SOF -> bIsSOFEmail True!!!!!!!");
			
//Changes for OMNI-63471 Start
			
			Document docOutGetCommonCodeList = AcademyBOPISUtil.getCommonCodeList(env, AcademyConstants.STR_BOPIS_CUSTOMER_PICKUP_SLA, AcademyConstants.PRIMARY_ENTERPRISE);
			log.verbose("checkIfBOPISEmailNotToBeSent->docOutGetCommonCodeList"+XMLUtil.getXMLString(docOutGetCommonCodeList));
			String strStartTime = AcademyBOPISUtil.retrieveCodeShortDesc(env, docOutGetCommonCodeList, "BOPIS_COM_STOP_START_TIME");
			String strEndTime = AcademyBOPISUtil.retrieveCodeShortDesc(env, docOutGetCommonCodeList, "BOPIS_COM_STOP_END_TIME");
			String strEndHours = AcademyBOPISUtil.retrieveCodeShortDesc(env, docOutGetCommonCodeList, "BOPIS_COM_END_HOUR");
			//OMNI-92608, OMNI-92956 Start
			strIsRFCPEmailConsolidation = AcademyBOPISUtil.retrieveCodeShortDesc(env, docOutGetCommonCodeList, AcademyConstants.STR_IS_REMINDER_CONSOLIDATION_ENABLED);
			//OMNI-92608, OMNI-92956 END
			
			if(AcademyBOPISUtil.checkIfBOPISEmailNotToBeSent(env,strStartTime,strEndTime))
			{
			    Calendar cal = Calendar.getInstance();
			    int nowHour = cal.get(Calendar.HOUR_OF_DAY);
			    int nowMin  = cal.get(Calendar.MINUTE);
			    log.verbose("nowHour "+nowHour+" nowMin "+nowMin+" strEndHours "+strEndHours);
				String strDealay = AcademyBOPISUtil.getHoursToAddForBOPISEmail(nowHour, nowMin, strEndHours);
				String sTemp[] = strDealay.split(":");
				String strHour=null;
				String strMin = null;
				
				if(sTemp.length > 0)
				{
					strHour = sTemp[0];
					strMin = sTemp[1];
				}
				AcademyBOPISUtil.putAheadAvailableDateInTaskQForBOPIS(env, taskQKey, Integer.parseInt(strHour), Integer.parseInt(strMin));
				log.verbose("::: Email will be sent after "+strHour+" Hours ::: "+" Minutes ::: "+strMin+" "+" sTemp "+sTemp);
				return doc;
			}
			
			//Changes for OMNI-63471 End
			
			// Alternate StorePick Email changes
			/**
			 * 	[@MinLineStatus <'1500'] check is to extend available date by configured time until BOPIS shortpicked line is above scheduled status	
			 */
			NodeList nlBackOrderLines = XPathUtil.getNodeList(outXML,".//ShipmentLines/ShipmentLine/OrderLine[@MinLineStatus <'1500']");
				if (!YFCObject.isVoid(nlBackOrderLines) && nlBackOrderLines.getLength() > 0) {
					log.verbose("Shortpicked is in backordered status, delay RFCP email");
					putAheadAvailableDateInTaskQ(env, taskQKey, intDelayedLinesPutAheadTime);
					log.verbose("::: Consolidated Email will be sent after " + intDelayedLinesPutAheadTime
							+ " minutes :::");
					return doc;
				}
			// Alternate StorePick Email changes
			
			String strDataKey = doc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_KEY);
			orderElem = beforeSendingSOFRdyForCustEmail(env, strDataKey);
			// OMNI-30146 - START :: Logic to send Consolidated Email if Save The Sale Lines
			// Exists in BOPIS Flow
			if (!doNotDelayEmail) {
				putAheadAvailableDateInTaskQ(env, taskQKey, intDelayedLinesPutAheadTime);
				log.verbose("::: Consolidated Email will be sent after "+intDelayedLinesPutAheadTime+" minutes :::");
					return doc;
			}
			// OMNI-30146 - END
			
			// Alternate StorePick Email changes
			updateHasAlternateStorePickLines(outXML, orderElem);
			// Alternate StorePick Email changes
			orderHeaderKey = orderElem.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
			log.verbose("orderHeaderKey : "+orderHeaderKey);
		}else{
			log.verbose("Non-SOF -> bIsSOFEmail False!!!!!!!");
		//SOF :: End WN-1814 Ready for Customer Pickup Email
		if(currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID).equals("SEND_EMAIL_ON_INVOICE.0001.ex")){
		//shipmentKey=doc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_KEY);
		//Changes for OMNI-68222 Start
		//Document outXML=getShipmentListCall(env,shipmentKey);
		//Changes for OMNI-68222 End
		orderHeaderKey=((Element) outXML.getDocumentElement().getElementsByTagName("ShipmentLine").item(0)).getAttribute("OrderHeaderKey");
		
		//Start : OMNI-38141 : Push Notification
		if (!YFCCommon.isVoid(strDelMethod) && !strDelMethod.equals(AcademyConstants.STR_PICK)) {
			log.verbose("OMNI-38141 - Push Notification Ship Confirm Start");
			outXML1 = callgetCompleteOrderDetails(env, orderHeaderKey, false);
			outXML1.getDocumentElement().setAttribute("CurrentShipmentKey", shipmentKey);
			log.verbose("OMNI-38141 - Push Notification Ship Confirm Start - input " + XMLUtil.getXMLString(outXML1));
			AcademyUtil.invokeService(env, AcademyConstants.SERVICE_PUSH_NOTIFY_SHIP_CONFIRM, outXML1);
			log.verbose("OMNI-38141 - Push Notification Ship Confirm End");
			// Code Changes for OMNI-41165
			String strTrackingNo = SCXmlUtil.getXpathAttribute(outXML.getDocumentElement(),
								"Shipment/Containers/Container/@TrackingNo");
			if (!YFCCommon.isVoid(strTrackingNo)) {
				outXML1.getDocumentElement().setAttribute("CurrentShipmentTrackingNo", strTrackingNo);
			}
			// Code Changes for OMNI-41165
		}
		//End : OMNI-38141 : Push Notification
		//Start : OMNI-2732 : Narvar: Separate Source for BOPIS and SFS Emails
		//Start : Convey Email Changes Start OMNI-108945
		log.verbose("strEmailType ::" + strEmailType);
		if(YFCObject.isVoid(strEmailType)  && 
				(!YFCCommon.isVoid(strDelMethod) && !strDelMethod.equals(AcademyConstants.STR_PICK))){
			log.verbose("Skipping SFS Emails as EmailType is Blank");
			callregisterProcessCompletionAPI(env,taskQKey); 
			return doc;
		}
		//END : Convey Email Changes Start OMNI-108945
		//End : OMNI-2732 : Narvar: Separate Source for BOPIS and SFS Emails
		
		}else{
			
				orderHeaderKey=doc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_KEY);
			
		}
		/**
		 * CR - New WG Carrier and Prorate WG charges and Tax
		 */
		if(currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID).equals("SEND_EMAIL_RET_INVOICE.0003.ex")){
			outXML1=callgetCompleteOrderDetails(env,orderHeaderKey, true);
			// get the latest Invoice of Return Order
			getLatestOrderInvoiceDetailsAndUpdate(env,orderHeaderKey, outXML1,"");
		}
		/**
		 * OMNI-75378 & OMNI-66692
		 */
		if(currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID).equals("SEND_EMAIL_RET_REF.0003.ex")){
			String sStrOrderInvoiceKey= doc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_KEY);
			// get the Complete Order Details
			outXML1=getLatestOrderInvoiceDetailsAndUpdate(env,sStrOrderInvoiceKey,null,"SEND_EMAIL_RET_REF.0003.ex");
			orderHeaderKey=outXML1.getDocumentElement().getAttribute("OrderHeaderKey");
		}else
			outXML1=callgetCompleteOrderDetails(env,orderHeaderKey, false);
		
		orderElem=outXML1.getDocumentElement();
		
		// To Check if Alternate Contact Details are available for Pickup Confirmation
		// Email.
		// if Present, Email will be sent to the Alternate Contact as well.
		String CustomerEMailID = orderElem.getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID);

		if (!bIsSOFEmail && currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID)
				.equals(AcademyConstants.SEND_EMAIL_ON_INVOICE) && (!YFCCommon.isVoid(strDelMethod) && strDelMethod.equals(AcademyConstants.STR_PICK))) {
			
			String emailText="";
			Document docGetReasonCodesInput = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			docGetReasonCodesInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE,
					"EMAIL_CONTENT");
			docGetReasonCodesInput.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.HUB_CODE);
			Document docCommonCodeListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST,
					docGetReasonCodesInput);
			   
			List<Node> eleOrderPickUp=XMLUtil.getElementListByXpath(docCommonCodeListOutput, "/CommonCodeList/CommonCode[@CodeShortDescription='ORD_PICKED_UP']");
			
			for (int i = 1; i <= eleOrderPickUp.size(); i++)
			{
				String strCodeValue="ORD_PICKED_UP_"+i;
				emailText=emailText+XMLUtil
						.getElementByXPath(docCommonCodeListOutput,
								"/CommonCodeList/CommonCode[@CodeValue='"+ strCodeValue + "']")
						.getAttribute("CodeLongDescription");
						
			}
			orderElem.setAttribute("EmailText", emailText);
			
			if (!YFCCommon
					.isVoid(SCXmlUtil.getChildElement(outXML1.getDocumentElement(), AcademyConstants.ELE_PERSON_INFO_MARK_FOR))) {
				Element elePersonInfoMarkFor = SCXmlUtil.getChildElement(orderElem, AcademyConstants.ELE_PERSON_INFO_MARK_FOR);
				if (elePersonInfoMarkFor.hasAttribute(AcademyConstants.ATTR_EMAILID)) {
					String StrAltEmailID = elePersonInfoMarkFor.getAttribute(AcademyConstants.ATTR_EMAILID);
					log.verbose("******* Alternate Contact EmailId Available **** " + StrAltEmailID);
					CustomerEMailID = CustomerEMailID + "," + StrAltEmailID;
					orderElem.setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID, CustomerEMailID);
				}
			}
		}

		log.verbose("******** Order Element ***** " + XMLUtil.getElementXMLString(orderElem));
		if(currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID).equals("SEND_EMAIL_ON_INVOICE.0001.ex")){
			orderElem.setAttribute("CurrentShipmentKey", shipmentKey);
			appendDataForEmail(env, orderElem);
		}
		if(log.isVerboseEnabled()){
		log.verbose("getCompleteOrderDetails XML ..."+XMLUtil.getElementXMLString(orderElem));
		}
		if(currElem!=null){
			orderElem.setAttribute(AcademyConstants.ATTR_TRANS_ID_EMAIL, currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID));
		}
		}//SOF :: End WN-1814 Ready for Customer Pickup Email		
		
		//SOF :: Start WN-1814 Ready for Customer Pickup Email
		nlSOFShipments = XPathUtil.getNodeList(orderElem, AcademyConstants.XPATH_SHIPMENT_SHIPMENTTYPE);
		strTranID = currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID);
		if (!(nlSOFShipments.getLength()>0 && AcademyConstants.SEND_EMAIL_ON_INVOICE.equals(strTranID)))
		{
			log.verbose("******** Non SOF Order **********");
		//SOF :: End WN-1814 Ready for Customer Pickup Email
		
		log.verbose("******** calling send email service **********");
		boolean sentMail = callSendEmailService(env,orderElem,currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID));
		//Changes for OMNI-68222 Start
		if(bIsSOFEmail){
			log.verbose("Invoked SMS servive from Agent");
			if(outXML!=null){
			Element eleDocEle= outXML.getDocumentElement();
			Element eleShipment = SCXmlUtil.getChildElement(eleDocEle, AcademyConstants.ELE_SHIPMENT);
			Document docShipment=null;
			if(eleShipment!=null)
			{
				//OMNI-92779--start
				eleShipment.setAttribute(AcademyConstants.STR_IS_REMINDER_CONSOLIDATION_ENABLED,strIsRFCPEmailConsolidation);
				//OMNI-92779--END 
				docShipment = XMLUtil.getDocumentForElement(eleShipment);
			}
			log.verbose("DocShipMent in Agent call->"+XMLUtil.getXMLString(docShipment));
			AcademyUtil.invokeService(env, AcademyConstants.FLOW_ACADEMY_BOPIS_CHECK_AND_SEND_SMS_SERVICE, docShipment);
			}
		}
		//Changes for OMNI-68222 End
		if(sentMail){
			StringBuffer notetext=new StringBuffer();
			notetext=notetext.append("Sent ");
			//SOF :: Start WN-1814 Ready for Customer Pickup Email
			if(bIsSOFEmail){
				notetext.append("SOF Ready for Customer Pickup ");
			}
			//SOF :: End WN-1814 Ready for Customer Pickup Email
			if(currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID).equals(AcademyConstants.ACAD_LITERAL_SEND_EMAIL_CREATE)){
			notetext.append("Order Creation ");
			}
			if(currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID).equals("SEND_EMAIL_ON_CANCEL.0001.ex")){
				notetext.append("Order Cancellation ");	
			}
			if(currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID).equals("SEND_EMAIL_ON_INVOICE.0001.ex")){
				notetext.append("Shipment Confirmation ");
			}
			/**
			 * OMNI-75378 & OMNI-66692
			 */
			if(currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID).equals("SEND_EMAIL_RET_INVOICE.0003.ex")){
				notetext.append("Return Invoice ");
			}
			/**
			 * OMNI-75378 & OMNI-66692
			 */
			if(currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID).equals("SEND_EMAIL_RET_REF.0003.ex")){
				notetext.append("Return Refund ");
			}
			
			// *************** adding code to send email when White Glove Return is created  **************
			if(currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID).equals("SEND_MAIL_WG_RET.0003.ex")){
				notetext.append("White Glove Return ");
			}
			notetext.append("mail on ");
			YFCDate dt = new YFCDate();
			notetext.append(dt.getString());
			notetext.append(" to "+orderElem.getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID));
			if(log.isVerboseEnabled()){
				log.verbose("Note Text is :"+notetext.toString());
			}
			callChangeOrder(env,orderHeaderKey,notetext);
		}
		} // SOF :: WN-1814 Ready for Customer Pickup Email
		//OMNI-95359 - Starts
		else if (nlSOFShipments.getLength()>0 && AcademyConstants.SEND_EMAIL_ON_INVOICE.equals(strTranID) && 
				(!YFCCommon.isVoid(strDelMethod) && strDelMethod.equals(AcademyConstants.STR_PICK))) {
			log.verbose("******** SOF Order **********");			
			log.verbose("******** calling send email service **********");
			boolean sentMail = callSendEmailService(env,orderElem,currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID));
			StringBuffer notetext=new StringBuffer();
			notetext=notetext.append("Sent ");
			notetext.append("Shipment Confirmation ");
			notetext.append("mail on ");
			YFCDate dt = new YFCDate();
			notetext.append(dt.getString());
			notetext.append(" to "+orderElem.getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID));
			if(log.isVerboseEnabled()){
				log.verbose("Note Text is :"+notetext.toString());
			}
			callChangeOrder(env,orderHeaderKey,notetext);
			}
		//OMNI-95359 - Ends
		}
		callregisterProcessCompletionAPI(env,taskQKey); 
		return doc;
	}
	
	private Document getLatestOrderInvoiceDetailsAndUpdate(YFSEnvironment env,
			String orderHeaderKey, Document rtnOrderDetails,String strInvoiceType) throws Exception {
		log.verbose("starting method getLatestOrderInvoiceDetails of Return Order ...");
		// The getOrderInvoiceList API is not returning attributes computed at template like as TotalDiscount. Hence, need a getOrderInvoiceDetails API call
		Document docInvoiceList=null;
		env.setApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_LIST, 
				XMLUtil.getDocument("<OrderInvoiceList><OrderInvoice OrderInvoiceKey=\"\" InvoiceNo=\"\" OrderHeaderKey=\"\"/></OrderInvoiceList>"));
		/**
		 * OMNI-75378 & OMNI-66692
		 */
		if("SEND_EMAIL_RET_REF.0003.ex".equals(strInvoiceType))
		{
			log.verbose("input to getOrderInvoiceList for last invoice is :"+ XMLUtil.getXMLString(XMLUtil.getDocument("<OrderInvoice OrderInvoiceKey=\""+orderHeaderKey+"\"/>")));
			docInvoiceList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_INVOICE_LIST, XMLUtil.getDocument("<OrderInvoice OrderInvoiceKey=\""+orderHeaderKey+"\"/>"));
			String sOrderHeaderKey=SCXmlUtil.getXpathAttribute(docInvoiceList.getDocumentElement(), "OrderInvoice/@OrderHeaderKey");
			rtnOrderDetails=callgetCompleteOrderDetails(env,sOrderHeaderKey, false);
		}
		else
		{
			log.verbose("input to getOrderInvoiceList for last invoice is :"+ XMLUtil.getXMLString(XMLUtil.getDocument("<OrderInvoice OrderHeaderKey=\""+orderHeaderKey+"\"/>")));
			docInvoiceList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_INVOICE_LIST, XMLUtil.getDocument("<OrderInvoice OrderHeaderKey=\""+orderHeaderKey+"\"/>"));
					
		}
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_LIST);
		log.verbose("output of getOrderInvoiceList :"+ XMLUtil.getXMLString(docInvoiceList));
		if(docInvoiceList != null && docInvoiceList.getDocumentElement().hasChildNodes()){
			String latestInvoiceKey =""; 
					if("SEND_EMAIL_RET_REF.0003.ex".equals(strInvoiceType))
					{		
						latestInvoiceKey=orderHeaderKey;			
					}
					else
					{
						latestInvoiceKey=docInvoiceList.getDocumentElement().getAttribute(AcademyConstants.ATTR_LATEST_INVOICE_KEY);
					}
			log.verbose("input to getOrderInvoiceDetails for last invoice is :"+ XMLUtil.getXMLString(XMLUtil.getDocument("<GetOrderInvoiceDetails InvoiceKey=\""+latestInvoiceKey+"\"/>")));
			env.setApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_DETAILS, "global/template/api/getReturnOrderInvoiceDetails.ToSendEmail.xml");
			Document docInvoiceDetails = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_INVOICE_DETAILS, XMLUtil.getDocument("<GetOrderInvoiceDetails InvoiceKey=\""+latestInvoiceKey+"\"/>"));
			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_DETAILS);
			log.verbose("output of getOrderInvoiceDetails :"+ XMLUtil.getXMLString(docInvoiceDetails));
			
			//Loop through each OrderLine element in Return Order Details 
			NodeList lstOrderLines = rtnOrderDetails.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
			for(int i=0; i<lstOrderLines.getLength(); i++){
				Element eleOrderLine = (Element)lstOrderLines.item(i);
				String strOrderLineKey = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
				// Find the matching Invoice Line
				log.verbose("Find the matching Invoice Line");
				Element eleInvoiceLineDetail = (Element)XPathUtil.getNode(docInvoiceDetails, "InvoiceDetail/InvoiceHeader/LineDetails/LineDetail[@OrderLineKey='"+strOrderLineKey+"']");
				if(!YFCObject.isVoid(eleInvoiceLineDetail)){
					// Found the matching Invoice Line Details
					log.verbose("Found the matching Invoice Line Details");
					//Replace specific attribute's value with Invoiced value
					//A. Ordered Qty
					eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, eleInvoiceLineDetail.getAttribute(AcademyConstants.ATTR_RTN_SHIPPED_QTY));
					// The value on Order/OrderLine is other way round on Invoice details. Therefore, reverse the symbol
					//B. LineOverallTotals
					Element eleOLOverAllTotals = (Element)XPathUtil.getNode(eleOrderLine, AcademyConstants.ELE_LINE_OVERALL_TOTALS);
					//B.1. ExtendedPrice
					eleOLOverAllTotals.setAttribute(AcademyConstants.ATTR_EXTENDED_PRICE, modifyValue(eleInvoiceLineDetail.getAttribute(AcademyConstants.ATTR_EXTENDED_PRICE), "-"));
					//B.2 UnitPrice
					eleOLOverAllTotals.setAttribute(AcademyConstants.ATTR_UNIT_PRICE, modifyValue(eleInvoiceLineDetail.getAttribute(AcademyConstants.ATTR_UNIT_PRICE), "-"));					
				}else{
					// Not Found. Remove element from template
					log.verbose("Not Found. Remove element from template");
					eleOrderLine.getParentNode().removeChild(eleOrderLine);
					i--;
				}
			}
				//Begin - OMNI-74255 - return received listrak Email
			if(!YFCObject.isVoid(strInvoiceType)) {
			if ( !"SEND_EMAIL_RET_REF.0003.ex".equals(strInvoiceType)) {
				// Loop through each collection details
				/*
				 * Element collectionDetail = (Element)XPathUtil.getNode(docInvoiceDetails,
				 * "InvoiceDetail/InvoiceHeader/CollectionDetails"); Node collectionDetails =
				 * docInvoiceDetails.importNode(collectionDetail, true);
				 * rtnOrderDetails.getDocumentElement().appendChild(docInvoiceList);
				 * log.verbose("return order details after importing collections" +
				 * XMLUtil.getXMLString(rtnOrderDetails));
				 */
			}
			}
			//End - OMNI-74255 - return received listrak Email
			// Update Header level totals with Invoice Totals
			Element overAllTotals = (Element)XPathUtil.getNode(rtnOrderDetails, "Order/OverallTotals");
			if(!YFCObject.isVoid(overAllTotals)){
				//C. OverallTotals
				Element eleInvoiceHeader = (Element)XPathUtil.getNode(docInvoiceDetails, "InvoiceDetail/InvoiceHeader");
				//C.1. LineSubTotal
				overAllTotals.setAttribute("LineSubTotal", modifyValue(eleInvoiceHeader.getAttribute("LineSubTotal"), "-"));
				//C.2. GrandDiscount
				overAllTotals.setAttribute("GrandDiscount", modifyValue(eleInvoiceHeader.getAttribute("TotalDiscount"), "-"));
				//C.3. GrandCharges
				overAllTotals.setAttribute("GrandCharges", modifyValue(eleInvoiceHeader.getAttribute("TotalCharges"), "-"));
				//C.4. GrandTax
				overAllTotals.setAttribute("GrandTax", modifyValue(eleInvoiceHeader.getAttribute("TotalTax"), "-"));
				//C.5. GrandTotal
				overAllTotals.setAttribute("GrandTotal", modifyValue(eleInvoiceHeader.getAttribute("TotalAmount"), "-"));
				//Start Code Changes for OMNI-82782
				if ( !"SEND_EMAIL_RET_REF.0003.ex".equals(strInvoiceType)) {
				// Update OrderDate to Invoiced Date
				rtnOrderDetails.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_DATE, eleInvoiceHeader.getAttribute("DateInvoiced"));
				}
				//End Code Changes for OMNI-82782
			}			
		}
		return rtnOrderDetails;
	}

	private String modifyValue(String value, String symbol) {
		if(value.startsWith(symbol)){
			value = value.substring(1);
		}else if(Double.valueOf(value) != 0){
				value = "-"+value;
		}
		return value;
	}

	private Element appendDataForEmail(YFSEnvironment env, Element orderElem) throws Exception {
		
		//Element orderElem = orderDoc.getDocumentElement();

		double totalItemDiscount = 0.00;
		double totalShippingCharge = 0.00;
		
		Element orderLines = (Element) orderElem.getElementsByTagName("OrderLines").item(0);
		NodeList orderLinesNL = orderLines.getElementsByTagName("OrderLine"); 
		for (int i = 0; i < orderLinesNL.getLength(); i++) {
			Element orderLineNode = (Element) orderLinesNL.item(i);
			NodeList lineChargeNL = orderLineNode.getElementsByTagName("LineCharge");
			
			for (int j = 0; j < lineChargeNL.getLength(); j++) {
				
				Element LineCharge = (Element) lineChargeNL.item(j);
				String IsDiscount = "";
				String ChargeAmount = "";
				String chargeName = LineCharge.getAttribute("ChargeName");
				
				
				if ((chargeName.contains("Shipping"))) {
				String strShippingChargeAtLineLevel = LineCharge.getAttribute("ChargeAmount"); 
				if (!YFCObject.isNull(strShippingChargeAtLineLevel)) {
					if ("Y".equals(LineCharge.getAttribute("IsDiscount"))) {
						totalShippingCharge = totalShippingCharge - Double.parseDouble(strShippingChargeAtLineLevel);
					} else {
						totalShippingCharge = totalShippingCharge + Double.parseDouble(strShippingChargeAtLineLevel);
						}
					}
				}
				IsDiscount = LineCharge.getAttribute("IsDiscount");
				ChargeAmount = LineCharge.getAttribute("ChargeAmount");
				if ("BOGO".equals(LineCharge.getAttribute("ChargeName")) || 
						("DiscountCoupon".equals(LineCharge.getAttribute("ChargeName"))) || 
							("CUSTOMER_APPEASEMENT".equals(LineCharge.getAttribute("ChargeName")))) {
					if ("Y".equals(IsDiscount)) { 															
						totalItemDiscount = totalItemDiscount + Double.parseDouble(ChargeAmount);
					}
				} 
			}
		
/*		if (!YFCObject.isVoid(lineTaxNL)) {
			double shippingTaxAtLineLevel = 0.00;
			double itemTaxAtLineLevel = 0.00;
			double tax = 0.00;
				for (int k = 0; k < lineTaxNL.getLength(); k++) {
					Element eleLineTax = (Element) lineTaxNL.item(k);
					String chargeCategory =  eleLineTax.getAttribute("ChargeCategory");
						if ("Shipping".equals(chargeCategory)) {
							String strShippingTaxAtLineLevel = eleLineTax.getAttribute("Tax");
							shippingTaxAtLineLevel += Double.parseDouble(strShippingTaxAtLineLevel);
							tax += shippingTaxAtLineLevel;
						}
						if ("TAXES".equals(chargeCategory)) {
							String strItemTaxAtLineLevel = eleLineTax.getAttribute("Tax");
							itemTaxAtLineLevel += Double.parseDouble(strItemTaxAtLineLevel);
							tax += itemTaxAtLineLevel;
						}
					}
				}*/
		}

		Element overallTotals = (Element) orderElem.getElementsByTagName("OverallTotals").item(0);
		String lineSubTotal = overallTotals.getAttribute("LineSubTotal");
		String grandTax = overallTotals.getAttribute("GrandTax");
		double dLineSubTotal = 0.00;
		double dGrandTax = 0.00;
		double dTotalAmount = 0.00;
		
		if (!YFCObject.isNull(lineSubTotal)) {
			dLineSubTotal = Double.parseDouble(lineSubTotal);
		}
		if (!YFCObject.isNull(grandTax)) {
			dGrandTax = Double.parseDouble(grandTax);
		}
		
		dTotalAmount = dLineSubTotal + dGrandTax + totalShippingCharge - totalItemDiscount ;

		orderElem.setAttribute("TotalItemDiscount", Double.toString(totalItemDiscount));
		orderElem.setAttribute("TotalShippingCharge", Double.toString(totalShippingCharge));
		orderElem.setAttribute("TotalAmount", Double.toString(dTotalAmount));
		 
		return orderElem;
	}

	
	
	
	private String callgetOrderList(YFSEnvironment env, String retHeaderKey) {
		Document inpXML=null;
		Document outXML=null;
		String orderHeaderKey="";
		try {
			inpXML=XMLUtil.createDocument("Order");
			inpXML.getDocumentElement().setAttribute("OrderHeaderKey", retHeaderKey);
			Document templateXML=XMLUtil.createDocument(AcademyConstants.ELE_ORDER_LIST);
			Element orderElem=templateXML.createElement(AcademyConstants.ELE_ORDER);
			orderElem.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY," ");
			orderElem.setAttribute(AcademyConstants.ATTR_ORDER_NO," ");
			orderElem.setAttribute("DerivedFromOrderHeaderKey"," ");
			templateXML.getDocumentElement().appendChild(orderElem);
			
			env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, templateXML);
			outXML=AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, inpXML);
			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
			orderHeaderKey=((Element) outXML.getDocumentElement().getElementsByTagName("Order").item(0)).getAttribute("DerivedFromOrderHeaderKey");
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return orderHeaderKey;
	}

	//SOF :: Start WN-1814 Ready for Customer Pickup Email
	//private String getShipmentListCall(YFSEnvironment env, String shipmentKey) {
	private Document getShipmentListCall(YFSEnvironment env, String strShipmentIdentifier) {
		/*if(bIsSOFEmail)
			strShipmentIdentifier -> ShipmentNo
		else
			strShipmentIdentifier -> ShipmentKey*/
		//SOF :: End WN-1814 Ready for Customer Pickup Email
		Document inpXML=null;
		Document outXML=null;
		try {
			inpXML=XMLUtil.createDocument("Shipment");
			//SOF :: Start WN-1814 Ready for Customer Pickup Email
			//inpXML.getDocumentElement().setAttribute("ShipmentKey", shipmentKey);
			if(bIsSOFEmail){
				inpXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentIdentifier);
				inpXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
				inpXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
			}else{	
				inpXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentIdentifier);
			}
			/*
			//SOF :: Start WN-1814 Ready for Customer Pickup Email
			
			Document templateXML=XMLUtil.createDocument("Shipments");
			Element shipElem=templateXML.createElement("Shipment");
			shipElem.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, "");
			//Changes for OMNI-68222 Start
			shipElem.setAttribute(AcademyConstants.ATTR_STATUS, "");
			shipElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, "");
			shipElem.setAttribute(AcademyConstants.ATTR_SHIP_NODE, "");
			
			//Changes for OMNI-68222 End
			Element shipLinesElem=templateXML.createElement("ShipmentLines");
			shipElem.appendChild(shipLinesElem);
			Element shipLineElem=templateXML.createElement("ShipmentLine");
			shipLineElem.setAttribute("OrderHeaderKey", " ");
			shipElem.setAttribute("ShipmentKey", " ");
			//Commented code for OMNI-30147
			Element orderLineElem=templateXML.createElement("OrderLine");
			orderLineElem.setAttribute("OrderLineKey", " ");
			orderLineElem.setAttribute("OrderedQty", " ");
			orderLineElem.setAttribute("FulfillmentType", " "); //OMNI-99100
			//OMNI-53844 Start Changes
			Element orderLineExtnElem=templateXML.createElement("Extn");
			orderLineExtnElem.setAttribute("ExtnOriginalFulfillmentType","");
			orderLineElem.appendChild(orderLineExtnElem);
			//OMNI-53844 End Changes
			shipLineElem.appendChild(orderLineElem);
			//Commented code for OMNI-30147
			//Code Changes for OMNI-41165
			Element containers=templateXML.createElement("Containers");
			Element container=templateXML.createElement("Container");
			container.setAttribute("TrackingNo", " ");
			container.setAttribute("ContainerNo", " ");
			containers.appendChild(container);
			shipElem.appendChild(containers);
			//Code Changes for OMNI-41165
			//shipElem.setAttribute("OrderHeaderKey", " ");
			shipLinesElem.appendChild(shipLineElem);
			templateXML.getDocumentElement().appendChild(shipElem);
			log.verbose("GetShipmentList Template doc->"+XMLUtil.getXMLString(templateXML));
			env.setApiTemplate("getShipmentList", templateXML);
			outXML=AcademyUtil.invokeAPI(env, "getShipmentList", inpXML);
			log.verbose("GetShipmentList Output  doc->"+XMLUtil.getXMLString(outXML));
			env.clearApiTemplate("getShipmentList"); */
			env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, "global/template/api/getShipmentList.academySendEmailAgent.xml");
			outXML=AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, inpXML);
			log.verbose("GetShipmentList Output  doc->"+XMLUtil.getXMLString(outXML));
			env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
			//setting shipmentkey to environment obj
			env.setTxnObject("SHP_Key", SCXmlUtil.getChildElement(outXML.getDocumentElement(), AcademyConstants.ELE_SHIPMENT).getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
			strDelMethod = SCXmlUtil.getChildElement(outXML.getDocumentElement(), AcademyConstants.ELE_SHIPMENT).getAttribute(AcademyConstants.ATTR_DEL_METHOD);
		
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return outXML;
	}

	private void callChangeOrder(YFSEnvironment env, String orderHeaderKey, StringBuffer notetext) {
		try {
			Document changeOrderInputXML=XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			changeOrderInputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, orderHeaderKey);
			Element notesElem=changeOrderInputXML.createElement(AcademyConstants.ELE_NOTES);
			changeOrderInputXML.getDocumentElement().appendChild(notesElem);
			Element noteElem=changeOrderInputXML.createElement(AcademyConstants.ELE_NOTE);
			noteElem.setAttribute(AcademyConstants.ATTR_NOTE_TEXT, notetext.toString());
			notesElem.appendChild(noteElem);
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, changeOrderInputXML);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private Document callgetTransactionList(YFSEnvironment env, String transactionkey) {
		Document inXML,outXML=null;
		try {
			inXML=XMLUtil.createDocument(AcademyConstants.ELE_TRANSACTION);
			inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_TRANS_KEY, transactionkey);
			outXML=AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_TRANSACTION_LIST, inXML);
		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return outXML;
	}

	private void callregisterProcessCompletionAPI(YFSEnvironment env, String taskQKey) {
		try {
			
			log.verbose("********callregisterProcessCompletionAPI***** ");
			Document inXML=XMLUtil.createDocument(AcademyConstants.ELE_REG_PROCESS_COMP_INPUT);
			Element rootElem=inXML.getDocumentElement();
			rootElem.setAttribute(AcademyConstants.ATTR_KEEP_TASK_OPEN, "N");
			Element currTaskElem=inXML.createElement(AcademyConstants.ELE_CURR_TASK);
			currTaskElem.setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, taskQKey);
			rootElem.appendChild(currTaskElem);
			AcademyUtil.invokeAPI(env, AcademyConstants.API_REGISTER_PROCESS_COMPLETION, inXML);
			log.verbose("********callregisterProcessCompletionAPI END***** ");
		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
	}
	
	//Start WN-697 : Sterling to consume special characters and include them in customer-facing emails, but to remove them before settlement.
	/**
	 * Method passes PersonInfoBillTo and PersonInfoShipTo elements to convert FirstName/LastName/AddressLine1 attribute's unicode's to special char, if any.
	 * @param env
	 * @param inDoc
	 * @throws Exception
	 */
	private void convertUnicodeToSpecialChars(YFSEnvironment env, Document inDoc) throws Exception{		
		log.verbose("ConvertUnicodeToSpecialChars inDoc : " + XMLUtil.getXMLString(inDoc));
		Element elePersonInfoBillTo = null;
		Element elePersonInfoShipTo = null;
		
		elePersonInfoBillTo = XMLUtil.getElementByXPath(inDoc, AcademyConstants.XPATH_ORDER_PERSONINFOBILLTO);
		
		if(inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_TRANS_ID_EMAIL).equals(AcademyConstants.SEND_EMAIL_ON_INVOICE)){
			elePersonInfoShipTo = XMLUtil.getElementByXPath(inDoc, AcademyConstants.XPATH_ORDERLINE_PERSONINFOSHIPTO);
		}else{
			elePersonInfoShipTo = XMLUtil.getElementByXPath(inDoc, AcademyConstants.XPATH_ORDER_PERSONINFOSHIPTO);
		}
		
		AcademyUtil.convertUnicodeToSpecialChar(env, elePersonInfoBillTo, elePersonInfoShipTo, false);
		
		log.verbose("ConvertUnicodeToSpecialChars outDoc : " + XMLUtil.getXMLString(inDoc));
	}
	//End WN-697 : Sterling to consume special characters and include them in customer-facing emails, but to remove them before settlement.

	private boolean callSendEmailService(YFSEnvironment env, Element orderElem, String tranId) {
		String orderKey="";
		boolean sendMailSuccess = true;
		//System.out.println("Order Details>>>"+XMLUtil.getElementXMLString(orderElem));
		try {
			
			//Start WN-697 : Sterling to consume special characters and include them in customer-facing emails, but to remove them before settlement.
			convertUnicodeToSpecialChars(env, orderElem.getOwnerDocument());
			//End WN-697 : Sterling to consume special characters and include them in customer-facing emails, but to remove them before settlement.
			
			//SOF :: Start WN-1814 Ready for Customer Pickup Email
			if(bIsSOFEmail){
				//Changes for OMNI-43227 Start
				orderElem.setAttribute("STHSOFSMSRequired", strIsSTHSOFSMSRequired);
				//Changes for OMNI-43227 Start
				//OMNI-92956 Start
				orderElem.setAttribute("IS_REMINDER_CONSOLIDATION_ENABLED", strIsRFCPEmailConsolidation);
				//OMNI-92956 END
				AcademyUtil.invokeService(env, AcademyConstants.SERVICE_SOF_SEND_RDYFORCUST_PICKUP_EMAIL, orderElem.getOwnerDocument());
			}
			//SOF :: End WN-1814 Ready for Customer Pickup Email
			
			if(orderElem.getAttribute("TranIDForEmail").equals("SEND_EMAIL_ON_INVOICE.0001.ex")){
				// OMNI-108945 Start
					orderElem.setAttribute("EmailTemplateType", strEmailType);
					if(AcademyConstants.STR_PACK.equals(strEmailType)) {
						AcademyUtil.invokeService(env,"AcademySendEmailOnShipConfirmServiceForConvey" , orderElem.getOwnerDocument());
					}
					else {
						AcademyUtil.invokeService(env,"AcademySendEmailOnShipConfirmService" , orderElem.getOwnerDocument());
					}
					// OMNI-108945 End
			}
			if(orderElem.getAttribute("TranIDForEmail").equals("SEND_EMAIL_RET_INVOICE.0003.ex")){
				/**
				 *  CR - New White Glove Carrier and Prorate charges and Tax if Partial Receipt and invoice
				 */
				AcademyUtil.invokeService(env,"AcademySendEmailOnReturnInvoiceService" , orderElem.getOwnerDocument());
			}
			/**
			 * OMNI-75378 & OMNI-66692
			 */
			if(orderElem.getAttribute("TranIDForEmail").equals("SEND_EMAIL_RET_REF.0003.ex")){
				AcademyUtil.invokeService(env,"AcademySendEmailOnReturnRefund" , orderElem.getOwnerDocument());
			}
			if(orderElem.getAttribute("TranIDForEmail").equals("SEND_EMAIL_AFTER_CREATE.0001.ex")){
				AcademyUtil.invokeService(env,"AcademySendEmailOnCreateService" , orderElem.getOwnerDocument());
			}
			if(orderElem.getAttribute("TranIDForEmail").equals("SEND_EMAIL_ON_CANCEL.0001.ex")){
				AcademyUtil.invokeService(env,"AcademySendEmailOnOrderCancelService" , orderElem.getOwnerDocument());
			}
			
			// *************** adding code to send email when White Glove Return is created  **************
			log.verbose("******** Transaction ID for Email ***** " + orderElem.getAttribute("TranIDForEmail"));
			if(orderElem.getAttribute("TranIDForEmail").equals("SEND_MAIL_WG_RET.0003.ex"))
			{
				log.verbose("******** Inside sendEmail for WhiteGlove Return Create ***** " + XMLUtil.getElementXMLString(orderElem));
				log.verbose("******** Order XML Document ***** " + XMLUtil.getXMLString(orderElem.getOwnerDocument()));
				
				NodeList itemsNodeList = orderElem.getElementsByTagName(AcademyConstants.ELE_ITEM_DETAILS);
				if(itemsNodeList != null)
				{
					int numberOfNodes = itemsNodeList.getLength();
      				for(int i=0;i<numberOfNodes;i++)
      				{
      					Element itemDetailsElem = (Element) itemsNodeList.item(i);
      					if(itemDetailsElem != null)
      					{
          					log.verbose("******** returned item details ***** " + XMLUtil.getElementXMLString(itemDetailsElem));
          					if (((Element) itemDetailsElem.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0))
          							.getAttribute(AcademyConstants.ATTR_EXTN_WHITE_GLOVE_ELIGIBLE)
          							.equalsIgnoreCase(AcademyConstants.ATTR_Y)) 
          					{
          						log.verbose("******** Order has white glove item, calling AcademySendEmailOnWhiteGloveReturnCreationService ***** ");
          						AcademyUtil.invokeService(env,"AcademySendEmailOnWhiteGloveReturnCreationService" , orderElem.getOwnerDocument());
          						break;
          					}else{
          						sendMailSuccess = false;
          					}
      					}
      				}
				}
			}
			log.verbose("******** callSendEmailService end ***** ");
		} 
		
		catch (Exception e) {
			
			YIFApi api;
			Document envDoc = null;
			YFSEnvironment newenv;
			String strInvalidEmailError = null; //WN-1560 Shipment Confirmation email - Wrong email IDs being passed
			try {
				((YCPContext)env).rollback();
				api = YIFClientFactory.getInstance().getLocalApi();
				
				envDoc = YFCDocument.parse("<Environment userId=\"yantra\" progId=\"yantra\"/>").getDocument();
				Document env1Doc = XMLUtil.createDocument(AcademyConstants.ELE_ENV);
				env1Doc.getDocumentElement().setAttribute(AcademyConstants.ATTR_USR_ID, env.getUserId());
				env1Doc.getDocumentElement().setAttribute(AcademyConstants.ATTR_PROG_ID, env.getProgId());
				newenv = api.createEnvironment(env1Doc);

				/* Fix for Academy Defect #2070 - Whenever shipment confirmation email canï¿½t be sent, 
				 * agent server is creating notes on the order every 6 minutes. This is causing issues 
				 * in opening notes popup from call center application. So no need to create note, just 
				 * raise an alert in case of email failure */

				/*
				StringBuffer notetext=new StringBuffer();
				notetext=notetext.append("Could not send ");
				if(tranId.equals(AcademyConstants.ACAD_LITERAL_SEND_EMAIL_CREATE)){
				notetext.append("Order Creation ");
				}
				if(tranId.equals("SEND_EMAIL_ON_CANCEL.0001.ex")){
					notetext.append("Order Cancellation ");
					}
				if(tranId.equals("SEND_EMAIL_RET_INVOICE.0003.ex")){
					notetext.append("Return Invoicing ");
					}
				if(tranId.equals("SEND_EMAIL_ON_INVOICE.0001.ex")){
					notetext.append("Shipment Confirmation ");
					}
				if(tranId.equals("SEND_MAIL_WG_RET.0003.ex")){
					notetext.append("White Glove Return Creation ");
					}
				notetext.append("mail on ");
				YFCDate dt = new YFCDate();
				notetext.append(dt.getString());
				notetext.append(" to "+orderElem.getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID));
				if(log.isVerboseEnabled()){
					log.verbose("Note Text is :"+notetext.toString());
				}
				
				callChangeOrder(newenv,orderElem.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY),notetext);
				*/
				
				//Start WN-1560 Shipment Confirmation email - Wrong email IDs being passed
				strInvalidEmailError = e.getMessage();
				if(strInvalidEmailError.contains(AcademyConstants.STR_INVALID_ADDRESSES)){
					log.verbose("Invalid Addresses Alert....");
					orderElem.setAttribute(AcademyConstants.ATTR_INVALID_EMAIL_ID, AcademyConstants.STR_YES);
				}
				//End WN-1560 Shipment Confirmation email - Wrong email IDs being passed
				
				Document alertInput=XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
				XMLUtil.copyElement(alertInput, orderElem, alertInput.getDocumentElement());
				AcademyUtil.invokeService(newenv, AcademyConstants.RAISE_ALERT_ON_EMAIL_FAIL_SERVICE, alertInput);
				
				//((YFSContext)newenv).commit();
				sendMailSuccess = false;
			} catch (YIFClientCreationException e4) {
				// TODO Auto-generated catch block
				e4.printStackTrace();
			} catch (SAXException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (SQLException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			} catch (Exception e5) {
				// TODO Auto-generated catch block
				e5.printStackTrace();
			}
             
			//Start WN-1560 Shipment Confirmation email - Wrong email IDs being passed
			//throw new YFSException(e.getMessage());
			if(!strInvalidEmailError.contains(AcademyConstants.STR_INVALID_ADDRESSES)){
				log.verbose("Invalid Addresses....");
				throw new YFSException(e.getMessage());
			}
			//End WN-1560 Shipment Confirmation email - Wrong email IDs being passed
			
			}
		return sendMailSuccess;
	}

	private void callAlertService(YFSEnvironment env, Document outXML) {
		try {
			AcademyUtil.invokeService(env, "TestPublish", outXML);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private Document callgetCompleteOrderDetails(YFSEnvironment env, String orderHeaderKey, boolean isRtnInvoice) {
		Document inputXML=null;
		Document outXML=null;
		try {
			inputXML = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			inputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, orderHeaderKey);
			inputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
			if(isRtnInvoice)
				env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, "global/template/api/getOrderDetails.ToSendReturnInvoiceEmail.xml");
			//SOF :: Start WN-1814 Ready for Customer Pickup Email
			//else
				//env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, "global/template/api/getCompleteOrderDetails.ToSendEmail.xml");
			else{
				if(bIsSOFEmail){
					env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, AcademyConstants.STR_SOF_TEMPLATEFILE_GETCOMPLETEORDER_DETAILS);
				}else{
					env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, "global/template/api/getCompleteOrderDetails.ToSendEmail.xml");	
				}
			}
			//SOF :: End WN-1814 Ready for Customer Pickup Email
			
			/*Document templateXML=XMLUtil.createDocument(AcademyConstants.ELE_ORDER_LIST);
			Element orderElem=templateXML.createElement(AcademyConstants.ELE_ORDER);
			orderElem.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY," ");
			orderElem.setAttribute(AcademyConstants.ATTR_ORDER_NO," ");
			orderElem.setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID," ");
			templateXML.getDocumentElement().appendChild(orderElem);*/
			
			//env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, templateXML);
			//outXML=AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, inputXML);
			//env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
			//outXML=AcademyUtil.invokeService(env, "AcademyGetCompleteOrderDetailsService", inputXML);
			outXML = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, inputXML);
			//Changes for OMNI-4017 Start
			if (bIsSOFEmail) {
				outXML = AcademyUtil.validateAndAppendShowCurbsideInstructions(outXML, strSOFDepartments);
			}
			//Changes for OMNI-4017 End
			log.verbose(XMLUtil.getXMLString(outXML));
			env.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS);
		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return outXML;
	}
	//SOF :: Start WN-1814 Ready for Customer Pickup Email
	/**
	 * Form the element with all the required data for sending SOF 'Ready For Customer Pickup' email
	 * @param strDataKey
	 * @return eleGetCompleteOrderDetailsOutput
	 * @throws Exception
	 */	
	private Element beforeSendingSOFRdyForCustEmail(YFSEnvironment env, String strDataKey) throws Exception{
		log.verbose("Entering  beforeSendingSOFRdyForCustEmail()");
		Document docOutGetCompleteOrderDetails = null;
		Element eleGetCompleteOrderDetailsOutput = null;
		Element eleShipment = null;
		String strShipmentNo = null;
		String strShipmentKey = null;
		String strOHK = null;		
		String emailText="";
		String actualPickupDate = null;

		//OMNI-30146
		boolean noRFCPLine = false;
		log.verbose("DataKey(ShipmentNo_RdyForPickQty) :" + strDataKey);
		
		log.verbose("DataKey(ShipmentNo_RdyForPickQty) :" + strDataKey);
		if (strDataKey.contains("_")) {
			strShipmentNo = strDataKey.substring(0, strDataKey.indexOf(AcademyConstants.STR_UNDERSCORE));
		} else {
			strShipmentNo = strDataKey;
		}
		
		log.verbose("strShipmentNo :" + strShipmentNo);
		
		Document outXML = getShipmentListCall(env, strShipmentNo);
		log.verbose("strOHK :" + strOHK);
		//Commented code for OMNI-30147
		strOHK=((Element) outXML.getDocumentElement().getElementsByTagName("ShipmentLine").item(0)).getAttribute("OrderHeaderKey");
		docOutGetCompleteOrderDetails = callgetCompleteOrderDetails(env, strOHK, false);
		eleGetCompleteOrderDetailsOutput = docOutGetCompleteOrderDetails.getDocumentElement();
		if(!YFCCommon.isVoid(eleGetCompleteOrderDetailsOutput))
			{
			 String strOrderedQuantity = SCXmlUtil.getXpathAttribute(outXML.getDocumentElement(),
					"Shipment/ShipmentLines/ShipmentLine/OrderLine[@OrderedQty > '0']/@OrderedQty");
			 if (YFCCommon.isVoid(strOrderedQuantity)) {
				 eleGetCompleteOrderDetailsOutput.setAttribute("HasAllShipmentLinesCancelled", "Y");
				}
			else
				{
				 eleGetCompleteOrderDetailsOutput.setAttribute("HasAllShipmentLinesCancelled", "N");
				}
			// OMNI-99100 - Starts
			String strFulfillmentType = SCXmlUtil.getXpathAttribute(outXML.getDocumentElement(),
					"Shipment/ShipmentLines/ShipmentLine/OrderLine[@FulfillmentType='SOF']/@FulfillmentType");
			String strDeliveryMethod = SCXmlUtil.getXpathAttribute(outXML.getDocumentElement(),
					"Shipment/@DeliveryMethod");
			log.verbose("FulfillmentType ::" + strFulfillmentType);
			log.verbose("DeliveryMethod ::" + strDeliveryMethod);
			if (!YFCCommon.isVoid(strFulfillmentType) && ("SOF".equals(strFulfillmentType))
					&& !YFCCommon.isVoid(strDeliveryMethod) && "PICK".equals(strDeliveryMethod)) {
				eleGetCompleteOrderDetailsOutput.setAttribute("IsDSVSOFShipment", "Y");
			} else {
				eleGetCompleteOrderDetailsOutput.setAttribute("IsDSVSOFShipment", "N");
			}
			// OMNI-99100 - Ends
			}
		log.verbose("getCompleteOrderDetails Output : " + XMLUtil.getElementXMLString(eleGetCompleteOrderDetailsOutput));		
		//Commented code for OMNI-30147
		
		
		// OMNI-30146 - START :: Logic to send Consolidated Email if Save The Sale Lines
				// Exists in BOPIS Flow
				//OMNI-53171 start changes
				//Element eleSaveTheSaleLineInRFCPStatus = (Element) XPathUtil.getNode(eleGetCompleteOrderDetailsOutput,
						//"/Order/OrderLines/OrderLine[@FulfillmentType='STS' and @MinLineStatus='3350.400']");
		 		 Element eleSaveTheSaleLineInRFCPStatus = (Element) XPathUtil.getNode(eleGetCompleteOrderDetailsOutput,
					"/Order/OrderLines/OrderLine[@FulfillmentType='STS' and @MinLineStatus='3350.400' and ./Extn[@ExtnOriginalFulfillmentType='BOPIS']]");
		 		//OMNI-53171 End changes
				NodeList nlSaveTheSaleLines = XPathUtil.getNodeList(eleGetCompleteOrderDetailsOutput,
						"/Order/OrderLines/OrderLine/Extn[@ExtnOriginalFulfillmentType='BOPIS']");
				if (YFCObject.isVoid(eleSaveTheSaleLineInRFCPStatus) && nlSaveTheSaleLines.getLength() > 0) {
					boolean triggerEmail = checkStatusOfSaveTheSaleLines(nlSaveTheSaleLines);
					doNotDelayEmail = triggerEmail;
					log.verbose("Consolidated Email Should be triggered? :: " + triggerEmail);
					if (!triggerEmail) {
						return eleGetCompleteOrderDetailsOutput;
					}
				}
		// OMNI-30146 - END
		eleShipment = (Element) XPathUtil.getNode(eleGetCompleteOrderDetailsOutput, "/Order/Shipments/Shipment[@ShipmentNo='" + strShipmentNo + "']");
		strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		boolean bHasCancelledLines = false;		
		// OMNI-50736 - Start changes - Item details not populated for SOF line
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		Date dtCurrentDate = cal.getTime();
		String strCurrentDate=sdf.format(dtCurrentDate);
		String  currentMON=formatDate(strCurrentDate, AcademyConstants.STR_DATE_TIME_PATTERN,"MMM");
		String  currentDate=formatDate(strCurrentDate, AcademyConstants.STR_DATE_TIME_PATTERN,"dd");
		
		eleGetCompleteOrderDetailsOutput.setAttribute("CurrentDate", setOrderPlacedDate(docOutGetCompleteOrderDetails));
		// OMNI-50736 - End changes - Item details not populated for SOF line
		
		if (eleShipment.getAttribute("DeliveryMethod").equals("PICK")) {
			
			Document docGetReasonCodesInput = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			docGetReasonCodesInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE,
					"EMAIL_CONTENT");
			docGetReasonCodesInput.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.HUB_CODE);
			Document docCommonCodeListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST,
					docGetReasonCodesInput);
			   
			List<Node> eleOrderPickUp=XMLUtil.getElementListByXpath(docCommonCodeListOutput, "/CommonCodeList/CommonCode[@CodeShortDescription='ORD_READY_FOR_PICK']");
			
			for (int i = 1; i <= eleOrderPickUp.size(); i++)
			{
				String strCodeValue="ORD_READY_FOR_PICK_"+i;
				emailText=emailText+XMLUtil
						.getElementByXPath(docCommonCodeListOutput,
								"/CommonCodeList/CommonCode[@CodeValue='"+ strCodeValue + "']")
						.getAttribute("CodeLongDescription");
						
			}
			
			
			eleShipment = (Element) XPathUtil.getNode(eleGetCompleteOrderDetailsOutput,
					"/Order/Shipments/Shipment[@ShipmentNo='" + strShipmentNo
							+ "']/AdditionalDates/AdditionalDate[@DateTypeId='ACADEMY_MAX_CUSTOMER_PICK_DATE']");
			if(!YFCCommon.isVoid(eleShipment)) {
				actualPickupDate = eleShipment.getAttribute("ActualDate");
			}
			
			//OMNI-30146 -START In case of  only STS and Cancellation line,stamp actualdate as ExtnInitialPromiseDate
			//as if no RFCP lines exists, there are no Additional dates stamped.
			if(YFCCommon.isVoid(actualPickupDate)){
				log.verbose("Stamping Actual Pick Date as ExtnInitialPromiseDate :::");
				Element eleExtn = (Element) XPathUtil.getNode(eleGetCompleteOrderDetailsOutput,
						"/Order/OrderLines/OrderLine/Extn[@ExtnOriginalFulfillmentType='BOPIS']");
				if (YFCCommon.isVoid(eleExtn)) {
					actualPickupDate = strCurrentDate;
				} else {
					actualPickupDate = eleExtn.getAttribute("ExtnInitialPromiseDate");
				}
				emailText = "Your order is delayed. You can now pick up your purchase on $$$$.";
				noRFCPLine = true;
			}
			log.verbose("actualPickupDate :: "+actualPickupDate);
			//OMNI-30146 -END
			String  actualPickupDateMON=formatDate(actualPickupDate, AcademyConstants.STR_DATE_TIME_PATTERN,"MMM");
			String  actualPickupDateDate=formatDate(actualPickupDate, AcademyConstants.STR_DATE_TIME_PATTERN,"dd");
			
			//Start of defect BOPIS - 1111 					
			actualPickupDate = formatDate(actualPickupDate, AcademyConstants.STR_DATE_TIME_PATTERN,"M/d/yy");			
							
			if(!YFCCommon.isVoid(actualPickupDate)){
			//actualPickupDate=actualPickupDate.replace("0", "");
			}
			//End  of defect BOPIS - 1111 
			
			emailText = emailText.replace("$$$$", actualPickupDate);
			
			// OMNI-50736 - Start changes - Item details not populated for SOF line
			/*Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			Date dtCurrentDate = cal.getTime();
			String strCurrentDate=sdf.format(dtCurrentDate);
			String  currentMON=formatDate(strCurrentDate, AcademyConstants.STR_DATE_TIME_PATTERN,"MMM");
			String  currentDate=formatDate(strCurrentDate, AcademyConstants.STR_DATE_TIME_PATTERN,"dd");
			// OMNI-50736 - End changes - Item details not populated for SOF line
			
			eleGetCompleteOrderDetailsOutput.setAttribute("CurrentDate", currentMON+" "+currentDate);*/
			eleGetCompleteOrderDetailsOutput.setAttribute("PickUpUntilDate", actualPickupDateMON+" "+actualPickupDateDate);
			eleGetCompleteOrderDetailsOutput.setAttribute("EmailText", emailText);
			eleGetCompleteOrderDetailsOutput.setAttribute("EmailType", "RFCP");
             
			//checking for alternate person, if it present then  append alternate emailID with primary emailID  separated by commo(,)
			
			if(!YFCCommon.isVoid(SCXmlUtil.getChildElement(eleGetCompleteOrderDetailsOutput, AcademyConstants.ELE_PERSON_INFO_MARK_FOR))) {
				Element elePersonInfomarkFor = SCXmlUtil.getChildElement(eleGetCompleteOrderDetailsOutput, AcademyConstants.ELE_PERSON_INFO_MARK_FOR);
				String strToEmailId = eleGetCompleteOrderDetailsOutput.getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID);
				String strAlternateEmailID = elePersonInfomarkFor.getAttribute(AcademyConstants.ATTR_EMAILID);
				strToEmailId = strToEmailId+","+strAlternateEmailID;
				eleGetCompleteOrderDetailsOutput.setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID, strToEmailId);
				
			}
	// OMNI-4017, 5888, 5885 BOPIS: Cancel Email consolidation at Order level for cancellations - START
		Element eleShipmentForCancelCheck = (Element) XPathUtil.getNode(eleGetCompleteOrderDetailsOutput, "/Order/Shipments/Shipment[@ShipmentNo='" + strShipmentNo + "']");
		 NodeList nlShipmentLineList = eleShipmentForCancelCheck.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		 for(int index=0; index<nlShipmentLineList.getLength(); index++)
		 {
			 Element eleShipmentLine = (Element) nlShipmentLineList.item(index);
			 String strShortageQty= eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHORTAGE_QTY);
			 if(!YFCObject.isVoid(strShortageQty))
			 {
				 double dShortageQty =Double.parseDouble(strShortageQty);
				 if(dShortageQty>0.0)
				 {
					 bHasCancelledLines = true;
					 break;
				 }
			 }
			
			 
		 }
	// OMNI-4017, 5888, 5885 BOPIS: Cancel Email consolidation at Order level for cancellations - END
		
		//OMNI-30146 - START Shorted line can go to STS flow
			// this logic invoked only for BOPIS flow for sending Consolidated email
			if (YFCObject.isVoid(eleSaveTheSaleLineInRFCPStatus) && nlSaveTheSaleLines.getLength() > 0) {
				bHasCancelledLines = false;
				// if only SaveTheSale Lines are there bHasCancelledLines will be false
				// SaveTheSale Lines may or may not get cancelled.
				for (int index = 0; index < nlShipmentLineList.getLength(); index++) {
					Element eleShipmentLine = (Element) nlShipmentLineList.item(index);
					String strShortageQty = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHORTAGE_QTY);
					if (!YFCObject.isVoid(strShortageQty)) {
						double dShortageQty = Double.parseDouble(strShortageQty);
						if (dShortageQty > 0.0) {
							String strOrderLineKey = eleShipmentLine.getAttribute("OrderLineKey");
							Element eleOrderLineExtn = (Element) XPathUtil.getNode(eleGetCompleteOrderDetailsOutput,
									"/Order/OrderLines/OrderLine[@OrderLineKey='" + strOrderLineKey
											+ "']/Extn[@ExtnOriginalFulfillmentType='BOPIS']");
							if (YFCObject.isVoid(eleOrderLineExtn)) {
								bHasCancelledLines = true;
								break;
							}

						}
					}

				}
				// Logic To handle the scenario - if all the SaveTheSale lines get Cancelled
				// before TO Shipment created
				Element eleCancelNodes = (Element) XPathUtil.getNode(eleGetCompleteOrderDetailsOutput,
						"/Order/OrderLines/OrderLine[@MaxLineStatus='9000']/Extn[@ExtnOriginalFulfillmentType='BOPIS']");
				if (!YFCObject.isVoid(eleCancelNodes)) {
					bHasCancelledLines = true;
				}
			}
			//OMNI-30146 - END  
			
		}
		eleGetCompleteOrderDetailsOutput.setAttribute(AcademyConstants.STR_CURRENT_SHIPMENTKEY, strShipmentKey);
		// OMNI-4017 BOPIS : Cancellation Emails to be order level - START
		log.verbose("HasCancelledLines"+ bHasCancelledLines);
		if(bHasCancelledLines)
		{
		eleGetCompleteOrderDetailsOutput.setAttribute(AcademyConstants.ATTR_HAS_CANCELLED_LINES, AcademyConstants.STR_YES);
		String strCancellationEmailText= "Please review the below update as we were unable to fulfill some of the item(s) on your order as they are currently out of stock. Heads Up! After " +actualPickupDate +", the remaining item(s) on your order will be canceled.";
		// OMNI-30146 - START
			if (noRFCPLine) {
				strCancellationEmailText = "Please review the below update as we were unable to fulfill some of the item(s) on your order as they are currently out of stock. The remaining item(s) on your order are delayed and will be available for pick up on "
						+ actualPickupDate;
			}
			if (noRFCPLine && noOfSaveTheSaleLinesAllocated == 0) {
				strCancellationEmailText = "We were unable to fulfill your order as Item(s) are currently out of stock. ";
			}
		// OMNI-30146 -END
		eleGetCompleteOrderDetailsOutput.setAttribute(AcademyConstants.ATTR_CANCELLATION_EMAIL_TEXT, strCancellationEmailText);
		}
		// OMNI-4017, 5888, 5885 BOPIS: Cancel Email consolidation at Order level for cancellations - END
		eleGetCompleteOrderDetailsOutput.setAttribute(AcademyConstants.ATTR_TRANS_ID_EMAIL, AcademyConstants.SOF_RDYFORCUST_PICKUP_EMAIL_TRAN_ID);	
		log.verbose("Exiting  beforeSendingSOFRdyForCustEmail()"+ XMLUtil.getElementXMLString(eleGetCompleteOrderDetailsOutput));
		
		//OMNI-53844 Start changes
		NodeList nlSaveTheSaleLinesInShipment = XPathUtil.getNodeList(outXML.getDocumentElement(),
				"/Shipments/Shipment/ShipmentLines/ShipmentLine/OrderLine/Extn[@ExtnOriginalFulfillmentType='BOPIS']");
		//OMNI-30146 -START
		//if(noOfSaveTheSaleLinesAllocated>0) {
		if(noOfSaveTheSaleLinesAllocated>0 && nlSaveTheSaleLinesInShipment.getLength()>0 ) {
		//OMNI-53844 End changes
			appendDelayedLineDetails(eleGetCompleteOrderDetailsOutput);
		}
		//OMNI-30146 - END
		return eleGetCompleteOrderDetailsOutput;
		
	}
	
	/**
	 * Format order date in required format(MMM DD)
	 * 
	 * @param eleIndoc
	 */
	private String setOrderPlacedDate(Document inDoc) {
		Date orderDate = null;
		String strOrderDate = inDoc.getDocumentElement().getAttribute("OrderDate");
		strOrderDate = strOrderDate.substring(0, 10);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String strRequiredDate = "";
		try {
			orderDate = sdf.parse(strOrderDate);
			strRequiredDate = orderDate.toString();
			strRequiredDate = strRequiredDate.substring(4, 10);
			log.verbose("strRequiredDate : " + strRequiredDate);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strRequiredDate;

	}
	
	/**
	 * OMNI-30146 This method will return true to send Consolidate Email to
	 * customer, if both of the below two conditions satisfied: 1. At least one
	 * 'Save The Sale' Line Exist. 2. All 'Save The Sale' Lines either get allocated
	 * or get cancelled.
	 * 
	 * @param nodesOrderLineExtn
	 * @return
	 */
	private boolean checkStatusOfSaveTheSaleLines(NodeList nodesOrderLineExtn) {
		boolean triggerEmail = true;
		int noOfSaveTheSaleLines = 0;
		int noOfSaveTheSaleLinesGotCancelled = 0;
		try {
			log.verbose("********checkStatusOfSaveTheSaleLines START ******* ");

			
			if (!YFCObject.isVoid(nodesOrderLineExtn) && nodesOrderLineExtn.getLength() > 0) {
				triggerEmail = false;
				noOfSaveTheSaleLines = nodesOrderLineExtn.getLength();
				for (int i = 0; i < noOfSaveTheSaleLines; i++) {
					Element eleOrderLine = (Element) nodesOrderLineExtn.item(i).getParentNode();
					log.verbose(i + " ::::: Save The Sale OrderLine :::::" + XMLUtil.getElementXMLString(eleOrderLine));
					String minLineStatus = eleOrderLine.getAttribute("MinLineStatus");
					log.verbose(i + ". MinLineStatus for Save The Sale OrderLine ::::: " + minLineStatus);
					
					//Changes for OMNI-63470
					/*if ("2160.00.01".equals(minLineStatus)) {*/
					if (!StringUtil.isEmpty(minLineStatus) && minLineStatus.startsWith(YantriksConstants.V_STATUS_2160_00_01)) {
						noOfSaveTheSaleLinesAllocated++;
					} else if ("9000".equals(minLineStatus)) {
						
								noOfSaveTheSaleLinesGotCancelled++;
					
					}
				}
			}
			int noOfSTSLinesAllocatedOrCancelled = noOfSaveTheSaleLinesAllocated + noOfSaveTheSaleLinesGotCancelled;

			log.verbose("Number of Save The Sale Lines exists :: " + noOfSaveTheSaleLines);
			log.verbose("Number of Save The Sale lines got allocated :: " + noOfSaveTheSaleLinesAllocated);
			log.verbose("Number of Save The Sale lines got cancelled :: " + noOfSaveTheSaleLinesGotCancelled);
			if (noOfSTSLinesAllocatedOrCancelled == noOfSaveTheSaleLines) {
				triggerEmail = true;
			}
			
			log.verbose("********checkStatusOfSaveTheSaleLines END ******* ");

		} catch (Exception e) {

			e.printStackTrace();
		}
		return triggerEmail;
	}

	/**
	 * OMNI-30146 This method put ahead the AvailableDate in YFS_TASK_Q, if Save The
	 * Sale lines has not been allocated or cancelled.
	 * 
	 * @param env
	 * 
	 * @param strTaskQKey
	 * @param intDelayedLinesPutAheadTime 
	 * @throws ParserConfigurationException
	 */
	private void putAheadAvailableDateInTaskQ(YFSEnvironment env, String strTaskQKey, Integer intDelayedLinesPutAheadTime) {
		try {
			log.verbose("********putAheadAvailableDateInTaskQ Start ******* ");

			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			log.verbose("Current Date ::: " + sdf.format(cal.getTime()));
			cal.add(Calendar.MINUTE, intDelayedLinesPutAheadTime);
			Date availableDate = cal.getTime();
			String strAvailableDate = sdf.format(availableDate);
			log.verbose("AvailableDate to be stamped :::" + strAvailableDate);
			Document docManageTaskQueue = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
			Element eleManageTaskQueue = docManageTaskQueue.getDocumentElement();
			eleManageTaskQueue.setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, strTaskQKey);
			eleManageTaskQueue.setAttribute("AvailableDate", strAvailableDate);
			log.verbose("***** Input doc to manageTaskQueue ***** "
					+ XMLUtil.getElementXMLString(docManageTaskQueue.getDocumentElement()));
			AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_TASK_QUEUE, docManageTaskQueue);
			log.verbose("********putAheadAvailableDateInTaskQ END ******* ");
		} catch (ParserConfigurationException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

	}
	
	/**
	 * OMNI-30146
	 * This  method will append information on Delayed Orderlines (Save The Sale Lines)
	 * 
	 * @param eleGetCompleteOrderDetailsOutput
	 */
	private Element appendDelayedLineDetails(Element eleGetCompleteOrderDetailsOutput) {
		
		try {
			log.verbose("********appendDelayedLineDetails START ******* ");
			Document doc = eleGetCompleteOrderDetailsOutput.getOwnerDocument();
			eleGetCompleteOrderDetailsOutput.setAttribute("HasDelayedLines", "Y");
			String strOrderNo = eleGetCompleteOrderDetailsOutput.getAttribute("OrderNo");
			Element elePersonInfoBillTo = (Element) XPathUtil.getNode(eleGetCompleteOrderDetailsOutput,
					"/Order/PersonInfoBillTo");
			String strZipCode = elePersonInfoBillTo.getAttribute("ZipCode");
			NodeList nodesOrderLineExtn = XPathUtil.getNodeList(eleGetCompleteOrderDetailsOutput,
					"/Order/OrderLines/OrderLine/Extn[@ExtnOriginalFulfillmentType='BOPIS']");
			if (!YFCObject.isVoid(nodesOrderLineExtn) && nodesOrderLineExtn.getLength() > 0) {
				
				for (int i = 0; i < nodesOrderLineExtn.getLength(); i++) {
					Element eleExtn = (Element) nodesOrderLineExtn.item(i);
					String strExtnInitialPromiseDate = eleExtn.getAttribute("ExtnInitialPromiseDate");
					Element eleOrderLine = (Element) nodesOrderLineExtn.item(i).getParentNode();
					String minLineStatus = eleOrderLine.getAttribute("MinLineStatus");
					//Changes For OMNI-63470
					/*if ("2160.00.01".equals(minLineStatus)) {*/
					if(!StringUtil.isEmpty(minLineStatus) && minLineStatus.startsWith(YantriksConstants.V_STATUS_2160_00_01)){
						String pickUpDateForDelayedLine = formatDate(strExtnInitialPromiseDate, AcademyConstants.STR_DATE_TIME_PATTERN,"M/d/yy");
						Element eleLineDetailsText = doc.createElement("LineDetailsText");
						eleLineDetailsText.setAttribute("Text", "This line is delayed and be available for pick up on "+pickUpDateForDelayedLine);
						eleLineDetailsText.setAttribute("CancellationUrl",
								"https://uat7www.academy.com/myaccount/orderSearch/" + strOrderNo + "/" + strZipCode
										+ "?shipmentUpdate=false");
						XMLUtil.appendChild(eleOrderLine, eleLineDetailsText);
					}
				}
			}
			
			log.verbose(" Email XML after appending delayed line details :::::" + XMLUtil.getElementXMLString(eleGetCompleteOrderDetailsOutput));

			log.verbose("********appendDelayedLineDetails END ******* ");

		} catch (ParserConfigurationException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
		return eleGetCompleteOrderDetailsOutput;
		
	}
	
	//SOF :: End WN-1814 Ready for Customer Pickup Email
	
	
	// SOF :: End WN-1814 Ready for Customer Pickup Email
		// method to change the format of date
		private String formatDate(String date, String fromDatePattern, String toDatePattern) {
			try {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fromDatePattern);
				Date parsedDate = simpleDateFormat.parse(date);
				simpleDateFormat.applyPattern(toDatePattern);
				date = simpleDateFormat.format(parsedDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			return date;
		}


	// Alternate StorePick Email changes
	/**
	 * This method updates getCompleteOrderDeatils root with
	 * HasAlternateStorePickLines=N/Y when current shipment has active
	 * AlternatePickUp lines.
	 *
	 * @param getShipmentList
	 * @param eleGetCompleteOrderDetailsOutput
	 * @throws Exception
	 */
	private void updateHasAlternateStorePickLines(Document getShipmentList, Element eleGetCompleteOrderDetailsOutput)
			throws Exception {
		NodeList nlASPLines = XPathUtil.getNodeList(getShipmentList, ".//ShipmentLine/OrderLine/Extn[@ExtnIsASP='Y']");
		if (!YFCCommon.isVoid(nlASPLines) && nlASPLines.getLength() > 0) {
			eleGetCompleteOrderDetailsOutput.setAttribute("HasAlternateStorePickLines", "Y");
			appendPendingLinesDetailsText(getShipmentList, eleGetCompleteOrderDetailsOutput);
			areAllDelayedLinesAS(eleGetCompleteOrderDetailsOutput);
		}
	}

	/**
	 * This method add URL_CancelASItems for AlternatePickLines below RFCP status.
	 *
	 * @param getShipmentList
	 * @param eleGCOD
	 */
	private void appendPendingLinesDetailsText(Document getShipmentList, Element eleGCOD) {
		log.beginTimer("appendDelayedAlternateStorePicklines");
		try {
			boolean isOriginalBOPIS = AcademyEmailUtil.isOriginalBOPIS(eleGCOD.getOwnerDocument());
			Document docGCOD = eleGCOD.getOwnerDocument();
			String strOrderNo = eleGCOD.getAttribute("OrderNo");
			Element elePersonInfoBillTo = (Element) XPathUtil.getNode(eleGCOD, "/Order/PersonInfoBillTo");
			String strZipCode = elePersonInfoBillTo.getAttribute("ZipCode");
			NodeList nlASPLines = XPathUtil.getNodeList(getShipmentList,
					".//ShipmentLine/OrderLine/Extn[@ExtnIsASP='Y']/..");
			updateCancelASItemsURL(eleGCOD, strOrderNo, strZipCode);
			if (!YFCObject.isVoid(nlASPLines) && nlASPLines.getLength() > 0) {
				for (int i = 0; i < nlASPLines.getLength(); i++) {
					Element eleOrderLine = (Element) nlASPLines.item(i);
					String strOrderLineKey = eleOrderLine.getAttribute("OrderLineKey");
					String strMaxLineStatus = eleOrderLine.getAttribute("MaxLineStatus");
					Element eleOdrLine = (Element) XMLUtil.getNode(eleGCOD,
							".//OrderLines/OrderLine[@OrderLineKey='" + strOrderLineKey + "']");

					if (!strMaxLineStatus.equalsIgnoreCase(AcademyConstants.VAL_CANCELLED_STATUS) && isOriginalBOPIS) {
						eleGCOD.setAttribute(AcademyConstants.ATTR_HAS_PENDING_LINES, AcademyConstants.STR_YES);
					}
				     	Element eleLineDetailsText = docGCOD.createElement("LineDetailsText");
						updateCancelASItemsURL(eleLineDetailsText, strOrderNo, strZipCode);
						XMLUtil.appendChild(eleOdrLine, eleLineDetailsText);
				}
			}

		} catch (Exception e) {
			log.error("Exception in appendPendingLinesDetailsText :: \n" + e.toString());
			throw new YFSException(e.toString());
		}
		log.endTimer("appendDelayedAlternateStorePicklines");
	}

	/**
	 * This method read property from customeroverides.properies and update
	 * Alternate Store Pick Cancellation URL.
	 *
	 * @param inElement
	 * @param strOrderNo
	 * @param strZipCode
	 */
	private void updateCancelASItemsURL(Element inElement, String strOrderNo, String strZipCode) {
		String strASItemsCancelURL = YFSSystem.getProperty("URL_CancelASItems");
		if (!YFCCommon.isVoid(strASItemsCancelURL)) {
			strASItemsCancelURL = strASItemsCancelURL.replace("@@@@", strOrderNo);
			strASItemsCancelURL = strASItemsCancelURL.replace("$$$$", strZipCode);
			inElement.setAttribute("URL_CancelASItems", strASItemsCancelURL);
		}
	}

	  /**
	 * This Method Set <Strong>AllDelayedLinesAS=Y</Strong> when there is only AS line in Pending on Original BOPIS Shipment
	 * @param eleGetCompleteOrderDetailsOutput
	 * @throws Exception 
	 * @throws DOMException 
	 */
	private void areAllDelayedLinesAS(Element eleGetCompleteOrderDetailsOutput) throws Exception {
		Document docGCOD = eleGetCompleteOrderDetailsOutput.getOwnerDocument();
		if (hasOnlyPendingASlines(docGCOD) && !AcademyEmailUtil.isAlternateLineRFCP(docGCOD)) {
			log.verbose("Shipment is full shortpicked & has only Alternate Store Pick Lines, Delay email not applicable for AS lines");
			eleGetCompleteOrderDetailsOutput.setAttribute("AllDelayedLinesAS", "Y");
		}
	}
	
	/**
	 * This method return <strong> TRUE </strong> when full shipment is <strong>shortpicked</strong> but converted to <strong>only AS lines.</strong>
	 * </br> Default return <strong> FALSE </strong>
	 * @param docGetShipmentList
	 * @return
	 * @throws Exception
	 */
	private boolean hasOnlyPendingASlines(Document docGetShipmentList) throws Exception {
		if (isShipmentCancelled(docGetShipmentList)) {
			String currentShipmentKey = XPathUtil.getString(docGetShipmentList, "/Order/@CurrentShipmentKey");
			NodeList ndOrderLines =  XPathUtil.getNodeList(docGetShipmentList, "/Order/Shipments/Shipment[@ShipmentKey='" + currentShipmentKey + "']/ShipmentLines/ShipmentLine/OrderLine[@MaxLineStatus!='9000']");
			NodeList ndASOrderLines = XPathUtil.getNodeList(docGetShipmentList, "/Order/Shipments/Shipment[@ShipmentKey='" + currentShipmentKey + "']/ShipmentLines/ShipmentLine/OrderLine[@MaxLineStatus!='9000']/Extn[@ExtnIsASP='Y']");
			int iOrderLines = ndOrderLines.getLength();
			int iASOrderLines = ndASOrderLines.getLength();
			if (iOrderLines == iASOrderLines) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This Method return <strong> TRUE </strong> when full Shipment is <strong>Cancelled</strong> 
	 * @param docGetShipmentList
	 * @return
	 * @throws Exception
	 */
	private boolean isShipmentCancelled(Document docGetShipmentList) throws Exception {
		String currentShipmentKey = XPathUtil.getString(docGetShipmentList, "/Order/@CurrentShipmentKey");
		return XMLUtil.getString(docGetShipmentList, ".//Shipments/Shipment[@ShipmentKey='" + currentShipmentKey +"']/@Status").equalsIgnoreCase("9000") ? true : false;
	}
}
