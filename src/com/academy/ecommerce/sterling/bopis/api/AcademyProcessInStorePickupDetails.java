package com.academy.ecommerce.sterling.bopis.api;

/**#########################################################################################
 *
 * Project Name                : Fulfillment POD
 * Date                        : 20-Apr-2023 
 * Description				  : This class retrieves the InStore Pickup details from WCS and
 * 								the same is updated on the Shipment.
 * 								 
 * ---------------------------------------------------------------------------------
 * Date            Author         		Version#       		Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 20-Apr-2023		EVEREST  	 			  1.0           	Updated version
 *
 * #########################################################################################*/

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyProcessInStorePickupDetails implements YIFCustomApi {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyProcessInStorePickupDetails.class);
	public static final String STR_DATE_TIME_PATTERN_NEW = "yyyyMMddHHmmss";

	// Define properties to fetch service level argument values
	private Properties props;

	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	/**
	 * This method is invoked from the Service to validate and process InStore Pickup
	 * details
	 * 
	 * @param inDoc
	 * @return inDoc
	 * @throws Exception
	 */
	public Document processInStorePickupDetails(YFSEnvironment env, Document inDoc) throws Exception {

		log.beginTimer("AcademyProcessInStorePickupDetails::processInStorePickupDetails");
		log.verbose("Entering AcademyProcessInStorePickupDetails.processInStorePickupDetails() with Input:: "
				+ XMLUtil.getXMLString(inDoc));

		String strShipmentNo;
		String strOrderNo;
		String strStoreNo;
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
			log.verbose("strOrderNo-->"+strOrderNo+" "+"strStoreNo-->"+strStoreNo);
		}

		// Returning an error in case mandatory fields are missing in Input
		if (YFCObject.isVoid(strOrderNo) || YFCObject.isVoid(strShipmentNo)) {
			inDoc = updateResponse(inDoc, AcademyConstants.STATUS_CODE_ERROR,
					AcademyConstants.STR_MANDATORY_PARMS_MISSING_ERROR_CODE);
			return inDoc;
		}

		// Retrieving the shipment details and validating the same.
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
		} 

		log.endTimer("AcademyProcessInStorePickupDetails::processCurbPickupDetials");
		log.verbose("Entering AcademyProcessInStorePickupDetails.processCurbPickupDetials() with output:: "
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

		log.beginTimer("AcademyProcessInStorePickupDetails::getShipmentList");
		log.verbose(" Input strShipmentNo :: " + strShipmentNo + " :: strOrderNo :: " + strOrderNo
				+ " :: strStoreNo :: " + strStoreNo);

		// Prepare API input for getShipmentList
		Document docGetShipmentListInp = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleShipment = docGetShipmentListInp.getDocumentElement();

		eleShipment.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		eleShipment.setAttribute(AcademyConstants.SHIP_NODE, strStoreNo);
		eleShipment.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, AcademyConstants.STR_PICK);

		//Requesting for multiple Shipments on the Order
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

		// Preparing the API template
		Document docShipmentListTemplate = XMLUtil.getDocument("<Shipments><Shipment ShipmentNo='' ShipmentKey='' "
				+ "Status='' OrderNo='' ShipNode='' ><Extn ExtnIsInstorePickupOpted='' /></Shipment></Shipments>");

		// Setting template for API and invoking getShipmentList API
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docShipmentListTemplate);
		Document docGetShipmentListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
				docGetShipmentListInp);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);

		log.verbose("getShipmentList Output " + XMLUtil.getXMLString(docGetShipmentListOut));
		log.endTimer("AcademyProcessInStorePickupDetails::getShipmentList");
		return docGetShipmentListOut;
	}

	/**
	 * This method validates the Shipments, Status and also checks if the in store
	 * details were already updated on the order and based on each of them the
	 * response is updated as ERROR/SUCCESS with the reason and the same is sent as
	 * the service output
	 * 
	 * @param inDoc
	 * @param docShipmentList
	 * @return inDoc
	 * @throws Exception
	 */
	private Document validateShipmentEligibilityForUpdate(Document docShipmentList, Document inDoc) throws Exception {

		log.beginTimer("AcademyProcessInStorePickupDetails::validateShipmentEligibilityForUpdate");
		String strShipmentNo = null;

		if (inDoc.getDocumentElement().hasChildNodes()) {
			log.verbose("Has Child Nodes");
			strShipmentNo = XPathUtil.getString(inDoc.getDocumentElement(), "./Order/@ShipmentNo");
		} else {
			strShipmentNo = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		}

		NodeList nlShipment = XPathUtil.getNodeList(docShipmentList.getDocumentElement(), "/Shipments/Shipment");
		// Condition true only if there are no Shipments available for given OrderNo/ShipmentNo/StoreNo
		if (nlShipment.getLength() == 0) {
			log.verbose(" No shipments eligible iwth the combination. Invalid ShipmentNo/OrderNo ");
			inDoc = updateResponse(inDoc, AcademyConstants.STATUS_CODE_ERROR,
					AcademyConstants.STR_ERROR_INVALID_ORDER_NO);
		}
		// Condition true only if 1 valid shipment exists
		else if (nlShipment.getLength() == 1) {
			log.verbose(" Only 1 shipment eligible. Validate Status.");
			inDoc = validateShipmentStatusForInStore((Element) nlShipment.item(0), inDoc);

		}
		//Handling Instore Update at Order level i.e multiple shipments in single request
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
					inDoc = validateShipmentStatusForInStore(eleShipment, inDoc);
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
		//Edge Case exception
		else {
			log.verbose(" Multiple shipments eligible with the combination. Invalid ShipmentNo/OrderNo ");
			inDoc = updateResponse(inDoc, AcademyConstants.STATUS_CODE_ERROR,
					AcademyConstants.STR_ERROR_INVALID_ORDER_NO);
		}

		log.verbose("update API Output " + XMLUtil.getXMLString(inDoc));
		log.endTimer("AcademyProcessInStorePickupDetails::validateShipmentEligibilityForUpdate");
		return inDoc;
	}

	/**
	 * This method invokes changeShipment to update the details on the Shipment 
	 * 
	 * @param docgetShipmentList
	 * @param inDoc
	 * @throws Exception
	 */
	private void updateShipmentDetails(YFSEnvironment env, Document docGetShipmentList, Document inDoc)
			throws Exception {

		log.beginTimer("AcademyProcessInStorePickupDetails::updateShipmentDetails");
		String strSOMResetInStore = null;
		String strExtnIsInStorePickupOpted = null;

		if (!inDoc.getDocumentElement().hasChildNodes()) {
			strSOMResetInStore = inDoc.getDocumentElement().getAttribute("ResetInstorePickUp");
		}

		if (YFCObject.isVoid(strSOMResetInStore) || !strSOMResetInStore.equalsIgnoreCase(AcademyConstants.STR_YES)) {
			Document docMultiAPIInp = prepareInputForInStoreShipment(inDoc, docGetShipmentList);
			AcademyUtil.invokeAPI(env, AcademyConstants.API_MULTI_API, docMultiAPIInp);
			strExtnIsInStorePickupOpted = AcademyConstants.STR_YES;

		} else if (!inDoc.getDocumentElement().hasChildNodes()) {

			log.verbose("Has No Child Nodes");
			Document docShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element eleShipment = docShipment.getDocumentElement();

			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
					inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
			eleShipment.setAttribute(AcademyConstants.ATTR_ORDER_NO,
					inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO));
			eleShipment.setAttribute("AppointmentNo", "");
			Element eleResetExtn = XmlUtils.createChild(eleShipment, AcademyConstants.ELE_EXTN);
			eleResetExtn.setAttribute(AcademyConstants.ATTR_EXTN_IS_INSTORE_PICKUP_OPTED, "");
			eleResetExtn.setAttribute(AcademyConstants.ATTR_EXTN_INSTORE_PICKUP_INFO, "");
			eleResetExtn.setAttribute(AcademyConstants.ATTR_EXTN_INSTORE_ATTENDED_BY, AcademyConstants.STR_BLANK);

			String strShipmentKeys[] = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY).split(AcademyConstants.STR_COMMA);
			if(strShipmentKeys.length > 1) {
				int iShipmentKey = 0;
				do {
					log.verbose(" :: strShipmentKey :: " + strShipmentKeys[iShipmentKey]);
					
					eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKeys[iShipmentKey]);
					iShipmentKey ++ ;
					// Post the above message to an internal queue for processing. Invokes changeShipment API
					AcademyUtil.invokeService(env, "AcademyUpdateInStoreInfoSyncService", docShipment);
				}
				while (iShipmentKey < strShipmentKeys.length);
			}

			else {
				log.verbose(" docShipment :: " + XMLUtil.getXMLString(docShipment));
				// Post the above message to an internal queue for processing. Invokes
				// changeShipment API
				AcademyUtil.invokeService(env, "AcademyUpdateInStoreInfoSyncService", docShipment);
			}
			
		}
		
		log.endTimer("AcademyProcessInStorePickupDetails::updateShipmentDetails");
	}

	
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
		log.beginTimer("AcademyProcessInStorePickupDetails::updateResponse");

		if (inDoc.getDocumentElement().hasChildNodes()) {
			Element eleOrder = SCXmlUtil.getChildElement(inDoc.getDocumentElement(), AcademyConstants.ELE_ORDER);
			eleOrder.setAttribute(AcademyConstants.ATTR_RESPONSE, strErrorCode);
			eleOrder.setAttribute(AcademyConstants.ATTR_REASON, strErrorDesc);
		} else {
			inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_RESPONSE, strErrorCode);
			inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_REASON, strErrorDesc);
		}

		log.endTimer("AcademyProcessInStorePickupDetails::updateResponse");
		return inDoc;
	}

	/**
	 * This method validates the shipment status and provides the response
	 * 
	 * @param inDoc
	 * @param strErrorCode
	 * @param strErrorDesc
	 * @return inDoc
	 * @throws Exception
	 */
	private Document validateShipmentStatusForInStore(Element eleShipment, Document inDoc) throws Exception {

		log.beginTimer("AcademyProcessInStorePickupDetails::validateShipmentStatusForInStore");
		
		String strSOMResetInStore = null;
		strSOMResetInStore = inDoc.getDocumentElement().getAttribute("ResetInstorePickUp");
		String strStatus = XPathUtil.getString(eleShipment, "./@Status");
		log.verbose(":: strStatus ::" + strStatus);
		// Check if Shipment is in Ready For Customer Status
		if (!YFCObject.isVoid(strStatus) && strStatus.equals(AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS)) {
			log.verbose(" Shipment in a Ready for Customer Status. Valid");
			String strExtnIsInStorePickupOpted = XPathUtil.getString(eleShipment, "./Extn/@ExtnIsInStorePickupOpted");
			log.verbose(":: strExtnIsInStorePickupOpted ::" + strExtnIsInStorePickupOpted);

			if (!YFCObject.isVoid(strExtnIsInStorePickupOpted)
					&& strExtnIsInStorePickupOpted.equals(AcademyConstants.STR_YES)) {
				if (strSOMResetInStore.equals(AcademyConstants.STR_YES)) {
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

		log.endTimer("AcademyProcessInStorePickupDetails::validateShipmentStatusForInStore");
		return inDoc;
		// OMNI-82081 End
	}

	/**
	 * This method is invoked to prepare the API input to update Instore Info at eac Shipment level
	 * 
	 * @param env
	 * @return shipmentConfDoc
	 * @throws Exception
	 */

	private Document prepareInputForInStoreShipment(Document inDoc, Document docGetShipmentList) throws Exception {

		log.beginTimer("AcademyProcessInStorePickupDetails.prepareInputForInStoreShipment() method:: ");

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
				eleAPI.setAttribute(AcademyConstants.ATTR_FLOW_NAME, "AcademyUpdateInStoreInfoSyncService");

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

				//Updating Appointment No as Current time stamp
				String strDateFormat = AcademyConstants.STR_DATE_TIME_PATTERN_NEW;
				SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
				Calendar cal = Calendar.getInstance();
				String strCurrentDate = sDateFormat.format(cal.getTime());
				log.verbose("Appointment date time : " + strCurrentDate);

				eleShipmentInp.setAttribute("AppointmentNo", strCurrentDate);

				Element eleExtn = docChangeShipmentMultiAPI.createElement(AcademyConstants.ELE_EXTN);
				eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_IS_INSTORE_PICKUP_OPTED, AcademyConstants.STR_YES);

				String strInStorePickupInfo;
				// Retrieve Customer Details from input and update on the Shipment
				if (inDoc.getDocumentElement().hasChildNodes()) {
					log.verbose("Has Child Nodes");
					strInStorePickupInfo = XPathUtil.getString(inDoc.getDocumentElement(), "./Order/@CustomerDetails");
				} else {
					strInStorePickupInfo = inDoc.getDocumentElement()
							.getAttribute(AcademyConstants.ATTR_CUSTOMER_DETAILS);
				}

				eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_INSTORE_PICKUP_INFO, strInStorePickupInfo);
				eleShipmentInp.appendChild(eleExtn);
				eleInput.appendChild(eleShipmentInp);
				eleAPI.appendChild(eleInput);
				docChangeShipmentMultiAPI.getDocumentElement().appendChild(eleAPI);
			}

		}

		log.endTimer("AcademyProcessInStorePickupDetails.prepareInputForInStoreShipment() method:: ");
		return docChangeShipmentMultiAPI;
	}
}