package com.academy.ecommerce.server;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.yantra.yfc.dblayer.YFCContext;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * /* Created for STL-759 Description: Class ACADCloseManifestReprocessingAgent
 * picks the exceptions for the service
 * AcademySFSCloseManifestAsyncService.Checks if its Future Dated Manifest- If
 * true,then ignores the exception and deletes the record from
 * acad_close_manifest_sim table.Also updates the status to '1100' so that the
 * Manifest is in "Open" status in SIM console, Else reprocesses the exceptions
 * that are in '1100' status.
 * 
 */
public class ACADCloseManifestReprocessingAgent extends YCPBaseAgent {

	private final Logger logger = Logger.getLogger(ACADCloseManifestReprocessingAgent.class.getName());
	private final String MANIFEST_STATUS_CLOSED = "2000";

	@Override
	public List<Document> getJobs(YFSEnvironment env, Document inXML) throws Exception {
		logger.verbose("Inside ACADCloseManifestReprocessingAgent getJobs.The Input xml is : " + XMLUtil.getXMLString(inXML));
		List<Document> outputList = new ArrayList<Document>();

		Document docGetIntegrationErrorListOutput = null;
		Document docGetIntegrationErrorListOutputTemplate = null;
		Element eleIntegrationError = null;

		YFCDate yfcDate = new YFCDate(new Date());
		String strDateFormat = "yyyy-MM-dd";
		SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
		String strFromRange = sDateFormat.format(yfcDate) + "T00:00:00";
		String strToRange = sDateFormat.format(yfcDate) + "T21:59:59";

		Document docGetIntegrationErrorListInput = XMLUtil.createDocument(AcademyConstants.ELE_INTREGRATION_ERROR);
		Element eleGetIntegrationErrorListInput = docGetIntegrationErrorListInput.getDocumentElement();

		eleGetIntegrationErrorListInput.setAttribute(AcademyConstants.ATTR_CREATETS_QRY_TYPE, AcademyConstants.BETWEEN);
		eleGetIntegrationErrorListInput.setAttribute(AcademyConstants.ATTR_FLOW_NAME, AcademyConstants.SERV_ACADEMY_SFS_CLOSEP_MANIFEST_ASYNC_SERVICE);
		eleGetIntegrationErrorListInput.setAttribute(AcademyConstants.ATTR_STATE, AcademyConstants.INITIAL);
		eleGetIntegrationErrorListInput.setAttribute(AcademyConstants.ATTR_FROM_CREATETS, strFromRange);
		eleGetIntegrationErrorListInput.setAttribute(AcademyConstants.ATTR_TO_CREATETS, strToRange);
		// Fetching the template for Integration errors.
		docGetIntegrationErrorListOutputTemplate = XMLUtil.getDocument("<IntegrationErrors><IntegrationError ErrorTxnId='' /></IntegrationErrors>");
		env.setApiTemplate(AcademyConstants.API_GET_INTEGRATION_ERROR_LIST, docGetIntegrationErrorListOutputTemplate);

		docGetIntegrationErrorListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_INTEGRATION_ERROR_LIST, docGetIntegrationErrorListInput);
		env.clearApiTemplate(AcademyConstants.API_GET_INTEGRATION_ERROR_LIST);
		// Fetch the child element
		NodeList nlIntegrationError = docGetIntegrationErrorListOutput.getElementsByTagName(AcademyConstants.ELE_INTREGRATION_ERROR);

