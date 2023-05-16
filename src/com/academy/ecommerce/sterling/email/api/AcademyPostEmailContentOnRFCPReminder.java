package com.academy.ecommerce.sterling.email.api;

/**#########################################################################################
 *
 * Project Name                : ESP
 * Module                      : OMNI-20313,20314
 * Date                        : 22-JUN-2021 
 * Description				  : This class translates/updates RFCP Reminder and Final Reminder Email message xml posted to ESB queue.
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

public class AcademyPostEmailContentOnRFCPReminder implements YIFCustomApi {

	private Properties props;

	public void setProperties(Properties props) throws Exception {

		this.props = props;
	}

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPostEmailContentOnRFCPReminder.class);

	/**
	 * This method customizes input xml to post Ready For RFCP Reminder and Final Reminder email message to
	 * ESB queue
	 * 
	 * @param inDoc
	 * @return docSMSMessage
	 * @throws Exception
	 */

	public Document prepareEmailContent(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose(
				"AcademyPostEmailContentOnRFCPReminder.prepareEmailContent()_InXML:" + XMLUtil.getXMLString(inDoc));

		updateEMailTemplateReminderForBOPIS(env, inDoc);
		sendEmail(env, inDoc);

		log.verbose("AcademyPostEmailContentOnRFCPReminder.prepareEmailContent()_returnDoc:"
				+ XMLUtil.getXMLString(inDoc));
		return inDoc;

	}

	/**
	 * This method updates Bopis ready for pickup email template XML to post to ESB
	 * queue.
	 * 
	 * @param inDoc
	 * @throws Exception
	 */

	private void updateEMailTemplateReminderForBOPIS(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("Inside updateEMailTemplateReminderForBOPIS Method");		
		// Remove Orderlines
		Element eleIndoc = inDoc.getDocumentElement();
		//Start : OMNI-93701 : Ignore Sending Curbside URL for Orders which have open Firearm item
		// Add Urls
		addUrls(inDoc, env, eleIndoc);
		//End : OMNI-93701 : Ignore Sending Curbside URL for Orders which have open Firearm item

		
		//Start : OMNI-74228 : BOPIS/STS Consolidated Pickup Reminders
		String isReminderConsolidation = eleIndoc.getAttribute(AcademyConstants.STR_IS_REMINDER_CONSOLIDATION_ENABLED);
		if (!AcademyConstants.STR_YES.equals(isReminderConsolidation)) {
			//Remove Orderlines			
			AcademyEmailUtil.removeOrderlines(inDoc, env, eleIndoc);
		} else {
			log.verbose("Removing nonRFCP lines as isReminderConsolidation is: " + isReminderConsolidation);
			AcademyEmailUtil.removeNonRFCPOrderlines(eleIndoc);
		}
		//End : OMNI-74228 : BOPIS/STS Consolidated Pickup Reminders
		
		String strIsFinalReminder = eleIndoc.getAttribute("IsFinalReminder");
		
		// Update Order Level Attributes
		eleIndoc.setAttribute("CustomerName", AcademyEmailUtil.getBillToCustomerName(eleIndoc));
		eleIndoc.setAttribute("AlternamePickUpPerson",
				AcademyEmailUtil.getAlternamePickUpCustomerName(eleIndoc));
		eleIndoc.setAttribute("DayPhone", AcademyEmailUtil.getDayPhone(eleIndoc));
		
		//populate messageType and messageID
		if (!YFCCommon.isVoid(strIsFinalReminder) && (strIsFinalReminder.equals(AcademyConstants.ATTR_Y)))
		{
			AcademyEmailUtil.updateMessageRef(env, eleIndoc, "RFCP_FINAL_REM_MSG_ID_PROD", "RFCP_FINAL_REM_MSG_ID_STAGE", "PN_RFCP_FINAL_REM_MSG_TYPE");
		}
		else {
			AcademyEmailUtil.updateMessageRef(env, eleIndoc, "RFCP_REM_MSG_ID_PROD", "RFCP_REM_MSG_ID_STAGE", "PN_RFCP_REM_MSG_TYPE");
		}

		// Check for RFCP lines and update the email content 
		NodeList nlOrderLines = AcademyEmailUtil.getCompleteOrderLineLines(eleIndoc);
		for (int i = 0; i < nlOrderLines.getLength(); i++) {
			Element eleOrderline = (Element) nlOrderLines.item(i);
			createProductInfoElement(inDoc);
			if(checkRFCPLine(eleOrderline) | checkPartialRFCPLine(eleOrderline)){
				// Alternate StorePick email changes
				if(isAlternateStoreLine(eleOrderline)) {
					AddASRFCPItem(inDoc,eleOrderline);
				}else {
					AddRFCPItem(inDoc,eleOrderline);	
				}
			}
		}			

		log.verbose("End of updateEMailTemplateReminderForBOPIS Method");
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
				AcademyConstants.ACAD_RFCP_REMINDER_LISTRAK_EMAIL_SERVICE, inDoc);
		log.verbose("Sent customer email :" + XMLUtil.getXMLString(emailSentOutDoc));
		// check and send alternate email
		if (AcademyEmailUtil.isAlternamePickUpCustomerEmailExist(inDoc.getDocumentElement())) {
			AcademyEmailUtil.setAlternamePickUpCustomerEmailExist(inDoc.getDocumentElement());
			log.verbose("Before Sending Alternate pickup email :" + XMLUtil.getXMLString(inDoc));
			emailSentOutDoc = AcademyUtil.invokeService(env,
			AcademyConstants.ACAD_RFCP_REMINDER_LISTRAK_EMAIL_SERVICE, inDoc);
			log.verbose("Sent Alternate pickup email :" + XMLUtil.getXMLString(emailSentOutDoc));
		}
	}

	/**
	 * This method updates urls to the output email messsage xml
	 * 
	 * @param inDoc
	 * @param env
	 * @param eleIndoc
	 * @throws Exception
	 */
	private static void addUrls(Document inDoc, YFSEnvironment env, Element eleIndoc) throws Exception {
		Element eleInputShip = null;
		String strCurbPickUpLink = null;
		String strShipmentNo = null;
		String strShipNode = null;
		String strURL_ViewOrderDetails = null;
		String strZipCode = null;
		String strCurrentShipmentKey = null;
		//OMNI-72427 start
		String strBillingZipCodeArg = AcademyConstants.CURBSIDE_BILLING_ZIPCODE_ARG;
		//OMNI-72427 End

		String strOrderNo = eleIndoc.getAttribute(AcademyConstants.ATTR_ORDER_NO);
		strCurrentShipmentKey = eleIndoc.getAttribute(AcademyConstants.STR_CURRENT_SHIPMENTKEY);
		
		String strShowCurbSideUrl =  SCXmlUtil.getXpathAttribute(inDoc.getDocumentElement(), "OrderLines/OrderLine[@ShowCurbsideInstructions='N'and (@MaxLineStatus!='9000')]/@ShowCurbsideInstructions");//OMNI-86861
		
		eleInputShip = (Element) XPathUtil.getNode(inDoc,
				"/Order/Shipments/Shipment[@ShipmentKey='" + strCurrentShipmentKey + "']");
		if (!YFCCommon.isVoid(eleInputShip)) {
			strShipmentNo = eleInputShip.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			strShipNode = eleInputShip.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
		}

		Element elePersonInfoBillTo = SCXmlUtil.getChildElement(eleIndoc, AcademyConstants.ELE_PERSON_INFO_BILL_TO);
		if (!YFCCommon.isVoid(elePersonInfoBillTo)) {
			strZipCode = elePersonInfoBillTo.getAttribute(AcademyConstants.ZIP_CODE);
		}

		// Fetching CURBSIDEPICKUP URL from CO file
		String strCurbPickUpOrderLink = YFSSystem.getProperty(AcademyConstants.PROP_CURB_PICKUP_ORDER_URL);
		
		//OMNI-81860 Start
		String strOrdlvlRemConsol = eleIndoc.getAttribute(AcademyConstants.STR_IS_REMINDER_CONSOLIDATION_ENABLED);
		if ((!YFCObject.isVoid(strOrdlvlRemConsol)
			&& strOrdlvlRemConsol.equals(AcademyConstants.STR_YES)) && strCurbPickUpOrderLink.contains("$Shipment_No")) {
			strCurbPickUpOrderLink = strCurbPickUpOrderLink.replace("-$Shipment_No-", "-");
			log.verbose("Curb Pick Up Link is when consolidation is enabled::"+strCurbPickUpOrderLink);
		}
		//OMNI-81860 End
			
		// https://uat1www.academy.com/curbside?details=$Order_No-$Shipment_No-$Store_No

		if (!YFCCommon.isVoid(strCurbPickUpOrderLink)) {
			if(!"N".equals(strShowCurbSideUrl)) {
				strCurbPickUpLink = strCurbPickUpOrderLink.replace(AcademyConstants.ATT_$ORDER_NO, strOrderNo);
				//OMNI-81860 Start
				if (YFCObject.isVoid(strOrdlvlRemConsol) && strCurbPickUpOrderLink.contains("$Shipment_No")) {
					strCurbPickUpLink = strCurbPickUpLink.replace(AcademyConstants.ATT_$SHIPMENT_NO, strShipmentNo);
					}
				//OMNI-81860 End
				strCurbPickUpLink = strCurbPickUpLink.replace(AcademyConstants.ATT_$STORE_NO, strShipNode);
				//OMNI-72427 start
				strBillingZipCodeArg=strBillingZipCodeArg.concat(strZipCode);
				strCurbPickUpLink = strCurbPickUpLink.concat(strBillingZipCodeArg);
				//OMNI-72427 End
				eleIndoc.setAttribute(AcademyConstants.ATTR_SHOW_CURB_INS, "Y");
				eleIndoc.setAttribute("URL_CurbsidePickup", strCurbPickUpLink);
			}
			else {
				eleIndoc.setAttribute(AcademyConstants.ATTR_SHOW_CURB_INS, "N");	
			}
		}

		// Fetching ORDERDETAILS URL from CO file
		String strViewOrderDetails = YFSSystem.getProperty(AcademyConstants.PROP_VIEW_ORDERDETAILS_URL);

		// https://uat4www.academy.com/webapp/wcs/stores/servlet/UserAccountOrderStatus?orderId=@@@@&zipCode=$$$$&langId=-1&storeId=10151&catalogId=10051&isSubmitted=true&URL=NonAjaxOrderDetail&errorViewName=GuestOrderStatusView&splitshipstatus=true&isDisplayLeftNav=false

		if (!YFCCommon.isVoid(strViewOrderDetails)) {
			strURL_ViewOrderDetails = strViewOrderDetails.replace("@@@@", strOrderNo);
			strURL_ViewOrderDetails = strURL_ViewOrderDetails.replace("$$$$", strZipCode);
			eleIndoc.setAttribute("ViewOrderDetails", strURL_ViewOrderDetails);

		}

	}

	/**
	 * This method checks if the orderline is RFCP line or not
	 * message xml
	 * 
	 * @param eleOrderLine
	 * @throws Exception
	 */
	private boolean checkRFCPLine(Element eleOrderLine) throws Exception {
		// OMNI-50857 - Start changes - Items not displayed in BOPIS Reminder emails
		return (!YFCObject.isVoid(eleOrderLine.getAttribute("OrderedQty")) && !YFCObject.isVoid(eleOrderLine.getAttribute("OriginalOrderedQty")) 
						&&  Double.parseDouble(eleOrderLine.getAttribute("OrderedQty")) == Double.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"))) ? true : false;
		// OMNI-50857 - End changes - Items not displayed in BOPIS Reminder emails
	}

	/**
	 * This method checks if the orderline is Partial RFCP line or not
	 * message xml
	 * 
	 * @param eleOrderLine
	 * @throws Exception
	 */
	private boolean checkPartialRFCPLine(Element eleOrderLine) throws Exception {
		// OMNI-50857 - Start changes - Items not displayed in BOPIS Reminder emails
		double dOrderedQty = 0.00;
		if (!YFCObject.isVoid(eleOrderLine.getAttribute("OrderedQty"))) {
			dOrderedQty = Double.parseDouble(eleOrderLine.getAttribute("OrderedQty"));
		}
		double dOriginalOrderedQty = Double.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"));
		return (!YFCObject.isDoubleVoid(dOrderedQty) && !YFCObject.isVoid(dOriginalOrderedQty) 
						&&  dOrderedQty != dOriginalOrderedQty) ? true : false;
		// OMNI-50857 - End changes - Items not displayed in BOPIS Reminder emails
	}


	/**
	 * This method checks and fetches ExtnOriginalFulfillmentType attribute value
	 * message xml
	 * 
	 * @param eleOrderLine
	 * @throws Exception
	 */
	private String getExtnOriginalFulfillmentType(Element eleOrderLine) throws Exception {
		String olkey = eleOrderLine.getAttribute("OrderLineKey");
		return XMLUtil.getString(eleOrderLine, "/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/Extn/@ExtnOriginalFulfillmentType");
	}

	/**
	 * This method creates ProductInfo elements and appends it to the output email
	 * message xml
	 * 
	 * @param inDoc
	 * @throws Exception
	 */
	private void createProductInfoElement(Document inDoc) throws Exception {
		if (YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo"))) {
			Element eleProductInfo = XMLUtil.createElement(inDoc, "ProductInfo", null);
			inDoc.getDocumentElement().appendChild(eleProductInfo);
		}
	}

	/**
	 * This method adds RFCP Item Info to the ProductInfo element
	 * 
	 * @param inDoc
	 * @param eleOrderLine
	 * @throws Exception
	 */
	private void AddRFCPItem(Document inDoc, Element eleOrderLine) throws Exception {
		Element eleFRCP = null;
		if (!YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/RFCP"))) {
			eleFRCP = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/RFCP");
		} else {
			eleFRCP = XMLUtil.createElement(inDoc, "RFCP", null);
			Element eleProductInfo = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo");
			eleProductInfo.appendChild(eleFRCP);
		}
		String orderQty = eleOrderLine.getAttribute("OrderedQty");
		addItemInfo(inDoc, eleOrderLine, orderQty, eleFRCP);

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
	
	// Alternate StorePick email changes
	
	private boolean isAlternateStoreLine(Element eleOrderline) throws Exception {
		String olkey = eleOrderline.getAttribute("OrderLineKey");
		String strExtnIsASP = XMLUtil.getString(eleOrderline, "/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/Extn/@ExtnIsASP");
		return strExtnIsASP.equalsIgnoreCase("Y") ? true : false;
	}
	
	private void AddASRFCPItem(Document inDoc, Element eleOrderLine) throws Exception {
		Element eleFRCP = null;
		if (!YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/ASRFCP"))) {
			eleFRCP = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/ASRFCP");
		} else {
			eleFRCP = XMLUtil.createElement(inDoc, "ASRFCP", null);
			Element eleProductInfo = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo");
			eleProductInfo.appendChild(eleFRCP);
		}
		String orderQty = eleOrderLine.getAttribute("OrderedQty");
		addItemInfo(inDoc, eleOrderLine, orderQty, eleFRCP);
	}

}
