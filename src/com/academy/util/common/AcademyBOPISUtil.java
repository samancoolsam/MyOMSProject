package com.academy.util.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @Author : Chiranthan(SapientRazorfish_)
 * @JIRA# : BOPIS-Phase 1
 * @Date : Created on June 2018
 * @Purpose : This class has a set of utility methods used for BOPIS-Phase 1 project.
 * 
 **/

public class AcademyBOPISUtil {
	
	private static Logger log = Logger.getLogger(AcademyBOPISUtil.class.getName());
	
	/**
	 * This method checks if order has any BOPIS line(In case of mixedcart/only BOPIS order)
	 * @param inDoc - must contain <OrderLine>
	 * @return boolean - bHasBOPISline="true/false"
	 */ 
	public static boolean checkIfOrderHasBOPISline(Document inDoc) {
		log.verbose("Entering AcademyBOPISUtil.checkIfOrderHasBOPISline() ::"+XMLUtil.getXMLString(inDoc));
		
		boolean bHasBOPISline = false;
		String strDeliveryMethod = null;
		Element eleOrderLine = null;		
		
		NodeList nlOrderLine = inDoc.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
		
		for (int i = 0; i < nlOrderLine.getLength(); i++) {			
			eleOrderLine = (Element) nlOrderLine.item(i);			
			strDeliveryMethod = eleOrderLine.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
			
			if(AcademyConstants.STR_PICK_DELIVERY_METHOD.equals(strDeliveryMethod)){
				bHasBOPISline = true;
			}
		}	
		log.verbose("Exiting AcademyBOPISUtil.checkIfOrderHasBOPISline() :: bHasBOPISline - "+bHasBOPISline);
		
		return bHasBOPISline;
	}

	/**
	 * check cart has  E-Gift Card Lines OMNI-5001
	 */
	public static boolean checkIfOrderHasEGCline(Document inDoc) {
		log.verbose("Check cart, if it has  E-Gift Card Lines ::"+XMLUtil.getXMLString(inDoc));
		
		boolean bHasEGCLine = false;
		String strFulfillmentType = null;
		Element eleOrderLine = null;		
		
		NodeList nlOrderLine = inDoc.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
		
		for (int i = 0; i < nlOrderLine.getLength(); i++) {			
			eleOrderLine = (Element) nlOrderLine.item(i);			
			strFulfillmentType = eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
			
			if( AcademyConstants.STR_E_GIFT_CARD.equals(strFulfillmentType)){
				bHasEGCLine = true;
			}
		}	
		log.verbose("Cart has  E-Gift Card Lines: bHasEGCline - "+bHasEGCLine);
		
		return bHasEGCLine;
	}
	
