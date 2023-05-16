/**************************************************************************
  * Description	    : This class is for handling Demand Updates on backorder during shortage as part of OMNI-52209 and is invoked on 
  * 				  ORDER_CHANGE.ON_ORDER_RELEASE_STATUS_CHANGE event.		
 * --------------------------------
 * 	Date             Author               
 * --------------------------------
 *  29-SEP-2021      Cognizant			 	 
 * 
 * -------------------------------------------------------------------------
 **************************************************************************/


package com.academy.ecommerce.yantriks.inventory;

import static com.academy.util.constants.AcademyConstants.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;



public class AcademyPublishDemandUpdateOnBackOrder {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyPublishDemandUpdateOnBackOrder.class);
	

	/**
	 * 
	 * This method will check if the From and To Order Release Status of the orderline are eligible for backorder against a set of FromToStatus 
	 * combinations stored in customer_overrides.properties and if eligible will post a demand XML to integration server queue for publishing
	 * demand update to Yantriks. 
	 * @param env
	 * @param inpDoc
	 * @return Document
	 * @throws Exception 
	 */
	public Document publishDemandUpdateOnBackorder(YFSEnvironment env, Document inpDoc) throws Exception  {

		String methodName = "publishDemandUpdateOnBackorder";
		log.beginTimer(methodName);
		log.verbose("Input Document to publishDemandUpdateOnBackorder method:: " + SCXmlUtil.getString(inpDoc));
		
		List<String> backOrderDmdUpdtFromToStatusList = new ArrayList<String>();
		List<String> orderLineFromToStatusList = new ArrayList<String>();
		String inputXMLTostatus="";
		String validFromToStatusForBackorder="N";
		Element eleOrder = inpDoc.getDocumentElement();
		Element eleOrderLines = SCXmlUtil.getChildElement(eleOrder, AcademyConstants.ELE_ORDER_LINES);
		NodeList nlOrderLine = eleOrderLines.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
		int orderLineLength = nlOrderLine.getLength();
		log.verbose("orderLineLength:"+orderLineLength);
		/*Fetching the combination of from To status from customer overrides properties for the various shortage scenarios 
		 * which are eligible for demand Update and store it in a list*/
		String backOrderDmdUpdtFromToStatus=YFSSystem.getProperty(BACKORDER_DMD_UPDT_FROM_TO_STATUS);
		if(!YFCObject.isVoid(backOrderDmdUpdtFromToStatus)) {
			backOrderDmdUpdtFromToStatusList = Arrays.asList(backOrderDmdUpdtFromToStatus.split(STR_COMMA));
			log.verbose("backOrderDmdUpdtFromToStatusList:"+backOrderDmdUpdtFromToStatusList);
		}
		/*Loop through the orderlines and read the FromOrderReleaseStatus and ToOrderReleaseStatus combination for each orderline and store them in a list*/
		for (int i = 0; i < orderLineLength; i++) {
				Element currInOrderLine = (Element) nlOrderLine.item(i);
				Element nodeFromOrderReleaseStatuses = SCXmlUtil.getChildElement(currInOrderLine, STR_FROM_ORDER_RELEASE_STATUSES);
				List<Element> listOfFromOrderReleaseStatus = SCXmlUtil.getChildren(nodeFromOrderReleaseStatuses,STR_FROM_ORDER_RELEASE_STATUS);
				log.verbose("listOfFromOrderReleaseStatus.size()"+listOfFromOrderReleaseStatus.size());
				String inputXMLFromstatus = XPathUtil.getString(nodeFromOrderReleaseStatuses,XPATH_ORDERLINE_FROM_STATUS);
				log.verbose("inputXMLFromstatus:"+inputXMLFromstatus);
				for (Element eleFromOrderReleaseStatus : listOfFromOrderReleaseStatus) {
						Element nodeToOrderReleaseStatuses = SCXmlUtil.getChildElement(eleFromOrderReleaseStatus, STR_TO_ORDER_RELEASE_STATUSES);
						List<Element> listOfToOrderReleaseStatus = SCXmlUtil.getChildren(nodeToOrderReleaseStatuses,STR_TO_ORDER_RELEASE_STATUS);
						for (Element eleToOrderReleaseStatus : listOfToOrderReleaseStatus) {
							inputXMLTostatus = eleToOrderReleaseStatus.getAttribute(ATTR_STATUS);
							log.verbose("inputXMLTostatus:"+inputXMLTostatus);
						}
						orderLineFromToStatusList.add(inputXMLFromstatus+"_"+inputXMLTostatus);
				}
				log.verbose("orderLineFromToStatusList:"+orderLineFromToStatusList);
		}
		/*Loop through the FromToStatusList combination for all the orderlines and check if it matches with any of the combinations 
		 *eligible for backorder demand update and if eligible set the validFromToStatusForBackorder flag as Y*/
		if (orderLineFromToStatusList != null && orderLineFromToStatusList.size() > 0){
			for (int i=0;i<orderLineFromToStatusList.size();i++) {
				String orderLineFromToStatus=orderLineFromToStatusList.get(i);
				log.verbose("orderLineFromToStatus :"+orderLineFromToStatus);
				if (backOrderDmdUpdtFromToStatusList.contains(orderLineFromToStatus)) {
					validFromToStatusForBackorder="Y";
				}
			}
		}
	
		/*If the validFromToStatusForBackorder flag is  Y after looping through all the orderlines, then we invoke the service to publish demand XML to queue */
		if (STR_YES.equals(validFromToStatusForBackorder)) {
			log.verbose("Invoke ShipmentDemandUpdate service to post Demand XML To Queue for demand update on shortage");
			AcademyUtil.invokeService(env, SERVICE_YAN_SHIPMENT_DEMAND_UPDATE, inpDoc);
		}
		
		log.endTimer(methodName);
		return inpDoc;
	}

}

