package com.academy.ecommerce.sterling.bopis.shipment;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.academy.ecommerce.server.AcademySendEmailAgentServer;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.ycp.core.YCPContext;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @Author Sumit Arora
 * @Date Created JULY 20th 2018
 * 
 * @Purpose 1.Call Trigger an Reminder Email- For BOPIS Shipments
 **/

public class AcademyBOPISCustomerReminderEmailWrapper implements YIFCustomApi {

	private static Logger log = Logger.getLogger(AcademySendEmailAgentServer.class.getName());
	Document docOutGetCompleteOrderDetails = null;
	String isReminderConsolidation = AcademyConstants.STR_NO;
	private Properties props;
	/**
	 * Preparing input for manageTaskQueue API
	 * 
	 * @param strTaskQDataKey
	 * @return docInManageTaskQueue
	 * @throws Exception
	 */
	public Document triggerReminderEmail(YFSEnvironment env, Document inXML) throws Exception {
		String strShipmentNo = null;
		Element orderElem = null;
		String strStatus = null;
		//Start : OMNI-4219
		String strFinalReminder = null;
		//End : OMNI-4219
		
		strShipmentNo = ((Element) (inXML.getElementsByTagName("Shipment").item(0))).getAttribute("ShipmentNo");
		strStatus = ((Element) (inXML.getElementsByTagName("Shipment").item(0))).getAttribute("Status");
		//Start : OMNI-4219
		strFinalReminder = ((Element) (inXML.getElementsByTagName("Shipment").item(0))).getAttribute(AcademyConstants.ATTR_FINAL_REMINDER);
		//End : OMNI-4219
		//Start : OMNI-74228 : BOPIS/STS Consolidated Pickup Reminders
		isReminderConsolidation = inXML.getDocumentElement().getAttribute(AcademyConstants.STR_IS_REMINDER_CONSOLIDATION_ENABLED);
		//End : OMNI-74228 : BOPIS/STS Consolidated Pickup Reminders
		orderElem = beforeSendingBOPISFRdyForCustEmail(env, strShipmentNo, strFinalReminder);
		orderElem.setAttribute("CurrentShiomentStatus", strStatus);
		// OMNI-50857 - Start changes - Items not displayed in BOPIS Reminder emails
		if(!"9000".equals(strStatus)){
		boolean sentMail = callSendEmailService(env, orderElem);
		}// OMNI-50736 - End changes - Items not displayed in BOPIS Reminder emails

		return docOutGetCompleteOrderDetails;

	}

	private boolean callSendEmailService(YFSEnvironment env, Element orderElem) {
		boolean sendMailSuccess = true;
		try {
			AcademyUtil.invokeService(env, AcademyConstants.SERVICE_BOPIS_SEND_REMINDER_EMAIL,
					orderElem.getOwnerDocument());
			log.verbose("******** callSendEmailService end ***** ");
		}

		catch (Exception e) {

			YIFApi api;
			Document envDoc = null;
			YFSEnvironment newenv;
			String strInvalidEmailError = null; // WN-1560 Shipment Confirmation email - Wrong email IDs being passed
			try {
				((YCPContext) env).rollback();
				api = YIFClientFactory.getInstance().getLocalApi();

				envDoc = YFCDocument.parse("<Environment userId=\"yantra\" progId=\"yantra\"/>").getDocument();
				Document env1Doc = XMLUtil.createDocument(AcademyConstants.ELE_ENV);
				env1Doc.getDocumentElement().setAttribute(AcademyConstants.ATTR_USR_ID, env.getUserId());
				env1Doc.getDocumentElement().setAttribute(AcademyConstants.ATTR_PROG_ID, env.getProgId());
				newenv = api.createEnvironment(env1Doc);

				strInvalidEmailError = e.getMessage();
				if (strInvalidEmailError.contains(AcademyConstants.STR_INVALID_ADDRESSES)) {
					log.verbose("Invalid Addresses Alert....");
					orderElem.setAttribute(AcademyConstants.ATTR_INVALID_EMAIL_ID, AcademyConstants.STR_YES);
				}
				// End WN-1560 Shipment Confirmation email - Wrong email IDs being passed

				Document alertInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
				XMLUtil.copyElement(alertInput, orderElem, alertInput.getDocumentElement());
				AcademyUtil.invokeService(newenv, AcademyConstants.RAISE_ALERT_ON_EMAIL_FAIL_SERVICE, alertInput);

				// ((YFSContext)newenv).commit();
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

			// Start WN-1560 Shipment Confirmation email - Wrong email IDs being passed
			// throw new YFSException(e.getMessage());
			if (!strInvalidEmailError.contains(AcademyConstants.STR_INVALID_ADDRESSES)) {
				log.verbose("Invalid Addresses....");
				throw new YFSException(e.getMessage());
			}
			// End WN-1560 Shipment Confirmation email - Wrong email IDs being passed

		}
		return sendMailSuccess;
	}

	/**
	 * Form the element with all the required data for sending BOPIS Reminder 'Ready
	 * For Customer Pickup' email
	 * 
	 * @param strShipmentNo,strFinalRemnder(OMNI-4219)
	 * @return eleGetCompleteOrderDetailsOutput
	 * @throws Exception
	 */
	private Element beforeSendingBOPISFRdyForCustEmail(YFSEnvironment env, String strShipmentNo, String strFinalReminder) throws Exception {
		log.verbose("Entering  beforeSendingBOPISFRdyForCustEmail()");
		Element eleGetCompleteOrderDetailsOutput = null;
		Element eleShipment = null;
		String strShipmentKey = null;
		String strOHK = null;
		String emailText = "";
		String actualPickupDate = null;

		log.verbose("strShipmentNo :" + strShipmentNo);

		strOHK = getShipmentListCall(env, strShipmentNo);
		log.verbose("strOHK :" + strOHK);

		docOutGetCompleteOrderDetails = callgetCompleteOrderDetails(env, strOHK, false);

		eleGetCompleteOrderDetailsOutput = docOutGetCompleteOrderDetails.getDocumentElement();
		log.verbose(
				"getCompleteOrderDetails Output : " + XMLUtil.getElementXMLString(eleGetCompleteOrderDetailsOutput));
		eleShipment = (Element) XPathUtil.getNode(eleGetCompleteOrderDetailsOutput,
				"/Order/Shipments/Shipment[@ShipmentNo='" + strShipmentNo + "']");
		strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		if (eleShipment.getAttribute("DeliveryMethod").equals("PICK")) {

			Document docGetReasonCodesInput = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			docGetReasonCodesInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE,
					"EMAIL_CONTENT");
			docGetReasonCodesInput.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.HUB_CODE);
			Document docCommonCodeListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST,
					docGetReasonCodesInput);
			
