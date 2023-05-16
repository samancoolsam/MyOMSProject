package com.academy.util.common;

//J2SE Imports
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSConnectionHolder;
import com.yantra.yfs.japi.YFSEnvironment;

/**
* Encapsulates set of utility methods used by the Academy Solution.
* @author ssankar
*/
public final class AcademyUtil {
  

	/**
	 * Logger Instance.
	 */
	private static Logger logger = Logger.getLogger(AcademyUtil.class.getName());

	// Utility Class - Mask Constructor
	private AcademyUtil() {
	}

	/**
	 * Instance of YIFApi used to invoke Yantra APIs or services.
	 */
	private static YIFApi api = null;

	private static Object strCarrierService;

	private static Object eleShipment;

	private static Object strScac;

	private static YFCDocument docChangeShipment;

	
	static {
		try {
			api = YIFClientFactory.getInstance().getApi();
		} catch (Exception e) {
			logger.error("IOM_UTIL_0001", e);
		}
	}

	/**
	 * Stores the object in the environment under a certain key.
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 * @param key
	 *            Key to identify object in environment.
	 * @param value
	 *            Object to be stored in the environment under the given key.
	 * @return Previous object stored in the environment with the same key (if
	 *         present).
	 */
	public static Object setContextObject(YFSEnvironment env, String key, Object value) {
		Object oldValue = null;
		Map map = env.getTxnObjectMap();
		if (map != null)
			oldValue = map.get(key);
		env.setTxnObject(key, value);
		return oldValue;
	}

	/**
	 * Retrieves the object stored in the environment under a certain key.
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 * @param key
	 *            Key to identify object in environment.
	 * @return Object retrieved from the environment under the given key.
	 */
	public static Object getContextObject(YFSEnvironment env, String key) {
		return env.getTxnObject(key);
	}

	/**
	 * Retrieves the property stored in the environment under a certain key.
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 * @param key
	 *            Key to identify object in environment.
	 * @return Poperty retrieved from the environment under the given key.
	 */
	public static String getContextProperty(YFSEnvironment env, String key) {
		String value = null;
		Object obj = env.getTxnObject(key);
		if (obj != null)
			value = obj.toString();
		return value;
	}

	/**
	 * Removes an object from the environment.
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 * @param key
	 *            Key to identify object in environment.
	 * @return The object stored in the environment under the specified key (if
	 *         any).
	 */
	public static Object removeContextObject(YFSEnvironment env, String key) {
		Object oldValue = null;
		Map map = env.getTxnObjectMap();
		if (map != null)
			oldValue = map.remove(key);
		return oldValue;
	}

	/**
	 * Clears the environment of any user objects stored.
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 */
	public static void clearContextProperties(YFSEnvironment env) {
		Map map = env.getTxnObjectMap();
		if (map != null) {
			map.clear();
		}
	}

	/**
	 * Determines if the input document is a SOAP Fault message.
	 * 
	 * @param doc
	 *            Input Document to check.
	 * @return true if SOAP Fault, false otherwise.
	 */
	/*
	 * public static boolean isFaultMessage(Document doc) { return
	 * isFaultMessage(doc.getDocumentElement()); }
	 */

	/**
	 * Invokes a Yantra API.
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 * @param apiName
	 *            Name of API to invoke.
	 * @param inDoc
	 *            Input Document to be passed to the API.
	 * @throws java.lang.Exception
	 *             Exception thrown by the API.
	 * @return Output of the API.
	 */
	public static Document invokeAPI(YFSEnvironment env, String apiName, Document inDoc) throws Exception {
		return api.invoke(env, apiName, inDoc);
	}

	/**
	 * Invokes a Yantra API.
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 * @param apiName
	 *            Name of API to invoke.
	 * @param str
	 *            Input to be passed to the API. Should be a valid XML string.
	 * @throws java.lang.Exception
	 *             Exception thrown by the API.
	 * @return Output of the API.
	 */
	public static Document invokeAPI(YFSEnvironment env, String apiName, String str) throws Exception {
		return api.invoke(env, apiName, YFCDocument.parse(str).getDocument());
	}

	/**
	 * Invokes a Yantra Service.
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 * @param serviceName
	 *            Name of Service to invoke.
	 * @param inDoc
	 *            Input Document to be passed to the Service.
	 * @throws java.lang.Exception
	 *             Exception thrown by the Service.
	 * @return Output of the Service.
	 */
	public static Document invokeService(YFSEnvironment env, String serviceName, Document inDoc) throws Exception {
		return api.executeFlow(env, serviceName, inDoc);
	}

