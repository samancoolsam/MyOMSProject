package com.academy.ecommerce.sterling.interfaces.api;

/**
 * This Custom Api implemented for Store/CLS Return Receipt process which gets the msg from external system..
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.ecommerce.sterling.util.AcademyReturnOrderUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCIterable;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyProcessReturnMessageAPI implements YIFCustomApi {
	
	

	
	private static final String Document = null;

	private static final String YFSEnvironment = null;

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyProcessReturnMessageAPI.class);

	/*
	 * Variable to store the Ship Node that's passed in the interface input
	 */
	String strShipNodeInInput = null;

	String strTrackingNo = null;

	String totalStoreRefundAmt = null;

	boolean bChangeOrder = false;
	//boolean checkProNumReturnOrder=false;
	boolean existedReturnCancelled=false;
	boolean bCreateOrder = false;
	boolean bRedLineCustomer = false;

	Document docCreateOrderInput = null;

	Document docReturnLinesReceiveDoc = null;

	Document docCreateOrderOutput = null;

	Document docChangeReturnOrderInput = null;

	Document docChangeReturnOrderInputList = null;

	Document docReturnLinesReceiveDocList = null;

	Document docRefundableReasonCodes = null;
	Document docRefundableReasonCodeValues = null;

	Element eleSalesOrderDetails = null;

	Document returnAvailQtyDoc = null;

	Element elePayments = null;

	String returnOrdDate = null;
	String salesInvoiceNo=null;
	String existedReturnReasonCode=null;
	String extnExistedReturnReasonCode=null;
	String clsReturnReason=null;
	String clsReturnReasonCode=null;
	String existedReturnReasonCodeVal=null;
	String existedReturnOrderNo=null;
	boolean bPaymentPresent = false;
	boolean removeLineShipCharges=true;
	boolean removeLineCharges = false;
	HashMap<String, Double> hmCLSItemList = null;
	HashMap<String, Double> hmInvoiceNoItemList = null;	
	HashMap<String, Double> hmgetOrderListItemList = null;
	boolean bQtyItemMismatch = false;
	// #2880
	String strReturnUser = null;
	// START OMNI-78324
	List<String> returnNodeList = null;
	// END OMNI-78324
		
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}
	public Document processReturnMessage(YFSEnvironment env, Document inputDoc) throws Exception {
		
		 Document inDoc=createReturnOrdMsg( env,inputDoc);
		Document docOutput = null;

		String strOrigSalesOrderNo = null;
		String strDocType = null;
		Element eleGetOrderInvoiceDetails = null;
		NodeList nListOrderLines = null;
		String strInvoiceNo = null;
		Element elePaymentMethod = null;
		NodeList nListOfRetOrders = null;
		Element eleReturnOrders = null;
		Element eleCurrOrderLine = null;
		int iNoOfOrderLines = 0;
		Element elePersonInfoBillTo = null;
	
				
		try {
			log.beginTimer(" Begining of processReturnMessage Api");
			log
					.verbose("*************** Inside processReturnMessage of Return Interface ************ ");
			strReturnUser = inDoc.getDocumentElement().getAttribute("ReturnUser");
			returnOrdDate = inDoc.getDocumentElement().getAttribute(
					"ReturnDate");
			salesInvoiceNo=inDoc.getDocumentElement().getAttribute(
					AcademyConstants.ATTR_INVOICE_NO);
			nListOrderLines = XMLUtil.getNodeList(inDoc.getDocumentElement(),
					AcademyConstants.XPATH_ORDERLINE);
			iNoOfOrderLines = nListOrderLines.getLength();
			bPaymentPresent = checkIfPaymentPresentInDocument(inDoc);

			// Start CR -18
			strInvoiceNo = inDoc.getDocumentElement().getAttribute(
					AcademyConstants.ATTR_INVOICE_NO);
			strTrackingNo = inDoc.getDocumentElement().getAttribute(
					AcademyConstants.ATTR_TRACKING_NO);
			// START OMNI-78324
			returnNodeList = AcademyCommonCode.getCodeValueList(env, AcademyConstants.STR_CLS_RETURN_NODE_VALUE, AcademyConstants.CONST_DEFAULT);
			// END OMNI-78324
			
			
			boolean isAcademyTrcNo = AcademyReturnOrderUtil.isAcademyTrackingNo(strTrackingNo); 
			//isAcademyTrackingNo(strTrackingNo);
			
			// End CR-18
			strOrigSalesOrderNo = inDoc.getDocumentElement().getAttribute(
					AcademyConstants.ATTR_ORDER_NO);

			strDocType = inDoc.getDocumentElement().getAttribute(
					AcademyConstants.ATTR_DOC_TYPE);
			strShipNodeInInput = inDoc.getDocumentElement().getAttribute(
					AcademyConstants.SHIP_NODE);
			elePaymentMethod = (Element) XPathUtil.getNode(inDoc
					.getDocumentElement(), AcademyConstants.ELE_PAYMENT_METHOD);

			/*
			 * Returns CR 18 changes start here
			 */
			
			//including changes to handle Return Reason Codes than ReturnReason Text.
			docRefundableReasonCodeValues = AcademyReturnOrderUtil.getRefundableReasonCodeValuesList(env);
			log
					.verbose("*************** Calling method to get order invoice details of Sales Order ************ "
							+ strInvoiceNo);
			//finding the sales Order Details by using InvoiceNo from Receipt msg
			eleGetOrderInvoiceDetails = getOrderInvoiceDetails(strInvoiceNo,env);
			// start
			Element eleOrder = (Element) eleGetOrderInvoiceDetails.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
			strOrigSalesOrderNo = eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			log.verbose("*************** Calling method to get complete order details of Sales Order ************ "+ strOrigSalesOrderNo);
			eleSalesOrderDetails = getCompleteSalesOrderDetails(strOrigSalesOrderNo, strDocType, env);
			log.verbose("*************** Complete Order Details::eleSalesOrderDetails:: ************ "+ XMLUtil.getElementXMLString(eleSalesOrderDetails));
			elePersonInfoBillTo = (Element) XPathUtil.getNode(eleSalesOrderDetails, "PersonInfoBillTo");
			eleReturnOrders = (Element) eleSalesOrderDetails.getElementsByTagName(AcademyConstants.ELE_RETURN_ORDERS).item(0);
			Document docOrderListOutput = getOrderListWithAllInvoice(eleGetOrderInvoiceDetails, env);
			prepareItemListHashMapForRequestedInvoice(eleGetOrderInvoiceDetails);
			Document outDocItemsList = getChildItemsList(env, docOrderListOutput, eleReturnOrders, inDoc);
			// end 
			// start chnages for 1204
			prepareCLSItemListHasMap(inputDoc, docOrderListOutput,outDocItemsList);
			
			/* Start - Changes made for STL 1087 - Throw exception on invalid item for an Invoice */
			//validateItemInInvoice(eleGetOrderInvoiceDetails, inputDoc);
			validateItemInInvoice(eleGetOrderInvoiceDetails, inputDoc,env);
			/* End - Changes made for STL 1087 - Throw exception on invalid item for an Invoice */
			
//			Commented as part of OMNI-81582 and moved code before CLS hashmap  
//			//End chnages for 1204
//			Element eleOrder = (Element) eleGetOrderInvoiceDetails
//					.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
//			strOrigSalesOrderNo = eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO);
//			/*
//			 * Returns CR 18 changes end here
//			 */
//
//			log
//					.verbose("*************** Calling method to get complete order details of Sales Order ************ "
//							+ strOrigSalesOrderNo);
//			eleSalesOrderDetails = getCompleteSalesOrderDetails(
//					strOrigSalesOrderNo, strDocType, env);
//			log
//					.verbose("*************** Complete Order Details::eleSalesOrderDetails:: ************ "
//							+ XMLUtil.getElementXMLString(eleSalesOrderDetails));
//			elePersonInfoBillTo = (Element) XPathUtil.getNode(
//					eleSalesOrderDetails, "PersonInfoBillTo");
//
//			eleReturnOrders = (Element) eleSalesOrderDetails
//					.getElementsByTagName(AcademyConstants.ELE_RETURN_ORDERS)
//					.item(0);
//			Commented as part of OMNI-81582 and moved code before CLS hashmap 
			// Begin : OMNI-42367
			
			/*
			 * Code changes to ignore CLS updates if Return order is already received
			 * on receipt of carrier updates. 
			 */
			
			Document docReturnCarrierUpdate = returnCarrierUpdates(env, AcademyConstants.STR_RET_CARRIER_UPDATES,
					AcademyConstants.PRIMARY_ENTERPRISE);
			
			Node nReturnCarrierUpdatesCC = XPathUtil.getNode(docReturnCarrierUpdate,
					AcademyConstants.ELE_COMMON_CODE_LIST);

			String strReturnOrderUpdateFlag = XPathUtil.getString(nReturnCarrierUpdatesCC,
					AcademyConstants.XPATH_RETURN_FLAG);

			String strEligibleReturnOrderStatuses = XPathUtil.getString(nReturnCarrierUpdatesCC,
					AcademyConstants.XPATH_RETURN_ORDER_STATUS);
			

			if (AcademyConstants.STR_YES.equalsIgnoreCase(strReturnOrderUpdateFlag)) {
				
				if (!StringUtil.isEmpty(strTrackingNo)) {
					
					NodeList corrRetOrderList = XPathUtil.getNodeList(eleReturnOrders,
							"ReturnOrder[Extn/@ExtnTrackingNo='" + strTrackingNo + "']");
					
					if (corrRetOrderList.getLength() > 0) {
						
						Element eleReturnOrder = (Element) corrRetOrderList.item(0);
						String strStatus = eleReturnOrder.getAttribute(AcademyConstants.STR_MAX_ORDER_STATUS);
						//OMNI-50871 -Start
						//read the OrderheaderKey

                        String strReturnOHK=eleReturnOrder.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);


						
						if (!strEligibleReturnOrderStatuses.contains(strStatus)) {
							//before we return stamp the details on when was the CLS returned
							String strItemId = null;
							String strQty=null;
                            // call the new method to store data in custom table
							for (int i = 0; i < iNoOfOrderLines; i++) {
								eleCurrOrderLine = (Element) nListOrderLines.item(i);

								 strItemId = eleCurrOrderLine
										.getAttribute(AcademyConstants.ATTR_ITEM_ID);
								 strQty = eleCurrOrderLine
										.getAttribute(AcademyConstants.ATTR_RETURN_QTY);
								 AcademyReturnOrderUtil.CaptureCLSProcessedTimeStamp(env,strReturnOHK,strTrackingNo,strItemId,strQty);
									}
							//OMNI-50871 End
							return inputDoc;
						}
					}

				}
			}
			// End : OMNI-42367
			
			nListOfRetOrders = eleReturnOrders
					.getElementsByTagName(AcademyConstants.ELE_RETURN_ORDER);
			//markReturnLinesWithZero(nListOfRetOrders);
			AcademyReturnOrderUtil.markReturnLinesWithZero(nListOfRetOrders);
			// Added as part of CR -18
			docChangeReturnOrderInputList = XMLUtil
					.createDocument("ChangeReturnList");
			// docReturnLinesReceiveDocList=XMLUtil.createDocument("ReceiveLinesList");
			// Looping through all lines in input
			NodeList corrRetOrderList = XPathUtil
					.getNodeList(eleReturnOrders,
							"ReturnOrder[Extn/@ExtnTrackingNo='"
									+ strTrackingNo + "']");

			if (returnNodeList.contains(strShipNodeInInput)) {
				for (int i = 0; i < iNoOfOrderLines; i++) {
					eleCurrOrderLine = (Element) nListOrderLines.item(i);

					String strItemId = eleCurrOrderLine
							.getAttribute(AcademyConstants.ATTR_ITEM_ID);
					String strQty = eleCurrOrderLine
							.getAttribute(AcademyConstants.ATTR_RETURN_QTY);
					eleCurrOrderLine.setAttribute("ExtraQtyToReceive", strQty);
					if (isAcademyTrcNo) {
						Element matchingRetOrder = getMatchingReturnOrder(
								eleReturnOrders, strTrackingNo);
						if (!YFCObject.isVoid(matchingRetOrder)) {
							//allowing returns if Order in shipped status as well - env.setTxnObject("existedReturnCancelled", AcademyConstants.STR_YES);
							log.verbose("Found the matching return order; in the first for loop and calling findMatchingReturnLinesInCurrentOrder()");
							findMatchingReturnLinesInCurrentOrder(strItemId,
									strQty, matchingRetOrder, eleCurrOrderLine,
									eleReturnOrders, strTrackingNo, env);
						}
					}

				}
				
				// - For White GLove Items - will get  Pro Number as a TrackingNo from external system Returns Receiving message 
				//to identify same existing Returns to do a receipt process
				for (int i = 0; i < iNoOfOrderLines; i++) {
					eleCurrOrderLine = (Element) nListOrderLines.item(i);

					String strItemId = eleCurrOrderLine
							.getAttribute(AcademyConstants.ATTR_ITEM_ID);
					String strQty = eleCurrOrderLine
							.getAttribute(AcademyConstants.ATTR_RETURN_QTY);
					//eleCurrOrderLine.setAttribute("ExtraQtyToReceive", strQty);
					if (Double.parseDouble(eleCurrOrderLine
							.getAttribute("ExtraQtyToReceive")) > 0)
					{
					if (isAcademyTrcNo) {
						Element matchingRetOrder = getMatchingReturnOrder(
								eleReturnOrders, strTrackingNo);
						if (!YFCObject.isVoid(matchingRetOrder)) {
							//allowing returns if Order in shipped status as well
							//env.setTxnObject("existedReturnCancelled", AcademyConstants.STR_YES);
							log.verbose("Found the matching return order; in the second for loop and calling findMatchingReturnLinesInCurrentOrder()");
							findMatchingReturnLinesInCurrentOrder(strItemId,
									strQty, matchingRetOrder, eleCurrOrderLine,
									eleReturnOrders, strTrackingNo, env);
						}
					}
					}

				}
				//this.checkProNumReturnOrder=false;
				new AcademyReturnOrderUtil().checkProNumReturnOrder=false;
				//End of Pro Number

				for (int i = 0; i < iNoOfOrderLines; i++) {
					eleCurrOrderLine = (Element) nListOrderLines.item(i);
					String strItemId = eleCurrOrderLine
							.getAttribute(AcademyConstants.ATTR_ITEM_ID);
					int len = nListOfRetOrders.getLength();
					for (int j = 0; j < len; j++) {
						Element matchingRetOrder = (Element) nListOfRetOrders
								.item(j);
						if (Double.parseDouble(eleCurrOrderLine
								.getAttribute("ExtraQtyToReceive")) > 0) {
							//allowing returns if Order in shipped status as well
							//env.setTxnObject("existedReturnCancelled", AcademyConstants.STR_YES);
							log.verbose("Found the matching return order; in the third for loop and now calling method findMatchingReturnLinesInCurrentOrder()");
							findMatchingReturnLinesInCurrentOrder(strItemId,
									eleCurrOrderLine
											.getAttribute("ExtraQtyToReceive"),
									matchingRetOrder, eleCurrOrderLine,
									eleReturnOrders, strTrackingNo, env);
						} else {
							break;
						}
					}

				}

				for (int i = 0; i < iNoOfOrderLines; i++) {
					eleCurrOrderLine = (Element) nListOrderLines.item(i);
					String strItemId = eleCurrOrderLine
							.getAttribute(AcademyConstants.ATTR_ITEM_ID);

					if (Double.parseDouble(eleCurrOrderLine
							.getAttribute("ExtraQtyToReceive")) > 0) {
						log.verbose("For direct CLS where we don't have return order present,calling findMatchingOrderLineInSalesOrder()");	
						// START OMNI-64480  
						strShipNodeInInput = AcademyCommonCode.getCodeValue(env, AcademyConstants.STR_CLS_RETURN_NODE_VALUE,
								AcademyConstants.STR_ACTIVE, AcademyConstants.CONST_DEFAULT);
						// END OMNI-64480
						markLinesInSalesOrder(eleSalesOrderDetails,env);
						findMatchingOrderLineInSalesOrder(strItemId,
								eleCurrOrderLine
										.getAttribute("ExtraQtyToReceive"),
								eleSalesOrderDetails, eleCurrOrderLine,env);
					}

				}
			}

			// For Store -returns logic Implementation ------ if ShipNode is not 703 then we have treated as Store returns message.(701 is the store returns node as of now configured)
			else {
				for (int i = 0; i < iNoOfOrderLines; i++) {
					eleCurrOrderLine = (Element) nListOrderLines.item(i);
					String strItemId = eleCurrOrderLine
							.getAttribute(AcademyConstants.ATTR_ITEM_ID);

					// including ...this logic to store qty in ExtraQtyToReceive
					String strQty = eleCurrOrderLine
							.getAttribute(AcademyConstants.ATTR_RETURN_QTY);
					eleCurrOrderLine.setAttribute("ExtraQtyToReceive", strQty);

					int len = nListOfRetOrders.getLength();
					for (int j = 0; j < len; j++) {
						Element matchingRetOrder = (Element) nListOfRetOrders
								.item(j);
						if (Double.parseDouble(eleCurrOrderLine
								.getAttribute("ExtraQtyToReceive")) > 0) {
							//allowing returns if Order in shipped status as well
							//env.setTxnObject("existedReturnCancelled", AcademyConstants.STR_YES);
						log.verbose("For store returns if return order is present,calling findMatchingReturnLinesInCurrentOrder()");				
							findMatchingReturnLinesInCurrentOrder(strItemId,
									eleCurrOrderLine
											.getAttribute("ExtraQtyToReceive"),
									matchingRetOrder, eleCurrOrderLine,
									eleReturnOrders, strTrackingNo, env);
						} else {
							break;
						}
					}

				}

				for (int i = 0; i < iNoOfOrderLines; i++) {
					eleCurrOrderLine = (Element) nListOrderLines.item(i);
					String strItemId = eleCurrOrderLine
							.getAttribute(AcademyConstants.ATTR_ITEM_ID);
					// if(Integer.parseInt(eleCurrOrderLine.getAttribute("ExtraQtyToReceive"))>0){
					// including - store returns
					if (Double.parseDouble(eleCurrOrderLine
							.getAttribute("ExtraQtyToReceive")) > 0) {
						// As part of code clean up, commented the redundant API call 'getCompleteOrderDetails API'  
						/*eleSalesOrderDetails = getCompleteSalesOrderDetails(
								strOrigSalesOrderNo, strDocType, env);*/
						markLinesInSalesOrder(eleSalesOrderDetails,env);
					log.verbose("For store returns if return order is not present,calling findMatchingOrderLineInSalesOrder()");
						findMatchingOrderLineInSalesOrder(strItemId,
								eleCurrOrderLine
										.getAttribute("ExtraQtyToReceive"),
								eleSalesOrderDetails, eleCurrOrderLine,env);
					}

				}

			}

			if (bChangeOrder) {
				log
						.verbose("*************** Calling 'changeReturnOrder' to change return order************ ");
				
				changeReturnOrder(docChangeReturnOrderInputList, env);
			}

			if (bCreateOrder) {
				

				log
						.verbose("*************** Payment present in Input:************");
				log
						.verbose("*************** Calling appendPaymentInformationFromInput to append payment information from input************");

				// Fix for bug -8757
				if (!YFCObject.isVoid(elePaymentMethod)) {
					appendPaymentInformationFromInput(docCreateOrderInput,
							elePaymentMethod);
				}

				log
						.verbose("*************** Calling createReturnOrder with input:************"
								+ XMLUtil.getXMLString(docCreateOrderInput));
								docCreateOrderOutput = createReturnOrder(docCreateOrderInput,
						env);
				if (!YFCObject.isVoid(docCreateOrderOutput)) {
					log
							.verbose("*************** docCreateOrderOutput:************"
									+ XMLUtil
											.getXMLString(docCreateOrderOutput));
				}
				docReturnLinesReceiveDoc = processReturnLinesForReceiving(
						docCreateOrderOutput, docReturnLinesReceiveDoc,env);
			}

			if (!YFCObject.isVoid(docReturnLinesReceiveDoc)) {

				log
						.verbose("*************** docReturnLinesReceiveDoc for return receipt is************ "
								+ XMLUtil
										.getXMLString(docReturnLinesReceiveDoc));

			} else {
				log
						.verbose("*************** docReturnLinesReceiveDoc for return receipt is NULL************");

			}
			log
					.verbose("*************** Calling receiveReturnOrder ro Start, Receive and Close Receipt for Return Order************");
			
			receiveReturnOrder(docReturnLinesReceiveDoc, env);
			
		
			
		
			if (returnNodeList.contains(strShipNodeInInput))
			{
			env.setTxnObject("CLSRemoveShippingCharges", "");
			
			log.verbose("Now the value of CLSRemoveShippingCharges is cleared in the Env");
						}
			//
			//env.setTxnObject("existedReturnCancelled", AcademyConstants.STR_NO);
			log.endTimer(" End of processReturnMessage Api");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		return docOutput;
	}
	
	
	 /**
	 * Method created for STL 1087 - Throw exception on invalid item for an Invoice.
	 * 
	 * Validate the ItemID received from CLS returns(In-Store or Mail-In) with the ItemID in Invoice thats created in Sterling.
	 * If ItemID is not present then throw an exception.  
	 * @param eleGetOrderInvoiceDetails - Document from Sterling.
	 * @param inputDoc - CLS Returns document sent from external system.
	 */
	 /* //This is the old method used for validation
	 private void validateItemInInvoice(Element eleGetOrderInvoiceDetails,Document inputDoc) throws YFSException {
	 		
	 		log.verbose("AcademyProcessReturnMessageAPI_validateItemInInvoice():");
	 		ArrayList<String> itemIDList = new ArrayList<String>();
	 		
	 		// Form Item ID list with the items in the invoice present in sterling system.
	 		YFCDocument getOrderInvoiceListDoc = YFCDocument.getDocumentFor(eleGetOrderInvoiceDetails.getOwnerDocument());
	 		YFCElement orderInvoiceListElem = getOrderInvoiceListDoc.getDocumentElement();
	 		YFCElement orderInvoiceElem = orderInvoiceListElem.getChildElement(AcademyConstants.ELE_ORDER_INVOICE);
	 		if (orderInvoiceElem != null) {
	 			YFCElement lineDetailsElem = orderInvoiceElem.getChildElement(AcademyConstants.ELEM_LINE_DETAILS);
	 			if (lineDetailsElem != null) {
	 				YFCIterable<YFCElement> lineDetailItr = lineDetailsElem.getChildren(AcademyConstants.ELEM_LINE_DETAIL);
	 				while (lineDetailItr.hasNext()) {
	 					YFCElement lineDetailElem = lineDetailItr.next();
	 					String itemID = lineDetailElem.getAttribute(AcademyConstants.ITEM_ID);
	 					if (!YFCObject.isVoid(itemID)) {
	 						itemIDList.add(itemID);
	 					}
	 				}
	 			}
	 		} else {
	 			log.verbose("Order Invoice details not available in Sterling System.");
	 			throw new YFSException("Order Invoice Doesn't exist");
	 		}
	 				
	 		if (itemIDList != null) {
	 			log.verbose("Item List Size: " + itemIDList.size());
	 			// Check if the CLS returns input has the ItemID.
	 			NodeList orderLineElemList = inputDoc.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINE);
	 			if (orderLineElemList != null) {
	 				int noOfOrderLine = orderLineElemList.getLength();
	 				for (int lineItr=0;lineItr<noOfOrderLine;lineItr++) {
	 					Element orderLineElem = (Element)orderLineElemList.item(lineItr);
	 					String itemID = orderLineElem.getAttribute(AcademyConstants.ITEM_ID);
	 					if (!YFCObject.isVoid(itemID)) {
	 						if (!itemIDList.contains(itemID)) {
	 							// Throw Exception as there is a Item mismatch.
	 							String errorDesc = "Item("+itemID+") received in Invoice doesn't match with Invoice created in Sterling";
	 							throw new YFSException(errorDesc);
	 						}
	 					}
	 				}
	 			}
	 		}		
	 	}
	 */
	
	
	
