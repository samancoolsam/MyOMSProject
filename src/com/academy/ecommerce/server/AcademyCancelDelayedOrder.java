package com.academy.ecommerce.server;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.format.DateTimeFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfc.log.YFCLogCategory;
import com.academy.util.common.AcademyBOPISUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;

/**
 * @author C0008473
 * Modified by ndey as part of WN-198
 * Modified by @Sanchit as a part of BOPIS 2023
 * This agent is used for auto cancel of orders and shipments which are older than 7 days. JIRA#STL-1725
 */
public class AcademyCancelDelayedOrder extends YCPBaseAgent {
	
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyCancelDelayedOrder.class);	
	
	
	/**This list holds the MinOrderStatus which need to be excluded
	 * 
	 */
	private static List<String> listMinOrderStatus = null;
	
	/**This list holds the MinLineStatus which need to be excluded
	 * 
	 */
	private static List<String> listMinLineStatus = null;
	
	/**This list holds the ShipmentStatus for which change shipment is needed
	 * 
	 */
	private static List<String> listShipmentStatus = null;
	
	/**This list holds the WMSShipmentStatus for which order has to be excluded
	 * 
	 */
	private static List<String> listWMSShipmentStatus = null;
	
	/**This list holds the DSVShipmentStatus for which order has to be excluded
	 * 
	 */
	private static List<String> listDSVShipmentStatus = null;
	
	/**This static block initializes respective status from customer_overrides.properties file
	 * 
	 */
	static {
		log.verbose("Begin of AcademyCancelDelayedOrder Static block ...");		

		listMinOrderStatus = Arrays.asList(YFSSystem.getProperty(AcademyConstants.STR_EXCLUDE_MIN_ORDER_STATUS).split(AcademyConstants.STR_COMMA));
		listMinLineStatus = Arrays.asList(YFSSystem.getProperty(AcademyConstants.STR_EXCLUDE_MIN_LINE_STATUS).split(AcademyConstants.STR_COMMA));
		listShipmentStatus = Arrays.asList(YFSSystem.getProperty(AcademyConstants.STR_DELAY_SHIPMENT_STATUS).split(AcademyConstants.STR_COMMA));
		listWMSShipmentStatus = Arrays.asList(YFSSystem.getProperty(AcademyConstants.STR_EXCLUDE_WMS_DC_SHIPMENT_STATUS).split(AcademyConstants.STR_COMMA));
		listDSVShipmentStatus = Arrays.asList(YFSSystem.getProperty(AcademyConstants.STR_EXCLUDE_DSV_SHIPMENT_STATUS).split(AcademyConstants.STR_COMMA));
		
		log.verbose("End of AcademyCancelDelayedOrder Static block ...");
        }

	/**This string holds the complex query element for getOrderList
	 * 
	 */
	private static String strOrderListComplexQry = "<ComplexQuery Operation=\"AND\">"
		+ "<Or>"
		+ "<Exp Name=\"PaymentStatus\" QryType=\"EQ\" Value=\"AUTHORIZED\"/>"
		+ "<Exp Name=\"PaymentStatus\" QryType=\"EQ\" Value=\"AWAIT_AUTH\"/>"
		+ "<Exp Name=\"PaymentStatus\" QryType=\"EQ\" Value=\"AWAIT_PAY_INFO\"/>"
		+ "<Exp Name=\"PaymentStatus\" QryType=\"EQ\" Value=\"NOT_APPLICABLE\"/>"
		+ "</Or>" + "</ComplexQuery>";
	
	private static String strChangeShipmentTemplate = "<Shipment SellerOrganizationCode='' ShipNode='' ShipmentKey='' ShipmentNo='' Status=''/>";
	
	/**
	 * This is used for avoiding consecutive trigger of getJob() method
	 */
	private static int  iGetOrderListCount  = 0;
	
	/**It stores the ShipmentNo for which changeShipment and changeShipmentStatus call would be skipped
	 * 
	 */
	private List<String> listSkipShipment = null;
	
	private  HashMap<String,Document> hmpChangeShipmentStatus = null;
	
	/* This method fetches eligible orders for cancellation
	 * (non-Javadoc)
	 * @see com.yantra.ycp.japi.util.YCPBaseAgent#getJobs(com.yantra.yfs.japi.YFSEnvironment, org.w3c.dom.Document)
	 * 
	 */
	public List<Document> getJobs(YFSEnvironment env, Document inXML) throws Exception {

		log.verbose("Begin of AcademyCancelDelayedOrder.getJobs() method");
		List<Document> docOrderListOut = new ArrayList<Document>();
		Document docGetOrderListOut = null;
		Element eleCurrentOrder = null;
		String strNumRecordsToBuffer = null;
		String strDelayedReleaseToDate = null; //WN-802 Hot Market - Delayed Release
			
		Element eleXMLRoot = inXML.getDocumentElement();

		String strFromDateRange = eleXMLRoot.getAttribute(AcademyConstants.STR_FROM_DATE_RANGE);
		String strToDateRange = eleXMLRoot.getAttribute(AcademyConstants.STR_TO_DATE_RANGE);
		
		//WN-920 Hot Market - Ignore cancel agent
		strDelayedReleaseToDate = eleXMLRoot.getAttribute(AcademyConstants.STR_DELAYED_RELEASE_TO_DATE_RANGE);
				
		strNumRecordsToBuffer = eleXMLRoot.getAttribute(AcademyConstants.ATTR_NUM_RECORDS);
		int iNumRecordsToBuffer = Integer.parseInt(strNumRecordsToBuffer);
		log.verbose("iNumRecordsToBuffer ::"+iNumRecordsToBuffer);
		log.verbose("iGetOrderListCount ::"+iGetOrderListCount);
		log.verbose("eleXMLRoot.hasChildNodes() ::"+eleXMLRoot.hasChildNodes());
		
		if((eleXMLRoot.hasChildNodes() && (iNumRecordsToBuffer > iGetOrderListCount))){
			log.verbose("Not invoking any API as all jobs are already processed ...");
			return null;
		}
		
		Calendar cal = Calendar.getInstance();
		Calendar cal1 = Calendar.getInstance();

		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_SIMPLE_DATE_PATTERN);//"yyyy-MM-dd"

		cal.add(Calendar.DATE, (-Integer.parseInt(strFromDateRange)));
		String strFromOrderDate = sdf.format(cal.getTime());//oldest date		

		cal1.add(Calendar.DATE, (-Integer.parseInt(strToDateRange)));
		String strToOrderDate = sdf.format(cal1.getTime());//earliest date

		docGetOrderListOut = callGetOrderList(env, strFromOrderDate,strToOrderDate);
		log.verbose("docGetOrderListOut XML"+XMLUtil.getXMLString(docGetOrderListOut));

		NodeList nlOrderList = docGetOrderListOut.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_ORDER);
		iGetOrderListCount = nlOrderList.getLength();
		log.verbose("nlOrderList_Length:"+iGetOrderListCount);
				
		for (int iOrderCount = 0; iOrderCount < iGetOrderListCount; iOrderCount++) {
			eleCurrentOrder = (Element) nlOrderList.item(iOrderCount);
			//Skip Orders with MinOrderStatus in listMinOrderStatus or APPEASEMENT orders
			if(!listMinOrderStatus.contains(eleCurrentOrder.getAttribute(AcademyConstants.ATTR_MIN_ORDER_STATUS))
					&& !eleCurrentOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO).startsWith(AcademyConstants.STR_YES)){
				
				//WN-920 Hot Market - Ignore cancel agent
				eleCurrentOrder.setAttribute(AcademyConstants.ATTR_ELIGIBLE_DAYS_TO_CANCEL_DELAYED_RELEASE, strDelayedReleaseToDate);
				
				docOrderListOut.add(XMLUtil.getDocumentForElement(eleCurrentOrder));
			}
		}
		
			
		log.verbose("End of AcademyCancelDelayedOrder.getJobs() method");

		return docOrderListOut;
	}

	/* This method cancels the eligible orders
	 * (non-Javadoc)
	 * @see com.yantra.ycp.japi.util.YCPBaseAgent#executeJob(com.yantra.yfs.japi.YFSEnvironment, org.w3c.dom.Document)
	 * This method cancels the eligible orders
	 */
	public void executeJob(YFSEnvironment env, Document docOrderInput) throws Exception {
		log.verbose("Begin of AcademyCancelDelayedOrder.executeJob() method");

		Document docCompleteOrderDetailsOut = null;
		//Document docChangeShipmentOut = null;
		//Document docChangeShipmentStatusOut = null;
		//Document docChangeOrderOut = null;
		Document docOrderLinesIn = null;

		Element eleOrderIn = null;
		Element eleOrderOut = null;		
		Element eleOrderLines = null;		
		Element eleOrderLine = null;
		Element eleShipmentLines = null;		
		Element eleShipmentLine = null;
		Element eleShipment = null;
		//Element eleChangeShipmentOut = null;
			
		NodeList nlOrderLines = null;
		NodeList nlShipmentLines = null;

		double dTotalShipLineQty = 0.0;
		double dTotalBackRoomPickQty = 0.0;
		double dFullShipPendingQty = 0.0;
		double dTotalShipLineQtyWMSDC = 0.0;
		double dTotalBackRoomPickQtyWMSDC = 0.0;		
		double dNewOrderLineQty = 0.0;
		double dSLineQty = 0.0;
		double dTotalLineQty =0.0;
		double dOrderedQty=0.0;
		double dneworderedQty=0.0;
		double dSLineBackPickQty = 0.0;
		String strOrderDate = null;
		String strEligibleDaysToCancelDelayedOrder = null;
		String strEarliestScheduleDate = null;
		String strEligibleCancelDate = null;
		boolean hotSKUOrderLine=false;
		boolean isGiftItem=false;
		String strExtnIsPromoItem=null;
		
		eleOrderIn = docOrderInput.getDocumentElement();

		docCompleteOrderDetailsOut = callGetCompleteOrderDetails(env, eleOrderIn.getAttribute(AcademyConstants.ATTR_ORDER_NO));
		
		log.verbose("docCompleteOrderDetailsOut XML"+XMLUtil.getXMLString(docCompleteOrderDetailsOut));

		eleOrderOut = docCompleteOrderDetailsOut.getDocumentElement();

		eleOrderLines = (Element) eleOrderOut.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINES).item(0);
		
		log.verbose("OrderDate :: "+eleOrderOut.getAttribute(AcademyConstants.ATTR_ORDER_DATE));
		
		//WN-920 Hot Market - Ignore cancel agent
		strEligibleDaysToCancelDelayedOrder = eleOrderIn.getAttribute(AcademyConstants.ATTR_ELIGIBLE_DAYS_TO_CANCEL_DELAYED_RELEASE);
		log.verbose("DaysToCancelDelayedRelease :: " +strEligibleDaysToCancelDelayedOrder);

		if(!YFSObject.isVoid(eleOrderLines)){

			nlOrderLines = eleOrderLines.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINE);
			log.verbose("OrderLine Count :: "+nlOrderLines.getLength());
			log.verbose("*************");
			
			//WN-920 Hot Market - Ignore cancel agent
			//if(checkForSkipOrder(nlOrderLines)) {
			if(checkForSkipOrder(nlOrderLines,strEligibleDaysToCancelDelayedOrder)) {
							
				log.verbose("Processing OrderNo :: "+eleOrderOut.getAttribute(AcademyConstants.ATTR_ORDER_NO));
				docOrderLinesIn = XMLUtil.createDocument(AcademyConstants.ELEM_ORDER_LINES);
				listSkipShipment = new ArrayList<String>();
				hmpChangeShipmentStatus = new HashMap<String, Document>();
				
				for (int iOrderLineCount = 0; iOrderLineCount < nlOrderLines.getLength(); iOrderLineCount++) {
					dTotalShipLineQty = 0;
					dTotalBackRoomPickQty = 0;
					dFullShipPendingQty = 0;
					dTotalShipLineQtyWMSDC = 0;
					dTotalBackRoomPickQtyWMSDC = 0;
					dNewOrderLineQty = 0;
					dTotalLineQty=0;
					String strBackroomPickedQuantity = "";
					hotSKUOrderLine=false;
					isGiftItem=false;
					eleOrderLine = (Element) nlOrderLines.item(iOrderLineCount);
					
                    //BOPIS 2023 -Start Skip Hot Sku orderLine
					strEarliestScheduleDate = eleOrderLine.getAttribute(AcademyConstants.ATTR_EARLIEST_SCHEDULE_DATE);
					log.verbose("EARLIEST_SCHEDULE_DATE :: " +strEarliestScheduleDate);
					
					if(!YFSObject.isVoid(strEarliestScheduleDate))
					{	
						log.verbose(" Delayed Release Order " );
						SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_SIMPLE_DATE_PATTERN);//"yyyy-MM-dd"
						Calendar cal = Calendar.getInstance();
						cal.add(Calendar.DATE, (-Integer.parseInt(strEligibleDaysToCancelDelayedOrder)));
						strEligibleCancelDate = sdf.format(cal.getTime());
						Date dateEligibleCancelDate = (Date)sdf.parse(strEligibleCancelDate);
						
						Date dateEarliestScheduleDate = (Date)sdf.parse(strEarliestScheduleDate);
									
						if(dateEligibleCancelDate.before(dateEarliestScheduleDate)){
							hotSKUOrderLine=true;
						}
					}
					
					log.verbose(" hotSKUOrderLine " + hotSKUOrderLine);
					//OMNI-92329 Changes START
					strExtnIsPromoItem=XPathUtil.getString(eleOrderLine,AcademyConstants.ATTR_IS_PROMO_ITEM_XPATH);
					log.verbose("Whether Item is a gift Item :"+ strExtnIsPromoItem);
					if(!YFSObject.isVoid(strExtnIsPromoItem)&&strExtnIsPromoItem.equalsIgnoreCase(AcademyConstants.STR_YES)) {
						isGiftItem=true;
						log.verbose("Item is giftItem");
					}
					//OMNI-92329 Changes END
					//BOPIS 2023 -End Skip Hot Sku orderLine
					//Skip OrderLines with MinLineStatus in listMinLineStatus and skip hotSKU orderLines
					if(!listMinLineStatus.contains(eleOrderLine.getAttribute(AcademyConstants.ATTR_MIN_LINE_STATUS))&&(!hotSKUOrderLine)&&!isGiftItem){//Condition added for OMNI-92329

						log.verbose("OrderLineKey:"+eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
						String strFulfillmentType = eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
						String strOrderedQty=eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
						dOrderedQty=Double.parseDouble(strOrderedQty);
						log.verbose("dOrderedQty:"+ dOrderedQty);
						log.verbose("FulfillmentType:"+ strFulfillmentType);
						eleShipmentLines = (Element) eleOrderLine.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES).item(0);

						//BOPIS 2023 Start Check to Ignore SOF, BOPIS and DropShip Lines
						if(!YFSObject.isVoid(eleShipmentLines) && 
								!(AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equals(strFulfillmentType)||
								AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE.equals(strFulfillmentType)||
								AcademyConstants.STR_DROP_SHIP.equals(strFulfillmentType)||
								//Begin: OMNI-11359
								AcademyConstants.STR_SHIP_TO_STORE.equals(strFulfillmentType)|| AcademyConstants.STR_E_GIFT_CARD.equals(strFulfillmentType))//Condition added for OMNI-90430
								//End: OMNI-11359
								){	
								log.verbose("entering in if block with fulfillment type as "+strFulfillmentType);
							//BOPIS 2023 End Check to Ignore SOF and BOPIS Lines
							nlShipmentLines = eleShipmentLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
							log.verbose("ShipmentLine Count :: "+nlShipmentLines.getLength());

							for (int iShipmenLineCount = 0; iShipmenLineCount < nlShipmentLines.getLength(); iShipmenLineCount++) {
								dSLineQty = 0;
								dSLineBackPickQty = 0;
								
								eleShipmentLine = (Element) nlShipmentLines.item(iShipmenLineCount);

								dSLineQty = Double.parseDouble(eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY));
								eleShipment = (Element) eleShipmentLine.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
								String strShipNode=eleShipment.getAttribute(AcademyConstants.SHIP_NODE);

								log.verbose("ShipmentLineKey :: "+eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
								log.verbose("ShipmentNo :: "+eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
								
								//WN-735 Added cancel status validation as there are many cancel shipments in production which has BackroomPickedQuantity
								if(!AcademyConstants.VAL_CANCELLED_STATUS.equals(eleShipment.getAttribute(AcademyConstants.ATTR_STATUS))){									
								
								//Below quantity would be used for changing the qty at order line level
								if(AcademyConstants.WMS_NODE.equalsIgnoreCase(eleShipment.getAttribute(AcademyConstants.SHIP_NODE))){								
									dTotalShipLineQtyWMSDC = dTotalShipLineQtyWMSDC + dSLineQty;
								} 
								
								//BOPIS 2023 Start To Calculate ShipmentLine Qty
								boolean rcpNodeExist=checkRCPNode(env,strShipNode);
								log.verbose("RCPNode as a ShipNode :: "+ rcpNodeExist);
								
								if(rcpNodeExist)
								{
									strBackroomPickedQuantity = eleShipmentLine.getAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY);
								    if(!YFSObject.isVoid(strBackroomPickedQuantity)){
									dSLineBackPickQty = Double.parseDouble(strBackroomPickedQuantity);
									dTotalBackRoomPickQty = dTotalBackRoomPickQty + dSLineBackPickQty;
									log.verbose("RCPNode Total BackroomPicked Qty "+ dTotalBackRoomPickQty);
									log.verbose("RCPNode Line BackroomPicked Qty "+ dSLineBackPickQty);
								    } 
								}
								else if(listShipmentStatus.contains(eleShipment.getAttribute(AcademyConstants.ATTR_STATUS))){
								dTotalLineQty=dTotalLineQty+dSLineQty;
								log.verbose(" Total Line Qty "+ dTotalLineQty);
								}
								//BOPIS 2023 End To Calculate ShipmentLine Qty
								
								//1100.70.06.10 -Ready For Backroom Pick , 1100.70.06.30  -Ready To Ship, 9000 -Shipment Cancelled,1600.002 -Shipment Invoiced
								//1100.70 - Sent To Node (for 005 node)
								//Shipment status released to 005 node is not configured  
								if(listShipmentStatus.contains(eleShipment.getAttribute(AcademyConstants.ATTR_STATUS))
										&& !listSkipShipment.contains(eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO))){

									//This condition is to check if Quantity and BackroomPickQty are equal or not ShipmentLine level
									//If ShipmentLine qty is 0 then its already cancelled
									//If ShipmentLine qty and ShipmentLine Backroom pick qty are same then cancel is not allowed
									log.verbose(" dSLineQty :: "+dSLineQty +" dSLineBackPickQty :: "+dSLineBackPickQty);
									
									if(dSLineQty >0.0){
										cancelDelayedShipment(env, eleShipmentLine, dSLineBackPickQty);
										//Vertex call was happening only for first shipment and skipping for other shipments because of this txn obj. To avoid this, setting the value as null.
										env.setTxnObject(AcademyConstants.STR_SHIPMENT_QUOTE_CALL,null);
										log.verbose("changeShipment successfull -- AcademyCancelDelayedOrder.executeJob() method");
									}
									
									if(!listSkipShipment.contains(eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO))){
										//adding shipment to map to make only one ChangeShipmentStatus() call for each shipment.
										if(!hmpChangeShipmentStatus.containsKey(eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO))){
											addToChangeShipmentStatusMap(eleShipment);
										}
									}
								}
								
								//The dNewOrderLineQty will be order line qty after changeOrder call
								//BOPIS 2023 Start New Ordered Qty
								if(rcpNodeExist)
								{
									dNewOrderLineQty = dTotalBackRoomPickQty + dTotalShipLineQtyWMSDC;
								}
								else{
								dneworderedQty=(dOrderedQty- dTotalLineQty);
								dNewOrderLineQty = dneworderedQty+ dTotalShipLineQtyWMSDC ;
								}
								//BOPIS 2023 Start New Ordered Qty
								}//End of Shipment cancel status validation
							} //end of ShipmentLine for loop

							log.verbose("dTotalBackRoomPickQty ::"+dTotalBackRoomPickQty+ " dNewOrderLineQty :: "+dNewOrderLineQty +" dTotalShipLineQtyWMSDC :: "+dTotalShipLineQtyWMSDC
									+"dFullShipPendingQty :::"+dFullShipPendingQty+"dTotalShipLineQty ::"+dTotalShipLineQty);

							prepareInDocForChangeOrder(docOrderLinesIn,XMLUtil.getDocumentForElement(eleOrderLine),dNewOrderLineQty);
							log.verbose("*************");
						} 
					}//end of inner if block

				}//end of OrderLine for loop
				
				invokeChangeShipmentStatus(env);
				
				cancelDelayedOrder(env, XMLUtil.getDocumentForElement(eleOrderOut),docOrderLinesIn);

			} else {
				log.info("Order no :: "+ eleOrderOut.getAttribute(AcademyConstants.ATTR_ORDER_NO) +" is skipped in AcademyCancelDelayedOrder.executeJob() method.");
			}
		}//end of outer if block of OrderLine

		log.verbose("End of AcademyCancelDelayedOrder.executeJob() method ::");
	}

	//Bopis: 2023 Start
	/**
	 * @param env
	 * @param strShipNode
	 * @throws Exception
	 */
	private boolean checkRCPNode(YFSEnvironment env,String strShipNode) throws Exception {
		List<String> listRCPNode = new ArrayList<String>();
		Document docOutGetCommonCodeList = AcademyBOPISUtil.getCommonCodeList
				(env, AcademyConstants.STR_RCP_NODE, AcademyConstants.PRIMARY_ENTERPRISE);
		Element eleoutgetcommoncodeList = docOutGetCommonCodeList.getDocumentElement();
		NodeList nlCommoncodeList = eleoutgetcommoncodeList.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
		for (int l = 0; l < nlCommoncodeList.getLength(); l++) 
		{
			Element eleCommoncode = (Element) nlCommoncodeList.item(l);
		     String strRCPNode=eleCommoncode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
		     listRCPNode.add(strRCPNode);
		}
		if(listRCPNode.contains(strShipNode)){
		return true;
		}
		else{
		return false;
		}
	}
	//Bopis: 2023 End

	
	/** Add changeShipmentStatus input to has map for each shipment to avoid multiple api call.
	 * @param eleShipment
	 * @throws ParserConfigurationException
	 */
	private void addToChangeShipmentStatusMap(Element eleShipment) throws Exception {
		log.verbose("Begin of AcademyCancelDelayedOrder.addToChangeShipmentStatusMap() method");
		
		Document docChangeShipmentStatusInput = null;
		Element eleChangeShipmentStatusInput = null;

		docChangeShipmentStatusInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleChangeShipmentStatusInput =docChangeShipmentStatusInput.getDocumentElement();
		eleChangeShipmentStatusInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
		
		if (AcademyConstants.STR_SHIPMENT_CREATED_STATUS.equals(eleShipment.getAttribute(AcademyConstants.ATTR_STATUS))){
			eleChangeShipmentStatusInput.setAttribute(AcademyConstants.ATTR_TRANSID, AcademyConstants.STR_CANCEL_SHIPMENT_TRAN);
		}
		else if (AcademyConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL.equals(eleShipment.getAttribute(AcademyConstants.ATTR_STATUS))){
			eleChangeShipmentStatusInput.setAttribute(AcademyConstants.ATTR_TRANSID, AcademyConstants.STR_BACKROOM_PICK_TRAN);
			//BOPIS 2023 Start added BaseDrop Status
			eleChangeShipmentStatusInput.setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS,"1100.70.06.30");
			//BOPIS 2023 End added BaseDrop Status
		}
		
		log.verbose("hmpChangeShipmentStatus.size() : " + hmpChangeShipmentStatus.size());
		hmpChangeShipmentStatus.put(eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO), docChangeShipmentStatusInput);
		
		log.verbose("End of AcademyCancelDelayedOrder.addToChangeShipmentStatusMap() method");
	}

	private Document invokeChangeShipmentStatus(YFSEnvironment env) {

		log.verbose("Begin of AcademyCancelDelayedOrder.invokeChangeShipmentStatus() method");
		Document docInChangeShipmentStatus = null;
		Document docOutChangeShipmentStatus = null;
		String strShipmentNo = "";
		
		try{
			Iterator itChangeShipmentStatus = hmpChangeShipmentStatus.entrySet().iterator();
			while (itChangeShipmentStatus.hasNext()) {
				Map.Entry<String, Document> meChangeShipmentStatus = (Map.Entry<String, Document>)itChangeShipmentStatus.next();
				docInChangeShipmentStatus = (Document)meChangeShipmentStatus.getValue();
				strShipmentNo = meChangeShipmentStatus.getKey();

				log.verbose("Input to changeShipmentStatus API for ShipmentNo:" +strShipmentNo+" :: \n"+XMLUtil.getXMLString(docInChangeShipmentStatus));
				docOutChangeShipmentStatus = AcademyUtil.invokeAPI(env,AcademyConstants.CHANGE_SHIPMENT_STATUS_API, docInChangeShipmentStatus);
				log.verbose("Output of changeShipmentStatus API ::"+ XMLUtil.getXMLString(docOutChangeShipmentStatus));
			}
			log.verbose("End of AcademyCancelDelayedOrder.invokeChangeShipmentStatus() method");
			
		} catch (Exception e) {
			log.info("Exception occured in AcademyCancelDelayedOrder.invokeChangeShipmentStatus() method for ShipmentNo :" + strShipmentNo);
			log.verbose(e.getMessage());
			throw new YFSException(e.getMessage());
		}
		return docOutChangeShipmentStatus;
	}
	
	
	/** This method executes getOrderList API to get the eligible orders for cancellation
	 * getOrderList o/p template
	 *  <OrderList>
	 *		<Order OrderNo="" OrderHeaderKey="" MaxOrderStatus="" MinOrderStatus="" Status=""/> 
	 *	</OrderList>
	 * @param env
	 * @param fromDate
	 * @param toDate
	 * @return
	 * @throws Exception
	 */
	private Document callGetOrderList(YFSEnvironment env, String fromDate, String toDate) throws Exception {

		log.verbose("Begin of AcademyCancelDelayedOrder.callGetOrderList() method");
		Document docInOrderList = null;
		Document docOutOrderList = null;
		Document docOrderListTemplate = null;
		Element eleInOrder = null;

		docInOrderList = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		eleInOrder = docInOrderList.getDocumentElement();
		eleInOrder.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
		eleInOrder.setAttribute(AcademyConstants.DRAFT_ORDER_FLAG, AcademyConstants.STR_NO);
		eleInOrder.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		eleInOrder.setAttribute(AcademyConstants.ATTR_IS_HISTORY, AcademyConstants.STR_NO);
		eleInOrder.setAttribute(AcademyConstants.ATTR_ORDER_DATE_QRY_TYPE, AcademyConstants.BETWEEN);
		eleInOrder.setAttribute(AcademyConstants.ORDER_DATE_FROM_RANGE, fromDate);
		eleInOrder.setAttribute(AcademyConstants.ORDER_DATE_TO_RANGE, toDate);
		eleInOrder.setAttribute(AcademyConstants.ATTR_STATUS_QRY_TYPE, AcademyConstants.STR_NE);
		eleInOrder.setAttribute(AcademyConstants.ATTR_STATUS, AcademyConstants.VAL_CANCELLED_STATUS);
		
		Document docOrderListComplexQryInput = XMLUtil.getDocument(strOrderListComplexQry);
		XMLUtil.importElement(eleInOrder, docOrderListComplexQryInput.getDocumentElement());

		docOrderListTemplate = XMLUtil.createDocument(AcademyConstants.ELE_ORDER_LIST);
		Element eleOrderOut = docOrderListTemplate.createElement(AcademyConstants.ELE_ORDER);
		eleOrderOut.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, AcademyConstants.STR_EMPTY_STRING);
		eleOrderOut.setAttribute(AcademyConstants.ATTR_ORDER_NO, AcademyConstants.STR_EMPTY_STRING);
		eleOrderOut.setAttribute(AcademyConstants.ATTR_MAX_ORDER_STATUS, AcademyConstants.STR_EMPTY_STRING);
		eleOrderOut.setAttribute(AcademyConstants.ATTR_MIN_ORDER_STATUS, AcademyConstants.STR_EMPTY_STRING);
		eleOrderOut.setAttribute(AcademyConstants.ATTR_STATUS, AcademyConstants.STR_EMPTY_STRING);
		docOrderListTemplate.getDocumentElement().appendChild(eleOrderOut);

		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST,docOrderListTemplate);
		docOutOrderList = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_ORDER_LIST, docInOrderList);
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);

		log.verbose("End of AcademyCancelDelayedOrder.callGetOrderList() method");

		return docOutOrderList;
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
	private Document callGetCompleteOrderDetails(YFSEnvironment env, String strOrderNo) throws Exception {
		
		log.verbose("Begin of AcademyCancelDelayedOrder.callGetCompleteOrderDetails() method");
		Document docGetCompleteOrderDetailsInput = null;
		Document docGetCompleteOrderDetailsOut = null;
		Element eleOrder = null;

		docGetCompleteOrderDetailsInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		eleOrder = docGetCompleteOrderDetailsInput.getDocumentElement();
		eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		eleOrder.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,AcademyConstants.PRIMARY_ENTERPRISE);
		eleOrder.setAttribute(AcademyConstants.ATTR_DOC_TYPE,AcademyConstants.SALES_DOCUMENT_TYPE);

		log.verbose("Input of getCompleteOrderDetails :: "+ XMLUtil.getXMLString(docGetCompleteOrderDetailsInput));
		env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,AcademyConstants.STR_TEMPLATEFILE_GETCOMPLETEORDER_DETAILS_API);
		docGetCompleteOrderDetailsOut = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,docGetCompleteOrderDetailsInput);
		env.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS);
		log.verbose("Output of getCompleteOrderDetails ::"+ XMLUtil.getXMLString(docGetCompleteOrderDetailsOut));

		log.verbose("End of AcademyCancelDelayedOrder.callGetCompleteOrderDetails() method");
		return docGetCompleteOrderDetailsOut;
	}

	/** This prepares OrderLine element for changeOrder Input
	 * <OrderLine Action="" OrderLineKey="201702150011522424720620" OrderedQty="0"/>
	 * @param docInXML
	 * @param newChangeOrderQty
	 * @return
	 */
	private void prepareInDocForChangeOrder(Document docOrderLinesInput,Document docInXML,double newChangeOrderQty) {
		log.verbose("Begin of AcademyCancelDelayedOrder.prepareDocumentForChangeOrder() method");
		
		Element eleOrderLine = null;
		Element eleXMLRoot = null;
		
		try {
			if (docOrderLinesInput != null){
				eleXMLRoot = docInXML.getDocumentElement();
				eleOrderLine = docOrderLinesInput.createElement(AcademyConstants.ELEM_ORDER_LINE);
				//eleOrderLine.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_EMPTY_STRING);
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,eleXMLRoot.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, String.valueOf(newChangeOrderQty));
				docOrderLinesInput.getDocumentElement().appendChild(eleOrderLine);
			}
			
			log.verbose("End of AcademyCancelDelayedOrder.prepareDocumentForChangeOrder() method");

		} catch (Exception e) {
			log.verbose("Exception occured in AcademyCancelDelayedOrder.prepareDocumentForChangeOrder() method for OrderLineKey :"
					+eleXMLRoot.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
			log.verbose(e.getMessage());
			throw new YFSException(e.getMessage());
		}
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
	private Document cancelDelayedOrder(YFSEnvironment env, Document docInXML,Document docOrdLinesInXML) {

		log.verbose("Begin of AcademyCancelDelayedOrder.cancelDelayedOrder() method");
		Document docInChangeOrder = null;
		Document docOutChangeOrder = null;
		Element eleOrder = null;
		Element eleXMLRoot = null;

		try {
			eleXMLRoot = docInXML.getDocumentElement();
			
			docInChangeOrder = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			eleOrder =docInChangeOrder.getDocumentElement();
			eleOrder.setAttribute(AcademyConstants.ATTR_IGNORE_ORDERING, AcademyConstants.ATTR_Y);
			if(!YFSObject.isVoid(SCXmlUtil.getChildElement(docOrdLinesInXML.getDocumentElement(), "OrderLine"))){
			eleOrder.setAttribute(AcademyConstants.ATTR_MOD_REASON_CODE, AcademyConstants.STR_CANCEL_REASON_CODE);
			}
			eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,eleXMLRoot.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
			eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO,eleXMLRoot.getAttribute(AcademyConstants.ATTR_ORDER_NO));
			eleOrder.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.ATTR_Y);
			eleOrder.setAttribute(AcademyConstants.ATTR_CHANGE_ORDER, AcademyConstants.ATTR_Y);			
			XMLUtil.importElement(eleOrder, docOrdLinesInXML.getDocumentElement());
			
			log.verbose("Input to changeOrder :: "+XMLUtil.getXMLString(docInChangeOrder));
			docOutChangeOrder = AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_ORDER, docInChangeOrder);
			log.verbose("Output of changeOrder ::"+ XMLUtil.getXMLString(docOutChangeOrder));
			log.verbose("End of AcademyCancelDelayedOrder.cancelDelayedOrder() method");

		} catch (Exception e) {
			log.info("Exception occured in AcademyCancelDelayedOrder.cancelDelayedOrder() method for OrderLineKey :"
					+eleXMLRoot.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
			log.verbose(e.getMessage());
			throw new YFSException(e.getMessage());
		}
		return docOutChangeOrder;
	}

	/**This method executes changeShipment API to reduce shipment line quantity
	 * @param env
	 * @param inDoc
	 * @return
	 */
	private Document cancelDelayedShipment(YFSEnvironment env,Element eleXMLRoot,Double dSLineBackPickQty) {

		log.verbose("Begin of AcademyCancelDelayedOrder.cancelDelayedShipments() method");

		Document docInChangeShipment = null;
		Document docOutChangeShipment = null;
		Element eleShipmentFromInXML = null;
		Element eleShipment = null;
		Element eleExtn = null;
		Element eleShipmentLines = null;
		Element eleShipmentLine = null;
		Document docChangeShipmentTemplate = null;
		
		try {			
			eleShipmentFromInXML = (Element) eleXMLRoot.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
			docInChangeShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);

			eleShipment = docInChangeShipment.getDocumentElement();
			eleShipment.setAttribute(AcademyConstants.ATTR_BACK_ORD_REM_QTY, AcademyConstants.STR_YES);
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, eleShipmentFromInXML.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
			
			//ShipmentNo and DocumentType is set to make vertex call as per existing changeShipment UE behavior.
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, eleShipmentFromInXML.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
			eleShipment.setAttribute(AcademyConstants.ATTR_DOC_TYPE, eleShipmentFromInXML.getAttribute(AcademyConstants.ATTR_DOC_TYPE));
			
			if(YFSObject.isVoid(eleShipmentFromInXML.getAttribute(AcademyConstants.ATTR_MANIFEST_NO))){
				eleShipment.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.VAL_CANCEL);
				//Add the ShipmentNo to skip for next changeShipment or changeShipmentStatus call as whole shipment will get canceled
				listSkipShipment.add(eleShipmentFromInXML.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
			}
			
			eleExtn = docInChangeShipment.createElement(AcademyConstants.ELE_EXTN);
			eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_SHORTPICK_REASON_CODE, AcademyConstants.STR_CANCEL_REASON_CODE);
			
			eleShipmentLines = docInChangeShipment.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
			
			eleShipmentLine = docInChangeShipment.createElement(AcademyConstants.ELE_SHIPMENT_LINE);			
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY,String.valueOf(dSLineBackPickQty));
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, eleXMLRoot.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
			
			eleShipment.appendChild(eleExtn);
			eleShipment.appendChild(eleShipmentLines);
			eleShipmentLines.appendChild(eleShipmentLine);
			
			docChangeShipmentTemplate = XMLUtil.getDocument(strChangeShipmentTemplate);
			env.setApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipmentTemplate);
			
			log.verbose("Input to changeShipment API :: "+ XMLUtil.getXMLString(docInChangeShipment));
			docOutChangeShipment = AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_SHIPMENT, docInChangeShipment);
			env.clearApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT);
			log.verbose("Output of changeShipment API ::"+ XMLUtil.getXMLString(docOutChangeShipment));
			log.verbose("End of AcademyCancelDelayedOrder.cancelDelayedShipments() method");

		} catch (Exception e) {
			log.info("Exception occured in AcademyCancelDelayedOrder.cancelDelayedShipment() method for ShipmentLineKey :"
					+eleXMLRoot.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
			log.verbose(e.getMessage());
			throw new YFSException(e.getMessage());
		}
		return docOutChangeShipment;
	}
	
	/**This method checks If any order line released to 005 node and in less than the Shipped status then skip the complete order.
	 * If any order line is DSV and in less than Delivered status then skip the complete order.
	 * @param nlOrderLines
	 * @return
	 * @throws ParseException 
	 */
	
	//Modified for WN-802 Hot Market - Delayed Release. Added new parameter strDaysToCancelDelayedOrder
	//private boolean checkForSkipOrder(NodeList nlOrderLines)
	private boolean checkForSkipOrder(NodeList nlOrderLines, String strEligibleDaysToCancelDelayedOrder) throws ParseException{
			
		log.verbose("Begin of AcademyCancelDelayedOrder.checkForSkipOrder() method");
		
		Element eleOrderLine = null;
		Element eleShipmentLines = null;		
		Element eleShipmentLine = null;
		Element eleShipment = null;
		NodeList nlShipmentLines = null;
		
		//Start WN-920 Hot Market - Ignore cancel agent
		//String strEarliestScheduleDate = null;
		//String strEligibleCancelDate = null;
		//End WN-920 Hot Market - Ignore cancel agent
		
		int iWMSPendingCnt = 0;
		int iDSVPendingCnt = 0;
		//SOF :: Start WN-2043 - Cancel agent
		//String strFulfillmentType = null;
		//SOF :: End WN-2043 - Cancel agent

		if(!YFSObject.isVoid(nlOrderLines)){

			for (int iOrderLine = 0; iOrderLine < nlOrderLines.getLength(); iOrderLine++) {
				eleOrderLine = (Element) nlOrderLines.item(iOrderLine);
				eleShipmentLines = (Element) eleOrderLine.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES).item(0);
				
				//SOF :: Start WN-2043 - Cancel agent
				//Commenting as a part of Bopis 2023
				//strFulfillmentType = eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
				//if(AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equals(strFulfillmentType)){
				//	log.verbose(" Skip SOF Order without cancelling " );
				//	return false;
				//}
				//Commenting as a part of Bopis 2023
				//SOF :: End WN-2043 - Cancel agent

				//Commenting as a part of Bopis 2023
				//Start WN-920 Hot Market - Ignore cancel agent
//				strEarliestScheduleDate = eleOrderLine.getAttribute(AcademyConstants.ATTR_EARLIEST_SCHEDULE_DATE);
//				log.verbose("EARLIEST_SCHEDULE_DATE :: " +strEarliestScheduleDate);
//				
//				if(!YFSObject.isVoid(strEarliestScheduleDate))
//				{	
//					log.verbose(" Delayed Release Order " );
//					SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_SIMPLE_DATE_PATTERN);//"yyyy-MM-dd"
//					Calendar cal = Calendar.getInstance();
//					cal.add(Calendar.DATE, (-Integer.parseInt(strEligibleDaysToCancelDelayedOrder)));
//					strEligibleCancelDate = sdf.format(cal.getTime());
//					Date dateEligibleCancelDate = (Date)sdf.parse(strEligibleCancelDate);
//					
//					Date dateEarliestScheduleDate = (Date)sdf.parse(strEarliestScheduleDate);
//								
//					if(dateEligibleCancelDate.before(dateEarliestScheduleDate))
//					{
//						log.verbose(" Skip Delayed Release Order without cancelling " );
//						return false;
//					}
//					
//				}
				//End WN-920 Hot Market - Ignore cancel agent
				//Commenting as a part of Bopis 2023
				if(!YFSObject.isVoid(eleShipmentLines)){
					nlShipmentLines = eleShipmentLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
					for (int iShipmenLine = 0; iShipmenLine < nlShipmentLines.getLength(); iShipmenLine++) {

						eleShipmentLine = (Element) nlShipmentLines.item(iShipmenLine);
						eleShipment = (Element) eleShipmentLine.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
						if(AcademyConstants.WMS_NODE.equalsIgnoreCase(eleShipment.getAttribute(AcademyConstants.SHIP_NODE)) &&
								!listWMSShipmentStatus.contains(eleShipment.getAttribute(AcademyConstants.ATTR_STATUS))){
							iWMSPendingCnt ++;

						} 
						//Commenting as a part of Bopis 2023
						//else if (AcademyConstants.DOCUMENT_TYPE_PO.equalsIgnoreCase(eleShipment.getAttribute(AcademyConstants.ATTR_DOC_TYPE)) &&
							//	!listDSVShipmentStatus.contains(eleShipment.getAttribute(AcademyConstants.ATTR_STATUS))){
							//iDSVPendingCnt ++;							
						//}
						//Commenting as a part of Bopis 2023
					}//end of ShipmenLine for loop
				}
			}// end of OrderLine for loop
		}
		log.verbose("WMSPendingCount :"+iWMSPendingCnt+" DSVPendingCount : "+iDSVPendingCnt);
		log.verbose("End of AcademyCancelDelayedOrder.checkForSkipOrder() method");
		
		if(iWMSPendingCnt > 0 || iDSVPendingCnt > 0){
			return false;//Skip the order
		} else {
			return true;
		}
	}
}//End of AcademyCancelDelayedOrder class