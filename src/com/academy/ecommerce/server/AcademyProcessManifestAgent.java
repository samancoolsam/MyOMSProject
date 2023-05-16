package com.academy.ecommerce.server;


import java.sql.SQLException;
import java.sql.Statement;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dblayer.YFCContext;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * Created for STL-1456 - Auto Manifest Close for Stores.
 * Description: Class AcademyProcessManifestAgent.
 * Get the records from YFS_TASK_Q table and will call close manifest API and call registerprocessCompletion API
 * to delete the processed record from table.
 * If any error occur during closeManifest API invocation then it will raise an alert and 
 * set the AvailableDate for 30minSo that after 30min it will again pick and process that record 
 * 
 */

public class AcademyProcessManifestAgent extends YCPBaseTaskAgent {
	
	private static Logger log=Logger.getLogger(AcademyProcessManifestAgent.class.getName());
	
	@Override
	public Document executeTask(YFSEnvironment env, Document doc)throws Exception {
		log.verbose("Entering into AcademyProcessManifestAgent executeJob with input xml : \n" + XMLUtil.getXMLString(doc));
		Document docCloseManifestInput= null;
		Document docCloseManifestOutput= null;
		Element eleRootElement = null;
		String strManifestKey = "";
		String strtaskQKey = "";
		String strNextTriggerTime = "";
		String strNextTriggerTimeInMin = "";
		Element eleRoot = null;
		Element eleTransactionFilters = null;
		
		try{
		 //Fetch the DataKey=ManifestKey from the input document	
		 strManifestKey = doc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_KEY);
		 log.verbose("Returned DataKey that is ManifestKey : "+ strManifestKey);
		 strtaskQKey = doc.getDocumentElement().getAttribute(AcademyConstants.ATTR_TASK_Q_KEY);
		 log.verbose("Returned DataKey that is TaskQKey : "+ strtaskQKey);
		 
		 eleRoot = doc.getDocumentElement();
		 eleTransactionFilters = (Element)eleRoot.getElementsByTagName(AcademyConstants.ELE_TRANSACTI_ON_FILTERS).item(0);
		 strNextTriggerTimeInMin = eleTransactionFilters.getAttribute(AcademyConstants.ATTR_NEXT_TRIGGER_TIME_IN_MIN);
			 
		 //prepare input to call closeManifest API
		 docCloseManifestInput = XMLUtil.createDocument(AcademyConstants.ELEM_MANIFEST);
		 eleRootElement = docCloseManifestInput.getDocumentElement();
		 eleRootElement.setAttribute(AcademyConstants.ATTR_MANIFEST_KEY, strManifestKey);
		 log.verbose("Calling API closeManifest, docCloseManifestInput : \n" + XMLUtil.getXMLString(docCloseManifestInput));
		 docCloseManifestOutput = AcademyUtil.invokeAPI(env,AcademyConstants.API_CLOSE_MANIFEST, docCloseManifestInput); 
		 log.verbose("Done with Calling closeManifest API, docCloseManifestOutput : \n "+XMLUtil.getXMLString(docCloseManifestOutput));	
		 
		 //call manageTaskQ API to delete the processed record
		 callManageTaskQueueAPI(env, strtaskQKey, null, AcademyConstants.STR_OPERATION_VAL_DELETE);
		 log.info("Successfully processed the task q record");
		 	 
		}catch(YFSException yfsEx){
			// Get the stack trace.
			log.error("Exception Stack Trace: "+ yfsEx);
			
			if(AcademyConstants.STR_MANIFEST_CLOSED_ERR_CODE_VAL.equals(yfsEx.getErrorCode())){				
				//If the manifest is already closed call registerProcessCompletion API to delete the record from YFS_TASK_Q table
				callManageTaskQueueAPI(env, strtaskQKey, null, AcademyConstants.STR_OPERATION_VAL_DELETE);
				log.info("Manifest is already Closed");
			}
			else{
				//Start EFP-12 USPS To Stores
				String strIsUSPSManifest = (String)env.getTxnObject(AcademyConstants.STR_IS_USPS_MANIFEST);
				log.verbose("Environment Handle::"+env);
				if(!YFCObject.isVoid(strIsUSPSManifest) 
						&& strIsUSPSManifest.equalsIgnoreCase(AcademyConstants.STR_YES)){
					log.verbose("##Caught Exception For USPS##");
					log.verbose("##Input Document::##"+XMLUtil.getXMLString(doc));
					try{
						env.setTxnObject(AcademyConstants.STR_IS_USPS_MANIFEST, AcademyConstants.STR_NO);
						log.verbose("##Setting Environment Object IsUSPSmanifest to 'N' in AcademyProcessManifestAgent##");
						UpdateManifestStatus(env,strManifestKey);
					}catch(SQLException sqlEx){
						log.verbose("Error occured while updating the manifest status- AcademyProcessManifestAgent");
						log.error("Exception Stack Trace: "+ sqlEx);
					}
				}else{
					//End EFP-12 USPS To Stores
					//Below is the logic to raise alert if Manifest is not USPS
					//Method to Raise Alert
					Document docAlertInput = prepareInputToRaiseAlert(env, doc, strManifestKey);
					log.verbose("Calling Service AcademyRaiseAlertOnCloseManifestFailure, docAlertInput : \n" + XMLUtil.getXMLString(docAlertInput));
					AcademyUtil.invokeService(env, AcademyConstants.RAISE_ALERT_ON_CLOSE_MANIFEST_FAILURE, docAlertInput);
					log.verbose("Done with calling Service AcademyRaiseAlertOnCloseManifestFailure");
					log.info("Close Manifest Failed: InXML: " + XMLUtil.getXMLString(docAlertInput));
								 
					Calendar cal = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
					cal.add(Calendar.MINUTE, (Integer.parseInt(strNextTriggerTimeInMin)));
					strNextTriggerTime = sdf.format(cal.getTime());
					
					//Retain the taskQ record and change the available date
					callManageTaskQueueAPI(env, strtaskQKey, strNextTriggerTime, null);
					log.info("Calling manageTaskQueue Api to set the next AvailableDate");
				}
			}
		}	
		log.verbose("Exiting AcademyProcessManifestAgent : executeJob ()");
		return docCloseManifestOutput;
	}
		
	/** This method will prepare an input to call manageTaskQueue API
	 * @param env
	 * @param strtaskQKey
	 * @param strNextTriggerTime
	 * @throws ParserConfigurationException
	 * @throws Exception
	 */
	private void callManageTaskQueueAPI(YFSEnvironment env, String strtaskQKey,
			String strNextTriggerTime, String strOperation) throws ParserConfigurationException,
			Exception {
		log.verbose("Entering into callManageTaskQueueAPI Method");
		Document manageTaskQueueInDoc = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
		Element manageTaskQueueInEle = manageTaskQueueInDoc.getDocumentElement();
		if(!YFCObject.isVoid(strNextTriggerTime)){
		manageTaskQueueInEle.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, strNextTriggerTime);
		}
		if(!YFCObject.isVoid(strOperation)){
		manageTaskQueueInEle.setAttribute(AcademyConstants.ATTR_OPERATION, strOperation);	
		}
		manageTaskQueueInEle.setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, strtaskQKey);
		log.verbose("manageTaskQueue  API \n"+XMLUtil.getXMLString(manageTaskQueueInDoc));
		AcademyUtil.invokeAPI(env,AcademyConstants.DSV_MANAGE_TASK_QUEUE_API, manageTaskQueueInDoc);
		log.verbose("Exiting from callManageTaskQueueAPI Method");
	}

	/**
	 * @param env
	 * @param strtaskQKey
	 *//*
	private void callregisterProcessCompletionAPI(YFSEnvironment env, String strtaskQKey) {
		try {
			Document inXML=XMLUtil.createDocument(AcademyConstants.ELE_REG_PROCESS_COMP_INPUT);
			Element rootElem=inXML.getDocumentElement();
			rootElem.setAttribute(AcademyConstants.ATTR_KEEP_TASK_OPEN, "N");
			Element currTaskElem=inXML.createElement(AcademyConstants.ELE_CURR_TASK);
			currTaskElem.setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, strtaskQKey);
			rootElem.appendChild(currTaskElem);
			AcademyUtil.invokeAPI(env, AcademyConstants.API_REGISTER_PROCESS_COMPLETION, inXML);
		} catch (ParserConfigurationException e) {			
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}		
	}*/

	/**
	 * Prepares input document to raise an Alert for close manifest failure
	 * @param env
	 * @param doc
	 * @throws Exception
	 */
	private Document prepareInputToRaiseAlert(YFSEnvironment env, Document doc, String strManifestKey) throws Exception {
		log.verbose("Inside prepareInputToRaiseAlert Method \n" +XMLUtil.getXMLString(doc));
		//prepare input to call getmanifestList
		Document docgetManifestListInput = XMLUtil.createDocument(AcademyConstants.ELEM_MANIFEST);
		Element eleManifest = docgetManifestListInput.getDocumentElement();
		eleManifest.setAttribute(AcademyConstants.ATTR_MANIFEST_KEY, strManifestKey);
		
		env.setApiTemplate(AcademyConstants.API_GET_MANIFEST_LIST, XMLUtil.getDocument(AcademyConstants.GET_MANIFEST_LIST_TEMPLATE));
		log.verbose("Template XML is"+ AcademyConstants.GET_MANIFEST_LIST_TEMPLATE);
		
		log.verbose("Calling API getManifestList, docgetManifestListInput : \n" + XMLUtil.getXMLString(docgetManifestListInput));
		Document getManifestListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_MANIFEST_LIST, docgetManifestListInput);
		log.verbose("Done with Calling getManifestList, getManifestListOutDoc : \n "+XMLUtil.getXMLString(getManifestListOutDoc));
		env.clearApiTemplate(AcademyConstants.API_GET_MANIFEST_LIST);
		
		log.verbose("End of prepareInputToRaiseAlert Method \n" +XMLUtil.getXMLString(getManifestListOutDoc));
		return getManifestListOutDoc;				
	}

	/**
	 * Update MANIFEST_STATUS = '2000' and MANIFEST_CLOSED_FLAG = 'Y' in YFS_MANIFEST Table 
	 * for USPS Manifests which are failed to get closed
	 * @param env
	 * @param strManifestKey
	 * @throws Exception
	 */
	private void UpdateManifestStatus(YFSEnvironment env, String strManifestKey) throws SQLException {
		Statement stmt = null;

		log.verbose("##Setting Manifest Status##");
		String strUpdateManifestStatus = "UPDATE YFS_MANIFEST set MANIFEST_STATUS='2000', MANIFEST_CLOSED_FLAG='Y'" +
				" where MANIFEST_KEY='" + strManifestKey + "'";
		log.verbose("Update Query:" + strUpdateManifestStatus);
		try {
			YFCContext ctxt = (YFCContext) env;
			stmt = ctxt.getConnection().createStatement();
			int hasUpdated = stmt.executeUpdate(strUpdateManifestStatus);
			if (hasUpdated > 0) {
				log.verbose("Manifest Status has been updated");
			}
		} catch (SQLException sqlEx) {
			log.verbose("Error occured while updating the manifest status");
			sqlEx.printStackTrace();
			throw sqlEx;
		} finally {
			if (stmt != null)
				stmt.close();
			stmt = null;
		}
	}
	
}


	