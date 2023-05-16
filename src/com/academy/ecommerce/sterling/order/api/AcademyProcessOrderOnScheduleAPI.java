package com.academy.ecommerce.sterling.order.api;

import java.util.Properties;
import java.util.HashMap;

import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.academy.util.xml.XPathWrapper;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;

public class AcademyProcessOrderOnScheduleAPI implements YIFCustomApi {

	/** log variable for logging messages */
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyProcessOrderOnScheduleAPI.class);

	/** Output document class variable */
	Document docOutput = null;

	Document docInput = null;

	boolean bOrderOnHold = false;

	boolean bCreateHoldAndAlert = false;

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	public Document processOrderOnScheduleSuccess(YFSEnvironment env,
			Document inDoc) throws Exception {
		log.beginTimer(" Begin of AcademyProcessOrderOnScheduleAPI  processOrderOnScheduleSuccess()- Api");
		log.verbose("************* Inside processOrderOnScheduleSuccess *************** indoc --> " + XMLUtil.getXMLString(inDoc)+"<--");
		this.docInput = inDoc;
		Element eleExceptionInput = null;
		Document docCreateExceptionAPIInput = null;
		String strOrderHeaderKey = null;
		String strInboxKey = null;
		Document docExceptionListForOrder = null;
		Element eleInbox = null;
		
		if (YFCObject.isVoid(inDoc)) {
			YFSException excItemID = new YFSException(
					AcademyConstants.ERR_ITEM_NOT_FOUND);
			// excItemID.setErrorCode(AcademyConstants.ERR_CODE_01);
			throw excItemID;
		}
		strOrderHeaderKey = inDoc.getDocumentElement().getAttribute(AcademyConstants.STR_ORDR_HDR_KEY);
		log.verbose("############# Order Header Key is "+strOrderHeaderKey);
		
		// read fulfillmentType from OrderLine level. Dont apply hold if FT="DROP_SHIP"
		
		YFCDocument inYFCDoc = YFCDocument.getDocumentFor(inDoc);
		YFCNodeList<YFCElement> orderLineList = inYFCDoc.getElementsByTagName(AcademyConstants.XPATH_ORDERLINE);
		 
			
		
		
		
		log.verbose("*************Calling checkIfOrderOnHold to check if Order on Hold***************");
		bOrderOnHold = checkIfOrderOnHold(inDoc);
		log.verbose("############# Order is on HOLD "+bOrderOnHold);
		log.verbose("************* Calling processOrderLinesForHoldAndAlert to prepare input for createException***************");
		docCreateExceptionAPIInput = processOrderLinesForHoldAndAlert(inDoc);
		//log.verbose("docCreateExceptionAPIInput --> "+XMLUtil.getXMLString(docCreateExceptionAPIInput)+"<--- ");
		log.verbose("bCreateHoldAndAlert: "+bCreateHoldAndAlert);
		
		if (bCreateHoldAndAlert) {
			log.verbose("************* Backordered line exists, Alert will be raised***************");
			if (bOrderOnHold) {
				log.verbose("************* Calling getExceptionListForOrder to get exception for the Order***************"
								+ strOrderHeaderKey);
				docExceptionListForOrder = getExceptionListForOrder(
						strOrderHeaderKey, env);
				eleInbox = (Element) docExceptionListForOrder
						.getDocumentElement().getElementsByTagName(
								AcademyConstants.ELE_INBOX).item(0);
				strInboxKey = eleInbox
						.getAttribute(AcademyConstants.ATTR_INBOX_KEY);
				docCreateExceptionAPIInput.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_INBOX_KEY, strInboxKey);
				log.verbose("************* Calling changeExceptionDetailsForOrder to change existing exception***************");
				changeExceptionDetailsForOrder(docCreateExceptionAPIInput, env);
				// createAlertsForAvailabiliy(docCreateExceptionAPIInput, env);
			} else {
				createAlertsForAvailabiliy(docCreateExceptionAPIInput, env);
				log.verbose("before calling putOrderOnAvailabilityHold () *********** ");
				putOrderOnAvailabilityHold(strOrderHeaderKey, env);			
			}
		} else {

			log.verbose("************* No back ordered lines***************");
			if (bOrderOnHold) {
				log.verbose("************* Releasing Existing Hold and Closing Alert***************");
				releaseOrderFromHold(strOrderHeaderKey, env);
				resolveExceptionForOrder(strOrderHeaderKey, env);
			}
		}
		log.endTimer(" End of AcademyProcessOrderOnScheduleAPI  processOrderOnScheduleSuccess()- Api");
		return this.docInput;
	}

	private Document getExceptionListForOrder(String strOrderHeaderKey,
			YFSEnvironment env) {
		Document docGetExceptionListInput = null;
		Document docGetExceptionListOutput = null;
		log.verbose("************* Inside getExceptionListForOrder***************");
		try {
			log.beginTimer(" Begin of AcademyProcessOrderOnScheduleAPI  getExceptionListForOrder()- Api");
			docGetExceptionListInput = XMLUtil
					.createDocument(AcademyConstants.ELE_INBOX);
			docGetExceptionListInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
			docGetExceptionListInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_EXCPTN_TYPE,
					AcademyConstants.STR_AVAIL_EXCEPTION);
			log.verbose("************* Calling  getExceptionList with input***************"
							+ XMLUtil.getXMLString(docGetExceptionListInput));
			docGetExceptionListOutput = AcademyUtil.invokeAPI(env,
					"getExceptionList", docGetExceptionListInput);
			log.verbose("************* Output of getExceptionList***************"
							+ XMLUtil.getXMLString(docGetExceptionListOutput));
			log.endTimer(" End of AcademyProcessOrderOnScheduleAPI  getExceptionListForOrder()- Api");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		log.verbose("************* Exiting getExceptionListForOrder***************");
		return docGetExceptionListOutput;
	}

	private Document processOrderLinesForHoldAndAlert(Document docInput) {
		NodeList nListOrderLines = null;
		Element eleCurrentOrderLine = null;
		Element eleBackOrderedLine = null;
		Element eleCreateExceptionInput = null;
		Document docInputCreateException = null;
		String strBackOrderQty = null;
		String strFulfillmentType="";
		
		// boolean bFirstTimeCreate=true;
		int iNoOfOrderLines = 0;
		log.verbose("************* Inside processOrderLinesForHoldAndAlert***************");
		try {
			log.beginTimer(" Begin of AcademyProcessOrderOnScheduleAPI  processOrderLinesForHoldAndAlert()- Api");
			nListOrderLines = XMLUtil.getNodeList(
					docInput.getDocumentElement(),
					AcademyConstants.XPATH_ORDERLINE);

			if (!YFCObject.isVoid(nListOrderLines)) {
				iNoOfOrderLines = nListOrderLines.getLength();
			}
			for (int i = 0; i < iNoOfOrderLines; i++) {
				eleCurrentOrderLine = (Element) nListOrderLines.item(i);
				
				/**
				 * Added for DSV. No need to put hold for an Order, if only the DSV line is back ordered/ partially backordered.
				 * For normal item orderlines, no change in existing logic.
				 * 
				 * Read the FulfillmentType from <OrderLine>
				 */
				strFulfillmentType=eleCurrentOrderLine.getAttribute("FulfillmentType");
				log.verbose("FulfillmentType @ <OrderLine> = "+strFulfillmentType);
				/**
				 * DSV change ends
				 */
				if (!YFCObject.isVoid(eleCurrentOrderLine)) {
					eleBackOrderedLine = (Element) XPathUtil.getNode(
							eleCurrentOrderLine,
							AcademyConstants.XPATH_BACKORDER_LINE_STATUS);
					if (!YFCObject.isVoid(eleBackOrderedLine)) {
						strBackOrderQty = eleBackOrderedLine
								.getAttribute(AcademyConstants.ATTR_STAT_QTY);
						if (!YFCObject.isVoid(strBackOrderQty)) {
							if (new Float(strBackOrderQty).intValue() > 0) {
								
								/**
								 * Added for DSV. No need to put hold for an Order, if only the DSV line is back ordered/ partially backordered.
								 * For normal item orderlines, no change in existing logic.
								 * 
								 * Check whether the FulfillmentType from <OrderLine> is DROP_SHIP.
								 * 
								 * If yes, then do not set bCreateHoldAndAlert = true 
								 * else, no change in the existing logic.
								 */
								
								if (AcademyConstants.FULFILLMENT_TYPE.equals(strFulfillmentType)){
									log.verbose("DROP_SHIP Line");
								}else{
									log.verbose("not a DROP_SHIP Line");
									bCreateHoldAndAlert = true;
								}
								
								/**
								 * DSV Change Ends.
								 */
								log.verbose("************* Calling createInputForAlert to add back ordered line as reference***************");
								docInputCreateException = createInputForAlert(
										eleCurrentOrderLine,
										docInputCreateException);
							}// end if
						}// end if void check

					}
				}

			}
			log.endTimer(" End of AcademyProcessOrderOnScheduleAPI  processOrderLinesForHoldAndAlert()- Api");
			log.verbose("************* Exiting processOrderLinesForHoldAndAlert***************");
			return docInputCreateException;
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

	} // end method -processOrderLinesForHoldAndAlert

	private boolean checkIfOrderOnHold(Document inDoc) {
		Element eleHoldType = null;
		String strHoldStatus = null;
		log.verbose("************* Inside checkIfOrderOnHold***************");
		try {
			log.beginTimer(" Begin of AcademyProcessOrderOnScheduleAPI  checkIfOrderOnHold()- Api");
			eleHoldType = (Element) XPathUtil.getNode(inDoc
					.getDocumentElement(),
					AcademyConstants.XPATH_PART_AVAIL_HOLD_TYPE);
			if (!YFCObject.isVoid(eleHoldType))
				strHoldStatus = eleHoldType.getAttribute(AcademyConstants.ATTR_STATUS);
			if (AcademyConstants.STR_HOLD_CREATED_STATUS.equals(strHoldStatus)) {
				log.verbose("************* Inside checkIfOrderOnHold::Order is on Hold***************");
				log.endTimer(" End of AcademyProcessOrderOnScheduleAPI  checkIfOrderOnHold()- Api");
				return true;
			} else
				return false;

		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

	}// end method -checkIfOrderOnHold

	private Document createInputForAlert(Element eleOrderLine,
			Document docExceptionInput) {
		// Element eleInbox=null;
		// Document docExceptionInput = null;
		Element eleInboxRefList = null;
		Element eleInboxRef = null;
		String strExceptionValue = null;
		log.verbose("************* Inside createInputForAlert***************");
		try {
			log.beginTimer(" Begin of AcademyProcessOrderOnScheduleAPI  createInputForAlert()- Api");
			if (YFCObject.isVoid(docExceptionInput)) {
				docExceptionInput = XMLUtil
						.createDocument(AcademyConstants.ELE_INBOX);
				docExceptionInput.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_ACTIVE_FLAG,
						AcademyConstants.STR_YES);
				docExceptionInput.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_EXCPTN_TYPE,
						AcademyConstants.STR_AVAIL_EXCEPTION);
				docExceptionInput
						.getDocumentElement()
						.setAttribute(
								AcademyConstants.STR_ORDR_HDR_KEY,
								eleOrderLine
										.getAttribute(AcademyConstants.STR_ORDR_HDR_KEY));
				docExceptionInput.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_ORDER_NO,
						docInput.getDocumentElement().getAttribute(
								AcademyConstants.ATTR_ORDER_NO));
				eleInboxRefList = docExceptionInput
						.createElement(AcademyConstants.ELE_INBOX_REF_LIST);
				XMLUtil.appendChild(docExceptionInput.getDocumentElement(),
						eleInboxRefList);
				eleInboxRef = docExceptionInput
						.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
				XMLUtil.appendChild(eleInboxRefList, eleInboxRef);
				
				/* Start Bug Fix -10322 */
				/*eleInboxRef
						.setAttribute(
								AcademyConstants.ATTR_INBOX_REFKEY,
								eleOrderLine
										.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));*/
				
				/* End Bug Fix -10322 */
				eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE,
						AcademyConstants.STR_EXCPTN_REF_VALUE);
				eleInboxRef
						.setAttribute(
								AcademyConstants.ATTR_NAME,
								eleOrderLine
										.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));

				// eleInboxRef.setAttribute("Value","Order Line Fulfillment");
				strExceptionValue = getExceptionReferenceValue(eleOrderLine);
				eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE,
						strExceptionValue);

			} else {

				eleInboxRefList = (Element) XMLUtil.getNode(docExceptionInput
						.getDocumentElement(),
						AcademyConstants.ELE_INBOX_REF_LIST);
				// eleInboxRefList = docExceptionInput
				// .getElementById("InboxReferencesList");
				if (!YFCObject.isVoid(eleInboxRefList)) {
					eleInboxRef = docExceptionInput
							.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
					XMLUtil.appendChild(eleInboxRefList, eleInboxRef);
					
					/* Start Bug Fix -10322 */
/*					eleInboxRef
							.setAttribute(
									AcademyConstants.ATTR_INBOX_REFKEY,
									eleOrderLine
											.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));*/
					
					/* End Bug Fix -10322 */
					eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE,
							AcademyConstants.STR_EXCPTN_REF_VALUE);
					eleInboxRef
							.setAttribute(
									AcademyConstants.ATTR_NAME,
									eleOrderLine
											.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
					strExceptionValue = getExceptionReferenceValue(eleOrderLine);
					eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE,
							strExceptionValue);
				}

			}
			log.endTimer(" End of AcademyProcessOrderOnScheduleAPI  createInputForAlert()- Api");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		log.verbose("************* Returning docExceptionInput***************"
				+ XMLUtil.getXMLString(docExceptionInput));
		log.verbose("************* Exiting createInputForAlert***************");

		return docExceptionInput;

	}// end - createInputForAlert

	private String getExceptionReferenceValue(Element eleCurrentOrderLine) {

		String strInboxRefValue = "";
		String strBackOrderedQty = null;
		String strBackOrder = AcademyConstants.STR_QTY_BKORDERED;
		String strSourceFrom = AcademyConstants.STR_QTY_SOURCED_FROM;
		// String strEqual = "= ";
		Element eleCurrentStatusLine = null;
		NodeList nListLineStatuses = null;
		int iNoOfStatuses = 0;
		int iNoOfKeys = 0;
		HashMap hStatusByShipNode = new HashMap();

		// String strKeys[]=new String[];

		try {
			log.beginTimer(" Begin of AcademyProcessOrderOnScheduleAPI  getExceptionReferenceValue()- Api");
			log.verbose("************* Inside getExceptionReferenceValue***************");
			strBackOrderedQty = XPathUtil.getString(eleCurrentOrderLine,
					AcademyConstants.XPATH_BKORDERED_QTY);
			if (!YFCObject.isVoid(strBackOrderedQty)) {
				strInboxRefValue = strInboxRefValue
						+ strBackOrder
						+ AcademyConstants.STR_COLON
						+ String.valueOf(new Float(strBackOrderedQty)
								.intValue());
			}
			nListLineStatuses = XMLUtil.getNodeList(eleCurrentOrderLine,
					AcademyConstants.XPATH_SCHEDULED_STATUSLINES);
			iNoOfStatuses = nListLineStatuses.getLength();
			for (int i = 0; i < iNoOfStatuses; i++) {
				eleCurrentStatusLine = (Element) nListLineStatuses.item(i);
				String strCurrShipNode = eleCurrentStatusLine
						.getAttribute(AcademyConstants.SHIP_NODE);
				String strQtyScheduled = eleCurrentStatusLine
						.getAttribute(AcademyConstants.ATTR_STAT_QTY);
				if (hStatusByShipNode.isEmpty()) {
					hStatusByShipNode.put(strCurrShipNode, strQtyScheduled);
				} else {
					if (hStatusByShipNode.containsKey(strCurrShipNode)) {
						String strPrevQty = (String) hStatusByShipNode
								.get(strCurrShipNode);
						if (!YFCObject.isVoid(strPrevQty)) {
							int iAccruedQty = new Float(strPrevQty).intValue()
									+ new Float(strQtyScheduled).intValue();
							String strAccruedQty = String.valueOf(iAccruedQty);
							hStatusByShipNode.put(strCurrShipNode,
									strAccruedQty);
						}

					} else {
						hStatusByShipNode.put(strCurrShipNode, strQtyScheduled);
					}
				}

			}

			if (!hStatusByShipNode.isEmpty()) {
				Set sMapSet = hStatusByShipNode.keySet();
				String[] strKeys = new String[sMapSet.size()];
				strKeys = (String[]) hStatusByShipNode.keySet()
						.toArray(strKeys);
				if (!YFCObject.isVoid(strKeys)) {
					iNoOfKeys = strKeys.length;
					for (int j = 0; j < iNoOfKeys; j++) {
						strInboxRefValue = strInboxRefValue
								+ AcademyConstants.STR_SEMICOLON
								+ strSourceFrom + strKeys[j]
								+ AcademyConstants.STR_COLON
								+ (String) hStatusByShipNode.get(strKeys[j]);
					}
				}
			}
			log.endTimer(" End of AcademyProcessOrderOnScheduleAPI  getExceptionReferenceValue()- Api");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		log.verbose("************* Returning Inbox Reference Line Value***************"
						+ strInboxRefValue);
		return strInboxRefValue;
	}

	private void createAlertsForAvailabiliy(Document docCreateAlertInput,
			YFSEnvironment env) {
		Document docCreateExceptionAPIOutput = null;

		log.verbose("************* Inside createAlertsForAvailabiliy***************");
		try {
			log.verbose("************* Calling createException with input***************"
							+ XMLUtil.getXMLString(docCreateAlertInput));
			docCreateExceptionAPIOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_CREATE_EXCEPTION, docCreateAlertInput);
			log.verbose("************* Output of createException ***************"
							+ XMLUtil.getXMLString(docCreateExceptionAPIOutput));
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

	}

	private void changeExceptionDetailsForOrder(
			Document docChangeExceptionInput, YFSEnvironment env) {
		Document docChangeExceptionOutput = null;
		try {
			log.verbose("************* Inside changeExceptionDetailsForOrder***************");
			log.verbose("************* Calling changeException with input***************"
							+ XMLUtil.getXMLString(docChangeExceptionInput));
			docChangeExceptionOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_CHANGE_EXCEPTION,
					docChangeExceptionInput);
			log.verbose("************* Output of changeException ***************"
							+ XMLUtil.getXMLString(docChangeExceptionOutput));
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

	}

	/*
	 * 
	 */
	private void putOrderOnAvailabilityHold(String orderHeaderKey,
			YFSEnvironment env) {

		Document docChangeOrderInput = null;
		Document docApiOutput = null;
		Element eleOrderHoldTypes = null;
		Element eleOrderHoldType = null;

		try {
			log.beginTimer(" Begin of AcademyProcessOrderOnScheduleAPI  putOrderOnAvailabilityHold()- Api");
			log.verbose("************* Inside putOrderOnAvailabilityHold***************");
			
			docChangeOrderInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			docChangeOrderInput.getDocumentElement().setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, orderHeaderKey);
			//adding this attribute based on build Audit comments - to avoid DB Record Locking
			
			docChangeOrderInput.getDocumentElement().setAttribute("SelectMethod","WAIT");
			eleOrderHoldTypes = docChangeOrderInput.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPES);
			XMLUtil.appendChild(docChangeOrderInput.getDocumentElement(), eleOrderHoldTypes);
			eleOrderHoldType = docChangeOrderInput.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPE);
			
			XMLUtil.appendChild(eleOrderHoldTypes, eleOrderHoldType);
			eleOrderHoldType.setAttribute(AcademyConstants.ATTR_HOLD_TYPE, AcademyConstants.ATTR_PART_AVAIL_HOLD);
			eleOrderHoldType.setAttribute(AcademyConstants.ATTR_STATUS, AcademyConstants.STR_HOLD_CREATED_STATUS);
			
			log.verbose("************* Calling changeOrder with input*************** "+ XMLUtil.getXMLString(docChangeOrderInput));
			
			docApiOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, docChangeOrderInput);
			
			log.verbose("************* changeOrder output***************" + XMLUtil.getXMLString(docApiOutput));
			log.endTimer(" End of AcademyProcessOrderOnScheduleAPI  putOrderOnAvailabilityHold()- Api");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

	}

	private void releaseOrderFromHold(String orderHeaderKey, YFSEnvironment env) {
		Document docChangeOrderInput = null;
		Document docChangeOrderOutput = null;
		Element eleOrderHoldTypes = null;
		Element eleOrderHoldType = null;

		try {
			log.beginTimer(" Begin of AcademyProcessOrderOnScheduleAPI  releaseOrderFromHold()- Api");
			log.verbose("************* Inside releaseOrderFromHold***************");
			docChangeOrderInput = XMLUtil
					.createDocument(AcademyConstants.ELE_ORDER);
			docChangeOrderInput.getDocumentElement().setAttribute(
					AcademyConstants.STR_ORDR_HDR_KEY, orderHeaderKey);
//			adding this attribute based on build Audit comments - to avoid DB Record Locking
			docChangeOrderInput.getDocumentElement().setAttribute(
					"SelectMethod","WAIT");
			eleOrderHoldTypes = docChangeOrderInput
					.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPES);
			XMLUtil.appendChild(docChangeOrderInput.getDocumentElement(),
					eleOrderHoldTypes);
			eleOrderHoldType = docChangeOrderInput
					.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPE);
			XMLUtil.appendChild(eleOrderHoldTypes, eleOrderHoldType);
			eleOrderHoldType.setAttribute(AcademyConstants.ATTR_HOLD_TYPE,
					AcademyConstants.ATTR_PART_AVAIL_HOLD);
			eleOrderHoldType.setAttribute(AcademyConstants.ATTR_STATUS,
					AcademyConstants.STR_HOLD_RESOLVED_STATUS);
			// log.verbose("######### Input to changeOrder to release
			// hold :::::"+XMLUtil.getXMLString(docChangeOrderInput));
			log.verbose("************* Calling changeOrder with input***************"
							+ XMLUtil.getXMLString(docChangeOrderInput));
			docChangeOrderOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_CHANGE_ORDER, docChangeOrderInput);
			log.verbose("************* changeOrder output***************"
					+ XMLUtil.getXMLString(docChangeOrderOutput));
			log.endTimer(" End of AcademyProcessOrderOnScheduleAPI  releaseOrderFromHold()- Api");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

	}

	private void resolveExceptionForOrder(String orderHeaderKey,
			YFSEnvironment env) {
		Document docResolveExceptionAPIInput = null;
		Document docResolveExceptionAPIOutput = null;
		Element eleInbox = null;

		try {
			log.beginTimer(" Begin of AcademyProcessOrderOnScheduleAPI  resolveExceptionForOrder()- Api");
			log.verbose("************* Inside resolveExceptionForOrder***************");
			docResolveExceptionAPIInput = XMLUtil
					.createDocument(AcademyConstants.ELE_RESOLN_DETAILS);
			docResolveExceptionAPIInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_IGNORE_HOOK_ERRORS,
					AcademyConstants.STR_YES);
			docResolveExceptionAPIInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_RESOLVED_BY,
					AcademyConstants.STR_RESOLVED_BY_VAL);
			eleInbox = docResolveExceptionAPIInput
					.createElement(AcademyConstants.ELE_INBOX);
			XMLUtil.appendChild(docResolveExceptionAPIInput
					.getDocumentElement(), eleInbox);
			eleInbox.setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG,
					AcademyConstants.STR_YES);
			eleInbox.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE,
					AcademyConstants.STR_AVAIL_EXCEPTION);
			eleInbox.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY,
					orderHeaderKey);
			log.verbose("************* Calling resolveException with input***************"
							+ XMLUtil.getXMLString(docResolveExceptionAPIInput));
			docResolveExceptionAPIOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_RESOLVE_EXCEPTION,
					docResolveExceptionAPIInput);
			log.verbose("************* resolveException output***************"
					+ XMLUtil.getXMLString(docResolveExceptionAPIOutput));
			log.endTimer(" End of AcademyProcessOrderOnScheduleAPI  resolveExceptionForOrder()- Api");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

	}

}
