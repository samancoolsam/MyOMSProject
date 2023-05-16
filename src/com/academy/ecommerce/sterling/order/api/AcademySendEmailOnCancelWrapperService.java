package com.academy.ecommerce.sterling.order.api;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.academy.ecommerce.sterling.shipment.AcademySFSLOSUpgradeDowngradeProcess;
import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author sjohn
 * 
 *         Service Name: AcademySendEmailOnCancelWrapperService
 * 
 *         Wrapper Service to implement error handler when any exception occurs
 *         on sending email.
 * 
 **/

public class AcademySendEmailOnCancelWrapperService implements YIFCustomApi {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademySFSLOSUpgradeDowngradeProcess.class);
	private Properties props;

	public Document handleException(YFSEnvironment env, Document inXML) throws Exception {
		log.verbose("AcademySendEmailOnCancelWrapperService_handleException()_InXML:" + XMLUtil.getXMLString(inXML));
		
		//Code commented for OMNI-30147
		inXML.getDocumentElement().setAttribute("IsAllLinesCancelled", "N");
		inXML.getDocumentElement().setAttribute("IsSaveTheSale", "N");
		/*
		 * // Begin : OMNI-30147 boolean bSaveTheSaleCancel = false; // End : OMNI-30147
		 */
		
		// Start WN-697 : Sterling to consume special characters and include them in
		// customer-facing emails, but to remove them before settlement.
		Element elePersonInfoBillTo = XMLUtil.getElementByXPath(inXML, AcademyConstants.XPATH_ORDER_PERSONINFOBILLTO);
		AcademyUtil.convertUnicodeToSpecialChar(env, elePersonInfoBillTo, null, false);
		// End WN-697 : Sterling to consume special characters and include them in
		// customer-facing emails, but to remove them before settlement.
		inXML.getDocumentElement().setAttribute("IsBOPISEMailTemplateRequired", "N");
		try {

			HashMap<String, String> hmUnicodeMap = AcademyCommonCode.getCommonCodeListAsHashMapLngDesc(env,
					"CANCEL_REASON_EMAILS", AcademyConstants.HUB_CODE);

			List<Node> orderLineList = XMLUtil.getElementListByXpath(inXML, "Order/OrderLines/OrderLine");
			for (Node eleOrderLine : orderLineList) {

				String strDeliveryMethod = ((Element) eleOrderLine).getAttribute("DeliveryMethod");

				if (strDeliveryMethod != null && !YFCObject.isVoid(strDeliveryMethod)
						&& strDeliveryMethod.equals("PICK")) {
					String reasonCode = "";
					String emailText = "";
					Element OrderAuditLevel = null;
					String orderLineKey = ((Element) eleOrderLine).getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
					
					//Begin : OMNI-30147
					Element orderLineEle = (Element) eleOrderLine;					
					String strFulfillmentType = orderLineEle.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE) ;
					
					Element eleOrderLineExtn = XmlUtils.getChildElement(orderLineEle, AcademyConstants.ELE_EXTN);
					String strOrigFulfillmentType = null;
					if(null != eleOrderLineExtn) {
						
						log.verbose("Extn element :: "+XMLUtil.getElementXMLString(eleOrderLineExtn));
						
						strOrigFulfillmentType = eleOrderLineExtn.getAttribute("ExtnOriginalFulfillmentType");
					}
					//End : OMNI-30147
					
					
					
					List<Node> OrderAuditLevels = XMLUtil.getElementListByXpath(inXML,
							"Order/OrderAudit/OrderAuditLevels/OrderAuditLevel");
					for (Node OrderAuditlevel : OrderAuditLevels) {

						Document auditlevel = XMLUtil.getDocumentForElement((Element) OrderAuditlevel);
						Element ModificationType = XMLUtil.getElementByXPath(auditlevel,
								"OrderAuditLevel[@OrderLineKey='" + orderLineKey
										+ "']/ModificationTypes/ModificationType[@Name='CANCEL']");
						if (!YFCObject.isVoid(ModificationType)) {
							OrderAuditLevel = (Element) OrderAuditlevel;
						}

					}

					if (!YFCObject.isVoid(OrderAuditLevel)) {
						Document auditlevel = XMLUtil.getDocumentForElement((Element) OrderAuditLevel);
						reasonCode = XMLUtil.getElementByXPath(auditlevel,
								"OrderAuditLevel/OrderAuditDetails/OrderAuditDetail[@AuditType='Note']/Attributes/Attribute[@Name='ReasonCode']") == null
										? "Other_Reasons"
										: XMLUtil.getElementByXPath(auditlevel,
												"OrderAuditLevel/OrderAuditDetails/OrderAuditDetail[@AuditType='Note']/Attributes/Attribute[@Name='ReasonCode']")
												.getAttribute("NewValue");
						
						//Begin: OMNI-30147
						if(AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE.equals(strOrigFulfillmentType)
								&& AcademyConstants.STR_SHIP_TO_STORE.equals(strFulfillmentType)
								&& AcademyConstants.STR_INVENTORY_RESERVATION_FAILURE.equals(reasonCode)) {
							
							//Commented code for OMNI-30147
							boolean isAllLineCancelled = AcademyUtil.isAllLinesCancelledOfShipment(env, "",
									orderLineKey);
							if (isAllLineCancelled) {
								log.verbose("isAllLineCancelled flag is set :: "+isAllLineCancelled);
								inXML.getDocumentElement().setAttribute("IsAllLinesCancelled", "Y");
								
							}
							//Commented code for OMNI-30147
								log.verbose("SaveTheSale flag is set :: "+isAllLineCancelled);
								inXML.getDocumentElement().setAttribute("IsSaveTheSale", "Y");
						}
						//End: OMNI-30147
						
						String reasonText = hmUnicodeMap.get(reasonCode);
						if (reasonText != null && !YFCObject.isVoid(reasonText)) {

							Document docGetReasonCodesInput = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
							docGetReasonCodesInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE,
									"EMAIL_CONTENT");
							docGetReasonCodesInput.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE,
									AcademyConstants.HUB_CODE);
							Document docCommonCodeListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST,
									docGetReasonCodesInput);
							   
							List<Node> eleOrderPickUp=XMLUtil.getElementListByXpath(docCommonCodeListOutput, "/CommonCodeList/CommonCode[@CodeShortDescription='"+reasonText+"']");
							
							for (int i = 1; i <= eleOrderPickUp.size(); i++)
							{
								String strCodeValue=reasonText+"_"+i;
								emailText=emailText+XMLUtil
										.getElementByXPath(docCommonCodeListOutput,
												"/CommonCodeList/CommonCode[@CodeValue='"+ strCodeValue + "']")
										.getAttribute("CodeLongDescription");
										
							}

							inXML.getDocumentElement().setAttribute("EmailText", emailText);
							inXML.getDocumentElement().setAttribute("IsBOPISEMailTemplateRequired", "Y");
							
							
							//checking for alternate email person, if presents appending alternate emailID to primaryEmail with comma(,) separation.
							if (!YFCCommon
									.isVoid(SCXmlUtil.getChildElement(inXML.getDocumentElement(), AcademyConstants.ELE_PERSON_INFO_MARK_FOR))) {
								Element elePersonInfoMarkFor = SCXmlUtil.getChildElement(inXML.getDocumentElement(), AcademyConstants.ELE_PERSON_INFO_MARK_FOR);
								if (elePersonInfoMarkFor.hasAttribute(AcademyConstants.ATTR_EMAILID)) {
									String StrAltEmailID = elePersonInfoMarkFor.getAttribute(AcademyConstants.ATTR_EMAILID);
									String CustomerEMailID = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID);
									log.verbose("******* Alternate Contact EmailId Available **** " + StrAltEmailID);
									CustomerEMailID = CustomerEMailID + "," + StrAltEmailID;
									inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID, CustomerEMailID);
								}
							}
							
						}

					}

				}

				
			}
			
			
			//Commented code for OMNI-30147
			/*
			 * // Begin: OMNI-30147 : If condition is added if (!bSaveTheSaleCancel) { //
			 * End: OMNI-30147
			 * 
			 * Document emailSentOutDoc = AcademyUtil.invokeService(env,
			 * AcademyConstants.ACAD_SEND_EMAIL_ON_CANCEL_SERVICE, inXML); }
			 */
			
		} catch (Exception ex) {
			ex.printStackTrace();
			log.info("An exception has occured while sending email on Order Cancellation");
			log.info("AcademySendEmailOnCancelWrapperService_InXML: " + XMLUtil.getXMLString(inXML));
		}
		return inXML;
	}

	public void setProperties(Properties props) {
		this.props = props;
	}
}
