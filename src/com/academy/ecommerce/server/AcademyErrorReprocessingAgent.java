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
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;

/*
 * 
getJobs logic
        1. get commoncodelist for both
                        a. REP_ERR_CODE - Error codes which need to reprocess
                        b. SERV_TO_PROCESS -  Services which should not reprocess
                       
                        getCommonCodeList API:
                                        <CommonCode OrganizationCode="Academy_Direct" >
                                        <ComplexQuery>
                                        <Or>
                                        <Exp Name="CodeType" Value="REP_ERR_CODE"/>
                                        <Exp Name="CodeType" Value="SERV_TO_PROCESS"/>
                                        </Or>
                                        </ComplexQuery>
                                        </CommonCode>
                       
        2. Loop through the getCommonCodeList output and form the input for getIntegrationErrorList API also prepare a ArrayList of Sevice name which should not reprocess.
                       
        3. getIntegrationErrorList API to get ErrorList with proper template.
                        getIntegrationErrorList API:
                        <IntegrationError State="Initial" FromCreatets="2014-06-12T00:00:00" ToCreatets="2014-06-12T21:59:59" CreatetsQryType="BETWEEN">
                                 <ComplexQuery>
                                        <And>
	                                        <Or>
	                                        	<Exp Name="ErrorCode" Value="YCM0033"/>
	                                        	<Exp Name="ErrorCode" Value="YFS10276"/>
	                                        </Or>
	                                        <Or>
	                                        	<Exp Name="FlowName" Value="<Service1>"/>
	                                        	<Exp Name="FlowName" Value="<Service2>"/>
	                                        </Or>
                                        </And>
                                   </ComplexQuery>
                        </IntegrationError>
                                       
                        4. loop through getIntegrationErrorList output and add to arraylist for execute job
                        
ExecuteJobs() logic:                      
        get the input xml and call reprocessIntegrationError API
*/


public class AcademyErrorReprocessingAgent extends YCPBaseAgent {

	private final Logger logger = Logger.getLogger(AcademyErrorReprocessingAgent.class.getName());
	private String strCodeTypeReprocessError = "REP_ERR_CODE";
	private String strCodeTypeServiceToProcess = "SERV_TO_PROCESS";
	@Override
	public List<Document> getJobs(YFSEnvironment env, Document inXML) throws Exception {
		logger.verbose("Inside AcademyErrorReprocessingAgent getJobs.The Input xml is : " + XMLUtil.getXMLString(inXML));
		List<Document> outputList = new ArrayList<Document>();
		Document docGetIntegrationErrorListOutput = null;		
		Element eleIntegrationError = null;		
		String strNoOfDays = inXML.getDocumentElement().getAttribute("NoOfDays");
		logger.verbose("strNoOfDays:\t"+strNoOfDays);
		
		//getCommonCodeList for CodeType REP_ERR_CODE and SERV_TO_PROCESS
		Document docGetCommonCodeListOutput = getCommonCodeList(env);
		
		docGetIntegrationErrorListOutput = getIntegrationErrorList(env, docGetCommonCodeListOutput, strNoOfDays);
		
		if(!YFSObject.isVoid(docGetIntegrationErrorListOutput)){
			// Fetch the child element
			NodeList nlIntegrationError = docGetIntegrationErrorListOutput.getElementsByTagName(AcademyConstants.ELE_INTREGRATION_ERROR);

			// Iterate through the aElementsList. nlIntegrationError will be null if common code(REP_ERR_CODE) is not configured
			for (int iEleCount = 0; ((!YFCObject.isVoid(nlIntegrationError)) && iEleCount < nlIntegrationError.getLength()); iEleCount++) {
				// Return the document for the fetched child element
				eleIntegrationError = (Element) nlIntegrationError.item(iEleCount);

				// Add the response document into the array list
				outputList.add(XMLUtil.getDocumentForElement(eleIntegrationError));
			}
		}		
		logger.verbose("Exiting AcademyErrorReprocessingAgent : getJobs ");
		return outputList;
	}

