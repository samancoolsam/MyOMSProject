package com.academy.ecommerce.sterling.bopis.api;

import static com.academy.util.constants.AcademyConstants.SERVICE_YANTRIKS_KAFKA_DELTA_UPDATE;

import java.io.*;
import java.net.*;
import java.util.Properties;

import javax.xml.transform.*;

import org.w3c.dom.*;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;

/**
 * @author Mohamed Shaikna
 *
 */

public class AcademyBopisStoreEligibility {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyBopisStoreEligibility.class);

	/**
	 * Instance to store the properties configured for the condition in
	 * Configurator.
	 */
	private Properties props;

	/**
	 * Stores properties configured in configurator.
	 * 
	 * @param props
	 */
	public void setProperties(Properties props) {
		this.props = props;
	}

	/**
	 * This method will invoke the REST Web service exposed by WCS to update the
	 * isBopisEligible Flag to Y or N.
	 * 
	 * @param env
	 * @param inXML
	 * @return inXML
	 * @throws Exception 
	 *
	 */

	public Document bopisStoreEligibility(YFSEnvironment env, Document inXML) throws Exception {

		YFSException ex = new YFSException();

		log.beginTimer("AcademyBopisStoreEligibility::BopisStoreEligibility");
		log.verbose("Entering the method AcademyBopisStoreEligibility.BopisStoreEligibility ");
		log.verbose("Input XML for AcademyBopisStoreEligibility ==> " + XMLUtil.getXMLString(inXML));

		Element eleOrganization = inXML.getDocumentElement();
		String storeID = eleOrganization.getAttribute(AcademyConstants.ORGANIZATION_CODE);

		Element eleExtn = (Element) eleOrganization.getElementsByTagName("Extn").item(0);
		String isBopisFlag = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_IS_BOPIS_ENABLED);

		/**
		 * If the Bopis Flag is Y, Send 1 in request, if N, Send 0 in request to WCS.
		 **/
		if (isBopisFlag.equalsIgnoreCase(AcademyConstants.STR_YES))
			isBopisFlag = AcademyConstants.STR_ONE;
		else
			isBopisFlag = AcademyConstants.STR_ZERO;

		// Get ErrorDescription from Service Arguments
		String errorDescription = props.getProperty("bopisFlagErrorDescription",
				"Failed to Update Store BOPIS Eligibility Attribute in GCP MySQL DB for Store Info.");
		String errorCode = "";

		log.verbose("OrganizationCode : " + storeID);
		log.verbose("isBopisEnabledFlag : " + isBopisFlag);

		// Get URL from COP and append the I/P to it.
		String strURL = props.getProperty("bopisFlagWebserviceURL");
		strURL += "?storeNbr=" + storeID + "&bopisFlag=" + isBopisFlag;

		int connectTimeOut = Integer.parseInt(props.getProperty("ConnectTimeOut", "60000"));
		int readTimeOut = Integer.parseInt(props.getProperty("ReadTimeOut", "60000"));

		log.verbose("WebServiceURL : " + strURL);

		URL url = null;

		try {
			url = new URL(strURL);
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("POST");
			// BOPIS-1996 - Fix for 411 Error
			httpCon.setRequestProperty("Content-Length", "0");
			httpCon.setConnectTimeout(connectTimeOut);
			httpCon.setReadTimeout(readTimeOut);
		
			// BOPIS-1996 - Fix for 411 Error - Start
			log.verbose("writerContent : " + props.getProperty("writerContent"));
			OutputStream os = httpCon.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(props.getProperty("writerContent"));
			writer.flush();
			writer.close();
			os.close();
			// BOPIS-1996 - Fix for 411 Error - End
			
			log.verbose("ConnectTimeout : " + connectTimeOut);
			log.verbose("ReadTimeout : " + readTimeOut);
			log.verbose("ResponseCode : " + httpCon.getResponseCode());
			log.verbose("ResponseMessage : " + httpCon.getResponseMessage());

			// Check if the HTTP Response is 200. If yes, Check for Status. Else, Throw
			// Error
			if (Integer.toString(httpCon.getResponseCode()).equalsIgnoreCase(AcademyConstants.SUCCESS_CODE)) {

				BufferedReader br = new BufferedReader(new InputStreamReader((httpCon.getInputStream())));
				StringBuilder sb = new StringBuilder();
				String output;
				while ((output = br.readLine()) != null) {
					sb.append(output);
				}

				JSONObject json = new JSONObject(sb.toString());

				String strStatus = "", strUpdateCount = "";

				/**
				 * Check if the O/P Json has the expected format.
				 * 
				 * { "status": { "status": "Success", "updateCount": "1" } }
				 **/

				if (json.has("status") && (json.getJSONObject("status").has("status")
						&& json.getJSONObject("status").has("updateCount"))) {

					strStatus = json.getJSONObject("status").getString("status");
					strUpdateCount = json.getJSONObject("status").getString("updateCount");

					log.verbose("Output Status : " + strStatus + " | Output Update Count : " + strUpdateCount);
				} else {
					errorCode = "ACAD_ERROR_001";

					ex.setErrorCode(errorCode);
					ex.setErrorDescription(errorDescription);
					ex.setAttribute("ErrorMessage", "Webservice O/P doesn't have expected format to parse.");

					throw ex;
				}

				// Check if the Status is Success and the DB Update Count is 1. if yes, Do
				// Nothing. Else, Throw Error.
				if (!strStatus.equalsIgnoreCase(AcademyConstants.VALUE_SUCCESSS)
						|| !strUpdateCount.equalsIgnoreCase(AcademyConstants.STR_ONE)) {

					errorCode = "ACAD_ERROR_001";

					ex.setErrorCode(errorCode);
					ex.setErrorDescription(errorDescription);
					ex.setAttribute("ErrorMessage", "Update to WCS Failed : ResponseStatus : " + strStatus
							+ " : UpdateCount : " + strUpdateCount);

					throw ex;
				}

			} else {

				errorCode = "ACAD_ERROR_001";
				ex.setErrorCode(errorCode);
				ex.setErrorDescription(errorDescription);
				ex.setAttribute("ErrorMessage", "Webservice Call Failed with Error Response Code : "
						+ httpCon.getResponseCode() + "ResponseMessage : " + httpCon.getResponseMessage());
				throw ex;
			}

		} catch (IOException e) {
			log.verbose(e.toString());

			errorCode = "ACAD_ERROR_001";

			ex.setErrorCode(errorCode);
			ex.setErrorDescription(errorDescription);
			ex.setAttribute("ErrorMessage", e.toString());

			throw ex;

		} catch (JSONException e) {
			log.verbose(e.toString());

			errorCode = "ACAD_ERROR_001";

			ex.setErrorCode(errorCode);
			ex.setErrorDescription(errorDescription);
			ex.setAttribute("ErrorMessage", e.toString());

			throw ex;

		} catch (TransformerFactoryConfigurationError e) {
			log.verbose(e.toString());

			errorCode = "ACAD_ERROR_001";

			ex.setErrorCode(errorCode);
			ex.setErrorDescription(errorDescription);
			ex.setAttribute("ErrorMessage", e.toString());

			throw ex;
		}
		/*
		 * OMNI-34707  : BEGIN
		 * 
		 * The below code change is added to publish the update to Yantriks whenever there is a flag update for BOPIS fulfillment type
		 * for a particular location. 
		 */
		log.verbose("Sending  Yantriks Fulfillment Update for BOPIS to Queue");
		inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_FLAG_UPDATE, AcademyConstants.V_FULFILLMENT_TYPE_BOPIS);
		AcademyUtil.invokeService(env, AcademyConstants.SERVICE_YANTRIKS_LOCATION_FULFILLMENT_UPDATE, inXML);
		inXML.getDocumentElement().removeAttribute(AcademyConstants.ATTR_FLAG_UPDATE);
		log.verbose("BOPIS Message Published to Queue");
		// * OMNI-34707  : END
		
		
		log.endTimer("AcademyBopisStoreEligibility::BopisStoreEligibility");
		log.verbose("Output XML for AcademyBopisStoreEligibility ==> " + XMLUtil.getXMLString(inXML));

		return inXML;
	}
}