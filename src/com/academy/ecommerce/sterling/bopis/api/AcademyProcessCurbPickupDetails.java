package com.academy.ecommerce.sterling.bopis.api;

/**#########################################################################################
*
* Project Name                : OMS_CURB_PICKUP_2020
* Author                      : C0014737
* Author Group				  : CTS-POD
* Date                        : 27-Mar-2020 
* Description				  : This class retrieves the Curb Pickup details from WCS and
* 								the same is updated on the Shipment.
* 								 
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       		Remarks/Description                      
* ---------------------------------------------------------------------------------
* 27-Mar-2020		CTS  	 			  1.0           	Updated version
*
* #########################################################################################*/

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyProcessCurbPickupDetails implements YIFCustomApi {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyProcessCurbPickupDetails.class);
	public static final String STR_DATE_TIME_PATTERN_NEW = "yyyyMMddHHmmss";

	// Define properties to fetch service level argument values
	private Properties props;

	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	/**
	 * This method is invoked from the Service to validate and process Curb Pickup
	 * details
	 * 
	 * @param inDoc
	 * @return inDoc
	 * @throws Exception
	 */
	public Document processCurbPickupDetials(YFSEnvironment env, Document inDoc) throws Exception {

		log.beginTimer("AcademyProcessCurbPickupDetails::processCurbPickupDetials");
		log.verbose("Entering AcademyProcessCurbPickupDetails.processCurbPickupDetials() with Input:: "
				+ XMLUtil.getXMLString(inDoc));

		String strShipmentNo;
		String strOrderNo;
		String strStoreNo;
		// OMNI - 10548 Curbside Shipment List
		String strShipmentKey = null;
		// String strSOMResetCurbside = null;
		Document docGetShipmentList = null;
		String strResponse = null;

		if (inDoc.getDocumentElement().hasChildNodes()) {
			log.verbose("Has Child Nodes");
			strShipmentNo = XPathUtil.getString(inDoc.getDocumentElement(), "./Order/@ShipmentNo");
			strOrderNo = XPathUtil.getString(inDoc.getDocumentElement(), "./Order/@OrderNo");
			strStoreNo = XPathUtil.getString(inDoc.getDocumentElement(), "./Order/@StoreNo");
		} else {
			strShipmentNo = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			strOrderNo = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO);
			strStoreNo = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_STORE_NO);
			// OMNI - 10548 Curbside Shipment List
			strShipmentKey = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			log.verbose("strOrderNo-->"+strOrderNo+" "+"strStoreNo-->"+strStoreNo+" "+"strShipmentKey-->"+strShipmentKey);
			// strSOMResetCurbside =
			// inDoc.getDocumentElement().getAttribute("ResetCurbside");
		}

		// OMNI - 10548 Curbside Shipment List
		// If shipmentkey is null, invoke getShipmentList
		if (YFCObject.isVoid(strShipmentKey)) {
			// Returning an error in case mandatory fields are missing in Input
			if (YFCObject.isVoid(strOrderNo) || YFCObject.isVoid(strShipmentNo)) {
				inDoc = updateResponse(inDoc, AcademyConstants.STATUS_CODE_ERROR,
						AcademyConstants.STR_MANDATORY_PARMS_MISSING_ERROR_CODE);
				return inDoc;
			}

			// Retrieveing the shipment details and validating the same.

			try {
				docGetShipmentList = getShipmentList(env, strShipmentNo, strOrderNo, strStoreNo);
			} catch (Exception e) {
				log.info(" Failed fetching Shipment data from OMS. :: " + e.getMessage());
				log.info(" Error Trace :: " + e.toString());
				inDoc = updateResponse(inDoc, AcademyConstants.STATUS_CODE_ERROR, "OMS Failure");
				return inDoc;
			}

			// Validating if shipment is valid and is already not updated and not
			// processed/cancelled
			inDoc = validateShipmentEligibilityForUpdate(docGetShipmentList, inDoc);

			if (inDoc.getDocumentElement().hasAttribute(AcademyConstants.ATTR_RESPONSE)) {
				strResponse = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE);
			} else {
				strResponse = XPathUtil.getString(inDoc.getDocumentElement(), "./Order/@Response");
			}

		}

		// Update Customer details on Shipment and send Notification
		if (!YFCObject.isVoid(strResponse) && strResponse.equals(AcademyConstants.STR_SUCCESS)) {
			log.verbose(" Update the details on Shipment.");
			try {
				updateShipmentDetails(env, docGetShipmentList, inDoc);
			} catch (Exception e) {
				log.info(" Failed to Update the data in OMS. :: " + e.getMessage());
				log.info(" Error Trace :: " + e.toString());
				inDoc = updateResponse(inDoc, AcademyConstants.STATUS_CODE_ERROR,
						AcademyConstants.STR_ERROR_OMS_FAILURE);
			}
		} else if (!YFCObject.isVoid(strShipmentKey)) {
			updateShipmentDetails(env, null, inDoc);
		}

		log.endTimer("AcademyProcessCurbPickupDetails::processCurbPickupDetials");
		log.verbose("Entering AcademyProcessCurbPickupDetails.processCurbPickupDetials() with output:: "
				+ XMLUtil.getXMLString(inDoc));

		return inDoc;
	}

	/**
	 * This method prepares input and invokes getShipmentList API with ShipmentNo,
	 * OrderNo and ShipNode. It only searches for PICK-UP Orders
	 * 
	 * @param strShipmentNo
	 * @param strOrderNo
	 * @param strStoreNo
	 * @return docGetShipmentListOut
	 * @throws Exception
	 */
	private Document getShipmentList(YFSEnvironment env, String strShipmentNo, String strOrderNo, String strStoreNo)
			throws Exception {

		log.beginTimer("AcademyProcessCurbPickupDetails::getShipmentList");
		log.verbose(" Input strShipmentNo :: " + strShipmentNo + " :: strOrderNo :: " + strOrderNo
				+ " :: strStoreNo :: " + strStoreNo);

		// Prepare API input for getShipmentList
		Document docGetShipmentListInp = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleShipment = docGetShipmentListInp.getDocumentElement();

		eleShipment.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		eleShipment.setAttribute(AcademyConstants.SHIP_NODE, strStoreNo);
		eleShipment.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, AcademyConstants.STR_PICK);

		// Start : OMNI-82081 : Updating curbside at Order Level
		if (strShipmentNo.contains(AcademyConstants.STR_COMMA)) {
			log.verbose(" Request contains multiple ShipmentNo ");
			String[] strArryShipmentNo = strShipmentNo.split(AcademyConstants.STR_COMMA);

			Element eleComplexQry = SCXmlUtil.createChild(eleShipment, AcademyConstants.COMPLEX_QRY_ELEMENT);
			eleComplexQry.setAttribute(AcademyConstants.COMPLEX_OPERATOR_ATTR,
					AcademyConstants.COMPLEX_OPERATOR_AND_VAL);

			Element eleOr = SCXmlUtil.createChild(eleComplexQry, AcademyConstants.COMPLEX_OR_ELEMENT);

			for (int iShipNo = 0; iShipNo < strArryShipmentNo.length; iShipNo++) {
				Element eleExp = SCXmlUtil.createChild(eleOr, AcademyConstants.COMPLEX_EXP_ELEMENT);
				eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
				eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_SHIPMENT_NO);
				eleExp.setAttribute(AcademyConstants.ATTR_VALUE, strArryShipmentNo[iShipNo]);

				eleOr.appendChild(eleExp);
			}

			eleComplexQry.appendChild(eleOr);
			eleShipment.appendChild(eleComplexQry);
		} else {
			docGetShipmentListInp.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		}
		// End : OMNI-82081 : Updating curbside at Order Level

		// Preparing the API template
		Document docShipmentListTemplate = XMLUtil.getDocument("<Shipments><Shipment ShipmentNo='' ShipmentKey='' "
				+ "Status='' OrderNo='' ShipNode='' ><Extn ExtnIsCurbsidePickupOpted='' /></Shipment></Shipments>");

		// Setting template for API and invoking getShipmentList API
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docShipmentListTemplate);
		Document docGetShipmentListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
				docGetShipmentListInp);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);

		log.verbose("getShipmentList Output " + XMLUtil.getXMLString(docGetShipmentListOut));
		log.endTimer("AcademyProcessCurbPickupDetails::getShipmentList");
		return docGetShipmentListOut;
	}

	/**
	 * This method validates the Shipments, Status and also checks if the curb side
	 * details were already updated on the order and based on each of them the
	 * response is updated as ERROR/SUCCESS with the reason and the smae is sent as
	 * the service output
	 * 
	 * @param inDoc
	 * @param docShipmentList
	 * @return inDoc
	 * @throws Exception
	 */
	private Document validateShipmentEligibilityForUpdate(Document docShipmentList, Document inDoc) throws Exception {

		log.beginTimer("AcademyProcessCurbPickupDetails::validateShipmentEligibilityForUpdate");
		// Start: OMNI-82081 : Updating curbside at Order Level
		String strShipmentNo = null;

		if (inDoc.getDocumentElement().hasChildNodes()) {
			log.verbose("Has Child Nodes");
			strShipmentNo = XPathUtil.getString(inDoc.getDocumentElement(), "./Order/@ShipmentNo");
		} else {
			strShipmentNo = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		}
		// End: OMNI-82081 : Updating curbside at Order Level
		NodeList nlShipment = XPathUtil.getNodeList(docShipmentList.getDocumentElement(), "/Shipments/Shipment");
		// Condition true only if there are no Shipments available for given
		// OrderNo/ShipmentNo/StoreNo
		if (nlShipment.getLength() == 0) {
			log.verbose(" No shipments eligible iwth the combination. Invalid ShipmentNo/OrderNo ");
			inDoc = updateResponse(inDoc, AcademyConstants.STATUS_CODE_ERROR,
					AcademyConstants.STR_ERROR_INVALID_ORDER_NO);
		}
		// Condition true only if 1 valid shipment exists
		else if (nlShipment.getLength() == 1) {
			log.verbose(" Only 1 shipment eligible. Validate Status.");
			inDoc = validateShipmentStatusForCurbside((Element) nlShipment.item(0), inDoc);

		}
		// Start: OMNI-82081 : Updating curbside at Order Level
		else if (!YFCObject.isVoid(strShipmentNo) && strShipmentNo.contains(AcademyConstants.STR_COMMA)) {
			log.verbose(" Input contains multiple Shipment Details. Validating the same ");

			String[] strArrayShipmentNo = strShipmentNo.split(AcademyConstants.STR_COMMA);
			List<String> lUpdatedShipment = new ArrayList<String>();

			if (nlShipment.getLength() == strArrayShipmentNo.length) {
				log.verbose(" Validating status for each shipment ");

				for (int iShipNo = 0; iShipNo < strArrayShipmentNo.length; iShipNo++) {
					String strReqShipmentNo = strArrayShipmentNo[iShipNo];
					Element eleShipment = (Element) XPathUtil.getNode(docShipmentList,
							"/Shipments/Shipment[@ShipmentNo='" + strReqShipmentNo + "']");
					inDoc = validateShipmentStatusForCurbside(eleShipment, inDoc);
					String strResponse = null;
					String strReason = null;

					if (inDoc.getDocumentElement().hasAttribute(AcademyConstants.ATTR_RESPONSE)) {
						strResponse = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE);
						strReason = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_REASON);
					} else {
						strResponse = XPathUtil.getString(inDoc.getDocumentElement(), "./Order/@Response");
						strReason = XPathUtil.getString(inDoc.getDocumentElement(), "./Order/@Reason");
					}

					// Verify if the Shipment has any error and return the same response
					if (!YFCObject.isVoid(strResponse) && strResponse.equals(AcademyConstants.STATUS_CODE_ERROR)) {
						log.verbose(" Validating status for each shipment ");
						if (strReason.equals(AcademyConstants.STR_ERROR_DUPLICATE_UPDATE)) {
							lUpdatedShipment.add(strReqShipmentNo);
						} else {
							break;
						}

					}
				}
				log.verbose(" strUpdatedShipment ::  " + lUpdatedShipment.toString());

				if (lUpdatedShipment.size() > 0 && lUpdatedShipment.size() == strArrayShipmentNo.length) {
					log.verbose("All shipments in the request were already updated. Throw error");
					inDoc = updateResponse(inDoc, AcademyConstants.STATUS_CODE_ERROR,
							AcademyConstants.STR_ERROR_DUPLICATE_UPDATE);
				} else if (lUpdatedShipment.size() > 0) {
					log.verbose("Some Shipments are pending update");
					inDoc = updateResponse(inDoc, AcademyConstants.STR_SUCCESS, AcademyConstants.STR_NONE);
					inDoc.getDocumentElement().setAttribute("DuplicateShipments", lUpdatedShipment.toString());
				}
			} else {
				log.verbose(" Mismatch between ShipmentNo passed and ShipmentList ");
				inDoc = updateResponse(inDoc, AcademyConstants.STATUS_CODE_ERROR,
						"Shipment in Request mismatch in OMS");
			}
		}
		// End: OMNI-82081 : Updating curbside at Order Level
		else {
			log.verbose(" Multiple shipments eligible with the combination. Invalid ShipmentNo/OrderNo ");
			inDoc = updateResponse(inDoc, AcademyConstants.STATUS_CODE_ERROR,
					AcademyConstants.STR_ERROR_INVALID_ORDER_NO);
		}

		log.verbose("update API Output " + XMLUtil.getXMLString(inDoc));
		log.endTimer("AcademyProcessCurbPickupDetails::validateShipmentEligibilityForUpdate");
		return inDoc;
	}

	/**
	 * This method invokes changeShipment to update the details on the Shipment and
	 * also triggers the TC70 Ready For Curb Pickup notifications
	 * 
	 * @param docgetShipmentList
	 * @param inDoc
	 * @throws Exception
	 */
	private void updateShipmentDetails(YFSEnvironment env, Document docGetShipmentList, Document inDoc)
			throws Exception {

		log.beginTimer("AcademyProcessCurbPickupDetails::updateShipmentDetails");

		// OMNI - 10548 Curbside Shipment List
		String strSOMResetCurbside = null;
		// String strShipmentKey = null;
		String strExtnIsCurbsidePickupOpted = null;

		if (!inDoc.getDocumentElement().hasChildNodes()) {
			strSOMResetCurbside = inDoc.getDocumentElement().getAttribute("ResetCurbside");
		}
		// OMNI - 10548 Curbside Shipment List

		if (YFCObject.isVoid(strSOMResetCurbside) || !strSOMResetCurbside.equalsIgnoreCase(AcademyConstants.STR_YES)) {
			Document docMultiAPIInp = prepareInputForCurbsideShipment(inDoc, docGetShipmentList);
			AcademyUtil.invokeAPI(env, AcademyConstants.API_MULTI_API, docMultiAPIInp);
			strExtnIsCurbsidePickupOpted = AcademyConstants.STR_YES;

			// OMNI - 10548 Curbside Shipment List
		} else if (!inDoc.getDocumentElement().hasChildNodes()) {

			log.verbose("Has No Child Nodes");
			Document docShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element eleShipment = docShipment.getDocumentElement();

			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
					inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
			eleShipment.setAttribute(AcademyConstants.ATTR_ORDER_NO,
					inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO));
			// eleShipment.setAttribute(AcademyConstants.ATTR_SHIP_NODE,
			// inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE));
			// eleShipment.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD,
			// inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD));
			eleShipment.setAttribute("AppointmentNo", "");
			Element eleResetExtn = XmlUtils.createChild(eleShipment, AcademyConstants.ELE_EXTN);
			eleResetExtn.setAttribute(AcademyConstants.ATTR_EXTN_CURSIDE_PICK_OPTED, "");
			eleResetExtn.setAttribute(AcademyConstants.ATTR_EXTN_CURSIDE_PICK_INFO, "");
			//OMNI-105674 - Start
			eleResetExtn.setAttribute(AcademyConstants.ATTR_EXTN_IS_INSTORE_PICKUP_OPTED, "");
			eleResetExtn.setAttribute(AcademyConstants.ATTR_EXTN_INSTORE_PICKUP_INFO, "");
			eleResetExtn.setAttribute(AcademyConstants.ATTR_EXTN_INSTORE_ATTENDED_BY, AcademyConstants.STR_BLANK);
			//OMNI-105674 - End
			//OMNI-81257  - START
			eleResetExtn.setAttribute(AcademyConstants.ATTR_CURBSIDE_DELAY_COUNT, AcademyConstants.STR_ZERO);
			eleResetExtn.setAttribute(AcademyConstants.ATTR_CURBSIDE_DELAY_MINS,AcademyConstants.STR_ZERO);
			eleResetExtn.setAttribute(AcademyConstants.ATTR_CURBSIDE_DELAY_REQS_TS, AcademyConstants.STR_EMPTY_STRING);
			eleResetExtn.setAttribute(AcademyConstants.ATTR_CURBSIDE_EXP_DEL_TS, AcademyConstants.STR_EMPTY_STRING);
			eleResetExtn.setAttribute(AcademyConstants.EXTN_CURBSIDE_ATTENDED_BY, AcademyConstants.STR_BLANK);
			//OMNI-81257  - END
			//Changes for OMNI-85022 Start
			String strShipmentKeys[] = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY).split(AcademyConstants.STR_COMMA);
			if(strShipmentKeys.length > 1) {
				int iShipmentKey = 0;
				do {
					log.verbose(" :: strShipmentKey :: " + strShipmentKeys[iShipmentKey]);
					
					eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKeys[iShipmentKey]);
					iShipmentKey ++ ;
					// Post the above message to an internal queue for processing. Invokes changeShipment API
					AcademyUtil.invokeService(env, "AcademyUpdateCurbSideInfoSyncService", docShipment);
				}
				while (iShipmentKey < strShipmentKeys.length);
			}
			//OMNI-85172 End : Reset Curbside for multiple shipments
			else {
				// OMNI - 10548 Curbside Shipment List
				log.verbose(" docShipment :: " + XMLUtil.getXMLString(docShipment));
				// Post the above message to an internal queue for processing. Invokes
				// changeShipment API
				AcademyUtil.invokeService(env, "AcademyUpdateCurbSideInfoSyncService", docShipment);
			}
			//Changes for OMNI-85022 End

			
		}

		// OMNI - 54336 & OMNI - 54337 - Curbside Reminder/Escalation -- Start
		// If Curbside is Enabled create task q entry
		if (!YFCObject.isVoid(strExtnIsCurbsidePickupOpted)
				&& strExtnIsCurbsidePickupOpted.equals(AcademyConstants.STR_YES)) {
			log.verbose(
					"Curbside is Opted, so creating the task q record for sending Curbside Reminder/Escalation Notifications");
			String strDuplicateShipment = inDoc.getDocumentElement().getAttribute("DuplicateShipments");
			log.verbose(" :: strDuplicateShipment :: " + strDuplicateShipment);

			prepareManageTaskQueueInput(env, docGetShipmentList, strDuplicateShipment);
		}
		log.endTimer("AcademyProcessCurbPickupDetails::updateShipmentDetails");
	}

	/**
	 * Preparing input for manageTaskQueue API
	 * 
	 * @param env
	 * @param docGetShipmentList
	 * @return docInManageTaskQueue
	 * @throws Exception
	 * 
	 *                   <TaskQueue AvailableDate=
	 *                   "SystemDate+InitialReminderSLA(10Mnts)" DataKey=
	 *                   "2019080906191417896577" DataType="ShipmentKey"
	 *                   TransactionId="ACAD_CURBSIDE.0001.ex"/>
	 */

	private Document prepareManageTaskQueueInput(YFSEnvironment env, Document docGetShipmentList,
			String strDuplicateShipment) throws Exception {
		log.beginTimer("AcademyProcessCurbPickupDetails.prepareManageTaskQueueInput()");

		Document docInManageTaskQueue = null;
		Element eleInManageTaskQueue = null;
		SimpleDateFormat sdfDateFormat = null;
		String strInitialReminderSLA = null;
		String strNextAvailDate = null;

		// Fetch the InitialReminderSLAMinutes from service argument
		strInitialReminderSLA = props.getProperty(AcademyConstants.STR_INITIAL_REMINDER_SLA_MINTS);

		// Get the SystemDate and Add Initial Reminder SLA(configured value) as next
		// available date
		Calendar cal = Calendar.getInstance();
		sdfDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		cal.add(Calendar.MINUTE, Integer.parseInt(strInitialReminderSLA));
		strNextAvailDate = sdfDateFormat.format(cal.getTime());

		// Start : OMNI-82081 : Updating curbside at Order Level
		NodeList nlShipment = XPathUtil.getNodeList(docGetShipmentList.getDocumentElement(), "/Shipments/Shipment");

		for (int i = 0; i < nlShipment.getLength(); i++) {

			String strTaskQDataKey = ((Element) nlShipment.item(i)).getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			String strShipmentNo = ((Element) nlShipment.item(i)).getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);

			if (YFCObject.isVoid(strDuplicateShipment)
					|| (!YFCObject.isVoid(strDuplicateShipment) && !strDuplicateShipment.contains(strShipmentNo))) {
				// manageTaskQueue API inDoc
				docInManageTaskQueue = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
				eleInManageTaskQueue = docInManageTaskQueue.getDocumentElement();
				eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, strNextAvailDate);
				eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY, strTaskQDataKey);
				eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE, AcademyConstants.ATTR_SHIPMENT_KEY);
				eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_TRANS_ID,
						AcademyConstants.ACAD_CURBSIDE_NOTIFICATIONS_TRAN_ID);

				log.verbose("TaskQ Entry for Curbside Notifications :: " + SCXmlUtil.getString(docInManageTaskQueue));
				AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_TASK_QUEUE, docInManageTaskQueue);
			}

		}

		// End : OMNI-82081 : Updating curbside at Order Level

		log.endTimer("AcademyProcessCurbPickupDetails.prepareManageTaskQueueInput()");
		return docInManageTaskQueue;
	}
	// OMNI - 54336 & OMNI - 54337 - Curbside Reminder/Escalation -- End

	/**
	 * This method preparesupdates the input with the success and failure reasons
	 * 
	 * @param inDoc
	 * @param strErrorCode
	 * @param strErrorDesc
	 * @return inDoc
	 * @throws Exception
	 */
	private Document updateResponse(Document inDoc, String strErrorCode, String strErrorDesc) throws Exception {
		log.beginTimer("AcademyProcessCurbPickupDetails::updateResponse");

		if (inDoc.getDocumentElement().hasChildNodes()) {
			Element eleOrder = SCXmlUtil.getChildElement(inDoc.getDocumentElement(), AcademyConstants.ELE_ORDER);
			eleOrder.setAttribute(AcademyConstants.ATTR_RESPONSE, strErrorCode);
			eleOrder.setAttribute(AcademyConstants.ATTR_REASON, strErrorDesc);
		} else {
			inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_RESPONSE, strErrorCode);
			inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_REASON, strErrorDesc);
		}

		log.endTimer("AcademyProcessCurbPickupDetails::updateResponse");
		return inDoc;
	}

	// OMNI-82081 Start
	/**
	 * This method validates the shipment status and provides the response
	 * 
	 * @param inDoc
	 * @param strErrorCode
	 * @param strErrorDesc
	 * @return inDoc
	 * @throws Exception
	 */
	private Document validateShipmentStatusForCurbside(Element eleShipment, Document inDoc) throws Exception {

		log.beginTimer("AcademyProcessCurbPickupDetails::validateShipmentStatusForCurbside");
		// OMNI - 10548 Curbside Shipment List
		String strSOMResetCurbside = null;
		strSOMResetCurbside = inDoc.getDocumentElement().getAttribute("ResetCurbside");
		// OMNI - 10548 Curbside Shipment List

		String strStatus = XPathUtil.getString(eleShipment, "./@Status");
		log.verbose(":: strStatus ::" + strStatus);
		// Check if Shipment is in Ready For Customer Status
		if (!YFCObject.isVoid(strStatus) && strStatus.equals(AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS)) {
			log.verbose(" Shipment in a Ready for Customer Status. Valid");
			String strExtnIsCurbsidePickupOpted = XPathUtil.getString(eleShipment, "./Extn/@ExtnIsCurbsidePickupOpted");
			log.verbose(":: strExtnIsCurbsidePickupOpted ::" + strExtnIsCurbsidePickupOpted);

			if (!YFCObject.isVoid(strExtnIsCurbsidePickupOpted)
					&& strExtnIsCurbsidePickupOpted.equals(AcademyConstants.STR_YES)) {

				if (strSOMResetCurbside.equals(AcademyConstants.STR_YES)) {
					inDoc = updateResponse(inDoc, AcademyConstants.STR_SUCCESS, AcademyConstants.STR_NONE);
				} else {
					log.verbose(" Shipment is already updated with detials ");
					inDoc = updateResponse(inDoc, AcademyConstants.STATUS_CODE_ERROR,
							AcademyConstants.STR_ERROR_DUPLICATE_UPDATE);
				}
			} else {
				inDoc = updateResponse(inDoc, AcademyConstants.STR_SUCCESS, AcademyConstants.STR_NONE);
			}
		}
		// Check if shipment is already cancelled
		else if (!YFCObject.isVoid(strStatus) && strStatus.equals(AcademyConstants.VAL_CANCELLED_STATUS)) {
			inDoc = updateResponse(inDoc, AcademyConstants.STATUS_CODE_ERROR, AcademyConstants.STR_STATUS_CANCELLED);
		}
		// Check if Shipment is already Picked up
		else if (!YFCObject.isVoid(strStatus) && Integer.parseInt(strStatus.substring(0, 4)) >= 1400) {
			inDoc = updateResponse(inDoc, AcademyConstants.STATUS_CODE_ERROR, AcademyConstants.STR_PICKED_UP);
		}
		// None of the statys are valid for Curb Pickup Updates
		else {
			inDoc = updateResponse(inDoc, AcademyConstants.STATUS_CODE_ERROR,
					AcademyConstants.STR_ERROR_INVALID_STATUS);
		}

		log.endTimer("AcademyProcessCurbPickupDetails::validateShipmentStatusForCurbside");
		return inDoc;
		// OMNI-82081 End
	}

	// OMNI-82081 Start
	/**
	 * This method is invoked to prepare the API input to cancel Shipment
	 * 
	 * @param env
	 * @return shipmentConfDoc
	 * @throws Exception
	 */

	private Document prepareInputForCurbsideShipment(Document inDoc, Document docGetShipmentList) throws Exception {

		log.beginTimer("AcademyProcessCurbPickupDetails.prepareInputForCurbsideShipment() method:: ");

		Document docChangeShipmentMultiAPI = XMLUtil.createDocument(AcademyConstants.ELE_MULTIAPI);

		String strDuplicateShipment = inDoc.getDocumentElement().getAttribute("DuplicateShipments");
		log.verbose(" :: strDuplicateShipment :: " + strDuplicateShipment);

		// Loop through the doc for each shipment line, and prepare API inp to cancel
		// Shipment
		NodeList nlShipment = docGetShipmentList.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
		for (int i = 0; i < nlShipment.getLength(); i++) {
			Element eleShipment = (Element) nlShipment.item(i);

			if (YFCObject.isVoid(strDuplicateShipment) || (!YFCObject.isVoid(strDuplicateShipment)
					&& !strDuplicateShipment.contains(XPathUtil.getString(eleShipment, "./@ShipmentNo")))) {

				log.verbose("Preparing multi API for  : " + XPathUtil.getString(eleShipment, "./@ShipmentNo"));

				Element eleAPI = docChangeShipmentMultiAPI.createElement(AcademyConstants.ELE_API);
				eleAPI.setAttribute(AcademyConstants.ATTR_FLOW_NAME, "AcademyUpdateCurbSideInfoSyncService");

				// Prepare input for MultiPAI for multiple shipments
				Element eleInput = docChangeShipmentMultiAPI.createElement(AcademyConstants.ELE_INPUT);
				Element eleShipmentInp = docChangeShipmentMultiAPI.createElement(AcademyConstants.ELE_SHIPMENT);

				eleShipmentInp.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
						XPathUtil.getString(eleShipment, "./@ShipmentKey"));
				eleShipmentInp.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO,
						XPathUtil.getString(eleShipment, "./@ShipmentNo"));
				eleShipmentInp.setAttribute(AcademyConstants.ATTR_ORDER_NO,
						XPathUtil.getString(eleShipment, "./@OrderNo"));
				eleShipmentInp.setAttribute(AcademyConstants.ATTR_SHIP_NODE,
						XPathUtil.getString(eleShipment, "./@ShipNode"));

				// OMNI - 10548 Curbside Shipment List

				String strDateFormat = AcademyConstants.STR_DATE_TIME_PATTERN_NEW;
				SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
				Calendar cal = Calendar.getInstance();
				String strCurrentDate = sDateFormat.format(cal.getTime());
				log.verbose("Appointment date time : " + strCurrentDate);

				eleShipmentInp.setAttribute("AppointmentNo", strCurrentDate);

				// OMNI - 10548 Curbside Shipment List

				Element eleExtn = docChangeShipmentMultiAPI.createElement(AcademyConstants.ELE_EXTN);
				eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_CURSIDE_PICK_OPTED, AcademyConstants.STR_YES);

				String strCurbSidePickupInfo;
				// Retrieve Customer Details from input and update on the Shipment
				if (inDoc.getDocumentElement().hasChildNodes()) {
					log.verbose("Has Child Nodes");
					strCurbSidePickupInfo = XPathUtil.getString(inDoc.getDocumentElement(), "./Order/@CustomerDetails");
				} else {
					strCurbSidePickupInfo = inDoc.getDocumentElement()
							.getAttribute(AcademyConstants.ATTR_CUSTOMER_DETAILS);
				}

				eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_CURSIDE_PICK_INFO, strCurbSidePickupInfo);
				eleShipmentInp.appendChild(eleExtn);
				eleInput.appendChild(eleShipmentInp);
				eleAPI.appendChild(eleInput);
				docChangeShipmentMultiAPI.getDocumentElement().appendChild(eleAPI);
			}

		}

		log.endTimer("AcademyProcessCurbPickupDetails.prepareInputForCurbsideShipment() method:: ");
		return docChangeShipmentMultiAPI;
	}
	// OMNI-82081 End
}