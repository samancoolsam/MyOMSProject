package com.academy.ecommerce.sterling.bopis.planogram;

import java.util.Properties;
import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.ibm.sterling.afc.jsonutil.PLTJSONUtils;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Sanchit
 *
 */

public class AcademyFetchPlanogramInfo {


	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademyFetchPlanogramInfo.class);
	/**This Method makes a Planogram WebService Call to fetch item location details and stamp these values in Extn Attribute in Shipment 
	 * Line level
	 * @param env
	 * @param inXML
	 * @return
	 */

	//Define properties to fetch argument value from service configuration
	private Properties props;
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}


	public  Document fetchPlanogramInfo (YFSEnvironment env, Document inXML) {

		log.beginTimer("AcademyFetchPlanogramInfo::fetchPlanogramInfo");
		log.verbose("Entering the method AcademyFetchPlanogramInfo.fetchPlanogramInfo ");
		log.verbose("Input XML for AcademyFetchPlanogramInfo : =" +XMLUtil.getXMLString(inXML));

		

		Element eleShipment=inXML.getDocumentElement();
		String strShipNode=eleShipment.getAttribute(AcademyConstants.SHIP_NODE);
		String strStoreInDB=eleShipment.getAttribute(AcademyConstants.ATTR_STOREINDB);


		String strAppId=props.getProperty(AcademyConstants.STR_APP_ID);
		String strAppName=props.getProperty(AcademyConstants.STR_APP_NAME);
		String strEndPointURL=props.getProperty(AcademyConstants.STR_END_POINT_URL);

		NodeList nlShipmentLine=eleShipment.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

		for (int i = 0; i < nlShipmentLine.getLength(); i++) { 
			
			ResponseEntity<String> retryResponse = null;
			Element eleShipmentLine = (Element) nlShipmentLine.item(i);
			String strItemId=eleShipmentLine.getAttribute(AcademyConstants.ITEM_ID);
			Element eleExtnShipment = SCXmlUtil.createChild(eleShipmentLine, AcademyConstants.ELE_EXTN);
			String retryResponseBody="";
			String retryResponseCode="";

			//Prepare Input for Planogram WebService call
			try{
				JSONObject payload = new JSONObject();
				JSONObject itemlocation = new JSONObject();
				itemlocation.put(AcademyConstants.JSON_ELE_APPID, strAppId);
				itemlocation.put(AcademyConstants.JSON_ELE_APPNAME, strAppName);
				itemlocation.put(AcademyConstants.JSON_ELE_STOREID, Integer.parseInt(strShipNode));
				itemlocation.put(AcademyConstants.JSON_ELE_ITEMID, strItemId);
				payload.put(AcademyConstants.JSON_OBJ_ITEMLOCATION, itemlocation);
				log.verbose("Payload::" + payload);


				final HttpEntity<String> request = new HttpEntity<String>(payload.toString());
				SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();


				RestTemplate restTemplate = new RestTemplate(requestFactory);


				retryResponse = restTemplate.exchange(strEndPointURL, HttpMethod.POST, request,String.class);
				retryResponseBody = retryResponse.getBody();
                retryResponseCode = retryResponse.getStatusCode().toString();
			}catch(Exception e){
				log.error(e);
				eleExtnShipment.setAttribute(AcademyConstants.ATTR_EXTN_PLANOGRAM_STATUS, AcademyConstants.VAL_SERVICE_NOT_REACHABLE);
			}



			fetchLocationDetails(eleExtnShipment, retryResponseBody, retryResponseCode);
			
		}

		if((AcademyConstants.STR_YES).equalsIgnoreCase(strStoreInDB))
		{
			log.verbose("Input to Call ChangeShipment API : =" +XMLUtil.getXMLString(inXML));
			try {
				AcademyUtil.invokeService(env,AcademyConstants.SERVICE_ACADEMY_CHANGE_SHIPMENT,inXML);
			} catch (Exception e) {
				log.error(e);
			}
		}
		log.endTimer("AcademyFetchPlanogramInfo::fetchPlanogramInfo");
		log.verbose("Output XML for AcademyFetchPlanogramInfo : =" +XMLUtil.getXMLString(inXML));
		return inXML;

	}


	
	
	
	/**This Method makes a Planogram WebService Call to fetch item location details and stamp these values in Extn Attribute in Shipment 
	 * Line level
	 * @param eleExtnShipment
	 * @param retryResponseBody
	 * @param retryResponseCode
	 * @return
	 */
	private void fetchLocationDetails(Element eleExtnShipment, String retryResponseBody, String retryResponseCode) {
		
		if(retryResponseCode.equalsIgnoreCase(AcademyConstants.SUCCESS_CODE))
		{
			Document docPlanogramoutputResponse = null;
			try {
				docPlanogramoutputResponse = PLTJSONUtils.getXmlFromJSON(retryResponseBody, "Response");
			} catch (JSONException e) {
				log.error(e);
			}
			if(!YFCCommon.isVoid(docPlanogramoutputResponse))
			{
				Element eleResponse=docPlanogramoutputResponse.getDocumentElement();
				Element eleLocation = (Element) eleResponse.getElementsByTagName("location").item(0);

				if(!YFCCommon.isVoid(eleLocation))
				{
					eleExtnShipment.setAttribute(AcademyConstants.ATTR_EXTNPOGID,eleLocation.getAttribute(AcademyConstants.ATTR_POGID));
					eleExtnShipment.setAttribute(AcademyConstants.ATTR_EXTNDEPARTMENT,eleLocation.getAttribute(AcademyConstants.ATTR_DEPT));
					eleExtnShipment.setAttribute(AcademyConstants.ATTR_EXTNSECTION,eleLocation.getAttribute(AcademyConstants.ATTR_SEC));
					eleExtnShipment.setAttribute(AcademyConstants.ATTR_EXTNPOGNUMBER,eleLocation.getAttribute(AcademyConstants.ATTR_POGNUM));
					String strlivedate=eleLocation.getAttribute(AcademyConstants.ATTR_lIVEDATE);
					String strtrimlivedate=strlivedate.trim();
					String strSublivedate = strtrimlivedate.substring(0, 10);
					eleExtnShipment.setAttribute(AcademyConstants.ATTR_EXTNLIVEDATE,strSublivedate);
					eleExtnShipment.setAttribute(AcademyConstants.ATTR_EXTN_PLANOGRAM_STATUS, AcademyConstants.VAL_LOCATION_AVAILABLE);
					
				}
				else
				{
					eleExtnShipment.setAttribute(AcademyConstants.ATTR_EXTN_PLANOGRAM_STATUS, AcademyConstants.VAL_LOCATION_EMPTY);
				}
			} 
		}
	}

}

