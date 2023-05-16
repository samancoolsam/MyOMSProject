package com.academy.ecommerce.sterling.shipment;

/*###############################################################################################################################################
 *
 * Project Name                : POD September Release
 * Module                      : OMS
 * Author                      : CTS
 * Date                        : 22-AUG-2022
 * Description                 : This file implements the logic to 
 * 								1. Get the List of Shipments in RFCP or PaperWorkInitiated status for last X days (Configurable) 
 *                              2. Get the List of Shipments that are scanned in the current batch and not cancelled as part of Audit Staging
 *                              3. Get the List of Missed Shipments (Shipments in RFCP but not scanned in current batch)
 *                              4. Get the List of Shipments that are scanned in Last X COMPLETED batches
 *                              5. Prepare MSL Input to Insert records into new custom table ACAD_MISSED_SCAN_DETAILS 
 *                              6. Invoke service to insert Missed Shipment records into new custom table ACAD_MISSED_SCAN_DETAILS                                              
 *
 * Change Revision
 * ------------------------------------------------------------------------------------------------------------------------------------------------
 * Date            Author                  Version#       Remarks/Description                     
 * ------------------------------------------------------------------------------------------------------------------------------------------------
 * 22-AUG-2022     CTS                      1.0            Initial version (OMNI-82356, OMNI-82359, OMNI-81605)

 * ###############################################################################################################################################*/