//start of STL-385
public Document createReturnOrdMsg(YFSEnvironment env, Document docApiInput)
throws Exception {
    // TODO Auto-generated method stub

     log.verbose("*********input ************ "
     +XMLUtil.getXMLString(docApiInput));

    NodeList docOrderLineList = docApiInput
                .getElementsByTagName("OrderLine");

    int nodeListLength = docOrderLineList.getLength();

    HashMap<String, String> hmItemIDRetQty = new HashMap<String, String>();

    for (int i = 0; i < nodeListLength; i++) {
          Element docOrderLine = (Element) docOrderLineList.item(i);

          String itemId = docOrderLine.getAttribute("ItemID");
          String qty = docOrderLine.getAttribute("ReturnQty");

          if (!hmItemIDRetQty.isEmpty() && hmItemIDRetQty.containsKey(itemId)) {
                double retQtyValue = Double.parseDouble(hmItemIDRetQty
                            .get(itemId));
                retQtyValue = retQtyValue + Double.parseDouble(qty);
                hmItemIDRetQty.put(itemId, String.valueOf(retQtyValue));

                 log
                 .verbose("*********Inside IF retQtyValue ************ "
                 +retQtyValue);
                 log
                 .verbose("*********Inside IF qty ************ " +qty);
                Element parentElement = (Element) docOrderLine.getParentNode();
                parentElement.removeChild(docOrderLine);
                Element originalDocOrderLine = (Element) XMLUtil.getNode(
                            parentElement, "OrderLine[@ItemID='" + itemId + "']");
                originalDocOrderLine.setAttribute("ReturnQty", hmItemIDRetQty
                            .get(itemId));
                i = i - 1;
                nodeListLength = nodeListLength - 1;
          } else {
                hmItemIDRetQty.put(itemId, qty);
          }

        
    }

     log
     .verbose("*********Output ************ "
     +XMLUtil.getXMLString(docApiInput));

    return docApiInput;
}

// End  of STL-385

/**
 *  checking & setting for available Qty for Returns. 
 * @param eleSalesOrderDetails
 * @param env
 */
	private void markLinesInSalesOrder(Element eleSalesOrderDetails,YFSEnvironment env) {
		try {
			log.beginTimer(" Begin of markLinesInSalesOrder()");
			NodeList lstOfOrderLines = XPathUtil.getNodeList(
					eleSalesOrderDetails, AcademyConstants.XPATH_ORDERLINE);
			int len = lstOfOrderLines.getLength();
			String existedReturnCancellQty="",shippedQty="0",deliveredQty="0";
			for (int i = 0; i < len; i++) {
				Element eleCurrSalesLine = (Element) lstOfOrderLines.item(i);
			
				Element eleLineStatus = (Element) XPathUtil.getNode(
						eleCurrSalesLine,
						"OrderStatuses/OrderStatus[@Status='3700.7777']");
				
				
				//Commenting this portion - as we are allowing Shipped Order also for Return Creation
				/*if((String)env.getTxnObject("existedReturnCancelled")!=null)
					existedReturnCancellQty=(String)env.getTxnObject("existedReturnCancelled");
				
				//if(existedReturnCancellQty.equalsIgnoreCase(AcademyConstants.STR_YES))
			*/		
				//adding the Shipped Qty as well for Returns Creation.
				Element eleLineShipStatus = (Element) XPathUtil.getNode(
						eleCurrSalesLine,
						"OrderStatuses/OrderStatus[@Status='3700']");
				if (!YFCObject.isVoid(eleLineShipStatus)) {
					
					 shippedQty=eleLineShipStatus.getAttribute("StatusQty");
				}
				
				if (!YFCObject.isVoid(eleLineStatus)) {
					deliveredQty=eleLineStatus.getAttribute("StatusQty");
				} 
				eleCurrSalesLine.setAttribute("QtyAvailableForReceipt",
						Double.toString((Double.parseDouble(shippedQty)+ Double.parseDouble(deliveredQty))));
				//commenting this portion as covered in above logic as well - shipped Qty + DeliveredQty.
				/*else {

					if(existedReturnCancellQty.equalsIgnoreCase(AcademyConstants.STR_YES))
					       eleCurrSalesLine
							.setAttribute("QtyAvailableForReceipt", shippedQty);
					else   eleCurrSalesLine
					.setAttribute("QtyAvailableForReceipt", "0");
				}*/
			
			}
			log.endTimer(" End of markLinesInSalesOrder()");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new YFSException(e.getMessage());
		}

	}
/**
 * this method is used to get the Available Qty from Sales Order Lines - for Returns if Return order not existed or return Qty exceeds from existed return.
 * @param strItemId
 * @param qtyToReceive
 * @param eleSalesOrderDetails
 * @param eleCurrOrderLine
 * @param env
 */
	private void findMatchingOrderLineInSalesOrder(String strItemId,
			String qtyToReceive, Element eleSalesOrderDetails,
			Element eleCurrOrderLine,YFSEnvironment env) {
		try {
			log.beginTimer(" Begin of findMatchingOrderLineInSalesOrder()");
			NodeList lstOfOrderLines = XPathUtil.getNodeList(
					eleSalesOrderDetails,
					"OrderLines/OrderLine[./Item/@ItemID='" + strItemId + "']");
			int len = lstOfOrderLines.getLength();
			
			if (returnNodeList.contains(strShipNodeInInput))
			{
			env.setTxnObject("CLSRemoveShippingCharges", AcademyConstants.STR_YES);
			}
			if (len > 0) {
				
				for (int i = 0; i < len; i++) {
					if (Double.parseDouble(eleCurrOrderLine
							.getAttribute("ExtraQtyToReceive")) > 0) {
						
						Element currLine = (Element) lstOfOrderLines.item(i);
						String availQtyForReceipt = currLine
								.getAttribute("QtyAvailableForReceipt");
						qtyToReceive = eleCurrOrderLine
								.getAttribute("ExtraQtyToReceive");
						if (Double.parseDouble(availQtyForReceipt) == 0) {
							
							continue;
						} else {
						
							if (Double.parseDouble(availQtyForReceipt) >= Double
									.parseDouble(qtyToReceive)) {
								double availqty = Double
										.parseDouble(availQtyForReceipt)
										- Double.parseDouble(qtyToReceive);
								currLine.setAttribute("QtyAvailableForReceipt",
										Double.toString(availqty));
								eleCurrOrderLine.setAttribute(
										"ExtraQtyToReceive", "0");
								/*
								 * updateDocuments(currLine,
								 * docChangeReturnOrderInput,
								 * docReturnLinesReceiveDoc,
								 * currLine.getAttribute("OrderHeaderKey"),
								 * currLine.getAttribute("OrderLineKey"));
								 */

								docCreateOrderInput = prepareInputForCreateOrder(
										eleSalesOrderDetails,
										docCreateOrderInput, eleCurrOrderLine,
										strItemId, qtyToReceive, currLine);
								bCreateOrder = true;
								// XMLUtil.importElement(doc, ele2beImported)

							} else {
								double availqty = 0;
								currLine.setAttribute("QtyAvailableForReceipt",
										Double.toString(availqty));
								double extraQty = Double
										.parseDouble(qtyToReceive)
										- Double
												.parseDouble(availQtyForReceipt);
								eleCurrOrderLine.setAttribute(
										"ExtraQtyToReceive", Double
												.toString(extraQty));
								/*
								 * updateDocuments(currLine,
								 * docChangeReturnOrderInput,
								 * docReturnLinesReceiveDoc,
								 * currLine.getAttribute("OrderHeaderKey"),
								 * currLine.getAttribute("OrderLineKey"));
								 */
								docCreateOrderInput = prepareInputForCreateOrder(
										eleSalesOrderDetails,
										docCreateOrderInput, eleCurrOrderLine,
										strItemId, availQtyForReceipt, currLine);
								
								bCreateOrder = true;
							}
						}
					}
				}
			}
			log.endTimer(" End of findMatchingOrderLineInSalesOrder()");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new YFSException(e.getMessage());
		}

	}
	