	/**
	 * Invokes a Yantra Service.
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 * @param serviceName
	 *            Name of Service to invoke.
	 * @param str
	 *            Input to be passed to the Service. Should be a valid XML
	 *            String.
	 * @throws java.lang.Exception
	 *             Exception thrown by the Service.
	 * @return Output of the Service.
	 */
	public static Document invokeService(YFSEnvironment env, String serviceName, String str) throws Exception {
		return api.executeFlow(env, serviceName, YFCDocument.parse(str).getDocument());
	}

	/**
	 * 
	 * @param env
	 * @return
	 */
	public static Connection getDBConnection(YFSEnvironment env){
		Connection conn = ((YFSConnectionHolder)env).getDBConnection();
		return conn;
	}
	
	  /**
	   * This method return the sysdate in MM/DD/YYYY format.
	   */
	
	public static String getSysDate(){
		Date currentDate = new Date();
		DateFormat AcademyFormat = new SimpleDateFormat("MM/dd/yyyy");
		
		return AcademyFormat.format(currentDate);
		
	}
	
	/**
     * This function returns the current GMT date string in the yantra format yyyyMMdd'T'HH:mm:ss
     * for example 2004-09-30T11:16:18
     * 
     * @return
     */
    public static String getCurrentXMLGMTTime()
    {
    	String sCurrentGMTTime="";
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date currentDate = new Date();
        Calendar here = Calendar.getInstance();
        int gmtoffset = here.get(Calendar.DST_OFFSET)
        + here.get(Calendar.ZONE_OFFSET);
        Date GMTDate = new Date(currentDate.getTime() - gmtoffset);
        sCurrentGMTTime = formatter.format(GMTDate);
        
        logger.verbose("Datetime is:" + sCurrentGMTTime);
        
        return sCurrentGMTTime;  
        
    }   
    
    /**
     * This function returns the current date string in the yantra format yyyyMMdd'T'HH:mm:ss
     * for example 2004-09-30T11:16:18
     * 
     * @return
     */
	public static String getDate(){
		String strDateFormat =AcademyConstants.STR_DATE_TIME_PATTERN;
	    SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
	    Calendar cal = Calendar.getInstance();
	    String strCurrentDate = sDateFormat.format(cal.getTime());
	    return strCurrentDate;
	}

	/**
	 * Update the company attribute to DOT in the changeShipmentInput Document
	 * if the SCAC is Fedex and Level of service (LOS) as ground.
	 * 
	 * @param SCAC -
	 *            Carrier Name
	 * @param carrierService -
	 *            level of service
	 * @param docChangeShipment -
	 *            Document which has the changes that needs to be updated on the
	 *            shipment.
	 * @return docChangeShipment - Document which has the changes to be made on
	 *         the shipment.
	 */

