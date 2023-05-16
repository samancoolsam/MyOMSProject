package com.academy.ecommerce.sterling.email.api;

/**#########################################################################################
 *
 * Author					   : Everest
 * Module                      : OMNI-95654
 * Date                        : 28-MAR-2023 
 * Description				   : This class translates/updates Assembly in Progress Email message xml posted to Listrak
 *
 * #########################################################################################*/

import org.w3c.dom.Document;

import com.academy.ecommerce.sterling.email.util.AcademyEmailUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.util.Properties;

public class AcademyPostEmailContentOnAssemblyInProgress implements YIFCustomApi {

	private Properties props;

	public void setProperties(Properties props) throws Exception {

		this.props = props;
	}

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPostEmailContentOnAssemblyInProgress.class);

	/**
	 * This method customizes input xml to post AIP email message to
	 * ESB queue
	 * 
	 * @param inDoc
	 * @return docSMSMessage
	 * @throws Exception
	 */

	public Document prepareEmailContent(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose(
				"AcademyPostEmailContentOnAssemblyInProgress.prepareEmailContent()_InXML:" + XMLUtil.getXMLString(inDoc));

		String strStatus = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_STATUS);
		if(strStatus.equals(AcademyConstants.STR_ASSEMBLY_IN_PROGRESS_STATUS)) {
			inDoc = updateEmailTemplateAssemblyInProgress(env, inDoc);
			sendEmail(env, inDoc);
		}
		

		log.verbose("AcademyPostEmailContentOnAssemblyInProgress.prepareEmailContent()_returnDoc:"
				+ XMLUtil.getXMLString(inDoc));
		return inDoc;

	}

	/**
	 * This method updates Assembly In Progress email template XML to post to ESB
	 * queue.
	 * 
	 * @param inDoc
	 * @throws Exception
	 */

	private Document updateEmailTemplateAssemblyInProgress(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("Inside updateEmailTemplateAssemblyInProgress Method");
		Document docOrder = null;
		Element eleIndoc = inDoc.getDocumentElement();
		String strEmailText = AcademyConstants.STR_MESSAGE_TYPE_FOR_ASSEMBLY_IN_PROGRESS;
		String strMessageType = AcademyConstants.STR_MESSAGE_TYPE_FOR_ASSEMBLY_IN_PROGRESS;
		// Update Order Level Attributes
		String strSalesOrderHeaderKey = ((Element) inDoc.getDocumentElement().getElementsByTagName("ShipmentLine").item(0))
				.getAttribute("OrderHeaderKey");
		
		log.verbose("SO order header key-" + strSalesOrderHeaderKey);

		if (!YFCObject.isVoid(strSalesOrderHeaderKey)) {
			Document inputXML = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			inputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strSalesOrderHeaderKey);
			inputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE,
					AcademyConstants.SALES_DOCUMENT_TYPE);

			env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
					"global/template/api/getCompleteOrderDetails.ToSendEmail.xml");
			docOrder = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, inputXML);
			env.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS);
			log.verbose("API_GET_COMPLETE_ORDER_DETAILS output:"
					+ XMLUtil.getXMLString(docOrder));
		}
		
		Element eleOrder = docOrder.getDocumentElement();
		eleOrder.setAttribute("CustomerName", AcademyEmailUtil.getBillToCustomerName(eleOrder));
		eleOrder.setAttribute("AlternamePickUpPerson",
				AcademyEmailUtil.getAlternamePickUpCustomerName(eleOrder));
		eleOrder.setAttribute("DayPhone", AcademyEmailUtil.getDayPhone(eleOrder));
		
		//AcademyEmailUtil.removeOrderlines(docOrder, env, eleOrder);
		
		Document outDocGetCommonCodeList = getCommonCodeList(env, strEmailText);
		String strEmailMessageText = getEmailMessage(outDocGetCommonCodeList, strEmailText);
		String strEmailSubject = XPathUtil.getString(outDocGetCommonCodeList, "/CommonCodeList/CommonCode[@CodeValue='" + strEmailText
				+ "_EmailSub" + "']/@CodeLongDescription");
		strEmailMessageText = strEmailMessageText.replace("$$", " ");
		eleOrder.setAttribute("EmailText", strEmailMessageText);
		eleOrder.setAttribute("EmailSubject", strEmailSubject);
		
		NodeList nlOrderLines = AcademyEmailUtil.getCompleteOrderLineLines(eleOrder);
		
		for (int i = 0; i < nlOrderLines.getLength(); i++) {

			log.verbose("Inside nlOrderline : #" + i); 
			Element eleOrderline = (Element) nlOrderLines.item(i);

			// populate messageType,messageID and ProductInfo elements

			createProductInfoElement(docOrder);			
			AcademyEmailUtil.updateMessageRef(env, eleOrder, "ASSEMBLY_IN_PROGRESS_MSG_ID_PROD", "ASSEMBLY_IN_PROGRESS_MSG_ID_STAGE",
					"ASSEMBLY_IN_PROGRESS_MSG_TYPE");
			//AddAssemblyItem(docOrder,eleOrderline);
			Element eleProductInfo = (Element) XPathUtil.getNode(docOrder.getDocumentElement(), "/Order/ProductInfo");
			String orderQty = eleOrderline.getAttribute("OrderedQty");
			addItemInfo(docOrder, eleOrderline, orderQty, eleProductInfo);
			
			

		}
		
		log.verbose("End of updateEmailTemplateAssemblyInProgress Method");
		return docOrder;

	}
	
	
	/**
	 * This method prepares the Item Info to be added
	 * 
	 * @param inDoc
	 * @param eleOrderLine
	 * @throws Exception
	 */
	private void addItemInfo(Document inDoc, Element eleOrderLine, String orderQty, Element eleToAdd) throws Exception {
		String olkey = eleOrderLine.getAttribute("OrderLineKey");
		String olUnitPrice = XMLUtil.getString(inDoc,
				"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/LinePriceInfo/@UnitPrice");
		Element eleItemInfo = createItemInfo(inDoc, olkey, orderQty, olUnitPrice);
		eleToAdd.appendChild(eleItemInfo);
	}
	
	/**
	 * This method populates the Item Info
	 * 
	 * @param inDoc
	 * @param olkey
	 * @param orderQty
	 * @param olUnitPrice
	 * @throws Exception
	 */
	private Element createItemInfo(Document inDoc, String olkey, String orderQty, String olUnitPrice) throws Exception {
		Element eleItemInfo = XMLUtil.createElement(inDoc, "ItemInfo", null);
		eleItemInfo.setAttribute("OrderLineKey", olkey);
		eleItemInfo.setAttribute("ItemID", XMLUtil.getString(inDoc,
				"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/ItemDetails/@ItemID"));
		eleItemInfo.setAttribute("Description", XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine[@OrderLineKey='"
				+ olkey + "']/ItemDetails/PrimaryInformation/@Description"));
		eleItemInfo.setAttribute("OrderQty", orderQty);
		eleItemInfo.setAttribute("ImageLoc", XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine[@OrderLineKey='"
				+ olkey + "']/ItemDetails/PrimaryInformation/@ImageLocation"));
		eleItemInfo.setAttribute("ImageID", XMLUtil.getString(inDoc,
				"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/ItemDetails/PrimaryInformation/@ImageID"));
		eleItemInfo.setAttribute("UnitPrice", olUnitPrice);
		return eleItemInfo;
	}
	/**
	 * 
	 * This method creates ProductInfo elements and appends it to the output email
	 * 
	 * message xml
	 * 
	 * 
	 * 
	 * @param inDoc
	 * 
	 * @throws Exception
	 * 
	 */

	private void createProductInfoElement(Document inDoc) throws Exception {
		
		
		log.verbose("Before Product Info : " +  XMLUtil.getXMLString(inDoc));
		if (YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo"))) {

			Element eleProductInfo = XMLUtil.createElement(inDoc, "ProductInfo", null);

			inDoc.getDocumentElement().appendChild(eleProductInfo);
			log.verbose("After Product Info : " +  XMLUtil.getXMLString(inDoc));

		}

	}
	
	private void sendEmail(YFSEnvironment env, Document inDoc) throws Exception {
		// send customer email
		log.verbose("Before Sending customer email :" + XMLUtil.getXMLString(inDoc));
		String customerEMailID = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID);
		if (!YFCCommon.isVoid(customerEMailID) && customerEMailID.contains(",")) {
			customerEMailID = customerEMailID.substring(0, customerEMailID.indexOf(","));
			inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID, customerEMailID);
		}
		Document emailSentOutDoc = AcademyUtil.invokeService(env,
				"AcademySendAssemblyInProgressEmailToListrak", inDoc);
		log.verbose("Sent customer email :" + XMLUtil.getXMLString(emailSentOutDoc));
		// check and send alternate email
		if (AcademyEmailUtil.isAlternamePickUpCustomerEmailExist(inDoc.getDocumentElement())) {
			AcademyEmailUtil.setAlternamePickUpCustomerEmailExist(inDoc.getDocumentElement());
			log.verbose("Before Sending Alternate pickup email :" + XMLUtil.getXMLString(inDoc));
			emailSentOutDoc = AcademyUtil.invokeService(env,
			"AcademySendAssemblyInProgressEmailToListrak", inDoc);
			log.verbose("Sent Alternate pickup email :" + XMLUtil.getXMLString(emailSentOutDoc));
		}
	}

	//This method is used to prepare input for commoncode and call getCommonCodeList and return the output document.
	/**
		 * @param env
		 * @param strCodeType
		 * @return
		 * @throws Exception
		 */
		private Document getCommonCodeList(YFSEnvironment env, String strCodeType) throws Exception {
			log.beginTimer("AcademyPostEmailContentOnAssemblyInProgress.getCommonCodeList");
			log.verbose(
					"Start AcademyPostEmailContentOnAssemblyInProgress.getCommonCodeList() strCodeType :: " + strCodeType);
			Document inDocGetCommonCodeList = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, strCodeType);
			log.verbose("input to getCommonCodeList " + XMLUtil.getXMLString(inDocGetCommonCodeList));
			Document outDocGetCommonCodeList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST,
					inDocGetCommonCodeList);
			log.verbose("End AcademyPostEmailContentOnAssemblyInProgress.getCommonCodeList() output XML: "
					+ XMLUtil.getXMLString(outDocGetCommonCodeList));
			log.endTimer("AcademyPostEmailContentOnAssemblyInProgress.getCommonCodeList");
			return outDocGetCommonCodeList;
		}	
		
		//This method is used to prepare the Email Message from the common code list output document. 
		/**
		 * @param outDocGetCommonCodeList
		 * @param strEmailText
		 * @return
		 * @throws Exception
		 */
		private String getEmailMessage(Document outDocGetCommonCodeList, String strEmailText) throws Exception {
			log.beginTimer("AcademyPostEmailContentOnAssemblyInProgress.getEmailMessage");
			log.verbose("Start AcademyPostEmailContentOnAssemblyInProgress.getEmailMessage()  strEmailText ::" + strEmailText);

			String strSMSMessage = "";
			int iMsgCount = 1;
			String strMessage = "";

			do {
				strMessage = XPathUtil.getString(outDocGetCommonCodeList, "/CommonCodeList/CommonCode[@CodeValue='" + strEmailText
						+ "_Email" + iMsgCount + "']/@CodeLongDescription");
				log.verbose("strMessage" + strMessage);
				if (YFCObject.isVoid(strMessage)) {
					iMsgCount = 0;
				} else {
					strSMSMessage = strSMSMessage + strMessage;
					iMsgCount++;
				}
			} while (iMsgCount != 0);

			log.verbose("End AcademyPostEmailContentOnAssemblyInProgress.getEmailMessage() Output message " + strSMSMessage);
			log.endTimer("AcademyPostEmailContentOnAssemblyInProgress.getEmailMessage");
			return strSMSMessage;
		}	
	
}