/**
 *  This method is used for finding the existed Return Order for receipt process based on Tracking No or Pro No.
 * @param eleReturnOrders
 * @param strTrackingNo
 * @return
 */
	private Element getMatchingReturnOrder(Element eleReturnOrders,
			String strTrackingNo) {
		Element eleMatchingRetOrder = null;
		try {
			NodeList nList;
			//if(this.checkProNumReturnOrder)
			if(new AcademyReturnOrderUtil().checkProNumReturnOrder)
			{
				
			 nList = XPathUtil
			.getNodeList(eleReturnOrders,
					"ReturnOrder[Extn/@ExtnProNo='"
							+ strTrackingNo + "']");
			/* nList = XPathUtil
				.getNodeList(eleReturnOrders,
						"ReturnOrder[Extn/@ExtnTrackingNo='"
								+ strTrackingNo + "']");
			*/}
			else
			nList=XPathUtil
			.getNodeList(eleReturnOrders,
					"ReturnOrder[Extn/@ExtnTrackingNo='"
							+ strTrackingNo + "']");
			
			if (nList != null && !YFCObject.isVoid(nList)) {
				eleMatchingRetOrder = (Element) nList.item(0);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return eleMatchingRetOrder;
	}

	// This method is used for updating the Return Lines .
	private void updateDocuments(Element eleMatchingReturnOrderLine,
			Document docChangeReturnOrderInput,
			Document docReturnLinesReceiveDoc, String strReturnHeaderKey,
			String strReturnOrderLineKey, String returnReason, String strQty,
			Element eleReturnOrders, String strTrackingNo,String clsReturnReasonCode) {
		log.beginTimer(" Begin of updateDocuments()");
		log
				.verbose("*************** Matching Return Order Line Exists************ ");
		
		bChangeOrder = true;
		log.verbose("The value of bChangeOrder is "+bChangeOrder);
		log
				.verbose("*************** Calling method to prepare input for Change Return Order Call************ ");

		docChangeReturnOrderInput = prepareInputForChangeOrder(
				docChangeReturnOrderInput, strReturnHeaderKey,
				strReturnOrderLineKey, returnReason, eleReturnOrders,
				strTrackingNo, strQty,clsReturnReasonCode);
	

		if (!YFCObject.isVoid(docChangeReturnOrderInput))
			log
					.verbose("*************** Input to change Return Order************ "
							+ XMLUtil.getXMLString(docChangeReturnOrderInput));
		this.docReturnLinesReceiveDoc = updateReturnLinesDocument(
				this.docReturnLinesReceiveDoc, eleMatchingReturnOrderLine,
				strReturnHeaderKey, strQty);
		log.endTimer(" End of updateDocuments()");
		
	}

	/**
	 *  This below is implemented to get the matching/existing Return Lines and setting the Qty on corresponding sales Order Lines - for receipt process.
	 * @param strItemId
	 * @param strQty
	 * @param matchingRetOrder
	 * @param eleCurrOrderLine
	 * @param eleReturnOrders
	 * @param strTrackingNo
	 * @param env
	 */
	private void findMatchingReturnLinesInCurrentOrder(String strItemId,
			String strQty, Element matchingRetOrder, Element eleCurrOrderLine,
			Element eleReturnOrders, String strTrackingNo, YFSEnvironment env) {

		try {
			log.beginTimer(" Begin of findMatchingReturnLinesInCurrentOrder()");
			NodeList nReturnLineList = XPathUtil.getNodeList(matchingRetOrder,
					"OrderLines/OrderLine[./Item/@ItemID='" + strItemId + "']");
			if (!YFCObject.isVoid(nReturnLineList)) {
				int length = nReturnLineList.getLength();
				
				String clsReturnReason = eleCurrOrderLine
						.getAttribute(AcademyConstants.ATTR_RETURN_REASON);
				String existedReturnOrdNo=null;
				Document UpdateRedLiningDoc = XMLUtil
				.createDocument("UpdateCustomer");
				//including code changes to handle ReturnReasonCode
				/*clsReturnReasonCode = eleCurrOrderLine
				.getAttribute("ReturnReasonCode");*/
				
//				changing this part as - with new requirements - we will get only ReturnReason - 0-10 values .
				 clsReturnReasonCode = eleCurrOrderLine
				.getAttribute(AcademyConstants.ATTR_RETURN_REASON);
				 existedReturnReasonCode=null;	
				if (length > 0) {

					for (int i = 0; i < length; i++) {
						if (Double.parseDouble(eleCurrOrderLine
								.getAttribute("ExtraQtyToReceive")) > 0) {
							Element currLine = (Element) nReturnLineList
									.item(i);
							String availQtyForReceipt = currLine
									.getAttribute("QtyAvailableForReceipt");
							strQty = eleCurrOrderLine
									.getAttribute("ExtraQtyToReceive");
//							chaning this part as - with new requirements - we will get only ReturnReason - 0-10 values .
							 existedReturnReasonCode=currLine
							.getAttribute(AcademyConstants.ATTR_RETURN_REASON);
							 Element eleExistedReturnRefundableReasonCode = (Element) XPathUtil.getNode(
										this.docRefundableReasonCodeValues.getDocumentElement(),
										"CommonCode[@CodeValue='" + existedReturnReasonCode + "']");
							 
							 //Changes - Return Reason Code Handling instead of Return reason Text .
							 Element eleCLSRefundableReasonCodeValue = (Element) XPathUtil.getNode(
										this.docRefundableReasonCodeValues.getDocumentElement(),
										"CommonCode[@CodeValue='" + clsReturnReasonCode + "']");
							 
								
							 
							 if (Double.parseDouble(availQtyForReceipt) == 0) {
								 
								continue;
							} else {
								
								if (Double.parseDouble(availQtyForReceipt) >= Double
										.parseDouble(strQty)) {
									
									double availqty = Double
											.parseDouble(availQtyForReceipt)
											- Double.parseDouble(strQty);
									currLine.setAttribute(
											"QtyAvailableForReceipt", Double
													.toString(availqty));
									eleCurrOrderLine.setAttribute(
											"ExtraQtyToReceive", "0");
									
									// including changes for Store-returns -
									// creating a new return Order with
									// remaining Qty
									if (!returnNodeList.contains(strShipNodeInInput)) {

										docCreateOrderInput = prepareInputForCreateOrder(
												eleSalesOrderDetails,
												docCreateOrderInput,
												eleCurrOrderLine, strItemId,
												strQty, currLine);
											((Element) (docCreateOrderInput
												.getDocumentElement()
												.getElementsByTagName(
														AcademyConstants.ATTR_DERIVED_FROM)
												.item(0)))
												.setAttribute(
														AcademyConstants.ATTR_ORDER_LINE_KEY,
														currLine
																.getAttribute(AcademyConstants.ATTR_DERIVEDFROM_ORDERLINE_KEY));

																				Document inputDoc = XMLUtil
												.createDocument(AcademyConstants.ELE_ORDER);
										Element rootElement = inputDoc
												.getDocumentElement();
										rootElement
												.setAttribute(
														AcademyConstants.ATTR_ORDER_HEADER_KEY,
														currLine
																.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
										rootElement.setAttribute(AcademyConstants.ATTR_OVERRIDE,
												AcademyConstants.STR_YES);
										Element eleOrderLines = inputDoc
												.createElement(AcademyConstants.ELEM_ORDER_LINES);
										Element eleOrderLine = inputDoc
												.createElement(AcademyConstants.ELEM_ORDER_LINE);
										eleOrderLine
												.setAttribute(
														AcademyConstants.ATTR_ORDER_LINE_KEY,
														currLine
																.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
//										Retunr Order with Partially Receipt Closed
										
										String returnStatus=matchingRetOrder.getAttribute(AcademyConstants.ATTR_STATUS);
										if(returnStatus.equalsIgnoreCase("Partially Receipt Closed"))
										availqty=Double.parseDouble(currLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY))-Double.parseDouble(strQty);
									
										eleOrderLine
												.setAttribute(
														AcademyConstants.ATTR_ORDERED_QTY,
														Double
																.toString(availqty));
										eleOrderLine.setAttribute(
												AcademyConstants.ATTR_UOM,
												AcademyConstants.UNIT_OF_MEASURE);
										eleOrderLines
												.appendChild((Node) eleOrderLine);
										rootElement
												.appendChild((Node) eleOrderLines);
										env.setTxnObject("RepricingFlag", AcademyConstants.STR_NO);
										AcademyUtil
												.invokeAPI(
														env,
														AcademyConstants.API_CHANGE_ORDER,
														inputDoc);
										bCreateOrder = true;
										continue;
									}

									// including change for Store-returns - to
									// filter CLS - returns node
									if (returnNodeList.contains(strShipNodeInInput)) {
										// START OMNI-64480
										this.strShipNodeInInput = currLine.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
										// end OMNI-64480
										updateDocuments(
												currLine,
												docChangeReturnOrderInput,
												docReturnLinesReceiveDoc,
												currLine
														.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY),
												currLine
														.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY),
														clsReturnReason, strQty,
												eleReturnOrders, strTrackingNo,clsReturnReasonCode);
										bChangeOrder = true;
										//Indicating  flag for Tracking RedLining with Customers
										if (!YFCObject.isVoid(eleExistedReturnRefundableReasonCode)&& (YFCObject.isVoid(eleCLSRefundableReasonCodeValue)))
										{   //eleExistedReturnRefundableReasonCodeValue & eleCLSRefundableReasonCodeValue
											//eleExistedReturnRefundableReasonCode & eleCLSRefundableReasonCode
											
											Document returnOrdDoc = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
											returnOrdDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,currLine
													.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
											env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, "<OrderList> <Order OrderNo=''/> </OrderList>" );
											Document returnOrderListDocument = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, returnOrdDoc);
											env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
											Element returnOrderElement = returnOrderListDocument.getDocumentElement();
											log.verbose("return order list Element - " + XMLUtil.getElementXMLString(returnOrderElement));
											Element orderElement = (Element) XPathUtil.getNode(returnOrderElement, "/OrderList/Order");
											if(!YFCObject.isVoid(orderElement))
											{
												existedReturnOrdNo = orderElement.getAttribute(AcademyConstants.ATTR_ORDER_NO);
											}
										
											UpdateRedLiningDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXISTED_RETURN_ORDNO,existedReturnOrdNo);
											UpdateRedLiningDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXISTED_RETURN_REASON,existedReturnReasonCode);
											UpdateRedLiningDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CLS_RETURN_REASON,clsReturnReason);
											UpdateRedLiningDoc.getDocumentElement().setAttribute("EmailId",eleSalesOrderDetails.getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID));
											//Including Changes to track RED Lining with CUSTOMERS
											AcademyUtil.invokeService(env,AcademyConstants.ACADEMY_UPDATE_RED_LINING_CUSTOMER_SERVICE, UpdateRedLiningDoc);
										}
										
										//Indicating  flag for Tracking RedLining with Customers
										
										continue;
									}
								} else {
									if (returnNodeList.contains(strShipNodeInInput)) {
										
										double availqty = 0;
										currLine.setAttribute(
												"QtyAvailableForReceipt",
												Double.toString(availqty));
										double extraQty = Double
												.parseDouble(strQty)
												- Double
														.parseDouble(availQtyForReceipt);
										eleCurrOrderLine.setAttribute(
												"ExtraQtyToReceive", Double
														.toString(extraQty));

										bChangeOrder = true;
									
										//Indicating  flag for Tracking RedLining with Customers
									if (!YFCObject.isVoid(eleExistedReturnRefundableReasonCode)&& (YFCObject.isVoid(eleCLSRefundableReasonCodeValue)))
										{  
//										if (!YFCObject.isVoid(eleExistedReturnRefundableReasonCode)&& (YFCObject.isVoid(eleCLSRefundableReasonCode)))
										//eleExistedReturnRefundableReasonCodeValue & eleCLSRefundableReasonCodeValue
										//eleExistedReturnRefundableReasonCode & eleCLSRefundableReasonCode
										Document returnOrdDoc = XMLUtil
										.createDocument(AcademyConstants.ELE_ORDER);
										returnOrdDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,currLine
												.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
										env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST,
										AcademyConstants.STR_TEMPLATEFILE_GETRETURNORDER_DETAILS);
										Document returnOutDoc=AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, returnOrdDoc);
										//this.existedReturnOrderNo=returnOutDoc.getDocumentElement().getAttribute("OrderNo");
										 existedReturnOrdNo=((Element)returnOutDoc.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0)).getAttribute(AcademyConstants.ATTR_ORDER_NO);
											UpdateRedLiningDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXISTED_RETURN_ORDNO,existedReturnOrdNo);
											UpdateRedLiningDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXISTED_RETURN_REASON,existedReturnReasonCode);
											UpdateRedLiningDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CLS_RETURN_REASON,clsReturnReason);
											UpdateRedLiningDoc.getDocumentElement().setAttribute("EmailId",eleSalesOrderDetails.getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID));
