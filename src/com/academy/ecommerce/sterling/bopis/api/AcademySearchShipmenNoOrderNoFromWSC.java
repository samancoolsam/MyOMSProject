package com.academy.ecommerce.sterling.bopis.api;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.w3c.dom.*;
import com.academy.ecommerce.sterling.los.XMLUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XPathUtil;
import com.comergent.api.xml.XMLUtils;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import java.util.Date;
import java.text.DateFormat;

/**
 * This class is used to return the Search results for OOB and STS search screens of WebSOM based on Search criteria  
 * @author Nandhini Selvaraj
 */

public class AcademySearchShipmenNoOrderNoFromWSC {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademySearchShipmenNoOrderNoFromWSC.class);

	/*
	 * Displays search result based on SO Shipments 
	 * For STS Order - If no SO Shipment is available, search result is based on TO Order level
	 * Search with container - grouped based on TO Order (if no SO shipment)
	 * Search with container - grouped based on SO Order (if SO shipment is present)
	 * Additional logic to get the list of TO containers for Sales Order based on ExtnSOShipmentNo stamped at TO Shipment Container level
	 * Re-written the class for BOPIS Save The Sale functionality to display multiple TOs
	 */

	public Document searchShipmentNoOrderNo(YFSEnvironment env, Document inDoc) throws Exception {

		log.beginTimer(this.getClass() + ".searchShipmentNoOrderNo");
		log.verbose("Entering AcademySearchShipmenNoOrderNoFromWSC.searchShipmentNoOrderNo() with Input document : \n"
				+ XMLUtil.getString(inDoc));

		Element eleSearchIndoc = inDoc.getDocumentElement();
		Document docGetShipmentListOut = null;
		Document docFinalShipmentListOut = null;
		Document docGetShipmentListWithOrderNo = null;
		String strCurbsideOpted = "";
		Document docGetShipmentList=null;
		String strShowRelatedOrder = eleSearchIndoc.getAttribute("ShowRelatedOrders");//OMNI-71289
		//OMNI-102321--START
		String strItemID = eleSearchIndoc.getAttribute(AcademyConstants.ATTR_ITEM_ID);
			Element elePaginate = (Element) XPathUtil.getNodeList(eleSearchIndoc, "//Shipment/Paginate").item(0);
		log.verbose("ItemID****************" +strItemID);
		if(!YFCObject.isVoid(strItemID)) {
			log.verbose("inside IF block if ItemId is present");
			Document getShipmentListWithItemIDDoc = null;
			Document docShipmenListtInput = XmlUtils.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element eleShipmentListInput = docShipmenListtInput.getDocumentElement();
			eleShipmentListInput.setAttribute(AcademyConstants.ATTR_CREATETS,
					getFromDate(AcademyConstants.STR_SIMPLE_DATE_PATTERN));
			eleShipmentListInput.setAttribute(AcademyConstants.ATTR_CREATETS_QRY_TYPE, AcademyConstants.GT_QRY_TYPE);
			eleShipmentListInput.setAttribute(AcademyConstants.SHIP_NODE,eleSearchIndoc.getAttribute(AcademyConstants.SHIP_NODE));
			eleShipmentListInput.setAttribute(AcademyConstants.STR_SEARCH_TYPE,"OOBSearch");
			Element eleShipemntLines = docShipmenListtInput.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
			Element eleShipemntLine = docShipmenListtInput.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
			eleShipmentListInput.appendChild(eleShipemntLines);
			eleShipemntLines.appendChild(eleShipemntLine);
			eleShipemntLine.setAttribute(AcademyConstants.ATTR_ITEM_ID, strItemID);
			Element eleComplexQuery = docShipmenListtInput.createElement(AcademyConstants.COMPLEX_QRY_ELEMENT);
			Element eleOr = docShipmenListtInput.createElement(AcademyConstants.COMPLEX_OR_ELEMENT);
			Element eleExp1 = docShipmenListtInput.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
			Element eleExp2 = docShipmenListtInput.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
			eleExp1.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.STATUS);
			eleExp1.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL);
			eleExp2.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.STATUS);
			eleExp2.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STATUS_BACKROOM_PICK_IN_PROGRESS_VAL);
			eleOr.appendChild(eleExp1);
			eleOr.appendChild(eleExp2);
			eleComplexQuery.appendChild(eleOr);
			eleShipmentListInput.appendChild(eleComplexQuery);
			Element eleOrderBy = docShipmenListtInput.createElement(AcademyConstants.ELE_ORDERBY);
			Element eleAttribute = docShipmenListtInput.createElement(AcademyConstants.ELE_ATTRIBUTE);
			eleAttribute.setAttribute(AcademyConstants.ATTR_DESC_SHORT, AcademyConstants.STR_YES);
			eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, "ExpectedShipmentDate");
			eleOrderBy.appendChild(eleAttribute);
			eleShipmentListInput.appendChild(eleOrderBy);
			
			Element eleInPaginate = (Element) docShipmenListtInput.importNode(elePaginate, true);
			eleShipmentListInput.appendChild(eleInPaginate);
			
			log.verbose("Input to AcademyGetShipmentListForWSC Service docShipmenListtInput:: "+ XMLUtil.getString(docShipmenListtInput));
			getShipmentListWithItemIDDoc = AcademyUtil.invokeService(env, AcademyConstants.SERV_ACADEMY_GET_SHIPMENT_LIST_FOR_WSC, docShipmenListtInput);
				log.verbose("Output to AcademyGetShipmentListForWSC Service docShipmenListtInput:: "+ XMLUtil.getString(getShipmentListWithItemIDDoc));
			log.verbose("Output to AcademyGetShipmentListForWSC Service docShipmenListtInput:: "
					+ XMLUtil.getString(getShipmentListWithItemIDDoc));
					
			getShipmentListWithItemIDDoc = removeShipNodeElement(getShipmentListWithItemIDDoc);
			return getShipmentListWithItemIDDoc;
				
		}
		//OMNI-102321--END
			// OMNI-102102 START
		log.verbose("OMNI-102102 START");
		String sDelContainerListFlag = eleSearchIndoc.getAttribute(AcademyConstants.ATTR_DLVR_CONTAINER_LIST_FLAG);
		if (!YFCObject.isVoid(sDelContainerListFlag)
				&& AcademyConstants.STR_YES.equalsIgnoreCase(sDelContainerListFlag)) {
				
			docFinalShipmentListOut = getDeliveredInTransitSTSShipListForWSC(env, AcademyConstants.COMPLEX_QRY_TYPE_EQ,
					eleSearchIndoc.getAttribute(AcademyConstants.SHIP_NODE), AcademyConstants.ATTR_DLVR_CONTAINER_LIST_FLAG,elePaginate);
			docFinalShipmentListOut = removeShipNodeElement(docFinalShipmentListOut);
			return docFinalShipmentListOut;
		}
		log.verbose("OMNI-102102 END");
		// OMNI-102102 END
		
		// OMNI-102286 START
		log.verbose("OMNI-102286 START");
		String sIntransitContainerListFlag = eleSearchIndoc
				.getAttribute(AcademyConstants.ATTR_INTRANSIT_CONTAINER_LIST_FLAG);
		if (!YFCObject.isVoid(sIntransitContainerListFlag) && AcademyConstants.STR_YES.equalsIgnoreCase(sIntransitContainerListFlag)) {
		
			docFinalShipmentListOut = getDeliveredInTransitSTSShipListForWSC(env, AcademyConstants.STR_NE,
					eleSearchIndoc.getAttribute(AcademyConstants.SHIP_NODE), AcademyConstants.ATTR_INTRANSIT_CONTAINER_LIST_FLAG, elePaginate);
			docFinalShipmentListOut = removeShipNodeElement(docFinalShipmentListOut);
			return docFinalShipmentListOut;
		}
		log.verbose("OMNI-102286 END");
		// OMNI-102286 END
		
		// OMNI - 10548 Curbside Shipment List
		Element eleExtn = (Element) eleSearchIndoc.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);

		if (eleExtn.hasAttribute(AcademyConstants.ATTR_EXTN_CURSIDE_PICK_OPTED)) {
			strCurbsideOpted = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_CURSIDE_PICK_OPTED);
		}
		
		if (!YFCCommon.isVoid(strCurbsideOpted) && !strCurbsideOpted.equals(AcademyConstants.STR_YES)) {
			log.verbose("Curbside flag : " + strCurbsideOpted);
			eleSearchIndoc.removeAttribute(AcademyConstants.ATTR_APPOINTMENT_NO_QRYTYPE);
		}
		// OMNI - 10548 Curbside Shipment List

		log.verbose("OMNI - 10548 Curbside Shipment List");
		// Fetch the container number from the input document
		Element eleContainers = XMLUtils.getElementByName(eleSearchIndoc, AcademyConstants.ELE_CONTAINERS);
		Element eleContainer = (Element) eleSearchIndoc.getElementsByTagName(AcademyConstants.ELE_CONTAINER).item(0);
		String strContainerNo = eleContainer.getAttribute(AcademyConstants.ATTR_CONTAINER_NO);
		log.verbose("strContainerNo: "+strContainerNo);
		
		// Check if Container number is used as search term
		if (!YFCCommon.isVoid(strContainerNo)) {
			log.verbose("Invoking getShipmentListFromContainerNo to get the container list");
			docFinalShipmentListOut = getShipmentListFromContainerNo(env, inDoc);
			//OMNI-105859 Start
			docFinalShipmentListOut = removeShipNodeElement(docFinalShipmentListOut);
			//OMNI-105859 End
			return docFinalShipmentListOut;
		} else {
			//Remove the Containers element from inDoc
			eleSearchIndoc.removeChild(eleContainers);
		}

		// Fetch the Order number from inDoc
		Element eleOrder = (Element) eleSearchIndoc.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
		String strOrderNo = eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO);

		// Fetch the search type from input document (STSOrderSearch/OOBSearch)
		String strSearchType = eleSearchIndoc.getAttribute(AcademyConstants.STR_SEARCH_TYPE);
		
		//OMNI-72201 - START
		Element eleBillToAddress = XmlUtils.getChildElement(eleSearchIndoc,  AcademyConstants.ELE_BILL_TO_ADDRESS);
		String emailID = eleBillToAddress.getAttribute(AcademyConstants.ATTR_EMAILID);
		String shipNode = eleSearchIndoc.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
		//OMNI-72201 - END

		//OMNI-87783 START
		String strStatus = eleSearchIndoc.getAttribute(AcademyConstants.STATUS);
		
		// Check if Order number/Shipment number is used as search term
		if (!YFCCommon.isVoid(strOrderNo)) {

			log.verbose("Order number is used as search term");

			log.verbose("Invoke AcademyGetShipmentListForWSC Start: " + XMLUtil.getString(inDoc));
			eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo.trim());
			docGetShipmentListWithOrderNo = AcademyUtil.invokeService(env, "AcademyGetShipmentListForWSC", inDoc);
			log.verbose("Output of AcademyGetShipmentListForWSC with OrderNo: "
					+ XMLUtil.getString(docGetShipmentListWithOrderNo));
			//OMNI-87783 END
			
			String strShipNode = eleSearchIndoc.getAttribute(AcademyConstants.ATTR_SHIP_NODE);

			//log.verbose("Invoking getCompleteOrderDetails api to get the SO/TO shipments for Sales Order");
			//docGetCompleteOrderDetailsOut = getCompleteOrderDetails(env, strOrderNo, strSearchType, inDoc);
			
			//OMNI-87783 START
			// fetch Pagination element from AcademyGetShipmentListForWSC
			Element eleGetShipmentListwithOrderNo = docGetShipmentListWithOrderNo.getDocumentElement();
			Element elePage = (Element) XPathUtil.getNodeList(eleGetShipmentListwithOrderNo, "//Shipments/PageData")
					.item(0);
			
			
			log.verbose("Invoke getShipmentListForOrder Start");	
			docGetShipmentList=getShipmentListForOrder(env, strOrderNo, strSearchType, inDoc);
			log.verbose("Invoke getShipmentListForOrder END "+ XMLUtil.getString(docGetShipmentList));

			//OMNI-87783 END
			
			if(!YFCObject.isVoid(docGetShipmentList)) {
				String totNoRecords = XMLUtil.getAttributeFromXPath(docGetShipmentList,
						"//Shipments/@TotalNumberOfRecords");
				
				if(!YFCObject.isVoid(totNoRecords) && totNoRecords.equalsIgnoreCase("0")) {
					return XmlUtils.createDocument(AcademyConstants.ELE_SHIPMENTS);
				}
			}
			// Invoke getSOShipmentListFORSOTO to get the corresponding containers for the Shipments
			
			log.verbose("getSOShipmentListFORSOTO Start");
			docFinalShipmentListOut = getSOShipmentListFORSOTO(env, docGetShipmentList, strSearchType, strShipNode);
			log.verbose("getSOShipmentListFORSOTO End: " + XMLUtil.getString(docFinalShipmentListOut));
			
			//OMNI-72201 - START
			if(!YFCObject.isVoid(strShowRelatedOrder) && AcademyConstants.STR_YES.equalsIgnoreCase(strShowRelatedOrder)) {
				//OMNI-74400 - check if one of the Shipments is in RFCP status
				Element eleRFCPShipment = (Element) XPathUtil.getNode(docFinalShipmentListOut, 
						AcademyConstants.XPATH_ATTR_SHIPMENT_RFCP_STATUS);
				if (YFCCommon.isVoid(eleRFCPShipment)) {
					log.verbose("Provided Order is not Ready for Customer Pickup..");
					log.verbose("No Orders Found");
					return XmlUtils.createDocument(AcademyConstants.ELE_SHIPMENTS);
				}
				
				//If the RFCP Shipment date is before currentDate-45, show the RFCP Shipments from the order list
				String strCreatets = eleRFCPShipment.getAttribute("Createts");
				DateFormat df = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
				Date createts = df.parse(strCreatets);
				
				String strDefaultCreatets = getDefaultCreatetsDate();
				Date defaultCreatets = df.parse(strDefaultCreatets);
				
				Document docRFCPShipments = XmlUtils.createDocument(AcademyConstants.ELE_SHIPMENTS);
				Element eleRFCPShipments = docRFCPShipments.getDocumentElement();
				log.verbose("RFCP Shipment Createts = " + strCreatets + " CurrentDate-45 = " + strDefaultCreatets);
				if (createts.before(defaultCreatets)) {
					log.verbose("RFCP Shipment createts is 45 days before current date !");
					NodeList nlShipments = docFinalShipmentListOut.getDocumentElement().getChildNodes();
					for (int i=0; i< nlShipments.getLength(); i++) {
						Element eleShipment = (Element) nlShipments.item(i);
						Element eleStatus = SCXmlUtil.getChildElement(eleShipment, "Status");
						strStatus = eleStatus.getAttribute("Status");
						log.verbose("Shipment Status - " + strStatus);
						if (!YFCCommon.isVoid(strStatus) && AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS.equals(strStatus)) {
							Element eleNewShipment = (Element) docRFCPShipments.importNode(eleShipment, true);
							eleRFCPShipments.appendChild(eleNewShipment);
						}
					}
					Element eleNewElement = (Element) docRFCPShipments.importNode(elePage, true);
					eleRFCPShipments.appendChild(eleNewElement);
					
					return docRFCPShipments;
				}
				
				
				String actualEmailID = XMLUtil.getAttributeFromXPath(docFinalShipmentListOut,
						AcademyConstants.XPATH_SHIPMENT_CUST_EMAILID);
				if(!YFCCommon.isVoid(strOrderNo) && YFCCommon.isVoid(emailID)) {
					log.verbose("Only the OrderNo is provided: " + strOrderNo);
					return verifyAndGetRFCPOrderList(env, shipNode, actualEmailID, elePaginate);
				}
				if (!YFCCommon.isVoid(strOrderNo) && !YFCCommon.isVoid(emailID)) {
					log.verbose("Both the OrderNo and EMailId were provided::");
					log.verbose("OrderNo: " + strOrderNo + " EmailID:: " + emailID);
					if (actualEmailID.equalsIgnoreCase(emailID)) {
						log.verbose("Provided EMailID is related to the OrderNo..");
						return verifyAndGetRFCPOrderList(env, shipNode, actualEmailID,elePaginate);
					} else {
						log.verbose("Provided EMailID is not related to the OrderNo..");
						log.verbose("No Orders Found");
						return XmlUtils.createDocument(AcademyConstants.ELE_SHIPMENTS);
					}					
				}
			}			
			//OMNI-72201 - END
			//OMNI-102014 Start
			
			docFinalShipmentListOut = removeShipNodeElement(docFinalShipmentListOut);
			
			//OMNI-87783 START
			
			Element eleNewElement = (Element) docFinalShipmentListOut.importNode(elePage, true);
			docFinalShipmentListOut.getDocumentElement().appendChild(eleNewElement);
			log.verbose("docFinalShipmentListOut:::: " + XMLUtil.getString(docFinalShipmentListOut));
			
			//OMNI-87783 END
			//OMNI-102104 End
			return docFinalShipmentListOut;
			
		} else {
			// Customer details are used for search
			//OMNI-72201 START
			if(!YFCObject.isVoid(strShowRelatedOrder) && AcademyConstants.STR_YES.equalsIgnoreCase(strShowRelatedOrder) 
					&& !YFCCommon.isVoid(emailID)) {
				log.verbose("Getting RFCP Orders as per the given customer emailid ");
				return verifyAndGetRFCPOrderList(env, shipNode, emailID, elePaginate);
			}
			//OMNI-72201 END
			if (AcademyConstants.STR_STS_SEARCH_TYPE.equals(strSearchType)) {

				eleSearchIndoc.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.STR_SHIP_TO_STORE);
				strStatus = eleSearchIndoc.getAttribute(AcademyConstants.STATUS);

				if ((!YFCCommon.isVoid(strStatus)) && (strStatus.equals(AcademyConstants.STR_SHP_1400))) {
					eleSearchIndoc.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.DOCUMENT_TYPE);
					eleSearchIndoc.setAttribute(AcademyConstants.STATUS, AcademyConstants.VAL_SHIPPED_STATUS);
					eleSearchIndoc.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, AcademyConstants.STR_SHIP_DELIVERY_METHOD);
				} else if ((!YFCCommon.isVoid(strStatus)) && (strStatus.equals(AcademyConstants.STR_PICK_1400))) {
					eleSearchIndoc.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
					eleSearchIndoc.setAttribute(AcademyConstants.STATUS, "");
					eleSearchIndoc.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, AcademyConstants.STR_PICK);
					// complex query to consider both 1400 and 1600.002 when user selects "Picked up
					// by Customer option in the search"
					Element eleComplexQuery = inDoc.createElement(AcademyConstants.COMPLEX_QRY_ELEMENT);
					Element eleOr = inDoc.createElement(AcademyConstants.COMPLEX_OR_ELEMENT);
					Element eleExp1 = inDoc.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
					Element eleExp2 = inDoc.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
					eleExp1.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.STATUS);
					eleExp1.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.VAL_SHIPPED_STATUS);
					eleExp2.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.STATUS);
					eleExp2.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.VAL_SHIPMENT_INVOICED_STATUS);
					eleOr.appendChild(eleExp1);
					eleOr.appendChild(eleExp2);
					eleComplexQuery.appendChild(eleOr);
					eleSearchIndoc.appendChild(eleComplexQuery);

				} else if ((!YFCCommon.isVoid(strStatus))
						&& (strStatus.equals(AcademyConstants.VAL_RECIEVING_IN_PROGRESS_STATUS) || strStatus.equals(AcademyConstants.VAL_READY_TO_SHIP_STATUS))) {
					eleSearchIndoc.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.DOCUMENT_TYPE);
					eleSearchIndoc.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, AcademyConstants.STR_SHIP_DELIVERY_METHOD);
				}
			}
			
			String strCreatets = getDefaultCreatetsDate();
			eleSearchIndoc.setAttribute(AcademyConstants.ATTR_CREATETS, strCreatets);
			eleSearchIndoc.setAttribute(AcademyConstants.ATTR_CREATETS_QRY_TYPE, AcademyConstants.GT_QRY_TYPE);
			
			//OMNI-88764 - Start
			String strIsCurbsidePickupOrder = eleSearchIndoc.getAttribute(AcademyConstants.ATTR_IS_CURB_PICKUP_ORDER);
			//OMNI-89343 - added CurbsideConsolidationToggle condition check
			String strCurbsideConsolidationToggle = eleSearchIndoc.getAttribute(AcademyConstants.ATTR_CURB_SIDE_CONSOLIDATION_TOGGLE);
			String strIsInstorePickupOrder = eleSearchIndoc.getAttribute("IsInStorePickupOrder");//OMNI-105498
			if ((!YFCObject.isVoid(strIsCurbsidePickupOrder)
					&& AcademyConstants.STR_YES.equalsIgnoreCase(strIsCurbsidePickupOrder)
					&& (!YFCObject.isVoid(strCurbsideConsolidationToggle))
					&& AcademyConstants.STR_YES.equalsIgnoreCase(strCurbsideConsolidationToggle)) ||
					AcademyConstants.STR_YES.equalsIgnoreCase(strIsInstorePickupOrder)){
				eleSearchIndoc.setAttribute(AcademyConstants.STATUS, "");
				//Complex query to consider both RFCP and Paper Work Status when user selects "Waiting for CurbsidePickup" Page
				Element eleComplexQuery = inDoc.createElement(AcademyConstants.COMPLEX_QRY_ELEMENT);
				Element eleOr = inDoc.createElement(AcademyConstants.COMPLEX_OR_ELEMENT);
				Element eleExp1 = inDoc.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
				Element eleExp2 = inDoc.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
				eleExp1.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.STATUS);
				eleExp1.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS);
				eleExp2.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.STATUS);
				eleExp2.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STR_PAPER_WORK_INITIATED_STATUS);
				eleOr.appendChild(eleExp1);
				eleOr.appendChild(eleExp2);
				eleComplexQuery.appendChild(eleOr);
				eleSearchIndoc.appendChild(eleComplexQuery);
			}
			
			//OMNI-101645 Start
			Element eleShipNode = XmlUtils.getChildElement(eleSearchIndoc, AcademyConstants.ELE_SHIP_NODE);
			String strNodeType="";
			if (!YFCCommon.isVoid(eleShipNode)) {
				strNodeType = eleShipNode.getAttribute(AcademyConstants.ATTR_NODE_TYPE);
			}
			String strDocumentType = eleSearchIndoc.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
			log.verbose("strDocumentType:: " + strDocumentType);
			if (!YFCCommon.isVoid(strNodeType) && AcademyConstants.STR_STORE.equals(strNodeType) && AcademyConstants.DOCUMENT_TYPE.equals(strDocumentType)) {
				log.verbose("strNodeType:: " + strNodeType);
				eleSearchIndoc.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, "");
			}
			//OMNI-101645 End
			//OMNI-88764 - End
			
			log.verbose("Input to AcademyGetShipmentListForWSC Service :: "+ XMLUtil.getString(inDoc));
			docGetShipmentListOut = AcademyUtil.invokeService(env, AcademyConstants.SERV_ACADEMY_GET_SHIPMENT_LIST_FOR_WSC, inDoc);
			log.verbose("Output of AcademyGetShipmentListForWSC Service :: " + XMLUtil.getString(docGetShipmentListOut));
			
			docFinalShipmentListOut = getSOShipmentListFORSOTO(env, docGetShipmentListOut, strSearchType, "");
			docFinalShipmentListOut = removeShipNodeElement(docFinalShipmentListOut);
			log.verbose("Output of AcademySearchShipmenNoOrderNoFromWSC.searchShipmentNoOrderNo() :: "
					+ XMLUtil.getString(docFinalShipmentListOut));
			log.endTimer(this.getClass() + ".searchShipmentNoOrderNo");
			
			return docFinalShipmentListOut;
		}

	}
	
	private Document getShipmentListForOrder(YFSEnvironment env, String strOrderNo, String strSearchType, Document inDoc) throws Exception {
		
		// Invoking getShipmentListForOrder api to get the SO/TO shipments for Sales
		// Order"
		/*
		 * getShipmentListForOrder Input, Template same as AcademyGetShipmentListForWSC
		 * <Order OrderNo="230320230003" DocumentType="0001"
		 * EnterpriseCode="Academy_Direct"/>
		 */
		log.verbose("Entering AcademySearchShipmenNoOrderNoFromWSC.getShipmentListForOrder()" );
		
		Document docGetShipmentList = null;
	try
	{
		 Document inDocGetShipmentListForOrderWSC = XmlUtils.createDocument(AcademyConstants.ELE_ORDER);
		Element eleinDocGetShipmentListForOrderWSC = inDocGetShipmentListForOrderWSC.getDocumentElement();
		eleinDocGetShipmentListForOrderWSC.setAttribute("OrderNo", strOrderNo);
		eleinDocGetShipmentListForOrderWSC.setAttribute("EnterpriseCode", "Academy_Direct");

		log.verbose("Invoke getShipmentListForOrder Start: " + XMLUtil.getString(inDocGetShipmentListForOrderWSC));
		Document docGetShipmentListForOrderOp = AcademyUtil.invokeService(env, "AcademyGetShipmentListForOrderWSC",
				inDocGetShipmentListForOrderWSC);
		log.verbose("Output of getShipmentListForOrder END: " + XMLUtil.getString(docGetShipmentListForOrderOp));

		Element eleGetShipmentListForOrderOp = docGetShipmentListForOrderOp.getDocumentElement();

		docGetShipmentList = XmlUtils.createDocument(AcademyConstants.ELE_SHIPMENTS);
		Element eleGetShipmentListOut = docGetShipmentList.getDocumentElement();
		eleGetShipmentListOut.setAttribute("TotalNumberOfRecords", eleGetShipmentListForOrderOp.getAttribute("TotalNumberOfRecords"));
		
		// To add all the shipment to a new document - this is done to remove the Order
		// root element from getCompleteOrderDetails output
		/* OMNI-90272 Start */

					NodeList nlShipmentsNonCancelled = XPathUtil.getNodeList(eleGetShipmentListForOrderOp,"//ShipmentList/Shipment[@Status!='9000']");

					NodeList nlShipmentsCancelled = XPathUtil.getNodeList(eleGetShipmentListForOrderOp,
"//ShipmentList/Shipment[@Status='9000']");
					for (int i = 0; i < nlShipmentsNonCancelled.getLength(); i++) {
						Element eleShipment = (Element) nlShipmentsNonCancelled.item(i);
						Element eleNewShipment = (Element) docGetShipmentList.importNode(eleShipment, true);
						eleGetShipmentListOut.appendChild(eleNewShipment);
					}
					for (int i = 0; i < nlShipmentsCancelled.getLength(); i++) {
						Element eleShipment = (Element) nlShipmentsCancelled.item(i);
						Element eleNewShipment = (Element) docGetShipmentList.importNode(eleShipment, true);
						eleGetShipmentListOut.appendChild(eleNewShipment);
					}
					/* OMNI-90272 End */
					
	}	
	catch (Exception e) {

		// If there are no orders and strSearchType is not STSOrderSearch, then use the
		// shipment number as the search term to getShipmentList
		if (!AcademyConstants.STR_STS_SEARCH_TYPE.equals(strSearchType)) {
			inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strOrderNo);
			
			// Remove Order elements - Required in case the search term passed is Shipment Number
				Element eleOrder = (Element) inDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
				eleOrder.getParentNode().removeChild(eleOrder);
				
			docGetShipmentList = AcademyUtil.invokeService(env,
					AcademyConstants.SERV_ACADEMY_GET_SHIPMENT_LIST_FOR_WSC, inDoc);
		}
	}
		log.verbose("AcademySearchShipmenNoOrderNoFromWSC.getCompleteOrderDetails() - Shipment List :: " + XMLUtil.getString(docGetShipmentList));
		return docGetShipmentList;
	}
	
	
	/**
	 * This method will fetch the NodeType attribute and remove the ship node element from final output - OMNI-102104
	 */
	private Document removeShipNodeElement(Document docFinalShipmentListOut) throws Exception {
		log.verbose("removeShipNodeElement:: "+ XMLUtil.getString(docFinalShipmentListOut));
		NodeList nlShipments = docFinalShipmentListOut.getDocumentElement().getElementsByTagName
				(AcademyConstants.ELE_SHIPMENT);
		for (int i = 0; i < nlShipments.getLength(); i++) {
			Element eleShipment = (Element) nlShipments.item(i);
			String strNodeType = "";
			Element eleShipNode = XmlUtils.getChildElement(eleShipment, AcademyConstants.ELE_SHIP_NODE);
			// procureFromNode
			
			if (!YFCCommon.isVoid(eleShipNode)) {
				strNodeType = eleShipNode.getAttribute(AcademyConstants.ATTR_NODE_TYPE);
				
				String sProcureFromNode = XPathUtil.getString(eleShipment, "ShipmentLines/ShipmentLine/OrderLine/@ProcureFromNode");
				log.verbose("strNodeType::" + strNodeType);
				if (AcademyConstants.ATTR_VAL_SHAREDINV_DC.equalsIgnoreCase(strNodeType) || (!YFCCommon.isVoid(sProcureFromNode))){
					eleShipment.setAttribute("ShipmentNodeType", AcademyConstants.STR_DC);
				}
				else
				{
					eleShipment.setAttribute("ShipmentNodeType", strNodeType);
				}
				
				log.verbose("ShipmentNodeType::" + strNodeType);
				eleShipNode.getParentNode().removeChild(eleShipNode);
			}
		}
		return docFinalShipmentListOut;
	}
	
	/*
	 * This method will fetch the list of Delivered / Intransit Shipments to be displayed on TC70 device based on the Flag sent in the request
	 */
	private Document getDeliveredInTransitSTSShipListForWSC(YFSEnvironment env, String sContainerListFlag,
			String sShipNode, String sFlag, Element elePaginate) throws IllegalArgumentException, Exception {
	
		log.verbose("getDeliveredInTransitSTSShipListForWSC START");
	
		Document getShipListRequest = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleShipListRequest = getShipListRequest.getDocumentElement();
		Element eleContainers = getShipListRequest.createElement("Containers");
		Element eleContainer = getShipListRequest.createElement(AcademyConstants.ELE_CONTAINER);
		
		eleContainer.setAttribute(AcademyConstants.ATTR_CREATETS,
				getFromDate(AcademyConstants.STR_SIMPLE_DATE_PATTERN));
		eleContainer.setAttribute(AcademyConstants.ATTR_CREATETS_QRY_TYPE, AcademyConstants.GT_QRY_TYPE);
		eleContainer.setAttribute(AcademyConstants.ATTR_IS_RECEIVED, AcademyConstants.STR_NO);

		Element eleExtn = getShipListRequest.createElement(AcademyConstants.ATTR_EXTN);
		eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_IS_SO_CANCELLED, AcademyConstants.STR_YES);
		eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_IS_SO_CANCELLED_QRY_TYPE, AcademyConstants.STR_NE);
		eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_TRACKING_STATUS, AcademyConstants.STR_DELIVERED);
		eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_TRACKING_STATUS_QRY_TYPE, sContainerListFlag);
	
		Element eleShipment = getShipListRequest.createElement(AcademyConstants.ELE_SHIPMENT);
		eleShipment.setAttribute(AcademyConstants.Att_DeliveryMethod, AcademyConstants.STR_SHIP_DELIVERY_METHOD);
		eleShipment.setAttribute(AcademyConstants.STR_DOCUMENT_TYPE, AcademyConstants.DOCUMENT_TYPE);
		eleShipment.setAttribute(AcademyConstants.ATTR_RECV_NODE, sShipNode);
		eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.STR_SHIP_TO_STORE);
		eleShipment.setAttribute(AcademyConstants.STR_PACK_LIST_TYPE, AcademyConstants.STS_FA);
		eleShipment.setAttribute(AcademyConstants.ATTR_PACKLISTTYPE_QRY_TYPE, AcademyConstants.STR_NE);

		Element eleComplexQuery = getShipListRequest.createElement(AcademyConstants.COMPLEX_QRY_ELEMENT);
		eleComplexQuery.setAttribute(AcademyConstants.COMPLEX_OPERATOR_ATTR, AcademyConstants.COMPLEX_OPERATOR_AND_VAL);

		Element eleOr = getShipListRequest.createElement(AcademyConstants.COMPLEX_OR_ELEMENT);
		Element eleExp = getShipListRequest.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_STATUS);
		eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
		eleExp.setAttribute(AcademyConstants.ATTR_VALUE, "1400");

		Element eleExp2 = getShipListRequest.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp2.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_STATUS);
		eleExp2.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
		eleExp2.setAttribute(AcademyConstants.ATTR_VALUE, "1600");

		eleOr.appendChild(eleExp);
		eleOr.appendChild(eleExp2);
		eleComplexQuery.appendChild(eleOr);

		eleShipment.appendChild(eleComplexQuery);
		eleContainer.appendChild(eleShipment);
		eleContainer.appendChild(eleExtn);
		
		Element eleOrderBy = getShipListRequest.createElement(AcademyConstants.ELE_ORDERBY);
		Element eleAttribute = getShipListRequest.createElement(AcademyConstants.ELE_ATTRIBUTE);
		eleAttribute.setAttribute(AcademyConstants.ATTR_DESC_SHORT, AcademyConstants.STR_YES);
		eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_CREATETS);

		eleOrderBy.appendChild(eleAttribute);
		eleContainer.appendChild(eleOrderBy);
		
		Element eleInPaginate = (Element) getShipListRequest.importNode(elePaginate, true);
		eleShipListRequest.appendChild(eleInPaginate);
		eleContainers.appendChild(eleContainer);
		eleShipListRequest.appendChild(eleContainers);
		
		log.verbose("getShipContainerList Complex Query Input:: " + XMLUtil.getString(getShipListRequest));

		Document getShipListResponse = AcademyUtil.invokeService(env, "AcademyGetShipmentListWithContainersForWSC", getShipListRequest);

		log.verbose("getShipContainerList Complex Query Output:: " + XMLUtil.getString(getShipListResponse));

		// OMNI-87783 START

		Document docFinalShipmentListOut = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENTS);
		Node shipmentsNode = (Node) docFinalShipmentListOut.getDocumentElement();
		
		Element elePage = (Element) XPathUtil.getNodeList(getShipListResponse.getDocumentElement(), "//Shipments/PageData").item(0);
		Element eleOutPage = (Element) docFinalShipmentListOut.importNode(elePage, true);
		docFinalShipmentListOut.getDocumentElement().appendChild(eleOutPage);
		
		NodeList containerNL = com.academy.util.xml.XMLUtil.getNodeListByXPath(getShipListResponse,
				"/Shipments/Shipment/Containers/Container");
				
				
		for (int i = 0; i < containerNL.getLength(); i++) {
			Element containerEle = (Element) containerNL.item(i);
			Document docContainerEle= com.academy.util.xml.XMLUtil.getDocumentForElement(containerEle);

			String sExtnTrackingStatus = com.academy.util.xml.XMLUtil.getAttributeFromXPath
					 (docContainerEle, "/Container/Extn/@ExtnTrackingStatus");
			log.verbose("ExtnTrackingStatus:::: "+sExtnTrackingStatus);
			
			 String sIsReceived = com.academy.util.xml.XMLUtil.getAttributeFromXPath
					 (com.academy.util.xml.XMLUtil.getDocumentForElement(containerEle), "/Container/@IsReceived");
			 log.verbose("IsReceived:::: "+sIsReceived);

 			if (!YFCObject.isVoid(sExtnTrackingStatus) && sExtnTrackingStatus.equalsIgnoreCase(AcademyConstants.STR_DELIVERED)
						&& sIsReceived.equalsIgnoreCase("N") && sFlag.equalsIgnoreCase(AcademyConstants.ATTR_DLVR_CONTAINER_LIST_FLAG)){
				 
				Node shipmentNode = containerEle.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
				
				((Element)shipmentNode).setAttribute(sFlag, "Y");
				shipmentsNode.appendChild(docFinalShipmentListOut.importNode(shipmentNode, true));
				 
			}
			 else if (!sExtnTrackingStatus.equalsIgnoreCase(AcademyConstants.STR_DELIVERED)
						&& sIsReceived.equalsIgnoreCase("N") && sFlag.equalsIgnoreCase(AcademyConstants.ATTR_INTRANSIT_CONTAINER_LIST_FLAG)) 
			 {
				Node shipmentNode = containerEle.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
					((Element)shipmentNode).setAttribute(sFlag, "Y");
					shipmentsNode.appendChild(docFinalShipmentListOut.importNode(shipmentNode, true));
			 }
			 
		}

		log.verbose("getDeliveredInTransitSTSShipListForWSC END: " + XMLUtil.getString(docFinalShipmentListOut));

		return docFinalShipmentListOut;
	}

	/**
	 * This method will fetch the date based on the format and min days to consider
	 * 
	 * @param env,
	 * @param strShipNode
	 * @return String ContainerNO
	 */
	private String getFromDate(String strDateTypeFormat) throws Exception {
		String strFromDate = null;
		// Default number of days is set as 45
		int iNoOfDays = 45;

		String strMinNofOfDays = YFSSystem.getProperty(AcademyConstants.PROP_STS_MOBILE_DAYS_TO_CONSIDER);
		if (!YFCObject.isVoid(strMinNofOfDays)) {
			log.verbose("Overriding The no of days to consider with :: " + strMinNofOfDays);
			iNoOfDays = Integer.parseInt(strMinNofOfDays);
		}
		SimpleDateFormat sdf = new SimpleDateFormat(strDateTypeFormat);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -iNoOfDays);
		strFromDate = sdf.format(cal.getTime());

		log.verbose("Update From Date : " + strFromDate);

		return strFromDate;
	}
	
	/**
	 * This method is used to invoke getCompleteOrderDetails API to get all the SO and
	 * TO shipments for the order. If the order is invalid, then use the order number
	 * as shipment number and invoke getShipmentList if search is not STSOrderSearch
	 */

	public Document getCompleteOrderDetails(YFSEnvironment env, String strOrderNo, String strSearchType, Document inDoc)
			throws Exception {

		log.beginTimer(this.getClass() + ".getCompleteOrderDetails");
		log.verbose("Entering AcademySearchShipmenNoOrderNoFromWSC.getCompleteOrderDetails()");

		Document docGetCompleteOrderDetailsOut = null;
		Document docGetShipmentList = null;
		try {

			docGetShipmentList = XmlUtils.createDocument(AcademyConstants.ELE_SHIPMENTS);

			Element eleGetShipmentListOut = docGetShipmentList.getDocumentElement();

			Document docGetCompleteOrderDetailsIn = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			Element eleGetCompleteOrderDetailsIn = docGetCompleteOrderDetailsIn.getDocumentElement();
			eleGetCompleteOrderDetailsIn.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
			eleGetCompleteOrderDetailsIn.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
			log.verbose("API Input to getCompleteOrderDetails :: " + XMLUtil.getString(docGetCompleteOrderDetailsIn));
			docGetCompleteOrderDetailsOut = AcademyUtil.invokeService(env, AcademyConstants.SERV_ACADEMY_GET_COMPLETE_ORDER_DETAILS,
					docGetCompleteOrderDetailsIn);

			log.verbose(
					"Output from AcademySearchShipmenNoOrderNoFromWSC.getCompleteOrderDetails() - getCompleteOrderDetails :: "
							+ XMLUtil.getString(docGetCompleteOrderDetailsOut));

			// To add all the shipment to a new document - this is done to remove the Order
			// root element from getCompleteOrderDetails output
			/* OMNI-90272 Start */
		
			NodeList nlShipments = XPathUtil.getNodeList(docGetCompleteOrderDetailsOut,
					AcademyConstants.XPATH_NON_CANCELLED_SHPMNTS);

			NodeList nlShipmentsCancelled = XPathUtil.getNodeList(docGetCompleteOrderDetailsOut,
					AcademyConstants.XPATH_CANCELLED_SHPMNTS);
			for (int i = 0; i < nlShipments.getLength(); i++) {
				Element eleShipment = (Element) nlShipments.item(i);
				Element eleNewShipment = (Element) docGetShipmentList.importNode(eleShipment, true);
				eleGetShipmentListOut.appendChild(eleNewShipment);
			}
			for (int i = 0; i < nlShipmentsCancelled.getLength(); i++) {
				Element eleShipment = (Element) nlShipmentsCancelled.item(i);
				Element eleNewShipment = (Element) docGetShipmentList.importNode(eleShipment, true);
				eleGetShipmentListOut.appendChild(eleNewShipment);
			}
			/* OMNI-90272 End */
		} catch (Exception e) {

			// If there are no orders and strSearchType is not STSOrderSearch, then use the
			// shipment number as the search term to getShipmentList
			if (!AcademyConstants.STR_STS_SEARCH_TYPE.equals(strSearchType)) {
				inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strOrderNo);
				docGetShipmentList = AcademyUtil.invokeService(env,
						AcademyConstants.SERV_ACADEMY_GET_SHIPMENT_LIST_FOR_WSC, inDoc);
			}
		}

		log.verbose("AcademySearchShipmenNoOrderNoFromWSC.getCompleteOrderDetails() - Shipment List :: "
				+ XMLUtil.getString(docGetShipmentList));
		log.endTimer(this.getClass() + ".getCompleteOrderDetails");

		return docGetShipmentList;
	}

	/**
	 * This method is used to call the getShipmentContainerList with
	 * ExtnSOShipmentNo or TO OrderHeaderKey
	 */

	public Document getShipmentListFromContainerNo(YFSEnvironment env, Document inDoc) throws Exception {

		log.beginTimer(this.getClass() + ".getShipmentListFromContainerNo");
		log.verbose("AcademySearchShipmenNoOrderNoFromWSC.getShipmentListFromContainerNo() - Begin");

		Document docGetShipmentListOut = null;
		Document docGetContainersOut = null;
		String strExtnSOShipmentNo = null;
		String strTOOrderHeaderKey = null;

		// Invoke getShipmentList to get shipment with container details

		docGetShipmentListOut = AcademyUtil.invokeService(env, AcademyConstants.SERV_ACADEMY_GET_SHIPMENT_LIST_FOR_WSC, inDoc);

		log.verbose("AcademySearchShipmenNoOrderNoFromWSC.getShipmentListFromContainerNo() :: Shipment List "
				+ XMLUtil.getString(docGetShipmentListOut));

		Element eleShipment = (Element) XPathUtil.getNodeList(docGetShipmentListOut, AcademyConstants.XPATH_SHIPMENT).item(0);
		
		if(YFCCommon.isVoid(eleShipment)) {
			docGetContainersOut = XmlUtils.createDocument(AcademyConstants.ELE_SHIPMENTS);
			log.verbose("No such containers exist");
			return docGetContainersOut;

		}

		Element eleExtn = (Element) XPathUtil
				.getNodeList(docGetShipmentListOut, AcademyConstants.XPATH_CONTAINER_ELE).item(0);

		if (!YFCCommon.isVoid(eleExtn)) {
			strExtnSOShipmentNo = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_SO_SHIPMENT_NO);
		}

		if (!YFCCommon.isVoid(strExtnSOShipmentNo)) {
			
			log.verbose("ExtnSOShipmentNo :: " +strExtnSOShipmentNo);
			log.verbose(
					"Invoke getShipmentContainerList API with ExtnSOShipmentNo as the SO shipment is created for TO");
			docGetContainersOut = getShipmentContainerList(env, strExtnSOShipmentNo, "");
			
		} else {
		
			strTOOrderHeaderKey = eleShipment.getAttribute(AcademyConstants.STR_ORDR_HDR_KEY);
			
			if (!YFCCommon.isVoid(strTOOrderHeaderKey)) {
				log.verbose(
						"Invoke getShipmentContainerList with TO OrderHeaderKey as the SO shipment is not created");
				docGetContainersOut = getShipmentContainerList(env, "", strTOOrderHeaderKey);
			}
		}

		log.verbose("AcademySearchShipmenNoOrderNoFromWSC.getShipmentListContainerNo():: Containers "
				+ XMLUtil.getString(docGetContainersOut));

		/*
		 * START - Remove existing shipment containers from so Shipment and add the
		 * updated set of containers from the TO to SO
		 */

		Element eleContainers = XMLUtils.getElementByName(eleShipment,  AcademyConstants.ELE_CONTAINERS);
		eleShipment.removeChild(eleContainers);

		Element eleContainerList = docGetContainersOut.getDocumentElement();
		Element newTOContainers = docGetShipmentListOut.createElement( AcademyConstants.ELE_CONTAINERS);

		newTOContainers = (Element) docGetShipmentListOut.importNode(eleContainerList, true);
		eleShipment.appendChild(newTOContainers);

		/*
		 * END - Remove existing shipment containers from so Shipment and add the
		 * updated set of containers from the TO to SO
		 */

		log.verbose("Output of AcademySearchShipmenNoOrderNoFromWSC.getShipmentListFromContainerNo() : "
				+ XMLUtil.getString(docGetShipmentListOut));
		
		log.verbose("AcademySearchShipmenNoOrderNoFromWSC.getShipmentListFromContainerNo() - End");

		log.endTimer(this.getClass() + ".getShipmentListFromContainerNo");

		return docGetShipmentListOut;
	}

	/**
	 * This method is used to get the shipment container list. If SO Shipment is
	 * created, returns all the TO Containers associated with the SO. If SO Shipment
	 * is not yet created for the container, return all the containers associated to
	 * Transfer Order
	 */

	public Document getShipmentContainerList(YFSEnvironment env, String strExtnSOShipmentNo, String strTOOrderHeaderKey)
			throws Exception {

		log.beginTimer(this.getClass() + ".getShipmentContainerList");
		log.verbose("AcademySearchShipmenNoOrderNoFromWSC.getShipmentContainerList() - Begin");

		Document docGetShipmentContainerListOut = null;
		Document docGetShipmentContainerListIn = XMLUtil.createDocument( AcademyConstants.ELE_CONTAINER);
		Element eleGetShipmentContainerListIn = docGetShipmentContainerListIn.getDocumentElement();
		String status=""; //Added for OMNI-72218

		if (!YFCCommon.isVoid(strExtnSOShipmentNo)) {
			// To getContainer list if SO Shipment is created for STS order
			//Changes for OMNI-72218 --Start
			Document docGetShipmentListInput=XMLUtil.createDocument( AcademyConstants.ELE_SHIPMENT);
			docGetShipmentListInput.getDocumentElement().setAttribute("ShipmentNo", strExtnSOShipmentNo);
			log.verbose("Input xml for getShipmentList api:"+ com.academy.util.xml.XMLUtil.getXMLString(docGetShipmentListInput));
			Document docGetShipmentListOutputTemplate = XMLUtil
					.getDocument("<Shipments>\r\n" + 
							"<Shipment Status=' ' >\r\n" + 
							"</Shipment>\r\n" + 
							"</Shipments>");
			env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docGetShipmentListOutputTemplate);
			
			Document  docSOShipmentList= AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_SHIPMENT_LIST, docGetShipmentListInput);
			status=XPathUtil.getString(docSOShipmentList, "Shipments/Shipment/@Status");
			log.verbose("SO Status: " + status);
			//Changes for OMNI-72218 --End
			Element eleExtn = docGetShipmentContainerListIn.createElement(AcademyConstants.ELE_EXTN);
			eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_SO_SHIPMENT_NO, strExtnSOShipmentNo);
			eleGetShipmentContainerListIn.appendChild(eleExtn);
		} else if (!YFCCommon.isVoid(strTOOrderHeaderKey)) {
			// To getContainer list if SO Shipment is not created for STS order by using TO
			// Order Header Key
			Element eleShipment = docGetShipmentContainerListIn.createElement(AcademyConstants.ELE_SHIPMENT);
			eleShipment.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, strTOOrderHeaderKey);
			eleGetShipmentContainerListIn.appendChild(eleShipment);
		}
		log.verbose("Get Shipment Container List Input: " + XMLUtil.getString(docGetShipmentContainerListIn));

		docGetShipmentContainerListOut = AcademyUtil.invokeService(env, AcademyConstants.SERV_ACADEMY_FETCH_STS_SHIPMENT_CONTAINERS_FOR_WSC,
				docGetShipmentContainerListIn);
		
		//Changes for OMNI-72218 --Start
		log.verbose("Output of AcademySearchShipmenNoOrderNoFromWSC.getShipmentContainerList() before adding status"
				+ XMLUtil.getString(docGetShipmentContainerListOut));
		Element eleGetShipmentContainerListOut=docGetShipmentContainerListOut.getDocumentElement();
		NodeList nl=eleGetShipmentContainerListOut.getElementsByTagName("Extn");
		for (int i = 0; i < nl.getLength(); i++) {
			Element eleExtnFromOutput = (Element) nl.item(i);
		eleExtnFromOutput.setAttribute("SOShipmentStatus",status);
		eleGetShipmentContainerListOut.appendChild(eleExtnFromOutput);
		}
		//Changes for OMNI-72218 --End
		log.verbose("Output of AcademySearchShipmenNoOrderNoFromWSC.getShipmentContainerList()"
				+ XMLUtil.getString(docGetShipmentContainerListOut));
		log.verbose("AcademySearchShipmenNoOrderNoFromWSC.getShipmentContainerList() - End");

		log.endTimer(this.getClass() + ".getShipmentContainerList");

		return docGetShipmentContainerListOut;

	}
	
	/*
	 * This method is used to modify the o/p of Shipment list based on Search type
	 * If search type - OOB, then displays all shipments
	 * If search type - STS, then displays only STS shipments that are greater than/equal to  "Ready To Ship" status
	 * Modifies o/p to stamp fulfillment type as "BOPIS" if shipment type is not AcademyConstants.STR_SHIP_TO_STORE and ExtOriginalFulfillmentType = AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE 
	 */

	public Document getSOShipmentListFORSOTO(YFSEnvironment env, Document docShipmentList, String strSearchType, String strShipNode)
			throws Exception {

		log.beginTimer(this.getClass() + ".getSOShipmentListFORSOTO");
		log.verbose("AcademySearchShipmenNoOrderNoFromWSC.getSOShipmentListFORSOTO() - Begin");

		// Remove the Shipments from getCompleteOrderDetails o/p if shipmentType!="STS" for STS Search

		NodeList nlShipments = XPathUtil.getNodeList(docShipmentList, AcademyConstants.XPATH_NON_STS_SHIPMENTS);

		if (AcademyConstants.STR_STS_SEARCH_TYPE.equals(strSearchType)) {
			for (int i = 0; i < nlShipments.getLength(); i++) {
				Element eleShipment = (Element) nlShipments.item(i);
				//OMNI-105846 Start
				String strDocumentType = eleShipment.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
				String strDeliveryMethod = eleShipment.getAttribute(AcademyConstants.ATTR_DEL_METHOD);
				String sFulfillmentType = XPathUtil.getString(eleShipment, "ShipmentLines/ShipmentLine/OrderLine/@FulfillmentType");
				log.verbose("sFulfillmentType::"+sFulfillmentType);
				if ((AcademyConstants.SALES_DOCUMENT_TYPE.equals(strDocumentType) 
						&& AcademyConstants.STR_SHIP_DELIVERY_METHOD.equals(strDeliveryMethod)) || AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE.equals(sFulfillmentType)) {//OMNI-105846 End
				log.verbose("Inside IF::::");
				eleShipment.getParentNode().removeChild(eleShipment);
				}
			}
		}
		
		if (!YFCCommon.isVoid(strShipNode)) {
			// Remove the SO Shipments from getCompleteOrderDetails o/p if ship node!=
			// selected store

			NodeList nlShipNodeSOList = XPathUtil.getNodeList(docShipmentList,
					AcademyConstants.XPATH_SHIPNODE_SO_SHIPMENTS + strShipNode + AcademyConstants.CLOSING_BACKET);

			for (int i = 0; i < nlShipNodeSOList.getLength(); i++) {
				Element eleShipment = (Element) nlShipNodeSOList.item(i);
				eleShipment.getParentNode().removeChild(eleShipment);
			}

			// Remove the TO Shipments from getCompleteOrderDetails o/p if receiving node!=
			// selected store

			NodeList nlShipNodeTOList = XPathUtil.getNodeList(docShipmentList,
					AcademyConstants.XPATH_SHIPNODE_STS_SHIPMENTS);

			for (int i = 0; i < nlShipNodeTOList.getLength(); i++) {
				Element eleShipment = (Element) nlShipNodeTOList.item(i);
				String strShipmentType = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
				
				if (AcademyConstants.STR_SHIP_TO_STORE.equals(strShipmentType)) {
					if(!eleShipment.getAttribute(AcademyConstants.ATTR_RECV_NODE).equals(strShipNode)) {
						eleShipment.getParentNode().removeChild(eleShipment);
					}
				}					
				else {
					//OMNI-48475 STS2.0 Enable search
					if(!eleShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE).equals(strShipNode)) {
						eleShipment.getParentNode().removeChild(eleShipment);	
					}					
				}
			}
		}
		

		/*
		 * OMNI-34406 - Start : Display BOPIS turned Save The Sale SO Shipment as BOPIS
		 */

		if (!AcademyConstants.STR_STS_SEARCH_TYPE.equals(strSearchType)) {
			NodeList nlNonSTSShipment = XPathUtil.getNodeList(docShipmentList,
					AcademyConstants.XPATH_SAVE_THE_SALE_BOPIS_LINE);

			for (int i = 0; i < nlNonSTSShipment.getLength(); i++) {
				Element eleNonSTSShipment = (Element) nlNonSTSShipment.item(i);
				eleNonSTSShipment.setAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE, AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE);
				log.verbose(
						"Updated BOPIS turned Save The Sale line FulfillmentType to BOPIS to process with BOPIS Packing");
			}
		}

		/*
		 * OMNI-34406 - End : Display BOPIS turned Save The Sale SO Shipment as BOPIS
		 */

		/*
		 * START - Get the list of STS SO Shipments and get the corresponding TO
		 * Containers and add it to SO Shipment
		 */

		NodeList nlSOSTSShipments = XPathUtil.getNodeList(docShipmentList,
				AcademyConstants.XPATH_SO_STS_SHIPMENT);

		log.verbose("Number of Sales Order STS Shipments " + nlSOSTSShipments.getLength());

		Set<String> sShipmentsToRemove = new HashSet<String>();
		Element newShipmentContainers = null;

		for (int i = 0; i < nlSOSTSShipments.getLength(); i++) {

			Element eleSOSTSShipment = (Element) nlSOSTSShipments.item(i);
			String strSOShipmentNo = eleSOSTSShipment.getAttribute("ShipmentNo");

			NodeList nlTOShipment = XPathUtil.getNodeList(docShipmentList,
					AcademyConstants.XPATH_TO_SALES_SHIPMENT + strSOShipmentNo + AcademyConstants.CLOSING_BACKET);

			Integer iTotalNumberOfRecords = 0;

			if (nlTOShipment.getLength() > 0) {

				log.verbose("SO Shipment " + strSOShipmentNo + " has " + nlTOShipment.getLength() + " TO Shipments");
				Element eleSOContainer = XmlUtils.getChildElement(eleSOSTSShipment,  AcademyConstants.ELE_CONTAINERS);
				eleSOSTSShipment.removeChild(eleSOContainer);
				newShipmentContainers = docShipmentList.createElement( AcademyConstants.ELE_CONTAINERS);
			}

			for (int j = 0; j < nlTOShipment.getLength(); j++) {

				Element eleTOShipment = (Element) nlTOShipment.item(j);
				Element eleContainers = XMLUtils.getElementByName(eleTOShipment,  AcademyConstants.ELE_CONTAINERS);
				NodeList nlContainers = eleContainers.getElementsByTagName( AcademyConstants.ELE_CONTAINER);
				for (int l = 0; l < nlContainers.getLength(); l++) {
					Element eleTOShipmentContainer = (Element) nlContainers.item(l);
					Element eleNewContainer = docShipmentList.createElement( AcademyConstants.ELE_CONTAINER);
					eleNewContainer = (Element) docShipmentList.importNode(eleTOShipmentContainer, true);
					newShipmentContainers.appendChild(eleNewContainer);
					iTotalNumberOfRecords++;
					String strShipmentKey = eleTOShipmentContainer.getAttribute(AcademyConstants.SHIPMENT_KEY);
					sShipmentsToRemove.add(strShipmentKey);

				}
			}

			if (nlTOShipment.getLength() > 0) {

				newShipmentContainers.setAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS, iTotalNumberOfRecords.toString());
				eleSOSTSShipment.appendChild(newShipmentContainers);
			}

			else {

				log.verbose(
						"Invoke getShipmentContainerList to get TO Containers when Order number is not used in search");

				Document docGetContainersOut = getShipmentContainerList(env, strSOShipmentNo, "");

				Element eleContainers = XMLUtils.getElementByName(eleSOSTSShipment,  AcademyConstants.ELE_CONTAINERS);
				eleSOSTSShipment.removeChild(eleContainers);

				Element eleContainerList = docGetContainersOut.getDocumentElement();
				newShipmentContainers = docShipmentList.createElement( AcademyConstants.ELE_CONTAINERS);

				newShipmentContainers = (Element) docShipmentList.importNode(eleContainerList, true);
				eleSOSTSShipment.appendChild(newShipmentContainers);

			}

		}

		/*
		 * END - Get the list of STS SO Shipments and get the corresponding TO
		 * Containers and add it to SO Shipment
		 */

		/*
		 * Remove the STS TO shipments once TO containers are added to respective SO
		 * shipments
		 */

		NodeList nlTOSTSShipmentToBeRemoved = XPathUtil.getNodeList(docShipmentList,
				AcademyConstants.XPATH_TO_STS_SHIPMENT);

		for (int m = 0; m < nlTOSTSShipmentToBeRemoved.getLength(); m++) {

			Element eleTO = (Element) nlTOSTSShipmentToBeRemoved.item(m);
			String strShipmentKeyToBeRemoved = eleTO.getAttribute(AcademyConstants.SHIPMENT_KEY);

			if (sShipmentsToRemove.contains(strShipmentKeyToBeRemoved)) {

				eleTO.getParentNode().removeChild(eleTO);
			}
		}

		/*
		 * START - Get the list of STS TO Shipments and group it based on TO OHK
		 */

		NodeList nlTOShipmentList = XPathUtil.getNodeList(docShipmentList,
				AcademyConstants.XPATH_TO_STS_SHIPMENT);

		Set<String> sUniqueTOOHK = new HashSet<String>();
		Set<String> sMultipeTOOHK = new HashSet<String>();

		for (int m = 0; m < nlTOShipmentList.getLength(); m++) {

			// Get TO Order header key for each TO Shipment
			Element eleTOShipment = (Element) nlTOShipmentList.item(m);
			Element eleContainers = XmlUtils.getChildElement(eleTOShipment,  AcademyConstants.ELE_CONTAINERS);

			if (AcademyConstants.ATTR_ZERO.equals(eleContainers.getAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS))) {
				eleTOShipment.getParentNode().removeChild(eleTOShipment);
				continue;
			} else {
				String strTOOHK = eleTOShipment.getAttribute(AcademyConstants.STR_ORDR_HDR_KEY);
				if (!sUniqueTOOHK.contains(strTOOHK)) {
					// Add TO OHK to sUniqueTOOHK if it is not present in sUniqueTOOHK
					sUniqueTOOHK.add(strTOOHK);
				} else {
					// Add TO OHK to sMultipeTOOHK if the TO is already present as part of a
					// shipment (Multiple Shipment case for a TO)
					sMultipeTOOHK.add(strTOOHK);
				}
			}
		}

		Iterator<String> multipleTO = sMultipeTOOHK.iterator();

		while (multipleTO.hasNext()) {

			// If multiple shipments exist for a TO

			NodeList nlC = XPathUtil.getNodeList(docShipmentList,
					AcademyConstants.XPATH_TO_OHK + multipleTO.next() + AcademyConstants.CLOSING_BACKET);

			Integer iTotalNumberOfRecords = 0;

			if (nlC.getLength() > 1) {
				newShipmentContainers = docShipmentList.createElement( AcademyConstants.ELE_CONTAINERS);

				for (int n = 0; n < nlC.getLength(); n++) {
					Element eleTOShipment = (Element) nlC.item(n);
					Element eleTOContainers = XmlUtils.getChildElement(eleTOShipment,  AcademyConstants.ELE_CONTAINERS);
					NodeList nlContainers = eleTOContainers.getElementsByTagName( AcademyConstants.ELE_CONTAINER);

					for (int mn = 0; mn < nlContainers.getLength(); mn++) {
						Element eleTOContainer = (Element) nlContainers.item(mn);
						Element eleNewContainer = docShipmentList.createElement( AcademyConstants.ELE_CONTAINER);
						eleNewContainer = (Element) docShipmentList.importNode(eleTOContainer, true);
						newShipmentContainers.appendChild(eleNewContainer);
						iTotalNumberOfRecords++;
					}
				}

				newShipmentContainers.setAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS, iTotalNumberOfRecords.toString());
				Element eleFirstTO = (Element) nlC.item(0);
				Element eleContainers = XmlUtils.getChildElement(eleFirstTO,  AcademyConstants.ELE_CONTAINERS);
				eleFirstTO.removeChild(eleContainers);
				eleFirstTO.appendChild(newShipmentContainers);
			}

			for (int n = 1; n < nlC.getLength(); n++) {
				// Remove multiple shipments for a TO keeping only one TO shipment with all
				// containers
				Element eleShipmentToRemove = (Element) nlC.item(n);
				eleShipmentToRemove.getParentNode().removeChild(eleShipmentToRemove);
			}

		}
		
		log.verbose("Output of AcademySearchShipmenNoOrderNoFromWSC.getSOShipmentListFORSOTO() Before updating DSV search"+ XMLUtil.getString(docShipmentList));
		
		//Start DSV Search Fix
		 Set<String> sRemovePOShipments = new HashSet<String>();
		 NodeList nlTOShipment = XPathUtil.getNodeList(docShipmentList,"/Shipments/Shipment[@DocumentType='0005']");
		 NodeList nlDSVSOF = XPathUtil.getNodeList(docShipmentList,"/Shipments/Shipment[@DeliveryMethod='PICK' and @ShipmentType='SOF']");
		 log.verbose("The length of TO Shipmentlist:: " +nlTOShipment.getLength());
		 log.verbose("The length of SOF Shipmentlist:: " +nlDSVSOF.getLength());
		 for (int i=0; i< nlDSVSOF.getLength(); i++) {
		 Element eleShipment = (Element) nlDSVSOF.item(i);
	 String strSOOrderNo = eleShipment.getAttribute("OrderNo");
		 log.verbose("The sales orderNo added to set:: "+strSOOrderNo);
		 sRemovePOShipments.add(strSOOrderNo);
		 }
		 for (int i=0; i< nlTOShipment.getLength(); i++) {
	 Element eleShipment = (Element) nlTOShipment.item(i);
		 String strChainedOrderNo = XMLUtil.getAttributeFromXPath(docShipmentList,
		 "/Shipments/Shipment/ShipmentLines/ShipmentLine/OrderLine/ChainedFromOrderLine/Order/@OrderNo");
		 log.verbose("The transer related sales orderNo:: "+strChainedOrderNo);
		log.verbose("Input shipnode:: "+strShipNode);
		String strPOShipNode = eleShipment.getAttribute("ShipNode");
		 log.verbose("PO shipnode:: "+strPOShipNode);
		 if(sRemovePOShipments.contains(strChainedOrderNo)){
		 log.verbose("DSV SOF with related shipment exist");
		if(!strShipNode.equals(strPOShipNode)){
		 log.verbose("DSV SOF with related shipment exist");
		 docShipmentList.getDocumentElement().removeChild(eleShipment);
		 }
		 }

		 }
		 log.verbose("Output of AcademySearchShipmenNoOrderNoFromWSC.getSOShipmentListFORSOTO() After updating DSV search"+ XMLUtil.getString(docShipmentList));
	     log.verbose("AcademySearchShipmenNoOrderNoFromWSC.getSOShipmentListFORSOTO() - End");
	     log.endTimer(this.getClass() + ".getSOShipmentListFORSOTO");
		 return docShipmentList;
	}

	/*
	 * This is used to calculate the date for status search in order to consider the
	 * 45 days range
	 *
	 */
	public String getDefaultCreatetsDate() {
		String strCreatets = null;
		Calendar cal = null;

		// Default number of days is set as 45
		int iNoOfDays = 45;

		String strMinNofOfDays = YFSSystem.getProperty(AcademyConstants.PROP_STS_MOBILE_DAYS_TO_CONSIDER);
		if (!YFCCommon.isVoid(strMinNofOfDays)) {
			log.verbose("Overriding The no of days to consider with :: " + strMinNofOfDays);
			iNoOfDays = Integer.parseInt(strMinNofOfDays);
		}

		cal = Calendar.getInstance();
		SimpleDateFormat sdfDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		log.verbose(sdfDateFormat.format(cal.getTime()));

		cal.add(Calendar.DATE, -iNoOfDays);
		strCreatets = sdfDateFormat.format(cal.getTime());
		log.verbose(strCreatets);

		return strCreatets;
	}
	
	//Added as part of OMNI-72201
		public Document verifyAndGetRFCPOrderList(YFSEnvironment env, String shipNode, String emailId, Element elePaginate) throws Exception {
			
			log.beginTimer(this.getClass() + ".verifyAndGetRFCPOrderList");
			log.verbose("AcademySearchShipmenNoOrderNoFromWSC.verifyAndGetRFCPOrderList()"
					+ "\nshipNode: " + shipNode + "\nemailId:  " + emailId);
			
			Document docRFCPShipmentInput = null;
			Document docRFCPShipmentListOut = null;
			
			docRFCPShipmentInput = XmlUtils.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element eleRFCPShipmentListIn = docRFCPShipmentInput.getDocumentElement();
			
			eleRFCPShipmentListIn.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, AcademyConstants.STR_PICK);
			eleRFCPShipmentListIn.setAttribute(AcademyConstants.ATTR_SHIP_NODE, shipNode);
			eleRFCPShipmentListIn.setAttribute(AcademyConstants.STATUS, AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS);
			
			Element eleBillToCustomer = SCXmlUtil.createChild(eleRFCPShipmentListIn, AcademyConstants.ELE_BILL_TO_ADDRESS);
			eleBillToCustomer.setAttribute(AcademyConstants.ATTR_EMAILID, emailId);
			
			String strCreatets = getDefaultCreatetsDate();
			eleRFCPShipmentListIn.setAttribute(AcademyConstants.ATTR_CREATETS, strCreatets);
			eleRFCPShipmentListIn.setAttribute(AcademyConstants.ATTR_CREATETS_QRY_TYPE, AcademyConstants.GT_QRY_TYPE);
			
			if (!YFCCommon.isVoid(elePaginate)) {
			Element eleInPaginate = (Element) docRFCPShipmentInput.importNode(elePaginate, true);
			eleRFCPShipmentListIn.appendChild(eleInPaginate);
			}
			
			log.verbose("Input to AcademyGetShipmentListForWSC : " +XMLUtil.getString(docRFCPShipmentInput));
			docRFCPShipmentListOut = AcademyUtil.invokeService(env, AcademyConstants.SERV_ACADEMY_GET_SHIPMENT_LIST_FOR_WSC, docRFCPShipmentInput);
			
			log.verbose("Output of AcademySearchShipmenNoOrderNoFromWSC.verifyAndGetRFCPOrderList()"
					+ XMLUtil.getString(docRFCPShipmentListOut));
			log.verbose("AcademySearchShipmenNoOrderNoFromWSC.verifyAndGetRFCPOrderList() - End");

			log.endTimer(this.getClass() + ".verifyAndGetRFCPOrderList");
			return docRFCPShipmentListOut;
		}

}