			String strCodeType = "ORD_REM_READY_FOR_PICK";
			
			//Start : OMNI-4219
			if(!YFCObject.isVoid(strFinalReminder) && strFinalReminder.equals(AcademyConstants.STR_YES)) {
				strCodeType = "ORD_LAST_READY_FOR_PICK";
			}
			//End : OMNI-4219
			
			List<Node> eleOrderPickUp=XMLUtil.getElementListByXpath(docCommonCodeListOutput, 
					"/CommonCodeList/CommonCode[@CodeShortDescription='" + strCodeType +"']");
			
			for (int i = 1; i <= eleOrderPickUp.size(); i++)
			{
				String strCodeValue=strCodeType + "_" + i;
				emailText=emailText+XMLUtil
						.getElementByXPath(docCommonCodeListOutput,
								"/CommonCodeList/CommonCode[@CodeValue='"+ strCodeValue + "']")
						.getAttribute("CodeLongDescription");
						
			}
			
			eleShipment = (Element) XPathUtil.getNode(eleGetCompleteOrderDetailsOutput,
					"/Order/Shipments/Shipment[@ShipmentNo='" + strShipmentNo
							+ "']/AdditionalDates/AdditionalDate[@DateTypeId='ACADEMY_MAX_CUSTOMER_PICK_DATE']");
			actualPickupDate = eleShipment.getAttribute("ActualDate");


			String actualPickupDateMON = formatDate(actualPickupDate, AcademyConstants.STR_DATE_TIME_PATTERN, "MMM");
			String actualPickupDateDate = formatDate(actualPickupDate, AcademyConstants.STR_DATE_TIME_PATTERN, "dd");

			actualPickupDate = formatDate(actualPickupDate, AcademyConstants.STR_DATE_TIME_PATTERN, "MM/dd/yy");
			emailText = emailText.replace("$$$$", actualPickupDate);

			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			Date dtCurrentDate = cal.getTime();
			String strCurrentDate = sdf.format(dtCurrentDate);
			String currentMON = formatDate(strCurrentDate, AcademyConstants.STR_DATE_TIME_PATTERN, "MMM");
			String currentDate = formatDate(strCurrentDate, AcademyConstants.STR_DATE_TIME_PATTERN, "dd");

			eleGetCompleteOrderDetailsOutput.setAttribute("CurrentDate", currentMON + " " + currentDate);
			eleGetCompleteOrderDetailsOutput.setAttribute("PickUpUntilDate",
					actualPickupDateMON + " " + actualPickupDateDate);
			eleGetCompleteOrderDetailsOutput.setAttribute("EmailText", emailText);
			eleGetCompleteOrderDetailsOutput.setAttribute("EmailType", "REMRFCP");
			
