package com.academy.ecommerce.sterling.condition;

import java.util.Map;
import java.util.Arrays;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.ParserConfigurationException;
import com.academy.ecommerce.sterling.carrierupdates.AcademyCarrierStatusLookup;
import com.academy.ecommerce.sterling.carrierupdates.AcademyCarrierStatusTracker;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

//EFP-21 Carrier Updates Consumption
public class AcademyIsDeliveredUpdate implements
YCPDynamicConditionEx {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyIsDeliveredUpdate.class);
	private Map propMap = null;
	private AcademyCarrierStatusTracker objCarrTracker = new AcademyCarrierStatusTracker();
	
	public void setProperties(Map propMap) {
		this.propMap=propMap;
		log.verbose("setup the PropMap");
	}
	
	public boolean evaluateCondition(YFSEnvironment env, String sName,
			Map mapData, Document inDoc) {
		
		boolean isDeliveryUpdate = false;
		
		try{
			
			log.debug("Start - AcademyIsDeliveredUpdate ###");
			
			String strProcessSubUpdates = (String) propMap.get(AcademyConstants.STR_PROCESS_SUB_UPDATES_SWITCH);
			
			//OMNI-437 : Begin
			String strUSPSRAI = (String) propMap.get(AcademyConstants.STR_USPS_RAI);
			String strUSPSRAILength = (String) propMap.get(AcademyConstants.STR_USPS_RAI_LENGTH);
			//OMNI-437 : End
			
			// Start : OMNI-109332 : Handling In Flight Orders with Convey Enabled
			String strCarrierConveyStartDate = (String) propMap.get("ConveyStartDate");
			boolean isTrackingUpdateIgnored = false;
			// End : OMNI-109332 : Handling In Flight Orders with Convey Enabled
			
			if(!YFCObject.isNull(strProcessSubUpdates) && !YFCObject.isVoid(strProcessSubUpdates)){
				
				log.debug("AcademyIsDeliveredUpdate - Switch is on ###");
				
				Element inpEle = inDoc.getDocumentElement();
				String strStatus = inpEle.getAttribute(AcademyConstants.ATTR_STATUS);
				//Start : OMNI-81897 : Skipping duplicate carrier delivery updates
				inDoc = getScacAndShipmentStatusForCarrierUpdates(env, inDoc);
				String strScac = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SCAC);				
				String strCurrentShipmentStatus = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATT_CURR_SHP_STATS);				
				//End : OMNI-81897 : Skipping duplicate carrier delivery updates
				//OMNI-101569 Begin
				String strDocumentType=inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DOC_TYPE);
				String strSTSTrackingUpdates=(String)propMap.get(AcademyConstants.STR_STS_TRACKING_UPDATES_SWITCH);
				//OMNI-101569 End
				//OMNI-101569 Begin
				String strMsterStatus=null;
				String strStatusDesc=null;
				 String strTrackingNo = inpEle.getAttribute(AcademyConstants.ATTR_TRACKING_NO);
				 String	strShipmentKey=inDoc.getDocumentElement().getAttribute(AcademyConstants.SHIPMENT_KEY);
				if(!YFCObject.isVoid(strSTSTrackingUpdates)&&AcademyConstants.STR_YES.equalsIgnoreCase(strSTSTrackingUpdates)&&
				   !YFCObject.isVoid(strDocumentType)&&AcademyConstants.DOCUMENT_TYPE.equals(strDocumentType))
				{
					//get carrier lookup
					if(!YFCObject.isNull(strStatus) && !YFCObject.isVoid(strStatus)
							&& !YFCObject.isNull(strScac) && !YFCObject.isVoid(strScac)){
						
						Document carrierStatusLookupDoc = AcademyCarrierStatusLookup
							.getCarrierStatusDetails(env, strStatus.trim(), strScac.trim());
						log.debug("Output from AcademyCarrierStatusLookup API::"+XMLUtil.getXMLString(carrierStatusLookupDoc));
						if(!YFCObject.isNull(carrierStatusLookupDoc) && 
								!YFCObject.isVoid(carrierStatusLookupDoc))
						{
							strMsterStatus = carrierStatusLookupDoc.getDocumentElement()
							.getAttribute(AcademyConstants.ATTR_MASTER_STATUS);
							strStatusDesc=carrierStatusLookupDoc.getDocumentElement()
								.getAttribute(AcademyConstants.ATTR_STATUS_DESCRIPTION);
						log.debug("MasterStatus  :"+strMsterStatus +" "+"Status Description :"+strStatusDesc);	
						}
					}
					
					//get Container status
					Document docContainerListOut=getExtnTrackingStatusForContainer(env,strTrackingNo);
					String strExtnTrackingStatus=XMLUtil.getAttributeFromXPath(docContainerListOut, "//Containers/Container/Extn/@ExtnTrackingStatus");
					//String strExtnTrackingMasterStatus=null;
					String strShipmentContainerKey=XMLUtil.getAttributeFromXPath(docContainerListOut, "//Containers/Container/@ShipmentContainerKey");
					//First tracking update received
					log.verbose("ExtnTrackingStatus"+strExtnTrackingStatus);
					log.debug("Shipment Container Key"+strShipmentContainerKey);
					
					// Start : OMNI-109332 : Handling In Flight Orders with Convey Enabled
					isTrackingUpdateIgnored = checkIfTrackingUpdateIsIgnored(strCarrierConveyStartDate, strShipmentContainerKey);
							
					if(YFCObject.isVoid(strExtnTrackingStatus) && !isTrackingUpdateIgnored) 
					{
						//update the status
						log.debug("First Tracking update");
						prepareAndInvokeChangeShipmentForExtnTrackingStatus(env,strShipmentKey,strShipmentContainerKey,strStatusDesc);
					}
					else if (!YFCObject.isVoid(strExtnTrackingStatus) && !isTrackingUpdateIgnored) 
					{
					Document docAcademygetCarrierLookupIn=XMLUtil.createDocument("AcadCarrierStatusLookup");
					Element eleAcadCarrierStatusLkup=docAcademygetCarrierLookupIn.getDocumentElement();
					eleAcadCarrierStatusLkup.setAttribute("StatusDescription", strExtnTrackingStatus);
					eleAcadCarrierStatusLkup.setAttribute("SCAC", strScac);
					log.debug("Input to AcademyGetCarrierLookupListForTrackingStatus "+XMLUtil.getXMLString(docAcademygetCarrierLookupIn));			       
			        Document docAcademygetCarrierLkupOut=AcademyUtil.invokeService(env,"AcademyGetCarrierLookupListForTrackingStatus", docAcademygetCarrierLookupIn) ;
			        log.debug("Output to AcademyGetCarrierLookupListForTrackingStatus "+XMLUtil.getXMLString(docAcademygetCarrierLkupOut));			        
			        String strCurrentMasterStatus=XMLUtil.getAttributeFromXPath(docAcademygetCarrierLkupOut, "//AcadCarrierStatusLookupList/AcadCarrierStatusLookup/@MasterStatus");
					
					//Start: Changes for the Picked Up Status update for STS2.0
					Integer int1=0;
			        if(!YFCObject.isVoid(strCurrentMasterStatus)) {
						String[] strCurrentMasterStatusSplit = strCurrentMasterStatus.split("\\.");
						if(strCurrentMasterStatusSplit.length > 1) {
							int1=Integer.parseInt(strCurrentMasterStatusSplit[2]);
						}
			        }					
			        //End: Changes for the Picked Up Status update for STS2.0
					
					log.debug("int1"+int1);
					if (strMsterStatus != null) {
						String[] strMsterStatusSplit = strMsterStatus.split("\\.");
						Integer int2 = Integer.parseInt(strMsterStatusSplit[2]);
						log.debug("int2" + int2);
						if (int2 > int1) {
							prepareAndInvokeChangeShipmentForExtnTrackingStatus(env, strShipmentKey,
									strShipmentContainerKey, strStatusDesc);
						}
					}
					}
					// End : OMNI-109332 : Handling In Flight Orders with Convey Enabled

					//OMNI-109225 Start
					carrierTrackingUpdatesForSTS(env, inDoc, strStatus, strScac, isTrackingUpdateIgnored,
							strCurrentShipmentStatus, isDeliveryUpdate);
					//OMNI-109225 End
					
					return false;
					
				}
				
				//OMNI-101569 End
				// OMNI-437 - Begin
				

				if (!StringUtil.isEmpty(strTrackingNo) && StringUtil.isEmpty(strScac) && !StringUtil.isEmpty(strUSPSRAI)
						&& !StringUtil.isEmpty(strUSPSRAILength)) {

					log.debug("TrackingNo:: " + strTrackingNo);
					log.debug("USPS_RouteApplicationIdentifier::" + strUSPSRAI);
					log.debug("USPS_RouteApplicationIdentifier_Length::" + strUSPSRAILength);

					String strSubTrackingNo = strTrackingNo.substring(0, Integer.parseInt(strUSPSRAILength));

					if (strUSPSRAI.contains(strSubTrackingNo)) {
						String strDigitsToBeTrimmed = (String) propMap
								.get(AcademyConstants.STR_USPS_TRIM_ZIPCODE_DIGITS_1);
						String strNewTrackingNo = strTrackingNo.substring(Integer.parseInt(strDigitsToBeTrimmed));

						log.debug("Correct TrackingNo (From : Condition_1):: " + strNewTrackingNo);

						inpEle.setAttribute(AcademyConstants.ATTR_TRACKING_NO, strNewTrackingNo);
						strScac = getScacForCarrierUpdates(env, inDoc);

						if (StringUtil.isEmpty(strScac)) {
							strDigitsToBeTrimmed = (String) propMap
									.get(AcademyConstants.STR_USPS_TRIM_ZIPCODE_DIGITS_2);
							strNewTrackingNo = strTrackingNo.substring(Integer.parseInt(strDigitsToBeTrimmed));

							log.debug("Correct TrackingNo  (From : Condition_2):: " + strNewTrackingNo);

							inpEle.setAttribute(AcademyConstants.ATTR_TRACKING_NO, strNewTrackingNo);
							strScac = getScacForCarrierUpdates(env, inDoc);
						}

						if (!StringUtil.isEmpty(strScac)) {							
							inpEle.setAttribute(AcademyConstants.ATTR_PRO_NO, strTrackingNo);
						} else {
							inpEle.setAttribute(AcademyConstants.ATTR_TRACKING_NO, strTrackingNo);
						}
						
						log.debug("Modified Input:: " + XMLUtil.getXMLString(inDoc));
					}
				}
				// OMNI-437 - End
				
				// Begin : OMNI-42367

				/*
				 * Code changes to consume return order (WCS Return Orders) carrier updates, if
				 * corresponding common code switch is enabled.
				 */

				Document docReturnCarrierUpdate = returnCarrierUpdates(env, AcademyConstants.STR_RET_CARRIER_UPDATES,
						AcademyConstants.PRIMARY_ENTERPRISE);

				Node nlReturnCarrierUpdatesCC = XPathUtil.getNode(docReturnCarrierUpdate,
						AcademyConstants.ELE_COMMON_CODE_LIST);
				String strReturnStatusCode = XPathUtil.getString(nlReturnCarrierUpdatesCC,
						AcademyConstants.XPATH_RETURN_STATUS);
				String strReturnOrderUpdateFlag = XPathUtil.getString(nlReturnCarrierUpdatesCC,
						AcademyConstants.XPATH_RETURN_FLAG);
				String strEligibleReturnOrderStatuses = XPathUtil.getString(nlReturnCarrierUpdatesCC,
						AcademyConstants.XPATH_RETURN_ORDER_STATUS);
				/*
				 * when, PROCESS_CARRIER_UPDATE = Y & input Status = CARRIER_STATUS_CODE &
				 * RETURN ORDER MAX STATUS = ELIGIBLE_RETURN_ORDER_STATUS
				 * 
				 * ACAD_TRACKING_UPDATES table will be updated to receive the return order.
				 */
				if (!StringUtil.isEmpty(strTrackingNo) && StringUtil.isEmpty(strScac)
						&& AcademyConstants.STR_YES.equalsIgnoreCase(strReturnOrderUpdateFlag)
						&& strStatus.equalsIgnoreCase(strReturnStatusCode)) {

					log.verbose("Receive Return logic is enabled :: ");

					Document docGetOrderListOp = getReturnOrderDetailsUsingTrackingNo(env, strTrackingNo);

					if (!YFCObject.isNull(docGetOrderListOp)) {

						Element eleOrder = (Element) docGetOrderListOp.getElementsByTagName(AcademyConstants.ELE_ORDER)
								.item(0);

						if (!YFCObject.isNull(eleOrder)) {

							String strMaxOrderStatus = eleOrder.getAttribute(AcademyConstants.ATTR_MAX_ORDER_STATUS);
							Element eleOrdExtn = XmlUtils.getChildElement(eleOrder, AcademyConstants.ELE_EXTN);
							String strIsWebOrder = null;

							if (!YFCObject.isNull(eleOrdExtn)) {
								strIsWebOrder = eleOrdExtn.getAttribute(AcademyConstants.ATTR_EXTN_IS_WEBORDER);
							}

							if (AcademyConstants.STR_YES.equals(strIsWebOrder)
									&& strEligibleReturnOrderStatuses.contains(strMaxOrderStatus)) {

								log.verbose("Creating Acad Tracking Updates entry to receive the return :: ");
								/*
								 * <!-- AcademyCreateTrackingUpdates --> <AcadTrackingUpdates TrackingNo=""
								 * Status="RETURN_RECEIVE" IsProcessed="N" StatusDescription="" StatusDate=""/>
								 */

								Document docAcadTrackingUpdatesIp = XMLUtil
										.createDocument(AcademyConstants.ELE_ACAD_TRACKING_UPDATES);
								Element eleAcadTrackingUpdatesIp = docAcadTrackingUpdatesIp.getDocumentElement();
								eleAcadTrackingUpdatesIp.setAttribute(AcademyConstants.ATTR_TRACKING_NO, strTrackingNo);
								eleAcadTrackingUpdatesIp.setAttribute(AcademyConstants.ATTR_STATUS,
										AcademyConstants.STR_RETURN_RECEIVE);
								eleAcadTrackingUpdatesIp.setAttribute(AcademyConstants.ATTR_STATUS_DESCRIPTION,
										strStatus);
								eleAcadTrackingUpdatesIp.setAttribute(AcademyConstants.ATTR_STATUS_DATE,
										inpEle.getAttribute(AcademyConstants.ATTR_STATUS_DATE));
								eleAcadTrackingUpdatesIp.setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED,
										AcademyConstants.STR_NO);

								log.verbose("Input to AcademyCreateTrackingUpdates: \n"
										+ XMLUtil.getXMLString(docAcadTrackingUpdatesIp));

								AcademyUtil.invokeService(env, AcademyConstants.ACAD_CREATE_TRACKING_UPDATES,
										docAcadTrackingUpdatesIp);
								return isDeliveryUpdate;
							}
						}
					}

				}
				// End : OMNI-42367
				
				if(!YFCObject.isNull(strStatus) && !YFCObject.isVoid(strStatus)
						&& !YFCObject.isNull(strScac) && !YFCObject.isVoid(strScac)){
					
					Document carrierStatusLookupDoc = AcademyCarrierStatusLookup
						.getCarrierStatusDetails(env, strStatus.trim(), strScac.trim());
					
					if(!YFCObject.isNull(carrierStatusLookupDoc) && 
							!YFCObject.isVoid(carrierStatusLookupDoc)){
						
						log.debug("Output from API::"+XMLUtil.getXMLString(carrierStatusLookupDoc));
						String strMasterStatus = carrierStatusLookupDoc.getDocumentElement()
							.getAttribute(AcademyConstants.ATTR_MASTER_STATUS);
						
						//OMNI-92660 Start: Skip Delivered call if Order line status is >= 3700.01(Return)
						Document getShipmentListOutDoc = prepareAndInvokeGetShipmentList(env,inDoc);
						log.debug("getShipmentListOutDoc is ::"+XMLUtil.getXMLString(getShipmentListOutDoc));
						
						if(null != getShipmentListOutDoc) {
						Element eleShipments = getShipmentListOutDoc.getDocumentElement();
						Element eleShipment = SCXmlUtil.getChildElement(eleShipments, AcademyConstants.ELE_SHIPMENT);
						Element eleShipmentLines = SCXmlUtil.getChildElement(eleShipment, AcademyConstants.ELE_SHIPMENT_LINES);
						Element eleShipmentLine = SCXmlUtil.getChildElement(eleShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE);
						log.debug("eleShipmentLine is ::"+XMLUtil.getElementXMLString(eleShipmentLine));
						NodeList nlOrderLine = eleShipmentLine.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
						for (int i = 0; i < nlOrderLine.getLength(); i++) {
							Element eleOrderLine = (Element) nlOrderLine.item(i);
							String strMaxLineStatus = eleOrderLine.getAttribute(AcademyConstants.ATTR_MAXLINE_STATUS);
							double dbMaxLineStatus = Double.parseDouble(strMaxLineStatus);
							System.out.println("dbMaxLineStatus is ::" + dbMaxLineStatus);
							if ((dbMaxLineStatus > 3700)) {
								return isDeliveryUpdate;
								}
							}
						}
						//OMNI-92660 END: Skip Delivered call if Order line status is >= 3700.01(Return)
						
						// Start : OMNI-109332 : Handling In Flight Orders with Convey Enabled
						String strShipmentContainerKey = inDoc.getDocumentElement()
								.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY);
						isTrackingUpdateIgnored = checkIfTrackingUpdateIsIgnored(strCarrierConveyStartDate,
								strShipmentContainerKey);
						// End : OMNI-109332 : Handling In Flight Orders with Convey Enabled
						
						//Start : OMNI-81897 : Skipping duplicate carrier delivery updates
						if(!YFCObject.isVoid(strMasterStatus) 
								&& !YFCObject.isNull(strMasterStatus)
								&&  !AcademyConstants.STATUS_SHIPMENT_DELIVERED
								.equals(strCurrentShipmentStatus)
								&&  !AcademyConstants.STATUS_SHIPMENT_CANCELLED
								.equals(strCurrentShipmentStatus)
								&& AcademyConstants.STATUS_SHIPMENT_DELIVERED
									.equals(strMasterStatus.trim())){
							//End : OMNI-81897 : Skipping duplicate carrier delivery updates

							// Start : OMNI-109332 : Handling In Flight Orders with Convey Enabled
							if (isTrackingUpdateIgnored) {
								isDeliveryUpdate = false;
							} else {
								isDeliveryUpdate = true;
							}
							// End : OMNI-109332 : Handling In Flight Orders with Convey Enabled
							
							Document cloneDoc = XMLUtil.cloneDocument(inDoc);
							String strStatusCarrier = inDoc.getDocumentElement()
								.getAttribute(AcademyConstants.ATTR_STATUS);
							inDoc.getDocumentElement().removeAttribute(AcademyConstants.ATTR_STATUS);
							String strStatusDate = inDoc.getDocumentElement()
								.getAttribute(AcademyConstants.ATTR_STATUS_DATE);
							
							if(!YFCObject.isNull(strStatusDate) &&
									!YFCObject.isVoid(strStatusDate)){
								inDoc.getDocumentElement().setAttribute("DeliveryDate", strStatusDate);			
							}
							inDoc.getDocumentElement().removeAttribute(AcademyConstants.ATTR_STATUS_DATE);
							//OMNI-437 : Begin
							inDoc.getDocumentElement().removeAttribute(AcademyConstants.ATTR_PRO_NO);
							//OMNI-437 : End
							log.debug("It is Delivery update from carrier ####");
							log.debug("Input Doc after modification: "+XMLUtil.getXMLString(inDoc));
														
							cloneDoc.getDocumentElement()
								.setAttribute(AcademyConstants.ATTR_STATUS, strMasterStatus);
							// Start : OMNI-109332 : Handling In Flight Orders with Convey Enabled
							if (isTrackingUpdateIgnored) {
								cloneDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED,
										AcademyConstants.STR_IGNORED);
							} else {
								cloneDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED,
										AcademyConstants.STR_YES);
							}
							// End : OMNI-109332 : Handling In Flight Orders with Convey Enabled
							
							if(!YFCObject.isNull(strStatusCarrier) &&
									!YFCObject.isVoid(strStatusCarrier)){
								cloneDoc.getDocumentElement()
								.setAttribute(AcademyConstants.ATTR_STATUS_DESCRIPTION, strStatusCarrier);		
							}
							log.debug("Clone-Doc after modification: "+XMLUtil.getXMLString(cloneDoc));
							objCarrTracker.addCarrierStatusTracker(env, cloneDoc);
							
						}else if (!YFCObject.isVoid(strMasterStatus) 
								&& !YFCObject.isNull(strMasterStatus)
								&& !AcademyConstants.STATUS_SHIPMENT_DELIVERED
									.equals(strMasterStatus.trim())){
							String strStatusCarrier = inDoc.getDocumentElement()
								.getAttribute(AcademyConstants.ATTR_STATUS);							
							inDoc.getDocumentElement()
								.setAttribute(AcademyConstants.ATTR_STATUS, strMasterStatus);
							
							// Start : OMNI-109332 : Handling In Flight Orders with Convey Enabled
							if (isTrackingUpdateIgnored) {
								inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED,
										AcademyConstants.STR_IGNORED);
							} else {
								inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED,
										AcademyConstants.STR_NO);
							}
							// End : OMNI-109332 : Handling In Flight Orders with Convey Enabled
							
							if(!YFCObject.isNull(strStatusCarrier) &&
									!YFCObject.isVoid(strStatusCarrier)){
								inDoc.getDocumentElement()
								.setAttribute(AcademyConstants.ATTR_STATUS_DESCRIPTION, strStatusCarrier);		
							}
							objCarrTracker.addCarrierStatusTracker(env, inDoc);
							
						}else{
							inDoc.getDocumentElement()
								.setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED, 
									AcademyConstants.STR_IGNORED);
							String strStatusCarrier = inDoc.getDocumentElement()
								.getAttribute(AcademyConstants.ATTR_STATUS);
							
							inDoc.getDocumentElement()
								.removeAttribute(AcademyConstants.ATTR_STATUS);
							
							if(!YFCObject.isNull(strStatusCarrier) &&
									!YFCObject.isVoid(strStatusCarrier)){
								inDoc.getDocumentElement()
								.setAttribute(AcademyConstants.ATTR_STATUS_DESCRIPTION, strStatusCarrier);		
							}						
							objCarrTracker.addCarrierStatusTracker(env, inDoc);
						}
					}else{
						inDoc.getDocumentElement()
							.setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED, 
									AcademyConstants.STR_IGNORED);
						String strStatusCarrier = inDoc.getDocumentElement()
							.getAttribute(AcademyConstants.ATTR_STATUS);
						
						inDoc.getDocumentElement()
							.removeAttribute(AcademyConstants.ATTR_STATUS);
						
						if(!YFCObject.isNull(strStatusCarrier) &&
								!YFCObject.isVoid(strStatusCarrier)){
							inDoc.getDocumentElement()
								.setAttribute(AcademyConstants.ATTR_STATUS_DESCRIPTION, strStatusCarrier);		
						}						
						objCarrTracker.addCarrierStatusTracker(env, inDoc);
					}
				}else{
					
					log.debug("SCAC or STATUS not found");
					
					inDoc.getDocumentElement()
					.setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED, 
							AcademyConstants.STR_IGNORED);
					String strStatusCarrier = inDoc.getDocumentElement()
						.getAttribute(AcademyConstants.ATTR_STATUS);
				
					inDoc.getDocumentElement()
						.removeAttribute(AcademyConstants.ATTR_STATUS);
				
					if(!YFCObject.isNull(strStatusCarrier) &&
						!YFCObject.isVoid(strStatusCarrier)){
						inDoc.getDocumentElement()
							.setAttribute(AcademyConstants.ATTR_STATUS_DESCRIPTION, strStatusCarrier);		
				}						
				objCarrTracker.addCarrierStatusTracker(env, inDoc);
					
					
				}
			}else{
				
				log.debug("Removing 'Status' attribute. By Default Delivered Update flow is considered ####");
				inDoc.getDocumentElement().removeAttribute(AcademyConstants.ATTR_STATUS);
				String strStatusDate = inDoc.getDocumentElement()
					.getAttribute(AcademyConstants.ATTR_STATUS_DATE);
			
				if(!YFCObject.isNull(strStatusDate) &&
						!YFCObject.isVoid(strStatusDate)){
					inDoc.getDocumentElement().setAttribute("DeliveryDate", strStatusDate);			
				}
				log.debug("Input Doc after modification: "+XMLUtil.getXMLString(inDoc));
					// Start : OMNI-109332 : Handling In Flight Orders with Convey Enabled
				if (!YFCObject.isVoid(strCarrierConveyStartDate)) {
					isDeliveryUpdate = false;
				} else {
					isDeliveryUpdate = true;
				}
				// End : OMNI-109332 : Handling In Flight Orders with Convey Enabled
			}
			
		}catch(Exception e){
			e.printStackTrace();
			log.error("Error while evaluating dynamic condition in " +
					"AcademyIsDeliveredUpdate.evaluateCondition() is:  "+e);
		}
		return isDeliveryUpdate;
	}
	//OMNI-101569 Begin
	private void prepareAndInvokeChangeShipmentForExtnTrackingStatus(YFSEnvironment env, String strShipmentKey,
			String strShipmentContainerKey, String strStatusDesc) throws Exception {
		log.debug("Inside prepareAndInvokeChangeShipmentForExtnTrackingStatus" );
		// TODO Auto-generated method stub
		try {
			Document docChangeShipmentIn=XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			//Document docChangeShipmentOut=null;
			Element eleShipment=docChangeShipmentIn.getDocumentElement();
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			Element eleContainers=docChangeShipmentIn.createElement("Containers");
			Element eleContainer=docChangeShipmentIn.createElement("Container");
			eleContainer.setAttribute("ShipmentContainerKey", strShipmentContainerKey);
			Element eleExtn=docChangeShipmentIn.createElement("Extn");
			eleExtn.setAttribute("ExtnTrackingStatus", strStatusDesc);
			eleContainer.appendChild(eleExtn);
			eleContainers.appendChild(eleContainer);
			eleShipment.appendChild(eleContainers);
			log.verbose("Change Shipment for Tracking Status update Input : :"+XMLUtil.getXMLString(docChangeShipmentIn));
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT,docChangeShipmentIn);

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			
			log.error(e);
		}
	}

	private Document getExtnTrackingStatusForContainer(YFSEnvironment env, String  strTrackingNo) {
		// TODO Auto-generated method stub
		Document docOutput=null;
		Document docInput=null;
		log.verbose("Inside getExtnTrackingStatusForContainer " + strTrackingNo);
		try
		{
			docInput=XMLUtil.createDocument(AcademyConstants.ATTR_CONTAINER);
			Element eleContainer=docInput.getDocumentElement();
			eleContainer.setAttribute(AcademyConstants.ATTR_TRACKING_NO, strTrackingNo);
			if (!YFCObject.isNull(docInput)) {
				log.verbose("getShipmentContainerList Input"+docInput);
				Document docGetContainerListTemplate = XMLUtil.getDocument(
						"<Containers><Container ContainerNo='' ShipmentContainerKey=''><Extn ExtnTrackingStatus=''/></Container></Containers>");

				env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST, docGetContainerListTemplate);

				docOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST, docInput);

				log.verbose("getContainerList For Tracking Status output :: " + XMLUtil.getXMLString(docOutput));

				env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST);
			}
			
		}
		catch(Exception e)
		{
			log.error("Exception caught is ::"+e);
		}
		
		return docOutput;
	}
