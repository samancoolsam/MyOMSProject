package com.academy.ecommerce.sterling.bopis.batch.api;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyBOPISUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @Author : Chiranthan(SapientRazorfish_)
 * @JIRA# : BOPIS-<>
 * @Date : Created on 22-Jun-2018
 * 
 * @Purpose : 
 *This class is invoked when Store associates click on 'Batch Reset' from WebStore
 *
 * As part of Batch reset, following is the backend logic -
 * 1. For all shipments in the batch which have status < 'ReadyToShip', 
 * 		- removing the shipmentLines from batch by setting StoreBatchKey to '', setting BackroomPickedQuantity='0'
 *      - removing shipment from batch by setting IncludedInBatch="N"
 *      - changeShipmentStatus to ReadyForBackroomPick
 * 2. In case any shipment in batch has status > 'ReadyToShip', then won't delete the batch.
 * 	  
 **/


public class AcademyResetBatchWrapper implements YIFCustomApi{
	
	private static Logger log = Logger.getLogger(AcademyResetBatchWrapper.class.getName());
	private Properties props;
	public void setProperties(Properties arg0) throws Exception {
		this.props = props;		
	}
	String strStoreBatchKey = null;
	
	public void resetBatch(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Entering AcademyResetBatchWrapper.resetBatch() :: "+XMLUtil.getXMLString(inDoc));
		
		Boolean bIsDeleteBatch = true;
		String strShipmentStatus = null;
		String strReadyToShipStatus = AcademyConstants.VAL_READY_TO_SHIP_STATUS;
		NodeList nlShipment = null;
		NodeList nlShipmentline = null;
		Set<String> setEligibleShipmentKeysForBatchReset = new HashSet<String>();
		Element eleShipment = null;
		Document docOutGetStoreBatchDetails  = null;
		
		Element eleInDoc = inDoc.getDocumentElement();
		try{
			strStoreBatchKey = eleInDoc.getAttribute(AcademyConstants.ATTR_STORE_BATCH_KEY);
			log.verbose("StoreBatchKey - "+strStoreBatchKey);
			
			docOutGetStoreBatchDetails = getStoreBatchDetails(env);		
			
			//nlShipment - Can have duplicate shipments, since we are fetching from ShipmentLine level. Will be using 'Set' collections to have unique shipments
			nlShipment = docOutGetStoreBatchDetails.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);

			//Loop through all the shipments
			for (int i = 0; i < nlShipment.getLength(); i++){				
				eleShipment = (Element) nlShipment.item(i);
				strShipmentStatus = eleShipment.getAttribute(AcademyConstants.ATTR_STATUS);
				//If the shipmentStatus < 'ReadyToShip', remove only those shipments from batch.
		    	if(AcademyBOPISUtil.compareStatus(strShipmentStatus, strReadyToShipStatus) < 0) 
		    	{
		    		//Unique list of Shipmentkey's which are eligible for Batch Reset
		    		setEligibleShipmentKeysForBatchReset.add(eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
		    	}else{
		    		bIsDeleteBatch = false;
		    		log.verbose("Batch deletion is disAllowed, since one of the shipment is not in eligible status!!");
		    	}
			}
			
			//For each batchReset eligible shipment's -> invoke changeShipment(to Remove shipment from batch) & changeShipmentStatus(to ReadyForBackroomPick)
			for (String strShipmentKey : setEligibleShipmentKeysForBatchReset){
				//Get ShipmentLines for each Shipment
				nlShipmentline = XPathUtil.getNodeList(docOutGetStoreBatchDetails, 
						"/StoreBatch/StoreBatchLines/StoreBatchLine/ShipmentLines/ShipmentLine[@ShipmentKey='" + strShipmentKey + "']");
				
				changeShipmentToResetBatch(env, nlShipmentline);
				
				strShipmentStatus = XMLUtil.getAttributeFromXPath(XMLUtil.getDocumentForElement((Element) nlShipmentline.item(0)), 
						AcademyConstants.XPATH_SHIPMENTLINE_SHIPMENT_STATUS);			
				if(!AcademyConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL.equals(strShipmentStatus)){
					
					changeShipmentStatusToRBP(env, strShipmentKey);
				
				}
			}
			
			// If all the shipments in the batch are below 'ReadyToShip', only then delete the batch
			if(bIsDeleteBatch){
				manageStoreBatch(env);
			}			

		}catch (Exception e) {
			//log.error(e);
			throw new YFSException("Exception Ocuured in AcademyBOPISResetBatchWrapper.resetBatch()"+e.getMessage());
		}
		
		log.verbose("Exiting AcademyResetBatchWrapper.resetBatch() :: ");
	}
	
	
	private Document getStoreBatchDetails(YFSEnvironment env) throws Exception{
		log.verbose("Entering getStoreBatchDetails()");
		Document docInGetStoreBatchDetails  = null;
		Document docOutGetStoreBatchDetails  = null;
		
		docInGetStoreBatchDetails = XMLUtil.createDocument(AcademyConstants.ELE_STORE_BATCH);
		docInGetStoreBatchDetails.getDocumentElement().setAttribute(AcademyConstants.ATTR_STORE_BATCH_KEY, strStoreBatchKey);
		
		log.verbose("Input to getStoreBatchDetails API : "+XMLUtil.getXMLString(docInGetStoreBatchDetails));
		docOutGetStoreBatchDetails = AcademyUtil.invokeService(env, AcademyConstants.SERVICE_GET_STORE_BATCH_DETAILS, docInGetStoreBatchDetails);
		log.verbose("Output from getStoreBatchDetails API : "+XMLUtil.getXMLString(docOutGetStoreBatchDetails));
		
		log.verbose("Exiting getStoreBatchDetails()");
		return docOutGetStoreBatchDetails;
	}
	
	
	private void changeShipmentToResetBatch(YFSEnvironment env, NodeList nlShipmentline) throws Exception{
		log.verbose("Entering changeShipmentToResetBatch() :: No of ShipmentLines : "+nlShipmentline.getLength());
		
		Document docInChangeShipment = null;
		Document docOutChangeShipment = null;
		Element eleChangeShipment = null;
		Element eleShipmentLines = null;
		Element eleShipmentLine = null;
		Element elemShipmentLine = null;		
		String strShipmentKey = XMLUtil.getAttributeFromXPath(XMLUtil.getDocumentForElement((Element) nlShipmentline.item(0)), 
										AcademyConstants.XPATH_SHIPMENTLINE_SHIPMENT_SHIPMENTKEY);
		
		docInChangeShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleChangeShipment = docInChangeShipment.getDocumentElement();
		eleChangeShipment.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY);
		eleChangeShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
		eleChangeShipment.setAttribute(AcademyConstants.ATTR_OVERRIDE_MODIFICATION_RULES, AcademyConstants.STR_YES);
		eleChangeShipment.setAttribute(AcademyConstants.ATTR_INCLUDED_IN_BATCH, AcademyConstants.STR_NO);
		eleChangeShipment.setAttribute(AcademyConstants.ATTR_IS_PICKTICKET_PRINTED, AcademyConstants.STR_EMPTY_STRING);
		eleChangeShipment.setAttribute(AcademyConstants.ATTR_PICK_TICKET_NO, AcademyConstants.STR_EMPTY_STRING);
		
		
		eleShipmentLines = docInChangeShipment.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
		XMLUtil.appendChild(eleChangeShipment, eleShipmentLines);
		
