package com.academy.ecommerce.sterling.bopis.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyInStoreAutoPickup extends YCPBaseAgent {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyInStoreAutoPickup.class);
	public static final String STR_DATE_TIME_PATTERN_NEW = "yyyyMMddHHmmss";
	SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_NEW);

	/**
	 * This method is invoked to read all the eligible shipments for Auto confirm shipment
	 * 
	 * @param env
	 * @param inDoc
	 **Sample input
	 */
	public List<Document> getJobs(YFSEnvironment env, Document inXML) 
			throws Exception {
		log.beginTimer("AcademyInStoreAutoPickup::getJobs");
		log.verbose("Inside AcademyInStoreAutoPickup getJobs.The Input xml is : " + XMLUtil.getXMLString(inXML));

		List<Document> outputList = new ArrayList<Document>();

		String strNumOfRecords = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_NUM_RECORDS);
		String strCutOffTime = inXML.getDocumentElement().getAttribute("CutOffTime");
		String strTriggerStartTime = inXML.getDocumentElement().getAttribute("TriggerStartTime");
		String strTriggerEndTime = inXML.getDocumentElement().getAttribute("TriggerEndTime");

		if(!checkIfAutoCloseShipmentsToBeProcessed(env, strTriggerStartTime, strTriggerEndTime)) {
			return outputList;
		}

		if(YFCObject.isVoid(strNumOfRecords)){
			strNumOfRecords = "500";
		}

		NodeList nlLastMessage = inXML.getElementsByTagName(AcademyConstants.STR_LAST_MESSAGE);
		String strShipmentKey = "";
		if (nlLastMessage.getLength() == 1) {
			strShipmentKey = ((Element) (((Element) (inXML.getElementsByTagName(AcademyConstants.STR_LAST_MESSAGE)
					.item(0))).getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0)))
					.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		}
		log.verbose("*** strShipmentKey :: ***" +strShipmentKey);

		Document docGetShipmentListInp = prepareInputForGetShipmentList(strShipmentKey, strCutOffTime);
		docGetShipmentListInp.getDocumentElement().setAttribute(AcademyConstants.ATTR_NUM_RECORDS, strNumOfRecords);
		
		Document docShipmentListTemplate = XMLUtil.getDocument("<Shipments><Shipment ShipmentNo='' ShipmentKey='' "
				+ "Status='' OrderNo='' ShipNode='' ><Extn ExtnInstoreAttendedBy='' /></Shipment></Shipments>");
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docShipmentListTemplate);
		log.verbose("getShipmentList input XML " + XMLUtil.getXMLString(docGetShipmentListInp));
		
		Document docGetShipmentListOut = AcademyUtil.invokeAPI(env,
				AcademyConstants.API_GET_SHIPMENT_LIST, docGetShipmentListInp);
		log.verbose(" getShipmentList Output " + XMLUtil.getXMLString(docGetShipmentListOut));
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);

		log.verbose("*** docGetShipmentListOut length ***" + docGetShipmentListOut
				.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).getLength());


		if(!YFCObject.isNull(docGetShipmentListOut)){
			NodeList nlShipments = docGetShipmentListOut.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);

			if(nlShipments.getLength() > 0){
				for(int i=0;i<nlShipments.getLength();i++){
					Element eleShipment = (Element)nlShipments.item(i);
					outputList.add(XMLUtil.getDocumentForElement(eleShipment));
				}
			}
		}
		return outputList;
	}

	
	
	/**
	 * This method process the individual Shipments which are meant to be closed.
	 * 
	 * @param env
	 * @param docInput
	 **Sample input
	 */
	public void executeJob(YFSEnvironment env, Document docInput) throws Exception {
		log.beginTimer("AcademyInStoreAutoPickup::executeJob");

		Document docShipmentConfirm = prepareInputForShipmentClosure(docInput.getDocumentElement());
		
		AcademyUtil.invokeService(env, "AcademyRecordCustPickForCurbsideConsolidation", docShipmentConfirm);
		
		
		log.endTimer("AcademyInStoreAutoPickup::executeJob");
	}



	/**
	 * This method is prepares the input to the getShipmentList to
	 * fetch eligible records i.e ExtnInStorePickupOpted=Y and Extn
	 * 
	 * @param strShipmentKey
	 * @return docGetShipmentListInp
	 * @throws ParserConfigurationException 
	 * @throws Exception
	 * Sample Input XML for getShipmentList: 
	 * 
	 */

	private Document prepareInputForGetShipmentList(String strShipmentKey, String strCutOffTime) throws Exception   {
		log.beginTimer("AcademyInStoreAutoPickup.prepareInputForGetShipmentList()");
		log.verbose("*** strShipmentKey ***" + strShipmentKey);

		Document docGetShipmentListInp = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleShipment = docGetShipmentListInp.getDocumentElement();

		if (!YFCObject.isVoid(strShipmentKey)) {
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY+ AcademyConstants.ATTR_QRY_TYPE,
					AcademyConstants.GT_QRY_TYPE);
		}

		Calendar cal = Calendar.getInstance();
		if(strCutOffTime.contains(":")) {
			cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strCutOffTime.split(":")[0]));
			cal.set(Calendar.MINUTE, Integer.parseInt(strCutOffTime.split(":")[1]));
		}
		else {
			cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strCutOffTime));
		}
		cal.set(Calendar.SECOND, 0);

		String strAppointmentNo = sdf.format(cal.getTime());

		eleShipment.setAttribute(AcademyConstants.ATTR_APPOINTMENT_NO, strAppointmentNo);
		eleShipment.setAttribute(AcademyConstants.ATTR_APPOINTMENT_NO 
				+ AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.LT_QRY_TYPE);
	
		eleShipment.setAttribute(AcademyConstants.ATTR_PACKLIST_TYPE, AcademyConstants.STS_FA );
		eleShipment.setAttribute(AcademyConstants.ATTR_PACKLIST_TYPE_QRY_TYPE, AcademyConstants.STR_NE );
		Element eleExtn = SCXmlUtil.createChild(eleShipment, AcademyConstants.ELE_EXTN);
		eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_IS_INSTORE_PICKUP_OPTED, AcademyConstants.STR_YES);
		eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_INSTORE_PICKUP_INFO  + AcademyConstants.ATTR_QRY_TYPE ,"NOTNULL");

		Element eleComplexQry = SCXmlUtil.createChild(eleShipment, AcademyConstants.COMPLEX_QRY_ELEMENT);
		eleComplexQry.setAttribute(AcademyConstants.COMPLEX_OPERATOR_ATTR, AcademyConstants.COMPLEX_OPERATOR_AND_VAL);

		Element eleOr = SCXmlUtil.createChild(eleComplexQry, AcademyConstants.COMPLEX_OR_ELEMENT);
		Element eleExpY = SCXmlUtil.createChild(eleOr, AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExpY.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
		eleExpY.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_STATUS);
		eleExpY.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS);

		Element eleExp1 = SCXmlUtil.createChild(eleOr, AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp1.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
		eleExp1.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_STATUS);
		eleExp1.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STR_PAPER_WORK_INITIATED_STATUS);

		Element eleOrderBy = SCXmlUtil.createChild(eleShipment, AcademyConstants.ELE_ORDERBY);
		Element eleAttribute = SCXmlUtil.createChild(eleOrderBy, AcademyConstants.ELE_ATTRIBUTE);

		eleAttribute.setAttribute(AcademyConstants.ATTR_DESC_SHORT, AcademyConstants.STR_NO);
		eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_SHIPMENT_KEY);

		log.endTimer("AcademyInStoreAutoPickup.prepareInputForGetShipmentList()");

		return docGetShipmentListInp;
	}

	/**
	 * This method check if the Auto Close shipment agent is to be triggered or not
	 * 
	 * @param strShipmentKey
	 * @return docGetShipmentListInp
	 * @throws ParserConfigurationException 
	 * @throws Exception
	 * Sample Input XML for getShipmentList: 
	 * 
	 */
	private Boolean checkIfAutoCloseShipmentsToBeProcessed(YFSEnvironment env, String strStartTime, String strEndTime) throws Exception
	{
		log.beginTimer("AcademyInStoreAutoPickup.checkIfAutoCloseShipmentsToBeProcessed()");
		Boolean isBetween=false;

		log.verbose("strStartTime-> "+strStartTime);
		log.verbose("strEndTime-> "+strEndTime);

		Date dtSysDate = new Date();
		Calendar calNow = Calendar.getInstance();
		calNow.setTime(dtSysDate);

		Calendar calStartTime = Calendar.getInstance();
		if(strStartTime.contains(":")) {
			calStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strStartTime.split(":")[0]));
			calStartTime.set(Calendar.MINUTE, Integer.parseInt(strStartTime.split(":")[1]));
		}
		else {
			calStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strStartTime));
		}
		calStartTime.set(Calendar.SECOND, 0);

		Date dtStartTime = calStartTime.getTime();
		log.verbose(" StartTime is :: "+ sdf.format(calStartTime.getTime()));

		Calendar calEndTime = Calendar.getInstance();
		if(strEndTime.contains(":")) {
			calEndTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strEndTime.split(":")[0]));
			calEndTime.set(Calendar.MINUTE, Integer.parseInt(strEndTime.split(":")[1]));
		}
		else {
			calEndTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strEndTime));
		}
		calEndTime.set(Calendar.SECOND, 0);

		Date dtEndTime = calEndTime.getTime();

		log.verbose(" EndTime is :: "+ sdf.format(calEndTime.getTime()));
		//Check if the StartTime is less than End Time. Else update End time as next date
		if(dtEndTime.before(dtStartTime)) {
			calEndTime.add(Calendar.HOUR_OF_DAY, 24);
			log.verbose(" EndTime is :: "+ sdf.format(calEndTime.getTime()));
		}

		if(dtSysDate.after(dtStartTime) && dtSysDate.before(dtEndTime)) {
			isBetween = true;
		}

		log.verbose("isBetween-> "+isBetween);
		log.endTimer("AcademyInStoreAutoPickup.checkIfAutoCloseShipmentsToBeProcessed()");
		return isBetween;
	}


	/**
	 * This method prepares input to confirm the shipment and update the shipments as PickedUp
	 * 
	 * @param strShipmentKey
	 * @return docGetShipmentListInp
	 * @throws ParserConfigurationException 
	 * @throws Exception
	 * Sample Input XML for getShipmentList: 
	 * 
	 */
	private Document prepareInputForShipmentClosure(Element eleShipment) throws ParserConfigurationException {
		log.beginTimer("AcademyInStoreAutoPickup.prepareInputForShipmentClosure()");

		Document docConfirmShipmentInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleShipmentInp = docConfirmShipmentInput.getDocumentElement();
		
		String strShipmentNo = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		String strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		Element eleExtn = SCXmlUtil.getChildElement(eleShipment, AcademyConstants.ELE_EXTN);
		
		String strInstoreAssignedUser = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_INSTORE_ATTENDED_BY);
		
		String strConsolidatedShipment = strShipmentNo + "-" + strShipmentKey;
		
		eleShipmentInp.setAttribute(AcademyConstants.ATTR_CONSOLD_SHIPMENT, strConsolidatedShipment);
		eleShipmentInp.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
		eleShipmentInp.setAttribute(AcademyConstants.ATTR_VERIFICATION_METHOD, AcademyConstants.STR_YES);
		eleShipmentInp.setAttribute(AcademyConstants.DISPLAY_LOCALIZED_FIELD_IN_LOCALE, "en_US_CST");
		eleShipmentInp.setAttribute(AcademyConstants.ATTR_IGNORE_ORDERING, AcademyConstants.STR_YES);
		eleShipmentInp.setAttribute(AcademyConstants.ATTR_TRANSID, AcademyConstants.TRAN_CONFIRM_SHIPMENT);
		
		Element eleNotes = SCXmlUtil.createChild(eleShipmentInp, AcademyConstants.ELE_NOTES);
		Element eleNote = SCXmlUtil.createChild(eleNotes, AcademyConstants.ELE_NOTE);
		eleNote.setAttribute(AcademyConstants.STR_CONTACT_USER, strInstoreAssignedUser);
		eleNote.setAttribute(AcademyConstants.ATTR_NOTE_TEXT, "Shipment auto closed after being initiated by customer");
		eleNote.setAttribute(AcademyConstants.KEY_ARG_PRIORITY, AcademyConstants.ATTR_ZERO);
		eleNote.setAttribute(AcademyConstants.ATTR_REASON_CODE, "YCD_CUSTOMER_VERIFICATION");
		eleNote.setAttribute("VisibleToAll", AcademyConstants.STR_YES);
		
		
		log.verbose("Inside prepareInputForConfirmShipment:" );
		log.verbose("docConfirmShipmentInput" +  XMLUtil.getXMLString(docConfirmShipmentInput));
		
		log.endTimer("AcademyInStoreAutoPickup.prepareInputForShipmentClosure()");
		return docConfirmShipmentInput;
	}



}	