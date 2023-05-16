/**************************************************************************
 * Description	    : This class is invoked by integ server to handle the async updates .
 * 
 * Input xml to the class depends upon the requirement. Few samples are below :
 *	<Supply ItemID="112140737" NodeType="DC" Quantity="-1" ShipNode="711" SupplyType="SOH" UnitOfMeasure="EACH"/>
 * <Demand DocumentType="0001" EnterpriseCode="Academy_Direct" OrderHeaderKey="202103300601383652470257" OrderNo="SO_STS_Z3003_04">
 * <AvailabilitySnapShot EnableDSVSupplyUpdate="Y" EnableProcessAvailabilitySnapshot="Y">
   <ShipNode ShipNode="629">
   <Item InventoryOrganizationCode="DEFAULT" ItemID="121252247" ProductClass="GOOD" UnitOfMeasure="EACH">
   <AvailabilityDetails Quantity="0" SupplyType="ONHAND"/>
   </Item>
   </ShipNode>
   </AvailabilitySnapShot>
 * --------------------------------
 * 	Date             Author               
 * --------------------------------
 *  6-April-2021      Cognizant			 	 
 * 
 * -------------------------------------------------------------------------
 **************************************************************************/

package com.academy.ecommerce.yantriks.inventory;

import static com.academy.util.constants.AcademyConstants.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantriks.yih.adapter.util.YantriksCommonUtil;

import org.apache.commons.json.JSONException;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantriks.yih.adapter.util.YantriksConstants;

