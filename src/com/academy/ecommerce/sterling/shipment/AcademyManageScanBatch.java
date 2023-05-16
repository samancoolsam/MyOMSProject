package com.academy.ecommerce.sterling.shipment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.academy.util.common.AcademyUtil;
import com.academy.ecommerce.sterling.shipment.AcademyFetchScanBatchDetails;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;

/* *
 * Input: <ACADScanBatchHeader Action='INPROGRESS' StoreNo='033' UserID="User123" />
 * 
 * Output: <ACADScanBatchHeader AcadScanBatchHeaderKey="20220406080426266116418" BatchID="03320220406080416" BatchScanStatus="INPROGRESS" Createprogid="SterlingHttpTester" Createts="2022-04-06T08:04:26-05:00" Createuserid="admin" EndTime="2500-01-01T00:00:00-06:00" Lockid="0" Modifyprogid="SterlingHttpTester" Modifyts="2022-04-06T08:04:26-05:00" Modifyuserid="admin" StagedShipmentsCount="116" StartTime="2022-04-06T08:04:26-05:00" StoreNo="033" UserID="User123">	<ACADScanBatchDetailsList TotalNumberOfRecords="0"/> </ACADScanBatchHeader>
 * */

public class AcademyManageScanBatch implements YIFCustomApi {
	public static final YFCLogCategory log = YFCLogCategory.instance(AcademyManageScanBatch.class);

	public static String strUserId = null;
	public static String strStoreNo = null;
	public static String strAction = null;