//											Including Changes to track RED Lining with CUSTOMERS
												AcademyUtil.invokeService(env,AcademyConstants.ACADEMY_UPDATE_RED_LINING_CUSTOMER_SERVICE, UpdateRedLiningDoc);
											//this.bRedLineCustomer=true;
												env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
										}
											//Indicating  flag for Tracking RedLining with Customers
										
										updateDocuments(
												currLine,
												docChangeReturnOrderInput,
												docReturnLinesReceiveDoc,
												currLine
														.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY),
												currLine
														.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY),
														clsReturnReason,
												availQtyForReceipt,
												eleReturnOrders, strTrackingNo,clsReturnReasonCode);
										
										continue;
										
									}

									// including changes for Store-returns -
									// cancelling the availQtyForReceipt on
									// existed ReturnOrder and it should pick
									// from SalesOrder qty
									if (!returnNodeList.contains(strShipNodeInInput)) {
										double availqty = 0;
										currLine.setAttribute(
												"QtyAvailableForReceipt",
												Double.toString(availqty));
										
										Document inputDoc = XMLUtil
												.createDocument(AcademyConstants.ELE_ORDER);
										
										Element rootElement = inputDoc
												.getDocumentElement();
										rootElement
												.setAttribute(
														AcademyConstants.ATTR_ORDER_HEADER_KEY,
														currLine
																.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
										rootElement.setAttribute(AcademyConstants.ATTR_OVERRIDE,
												AcademyConstants.STR_YES);
										Element eleOrderLines = inputDoc
												.createElement(AcademyConstants.ELEM_ORDER_LINES);
										Element eleOrderLine = inputDoc
												.createElement(AcademyConstants.ELEM_ORDER_LINE);
										eleOrderLine
												.setAttribute(
														AcademyConstants.ATTR_ORDER_LINE_KEY,
														currLine
																.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
										eleOrderLine
												.setAttribute(
														AcademyConstants.ATTR_ORDERED_QTY,
														Double
																.toString(availqty));
										eleOrderLine.setAttribute(
												AcademyConstants.ATTR_UOM,
												AcademyConstants.UNIT_OF_MEASURE);
										eleOrderLines
												.appendChild((Node) eleOrderLine);
										rootElement
												.appendChild((Node) eleOrderLines);
										env.setTxnObject("RepricingFlag", AcademyConstants.STR_NO);
										AcademyUtil
												.invokeAPI(
														env,
														AcademyConstants.API_CHANGE_ORDER,
														inputDoc);
										
										continue;
									}
									// End of logic for Store-returns

								}
							}

						}
						break;
					}
				}
			}
			log.endTimer(" Begin of findMatchingReturnLinesInCurrentOrder()");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

	}

	
//getting the Sales Order Invoice details by using invoice NO gets from external system return receipt process Msg.
	private Element getOrderInvoiceDetails(String strInvoiceNo,
			YFSEnvironment env) {
		Element eleGetOrderInvoiceDetails = null;
		Document docGetOrderInvoiceDetailsInput = null;
		Document docGetOrderInvoiceDetailsOutput = null;

		try {

			log
					.verbose("*************** Inside getOrderInvoiceDetails method************ ");
			docGetOrderInvoiceDetailsInput = XMLUtil
					.createDocument("OrderInvoice");
			eleGetOrderInvoiceDetails = docGetOrderInvoiceDetailsInput
					.getDocumentElement();
			eleGetOrderInvoiceDetails.setAttribute(AcademyConstants.ATTR_INVOICE_NO, strInvoiceNo);
			// env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
			// AcademyConstants.STR_TEMPLATEFILE_GETCOMPLETEORDER_DETAILS);
			
			env.setApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_LIST,
					AcademyConstants.STR_TEMPLATEFILE_GETORDER_INVOICEDETAILS);
			if (!YFCObject.isVoid(docGetOrderInvoiceDetailsInput))
				log
						.verbose("*************** Calling gOrderInvoiceList API with input************ "
								+ XMLUtil
										.getXMLString(docGetOrderInvoiceDetailsInput));
			docGetOrderInvoiceDetailsOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_ORDER_INVOICE_LIST, docGetOrderInvoiceDetailsInput);
			if (!YFCObject.isVoid(docGetOrderInvoiceDetailsOutput))
				log
						.verbose("*************** Output of gOrderInvoiceList ************ "
								+ XMLUtil
										.getXMLString(docGetOrderInvoiceDetailsOutput));
			eleGetOrderInvoiceDetails = docGetOrderInvoiceDetailsOutput
					.getDocumentElement();
			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_LIST);
					log.verbose("invoice Temp="+XMLUtil.getElementXMLString(eleGetOrderInvoiceDetails));
					
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		
		return eleGetOrderInvoiceDetails;
	}

	private Document processReturnLinesForReceiving(
			Document docCreateOrderOutput, Document docReturnLinesReceiveDoc,YFSEnvironment env) {

		String strReturnHeaderKey = null;
		String strCurrReturnLineKey = null;
		String strReturnQty = null;
		NodeList nCreatedRetOrderLines = null;
		Element eleCurrentReturnLine = null;
		int iCreatedReturnLines = 0;

		try {
			log.endTimer(" Begin of processReturnLinesForReceiving()");
			log
					.verbose("*************** Inside processReturnLinesForReceiving method************ ");
			log
					.verbose("*************** Processing receipt for Return Order Header Key************ "
							+ strReturnHeaderKey);
			strReturnHeaderKey = docCreateOrderOutput.getDocumentElement()
					.getAttribute(AcademyConstants.STR_ORDR_HDR_KEY);
			
			nCreatedRetOrderLines = XPathUtil.getNodeList(docCreateOrderOutput
					.getDocumentElement(), AcademyConstants.XPATH_ORDERLINE);
			iCreatedReturnLines = nCreatedRetOrderLines.getLength();
			for (int j = 0; j < iCreatedReturnLines; j++) {
				eleCurrentReturnLine = (Element) nCreatedRetOrderLines.item(j);
				if (!YFCObject.isVoid(eleCurrentReturnLine)) {
					strCurrReturnLineKey = eleCurrentReturnLine
							.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
					strReturnQty = eleCurrentReturnLine
							.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
				}
             	docReturnLinesReceiveDoc = updateReturnLinesDocument(
						docReturnLinesReceiveDoc, eleCurrentReturnLine,
						strReturnHeaderKey, strReturnQty);
				
			}// end for loop

			log
					.verbose("*************** Exiting processReturnLinesForReceiving method************ ");
			if (!YFCObject.isVoid(docReturnLinesReceiveDoc))
				log
						.verbose("*************** Returning docReturnLinesReceiveDoc************ "
								+ XMLUtil
										.getXMLString(docReturnLinesReceiveDoc));
			log.endTimer(" End of processReturnLinesForReceiving()");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
				return docReturnLinesReceiveDoc;

	}

	//calling changeOrder for modifing the existing return ORders.
	private void changeReturnOrder(Document docChangeReturnOrderInputList,
			YFSEnvironment env) {
		log
				.verbose("*************** Inside changeReturnOrder method************ ");
		log.verbose("*************** Input to changeOrder is ::************ "
				+ XMLUtil.getXMLString(docChangeReturnOrderInputList));
		if (!YFCObject.isVoid(docChangeReturnOrderInputList)) {
			try {
				NodeList lstOfOrders = docChangeReturnOrderInputList
						.getElementsByTagName(AcademyConstants.ELE_ORDER);
				int len = lstOfOrders.getLength();
				if (len > 0) {
					for (int i = 0; i < len; i++) {
						Document inputDoc = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
						Element rootElement = inputDoc.getDocumentElement();
						Element currOrder = (Element) lstOfOrders.item(i);
						XMLUtil.copyElement(inputDoc, currOrder, rootElement);
						env.setTxnObject("RepricingFlag", AcademyConstants.STR_NO);
						AcademyUtil.invokeAPI(env,
								AcademyConstants.API_CHANGE_ORDER, inputDoc);
						
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new YFSException(e.getMessage());
			}
		}
	}
//preparing the input for calling changeOrder for return Orders & also removing the Shipping or Return charges/taxes based on Reason Code.
	private Document prepareInputForChangeOrder(
			Document docChangeReturnOrderInput, String returnOrderHeaderKey,
			String returnLineKey, String returnReason, Element eleReturnOrders,
			String strTrackingNo, String strQty,String clsReturnReasonCode) {

		Element eleOrderLines = null;
		Element eleOrderLine = null;
		
		log.verbose("Original value of the flag removeLineShipCharges when it entered the method" +removeLineShipCharges);
		log.verbose("Original value of the flag removeLineCharges when it entered the method" +removeLineCharges);
		
		
		try {
			log.beginTimer(" Begin of prepareInputForChangeOrder()");
			log
					.verbose("*************** Inside prepareInputForChangeOrder method************ ");
		
			//Handling Return Reason Code than using Return Reason Text
			Element eleRefundableReasonCodeValue = (Element) XPathUtil.getNode(
					this.docRefundableReasonCodeValues.getDocumentElement(),
					"CommonCode[@CodeValue='" + clsReturnReasonCode + "']");
			//checking the Reason Code and removeLineShipCharges=false if its a Academy Fault. //eleRefundableReasonCode
			if (!YFCObject.isVoid(eleRefundableReasonCodeValue)) {
					log.verbose("is Academy at fault???");
				removeLineShipCharges = false;
				
			}
			// checking and removeLineCharges=true if trackingNo is not belongs
			// to Academy and if return received from Store.

			//if (isAcademyTrackingNo(strTrackingNo)) 
			if(AcademyReturnOrderUtil.isAcademyTrackingNo(strTrackingNo))
			{
				if (!YFCObject.isVoid(eleRefundableReasonCodeValue)) {
					removeLineCharges = true;
									}
			} else {
				removeLineCharges = true;
			}
			/*
			 * If current Order is not present in the List document then add
			 * this Order and the line. If current Order is present , then check
			 * for current line. If line is not present add this line else do
			 * nothing
			 */
	
			log.verbose("After entering thf if blocks, the value of  removeLineShipCharges " +removeLineShipCharges);
			log.verbose("After entering thf if blocks, the value of  removeLineCharges "  +removeLineCharges);
			
			Element currOrderElem = (Element) XPathUtil.getNode(
					docChangeReturnOrderInputList,
					"ChangeReturnList/Order[@OrderHeaderKey='"
							+ returnOrderHeaderKey + "']");
			
			if (YFCObject.isVoid(currOrderElem)) {
				docChangeReturnOrderInput = XMLUtil
						.createDocument(AcademyConstants.ELE_ORDER);
				
//adding References - to store sales order Invoice No for Return Invoice T-log message
				
				Element eleReferences = docChangeReturnOrderInput
				.createElement("References");
				Element eleRef =docChangeReturnOrderInput.createElement("Reference");
			eleRef.setAttribute(AcademyConstants.ATTR_NAME,"SalesOrderInvoiceNo");
				eleRef.setAttribute(AcademyConstants.ATTR_VALUE,salesInvoiceNo);
				eleReferences.appendChild((Node)eleRef);
				docChangeReturnOrderInput.getDocumentElement().appendChild((Node) eleReferences);
			    //adding References - to store sales order Invoice No for Return Invoice T-log message

				
				// including this change for store returns --adding override as
				// Y
				docChangeReturnOrderInput.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);

				docChangeReturnOrderInput.getDocumentElement()
						.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY,
								returnOrderHeaderKey);
				eleOrderLines = docChangeReturnOrderInput
						.createElement(AcademyConstants.ELEM_ORDER_LINES);
				eleOrderLine = docChangeReturnOrderInput
						.createElement(AcademyConstants.ELEM_ORDER_LINE);
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,
						returnLineKey);

				// including this change for Store returns only changing the
				// returnQty to store returns Qty.
				if (!(returnNodeList.contains(strShipNodeInInput)))
					eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, strQty);

				eleOrderLine.setAttribute(AcademyConstants.SHIP_NODE,
						this.strShipNodeInInput);
//				1a				updating the ReturnReason at line level with updated one.
				eleOrderLine.setAttribute(AcademyConstants.ATTR_RETURN_REASON,
						clsReturnReasonCode);
				if (true) {
					Element lineCharges = docChangeReturnOrderInput
							.createElement(AcademyConstants.ELE_LINE_CHARGES);
					Element lineChargesinParentOrder = AcademyReturnOrderUtil.getLineCharges(
							returnLineKey, eleReturnOrders);
					log.verbose("Parent Line charges :: "+XMLUtil.getElementXMLString(lineChargesinParentOrder));
					XMLUtil.copyElement(docChangeReturnOrderInput,
							lineChargesinParentOrder, lineCharges);
					log.verbose("Dist Line Charges : "+XMLUtil.getElementXMLString(lineCharges));
					lineCharges = removeRetShippingCharge(lineCharges);
					log.verbose("Final Line Charges : "+XMLUtil.getElementXMLString(lineCharges));
					lineCharges.setAttribute(AcademyConstants.ATTR_RESET, AcademyConstants.STR_YES);
					XMLUtil.importElement(eleOrderLine, lineCharges);
					
					//remove corresponding ReturnShippingTaxes
					Element lineTaxes = docChangeReturnOrderInput
					.createElement(AcademyConstants.ELE_LINE_TAXES);
			Element lineTaxesinParentOrder = AcademyReturnOrderUtil.getLineTaxes(
					returnLineKey, eleReturnOrders);
			log.verbose("Parent Taxes : "+XMLUtil.getElementXMLString(lineTaxesinParentOrder));
			XMLUtil.copyElement(docChangeReturnOrderInput,
					lineTaxesinParentOrder, lineTaxes);
			log.verbose("Dist Taxes : "+XMLUtil.getElementXMLString(lineTaxes));
			lineTaxes = removeRetShippingTax(lineTaxes);
			log.verbose("Final Tax : "+XMLUtil.getElementXMLString(lineTaxes));
			lineTaxes.setAttribute(AcademyConstants.ATTR_RESET, AcademyConstants.STR_YES);
			XMLUtil.importElement(eleOrderLine, lineTaxes);
			
				}
				
				XMLUtil.importElement(eleOrderLines, eleOrderLine);
				XMLUtil.importElement(docChangeReturnOrderInput
						.getDocumentElement(), eleOrderLines);

				Element eleRootElem = (Element) docChangeReturnOrderInput
						.getDocumentElement();
				Element tempNode = (Element) eleRootElem.cloneNode(true);

				Element changeListRootEle = docChangeReturnOrderInputList
						.getDocumentElement();
				Element orderElem = docChangeReturnOrderInputList
						.createElement(AcademyConstants.ELE_ORDER);
				XMLUtil.copyElement(docChangeReturnOrderInputList, tempNode,
						orderElem);
				changeListRootEle.appendChild(orderElem);

			} else {
				// Check if OrderLines element is present.
//adding References - to store sales order Invoice No for Return Invoice T-log message
				
				Element eleReferences = docChangeReturnOrderInputList
				.createElement("References");
				Element eleRef =docChangeReturnOrderInputList.createElement("Reference");
			eleRef.setAttribute(AcademyConstants.ATTR_NAME,"SalesOrderInvoiceNo");
				eleRef.setAttribute(AcademyConstants.ATTR_VALUE,salesInvoiceNo);
				eleReferences.appendChild((Node)eleRef);
				docChangeReturnOrderInputList.getDocumentElement().appendChild((Node) eleReferences);
			    //adding References - to store sales order Invoice No for Return Invoice T-log message
				
				
				Element eleOrderLines1 = (Element) currOrderElem
						.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINES).item(0);
				if (YFCObject.isVoid(eleOrderLines1)) {
					eleOrderLines1 = docChangeReturnOrderInputList
							.createElement(AcademyConstants.ELEM_ORDER_LINES);
					currOrderElem.appendChild(eleOrderLines1);
				}
				// Check if line is present
				Element currReturnLine = (Element) XPathUtil.getNode(
						currOrderElem,
						"Order/OrderLines/OrderLine[@OrderLineKey='"
								+ returnLineKey + "']");
				if (YFCObject.isVoid(currReturnLine)) {
					eleOrderLine = docChangeReturnOrderInputList
							.createElement(AcademyConstants.ELEM_ORDER_LINE);
					eleOrderLine
							.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,
									returnLineKey);
					eleOrderLine.setAttribute(AcademyConstants.SHIP_NODE,
							this.strShipNodeInInput);
//					1			updating the ReturnReason at line level with updated one.
					eleOrderLine.setAttribute(AcademyConstants.ATTR_RETURN_REASON,
							clsReturnReasonCode);
					if (true) {
						log.verbose("*********** Else Block for Existed RO****************");
						Element lineCharges = docChangeReturnOrderInputList
								.createElement(AcademyConstants.ELE_LINE_CHARGES);
						Element lineChargesinParentOrder = AcademyReturnOrderUtil.getLineCharges(
								returnLineKey, eleReturnOrders);
						XMLUtil.copyElement(docChangeReturnOrderInputList,
								lineChargesinParentOrder, lineCharges);
						lineCharges = removeRetShippingCharge(lineCharges);
						lineCharges.setAttribute(AcademyConstants.ATTR_RESET, AcademyConstants.STR_YES);
						XMLUtil.importElement(eleOrderLine, lineCharges);
						
						//Fix for #3821 - as part R026
						//remove corresponding ReturnShippingTaxes
						Element lineTaxes = docChangeReturnOrderInputList.createElement(AcademyConstants.ELE_LINE_TAXES);
						Element lineTaxesinParentOrder = AcademyReturnOrderUtil.getLineTaxes(
								returnLineKey, eleReturnOrders);
						log.verbose("Parent Taxes : "+XMLUtil.getElementXMLString(lineTaxesinParentOrder));
						XMLUtil.copyElement(docChangeReturnOrderInputList,
								lineTaxesinParentOrder, lineTaxes);
						log.verbose("Dist Taxes : "+XMLUtil.getElementXMLString(lineTaxes));
						lineTaxes = removeRetShippingTax(lineTaxes);
						log.verbose("Final Tax : "+XMLUtil.getElementXMLString(lineTaxes));
						lineTaxes.setAttribute(AcademyConstants.ATTR_RESET, AcademyConstants.STR_YES);
						XMLUtil.importElement(eleOrderLine, lineTaxes);
						//End of #3821
					}
					XMLUtil.importElement(eleOrderLines1, eleOrderLine);

				}
			}
			log.endTimer("End of prepareInputForChangeOrder");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		log
				.verbose("*************** Exiting prepareInputForChangeOrder method************ ");
		if (!YFCObject.isVoid(docChangeReturnOrderInput))
			log
					.verbose("*************** Returning document docChangeReturnOrderInput::************ "
							+ XMLUtil.getXMLString(docChangeReturnOrderInput));
		return docChangeReturnOrderInput;
	}
