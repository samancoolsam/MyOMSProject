package com.academy.ecommerce.sterling.shipment;

/*##################################################################################
* Project Name                : CurbSide Acknowledgement
* Module                      : OMS
* Author                      : CTS
* Date                        : 16-AUG-2022 
* Description                 : Class to Update the Assignee value with the Format as UserName(UserId) having Input Attribute ExtnCurbsideAttendedBy as LoginId 
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author                Version#       Remarks/Description                      
* ---------------------------------------------------------------------------------
* 29-JULY-2022      CTS                   1.0               Initial version
* 25-AUG-2022       CTS                   2.0               OMNI-81715
* 20-OCT-2022       CTS                   3.0               OMNI-88936
* ##################################################################################*/
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AcademyUpdateCurbsideAssignee implements YIFCustomApi {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyUpdateCurbsideAssignee.class);

	public Document updatedUserid(YFSEnvironment env, Document inDoc) throws Exception {

		Document getUserListInDoc = null;
		Document getUserListOutDoc = null;
		String strUserName = null;
		String strAssignedUser = null;
		Element inDocEle = inDoc.getDocumentElement();
		String strLoginID = XPathUtil.getString(inDocEle, AcademyConstants.XPATH_EXTN_CURBSIDE_ATTENDED_BY);
		
		/* OMNI-81715 - Start */
		Node nlShipment =  XPathUtil.getNode(inDoc, AcademyConstants.ELE_SHIPMENT);
		String strCurbsideDelayMins =  XPathUtil.getString(nlShipment, AcademyConstants.XPATH_EXTN_CURB_DELAY_MINS);
		
	
			String strDateTypeFormat = AcademyConstants.STR_DATE_TIME_PATTERN;
			SimpleDateFormat sdf = new SimpleDateFormat(strDateTypeFormat);
		if(!YFCCommon.isVoid(strCurbsideDelayMins)) {
			Calendar cal = Calendar.getInstance();
			String strCurrentDateTime = sdf.format(cal.getTime());
			Element eleExtn = XmlUtils.getChildElement(inDocEle,  AcademyConstants.ELE_EXTN);
			eleExtn.setAttribute(AcademyConstants.EXTN_CURBSIDE_DELAY_REQ_TS, strCurrentDateTime);
		}
		/* OMNI-81715 - End */ 
		/* OMNI-79883 - START */
		String strCurbsideFinalDeliveryTS =  XPathUtil.getString(nlShipment, AcademyConstants.XPATH_EXTN_CURBSIDE_EXPECTED_DELIVERY_TS);
		if(!YFCCommon.isVoid(strCurbsideFinalDeliveryTS)) {
			String DateTimeFormatter = AcademyConstants.STR_DATE_TIME_FORMATTER;  //  "MM/dd/yyyy, hh:mm:ss aa"
			SimpleDateFormat dateFormat = new SimpleDateFormat(DateTimeFormatter , Locale.ENGLISH);
			Date parsingTSForDB =  dateFormat.parse(strCurbsideFinalDeliveryTS);
			String strFinalCurbDeliveryTStoDB = sdf.format(parsingTSForDB.getTime());
			// replacing the TS format value in ExtnCurbsideExpectedDeliveryTS with the new TS format value which DB will accept
			Element childEleExtn = XmlUtils.getChildElement(inDocEle,  AcademyConstants.ELE_EXTN);
			childEleExtn.removeAttribute(AcademyConstants.EXTN_CURB_EXPECT_DELIVERY_TS);
			childEleExtn.setAttribute(AcademyConstants.EXTN_CURB_EXPECT_DELIVERY_TS, strFinalCurbDeliveryTStoDB);
		}
		/* OMNI-79883 - END */
		
		if (!YFCObject.isVoid(strLoginID)) {

			getUserListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_USER);
			getUserListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_LOGINID, strLoginID);

			log.verbose("Input to getUserList API : " + XMLUtil.getXMLString(getUserListInDoc));

			env.setApiTemplate(AcademyConstants.API_GET_USER_LIST, AcademyConstants.GET_USER_LIST_TEMPLATE);
			getUserListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_USER_LIST, getUserListInDoc);
			env.clearApiTemplates();
			strUserName = XPathUtil.getString(getUserListOutDoc.getDocumentElement(), AcademyConstants.XPATH_USER_NAME);
			log.verbose("UserName " + strUserName);
			if (!YFCObject.isVoid(strUserName)) {
				strAssignedUser = strUserName + AcademyConstants.LEFT_PARENTHESIS + strLoginID
						+ AcademyConstants.RIGHT_PARENTHESIS;
			} else {
				return inDoc;
			}

			log.verbose("Updated User Field " + strAssignedUser);

			Node nExtn = inDoc.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
			Node nAttendedBy = nExtn.getAttributes().getNamedItem(AcademyConstants.EXTN_CURBSIDE_ATTENDED_BY);
			nAttendedBy.setTextContent(strAssignedUser);

			log.verbose("Updated Input to  ChangeShipment API : " + XMLUtil.getXMLString(inDoc));
		}

		return inDoc;

	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		

	}
	
	/**This method 
	 * 1. Add a new method updateCurbsideConsolidatedShipmentList in existing AcademyUpdateCurbsideAssignee.java
	 * 2. Call updatedUserid method to update fields as per existing flow
	 * 3. Split ConsolidatedShipmentKey attribute value to get the list of shipments to update.
	 * 4. For each Shipment (comma separated) changeShipment  is called with modified attributes. 
	 * 
	 */
	/**
	 * Sample I/p: 
	 *<Shipment ConsolidatedShipmentKey="202209160429255681775857"
     * CurbsideConsolidationToggle="Y" IgnoreOrdering="Y" ShipmentKey="202209160429255681775857">
     * <Extn ExtnCurbsideAttendedBy="sfstest033" ExtnCurbsideDelayCount="1" ExtnCurbsideDelayMains="5"/>
     * </Shipment>
	 * 
	 * Sample Output:
	 * <Shipment ShipmentKey="20220919004438288986822">
     * <Extn ExtnBulkGCActivationStatus="ReadyToProcess" ExtnCurbsideAttendedBy="sfs033(sfs033)" ExtnCurbsideDelayCount="0" ExtnCurbsideDelayMins="0" 
     * ExtnCurbsidePickupInfo="Acura:Black:1" ExtnExeterContainerId="" ExtnGCActivationCode="" ExtnGCBulkActivationFlag="N" ExtnIsCurbsidePickupOpted="Y" 
     * ExtnLinesCancelled="N" ExtnNonBulkGCActStatus="ReadyToProcess" ExtnOriginalShipmentLos="" ExtnOriginalShipmentScac="" ExtnOverrideUOrD="N" ExtnRMSTransferNo="" 
     * ExtnSCACPriority="100" ExtnShipUpgrdOrDowngrd="" ExtnShipmentPickedBy="Primary" ExtnShortpickReasonCode="" ExtnSopModifyts="2022-09-19T00:44:38-05:00" ExtnVendorShipmentNo=""/>
     *</Shipment>
	 * 
	 */
	public Document updateCurbsideConsolidatedShipmentList(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer("updateCurbSsideConsolidatedShipmentList");
		log.verbose(
				"AcademyUpdateCurbsideAssignee.updateCurbSsideConsolidatedShipmentList Input inDoc:: " + XMLUtil.getXMLString(inDoc));
		Document docSrvChangeShipOutput=null;
		updatedUserid(env,inDoc);
		String strCurbsideShipmentKey = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		log.debug("strCurbsideShipmentKey:: "+ strCurbsideShipmentKey);
		String strCurbsideConsolidatedShipKeys =inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_CONSOLIDATED_SHIPMENT_KEY);
		log.debug("strCurbsideConsolidatedShipKeys:: "+ strCurbsideConsolidatedShipKeys);
		List<String> lShipmentKeys = new ArrayList<String>(Arrays.asList(strCurbsideConsolidatedShipKeys.split(AcademyConstants.STR_COMMA)));
		log.debug("lShipmentKeys.size():: "+ lShipmentKeys.size());
		log.verbose("lShipmentKeys:: "+ lShipmentKeys);
		for(String strShipmentKey : lShipmentKeys)
		{  
			log.debug("strShipmentKey:: "+ strShipmentKey);
			inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,strShipmentKey);
			if(strCurbsideShipmentKey.equalsIgnoreCase(strShipmentKey))
			{
				log.verbose(
						"If block AcademyCurbsideChangeShipmentService Input XML inDoc:: "
								+ XMLUtil.getXMLString(inDoc));
				docSrvChangeShipOutput= AcademyUtil.invokeService(env,
						AcademyConstants.SERV_ACADEMY_CURBSIDE_CHANGE_SHIPMENT, inDoc);
			}else {
				log.verbose(
						"AcademyCurbsideChangeShipmentService Input XML inDoc:: "
								+ XMLUtil.getXMLString(inDoc));
				 AcademyUtil.invokeService(env,
							AcademyConstants.SERV_ACADEMY_CURBSIDE_CHANGE_SHIPMENT, inDoc);
			}
			
		}
		log.verbose(
				"AcademyUpdateCurbsideAssignee.updateCurbSsideConsolidatedShipmentList Output XML docSrvChangeShipoutput:: "
						+ XMLUtil.getXMLString(docSrvChangeShipOutput));
		log.endTimer("updateCurbSsideConsolidatedShipmentList");
		return docSrvChangeShipOutput;
		
	}
	
	// OMNI-88936 - Backend: Prevent additional time extensions in case of
	// Concurrent users - START
	/**
	 * 
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */

	/**
	 * This method is invoked to check if the Curbside extensions are already done
	 * to one/more shipments of a Curbside Enabled order If yes, throw exception If
	 * no, return inDoc
	 * 
	 **/
	/**
	 * Sample I/p: <Shipment BillToDayPhone="7730012642" BillToZipCode="77064"
	 * ConsolidatedShipmentKey="20221017024243293898599,20221017024927293898925"
	 * CurbsideConsolidationToggle="Y" CurbsideDelayMaxCounter="1" Msg1="We?re
	 * almost there. Curbside delivery of your order Order_No is taking longer than
	 * expected. We" Msg2="will be coming to you with your items shortly. We
	 * apologize for the inconvenience. Check your" Msg3="Curbside Pick Up status
	 * at" NotifyDelayToCustFlag="Y" OrderNo="511314352" ShipNode="033" ShipmentKey=
	 * "20221017024927293898925">
	 * <Extn ExtnCurbsideDelayCount="1" ExtnCurbsideDelayMins="2"
	 * ExtnCurbsideExpectedDeliveryTS="10/19/2022, 6:51:46 AM"/> </Shipment>
	 * 
	 * Sample Output: <Shipment BillToDayPhone="7730012642" BillToZipCode="77064"
	 * ConsolidatedShipmentKey="20221017024243293898599,20221017024927293898925"
	 * CurbsideConsolidationToggle="Y" CurbsideDelayMaxCounter="1" Msg1="We?re
	 * almost there. Curbside delivery of your order Order_No is taking longer than
	 * expected. We" Msg2="will be coming to you with your items shortly. We
	 * apologize for the inconvenience. Check your" Msg3="Curbside Pick Up status
	 * at" NotifyDelayToCustFlag="Y" OrderNo="511314352" ShipNode="033" ShipmentKey=
	 * "20221017024927293898925">
	 * <Extn ExtnCurbsideDelayCount="1" ExtnCurbsideDelayMins="2"
	 * ExtnCurbsideExpectedDeliveryTS="10/19/2022, 6:51:46 AM"/> </Shipment>
	 * 
	 * Sample Exception Output: <Errors>
	 * <Error ErrorCode="EXTN_ACADEMY_21" ErrorDescription="EXTN_ACADEMY_21"
	 * ErrorRelatedMoreInfo="">
	 * <Attribute Name="ErrorCode" Value="EXTN_ACADEMY_21"/>
	 * <Attribute Name="ErrorDescription" Value="EXTN_ACADEMY_21"/>
	 * </Error> </Errors>
	 */

	public Document isDelayExtensionAllowed(YFSEnvironment env, Document inDoc) throws YFCException, Exception {
		log.beginTimer("AcademyUpdateCurbsideAssignee.isDelayExtensionAllowed()");
		log.verbose("AcademyUpdateCurbsideAssignee.isDelayExtensionAllowed() :: Input doc: " +XMLUtil.getXMLString(inDoc));
		Element eleInDoc = inDoc.getDocumentElement();
		String strConsolidatedShipmentKey = eleInDoc.getAttribute(AcademyConstants.ATTR_CONSOLIDATED_SHIPMENT_KEY);
		String strShipmentKey = eleInDoc.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		if (YFCCommon.isVoid(strConsolidatedShipmentKey) && (YFCCommon.isVoid(strShipmentKey))) {
			return inDoc;
		}
		YFCDocument yfcDocShipment = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
		YFCElement yfcEleShipment = yfcDocShipment.getDocumentElement();
		YFCElement eleExtn = yfcEleShipment.createChild(AcademyConstants.ELE_EXTN);
		eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_CURSIDE_PICK_OPTED, AcademyConstants.STR_YES);
		YFCElement eleComplexQuery = yfcEleShipment.createChild(AcademyConstants.COMPLEX_QRY_ELEMENT);
		eleComplexQuery.setAttribute(AcademyConstants.COMPLEX_OPERATOR_ATTR, AcademyConstants.COMPLEX_AND_ELEMENT);
		YFCElement eleOr = eleComplexQuery.createChild(AcademyConstants.COMPLEX_OR_ELEMENT);

		if (!YFCCommon.isVoid(strConsolidatedShipmentKey)) {
			String strCurbsideConsolidatedShipKeys = eleInDoc
					.getAttribute(AcademyConstants.ATTR_CONSOLIDATED_SHIPMENT_KEY);
			log.verbose("AcademyUpdateCurbsideAssignee:: strCurbsideConsolidatedShipKeys:: " + strCurbsideConsolidatedShipKeys);
			List<String> lShipmentKeys = new ArrayList<String>(
					Arrays.asList(strCurbsideConsolidatedShipKeys.split(AcademyConstants.STR_COMMA)));
			lShipmentKeys.stream().forEach(str -> {
				YFCElement eleExp = eleOr.createChild(AcademyConstants.COMPLEX_EXP_ELEMENT);
				eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_SHIPMENT_KEY);
				eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
				eleExp.setAttribute(AcademyConstants.ATTR_VALUE, str);
			});
		} else if (!YFCCommon.isVoid(strShipmentKey)) {
			YFCElement eleExp = eleOr.createChild(AcademyConstants.COMPLEX_EXP_ELEMENT);
			eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_SHIPMENT_KEY);
			eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
			eleExp.setAttribute(AcademyConstants.ATTR_VALUE, strShipmentKey);
		}
		log.verbose("AcademyUpdateCurbsideAssignee.isDelayExtensionAllowed() :: getShipmentList Input XML inDoc:: "
				+ XMLUtil.getXMLString(yfcDocShipment.getDocument()));
		String template = "<Shipments>\r\n" + "<Shipment ShipmentKey=\"\">\r\n" + "<Extn/>\r\n" + "</Shipment>\r\n"
				+ "</Shipments>";
		env.setApiTemplate(AcademyConstants.GET_SHIPMENT_LIST_API, YFCDocument.getDocumentFor(template).getDocument());
		Document outDoc = AcademyUtil.invokeAPI(env, AcademyConstants.GET_SHIPMENT_LIST_API,
				yfcDocShipment.getDocument());
		env.clearApiTemplate(AcademyConstants.GET_SHIPMENT_LIST_API);
		log.verbose("AcademyUpdateCurbsideAssignee.isDelayExtensionAllowed() :: getShipmentList - outDoc:: " + XMLUtil.getXMLString(outDoc));
		String strDelayMaxCounter = eleInDoc.getAttribute(AcademyConstants.STR_CURB_DELAY_MAX_CNT);
		if (XPathUtil.getNodeList(outDoc, AcademyConstants.XPATH_EXTN_CURBDELAYCOUNT
				+ Integer.valueOf(strDelayMaxCounter) + AcademyConstants.CLOSING_BACKET).getLength() > 0) {
			log.verbose("AcademyUpdateCurbsideAssignee.isDelayExtensionAllowed() :: Delay Extensions Not Allowed... Throwing exception ");
			YFCException yfcEx = new YFCException(AcademyConstants.ERR_CODE_21);
			throw yfcEx;
		}
		log.endTimer("AcademyUpdateCurbsideAssignee.isDelayExtensionAllowed()");
		return inDoc;
	}

	// OMNI-88936 - Backend: Prevent additional time extensions in case of
	// Concurrent users - END

}