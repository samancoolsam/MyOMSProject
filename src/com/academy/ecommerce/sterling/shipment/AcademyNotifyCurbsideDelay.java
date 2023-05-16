package com.academy.ecommerce.sterling.shipment;
/*##################################################################################
 * Project Name                : CurbSide Acknowledgement
 * Module                      : OMS
 * Author                      : CTS
 * Date                        : 12-SEP-2022 
 * Description                 : This class prepares and posts an SMS and Push Notification xml to respective queues (SMS-ESB , PushNotification-Mobile app ) whenever store associate clicks on delay buttons for an curbside order
 * Change Revision
 * ---------------------------------------------------------------------------------
 * Date            Author                Version#       Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 12-SEP-2022      CTS                   1.0               Initial version
 * 23-SEP-2023      CTS                   2.0		Updated method for PushNotification
 *
 * ##################################################################################*/
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/*
 * Sample Input:
 * 
	<Shipment BillToDayPhone="7730012642" BillToZipCode="77064"
	IgnoreOrdering="Y"
	Msg1="We?re almost there. Curbside delivery of your order Order_No is taking longer than expected. We"
	Msg2="will be coming to you with your items shortly. We apologize for the inconvenience. Check your"
	Msg3="Curbside Pick Up status at"
	NotifyDelayToCustFlag="Y" OrderNo="5514092022" ShipNode="033" ShipmentKey="20220914081226288300203">
	<Extn ExtnCurbsideAttendedBy="sfs033" ExtnCurbsideDelayCount="1"
    ExtnCurbsideDelayMins="2" ExtnCurbsideExpectedDeliveryTS="9/21/2022, 2:19:02 AM"/>
	</Shipment> */

public class AcademyNotifyCurbsideDelay implements YIFCustomApi{

	private Properties props;


