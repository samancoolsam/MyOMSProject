package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.util.YFCUtils;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * Class to create Fedex Express Pickup Request for SFS Shipments.
 * 
 * @version 1.0
 * @author canand
 * 
 * @version 1.1
 */
public class AcademySFSCreateFedexExpPickupReq implements YIFCustomApi {

	// Set the logger
	private static final YFCLogCategory log = YFCLogCategory
			.instance(AcademySFSCreateFedexExpPickupReq.class);

	// Set the properties variable
	private Properties props = new Properties();

	public static final String strAvailabiltyReq = "PICKUPAVLREQ";

	public static final String strCreatePickupReq = "CREATEPICKUPREQ";

	public void setProperties(Properties arg0) throws Exception {
		this.props = props;
	}

	public void createFedexExpressPickupReq(YFSEnvironment env, Document inXML) {

		Document newInXML = null;

		try {

			if (log.isVerboseEnabled()) {
				log.verbose("AcademySFSCreateFedexExpPickupReq: START");
			}

			if (log.isVerboseEnabled()) {
				log
						.verbose("AcademySFSCreateFedexExpPickupReq: createFedexExpressPickupReq: inXML: "
								+ XMLUtil.getXMLString(inXML));
			}

			Element eleRoot = (Element) inXML.getElementsByTagName(
					AcademyConstants.ELE_CONTAINER).item(0);

			newInXML = XMLUtil.getDocumentForElement(eleRoot);
			Element newEleRoot = newInXML.getDocumentElement();

			if (log.isVerboseEnabled()) {
				log
						.verbose("AcademySFSCreateFedexExpPickupReq: createFedexExpressPickupReq: newInXML: "
								+ XMLUtil.getXMLString(newInXML));
			}

			// Getting the ShipNode
			Element eleShipNode = (Element) XPathUtil.getNode(newEleRoot,
					"Shipment/ShipNode");
			String strShipNode = eleShipNode.getAttribute("ShipNode");

			// Getting Fedex Calendar of the node for a week from the current
			// day
			Document docNodeCalendarOutXML = getStoreFedexCalendar(env,
					strShipNode);

			// Getting the first working day(mostly it will be the current day)
			String strFirstWorkingDate = getWorkingDate(docNodeCalendarOutXML,
					false);

			// Setting the working date as the dispatch date for the Pickup
			// Availabilty Webservice
			newEleRoot.setAttribute(
					AcademyConstants.FDX_PICK_REQ_DISPATCH_DATE,
					strFirstWorkingDate);
			// Invoking the Fedex Pickup Availability Request
			Document docPickupAvailRes = AcademyUtil
					.invokeService(env,
							"AcademySFSInvokeFedexExpressPickupAvlWebservice",
							newInXML);

			// Parse the PickUp Availability response to get the cutoff date and
			// time
			String strPickupAvlCutOffDateTime = parsePickupAvailResAndGetCutOffDateTime(docPickupAvailRes);

			if (YFCUtils.isVoid(strPickupAvlCutOffDateTime)) {
				createAlert(env, newInXML, docPickupAvailRes, false,
						strAvailabiltyReq);
			} else {

				// Determine whether packing is done before or after Fedex
				// Cutoff
				// date and time
				boolean result = checkPackingStatusWithFedexCutOffTime(
						strPickupAvlCutOffDateTime, newEleRoot
								.getAttribute("Createts"));

				if (result) {

					// Setting the ReadyTimeStamp as the PickupAvlCutOffDateTime
					// for
					// the Create Pickup Webservice
					newEleRoot
							.setAttribute(
									AcademyConstants.FDX_CREATE_PICK_REQ_READYTIMESTAMP,
									strPickupAvlCutOffDateTime);
					// Create Fedex Pickup Request with the ReadyTimeStamp as
					// the
					// CutOff Time
					Document docCreatePickupRes = AcademyUtil
							.invokeService(
									env,
									"AcademySFSInvokeFedexExpressCreatePickupReqWebservice",
									newInXML);
					parseCreatePickupResAndRaiseAlert(env, newInXML,
							docCreatePickupRes, strCreatePickupReq);

				} else {
					// Getting the next working day
					String strNextWorkingDate = getWorkingDate(
							docNodeCalendarOutXML, true);
					// Setting the working date as the dispatch date for the
					// Pickup
					// Availabilty Webservice
					newEleRoot.setAttribute(
							AcademyConstants.FDX_PICK_REQ_DISPATCH_DATE,
							strNextWorkingDate);
					// Invoking the Fedex Pickup Availability Request
					Document docPickupAvailResNextWrkDay = AcademyUtil
							.invokeService(
									env,
									"AcademySFSInvokeFedexExpressPickupAvlWebservice",
									newInXML);
					// Parse the PickUp Availability response to get the cutoff
					// date
					// and time
					String strPickupAvlCutOffDateTimeNextWrkDay = parsePickupAvailResAndGetCutOffDateTime(docPickupAvailResNextWrkDay);

					if (YFCUtils.isVoid(strPickupAvlCutOffDateTimeNextWrkDay)) {
						createAlert(env, newInXML, docPickupAvailResNextWrkDay,
								false, strAvailabiltyReq);
					} else {

						// Setting the ReadyTimeStamp as the
						// PickupAvlCutOffDateTime
						// for
						// the Create Pickup Webservice
						newEleRoot
								.setAttribute(
										AcademyConstants.FDX_CREATE_PICK_REQ_READYTIMESTAMP,
										strPickupAvlCutOffDateTimeNextWrkDay);
						// Create Fedex Pickup Request with the ReadyTimeStamp
						// as
						// the
						// CutOff Time
						Document docCreatePickupRes = AcademyUtil
								.invokeService(
										env,
										"AcademySFSInvokeFedexExpressCreatePickupReqWebservice",
										newInXML);
						parseCreatePickupResAndRaiseAlert(env, newInXML,
								docCreatePickupRes, strCreatePickupReq);
					}
				}
			}

			if (log.isVerboseEnabled()) {
				log.verbose("AcademySFSCreateFedexExpPickupReq: END");
			}

		} catch (Exception e) {
			e.printStackTrace();
			try {
				raiseAlertOnGeneralFailure(env, newInXML);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private void raiseAlertOnGeneralFailure(YFSEnvironment env, Document inXML)
			throws Exception {

		if (log.isVerboseEnabled()) {
			log.verbose("raiseAlertOnGeneralFailure: START");
		}

		Element eleRoot = inXML.getDocumentElement();

		String sContainerNo = eleRoot.getAttribute("ContainerNo");

		Element eleShipment = (Element) eleRoot
				.getElementsByTagName("Shipment").item(0);

		String sShipmentNo = eleShipment.getAttribute("ShipmentNo");
		String sShipNode = eleShipment.getAttribute("ShipNode");

		String sOrderNo = eleShipment.getAttribute("OrderNo");
		String sOrderHeaderKey = eleShipment.getAttribute("OrderHeaderKey");

		Document inputCreateException = XMLUtil
				.createDocument(AcademyConstants.ELE_INBOX);
		Element rootEle = inputCreateException.getDocumentElement();
		rootEle.setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG,
				AcademyConstants.STR_YES);

		rootEle.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE,
				AcademyConstants.FDX_CREATE_PICK_REQ_FAILURE_ALERT_TYPE);
		rootEle.setAttribute("Description",
				"Fedex Express Pickup Request failed for Container No:"
						+ sContainerNo);

		rootEle.setAttribute(AcademyConstants.ATTR_ORDER_NO, sOrderNo);
		rootEle.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
				sOrderHeaderKey);
		rootEle.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, sShipmentNo);
		rootEle.setAttribute(AcademyConstants.ATTR_SHIP_NODE_KEY, sShipNode);

