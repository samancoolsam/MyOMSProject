package com.academy.ecommerce.sterling.invoice.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCIterable;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author kparvath
 * 
 * getting the ShippingSKUItemID and ShippingDeptNo from CommonCode 
 * 	and DeptNo from getItemDetails API
 * 
 * then Stamping these attributes in TLOG message, while publishing 
 * 	to MATRA system for settlement. 
 *
 *	STL -748 -  Appeasement appearing as no sale in RESA
 */
public class AcademyUpdateShippingSKUForTLogsAPI {
	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyUpdateShippingSKUForTLogsAPI.class);
	/* Start - changes for STL-748 */
	private String getOrderListOutTemplate = "global/template/api/AcademyUpdateShippingSKUForTLogsAPI_getOrderList.xml";
	/* End - changes for STL-748 */
	
	
	public Document updateShippingSKUForTLog(YFSEnvironment env, Document inXML) throws Exception {
		
		log.beginTimer(" Begining of AcademyUpdateShippingSKUForTLogsAPI->updateShippingSKUForTLog()");
		
		log.verbose("AcademyUpdateShippingSKUForTLogsAPI :: inXML-->" + XMLUtil.getXMLString(inXML));
		
		String scacAndServiceCode = "";
		
		
		/**
		 * code modified on 07-16-2013 to stamp the below attributes in Return TLogs.
		 * stamping the ShippingSku and ShippingDeptNo based on InvoiceType.
		 */
		
		
		//get the InvoiceType from the incoming xml.
		String strInvoiceType = XMLUtil.getString(inXML, "/InvoiceDetail/InvoiceHeader/@InvoiceType");
		
		/* Start - changes for STL-748 */
		// If Invoice Type is CREDIT_MEMO then Update Line details for
		// Appeasement created through sterling console.
		if (strInvoiceType.equalsIgnoreCase(AcademyConstants.VALUE_CREDIT_MEMO)) {
			log.verbose("The Invoice type is :" + strInvoiceType);
			updateLineDetails(env, inXML);
		}
		/* End - changes for STL-748 */
		
		//if the InvoiceType is SHIPMENT then get the ScacAndService value from <Shipment> element.
		if (strInvoiceType.equals("SHIPMENT")) {
			NodeList nlShipment = inXML.getElementsByTagName("Shipment");
			Element eleShipment = (Element) nlShipment.item(0);
			scacAndServiceCode = eleShipment.getAttribute("ScacAndService");
			log.verbose("scacAndServiceCode :: " + scacAndServiceCode);
			//call getCommonCodeList API for the ScacAndService fetched.
			String[] shippingSKUAndDepartmentNo = getShipmentSkuAndDepartment(env, scacAndServiceCode);
			//stamp CodeShortDescription and CodeLongDescription as ShippingSKUItemID and ShippingDeptNo respectively.
			eleShipment.setAttribute("ShippingSKUItemID", shippingSKUAndDepartmentNo[0]);
			eleShipment.setAttribute("ShippingDeptNo", shippingSKUAndDepartmentNo[1]);
			
		}
		//else if the InvoiceType is RETURN
		else if (strInvoiceType.equals("RETURN")) {
			//traverse to LineDetail element and get the NodeList
			NodeList lineDetailNL = XMLUtil.getNodeList(inXML, "/InvoiceDetail/InvoiceHeader/LineDetails/LineDetail");
			//Loop through the <LineDetail> element
			for (int iCount = 0; iCount < lineDetailNL.getLength(); iCount++) {
				
				boolean bShippingSKUDisplay = false;
				
				
				//traverse to <DerivedFromOrderLine> element
				Element eleLineDetail = (Element) lineDetailNL.item(iCount);
				
				NodeList nlLineCharge = XMLUtil.getNodeList(eleLineDetail, "LineCharges/LineCharge");
				for (int i = 0; i < nlLineCharge.getLength(); i++) {
					Element eleLineCharge = (Element) nlLineCharge.item(i);
					String strChargeCategory = eleLineCharge.getAttribute("ChargeCategory");
					String strChargeName = eleLineCharge.getAttribute("ChargeName");
					
					if ((("Shipping").equalsIgnoreCase(strChargeCategory) && ("ShippingCharge").equalsIgnoreCase(strChargeName))
							|| (("ReturnShippingCharge").equalsIgnoreCase(strChargeCategory) && ("ReturnShippingCharge")
									.equalsIgnoreCase(strChargeName))) {
						bShippingSKUDisplay = true;
						break;
					}
				}
				
				if (bShippingSKUDisplay) {
					
					Element eleOrderLine = (Element) eleLineDetail.getElementsByTagName("OrderLine").item(0);
					Element eleDerivedFromOrderLine = (Element) eleOrderLine.getElementsByTagName("DerivedFromOrderLine").item(0);
					//get the ScacAndService value
					scacAndServiceCode = eleDerivedFromOrderLine.getAttribute("ScacAndService");
					//call getCommonCodeList API for the ScacAndService fetched.
					String[] shippingSKUAndDepartmentNo = getShipmentSkuAndDepartment(env, scacAndServiceCode);
					//stamp CodeShortDescription and CodeLongDescription as ShippingSKUItemID and ShippingDeptNo respectively at <OrderLine> level.
					eleOrderLine.setAttribute("ShippingSKUItemID", shippingSKUAndDepartmentNo[0]);
					eleOrderLine.setAttribute("ShippingDeptNo", shippingSKUAndDepartmentNo[1]);
					//stamp CodeShortDescription and CodeLongDescription as ShippingSKUItemID and ShippingDeptNo respectively at <DerivedFromOrderLine> level.
					eleDerivedFromOrderLine.setAttribute("ShippingSKUItemID", shippingSKUAndDepartmentNo[0]);
					eleDerivedFromOrderLine.setAttribute("ShippingDeptNo", shippingSKUAndDepartmentNo[1]);
				}
			}
		}
		
		NodeList nlItem = inXML.getElementsByTagName("Item");
		
		if (nlItem.getLength() != 0) {
			
			log.verbose("<Item> Length :: " + nlItem.getLength());
			
			for (int i = 0; i < nlItem.getLength(); i++) {
				
				Element eleItem = (Element) nlItem.item(i);
				
				String departNo = getItemDepartment(env, eleItem);
				
				eleItem.setAttribute("DeptNo", departNo);
				
			}
		}
		
		log.verbose("AcademyUpdateShippingSKUForTLogsAPI :: updated XML-->" + XMLUtil.getXMLString(inXML));
		return inXML;
		
	}
	
	/* Start change for STL-748 */
	private void updateLineDetails(YFSEnvironment env, Document inXML) throws Exception {
		YFCDocument inXMLYFCDoc = YFCDocument.getDocumentFor(inXML);
		YFCElement invoiceDetailElem = inXMLYFCDoc.getDocumentElement();
		YFCElement invoiceHeaderElem = invoiceDetailElem.getChildElement(AcademyConstants.ELE_INVOICE_HDR);
		YFCElement lineDetailsElem = invoiceHeaderElem.getChildElement(AcademyConstants.ELEM_LINE_DETAILS);
		if (lineDetailsElem == null) {
			log.verbose("Line Details element is null and hence will be created in the invoice document.");
			lineDetailsElem = invoiceHeaderElem.createChild(AcademyConstants.ELEM_LINE_DETAILS);
		}

		// Check if line detail element exist. Line detail element won't be
		// available for appeasement created from Sterling Console.
		// Line detail element will only be available for appeasement created
		// from COM console.
		YFCElement lineDetailElem = lineDetailsElem.getChildElement(AcademyConstants.ELEM_LINE_DETAIL);
		if (lineDetailElem == null) {

			// Get the amount to be collected.
			String amountCollected = getAmountToBeCollected(invoiceHeaderElem);
			log.verbose("Appeasement has been created from Sterling Console. Need to form line details.");
			addLineDetailElem(env, lineDetailsElem, amountCollected, invoiceHeaderElem);
		}
	}

	private String getAmountToBeCollected(YFCElement invoiceHeaderElem) {

		YFCElement collectionDetailsElem = invoiceHeaderElem.getChildElement(AcademyConstants.ELEM_COLLECTION_DETAILS);
		YFCIterable<YFCElement> collectionDetailItr = collectionDetailsElem.getChildren(AcademyConstants.ELEM_COLLECTION_DETAIL);
		Double totalAmountCollected = 0.0;
		while (collectionDetailItr.hasNext()) {
			YFCElement collectionDetailElem = collectionDetailItr.next();
			String amountCollected = collectionDetailElem.getAttribute(AcademyConstants.ATTR_AMOUNT_COLLECTED);
			if (!YFCObject.isVoid(amountCollected)) {
				// Remove the negative symbol if exist.
				if (amountCollected.startsWith("-")) {
					amountCollected = new String(amountCollected.substring(1));
				}
				Double amountCollectedD = Double.valueOf(amountCollected);
				totalAmountCollected = totalAmountCollected + amountCollectedD;
			}
		}

		log.verbose("Appeasement amount to be collected:" + String.valueOf(totalAmountCollected));

		return String.valueOf(totalAmountCollected);
	}

	private void addLineDetailElem(YFSEnvironment env, YFCElement lineDetailsElem, String amountCollected, YFCElement invoiceHeaderElem) throws Exception {

		// Line detail will be appended to the invoice thats sent to MATRA.
		lineDetailsElem.setAttribute(AcademyConstants.ATTR_TOTAL_LINES, "1");
		YFCElement lineDetailElem = lineDetailsElem.createChild(AcademyConstants.ELEM_LINE_DETAIL);
		YFCElement lineChargesElem = lineDetailElem.createChild(AcademyConstants.ELE_LINE_CHARGES);
		YFCElement lineChargeElem = lineChargesElem.createChild(AcademyConstants.ELE_LINE_CHARGE);
		lineChargeElem.setAttribute(AcademyConstants.ATTR_CHARGE_AMT, amountCollected);
		lineChargeElem.setAttribute(AcademyConstants.ATTR_CHARGE_CATEGORY, AcademyConstants.VALUE_CUST_APPEASEMENT);
		lineChargeElem.setAttribute(AcademyConstants.ATTR_CHARGE_NAME, AcademyConstants.VALUE_MERCHANDISE);
		lineChargeElem.setAttribute(AcademyConstants.ATTR_REFERENCE, AcademyConstants.VALUE_REFERENCE_MERCHANDISE);
		// Get the order line details.
		YFCElement orderElem = invoiceHeaderElem.getChildElement(AcademyConstants.ELE_ORDER);
		if (orderElem != null) {
			String orderHeaderKey = orderElem.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
			if (!YFCObject.isVoid(orderHeaderKey)) {
				YFCDocument getOrderListInDoc = YFCDocument.createDocument(AcademyConstants.ELE_ORDER);
				getOrderListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, orderHeaderKey);

				log.verbose("AcademyUpdateShippingSKUForTLogsAPI_getOrderListInDoc" + getOrderListInDoc.getString());
				env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, getOrderListOutTemplate);
				Document getOrderListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, getOrderListInDoc.getDocument());
				env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
				YFCDocument getOrderListYFCOutDoc = YFCDocument.getDocumentFor(getOrderListOutDoc);
				log.verbose("AcademyUpdateShippingSKUForTLogsAPI_getOrderListYFCOutDoc" + getOrderListYFCOutDoc.getString());

				YFCElement orderListElem = getOrderListYFCOutDoc.getDocumentElement();
				YFCElement orderElemFromOutput = orderListElem.getChildElement(AcademyConstants.ELE_ORDER);
				YFCElement orderLinesElem = orderElemFromOutput.getChildElement(AcademyConstants.ELE_ORDER_LINES);
				YFCElement orderLineElem = orderLinesElem.getChildElement(AcademyConstants.ELE_ORDER_LINE);
				lineDetailElem.importNode(orderLineElem);
				log.verbose("Order line element appended in line detail element:" + lineDetailElem.getString());
			}
		}
	}
	/* End change for STL-748 */
	
	private String[] getShipmentSkuAndDepartment(YFSEnvironment env, String scacAndServiceCode) {
		
		log.beginTimer(" End of AcademyUpdateShippingSKUForTLogsAPI->getShipmentSkuAndDepartment() Api");
		
		log.verbose("getShipmentSkuAndDepartment and scacAndServiceCode is " + scacAndServiceCode);
		
		Document getCommonCodeListOutDoc = null;
		Document getCommonCodeListInDoc = null;
		String[] shippingSKUAndDepartmentNo = new String[] { "", "" };
		
		if (!YFCObject.isVoid(scacAndServiceCode)) {
			try {
				getCommonCodeListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
				
				getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, "SHP_SKU_TLOG");
				
				getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE, scacAndServiceCode);
				
				getCommonCodeListInDoc.getDocumentElement().setAttribute("OrganizationCode", AcademyConstants.PRIMARY_ENTERPRISE);
				log.verbose("***getCommonCodeList input ***" + XMLUtil.getXMLString(getCommonCodeListInDoc));
				
				getCommonCodeListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST, getCommonCodeListInDoc);
				
				log.verbose("********getCommonCodeList is :: **********" + XMLUtil.getXMLString(getCommonCodeListOutDoc));
				
				if (!YFCObject.isVoid(getCommonCodeListOutDoc)) {
					shippingSKUAndDepartmentNo[0] = XPathUtil.getString(getCommonCodeListOutDoc, "CommonCodeList/CommonCode/@CodeShortDescription");
					shippingSKUAndDepartmentNo[1] = XPathUtil.getString(getCommonCodeListOutDoc, "CommonCodeList/CommonCode/@CodeLongDescription");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		log.verbose("shippingSKUAndDepartmentNo returning [0] : " + shippingSKUAndDepartmentNo[0]);
		log.verbose("shippingSKUAndDepartmentNo returning [1] : " + shippingSKUAndDepartmentNo[1]);
		log.endTimer(" End of AcademyUpdateShippingSKUForTLogsAPI->getShipmentSkuAndDepartment() Api");
		
		return shippingSKUAndDepartmentNo;
		
	}
	
	
	private String getItemDepartment(YFSEnvironment env, Element eleItem) throws Exception {
		
		log.beginTimer(" End of AcademyUpdateShippingSKUForTLogsAPI->getItemDepartment() Api");
		
		Document getItemDetailsOutDoc = null;
		Document getItemDetailsInDoc = XMLUtil.createDocument(AcademyConstants.ITEM);
		
		getItemDetailsInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ITEM_ID, eleItem.getAttribute("ItemID"));
		getItemDetailsInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_UOM, eleItem.getAttribute("UnitOfMeasure"));
		getItemDetailsInDoc.getDocumentElement().setAttribute(AcademyConstants.ORG_CODE_ATTR, AcademyConstants.PRIMARY_ENTERPRISE);
		
		log.verbose("***getItemDetails input ***" + XMLUtil.getXMLString(getItemDetailsInDoc));
		
		env.setApiTemplate("getItemDetails", "global/template/api/getItemDetailsForTLOG.xml");
		
		getItemDetailsOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_DETAILS, getItemDetailsInDoc);
		
		env.clearApiTemplate("getItemDetails");
		log.verbose("***getItemDetails output ***" + XMLUtil.getXMLString(getItemDetailsOutDoc));
		
		String departNo = ((Element) getItemDetailsOutDoc.getElementsByTagName("Extn").item(0)).getAttribute("ExtnDepartment");
		
		log.verbose("EXTN Department No :: " + departNo);
		
		log.endTimer(" End of AcademyUpdateShippingSKUForTLogsAPI->getItemDepartment() Api");
		return departNo;
	}
	
}