	public void setProperties(Properties props) {
		this.props = props;
	}



	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyNotifyCurbsideDelay.class);

	/**
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	//This method fetches all the values sent from UI , prepared SMS doc and posts to ESB queue
	//OMNI -82169 -Start - Send SMS on curbside Delay	public Document sendSMSAndPushNotification(YFSEnvironment env, Document inDoc) throws Exception {
	public Document sendSMSOnCurbsideDelay (YFSEnvironment env, Document inDoc)  {

		log.verbose(
				"AcademyNotifyCurbsideDelay.sendSMSOnCurbsideDelay()_InXML:" + XMLUtil.getXMLString(inDoc));

		String shipNode = null;
		String orderNo = null;
		String billToDayPhone= null;
		String billToZipCode= null;
		String markForDayPhone= null;
		String smsMsg1= null;
		String smsMsg2= null;
		String smsMsg3= null;

		try {	
		shipNode = XPathUtil.getString(inDoc.getDocumentElement(), AcademyConstants.XPATH_SHIP_NODE);
		orderNo = XPathUtil.getString(inDoc.getDocumentElement(), AcademyConstants.XPATH_ORDERNO);
		billToDayPhone = XPathUtil.getString(inDoc.getDocumentElement(), AcademyConstants.XPATH_BILL_TO_DAY_PHONE);
		billToZipCode = XPathUtil.getString(inDoc.getDocumentElement(), AcademyConstants.XPATH_BILL_TO_ZIP_CODE);
		markForDayPhone = XPathUtil.getString(inDoc.getDocumentElement(),AcademyConstants.XPATH_MARK_FOR_DAY_PHONE);
		smsMsg1 =XPathUtil.getString(inDoc.getDocumentElement(), AcademyConstants.XPATH_MSG1);
		smsMsg2 = XPathUtil.getString(inDoc.getDocumentElement(), AcademyConstants.XPATH_MSG2);
		smsMsg3 = XPathUtil.getString(inDoc.getDocumentElement(), AcademyConstants.XPATH_MSG3);
		

			log.verbose("Shipnode ::"+shipNode);
		//Get url from customeroverride.properties
		String strCurbsideDelayLink = YFSSystem.getProperty(AcademyConstants.CURBSIDE_DELAY_URL);

			log.verbose("CurbSide Delay Link is ::"+strCurbsideDelayLink);

		if (!YFCObject.isVoid(smsMsg1) && !YFCObject.isVoid(smsMsg2) && !YFCObject.isVoid(smsMsg3) && !YFCObject.isVoid(strCurbsideDelayLink) && !YFCObject.isVoid(billToZipCode)){
			String strDelayMessage= smsMsg1.concat(AcademyConstants.STR_BLANK).concat(smsMsg2).concat(AcademyConstants.STR_BLANK).concat(smsMsg3);
			

			strDelayMessage=strDelayMessage.replace(AcademyConstants.SMS_ORDER_NO, AcademyConstants.SPL_CHAR_HASH+orderNo);

				log.verbose("Message String :: " + strDelayMessage);

				strCurbsideDelayLink= strCurbsideDelayLink.replace(AcademyConstants.ATT_$ORDER_NO, orderNo);
				strCurbsideDelayLink= strCurbsideDelayLink.replace(AcademyConstants.ATT_$STORE_NO, shipNode);
				strCurbsideDelayLink= strCurbsideDelayLink.replace(AcademyConstants.ATTR_$BILLING_ZIPCODE, billToZipCode);

				strDelayMessage= strDelayMessage.concat(AcademyConstants.STR_BLANK).concat(strCurbsideDelayLink);
				log.verbose("Message String after appending link ::" + strDelayMessage);


				Document docSMSMessage = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
				docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE, strDelayMessage);

				if (!YFCObject.isVoid(billToDayPhone)) {
					docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_DAY_PHONE, billToDayPhone);
					log.verbose("primary customer SMS message : " + XMLUtil.getXMLString(docSMSMessage));
					AcademyUtil.invokeService(env,AcademyConstants.SERVICE_BOPIS_POST_SMS_TO_Q,docSMSMessage);
					
				}
				if (!YFCObject.isVoid(markForDayPhone)) {
					docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_DAY_PHONE, markForDayPhone);
					log.verbose("alternate person SMS message : " + XMLUtil.getXMLString(docSMSMessage));
					AcademyUtil.invokeService(env,AcademyConstants.SERVICE_BOPIS_POST_SMS_TO_Q,docSMSMessage);
				
				}

			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

		return inDoc;

	}

	//OMNI -82169 -End - Send SMS on curbside Delay

	/* This method gets the shipmentkey form the i/p and calls getShipmentList, massages the output as per push notification template and appends MessageType (from service arguments)to it. */
	/**
	 * @param env
	 * @param inDoc
	 * @return docPushNotificationMsg
	 * @throws Exception
	 */
	//OMNI -82170 -End - Send PushNotification on curbside Delay
	public Document sendPushNotifyOnCurbsideDelay (YFSEnvironment env, Document inDoc)  {
		log.verbose(
				"AcademyNotifyCurbsideDelay.sendPushNotifyOnCurbsideDelay()_InXML:" + XMLUtil.getXMLString(inDoc));
		try {
			String strMessageType =  props.getProperty(AcademyConstants.ATTR_MESSAGE_TYPE, AcademyConstants.STR_CURBSIDE_PICKUP_DELAYED);
			log.verbose("MessageType from service arguments:  " +strMessageType);

			String strShipmentKey = XPathUtil.getString(inDoc.getDocumentElement(),AcademyConstants.XPATH_SHIPMENT_KEY);

			Document getShipmentDetailListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);

			//sending shipment key for getShipmentList() Input
			getShipmentDetailListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			log.verbose("Input to getShipmentList ::" +XMLUtil.getXMLString(getShipmentDetailListInDoc));
			env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, AcademyConstants.TEMPLATE_GET_SHIPMENT_LIST1);
			Document getShipmentListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, getShipmentDetailListInDoc);
			env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
			log.verbose("Output getShipmentList :: " +XMLUtil.getXMLString(getShipmentListOutDoc));


			NodeList currentShipmentLines = XPathUtil.getNodeList(getShipmentListOutDoc, AcademyConstants.XPATH_ELE_SHIPMENT_LINE);
			//Fetching Order element 
			Element eleOrder = XMLUtil.getElementByXPath(getShipmentListOutDoc, AcademyConstants.XPATH_ELE_ORDER);

			//Creating new Document for push notfication (Append MessageType, OrderLines, OrderLine Elements)
			Document pushNotificationMsgDoc = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			XMLUtil.copyElement(pushNotificationMsgDoc, eleOrder, pushNotificationMsgDoc.getDocumentElement());
			pushNotificationMsgDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, strMessageType);
			Element eleOrderLinesForPushNotify = XMLUtil.createElement(pushNotificationMsgDoc, AcademyConstants.ELE_ORDER_LINES, true);
			pushNotificationMsgDoc.getDocumentElement().appendChild(eleOrderLinesForPushNotify);
			log.verbose("Initial Push notification xml ::" +XMLUtil.getXMLString(pushNotificationMsgDoc));

			//Fetch the order lines from each shipment line and append them to "OrderLines" Element
			Element eleOrderLines = XMLUtil.createElement(getShipmentListOutDoc, AcademyConstants.ELE_ORDER_LINES, true);

			int shipmentLinesLength = currentShipmentLines.getLength();
			for (int i = 0; i < shipmentLinesLength; i++) {	
				Element shipmentLine = (Element) currentShipmentLines.item(i);
				//fetch orderlineKey for current shipment line
				Element eleOrderLine = XmlUtils.getChildElement(shipmentLine, AcademyConstants.ELE_ORDER_LINE);
				//store orderlineKey in hashset
				eleOrderLines.appendChild(eleOrderLine);				

			}

			//copy orderLines to new doc
			XMLUtil.copyElement(pushNotificationMsgDoc, eleOrderLines, eleOrderLinesForPushNotify);

			//Create elements Shipments and shipment in new doc (Append Shipments to Order and Shipment to Shipments as child elements)
			Element eleShipments = XMLUtil.createElement(pushNotificationMsgDoc, AcademyConstants.ELE_SHIPMENTS, true);
			pushNotificationMsgDoc.getDocumentElement().appendChild(eleShipments);
			Element eleShipmentForPushNotify = XMLUtil.createElement(pushNotificationMsgDoc, AcademyConstants.ELE_SHIPMENT, true);
			eleShipments.appendChild(eleShipmentForPushNotify);

			//Fetch the shipment element from getShipmentListOutput and remove shipmentLines Element
			Element eleShipment = XMLUtil.getElementByXPath(getShipmentListOutDoc, AcademyConstants.XPATH_SHIPMENT);
			Node shipmentLines = XMLUtil.getNode(eleShipment, AcademyConstants.XPATH_ELE_SHIPMENT_LINES);
			if (!YFCObject.isVoid(shipmentLines)) {
				XMLUtil.removeChild(eleShipment, (Element) shipmentLines);
			}

			//Copy Shipment Element from getShipmentListOutput to new Doc
			XMLUtil.copyElement(pushNotificationMsgDoc, eleShipment, eleShipmentForPushNotify);
			log.verbose("Final Push notification xml to mobile app Q:: " +XMLUtil.getXMLString(pushNotificationMsgDoc));
			return pushNotificationMsgDoc;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

	}
	//OMNI -82170 -End - Send PushNotification on curbside Delay
}
