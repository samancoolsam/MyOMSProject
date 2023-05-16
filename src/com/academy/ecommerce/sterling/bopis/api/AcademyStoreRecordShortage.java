package com.academy.ecommerce.sterling.bopis.api;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.ecommerce.yantriks.inventory.YASPostNodeControlToKafka;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyStoreRecordShortage {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyStoreRecordShortage.class);
	// Define properties to fetch argument value from service configuration
	private Properties props;

	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	//OMNI-50489 -- START
	Boolean eligibleForTOCreation = false;
	Boolean eligibleForSTORETOCreation = false;
	Boolean eligibleForDCTOCreation = false;
	String inventoryCheckRequired="N";
	//OMNI-50489 -- END
	
	/**
	 * This method will call changeShipment api to cancel the Shipment. Then calls
	 * changeOrder api to add Notes for cancelled orderliness. If it's a BOPIS line,
	 * OrderedQty will also be updated for this change Order api call to cancel the
	 * shortage Quantity. Along with cancelling the short picked quantity on the
	 * item, there will be modelling in OMS to also mark the item + node combination
	 * as 'dirty' Marking Item Node Combination Dirty will be done based on a common
	 * code. DIRTY_NODE_REASONS.
	 * 
	 * @param env
	 * @param inXML
	 */
	public Document recordshortageFromStore(YFSEnvironment env, Document inXML) {
		log.beginTimer("AcademyStoreRecordShortage::recordshortageFromStore");
		log.verbose("Input of AcademyStoreRecordShortage.recordshortageFromStore  :: "+XMLUtil.getXMLString(inXML));

		try {
			Document changeShipmentInputDoc = null;
			Document changeShipmentOutDoc = null;
			Document docchangeOrderInput = null;
			Element eleRootchangeOrderInput = null;
			Element eleOrderLines = null;
			String strStatus = null;
			String strShipmentKey = null;
			String strDeliveryMethod = null;
			String strShipNode = null;
			Double dOldShortedQty = 0.00;
			String strModificationReasonCode = null;
			String strOrderHeaderKey = null;
			String strShipmentStatus = null;
			String strShipmentContainerizedFlag = null;
			// BOPIS-1262 StartOver Issue - Start
			String strIncludedInBatch = "";
			// BOPIS-1262 StartOver Issue - End
			Boolean bisCustPickCancellation = false;
			Boolean bisPackingScreen = false;
			Boolean bOtherStoreCancel = false;
			Boolean bcancelShipment = true;
			HashMap<String, String> shortageReasonMap = new HashMap<String, String>();
			HashMap<String, String> shortageQtyMap = new HashMap<String, String>();
			HashMap<String, String> orderLineKeyMap = new HashMap<String, String>();
			HashMap<String, String> orderedQtyMap = new HashMap<String, String>();
			HashMap<String, String> itemDetailsMap = new HashMap<String, String>();
			//OMNI-81672 Start
			HashMap<String, String> hmpOriginalOrderedQty = new HashMap<String, String>();
			//OMNI-81672 End
			// OMNI-70080 - STS Resourcing Start
			String strNodeType = null;
			String strDocumentType = null;
			HashMap<String, String> originalFulfillmentMap = new HashMap<String, String>();
			int iNoOfResourcingHops = 0;
			String strResourcingHops = props.getProperty(AcademyConstants.STS_RESOURCING_HOPS);
			if (!YFCObject.isVoid(strResourcingHops)) {
				iNoOfResourcingHops = Integer.parseInt(strResourcingHops);
			}
			log.verbose("No of STS Resourcing Hops" + iNoOfResourcingHops);
			// OMNI-70080 - STS Resourcing End

			Element eleRoot = inXML.getDocumentElement();
			// OMNI-4017 BOPIS: Cancellation Emails to be order level - START
			String isWebStoreFlag = eleRoot.getAttribute(AcademyConstants.STR_IS_SHORTED_FROM_WEB_STORE);
			// OMNI-4017 BOPIS: Cancellation Emails to be order level - END
			// BOPIS-1262 StartOver Issue - Start
			if (eleRoot.hasAttribute("IncludedInBatch")) {
				strIncludedInBatch = eleRoot.getAttribute("IncludedInBatch");
			}
			// BOPIS-1262 StartOver Issue - End
			
			// OMNI-70080 - STS Resourcing
			String  strSTS2Short = eleRoot.getAttribute("IsSTS2ShipmentShort");
			log.verbose("IsSTS2ShipmentShort From Input :: " + strSTS2Short);
			boolean isSTS2Short = false;
			if (!YFCObject.isVoid(strSTS2Short) && AcademyConstants.STR_YES.equals(strSTS2Short) && iNoOfResourcingHops > 0) {
				isSTS2Short = true;
			}
			log.verbose("IsSTS2ShipmentShort :: " + isSTS2Short);
			// OMNI-70080 - STS Resourcing

			// BOPIS-1262 StartOver Issue - Added condition to remove Assignment only if the
			// Shipment is from Batch
			if (eleRoot.hasAttribute(AcademyConstants.ATTR_ASSIGNED_TO_USER_ID)) {
				if (!AcademyConstants.STR_NO.equalsIgnoreCase(strIncludedInBatch)) {
					eleRoot.removeAttribute(AcademyConstants.ATTR_ASSIGNED_TO_USER_ID);
					log.verbose("Removing the AssignedToUserId attribute");
				}
			}
			if (YFCCommon.equalsIgnoreCase("StoreBatch", eleRoot.getTagName())) {
				Document outDocCycleCount = AcademyUtil.invokeService(env, "AcademyBOPISPrepareCycleCountMsgToSIM",
						inXML);
				log.verbose("StoreBatch ::");

				setDirtyNodeForShrtgOfStoreBatch(env, inXML);
				return inXML;
			} else if (eleRoot.hasAttribute(AcademyConstants.STR_EXTN_REASON_CODE)
					&& !YFCCommon.isVoid(eleRoot.getAttribute(AcademyConstants.STR_EXTN_REASON_CODE))) {
				// Other Store Order Cancellation
				bOtherStoreCancel = true;
				strShipmentKey = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
				inXML = formChangeShipmentInDoc(env, strShipmentKey,
						eleRoot.getAttribute(AcademyConstants.STR_EXTN_REASON_CODE));
				// BOPIS-1090 ::Begin
				if (inXML.getDocumentElement().hasAttribute(AcademyConstants.ATTR_ERROR_MESSAGE)) {
					return inXML;
				}
				// BOPIS-1090 ::End

			}

			String strRCP = props.getProperty(AcademyConstants.STR_READY_FOR_CUSTOMER_PICKUP_);
			String strInvPictureIncorrectTillDateConstant = props
					.getProperty(AcademyConstants.STR_INV_PICTURE_INCORRECT_TILL_DATE_CONSTANT);
			strStatus = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_STATUS);
			strShipmentKey = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			if (!YFCObject.isVoid(strStatus)) {
				if (strStatus.equalsIgnoreCase(strRCP)
						|| strStatus.equalsIgnoreCase(AcademyConstants.STR_PAPER_WORK_INITIATED_STATUS)) {
					changeShipmentInputDoc = formChangeShipmentInDocForCustPickShortage(env, inXML);
					bisCustPickCancellation = true;

				} else if (strStatus.equalsIgnoreCase("1100.70.06.70")) {
					// Packing Screen as status is shipment being packed
					bisPackingScreen = true;
					changeShipmentInputDoc = inXML;
				}

			} else {

				// Start BOPIS::1939
				Element eleShipmentLineforoldshtgqty = (Element) inXML.getDocumentElement()
						.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE).item(0);
				if (eleShipmentLineforoldshtgqty.hasAttribute(AcademyConstants.ATTR_OLD_SHORTAGE_QTY)) {
					String stroldshortedqty = eleShipmentLineforoldshtgqty
							.getAttribute(AcademyConstants.ATTR_OLD_SHORTAGE_QTY);
					dOldShortedQty = Double.parseDouble(stroldshortedqty);

					log.verbose("Old Shortage Qty for a ShipmentLine" + stroldshortedqty);
					eleShipmentLineforoldshtgqty.removeAttribute(AcademyConstants.ATTR_OLD_SHORTAGE_QTY);
				}

				// End BOPIS::1939

				changeShipmentInputDoc = inXML;
			}
			// OMNI-70080 - STS Resourcing
			if (isSTS2Short) {
				env.setTxnObject(AcademyConstants.IS_STS2_SHIPMENT, strSTS2Short);
			}
			// OMNI-70080 - STS Resourcing
			log.verbose("cHANGESHIPMENT Input  " + XMLUtil.getXMLString(changeShipmentInputDoc));
			changeShipmentOutDoc = AcademyUtil.invokeService(env, AcademyConstants.SERVICE_CALL_CHANGE_SHIPMENT_API,
					changeShipmentInputDoc);
			log.verbose("cHANGESHIPMENT Output  " + XMLUtil.getXMLString(changeShipmentOutDoc));
			strShipmentStatus = changeShipmentOutDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_STATUS);
			strShipmentContainerizedFlag = changeShipmentOutDoc.getDocumentElement()
					.getAttribute("ShipmentContainerizedFlag");

			Element elechangeShipmentOut = changeShipmentOutDoc.getDocumentElement();
			String strStatusbp = elechangeShipmentOut.getAttribute(AcademyConstants.ATTR_STATUS);
			
			
			if (strStatusbp.equalsIgnoreCase("1100.70.06.20")) {
				Element eleinXML = inXML.getDocumentElement();
				eleinXML.setAttribute(AcademyConstants.ATTR_STATUS, strStatusbp);
			}

			NodeList NLShipmentLine = inXML.getDocumentElement()
					.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

			for (int i = 0; i < NLShipmentLine.getLength(); i++) {
				Element EleNLShipmentLine = (Element) NLShipmentLine.item(i);
				String strShipmentLineKey = EleNLShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
				String strShortageQty = EleNLShipmentLine.getAttribute(AcademyConstants.ATTR_SHORTAGE_QTY);
				Element eleExtn = (Element) EleNLShipmentLine.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
				String strExtnReasonCode = eleExtn.getAttribute(AcademyConstants.STR_EXTN_REASON_CODE);
				strModificationReasonCode = eleExtn.getAttribute(AcademyConstants.STR_EXTN_REASON_CODE);
				if (!YFCObject.isVoid(strExtnReasonCode)) {
					shortageReasonMap.put(strShipmentLineKey, strExtnReasonCode);
					log.verbose("Adding ExtnReasonCode=" + strExtnReasonCode + "in Map for the ShipmentKey"
							+ strShipmentLineKey);
				}
				if (!YFCObject.isVoid(strShortageQty)) {
					Double dShortageQty = Double.parseDouble(strShortageQty);
					if (dShortageQty > 0.00) {
						shortageQtyMap.put(strShipmentLineKey, strShortageQty);
						log.verbose("Adding strShortageQty=" + strShortageQty + "in Map for the ShipmentKey"
								+ strShipmentLineKey);
					}
				}

			}

			if (bisCustPickCancellation || bisPackingScreen || bOtherStoreCancel) {
				NodeList ShipmentLineNL = changeShipmentOutDoc.getDocumentElement()
						.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
				for (int j = 0; j < ShipmentLineNL.getLength(); j++) {
					Element EleNLShipmentLine = (Element) ShipmentLineNL.item(j);
					String strQuantity = EleNLShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
					Double dstrQuantity = Double.parseDouble(strQuantity);

					if (!YFCObject.isVoid(strQuantity) && dstrQuantity != 0.00) {
						bcancelShipment = false;
						break;
					}
				}
			}
			log.verbose("bcancelShipment= " + bcancelShipment);
			if ((bcancelShipment) && (bisCustPickCancellation || bisPackingScreen || bOtherStoreCancel)) {
				// <Shipment Action="Cancel" ShipmentKey="20180531144044483573"/>
				Document docChangeShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
				Element eleChangeShipmentInput = docChangeShipment.getDocumentElement();
				eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.VAL_CANCEL);
				eleChangeShipmentInput.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
				log.verbose("cHANGESHIPMENT Input when bcancelShipment is true " + XMLUtil.getXMLString(docChangeShipment));
				Document changeShipmentOutDocForPackingScreen = AcademyUtil.invokeService(env,
						AcademyConstants.SERVICE_CALL_CHANGE_SHIPMENT_API, docChangeShipment);
				strShipmentStatus = changeShipmentOutDocForPackingScreen.getDocumentElement()
						.getAttribute(AcademyConstants.ATTR_STATUS);
			}

			strDeliveryMethod = changeShipmentOutDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
			strShipNode = changeShipmentOutDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE);
			// OMNI-70080 - STS Resourcing
			strDocumentType = elechangeShipmentOut.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
			NodeList nlShipNode = elechangeShipmentOut.getElementsByTagName("ShipNode");
			if (!YFCObject.isVoid(nlShipNode) && nlShipNode.getLength() > 0) {
				Element eleShipNode = (Element) nlShipNode.item(0);
				strNodeType = eleShipNode.getAttribute("NodeType");
			}
			log.verbose("Shipment Document Type - " + strDocumentType);
			log.verbose("Shipment Ship Node Type -" + strNodeType);
			boolean isSTS2Shipment = false;
			if (AcademyConstants.STR_SHP.equals(strDeliveryMethod)
					&& AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE.equals(strDocumentType)
					&& AcademyConstants.STR_STORE.equals(strNodeType) && isSTS2Short) {
				log.verbose("STS Resourcing true");
				isSTS2Shipment = true;
				strShipNode = elechangeShipmentOut.getAttribute(AcademyConstants.ATTR_RECV_NODE);
				log.verbose("Set Shipment Receiving Node as ShipNode - " + strShipNode);
			}
			// OMNI-70080 - STS Resourcing
			
			// Element eleShipLine = (Element)
			// changeShipmentOutDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE).item(0);
			Element eleOrdrLine = (Element) changeShipmentOutDoc.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE)
					.item(0);
			strOrderHeaderKey = eleOrdrLine.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
			// OMNI-70080 - STS Resourcing
			if (isSTS2Shipment) {
				strOrderHeaderKey = eleOrdrLine.getAttribute(AcademyConstants.ATTR_CHAINED_FROM_ORDER_HEADER_KEY);
				log.verbose("STS Resourcing SO Order Header Key - " + strOrderHeaderKey);
			}
			// OMNI-70080 - STS Resourcing
			NodeList ShipmentLine = changeShipmentOutDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			for (int k = 0; k < ShipmentLine.getLength(); k++) {
				Element EleNLShipmentLine = (Element) ShipmentLine.item(k);
				String strShipLineKey = EleNLShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
				Element eleOrderLine = (Element) EleNLShipmentLine.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE).item(0);
				String strOrderlineKey = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
				String strqty = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
				String strItemID = EleNLShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID);
				//OMNI-81672 Start
				String strOriginalOrderedqty = null;
				//OMNI-81672 End
				// OMNI-70080 - STS Resourcing
				String strOriginalFulfillment = "";
				if (isSTS2Shipment) {
					strOrderlineKey = eleOrderLine.getAttribute(AcademyConstants.ATTR_CHAINED_FROM_ORDER_LINE_KEY);
					NodeList nlChainedFromOL = eleOrderLine.getElementsByTagName("ChainedFromOrderLine");
					if (!YFCObject.isVoid(nlChainedFromOL) && nlChainedFromOL.getLength() > 0) {
						Element eleChainedOL = (Element) nlChainedFromOL.item(0);
						strqty = eleChainedOL.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
						//OMNI-81672 Start
						strOriginalOrderedqty = eleChainedOL.getAttribute(AcademyConstants.ATTR_ORIGL_ORDERED_Qty);
						//OMNI-81672 End
						NodeList nlExtn = eleChainedOL.getElementsByTagName("Extn");
						if (!YFCObject.isVoid(nlExtn) && nlExtn.getLength() > 0) {
							Element eleExtn = (Element) nlExtn.item(0);
							strOriginalFulfillment = eleExtn.getAttribute(AcademyConstants.EXTN_ORIGINAL_FULFILLMENT_TYPE);
						}
						log.verbose("STS Resourcing SO Order Line Key - " + strOrderlineKey + " SO order qty - " + strqty);
					}
				}
				// OMNI-70080 - STS Resourcing
				// String
				// strUnitOfMeasure=EleNLShipmentLine.getAttribute(AcademyConstants.ATTR_UOM);
				// String
				// strProductClass=EleNLShipmentLine.getAttribute(AcademyConstants.ATTR_PROD_CLASS);

				orderLineKeyMap.put(strShipLineKey, strOrderlineKey);
				orderedQtyMap.put(strOrderlineKey, strqty);
				itemDetailsMap.put(strShipLineKey, strItemID);
				//OMNI-81672 Start
				if(YFCObject.isNull(strOriginalOrderedqty)){
					hmpOriginalOrderedQty.put(strOrderlineKey, strqty);
				}else {
					hmpOriginalOrderedQty.put(strOrderlineKey, strOriginalOrderedqty);
				}
				//OMNI-81672 End
				// orderLineKeyMap for the ShipmentKey"+strShipLineKey);
				log.verbose("Adding strOrderlineKey= " + strOrderlineKey + " in orderLineKeyMap for the ShipmentKey "
						+ strShipLineKey);

				// strOrderlineKey"+strOrderlineKey);
				log.verbose("Adding strqty= " + strqty + " in orderedQtyMap for the strOrderlineKey " + strOrderlineKey);

				// strShipLineKey"+strShipLineKey);
				log.verbose(
						"Adding strItemID= " + strItemID + " in itemDetailsMap for the strShipLineKey " + strShipLineKey);
				
				// strOrderlineKey"+strOrderlineKey);
				log.verbose("Adding strOriginalOrderedqty= " + strOriginalOrderedqty + " in originalOrderedQtyMap for the strOrderlineKey " + strOrderlineKey);

				// OMNI-70080 - STS Resourcing - add in Map if Original Fulfillment type is not empty
				// Map will contain OrderLineKeys which has original Fulfillment type as BOPIS
				if (!YFCObject.isVoid(strOriginalFulfillment)) {
					originalFulfillmentMap.put(strOrderlineKey, strOriginalFulfillment);
					log.verbose(
							"Adding OriginalfulfillmentType= " + strOriginalFulfillment + " in originalFulfillmentMap for the strOrderlineKey " + strOrderlineKey);
				}
				// OMNI-70080 - STS Resourcing
			}
			docchangeOrderInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			eleRootchangeOrderInput = docchangeOrderInput.getDocumentElement();
			eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
			eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_MOD_REASON_CODE, strModificationReasonCode);
			eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_OVERRIDE,"Y");
			// eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_ACTION,AcademyConstants.STR_CANCEL);
			eleOrderLines = docchangeOrderInput.createElement(AcademyConstants.ELE_ORDER_LINES);
			eleRootchangeOrderInput.appendChild(eleOrderLines);
			for (String ShipmentLineKey : shortageReasonMap.keySet()) {
				log.verbose("Short Shipment Line Key - " + ShipmentLineKey);
				Element eleChangeOrderLine = docchangeOrderInput.createElement(AcademyConstants.ELE_ORDER_LINE);
				eleOrderLines.appendChild(eleChangeOrderLine);
				String eligibleForAlternatePick = "N";
				String orderlinekey = null;
				if (orderLineKeyMap.containsKey(ShipmentLineKey) == true) {
					orderlinekey = orderLineKeyMap.get(ShipmentLineKey);
					log.verbose("Short Order Line Key - " + orderlinekey);
					eleChangeOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, orderlinekey);
					Element elechangeOrderNotes = docchangeOrderInput.createElement(AcademyConstants.ELE_NOTES);
					eleChangeOrderLine.appendChild(elechangeOrderNotes);
					Element elechangeOrderNote = docchangeOrderInput.createElement(AcademyConstants.ELE_NOTE);
					elechangeOrderNotes.appendChild(elechangeOrderNote);
					elechangeOrderNote.setAttribute(AcademyConstants.ATTR_OPERATION,AcademyConstants.STR_OPERATION_VAL_CREATE);
					elechangeOrderNote.setAttribute(AcademyConstants.ATTR_NOTE_TEXT,shortageReasonMap.get(ShipmentLineKey));
					elechangeOrderNote.setAttribute(AcademyConstants.ATTR_REASON_CODE,shortageReasonMap.get(ShipmentLineKey));
				}
				
				// OMNI-70080 - STS Resourcing - added STS Condition and check if OriginalFulfillmentMap does not contain the orderline
				if (!YFCObject.isVoid(strDeliveryMethod)&& (strDeliveryMethod.equalsIgnoreCase(AcademyConstants.STR_PICK) || 
						(isSTS2Shipment && !originalFulfillmentMap.containsKey(orderlinekey)))) {
					log.verbose("Calling ChangeOrder for BOPIS Line");
					String orderedQTy = orderedQtyMap.get(orderlinekey);
					String shortageQty = shortageQtyMap.get(ShipmentLineKey);
					Double dorderedQTy = Double.parseDouble(orderedQTy);
					Double doshortageQty = Double.parseDouble(shortageQty);
					Double dcurrentShortageqty = doshortageQty - dOldShortedQty;
					log.verbose("Current Shortage Qty for a ShipmentLine" + dcurrentShortageqty);
					Double corderedQTy = Double.parseDouble(orderedQTy);
					log.verbose("Ordered quantity is"+corderedQTy);
					dorderedQTy = dorderedQTy - dcurrentShortageqty;
					orderedQTy = String.valueOf(dorderedQTy);
					String strOrderedQty=String.valueOf(corderedQTy);
					String strdcurrentShortageqty=String.valueOf(dcurrentShortageqty);
					//OMNI-81672 Start
					String strOriginalOrdQty = hmpOriginalOrderedQty.get(orderlinekey);
					Double dOriginalOrderedQty = Double.parseDouble(strOriginalOrdQty);
					log.verbose("Original Ordered quantity is"+ dOriginalOrderedQty);
					String strOriginalOrderedQty=String.valueOf(dOriginalOrderedQty);
					//OMNI-81672 End
					log.verbose("Shipment status : " +strShipmentStatus );


					if ((!YFCObject.isVoid(strShipmentStatus) && strShipmentStatus.equalsIgnoreCase(AcademyConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL))
							|| (!YFCObject.isVoid(strShipmentStatus) && strShipmentStatus.equalsIgnoreCase(AcademyConstants.STATUS_BACKROOM_PICK_IN_PROGRESS_VAL))){
						
						//if (strdcurrentShortageqty.equals(strOrderedQty)) {
						//OMNI-81672 Start
						if (strdcurrentShortageqty.equals(strOriginalOrderedQty)) {
						//OMNI-81672 End
							log.verbose("Complete Line Shortage");
							Map<String, String> mapSaveTheSalePermissions = getSaveTheSalePermissions(env);
							String strBOPISSaveTheSaleFlag = mapSaveTheSalePermissions.get("STS");
							String strSaveTheSaleStoreEnabled = mapSaveTheSalePermissions.get("STS_STORE");
							String strSaveTheSaleDCEnabled = mapSaveTheSalePermissions.get("STS_DC");
							String strSaveTheSaleAlternatePickEnabled = mapSaveTheSalePermissions.get("STS_AS");
							
							String strOriginalFulfillment = originalFulfillmentMap.get(orderlinekey);
							Document docOrderLineList = getOrderLineList(env, orderlinekey, strOrderHeaderKey);
							boolean isAlternateStoreShortPicked = alternateStoreShortPicked(strOriginalFulfillment,docOrderLineList,strSaveTheSaleAlternatePickEnabled);
							boolean isSaveTheSaleBOPISEnabled = !YFCObject.isVoid(strBOPISSaveTheSaleFlag) && strBOPISSaveTheSaleFlag.equalsIgnoreCase(AcademyConstants.BOPIS_STS_ENABLED);
							log.verbose("Is BOPIS Save The Sale Feature Enabled :: " + isSaveTheSaleBOPISEnabled);
							log.verbose("Is Alternate Pick got Shortpicked :: " + isAlternateStoreShortPicked);
							
							// OMNI-70080 - STS Resourcing - added STS Condition
							if (( isSaveTheSaleBOPISEnabled && !isAlternateStoreShortPicked ) || isSTS2Shipment) {
								log.verbose("BOPIS SAVE THE SALE FLAG is Y");
								log.verbose("Validating Item and ShipNode eligibility for TO creation");
								
								/*  Rewrite this method to  update various global below values,
								 *  eligibleForTOCreation=FALSE
								 *  eligibleForDCTOCreation = FALSE
								 *  eligibleForSTORETOCreation= FALSE
									*/
								// OMNI-70080 - STS Resourcing
								eligibleForTransferOrderCreation(env, itemDetailsMap, strShipNode, changeShipmentInputDoc, isSTS2Shipment);
								
								//OMNI-61146 Creating new doc and setting attributes for dropping msg to Async Schedule queue for STS3.0 START

								Document docScheduleSaveTheSale = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
								Element eleScheduleSaveTheSale=docScheduleSaveTheSale.getDocumentElement();
								if(!YFCCommon.isVoid(strOrderHeaderKey))eleScheduleSaveTheSale.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
								if(!YFCCommon.isVoid(strShipmentKey))eleScheduleSaveTheSale.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
								Element eleSchSTSOrderLines=docScheduleSaveTheSale.createElement(AcademyConstants.ELE_ORDER_LINES);
								eleScheduleSaveTheSale.appendChild(eleSchSTSOrderLines);
								Element eleSchSTSOrderLine=eleSchSTSOrderLines.getOwnerDocument().createElement(AcademyConstants.ELE_ORDER_LINE);
								if(!YFCCommon.isVoid(orderlinekey))eleSchSTSOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, orderlinekey);
								// OMNI-105693 - Fix
								eleSchSTSOrderLine.setAttribute(AcademyConstants.ATTR_REASON_CODE,shortageReasonMap.get(ShipmentLineKey));
								// OMNI -105693 - Fix
								if(!YFCCommon.isVoid(ShipmentLineKey))eleSchSTSOrderLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, ShipmentLineKey);
								if(!YFCCommon.isVoid(strShipNode))eleSchSTSOrderLine.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strShipNode);
								eleSchSTSOrderLines.appendChild(eleSchSTSOrderLine);
								Element eleSchSTSItem=eleSchSTSOrderLine.getOwnerDocument().createElement(AcademyConstants.ELEM_ITEM);
								String strItemId = itemDetailsMap.get(ShipmentLineKey);
								eleSchSTSItem.setAttribute(AcademyConstants.ATTR_ITEM_ID, strItemId);
								eleSchSTSOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, strOriginalOrderedQty);
								eleSchSTSOrderLine.appendChild(eleSchSTSItem);
								
								//OMNI-61146 Creating new doc and setting attributes for dropping msg to Async Schedule queue for STS3.0 END
								if (eligibleForTOCreation == true) { 
									log.verbose("Item and ShipNode are eligible for TO creation");
									
									// OMNI-70080 - STS Resourcing - check No of Hops
									if (isSTS2Shipment) {
										int iNoOfResourcingPicks = getNoOfResourcingPicks(env, orderlinekey);
										if (iNoOfResourcingPicks >= iNoOfResourcingHops) {
											log.verbose("No of hops exceeded !! No SO Short Picks - "
													+ iNoOfResourcingPicks + " Max Hops - " + iNoOfResourcingHops);
											eleChangeOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY,
													orderedQTy);
											continue;
										}
									   if(strSaveTheSaleAlternatePickEnabled.equalsIgnoreCase("Y")) eligibleForAlternatePick = "N";
									}else {
										if(strSaveTheSaleAlternatePickEnabled.equalsIgnoreCase("Y")) eligibleForAlternatePick = "Y";
									}
									eleScheduleSaveTheSale.setAttribute(AcademyConstants.ATTR_INVENTORY_CHECK_REQUIRED, inventoryCheckRequired);
									eleScheduleSaveTheSale.setAttribute("AlternateStorePick", eligibleForAlternatePick);
									eleScheduleSaveTheSale.setAttribute("SaveTheSale", "Y");
									// OMNI-70080 - STS Resourcing
									Boolean boolDCValidated = false;
									Boolean boolStoreValidated = false;
									Element elechangeOrderLineExtn = docchangeOrderInput.createElement(AcademyConstants.ELE_EXTN);
									eleChangeOrderLine.appendChild(elechangeOrderLineExtn);
									if(!YFCObject.isVoid(eligibleForDCTOCreation) && eligibleForDCTOCreation==true && "Y".equalsIgnoreCase(strSaveTheSaleDCEnabled)) { 
										Document strDCMappingOutputDoc = null;
										NodeList nlStoreDCMappings = null;
											log.verbose("Fetching the mapped DC shipnode for the store");
											strDCMappingOutputDoc = getStoreDCMapping(env, strShipNode);											
											if(!YFCObject.isVoid(strDCMappingOutputDoc)) {
													nlStoreDCMappings = strDCMappingOutputDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
													if(nlStoreDCMappings.getLength()>0) {														
														Element eleCommonCode = (Element) nlStoreDCMappings.item(0);
														String strShipnodeMapped = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
														eleChangeOrderLine.setAttribute(AcademyConstants.ATTR_PROCURE_FROM_NODE, strShipnodeMapped);
												        eleSchSTSOrderLine.setAttribute(AcademyConstants.ATTR_PROCURE_FROM_NODE, strShipnodeMapped);//Change for OMNI-61616 STS3.0 ,adding procureFromNode for DC to the queue input
														boolDCValidated=true;
												}
											
										} 
									//OMNI-50489 -- START
									} else if (!YFCObject.isVoid(eligibleForSTORETOCreation) && eligibleForSTORETOCreation==true && "Y".equalsIgnoreCase(strSaveTheSaleStoreEnabled)) {
											//Document docOrderLineList = getOrderLineList(env, orderlinekey,strOrderHeaderKey);
											Element eleOrderLineList = docOrderLineList.getDocumentElement();
											String sSourceControlNode = XPathUtil.getString(eleOrderLineList,"//OrderLineList/OrderLine/@ShipNode");
											boolStoreValidated = true;
											eleChangeOrderLine.setAttribute(AcademyConstants.ATTR_PROCURE_FROM_NODE, "");
										    eleSchSTSOrderLine.setAttribute(AcademyConstants.ATTR_PROCURE_FROM_NODE, ""); //Change for OMNI-61616 STS3.0 ,adding procureFromNode for store to the queue input
											eleChangeOrderLine.setAttribute("IsProcurementAllowed", "Y");
						                	Element eleOrderLineSourcingCntrls = docchangeOrderInput.createElement(AcademyConstants.ELE_ORDER_LINE_SOURCING_CNTRLS);
						                	Element eleOrderLineSourcingCntrl = docchangeOrderInput.createElement(AcademyConstants.ELE_ORDER_LINE_SOURCING_CNTRL);
						                	eleOrderLineSourcingCntrl.setAttribute("Node", sSourceControlNode);
						                	eleOrderLineSourcingCntrl.setAttribute("SuppressSourcing", "N");
						                	eleOrderLineSourcingCntrl.setAttribute("InventoryCheckCode", "NOINV");
											eleOrderLineSourcingCntrls.appendChild(eleOrderLineSourcingCntrl);					                	
											eleChangeOrderLine.appendChild(eleOrderLineSourcingCntrls);
										}

									//OMNI-50489 -- END
										if(boolDCValidated || boolStoreValidated){		 				
											eleChangeOrderLine.setAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE, "STS");
											Element elechangeOrderHoldTypes = docchangeOrderInput.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPES);
											eleChangeOrderLine.appendChild(elechangeOrderHoldTypes);
											Element elechangeOrderHoldType = docchangeOrderInput.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPE);
											elechangeOrderHoldTypes.appendChild(elechangeOrderHoldType);
											elechangeOrderHoldType.setAttribute(AcademyConstants.ATTR_HOLD_TYPE,AcademyConstants.STR_STS_RELEASE_HOLD);
											elechangeOrderHoldType.setAttribute(AcademyConstants.ATTR_STATUS,AcademyConstants.STR_HOLD_CREATED_STATUS);	
											
											Element eleSchSTSOrderLineHoldTypes = docScheduleSaveTheSale.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPES);
											eleSchSTSOrderLine.appendChild(eleSchSTSOrderLineHoldTypes);
											Element eleSchSTSOrderLineHoldType = docScheduleSaveTheSale.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPE);
											eleSchSTSOrderLineHoldTypes.appendChild(eleSchSTSOrderLineHoldType);
											eleSchSTSOrderLineHoldType.setAttribute(AcademyConstants.ATTR_HOLD_TYPE,AcademyConstants.STR_STS_RELEASE_HOLD);
											eleSchSTSOrderLineHoldType.setAttribute(AcademyConstants.ATTR_STATUS,AcademyConstants.STR_HOLD_CREATED_STATUS);	
											
											// OMNI-70080 - STS Resourcing
											String orginalFulfillmentType = "BOPIS";
											if (isSTS2Shipment) {
												 orginalFulfillmentType = "";
											}
											elechangeOrderLineExtn.setAttribute(AcademyConstants.EXTN_ORIGINAL_FULFILLMENT_TYPE, orginalFulfillmentType);
											// OMNI-70080 - STS Resourcing
											//OMNI-50489 -- START
											String strPromDateSLA =null;
											if(boolDCValidated == true)
											{
												 strPromDateSLA = props.getProperty(AcademyConstants.PROMISE_DATE_DC_SLA);
											}
											else if(boolStoreValidated == true){
												 strPromDateSLA = props.getProperty(AcademyConstants.PROMISE_DATE_STORE_SLA);
											}
											//OMNI-50489 -- END
											String strDepartDateSLA = props.getProperty(AcademyConstants.DEPARTURE_DATE_SLA);
											String strArrivalDateSLA = props.getProperty(AcademyConstants.ARRIVAL_DATE_SLA);
											
											Integer iPromDateSLA = Integer.parseInt(strPromDateSLA);
											Integer iDepartDateSLA = Integer.parseInt(strDepartDateSLA);
											Integer iArrivalDateSLA = Integer.parseInt(strArrivalDateSLA);
	
											if (iPromDateSLA != 0 && iDepartDateSLA != 0 && iArrivalDateSLA != 0) {
												DateFormat dateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
												Calendar cal = Calendar.getInstance();
												cal.add(Calendar.DATE, iPromDateSLA);
												String promDate = dateFormat.format(cal.getTime());
												log.verbose("Intial Promise date is" + promDate);
												cal.setTime(dateFormat.parse(promDate));
												cal.add(Calendar.DATE, -iDepartDateSLA);
												String departDate = dateFormat.format(cal.getTime());
												log.verbose("DC departure date is" + departDate);
												cal.setTime(dateFormat.parse(promDate));
												cal.add(Calendar.DATE, -iArrivalDateSLA);
												String arrivalDate = dateFormat.format(cal.getTime());
												log.verbose("Store arrival date is" + arrivalDate);
												elechangeOrderLineExtn.setAttribute(AcademyConstants.EXTN_INITIAL_PROMISE_DATE,promDate);
												elechangeOrderLineExtn.setAttribute(AcademyConstants.EXTN_DC_DEPARTURE_DATE,departDate);
												elechangeOrderLineExtn.setAttribute(AcademyConstants.EXTN_STORE_DELIVERY_DATE,arrivalDate);
											}
										}else {
											eleChangeOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, orderedQTy);
										}
									//OMNI-61446 Change For STS3.0 START
									//Setting inventoryCheckRequired flag in input and invoking service to drop the document to queue
									eleScheduleSaveTheSale.setAttribute(AcademyConstants.ATTR_INVENTORY_CHECK_REQUIRED, inventoryCheckRequired);
									if (isSTS2Shipment) {
										eleScheduleSaveTheSale.setAttribute(AcademyConstants.IS_STS2_SHIPMENT, AcademyConstants.STR_YES);
									}
									log.verbose("input to the queue :" + XMLUtil.getXMLString(docScheduleSaveTheSale));
									AcademyUtil.invokeService(env,AcademyConstants.STS_POST_TO_Q_SERVICE, docScheduleSaveTheSale);
									//OMNI-61446 Change For STS3.0 END
								} else {
									if (strSaveTheSaleAlternatePickEnabled.equalsIgnoreCase("Y")) {
									    eleScheduleSaveTheSale.setAttribute("AlternateStorePick", "Y");
									    eleScheduleSaveTheSale.setAttribute(AcademyConstants.ATTR_INVENTORY_CHECK_REQUIRED, "N");
									    eleScheduleSaveTheSale.setAttribute("SaveTheSale", "N");
									    log.verbose("input to the queue :" + XMLUtil.getXMLString(docScheduleSaveTheSale));
										AcademyUtil.invokeService(env, AcademyConstants.STS_POST_TO_Q_SERVICE, docScheduleSaveTheSale);
									} else {
										if (isSTS2Shipment) {
											log.verbose("Reset Order Qty for Non eligible for STS Resourcing");
											orderedQTy = orderedQtyMap.get(orderlinekey);
										}
										eleChangeOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, orderedQTy);
									}
								}
							}else {
								if (isSTS2Shipment) {
									log.verbose("Reset Order Qty for no STS Resourcing");
									orderedQTy = orderedQtyMap.get(orderlinekey);
								}
								eleChangeOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, orderedQTy);
							}
						}else {
							log.verbose("Partial Shortage");
							if (isSTS2Shipment) {
								log.verbose("Reset Order Qty for Partial Shortage for STS Resourcing");
								orderedQTy = orderedQtyMap.get(orderlinekey);
							}
							eleChangeOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, orderedQTy);
						}
					}else {
						if (isSTS2Shipment) {
							log.verbose("Reset Order Qty for Non Backroom Pick statuses for STS Resourcing");
							orderedQTy = orderedQtyMap.get(orderlinekey);
						}
						eleChangeOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, orderedQTy);
					}

				} else {
					log.verbose("Calling ChangeOrder for Other Lines");
					if (originalFulfillmentMap.containsKey(orderlinekey) && isSTS2Shipment) {
						String strTOOrderedQty = XPathUtil.getString(changeShipmentOutDoc.getDocumentElement(),
								"//OrderLine[@ChainedFromOrderLineKey='" + orderlinekey + "']/@OrderedQty");
						eleChangeOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, strTOOrderedQty);
						
					} else {
						eleChangeOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, orderedQtyMap.get(orderlinekey));
					}
				}
			
			}
			log.verbose("ChangeOrder Input  " + XMLUtil.getXMLString(docchangeOrderInput));
			// OMNI-4017, 5888, 5885 BOPIS: Cancel Email consolidation at Order level for
			// cancellations - START
			if ((!YFCObject.isVoid(isWebStoreFlag)) && (isWebStoreFlag.equals(AcademyConstants.ATTR_Y))) {
				log.verbose("Shipment Key  " + strShipmentKey);
				env.setTxnObject(AcademyConstants.ATTR_IS_WEB_STORE_FLAG, "Y");
			}
			// OMNI-4017, 5888, 5885 BOPIS: Cancel Email consolidation at Order level for
			// cancellations - END
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, docchangeOrderInput);
			Document docCommonCode = AcademyCommonCode.getCommonCodeList(env, AcademyConstants.DIRTY_NODE_REASONS,AcademyConstants.HUB_CODE);
			
			log.verbose("Calling OMS manageInventotyNodeControl method");
			if (isSTS2Shipment) {
				log.verbose("Reset Ship Node for Dirty Node");
				strShipNode = changeShipmentOutDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE);
			}
			callmanageInventoryNodeControl(env, changeShipmentInputDoc, docCommonCode, itemDetailsMap, strShipNode,strInvPictureIncorrectTillDateConstant, strShipmentKey);
		

			if (YFCCommon.equalsIgnoreCase("Shipment", inXML.getDocumentElement().getTagName())) {
				inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_STATUS, strShipmentStatus);
				// BOPIS-1431-BEGIN
				// inXML.getDocumentElement().setAttribute("ShipmentContainerizedFlag",
				// strShipmentContainerizedFlag);
				// BOPIS-1431-END
			}

		} catch (Exception e) {
			log.error(e);
			throw new YFSException(
					"Exception in the method AcademyStoreRecordShortage.recordshortageFromStore" + e.getMessage());
		}

		log.endTimer("AcademyStoreRecordShortage::recordshortageFromStore");
		return inXML;
	}
	

	private Boolean checkRequestedShipDateValidation(String strRequestedShipDate, int iReallocationDays) throws ParseException {
		
		Boolean validate = false;
		DateFormat dateFormat = new SimpleDateFormat(
				AcademyConstants.STR_DATE_TIME_PATTERN);
		
		Calendar cal = Calendar.getInstance();
		String strCurrentDate = AcademyUtil.getDate();
		Date dateCurrentDate = dateFormat.parse(strCurrentDate);
		cal.setTime(dateCurrentDate);
		cal.add(Calendar.DATE, iReallocationDays);
		
		Calendar calRequestedShipmentDate = Calendar.getInstance();
		Date dateRequestedShipmentDate = dateFormat.parse(strRequestedShipDate);
		calRequestedShipmentDate.setTime(dateRequestedShipmentDate);
		
		if(cal.compareTo(calRequestedShipmentDate)<0)
			validate = true;
		
		return validate;
	}


	// OMNI-70080 - STS Resourcing - Start
	private int getNoOfResourcingPicks(YFSEnvironment env, String strSalesOrderLineKey) throws Exception {
		int noOfShortPicks = 0;
		Document outDoc = getSalesOrderLineStatus(env, strSalesOrderLineKey);
		if (!YFCObject.isVoid(outDoc)) {
			Element eleOrderLineList = outDoc.getDocumentElement();
			NodeList nlBackorderedStatues = XPathUtil.getNodeList(eleOrderLineList,
					"/OrderLineList/OrderLine/OrderStatuses/OrderStatus[@Status='"
							+ AcademyConstants.BACKORDERED_FROM_NODE + "']");
			noOfShortPicks = nlBackorderedStatues.getLength();
		}
		log.verbose("No of Short Pick on Sales Order Line - " + noOfShortPicks);
		return noOfShortPicks;
	}
	
	private Document getSalesOrderLineStatus(YFSEnvironment env, String strSalesOrderLineKey) throws Exception {
		String strGetOrderLineDetailsInput = "<OrderLine ChainedFromOrderLineKey='" + strSalesOrderLineKey + "'/>";
		Document docGetOrderLineDetailInput = XMLUtil.getDocument(strGetOrderLineDetailsInput);
		Document docGetOrderLineDetailsOutputTemplate = XMLUtil.getDocument("<OrderLineList>"
				+ "<OrderLine OrderLineKey=''>" + "<OrderStatuses>"
				+ "<OrderStatus OrderHeaderKey='' OrderLineKey='' ShipNode='' OrderLineScheduleKey='' OrderReleaseKey='' OrderReleaseStatusKey='' Status='' StatusDate='' StatusDescription='' StatusQty='' StatusReason='' TotalQuantity='' ProcureFromNode='' />"
				+ "</OrderStatuses>" + "</OrderLine>" + "</OrderLineList>");

		YIFApi api = YIFClientFactory.getInstance().getLocalApi();
		
		Document env1Doc = XMLUtil.createDocument(AcademyConstants.ELE_ENV);
		env1Doc.getDocumentElement().setAttribute(AcademyConstants.ATTR_USR_ID, "admin");
		env1Doc.getDocumentElement().setAttribute(AcademyConstants.ATTR_PROG_ID, env.getProgId());
		YFSEnvironment newenv = api.createEnvironment(env1Doc);
		
		newenv.setApiTemplate("getOrderLineList", docGetOrderLineDetailsOutputTemplate);
		Document docGetOrderLineDetailsOutput = AcademyUtil.invokeAPI(newenv, "getOrderLineList",
				docGetOrderLineDetailInput);
		newenv.clearApiTemplate("getOrderLineList");
		log.verbose("Sales order Line list Output - " + XMLUtil.getXMLString(docGetOrderLineDetailsOutput));
		return docGetOrderLineDetailsOutput;
	}
	// OMNI-70080 - STS Resourcing - End

	private Document getSalesOrderLineFromTransferOrderLine(YFSEnvironment env, String strTransferOrderLineKey) throws Exception {
		
		String strGetOrderLineDetailsInput = "<OrderLineDetail OrderLineKey='"
				+ strTransferOrderLineKey + "'/>";
		Document docGetOrderLineDetailInput = XMLUtil.getDocument(strGetOrderLineDetailsInput);
		Document docGetOrderLineDetailsOutputTemplate =
					XMLUtil.getDocument("<OrderLine OrderLineKey=''><Extn ExtnNoOfShortPicks='' ExtnInitialPromiseDate=''></Extn></OrderLine>");
		env.setApiTemplate("getOrderLineDetails", docGetOrderLineDetailsOutputTemplate);

		Document docGetOrderLineDetailsOutput = AcademyUtil.invokeAPI(env, "getOrderLineDetails", docGetOrderLineDetailInput);
		env.clearApiTemplate("getOrderLineDetails");
		
			
		
		return docGetOrderLineDetailsOutput;
		 
	}

	private boolean validateStoreTransfer(YFSEnvironment env, int currentNumberOfShortPicks, String strRequestedShipmentDate) throws Exception {
		
		boolean validateStoreTO = false;
		String strGetCommonCodeListInput = "<CommonCode CodeType='" + "BOPIS_SHORT_PICK"
				+ "'/>";
		Document docGetCommonCodeListInputDoc = XMLUtil.getDocument(strGetCommonCodeListInput);
		Document docGetCommonCodeListOutputDoc = AcademyUtil.invokeAPI(env,
				AcademyConstants.API_GET_COMMON_CODELIST, docGetCommonCodeListInputDoc);
		//String strCodeValueShortPicks="NO_OF_SHORT_PICKS_ALLOWED";
		String strShortPicksLimit = XPathUtil.getString(docGetCommonCodeListOutputDoc.getDocumentElement(),
				"//CommonCodeList/CommonCode[@CodeValue='" + "NO_OF_SHORT_PICKS_ALLOWED" + "']/@CodeShortDescription");
		
		String strReallocationDays = XPathUtil.getString(docGetCommonCodeListOutputDoc.getDocumentElement(),
				"//CommonCodeList/CommonCode[@CodeValue='" + "NO_OF_DAYS_FOR_REALLOCATION" + "']/@CodeShortDescription");
		
		if(currentNumberOfShortPicks<= Integer.parseInt(strShortPicksLimit) && 
				checkRequestedShipDateValidation(strRequestedShipmentDate,
						Integer.parseInt(strReallocationDays)))
			validateStoreTO = true;
		
		return validateStoreTO;
	}

	private Map<String, String> getSaveTheSalePermissions(YFSEnvironment env) throws Exception {
		
		String strGetCommonCodeListInput = "<CommonCode CodeType='" + AcademyConstants.BOPIS_SAVE_THE_SALE_FLAG
				+ "'/>";
		Document docGetCommonCodeListInputDoc = XMLUtil.getDocument(strGetCommonCodeListInput);
		Document docGetCommonCodeListOutputDoc = AcademyUtil.invokeAPI(env,
				AcademyConstants.API_GET_COMMON_CODELIST, docGetCommonCodeListInputDoc);
		List<Element> listGetCommonCodeListOutput = XMLUtil.getElementsByTagName(docGetCommonCodeListOutputDoc.getDocumentElement(),
												"CommonCode");
		Map<String,String> mapSaveTheSalePermissions = new HashMap<String, String>();
		String strCodeValue = null;
		
		for(Element eleCommonCode : listGetCommonCodeListOutput) {
			strCodeValue = eleCommonCode.getAttribute("CodeValue");
			
			if("BOPIS_SAVE_THE_SALE_FLAG".equals(strCodeValue))
				mapSaveTheSalePermissions.put("STS", eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC));				
			
			else if("BOPIS_SAVE_THE_SALE_STORE_FLAG".equals(strCodeValue)) 
				mapSaveTheSalePermissions.put("STS_STORE", eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC));
			
			 else if ("BOPIS_SAVE_THE_SALE_DC_FLAG".equals(strCodeValue)) 
				 mapSaveTheSalePermissions.put("STS_DC", eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC));	
			
			 else if ("BOPIS_SAVE_THE_SALE_AS_FLAG".equalsIgnoreCase(strCodeValue))
				 mapSaveTheSalePermissions.put("STS_AS", eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC)); 
		}
		
		return mapSaveTheSalePermissions;
	}

	/**
	 * 
	 * @param env
	 * @param strItemId
	 * @return
	 * @throws Exception 
	 */
	private String getItemShipNodeTypeAssociation(YFSEnvironment env, String strItemId) throws Exception {
		String strGetItemListInput = "<Item ItemID='" + strItemId + "' OrganizationCode='DEFAULT'/>";
		Document docGetItemListInput = XMLUtil.getDocument(strGetItemListInput);
		Document docGetItemListOutputTemplate = XMLUtil.getDocument("<ItemList><Item><Extn ExtnShipToStoreItem =''/></Item></ItemList>");
		env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, docGetItemListOutputTemplate);

		Document docGetItemListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST, docGetItemListInput);
		env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
		Element EleItemListOutExtn = XMLUtil.getElementByXPath(docGetItemListOutput, "ItemList/Item/Extn");
		String strItemShipNodeAssociation = EleItemListOutExtn.getAttribute("ExtnShipToStoreItem");
		
		
		return strItemShipNodeAssociation;
	}
	
	private Document getStoreDCMapping(YFSEnvironment env, String strShipNode) throws Exception {
		String strDCMappingInput = "<CommonCode CodeType='"
				+ AcademyConstants.STS_STORE_DC_MAPPING + "' CodeValue='" + strShipNode + "'/>";
		Document strDCMappingInputDoc = XMLUtil.getDocument(strDCMappingInput);
		Document strDCMappingOutputDoc = AcademyUtil.invokeAPI(env,
				AcademyConstants.API_GET_COMMON_CODELIST, strDCMappingInputDoc);
		return strDCMappingOutputDoc;
	}
	
	private Document getOrderLineList (YFSEnvironment env, String strOrderLineKey, String strOrderHeaderKey) throws Exception {
		String strGetOrderLineList = "<OrderLine OrderLineKey='"+ strOrderLineKey + "'/>";
		Document inDocGetOrderLineList = XMLUtil.getDocument(strGetOrderLineList);
		Document outDocGetOrderLineListTemplate = XMLUtil.getDocument("<OrderLineList LastOrderLineKey=''>\r\n" + 
				"	<OrderLine FulfillmentType='' ShipNode='' OrderLineKey='' ChainedFromOrderLineKey='' >\r\n" + 
				"		<Order DocumentType=''> \r\n" + 
				"			<Extn ExtnOriginalShipNode='' ExtnASShipNode=''/>\r\n" + 
				"		</Order>\r\n" + 
				"		<Extn ExtnNoOfShortPicks='' ExtnInitialPromiseDate=''/>\r\n" + 
				"	</OrderLine>\r\n" + 
				"</OrderLineList>");
		env.setApiTemplate("getOrderLineList", outDocGetOrderLineListTemplate);
		log.verbose("In Document getOrderLineList :: " + XMLUtil.getXMLString(inDocGetOrderLineList));
		Document outDocGetOrderLineList = AcademyUtil.invokeAPI(env, "getOrderLineList", inDocGetOrderLineList);
		env.clearApiTemplate("getOrderLineList");
		log.verbose("Out Document getOrderLineList :: " + XMLUtil.getXMLString(outDocGetOrderLineList));
		return outDocGetOrderLineList;
	}
		
		
	/**
	 * Calling eligibleForTransferOrderCreation method to check if Organization and
	 * Item is eligible for TO creation.
	 * 
	 * @param eligibleForTOCreation
	 * @param strShipNode
	 * @param eligibleForResourcing 
	 * @param strItemID
	 * @throws Exception
	 */
	
	private void eligibleForTransferOrderCreation(YFSEnvironment env, HashMap<String, String> itemDetailsMap,
			String strShipNode, Document changeShipmentInputDoc, boolean eligibleForResourcing) throws Exception {
		
		log.verbose(
				"Entering AcademyStoreRecordShortage.eligibleForTransferOrderCreation() :: " + eligibleForTOCreation);
		NodeList shipmentLineFromchangeShipmentInputDoc = changeShipmentInputDoc.getDocumentElement()
				.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

		for (int l = 0; l < shipmentLineFromchangeShipmentInputDoc.getLength(); l++) {
			Element eleShipmentLine = (Element) shipmentLineFromchangeShipmentInputDoc.item(l);
			String strShipLineKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
			String strItemID = itemDetailsMap.get(strShipLineKey);
			log.verbose("ItemID is "+strItemID);
			String strGetOrganizationListInput = "<Organization OrganizationCode='" + strShipNode + "'/>";
			log.verbose("getOrganizationList input is " + strGetOrganizationListInput);
			Document docGetOrganizationListInput = XMLUtil.getDocument(strGetOrganizationListInput);
			Document docGetOrganizationListOutputTemplate = XMLUtil.getDocument(
					"<OrganizationList><Organization><Extn ExtnIsSTSEnabled=''/></Organization></OrganizationList>");
			env.setApiTemplate(AcademyConstants.API_GET_ORGANIZATION_LIST, docGetOrganizationListOutputTemplate);
			Document docGetOrganizationListOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_ORGANIZATION_LIST, docGetOrganizationListInput);
			env.clearApiTemplate(AcademyConstants.API_GET_ORGANIZATION_LIST);
			Element docGetOrganizationListOutputEle = XMLUtil.getElementByXPath(docGetOrganizationListOutput,
					"OrganizationList/Organization/Extn");
			String orgSTSFlag = docGetOrganizationListOutputEle.getAttribute("ExtnIsSTSEnabled");
			log.verbose("Organization STS eligible flag is" + orgSTSFlag);
			String strGetItemListInput = "<Item ItemID='" + strItemID + "' OrganizationCode='DEFAULT'/>";
			log.verbose("getItemList input is " + strGetItemListInput);
			Document docGetItemListInput = XMLUtil.getDocument(strGetItemListInput);
			Document docGetItemListOutputTemplate = XMLUtil
					.getDocument("<ItemList><Item><Extn ExtnShipToStoreItem='' ExtnSTSPrimarySource='' /></Item></ItemList>");
			env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, docGetItemListOutputTemplate);

			Document docGetItemListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST,
					docGetItemListInput);
			env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
			Element docGetItemListOutputEle = XMLUtil.getElementByXPath(docGetItemListOutput, "ItemList/Item/Extn");
			String itemSTSFlag = docGetItemListOutputEle.getAttribute(AcademyConstants.ATTR_EXTN_SHIP_TO_STORE_ITEM);
			String primarySource = docGetItemListOutputEle.getAttribute(AcademyConstants.ATTR_EXTN_PRIMARY_SOURCE);
			log.verbose("Item STS eligible flag is" + itemSTSFlag);

			//OMNI-50489 -- START
			/** 
			 * Pseudo Logic to validate if a line is STS1.1/STS2.0 save the sale eligible or not
			 * eligibleForTOCreation=FALSE
			 *  eligibleForDCTOCreation = FALSE
			 *  eligibleForSTORETOCreation= FALSE  
			 */
			if ((!YFCCommon.isVoid(itemSTSFlag)) && (!YFCCommon.isVoid(orgSTSFlag))){
				// OMNI-70080 - STS Resourcing - Start
				// Item Flag S 
				// Store Flag S , Primary Source Store/DC then resourcing should be done from Store
				// Store Flag B, Primary Source DC then resourcing should be done from store
				if (eligibleForResourcing) {
					log.verbose("Eligible for Resourcing !");
					if ((AcademyConstants.ATTR_S.equals(orgSTSFlag) || AcademyConstants.ATTR_B.equals(orgSTSFlag))) {
						
						if ((AcademyConstants.ATTR_S.equals(itemSTSFlag) || AcademyConstants.ATTR_B.equals(itemSTSFlag))) {
							
							if(!YFCCommon.isVoid(primarySource) && primarySource.equals("DC")) {
								log.verbose("Primary Source");
								eligibleForTOCreation = true;
								eligibleForDCTOCreation = true;
								eligibleForSTORETOCreation = false;
								inventoryCheckRequired="Y";
							}
							else if(!YFCCommon.isVoid(primarySource) && primarySource.equals("STORE")) {
								log.verbose("inside store Primary Source");
								eligibleForTOCreation = true;
								eligibleForDCTOCreation = false;
								eligibleForSTORETOCreation = true;
								inventoryCheckRequired="Y";
							}
							else {
								log.verbose("Primary Source is blank or null or having other value");
								eligibleForTOCreation = true;
								eligibleForDCTOCreation = true;
							}
						}
					} 
					// OMNI-70080 - STS Resourcing - End
				} 
				else {
						
					if ((itemSTSFlag.equals(AcademyConstants.ATTR_N)) || (orgSTSFlag.equals(AcademyConstants.ATTR_N))) {
						eligibleForTOCreation = false;
						eligibleForDCTOCreation = false;
						eligibleForSTORETOCreation = false;
					}
					else if((itemSTSFlag.equals(AcademyConstants.ATTR_Y)) && (orgSTSFlag.equals(AcademyConstants.ATTR_Y) 
																		|| orgSTSFlag.equals(AcademyConstants.ATTR_B))) {
						eligibleForTOCreation = true;
						eligibleForDCTOCreation = true;
						eligibleForSTORETOCreation = false;
					}
					else if ((itemSTSFlag.equals(AcademyConstants.ATTR_S)) && (orgSTSFlag.equals(AcademyConstants.ATTR_S) 
																		|| orgSTSFlag.equals(AcademyConstants.ATTR_B))) {
						eligibleForTOCreation = true;
						eligibleForDCTOCreation = false;
						eligibleForSTORETOCreation = true;
			    	}
					else if((itemSTSFlag.equals(AcademyConstants.ATTR_B))) {
						
						if(orgSTSFlag.equals(AcademyConstants.ATTR_S)) {
							eligibleForTOCreation = true;
							eligibleForDCTOCreation = false;
							eligibleForSTORETOCreation = true;
						}
						else if(orgSTSFlag.equals(AcademyConstants.ATTR_Y)) {
							eligibleForTOCreation = true;
							eligibleForDCTOCreation = true;
							eligibleForSTORETOCreation = false;
						}
						else if(orgSTSFlag.equals(AcademyConstants.ATTR_B)) {
							
							if(!YFCCommon.isVoid(primarySource) && primarySource.equals("DC")) {
								log.verbose("Primary Source");
								eligibleForTOCreation = true;
								eligibleForDCTOCreation = true;
								eligibleForSTORETOCreation = false;
								inventoryCheckRequired="Y";
							}
							else if(!YFCCommon.isVoid(primarySource) && primarySource.equals("STORE")) {
								log.verbose("inside store Primary Source");
								eligibleForTOCreation = true;
								eligibleForDCTOCreation = false;
								eligibleForSTORETOCreation = true;
								inventoryCheckRequired="Y";
							}
							else {
								log.verbose("Primary Source is blank or null or having other value");
								eligibleForTOCreation = true;
								eligibleForDCTOCreation = true;
							}
					}
				}
			}
		}

			log.verbose("Exiting AcademyStoreRecordShortage.eligibleForTransferOrderCreation() ::eligibleForTOCreation:== "
					+ eligibleForTOCreation+  " eligibleForDCTOCreation := "+eligibleForDCTOCreation+" eligibleForSTORETOCreation := "+eligibleForSTORETOCreation
					+" inventoryCheckRequired := "+inventoryCheckRequired +" primarySource := "+primarySource);
			//OMNI-50489 -- END

		}
	}

	/**
	 * Call getShipmentList api and forms input to changeShipment api.
	 * 
	 * @param env
	 * @param strShipmentKey
	 * @param extnReasonCode
	 * @return
	 * @throws Exception
	 */
	private Document formChangeShipmentInDoc(YFSEnvironment env, String strShipmentKey, String extnReasonCode)
			throws Exception {
		log.beginTimer("formChangeShipmentInDoc");
		Document getShipmentListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element elegetShipListRoot = getShipmentListInDoc.getDocumentElement();
		elegetShipListRoot.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
		Document getShipListOutDOc = AcademyUtil.invokeService(env,
				AcademyConstants.ACAD_GET_SHIPMENT_LIST_FOR_OTHER_STORE, getShipmentListInDoc);

		Element elegetShipList = getShipListOutDOc.getDocumentElement();
		Element elegetShipShipments = SCXmlUtil.getChildElement(elegetShipList, AcademyConstants.ELE_SHIPMENT);
		String strStatus = elegetShipShipments.getAttribute(AcademyConstants.ATTR_STATUS);
		String strSubStatus = strStatus.substring(0, 4);
		int iStatus = Integer.parseInt(strSubStatus);
		if (iStatus >= 1400) {// If shipment is shipped or cancelled
			// BOPIS-1090 ::Begin
			Document outDoc = SCXmlUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			outDoc.getDocumentElement().setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
			outDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_STATUS, strStatus);
			outDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ERROR_MESSAGE,
					"Shipment is not in Cancellable Status");
			log.verbose("Shipment is not in Cancellable Status. Current Shipment Status is ::" + strStatus);
			return outDoc;
			// BOPIS-1090 ::End
		}

		Document changeShipmentInDocument = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleChangeShipRoot = changeShipmentInDocument.getDocumentElement();
		eleChangeShipRoot.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
		eleChangeShipRoot.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY);
		eleChangeShipRoot.setAttribute(AcademyConstants.ATTR_BACK_ORD_REM_QTY, AcademyConstants.STR_YES);
		Element eleShipmentLines = changeShipmentInDocument.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
		eleChangeShipRoot.appendChild(eleShipmentLines);
		NodeList shipmentLnes = getShipListOutDOc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		for (int i = 0; i < shipmentLnes.getLength(); i++) {
			Element eleShipLine = (Element) shipmentLnes.item(i);
			String qty = eleShipLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
			String shipmentLineKey = eleShipLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
			Element eleShipmentLine = changeShipmentInDocument.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
			eleShipmentLines.appendChild(eleShipmentLine);
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, shipmentLineKey);
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, AcademyConstants.ATTR_ZERO);
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHORTAGE_QTY, qty);
			Element eleExtn = changeShipmentInDocument.createElement(AcademyConstants.ELE_EXTN);
			eleShipmentLine.appendChild(eleExtn);
			eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_MSG_TO_SIM, AcademyConstants.ATTR_Y);
			eleExtn.setAttribute(AcademyConstants.STR_EXTN_REASON_CODE, extnReasonCode);
		}
		log.endTimer("formChangeShipmentInDoc");
		return changeShipmentInDocument;
	}

	/**
	 * This method marks the node dirty for batch pick.
	 * 
	 * @param env
	 * @param inXML
	 * @throws Exception
	 */
	public void setDirtyNodeForShrtgOfStoreBatch(YFSEnvironment env, Document inXML) throws Exception {

	Element eleInDocShp = inXML.getDocumentElement();
	String strInDocShipNode = eleInDocShp.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
	String strMarkDirty = eleInDocShp.getAttribute("MarkDirty");
	String strItemID = XPathUtil.getString(inXML.getDocumentElement(), "//StoreBatch/Item/@ItemID");
	String strReasonCode = XPathUtil.getString(inXML.getDocumentElement(), "//StoreBatch/Item/@ShortageReason");
	Document docCommonCode = AcademyCommonCode.getCommonCodeList(env, AcademyConstants.DIRTY_NODE_REASONS,
			AcademyConstants.HUB_CODE);
	String strCodeShortDesc = XPathUtil.getString(docCommonCode.getDocumentElement(),
			"//CommonCodeList/CommonCode[@CodeValue='" + strReasonCode + "']/@CodeShortDescription");

	// SFS33 Batch short pick issue-Node inventory expiry date not updating
	// MarkDity attribute not getting populated in some use cases
	if (!YFCObject.isVoid(strCodeShortDesc) && strCodeShortDesc.equalsIgnoreCase(AcademyConstants.STR_YES))
		// && YFCCommon.equalsIgnoreCase(AcademyConstants.STR_YES, strMarkDirty))
	{
		Document docmanageInventoryNodeControl = XMLUtil.createDocument(AcademyConstants.INVENTORY_NODE_CONTROL);
		Element eleRootmanageInventoryNodeControl = docmanageInventoryNodeControl.getDocumentElement();
		// BOPIS-1207: Configuration for batch shortage inventory node control
		// expiration : Begin
		String strInvPictureIncorrectTillDateConstant = props
				.getProperty(AcademyConstants.STR_INV_PICTURE_INCORRECT_TILL_DATE_CONSTANT);
		Integer iInvPictureIncorrectTillDate = Integer.parseInt(strInvPictureIncorrectTillDateConstant);

		if (iInvPictureIncorrectTillDate != 0) {
		
			/** OMNI-34709 : Start **/	
			log.verbose("iInvPictureIncorrectTillDate " + iInvPictureIncorrectTillDate);
			
			Map<String,String> YantrkNCEnabled = YASPostNodeControlToKafka.getNodeControlValue(env);
			if (!YFCObject.isVoid(YantrkNCEnabled) && YantrkNCEnabled.get(AcademyConstants.YFS_NODE_CONTROL_CODE_VALUE).equalsIgnoreCase(AcademyConstants.STR_YES)) {
				log.verbose("Yantriks node control is ON in setDirtyNodeForShrtgOfStoreBatch");

				YASPostNodeControlToKafka.kafkaUpdateForBatchStoreNC(env, inXML);

			}
			if (!YFCObject.isVoid(YantrkNCEnabled) && YantrkNCEnabled.get(AcademyConstants.OMS_NODE_CONTROL_CODE_VALUE).equalsIgnoreCase(AcademyConstants.STR_YES)) {
				log.verbose("OMS node control is ON in setDirtyNodeForShrtgOfStoreBatch");

			DateFormat dateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			Calendar cal = Calendar.getInstance();
			Calendar cal1 = Calendar.getInstance();
			cal.add(Calendar.DATE, iInvPictureIncorrectTillDate);
			String sysdate = dateFormat.format(cal1.getTime());
			String InvPictureIncorrectTillDate = dateFormat.format(cal.getTime());
			eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_INV_PIC_INCORRECT_TILL_DATE,
					InvPictureIncorrectTillDate);
			eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_PROD_CLASS,
					AcademyConstants.PRODUCT_CLASS);
			eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_NODE_CONTROL_TYPE,
					AcademyConstants.STR_ON_HOLD);
			eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_UOM,
					AcademyConstants.UNIT_OF_MEASURE);
			// eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_START_DATE,sysdate);
			eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_ITEM_ID, strItemID);
			eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_NODE, strInDocShipNode);
			log.verbose(
					"callmanageInventoryNodeControl Input1" + XMLUtil.getXMLString(docmanageInventoryNodeControl));
			// System.out.println("callmanageInventoryNodeControl Input"+
			// XMLUtil.getXMLString(docmanageInventoryNodeControl));
			AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_INVENTORY_NODE_CONTROL,
					docmanageInventoryNodeControl);
			}
			/** OMNI-34709 : End **/	

		}
		// BOPIS-1207: Configuration for batch shortage inventory node control
		// expiration : End
	}
}

