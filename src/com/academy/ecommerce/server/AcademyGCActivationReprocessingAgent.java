package com.academy.ecommerce.server;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reprocess Failed GC Activations
 * @author C0007277
 * 30-August-2016
 */

/*
 * The custom non task queue based agent picks the Failed GC Activation records from 
 * ACAD_REPROCESS_GC_ACTIVATION and tries to reprocess by making a call to RTS system.
 * If the GC activation is successful, marks the record as processed else 
 * increments the No of Retry count. After 3 unsuccessfull re-tries raise an alert for CSR
 */

public class AcademyGCActivationReprocessingAgent extends YCPBaseAgent{
	
	private final Logger logger = Logger.getLogger(AcademyGCActivationReprocessingAgent.class.getName());
	
	/*
	 * The getJobs method invokes the getAcadReprocessGCActivationList service to fetch all
	 * elligible records from ACAD_REPROCESS_GC_ACTIVATION. Forms a List of documents, of records which
	 * has Count < 3 and processed flag 'N' and returns the same
	 *  
	 *  @param env 
	 *  	YFSEnvironment
	 *  @param inXML 
	 *  	Document
	 *  @param lastMessage 
	 *  	Document
	 *  @return outputList
	 *  	List  
	 */
	@Override
	public List<Document> getJobs(YFSEnvironment env,Document inXML, Document lastMessage) throws Exception{
		logger.verbose("Inside AcademyGCActivationReprocessingAgent getJobs.The Input xml is : " + XMLUtil.getXMLString(inXML));
		Document docGetAcadReprocessGCActivation = null;
		Document docGetAcadReprocessGCActivationOutput = null;
		List<Document> outputList = new ArrayList<Document>();
		
		String strGCReprocessRetryCount = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_GC_REPR_RETRY_COUNT);
		String strGCReprocessRetryInterval = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_GC_REPR_RETRY_INTERVAL);

		//Forming input doc to AcademyGetAcadReprocessGCActivationList
		docGetAcadReprocessGCActivation = XMLUtil.createDocument(AcademyConstants.ELE_ACAD_REPROCESS_GC_ACTIVATION);
		Element eleAcadReprocessGCActivation = docGetAcadReprocessGCActivation.getDocumentElement();
		eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_PROCESSED, AcademyConstants.STR_NO);		
		eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_COUNT_QRY_TYPE, AcademyConstants.LE_QRY_TYPE);
		//Reading Count value from Agent Criteria params
		eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_COUNT, strGCReprocessRetryCount);
		eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(Calendar.getInstance().getTime()));
		eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_AVAIL_DATE_QRY_TYPE, AcademyConstants.LT_QRY_TYPE);
		eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_MAX_RECORD, inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_NUM_RECORDS));		
		
		if (!YFCObject.isVoid(lastMessage)) {
			logger.verbose("LastMessage is present for the GCActivationReprocessing"+XMLUtil.getXMLString(lastMessage));
			String lastGcRepKey = lastMessage.getDocumentElement().getAttribute(AcademyConstants.ATTR_REPR_GC_ACTVN_KEY);
			eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_REPR_GC_ACTVN_KEY,lastGcRepKey);
			eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_REPR_GC_ACTVN_KEY_QRY_TYPE, AcademyConstants.GT_QRY_TYPE);
		}		
		logger.verbose("Input to getAcadReprocessGCActivationList :: " + XMLUtil.getXMLString(docGetAcadReprocessGCActivation));
		//<ACADReprocessGCActivation AvailableDate="2016-09-16T06:11:10"  AvailableDateQryType="LT" Count="3" CountQryType="LE"
	    // MaximumRecords="10" Processed="N" RepGcActKey="201609160616452420512340" RepGcActKeyQryType="GT"/>
		docGetAcadReprocessGCActivationOutput = AcademyUtil.invokeService(env,"AcademyGetAcadReprocessGCActivationList", docGetAcadReprocessGCActivation);
		
		NodeList nlReprocessGCActivation = docGetAcadReprocessGCActivationOutput.getElementsByTagName(AcademyConstants.ELE_ACAD_REPROCESS_GC_ACTIVATION);		
	
		for (int iEleCount = 0; iEleCount < nlReprocessGCActivation.getLength(); iEleCount++ ){
			Element eleACADReprocessGCActivation = (Element) nlReprocessGCActivation.item(iEleCount);
			//Passing gcReprocessRetryInterval and gcReprocessRetryCount values to executeJobs 
			eleACADReprocessGCActivation.setAttribute(AcademyConstants.ATTR_GC_REPR_RETRY_INTERVAL, strGCReprocessRetryInterval);
			eleACADReprocessGCActivation.setAttribute(AcademyConstants.ATTR_GC_REPR_RETRY_COUNT, strGCReprocessRetryCount);
			logger.verbose("getAcadReprocessGCActivationList.ACADReprocessGCActivation : eleACADReprocessGCActivation :: " 
					+ XMLUtil.getXMLString(XMLUtil.getDocumentForElement(eleACADReprocessGCActivation)));

          	outputList.add(XMLUtil.getDocumentForElement(eleACADReprocessGCActivation));
		}
		logger.verbose("outputList Length" + outputList.size());
		return outputList;		
	}

	/*
	 * The executeJob method processes each record which is returned by getJobs 
	 * Method and makes a call to RTS. If succesfull update corresponding record in 
	 * ACAD_REPROCESS_GC_ACTIVATION to processed, else update Count. If retry attempt increases 
	 * to 3 raise an alert for CSR
	 * 
	 * 	@param env 
	 *  	YFSEnvironment
	 *  @param input 
	 *  	Document
	 *  @return void
	 */	
	@Override
	public void executeJob(YFSEnvironment env, Document input) throws Exception{
		logger.verbose("AcademyGCActivationReprocessingAgent executeJob input :: " + XMLUtil.getXMLString(input));	
		String activationResult = null,gcCardNo = null,gcResFault = null, strProcessed = "N", gcFailType = null;
		Boolean isActivationFailed = false, isRTSFault = false, isRTScallException = false;		
		Element ele = input.getDocumentElement();
		Document docMessageXML = XMLUtil.getDocument(ele.getAttribute(AcademyConstants.ATTR_MSG_XML));
		String strCount = ele.getAttribute(AcademyConstants.ATTR_COUNT);	
		int iCount = Integer.parseInt(strCount);
		String strRepGcActKey = ele.getAttribute(AcademyConstants.ATTR_REPR_GC_ACTVN_KEY);
		String strGCReprocessRetryInterval = ele.getAttribute(AcademyConstants.ATTR_GC_REPR_RETRY_INTERVAL);
		String strGCReprocessRetryCount = ele.getAttribute(AcademyConstants.ATTR_GC_REPR_RETRY_COUNT);
		
		logger.verbose("Input to AcademyInvokeRTSReprocessGCActivation :: " + XMLUtil.getXMLString(docMessageXML));
		Document docRTSresp = null;
		try {
			docRTSresp = AcademyUtil.invokeService(env,"AcademyInvokeRTSReprocessGCActivation", docMessageXML);
		} catch (Exception e) {
			logger.error("AcademyGCActivationReprocessingAgent.executeJob :: RTS Exception - GC Activation reprocess call failed",e);
			isRTScallException = true;
			e.printStackTrace();
		}
		Element gcResEle = docRTSresp.getDocumentElement();
		logger.verbose("AcademyInvokeRTSReprocessGCActivation output :: " + XMLUtil.getXMLString(docRTSresp));		
		
		if (gcResEle != null) {
			activationResult = gcResEle.getElementsByTagName(AcademyConstants.ATTR_RTS_ACTION_CODE).item(0).getTextContent();
			gcCardNo = gcResEle.getElementsByTagName(AcademyConstants.ATTR_RTS_ACCOUNT).item(0).getTextContent();
			//TODO: What if RTS wont send this tag - IxReceiptDisplay ?????
			gcResFault = gcResEle.getElementsByTagName(AcademyConstants.ATTR_RTS_RECEIPT_DISP).item(0).getTextContent();			
			logger.verbose("activationResult : IxActionCode ::  " +activationResult);
			logger.verbose("gcCardNo : IxAccount ::  " +gcCardNo);
			logger.verbose("strGCReprocessRetryCount - 1 ::  " +String.valueOf(Integer.parseInt(strGCReprocessRetryCount) - 1));
			
			if (!AcademyConstants.STR_RTS_SUCCESS_RESP.equalsIgnoreCase(activationResult)) {
				logger.verbose("GC Activation Failed, '1'/'2' response from RTS");
				if(strCount.equals(String.valueOf(Integer.parseInt(strGCReprocessRetryCount) - 1))){
					logger.verbose("Retry limit reached, raise Alert : isActivationFailed");
					//3rd call to RTS also failed, raise Alert 
					isActivationFailed = true;
				}
			}else{//GC Activation Successful - RTS response is '0'
				strProcessed = "Y";
			}
		} else {// If RTS service returns null : No response
			logger.verbose("GC Activation Failed, No response from RTS");
			if(strCount.equals(String.valueOf(Integer.parseInt(strGCReprocessRetryCount) - 1))){
				logger.verbose("Retry limit reached, raise Alert : isRTSFault");
				isRTSFault = true;
			}
		}
		if (YFCCommon.isVoid(gcResFault))
			gcResFault = "RTS Exception";
		logger.verbose("gcResFault : IxReceiptDisplay ::  " +gcResFault);
		
		//TODO: If rts call exception occurs on 3rd call, then we will raise alert(coz count will be 2) but wont increase Count, again will make a call to RTS
		//When ever the RTS call fails due to timeout or some exception in the catch block do not increase the counter
		if(!isRTScallException)
			iCount = iCount + 1;
		
		if(isActivationFailed){
			gcFailType = "GCActivationFailed";
			raiseAlertForCSR(input, env, gcFailType, gcResFault);
		}
		if (isRTSFault) {
			gcFailType = "RTSFault";
			raiseAlertForCSR(input, env, gcFailType, gcResFault);
		}
		
		//Forming input to AcademyChangeAcadReprocessGCActivation
		Document docChangeAcadReprocessGCActivation = XMLUtil.createDocument(AcademyConstants.ELE_ACAD_REPROCESS_GC_ACTIVATION);
		Element eleAcadReprocessGCActivation = docChangeAcadReprocessGCActivation.getDocumentElement();
		eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_REPR_GC_ACTVN_KEY, strRepGcActKey);
		eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_PROCESSED, strProcessed);
		eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_COUNT, String.valueOf(iCount));
		if(!strProcessed.equals(AcademyConstants.STR_YES)){
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
			//Setting GCReprocessRetryInterval in getJobs doc and passing it to executeJobs
			cal.add(Calendar.MINUTE,Integer.parseInt(strGCReprocessRetryInterval));
			String strAvailableDate = sdf.format(cal.getTime());
			
			eleAcadReprocessGCActivation.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, strAvailableDate);
		}
		//<ACADReprocessGCActivation AvailableDate="20160912T055906" Count="2" Processed="N" RepGcActKey="201609091324502420407674"/>
		logger.verbose("Input to AcademyChangeAcadReprocessGCActivation :: " + XMLUtil.getXMLString(docChangeAcadReprocessGCActivation));
		
		try {
			AcademyUtil.invokeService(env,"AcademyChangeAcadReprocessGCActivation", docChangeAcadReprocessGCActivation);
		} catch (Exception e) {
			logger.error("AcademyGCActivationReprocessingAgent.executeJob :: Exception - AcademyChangeAcadReprocessGCActivation call failed ");
			e.printStackTrace();
		}
			
	}
	
	private void raiseAlertForCSR(Document inDoc, YFSEnvironment env, String gcFailType, String gcResFault) throws Exception {
		logger.verbose("raiseAlertForCSR inDoc:: "+XMLUtil.getXMLString(inDoc));
		Element eleInboxRefList = null, eleInboxRef1 = null, eleInboxRef2 = null;
		Document docExceptionInput = null;
		Element eleInput = inDoc.getDocumentElement();
		String strCount = eleInput.getAttribute(AcademyConstants.ATTR_COUNT);
		String strOrderNo = eleInput.getAttribute(AcademyConstants.ATTR_ORDER_NO);
		String strSvcNo = eleInput.getAttribute(AcademyConstants.ATTR_SVCNO);
		String strShipmentNo = eleInput.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		String strAmount = eleInput.getAttribute(AcademyConstants.ATTR_GC_AMOUNT);
		
		//Start : GCD-142
		Document docGetOrderListInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		docGetOrderListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		docGetOrderListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		docGetOrderListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
		logger.verbose("Input to getOrderList :: "+XMLUtil.getXMLString(docGetOrderListInput));		
		
		Document docTemplate = XMLUtil.createDocument(AcademyConstants.ELE_ORDER_LIST);
		Element eleOrder = docTemplate.createElement(AcademyConstants.ELE_ORDER);
		eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,"");
		docTemplate.getDocumentElement().appendChild(eleOrder);
		logger.verbose("getOrderList Template :: "+XMLUtil.getXMLString(docTemplate));
		
		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, docTemplate);
		Document docGetOrderListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, docGetOrderListInput);
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
		logger.verbose("Output from getOrderList :: "+XMLUtil.getXMLString(docGetOrderListOutput));
		
		String strOHK = ((Element) docGetOrderListOutput.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0)).getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
		logger.verbose("strCount:: "+strCount+" strOrderNo::"+strOrderNo+" strOHK::"+strOHK+" strSvcNo::"+strSvcNo+" strAmount::"+strAmount);
		//End : GCD-142
		
		try {
			//Forming input to createException
			docExceptionInput = XMLUtil.createDocument(AcademyConstants.ELE_INBOX);
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG,AcademyConstants.STR_YES);
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE, AcademyConstants.NON_BULK_GC_ACTIVATION_PROCESS_FAILED);
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOHK);
			//GCD-142
			docExceptionInput.getDocumentElement().setAttribute("DetailDescription", "GiftCard Activation Failed for Order# : "+strOrderNo
					+" ::GiftCard Value : "+strAmount+" :: Reason : "+gcResFault);
			
			eleInboxRefList = docExceptionInput.createElement(AcademyConstants.ELE_INBOX_REF_LIST);
			XMLUtil.appendChild(docExceptionInput.getDocumentElement(),eleInboxRefList);
			eleInboxRef1 = docExceptionInput.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			XMLUtil.appendChild(eleInboxRefList, eleInboxRef1);

			eleInboxRef1.setAttribute(AcademyConstants.ATTR_REF_TYPE,AcademyConstants.STR_EXCPTN_REF_VALUE);
			
			String strSVCnoErrorCode = strSvcNo +" "+ gcResFault;
			if (!gcFailType.equalsIgnoreCase("RTSFault")) {
				eleInboxRef1.setAttribute(AcademyConstants.ATTR_VALUE, strSVCnoErrorCode.trim());
				eleInboxRef1.setAttribute(AcademyConstants.ATTR_NAME,"Failed GifCard Number is");		
			} else {
				eleInboxRef1.setAttribute(AcademyConstants.ATTR_VALUE, strSVCnoErrorCode.trim());
				eleInboxRef1.setAttribute(AcademyConstants.ATTR_NAME,"GCACTIVATION FAILED RTSFAULT ISSUE");
			}
			
			eleInboxRef2 = docExceptionInput.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			XMLUtil.appendChild(eleInboxRefList, eleInboxRef2);
			eleInboxRef2.setAttribute(AcademyConstants.ATTR_REF_TYPE,AcademyConstants.STR_EXCPTN_REF_VALUE);
			eleInboxRef2.setAttribute(AcademyConstants.ATTR_VALUE, strAmount);
			eleInboxRef2.setAttribute(AcademyConstants.ATTR_NAME,"Failed GifCard Value");
			
			logger.verbose("raiseAlertForCSR : Input to createException API :: "+XMLUtil.getXMLString(docExceptionInput));
			
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CREATE_EXCEPTION,docExceptionInput);
			
			logger.verbose(" End of AcademyGCXMLSplitter  raiseAlertForCSR() Api");
		} catch (Exception e) {
			throw new YFSException(e.getMessage());
		}

	}

}