	/**getCommonCodeList for CodeType REP_ERR_CODE and SERV_TO_PROCESS
	 * @param env
	 * @return
	 * @throws Exception
	 */
	private Document getCommonCodeList(YFSEnvironment env) throws Exception {
		String strGetCommonCodeListInput = "<CommonCode OrganizationCode=''>" +
												"<ComplexQuery>" +
													"<Or>" +
														"<Exp Name='CodeType' Value='"+strCodeTypeReprocessError+"'/>" +
														"<Exp Name='CodeType' Value='"+strCodeTypeServiceToProcess +"'/>" +
													"</Or>" +
												"</ComplexQuery>" +
											"</CommonCode>";
		
		Document docGetCommonCodeListInput = XMLUtil.getDocument(strGetCommonCodeListInput);
		Document docGetCommonCodeListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST, docGetCommonCodeListInput);
		return docGetCommonCodeListOutput;
	}

	/**
	 * @param docGetCommonCodeListOutput
	 * @return
	 * @throws Exception
	 */
	private Document getIntegrationErrorList(YFSEnvironment env,Document docGetCommonCodeListOutput, String strNoOfDays) throws Exception {
		logger.verbose("Entering into AcademyErrorReprocessingAgent : getIntegrationErrorList() ");
		Element eleExp;
		Element eleCommonCode;
		Document docGetIntegrationErrorListOutputTemplate = null;
		Document docGetIntegrationErrorListOutput = null;
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String strToRange = sdf.format(cal.getTime());		
		cal.add(Calendar.DATE, (-Integer.parseInt(strNoOfDays)));
		String strFromRange = sdf.format(cal.getTime());
		boolean isReprocessErrorCodeConfigured = false;
		boolean isReprocessServiceConfigured = false;
		
		String strGetIntegrationErrorListInput = "<IntegrationError State='Initial' FromCreatets='"+strFromRange+"' ToCreatets='"+strToRange+"' CreatetsQryType='BETWEEN'>" +				
														"<ComplexQuery>" +
														"<And>" +
														"<Or>" +
														//"<Exp Name='ErrorCode' Value='<ErrorCode>'/>" +
														//"</Or>" +
														
														//"<Or>" +
														//"<Exp Name='FlowName' Value='<FlowName>'/>" +
														"</Or>" +
														"</And>" +
														"</ComplexQuery>" +
													"</IntegrationError>";
		
		Document docGetIntegrationErrorListInput = XMLUtil.getDocument(strGetIntegrationErrorListInput);
		Element eleAnd = XMLUtil.getElementByXPath(docGetIntegrationErrorListInput, "IntegrationError/ComplexQuery/And");
		Element eleOrForError = XMLUtil.getElementByXPath(docGetIntegrationErrorListInput, "IntegrationError/ComplexQuery/And/Or");
		Element eleOrForService = docGetIntegrationErrorListInput.createElement("Or");
		eleAnd.appendChild(eleOrForService);
		//For loop on commoncodelist output
		NodeList nlCommonCodeList = docGetCommonCodeListOutput.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
		
		// Iterate through the aElementsList
		for (int iEleCount = 0; iEleCount < nlCommonCodeList.getLength(); iEleCount++) {			
			eleCommonCode = (Element) nlCommonCodeList.item(iEleCount);
			eleExp = docGetIntegrationErrorListInput.createElement("Exp");
			eleExp.setAttribute("Value", eleCommonCode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE));

			if(strCodeTypeReprocessError.equals(eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_TYPE))){
				isReprocessErrorCodeConfigured = true;
				eleExp.setAttribute("Name", "ErrorCode");		
				eleOrForError.appendChild(eleExp);
			}else if(strCodeTypeServiceToProcess.equals(eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_TYPE))){
				isReprocessServiceConfigured = true;
				eleExp.setAttribute("Name", "FlowName");
				eleOrForService.appendChild(eleExp);
			}
		}
		
		logger.verbose("isReprocessErrorCodeConfigured : " + isReprocessErrorCodeConfigured);
		logger.verbose("isReprocessServiceConfigured : " + isReprocessServiceConfigured);
		if(isReprocessServiceConfigured && isReprocessErrorCodeConfigured){
			docGetIntegrationErrorListOutputTemplate = XMLUtil.getDocument("<IntegrationErrors><IntegrationError ErrorTxnId='' FlowName=''/></IntegrationErrors>");
			env.setApiTemplate(AcademyConstants.API_GET_INTEGRATION_ERROR_LIST, docGetIntegrationErrorListOutputTemplate);

			docGetIntegrationErrorListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_INTEGRATION_ERROR_LIST, docGetIntegrationErrorListInput);
			env.clearApiTemplate(AcademyConstants.API_GET_INTEGRATION_ERROR_LIST);
		}
		logger.verbose("Exiting AcademyErrorReprocessingAgent : getIntegrationErrorList() ");
		return docGetIntegrationErrorListOutput;
	}

	@Override
	public void executeJob(YFSEnvironment env, Document input) {
		logger.verbose("Entering into AcademyErrorReprocessingAgent executeJob with input xml : " + XMLUtil.getXMLString(input));
		try {
			AcademyUtil.invokeAPI(env, AcademyConstants.API_REPROCESS_INTEGRATION_ERROR, input);
			logger.verbose("AcademyErrorReprocessingAgent.executeJob() :\n" + XMLUtil.getXMLString(input));
		} catch (Exception e) {
			logger.verbose("Exception inside AcademyErrorReprocessingAgent : executeJob ");
			e.printStackTrace();
		}

		logger.verbose("Exiting AcademyErrorReprocessingAgent : executeJob");
	}
}
