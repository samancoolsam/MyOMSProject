package com.academy.ecommerce.sterling.shipment;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.ibm.icu.text.SimpleDateFormat;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class is used to print / re-print STS Order Ticket (STS staging Label
 * from Loftware if Hip Printer is Disabled)
 * 
 * @author Nandhini Selvaraj OMNI-66709 -> STS Fallback Printer: Print on
 *         Loftware printer incase no HIP printer is enabled at store
 * 
 *         OMNI-67859 -> STS Fallback Printer: Reprint on Loftware printer
 *         incase no HIP printer is enabled at store and actual print happened
 *         on Loftware/HIP Printer
 * 
 *         Output of the method will be Input to printDocumentSet api
 *
 */
public class AcademySTSPrintOrderTicket implements YIFCustomApi {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSPrintOrderTicket.class);
	private Properties props;

	@Override
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	public Document printSTSOrderTicket(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer(this.getClass() + ".printSTSOrderTicket");
		log.verbose("AcademySTSprintSTSOrderTicket.java:printSTSOrderTicket():InDoc :: " + XMLUtil.getXMLString(inDoc));

		Node nlShipment = XPathUtil.getNode(inDoc, AcademyConstants.ELE_SHIPMENT);

		String strShipmentKey = XPathUtil.getString(nlShipment, AcademyConstants.XPATH_SHIPMENT_SHIPMENTKEY);
		Document docInDocForGetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		docInDocForGetShipmentList.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
				strShipmentKey);

		Document docGetShpLstOut = AcademyUtil.invokeService(env,
				AcademyConstants.SERV_ACAD_BOPIS_PRINT_ORDER_SHIPMENT_LIST, docInDocForGetShipmentList);

		log.verbose("AcademySTSPrintOrderTicket.java:printSTSOrderTicket():outDoc getShipmentList"
				+ XMLUtil.getXMLString(docGetShpLstOut));

		// Fetch the elements from getShipmentList Output.
		YFCDocument yfcOutDocShpList = YFCDocument.getDocumentFor(docGetShpLstOut);
		YFCElement eleOutDocShpListShpmts = yfcOutDocShpList.getDocumentElement();
		YFCElement eleOutDocShpListShpmt = eleOutDocShpListShpmts.getChildElement(AcademyConstants.ELE_SHIPMENT);
		YFCElement eleShipmentLines = eleOutDocShpListShpmt.getChildElement(AcademyConstants.ELE_SHIPMENT_LINES);
		YFCNodeList<YFCElement> nlShipmentLine = eleShipmentLines
				.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		YFCNodeList<YFCElement> nlShipStatusAudit = eleOutDocShpListShpmt
				.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_STATUS_AUDIT);

		Node nlShipments = XPathUtil.getNode(docGetShpLstOut, AcademyConstants.ELE_SHIPMENTS);

		String strStoreID = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_SHIP_NODE);
		// Concatenating LastName and FisrstName together with a Comma in between and
		// setting it as Customer name.
		String strCustFName = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_CUST_FIRSTNAME);
		String strCustLName = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_CUST_LASTNAME);
		String strCustName = strCustLName + ", " + strCustFName;
		String strOrderNo = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_ORDERNO);
		String strOrderDate = fetchFormattedDate(
				XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_ORDER_DATE));
		String strShipmentNo = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_SHIPMENTNO);
		String strPickUpDate = fetchPickUpDate(nlShipStatusAudit);
		// If the shipment container is scanned and shipment is not in RFCP when
		// shipment has multiple containers,
		// Make current date as Pick date
		if (YFCCommon.isVoid(strPickUpDate)) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			Calendar cal = Calendar.getInstance();
			Date sysDate = cal.getTime();
			String strSysDate = dateFormat.format(sysDate);
			strPickUpDate = fetchFormattedDate(strSysDate);
		}
		
		Double dQty = 0.0;
		for (YFCElement eleShipmentLine : nlShipmentLine) {
			String strQuantity = eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
			dQty = dQty + Double.parseDouble(strQuantity);
		}
		
		int iQty = (int) Math.round(dQty);
		String strItemQty = Integer.toString(iQty);
		
		String strAltCustPickUpName = "";
		String strAlternateFirstName = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_ALT_CUSTFNAME);
		String strAlternateLastName = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_ALT_CUSTLNAME);
		if (!YFCCommon.isVoid(strAlternateFirstName) || !YFCCommon.isVoid(strAlternateLastName)) {
			// Concatenating AltLastName and AltFisrstName together with a Comma in between
			// and setting it as AltCustomer name.
			strAltCustPickUpName = strAlternateLastName + ", " + strAlternateFirstName;
		}

		// fetch PrinterName, PrinterIDD , Header and Document id from Service
		// Arguments.
		String strDocumtID = props.getProperty(AcademyConstants.KEY_DOCUMENT_ID_VALUE);
		String strPrinterId = props.getProperty(AcademyConstants.KEY_PRINTER_ID_VALUE);
		String strHeader = props.getProperty(AcademyConstants.KEY_HEADER_VALUE);
		String strPrinterName = props.getProperty(AcademyConstants.ATTR_PRINT_NAME);

		// Create input document for printDocument Set.
		YFCDocument outDocPrint = YFCDocument.createDocument(AcademyConstants.ELE_PRINT_DOCUMENTS);
		YFCElement elePrintDocumts = outDocPrint.getDocumentElement();
		elePrintDocumts.setAttribute(AcademyConstants.ATTR_FLUSH_TO_PRINTER, AcademyConstants.STR_YES);
		elePrintDocumts.setAttribute(AcademyConstants.ATTR_IS_PICKTICKET_PRINTED, AcademyConstants.STR_YES);
		elePrintDocumts.setAttribute(AcademyConstants.ATTR_PRINT_NAME, strPrinterName);

		YFCElement elePrintDocumt = elePrintDocumts.createChild(AcademyConstants.ELE_PRINT_DOCUMENT);
		elePrintDocumt.setAttribute(AcademyConstants.ATTR_DOCUMENT_ID, strDocumtID);
		elePrintDocumt.setAttribute(AcademyConstants.ATTR_DATA_ELEMENT_PATH, AcademyConstants.VAL_DATA_ELEMENT_PATH);

		YFCElement elePrinterPref = elePrintDocumt.createChild(AcademyConstants.ELE_PRINT_PREFERENCE);
		elePrinterPref.setAttribute(AcademyConstants.ORGANIZATION_CODE, strStoreID);
		elePrinterPref.setAttribute(AcademyConstants.ATTR_PRINTER_ID, strPrinterId);

		YFCElement eleInputData = elePrintDocumt.createChild(AcademyConstants.ELE_INPUT_DATA);
		YFCElement eleShipmnt = eleInputData.createChild(AcademyConstants.ELE_SHIPMENT);
		eleShipmnt.setAttribute(AcademyConstants.ATTR_HEADER, strHeader);
		eleShipmnt.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		eleShipmnt.setAttribute(AcademyConstants.ATTR_PICKUP_DATE, strPickUpDate);
		eleShipmnt.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		eleShipmnt.setAttribute(AcademyConstants.ATTR_ORDER_DATE, strOrderDate);
		eleShipmnt.setAttribute(AcademyConstants.ATTR_CUST_NAME, strCustName);
		eleShipmnt.setAttribute(AcademyConstants.ALTERNATE_PICKUP_PERSON, strAltCustPickUpName);
		eleShipmnt.setAttribute(AcademyConstants.ATTR_QUANTITY, strItemQty);

		log.verbose("AcademySTSprintSTSOrderTicket.java:printSTSOrderTicket():OutDoc ::"
				+ XMLUtil.getXMLString(outDocPrint.getDocument()));
		log.endTimer(this.getClass() + ".printSTSOrderTicket");
		return outDocPrint.getDocument();

	}

	/**
	 * This method formats the Date.
	 * 
	 * @param strFormattedDate
	 * @return strPickUpDate
	 */
	public String fetchFormattedDate(String strFormattedDate) {
		log.beginTimer(this.getClass() + ".fetchFormattedDate");
		String strPickUpDate = "";
		String[] strDate = strFormattedDate.split("T");
		String[] strYYYYMMDD = strDate[0].split("-");
		strPickUpDate = strYYYYMMDD[1] + "/" + strYYYYMMDD[2] + "/" + strYYYYMMDD[0];
		log.verbose("AcademySTSprintSTSOrderTicket.fetchFormattedDate(): " + strPickUpDate);
		log.endTimer(this.getClass() + ".fetchFormattedDate");
		return strPickUpDate;
	}

	/**
	 * This method formats the Pick up date.
	 * 
	 * @param nlShipStatusAudit
	 * @return strPickUpDate
	 */

	public String fetchPickUpDate(YFCNodeList<YFCElement> nlShipStatusAudit) {
		log.beginTimer(this.getClass() + ".fetchPickUpDate");

		String strPickUpDate = "";
		for (YFCElement eleShipAudit : nlShipStatusAudit) {
			String strNewStatus = eleShipAudit.getAttribute(AcademyConstants.ATTR_NEW_STATUS);
			if (YFCCommon.equalsIgnoreCase("1100.70.06.30.5", strNewStatus)) {
				String strStatusDate = eleShipAudit.getAttribute("NewStatusDate");
				String[] strDate = strStatusDate.split("T");
				String[] strYYYYMMDD = strDate[0].split("-");
				strPickUpDate = strYYYYMMDD[1] + "/" + strYYYYMMDD[2] + "/" + strYYYYMMDD[0];
				log.verbose("AcademySTSprintSTSOrderTicket.fetchPickUpDate():PickUpdate: " + strPickUpDate);
				break;
			}
		}
		
		log.endTimer(this.getClass() + ".fetchPickUpDate");
		return strPickUpDate;
	}
}