import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.ibm.icu.text.SimpleDateFormat;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyManageMissedShipments {
	private Properties props;

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyManageMissedShipments.class);

	/**
	 * This method is invoked as part of Audit Staging Process when store user
	 * clicks on "YES" from Finish Summary Screen in WebSOM when the Missed Shipment
	 * Feature is ENABLED
	 * Input doc:
	 * <ACADScanBatchHeader StoreNo="033" AcadScanBatchHeaderKey=
	 * "202207130211303211378234"/>
	 */

	public Document manageMissedShipments(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer(this.getClass() + ".manageMissedShipments");
		log.verbose("AcademyManageMissedShipment.manageMissedShipments() :: Input Doc " + XMLUtil.getXMLString(inDoc));

		String strAcadScanBatchHdrKey = XMLUtil.getAttributeFromXPath(inDoc,
				AcademyConstants.XPATH_ACAD_SCAN_BATCH_HEADER_KEY);
		String strStoreNo = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_STORE_NO);

		// Get the List of Shipments in RFCP or PaperWorkInitiated status for last X
		// days
		Set<String> setRFCPShipments = getRFCPShipments(env, strStoreNo);
		if (setRFCPShipments.size() > 0) {

			// Get the List of RFCP Shipments that are scanned in the current INPROGRESS
			// batch but not cancelled
			Set<String> setCurrentBatchShipments = getCurrentBatchShipments(env, inDoc);
			Set<String> setMissedShipments = new HashSet<String>(setRFCPShipments);

			// Get the List of Missed Shipments (Shipments in RFCP but not scanned in
			// current batch)
			setMissedShipments.removeAll(setCurrentBatchShipments);

			if (setMissedShipments.size() > 0) {

				log.verbose("AcademyManageMissedShipments.manageMissedShipments() :: Missed Shipment List :: "
						+ setMissedShipments + " Length: " + setMissedShipments.size());

				// Get the List of Shipments that are scanned in Last X COMPLETED batches
				Document docCompletedBatchShipments = getCompletedBatchShipments(env, strStoreNo);

				// Prepare Input to Insert records into new custom table
				// ACAD_MISSED_SCAN_DETAILS
				prepareMissedShipmentDetails(env, docCompletedBatchShipments, setMissedShipments, strStoreNo,
						strAcadScanBatchHdrKey);
			}
		}

		log.endTimer(this.getClass() + ".manageMissedShipments");
		return inDoc;
	}

	/**
	 * This method is used to invoke getShipmentList API (ComplexQuery) to get the
	 * List of Shipments in RFCP or PaperWorkInitiated status for last X days
	 * (Configurable) Adds ShipmentNo to a Set Returns Set
	 */

	private Set<String> getRFCPShipments(YFSEnvironment env, String strStoreNo) throws Exception {
		log.beginTimer(this.getClass() + ".getRFCPShipments");

		// Preparing InDoc for Service

		String strNoOfDays = props.getProperty(AcademyConstants.STR_NO_OF_DAYS);
		String strStatus = props.getProperty(AcademyConstants.STR_VALIDATE_STATUS);
		String[] lStatus = strStatus.split(AcademyConstants.STR_COMMA);

		// Calculating date from last 45 days
		int days = Integer.parseInt(strNoOfDays);
		Date pastday = DateUtils.addDays(new Date(), -days);
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_SIMPLE_DATE_PATTERN);
		String strPastDay = sdf.format(pastday);

		StringBuilder statusDate = new StringBuilder();
		statusDate.append(strPastDay);
		statusDate.append(AcademyConstants.STR_12AM_TIME);

		// Preparing InDoc for getShipmentList (ComplexQuery)
		YFCDocument getShipmentListInputDoc = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
		YFCElement eleShipment = getShipmentListInputDoc.getDocumentElement();
		YFCElement eleComplexQuery = eleShipment.createChild(AcademyConstants.COMPLEX_QRY_ELEMENT);

		eleComplexQuery.setAttribute(AcademyConstants.COMPLEX_OPERATOR_ATTR, AcademyConstants.COMPLEX_OPERATOR_AND_VAL);
		YFCElement eleAnd = eleComplexQuery.createChild(AcademyConstants.COMPLEX_AND_ELEMENT);
		YFCElement eleOr = eleComplexQuery.createChild(AcademyConstants.COMPLEX_OR_ELEMENT);

		for (String str : lStatus) {
			YFCElement eleExp0 = eleOr.createChild(AcademyConstants.COMPLEX_EXP_ELEMENT);
			eleExp0.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_STATUS);
			eleExp0.setAttribute(AcademyConstants.ATTR_VALUE, str);
			eleExp0.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
			eleOr.appendChild(eleExp0);
			eleAnd.appendChild(eleOr);
		}

		YFCElement eleExp1 = eleAnd.createChild(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp1.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_STATUS_DATE);
		eleExp1.setAttribute(AcademyConstants.ATTR_VALUE, statusDate.toString());
		eleExp1.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.GT_QRY_TYPE);
		eleAnd.appendChild(eleExp1);

		YFCElement eleExp2 = eleAnd.createChild(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp2.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ELE_SHIP_NODE);
		eleExp2.setAttribute(AcademyConstants.ATTR_VALUE, strStoreNo);
		eleExp2.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
		eleAnd.appendChild(eleExp2);

		YFCElement eleExp3 = eleAnd.createChild(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp3.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.STR_DOCUMENT_TYPE);
		eleExp3.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STR_DOCUMENT_TYPE_VALUE);
		eleExp3.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
		eleAnd.appendChild(eleExp3);

		YFCElement eleExp4 = eleAnd.createChild(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp4.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_DEL_METHOD);
		eleExp4.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STR_DELIVERY_METHOD_VALUE);
		eleExp4.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
		eleAnd.appendChild(eleExp4);

		Document docRFCPShipments = getRFCPShipmentListUsingComplexQuery(env, getShipmentListInputDoc.getDocument());

		Set<String> hsRFCPShipments = new HashSet<>();
		if (!YFCCommon.isVoid(docRFCPShipments)) {
			NodeList nlRFCPShipments = XPathUtil.getNodeList(docRFCPShipments, AcademyConstants.XPATH_SHIPMENT);
			for (int i = 0; i < nlRFCPShipments.getLength(); i++) {
				Element eleRFCPShipment = (Element) nlRFCPShipments.item(i);
				hsRFCPShipments.add(eleRFCPShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
			}
		}
		log.verbose("AcademyManageMissedShipments.getRFCPShipments() :: RFCP Shipment list :: " + hsRFCPShipments
				+ " Length: " + hsRFCPShipments.size());
		log.endTimer(this.getClass() + ".getRFCPShipments");
		return hsRFCPShipments;
	}

	/**
	 * This method is used to invoke AcademyGetScanBatchHeaderListService Service to
	 * get the List of Shipments that are scanned in the current batch and not
	 * cancelled as part of Audit Staging Adds ShipmentNo to a Set Returns Set
	 */

	private Set<String> getCurrentBatchShipments(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer(this.getClass() + ".getCurrentBatchShipments");

		Document outDocCurrentBatchShipments = getAcadScanBatchHeaderAndDetails(env, inDoc);

		Set<String> hsCurrentBatchShipments = new HashSet<>();
		if (!YFCCommon.isVoid(outDocCurrentBatchShipments)) {
			NodeList nlCurrentBatchShipmentList = XPathUtil.getNodeList(outDocCurrentBatchShipments,
					AcademyConstants.XPATH_SCANNED_SHIPMENT_RFCP);
			for (int i = 0; i < nlCurrentBatchShipmentList.getLength(); i++) {
				Element eleShipment = (Element) nlCurrentBatchShipmentList.item(i);
				String strShipmentNo = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
				hsCurrentBatchShipments.add(strShipmentNo);
			}
			log.verbose("AcademyManageMissedShipments.getCurrentBatchShipments() :: Shipments in Current Batch :: "
					+ hsCurrentBatchShipments + " Length: " + hsCurrentBatchShipments.size());
		}
		log.endTimer(this.getClass() + ".getCurrentBatchShipments");
		return hsCurrentBatchShipments;

	}

	/**
	 * This method is used to invoke AcademyGetScanBatchHeaderListService Service to
	 * get the List of Shipments that are scanned in Last X COMPLETED batches
	 * Returns Document
	 */

	private Document getCompletedBatchShipments(YFSEnvironment env, String strStoreNo) throws Exception {
		log.beginTimer(this.getClass() + ".getCompletedBatchShipments");
		String strNoOfBatchesConfigured = props.getProperty(AcademyConstants.STR_NO_OF_BATCHES_CONFIGURED);

		/*
		 * SAMPLE - <ACADScanBatchHeader IgnoreOrdering='N' MaximumRecords='3'
		 * StoreNo='033' BatchScanStatus='COMPLETED'> <OrderBy> <Attribute
		 * Name='AcadScanBatchHeaderKey' Desc='Y'/> </OrderBy> </ACADScanBatchHeader>;
		 */

		Document inDoc = XmlUtils.createDocument(AcademyConstants.STR_SCAN_BATCH_HDR);
		Element inEle = inDoc.getDocumentElement();
		inEle.setAttribute(AcademyConstants.ATTR_IGNORE_ORDERING, AcademyConstants.STR_NO);
		inEle.setAttribute(AcademyConstants.ATTR_MAX_RECORD, strNoOfBatchesConfigured);
		inEle.setAttribute(AcademyConstants.ATTR_STORE_NO, strStoreNo);
		inEle.setAttribute(AcademyConstants.ATTR_BATCH_SCAN_STATUS, AcademyConstants.VALUE_COMPLETED);
		Element eleOrderBy = inDoc.createElement(AcademyConstants.ELE_ORDERBY);
		Element eleAttribute = inDoc.createElement(AcademyConstants.ELE_ATTRIBUTE);
		eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ACAD_SCAN_BATCH_HDR_KEY);
		eleAttribute.setAttribute(AcademyConstants.ATTR_DESC_SHORT, AcademyConstants.ATTR_Y);
		eleOrderBy.appendChild(eleAttribute);
		inEle.appendChild(eleOrderBy);

		Document outDocCompletedBatches = getAcadScanBatchHeaderAndDetails(env, inDoc);

		log.endTimer(this.getClass() + ".getCompletedBatchShipments");
		return outDocCompletedBatches;
	}

	/**
	 * This method is used to prepare Input to Insert records into new custom table
	 * ACAD_MISSED_SCAN_DETAILS
	 */

	private void prepareMissedShipmentDetails(YFSEnvironment env, Document docCompletedBatchShipments,
			Set<String> setMissedShipments, String strStoreNo, String strAcadScanBatchHdrKey) throws Exception {
		log.beginTimer(this.getClass() + ".prepareMissedShipmentDetails");
		Document inDoc = null;
		NodeList nlShipmentList = XPathUtil.getNodeList(docCompletedBatchShipments,
				AcademyConstants.XPATH_BATCH_DETAILS);
		for (String strShipmentNo : setMissedShipments) {
			inDoc = XmlUtils.createDocument(AcademyConstants.ELE_ACAD_MISSED_SCAN_DETAILS);
			Element eleInDoc = inDoc.getDocumentElement();
			eleInDoc.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
			eleInDoc.setAttribute(AcademyConstants.ACAD_SCAN_BATCH_HDR_KEY, strAcadScanBatchHdrKey);
			eleInDoc.setAttribute(AcademyConstants.ATTR_STORE_NO, strStoreNo);
			for (int i = 0; i < nlShipmentList.getLength(); i++) {
				Element eleBatchDetail = (Element) nlShipmentList.item(i);
				String strShipNo = eleBatchDetail.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
				if (strShipNo.equals(strShipmentNo)) {
					String strLastScanDate = eleBatchDetail.getAttribute(AcademyConstants.ATTR_CREATETS);
					eleInDoc.setAttribute(AcademyConstants.STR_LAST_SCAN_DATE, strLastScanDate);
					break;
				}
			}
			createMissedShipmentDetailRecord(env, inDoc);
		}
		log.endTimer(this.getClass() + ".prepareMissedShipmentDetails");
	}

	/**
	 * This method is used to Invoke service "AcademyCreateMissedShipmentsService"
	 * to insert Missed Shipment records into new custom table
	 * ACAD_MISSED_SCAN_DETAILS Sample Input:
	 * <AcadMissedScanDetails AcadScanBatchHeaderKey="20220818065701284593620"
	 * ShipmentNo="100316139" StoreNo="033"/> (or)
	 * <AcadMissedScanDetails AcadScanBatchHeaderKey="20220818065701284593620"
	 * LastScanDate="2022-08-18T06:56:34-05:00" ShipmentNo="100316057" StoreNo=
	 * "033"/>
	 */

	private void createMissedShipmentDetailRecord(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("AcademyManageMissedShipments.createMissedShipmentDetailRecord() :: Input \n "
				+ XMLUtil.getXMLString(inDoc));
		Document outDoc = AcademyUtil.invokeService(env, AcademyConstants.SERV_ACAD_CREATE_MISSED_SHIPMENTS, inDoc);
		log.verbose("AcademyManageMissedShipments.createMissedShipmentDetailRecord() :: Output \n "
				+ XMLUtil.getXMLString(outDoc));
	}

	/**
	 * This method is used to Invoke service "AcademyGetScanBatchHeaderListService"
	 * Sample Input: <ACADScanBatchHeader StoreNo="033" AcadScanBatchHeaderKey=
	 * "202207130211303211378234"/> (or)
	 * <ACADScanBatchHeader IgnoreOrdering='N' MaximumRecords='3' StoreNo='033'
	 * BatchScanStatus='COMPLETED'> <OrderBy>
	 * <Attribute Name='AcadScanBatchHeaderKey' Desc='Y'/> </OrderBy>
	 * </ACADScanBatchHeader>
	 * 
	 * @return Document
	 */

	private Document getAcadScanBatchHeaderAndDetails(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("AcademyManageMissedShipments.getAcadScanBatchHeaderAndDetails() :: Input Doc :: "
				+ XMLUtil.getXMLString(inDoc));
		Document outDoc = AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_GET_SCAN_BATCH_HDR_LIST_SERVICE,
				inDoc);
		log.verbose("AcademyManageMissedShipments.getAcadScanBatchHeaderAndDetails() :: Output Doc :: "
				+ XMLUtil.getXMLString(outDoc));
		return outDoc;
	}

	/**
	 * This method is used to Invoke API "getShipmentList" Sample Input: <Shipment>
	 * <ComplexQuery Operator="AND"> <And> <Or>
	 * <Exp Name="Status" Value="1100.70.06.30.5" QryType="EQ"/>
	 * <Exp Name="Status" Value="1100.70.06.30.7" QryType="EQ"/> </Or>
	 * <Exp Name="StatusDate" Value="2022-07-10 00:00:00-00:00" QryType="GT"/>
	 * <Exp Name="ShipNode" Value="033" QryType="EQ"/>
	 * <Exp Name="DocumentType" Value="0001" QryType="EQ"/>
	 * <Exp Name="DeliveryMethod" Value="PICK" QryType="EQ"/> </And> </ComplexQuery>
	 * </Shipment>
	 * 
	 * @return Shipment List output <Shipments TotalNumberOfRecords="3">
	 *         <Shipment ShipmentNo="100316126"/> <Shipment ShipmentNo="100316130"/>
	 *         <Shipment ShipmentNo="100316131"/> </Shipments>
	 */

	private Document getRFCPShipmentListUsingComplexQuery(YFSEnvironment env, Document getShipmentListInputDoc)
			throws Exception {
		log.verbose(
				"AcademyManageMissedShipments.getRFCPShipmentListUsingComplexQuery() :: getShipmentList API Input :: \n "
						+ XMLUtil.getXMLString(getShipmentListInputDoc));

		String strGetShipmentListTemplate = AcademyConstants.GET_SHIPMENT_LIST_TEMPLATE;

		Document docGetShipmentListTemplate = YFCDocument.getDocumentFor(strGetShipmentListTemplate).getDocument();
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docGetShipmentListTemplate);
		Document docRFCPShipments = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
				getShipmentListInputDoc);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);

		log.verbose(
				"AcademyManageMissedShipments.getRFCPShipmentListUsingComplexQuery() :: getShipmentList API Output :: \n"
						+ XMLUtil.getXMLString(docRFCPShipments));
		return docRFCPShipments;
	}

	/**
	 * This method is used to set Properties
	 */

	public void setProperties(Properties props) {
		this.props = props;
	}
}
