package com.academy.util.common;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyPublishCustomerInfoToGSM {
	
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyPublishCustomerInfoToGSM.class);
	
	public Document publishCustomerInfoTOGSM(YFSEnvironment env, Document inXML) throws Exception {

		Document docCustomerInfoDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		
		try {
			log.verbose(
					"AcademyPublishCustomerInfoToGSM: publishCustomerInfoTOGSM inXML:" + XMLUtil.getXMLString(inXML));
			
			String strFirstName = null;
			String strLastName = null;
			String strDayPhone = null;
			String strShipNode = null;
			String strReceivingNode = null;
			Element eleInXML = inXML.getDocumentElement();
			String strIsAddPaymentRequired=eleInXML.getAttribute("IsAddPaymentRequired");
			String strOrderNo = eleInXML.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			String strSalesOrderNo = strOrderNo;
			if(YFCObject.isNull(strOrderNo)) {
				strOrderNo=XPathUtil.getString(eleInXML,"/Shipment/ShipmentLines/ShipmentLine/@OrderNo");	
			}
			String strDocumentType = eleInXML.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
			Element eleShipment = docCustomerInfoDoc.getDocumentElement();
			String strFulfillmentType = null;
			
			if (!AcademyConstants.SALES_DOCUMENT_TYPE.equals(strDocumentType)) {
				strReceivingNode = eleInXML.getAttribute(AcademyConstants.ATTR_RECV_NODE);
				strSalesOrderNo = XMLUtil.getAttributeFromXPath(inXML, AcademyConstants.XPATH_CHAINED_SALES_ORDER_NO);
				eleShipment.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strReceivingNode);
				eleShipment.setAttribute(AcademyConstants.ATTR_SALES_ORDER_NO, strSalesOrderNo);
				//Code Changes for OMNI-105753--Start
				String strExtnRMSTransferNo= SCXmlUtil.getXpathAttribute(eleInXML, "Extn/@ExtnRMSTransferNo");
				eleShipment.setAttribute(AcademyConstants.ATTR_DISTROBNR, strExtnRMSTransferNo);
				//Code Changes for OMNI-105753--end
			} else {
				strShipNode = eleInXML.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
				eleShipment.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strShipNode);
			}
			//Code Changes for OMNI-99059 & OMNI-99060--Start
				if(!YFCCommon.isVoid(strIsAddPaymentRequired) && AcademyConstants.V_ACTIVE_FLAG.equals(strIsAddPaymentRequired))
				{
					addPaymentDetails(env,strSalesOrderNo,eleShipment);
				}
			//Code Changes for OMNI-99059 & OMNI-99060--End

			String strShipmentNo = eleInXML.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			Element eleBillTOAddressFromOutDoc = (Element) eleInXML
					.getElementsByTagName(AcademyConstants.ELE_BILL_TO_ADDRESS).item(0);
			if (!YFCObject.isVoid(eleBillTOAddressFromOutDoc)) {
				strFirstName = eleBillTOAddressFromOutDoc.getAttribute(AcademyConstants.ATTR_FNAME);
				strLastName = eleBillTOAddressFromOutDoc.getAttribute(AcademyConstants.ATTR_LNAME);
				strDayPhone = eleBillTOAddressFromOutDoc.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
			}

			NodeList nlShipmentLineFromOutDoc = XPathUtil.getNodeList(eleInXML,"/Shipment/ShipmentLines/ShipmentLine");
			
			eleShipment.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);

			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
						
			Element eleBillTOAddress = SCXmlUtil.createChild(eleShipment,AcademyConstants.ELE_BILL_TO_ADDRESS);
			eleShipment.appendChild(eleBillTOAddress);
			eleBillTOAddress.setAttribute(AcademyConstants.ATTR_FNAME, strFirstName);
			eleBillTOAddress.setAttribute(AcademyConstants.ATTR_LNAME, strLastName);
			eleBillTOAddress.setAttribute(AcademyConstants.ATTR_DAY_PHONE, strDayPhone);
			Element eleShipmentLines = SCXmlUtil.createChild(eleShipment,AcademyConstants.ELE_SHIPMENT_LINES);
			eleShipment.appendChild(eleShipmentLines);
						
			for (int i = 0; i < nlShipmentLineFromOutDoc.getLength(); i++) {
				Element eleShipmentLine = SCXmlUtil.createChild(eleShipmentLines,AcademyConstants.ELE_SHIPMENT_LINE);
				Element eleShipmentTagSerials = SCXmlUtil.createChild(eleShipmentLine,AcademyConstants.ELE_SHIPEMNT_TAG_SERIALS);
				eleShipmentLines.appendChild(eleShipmentLine);
				Element eleShipmentLineFromOutDoc = (Element) nlShipmentLineFromOutDoc.item(i);
				eleShipmentLine.setAttribute(AcademyConstants.ATTR_ITEM_DESC,
						eleShipmentLineFromOutDoc.getAttribute(AcademyConstants.ATTR_ITEM_DESC));
				eleShipmentLine.setAttribute(AcademyConstants.ATTR_ITEM_ID,
						eleShipmentLineFromOutDoc.getAttribute(AcademyConstants.ATTR_ITEM_ID));
				String strReducedQty = eleShipmentLineFromOutDoc.getAttribute(AcademyConstants.ATTR_QUANTITY_REDUCED);
				if(!YFCObject.isVoid(strReducedQty) && (Double.parseDouble(strReducedQty)>0)) {
					eleShipmentLine.setAttribute(AcademyConstants.ATTR_ACTUAL_QUANTITY,	strReducedQty);
					eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, strReducedQty);					
				}else {
					eleShipmentLine.setAttribute(AcademyConstants.ATTR_ACTUAL_QUANTITY,
							eleShipmentLineFromOutDoc.getAttribute(AcademyConstants.ATTR_ACTUAL_QUANTITY));
					eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY,
							eleShipmentLineFromOutDoc.getAttribute(AcademyConstants.ATTR_QUANTITY));
				}
				eleShipmentLine.setAttribute(AcademyConstants.ATTR_PRIME_LINE_NO,
						eleShipmentLineFromOutDoc.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO));
				eleShipmentLine.setAttribute(AcademyConstants.SUB_LINE_NO,
						eleShipmentLineFromOutDoc.getAttribute(AcademyConstants.SUB_LINE_NO));
				eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHIP_LINE_NO,
						eleShipmentLineFromOutDoc.getAttribute(AcademyConstants.ATTR_SHIP_LINE_NO));
				eleShipmentLine.setAttribute(AcademyConstants.ATTR_PROD_CLASS,
						eleShipmentLineFromOutDoc.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
				eleShipmentLine.appendChild(eleShipmentTagSerials);
					NodeList nlShipmentTagSerial = XPathUtil.getNodeList(eleShipmentLineFromOutDoc, "ShipmentTagSerials/ShipmentTagSerial");
					strFulfillmentType = XPathUtil.getString(eleShipmentLineFromOutDoc,"OrderLine/@FulfillmentType");	
				if(!YFCObject.isVoid(strFulfillmentType) && AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE.equals(strFulfillmentType)) {
					for (int j = 0; j < nlShipmentTagSerial.getLength(); j++) {
							Element eleShipmentTagSerialsOut = (Element) nlShipmentTagSerial.item(j);
							Element eleShipmentTagSerial = SCXmlUtil.createChild(eleShipmentTagSerials,AcademyConstants.ELE_SHIPMENT_TAG_SERIAL);		
							eleShipmentTagSerial.setAttribute(AcademyConstants.ATTR_SERIAL_NO, eleShipmentTagSerialsOut.getAttribute(AcademyConstants.ATTR_SERIAL_NO));
							eleShipmentTagSerials.appendChild(eleShipmentTagSerial);
				}
			}
					
			}
			log.verbose("customerInfoDoc:" + XMLUtil.getXMLString(docCustomerInfoDoc));

		} catch (Exception e) {
			log.error("Exception - AcademyPublishCustomerInfoToGSM():: publishCustomerInfoToGSM::" + e.getMessage());
		}

		return docCustomerInfoDoc;
	}
		
		/**
		 * Code Changes for OMNI-99059 & OMNI-99060
		 * Code Changes to add payment details
		 */
		private void addPaymentDetails(YFSEnvironment env, String strSalesOrderNo, Element eleShipment) throws Exception {
	
					Document getOrderListInDoc = SCXmlUtil.createDocument(AcademyConstants.ELE_ORDER);
					Element eleOrder=getOrderListInDoc.getDocumentElement();
					eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO, strSalesOrderNo);
					eleOrder.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.A_ACADEMY_DIRECT);
					eleOrder.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.STR_DOCUMENT_TYPE_VALUE);
	
					log.verbose("AcademyPublishCustomerInfoToGSM_addPaymentDetails" + SCXmlUtil.getString(getOrderListInDoc));
					String getOrderListOutTemplate = "<OrderList><Order OrderHeaderKey=''><PaymentMethods><PaymentMethod DisplayCreditCardNo='' PaymentType=''/></PaymentMethods></Order></OrderList>";
					Document outputTemplate =SCXmlUtil.createFromString(getOrderListOutTemplate);
					env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, outputTemplate);
					Document getOrderListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, getOrderListInDoc);
					env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
					log.verbose("AcademyPublishCustomerInfoToGSM_addPaymentDetails" + SCXmlUtil.getString(getOrderListOutDoc));
				    Element elePaymentMethod=SCXmlUtil.getXpathElement(getOrderListOutDoc.getDocumentElement(), "Order/PaymentMethods/PaymentMethod[@PaymentType='Credit_Card' or @PaymentType='PLCC']");
					if(!YFCCommon.isVoid(elePaymentMethod))
				    {
					    	Element eleShipPaymentMethod=SCXmlUtil.createChild(eleShipment, AcademyConstants.ELE_PAYMENT);
					    	eleShipPaymentMethod.setAttribute("DisplayCreditCardNo", elePaymentMethod.getAttribute("DisplayCreditCardNo"));
					    	eleShipPaymentMethod.setAttribute("PaymentType", elePaymentMethod.getAttribute("PaymentType"));
					}
					log.verbose("AcademyPublishCustomerInfoToGSM_addPaymentDetails out Element:" + SCXmlUtil.getString(eleShipment));
				
		}
		/* End change for OMNI-99059 & OMNI-99060 */
		
		/**
		 * Code Changes for sending STS FA Cancelled items information to GSM
		 */
		
		public Document publishSTSCustomerInfoTOGSM( Document inXML) throws Exception {

			Document docCustomerInfoDoc = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			
			try {
				log.verbose(
						"AcademyPublishCancelInfoToGSM: publishSTSCustomerInfoTOGSM inXML:" + XMLUtil.getXMLString(inXML));
				
				String strFirstName = null;
				String strLastName = null;
				String strDayPhone = null;
				String strBackroomPickedQuantity = null;
				String strShortageQty = null ;
				String strExtnReasonCode = null ;
				String strOrderNo = null ;
				String strCancelledQuantity = null;
				
				Element eleInXML = inXML.getDocumentElement();
				Element eleOrder = docCustomerInfoDoc.getDocumentElement();
				String rootElementName = eleInXML.getNodeName();
				
				if(AcademyConstants.ELE_SHIPMENT.equalsIgnoreCase(rootElementName)) {
					
					strOrderNo = eleInXML.getAttribute(AcademyConstants.ATTR_ORDER_NO);
					eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
					Element eleBillTOAddressFromOutDoc = (Element) eleInXML
							.getElementsByTagName(AcademyConstants.ELE_BILL_TO_ADDRESS).item(0);
					
					if (!YFCObject.isVoid(eleBillTOAddressFromOutDoc)) {
						strFirstName = eleBillTOAddressFromOutDoc.getAttribute(AcademyConstants.ATTR_FNAME);
						strLastName = eleBillTOAddressFromOutDoc.getAttribute(AcademyConstants.ATTR_LNAME);
						strDayPhone = eleBillTOAddressFromOutDoc.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
					}
					
					Element eleBillTOAddress = SCXmlUtil.createChild(eleOrder,AcademyConstants.ELE_PERSON_INFO_BILL_TO);
					eleOrder.appendChild(eleBillTOAddress);
					eleBillTOAddress.setAttribute(AcademyConstants.ATTR_FNAME, strFirstName);
					eleBillTOAddress.setAttribute(AcademyConstants.ATTR_LNAME, strLastName);
					eleBillTOAddress.setAttribute(AcademyConstants.ATTR_DAY_PHONE, strDayPhone);
					
					Element eleOrderLines = SCXmlUtil.createChild(eleOrder,AcademyConstants.ELE_ORDER_LINES);
					eleOrder.appendChild(eleOrderLines);

					NodeList nlShipmentLineFromOutDoc = XPathUtil.getNodeList(eleInXML,"/Shipment/ShipmentLines/ShipmentLine");
								
					for (int i = 0; i < nlShipmentLineFromOutDoc.getLength(); i++) {
						
						Element eleOrderLine = SCXmlUtil.createChild(eleOrderLines,AcademyConstants.ELE_ORDER_LINE);
						Element eleItem = SCXmlUtil.createChild(eleOrderLine,AcademyConstants.ELEM_ITEM);
						eleOrderLines.appendChild(eleOrderLine);
						Element eleShipmentLineFromOutDoc = (Element) nlShipmentLineFromOutDoc.item(i);
						eleItem.setAttribute(AcademyConstants.ATTR_ITEM_DESC,
								eleShipmentLineFromOutDoc.getAttribute(AcademyConstants.ATTR_ITEM_DESC));
						eleItem.setAttribute(AcademyConstants.ATTR_ITEM_ID,
								eleShipmentLineFromOutDoc.getAttribute(AcademyConstants.ATTR_ITEM_ID));
						eleItem.setAttribute(AcademyConstants.ATTR_PROD_CLASS,
								eleShipmentLineFromOutDoc.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
						eleOrderLine.appendChild(eleItem);
						
						Element eleExtn =(Element) eleShipmentLineFromOutDoc.getElementsByTagName(AcademyConstants.ATTR_EXTN).item(0);
						strExtnReasonCode = eleExtn.getAttribute(AcademyConstants.STR_EXTN_REASON_CODE);
						strBackroomPickedQuantity= eleShipmentLineFromOutDoc.getAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY);
						strShortageQty= eleShipmentLineFromOutDoc.getAttribute(AcademyConstants.ATTR_SHORTAGE_QTY);
						strCancelledQuantity = String.valueOf(Double.parseDouble(strBackroomPickedQuantity) - Double.parseDouble(strShortageQty)) ;
						if(AcademyConstants.STR_CUSTOMER_ABANDONED.equalsIgnoreCase(strExtnReasonCode)) {
							strCancelledQuantity = strBackroomPickedQuantity ;
						}
						eleOrderLine.setAttribute("CancelledQuantity",strCancelledQuantity);
						
						eleOrderLine.setAttribute(AcademyConstants.ATTR_PRIME_LINE_NO,
								eleShipmentLineFromOutDoc.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO));
						eleOrderLine.setAttribute(AcademyConstants.SUB_LINE_NO,
								eleShipmentLineFromOutDoc.getAttribute(AcademyConstants.SUB_LINE_NO));		
							
					}
				}
				
				else if(AcademyConstants.ELE_ORDER.equalsIgnoreCase(rootElementName)) {
					
					strOrderNo = eleInXML.getAttribute(AcademyConstants.ATTR_ORDER_NO);
					eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
					
					Element elePersonInfoBillToFromOutDoc = (Element) eleInXML
							.getElementsByTagName(AcademyConstants.ELE_PERSON_INFO_BILL_TO).item(0);
					
					if (!YFCObject.isVoid(elePersonInfoBillToFromOutDoc)) {
						strFirstName = elePersonInfoBillToFromOutDoc.getAttribute(AcademyConstants.ATTR_FNAME);
						strLastName = elePersonInfoBillToFromOutDoc.getAttribute(AcademyConstants.ATTR_LNAME);
						strDayPhone = elePersonInfoBillToFromOutDoc.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
					}
					Element elePersonInfoBillTo = SCXmlUtil.createChild(eleOrder,AcademyConstants.ELE_PERSON_INFO_BILL_TO);
					eleOrder.appendChild(elePersonInfoBillTo);
					elePersonInfoBillTo.setAttribute(AcademyConstants.ATTR_FNAME, strFirstName);
					elePersonInfoBillTo.setAttribute(AcademyConstants.ATTR_LNAME, strLastName);
					elePersonInfoBillTo.setAttribute(AcademyConstants.ATTR_DAY_PHONE, strDayPhone);
					
					Element eleOrderLines = SCXmlUtil.createChild(eleOrder,AcademyConstants.ELE_ORDER_LINES);
					eleOrder.appendChild(eleOrderLines);
					
					NodeList nlOrderLineFromOutDoc = XPathUtil.getNodeList(eleInXML,"/Order/OrderLines/OrderLine");
					for (int i = 0; i < nlOrderLineFromOutDoc.getLength(); i++) {
						
						Element eleOrderLine = SCXmlUtil.createChild(eleOrderLines,AcademyConstants.ELE_ORDER_LINE);
						Element eleItem = SCXmlUtil.createChild(eleOrderLine,AcademyConstants.ELEM_ITEM);
						eleOrderLines.appendChild(eleOrderLine);
						Element eleOrderLineFromOutDoc = (Element) nlOrderLineFromOutDoc.item(i);
						Element eleItemFromOutDoc =(Element) eleOrderLineFromOutDoc.getElementsByTagName("Item").item(0);
						eleItem.setAttribute(AcademyConstants.ATTR_ITEM_DESC,
								eleItemFromOutDoc.getAttribute(AcademyConstants.ATTR_ITEM_DESC));
						eleItem.setAttribute(AcademyConstants.ATTR_ITEM_ID,
								eleItemFromOutDoc.getAttribute(AcademyConstants.ATTR_ITEM_ID));
						eleItem.setAttribute(AcademyConstants.ATTR_PROD_CLASS,
								eleItemFromOutDoc.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
						eleOrderLine.appendChild(eleItem);
						
						Element eleCanceledFrom =(Element) eleOrderLineFromOutDoc.getElementsByTagName("CanceledFrom").item(0);
						strCancelledQuantity = eleCanceledFrom.getAttribute("Quantity");
						
						eleOrderLine.setAttribute("CancelledQuantity",strCancelledQuantity);
						
						eleOrderLine.setAttribute(AcademyConstants.ATTR_PRIME_LINE_NO,
								eleOrderLineFromOutDoc.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO));
						eleOrderLine.setAttribute(AcademyConstants.SUB_LINE_NO,
								eleOrderLineFromOutDoc.getAttribute(AcademyConstants.SUB_LINE_NO));	
						
					}
				}
				log.verbose("customerInfoDoc: " + XMLUtil.getXMLString(docCustomerInfoDoc));

			} catch (Exception e) {
				log.error("Exception - AcademyPublishCancelInfoToGSM():: publishSTSCustomerInfoTOGSM::" + e.getMessage());
			}

			return docCustomerInfoDoc;
		}
}
