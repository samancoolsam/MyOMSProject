package com.academy.ecommerce.sterling.sof.shipment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @Author Chiranthan Narayanappa(C0007277)
 * @JIRA# WN-2041 - Acquisition interface ; WN-2042 - Disposition interface ; WN-1814 Ready for Customer Pickup Email
 * @Date Created Sept 5th 2017
 * 
 * @Purpose
 * As part of SOF fulfillment, we have receiving inventory from vendor to store and customer pick up from store,operations(A&D operations).
 * Acquisition : When Store acquires inventory from vendor.
 * Disposition : When Customer comes to pick up the items, Store disposes the acquired inventory.
 * This Class does the following :
 * 1.Call 'getShipmentListForOrder' to get shipments details using orderNo.
 * 2.Call 'changeShipment' to update YFS_SHIPMENT_LINE.EXTN_SOF_RDY_FOR_CUSTPICK_QTY(during Acquisition), 
 * 			and YFS_SHIPMENT_LINE.EXTN_SOF_CUST_PICKED_QTY(during Disposition).
 * 3.Call 'changeShipmentStatus' to update the shipment status to 'Partial Ready for Customer Pickup'/'Ready for Customer Pickup'/'Picked up'.
 * 4.Call manageTaskQueue - For every acquired firearm qty, we will have to send 'Ready for Pickup Email'. To follow the existing email process, we insert record into TaskQ table. ACADEMY_EMAIL_AGENT_SERVER sends out the emails
 * 
 * 5. Call 'AcademySOFSendStatusUpdateToWCS' - Sterling will send status update message to WCS when complete quantity of the Shipment Line (SKU) is updated. (Example : When all the units of SO Shipment line are completely Acquired/ completely picked up). If the complete quantity of a line is not Acquired or completely picked up then Sterling will not send corresponding status update message to WCS. WCS may end up showing the previous status in My Account page. 
 * 6. Upon Receiving Change shipment to PWI status upon receiving  Status = "P" from GSM for Firearm orders 
 **/

public class AcademySOFAcquisitionDisposition implements YIFCustomApi{

	String strOperation = null;
	String strShipmentKey = null;

	public static final YFCLogCategory log = YFCLogCategory.instance(AcademySOFAcquisitionDisposition.class);

	@Override
	public void setProperties(Properties arg0) throws Exception {
   // TODO document 
 }