	public Document manageScanBatch(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Input to AcademyManageScanBatch.manageScanBatch " + XMLUtil.getXMLString(inDoc));
		Element eleOfInput = inDoc.getDocumentElement();
		strAction = eleOfInput.getAttribute(AcademyConstants.ATTR_ACTION);
		strStoreNo = eleOfInput.getAttribute(AcademyConstants.ATTR_STORE_NO);
		strUserId = eleOfInput.getAttribute(AcademyConstants.ATTR_USER_ID);
		boolean hasInProgressBatch = false;

		if (!YFCObject.isVoid(strUserId) && !YFCObject.isVoid(strStoreNo) && !YFCObject.isVoid(strAction)) {

			/* OMNI-68376 - START
			 * If the Action value is "INPROGRESS", check if there is already Inprogress
			 * Batch in that Store. If Yes, return the Batch details. If No, Create a new
			 * Batch.
			 */
			if ((AcademyConstants.STR_INPROGRESS).equalsIgnoreCase(strAction)) {

				Document docToPassToGetHdrService = XMLUtil.createDocument(AcademyConstants.ACAD_SCAN_BATCH_HEADER);
				Element eleToPassToGetHdrService = docToPassToGetHdrService.getDocumentElement();
				eleToPassToGetHdrService.setAttribute(AcademyConstants.ATTR_BATCH_SCAN_STATUS, strAction);
				eleToPassToGetHdrService.setAttribute(AcademyConstants.ATTR_STORE_NO, strStoreNo);
				/*
				 * Invoking "AcademyGetScanBatchHdrList" Service to get all the Batches whose
				 * BatchScanStatus="INPROGRESS"
				 */
				log.verbose("Input to the AcademyGetScanBatchHeaderListService "+XMLUtil.getXMLString(docToPassToGetHdrService));
				Document getInprogressBatchListDoc = AcademyUtil.invokeService(env,
						AcademyConstants.ACADEMY_GET_SCAN_BATCH_HDR_LIST_SERVICE, docToPassToGetHdrService);
				log.verbose("Output of the AcademyGetScanBatchHeaderListService "+XMLUtil.getXMLString(getInprogressBatchListDoc));
				NodeList lisOfBatches = getInprogressBatchListDoc
						.getElementsByTagName(AcademyConstants.ACAD_SCAN_BATCH_HEADER);

				if (!YFCObject.isVoid(lisOfBatches) && lisOfBatches.getLength() > 0) {
					hasInProgressBatch = true;
				}

				Document docForAlreadyInprogressBatch = XMLUtil.newDocument();

				if (hasInProgressBatch) {
					ArrayList<String> lisOfModifyts = new ArrayList<String>();
					Node alreadyInprogBatchDBNode = lisOfBatches.item(0);
					Element alreadyInprogBatchDBEle = (Element) alreadyInprogBatchDBNode;

					/* Appending an attribute "HasActiveBatch" to the above element */
					alreadyInprogBatchDBEle.setAttribute(AcademyConstants.ATTR_HAS_ACTIVE_BATCH,
							AcademyConstants.VALUE_HAS_ACTIVE_BATCH_Y);

					// Getting the latest ModifyTs attribute value from AcadScanBatchDetails table
					// and appending it to the alreadyInprogBatchDBEle as 'lastScan'.
					Document docToPassToGetDetails = XMLUtil.createDocument(AcademyConstants.ACAD_SCAN_BATCH_DETAILS);
					Element eleToPassToGetDetails = docToPassToGetDetails.getDocumentElement();
					eleToPassToGetDetails.setAttribute(AcademyConstants.ACAD_SCAN_BATCH_HDR_KEY,
							alreadyInprogBatchDBEle.getAttribute(AcademyConstants.ACAD_SCAN_BATCH_HDR_KEY));
					/*
					 * Invoking the "AcademyScanBatchDtlListService" service using
					 * AcadScanBatchHeaderKey to get the details of the Batch
					 */
					log.verbose("Input to the AcademyScanBatchDtlListService " +XMLUtil.getXMLString(docToPassToGetDetails));
					Document getInprogressBatchDetailsDoc = AcademyUtil.invokeService(env,
							AcademyConstants.SERVICE_GET_ACAD_SCAN_BATCH_DETAILS_LIST, docToPassToGetDetails);
					log.verbose("Output of the AcademyScanBatchDtlListService " +XMLUtil.getXMLString(getInprogressBatchDetailsDoc));
					NodeList listOfBatchDetails = getInprogressBatchDetailsDoc
							.getElementsByTagName(AcademyConstants.ACAD_SCAN_BATCH_DETAILS);
					if (listOfBatchDetails.getLength() > 0) {
						for (int i = 0; i < listOfBatchDetails.getLength(); i++) {
							Element eleOfBatchDetails = (Element) listOfBatchDetails.item(i);
							lisOfModifyts.add(eleOfBatchDetails.getAttribute(AcademyConstants.ATTR_MODIFY_TS));
						}
						/* sorting the lisOFModifyts in Descending order to get the lastScan time. */
						Collections.sort(lisOfModifyts, Collections.reverseOrder());
						alreadyInprogBatchDBEle.setAttribute(AcademyConstants.ATTR_LAST_SCAN, lisOfModifyts.get(0));
					} else {
						alreadyInprogBatchDBEle.setAttribute(AcademyConstants.ATTR_LAST_SCAN,
								alreadyInprogBatchDBEle.getAttribute(AcademyConstants.ATTR_MODIFY_TS));
					}
					docForAlreadyInprogressBatch.appendChild(
							docForAlreadyInprogressBatch.adoptNode(alreadyInprogBatchDBNode.cloneNode(true)));

					return docForAlreadyInprogressBatch;

				}
				/*
				 * If there is no INPROGRESS Batch in the store then New Batch will be Created.
				 */
				else {
					Document newBatchHeaderdoc = createNewBatchHeader(env, inDoc);
					return newBatchHeaderdoc;
				}
				
			} else if ((AcademyConstants.STR_ACTION_RESET).equalsIgnoreCase(strAction)) {
				/*
				 * <ACADScanBatchHeader Action="RESET" StoreNo="033" UserID="sfs033" AcadScanBatchHeaderKey='20220426015845268411156'/>
				 */
				Element eleOfIpDoc = inDoc.getDocumentElement();
				String strBatchHeaderKey = eleOfIpDoc.getAttribute(AcademyConstants.ACAD_SCAN_BATCH_HDR_KEY);
				log.verbose("The value of attribute BatchHeaderKey is:" + strBatchHeaderKey);
				if (!YFCObject.isVoid(strBatchHeaderKey)) {
					Document docToPassToAbort = XMLUtil.createDocument(AcademyConstants.ACAD_SCAN_BATCH_HEADER);
					Element eleToPassToAbort = docToPassToAbort.getDocumentElement();
					eleToPassToAbort.setAttribute(AcademyConstants.ACAD_SCAN_BATCH_HDR_KEY, strBatchHeaderKey);
					eleToPassToAbort.setAttribute(AcademyConstants.ATTR_BATCH_SCAN_STATUS,
							AcademyConstants.VALUE_ABORTED);
					
					// calling updateInProgressBatch method to Abort the batch.
					log.verbose("Input to the updateInProgressBatch " +XMLUtil.getXMLString(docToPassToAbort));
					Document abortedBatchDoc = updateInProgressBatch(env, docToPassToAbort);
					log.verbose("Output of the updateInProgressBatch " +XMLUtil.getXMLString(abortedBatchDoc));
					/*
					 * Creating new document with StoreNo, UserID to pass to createNewBatchHeader method.
					 */
					Document docToCreateNewHeader = XMLUtil.createDocument(AcademyConstants.ACAD_SCAN_BATCH_HEADER);
					Element eleToCreateNewHeader = docToCreateNewHeader.getDocumentElement();
					eleToCreateNewHeader.setAttribute(AcademyConstants.ATTR_STORE_NO, eleOfIpDoc.getAttribute(AcademyConstants.ATTR_STORE_NO));
					eleToCreateNewHeader.setAttribute(AcademyConstants.ATTR_USER_ID, eleOfIpDoc.getAttribute(AcademyConstants.ATTR_USER_ID));
					/* calling the method to create a new Batch. */
					log.verbose("Input to the createNewBatchHeader " +XMLUtil.getXMLString(docToCreateNewHeader));
					Document docCreatedNewBatch = createNewBatchHeader(env, docToCreateNewHeader);
					log.verbose("Output of the createNewBatchHeader " +XMLUtil.getXMLString(docCreatedNewBatch));
					return docCreatedNewBatch;
				} else {
					Document invaidInputs = XMLUtil.createDocument("InvalidInputs");
					Element invalidInputEle = invaidInputs.getDocumentElement();
					invalidInputEle.setAttribute("InvalidInputs", "AcadScanBatchHeaderKey is null");
					return invaidInputs;
				} 
			} 
			// OMNI-68376 - END
			
			// OMNI-67585 and OMNI-67007 START
			else if ((AcademyConstants.VALUE_ABORTED).equalsIgnoreCase(strAction)
					|| (AcademyConstants.VALUE_COMPLETED).equalsIgnoreCase(strAction)) {
				Element eleOfIpDoc = inDoc.getDocumentElement();
				String strBatchHeaderKey = eleOfIpDoc.getAttribute(AcademyConstants.ACAD_SCAN_BATCH_HDR_KEY);
				log.verbose("Value of ScanBatchHeaderKey: " + strBatchHeaderKey);
				Document docToPassToService = XMLUtil.createDocument(AcademyConstants.ACAD_SCAN_BATCH_HEADER);
				Element eleToPassToService = docToPassToService.getDocumentElement();
				eleToPassToService.setAttribute(AcademyConstants.ACAD_SCAN_BATCH_HDR_KEY, strBatchHeaderKey);
				/*
				 * Invoking 'AcademyGetScanBatchHeaderListService' to get the value of
				 * BatchScanStatus.
				 */
				Document getBatchHdrDetailsDoc = AcademyUtil.invokeService(env,
						AcademyConstants.ACADEMY_GET_SCAN_BATCH_HDR_LIST_SERVICE, docToPassToService);
				log.verbose("Input of updateInProgressBatch is: " + XMLUtil.getXMLString(getBatchHdrDetailsDoc));
				Element eleBatchHdrDetails = (Element) getBatchHdrDetailsDoc
						.getElementsByTagName(AcademyConstants.ACAD_SCAN_BATCH_HEADER).item(0);
				String strBatchScanStatus = eleBatchHdrDetails.getAttribute(AcademyConstants.ATTR_BATCH_SCAN_STATUS);
				log.verbose("Batch Scan Status : " + strBatchScanStatus);
				if ((AcademyConstants.STR_INPROGRESS).equalsIgnoreCase(strBatchScanStatus)) {
				//calling the method to update the batch.
				Document updatedDoc = updateInProgressBatch(env,inDoc);
					log.verbose("Output of updateInProgressBatch is: " + XMLUtil.getXMLString(updatedDoc));
				return updatedDoc;
				} else {	
					return getBatchHdrDetailsDoc;
				} 
			}
			// OMNI- 67585 and OMNI-67007 END			
			else if ((AcademyConstants.VALUE_CONTINUE).equalsIgnoreCase(strAction)) {

				// Preparing InDoc for Service
				Document getACADScanBatchHeaderOutDoc = null;
				String AcadScanBatchHeaderKey = eleOfInput.getAttribute(AcademyConstants.ACAD_SCAN_BATCH_HDR_KEY);
				//Document docActiveShipmentCountInput = XMLUtil.createDocument(AcademyConstants.ACAD_SCAN_BATCH_HEADER);
				if (!YFCObject.isVoid(AcadScanBatchHeaderKey)) {
					/*Element eleAcadScanBatchHder = docActiveShipmentCountInput.getDocumentElement();
					eleAcadScanBatchHder.setAttribute(AcademyConstants.STR_STORE_NO, strStoreNo);
					eleAcadScanBatchHder.setAttribute(AcademyConstants.ACAD_SCAN_BATCH_HDR_KEY, AcadScanBatchHeaderKey);*/
					log.verbose("Input to AcademyFetchScanBatchDetailsService:: "
							+ XMLUtil.getXMLString(inDoc));
					AcademyFetchScanBatchDetails academyFetchScanBatchDetails = new AcademyFetchScanBatchDetails();
					getACADScanBatchHeaderOutDoc = academyFetchScanBatchDetails.returnBatchSummaryDetailsForContinue(env, inDoc);
					// invoking AcademyFetchScanBatchDetailsService to fetch the Active Status
					// Shipments(RFCP & PaperWorkIntiated)count
					//getACADScanBatchHeaderOutDoc = AcademyUtil.invokeService(env,
					//		AcademyConstants.SERVICE_ACAD_FETCH_SCAN_BATCH_DETAILS, docActiveShipmentCountInput);
					log.verbose("Output of AcademyFetchScanBatchDetailsService is: "
							+ XMLUtil.getXMLString(getACADScanBatchHeaderOutDoc));
					return getACADScanBatchHeaderOutDoc;
				} else {
					Document invaidInputs = XMLUtil.createDocument("InvalidInputs");
					Element invalidInputEle = invaidInputs.getDocumentElement();
					invalidInputEle.setAttribute("InvalidInputs", "AcadScanBatchHeaderKey is null");
					return invaidInputs;
				}
			}
		}
		
		/*
		 * If the Attributes in the input document are invalid below "else" condition
		 * will be invoked
		 */
		else {
			Document invaidInputs = XMLUtil.createDocument("InvalidInputs");
			Element invalidInputEle = invaidInputs.getDocumentElement();
			invalidInputEle.setAttribute("InvalidInputs", "Either UserID/StoreNo/Action is null");
			return invaidInputs;
		}
		return inDoc;

	}
	public Document updateInProgressBatch(YFSEnvironment env, Document inDoc) throws Exception{
		
		Element eleOfIpDoc = inDoc.getDocumentElement();
		/* Setting the End-Time of the Batch as Current time-stamp */
		String strDateTypeFormat = AcademyConstants.STR_DATE_TIME_PATTERN;
		SimpleDateFormat sdf = new SimpleDateFormat(strDateTypeFormat);
		Calendar cal = Calendar.getInstance();
		String strCurrentDateTime = sdf.format(cal.getTime());
		eleOfIpDoc.setAttribute(AcademyConstants.ATTR_END_TIME, strCurrentDateTime);
		/* Invoking AcademyChageScanBatchHdrService to change the 
		 * BatchScanStatus from "INPROGRESS" to "ABORTED" or "COMPLETED"
		 */
		log.verbose("Input to AcademyChangeScanBatchHeaderService " + XMLUtil.getXMLString(inDoc));
		Document updatedBatchDoc = AcademyUtil.invokeService(env,
				AcademyConstants.ACADEMY_CHANGE_SCAN_BATCH_HDR_SERVICE, inDoc);
		log.verbose("Output of AcademyChangeScanBatchHeaderService" + XMLUtil.getXMLString(updatedBatchDoc));
		return updatedBatchDoc;
	}