/**
 *  this method is used to remove the shipping/Return charges based on Return Reason Codes (Academy fault or customer faults)
 * @param lineCharges
 * @return
 */
	private Element removeRetShippingCharge(Element lineCharges) {
		try {
			log
			.verbose("*************** Inside removeRetShippingCharge method************ ");
			NodeList nLineChargeList = XPathUtil.getNodeList(lineCharges,
					"LineCharge[@ChargeCategory='ReturnShippingCharge']");
			
			NodeList nLineShipChargeList = XPathUtil.getNodeList(lineCharges,
			"LineCharge[@ChargeName='ShippingCharge']");
			
			NodeList nLineShipChargePromotionList = XPathUtil.getNodeList(lineCharges,
			"LineCharge[@ChargeName='ShippingPromotion']");
			log
			.verbose("*************** Inside removeRetShippingCharge method & removing the Return charges************!YFCObject.isVoid(nLineChargeList) "+nLineChargeList.getLength());
			if (nLineChargeList!=null && nLineChargeList.getLength() > 0 &&(removeLineCharges)) {
				log
				.verbose("*************** Inside nLineChargeList and removing line charges************ ");
				Element remLineChrg = (Element) nLineChargeList.item(0);
				lineCharges.removeChild(remLineChrg);
			}
			
			//Removing the Shipping Charges if its a Customer Fault
			log.verbose("nLineShipChargeList.getLength() : "+nLineShipChargeList.getLength());
			if (nLineShipChargeList!=null && nLineShipChargeList.getLength() > 0 && (removeLineShipCharges)) {
				log
				.verbose("*************** Inside nLineShipChargeList and removing the shipping charges************ ");
				Element remLineShipChrg = (Element) nLineShipChargeList.item(0);
				lineCharges.removeChild(remLineShipChrg);

			}
			//Remove Shipping Promotion being copied over to return.
			log.verbose("nLineShipChargePromotionList.getLength() : "+nLineShipChargePromotionList.getLength());
			if (nLineShipChargePromotionList!=null && nLineShipChargePromotionList.getLength() > 0 && (removeLineShipCharges)) {
				log
				.verbose("*************** Inside nLineShipChargePromotionList and removing the promotions************ ");
				Element remLineShipPromotionChrg = (Element) nLineShipChargePromotionList.item(0);
				lineCharges.removeChild(remLineShipPromotionChrg);

			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//throw new YFSException(e.getMessage());
		}
		return lineCharges;
	}
//	Remove corresponding ReturnShippingTax along with ReturnShippingCharges
	private Element removeRetShippingTax(Element lineTaxes) {
		
		log.verbose("Inside removeRetShippingTax() "+XMLUtil.getElementXMLString(lineTaxes));
		log.verbose("The value of removeLineCharges inside the method removeRetShippingTax() is "+removeLineCharges);
		log.verbose("The value of removeLineShipCharges inside the method removeRetShippingTax() is "+removeLineShipCharges);
		try {
			NodeList nLineTaxList = XPathUtil.getNodeList(lineTaxes,
					"LineTax[@TaxName='ReturnShipTax']");
			NodeList nLineShipTaxList = XPathUtil.getNodeList(lineTaxes,
			"LineTax[@ChargeCategory='Shipping']");
			//NodeList nLineShipTaxList = XPathUtil.getNodeList(lineTaxes,
			//"LineTax[@TaxName='ShippingTax']");
			log.verbose("nLineTaxList.getLength() : "+nLineTaxList.getLength()+"\t"+removeLineCharges);
			if (nLineTaxList!=null && nLineTaxList.getLength() > 0 && (removeLineCharges)) {
			log.verbose("Inside the block where it removes return shipping taxes");
				Element remLineTax = (Element) nLineTaxList.item(0);
				lineTaxes.removeChild(remLineTax);
			}
	//		Removing the Shipping Charge Taxes if its a Customer Fault - bug#
			log.verbose("nLineShipTaxList.getLength() : "+nLineShipTaxList.getLength()+"\t"+removeLineShipCharges);
			if (nLineShipTaxList!=null && nLineShipTaxList.getLength() > 0 && (removeLineShipCharges)) {
			//removing all Shipping Taxes along with ShippingTax values.
			log.verbose("Inside the block where it removes outbound shipping taxes");
				int shipTaxLen=nLineShipTaxList.getLength();
			log.verbose("shipTaxLen----> "+shipTaxLen);	
				for(int j=0;j<shipTaxLen;j++)
				{
				log.verbose("Inside the loop which removes the shipping taxes");	
				Element remShipLineTax = (Element) nLineShipTaxList.item(j);
				lineTaxes.removeChild(remShipLineTax);
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//throw new YFSException(e.getMessage());
		}
		return lineTaxes;
	}
	//this method is used to preparing the input XML for cretaing the Return Order
	private Document prepareInputForCreateOrder(Element eleSalesOrderDetails,
			Document docCreateOrderInput, Element eleCurrOrderLine,
			String strItemId, String strQty, Element currLine) {
		Element eleOrderInput = null;
		Element eleOrderLines = null;
		Element eleOrderLine = null;
		Element eleDerivedFrom = null;
		Element eleOrderLineTransQty = null;
		Element elePersonInfoBillTo = null;
		//Element eleCurrentReturnLine = null;
		Element eleTemp = null;
		//Element eleSalesOrderLine = null;		

		try {
			log.beginTimer("begin of prepareInputForCreateOrder");
			log
					.verbose("*************** Inside prepareInputForCreateOrder method************ ");
			elePersonInfoBillTo = (Element) XPathUtil.getNode(
					eleSalesOrderDetails, "PersonInfoBillTo");

			if (YFCObject.isVoid(docCreateOrderInput)) {
				docCreateOrderInput = XMLUtil
						.createDocument(AcademyConstants.ELE_ORDER);
				eleOrderInput = docCreateOrderInput.getDocumentElement();
				Element eleExtn = docCreateOrderInput
						.createElement(AcademyConstants.ELE_EXTN);
				// #2880
				log.verbose("preparing Notes for Return Order");
				Element eleOrderNotes = docCreateOrderInput.createElement(AcademyConstants.ELE_NOTES);
				Element eleNote = docCreateOrderInput.createElement(AcademyConstants.ELE_NOTE);
				String noteText = "Return Order created for sales order # "+eleSalesOrderDetails.getAttribute(AcademyConstants.ATTR_ORDER_NO);
				eleNote.setAttribute(AcademyConstants.ATTR_NOTE_TEXT, noteText);
				eleOrderNotes.appendChild(eleNote);
				eleOrderInput.appendChild(eleOrderNotes);
				log.verbose("Notes message : \n"+XMLUtil.getElementXMLString(eleOrderNotes));
				// #2880
				
				eleOrderInput.setAttribute(
						AcademyConstants.ATTR_APPLY_DEFLT_TMPLT,
						AcademyConstants.STR_YES);
				eleOrderInput.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
						AcademyConstants.STR_RETURN_DOCTYPE);
				
				// Start Change - stamp CustomerEmailID on return order - Manju 12/28/2010 
				String soCustEmailID = eleSalesOrderDetails.getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID);
				eleOrderInput.setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID, soCustEmailID);
				// End Change - stamp CustomerEmailID on return order - Manju 12/28/2010
				
				// Including Change for Store-Returns Stamping ReturnOrder Date
				// comes from CLS/Store Interfaces XML's
				if (this.returnOrdDate != null || this.returnOrdDate != "")
					eleOrderInput.setAttribute("OrderDate", this.returnOrdDate);

				//adding References - to store sales order Invoice No for Return Invoice T-log message
				
				Element eleReferences = docCreateOrderInput
				.createElement("References");
				Element eleRef =docCreateOrderInput.createElement("Reference");
			eleRef.setAttribute(AcademyConstants.ATTR_NAME,"SalesOrderInvoiceNo");
				eleRef.setAttribute(AcademyConstants.ATTR_VALUE,salesInvoiceNo);
				eleReferences.appendChild((Node)eleRef);
				eleOrderInput.appendChild((Node) eleReferences);
			    //adding References - to store sales order Invoice No for Return Invoice T-log message
				
				
				eleOrderInput.setAttribute(AcademyConstants.ATTR_RECV_NODE,
						this.strShipNodeInInput);
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

				// Including change for Store-returns adding Payment Details to
				// ReturnOrder
				if (!returnNodeList.contains(strShipNodeInInput)) {
					
					if (bPaymentPresent) {
						Element elePaymethods = docCreateOrderInput
								.createElement(AcademyConstants.ELE_PAYMENT_METHODS);
						int payMethLength=elePayments.getElementsByTagName(
								AcademyConstants.ELE_PAYMENT_METHOD)
								.getLength();
						for (int i = 0; i <payMethLength; i++) {
					
							Element elePaymethod = docCreateOrderInput
									.createElement(AcademyConstants.ELE_PAYMENT_METHOD);
							Element elePaymethodDetail = docCreateOrderInput
									.createElement("PaymentDetails");

							String refundAmt = ((Element) elePayments
									.getElementsByTagName(
											AcademyConstants.ELE_PAYMENT_METHOD)
									.item(i)).getAttribute("TotalRefundAmount");
							refundAmt = "-" + refundAmt;
					    	elePaymethodDetail.setAttribute("RequestAmount",
									refundAmt);
							elePaymethodDetail.setAttribute("ProcessedAmount",
									refundAmt);
							elePaymethodDetail.setAttribute("ChargeType",
									"CHARGE");
							elePaymethodDetail.setAttribute("TranType",
									"REFUND");
							elePaymethodDetail.setAttribute("TranReturnCode",
							"REFUND");
														
							elePaymethod
									.setAttribute(
											AcademyConstants.ATTR_PAYMENT_TYPE,
											(((Element) elePayments
													.getElementsByTagName(
															AcademyConstants.ELE_PAYMENT_METHOD)
													.item(i))
													.getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE)).toUpperCase());
							elePaymethod
									.setAttribute(
											"CreditCardNo",
											((Element) elePayments
													.getElementsByTagName(
															AcademyConstants.ELE_PAYMENT_METHOD)
													.item(i))
													.getAttribute("DisplayCreditCardNo"));
							/*elePaymethod
							.setAttribute(
									"DisplayCreditCardNo",
									((Element) elePayments
											.getElementsByTagName(
													AcademyConstants.ELE_PAYMENT_METHOD)
											.item(i))
											.getAttribute("DisplayCreditCardNo"));*/
							elePaymethod
									.setAttribute(
											AcademyConstants.ATTR_SVCNO,
											((Element) elePayments
													.getElementsByTagName(
															AcademyConstants.ELE_PAYMENT_METHOD)
													.item(i))
													.getAttribute(AcademyConstants.ATTR_SVCNO));

							elePaymethod.appendChild((Node) elePaymethodDetail);
							elePaymethods.appendChild((Node) elePaymethod);
						}
						eleOrderInput.appendChild((Node) elePaymethods);
						
						}
					eleExtn.setAttribute(
							AcademyConstants.ATTR_EXTN_IS_WEBORDER,
							AcademyConstants.STR_YES);
					eleOrderInput.appendChild((Node) eleExtn);

				}
				// Including change for Store-returns stamping ExtnIsWebOrder as
				// No for CLS-return Receipt process
				if (returnNodeList.contains(strShipNodeInInput)) {
					
					//if (isAcademyTrackingNo(this.strTrackingNo))
						if(AcademyReturnOrderUtil.isAcademyTrackingNo(strTrackingNo))
						eleExtn.setAttribute(
								AcademyConstants.ATTR_EXTN_IS_WEBORDER,
								AcademyConstants.STR_NO);
					else
						eleExtn.setAttribute(
								AcademyConstants.ATTR_EXTN_IS_WEBORDER,
								AcademyConstants.STR_YES);
					
					eleOrderInput.appendChild((Node) eleExtn);

				}

				eleTemp = (Element) docCreateOrderInput.importNode(
						elePersonInfoBillTo, true);
				eleOrderInput.appendChild(eleTemp);
				// eleTemp=(Element)eleOrderInput.appendChild(elePersonInfoBillTo);
				// XMLUtil.importElement(eleOrderInput,eleTemp);

				eleOrderInput.appendChild(eleTemp);
				eleOrderLines = docCreateOrderInput
						.createElement(AcademyConstants.ELEM_ORDER_LINES);
				eleOrderInput.appendChild(eleOrderLines);

			} else {
				eleOrderInput = docCreateOrderInput.getDocumentElement();
				eleOrderLines = (Element) eleOrderInput.getElementsByTagName(
						AcademyConstants.ELEM_ORDER_LINES).item(0);
			}
			/*
			 * eleSalesOrderLine =
			 * getMatchingSalesOrderLine(eleSalesOrderDetails, strItemId,
			 * strQty);
			 */

			if (!YFCObject.isVoid(currLine)) {
				log
						.verbose("*************** Matching Sales Order Line Element************ "
								+ XMLUtil.getElementXMLString(currLine));
				
			}
			if (!YFCObject.isVoid(currLine)) {

				eleOrderLine = docCreateOrderInput
						.createElement(AcademyConstants.ELEM_ORDER_LINE);
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ACTION,
						AcademyConstants.STR_CREATE);
				eleOrderLine.setAttribute(AcademyConstants.SHIP_NODE,
						this.strShipNodeInInput);
//				2 updating the ReturnReason at line level with updated one.
				eleOrderLine
						.setAttribute(
								AcademyConstants.ATTR_RETURN_REASON,
								eleCurrOrderLine
										.getAttribute(AcademyConstants.ATTR_RETURN_REASON));
			
				eleDerivedFrom = docCreateOrderInput
						.createElement(AcademyConstants.ATTR_DERIVED_FROM);
				eleDerivedFrom
						.setAttribute(
								AcademyConstants.ATTR_ORDER_LINE_KEY,
								currLine
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
				XMLUtil.importElement(eleOrderLine, eleDerivedFrom);
				XMLUtil.importElement(eleOrderLine, eleOrderLineTransQty);

				// adding the tax charges that comes from Store-returns - if
				// ShipNode!=703 then.
				if (!(returnNodeList.contains(strShipNodeInInput))) {
				
					Element eleLineCharges = docCreateOrderInput
							.createElement(AcademyConstants.ELE_LINE_CHARGES);
					Element eleLineCharge = docCreateOrderInput
							.createElement(AcademyConstants.ELE_LINE_CHARGE);

					Element eleLineTaxes = docCreateOrderInput
							.createElement(AcademyConstants.ELE_LINE_TAXES);
					Element eleLineTax = docCreateOrderInput
							.createElement(AcademyConstants.ELE_LINE_TAX);
					eleLineTaxes.appendChild((Node) eleLineTax);
					eleLineCharges.appendChild((Node) eleLineCharge);
					eleLineCharge.setAttribute(
							AcademyConstants.ATTR_CHARGE_CATEGORY,
							AcademyConstants.STR_SHIPPING);
					eleLineCharge.setAttribute(
							AcademyConstants.ATTR_CHARGES_PER_LINE,
							eleCurrOrderLine.getAttribute(AcademyConstants.STR_SHIPPING_CHARGE));
					eleLineCharge
							.setAttribute(AcademyConstants.ATTR_CHARGE_NAME,
									AcademyConstants.STR_SHIPPING_CHARGE);
					  
					  eleLineTax.setAttribute(AcademyConstants.ATTR_TAX,
					  eleCurrOrderLine.getAttribute(AcademyConstants.ATTR_TAX));
					  eleLineTax.setAttribute(AcademyConstants.ATTR_CHARGE_NAME,AcademyConstants.STR_SHIPPING_CHARGE);
					  eleLineTax.setAttribute(AcademyConstants.ATTR_CHARGE_CATEGORY,AcademyConstants.STR_SHIPPING);
					  eleLineTax.setAttribute(AcademyConstants.ATTR_TAX_NAME,AcademyConstants.STR_SHIPPING_TAX);
					  eleOrderLine.appendChild((Node)eleLineTaxes);
					 
					eleOrderLine.appendChild((Node) eleLineCharges);
					
				}
				// End of the logic

				eleOrderLines.appendChild(eleOrderLine);
				
				
			} else {
				log
						.verbose("***************Throwing exception since no matching Sales Order line found for item************ ");
				throw new Exception(
						"No Matching Sales Order Line found for Item"
								+ strItemId + " and Quantity" + strQty);
			}
			log.endTimer("End of prepareInputForCreateOrder");
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

	
// this Method is used to update teh return Lines with Corresponding Qty for receipt process & itemID,ShipNode etc.
	private Document updateReturnLinesDocument(Document returnReceiptDocument,
			Element eleCurrentOrderLine, String returnHeaderKey, String strQty) {
		String strUOM = null;
		String strItemID = null;
		String strReturnOrderLineKey = null;
		String strReturnQty = null;
		Element eleMatchingHeader = null;

		try {
			log.beginTimer("Begin of updateReturnLinesDocument");
			log
					.verbose("*************** Inside updateReturnLinesDocument method************ ");
			if (!YFCObject.isVoid(eleCurrentOrderLine)) {
				strReturnOrderLineKey = eleCurrentOrderLine
						.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
				strReturnQty = eleCurrentOrderLine
						.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
				strUOM = XPathUtil.getString(eleCurrentOrderLine,
						"Item/@UnitOfMeasure");
				strItemID = XPathUtil.getString(eleCurrentOrderLine,
						"Item/@ItemID");
			}

			if (YFCObject.isVoid(returnReceiptDocument)) {
				returnReceiptDocument = XMLUtil
						.createDocument(AcademyConstants.ELE_RETURN_ORDERS);
				Element eleChildOrder = returnReceiptDocument
						.createElement(AcademyConstants.ELE_ORDER);
				eleChildOrder.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY,
						returnHeaderKey);
				eleChildOrder.setAttribute(AcademyConstants.ATTR_RECV_NODE,
						strShipNodeInInput);
				Element eleOrderLines = returnReceiptDocument
						.createElement(AcademyConstants.ELEM_ORDER_LINES);
				Element eleOrderLine = returnReceiptDocument
						.createElement(AcademyConstants.ELEM_ORDER_LINE);
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,
						strReturnOrderLineKey);
				eleOrderLine.setAttribute(AcademyConstants.ATTR_QUANTITY,
						strQty);
				eleOrderLine.setAttribute(AcademyConstants.ATTR_UOM, strUOM);
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ITEM_ID,
						strItemID);
				XMLUtil.importElement(eleOrderLines, eleOrderLine);
				XMLUtil.importElement(eleChildOrder, eleOrderLines);
				XMLUtil.importElement(returnReceiptDocument
						.getDocumentElement(), eleChildOrder);
			} else {

				eleMatchingHeader = (Element) XPathUtil.getNode(
						returnReceiptDocument.getDocumentElement(),
						"//Order[@OrderHeaderKey='" + returnHeaderKey + "']");
				if (YFCObject.isVoid(eleMatchingHeader)) {
					Element eleChildOrder = returnReceiptDocument
							.createElement(AcademyConstants.ELE_ORDER);
					eleChildOrder.setAttribute(
							AcademyConstants.ATTR_ORDER_HEADER_KEY,
							returnHeaderKey);
					eleChildOrder.setAttribute(AcademyConstants.ATTR_RECV_NODE,
							strShipNodeInInput);
					Element eleOrderLines = returnReceiptDocument
							.createElement(AcademyConstants.ELEM_ORDER_LINES);
					Element eleOrderLine = returnReceiptDocument
							.createElement(AcademyConstants.ELEM_ORDER_LINE);
					eleOrderLine.setAttribute(
							AcademyConstants.ATTR_ORDER_LINE_KEY,
							strReturnOrderLineKey);
					eleOrderLine.setAttribute(AcademyConstants.ATTR_QUANTITY,
							strQty);
					eleOrderLine
							.setAttribute(AcademyConstants.ATTR_UOM, strUOM);
					eleOrderLine.setAttribute(AcademyConstants.ATTR_ITEM_ID,
							strItemID);
					XMLUtil.importElement(eleOrderLines, eleOrderLine);
					XMLUtil.importElement(eleChildOrder, eleOrderLines);
					XMLUtil.importElement(returnReceiptDocument
							.getDocumentElement(), eleChildOrder);
				} else {
					Element eleOrderLines = (Element) eleMatchingHeader
							.getElementsByTagName(
									AcademyConstants.ELEM_ORDER_LINES).item(0);
					Element eleOrderLine = returnReceiptDocument
							.createElement(AcademyConstants.ELEM_ORDER_LINE);
					eleOrderLine.setAttribute(
							AcademyConstants.ATTR_ORDER_LINE_KEY,
							strReturnOrderLineKey);
					eleOrderLine.setAttribute(AcademyConstants.ATTR_QUANTITY,
							strQty);
					eleOrderLine
							.setAttribute(AcademyConstants.ATTR_UOM, strUOM);
					eleOrderLine.setAttribute(AcademyConstants.ATTR_ITEM_ID,
							strItemID);
					XMLUtil.importElement(eleOrderLines, eleOrderLine);

				}
			}
			log.endTimer("End of updateReturnLinesDocument");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());

		}
		log
				.verbose("*************** Exiting updateReturnLinesDocument method************ ");
		if (!YFCObject.isVoid(returnReceiptDocument))
			log
					.verbose("*************** Returning  Document returnReceiptDocument ************ "
							+ XMLUtil.getXMLString(returnReceiptDocument));
		
		return returnReceiptDocument;

	}

