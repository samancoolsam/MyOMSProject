package com.academy.ecommerce.sterling.sourcing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.ibm.icu.text.SimpleDateFormat;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
public class AcademySourcingOptimizer {

	private  int intNumberOfStore;

	private  String strOptTurnOffFlag = null;

	private  String strAgileReq = null;

	private  String strloginfo =null;

	private  int iTotalShipments;

	private  boolean blUnholdAllOrdLines = false;

	private  float floatThresholdDistance = 0;

	private  float floatRankConstant = 0;

	private  float floatInventoryConstant = 0;

	private  float floatDistanceConstant = 0;

	private  float floatAgileConstant = 0;

	private  ArrayList<String> alDistance = new ArrayList<String>();

	private  ArrayList<String> alFinallDistance = new ArrayList<String>();

	//private  ArrayList<Float> alInventory = new ArrayList<Float>();
	private  ArrayList<String> alInventory = new ArrayList<String>();

	private  ArrayList<Float> alFinalInventory = new ArrayList<Float>();

	//private  ArrayList<Float> alRank = new ArrayList<Float>();

	private  ArrayList<String> alRank = new ArrayList<String>();
	private  ArrayList<Float> alFinalRank = new ArrayList<Float>();

	private  ArrayList<Float> alFinalHybrid = new ArrayList<Float>();

	private  ArrayList<Float> alRateShopCharges=new ArrayList<Float>();

	private  ArrayList<Float> alFinalRateShopCharges=new ArrayList<Float>();

	private  ArrayList<String> alShipNode = new ArrayList<String>(); //track of iteamid and shipnode

	private  LinkedHashSet<String> lhsShipNode = new LinkedHashSet<String>(); // track of unique ship node

	private  ArrayList<String> alNeglectOptimizationLines = new ArrayList<String>();

	private  ArrayList<String> alOptionShipNodeFinal = new ArrayList<String>();

	private  LinkedHashSet<String> lhsAloption = new LinkedHashSet<String>();

	private  HashMap<String, Document> mapGetSurrondingList = new HashMap<String, Document>();

	private  HashMap<String, ArrayList<String>> mapReqDetailsForRateShop = new HashMap<String,ArrayList<String>>();

	private  NodeList nlShipNodeList = null;

	private  Properties props;

	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	private static YFCLogCategory log = YFCLogCategory.instance(AcademySourcingOptimizer.class);