public class AcademyKafkaDeltaUpdate {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyKafkaDeltaUpdate.class);

	public Document kafkaInvDeltaUpdate(YFSEnvironment env, Document docInXml) throws ParserConfigurationException, Exception {
		Element eleRoot = docInXml.getDocumentElement();
		String strRootName = eleRoot.getNodeName();
		log.verbose("RootName is " + strRootName);
		/*  Below transaction object is set to make sure yantrix needs to be updated for demand update.
		 *  This transaction object is also set at different places so that the check will happen before publishing demand
		 * update to Yantriks
		 */
		env.setTxnObject(ATTR_SCH_REL_PROCESSED, STR_YES);
		
		/*OMNI-48035 :Start Change - Setting a transaction object to determine demand update is invoked from Async Server*/
		env.setTxnObject(DMD_UPDT_ON_ASYNC, STR_YES);
		/*OMNI-48035 :End Change*/

		if (E_SUPPLY.equalsIgnoreCase(strRootName)) {
			publishSupplyDetailsToYantriks(env, eleRoot);
		} else if (AcademyConstants.ELE_DEMAND.equalsIgnoreCase(strRootName)) {
			log.verbose("Invoking Common Service to Publish Demand details for entire order");
			String strReadyToShip = eleRoot.getAttribute("ReadyToShipFromWebStore");
			if (!YFCCommon.isVoid(strReadyToShip) && strReadyToShip.equals("Y")) {
				env.setTxnObject("ReadyToShipFromWebStore", "Y");
			}else {
				env.setTxnObject("ReadyToShipFromWebStore", "N");
			}
			AcademyUtil.invokeService(env, SERVICE_YANTRIKS_DEMAND_UPDATE, docInXml);
		} else if (ELE_LOC_FULFILLMENT.equals(strRootName)) {
			publishLocationFulfillment(env, docInXml);
		}
		/* OMNI-22625: Start Change - Added to handle location updation to Yantriks */
		else if (ORG_ELEMENT.equalsIgnoreCase(strRootName)) {
			postLocationOnboardingDetailsToYantriks(env,docInXml);
		}
		/* OMNI-22625: End Change */

		
		/* OMNI-34709: Start Change - Added to handle node control entry to yantriks */
		else if (AcademyConstants.ELE_BOPIS_NODE_CONTROL.equalsIgnoreCase(strRootName)) {

			log.verbose("Bopis NC RootElement :: " + eleRoot);

			JSONObject pickInput = prepareNodeControlJson( eleRoot);
			callYantrikNodeControlApi(pickInput, env);
		} 
		else if (AcademyConstants.ELE_SHARED_NODE_CONTROL.equalsIgnoreCase(strRootName)) {

			log.verbose("Shared Inv NC RootElement :: " + eleRoot);

			JSONObject stsInput = prepareNodeControlJson(eleRoot);
			callYantrikNodeControlApi(stsInput, env);
		}	
		/* OMNI-34709: End Change */

		/* OMNI-34708: Start Change - DSV OOS Supply Update*/
		else if(AcademyConstants.ELE_AVAILABLE_SNAPSHOT.equals(strRootName))
		{
			populateSupplyDetailsToYantriks(env,docInXml);
		}
		/* OMNI-34708: End Change - DSV OOS Supply Update*/
		
		return docInXml;
	}

	private void publishSupplyDetailsToYantriks(YFSEnvironment env, Element eleRoot) throws JSONException {
		log.verbose("Preparing Supply Update Kafka JSON");
		String strItemID = eleRoot.getAttribute(ATTR_ITEM_ID);
		String strItemUom = eleRoot.getAttribute(ATTR_UOM);
		String strShipNode = eleRoot.getAttribute(ATTR_SHIP_NODE);
		String strNodeType = eleRoot.getAttribute(ATTR_NODE_TYPE);
		String strSupplyType = eleRoot.getAttribute(ATTR_SUPPLY_TYPE);
		String strQuantity = eleRoot.getAttribute(ATTR_QUANTITY);
		double dblQty = Double.parseDouble(strQuantity);
		JSONObject input = new JSONObject();
		input.put(A_IS_FULLYQUALIFIED_TOPIC_NAME, false);
		input.put(A_OPERATION, STR_CREATE);
		input.put(A_TOPIC, V_CUSTOM_INV_FEED_UPDATE);
		JSONObject jKey = new JSONObject();
		jKey.put(JSON_ATTR_ORG_ID, ORG_ID_ACADEMY);
		jKey.put(JSON_ATTR_PRODUCT_ID, strItemID);
		jKey.put(JSON_ATTR_UOM, strItemUom);
		jKey.put(YIH_LOCATION_ID, strShipNode);
		jKey.put(JSON_ATTR_LOCATION_TYPE, strNodeType.toUpperCase());
		input.put(JSON_ATTR_KEY, jKey);
		JSONObject jValue = new JSONObject();
		jValue.put(JSON_ATTR_EVENT_TYPE, V_SUPPLY_UPDATE);
		jValue.put(JSON_ATTR_FEED_TYPE, V_DELTA);
		jValue.put(YIH_LOCATION_ID, strShipNode);
		jValue.put(JSON_ATTR_LOCATION_TYPE, strNodeType.toUpperCase());
		jValue.put(JSON_ATTR_ORG_ID, ORG_ID_ACADEMY);
		jValue.put(JSON_ATTR_PRODUCT_ID, strItemID);
		jValue.put(JSON_ATTR_UOM, strItemUom);
		SimpleDateFormat formatter = new SimpleDateFormat(YAS_TIME_STAMP_FORMAT, Locale.US);
		jValue.put(JSON_ATTR_UPDATE_TIME_STAMP, YantriksCommonUtil.getCurrentDateOrTimeStamp(formatter));
		input.put(JSON_ATTR_VALUE, jValue);
		JSONObject jAudit = new JSONObject();
		jAudit.put(JSON_ATTR_TXN_ID, V_INVSC);
		jAudit.put(JSON_ATTR_TXN_REASON, V_SKULOC);
		jAudit.put(JSON_ATTR_TXN_SYSTEM, V_INV_SERVICE_CONSUMER);
		jAudit.put(JSON_INP_TRANSACTION_TYPE, V_SUPPLY_FEED);
		jAudit.put(JSON_ATTR_TXN_USER, V_OMS);
		jValue.put(JSON_ATTR_AUDIT, jAudit);
		JSONObject jTo = new JSONObject();
		JSONArray jArraySupplyTypes = new JSONArray();
		JSONObject jSupply = new JSONObject();
		jSupply.put(JSON_ATTR_SUPPLY_TYPE, strSupplyType);
		jSupply.put(JSON_ATTR_QUANTITY, dblQty);
		jArraySupplyTypes.add(jSupply);
		jTo.put(JSON_ATTR_SUPPLY_TYPES, jArraySupplyTypes);
		jValue.put(JSON_ATTR_TO, jTo);
		log.verbose("Publishing supply feed to Yantrix : " + input.toString());

		YantriksCommonUtil.callYantriksAPI(YFSSystem.getProperty(AcademyConstants.URL_YANTRIK_KAFKA_UPDATE),
				YIH_HTTP_METHOD_POST, input.toString(), YantriksCommonUtil.getAvailabilityProduct(), env);
		// Modified the method argument to pass environment object as part of OMNI-16848
		//		log.verbose("Response :: " + strResponse);
		//		if (V_FAILURE.equalsIgnoreCase(strResponse)) {
		//			throw new YFSException("Yantriks call Failed", "Acad_01", "Yantriks call Failed");
		//		}
	}

	/* OMNI-22625: Start Change - New Method created to form the location onboarding JSON Input and post to Yantriks */
	private void postLocationOnboardingDetailsToYantriks(YFSEnvironment env, Document docInXml) throws Exception  {
		String methodName = "postLocationOnboardingDetailsToYantriks";
		log.beginTimer(methodName);
		log.verbose("Input to " + methodName + " : " + SCXmlUtil.getString(docInXml));
		
		/*OMNI-47902 - Start Change */
		String onboardLocation="Y";
		Element rootEleOrg =docInXml .getDocumentElement();
		Element nodeElement= SCXmlUtil.getChildElement(rootEleOrg,ELE_NODE);
		
		// Sample for DSV from JMeter
		/*<Organization LocaleCode="en_US_CST" OrganizationCode="GENESCO INC. DBA LEVI'S" >
	    <CorporatePersonInfo AddressLine1="6301 Imperial Drive" AddressLine2=" " City="Waco" Country="US" State="TX" ZipCode="76712"/>
	    <SubOrganization>
	    <Organization LocaleCode="en_US_CST" OrganizationCode="4233">
	    <CorporatePersonInfo AddressLine1="6301 Imperial Drive" AddressLine2=" " City="Waco" Country="US" State="TX" ZipCode="76712"/>
	    <Node NodeType="Drop Ship"/>
		</Organization>
	    </SubOrganization>
		</Organization> */
		
		//Sample for DSV from Config
		/*<Organization LocaleCode="en_US_CST" OrganizationCode="4233" >
	    <CorporatePersonInfo AddressLine1="6301 Imperial Drive" AddressLine2=" " City="Waco" Country="US" State="TX" ZipCode="76712"/>
	    <Node NodeType="Drop Ship"/>
		</Organization> */
		
		// Sample for Store from Config 
		/*<Organization LocaleCode="en_US_EST" OrganizationCode="116" >
	    <CorporatePersonInfo AddressLine1="Marine Drive" AddressLine2="First Cross Street" City="Houston" Country="US" State="HX" ZipCode="99061"/>
	    <Node NodeType="Store"/>
		</Organization> */
		
		/*
		 * This condition check is used to determine if the node element has to be
		 * fetched from the root Org element or from within the SubOrg element
		 */
		if (YFCObject.isVoid(nodeElement)) {
			rootEleOrg = (Element) XPathUtil.getNode(docInXml, XPATH_SUBORG_ORG);
			if (!YFCObject.isVoid(rootEleOrg)) { // if Node element is present within the SubOrg element then it means we have a child node within the parent Org
				nodeElement = SCXmlUtil.getChildElement(rootEleOrg, ELE_NODE);
				// If we do not get the Node Element <Node NodeType="Drop Ship"/> even within
				// SubOrg element , then we set the Flag as N to skip the location onboarding
				if (YFCObject.isVoid(nodeElement)) {
					onboardLocation = "N";
				}
			} else { // This else block is for handling a scenario when just a parent Org is created
						// where Node Element is neither present at the root Org Level nor at the
						// SubOrg level , in this case we set the Flag as N to skip the location
						// onboarding
				onboardLocation = "N";
			}
		}
		
		/*Handles location onboarding for a node if just a node is created and also if a node is created along with a parent only if the onboardLocation flag is set as Y */
		if(STR_YES.equalsIgnoreCase(onboardLocation)) {
		/*OMNI-47902 - End Change */
		String locationId=rootEleOrg.getAttribute(ORGANIZATION_CODE);
		String inpLocaleCode=rootEleOrg.getAttribute(LOCALE_CODE);
		log.verbose("locationId:"+locationId+"inpLocaleCode:"+inpLocaleCode);
		Document docInXmlgetCommonCodeList=XmlUtils.createDocument(ELE_COMMON_CODE);
		Element eleGetCommonCodeList=docInXmlgetCommonCodeList.getDocumentElement();
		eleGetCommonCodeList.setAttribute(ATTR_COMMON_CODE_VALUE, inpLocaleCode);
		Document outDocgetCommonCodeList = YantriksCommonUtil.invokeAPI(env, TEMPLATE_GET_COMMON_CODE_LIST,API_GET_COMMONCODE_LIST, docInXmlgetCommonCodeList);
		String localeCode="";
		YFCDocument getCommonCodeListOutDoc = YFCDocument.getDocumentFor(outDocgetCommonCodeList);
		YFCElement rootEleGetCommonCodeList = getCommonCodeListOutDoc.getDocumentElement();
		if (!YFCCommon.isVoid(rootEleGetCommonCodeList)) {
			YFCNodeList<YFCElement> nCommonCodeElement= rootEleGetCommonCodeList.getElementsByTagName(ELE_COMMON_CODE);
			if (!YFCCommon.isVoid(nCommonCodeElement)) {
				YFCElement commonCodeElement = (YFCElement) nCommonCodeElement.item(0);
				if (!YFCCommon.isVoid(commonCodeElement)) {
					localeCode=commonCodeElement.getAttribute(ATTR_CODE_SHORT_DESC);
				}

			}

		}
		String strNodeType=nodeElement.getAttribute(ATTR_NODE_TYPE);
		log.verbose("nodeType:"+strNodeType);
		log.verbose("localeCode:"+localeCode);

		String locationType="";
		if(!YFCCommon.isVoid(strNodeType)) {
			if (strNodeType.equals(ATTR_VAL_SHAREDINV_DC)) {
				locationType=STR_DC;
			}
			else if(strNodeType.equals(STR_STORE)){
				locationType=STORE_VAL;	
			}
			else if (strNodeType.equals(DSV_NODE_TYPE)) {
				locationType=DSV_VAL;
			}
		}
		log.verbose("locationType:"+locationType);
		Element addressEle=SCXmlUtil.getChildElement(rootEleOrg,ELE_CORPORATE_PERSONAL_INFO);
		String addressLine1=addressEle.getAttribute(ATTR_ADDRESS_LINE_1);
		String addressLine2=addressEle.getAttribute(ATTR_ADDRESS_LINE_2);
		String city=addressEle.getAttribute(ATTR_CITY);
		String state=addressEle.getAttribute(ATTR_STATE);
		String country=addressEle.getAttribute(COUNTRY);
		String zipCode=addressEle.getAttribute(ZIP_CODE);

		JSONObject addressObject = new JSONObject();
		addressObject.put(JSON_ADDRESS_LINE_1, addressLine1);
		if(!YFCCommon.isVoid(addressLine2)) {
			log.verbose("addressLine2 Is Not Void");
			addressObject.put(JSON_ADDRESS_LINE_2, addressLine2);
		}
		addressObject.put(JSON_CITY, city);
		addressObject.put(JSON_STATE, state);
		addressObject.put(JSON_COUNTRY, country);
		addressObject.put(JSON_ZIPCODE, zipCode);

		JSONObject inputJson = new JSONObject();
		String orgId = ORG_ID;
		inputJson.put(JSON_ATTR_ORG_ID, orgId);
		inputJson.put(JSON_ATTR_LOCATION_TYPE, locationType);
		inputJson.put(YIH_LOCATION_ID, locationId);
		inputJson.put(JSON_ATTR_LOCATION_NAME, locationId);
		inputJson.put(JSON_ATTR_TRACK_CAPACITY, false);
		inputJson.put(JSON_ATTR_ENABLED,true);
		inputJson.put(JSON_ATTR_PRIORITY, 1.0);
		inputJson.put(JSON_ATTR_ADDRESS, addressObject);
		inputJson.put(JSON_ATTR_LOCALE, localeCode);
		SimpleDateFormat formatter = new SimpleDateFormat(YAS_TIME_STAMP_FORMAT, Locale.US);
		inputJson.put(JSON_ATTR_UPDATE_TIME, YantriksCommonUtil.getCurrentDateOrTimeStamp(formatter));
		inputJson.put(JSON_ATTR_UPDATE_USER,TRANS_TYPE);

		log.verbose("Publishing location Onboading details to Yantrix : " + inputJson.toString());


		String apiUrl=YFSSystem.getProperty(API_URL_LOCATION_ONBOARD);
		env.setTxnObject("postCallRequired", true);//setting transaction object as true since POST call is needed 
		/*Invoking the Yantriks API and fetch the response*/
		String strResponse = YantriksCommonUtil.callYantriksAPI(apiUrl,HTTP_METHOD_PUT, inputJson.toString(), YantriksCommonUtil.getAvailabilityProduct(),env);		
		log.verbose("Response from Yantriks on location Onboarding update :"+strResponse);
		}
		/*OMNI-47902 - Start Change */
		else {
			log.verbose("Location is not valid for onboarding to Yantriks");
		}
		/*OMNI-47902 - End Change */

	}
	/*OMNI-22625:End Change*/

	private void publishLocationFulfillment(YFSEnvironment env, Document docInXml) throws JSONException {
		log.verbose("Publishing Location Update");

		Element eleRoot = docInXml.getDocumentElement();
		String strIsLocationEnabled = eleRoot.getAttribute(ATTR_ENABLED);
		String strUpdateFlag = eleRoot.getAttribute(ATTR_FLAG_UPDATE);
		String strNodeTYpe = eleRoot.getAttribute(A_NODE_TYPE).toUpperCase();
		String strShipNode = eleRoot.getAttribute(ATTR_SHIP_NODE);
		String strFulfillmentType = eleRoot.getAttribute(ATTR_FULFILLMENT_TYPE);
		String strUrlFromProperties = YFSSystem.getProperty(URL_FULFILLMENT_TYPE_UPDATE);
		log.verbose("Location Flag  is " + strIsLocationEnabled + " for " + strUpdateFlag);
		if (STR_NO.equals(strIsLocationEnabled)) {
			for (int i = 0; i < 2; i++) {
				JSONObject update = new JSONObject();
				update.put(JSON_ATTR_ORG_ID, ORG_ID_ACADEMY);
				update.put(JSON_ATTR_LOCATION_TYPE, strNodeTYpe);
				update.put(YIH_LOCATION_ID, strShipNode);
				update.put(JSON_ATTR_SELLING_CHANNEL, SELLING_CHANNEL_GLOBAL);
				update.put(JSON_ATTR_FULFILLMENT_TYPE, strFulfillmentType);
				update.put(JSON_ATTR_UPDATE_USER, TRANS_TYPE);
				if (i == 0) {
					update.put(JSON_INP_TRANSACTION_TYPE, TRANS_TYPE_WCS);
				} else {
					update.put(JSON_INP_TRANSACTION_TYPE, TRANS_TYPE);
				}

				log.verbose("Publishing LocationFulfillment update to yantriks");
				log.verbose(strUrlFromProperties);
				log.verbose(update.toString());
				YantriksCommonUtil.callYantriksAPI(strUrlFromProperties, YIH_HTTP_METHOD_POST,
						update.toString(), YantriksCommonUtil.getAvailabilityProduct(), env);
			}
		} else {

			for (int i = 1; i <= 2; i++) {
				String strUrl = strUrlFromProperties + STR_SLASH + ORG_ID + STR_SLASH + strNodeTYpe + STR_SLASH
						+ strShipNode + STR_SLASH + SELLING_CHANNEL_GLOBAL + STR_SLASH + strFulfillmentType;
				/** OMNI-51486 BEGIN
				 * OMS should send location fulfillment type update to Yantriks without transactionType when it is enabled in OMS.
				 * 
						+ STR_QUESTION_MARK;
				if (i == 1) {
					strUrl = strUrl + STR_TRANS_TYPE_WCS;
				} else {
					strUrl = strUrl + STR_TRANS_TYPE_OMS;
				}
				
				OMNI-51486 END
				**/
				log.verbose("No Transaction Type should be appended to URL");
				log.verbose("\nInvoking URL to delete the Fulfillment Type::: " + strUrl);
				YantriksCommonUtil.callYantriksAPI(strUrl, YIH_HTTP_METHOD_DELETE,
						STR_EMPTY_STRING, YantriksCommonUtil.getAvailabilityProduct(), env);
			}

		}
	}

	private JSONObject prepareNodeControlJson(Element eleRoot) throws JSONException {

		String itemId = eleRoot.getAttribute(AcademyConstants.ATTR_ITEM_ID);
		String locId = eleRoot.getAttribute(AcademyConstants.YIH_LOCATION_ID);
		String locType = eleRoot.getAttribute(AcademyConstants.JSON_ATTR_LOCATION_TYPE);

		JSONObject nodeControlJson = new JSONObject();

		nodeControlJson.put(AcademyConstants.JSON_ATTR_ORG_ID, AcademyConstants.ORG_ID_ACADEMY);
		nodeControlJson.put(AcademyConstants.JSON_ATTR_PRODUCT_ID, itemId);
		nodeControlJson.put(AcademyConstants.JSON_ATTR_UOM, AcademyConstants.UNIT_OF_MEASURE);
		nodeControlJson.put(AcademyConstants.JSON_ATTR_LOCATION_TYPE, locType);
		nodeControlJson.put(AcademyConstants.YIH_LOCATION_ID , locId);
		nodeControlJson.put(AcademyConstants.JSON_ATTR_SUPPLY_TYPE, AcademyConstants.STR_SUPP_TYPE_VAL);
		nodeControlJson.put(AcademyConstants.TEMPORARY_DISABLE_EXP_TIME, addHoursToTime());
		nodeControlJson.put(AcademyConstants.JSON_ATTR_UPDATE_USER, AcademyConstants.V_OMS);
		log.verbose("Node Control json to Yantrik : " + nodeControlJson);

		return nodeControlJson;


	}

	/**
	 * This method will add config. x hours to current UTC time
	 * @return
	 */
	public static String addHoursToTime() {
		String timeZone = "UTC";
		int tdetConfigHrs = 0;
		SimpleDateFormat formatter = new SimpleDateFormat(YAS_TIME_STAMP_FORMAT, Locale.US);
		String configHours = YFSSystem.getProperty(AcademyConstants.CONFIG_HOURS);
		if (YFCCommon.isVoid(configHours)) {
			configHours = AcademyConstants.DEFAULT_CONFIG_HOURS;
		}
		log.info("configHours ::"+configHours);

		tdetConfigHrs =  Integer.parseInt(configHours);
	
		formatter.setTimeZone(TimeZone.getTimeZone(timeZone));
		Date date = new Date();

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.HOUR, tdetConfigHrs);
		return formatter.format(c.getTime());
	}

	private void callYantrikNodeControlApi(JSONObject nodeControlReq,
			YFSEnvironment env) {
		String apiUrl = YFSSystem.getProperty(AcademyConstants.URL_YANTRIKS_NODE_CONTROL_UPDATE);
		env.setTxnObject("putCallRequired", true);//setting transaction object as true since PUT call is needed 
		YantriksCommonUtil.callYantriksAPI(apiUrl, YIH_HTTP_METHOD_POST, nodeControlReq.toString(), YantriksCommonUtil.getAvailabilityProduct(),env);
	}
	
	/* OMNI-34708: Start Change - DSV OOS Supply Update*/
	private void populateSupplyDetailsToYantriks(YFSEnvironment env, Document inDoc) throws JSONException {
		log.beginTimer("AcademyKafkaDeltaUpdate::populateSupplyDetailsToYantriks");
		log.info("AcademyKafkaDeltaUpdate.populateSupplyDetailsToYantriks() Input XML ::" + XMLUtil.getXMLString(inDoc));
		
		Element eleAvailabilitySnapshot = inDoc.getDocumentElement();
		Element eleShipNode = XmlUtils.getChildElement(eleAvailabilitySnapshot,AcademyConstants.ELE_SHIP_NODE);
		String strShipNode = eleShipNode.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
		Element eleItem = XmlUtils.getChildElement(eleShipNode,AcademyConstants.ELEM_ITEM);
		String strproductId = eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID);
		String strUOM = eleItem.getAttribute(AcademyConstants.ATTR_UOM);
		Element eleAvailabilityDetails = XmlUtils.getChildElement(eleItem,AcademyConstants.ATTR_AVAILABILITY_DETAILS);
		String strQuantity = eleAvailabilityDetails.getAttribute(AcademyConstants.ATTR_QUANTITY);
		String strSupplyType = eleAvailabilityDetails.getAttribute(AcademyConstants.ATTR_SUPPLY_TYPE);
		
		JSONObject adjustInventory = new JSONObject();
		adjustInventory.put(AcademyConstants.A_IS_FULLYQUALIFIED_TOPIC_NAME, AcademyConstants.STR_FALSE);
		JSONObject key=new JSONObject();
		key.put(AcademyConstants.JSON_ATTR_ORG_ID, AcademyConstants.ORG_ID_ACADEMY);
		key.put(AcademyConstants.JSON_ATTR_PRODUCT_ID,strproductId);
		key.put(AcademyConstants.JSON_ATTR_UOM, strUOM);
		key.put(AcademyConstants.YIH_LOCATION_ID,strShipNode);
		key.put(AcademyConstants.JSON_ATTR_LOCATION_TYPE,YantriksConstants.LT_DSV);
		adjustInventory.put(AcademyConstants.A_OPERATION, AcademyConstants.STR_CREATE);
		adjustInventory.put(AcademyConstants.A_TOPIC, AcademyConstants.V_INV_FEED_UPDATE);
		JSONObject value=new JSONObject();
		value.put(AcademyConstants.JSON_ATTR_EVENT_TYPE, AcademyConstants.V_SUPPLY_UPDATE);
		value.put(AcademyConstants.JSON_ATTR_FEED_TYPE, YantriksConstants.FEED_TYPE_VAL_ABSOLUTE);
		value.put(AcademyConstants.YIH_LOCATION_ID,strShipNode);
		value.put(AcademyConstants.JSON_ATTR_LOCATION_TYPE, YantriksConstants.LT_DSV);
		value.put(AcademyConstants.JSON_ATTR_ORG_ID, AcademyConstants.ORG_ID_ACADEMY);
		value.put(AcademyConstants.JSON_ATTR_PRODUCT_ID, strproductId);
		value.put(AcademyConstants.JSON_ATTR_UOM, strUOM);
		value.put(AcademyConstants.JSON_ATTR_QUANTITY,strQuantity);
		SimpleDateFormat formatter = new SimpleDateFormat(AcademyConstants.JSON_YAS_TIME_STAMP_FORMAT, Locale.US);
		String strcurrentDate = YantriksCommonUtil.getCurrentDateOrTimeStamp(formatter);
		value.put(AcademyConstants.JSON_ATTR_UPDATE_TIME_STAMP, strcurrentDate);	
		JSONObject to=new JSONObject();
		to.put(AcademyConstants.JSON_ATTR_SUPPLY_TYPE, strSupplyType);
		value.put(AcademyConstants.JSON_ATTR_TO,to);
		JSONObject audit=new JSONObject();
		audit.put(AcademyConstants.JSON_ATTR_TXN_ID, YantriksConstants.V_TXN_ID);
		audit.put(AcademyConstants.JSON_ATTR_TXN_REASON,YantriksConstants.V_TXN_RSN);
		audit.put(AcademyConstants.JSON_ATTR_TXN_SYSTEM, YantriksConstants.V_OMS);
		audit.put(AcademyConstants.JSON_INP_TRANSACTION_TYPE, YantriksConstants.V_TXN_TYPE);
		audit.put(AcademyConstants.JSON_ATTR_TXN_USER, YantriksConstants.V_OMS);
		value.put(AcademyConstants.JSON_ATTR_AUDIT,audit);
		adjustInventory.put(AcademyConstants.JSON_ATTR_KEY,key);
		adjustInventory.put(AcademyConstants.JSON_ATTR_VALUE, value);

		YantriksCommonUtil.callYantriksAPI(YFSSystem.getProperty(AcademyConstants.URL_YANTRIK_KAFKA_UPDATE),
				YIH_HTTP_METHOD_POST, adjustInventory.toString(), YantriksCommonUtil.getAvailabilityProduct(), env);
		
		log.endTimer("AcademyKafkaDeltaUpdate::populateSupplyDetailsToYantriks");
	}
	/* OMNI-34708: End Change - DSV OOS Supply Update*/
}