package com.academy.ecommerce.sterling.sts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.academy.ecommerce.sterling.bopis.api.AcademySearchShipmenNoOrderNoFromWSC;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.shared.ycp.YFSContext;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantriks.yih.adapter.util.YantriksCommonUtil;
import com.yantriks.yih.adapter.util.YantriksConstants;


/**************************************************************************
 * File Name		: AcademySTSReceiveTOContainer
 *
 * Description	    : OMNI-6581. The method fetches the order details to 
 * validate the container  if not received, scanned already, belongs to TO 
 * order
 * Makes entry to Custom table ACAD_STS_TO_CONTAINERS.
 * Posts a message to a queue, to proceed further with order fulfillment.
 * 
 * Input xml to the custom table
 * <AcadSTSTOContainer BatchNo="1000000025" ClosedFlag="N" ContainerNo="134800004"
 *  MaximumRecords="5000" ShipmentContainerKey="202006020613312160698334"
 *  ShipmentKey="202006020609222160697949" Status="New" StoreNo="134" TOOrderNo="Y159767989" Createuserid=""/>
 * --------------------------------
 * 	Date             Author               
 * --------------------------------
 *  5-Jun-2020      Cognizant			 	 
 * 
 * -------------------------------------------------------------------------
 **************************************************************************/
public class AcademySTSReceiveTOContainer implements YIFCustomApi {

