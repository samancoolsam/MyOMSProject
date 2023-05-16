package com.academy.ecommerce.sterling.bopis.shipment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/*##################################################################################
*
* Project Name                : POD August Release - 2022
* Module                      : Fulfillment
* Author                      : Everest
* Date                        : 10-Aug-2022
* Description                 : This class is invoked as part of custom agent ACAD_RFCP_STORE_NOTIFY
*  								for posting RFCP in-store notification messages.
* 								As part of getJobs service, ACAD_STORE_ACTION_DATA record is retrieved for 
* 								for respective shipments without duplicates. With the help of executeJob service,
* 								this message will be checked for time interval at which the record is created
* 								and based on that notification message will be triggered from OMS via queue.
*								OMNI- 80163
* ---------------------------------------------------------------------------------
* Date            Author                  Version#       Remarks/Description                     
* ---------------------------------------------------------------------------------
* 10-AUG-2022     Everest                      1.0            Initial version
* ##################################################################################*/

public class AcademyBOPISProcessInstorePickupNotification {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyBOPISProcessInstorePickupNotification.class);

	/**
	 * This is the the main method being called as part of getJobs logic
	 * 
	 * @param env
	 * @param strAcadStoreActionDataKey
	 * @param strnotificationInstance
	 * @return
	 * @throws Exception
	 * Sample Input XML: <MessageXml Action="Get" DocumentParamsKey="0001" DocumentType="0001"
    	FirstNotification="10" JOB_ELEMENT_NAME="AcadStoreActionData" NumRecordsToBuffer="5000" ProcessType="ORDER_DELIVERY"
    	ProcessTypeKey="ORDER_DELIVERY" SERVICE_FOR_EXECUTE_JOB="AcademyExecShipActionDataForRemNotify"
    	SERVICE_FOR_GET_JOBS="AcademyGetShipActionDataForRemNotify" SecondNotification="15" ThirdNotification="45"
    	TransactionId="ACAD_SHIP_ACTION_MONI.0001.ex" TransactionKey="202208090630215665990535">
    		<LastMessage>
        		<AcadStoreActionData Createprogid="SterlingHttpTester" Createts="2022-08-10T03:32:35-05:00" Createuserid="admin"
            	Delivery_Method="PICK" Lockid="0" NotifyStore="Y" NotifyStoreUpdated="N"
            	OrderNo="BPS2022062114" SERVICE_FOR_EXECUTE_JOB="AcademyExecShipActionDataForRemNotify"
            	ShipmentNo="100546132" StoreActionDataKey="202208100332355666299210" UserID="sfs165"/>
    		</LastMessage>
		</MessageXml>
	 *
	 */

	public Document pickShipmentsForReminder(YFSEnvironment env, Document inXML) throws Exception {
		log.beginTimer("AcademyBOPISProcessInstorePickupNotification.pickShipmentsForReminder() :: ");
		log.verbose("*** pickShipmentsForReminder input doc ***" + XMLUtil.getXMLString(inXML));

		NodeList nlLastMessage = inXML.getElementsByTagName(AcademyConstants.STR_LAST_MESSAGE);
		String strShipmentNo = "";
		if (nlLastMessage.getLength() == 1) {
			strShipmentNo = ((Element) (((Element) (inXML.getElementsByTagName(AcademyConstants.STR_LAST_MESSAGE)
					.item(0))).getElementsByTagName(AcademyConstants.STR_ACAD_STORE_ACTION_DATA).item(0)))
							.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		}
		log.verbose("*** strShipmentNo :: ***" +strShipmentNo);

		Document inDocAcadStoreActionData = prepareInputForGetAcadStoreActionData(strShipmentNo);

		log.verbose("getAcadStoreActionDataList input XML " + XMLUtil.getXMLString(inDocAcadStoreActionData));

		Document outDocGetAcadStoreActionDataList = AcademyUtil.invokeService(env,
				AcademyConstants.STR_SERV_ACADEMY_GET_ACAD_STORE_ACTION_DATA_LIST, inDocAcadStoreActionData);
		log.verbose(" getAcadStoreActionDataList Output " + XMLUtil.getXMLString(outDocGetAcadStoreActionDataList));

		log.verbose("*** outDocGetAcadStoreActionDataList length ***" + outDocGetAcadStoreActionDataList
				.getElementsByTagName(AcademyConstants.STR_ACAD_STORE_ACTION_DATA).getLength());

		outDocGetAcadStoreActionDataList = processStoreActionDataOutput(outDocGetAcadStoreActionDataList, inXML);

		log.verbose("*** outDocGetAcadStoreActionDataList length ***" + outDocGetAcadStoreActionDataList
				.getElementsByTagName(AcademyConstants.STR_ACAD_STORE_ACTION_DATA).getLength());
		log.endTimer("AcademyBOPISProcessInstorePickupNotification.pickShipmentsForReminder() :: ");
		
		return outDocGetAcadStoreActionDataList;
	}

	/**
	 * This is the the main method being called as part of executeJobs logic
	 * 
	 * @param env
	 * @param inDoc
	 * @return inDoc
	 * @throws Exception
	 * Sample Input XML: <AcadStoreActionData Createprogid="SterlingHttpTester"
    			Createts="2022-08-10T02:55:28-05:00" Createuserid="admin" Delivery_Method="PICK" Lockid="0" 
    			NotifyStore="Y" NotifyStoreUpdated="1" OrderNo="BPS2022062114" ShipmentNo="100546132" 
    			StoreActionDataKey="202208100255285666289839" UserID="sfs165"/>
	 * 
	 */
	public Document postNotificationMsg(YFSEnvironment env, Document inDoc) throws Exception {

		log.beginTimer("AcademyBOPISProcessInstorePickupNotification.postNotificationMsg() :: ");
		log.verbose("Inside AcademyBOPISProcessInstorePickupNotification postNotificationMsg:: Input XML :: "
				+ XMLUtil.getXMLString(inDoc));

		String strShipmentNo = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		String strOrderNo = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO);

		String strAcadStoreActionDataKey = inDoc.getDocumentElement()
				.getAttribute(AcademyConstants.STR_STORE_ACTION_DATA_KEY);
		String strNotifyStoreUpdated = inDoc.getDocumentElement()
				.getAttribute(AcademyConstants.STR_NOTIFY_STORE_UPDATED);

		try {
			Document outDocGetShipmentList = getShipmentList(env, strShipmentNo);

			if (outDocGetShipmentList.getDocumentElement().hasChildNodes()) {

				String strStatus = XPathUtil.getString(outDocGetShipmentList.getDocumentElement(),
						"/Shipments/Shipment/@Status");
				String strShipNode = XPathUtil.getString(outDocGetShipmentList.getDocumentElement(),
						"/Shipments/Shipment/@ShipNode");

				if (AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS.equalsIgnoreCase(strStatus)) {
					if (AcademyConstants.STR_DIGIT_ONE.equalsIgnoreCase(strNotifyStoreUpdated)) {
						log.verbose("Inside First Notification Case::");
						prepareAndSendNotification(env, strShipmentNo, strOrderNo, strShipNode,
								AcademyConstants.STR_INSTORE_PICKUP_NOTIF_1);
						updateAcadStoreActionData(env, strAcadStoreActionDataKey, strNotifyStoreUpdated);
					} else if (AcademyConstants.STR_DIGIT_TWO.equalsIgnoreCase(strNotifyStoreUpdated)) {
						log.verbose("Inside Second Notification Case::");
						prepareAndSendNotification(env, strShipmentNo, strOrderNo, strShipNode,
								AcademyConstants.STR_INSTORE_PICKUP_NOTIF_2);
						updateAcadStoreActionData(env, strAcadStoreActionDataKey, strNotifyStoreUpdated);
					} else if (AcademyConstants.STR_DIGIT_THREE.equalsIgnoreCase(strNotifyStoreUpdated)) {
						log.verbose("Inside Third Notification Case::");
						prepareAndSendNotification(env, strShipmentNo, strOrderNo, strShipNode,
								AcademyConstants.STR_INSTORE_PICKUP_NOTIF_3);
						updateAcadStoreActionData(env, strAcadStoreActionDataKey, strNotifyStoreUpdated);
					} else {
						log.verbose("Shipment is not eligible for Notification::");
						updateAcadStoreActionData(env, strAcadStoreActionDataKey, AcademyConstants.STR_NO);
					}

				}
				else {
					log.verbose("Status is not eligible");
					updateAcadStoreActionData(env, strAcadStoreActionDataKey, AcademyConstants.STR_NO);
				}
			} else {
				// Invalid Shipment No. Ignore the record
				log.verbose("Invalid Shipment No");
				updateAcadStoreActionData(env, strAcadStoreActionDataKey, AcademyConstants.STR_NO);
			}

		} catch (Exception e) {
			log.verbose("Exception inside AcademyExecShipActionDataForRemNotify : postNotificationMsg ");
			e.printStackTrace();
		}
		return inDoc;
	}

	/**
	 * This method is prepares the input to the ACAD_STORE_ACTION_DATE table to
	 * fetch eligible records
	 * 
	 * @param strShipmentKey
	 * @return docAcadStoreActionData
	 * @throws ParserConfigurationException 
	 * @throws Exception
	 * Sample Input XML for getAcadStoreActionDataList: 
	 * <AcadStoreActionData> <ComplexQuery Operator="AND"> <Or>
     * 		 <Exp QryType="EQ" Name="NotifyStore" Value="Y" />
     * 		 <Exp QryType="EQ" Name="NotifyStore" Value="1" />
     * 		 <Exp QryType="EQ" Name="NotifyStore" Value="2" /> </Or> </ComplexQuery>
     * 	</AcadStoreActionData>
	 */

	private Document prepareInputForGetAcadStoreActionData(String strShipmentNo) throws ParserConfigurationException   {
		log.beginTimer("AcademyBOPISProcessInstorePickupNotification.prepareInputForGetAcadStoreActionData()");
		log.verbose("*** strShipmentNo ***" + strShipmentNo);

		Document docAcadStoreActionData = XMLUtil.createDocument(AcademyConstants.STR_ACAD_STORE_ACTION_DATA);
		Element eleAcadStoreActionData = docAcadStoreActionData.getDocumentElement();

		if (!YFCObject.isVoid(strShipmentNo)) {
			eleAcadStoreActionData.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
			eleAcadStoreActionData.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO + AcademyConstants.ATTR_QRY_TYPE,
					AcademyConstants.STR_GREATER_THAN_OR_EQUALS);

		}

		Element eleComplexQry = SCXmlUtil.createChild(eleAcadStoreActionData, AcademyConstants.COMPLEX_QRY_ELEMENT);
		eleComplexQry.setAttribute(AcademyConstants.COMPLEX_OPERATOR_ATTR, AcademyConstants.COMPLEX_OPERATOR_AND_VAL);

		Element eleOr = SCXmlUtil.createChild(eleComplexQry, AcademyConstants.COMPLEX_OR_ELEMENT);
		Element eleExpY = SCXmlUtil.createChild(eleOr, AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExpY.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
		eleExpY.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.STR_NOTIFY_STORE);
		eleExpY.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STR_YES);

		Element eleExp1 = SCXmlUtil.createChild(eleOr, AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp1.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
		eleExp1.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.STR_NOTIFY_STORE);
		eleExp1.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STR_DIGIT_ONE);

		Element eleExp2 = SCXmlUtil.createChild(eleOr, AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp2.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
		eleExp2.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.STR_NOTIFY_STORE);
		eleExp2.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STR_DIGIT_TWO);

		Element eleOrderBy = SCXmlUtil.createChild(eleAcadStoreActionData, AcademyConstants.ELE_ORDERBY);
		Element eleAttribute = SCXmlUtil.createChild(eleOrderBy, AcademyConstants.ELE_ATTRIBUTE);

		eleAttribute.setAttribute(AcademyConstants.ATTR_DESC_SHORT, AcademyConstants.STR_NO);
		eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_SHIPMENT_NO);

		log.endTimer("AcademyBOPISProcessInstorePickupNotification.prepareInputForGetAcadStoreActionData()");

		return docAcadStoreActionData;
	}

	/**
	 * This method is removes duplicate records and updates what notifications are
	 * eligible for each shipment
	 * 
	 * @param docAcadStoreActionData
	 * @param inXML
	 * @return docAcadStoreActionData
	 * @throws ParseException 
	 * @throws Exception
	 */

	private Document processStoreActionDataOutput(Document docGetAcadStoreActionDataList, Document inXML) throws ParseException
			 {
		log.beginTimer("AcademyBOPISProcessInstorePickupNotification.processStoreActionDataOutput()");

		NodeList nlAcadStoreActionData = docGetAcadStoreActionDataList.getDocumentElement()
				.getElementsByTagName(AcademyConstants.STR_ACAD_STORE_ACTION_DATA);

		log.verbose(":: nlAcadStoreActionData :: " + nlAcadStoreActionData.getLength());

		String strPreviousShipmentNo = null;

		// Loop through each record and see which records are valid.
		for (int iAcadStoreActionData = 0; iAcadStoreActionData < nlAcadStoreActionData.getLength(); iAcadStoreActionData++) {
			Element eleAcadStoreActionData = (Element) nlAcadStoreActionData.item(iAcadStoreActionData);
			String strShipmentNo = eleAcadStoreActionData.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			log.verbose(
					":: strPreviousShipmentNo :: " + strPreviousShipmentNo + ":: strShipmentNo :: " + strShipmentNo);

			if (!YFCObject.isVoid(strPreviousShipmentNo) && strPreviousShipmentNo.equals(strShipmentNo)) {
				log.verbose(":: Duplicate Shipment No strPreviousShipmentNo :: " + strPreviousShipmentNo);
				// Update the duplicate record as Closed.
				eleAcadStoreActionData.setAttribute("NotifyStoreUpdated", AcademyConstants.STR_NO);
			} else {
				String strNotifyStoreUpdated = validateShipmentNotification(eleAcadStoreActionData, inXML);

				if (YFCObject.isVoid(strNotifyStoreUpdated)) {
					// Shipment not eligible for Notification. Remove from input
					 eleAcadStoreActionData.getParentNode().removeChild(eleAcadStoreActionData);
					iAcadStoreActionData--;
				} else {
					eleAcadStoreActionData.setAttribute(AcademyConstants.STR_NOTIFY_STORE_UPDATED,
							strNotifyStoreUpdated);
				}
			}

			strPreviousShipmentNo = strShipmentNo;
		}

		log.endTimer("AcademyBOPISProcessInstorePickupNotification.processStoreActionDataOutput()");
		return docGetAcadStoreActionDataList;
	}

	/**
	 * This method validates if the record is eligible for any notification
	 * 
	 * @param eleAcadStoreActionData
	 * @param inXML
	 * @return String
	 * @throws ParseException 
	 * @throws Exception
	 */

	private String validateShipmentNotification(Element eleAcadStoreActionData, Document inXML) throws ParseException  {
		log.beginTimer("AcademyBOPISProcessInstorePickupNotification.validateShipmentNotification()");

		SimpleDateFormat dateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		Calendar cal = Calendar.getInstance();

		int iFirstNotificationTime = 0;
		int iSecondNotificationTime = 0;
		int iThirdNotificationTime = 0;

		String strFirstNotificationTime = inXML.getDocumentElement()
				.getAttribute(AcademyConstants.STR_FIRST_NOTIFICATION);
		String strSecondNotificationTime = inXML.getDocumentElement()
				.getAttribute(AcademyConstants.STR_SECOND_NOTIFICATION);
		String strThirdNotificationTime = inXML.getDocumentElement()
				.getAttribute(AcademyConstants.STR_THIRD_NOTIFICATION);

		if (!YFCObject.isVoid(strFirstNotificationTime))
			iFirstNotificationTime = Integer.parseInt(strFirstNotificationTime);

		if (!YFCObject.isVoid(strSecondNotificationTime))
			iSecondNotificationTime = Integer.parseInt(strSecondNotificationTime);

		if (!YFCObject.isVoid(strThirdNotificationTime))
			iThirdNotificationTime = Integer.parseInt(strThirdNotificationTime);

		log.verbose(":: iFirstNotificationTime :: " + iFirstNotificationTime + ":: iSecondNotificationTime :: "
				+ iSecondNotificationTime + " :: iThirdNotificationTime :: " + iThirdNotificationTime);

		String strCreatets = eleAcadStoreActionData.getAttribute(AcademyConstants.ATTR_CREATETS);
		String strCurrentNotifyStore = eleAcadStoreActionData.getAttribute(AcademyConstants.STR_NOTIFY_STORE);

		Date sysDate = cal.getTime();
		String strSysDate = dateFormat.format(sysDate);
		Date dtstrSysDate = dateFormat.parse(strSysDate);
		Date dtStartPickupCreatets = dateFormat.parse(strCreatets);

		long lTimeDiff = Math.abs(dtstrSysDate.getTime() - dtStartPickupCreatets.getTime());
		log.verbose("Time difference of current time and Start Pickup Createts in milliseconds:: " + lTimeDiff);

		int iMinuteDiff = (int) TimeUnit.MINUTES.convert(lTimeDiff, TimeUnit.MILLISECONDS);
		log.verbose("Minute difference of current time and Start Pickup Createts : " + iMinuteDiff);

		if (iFirstNotificationTime > 0 && iMinuteDiff < iFirstNotificationTime) {
			// ShipmentNo not eligible for Notification
			log.verbose("ShipmentNo not eligible for Notification");
			return null;
		}
		// Eligible for First Notification
		else if (iFirstNotificationTime > 0 && iSecondNotificationTime > iFirstNotificationTime
				&& iMinuteDiff > iFirstNotificationTime && (AcademyConstants.STR_YES.equals(strCurrentNotifyStore))) {
			log.verbose(":Eligible for Notification 1 "
					+ eleAcadStoreActionData.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
			return AcademyConstants.STR_DIGIT_ONE;
		}
		// Eligible for Second Notification
		else if (iSecondNotificationTime > 0 && iThirdNotificationTime > iSecondNotificationTime
				&& iMinuteDiff > iSecondNotificationTime
				&& (AcademyConstants.STR_DIGIT_ONE.equals(strCurrentNotifyStore))) {
			log.verbose(":Eligible for Notification 2 "
					+ eleAcadStoreActionData.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
			return AcademyConstants.STR_DIGIT_TWO;
		}
		// Eligible for Third Notification
		else if (iThirdNotificationTime > 0 && iMinuteDiff > iThirdNotificationTime
				&& (AcademyConstants.STR_DIGIT_TWO.equals(strCurrentNotifyStore))) {
			log.verbose(":Eligible for Notification 3 "
					+ eleAcadStoreActionData.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
			return AcademyConstants.STR_DIGIT_THREE;
		}
		// Edge case error handling
		else {
			log.verbose(": Invalid Data or Not handled in OMS :: "
					+ eleAcadStoreActionData.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
		}

		log.endTimer("AcademyBOPISProcessInstorePickupNotification.validateShipmentNotification()");
		return null;
	}

	/**
	 * This method executes getShipmentList API to fetch the shipment details.
	 * Sample Input XML <Shipment ShipmentNo="201910254"/>
	 * 
	 * @param env
	 * @param strShipmentNo
	 * @return outDoc
	 * @throws Exception
	 **/
	private Document getShipmentList(YFSEnvironment env, String strShipmentNo) throws Exception {
		log.beginTimer("AcademyExecShipActionDataForRemNotify.getShipmentList() method");

		Document docIngetShipmentList = null;
		Document docOutgetShipmentList = null;

		// getShipmentList API inDoc
		docIngetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		docIngetShipmentList.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);

		// getShipmentList API Template
		Document docgetShipmentListTemplate = XMLUtil
				.getDocument("<Shipments>" + "<Shipment ShipmentNo=\"\" OrderNo=\"\" Status=\"\"  ShipNode=\"\" >"
						+ "</Shipment>" + "</Shipments>");
		log.verbose("getShipmentList API indoc XML : \n" + XMLUtil.getXMLString(docIngetShipmentList));
		// Invoking the getShipmentList API
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docgetShipmentListTemplate);
		docOutgetShipmentList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
				docIngetShipmentList);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);

		log.verbose("Outdoc of getShipmentList :: \n" + XMLUtil.getXMLString(docOutgetShipmentList));
		log.endTimer("AcademyExecShipActionDataForRemNotify.getShipmentList() method");

		return docOutgetShipmentList;
	}

	/**
	 * This method updates the NotifyStore attribute in the ACAD_STORE_ACTION_DATA
	 * table
	 * 
	 * @param env
	 * @param strAcadStoreActionDataKey
	 * @param strNotifyStoreUpdated
	 * @return
	 * @throws Exception
	 * Sample Input XML:
	 * <AcadStoreActionData NotifyStore="1" StoreActionDataKey="202208120226375667174879"/>
	 */

	private void updateAcadStoreActionData(YFSEnvironment env, String strAcadStoreActionDataKey,
			String strNotifyStoreUpdated) throws Exception {
		log.beginTimer("AcademyBOPISProcessInstorePickupNotification.updateAcadStoreActionData() method");

		Document docAcadStoreActionDataInput = XMLUtil.createDocument(AcademyConstants.STR_ACAD_STORE_ACTION_DATA);
		Element eleRoot = docAcadStoreActionDataInput.getDocumentElement();
		eleRoot.setAttribute(AcademyConstants.STR_STORE_ACTION_DATA_KEY, strAcadStoreActionDataKey);
		eleRoot.setAttribute(AcademyConstants.STR_NOTIFY_STORE, strNotifyStoreUpdated);

		log.verbose("Outdoc of docAcadStoreActionDataInput :: \n" + XMLUtil.getXMLString(docAcadStoreActionDataInput));
		AcademyUtil.invokeService(env, AcademyConstants.STR_SERV_ACADEMY_CHANGE_ACAD_STORE_ACTION_DATA,
				docAcadStoreActionDataInput);
		log.endTimer("AcademyBOPISProcessInstorePickupNotification.updateAcadStoreActionData() method");

	}

	/**
	 * This method prepares the request and sends the In-store Pick notification to
	 * TC70
	 * 
	 * @param env
	 * @param strShipmentNo
	 * @param strOrderNo
	 * @param strShipNode
	 * @param strContentTitle
	 * @return
	 * @throws Exception
	 * Sample Output XML:
	 * <Shipment ContentTitle="INSTORE_PICKUP_NOTIF_1" OrderNo="441622024"
    	ShipNode="033" ShipmentNo="100497831"/>

	 */

	private void prepareAndSendNotification(YFSEnvironment env, String strShipmentNo, String strOrderNo,
			String strShipNode, String strContentTitle) throws Exception {
		log.beginTimer("AcademyBOPISProcessInstorePickupNotification.prepareNotificationMsgForInstorePickup() method");

		Document docShipmentRoot = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleRoot = docShipmentRoot.getDocumentElement();
		eleRoot.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		eleRoot.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		eleRoot.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strShipNode);
		eleRoot.setAttribute(AcademyConstants.ELE_SHIPEMNT_CONTENT_TITLE, strContentTitle);

		log.verbose("Outdoc of prepareNotificationMsgForInstorePickup:: docShipmentRoot :: \n"
				+ XMLUtil.getXMLString(docShipmentRoot));
		AcademyUtil.invokeService(env, AcademyConstants.STR_SERV_ACADEMY_POST_INSTORE_PCK_NOTI_MSG, docShipmentRoot);

		log.endTimer("AcademyBOPISProcessInstorePickupNotification.prepareNotificationMsgForInstorePickup() method");

	}

}