		for (int j = 0; j < nlShipmentline.getLength(); j++){	
			elemShipmentLine = (Element) nlShipmentline.item(j);
			
			eleShipmentLine = docInChangeShipment.createElement(AcademyConstants.ELE_SHIPMENT_LINE);		
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_STORE_BATCH_KEY, AcademyConstants.STR_EMPTY_STRING);
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY, AcademyConstants.STR_ZERO);
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, elemShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
			XMLUtil.appendChild(eleShipmentLines, eleShipmentLine);
		}
		
		log.verbose("Input to changeShipment API : "+XMLUtil.getXMLString(docInChangeShipment));
		docOutChangeShipment = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docInChangeShipment);
		log.verbose("Output from changeShipment API : "+XMLUtil.getXMLString(docOutChangeShipment));
		
		log.verbose("Exiting changeShipmentToResetBatch()");
	}
	
	
	private void changeShipmentStatusToRBP(YFSEnvironment env, String strShipmentKey) throws Exception{
		log.verbose("Entering changeShipmentStatusToRBP() : "+strShipmentKey);

		Document docInChangeShipmentStatus = null;
		Document docOutChangeShipmentStatus = null;
		Element eleChangeShipmentStatus = null;
		Element eleShipmentStatusAudit = null;
		
		docInChangeShipmentStatus = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleChangeShipmentStatus = docInChangeShipmentStatus.getDocumentElement();
		eleChangeShipmentStatus.setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS, AcademyConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL);
		eleChangeShipmentStatus.setAttribute(AcademyConstants.ATTR_TRANSID, AcademyConstants.STR_BACKROOM_PICK_TRAN);
		eleChangeShipmentStatus.setAttribute(AcademyConstants.ATTR_SELL_ORG_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		eleChangeShipmentStatus.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
		
		eleShipmentStatusAudit = docInChangeShipmentStatus.createElement(AcademyConstants.ELE_SHIPMENT_STATUS_AUDIT);		
		eleShipmentStatusAudit.setAttribute(AcademyConstants.ATTR_REASON_CODE, AcademyConstants.STR_REASONCODE_BATCH_RESET);		
		eleShipmentStatusAudit.setAttribute(AcademyConstants.ATTR_REASON_TEXT, AcademyConstants.STR_REASONTEXT_BATCH_RESET);
		XMLUtil.appendChild(eleChangeShipmentStatus, eleShipmentStatusAudit);
		
		log.verbose("Input to changeShipmentStatus API : "+XMLUtil.getXMLString(docInChangeShipmentStatus));
		docOutChangeShipmentStatus = AcademyUtil.invokeAPI(env, AcademyConstants.CHANGE_SHIPMENT_STATUS_API, docInChangeShipmentStatus);
		log.verbose("Output from changeShipmentStatus API : "+XMLUtil.getXMLString(docOutChangeShipmentStatus));
		
		log.verbose("Exiting changeShipmentStatusToRBP()");
	}
	
	
	private void manageStoreBatch(YFSEnvironment env) throws Exception{
		log.verbose("Entering manageStoreBatch()");
		Document docInManageStoreBatch  = null;
		Document docOutManageStoreBatch  = null;
		
		docInManageStoreBatch = XMLUtil.createDocument(AcademyConstants.ELE_STORE_BATCH);
		docInManageStoreBatch.getDocumentElement().setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_OPERATION_VAL_DELETE);
		docInManageStoreBatch.getDocumentElement().setAttribute(AcademyConstants.ATTR_STORE_BATCH_KEY, strStoreBatchKey);
		
		log.verbose("Input to manageStoreBatch API : "+XMLUtil.getXMLString(docInManageStoreBatch));
		docOutManageStoreBatch = AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_STORE_BATCH, docInManageStoreBatch);
		log.verbose("Output from manageStoreBatch API : "+XMLUtil.getXMLString(docOutManageStoreBatch));
		
		log.verbose("Exiting manageStoreBatch()");
	}

}
