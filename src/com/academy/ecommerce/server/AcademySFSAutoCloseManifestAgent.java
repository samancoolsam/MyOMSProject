package com.academy.ecommerce.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * Created for STL-1456 - Auto Manifest Close for Stores.
 * Description: Class AcademySFSAutoCloseManifestAgent.
 * Gets list of current day Manifest in the open status - 1100 and ManifetsClosedFlag=N.
 * Checks whether shipment is OverPacked/UnderPacked
 * If the shipment id packed properly then it will call manageTaskQueue API to insert record into YFS_TASK_Q table 
 * If its shipment is OverPacked/UnderPacked it send an email notification.
 */
 
public class AcademySFSAutoCloseManifestAgent extends YCPBaseAgent {
	private final Logger logger = Logger.getLogger(AcademySFSAutoCloseManifestAgent.class.getName());
	public static long lastUpdateTime = 0;
	
	@Override
	public List<Document> getJobs(YFSEnvironment env, Document inXML) throws Exception {
		logger.verbose("Inside AcademySFSAutoCloseManifestAgent getJobs().The Input xml is : \n" + XMLUtil.getXMLString(inXML));
		List<Document> outputList = new ArrayList<Document>();
		Document docGetOpenManifestListOutput = null;
		long currentTime = System.currentTimeMillis();
		String strCurrentDate = "";
		Document docGetOpenManifestListInput = null;
		Element eleGetOpenManifestListInput = null;
		

		try
		{
			// Run getJobs once only in one trigger. As getjob is skipping some Manifest due to data issue. Method will be called till outputList is 0.
			long timeDiff =  (currentTime - lastUpdateTime ) / 1000; // timeDiff in seconds
			logger.verbose("currentTime : " + currentTime + " , lastUpdateTime : " + lastUpdateTime + "timeDiff from last run : " + timeDiff );
			if ( lastUpdateTime !=0 && timeDiff < 600){ // If not first time in trigger and difference from last run is less than 600s then exit with 0 outputList.
				lastUpdateTime = 0;
				return outputList; // Nothing to do. Return empty outputList
			}

			lastUpdateTime = System.currentTimeMillis(); // update lastUpdateTime for time difference calculation.
			 				
			//prepare input for getManifestList API
			/* 
			   <Manifest ManifestStatus="1100" ManifestClosedFlag="N" ManifestDate="2015-12-15"/ >
			 */
			docGetOpenManifestListInput = XMLUtil.createDocument(AcademyConstants.ELEM_MANIFEST);
			eleGetOpenManifestListInput = docGetOpenManifestListInput.getDocumentElement();
			
			//Get the current system date
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_SIMPLE_DATE_PATTERN);
			strCurrentDate = sdf.format(cal.getTime());	
			
			eleGetOpenManifestListInput.setAttribute(AcademyConstants.ATTR_MANIFEST_CLOSED_FLAG, AcademyConstants.STR_NO);
			eleGetOpenManifestListInput.setAttribute(AcademyConstants.ATTR_MANIFEST_DATE, strCurrentDate);
			Element eleComplexQuery=SCXmlUtil.createChild(eleGetOpenManifestListInput, AcademyConstants.COMPLEX_QRY_ELEMENT);
			eleComplexQuery.setAttribute(AcademyConstants.COMPLEX_OPERATOR_ATTR, AcademyConstants.COMPLEX_OPERATOR_AND_VAL);
			Element eleComplexQueryAND=SCXmlUtil.createChild(eleComplexQuery, AcademyConstants.COMPLEX_AND_ELEMENT);
			Element eleComplexQueryOR=SCXmlUtil.createChild(eleComplexQueryAND, AcademyConstants.COMPLEX_OR_ELEMENT);
			Element eleComplexQueryExp1=SCXmlUtil.createChild(eleComplexQueryOR, AcademyConstants.COMPLEX_EXP_ELEMENT);
			eleComplexQueryExp1.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_MANIFEST_STATUS);
			eleComplexQueryExp1.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.ATTR_MANIFEST_STATUS_OPEN);
			eleComplexQueryExp1.setAttribute(AcademyConstants.ATTR_MANIFEST_STATUS_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
			Element eleComplexQueryExp2=SCXmlUtil.createChild(eleComplexQueryOR, AcademyConstants.COMPLEX_EXP_ELEMENT);
			eleComplexQueryExp2.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_MANIFEST_STATUS);
			eleComplexQueryExp2.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.ATTR_MANIFEST_STATUS_CLOSURE_REQUESTED);
			eleComplexQueryExp2.setAttribute(AcademyConstants.ATTR_MANIFEST_STATUS_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
			
			
			
			env.setApiTemplate(AcademyConstants.API_GET_MANIFEST_LIST, XMLUtil.getDocument(AcademyConstants.STR_GET_MANIFEST_LIST_TEMPLATE));
			logger.verbose("Template XML is"+ AcademyConstants.STR_GET_MANIFEST_LIST_TEMPLATE);
					
			// Call API getManifestList for get list of open Manifests
			logger.verbose("Calling API getManifestList, docGetOpenManifestListInput : \n" + XMLUtil.getXMLString(docGetOpenManifestListInput));
			docGetOpenManifestListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_MANIFEST_LIST, docGetOpenManifestListInput);
			logger.verbose("Done with Calling getManifestList, docGetOpenManifestListOutput : \n "+XMLUtil.getXMLString(docGetOpenManifestListOutput));
			env.clearApiTemplate(AcademyConstants.API_GET_MANIFEST_LIST);
			
			NodeList nlManifestList = docGetOpenManifestListOutput.getElementsByTagName(AcademyConstants.ELEM_MANIFEST);
			int iManifestLength = nlManifestList.getLength();
			logger.verbose("nlManifestList.getLength() : " + nlManifestList.getLength());
			for (int iManifestCount = 0; iManifestCount < iManifestLength; iManifestCount++) {
					Element eleCurrentManifest = (Element) nlManifestList.item(iManifestCount);
					outputList.add(XMLUtil.getDocumentForElement(eleCurrentManifest));
				}				

		} catch (Exception e) {
			logger.verbose("Exception inside AcademySFSAutoCloseManifestAgent : getJobs() ");
			logger.verbose("Exception " + e.getMessage());
			e.printStackTrace();
		}
		logger.verbose("outputList.size() : " + outputList.size());
		logger.verbose("Exiting AcademySFSAutoCloseManifestAgent : getJobs ()");
		return outputList;
	}

	@Override
	public void executeJob(YFSEnvironment env, Document input) throws Exception {
		logger.verbose("Entering into AcademySFSAutoCloseManifestAgent executeJob with input xml : \n" + XMLUtil.getXMLString(input));
		
		Document docManageTaskQInput = null;
		Document docMangaeTaskQOutput= null;
		Document docGetManifestListOutput = null;
		
		try {
			String strManifestKey = input.getDocumentElement().getAttribute(AcademyConstants.ATTR_MANIFEST_KEY);
			env.setApiTemplate(AcademyConstants.API_GET_MANIFEST_LIST, AcademyConstants.STR_TEMPLATEFILE_API_GET_MANIFEST_LIST);
			
			// Call API getManifestList API
			logger.verbose("Calling API getManifestList, docGetOpenManifestListInput : \n" + XMLUtil.getXMLString(input));
			docGetManifestListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_MANIFEST_LIST, input);
			logger.verbose("Done with Calling getManifestList, docGetOpenManifestListOutput : \n "+XMLUtil.getXMLString(docGetManifestListOutput));
			env.clearApiTemplate(AcademyConstants.API_GET_MANIFEST_LIST);
			
			boolean isEligibleForClose = isEligibleForClose(env,docGetManifestListOutput);
			
			if(isEligibleForClose){
				//prepare input to call manageTaskQueue API 
				/* Sample Input XML
				   <TaskQueue DataKey="201601050109582411044499" DataType="ManifestKey" Operation="Create" 
				   TransactionId="ACAD_CLOSE_MANIFEST.2008.ex" TransactionKey="20160107053414314198" />  */
				
				docManageTaskQInput = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
				Element eleTaskQueue = docManageTaskQInput.getDocumentElement();
				eleTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY, strManifestKey);
				eleTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE, AcademyConstants.ATTR_MANIFEST_KEY);
				eleTaskQueue.setAttribute(AcademyConstants.ATTR_OPERATION,AcademyConstants.STR_OPERATION_VAL_CREATE);
				eleTaskQueue.setAttribute(AcademyConstants.ATTR_TRANS_ID,AcademyConstants.ACAD_CLOSE_MANIFEST_TRANS_ID);
				eleTaskQueue.setAttribute(AcademyConstants.ATTR_TRANS_KEY,AcademyConstants.ACAD_CLOSE_MANIFEST_TRANS_KEY);
				
				logger.verbose("Calling API manageTaskQueue, docmanageTaskQInput : \n" + XMLUtil.getXMLString(docManageTaskQInput));
				docMangaeTaskQOutput = AcademyUtil.invokeAPI(env, AcademyConstants.DSV_MANAGE_TASK_QUEUE_API, docManageTaskQInput);
				logger.verbose("Done with Calling manageTaskQueue, docMangaeTaskQOutput : \n "+XMLUtil.getXMLString(docMangaeTaskQOutput));
			}
					
		} catch (YFSException yfsEx) {		
			if(AcademyConstants.ERROR_CODE_VAL.equals(yfsEx.getErrorCode())){
				logger.verbose("Record Already Exists in Database");
			}
			else{
			logger.verbose("Exception inside AcademySFSAutoCloseManifestAgent : executeJob ");
			logger.verbose("Exception " + yfsEx.getMessage());
			throw yfsEx;
			}
		}
		logger.verbose("Exiting AcademySFSAutoCloseManifestAgent : executeJob() ");		
	}
	

	/** This method will validate all shipments and check if the manifest is eligible to close.
	 * @param env
	 * @param docGetManifestListOutput
	 * @return
	 */
	public boolean isEligibleForClose(YFSEnvironment env, Document docGetManifestListOutput) {
		boolean isEligible = true;
		String strManifestNo;
		String strShipNode;
		String strManifestDate;
		String strManifestKey;
		boolean isOverPacked=false;
		boolean isIncorrectStatus=false;
		double packedQuantity = 0;

		Document docManifestDataIssueInput = null;
		Element eleManifestDataIssueInput = null;
		Element eleShipmentsIn = null;
		Element eleManifest = null;
		logger.verbose("Calling AcademySFSAutoCloseManifestAgent : isEligibleForClose() \n" +XMLUtil.getXMLString(docGetManifestListOutput));
		
		try {
		// Check the result returned by the getManifestDetails to see shipment container status ready to be Manifest Closing.
		eleManifest = (Element) docGetManifestListOutput.getElementsByTagName(AcademyConstants.ELEM_MANIFEST).item(0);
	    NodeList shipmentList = docGetManifestListOutput.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
		logger.verbose("shipmentsNodesList length :" + shipmentList.getLength());
		strManifestNo = eleManifest.getAttribute(AcademyConstants.ATTR_MANIFEST_NO);
		strManifestDate = eleManifest.getAttribute(AcademyConstants.ATTR_MANIFEST_DATE);
		strShipNode = eleManifest.getAttribute(AcademyConstants.SHIP_NODE);
		strManifestKey = eleManifest.getAttribute(AcademyConstants.ATTR_MANIFEST_KEY);
		
		docManifestDataIssueInput = XMLUtil.createDocument(AcademyConstants.ELEM_MANIFEST);
		eleManifestDataIssueInput = docManifestDataIssueInput.getDocumentElement();
		eleManifestDataIssueInput.setAttribute(AcademyConstants.ATTR_MANIFEST_NO, strManifestNo);
		eleManifestDataIssueInput.setAttribute(AcademyConstants.SHIP_NODE, strShipNode);
		eleManifestDataIssueInput.setAttribute(AcademyConstants.ATTR_MANIFEST_DATE, strManifestDate);
		eleShipmentsIn = docManifestDataIssueInput.createElement(AcademyConstants.ELE_SHIPMENTS);
		eleManifestDataIssueInput.appendChild(eleShipmentsIn);
		Element eleShipment = null;
		
		for (int iShipment = 0,iShipmentcount=shipmentList.getLength(); iShipment < iShipmentcount; iShipment++) {
			packedQuantity = 0;
			logger.verbose("Getting shipment");
			eleShipment = (Element) shipmentList.item(iShipment);
			String strShipmentNo = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			logger.verbose("ShipmentNo : '"+ strShipmentNo +"'");
			String strStatus = eleShipment.getAttribute(AcademyConstants.ATTR_STATUS);
			logger.verbose("Shipment Status : '"+ strStatus +"'");
			
			NodeList  ContainerDetailList = eleShipment.getElementsByTagName(AcademyConstants.CONTAINER_DETL_ELEMENT);
			for (int iContainerDetail= 0; iContainerDetail < ContainerDetailList.getLength(); iContainerDetail++) {
				Element eleContainerDetail = (Element) ContainerDetailList.item(iContainerDetail);
				String strQuantity =  eleContainerDetail.getAttribute(AcademyConstants.ATTR_QUANTITY);
				
				if (!YFSObject.isVoid(strQuantity))
				{
					packedQuantity = packedQuantity + Double.valueOf(strQuantity);
				}
			}
			
			NodeList  shipmentLineList = eleShipment.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			double shipmentActualQuantity = 0;
			for (int iContainer= 0,iShipmentLineCount=shipmentLineList.getLength(); iContainer < iShipmentLineCount; iContainer++) {
				Element eleShipmentLine = (Element) shipmentLineList.item(iContainer);
				String strActualQuantity = eleShipmentLine.getAttribute(AcademyConstants.ATTR_ACTUAL_QUANTITY);
				if (!YFSObject.isVoid(strActualQuantity))
					{
						shipmentActualQuantity = shipmentActualQuantity + Double.valueOf(strActualQuantity);
					}
					logger.verbose("shipmentActualQuantity : " + shipmentActualQuantity);
			}
			logger.verbose("shipmentActualQuantity : " + shipmentActualQuantity + ", Packed Quantity : "  + packedQuantity);
			
			if (packedQuantity > shipmentActualQuantity) {
				isEligible=false;
				isOverPacked=true;	
				logger.verbose("isOverPacked:  "+isOverPacked);
				addShipmentToErrorList(docManifestDataIssueInput,eleShipmentsIn,strShipmentNo,packedQuantity,shipmentActualQuantity,true);
			}
			else if (AcademyConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL.equalsIgnoreCase(strStatus)){
				isEligible=false;
				isIncorrectStatus=true;
				logger.verbose("isIncorrectStatus:  "+isIncorrectStatus);								
				addShipmentToErrorList(docManifestDataIssueInput,eleShipmentsIn,strShipmentNo, packedQuantity,shipmentActualQuantity,false);				
				}		
		}
		if(isIncorrectStatus || isOverPacked) {
			sendFailureNotification(env, isOverPacked, docManifestDataIssueInput, eleManifestDataIssueInput);
			}
		} catch (Exception e){
		logger.verbose("Exception inside AcademySFSAutoCloseManifestAgent : isEligibleForClose ");
		logger.verbose("Exception " + e.getMessage());
		e.printStackTrace();
		}
		return isEligible;
	}

	/** This method will send email notification for Manifest which are not eligible.
	 * @param env
	 * @param isOverPacked
	 * @param docManifestDataIssueInput
	 * @param eleManifestDataIssueInput
	 * @throws Exception
	 */
	private void sendFailureNotification(YFSEnvironment env,boolean isOverPacked, Document docManifestDataIssueInput,
			Element eleManifestDataIssueInput) throws Exception {
		if(isOverPacked){
			eleManifestDataIssueInput.setAttribute(AcademyConstants.STR_MANIFEST_ISSUE, AcademyConstants.STR_MANIFEST_ISSUE_CONTAINER);
		}else{
			eleManifestDataIssueInput.setAttribute(AcademyConstants.STR_MANIFEST_ISSUE, AcademyConstants.STR_MANIFEST_ISSUE_STATUS);
		}
		eleManifestDataIssueInput.setAttribute("ShipmentIssue", "true");
		// Calling API AcademySFSManifestDataIssueNotification to send Notification Mail for Manifest Issue Details.
		logger.verbose("Calling API AcademySFSManifestDataIssueNotification ,  docGetManifestDetails : \n" + XMLUtil.getXMLString(docManifestDataIssueInput));
		Document docManifestDataIssueOutput =  AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_SFS_MANIFEST_DATA_ISSUE_NOTIFICATION, docManifestDataIssueInput);
		logger.verbose("Done with Calling service AcademySFSManifestDataIssueNotification  ,  docManifestDataIssueOutput : \n" + XMLUtil.getXMLString(docManifestDataIssueOutput));
	}

	/**
	 * @param packedQuantity
	 * @param docManifestDataIssueInput
	 * @param eleShipmentsIn
	 * @param strShipmentNo
	 * @param shipmentActualQuantity
	 */
	private void addShipmentToErrorList(Document docManifestDataIssueInput, Element eleShipmentsIn,
			String strShipmentNo, double packedQuantity, double shipmentActualQuantity, boolean isOverPacked) {
		Element eleShipmentIn;
		eleShipmentIn = docManifestDataIssueInput.createElement("Shipment");
		eleShipmentsIn.appendChild(eleShipmentIn);		
		eleShipmentIn.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		eleShipmentIn.setAttribute("ShipmentLineQuantity", String.valueOf(shipmentActualQuantity));
		eleShipmentIn.setAttribute("ShipmentPackedQuantity", String.valueOf(packedQuantity));
		
		if(isOverPacked){
			eleShipmentIn.setAttribute("StatusMessage", AcademyConstants.STR_SHIPMENT_MSG_OVERPACKED);	
		}else{
			eleShipmentIn.setAttribute("StatusMessage", AcademyConstants.STR_SHIPMENT_MSG_STATUS);	
		}
	}
	
}