	/**
	 * Calling getShipmentListForOrder API, changeShipment API, changeShipmentStatus API
	 * @param env
	 * @param inDoc
	 * @return 
	 * @throws Exception
	 */	
	public Document acqDisp(YFSEnvironment env, Document inDoc) throws Exception{
		log.verbose("Entering AcademySOFAcquisitionDisposition.acqDisp() :: "+XMLUtil.getXMLString(inDoc));

		Document docOutGetShipmentListForOrder = null;
		Element eleShipment = null;
		Element eleIn = null;
		NodeList nlSOShipments = null;
		String strSalesOrderNo = null;
		String strShipmentNo = null;
		String strItemID = null;
		String strStatus = null;
		String strShipNode = null;
		String strQuantity = null;
		String strExtnReadyForCustomerQty = null;
		String strExtnCustPickedQty = null;		
		double dShipmentLineQuantity = 0.00;
		double dReadyForCustomerQty = 0.00;
		double dPickedUpQty = 0.00;
		Boolean bFlag = false;
		Boolean isEligibleForRecordCustPick = false;
		int iShipments = 0;
		int iTOShipments = 0;
		String strShipmentType = null;
		String strDeliveryMethod = null;
		NodeList nlShipmentLine = null;
		eleIn = inDoc.getDocumentElement();
		strOperation = eleIn.getAttribute(AcademyConstants.ATTR_STATUS);
		strItemID = eleIn.getAttribute(AcademyConstants.ATTR_ITEM_ID);
		strSalesOrderNo = eleIn.getAttribute(AcademyConstants.STR_SOF_ACQDSP_CUSTOMER_ORDERNO);
		NodeList nlTOShipments = null;
		boolean bIsSTSFireArmAcq = false;
		double dStatus = 0.00; //OMNI-90831
		String strIsDispositionCompleted = null; //OMNI-93545
		NodeList nlDSVSOFShipments = null;
		int iDSVSOFShipments = 0;
		//START OMNI-95133 
		String sFulfillmentType = null;
		String strBackroomPickedQty=null;
		String strPackListType= null;
		
		//OMNI-99050 - Starts
		if (AcademyConstants.ATTR_OPERATION_CANCEL.equals(strOperation)) {							
			log.verbose("Invoking AcademyProcessHardDeclineFromGSM with input :: " + XMLUtil.getXMLString(inDoc));
			AcademyUtil.invokeService(env, AcademyConstants.SERVICE_PROCESS_HARD_DECLINE_FROM_GSM, inDoc);				
			return inDoc;
		}
		//OMNI-99050 - Ends
		docOutGetShipmentListForOrder = getShipmentListForOrder(env, strSalesOrderNo);		
		docOutGetShipmentListForOrder.getDocumentElement().setAttribute(AcademyConstants.ATTR_IS_DISPOSITION_COMPLETED,AcademyConstants.STR_NO);
		
		nlSOShipments = XPathUtil.getNodeList(docOutGetShipmentListForOrder,
				"ShipmentList/Shipment[@DocumentType='0001'][ShipmentLines/ShipmentLine/@ItemID='" + strItemID + "']");	
		iShipments = nlSOShipments.getLength();		
		
		nlTOShipments = XPathUtil.getNodeList(docOutGetShipmentListForOrder,
				"ShipmentList/Shipment[@DocumentType='0006'][ShipmentLines/ShipmentLine/@ItemID='" + strItemID + "']");
		iTOShipments = nlTOShipments.getLength();
		log.verbose("iTOShipments Length :: "+iTOShipments);
		if (iTOShipments > 0) {
			Element eleTOShipment = (Element) nlTOShipments.item(0);
			String sShipmentType = eleTOShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
			if (sShipmentType.equals(AcademyConstants.STR_SHIP_TO_STORE)
					&& AcademyConstants.STR_ACQUISITION.equals(strOperation)) {
				Element eleShipmentListOut = inDoc.createElement(AcademyConstants.ATTR_SHIPMENT_LIST_OUT);
				XMLUtil.appendChild(inDoc.getDocumentElement(), eleShipmentListOut);
				Element eleShpmt = XMLUtil.createElement(inDoc, AcademyConstants.ELE_SHIPMENT, null);
				XMLUtil.copyElement(inDoc, eleTOShipment, eleShpmt);
				eleShipmentListOut.appendChild(eleShpmt);
				bIsSTSFireArmAcq = true;
			}
		}
		//OMNI-93544 - Start
		log.verbose("DSV shipments:::: ");
		nlDSVSOFShipments = XPathUtil.getNodeList(docOutGetShipmentListForOrder,
                "ShipmentList/Shipment[@DocumentType='0005' and (@Status='1400' or @Status='1600')][ShipmentLines/ShipmentLine/@ItemID='" + strItemID + "']");
		iDSVSOFShipments = nlDSVSOFShipments.getLength();
		log.verbose("iDSVSOFShipments Length :: " + iDSVSOFShipments);
		String acqQty = eleIn.getAttribute(AcademyConstants.ATTR_QUANTITY);
		
		if (iDSVSOFShipments > 0 && AcademyConstants.STR_ACQUISITION.equals(strOperation)) {							
			Element elePOShipment = (Element) nlDSVSOFShipments.item(0);
			log.verbose("Element :::: " + XMLUtil.getElementXMLString(elePOShipment));
			elePOShipment.setAttribute("IsDSVFA", AcademyConstants.STR_YES);
			elePOShipment.setAttribute("AcqQuantity", acqQty);
			elePOShipment.setAttribute("AcqItemID", strItemID);
			elePOShipment.setAttribute("InvokedFromAcquisition", AcademyConstants.STR_YES);
			Document docShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element eleShip = docShipment.getDocumentElement();
			XMLUtil.copyElement(docShipment, elePOShipment, eleShip);
			log.verbose("Invoking AcademySOFAutoPOReceiveSORelease with input :: " + XMLUtil.getXMLString(docShipment));
			AcademyUtil.invokeService(env, AcademyConstants.ACAD_SOF_AUTO_SO_RELEASE, docShipment);				
			return inDoc;
		}
		//OMNI-93544 - End
		log.verbose("bIsSTSFireArmAcq value :: "+bIsSTSFireArmAcq);
		if (bIsSTSFireArmAcq) {
			AcademyUtil.invokeService(env,AcademyConstants.SERV_ACADEMY_STS_FIREARMS_ACQ, inDoc);
		}
		else {
			//Start WN-2730 SOF_Error Handling
			if(iShipments == 0){
				YFSException exception = new YFSException(AcademyConstants.STR_SHIPMENT_DOES_NOT_EXIST);
				exception.setErrorCode(AcademyConstants.STR_EXTN_ACADEMY_17);
				throw exception;
			}
			//End WN-2730 SOF_Error Handling

		//An Order can have multiple shipments for same ItemID, hence looping through.
		for(int iShipmentCounter=0; iShipmentCounter < iShipments; iShipmentCounter++){	
			bFlag = false;		
			eleShipment = (Element) nlSOShipments.item(iShipmentCounter);
			log.verbose("eleShipment :: "+XMLUtil.getElementXMLString(eleShipment));
			
			//START OMNI-76206 STS Firearm Integration - Customer received via disposition
			//Handling multiple shipment line scenarios by matching the ItemID coming from the GSM request
			nlShipmentLine = eleShipment.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			for (int shpmtLine = 0; shpmtLine < nlShipmentLine.getLength(); shpmtLine++) {
				
			Element	eleShipmentLine = (Element) nlShipmentLine.item(shpmtLine);
			String strShpLineItemID = eleShipmentLine.getAttribute(AcademyConstants.ITEM_ID);
			String strShipLineQty = eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
			//START OMNI-95133 
			Element eleOrderLine=(Element) eleShipmentLine.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE).item(0);
			sFulfillmentType=eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
			log.verbose("sFulfillmentType :" + sFulfillmentType);
			//END OMNI-95133			
			if((strItemID.equalsIgnoreCase(strShpLineItemID)) && Double.parseDouble(strShipLineQty) > 0)  {
			log.verbose("eleShipmentLine :: "+XMLUtil.getElementXMLString(eleShipmentLine));
			strShipmentType = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
			strDeliveryMethod = eleShipment.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);		
			//END OMNI-76206 STS Firearm Integration - Customer received via disposition
			strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			strShipmentNo = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			strStatus = eleShipment.getAttribute(AcademyConstants.ATTR_STATUS);
			strShipNode = eleShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
			strQuantity =  eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
			strExtnReadyForCustomerQty = XPathUtil.getString(eleShipmentLine, AcademyConstants.ATTR_RDY_FOR_CUSTPICKQTY_XPATH);
			strExtnCustPickedQty = XPathUtil.getString(eleShipmentLine, AcademyConstants.ATTR_SOF_CUSTPICKQTY_XPATH);			
			strIsDispositionCompleted = docOutGetShipmentListForOrder.getDocumentElement().getAttribute(AcademyConstants.ATTR_IS_DISPOSITION_COMPLETED); //OMNI-93545		
			log.verbose("IsDispositionCompleted::" +strIsDispositionCompleted);
			
			log.verbose("strQuantity : "+ strQuantity +" :: strExtnReadyForCustomerQty : "+ strExtnReadyForCustomerQty +" " +
					":: strExtnCustPickedQty : "+strExtnCustPickedQty);
			strPackListType = eleOrderLine.getAttribute(AcademyConstants.ATTR_PACKLIST_TYPE);
					
			if(AcademyConstants.STR_SHIP_TO_STORE.equals(strShipmentType)) 
			{
				strExtnReadyForCustomerQty = strQuantity;
				
			}
			if(!YFCCommon.isVoid(strPackListType) && AcademyConstants.STS_FA.equals(strPackListType) &&
		            (!YFCCommon.isVoid(sFulfillmentType) && AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE.equals(sFulfillmentType))){
				strBackroomPickedQty=eleShipmentLine.getAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY);
                log.verbose("strBackroomPickedQty : "+ strBackroomPickedQty + " :: strExtnCustPickedQty : "+strExtnCustPickedQty);
                strExtnReadyForCustomerQty=strBackroomPickedQty;
                bFlag=false;                
			 }	
			
			
			
			//END OMNI-95133 BOPIS Firearm Integration - Disposition ESB to OMS			
			if(AcademyConstants.STR_ACQUISITION.equals(strOperation)){	
				dShipmentLineQuantity = Double.parseDouble(strQuantity);
				dReadyForCustomerQty = Double.parseDouble(strExtnReadyForCustomerQty);

				if((Double.parseDouble(strExtnReadyForCustomerQty)) < (Double.parseDouble(strQuantity))){	
					//To update YFS_SHIPMENT_LINE.EXTN_SOF_RDY_FOR_CUSTPICK_QTY
					changeShipment(env, eleShipment,strItemID);
					readyForCustEmailManageTaskQueue(env, strShipmentNo, 
							strExtnReadyForCustomerQty.substring(0, strExtnReadyForCustomerQty.indexOf(AcademyConstants.STR_DOT)));

					/*If Shipment status is already in 'Partial Ready for Customer Pickup', 
					do not call changeShipmentStatus again for rest of the quantities.*/
					if( (dShipmentLineQuantity != (dReadyForCustomerQty + 1)) 
							&&  isStatusChangeRequired(strStatus, AcademyConstants.STR_PARTIALLY_READYFORCUSTOMER_STATUS)){
						log.verbose("Calling changeShipmentStatus to update shipment status to 'Partial Ready for Customer Pickup'");
						changeShipmentStatus(env, strShipNode, AcademyConstants.STR_PARTIALLY_READYFORCUSTOMER_STATUS);	
					} else if((dShipmentLineQuantity == (dReadyForCustomerQty + 1))
							&&  isStatusChangeRequired(strStatus, AcademyConstants.STR_READYFORCUSTOMER_STATUS)){
						log.verbose("Calling changeShipmentStatus to update shipment status to 'Ready for Customer Pickup'");
						changeShipmentStatus(env, strShipNode, AcademyConstants.STR_READYFORCUSTOMER_STATUS);							
						sendStatusUpdateToWCS(env);
					}
					break;
				}

				//Start WN-2730 SOF_Error Handling
				else {
					bFlag = true;
					}
				//End WN-2730 SOF_Error Handling

			}
			//OMNI-95777 Start: Change shipment to PWI status upon receiving message from GSM for Firearm orders
			else if(AcademyConstants.STR_PAPERWORKINITIATED.equals(strOperation))
			{
				log.verbose("Value of strStatus ::" + strStatus);
				if(strStatus.equalsIgnoreCase(AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS) ) 
				{				
					initiatePaperwork(env, strShipmentKey);	
					bFlag = false;
					break;
				}else if(strStatus.equalsIgnoreCase(AcademyConstants.STR_PAPER_WORK_INITIATED_STATUS))
				{
					log.verbose("Shipment is Already in PaperWork Initiated" + strStatus);
					bFlag = false;
					break;
				}else
				{
					bFlag=true;
				}
								
			} 			
			//OMNI-95777 : END
			else if(AcademyConstants.STR_DISPOSITION.equals(strOperation) && 
					(!YFCCommon.isVoid(strIsDispositionCompleted) && "N".equals(strIsDispositionCompleted))){
				// OMNI-90831 - Starts
				log.verbose("Value of strStatus ::" + strStatus);
				String strPrefixStatus = strStatus.substring(0, 4);
				dStatus = Double.parseDouble(strPrefixStatus);
				if (AcademyConstants.STR_PICK.equals(strDeliveryMethod) && dStatus >= 1400 && (Double.parseDouble(strExtnCustPickedQty) < Double.parseDouble(strExtnReadyForCustomerQty))) {
					log.info("Shipment is already picked up:: ignoring disposition for shipment::" + strShipmentNo);
					bFlag = false;
					break;
				}
				// OMNI-90831 - Ends
				if((Double.parseDouble(strExtnCustPickedQty)) < (Double.parseDouble(strExtnReadyForCustomerQty))){
					
					log.verbose("dPickedUpQty is less than dReadyForCustomerQty");
					changeShipment(env, eleShipment,strItemID);					
					dPickedUpQty = Double.parseDouble(strExtnCustPickedQty);
					dReadyForCustomerQty = Double.parseDouble(strExtnReadyForCustomerQty);
					dShipmentLineQuantity = Double.parseDouble(strQuantity);

					//Soon after first quantity is picked up by the customer, change shipment status to 'Picked Up'.
					//START OMNI-76206 STS Firearm Integration - Customer received via disposition	
					if(AcademyConstants.STR_PICK.equals(strDeliveryMethod)) {
						dPickedUpQty = dPickedUpQty+1;
						log.verbose("PickedUp Qty After updating" +dPickedUpQty);
						
						isEligibleForRecordCustPick = isFinalUpdate(nlShipmentLine,strItemID,dPickedUpQty);
						if((dReadyForCustomerQty == dPickedUpQty) && isEligibleForRecordCustPick) {
							log.verbose("ShipmentLineQty is equal to PickedQty");
							recordCustomerPick(env, strShipmentKey,nlShipmentLine);
							updatePickedUpDate(env, strShipmentKey);	
							stampInvoiceNo(env, strShipmentKey, strDeliveryMethod);	
							docOutGetShipmentListForOrder.getDocumentElement().setAttribute(
									AcademyConstants.ATTR_IS_DISPOSITION_COMPLETED, AcademyConstants.STR_YES);
						}
						}
					//END OMNI-76206 STS Firearm Integration - Customer received via disposition
					else {
						if(dPickedUpQty == 0.00){
							log.verbose("Calling changeShipmentStatus to update shipment status to 'Picked Up', only for the first quantity/first 'D' msg");
							changeShipmentStatus(env, strShipNode, AcademyConstants.STR_PICKEDUP_STATUS);	
						}
						if(dShipmentLineQuantity == dPickedUpQty + 1){
							sendStatusUpdateToWCS(env);
						}
					}
					break;
				}
				//Start WN-2730 SOF_Error Handling
				else {
					bFlag = true;
				}
				//End WN-2730 SOF_Error Handling

			}			
			}
			}
		}
				
		//Start WN-2730 SOF_Error Handling
		log.verbose("Value of boolean ::" +bFlag);
		if(Boolean.TRUE.equals(bFlag)) {
			if(AcademyConstants.STR_ACQUISITION.equals(strOperation)){
				YFSException exception = new YFSException(AcademyConstants.STR_ACQUISITION_COMPLETED);
				exception.setErrorCode(AcademyConstants.STR_EXTN_ACADEMY_18);
				throw exception;
			}
			else if(AcademyConstants.STR_DISPOSITION.equals(strOperation))
			{
				YFSException exception = new YFSException(AcademyConstants.STR_ACQ_TO_BE_COMPLETED_BEFORE_DISP);
				exception.setErrorCode(AcademyConstants.STR_EXTN_ACADEMY_19);
				throw exception;
			}
			else if(AcademyConstants.STR_PAPERWORKINITIATED.equals(strOperation))
			{
				YFSException exception = new YFSException(AcademyConstants.STR_PAPERWORK_ISSUE);
				exception.setErrorCode(AcademyConstants.STR_EXTN_ACADEMY_23);
				throw exception;
			}
		}
		//End WN-2730 SOF_Error Handling
	}
		log.verbose("Exiting AcademySOFAcquisitionDisposition.acqDisp() ");
		return null;		
	}
	
	private Boolean isFinalUpdate(NodeList nlShipmentLine, String strItemID, double dPickedUpQty) throws Exception {
		log.beginTimer(this.getClass() + ".isFinalUpdate");

		int iShipLineCount = 0;
		Element eleEachShipLine = null;
		String strShipLineItemID = null;
		String strQuantity = null;
		String strExtnCustPickedQty = null;
		boolean bFinalUpdateofCurrLine = false;
		boolean bAllShipmentLinesUpdated = false;
		boolean isFinalUpdateFlag = false;
		
		iShipLineCount = nlShipmentLine.getLength();
		for(int i = 0; i < iShipLineCount; i++) {
			eleEachShipLine = (Element)nlShipmentLine.item(i);
			strShipLineItemID = eleEachShipLine.getAttribute(AcademyConstants.ITEM_ID);
			strQuantity =  eleEachShipLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
			strExtnCustPickedQty = XPathUtil.getString(eleEachShipLine, AcademyConstants.ATTR_SOF_CUSTPICKQTY_XPATH);			

			if(strItemID.equalsIgnoreCase(strShipLineItemID)) {
				if(dPickedUpQty == (Double.parseDouble(strQuantity))){	
					bFinalUpdateofCurrLine = true;
					log.verbose("bFinalUpdateofCurrLine::"+bFinalUpdateofCurrLine);
				}				
			}
			else {				
				if((Double.parseDouble(strExtnCustPickedQty)) == (Double.parseDouble(strQuantity))){	
					bAllShipmentLinesUpdated = true;
					log.verbose("bAllShipmentLinesUpdated :"+bAllShipmentLinesUpdated);
				}else {
					bAllShipmentLinesUpdated = false;
					log.verbose("bAllShipmentLinesUpdated :: "+bAllShipmentLinesUpdated);
					break;
				}
			}
		}
		if(((iShipLineCount == 1) && bFinalUpdateofCurrLine) || (bFinalUpdateofCurrLine && bAllShipmentLinesUpdated)) {
			isFinalUpdateFlag = true;
		}
		log.verbose("isFinalUpdateFlag::" +isFinalUpdateFlag);
		log.endTimer(this.getClass() + ".isFinalUpdate");
		return isFinalUpdateFlag;
}
	//OMNI-95133 BOPIS Firearm
	//START OMNI-76206 STS Firearm Integration - Customer received via disposition
	/**
	 * This method records the customer pick for STS and BOPIS firearm shipments
	 * @param env
	 * @param strShipmentKey
	 * @param nlShipmentLine
	 * @param strItemID 
	 * @throws Exception
	 */
	private void recordCustomerPick(YFSEnvironment env, String strShipmentKey, NodeList nlShipmentLine) throws Exception {
		log.beginTimer(this.getClass() + ".recordCustomerPick");
		Document docRecordPick = null;
		Element eleRecordPick = null;
		Element eleShipmentLines = null;
		Element eleShipmentLine = null;
		Element eleEachShipLine = null;
		String strTransaction = AcademyConstants.TRAN_CONFIRM_SHIPMENT;
		String strShipLineKey = null;
		String strQuantity = null;
		int iShipLineCount = 0;
		boolean isFireArmItem = false;

		iShipLineCount = nlShipmentLine.getLength();
		log.verbose("The total length of shipmentline:" +iShipLineCount);
		docRecordPick =  XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleRecordPick = docRecordPick.getDocumentElement();
		eleRecordPick.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
		eleRecordPick.setAttribute(AcademyConstants.ATTR_TRANS_ID, strTransaction);
		
		//handle multiple shipmentlines
		eleShipmentLines = docRecordPick.createElement(AcademyConstants.ELE_SHIPMENT_LINES);		
		XMLUtil.appendChild(eleRecordPick, eleShipmentLines);

		for(int i = 0; i < iShipLineCount; i++) {		
				eleEachShipLine = (Element)nlShipmentLine.item(i);
				String shipLineItemID = eleEachShipLine.getAttribute(AcademyConstants.ITEM_ID);
				//Code Changes OMSNI-105592--Start
				String strExtnhasFirearm02Items = SCXmlUtil.getXpathAttribute(eleEachShipLine,
						"Extn/@ExtnHasFireArm02Items");
				isFireArmItem = checkForFireArmItems(env,shipLineItemID,strExtnhasFirearm02Items);
				//Code Changes OMSNI-105592--end
				
				if(isFireArmItem) {
				strShipLineKey = eleEachShipLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
				strQuantity = eleEachShipLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
				eleShipmentLine = docRecordPick.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
				eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY,strShipLineKey);
				eleShipmentLine.setAttribute(AcademyConstants.ATTR_PICKED_QUANTITY,strQuantity);
				eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY,strQuantity);
				XMLUtil.appendChild(eleShipmentLines, eleShipmentLine);
				}
		}
		//call recordCustomerPick api
		log.verbose("Input to recordCustomerPick : "+XMLUtil.getXMLString(docRecordPick));
		AcademyUtil.invokeAPI(env, "recordCustomerPick", docRecordPick);
		log.endTimer(this.getClass() + ".recordCustomerPick");
				
	}
	private boolean checkForFireArmItems(YFSEnvironment env, String shipLineItemID, String strExtnhasFirearm02Items) throws Exception {
		
		boolean isFireArmItemID = false;
		// Code Changes OMSNI-105592--Start added strExtnDepartmentName check
		if (YFCObject.isVoid(strExtnhasFirearm02Items)) {
			String strGetItemListInput = "<Item ItemID='" + shipLineItemID + "'/>";
			Document docGetItemListInput = XMLUtil.getDocument(strGetItemListInput);
			Document docGetItemListOutputTemplate = XMLUtil
					.getDocument("<ItemList><Item><Extn ExtnDepartmentName =''/></Item></ItemList>");
			env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, docGetItemListOutputTemplate);

			Document docGetItemListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST,
					docGetItemListInput);
			env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
			Element EleItemListOutExtn = XMLUtil.getElementByXPath(docGetItemListOutput, "ItemList/Item/Extn");
			String strExtnDepartmentName = EleItemListOutExtn.getAttribute("ExtnDepartmentName");
			if (((strExtnDepartmentName.equalsIgnoreCase("Hand Guns"))
					|| (strExtnDepartmentName.equalsIgnoreCase("Long Guns"))
					|| (strExtnDepartmentName.equalsIgnoreCase("Air Guns")))) {
				isFireArmItemID = true;
			}
		} else if (AcademyConstants.ATTR_Y.equals(strExtnhasFirearm02Items)) {
			log.verbose("******** strExtnhasFirearm02Items ****" + strExtnhasFirearm02Items);
			isFireArmItemID = true;
		}
	return isFireArmItemID;
}

	/**
	 * This method updates the picked up date for STS and BOPIS firearm shipments
	 * @param env
	 * @param strShipmentKey
	 * @throws Exception
	 */
	private void updatePickedUpDate(YFSEnvironment env, String strShipmentKey) throws Exception {
		log.beginTimer(this.getClass() + ".updatePickedUpDate");
		Document docChangeShipment = null;
		Document docChangeShipmentOut = null;
		Element eleChangeShipment = null;
		Element eleAdditionalDates = null;
		Element eleAdditionalDate = null;
		String strCurrentDate = null;
		String strDataType = AcademyConstants.DATE_TYPE_PICKUP_DATE_ACKSLIP;
		
		//Calculate current date
		DateFormat dateFormat = new SimpleDateFormat(AcademyConstants.STR_SIMPLE_DATE_PATTERN);
		Date date = new Date();
		strCurrentDate = dateFormat.format(date);
		
		log.debug("Current Date :\t"+strCurrentDate);

		docChangeShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleChangeShipment = docChangeShipment.getDocumentElement();
		eleChangeShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);		
		
		eleAdditionalDates = docChangeShipment.createElement("AdditionalDates");		
		XMLUtil.appendChild(eleChangeShipment, eleAdditionalDates);

		eleAdditionalDate = docChangeShipment.createElement("AdditionalDate");
		eleAdditionalDate.setAttribute(AcademyConstants.ATTR_ACTUAL_DATE, strCurrentDate);
		eleAdditionalDate.setAttribute(AcademyConstants.A_DATE_TYPE_ID, strDataType);
		XMLUtil.appendChild(eleAdditionalDates, eleAdditionalDate);

		//call changeShipment to update the date to currentdate in yyyy-mm-dd format

		log.verbose("Input to changeShipment : "+XMLUtil.getXMLString(docChangeShipment));
		docChangeShipmentOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipment);
		log.verbose("Output from changeShipment : "+XMLUtil.getXMLString(docChangeShipmentOut));	
		log.endTimer(this.getClass() + ".updatePickedUpDate");
		
	}
	/**
	 * This method stamps the invoice no for STS and BOPIS firearm orders
	 * @param env
	 * @param strShipmentKey
	 * @param strDeliveryMethod
	 * @throws Exception
	 */
	//invoke AcademyStampInvoiceNoOnBOPISOrders with  <Shipment DeliveryMethod="" ShipmentKey=""/>
	private void stampInvoiceNo(YFSEnvironment env, String strShipmentKey, String strDeliveryMethod) throws Exception {
		log.beginTimer(this.getClass() + ".stampInvoiceNo");
		Document docStampInvoice = null;
		Element eleStampInvoice = null;

		docStampInvoice = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleStampInvoice = docStampInvoice.getDocumentElement();
		eleStampInvoice.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);		
		eleStampInvoice.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, strDeliveryMethod);	
		
		//call AcademyStampInvoiceNoOnBOPISOrders to stamp the shipment invoice no
		log.verbose("Input to the Service AcademyStampInvoiceNoOnBOPISOrders  :: "+XMLUtil.getXMLString(docStampInvoice));
		AcademyUtil.invokeService(env, "AcademyStampInvoiceNoOnBOPISOrders" , docStampInvoice);
		log.endTimer(this.getClass() + ".stampInvoiceNo");

	}
	//END OMNI-76206 STS Firearm Integration - Customer received via disposition
	/**
	 * For sending 'Ready for Pickup Emails', manually insert TaskQ entry for each acquired firearm qty
	 * @param strShipmentNo
	 * @param strReadyForCustomerPickupQty
	 * @return bIsStatusChangeRequired
	 * @throws Exception
	 */
	private void readyForCustEmailManageTaskQueue(YFSEnvironment env, String strShipmentNo, String strReadyForCustomerPickupQty) throws Exception{
		Document docInManageTaskQueue = null;
		Document docOutManageTaskQueue = null;
		String strTaskQDataKey = null;

		/*For every acquired firearm Qty, we will have to send 'Ready for Pickup Email'.
		We cannot insert multiple taskQ records(for each Qty) with same ShipmentKey
		And we can not append ShipmentKey_ReadyForCustomerPickupQty and store in YFS_TASK_Q.DATA_KEY because of column length constraint - 24.
		Hence storing  ShipmentNo_ReadyForCustomerPickupQty as DataKey and later reading the shipmentNo substring*/
		strTaskQDataKey = strShipmentNo.concat(AcademyConstants.STR_UNDERSCORE).concat(strReadyForCustomerPickupQty);
		log.verbose("strTaskQDataKey : "+strTaskQDataKey);

		docInManageTaskQueue = prepareManageTaskQueueInput(env, strTaskQDataKey);

		log.verbose("Input to manageTaskQueue : "+XMLUtil.getXMLString(docInManageTaskQueue));
		docOutManageTaskQueue = AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_TASK_QUEUE, docInManageTaskQueue);
		log.verbose("Output from manageTaskQueue : "+XMLUtil.getXMLString(docOutManageTaskQueue));
	}

	/**
	 * Preparing input for manageTaskQueue API
	 * @param strTaskQDataKey
	 * @return docInManageTaskQueue
	 * @throws Exception
	 */
	private Document prepareManageTaskQueueInput(YFSEnvironment env, String strTaskQDataKey) throws Exception{
		Document docInManageTaskQueue = null;
		Element eleInManageTaskQueue = null;
		String strTransactionKey = null;		

		strTransactionKey = getTransactionKey(env, AcademyConstants.SOF_RDYFORCUST_PICKUP_EMAIL_TRAN_ID);

		docInManageTaskQueue = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
		eleInManageTaskQueue = docInManageTaskQueue.getDocumentElement();
		eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY, strTaskQDataKey);
		eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE, AcademyConstants.ATTR_SHIPMENT_NO);
		eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_TRANS_ID, AcademyConstants.SOF_RDYFORCUST_PICKUP_EMAIL_TRAN_ID);
		eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_TRANS_KEY, strTransactionKey);

		return docInManageTaskQueue;
	}

	/**
	 * Calling 'getTransactionKey' API to get TransactionKey using Tranid
	 * @param strRdyForCustEmailTranID
	 * @return strTransactionKey
	 * @throws Exception
	 */
	public String getTransactionKey(YFSEnvironment env, String strRdyForCustEmailTranID) throws Exception{
		Document docInGetTransactionList = null;
		Document docOutGetTransactionList = null;
		Document docGetTransactionListTemplate = null;
		String strGetTransactionListTemplate = null;
		String strTransactionKey = null;

		docInGetTransactionList = XMLUtil.createDocument(AcademyConstants.ELE_TRANSACTION);
		docInGetTransactionList.getDocumentElement().setAttribute(AcademyConstants.ATTR_TRAN_ID, strRdyForCustEmailTranID);

		strGetTransactionListTemplate = "<TransactionList><Transaction Tranid='' TransactionKey='' /></TransactionList>";				
		docGetTransactionListTemplate = YFCDocument.getDocumentFor(strGetTransactionListTemplate).getDocument();

		env.setApiTemplate(AcademyConstants.API_GET_TRANSACTION_LIST, docGetTransactionListTemplate);
		log.verbose("Input to getTransactionList : "+XMLUtil.getXMLString(docInGetTransactionList));
		docOutGetTransactionList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_TRANSACTION_LIST, docInGetTransactionList);
		log.verbose("Output from getTransactionList : "+XMLUtil.getXMLString(docOutGetTransactionList));
		env.clearApiTemplate(AcademyConstants.API_GET_TRANSACTION_LIST);

		strTransactionKey = ((Element) docOutGetTransactionList.getDocumentElement()
				.getElementsByTagName(AcademyConstants.ELE_TRANSACTION).item(0)).getAttribute(AcademyConstants.ATTR_TRANS_KEY);
		log.verbose("Transaction Key : "+XMLUtil.getXMLString(docOutGetTransactionList));

		return strTransactionKey;

	}

	/**
	 * Evaluate if changeShipmentStatus call is required.
	 * If the current shipment status is > new shipment status, skip changeShipmentStatus API call
	 * @param strCurrentStatus
	 * @param strNewStatus
	 * @return bIsStatusChangeRequired
	 * @throws Exception
	 */	
	private boolean isStatusChangeRequired(String strCurrentStatus, String strNewStatus ){
		boolean bIsStatusChangeRequired = false;

		if(strCurrentStatus.compareTo(strNewStatus) < 0){
			bIsStatusChangeRequired = true;
		}

		return bIsStatusChangeRequired;
	}

	/**
	 * Calling 'changeShipmentStatus' to update the shipment status to 'Partial Ready for Customer Pickup'/'Ready for Customer Pickup'/'Picked up'.
	 * @param strShipNode
	 * @param strStatusChange
	 * @return 
	 * @throws Exception
	 */	
	private void changeShipmentStatus(YFSEnvironment env, String strShipNode, String strStatusChange) throws Exception{
		Document docInChangeShipmentStatus = null;
		Document docOutChangeShipmentStatus = null;

		docInChangeShipmentStatus = prepareChangeShipmentStatusInput(strShipNode, strStatusChange);		

		log.verbose("Input to changeShipmentStatus : "+XMLUtil.getXMLString(docInChangeShipmentStatus));
		docOutChangeShipmentStatus = AcademyUtil.invokeAPI(env, AcademyConstants.CHANGE_SHIPMENT_STATUS_API, docInChangeShipmentStatus);
		log.verbose("Output from changeShipmentStatus : "+XMLUtil.getXMLString(docOutChangeShipmentStatus));	

	}

	/**
	 * Preparing input for changeShipmentStatus API
	 * @param strShipNode
	 * @param strStatusChange
	 * @return docInChangeShipmentStatus
	 * @throws ParserConfigurationException
	 */
	private Document prepareChangeShipmentStatusInput(String strShipNode, String strStatusChange) throws ParserConfigurationException{
		Document docInChangeShipmentStatus = null;
		Element eleChangeShipmentStatus = null;

		docInChangeShipmentStatus = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleChangeShipmentStatus = docInChangeShipmentStatus.getDocumentElement();
		eleChangeShipmentStatus.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);		
		eleChangeShipmentStatus.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strShipNode);
		eleChangeShipmentStatus.setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS, strStatusChange);
		eleChangeShipmentStatus.setAttribute(AcademyConstants.ATTR_SELL_ORG_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		eleChangeShipmentStatus.setAttribute(AcademyConstants.ATTR_TRANSID, AcademyConstants.STR_SOF_ACQ_DISP_TRAN_ID);

		return docInChangeShipmentStatus;
	}

	/**
	 * Calling 'changeShipment' to update YFS_SHIPMENT_LINE.EXTN_SOF_RDY_FOR_CUSTPICK_QTY(during Acquisition), 
	 * 			and YFS_SHIPMENT_LINE.EXTN_SOF_CUST_PICKED_QTY(during Disposition).
	 * @param eleShipment
	 * @param strItemID 
	 * @return 
	 * @throws Exception
	 */
	private void changeShipment(YFSEnvironment env, Element eleShipment, String strItemID) throws Exception{
		Document docInChangeShipment = null;
		Document docOutChangeShipment = null;

		docInChangeShipment = prepareChangeShipmentInput(eleShipment,strItemID);

		log.verbose("Input to changeShipment : "+XMLUtil.getXMLString(docInChangeShipment));
		docOutChangeShipment = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docInChangeShipment);
		log.verbose("Output from changeShipment : "+XMLUtil.getXMLString(docOutChangeShipment));	

	}

	/**
	 * Preparing input for changeShipment API
	 * @param eleShipment
	 * @param strItemID 
	 * @return docInChangeShipment
	 * @throws Exception
	 */
	private Document prepareChangeShipmentInput(Element eleShipment, String strItemID) throws Exception{
		Document docInChangeShipment = null;		
		Element eleChangeShipment = null;
		Element eleShipmentLines = null;
		Element eleShipmentLine = null;
		Element eleExtnShipmentLine = null;
		String strShipmentLineKey = null;
		String strExtnReadyForCustomerQty = null;
		String strExtnCustPickedQty = null;

		docInChangeShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleChangeShipment = docInChangeShipment.getDocumentElement();
		eleChangeShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);

		eleShipmentLines = docInChangeShipment.createElement(AcademyConstants.ELE_SHIPMENT_LINES);		
		XMLUtil.appendChild(docInChangeShipment.getDocumentElement(), eleShipmentLines);

		
		eleShipmentLine = docInChangeShipment.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
		strShipmentLineKey = XPathUtil.getString(eleShipment, "ShipmentLines/ShipmentLine[@ItemID='"+ strItemID +"']/@ShipmentLineKey");
		eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, strShipmentLineKey);
		XMLUtil.appendChild(eleShipmentLines, eleShipmentLine);

		eleExtnShipmentLine = docInChangeShipment.createElement(AcademyConstants.ELE_EXTN);

		if(AcademyConstants.STR_ACQUISITION.equals(strOperation)){
			strExtnReadyForCustomerQty = XPathUtil.getString(eleShipment, "ShipmentLines/ShipmentLine[@ItemID='"+ strItemID +"']/Extn/@ExtnSOFRdyForCustPickQty");
			eleExtnShipmentLine.setAttribute(AcademyConstants.ATTR_EXTN_SOF_RDYFORCUSTPICK_QTY, String.valueOf(Double.parseDouble(strExtnReadyForCustomerQty) + 1));
		} else if(AcademyConstants.STR_DISPOSITION.equals(strOperation)){
			strExtnCustPickedQty = XPathUtil.getString(eleShipment,"ShipmentLines/ShipmentLine[@ItemID='"+ strItemID +"']/Extn/@ExtnSOFCustPickedQty");
			eleExtnShipmentLine.setAttribute(AcademyConstants.ATTR_EXTN_SOF_CUSTPICKED_QTY, String.valueOf(Double.parseDouble(strExtnCustPickedQty) + 1));
		}	
		XMLUtil.appendChild(eleShipmentLine, eleExtnShipmentLine);

		return docInChangeShipment;

	}

	/**
	 * Calling getShipmentListForOrder - Input from 'A'/'D' , has only order details. Hence fetching shipment details using orderNo
	 * @param env
	 * @param inDoc
	 * @return docOutGetShipmentListForOrder
	 * @throws Exception
	 */
	private Document getShipmentListForOrder(YFSEnvironment env, String strSalesOrderNo) throws Exception{		
		Document docInGetShipmentListForOrder = null;
		Document docOutGetShipmentListForOrder = null;
		//START OMNI-76206 STS Firearm Integration - Customer received via disposition
		//END OMNI-76206 STS Firearm Integration - Customer received via disposition
		docInGetShipmentListForOrder = prepareGetShipmentListForOrderInput(strSalesOrderNo);
		log.verbose("Input to getShipmentListForOrder : "+XMLUtil.getXMLString(docInGetShipmentListForOrder));
		docOutGetShipmentListForOrder = AcademyUtil.invokeService(env,AcademyConstants.SERV_STS_GET_SHIPMENTLST_FOR_ORDER,
				docInGetShipmentListForOrder);
		log.verbose("Output from getShipmentListForOrder : "+XMLUtil.getXMLString(docOutGetShipmentListForOrder));
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENTLIST_FORORDER);

		return docOutGetShipmentListForOrder;
	}

	/**
	 * Preparing input for getShipmentListForOrder API
	 * @param strSalesOrderNo
	 * @param inDoc
	 * @return docInGetShipmentListForOrder
	 * @throws ParserConfigurationException
	 */
	private Document prepareGetShipmentListForOrderInput(String strSalesOrderNo) throws ParserConfigurationException{		
		Document docInGetShipmentListForOrder = null;
		Element eleGetShipmentListForOrderInput = null;

		docInGetShipmentListForOrder = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		eleGetShipmentListForOrderInput = docInGetShipmentListForOrder.getDocumentElement();
		eleGetShipmentListForOrderInput.setAttribute(AcademyConstants.ATTR_ORDER_NO, strSalesOrderNo);
		eleGetShipmentListForOrderInput.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
		eleGetShipmentListForOrderInput.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);

		return docInGetShipmentListForOrder;
	}

	/**
	 * Method to send the status update to WCS on Acquisition/Disposition
	 * @param env
	 * @throws Exception
	 */
	private void sendStatusUpdateToWCS (YFSEnvironment env) throws Exception {

		log.verbose(" Method to send the status update to WCS ");
		Document docInGetShipmentList = null;
		Document docOutGetShipmentList = null;
		Document docShipment = null;
		Element eleInShipment = null;
		Element eleOutShipments = null;
		Element eleOutShipment = null;

		docInGetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleInShipment = docInGetShipmentList.getDocumentElement();
		eleInShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);

		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, AcademyConstants.XPATH_SHIPMENT_LIST_API_TEMPLATE);
		log.verbose("Input to getShipmentList API: "+XMLUtil.getXMLString(docInGetShipmentList));
		docOutGetShipmentList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, docInGetShipmentList);
		log.verbose("Output of getShipmentList API: "+XMLUtil.getXMLString(docOutGetShipmentList));
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);

		eleOutShipments = docOutGetShipmentList.getDocumentElement();
		eleOutShipment = (Element) eleOutShipments.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
		if (AcademyConstants.STR_ACQUISITION.equals(strOperation)){
			eleOutShipment.setAttribute(AcademyConstants.ATTR_STATUS_CONDITION, AcademyConstants.STR_K );	
		} 
		else if (AcademyConstants.STR_DISPOSITION.equals(strOperation)){
			eleOutShipment.setAttribute(AcademyConstants.ATTR_STATUS_CONDITION, AcademyConstants.STR_V );	
		}

		docShipment = XMLUtil.getDocumentForElement(eleOutShipment);

		log.verbose("Input to the Service AcademySOFSendStatusUpdateToWCS  :: "+XMLUtil.getXMLString(docShipment));
		AcademyUtil.invokeService(env, AcademyConstants.SERVICE_SOF_SEND_STATUS_UPDATE_TO_WCS, docShipment);

	}
	//OMNI-95777 Start : Change shipment to PWI status upon receiving message from GSM for Firearm orders 
	
	/**
	 * Method to change the Shipment status to PWI upon receiving message from GSM
	 * @param env
	 * @throws Exception
	 */
	private void initiatePaperwork(YFSEnvironment env, String strShipmentKey) throws Exception 
	{
		log.verbose("Start AcademySOFAcquisitionDisposition.initiatePaperwork" +strShipmentKey);
		Document indocToPaperWorkService =null;
		Element eleShip = null;
		
		indocToPaperWorkService = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleShip = indocToPaperWorkService.getDocumentElement();
		eleShip.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
		log.verbose("Input to the Service AcademyBeginPaperWorkService  :: "+XMLUtil.getXMLString(indocToPaperWorkService));
		AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_BEGIN_PAPERWORK, indocToPaperWorkService);		
		log.verbose("END AcademySOFAcquisitionDisposition.initiatePaperwork");	
		
	} //OMNI-95777 END
	
}