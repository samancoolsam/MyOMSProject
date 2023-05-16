package com.academy.ecommerce.server;


/*##################################################################################
*
* Project Name                : Ignore Error
* Module                      : OMS
* Author                      : CTS
* Date                        : 31-JULY-2020
* Description				  : Created As Per Support Enhancement: OMNI-8894 
*                               This  class is used to get all the exceptions based on the values configured in Common Code Type and reprocess them.
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       Remarks/Description                      
* ---------------------------------------------------------------------------------
* 31-JULY-2020		CTS  	 			  1.0           	Initial version
* 03-AUG-2020		CTS  	 			  2.0           	strCodeTypeIgnoreError variable scope changed from class to method-local level
* 04-AUG-2020		CTS  	 			  3.0           	nuLL checked changed for variable strCodeTypeIgnoreError

* ##################################################################################*/


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
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;


/*
 * 
getJobs logic
        1. get commoncodelist for both
                        a. ERROR_IGNORE - Error and Service codes which need to be ignored, passed as arg from criteria param of agent
                                            
                        getCommonCodeList API:
                                        <CommonCode CodeType="ERROR_IGNORE"/>                                        
                                      
                       
        2. Loop through the getCommonCodeList output and form the input for getIntegrationErrorList API.
                       
        3. getIntegrationErrorList API to get ErrorList with proper template.
                        getIntegrationErrorList API:
                        
       <IntegrationError State="Initial" FromCreatets="2019-05-12T00:00:00" ToCreatets="2019-05-13T21:59:59" CreatetsQryType="BETWEEN" >
    <ComplexQuery>
        <And>
            <Or>
                <And>
                    <Exp Name="FlowName" Value="AcademyFraudStatusUpdateToKountAsyncService"/>
                    <Exp Name="ErrorCode" Value="EXTN_KOUNT_01"/>
                </And>
                <And>
                    <Exp Name="FlowName" Value="AcademyENSFraudStatusFromKount"/>
                    <Exp Name="ErrorCode" Value="YCP0270"/>
                </And>
            </Or>
        </And>
    </ComplexQuery>
</IntegrationError>
                                       
                        4. loop through getIntegrationErrorList output and add to arraylist for execute job
                        
ExecuteJobs() logic:                      
        get the input xml and call reprocessIntegrationError API
*/


public class AcademyEnhancedIgnoreErrorAgent extends YCPBaseAgent {

	private final Logger logger = Logger.getLogger(AcademyEnhancedIgnoreErrorAgent.class.getName());
	
	@Override
	public List<Document> getJobs(YFSEnvironment env, Document inXML) throws Exception {
		logger.verbose("Inside AcademyEnhancedIgnoreErrorAgent getJobs.The Input xml is : " + XMLUtil.getXMLString(inXML));
		List<Document> outputList = new ArrayList<Document>();
		Document docGetIntegrationErrorListOutput = null;		
		Element eleIntegrationError = null;		
		String strNoOfDays = inXML.getDocumentElement().getAttribute("NoOfDays");
		logger.verbose("strNoOfDays:\t"+strNoOfDays);
		
		//getCommonCodeList for CodeType ERROR_IGNORE
		Document docGetCommonCodeListOutput = getCommonCodeList(env,inXML);
		
		docGetIntegrationErrorListOutput = getIntegrationErrorList(env, docGetCommonCodeListOutput, strNoOfDays);
		
		if(!YFSObject.isVoid(docGetIntegrationErrorListOutput)){
			// Fetch the child element
			NodeList nlIntegrationError = docGetIntegrationErrorListOutput.getElementsByTagName(AcademyConstants.ELE_INTREGRATION_ERROR);

			// Iterate through the aElementsList. nlIntegrationError will be null if common code is not configured
			for (int iEleCount = 0; ((!YFCObject.isVoid(nlIntegrationError)) && iEleCount < nlIntegrationError.getLength()); iEleCount++) {
				// Return the document for the fetched child element
				eleIntegrationError = (Element) nlIntegrationError.item(iEleCount);

				// Add the response document into the array list
				outputList.add(XMLUtil.getDocumentForElement(eleIntegrationError));
				
			}
		}	
		
		logger.verbose("Exiting AcademyEnhancedIgnoreErrorAgent : getJobs ");
		return outputList;
	}
	
