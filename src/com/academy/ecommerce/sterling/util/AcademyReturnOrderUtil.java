package com.academy.ecommerce.sterling.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyReturnOrderUtil {
	
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyReturnOrderUtil.class);
	public static boolean  checkProNumReturnOrder=false;
	public static Element getLineCharges(String returnLineKey, Element eleReturnOrders) {
		Element lineChargesElem = null;
		NodeList nlineChargesList = null;
		try {
			nlineChargesList = XPathUtil.getNodeList(eleReturnOrders,
					"ReturnOrder/OrderLines/OrderLine[@OrderLineKey='"
							+ returnLineKey + "']/LineCharges");
			if (!YFCObject.isVoid(nlineChargesList)) {
				lineChargesElem = (Element) nlineChargesList.item(0);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new YFSException(e.getMessage());
		}
		return lineChargesElem;
	}
	
//	Get the Line Taxes - remove corresponding ReturnShippingTax along with ReturnShippingCharges
	public static Element getLineTaxes(String returnLineKey, Element eleReturnOrders) {
		Element lineTaxesElem = null;
		NodeList nlineTaxesList = null;
		try {
			
			nlineTaxesList = XPathUtil.getNodeList(eleReturnOrders,
					"ReturnOrder/OrderLines/OrderLine[@OrderLineKey='"
							+ returnLineKey + "']/LineTaxes");
			if (!YFCObject.isVoid(nlineTaxesList)) {
				lineTaxesElem = (Element) nlineTaxesList.item(0);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new YFSException(e.getMessage());
		}
		return lineTaxesElem;
	}
	
	
	
	public static void markReturnLinesWithZero(NodeList listOfRetOrders) {
		int length = listOfRetOrders.getLength();
		try {
			
			for (int i = 0; i < length; i++) {
				Element currRetOrder = (Element) listOfRetOrders.item(i);
				NodeList nReturnLineList = currRetOrder
						.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
				int lengOfLines = nReturnLineList.getLength();
				if (lengOfLines > 0) {
					for (int j = 0; j < lengOfLines; j++) {
						Element currRetLine = (Element) nReturnLineList.item(j);

						Element eleOrderStatus = (Element) XPathUtil.getNode(
								currRetLine,
								"OrderStatuses/OrderStatus[@Status='1100.01']");
						if (!YFCObject.isNull(eleOrderStatus)) {
							currRetLine.setAttribute("QtyAvailableForReceipt",
									eleOrderStatus.getAttribute("StatusQty"));
						} else {
							currRetLine.setAttribute("QtyAvailableForReceipt",
									"0");
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new YFSException(e.getMessage());
		}

	}

	
	public static boolean isReturnReasonCodeExist(YFSEnvironment env, String returnReasonCode) {
		try {
			Document docRefundableReasonCodeValues = getRefundableReasonCodeValuesList(env);
			Element eleExistedReturnReasonCode = (Element) XPathUtil.getNode(
						docRefundableReasonCodeValues.getDocumentElement(),
						"CommonCode[@CodeValue='" + returnReasonCode + "']");
			return (!YFCObject.isVoid(eleExistedReturnReasonCode));
		} catch (Exception e) {
			log.error(e);
		}
		return false;
	}
	
	public static boolean isAcademyTrackingNo(String strTrackingNo) {
		
		// 1/11/2011 - Manju: Fix for 3534 - Changing acacdemy account number to 1Z2A297R
		
		if (!YFCObject.isNull(strTrackingNo) && strTrackingNo.length() >= 8
				&& strTrackingNo.substring(0, 8).equals("1Z2A297R")) {
			
			return true;
		}
//PRO # Number logic to check & returning true if it belongs to Pro# logic as below 
		if (!YFCObject.isNull(strTrackingNo) && strTrackingNo.length() == 9)
		{ 	char lastChar=strTrackingNo.charAt(strTrackingNo.length()-1);
			if(lastChar<='6')
			{ 
				checkProNumReturnOrder=true;
			return true;
			
			}

		}
		return false;
	}
	
	//Addition address
	public static Document addingAddresses(Document doc,Document outXML,Document clsNodeDocDetails)
	{
		
		log.verbose("inside the address Logic copying under Retur Order UTil file -> addingAddresses()");
		Element personOrderShipToEle,personOrderBillToEle;
		
		Element personInfoShipTo = (Element) outXML.getDocumentElement().getElementsByTagName(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO).item(0);
		if (personInfoShipTo != null)
		{
			log.verbose("copy Sales Order shipTo details to personInfoContact at Return order header Level - PersonInfoContact");
		personOrderShipToEle = doc.createElement(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO);
			personOrderShipToEle.setAttribute("AddressLine1", personInfoShipTo.getAttribute("AddressLine1"));
			personOrderShipToEle.setAttribute("State", personInfoShipTo.getAttribute("State"));
			personOrderShipToEle.setAttribute("City", personInfoShipTo.getAttribute("City"));
			personOrderShipToEle.setAttribute("Country", personInfoShipTo.getAttribute("Country"));
			personOrderShipToEle.setAttribute("ZipCode", personInfoShipTo.getAttribute("ZipCode"));
			doc.getDocumentElement().appendChild((Node) personOrderShipToEle);
		}
			
			//<Organization OrganizationCode="703" OrganizationKey="" ResourceIdentifier=""/> getOrganizationHierarchy
			//<Organization OrganizationName=""> PersonInfoContact 
			//<ContactPersonInfo />
			//</Organization>
			
			log.verbose("CLS Node Address Details are "+XMLUtil.getXMLString(clsNodeDocDetails));
			Element personContactInfo = (Element) clsNodeDocDetails.getDocumentElement().getElementsByTagName("ContactPersonInfo").item(0);
			if (personContactInfo != null){
				
				log.verbose("copy SContactPersonInfo at Return order header  Level - PersonInfo ShipTo");
			Element additionalAddEle = doc.createElement("AdditionalAddresses");
			Element addressEle = doc.createElement("AdditionalAddress");
			addressEle.setAttribute("AddressType", "additional");
			 Element personInfoEle = doc.createElement("PersonInfo");
			
			 personInfoEle.setAttribute("AddressLine1", personContactInfo.getAttribute("AddressLine1"));
			 personInfoEle.setAttribute("State", personContactInfo.getAttribute("State"));
			 personInfoEle.setAttribute("City", personContactInfo.getAttribute("City"));
			 personInfoEle.setAttribute("Country", personContactInfo.getAttribute("Country"));
			 personInfoEle.setAttribute("ZipCode", personContactInfo.getAttribute("ZipCode"));
			 addressEle.appendChild((Node)personInfoEle);
			 additionalAddEle.appendChild((Node)addressEle);
			 
			doc.getDocumentElement().appendChild((Node) additionalAddEle);
			log.verbose("additonal add is "+XMLUtil.getElementXMLString(additionalAddEle));
			
			}
		Element personInfoBillTo = (Element) outXML.getDocumentElement().getElementsByTagName("PersonInfoBillTo").item(0);
			if(personInfoBillTo!=null)
			{ log.verbose("copy Sales Order BillTo details to personInfoContact at Return order header Level- PersonInfoBillTo");
			personOrderBillToEle = doc.createElement("PersonInfoBillTo");
			personOrderBillToEle.setAttribute("AddressLine1", personInfoBillTo.getAttribute("AddressLine1"));
			personOrderBillToEle.setAttribute("State", personInfoBillTo.getAttribute("State"));
			personOrderBillToEle.setAttribute("City", personInfoBillTo.getAttribute("City"));
			personOrderBillToEle.setAttribute("Country", personInfoBillTo.getAttribute("Country"));
			personOrderBillToEle.setAttribute("ZipCode", personInfoBillTo.getAttribute("ZipCode"));
			doc.getDocumentElement().appendChild((Node) personOrderBillToEle);
			}
			log.verbose(" exiting the method Retur Order UTil file -> addingAddresses() & resulted XML Document is "+XMLUtil.getXMLString(doc));
		
			return doc;
		
	}
	
//	handling Return Reason Code than ReturnReasonDescription.
	public static Document getRefundableReasonCodeValuesList(YFSEnvironment env) {
		Document docCommonCodeListOutput = null;
		Document docGetReasonCodesInput = null;
		try {
			
			docGetReasonCodesInput = XMLUtil
					.createDocument(AcademyConstants.ELE_COMMON_CODE);
			docGetReasonCodesInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_CODE_TYPE,"RET_RSN_CODES"
					);
			docCommonCodeListOutput = AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_GET_COMMONCODE_SERVICE, docGetReasonCodesInput);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

		return docCommonCodeListOutput;
	}
	
	// In-store Returns - Begin

	/*
	 * Moving this method to utils getting the Sales Order Invoice details by
	 * using invoice NO gets from external system return receipt process Msg.
	 */
	public static Element getOrderInvoiceDetails(YFSEnvironment env,
			String strInvoiceNo) {

		Element eleGetOrderInvoiceDetails = null;
		Document docGetOrderInvoiceDetailsInput = null;
		Document docGetOrderInvoiceDetailsOutput = null;

		try {

			log
					.verbose("*************** Inside getOrderInvoiceDetails method************ ");
			docGetOrderInvoiceDetailsInput = XMLUtil
					.createDocument("OrderInvoice");
			eleGetOrderInvoiceDetails = docGetOrderInvoiceDetailsInput
					.getDocumentElement();
			eleGetOrderInvoiceDetails.setAttribute(
					AcademyConstants.ATTR_INVOICE_NO, strInvoiceNo);

			if (!YFCObject.isVoid(docGetOrderInvoiceDetailsInput)) {

				log
						.verbose("*************** Calling gOrderInvoiceList API with input************ "
								+ XMLUtil
										.getXMLString(docGetOrderInvoiceDetailsInput));
				env.setApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_LIST,
						AcademyConstants.STR_TEMPLATE_GETORDER_INVOICE_DETAILS);

				docGetOrderInvoiceDetailsOutput = AcademyUtil.invokeAPI(env,
						AcademyConstants.API_GET_ORDER_INVOICE_LIST,
						docGetOrderInvoiceDetailsInput);
				env
						.clearApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_LIST);

				if (!YFCObject.isVoid(docGetOrderInvoiceDetailsOutput)) {
					log
							.verbose("*************** Output of gOrderInvoiceList ************ "
									+ XMLUtil
											.getXMLString(docGetOrderInvoiceDetailsOutput));
					eleGetOrderInvoiceDetails = docGetOrderInvoiceDetailsOutput
							.getDocumentElement();

				}
			}

		} catch (Exception e) {
			log.error("Exception - getOrderInvoiceDetails()::" + e);
		}

		return eleGetOrderInvoiceDetails;
	}

	//In-store Returns - End
	
	/*OMNI-50871 --START*/
	public static void CaptureCLSProcessedTimeStamp (YFSEnvironment env,String ReturnOrderHeaderKey,String trackingNo,String itemId,String Qty) throws Exception
    {	
		Document docInputGetReceiptList = XMLUtil.createDocument(AcademyConstants.ELE_RECEIPT);
		docInputGetReceiptList.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.STR_RETURN_DOCTYPE);
		Element eleOrder=SCXmlUtil.createChild(docInputGetReceiptList.getDocumentElement(), AcademyConstants.ELE_ORDER);
		eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, ReturnOrderHeaderKey);
		
		//Call getReceiptList to fetch receiptHeaderKey to invoke getReceiptDetail
		Document receiptListDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_RECEIPT_LIST,docInputGetReceiptList);
		Document docInputGetReceiptdetails = XMLUtil.createDocument(AcademyConstants.ELE_RECEIPT);
		String receiptHeaderKey = XMLUtil.getAttributeFromXPath(receiptListDoc,
				AcademyConstants.XPATH_RECEIPT_HEADER_KEY);
		log.verbose("receipt header key: " + receiptHeaderKey);
		docInputGetReceiptdetails.getDocumentElement().setAttribute(AcademyConstants.ATTR_RECPT_HEADERKEY,receiptHeaderKey);
		env.setApiTemplate(AcademyConstants.API_GET_RECEIPT_DETAILS,
				AcademyConstants.STR_TEMPLATE_GET_RETURN_RECEIPT_DETAIL);
		log.verbose("Input to getReceiptDetail: " + XMLUtil.getXMLString(docInputGetReceiptdetails));
		Document receiptDetailDocument = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_RECEIPT_DETAILS,docInputGetReceiptdetails);
		
		String returnOrderNo = XMLUtil.getAttributeFromXPath(receiptDetailDocument,
				AcademyConstants.XPATH_RETURN_ORDER_NO);
		String orderNo = XMLUtil.getAttributeFromXPath(receiptDetailDocument,
				AcademyConstants.XPATH_DERIVED_ORDER_NO);
		String returnReceiptCreatedBy = receiptDetailDocument.getDocumentElement().getAttribute(AcademyConstants.ATTR_CREATE_PROGID);
		String TrackingNo =trackingNo;
		String createts = receiptDetailDocument.getDocumentElement().getAttribute(AcademyConstants.ATTR_CREATETS);
		
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		String strCurrentTimestamp = sdf.format(new Date());
		if ("PROCESS_CARRIER_UPDATES".equalsIgnoreCase(returnReceiptCreatedBy)) {
			Document docCaptureCLSDoc = XMLUtil.createDocument(AcademyConstants.ELE_ACADEMY_CLS_CARRIER_UPDATE);
		NodeList  nReceiptLine=XMLUtil.getNodeList(receiptDetailDocument,AcademyConstants.XPATH_RECEIPT_LINE);
		for (int i=0;i<nReceiptLine.getLength();i++){
			docCaptureCLSDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_RETURN_ORDER_NO, returnOrderNo);
			docCaptureCLSDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_NO, orderNo);
			docCaptureCLSDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CARRIER_UPDATE_PROCESS_DATE, createts);
			docCaptureCLSDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CLS_RECEIVE_DATE, strCurrentTimestamp);
			docCaptureCLSDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_TRACKING_NO, TrackingNo);
			docCaptureCLSDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ITEM_ID, itemId);
			docCaptureCLSDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_QTY, Qty);
			log.verbose("Input to AcademycaptureCLSProcessedTimeStamp: " + XMLUtil.getXMLString(docCaptureCLSDoc));
			//Invoke service to store data in the custom table
			AcademyUtil.invokeService(env, AcademyConstants.SER_CAPTURE_CLS_PROCESSED_DATA,
					docCaptureCLSDoc);
		}
    }
}
/*OMNI-50871 --END*/
}