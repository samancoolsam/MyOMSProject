package com.academy.ecommerce.sterling.sourcing;


import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;



import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySourcingOptimizationForHold {

	private Properties props;

	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	private static YFCLogCategory log = YFCLogCategory.instance(AcademySourcingOptimizationForHold.class);

	public void processOnSuccessScheduleForHold(YFSEnvironment env, Document inXML) throws Exception {

		log.beginTimer(" Begining of AcademySourcingOptimizationForHold -> processOnSuccessScheduleForHold");

		String strOptTurnOffFlg = null;
		String strOrdHeadKey = null;
		Document outDocRankTable = null;
		NodeList nlOrdLine = null;
		Element eleOrdLine = null;
		NodeList nlShipNodeRank = null;
		Set<String> setShipNodeList = new TreeSet<String>();
		ArrayList<Element> alOrdEle = new ArrayList<Element>();
		ArrayList<String> alElgNodeHold = new ArrayList<String>();
		boolean chngOrdReqFlag = false;

		strOptTurnOffFlg = props.getProperty("OPTIMIZATION_TURN_OFF_FLAG");
		if (strOptTurnOffFlg.equalsIgnoreCase("N")) {
			log.debug("Optimization Flag is ON #####");
			strOrdHeadKey = inXML.getDocumentElement().getAttribute("OrderHeaderKey");
			nlOrdLine = inXML.getDocumentElement().getElementsByTagName("OrderLine");

			if (!YFCObject.isNull(nlOrdLine)) {
				for (int i = 0; i < nlOrdLine.getLength(); i++) {
					eleOrdLine = (Element) nlOrdLine.item(i);
					if (!YFCObject.isNull(eleOrdLine)) {
						String strMinLineStatus = eleOrdLine.getAttribute("MinLineStatus");						
						if (!YFCObject.isNull(strMinLineStatus) && !YFCObject.isVoid(strMinLineStatus)){
							log.debug("MinLineStatus::"+strMinLineStatus);							
							if ((Integer.parseInt(strMinLineStatus.substring(0,4))<= 1500) 
									&& !((("BOPIS").equalsIgnoreCase(eleOrdLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE))
									|| ("SOF").equalsIgnoreCase(eleOrdLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE)))
									|| ("PICK".equalsIgnoreCase(eleOrdLine.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD))))) {
								
								NodeList nlOrderStatus = eleOrdLine.getElementsByTagName("OrderStatus");
								if(null != nlOrderStatus){
									for(int j=0; j<nlOrderStatus.getLength(); j++){										
										Element eleOrdStatus = (Element) nlOrderStatus.item(j);										
										if(!YFCObject.isNull(eleOrdStatus)){					
											log.debug("OrderStatus::"+XMLUtil.getElementXMLString(eleOrdStatus));
											if("1500".equalsIgnoreCase(eleOrdStatus.getAttribute("Status"))){												
												alOrdEle.add(eleOrdStatus);
												setShipNodeList.add(eleOrdStatus.getAttribute("ShipNode"));
											}
										}
									}
								}								
							}
							
						}
					}
				}
			}

			Document inDocShipNodeRankList = XMLUtil.createDocument("AcadShipNodeRank");
			Element inEleShipNodeRankList = inDocShipNodeRankList.getDocumentElement();
			Element eleComplexQry = inDocShipNodeRankList.createElement("ComplexQuery");
			eleComplexQry.setAttribute("Operator", "AND");
			Element eleAnd = inDocShipNodeRankList.createElement("And");
			Element eleOr = inDocShipNodeRankList.createElement("Or");

			for (String shipNode : setShipNodeList) {
				Element eleExp = inDocShipNodeRankList.createElement("Exp");
				eleExp.setAttribute("Name", "ShipNode");
				eleExp.setAttribute("Value", shipNode);
				eleExp.setAttribute("ShipNodeQryType", "EQ");
				eleOr.appendChild(eleExp);
			}
			eleAnd.appendChild(eleOr);
			eleComplexQry.appendChild(eleAnd);
			inEleShipNodeRankList.appendChild(eleComplexQry);
			log.verbose("getRankListInput:" + XMLUtil.getXMLString(inDocShipNodeRankList));

			outDocRankTable = AcademyUtil.invokeService(env, "AcademygetShipNodeRankList", inDocShipNodeRankList);
			nlShipNodeRank = outDocRankTable.getElementsByTagName("AcadShipNodeRank");

			if (!YFCObject.isNull(nlShipNodeRank)) {
				for (int i = 0; i < nlShipNodeRank.getLength(); i++) {
					Element eleShipNode = (Element) nlShipNodeRank.item(i);
					if (eleShipNode.getAttribute("StoreFlag").equalsIgnoreCase("Y")
							&& eleShipNode.getAttribute("StoreElgFlag").equalsIgnoreCase("Y")) {
						alElgNodeHold.add(eleShipNode.getAttribute("ShipNode"));
					}
				}
			}

			//OMNI-92303 - Stop Creating AcademySourceOptHold - Start
			/*Document inDocMultiApi=XMLUtil.createDocument("MultiApi");
			Element eleMultiApi=inDocMultiApi.getDocumentElement();*/
			//OMNI-92303 - Stop Creating AcademySourceOptHold - End
			
			Document inDocChangeOrder = XMLUtil.createDocument("Order");
			Element inEleChangeOrder = inDocChangeOrder.getDocumentElement();
			inEleChangeOrder.setAttribute("Override", "Y");
			inEleChangeOrder.setAttribute("OrderHeaderKey", strOrdHeadKey);
			Element eleOrdLines = inDocChangeOrder.createElement("OrderLines");

			for (Element ordLine : alOrdEle) {
				if (alElgNodeHold.contains(ordLine.getAttribute("ShipNode"))) {
					Element eleChgOrdLine = inDocChangeOrder.createElement("OrderLine");
					eleChgOrdLine.setAttribute("OrderLineKey", ordLine.getAttribute("OrderLineKey"));
					eleChgOrdLine.setAttribute("Action", "MODIFY");

					Element eleOrdHoldTypes = inDocChangeOrder.createElement("OrderHoldTypes");
					Element eleOrdHold = inDocChangeOrder.createElement("OrderHoldType");
					eleOrdHold.setAttribute("ReasonText", "ShipNodeCheck");
					eleOrdHold.setAttribute("Status", "1100");
					eleOrdHold.setAttribute("HoldType", "ACADEMY_SCHREL_HOLD");
					eleOrdHoldTypes.appendChild(eleOrdHold);
					eleChgOrdLine.appendChild(eleOrdHoldTypes);
					eleOrdLines.appendChild(eleChgOrdLine);
					chngOrdReqFlag = true;
					//OMNI-92303 - Stop Creating AcademySourceOptHold - Start
					/*Element eleAPI=inDocMultiApi.createElement("API");
					eleAPI.setAttribute("Name", "createException");
					Element eleInput=inDocMultiApi.createElement("Input");
					Element eleInbox=inDocMultiApi.createElement("Inbox");
					eleInbox.setAttribute("ExceptionType", "AcademySourcingOptHold");
					eleInbox.setAttribute("OrderHeaderKey", strOrdHeadKey);
					eleInbox.setAttribute("OrderLineKey", ordLine.getAttribute("OrderLineKey"));
					eleInbox.setAttribute("Description", "Sourcing and Optimization Hold");
					eleInput.appendChild(eleInbox);
					eleAPI.appendChild(eleInput);
					eleMultiApi.appendChild(eleAPI);*/
					//OMNI-92303 - Stop Creating AcademySourceOptHold - End
				}
				
			}
			inEleChangeOrder.appendChild(eleOrdLines);

			if (chngOrdReqFlag) {
				log.verbose("ChangeOrderInput:" + XMLUtil.getXMLString(inDocChangeOrder));
				AcademyUtil.invokeAPI(env, "changeOrder", inDocChangeOrder);
				//OMNI-92303 - Stop Creating AcademySourceOptHold - Start
				/*log.verbose("MultiApiInput for Exception:" + XMLUtil.getXMLString(inDocMultiApi));
				AcademyUtil.invokeAPI(env, "multiApi", inDocMultiApi);*/
				//OMNI-92303 - Stop Creating AcademySourceOptHold - End
			}

		}
		log.endTimer(" Begining of AcademySourcingOptimizationForHold -> processOnSuccessScheduleForHold");

	}

}