//OMNI-101569 End
	private String getScacForCarrierUpdates (YFSEnvironment env, Document docInput){
		
		String strShipmentScac = "";
		
		try{
			Element inEle = docInput.getDocumentElement();
			String strShipmentNo = inEle.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			String strTrackingNo = inEle.getAttribute(AcademyConstants.ATTR_TRACKING_NO);			
			Document getShipmentListInDoc = null;
			Document getShipmentListOutDoc = null;
			//Create input for getShipmentList
			
			
			if(!YFCObject.isNull(strShipmentNo) 
					&& !YFCObject.isVoid(strShipmentNo)){
				getShipmentListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
				getShipmentListInDoc.getDocumentElement()
					.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
			}else{
				if(!YFCObject.isNull(strTrackingNo) 
						&& !YFCObject.isVoid(strTrackingNo)){
					
					getShipmentListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);					
					Element containersEle = getShipmentListInDoc
						.createElement(AcademyConstants.ELE_CONTAINERS);
					getShipmentListInDoc.getDocumentElement().appendChild(containersEle);
					Element containerEle = getShipmentListInDoc
						.createElement(AcademyConstants.ELE_CONTAINER);
					containerEle.setAttribute(AcademyConstants.ATTR_TRACKING_NO, strTrackingNo);
					containersEle.appendChild(containerEle);
				}
			}
			
			if(!YFCObject.isNull(getShipmentListInDoc)){
				
				log.debug("Input to API::"+XMLUtil.getXMLString(getShipmentListInDoc));
				Document outputTemplate = YFCDocument.getDocumentFor("<Shipments> <Shipment SCAC=''/> </Shipments>").getDocument();
				env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, outputTemplate);
				getShipmentListOutDoc = AcademyUtil.invokeAPI(env,
						AcademyConstants.API_GET_SHIPMENT_LIST, getShipmentListInDoc);
			}
			
			if(!YFCObject.isNull(getShipmentListOutDoc)){
				
				strShipmentScac = XMLUtil.getAttributeFromXPath(getShipmentListOutDoc, 
						"//Shipments/Shipment/@SCAC");
				
				if(!YFCObject.isNull(strShipmentScac) &&
						!YFCObject.isVoid(strShipmentScac)){
					
					if(AcademyConstants.STR_USPS.
							equalsIgnoreCase(strShipmentScac.substring(0, 4))){
						strShipmentScac = "USPS";
					}
				}
				
				log.debug("Shipment SCAC is ::"+strShipmentScac);
			}
			
		}catch(Exception e){
			log.error("Exception caught is ::"+e);
		}
		
		return strShipmentScac;
		
	}
	
	// Begin : OMNI-42367
	public Document returnCarrierUpdates(YFSEnvironment env, String strCodeType, String strOrganizationCode)
			throws Exception {

		log.verbose("AcademyIsDeliveredUpdate.returnCarrierUpdates() :: Begin");

		Document docCommonCodeListIn = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		docCommonCodeListIn.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, strCodeType);
		docCommonCodeListIn.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE, strOrganizationCode);

		Document getCommonCodeListOPTempl = XMLUtil.getDocument("<CommonCode CodeValue='' CodeShortDescription=''/>");
		env.setApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST, getCommonCodeListOPTempl);

		log.verbose("Input - getCommonCodeList API :: " + XMLUtil.getXMLString(docCommonCodeListIn));

		Document docCommonCodeListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST,
				docCommonCodeListIn);
		env.clearApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST);

		if (null != docCommonCodeListOut) {

			log.verbose("Output - getCommonCodeList API :: " + XMLUtil.getXMLString(docCommonCodeListOut));
		}

		log.verbose("AcademyIsDeliveredUpdate.returnCarrierUpdates() :: End");

		return docCommonCodeListOut;

	}

	private Document getReturnOrderDetailsUsingTrackingNo(YFSEnvironment env, String strTrackingNo) throws Exception {

		log.verbose("AcademyIsDeliveredUpdate.getReturnOrderDetailsUsingTrackingNo() :: Begin");

		Document docGetOrderListOut = null;
		Document docGetOrderListInp = prepareGetOrderListInp(strTrackingNo);

		if (!YFCObject.isNull(docGetOrderListInp)) {

			Document docGetOrderListTemplate = XMLUtil.getDocument(
					"<OrderList> <Order OrderNo='' DocumentType='' MaxOrderStatus='' > <Extn ExtnIsWebOrder='' /> </Order> </OrderList>");

			env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, docGetOrderListTemplate);

			docGetOrderListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, docGetOrderListInp);

			log.verbose("getOrderList output :: " + XMLUtil.getXMLString(docGetOrderListOut));

			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
		}

		log.verbose("AcademyIsDeliveredUpdate.getReturnOrderDetailsUsingTrackingNo() :: End");

		return docGetOrderListOut;
	}

	private Document prepareGetOrderListInp(String strTrackingNo) throws Exception {

		log.verbose("AcademyIsDeliveredUpdate.prepareGetOrderListInp() :: Begin");

		Document docGetOrderListInp = null;

		if (!StringUtil.isEmpty(strTrackingNo)) {

			docGetOrderListInp = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			Element eleOrder = docGetOrderListInp.getDocumentElement();
			eleOrder.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.STR_RETURN_DOCTYPE);
			Element eleExtn = docGetOrderListInp.createElement(AcademyConstants.ELE_EXTN);
			eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_TRACKING_NO, strTrackingNo);
			eleOrder.appendChild(eleExtn);

			log.verbose("getOrderList input :: " + XMLUtil.getXMLString(docGetOrderListInp));
		}

		log.verbose("AcademyIsDeliveredUpdate.prepareGetOrderListInp() :: End");

		return docGetOrderListInp;
	}
	// End : OMNI-42367

	// Code changes for OMNI-81897 : Start - Skipping duplicate carrier delivery updates
	/*
				Input to this method - <Shipment Status="DL" StatusDate="2019-09-10" TrackingNo="586766876627"/>
				Expected Output from this method - <Shipment>
											<Containers>
												<Container TrackingNo="586766876627"/>
											</Containers>
										</Shipment>
	*/

	private Document getScacAndShipmentStatusForCarrierUpdates (YFSEnvironment env, Document docInput){
		log.debug("getScacAndShipmentStatusForCarrierUpdates input :: " + XMLUtil.getXMLString(docInput));
		String strShipmentScac = null;
		Document getShipmentListOutDoc = null;
		String strDocumentType=null;
		String strShipmentKey=null;

		// Start : OMNI-109332 : Handling In Flight Orders with Convey Enabled
		String strShipmentContainerKey = null;
		// End : OMNI-109332 : Handling In Flight Orders with Convey Enabled
		
		try{
			//OMNI-92660 : Start : Call getShipmentList to retrieve maxLineStatus
			getShipmentListOutDoc = prepareAndInvokeGetShipmentList(env,docInput);
			//OMNI-92660 : End

			if(!YFCObject.isNull(getShipmentListOutDoc)){

				strShipmentScac = XMLUtil.getAttributeFromXPath(getShipmentListOutDoc, "//Shipments/Shipment/@SCAC");
				//OMNI-101569 Begin
				strDocumentType=XMLUtil.getAttributeFromXPath(getShipmentListOutDoc,"//Shipments/Shipment/@DocumentType" );
				strShipmentKey=XMLUtil.getAttributeFromXPath(getShipmentListOutDoc,"//Shipments/Shipment/@ShipmentKey" );
				//OMNI-101569 End

				// Start : OMNI-109332 : Handling In Flight Orders with Convey Enabled
				strShipmentContainerKey = XMLUtil.getAttributeFromXPath(getShipmentListOutDoc,
						"//Shipments/Shipment/Containers/Container/@ShipmentContainerKey");
				// End : OMNI-109332 : Handling In Flight Orders with Convey Enabled
				
				if(!YFCObject.isVoid(strShipmentScac)){
					if(AcademyConstants.STR_USPS.
							equalsIgnoreCase(strShipmentScac.substring(0, 4))){
						strShipmentScac = AcademyConstants.STR_USPS;
					}
				}
				docInput.getDocumentElement().setAttribute(AcademyConstants.ATT_CURR_SHP_STATS, 
						XMLUtil.getAttributeFromXPath(getShipmentListOutDoc, "//Shipments/Shipment/@Status"));
								//OMNI-101569 Begin
				if(!YFCObject.isVoid(strDocumentType)&&AcademyConstants.DOCUMENT_TYPE.equalsIgnoreCase(strDocumentType))
				{
				docInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE,strDocumentType);
				docInput.getDocumentElement().setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
				}

				log.debug("Shipment SCAC is ::"+strShipmentScac);
			}

		}catch(Exception e){
			log.error("Exception caught is::"+e);
		}

		docInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_SCAC, strShipmentScac);
		
		// Start : OMNI-109332 : Handling In Flight Orders with Convey Enabled
		docInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY, strShipmentContainerKey);
		// End : OMNI-109332 : Handling In Flight Orders with Convey Enabled
		
		log.debug("getScacAndShipmentStatusForCarrierUpdates output :: " + XMLUtil.getXMLString(docInput));

		return docInput;

	}
	
	//OMNI-92660 : Start : Added method to retrieve getShipmentList details
		private Document prepareAndInvokeGetShipmentList  (YFSEnvironment env, Document docInput) throws Exception{
			Element inEle = docInput.getDocumentElement();
			String strShipmentNo = inEle.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			String strTrackingNo = inEle.getAttribute(AcademyConstants.ATTR_TRACKING_NO);
			Document getShipmentListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			Document getShipmentListOutDoc = null;

			
			if(!YFCObject.isNull(strShipmentNo) 
					&& !YFCObject.isVoid(strShipmentNo)){
				getShipmentListInDoc.getDocumentElement()
				.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
			}else{
				if(!YFCObject.isNull(strTrackingNo) 
						&& !YFCObject.isVoid(strTrackingNo)){
					Element containersEle = getShipmentListInDoc
							.createElement(AcademyConstants.ELE_CONTAINERS);
					getShipmentListInDoc.getDocumentElement().appendChild(containersEle);
					Element containerEle = getShipmentListInDoc
							.createElement(AcademyConstants.ELE_CONTAINER);
					containerEle.setAttribute(AcademyConstants.ATTR_TRACKING_NO, strTrackingNo);
					containersEle.appendChild(containerEle);
				}
			}

			if(!YFCObject.isNull(getShipmentListInDoc)){

				log.debug("Input to API getShipmentList::"+XMLUtil.getXMLString(getShipmentListInDoc));
				
				// OMNI-92660, OMNI-109332 START: template changes
				Document outputTemplate = YFCDocument.getDocumentFor("<Shipments> \r\n"
							+ "<Shipment Status='' SCAC='' DocumentType='' ShipmentKey=''> \r\n"
							+ "<Containers><Container ShipmentContainerKey='' /></Containers>" + "<ShipmentLines>\r\n"
							+ "<ShipmentLine OrderHeaderKey='' OrderLineKey='' OrderNo=''>\r\n"
							+ "<OrderLine MaxLineStatus='' MaxLineStatusDesc=''>\r\n" + "</OrderLine>\r\n"
							+ "</ShipmentLine>\r\n" + "</ShipmentLines>\r\n" + "</Shipment>\r\n" + "</Shipments>").getDocument();
				//OMNI-92660, OMNI-109332 END: template changes
				try{
				env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, outputTemplate);
				getShipmentListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, getShipmentListInDoc);
				log.debug("Output of API getShipmentList::"+XMLUtil.getXMLString(getShipmentListOutDoc));
			}catch(Exception e){
				log.error("Exception caught ::"+e);
				
			}
			
			
		}
			return getShipmentListOutDoc;
		}
		//OMNI-92660 : End
		
	// Code changes for OMNI-81897 : End - Skipping duplicate carrier delivery updates