		if (log.isVerboseEnabled()) {
			log.verbose("raiseAlertOnGeneralFailure : Input Doc"
					+ XMLUtil.getXMLString(inputCreateException));
		}

		AcademyUtil.invokeAPI(env, AcademyConstants.API_CREATE_EXCEPTION,
				inputCreateException);

		if (log.isVerboseEnabled()) {
			log.verbose("raiseAlertOnGeneralFailure: END");
		}

	}

	private void parseCreatePickupResAndRaiseAlert(YFSEnvironment env,
			Document inXML, Document docCreatePickupRes, String req)
			throws Exception {

		if (log.isVerboseEnabled()) {
			log.verbose("parseCreatePickupResAndRaiseAlert: START");
		}

		String strResponse = docCreatePickupRes.getElementsByTagName(
				"v5:HighestSeverity").item(0).getTextContent();

		if (log.isVerboseEnabled()) {
			log.verbose("strResponse:" + strResponse);
		}

		if ("SUCCESS".equalsIgnoreCase(strResponse)) {
			createAlert(env, inXML, docCreatePickupRes, true, req);
		} else {
			createAlert(env, inXML, docCreatePickupRes, false, req);
		}

		if (log.isVerboseEnabled()) {
			log.verbose("parseCreatePickupResAndRaiseAlert: END");
		}
	}

	public void createAlert(YFSEnvironment env, Document inDoc,
			Document docCreatePickupRes, boolean isSuccess, String req)
			throws Exception {

		if (log.isVerboseEnabled()) {
			log.verbose("createAlert: START");
		}

		Element eleRoot = inDoc.getDocumentElement();

		String sContainerNo = eleRoot.getAttribute("ContainerNo");

		Element eleShipment = (Element) eleRoot
				.getElementsByTagName("Shipment").item(0);

		String sShipmentNo = eleShipment.getAttribute("ShipmentNo");
		String sShipNode = eleShipment.getAttribute("ShipNode");

		String sOrderNo = eleShipment.getAttribute("OrderNo");
		String sOrderHeaderKey = eleShipment.getAttribute("OrderHeaderKey");

		String sErrorDesc = docCreatePickupRes.getElementsByTagName(
				"v5:Message").item(0).getTextContent();
		String sErrorCode = docCreatePickupRes.getElementsByTagName("v5:Code")
				.item(0).getTextContent();

		Document inputCreateException = XMLUtil
				.createDocument(AcademyConstants.ELE_INBOX);
		Element rootEle = inputCreateException.getDocumentElement();
		rootEle.setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG,
				AcademyConstants.STR_YES);
		if (isSuccess) {
			rootEle.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE,
					AcademyConstants.FDX_CREATE_PICK_REQ_SUCCESS_ALERT_TYPE);
			rootEle.setAttribute("Description",
					"Fedex Express Pickup Request successfully created for Container No:"
							+ sContainerNo);
		} else {
			rootEle.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE,
					AcademyConstants.FDX_CREATE_PICK_REQ_FAILURE_ALERT_TYPE);
			rootEle.setAttribute("Description",
					"Fedex Express Pickup Request failed for Container No:"
							+ sContainerNo);
		}

		rootEle.setAttribute(AcademyConstants.ATTR_ORDER_NO, sOrderNo);
		rootEle.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
				sOrderHeaderKey);
		rootEle.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, sShipmentNo);
		rootEle.setAttribute(AcademyConstants.ATTR_SHIP_NODE_KEY, sShipNode);

		Element eleInboxRefLst = inputCreateException
				.createElement(AcademyConstants.ELE_INBOX_REF_LIST);

		Element eleInboxRef = inputCreateException
				.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME,
				"Fedex Response Code");
		eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE,
				AcademyConstants.STR_EXCPTN_REF_VALUE);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, sErrorCode);
		eleInboxRefLst.appendChild(eleInboxRef);

		eleInboxRef = inputCreateException
				.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME,
				"Fedex Response Message");
		eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE,
				AcademyConstants.STR_EXCPTN_REF_VALUE);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, sErrorDesc);
		eleInboxRefLst.appendChild(eleInboxRef);

		if (isSuccess) {
			String sPickConfirmNo = docCreatePickupRes.getElementsByTagName(
					"v5:PickupConfirmationNumber").item(0).getTextContent();
			eleInboxRef = inputCreateException
					.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME,
					"Pickup Confirmation Number");
			eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE,
					AcademyConstants.STR_EXCPTN_REF_VALUE);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE,
					sPickConfirmNo);
			eleInboxRefLst.appendChild(eleInboxRef);
		}

		rootEle.appendChild(eleInboxRefLst);

		if (log.isVerboseEnabled()) {
			log.verbose("createAlert : Input Doc"
					+ XMLUtil.getXMLString(inputCreateException));
		}

		AcademyUtil.invokeAPI(env, AcademyConstants.API_CREATE_EXCEPTION,
				inputCreateException);

		if (log.isVerboseEnabled()) {
			log.verbose("createAlert: END");
		}
	}

	private boolean checkPackingStatusWithFedexCutOffTime(
			String strPickupAvlCutOffDateTime, String strContainerCreateDateTime) {
		if (log.isVerboseEnabled()) {
			log.verbose("checkPackingStatusWithFedexCutOffTime: START");
		}

		YFCDate dtPickupAvlCutOffDateTime = YFCDate
				.getYFCDate(strPickupAvlCutOffDateTime);
		if (log.isVerboseEnabled()) {
			log
					.verbose("checkPackingStatusWithFedexCutOffTime: dtPickupAvlCutOffDateTime"
							+ dtPickupAvlCutOffDateTime);
		}
		YFCDate dtContainerCreateDateTime = YFCDate
				.getYFCDate(strContainerCreateDateTime);
		if (log.isVerboseEnabled()) {
			log
					.verbose("checkPackingStatusWithFedexCutOffTime: dtContainerCreateDateTime"
							+ dtContainerCreateDateTime);
		}

		boolean result = dtContainerCreateDateTime
				.before(dtPickupAvlCutOffDateTime);

		if (log.isVerboseEnabled()) {
			log.verbose("checkPackingStatusWithFedexCutOffTime: result"
					+ result);
			log.verbose("checkPackingStatusWithFedexCutOffTime: END");
		}

		return result;
	}

	private String parsePickupAvailResAndGetCutOffDateTime(
			Document docPickupAvailRes) throws Exception {
		if (log.isVerboseEnabled()) {
			log.verbose("parsePickupAvailResAndGetCutOffDateTime: START");
		}
		String strPickupAvlCutOffDateTime = null;
		String strResponse = docPickupAvailRes.getElementsByTagName(
				"v5:HighestSeverity").item(0).getTextContent();
		if ("SUCCESS".equalsIgnoreCase(strResponse)) {
			Element eleOptions = (Element) docPickupAvailRes
					.getElementsByTagName("v5:Options").item(0);
			if (!YFCObject.isVoid(eleOptions)) {
				strPickupAvlCutOffDateTime = docPickupAvailRes
						.getElementsByTagName("v5:PickupDate").item(0)
						.getTextContent()
						+ "T"
						+ docPickupAvailRes.getElementsByTagName(
								"v5:CutOffTime").item(0).getTextContent();
			}
		}
		if (log.isVerboseEnabled()) {
			log.verbose("strPickupAvlCutOffDateTime: "
					+ strPickupAvlCutOffDateTime);
		}
		if (log.isVerboseEnabled()) {
			log.verbose("parsePickupAvailResAndGetCutOffDateTime: END");
		}
		return strPickupAvlCutOffDateTime;
	}

	private String getWorkingDate(Document docNodeCalendarOutXML,
			boolean excludeCurrentDay) throws Exception {

		if (log.isVerboseEnabled()) {
			log.verbose("getWorkingDate: START");
		}

		NodeList nl = XPathUtil.getNodeList(docNodeCalendarOutXML
				.getDocumentElement(),
				"OrgWorkingDay/WorkingCalendar[@CalendarId='"
						+ AcademyConstants.FEDX_CALENDAR_ID
						+ "' and @WorkingDay='Y']");
		int i = 0;
		if (excludeCurrentDay) {
			i = 1;
		}
		Element eleOrgWorkingDay = (Element) (nl.item(i).getParentNode());
		String strWorkingDate = eleOrgWorkingDay.getAttribute("Date");

		if (log.isVerboseEnabled()) {
			log.verbose("strWorkingDate: " + strWorkingDate);
			log.verbose("getWorkingDate: END");
		}
		return strWorkingDate;
	}

	private Document getStoreFedexCalendar(YFSEnvironment env,
			String strShipNode) throws Exception {

		if (log.isVerboseEnabled()) {
			log.verbose("getStoreFedexCalendar: START");
		}

		Document docNodeCalendarInXML = XMLUtil.createDocument("StoreCalendar");
		Element eleNodeCalendarInXML = docNodeCalendarInXML
				.getDocumentElement();
		eleNodeCalendarInXML.setAttribute("OrganizationCode", strShipNode);
		eleNodeCalendarInXML.setAttribute("FromDayOffset",
				AcademyConstants.FDX_PICK_REQ_FROM_DAY_OFFSET);
		eleNodeCalendarInXML.setAttribute("ToDayOffset",
				AcademyConstants.FDX_PICK_REQ_TO_DAY_OFFSET);

		if (log.isVerboseEnabled()) {
			log
					.verbose("AcademySFSCreateFedexExpPickupReq: getStoreFedexCalendarWorkingDays: inXML: "
							+ XMLUtil.getXMLString(docNodeCalendarInXML));
		}
		Document docNodeCalendarOutXML = AcademyUtil.invokeService(env,
				"AcademySFSGetCalendarWorkingDays", docNodeCalendarInXML);
		if (log.isVerboseEnabled()) {
			log
					.verbose("AcademySFSCreateFedexExpPickupReq: getStoreFedexCalendarWorkingDays: outXML: "
							+ XMLUtil.getXMLString(docNodeCalendarOutXML));
		}
		if (log.isVerboseEnabled()) {
			log.verbose("getStoreFedexCalendar: END");
		}
		return docNodeCalendarOutXML;
	}
}
