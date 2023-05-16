package com.academy.ecommerce.sterling.bopis.shipment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.ecommerce.sterling.bopis.shipment.AcademyRecordStoreActionDataForCurbConsolidation;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

/*##################################################################################
*
* Project Name                : POD September Release - 2022
* Module                      : Fulfillment
* Author                      : Everest
* Date                        : 12-Sep-2022
* Description                 : This will be invoked on the initialization of Customer Pickup screen via Mashup. 
* 								With the incoming shipmentKey, OrderHeaderKey is fetched using getShipmentList and
* 								based on that curbside shipmentLines are fetched via getShipmentLineList API with OrderHeaderKey
* 								to retrieve all related shipments.
* 								OMNI-85083
* ---------------------------------------------------------------------------------
* Date            Author                  Version#       Remarks/Description                     
* ---------------------------------------------------------------------------------
* 12-SEP-2022     Everest                      1.0            Initial version
* ##################################################################################*/
public class AcademyGetCurbConsolidatedShipmentDetails {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyGetCurbConsolidatedShipmentDetails.class);
	AcademyRecordStoreActionDataForCurbConsolidation obj = new AcademyRecordStoreActionDataForCurbConsolidation();

	/**
	 * This is the the main method being called as part of Customer Pick UI Screen on initialization
	 * 
	 * @param env
	 * @param inXML
	 * 
	 * @return
	 * @throws Exception
	 * Sample Input XML: <Shipment ShipmentKey="" OrderNo=""/>
	 */
	public Document getConsoldCurbsideShipments(YFSEnvironment env, Document inXML) throws Exception {	
		log.verbose("AcademyGetCurbConsolidatedShipmentDetails.getConsoldCurbsideShipments input ::" + XMLUtil.getXMLString(inXML));
		String strOrderNo = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO);
		String strShipmentKey = inXML.getDocumentElement().getAttribute(AcademyConstants.SHIPMENT_KEY);
		String strShipNode = inXML.getDocumentElement().getAttribute(AcademyConstants.STR_SHIPNODE);
		String strEnableCurbsideConsolidation = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ENABLE_CURB_CONSOLIDATION);
		//OMNI-101862 START
		String strEnableInstoreConsolidation = inXML.getDocumentElement()
				.getAttribute(AcademyConstants.ATTR_ENABLE_INSTORE_CONSOLIDATION);
		String strPackListType = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_PACKLIST_TYPE);
		String strShipmentType = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
		Document docFlagResult = null;
		// OMNI-101862 END
		String strExtnIsCurbsidePickupOpted = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_EXTN_CURSIDE_PICK_OPTED);
		String strStatus = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_STATUS);
		String strIsMobile = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_IS_MOBILE);
		Document docGetShipmentListOut = null;
		Document docGetShipmentLineListOut = null;
		String strCurbShpNo = null;
		String strCurbShpKey = null;
		String strTotalShipmentLines = null;
		String strCurbsideShipment = "";
		String strCurbsideShipmentKey = "";
		String strCurbsideShipmentNo = "";
		
		String strConsoldShpNo = null;
		String strConsoldShpKey = null;
		String strConsoldShipment = "";
		String strConsoldShipmentKey = "";
		String strConsoldShipmentNo = "";
		
		String strLoginID = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_LOGIN_ID);
		String strCurbConsolForMobile = "N";//OMNI-101862
		String strInstoreConsolForMobile = "N";//OMNI-101862
		
		String strExtnIsinstorePickupOpted = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_EXTN_IS_INSTORE_PICKUP_OPTED);//OMNI-105578
		String strInStorePickupFlagEnabled = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_INSTORE_PICKUP_FLAG_ENABLED);//OMNI-105578
		log.verbose("Instore pickup opted : "+strExtnIsinstorePickupOpted +" Instore pickup flag Enabled : "+strInStorePickupFlagEnabled);//OMNI-105778
		boolean isOrderNoVoid = false;
		isOrderNoVoid = validateOrderNo(strOrderNo,strShipmentKey);
		
		if (isOrderNoVoid && AcademyConstants.STR_YES.equals(strIsMobile)) {
			strCurbConsolForMobile = "Y";
			log.verbose("The input doesnt contain order# or status");
			docGetShipmentListOut = invokeGetShipmentList(env, strShipmentKey);
			Element eleShipmentLst = docGetShipmentListOut.getDocumentElement();
			Element eleShipment = XMLUtil.getFirstElementByName(eleShipmentLst, AcademyConstants.ELE_SHIPMENT);
			Element eleExtn = (Element) eleShipment.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
			strExtnIsCurbsidePickupOpted = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_CURSIDE_PICK_OPTED);
			strOrderNo = eleShipment.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			strStatus = eleShipment.getAttribute(AcademyConstants.ATTR_STATUS);
		} else {
			docFlagResult = getFlagDetails(strIsMobile, strEnableCurbsideConsolidation,strEnableInstoreConsolidation, 
			strExtnIsCurbsidePickupOpted, strStatus, strOrderNo, strShipmentKey, strPackListType, strShipmentType);
			strCurbConsolForMobile = docFlagResult.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURBSIDE_ENABLED);
			strInstoreConsolForMobile = docFlagResult.getDocumentElement().getAttribute(AcademyConstants.ATTR_INSTORE_ENABLED);
		}
		//Updated conditions for OMNI-87912/ OMNI-88833 - Consolidated view for RFCP and PWI status
		
		log.verbose("The Order# :" +strOrderNo+ " and ShipmentKey : " +strShipmentKey+ " and status of shipment : " +strStatus+ " IsInvokedFromMobile :" 
				+strIsMobile+ " Is current shipment curbside opted: " +strExtnIsCurbsidePickupOpted);
		if ("Y".equals(strCurbConsolForMobile)) {
			log.verbose("bCurbConsolForMobile is true");
			docGetShipmentLineListOut = invokeGetShipmentLineList(env, strOrderNo,strInstoreConsolForMobile,strShipNode);
			docGetShipmentLineListOut = assignTeamMember(env, docGetShipmentLineListOut, strLoginID,
					strExtnIsCurbsidePickupOpted, strExtnIsinstorePickupOpted, strInStorePickupFlagEnabled);
			Element eleShipLines = docGetShipmentLineListOut.getDocumentElement();
			strTotalShipmentLines = eleShipLines.getAttribute(AcademyConstants.ATTRIBUTE_TOTAL_NO_RECORDS);

			NodeList nlShpLines = eleShipLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			for (int iShpLines = 0; iShpLines < nlShpLines.getLength(); iShpLines++) {
				Element eleShipmentLine = (Element) nlShpLines.item(iShpLines);
				Element eleShpmt = (Element) eleShipmentLine.getElementsByTagName(AcademyConstants.ELE_SHIPMENT)
						.item(0);
				strCurbShpNo = eleShpmt.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
				strCurbShpKey = eleShipmentLine.getAttribute(AcademyConstants.SHIPMENT_KEY);
				if (!strCurbsideShipment.contains(strCurbShpKey)) {
					strCurbsideShipment = strCurbsideShipment + strCurbShpNo + AcademyConstants.STR_HYPHEN
							+ strCurbShpKey + AcademyConstants.STR_COMMA;
					strCurbsideShipmentKey = strCurbsideShipmentKey + strCurbShpKey + AcademyConstants.STR_COMMA;
					strCurbsideShipmentNo = strCurbsideShipmentNo + strCurbShpNo + AcademyConstants.STR_COMMA;
				}
			}
			// To remove extra comma from the string
			strCurbsideShipment = strCurbsideShipment.substring(0, strCurbsideShipment.length() - 1);
			log.verbose("strCurbsideShipment::" + strCurbsideShipment);
			strCurbsideShipmentKey = strCurbsideShipmentKey.substring(0, strCurbsideShipmentKey.length() - 1);
			strCurbsideShipmentNo = strCurbsideShipmentNo.substring(0, strCurbsideShipmentNo.length() - 1);

			eleShipLines.setAttribute(AcademyConstants.ATTR_CURBSIDE_SHIPMENT, strCurbsideShipment);
			eleShipLines.setAttribute(AcademyConstants.ATTR_CURBSIDE_SHIPMENT_KEY, strCurbsideShipmentKey);
			eleShipLines.setAttribute(AcademyConstants.ATTR_CURBSIDE_SHIPMENT_NO, strCurbsideShipmentNo);
			eleShipLines.setAttribute(AcademyConstants.ATTR_TOTAL_SHIPMENT_LINES, strTotalShipmentLines);
			eleShipLines.setAttribute(AcademyConstants.ATTR_CURB_SIDE_CONSOLIDATION_FLAG, AcademyConstants.STR_YES);
		
		} //OMNI-101862 - Start
		else if ("Y".equals(strInstoreConsolForMobile)) {
			docGetShipmentLineListOut = invokeGetShipmentLineList(env, strOrderNo, strInstoreConsolForMobile,strShipNode);
			//OMNI-106483 - Start
			if ("Y".equals(strExtnIsCurbsidePickupOpted) ||	"Y".equals(strInStorePickupFlagEnabled)) {//OMNI-105578 - added strExtnIsinstorePickupOpted
				docGetShipmentLineListOut = assignTeamMember(env, docGetShipmentLineListOut, strLoginID,//OMNI-105778 - additional parameters used as input
						strExtnIsCurbsidePickupOpted, strExtnIsinstorePickupOpted, strInStorePickupFlagEnabled);
			}
			//OMNI-106483 - End
			Element eleShipLines = docGetShipmentLineListOut.getDocumentElement();
			strTotalShipmentLines = eleShipLines.getAttribute(AcademyConstants.ATTRIBUTE_TOTAL_NO_RECORDS);

			NodeList nlShpLines = eleShipLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			for (int iShpLines = 0; iShpLines < nlShpLines.getLength(); iShpLines++) {
				Element eleShipmentLine = (Element) nlShpLines.item(iShpLines);
				Element eleShpmt = (Element) eleShipmentLine.getElementsByTagName(AcademyConstants.ELE_SHIPMENT)
						.item(0);
				strConsoldShpNo = eleShpmt.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
				strConsoldShpKey = eleShipmentLine.getAttribute(AcademyConstants.SHIPMENT_KEY);
				if (!strConsoldShipment.contains(strConsoldShpKey)) {
					strConsoldShipment = strConsoldShipment + strConsoldShpNo + AcademyConstants.STR_HYPHEN
							+ strConsoldShpKey + AcademyConstants.STR_COMMA;
					strConsoldShipmentKey = strConsoldShipmentKey + strConsoldShpKey + AcademyConstants.STR_COMMA;
					strConsoldShipmentNo = strConsoldShipmentNo + strConsoldShpNo + AcademyConstants.STR_COMMA;
				}
			}
			// To remove extra comma from the string
			strConsoldShipment = strConsoldShipment.substring(0, strConsoldShipment.length() - 1);
			log.verbose("strConsoldShipment::" + strConsoldShipment);
			strConsoldShipmentKey = strConsoldShipmentKey.substring(0, strConsoldShipmentKey.length() - 1);
			strConsoldShipmentNo = strConsoldShipmentNo.substring(0, strConsoldShipmentNo.length() - 1);

			eleShipLines.setAttribute(AcademyConstants.ATTR_CONSOLD_SHIPMENT, strConsoldShipment);
			eleShipLines.setAttribute(AcademyConstants.ATTR_CONSOLD_SHIPMENT_KEY, strConsoldShipmentKey);
			eleShipLines.setAttribute(AcademyConstants.ATTR_CONSOLD_SHIPMENT_NO, strConsoldShipmentNo);
			eleShipLines.setAttribute(AcademyConstants.ATTR_TOTAL_SHIPMENT_LINES, strTotalShipmentLines);
			eleShipLines.setAttribute(AcademyConstants.ATTR_INSTORE_CONSOLIDATION_FLAG, AcademyConstants.STR_YES);
		}//OMNI-101862 - End
		else {
			log.verbose("Curbside consolidation is not enabled OR status is not in RFCP/ PWI OR current shipment is not curbside opted");
			docGetShipmentLineListOut = callgetShipmentLineList(env, strShipmentKey, strStatus);
			Element eleShipLines = docGetShipmentLineListOut.getDocumentElement();
			strTotalShipmentLines = eleShipLines.getAttribute(AcademyConstants.ATTRIBUTE_TOTAL_NO_RECORDS);
			eleShipLines.setAttribute(AcademyConstants.ATTR_CURB_SIDE_CONSOLIDATION_FLAG, AcademyConstants.STR_NO);
			eleShipLines.setAttribute(AcademyConstants.ATTR_TOTAL_SHIPMENT_LINES, strTotalShipmentLines);
			
			if ("Y".equals(strInStorePickupFlagEnabled)) {// OMNI-105578
				Document changeShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
				String strAttendedBy = obj.getUserID(env, strLoginID);
				changeShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
				Element eleShipment = changeShipment.getDocumentElement();
				eleShipment.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
				
				if ("Y".equals(strExtnIsinstorePickupOpted)) {
					String strCurrAttendedBy = "";
					NodeList nlShpLines = eleShipLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
					for (int iShpLines = 0; iShpLines < nlShpLines.getLength(); iShpLines++) {
						Element eleShipmentLine = (Element) nlShpLines.item(iShpLines);
						Element eleShpmt = (Element) eleShipmentLine.getElementsByTagName(AcademyConstants.ELE_SHIPMENT)
								.item(0);
						Element eleExtn = XMLUtil.getFirstElementByName(eleShpmt, AcademyConstants.ELE_EXTN);
						strCurrAttendedBy = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_INSTORE_ATTENDED_BY);
						log.verbose("The Current Attendee is : " + strCurrAttendedBy);
					}
					
					Element shipment = XmlUtils.createChild(eleShipLines, AcademyConstants.ELE_SHIPMENT);
					shipment.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
					Element extn = XmlUtils.createChild(shipment, AcademyConstants.ELE_EXTN);

					if (YFCCommon.isVoid(strCurrAttendedBy) || !strCurrAttendedBy.equals(strAttendedBy)) {
						Element eleExtn = XmlUtils.createChild(eleShipment, AcademyConstants.ELE_EXTN);
						eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_INSTORE_ATTENDED_BY, strAttendedBy);
						log.verbose("The Input to changeShipment API :" + XMLUtil.getXMLString(changeShipment));
						AcademyUtil.invokeService(env,
								AcademyConstants.SERVICE_ACADEMY_CURBSIDE_CHANGE_SHIPMENT_ASSIGNEE, changeShipment);
						extn.setAttribute(AcademyConstants.ATTR_EXTN_INSTORE_ATTENDED_BY, strAttendedBy);
					} else {
						log.verbose("Ignoring attendee update since the Current Attendee is : " + strCurrAttendedBy
								+ " and LoggedIn user is : " + strAttendedBy);
						extn.setAttribute(AcademyConstants.ATTR_EXTN_INSTORE_ATTENDED_BY, strCurrAttendedBy);
					}
					
				} else {
					Element eleExtn = XmlUtils.createChild(eleShipment, AcademyConstants.ELE_EXTN);
					eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_INSTORE_ATTENDED_BY, strAttendedBy);
					eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_IS_INSTORE_PICKUP_OPTED, AcademyConstants.STR_YES);
					String strDateFormat = AcademyConstants.STR_DATE_TIME_PATTERN_NEW;
					SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
					Calendar cal = Calendar.getInstance();
					String strCurrentDate = sDateFormat.format(cal.getTime());
					log.verbose("Appointment date time : " + strCurrentDate);
					eleShipment.setAttribute(AcademyConstants.ATTR_APPOINTMENT_NO, strCurrentDate);
					
					log.verbose("The Input to changeShipment API :" + XMLUtil.getXMLString(changeShipment));
					AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_CURBSIDE_CHANGE_SHIPMENT_ASSIGNEE,
							changeShipment);

					Element shipment = XmlUtils.createChild(eleShipLines, AcademyConstants.ELE_SHIPMENT);
					shipment.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
					shipment.setAttribute(AcademyConstants.ATTR_APPOINTMENT_NO, strCurrentDate);
					Element extn = XmlUtils.createChild(shipment, AcademyConstants.ELE_EXTN);
					extn.setAttribute(AcademyConstants.ATTR_EXTN_INSTORE_ATTENDED_BY, strAttendedBy);
					extn.setAttribute(AcademyConstants.ATTR_EXTN_IS_INSTORE_PICKUP_OPTED, AcademyConstants.STR_YES);
				}
			}
		}		
		log.verbose("The output:" +XMLUtil.getXMLString(docGetShipmentLineListOut));
		return docGetShipmentLineListOut;	
	}
	
	private boolean validateOrderNo(String strOrderNo, String strShipmentKey) {
		
		if (YFCCommon.isVoid(strOrderNo) && !YFCCommon.isVoid(strShipmentKey)) 
			return true;
		return false;
	}

	
	private Document getFlagDetails(String strIsMobile, String strEnableCurbsideConsolidation,
			String strEnableInstoreConsolidation, String strExtnIsCurbsidePickupOpted, String strStatus,
			String strOrderNo, String strShipmentKey, String strPackListType, String strShipmentType)
			throws ParserConfigurationException {
		Document docFlagResult = XMLUtil.createDocument(AcademyConstants.ATTR_FLAG_DETAILS);
		Element eleFlagResult = docFlagResult.getDocumentElement();
		if (AcademyConstants.STR_YES.equalsIgnoreCase(strIsMobile)
				&& (AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS.equalsIgnoreCase(strStatus)
						|| AcademyConstants.STR_PAPER_WORK_INITIATED_STATUS.equalsIgnoreCase(strStatus))
				&& (!YFCCommon.isVoid(strOrderNo) && !YFCCommon.isVoid(strShipmentKey))) {
			if ((!YFCCommon.isVoid(strPackListType) && "FA".equals(strPackListType))
					|| (!YFCCommon.isVoid(strShipmentType) && "SOF".equals(strShipmentType))) {
				eleFlagResult.setAttribute(AcademyConstants.ATTR_NO_FLAG_ENABLED, "Y");
			} else if (AcademyConstants.STR_YES.equalsIgnoreCase(strEnableInstoreConsolidation)) {
				eleFlagResult.setAttribute(AcademyConstants.ATTR_INSTORE_ENABLED, "Y");
			} else if (AcademyConstants.STR_YES.equalsIgnoreCase(strEnableCurbsideConsolidation)
					&& AcademyConstants.STR_YES.equalsIgnoreCase(strExtnIsCurbsidePickupOpted)) {
				eleFlagResult.setAttribute(AcademyConstants.ATTR_CURBSIDE_ENABLED, "Y");
			} else {
				eleFlagResult.setAttribute(AcademyConstants.ATTR_NO_FLAG_ENABLED, "Y");
			}
		}
		log.verbose("The output of flag" + XMLUtil.getXMLString(docFlagResult));
		return docFlagResult;
	}

	//OMNI-105587 - Added parameters strExtnIsCurbsidePickupOpted, strExtnIsinstorePickupOpted for Instore Pickup Assignee for instoreConsolidation enabled
	private Document assignTeamMember(YFSEnvironment env, Document docGetShipmentListOut, String strLoginID,
			String strExtnIsCurbsidePickupOpted, String strExtnIsinstorePickupOpted,String strInStorePickupFlagEnabled) {
		log.beginTimer("assignTeamMember to update ExtnCurbsideAttendedBy/ExtnInstoreAttendedBy");//OMNI-105578 - ExtnInstoreAttendedBy
		String strShipmentKey = "";
		String strStatus = "";
		String strAttendedBy = "";
		String previousLoginID = "";
		ArrayList <String> alShipmentKey = new ArrayList <>();
		//OMN-105857
		String strInstoreUpdateNeeded="";

		Document multiApiOutput = null;
		try {
			NodeList nlShipmentList = docGetShipmentListOut.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
			for (int i = 0; i < nlShipmentList.getLength(); i++) {
				Element eleCurrentShipment = (Element) nlShipmentList.item(i);
				strShipmentKey = eleCurrentShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
				strStatus = eleCurrentShipment.getAttribute(AcademyConstants.ATTR_STATUS);
				//For Instore Pickup Opted - OMNI-105578
				if("Y".equals(strExtnIsCurbsidePickupOpted)){
					strAttendedBy = XPathUtil.getString(eleCurrentShipment, AcademyConstants.XPATH_CURBSIDE_ATTENDED_BY);
					log.verbose("ExtnCurbsideAttendedBy ::" + strAttendedBy);
					if (!YFCCommon.isVoid(strAttendedBy)) {
						previousLoginID = strAttendedBy.substring(strAttendedBy.indexOf("(") + 1, strAttendedBy.indexOf(")"));
						log.verbose("Shipment is already assigned and Previous LoginID :: " + previousLoginID);
					}
					
					if ((AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS.equalsIgnoreCase(strStatus) ||
							AcademyConstants.STR_PAPER_WORK_INITIATED_STATUS.equalsIgnoreCase(strStatus))
							&& !YFCCommon.isVoid(strShipmentKey)
							&& (YFCCommon.isVoid(strAttendedBy) || !previousLoginID.equalsIgnoreCase(strLoginID))) {
						alShipmentKey.add(strShipmentKey);
						log.verbose("ShipmentKey ::" + strShipmentKey);
					}
				} //OMNI-105578 - In store Pickup Assignee - Auto Populating Assignee
				else if("Y".equals(strExtnIsinstorePickupOpted) && "Y".equals(strInStorePickupFlagEnabled)) {
					strAttendedBy = XPathUtil.getString(eleCurrentShipment, AcademyConstants.ATTR_EXTN_INSTORE_ATTENDED_BY);
					if (!YFCCommon.isVoid(strAttendedBy)) {
					previousLoginID = strAttendedBy.substring(strAttendedBy.indexOf("(") + 1, strAttendedBy.indexOf(")"));
					log.verbose("ExtnInstoreAttendedBy :: " + strAttendedBy + " and updating Attendee for LoginID: " + strLoginID);
					}
					if ((AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS.equalsIgnoreCase(strStatus) ||
							AcademyConstants.STR_PAPER_WORK_INITIATED_STATUS.equalsIgnoreCase(strStatus))
							&& !YFCCommon.isVoid(strShipmentKey) && !(previousLoginID.equalsIgnoreCase(strLoginID))) {
						alShipmentKey.add(strShipmentKey);
						log.verbose("ShipmentKey ::" + strShipmentKey);
					}
				log.verbose("LoginID ::" + strLoginID);
				}
				//OMNI-105857 begin
				else 
				{
					if(AcademyConstants.STR_YES.equalsIgnoreCase(strInStorePickupFlagEnabled)&&!AcademyConstants.STR_YES.equalsIgnoreCase(strExtnIsCurbsidePickupOpted))
					{
						log.verbose("Instore flag enabled for non curbside and non instore order");
						if ((AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS.equalsIgnoreCase(strStatus) ||
								AcademyConstants.STR_PAPER_WORK_INITIATED_STATUS.equalsIgnoreCase(strStatus))
								&& !YFCCommon.isVoid(strShipmentKey)) {
							alShipmentKey.add(strShipmentKey);
							log.verbose("ShipmentKey ::" + strShipmentKey);
						}
						log.verbose("LoginID ::" + strLoginID);
						strInstoreUpdateNeeded="Y";
					}
				}
			//OMNI-105857 End
			}
			if (alShipmentKey.size() > 0) {
				log.verbose("List of ShipmentKeys to update ExtnCurbsideAttendedBy:: " + alShipmentKey);
				strAttendedBy = obj.getUserID(env, strLoginID);
				//OMNI-105857 Begin
				if(AcademyConstants.STR_YES.equalsIgnoreCase(strInstoreUpdateNeeded)) {
				  multiApiOutput=obj.changeShipmentForAssigneeAndInstoreOpted(env, alShipmentKey, strAttendedBy);
				  strExtnIsinstorePickupOpted = AcademyConstants.STR_YES;//OMNI-108608 - Fix added
				}
				else {
				 multiApiOutput = obj.changeShipmentForAssignee(env, alShipmentKey, strAttendedBy,
						strExtnIsCurbsidePickupOpted, strExtnIsinstorePickupOpted);// OMNI-105778 additional parameters used
				}
				
				docGetShipmentListOut = updateAttendee(multiApiOutput, docGetShipmentListOut,
						strExtnIsinstorePickupOpted);// OMNI-105778 additional parameters used
			}
		} catch (Exception a) {
			a.printStackTrace();
			log.info(" Exception in assignTeamMember :: " + a.toString());
		}
		log.endTimer("assignTeamMember to update ExtnCurbsideAttendedBy");
		return docGetShipmentListOut;
	}

	//OMNI-105778 - Added parameter strExtnIsinstorePickupOpted
	private Document updateAttendee(Document multiApiOutput, Document docGetShipmentListOut, String strExtnIsinstorePickupOpted) {
		String shipmentKey = "";
		String assignee = "";
		Element eleExtn = null;
		String strAppointmentNo = "";//OMNI-105857
		try {
			NodeList nlShipLines = docGetShipmentListOut.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			for (int i = 0; i < nlShipLines.getLength(); i++) {
				Element eleShipmentLine = (Element) nlShipLines.item(i);
				Element eleShipment = XMLUtil.getFirstElementByName(eleShipmentLine, AcademyConstants.ELE_SHIPMENT);
				shipmentKey = eleShipment.getAttribute(AcademyConstants.SHIPMENT_KEY);
				log.verbose("shipmentKey ::" + shipmentKey);
				if("Y".equals(strExtnIsinstorePickupOpted)){//OMNI-105778 - ExtnInstoreAttendedBy
					assignee = XPathUtil.getString(multiApiOutput.getDocumentElement(),
							"/MultiApi/API/Output/Shipment[@ShipmentKey='" + shipmentKey
									+ "']/Extn/@ExtnInstoreAttendedBy");
					strAppointmentNo = XPathUtil.getString(multiApiOutput.getDocumentElement(),
							"/MultiApi/API/Output/Shipment[@ShipmentKey='"  + shipmentKey
									+ "']/@AppointmentNo");//OMNI-108608 - Fix added
				} else {
					assignee = XPathUtil.getString(multiApiOutput.getDocumentElement(),
							"/MultiApi/API/Output/Shipment[@ShipmentKey='" + shipmentKey
									+ "']/Extn/@ExtnCurbsideAttendedBy");
				}
				log.verbose("strAssignee ::" + assignee);
				eleExtn = XMLUtil.getFirstElementByName(eleShipment, AcademyConstants.ELE_EXTN);
				if (!YFCCommon.isVoid(assignee) && ("Y".equals(strExtnIsinstorePickupOpted))) {//OMNI-105778 - Start
					eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_INSTORE_ATTENDED_BY, assignee);
					eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_IS_INSTORE_PICKUP_OPTED, strExtnIsinstorePickupOpted);
					eleShipment.setAttribute(AcademyConstants.ATTR_APPOINTMENT_NO, strAppointmentNo);//OMNI-108608 - Fix added
				} else if (!YFCCommon.isVoid(assignee)){//OMNI-105778 - End
					eleExtn.setAttribute(AcademyConstants.EXTN_CURBSIDE_ATTENDED_BY, assignee);
				}
			}
			log.verbose("After updating ExtnCurbsideAttendedBy :: \n" + XMLUtil.getXMLString(docGetShipmentListOut));
		} catch (Exception u) {
			u.printStackTrace();
			log.info(" Exception in assignTeamMember :: " + u.toString());
		}

		return docGetShipmentListOut;
	}

	

	/**
	 * This method is be used to frame input and invoke getShipmentLineList API for getting order related shipments
	 * @param strShipNode 
	 * @param bInstoreConsolForMobile 
	 * 
	 * @param env,
	 * @param OrderNo
	 * Sample Input for getShipmentLineList:
	 * <ShipmentLine ShipmentKey="" IsPickable="Y" OrderNo="" Quantity="0.00" QuantityQryType="NE">
		    <Shipment DeliveryMethod="PICK" ShipNode="">
		        <ComplexQuery>
		            <Or>
		                <Exp Name="Status" QryType="EQ" Value="1100.70.06.30.5"/>
		                <Exp Name="Status" QryType="EQ" Value="1100.70.06.30.7"/>
		            </Or>
		        </ComplexQuery>
		        <Extn ExtnIsCurbsidePickupOpted="Y"/>
		    </Shipment>
		</ShipmentLine>
	 */
	private Document invokeGetShipmentLineList(YFSEnvironment env, String strOrderNo, String strInstoreConsolForMobile, String strShipNode) throws Exception {
		log.beginTimer("Begining of AcademyGetCurbConsolidatedShipmentDetails -> invokeGetShipmentLineList API");
		Document docIngetShipmentLineList = null;
		Document docOutgetShipmentLineList = null;
		docIngetShipmentLineList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT_LINE);
		Element eleShipmentLine = docIngetShipmentLineList.getDocumentElement();
		eleShipmentLine.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		eleShipmentLine.setAttribute(AcademyConstants.ATTR_IS_PICKABLE_FLAG,AcademyConstants.STR_YES);
		eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, AcademyConstants.STR_ZERO_WITH_DECIMAL);
		eleShipmentLine.setAttribute(AcademyConstants.ATTR_QTY_QRY_TYPE, AcademyConstants.STR_NE);
		
		Element eleShipment = XmlUtils.createChild(eleShipmentLine, AcademyConstants.ELE_SHIPMENT);
		eleShipment.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, AcademyConstants.STR_PICK);
		eleShipment.setAttribute(AcademyConstants.STR_SHIPNODE, strShipNode);
		
		//OMNI-87912 - Consolidated view for RFCP and PWI status - Start
		Element eleComplexQuery = docIngetShipmentLineList.createElement(AcademyConstants.COMPLEX_QRY_ELEMENT);
		Element eleOr = docIngetShipmentLineList.createElement(AcademyConstants.COMPLEX_OR_ELEMENT);
		Element eleExp = docIngetShipmentLineList.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.STATUS);
		eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
		eleExp.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS);
		eleOr.appendChild(eleExp);
		
		eleExp = docIngetShipmentLineList.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.STATUS);
		eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
		eleExp.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STR_PAPER_WORK_INITIATED_STATUS);
		eleOr.appendChild(eleExp);
		eleComplexQuery.appendChild(eleOr);
		eleShipment.appendChild(eleComplexQuery);
		//OMNI-87912 - Consolidated view for RFCP and PWI status - End
		
		Element eleExtn = XmlUtils.createChild(eleShipment, AcademyConstants.ELE_EXTN);
		eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_CURSIDE_PICK_OPTED, AcademyConstants.STR_YES);
	
		
		if ("Y".equals(strInstoreConsolForMobile)) {
			eleShipment.setAttribute(AcademyConstants.ATTR_PACKLIST_TYPE, AcademyConstants.STS_FA);
			eleShipment.setAttribute(AcademyConstants.ATTR_PACKLIST_TYPE_QRY_TYPE, AcademyConstants.STR_NE);
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.STR_SPECIAL_ORDER_FIREARMS);
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE_QRY_TYPE, AcademyConstants.STR_NE);
			eleExtn.removeAttribute(AcademyConstants.ATTR_EXTN_CURSIDE_PICK_OPTED);
		}

		log.verbose("AcademyCurbsideGetShipmentLineList Service Input XML : \n" + XMLUtil.getXMLString(docIngetShipmentLineList));
		docOutgetShipmentLineList = AcademyUtil.invokeService(env, AcademyConstants.ATTR_SERV_ACADEMY_CURB_SHPLINE_LIST,
				docIngetShipmentLineList);
		log.verbose("AcademyCurbsideGetShipmentLineList Service Output XML : \n" + XMLUtil.getXMLString(docOutgetShipmentLineList));
		log.endTimer("End of AcademyGetCurbConsolidatedShipmentDetails -> invokeGetShipmentLineList API");

		return docOutgetShipmentLineList;
	}

	/**
	 * This method is be used to frame input and invoke getShipmentList API for getting order related shipments
	 * 
	 * @param env,
	 * @param strOrderNo
	 * Sample Input for getShipmentList:
	 * <Shipment ShipmentKey="" />
	 */
	private Document invokeGetShipmentList(YFSEnvironment env, String strShipmentKey) throws Exception {
		
		log.beginTimer("Begining of AcademyGetCurbConsolidatedShipmentDetails -> invokeGetShipmentList API");
		Document docIngetShipmentList = null;
		Document docOutgetShipmentList = null;

		// getShipmentList API inDoc
		docIngetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		docIngetShipmentList.getDocumentElement().setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);

		// getShipmentList API Template
		Document docgetShipmentListTemplate = XMLUtil.getDocument(AcademyConstants.TEMP_GET_SHIPMENT_LIST);
		log.verbose("getShipmentList API indoc XML : \n" + XMLUtil.getXMLString(docIngetShipmentList));
		// Invoking the getShipmentList API
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docgetShipmentListTemplate);
		docOutgetShipmentList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,docIngetShipmentList);
		log.verbose("getShipmentList API Output XML : \n" + XMLUtil.getXMLString(docgetShipmentListTemplate));
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		log.endTimer("End of AcademyGetCurbConsolidatedShipmentDetails -> invokeGetShipmentList API");

		return docOutgetShipmentList;
	}
	
	/**
	 * This method prepares the input and calls getShipmentLineList API
	 * 
	 * @param strShipmentKey
	 * @return
	 * @throws Exception
	 */
	private Document callgetShipmentLineList(YFSEnvironment env, String strShipmentKey, String strStatus) throws Exception {
		log.beginTimer("AcademyGetCurbConsolidatedShipmentDetails.callgetShipmentLineList Method");
		Document outDocgetShipmentLineList = null;
		Document inDocgetShipmentLineList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT_LINE);

		Element elegetShipmentLineList = inDocgetShipmentLineList.getDocumentElement();
		elegetShipmentLineList.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
		log.verbose("Calling callgetShipmentLineList API with the input" + XMLUtil.getXMLString(inDocgetShipmentLineList));

		if (!AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS.equalsIgnoreCase(strStatus)) {
			log.verbose("Invoking AcademyCurbsideGetShipmentLineList as the status is :: " + strStatus);
			outDocgetShipmentLineList = AcademyUtil.invokeService(env, AcademyConstants.ATTR_SERV_ACADEMY_CURB_SHPLINE_LIST,
					inDocgetShipmentLineList);
		} else {
			Document outputTemplate = YFCDocument.getDocumentFor(AcademyConstants.TEMP_GET_SHIPMENTLIST).getDocument();
			env.setApiTemplate(AcademyConstants.SER_GET_SHIPMENT_LINE_LIST, outputTemplate);
			outDocgetShipmentLineList = AcademyUtil.invokeAPI(env, AcademyConstants.SER_GET_SHIPMENT_LINE_LIST,
					inDocgetShipmentLineList);
			env.clearApiTemplate(AcademyConstants.SER_GET_SHIPMENT_LINE_LIST);
		}		
		log.verbose("Output of the  callgetShipmentLineList API " + XMLUtil.getXMLString(outDocgetShipmentLineList));
		log.endTimer("AcademyGetCurbConsolidatedShipmentDetails.callgetShipmentLineList Method");
		return outDocgetShipmentLineList;
	}
}