	/* Method for Creating a New Batch Header in DB */
	public Document createNewBatchHeader(YFSEnvironment env, Document inDoc) throws Exception {

		Document getACADScanBatchHeaderOutDoc = null;
		Document getShipmentsCountOutDoc = null;
		Element inDocEle;
		inDocEle = inDoc.getDocumentElement();
		// OMNI-69511- Start - Add user name to header table
		Document getUserListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_USER);
		getUserListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_LOGINID, strUserId);
		log.verbose("Input to getUserList API : " + XMLUtil.getXMLString(getUserListInDoc));
		env.setApiTemplate(AcademyConstants.API_GET_USER_LIST, AcademyConstants.GET_USER_LIST_TEMPLATE);
		Document getUserListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_USER_LIST, getUserListInDoc);
		env.clearApiTemplates();
		String strUserName = XPathUtil.getString(getUserListOutDoc.getDocumentElement(),
				AcademyConstants.XPATH_USER_NAME);
		log.verbose("UserName " + strUserName);
		// OMNI-69511- End
		String strDateTypeFormat = AcademyConstants.STR_DATE_TIME_PATTERN_NEW;
		SimpleDateFormat sdf = new SimpleDateFormat(strDateTypeFormat);
		Calendar cal = Calendar.getInstance();
		String strCurrentDateTime = sdf.format(cal.getTime());
		log.verbose("Calculated Current Date-Time : " + strCurrentDateTime);
		String strConcatStoreAndDateTime = strStoreNo + strCurrentDateTime;
		log.verbose("Concatenated Store and Date-Time : " + strConcatStoreAndDateTime);

		inDocEle.setAttribute("BatchID", strConcatStoreAndDateTime);
		// inDocEle.setAttribute("EndTime", AcademyConstants.STR_END_TIME_FUTURE_DATE);
		inDocEle.setAttribute("UserID", strUserId);
		inDocEle.setAttribute("UserName", strUserName);

		// invoking AcademyScanBatchHdrCreateService to insert record in to
		// ACAD_SCAN_BATCH_HEADER table
		getACADScanBatchHeaderOutDoc = AcademyUtil.invokeService(env, "AcademyScanBatchHdrCreateService", inDoc);
		log.verbose("AcademyManageScanBatch.java:createNewBatchHeader():OutDoc "
				+ XMLUtil.getXMLString(getACADScanBatchHeaderOutDoc));

		// invoking AcademyStoreActiveShipmentStatusCountService to fetch the Active
		// Status Shipments(RFCP & PaperWorkIntiated)count
		getShipmentsCountOutDoc = AcademyUtil.invokeService(env, "AcademyStoreActiveShipmentStatusCountService", inDoc);
		String strActiveShipmentsCount = XPathUtil.getString(getShipmentsCountOutDoc,
				AcademyConstants.XPATH_SHIPMENTS_COUNT);
		log.verbose("Staged Shipments Count is : " + strActiveShipmentsCount);
		Element eleACADScanBatchHdr = getACADScanBatchHeaderOutDoc.getDocumentElement();
		eleACADScanBatchHdr.setAttribute(AcademyConstants.STR_STAGED_SHIPMENTS_COUNT, strActiveShipmentsCount);
		log.verbose("AcademyStoreActiveShipmentStatusCountService.java::OutDoc "
				+ XMLUtil.getXMLString(getShipmentsCountOutDoc));

		return getACADScanBatchHeaderOutDoc;
	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}

}