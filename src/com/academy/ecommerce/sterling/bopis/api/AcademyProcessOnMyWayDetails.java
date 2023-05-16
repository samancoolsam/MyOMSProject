package com.academy.ecommerce.sterling.bopis.api;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyProcessOnMyWayDetails {
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyProcessOnMyWayDetails.class);
	
	/** This method process the On My Details and updates EXTN_ON_MY_WAY_OPTED accordingly.
	 * @param env
	 * @param inDoc
	 * @return
	 */
	public Document processOnMyWayDetails(YFSEnvironment env, Document inDoc) {
		
		log.beginTimer("AcademyProcessOnMyWayDetails::processOnMyWayDetails");
		log.verbose("Entering AcademyProcessOnMyWayDetails.processOnMyWayDetails() with Input::\n "
				+ XMLUtil.getXMLString(inDoc));
		
		String strShipmentNo;
		String strOrderNo;
		String strStoreNo;
		String strOnMyWay = "";
		Document docGetShipmentList = null;
		String strResponse = null;
		
		try {
			
			if(!YFCCommon.isVoid(inDoc)) {
				Element eleInput = inDoc.getDocumentElement();
				strShipmentNo = XPathUtil.getString(eleInput, AcademyConstants.XPATH_OMW_SHIPMENT_NO);
				strOrderNo = XPathUtil.getString(eleInput, AcademyConstants.XPATH_OMW_ORDER_NO);
				strStoreNo = XPathUtil.getString(eleInput, AcademyConstants.XPATH_OMW_STORE_NO);
				strOnMyWay = XPathUtil.getString(eleInput, AcademyConstants.XPATH_OMW_FLAG);
				
				if(YFCObject.isVoid(strOrderNo) || YFCObject.isVoid(strShipmentNo)
						|| YFCObject.isVoid(strOnMyWay)) {
					inDoc = updateResponse(inDoc, AcademyConstants.STATUS_CODE_ERROR,
							AcademyConstants.STR_MANDATORY_PARMS_MISSING_ERROR_CODE);
					return inDoc;
				}
				
				if(!YFCObject.isVoid(strOnMyWay) && strOnMyWay.equals(AcademyConstants.STR_YES)) {
					docGetShipmentList = getShipmentList(env, strShipmentNo, strOrderNo, strStoreNo);
					inDoc = validateShipmentEligibilityForUpdate(docGetShipmentList, inDoc, strOnMyWay);
					
				} else if(!YFCObject.isVoid(strOnMyWay) && !strOnMyWay.equals(AcademyConstants.STR_YES)) {
					inDoc = updateResponse(inDoc, AcademyConstants.STATUS_CODE_ERROR, AcademyConstants.INVALID_ON_MY_WAY);
				}
				strResponse = XPathUtil.getString(eleInput, AcademyConstants.XPATH_OMW_RESPONSE);
				
				if(!YFCObject.isVoid(strResponse) && strResponse.equals(AcademyConstants.STR_SUCCESS)) {
					log.verbose(" Update the details on Shipment.");
					updateShipmentDetails(env, docGetShipmentList);
				}
			}
		} catch (Exception e) {
			e.printStackTrace(); 
			log.verbose("Exception occurred while invoking AcademyProcessOnMyWayDetails.processOnMyWayDetails "
					+ "with input:\n" + XMLUtil.getXMLString(inDoc));
			log.info(" Error Trace :: "+e.toString());
			inDoc = updateResponse(inDoc, AcademyConstants.STATUS_CODE_ERROR, AcademyConstants.STR_ERROR_OMS_FAILURE);
			return inDoc;
		}
		return inDoc;
		
	}
	
	/**
	 * This method validates the Shipments, Status and also checks if the ON My Way flag is already
	 * updated on the Shipment and based on that the response is updated as ERROR/SUCCESS with 
	 * the reason and the same is sent as the service output.
	 *
	 * @param docShipmentList
	 * @param inDoc
	 * @param strOnMyWay
	 * @return
	 */
	private Document validateShipmentEligibilityForUpdate(Document docShipmentList, Document inDoc, String strOnMyWay) {
		log.beginTimer("AcademyProcessOnMyWayDetails::validateShipmentEligibilityForUpdate");
		String strReason = null;
		String strResponse = AcademyConstants.STATUS_CODE_ERROR;
		try {
			NodeList nlShipment = XPathUtil.getNodeList(docShipmentList.getDocumentElement(), AcademyConstants.XPATH_SHIPMENT_LIST);
			if (nlShipment.getLength() == 1) {
				String strStatus = XPathUtil.getString(docShipmentList, AcademyConstants.XPATH_SHIPMENT_STATUS);
				log.verbose("Only one eligible shipment is available with status :: " + strStatus);
				if(!YFCObject.isVoid(strStatus) && strStatus.equals(AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS)) {
					log.verbose(" Shipment in a valid status: Ready for Customer Pickup Status.");
					log.verbose(":: strExtnOnMyWayOpted ::" + strOnMyWay);
					String strExtnOnMyWay = XPathUtil.getString(docShipmentList, AcademyConstants.XPATH_SHIPMENT_ON_MY_WAY);
					if(!YFCObject.isVoid(strExtnOnMyWay) && strExtnOnMyWay.equals(AcademyConstants.STR_YES)) {
						strReason = AcademyConstants.STR_ERROR_DUPLICATE_UPDATE;
					} else if (!YFCObject.isVoid(strExtnOnMyWay) && strExtnOnMyWay.equals(AcademyConstants.STR_ACK)) {
						strReason = AcademyConstants.STR_ALREADY_ACK;
						strResponse = AcademyConstants.STR_SUCCESS;
					} else if(!YFCObject.isVoid(strOnMyWay) && strOnMyWay.equals(AcademyConstants.STR_YES)) {						
						strReason = AcademyConstants.STR_NONE;
						strResponse = AcademyConstants.STR_SUCCESS;
					} 
				} else {
					strReason = AcademyConstants.STR_ERROR_INVALID_STATUS;
				}
	 		} else {
				log.verbose(" No/ Multiple shipments eligible with the combination. Invalid ShipmentNo/ OrderNo ");
				strReason = AcademyConstants.NO_VALID_SHIPMENTS_AVAILABLE;			
			}
			inDoc = updateResponse(inDoc, strResponse, strReason);
		} catch (Exception e) {
			e.printStackTrace(); 
			log.verbose("Exception at AcademyProcessOnMyWayDetails.validateShipmentEligibilityForUpdate");
		}		
		log.endTimer("AcademyProcessOnMyWayDetails::validateShipmentEligibilityForUpdate");
		return inDoc;
	}
	
	/**
	 * This method invokes changeShipment to update the OnMyWay flag on the Shipment and 
	 * also triggers the TC70 On My Way notifications. 
	 * 
	 * @param env
	 * @param docGetShipmentList
	 */
	private void updateShipmentDetails(YFSEnvironment env, Document docGetShipmentList) {
		log.beginTimer("AcademyProcessOnMyWayDetails::updateShipmentDetails");
		try {
			Document docShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element eleShip = docShipment.getDocumentElement();
			Element eleShipment = SCXmlUtil.getChildElement(docGetShipmentList.getDocumentElement(), AcademyConstants.ELE_SHIPMENT);
			Element eleExtn = XMLUtil.getElementByXPath(docGetShipmentList, AcademyConstants.XPATH_SHIPMENT_EXTN);
			
			// OMNI-79054 - On My Way Status Timer - Start
			String strCurbside = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_CURSIDE_PICK_OPTED);
			String strDateFormat = AcademyConstants.STR_DATE_TIME_PATTERN_NEW;
		    SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
		    Calendar cal = Calendar.getInstance();
		    String strCurrentDate = sDateFormat.format(cal.getTime());												
			log.verbose("Appointment date time : "+ strCurrentDate);
			if (!AcademyConstants.STR_YES.equals(strCurbside)) {
				eleShipment.setAttribute(AcademyConstants.ATTR_APPOINTMENT_NO, strCurrentDate);
			}
			// OMNI-79054 - On My Way Status Timer - End
			eleExtn.setAttribute(AcademyConstants.EXTN_ON_MY_WAY_OPTED, AcademyConstants.STR_YES);
			XMLUtil.copyElement(docShipment, eleShipment, eleShip);
			log.verbose("Input to AcademyUpdateOnMyWayInfoSyncService is::\n" + XMLUtil.getXMLString(docShipment));
			AcademyUtil.invokeService(env, AcademyConstants.SERVICE_UPDATE_ON_MY_WAY_INFO, docShipment);
		} catch (Exception ex) {
			ex.printStackTrace(); 
			log.verbose("Exception at AcademyProcessOnMyWayDetails.updateShipmentDetails");
		}		
		log.endTimer("AcademyProcessOnMyWayDetails::updateShipmentDetails");
	}
	
	/**
	 * This method invokes getShipmentList API with ShipmentNo, 
	 * OrderNo and ShipNode for PICKUP Orders.
	 * 
	 * @param env
	 * @param strShipmentNo
	 * @param strOrderNo
	 * @param strStoreNo
	 * @return
	 */
	private Document getShipmentList(YFSEnvironment env, String strShipmentNo, 
			String strOrderNo, String strStoreNo) {

		log.beginTimer("AcademyProcessOnMyWayDetails::getShipmentList");
		Document docOutGetShipmentList = null;
		log.verbose(" Input ShipmentNo :: "+ strShipmentNo + " :: OrderNo :: "+strOrderNo + " :: StoreNo :: "+strStoreNo);
		try {
			Document inDocGetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			inDocGetShipmentList.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
			inDocGetShipmentList.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
			inDocGetShipmentList.getDocumentElement().setAttribute(AcademyConstants.SHIP_NODE, strStoreNo);
			inDocGetShipmentList.getDocumentElement().setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, AcademyConstants.STR_PICK);
			
			Document docShipmentListTemplate = YFCDocument.getDocumentFor(AcademyConstants.GET_SHIPMENT_LIST_OMW)
					.getDocument();
			env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docShipmentListTemplate);
			docOutGetShipmentList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, inDocGetShipmentList);
			env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
						log.verbose("getShipmentList Output \n" + XMLUtil.getXMLString(docOutGetShipmentList));
		} catch (Exception x) {
			x.printStackTrace(); 
			log.verbose("Exception at AcademyProcessOnMyWayDetails.getShipmentList");
		}		
		log.endTimer("AcademyProcessOnMyWayDetails::getShipmentList");
		return docOutGetShipmentList;
	}
	
	/**
	 * This method prepares and updates the input with the success and failure reasons. 
	 * 
	 * @param inDoc
	 * @param strResponse
	 * @param strReason
	 * @return
	 */
	private Document updateResponse(Document inDoc, String strResponse, String strReason) {
		log.beginTimer("AcademyProcessOnMyWayDetails::updateResponse");		
		try {
			if(inDoc.getDocumentElement().hasChildNodes()) {
				Element eleOrder = SCXmlUtil.getChildElement(inDoc.getDocumentElement(), AcademyConstants.ELE_ORDER);
				eleOrder.setAttribute(AcademyConstants.ATTR_RESPONSE, strResponse);
				eleOrder.setAttribute(AcademyConstants.ATTR_REASON, strReason);
			}
		} catch (Exception e) {
			e.printStackTrace(); 
			log.verbose("Exception at AcademyProcessOnMyWayDetails.updateResponse");
		}	
		log.endTimer("AcademyProcessOnMyWayDetails::updateResponse");
		return inDoc;
	}

}