	/**getCommonCodeList for CodeType ERROR_IGNORE
	 * @param env
	 * @return
	 * @throws Exception
	 */
	private Document getCommonCodeList(YFSEnvironment env,Document inXML) throws Exception {
		
		String strCodeTypeIgnoreError = null;
		Document docGetCommonCodeListOutput=XMLUtil.getDocument("<CommonCodeList/>");
		strCodeTypeIgnoreError=inXML.getDocumentElement().getAttribute("CommonCodeType");
		logger.verbose("strCodeTypeIgnoreError:\t"+strCodeTypeIgnoreError);
		if(!YFCObject.isNull(strCodeTypeIgnoreError)
				&& !YFCObject.isVoid(strCodeTypeIgnoreError))
		{
		String strGetCommonCodeListInput = "<CommonCode CodeType='"+strCodeTypeIgnoreError+"'/>";
		
		Document docGetCommonCodeListInput = XMLUtil.getDocument(strGetCommonCodeListInput);
		docGetCommonCodeListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST, docGetCommonCodeListInput);
		}
		return docGetCommonCodeListOutput;
	}

	/**
	 * @param docGetCommonCodeListOutput
	 * @return
	 * @throws Exception
	 */
	private Document getIntegrationErrorList(YFSEnvironment env,Document docGetCommonCodeListOutput, String strNoOfDays) throws Exception {
		logger.verbose("Entering into AcademyEnhancedIgnoreErrorAgent : getIntegrationErrorList() ");
		Element eleExp;
		Element eleExp1;
		Element eleCommonCode;
		Document docGetIntegrationErrorListOutputTemplate = null;
		Document docGetIntegrationErrorListOutput = null;
		String sCodeValue=null;
		boolean isErrorIgnoreCodeConfigured = false;
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String strToRange = sdf.format(cal.getTime());		
		cal.add(Calendar.DATE, (-Integer.parseInt(strNoOfDays)));
		String strFromRange = sdf.format(cal.getTime());
		
				
		String strGetIntegrationErrorListInput = "<IntegrationError State='Initial' FromCreatets='"+strFromRange+"' ToCreatets='"+strToRange+"' CreatetsQryType='BETWEEN'>" +				
														"<ComplexQuery>" +
														"<And>" +
														"<Or>" +
														//"<And>" +
														//"<Exp Name='ErrorCode' Value='<ErrorCode>'/>" +							
																						
														//"<Exp Name='FlowName' Value='<FlowName>'/>" +
														//"</And>" +
														"</Or>" +
														"</And>" +
														"</ComplexQuery>" +
													"</IntegrationError>";
		
		Document docGetIntegrationErrorListInput = XMLUtil.getDocument(strGetIntegrationErrorListInput);
		Element eleAndOr = XMLUtil.getElementByXPath(docGetIntegrationErrorListInput, "IntegrationError/ComplexQuery/And/Or");
				
		//For loop on commoncodelist output
		NodeList nlCommonCodeList = docGetCommonCodeListOutput.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
		int i=nlCommonCodeList.getLength();
		
		logger.verbose("length of commonCodeList :" +i);
		// Iterate through the ElementsList
		for (int iEleCount = 0; iEleCount < i; iEleCount++) {
			Element eleAndForErrorService = docGetIntegrationErrorListInput.createElement("And");				
			 eleCommonCode = (Element) nlCommonCodeList.item(iEleCount);
			 			
			sCodeValue  = eleCommonCode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
			logger.verbose("The values of the codevalue " + sCodeValue  +"shortdescription value is " + eleCommonCode.getAttribute("CodeShortDescription") + "LongDescription value is " +eleCommonCode.getAttribute("CodeLongDescription"));
			
					
			if(sCodeValue.startsWith("COMBO")){
				isErrorIgnoreCodeConfigured= true;
				eleExp = docGetIntegrationErrorListInput.createElement("Exp");
				 eleExp.setAttribute("Name", "FlowName");
				 eleExp.setAttribute("Value", eleCommonCode.getAttribute("CodeLongDescription"));
				 eleExp1 = docGetIntegrationErrorListInput.createElement("Exp");
				 eleExp1.setAttribute("Name", "ErrorCode");
				 eleExp1.setAttribute("Value", eleCommonCode.getAttribute("CodeShortDescription"));				
				 eleAndForErrorService.appendChild(eleExp);
				 eleAndForErrorService.appendChild(eleExp1);
			 }
				 
			 else if(sCodeValue.startsWith("SERVICENAME")){
				    isErrorIgnoreCodeConfigured= true;
					 eleExp = docGetIntegrationErrorListInput.createElement("Exp");
					 eleExp.setAttribute("Name", "FlowName");
					 eleExp.setAttribute("Value", eleCommonCode.getAttribute("CodeLongDescription"));
					 eleAndForErrorService.appendChild(eleExp);
				 }else if(sCodeValue.startsWith("ERRORCODE")){
					 isErrorIgnoreCodeConfigured= true;
					 eleExp = docGetIntegrationErrorListInput.createElement("Exp");
					 eleExp.setAttribute("Name", "ErrorCode");
					 eleExp.setAttribute("Value", eleCommonCode.getAttribute("CodeShortDescription"));
					 eleAndForErrorService.appendChild(eleExp);
				 }					 
				 
			 				
			 eleAndOr.appendChild(eleAndForErrorService);	
		}
			 
			 logger.verbose( "docGetIntegrationErrorListInput ---> "+  XMLUtil.getXMLString(docGetIntegrationErrorListInput));
					 
		if(isErrorIgnoreCodeConfigured){
			logger.verbose("isErrorIgnoreCodeConfigured : " + isErrorIgnoreCodeConfigured);
		
			docGetIntegrationErrorListOutputTemplate = XMLUtil.getDocument("<IntegrationErrors><IntegrationError ErrorTxnId='' FlowName=''/></IntegrationErrors>");
			env.setApiTemplate(AcademyConstants.API_GET_INTEGRATION_ERROR_LIST, docGetIntegrationErrorListOutputTemplate);

			docGetIntegrationErrorListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_INTEGRATION_ERROR_LIST, docGetIntegrationErrorListInput);
			env.clearApiTemplate(AcademyConstants.API_GET_INTEGRATION_ERROR_LIST);
		}
		logger.verbose("Exiting AcademyEnhancedIgnoreErrorAgent : getIntegrationErrorList() ");
		return docGetIntegrationErrorListOutput;
	}
		

	@Override
	public void executeJob(YFSEnvironment env, Document input) {
		logger.verbose("Entering into AcademyEnhancedIgnoreErrorAgent executeJob with input xml : " + XMLUtil.getXMLString(input));
		try {
			AcademyUtil.invokeAPI(env, AcademyConstants.API_IGNORE_INTEGRATION_ERROR, input);
			logger.verbose("AcademyEnhancedIgnoreErrorAgent.executeJob() :\n" + XMLUtil.getXMLString(input));
		} catch (Exception e) {
			logger.verbose("Exception inside AcademyEnhancedIgnoreErrorAgent : executeJob ");
			e.printStackTrace();
		}

		logger.verbose("Exiting AcademyEnhancedIgnoreErrorAgent : executeJob");
	}
}
