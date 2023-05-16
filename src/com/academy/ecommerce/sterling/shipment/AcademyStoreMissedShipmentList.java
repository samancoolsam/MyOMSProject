package com.academy.ecommerce.sterling.shipment;

/**
##################################################################################

* Project Name                : POD September Release
* Module                      : OMS
* Author                      : Cognizant Technology Solutions
* Date                        : 02-SEP-2022
* Description                 : This file implements the logic to 
* 							   1. Get the Last Completed Batch Header Key from ACAD_SCAN_BATCH_HEADER table
* 							   2. List of Missed Shipments from new Reference Table - ACAD_MISSED_SCAN_DETAILS
* 							   3. Get the list of Shipments which are still in  RFCP or PaperWorkInitiated status 
*                              4. Prepare Output for valid Shipments, which are still in  RFCP or PaperWorkInitiated 
*                              status and are to be sorted based on LastScanDate
Change Revision
---------------------------------------------------------------------------------
Date            Author                  Version#       Remarks/Description                     
---------------------------------------------------------------------------------
02-SEP-2022     CTS                     1.0            Initial version
												 (OMNI-81386, OMNI-81389, OMNI-82409)
##################################################################################*/

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.comergent.api.xml.XMLUtils;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyStoreMissedShipmentList implements YIFCustomApi {
	private Properties props;

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyStoreMissedShipmentList.class);
	private static DateTimeFormatter dtfMMSddSYYYY = DateTimeFormatter.ofPattern(AcademyConstants.STR_MISSED_SHIP_DATE_FORMAT);
	private static DateTimeFormatter dtfYYYYMMDD = DateTimeFormatter.ofPattern(AcademyConstants.DATE_YYYYMMDD_FORMAT);
	
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	/**
	 * Sample I/p: <Shipment ApplyQueryTimeout="Y" DeliveryMethod="PICK"
	 * DisplayLocalizedFieldInLocale="en_US_CST" EnterpriseCode="Academy_Direct"
	 * DocumentType="0001" IgnoreOrdering="Y" MaximumRecords="5000"
	 * MissedScanFeatureEnabled="Y" QueryTimeout="60" ReceivingNode="" SCAC=""
	 * ShipNode="195" ShowRelatedOrders=""> <!-- OMNI-81388
	 * added @MissedScanFeatureEnabled --> <!--OMNI-71303 Begin--> <!--OMNI-71303
	 * End-->
	 * <Paginate IgnoreOrdering="Y" PageNumber="1" PageSize="200" PaginationStrategy
	 * ="NEXTPAGE"> <PreviousPage/> </Paginate> <ComplexQuery Operator="AND"> <And>
	 * <Or> <Exp Name="Status" QryType="EQ" Value="1100.70.06.30.5"/>
	 * <Exp Name="Status" QryType="EQ" Value="1100.70.06.30.7"/> </Or> </And>
	 * </ComplexQuery> </Shipment>
	 *
	 * 
	 * Sample Output:
	 * <Shipments BatchCompletedOn="08/19/2022" TotalNumberOfRecords="5">
	 * <Shipment LastScanDate="08/15/2022" OrderNo="Y100219509" ShipmentNo=
	 * "100316136" SortDate="20220815"/>
	 * <Shipment OrderNo="Y100219536" ShipmentNo="100316138" SortDate="00000000"/>
	 * <Shipment LastScanDate="08/01/2022" OrderNo="180720221" ShipmentNo=
	 * "100319211" SortDate="20220801"/>
	 * <Shipment OrderNo="180720222" ShipmentNo="100319351" SortDate="00000000"/>
	 * <Shipment LastScanDate="08/18/2022" OrderNo="5431314004" ShipmentNo=
	 * "100319702" SortDate="20220818"/>
	 * <PageData PageNumber="1" PageSize="1" IsFirstPage="Y" IsLastPage="N"
	 * IsValidPage="Y" PageNumber="1" PageSize="1"> </Shipments>
	 * 
	 */

	public Document fetchMissedShipmentList(YFSEnvironment env, Document inDoc) {
		log.beginTimer("fetchMissedShipmentList");
		log.verbose(
				"AcademyStoreMissedShipmentList.fetchMissedShipmentList Input inDoc:: " + XMLUtil.getXMLString(inDoc));

		Set<String> hsUnqueMissedShipmentNo = new HashSet<String>();
		Document docMissedShipmentListSrvOutput = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENTS)
				.getDocument();
	
		try {
			String strStoreNo = inDoc.getDocumentElement().getAttribute(AcademyConstants.SHIP_NODE);
			Document docMissedShipmentBatchHDR = getLastCompletedBatchHrdDtls(env, strStoreNo);
			String strLastCompletedBatchHdrKey = XMLUtil.getAttributeFromXPath(docMissedShipmentBatchHDR,
					AcademyConstants.XPATH_ACAD_FETCH_SCAN_BATCH_HDR_COM_KEY);
			String strEndTime = XMLUtil.getAttributeFromXPath(docMissedShipmentBatchHDR,
					AcademyConstants.XPATH_ACAD_FETCH_SCAN_BATCH_END_DATE);
			log.debug("strLastCompletedBatchHdrKey:: " + strLastCompletedBatchHdrKey);
			Document docGetMissedScanShipmentList = null;
			if (!YFCObject.isVoid(strLastCompletedBatchHdrKey)) {
				log.verbose("Inside LastCompleted/Missed Scan Populated  Batch AcadScanBatchHeaderKey:: "
						+ strLastCompletedBatchHdrKey);
				log.verbose("Inside LastCompleted/Missed Scan Populated  Batch Batch EndTime:: " + strEndTime);
				docGetMissedScanShipmentList = getMissedScanShipmentList(env, strLastCompletedBatchHdrKey, strStoreNo);
				log.verbose("docGetMissedScanShipmentList::" + XMLUtil.getXMLString(docGetMissedScanShipmentList));
				populateMissedDataCollection(docGetMissedScanShipmentList, hsUnqueMissedShipmentNo);
				docMissedShipmentListSrvOutput = hsUnqueMissedShipmentNo.isEmpty() ? docMissedShipmentListSrvOutput
						: getFilteredShipmentList(env, hsUnqueMissedShipmentNo, inDoc);
				log.verbose("docMissedShipmentListSrvOutput::" + XMLUtil.getXMLString(docMissedShipmentListSrvOutput));
			}
			log.debug("hsUnqueMissedShipmentNo.size()::" + hsUnqueMissedShipmentNo.size());
			log.verbose("hsUnqueMissedShipmentNo::" + hsUnqueMissedShipmentNo);
			/**
			 * No Need for a null check on docGetMissedScanShipmentList,If no record in
			 * ACAD_MISSED_SCAN_DETAILS, hsUnqueMissedShipmentNo will be empty and
			 * docRelevantShipmentList will be null, only checking
			 * docGetMissedScanShipmentList should be fine.
			 */
			if (docMissedShipmentListSrvOutput.getDocumentElement().hasChildNodes()) {
				log.debug("before prepareMissedShipmentOutDocument call for Shipments Not picked/cancelled yet");
				prepareMissedShipmentOutDocument(docGetMissedScanShipmentList, docMissedShipmentListSrvOutput);
				log.verbose("After prepareMissedShipmentOutDocument call for Shipments Not picked/cancelled yet"
						+ XMLUtil.getXMLString(docMissedShipmentListSrvOutput));
				log.debug("LastCompleted/Missed Scan Populated  Batch EndTime:: " + strEndTime);
				ZonedDateTime zdtimeBatchComplete = ZonedDateTime.parse(strEndTime);
				docMissedShipmentListSrvOutput.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_DATE_BATCH_COMPLETED, zdtimeBatchComplete.format(dtfMMSddSYYYY));
			} else {
				docMissedShipmentListSrvOutput.getDocumentElement()
						.setAttribute(AcademyConstants.ATTRIBUTE_TOTAL_NO_RECORDS, "0");
			}
			log.verbose(
					"AcademyStoreMissedShipmentList.fetchMissedShipmentList Output XML docMissedShipmentListSrvOutput:: "
							+ XMLUtil.getXMLString(docMissedShipmentListSrvOutput));
		} catch (Exception exGen) {
			log.error("fetchMissedShipmentList exGen::'" + exGen);
			if (YFCObject.isNull(docMissedShipmentListSrvOutput)) {
				docMissedShipmentListSrvOutput = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENTS)
						.getDocument();

			}
			docMissedShipmentListSrvOutput.getDocumentElement()
					.setAttribute(AcademyConstants.ATTRIBUTE_TOTAL_NO_RECORDS, "0");
		} finally {
			docMissedShipmentListSrvOutput.getDocumentElement().setAttribute(AcademyConstants.ATTR_MSL_FEATURE_ENABLED, 
					inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_MSL_FEATURE_ENABLED));
			log.debug(
					"AcademyStoreMissedShipmentList.fetchMissedShipmentList Output XML docMissedShipmentListSrvOutput:: "
							+ XMLUtil.getXMLString(docMissedShipmentListSrvOutput));
		}
		log.endTimer("fetchMissedShipmentList");
		return docMissedShipmentListSrvOutput;
	}

	/**
	 * This method finds LastCompleted BatchHdrKey for the Store
	 * 
	 * @param env
	 * @param strStoreNo
	 * @return
	 * @throws Exception
	 */
	private Document getLastCompletedBatchHrdDtls(YFSEnvironment env, String strStoreNo) throws Exception {
		log.verbose("getLastCompletedBatchHrdDtls Input strStoreNo:: " + strStoreNo);
		Document docGetLastCompletedBatchListInp = XMLUtil.createDocument(AcademyConstants.ACAD_SCAN_BATCH_HDR);
		Element eleLastComBatch = docGetLastCompletedBatchListInp.getDocumentElement();
		eleLastComBatch.setAttribute(AcademyConstants.ATTR_IGNORE_ORDERING, AcademyConstants.STR_NO);
		eleLastComBatch.setAttribute(AcademyConstants.ATTR_MAX_RECORD, AcademyConstants.STR_ONE);
		eleLastComBatch.setAttribute(AcademyConstants.ATTR_STORENO, strStoreNo);
		eleLastComBatch.setAttribute(AcademyConstants.ATTR_BATCH_SCAN_STATUS, AcademyConstants.VALUE_COMPLETED);
		Element eleOrderBy = docGetLastCompletedBatchListInp.createElement(AcademyConstants.ELE_ORDERBY);
		Element eleAttribute = docGetLastCompletedBatchListInp.createElement(AcademyConstants.ELE_ATTRIBUTE);
		eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ACAD_SCAN_BATCH_HDR_KEY);
		eleAttribute.setAttribute(AcademyConstants.ATTR_ORD_BY_DESC, AcademyConstants.STR_YES);
		eleOrderBy.appendChild(eleAttribute);
		eleLastComBatch.appendChild(eleOrderBy);
		/**
		 * <ACADScanBatchHeader BatchScanStatus="COMPLETED" IgnoreOrdering="N"
		 * MaximumRecords="1" StoreNo="033"> <OrderBy>
		 * <Attribute Desc="Y" Name="AcadScanBatchHeaderKey"/> </OrderBy>
		 * </ACADScanBatchHeader>
		 */
		log.verbose("getLastCompletedBatchHrdDtls docGetLastCompletedBatchListInp::"
				+ XMLUtil.getXMLString(docGetLastCompletedBatchListInp));
		Document docGetScanBatchHdrListOutput = AcademyUtil.invokeService(env,
				AcademyConstants.SERV_ACAD_UI_GET_MSL_BATCH_HDR, docGetLastCompletedBatchListInp);
		log.verbose("getLastCompletedBatchHrdDtls docGetScanBatchHdrListOutput:: "
				+ XMLUtil.getXMLString(docGetScanBatchHdrListOutput));
		return docGetScanBatchHdrListOutput;
	}

	/**
	 * This method get the list of Missed Shipments for a BatchHdrKey from
	 * ACAD_MISSED_SCAN_DETAILS table
	 * 
	 * @param env
	 * @param sMissedScanBatchHrdKey
	 * @param strStoreNo
	 * @return
	 * @throws Exception
	 */
	private Document getMissedScanShipmentList(YFSEnvironment env, String sMissedScanBatchHrdKey, String strStoreNo)
			throws Exception {
		log.verbose("getMissedScanShipmentList Input sMissedScanBatchHrdKey :: " + sMissedScanBatchHrdKey);
		log.verbose("getMissedScanShipmentList Input strStoreNo :: " + strStoreNo);
		Document docMissedScanListInput = XMLUtil.createDocument(AcademyConstants.ELE_ACAD_MISSED_SCAN_DETAILS);
		Element eleMissedScanhDetailsList = docMissedScanListInput.getDocumentElement();
		eleMissedScanhDetailsList.setAttribute(AcademyConstants.ATTR_SCAN_BATCH_HDR_KEY, sMissedScanBatchHrdKey);
		eleMissedScanhDetailsList.setAttribute(AcademyConstants.ATTR_STORENO, strStoreNo);
		/**
		 * Input <AcadMissedScanDtlsList>
		 * <AcadMissedScanDtls AcadScanBatchHeaderKey="" LastScannedOn="" ShipmentNo=
		 * ""/> </AcadMissedScanDtlsList>
		 */

		log.verbose(
				"getMissedScanShipmentList docMissedScanListInput :: " + XMLUtil.getXMLString(docMissedScanListInput));
		Document docMissedScanListOutput = AcademyUtil.invokeService(env,
				AcademyConstants.SERV_ACAD_UI_GET_MISSED_SCAN_LIST, docMissedScanListInput);
		log.verbose("getMissedScanShipmentList docMissedScanListOutput :: "
				+ XMLUtil.getXMLString(docMissedScanListOutput));
		return docMissedScanListOutput;

	}

	/**
	 * This method populates a HastSet of unique Missed Shipment Data, requires to
	 * filter out shipments which are already picked/canceled
	 * 
	 * @param docGetMissedScanShipmentList
	 * @param hsUnqueMissedShipmentNo
	 * @throws Exception
	 */
	private void populateMissedDataCollection(Document docGetMissedScanShipmentList,
			Set<String> hsUnqueMissedShipmentNo) throws Exception {

		log.verbose("populateMissedDatacollections Input  hsUnqueMissedShipmentNo :: " + hsUnqueMissedShipmentNo);
		log.verbose("populateMissedDatacollections Input  docGetMissedScanShipmentList :: "
				+ XMLUtil.getXMLString(docGetMissedScanShipmentList));
		XMLUtil.getElementsByTagName(docGetMissedScanShipmentList.getDocumentElement(),
				AcademyConstants.ELE_ACAD_MISSED_SCAN_DETAILS).stream().forEach(eleMissedScan -> {
					log.debug("eleMissedScan:: " + XMLUtil.getElementXMLString((Element) eleMissedScan));
					String strShipmentNo = ((Element) eleMissedScan).getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
					hsUnqueMissedShipmentNo.add(strShipmentNo);
					log.verbose("populateMissedDataCollections hsUnqueMissedShipmentNo::" + hsUnqueMissedShipmentNo);

				});
		log.debug("populateMissedDataCollections Output hsUnqueMissedShipmentNo.size()::"
				+ hsUnqueMissedShipmentNo.size());
		log.verbose("populateMissedDatacollections Onput hsUnqueMissedShipmentNo :: " + hsUnqueMissedShipmentNo);
	}

	/**
	 * This method removes extra elements, set Pagination to a CustomPageSize and
	 * calls getShipmnetList to get the list missed shipments which are yet not
	 * picked/cancelled from the list of missed scan unique shipment list.
	 * 
	 * @param env
	 * @param unqueMissedShipmentNoSet
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	private Document getFilteredShipmentList(YFSEnvironment env, Set<String> unqueMissedShipmentNoSet, Document inDoc)
			throws Exception {

		log.verbose("getFilteredShipmentList Input unqueMissedShipmentNoSet:: " + unqueMissedShipmentNoSet);
		// Fetch the ValidateStatus from service arguments

		String strStatus = YFCObject.isVoid(props.getProperty(AcademyConstants.STR_VALIDATE_STATUS))
				? "1100.70.06.30.5,1100.70.06.30.7"
				: props.getProperty(AcademyConstants.STR_VALIDATE_STATUS);
		// Preparing InDoc for getShipmentList (ComplexQuery)

		Element eleShipmentSearchInp = inDoc.getDocumentElement();
		eleShipmentSearchInp.setAttribute(AcademyConstants.ATTR_DEL_METHOD, AcademyConstants.STR_DELIVERY_METHOD_VALUE);
		eleShipmentSearchInp.setAttribute(AcademyConstants.STR_DOCUMENT_TYPE, AcademyConstants.STR_DOCUMENT_TYPE_VALUE);
		Element eleContainers = XMLUtils.getElementByName(eleShipmentSearchInp, AcademyConstants.ELE_CONTAINERS);
		eleShipmentSearchInp.removeChild(eleContainers);
		Element eleShipmentLines = XMLUtils.getElementByName(eleShipmentSearchInp, AcademyConstants.ELE_SHIPMENT_LINES);
		eleShipmentSearchInp.removeChild(eleShipmentLines);
		Element eleBillToAddress = XmlUtils.getChildElement(eleShipmentSearchInp, AcademyConstants.ELE_BILL_TO_ADDRESS);
		eleShipmentSearchInp.removeChild(eleBillToAddress);
		Element eleOrderBy = XMLUtils.getElementByName(eleShipmentSearchInp, AcademyConstants.ELE_ORDERBY);
		eleShipmentSearchInp.removeChild(eleOrderBy);
		eleShipmentSearchInp.removeAttribute(AcademyConstants.ATTR_APPOINTMENT_NO_QRYTYPE);
		eleShipmentSearchInp.removeAttribute(AcademyConstants.ATTR_SHIP_NO_QRY_TYPE);
		eleShipmentSearchInp.removeAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		eleShipmentSearchInp.removeAttribute(AcademyConstants.STR_SEARCH_TYPE);
		eleShipmentSearchInp.removeAttribute(AcademyConstants.ATTR_STATUS);
		eleShipmentSearchInp.removeAttribute(AcademyConstants.ATTR_STATUS);
		eleShipmentSearchInp.removeAttribute(AcademyConstants.STR_SHIPMENT_CREATETS);
		eleShipmentSearchInp.removeAttribute(AcademyConstants.ATTR_CREATETS_QRY_TYPE);
		Element elExtn = XmlUtils.getChildElement(eleShipmentSearchInp, AcademyConstants.ELE_EXTN);
		eleShipmentSearchInp.removeChild(elExtn);
		eleShipmentSearchInp.setAttribute(AcademyConstants.ATTR_IGNORE_ORDERING, AcademyConstants.STR_YES);

		Element eleComplexQuery = inDoc.createElement(AcademyConstants.COMPLEX_QRY_ELEMENT);
		eleComplexQuery.setAttribute(AcademyConstants.COMPLEX_OPERATOR_ATTR, AcademyConstants.COMPLEX_OPERATOR_AND_VAL);
		Element eleAnd = inDoc.createElement(AcademyConstants.COMPLEX_AND_ELEMENT);

		Element eleStatusOr = inDoc.createElement(AcademyConstants.COMPLEX_OR_ELEMENT);
		List<String> lStatus = new ArrayList<String>(Arrays.asList(strStatus.split(AcademyConstants.STR_COMMA)));
		lStatus.forEach((sStatus) -> {
			Element eleExp = inDoc.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
			eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_STATUS);
			eleExp.setAttribute(AcademyConstants.ATTR_VALUE, sStatus);
			eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
			eleStatusOr.appendChild(eleExp);
			;

		});
		eleAnd.appendChild(eleStatusOr);
		Element eleShipNoOr = inDoc.createElement(AcademyConstants.COMPLEX_OR_ELEMENT);
		unqueMissedShipmentNoSet.forEach((strShipmentNo) -> {
			Element eleExp = inDoc.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
			eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_SHIPMENT_NO);
			eleExp.setAttribute(AcademyConstants.ATTR_VALUE, strShipmentNo);
			eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
			eleShipNoOr.appendChild(eleExp);
		});
		eleAnd.appendChild(eleShipNoOr);
		eleComplexQuery.appendChild(eleAnd);
		eleShipmentSearchInp.appendChild(eleComplexQuery);

		/**
		 * <Shipment ApplyQueryTimeout="Y" DeliveryMethod="PICK"
		 * DisplayLocalizedFieldInLocale="en_US_CST" DocumentType="0001" EnterpriseCode=
		 * "Academy_Direct" IgnoreOrdering="Y" MaximumRecords="5000"
		 * MissedScanFeatureEnabled="Y" QueryTimeout="60" ReceivingNode="" SCAC=""
		 * ShipNode="033" ShowRelatedOrders="">
		 * <Paginate IgnoreOrdering="Y" PageNumber="1" PageSize="200" PaginationStrategy
		 * ="NEXTPAGE"> <PreviousPage/> </Paginate> <ComplexQuery Operator="AND"> <And>
		 * <Or> <Exp Name="Status" QryType="EQ" Value="1100.70.06.30.5"/>
		 * <Exp Name="Status" QryType="EQ" Value="1100.70.06.30.7"/> </Or> <Or>
		 * <Exp Name="ShipmentNo" QryType="EQ" Value="100316138"/>
		 * <Exp Name="ShipmentNo" QryType="EQ" Value="100316248"/> </Or> </And>
		 * </ComplexQuery> </Shipment>
		 */
		Element elePaginate = XMLUtils.getElementByName(eleShipmentSearchInp, "Paginate");
		if (!YFCObject.isNull(elePaginate)) {
			// Fetch the CustomPageSIze from service arguments
			String strCustomPageSIze = props.getProperty(AcademyConstants.STR_MSL_CUSTOM_PAGE_SIZE);
			elePaginate.setAttribute(AcademyConstants.ATTR_PAGE_SIZE, strCustomPageSIze);
		}

		log.verbose("getFilteredShipmentList  getShipmentListInputDoc:: " + XMLUtil.getXMLString(inDoc));
		// Preparing OutDoc for getShipmentList
		Document docGetShipmentListOutput = AcademyUtil.invokeService(env,
				AcademyConstants.SERV_ACADEMY_UI_GET_MSL_SHIPMENT_LIST_FOR_WSC, inDoc);
		log.verbose("getFilteredShipmentList  getShipmentListOutputDoc:: "
				+ XMLUtil.getXMLString(docGetShipmentListOutput));
		return docGetShipmentListOutput;
	}

	/**
	 * This method adds LastScanDate and a CustomField in format 'yyyyMMdd' to the
	 * output of gteShipmentList to sort in an xslt
	 *
	 * 
	 * @param docMissedShipmentListSrvOutput
	 * @param docMissedShipmentListSrvOutput
	 * @param blankLastScanDtMissedShipmentNoSet
	 * @throws Exception
	 * @throws ParseException
	 * @throws TransformerException
	 */
	private void prepareMissedShipmentOutDocument(Document docGetMissedScanShipmentList,
			Document docMissedShipmentListSrvOutput) throws Exception {

		log.verbose("prepareMissedShipmentOutDocument Input docGetMissedScanShipmentList:: "
				+ XMLUtil.getXMLString(docGetMissedScanShipmentList));
		log.verbose("prepareMissedShipmentOutDocument Input docMissedShipmentListSrvOutput:: "
				+ XMLUtil.getXMLString(docMissedShipmentListSrvOutput));
		DateTimeFormatter dtfLastScanDt = DateTimeFormatter.ofPattern(AcademyConstants.STR_MISSED_SHIP_DATE_FORMAT);
		DateTimeFormatter dtfSortedDate = DateTimeFormatter.ofPattern(AcademyConstants.DATE_YYYYMMDD_FORMAT);
		XMLUtil.getElementsByTagName(docMissedShipmentListSrvOutput.getDocumentElement(), AcademyConstants.ELE_SHIPMENT)
				.stream().forEach(eleMissedShipment -> {
					try {
						String strShipmentNo = ((Element) eleMissedShipment)
								.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
						log.debug("prepareMissedShipmentOutDocument strShipmentNo:: " + strShipmentNo);
						String strLastScannedDate = XMLUtil.getAttributeFromXPath(docGetMissedScanShipmentList,
								new StringBuilder(AcademyConstants.XPATH_MISSED_SCAN_SHIP).append(strShipmentNo)
										.append(AcademyConstants.XPATH_END_LAST_SCAN_NO).toString());
						log.debug("prepareMissedShipmentOutDocument strLastScannedDate:: " + strLastScannedDate);
						ZonedDateTime zdSrvtimeLastScannedDate = YFCObject.isVoid(strLastScannedDate) ? null
								: ZonedDateTime.parse(strLastScannedDate);
						log.debug("prepareMissedShipmentOutDocument zdSrvtimeLastScannedDate:: "
								+ zdSrvtimeLastScannedDate);
						if (!YFCObject.isVoid(zdSrvtimeLastScannedDate)) {
							((Element) eleMissedShipment).setAttribute(AcademyConstants.STR_LAST_SCAN_DATE,
									zdSrvtimeLastScannedDate.format(dtfMMSddSYYYY));
							((Element) eleMissedShipment).setAttribute(AcademyConstants.ATTR_SORT_DATE,
									zdSrvtimeLastScannedDate.format(dtfYYYYMMDD));
						} else {
							((Element) eleMissedShipment).setAttribute(AcademyConstants.ATTR_SORT_DATE,
									AcademyConstants.STR_EIGHT_ZEROS);
						}
						//OMNI-87502, 87879 - Start
						((Element) eleMissedShipment).setAttribute(AcademyConstants.ATTR_MSL, AcademyConstants.ATTR_Y);
						//OMNI-87502, 87879 - End
						log.verbose("prepareMissedShipmentOutDocument eleMissedShipOut:: "
								+ XMLUtil.getElementXMLString((Element) eleMissedShipment));

					}
					/**
					 * ParserException will never happen unless integration of the Application is
					 * lost for LastScannedDate attribute or XML is not in proper format And in
					 * above such scenarios throwing a checked Exception as a RuntimeException is
					 * commonly accepted in industry, for Microserviceas development and in other
					 * feature developemnt,e.g. FileNotFoundException, though CheckedException but
					 * is handled as runtime.
					 */
					catch (Exception exGen) {
						log.error(exGen);
						throw new RuntimeException(exGen);
					}
				});
		log.verbose("prepareMissedShipmentOutDocument docMissedShipmentListSrvOutput:: "
				+ XMLUtil.getXMLString(docMissedShipmentListSrvOutput));
	}
}