	/**
	 * This method invokes getCommonCodeList API & returns the list of common code values for the required CODE_TYPE.
	 * @param env
	 * @param strCodeType - String value to determine common code.
	 * @param strOrganizationCode - Academy_Direct/DEFAULT 
	 * @return Document - getCommonCodeList API output
	 * @throws Exception 
	 */ 
	public static Document getCommonCodeList(YFSEnvironment env,String strCodeType,String strOrganizationCode) throws Exception 
	{
		log.verbose("Entering AcademyBOPISUtil.getCommonCodeList() :: strCodeType - "+strCodeType);
		
		Element eleInGetCommonCodeList = null;
		Document docInGetCommonCodeList = null;
		Document docOutGetCommonCodeList = null;
		Document templateGetCommonCodeListAPI = null;

		docInGetCommonCodeList = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		eleInGetCommonCodeList = docInGetCommonCodeList.getDocumentElement();
		eleInGetCommonCodeList.setAttribute(AcademyConstants.ATTR_CODE_TYPE, strCodeType);
		eleInGetCommonCodeList.setAttribute(AcademyConstants.ORGANIZATION_CODE, strOrganizationCode);
		
		templateGetCommonCodeListAPI = YFCDocument.getDocumentFor("<CommonCode CodeValue=\"\" CodeLongDescription=\"\" CodeShortDescription=\"\" />").getDocument();

		log.verbose("Input to getCommonCodeList : "+XMLUtil.getXMLString(docInGetCommonCodeList));
		env.setApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST, templateGetCommonCodeListAPI);
		docOutGetCommonCodeList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST, docInGetCommonCodeList);
		env.clearApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST);
		log.verbose("Output from getCommonCodeList : "+XMLUtil.getXMLString(docOutGetCommonCodeList));
		
		log.verbose("Exiting AcademyBOPISUtil.getCommonCodeList()");
		
		return docOutGetCommonCodeList;
	}
	
	
	/**
	 * This method compares 2 statuses to check - Equals/Lesser/Greater.
	 * @param status1 
	 * @param status2
	 * @return
	 * if status1 == status2, it returns 0
	 * else if status1 > status2, it returns positive integer
	 * else if status1 < status2, it returns negative integer
	 */
	public static Integer compareStatus(String strStatus1, String strStatus2) {
		int iComparisonNumber = YFCCommon.compareStrings(strStatus1, strStatus2);
		if(iComparisonNumber == 0){
			return 0;
		}else if(!YFCCommon.isVoid(strStatus1) && YFCCommon.isVoid(strStatus2)){
			return 1;
		}else if(YFCCommon.isVoid(strStatus1) && !YFCCommon.isVoid(strStatus2)){
			return -1;
		}
		
		String[] splittedStatus1 = {strStatus1};
		if(strStatus1.indexOf('.') > 0){
			splittedStatus1 = strStatus1.split("\\.");
		}
		
		String[] splittedStatus2 = {strStatus2};
		if(strStatus2.indexOf('.') > 0){
			splittedStatus2 = strStatus2.split("\\.");
		}
		
		int minSubStatusNo = Math.min(splittedStatus1.length, splittedStatus2.length);
		int i = 0;
		for(;i < minSubStatusNo; i++){
			iComparisonNumber = Integer.parseInt(splittedStatus1[i]) - Integer.parseInt(splittedStatus2[i]);
			if(iComparisonNumber != 0){
				return iComparisonNumber;
			}
		}
		
		if(splittedStatus1.length == splittedStatus2.length){
			return 0;
		}else if(splittedStatus1.length > splittedStatus2.length){
			return 1;
		}else if(splittedStatus1.length < splittedStatus2.length){
			return -1;
		}
			
		return null;
	}

	public static String retrieveCodeShortDesc(YFSEnvironment objEnv, Document docOutGetCommonCodeList, String strCodeValue) throws Exception {

		log.verbose("Starting AcademyBOPISUtil.retrieveCodeShortDesc() Start");
		Element eleCommonCode = null;

		String strCodeShortDesc = null;

	

			if (!YFCObject.isNull(docOutGetCommonCodeList)) {
				eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList,
						"/CommonCodeList/CommonCode[@CodeValue='" + strCodeValue + "']");
				strCodeShortDesc = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
			}
		log.verbose("AcademyBOPISUtil.retrieveCodeShortDesc.strCodeShortDesc "+strCodeShortDesc);
		log.verbose("Starting AcademyBOPISUtil.retrieveCodeShortDesc() End");
		return strCodeShortDesc;
	}

	
	public static Boolean checkIfBOPISEmailNotToBeSent(YFSEnvironment env, String strStartTime, String strEndTime) throws Exception
	{
		log.verbose("Starting AcademyBOPISUtil.checkIfBOPISEmailNotToBeSent() Start");
		Boolean isBetween=false;
		
	
		log.verbose("strStartTime-> "+strStartTime);
		log.verbose("strEndTime-> "+strEndTime);
		int from = Integer.parseInt(strStartTime);
	    int to = Integer.parseInt(strEndTime);
		
		log.verbose("from-> "+from);
		log.verbose("to-> "+to);
	    
		Date date = new Date();
	    Calendar c = Calendar.getInstance();
	    c.setTime(date);
	    int t = c.get(Calendar.HOUR_OF_DAY) * 100 + c.get(Calendar.MINUTE);
	    log.verbose("Current timr t-> "+t);
	    
	    isBetween = to > from && t >= from && t <= to || to < from && (t >= from || t <= to);
		log.verbose("isBetween-> "+isBetween);
		log.verbose("Starting AcademyBOPISUtil.checkIfBOPISEmailNotToBeSent() End");
		return isBetween;
	}
	
	public static String getHoursToAddForBOPISEmail(int nowHour, int nowMin, String endTime) {
		
		log.verbose("Starting AcademyBOPISUtil.getHoursToAddForBOPISEmail() Start");
		String strHrMin=null;
	    Matcher m = Pattern.compile("(\\d{2}):(\\d{2})").matcher(endTime);
	    if (! m.matches())
	        throw new IllegalArgumentException("Invalid time format: " + endTime);
	    int endHour = Integer.parseInt(m.group(1));
	    int endMin  = Integer.parseInt(m.group(2));
	    if (endHour >= 24 || endMin >= 60)
	        throw new IllegalArgumentException("Invalid time format: " + endTime);
	    int minutesLeft = endHour * 60 + endMin - (nowHour * 60 + nowMin);
	    if (minutesLeft < 0)
	        minutesLeft += 24 * 60; // Time passed, so time until 'end' tomorrow
	    int hours = minutesLeft / 60;
	    int minutes = minutesLeft - hours * 60;
	    
	    strHrMin = hours+":"+minutes;
	    log.verbose("strHrMin-> "+strHrMin);
	    log.verbose("hours-> "+hours);
	    log.verbose("minutes-> "+minutes);
	    log.verbose("Starting AcademyBOPISUtil.getHoursToAddForBOPISEmail() End");
	    return strHrMin;

	}
	
	public static void putAheadAvailableDateInTaskQForBOPIS(YFSEnvironment env, String strTaskQKey, Integer intDelayInHr, Integer intDelayInMin) {
		try {
			log.verbose("********putAheadAvailableDateInTaskQ Start ******* ");

			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			log.verbose("Current Date ::: " + sdf.format(cal.getTime()));
			cal.add(Calendar.HOUR, intDelayInHr);
			cal.add(Calendar.MINUTE, intDelayInMin);
			Date availableDate = cal.getTime();
			String strAvailableDate = sdf.format(availableDate);
			log.verbose("AvailableDate to be stamped :::" + strAvailableDate);
			Document docManageTaskQueue = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
			Element eleManageTaskQueue = docManageTaskQueue.getDocumentElement();
			eleManageTaskQueue.setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, strTaskQKey);
			eleManageTaskQueue.setAttribute("AvailableDate", strAvailableDate);
			log.verbose("***** Input doc to manageTaskQueue ***** "
					+ XMLUtil.getElementXMLString(docManageTaskQueue.getDocumentElement()));
			AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_TASK_QUEUE, docManageTaskQueue);
			log.verbose("********putAheadAvailableDateInTaskQ END ******* ");
		} catch (ParserConfigurationException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

	}
}