	/**
	 * log variable.
	 */
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSReceiveTOContainer.class);

	public Document createSTSContainerEntry(YFSEnvironment env, Document inXML) throws ParserConfigurationException{
		log.beginTimer("====AcademySTSReceiveTOContainer====");
		log.verbose("Start Inside AcademySTSReceiveTOContainer.createSTSContainerEntry()");

		//Declarations
		Document inDocGetContainerList = null;				
		Document getContEntryDoc = null;
		Element eleContainer,eleOrderBy, eleAttribute = null;

		if (!YFCObject.isVoid(inXML.getDocumentElement())) 
		{
			log.verbose("Input xml:: " + XMLUtil.getXMLString(inXML));
			String inContainerNo = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_CONTAINER_NO);

			try {
				inDocGetContainerList = XMLUtil.createDocument(AcademyConstants.ELE_CONTAINER);

				if(!YFSObject.isVoid(inContainerNo)){

					eleContainer = inDocGetContainerList.getDocumentElement();				
					eleContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_NO, inContainerNo);
					//handle duplicate containerno
					eleOrderBy = XmlUtils.createChild(eleContainer, AcademyConstants.ELE_ORDERBY);
					eleAttribute = XmlUtils.createChild(eleOrderBy, AcademyConstants.ELE_ATTRIBUTE);
					eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_SHIP_CONT_KEY);
					eleAttribute.setAttribute(AcademyConstants.ATTR_DESC_SHORT, AcademyConstants.ATTR_Y);
					log.verbose("Document for getShipmentContainerList:: " + XMLUtil.getXMLString(inDocGetContainerList));
					//Fetch the order details for the container and insert into custom table
					getContEntryDoc = validateContainersOrderNo(env, inDocGetContainerList, inXML);

				}else {
					throw new YFSException("Container ID field cannot be blank. Scan a valid Container ID.");
				}
			} catch (Exception e) {

				log.error("Exception Occured in fetching the container"
						+ e.getMessage());
				throw new YFSException(new YFCException(e).getXMLErrorBuf());
			}

		}
		log.verbose("End of AcademySTSReceiveTOContainer.createSTSContainerEntry()");
		log.endTimer("====AcademySTSReceiveTOContainer====");
		return getContEntryDoc;

	}
	/**
	 * The method invokes getShipmentContainerList to fetch the TO Order No of the
	 * ContainerNo entered in SOM UI screen.
	 * Form input to make DB entry into the custom table.
	 * @param env
	 * @param inDocGetContainerList
	 * @throws ParserConfigurationException
	 */
	private Document validateContainersOrderNo(YFSEnvironment env, Document inDocGetContainerList, Document inXML)
			throws ParserConfigurationException {
		log.verbose("Start Inside AcademySTSReceiveTOContainer.validateContainersOrderNo()");
		Document docCreateContainerEntryOut = null;		
		Document docContainerList = null;
		Document docContainerListOut = null;
		Document docAcadCreateSTSContainer = null;
		Element eleSTSContainers = null;
		Element eleOrderBy = null;
		Element eleAttribute =  null;
		String sIsReceived, sBatchClosedFlag, sNewBatchNo, sSTSCreatets = "";

		Element eleAcadCreateSTSContainer=null;
		String sStoreNo = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_STORE_NO);
		String sCreateuserid = inXML.getDocumentElement().getAttribute("Createuserid");
		
		String sStatus = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_STATUS);
		String sContainerNo = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_CONTAINER_NO);
		String sBatchNo= inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_BATCHNO);
		Document docGetShipmentContListOut = null;
		log.verbose("***getShipmentContainerList input***" +XMLUtil.getXMLString(inDocGetContainerList));
		AcademySearchShipmenNoOrderNoFromWSC fetchSTSValidationDate = new AcademySearchShipmenNoOrderNoFromWSC();

		try {
          // handling duplicate containers 
		  // OMNI-10249  STS - Container ID Belongs To a Different Store - START
			Element eleContainerShipment = XmlUtils.createChild(inDocGetContainerList.getDocumentElement(), AcademyConstants.ELE_SHIPMENT);
			eleContainerShipment.setAttribute(AcademyConstants.ATTR_RECV_NODE, sStoreNo);
			eleContainerShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.STR_SHIP_TO_STORE);
			log.verbose("***getShipmentContainerList input***" +XMLUtil.getXMLString(inDocGetContainerList));
			// OMNI-10249  STS - Container ID Belongs To a Different Store - END
			docGetShipmentContListOut = AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACAD_GETSHIPMENT_CONT_LIST, inDocGetContainerList);
			log.verbose("***getShipmentContainerList output***" +XMLUtil.getXMLString(docGetShipmentContListOut));

			Element eleShipContListOut = docGetShipmentContListOut.getDocumentElement();
			Element eleContainer = (Element) XPathUtil.getNode(eleShipContListOut, "Container[@ContainerNo='"+sContainerNo+"']");
			sIsReceived = eleContainer.getAttribute(AcademyConstants.ATTR_IS_RECEIVED);		

			log.verbose("***Container element***" +XMLUtil.getElementXMLString(eleContainer));
			docAcadCreateSTSContainer = XmlUtils.createDocument(AcademyConstants.ELE_ACAD_STS_TO_CONTAINERS);
			eleAcadCreateSTSContainer = docAcadCreateSTSContainer.getDocumentElement();

			if((!YFSObject.isVoid(eleContainer)) && (sIsReceived.equals(AcademyConstants.STR_NO))){
				log.info("***Container exists***");
				String sContainerKey = eleContainer.getAttribute(AcademyConstants.ATTR_SHIP_CONT_KEY);
				Element eleShipment = XmlUtils.getChildElement(eleContainer, AcademyConstants.ELE_SHIPMENT);
				String sTOOrderNo = eleShipment.getAttribute(AcademyConstants.ATTR_ORDER_NO);
				String sShpStatus = eleShipment.getAttribute(AcademyConstants.ATTR_STATUS);
				String sShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
				//OMNI-47426 - changes to mark shipment as shipped during receiving if the shipment was not marked shipped initially - Start
				String sShipNode = eleShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
				boolean bnodetypestore=false;
				bnodetypestore=AcademyUtil.checkNodeTypeIsStore(env, sShipNode);
				//OMNI-47426 - changes to mark shipment as shipped during receiving if the shipment was not marked shipped initially - End
				log.verbose("sTOOrderNo::" + sTOOrderNo);
				//Check if container belongs to a valid TO
				if(!YFSObject.isVoid(sTOOrderNo)){

					eleAcadCreateSTSContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_NO, sContainerNo);
					eleAcadCreateSTSContainer.setAttribute(AcademyConstants.ATTR_STORE_NO, sStoreNo);				
					eleAcadCreateSTSContainer.setAttribute(AcademyConstants.ATTR_TO_ORDERNO, sTOOrderNo);
					eleAcadCreateSTSContainer.setAttribute(AcademyConstants.ATTR_STATUS, "New");
					eleAcadCreateSTSContainer.setAttribute(AcademyConstants.ATTR_CLOSED_FLAG, AcademyConstants.STR_NO);							
					eleAcadCreateSTSContainer.setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY, sContainerKey);
					eleAcadCreateSTSContainer.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, sShipmentKey);
					log.verbose("***docAcadCreateSTSContainer ***" +XMLUtil.getXMLString(docAcadCreateSTSContainer));
					//BatchNo From UI - not empty, set the old batchno and insert record for N
					if(!YFSObject.isVoid(sBatchNo)){
						eleAcadCreateSTSContainer.setAttribute(AcademyConstants.ATTR_BATCHNO, sBatchNo);
					}else {									
						// Fetch the latest max BatchNo
						//the timestamp value is sysdate - 45days
						sSTSCreatets = fetchSTSValidationDate.getDefaultCreatetsDate();
						
						docContainerList = XmlUtils.createDocument(AcademyConstants.ELE_ACAD_STS_TO_CONTAINERS);
						eleSTSContainers = docContainerList.getDocumentElement();
						eleSTSContainers.setAttribute(AcademyConstants.ATTR_CREATETS, sSTSCreatets);
						eleSTSContainers.setAttribute(AcademyConstants.ATTR_CREATETS_QRY_TYPE, AcademyConstants.STR_GREATER_THAN_OR_EQUALS);
						//Fix as part OMNI-9405
						eleSTSContainers.setAttribute(AcademyConstants.ATTR_STORE_NO, sStoreNo);
						eleSTSContainers.setAttribute("Createuserid", sCreateuserid);
						//Fix as part OMNI-9405
						
						eleOrderBy = XmlUtils.createChild(eleSTSContainers, AcademyConstants.ELE_ORDERBY);
						eleAttribute = XmlUtils.createChild(eleOrderBy, AcademyConstants.ELE_ATTRIBUTE);
						eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_BATCHNO);
						eleAttribute.setAttribute(AcademyConstants.ATTR_DESC_SHORT, AcademyConstants.ATTR_Y);

						docContainerListOut = AcademyUtil.invokeService(env,AcademyConstants.API_GET_STS_CONTAINER_LIST, docContainerList);
						log.verbose("***Max batch no fetched::***" +XMLUtil.getXMLString(docContainerListOut));

						Element eleContainerList = docContainerListOut.getDocumentElement();
						sBatchNo = XPathUtil.getString(eleContainerList, "AcadSTSTOContainers/@BatchNo");						
						log.verbose("***Max Batch No***" + sBatchNo);
												
						//First time - if BatchNo is empty string - no batch - create a batch no
						if(YFSObject.isVoid(sBatchNo)) {
							//Fetch nextseq as batchno
							sBatchNo = getNextDBSeqno(env, null);

							eleAcadCreateSTSContainer.setAttribute(AcademyConstants.ATTR_BATCHNO, sBatchNo);

						}else{
																				
							//fetch the list of containers belonging to the batch.
//							docContainerList = XmlUtils.createDocument(AcademyConstants.ELE_ACAD_STS_TO_CONTAINERS);
//							docContainerList.getDocumentElement().setAttribute(AcademyConstants.ATTR_BATCHNO, sBatchNo);
//							docContainerListOut = AcademyUtil.invokeService(env, AcademyConstants.API_GET_STS_CONTAINER_LIST, docContainerList);
//							log.verbose("***List of containers feched for the max batchno::***" +XMLUtil.getXMLString(docContainerListOut));
//
//							eleContainerList = docContainerListOut.getDocumentElement();
							sBatchClosedFlag = XPathUtil.getString(eleContainerList, "AcadSTSTOContainers[@BatchNo='"+sBatchNo+"']/@ClosedFlag");
							log.verbose("***Max Batch Closed Flag***" + sBatchClosedFlag);
							
							//if closedflag is Y, fetch a new batchno to insert a record with flag N.
							if(AcademyConstants.ATTR_Y.equals(sBatchClosedFlag)) {
								sNewBatchNo = getNextDBSeqno(env, null);
								eleAcadCreateSTSContainer.setAttribute(AcademyConstants.ATTR_BATCHNO, sNewBatchNo);
							}else {
								//if closedflag is N, max batchno inserted with flag N.
								eleAcadCreateSTSContainer.setAttribute(AcademyConstants.ATTR_BATCHNO, sBatchNo);
							}
							

						}
					}

					log.verbose("***docAcadCreateSTSContainer input***" +XMLUtil.getXMLString(docAcadCreateSTSContainer));

					docCreateContainerEntryOut = AcademyUtil.invokeService(env, AcademyConstants.API_CREATE_STS_CONTAINER, docAcadCreateSTSContainer);
					log.verbose("***docAcadCreateSTSContainer output***" +XMLUtil.getXMLString(docCreateContainerEntryOut));					

					//Update Status to Received in custom table before TO Ship confirmation
					updateTOStatusInCustomTable(env, sContainerNo);

					//confirm TO Shipment - only if shipment is not confirmed by exeter
					//OMNI-47426 - changes to mark shipment as shipped during receiving if the shipment was not marked shipped initially - Start
					if((!YFSObject.isVoid(sShpStatus)) && sShpStatus.equals("1100.70.06.30")) {
						if(!YFSObject.isVoid(bnodetypestore)&& bnodetypestore==true)
							{
								confirmTOShipmentWithManifest(env, sShipmentKey);
							}
							else {	
								confirmTOShipment(env, sShipmentKey);
							}
						}
					//OMNI-47426 - changes to mark shipment as shipped during receiving if the shipment was not marked shipped initially - end

					
					/*OMNI-46123: Start Change : Invoking the method to set transaction objects for posting supply , demand updates for arrived at store
					 *  if Arrived At Store feed is not received from SIM system . This is an exception scenario*/
					String exceptionAISAtReceiveIsEnabled=YFSSystem.getProperty(AcademyConstants.EXCEPTION_AIS_AT_RECEIVE);
					//For controlling turn ON/OFF of the logic handled for exception scenario on STS Arrived At store
					log.verbose("exceptionAISAtReceiveIsEnabled: "+exceptionAISAtReceiveIsEnabled);
					if(!YFCObject.isVoid(exceptionAISAtReceiveIsEnabled) && AcademyConstants.STR_YES.equals(exceptionAISAtReceiveIsEnabled)) {
						postSupplyAndDemandUpdateToYantriks(eleContainer,env);
					}
					/*OMNI-46123: End Change */

					//Service invoked to complete TO receipt and and drop message to queue to resolve the hold on SO
					AcademyUtil.invokeService(env,AcademyConstants.SERVICE_PROCESS_CONTAINER_TO_RECEIVE, docAcadCreateSTSContainer);

				}
				else {

					YFSException exception = new YFSException("Invalid Container ID. Scan a valid Container ID.");
					exception.setErrorCode("Invalid Container ID. Scan a valid Container ID.");
					throw exception;
				}
			}
			else {
				YFSException exception = new YFSException("Invalid Container ID. Scan a valid Container ID.");
				exception.setErrorCode("Invalid Container ID. Scan a valid Container ID.");
				throw exception;
			}
		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.verbose("End Inside AcademySTSReceiveTOContainer.validateContainersOrderNo()");
		return docCreateContainerEntryOut;
	}


	/*OMNI-46123: Start Change - Method to handle the demand update to Yantriks when container is received in SOM 
	 * even before the arrived At store feed is consumed */
	private void postSupplyAndDemandUpdateToYantriks(Element eleContainer,YFSEnvironment env) throws Exception {
		log.beginTimer("postSupplyAndDemandUpdateToYantriks");
		log.verbose("Start Inside AcademySTSReceiveTOContainer.postSupplyAndDemandUpdateToYantriks()");
		String containerNo=eleContainer.getAttribute(AcademyConstants.ATTR_CONTAINER_NO);
		Element eleShipment = XmlUtils.getChildElement(eleContainer, AcademyConstants.ELE_SHIPMENT);
		String sTOOrderHeaderKey = eleShipment.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
		String shipmentKey=eleShipment.getAttribute(AcademyConstants.SHIPMENT_KEY);
		log.verbose("sTOOrderHeaderKey::" + sTOOrderHeaderKey);
		Element eleContainerDetails = SCXmlUtil.getChildElement(eleContainer, AcademyConstants.ELE_CONTAINER_DTLS);
		NodeList nlContainerDetail = eleContainerDetails.getElementsByTagName(AcademyConstants.CONTAINER_DETAIL);
		List<String> orderLineKeyList = new ArrayList<String>();
		for (int i = 0; i < nlContainerDetail.getLength(); i++) {
			Element eleContainerDetail = (Element) nlContainerDetail.item(i);
			log.verbose("eleContainerDetail::" + SCXmlUtil.getString(eleContainerDetail));
			String containerOrderLineKey = XPathUtil.getString(eleContainerDetail,AcademyConstants.XPATH_CONTAINER_ORDRLINEKEY);
			log.verbose("containerOrderLineKey::" + containerOrderLineKey);
			orderLineKeyList.add(containerOrderLineKey);
		}
		
		log.verbose("orderLineKeyList:"+orderLineKeyList);
		
		
		Document getOrderListIP = SCXmlUtil.createDocument(AcademyConstants.ELE_ORDER);
		getOrderListIP.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,sTOOrderHeaderKey);
		log.verbose("Input to getOrderList :: " + SCXmlUtil.getString(getOrderListIP));
		/*GetOrderList call is done for the Transfer Order No*/
		Document outGetOrderList = YantriksCommonUtil.invokeAPI(env, AcademyConstants.TEMP_RECONCILE_GET_ORDER_LIST,AcademyConstants.API_GET_ORDER_LIST, getOrderListIP);
		log.verbose("Output of getOrderList :: " + SCXmlUtil.getString(outGetOrderList));
		Element eleOrder = SCXmlUtil.getChildElement(outGetOrderList.getDocumentElement(), AcademyConstants.ELE_ORDER);
		Element eleOrderLines = SCXmlUtil.getChildElement(eleOrder, AcademyConstants.ELE_ORDER_LINES);
		NodeList nlOrderLines = eleOrderLines.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
		int orderLineLength = nlOrderLines.getLength();
		for (int j = 0; j < orderLineLength; j++) {
			Element currInOrderLine = (Element) nlOrderLines.item(j);
			String orderLineKey=currInOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);		
			if(orderLineKeyList.contains(orderLineKey)) {
				log.verbose("orderLine is present in the container::" + orderLineKey);
				Element eleOrderStatuses = SCXmlUtil.getChildElement(currInOrderLine, AcademyConstants.ELE_ORDER_STATUSES);
				NodeList nlOrderStatus = eleOrderStatuses.getElementsByTagName(AcademyConstants.ELE_ORDER_STATUS);
				int orderStatusesLength = nlOrderStatus.getLength();
				for (int k = 0; k < orderStatusesLength; k++) {
					Element currOrderStatus = (Element) nlOrderStatus.item(k);
					String status = currOrderStatus.getAttribute(AcademyConstants.ATTR_STATUS);
					log.verbose("OrderLine status is:"+status);
					if(!YFCObject.isVoid(status) && status.equals(AcademyConstants.V_STATUS_3700_200)) { /*These txn objects are read in AcademyPostKafkaUpdateToQueue.class on ORDER_CHANGE.ON_ORDER_RELEASE_STATUS_CHANGE event*/
						log.verbose("Setting Transaction objects if status is Shipped To Store");
						env.setTxnObject(AcademyConstants.ATTR_STATUS, AcademyConstants.STR_RECEIVED);
						env.setTxnObject(AcademyConstants.ATTR_SHIPMENT_KEY,shipmentKey);
						env.setTxnObject(AcademyConstants.ATTR_CONTAINER_NO,containerNo);
						log.verbose("shipmentKey::" + shipmentKey);
						log.verbose("containerNo::" + containerNo);
						// OMNI-53346, Change start
						String sShipNode = currInOrderLine.getAttribute("ShipNode");
						log.verbose("ShipNode::" + sShipNode);
						String nodeType="";
						if (!YFCObject.isVoid(sShipNode)) 
						nodeType=YantriksCommonUtil.getShipNodeType(env,sShipNode);
						if(nodeType.equals(YantriksConstants.AT_LT_STORE)) {
							log.verbose("STS2.0 line");
							env.setTxnObject("STS2.0_RECEIVED", "Y");
						}
						//OMNI-53346, Change End
						log.verbose("Txn Objects set successfully");
					}
					
				}
			}
			
			
			
			
		}
		log.endTimer("postSupplyAndDemandUpdateToYantriks");
	}
	/*OMNI-46123: End Change */
	/**
	 * Method invoke confirmShipment API
	 * @param env
	 * @param sShipmentKey
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError
	 */
	public void confirmTOShipment(YFSEnvironment env, String sShipmentKey)
			throws ParserConfigurationException, FactoryConfigurationError{
		Document docShipment;
		docShipment = XmlUtils.createDocument(AcademyConstants.ELE_SHIPMENT);
		docShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, sShipmentKey);
		docShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_ACTION, "Confirm");

		log.verbose("ConfirmShipment input" + XMLUtil.getXMLString(docShipment));
		try {
			AcademyUtil.invokeAPI(env,AcademyConstants.CONFIRM_SHIPMENT_API, docShipment);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.verbose("****Shipment confirmed succesfully****");
	}
	
	/**
	 * Method invoke confirmShipment API for STS2.0 Shipments which are not shipped but store associate is trying to receive
	 * @param env
	 * @param sShipmentKey
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError
	 */
	public void confirmTOShipmentWithManifest(YFSEnvironment env, String sShipmentKey)
			throws ParserConfigurationException, FactoryConfigurationError{
		Document docShipment;
		docShipment = XmlUtils.createDocument(AcademyConstants.ELE_SHIPMENT);
		docShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, sShipmentKey);
		docShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_ACTION, "Confirm");
		docShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_DO_INVENTORY_UPDATES_OFFSLINE, AcademyConstants.STR_NO);
		docShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_MANIFEST_BEING_CLOSED, AcademyConstants.STR_YES);
		docShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIP_COMPLETE, AcademyConstants.STR_YES);

		log.verbose("ConfirmShipment input" + XMLUtil.getXMLString(docShipment));
		try {
			AcademyUtil.invokeAPI(env,AcademyConstants.CONFIRM_SHIPMENT_API, docShipment);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.verbose("****Shipment confirmed succesfully****");
	}
	/**
	 * Method updates Status to Received in the custom table
	 * @param env
	 * @param inDocGetContainerList
	 * @param sContainerNo
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError
	 * @throws Exception
	 * 
	 */
	private void updateTOStatusInCustomTable(YFSEnvironment env, String sContainerNo)
			throws ParserConfigurationException, FactoryConfigurationError, Exception {
		Document docContainer;
		docContainer = XmlUtils.createDocument(AcademyConstants.ELE_ACAD_STS_TO_CONTAINERS);
		docContainer.getDocumentElement().setAttribute(AcademyConstants.ATTR_CONTAINER_NO, sContainerNo);
		docContainer.getDocumentElement().setAttribute(AcademyConstants.ATTR_STATUS, AcademyConstants.STR_RECEIVED);
		log.verbose("****Custom table Status update****" + XmlUtils.getString(docContainer));
		AcademyUtil.invokeService(env,AcademyConstants.API_CHANGE_STS_CONTAINER, docContainer);
		log.verbose("****Status in custom table updated to received****");
	}



	/**
	 * Method getNextDBSeqno:: This is a method which reads the
	 * seq_inv_snap_shot sequence and returns the same.
	 * 
	 * @param YFSEnvironment
	 * @param String
	 * @return long seqNo
	 */
	public static String getNextDBSeqno(YFSEnvironment env, Document inSeqDoc)
	{

		// Get the Database Sequence Number from the ACAD_BATCH_NO_SEQ sequence
		long seqNo = ((YFSContext) env).getNextDBSeqNo("ACAD_BATCH_NO_SEQ");
		log.verbose("***seqNo output***" + seqNo);

		String stringSeqNo = String.valueOf(seqNo);

		// Return the Database Sequence Number
		return stringSeqNo; 

	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}


}