//This method is to verify the payment details from Strore Return Receipt msg and setting boolean flags correspondingly.
	private boolean checkIfPaymentPresentInDocument(Document docInput) {

		boolean bPaymentExists = false;
		Element elePayment = null;
		try {

			if (docInput.getDocumentElement().getAttribute("TotalRefundAmount") != null)
				totalStoreRefundAmt = docInput.getDocumentElement()
						.getAttribute("TotalRefundAmount");
			log
					.verbose("*************** Inside checkIfPaymentPresentInDocument method************ ");
			elePayment = (Element) XPathUtil
					.getNode(docInput.getDocumentElement(),
							AcademyConstants.ELE_PAYMENT_METHODS);
			if (!YFCObject.isVoid(elePayment)) {
				bPaymentExists = true;
				bPaymentPresent = true;
				
				elePayments = (Element) XPathUtil.getNode(docInput
						.getDocumentElement(),
						AcademyConstants.ELE_PAYMENT_METHODS);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		log.verbose("*************** Payment Exists in Input************ "
				+ String.valueOf(bPaymentExists));
		log
				.verbose("*************** Exiting checkIfPaymentPresentInDocument method************ ");

		return bPaymentExists;
	}

	//Getting the Order Details with filtered output template format - which contains return order details too.
	private Element getCompleteSalesOrderDetails(String orderNo,
			String docType, YFSEnvironment env) {
		Element eleSalesOrderDetails = null;
		Element eleOrder = null;
		Document docGetOrderDetailsInput = null;
		Document docGetOrderDetailsOutput = null;

		try {

			log
					.verbose("*************** Inside getCompleteSalesOrderDetails method************ ");
			docGetOrderDetailsInput = XMLUtil
					.createDocument(AcademyConstants.ELE_ORDER);
			eleOrder = docGetOrderDetailsInput.getDocumentElement();
			eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO, orderNo);
			eleOrder.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			eleOrder.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
			env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
					AcademyConstants.STR_TEMPLATEFILE_GETCOMPLETEORDER_DETAILS);
			if (!YFCObject.isVoid(docGetOrderDetailsInput))
				log.verbose("*************** Calling getCompleteOrderDetails API with input************ "
								+ XMLUtil.getXMLString(docGetOrderDetailsInput));
			docGetOrderDetailsOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
					docGetOrderDetailsInput);
			env.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS);
			if (!YFCObject.isVoid(docGetOrderDetailsOutput))
				log
						.verbose("*************** Output of getCompleteOrderDetails************ "
								+ XMLUtil
										.getXMLString(docGetOrderDetailsOutput));
			eleSalesOrderDetails = docGetOrderDetailsOutput
					.getDocumentElement();
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		log
				.verbose("*************** Exiting getCompleteSalesOrderDetails method************ ");
		return eleSalesOrderDetails;
	}

	//this method is used to create a return order with prepared retunr input XML if return not existed sceanario.
	private Document createReturnOrder(Document docApiInput, YFSEnvironment env) {

		Document docCreateReturnOutput = null;
		try {
			log.beginTimer("Begining of createReturnOrder");
			log
					.verbose("*************** Inside createReturnOrder method************ ");
			if (returnNodeList.contains(strShipNodeInInput)) {
				docApiInput.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_ORDER_NAME,
						AcademyConstants.STR_CLS_RETURN);
			} else {
				docApiInput.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_ORDER_NAME,
						AcademyConstants.STR_INSTORE_RETURN);
				docApiInput.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_RETURN_GIFT_RECIPIENT,
						AcademyConstants.STR_YES);
			}
			env.setApiTemplate(AcademyConstants.API_CREATE_ORDER,
					AcademyConstants.STR_TEMPLATEFILE_CREATE_ORDER);

			log
					.verbose("*************** Calling createOrder with input************ "
							+ XMLUtil.getXMLString(docApiInput));
			env.setTxnObject("RepricingFlag", AcademyConstants.STR_YES);
			
					docCreateReturnOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_CREATE_ORDER, docApiInput);
					
					
					
			// Tried for testing - Temporary fix
			// ((YCPContext) env).commit();
			if (!YFCObject.isVoid(docCreateReturnOutput))
				log
						.verbose("*************** Output of createOrder (Return)************ "
								+ XMLUtil.getXMLString(docCreateReturnOutput));
			log.endTimer("End of createReturnOrder");
			// # 2880 - Update notes for original sales order with new return order details
			// prepare input to changeOrder API with Notes
			//Element salesOrderEle = (Element)eleSalesOrderDetails.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
			if(!YFCObject.isVoid(eleSalesOrderDetails) && !YFCObject.isVoid(docCreateReturnOutput)){
				log.verbose("Prepare input to changeOrder API to update Notes on sales order# :"+eleSalesOrderDetails.getAttribute(AcademyConstants.ATTR_ORDER_NO));
				Document docChangeOrderInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
				Element eleSalesOrder = docChangeOrderInput.getDocumentElement();
				Element eleNotes = docChangeOrderInput.createElement(AcademyConstants.ELE_NOTES);
				Element eleNote = docChangeOrderInput.createElement(AcademyConstants.ELE_NOTE);
				// Set required data
				eleSalesOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, eleSalesOrderDetails.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
				eleSalesOrder.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.ATTR_Y);
				String noteTxt = "The return order# "+docCreateReturnOutput.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO);
				eleNote.setAttribute(AcademyConstants.ATTR_NOTE_TEXT, noteTxt);
				// append the child elements
				eleNotes.appendChild(eleNote);
				eleSalesOrder.appendChild(eleNotes);
				log.verbose("The Input xml to changeOrder API is :\n"+XMLUtil.getXMLString(docChangeOrderInput));
				// Invoke changeOrder API to update the Notes on Original SalesOrder
				AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, docChangeOrderInput);
				log.verbose("changeOrder API invoked successfully......");
			}
			// # 2880
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		log
				.verbose("*************** Exiting createReturnOrder method************ ");
		return docCreateReturnOutput;
	}

	private void appendPaymentInformationFromInput(
			Document docCreateOrderInput, Element paymentMethod) {

		/*
		 * Start Fix Fix for bug -8757
		 */
		log
				.verbose("*************** Inside appendPaymentInformationFromInput method************ ");
		Document docPayment = null;
		Element elePaymentMethods = null;
		Element elePaymentMethod = null;
		try {
			
			docPayment = XMLUtil
					.createDocument(AcademyConstants.ELE_PAYMENT_METHODS);
			elePaymentMethods = docPayment.getDocumentElement();
			elePaymentMethod = docPayment
					.createElement(AcademyConstants.ELE_PAYMENT_METHOD);
			elePaymentMethod.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE,
					AcademyConstants.STR_GIFT_CARD);
			elePaymentMethod.setAttribute(AcademyConstants.ATTR_SVCNO,
					paymentMethod.getAttribute(AcademyConstants.ATTR_SVCNO));
			XMLUtil.importElement(elePaymentMethods, elePaymentMethod);
			XMLUtil.importElement(docCreateOrderInput.getDocumentElement(),
					elePaymentMethods);
			if (!YFCObject.isVoid(docCreateOrderInput))
				log
						.verbose("***************docCreateOrderInput after appending Payment Details ************ "
								+ XMLUtil.getXMLString(docCreateOrderInput));

		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

		/*
		 * End Fix Fix for bug -8757
		 */

	}

	// Below following methods are used to process  moving the Return Order Authorized status to return received, ReceiptClosed statuses. 
	
	
	private void receiveReturnOrder(Document docReturnLinesReceiveDoc,
			YFSEnvironment env) {
		Element eleReturnOrders = null;
		Element eleCurrentReturnOrder = null;
		Document docStartReceipt = null;
		Document docReturnReceiveOutput = null;
		Document docCloseReceiptOutput = null;
		NodeList nListReturnOrders = null;
		int iNumberOfReturns = 0;
		log
				.verbose("*************** Inside receiveReturnOrder method************ ");
		log.beginTimer("Begin of receiveReturnOrder");
		try {
			if (!YFCObject.isVoid(docReturnLinesReceiveDoc)) {
				eleReturnOrders = docReturnLinesReceiveDoc.getDocumentElement();
				nListReturnOrders = XMLUtil.getNodeList(eleReturnOrders,
						"Order");
				iNumberOfReturns = nListReturnOrders.getLength();
				for (int i = 0; i < iNumberOfReturns; i++) {
					eleCurrentReturnOrder = (Element) nListReturnOrders.item(i);
					docStartReceipt = startReceiptForReturnOrder(
							eleCurrentReturnOrder, env);
					if (!YFCObject.isVoid(docStartReceipt)) {
						log.verbose("*************** docStartReceipt::::"
								+ XMLUtil.getXMLString(docStartReceipt));
					}
					docReturnReceiveOutput = receiveOrderForReturn(
							docStartReceipt, eleCurrentReturnOrder, env);
					if (!YFCObject.isVoid(docReturnReceiveOutput)) {
						log
								.verbose("*************** docReturnReceiveOutput::::"
										+ XMLUtil
												.getXMLString(docReturnReceiveOutput));
					}
					docCloseReceiptOutput = closeReturnReceipt(docStartReceipt,
							env);
					if (!YFCObject.isVoid(docCloseReceiptOutput)) {
						log.verbose("*************** docCloseReceiptOutput::::"
								+ XMLUtil.getXMLString(docCloseReceiptOutput));

					}
				}
			} else
				log
						.verbose("******************Inside receiveReturnOrder docReturnLinesReceiveDoc is NULL ***********8");
			log.endTimer("End of receiveReturnOrder");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		log
				.verbose("*************** Exiting receiveReturnOrder method************ ");

	}

	private Document receiveOrderForReturn(Document docStartReceipt,
			Element eleCurrentReturnOrder, YFSEnvironment env) {
		Document receiveReturnOrderOutput = null;
		Document docReceiveReturn = null;
		Element eleReceiptLines = null;
		Element eleReceiptLine = null;
		log
				.verbose("*************** Inside receiveOrderForReturn method************ ");
		try {
			log.beginTimer("Beging of receiveOrderForReturn");
			docReceiveReturn = XMLUtil
					.createDocument(AcademyConstants.ELE_RECPT);
			docReceiveReturn.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_DOC_TYPE,
					AcademyConstants.STR_RETURN_DOCTYPE);
		
			docReceiveReturn.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_RECPT_NO,
					docStartReceipt.getDocumentElement().getAttribute(
							AcademyConstants.ATTR_RECPT_NO));
			docReceiveReturn.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_RECPT_HEADERKEY,
					docStartReceipt.getDocumentElement().getAttribute(
							AcademyConstants.ATTR_RECPT_HEADERKEY));
			docReceiveReturn.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_RECV_NODE,
					docStartReceipt.getDocumentElement().getAttribute(
							AcademyConstants.ATTR_RECV_NODE));
			docReceiveReturn.getDocumentElement().setAttribute(
					AcademyConstants.STR_ORDR_HDR_KEY,
					eleCurrentReturnOrder
							.getAttribute(AcademyConstants.STR_ORDR_HDR_KEY));
			eleReceiptLines = docReceiveReturn
					.createElement(AcademyConstants.RECEIPT_LINES);
			NodeList nReturnLines = XPathUtil.getNodeList(
					eleCurrentReturnOrder, AcademyConstants.XPATH_ORDERLINE);
			int iNoOfReturnLines = nReturnLines.getLength();
			for (int j = 0; j < iNoOfReturnLines; j++) {
				Element eleCurrentLine = (Element) nReturnLines.item(j);
				eleReceiptLine = docReceiveReturn
						.createElement(AcademyConstants.RECEIPT_LINE);
				eleReceiptLine.setAttribute(AcademyConstants.ATTR_UOM,
						eleCurrentLine.getAttribute(AcademyConstants.ATTR_UOM));
				eleReceiptLine.setAttribute(AcademyConstants.ATTR_ITEM_ID,
						eleCurrentLine
								.getAttribute(AcademyConstants.ATTR_ITEM_ID));
				// eleReceiptLine.setAttribute(AcademyConstants.ATTR_PROD_CLASS,
				// AcademyConstants.PRODUCT_CLASS);
				eleReceiptLine
						.setAttribute(
								AcademyConstants.ATTR_ORDER_LINE_KEY,
								eleCurrentLine
										.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
				eleReceiptLine.setAttribute(AcademyConstants.ATTR_QUANTITY,
						eleCurrentLine
								.getAttribute(AcademyConstants.ATTR_QUANTITY));
				
				XMLUtil.importElement(eleReceiptLines, eleReceiptLine);
			}
			XMLUtil.importElement(docReceiveReturn.getDocumentElement(),
					eleReceiptLines);
			if (!YFCObject.isVoid(docReceiveReturn))
				log
						.verbose("***************** Input to receiveOrder is **************"
								+ XMLUtil.getXMLString(docReceiveReturn));
			receiveReturnOrderOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_RECEIVE_ORDER, docReceiveReturn);
			log.endTimer("End of receiveOrderForReturn");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		log
				.verbose("*************** Exiting receiveOrderForReturn method************ ");
		return receiveReturnOrderOutput;
	}

	private Document closeReturnReceipt(Document docStartReceipt,
			YFSEnvironment env) {
		Document docCloseReceiptInput = null;
		Document docCloseReceiptOutput = null;
		Element eleReceipt = null;

		log
				.verbose("**************** Inside closeReturnReceipt ******************");

		try {
			log.beginTimer("Begin of closeReturnReceipt");
			docCloseReceiptInput = XMLUtil
					.createDocument(AcademyConstants.ELE_RECPT);
			eleReceipt = docCloseReceiptInput.getDocumentElement();
			eleReceipt.setAttribute(AcademyConstants.ATTR_RECPT_HEADERKEY,
					docStartReceipt.getDocumentElement().getAttribute(
							AcademyConstants.ATTR_RECPT_HEADERKEY));
			eleReceipt.setAttribute(AcademyConstants.ATTR_RECPT_NO,
					docStartReceipt.getDocumentElement().getAttribute(
							AcademyConstants.ATTR_RECPT_NO));
			eleReceipt.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
					AcademyConstants.STR_RETURN_DOCTYPE);
			eleReceipt.setAttribute(AcademyConstants.ATTR_RECV_NODE,
					docStartReceipt.getDocumentElement().getAttribute(
							AcademyConstants.ATTR_RECV_NODE));
			if (!YFCObject.isVoid(docCloseReceiptInput))
				log
						.verbose("*****************Input to closeReceipt is *******************"
								+ XMLUtil.getXMLString(docCloseReceiptInput));
			docCloseReceiptOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_CLOSE_RCPT, docCloseReceiptInput);
			log.endTimer("End of closeReturnReceipt");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		log
				.verbose("**************** Exiting closeReturnReceipt ******************");
		return docCloseReceiptOutput;
	}

	private Document startReceiptForReturnOrder(Element currOrder,
			YFSEnvironment env) {
		Element eleReceipt = null;
		Element eleShipment = null;
		Document docStartRecptInput = null;
		Document docStartRecptOutput = null;

		log
				.verbose("***************Inside startReceiptForReturnOrder **************");

		try {
			log.beginTimer("begin of startReceiptForReturnOrder");
			docStartRecptInput = XMLUtil
					.createDocument(AcademyConstants.ELE_RECPT);
			eleReceipt = docStartRecptInput.getDocumentElement();
			eleReceipt.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
					AcademyConstants.STR_RETURN_DOCTYPE);
			eleReceipt.setAttribute(AcademyConstants.ATTR_RECV_NODE,
					strShipNodeInInput);
			eleShipment = docStartRecptInput
					.createElement(AcademyConstants.ELE_SHIPMENT);
			eleShipment.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY,
					currOrder.getAttribute(AcademyConstants.STR_ORDR_HDR_KEY));
			eleShipment.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			XMLUtil.importElement(eleReceipt, eleShipment);
			if (!YFCObject.isVoid(docStartRecptInput))
				log
						.verbose("************ Input to startReceipt is ****************"
								+ XMLUtil.getXMLString(docStartRecptInput));
			docStartRecptOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_START_RCPT, docStartRecptInput);
			log.endTimer("End of startReceiptForReturnOrder");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		log
				.verbose("***************Exiting startReceiptForReturnOrder **************");
		return docStartRecptOutput;
	}
	

