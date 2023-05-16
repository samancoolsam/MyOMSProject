package com.academy.ecommerce.sterling.bopis.order.api;

import java.util.ArrayList;
import java.util.List;

/**#########################################################################################
*
* Project Name                : OMS_APR_2020.CURB SIDE
* Module                      : OMNI-5398,5399,5600
* Author                      : C0015576
* Author Group				  : CTS-POD
* Date                        : 27-Mar-2020 
* Description				  : This class publish SMS to ESB queue when BOPIS order is moved
* 								to Ready For Customer Pick Status, the code is updated to 
* 								enable curbside pick up.
* 								 
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       		Remarks/Description                      
* ---------------------------------------------------------------------------------
* 27-Mar-2020		CTS  	 			  1.0           	Updated version
* 09-Apr-2020		CTS  	 			  2.0           	Updated version
*
* #########################################################################################*/

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyBOPISCheckAndSendSMSAPI implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyBOPISCheckAndSendSMSAPI.class);
	private Properties props;
	private String strSOFDepartments;

	public Document checkAndSendSMS(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("Start of method checkAndSendSMS : input is :: " + XMLUtil.getXMLString(inDoc));

		String strStatus = "";
		String smsFOR = "";
		String messageType = "";
		String primaryPhoneNo = "";
		String alternatePhoneNo = "";
		String strDeliveryMethod = "";
		String strShipmentNo = "";
		String strShipNode = "";
		String strMessage = "";
		String strCurbPickUpLink = "";
		String strFinalReminder = "";
		Document outDocGetShipmentList = null;
		Element eleInputShip = null;
		//OMNI-72427 start
		String strBillingZipcode="";
		String strBillingZipCodeArg = AcademyConstants.CURBSIDE_BILLING_ZIPCODE_ARG;
		//OMNI-72427 End
		
		String strCurbPickUpOrderLink = YFSSystem.getProperty(AcademyConstants.PROP_CURB_PICKUP_ORDER_URL); 
		log.verbose("Curb Pick Up Link is "+strCurbPickUpOrderLink);
		
		String strRootString = inDoc.getDocumentElement().getNodeName();

		if (!YFCCommon.isVoid(strRootString) && strRootString.equals(AcademyConstants.ELE_MONITOR_CONSOLIDATION))
			eleInputShip = SCXmlUtil.getChildElement(inDoc.getDocumentElement(), AcademyConstants.ELE_SHIPMENT);
		else
			eleInputShip = inDoc.getDocumentElement();
		//OMNI-81665 Start
		Element eleIndoc = inDoc.getDocumentElement();
		String strOrdlvlRemConsol = eleIndoc.getAttribute(AcademyConstants.STR_IS_REMINDER_CONSOLIDATION_ENABLED);
		if ((!YFCObject.isVoid(strOrdlvlRemConsol)
			&& strOrdlvlRemConsol.equals(AcademyConstants.STR_YES)) && strCurbPickUpOrderLink.contains("$Shipment_No")) {
			strCurbPickUpOrderLink = strCurbPickUpOrderLink.replace("-$Shipment_No-", "-");
		log.verbose("Curb Pick Up Link is when consolidation is enabled::"+strCurbPickUpOrderLink);
		}
		//OMNI-81665 End
		// Start OMNI-5398,5399
		String strIsCurbSidePickUpEnabled = props.getProperty(AcademyConstants.ATTR_IS_CURB_SIDE_PICKUP_ENABLED);
		log.verbose("IsCurbSidePickUpEnabled:: "+strIsCurbSidePickUpEnabled);
		// End OMNI-5398,5399
		//Start OMNI 4347,6212 Partial Cancel + Pick SMS
		boolean bHasCancelledLines = checkforCancelledLines(eleInputShip);
		//End OMNI 4347,6212 Partial Cancel + Pick SMS
		strStatus = eleInputShip.getAttribute(AcademyConstants.ATTR_STATUS);
		strDeliveryMethod = eleInputShip.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
		// Start OMNI-5398,5399
		strShipmentNo = eleInputShip.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		strShipNode = eleInputShip.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
		// End OMNI-5398,5399
		//Start OMNI-4351 Last day Reminder SMS
		strFinalReminder = eleInputShip.getAttribute(AcademyConstants.ATTR_FINAL_REMINDER);
		//End OMNI-4351 Last day Reminder SMS
		// chekcing for the shipment status "Ready For Customer Pick"
		if (!YFCCommon.isVoid(strStatus) && strStatus.equals(AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS)) {
			if (!YFCCommon.isVoid(strRootString) && strRootString.equals(AcademyConstants.ELE_MONITOR_CONSOLIDATION)) {
				if(!YFCObject.isVoid(strIsCurbSidePickUpEnabled) && strIsCurbSidePickUpEnabled.equals(AcademyConstants.ATTR_Y)) {
					//Start OMNI-4351 Last day Reminder SMS
					if(!YFCObject.isVoid(strFinalReminder) && strFinalReminder.equals(AcademyConstants.ATTR_Y))
						smsFOR = "FINAL_CURB_PICK_REM";
					else
					//End OMNI-4351 Last day Reminder SMS
						// Start OMNI-5398,5399
						smsFOR = "CURB_PICKUP_REMINDER";
						// End OMNI-5398,5399
				}
				else {//Start OMNI-4351 Last day Reminder SMS
					if(!YFCObject.isVoid(strFinalReminder) && strFinalReminder.equals(AcademyConstants.ATTR_Y))
						smsFOR = "FINAL_CUST_PICK_REM";
					else
					  //End OMNI-4351 Last day Reminder SMS
						smsFOR = "CUST_PICKUP_REMINDER";		
				}
				messageType = "Reminder Ready for Pickup";
			} else {
				if(!YFCObject.isVoid(strIsCurbSidePickUpEnabled) && strIsCurbSidePickUpEnabled.equals(AcademyConstants.ATTR_Y)) {
					//Start OMNI 4347 Partial Cancel + Pick SMS
					if(bHasCancelledLines)
						smsFOR = "RDY_CURB_PICK_CANCEL";
					//End OMNI 4347 Partial Cancel + Pick SMS
					else
						// Start OMNI-5398,5399
						smsFOR = "READY_FOR_CURB_PICK";
						// End OMNI-5398,5399
				}
				else {//Start OMNI - 4347 Partial Cancel + Pick SMS
					if(bHasCancelledLines)
						smsFOR = "RDY_CUST_PICK_CANCEL";
					  //End OMNI - 4347 Partial Cancel + Pick SMS
					else
						smsFOR = "READY_FOR_CUST_PICK";
					
				}
				messageType = "Ready for Pickup";
			}
		} else if (!YFCCommon.isVoid(strStatus) && strStatus.equals(AcademyConstants.VAL_SHIPPED_STATUS)) {
			smsFOR = "PICKUP_CONFIRM";
			messageType = "Pickup Confirmation";
		}
		log.verbose("Shipment Staus :" + strStatus + "delivery method :" + strDeliveryMethod);
		// checking whether shipment is BOPIS Shipment or not based on delivery method
		if (!YFCCommon.isVoid(strDeliveryMethod) && strDeliveryMethod.equals(AcademyConstants.STR_PICK)
				&& !YFCCommon.isVoid(smsFOR)) {

			outDocGetShipmentList = getShipmentList(env, eleInputShip.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
			Element eleOutShipLine = (Element) inDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE).item(0);
			// Code Changes for OMNI-42477 Start
			if (!YFCCommon.isVoid(strStatus) && strStatus.equals(AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS)) {
			strSOFDepartments = props.getProperty("SOFItemDeps");
			//OMNI-95706--start
			List<Element> eleDocGetShipmentList = SCXmlUtil.getElements(outDocGetShipmentList.getDocumentElement(),
					"Shipment/ShipmentLines/ShipmentLine/Order/OrderLines/OrderLine");
			for (Element eleOrderLine : eleDocGetShipmentList) {
				String strExtnDepartmentName = SCXmlUtil.getXpathAttribute(eleOrderLine,
						"ItemDetails/Extn/@ExtnDepartmentName");
				String strMaxLineStatus = eleOrderLine.getAttribute(AcademyConstants.ATTR_MAXLINE_STATUS);
				if (!YFCCommon.isVoid(strSOFDepartments) && !YFCCommon.isVoid(strExtnDepartmentName)
						&& strSOFDepartments.contains(strExtnDepartmentName) && !YFCCommon.isVoid(strMaxLineStatus) && !strMaxLineStatus.equals("9000")) {
					eleOrderLine.setAttribute("ShowCurbsideInstructions", AcademyConstants.STR_NO);
					//OMNI-95706--end
					strIsCurbSidePickUpEnabled = "N";
					strCurbPickUpOrderLink = "N";
					//OMNI-99368 - Start
					if (!YFCCommon.isVoid(strRootString)
							&& strRootString.equals(AcademyConstants.ELE_MONITOR_CONSOLIDATION)) {
						if (!YFCObject.isVoid(strFinalReminder)
								&& strFinalReminder.equals(AcademyConstants.ATTR_Y)) {
							smsFOR = "FINAL_CUST_PICK_REM";
						} else {
							smsFOR = "CUST_PICKUP_REMINDER";
						}
					}//OMNI-99368 - End 
					else {
						if (bHasCancelledLines)
							smsFOR = "READY_NOCURB_PICK_CN";
						else
							smsFOR = "READY_NOCURB_PICK";
					}
					break;
					}
				}
			}
			// Code Changes for OMNI-42477 End
			String strOHK = "";
			if (!YFCCommon.isVoid(eleOutShipLine))
				strOHK = eleOutShipLine.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
			log.verbose("Message is for " + messageType);
			// checking whether smsFOR is empty or not
			if (!YFCCommon.isVoid(strOHK)) {

				Document docOutGetOrderList = getOrderList(env, strOHK);
				Element eleOrderOut = SCXmlUtil.getChildElement(docOutGetOrderList.getDocumentElement(),
						AcademyConstants.ELE_ORDER);
				Element elePersonInfoBillToOut = SCXmlUtil.getChildElement(eleOrderOut,
						AcademyConstants.ELE_PERSON_INFO_BILL_TO);
				Element elePersonInfoMarkForOut = SCXmlUtil.getChildElement(eleOrderOut,
						AcademyConstants.ELE_PERSON_INFO_MARK_FOR);

				if (!YFCCommon.isVoid(elePersonInfoBillToOut)) {
					primaryPhoneNo = elePersonInfoBillToOut.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
				}
				if (!YFCCommon.isVoid(elePersonInfoMarkForOut)) {
					alternatePhoneNo = elePersonInfoMarkForOut.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
				}

				if (!YFCCommon.isVoid(primaryPhoneNo) || !YFCCommon.isVoid(alternatePhoneNo)) {
					// Start OMNI-5398,5399
					/*
					 * String mes1 = ""; String mes2 = ""; String mes3 = "";
					 */

					// prepare input doc for getCommonCodeList to get mes1,mes2 and mes3 values,
					// which further used to for SMS message.
					Document outDocGetCommonCodeList = getCommonCodeList(env, smsFOR);
					
					/*
					 * Document outDocGetCommonCodeList =
					 * XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
					 * inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.
					 * ATTR_CODE_TYPE, smsFOR); log.verbose("input to getCommonCodeList " +
					 * XMLUtil.getXMLString(inDocGetCommonCodeList));
					 * 
					 * Document outDocGetCommonCodeList = AcademyUtil.invokeAPI(env,
					 * AcademyConstants.API_GET_COMMON_CODELIST, inDocGetCommonCodeList);
					 * log.verbose("output of getCommonCodeList " +
					 * XMLUtil.getXMLString(outDocGetCommonCodeList));
					 */
					
					String strSMSMessage = getSMSMessage(outDocGetCommonCodeList, smsFOR);

					/*
					 * NodeList commonCodeNL = outDocGetCommonCodeList
					 * .getElementsByTagName(AcademyConstants.ELE_COMMON_CODE); for (int i = 0; i <
					 * commonCodeNL.getLength(); i++) { Element tempCommonCodeEle = (Element)
					 * commonCodeNL.item(i); if
					 * (tempCommonCodeEle.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE)
					 * .equals(smsFOR + "_Mes1")) mes1 =
					 * tempCommonCodeEle.getAttribute(AcademyConstants.CODE_LONG_DESC); else if
					 * (tempCommonCodeEle.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE)
					 * .equals(smsFOR + "_Mes2")) mes2 =
					 * tempCommonCodeEle.getAttribute(AcademyConstants.CODE_LONG_DESC); else if
					 * (tempCommonCodeEle.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE)
					 * .equals(smsFOR + "_Mes3")) mes3 =
					 * tempCommonCodeEle.getAttribute(AcademyConstants.CODE_LONG_DESC); }
					 */
					// Prepare SMS message document to Queue.
					Document docSMSMessage = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
					docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, messageType);
					String orderNo = eleOrderOut.getAttribute(AcademyConstants.ATTR_ORDER_NO);

					if (smsFOR.equals("PICKUP_CONFIRM")) {
						String strURL_ViewOrderDetails = null;
						String strViewOrderDetails = YFSSystem.getProperty(AcademyConstants.PROP_VIEW_ORDERDETAILS_URL);
						String strZipCode = null;
						if (!YFCCommon.isVoid(elePersonInfoBillToOut)) {

							strZipCode = elePersonInfoBillToOut.getAttribute(AcademyConstants.ZIP_CODE);

						}
						if (!YFCCommon.isVoid(strViewOrderDetails)) {

							strURL_ViewOrderDetails = strViewOrderDetails.replace("@@@@", orderNo);

							strURL_ViewOrderDetails = strURL_ViewOrderDetails.replace("$$$$", strZipCode);
						}
						strMessage = strSMSMessage.replace(AcademyConstants.ATT_$ORDER_NO, orderNo);
						strMessage = strMessage.replace("$Order_Details_Link", strURL_ViewOrderDetails);
					} else {
						
						String strAddress = getStoreAddress(outDocGetShipmentList);
						String strStorePhoneNum = XMLUtil.getAttributeFromXPath(outDocGetShipmentList, AcademyConstants.XPATH_SHIPMENT_STORE_MOBILE);
						log.verbose("StoreMobile :: " + strStorePhoneNum);
						strMessage = strSMSMessage.replace(AcademyConstants.ATT_$ORDER_NO, orderNo);
						strMessage = strMessage.replace(AcademyConstants.ATT_$STORE_ADDRESS, strAddress);
												
						if (strIsCurbSidePickUpEnabled.equals(AcademyConstants.ATTR_Y)) {
							//OMNI-72427 End
							if (!YFCCommon.isVoid(elePersonInfoBillToOut)) {
								strBillingZipcode=elePersonInfoBillToOut.getAttribute(AcademyConstants.ZIP_CODE);
								strBillingZipCodeArg=strBillingZipCodeArg.concat(strBillingZipcode);

							}
							//OMNI-72427 end
							

							if (!YFCObject.isVoid(strCurbPickUpOrderLink)) {
								
								strCurbPickUpLink = strCurbPickUpOrderLink.replace(AcademyConstants.ATT_$ORDER_NO, orderNo);
								//OMNI-81665 Start
								//removed void condition check for strOrdlvlRemConsol part of OMNI-92779
								if (strCurbPickUpOrderLink.contains("$Shipment_No")) {
									strCurbPickUpLink = strCurbPickUpLink.replace(AcademyConstants.ATT_$SHIPMENT_NO, strShipmentNo);
								}
								//OMNI-81665 End
								strCurbPickUpLink = strCurbPickUpLink.replace(AcademyConstants.ATT_$STORE_NO, strShipNode);
								//OMNI-72427 start
								strCurbPickUpLink = strCurbPickUpLink.concat(strBillingZipCodeArg);
								//OMNI-72427 End
								log.verbose("Curb Pick Up Link is :: " + strCurbPickUpLink);
								strMessage = strMessage.replace(AcademyConstants.ATT_$CURBPICK_LINK, strCurbPickUpLink);
							}

							if (!YFCObject.isVoid(strStorePhoneNum)) {
								strMessage = strMessage.replace(AcademyConstants.ATT_$STORE_MOBILE, strStorePhoneNum);
							}
						}
						else
						{
							if (!YFCObject.isVoid(strStorePhoneNum)) {
								strMessage = strMessage.replace(AcademyConstants.ATT_$STORE_MOBILE, strStorePhoneNum);
							}
						}
						
						/*
						 * Element eleShip =
						 * SCXmlUtil.getChildElement(outDocGetShipmentList.getDocumentElement(),
						 * AcademyConstants.ELE_SHIPMENT);
						 * 
						 * Element eleToAddress = SCXmlUtil.getChildElement(eleShip,
						 * AcademyConstants.ELEM_TOADDRESS); strMessage = mes1 + orderNo + " " + mes2 +
						 * " " //Start : OMNI-4868 : Ready for Pick : Extra space //+
						 * eleToAddress.getAttribute(AcademyConstants.ATTR_FNAME) + " " //End :
						 * OMNI-4868 : Ready for Pick : Extra space +
						 * eleToAddress.getAttribute(AcademyConstants.ATTR_LNAME) + " " +
						 * eleToAddress.getAttribute(AcademyConstants.ATTR_ADDRESS_LINE_1) +". " +
						 * eleToAddress.getAttribute(AcademyConstants.ATTR_CITY) + ", " +
						 * eleToAddress.getAttribute(AcademyConstants.ATTR_STATE) + " " +
						 * eleToAddress.getAttribute(AcademyConstants.ZIP_CODE) + mes3;
						 */
					}
					log.verbose("SMS message Content :: " + strMessage);
					docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE, strMessage);
					// End OMNI-5398,5399
					if (!YFCCommon.isVoid(alternatePhoneNo)) {
						docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_DAY_PHONE,
								alternatePhoneNo);
						log.verbose("alternate person SMS message : " + XMLUtil.getXMLString(docSMSMessage));
						AcademyUtil.invokeService(env, AcademyConstants.ACD_BOPIS_POST_MES_Q_SERVICE, docSMSMessage);
					}
					if (!YFCCommon.isVoid(primaryPhoneNo)) {
						docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_DAY_PHONE, primaryPhoneNo);
						log.verbose("primary customer SMS message : " + XMLUtil.getXMLString(docSMSMessage));
						AcademyUtil.invokeService(env, AcademyConstants.ACD_BOPIS_POST_MES_Q_SERVICE, docSMSMessage);
					}
				}

			}

		}
		return inDoc;
	}
	// New method for Alternate Store SMS
	public Document checkAndSendSMSForAS(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("Start of method checkAndSendSMSForAS : input is :: " + XMLUtil.getXMLString(inDoc));

		String strStatus = "";
		String smsFOR = "";
		String messageType = "";
		String primaryPhoneNo = "";
		String alternatePhoneNo = "";
		String strDeliveryMethod = "";
		String strShipmentNo = "";
		String strShipNode = "";
		String strMessage = "";
		String strCurbPickUpLink = "";
		String strFinalReminder = "";
		Document outDocGetShipmentList = null;
		Element eleInputShip = null;
		// OMNI-72427 start
		String strBillingZipcode = "";
		String strBillingZipCodeArg = AcademyConstants.CURBSIDE_BILLING_ZIPCODE_ARG;
		// OMNI-72427 End
		String smsContent = "";
		String strSMSMessage = "";
		String strExtnASShipNode = "";

		String strCurbPickUpOrderLink = YFSSystem.getProperty(AcademyConstants.PROP_CURB_PICKUP_ORDER_URL);
		log.verbose("Curb Pick Up Link is " + strCurbPickUpOrderLink);
		String strMyAccountUrl = YFSSystem.getProperty(AcademyConstants.STR_MYACCOUNT_URL);
		log.verbose("My Account Url is " + strMyAccountUrl);

		String strRootString = inDoc.getDocumentElement().getNodeName();

		if (!YFCCommon.isVoid(strRootString) && strRootString.equals(AcademyConstants.ELE_MONITOR_CONSOLIDATION))
			eleInputShip = SCXmlUtil.getChildElement(inDoc.getDocumentElement(), AcademyConstants.ELE_SHIPMENT);
		else
			eleInputShip = inDoc.getDocumentElement();
		// OMNI-81665 Start

		Element eleIndoc = inDoc.getDocumentElement();

		String strOrdlvlRemConsol = eleIndoc.getAttribute(AcademyConstants.STR_IS_REMINDER_CONSOLIDATION_ENABLED);

		if ((!YFCObject.isVoid(strOrdlvlRemConsol) && strOrdlvlRemConsol.equals(AcademyConstants.STR_YES))
				&& strCurbPickUpOrderLink.contains("$Shipment_No")) {
			strCurbPickUpOrderLink = strCurbPickUpOrderLink.replace("-$Shipment_No-", "-");
			log.verbose("Curb Pick Up Link is when consolidation is enabled::" + strCurbPickUpOrderLink);
		}

		// OMNI-81665 End
		// Start OMNI-5398,5399
		String strIsCurbSidePickUpEnabled = props.getProperty(AcademyConstants.ATTR_IS_CURB_SIDE_PICKUP_ENABLED);
		log.verbose("IsCurbSidePickUpEnabled:: " + strIsCurbSidePickUpEnabled);
		// End OMNI-5398,5399
		// Start OMNI 4347,6212 Partial Cancel + Pick SMS

		boolean bHasCancelledLines = checkforCancelledLines(eleInputShip);

		// End OMNI 4347,6212 Partial Cancel + Pick SMS
		strStatus = eleInputShip.getAttribute(AcademyConstants.ATTR_STATUS);
		strDeliveryMethod = eleInputShip.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
		// Start OMNI-5398,5399
		strShipmentNo = eleInputShip.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		strShipNode = eleInputShip.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
		// End OMNI-5398,5399
		// Start OMNI-4351 Last day Reminder SMS
		strFinalReminder = eleInputShip.getAttribute(AcademyConstants.ATTR_FINAL_REMINDER);
		// End OMNI-4351 Last day Reminder SMS
		strExtnASShipNode = XPathUtil.getString(eleInputShip, "ShipmentLines/ShipmentLine/Order/Extn/@ExtnASShipNode");

		// chekcing for the shipment status "Ready For Customer Pick"
		if (!YFCCommon.isVoid(strStatus) && strStatus.equals(AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS)) {

			if (!YFCCommon.isVoid(strRootString) && strRootString.equals(AcademyConstants.ELE_MONITOR_CONSOLIDATION)) {
				if (!YFCObject.isVoid(strIsCurbSidePickUpEnabled)
						&& strIsCurbSidePickUpEnabled.equals(AcademyConstants.ATTR_Y)) {
					// Start OMNI-4351 Last day Reminder SMS
					if (!YFCObject.isVoid(strFinalReminder) && strFinalReminder.equals(AcademyConstants.ATTR_Y))
						smsFOR = "FINAL_CURB_PICK_REM";
					else
						// End OMNI-4351 Last day Reminder SMS
						// Start OMNI-5398,5399
						smsFOR = "CURB_PICKUP_REMINDER";
					// End OMNI-5398,5399
				} else {// Start OMNI-4351 Last day Reminder SMS
					if (!YFCObject.isVoid(strFinalReminder) && strFinalReminder.equals(AcademyConstants.ATTR_Y))
						smsFOR = "FINAL_CUST_PICK_REM";
					else
						// End OMNI-4351 Last day Reminder SMS
						smsFOR = "CUST_PICKUP_REMINDER";
				}
				messageType = "Reminder Ready for Pickup";
			} else {
				log.verbose(" Entering into AS Flow :");
				if (!YFCObject.isVoid(strIsCurbSidePickUpEnabled)
						&& strIsCurbSidePickUpEnabled.equals(AcademyConstants.ATTR_Y)) {
					if (!YFCCommon.isVoid(strExtnASShipNode) && strExtnASShipNode.equals(strShipNode)) {
						smsContent = YFSSystem.getProperty(AcademyConstants.STR_CURB_AS_MSG);
					} else {
						smsContent = YFSSystem.getProperty(AcademyConstants.STR_CURB_BOPIS_AS_MSG);
					}

				}

				else {

					if (!YFCCommon.isVoid(strExtnASShipNode) && strExtnASShipNode.equals(strShipNode)) {
						smsContent = YFSSystem.getProperty(AcademyConstants.STR_CUST_AS_MSG);
					} else {
						smsContent = YFSSystem.getProperty(AcademyConstants.STR_CUST_BOPIS_AS_MSG);
					}
				}

				smsContent = smsContent.replace("$MyAccount_Url", strMyAccountUrl);
				log.verbose(" Print the smsContent :" + smsContent);

				messageType = "AlternateReadyForPickUp";
			}
		}

		else if (!YFCCommon.isVoid(strStatus) && strStatus.equals(AcademyConstants.VAL_SHIPPED_STATUS)) {
			smsFOR = "PICKUP_CONFIRM";
			messageType = "Pickup Confirmation";
		}
		log.verbose("Shipment Staus :" + strStatus + "delivery method :" + strDeliveryMethod);
		// checking whether shipment is BOPIS Shipment or not based on delivery method
		if (!YFCCommon.isVoid(strDeliveryMethod) && strDeliveryMethod.equals(AcademyConstants.STR_PICK)
				&& (!YFCCommon.isVoid(smsFOR) || !YFCCommon.isVoid(smsContent))) {

			outDocGetShipmentList = getShipmentList(env, eleInputShip.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
			Element eleOutShipLine = (Element) inDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE).item(0);
			// Code Changes for OMNI-42477 Start
			if (!YFCCommon.isVoid(strStatus) && strStatus.equals(AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS)) {
				strSOFDepartments = props.getProperty("SOFItemDeps");
				// OMNI-95706--start
				List<Element> eleDocGetShipmentList = SCXmlUtil.getElements(outDocGetShipmentList.getDocumentElement(),
						"Shipment/ShipmentLines/ShipmentLine/Order/OrderLines/OrderLine");
				for (Element eleOrderLine : eleDocGetShipmentList) {
					String strExtnDepartmentName = SCXmlUtil.getXpathAttribute(eleOrderLine,
							"ItemDetails/Extn/@ExtnDepartmentName");
					String strMaxLineStatus = eleOrderLine.getAttribute(AcademyConstants.ATTR_MAXLINE_STATUS);
					if (!YFCCommon.isVoid(strSOFDepartments) && !YFCCommon.isVoid(strExtnDepartmentName)
							&& strSOFDepartments.contains(strExtnDepartmentName) && !YFCCommon.isVoid(strMaxLineStatus)
							&& !strMaxLineStatus.equals("9000")) {
						eleOrderLine.setAttribute("ShowCurbsideInstructions", AcademyConstants.STR_NO);
						// OMNI-95706--end
						strIsCurbSidePickUpEnabled = "N";
						strCurbPickUpOrderLink = "N";
						// OMNI-99368 - Start
						if (!YFCCommon.isVoid(strRootString)
								&& strRootString.equals(AcademyConstants.ELE_MONITOR_CONSOLIDATION)) {
							if (!YFCObject.isVoid(strFinalReminder)
									&& strFinalReminder.equals(AcademyConstants.ATTR_Y)) {
								smsFOR = "FINAL_CUST_PICK_REM";
							} else {
								smsFOR = "CUST_PICKUP_REMINDER";
							}
						} // OMNI-99368 - End
						else {
							if (bHasCancelledLines)
								smsFOR = "READY_NOCURB_PICK_CN";
							else
								smsFOR = "READY_NOCURB_PICK";
						}
						break;
					}
				}
			}
			// Code Changes for OMNI-42477 End
			String strOHK = "";
			if (!YFCCommon.isVoid(eleOutShipLine))
				strOHK = eleOutShipLine.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
			log.verbose("Message is for " + messageType);
			// checking whether smsFOR is empty or not
			if (!YFCCommon.isVoid(strOHK)) {

				Document docOutGetOrderList = getOrderList(env, strOHK);
				Element eleOrderOut = SCXmlUtil.getChildElement(docOutGetOrderList.getDocumentElement(),
						AcademyConstants.ELE_ORDER);
				Element elePersonInfoBillToOut = SCXmlUtil.getChildElement(eleOrderOut,
						AcademyConstants.ELE_PERSON_INFO_BILL_TO);
				Element elePersonInfoMarkForOut = SCXmlUtil.getChildElement(eleOrderOut,
						AcademyConstants.ELE_PERSON_INFO_MARK_FOR);

				if (!YFCCommon.isVoid(elePersonInfoBillToOut)) {
					primaryPhoneNo = elePersonInfoBillToOut.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
				}
				if (!YFCCommon.isVoid(elePersonInfoMarkForOut)) {
					alternatePhoneNo = elePersonInfoMarkForOut.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
				}

				if (!YFCCommon.isVoid(primaryPhoneNo) || !YFCCommon.isVoid(alternatePhoneNo)) {

					if (!YFCCommon.isVoid(smsFOR)) {
						Document outDocGetCommonCodeList = getCommonCodeList(env, smsFOR);

						strSMSMessage = getSMSMessage(outDocGetCommonCodeList, smsFOR);
					} else {
						strSMSMessage = smsContent;

					}

					// Prepare SMS message document to Queue.
					Document docSMSMessage = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
					docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, messageType);
					String orderNo = eleOrderOut.getAttribute(AcademyConstants.ATTR_ORDER_NO);

					if (smsFOR.equals("PICKUP_CONFIRM")) {
						String strURL_ViewOrderDetails = null;
						String strViewOrderDetails = YFSSystem.getProperty(AcademyConstants.PROP_VIEW_ORDERDETAILS_URL);
						String strZipCode = null;
						if (!YFCCommon.isVoid(elePersonInfoBillToOut)) {

							strZipCode = elePersonInfoBillToOut.getAttribute(AcademyConstants.ZIP_CODE);

						}
						if (!YFCCommon.isVoid(strViewOrderDetails)) {

							strURL_ViewOrderDetails = strViewOrderDetails.replace("@@@@", orderNo);

							strURL_ViewOrderDetails = strURL_ViewOrderDetails.replace("$$$$", strZipCode);
						}
						strMessage = strSMSMessage.replace(AcademyConstants.ATT_$ORDER_NO, orderNo);
						strMessage = strMessage.replace("$Order_Details_Link", strURL_ViewOrderDetails);
					} else {

						String strAddress = getStoreAddress(outDocGetShipmentList);
						String strStorePhoneNum = XMLUtil.getAttributeFromXPath(outDocGetShipmentList,
								AcademyConstants.XPATH_SHIPMENT_STORE_MOBILE);
						log.verbose("StoreMobile :: " + strStorePhoneNum);

						strMessage = strSMSMessage.replace(AcademyConstants.ATT_$ORDER_NO, orderNo);
						strMessage = strMessage.replace(AcademyConstants.ATT_$STORE_ADDRESS, strAddress);

						if (strIsCurbSidePickUpEnabled.equals(AcademyConstants.ATTR_Y)) {
							// OMNI-72427 End
							if (!YFCCommon.isVoid(elePersonInfoBillToOut)) {
								strBillingZipcode = elePersonInfoBillToOut.getAttribute(AcademyConstants.ZIP_CODE);
								strBillingZipCodeArg = strBillingZipCodeArg.concat(strBillingZipcode);

							}
							// OMNI-72427 end

							if (!YFCObject.isVoid(strCurbPickUpOrderLink)) {

								strCurbPickUpLink = strCurbPickUpOrderLink.replace(AcademyConstants.ATT_$ORDER_NO,
										orderNo);
								// OMNI-81665 Start
								// removed void condition check for strOrdlvlRemConsol part of OMNI-92779
								if (strCurbPickUpOrderLink.contains("$Shipment_No")) {
									strCurbPickUpLink = strCurbPickUpLink.replace(AcademyConstants.ATT_$SHIPMENT_NO,
											strShipmentNo);
								}
								// OMNI-81665 End
								strCurbPickUpLink = strCurbPickUpLink.replace(AcademyConstants.ATT_$STORE_NO,
										strShipNode);
								// OMNI-72427 start
								strCurbPickUpLink = strCurbPickUpLink.concat(strBillingZipCodeArg);
								// OMNI-72427 End
								log.verbose("Curb Pick Up Link is :: " + strCurbPickUpLink);
								strMessage = strMessage.replace(AcademyConstants.ATT_$CURBPICK_LINK, strCurbPickUpLink);
							}

							if (!YFCObject.isVoid(strStorePhoneNum)) {
								strMessage = strMessage.replace(AcademyConstants.ATT_$STORE_MOBILE, strStorePhoneNum);
							}
						} else {
							if (!YFCObject.isVoid(strStorePhoneNum)) {
								strMessage = strMessage.replace(AcademyConstants.ATT_$STORE_MOBILE, strStorePhoneNum);
							}
						}

					}
					log.verbose("SMS message Content :: " + strMessage);
					docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE, strMessage);
					// End OMNI-5398,5399
					if (!YFCCommon.isVoid(alternatePhoneNo)) {
						docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_DAY_PHONE,
								alternatePhoneNo);
						log.verbose("alternate person SMS message : " + XMLUtil.getXMLString(docSMSMessage));
						AcademyUtil.invokeService(env, AcademyConstants.ACD_BOPIS_POST_MES_Q_SERVICE, docSMSMessage);
					}
					if (!YFCCommon.isVoid(primaryPhoneNo)) {
						docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_DAY_PHONE,
								primaryPhoneNo);
						log.verbose("primary customer SMS message : " + XMLUtil.getXMLString(docSMSMessage));
						AcademyUtil.invokeService(env, AcademyConstants.ACD_BOPIS_POST_MES_Q_SERVICE, docSMSMessage);
					}
				}

			}

		}
		return inDoc;
	}
	// End of New method for Alternate Store SMS
	
	/*
	 * This method is to check the shipment has Cancelled Lines or not
	 * 
	 */
	// Start OMNI-4347,6212 
	private boolean checkforCancelledLines(Element eleInputShip) {
		// TODO Auto-generated method stub
		log.verbose("Start AcademyBOPISCheckAndSendSMSAPI.checkforCancelledLines()");
		boolean bHasCancelledLines = false; 
		NodeList nlShipmentLineList = eleInputShip.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE); 
		int length = nlShipmentLineList.getLength();
		log.verbose("length :: "+length);
		for(int i=0; i< length; i++) {
			Element eleShipmentLine = (Element)nlShipmentLineList.item(i); 
			String strShortageQty = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHORTAGE_QTY);
			if (!YFCObject.isVoid(strShortageQty)) {
				Double dShortageQty = Double.parseDouble(strShortageQty);
				if (dShortageQty > 0.0) {
					bHasCancelledLines = true;
					break;
				}
			}
		}
		log.verbose("bHasCancelledLines :: "+bHasCancelledLines);
		log.verbose("End AcademyBOPISCheckAndSendSMSAPI.checkforCancelledLines()");
		return bHasCancelledLines;
	}
	// End OMNI-4347,6212
	
	// Start OMNI-5398,5399
	private String getStoreAddress(Document outDocGetShipmentList) {
		log.verbose("Start AcademyBOPISCheckAndSendSMSAPI.getStoreAddress()");
		
		String strAddress = "";
		Element eleShip = SCXmlUtil.getChildElement(outDocGetShipmentList.getDocumentElement(),
				AcademyConstants.ELE_SHIPMENT);

		Element eleToAddress = SCXmlUtil.getChildElement(eleShip, AcademyConstants.ELEM_TOADDRESS);
		//Start : OMNI-4868 : Ready for Pick : Extra space 
		//+ eleToAddress.getAttribute(AcademyConstants.ATTR_FNAME) + " "
		//End : OMNI-4868 : Ready for Pick : Extra space 
		strAddress =  eleToAddress.getAttribute(AcademyConstants.ATTR_LNAME) + " "
				+  eleToAddress.getAttribute(AcademyConstants.ATTR_ADDRESS_LINE_1) +", "
				+ eleToAddress.getAttribute(AcademyConstants.ATTR_CITY) + ", "
				+ eleToAddress.getAttribute(AcademyConstants.ATTR_STATE) + " "
				+ eleToAddress.getAttribute(AcademyConstants.ZIP_CODE) ;
		
		log.verbose("End AcademyBOPISCheckAndSendSMSAPI.getStoreAddress()" + strAddress);
		return strAddress;
	}

	private String getSMSMessage(Document outDocGetCommonCodeList, String smsFOR) throws Exception {
		log.verbose("Start AcademyBOPISCheckAndSendSMSAPI.getSMSMessage()  strSMSType :: "+smsFOR);
		
		String strSMSMessage = "";
		int iMsgCount = 1;
		String strMessage = "";
		
		do {
			strMessage = XPathUtil.getString(outDocGetCommonCodeList, "/CommonCodeList/CommonCode[@CodeValue='" 
					+ smsFOR + "_Mes" + iMsgCount + "']/@CodeLongDescription");
			if(YFCObject.isVoid(strMessage)) {
				iMsgCount=0;
			}
			else {
				strSMSMessage = strSMSMessage + strMessage;
				iMsgCount++;
			}
		}
		while (iMsgCount !=0);
		
		log.verbose("End AcademyBOPISCheckAndSendSMSAPI.getSMSMessage() Output message " + strSMSMessage);
		return strSMSMessage;
	}

	private Document getCommonCodeList(YFSEnvironment env, String strCodeType) throws Exception {
		
		log.verbose("Start AcademyBOPISCheckAndSendSMSAPI.getCommonCodeList() strCodeType :: " +strCodeType);
		// TODO Auto-generated method stub
		Document inDocGetCommonCodeList = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, strCodeType);
		log.verbose("input to getCommonCodeList " + XMLUtil.getXMLString(inDocGetCommonCodeList));
		Document outDocGetCommonCodeList = AcademyUtil.invokeAPI(env,
				AcademyConstants.API_GET_COMMON_CODELIST, inDocGetCommonCodeList);
		log.verbose("End AcademyBOPISCheckAndSendSMSAPI.getCommonCodeList() output XML: " + XMLUtil.getXMLString(outDocGetCommonCodeList));
		return outDocGetCommonCodeList;
	}
	// End OMNI-5398,5399
	
	public void setProperties(Properties props) throws Exception {
		this.props = props;

	}

	private Document getShipmentList(YFSEnvironment env, String strShipmentKey) throws Exception {

		// getShipmentList API for PersonInfoShipTo details
		// indoc getShipmentList
		Document inDocGetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		inDocGetShipmentList.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
		log.verbose("Input to getShipmentList" + XMLUtil.getXMLString(inDocGetShipmentList));
		// template for getShipmentList
		Document tempDocGetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENTS);
		Element eleTempShipment = SCXmlUtil.createChild(tempDocGetShipmentList.getDocumentElement(),
				AcademyConstants.ELE_SHIPMENT);
		Element elePersonInfoShip = SCXmlUtil.createChild(eleTempShipment, AcademyConstants.ELEM_TOADDRESS);
		Element eleTempShipmentLines = SCXmlUtil.createChild(eleTempShipment, AcademyConstants.ELE_SHIPMENT_LINES);
		Element eleTempShipmentLine = SCXmlUtil.createChild(eleTempShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE);
		// Code Changes for OMNI-42477 Start
		//OMNI-95706--start
		Element eleTempOrder = SCXmlUtil.createChild(eleTempShipmentLine, AcademyConstants.ELE_ORDER);
		Element eleTempOrderLines = SCXmlUtil.createChild(eleTempOrder, AcademyConstants.ELE_ORDER_LINES);
		Element eleTempOrderLine = SCXmlUtil.createChild(eleTempOrderLines, AcademyConstants.ELE_ORDER_LINE);
		//OMNI-95706--end		
		eleTempOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, "");
		eleTempOrderLine.setAttribute(AcademyConstants.ATTR_MAXLINE_STATUS, "");
		Element eleTempItemDetails = SCXmlUtil.createChild(eleTempOrderLine, AcademyConstants.ELE_ITEM_DETAILS);
		eleTempItemDetails.setAttribute(AcademyConstants.ITEM_ID, "");
		Element eleTempItemDetailsExtn = SCXmlUtil.createChild(eleTempItemDetails,
				AcademyConstants.ELE_ITEM_DETAILS_EXTN);
		eleTempItemDetailsExtn.setAttribute("ExtnDepartmentName", "");
		// Code Changes for OMNI-42477 End
		log.verbose("template for getShipmentList" + XMLUtil.getXMLString(tempDocGetShipmentList));
		
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, tempDocGetShipmentList);
		// invoking getShipmmetList
		Document outDocGetShipmentList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
				inDocGetShipmentList);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);

		log.verbose("Out Doc from getShipmentList" + XMLUtil.getXMLString(outDocGetShipmentList));
		return outDocGetShipmentList;
	}

	private Document getOrderList(YFSEnvironment env, String strOHK) throws Exception {

		// input Doc to getOrderList
		Document inDocGetOrderListInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		inDocGetOrderListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOHK);

		log.verbose("Input to getOrderList " + XMLUtil.getXMLString(inDocGetOrderListInput));

		// Template to getOrderList
		Document tempDocGetOrderList = XMLUtil.createDocument(AcademyConstants.ELE_ORDER_LIST);
		Element eleGetOrderListTemp = SCXmlUtil.createChild(tempDocGetOrderList.getDocumentElement(),
				AcademyConstants.ELE_ORDER);
		Element elePersonInfo = SCXmlUtil.createChild(eleGetOrderListTemp, AcademyConstants.ELE_PERSON_INFO_BILL_TO);
		Element elepersonInfoMarkFor = SCXmlUtil.createChild(eleGetOrderListTemp,
				AcademyConstants.ELE_PERSON_INFO_MARK_FOR);

		log.verbose("template for getOrderList " + XMLUtil.getXMLString(tempDocGetOrderList));

		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, tempDocGetOrderList);
		// invoking getOrderList API
		Document docOutGetOrderList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST,
				inDocGetOrderListInput);
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);

		log.verbose("Output from getOrderList " + XMLUtil.getXMLString(docOutGetOrderList));

		return docOutGetOrderList;

	}
}