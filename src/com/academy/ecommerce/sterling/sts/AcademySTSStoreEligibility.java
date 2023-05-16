package com.academy.ecommerce.sterling.sts;

/*##################################################################################
*
* Project Name                : STS
* Module                      : OMS
* Author                      : CTS
* Date                        : 21-July-2020 
* Description				  : This class updaate STS Flag changes to WCS
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       Remarks/Description                      
* ---------------------------------------------------------------------------------
* 21-JULY-2020		CTS  	 			  1.0           	Initial version
* ##################################################################################*/

import java.io.*;
import java.net.*;
import java.util.Properties;

import javax.xml.transform.*;

import org.w3c.dom.*;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;


public class AcademySTSStoreEligibility {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSStoreEligibility.class);

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
	 * isSTSEligible Flag to Y or N.
	 * 
	 * @param env
	 * @param inXML
	 * @return inXML
	 * @throws Exception 
	 *
	 */

	public Document processSTSStoreEligibility(YFSEnvironment env, Document inXML) throws Exception {

		YFSException ex = new YFSException();

		log.beginTimer("AcademySTSStoreEligibility::processSTSStoreEligibility");
		log.verbose("Entering the method AcademySTSStoreEligibility.processSTSStoreEligibility ");
		log.verbose("Input XML for AcademySTSStoreEligibility ==> " + XMLUtil.getXMLString(inXML));
		
		String strWebServiceURL ="";
		
		Element eleOrganization = inXML.getDocumentElement();
		String strStoreID = eleOrganization.getAttribute(AcademyConstants.ORGANIZATION_CODE);

		Element eleExtn = (Element) eleOrganization.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
		String strIsSTSFlag = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_IS_STS_ENABLED);

		/* OMNI-48280 - Start - checking  if wcs sts2.0 funtionality  is turned off */
		String strSTSFlagForWCS = props.getProperty(AcademyConstants.STS_STORE_FLAG_FOR_WCS);
		log.verbose("STSStoreFlagForWCS" +strSTSFlagForWCS);
		if (!YFCObject.isVoid(strSTSFlagForWCS) && strSTSFlagForWCS.equalsIgnoreCase(AcademyConstants.STR_YES)){
		
			if (YFCObject.isVoid(strIsSTSFlag))
				strIsSTSFlag = AcademyConstants.STR_NO;
		}
		else if (!YFCObject.isVoid(strSTSFlagForWCS) && strSTSFlagForWCS.equalsIgnoreCase(AcademyConstants.STR_NO) ) {
			
			log.verbose("WCS flag is turned off ");
			if (strIsSTSFlag.equalsIgnoreCase(AcademyConstants.STR_YES) || strIsSTSFlag.equalsIgnoreCase(AcademyConstants.ATTR_B) || strIsSTSFlag.equalsIgnoreCase(AcademyConstants.ATTR_S))
			strIsSTSFlag = AcademyConstants.STR_ONE;
		else
			strIsSTSFlag = AcademyConstants.STR_ZERO;
		}
		/* OMNI-48280 - End  */


		// Get ErrorDescription from Service Arguments
		String errorDescription = props.getProperty("STSFlagErrorDescription",
				"Failed to Update Store STS Eligibility Attribute in GCP MySQL DB for Store Info.");
		String errorCode = "";

		log.verbose("OrganizationCode : " + strStoreID + ":: isSTSEnabledFlag : " + strIsSTSFlag);

		// Get URL from COP and append the I/P to it
		if (!YFCObject.isVoid(props.getProperty("STSFlagWebserviceURL"))) {
			
			strWebServiceURL = props.getProperty("STSFlagWebserviceURL").replace(AcademyConstants.ATTR_$STORE_ID, strStoreID);
			strWebServiceURL = strWebServiceURL.replace(AcademyConstants.ATTR_$STS_FLAG, strIsSTSFlag);
		}
		log.verbose(" Final WebServiceURL : " + strWebServiceURL);

		URL url = null;
		int iConnectTimeOut = Integer.parseInt(props.getProperty("ConnectTimeOut", "60000"));
		int iReadTimeOut = Integer.parseInt(props.getProperty("ReadTimeOut", "60000"));

		try {
			url = new URL(strWebServiceURL);
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod(AcademyConstants.WEBSERVICE_POST);

			httpCon.setRequestProperty(AcademyConstants.STR_CONTENT_LENGTH, AcademyConstants.STR_ZERO);
			httpCon.setConnectTimeout(iConnectTimeOut);
			httpCon.setReadTimeout(iReadTimeOut);
		
			OutputStream os = httpCon.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(props.getProperty("writerContent"));
			writer.flush();
			writer.close();
			os.close();

			log.verbose("ResponseCode : " + httpCon.getResponseCode());
			log.verbose("ResponseMessage : " + httpCon.getResponseMessage());

			// Check if the HTTP Response is 200. If yes, Check for Status. Else, Throw Error
			if (Integer.toString(httpCon.getResponseCode()).equalsIgnoreCase(AcademyConstants.SUCCESS_CODE)) {
				log.verbose("Success response ");
				BufferedReader br = new BufferedReader(new InputStreamReader((httpCon.getInputStream())));
				StringBuilder sb = new StringBuilder();
				String output;
				while ((output = br.readLine()) != null) {
					sb.append(output);
				}

				log.verbose(" Final String Builder " + sb.toString());
				JSONObject json = new JSONObject(sb.toString());

				String strStatus = "", strUpdateCount = "";

				/**
				 * Check if the O/P Json has the expected format.
				 * 
				 * { "status": { "status": "Success", "updateCount": "1" } }
				 **/
				
				if (json.has(AcademyConstants.STR_STATUS) 
						&& (json.getJSONObject(AcademyConstants.STR_STATUS).has(AcademyConstants.STR_STATUS)
						&& json.getJSONObject(AcademyConstants.STR_STATUS).has(AcademyConstants.STR_COUNT))) {

					strStatus = json.getJSONObject(AcademyConstants.STR_STATUS).getString(AcademyConstants.STR_STATUS);
					strUpdateCount = json.getJSONObject(AcademyConstants.STR_STATUS).getString(AcademyConstants.STR_COUNT);

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
		 * The below code change is added to publish the update to Yantriks whenever there is a flag update for STS fulfillment type
		 * for a particular location. For now STS update is not needed, but the service will be used to identify if the update is for any flag
		 * to make sure other updates are properly processed. 
		 */
		log.verbose("Sending  Yantriks Fulfillment Update for STS to Queue");
		inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_FLAG_UPDATE, AcademyConstants.V_FULFILLMENT_TYPE_STS);
		AcademyUtil.invokeService(env, AcademyConstants.SERVICE_YANTRIKS_LOCATION_FULFILLMENT_UPDATE, inXML);
		inXML.getDocumentElement().removeAttribute(AcademyConstants.ATTR_FLAG_UPDATE);
		log.verbose("STS Message Published to Queue");
		// OMNI-34707  : END
		
		log.endTimer("AcademySTSStoreEligibility::processSTSStoreEligibility");
		log.verbose("Output XML for processSTSStoreEligibility ==> " + XMLUtil.getXMLString(inXML));

		return inXML;
	}
}