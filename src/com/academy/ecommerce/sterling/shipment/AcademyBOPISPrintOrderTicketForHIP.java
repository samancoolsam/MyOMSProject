package com.academy.ecommerce.sterling.shipment;

/*##################################################################################
*
* Project Name                : POD October Release
* Module                      : OMS
* Author                      : CTS
* Date                        : 05-OCT-2020
* Description                 : This file implements the logic to 
* 								1. Insert Record into ACAD_FIN_TRANSACAT if record is not print
*                               2. Issue a Print Request to MSTR
*
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author                  Version#       Remarks/Description                     
* ---------------------------------------------------------------------------------
* 05-OCT-2020     CTS                      1.0            Initial version
* 18-MAR-2022	  CTS                      2.0            Revised version (OMNI-56183)
* 22-JUL-2022	  CTS                      3.0            Revised version (OMNI-78560)
* ##################################################################################*/


import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.util.xml.XMLUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XPathUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.academy.util.common.AcademyEncoderUtil;

/**
 * This class is used to print and reprint Order Ticket for HIP Printer
 * 
 * @author Nandhini Selvaraj
 */
public class AcademyBOPISPrintOrderTicketForHIP implements YIFCustomApi {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyBOPISPrintOrderTicketForHIP.class);
	private Properties props;

	@Override
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	/**
	 * This method is used to check if the request is for Print or Re-print
	 * In case of print insert shipment record into custom table ACAD_LABEL_PRINT_DATA along with Encoded Order Number
	 * In case of reprint invoke MicroStrategyPrintAdapter
	 *
	 */

	public Document printOrderTicket(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer(this.getClass() + ".printOrderTicket");
		log.debug("AcademyBOPISPrintOrderTicketForHIP.java:printOrderTicket():InDoc " + XMLUtil.getXMLString(inDoc));

		Node nlShipment = XPathUtil.getNode(inDoc, AcademyConstants.ELE_SHIPMENT);

		/**
		 * Check for re-print flag not equal to "Y" or FulfillmentType is STS
		 * Insert records into custom table "ACAD_LABEL_PRINT_DATA"
		 */
		

		//OMNI-67859 - START/END : Check to see if record is present in DB for MSTR Reprint of STS, if not insert one -- Obsolute
		//OMNI-78560 - START/END : Remove the if conditions, so we check for both BOPIS and STS Reprint if record is present in DB, if not insert one

		//OMNI-56177- Start - adding boolean check to verify  if table entry is already present for a particular shipment, if yes then skipping code to insert record in custom table
		Boolean isTableEntryRequired = true;

		String stShipmentNo = XPathUtil.getString(nlShipment, AcademyConstants.XPATH_SHIPMENT_NO);
		Document docInDocForAcademyGetLabelPrintData = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		docInDocForAcademyGetLabelPrintData.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO,
				stShipmentNo);
		Document docOutDocForAcademyGetLabelPrintData = AcademyUtil.invokeService(env,
				AcademyConstants.SERV_ACAD_GET_LABEL_PRINT_DATA, docInDocForAcademyGetLabelPrintData);
		NodeList nlAcadGetLabelPrintData = docOutDocForAcademyGetLabelPrintData
				.getElementsByTagName(AcademyConstants.ELE_ACAD_GET_LABEL_PRINT_DATA);
		log.verbose("Length of AcadGetLabelPrintData nodelist" + nlAcadGetLabelPrintData.getLength());
		if (nlAcadGetLabelPrintData.getLength() > 0) {
			isTableEntryRequired = false;
			log.verbose("isTableEntryRequired flag is set as false");
		}

		// OMNI-56177- End

		if (Boolean.TRUE.equals(isTableEntryRequired)) {

			log.verbose("isTableEntryRequired flag is true");
			int iNumberOfLabels = 1;
			String strNumberOfLabels = XPathUtil.getString(nlShipment, AcademyConstants.XPATH_SHIPMENT_NO_OF_LABELS);
			if (!YFCCommon.isVoid(strNumberOfLabels)) {
				iNumberOfLabels = Integer.parseInt(strNumberOfLabels);
				log.verbose("AcademyBOPISPrintOrderTicketForHIP.java:printOrderTicket(): Number of Labels : "
						+ iNumberOfLabels);
			}

			// The input contains the input for getShipmentList.
			
			String strShipmentKey = XPathUtil.getString(nlShipment, AcademyConstants.XPATH_SHIPMENT_SHIPMENTKEY);
			Document docInDocForGetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			docInDocForGetShipmentList.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
					strShipmentKey);

			Document docGetShpLstOut = AcademyUtil.invokeService(env, AcademyConstants.SERV_ACAD_BOPIS_PRINT_ORDER_SHIPMENT_LIST,
					docInDocForGetShipmentList);

			log.debug("AcademyBOPISPrintOrderTicketForHIP.java:printOrderTicket():outDoc getShipmentList"
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
			String strStoreName = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_STORE_NAME);
			String strStorePhone = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_STORE_PHONE);
			String strCustFName = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_CUST_FIRSTNAME);
			String strCustLName = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_CUST_LASTNAME);
			String strCustPhone = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_CUST_PHONE);
			String strOrderNo = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_ORDERNO);
			String strOrderDate = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_ORDER_DATE);
			String strShipmentNo = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_SHIPMENTNO);
			String strStatusDate = null;
			String strEncodedOrderNo=null;
			
			
			// Encode_Code128_NCHAR
			//OMNI-102203: Start
				strEncodedOrderNo = AcademyEncoderUtil.Encode_Code128_NCHAR(strShipmentNo);
				log.debug("Encoding Shipment number for STS and BOPIS" );
				//OMNI-102203: End
			
			for (YFCElement eleShipAudit : nlShipStatusAudit) {
				String strNewStatus = eleShipAudit.getAttribute(AcademyConstants.ATTR_NEW_STATUS);
				if (YFCCommon.equalsIgnoreCase(AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS, strNewStatus)) {
					strStatusDate = eleShipAudit.getAttribute(AcademyConstants.STR_NEW_STATUS);
				}
			}

			Double dQty = 0.0;
			for (YFCElement eleShipmentLine : nlShipmentLine) {
				String strQuantity = eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
				dQty = dQty + Double.parseDouble(strQuantity);
			}
			int iQty = (int) Math.round(dQty);
			String strNoOfUnits = Integer.toString(iQty);

			String strAlternateFirstName = XPathUtil.getString(nlShipments,
					AcademyConstants.XPATH_SHIPMENT_ALT_CUSTFNAME);
			String strAlternateLastName = XPathUtil.getString(nlShipments,
					AcademyConstants.XPATH_SHIPMENT_ALT_CUSTLNAME);
			String strAlternatePhone = XPathUtil.getString(nlShipments, AcademyConstants.XPATH_SHIPMENT_ALT_CUSTPHONE);
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
				eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_PICK_DATE, strStatusDate);
				eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_CURRENT_LABEL_NO,
						Integer.toString(iCurrentLabel));
				eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_TOTAL_LABEL_COUNT,
						Integer.toString(iNumberOfLabels));
				eleAcadLabelPrintData.setAttribute(AcademyConstants.ATTR_ENCODED_ORDER_NO, strEncodedOrderNo);

				log.debug(
						"AcademyBOPISPrintOrderTicketForHIP.java:printOrderTicket():inDoc AcademyCreateLabelPrintDataService : "
								+ XMLUtil.getXMLString(docAcadLabelPrintDataInput));
				AcademyUtil.invokeService(env, AcademyConstants.SERV_ACAD_CREATE_LABEL_PRINT_DATA,
						docAcadLabelPrintDataInput);
			}
		}
		

		// Invoke printDocumentSetAPI for both print and re-print
		Document docPrintDocOut = invokePrintDocumentSetAPI(env, inDoc);
		log.debug(
				"AcademyBOPISPrintOrderTicketForHIP.java:printOrderTicket():input to printDocumentSet : "
						+ XMLUtil.getXMLString(docPrintDocOut));
		log.endTimer(this.getClass() + ".printOrderTicket");
		return docPrintDocOut;

	}

	/**
	 * This method is used to invoke printDocumentSet API for Hip Printer specific document
	 * 
	 */

	private Document invokePrintDocumentSetAPI(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer(this.getClass() + ".invokePrintDopcumentSetAPI");
		log.debug("AcademyBOPISPrintOrderTicketForHIP.java:printOrderTicket():inDoc printDocumentSet for HIP ----> "
				+ XMLUtil.getXMLString(inDoc));
		
		Node nlShipment = XPathUtil.getNode(inDoc, AcademyConstants.ELE_SHIPMENT);

		String strHipPrinterDocID = props.getProperty(AcademyConstants.STR_DOCUMENT_ID);
		String strStoreID = XPathUtil.getString(nlShipment, AcademyConstants.XPATH_SHIPMENT_SHIPNODE);
		String strPrinterId = XPathUtil.getString(nlShipment, AcademyConstants.XPATH_SHIPMENT_PRINTERID);
		String strPrinterIPAddress = XPathUtil.getString(nlShipment, AcademyConstants.XPATH_SHIPMENT_PRINTERIP);
		String strShipmentNo = XPathUtil.getString(nlShipment, AcademyConstants.XPATH_SHIPMENT_SHIPMENT_NO);

		YFCDocument docPrintInput = YFCDocument.createDocument(AcademyConstants.ELE_PRINT_DOCUMENTS);
		YFCElement elePrintDocumts = docPrintInput.getDocumentElement();
		elePrintDocumts.setAttribute(AcademyConstants.ATTR_FLUSH_TO_PRINTER, AcademyConstants.STR_YES);
		elePrintDocumts.setAttribute(AcademyConstants.ATTR_IS_PICKTICKET_PRINTED, AcademyConstants.STR_YES);
		elePrintDocumts.setAttribute(AcademyConstants.ATTR_PRINT_NAME, AcademyConstants.VAL_ORDER_TICKET_PRINT);

		YFCElement elePrintDocumt = elePrintDocumts.createChild(AcademyConstants.ELE_PRINT_DOCUMENT);
		elePrintDocumt.setAttribute(AcademyConstants.ATTR_DOCUMENT_ID, strHipPrinterDocID);
		elePrintDocumt.setAttribute(AcademyConstants.ATTR_DATA_ELEMENT_PATH, AcademyConstants.VAL_DATA_ELEMENT_PATH);
		YFCElement elePrinterPref = elePrintDocumt.createChild(AcademyConstants.ELE_PRINT_PREFERENCE);
		elePrinterPref.setAttribute(AcademyConstants.ORGANIZATION_CODE, strStoreID);
		elePrinterPref.setAttribute(AcademyConstants.ATTR_PRINTER_ID, strPrinterId);

		YFCElement eleInputData = elePrintDocumt.createChild(AcademyConstants.ELE_INPUT_DATA);
		YFCElement eleShipmnt = eleInputData.createChild(AcademyConstants.ELE_SHIPMENT);
		elePrintDocumt.setAttribute(AcademyConstants.ATTR_DOCUMENT_ID, strHipPrinterDocID);
		eleShipmnt.setAttribute(AcademyConstants.ATTR_PRINTER_IP_ADDRESS, strPrinterIPAddress);
		eleShipmnt.setAttribute(AcademyConstants.ATTR_VALUE_ANSWERS, strShipmentNo);
		/**
		 * Suggestion to append shipment number with order number in the URL of MSTR is
		 * not required as we are storing the encoded order number in the custom table
		 * for MSTR to read , hence commented
		 */
		
		Document docPrintOut = AcademyUtil.invokeService(env, AcademyConstants.SERV_ACAD_BOPIS_PRINT_ORDER_TICKET,
				docPrintInput.getDocument());
		
		log.debug("AcademyBOPISPrintOrderTicketForHIP.java:printOrderTicket():outDoc printDocumentSet for HIP ----> "
				+ XMLUtil.getXMLString(docPrintOut));
		log.endTimer(this.getClass() + ".invokePrintDopcumentSetAPI");
		return docPrintOut;
	}
}
