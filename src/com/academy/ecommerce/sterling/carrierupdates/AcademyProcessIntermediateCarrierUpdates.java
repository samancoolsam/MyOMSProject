package com.academy.ecommerce.sterling.carrierupdates;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

//EFP-21 Carrier Updates Consumption
public class AcademyProcessIntermediateCarrierUpdates extends YCPBaseAgent{
	
	private static final Logger logger = Logger.getLogger(AcademyProcessIntermediateCarrierUpdates.class.getName());
	private static HashMap<String,Integer> eligibleStatus = null;
	
	static{
		logger.verbose("Inside static block##");
		if(YFCObject.isNull(eligibleStatus)){
			logger.verbose("Setting HashMap##");
			eligibleStatus = new HashMap<String, Integer>();
			eligibleStatus.put("1600.002", 1);
			eligibleStatus.put("1600.002.51", 1);
			eligibleStatus.put("1600.002.60", 2);
			eligibleStatus.put("1600.002.70", 3);
			eligibleStatus.put("1600.002.80", 4);
			eligibleStatus.put("1600.002.90", 5);
			logger.verbose("Set HashMap##");
		}
		logger.verbose("Exit Static##");		
	}
	@Override
	public List<Document> getJobs(YFSEnvironment env, Document inXML) 
		throws Exception {
		logger.verbose("Inside AcademyProcessIntermediateCarrierUpdates getJobs.The Input xml is : "
				+ XMLUtil.getXMLString(inXML));
		
		List<Document> outputList = new ArrayList<Document>();
		NodeList nlCarrierStatusTracker = null;
		Element carrierStatusTrackerEle = null;
		String strNumOfRecords = null;
		String strFromDateRange = null;
		String strToDateRange = null;
		
		strNumOfRecords = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_NUM_RECORDS);
		strFromDateRange = inXML.getDocumentElement().getAttribute(AcademyConstants.STR_FROM_DATE_RANGE);
		strToDateRange = inXML.getDocumentElement().getAttribute(AcademyConstants.STR_TO_DATE_RANGE);
		
		
		if(YFCObject.isNull(strNumOfRecords) && 
				YFCObject.isVoid(strNumOfRecords)){
			strNumOfRecords = "500";
		}
		
