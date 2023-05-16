package com.academy.ecommerce.sterling.shipment;

/**#########################################################################################
*
* Project Name                : ASO
* Module                      : OMNI-67016
* Author                      : C0028009(Arun Kumar)
* Author Group				  : CTS - POD
* Date                        : 04-APRIL-2022
* Description				  : This class  will be invoked as a Extended API in a service AcademyScanLabelReportCreateService.
*                               This Class will create a Input Document to the GetShipmentList with the InDoc input and Internally
*                               frame the Complex Query and pass it to the getShipmentList APi to get the ShipmentDetails and 
*                               frame a Input with the details from Output and pass it service AcademyScanBatchDetailsCreateService
*                               to create a record into ScanDetails table and  invoke the method in AcademyFetchScanBatchDetails
*                               to get the Batch Details and create a Return Document with the Required Output and return the Document
* #########################################################################################*/
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import com.academy.ecommerce.sterling.shipment.AcademyFetchScanBatchDetails;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyScanLabelForReport implements YIFCustomApi {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyScanLabelForReport.class);

	@Override
	public void setProperties(Properties arg0) throws Exception {

	}

	/**
	 * #########################################################################################
	 *
	 * Sample Input : LabelNo may be OrderNo || ShipmentNo
	 * <ACADScanBatchDetails AcadScanBatchHeaderKey="20220404020126265874712" LabelNo=
	 * '100317381' StoreNo='033' />
	 * 
	 * #########################################################################################
	 */
	public Document scanLabelTicket(YFSEnvironment env, Document inDoc) {
		log.beginTimer(this.getClass() + ".scanLabelTicket");
		log.debug("java:AcademyScanTicketForReport:scanLabelTicket():InDoc " + XMLUtil.getXMLString(inDoc));

		String strIsCancelled = null;
		String strOrderNo = null;
		String strShipmentNo = null;
		String strShipmentType = null;
		String strStatus = null;
		Document getShipmentListOutDoc = null;
		Document docScanOutputShipment = null;
		boolean isInValidShipment = true;
		try {

			Element eleBatchDetail = inDoc.getDocumentElement();
			String strBatchKey = eleBatchDetail.getAttribute(AcademyConstants.ACAD_SCAN_BATCH_HDR_KEY);
			String strLabelNo = eleBatchDetail.getAttribute(AcademyConstants.STR_LABEL_NO);
			String strStoreNo = eleBatchDetail.getAttribute(AcademyConstants.ATTR_STORE_NO);

			log.verbose("Batch header Key : " + strBatchKey + " Label No : " + strLabelNo + " StoreNo : " + strStoreNo);
			if (!YFCObject.isVoid(strLabelNo)) {
				// getShipmentList using LabelNo,StoreNo
				getShipmentListOutDoc = getShipmentListOut(env, strLabelNo, strStoreNo);
				Element Shipments = getShipmentListOutDoc.getDocumentElement();
				String strTotalNoOfRecords = Shipments.getAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS);
				
				int iShipmentCount = 0;
				if (!YFCObject.isVoid(strTotalNoOfRecords)) {
				 iShipmentCount = Integer.parseInt(strTotalNoOfRecords);
				log.verbose("Total Records " + strTotalNoOfRecords);
				}
				
				if (iShipmentCount > 0) {

					NodeList nlPickShipments = null;
					if (iShipmentCount > 0) {
		    				
						nlPickShipments = XPathUtil.getNodeList(getShipmentListOutDoc,AcademyConstants.XPATH_SHIPMENT);
		   				 
					}

					if (!YFCObject.isVoid(nlPickShipments) && nlPickShipments.getLength() > 0) {

						for (int iCount = 0; iCount < nlPickShipments.getLength(); iCount++) {
							
							Element elemShipment = (Element) nlPickShipments.item(iCount);
							strOrderNo = elemShipment.getAttribute(AcademyConstants.ATTR_ORDER_NO);
							strShipmentNo = elemShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
							strShipmentType = elemShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
							strStatus = elemShipment.getAttribute(AcademyConstants.ATTR_STATUS);
							    //OMNI-72066-Ability to  Scan by ShipmentNo-Start
							if (strLabelNo.equalsIgnoreCase(strShipmentNo)) {
							
								log.verbose("Shipment No : " + strShipmentNo + "Shipment Type : " + strShipmentType);

								strShipmentType = elemShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
								if (!YFCObject.isVoid(strShipmentType)
										&& strShipmentType.equalsIgnoreCase(AcademyConstants.STR_SHIP_TO_STORE)) {
									strShipmentType = AcademyConstants.STR_SHIP_TO_STORE;
								} else {
									strShipmentType = AcademyConstants.V_FULFILLMENT_TYPE_BOPIS;
								}
								//OMNI-72066-Ability to  Scan by ShipmentNo-End
								if (!YFCObject.isVoid(strStatus)
										&& strStatus.equalsIgnoreCase(AcademyConstants.VAL_CANCELLED_STATUS)) {
									strIsCancelled = AcademyConstants.STR_YES;
								} else {
									strIsCancelled = AcademyConstants.STR_NO;
								}
								isInValidShipment = false;
								break;
							} else if ((strLabelNo.equalsIgnoreCase(strOrderNo)) && 
							(!strShipmentType.equalsIgnoreCase(AcademyConstants.STR_SHIP_TO_STORE))) {

								strShipmentType = AcademyConstants.V_FULFILLMENT_TYPE_BOPIS;

								if (!YFCObject.isVoid(strStatus)
										&& strStatus.equalsIgnoreCase(AcademyConstants.VAL_CANCELLED_STATUS)) {
									strIsCancelled = AcademyConstants.STR_YES;
								} else {
									strIsCancelled = AcademyConstants.STR_NO;
								}
								isInValidShipment = false;
								break;
							} 

						}

					} else {

						isInValidShipment = true;

					}

					log.verbose("OrderNo " + strOrderNo + " ShipmentNo " + strShipmentNo + " ShipmentType "
							+ strShipmentType + " Status " + strStatus);

					log.verbose("isInValidShipment" + isInValidShipment);

					if (!isInValidShipment) {

						Document docAcadLabelPrintDataInput = prepareDetailRecordInDoc(env, strShipmentNo, strOrderNo,
								strBatchKey, strIsCancelled, strShipmentType);
                        //Invoke Service to Create Record into Entry Table -Start
						AcademyUtil.invokeService(env, AcademyConstants.SER_ACADEMY_SCAN_BATCH_DETAILS_CREATE_SERVICE,
								docAcadLabelPrintDataInput);
						//Invoke Service to Create Record into Entry Table - End
						Document docScanInput = XMLUtil.createDocument(AcademyConstants.ACAD_SCAN_BATCH_HDR);
						Element eleBatchHdr = docScanInput.getDocumentElement();
						eleBatchHdr.setAttribute(AcademyConstants.ACAD_SCAN_BATCH_HDR_KEY, strBatchKey);
						Document getCountDoc = AcademyFetchScanBatchDetails.getBatchShipmentCount(env, docScanInput);
						log.verbose("Output of AcademyGetScanBatchHeaderListService :: "
								+ XMLUtil.getXMLString(getCountDoc));
						Element eleScanDetail = getCountDoc.getDocumentElement();

						String strShipmentsCancelled = eleScanDetail
								.getAttribute(AcademyConstants.ATTR_SHIPMENTS_CANCELLED);
						String strShipmentsScanned = eleScanDetail
								.getAttribute(AcademyConstants.ATTR_SHIPMENTS_SCANNED);
						String strElapseTime = eleScanDetail.getAttribute(AcademyConstants.ATTR_BATCH_SCAN_DURATION);
									
						docScanOutputShipment = prepareReturnDoc(env, strShipmentNo, strIsCancelled, strStoreNo,
								strShipmentsScanned, strShipmentsCancelled, strOrderNo, strBatchKey, strElapseTime,
								strShipmentType);
						// Added Extra Attributes to MeshupOutput if shipment is cancelled OMNI-69370
						//OMNI-74000 starts --
                        //if (AcademyConstants.STR_YES.equalsIgnoreCase(strIsCancelled)) {
                            docScanOutputShipment = prepareReturnDocCancelledShipment(env, docScanOutputShipment,getShipmentListOutDoc);
							log.verbose("Returned output for CancelledShipment is :: "
									+ XMLUtil.getXMLString(docScanOutputShipment));
                        //}
						//OMNI-74000 ends--
						// Added Extra Attributes to MeshupOutput if shipment is cancelled OMNI-69370
						log.verbose("Returned output for Shipment is :: " + XMLUtil.getXMLString(docScanOutputShipment));


					} else {
						docScanOutputShipment = prepareInvalidLabelSearchResponse();
						
					}

				} else {
					docScanOutputShipment = prepareInvalidLabelSearchResponse();
				
				}

			} else {
				docScanOutputShipment = prepareInvalidLabelSearchResponse();
			
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		log.endTimer("AcademyScanTicketForReport::scanLabelTicket");

		return docScanOutputShipment;
	}

	private Document prepareInvalidLabelSearchResponse() throws ParserConfigurationException {
		Document docScanOutputShipment = XMLUtil.createDocument(AcademyConstants.ACAD_SCAN_BATCH_HEADER);
		Element eleBatchHeader = docScanOutputShipment.getDocumentElement();
		eleBatchHeader.setAttribute(AcademyConstants.IS_VALID_LABEL, AcademyConstants.STR_NO);
		log.debug("AcademyScanTicketForReport.java:scanLabelTickets():inDoc  : "
				+ XMLUtil.getXMLString(docScanOutputShipment));
		return docScanOutputShipment;
	}

	/*
	 * Sample Output: <Shipments TotalNumberOfRecords="1"> <Shipment
	 * DeliveryMethod="PICK" OrderNo="Y100224150" ShipmentNo="100317124"
	 * ShipmentType="STS" Status="1100.70.06.30.5"/> </Shipments>
	 */
	
	private static Document getShipmentListOut(YFSEnvironment env, String strLabelNo, String strStoreNo) throws Exception {
		Document getShipmentListOutDoc = null;
		Document docInputForGetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleShipment = docInputForGetShipmentList.getDocumentElement();

		Element eleComplexQuery = docInputForGetShipmentList.createElement(AcademyConstants.COMPLEX_QRY_ELEMENT);
		eleShipment.appendChild(eleComplexQuery);
		eleComplexQuery.setAttribute(AcademyConstants.COMPLEX_OPERATOR_ATTR, AcademyConstants.COMPLEX_OR_ELEMENT);

		Element eleAnd = docInputForGetShipmentList.createElement(AcademyConstants.COMPLEX_AND_ELEMENT);
		eleComplexQuery.appendChild(eleAnd);
		  Element eleExp2 = docInputForGetShipmentList.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
			eleExp2.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_DOC_TYPE);
			eleExp2.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.SALES_DOCUMENT_TYPE);
			eleAnd.appendChild(eleExp2);
			//Added ShipNode to Complex Query-Start
		Element eleExp3 = docInputForGetShipmentList.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp3.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.SHIP_NODE);
		eleExp3.setAttribute(AcademyConstants.ATTR_VALUE, strStoreNo);
		eleAnd.appendChild(eleExp3);
            //Added ShipNode to Complex Query-End
			// Added DeliveryMethod to Complex Query-OMNI-72066-Start
		Element eleExp4 = docInputForGetShipmentList.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp4.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_DEL_METHOD);
		eleExp4.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STR_PICK);
		eleAnd.appendChild(eleExp4);
		    // Added DeliveryMethod to Complex Query-OMNI-72066-End
		Element eleOr = docInputForGetShipmentList.createElement(AcademyConstants.COMPLEX_OR_ELEMENT);
		eleAnd.appendChild(eleOr);

		Element eleExp = docInputForGetShipmentList.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_SHIPMENT_NO);
		eleExp.setAttribute(AcademyConstants.ATTR_VALUE, strLabelNo);
		eleOr.appendChild(eleExp);

		Element eleExp1 = docInputForGetShipmentList.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp1.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_ORDER_NO);
		eleExp1.setAttribute(AcademyConstants.ATTR_VALUE, strLabelNo);
		eleOr.appendChild(eleExp1);

		log.verbose("getOrderList API Input is :: " + XMLUtil.getXMLString(docInputForGetShipmentList));
		//OMNI-74000 starts here--
		Document outputTemplate = YFCDocument.getDocumentFor(
                "<Shipments TotalNumberOfRecords=''>\r\n" +
                "   <Shipment DeliveryMethod='' OrderNo='' ShipmentNo=''\r\n" +
                "       ShipmentType='' Status=''>\r\n" +
                "       <BillToAddress FirstName='' LastName='' />\r\n" +
                "       <ShipmentLines>\r\n" +
                "           <ShipmentLine ItemID=''>\r\n" +
                "               <Order OrderDate='' />\r\n" +
                "               <OrderLine DeliveryMethod=''>\r\n" +
                "                   <PersonInfoMarkFor LastName='' FirstName='' />\r\n" +
                "               </OrderLine>\r\n" +
                "           </ShipmentLine>\r\n" +
                "       </ShipmentLines>\r\n" +
                "       <ShipmentStatusAudits>\r\n" +
                "           <ShipmentStatusAudit NewStatus=''\r\n" +
                "               NewStatusDate='' />\r\n" +
                "       </ShipmentStatusAudits>\r\n" +
                "   </Shipment>\r\n" +
                "</Shipments>")
		//OMNI-74000 ends here--
				.getDocument();
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, outputTemplate);
		getShipmentListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
				docInputForGetShipmentList);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		log.verbose("getShipmentList API output is :: " + XMLUtil.getXMLString(getShipmentListOutDoc));

		return getShipmentListOutDoc;
	}

	private static Document prepareDetailRecordInDoc(YFSEnvironment env, String strShipmentNo, String strOrderNo,
			String strBatchKey, String strIsCancelled, String strShipmentType) throws Exception {

		Document docAcadLabelPrintDataInput = XMLUtil.createDocument(AcademyConstants.ACAD_SCAN_BATCH_DETAILS);
		Element eleAcadLabelPrintData = docAcadLabelPrintDataInput.getDocumentElement();
		eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		eleAcadLabelPrintData.setAttribute(AcademyConstants.ACAD_SCAN_BATCH_HDR_KEY, strBatchKey);
		eleAcadLabelPrintData.setAttribute(AcademyConstants.IS_CANCELLED, strIsCancelled);
		eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, strShipmentType);

		log.debug(
				"AcademyScanTicketForReport.java:scanLabelTickets():inDoc AcademyCreateLabelPrintDataServiceForScanDetailTable : "
						+ XMLUtil.getXMLString(docAcadLabelPrintDataInput));
		return docAcadLabelPrintDataInput;
	}

	private static Document prepareReturnDoc(YFSEnvironment env, String strShipmentNo, String strIsCancelled,
			String strStoreNo, String strShipmentsScanned, String strShipmentsCancelled, String strOrderNo,
			String strBatchKey, String strElapseTime, String strShipmentType) throws Exception {

		Document docScanOutput = XMLUtil.createDocument(AcademyConstants.ACAD_SCAN_BATCH_HEADER);
		Element eleBatchHeader = docScanOutput.getDocumentElement();
		eleBatchHeader.setAttribute(AcademyConstants.ACAD_SCAN_BATCH_HEADER_KEY, strBatchKey);
		eleBatchHeader.setAttribute(AcademyConstants.ATTR_STORE_NO, strStoreNo);
		eleBatchHeader.setAttribute(AcademyConstants.SCANNED_SHIPMENT_COUNT, strShipmentsScanned);
		eleBatchHeader.setAttribute(AcademyConstants.CANCELLED_SHIPMENT_COUNT, strShipmentsCancelled);
		eleBatchHeader.setAttribute(AcademyConstants.ELAPSED_TIME, strElapseTime);
		Element eleACADScanBatchDetails = docScanOutput.createElement(AcademyConstants.ACAD_SCAN_BATCH_DETAILS);
		eleBatchHeader.appendChild(eleACADScanBatchDetails);
		eleACADScanBatchDetails.setAttribute(AcademyConstants.IS_CANCELLED, strIsCancelled);
		eleACADScanBatchDetails.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		eleACADScanBatchDetails.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);

		log.debug(
				"AcademyScanTicketForReport.java:scanLabelTickets():inDoc AcademyCreateLabelPrintDataServiceForScanDetailTable : "
						+ XMLUtil.getXMLString(docScanOutput));
		return docScanOutput;
	}
    /*
	 * OMNI-69370--Start This Method is Invoked when Shipment is
	 * Cancelled(IsCancelled='Y'). This Method Contains the logic of Invocation of
	 * getShipmentList with ShipmentNo and from the output fetch
	 * Orderdate,CustomerName,AlternatePickupPersonName,LastPickUDate and Added it
	 * to the InDoc and Return the Document. Sample Output to the Service
	 * <Shipments> <Shipment DeliveryMethod="PICK" DocumentType="0001"
	 * OrderNo="853016" ShipNode="033" ShipmentNo="100316472" ShipmentType="STS"
	 * Status="9000"> <BillToAddress FirstName="vignesh" LastName="kb"/>
	 * <ShipmentLines> <ShipmentLine ItemID="024411381"> <Order
	 * OrderDate="2021-04-20T10:54:54-05:00"/> <OrderLine DeliveryMethod="PICK">
	 * <PersonInfoMarkFor FirstName="vignesh" LastName="kb"/> </OrderLine>
	 * </ShipmentLine> </ShipmentLines> <ShipmentStatusAudits> <ShipmentStatusAudit
	 * NewStatus="1100.70.06.20" NewStatusDate="2022-02-28T02:50:45-06:00"/>
	 * <ShipmentStatusAudit NewStatus="1100.70.06.30.5"
	 * NewStatusDate="2022-02-28T03:27:55-06:00"/> <ShipmentStatusAudit
	 * NewStatus="9000" NewStatusDate="2022-04-21T07:00:21-05:00"/>
	 * <ShipmentStatusAudit NewStatus="1100"
	 * NewStatusDate="2022-02-09T14:12:40-06:00"/> <ShipmentStatusAudit
	 * NewStatus="1100.70.06.10" NewStatusDate="2022-02-09T14:12:40-06:00"/>
	 * </ShipmentStatusAudits> </Shipment> </Shipments>
	 */

    public Document prepareReturnDocCancelledShipment(YFSEnvironment env, Document docScanOutput, Document getShipmentListOutDoc) throws Exception {
		String strNewStatus = null;
		String strAlternateFirstName = null;
		String strAlternateLastName = null;
		String strAlterCustomerName = null;
		String ExpectedDateFormatforPick = null;
		String strCustFirstName = null;
		String strCustLastName = null;
		String strAlternateCustomerName = "";
		//OMNI-71479 changes - START
		String strAlterFirstName = null;
		String strAlterLastName = null;
		//OMNI-71479 changes - END
		int i = 0;
		//OMNI-74000 starts here--
//      Node nlBatchHeader = XPathUtil.getNode(docScanOutput, AcademyConstants.ACAD_SCAN_BATCH_HEADER);
//      String strShipmentNo = XPathUtil.getString(nlBatchHeader, AcademyConstants.XPATH_ACAD_SCAN_DETAIL_SHIPMENT_NO);
//      Document docIpGetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
//      Element eleShipment = docIpGetShipmentList.getDocumentElement();
//      eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
//      log.verbose("getShipmentList API Input is :: " + XMLUtil.getXMLString(docIpGetShipmentList));
//      Document docTemplateGetShipmentList = YFCDocument.getDocumentFor(
//              "<Shipments><Shipment Status=''><BillToAddress FirstName='' LastName='' /><ShipmentLines><ShipmentLine ItemID=''><Order OrderDate='' /><OrderLine DeliveryMethod=''><PersonInfoMarkFor LastName=''  FirstName=''/></OrderLine></ShipmentLine></ShipmentLines><ShipmentStatusAudits ><ShipmentStatusAudit NewStatus='' NewStatusDate='' /></ShipmentStatusAudits></Shipment></Shipments>")
//              .getDocument();
//      env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docTemplateGetShipmentList);
//      Document docOpGetShipmentList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
//              docIpGetShipmentList);
// OMNI-74000 ends--
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
        log.verbose("getShipmentList API output is :: " + XMLUtil.getXMLString(getShipmentListOutDoc));
		// CustomerName --Start
        Node nlShipments = XPathUtil.getNode(getShipmentListOutDoc, AcademyConstants.ELE_SHIPMENTS);
		String strCustFName = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_CUST_FIRSTNAME);
		String strCustLName = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_CUST_LASTNAME);
		String strCustomerName = strCustLName + AcademyConstants.STR_COMMA + AcademyConstants.STR_BLANK + strCustFName;
		//OMNI-71479 changes - START
		if (strCustFName.length() > AcademyConstants.CUSTALT_TRIMMED_LENGTH) {
			strCustFirstName = strCustFName.substring(0, AcademyConstants.SUBSTRING_TRIMMED_LENGTH) + "...";
		} else {
			strCustFirstName = strCustFName;
		}
		if (strCustLName.length() > AcademyConstants.CUSTALT_TRIMMED_LENGTH) {
			strCustLastName = strCustLName.substring(0, AcademyConstants.SUBSTRING_TRIMMED_LENGTH) + "...";
		} else {
			strCustLastName = strCustLName;
		}
		//OMNI-71479 changes - END
		log.verbose("CustomerName ::: " + strCustomerName);
		// CustomerName--End
		// Orderdate--Start
		String strOrderDate = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_CANCEL_SHIPMENT_ORDER_DATE);
		String strOrderYear = strOrderDate.substring(0, 4);
		String strOrdeMnt = strOrderDate.substring(5, 7);
		String strOrderdate = strOrderDate.substring(8, 10);
		String strExpectedOrderDateFormat = strOrdeMnt + AcademyConstants.STR_SLASH + strOrderdate
				+ AcademyConstants.STR_SLASH + strOrderYear;
		log.verbose("Order Date :: " + strExpectedOrderDateFormat);
		// Orderdate --End
		// AlternateCustomerName --Start
		strAlternateFirstName = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_ALT_CUSTFNAME);
		strAlternateLastName = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_ALT_CUSTLNAME);
		if ((!YFCObject.isVoid(strAlternateFirstName)) && (!YFCObject.isVoid(strAlternateLastName))) {
		//OMNI-71479 changes - START
			if(strAlternateFirstName.length() > AcademyConstants.CUSTALT_TRIMMED_LENGTH) {
				strAlterFirstName = strAlternateFirstName.substring(0, AcademyConstants.SUBSTRING_TRIMMED_LENGTH) + "...";
			} else {
				strAlterFirstName = strAlternateFirstName;
					}
			if(strAlternateLastName.length() > AcademyConstants.CUSTALT_TRIMMED_LENGTH) {
				strAlterLastName = strAlternateLastName.substring(0, AcademyConstants.SUBSTRING_TRIMMED_LENGTH) + "...";
				} else {
				strAlterLastName = strAlternateLastName;
				}
			
		} else {
			strAlterFirstName = AcademyConstants.STR_HYPHEN;
			strAlterLastName = AcademyConstants.STR_HYPHEN;
		}
		//OMNI-71479 changes - END
		// AlternateCustomerName--End
		// LastPickDate--Start
        NodeList nlShipStatusAudit = getShipmentListOutDoc
				.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_STATUS_AUDIT);
		for (int c = 0; c < nlShipStatusAudit.getLength(); c++) {
			Node ndStatusAudit = nlShipStatusAudit.item(c);
			strNewStatus = ndStatusAudit.getAttributes().getNamedItem(AcademyConstants.ATTR_NEW_STATUS).getNodeValue();
			if ((!YFCObject.isVoid(strNewStatus))
					&& AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS.equalsIgnoreCase(strNewStatus)) {
				String strLastPickDate = ndStatusAudit.getAttributes().getNamedItem(AcademyConstants.STR_NEW_STATUS)
						.getNodeValue();
				String strPickYear = strLastPickDate.substring(0, 4);
				String strPickMnt = strLastPickDate.substring(5, 7);
				String strPickDate = strLastPickDate.substring(8, 10);
				ExpectedDateFormatforPick = strPickMnt + AcademyConstants.STR_SLASH + strPickDate
						+ AcademyConstants.STR_SLASH + strPickYear;
				log.verbose("Last Pick Date :: " + ExpectedDateFormatforPick);
				// LastPickDate----End
				break;
			}
		}
		Element eleScanHeader = docScanOutput.getDocumentElement();
		Element eleBatchScanDetails = (Element) eleScanHeader.getFirstChild();
		eleBatchScanDetails.setAttribute(AcademyConstants.ATTR_ORDER_DATE, strExpectedOrderDateFormat);
		eleBatchScanDetails.setAttribute(AcademyConstants.STR_LAST_PICK_DATE, ExpectedDateFormatforPick);
		eleBatchScanDetails.setAttribute(AcademyConstants.ATTR_CUSTOMER_FIRST_NAME, strCustFirstName);
		eleBatchScanDetails.setAttribute(AcademyConstants.ATTR_CUSTOMER_LAST_NAME, strCustLastName);
		eleBatchScanDetails.setAttribute(AcademyConstants.ATTR_CUST_NAME, strCustomerName);
		//OMNI-71479 changes - START
		eleBatchScanDetails.setAttribute("AlternateCustFirstName", strAlterFirstName);
		eleBatchScanDetails.setAttribute("AlternateCustLastName", strAlterLastName);
		//OMNI-71479 changes - END
		log.verbose("Extra Details of Cancelled Shipment : " + XMLUtil.getXMLString(docScanOutput));
		return docScanOutput;
	}
	// OMNI-69370--End
}