/**This method will validate if all requested Items are belogs to any single Invoice.
 * @param eleGetOrderInvoiceDetails
 * @param inputDoc
 * @param env
 * @throws Exception
 */
private void validateItemInInvoice(Element eleGetOrderInvoiceDetails,Document inputDoc,YFSEnvironment env) throws Exception {
		
		log.verbose("AcademyProcessReturnMessageAPI_validateItemInInvoice():");
		String strCorrectInvoiceNo="";
		Element eleRootOrderList = null;
		NodeList nlRootOrderInv = null;
		Element eleOrderInv = null;
		String strInvoiceType = null;
		Element eleShipment = null;
		Element eleShipmentLines = null;
		NodeList nlShipmentLine = null;
		Element eleShipmentLine = null;
		Element eleSLOL =  null;
		Double dShippedQty = null;
		Document docOrderListOutput = null;
		
		if (hmCLSItemList != null) {
		//	prepareItemListHashMapForRequestedInvoice(eleGetOrderInvoiceDetails);
			
			if(isQtyItemMismatchForRequestedInvoice())
			{
				hmgetOrderListItemList = new HashMap<String, Double>();						

				docOrderListOutput = getOrderListWithAllInvoice(eleGetOrderInvoiceDetails,env);
				eleRootOrderList = docOrderListOutput.getDocumentElement();
				
				Element eleRootOrderInvList = (Element)eleRootOrderList.getElementsByTagName("OrderInvoiceList").item(0);
				if ((!YFCObject.isVoid(eleRootOrderInvList))&&(eleRootOrderInvList.hasChildNodes()))  {
					nlRootOrderInv = XMLUtil.getNodeList(eleRootOrderInvList, AcademyConstants.ELE_ORDER_INVOICE);
					for (int iOrderInvoice = 0; iOrderInvoice < nlRootOrderInv.getLength(); iOrderInvoice++) {
						eleOrderInv = (Element) eleRootOrderInvList.getElementsByTagName(AcademyConstants.ELE_ORDER_INVOICE).item(iOrderInvoice);
						strInvoiceType = eleOrderInv.getAttribute(AcademyConstants.ATTR_INVOICE_TYPE); 
						if ("SHIPMENT".equalsIgnoreCase(strInvoiceType)){
							strCorrectInvoiceNo = eleOrderInv.getAttribute(AcademyConstants.ATTR_INVOICE_NO);
							eleShipment = (Element)eleOrderInv.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
							Double shipStatus = Double.parseDouble(eleShipment.getAttribute(AcademyConstants.ATTR_STATUS));
							if (shipStatus > 1300){
								//Element eleRootContainer = (Element)eleRootShipment.getElementsByTagName(AcademyConstants.ELE_CONTAINERS).item(0);
								//If tracking also needs validation then add some code here
								eleShipmentLines = (Element)eleShipment.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES).item(0);
								if ((!YFCObject.isVoid(eleShipmentLines))&&(eleShipmentLines.hasChildNodes()))  {
									nlShipmentLine = XMLUtil.getNodeList(eleShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE);
									for (int iShipmentLineCount = 0; iShipmentLineCount < nlShipmentLine.getLength(); iShipmentLineCount++) {
										eleShipmentLine = (Element) eleShipmentLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE).item(iShipmentLineCount);
										String strSLItemId = eleShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID);
										eleSLOL =  (Element)eleShipmentLine.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE).item(0);
										dShippedQty = Double.parseDouble(eleSLOL.getAttribute("ShippedQuantity"));

										/*if(hmCLSItemList.containsKey(strSLItemId)){
											Double dbRetQty = (Double)hmCLSItemList.get(strSLItemId);
											if (dbRetQty <= dShippedQty){
												salesInvoiceNo = strCorrectInvoiceNo;												
												hmgetOrderListItemList.put(strSLItemId, dbRetQty);
											}	
										}*/
										if(hmCLSItemList.containsKey(strSLItemId)){
											hmgetOrderListItemList.put(strSLItemId, dShippedQty);
										}
									}
								}
							}
						}
						/*if(hmgetOrderListItemList.size() > 0 && hmgetOrderListItemList.size() != hmCLSItemList.size()){
							hmgetOrderListItemList.clear();
						}*/
					}

					log.verbose("hmgetOrderListItemList\n"+hmgetOrderListItemList.toString());

					if(hmgetOrderListItemList.size() != hmCLSItemList.size()){
						log.verbose("Throwing Error");
						YFSException e = new YFSException();
						e.setErrorCode("Item(s) not found");
						e.setErrorDescription("All requested items are not belongs to the order of given invoice.");
						throw e;
					}
					log.verbose("All Item Id and invoice match");
				}				
			}
		}
	}

/**
 * @return
 * @throws Exception
 */
private Document getOrderListWithAllInvoice(Element eleGetOrderInvoiceDetails,YFSEnvironment env ) throws Exception {

	String strOrderno = XPathUtil.getString(eleGetOrderInvoiceDetails.getOwnerDocument(), "OrderInvoiceList/OrderInvoice/@OrderNo");
	Document docOrderListInput = XMLUtil.createDocument("Order");
	Element eleOrder = docOrderListInput.getDocumentElement();
	eleOrder.setAttribute("DocumentType", AcademyConstants.SALES_DOCUMENT_TYPE);
	eleOrder.setAttribute("EnterpriseCode", AcademyConstants.ENTERPRISE_CODE_SHIPMENT);
	eleOrder.setAttribute("OrderNo",strOrderno );
	env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST,"global/template/api/getOrderListTemplateForCLSReturn.xml");
	
	log.verbose("*************** Calling getOrderList API with input************ "+ XMLUtil.getXMLString(docOrderListInput));
	Document docOrderListOutput = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_ORDER_LIST, docOrderListInput);
	log.verbose("*************** Output of getOrderList ************ "+ XMLUtil.getXMLString(docOrderListOutput));
	return docOrderListOutput;
}

