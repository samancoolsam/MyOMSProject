package com.academy.ecommerce.sterling.userexits;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.YFSUserExitException;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import org.apache.commons.lang3.time.DateUtils;
import com.yantra.yfs.japi.ue.YFSProcessOrderHoldTypeUE;


/*#####################################################################################
 *
 * Project Name                : POD OMS
 * Module                      : OMS
 * Author                      : CTS
 * Date                        : 18-NOV-2022
 * Description                 : This file implements the logic to
 * 								1. Check & Resolve Holds for Free Items based on
 * 									 Shipped/Cancelled dates + SLA
 * 								2. Update TaskQ if hold resolution criteria is not met
 *
 * Change Revision
 * ------------------------------------------------------------------------------------
 * Date            Author                  Version#       Remarks/Description
 * ------------------------------------------------------------------------------------
 * 18-NOV-2022     CTS                      1.0            Initial version
 * #####################################################################################*/

public class AcademyProcessFreeItemHoldTypeUE implements YFSProcessOrderHoldTypeUE, YIFCustomApi {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyProcessFreeItemHoldTypeUE.class);

	private static String strHoldType = null;
	/**
	 *  ArrayList aryLineLevelPro stores Line Level Free/Promo Item's OrderLineKey 
	 *   <p> OrderLineKey is added in aryLineLevelPro in {@link AcademyProcessFreeItemHoldTypeUE#hasFreeOrderLines()} method. 
	 */
	private ArrayList<String> aryLineLevelPro = new ArrayList<String>();
	/**
	 *  ArrayList aryResolvedHoldLKey has OrderLineKey of FreeItem/Promo Items, whose hold needs to be resolved
	 *   
	 */
	private ArrayList<String> aryResolvedHoldLKey = new ArrayList<String>();
	/**
	 *  ArrayList aryParentLines has OrderLineKey of Order lines
	 *   <p> OrderLineKeys are added in aryParentLines in {@link AcademyProcessFreeItemHoldTypeUE#hasFreeOrderLines()} method. 
	 */
	private ArrayList<String> aryParentLines = new ArrayList<String>();
	/**
	 *  String strOrderPromoLinKey has OrderLineKey of Order level FreeItem/Promo 
	 *   <p>Promo OrderLineKeys is added in strOrderPromoLinKey in {@link AcademyProcessFreeItemHoldTypeUE#hasFreeOrderLines()} method. 
	 */
	private String strOrderPromoLinKey = null;
	/**
	 *  A Properties list can contain list of valuesto be passed as serive levle arguments
	 */
	private Properties propMap = null;
	/**
	 *  boolean isUpdateAvailableDateForLine default value false.
	 *  <p> isUpdateAvailableDateForLine will be set true if Shipdate + 14 days is greater than current time stamp
	 *  <p> flag is updated conditionally in {@link AcademyProcessFreeItemHoldTypeUE#validateLineLevelPromo(inDocUE)
	 */
	private boolean isUpdateAvailableDateForLine = false;
	/**
	 *  boolean isUpdateAvailableDateForOrder default value false.
	 *  <p> isUpdateAvailableDateForLine will be set true if Shipdate + 14 days is greater than current time stamp
	 *  <p> flag is updated conditionally in {@link AcademyProcessFreeItemHoldTypeUE#validateLineLevelPromo(inDocUE)
	 */
	private boolean isUpdateAvailableDateForOrder = false;
	/**
	 *  ArrayList arrCustomerReturnReasons has customer fault return reasoncodes 
	 *   <p> reason codes are added in arrCustomerReturnReasons in {@link AcademyProcessFreeItemHoldTypeUE#getCustomerReturnsReasonCodes()} method. 
	 */
	private ArrayList<String> arrCustomerReturnReasons = new ArrayList<>();
	/**
	 *  ArrayList arrCancelFreeItems has FreeItem/Promo Item's orderlinekeys which needs to be cancelled 
	 *   <p> orderlinekeys are added in arrCancelFreeItems in {@link AcademyProcessFreeItemHoldTypeUE#validateParentItemReturns()} method. 
	 */
	private ArrayList<String> arrCancelFreeItems = new ArrayList<>();
	/**
	 *   Document outDocGetCompOdrDlts will be not null when SO has Return lines present
	 *   <p> data is set to outDocGetCompOdrDlts in {@link AcademyProcessFreeItemHoldTypeUE#getCompleteOrderDeatils()} method.
	 */
	private Document outDocGetCompOdrDlts;
	/**
	 *  default boolean isSOTotalBelowThreshold = false
	 *  <p> isSOTotalBelowThreshold value is considered only during Order level FreeItem Cancelation logic
	 *  <p> value is updated conditionally when Updated Sales Order Total is lessthan Threshold value
	 *  <p> isSOTotalBelowThreshold is updated in {@link AcademyProcessFreeItemHoldTypeUE#validateOrderThreshold()} method.
	 */
	private boolean isSOTotalBelowThreshold = false;
	/**
	 *  default boolean bCancelFreeItem = false
	 *  <p> value is updated conditionally in {@link AcademyProcessFreeItemHoldTypeUE#validateParentItemReturns()} method.
	 */
	private boolean bCancelFreeItem=false; 
	
	@Override
	public void setProperties(Properties arg0) throws Exception {
		propMap = arg0;
	}

	/*
	 * This method reads UE Input with orderlines and promo items
	 * Invokes prepareManageTaskQInput() with available date (before adding + 14)
	 * Separate methods are invoked for calculating next available date
	 * for order level and line level promotions
	 * Returns UE output
	 */

	@Override
	public Document processOrderHoldType(YFSEnvironment env, Document inDocUE) throws YFSUserExitException {
		log.setLogAll(true);
		log.beginTimer("START - AcademyProcessFreeItemHoldTypeUE.processOrderHoldType()");
		log.verbose("Input to processOrderHoldType() :: " + XMLUtil.getXMLString(inDocUE));
		Date dtOrderAvailDate = new Date();
		Date dtLineAvailDate = new Date();
		Element inEle = inDocUE.getDocumentElement();
		Document outDocUE = null;

		try {
			strHoldType = (String) propMap.getProperty(AcademyConstants.STR_HOLD_TYPE);
			String strORderHeaderKey = inEle.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
			log.verbose("Hold type from properties :: " + strHoldType);
			String strMaxOrderStatus = XPathUtil.getString(inDocUE,AcademyConstants.XPATH_ORDER_MAXORDERSTATUS);
			double dMaxOrderStatus = Double.parseDouble(strMaxOrderStatus);
			double dLineTerminalSts = Double.parseDouble(AcademyConstants.STR_TERMINAL_STATUS);
			if(dMaxOrderStatus >= dLineTerminalSts ) {
				hasFreeOrderLines(inDocUE);
				NodeList ndHasChild = XMLUtil.getNodeList(inDocUE, AcademyConstants.XPATH_ORDER_HASDERIVEDCHILD);
				if(!YFCCommon.isVoid(ndHasChild)) {
					int iHasDerivedChild = ndHasChild.getLength();
					if(iHasDerivedChild >0) {
						log.verbose("HasDerivedChild: TRUE");
					getCustomerReturnsReasonCodes(env);
					getCompleteOrderDeatils(env,inDocUE);
					if (strOrderPromoLinKey != null) validateOrderThreshold(inDocUE);
					validateParentItemReturns(inDocUE);
					}
				}
				// call validateOrderLevelPromo() for getting taskq available date for order level promotions
				if (strOrderPromoLinKey != null && !arrCancelFreeItems.contains(strOrderPromoLinKey)) {
					dtOrderAvailDate = validateOrderLevelPromo(inDocUE);
					log.verbose("Available date returned from validateOrderLevelPromo() :: " + dtOrderAvailDate);
				}
				// call validateLineLevelPromo() for getting taskq available date for line level promotions
				if (aryLineLevelPro.size() > 0) {
					dtLineAvailDate = validateLineLevelPromo(inDocUE);
					log.verbose("Available date returned from validateLineLevelPromo() :: " + dtLineAvailDate);
				}
				if (!YFCCommon.isVoid(strORderHeaderKey)) {
					//Call method to prepare UE output
					outDocUE = prepareUEOutDocument(strORderHeaderKey);
				}
				//If Order/Line Level promo item hold cannot be resolved
				if (isUpdateAvailableDateForLine || isUpdateAvailableDateForOrder) {
					updateAvailableDate(env, inDocUE,dtOrderAvailDate,dtLineAvailDate);
				}
				if(bCancelFreeItem) {
					setFreeItemHoldAgentChangeOrderTxnObj(env,inDocUE);
				}
			}else{
				log.verbose("Status is < Shipped or Returned... Not eligible for Hold Resolution");
				outDocUE = prepareUEOutDocument(strORderHeaderKey);
				prepareManageTaskQInput(env, inDocUE, dtOrderAvailDate);
			}
		} catch (Exception e) {
			throw new YFSException(e.getMessage());
		}
		if (outDocUE!=null) {
			log.verbose("AcademyProcessFreeItemHoldTypeUE : processOrderHoldType : outDoc : "+ XMLUtil.getElementXMLString(outDocUE.getDocumentElement()));
		}
		log.endTimer("END - AcademyProcessFreeItemHoldTypeUE.processOrderHoldType()");
		return outDocUE;
	}

	/*
	 * This method prepares output UE Document to resolve hold if eligible
	 * Returns output
	 */

	private Document prepareUEOutDocument(String strORderHeaderKey) throws ParserConfigurationException {

		log.beginTimer("START - AcademyProcessFreeItemHoldTypeUE.prepareUEOutDocument()");
		Document outDocUE = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		//Hold type is read from service documents
		strHoldType = (String) propMap.getProperty(AcademyConstants.STR_HOLD_TYPE);
		Element eleOrderLines = outDocUE.createElement(AcademyConstants.ELE_ORDER_LINES);
		outDocUE.getDocumentElement().setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, strORderHeaderKey);
		outDocUE.getDocumentElement().appendChild(eleOrderLines);
		log.verbose("Arraylist of OrderLineKey's for Hold Resolution :" + aryResolvedHoldLKey);

		for (String strFreeLineKey : aryResolvedHoldLKey) {
			log.verbose("Free Order Line Key :" + strFreeLineKey);
			Element eleSOOrderLine = outDocUE.createElement(AcademyConstants.ELE_ORDER_LINE);
			eleSOOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, strFreeLineKey);
			Element eleOrderHoldTypes = outDocUE.createElement(AcademyConstants.ELE_PROCESS_HOLD_TYPES);
			Element eleOrderHoldType = outDocUE.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPE);
			eleOrderHoldType.setAttribute(AcademyConstants.ATTR_STATUS, AcademyConstants.HOLD_RESOLVE_STATUS);
			eleOrderHoldType.setAttribute(AcademyConstants.ATTR_HOLD_TYPE, strHoldType);
			eleOrderHoldTypes.appendChild(eleOrderHoldType);
			eleSOOrderLine.appendChild(eleOrderHoldTypes);
			eleOrderLines.appendChild(eleSOOrderLine);
		}

		log.verbose("Output doc of  prepareUEOutDocument() :: " + XMLUtil.getXMLString(outDocUE));
		log.endTimer("END - AcademyProcessFreeItemHoldTypeUE.prepareUEOutDocument()");

		return outDocUE;
	}

	/**
	 * @author CTS
	 * @param inDocUE : UE input
	 * @logic
	 * <p> Method is invoked for order level free item validation
	 * Returns strAvailableDate to update the task q based on the parent line status date
	 * If promoline is eligible for hold resolution corresponding line key is added to
	 * aryResolvedHoldLKey array
	 */

	private Date validateOrderLevelPromo(Document inDoc) throws Exception {
		log.beginTimer("START - AcademyProcessFreeItemHoldTypeUE.validateOrderLevelPromo()");
		log.verbose("Input to AcademyProessFreeItemHoldTypeUE.validateOrderLevelPromo() :: " + XMLUtil.getXMLString(inDoc));
		String strStatusDate = null;
		String strorderLineKey = null;
		Date strAvailableDate = null;
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		ArrayList<String> arrStatusDate = new ArrayList<String>();
		int strTeminalStatus = Integer.parseInt(AcademyConstants.STR_TERMINAL_STATUS);

		NodeList nl = XPathUtil.getNodeList(inDoc.getDocumentElement(), AcademyConstants.XPATH_ORDERLINES_LINE);
		int noOfOrderLine = nl.getLength();

		for (int i = 0; i < noOfOrderLine; i++) {
			Element eleOrderline = (Element) nl.item(i);
			strorderLineKey = eleOrderline.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			if (!YFCCommon.isVoid(strorderLineKey) && aryParentLines.contains(strorderLineKey)) {

				//Check max status of Parent line
				String strMaxStatus = eleOrderline.getAttribute(AcademyConstants.ATTR_MAXLINE_STATUS);
				log.verbose("Max Status of the line:: " + strMaxStatus);

				if (!YFCCommon.isVoid(strMaxStatus)) {
					double intMaxStatus = Double.parseDouble(strMaxStatus);

					// If status is >= shipped status, add to status date arraylist
					if (intMaxStatus >= strTeminalStatus) {
						String statusWithDate = returnStatusAndStatusDate(strorderLineKey, inDoc);
						if(!YFCCommon.isVoid(statusWithDate)) {
							strStatusDate = statusWithDate.substring(statusWithDate.indexOf(AcademyConstants.STR_COMMA) + 1,statusWithDate.length());
						}else {
							strStatusDate = getLatestShipmentDate(strorderLineKey);
						}
						arrStatusDate.add(strStatusDate);
						log.verbose("Status date array list :: " + arrStatusDate);

					} else {

						log.verbose("Status is < Shipped or cancelled... Not eligible for Hold Resolution");
						break;
					}
				}
			}
		}
		//Read from service arguments
		int dayToAdd = Integer.parseInt(propMap.getProperty(AcademyConstants.STR_NEXT_TASKQ_INTERVAL));

		//Checking if all the parent lines are >= shipped status
		if (arrStatusDate.size() == aryParentLines.size()) {

			log.verbose("Lines are shipped or Returned ");
			// Sort the status dates to fetch the latest shipped/cancelled date
			Collections.sort(arrStatusDate);
			int arrStatusDateSize = arrStatusDate.size();
			Date availbleDate = sdf.parse(arrStatusDate.get(arrStatusDateSize - 1));
			Date availbleDateToSet = DateUtils.addDays(availbleDate, dayToAdd);
			Date strCurrentAvailableDate = new Date();
			if (strCurrentAvailableDate.after(availbleDateToSet)) {
				aryResolvedHoldLKey.add(strOrderPromoLinKey);
				log.verbose("Adding " + strOrderPromoLinKey + " to resolve hold key arraylist");
			} else {
				strAvailableDate = availbleDate;
				log.verbose(
						" AcademyProessFreeItemHoldTypeUE.validateOrderLevelPromo() :: Available date :: when current day > statusdate+14 days"
								+ strAvailableDate);
				isUpdateAvailableDateForOrder = true;
			}
		} else {

			strAvailableDate = new Date();
			log.verbose(
					" AcademyProessFreeItemHoldTypeUE.validateOrderLevelPromo() :: Available date :: when any line is not shipped "
							+ strAvailableDate);
			isUpdateAvailableDateForOrder = true;
		}

		log.endTimer("END - AcademyProcessFreeItemHoldTypeUE.validateOrderLevelPromo()");

		return strAvailableDate;
	}

	/*
	 * Method returns shipped/cancelled status and latest status date for the given order line
	 * Return string format - "Status,StatusDate"
	 */

	public String returnStatusAndStatusDate(String strOrderLineKey, Document inDoc) throws Exception {

		log.beginTimer("START - AcademyProcessFreeItemHoldTypeUE.returnStatusAndStatusDate()");
		log.verbose("Input doc to AcademyProcessFreeItemHoldTypeUE.returnStatusAndStatusDate :: "
				+ XMLUtil.getXMLString(inDoc));
		log.verbose(
				"Parent OrderLineKey :: " + strOrderLineKey);
		String strStatusDate = null;
		Date dStatusDate = null;
		String strStatus = null;
		String strStatusAndStatusDate = null;
		SimpleDateFormat sdfStatusDate = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);

		NodeList nlOrderLineStatus = XPathUtil.getNodeList(inDoc,
				AcademyConstants.XPATH_SHIPPED_OR_CANCELLED + strOrderLineKey + AcademyConstants.CLOSING_BACKET);

		for (int i = 0; i < nlOrderLineStatus.getLength(); i++) {

			Element eleOrderLineStatus = (Element) nlOrderLineStatus.item(i);

			if (YFCCommon.isVoid(strStatusDate) || YFCCommon.isVoid(strStatus)) {

				strStatusDate = eleOrderLineStatus.getAttribute(AcademyConstants.ATTR_STATUS_DATE);
				dStatusDate = sdfStatusDate.parse(strStatusDate);
				strStatus = eleOrderLineStatus.getAttribute(AcademyConstants.ATTR_STATUS);
				strStatusAndStatusDate = strStatus + AcademyConstants.STR_COMMA + strStatusDate;

			} else {

				String strNewStatusDate = eleOrderLineStatus.getAttribute(AcademyConstants.ATTR_STATUS_DATE);
				Date dNewStatusDate = sdfStatusDate.parse(strNewStatusDate);

				if (dNewStatusDate.after(dStatusDate)) {

					strStatusDate = strNewStatusDate;
					strStatus = eleOrderLineStatus.getAttribute(AcademyConstants.ATTR_STATUS);
					strStatusAndStatusDate = strStatus + AcademyConstants.STR_COMMA + strStatusDate;
				}
			}
		}

		log.verbose("Status, StatusDate : " + strStatusAndStatusDate);
		log.endTimer("END - AcademyProcessFreeItemHoldTypeUE.returnStatusAndStatusDate()");

		return strStatusAndStatusDate;
	}

	/*
	 * Method is invoked for Line level free item validation
	 * Returns strAvailableDate to update the task q based on the parent line status date
	 * If Free Line is eligible for hold resolution corresponding line key is added to
	 * aryResolvedHoldLKey array
	 * Else set next available date to update task queue
	 */

	private Date validateLineLevelPromo(Document inDoc) throws Exception {

		log.beginTimer("START - AcademyProcessFreeItemHoldTypeUE.validateLineLevelPromo()");
		log.verbose(
				"Input to AcademyProcessFreeItemHoldTypeUE.validateLineLevelPromo :: " + XMLUtil.getXMLString(inDoc));
		Date dtAvailableDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		Iterator<String> itraryLineLevelPro = aryLineLevelPro.iterator();

		while (itraryLineLevelPro.hasNext()) {
			
			String[] strChildLineParentItem = itraryLineLevelPro.next().split(AcademyConstants.STR_UNDERSCORE);
			String strFreeLineKey = strChildLineParentItem[0];
			String strParentItemID = strChildLineParentItem[1];
			if(!arrCancelFreeItems.contains(strFreeLineKey)) {
			//Read the Parent OrderLineKey for free item
			String strParentOrderLineKey = XPathUtil.getString(inDoc, AcademyConstants.XPATH_PARENT_ORDERLINE_WCSID
					+ strParentItemID + AcademyConstants.XPATH_PARENT_ORDERLINE_KEY);

			//Check if line has Status in Shipped/Picked Up/Cancelled
			NodeList nlOrderLine = XPathUtil.getNodeList(inDoc,
					AcademyConstants.XPATH_OLINE_STATUS + strParentOrderLineKey + AcademyConstants.CLOSING_BACKET);
			Date currentDate = new Date();
			if (nlOrderLine.getLength() > 0) {
				//Call method to get the statusdate when the line is shipped/cancelled
				String strStatusAndStatusDate = returnStatusAndStatusDate(strParentOrderLineKey, inDoc);
				String strStatusDate = null;
				//String strStatus = null;
				int dayToAdd = Integer.parseInt(propMap.getProperty(AcademyConstants.STR_NEXT_TASKQ_INTERVAL));
				log.verbose("Days to add from property :: " + dayToAdd);

				if (!YFCCommon.isVoid(strStatusAndStatusDate)) {
					String[] strStats = strStatusAndStatusDate.split(AcademyConstants.STR_COMMA);
					strStatusDate = strStats[1];
				} else {
					strStatusDate = getLatestShipmentDate(strParentOrderLineKey);
				}
				Date shippedDate = sdf.parse(strStatusDate);
				Date shippedDateSLA = DateUtils.addDays(shippedDate, dayToAdd);
				if (shippedDateSLA.compareTo(currentDate) <= 0) {
					log.verbose("Shipped Date + 14 days is less than Current date");
					aryResolvedHoldLKey.add(strFreeLineKey);
				} else {
					dtAvailableDate = shippedDate.after(dtAvailableDate) ? dtAvailableDate : shippedDate;
					log.verbose("Avalilable date when Shipped Date + 14 days is greater than Current date :: "+ dtAvailableDate);
					isUpdateAvailableDateForLine = true;
				}
			} else {
				log.verbose("With respect to current Date item is not shipped/cancelled yet, Setting current date as next available date");
				isUpdateAvailableDateForLine = true;
			}
		 }
		}

		log.verbose("Avalilable date from AcademyProcessFreeItemHoldTypeUE.validateLineLevelPromo :: " + dtAvailableDate);
		log.verbose("Set isUpdateAvailableDateForLine to :: " + isUpdateAvailableDateForLine);
		log.endTimer("END - AcademyProcessFreeItemHoldTypeUE.validateLineLevelPromo()");

		return dtAvailableDate;
	}

	/* Method prepares input to invoke manageTaskQueue and sets the i/p to transaction
	 * object FreeItemHoldAgentTxnObj */

	public void prepareManageTaskQInput(YFSEnvironment env, Document inDocUE, Date strAvailableDate) {

		log.beginTimer("START - AcademyProcessFreeItemHoldTypeUE.prepareManageTaskQInput()");
		log.verbose("Inside AcademyProcessFreeItemHoldTypeUE.prepareManageTaskQInput :: Available Date ::"	+ strAvailableDate);
		Document inDocTaskQueue = null;

		try {
			Element inEleUE = inDocUE.getDocumentElement();
			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			int daystoAdd = Integer.parseInt(propMap.getProperty(AcademyConstants.STR_NEXT_TASKQ_INTERVAL));
			String strORderHeaderKey = inEleUE.getAttribute(AcademyConstants.STR_ORDR_HDR_KEY);
			String strTransactionId = inEleUE.getAttribute(AcademyConstants.ATTR_TRANS_ID);
			Date strNextAvailableDate = DateUtils.addDays(strAvailableDate, daystoAdd);
			inDocTaskQueue = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
			Element inEleTQ = inDocTaskQueue.getDocumentElement();
			inEleTQ.setAttribute(AcademyConstants.ATTR_OPERATION, AcademyConstants.STR_ACTION_MODIFY);
			inEleTQ.setAttribute(AcademyConstants.ATTR_DATA_TYPE, AcademyConstants.STR_ORDR_HDR_KEY);
			inEleTQ.setAttribute(AcademyConstants.ATTR_DATA_KEY, strORderHeaderKey);
			inEleTQ.setAttribute(AcademyConstants.ATTR_TRANSID, strTransactionId);
			inEleTQ.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, sdf.format(strNextAvailableDate));
			env.setTxnObject(AcademyConstants.FREE_ITEM_HOLD_AGENT_TXN_OBJ, inDocTaskQueue);
			log.verbose("FreeItemHoldAgentTxnObj :: " + XMLUtil.getElementXMLString(inDocTaskQueue.getDocumentElement()));
		} catch (Exception e) {
			throw (new YFSException(e.getMessage()));
		}
		log.endTimer("END - AcademyProcessFreeItemHoldTypeUE.prepareManageTaskQInput()");
	}

	/**
	 * @author CTS
	 * @param env
	 * @param inDoc : UE input
	 * @logic
	 * <p> {@code update {<strong> aryParentLines, strOrderPromoLinKey, aryLineLevelPro}
	 * @throws Exception
	 */
	private void hasFreeOrderLines (Document inDoc) throws Exception{
		log.beginTimer("START AcademyProcessFreeItemHoldTypeUE.processOrderHoldType.hasFreeOrderLines");
		List<Node> nlLineLevelPromo = XMLUtil.getElementListByXpath(inDoc, AcademyConstants.XPATH_ORDER_LINE);
		strHoldType = (String) propMap.getProperty(AcademyConstants.STR_HOLD_TYPE);
		log.verbose("Hold type from properties :: " + strHoldType);
		for (int iCount = 0; iCount < nlLineLevelPromo.size(); iCount ++) {
			Element eleOL = (Element) nlLineLevelPromo.get(iCount);
			//FREE_ITEM list with hold in created status
			NodeList nHold = XPathUtil.getNodeList(eleOL,AcademyConstants.XPATH_HOLD_TYPE_FREE_ITEM + strHoldType + AcademyConstants.CLOSING_BACKET);
			//Promo Lines List
			NodeList nHoldIsPromo = XPathUtil.getNodeList(eleOL, AcademyConstants.XPATH_EXTN_PROMO_ITEM);
			log.verbose("nHold length :" + nHold.getLength());
			log.verbose("nHoldIsPromo length :" + nHold.getLength());
			String strOrderLineKey = eleOL.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);

			// if line doesn't have promo item add orderline to parents array list
			if (nHold.getLength() <= 0 && nHoldIsPromo.getLength() <= 0) {
				aryParentLines.add(strOrderLineKey);
			}
				/* For line that has promo item, checking if it is line level or order level promo
				   item and adding it to array list in case of line level promo */
			else if (nHold.getLength() > 0 && nHoldIsPromo.getLength() > 0) {
				if (YFCCommon.equalsIgnoreCase(XPathUtil.getString(eleOL, AcademyConstants.XPATH_EXTN_PROMO_TYPE),AcademyConstants.ELE_ORDER)) {
					strOrderPromoLinKey = strOrderLineKey;
				}else {
					//Concatenate Child free item's OLK with Parent line's WCS OrderItemIdentifier
					String strExtnParentID = XPathUtil.getString(eleOL, AcademyConstants.XPATH_EXTN_PROMO_PARENTID);
					String strChildLineParentItem = strOrderLineKey + "_" + strExtnParentID;
					aryLineLevelPro.add(strChildLineParentItem);
				}
			}
		}
		log.verbose("Order level promo line :: " + strOrderPromoLinKey);
		log.verbose("Array of line level promo lines :: " + aryLineLevelPro);
		log.verbose("Array of Parent OrderlineKey :: " + aryParentLines );
		log.endTimer("END AcademyProcessFreeItemHoldTypeUE.processOrderHoldType.hasFreeOrderLines");
	}

	/**
	 * @author CTS
	 * @param env : env
	 * @logic
	 * <p> This method takes env object as input. Invokes getCommonCodeList API and populates the array list with customer fault (only) reason codes.
	 * <p> update <strong> arrCustomerReturnReasons
	 * @return void
	 */
	private void getCustomerReturnsReasonCodes(YFSEnvironment env){
		log.beginTimer("START :: AcademyProcessFreeItemHoldTypeUE.getCustomerReturnsReasonCodes :: START");
		try {
			Document inDocGetCC = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			Element inEleGetCC = inDocGetCC.getDocumentElement();
			inEleGetCC.setAttribute(AcademyConstants.ATTR_CODE_TYPE, AcademyConstants.RETURN_REASON);
			inEleGetCC.setAttribute(AcademyConstants.CODE_LONG_DESC, AcademyConstants.STR_RTN_CUSTOMER_FAULT);
			inEleGetCC.setAttribute(AcademyConstants.ORG_CODE_ATTR, AcademyConstants.DSV_ENTERPRISE_CODE);
			inEleGetCC.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.STR_RETURN_DOCTYPE);
			log.verbose("getCustomerReturnsReasonCodes.getCommonCodeList.inDoc :: " + XMLUtil.getXMLString(inDocGetCC));
			Document outDocGetCC = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST, inDocGetCC);
			log.verbose("getCustomerReturnsReasonCodes.getCommonCodeList.outDoc :: " + XMLUtil.getXMLString(outDocGetCC));
			NodeList nCommonCode = XPathUtil.getNodeList(outDocGetCC, AcademyConstants.XPATH_COMMONCODE);
			List<Node> lstNdCC = IntStream.range(0, nCommonCode.getLength()).mapToObj(nCommonCode::item).collect(Collectors.toList());
			lstNdCC.stream().forEach(ccNode -> {
				Element ccEle = (Element) ccNode;
				arrCustomerReturnReasons.add(ccEle.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE));
			});
			log.verbose("getCustomerReturnsReasonCodes.getCommonCodeList.arrCustomerReturnReasons :: " + arrCustomerReturnReasons);
		} catch (Exception e) {
			throw new YFSException(e.getMessage());
		}
		log.endTimer("END :: AcademyProcessFreeItemHoldTypeUE.getCustomerReturnsReasonCodes :: END");
	}

	/**
	 * @author CTS
	 * @param env : env
	 * @param inDocUE : UE input
	 * @logic
	 * <p> invokes getCompleteOrderDetails & update <strong> outDocGetCompOdrDlts
	 */
	private void getCompleteOrderDeatils(YFSEnvironment env,Document inDocUE){
		log.beginTimer("START :: AcademyProcessFreeItemHoldTypeUE.getCompleteOrderDeatils :: START");
		try {
			String strSOHdrKey = XPathUtil.getString(inDocUE, "Order/@OrderHeaderKey");
			Document inDocGetCopmOdrDlt = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			Element inEleGetCopmOdrDlt = inDocGetCopmOdrDlt.getDocumentElement();
			inEleGetCopmOdrDlt.setAttribute(AcademyConstants.STR_DOCUMENT_TYPE, AcademyConstants.STR_DOCUMENT_TYPE_VALUE);
			inEleGetCopmOdrDlt.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.A_ACADEMY_DIRECT);
			inEleGetCopmOdrDlt.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, strSOHdrKey);
			log.verbose("getCompleteOrderDetails :: inDoc " + XMLUtil.getXMLString(inDocGetCopmOdrDlt));
			env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, AcademyConstants.TEMPLATE_GET_COMPLETE_ORDER_DETAILS_CUST_RTN);
			outDocGetCompOdrDlts = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, inDocGetCopmOdrDlt);
			env.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS);
			log.verbose("getCompleteOrderDetails :: outDoc " + XMLUtil.getXMLString(outDocGetCompOdrDlts));
		} catch (Exception e) {
			throw new YFSException(e.getMessage());
		}
		log.endTimer("END :: AcademyProcessFreeItemHoldTypeUE.getCompleteOrderDeatils :: END");
	}

	/**
	 * @author CTS
	 * @param env
	 * @param inDoc : UE input
	 * @logic
	 * {@code <p> This method evaluates if current sales order total is greater than or equal to the OrderPromo Threshold or not and updates global variable with true or false. }
	 * @return void
	 */
	private void validateOrderThreshold(Document inDoc) throws Exception {
		log.beginTimer("START - AcademyProcessFreeItemHoldTypeUE.validateOrderThreshold");
		double dReturnsTotal = 0.00;
		double dSOTotalAmount = 0.00;
		double dSORemainingTotal = 0.00;
		double dOrderThresholdValue = 0.00;
		String strExtnOrderPromoThreshold = XPathUtil.getString(inDoc, AcademyConstants.XPATH_ORDER_EXTN_ORDERPROMOTHRESHOLD);
		NodeList ndiROLinePrice = XPathUtil.getNodeList(outDocGetCompOdrDlts, AcademyConstants.XPATH_RO_OVERALLTOTALS);
		if (!YFCCommon.isVoid(strExtnOrderPromoThreshold) && !YFCCommon.isVoid(ndiROLinePrice)) {
			int iLineProce = ndiROLinePrice.getLength();
			for (int priceCount = 0; priceCount < iLineProce; priceCount++) {
				Element eleLinePrice = (Element) ndiROLinePrice.item(priceCount);
				String strLineTotal = eleLinePrice.getAttribute("GrandTotal");
				if (!YFCCommon.isVoid(strLineTotal)) {
					double dLineTotal = Double.valueOf(strLineTotal);
					dReturnsTotal = dReturnsTotal + dLineTotal;
				}
			}
		    String strCurrentSOTotal = XPathUtil.getString(outDocGetCompOdrDlts, AcademyConstants.XPATH_SO_GRANDTOTAL);
			dOrderThresholdValue = Double.valueOf(strExtnOrderPromoThreshold);
			dSOTotalAmount = Double.valueOf(strCurrentSOTotal);
			dSORemainingTotal = dSOTotalAmount - dReturnsTotal;
			isSOTotalBelowThreshold = Double.compare(dSORemainingTotal, dOrderThresholdValue) >= 0 ? false : true;
			log.verbose("Sales Order Total :: " + dSOTotalAmount);
			log.verbose("Return Order Total :: " + dReturnsTotal);
			log.verbose("SO total - RO Total :: " + dSORemainingTotal);
			log.verbose("Sales Order Threshold Limit :: " + dOrderThresholdValue);
			log.verbose("AcademyProcessFreeItemHoldTypeUE.validateOrderThreshold set isBelowThreshold :: " + isSOTotalBelowThreshold);
		}
		log.endTimer("END - AcademyProcessFreeItemHoldTypeUE.validateOrderThreshold");
	}

	private void updateAvailableDate(YFSEnvironment env, Document inDocUE, Date dtOrderAvailDate,Date dtLineAvailDate){
		log.beginTimer("START updateAvailableDate");
		Date strAvailableDate = new Date();

		//If both Order&Line Level promo item hold cannot be resolved and available date to be updated
		//Update date based on earliest date from dtOrderAvailDate and dtLineAvailDate
		if (isUpdateAvailableDateForLine && isUpdateAvailableDateForOrder) {

			strAvailableDate = dtOrderAvailDate.after(dtLineAvailDate) ? dtLineAvailDate : dtOrderAvailDate;

		}
		//If only Line Level promo item hold cannot be resolved and available date to be updated
		//Update date as dtLineAvailDate
		else if (isUpdateAvailableDateForLine) {

			strAvailableDate = dtLineAvailDate;

		}
		//If only Order Level promo item hold cannot be resolved and available date to be updated
		//Update date as dtOrderAvailDate
		else if (isUpdateAvailableDateForOrder) {

			strAvailableDate = dtOrderAvailDate;
		}
		//Invoke manageTaskQueue
		prepareManageTaskQInput(env, inDocUE, strAvailableDate);
		log.endTimer("END updateAvailableDate");
	}

	/**
	 * @author CTS
	 * @param inDocUE : UE input
	 * @logic
	 * <p> This Method updates <strong> arrCancelFreeItems </strong> with free item order line keys which are eligible to be canceled  
	 */
	private void validateParentItemReturns(Document inDocUE) {
		log.beginTimer("START validateParentItemReturns");
		try {
			// Order Level Promo Validation
			if (isSOTotalBelowThreshold && strOrderPromoLinKey!=null) {
				for (String strLineKey : aryParentLines) {
					boolean bEligilbleForCancelation = validateFreeItemCancelation(strLineKey);
					if (bEligilbleForCancelation) {
						arrCancelFreeItems.add(strOrderPromoLinKey);
						bCancelFreeItem = true;
						log.verbose("Order Level Promo Free Item :: " + strOrderPromoLinKey );
						log.verbose("Eligible for cancelation of Free Item :: " + bCancelFreeItem );
						break;
					}
				}
			}
			// Line Level Promo Validation
			if (!aryLineLevelPro.isEmpty() && aryLineLevelPro.size() > 0) {
				Iterator<String> itraryLineLevelPro = aryLineLevelPro.iterator();
				while (itraryLineLevelPro.hasNext()) {
					String[] strChildLineParentItem = itraryLineLevelPro.next().split(AcademyConstants.STR_UNDERSCORE);
					String strParentItemID = strChildLineParentItem[1];
					String strParentOrderLineKey = XPathUtil.getString(inDocUE,
							AcademyConstants.XPATH_PARENT_ORDERLINE_WCSID + strParentItemID + AcademyConstants.XPATH_PARENT_ORDERLINE_KEY);
					boolean bEligilbleForCancelation = validateFreeItemCancelation(strParentOrderLineKey);
					if (bEligilbleForCancelation) {
						String strFreeLineKey = strChildLineParentItem[0];
						arrCancelFreeItems.add(strFreeLineKey);
						bCancelFreeItem=true;
						log.verbose("Line Level Promo Free Item :: " + strFreeLineKey );
						log.verbose("Eligible for cancelation of Free Item :: " + bCancelFreeItem );
					}
				}
			}
		} catch (Exception e) {
			throw new YFSException(e.getMessage());
		}
		log.endTimer("END validateParentItemReturns");
	}
	/**
	 * @author CTS
	 * @param String : OrderLineKey
	 * @logic
	 * <p> This Method evalidates if Return Reason is due to the customer fault.
	 * @return  return <strong> True </strong> if Return Reason Code is Customers Fault else <strong> False </strong>
	 */
	private boolean validateFreeItemCancelation(String strOrderLineKey){
		log.beginTimer("START validateFreeItemCancelation");
		boolean bEligilbleForCancelation = false;
		ArrayList<String> arryReturnReasons = new ArrayList<>();
		NodeList ndlRtnLines;
		try {
			ndlRtnLines = XPathUtil.getNodeList(outDocGetCompOdrDlts,
					AcademyConstants.XPATH_FREEITEM_RTN_LINES_USEING_DERIVED_LNKEY + strOrderLineKey + AcademyConstants.CLOSING_BACKET);
			if(!YFCCommon.isVoid(ndlRtnLines)) {
			int iRtnLines = ndlRtnLines.getLength();
			if (iRtnLines > 0) {
				for (int jCount = 0; jCount < iRtnLines; jCount++) {
					Element eleRtnOdrLine = (Element) ndlRtnLines.item(jCount);
					String strReturnReason = eleRtnOdrLine.getAttribute(AcademyConstants.ATTR_RETURN_REASON);
					String strOrderHeaderKey = eleRtnOdrLine.getAttribute(AcademyConstants.STR_ORDR_HDR_KEY);
					String strRtnOrderNo = XPathUtil.getString(outDocGetCompOdrDlts,
							AcademyConstants.XPATH_RETURNORDER_WITH_OHY_OPEN_BRACKET + strOrderHeaderKey + AcademyConstants.XPATH_ORDERNO_CLOSE_BRACKET);
					String strRtnEntryType = XPathUtil.getString(outDocGetCompOdrDlts,
							AcademyConstants.XPATH_RETURNORDER_WITH_OHY_OPEN_BRACKET + strOrderHeaderKey + AcademyConstants.XPATH_ENTRYTYPE_CLOSE_BRACKET);
					boolean isWCSorCCReturn = !strRtnOrderNo.startsWith(AcademyConstants.ATTR_Y) || AcademyConstants.VAL_CALL_CENTER.equalsIgnoreCase(strRtnEntryType);
					if (!YFCCommon.isVoid(strReturnReason) && isWCSorCCReturn) {
						strReturnReason = strReturnReason.startsWith(AcademyConstants.ATTR_ZERO) ? strReturnReason.substring(1, strReturnReason.length()) : strReturnReason;
						arryReturnReasons.add(strReturnReason);
					}
				}
				Iterator<String> itreturnResons = arryReturnReasons.iterator();
				while (itreturnResons.hasNext()) {
					String strReasons = itreturnResons.next();
					if (arrCustomerReturnReasons.contains(strReasons)) {
						bEligilbleForCancelation = true;
						break;
					}
				}
			}
		}
		} catch (Exception e) {
			throw new YFSException(e.getMessage());
		}
		log.verbose("validateFreeItemCancelation :: " + bEligilbleForCancelation);
		log.endTimer("END validateFreeItemCancelation");
		return bEligilbleForCancelation;
	}
	
	private void setFreeItemHoldAgentChangeOrderTxnObj(YFSEnvironment env, Document inDocUE) {
		Document docChangeOrder;
		try {
			String strSOrderNo = XPathUtil.getString(inDocUE, AcademyConstants.XPATH_ORDER_NO);
			String strOrderHeaderKey = XPathUtil.getString(inDocUE, AcademyConstants.XPATH_ORDERHEADER_KEY);
			docChangeOrder = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			Element eleChangeOrder = docChangeOrder.getDocumentElement();
			eleChangeOrder.setAttribute(AcademyConstants.STR_DOCUMENT_TYPE, AcademyConstants.STR_DOCUMENT_TYPE_VALUE);
			eleChangeOrder.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
			eleChangeOrder.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);
			eleChangeOrder.setAttribute(AcademyConstants.ATTR_SELECT_METHOD, AcademyConstants.STR_WAIT);
			eleChangeOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO, strSOrderNo);
			eleChangeOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
			Element eleOrderLines = docChangeOrder.createElement(AcademyConstants.ELEM_ORDER_LINES);
			eleChangeOrder.appendChild(eleOrderLines);
			for (String strLineKey : arrCancelFreeItems) {
				Element eleOrderLine = docChangeOrder.createElement(AcademyConstants.ELEM_ORDER_LINE);
				eleOrderLines.appendChild(eleOrderLine);
				eleOrderLine.setAttribute(AcademyConstants.STR_ACTION, AcademyConstants.STR_CANCEL);
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, strLineKey);
			}
			env.setTxnObject(AcademyConstants.FREE_ITEM_HOLD_AGENT_CHANGE_ORDER_TXN_OBJ, docChangeOrder);
			log.verbose("FreeItemHoldAgentChangeOrderTxnObj :: " + XMLUtil.getElementXMLString(docChangeOrder.getDocumentElement()));
		} catch (Exception e) {
			throw new YFSException(e.getMessage());
		}
	}
	
	private String getLatestShipmentDate(String strOrderLineKey) {
		log.beginTimer("START getLatestShipmentDate");
		NodeList ndlShipments;
		Date dtLatestShipDate = new Date();
		String strLatestShipDate = null;
		ArrayList<String> arrStatusDate = new ArrayList<String>();
		try {
			ndlShipments = XPathUtil.getNodeList(outDocGetCompOdrDlts,
					AcademyConstants.XPATH_FREEITEM_RTN_ACTSHPDATE_OPN + strOrderLineKey + AcademyConstants.XPATH_FREEITEM_RTN_ACTSHPDATE_CLOSE);
			if (!YFCCommon.isVoid(ndlShipments)) {
				List<Node> lstNdShipments = IntStream.range(0, ndlShipments.getLength()).mapToObj(ndlShipments::item).collect(Collectors.toList());
				for (Node shipment : lstNdShipments) {
					Element eleShipment = (Element) shipment;
					String strActualDeliveryDate = eleShipment.getAttribute(AcademyConstants.ATTR_ACTUAL_SHIPMENT_DATE);
					if (!YFCCommon.isVoid(strActualDeliveryDate)) {
						arrStatusDate.add(strActualDeliveryDate);
					}
				}
				if (arrStatusDate.isEmpty()) {
					strLatestShipDate = dtLatestShipDate.toString();
				} else {
					log.verbose("Shipments dates :: " + arrStatusDate.toString());
					Collections.sort(arrStatusDate);
					log.verbose("Sorted Shipments dates :: " + arrStatusDate.toString());
					strLatestShipDate = arrStatusDate.get(arrStatusDate.size() - 1);
				}
			} else {
				strLatestShipDate = dtLatestShipDate.toString();
			}
		} catch (Exception e) {
			throw new YFSException(e.getMessage());
		}
		log.verbose("getLatestShipmentDate :: "+ strLatestShipDate);
		log.endTimer("END getLatestShipmentDate");
		return strLatestShipDate;
	}
	
}