	public  void processSourcingOptimizer(YFSEnvironment env,
			Document inXML) throws Exception {

		try {
			log.verbose("AcademySourcingOptimizer :: processSourcingOptimizer :: Input xml is : " + XMLUtil.getXMLString(inXML));
			
			/* OMNI-25630: Start change */
			//setting the OrderHeaderKey in env object 
			NodeList nlOrder = inXML.getDocumentElement()
				.getElementsByTagName(AcademyConstants.ELE_ORDER);
			Element orderEle = (Element) nlOrder.item(0);
			String strOrderHeaderKey = orderEle.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
			env.setTxnObject(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
			log.debug("Set Transaction Object - strOrderHeaderKey: "+strOrderHeaderKey);
			/* OMNI-25630: End change */

			ArrayList<String> alFlagStore = new ArrayList<String>();
			HashMap<String, String> mapUnscheduleDetails = new HashMap<String, String>();
			NodeList nlgetOrgDropShipList = null;
			Element getOrgDropShipEle=null;
			strOptTurnOffFlag=props.getProperty("OPTIMIZATION_TURN_OFF_FLAG");

			if (strOptTurnOffFlag.equalsIgnoreCase("N")) {

				strAgileReq=props.getProperty("AGILE_FLAG");
				strloginfo =props.getProperty("LOG_INFO_FLAG");
				floatThresholdDistance=Float.parseFloat(props.getProperty("THRESHOLD_DISTANCE"));
				floatRankConstant=Float.parseFloat(props.getProperty("RANK_CONST"));
				floatInventoryConstant=Float.parseFloat(props.getProperty("INV_CONST"));
				floatDistanceConstant=Float.parseFloat(props.getProperty("DISTANCE_CONST"));
				floatAgileConstant=Float.parseFloat(props.getProperty("AGILE_CONST"));

				String strExcludNodeList=props.getProperty("DC_NODES");

				String strExcludNodeArry[] =strExcludNodeList.split(",");
				for(int i=0;i<strExcludNodeArry.length;i++) {
					alFlagStore.add(strExcludNodeArry[i]);	
				}

				Document getOrgListInDoc = XMLUtil.createDocument("Organization");
				getOrgListInDoc.getDocumentElement().setAttribute("IsNode", "Y");
				Element eleNode = getOrgListInDoc.createElement("Node");
				eleNode.setAttribute("NodeType", "Drop Ship");
				getOrgListInDoc.getDocumentElement().appendChild(eleNode);
				Document getOrgListOutDoc = AcademyUtil.invokeService(env, "AcademygetOrganizationList", getOrgListInDoc);
				if (!YFCObject.isNull(getOrgListOutDoc)) {
					nlgetOrgDropShipList = getOrgListOutDoc.getElementsByTagName("Organization");
					if (!YFCObject.isNull(nlgetOrgDropShipList)) {
						for (int i = 0; i < nlgetOrgDropShipList.getLength(); i++) {

							getOrgDropShipEle = (Element) nlgetOrgDropShipList.item(i);
							if (!YFCObject.isNull(getOrgDropShipEle)) {
								alFlagStore.add(getOrgDropShipEle.getAttribute("OrganizationCode"));	
							}
						}
					}

				}

				Element shipNodeEle = null;

				Document getShipNodeRankListInDoc = XMLUtil.createDocument("AcadShipNodeRank");
				Document getShipNodeRankListOutDoc = AcademyUtil.invokeService(env, "AcademygetShipNodeRankList",
						getShipNodeRankListInDoc);
				log.verbose(" AcademygetShipNodeRankList output is" + XMLUtil.getXMLString(getShipNodeRankListOutDoc));
				if (!YFCObject.isNull(getShipNodeRankListOutDoc)) {
					nlShipNodeList = getShipNodeRankListOutDoc
					.getElementsByTagName("AcadShipNodeRank");
					if (!YFCObject.isNull(nlShipNodeList)) {
						for (int i = 0; i < nlShipNodeList.getLength(); i++) {
							shipNodeEle = (Element) nlShipNodeList.item(i);
							if (!YFCObject.isNull(shipNodeEle)) {
								if (shipNodeEle.getAttribute("StoreFlag")
										.equalsIgnoreCase("Y")) {
									alFlagStore.add(shipNodeEle
											.getAttribute("ShipNode"));
								}
							}
						}
					}
				}

				Document docInputFindInv = XMLUtil.createDocument("Promise");
				Element eleInputFinfInv = docInputFindInv.getDocumentElement();
				eleInputFinfInv.setAttribute("CheckInventory", "Y");
				eleInputFinfInv.setAttribute("EnterpriseCode", "Academy_Direct");
				eleInputFinfInv.setAttribute("OrganizationCode", "Academy_Direct");
				/*eleInputFinfInv.setAttribute("FulfillmentType",
							"Academy FF Type for Shipping");*/
				eleInputFinfInv.setAttribute("MaximumRecords", "10");
				Element eleExcludNodes = docInputFindInv
				.createElement("ExcludedShipNodes");

				for (int i = 0; i < alFlagStore.size(); i++) {

					Element eleExcludNode = docInputFindInv
					.createElement("ExcludedShipNode");
					eleExcludNode.setAttribute("Node", alFlagStore.get(i));
					//OMNI-4252 Node capacity Removed, Hence updated SuppressNodeCapacity to Y
					eleExcludNode.setAttribute("SuppressNodeCapacity", "Y");
					eleExcludNode.setAttribute("SupressProcurement", "Y");
					eleExcludNode.setAttribute("SupressSourcing", "Y");
					eleExcludNodes.appendChild(eleExcludNode);
				}

				eleInputFinfInv.appendChild(eleExcludNodes);
				Element elePromiseLines = docInputFindInv
				.createElement("PromiseLines");
				NodeList nlOrdLine = inXML.getDocumentElement()
				.getElementsByTagName("OrderLine");
				//iNoOfOrdLinesForHold = nlOrdLine.getLength();
				if(strloginfo.equalsIgnoreCase("Y"))
					log.info("ItemID     :   ShipNode    :  OrderLineKey");
				for (int i = 0; i < nlOrdLine.getLength(); i++) {
					Element eleOrdLine = (Element) nlOrdLine.item(i);
					if (!YFCObject.isNull(eleOrdLine)) {

						Element eleItem = (Element) eleOrdLine.getElementsByTagName(
								"Item").item(0);
						Element elePersonInfoShipTo = (Element) eleOrdLine
						.getElementsByTagName("PersonInfoShipTo").item(0);
						Element eleOrderStatuses = (Element) eleOrdLine
						.getElementsByTagName("OrderStatuses").item(0);
						Element eleOrderStatus = (Element) eleOrderStatuses
						.getElementsByTagName("OrderStatus").item(0);
						alShipNode.add(eleItem.getAttribute("ItemID"));
						alShipNode.add(eleOrderStatus.getAttribute("ShipNode"));
						if(strloginfo.equalsIgnoreCase("Y"))
							log.info(eleItem.getAttribute("ItemID")+" : "+eleOrderStatus.getAttribute("ShipNode")+" : "+eleOrdLine.getAttribute("OrderLineKey"));
						alShipNode.add(elePersonInfoShipTo.getAttribute("ZipCode"));
						alShipNode.add(elePersonInfoShipTo.getAttribute("Country"));
						alShipNode.add(elePersonInfoShipTo.getAttribute("State"));
						alShipNode.add(eleOrdLine.getAttribute("PrimeLineNo"));
						lhsShipNode.add(eleOrderStatus.getAttribute("ShipNode"));
						Element elePromiseLine = docInputFindInv
						.createElement("PromiseLine");
						elePromiseLine.setAttribute("ItemID", eleItem
								.getAttribute("ItemID"));
						elePromiseLine.setAttribute("ProductClass", eleItem
								.getAttribute("ProductClass"));
						elePromiseLine.setAttribute("UnitOfMeasure", eleItem
								.getAttribute("UnitOfMeasure"));
						elePromiseLine.setAttribute("LineId", eleOrdLine
								.getAttribute("PrimeLineNo"));
						elePromiseLine.setAttribute("RequiredQty", eleOrdLine
								.getAttribute("OrderedQty"));
						elePromiseLine.setAttribute("FulfillmentType", eleOrdLine
								.getAttribute("FulfillmentType"));
						elePromiseLines.appendChild(elePromiseLine);

						mapUnscheduleDetails.put(eleOrdLine.getAttribute("PrimeLineNo"), eleOrdLine.getAttribute("OrderLineKey"));

						if(strAgileReq.equalsIgnoreCase("Y")){

							SimpleDateFormat newDateFormat = new SimpleDateFormat("yyyy-MM-dd");

							Date sysDate = new Date();
							String strcurDate = newDateFormat.format(sysDate);
							Date reqDateFormat=newDateFormat.parse(strcurDate);

							ArrayList<String> alReqDetailsList=new ArrayList<String>();
							alReqDetailsList.add(eleItem.getAttribute("ItemID"));
							alReqDetailsList.add(eleOrdLine.getAttribute("OrderedQty"));

							String strReqDeliveryDate=eleOrdLine.getAttribute("ReqDeliveryDate");
							String strReqShipDate=eleOrdLine.getAttribute("ReqShipDate");
							if(!(StringUtil.isEmpty(strReqDeliveryDate))){ 
								Date dtReqDeliveryDate = newDateFormat.parse(strReqDeliveryDate.substring(0, 10));

								int result=dtReqDeliveryDate.compareTo(reqDateFormat);
								if(result<0){
									alReqDetailsList.add(new SimpleDateFormat("yyyy-MM-dd").format(new Date(((new Date()).getTime()))));
								}
								else{
									alReqDetailsList.add(strReqDeliveryDate);
								}
							}
							else{
								alReqDetailsList.add(new SimpleDateFormat("yyyy-MM-dd").format(new Date(((new Date()).getTime()))));
							}

							if(!(StringUtil.isEmpty(strReqShipDate))){ 
								Date dtReqShipDate = newDateFormat.parse(strReqShipDate.substring(0, 10));

								int result=dtReqShipDate.compareTo(reqDateFormat);
								if(result<0){
									alReqDetailsList.add(new SimpleDateFormat("yyyy-MM-dd").format(new Date(((new Date()).getTime()))));
								}
								else{
									alReqDetailsList.add(strReqShipDate);
								}
							}
							else{
								alReqDetailsList.add(new SimpleDateFormat("yyyy-MM-dd").format(new Date(((new Date()).getTime()))));
							}
							alReqDetailsList.add(elePersonInfoShipTo.getAttribute("AddressLine1"));
							alReqDetailsList.add(elePersonInfoShipTo.getAttribute("City"));
							alReqDetailsList.add(elePersonInfoShipTo.getAttribute("State"));
							alReqDetailsList.add(elePersonInfoShipTo.getAttribute("ZipCode"));
							alReqDetailsList.add(elePersonInfoShipTo.getAttribute("Country"));

							mapReqDetailsForRateShop.put(eleOrdLine.getAttribute("PrimeLineNo"),alReqDetailsList);
						}
					}
				}
				eleInputFinfInv.appendChild(elePromiseLines);
				log.verbose("Number of shipments  = " + lhsShipNode.size());
				intNumberOfStore = lhsShipNode.size();
				if (intNumberOfStore > 0) {
					getDistanceFromSurroundingNodeListForThreshold(env, alShipNode,
							inXML);
					findInventory(env, docInputFindInv, inXML);

					if(blUnholdAllOrdLines) {		
						if(strloginfo.equalsIgnoreCase("Y"))
							log.info("Not getting Suggested Options from Find Inventory Output :: Stoping Optimization Logic and Releasing Hold.");
						unHoldAllOrderLines(env,inXML);
					}
					else {
						if(strloginfo.equalsIgnoreCase("Y")){
							log.info("Ouput of available Options");
							for (int i = 0; i < alOptionShipNodeFinal.size(); i++) {
								log.info(alOptionShipNodeFinal.get(i));
							}	
						}

						if(strAgileReq.equalsIgnoreCase("Y")){
							getShippingCostFromAgile(env,
									alOptionShipNodeFinal,mapReqDetailsForRateShop, inXML);	

							/*log.info("Charge Array List ");
							for (int i = 0; i < alRateShopCharges.size(); i++) {
								log.info(alRateShopCharges.get(i));
							}*/

							AgileRateShopCall(inXML, env);
							if(strloginfo.equalsIgnoreCase("Y")){
								log.info("Shipping Cost of available options");
								for (int i = 0; i < alFinalRateShopCharges.size(); i++) {
									log.info(alFinalRateShopCharges.get(i));
								}
							}


						}
						else{
							getDistanceFromSurroundingNodeListForDistance(env,
									alOptionShipNodeFinal, inXML, mapGetSurrondingList);

							/*log.info("Distance Array List ");
						for (int i = 0; i < alDistance.size(); i++) {
							log.info(alDistance.get(i));
						}*/
							//Fix:EFP-83 corrected log printing.
							if(strloginfo.equalsIgnoreCase("Y")){
								log.info("Distance of the available option");
								for (int i = 0; i < alDistance.size(); i=i+2) {
									log.info("Ship Node " + alDistance.get(i)+ " : " + alDistance.get(i+1) );
								//EFP-83Changes end
								}	
							}
						}
						getShipNodeInventory(env, alOptionShipNodeFinal, inXML);
						getRank(env, alOptionShipNodeFinal, inXML);

						/*log.info("Inventory Array List ");
						for (int i = 0; i < alInventory.size(); i++) {
							log.info(alInventory.get(i));
						}*/
						
						//Fix:EFP-83 correcting logs
						if(strloginfo.equalsIgnoreCase("Y")){
							log.info("Inventory of available options");
							for (int i = 0; i < alInventory.size(); i++) {
								log.info(alInventory.get(i));
							}
						}

						/*log.info("Rank Array List ");
						for (int i = 0; i < alRank.size(); i++) {
							log.info(alRank.get(i));
						}*/
						if(strloginfo.equalsIgnoreCase("Y")){
							log.info("Rank of available options");
							for (int i = 0; i < alFinalRank.size(); i++) {
								log.info(alFinalRank.get(i));
							}
						}


						Document finalDocForOpt = optimize(inXML, env);
						if(strloginfo.equalsIgnoreCase("Y")){
							log.info("Score of available options");
							for (int i = 1; i < alFinalHybrid.size(); i++) {
								log.info(alFinalHybrid.get(i));
							}
						}

						log.verbose("Final Return Doc From Optimization Logic:"
								+ XMLUtil.getXMLString(finalDocForOpt));
						log.info("Final Return Doc From Optimization Logic:"
								+ XMLUtil.getXMLString(finalDocForOpt));
						unHoldOrderLinesAndChnageShipNode(env,finalDocForOpt,inXML,alNeglectOptimizationLines,mapUnscheduleDetails);

					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();				
			unHoldOrderLinesWithUnChnageShipNode( env,  inXML);
		}			
	}

	private  void unHoldOrderLinesAndChnageShipNode(YFSEnvironment env, Document finalDocForOpt,
			Document inXML, ArrayList<String> alNeglectOptimizationLines, HashMap<String, String> mapUnscheduleDetails) throws Exception {

		try {
			boolean blchangeOrder2Req=false;

			Document inDocUnScheduleOrder=prepareUnScheduleOrderInput(finalDocForOpt,inXML,alNeglectOptimizationLines,mapUnscheduleDetails, env);
			AcademyUtil.invokeAPI(env, "unScheduleOrder", inDocUnScheduleOrder);

			//Document inDocChangeOrder=preparechangeOrderInput(finalDocForOpt,inXML,alNeglectOptimizationLines,mapUnscheduleDetails, env);
			//AcademyUtil.invokeAPI(env, "changeOrder", inDocChangeOrder);

			Document inDocScheduleOrderLine = getInputForScheduleORderines(inXML,finalDocForOpt,alNeglectOptimizationLines);
			Document outDocScheduleOrderLine = AcademyUtil.invokeAPI(env, "scheduleOrderLines", inDocScheduleOrderLine);
			log.verbose("Output for the scheduleOrderLine API" +XMLUtil.getXMLString(outDocScheduleOrderLine));
			Element eleOrder=(Element) inXML.getDocumentElement().getElementsByTagName("Order").item(0);
			String strOrdHeaderKey=eleOrder.getAttribute("OrderHeaderKey");

			/*Document inDocScheduleOrder=XMLUtil.createDocument("ScheduleOrder");
				Element eleScheduleOrder=inDocScheduleOrder.getDocumentElement();
				eleScheduleOrder.setAttribute("OrderHeaderKey", strOrdHeaderKey);
				eleScheduleOrder.setAttribute("CheckInventory", "Y");*/

			//AcademyUtil.invokeAPI(env, "scheduleOrder", inDocScheduleOrder);

			Document inDocgetOrderList=XMLUtil.createDocument("Order");
			Element eleinDoc=inDocgetOrderList.getDocumentElement();
			eleinDoc.setAttribute("OrderHeaderKey", strOrdHeaderKey);

			Document getOrderListOutDoc = AcademyUtil.invokeService(env, "AcademygetOrderListForSourcing",inDocgetOrderList);


			Document changeOrder2InDoc=XMLUtil.createDocument("Order");
			Element elechangeOrder2InDoc=changeOrder2InDoc.getDocumentElement();
			elechangeOrder2InDoc.setAttribute("Override", "Y");
			elechangeOrder2InDoc.setAttribute("OrderHeaderKey", strOrdHeaderKey);

			Element eleChgOrd2Lines=changeOrder2InDoc.createElement("OrderLines");

			NodeList nlOrderHoldType=getOrderListOutDoc.getDocumentElement().getElementsByTagName("OrderHoldType");
			if(nlOrderHoldType.getLength()>0){
				for(int i=0;i<nlOrderHoldType.getLength();i++){
					Element eleOrderHoldType=(Element) nlOrderHoldType.item(i);
					if (!YFCObject.isNull(eleOrderHoldType)) {
						String strHoldType=eleOrderHoldType.getAttribute("HoldType");
						String strStatus=eleOrderHoldType.getAttribute("Status");
						if(strHoldType.equalsIgnoreCase("ACADEMY_SCHREL_HOLD") && strStatus.equalsIgnoreCase("1100")){

							Element eleChgOrd2Line=changeOrder2InDoc.createElement("OrderLine");
							eleChgOrd2Line.setAttribute("OrderLineKey", eleOrderHoldType.getAttribute("OrderLineKey"));
							eleChgOrd2Line.setAttribute("Action", "MODIFY");
							Element eleOrder2HoldTypes=changeOrder2InDoc.createElement("OrderHoldTypes");
							Element eleOrder2HoldType=changeOrder2InDoc.createElement("OrderHoldType");
							eleOrder2HoldType.setAttribute("ReasonText", "ShipNodeCheck");
							eleOrder2HoldType.setAttribute("Status", "1300");
							eleOrder2HoldType.setAttribute("HoldType", "ACADEMY_SCHREL_HOLD");
							eleOrder2HoldTypes.appendChild(eleOrder2HoldType);
							eleChgOrd2Line.appendChild(eleOrder2HoldTypes);
							eleChgOrd2Lines.appendChild(eleChgOrd2Line);
							blchangeOrder2Req=true;
						}

					}
				}

			}

			elechangeOrder2InDoc.appendChild(eleChgOrd2Lines);

			if(blchangeOrder2Req){
				log.verbose("ChangeOrder Input XML:"+XMLUtil.getXMLString(changeOrder2InDoc));
				AcademyUtil.invokeAPI(env, "changeOrder", changeOrder2InDoc);
			}


			log.verbose("Successfully Completed Optimization Logic");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			e.printStackTrace();
			unHoldOrderLinesWithUnChnageShipNode(env,  inXML);
		}

	}

	private  Document preparechangeOrderInput(Document finalDocForOpt, Document inXML,
			ArrayList<String> alNeglectOptimizationLines, HashMap<String, String> mapUnscheduleDetails,YFSEnvironment env) throws Exception {

		Document changeOrderInDoc = null;
		try {
			Element eleOrder=(Element) inXML.getDocumentElement().getElementsByTagName("Order").item(0);
			String strOrdHeaderKey=eleOrder.getAttribute("OrderHeaderKey");

			changeOrderInDoc=XMLUtil.createDocument("Order");
			Element elechangeOrderInDoc=changeOrderInDoc.getDocumentElement();
			elechangeOrderInDoc.setAttribute("Override", "Y");
			elechangeOrderInDoc.setAttribute("OrderHeaderKey", strOrdHeaderKey);

			Element eleChgOrdLines=changeOrderInDoc.createElement("OrderLines");

			NodeList nlOrdLine = inXML.getDocumentElement().getElementsByTagName("OrderLine");
			for (int i = 0; i < nlOrdLine.getLength(); i++) {
				Element eleOrdLine = (Element) nlOrdLine.item(i);
				if (!YFCObject.isNull(eleOrdLine)) {	
					String strPrimeLIneNo=eleOrdLine.getAttribute("PrimeLineNo");
					Element eleChgOrdLine=changeOrderInDoc.createElement("OrderLine");
					eleChgOrdLine.setAttribute("OrderLineKey", eleOrdLine.getAttribute("OrderLineKey"));
					eleChgOrdLine.setAttribute("Action", "MODIFY");

					NodeList nlOrdLineFromOpt=finalDocForOpt.getDocumentElement().getElementsByTagName("OrderLine");
					for(int j=0;j<nlOrdLineFromOpt.getLength();j++) {	
						Element eleOrdLineFromOpt=(Element) nlOrdLineFromOpt.item(j);
						String strPrimeLineNoOpt=eleOrdLineFromOpt.getAttribute("PrimeLineNo");
						if (!YFCObject.isNull(eleOrdLineFromOpt)) {
							if(!alNeglectOptimizationLines.contains(strPrimeLineNoOpt) && strPrimeLIneNo.equalsIgnoreCase(strPrimeLineNoOpt)) {
								eleChgOrdLine.setAttribute("ShipNode", eleOrdLineFromOpt.getAttribute("ShipNode"));		
							}
						}
					}	
					Element eleOrderHoldTypes=changeOrderInDoc.createElement("OrderHoldTypes");
					Element eleOrderHoldType=changeOrderInDoc.createElement("OrderHoldType");
					eleOrderHoldType.setAttribute("ReasonText", "ShipNodeCheck");
					eleOrderHoldType.setAttribute("Status", "1300");
					eleOrderHoldType.setAttribute("HoldType", "ACADEMY_SCHREL_HOLD");
					eleOrderHoldTypes.appendChild(eleOrderHoldType);
					eleChgOrdLine.appendChild(eleOrderHoldTypes);
					eleChgOrdLines.appendChild(eleChgOrdLine);

				}
			}
			elechangeOrderInDoc.appendChild(eleChgOrdLines);
			log.verbose("Change Order Input XML:"+XMLUtil.getXMLString(changeOrderInDoc));

			return changeOrderInDoc;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();				
			unHoldOrderLinesWithUnChnageShipNode(env,  inXML);
		}

		return  changeOrderInDoc;
	}

	private  Document prepareUnScheduleOrderInput(Document finalDocForOpt, Document inXML,
			ArrayList<String> alNeglectOptimizationLines, HashMap<String, String> mapUnscheduleDetails,YFSEnvironment env) throws Exception {
		Document inDocUnScheduleOrder = null;
		try {
			Element eleOrder=(Element) inXML.getDocumentElement().getElementsByTagName("Order").item(0);
			String strOrdHeaderKey=eleOrder.getAttribute("OrderHeaderKey");

			inDocUnScheduleOrder=XMLUtil.createDocument("UnScheduleOrder");
			Element eleUnScheduleOrder=inDocUnScheduleOrder.getDocumentElement();
			eleUnScheduleOrder.setAttribute("OrderHeaderKey", strOrdHeaderKey);
			Element eleUnSchOrderLines=inDocUnScheduleOrder.createElement("OrderLines");

			NodeList nlOrdLineFromOpt=finalDocForOpt.getDocumentElement().getElementsByTagName("OrderLine");
			for(int i=0;i<nlOrdLineFromOpt.getLength();i++) {

				Element eleOrdLineFromOpt=(Element) nlOrdLineFromOpt.item(i);
				String strPrimeLineNo=eleOrdLineFromOpt.getAttribute("PrimeLineNo");
				if (!YFCObject.isNull(eleOrdLineFromOpt)) {
					if(!alNeglectOptimizationLines.contains(strPrimeLineNo)) {
						Element eleUnSchOrderLine=inDocUnScheduleOrder.createElement("OrderLine");
						eleUnSchOrderLine.setAttribute("SubLineNo", "1");
						eleUnSchOrderLine.setAttribute("PrimeLineNo", strPrimeLineNo);
						eleUnSchOrderLine.setAttribute("OrderLineKey", mapUnscheduleDetails.get(strPrimeLineNo));
						eleUnSchOrderLines.appendChild(eleUnSchOrderLine);
					}

				}
			}
			eleUnScheduleOrder.appendChild(eleUnSchOrderLines);
			log.verbose("UnScheduleOrder Input XML:"+XMLUtil.getXMLString(inDocUnScheduleOrder));

//			return inDocUnScheduleOrder;
		} catch (Exception e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			unHoldOrderLinesWithUnChnageShipNode(env,  inXML);
		}

		return inDocUnScheduleOrder;
	}

	private  void unHoldAllOrderLines(YFSEnvironment env, Document inXML) throws Exception {

		try {
			Element eleOrder=(Element) inXML.getDocumentElement().getElementsByTagName("Order").item(0);

			NodeList nlOrdLine = inXML.getDocumentElement()
			.getElementsByTagName("OrderLine");

			Document changeOrderInDoc=XMLUtil.createDocument("Order");
			Element elechangeOrderInDoc=changeOrderInDoc.getDocumentElement();
			elechangeOrderInDoc.setAttribute("Override", "Y");
			elechangeOrderInDoc.setAttribute("OrderHeaderKey", eleOrder.getAttribute("OrderHeaderKey"));

			Element eleChgOrdLines=changeOrderInDoc.createElement("OrderLines");

			for (int i = 0; i < nlOrdLine.getLength(); i++) {
				Element eleOrdLine = (Element) nlOrdLine.item(i);
				if (!YFCObject.isNull(eleOrdLine)) {
					Element eleChgOrdLine=changeOrderInDoc.createElement("OrderLine");
					eleChgOrdLine.setAttribute("OrderLineKey", eleOrdLine.getAttribute("OrderLineKey"));
					eleChgOrdLine.setAttribute("Action", "MODIFY");
					Element eleOrderHoldTypes=changeOrderInDoc.createElement("OrderHoldTypes");
					Element eleOrderHoldType=changeOrderInDoc.createElement("OrderHoldType");
					eleOrderHoldType.setAttribute("ReasonText", "ShipNodeCheck");
					eleOrderHoldType.setAttribute("Status", "1300");
					eleOrderHoldType.setAttribute("HoldType", "ACADEMY_SCHREL_HOLD");
					eleOrderHoldTypes.appendChild(eleOrderHoldType);
					eleChgOrdLine.appendChild(eleOrderHoldTypes);
					eleChgOrdLines.appendChild(eleChgOrdLine);
				}
			}
			elechangeOrderInDoc.appendChild(eleChgOrdLines);
			log.verbose("ChangeOrder Input XML:"+XMLUtil.getXMLString(changeOrderInDoc));

			AcademyUtil.invokeAPI(env, "changeOrder", changeOrderInDoc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();				
			unHoldOrderLinesWithUnChnageShipNode(env,  inXML);
		}

	}

	private  void getShippingCostFromAgile(YFSEnvironment env, ArrayList<String> alOptionShipNodeFinal, HashMap<String, ArrayList<String>> mapReqDetailsForRateShop2,Document inXML) throws Exception {

		try {
			Document inDocgetItemList=preparegetItemListInput(alShipNode, inXML, env);
			Document getItemListOutDoc = AcademyUtil.invokeService(env, "AcademygetgetItemList",inDocgetItemList);

			Document inDocgetOrganizationList=preparegetOrganizationListInput(alOptionShipNodeFinal, inXML, env);
			Document getOrgListOutDoc = AcademyUtil.invokeService(env, "AcademygetOrgShipNodeList",inDocgetOrganizationList);

			String strNodeType =null;
			for(int i=0;i<alOptionShipNodeFinal.size();i=i+3){

				Document inDocRateShopCall=XMLUtil.createDocument("PierbridgeRateShopRequest");
				Element elePierbridgeRateShopRequest=inDocRateShopCall.getDocumentElement();

				Element eleRateGroup=inDocRateShopCall.createElement("RateGroup");
				eleRateGroup.appendChild(inDocRateShopCall.createTextNode("5"));
				elePierbridgeRateShopRequest.appendChild(eleRateGroup);

				Element eleRequiredDate=inDocRateShopCall.createElement("RequiredDate");

				ArrayList <String> alReqDetail=new ArrayList<String>();
				alReqDetail.addAll(mapReqDetailsForRateShop.get(alOptionShipNodeFinal.get(i)));

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				sdf.setLenient(false);
				Date DateReqDate=sdf.parse(alReqDetail.get(2).substring(0, 10));
				SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd/yyyy");

				eleRequiredDate.appendChild(inDocRateShopCall.createTextNode(sdf1.format(DateReqDate)));
				elePierbridgeRateShopRequest.appendChild(eleRequiredDate);

				Date DateShipDate=sdf.parse(alReqDetail.get(3).substring(0, 10));

				Element eleShipDate=inDocRateShopCall.createElement("ShipDate");
				eleShipDate.appendChild(inDocRateShopCall.createTextNode(sdf1.format(DateShipDate)));
				elePierbridgeRateShopRequest.appendChild(eleShipDate);

				Element eleFilterMode=inDocRateShopCall.createElement("FilterMode");
				eleFilterMode.appendChild(inDocRateShopCall.createTextNode("2"));
				elePierbridgeRateShopRequest.appendChild(eleFilterMode);

				Element eleCloseShipment=inDocRateShopCall.createElement("CloseShipment");
				eleCloseShipment.appendChild(inDocRateShopCall.createTextNode("True"));
				elePierbridgeRateShopRequest.appendChild(eleCloseShipment);

				Element eleSender=inDocRateShopCall.createElement("Sender");


				NodeList nlShipNode=getOrgListOutDoc.getDocumentElement().getElementsByTagName("Node");

				for(int k=0;k<nlShipNode.getLength();k++){

					Element eleShipNode=(Element) nlShipNode.item(k);

					if(eleShipNode.getAttribute("ShipNode").equalsIgnoreCase(alOptionShipNodeFinal.get(i+2))){

						Element eleShipNodePersonInfo=(Element) eleShipNode.getElementsByTagName("ShipNodePersonInfo").item(0);

						Element eleSentBy=inDocRateShopCall.createElement("SentBy");
						eleSentBy.appendChild(inDocRateShopCall.createTextNode(eleShipNodePersonInfo.getAttribute("AddressLine1")));
						eleSender.appendChild(eleSentBy);

						Element eleCity=inDocRateShopCall.createElement("City");
						eleCity.appendChild(inDocRateShopCall.createTextNode(eleShipNodePersonInfo.getAttribute("City")));
						eleSender.appendChild(eleCity);

						Element eleRegion=inDocRateShopCall.createElement("Region");
						eleRegion.appendChild(inDocRateShopCall.createTextNode(eleShipNodePersonInfo.getAttribute("State")));
						eleSender.appendChild(eleRegion);

						Element elePostalCode=inDocRateShopCall.createElement("PostalCode");
						elePostalCode.appendChild(inDocRateShopCall.createTextNode(eleShipNodePersonInfo.getAttribute("ZipCode")));
						eleSender.appendChild(elePostalCode);

						Element eleCountry=inDocRateShopCall.createElement("Country");
						eleCountry.appendChild(inDocRateShopCall.createTextNode(eleShipNodePersonInfo.getAttribute("Country")));
						eleSender.appendChild(eleCountry);
						
						//Start : OMNI-326 : Changes for Agile Stabilization 
						strNodeType = XPathUtil.getString(getOrgListOutDoc.getDocumentElement(), AcademyConstants.XPATH_SOURCING_OPTIMIZATION_SHOP_REQUEST_NODE_TYPE);
						Element eleNodeType=inDocRateShopCall.createElement(AcademyConstants.ATTR_NODE_TYPE);
						eleNodeType.appendChild(inDocRateShopCall.createTextNode(strNodeType));
						eleSender.appendChild(eleNodeType);
						//End : OMNI-326 : Changes for Agile Stabilization
						elePierbridgeRateShopRequest.appendChild(eleSender);

					}

				}


				Element eleReceiver=inDocRateShopCall.createElement("Receiver");

				Element eleCompanyName=inDocRateShopCall.createElement("CompanyName");
				eleCompanyName.appendChild(inDocRateShopCall.createTextNode("."));
				eleReceiver.appendChild(eleCompanyName);

				Element eleResidential=inDocRateShopCall.createElement("Residential");
				eleResidential.appendChild(inDocRateShopCall.createTextNode("0"));
				eleReceiver.appendChild(eleResidential);

				Element eleStreet=inDocRateShopCall.createElement("Street");
				eleStreet.appendChild(inDocRateShopCall.createTextNode(alReqDetail.get(4)));
				eleReceiver.appendChild(eleStreet);

				Element eleCityRx=inDocRateShopCall.createElement("City");
				eleCityRx.appendChild(inDocRateShopCall.createTextNode(alReqDetail.get(5)));
				eleReceiver.appendChild(eleCityRx);

				Element eleRegionRx=inDocRateShopCall.createElement("Region");
				eleRegionRx.appendChild(inDocRateShopCall.createTextNode(alReqDetail.get(6)));
				eleReceiver.appendChild(eleRegionRx);

				Element elePostalCodeRx=inDocRateShopCall.createElement("PostalCode");
				elePostalCodeRx.appendChild(inDocRateShopCall.createTextNode(alReqDetail.get(7)));
				eleReceiver.appendChild(elePostalCodeRx);

				Element eleCountryRx=inDocRateShopCall.createElement("Country");
				eleCountryRx.appendChild(inDocRateShopCall.createTextNode(alReqDetail.get(8)));
				eleReceiver.appendChild(eleCountryRx);

				elePierbridgeRateShopRequest.appendChild(eleReceiver);

				Element elePackages=inDocRateShopCall.createElement("Packages");
				Element elePackage=inDocRateShopCall.createElement("Package");


				NodeList nlItem=getItemListOutDoc.getElementsByTagName("Item");
				for(int j=0;j<nlItem.getLength();j++){
					Element eleItem=(Element) nlItem.item(j);
					if(eleItem.getAttribute("ItemID").equalsIgnoreCase(alOptionShipNodeFinal.get(i+1))){
						Element elePrimaryInformation=(Element) eleItem.getElementsByTagName("PrimaryInformation").item(0);
						double dqty=Double.parseDouble(alReqDetail.get(1));
						double dItemHeight=Double.parseDouble(elePrimaryInformation.getAttribute("UnitHeight"));
						double dItemLength=Double.parseDouble(elePrimaryInformation.getAttribute("UnitLength"));
						double dItemWeight=Double.parseDouble(elePrimaryInformation.getAttribute("UnitWeight"));
						double dItemWidth=Double.parseDouble(elePrimaryInformation.getAttribute("UnitWidth"));


						Element eleReceiverName=inDocRateShopCall.createElement("ReceiverName");
						eleReceiverName.appendChild(inDocRateShopCall.createTextNode("."));
						elePackage.appendChild(eleReceiverName);

						Element eleWeight=inDocRateShopCall.createElement("Weight");
						eleWeight.appendChild(inDocRateShopCall.createTextNode(String.valueOf(dItemWeight * dqty)));
						elePackage.appendChild(eleWeight);

						Element eleLength=inDocRateShopCall.createElement("Length");
						eleLength.appendChild(inDocRateShopCall.createTextNode(String.valueOf(dItemLength * dqty)));
						elePackage.appendChild(eleLength);

						Element eleHeight=inDocRateShopCall.createElement("Height");
						eleHeight.appendChild(inDocRateShopCall.createTextNode(String.valueOf(dItemHeight)));
						elePackage.appendChild(eleHeight);

						Element eleWidth=inDocRateShopCall.createElement("Width");
						eleWidth.appendChild(inDocRateShopCall.createTextNode(String.valueOf(dItemWidth)));
						elePackage.appendChild(eleWidth);
					}
				}

				elePackages.appendChild(elePackage);
				elePierbridgeRateShopRequest.appendChild(elePackages);

				Element eleUserName=inDocRateShopCall.createElement("UserName");
				eleUserName.appendChild(inDocRateShopCall.createTextNode("STR005"));
				elePierbridgeRateShopRequest.appendChild(eleUserName);

				log.verbose("Agile Input:"+XMLUtil.getXMLString(inDocRateShopCall));

				invokeAgileRateShopCall(env,inDocRateShopCall, inXML);

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			unHoldOrderLinesWithUnChnageShipNode(env,  inXML);

		}

	}

	private  void invokeAgileRateShopCall(YFSEnvironment env, Document inDocRateShopCall,Document inXML) throws Exception, Exception {

		try {
			Document outDocFromRateShopCall = AcademyUtil.invokeService(env, "AcademyAgileRateServiceForSourcingOpt",inDocRateShopCall);
			Element eleStatus=(Element) outDocFromRateShopCall.getDocumentElement().getElementsByTagName("Status").item(0);
			Element eleDescription=(Element) eleStatus.getElementsByTagName("Description").item(0);

			if(eleDescription.getTextContent().equalsIgnoreCase("Success")){
				Element eleShipping=(Element) outDocFromRateShopCall.getDocumentElement().getElementsByTagName("Shipping").item(0);
				Element eleTotalCharge=(Element) eleShipping.getElementsByTagName("TotalCharge").item(0);
				alRateShopCharges.add(Float.valueOf(eleTotalCharge.getTextContent()));

				log.verbose("Agile Output:"+XMLUtil.getXMLString(outDocFromRateShopCall));
			}
			else
			{
				log.verbose("RateShop Call Failed to Give Charges:: Again Trying without FileterMode ");
				boolean blCheckFedex=false;

				Element elePierbridgeRateShopRequest=(Element) inDocRateShopCall.getDocumentElement();
				Element eleFilterMode=(Element) inDocRateShopCall.getDocumentElement().getElementsByTagName("FilterMode").item(0);

				XMLUtil.removeChild(elePierbridgeRateShopRequest, eleFilterMode);

				log.verbose("Agile Input without Filter Mode:"+XMLUtil.getXMLString(inDocRateShopCall));
				Document outDocFromRateShopCallNoFilterMode = AcademyUtil.invokeService(env, "AcademyAgileRateServiceForSourcingOpt",inDocRateShopCall);
				log.verbose("Agile Output without Filter Mode:"+XMLUtil.getXMLString(outDocFromRateShopCallNoFilterMode));

				NodeList nlRate=outDocFromRateShopCallNoFilterMode.getDocumentElement().getElementsByTagName("Rate");
				for(int i=0;i<nlRate.getLength();i++){	
					Element eleRate=(Element) nlRate.item(i);
					Element eleRateStatus=(Element) eleRate.getElementsByTagName("Status").item(0);
					Element eleRateDescription=(Element) eleRateStatus.getElementsByTagName("Description").item(0);
					if(eleRateDescription.getTextContent().equalsIgnoreCase("Success")){
						Element eleCarrier=(Element) eleRate.getElementsByTagName("Carrier").item(0);
						Element eleCarrierDesc=(Element) eleCarrier.getElementsByTagName("Description").item(0);
						if(eleCarrierDesc.getTextContent().equalsIgnoreCase("FedEx Server")){
							Element eleCustomer=(Element) eleRate.getElementsByTagName("Customer").item(0);
							Element eleCusTotalCharge=(Element) eleCustomer.getElementsByTagName("TotalCharge").item(0);
							alRateShopCharges.add(Float.valueOf(eleCusTotalCharge.getTextContent()));
							blCheckFedex=true;
							break;
						}
					}

				}
				if(!blCheckFedex){
					alRateShopCharges.add(Float.valueOf("0.00"));
				}	
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			unHoldOrderLinesWithUnChnageShipNode(env,  inXML);
			//e.printStackTrace();
		}

	}

	private  void AgileRateShopCall(Document inXML,YFSEnvironment env) throws Exception
	{

		try {
			for (int i = 0; i < alRateShopCharges.size(); i = i + lhsAloption.size()) {
				float temp = 0;
				for (int j = i; j < i + lhsAloption.size(); j++) {
					temp += alRateShopCharges.get(j);
				}
				alFinalRateShopCharges.add(temp);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			unHoldOrderLinesWithUnChnageShipNode(env,  inXML);
//			e.printStackTrace();
		}


	}

	private  Document preparegetOrganizationListInput(ArrayList<String> alOptionShipNodeFinal,Document inXML,YFSEnvironment env) throws Exception {

		Document inDocgetOrgList = null;
		try {
			inDocgetOrgList = XMLUtil.createDocument("Organization");
			Element inEleOrgListList=inDocgetOrgList.getDocumentElement();
			inEleOrgListList.setAttribute("IsNode", "Y");
			Element eleNode=inDocgetOrgList.createElement("Node");
			Element eleComplexQry=inDocgetOrgList.createElement("ComplexQuery");
			eleComplexQry.setAttribute("Operator", "AND");
			Element eleAnd=inDocgetOrgList.createElement("And");
			Element eleOr=inDocgetOrgList.createElement("Or");

			for(int i=2;i<alOptionShipNodeFinal.size();i=i+3){
				Element eleExp=inDocgetOrgList.createElement("Exp");
				eleExp.setAttribute("Name", "ShipNode");
				eleExp.setAttribute("Value", alOptionShipNodeFinal.get(i));
				eleExp.setAttribute("ShipNodeQryType", "EQ");
				eleOr.appendChild(eleExp);
			}
			eleAnd.appendChild(eleOr);
			eleComplexQry.appendChild(eleAnd);
			eleNode.appendChild(eleComplexQry);
			inEleOrgListList.appendChild(eleNode);


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			unHoldOrderLinesWithUnChnageShipNode(env,  inXML);
			//e.printStackTrace();
		}

		return inDocgetOrgList;
	}

	private  Document preparegetItemListInput(ArrayList<String> alShipNode2,Document inXML,YFSEnvironment env) throws Exception {


		Document inDocgetItemList = null;
		try {
			inDocgetItemList = XMLUtil.createDocument("Item");
			Element inEleItemListList=inDocgetItemList.getDocumentElement();
			Element eleComplexQry=inDocgetItemList.createElement("ComplexQuery");
			eleComplexQry.setAttribute("Operator", "AND");
			Element eleAnd=inDocgetItemList.createElement("And");
			Element eleOr=inDocgetItemList.createElement("Or");

			for(int i=0;i<alShipNode.size();i=i+6){
				Element eleExp=inDocgetItemList.createElement("Exp");
				eleExp.setAttribute("Name", "ItemID");
				eleExp.setAttribute("Value", alShipNode.get(i));
				eleExp.setAttribute("ItemIDQryType", "EQ");
				eleOr.appendChild(eleExp);
			}
			eleAnd.appendChild(eleOr);
			eleComplexQry.appendChild(eleAnd);
			inEleItemListList.appendChild(eleComplexQry);


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			unHoldOrderLinesWithUnChnageShipNode(env,  inXML);

		}
		return inDocgetItemList;
	}

	public  void getDistanceFromSurroundingNodeListForThreshold(
			YFSEnvironment env, ArrayList<String> alShipNode, Document inXML)
	throws Exception {

		try {
			for (int z = 0; z < alShipNode.size(); z = z + 6) {

				Document docGetSurroundingNodeListInput = XMLUtil
				.createDocument("GetSurroundingNodeList");
				Element eleGetSurroundingNodeListInput = docGetSurroundingNodeListInput
				.getDocumentElement();
				eleGetSurroundingNodeListInput.setAttribute("OrganizationCode",
				"DEFAULT");
				eleGetSurroundingNodeListInput.setAttribute(
						"DistanceToConsiderUOM", "MILE");
				eleGetSurroundingNodeListInput.setAttribute("DistanceToConsider",
						String.valueOf(floatThresholdDistance));
				Element eleShipToAddr = docGetSurroundingNodeListInput
				.createElement("ShipToAddress");
				eleShipToAddr.setAttribute("Country", alShipNode.get(z + 3));
				eleShipToAddr.setAttribute("State", alShipNode.get(z + 4));
				eleShipToAddr.setAttribute("ZipCode", alShipNode.get(z + 2));
				eleGetSurroundingNodeListInput.appendChild(eleShipToAddr);

				Document docGetSurroundingNodeList = AcademyUtil.invokeAPI(env, "getSurroundingNodeList",
						docGetSurroundingNodeListInput);
				mapGetSurrondingList.put(alShipNode.get(z + 2),
						docGetSurroundingNodeList);
				NodeList nlNode = docGetSurroundingNodeList.getDocumentElement()
				.getElementsByTagName("Node");
				for (int j = 1; j < alShipNode.size(); j = j + 6) {
					for (int i = 0; i < nlNode.getLength(); i++) {
						Element eleNode = (Element) nlNode.item(i);
						if (eleNode.getAttribute("ShipnodeKey").equalsIgnoreCase(
								alShipNode.get(j))) {
							floatThresholdDistance += Float.valueOf(eleNode
									.getAttribute("DistanceFromShipToAddress"));
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			unHoldOrderLinesWithUnChnageShipNode(env,  inXML);
		}

	}

	public  void findInventory(YFSEnvironment env,
			Document docInputFindInventory, Document inXML) throws Exception {

		log.verbose("Entering the findInventory method");
		String hasAnyUnavailableQuant = "";
		try {
			docInputFindInventory.getDocumentElement().setAttribute(
					"DistanceToConsider",
					String.valueOf(floatThresholdDistance));

			log.verbose("Find Inventory Input XML:"+XMLUtil.getXMLString(docInputFindInventory));

			Document outDocFindInv = AcademyUtil.invokeAPI(env, "findInventory",
					docInputFindInventory);

			log.verbose("FindInventory Output XML :: " + XMLUtil.getXMLString(outDocFindInv));

			Element eleSuggestedOption = (Element) outDocFindInv
			.getDocumentElement().getElementsByTagName(
					"SuggestedOption").item(0);

			if (!YFCObject.isNull(eleSuggestedOption)) {
				Element eleOption = (Element) eleSuggestedOption
				.getElementsByTagName("Option").item(0);

				if(!YFCObject.isNull(eleOption)){
					iTotalShipments = Integer.parseInt(eleOption
							.getAttribute("TotalShipments"));
					if(eleOption.hasAttribute("HasAnyUnavailableQty"))
					{
					hasAnyUnavailableQuant  = eleOption.getAttribute("HasAnyUnavailableQty");
					}
					ArrayList<String> alSuggestedOptionShipNode = new ArrayList<String>();
					if (intNumberOfStore >= iTotalShipments && !"Y".equalsIgnoreCase(hasAnyUnavailableQuant) ) {
						NodeList nlPromiseLine = eleSuggestedOption
						.getElementsByTagName("PromiseLine");
						for (int j = 0; j < nlPromiseLine.getLength(); j++) {
							Element elePromiseLine = (Element) nlPromiseLine.item(j);
							if (elePromiseLine != null) {
								Element eleAssignment = (Element) elePromiseLine
								.getElementsByTagName("Assignment").item(0);
								if (StringUtil.isEmpty(eleAssignment
										.getAttribute("ShipNode"))) {
									alNeglectOptimizationLines.add(elePromiseLine
											.getAttribute("LineId"));
								} else {
									alSuggestedOptionShipNode.add(elePromiseLine
											.getAttribute("LineId"));
									alSuggestedOptionShipNode.add(elePromiseLine
											.getAttribute("ItemID"));
									alSuggestedOptionShipNode.add(eleAssignment
											.getAttribute("ShipNode"));
								}
							}
						}
						alOptionShipNodeFinal.addAll(0, alSuggestedOptionShipNode);
						Element eleOptions = (Element) outDocFindInv
						.getDocumentElement().getElementsByTagName("Options")
						.item(0);
						NodeList nlOpOption = eleOptions.getElementsByTagName("Option");
						if(!YFCObject.isNull(nlOpOption)){
							for (int i = 0; i < nlOpOption.getLength(); i++) {
								Element eleOpOption = (Element) nlOpOption.item(i);
								if(!YFCObject.isNull(eleOpOption)){
									int iOptionTotalShpipments = Integer.parseInt(eleOpOption
											.getAttribute("TotalShipments"));
									if (iTotalShipments == iOptionTotalShpipments) {
										NodeList nlOpPromiseLine = eleOpOption
										.getElementsByTagName("PromiseLine");
										for (int j = 0; j < nlOpPromiseLine.getLength(); j++) {
											Element eleOpPromiseLine = (Element) nlOpPromiseLine
											.item(j);
											if (eleOpPromiseLine != null) {
												Element eleOpAssignment = (Element) eleOpPromiseLine
												.getElementsByTagName("Assignment")
												.item(0);
												if (!StringUtil.isEmpty(eleOpAssignment
														.getAttribute("ShipNode"))) {
													alOptionShipNodeFinal.add(eleOpPromiseLine
															.getAttribute("LineId"));
													alOptionShipNodeFinal.add(eleOpPromiseLine
															.getAttribute("ItemID"));
													alOptionShipNodeFinal.add(eleOpAssignment
															.getAttribute("ShipNode"));
												}
											}
										}
									}
								}
							}
						}
						if(strloginfo.equalsIgnoreCase("Y")){
							log.info("Ouput of available Options : After findInventory");
							for(int p = 0; p < alOptionShipNodeFinal.size() ; p ++){
								log.info(alOptionShipNodeFinal.get(p));
							}
						}
						for(int q=0;q<alOptionShipNodeFinal.size();q=q+3)
						{
							lhsAloption.add(alOptionShipNodeFinal.get(q));

						}
					} else {
						log.verbose("Need to implement Unhold the all the Order lines");
						blUnholdAllOrdLines=true;
					}
				}
				else {
					blUnholdAllOrdLines=true;
				}
			}
			else {
				blUnholdAllOrdLines=true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			unHoldOrderLinesWithUnChnageShipNode(env,  inXML);
		}



	}

	public  void getDistanceFromSurroundingNodeListForDistance(
			YFSEnvironment env, ArrayList<String> alOptionShipNodeFinal,
			Document inXML, HashMap<String, Document> mapGetSurrondingList)
	throws Exception {

		try {
			Element eleinDoc = (Element) inXML.getElementsByTagName("Order")
			.item(0);
			NodeList nlOrderLine = eleinDoc.getElementsByTagName("OrderLine");

			for (int z = 0; z < alOptionShipNodeFinal.size(); z = z + 3) {

				for (int i = 0; i < nlOrderLine.getLength(); i++) {
					Element eleOrderLine = (Element) nlOrderLine.item(i);
					if (eleOrderLine.getAttribute("PrimeLineNo").equalsIgnoreCase(
							alOptionShipNodeFinal.get(z))) {

						Element elePersonInfo = ((Element) eleOrderLine
								.getElementsByTagName("PersonInfoShipTo").item(0));
						String strZipCode = elePersonInfo.getAttribute("ZipCode");
						if (mapGetSurrondingList.containsKey(strZipCode)) {
							Document docGetSurroundingDoc = mapGetSurrondingList
							.get(strZipCode);
							Element eleNodeList = docGetSurroundingDoc
							.getDocumentElement();
							NodeList nlNode = eleNodeList
							.getElementsByTagName("Node");
							for (int j = 0; j < nlNode.getLength(); j++) {
								Element eleNode = (Element) nlNode.item(j);
								if (eleNode.getAttribute("ShipnodeKey")
										.equalsIgnoreCase(
												alOptionShipNodeFinal.get(z + 2))) {
									//Fix:EFP-83 adding node to distance array
									alDistance.add (alOptionShipNodeFinal.get(z + 2));
									alDistance.add(eleNode.getAttribute("DistanceFromShipToAddress"));
								}
							}
						}
					}

				}

			}
			//Fix:EFP-83 
			 //don't need alFinallDistance.
			/*
			for (int i = 0; i < alDistance.size(); i = i + lhsAloption.size()) {
				float temp = 0;
				for (int j = i; j < i + lhsAloption.size(); j++) {
					temp += alDistance.get(j);
				}
				alFinallDistance.add(String.valueOf(temp));
			}*/
		} catch (Exception e) {
			// TODO Auto-generated catch block				
			//e.printStackTrace();
			unHoldOrderLinesWithUnChnageShipNode(env,  inXML);
		}


	}

	public  void getShipNodeInventory(YFSEnvironment env,
			ArrayList<String> alOptionShipNodeFinal,Document inXML) throws Exception {
		float floatInventory = 0;
		try {
			for (int i = 1; i < alOptionShipNodeFinal.size(); i = i + 3) {

				Document docGetShipNodeInventoryInput = XMLUtil
				.createDocument("getShipNodeInventory");
				Element eleGetShipNodeInventory = docGetShipNodeInventoryInput
				.getDocumentElement();
				eleGetShipNodeInventory.setAttribute("ConsiderAllNodes", "Y");
				eleGetShipNodeInventory.setAttribute("DistributionRuleId", "");
				eleGetShipNodeInventory.setAttribute("ItemID",
						alOptionShipNodeFinal.get(i));
				eleGetShipNodeInventory.setAttribute("Node", alOptionShipNodeFinal
						.get(i + 1));
				eleGetShipNodeInventory.setAttribute("OrganizationCode", "DEFAULT");
				eleGetShipNodeInventory.setAttribute("ProductClass", "GOOD");
				eleGetShipNodeInventory.setAttribute("UnitOfMeasure", "EACH");
				//Start-Fix for Sourcing optimization issue
				try{
				Document outDocgetShipNodeInv = AcademyUtil.invokeAPI(env, "getShipNodeInventory",
						docGetShipNodeInventoryInput);
				Element eleShipNode = (Element) outDocgetShipNodeInv
				.getDocumentElement().getElementsByTagName("ShipNode")
				.item(0);
				floatInventory = Float.valueOf(eleShipNode
						.getAttribute("TotalSupply"))
						- Float
						.valueOf(eleShipNode
								.getAttribute("TotalDemand"));
				}catch(Exception e)
				{
					floatInventory = (float)0;
				}
				
				alInventory.add(alOptionShipNodeFinal
						.get(i + 1));
				alInventory.add(alOptionShipNodeFinal
						.get(i));
				alInventory.add(alOptionShipNodeFinal
						.get(i - 1));			
				//End-Fix for Sourcing optimization issue
				alInventory.add(String.valueOf(floatInventory));

			}
			//for (int i = 0; i < alInventory.size(); i = i + lhsAloption.size()) 
			for(int i=0;i<alInventory.size();i=i+4){
				float temp = 0;

				for (int j = i; j < i + lhsAloption.size(); j++) {
					temp += Float.parseFloat(alInventory.get(j));

				}
				alFinalInventory.add(temp);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			e.printStackTrace();
			unHoldOrderLinesWithUnChnageShipNode(env,  inXML);
		}


	}

	public  void getRank(YFSEnvironment env,
			ArrayList<String> alOptionShipNodeFinal,Document inXML) throws Exception {

		try {
			for (int z = 1; z < alOptionShipNodeFinal.size(); z = z + 3) {
				for (int i = 0; i < nlShipNodeList.getLength(); i++) {
					Element shipNodeEle = (Element) nlShipNodeList.item(i);

					if (shipNodeEle.getAttribute("ShipNode").equalsIgnoreCase(
							alOptionShipNodeFinal.get(z + 1))) {
						//Fix:EFP-83 adding node id to rank array
						alRank.add(shipNodeEle.getAttribute("ShipNode"));
						alRank.add(shipNodeEle.getAttribute("Rank"));
					}
				}
			}
			//EFP-83 : Not using an of the Final arrays
			/*for (int i = 0; i < alRank.size(); i = i + lhsAloption.size()) {
				float temp = 0;

				for (int j = i; j < i + lhsAloption.size(); j++) {
					temp += alRank.get(j);

				}
				alFinalRank.add(temp);
			}*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.verbose(e);
			e.printStackTrace();
			unHoldOrderLinesWithUnChnageShipNode(env,  inXML);
		}


	}

	public  Document optimize(Document inXML,YFSEnvironment env)throws Exception {

		Document finalOutDoc = null;
		try {
			ArrayList<String> alfinalOut = new ArrayList<String>();
			//alFinallDistance.add(0, Float.valueOf("0"));
			alFinalRateShopCharges.add(0, Float.valueOf("0"));
			//alFinalRank.add(0, Float.valueOf("0"));
			//alFinalInventory.add(0, Float.valueOf("0"));
			//alFinalHybrid.add(0,Float.valueOf("0"));
			if(strAgileReq.equalsIgnoreCase("Y")){
				//Fix for handling IndexOutOfBound exception
				//for (int i = 0; i < alFinalInventory.size(); i++) {
				//Start - Fix for Sourcing Optimization issue
				//for (int i = 1; i < alFinalInventory.size(); i++) 
				//for (int i = 0; i < alInventory.size()-1; i++)
				//Fix:EFP-83 discontinued use of all *Final* arrays. Using originals with node instead.
				for(int i=0;i<alInventory.size();i=i+4){
					log.verbose("alInventory loop count::"+i);	
				
					//Fix:EFP-83 various. 
					
					int nodeIndexForRank = alRank.indexOf(alInventory.get(i));
						
					float hybrid_cal = (float) floatInventoryConstant
					* Float.parseFloat(alInventory.get(i+1)) + floatAgileConstant
					* alFinalRateShopCharges.get(i) + floatRankConstant
					* Float.parseFloat(alRank.get(nodeIndexForRank+1));
					alFinalHybrid.add(hybrid_cal);

				}
				//End - Fix EFP-83 for Sourcing Optimization issue
			}
			else
			{					
				//Fix for handling IndexOutOfBound exception
				//for (int i = 0; i < alFinalInventory.size(); i++) {
				//Start - Fix for Sourcing Optimization issue
				//for (int i = 1; i < alFinalInventory.size(); i++) 
				//for (int i = 0; i < alInventory.size()-1; i++) 
				for(int i=0;i<alInventory.size();i=i+4){
					log.verbose("alInventory loop count::"+i);	
					
					//Fix:EFP-83 various.
					int nodeIndexForRank = alRank.indexOf(alInventory.get(i));
					int nodeIndexForDistance = alDistance.indexOf(alInventory.get(i));
					
					float hybrid_cal = (float) floatInventoryConstant
					* Float.parseFloat(alInventory.get(i)) + floatDistanceConstant
					* Float.parseFloat(alDistance.get(nodeIndexForDistance+1)) + floatRankConstant
					* Float.parseFloat(alRank.get(nodeIndexForDistance+1));
					alFinalHybrid.add(hybrid_cal);

				}
			}
			//End - Fix EFP-83 for Sourcing Optimization issue

			log.verbose("Size of the array alFinalHybrid is" + alFinalHybrid.size());
			log.verbose("Printing the elements of alfinahybrid arrayalist");
			for(int p = 0; p < alFinalHybrid.size() ; p ++){
				log.info(alFinalHybrid.get(p));
			}
			/*int i = alFinalHybrid.indexOf(Collections.max(alFinalHybrid));
                log.verbose("Value of i is"+ i);

				log.verbose("Value of lhsAloption.size() is" + lhsAloption.size());

				for (int j = (i) * 3 * lhsAloption.size() - 1; j > (i) * 3* lhsAloption.size() - 1 - 3 * lhsAloption.size(); j--) {
					log.verbose("Value of j is "+j);
					alfinalOut.add(alOptionShipNodeFinal.get(j));
				}*/
			//start - Fix EFP-83 for Sourcing Optimization issue
			for(int i=0;i<alInventory.size();i=i+4)
			{
				log.verbose("Inside optimze() alInventory loop is::"+i);
				int j= alfinalOut.indexOf(alInventory.get(i+1));
				log.verbose("j is::"+j);
				if(j>=0)
				{					
					if(Float.parseFloat(alfinalOut.get(j+2)) < alFinalHybrid.get(i/4))
					{
						log.verbose("Item already exist in alfinalOut:Updating the same record");
						alfinalOut.set(j-1,(alInventory.get(i)));						
						alfinalOut.set(j+2, String.valueOf((alFinalHybrid.get(i/4))));
					}
				}else
				{
					log.verbose("New Item added to the alfinalOut");
					alfinalOut.add(String.valueOf(alInventory.get(i)));
					alfinalOut.add(String.valueOf(alInventory.get(i+1)));
					alfinalOut.add(String.valueOf(alInventory.get(i+2)));
					alfinalOut.add(String.valueOf(alFinalHybrid.get(i/4)));
				}

			}

			for( int k =0; k<alfinalOut.size();k++)
			{
				log.verbose("Final computed options -alfinalOut is::"+alfinalOut.get(k));
			}
			finalOutDoc = XMLUtil.createDocument("OrderLines");
			Element finalEle = finalOutDoc.getDocumentElement();
			for (int z = 0; z < alfinalOut.size(); z = z + 4) {
				Element finalOrdEle = finalOutDoc.createElement("OrderLine");
				finalOrdEle.setAttribute("ShipNode", alfinalOut.get(z));
				finalOrdEle.setAttribute("ItemId", alfinalOut.get(z + 1));
				finalOrdEle.setAttribute("PrimeLineNo", alfinalOut.get(z + 2));
				finalEle.appendChild(finalOrdEle);
				if(strloginfo.equalsIgnoreCase("Y"))
					log.info("ShipNode :"+alfinalOut.get(z)+" ItemId : "+alfinalOut.get(z + 1)+" PrimeLineNo :"+alfinalOut.get(z + 2));
			}
		//End - Fix EFP-83 for Sourcing Optimization issue
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			log.verbose(e);

			unHoldOrderLinesWithUnChnageShipNode(env,  inXML);
		}

		return finalOutDoc;
	}

	private  void unHoldOrderLinesWithUnChnageShipNode(YFSEnvironment env, Document inXML) throws Exception {

		try {
			boolean blchangeOrder2Req=false;

			Element eleOrder=(Element) inXML.getDocumentElement().getElementsByTagName("Order").item(0);
			String strOrdHeaderKey=eleOrder.getAttribute("OrderHeaderKey");

			Document inDocgetOrderList=XMLUtil.createDocument("Order");
			Element eleinDoc=inDocgetOrderList.getDocumentElement();
			eleinDoc.setAttribute("OrderHeaderKey", strOrdHeaderKey);

			Document getOrderListOutDoc = AcademyUtil.invokeService(env, "AcademygetOrderListForSourcing",inDocgetOrderList);


			Document changeOrder2InDoc=XMLUtil.createDocument("Order");
			Element elechangeOrder2InDoc=changeOrder2InDoc.getDocumentElement();
			elechangeOrder2InDoc.setAttribute("Override", "Y");
			elechangeOrder2InDoc.setAttribute("OrderHeaderKey", strOrdHeaderKey);

			Element eleChgOrd2Lines=changeOrder2InDoc.createElement("OrderLines");

			NodeList nlOrderHoldType=getOrderListOutDoc.getDocumentElement().getElementsByTagName("OrderHoldType");
			if(nlOrderHoldType.getLength()>0){
				for(int i=0;i<nlOrderHoldType.getLength();i++){
					Element eleOrderHoldType=(Element) nlOrderHoldType.item(i);
					if (!YFCObject.isNull(eleOrderHoldType)) {
						String strHoldType=eleOrderHoldType.getAttribute("HoldType");
						String strStatus=eleOrderHoldType.getAttribute("Status");
						if(strHoldType.equalsIgnoreCase("ACADEMY_SCHREL_HOLD") && strStatus.equalsIgnoreCase("1100")){

							Element eleChgOrd2Line=changeOrder2InDoc.createElement("OrderLine");
							eleChgOrd2Line.setAttribute("OrderLineKey", eleOrderHoldType.getAttribute("OrderLineKey"));
							eleChgOrd2Line.setAttribute("Action", "MODIFY");
							Element eleOrder2HoldTypes=changeOrder2InDoc.createElement("OrderHoldTypes");
							Element eleOrder2HoldType=changeOrder2InDoc.createElement("OrderHoldType");
							eleOrder2HoldType.setAttribute("ReasonText", "ShipNodeCheck");
							eleOrder2HoldType.setAttribute("Status", "1300");
							eleOrder2HoldType.setAttribute("HoldType", "ACADEMY_SCHREL_HOLD");
							eleOrder2HoldTypes.appendChild(eleOrder2HoldType);
							eleChgOrd2Line.appendChild(eleOrder2HoldTypes);
							eleChgOrd2Lines.appendChild(eleChgOrd2Line);
							blchangeOrder2Req=true;
						}

					}
				}

			}

			elechangeOrder2InDoc.appendChild(eleChgOrd2Lines);

			if(blchangeOrder2Req){
				log.verbose("ChangeOrder Input XML:"+XMLUtil.getXMLString(changeOrder2InDoc));
				AcademyUtil.invokeAPI(env, "changeOrder", changeOrder2InDoc);
			}
			log.info ("Optimization not possible because of the ZipCode");

			log.verbose("Releasing the OrderLines with unchanged ShipNode");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.verbose(e);
			e.printStackTrace();
		}

	}


	private Document getInputForScheduleORderines(Document inXML,Document finalDocForOpt,ArrayList<String> alNeglectOptimizationLines){

		Document inDoc = null;
		log.verbose("Entering the getInputForScheduleORderines");

		try{

			inDoc = XMLUtil.createDocument("Promise");
			inDoc.getDocumentElement().setAttribute("AllocationRuleID", "SYSTEM");
			inDoc.getDocumentElement().setAttribute("CheckInventory", "N");
			inDoc.getDocumentElement().setAttribute("DocumentType", "0001");
			inDoc.getDocumentElement().setAttribute("EnterpriseCode", "Academy_Direct");
			inDoc.getDocumentElement().setAttribute("IgnoreReleaseDate", "Y");
			inDoc.getDocumentElement().setAttribute("ScheduleAndRelease", "N");

			Element eleOrder       = (Element) inXML.getDocumentElement().getElementsByTagName("Order").item(0);
			String strOrdHeaderKey = eleOrder.getAttribute("OrderHeaderKey");
			String strOrderNo      = eleOrder.getAttribute("OrderNo");
			inDoc.getDocumentElement().setAttribute("OrderHeaderKey", strOrdHeaderKey);
			inDoc.getDocumentElement().setAttribute("OrderNo",strOrderNo);

			Element promiseLinesElement = inDoc.createElement("PromiseLines");
			inDoc.getDocumentElement().appendChild(promiseLinesElement);

			NodeList orderLineElementList = inXML.getElementsByTagName("OrderLine");

			for(int i = 0; i < orderLineElementList.getLength() ; i++){				

				Element orderLineElement = (Element) orderLineElementList.item(i);
				String strOrderLineKey   =  orderLineElement.getAttribute("OrderLineKey");
				String strQuantity      =  orderLineElement.getAttribute("RemainingQty");  // OriginalOrderedQty
				String primeLineNo        = orderLineElement.getAttribute("PrimeLineNo");
				String subLineNo        = orderLineElement.getAttribute("SubLineNo");
				Element promiseLineElement  = inDoc.createElement("PromiseLine");

				promiseLineElement.setAttribute("FromStatus", "1310");
				promiseLineElement.setAttribute("ToStatus", "1500");
				promiseLineElement.setAttribute("OrderLineKey",strOrderLineKey);
				promiseLineElement.setAttribute("PrimeLineNo", primeLineNo);
				promiseLineElement.setAttribute("Quantity", strQuantity);

				promiseLineElement.setAttribute("SubLineNo", subLineNo);

				//finding the right shipnode
				NodeList nlOrdLineFromOpt=finalDocForOpt.getDocumentElement().getElementsByTagName("OrderLine");
				for(int j=0;j<nlOrdLineFromOpt.getLength();j++) {	
					Element eleOrdLineFromOpt = (Element) nlOrdLineFromOpt.item(j);
					String strPrimeLineNoOpt  = eleOrdLineFromOpt.getAttribute("PrimeLineNo");
					if (!YFCObject.isNull(eleOrdLineFromOpt)) {
						if(!alNeglectOptimizationLines.contains(strPrimeLineNoOpt) && primeLineNo.equalsIgnoreCase(strPrimeLineNoOpt)) {
							promiseLineElement.setAttribute("ShipNode", eleOrdLineFromOpt.getAttribute("ShipNode"));		
						}
					}
				}	

				Element scheduleElement   = (Element) orderLineElement.getElementsByTagName("Schedule").item(0);
				String  strDeliveryDate   = scheduleElement.getAttribute("ExpectedDeliveryDate");
				String  strShipDate       = scheduleElement.getAttribute("ExpectedShipDate");
				promiseLineElement.setAttribute("ShipDate", strShipDate);
				promiseLineElement.setAttribute("DeliveryDate", strDeliveryDate);
				if(promiseLineElement.getAttribute("ShipNode") != null && !promiseLineElement.getAttribute("ShipNode").isEmpty()){
					promiseLinesElement.appendChild(promiseLineElement);
				}

			}

		}catch(Exception e){
			log.verbose(e);
			//e.printStackTrace();
		}

		log.verbose("Prepapred the input doc for scheduleOrderLines" + XMLUtil.getXMLString(inDoc));

		return inDoc;
	}

}
