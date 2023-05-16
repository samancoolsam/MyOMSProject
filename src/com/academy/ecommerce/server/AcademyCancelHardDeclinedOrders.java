package com.academy.ecommerce.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @author C0014737
 * 
 * This agent is used for auto cancel of orders and shipments which have been declined
 */

public class AcademyCancelHardDeclinedOrders extends YCPBaseTaskAgent {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyCancelHardDeclinedOrders.class);	


	private static List<String> listOrderLineStatus = null;
	private static List<String> listShipmentStatus = null;
	private static List<String> listSTSOrderStatus = null; //OMNI-99049

	static {
		log.verbose("Begin of AcademyCancelHardDeclinedOrders Static block ...");		
		//Start : KER-15763: Line item not cancelled automatically after auth expiry
		
		listOrderLineStatus = Arrays.asList(YFSSystem.getProperty(AcademyConstants.PROP_PAYZ_CANCEL_ORDERLINE_STATUS).split(AcademyConstants.STR_COMMA));
		listShipmentStatus = Arrays.asList(YFSSystem.getProperty(AcademyConstants.PROP_PAYZ_CANCEL_SHIPMENT_STATUS).split(AcademyConstants.STR_COMMA));
		
		//End : KER-15763: Line item not cancelled automatically after auth expiry
		listSTSOrderStatus = Arrays.asList(("1100,2160.00.01,2160,2060").split(AcademyConstants.STR_COMMA)); //OMNI-99049
		log.verbose("End of AcademyCancelHardDeclinedOrders Static block ...");
	}
	/**
	 * It stores the ShipmentNo for which changeShipment would be called
	 * 
	 */
	private  HashMap<String,Document> hmpChangeShipment = null;


	/* 
	 * This method invokes getCompleteOrderDetails to get the order and shipment details 
	 * and cancels the order and shipment if eligible for cancel.
	 * This method is to be invoked only for orders which have a Payment as Hard Declined.
	 * 
	 */
	public Document executeTask(YFSEnvironment env, Document docInput) throws Exception {
		log.verbose("Begin of AcademyCancelHardDeclinedOrders.executeTask() method");
		log.verbose(" Input XML \n"+XMLUtil.getXMLString(docInput));
		
		Document docCompleteOrderDetailsOut = null;
		Document docChangeOrderOut = null;
		Element eleOrderOut = null;		
		Element eleOrderLines = null;		
		Element eleOrderLine = null;
		Element eleShipmentLines = null;		
		Element eleShipmentLine = null;
		Element eleShipment = null;

		NodeList nlOrderLines = null;
		NodeList nlShipmentLines = null;

		double dCurrentQuantity = 0.0;
		double dNewLineQuantity = 0.0;

		Element eleInput = docInput.getDocumentElement();
		String strDataKey =  eleInput.getAttribute(AcademyConstants.ATTR_DATA_KEY);
		String strTaskQueueKey = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_TASK_Q_KEY);
		 
		Document docChangeOrderInp = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		Element eleChangeOrderInp = docChangeOrderInp.getDocumentElement();

		eleChangeOrderInp.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strDataKey);
		Element eleChangeOrderLines = docChangeOrderInp.createElement(AcademyConstants.ELE_ORDER_LINES);
		eleChangeOrderInp.appendChild(eleChangeOrderLines);
		//OMNI-99049 - Start
		Document docCancellationMsg = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		Element eleCancellationMsg = docCancellationMsg.getDocumentElement();

		Element eleCancellationLines = docCancellationMsg.createElement(AcademyConstants.ELE_ORDER_LINES);
		eleCancellationMsg.appendChild(eleCancellationLines);
		//OMNI-99049 - End
		docCompleteOrderDetailsOut = callGetCompleteOrderDetails(env, docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_KEY));

		log.verbose("docCompleteOrderDetailsOut XML"+XMLUtil.getXMLString(docCompleteOrderDetailsOut));

		eleOrderOut = docCompleteOrderDetailsOut.getDocumentElement();
		eleOrderLines = (Element) eleOrderOut.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINES).item(0);

		Element eleTransactionFilters = (Element) docInput.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_FILTERS).item(0);
		String strPendingCancelAlert = eleTransactionFilters.getAttribute(AcademyConstants.STR_RAISE_PENDING_CANCEL_ALERT);
		//Start OMNI-6397
		//listBopisShipmentStatus = Arrays.asList(eleTransactionFilters.getAttribute(AcademyConstants.STR_BOPIS_CANCELLED_STATUS).split(AcademyConstants.STR_COMMA));
		//Start OMNI-6397

		//Check if Order has any lines to be cancelled
		String strMinOrderStatus = eleOrderOut.getAttribute(AcademyConstants.ATTR_MIN_ORDER_STATUS);
		if(strMinOrderStatus.contains("."))
			strMinOrderStatus = strMinOrderStatus.substring(0,4);

		if(Integer.parseInt(strMinOrderStatus) < 3700) {
			log.verbose(" Order contains a non-shipped or cancelled line");

			if(!YFSObject.isVoid(eleOrderLines)){
				
				//OMNI-94174-Exclude free gifts from Hard declined agent - START 
				nlOrderLines = XPathUtil.getNodeList(docCompleteOrderDetailsOut, AcademyConstants.XPATH_EXTNISPROMOITEM);
				//OMNI-94174-END
				boolean bIsDSVLinesPresentOnOrder = false;
				
				log.verbose("OrderLine Count :: "+nlOrderLines.getLength());
				log.verbose("Processing OrderNo :: "+eleOrderOut.getAttribute(AcademyConstants.ATTR_ORDER_NO));
				hmpChangeShipment = new HashMap<String, Document>();

				for (int iOrderLineCount = 0; iOrderLineCount < nlOrderLines.getLength(); iOrderLineCount++) {
					eleOrderLine = (Element) nlOrderLines.item(iOrderLineCount);
					dCurrentQuantity = 0;
					dNewLineQuantity = 0;
					boolean bIsFireArm = false;
					
					String strOrderedQuantiy = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
					String strOrderLineKey = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
					dCurrentQuantity = Double.parseDouble(strOrderedQuantiy);
					dNewLineQuantity = dCurrentQuantity;
					
					//OMNI-99049 - Start
					String strFulfillmentType = eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
					String strPackListType = eleOrderLine.getAttribute(AcademyConstants.STR_PACK_LIST_TYPE);

					if (!YFCObject.isVoid(strFulfillmentType) && "STS".equals(strFulfillmentType)
							&& (!YFCObject.isVoid(strPackListType) && "FA".equals(strPackListType))) {
						bIsFireArm = true;
					}
					log.verbose("bIsFireArm::"+bIsFireArm);
					//OMNI-99049 - End
					
					//Check of any eligible shipments to be cancelled.
					eleShipmentLines = (Element) eleOrderLine.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES).item(0);
					if(!YFCObject.isVoid(eleShipmentLines)){							
						nlShipmentLines = eleShipmentLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
						log.verbose("ShipmentLine Count :: "+nlShipmentLines.getLength());
						double dShipmentLineQty;
						for (int iShipmenLineCount = 0; iShipmenLineCount < nlShipmentLines.getLength(); iShipmenLineCount++) {
							eleShipmentLine = (Element) nlShipmentLines.item(iShipmenLineCount);
							//Begin: OMNI-11359
							Element eleShpment = (Element) eleShipmentLine.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
							
							if(!YFCObject.isVoid(eleShpment)){
								
							String strShipmentType = eleShpment.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
							String strDocumentType = eleShpment.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
							
							if(!(AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE.equals(strDocumentType)
									&& AcademyConstants.STR_SHIP_TO_STORE.equals(strShipmentType))) {
							//End: OMNI-11359	
							String strShipmentLineKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);

							dShipmentLineQty = Double.parseDouble(eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY));
							eleShipment = (Element) eleShipmentLine.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);

							String strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
							String strStatus = eleShipment.getAttribute(AcademyConstants.ATTR_STATUS);
							String strManifestKey = eleShipment.getAttribute(AcademyConstants.ATTR_MANIFEST_KEY);
							
							log.verbose("ShipmentLineKey :: "+eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
							log.verbose("strManifestKey :: "+strManifestKey);
							
							//Code Changes for OMNI-78996
							String strContainerNo=SCXmlUtil.getXpathAttribute(eleShipment, "Containers/Container/@ContainerNo");
							//Code Changes for OMNI-78996
							
							if (dShipmentLineQty > 0 && !listShipmentStatus.contains(strStatus)
									&& YFCObject.isVoid(strManifestKey) && YFCCommon.isVoid(strContainerNo)) {
								log.verbose("Shipment Line is eligible for cancellation :: ");
								// Exception handling of a scenario where we have new status on Shipment after
								// Shipped Status.
								if (dShipmentLineQty > 0 && Integer.parseInt(strStatus.substring(0, 4)) > 1400) {
									log.verbose(
											"Shipment has a status more than Shipped. So ignoring line strStatus :: "
													+ strStatus);
								} else {
									//OMNI-99049
									if (!(bIsFireArm)) {
										// Changes for OMNI-88894 Error YFS10276 Start
										// Update Shiment Line in map with shipment details to be cancelled.
										addToChangeShipmentMap(strShipmentKey, strShipmentLineKey, strStatus);
										// Since Shipment in cancelled. Reduce corresponding Orderline qty to be
										// cancelled
									}
									dNewLineQuantity = dNewLineQuantity - dShipmentLineQty;
								} // Changes for OMNI-88894 Error YFS10276 Start
							} else if (dShipmentLineQty > 0
									&& (AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS.equals(strStatus)
											|| AcademyConstants.STR_PAPER_WORK_INITIATED_STATUS.equals(strStatus))
									&& YFCObject.isVoid(strManifestKey) && YFCCommon.isVoid(strContainerNo)
									&& bIsFireArm) {
								dNewLineQuantity = dNewLineQuantity - dShipmentLineQty;
							}
							//OMNI-81732 Start
								else if(dShipmentLineQty > 0 && ((listShipmentStatus.contains(strStatus)
										&& Integer.parseInt(strStatus.substring(0,4)) < 1400) || !YFCObject.isVoid(strManifestKey))) {
							//OMNI-81732 End
								log.verbose("Shipment Line contains Packed lines :: ");
								//Raise Alert on Shipment if it contains any Packed Lines
								if(!YFCObject.isVoid(strPendingCancelAlert) && strPendingCancelAlert.equals(AcademyConstants.STR_YES)){
									AcademyUtil.invokeService(env, AcademyConstants.SERV_ACAD_RAISE_PENDING_CANCEL_ON_DECLINE_ALERT, 
											XMLUtil.getDocumentForElement(eleShipment));
								}
							}
							
							//Begin: OMNI-11359
							}
							}
							//End: OMNI-11359
						}
					}

					//Check for Order line status to be cancelled.
					Element eleOrderStatuses = (Element) eleOrderLine.getElementsByTagName("OrderStatuses").item(0);
					if(!YFCObject.isVoid(eleOrderStatuses)){							
						NodeList nlOrderStatus = eleOrderStatuses.getElementsByTagName(AcademyConstants.ELE_ORDER_STATUS);
						log.verbose("nlOrderStatus Count :: "+nlOrderStatus.getLength());
											
						for (int iOrderStatus = 0; iOrderStatus < nlOrderStatus.getLength(); iOrderStatus++) {
							Element elOrderStatus = (Element) nlOrderStatus.item(iOrderStatus);
							log.verbose("elOrderStatus XML"+XMLUtil.getElementXMLString(elOrderStatus));
							String strStatus = elOrderStatus.getAttribute(AcademyConstants.ATTR_STATUS);
							String strStatusQty = elOrderStatus.getAttribute(AcademyConstants.ATTR_STAT_QTY);

							double dStatusQty = Double.parseDouble(strStatusQty);
							log.verbose("Order line Qty is eligible for cancellation :: "+strStatus);
							log.verbose("OrderLine before :: dNewLineQuantity :: "+ dNewLineQuantity +" before :: dStatusQty :: " +dStatusQty);
							
							//OMNI-81732 Start
							if(dStatusQty > 0 && !listOrderLineStatus.contains(strStatus) && Integer.parseInt(strStatus.substring(0,4)) < 3700) {
							//OMNI-81732 End
								log.verbose("Order line Qty is eligible for cancellation :: "+strStatus);
								//Reduce quantity from order line and calculate new order qty (exclude incude in shipment)
								if(!strStatus.startsWith("3350"))								
									dNewLineQuantity = dNewLineQuantity - dStatusQty;
							}
							else if(dStatusQty > 0 && listShipmentStatus.contains(strStatus)
									&& Integer.parseInt(strStatus.substring(0,4)) < 3350 ) {
								log.verbose("Order contains Drop Ship Vendor Lines :: ");
								bIsDSVLinesPresentOnOrder = true;
							}
						}					
					}

					log.verbose(" dCurrentQuantity :: "+ dCurrentQuantity + "dNewLineQuantity :: "+dNewLineQuantity);
					//Check if any lines on order to be cancelled and update changeOrder Input
					if(dCurrentQuantity > dNewLineQuantity){
						// Line nhad some qty to be cancelled
						String strMinLineStatus = eleOrderLine.getAttribute(AcademyConstants.ATTR_MIN_LINE_STATUS);
						log.verbose("OrderLineKey being cancelled"+ eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
						if (bIsFireArm && (!listSTSOrderStatus.contains(strMinLineStatus))) {
							prepareCancellationMsgForGSM(docCancellationMsg, eleCancellationLines, eleOrderLine);
						} else {
							prepareChangeOrderInp(docChangeOrderInp, eleChangeOrderLines, strOrderLineKey,
									Double.toString(dNewLineQuantity));
						}
					}
					else {
						log.verbose(" No qty on line eligible for cancellation ");
					}
				}
				
				if(bIsDSVLinesPresentOnOrder)
				//Raise Alert on Order if it contains any DSV Line
				if(!YFCObject.isVoid(strPendingCancelAlert) && strPendingCancelAlert.equals(AcademyConstants.STR_YES)){
					AcademyUtil.invokeService(env, AcademyConstants.SERV_ACAD_RAISE_PENDING_CANCEL_ON_DECLINE_ALERT,docCompleteOrderDetailsOut);
				}
				//OMNI-99049 - Start
				NodeList nlChangeOrderLines = XPathUtil.getNodeList(docChangeOrderInp.getDocumentElement(), "/Order/OrderLines/OrderLine");
				if(nlChangeOrderLines.getLength()>0) {
					invokeChangeShipment(env);
					docChangeOrderOut = cancelDeclinedOrder(env,docChangeOrderInp);
				}	
				NodeList nlCancellationMsgLines = XPathUtil.getNodeList(docCancellationMsg.getDocumentElement(),
						"/Order/OrderLines/OrderLine");
				if (nlCancellationMsgLines.getLength() > 0) {
					postCancellationMsgtoGSM(env,docCancellationMsg, docCompleteOrderDetailsOut);
				}
				//OMNI-99049 - Start
			}

		} else {
			log.verbose(" Order is completely shipped or cancelled. Ignore Order.");
		}
		
		//Remove Entry from task Q table since its processed successfully.
		removeTaskQueueRecord(env, strTaskQueueKey);
		log.verbose("End of AcademyCancelHardDeclinedOrders.executeJob() method ::");
		return docChangeOrderOut;
	}


	/** This method posts cancellation message to GSM
	 * <Order OrderNo="" ReasonCode="AUTH_FAILURE">
    	<OrderLines>
        	<OrderLine CancelledQuantity="" PrimeLineNo="" SubLineNo="1">
            <Item
                ItemDesc="" ItemID="" ProductClass=""/>
        	</OrderLine>
    	</OrderLines>
    	<PersonInfoBillTo DayPhone="" FirstName="" LastName=""/>
		</Order>
	 * @param env
	 * @param docCancellationMsg
	 * @param docCompleteOrderDetailsOut
	 * @return
	 * @throws Exception
	 */
	private void postCancellationMsgtoGSM(YFSEnvironment env, Document docCancellationMsg,
			Document docCompleteOrderDetailsOut) throws Exception {
		log.verbose("Begin of AcademyCancelHardDeclinedOrders.postCancellationMsgtoGSM() method");
		Element eleCompleteOrderDetails = docCompleteOrderDetailsOut.getDocumentElement();
		String strOrderHeaderKey = eleCompleteOrderDetails.getAttribute(AcademyConstants.STR_ORDR_HDR_KEY);
		Element eleOrder = null;
		Element elePersonInfoBillToXML = docCancellationMsg.createElement(AcademyConstants.ELE_PERSON_INFO_BILL_TO);
		Element elePersonInfoBillTo = (Element) eleCompleteOrderDetails.getElementsByTagName(AcademyConstants.ELE_PERSON_INFO_BILL_TO).item(0);

		eleOrder = docCancellationMsg.getDocumentElement();
		eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO,eleCompleteOrderDetails.getAttribute(AcademyConstants.ATTR_ORDER_NO));
		eleOrder.setAttribute(AcademyConstants.ATTR_REASON_CODE, AcademyConstants.ATTR_AUTH_FAILURE);

		elePersonInfoBillToXML.setAttribute(AcademyConstants.ATTR_DAY_PHONE, elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_DAY_PHONE));
		elePersonInfoBillToXML.setAttribute(AcademyConstants.ATTR_FNAME, elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_FNAME));
		elePersonInfoBillToXML.setAttribute(AcademyConstants.ATTR_LNAME, elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_LNAME));
		eleOrder.appendChild(elePersonInfoBillToXML);

		log.verbose("docCancellationMsg XML :: " + XMLUtil.getXMLString(docCancellationMsg));
		AcademyUtil.invokeService(env, AcademyConstants.SERV_ACADEMY_PUBLISH_CUST_INFO_TO_GSM, docCancellationMsg);
		updateNotesOnOrder(env,strOrderHeaderKey);

		log.verbose("End of AcademyCancelHardDeclinedOrders.postCancellationMsgtoGSM() method");

	}


	private void updateNotesOnOrder(YFSEnvironment env, String strOrderHeaderKey) throws Exception {
		log.verbose("Begin of AcademyCancelHardDeclinedOrders.updateNotesOnOrder() method");
		Document docChangeOrder = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		Element eleOrder = docChangeOrder.getDocumentElement();
		eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
		eleOrder.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);
		Element eleNotes = docChangeOrder.createElement(AcademyConstants.ELE_NOTES);
		Element eleNote = docChangeOrder.createElement(AcademyConstants.ELE_NOTE);
		eleNote.setAttribute(AcademyConstants.ATTR_NOTE_TEXT, AcademyConstants.STR_NOTES_GSM);

		eleNotes.appendChild(eleNote);
		eleOrder.appendChild(eleNotes);
		if (!YFCObject.isVoid(docChangeOrder)) {
			log.verbose("changeOrder input :: " + XMLUtil.getXMLString(docChangeOrder));
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, docChangeOrder);
		}
		log.verbose("End of AcademyCancelHardDeclinedOrders.updateNotesOnOrder() method");
	}


	private void prepareCancellationMsgForGSM(Document docCancellationMsg, Element eleCancellationLines,
			Element eleOrderLine) {
		log.verbose("Begin of AcademyCancelHardDeclinedOrders.prepareCancellationMsgForGSM() method");

		Element eleOrderLineXML = docCancellationMsg.createElement(AcademyConstants.ELEM_ORDER_LINE);
		eleOrderLineXML.setAttribute(AcademyConstants.ATTR_CANCELLED_QUANTITY, eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY));
		eleOrderLineXML.setAttribute(AcademyConstants.ATTR_PRIME_LINE_NO, eleOrderLine.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO));
		eleOrderLineXML.setAttribute(AcademyConstants.SUB_LINE_NO, eleOrderLine.getAttribute(AcademyConstants.SUB_LINE_NO));
		eleCancellationLines.appendChild(eleOrderLineXML);

		Element eleItemXML = docCancellationMsg.createElement(AcademyConstants.ELEM_ITEM);
		Element eleItem = (Element) eleOrderLine.getElementsByTagName(AcademyConstants.ITEM).item(0);
		eleItemXML.setAttribute(AcademyConstants.ATTR_ITEM_DESC, eleItem.getAttribute(AcademyConstants.ATTR_ITEM_DESC));
		eleItemXML.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleItem.getAttribute(AcademyConstants.ITEM_ID));
		eleItemXML.setAttribute(AcademyConstants.ATTR_PROD_CLASS, eleItem.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
		eleOrderLineXML.appendChild(eleItemXML);
		
		log.verbose("End of AcademyCancelHardDeclinedOrders.prepareCancellationMsgForGSM() method");
	}

	/** Add changeShipment input to has map for each shipment to avoid multiple api call.
	 * @param eleShipment
	 * @throws ParserConfigurationException
	 */
	private void addToChangeShipmentMap(String strShipmentKey, String strShipmentLineKey, String strStatus) throws Exception {
		log.verbose("Begin of AcademyCancelHardDeclinedOrders.addToChangeShipmentMap() method");

		Document docChangeShipmentInput = null;
		Element eleShipmentLines = null;
		if(hmpChangeShipment.containsKey(strShipmentKey)){
			docChangeShipmentInput = hmpChangeShipment.get(strShipmentKey);
			eleShipmentLines =XMLUtil.getElementByXPath(docChangeShipmentInput,"/Shipment/ShipmentLines");
		} else {
			docChangeShipmentInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			docChangeShipmentInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			//Start Code Changes for OMNI-78996
			docChangeShipmentInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_OVERRIDE_MODIFICATION_RULES, AcademyConstants.STR_YES);
			//End Code Changes for OMNI-78996
			
			//OMNI-99043:Start
				docChangeShipmentInput.getDocumentElement().setAttribute("CancelShipmentOnZeroTotalQuantity", AcademyConstants.STR_YES);
				docChangeShipmentInput.getDocumentElement().setAttribute("Action", AcademyConstants.VAL_CANCEL);
				log.verbose("Removed listBopisShipmentStatus if condition check");
				//OMNI-99043:END
			
			eleShipmentLines = docChangeShipmentInput.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
			docChangeShipmentInput.getDocumentElement().appendChild(eleShipmentLines);

			Element eleExtn = docChangeShipmentInput.createElement(AcademyConstants.ELE_EXTN);
			eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_SHORTPICK_REASON_CODE, "CANCEL_BY_HARD_DECLINED_AGENT");
			docChangeShipmentInput.getDocumentElement().appendChild(eleExtn);
		}

		Element eleShipmentLine=docChangeShipmentInput.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
		eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, strShipmentLineKey);
		eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, "0");
		eleShipmentLines.appendChild(eleShipmentLine);

		hmpChangeShipment.put(strShipmentKey, docChangeShipmentInput);

		log.verbose("End of AcademyCancelHardDeclinedOrders.addToChangeShipmentMap() method");
	}

	private void invokeChangeShipment(YFSEnvironment env) {
		log.verbose("Begin of AcademyCancelHardDeclinedOrders.invokeChangeShipment() method");
		Document docInChangeShipment = null;
		String strShipmentKey = "";
		try{
			Iterator itChangeShipment = hmpChangeShipment.entrySet().iterator();
			while (itChangeShipment.hasNext()) {
				Map.Entry<String, Document> meChangeShipmentStatus = (Map.Entry<String, Document>)itChangeShipment.next();
				docInChangeShipment = (Document)meChangeShipmentStatus.getValue();
				strShipmentKey = meChangeShipmentStatus.getKey();

				log.verbose("Input to changeShipment API for ShipmentNo:" +strShipmentKey+" :: \n"+XMLUtil.getXMLString(docInChangeShipment));
				AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_SHIPMENT, docInChangeShipment);

			}
			log.verbose("End of AcademyCancelHardDeclinedOrders.invokeChangeShipment() method");

		} catch (Exception e) {
			log.info("Exception occured in AcademyCancelHardDeclinedOrders.invokeChangeShipment() method for ShipmentKey :" + strShipmentKey);
			log.verbose(e.getMessage());
			throw new YFSException(e.getMessage());
		}
	}


	/** This method executes getCompleteOrderDetails API to fetch OrderLine,ShipmentLine and Shipment information
	 * getCompleteOrderDetails o/p template
	 * <Order OrderNo="" OrderDate="" OrderHeaderKey="" MaxOrderStatus="" MinOrderStatus="" Status="">
		  <OrderLines>
			   <OrderLine OrderHeaderKey="" OrderLineKey="" MinLineStatus="" MaxLineStatus="" FulfillmentType="">
				   <OrderLineTranQuantity InvoicedQuantity="" ModificationQty="" OpenQty="" OrderedQty="" OriginalOrderedQty="" ReceivedQty="" RemainingQty="" SettledQuantity="" ShippedQuantity="" SplitQty="" StatusQuantity="" />
			       <ShipmentLines>
				       <ShipmentLine OrderHeaderKey="" OrderLineKey="" ShipmentKey="" ShipmentLineKey="" OrderNo="" 
			                  OriginalQuantity="" BackroomPickedQuantity="" ActualQuantity="" ShortageQty="" Quantity="" ShipNode="" ItemId="">
			              <Shipment ShipmentNo="" ManifestNo="" ShipmentKey="" Status="" ShipNode="" IsPackProcessComplete="" ShipmentType="" DocumentType="" Quantity=""/>     
				       </ShipmentLine>
			      </ShipmentLines>
			   </OrderLine>
		  </OrderLines>
		</Order>
	 * @param env
	 * @param strOrderNo
	 * @return
	 * @throws Exception
	 */
	private Document callGetCompleteOrderDetails(YFSEnvironment env, String strOrderHeaderKey) throws Exception {

		log.verbose("Begin of AcademyCancelHardDeclinedOrders.callGetCompleteOrderDetails() method");
		Document docGetCompleteOrderDetailsInput = null;
		Document docGetCompleteOrderDetailsOut = null;
		Element eleOrder = null;

		docGetCompleteOrderDetailsInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		eleOrder = docGetCompleteOrderDetailsInput.getDocumentElement();
		eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);

		log.verbose("Input of getCompleteOrderDetails :: "+ XMLUtil.getXMLString(docGetCompleteOrderDetailsInput));
		docGetCompleteOrderDetailsOut = AcademyUtil.invokeService(env, 
				AcademyConstants.SERV_ACAD_GET_HARD_DECLINED_ORDER_DETAILS_SERVICE, docGetCompleteOrderDetailsInput);
		log.verbose("Output of getCompleteOrderDetails ::"+ XMLUtil.getXMLString(docGetCompleteOrderDetailsOut));

		log.verbose("End of AcademyCancelHardDeclinedOrders.callGetCompleteOrderDetails() method");
		return docGetCompleteOrderDetailsOut;
	}

	/** This prepares OrderLine element for changeOrder Input
	 * <OrderLine Action="" OrderLineKey="201702150011522424720620" OrderedQty="0"/>
	 * @param docInXML
	 * @param newChangeOrderQty
	 * @return
	 */
	private Document prepareChangeOrderInp(Document docChangeOrderInp, Element eleOrderLines, String strOrderLineKey, String strQuantiy) {
		log.verbose("Begin of AcademyCancelHardDeclinedOrders.prepareChangeOrderInp() method");

		Element eleOrderLine = docChangeOrderInp.createElement(AcademyConstants.ELEM_ORDER_LINE);
		eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, strOrderLineKey);
		eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, strQuantiy);
		eleOrderLines.appendChild(eleOrderLine);

		log.verbose("End of AcademyCancelHardDeclinedOrders.prepareChangeOrderInp() method");
		return docChangeOrderInp;

	}
	/** This method executes changeOrder API to cancel the order line quantity
	 * Sample Input XML
	 * 	<Order DocumentType="0001" IgnoreOrdering="Y" ModificationReasonCode="CANCEL_BY_AGENT" OrderHeaderKey="201701231632532424450217" Override="Y" changeOrder="Y">
		    <OrderLines>
		        <OrderLine Action="" OrderLineKey="201701231632532424450218" OrderedQty="0"/>
				 <OrderLine Action="" OrderLineKey="201701231632532424450219" OrderedQty="2"/>
		    </OrderLines>
		</Order>
	 * @param env
	 * @param inDoc
	 * @return
	 */
	private Document cancelDeclinedOrder(YFSEnvironment env, Document docChangeOrderInp) {

		log.verbose("Begin of AcademyCancelHardDeclinedOrders.cancelDeclinedOrder() method");
		Document docOutChangeOrder = null;
		Element eleOrder = null;
		
		try {
			eleOrder = docChangeOrderInp.getDocumentElement();
			eleOrder.setAttribute(AcademyConstants.ATTR_IGNORE_ORDERING, AcademyConstants.ATTR_Y);
			eleOrder.setAttribute(AcademyConstants.ATTR_MOD_REASON_CODE, "CANCEL_BY_HARD_DECLINED_AGENT");
			eleOrder.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.ATTR_Y);
			eleOrder.setAttribute(AcademyConstants.ATTR_CHANGE_ORDER, AcademyConstants.ATTR_Y);			

			log.verbose("Input to changeOrder :: "+XMLUtil.getXMLString(docChangeOrderInp));
			docOutChangeOrder = AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_ORDER, docChangeOrderInp);
			log.verbose("Output of changeOrder ::"+ XMLUtil.getXMLString(docOutChangeOrder));
			log.verbose("End of AcademyCancelHardDeclinedOrders.cancelDeclinedOrder() method");

		} catch (Exception e) {
			log.info("Exception occured in AcademyCancelHardDeclinedOrders.cancelDeclinedOrder() method for Order :"
					+eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
			log.verbose(e.getMessage());
			throw new YFSException(e.getMessage());
		}
		log.verbose("End of AcademyCancelHardDeclinedOrders.cancelDeclinedOrder() method");
		return docOutChangeOrder;
	}
	
	/** This method executes manageTaskQueue API to remove processed line details
	 * Sample Input XML
	 * 	<TaskQueue Action="Delete" TaskQKey="2018090110050322699633" />
	 * @param env
	 * @param inDoc
	 * @return
	 */
	private void removeTaskQueueRecord(YFSEnvironment env, String strTaskQueueKey) {

		log.verbose("Begin of AcademyCancelHardDeclinedOrders.removeTaskQueueRecord() method");
		try {
			
			Document docManageTaskQueue = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
			docManageTaskQueue.getDocumentElement().setAttribute(AcademyConstants.ATTR_ACTION, 
					AcademyConstants.STR_OPERATION_VAL_DELETE);
			docManageTaskQueue.getDocumentElement().setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, strTaskQueueKey);
			
			
			log.verbose("Input to manageTaskQueue :: "+XMLUtil.getXMLString(docManageTaskQueue));
			AcademyUtil.invokeAPI(env,AcademyConstants.API_MANAGE_TASK_QUEUE, docManageTaskQueue);

		} catch (Exception e) {
			log.info("Exception occured in AcademyCancelHardDeclinedOrders.removeTaskQueueRecord()" +
					" method for strTaskQueueKey :" +strTaskQueueKey);
			log.verbose(e.getMessage());
			throw new YFSException(e.getMessage());
		}
		log.verbose("End of AcademyCancelHardDeclinedOrders.removeTaskQueueRecord() method");
	}

}//End of AcademyCancelHardDeclinedOrders class