/**
 * @throws NumberFormatException
 */
private boolean isQtyItemMismatchForRequestedInvoice() throws Exception {
	boolean bQtyItemMismatch = false;
	Iterator hmCLSItemListIterator = hmCLSItemList.entrySet().iterator();			
	while (hmCLSItemListIterator.hasNext()) {
		Entry thisEntry = (Entry) hmCLSItemListIterator.next();
		String strReqItemID  = (String)thisEntry.getKey();
		Double dbReqQtyVal = (Double)thisEntry.getValue();
		
		if (hmInvoiceNoItemList != null)
		{
			if(hmInvoiceNoItemList.containsKey(strReqItemID))
			{
				Double dbInvoicedQtyValue = hmInvoiceNoItemList.get(strReqItemID);
				if(dbReqQtyVal > dbInvoicedQtyValue)
				{
					bQtyItemMismatch =  true;
					return bQtyItemMismatch;
				}
			}else{				
				bQtyItemMismatch =  true;
				return bQtyItemMismatch;
			}
		}
	}
	return bQtyItemMismatch;
}	
/**
 * @param eleGetOrderInvoiceDetails
 * @throws YFSException
 */
private void prepareItemListHashMapForRequestedInvoice(Element eleGetOrderInvoiceDetails) throws YFSException {
	YFCDocument getOrderInvoiceListDoc = YFCDocument.getDocumentFor(eleGetOrderInvoiceDetails.getOwnerDocument());
	YFCElement orderInvoiceListElem = getOrderInvoiceListDoc.getDocumentElement();
	YFCElement orderInvoiceElem = orderInvoiceListElem.getChildElement(AcademyConstants.ELE_ORDER_INVOICE);
	if (orderInvoiceElem != null) {
		YFCElement lineDetailsElem = orderInvoiceElem.getChildElement(AcademyConstants.ELEM_LINE_DETAILS);
		if (lineDetailsElem != null) {
			YFCIterable<YFCElement> lineDetailItr = lineDetailsElem.getChildren(AcademyConstants.ELEM_LINE_DETAIL);
			hmInvoiceNoItemList = new HashMap<String, Double>();
			while (lineDetailItr.hasNext()) {
				YFCElement lineDetailElem = lineDetailItr.next();
				String itemID = lineDetailElem.getAttribute(AcademyConstants.ITEM_ID);
				Double dbQty = lineDetailElem.getDoubleAttribute("ShippedQty");
				if (!YFCObject.isVoid(itemID)) {
					hmInvoiceNoItemList.put(itemID, dbQty);
				}
			}
		}
	} else {
		log.verbose("Order Invoice details not available in Sterling System.");
		//throw new YFSException("Order Invoice Doesn't exist");
		YFSException e = new YFSException();
		e.setErrorCode("Invalid Invoice");
		e.setErrorDescription("Order Invoice Doesn't exist.");
		throw e;
	}
}


/**
 * @param inputDoc
 * @throws Exception
 */
	private void prepareCLSItemListHasMap(Document inputDoc, Document docOrderListOutput, Document outDocItemsList) throws Exception {
	
		String strIsItemConsidered;
		String strReturnItem = null;
		ArrayList<String> alConsumedItems = new ArrayList<String>();
		NodeList orderLineElemList = inputDoc.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINE);
		if (orderLineElemList != null) {
			hmCLSItemList = new HashMap<String, Double>();
			int noOfOrderLine = orderLineElemList.getLength();
			for (int lineItr = 0; lineItr < noOfOrderLine; lineItr++) {
				Element orderLineElem = (Element) orderLineElemList.item(lineItr);
				String itemID = orderLineElem.getAttribute(AcademyConstants.ITEM_ID);
				boolean isValidaItem = hmInvoiceNoItemList.containsKey(itemID);
				String strCLSQty = orderLineElem.getAttribute(AcademyConstants.ATTR_RETURN_QTY);
				Double dbCLSQty = Double.parseDouble(strCLSQty);
				// OMNI-81582 START
				/*
				 * isValidaItem=false is when CLS item mapped to invoice number in CLS.
				 * Step 01: check if CLS sent item is a child item of modle present in RO
				 * Step 02: if not present in Step01 check if child item is present in SO  
				 */
				if (!isValidaItem) {
					log.verbose(itemID + " :: is a child item");
					String strModel = XMLUtil.getAttributeFromXPath(outDocItemsList,
							AcademyConstants.XPATH_ITEM_MODLE_1 + itemID + AcademyConstants.XPATH_ITEM_MODLE_2);
					List<Node> eleGetItemList = XMLUtil.getElementListByXpath(outDocItemsList,AcademyConstants.XPATH_GETITEMLIST_ITEMS_1 + itemID +
							AcademyConstants.XPATH_GETITEMLIST_ITEMS_2);
					log.verbose("model id :: " + strModel);
					boolean foundChildITem = false;
					Iterator itrItems = eleGetItemList.iterator();
					while (itrItems.hasNext()) {
						Element eleItems = (Element) itrItems.next();
						String tempITem = eleItems.getAttribute(AcademyConstants.ATTR_ITEM_ID);
						NodeList nlReturnItems = XPathUtil.getNodeList(eleSalesOrderDetails,AcademyConstants.XPATH_RETURNABLE_ITEMS + tempITem +
								AcademyConstants.CLOSING_BACKET);
						if (!YFCCommon.isVoid(nlReturnItems) && nlReturnItems.getLength() > 0) {
							for (int jCount = 0; jCount < nlReturnItems.getLength(); jCount++) {
								Element eleReturnItem = (Element) nlReturnItems.item(jCount);
								strIsItemConsidered = eleReturnItem.getAttribute(AcademyConstants.ATR_ISITEMCCONSIDERED);
								strReturnItem = eleReturnItem.getAttribute(AcademyConstants.ATTR_ITEM_ID);
								String strtmpModel = XMLUtil.getAttributeFromXPath(outDocItemsList,
								AcademyConstants.XPATH_ITEM_MODLE_1+ strReturnItem +AcademyConstants.XPATH_ITEM_MODLE_2);
								if (YFCCommon.isVoid(strIsItemConsidered) && strtmpModel.equalsIgnoreCase(strModel)) {
									log.verbose("#### @before :::::\n " + XMLUtil.getElementXMLString(orderLineElem));
									orderLineElem.setAttribute(AcademyConstants.ITEM_ID, strReturnItem);
									log.verbose("#### @after :::::\n " + XMLUtil.getElementXMLString(orderLineElem));
									itemID = strReturnItem;
									eleReturnItem.setAttribute(AcademyConstants.ATR_ISITEMCCONSIDERED, AcademyConstants.STR_YES);
									foundChildITem = true;
									alConsumedItems.add(strReturnItem);
									break;
								}
								if (foundChildITem)
									break;
							}
						}
						if (foundChildITem)
							break;
					}

					if (!hmInvoiceNoItemList.containsKey(itemID)) {
						NodeList nlSOItems = XPathUtil.getNodeList(docOrderListOutput, AcademyConstants.XPATH_ORDERINVOCIE_SHIPMENTTYPE);
						if (!YFCCommon.isVoid(nlSOItems)) {
							for (int lCount = 0; lCount < nlSOItems.getLength(); lCount++) {
								Element eleOrderInvoiceList = (Element) nlSOItems.item(lCount);
								NodeList nlShipmentLines = eleOrderInvoiceList.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
								if (nlShipmentLines.getLength() > 0) {
									for (int mCount = 0; mCount < nlShipmentLines.getLength(); mCount++) {
										Element eleShipment = (Element) nlShipmentLines.item(mCount);
										strReturnItem = eleShipment.getAttribute(AcademyConstants.ATTR_ITEM_ID);
										List<Node> ltReturnalItemNode = XMLUtil.getElementListByXpath(docOrderListOutput,
												AcademyConstants.XPATH_SO_RETURNABLE_ITEMS_1 + dbCLSQty
														+ AcademyConstants.XPATH_SO_RETURNABLE_ITEMS_2 + strReturnItem + "']");
										String strtmpModel = XMLUtil.getAttributeFromXPath(outDocItemsList,
												AcademyConstants.XPATH_ITEM_MODLE_1 + strReturnItem +AcademyConstants.XPATH_ITEM_MODLE_2);
										if (!alConsumedItems.contains(strReturnItem) && ltReturnalItemNode.size() > 0
												&& strtmpModel.equalsIgnoreCase(strModel)) {
											log.verbose("#### @before :::::\n " + XMLUtil.getElementXMLString(orderLineElem));
											orderLineElem.setAttribute(AcademyConstants.ITEM_ID, strReturnItem);
											log.verbose("#### @after :::::\n " + XMLUtil.getElementXMLString(orderLineElem));
											itemID = strReturnItem;
											foundChildITem = true;
											alConsumedItems.add(strReturnItem);
											break;
										}
									}
								}
								if (foundChildITem)
									break;
							}
						}
					}
				}
				// OMNI-81582 END
				// END Child item validation
				hmCLSItemList.put(itemID, dbCLSQty);
			}
		}
	
	} 
	
// Begin : OMNI-42367
private Document returnCarrierUpdates(YFSEnvironment env, String strCodeType, String strOrganizationCode)
		throws Exception {
	
	log.verbose("AcademyProcessReturnMessageAPI.returnCarrierUpdates() :: Begin ");
	
	Document docCommonCodeListIn = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
	docCommonCodeListIn.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, strCodeType);
	docCommonCodeListIn.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE, strOrganizationCode);
	
	log.verbose("getCommonCodeList - input :: "+XMLUtil.getXMLString(docCommonCodeListIn));
	
	Document getCommonCodeListOPTempl = XMLUtil.getDocument("<CommonCode CodeValue='' CodeShortDescription=''/>");
	env.setApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST, getCommonCodeListOPTempl);

	Document docCommonCodeListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST,
			docCommonCodeListIn);
	env.clearApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST);
	
	if(null != docCommonCodeListOut) {
		log.verbose("getCommonCodeList - output :: "+XMLUtil.getXMLString(docCommonCodeListOut));
	}
	
	log.verbose("AcademyProcessReturnMessageAPI.returnCarrierUpdates() :: End ");
	
	return docCommonCodeListOut;

}
// End : OMNI-42367
/***
 * This method invoke getItemList 
 * return item and modle details
 * @param env
 * @param docOrderListOutput
 * @param eleReturnOrders
 * @param inDocCLS
 * @throws Exception
 */
	private Document getChildItemsList(YFSEnvironment env, Document docOrderListOutput, Element eleReturnOrders, Document inDocCLS) throws Exception {
		log.beginTimer("# START AcademyProcessReturnMessageAPI.processReturnMessage.childItemValidation #");
		ArrayList<String> alItems = new ArrayList<String>();
		NodeList nlOrderInvoiceList = XPathUtil.getNodeList(docOrderListOutput, AcademyConstants.XPATH_ORDERINVOCIE_SHIPMENTTYPE);
		String strInvoiceNo;
		log.verbose(" start loopin on shipment items :: alItems");
		if (!YFCCommon.isVoid(nlOrderInvoiceList)) {
			for (int lCount = 0; lCount < nlOrderInvoiceList.getLength(); lCount++) {
				Element eleOrderInvoiceList = (Element) nlOrderInvoiceList.item(lCount);
				strInvoiceNo = eleOrderInvoiceList.getAttribute(AcademyConstants.ATTR_INVOICE_NO);
				if (!strInvoiceNo.equalsIgnoreCase(salesInvoiceNo)) {
					log.verbose(" ShipmentLine :: " + eleOrderInvoiceList);
					NodeList nlShipmentLines = eleOrderInvoiceList.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
					if (nlShipmentLines.getLength() > 0) {
						for (int mCount = 0; mCount < nlShipmentLines.getLength(); mCount++) {
							Element eleShipment = (Element) nlShipmentLines.item(mCount);
							alItems.add(eleShipment.getAttribute(AcademyConstants.ITEM_ID));
						}
					}
				}
			}
		}
		log.verbose(" alItems " + alItems.toString());
		log.verbose(" end loopin on shipment items :: alItems");
		log.verbose(" start loopin on CLS items :: alItems");
		NodeList nlCLSOrderLine = XPathUtil.getNodeList(inDocCLS, AcademyConstants.XPATH_CLS_ORDERLINE);
		for (int lCount = 0; lCount < nlCLSOrderLine.getLength(); lCount++) {
			Element eleCLSItem = (Element) nlCLSOrderLine.item(lCount);
			alItems.add(eleCLSItem.getAttribute(AcademyConstants.ITEM_ID));
		}
		log.verbose(" alItems " + alItems.toString());
		log.verbose(" end loopin on CLS items :: alItems");
		log.verbose(" start loopin on Invoice items :: alItems");
		hmInvoiceNoItemList.entrySet().stream().forEach(key -> alItems.add(key.getKey()));
		log.verbose(" alItems " + alItems.toString());
		log.verbose(" end loopin on Invoice items :: alItems");
		YFCDocument inDoc = YFCDocument.createDocument(AcademyConstants.ELEM_ITEM);
		YFCElement inEle = inDoc.getDocumentElement();
		inEle.setAttribute(AcademyConstants.ORGANIZATION_CODE,AcademyConstants.CONST_DEFAULT);
		inEle.setAttribute(AcademyConstants.ATTR_UOM,AcademyConstants.UOM_EACH_VAL);
		YFCElement eleComplexQuery = inEle.createChild(AcademyConstants.COMPLEX_QRY_ELEMENT);
		YFCElement eleOr = eleComplexQuery.createChild(AcademyConstants.COMPLEX_OR_ELEMENT);
		alItems.stream().forEach(str -> {
			YFCElement eleExp = eleOr.createChild(AcademyConstants.COMPLEX_EXP_ELEMENT);
			eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_ITEM_ID);
			eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
			eleExp.setAttribute(AcademyConstants.ATTR_VALUE, str);
		});
	
		YFCDocument outDocTemplate = YFCDocument.createDocument(AcademyConstants.ELE_ITEM_LIST);
		YFCElement outEleTemp = outDocTemplate.getDocumentElement();
		YFCElement outEleTempITem = outEleTemp.createChild(AcademyConstants.ELEM_ITEM);
		outEleTempITem.setAttribute(AcademyConstants.ATTR_ITEM_ID, AcademyConstants.STR_EMPTY_STRING);
		YFCElement outEleClassFCode = outEleTempITem.createChild(AcademyConstants.STR_CLASSIFICATIONCODES);
		outEleClassFCode.setAttribute(AcademyConstants.ATR_MODEL, AcademyConstants.STR_EMPTY_STRING);
		log.verbose("#### getItemList Input Document is  :::::\n " + XMLUtil.getXMLString(inDoc.getDocument()));
		log.verbose("#### getItemList Output Template is :::::\n " + XMLUtil.getXMLString(outDocTemplate.getDocument()));
		env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, outDocTemplate.getDocument());
		Document outDocItemsList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST, inDoc.getDocument());
		log.verbose("#### getItemList Output Document is :::::\n " + XMLUtil.getElementXMLString(outDocItemsList.getDocumentElement()));
		env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
		log.endTimer("# END AcademyProcessReturnMessageAPI.processReturnMessage.childItemValidation #");
		return outDocItemsList;
	}

}
