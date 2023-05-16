package com.academy.ecommerce.sterling.bopis.shipment;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.util.Calendar;
import java.util.Date;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

/*##################################################################################
*
* Project Name                : POD September Release - 2022
* Module                      : Fulfillment
* Author                      : Everest
* Date                        : 12-Sep-2022
* Description                 : This class is invoked from TC70 UI when the user clicks Start Customer
* 								Pick Option from JS. When Curbside Consolidation is enabled the related order's 
* 								shipment details will be recorded as part of ACAD_STORE_ACTION_DATA custom table eventhough 
* 								only one shipment is selected in UI.
* 								OMNI-85083
* ---------------------------------------------------------------------------------
* Date            Author                  Version#       Remarks/Description                     
* ---------------------------------------------------------------------------------
* 12-SEP-2022     Everest                      1.0            Initial version
* ##################################################################################*/

public class AcademyRecordStoreActionDataForCurbConsolidation{
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyRecordStoreActionDataForCurbConsolidation.class);

	/**
	 * This is the the main method being called as part of UI - Service
	 * 
	 * @param env
	 * @param inXML
	 * @return
	 * @throws Exception
	 * Sample Input XML: 
        		<AcadStoreActionData Delivery_Method="PICK" NotifyStore="N" 
            	OrderNo="BPS2022062114" ShipmentNo="100546132"  UserID="sfs165"/>
	 *
	 */
	public Document recordStoreActionDataForCurbside(YFSEnvironment env, Document inXML) throws Exception {
		
		log.verbose("AcademyRecordStoreActionDataForCurbsideConsolidation.recordStoreActionDataForCurbside input ::" + XMLUtil.getXMLString(inXML));
		String strOrderNo = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO);
		Document docGetShipmentListOut = null;
		ArrayList <String> arrShipmentNoList = new ArrayList <>();
		String strShipmentNo = null;
		docGetShipmentListOut = getShipmentList(env, strOrderNo);
		NodeList nlShipmentList = docGetShipmentListOut.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
		int iShipmentLength = nlShipmentList.getLength();
		for (int iShipmentCount = 0; iShipmentCount < iShipmentLength; iShipmentCount++) {
			
			Element eleCurrentShipment = (Element) nlShipmentList.item(iShipmentCount);
			strShipmentNo = eleCurrentShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			arrShipmentNoList.add(strShipmentNo);
		}	
		log.verbose("arrShipmentNoList::" + arrShipmentNoList);
		if (arrShipmentNoList.size() > 0) {
			callMultiApiForRecordStoreActionData(env,arrShipmentNoList,inXML);
		}
		log.verbose("End of AcademyRecordStoreActionDataForCurbConsolidation.recordStoreActionDataForCurbside() method");
		
		return inXML;
		
	}

	/** OMNI-83422 - Assign Team Member - Curbside Consolidation
	 * This method will invoke MultiApi with changeShipment and updates ExtnCurbsideAttendedBy for all the shipments.
	 * @param env
	 * @param alShipmentKey
	 * @param inXML
	 */
	public Document changeShipmentForAssignee(YFSEnvironment env, ArrayList<String> alShipmentKey, String strAttendedBy,
			String strExtnIsCurbsidePickupOpted, String strExtnIsinstorePickupOpted) {//OMNI-105778 - Added parameters
		//strExtnIsCurbsidePickupOpted and strExtnIsinstorePickupOpted - Start/End
		log.beginTimer("changeShipment to update ExtnCurbsideAttendedBy/ExtnInstoreAttendedBy");
		Document docMultiApiInput = null;
		Document docMultiApiOutput = null;
		Element eleShipment = null;
		Element eleExtn = null;
		Element eleApi = null;
		Element eleInput = null;
		String strShipmentKey = "";		
		try {
			docMultiApiInput = XMLUtil.createDocument(AcademyConstants.ELE_MULTIAPI);
			Element eleMultiApi = docMultiApiInput.getDocumentElement();
			
			if("Y".equals(strExtnIsCurbsidePickupOpted)){//OMNI-105578 - If Case
				for (int i = 0; i < alShipmentKey.size(); i++) {
					strShipmentKey = alShipmentKey.get(i);
					
					eleApi = SCXmlUtil.createChild(eleMultiApi, AcademyConstants.ELE_API);
					eleApi.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.API_CHANGE_SHIPMENT);
					eleInput = SCXmlUtil.createChild(eleApi, AcademyConstants.ELE_INPUT);
					eleShipment = SCXmlUtil.createChild(eleInput, AcademyConstants.ELE_SHIPMENT);
					eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
					eleExtn = SCXmlUtil.createChild(eleShipment, AcademyConstants.ELE_EXTN);
					eleExtn.setAttribute(AcademyConstants.EXTN_CURBSIDE_ATTENDED_BY, strAttendedBy);
				}
			
			log.verbose("MultiApi Input to update ExtnCurbsideAttendedBy:: \n" + XMLUtil.getXMLString(docMultiApiInput));
			docMultiApiOutput = AcademyUtil.invokeService(env, AcademyConstants.SEV_UPDATE_ASSIGNEE_CURB_MULTI_SHIPMENTS, docMultiApiInput);
			log.verbose("Multi API Output after updating ExtnCurbsideAttendedBy :: \n" + XMLUtil.getXMLString(docMultiApiOutput));
			} else if("Y".equals(strExtnIsinstorePickupOpted)) {//OMNI-105578 - ExtnInstoreAttendedBy Instore consolidation Assignee update - Start
				for (int i = 0; i < alShipmentKey.size(); i++) {
					strShipmentKey = alShipmentKey.get(i);
					
					eleApi = SCXmlUtil.createChild(eleMultiApi, AcademyConstants.ELE_API);
					eleApi.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.API_CHANGE_SHIPMENT);
					eleInput = SCXmlUtil.createChild(eleApi, AcademyConstants.ELE_INPUT);
					eleShipment = SCXmlUtil.createChild(eleInput, AcademyConstants.ELE_SHIPMENT);
					eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
					eleExtn = SCXmlUtil.createChild(eleShipment, AcademyConstants.ELE_EXTN);
					eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_INSTORE_ATTENDED_BY, strAttendedBy);
					eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_IS_INSTORE_PICKUP_OPTED, strExtnIsinstorePickupOpted);
					}
					
					log.verbose("MultiApi Input to update ExtnInstoreAttendedBy and strExtnIsinstorePickupOpted:: \n" + XMLUtil.getXMLString(docMultiApiInput));
					docMultiApiOutput = AcademyUtil.invokeService(env, AcademyConstants.SEV_UPDATE_ASSIGNEE_CURB_MULTI_SHIPMENTS, docMultiApiInput);
					log.verbose("Multi API Output after updating ExtnInstoreAttendedBy and strExtnIsinstorePickupOpted:: \n" + XMLUtil.getXMLString(docMultiApiOutput));
				}	//OMNI-105578 - End
		} catch (Exception c) {
			c.printStackTrace();
			log.info(" Exception in changeShipmentForAssignee :: " + c.toString());
		}
		log.endTimer("changeShipment to update ExtnCurbsideAttendedBy");
		return docMultiApiOutput;
	}
	
		//OMNI-105857 begin
	//This method is used to update the instorepickupopted,appointmentNo
	public Document changeShipmentForAssigneeAndInstoreOpted(YFSEnvironment env, ArrayList <String> alShipmentKey, String strAttendedBy) {
		log.beginTimer("changeShipment to update ExtnInstoreAttendedBy");
		Document docMultiApiInput = null;
		Document docMultiApiOutput = null;
		Element eleShipment = null;
		Element eleExtn = null;
		Element eleApi = null;
		Element eleInput = null;
		String strShipmentKey = "";		
		try {
			docMultiApiInput = XMLUtil.createDocument(AcademyConstants.ELE_MULTIAPI);
			Element eleMultiApi = docMultiApiInput.getDocumentElement(); 
			
			for (int i = 0; i < alShipmentKey.size(); i++) {
				strShipmentKey = alShipmentKey.get(i);
				
				eleApi = SCXmlUtil.createChild(eleMultiApi, AcademyConstants.ELE_API);
				eleApi.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.API_CHANGE_SHIPMENT);
				eleInput = SCXmlUtil.createChild(eleApi, AcademyConstants.ELE_INPUT);
				eleShipment = SCXmlUtil.createChild(eleInput, AcademyConstants.ELE_SHIPMENT);
				eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
				
				eleExtn = SCXmlUtil.createChild(eleShipment, AcademyConstants.ELE_EXTN);
				eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_INSTORE_ATTENDED_BY, strAttendedBy);//108608 - Fix update	
				eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_IS_INSTORE_PICKUP_OPTED,AcademyConstants.STR_YES);
				String strDateFormat = AcademyConstants.STR_DATE_TIME_PATTERN_NEW;
				SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
				Calendar cal = Calendar.getInstance();
				String strCurrentDate = sDateFormat.format(cal.getTime());
				log.verbose("Appointment date time : " + strCurrentDate);
				eleShipment.setAttribute(AcademyConstants.ATTR_APPOINTMENT_NO,strCurrentDate);
			}
			log.verbose("MultiApi Input to update ExtnInstoreAttendedBy:: \n" + XMLUtil.getXMLString(docMultiApiInput));
			docMultiApiOutput = AcademyUtil.invokeService(env, AcademyConstants.SEV_UPDATE_ASSIGNEE_CURB_MULTI_SHIPMENTS, docMultiApiInput);
			log.verbose("Multi API Output after updating ExtnInstoreAttendedBy :: \n" + XMLUtil.getXMLString(docMultiApiOutput));
		} catch (Exception c) {
			c.printStackTrace();
			log.info(" Exception in changeShipmentForAssignee :: " + c.toString());
		}
		log.endTimer("changeShipment to update ExtnInstoreAttendedBy");
		return docMultiApiOutput;
	}
	//OMNI-105857 End

	public String getUserID(YFSEnvironment env, String strUserID) {
		String strUserName = "";
		String strAttendedBy = "";
		Document getUserListInDoc = null;
		Document getUserListOutDoc = null;
		try {
			if (!YFCCommon.isVoid(strUserID)) {
				getUserListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_USER);
				getUserListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_LOGINID, strUserID);
				log.verbose("Input to getUserList API : " + XMLUtil.getXMLString(getUserListInDoc));

				env.setApiTemplate(AcademyConstants.API_GET_USER_LIST, 
						XMLUtil.getDocument(AcademyConstants.GET_USER_LIST_TEMPLATE));
				getUserListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_USER_LIST, getUserListInDoc);
				env.clearApiTemplate(AcademyConstants.API_GET_USER_LIST);
				
				strUserName = XPathUtil.getString(getUserListOutDoc.getDocumentElement(), AcademyConstants.XPATH_USER_NAME);
				log.verbose("UserName:: " + strUserName);
				if (!YFCCommon.isVoid(strUserName)) {
					strAttendedBy = strUserName + AcademyConstants.LEFT_PARENTHESIS + strUserID
							+ AcademyConstants.RIGHT_PARENTHESIS;
				}
			}
			log.verbose("ExtnCurbsideAttendedBy :: " + strAttendedBy);
		} catch (Exception i) {
			i.printStackTrace();
			log.info(" Exception in AcademyUpdateCurbsideAssignee :: " + i.toString());
		}		
		return strAttendedBy;
	}

	/**
	 * This method is be used to frame multiAPI input for ACAD_STORE_ACTION_DATA table for each shipments
	 * 
	 * @param env,
	 * @param strOrderNo
	 * Sample Input for MultiApi:
	 * <MultiApi>
    		<API FlowName="AcademyCreateStoreActionData">
        		<Input>
            		<AcadStoreActionData Delivery_Method="PICK" NotifyStore="N" OrderNo="73613027" ShipmentNo="2" UserID="sfs033"/>
        		</Input>
    		</API>
		</MultiApi>
	 */
	
	private void callMultiApiForRecordStoreActionData(YFSEnvironment env, List<String> arrShipmentNoList, Document inXML) throws Exception {
		    Document docMultiApi = null;
		    log.beginTimer(" Begining of AcademyRecordStoreActionDataForCurbsideConsolidation -> callMultiApiForRecordStoreActionData Api");
		    String strOrderNo = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO);
		    String strUserID = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_USER_ID);
		    String strDeliveryMethod = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_DELIV_METHOD);

		    docMultiApi = XMLUtil.createDocument("MultiApi");
		        Element eleMultiApi = docMultiApi.getDocumentElement();
		        
		        for (int j = 0; j < arrShipmentNoList.size(); j++) {
		        	
		            Element eleApi = docMultiApi.createElement(AcademyConstants.ELE_API);
		            eleMultiApi.appendChild(eleApi);
		            eleApi.setAttribute(AcademyConstants.ATTR_FLOW_NAME, AcademyConstants.ATTR_SEV_CREATE_STORE_ACTION_DATA);
		            Element eleInput = docMultiApi.createElement(AcademyConstants.ELE_INPUT);
		            eleApi.appendChild(eleInput);
		            String strShipmentNo = (String) arrShipmentNoList.get(j);
		            Element eleAcadStoreActionData = docMultiApi.createElement(AcademyConstants.STR_ACAD_STORE_ACTION_DATA);
		            eleInput.appendChild(eleAcadStoreActionData);
		            eleAcadStoreActionData.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		            eleAcadStoreActionData.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		            eleAcadStoreActionData.setAttribute(AcademyConstants.STR_NOTIFY_STORE, AcademyConstants.STR_NO);
		            eleAcadStoreActionData.setAttribute(AcademyConstants.ATTR_DELIV_METHOD, strDeliveryMethod);
		            eleAcadStoreActionData.setAttribute(AcademyConstants.ATTR_USER_ID, strUserID);
		        }
		        log.verbose("*** Input to MultiApi is *****" +XMLUtil.getXMLString(docMultiApi));
		        AcademyUtil.invokeAPI(env, AcademyConstants.API_MULTI_API, docMultiApi);
		        log.endTimer(" End of AcademyRecordStoreActionDataForCurbsideConsolidation  -> callMultiApiForRecordStoreActionData");
		}
	
	
	/**
	 * This method is be used to get Shipments which are Curbside opted and at RFCP status
	 * 
	 * @param env,
	 * @param strOrderNo
	 */
	private Document getShipmentList(YFSEnvironment env, String strOrderNo) throws Exception {
		log.verbose("Begin of AcademyRecordStoreActionDataForCurbConsolidation.getShipmentList() method");

		Document docShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleShipment = docShipmentList.getDocumentElement();

		eleShipment.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		eleShipment.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, AcademyConstants.STR_PICK);
		eleShipment.setAttribute(AcademyConstants.STATUS, AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS);
		
		Element eleExtn = XmlUtils.createChild(eleShipment, AcademyConstants.ELE_EXTN);
		eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_CURSIDE_PICK_OPTED, AcademyConstants.STR_YES);
		
		log.verbose("Input to API - getShipmentList :: " + XMLUtil.getXMLString(docShipmentList));
		
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST,
				XMLUtil.getDocument(AcademyConstants.TEMP_GET_SHIPMENTLIST));
		Document docGetShipmentListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, docShipmentList);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);

		log.verbose("End of AcademyRecordStoreActionDataForCurbConsolidation.getShipmentList() method");

		return docGetShipmentListOut;
	}
}