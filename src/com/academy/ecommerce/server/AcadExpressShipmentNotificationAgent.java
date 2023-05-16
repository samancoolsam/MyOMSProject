package com.academy.ecommerce.server; 


import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @author <a href="mailto:Shruthi.Kenkarenarendrababu@cognizant.com">Shruthi KN</a>, Created on
 *  5/31/2016       
 */

public class AcadExpressShipmentNotificationAgent extends YCPBaseAgent {

	private final Logger logger = Logger.getLogger(AcadExpressShipmentNotificationAgent.class.getName());

	public static final String strCreatePickupReq = "CREATEPICKUPREQ";
	@Override
	public ArrayList<Document> getJobs(YFSEnvironment env, Document inXML) throws Exception {
		logger.verbose("Inside AcadExpressShipmentNotificationAgent getJobs.The Input xml is : " + XMLUtil.getXMLString(inXML));

		Document docGetShipmentListOutput = null;
		Document docGetShipmentListInput = null;
		Element eleCurrentShipment = null;
		Element eleInput = null;
		String strCutOffTime = null;
		String strShipNode = null;
		String strNumRecordsToBuffer = null;
		ArrayList<Document> ShipmentList = new ArrayList<Document>();
		ArrayList<String> ShipNodesList = new ArrayList<String>();
		boolean isGetShipmentListNeed = true;

		eleInput = inXML.getDocumentElement();
		strCutOffTime = eleInput.getAttribute(AcademyConstants.STR_CUTOFF_TIME);
		strNumRecordsToBuffer = eleInput.getAttribute(AcademyConstants.ATTR_NUM_RECORDS);
		int iNumRecordsToBuffer = Integer.parseInt(strNumRecordsToBuffer);

		if(eleInput.hasChildNodes()){
			logger.verbose("Not invoking any API as all jobs are already processed ");
			return ShipmentList;
		}	
		// prepare input for getShipmentList API
		docGetShipmentListInput = prepareInputforGetShipmentList(env,inXML,strNumRecordsToBuffer);
		Element eleGetShipmentListInput = docGetShipmentListInput.getDocumentElement();
		while(isGetShipmentListNeed){		
			//invoke getShipmentList API
			docGetShipmentListOutput = invokeGetShipemntList(env,docGetShipmentListInput);

			NodeList nlShipmentList = docGetShipmentListOutput.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
			int iShipmentLength = nlShipmentList.getLength();
			if(iShipmentLength < iNumRecordsToBuffer){
				isGetShipmentListNeed = false;
			}
			logger.verbose("isGetShipmentListNeed: "+isGetShipmentListNeed);
			for (int iShipmentCount = 0; iShipmentCount < iShipmentLength; iShipmentCount++) {
				eleCurrentShipment = (Element) nlShipmentList.item(iShipmentCount);
				eleCurrentShipment.setAttribute(AcademyConstants.STR_CUTOFF_TIME, strCutOffTime);
				strShipNode = eleCurrentShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
				//check if ArrayList contains that ShipNode
				logger.verbose("iShipmentCount : "+iShipmentCount);
				logger.verbose("strShipmentNo : "+eleCurrentShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));

				if(!ShipNodesList.contains(strShipNode)){
					logger.verbose("strShipNode :" + strShipNode);
					ShipNodesList.add(strShipNode);
					ShipmentList.add(XMLUtil.getDocumentForElement(eleCurrentShipment));
				}
				//need to pass ShipmentKey if getShipmentList API call needed further. so that API will not return same set of shipments
				if(isGetShipmentListNeed && (iShipmentCount == (iShipmentLength-1))){
					//Set shipmentKey and ShipmentKeyQryType to getShipmentListinput
					String strLastShipmentKey = eleCurrentShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
					eleGetShipmentListInput.setAttribute(AcademyConstants.ATTR_SHIPMENTKEY_QRY_TYPE, AcademyConstants.GT_QRY_TYPE);
					eleGetShipmentListInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strLastShipmentKey);
					logger.verbose("strLastShipmentKey: "+strLastShipmentKey);
				}
			}
		}
		logger.verbose("Exiting AcadExpressShipmentNotificationAgent : getJobs ");
		return ShipmentList;
	}


	/**
	 * This method will invoke the getShipmentList API
	 * @param env
	 * @param docgetShipmentListInput
	 * @return
	 * @throws Exception
	 */
	private Document invokeGetShipemntList(YFSEnvironment env,Document docgetShipmentListInput) throws Exception {
		Document docgetShipmentListOutput = null;

		env.setApiTemplate(AcademyConstants.GET_SHIPMENT_LIST_API,AcademyConstants.TEMPLATE_FEDX_PING_GET_SHIPMENT_LIST_API);
		logger.verbose("Input xml for getShipmentList api:"+ com.academy.util.xml.XMLUtil.getXMLString(docgetShipmentListInput));
		docgetShipmentListOutput = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_SHIPMENT_LIST, docgetShipmentListInput);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		logger.verbose("Output xml for getShipmentList api:"+ com.academy.util.xml.XMLUtil.getXMLString(docgetShipmentListOutput));
		return docgetShipmentListOutput;
	}


	/**
	 * This method will prepare the input document for getShipmentList API
	 * @param env
	 * @param inXML
	 * @param strNumRecordsToBuffer
	 * @return
	 * @throws Exception
	 */
	private Document prepareInputforGetShipmentList(YFSEnvironment env, Document inXML, String strNumRecordsToBuffer) throws Exception {
		logger.verbose("Entering into method prepareInputAndCallGetShipmentList : getJobs ");
		Document docGetShipmentListInput = null;
		Element eleShipment = null;
		Element eleComplexQuery = null;
		Element eleOr = null;
		Element eleExp = null;

		docGetShipmentListInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleShipment = docGetShipmentListInput.getDocumentElement();
		eleShipment.setAttribute(AcademyConstants.ATTR_SCAC, AcademyConstants.SCAC_FEDX);
		eleShipment.setAttribute(AcademyConstants.ATTR_MAX_RECORD,strNumRecordsToBuffer);
		eleShipment.setAttribute(AcademyConstants.ATTR_STATUS, AcademyConstants.VAL_READY_TO_SHIP_STATUS);

		eleComplexQuery = docGetShipmentListInput.createElement(AcademyConstants.COMPLEX_QRY_ELEMENT);	
		eleShipment.appendChild(eleComplexQuery);
		//Stamp Standard Overnight
		eleOr = docGetShipmentListInput.createElement(AcademyConstants.COMPLEX_OR_ELEMENT);
		eleComplexQuery.appendChild(eleOr);
		eleExp = docGetShipmentListInput.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.CARRIER_SERVICE_CODE);
		eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
		eleExp.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.CARRIER_STANDARD_OVERNIGHT);
		eleOr.appendChild(eleExp);
		//Stamp 2 Day
		eleExp = docGetShipmentListInput.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.CARRIER_SERVICE_CODE);
		eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
		eleExp.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.CARRIER_TWO_DAY);		
		eleOr.appendChild(eleExp);

		logger.verbose("End of method prepareInputAndCallGetShipmentList : getJobs ");
		return docGetShipmentListInput;
	}


	@Override
	public void executeJob(YFSEnvironment env, Document input) throws Exception {
		logger.verbose("Entering into AcadExpressShipmentNotificationAgent executeJob");
		Element eleShipment = null;
		String strDate = null;
		Document docCreateAcadExpressFedxPing = null;
		//Document docGetAcadExpressFedxPingout = null;
		String strCutOffTime = null;
		String strSysDate = null;
		String strShipNode = null;
		String strConcatDateAndNode = null;
		Element eleOutput = null;

		eleShipment = input.getDocumentElement();
		strCutOffTime = eleShipment.getAttribute(AcademyConstants.STR_CUTOFF_TIME);
		strShipNode= eleShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE);

		//check whether the custom table has any records for the current day and storeNo combination
		Date calSystemDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.DATE_YYYYMMDD_FORMAT);
		strSysDate=sdf.format(calSystemDate);
		strConcatDateAndNode = strSysDate.concat(strShipNode);

		docCreateAcadExpressFedxPing = XMLUtil.createDocument(AcademyConstants.ELE_ACAD_FEDX_EXP_PING);
		Element eleAcadFedxExpressPing = docCreateAcadExpressFedxPing.getDocumentElement();
		eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_EXPRESS_KEY, strConcatDateAndNode);
		eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_PROCESS_DATE, strSysDate);
		eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_STORE_NO, strShipNode);

		//Stamp the “AcademyReadyTimestamp” in shipment element 
		calSystemDate = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat(AcademyConstants.STR_SIMPLE_DATE_PATTERN);
		strDate=sdf1.format(calSystemDate.getTime());
		eleShipment.setAttribute(AcademyConstants.FDX_CREATE_PICK_REQ_READYTIMESTAMP,strDate.concat(strCutOffTime));  
		try{
			//insert record to Database
			AcademyUtil.invokeService(env, AcademyConstants.CREATE_ACAD_FEDX_EXPRESS_PING, docCreateAcadExpressFedxPing);
			//invoke service AcademySFSInvokeFedexExpressCreatePickupReqWebservice
			Document docResponse = AcademyUtil.invokeService(env, AcademyConstants.FEDEX_EXPRESS_PICKUP_REQ_SERVICE, input);
			parseCreatePickupResAndRaiseAlert(env, input,docResponse); 
		}
		catch (YFSException yfsEx) {		
			if(AcademyConstants.ERROR_CODE_VAL.equals(yfsEx.getErrorCode())){
				logger.verbose("Record Already Exists in Database");
			}
			else{
			logger.verbose("Exception inside AcadExpressShipmentNotificationAgent: executeJob ");
			logger.verbose("Exception " + yfsEx.getMessage());
			throw yfsEx;	
			}
		}
		logger.verbose("Exiting from AcadExpressShipmentNotificationAgent executeJob");
	}	

	/**
	 * This method will validate whether the Fedx response is Success or Failure
	 * @param env
	 * @param input
	 * @param docResponse
	 * @throws Exception
	 */
	private void parseCreatePickupResAndRaiseAlert(YFSEnvironment env,
			Document input, Document docResponse) throws Exception {
		logger.verbose("parseCreatePickupResAndRaiseAlert: START");
		Element eleHighestSeverity = (Element)docResponse.getElementsByTagName(AcademyConstants.ELE_HIGHEST_SEVERITY).item(0);
		if(!YFCObject.isVoid(eleHighestSeverity)){
			//if the element is not void
			if ("SUCCESS".equalsIgnoreCase(eleHighestSeverity.getTextContent())) {
				createAlert(env, input, docResponse, true);
			} else {
				createAlert(env, input, docResponse, false);
			}
		}
		else{
			logger.verbose("Fedx has not returned HighestSeverity Element ");
			createAlert(env, input, docResponse, false);
		}		
		logger.verbose("parseCreatePickupResAndRaiseAlert: END");
	}

	/**
	 * This method will raise an alert in both the cases that is Success and Failure
	 * @param env
	 * @param input
	 * @param docResponse
	 * @param isSuccess
	 * @throws Exception
	 */
	private void createAlert(YFSEnvironment env, Document input,
			Document docResponse, boolean isSuccess) throws Exception {
		logger.verbose("createAlert Method: START");
		Element eleRoot = null;
		String strShipmentNo = "";
		String strShipNode = "";
		Element eleInboxRef = null;
		Element eleInboxRefLst = null;
		String sErrorDesc = "";
		String sErrorCode = "";
		Element eleMessage = null;
		Element eleCode = null;
		Element eleDesc = null;

		eleRoot = input.getDocumentElement();
		strShipmentNo = eleRoot.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		strShipNode = eleRoot.getAttribute(AcademyConstants.ATTR_SHIP_NODE);

		eleMessage = (Element)docResponse.getElementsByTagName(AcademyConstants.ATTR_MESSAGE).item(0);
		if(!YFCObject.isVoid(eleMessage)){
			sErrorDesc = eleMessage.getTextContent();
		}
		else{
			//Message element will not present if ContainerGrossWeightUOM is not LB.we have put this logic to handle similar type of eror 
			eleDesc = (Element)docResponse.getElementsByTagName(AcademyConstants.ATTR_FEDX_PICKUP_FAILURE_DESC).item(0);
			sErrorDesc = eleDesc.getTextContent();
		}
		
		eleCode = (Element)docResponse.getElementsByTagName(AcademyConstants.ATTR_CODE).item(0);
		if(!YFCObject.isVoid(eleCode)){
			sErrorCode = eleCode.getTextContent();
		}

		Document inputCreateException = XMLUtil.createDocument(AcademyConstants.ELE_INBOX);
		Element rootEle = inputCreateException.getDocumentElement();
		rootEle.setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG,AcademyConstants.STR_YES);
		rootEle.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		rootEle.setAttribute(AcademyConstants.ATTR_SHIP_NODE_KEY, strShipNode);

		eleInboxRefLst = inputCreateException.createElement(AcademyConstants.ELE_INBOX_REF_LIST);		
		rootEle.appendChild(eleInboxRefLst);
		//InboxRef for Fedex Response Code
		eleInboxRef = inputCreateException.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME,AcademyConstants.ATTR_FEDX_RESP_CODE);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE,AcademyConstants.STR_EXCPTN_REF_VALUE);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, sErrorCode);
		eleInboxRefLst.appendChild(eleInboxRef);
		//InboxRef for Fedex Response Message
		eleInboxRef = inputCreateException.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME,AcademyConstants.ATTR_FEDX_RESP_MSG);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE,AcademyConstants.STR_EXCPTN_REF_VALUE);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, sErrorDesc);
		eleInboxRefLst.appendChild(eleInboxRef);

		if (isSuccess) {
			rootEle.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE,AcademyConstants.FDX_CREATE_PICK_REQ_SUCCESS_ALERT_TYPE);
			rootEle.setAttribute("Description","Fedex Express Pickup Request successfully created for Shipment No:"+ strShipmentNo);

			String sPickConfirmNo = docResponse.getElementsByTagName(AcademyConstants.ELE_PICKUP_CONF_NUM).item(0).getTextContent();
			eleInboxRef = inputCreateException.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME,AcademyConstants.ATTR_PICKUP_CONF_NUM);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE,AcademyConstants.STR_EXCPTN_REF_VALUE);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE,sPickConfirmNo);
			eleInboxRefLst.appendChild(eleInboxRef);

		} else {
			rootEle.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE,AcademyConstants.FDX_CREATE_PICK_REQ_FAILURE_ALERT_TYPE);
			rootEle.setAttribute("Description","Fedex Express Pickup Request failed for Shipment No:"+ strShipmentNo);
		}

		logger.verbose("createAlert : Input Doc"+ XMLUtil.getXMLString(inputCreateException));
		AcademyUtil.invokeAPI(env, AcademyConstants.API_CREATE_EXCEPTION,inputCreateException);
		logger.verbose("createAlert Method: END");
	}
}

