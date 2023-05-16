package com.academy.ecommerce.sterling.api;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyGCXMLSplitter {

	public AcademyGCXMLSplitter() {
	}

	public void setProperties(Properties props) {
		this.props = props;
	}

	private Properties props;

	private static Logger log = Logger.getLogger(AcademyGCXMLSplitter.class
			.getName());
	private static final YFCLogCategory loger = YFCLogCategory.instance(AcademyGCXMLSplitter.class);

	private String shipKey = "", shipNo = "",
			shipLineKey = "", shipStatus, orderNo = "", requestAmount = "", strGCReprocessRetryInterval = "";

	public Document splitGCXMLAndExecuteService(YFSEnvironment env,
			Document inDoc) throws Exception {		
		loger.beginTimer(" Begining of AcademyGCXMLSplitter  splitGCXMLAndExecuteService() Api :inDoc :: "
				+XMLUtil.getXMLString(inDoc));
		String sChildXMLName = props.getProperty("ChildXML");
		String sServiceName = props.getProperty("ServiceName");//Service name is changed to AcademyInvokeRTSHTTPService	
		strGCReprocessRetryInterval = props.getProperty("gcReprocessRetryInterval");		
		log.verbose("strGCReprocessRetryInterval : "+strGCReprocessRetryInterval);
		

		//Commenting BULK GC ACTIVATION logic as part of GCD-47
		/*
		String sAPIName = props.getProperty("APIName");
		boolean bIsAPI = !YFCObject.isVoid(sAPIName);
		
		YFCDocument inYFCDocument = YFCDocument.getDocumentFor(inDoc);
		YFCElement inYFCElement = inYFCDocument.getDocumentElement();
		YFCNodeList nl = inYFCElement.getElementsByTagName(sChildXMLName);
		int iNodeListLength = nl.getLength();
		Map<Integer, Document> hmap = new HashMap();
		int count = 0;
		for (int i = 0; i < iNodeListLength; i++) {
			YFCElement eChildElem = (YFCElement) nl.item(i);
			YFCElement newElem = (YFCElement) inYFCElement
					.removeChild(eChildElem);
			YFCDocument childDoc = YFCDocument.parse(eChildElem.getString());
			shipKey = ((Element) childDoc.getDocument().getElementsByTagName(
					AcademyConstants.STR_GCLOAD_REQ).item(0))
					.getAttribute(AcademyConstants.SHIPMENT_KEY);
			shipNo = ((Element) childDoc.getDocument().getElementsByTagName(
					AcademyConstants.STR_GCLOAD_REQ).item(0))
					.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			shipLineKey = ((Element) childDoc.getDocument()
					.getElementsByTagName(AcademyConstants.STR_GCLOAD_REQ)
					.item(0))
					.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
			shipStatus = ((Element) childDoc.getDocument()
					.getElementsByTagName(AcademyConstants.STR_GCLOAD_REQ)
					.item(0)).getAttribute(AcademyConstants.ATTR_STATUS);
			checkIsBulkGC = ((Element) childDoc.getDocument()
					.getElementsByTagName(AcademyConstants.STR_GCLOAD_REQ)
					.item(0)).getAttribute("checkIsBulkGC");
			((Element) childDoc.getDocument().getElementsByTagName(
					AcademyConstants.STR_GCLOAD_REQ).item(i))
					.removeAttribute(AcademyConstants.SHIPMENT_KEY);
			((Element) childDoc.getDocument().getElementsByTagName(
					AcademyConstants.STR_GCLOAD_REQ).item(i))
					.removeAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			((Element) childDoc.getDocument().getElementsByTagName(
					AcademyConstants.STR_GCLOAD_REQ).item(i))
					.removeAttribute("checkIsBulkGC");
			((Element) childDoc.getDocument().getElementsByTagName(
					AcademyConstants.STR_GCLOAD_REQ).item(i))
					.removeAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);

			i--;
			iNodeListLength--;

			if (bIsAPI) {
				log.verbose((new StringBuilder("Invoking API :")).append(
						sAPIName).toString());
				AcademyUtil.invokeAPI(env, sAPIName, childDoc.getDocument());
			} else {
				log.verbose((new StringBuilder("Invoking Service :")).append(
						sServiceName).toString());
				Document doc = AcademyUtil.invokeService(env, sServiceName,
						childDoc.getDocument());
				hmap.put(new Integer(count), doc);
				++count;

			}
		}*/

		Element eleRoot = inDoc.getDocumentElement();
		NodeList nl = eleRoot.getElementsByTagName(sChildXMLName);
		int iNodeListLength = nl.getLength();
		Map<Integer, Document> hmap = new HashMap();
		int count = 0;		
		
		for (int i = 0; i < iNodeListLength; i++) {			
			Element eleChild = (Element) nl.item(i);
			// START GCD-47
			// Reading attributes for changeShipment API call input called after activation.
			shipKey = eleChild.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			shipNo = eleChild.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			shipLineKey = eleChild.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
			shipStatus = eleChild.getAttribute(AcademyConstants.ATTR_STATUS);
			orderNo = eleChild.getAttribute(AcademyConstants.ATTR_ORDER_NO); 
			requestAmount = eleChild.getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT);
			// Commenting bulk and non bulk GC activation related attributes as
			// only Non-bulk activation calls invoke this class.
			// Earlier COM application was invoking this class for both bulk and
			// non-bulk activation.
			// checkIsBulkGC = eleChild.getAttribute("checkIsBulkGC");
			log.verbose("shipKey:: "+shipKey+" shipNo::"+shipNo+" shipLineKey::"+shipLineKey+
					" shipStatus::"+shipStatus+"OrderNo::"+orderNo+"requestAmount::"+requestAmount);
			
			Document inDocToRTS = XMLUtil.getDocumentForElement(eleChild);
			log.verbose("Invoking NonBulkGCActivationService : "+sServiceName);
			log.verbose("Invoking NonBulkGCActivationService with inDocToRTS: "+XMLUtil.getXMLString(inDocToRTS));
			
			//Invoke RTS service
			Document doc = AcademyUtil.invokeService(env, sServiceName,inDocToRTS);
			log.verbose("NonBulkGCActivationService output :: "+XMLUtil.getXMLString(doc));
			hmap.put(new Integer(count), doc);
			count++;
			//END GCD:47
		}
		
		dealGCActivationResponse(env, hmap, inDoc);

		loger.endTimer(" End of AcademyGCXMLSplitter  splitGCXMLAndExecuteService() Api");
		return inDoc;
	}
	
	/**
	 * Reads the RTS response XML and calls changeShipment API to update extn
	 * attribute "ExtnNonBulkGCActStatus" at Shipment line level. ChangeShipment
	 * is invoked both in activation success and failure scenarios. In case of
	 * failure an alert is raised of type
	 * "NON_BULK_GC_ACTIVATION_PROCESS_FAILED".
	 * 
	 * @param env
	 * @param hmap
	 * @param inDoc
	 * @throws Exception
	 */
	void dealGCActivationResponse(YFSEnvironment env,
			Map<Integer, Document> hmap, Document inDoc) throws Exception {
		String actvationResult, gcCardNo;
		
		loger.beginTimer(" Begining of AcademyGCXMLSplitter  dealGCActivationResponse() Api");
		Set<Map.Entry<Integer, Document>> set = hmap.entrySet();
		Document rtsResXML = null;
		Element gcResEle = null, gcReqEle = null;
		Iterator itr = set.iterator();
		//Start GCD-255 GCD-256 GCD-257
		Document outGetShipmentDetails = null;
		NodeList shipmentTagSerialNodeList = null;		
		String gcActivationAuth = null;
		String gcAmount = null;
		double dGCAmount = 0.00;
		Element eleShipmentTagSerial = null;
		//End GCD-255 GCD-256 GCD-257
		//Commenting as part of GCD-47
		/*String gcFailType = "", gcResFault = null, errorDesc;
		StringBuffer sbr = new StringBuffer();
		boolean isActivationFailed = false;*/
		//Start	GCD-255 GCD-256 GCD-257
		Document gtDTL_Shipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		
		Element elgt_Shipment = gtDTL_Shipment.getDocumentElement();
		elgt_Shipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,this.shipKey);
		elgt_Shipment.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,AcademyConstants.ENTERPRISE_CODE_SHIPMENT);
		elgt_Shipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO,this.shipNo);
		
		Document outputTemplate = XMLUtil
				.getDocument("<Shipment EnterpriseCode=\"\"  ShipmentKey=\"\"  ShipmentNo=\"\">"
						+ "<Containers>"
						+ "<Container ContainerNo=\"\"  ContainerScm=\"\"  ContainerSeqNo=\"\"  ContainerType=\"\"  ShipmentContainerKey=\"\">"
						+ "<ContainerDetails>"
						+ "<ContainerDetail ContainerDetailsKey=\"\"  EnterpriseKey=\"\"  ItemID=\"\"  ProductClass=\"\"  ShipmentContainerKey=\"\"  ShipmentKey=\"\"  ShipmentLineKey=\"\">"
						+ "<ShipmentTagSerials>"
						+ "<ShipmentTagSerial  SerialNo=\"\"  LotAttribute1=\"\"  LotAttribute2=\"\"  ShipmentLineKey=\"\"  ShipmentTagSerialKey=\"\"/>"
						+ "</ShipmentTagSerials>"
						+ "</ContainerDetail>"
						+ "</ContainerDetails>"
						+ "</Container>"
						+ "</Containers>" 
						+ "</Shipment>");		
	
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_DETAILS,outputTemplate);
		outGetShipmentDetails = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_SHIPMENT_DETAILS,gtDTL_Shipment);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_DETAILS);
		log.verbose("ShipmentDetails ->getShipmentDetails OutputXML :: " +XMLUtil.getXMLString(outGetShipmentDetails));
		//End GCD-255 GCD-256 GCD-257
		int i = 0;
		while (itr.hasNext()) {
			
			//Commenting BULK GC ACTIVATION logic as part of GCD-47
			/*Document soapResXML = ((Map.Entry<Integer, Document>) itr.next())
					.getValue();
			i++;
			Element gcResEle = (Element) soapResXML.getElementsByTagName(
					"GiftCardLoadResponse").item(0);
			Element gcResFaultEle = (Element) soapResXML.getElementsByTagName(
					"soapenv:Fault").item(0);

			if (gcResEle != null) {

				actvationResult = gcResEle
						.getAttribute(AcademyConstants.STR_ACTIVATION_RES);
				gcCardNo = gcResEle
						.getAttribute(AcademyConstants.STR_GC_START_NO);

				errorDesc = gcResEle
						.getAttribute(AcademyConstants.STR_GC_ERROR);
				if (!actvationResult.equalsIgnoreCase(AcademyConstants.STR_YES)) {

					errorDesc = gcResEle.getAttribute("ErrorDescription");

					sbr = sbr.append(gcCardNo);
					sbr = sbr.append("  ");
					sbr = sbr.append(errorDesc);
					sbr = sbr.append(" , ");
					isActivationFailed = true;
				}

			}
			if (gcResFaultEle != null) {
				isSoapFault = true;
			}
		}*/
			
			//START GCD:47
			rtsResXML = (Document) ((java.util.Map.Entry) itr.next()).getValue();
			i++;
			gcReqEle = (Element) rtsResXML.getElementsByTagName("XMLAJBFipayRequest").item(0);
			log.verbose("dealGCActivationResponse->gcReqEle XMLAJBFipayRequest :: " +XMLUtil.getXMLString(XMLUtil.getDocumentForElement(gcReqEle)));
			gcResEle = (Element) rtsResXML.getElementsByTagName("XMLAJBFipayResponse").item(0);
			log.verbose("dealGCActivationResponse->gcResEle XMLAJBFipayResponse :: " +XMLUtil.getXMLString(XMLUtil.getDocumentForElement(gcResEle)));
			
			if (gcResEle != null) {
				actvationResult = gcResEle.getElementsByTagName(AcademyConstants.ATTR_RTS_ACTION_CODE).item(0).getTextContent();
				gcCardNo = gcResEle.getElementsByTagName(AcademyConstants.ATTR_RTS_ACCOUNT).item(0).getTextContent();
							
				//String gcResFault = gcResEle.getElementsByTagName("IxIsoResp").item(0).getTextContent();//Error Code(Wait for RTS)
				//gcResFault = gcResEle.getElementsByTagName("IxReceiptDisplay").item(0).getTextContent();								
				log.verbose("actvationResult : IxActionCode ::  " +actvationResult);
				log.verbose("gcCardNo : IxAccount ::  " +gcCardNo);
				//log.verbose("gcResFault : IxReceiptDisplay ::  " +gcResFault);
				
				if (!AcademyConstants.STR_RTS_SUCCESS_RESP.equalsIgnoreCase(actvationResult)) {
					log.verbose("GC Activation Failed,Reprocess it -> insertIntoAcadReprocessGCActivation");
					insertIntoAcadReprocessGCActivation(env, gcReqEle);
				}	
				//START WN-2907 Handle error scenario where RTS doesn't send IxAuthCode
				else{
					//Start GCD-255 GCD-256 GCD-257
					log.verbose("stamping LotAttribute1 and LotAttribute2 ::  " );
					gcActivationAuth = gcResEle.getElementsByTagName(AcademyConstants.ATTR_RTS_AUTH_CODE).item(0).getTextContent();
					gcAmount = gcResEle.getElementsByTagName(AcademyConstants.ATTR_RTS_AMOUNT).item(0).getTextContent();
					
					log.verbose("IxAuthCode ::  " +gcActivationAuth);
					log.verbose("gcAmount ::  " +gcAmount);
					
					if(!YFSObject.isVoid(gcAmount)){
						dGCAmount = (Double.parseDouble(gcAmount))/100;
					}
					
					shipmentTagSerialNodeList= XMLUtil.getNodeList(
							outGetShipmentDetails,
							"/Shipment/Containers/Container/ContainerDetails/ContainerDetail/ShipmentTagSerials/ShipmentTagSerial[@SerialNo='"
									+ gcCardNo + "']");				
					eleShipmentTagSerial = (Element) shipmentTagSerialNodeList.item(0);				
					eleShipmentTagSerial.setAttribute(AcademyConstants.ATTR_LOT_ATTRIBUTE_1, gcActivationAuth);
					eleShipmentTagSerial.setAttribute(AcademyConstants.ATTR_LOT_ATTRIBUTE_2, Double.toString(dGCAmount));
					//End GCD-255 GCD-256 GCD-257
				}
				//END WN-2907 Handle error scenario where RTS doesn't send IxAuthCode
				
				//if (!AcademyConstants.RTS_SUCCESS_CODE.equalsIgnoreCase(actvationResult)) {
				/*if (AcademyConstants.STR_RTS_DECLINED_RESP.equalsIgnoreCase(actvationResult)) {
					// Read error related description form IxReceiptDisplay
					// field.If it is blank set the activation code returned
					// from RTS response.
					if (!YFCCommon.isVoid(gcResFault))
						errorDesc = gcResFault;
					else
						errorDesc = "Activation response code is ::" + actvationResult;
					sbr = sbr.append(gcCardNo);
					sbr = sbr.append("  ");
					sbr = sbr.append(errorDesc);
					sbr = sbr.append(" , ");
					isActivationFailed = true;
				} else if(AcademyConstants.STR_RTS_UNAVL_RESP.equalsIgnoreCase(actvationResult)){
					log.verbose("RTS resp is '2' -> insertIntoAcadReprocessGCActivation");
					insertIntoAcadReprocessGCActivation(env, gcReqEle);
				}*/
			}
			// If RTS service returns null
			else {
				log.verbose("No response from RTS -> insertIntoAcadReprocessGCActivation");
				//isRTSFault = true;
				insertIntoAcadReprocessGCActivation(env, gcReqEle);
			}
		}			
		
		//TODO: For all cases do changeShipment ????????
		/*//Start GCD-255 GCD-256 GCD-257 comment
		 * Document rt_Shipment = XMLUtil
				.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element el_Shipment = rt_Shipment.getDocumentElement();
		el_Shipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
				this.shipKey);
		Element el_Extn = rt_Shipment.createElement(AcademyConstants.ELE_EXTN);
		//End GCD-255 GCD-256 GCD-257 comment
		 */
		//As part of GCD-47 : Commented and removed code related to Bulk and non bulk GC activation
		// check as RTS service is called only for non-bulk GC activation.
		/*// BulkGC Activation Updates
		if (checkIsBulkGC.equalsIgnoreCase(AcademyConstants.ATTR_Y)) {
			el_Extn.setAttribute(AcademyConstants.ATTR_EXTN_BULK_GCACT_STATUS,
					"Processed");
			el_Shipment.appendChild(el_Extn);
			this.activationType = AcademyConstants.BULK_GC_ACTIVATION_PROCESS_FAILED;
			// checkIsBulkGC - change ShipemntStatus
			String baseDropStatus = "1600.002.102";
			if (this.shipStatus.equalsIgnoreCase("1600.002.52")
					|| this.shipStatus.equalsIgnoreCase("1600.002.52.100")) {
				if (this.shipStatus.equalsIgnoreCase("1600.002.52.100"))
					baseDropStatus = "1600.002.101";
				// 1600.002.51 - GC Activated, Pending Bulk GC
				// activation(1600.002.52) Delivered But Pending GC
				// Activation(1600.002.52.100)
				// Bulk GC processed(1600.002.102) Delivered And Bulk GC
				// Processed(1600.002.101)

				Document changeShipmentStatusDoc = XMLUtil
						.createDocument(AcademyConstants.ELE_SHIPMENT);
				changeShipmentStatusDoc.getDocumentElement().setAttribute(
						AcademyConstants.SHIPMENT_KEY, this.shipKey);
				changeShipmentStatusDoc.getDocumentElement().setAttribute(
						"AcceptOutOfSequenceUpdates", AcademyConstants.STR_YES);
				changeShipmentStatusDoc.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_BASEDROP_STATUS, baseDropStatus);
				changeShipmentStatusDoc.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_TRANSID,
						"AcademyChangeGCShipment.0001.ex");
				
				AcademyUtil.invokeAPI(env,AcademyConstants.CHANGE_SHIPMENT_STATUS_API,
						changeShipmentStatusDoc);
			}
			AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_SHIPMENT, rt_Shipment);
		}
		// Non-BulkGC Activation Updates
		else if (checkIsBulkGC.equalsIgnoreCase(AcademyConstants.STR_NO)) {*/
		
		/*//Start GCD-255 GCD-256 GCD-257 comment
			Element el_ShipmentLines = rt_Shipment
					.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
			el_Shipment.appendChild(el_ShipmentLines);
			Element el_ShipmentLine = rt_Shipment
					.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
			el_ShipmentLine.setAttribute(
					AcademyConstants.ATTR_SHIPMENT_LINE_KEY, this.shipLineKey);
			el_ShipmentLines.appendChild(el_ShipmentLine);
			Element el_Extn_1 = rt_Shipment
					.createElement(AcademyConstants.ELE_EXTN);
			el_Extn_1.setAttribute(
					AcademyConstants.ATTR_EXTN_NON_BULK_GCACT_STATUS,
					"Processed");
			el_ShipmentLine.appendChild(el_Extn_1);
			el_Shipment.appendChild(el_Extn);
			//End GCD-255 GCD-256 GCD-257 comment
		 */
		//Start GCD-255 GCD-256 GCD-257	
		Document rt_Shipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT_LINES);
		Element el_ShipmentLines = rt_Shipment.getDocumentElement();
		Element el_ShipmentLine = rt_Shipment.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
		
		el_ShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, this.shipLineKey);
		el_ShipmentLines.appendChild(el_ShipmentLine);
		
		Element el_extn = rt_Shipment.createElement(AcademyConstants.ELE_EXTN);
		el_extn.setAttribute(AcademyConstants.ATTR_EXTN_NON_BULK_GCACT_STATUS,"Processed");
		el_ShipmentLine.appendChild(el_extn);

		Element el_getShipmentChShp = (Element) outGetShipmentDetails.importNode(rt_Shipment.getDocumentElement(),true);		
		outGetShipmentDetails.getDocumentElement().appendChild(el_getShipmentChShp);

		log.verbose("AcademyGCXMLSplitter.dealGCActivationResponse() rt_Shipment :: "+XMLUtil.getXMLString(rt_Shipment));
		log.verbose("AcademyGCXMLSplitter.dealGCActivationResponse() outGetShipmentDetails :: "+XMLUtil.getXMLString(outGetShipmentDetails));
		
			//this.activationType = AcademyConstants.NON_BULK_GC_ACTIVATION_PROCESS_FAILED;
			log.verbose("AcademyGCXMLSplitter.dealGCActivationResponse() Input to changeShipment API ::" +XMLUtil.getXMLString(rt_Shipment));
			//AcademyUtil.invokeAPI(env, "changeShipment", rt_Shipment);
			AcademyUtil.invokeAPI(env, "changeShipment", outGetShipmentDetails); //To store LotAttributes
			//}
			// END GCD:47
		
		//Commenting as part of GCD-47 : For all of the Unsuccessfull scenarios Insert an record into ACAD_REPROCESS_GC_ACTIVATION, then reprocess.
		/*if (isActivationFailed) {
			gcFailType = "GCActivationFailed";
			raiseAlertForCSR(inDoc, env, sbr, gcFailType);
		}
		if (isSoapFault) {
			gcFailType = "SoapFault";
			raiseAlertForCSR(inDoc, env, sbr, gcFailType);
		}
		if (isRTSFault) {
			gcFailType = "RTSFault";
			raiseAlertForCSR(inDoc, env, sbr, gcFailType);
		}*/
		
		loger.endTimer(" Begining of AcademyGCXMLSplitter  splitGCXMLAndExecuteService() Api");
	}
	
	/**
	 * Method inserts a record into custom table ACAD_REPROCESS_GC_ACTIVATION
	 * When GC Activation call to RTS fails, an record is inserted into ACAD_REPROCESS_GC_ACTIVATION for reprocessing those GC activation.
	 * RTS inputXML,Counter,Processed flag,etc are recorded
	 * 
	 * @param env
	 * @param gcReqEle
	 * @throws DOMException,Exception
	 */
	private void insertIntoAcadReprocessGCActivation(YFSEnvironment env, Element gcReqEle) throws DOMException, Exception{
		loger.beginTimer(" Begining of insertIntoAcadReprocessGCActivation Api");
		Document docCreateAcadReprocessGCActivation = null;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		cal.add(Calendar.MINUTE, Integer.parseInt(strGCReprocessRetryInterval));
		String strAvailableDate = sdf.format(cal.getTime());

		docCreateAcadReprocessGCActivation = XMLUtil.createDocument(AcademyConstants.ELE_ACAD_REPROCESS_GC_ACTIVATION);
		Element eleAcadReprocessGCActivation = docCreateAcadReprocessGCActivation.getDocumentElement();
		eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, shipNo);
		eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_SVCNO, gcReqEle.getElementsByTagName(AcademyConstants.ATTR_RTS_ACCOUNT).item(0).getTextContent());
		eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_MSG_XML, XMLUtil.getXMLString(XMLUtil.getDocumentForElement(gcReqEle)));
		eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_COUNT, AcademyConstants.ATTR_ZERO);
		eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, strAvailableDate);
		eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_ORDER_NO, orderNo);
		eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_PROCESSED, AcademyConstants.STR_NO);
		eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_GC_AMOUNT, requestAmount);
		
		log.verbose("AcademyCreateAcadReprocessGCActivation input :: " +XMLUtil.getXMLString(docCreateAcadReprocessGCActivation));
		//insert record into ACAD_REPROCESS_GC_ACTIVATION for reprocessing
		AcademyUtil.invokeService(env,"AcademyCreateAcadReprocessGCActivation", docCreateAcadReprocessGCActivation);
		loger.endTimer(" End of insertIntoAcadReprocessGCActivation Api");
	}

	//Commenting as part of GCD-111. We will be raising alert in AcademyGCActivationReprocessingAgent after 3 unsuccessfull retries to RTS
	/*private void raiseAlertForCSR(Document inDoc, YFSEnvironment env,
			StringBuffer gcCardNo, String gcFailType) {
		loger.beginTimer(" Begining of AcademyGCXMLSplitter  raiseAlertForCSR() Api");
		log.verbose("raiseAlertForCSR inDoc:: "+XMLUtil.getXMLString(inDoc));
		log.verbose("raiseAlertForCSR gcCardNo:: "+gcCardNo);
		log.verbose("raiseAlertForCSR gcFailType:: "+gcFailType);
		Element eleInboxRefList = null;
		Element eleInboxRef1 = null, eleInboxRef2 = null;
		Document docExceptionInput = null;

		try {
			docExceptionInput = XMLUtil
					.createDocument(AcademyConstants.ELE_INBOX);
			docExceptionInput.getDocumentElement()
					.setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG,
							AcademyConstants.STR_YES);

			docExceptionInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_EXCPTN_TYPE, this.activationType);

			docExceptionInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_SHIPMENT_KEY, shipKey);
			// shipKey

			docExceptionInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_SHIPMENT_NO, shipNo);
			//GCD-47 : Record OrderNo in Alert
			docExceptionInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_ORDER_NO, orderNo);
			
			eleInboxRefList = docExceptionInput
					.createElement(AcademyConstants.ELE_INBOX_REF_LIST);
			XMLUtil.appendChild(docExceptionInput.getDocumentElement(),
					eleInboxRefList);
			eleInboxRef1 = docExceptionInput
					.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			XMLUtil.appendChild(eleInboxRefList, eleInboxRef1);

			// eleInboxRef.setAttribute(AcademyConstants.ATTR_INBOX_REFKEY,
			// "2121212121212");
			eleInboxRef1.setAttribute(AcademyConstants.ATTR_REF_TYPE,
					AcademyConstants.STR_EXCPTN_REF_VALUE);

			//if (!gcFailType.equalsIgnoreCase("SoapFault")) { //Commenting as part of GCD-47
			if (!gcFailType.equalsIgnoreCase("RTSFault")) {
				eleInboxRef1.setAttribute(AcademyConstants.ATTR_VALUE, gcCardNo
						.toString());
				eleInboxRef1.setAttribute(AcademyConstants.ATTR_NAME,
						"Failed GifCard Numbers are");		
			} else {
				eleInboxRef1.setAttribute(AcademyConstants.ATTR_VALUE, gcCardNo
						.toString());
				eleInboxRef1.setAttribute(AcademyConstants.ATTR_NAME,
						"GCACTIVATION FAILED RTSFAULT ISSUE");
			}
			
			//GCD-47 : Record requestAmount in Alert
			eleInboxRef2 = docExceptionInput
			.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			XMLUtil.appendChild(eleInboxRefList, eleInboxRef2);
			eleInboxRef2.setAttribute(AcademyConstants.ATTR_REF_TYPE,
					AcademyConstants.STR_EXCPTN_REF_VALUE);
			eleInboxRef2.setAttribute(AcademyConstants.ATTR_VALUE, requestAmount);
			eleInboxRef2.setAttribute(AcademyConstants.ATTR_NAME,
					"requestAmount of each Failed GifCard");//Need any other info?? total or each GC amount
			
			log.verbose("raiseAlertForCSR : Input to createException API :: "+XMLUtil.getXMLString(docExceptionInput));
			
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CREATE_EXCEPTION,
					docExceptionInput);
			loger.endTimer(" End of AcademyGCXMLSplitter  raiseAlertForCSR() Api");
		} catch (Exception e) {
			throw new YFSException(e.getMessage());
		}

	}*/

}