		// Iterate through the aElementsList
		for (int iEleCount = 0; iEleCount < nlIntegrationError.getLength(); iEleCount++) {
			// Return the document for the fetched child element
			eleIntegrationError = (Element) nlIntegrationError.item(iEleCount);

			// Add the response document into the array list
			outputList.add(XMLUtil.getDocumentForElement(eleIntegrationError));
		}
		logger.verbose("Exiting ACADCloseManifestReprocessingAgent : getJobs ");
		return outputList;
	}

	@Override
	public void executeJob(YFSEnvironment env, Document input) {
		Element eleRootElement = null;
		Element eleGetIntegrationInputXML = null;
		Document docGetIntegrationInputXML = null;
		Document docGetManifestListInput = null;
		String strManifestStatus = "";
		String strManifestDate = "";
		logger.verbose("Entering into ACADCloseManifestReprocessingAgent executeJob with input xml : " + XMLUtil.getXMLString(input));
		eleRootElement = input.getDocumentElement();
		eleRootElement.removeAttribute(AcademyConstants.ATTR_ERROR_MESSAGE);

		try {
			docGetIntegrationInputXML = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_INTEGRATION_INPUT_XML, input);
			eleGetIntegrationInputXML = docGetIntegrationInputXML.getDocumentElement();
			Element eleInputXml = (Element) eleGetIntegrationInputXML.getFirstChild();
			String strManifestInput = eleInputXml.getAttribute(AcademyConstants.ATTR_INPUT_XML);
			docGetManifestListInput = XMLUtil.getDocument(strManifestInput);
			String strManifestKey = docGetManifestListInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_MANIFEST_KEY);
			boolean isFutureManifestDate = false;
			// Check if the ManifestKey is blank.
			if (!YFSObject.isVoid(strManifestKey)) {
				Document docGetManifestListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_MANIFEST_LIST, docGetManifestListInput);
				strManifestStatus = XMLUtil.getAttributeFromXPath(docGetManifestListOutput, "Manifests/Manifest/@ManifestStatus");
				strManifestDate = XMLUtil.getAttributeFromXPath(docGetManifestListOutput, "Manifests/Manifest/@ManifestDate");
				if (!YFCObject.isVoid(strManifestDate)) {
					logger.verbose("Manifest Date is: " + strManifestDate);
					isFutureManifestDate = checkManifestFutureDate(strManifestDate);
				}
				logger.verbose("ACADCloseManifestReprocessingAgent : ManifestStatus :\t" + strManifestStatus);
			}

			if (isFutureManifestDate) {
				// Update the Manifest Status to '1100' when the status is
				// '1300'(Closure Failed) so that its displayed as
				// Open on SIM screen.
				if (strManifestStatus.equals("1300")) {
					UpdateManifestStatus(env, strManifestKey, strManifestStatus);
					logger.verbose("Inside UpdateManifestStatus method.");
				}
				// Remove the manifest key from the custom table.
				AcademyUtil.invokeService(env, AcademyConstants.SERV_ACADEMY_DELETE_CLOSE_MANIFEST_SIM_SERVICE, docGetManifestListInput);
				logger.verbose("The ManifestKey is deleted from the table : " + strManifestKey);
				// Ignore the Exception.
				AcademyUtil.invokeAPI(env, AcademyConstants.API_IGNORE_INTEGRATION_ERROR, input);
				logger.verbose("The Exception has been ignored : ");
			} else {
				// Check if the Manifest satus is close, if yes then ignore the
				// exception else reprocess.
				if (MANIFEST_STATUS_CLOSED.equals(strManifestStatus)) {
					AcademyUtil.invokeAPI(env, AcademyConstants.API_IGNORE_INTEGRATION_ERROR, input);
				} else {
					AcademyUtil.invokeAPI(env, AcademyConstants.API_REPROCESS_INTEGRATION_ERROR, input);
				}
			}
		} catch (Exception e) {
			logger.verbose("Exception inside ACADCloseManifestReprocessingAgent : executeJob ");
			e.printStackTrace();
		}

		logger.verbose("Exiting ACADCloseManifestReprocessingAgent : executeJob ");
	}

	public boolean checkManifestFutureDate(String strManifestDate) throws ParseException {

		YFCDate CurrentDate = new YFCDate(new Date());
		logger.verbose("Today's date:" + CurrentDate);

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date ManifestDate = dateFormat.parse(strManifestDate);
		logger.verbose("Manifest Date:\t" + ManifestDate);
		logger.verbose("Is Future Dated Manifest:\t" + CurrentDate.before(ManifestDate));
		return CurrentDate.before(ManifestDate);
	}

	private void UpdateManifestStatus(YFSEnvironment env, String strManifestKey, String strManifestStatus) throws SQLException {
		Statement stmt = null;

		logger.verbose("The Manifest Status is 1300");
		String strUpdateManifestStatus = "UPDATE YFS_MANIFEST set MANIFEST_STATUS='1100' where MANIFEST_KEY='" + strManifestKey + "'";
		logger.verbose("Update Query:" + strUpdateManifestStatus);
		try {
			YFCContext ctxt = (YFCContext) env;
			stmt = ctxt.getConnection().createStatement();
			int hasUpdated = stmt.executeUpdate(strUpdateManifestStatus);
			if (hasUpdated > 0) {
				logger.verbose("Manifest Status has been updated");
			}
		} catch (SQLException sqlEx) {
			logger.verbose("Error occured while updating the manifest status");
			sqlEx.printStackTrace();
			throw sqlEx;
		} finally {
			if (stmt != null)
				stmt.close();
			stmt = null;
		}
	}
}
