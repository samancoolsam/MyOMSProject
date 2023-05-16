package com.academy.ecommerce.sterling.shipment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyEncoderUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class is used to print and reprint Order Ticket for HIP Printer
 * 
 * @author Meenakshi
 */

public class AcademySTSFAPrintOrderTicket implements YIFCustomApi {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSFAPrintOrderTicket.class);
	private Properties props;

	@Override
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	/**
	 * This method is used to check if the request is for Print or Re-print In case
	 * of print insert shipment record into custom table ACAD_LABEL_PRINT_DATA along
	 * with Encoded Order Number
	 *
	 */

	public Document printSTSFAOrderTicket(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer(this.getClass() + ".printOrderTicket");
		log.debug("AcademySTSFAPrintOrderTicket:printSTSFAOrderTicket:InDoc " + XMLUtil.getXMLString(inDoc));

		Node nlShipment = XPathUtil.getNode(inDoc, AcademyConstants.ELE_SHIPMENT);

		String strShipmentKey = XPathUtil.getString(nlShipment, AcademyConstants.XPATH_SHIPMENT_SHIPMENTKEY);
		//Preparing Input to fetch the getShipmentList API
		Document docInDocForGetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		docInDocForGetShipmentList.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
				strShipmentKey);

		//Invoke getShipmentList API
		Document docGetShpLstOut = AcademyUtil.invokeService(env,
				AcademyConstants.SERV_ACAD_BOPIS_PRINT_ORDER_SHIPMENT_LIST, docInDocForGetShipmentList);

		log.debug("AcademySTSFAPrintOrderTicket:printSTSFAOrderTicket():outDoc getShipmentList"
				+ XMLUtil.getXMLString(docGetShpLstOut));

		Element eleShipmentListOut = docGetShpLstOut.getDocumentElement();
		String strOrderNo = XPathUtil.getString(docGetShpLstOut, AcademyConstants.XPATH_SHIPMENT_ORDERNO);
		String strAlternateFirstName = XPathUtil.getString(docGetShpLstOut,	AcademyConstants.XPATH_SHIPMENT_ALT_CUSTFNAME);
		String strAlternateLastName = XPathUtil.getString(docGetShpLstOut, AcademyConstants.XPATH_SHIPMENT_ALT_CUSTLNAME);
		String strOrderDate = XPathUtil.getString(docGetShpLstOut, AcademyConstants.XPATH_SHIPMENT_ORDER_DATE);

		log.verbose("OrderNo is:"+strOrderNo+":strAlternateLastNamerNo is:"+strAlternateLastName+":strAlternateFirstName:"+strAlternateFirstName);

		Element eleShipment = SCXmlUtil.getChildElement(eleShipmentListOut, AcademyConstants.ELE_SHIPMENT);
		Element eleBillToAddress = SCXmlUtil.getChildElement(eleShipment, AcademyConstants.ELE_BILL_TO_ADDRESS); 		

		String strPickUpDate = getShipmentStatusDate(eleShipmentListOut);

		//Fetching the total number of Units present on Shipment
		String strNoOfUnits = getTotalItemQuantity(eleShipmentListOut);		

		//Invoke extended DB API to update the Label information in ACAD_LABEL_PRINT_DATA table
		invokeCreateLabelPrintData(env, eleShipmentListOut, strPickUpDate, eleBillToAddress, strAlternateFirstName, 
				strAlternateLastName, strNoOfUnits, strOrderDate, strOrderNo);

		//Concatenate the first name and last name present
		if (!YFCObject.isVoid(strAlternateFirstName) && !YFCObject.isVoid(strAlternateLastName)) {
			log.verbose("Alternate PickUp Person's First name is: " + strAlternateFirstName + " Last name is: " + strAlternateLastName);
			StringBuilder sb = new StringBuilder();
			strAlternateFirstName = sb.append(strAlternateLastName).append(",").append(strAlternateFirstName).toString();
			log.verbose("The Aleternate Pick Up Person Detail: " + strAlternateLastName);
		}

		// Invoke printDocumentSetAPI for both print and re-print
		Document docPrintDocOut = invokePrintDocumentSetAPI(env, inDoc, strPickUpDate, eleBillToAddress,
				strAlternateFirstName, strNoOfUnits, strOrderDate, strOrderNo);
		log.debug("AcademySTSFAPrintOrderTicket:printSTSFAOrderTicket():input to printDocumentSet : "
				+ XMLUtil.getXMLString(docPrintDocOut));
		log.endTimer(this.getClass() + ".printOrderTicket");
		return docPrintDocOut;

	}

	/**
	 * This method is used to invoke printDocumentSet API for Lexmark STS FA Printer
	 * specific document
	 * 
	 */

	private Document invokePrintDocumentSetAPI(YFSEnvironment env, Document inDoc, String strPickUpDate,
			Element eleBillToAddress, String strAlternateFirstName, String strNoOfUnits, String strOrderDate, String strOrderNo) throws Exception {
		log.beginTimer(this.getClass() + ".invokePrintDopcumentSetAPI");
		log.debug(
				"AcademySTSFAPrintOrderTicket:invokePrintDocumentSetAPI:inDoc printDocumentSet for Lexmark for STS FA ----> "
						+ XMLUtil.getXMLString(inDoc));

		Node nlShipment = XPathUtil.getNode(inDoc, AcademyConstants.ELE_SHIPMENT);

		String strSTSFAPrinterDocID = props.getProperty(AcademyConstants.STR_DOCUMENT_ID);
		String strPrinterId = props.getProperty(AcademyConstants.KEY_PRINTER_ID_VALUE);
		String strHeader = props.getProperty(AcademyConstants.KEY_HEADER_VALUE);

		String strStoreID = XPathUtil.getString(nlShipment, AcademyConstants.XPATH_SHIPMENT_SHIPNODE);
		String strShipmentNo = XPathUtil.getString(nlShipment, AcademyConstants.XPATH_SHIPMENT_SHIPMENT_NO);

		//Format the dates to be displayed correctly for printDocumentSetAPI
		strOrderDate = fetchFormattedDate(strOrderDate);
		strPickUpDate = fetchFormattedDate(strPickUpDate);

		Document docPrintInput = XMLUtil.createDocument(AcademyConstants.ELE_PRINT_DOCUMENTS);
		Element elePrintDocumts = docPrintInput.getDocumentElement();
		elePrintDocumts.setAttribute(AcademyConstants.ATTR_FLUSH_TO_PRINTER, AcademyConstants.STR_YES);
		elePrintDocumts.setAttribute(AcademyConstants.ATTR_IS_PICKTICKET_PRINTED, AcademyConstants.STR_YES);
		elePrintDocumts.setAttribute(AcademyConstants.ATTR_PRINT_NAME, "AcademySTSFAPrintOrderTicket");
		
		Element elePrintDocumt = SCXmlUtil.createChild(elePrintDocumts, AcademyConstants.ELE_PRINT_DOCUMENT);
		elePrintDocumt.setAttribute(AcademyConstants.ATTR_DOCUMENT_ID, strSTSFAPrinterDocID);
		elePrintDocumt.setAttribute(AcademyConstants.ATTR_DATA_ELEMENT_PATH, AcademyConstants.VAL_DATA_ELEMENT_PATH);
		
		Element elePrinterPref = SCXmlUtil.createChild(elePrintDocumt, AcademyConstants.ELE_PRINT_PREFERENCE);
		elePrinterPref.setAttribute(AcademyConstants.ORGANIZATION_CODE, strStoreID);
		elePrinterPref.setAttribute(AcademyConstants.ATTR_PRINTER_ID, strPrinterId);
		
		Element eleInputData = SCXmlUtil.createChild(elePrintDocumt, AcademyConstants.ELE_INPUT_DATA);
		Element eleShipmnt = SCXmlUtil.createChild(eleInputData, AcademyConstants.ELE_SHIPMENT);

		eleShipmnt.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		eleShipmnt.setAttribute(AcademyConstants.ATTR_HEADER, strHeader);
		eleShipmnt.setAttribute(AcademyConstants.ATTR_PICKUP_DATE, strPickUpDate);
		String strFirstName = eleBillToAddress.getAttribute(AcademyConstants.ATTR_FNAME);
		String strLastName = eleBillToAddress.getAttribute(AcademyConstants.ATTR_LNAME)+ " ";
		strFirstName = strLastName + strFirstName;
		eleShipmnt.setAttribute(AcademyConstants.ATTR_CUST_NAME, strFirstName);
		eleShipmnt.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		eleShipmnt.setAttribute(AcademyConstants.ALTERNATE_PICKUP_PERSON, strAlternateFirstName);
		eleShipmnt.setAttribute(AcademyConstants.ATTR_QUANTITY, strNoOfUnits);
		eleShipmnt.setAttribute(AcademyConstants.ATTR_ORDER_DATE, strOrderDate);

		Document docPrintOut = AcademyUtil.invokeService(env, AcademyConstants.SERV_ACAD_BOPIS_PRINT_ORDER_TICKET, docPrintInput);

		log.debug(
				"AcademySTSFAPrintOrderTicket:invokePrintDocumentSetAPI:inDoc printDocumentSet for Lexmark for STS FA ----> "
						+ XMLUtil.getXMLString(docPrintOut));
		log.endTimer(this.getClass() + ".invokePrintDopcumentSetAPI");
		return docPrintOut;
	}



	/**
	 * This method is used to invoke createAcadLabelPrintData API for reprint the labels for 
	 * specific document
	 * 
	 */

	private void invokeCreateLabelPrintData(YFSEnvironment env, Element eleShipmentListOut, String strPickUpDate,
			Element eleBillToAddress, String strAlternateFirstName, String strAlternateLastName, String strNoOfUnits, 
			String strOrderDate, String strOrderNo) throws Exception {
		log.beginTimer(this.getClass() + ".invokeCreateLabelPrintData");
		log.debug("AcademySTSFAPrintOrderTicket:invokePrintDocumentSetAPI:inDoc ----> "
				+ XMLUtil.getElementXMLString(eleShipmentListOut));

		log.verbose("isTableEntryRequired flag is true");
		int iNumberOfLabels = 1;

		String strStoreID = XPathUtil.getString(eleShipmentListOut, AcademyConstants.XPATH_SHIPMENT_SHIP_NODE);
		String strStoreName = XPathUtil.getString(eleShipmentListOut, AcademyConstants.XPATH_SHIPMENT_STORE_NAME);
		String strStorePhone = XPathUtil.getString(eleShipmentListOut, AcademyConstants.XPATH_SHIPMENT_STORE_PHONE);
		String strCustFName = XPathUtil.getString(eleShipmentListOut, AcademyConstants.XPATH_SHIPMENT_CUST_FIRSTNAME);
		String strCustLName = XPathUtil.getString(eleShipmentListOut, AcademyConstants.XPATH_SHIPMENT_CUST_LASTNAME);
		String strCustPhone = XPathUtil.getString(eleShipmentListOut, AcademyConstants.XPATH_SHIPMENT_CUST_PHONE);
		String strShipmentNo = XPathUtil.getString(eleShipmentListOut, AcademyConstants.XPATH_SHIPMENT_SHIPMENTNO);
		String strEncodedOrderNo = AcademyEncoderUtil.Encode_Code128_NCHAR(strShipmentNo);
		String strAlternatePhone = XPathUtil.getString(eleShipmentListOut, AcademyConstants.XPATH_SHIPMENT_ALT_CUSTPHONE);


		for (int iCurrentLabel = 1; iCurrentLabel <= iNumberOfLabels; iCurrentLabel++) {
			Document docAcadLabelPrintDataInput = XMLUtil
					.createDocument(AcademyConstants.ELE_ACAD_LABEL_PRINT_DATA);
			Element eleAcadLabelPrintData = docAcadLabelPrintDataInput.getDocumentElement();
			eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
			eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_PRIMARY_CUST_FNAME, strCustFName);
			eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_PRIMARY_CUST_LNAME, strCustLName);
			eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_PRIMARY_CUST_PHONE, strCustPhone);
			eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_ALTERNATE_CUST_FNAME, strAlternateFirstName);
			eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_ALTERNATE_CUST_LNAME, strAlternateLastName);
			eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_ALTERNATE_CUST_PHONE, strAlternatePhone);
			eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
			eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_ORDER_DATE, strOrderDate);
			eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_NO_OF_UNITS, strNoOfUnits);
			eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_STORE_NO, strStoreID);
			eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_STORE_NAME, strStoreName);
			eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_STORE_PHONE, strStorePhone);
			eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_PICK_DATE, strPickUpDate);
			eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_CURRENT_LABEL_NO,
					Integer.toString(iCurrentLabel));
			eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_TOTAL_LABEL_COUNT,
					Integer.toString(iNumberOfLabels));
			eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_ENCODED_ORDER_NO, strEncodedOrderNo);

			log.debug("AcademySTSFAPrintOrderTicket:printSTSFAOrderTicket():inDoc AcademyCreateLabelPrintDataService : "
					+ XMLUtil.getXMLString(docAcadLabelPrintDataInput));
			AcademyUtil.invokeService(env, AcademyConstants.SERV_ACAD_CREATE_LABEL_PRINT_DATA,
					docAcadLabelPrintDataInput);
		}

		log.endTimer(this.getClass() + ".invokeCreateLabelPrintData");
	}

	/**
	 * This method formats the Date.
	 * 
	 * @param strFormattedDate
	 * @return strPickUpDate
	 */
	private String fetchFormattedDate(String strFormattedDate) {
		log.beginTimer(this.getClass() + ".fetchFormattedDate");
		String strPickUpDate = "";
		String[] strDate = strFormattedDate.split("T");
		String[] strYYYYMMDD = strDate[0].split("-");
		strPickUpDate = strYYYYMMDD[1] + "/" + strYYYYMMDD[2] + "/" + strYYYYMMDD[0];
		log.verbose("AcademySTSFAPrintOrderTicket.fetchFormattedDate(): " + strPickUpDate);
		log.endTimer(this.getClass() + ".fetchFormattedDate");
		return strPickUpDate;
	}


	/**
	 * This method fetches the Status Date to be updated as PickupDate
	 * 
	 * @param strFormattedDate
	 * @return strPickUpDate
	 */
	private String getShipmentStatusDate(Element eleShipmentListOut) throws Exception{
		log.beginTimer(this.getClass() + ".getShipmentStatusDate");
		String strPickUpDate  = XPathUtil.getString(eleShipmentListOut, 
				"/Shipments/Shipment/ShipmentStatusAudits/ShipmentStatusAudit[@NewStatus='1100.70.06.30.5']/@NewStatusDate");

		if(YFCObject.isVoid(strPickUpDate)) {
			log.verbose("No StatusDate is available. Updating as Sysdate");
			Date dtTodayDate = new Date();
			SimpleDateFormat sDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_FORMAT);
			strPickUpDate = sDateFormat.format(dtTodayDate);
		}

		log.verbose("Final PickupDate : " + strPickUpDate);		
		log.endTimer(this.getClass() + ".getShipmentStatusDate");
		return strPickUpDate;
	}


	/**
	 * This method fetches the total quantity of items present on Shipment
	 * 
	 * @param strFormattedDate
	 * @return strPickUpDate
	 */
	private String getTotalItemQuantity(Element eleShipmentListOut) throws Exception{
		log.beginTimer(this.getClass() + ".getTotalItemQuantity");
		double dQty=0;
		
		log.verbose("getTotalItemQuantity.eleShipmentListOut ----> "+ XMLUtil.getElementXMLString(eleShipmentListOut));
		Element eleShipment = SCXmlUtil.getChildElement(eleShipmentListOut, AcademyConstants.ELE_SHIPMENT);
		Element eleShipmentLines = SCXmlUtil.getChildElement(eleShipment, AcademyConstants.ELE_SHIPMENT_LINES);
		log.verbose("eleShipmentLines ----> "+ XMLUtil.getElementXMLString(eleShipmentLines));
		NodeList nlShipmentLine = eleShipmentLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

		for (int iSL=0; iSL < nlShipmentLine.getLength(); iSL++ ) {
			Element eleShipmentLine = (Element) nlShipmentLine.item(iSL);
			String strQuantity = eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
			dQty = dQty + Double.parseDouble(strQuantity);			
		}

		int iQty = (int) Math.round(dQty);
		String strNoOfUnits = Integer.toString(iQty);

		log.verbose("Total Item Quantity :"+strNoOfUnits);
		log.endTimer(this.getClass() + ".getTotalItemQuantity");
		return strNoOfUnits;
	}

}
