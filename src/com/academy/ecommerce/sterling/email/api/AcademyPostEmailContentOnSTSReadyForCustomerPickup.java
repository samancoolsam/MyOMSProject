package com.academy.ecommerce.sterling.email.api;

import java.util.Properties;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.email.util.AcademyEmailUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantriks.yih.adapter.util.YantriksConstants;

public class AcademyPostEmailContentOnSTSReadyForCustomerPickup  implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPostEmailContentOnSTSReadyForCustomerPickup.class);

	public Document prepareEmailContent(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("AcademyPostEmailContentOnSTSReadyForCustomerPickup.prepareEmailContent() start :" + XMLUtil.getXMLString(inDoc));
		updateEMailContent(env, inDoc);
		sendEmail(env, inDoc);
		log.verbose("AcademyPostEmailContentOnSTSReadyForCustomerPickup.prepareEmailContent() end :" + XMLUtil.getXMLString(inDoc));
		return inDoc;

	}
	
	public Document prepareASEmailContent(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("AcademyPostEmailContentOnSTSReadyForCustomerPickup.prepareASEmailContent() start :" + XMLUtil.getXMLString(inDoc));
		boolean isASRFCPemail = AcademyEmailUtil.isAlternateLineRFCP(inDoc);
		if (hasOnlyPendingASlines(inDoc) && !isASRFCPemail) {
			log.verbose("Shipment is full shortpicked & has only Alternate Store Pick Lines, Delay email not applicable for AS lines");
			return inDoc;
		} else {
			updateAlternateStoreLineEMailContent(env, inDoc);
		    sendEmail(env, inDoc);
		}	
		log.verbose("AcademyPostEmailContentOnSTSReadyForCustomerPickup.prepareASEmailContent() end :" + XMLUtil.getXMLString(inDoc));
		return inDoc;

	}

	private void updateEMailContent(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Inside updateEMailContent"+XMLUtil.getXMLString(inDoc));
		Element eleIndoc = inDoc.getDocumentElement();
		eleIndoc.setAttribute("CustomerName", AcademyEmailUtil.getBillToCustomerName(eleIndoc));
		eleIndoc.setAttribute("AlternamePickUpPerson", AcademyEmailUtil.getAlternamePickUpCustomerName(eleIndoc));
		eleIndoc.setAttribute("DayPhone", AcademyEmailUtil.getDayPhone(eleIndoc));
		
		NodeList currentShipmentLines = getCurrentShipmentLines(eleIndoc);		
		//populate messageID
		if(isRFCPLineExist(eleIndoc,currentShipmentLines)) {
			AcademyEmailUtil.updateMessageRef(env, eleIndoc, "STS_RFCP_MSG_ID_PROD", "STS_RFCP_MSG_ID_STG", "STS_RFCP_MSG_TYPE");
		} else {
			AcademyEmailUtil.updateMessageRef(env, eleIndoc, "STS_MSG_ID_PROD", "STS_MSG_ID_STG", "STS_MSG_TYPE");
		}
		AcademyEmailUtil.addUrls(inDoc, env, eleIndoc);
		for (int i = 0; i < currentShipmentLines.getLength(); i++) {
			Element shipmentLine = (Element) currentShipmentLines.item(i);
			Element eleOrderLine = getOrderLine(eleIndoc,shipmentLine.getAttribute("OrderLineKey"));
			
			createProductInfoElement(inDoc);
			if(checkRFCPLine(eleOrderLine)) {
				checkAndAddRFCPItem(inDoc,eleOrderLine);
			} else if(checkPartialRFCPLine(eleOrderLine)) {
				checkAndAddRFCPItem(inDoc,eleOrderLine);
				checkAndAddCancelledItem(inDoc,eleOrderLine);
			} else if(checkDelayedLine(eleOrderLine)) {
				checkAndAddDelayedItem(inDoc,eleOrderLine);
			} else if (checkPartialDelayedLine(eleOrderLine)) {
				checkAndAddDelayedItem(inDoc,eleOrderLine);
				checkAndAddCancelledItem(inDoc,eleOrderLine);
			} else if(checkCancelledLine(eleOrderLine)) {
				checkAndAddCancelledItem(inDoc,eleOrderLine);
			}
			
		}
		log.verbose("Done updateEMailContent"+XMLUtil.getXMLString(inDoc));
		
	}
	
	private void sendEmail(YFSEnvironment env, Document inDoc) throws Exception {
		//send customer email
		log.verbose("Before Sending customer email :"+XMLUtil.getXMLString(inDoc));
		String customerEMailID = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID);
		if(!YFCCommon.isVoid(customerEMailID) && customerEMailID.contains(",")) {
			customerEMailID = customerEMailID.substring(0, customerEMailID.indexOf(","));
			inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID, customerEMailID);
		}
		Document emailSentOutDoc = AcademyUtil.invokeService(env, AcademyConstants.ACAD_STS_EMAIL_SERVICE, inDoc);
		log.verbose("Sent customer email :"+XMLUtil.getXMLString(emailSentOutDoc));
		//check and send alternate email
		if(AcademyEmailUtil.isAlternamePickUpCustomerEmailExist(inDoc.getDocumentElement())) {
			AcademyEmailUtil.setAlternamePickUpCustomerEmailExist(inDoc.getDocumentElement());
			log.verbose("Before Sending Alternate pickup email :"+XMLUtil.getXMLString(inDoc));
			emailSentOutDoc = AcademyUtil.invokeService(env, AcademyConstants.ACAD_STS_EMAIL_SERVICE, inDoc);
			log.verbose("Sent Alternate pickup email :"+XMLUtil.getXMLString(emailSentOutDoc));
		}
	}

	private boolean isRFCPLineExist(Element eleIndoc,NodeList currentShipmentLines) throws Exception {
		for (int i = 0; i < currentShipmentLines.getLength(); i++) {
			Element shipmentLine = (Element) currentShipmentLines.item(i);
			Element eleOrderLine = getOrderLine(eleIndoc,shipmentLine.getAttribute("OrderLineKey"));
			if(checkRFCPLine(eleOrderLine) || checkPartialRFCPLine(eleOrderLine)) {
				return true;
			}
		}
		return false;
		
	}
	
	private boolean checkRFCPLine(Element eleOrderLine) throws Exception {
		return !YFCObject.isVoid(eleOrderLine) && "PICK".equals(eleOrderLine.getAttribute("DeliveryMethod"))
				&& "BOPIS".equals(eleOrderLine.getAttribute("FulfillmentType")) && "3350.400".equals(eleOrderLine.getAttribute("MaxLineStatus"))
				&& (YFCObject.isVoid(getExtnOriginalFulfillmentType(eleOrderLine)))
				&& (!YFCObject.isVoid(eleOrderLine.getAttribute("OrderedQty")) && !YFCObject.isVoid(eleOrderLine.getAttribute("OriginalOrderedQty")) 
						&&  Double.parseDouble(eleOrderLine.getAttribute("OrderedQty")) == Double.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"))) ? true : false;
	}

	private boolean checkPartialRFCPLine(Element eleOrderLine) throws Exception {
		double dOrderedQty = 0.00;
		if (!YFCObject.isVoid(eleOrderLine.getAttribute("OrderedQty"))) {
			dOrderedQty = Double.parseDouble(eleOrderLine.getAttribute("OrderedQty"));
		}
		double dOriginalOrderedQty = Double.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"));
		return !YFCObject.isVoid(eleOrderLine) && "PICK".equals(eleOrderLine.getAttribute("DeliveryMethod"))
				&& "BOPIS".equals(eleOrderLine.getAttribute("FulfillmentType")) && "3350.400".equals(eleOrderLine.getAttribute("MaxLineStatus"))
				&& (YFCObject.isVoid(getExtnOriginalFulfillmentType(eleOrderLine)))
				&& (!YFCObject.isDoubleVoid(dOrderedQty) && !YFCObject.isVoid(dOriginalOrderedQty) 
						&&  dOrderedQty != dOriginalOrderedQty) ? true : false;
	}

	private boolean checkCancelledLine(Element eleOrderLine) throws Exception {
		// DeliveryMethod="PICK" AND FulfillmentType="BOPIS" / MaxLineStatus="9000" /ExtnOriginalFulfillmentType=""
		return !YFCObject.isVoid(eleOrderLine) && "PICK".equals(eleOrderLine.getAttribute("DeliveryMethod"))
				&& ("BOPIS".equals(eleOrderLine.getAttribute("FulfillmentType")) || "STS".equals(eleOrderLine.getAttribute("FulfillmentType"))) 
				&& "9000".equals(eleOrderLine.getAttribute("MaxLineStatus"))
				&& (YFCObject.isVoid(getExtnOriginalFulfillmentType(eleOrderLine)) || "BOPIS".equals(getExtnOriginalFulfillmentType(eleOrderLine))) ? true : false;
	}
	

	private boolean checkDelayedLine(Element eleOrderLine) throws Exception {
		//bopis DELAYED -sts -DeliveryMethod="PICK" AND FulfillmentType="STS" / MaxLineStatus="2160.00.01" / ExtnOriginalFulfillmentType="BOPIS"
		//- bopis DELAYED -sts and =""  OrderedQty="8.00" OriginalOrderedQty="8.00"
		String strMaxLineStatus = eleOrderLine.getAttribute("MaxLineStatus");
		return !YFCObject.isVoid(eleOrderLine) && "PICK".equals(eleOrderLine.getAttribute("DeliveryMethod"))
				//&& "STS".equals(eleOrderLine.getAttribute("FulfillmentType")) && "2160.00.01".equals(eleOrderLine.getAttribute("MaxLineStatus"))
				//Changes for OMNI-63470
				&& "STS".equals(eleOrderLine.getAttribute("FulfillmentType")) && !StringUtil.isEmpty(strMaxLineStatus) && strMaxLineStatus.startsWith(YantriksConstants.V_STATUS_2160_00_01)
				&& (!YFCObject.isVoid(getExtnOriginalFulfillmentType(eleOrderLine))) && "BOPIS".equals(getExtnOriginalFulfillmentType(eleOrderLine))
				&& (!YFCObject.isVoid(eleOrderLine.getAttribute("OrderedQty")) && !YFCObject.isVoid(eleOrderLine.getAttribute("OriginalOrderedQty")) 
						&& Double.parseDouble(eleOrderLine.getAttribute("OrderedQty")) == Double.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"))) ? true : false;

	}

	private boolean checkPartialDelayedLine(Element eleOrderLine) throws Exception {
		//bopis DELAYED -sts -DeliveryMethod="PICK" AND FulfillmentType="STS" / MaxLineStatus="2160.00.01" / ExtnOriginalFulfillmentType="BOPIS"
		//- bopis DELAYED -sts and =""  OrderedQty="6.00" OriginalOrderedQty="8.00"
		String strMaxLineStatus = eleOrderLine.getAttribute("MaxLineStatus");
		return !YFCObject.isVoid(eleOrderLine) && "PICK".equals(eleOrderLine.getAttribute("DeliveryMethod"))
				//&& "STS".equals(eleOrderLine.getAttribute("FulfillmentType")) && "2160.00.01".equals(eleOrderLine.getAttribute("MaxLineStatus"))
				//Changes for OMNI-63470
				&& "STS".equals(eleOrderLine.getAttribute("FulfillmentType")) && !StringUtil.isEmpty(strMaxLineStatus) && strMaxLineStatus.startsWith(YantriksConstants.V_STATUS_2160_00_01)
				&& (!YFCObject.isVoid(getExtnOriginalFulfillmentType(eleOrderLine))) && "BOPIS".equals(getExtnOriginalFulfillmentType(eleOrderLine))
				&& (!YFCObject.isVoid(eleOrderLine.getAttribute("OrderedQty")) && !YFCObject.isVoid(eleOrderLine.getAttribute("OriginalOrderedQty")) 
						&& Double.parseDouble(eleOrderLine.getAttribute("OrderedQty")) != Double.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"))) ? true : false;

	}
	
	
	private void createProductInfoElement(Document inDoc) throws Exception {
		if(YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(),"/Order/ProductInfo"))) {
			Element eleProductInfo = XMLUtil.createElement(inDoc, "ProductInfo", null);
			inDoc.getDocumentElement().appendChild(eleProductInfo);
		}
	}

	private void checkAndAddCancelledItem(Document inDoc,Element eleOrderLine) throws Exception {
		Element eleCancelled = null;
		if(!YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(),"/Order/ProductInfo/Cancelled"))) {
			eleCancelled =(Element) XPathUtil.getNode(inDoc.getDocumentElement(),"/Order/ProductInfo/Cancelled");
		} else {
			eleCancelled = XMLUtil.createElement(inDoc, "Cancelled", null);
			Element eleProductInfo = (Element)XPathUtil.getNode(inDoc.getDocumentElement(),"/Order/ProductInfo");
			eleProductInfo.appendChild(eleCancelled);
		}
		double orderQty = Double.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"));
		double cancelQty = Double.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty")) - Double.parseDouble(eleOrderLine.getAttribute("OrderedQty"));
		addCancelItemInfo(inDoc, eleOrderLine,Double.toString(orderQty), Double.toString(cancelQty),eleCancelled);
	}

	private void checkAndAddDelayedItem(Document inDoc,Element eleOrderLine) throws Exception {
		Element eleDelayed = null;
		if(!YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(),"/Order/ProductInfo/Delayed"))) {
			eleDelayed =(Element) XPathUtil.getNode(inDoc.getDocumentElement(),"/Order/ProductInfo/Delayed");
		} else {
			eleDelayed = XMLUtil.createElement(inDoc, "Delayed", null);
			Element eleProductInfo = (Element)XPathUtil.getNode(inDoc.getDocumentElement(),"/Order/ProductInfo");
			eleProductInfo.appendChild(eleDelayed);
		}
		String orderQty = eleOrderLine.getAttribute("OrderedQty");
		addItemInfo(inDoc, eleOrderLine,orderQty, eleDelayed);		
	}
	
	private void checkAndAddRFCPItem(Document inDoc,Element eleOrderLine) throws Exception {
		Element eleFRCP = null;
		if(!YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(),"/Order/ProductInfo/RFCP"))) {
			eleFRCP =(Element) XPathUtil.getNode(inDoc.getDocumentElement(),"/Order/ProductInfo/RFCP");
		} else {
			eleFRCP = XMLUtil.createElement(inDoc, "RFCP", null);
			Element eleProductInfo = (Element)XPathUtil.getNode(inDoc.getDocumentElement(),"/Order/ProductInfo");
			eleProductInfo.appendChild(eleFRCP);
		}
		String orderQty = eleOrderLine.getAttribute("OrderedQty");
		addItemInfo(inDoc, eleOrderLine,orderQty, eleFRCP);
		
	}

	private Element createItemInfo(Document inDoc, String olkey, String orderQty, String olUnitPrice) throws Exception {
		Element eleItemInfo = XMLUtil.createElement(inDoc, "ItemInfo", null);		
		eleItemInfo.setAttribute("OrderLineKey", olkey);
		eleItemInfo.setAttribute("ItemID", XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/ItemDetails/@ItemID"));
		eleItemInfo.setAttribute("Description", XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/ItemDetails/PrimaryInformation/@Description"));
		eleItemInfo.setAttribute("OrderQty", orderQty);
		eleItemInfo.setAttribute("ImageLoc", XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/ItemDetails/PrimaryInformation/@ImageLocation"));
		eleItemInfo.setAttribute("ImageID", XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/ItemDetails/PrimaryInformation/@ImageID"));
		eleItemInfo.setAttribute("UnitPrice",olUnitPrice);
		return eleItemInfo;
	}

	private void addItemInfo(Document inDoc, Element eleOrderLine,String orderQty, Element eleToAdd) throws Exception {
		String olkey = eleOrderLine.getAttribute("OrderLineKey");
		String olUnitPrice = XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/LinePriceInfo/@UnitPrice");
		Element eleItemInfo = createItemInfo(inDoc, olkey,orderQty,olUnitPrice);
		eleToAdd.appendChild(eleItemInfo);
	}

	private void addCancelItemInfo(Document inDoc, Element eleOrderLine,String orderQty,String cancelQty, Element eleToAdd) throws Exception {
		String olkey = eleOrderLine.getAttribute("OrderLineKey");
		String olUnitPrice = XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/LinePriceInfo/@UnitPrice");
		Element eleItemInfo = createItemInfo(inDoc, olkey,orderQty,olUnitPrice);
		eleItemInfo.setAttribute("CancelQty", cancelQty);
		eleToAdd.appendChild(eleItemInfo);
	}

	private String getExtnOriginalFulfillmentType(Element eleOrderLine) throws Exception {
		String olkey = eleOrderLine.getAttribute("OrderLineKey");
		return XMLUtil.getString(eleOrderLine, "/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/Extn/@ExtnOriginalFulfillmentType");
	}

	private NodeList getCurrentShipmentLines(Element eleIndoc) throws Exception  {
		String currentShipmentKey = XPathUtil.getString(eleIndoc, "/Order/@CurrentShipmentKey");
		return XPathUtil.getNodeList(eleIndoc, "/Order/Shipments/Shipment[@ShipmentKey='" + currentShipmentKey + "']/ShipmentLines/ShipmentLine");
	}
	
	private Element getOrderLine(Element eleIndoc , String orderlineKey) throws Exception {
		return  (Element) XPathUtil.getNode(eleIndoc,"/Order/OrderLines/OrderLine[@OrderLineKey='" + orderlineKey + "']");
	}
	
	
	
	/**
	 * This Method prepare email content xml for combination of below <br/>
	 * i) BOPIS + AS <br/>
	 * ii) BOPIS + STS + AS <br/>
	 * @param env
	 * @param inDoc
	 * @throws Exception
	 */
	private void updateAlternateStoreLineEMailContent(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer("updateAlternateStoreLineEMailContent");

		Element eleIndoc = inDoc.getDocumentElement();
		eleIndoc.setAttribute("CustomerName", AcademyEmailUtil.getBillToCustomerName(eleIndoc));
		eleIndoc.setAttribute("AlternamePickUpPerson", AcademyEmailUtil.getAlternamePickUpCustomerName(eleIndoc));
		eleIndoc.setAttribute("DayPhone", AcademyEmailUtil.getDayPhone(eleIndoc));

		NodeList currentShipmentLines = getCurrentShipmentLines(eleIndoc);
		AcademyEmailUtil.addUrls(inDoc, env, eleIndoc);	

		if(isRFCPLineExistNotAS(eleIndoc,currentShipmentLines)) {
			AcademyEmailUtil.updateMessageRef(env, eleIndoc, "STS_RFCP_MSG_ID_PROD", "STS_RFCP_MSG_ID_STG", "STS_RFCP_MSG_TYPE");
		} else {
			AcademyEmailUtil.updateMessageRef(env, eleIndoc, "STS_MSG_ID_PROD", "STS_MSG_ID_STG", "STS_MSG_TYPE");
		}

		for (int i = 0; i < currentShipmentLines.getLength(); i++) {
			Element shipmentLine = (Element) currentShipmentLines.item(i);
			Element eleOrderLine = getOrderLine(eleIndoc, shipmentLine.getAttribute("OrderLineKey"));

			boolean isAlternateStoreLine = AcademyEmailUtil.isAlternateStoreLine(eleOrderLine);
			createProductInfoElement(inDoc);
			if (isAlternateStoreLine ) {
				if(checkPendingLines(eleOrderLine))
					checkAndAddPendingLines(inDoc, eleOrderLine);
				// According to requirement not to display AS cancelled & AS in RFCP in email.
			} else if (checkRFCPLine(eleOrderLine)) {
				checkAndAddRFCPItem(inDoc, eleOrderLine);
			} else if (checkPartialRFCPLine(eleOrderLine)) {
				checkAndAddRFCPItem(inDoc, eleOrderLine);
				checkAndAddCancelledItem(inDoc, eleOrderLine);
			} else if (checkDelayedLine(eleOrderLine)) {
				checkAndAddDelayedItem(inDoc, eleOrderLine);
			} else if (checkPartialDelayedLine(eleOrderLine)) {
				checkAndAddDelayedItem(inDoc, eleOrderLine);
				checkAndAddCancelledItem(inDoc, eleOrderLine);
			} else if (checkCancelledLine(eleOrderLine)) {
				checkAndAddCancelledItem(inDoc, eleOrderLine);
			} 

		}

		log.verbose("Updated EMail Contenet" + XMLUtil.getXMLString(inDoc));
		log.endTimer("updateAlternateStoreLineEMailContent");
	}
	
	private boolean isRFCPLineExistNotAS(Element eleIndoc, NodeList currentShipmentLines) throws Exception {
		for (int i = 0; i < currentShipmentLines.getLength(); i++) {
			Element shipmentLine = (Element) currentShipmentLines.item(i);
			Element eleOrderLine = getOrderLine(eleIndoc,shipmentLine.getAttribute("OrderLineKey"));
			if( (checkRFCPLine(eleOrderLine) || checkPartialRFCPLine(eleOrderLine)) && !AcademyEmailUtil.isAlternateStoreLine(eleOrderLine) ) {
				return true;
			}
		}
		return false;
	}

	private boolean checkPendingLines(Element eleOrderLine) throws NumberFormatException, Exception {
		boolean bcheckPendingLines = !YFCObject.isVoid(eleOrderLine)
				&& AcademyEmailUtil.isBelowRFCPStatus(eleOrderLine.getAttribute("MaxLineStatus")) ? true : false;
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

	
	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