			//alternate pickup person changes-- BOPIS - Start
			if (!YFCCommon
					.isVoid(SCXmlUtil.getChildElement(eleGetCompleteOrderDetailsOutput, AcademyConstants.ELE_PERSON_INFO_MARK_FOR))) {
				Element elePersonInfoMarkFor = SCXmlUtil.getChildElement(eleGetCompleteOrderDetailsOutput, AcademyConstants.ELE_PERSON_INFO_MARK_FOR);
				if (elePersonInfoMarkFor.hasAttribute(AcademyConstants.ATTR_EMAILID)) {
					String StrAltEmailID = elePersonInfoMarkFor.getAttribute(AcademyConstants.ATTR_EMAILID);
					log.verbose("******* Alternate Contact EmailId Available **** " + StrAltEmailID);
					String strCustEmailID = eleGetCompleteOrderDetailsOutput.getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID);
					strCustEmailID = strCustEmailID + "," + StrAltEmailID;
					eleGetCompleteOrderDetailsOutput.setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID, strCustEmailID);
				}
			}
			//alternate pickup person changes-- BOPIS - End
		}
		//Start : OMNI-74228 : BOPIS/STS Consolidated Pickup Reminders
		if (!YFCObject.isVoid(isReminderConsolidation) && isReminderConsolidation.equals(AcademyConstants.STR_YES)) {
			eleGetCompleteOrderDetailsOutput.setAttribute(
					AcademyConstants.STR_IS_REMINDER_CONSOLIDATION_ENABLED, AcademyConstants.STR_YES);			
		} 
		eleGetCompleteOrderDetailsOutput.setAttribute(AcademyConstants.STR_CURRENT_SHIPMENTKEY, strShipmentKey);
		//End : OMNI-74228 : BOPIS/STS Consolidated Pickup Reminders
		eleGetCompleteOrderDetailsOutput.setAttribute(AcademyConstants.ATTR_TRANS_ID_EMAIL,
				AcademyConstants.SOF_RDYFORCUST_PICKUP_EMAIL_TRAN_ID);
		
		//Start : OMNI-4219 : Last Day reminder Emails
		if(!YFCObject.isVoid(strFinalReminder) && strFinalReminder.equals(AcademyConstants.STR_YES)){
			eleGetCompleteOrderDetailsOutput.setAttribute(AcademyConstants.ATTR_EMAIL_SUBJECT,
					AcademyConstants.STR_FINAL_REMINDER_SUBJECT);
			//Start : OMNI-38158 RFCP push notification
			eleGetCompleteOrderDetailsOutput.setAttribute("IsFinalReminder", "Y");
			//End : OMNI-38158 RFCP push notification
		}
		else {
			eleGetCompleteOrderDetailsOutput.setAttribute(AcademyConstants.ATTR_EMAIL_SUBJECT,
					AcademyConstants.STR_REMINDER_SUBJECT);
		}
		//End : OMNI-4219 : Last Day reminder Emails
					
		log.verbose("Exiting  beforeSendingSOFRdyForCustEmail()"
				+ XMLUtil.getElementXMLString(eleGetCompleteOrderDetailsOutput));

		return eleGetCompleteOrderDetailsOutput;

	}

	private Document callgetCompleteOrderDetails(YFSEnvironment env, String orderHeaderKey, boolean isRtnInvoice) {
		Document inputXML = null;
		Document outXML = null;
		String strSOFDepartments="";
		try {
			inputXML = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			inputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, orderHeaderKey);
			inputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE,
					AcademyConstants.SALES_DOCUMENT_TYPE);
			env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
					AcademyConstants.STR_SOF_TEMPLATEFILE_GETCOMPLETEORDER_DETAILS);
			outXML = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, inputXML);
			//Changes for OMNI-20313 Start
			strSOFDepartments=props.getProperty("SOFItemDeps");
			outXML = AcademyUtil.validateAndAppendShowCurbsideInstructions(outXML, strSOFDepartments);
			//Changes for OMNI-20313 End
			log.verbose(XMLUtil.getXMLString(outXML));
			env.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS);
		} catch (ParserConfigurationException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
		return outXML;
	}

	private String getShipmentListCall(YFSEnvironment env, String strShipmentIdentifier) {

		Document inpXML = null;
		Document outXML = null;
		String orderHeaderKey = "";
		try {
			inpXML = XMLUtil.createDocument("Shipment");
			inpXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentIdentifier);
			inpXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE,
					AcademyConstants.SALES_DOCUMENT_TYPE);
			inpXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);

			Document templateXML = XMLUtil.createDocument("Shipments");
			Element shipElem = templateXML.createElement("Shipment");
			Element shipLinesElem = templateXML.createElement("ShipmentLines");
			shipElem.appendChild(shipLinesElem);
			Element shipLineElem = templateXML.createElement("ShipmentLine");
			shipLineElem.setAttribute("OrderHeaderKey", " ");
			shipElem.setAttribute("ShipmentKey", " ");
			// shipElem.setAttribute("OrderHeaderKey", " ");
			shipLinesElem.appendChild(shipLineElem);
			templateXML.getDocumentElement().appendChild(shipElem);
			env.setApiTemplate("getShipmentList", templateXML);
			outXML = AcademyUtil.invokeAPI(env, "getShipmentList", inpXML);
			env.clearApiTemplate("getShipmentList");
			orderHeaderKey = ((Element) outXML.getDocumentElement().getElementsByTagName("ShipmentLine").item(0))
					.getAttribute("OrderHeaderKey");
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return orderHeaderKey;
	}

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

	
	public void setProperties(Properties props) throws Exception {
		this.props = props;

	}

}