		Calendar cal = Calendar.getInstance();
		Calendar cal1 = Calendar.getInstance();

		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_SIMPLE_DATE_PATTERN);//"yyyy-MM-dd"

		cal.add(Calendar.DATE, (-Integer.parseInt(strFromDateRange)));
		String strFromOrderDate = sdf.format(cal.getTime());//oldest date		

		cal1.add(Calendar.DATE, (-Integer.parseInt(strToDateRange)));
		String strToOrderDate = sdf.format(cal1.getTime());//earliest date
		
		
		Document getTracketListInDoc = XMLUtil.createDocument("AcadTrackingUpdates");
		getTracketListInDoc.getDocumentElement()
			.setAttribute(AcademyConstants.ATTR_STATUS_DATE_QRY_TYPE, 
					AcademyConstants.BETWEEN);
		getTracketListInDoc.getDocumentElement()
			.setAttribute(AcademyConstants.ATTR_FROM_STATUS_DATE, 
					strFromOrderDate+"T00:00:00");
		getTracketListInDoc.getDocumentElement()
			.setAttribute(AcademyConstants.ATTR_TO_STATUS_DATE, 
					strToOrderDate+"T23:59:59");
		getTracketListInDoc.getDocumentElement()
		.setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED,
				AcademyConstants.STR_NO);
		getTracketListInDoc.getDocumentElement()
			.setAttribute(AcademyConstants.ATTR_MAX_RECORD, 
				strNumOfRecords);
		Element orderByEle = getTracketListInDoc.createElement(AcademyConstants.ELE_ORDERBY);
		getTracketListInDoc.getDocumentElement().appendChild(orderByEle);
		Element attributeEle = getTracketListInDoc.createElement(AcademyConstants.ELE_ATTRIBUTE);
		orderByEle.appendChild(attributeEle);
		attributeEle.setAttribute(AcademyConstants.ATTR_DESC_SHORT, AcademyConstants.STR_NO);
		attributeEle.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_STATUS_DATE);
				
		logger.debug("Input Doc to get Tracker Details::"+
				XMLUtil.getXMLString(getTracketListInDoc));
		
		Document getTracketListOutDoc = AcademyUtil.invokeService(env,
				"AcademyListTrackingUpdates", getTracketListInDoc);
		if(!YFCObject.isNull(getTracketListOutDoc)){
			nlCarrierStatusTracker = getTracketListOutDoc
				.getElementsByTagName("AcadTrackingUpdates");
		}
				
		if(!YFCObject.isNull(nlCarrierStatusTracker)){
			for(int i=0;i<nlCarrierStatusTracker.getLength();i++){
				carrierStatusTrackerEle = (Element)nlCarrierStatusTracker.item(i);
				
				if(!YFCObject.isNull(carrierStatusTrackerEle)){
					outputList.add(XMLUtil.getDocumentForElement(carrierStatusTrackerEle));
				}
			}
		}
		
		return outputList;
	}
	
	@Override
	public void executeJob(YFSEnvironment env, Document input) throws Exception {
		logger.verbose("Inside AcademyProcessIntermediateCarrierUpdates executeJobs.The Input xml is : "
				+ XMLUtil.getXMLString(input));
		try{
		Element inEle = input.getDocumentElement();
		String strShipmentNo = inEle.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		String strTrackingNo = inEle.getAttribute(AcademyConstants.ATTR_TRACKING_NO);
		String strDropStats = inEle.getAttribute(AcademyConstants.ATTR_STATUS);
		String strStatusDate = inEle.getAttribute(AcademyConstants.ATTR_STATUS_DATE);
		Document getShipmentListInDoc = null;
		Document getShipmentListOutDoc = null;
		
		// Begin : OMNI-42367
		/*
		 * Changes to process carrier updates for return order.
		 */
		if (AcademyConstants.STR_RETURN_RECEIVE.equalsIgnoreCase(strDropStats) && !StringUtil.isEmpty(strTrackingNo)) {
			
			logger.verbose("Begin - Process Return Carrier Updates :: ");
			
			AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_RETURN_ORDER_TRACKING_UPDATES, input);
			
			logger.verbose("End - Process Return Carrier Updates :: ");
			return;
		}
		// End : OMNI-42367
		
		//Create input for getShipmentList
		getShipmentListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		
		if(!YFCObject.isNull(strShipmentNo) 
				&& !YFCObject.isVoid(strShipmentNo)){
			getShipmentListInDoc.getDocumentElement()
				.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		}else{
			if(!YFCObject.isNull(strTrackingNo) 
					&& !YFCObject.isVoid(strTrackingNo)){
				
				Element containersEle = getShipmentListInDoc
					.createElement(AcademyConstants.ELE_CONTAINERS);
				getShipmentListInDoc.getDocumentElement().appendChild(containersEle);
				Element containerEle = getShipmentListInDoc
					.createElement(AcademyConstants.ELE_CONTAINER);
				containerEle.setAttribute(AcademyConstants.ATTR_TRACKING_NO, strTrackingNo);
				containersEle.appendChild(containerEle);
			}
		}
		
		logger.debug("Input to API::"+XMLUtil.getXMLString(getShipmentListInDoc));
		getShipmentListOutDoc = AcademyUtil.invokeAPI(env,
				AcademyConstants.API_GET_SHIPMENT_LIST, getShipmentListInDoc);
		
		if(!YFCObject.isNull(getShipmentListOutDoc)){
			Document changeShipmentStatusInDoc = getChangeShipmentStatusInDoc(getShipmentListOutDoc,
					strDropStats, strStatusDate);
			if(!YFCObject.isNull(changeShipmentStatusInDoc)){
				
				logger.debug("Executing ChangeShipmentStatus API ##");
				
				AcademyUtil.invokeAPI(env,
						AcademyConstants.CHANGE_SHIPMENT_STATUS_API, 
							changeShipmentStatusInDoc);
				
				input.getDocumentElement().setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED, 
						AcademyConstants.STR_YES);
				modifyCarrierStatusTracker(env,input);
			}else{
				logger.info("Unable to process the update. Hence Ignoring ##"+
						XMLUtil.getXMLString(input));
				
				input.getDocumentElement().setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED, 
						AcademyConstants.STR_IGNORED);
				modifyCarrierStatusTracker(env,input);
			}
		}
		
		logger.debug("End - executeJobs() ##");
		
		}catch(Exception e){
			logger.error("Exception inside AcademyProcessIntermediateCarrierUpdates : executeJob " + e.getMessage());
			throw new YFSException(e.getMessage());						
		}
	}
	
	private Document getChangeShipmentStatusInDoc(Document shipmentListDoc, String dropStatus, String statusDate) 
		throws Exception{
		
		logger.debug("Start - getChangeShipmentStatusInDoc()##");
		Document changeShipmentStatusInDoc = null;
		Element shipmentEle = XMLUtil.getElementByXPath(shipmentListDoc, "//Shipments/Shipment");
		String strShipmentNo = shipmentEle.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		String strShipmentKey = shipmentEle.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		String strShipmentStatus = shipmentEle.getAttribute(AcademyConstants.ATTR_STATUS);
		logger.debug("Start - criteria check##");
		if(eligibleStatus.containsKey(strShipmentStatus.trim()) &&
				eligibleStatus.get(strShipmentStatus)<eligibleStatus.get(dropStatus)){
			logger.debug("criteria check passed##");
			changeShipmentStatusInDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			changeShipmentStatusInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			changeShipmentStatusInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
			changeShipmentStatusInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_TRANSID, "UpdateTrackingStatus.0001.ex");
			changeShipmentStatusInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS, dropStatus);
			Element shipmentStatusAuditEle = changeShipmentStatusInDoc.createElement("ShipmentStatusAudit");
			shipmentStatusAuditEle.setAttribute(AcademyConstants.ATTR_STATUS_DATE, statusDate);
			changeShipmentStatusInDoc.getDocumentElement().appendChild(shipmentStatusAuditEle);
			logger.debug("Input to API::"+XMLUtil.getXMLString(changeShipmentStatusInDoc));
		}
		return changeShipmentStatusInDoc;
	}
	
	public void modifyCarrierStatusTracker (YFSEnvironment env, Document inDoc)
		throws Exception{
		logger.debug("Start - modifyCarrierStatusTracker() ##");

		logger.debug("Input to API::"+XMLUtil.getXMLString(inDoc));
		AcademyUtil.invokeService(env,
			"AcademyModifyTrackingUpdates", inDoc);
	
		logger.debug("End - modifyCarrierStatusTracker() ##");

	}	
}