	public static Document updateCompanyToDot(String SCAC, String carrierService, Document docChangeShipment, Document shipmentListDoc) {

		logger.debug("AcademyUtil.updateCompanyToDot()_SCAC:" + SCAC);
		logger.debug("AcademyUtil.updateCompanyToDot()_carrierService:" + carrierService);

		if (!YFCObject.isVoid(SCAC) && !YFCObject.isVoid(carrierService) && (SCAC.equalsIgnoreCase(AcademyConstants.SCAC_FEDEX) || SCAC.equalsIgnoreCase(AcademyConstants.SCAC_FEDX)) && carrierService.equalsIgnoreCase(AcademyConstants.LOS_GROUND)) {
			// SCAC is fedex and LOS is Ground. Update company field to DOT if
			// the company field is empty.

			// Copying the ToAddress element from shipmentlist output document
			// to changeShipment Input document.
			importToAddressElem(shipmentListDoc, docChangeShipment);

			Element shipmentElem = docChangeShipment.getDocumentElement();
			Element toAddressElem = (Element) shipmentElem.getElementsByTagName(AcademyConstants.ELEM_TOADDRESS).item(0);
			String company = toAddressElem.getAttribute(AcademyConstants.ATTR_COMPANY);
			if ("".equals(company)) {
				// Update company field to DOT.
				logger.verbose("Company field has been modified to DOT!");
				toAddressElem.setAttribute(AcademyConstants.ATTR_COMPANY, ".");
				// changeShipmentRequired flag has been set as Y to identify the
				// calling method that a changeShipment API needs to be called.
				docChangeShipment.getDocumentElement().setAttribute("ChangeShipmentRequired", "Y");
			}
		}

		return docChangeShipment;
	}
	/**
	 * Copy all the attributes of the Element toAddress from getShipmentList
	 * Output Document to changeShipment Input Document
	 * 
	 * 
	 * @param shipmentListDoc -
	 *            Document from which the Element toAddress will be read.
	 * @param docChangeShipment -
	 *            Document to which the toAddress will be copied.
	 * 
	 */
	public static void importToAddressElem(Document shipmentListDoc, Document docChangeShipment) {

		logger.debug("ShipmentListDoc_importToAddressElem(): " + XMLUtil.getXMLString(shipmentListDoc));
		logger.debug("ChangeShipmentDocument_importToAddressElem(): " + XMLUtil.getXMLString(docChangeShipment));
		// Copy all the Ship To Address attributes to the changeShipment input
		Element sLToAddressElem = (Element) shipmentListDoc.getElementsByTagName(AcademyConstants.ELEM_TOADDRESS).item(0);

		Element shipmentElem = docChangeShipment.getDocumentElement();
		if (sLToAddressElem != null) {
			shipmentElem.appendChild(docChangeShipment.importNode(sLToAddressElem, true));
		}

		logger.debug("ChangeShipmentDocument_AfterImport_importToAddressElem(): " + XMLUtil.getXMLString(docChangeShipment));
	}
	
	/** Oracle Contact Center Fix
	 * Validate the Login ID when the webservice call is made from Oracle
	 * Contact Center
	 * 
	 * @param LoginID -
	 *            Login ID of the user
	 * @param Password -
	 *            Password provided by user
	 * @param docLoginRequest -
	 *            Document which has the input of the login API.
	 * @return
	 * @return docLoginResponse - Document which has the output of login API
	 */

