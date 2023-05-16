package com.academy.ecommerce.sterling.interfaces.api;
/**
 * this below Custom Api handles to create a Web Return Orders. if Sales order lines is Shipped or Order Delivered statuses then we are allowing returns Creation.  
 */
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyProcessReturnOrderMessageFromWC {
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyProcessReturnOrderMessageFromWC.class);

	private Document returnReasonCodeList = null;

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	public void processReturnMessage(YFSEnvironment env, Document inDoc) {
		
	String strParentOrderNo = null;
	String strReturnOrderNo=null;
	String strReturnDate=null;
	String webRMStrackingNo=null;
	Element eleSalesOrderDetails = null;

	NodeList nListOrderLines = null;

	int iNoOfOrderLines = 0;

	Element eleCurrOrderLine = null;

	NodeList nMatchingSalesLineList = null;

	Element eleMatchingOrderLine = null;

	Document docCreateOrderInput = null;
	Map usedSalesLinesmp=new HashMap();	
		
		
		/*
		 * The incoming message from WC shall be used to create a ReturnOrder
		 * within Sterling. <Order OrderNo="" RMANo="" DocumentType="0003"
		 * ReturnUser="" ReturnDate="" > <OrderLines> <OrderLine ItemID=""
		 * ReturnQty="" ReturnReason="CHANGED_MY_MIND" ReturnReasonCode=""
		 * TotalCharge="" Tax="" ShippingCharge="" TrackingNo=""/> <OrderLine
		 * ItemID="" ReturnQty="" ReturnReason="CHANGED_MY_MIND"
		 * ReturnReasonCode="" TotalCharge="" Tax="" ShippingCharge=""
		 * TrackingNo=""/> </OrderLines> </Order>
		 */
		try {
			log.beginTimer(" Begining of AcademyProcessReturnOrderMessageFromWC->processReturnMessage Api");
			Element orderElem = inDoc.getDocumentElement();
			nListOrderLines = XMLUtil.getNodeList(inDoc.getDocumentElement(),
					AcademyConstants.XPATH_ORDERLINE);
			iNoOfOrderLines = nListOrderLines.getLength();
			strParentOrderNo = orderElem
					.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			strReturnOrderNo=orderElem.getAttribute("RMANo");
			webRMStrackingNo=orderElem.getAttribute(AcademyConstants.ATTR_TRACKING_NO);
			strReturnDate=orderElem.getAttribute("ReturnDate");
			
			if (!YFCObject.isVoid(strParentOrderNo)) {
				log
						.verbose("*************** Calling method to get complete order details of Sales Order ************ "
								+ strParentOrderNo);
				eleSalesOrderDetails = getSalesOrderList(
						strParentOrderNo,AcademyConstants.SALES_DOCUMENT_TYPE, env);
				log
						.verbose("*************** Complete - Get Order List Details::eleSalesOrderDetails:: ************ "
								+ XMLUtil
										.getElementXMLString(eleSalesOrderDetails));
				
				// Looping through all lines in input
				for (int i = 0; i < iNoOfOrderLines; i++) {
					eleCurrOrderLine = (Element) nListOrderLines.item(i);
									String strItemId = eleCurrOrderLine
							.getAttribute(AcademyConstants.ATTR_ITEM_ID);
					String strQty = eleCurrOrderLine
							.getAttribute(AcademyConstants.ATTR_RETURN_QTY);
					
					if (eleSalesOrderDetails.hasChildNodes()) {

						nMatchingSalesLineList = XPathUtil
								.getNodeList(
										eleSalesOrderDetails,
										"Order/OrderLines/OrderLine[(@MaxLineStatus!='3900' and @MaxLineStatus!='3950' and @MaxLineStatus!='9000') and ./Item/@ItemID='"+ strItemId + "']");
						if (!YFCObject.isVoid(nMatchingSalesLineList)) {
							int len=nMatchingSalesLineList.getLength();
							for(int j=0;j<len;j++){
							eleMatchingOrderLine = (Element) nMatchingSalesLineList
									.item(j);
							String qtyEligForReturn=getEligibleQty(eleMatchingOrderLine);
							if(Double.parseDouble(qtyEligForReturn)>=Double.parseDouble(strQty)){
							if (!YFCObject.isVoid(eleMatchingOrderLine)) {
								if(usedSalesLinesmp==null ||usedSalesLinesmp.get(eleMatchingOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY))==null){
								docCreateOrderInput = prepareInputForCreateOrder(env,eleSalesOrderDetails,eleMatchingOrderLine,eleCurrOrderLine,docCreateOrderInput,strReturnOrderNo,usedSalesLinesmp,orderElem,webRMStrackingNo,strReturnDate);
								break;
							}
							}
							}
							}
						}
					}
				}
				log.verbose("input xml to createOrder API in AcademyProcessReturnOrderMessageFromWC is :\n"+XMLUtil.getXMLString(docCreateOrderInput));
				AcademyUtil.invokeAPI(env,AcademyConstants.API_CREATE_ORDER, docCreateOrderInput);
				// # 2880 - Update notes for original sales order with new return order details
				// prepare input to changeOrder API with Notes
				log.verbose("Prepare input to changeOrder API to update Notes on sales order# :"+strParentOrderNo);
				Document docChangeOrderInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
				Element eleSalesOrder = docChangeOrderInput.getDocumentElement();
				Element eleNotes = docChangeOrderInput.createElement(AcademyConstants.ELE_NOTES);
				Element eleNote = docChangeOrderInput.createElement(AcademyConstants.ELE_NOTE);
				// Set required data
				Element salesOrderEle = (Element)eleSalesOrderDetails.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
				eleSalesOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, salesOrderEle.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
				eleSalesOrder.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.ATTR_Y);
				String noteTxt = "The return order# "+strReturnOrderNo;
				eleNote.setAttribute(AcademyConstants.ATTR_NOTE_TEXT, noteTxt);
				// append the child elements
				eleNotes.appendChild(eleNote);
				eleSalesOrder.appendChild(eleNotes);
				log.verbose("The Input xml to changeOrder API is :\n"+XMLUtil.getXMLString(docChangeOrderInput));
				// Invoke changeOrder API to update the Notes on Original SalesOrder
				AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, docChangeOrderInput);
				log.verbose("changeOrder API invoked successfully......");
				// # 2880
			}
			log.endTimer(" End of AcademyProcessReturnOrderMessageFromWC->processReturnMessage Api");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new YFSException(e.getMessage());
		}
		/*
		 * if(!YFCObject.isVoid(salesOrderElem)){
		 * 
		 * 
		 * 
		 * Element personInfoShipTo=(Element)
		 * salesOrderElem.getElementsByTagName("PersonInfoShipTo").item(0);
		 * Element personInfoBillTo=(Element)
		 * salesOrderElem.getElementsByTagName("PersonInfoBillTo").item(0);
		 * Element tempNode=(Element) personInfoShipTo.cloneNode(true); Element
		 * eletempNode=(Element) inDoc.importNode(tempNode, true);
		 * orderElem.appendChild(eletempNode); Element tempNode1=(Element)
		 * personInfoBillTo.cloneNode(true); Element eletempNode1=(Element)
		 * inDoc.importNode(tempNode1, true);
		 * orderElem.appendChild(eletempNode1); XMLUtil.importElement(inDoc,
		 * tempNode); Element tempNode1=(Element)
		 * personInfoBillTo.cloneNode(true); XMLUtil.importElement(inDoc,
		 * tempNode1); try { AcademyUtil.invokeService(env,
		 * "AcademyCreateReturnOrderFromWC", inDoc); } catch (Exception e) { //
		 * TODO Auto-generated catch block e.printStackTrace(); } }
		 */
	}

	

	private String getEligibleQty(Element eleMatchingOrderLine) {
		double eligQty=0,eligShippedQty=0;
		try {
			if(!YFCObject.isVoid(eleMatchingOrderLine)){
				//Status="3700" Shipped- allowing Shiipped status also eligible Qty for Returns.
			Element eleOrderStatus = (Element) XPathUtil.getNode(
					eleMatchingOrderLine,
					"OrderStatuses/OrderStatus[@Status='3700.7777']");
			
			Element eleOrderShippedStatus = (Element) XPathUtil.getNode(
					eleMatchingOrderLine,
					"OrderStatuses/OrderStatus[@Status='3700']");
			//checking for shipped Qty - with new Change - will allow Returns even if SalesOrder in Shipped Status
			if(!YFCObject.isVoid(eleOrderShippedStatus)){
				eligShippedQty=Double.parseDouble(eleOrderShippedStatus.getAttribute(AcademyConstants.ATTR_STAT_QTY));
			}
			//checking for Delivered Qty
			if(!YFCObject.isVoid(eleOrderStatus)){
				eligQty=Double.parseDouble(eleOrderStatus.getAttribute(AcademyConstants.ATTR_STAT_QTY));
			}
			
			eligQty=eligQty+eligShippedQty;
			
			}	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Double.toString(eligQty);
	}

	private Document prepareInputForCreateOrder(YFSEnvironment env,Element eleSalesOrderDetails,Element eleMatchingOrderLine, Element eleCurrOrderLine, Document docCreateOrderInput, String strReturnOrderNo, Map usedSalesLinesmp, Element orderElem,String webRMStrackingNo, String strReturnDate) {

		Element eleOrderInput = null;
		Element eleOrderLines = null;
		Element eleOrderLine = null;
		Element eleDerivedFrom = null;
		Element eleOrderLineTransQty = null;
		Element elePersonInfoBillTo = null;
		Element eleCurrentReturnLine = null;
		Element eleTemp = null;
		Element eleSalesOrderLine = null;
		Element eleLineCharges=null;
		Element eleLineTaxes=null;
		Element eleLineCharge=null;
		Element eleOrderExtn=null;
		Element eleOrderLineExtn=null;
		// #2880
		Element eleOrderNotes=null;
		try {
			log.beginTimer(" End of AcademyProcessReturnOrderMessageFromWC->prepareInputForCreateOrder Api");
			log
					.verbose("*************** Inside prepareInputForCreateOrder method************ ");
			elePersonInfoBillTo = (Element) XPathUtil.getNode(
					eleSalesOrderDetails, "Order/PersonInfoBillTo");
			
			String strItemId = eleCurrOrderLine
			.getAttribute(AcademyConstants.ATTR_ITEM_ID);
			String strQty = eleCurrOrderLine
			.getAttribute(AcademyConstants.ATTR_RETURN_QTY);
			// OMNI-64479- Start
			String strCLSShipNode = AcademyCommonCode.getCodeValue(env, AcademyConstants.STR_CLS_RETURN_NODE_VALUE,
					AcademyConstants.STR_ACTIVE, AcademyConstants.HUB_CODE);
			// OMNI-64479-End
			if (YFCObject.isVoid(docCreateOrderInput)) {
				docCreateOrderInput = XMLUtil
						.createDocument(AcademyConstants.ELE_ORDER);
				eleOrderInput = docCreateOrderInput.getDocumentElement();
				eleOrderInput.setAttribute(
						AcademyConstants.ATTR_APPLY_DEFLT_TMPLT,
						AcademyConstants.STR_YES);
				eleOrderInput.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
						AcademyConstants.STR_RETURN_DOCTYPE);
				// OMNI-64479-Start
				eleOrderInput.setAttribute(AcademyConstants.ATTR_RECV_NODE, strCLSShipNode);
				// OMNI-64479-End
				eleOrderInput.setAttribute(
						AcademyConstants.ATTR_ENTERPRISE_CODE,
						AcademyConstants.PRIMARY_ENTERPRISE);
				eleOrderInput.setAttribute(
						AcademyConstants.ATTR_FULFILLMENT_TYPE,
						AcademyConstants.STR_RETURN_FULFILLMENT);
				eleOrderInput.setAttribute(AcademyConstants.ATTR_SELL_ORG_CODE,
						AcademyConstants.PRIMARY_ENTERPRISE);
				eleOrderInput.setAttribute(
						AcademyConstants.ATTR_CREATED_AT_NODE,
						AcademyConstants.STR_NO);
				eleOrderInput.setAttribute(AcademyConstants.ATTR_ORDER_NO, strReturnOrderNo);
				eleOrderInput.setAttribute(AcademyConstants.ATTR_ORDER_DATE, strReturnDate);
				
				// Start Change - stamp CustomerEmailID on return order - Manju 12/28/2010 
				Element salesOrderEle = (Element)eleSalesOrderDetails.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
				String soCustEmailID = salesOrderEle.getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID);
				eleOrderInput.setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID, soCustEmailID);
				// End Change - stamp CustomerEmailID on return order - Manju 12/28/2010
				
				eleTemp = (Element) docCreateOrderInput.importNode(
						elePersonInfoBillTo, true);
				eleOrderInput.appendChild(eleTemp);
				// eleTemp=(Element)eleOrderInput.appendChild(elePersonInfoBillTo);
				// XMLUtil.importElement(eleOrderInput,eleTemp);

				eleOrderInput.appendChild(eleTemp);
				eleOrderExtn=docCreateOrderInput.createElement(AcademyConstants.ELE_EXTN);
				eleOrderExtn.setAttribute(AcademyConstants.ATTR_EXTN_IS_WEBORDER, AcademyConstants.STR_YES);
				eleOrderExtn.setAttribute(AcademyConstants.ATTR_EXTN_TRACKING_NO,webRMStrackingNo);
				eleOrderInput.appendChild(eleOrderExtn);
				
				// # 2880
				log.verbose("preparing Notes for Return Order :"+strReturnOrderNo);
				eleOrderNotes = docCreateOrderInput.createElement(AcademyConstants.ELE_NOTES);				
				Element eleNote = docCreateOrderInput.createElement(AcademyConstants.ELE_NOTE);
				String noteText = "Return Order created for sales order # "+salesOrderEle.getAttribute(AcademyConstants.ATTR_ORDER_NO);
				eleNote.setAttribute(AcademyConstants.ATTR_NOTE_TEXT, noteText);
				eleOrderNotes.appendChild(eleNote);
				eleOrderInput.appendChild(eleOrderNotes);
				log.verbose("Notes message : \n"+XMLUtil.getElementXMLString(eleOrderNotes));
				// #2880
				eleOrderLines = docCreateOrderInput
						.createElement(AcademyConstants.ELEM_ORDER_LINES);
				XMLUtil.importElement(eleOrderInput, eleOrderLines);	
			}
			String reasonCodeDetails = null;
			eleSalesOrderLine = eleMatchingOrderLine;
			if (!YFCObject.isVoid(eleSalesOrderLine)) {
				log
						.verbose("*************** Matching Sales Order Line Element************ "
								+ XMLUtil
										.getElementXMLString(eleSalesOrderLine));
			
			}
			if (!YFCObject.isVoid(eleSalesOrderLine)) {
				eleOrderLines=(Element) docCreateOrderInput.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINES).item(0);
				usedSalesLinesmp.put(eleMatchingOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY), eleMatchingOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
				eleOrderLine = docCreateOrderInput
						.createElement(AcademyConstants.ELEM_ORDER_LINE);
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ACTION,
						AcademyConstants.STR_CREATE);
				// OMNI-64479-Start
				eleOrderLine.setAttribute(AcademyConstants.SHIP_NODE, strCLSShipNode);
				// OMNI-64479-End
				eleOrderLine
						.setAttribute(
								AcademyConstants.ATTR_RETURN_REASON,
								eleCurrOrderLine
										.getAttribute(AcademyConstants.ATTR_RETURN_REASON));
				/*eleCurrOrderLine
				.getAttribute("ReturnReasonCode"));*/