/**
	 * This method checks if tracking updates are to be ignored
	 * 
	 * @param strCarrierConveyStartDate
	 * @param strShipmentContainerKey
	 * @return boolean
	 * @throws Exception
	 */
	private boolean checkIfTrackingUpdateIsIgnored(String strCarrierConveyStartDate, String strShipmentContainerKey)
			throws Exception {
		log.beginTimer("AcademyIsDeliveredUpdate::checkIfTrackingUpdateIsIgnored");
		log.verbose(":strCarrierConveyStartDate :: " + strCarrierConveyStartDate + " : strShipmentContainerKey : "
				+ strShipmentContainerKey);

		boolean bIsTrackingUpdateIgnored = false;
		if (!YFCObject.isVoid(strCarrierConveyStartDate)) {
			String strContainerCreatedDate = strShipmentContainerKey.substring(0, 10);
			log.verbose(":strContainerCreatedDate :: " + strContainerCreatedDate);

			if (Long.valueOf(strCarrierConveyStartDate) < Long.valueOf(strContainerCreatedDate)) {
				log.verbose(" Shipment Container is eligible for skipping Convey updates ");
				bIsTrackingUpdateIgnored = true;
			}
		}

		log.verbose("Final bIsTrackingUpdateIgnored is " + bIsTrackingUpdateIgnored);
		log.endTimer("AcademyIsDeliveredUpdate::checkIfTrackingUpdateIsIgnored");
		return bIsTrackingUpdateIgnored;
	}

	private boolean carrierTrackingUpdatesForSTS(YFSEnvironment env, Document inDoc, String strStatus, String strScac,
			boolean isTrackingUpdateIgnored, String strCurrentShipmentStatus, boolean isDeliveryUpdate) throws Exception {
		
		log.debug("carrierTrackingUpdatesForSTS START: " + XMLUtil.getXMLString(inDoc));
		log.debug("strStatus: "+strStatus +" strScac: "+strScac +" isTrackingUpdateIgnored: " +isTrackingUpdateIgnored
			+" strCurrentShipmentStatus: "+strCurrentShipmentStatus +" isDeliveryUpdate: "+isDeliveryUpdate);
		Document carrierStatusLookupDoc = AcademyCarrierStatusLookup.getCarrierStatusDetails(env, strStatus.trim(),
				strScac.trim());
		
		if (!YFCObject.isNull(carrierStatusLookupDoc) && !YFCObject.isVoid(carrierStatusLookupDoc)) {

			log.debug("Output from getCarrierStatusDetails::" + XMLUtil.getXMLString(carrierStatusLookupDoc));
			String strMasterStatus = carrierStatusLookupDoc.getDocumentElement()
					.getAttribute(AcademyConstants.ATTR_MASTER_STATUS);
			log.debug("MasterStatus: "+strMasterStatus);
			// OMNI-92660 Start: Skip Delivered call if Order line status is >=3700.01(Return)
			Document getShipmentListOutDoc = prepareAndInvokeGetShipmentList(env, inDoc);
			log.debug("getShipmentListOutDoc is ::" + XMLUtil.getXMLString(getShipmentListOutDoc));

			if (null != getShipmentListOutDoc) {
				Element eleShipments = getShipmentListOutDoc.getDocumentElement();
				Element eleShipment = SCXmlUtil.getChildElement(eleShipments, AcademyConstants.ELE_SHIPMENT);
				Element eleShipmentLines = SCXmlUtil.getChildElement(eleShipment, AcademyConstants.ELE_SHIPMENT_LINES);
				Element eleShipmentLine = SCXmlUtil.getChildElement(eleShipmentLines,
						AcademyConstants.ELE_SHIPMENT_LINE);
				
				NodeList nlOrderLine = eleShipmentLine.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
					for (int i = 0; i < nlOrderLine.getLength(); i++) {
						Element eleOrderLine = (Element) nlOrderLine.item(i);
						String strMaxLineStatus = eleOrderLine.getAttribute(AcademyConstants.ATTR_MAXLINE_STATUS);
						double dbMaxLineStatus = Double.parseDouble(strMaxLineStatus);
						System.out.println("dbMaxLineStatus is ::" + dbMaxLineStatus);
						if ((dbMaxLineStatus > 3700.200)) {
							return isDeliveryUpdate;
							}
						}
			}
		// Start : OMNI-81897 : Skipping duplicate carrier delivery updates
			if (!YFCObject.isVoid(strMasterStatus) && !YFCObject.isNull(strMasterStatus)
					&& !AcademyConstants.STATUS_SHIPMENT_DELIVERED.equals(strCurrentShipmentStatus)
					&& !AcademyConstants.STATUS_SHIPMENT_CANCELLED.equals(strCurrentShipmentStatus)
					&& AcademyConstants.STATUS_SHIPMENT_DELIVERED.equals(strMasterStatus.trim())) {
		// End : OMNI-81897 : Skipping duplicate carrier delivery updates
		
				log.debug("MasterStatus is STATUS_SHIPMENT_DELIVERED ");
				
				
				Document cloneDoc = XMLUtil.cloneDocument(inDoc);
				String strStatusCarrier = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_STATUS);
				inDoc.getDocumentElement().removeAttribute(AcademyConstants.ATTR_STATUS);
				String strStatusDate = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_STATUS_DATE);

				if (!YFCObject.isNull(strStatusDate) && !YFCObject.isVoid(strStatusDate)) {
					inDoc.getDocumentElement().setAttribute("DeliveryDate", strStatusDate);
				}

				inDoc.getDocumentElement().removeAttribute(AcademyConstants.ATTR_STATUS_DATE);
				// OMNI-437 : Begin
				inDoc.getDocumentElement().removeAttribute(AcademyConstants.ATTR_PRO_NO);
				// OMNI-437 : End
				
				log.debug("It is Delivery update from carrier ####");
				log.debug("Input Doc after modification: " + XMLUtil.getXMLString(inDoc));
							
				cloneDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_STATUS, strMasterStatus);
				// Start : OMNI-109332 : Handling In Flight Orders with Convey Enabled
				if (isTrackingUpdateIgnored) {
					cloneDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED,
							AcademyConstants.STR_IGNORED);
				} else {
					cloneDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED,
							AcademyConstants.STR_YES);
				}
				// End : OMNI-109332 : Handling In Flight Orders with Convey Enabled

				if (!YFCObject.isNull(strStatusCarrier) && !YFCObject.isVoid(strStatusCarrier)) {
					cloneDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_STATUS_DESCRIPTION,
							strStatusCarrier);
				}
				log.debug("addCarrierStatusTracker inDoc: " + XMLUtil.getXMLString(cloneDoc));
				objCarrTracker.addCarrierStatusTracker(env, cloneDoc);

			} else if (!YFCObject.isVoid(strMasterStatus) && !YFCObject.isNull(strMasterStatus)
					&& !AcademyConstants.STATUS_SHIPMENT_DELIVERED.equals(strMasterStatus.trim())) {
				
				
				log.debug("MasterStatus is NOT STATUS_SHIPMENT_DELIVERED ");
				
				String strStatusCarrier = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_STATUS);
				inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_STATUS, strMasterStatus);
				// Start : OMNI-109332 : Handling In Flight Orders with Convey Enabled
				if (isTrackingUpdateIgnored) {
					inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED,
							AcademyConstants.STR_IGNORED);
				} else {
					inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED,
							AcademyConstants.STR_NO);
				}
				// End : OMNI-109332 : Handling In Flight Orders with Convey Enabled

				if (!YFCObject.isNull(strStatusCarrier) && !YFCObject.isVoid(strStatusCarrier)) {
					inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_STATUS_DESCRIPTION, strStatusCarrier);
				}
				
				log.debug("addCarrierStatusTracker inDoc: " + XMLUtil.getXMLString(inDoc));
				objCarrTracker.addCarrierStatusTracker(env, inDoc);

			} else {
				
				inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED,
						AcademyConstants.STR_IGNORED);
				String strStatusCarrier = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_STATUS);

				inDoc.getDocumentElement().removeAttribute(AcademyConstants.ATTR_STATUS);

				if (!YFCObject.isNull(strStatusCarrier) && !YFCObject.isVoid(strStatusCarrier)) {
					inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_STATUS_DESCRIPTION, strStatusCarrier);
				}
				
				log.debug("addCarrierStatusTracker inDoc: " + XMLUtil.getXMLString(inDoc));
				objCarrTracker.addCarrierStatusTracker(env, inDoc);
			}
		}
		
		log.debug("**** carrierTrackingUpdatesForSTS END**** ");
		return isDeliveryUpdate;
	}

}
