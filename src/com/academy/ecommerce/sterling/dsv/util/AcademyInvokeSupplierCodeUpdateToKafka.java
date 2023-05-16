package com.academy.ecommerce.sterling.dsv.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyProcessKafkaWebserviceUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.ibm.sterling.afc.jsonutil.PLTJSONUtils;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**#########################################################################################
*
* Project Name                : DSV
* Author                      : Fulfillment POD
* Author Group				  : DSV
* Date                        : 26-FEB-2023 
* Description				  : This class stamp the VendorID in the header level.
* 								 
* ---------------------------------------------------------------------------------
* Date            	Author         		Version#       		Remarks/Description                      
* ---------------------------------------------------------------------------------
* 27-Feb-2023		Everest  	 		1.0           		Initial version
*
* #########################################################################################*/

public class AcademyInvokeSupplierCodeUpdateToKafka implements YIFCustomApi {
	
	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyInvokeSupplierCodeUpdateToKafka.class);
	
	AcademyProcessKafkaWebserviceUtil acadKafkaWebserviceUtil = new AcademyProcessKafkaWebserviceUtil();

	
	/**
	 * This method prepares the API input to send the Supplier Code details to Firestarter DB 
	 * via Kafka Rest API 
	 * 
	 * @param env
	 * @param inDoc
	 * @return docOutput
	 */
	public Document prepareAndInvokeSupplierUpdateToKafka(YFSEnvironment env, Document inDoc) throws YFSException, Exception {
		logger.beginTimer("AcademyInvokeSupplierCodeUpdateToKafka.prepareAndInvokeSupplierUpdateToKafka method ");
		logger.verbose("Input to prepareAndInvokeSupplierUpdateToKafka" + XMLUtil.getXMLString(inDoc));
		
		Element eleOrganization = inDoc.getDocumentElement();
		String eleOrganizationCode = eleOrganization.getAttribute(AcademyConstants.ORGANIZATION_CODE);
		String strOrganizationName = eleOrganization.getAttribute(AcademyConstants.ORGANIZATION_NAME);
		
		if(YFCObject.isVoid(strOrganizationName)) {
			Document docGetOrganizationList = getOrganizationList(env, eleOrganizationCode);
			strOrganizationName = XPathUtil.getString(docGetOrganizationList, "/OrganizationList/Organization/@OrganizationName");
		}
		else {
			logger.verbose("None of the conditions are satisfied. So skipping getOrganizaitonList API");
		}

		logger.verbose(": eleOrganizationCode : " + eleOrganizationCode + ": strOrganizationName : " + strOrganizationName);
		
		Document docResponse = acadKafkaWebserviceUtil.invokeKafkaWebserviceApi(env, "academy.kafka.suppliercode", 
				prepareSupplierUpdateJson(eleOrganizationCode, strOrganizationName).toString(), "KafkaSupplierCode");
		
		String strStatus = docResponse.getDocumentElement().getAttribute(AcademyConstants.ATTR_STATUS);
		String strResponseCode = docResponse.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_CODE);
		String strResponseMessage = docResponse.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_MESSAGE);
		
		//Success REsponse for Token 
		if (!YFCObject.isVoid(strStatus) && AcademyConstants.STR_SUCCESS.equals(strStatus)) {
			logger.verbose("strResponse String = " + strStatus + " : Response Code as "+strResponseCode
					+ " : Response Message as "+strResponseMessage);			
		}
		else if(!YFCObject.isVoid(strStatus) && (AcademyConstants.STR_RETRY.equals(strStatus)
				|| AcademyConstants.STATUS_CODE_ERROR.equals(strStatus))) {
			//Creating info logger for Splunk alerts 
			logger.info("Kafka SupplierCode Error :: Error While trying update Supplier Code :: ResponseCode :: " 
					+ strResponseCode + " :: and ResponseMessage :: " + strResponseMessage
					+ " :: and OMS Configured Exception handling is :: " + strStatus);
			
			YFSException yfsTokenExec = new YFSException();
			yfsTokenExec.setErrorCode("KAFKA_102");
			yfsTokenExec.setErrorDescription(strStatus + " in Kafka Supplier Update. Response : " + strResponseCode + " : Message :" + strResponseMessage);
			throw yfsTokenExec;
		}
		else {
			//Creating info logger for Splunk alerts 
			logger.info("Kafka SupplierCode Error :: Error While trying to update Supplier Code:: ResponseCode :: " 
					+ strResponseCode + " :: and ResponseMessage :: " + strResponseMessage
					+ " :: and Exception Type not in COP :: " + strStatus);

			YFSException yfsTokenExec = new YFSException();
			yfsTokenExec.setErrorCode("KAFKA_102");
			yfsTokenExec.setErrorDescription("ERROR in Kakfa Supplier Code Update. Response : " + strResponseCode);
			throw yfsTokenExec;
		}
		
		eleOrganization.setAttribute(AcademyConstants.ATTR_RESPONSE, docResponse.getDocumentElement().getAttribute(AcademyConstants.ATTR_STATUS));
		
		logger.endTimer("AcademyInvokeSupplierCodeUpdateToKafka.prepareAndInvokeSupplierUpdateToKafka method ");
		return inDoc;
	}

	/**
	 * This method is creates a webservice for the Supplier Update to Kafka
	 * 
	 * @param env
	 * @return shipmentConfDoc
	 * @throws Exception
	 */
	private JSONObject prepareSupplierUpdateJson(String eleOrganizationCode, String strOrganizationName) throws Exception {
		logger.beginTimer("AcademyInvokeSupplierCodeUpdateToKafka.prepareSupplierUpdateJson method ");
		JSONObject jsonRoot = new JSONObject();

		try {
			JSONArray jsonRecords = new JSONArray();
			JSONObject jsonKey = new JSONObject();
			JSONObject jsonUPC = new JSONObject();

			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_2);
			String strUpc = sdf.format(cal.getTime());
			
			jsonUPC.put(AcademyConstants.ATTR_UPC, strUpc); //Contains a Unique Random Value
			jsonKey.put(AcademyConstants.JSON_ATTR_KEY, jsonUPC);

			JSONObject jsonVendorInfo = new JSONObject();

			jsonVendorInfo.put("vendor_name", strOrganizationName);
			jsonVendorInfo.put("vendor_number", eleOrganizationCode);

			jsonKey.put(AcademyConstants.JSON_ATTR_VALUE,jsonVendorInfo);
			jsonRecords.add(jsonKey);
			jsonRoot.put("records", jsonRecords);

			logger.verbose("Final JSON to Kafka--->" + jsonRoot.toString());			
		}
		catch (YFSException yfsEx) {
			throw yfsEx;
		}
		catch (Exception exp) {
			//Creating info logger for Splunk alerts 
			logger.info("Kafka Error :: Exception while preparing SupplierCode input ::" + 
					eleOrganizationCode	+ " and " + strOrganizationName +". Error Trace :: "+exp.getStackTrace());

			YFSException yfsExecPOCreate = new YFSException();
			yfsExecPOCreate.setErrorCode("KAFKA_200");
			yfsExecPOCreate.setErrorDescription("Error while trying to create the Kafka Input :: " + exp.getMessage());
			throw yfsExecPOCreate;
		}
		logger.endTimer("AcademyInvokeSupplierCodeUpdateToKafka.prepareSupplierUpdateJson method ");
		return jsonRoot;
	}

	
	/**
	 * @param env
	 * @param strOrganizationCode
	 * @return Document
	 * @throws Exception
	 * 
	 * This method Invokes the getOrganizationList to get the details of the Org
	 * 
	 */ 
	private Document getOrganizationList(YFSEnvironment env, String strOrganizationCode) throws Exception {
		logger.beginTimer("AcademyInvokeSupplierCodeUpdateToKafka.getOrganizationList method ");

		Document docGetOrgListInp = XMLUtil.createDocument(AcademyConstants.ELE_ORG);
		Element eleOrganizationInp = docGetOrgListInp.getDocumentElement();
		eleOrganizationInp.setAttribute(AcademyConstants.ORG_CODE_ATTR, strOrganizationCode);

		String strAuditListTemplate = "<OrganizationList><Organization OrganizationCode='' OrganizationName='' XrefOrganizationCode='' >"
				+ "<Node NodeType='' /></Organization></OrganizationList>";

		env.setApiTemplate(AcademyConstants.API_GET_ORGANIZATION_LIST, XMLUtil.getDocument(strAuditListTemplate));
		Document docGetOrganizationListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORGANIZATION_LIST, docGetOrgListInp);
		env.clearApiTemplate(AcademyConstants.API_GET_ORGANIZATION_LIST);

		logger.endTimer("AcademyInvokeSupplierCodeUpdateToKafka.getOrganizationList method ");
		return docGetOrganizationListOut;
	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