//		
		//commenting this part as - with new requirements - we will get only ReturnReason - 0-10 values and that is storing under ReturnReason as above statement
				eleOrderLineExtn=docCreateOrderInput.createElement(AcademyConstants.ELE_EXTN);
				eleOrderLineExtn.setAttribute(AcademyConstants.ATTR_EXTN_RETURN_CODE, eleCurrOrderLine
						.getAttribute(AcademyConstants.ATTR_RETURN_REASON));
				eleOrderLine.appendChild(eleOrderLineExtn);
						
				
				/*eleOrderLineExtn=docCreateOrderInput.createElement("Extn");
				eleOrderLineExtn.setAttribute("ExtnTrackingNo", eleCurrOrderLine.getAttribute("TrackingNo"));*/
				eleDerivedFrom = docCreateOrderInput
						.createElement(AcademyConstants.ATTR_DERIVED_FROM);
				eleDerivedFrom
						.setAttribute(
								AcademyConstants.ATTR_ORDER_LINE_KEY,
								eleSalesOrderLine
										.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));

				eleOrderLineTransQty = docCreateOrderInput
						.createElement(AcademyConstants.ELE_ORDER_LINE_TRANSQTY);
				
				eleOrderLineTransQty.setAttribute(
						AcademyConstants.ATTR_ORDERED_QTY, strQty);
				
				eleOrderLineTransQty.setAttribute(
						AcademyConstants.ATTR_STATUS_QTY, strQty);
				eleOrderLineTransQty.setAttribute(
						AcademyConstants.ATTR_TRANS_UOM,
						AcademyConstants.UNIT_OF_MEASURE);
				eleLineCharges = docCreateOrderInput
				.createElement(AcademyConstants.ELE_LINE_CHARGES);
				eleLineTaxes=docCreateOrderInput.createElement(AcademyConstants.ELE_LINE_TAXES);
				eleLineCharge=docCreateOrderInput.createElement(AcademyConstants.ELE_LINE_CHARGE);
				eleLineCharge.setAttribute(AcademyConstants.ATTR_CHARGE_CATEGORY, AcademyConstants.STR_RETURNSHIPPING_CHARGE);
				eleLineCharge.setAttribute(AcademyConstants.ATTR_CHARGE_NAME, AcademyConstants.STR_RETURNSHIPPING_CHARGE);
				//double chrgperunit=(Double.parseDouble(eleCurrOrderLine.getAttribute("ShippingCharge")))/(Double.parseDouble(eleCurrOrderLine.getAttribute("ReturnQty")));
				eleLineCharge.setAttribute(AcademyConstants.ATTR_CHARGES_PER_LINE, eleCurrOrderLine.getAttribute(AcademyConstants.STR_SHIPPING_CHARGE));
				
				
		Element eleLineTax=docCreateOrderInput.createElement(AcademyConstants.ELE_LINE_TAX);
		eleLineTax.setAttribute(AcademyConstants.ATTR_TAX_NAME,
				AcademyConstants.STR_RETURNSHIP_TAX);
		 eleLineTax.setAttribute(AcademyConstants.ATTR_CHARGE_NAME,AcademyConstants.STR_RETURNSHIPPING_CHARGE);
		  eleLineTax.setAttribute(AcademyConstants.ATTR_CHARGE_CATEGORY,AcademyConstants.STR_RETURNSHIPPING_CHARGE);
		eleLineTax.setAttribute(AcademyConstants.ATTR_TAX,eleCurrOrderLine.getAttribute(AcademyConstants.ATTR_TAX));
				XMLUtil.importElement(eleLineTaxes, eleLineTax);
				XMLUtil.importElement(eleLineCharges, eleLineCharge);
				XMLUtil.importElement(eleOrderLine, eleLineCharges);
				XMLUtil.importElement(eleOrderLine, eleLineTaxes);
				XMLUtil.importElement(eleOrderLine, eleDerivedFrom);
				XMLUtil.importElement(eleOrderLine, eleOrderLineTransQty);
				XMLUtil.importElement(eleOrderLines, eleOrderLine);
				
			} else {
				log
						.verbose("***************Throwing exception since no matching Sales Order line found for item************ ");
				throw new Exception(
						"No Matching Sales Order Line found for Item"
								+ strItemId + " and Quantity" + strQty);
			}
			log.endTimer(" End of AcademyProcessReturnOrderMessageFromWC->prepareInputForCreateOrder Api");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		log
				.verbose("*************** Exiting prepareInputForCreateOrder method************ ");
		if (!YFCObject.isVoid(docCreateOrderInput))
			log
					.verbose("*************** Returning document docCreateOrderInput ************ "
							+ XMLUtil.getXMLString(docCreateOrderInput));
		return docCreateOrderInput;
	
		
	}

	private Element getSalesOrderList(String orderNo,
			String docType, YFSEnvironment env) {
		Element eleSalesOrderDetails = null;
		Element eleOrder = null;
		Document docGetOrderDetailsInput = null;
		Document docGetOrderDetailsOutput = null;

		try {
			log.beginTimer(" begin of AcademyProcessReturnOrderMessageFromWC->getSalesOrderList()");
			log
					.verbose("*************** Inside getCompleteSalesOrderDetails method************ ");
			docGetOrderDetailsInput = XMLUtil
					.createDocument(AcademyConstants.ELE_ORDER);
			eleOrder = docGetOrderDetailsInput.getDocumentElement();
			eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO, orderNo);
			eleOrder.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			eleOrder.setAttribute(AcademyConstants.ATTR_DOC_TYPE, docType);
			env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST,
					AcademyConstants.STR_TEMPLATEFILE_GETORDERLIST_DETAILS);
			if (!YFCObject.isVoid(docGetOrderDetailsInput))
				log
						.verbose("*************** Calling getCompleteOrderDetails API with input************ "
								+ XMLUtil.getXMLString(docGetOrderDetailsInput));
			docGetOrderDetailsOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_ORDER_LIST,
					docGetOrderDetailsInput);
			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
			
			if (!YFCObject.isVoid(docGetOrderDetailsOutput))
				log
						.verbose("*************** Output of getCompleteOrderDetails************ "
								+ XMLUtil
										.getXMLString(docGetOrderDetailsOutput));
			eleSalesOrderDetails = docGetOrderDetailsOutput
					.getDocumentElement();
			log.endTimer(" End of AcademyProcessReturnOrderMessageFromWC->getSalesOrderList Api");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		log
				.verbose("*************** Exiting getCompleteSalesOrderDetails method************ ");
		return eleSalesOrderDetails;
	}
}