/**
 * @param env
 * @param changeShipmentInputDoc
 * @param docCommonCode
 * @param itemDetailsMap
 * @param strShipNode
 * @param changeShipmentOutDoc 
 */
public void callmanageInventoryNodeControl(YFSEnvironment env, Document changeShipmentInputDoc,
		Document docCommonCode, HashMap<String, String> itemDetailsMap, String strShipNode,
		String strInvPictureIncorrectTillDateConstant, String shipmentKey) {
	try {

		NodeList shipmentLineFromchangeShipmentInputDo = changeShipmentInputDoc.getDocumentElement()
				.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

		for (int l = 0; l < shipmentLineFromchangeShipmentInputDo.getLength(); l++) {
			Element eleShipmentLine = (Element) shipmentLineFromchangeShipmentInputDo.item(l);
			String strShipLineKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);

			Element eleextn = (Element) eleShipmentLine.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
			String strExtnReasonCode = eleextn.getAttribute(AcademyConstants.STR_EXTN_REASON_CODE);
			log.verbose("strExtnReasonCode while marking dirty" + strExtnReasonCode);
			String strCodeShortDesc = XPathUtil.getString(docCommonCode.getDocumentElement(),
					"//CommonCodeList/CommonCode[@CodeValue='" + strExtnReasonCode + "']/@CodeShortDescription");
			log.verbose("strCodeShortDesc while marking dirty" + strCodeShortDesc);
			if (!YFCObject.isVoid(strCodeShortDesc)
					&& strCodeShortDesc.equalsIgnoreCase(AcademyConstants.STR_YES)) {
				Document docmanageInventoryNodeControl = XMLUtil
						.createDocument(AcademyConstants.INVENTORY_NODE_CONTROL);
				Element eleRootmanageInventoryNodeControl = docmanageInventoryNodeControl.getDocumentElement();
				Integer iInvPictureIncorrectTillDate = Integer.parseInt(strInvPictureIncorrectTillDateConstant);

				DateFormat dateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
				Calendar cal = Calendar.getInstance();
				Calendar cal1 = Calendar.getInstance();
				cal.add(Calendar.DATE, iInvPictureIncorrectTillDate);
				String sysdate = dateFormat.format(cal1.getTime());
				String InvPictureIncorrectTillDate = dateFormat.format(cal.getTime());
				eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_INV_PIC_INCORRECT_TILL_DATE,
						InvPictureIncorrectTillDate);
				eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_PROD_CLASS,
						AcademyConstants.PRODUCT_CLASS);
				eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_NODE_CONTROL_TYPE,
						AcademyConstants.STR_ON_HOLD);
				eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ORGANIZATION_CODE,
						AcademyConstants.PRIMARY_ENTERPRISE);
				eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_UOM,
						AcademyConstants.UNIT_OF_MEASURE);
				// eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_START_DATE,sysdate);
				String itemId = itemDetailsMap.get(strShipLineKey);
				log.verbose("itemId while marking dirty" + itemId);
				log.verbose("strShipNode while marking dirty" + strShipNode);
				log.verbose("InvPictureIncorrectTillDate while marking dirty" + InvPictureIncorrectTillDate);
				log.verbose("strShipNode while marking dirty" + strShipNode);
				log.verbose("sysdate while marking dirty" + sysdate);

				if (!YFCObject.isVoid(itemId) && !YFCObject.isVoid(strShipNode)) {
					
					eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_ITEM_ID, itemId);
					eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_NODE, strShipNode);
					log.verbose("callmanageInventoryNodeControl Input2"
							+ XMLUtil.getXMLString(docmanageInventoryNodeControl));
					
					/** OMNI-34709 : Start **/	

					Map<String,String> YantrkNCEnabled = YASPostNodeControlToKafka.getNodeControlValue(env);
					
					if (!YFCObject.isVoid(YantrkNCEnabled) && YantrkNCEnabled.get(AcademyConstants.OMS_NODE_CONTROL_CODE_VALUE)
							.equalsIgnoreCase(AcademyConstants.STR_YES)) {
						
						log.verbose("OMS node control is ON in callmanageInventoryNodeControl");

					AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_INVENTORY_NODE_CONTROL,
							docmanageInventoryNodeControl);
					}
					
					if (!YFCObject.isVoid(YantrkNCEnabled) && YantrkNCEnabled.get(AcademyConstants.YFS_NODE_CONTROL_CODE_VALUE).equalsIgnoreCase(AcademyConstants.STR_YES)) {
						log.verbose("Yantriks node control is ON in callmanageInventoryNodeControl");

						YASPostNodeControlToKafka.kafkaUpdateForStoreNC(env, shipmentKey);

					}
					/** OMNI-34709 : End **/	

				} else {
					log.verbose("Not calling manageInventoryNodeControl because either the itemid or node is null");
				}
			}
		}

	} catch (ParserConfigurationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

}

	/**
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws ParserConfigurationException
	 */
	public Document formChangeShipmentInDocForCustPickShortage(YFSEnvironment env, Document inDoc)
			throws ParserConfigurationException {

		String strShipmentKey = inDoc.getDocumentElement().getAttribute(AcademyConstants.SHIPMENT_KEY);

		Document changeShipmentInputDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleShipment = changeShipmentInputDoc.getDocumentElement();
		eleShipment.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
		eleShipment.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY);
		eleShipment.setAttribute(AcademyConstants.ATTR_BACK_ORD_REM_QTY, AcademyConstants.STR_YES);
		Element eleShipmentLines = changeShipmentInputDoc.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
		eleShipment.appendChild(eleShipmentLines);
		NodeList NLShipmentLine = inDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		for (int i = 0; i < NLShipmentLine.getLength(); i++) {
			Element EleNLShipmentLine = (Element) NLShipmentLine.item(i);
			Element srcExtn = (Element) EleNLShipmentLine.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
			String strShipmentLineKey = EleNLShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
			String strShortageQty = EleNLShipmentLine.getAttribute(AcademyConstants.ATTR_SHORTAGE_QTY);
			String strQuantity = EleNLShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
			if (!YFCObject.isVoid(strShortageQty)) {
				Double dShortageQty = Double.parseDouble(strShortageQty);
				if (dShortageQty > 0.00) {

					Element ShipmentLine = changeShipmentInputDoc.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
					eleShipmentLines.appendChild(ShipmentLine);
					ShipmentLine.setAttribute(AcademyConstants.ATTR_SHORTAGE_QTY, strShortageQty);
					ShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, strShipmentLineKey);
					if (!YFCObject.isVoid(strQuantity)) {
						Double dQuantity = Double.parseDouble(strQuantity);
						dQuantity = dQuantity - dShortageQty;
						strQuantity = String.valueOf(dQuantity);
						ShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, strQuantity);
					}

					Element Extn = changeShipmentInputDoc.createElement(AcademyConstants.ELE_EXTN);
					XMLUtil.copyElement(changeShipmentInputDoc, srcExtn, Extn);
					ShipmentLine.appendChild(Extn);

				}
			}
		}

		log.verbose("Output of changeShipmentInputDoc" + XMLUtil.getXMLString(changeShipmentInputDoc));
		return changeShipmentInputDoc;
	}
	
	private boolean alternateStoreShortPicked(String strOriginalFulfillment, Document docOrderLineList, String strSaveTheSaleAlternatePickEnabled)
			throws Exception {
		String strAlternateStorenode = XPathUtil.getString(docOrderLineList, ".//OrderLine/Order/Extn/@ExtnASShipNode");
		String strShipNode = XPathUtil.getString(docOrderLineList, "//OrderLineList/OrderLine/@ShipNode");
		String strFulfillmentType = XPathUtil.getString(docOrderLineList, "//OrderLineList/OrderLine/@FulfillmentType");
		if (!YFCCommon.isVoid(strAlternateStorenode)) {
			log.verbose("AlternatePick ShipNode :: "+ strAlternateStorenode+ " BOPIS ShipNode :: "+ strShipNode + " FulfillmentType :: "+  strFulfillmentType);
			boolean isBOPISOrder = strFulfillmentType.equalsIgnoreCase(AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE);
			boolean isAPNode = strShipNode.equalsIgnoreCase(strAlternateStorenode);
			return isBOPISOrder && isAPNode ;
		} else {
			return false;
		}
	}

}