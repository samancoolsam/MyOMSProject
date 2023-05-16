package com.academy.ecommerce.sterling.bopis.order.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @author neeladha
 *
 */
/*
 * Sample Input to the class
 * <Order Action="CANCEL" DocumentType="0001" EnterpriseCode="Academy_Direct" OrderNo="201805230006" ReasonCode="CHANGE" />
 * 
 * Sample Line Level Cancellation Input to the class OMNI-30152 Line level cancellation enabled for STS lines.
 * 
 * <Order OrderNo="155760053" DocumentType="0001" CancelledBy="WCS" CancellingSystem="WCS" Action="CANCEL" ReasonCode="Customer Requested-WCS"  IsLineLevelCancellation="Y"> 
 *   <OrderLines>
 *          <OrderLine ItemID="122704698" ReasonCode="Customer Requested" >
 *                     <Extn ExtnWCOrderItemIdentifier="2203051402" />
 *          </OrderLine>
 *   </OrderLines>
 * </Order>
 * */
public class AcademyOrderCancellationFromWCS {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyOrderCancellationFromWCS.class);
	private Properties props;
	//OMNI-84839 
	private static CopyOnWriteArrayList<String> listWCSCSCancellationReasonCode  = new CopyOnWriteArrayList<String>();
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	public Document processCompleteOrderCancellation(YFSEnvironment env, Document inXML) throws Exception {
		log.beginTimer("AcademyOrderCancellationFromWCS::processCompleteOrderCancellation");
		log.verbose("Entering the method AcademyOrderCancellationFromWCS.processCompleteOrderCancellation ");

		log.verbose("Input to the class AcademyOrderCancellationFromWCS " + XMLUtil.getXMLString(inXML));
		Document returndoc = null;
		Document docchangeOrderInput = null;
		YFSException e = new YFSException();
		String errorDescription = null;
		boolean bmandatErrorCode = false;
		boolean bAlreadyCancelledOrShippedORPickedErrorCode = false;
		Boolean bchangeOrder = false;
		Boolean bchangeShipmentOrder = false;
		boolean bIsStoreCancellation = false;
		boolean bIsLineCancellation = false;
		boolean bLineAlreadyCancelledOrShipped = false; // OMNI-30152
		boolean bIsMultiCancelAllowed = false; //OMNI-69803
		boolean bIsDSVChubPOCancellationCall= false;
		YFCException yfce = new YFCException("Error while cancelling the order");

		try {

			if (!YFCObject.isVoid(inXML)) {
				Document docgetOrderListOutput = null;
				Document docgetShipNodeListInput = null;
				Document docgetShipmentListForOrderOutput = null;
				Document docgetShipNodeListOutput = null;

				Element elechangeOrderOrderLines = null;

				Document docCommonCodeForCancellationstatus = null;
				Element eleRootchangeOrderInput = null;
				Element eleOrder = null;
				Element elegetShipNodeListInput = null;
				String strOrderNo = null;
				String strAction = null;
				String strReasonCode = null;
				String strStatus = null;
				String strMinOrderStatus = null;
				String strMaxOrderStatus = null;
				String strNoteText = null;
				String strOrderHeaderKey=null;
				String strContactReference = null;
				String strContactUser = null;
				int iLinesToCancel =0;

				//OMNI-61630- START
				final String strMultiLineCancel = props.getProperty(AcademyConstants.MULTI_LINE_CNCL);
				log.verbose("Value of strMultiLineCancel is : " + strMultiLineCancel);								
				//OMNI-61630-END

				//OMNI-69803 - START	
				ArrayList<String> lstNotEligibleItems = new ArrayList<String>();
				String strItemNotEligible = null;	
				log.verbose("Value of strMultiLineCancel is : " + strMultiLineCancel);	
				if(!YFCObject.isNull(strMultiLineCancel) && !YFCObject.isVoid(strMultiLineCancel) && 	
						AcademyConstants.STR_YES.equalsIgnoreCase(strMultiLineCancel)) {	
					bIsMultiCancelAllowed = true;	
				}	
				log.verbose("Value of MultiLineCancellation Flag is : " + bIsMultiCancelAllowed);	
				//OMNI-69803 - END
				
				//OMNI-99051- START
				final String strDSVChubPOCancellationCall = props.getProperty(AcademyConstants.DSV_CHUB_PO_CNCL_CALL);
				log.verbose("Value of strDSVChubPOCancellationCall is : " + strDSVChubPOCancellationCall);
				
				if(!YFCObject.isNull(strDSVChubPOCancellationCall) && !YFCObject.isVoid(strDSVChubPOCancellationCall) && 	
						AcademyConstants.STR_YES.equalsIgnoreCase(strDSVChubPOCancellationCall)) {	
					bIsDSVChubPOCancellationCall = true;	
				}	
				
				//OMNI-99051- END

				strOrderNo = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO);
				strAction = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ACTION);
				strReasonCode = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_REASON_CODE);
				strContactUser = inXML.getDocumentElement().getAttribute(AcademyConstants.STR_CANCELLED_BY);
				strContactReference = inXML.getDocumentElement().getAttribute(AcademyConstants.STR_CANCELLING_SYSTEM);
				String strSourceID = inXML.getDocumentElement().getAttribute("SourceID");
				String strIsLineLevelCancellation = inXML.getDocumentElement().getAttribute("IsLineLevelCancellation"); //OMNI-30152
				//OMNI-95793 START
				if(AcademyConstants.TRANS_TYPE_WCS.equalsIgnoreCase(strContactReference)) {
					//OMNI-84839 START
					populateWCSResponseCodetaticMap(env);
					if(!YFCObject.isNull(strReasonCode) && !listWCSCSCancellationReasonCode.contains(strReasonCode)) {
						yfce.setAttribute("ErrorCode", "CAN0006");
						yfce.setErrorDescription("Invalid Reason Code");
						throw yfce;
					}
					//OMNI-84839 END
				}
				//OMNI-95793 END
				if (YFCCommon.equals("Webstore", strSourceID)) {
					bIsStoreCancellation = true;
				}
				//Start OMNI-30152 Getting IsLineLevelCancellation attribute from WCS Input XML 
				if(!YFCObject.isNull(strIsLineLevelCancellation) && !YFCObject.isVoid(strIsLineLevelCancellation) && 
						strIsLineLevelCancellation.equalsIgnoreCase(AcademyConstants.STR_YES)) {
					bIsLineCancellation = true;
				}
				//End OMNI-30152
				strNoteText = props.getProperty(AcademyConstants.ATTR_NOTE_TEXT);

				if (YFCObject.isVoid(strOrderNo) || YFCObject.isVoid(strAction) || YFCObject.isVoid(strReasonCode)
						|| YFCObject.isVoid(strContactUser) || YFCObject.isVoid(strContactReference)) {
					bmandatErrorCode = true;

				} else {

					log.verbose("Calling CallgetOrderListInputdoc method to invoke call getOrderList API : =");
					docgetOrderListOutput = CallgetOrderListInputdoc(env, strOrderNo);
					log.verbose("Output of AcademyCallgetOrderListForWCSCancellation service  "
							+ XMLUtil.getXMLString(docgetOrderListOutput));

					NodeList NLgetOrderList = docgetOrderListOutput.getElementsByTagName(AcademyConstants.ELE_ORDER);
					if (NLgetOrderList.getLength() <= 0) {
						log.verbose("It is an invalid Order");
						log.verbose("It is an invalid Order");
						errorDescription = "YFS:Invalid Order";
						e.setErrorCode("YFS10003");
						e.setErrorDescription(errorDescription);
						throw e;
					}

					//OMNI-69803 - START	
					NodeList nlGetOrderLineList = inXML.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);	
					if (nlGetOrderLineList.getLength() > 1 && !bIsMultiCancelAllowed) {	
						log.verbose("Multiline cancellation is not allowed");	
						errorDescription = "Multiline cancellation is not allowed";	
						e.setErrorCode("CAN005");	
						e.setErrorDescription(errorDescription);	
						throw e;	
					}	
					//OMNI-69803 - END

					eleOrder = (Element) docgetOrderListOutput.getDocumentElement()
							.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
					strOrderHeaderKey = eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
					strMaxOrderStatus = eleOrder.getAttribute(AcademyConstants.ATTR_MAX_ORDER_STATUS);
					strMinOrderStatus = eleOrder.getAttribute(AcademyConstants.ATTR_MIN_ORDER_STATUS);
					if (!YFCObject.isVoid(strMaxOrderStatus) && !YFCObject.isVoid(strMinOrderStatus)
							&& strMaxOrderStatus.equalsIgnoreCase(AcademyConstants.VAL_CANCELLED_STATUS)
							&& strMinOrderStatus.equalsIgnoreCase(AcademyConstants.VAL_CANCELLED_STATUS)) {
						log.verbose("Order is already Cancelled");
						// System.out.println("Order is already Cancelled");

						if(bIsLineCancellation) {
							bLineAlreadyCancelledOrShipped = true;
						} else {
							bAlreadyCancelledOrShippedORPickedErrorCode = true;
						}

					} else {

						docchangeOrderInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
						eleRootchangeOrderInput = docchangeOrderInput.getDocumentElement();
						eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
								AcademyConstants.SALES_DOCUMENT_TYPE);
						eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,
								AcademyConstants.PRIMARY_ENTERPRISE);
						eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
						eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_MOD_REASON_CODE, strReasonCode);
						eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
						if(!bIsLineCancellation) { //OMNI-30152
							log.verbose("Line Level Cancellation is false");
							eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_CANCEL);
						}else {
							eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY);
						}
						elechangeOrderOrderLines = docchangeOrderInput.createElement(AcademyConstants.ELE_ORDER_LINES);
						eleRootchangeOrderInput.appendChild(elechangeOrderOrderLines);
						// Start OMNI-30152
						NodeList NLOrderLine = null;
						if(bIsLineCancellation) {
							Document docCancelledOrderLines = getOrderLinesForCancellation(env,inXML,eleOrder);
							NLOrderLine = docCancelledOrderLines.getDocumentElement()
									.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
							iLinesToCancel = NLOrderLine.getLength();
							if(iLinesToCancel <= 0) {
								bmandatErrorCode = true ; 
							}
						}
						else {
							// End OMNI-30152

							NLOrderLine = docgetOrderListOutput.getDocumentElement()
									.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
						}

						for (int i = 0; i < NLOrderLine.getLength(); i++) {
							Element EleOrderLine = (Element) NLOrderLine.item(i);
							String strFulfillmentType = EleOrderLine
									.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
							// String strShipNode =
							// EleOrderLine.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
							String strOrderLinekey = EleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
							String strLineQty = EleOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY); //OMNI-30152
							
							Element eleItemDetails = SCXmlUtil.getChildElement(EleOrderLine, AcademyConstants.ELE_ITEM_DETAILS);	
							NodeList NLOrderStatus = EleOrderLine
									.getElementsByTagName(AcademyConstants.ELE_ORDER_STATUS);
							for (int j = 0; j < NLOrderStatus.getLength(); j++) {
								Element eleOrderStatus = (Element) NLOrderStatus.item(j);
								strStatus = eleOrderStatus.getAttribute(AcademyConstants.ATTR_STATUS);
								if (!YFCObject.isVoid(strStatus)
										&& strStatus.equalsIgnoreCase(AcademyConstants.VAL_CANCELLED_STATUS)) {
									// System.out.println("The Orderline"+strOrderLinekey+ "is in cancelled status
									// ");
									log.verbose("The Orderline" + strOrderLinekey + "is in cancelled status");
									log.verbose("strLineQty is "+strLineQty);
									Double dLineQty = Double.parseDouble(strLineQty);
									log.verbose("dLineQty is" + dLineQty);
									//OMNI-30152 Start
									if(bIsLineCancellation && dLineQty == 0.0) {
										bLineAlreadyCancelledOrShipped = true;
										lstNotEligibleItems.add(eleItemDetails.getAttribute(AcademyConstants.ITEM_ID));//OMNI-69803
									}
									//OMNI-30152 End
								} else if (!YFCObject.isVoid(strStatus)
										&& strStatus.equalsIgnoreCase(AcademyConstants.PICKED_BY_CUSTOMER)) {
									// Prepare the failure response
									// System.out.println("The Orderline"+strOrderLinekey+ "is in shipped status ");
									log.verbose("The Orderline" + strOrderLinekey + "is in shipped status ");

									if(bIsLineCancellation) {
										bLineAlreadyCancelledOrShipped = true;
										lstNotEligibleItems.add(eleItemDetails.getAttribute(AcademyConstants.ITEM_ID));//OMNI-69803
									} else {
										bAlreadyCancelledOrShippedORPickedErrorCode = true;
									}
									// System.out.println("failure response if
									// ");
									break;
								} else if(!YFCObject.isVoid(strStatus)  //OMNI-30152 Start
										&& strStatus.equalsIgnoreCase(AcademyConstants.BACKORDERED_FROM_NODE)) {
									/** 
									 * Added this condition to avoid creating multiple OrderLine element tags
									 * while the status is BackOrdered from Node when it was shorted from Store/DC
									 */
									log.verbose("The Orderline" + strOrderLinekey + "is Shorted From Store/DC ");
								}// OMNI-30152 End 

								else {

									String strCodeLongDesc = null;
									if (!YFCObject.isVoid(strFulfillmentType) && strFulfillmentType
											.equalsIgnoreCase(AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE)) {
										log.verbose("The OredrlineKey" + strOrderLinekey + "Is BOPIS");
										if (YFCObject.isVoid(docCommonCodeForCancellationstatus)) {
											docCommonCodeForCancellationstatus = CallCommonCodeAPI(env,
													AcademyConstants.STR_ORD_CANCELLATION_STATUS);
										}
										/* OMNI-53517 - Start */
										if(!bIsLineCancellation) {
											strCodeLongDesc = XPathUtil.getString(
													docCommonCodeForCancellationstatus.getDocumentElement(),
													AcademyConstants.XPATH_MAX_BOPIS_CANCEL_STATUS_CODE_LONG_DESC);
										}
										else {
											strCodeLongDesc = XPathUtil.getString(
													docCommonCodeForCancellationstatus.getDocumentElement(),
													AcademyConstants.XPATH_MAX_BOPIS_LINE_CANCEL_STATUS_CODE_LONG_DESC);
										}
										/* OMNI-53517 - End */

										if (!YFCObject.isVoid(strCodeLongDesc)) {
											Integer iCancellationAllowed = compareStatus(strCodeLongDesc, strStatus);
											if (iCancellationAllowed < 0) {
												if(bIsLineCancellation) {
													lstNotEligibleItems.add(eleItemDetails.getAttribute(AcademyConstants.ITEM_ID));//OMNI-69803
													bLineAlreadyCancelledOrShipped = true;
												} else {
													bAlreadyCancelledOrShippedORPickedErrorCode = true;
												}
											}

											else {

												bchangeOrder = true;
												Integer ihangeShipmentRequired = compareStatus(strStatus,
														AcademyConstants.STR_INCLUDE_IN_SHIPMENT_STATUS);
												if ((ihangeShipmentRequired == 0) || (ihangeShipmentRequired > 0)) {
													bchangeShipmentOrder = true;
												}
											}

										}

									} 
									//OMNI-6357 : BEGIN
									else if (!YFCObject.isVoid(strFulfillmentType) && strFulfillmentType
											.equalsIgnoreCase(AcademyConstants.STR_SHIP_TO_STORE)) {
										log.verbose("The OredrlineKey" + strOrderLinekey + "Is STS");
										if (YFCObject.isVoid(docCommonCodeForCancellationstatus)) {
											docCommonCodeForCancellationstatus = CallCommonCodeAPI(env,
													AcademyConstants.STR_ORD_CANCELLATION_STATUS);
										}
										/* OMNI-53517 - Start */
										if(bIsLineCancellation) {
											strCodeLongDesc = XPathUtil.getString(
													docCommonCodeForCancellationstatus.getDocumentElement(),
													AcademyConstants.XPATH_MAX_STS_LINE_CANCEL_STATUS_CODE_LONG_DESC);
										} else {
											strCodeLongDesc = XPathUtil.getString(
													docCommonCodeForCancellationstatus.getDocumentElement(),
													AcademyConstants.XPATH_MAX_STS_CANCEL_STATUS_CODE_LONG_DESC);
										}
										/* OMNI-53517 - End */
										if (!YFCObject.isVoid(strCodeLongDesc)) {
											Integer iCancellationAllowed = compareStatus(strCodeLongDesc, strStatus);
											if (iCancellationAllowed < 0) {
												if(bIsLineCancellation) {
													lstNotEligibleItems.add(eleItemDetails.getAttribute(AcademyConstants.ITEM_ID));//OMNI-69803
													bLineAlreadyCancelledOrShipped = true;
												} else {
													bAlreadyCancelledOrShippedORPickedErrorCode = true;
												}
											}

											else {

												bchangeOrder = true;
												Integer ihangeShipmentRequired = compareStatus(strStatus,
														AcademyConstants.STR_INCLUDE_IN_SHIPMENT_STATUS);
												if ((ihangeShipmentRequired == 0) || (ihangeShipmentRequired > 0)) {
													bchangeShipmentOrder = true;
												}
												/**
												 * OMNI-30152 Start
												 * This method is to check the TO Line corresponding to Sales Line
												 * is eligible for Cancellation or not.If not eligible we will throw
												 * the error 
												 * STS_SO_LINE_CANCEL common code has the status Not eligible for Cancellation
												 */
												if(bIsLineCancellation) {

													Document docCommonCodeForCancellableLinestatus = CallCommonCodeAPI(env,
															AcademyConstants.STR_STS_SO_LINE_CANCEL);

													List<String> alSOStatus = getStatusList(docCommonCodeForCancellableLinestatus);

													if(alSOStatus.contains(strStatus)) {
														lstNotEligibleItems.add(eleItemDetails.getAttribute(AcademyConstants.ITEM_ID));//OMNI-69803
														bLineAlreadyCancelledOrShipped = true;
													}
													else {
														if (strStatus.substring(0, 4).equals("2160")) {
															bchangeOrder = ValidateSTSLineEligibleForCancel(env,
																	strOrderLinekey, bIsMultiCancelAllowed); //OMNI-69805

															if (!bchangeOrder) {
																lstNotEligibleItems.add(eleItemDetails.getAttribute(AcademyConstants.ITEM_ID));
																bLineAlreadyCancelledOrShipped = true;
															}
														}
													}
												}
												/**OMNI-30152 END */
											}

										}

									}
									//OMNI-6357 : END
									else if (!YFCObject.isVoid(strFulfillmentType) && strFulfillmentType
											.equalsIgnoreCase(AcademyConstants.STR_SPECIAL_ORDER_FIREARMS)) {
										log.verbose("The OrderlineKey" + strOrderLinekey + "Is SOF");
										if (YFCObject.isVoid(docCommonCodeForCancellationstatus)) {
											docCommonCodeForCancellationstatus = CallCommonCodeAPI(env,
													AcademyConstants.STR_ORD_CANCELLATION_STATUS);
										}
										
										if(bIsLineCancellation) {
											strCodeLongDesc = XPathUtil.getString(
													docCommonCodeForCancellationstatus.getDocumentElement(),
													"//CommonCodeList/CommonCode[@CodeValue='MAX_SOF_LINE_CANCEL_STATUS']/@CodeLongDescription");
										}
										else {
											strCodeLongDesc = XPathUtil.getString(
													docCommonCodeForCancellationstatus.getDocumentElement(),
													AcademyConstants.XPATH_MAX_SOF_CANCEL_STATUS_CODE_LONG_DESC);
										}
										
										
										if (!YFCObject.isVoid(strCodeLongDesc)) {
											Integer iCancellationAllowed = compareStatus(strCodeLongDesc, strStatus);
											if (iCancellationAllowed < 0) {
												bAlreadyCancelledOrShippedORPickedErrorCode = true;
											}

											else {

												bchangeOrder = true;
												Integer ihangeShipmentRequired = compareStatus(strStatus,
														AcademyConstants.STR_INCLUDE_IN_SHIPMENT_STATUS);
												if ((ihangeShipmentRequired == 0) || (ihangeShipmentRequired > 0)) {
													bchangeShipmentOrder = true;
												}
											}

										}

									}

									else if (!YFCObject.isVoid(strFulfillmentType)
											&& strFulfillmentType.equalsIgnoreCase(AcademyConstants.FULFILLMENT_TYPE)) {
										log.verbose("The OrderlineKey" + strOrderLinekey + "Is DROP_SHIP");
										if (YFCObject.isVoid(docCommonCodeForCancellationstatus)) {
											docCommonCodeForCancellationstatus = CallCommonCodeAPI(env,
													AcademyConstants.STR_ORD_CANCELLATION_STATUS);
										}
										if(bIsLineCancellation && bIsDSVChubPOCancellationCall) {
											strCodeLongDesc = XPathUtil.getString(
													docCommonCodeForCancellationstatus.getDocumentElement(),
													AcademyConstants.XPATH_MAX_DROPSHIP_LINE_CANCEL_STATUS_CODE_LONG_DESC);
										}else {
											strCodeLongDesc = XPathUtil.getString(
													docCommonCodeForCancellationstatus.getDocumentElement(),
													AcademyConstants.XPATH_MAX_VENDOR_CANCEL_STATUS_CODE_LONG_DESC);
										}
										
										if (!YFCObject.isVoid(strCodeLongDesc)) {
											Integer iCancellationAllowed = compareStatus(strCodeLongDesc, strStatus);//2100.100
											if (iCancellationAllowed < 0) {
												if(bIsLineCancellation) {
													lstNotEligibleItems.add(eleItemDetails.getAttribute(AcademyConstants.ITEM_ID));
													bLineAlreadyCancelledOrShipped = true;
												} else {
													bAlreadyCancelledOrShippedORPickedErrorCode = true;
												}
											}

											else {

												bchangeOrder = true;
												Integer ihangeShipmentRequired = compareStatus(strStatus,
														AcademyConstants.STR_INCLUDE_IN_SHIPMENT_STATUS);
												if ((ihangeShipmentRequired == 0) || (ihangeShipmentRequired > 0)) {
													bchangeShipmentOrder = true;
												}
											}

										}

									}

									else {

										Integer iBeforeReleaseStatus = compareStatus(strStatus,
												AcademyConstants.VAL_RELE_STATUS);
										if ((iBeforeReleaseStatus == 0) || (iBeforeReleaseStatus < 0)) {
											bchangeOrder = true;
										}

										else {

											docgetShipNodeListInput = XMLUtil
													.createDocument(AcademyConstants.ATTR_SHIP_NODE);
											elegetShipNodeListInput = docgetShipNodeListInput.getDocumentElement();
											String strShipNode = eleOrderStatus
													.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
											elegetShipNodeListInput.setAttribute(AcademyConstants.ATTR_SHIP_NODE,
													strShipNode);
											log.verbose(
													"Calling AcademyCallgetShipNodeListForOrderForWCSCancellation Service with input : ="
															+ XMLUtil.getXMLString(docgetShipNodeListInput));
											docgetShipNodeListOutput = AcademyUtil.invokeService(env,
													AcademyConstants.ACADEMY_CALL_GET_SHIPNODE_LIST_FOR_ORDER_WCS_CANCELLATION,
													docgetShipNodeListInput);
											Element eleShipnode = (Element) docgetShipNodeListOutput
													.getDocumentElement()
													.getElementsByTagName(AcademyConstants.ATTR_SHIP_NODE).item(0);
											String strNodeType = eleShipnode
													.getAttribute(AcademyConstants.ATTR_NODE_TYPE);
											if (!YFCObject.isVoid(strNodeType)
													&& (strNodeType.equalsIgnoreCase(AcademyConstants.STR_DC))) {
												log.verbose("The OrderlineKey" + strOrderLinekey + "Is DC");
												// System.out.println("The OrderlineKey"+strOrderLinekey+"Is DC");
												if (YFCObject.isVoid(docCommonCodeForCancellationstatus)) {
													docCommonCodeForCancellationstatus = CallCommonCodeAPI(env,
															AcademyConstants.STR_ORD_CANCELLATION_STATUS);
												}
												strCodeLongDesc = XPathUtil.getString(
														docCommonCodeForCancellationstatus.getDocumentElement(),
														AcademyConstants.XPATH_MAX_DC_CANCEL_STATUS_CODE_LONG_DESC);
												if (!YFCObject.isVoid(strCodeLongDesc)) {
													Integer iCancellationAllowed = compareStatus(strCodeLongDesc,
															strStatus);
													if (iCancellationAllowed < 0) {
														bAlreadyCancelledOrShippedORPickedErrorCode = true;
													}

													else {

														bchangeOrder = true;
														Integer ihangeShipmentRequired = compareStatus(strStatus,
																AcademyConstants.STR_INCLUDE_IN_SHIPMENT_STATUS);
														if ((ihangeShipmentRequired == 0)
																|| (ihangeShipmentRequired > 0)) {
															bchangeShipmentOrder = true;
														}
													}

												}

											}

											else if (!YFCObject.isVoid(strNodeType) && strNodeType
													.equalsIgnoreCase(AcademyConstants.ATTR_VAL_SHAREDINV_DC)) {
												log.verbose("The OrderlineKey" + strOrderLinekey
														+ "Is Shared Inventory DC");
												// System.out.println("The OrderlineKey"+strOrderLinekey+"Is Shared
												// Inventory DC");
												if (YFCObject.isVoid(docCommonCodeForCancellationstatus)) {
													docCommonCodeForCancellationstatus = CallCommonCodeAPI(env,
															AcademyConstants.STR_ORD_CANCELLATION_STATUS);
												}
												strCodeLongDesc = XPathUtil.getString(
														docCommonCodeForCancellationstatus.getDocumentElement(),
														AcademyConstants.XPATH_MAX_SHARED_INV_DC_CANCEL_STATUS_CODE_LONG_DESC);
												if (!YFCObject.isVoid(strCodeLongDesc)) {
													Integer iCancellationAllowed = compareStatus(strCodeLongDesc,
															strStatus);
													if (iCancellationAllowed < 0) {
														bAlreadyCancelledOrShippedORPickedErrorCode = true;
													}

													else {

														bchangeOrder = true;
														Integer ihangeShipmentRequired = compareStatus(strStatus,
																AcademyConstants.STR_INCLUDE_IN_SHIPMENT_STATUS);
														if ((ihangeShipmentRequired == 0)
																|| (ihangeShipmentRequired > 0)) {
															bchangeShipmentOrder = true;
														}
													}

												}
											}

											else {
												log.verbose("The OrderlineKey" + strOrderLinekey + "Is SFS");
												if (YFCObject.isVoid(docCommonCodeForCancellationstatus)) {
													docCommonCodeForCancellationstatus = CallCommonCodeAPI(env,
															AcademyConstants.STR_ORD_CANCELLATION_STATUS);
												}
												strCodeLongDesc = XPathUtil.getString(
														docCommonCodeForCancellationstatus.getDocumentElement(),
														AcademyConstants.XPATH_MAX_SFS_CANCEL_STATUS_CODE_LONG_DESC);
												if (!YFCObject.isVoid(strCodeLongDesc)) {
													Integer iCancellationAllowed = compareStatus(strCodeLongDesc,
															strStatus);
													if (iCancellationAllowed < 0) {
														if(bIsLineCancellation) {
															bLineAlreadyCancelledOrShipped = true;
														} else {
															bAlreadyCancelledOrShippedORPickedErrorCode = true;
														}
													}

													else {

														bchangeOrder = true;
														Integer ihangeShipmentRequired = compareStatus(strStatus,
																AcademyConstants.STR_INCLUDE_IN_SHIPMENT_STATUS);
														if ((ihangeShipmentRequired == 0)
																|| (ihangeShipmentRequired > 0)) {
															bchangeShipmentOrder = true;
														}
													}

												}

											}
										}
									}

									if (bchangeOrder) {
										Element elechangeOrderOrderLine = docchangeOrderInput
												.createElement(AcademyConstants.ELE_ORDER_LINE);
										elechangeOrderOrderLines.appendChild(elechangeOrderOrderLine);
										elechangeOrderOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,
												strOrderLinekey);
										elechangeOrderOrderLine.setAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE,
												strFulfillmentType);
										if(bIsLineCancellation) {
											elechangeOrderOrderLine.setAttribute(AcademyConstants.ATTR_ACTION,
													AcademyConstants.STR_CANCEL);
										}else {
											elechangeOrderOrderLine.setAttribute(AcademyConstants.ATTR_ACTION,
													AcademyConstants.STR_ACTION_MODIFY);
										}
										Element elechangeOrderNotes = docchangeOrderInput
												.createElement(AcademyConstants.ELE_NOTES);
										elechangeOrderOrderLine.appendChild(elechangeOrderNotes);
										Element elechangeOrderNote = docchangeOrderInput
												.createElement(AcademyConstants.ELE_NOTE);
										elechangeOrderNotes.appendChild(elechangeOrderNote);
										elechangeOrderNote.setAttribute(AcademyConstants.ATTR_OPERATION,
												AcademyConstants.STR_OPERATION_VAL_CREATE);
										elechangeOrderNote.setAttribute(AcademyConstants.ATTR_NOTE_TEXT, strNoteText);
										elechangeOrderNote.setAttribute(AcademyConstants.ATTR_REASON_CODE,
												strReasonCode);
										elechangeOrderNote.setAttribute(AcademyConstants.STR_CONTACT_REFERENCE,
												strContactReference);
										elechangeOrderNote.setAttribute(AcademyConstants.STR_CONTACT_USER,
												strContactUser);
										
										//Start : OMNI - 94433
										String strPurchaseOrderNo = EleOrderLine.getAttribute("PurchaseOrderNo"); 
										String strPurchaseOrderLineNo = EleOrderLine.getAttribute("PurchaseOrderLineNo"); 
										if(!YFCObject.isNull(strPurchaseOrderNo) && !YFCObject.isNull(strPurchaseOrderLineNo)) {
											elechangeOrderOrderLine.setAttribute("PurchaseOrderNo",
													strPurchaseOrderNo);
											elechangeOrderOrderLine.setAttribute("PurchaseOrderLineNo",
													strPurchaseOrderLineNo);
										}
										elechangeOrderOrderLine.setAttribute(AcademyConstants.ITEM_ID,
												eleItemDetails.getAttribute(AcademyConstants.ITEM_ID));
										elechangeOrderOrderLine.setAttribute("CancellableQuantity",
												EleOrderLine.getAttribute(AcademyConstants.ITEM_ID));
										//End : OMNI - 94433
										
										
									}
								} // end of else
							} // end of order status loop

						} // end of orderline for loop
					}

				}
				log.verbose("bchangeOrder= " + bchangeOrder);

				log.verbose("bchangeShipmentOrder= " + bchangeShipmentOrder);

				log.verbose(
						"bAlreadyCancelledOrShippedORPickedErrorCode= " + bAlreadyCancelledOrShippedORPickedErrorCode);

				log.verbose("bmandatErrorCode= " + bmandatErrorCode);

				log.verbose("bIsLineCancellation= " + bIsLineCancellation);
				log.verbose(
						"bLineAlreadyCancelledOrShipped= " + bLineAlreadyCancelledOrShipped);
				if (bmandatErrorCode) {
					if (bIsStoreCancellation) {
						yfce.setAttribute("ErrorCode", "CAN0001");
						yfce.setErrorDescription("Invalid input parameter Or Mandatory attributes are missing");
						throw yfce;
					} else {
						errorDescription = "Invalid input parameter Or Mandatory attributes are missing";
						e.setErrorCode("CAN0001");
						e.setErrorDescription(errorDescription);
						throw e;
					}
				}

				if (bAlreadyCancelledOrShippedORPickedErrorCode) {
					if (bIsStoreCancellation) {
						yfce.setAttribute("ErrorCode", "CAN0002");
						yfce.setErrorDescription("Order is not eligible for cancellation");
						throw yfce;
					} else {
						errorDescription = "Order is not eligible for cancellation";
						e.setErrorCode("CAN0002");
						e.setErrorDescription(errorDescription);
						throw e;
					}

				}
				//OMNI-30152 Start Throws OrderLine Not Cancellation Eligible Error
				if(bLineAlreadyCancelledOrShipped) {
					//OMNI-69803 - START	
					if (bIsMultiCancelAllowed) {
						strItemNotEligible = String.join(", ", lstNotEligibleItems);
						errorDescription = "Item(s) " + strItemNotEligible + " is not in an eligible status for cancellation";	
						e.setErrorCode("CAN0004");	
						e.setErrorDescription(errorDescription);	
						throw e;	
					}	
					else {	
						//OMNI-69803 - END	
						errorDescription = "OrderLine is not eligible for cancellation";	
						e.setErrorCode("CAN0004");	
						e.setErrorDescription(errorDescription);	
						throw e;	
					}							

				}
				//OMNI-30152 End
				//OMNI-90242  - START
				env.setTxnObject(AcademyConstants.STR_IS_WCS_CANCELLATION, AcademyConstants.STR_YES);
				//OMNI-90242  - END
				if (bchangeShipmentOrder) {
					/**
					 * If STS line level Cancellation is present then below logic cancels the shipment 
					 * line corresponding to sales order line.
					 * Else condition will take care of Order Cancellation. 
					 */
					if(bIsLineCancellation) {
						boolean bLastShipmentLineCancel = false;
						String strOrderLineKey = null;
						String strFulfillmentType = null;
						log.verbose("Entered into ShipmentLineCancellation");

						//OMNI-69803 - START
						log.verbose("docchangeOrderInput XML is :  " + XMLUtil.getXMLString(docchangeOrderInput));
						log.verbose("BopisMultiLineCancel Flag is :  " + bIsMultiCancelAllowed);	

						if(bIsMultiCancelAllowed) {

							log.verbose("Inside BopisMultiLineCancel");	
							NodeList nlOrderLineToCancel = docchangeOrderInput.getDocumentElement()
									.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);

							for (int h = 0; h < nlOrderLineToCancel.getLength(); h++) {

								Element eleOrderLineInput = (Element) nlOrderLineToCancel.item(h);
								log.verbose("eleOrderLineInput XML is :  " + XMLUtil.getElementXMLString(eleOrderLineInput));
								strOrderLineKey = eleOrderLineInput.getAttribute("OrderLineKey");
								strFulfillmentType = eleOrderLineInput.getAttribute("FulfillmentType");

								log.verbose("OrderLineKey is "+strOrderLineKey);

								docgetShipmentListForOrderOutput = CallShipmentLineListForOrder(env, strOrderLineKey, strFulfillmentType);
								NodeList NLShipment = docgetShipmentListForOrderOutput.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);

								for(int q=0;q < NLShipment.getLength() ; q++) {

									Element eleShipment = (Element) NLShipment.item(q);
									String Status = eleShipment.getAttribute(AcademyConstants.STATUS);
									if (Status.equalsIgnoreCase(AcademyConstants.VAL_CANCELLED_STATUS) ||
											Status.equalsIgnoreCase(AcademyConstants.STATUS_RECEIPT_CLOSED)) {
										log.verbose("Shipment Status= " + Status);
										continue;
									}				
									else if (Status.equalsIgnoreCase(AcademyConstants.STATUS_SHIPMENT_BEING_PACKED))
									{							        
										log.verbose("Shipment Status = " + Status);
										if (bIsStoreCancellation) {
											yfce.setAttribute("ErrorCode", "CAN0004");
											yfce.setErrorDescription("OrderLine is not eligible for cancellation");
											throw yfce;
										} else {
											errorDescription = "OrderLine is not eligible for cancellation";
											e.setErrorCode("CAN0004");
											e.setErrorDescription(errorDescription);
											throw e;
										}										        
									}			
									String strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
									Document docChangeShipmentInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
									Element eleChangeShipmentInput = docChangeShipmentInput.getDocumentElement();
									eleChangeShipmentInput.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
									eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY);


									Element eleShpLines = docChangeShipmentInput.createElement("ShipmentLines");
									Element eleShipmLines = XmlUtils.getChildElement(eleShipment, AcademyConstants.ELE_SHIPMENT_LINES);
									NodeList nlShipmentLine = eleShipmLines.getChildNodes() ;
									int iShpLines = 0;
									for(int r=0; r< nlShipmentLine.getLength(); r++) {
										Element eleShpmentLine = (Element) nlShipmentLine.item(r);
										String strShpOrdLineKey = eleShpmentLine.getAttribute("OrderLineKey");
										String strshpqty = eleShpmentLine.getAttribute( AcademyConstants.ATTR_QUANTITY);
										log.verbose("Quantity is "+strshpqty );
										Double ishpqty = Double.parseDouble(strshpqty);
										if(ishpqty>0) {
											iShpLines++;
										}
										log.verbose("Quantity is "+ishpqty );

										if(strShpOrdLineKey.equals(strOrderLineKey)) {
											Element eleShpLine=docChangeShipmentInput.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
											eleShpLine.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.VAL_CANCEL);
											eleShpLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY,eleShpmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY) );					
											XMLUtil.appendChild(eleShpLines,eleShpLine);
										}	
									}

									XMLUtil.appendChild(eleChangeShipmentInput,eleShpLines);
									log.verbose("cHANGESHIPMENT Input  " + XMLUtil.getXMLString(docChangeShipmentInput));
									AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipmentInput);

								}
							}
						}
						//OMNI-69803 - END
						else {

							log.verbose("Inside else loop of BopisMultiLineCancel");
							strOrderLineKey = XPathUtil.getString(docchangeOrderInput, "/Order/OrderLines/OrderLine/@OrderLineKey");
							strFulfillmentType = XPathUtil.getString(docchangeOrderInput, "/Order/OrderLines/OrderLine/@FulfillmentType");
							log.verbose("OrderLineKey is "+strOrderLineKey);

							docgetShipmentListForOrderOutput = CallShipmentLineListForOrder(env, strOrderLineKey, strFulfillmentType);
							NodeList NLShipment = docgetShipmentListForOrderOutput.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);

							for(int q=0;q < NLShipment.getLength() ; q++) {

								Element eleShipment = (Element) NLShipment.item(q);
								String Status = eleShipment.getAttribute(AcademyConstants.STATUS);
								if (Status.equalsIgnoreCase(AcademyConstants.VAL_CANCELLED_STATUS) ||
										Status.equalsIgnoreCase(AcademyConstants.STATUS_RECEIPT_CLOSED)) {
									log.verbose("Shipment Status= " + Status);
									continue;
								}				
								else if (Status.equalsIgnoreCase(AcademyConstants.STATUS_SHIPMENT_BEING_PACKED))
								{							        
									log.verbose("Shipment Status = " + Status);
									if (bIsStoreCancellation) {
										yfce.setAttribute("ErrorCode", "CAN0004");
										yfce.setErrorDescription("OrderLine is not eligible for cancellation");
										throw yfce;
									} else {
										errorDescription = "OrderLine is not eligible for cancellation";
										e.setErrorCode("CAN0004");
										e.setErrorDescription(errorDescription);
										throw e;
									}										        
								}			
								String strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
								Document docChangeShipmentInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
								Element eleChangeShipmentInput = docChangeShipmentInput.getDocumentElement();
								eleChangeShipmentInput.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
								eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY);


								Element eleShpLines = docChangeShipmentInput.createElement("ShipmentLines");
								Element eleShipmLines = XmlUtils.getChildElement(eleShipment, AcademyConstants.ELE_SHIPMENT_LINES);
								NodeList nlShipmentLine = eleShipmLines.getChildNodes() ;
								int iShpLines = 0;
								for(int r=0; r< nlShipmentLine.getLength(); r++) {
									Element eleShpmentLine = (Element) nlShipmentLine.item(r);
									String strShpOrdLineKey = eleShpmentLine.getAttribute("OrderLineKey");
									String strshpqty = eleShpmentLine.getAttribute( AcademyConstants.ATTR_QUANTITY);
									log.verbose("Quantity is "+strshpqty );
									Double ishpqty = Double.parseDouble(strshpqty);
									if(ishpqty>0) {
										iShpLines++;
									}
									log.verbose("Quantity is "+ishpqty );

									if(strShpOrdLineKey.equals(strOrderLineKey)) {
										Element eleShpLine=docChangeShipmentInput.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
										eleShpLine.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.VAL_CANCEL);
										eleShpLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY,eleShpmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY) );					
										XMLUtil.appendChild(eleShpLines,eleShpLine);
									}	
								}

								XMLUtil.appendChild(eleChangeShipmentInput,eleShpLines);
								/* OMNI-56207 - START
								if(iLinesToCancel == iShpLines) {
									bLastShipmentLineCancel = true;
									log.verbose("bLastShipmentLineCancel is "+bLastShipmentLineCancel);
								}
								log.verbose("bLastShipmentLineCancel is "+bLastShipmentLineCancel);

								if(isSIMUnreserveMsgRequired && bLastShipmentLineCancel) {
									log.verbose("cHANGESHIPMENT to Modify Input " + XMLUtil.getXMLString(docChangeShipmentInput));
									AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipmentInput);

								}
								if(bLastShipmentLineCancel) {
									eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_ACTION,AcademyConstants.VAL_CANCEL);
								}
								OMNI-56207 - END*/
								log.verbose("cHANGESHIPMENT Input  " + XMLUtil.getXMLString(docChangeShipmentInput));

								AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipmentInput);

							}
						}
					}
					else {

						// call getShipmentListForOrder with sample input
						docgetShipmentListForOrderOutput = CallShipmnetListForOrder(env, strOrderNo);

						NodeList NLShipment = docgetShipmentListForOrderOutput.getDocumentElement()
								.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
						for (int j = 0; j < NLShipment.getLength(); j++) {
							Element eleShipment = (Element) NLShipment.item(j);
							String Status = eleShipment.getAttribute(AcademyConstants.STATUS);
							//BugFix for MixedCart with STS
							if (Status.equalsIgnoreCase(AcademyConstants.VAL_CANCELLED_STATUS) ||
									Status.equalsIgnoreCase(AcademyConstants.STATUS_RECEIPT_CLOSED) ||
									Status.equalsIgnoreCase(AcademyConstants.VAL_SHIPPED_STATUS)) {
								log.verbose("Shipment Status= " + Status);
								continue;
							}
							//BugFix for MixedCart with STS
							//Start : BOPIS-1552
							else if (Status.equalsIgnoreCase(AcademyConstants.STATUS_SHIPMENT_BEING_PACKED))
							{							        
								log.verbose("Shipment Status = " + Status);
								if (bIsStoreCancellation) {
									yfce.setAttribute("ErrorCode", "CAN0002");
									yfce.setErrorDescription("Order is not eligible for cancellation");
									throw yfce;
								} else {
									errorDescription = "Order is not eligible for cancellation";
									e.setErrorCode("CAN0002");
									e.setErrorDescription(errorDescription);
									throw e;
								}										        
							}
							//End : BOPIS-1552	
							String strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
							Document docChangeShipmentInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
							Element eleChangeShipmentInput = docChangeShipmentInput.getDocumentElement();
							eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.VAL_CANCEL);
							eleChangeShipmentInput.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);

							//Start: BOPIS-1815
							if (Status.equalsIgnoreCase(AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS) ||
									Status.equalsIgnoreCase(AcademyConstants.STR_PAPER_WORK_INITIATED_STATUS)){/*


							Document docChangeShipmentoModify = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
							Element eleChangeShipmenttomodify = docChangeShipmentoModify.getDocumentElement();
							eleChangeShipmenttomodify.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY);
							eleChangeShipmenttomodify.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
							Element eleShipmentLinestomodify=docChangeShipmentoModify.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
							XMLUtil.appendChild(eleChangeShipmenttomodify,eleShipmentLinestomodify);


						//Call getShipmentList	
							Document docgetShipmentList=XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
							Element elegetShipmentList = docgetShipmentList.getDocumentElement();
							elegetShipmentList.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,strShipmentKey);
							elegetShipmentList.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
									eleShipment.getAttribute(AcademyConstants.ATTR_DOC_TYPE));
							elegetShipmentList.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,
									eleShipment.getAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE));

							log.verbose("getShipmentList Input  " + XMLUtil.getXMLString(docgetShipmentList));
							Document docgetShipmentListOP=AcademyUtil.invokeService
									(env, AcademyConstants.ACADEMY_GET_SHIPMENT_LIST_FOR_WCS_CANCEL, docgetShipmentList);

							NodeList nlShipmentLine = docgetShipmentListOP.getDocumentElement()
									.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
							for(int k = 0; k < nlShipmentLine.getLength(); k++){
								Element eleShipmentline = (Element) nlShipmentLine.item(k);
								String strActualQuantity=eleShipmentline.getAttribute(AcademyConstants.ATTR_QUANTITY);

								Element eleShipmentLinetomodify=docChangeShipmentoModify.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
								eleShipmentLinetomodify.setAttribute(AcademyConstants.ATTR_SHORTAGE_QTY, strActualQuantity);

								Double dQuantity =  Double.parseDouble(strActualQuantity);
								Double dnewQty=dQuantity-dQuantity;
								String strnewQty=String.valueOf(dnewQty);

								eleShipmentLinetomodify.setAttribute(AcademyConstants.ATTR_QUANTITY, strnewQty);
								eleShipmentLinetomodify.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY,
										eleShipmentline.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
								XMLUtil.appendChild(eleShipmentLinestomodify,eleShipmentLinetomodify);
								Element eleExtntomodify=docChangeShipmentoModify.createElement(AcademyConstants.ELE_EXTN);
								eleExtntomodify.setAttribute(AcademyConstants.ATTR_EXTN_MSG_TO_SIM, AcademyConstants.STR_YES);
								XMLUtil.appendChild(eleShipmentLinetomodify,eleExtntomodify);


							}

							log.verbose("cHANGESHIPMENT to Modify Input  " + XMLUtil.getXMLString(docChangeShipmentoModify));
							AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipmentoModify);
									 */}
							//End: BOPIS-1815
							log.verbose("cHANGESHIPMENT Input  " + XMLUtil.getXMLString(docChangeShipmentInput));

							AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipmentInput);
						}
					}

				}
				if (bchangeOrder) { // call change order and // Prepare the
					// Success response
					//OMNI-30152 Start 
					if(bIsLineCancellation) {
						env.setTxnObject("IsLineLevelCancellationFromWCS", docchangeOrderInput);
					}
					//OMNI-30152 End
					
					//Start : OMNI-99051
					if(bIsDSVChubPOCancellationCall) {
						//Validate DSV Lines and invoke CHub Cancellation
						docchangeOrderInput = validateAndInvokeCHubCancellation(env, docchangeOrderInput, docCommonCodeForCancellationstatus, e);
					}
					//End : OMNI-99051
					
					log.verbose("changeOrder Input  " + XMLUtil.getXMLString(docchangeOrderInput));
					// System.out.println("changeOrder Input
					// "+XMLUtil.getXMLString(docchangeOrderInput));
					AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, docchangeOrderInput);
					returndoc = PrepareSuccessResponce(env, strOrderNo,inXML,bIsLineCancellation);
					log.verbose("returndoc" + XMLUtil.getXMLString(returndoc));
					// System.out.println("returndoc
					// "+XMLUtil.getXMLString(returndoc));
					return returndoc;

				}

			}

			else {
				if (bIsStoreCancellation) {
					yfce.setAttribute("ErrorCode", "CAN0001");
					yfce.setErrorDescription("Invalid input parameter. Or Mandatory attributes are missing");
					throw yfce;
				} else {
					errorDescription = "Invalid input parameter. Or Mandatory attributes are missing";
					e.setErrorCode("CAN0001");
					e.setErrorDescription(errorDescription);
					throw e;
				}
			}
		}

		catch (Exception y) {
			if (!StringUtil.isEmpty(e.getErrorCode())) {
				throw e;

			} else if (!YFCCommon.isVoid((yfce.getAttribute("ErrorCode")))){
				throw yfce;
			} else {

				if (bIsStoreCancellation ){
					YFCException ex = new YFCException("CAN0003");
					ex.setErrorDescription("Runtime Exception occurred while processing the cancellation Request.");
					throw ex;
				} else {
					errorDescription = "Runtime Exception occurred while processing the cancellation Request.";
					YFSException yfse = new YFSException();
					yfse.setErrorCode("CAN0003");
					yfse.setErrorDescription(errorDescription);
					throw yfse;
				}

			}
		}

		log.endTimer("AcademyOrderCancellationFromWCS::processCompleteOrderCancellation");

		return inXML;
	}

	/**
	 * @param env
	 * @param inXML 
	 * @param strOrderNo
	 * @return
	 * @throws ParserConfigurationException
	 */
	public Document PrepareSuccessResponce(YFSEnvironment env, String strOrderNo, Document inXML, boolean bIsLineCancellation) throws ParserConfigurationException {
		// <Result SUCCESS="Y" Description="OrderNo:'201805230006' is
		// cancelled"/>
		//OMNI-30152 Start
		if(bIsLineCancellation) {
			Element eleInXML = inXML.getDocumentElement();
			eleInXML.setAttribute("Status", "Success");

			return inXML;
		}
		//OMNI-30152 End
		else {
			Document docSuccessRespose = XMLUtil.createDocument(AcademyConstants.STR_RESULT);
			Element eleSuccessRespose = docSuccessRespose.getDocumentElement();
			eleSuccessRespose.setAttribute(AcademyConstants.STR_SUCCESS, AcademyConstants.STR_YES);
			eleSuccessRespose.setAttribute(AcademyConstants.ATTR_DESC, AcademyConstants.ATTR_ORDER_NO + ":" + "'"
					+ strOrderNo + "'" + " is " + AcademyConstants.STR_CANCELLED);
			eleSuccessRespose.setAttribute("httpcode", "200");
			return docSuccessRespose;
		}
	}

	/**
	 * @throws ParserConfigurationException
	 * @param env
	 * 			@param strOrderNo @return @throws
	 */
	public Document CallgetOrderListInputdoc(YFSEnvironment env, String strOrderNo)
			throws ParserConfigurationException {
		Document docgetOrderListInput = null;
		Document docgetOrderListOutput = null;
		Element elegetOrderListInput = null;
		docgetOrderListInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		elegetOrderListInput = docgetOrderListInput.getDocumentElement();
		elegetOrderListInput.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
		elegetOrderListInput.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		elegetOrderListInput.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);

		try {
			docgetOrderListOutput = AcademyUtil.invokeService(env,
					AcademyConstants.ACADEMY_CALL_GET_ORDER_LIST_FOR_WCS_CANCELLATION, docgetOrderListInput);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.verbose("Output of AcademyCallgetOrderListForWCSCancellation service  "
				+ XMLUtil.getXMLString(docgetOrderListOutput));
		return docgetOrderListOutput;
	}

	public Document CallShipmnetListForOrder(YFSEnvironment env, String strOrderNo)
			throws ParserConfigurationException {
		Document docShipmnetListForOrderInput = null;
		Document docShipmnetListForOrderOutput = null;
		Element eleRoot = null;
		docShipmnetListForOrderInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		eleRoot = docShipmnetListForOrderInput.getDocumentElement();
		eleRoot.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
		eleRoot.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		eleRoot.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		try {
			docShipmnetListForOrderOutput = AcademyUtil.invokeService(env,
					AcademyConstants.ACADEMY_CALL_GET_SHIPMENT_LIST_FOR_ORDER_WCS_CANCELLATION,
					docShipmnetListForOrderInput);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.verbose("Output of docgetShipmentListForOrderOutput service  "
				+ XMLUtil.getXMLString(docShipmnetListForOrderOutput));
		return docShipmnetListForOrderOutput;
	}

	/**
	 * @param env
	 * @param strFulfillmentType
	 * @return This method invokes the getcommonCodeList api for getting the max
	 *         Order Cancellation status
	 */
	public Document CallCommonCodeAPI(YFSEnvironment env, String ORD_CANCELTN_STATUS) {
		/*
		 * call getCommonCodeList API <CommonCode CodeType="APPEASE_GC"/>
		 */
		Document docMaxCancellationAllowedStatus = null;
		try {
			Document getCommonCodeListInputXML = XMLUtil.createDocument("CommonCode");
			getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeType", ORD_CANCELTN_STATUS);
			docMaxCancellationAllowedStatus = AcademyUtil.invokeAPI(env, "getCommonCodeList",
					getCommonCodeListInputXML);

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return docMaxCancellationAllowedStatus;
	}

	/**
	 * THis method compares statuses without using the == operator.
	 * 
	 * @param status1
	 *            yfs_status code
	 * @param status2
	 *            yfs_status code
	 * @return if status1 == status2, it returns 0 else if status1 > status2, it
	 *         returns positive integer else if status1 < status2, it returns
	 *         negative integer
	 */
	public static Integer compareStatus(String status1, String status2) {
		int comparisonNumber = YFCCommon.compareStrings(status1, status2);
		if (comparisonNumber == 0) {
			return 0;
		} else if (!YFCCommon.isVoid(status1) && YFCCommon.isVoid(status2)) {
			return 1;
		} else if (YFCCommon.isVoid(status1) && !YFCCommon.isVoid(status2)) {
			return -1;
		}

		String[] splittedStatus1 = { status1 };
		if (status1.indexOf('.') > 0) {
			splittedStatus1 = status1.split("\\.");
		}

		String[] splittedStatus2 = { status2 };
		if (status2.indexOf('.') > 0) {
			splittedStatus2 = status2.split("\\.");
		}

		int minSubStatusNo = Math.min(splittedStatus1.length, splittedStatus2.length);
		int i = 0;
		for (; i < minSubStatusNo; i++) {
			comparisonNumber = Integer.parseInt(splittedStatus1[i]) - Integer.parseInt(splittedStatus2[i]);
			if (comparisonNumber != 0) {
				return comparisonNumber;
			}
		}

		if (splittedStatus1.length == splittedStatus2.length) {
			return 0;
		} else if (splittedStatus1.length > splittedStatus2.length) {
			return 1;
		} else if (splittedStatus1.length < splittedStatus2.length) {
			return -1;
		}

		return null;
	}

	/**
	 * This method is used to Split the cancellation lines from the getOrderList API Output using ExtnWCOrderItemIdentifier
	 *  from the Input XML of WCS and prepare the list of lines as a document to cancel the those lines.
	 *  inXML is the Input from WCS, eleOrder is getOrderList API Output 
	 * @throws Exception 
	 */
	//OMNI-30152 Start
	public Document getOrderLinesForCancellation(YFSEnvironment env, Document inXML, Element eleOrder) throws Exception {
		// TODO Auto-generated method stub
		log.verbose("Entered into getOrderLinesForCancellation() at line level ");
		log.verbose("Input XML to the method is  "+ XMLUtil.getElementXMLString(eleOrder));
		Element eleInputOrder = null;
		Element eleInputOrderLine = null;

		Document docOutput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		Element eleOrderOutput = docOutput.getDocumentElement();

		XmlUtils.setAttributes(eleOrder, eleOrderOutput);	

		Element eleOrderLinesOut = docOutput.createElement(AcademyConstants.ELEM_ORDER_LINES);

		eleInputOrder = inXML.getDocumentElement();		
		NodeList NLOrderLine = eleInputOrder.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINE);
		NodeList NLeleOrderLine = eleOrder.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINE);

		//Start : OMNI-41168, OMNI-41751
		ArrayList<String> arrFulfillmentTypesForCancellation = fulfillmentTypesEligibleForCancellation(env);
		//End : OMNI-41168, OMNI-41751

		for(int i=0; i < NLOrderLine.getLength() ; i++) {
			eleInputOrderLine = (Element) NLOrderLine.item(i);
			Element eleInputExtn = XmlUtils.getChildElement(eleInputOrderLine, AcademyConstants.ELE_EXTN);
			String strInputWCOrderItemIdentifier = eleInputExtn.getAttribute(AcademyConstants.ATTR_EXTN_WCORDER_ITEM_IDENTIFIER);
			log.verbose("Input XML ExtnWCOrderItemIdentfier "+strInputWCOrderItemIdentifier);
			for(int j=0; j < NLeleOrderLine.getLength(); j++ ) {

				Element eleOrderLine = (Element) NLeleOrderLine.item(j);
				String strFulfillmentType = eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
				Element eleExtn = XmlUtils.getChildElement(eleOrderLine, "Extn");
				String strExtnWCOrderItemIdentifier = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_WCORDER_ITEM_IDENTIFIER);
				log.verbose("getOrderList API Output ExtnWCOrderItemIdentfier "+strExtnWCOrderItemIdentifier);
				//Start : OMNI-41168, OMNI-41751
				if(strExtnWCOrderItemIdentifier.equals(strInputWCOrderItemIdentifier) &&
						!YFCCommon.isVoid(arrFulfillmentTypesForCancellation) &&
						arrFulfillmentTypesForCancellation.contains(strFulfillmentType)) {
					log.verbose("Items are matched, ExtnWCOrderItemIdentifier is "+ strInputWCOrderItemIdentifier);
					log.verbose("Fulfillment Type is "+ strFulfillmentType);
					XMLUtil.importElement(eleOrderLinesOut, eleOrderLine);
				}	
				//End : OMNI-41168, OMNI-41751

			}
		}
		eleOrderOutput.appendChild(eleOrderLinesOut); 
		log.verbose("Document for the Cancellation is "+ XMLUtil.getXMLString(docOutput));
		log.verbose("Exiting the getOrderLinesForCancellation()");
		return  docOutput;
	}
	//OMNI-30152 End	

	/**
	 * This method is used to call the getShipmentLineList for OrderLine to get the STS Sales Shipment Line for cancellation
	 * OMNI-30152 Start
	 */
	public Document CallShipmentLineListForOrder(YFSEnvironment env, String strOrderLineKey, String strFulfillmentType) throws ParserConfigurationException {
		// TODO Auto-generated method stub
		log.verbose("Entered into CallShipmentLineListForOrder method:: ");
		Document docShipmnetLineListInput = null;
		Document docShipmnetLineListOutput = null;
		Element eleRoot = null;
		docShipmnetLineListInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT_LINE);
		eleRoot = docShipmnetLineListInput.getDocumentElement(); 
		eleRoot.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, strOrderLineKey);
		Element eleShipment = docShipmnetLineListInput.createElement(AcademyConstants.ELE_SHIPMENT);

		//Start : OMNI-41168, OMNI-41751
		if(AcademyConstants.STR_SHIP_TO_STORE.equalsIgnoreCase(strFulfillmentType)) {
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.STR_SHIP_TO_STORE);
		}
		//End : OMNI-41168, OMNI-41751

		eleRoot.appendChild(eleShipment);

		log.verbose("Input for getShipmentLineList API service is " + XMLUtil.getXMLString(docShipmnetLineListInput));
		try {
			docShipmnetLineListOutput = AcademyUtil.invokeService(env,
					AcademyConstants.ACADEMY_GET_SHIPMENT_LINE_LIST_FOR_WCS_CANCELLATION,
					docShipmnetLineListInput);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.verbose("Output of docgetShipmentLineListOutput service  "
				+ XMLUtil.getXMLString(docShipmnetLineListOutput));
		return docShipmnetLineListOutput;
	}
	//OMNI-30152 End

	/**
	 * OMNI-30152 Start
	 * This method is to check the TO Line corresponding to Sales Line is eligible for Cancellation or not.
	 * Eligible cancellation Status are present in STS_LINE_CANCEL_ELG. Checking the status is present in 
	 * common code or not and less than 3350 (STS Included In Shipment)
	 * For the Shipment we are checking if Containers are present are not if the containers are present 
	 * then it is not eligible for Line level Cancellation and we update it as False
	 * STS_TO_LINE_CANCEL common code has the status eligible for TO Line Cancellation
	 */

	public Boolean ValidateSTSLineEligibleForCancel(YFSEnvironment env, String strOrderLineKey, boolean bIsMultiCancelAllowed) throws Exception {
		// TODO Auto-generated method stub
		log.verbose("Begin of AcademyOrderCancellationFromWCS.ValidateSTSLineEligibleForCancel() method");

		boolean bOrdCancel = false;

		Document docTransferOrderLineListInp = XMLUtil.createDocument(AcademyConstants.ELE_ORDER_LINE);
		Element eleTransferOrderLineListInp = docTransferOrderLineListInp.getDocumentElement();

		log.verbose("Sales OrderLineKey is "+ strOrderLineKey);
		eleTransferOrderLineListInp.setAttribute(AcademyConstants.ATTR_CHAINED_FROM_ORDER_LINE_KEY, strOrderLineKey);

		log.verbose("Input to API - getCompleteOrderLineList :: " + XMLUtil.getXMLString(docTransferOrderLineListInp));
		env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_LINE_LIST,
				AcademyConstants.STR_TEMPLATE_FILE_GET_COMPLETE_ORDER_LINE_LIST_STS_CANCEL);
		Document docTransferOrderLineListOut = AcademyUtil.invokeAPI(env,
				AcademyConstants.API_GET_COMPLETE_ORDER_LINE_LIST, docTransferOrderLineListInp);
		env.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_LINE_LIST);
		log.verbose("Output of API - getCompleteOrderLineList :: " + XMLUtil.getXMLString(docTransferOrderLineListOut));

		Document docCommonCodeForCancellableLinestatus = CallCommonCodeAPI(env,
				AcademyConstants.STR_STS_TO_LINE_CANCEL);

		List<String> alStatus = getStatusList(docCommonCodeForCancellableLinestatus);

		Element eleTOOrderLine = XMLUtil.getFirstElementByName(docTransferOrderLineListOut.getDocumentElement(), AcademyConstants.ELE_ORDER_LINE);
		log.verbose("eleTOOrderLine :: " + eleTOOrderLine.toString());
		Element eleTOOrder = XmlUtils.getChildElement(eleTOOrderLine, AcademyConstants.ELE_ORDER);
		log.verbose("eleOrder ::" + eleTOOrder.toString() );
		String strLineStatus = eleTOOrderLine.getAttribute(AcademyConstants.ATTR_MAXLINE_STATUS);
		log.verbose("strLineStatus ::" + strLineStatus);
		//if(alStatus.contains(strLineStatus) && Integer.parseInt(strLineStatus) <= 3350) { \\OMNI-69804	
		//alStatus contains list of all the TO cancel eligible status
		if(alStatus.contains(strLineStatus)) {
			bOrdCancel =true;
			log.verbose("bOrdCancel= " + bOrdCancel);
			Element eleShipment = (Element) XPathUtil.getNode(eleTOOrderLine, "ShipmentLines/ShipmentLine/Shipment");

			if(!YFCObject.isNull(eleShipment) && !YFCObject.isVoid(eleShipment)) {
				log.verbose("Inside the method");
				String strShipmentStatus = eleShipment.getAttribute(AcademyConstants.ATTR_STATUS);
				log.verbose("strShipmenttatus ::" + strShipmentStatus );

				//OMNI-69805 - START
				log.verbose("bIsMultiCancelAllowed : " + bIsMultiCancelAllowed);
				if (bIsMultiCancelAllowed) {
					bOrdCancel = true;
					log.verbose("bOrdCancel= Inside If bIsMultiCancelAllowed " + bOrdCancel);
				}
				//OMNI-69805 - END
				else if (alStatus.contains(strShipmentStatus)) { 
					log.verbose("Inside the method after checking ShipmentStatus");
					NodeList nlContainers = eleShipment.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
					if (nlContainers.getLength() > 0) {
						bOrdCancel = false;
						log.verbose("bOrdCancel= Inside If " + bOrdCancel);
					} else {
						bOrdCancel = true;
						log.verbose("bOrdCancel= Inside else" + bOrdCancel);
					}
				}else {
					bOrdCancel = false;
					log.verbose("bOrdCancel= Inside final else" + bOrdCancel);
				}
			}
		}

		log.verbose("End of AcademyOrderCancellationFromWCS.ValidateSTSLineEligibleForCancel() method");
		log.verbose("returning bchangeOrder=" + bOrdCancel);
		return bOrdCancel;
	}

	public List<String> getStatusList(Document docCommonCodeForCancellableLinestatus) {
		log.verbose("Start of AcademyOrderCancellationFromWCS.getStatusList() method");

		List<String> list = new ArrayList<String>();	
		NodeList nlCommonCode = docCommonCodeForCancellableLinestatus.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);

		for(int i=0;i <nlCommonCode.getLength(); i++) {

			Element eleCommonCode = (Element) nlCommonCode.item(i);
			String strCodeLongDesc = eleCommonCode.getAttribute(AcademyConstants.CODE_LONG_DESC);
			list.add(strCodeLongDesc);
		}

		log.verbose("End of AcademyOrderCancellationFromWCS.getStatusList() method");
		return list;
	}	
	//OMNI-30152 End


	/**
	 * Method returns the FulfillmentTypes that are eligible for Line Level Cancellations from WCS
	 * @throws Exception
	 */

	//Start : OMNI-41168, OMNI-41751
	public static ArrayList<String> fulfillmentTypesEligibleForCancellation(YFSEnvironment env) throws Exception {

		Document getCommonCodeListInDoc = null;
		Document getCommonCodeListOutDoc = null;
		ArrayList<String> arrFulfillmentType = new ArrayList<String>();

		try {
			getCommonCodeListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, AcademyConstants.ATTR_LINE_CANCEL_FROM_WCS);

			Document getCommonCodeListOPTempl = XMLUtil
					.getDocument("<CommonCode CodeValue=''/>");
			env.setApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST, getCommonCodeListOPTempl);

			getCommonCodeListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST,
					getCommonCodeListInDoc);
			env.clearApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST);
			if (getCommonCodeListOutDoc != null) {
				NodeList nlCommonCodeList = XPathUtil.getNodeList(getCommonCodeListOutDoc, AcademyConstants.XPATH_COMMONCODELIST_COMMONCODE);
				for(int i=0; i< nlCommonCodeList.getLength();i++) {
					Element eleCommonCode = (Element) nlCommonCodeList.item(i);
					arrFulfillmentType.add(eleCommonCode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return arrFulfillmentType;

	}
	//End : OMNI-41168, OMNI-41751
	//OMNI-84839 START
	/**This method to populate static commoncode value for WCS raeson codes
	 * 
	 * @param env
	 */
	private static void populateWCSResponseCodetaticMap(YFSEnvironment env){
		if(listWCSCSCancellationReasonCode.isEmpty()) {
			listWCSCSCancellationReasonCode.addAll(AcademyCommonCode.getCodeValueThreadSafeList(env, AcademyConstants.STR_CANCLTN_RSN_CODE_WCS, AcademyConstants.HUB_CODE));

		}
		log.verbose("listWCSCSCancellationReasonCode ::" + listWCSCSCancellationReasonCode);

	}
	//OMNI-84839 END
	/**
	 * This method validates if the order is a Chub PO and invokes Chub PO Cancel
	 * 
	 * @param env
	 * @param docChangeOrder
	 * @return Document
	 * @throws Exception
	 */
	private Document validateAndInvokeCHubCancellation(YFSEnvironment env, Document docChangeOrder, Document docCommonCodeForCancellationstatus,YFSException e) throws Exception {
		log.beginTimer("AcademyOrderCancellationFromWCS.validateAndInvokeCHubCancellation() method");
		log.verbose("Input : " + SCXmlUtil.getString(docChangeOrder));
		NodeList nlDSVLines = XPathUtil.getNodeList(docChangeOrder,	"/Order/OrderLines/OrderLine[@FulfillmentType='DROP_SHIP']");
		NodeList nlSOFLines = XPathUtil.getNodeList(docChangeOrder,	"/Order/OrderLines/OrderLine[@FulfillmentType='SOF']");
		log.verbose(" NodeList for DSV Lines is " + nlDSVLines.getLength() + "SOF Lines are : " + nlSOFLines.getLength());

		if (nlDSVLines.getLength() > 0 || nlSOFLines.getLength() > 0) {
			Document docOrderList = XMLUtil.createDocument(AcademyConstants.ELE_ORDER_LIST);

			//Check if the input contains the PO Details 		
			NodeList nlPODetails = XPathUtil.getNodeList(docChangeOrder, "/Order/OrderLines/OrderLine[@FulfillmentType='DROP_SHIP' and @PurchaseOrderNo!='']");
			if(nlPODetails.getLength() > 0 && (nlDSVLines.getLength() + nlSOFLines.getLength() == nlPODetails.getLength())) {
				log.verbose("All the PO details are present in request. using same ");
				for (int iDSVLine = 0; iDSVLine < nlDSVLines.getLength(); iDSVLine++) {
					Element eleDSVOrderLine = (Element) nlDSVLines.item(iDSVLine);
					docOrderList = prepareCHUBCancellationInput(docOrderList, 
							eleDSVOrderLine.getAttribute(AcademyConstants.ATTR_PURCHASE_ORDER_NO),
							eleDSVOrderLine.getAttribute(AcademyConstants.ITEM_ID),
							eleDSVOrderLine.getAttribute(AcademyConstants.ATTR_PURCHASE_ORDER_LINE_NO),
							eleDSVOrderLine.getAttribute(AcademyConstants.ATTR_CANCELLABLE_QUANTITY));
					log.verbose(" docOrderList-----> " + SCXmlUtil.getString(docOrderList));
				}
			}
			else {
				Document docGetOrderLineListOut = invokeGetOrderLineList(env, docChangeOrder);

				if (!YFCObject.isNull(docGetOrderLineListOut)) {
					log.verbose("CHUB Lines present on the ORder. Proceed with CHub cancellation");
					// Prepare input to invoke the CHUB Cancellation call
					NodeList nlDropShipLines = XPathUtil.getNodeList(docGetOrderLineListOut,
							"/OrderLineList/OrderLine");

					log.verbose(" : nlDropShipLines : " + nlDropShipLines.getLength());
					//Verify if radial line is present then new common code should be checked 
					for (int iDSVLine = 0; iDSVLine < nlDropShipLines.getLength(); iDSVLine++) {
						Element eleDSVOrderLine = (Element) nlDropShipLines.item(iDSVLine);

						String strChainedFromOrderLineKey = eleDSVOrderLine.getAttribute(AcademyConstants.ATTR_CHAINED_FROM_ORDER_LINE_KEY);
						Element eleOrderLine = (Element) XPathUtil.getNode(docChangeOrder,	"/Order/OrderLines/OrderLine[@OrderLineKey='"+strChainedFromOrderLineKey+"']");

						//Checking if the ORderline being considered has been requested in cancel
						if(!YFCObject.isVoid(eleOrderLine)) {
							log.verbose("Orderline being considered is present in request for cancel");
							String strVendorID = XPathUtil.getString(eleDSVOrderLine, "./Order/@VendorID");
							String strStatus = XPathUtil.getString(eleDSVOrderLine, "./ChainedFromOrderLine/@MaxLineStatus");
							if(!YFCObject.isVoid(strStatus) && strStatus.equals(AcademyConstants.VAL_CANCELLED_STATUS)) {
								strStatus = XPathUtil.getString(eleDSVOrderLine, "./ChainedFromOrderLine/@MinLineStatus");
							}
							eleDSVOrderLine.getAttribute(AcademyConstants.ATTR_MIN_LINE_STATUS);

							log.verbose(" VendorId is "+strVendorID);
							if(!YFCObject.isNull(strVendorID) && AcademyConstants.STR_CHUB.equals(strVendorID)) {
								String strPOOrderNo = XPathUtil.getString(eleDSVOrderLine, "./Order/@OrderNo");
								String strItemID = XPathUtil.getString(eleDSVOrderLine, "./ItemDetails/@ItemID");
								docOrderList = prepareCHUBCancellationInput(docOrderList, strPOOrderNo, strItemID,
										eleDSVOrderLine.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO),
										eleDSVOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY));
								log.verbose(" docOrderList-----> " + SCXmlUtil.getString(docOrderList));
							}

							else {
								log.verbose("Is Radial Line" );
								String strFulfillmentType = XPathUtil.getString(eleDSVOrderLine, "./ChainedFromOrderLine/@FulfillmentType");

								String strCodeLongDesc = XPathUtil.getString(
										docCommonCodeForCancellationstatus.getDocumentElement(),
										"//CommonCodeList/CommonCode[@CodeValue='MAX_RADIAL_LINE_CANCEL_STATUS']/@CodeLongDescription");
										
								//Start : DSV SOF Orderline not cancelled in PO Created Status
								if(!YFCObject.isVoid(strFulfillmentType) && strFulfillmentType
										.equalsIgnoreCase(AcademyConstants.STR_SPECIAL_ORDER_FIREARMS)) {
									strStatus = eleDSVOrderLine.getAttribute(AcademyConstants.ATTR_MIN_LINE_STATUS);
									strCodeLongDesc = XPathUtil.getString(
											docCommonCodeForCancellationstatus.getDocumentElement(),
											"//CommonCodeList/CommonCode[@CodeValue='MAX_RADIAL_SOF_LINE_CANCEL_STATUS']/@CodeLongDescription");
								}
								//End : DSV SOF Orderline not cancelled in PO Created Status

								log.verbose(" strCodeLongDesc : " + strCodeLongDesc + " : StrStatus :" + strStatus);

								if (!YFCObject.isVoid(strCodeLongDesc)) {
									Integer iCancellationAllowed = compareStatus(strCodeLongDesc, strStatus);
									log.verbose(" Status Compare for Radial Line vs common Code :" + iCancellationAllowed);

									if (iCancellationAllowed < 0) {
										e.setErrorCode("CAN0004");
										e.setErrorDescription("OrderLine is not eligible for cancellation");
										throw e;
									}
								}
							}
						}
					}
				}

				if(docOrderList.getElementsByTagName(AcademyConstants.ELE_ORDER).getLength() > 0) {
					// Invoke PO Cancellation
					Document docCHubResponse = AcademyUtil.invokeService(env,
							AcademyConstants.SERVICE_ACAD_MULTIPLE_PO_CANCEL, docOrderList);
					log.verbose(" Response from CHub " + SCXmlUtil.getString(docCHubResponse));

					NodeList nlSuccessResponse = XPathUtil.getNodeList(docCHubResponse,
							"/OrderList/Order[@Response='SUCCESS']");
					NodeList nlRetryResponse = XPathUtil.getNodeList(docCHubResponse,
							"/OrderList/Order[@Response='RETRY']");
					NodeList nlErrorResponse = XPathUtil.getNodeList(docCHubResponse,
							"/OrderList/Order[@Response='ERROR']");

					log.verbose(" ::nlErrorResponse message is:: " + nlErrorResponse);
					log.verbose(" ::nlSuccessResponse:: " + nlSuccessResponse.getLength());
					log.verbose(" ::nlRetryResponse:: " + nlRetryResponse.getLength());
					log.verbose(" ::nlErrorResponse:: " + nlErrorResponse.getLength());

					if (nlErrorResponse.getLength() > 0) {
						log.verbose(" Length of the error response " + nlErrorResponse.getLength());
						String errorDescription = "CHUB DSV Webservice Cancellation Failure";
						e.setErrorCode("CAN0008");
						e.setErrorDescription(errorDescription);
						log.info("DSV CHUB Cancellation Error :: Error while trying to cancel order from OMS. Error Logic:"+XMLUtil.getXMLString(docChangeOrder));
						throw e;
					}

					else if (nlRetryResponse.getLength() > 0) {
						log.verbose(" Length of the retry response " + nlRetryResponse.getLength());
						String retryDescription = "CHUB DSV Webservice Cancellation. Attempted to Cancel";
						e.setErrorCode("CAN0007");
						e.setErrorDescription(retryDescription);
						log.info("DSV CHUB Cancellation Error ::  Error while trying to cancel order from OMS. Retry Logic:"+XMLUtil.getXMLString(docChangeOrder));
						throw e;
					}

				}

			}
		}
		
		//Start : OMNI-106941 : Modification Rules allowing cancel from WCC
		//Adding Override of modification rules for DSV lines.
		docChangeOrder.getDocumentElement().setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);
		//End : OMNI-106941 : Modification Rules allowing cancel from WCC


		log.verbose("docChangeOrder at the end of the method"+ XMLUtil.getXMLString(docChangeOrder));
		log.endTimer("AcademyOrderCancellationFromWCS.validateAndInvokeCHubCancellation() method");
		return docChangeOrder;
	}


	/**
	 * This method prepares API input to get the corresponding PO details for DSV lines
	 * 
	 * @param env
	 * @param docChangeOrder
	 * @return Document
	 * @throws Exception
	 */
	private Document invokeGetOrderLineList(YFSEnvironment env, Document docChangeOrder) throws Exception {
		log.beginTimer("AcademyOrderCancellationFromWCS.invokeGetOrderLineList() method");
		log.verbose("Input to the invokeGetOrderLineList call : "+SCXmlUtil.getString(docChangeOrder));
		Document docGetOrderLineListOut = null;

		Document docGetOrderLineListInp = XMLUtil.createDocument(AcademyConstants.ELE_ORDER_LINE);
		Element eleOrderLineInp = docGetOrderLineListInp.getDocumentElement();

		eleOrderLineInp.setAttribute(AcademyConstants.ATTR_CHAINED_FROM_ORDER_HEADER_KEY, 
				docChangeOrder.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));

		Element eleOrderInp = docGetOrderLineListInp.createElement(AcademyConstants.ELE_ORDER);
		eleOrderLineInp.appendChild(eleOrderInp);

		eleOrderInp.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.DOCUMENT_TYPE_PO);

		log.verbose("Input to the API call : "+SCXmlUtil.getString(docGetOrderLineListInp));

		Document docGetOrderLineListTemplate = XMLUtil.getDocument(
				"<OrderLineList><OrderLine PrimeLineNo='' OrderedQty='' ItemID='' MinLineStatus='' ChainedFromOrderLineKey='' >"
						+ "<ChainedFromOrderLine  MinLineStatus='' MaxLineStatus='' FulfillmentType='' /> <ItemDetails ItemID='' />"
						+ "<Order OrderNo='' VendorID='' /></OrderLine></OrderLineList>");

		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LINE_LIST, docGetOrderLineListTemplate);
		docGetOrderLineListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LINE_LIST, docGetOrderLineListInp);
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LINE_LIST);

		log.endTimer("AcademyOrderCancellationFromWCS.invokeGetOrderLineList() method");
		return docGetOrderLineListOut;
	}


	/**
	 * This method prepares input for CHUB cancellation
	 * 
	 * @param env
	 * @param docChangeOrder
	 * @return Document
	 * @throws Exception
	 */
	private Document prepareCHUBCancellationInput(Document docOrderList, String strPOOrderNo, String strItemID, String strPrimeLineNo, String strQuantity) throws Exception {
		log.beginTimer("AcademyOrderCancellationFromWCS.prepareCHUBCancellationInput() method");
		log.verbose("Input : "+SCXmlUtil.getString(docOrderList));

		Element eleOrder = (Element) XPathUtil.getNode(docOrderList, "/OrderList/Order[@OrderNo='" + strPOOrderNo + "']");

		if(YFCObject.isVoid(eleOrder)) {
			log.verbose("Order Element not present for PO : " + strPOOrderNo);
			eleOrder = docOrderList.createElement(AcademyConstants.ELE_ORDER);
			eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO, strPOOrderNo);
			docOrderList.getDocumentElement().appendChild(eleOrder);
		}

		Element eleOrderLines = SCXmlUtil.getChildElement(eleOrder, AcademyConstants.ELE_ORDER_LINES);
		if(YFCObject.isVoid(eleOrderLines)) {
			log.verbose("OrderLine Element not present for PO : " + strPOOrderNo);
			eleOrderLines = docOrderList.createElement(AcademyConstants.ELE_ORDER_LINES);
			eleOrder.appendChild(eleOrderLines);
		}

		Element eleOrderLine = docOrderList.createElement(AcademyConstants.ELE_ORDER_LINE);
		eleOrderLines.appendChild(eleOrderLine);
		eleOrderLine.setAttribute(AcademyConstants.ATTR_ITEM_ID, strItemID);
		eleOrderLine.setAttribute(AcademyConstants.ATTR_PRIME_LINE_NO, strPrimeLineNo);
		eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, strQuantity);

		log.endTimer("AcademyOrderCancellationFromWCS.prepareCHUBCancellationInput() method");
		return docOrderList;
	}
	
	
}
