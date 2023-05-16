package com.academy.ecommerce.sterling.email.util;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyEmailUtil {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyEmailUtil.class);
	
	/**
	 * This method updates urls to the output email messsage xml
	 *  
	 * @param inDoc
	 * @param env
	 * @param eleIndoc
	 * @throws Exception
	 */
	public static void addUrls(Document inDoc,YFSEnvironment env,Element eleIndoc) throws Exception {
	    Element eleInputShip= null;
		String strCurbPickUpLink =null;
		String strShipmentNo = null;
		String strShipNode = null;
		String strZipCode = null;
		String strCurrentShipmentKey = null;
		//OMNI-72427 start
		String strBillingZipCodeArg = AcademyConstants.CURBSIDE_BILLING_ZIPCODE_ARG;
		//OMNI-72427 End
				
		String strOrderNo = eleIndoc.getAttribute(AcademyConstants.ATTR_ORDER_NO);
		strCurrentShipmentKey = eleIndoc.getAttribute(AcademyConstants.STR_CURRENT_SHIPMENTKEY);
		eleInputShip = (Element) XPathUtil.getNode(inDoc,
				"/Order/Shipments/Shipment[@ShipmentKey='" + strCurrentShipmentKey + "']");
		log.verbose("eleInputShip"+XMLUtil.getElementXMLString(eleInputShip));
		if (!YFCCommon.isVoid(eleInputShip)) {
		strShipmentNo = eleInputShip.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		strShipNode = eleInputShip.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
		}
		Element elePersonInfoBillTo =SCXmlUtil.getChildElement(eleIndoc, AcademyConstants.ELE_PERSON_INFO_BILL_TO);
		if (!YFCCommon.isVoid(elePersonInfoBillTo)) {
			strZipCode = elePersonInfoBillTo.getAttribute(AcademyConstants.ZIP_CODE);
		}
		//Fetching CURBSIDEPICKUP URL from CO file
		String strCurbPickUpOrderLink = YFSSystem.getProperty(AcademyConstants.PROP_CURB_PICKUP_ORDER_URL);
		//https://uat1www.academy.com/curbside?details=$Order_No-$Shipment_No-$Store_No	
		if (!YFCCommon.isVoid(strCurbPickUpOrderLink)) {
			strCurbPickUpLink = strCurbPickUpOrderLink.replace(AcademyConstants.ATT_$ORDER_NO, strOrderNo);
			strCurbPickUpLink = strCurbPickUpLink.replace(AcademyConstants.ATT_$SHIPMENT_NO, strShipmentNo);
			strCurbPickUpLink = strCurbPickUpLink.replace(AcademyConstants.ATT_$STORE_NO, strShipNode);
			//OMNI-72427 start
			strBillingZipCodeArg=strBillingZipCodeArg.concat(strZipCode);
			strCurbPickUpLink = strCurbPickUpLink.concat(strBillingZipCodeArg);
			//OMNI-72427 End

			eleIndoc.setAttribute("URL_CurbsidePickup", strCurbPickUpLink);
		}
		//Fetching ORDERDETAILS URL from CO file
		String strViewOrderDetails = YFSSystem.getProperty(AcademyConstants.PROP_VIEW_ORDERDETAILS_URL);
		//https://uat4www.academy.com/webapp/wcs/stores/servlet/UserAccountOrderStatus?orderId=@@@@&zipCode=$$$$&langId=-1&storeId=10151&catalogId=10051&isSubmitted=true&URL=NonAjaxOrderDetail&errorViewName=GuestOrderStatusView&splitshipstatus=true&isDisplayLeftNav=false
		if (!YFCCommon.isVoid(strViewOrderDetails)) {
			String strURL_ViewOrderDetails = strViewOrderDetails.replace("@@@@", strOrderNo);
			strURL_ViewOrderDetails = strURL_ViewOrderDetails.replace("$$$$", strZipCode);
			eleIndoc.setAttribute("URL_ViewOrderDetails", strURL_ViewOrderDetails);
		}
		
		String strCancelDelayedItems = YFSSystem.getProperty(AcademyConstants.PROP_CANCEL_DELAYED_ITEMS_URL);
		if (!YFCCommon.isVoid(strCancelDelayedItems)) {
			String strURL_CancelDelayedItems = strCancelDelayedItems.replace("@@@@", strOrderNo);
			strURL_CancelDelayedItems = strURL_CancelDelayedItems.replace("$$$$", strZipCode);
			eleIndoc.setAttribute("URL_CancelDelayedItems", strURL_CancelDelayedItems);
		}
	}
	
	/**
	 * This method updates MessageID in input xml by performing a common code
	 * lookup.
	 * 
	 * @param env
	 * @param eleIndoc
	 * @param sMsgIDProd
	 * @param sMsgIdStg
	 * @throws Exception
	 */
	
	public static void updateMessageRef(YFSEnvironment env, Element eleIndoc, String sMsgIDProd, String sMsgIdStg,String sMsgType)
            throws Exception {
        log.verbose("Inside updateMessageRef Method");
        String strMessageID = "";
        HashMap<String, String> hmUnicodeMap = AcademyCommonCode.getCommonCodeListAsHashMap(env,
                AcademyConstants.LISTRAK_EMAIL_CODES, AcademyConstants.PRIMARY_ENTERPRISE);
        String strListrakProdEnabled = YFSSystem.getProperty(AcademyConstants.IS_LISTRAK_PROD_ENABLED);
        if (!YFCObject.isVoid(strListrakProdEnabled) && (strListrakProdEnabled.equals(AcademyConstants.ATTR_Y))) {
            strMessageID = hmUnicodeMap.get(sMsgIDProd);
        } else {
            strMessageID = hmUnicodeMap.get(sMsgIdStg);
        }
        String strMessageType=hmUnicodeMap.get(sMsgType);
        log.verbose("MessageID::" + strMessageID);
        eleIndoc.setAttribute(AcademyConstants.MESSAGE_ID, strMessageID);
        eleIndoc.setAttribute("MessageType", strMessageType);
        log.verbose("MessageType::" + strMessageType);
    }
	
	//Start : OMNI-38153
	/**
	 * This method Compares the list of OrderlineKey with the orderlineKey 
	 * fetched in the hashset and removes the orderline
	 * element from inDoc that doesn't match
	 * 
	 * @param inDoc
	 * @param eleIndoc
	 * @throws Exception
	 */
	
	public static void removeOrderlines(Document inDoc,YFSEnvironment env,Element eleIndoc) throws Exception {
		log.verbose("Inside removeOrderlines Method");
		NodeList currentShipmentLines = getCurrentShipmentLines(eleIndoc);
		Set<String> hsShipmentOrderLineKeys = new HashSet<String>();
		//Fetches the list of orderlinekey for the currentShipmentKey

		for (int i = 0; i < currentShipmentLines.getLength(); i++) {
			Element shipmentLine = (Element) currentShipmentLines.item(i);
			//fetch orderlineKey for current shipment line
			String strOrderlineKey =shipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			//store orderlineKey in hashset
			hsShipmentOrderLineKeys.add(strOrderlineKey);

		}

		NodeList completeOrderLines = getCompleteOrderLineLines(eleIndoc);
		for (int i = 0; i < completeOrderLines.getLength(); i++) {
			Element eleOrderline = (Element) completeOrderLines.item(i);
			//fetch orderlineKey for current orderline line
			String strOLK =eleOrderline.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);					
			if (!hsShipmentOrderLineKeys.contains(strOLK)) {					
				//remove current orderline from inDoc
				eleOrderline.getParentNode().removeChild(eleOrderline);
			}	
		}

	}
	//End : OMNI-38153
	
	public static String getBillToCustomerName(Element eleIndoc) throws Exception {
		Element elePersonInfoBillTo =SCXmlUtil.getChildElement(eleIndoc, AcademyConstants.ELE_PERSON_INFO_BILL_TO);
		if (!YFCObject.isVoid(elePersonInfoBillTo)) {
			 return elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_FNAME)
					.concat(AcademyConstants.STR_BLANK)
					.concat(elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_LNAME));
		}
		return "";
	}
	
	public static String getAlternamePickUpCustomerName(Element eleIndoc) throws Exception {
		Element elePersonInfoMarkFor =SCXmlUtil.getChildElement(eleIndoc, AcademyConstants.ELE_PERSON_INFO_MARK_FOR);
		if (!YFCObject.isVoid(elePersonInfoMarkFor)) {
			return elePersonInfoMarkFor.getAttribute(AcademyConstants.ATTR_FNAME)
				.concat(AcademyConstants.STR_BLANK)
				.concat(elePersonInfoMarkFor.getAttribute(AcademyConstants.ATTR_LNAME));
		}
		return null;
	}
	
	public static void setAlternamePickUpCustomerEmailExist(Element eleIndoc) throws Exception {
		Element elePersonInfoMarkFor = SCXmlUtil.getChildElement(eleIndoc, AcademyConstants.ELE_PERSON_INFO_MARK_FOR);
		if (!YFCObject.isVoid(elePersonInfoMarkFor) && elePersonInfoMarkFor.hasAttribute(AcademyConstants.ATTR_EMAILID)) {
			String strAltEmailID = elePersonInfoMarkFor.getAttribute(AcademyConstants.ATTR_EMAILID);
			log.verbose("Alternate EmailId :" + strAltEmailID);
			eleIndoc.setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID, strAltEmailID);
		}
	}
	
	public static boolean isAlternamePickUpCustomerEmailExist(Element eleIndoc) throws Exception {
		Element elePersonInfoMarkFor = SCXmlUtil.getChildElement(eleIndoc, AcademyConstants.ELE_PERSON_INFO_MARK_FOR);
		return !YFCObject.isVoid(elePersonInfoMarkFor) && elePersonInfoMarkFor.hasAttribute(AcademyConstants.ATTR_EMAILID) ? true : false;
	}
	
	public static String getDayPhone(Element eleIndoc) throws Exception {
		String sDayPhone=SCXmlUtil.getXpathAttribute(eleIndoc, "/Order/OrderLines/OrderLine[@DeliveryMethod='PICK']/PersonInfoShipTo/@DayPhone");
		if(!YFCObject.isVoid(sDayPhone) && !sDayPhone.isEmpty() && sDayPhone.length() > 7) {
			return sDayPhone=sDayPhone.substring(0,3).concat("-").concat(sDayPhone.substring(3,6)).concat("-").concat(sDayPhone.substring(7));
		}
		return null;
	}

	
	public static NodeList getCurrentShipmentLines(Element eleIndoc) throws Exception  {
		String currentShipmentKey = XPathUtil.getString(eleIndoc, "/Order/@CurrentShipmentKey");
		return XPathUtil.getNodeList(eleIndoc, "/Order/Shipments/Shipment[@ShipmentKey='" + currentShipmentKey + "']/ShipmentLines/ShipmentLine");
	}
	
	//Start : OMNI-38153

	public static NodeList getCompleteOrderLineLines(Element eleIndoc) throws Exception  {
		return XPathUtil.getNodeList(eleIndoc, "/Order/OrderLines/OrderLine");
	}

	//End : OMNI-38153
	
	//Start : OMNI-74228 : BOPIS/STS Consolidated Pickup Reminders
	public static void removeNonRFCPOrderlines(Element eleIndoc) throws Exception {
		log.verbose("Inside removeNonRFCPOrderlines Method");
		NodeList nlRFCPLines = XPathUtil.getNodeList(eleIndoc, 
				AcademyConstants.XPATH_RFCP_SHIPMENT_LINES);
		Set<String> hsShipmentOrderLineKeys = new HashSet<>();
		
		for (int i = 0; i < nlRFCPLines.getLength(); i++) {
			Element shipmentLine = (Element) nlRFCPLines.item(i);
			String strOrderlineKey = shipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			hsShipmentOrderLineKeys.add(strOrderlineKey);
		}
		log.verbose("RFCP OLKs" + hsShipmentOrderLineKeys);
		NodeList completeOrderLines = getCompleteOrderLineLines(eleIndoc);
		for (int i = 0; i < completeOrderLines.getLength(); i++) {
			Element eleOrderline = (Element) completeOrderLines.item(i);
			String strOLK =eleOrderline.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			if (!hsShipmentOrderLineKeys.contains(strOLK)) {					
				eleOrderline.getParentNode().removeChild(eleOrderline);
			}	
		}
		log.verbose("After removing non-RFCP lines: " + XMLUtil.getElementXMLString(eleIndoc));
	}
	//End : OMNI-74228 : BOPIS/STS Consolidated Pickup Reminders
	
	/**
	 * @param inDoc
	 * @return
	 * @throws Exception
	 * @author CTS
	 * @Return TRUE when HasAlternateStorePickLines=Y </br>
	 *         FALSE HasAlternateStorePickLines=N/Null
	 */
	public static boolean hasAlternateStorePickLines(Document inDoc) throws Exception {
		log.verbose("hasAlternateStorePickLines");
		boolean hasAlternateStorePickLines = false;
		hasAlternateStorePickLines = XMLUtil.getString(inDoc, "/Order/@HasAlternateStorePickLines")
				.equalsIgnoreCase("Y") ? true : false;
		log.verbose("hasAlternateStorePickLines return :: " + hasAlternateStorePickLines);
		return hasAlternateStorePickLines;
	}

	/**
	 * This Method returns Boolean value true when current shipment is BOPIS.
	 * @param inDoc
	 * @return
	 * @throws Exception
	 * @author CTS
	 * @MethodName isOriginalBOPIS
	 * @Return TRUE when Current Shipment ShipNode is not equal to Order/Extn/@ExtnASShipNode </br>
	 *         FALSE when other conditions.
	 */
	public static boolean isOriginalBOPIS(Document inDoc) throws Exception {
		String strCurrentShpKey = XPathUtil.getString(inDoc, "/Order/@CurrentShipmentKey");
		String strShipmnetShipNode = XPathUtil.getString(inDoc, "/Order/Shipments/Shipment[@ShipmentKey='" + strCurrentShpKey + "' and ./ShipmentLines/ShipmentLine[@Quantity>0] ]/@ShipNode");
		String strExtnASShipNode = XPathUtil.getString(inDoc, "/Order/Extn/@ExtnASShipNode");
		return strExtnASShipNode.equalsIgnoreCase(strShipmnetShipNode) ? false : true;
	}
	
	/**
	 * This Method returns Boolean value true when current shipment is BOPIS.
	 * @param inDoc
	 * @return
	 * @throws Exception
	 * @author CTS
	 * @MethodName isAlternateLineRFCP
	 * @Return TRUE when Current Shipment ShipNode is not equal to Order/Extn/@ExtnASShipNode </br>
	 *         FALSE when other conditions.
	 */
	public static boolean isAlternateLineRFCP(Document inDoc) throws Exception {
		String strCurrentShpKey = XPathUtil.getString(inDoc, "/Order/@CurrentShipmentKey");
		String strShipmnetShipNode = XPathUtil.getString(inDoc, "/Order/Shipments/Shipment[@ShipmentKey='" + strCurrentShpKey + "' and ./ShipmentLines/ShipmentLine[@Quantity>0] ]/@ShipNode");
		String strExtnASShipNode = XPathUtil.getString(inDoc, "/Order/Extn/@ExtnASShipNode");
		return strExtnASShipNode.equalsIgnoreCase(strShipmnetShipNode) ? true : false;
	}
	
	/**
	 * 
	 * @param eleOrderline
	 * @return
	 * @throws Exception
	 * @author CTS
	 * @MethodName isAlternateStoreLine
	 * @Return TRUE when current line is AlternateStorePick Line <br> FALSE when current line is not AlternateStorePick Line
	 */
	public static boolean isAlternateStoreLine(Element eleOrderline) throws Exception {
		String olkey = eleOrderline.getAttribute("OrderLineKey");
		String strExtnIsASP = XMLUtil.getString(eleOrderline, "/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/Extn/@ExtnIsASP");
		return strExtnIsASP.equalsIgnoreCase("Y") ? true : false;
	}
	
	
	public static boolean isBelowRFCPStatus(String strMinLineStatus) {
		Double dMinLineStatus = getDoubleDataType(strMinLineStatus);
		Double dRFCPStatus = getDoubleDataType("3350.400");
		return Double.compare(dMinLineStatus,dRFCPStatus) < 0 ? true : false;
	}

	private static Double getDoubleDataType(String strValue) {
		DecimalFormat decfor = new DecimalFormat("0.00");
		Double dValue = Double.valueOf(strValue);
    	return dValue;
    }
	

	

	
}
