package com.academy.ecommerce.sterling.bopis.monitor;

/**#########################################################################################
 *
 * Project Name                : Release 3
 * Module                      : OMS :: OMNI-436
 * Author                      : Radhakrishna Mediboina
 * Author Group				   : CTS-POD
 * Date                        : 12-Aug-2019 
 * Description				   : This agent is used to Fetch the list of shipments
 * 								 that are in ReadyforBackroomPick & 
 * 								 BackroomPickinProgress Status > 2 hrs then
 * 								 will send an Escalation email to respective store leaders.
 * 								 
 * ---------------------------------------------------------------------------------
 * Date            Author         		Version#       		Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 12-Aug-2019		CTS  	 			  1.0           	Initial version
 *
 * #########################################################################################*/

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySendBOPISEscalationEmails extends YCPBaseAgent{

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySendBOPISEscalationEmails.class);    
	
	/** 
	 * This method fetch the BOPIS shipments which are stuck in ReadyForBackroomPick &
	 * BackroomPickInProgress status where Shipment Createts > 2hrs.
	 * 
	 * @param env
	 * @param inXML
	 * @param docLastMessage
	 * 
	 * @return lShipments
	**/
	
	public List<Document> getJobs(YFSEnvironment env, Document inXML, Document docLastMessage) throws Exception {	
		log.verbose("Begin of AcademySendBOPISEscalationEmails.getJobs() method");
		List<Document> lShipments = new ArrayList<Document>();
		Document docGetShipmentListOut = null;
		Element eleShipment = null;
		String strLastShipmentKey = null;
		log.verbose("Input Message to the AcademySendBOPISEscalationEmails.getJobs()"+SCXmlUtil.getString(inXML)); 
		
		String strEscalationSLAForEmail = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ESCALATION_SLA_FOR_EMAIL);
		String strFromShipmentCreatets = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_FROM_SHIPMENT_CREATETS);
		String strMaximumRecords = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_RECORDS_TO_BUFFER);
		String strShipmentStatus = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_STATUS);
		String strStoreOpenTime = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_STORE_OPEN_TIME);
		String strStoreCloseTime = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_STORE_CLOSE_TIME);

		if (!YFCObject.isVoid(docLastMessage)) {
			log.verbose("LastMessage is present for AcademySendBOPISEscalationEmails:\n"+XMLUtil.getXMLString(docLastMessage));
			strLastShipmentKey = docLastMessage.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			log.verbose("LastMessage ShipmentKey : "+strLastShipmentKey);
		}
		//Calling getShipmentList API for BOPIS orders where DeliveryMethod='PICK' and NodeType='Store'
		docGetShipmentListOut = callgetShipmentList(env, strLastShipmentKey, strShipmentStatus, strFromShipmentCreatets, strEscalationSLAForEmail, strMaximumRecords);
		log.verbose("API getShipmentList output :: \n"+XMLUtil.getXMLString(docGetShipmentListOut));

		NodeList nlShipmentList = docGetShipmentListOut.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
		int iEleShipmentCount = nlShipmentList.getLength();
		log.verbose("NodeList nlShipmentList Length is : "+iEleShipmentCount);
		if(iEleShipmentCount > 0) {
			for (int iShipmentCount = 0; iShipmentCount < iEleShipmentCount; iShipmentCount++) {
				eleShipment = (Element) nlShipmentList.item(iShipmentCount);	
				eleShipment.setAttribute(AcademyConstants.ATTR_STORE_OPEN_TIME, strStoreOpenTime);
				eleShipment.setAttribute(AcademyConstants.ATTR_STORE_CLOSE_TIME, strStoreCloseTime);
				eleShipment.setAttribute(AcademyConstants.ATTR_ESCALATION_SLA_FOR_EMAIL, strEscalationSLAForEmail);
				lShipments.add(XMLUtil.getDocumentForElement(eleShipment));
			}
		}		
		log.verbose("End of AcademySendBOPISEscalationEmails.getJobs() method");		
		return lShipments;     
	}
	
	/** 
	 * This method invoke getShipmentList API to get the shipments which are stuck in ReadyForBackroomPick &
	 * BackroomPickInProgress Status > 2 hrs.
	 * 
	 * @param env
	 * @param strLastShipmentKey
	 * @param strShipmentStatus
	 * @param strFromShipmentCreatets
	 * @param strEscalationHoursForEmail
	 * @param strMaximumRecords
	 *  
	 * @return docgetShipmentListOutput
	 * 
	**/
	private Document callgetShipmentList(YFSEnvironment env, String strLastShipmentKey, String strShipmentStatus, String strFromShipmentCreatets,
			String strEscalationSLAForEmail, String strMaximumRecords) throws Exception {
		log.verbose("Begin of AcademySendBOPISEscalationEmails.callGetShipmentList() method");
		
		Document docgetShipmentListOutput = null;
		Element eleExp = null;

		String strFromCreatets = getCreatets(strFromShipmentCreatets);
		String strToCreatets = getCreatets(strEscalationSLAForEmail);
		log.verbose("Shipments CREATETS FromDate : " +strFromCreatets+" ToDate : "+strToCreatets);
		
		/*
		 * if ShipmentStatus = 1100.70.06.10,1100.70.06.20 in criteria parameters,
		 * 
		 * <Shipment CreatetsQryType="BETWEEN" DeliveryMethod="PICK" FromCreatets="2019-08-24T00:21:08" MaximumRecords="500"
			    ShipmentKey="201908252302072056256200" ShipmentKeyQryType="GT" ToCreatets="2019-08-28T22:21:08">
			    <ComplexQuery Operator="AND">
			        <Or>
			            <Exp Name="Status" StatusQryType="EQ" Value="1100.70.06.10"/>
			            <Exp Name="Status" StatusQryType="EQ" Value="1100.70.06.20"/>
			        </Or>
			    </ComplexQuery>
			    <ShipNode NodeType="Store"/>
			    <OrderBy>
			        <Attribute Desc="N" Name="ShipmentKey"/>
			    </OrderBy>
			</Shipment>
		 **/
		Document docgetShipmentrListInput = XMLUtil.getDocument(
				"<Shipment MaximumRecords='"+strMaximumRecords+"' DeliveryMethod='PICK' CreatetsQryType='BETWEEN' FromCreatets='"+strFromCreatets+"' ToCreatets='"+strToCreatets+"'>"
				+	"<ComplexQuery Operator='AND'>"
				+		"<Or>"
			  //The Exp element will be add dynamically, i.e. depends on num of ShipmentStatus and that was configured in Agent criteria parameters.
			  //+			"<Exp Name='Status' Value='1100.70.06.10' StatusQryType='EQ'/>"
			  //+			"<Exp Name='Status' Value='1100.70.06.20' StatusQryType='EQ'/>"
				+		"</Or>"
				+	"</ComplexQuery>" 
			    +	"<ShipNode NodeType='Store'/>"
				+	"<OrderBy>"
				+		"<Attribute Name='ShipmentKey' Desc='N'/>"
				+	"</OrderBy>" 
				+"</Shipment>");		
		Element eleOr = XMLUtil.getElementByXPath(docgetShipmentrListInput, AcademyConstants.XPATH_ELE_OR);
		String[] strStatus = strShipmentStatus.split(AcademyConstants.STR_COMMA);
		int iLength = strStatus.length;
		if(iLength != 0) {
			for(int i=0; i < iLength; i++) {			
				 eleExp = docgetShipmentrListInput.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
				 eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.STATUS);
				 eleExp.setAttribute(AcademyConstants.ATTR_VALUE, strStatus[i]);
				 eleExp.setAttribute(AcademyConstants.ATTR_STATUS_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
				 eleOr.appendChild(eleExp);			
			}
		}
		if(!YFCObject.isVoid(strLastShipmentKey)) {
			docgetShipmentrListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strLastShipmentKey);
			docgetShipmentrListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY+AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.GT_QRY_TYPE);
		}
		Document docgetShipmentListTemplate = XMLUtil.getDocument(
				"<Shipments>"
				+	"<Shipment ShipmentKey=\"\" Status=\"\" StatusDate=\"\" Createts=\"\" ShipNode=\"\" DeliveryMethod=\"\" TotalQuantity=\"\">"
				+		"<ShipmentLines>"
				+			"<ShipmentLine OrderNo=\"\" PrimeLineNo=\"\" Quantity=\"\">"
				+				"<Order OrderDate=\"\" OrderNo=\"\"/>"
				+			"</ShipmentLine>"
				+		"</ShipmentLines>"
				+		"<ShipNode Localecode=\"\" NodeType=\"\" ShipNode=\"\"/>"
				+	"</Shipment>"
				+"</Shipments>");		
		log.verbose("getShipmentList API indoc XML : \n" + XMLUtil.getXMLString(docgetShipmentrListInput));
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docgetShipmentListTemplate);
		docgetShipmentListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, docgetShipmentrListInput);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		log.verbose("Output of getShipmentList API Output XML : \n" + XMLUtil.getXMLString(docgetShipmentListOutput));
		log.verbose("End of AcademySendBOPISEscalationEmails.callGetOrderList() method");
		
		return docgetShipmentListOutput;
	}
	
	/** 
	 * This method return shipment Createts as SysDate-NoOfHours.
	 * 
	 * @param strNoOfHours
	 * 
	 * @return dateFormat
	 * 
	 */
	private String getCreatets(String strNoOfHours) {
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
        cal.add(Calendar.HOUR, -(Integer.parseInt(strNoOfHours)));
        return dateFormat.format(cal.getTime());	
	}

	/** 
	 * This method send an Escalation mails to respective stores based on store localecode and shipment create time.
	 * 
	 * @param inXML
	 * @param env
	 * 
	 */
	public void executeJob(YFSEnvironment env, Document inXML) throws Exception {		
		log.verbose("Begin of AcademySendBOPISEscalationEmails.executeJob() method");
		log.verbose("Document inXML ::\n" + XMLUtil.getXMLString(inXML));		
		Date dtYesterdayCutOffDate = null;
		Date dtShmntCreateDate = null;
		Date dtTodayCutOffDate = null;
		boolean bInvokeService = false;
		String strCurrentDate = null;
		String strTimezone = null;
		String strShipmentCreatets1 = null;
		String strOrderDate1 = null;
		SimpleDateFormat sdf = null;
		SimpleDateFormat sdf1 = null;
		String strYesterdayCutOffTime = null;
		String strTodayCutOffTime = null;
		Calendar cal = null;
		
		String strStoreOpenTime = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_STORE_OPEN_TIME);
		String strStoreCloseTime = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_STORE_CLOSE_TIME);
		String strShipmentCreatets = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_CREATETS);
		String strStoreID = inXML.getDocumentElement().getAttribute(AcademyConstants.SHIP_NODE);
		String strSLAHours = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ESCALATION_SLA_FOR_EMAIL);		
		String strOrderDate = XMLUtil.getAttributeFromXPath(inXML, AcademyConstants.XPATH_ATTR_ORDER_DATE);
		String strLocalecode = XMLUtil.getAttributeFromXPath(inXML, AcademyConstants.XPATH_ATTR_LOCALE_CODE);		
		log.verbose("Shipment Localecode is:: " + strLocalecode);					
		
		//get and set the Store leader and Regional District Manager email ID's
		String[] strEmailIDs = getEmailIDs( env, strStoreID);		
		if(!YFSObject.isVoid(strEmailIDs[0])) {		
			inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_STORE_EMAIL_ID, strEmailIDs[0]);
			inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_STORE_RDM_EMAIL_ID, strEmailIDs[1]);
		}		
		
		if(!YFCObject.isVoid(strLocalecode)) {
			
			sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			//Fetch the timezone for localecode
			strTimezone = getLocalecodeTimezone(env, strLocalecode);
			TimeZone.setDefault(TimeZone.getTimeZone(strTimezone));
			sdf1 = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);		
			cal = Calendar.getInstance();			
			strCurrentDate = sdf1.format(cal.getTime());
			log.verbose("Current Timezone is :: "+strTimezone+" and Time is :: " + strCurrentDate);				
			dtShmntCreateDate = sdf.parse(strShipmentCreatets);
			log.verbose("Converted Shipment Create Date to Current Timezone Date Format:: "+dtShmntCreateDate);				
			strShipmentCreatets1 = sdf1.format(dtShmntCreateDate);
			inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_CREATETS, strShipmentCreatets1);
			log.verbose("Converted Shipment Create Date to current Timezone String Format:: "+strShipmentCreatets1);
			strOrderDate1 = sdf1.format(sdf.parse(strOrderDate));
			inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_DATE, strOrderDate1);
			log.verbose("Converted Order Date to Current Timezone Date Format:: "+strOrderDate1);								
			int iCurrentTime = Integer.parseInt(strCurrentDate.substring(11,13));
			log.verbose("Current Time is:: "+iCurrentTime+", for the timezone : "+strTimezone);			
			strTodayCutOffTime = strCurrentDate.substring(0,10)+"T"+String.valueOf(Integer.parseInt(strStoreCloseTime)-Integer.parseInt(strSLAHours))+":01:00-05:00";
			log.verbose("Today CutOff Time of Timezone:: "+strTodayCutOffTime);
			//Today cutoff Date is StoreCloseTime-EscalationSLA
			dtTodayCutOffDate = sdf1.parse(strTodayCutOffTime);
			log.verbose("Today CutOff Date :: "+dtTodayCutOffDate+", for the timezone : "+strTimezone);
			cal.add(Calendar.DATE, -1);
			strYesterdayCutOffTime = sdf1.format(cal.getTime()).substring(0,10)+"T"+String.valueOf(Integer.parseInt(strStoreCloseTime)-Integer.parseInt(strSLAHours))+":01:00-05:00";
			log.verbose("Yesterday CutOff Time of timezone:: "+strYesterdayCutOffTime);
			//Yesterday cutoff Date is StoreCloseTime-EscalationSLA
			dtYesterdayCutOffDate = sdf1.parse(strYesterdayCutOffTime);
			log.verbose("Yesterday CutOff Date :: "+dtYesterdayCutOffDate+", for the timezone : "+strTimezone);
			TimeZone.setDefault(TimeZone.getTimeZone(AcademyConstants.STR_TIMEZONE_CST));			
			log.verbose("Before Invoking the AcadSendBOPISEscalationEmailSyncService inXML is :\n" + XMLUtil.getXMLString(inXML));
			//Current time should be in-between Store open and close timings(i.e. 8AM - 8PM) for the timeZone of localecode
			if( (iCurrentTime >= (Integer.parseInt(strStoreOpenTime))) && (iCurrentTime < (Integer.parseInt(strStoreCloseTime))) ) {
				
				if(dtShmntCreateDate.before(dtYesterdayCutOffDate)) {					
					bInvokeService = true;
					log.verbose("AcademySendBOPISEscalationEmails.executeJob(): Shipment created before yseterday cutoff SLA");
				}
				else if( iCurrentTime >= (Integer.parseInt(strStoreOpenTime)+Integer.parseInt(strSLAHours)) && (dtShmntCreateDate.before(dtTodayCutOffDate))){					
					bInvokeService = true;
					log.verbose("AcademySendBOPISEscalationEmails.executeJob(): Shipment created between after yseterday cutoff SLA and before today cutoff SLA");
				}
			}
			else {
				log.verbose("Stores were closed at this time : "+strCurrentDate+", for the timezone : "+strTimezone);
			}		
			if(bInvokeService){
				//Invoking the AcadSendBOPISEscalationEmailSyncService to send escalation emails
				log.verbose("Start: Invoking the AcadSendBOPISEscalationEmailSyncService service, inXML :\n" + XMLUtil.getXMLString(inXML));
				AcademyUtil.invokeService(env,AcademyConstants.SERVICE_ACAD_SEND_BOPIS_ESCALATION_EMAIL, inXML);
				log.verbose("End: Invoking the AcadSendBOPISEscalationEmailSyncService service ");
			}
		}
		log.verbose("End of AcademySendBOPISEscalationEmails.executeJob() method");
	}
	
	/** 
	 * This method return Timezone based on Localecode.
	 * 
	 * @param env
	 * @param strLocalecode
	 * 
	 * @return strTimeZone
	 * 
	 */
	private String getLocalecodeTimezone(YFSEnvironment env, String strLocalecode) throws Exception {
		
		Document docGetLocaleListInput = null;
		Document docGetLocaleListOutput = null;	
		String strTimezone = null;
		
		docGetLocaleListInput = XMLUtil.createDocument(AcademyConstants.ELE_LOCALE);
		docGetLocaleListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_LOCALECODE, strLocalecode);
		//Invoke getLocaleList API to fetch Timezone for LocaleCode
		log.verbose("API getLocaleList In Doc :\n" + XMLUtil.getXMLString(docGetLocaleListInput));
		docGetLocaleListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_LOCALE_LIST, docGetLocaleListInput);
		log.verbose("API getLocaleList Out Doc :\n" + XMLUtil.getXMLString(docGetLocaleListOutput));		
		strTimezone = XMLUtil.getAttributeFromXPath(docGetLocaleListOutput, AcademyConstants.XPATH_ATTR_TIMEZONE);
		log.verbose("Timezone is : "+strTimezone+", for the Localecode : "+strLocalecode);		
		return strTimezone;
	}
	
	/** 
	 * This method return Store email id and Regional District Manager email ID based on StoreID 
	 * Fetching from custom table "ACAD_STORE_REGION_LOOKUP".
	 * 
	 * @param env
	 * @param strStoreID
	 * 
	 * @return strEmailIds[]
	 * 
	 */
	private String[] getEmailIDs(YFSEnvironment env, String strShipNode) throws Exception {
		
		Document docGetStoreRegionLookupInput = null;
		Document docGetStoreRegionLookupOutput = null;		
		String[] strEmailIds  = new String[2];
		
		docGetStoreRegionLookupInput = XMLUtil.createDocument(AcademyConstants.ELE_ACADSTOREREGIONLOOKUP);
		docGetStoreRegionLookupInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIP_NODE, strShipNode);
		//Invoke AcademyGetStoreRegionLookup Service to get the Email IDs.
		log.verbose("AcademyGetStoreRegionLookup Service in Doc :\n" + XMLUtil.getXMLString(docGetStoreRegionLookupInput));
		docGetStoreRegionLookupOutput = AcademyUtil.invokeService(env,AcademyConstants.SERVICE_ACAD_GET_STORE_REGION_LOOKUP, docGetStoreRegionLookupInput);
		strEmailIds[0] = XMLUtil.getAttributeFromXPath(docGetStoreRegionLookupOutput, AcademyConstants.XPATH_ATTR_STORE_EMAIL_ID);
		strEmailIds[1] = XMLUtil.getAttributeFromXPath(docGetStoreRegionLookupOutput, AcademyConstants.XPATH_ATTR_STORE_RDM_EMAIL_ID);	
		log.verbose("\nStore Email ID : "+strEmailIds[0]+"\nRegional District Manager Email ID : "+strEmailIds[1]+"\nFor the Store ID : "+strShipNode+"\n");		
		return strEmailIds;
	}
}
