package com.academy.ecommerce.sterling.email.api;

/**#########################################################################################
 *
 * Project Name                : ESP
 * Module                      : OMNI-20307,20308
 * Date                        : 18-JUN-2021 
 * Description				  : This class translates/updates Ready For Pickup/ Partial RFCP  Email message xml posted to ESB queue.
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
import com.yantra.yfs.japi.YFSException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.text.DecimalFormat;
import java.util.Properties;

public class AcademyPostEmailContentOnRdyForCustPickup implements YIFCustomApi {

	private Properties props;

	public void setProperties(Properties props) throws Exception {

		this.props = props;
	}

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPostEmailContentOnRdyForCustPickup.class);

	/**
	 * This method customizes input xml to post Ready For Pickup/Partial RFCP email message to
	 * ESB queue
	 * 
	 * @param inDoc
	 * @return docSMSMessage
	 * @throws Exception
	 */

	public Document prepareEmailContent(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("AcademyPostEmailContentOnRdyForCustPickup.prepareEmailContent()_InXML:" + XMLUtil.getXMLString(inDoc));
		
		updateEMailTemplateRdyForCustPickupForBOPIS(env, inDoc);
		sendEmail(env, inDoc);

		log.verbose("AcademyPostEmailContentOnRdyForCustPickup.prepareEmailContent()_returnDoc:"+ XMLUtil.getXMLString(inDoc));
		return inDoc;

	}

	/**
	 * This method prepare email content for AS RFCP email.
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document prepareASEmailContent(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("AcademyPostEmailContentOnRdyForCustPickup.prepareASEmailContent()_InXML:" + XMLUtil.getXMLString(inDoc));
        updateEmailTemplateForOrdersHavingASlines(env, inDoc);
		sendEmail(env, inDoc);
		log.verbose("AcademyPostEmailContentOnRdyForCustPickup.prepareASEmailContent()_returnDoc:"+ XMLUtil.getXMLString(inDoc));
		return inDoc;

	}

	/**
	 * This method updates Bopis ready for pickup email template XML to post to ESB
	 * queue.
	 * 
	 * @param inDoc
	 * @throws Exception
	 */

	private void updateEMailTemplateRdyForCustPickupForBOPIS(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("Inside updateEMailTemplateRdyForCustPickupForBOPIS Method");

		// Remove Orderlines
		Element eleIndoc = inDoc.getDocumentElement();
		
		//Start : OMNI-93701 : Ignore Sending Curbside URL for Orders which have open Firearm item
		// Add Urls
		addUrls(inDoc, env, eleIndoc);

		// Update Order Level Attributes
		eleIndoc.setAttribute("CustomerName", AcademyEmailUtil.getBillToCustomerName(eleIndoc));
		eleIndoc.setAttribute("AlternamePickUpPerson",
				AcademyEmailUtil.getAlternamePickUpCustomerName(eleIndoc));
		eleIndoc.setAttribute("DayPhone", AcademyEmailUtil.getDayPhone(eleIndoc));
		//End : OMNI-93701 : Ignore Sending Curbside URL for Orders which have open Firearm item
		
		AcademyEmailUtil.removeOrderlines(inDoc, env, eleIndoc);

		// Check if DeliveryMethod is PICK or not
		NodeList nlOrderLines = AcademyEmailUtil.getCompleteOrderLineLines(eleIndoc);
		String strHasCancelledLine = eleIndoc.getAttribute(AcademyConstants.ATTR_HAS_CANCELLED_LINES);
		for (int i = 0; i < nlOrderLines.getLength(); i++) {
			Element eleOrderline = (Element) nlOrderLines.item(i);
			//if (!YFCObject.isVoid(eleOrderline) && "PICK".equals(eleOrderline.getAttribute("DeliveryMethod"))) {

				// populate messageType,messageID and ProductInfo elements
				createProductInfoElement(inDoc);
				if (AcademyConstants.ATTR_Y.equals(strHasCancelledLine)) {				
					AcademyEmailUtil.updateMessageRef(env, eleIndoc, "PARTIAL_RFCP_MSG_ID_PROD",
							"PARTIAL_RFCP_MSG_ID_STAGE", "PN_PARTIAL_RFCP_MSG_TYPE");
					if(checkRFCPLine(eleOrderline)) {
						AddRFCPItem(inDoc,eleOrderline);}
					// OMNI-50736 - Start changes - Item details not populated for SOF line
					else if(checkSOFRFCPLine(eleOrderline)) {
						AddRFCPItem(inDoc,eleOrderline);}
					// OMNI-50736 - End changes - Item details not populated for SOF line
					else if(checkPartialRFCPLine(eleOrderline)) {
						AddRFCPItem(inDoc, eleOrderline);
						AddCancelledItem(inDoc, eleOrderline);}
					else if(checkCancelledLine(eleOrderline)) {
						AddCancelledItem(inDoc,eleOrderline);}
				} else {
					AcademyEmailUtil.updateMessageRef(env, eleIndoc, "RFCP_MSG_ID_PROD", "RFCP_MSG_ID_STAGE",
							"PN_RFCP_MSG_TYPE");
					if(checkRFCPLine(eleOrderline)) {
						AddRFCPItem(inDoc,eleOrderline);}
					// OMNI-50736 - Start changes - Item details not populated for SOF line
					else if(checkSOFRFCPLine(eleOrderline)) {
						AddRFCPItem(inDoc,eleOrderline);}
					// OMNI-50736 - End changes - Item details not populated for SOF line
					//OMNI-52304 start changes- Total RFCP items are not appearing in RFCP email
					else {
						AddRFCPItem(inDoc,eleOrderline);
					}
					//OMNI-52304 End changes- Total RFCP items are not appearing in RFCP email
				}

				log.verbose("End of updateEMailTemplateRdyForCustPickupForBOPIS Method");
			//}
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
				AcademyConstants.ACAD_RFCP_LISTRAK_EMAIL_SERVICE, inDoc);
		log.verbose("Sent customer email :" + XMLUtil.getXMLString(emailSentOutDoc));
		// check and send alternate email
		if (AcademyEmailUtil.isAlternamePickUpCustomerEmailExist(inDoc.getDocumentElement())) {
			AcademyEmailUtil.setAlternamePickUpCustomerEmailExist(inDoc.getDocumentElement());
			log.verbose("Before Sending Alternate pickup email :" + XMLUtil.getXMLString(inDoc));
			emailSentOutDoc = AcademyUtil.invokeService(env,
			AcademyConstants.ACAD_RFCP_LISTRAK_EMAIL_SERVICE, inDoc);
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

		// https://uat1www.academy.com/curbside?details=$Order_No-$Shipment_No-$Store_No
				//OMNI-92608, OMNI-92956 Start
				String strOrdlvlRFCPEmail = eleIndoc.getAttribute(AcademyConstants.STR_IS_REMINDER_CONSOLIDATION_ENABLED);
				log.verbose("IS_REMINDER_CONSOLIDATION_ENABLED::"+strOrdlvlRFCPEmail);

				if ((!YFCObject.isVoid(strOrdlvlRFCPEmail)
					&& strOrdlvlRFCPEmail.equals(AcademyConstants.STR_YES)) && strCurbPickUpOrderLink.contains("$Shipment_No")) {
					strCurbPickUpOrderLink = strCurbPickUpOrderLink.replace("-$Shipment_No-", "-");
					log.verbose("Update Curb Pick Up Link From Shipment level to Order Level::"+strCurbPickUpOrderLink);
				}
				log.verbose("Update Curb Pick Up Link From Shipment level to Order Level out side if::"+strCurbPickUpOrderLink);
				//OMNI-92608, OMNI-92956 END
		if (!YFCCommon.isVoid(strCurbPickUpOrderLink)) {
			if(!"N".equals(strShowCurbSideUrl)) {
				strCurbPickUpLink = strCurbPickUpOrderLink.replace(AcademyConstants.ATT_$ORDER_NO, strOrderNo);
				//OMNI-92608, OMNI-92956 Start
				if (strCurbPickUpOrderLink.contains("$Shipment_No")) {
					strCurbPickUpLink = strCurbPickUpLink.replace(AcademyConstants.ATT_$SHIPMENT_NO, strShipmentNo);
					}
				//OMNI-92608, OMNI-92956 End
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
		return (!YFCObject.isVoid(eleOrderLine.getAttribute("OrderedQty")) && !YFCObject.isVoid(eleOrderLine.getAttribute("OriginalOrderedQty")) 
						&&  Double.parseDouble(eleOrderLine.getAttribute("OrderedQty")) == Double.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"))) ? true : false;
	}
	
	/**
	 * OMNI-50736 - Item details not populated for SOF line
	 * This method checks if the orderline is SOF RFCP line or not
	 * message xml
	 * 
	 * @param eleOrderLine
	 * @throws Exception
	 */
	private boolean checkSOFRFCPLine(Element eleOrderLine) throws Exception {
		return  (!YFCObject.isVoid(eleOrderLine.getAttribute("OrderedQty")) && !YFCObject.isVoid(eleOrderLine.getAttribute("OriginalOrderedQty")) 
						&&  Double.parseDouble(eleOrderLine.getAttribute("OrderedQty")) == Double.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"))) ? true : false;
	}

	/**
	 * This method checks if the orderline is Partial RFCP line or not
	 * message xml
	 * 
	 * @param eleOrderLine
	 * @throws Exception
	 */
	private boolean checkPartialRFCPLine(Element eleOrderLine) throws Exception {
		//OMNI-51680 Start  changes - Cancelled items are displaying in Items ready for store pick widget 
		double dOrderedQty = 0.00;
		if (!YFCObject.isVoid(eleOrderLine.getAttribute("OrderedQty"))) {
			dOrderedQty = Double.parseDouble(eleOrderLine.getAttribute("OrderedQty"));
		}
		double dOriginalOrderedQty = Double.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"));
		return "3350.400".equals(eleOrderLine.getAttribute("MaxLineStatus"))
		//OMNI-51680 End   changes - Cancelled items are displaying in Items ready for store pick widget 
				&& (!YFCObject.isDoubleVoid(dOrderedQty) && !YFCObject.isVoid(dOriginalOrderedQty) 
						&&  dOrderedQty != dOriginalOrderedQty) ? true : false;
	}

	/**
	 * This method checks if the orderline is Cancelled line or not
	 * message xml
	 * 
	 * @param eleOrderLine
	 * @throws Exception
	 */
	private boolean checkCancelledLine(Element eleOrderLine) throws Exception {
		// DeliveryMethod="PICK" AND FulfillmentType="BOPIS" / MaxLineStatus="9000" /ExtnOriginalFulfillmentType=""
		return ("BOPIS".equals(eleOrderLine.getAttribute("FulfillmentType")) || "STS".equals(eleOrderLine.getAttribute("FulfillmentType"))) 
				&& "9000".equals(eleOrderLine.getAttribute("MaxLineStatus"))
				&& (YFCObject.isVoid(getExtnOriginalFulfillmentType(eleOrderLine)) || "BOPIS".equals(getExtnOriginalFulfillmentType(eleOrderLine))) ? true : false;
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
	 * This method adds Cancelled Item Info to the ProductInfo element
	 * 
	 * @param inDoc
	 * @param eleOrderLine
	 * @throws Exception
	 */
	private void AddCancelledItem(Document inDoc, Element eleOrderLine) throws Exception {
		Element eleCancelled = null;
		if (!YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/Cancelled"))) {
			eleCancelled = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/Cancelled");
		} else {
			eleCancelled = XMLUtil.createElement(inDoc, "Cancelled", null);
			Element eleProductInfo = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo");
			eleProductInfo.appendChild(eleCancelled);
		}
		double orderQty = Double.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"));
		double cancelQty = Double.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"))
				- Double.parseDouble(eleOrderLine.getAttribute("OrderedQty"));
		addCancelItemInfo(inDoc, eleOrderLine, Double.toString(orderQty), Double.toString(cancelQty), eleCancelled);
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
	 * This method prepares the Cancelled Item Info to be added
	 * 
	 * @param inDoc
	 * @param eleOrderLine
	 * @throws Exception
	 */
	private void addCancelItemInfo(Document inDoc, Element eleOrderLine, String orderQty, String cancelQty,
			Element eleToAdd) throws Exception {
		String olkey = eleOrderLine.getAttribute("OrderLineKey");
		String olUnitPrice = XMLUtil.getString(inDoc,
				"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/LinePriceInfo/@UnitPrice");
		Element eleItemInfo = createItemInfo(inDoc, olkey, orderQty, olUnitPrice);
		eleItemInfo.setAttribute("CancelQty", cancelQty);
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
	 * @param env
	 * @param inDoc
	 */
	private Document updateEmailTemplateForOrdersHavingASlines(YFSEnvironment env, Document inDoc) {
		log.beginTimer("updateEmailTemplateForOrdersHavingASlines");
		Element eleIndoc = inDoc.getDocumentElement();
		try {
			addUrls(inDoc, env, eleIndoc);
			eleIndoc.setAttribute("CustomerName", AcademyEmailUtil.getBillToCustomerName(eleIndoc));
			eleIndoc.setAttribute("AlternamePickUpPerson", AcademyEmailUtil.getAlternamePickUpCustomerName(eleIndoc));
			eleIndoc.setAttribute("DayPhone", AcademyEmailUtil.getDayPhone(eleIndoc));
			AcademyEmailUtil.removeOrderlines(inDoc, env, eleIndoc);
			boolean isASRFCPemail = AcademyEmailUtil.isAlternateLineRFCP(inDoc);
			NodeList nlOrderLines = AcademyEmailUtil.getCompleteOrderLineLines(eleIndoc);
			boolean bHasCancelledLine = eleIndoc.getAttribute(AcademyConstants.ATTR_HAS_CANCELLED_LINES).equalsIgnoreCase(AcademyConstants.ATTR_Y) ? true : false;
			createProductInfoElement(inDoc);
			//boolean considerCancelledLines = considerCancelledLines(eleIndoc,isASREFCPemail, bHasCancelledLine);
			//if (bHasCancelledLine && considerCancelledLines) {
			if (isASRFCPemail) {
				// AS shipment
				AcademyEmailUtil.updateMessageRef(env, eleIndoc, "RFCP_AS_MSG_ID_PROD", "RFCP_AS_MSG_ID_STAGE","PN_RFCP_MSG_TYPE");
			} else {
				if (bHasCancelledLine) {
					AcademyEmailUtil.updateMessageRef(env, eleIndoc, "PARTIAL_RFCP_MSG_ID_PROD","PARTIAL_RFCP_MSG_ID_STAGE", "PN_PARTIAL_RFCP_MSG_TYPE");
				} else {
					AcademyEmailUtil.updateMessageRef(env, eleIndoc, "RFCP_MSG_ID_PROD", "RFCP_MSG_ID_STAGE","PN_RFCP_MSG_TYPE");
				}
			}

			for (int i = 0; i < nlOrderLines.getLength(); i++) {

				Element eleOrderline = (Element) nlOrderLines.item(i);
				boolean isAlternateStoreLine = AcademyEmailUtil.isAlternateStoreLine(eleOrderline);

				if (isAlternateStoreLine) {
					if (isASRFCPemail) {
						if (checkRFCPLine(eleOrderline)) {
							AddRFCPItem(inDoc, eleOrderline);
						} else if (checkPartialRFCPLine(eleOrderline)) {
							AddRFCPItem(inDoc, eleOrderline);
							AddCancelledItem(inDoc, eleOrderline);
						} else if (checkCancelledLine(eleOrderline)) {
							AddCancelledItem(inDoc, eleOrderline);
						}
					}
				} else if (checkRFCPLine(eleOrderline)) {
					AddRFCPItem(inDoc, eleOrderline);
				} else if (checkPartialRFCPLine(eleOrderline)) {
					AddRFCPItem(inDoc, eleOrderline);
					AddCancelledItem(inDoc, eleOrderline);
				} else if (checkCancelledLine(eleOrderline)) {
					AddCancelledItem(inDoc, eleOrderline);
				}

			}

		} catch (Exception e) {
			log.error("Exception in updateEmailTemplateForOrdersHavingASlines \n "+e.getMessage());
			YFSException custExp = new YFSException(e.getMessage());
			throw(custExp);
		}
		
		log.endTimer("updateEmailTemplateForOrdersHavingASlines");
		return inDoc;
	}
	

	private boolean checkASRFCPLine(boolean isOriginalBOPIS, boolean isAlternateStoreLine) throws Exception {
		if(isOriginalBOPIS && isAlternateStoreLine) {
			return false;
		}
		return true;
	}
	

	private boolean checkPendingLines(Element eleOrderLine) throws NumberFormatException, Exception {
		boolean bcheckPendingLines = !YFCObject.isVoid(eleOrderLine) && "PICK".equals(eleOrderLine.getAttribute("DeliveryMethod"))
				&& "BOPIS".equals(eleOrderLine.getAttribute("FulfillmentType")) && AcademyEmailUtil.isBelowRFCPStatus(eleOrderLine.getAttribute("MaxLineStatus"))
				&& (YFCObject.isVoid(getExtnOriginalFulfillmentType(eleOrderLine)))
				&& (!YFCObject.isVoid(eleOrderLine.getAttribute("OrderedQty")) && !YFCObject.isVoid(eleOrderLine.getAttribute("OriginalOrderedQty")) 
						&&  Double.parseDouble(eleOrderLine.getAttribute("OrderedQty")) == Double.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty")))
				? true : false;
		return bcheckPendingLines;
	}

	private void checkAndAddPendingLines(Document inDoc,Element eleOrderLine) throws Exception {
		Element elePending = null;
		if(!YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(),"/Order/ProductInfo/Pending"))) {
			elePending =(Element) XPathUtil.getNode(inDoc.getDocumentElement(),"/Order/ProductInfo/Pending");
		} else {
			elePending = XMLUtil.createElement(inDoc, "Pending", null);
			Element eleProductInfo = (Element)XPathUtil.getNode(inDoc.getDocumentElement(),"/Order/ProductInfo");
			eleProductInfo.appendChild(elePending);
		}
		String orderQty = eleOrderLine.getAttribute("OrderedQty");
		addItemInfo(inDoc, eleOrderLine,orderQty, elePending);
		
	}
	
    /**
     * This method returns 
     * true when cancel lined need to be ignored i.e not to display cancel line in RFCP email
     * false when cancel lined need to display in RFCP email
     * @param eleIndoc
     * @param isOriginalBOPIS
     * @param bHasCancelledLine
     * @return
     * @throws Exception
     */
	public static  boolean considerCancelledLines(Element eleIndoc,boolean isASREFCPemail, boolean bHasCancelledLine) throws Exception {
		if (bHasCancelledLine) {
			int iASPCanceledLines = getTotalASPCancelledLinesCount(eleIndoc);
			if (iASPCanceledLines > 0) {
				int iTotalCanceledLines = getTotalCancelledLinesCount(eleIndoc);
				if (iTotalCanceledLines == iASPCanceledLines) {
					if(!isASREFCPemail) {
						return false;
					}else {
						return true;
					}
					
				}
			}
		}
		return true;
	}
	
	// Alternate StorePick Email changes
	private static int getTotalASPCancelledLinesCount(Element eleIndoc) throws Exception {
		NodeList nlCanceledLines = XPathUtil.getNodeList(eleIndoc, "/Order/OrderLines/OrderLine[@MaxLineStatus='9000']/Extn[@ExtnIsASP='Y']");
		return nlCanceledLines.getLength();
	}
	// Alternate StorePick Email changes
	// Alternate StorePick Email changes
	private static int getTotalCancelledLinesCount(Element eleIndoc) throws Exception {
		NodeList nlCanceledLines = XPathUtil.getNodeList(eleIndoc, "/Order/OrderLines/OrderLine[@MaxLineStatus='9000']");
		return nlCanceledLines.getLength();
	}
	// Alternate StorePick Email changes
}
