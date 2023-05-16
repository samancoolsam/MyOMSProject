package com.academy.ecommerce.sterling.api;

import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @Author Sumit Arora - Everest
 * @Date Created 01/mar/2023
 * 
 *       JIRAS - OMNI-101542
 * @Purpose OMS to cancel a line item and the whole shipment if all lines are
 *          canceled when a cancelation occurs in GSM a message and a message is
 *          sent to OMS.
 * 
 *          Input to Service
 * 
 *          <?xml version="1.0" encoding="UTF-8"?> <Shipment
 *          CustomerOrderNo="786409447" ItemID="022678973" Quantity="1" 
 *          ShipNode="165" Status="C" SterlingPONo="Y101083243" />
 * 
 **/

public class AcademyCancelOrderFromGSM {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyCancelOrderFromGSM.class);
	private static final String ACADEMY_GET_SHIPMENT_LINE_LIST_FOR_GSM_CANCELLATION = "AcademyGetShipmentLineListForGSMCancellation";
	private static final String ACADEMY_CALL_GET_ORDER_LIST_FOR_GSM_CANCELLATION = "AcademyCallgetOrderListForGSMCancellation";
	private Properties props;
	String strSerialNo =null;
	Boolean bValidSerial = false ;

	public void setProperties(Properties props) throws YFSException {
		this.props = props;
	}

	public Document processOrderCancellation(YFSEnvironment env, Document inXML) throws Exception {

		log.beginTimer("AcademyOrderCancellationFromGSM::processCompleteOrderCancellation");
		log.verbose("Entering the method AcademyOrderCancellationFromGSM.processOrderCancellation ");
		log.verbose("Input to the class AcademyOrderCancellationFromGSM " + XMLUtil.getXMLString(inXML));
		Document returndoc = null;
		Document docchangeOrderInput = null;
		YFSException e = new YFSException();
		String errorDescription = null;
		YFCException yfce = new YFCException("Error while cancelling the order");

		try {
			Document docgetOrderListOutput = null;
			Document docgetShipmentListForOrderOutput = null;

			Element elechangeOrderOrderLines = null;
			Element elechangeOrderOrderLine = null;
			Element eleRootchangeOrderInput = null;
			Element eleOrder = null;
			String strOrderNo = null;
			String strInpShipNode = null;
			String strReasonCode = null;
			String strNoteText = null;

			if (!YFCObject.isVoid(inXML)) {
				Element eleInRoot = inXML.getDocumentElement();
				strOrderNo = eleInRoot.getAttribute(AcademyConstants.STR_SOF_ACQDSP_CUSTOMER_ORDERNO);
				strInpShipNode = eleInRoot.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
				strSerialNo = eleInRoot.getAttribute(AcademyConstants.ATTR_SERIAL_NO);
				strNoteText = props.getProperty(AcademyConstants.ATTR_NOTE_TEXT);
				strReasonCode = props.getProperty(AcademyConstants.ATTR_REASON_CODE);

				if (YFCObject.isVoid(strOrderNo)) {
					yfce.setAttribute(AcademyConstants.ATTR_ERROR_CODE, AcademyConstants.STR_ERROR_CODE_CAN_VAL);
					yfce.setErrorDescription("Invalid input parameter Or Mandatory attributes are missing");
					throw yfce;

				} else {
					log.verbose("Calling callgetOrderListInputdoc method to invoke call getOrderList API : =");
					docgetOrderListOutput = callgetOrderListInputdoc(env, strOrderNo);
					log.verbose("Output of AcademyCallgetOrderListForWCSCancellation service  "
							+ XMLUtil.getXMLString(docgetOrderListOutput));

					NodeList nlGetOrderList = docgetOrderListOutput.getElementsByTagName(AcademyConstants.ELE_ORDER);
					if (nlGetOrderList.getLength() <= 0) {
						log.verbose("It is an invalid Order");
						log.verbose("It is an invalid Order");

						errorDescription = "YFS:Invalid Order";
						e.setErrorCode("YFS10003");
						e.setErrorDescription(errorDescription);
						throw e;
					}

					String strItemId = eleInRoot.getAttribute(AcademyConstants.ATTR_ITEM_ID);
					String strQuantity = eleInRoot.getAttribute(AcademyConstants.ATTR_QUANTITY);

					eleOrder = (Element) docgetOrderListOutput.getDocumentElement()
							.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
					Element eleOrderLine = SCXmlUtil.getXpathElement(eleOrder,
							"OrderLines/OrderLine[ItemDetails/@ItemID='" + strItemId + "']");
					if (YFCCommon.isVoid(eleOrderLine)) {
						yfce.setAttribute(AcademyConstants.ATTR_ERROR_CODE, AcademyConstants.STR_ERROR_CODE_CAN_VAL);
						yfce.setErrorDescription("Invalid input parameter Or Mandatory attributes are missing");
						throw yfce;
					}
					int iQuantity = Integer.parseInt(strQuantity);

					String strOrderLineKey = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
					String strFulfillmentType = eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
					String strPackListType = eleOrderLine.getAttribute(AcademyConstants.STR_PACK_LIST_TYPE);
					//Start : OMNI-105602 : SOF cancellation from GSM
					if (!AcademyConstants.STS_FA.equals(strPackListType)) {
						if (!AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equals(strFulfillmentType))
								{
									yfce.setAttribute("ErrorCode", "CAN0001");
									yfce.setErrorDescription("ItemID is not FireArm");
									throw yfce;
								}
					}
					//End : OMNI-105602 : SOF cancellation from GSM
					
					docgetShipmentListForOrderOutput = callShipmentLineListForOrder(env, strOrderLineKey);

					List<Element> eleShipmentLines = SCXmlUtil.getElements(
							docgetShipmentListForOrderOutput.getDocumentElement(), AcademyConstants.ELE_SHIPMENT_LINE);
					boolean isValidShipmentNotFound = true;
					if (!YFCCommon.isVoid(eleShipmentLines) && !eleShipmentLines.isEmpty()) {

						for (Element eleShipmentLine : eleShipmentLines) {
							bValidSerial = false ;
							String strShipmentLineKey = eleShipmentLine
									.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
							String strShipNode = SCXmlUtil.getXpathAttribute(eleShipmentLine,
									"Shipment[/ShipmentLines/ShipmentLine[@ShipmentLineKey='" + strShipmentLineKey
											+ "']]/@ShipNode");
							//Code Changes for OMNI-105373
							String strShipmentOrdLineKey = eleShipmentLine
									.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);		
							if (strShipNode.equals(strInpShipNode) && strOrderLineKey.equals(strShipmentOrdLineKey)) {
								String strStatus = SCXmlUtil.getXpathAttribute(eleShipmentLine, "Shipment/@Status")
										.substring(0, 4);
								int iStatus = Integer.parseInt(strStatus);
								String strShipmentKey = SCXmlUtil.getXpathAttribute(eleShipmentLine,
										"Shipment[/ShipmentLines/ShipmentLine[@ShipmentLineKey='" + strShipmentLineKey
												+ "']]/@ShipmentKey");
								String sOrderedQty = SCXmlUtil.getXpathAttribute(eleShipmentLine,
										"Shipment/ShipmentLines/ShipmentLine[@ShipmentLineKey='" + strShipmentLineKey
												+ "']/@Quantity");
								double doOrderedQty = Double.parseDouble(sOrderedQty);
								double dQuantity = iQuantity;
								double canOrderedQty = doOrderedQty - dQuantity;

								if (iStatus < 1400 && doOrderedQty >= dQuantity) {

									Document docChangeShipmentInput = XMLUtil
											.createDocument(AcademyConstants.ELE_SHIPMENT);
									Element eleChangeShipmentInput = docChangeShipmentInput.getDocumentElement();
									eleChangeShipmentInput.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
									eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_SELECT_METHOD,
											AcademyConstants.STR_WAIT);
									eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_OVERRIDE,
											AcademyConstants.STR_YES);
									eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_ACTION,
											AcademyConstants.STR_ACTION_MODIFY);
									//eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_CANCEL_REMOVED_QTY,
									//		AcademyConstants.STR_YES);
									eleChangeShipmentInput.setAttribute(
											AcademyConstants.ATTR_CANCEL_SHIPMENT_ON_ZERO_QTY,
											AcademyConstants.STR_YES);
									Element eleShpLines = docChangeShipmentInput.createElement("ShipmentLines");
									Element eleShpLine = docChangeShipmentInput
											.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
									eleShpLine.setAttribute(AcademyConstants.ATTR_ACTION,
											AcademyConstants.STR_ACTION_MODIFY);
									eleShpLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY,
											eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
									//OMNI-101537 Start
									if(AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE.equals(strFulfillmentType)) {
										
										Element eleShipmentTagSerialsInput = docChangeShipmentInput.createElement(AcademyConstants.ELE_SHIPEMNT_TAG_SERIALS);
										Element eleShipTagSerialsInDoc = (Element) docgetShipmentListForOrderOutput.getElementsByTagName(AcademyConstants.ELE_SHIPEMNT_TAG_SERIALS).item(0);
										XMLUtil.copyElement(docChangeShipmentInput, eleShipTagSerialsInDoc, eleShipmentTagSerialsInput);
										eleShpLine.appendChild(eleShipmentTagSerialsInput);
										eleShipmentTagSerialsInput.setAttribute(AcademyConstants.ATTR_REPLACE, AcademyConstants.STR_YES);
										
										NodeList ndShipmentTagRemove = XPathUtil.getNodeList(eleShipmentTagSerialsInput, AcademyConstants.ELE_SHIPMENT_TAG_SERIAL );
										for (int j=0;j<ndShipmentTagRemove.getLength();j++) {
											Element eleShipmentTagToRemove= (Element) ndShipmentTagRemove.item(j);
											String strSerialNoToRemove =  eleShipmentTagToRemove.getAttribute(AcademyConstants.ATTR_SERIAL_NO);
											if(strSerialNoToRemove.equalsIgnoreCase(strSerialNo)) {
												bValidSerial =true;
												log.verbose("Shipment Tag Serial to be removed :: " + XMLUtil.getElementXMLString(eleShipmentTagToRemove));
												eleShipmentTagToRemove.getParentNode().removeChild(eleShipmentTagToRemove);
											}
											
										}
										if(bValidSerial) {
											eleShpLine.setAttribute(AcademyConstants.ATTR_QUANTITY, "" + (doOrderedQty-1));
											XMLUtil.appendChild(eleShpLines, eleShpLine);
											XMLUtil.appendChild(eleChangeShipmentInput, eleShpLines);
											log.verbose("Input to changeShipment API :: "+ XMLUtil.getXMLString(docChangeShipmentInput));
											AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipmentInput);
											isValidShipmentNotFound = false;
											break;
										}
										else {
											throw new YFSException("Invalid Serial No");
										}
										
									}
									//OMNI-101537 End
									eleShpLine.setAttribute(AcademyConstants.ATTR_QUANTITY, "" + canOrderedQty);
									XMLUtil.appendChild(eleShpLines, eleShpLine);
									XMLUtil.appendChild(eleChangeShipmentInput, eleShpLines);
									log.verbose(
											"cHANGESHIPMENT Input  " + XMLUtil.getXMLString(docChangeShipmentInput));
									AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT,
											docChangeShipmentInput);
									isValidShipmentNotFound = false;
									break;
								}

							}
						}
						if (isValidShipmentNotFound) {
							yfce.setAttribute(AcademyConstants.ATTR_ERROR_CODE, AcademyConstants.STR_ERROR_CODE_CAN_VAL);
							yfce.setErrorDescription("Valid Shipment Not found");
							throw yfce;
						}

					} else if (AcademyConstants.STR_SHIP_TO_STORE.equals(strFulfillmentType) || AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE.equals(strFulfillmentType)) {
						yfce.setAttribute(AcademyConstants.ATTR_ERROR_CODE, AcademyConstants.STR_EXTN_ACADEMY_17);
						yfce.setErrorDescription(AcademyConstants.STR_SHIPMENT_DOES_NOT_EXIST);
						throw yfce;
					} 

						docchangeOrderInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
						eleRootchangeOrderInput = docchangeOrderInput.getDocumentElement();
						eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_SELECT_METHOD,
								AcademyConstants.STR_WAIT);
						eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);
						eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
								AcademyConstants.SALES_DOCUMENT_TYPE);
						eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,
								AcademyConstants.PRIMARY_ENTERPRISE);
						eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
						eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_MOD_REASON_CODE, strReasonCode);
						eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_ACTION,
								AcademyConstants.STR_ACTION_MODIFY);
						elechangeOrderOrderLines = SCXmlUtil.createChild(eleRootchangeOrderInput,
								AcademyConstants.ELE_ORDER_LINES);
						elechangeOrderOrderLine = SCXmlUtil.createChild(elechangeOrderOrderLines,
								AcademyConstants.ELE_ORDER_LINE);
						elechangeOrderOrderLine.setAttribute(AcademyConstants.ATTR_ACTION,
								AcademyConstants.STR_ACTION_MODIFY);
						Element elechangeOrderNotes = SCXmlUtil.createChild(elechangeOrderOrderLine,
								AcademyConstants.ELE_NOTES);
						Element elechangeOrderNote = SCXmlUtil.createChild(elechangeOrderNotes,
								AcademyConstants.ELE_NOTE);
						elechangeOrderNote.setAttribute(AcademyConstants.ATTR_OPERATION,
								AcademyConstants.STR_OPERATION_VAL_CREATE);
						elechangeOrderNote.setAttribute(AcademyConstants.ATTR_NOTE_TEXT, strNoteText);
						elechangeOrderNote.setAttribute(AcademyConstants.ATTR_REASON_CODE, strReasonCode);

						String sOrderedQty = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
						double doOrderedQty = Double.parseDouble(sOrderedQty);
						double dQuantity = iQuantity;
						doOrderedQty = doOrderedQty - dQuantity;
						elechangeOrderOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY,
								"" + (int) doOrderedQty);
						elechangeOrderOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, strOrderLineKey);

						eleRootchangeOrderInput.appendChild(elechangeOrderOrderLines);

						log.verbose("changeOrder Input  " + XMLUtil.getXMLString(docchangeOrderInput));
						returndoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, docchangeOrderInput);
						log.verbose("returndoc" + XMLUtil.getXMLString(returndoc));
						return returndoc;
					

				}
			}

		} catch (

		Exception y) {

			y.printStackTrace();

			if (!StringUtil.isEmpty(e.getErrorCode())) {
				throw e;

			} else if (!YFCCommon.isVoid((yfce.getAttribute(AcademyConstants.ATTR_ERROR_CODE)))) {
				throw yfce;
			} else {

				errorDescription = "Runtime Exception occurred while processing the cancellation Request.";
				YFSException yfse = new YFSException();
				yfse.setErrorCode("CAN0003");
				yfse.setErrorDescription(errorDescription);
				throw yfse;

			}
		}
		log.endTimer("AcademyOrderCancellationFromWCS::processCompleteOrderCancellation");
		return inXML;
	}

	/**
	 * @throws ParserConfigurationException
	 * @param env
	 * @param strOrderNo @return @throws
	 */
	public Document callgetOrderListInputdoc(YFSEnvironment env, String strOrderNo)
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
			docgetOrderListOutput = AcademyUtil.invokeService(env, ACADEMY_CALL_GET_ORDER_LIST_FOR_GSM_CANCELLATION,
					docgetOrderListInput);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.verbose("Output of AcademyCallgetOrderListForWCSCancellation service  "
				+ XMLUtil.getXMLString(docgetOrderListOutput));
		return docgetOrderListOutput;
	}

	/**
	 * This method is used to call the getShipmentLineList for OrderLine to get the
	 * Sales Shipment Line for cancellation
	 */
	public Document callShipmentLineListForOrder(YFSEnvironment env, String strOrderLineKey)
			throws ParserConfigurationException {
		// TODO Auto-generated method stub
		log.verbose("Entered into CallShipmentLineListForOrder method:: ");
		Document docShipmnetLineListInput = null;
		Document docShipmnetLineListOutput = null;
		Element eleRoot = null;
		docShipmnetLineListInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT_LINE);
		eleRoot = docShipmnetLineListInput.getDocumentElement();
		eleRoot.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, strOrderLineKey);

		log.verbose("Input for getShipmentLineList API service is " + XMLUtil.getXMLString(docShipmnetLineListInput));
		try {
			docShipmnetLineListOutput = AcademyUtil.invokeService(env,
					ACADEMY_GET_SHIPMENT_LINE_LIST_FOR_GSM_CANCELLATION, docShipmnetLineListInput);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.verbose(
				"Output of docgetShipmentLineListOutput service  " + XMLUtil.getXMLString(docShipmnetLineListOutput));
		return docShipmnetLineListOutput;
	}

}