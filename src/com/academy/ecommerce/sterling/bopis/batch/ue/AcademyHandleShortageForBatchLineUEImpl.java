package com.academy.ecommerce.sterling.bopis.batch.ue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.w3c.dom.Document;
import com.academy.util.constants.AcademyConstants;
import com.yantra.pca.ycd.japi.ue.YCDhandleShortageForBatchLineUE;
import com.yantra.pca.ycd.utils.YCDUtils;
import com.yantra.yfc.core.YFCIterable;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.ui.backend.util.APIManager;
import com.yantra.yfc.util.YFCDoubleUtils;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

/**
 * This Class gets input for handleShortageForBatchLineUE.
 *
 * input: <StoreBatch BatchNo="" OrganizationCode="" StoreBatchKey="Required">
<Item ItemID="" ItemKey="Required" OrganizationCode="" ProductClass="" ShortageReason="Required" UnitOfMeasure=""/><ShipmentLines>
<ShipmentLine OrganizationCode="" ShipmentKey="" ShipmentLineKey="Required"/></ShipmentLines></StoreBatch>
 *
 * @author Sanchit
 *
 */
public class AcademyHandleShortageForBatchLineUEImpl implements YCDhandleShortageForBatchLineUE {

	
	/**
	 * LOGGER Object.
	 */
	static YFCLogCategory logger = YFCLogCategory.instance(AcademyHandleShortageForBatchLineUEImpl.class.getName());
	
	
	/**
	 *This method gets  the input to handleShortageForBatchLineUE and based on Shortage Reason , 
	 *it performs the actions respectively
	 *
	 * @param env
	 * @param inXML
	 * @return OutDoc
	 */
	@Override
	public Document handleShortageForBatchLine(YFSEnvironment env, Document inXML) throws YFSUserExitException {
		logger.beginTimer("AcademyHandleShortageForBatchLineUEImpl.handleShortageForBatchLine");
		
		YFCDocument yfcinXML=YFCDocument.getDocumentFor(inXML);
		YFCElement eleStoreBatch=yfcinXML.getDocumentElement();
		
		if (logger.isVerboseEnabled()) {
			logger.verbose("InputDoc to handleShortageForBatchLine: " + eleStoreBatch);
		}
		String strBatchKey=eleStoreBatch.getAttribute(AcademyConstants.ATTR_STORE_BATCH_KEY);
		
		YFCElement eleShipmentLines = eleStoreBatch.getChildElement(AcademyConstants.ELE_SHIPMENT_LINES);
		try{
		handleShortageReason(env, eleStoreBatch, strBatchKey, eleShipmentLines);
		}catch (Exception e) {
			logger.error("AcademyHandleShortageForBatchLineUEImpl.handleShortageForBatchLine(): Error while performing shorting.", e);
			throw new YFSUserExitException(e.getMessage());
		}
		
		
		return YFCDocument.createDocument(AcademyConstants.VALUE_SUCCESSS).getDocument();
	}

	
	/**
	 * This method Handle the Shortage Reason based on the Value 
	 * selected at the time of Shorting from Store UI
	 * @param env
	 * @param eleStoreBatch
	 * @param strBatchKey
	 * @param eleShipmentLines
	 * @throws Exception 
	 */
	private void handleShortageReason(YFSEnvironment env, YFCElement eleStoreBatch, String strBatchKey,
			YFCElement eleShipmentLines) throws Exception {
		
		logger.debug("AcademyHandleShortageForBatchLineUEImpl.handleShortageReason() - Start");
		logger.beginTimer("AcademyHandleShortageForBatchLineUEImpl.handleShortageReason()");
		
		//Arraylist to store Cancellable Shipment
		ArrayList<String> cancellableShipments = new ArrayList<String>();
		//HashMap tp store Change Shipment Input againt Shipment Keys
		Map<String, YFCElement> changeShipmentInputMap = new HashMap<String, YFCElement>();
		//Array list to store ShipmentKeys
		ArrayList<String> shipmentKeyList = new ArrayList<String>();
		//Map to store Shipment element against Shipment Key
		Map<String, YFCElement> shipmentDetailsMap = new HashMap<String, YFCElement>();
		
		YFCElement eleItem=eleStoreBatch.getChildElement(AcademyConstants.ITEM);
		String strOrgCode = eleItem.getAttribute(AcademyConstants.ORGANIZATION_CODE);
		String strShortageReason = eleItem.getAttribute(AcademyConstants.ATTR_SHORTAGE_REASON);
		
		//iterate over the input SHipment Lines
		YFCIterable<YFCElement> itr = eleShipmentLines.getChildren(AcademyConstants.ELE_SHIPMENT_LINE);
		while (itr.hasNext()){
			YFCElement eleChangeShipmentInput = null;
			YFCElement eleShipmentLine = (YFCElement)itr.next();
			YFCElement eleShipment = eleShipmentLine.getChildElement(AcademyConstants.ELE_SHIPMENT);
			String strShipmentKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			//update the value in shipmentDetailMap
			shipmentDetailsMap.put(strShipmentKey, eleShipment);
			String strShipmentLineKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
			double dBackroomPickedQty = eleShipmentLine.getDoubleAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY);
			double dShortageQuantity = eleShipmentLine.getDoubleAttribute(AcademyConstants.ATTR_QUANTITY) - dBackroomPickedQty;
		
			//match if Shortage Qty is not zero
			if (dShortageQuantity > 0.0D){
				double dNewQuantity = eleShipmentLine.getDoubleAttribute(AcademyConstants.ATTR_QUANTITY) - dShortageQuantity;
				double dShipmentLineShortQty = eleShipmentLine.getDoubleAttribute(AcademyConstants.ATTR_ORIGINAL_QTY) - dNewQuantity;
				//match if new quantity of shipment after calculation is zero then ad the shipment to cancel able shipment list
				if (YFCDoubleUtils.equal(dNewQuantity, 0.0D))
				{
					if (!cancellableShipments.contains(strShipmentKey)) {
						cancellableShipments.add(strShipmentKey);
					}
				}
				else if (!shipmentKeyList.contains(strShipmentKey)) {
					shipmentKeyList.add(strShipmentKey);
				}
				
				if (changeShipmentInputMap.containsKey(strShipmentKey))
				{
					//update the ChangeShipment with the BackOrder Input Quantities
					eleChangeShipmentInput = (YFCElement)changeShipmentInputMap.get(strShipmentKey);
					setChangeShipmentWithBackorderInput(eleChangeShipmentInput, strShipmentLineKey, 
							dNewQuantity, dShipmentLineShortQty, strShortageReason, dBackroomPickedQty);
					
				}
				else
				{
					eleChangeShipmentInput = prepareChangeShipmentInput(env, strShipmentKey);
					setChangeShipmentWithBackorderInput(eleChangeShipmentInput, strShipmentLineKey, 
							dNewQuantity, dShipmentLineShortQty, strShortageReason, dBackroomPickedQty);										
					changeShipmentInputMap.put(strShipmentKey, eleChangeShipmentInput);
				}
			}
			
		}
		if (changeShipmentInputMap.size() > 0) {
			callChangeShipmentForAllShipments(env, changeShipmentInputMap);
		}
		Iterator<String> it;
		if (cancellableShipments.size() > 0) {
			for (it = cancellableShipments.iterator(); it.hasNext();)
			{
				String strShipmentKey = (String)it.next();
				boolean shipmentCancelled = checkShipmentForCancellation(env, strShipmentKey, strBatchKey);
				if ((!shipmentCancelled) && 
						(!shipmentKeyList.contains(strShipmentKey))) {
					shipmentKeyList.add(strShipmentKey);
				}
			}
		}
		for (Iterator<String> iterator = shipmentKeyList.iterator(); iterator.hasNext();)
		{
			String strShipmentKey = (String)iterator.next();
			YFCElement shipmentDetailsElem = (YFCElement)shipmentDetailsMap.get(strShipmentKey);
			callChangeShipmentStatus(env, strShipmentKey, shipmentDetailsElem, strBatchKey, 
					eleStoreBatch.getAttribute(AcademyConstants.ATTR_ASSIGNED_TO_USER_ID));
		}
		logger.endTimer("AcademyHandleShortageForBatchLineUEImpl.handleShortageReason()");
		logger.debug("AcademyHandleShortageForBatchLineUEImpl.handleShortageReason()- END");
		
	}

	/**
	 * This method invokes the ChangeShipmentStatus to update the status on Shipment
	 * @param env
	 * @param strShipmentKey
	 * @param shipmentDetailsElem
	 * @param strBatchKey
	 * @param userId
	 */
	private void callChangeShipmentStatus(YFSEnvironment env, String strShipmentKey, YFCElement shipmentDetailsElem,
			String strBatchKey, String userId) {
		
		logger.debug("AcademyHandleShortageForBatchLineUEImpl.callChangeShipmentStatus() - Start");
		logger.beginTimer("AcademyHandleShortageForBatchLineUEImpl.callChangeShipmentStatus()");
		
		String status = shipmentDetailsElem.getAttribute(AcademyConstants.STATUS);
		if (status.contains("1100.70.06.10"))
		{
			YFCDocument yfcDocChangeShipmentStatusInputDoc = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
			YFCElement eleChangeShipmentStatusInput = yfcDocChangeShipmentStatusInputDoc.getDocumentElement();
			eleChangeShipmentStatusInput.setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS, "1100.70.06.20");
			eleChangeShipmentStatusInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			eleChangeShipmentStatusInput.setAttribute(AcademyConstants.ATTR_TRANSID, "YCD_BACKROOM_PICK_IN_PROGRESS");


			YFCDocument yfcDocChangeShipmentStatusTempateDoc = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
			YFCElement eleChangeShipmentStatusTempateElem = yfcDocChangeShipmentStatusTempateDoc.getDocumentElement();
			eleChangeShipmentStatusTempateElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, "");
			if (logger.isDebugEnabled()) {
				logger.debug("Input to changeShipmentStatus:" + yfcDocChangeShipmentStatusInputDoc);
			}
			YFCElement eleApi = YFCDocument.createDocument(AcademyConstants.ELEMENT_API).getDocumentElement();
			eleApi.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.CHANGE_SHIPMENT_STATUS);
			APIManager.getInstance().invokeAPI(env, eleApi, eleChangeShipmentStatusInput, eleChangeShipmentStatusTempateElem);




			YFCDocument yfcDocChangeShipmentInputDoc = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
			YFCElement eleChangeShipmentInput = yfcDocChangeShipmentInputDoc.getDocumentElement();
			eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_ASSIGNED_TO_USER_ID, userId);


			YFCDocument yfcDocChangeShipmentTemplateDoc = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
			YFCElement eleChangeShipmentTemplateElem = yfcDocChangeShipmentTemplateDoc.getDocumentElement();
			eleChangeShipmentStatusTempateElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, "");

			if (logger.isDebugEnabled()) {
				logger.debug("Input to changeShipment:" + yfcDocChangeShipmentInputDoc);
			}
			YFCElement apiElement = YFCDocument.createDocument(AcademyConstants.ELEMENT_API).getDocumentElement();
			apiElement.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.API_CHANGE_SHIPMENT);
			APIManager.getInstance().invokeAPI(env, apiElement, eleChangeShipmentInput, eleChangeShipmentTemplateElem);
		}
		logger.endTimer("AcademyHandleShortageForBatchLineUEImpl.callChangeShipmentStatus()");
		logger.debug("AcademyHandleShortageForBatchLineUEImpl.callChangeShipmentStatus()- END");
		
	}

	
	/**
	 * This method check if there are shipment for Cancellation
	 * @param env
	 * @param strShipmentKey
	 * @param strBatchKey
	 * @return
	 */
	private boolean checkShipmentForCancellation(YFSEnvironment env, String strShipmentKey, String strBatchKey) {
		

		logger.debug("AcademyHandleShortageForBatchLineUEImpl.checkShipmentForCancellation() - Start");
		logger.beginTimer("AcademyHandleShortageForBatchLineUEImpl.checkShipmentForCancellation()");
		
		YFCDocument yfcDocGetShipmentLineListInputDoc = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT_LINE);
		YFCElement eleGetShipmentLineListElem = yfcDocGetShipmentLineListInputDoc.getDocumentElement();
		eleGetShipmentLineListElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);

		YFCDocument yfcDocGetShipmentLineListTemplateDoc = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT_LINES);
		YFCElement eleGetShipmentLineListTemplate = yfcDocGetShipmentLineListTemplateDoc.getDocumentElement();
		eleGetShipmentLineListTemplate.setAttribute(AcademyConstants.ATTRIBUTE_TOTAL_NO_RECORDS, "");
		if (logger.isDebugEnabled())
		{
			logger.debug("Input to getShipmentLineList:" + eleGetShipmentLineListElem);
			logger.debug("Template to getShipmentLineList:" + yfcDocGetShipmentLineListTemplateDoc);
		}
		YFCElement eleApi = YFCDocument.createDocument(AcademyConstants.ELEMENT_API).getDocumentElement();
		eleApi.setAttribute(AcademyConstants.ATTR_NAME, "getShipmentLineList");
		YFCElement eleGetShipmentLineListOutElem = APIManager.getInstance().invokeAPI(env, eleApi, eleGetShipmentLineListElem, eleGetShipmentLineListTemplate);
		if (logger.isDebugEnabled()) {
			logger.debug("Output from getShipmentLineList:" + eleGetShipmentLineListOutElem);
		}
		int iNoOfShipmentLines = eleGetShipmentLineListOutElem.getIntAttribute(AcademyConstants.ATTRIBUTE_TOTAL_NO_RECORDS);
		eleGetShipmentLineListElem.setAttribute(AcademyConstants.ATTR_QUANTITY, "0");
		YFCElement eleGetShipmentLineListOutputElem = APIManager.getInstance().invokeAPI(env, eleApi, eleGetShipmentLineListElem, eleGetShipmentLineListTemplate);
		int iTotalNumberOfRecords = eleGetShipmentLineListOutputElem.getIntAttribute(AcademyConstants.ATTRIBUTE_TOTAL_NO_RECORDS);
		if (iTotalNumberOfRecords == iNoOfShipmentLines)
		{
			YFCElement eleChangeShipmentInput = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT).getDocumentElement();
			eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.VAL_CANCEL);
			eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);

			YFCDocument yfcDocChangeShipmentOutputDoc = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
			YFCElement eleChangeShipmentOutputElem = yfcDocChangeShipmentOutputDoc.getDocumentElement();
			eleChangeShipmentOutputElem.setAttribute(AcademyConstants.ATTR_STATUS, "");

			YFCElement elementApi = YFCDocument.createDocument(AcademyConstants.ELEMENT_API).getDocumentElement();
			elementApi.setAttribute(AcademyConstants.ATTR_NAME, "changeShipment");
			YFCElement eleChangeShipmentForCancelOutElem = APIManager.getInstance().invokeAPI(env, elementApi, eleChangeShipmentInput, eleChangeShipmentOutputElem);
			if (logger.isDebugEnabled()) {
				logger.debug("Output from changeShipment:" + eleChangeShipmentForCancelOutElem);
			}
			return true;
		}
		logger.endTimer("AcademyHandleShortageForBatchLineUEImpl.checkShipmentForCancellation()");
		logger.debug("AcademyHandleShortageForBatchLineUEImpl.checkShipmentForCancellation()- END");
		return false;
	}
	
	/**
	 * This method invokes the ChangeShipment api for all the Shipment.
	 * @param env
	 * @param changeShipmentInputMap
	 */

	private void callChangeShipmentForAllShipments(YFSEnvironment env, Map<String, YFCElement> changeShipmentInputMap) {
		
		logger.debug("AcademyHandleShortageForBatchLineUEImpl.callChangeShipmentForAllShipments() - Start");
		logger.beginTimer("AcademyHandleShortageForBatchLineUEImpl.callChangeShipmentForAllShipments()");
		
		YFCDocument eleChangeShipmentTemplateDoc = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
		YFCElement eleChangeShipmentTemplate = eleChangeShipmentTemplateDoc.getDocumentElement();
		eleChangeShipmentTemplate.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, "");

		Iterator it = changeShipmentInputMap.keySet().iterator();
		while (it.hasNext())
		{
			YFCElement eleChangeShipmentInput = (YFCElement)changeShipmentInputMap.get(it.next());
			YFCElement apiElem = YFCDocument.createDocument(AcademyConstants.ELEMENT_API).getDocumentElement();
			apiElem.setAttribute(AcademyConstants.ATTR_NAME, "changeShipment");
			APIManager.getInstance().invokeAPI(env, apiElem, eleChangeShipmentInput, eleChangeShipmentTemplate);
		}
		logger.endTimer("AcademyHandleShortageForBatchLineUEImpl.callChangeShipmentForAllShipments()");
		logger.debug("AcademyHandleShortageForBatchLineUEImpl.callChangeShipmentForAllShipments()- END");
	}

	/**
	 * This method prepares the input for changeShipmentAPI.
	 * @param env
	 * @param strShipmentKey
	 * @return
	 */
	private YFCElement prepareChangeShipmentInput(YFSEnvironment env, String strShipmentKey) {
		
		logger.debug("AcademyHandleShortageForBatchLineUEImpl.prepareChangeShipmentInput() - Start");
		logger.beginTimer("AcademyHandleShortageForBatchLineUEImpl.prepareChangeShipmentInput()");
		YFCElement changeShipmentWithBackorderInput = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT).getDocumentElement();
		changeShipmentWithBackorderInput.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY);
		changeShipmentWithBackorderInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
		changeShipmentWithBackorderInput.setAttribute(AcademyConstants.ATTR_BACK_ORD_REM_QTY, AcademyConstants.STR_YES);
		logger.endTimer("AcademyHandleShortageForBatchLineUEImpl.prepareChangeShipmentInput()");
		logger.debug("AcademyHandleShortageForBatchLineUEImpl.prepareChangeShipmentInput()- END");
		return changeShipmentWithBackorderInput;
		
	}

	/**
	 * This method prepares the input for changeShipment for BackOrderInput
	 * @param eleChangeShipmentInput
	 * @param strShipmentLineKey
	 * @param dNewQuantity
	 * @param dShortageQuantity
	 * @param strShortageReason
	 * @param dBackroomPickedQty
	 * @throws Exception 
	 */
	private void setChangeShipmentWithBackorderInput(YFCElement eleChangeShipmentInput, String strShipmentLineKey,
			double dNewQuantity, double dShortageQuantity, String strShortageReason, double dBackroomPickedQty) throws Exception {
		logger.debug("AcademyHandleShortageForBatchLineUEImpl.setChangeShipmentWithBackorderInput() - Start");
		logger.beginTimer("AcademyHandleShortageForBatchLineUEImpl.setChangeShipmentWithBackorderInput()");
		
		YFCElement eleCsShipmentLines = eleChangeShipmentInput.getChildElement(AcademyConstants.ELE_SHIPMENT_LINES, true);
		YFCElement eleCsShipmentLine = YCDUtils.getLineInXml(eleCsShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE, AcademyConstants.ATTR_SHIPMENT_LINE_KEY, strShipmentLineKey, true);
		eleCsShipmentLine.setDoubleAttribute(AcademyConstants.ATTR_QUANTITY, dNewQuantity);
		eleCsShipmentLine.setDoubleAttribute(AcademyConstants.ATTR_SHORTAGE_QTY, dShortageQuantity);
		eleCsShipmentLine.setDoubleAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY, dBackroomPickedQty);
		eleCsShipmentLine.setAttribute(AcademyConstants.ATTR_SHRTG_RESOL_REASON, strShortageReason);
		logger.endTimer("AcademyHandleShortageForBatchLineUEImpl.setChangeShipmentWithBackorderInput()");
		logger.debug("AcademyHandleShortageForBatchLineUEImpl.setChangeShipmentWithBackorderInput()- END");
	}

}