	public static Document validateLoginCredentials(YFSEnvironment env,
			String strLoginID, String strPassword) {

		logger.verbose("validateLoginCredentials()_loginID:" + strLoginID);
		logger.verbose("validateLoginCredentials()_password:" + strPassword);

		Document docLoginResponse = null;
		Document docLoginRequest = null;

		try {
			if (!YFCObject.isVoid(strLoginID) && !YFCObject.isVoid(strPassword)) {
				docLoginRequest = XMLUtil.createDocument("Login");
				docLoginRequest.getDocumentElement().setAttribute("LoginID",
						strLoginID);
				docLoginRequest.getDocumentElement().setAttribute("Password",
						strPassword);
				docLoginResponse = AcademyUtil.invokeAPI(env, "login",
						docLoginRequest);
				logger.verbose("Output of login API is :"
						+ XMLUtil.getXMLString(docLoginResponse));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return docLoginResponse;
		}
		return docLoginResponse;
	}
	
	public static String getItemAliasValueForItem(String strrItemID,YFSEnvironment env)throws Exception
	{
//		START: STL1241	  
	    String strItemAliasValue = "";
	
		Document docIpGetItemList = XMLUtil.createDocument(AcademyConstants.ITEM);
		docIpGetItemList.getDocumentElement().setAttribute(AcademyConstants.ATTR_ITEM_ID, strrItemID);
		Document outputTemp = YFCDocument.getDocumentFor("<ItemList> <Item><ItemAliasList><ItemAlias AliasValue =''/> </ItemAliasList></Item></ItemList>").getDocument();
		env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, outputTemp);
		Document itemListOutputDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST, docIpGetItemList);
		env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
		logger.verbose("Item list output - " + XMLUtil.getXMLString(itemListOutputDoc));
		if(!YFCObject.isVoid(itemListOutputDoc)  && itemListOutputDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_ITEM_ALIAS_LIST).getLength()>0)
		{
			
			logger.verbose("************ALIAS LIST IS GREATER THAN 0***********************");
			Element itemAliasListElement = (Element)itemListOutputDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_ITEM_ALIAS_LIST).item(0);
			if ((!YFCObject.isVoid(itemAliasListElement))&& itemAliasListElement.hasChildNodes() ){
				Element itemAliasElement = (Element)itemAliasListElement.getElementsByTagName(AcademyConstants.ELE_ITEM_ALIAS).item(0);
				strItemAliasValue=itemAliasElement.getAttribute(AcademyConstants.ATTR_ALIAS_VAL);
				 if(!YFCObject.isVoid(strItemAliasValue))
					 return strItemAliasValue;	
			}
							
		}
		return strItemAliasValue;
	    //END: STL1241
	}
	
	//START: STL-1647
	/** Returns the discount description for the given charge name
	 * @param ItemDiscChargeName
	 * @param itemDiscType
	 * @param nLineCharge
	 * @param l
	 * @return
	 */
	public static String getItemDiscTypeNames(String ItemDiscChargeName,
			String itemDiscType, NodeList nLineCharge, int l) {

		String itemDiscPrintName = "";
		if ("BOGO".equals(ItemDiscChargeName)) {
			itemDiscPrintName = "Buy More, Get More";
		}
		if ("DiscountCoupon".equals(ItemDiscChargeName)) {
			itemDiscPrintName = "Coupon";
		}
		if ("CUSTOMER_APPEASEMENT".equals(ItemDiscChargeName)) {
			itemDiscPrintName = "CS Adj.";
		}

		if (l < (nLineCharge.getLength() - 1)) {
			itemDiscType = itemDiscType + itemDiscPrintName + " /";
		} else {
			itemDiscType += itemDiscPrintName;
		}

		// if (itemDiscType.endsWith("/")) {
		// itemDiscType =
		// itemDiscType.substring(itemDiscType.length()-(itemDiscType.length()-1));
		// }
		return itemDiscType;
	}
	//END: STL-1647
	
    
    //Start WN-697 : Sterling to consume special characters and include them in customer-facing email's, but to remove them before settlement. 
	/**
	 * Method calls convertUnicodeToSpecialChar(env, hashmap, ele, boolean) for BillTo and/or ShipTo elements
	 * @param env
	 * @param eleInXML1
	 * @param eleInXML2
	 * @param isPaymentTran
	 * @throws Exception
	 */
	public static void convertUnicodeToSpecialChar(YFSEnvironment env, Element eleInXML1, Element eleInXML2, boolean isPaymentTran) throws Exception{
		logger.verbose("Entering convertUnicodeToSpecialChar1 ");
		HashMap<String, String> hmUnicodeMapping = null;
		
		if(!YFCObject.isVoid(eleInXML1)){
			convertUnicodeToSpecialChar(env, hmUnicodeMapping,eleInXML1,isPaymentTran);
		}
		if(!YFCObject.isVoid(eleInXML2)){
			convertUnicodeToSpecialChar(env, hmUnicodeMapping,eleInXML2,isPaymentTran);
		}		
		logger.verbose("Exiting convertUnicodeToSpecialChar1 ");		
	}
	/**
	 * Method looks for FirstName/LastName/AddressLine1 and if unicode's is present in them,then makes a convertUnicodeToSpecialChar() call.
	 * And if its a Payment transaction then all special characters are replaced with " "
	 * @param eleInXML - Contains FirstName/LastName/AddressLine1 either at PersonInfoShipTo/PersonInfoBillTo
	 * @param isPaymentTransaction - For payment calls,we read FirstName,LastName,address from BillToFirstName/BillToLastName/BillToAddressLine1 attributes 
     *								 And for other transactions i.e email - we read FirstName,LastName,address from FirstName/LastName/AddressLine1 attributes
     * @throws Exception								 
	 */
    public static void convertUnicodeToSpecialChar(YFSEnvironment env, HashMap<String, String> hmUnicodeMap, Element eleInXML, boolean isPaymentTransaction) throws Exception{
    	logger.verbose("Entering convertUnicodeToSpecialChar2 :: "+XMLUtil.getElementXMLString(eleInXML));
    	
		String strFirstName = null;
		String strLastName = null;
		String strAddressLine1 = null;				
		
		if(!YFCObject.isVoid(eleInXML)){
			if(isPaymentTransaction){
				strFirstName = eleInXML.getAttribute(AcademyConstants.ATTR_BILL_TO_FIRST_NAME);
				strLastName = eleInXML.getAttribute(AcademyConstants.ATTR_BILL_TO_LAST_NAME);
				strAddressLine1 = eleInXML.getAttribute(AcademyConstants.ATTR_BILL_TO_ADDRLINE1);
			}else{
				strFirstName = eleInXML.getAttribute(AcademyConstants.ATTR_FNAME);
				strLastName = eleInXML.getAttribute(AcademyConstants.ATTR_LNAME);
				strAddressLine1 = eleInXML.getAttribute(AcademyConstants.ATTR_ADDRESS_LINE_1);
			}
			
			if((strFirstName.contains(AcademyConstants.STR_AMPERSAND) && strFirstName.contains(AcademyConstants.STR_SEMICOLON)) ||
			   (strLastName.contains(AcademyConstants.STR_AMPERSAND) && strLastName.contains(AcademyConstants.STR_SEMICOLON)) ||
			   (strAddressLine1.contains(AcademyConstants.STR_AMPERSAND) && strAddressLine1.contains(AcademyConstants.STR_SEMICOLON))){    		
				
				if(YFCObject.isVoid(hmUnicodeMap)){
					logger.verbose("Calling Commode Code API");
					hmUnicodeMap = AcademyCommonCode.getCommonCodeListAsHashMap(env, AcademyConstants.COMMON_CODE_SPECIAL_CHARS,
							AcademyConstants.PRIMARY_ENTERPRISE);
				}
				
				logger.verbose("BEFORE :: BillToFirstName : "+ strFirstName + "\n" + " BillToLastName : " + strLastName + "\n" + " BillToAddressLine1 : "+strAddressLine1);
	    		strFirstName = convertUnicodeToSpecialChar(hmUnicodeMap, strFirstName);
				strLastName = convertUnicodeToSpecialChar(hmUnicodeMap, strLastName);
				strAddressLine1 = convertUnicodeToSpecialChar(hmUnicodeMap, strAddressLine1);	
				logger.verbose("AFTER :: BillToFirstName : "+ strFirstName + "\n" + " BillToLastName : " + strLastName + "\n" + " BillToAddressLine1 : "+strAddressLine1);
				
				if(!isPaymentTransaction){
					eleInXML.setAttribute(AcademyConstants.ATTR_FNAME, strFirstName);
					eleInXML.setAttribute(AcademyConstants.ATTR_LNAME, strLastName);
					eleInXML.setAttribute(AcademyConstants.ATTR_ADDRESS_LINE_1, strAddressLine1);
				}
			}
			
			if(isPaymentTransaction){
				strFirstName = strFirstName.replaceAll(AcademyConstants.REGEX_TO_REMOVE_SPECIAL_CHARS,AcademyConstants.STR_BLANKSPACE);
				strLastName = strLastName.replaceAll(AcademyConstants.REGEX_TO_REMOVE_SPECIAL_CHARS,AcademyConstants.STR_BLANKSPACE);
				strAddressLine1 = strAddressLine1.replaceAll(AcademyConstants.REGEX_TO_REMOVE_SPECIAL_CHARS,AcademyConstants.STR_BLANKSPACE);
				
				eleInXML.setAttribute(AcademyConstants.ATTR_BILL_TO_FIRST_NAME, strFirstName);
				eleInXML.setAttribute(AcademyConstants.ATTR_BILL_TO_LAST_NAME, strLastName);
				eleInXML.setAttribute(AcademyConstants.ATTR_BILL_TO_ADDRLINE1, strAddressLine1);
			}
		}
		
		logger.verbose("Exiting convertUnicodeToSpecialChar2 :: "+XMLUtil.getElementXMLString(eleInXML));
    }
    
    /**
	 * Method converts all the unicode's present in the string to special characters.
	 * @param hmUnicodeMap - Populated from CommonCode API call. Contains Unicode's-Special Character mapping
	 * @param strUnicodeString - Is either BillToFirstName/BillToLastName/BillToAddressLine1/ShipToFirstName/ShipToLastName/ShipToAddressLine1
	 * @throws Exception
	 */
    public static String convertUnicodeToSpecialChar(HashMap<String, String> hmUnicodeMap, String strUnicodeString) throws Exception {
    	logger.verbose("Entering convertUnicodeToSpecialChar3 : " +strUnicodeString);
    	int iStart = 0;
    	int iEnd = 0;
    	String strSubString = null;
    	String strUniCode = null;
    	
    	//To avoid infinite loop - Ex : "Academy&Sports;Outdoor"
    	int iCounter = strUnicodeString.length();
    	
        while(strUnicodeString.contains(AcademyConstants.STR_AMPERSAND) && strUnicodeString.contains(AcademyConstants.STR_SEMICOLON) && iCounter > 0){
        	iEnd = strUnicodeString.indexOf(AcademyConstants.STR_SEMICOLON) + 1 ;
        	strSubString = strUnicodeString.substring(0, iEnd);
        	iStart = strSubString.lastIndexOf(AcademyConstants.STR_AMPERSAND);
        	strUniCode = strUnicodeString.substring(iStart, iEnd);
            strUnicodeString = strUnicodeString.replace(strUniCode,hmUnicodeMap.get(strUniCode));
            iCounter--;
        }
	        
    	logger.verbose("Exiting convertUnicodeToSpecialChar3 : " +strUnicodeString);
    	return strUnicodeString;
    }
    
    
    //End WN-697 : Sterling to consume special characters and include them in customer-facing emails, but to remove them before settlement.
    
    
    /**
   	 * Method returns true if EGC functionality is enabled and returns false if EGC functionality is disabled
   	 * @throws Exception
   	 */
    //Start: OMNI-5006

    public static boolean isEGCEnabled(YFSEnvironment env) throws Exception {

    	Document getCommonCodeListInDoc = null;
    	Document getCommonCodeListOutDoc = null;
    	String strIsEGCEnabled = null;

    	try {
    		getCommonCodeListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
    		getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, AcademyConstants.STR_EGC_ENABLED);
    		getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ORG_CODE_ATTR, AcademyConstants.PRIMARY_ENTERPRISE);
    		getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE, AcademyConstants.STR_EGC_FLAG);

    		Document getCommonCodeListOPTempl = XMLUtil
    				.getDocument("<CommonCode OrganizationCode ='' CodeType='' CodeValue='' CodeShortDescription=''/>");
    		env.setApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST, getCommonCodeListOPTempl);

    		getCommonCodeListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST,
    				getCommonCodeListInDoc);
    		env.clearApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST);

    		if (getCommonCodeListOutDoc != null) {
    			Element CommonCodeListEle = getCommonCodeListOutDoc.getDocumentElement();
    			Element CommonCodeEle = (Element) CommonCodeListEle.getElementsByTagName("CommonCode").item(0);
    			strIsEGCEnabled = CommonCodeEle.getAttribute("CodeShortDescription");
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    		throw (Exception) e;
    	}
    	if (strIsEGCEnabled.equalsIgnoreCase("Y")) {
    		return true;
    	}
    	return false;
    }
    //End: OMNI-5006
  //Changes for OMNI-4017 Start
  	/**
  	 * Method added ShowCurbsideInstructions="Y/N" at order line in case for
  	 * ExtnDepartmentName='‘Hand Guns/Short Guns'
  	 * 
  	 * @throws Exception
  	 */
  	public static Document validateAndAppendShowCurbsideInstructions(Document orderDetails, String strSOFDepartements)
  			throws Exception {

  		NodeList eleorderLines = XPathUtil.getNodeList(orderDetails.getDocumentElement(),
  				"/Order/OrderLines/OrderLine");
  		for (int i = 0; i < eleorderLines.getLength(); i++) {
  			Element eleOrderLine = (Element) eleorderLines.item(i);
  			String strExtnDepartmentName = SCXmlUtil.getXpathAttribute(eleOrderLine, "ItemDetails/Extn/@ExtnDepartmentName");
  			//Verifying PackListType in if-condition as part of OMNI-86861
  			String strPackListType = eleOrderLine.getAttribute(AcademyConstants.STR_PACK_LIST_TYPE);
  			logger.verbose("strPackListType :: " +strPackListType);
  			if (!YFCCommon.isVoid(strSOFDepartements) && !YFCCommon.isVoid(strExtnDepartmentName)
  					&& strSOFDepartements.contains(strExtnDepartmentName) ||
  				(!YFCObject.isVoid(strPackListType) && (strPackListType.equals(AcademyConstants.STS_FA)))) {
  				eleOrderLine.setAttribute("ShowCurbsideInstructions", AcademyConstants.STR_NO);
  			} else {
  				eleOrderLine.setAttribute("ShowCurbsideInstructions", AcademyConstants.STR_YES);
  			}
  		}

  		return orderDetails;
  	}
  	//Changes for OMNI-4017 End
	//Commented code for OMNI-30147
	/**
	 * This method invoke getShipmentLineList API to Check Whether All Lines Of
	 * Shipment Cancelled
	 * 
	 * @param strShipmentKey
	 * @return
	 * @throws Exception
	 */
	public static boolean isAllLinesCancelledOfShipment(YFSEnvironment env, String strShipmentLineKey,
			String strOrderLineKey) throws Exception {
		logger.verbose("callgetShipmentLineList Method");
		Document OutDocgetShipmentLineList = null;
		Document getShipmentLineList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT_LINE);

		Element elegetShipmentLineList = getShipmentLineList.getDocumentElement();
		if (YFCCommon.isVoid(strOrderLineKey)) {
			elegetShipmentLineList.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, strShipmentLineKey);
		} else {
			elegetShipmentLineList.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, strOrderLineKey);
		}
		if (logger.isVerboseEnabled()) {
			logger.verbose(
					"Calling callgetShipmentLineList API with the input" + XMLUtil.getXMLString(getShipmentLineList));
		}

		String templateStr = "<ShipmentLines><ShipmentLine ShipmentLineKey='' ShipmentKey=''><Shipment ShipmentKey=''><ShipmentLines><ShipmentLine ShipmentLineKey=''>\r\n"
				+ "<OrderLine OrderLineKey='' OrderedQty=''/></ShipmentLine></ShipmentLines></Shipment></ShipmentLine></ShipmentLines>";

		Document outputTemplate = YFCDocument.getDocumentFor(templateStr).getDocument();
		env.setApiTemplate(AcademyConstants.SER_GET_SHIPMENT_LINE_LIST, outputTemplate);
		OutDocgetShipmentLineList = AcademyUtil.invokeAPI(env, AcademyConstants.SER_GET_SHIPMENT_LINE_LIST,
				getShipmentLineList);
		env.clearApiTemplate(AcademyConstants.SER_GET_SHIPMENT_LINE_LIST);
		if (logger.isVerboseEnabled()) {
			logger.verbose(
					"Output of the  callgetShipmentLineList API " + XMLUtil.getXMLString(OutDocgetShipmentLineList));
		}
		boolean isAllLineCancelled = true;
		String strOrderedQuantity = SCXmlUtil.getXpathAttribute(OutDocgetShipmentLineList.getDocumentElement(),
				"ShipmentLine/Shipment/ShipmentLines/ShipmentLine/OrderLine[@OrderedQty > '0']/@OrderedQty");
		if (!YFCCommon.isVoid(strOrderedQuantity)) {
			isAllLineCancelled = false;
		}

		logger.verbose("isAllLineCancelled " + isAllLineCancelled);
		logger.verbose("callgetShipmentLineList Method");
		return isAllLineCancelled;
	}
	//Commented code for OMNI-30147
	
	public static Boolean checkNodeTypeIsStore(YFSEnvironment env, String shipNode) throws Exception{
    	logger.verbose("inside checkIsDCOrStore ");
    	String strGetOrganizationHierarchyTemplate = "<Organization><Node NodeType='' /></Organization>";
    	Document docOutputTemplate=null;
    	Document docGetOrgDtls=null;
    	Boolean nodeTypeIsStore = false;
    	docGetOrgDtls = XMLUtil.createDocument(AcademyConstants.ORG_ELEMENT); 
        //Fetch attribute value for OrganizationCode 
        docGetOrgDtls.getDocumentElement().setAttribute(AcademyConstants.ORG_CODE_ATTR, shipNode); 
    	docOutputTemplate = YFCDocument.getDocumentFor(strGetOrganizationHierarchyTemplate).getDocument();
    	env.setApiTemplate(AcademyConstants.GET_ORG_HIERARCHY_API, docOutputTemplate);
    	 Document docOutputOrgDtls = AcademyUtil.invokeAPI(env, AcademyConstants.GET_ORG_HIERARCHY_API, docGetOrgDtls); 
         //Clear template 
         env.clearApiTemplates(); 
    	String isDCOrStore = XMLUtil.getString(docOutputOrgDtls.getDocumentElement(), "//Organization/Node/@NodeType");
    	logger.verbose("Is Store Or DC : " +isDCOrStore);
    	if(("Store".equals(isDCOrStore)) ) {
    		nodeTypeIsStore = true;
    	}
    	return nodeTypeIsStore;
    	
    	}

}
