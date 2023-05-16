package com.academy.ecommerce.sterling.sts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/*##################################################################################
 *
 * Project Name                : STS Project
 * Module                      : OMS
 * Author                      : CTS
 * Date                        : 10-JUN-2020 
 * Description				  : This class is to validate and resolve STS hold to enable
 * 								all STS to be released to a single release.
 * Change Revision
 * ---------------------------------------------------------------------------------
 * Date            Author         		Version#       Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 10-JUN-2020		CTS  	 			  1.0           	Initial version
 * 
 * ##################################################################################*/

public class AcademySTSResolveReleaseHolds {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSResolveReleaseHolds.class);
	private ArrayList<Element> listFALines = new ArrayList<Element>();
	private ArrayList<Element> listNONFALines = new ArrayList<Element>();
	private HashMap<String, ArrayList<Element>> mapOfOrderLines = null;

	public Document resolveReleaseHolds(YFSEnvironment env, Document inXML) throws Exception {
		log.verbose("Begin of AcademySTSResolveReleaseHolds.resolveReleaseHoldsMod() method");
		Element eleOrder = null;

		log.verbose(":: inXML.getNodeName() :: " + inXML.getDocumentElement().getNodeName());

		if (inXML.getDocumentElement().getNodeName().equals(AcademyConstants.ELE_ORDER)) {
			eleOrder = inXML.getDocumentElement();
		} else {
			log.verbose(" Non Order Element");
			eleOrder = (Element) inXML.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
		}

		String strDocumentType = eleOrder.getAttribute(AcademyConstants.ATTR_DOC_TYPE);

		if (!YFCObject.isVoid(strDocumentType)
				&& strDocumentType.equals(AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE)) {
			log.verbose("Order is a Transfer Order. Validate Status");

			String strMinOrderStatus = eleOrder.getAttribute(AcademyConstants.ATTR_MIN_ORDER_STATUS);

			log.verbose(" :: strMinOrderStatus :: " + strMinOrderStatus);
			Boolean bFANonFAFlag = null;
			//Changes for OMNI-90667 - Start
			if (!YFCObject.isVoid(strMinOrderStatus) && Integer.parseInt(strMinOrderStatus.substring(0, 4)) >= 3900) {
				log.verbose("Hold on Order should be resolved.");
				bFANonFAFlag = false;
				resolveOrderLineHolds(env, null, inXML, bFANonFAFlag);
			} else {
				bFANonFAFlag = true;
				mapOfOrderLines = getFAAndNonFALines(inXML);
				resolveOrderLineHolds(env, mapOfOrderLines, inXML, bFANonFAFlag);
			}
			//Changes for OMNI-90667 - End
		}

		log.verbose("End of AcademySTSResolveReleaseHolds.resolveReleaseHolds() method");

		return inXML;
	}

	/*
	 * This method is used to prepare a HashMap containing list of FA and Non FA Lines in Order.
	 * List will only be populated if all the FA or Non FA lines have MinStatus > = 3900 in order.
	 * If there is any FA and NON FA lines partially received then the list will be empty.
	 * 
	 * @param inDoc
	 * 
	 */
	
	public HashMap<String, ArrayList<Element>> getFAAndNonFALines(Document inDoc) {
		HashMap<String, ArrayList<Element>> mapOfOrderLinesLocal = new HashMap<String, ArrayList<Element>>();
		NodeList nlTOOrderLines = inDoc.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
		for (int index = 0; index < nlTOOrderLines.getLength(); index++) {
			Element eleTOOrderLine = (Element) nlTOOrderLines.item(index);

			String minLineStatus = eleTOOrderLine.getAttribute(AcademyConstants.ATTR_MIN_LINE_STATUS);
			String strPackListType = eleTOOrderLine.getAttribute(AcademyConstants.ATTR_PACKLIST_TYPE);
			if (!YFCObject.isVoid(strPackListType) && "FA".equalsIgnoreCase(strPackListType)) {
				if (!YFCObject.isVoid(minLineStatus) && Integer.parseInt(minLineStatus.substring(0, 4)) >= 3900) {

					listFALines.add(eleTOOrderLine);
				} else {
					listFALines.clear();
					break;
				}
			}
		}
		for (int index = 0; index < nlTOOrderLines.getLength(); index++) {
			Element eleTOOrderLine = (Element) nlTOOrderLines.item(index);

			String minLineStatus = eleTOOrderLine.getAttribute(AcademyConstants.ATTR_MIN_LINE_STATUS);
			String strPackListType = eleTOOrderLine.getAttribute(AcademyConstants.ATTR_PACKLIST_TYPE);
			if (YFCObject.isVoid(strPackListType)) {
				if (!YFCObject.isVoid(minLineStatus) && Integer.parseInt(minLineStatus.substring(0, 4)) >= 3900) {

					listNONFALines.add(eleTOOrderLine);
				} else {
					listNONFALines.clear();
					break;
				}
			}

		}
		if (!listFALines.isEmpty()) {
			mapOfOrderLinesLocal.put(AcademyConstants.FALINES, listFALines);
		}
		if (!listNONFALines.isEmpty()) {
			mapOfOrderLinesLocal.put(AcademyConstants.NONFALINES, listNONFALines);
		}
		log.verbose("mapOfOrderLinesLocal--->"+mapOfOrderLinesLocal);
		return mapOfOrderLinesLocal;
		
	}

	/**
	 * This method is be used to resolve the STS_RELEASE_HOLD on Sales Order
	 * by invoking a changeOrder API. This has been modified as part of OMNI-90667 
	 * to check for Both FA and Non FA lines so that they can be released independently 
	 * of each other. 
	 *  
	 * @param env,
	 * @param mapOfFANonFaLines,
	 * @param inXML,
	 * @param bFlag
	 */
	public void resolveOrderLineHolds(YFSEnvironment env,
			HashMap<String, ArrayList<Element>> mapOfFANonFaLines, Document inXML, Boolean bFlag) throws Exception {
		log.verbose("Begin of AcademySTSResolveReleaseHolds.resolveOrderLineHoldsForFAAndNonFANew() method");

		boolean isChangeOrderRequired = false;
		ArrayList<String> alOrderLines = new ArrayList<String>();
		ArrayList<Element> listFALines = null;
		ArrayList<Element> listNONFALines = null;
		ArrayList<Element> finalList = new ArrayList<Element>();
		Set<Element> newSet = new HashSet<Element>();
		boolean nodeTypeIsStore = false;
		if (bFlag) {
			if (mapOfFANonFaLines != null && !mapOfFANonFaLines.isEmpty()) {
				if (mapOfFANonFaLines.containsKey(AcademyConstants.FALINES)) {
					listFALines = mapOfFANonFaLines.get(AcademyConstants.FALINES);
					newSet = new HashSet<Element>(listFALines);
				}
				if (mapOfFANonFaLines.containsKey(AcademyConstants.NONFALINES)) {
					listNONFALines = mapOfFANonFaLines.get(AcademyConstants.NONFALINES);
					newSet.addAll(listNONFALines);
				}
				finalList = new ArrayList<Element>(newSet);
			}
		}
		log.verbose("FinalList--->" + finalList);

		/* OMNI-47442 -start */
		/*
		 * When STS2.0 Shipment is received the Line Level source controls will
		 * be removed along with STS_RELEASE_HOLD , this logic to understand if
		 * the receipt process for STS2.0
		 */
		String strShipNode = XPathUtil.getString(inXML.getDocumentElement(), AcademyConstants.XPATH_RECEIPT_SHIP_NODE);
		String strReceivingNode = XPathUtil.getString(inXML.getDocumentElement(),
				AcademyConstants.XPATH_RECEIPT_RECEIVING_NODE);
		if (!YFCObject.isVoid(strShipNode) && !YFCObject.isVoid(strReceivingNode)) {
			nodeTypeIsStore = AcademyUtil.checkNodeTypeIsStore(env,strShipNode);
		}
		/* OMNI-47442 -end */

		Document docChangeOrder = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		NodeList nlTOOrderLines = inXML.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);

		log.verbose("nlTOOrderLines Length :: " +nlTOOrderLines.getLength());
		Element eleOrderLines = docChangeOrder.createElement(AcademyConstants.ELE_ORDER_LINES);
		docChangeOrder.getDocumentElement().appendChild(eleOrderLines);
		Element eleTOOrderLine = null;
		int iLen = bFlag ? finalList.size() : nlTOOrderLines.getLength();
		for (int i = 0; i < iLen; i++) {
			if (bFlag) {
				eleTOOrderLine = finalList.get(i);
			} else {
				eleTOOrderLine = (Element) nlTOOrderLines.item(i);
			}
			log.verbose("The Elemment is New--->" + XMLUtil.getElementXMLString(eleTOOrderLine));
			// OMNI-71886 - STS Resourcing Start - Release Hold for the Received
			// line alone
			String minLineStatus = eleTOOrderLine.getAttribute("MinLineStatus");
			if (AcademyConstants.VAL_CANCELLED_STATUS.equals(minLineStatus)) {
				continue;
			}
			// OMNI-71886 - STS Resourcing - Release Hold for the Received line
			// alone

			String strSOHeaderKey = eleTOOrderLine.getAttribute(AcademyConstants.ATTR_CHAINED_FROM_ORDER_HEADER_KEY);
			if (!YFCObject.isVoid(strSOHeaderKey)) {
				docChangeOrder.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
						strSOHeaderKey);
			}

			String strHoldStatus = XPathUtil.getString(eleTOOrderLine,
					"ChainedFromOrderLine/OrderHoldTypes/OrderHoldType[@HoldType='STS_RELEASE_HOLD']/@Status");

			String strSOOrderLineKey = eleTOOrderLine.getAttribute(AcademyConstants.ATTR_CHAINED_FROM_ORDER_LINE_KEY);
			log.verbose(":: strHoldStatus::  " + strHoldStatus + " strSOOrderLineKey :: " + strSOOrderLineKey);

			if (!alOrderLines.contains(strSOOrderLineKey) && !YFCObject.isVoid(strHoldStatus)
					&& strHoldStatus.equals(AcademyConstants.STR_HOLD_CREATED_STATUS)) {

				Element eleSOOrderLine = docChangeOrder.createElement(AcademyConstants.ELE_ORDER_LINE);
				Element eleOrderHoldTypes = docChangeOrder.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPES);
				Element eleOrderHoldType = docChangeOrder.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPE);

				eleSOOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, strSOOrderLineKey);

				eleOrderHoldType.setAttribute(AcademyConstants.ATTR_HOLD_TYPE, AcademyConstants.STR_STS_RELEASE_HOLD);
				eleOrderHoldType.setAttribute(AcademyConstants.ATTR_STATUS, AcademyConstants.STR_HOLD_RESOLVED_STATUS);

				eleOrderHoldTypes.appendChild(eleOrderHoldType);
				eleSOOrderLine.appendChild(eleOrderHoldTypes);
				eleOrderLines.appendChild(eleSOOrderLine);
				isChangeOrderRequired = true;
				alOrderLines.add(strSOOrderLineKey);

				/*
				 * OMNI-47442 -STS 2.0 order allocation - prevent from ship node
				 * -START
				 */
				// Removing NOINV source control on orderline
				if (nodeTypeIsStore == true) {
					Element eleOrderLineSourcingControls = docChangeOrder
							.createElement(AcademyConstants.ELE_ORDER_LINE_SOURCING_CNTRLS);
					Element eleOrderLineSourcingCntrl = docChangeOrder
							.createElement(AcademyConstants.ELE_ORDER_LINE_SOURCING_CNTRL);
					eleOrderLineSourcingCntrl.setAttribute(AcademyConstants.ELE_NODE, strReceivingNode);
					eleOrderLineSourcingCntrl.setAttribute(AcademyConstants.INVENTORY_CHECK_CODE, "");
					eleSOOrderLine.appendChild(eleOrderLineSourcingControls);
					eleOrderLineSourcingControls.appendChild(eleOrderLineSourcingCntrl);

				}
				/*
				 * OMNI-47442 -STS 2.0 order allocation - prevent from ship node
				 * -END
				 */

			} else {
				log.verbose(" Hold ALready resolved Or Line added, :: strHoldStatus::  " + strHoldStatus
						+ " :: Line added ::" + alOrderLines.contains(strSOOrderLineKey));
			}
		}

		log.verbose("Final changeOrder input :: " + XMLUtil.getElementXMLString(docChangeOrder.getDocumentElement()));

		if (isChangeOrderRequired) {
			log.beginTimer("STS Resolve Release Holds");
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, docChangeOrder);
			log.endTimer("STS Resolve Release Holds");
		}

		log.verbose("End of AcademySTSResolveReleaseHolds.resolveOrderLineHoldsForFAAndNonFA() method");

	}


}
