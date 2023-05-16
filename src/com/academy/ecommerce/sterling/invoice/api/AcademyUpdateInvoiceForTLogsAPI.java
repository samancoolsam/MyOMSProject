/**
 * 
 */
package com.academy.ecommerce.sterling.invoice.api;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
//import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @author nsrivastava
 * 
 */
public class AcademyUpdateInvoiceForTLogsAPI implements YIFCustomApi {

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyUpdateInvoiceForTLogsAPI.class);

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}

	public Document updateMsg(YFSEnvironment env, Document inDoc) {
		log
				.beginTimer(" begin of AcademyUpdateInvoiceForTLogsAPI ->updateMsg Api");
		log.verbose("******* invoking updateMsg method ********");
		log.verbose("******* input doc ********" + XMLUtil.getXMLString(inDoc));
		Element eleInvoiceHeader = null;
		String docType = null;
		Element eleCurrOrder = null;
		String transNoStr = null;
		String regNoStr = null;
		Element eleInvoiceDetail = null;
		String strInvoiceNo = null;

		try {
			eleInvoiceDetail = inDoc.getDocumentElement();
			eleInvoiceHeader = (Element) inDoc.getDocumentElement()
					.getElementsByTagName("InvoiceHeader").item(0);
			strInvoiceNo = XMLUtil.getString(eleInvoiceDetail,
					"InvoiceHeader/@InvoiceNo");
			log.verbose("Invoice No " + strInvoiceNo);

	        /**
			 * Project: NeXus Integration - Enhancements. Code Change Begins
			 * getting the currentdate and stamping as TransactionDate at <InvoiceHeader> level.
			 */
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") 
			{
				public StringBuffer format(Date date, StringBuffer toAppendTo, java.text.FieldPosition pos) {
					StringBuffer toFix = super.format(date, toAppendTo, pos);
		            return toFix.insert(toFix.length()-2, ':');
		        };
		    };
		    String strCurrentDate=dateFormat.format(new Date());
		    eleInvoiceHeader.setAttribute("TransactionDate", strCurrentDate);
        	/**
        	 * Project: NeXus Integration - Enhancements. Code Change Ends
        	 */
			eleCurrOrder = (Element) eleInvoiceDetail.getElementsByTagName(
					"Order").item(0);
			docType = eleCurrOrder.getAttribute("DocumentType");
			if ((AcademyConstants.SALES_DOCUMENT_TYPE).equals(docType)) {
				eleInvoiceHeader.setAttribute(AcademyConstants.TLOG_OPERATOR,
						"00000001");
			} else {
				eleInvoiceHeader.setAttribute(AcademyConstants.TLOG_OPERATOR,
						"00000003");
			}
			if (strInvoiceNo != null & strInvoiceNo.length() > 0) {
				regNoStr = strInvoiceNo.substring(18, 22);
				log.verbose("TerminalNo" + regNoStr);
				transNoStr = strInvoiceNo.substring(22, strInvoiceNo.length());
				log.verbose("TransactionNo" + transNoStr);
			}
			eleInvoiceHeader.setAttribute(AcademyConstants.TLOG_TRANSACTIONNO,
					transNoStr);
			eleInvoiceHeader.setAttribute(AcademyConstants.TLOG_TERMINALNO,
					regNoStr);
			
			//code change to include the original scac, carrierservicecode and scacandservice
			Element eleShipment = (Element) eleInvoiceHeader.getElementsByTagName("Shipment").item(0);
			if(!YFCObject.isVoid(eleShipment))
			{
				String strShipNode = eleShipment.getAttribute("ShipNode");
				log.verbose("***node***"+strShipNode);
				//eliminating store shipments
				if("005".equals(strShipNode))
				{
					log.verbose("***node is 005***"+strShipNode);
					Element eleExtn = (Element) eleShipment.getElementsByTagName("Extn").item(0);
					if(!YFCObject.isVoid(eleExtn))
					{
						String strLOS = eleExtn.getAttribute("ExtnOriginalShipmentLos");
						String strSCAC = eleExtn.getAttribute("ExtnOriginalShipmentScac");
						String strUpgradeFlag = eleExtn.getAttribute("ExtnShipUpgrdOrDowngrd");
						log.verbose("***strLOS***"+strLOS);
						log.verbose("***strSCAC***"+strSCAC);
						log.verbose("***strUpgradeFlag***"+strUpgradeFlag);
						//eliminating the shipments which doesn't have the scac and carrierservicecode in extended fields
						if(!YFCObject.isVoid(strLOS) && !YFCObject.isVoid(strSCAC))
						{
							log.verbose("***SCAC and LOS are not blank***");
							//checking for the upgraded/downgraded shipments
							if("U".equals(strUpgradeFlag) || "D".equals(strUpgradeFlag))
							{
								//stamping original scac and carrierservicecode at shipment level
								eleShipment.setAttribute("SCAC", strSCAC);
								eleShipment.setAttribute("CarrierServiceCode", strLOS);
								NodeList orderLineList = XPathUtil.getNodeList(inDoc, "/InvoiceDetail/InvoiceHeader/LineDetails/LineDetail/OrderLine");
								if (orderLineList.getLength()>0)
								{
									int numberOfNodes = orderLineList.getLength();
									for (int i = 0; i < numberOfNodes; i++)
									{
										Element eleOrderLine = (Element) orderLineList.item(i);
										String strScacAndService = eleOrderLine.getAttribute("ScacAndService");
										//stamping original scac and carrierservicecode at orderline level
										eleOrderLine.setAttribute("SCAC", strSCAC);
										eleOrderLine.setAttribute("CarrierServiceCode", strLOS);
										log.verbose("***setting SCACAndService to original value***");
										//stamping original ScacAndService at shipment level
										eleShipment.setAttribute("ScacAndService", strScacAndService);
										log.verbose("***setting SCACAndService to original value***");
									}
								}
							}
						}
					}
					eleShipment.removeChild(eleExtn);
				}
				Element eleShipNode = (Element) eleShipment.getElementsByTagName("ShipNode").item(0);
				if(!YFCObject.isVoid(eleShipNode))
				{
					String strNodeType = eleShipNode.getAttribute("NodeType");
					log.verbose("***nodetype***"+strNodeType);
					//eliminating store shipments
					if("Drop Ship".equals(strNodeType))
					{
						Element eleExtn = (Element) eleShipment.getElementsByTagName("Extn").item(0);
						if(!YFCObject.isVoid(eleExtn))
						{
							if(!YFCObject.isVoid(eleExtn))
							{
								String strLOS = eleExtn.getAttribute("ExtnOriginalShipmentLos");
								String strSCAC = eleExtn.getAttribute("ExtnOriginalShipmentScac");
								if(!YFCObject.isVoid(strLOS) && !YFCObject.isVoid(strSCAC))
								{
									eleShipment.setAttribute("SCAC", strSCAC);
									eleShipment.setAttribute("CarrierServiceCode", strLOS);
									log.verbose("***SCAC and LOS are not blank***");
								}
							}
						}
						eleShipment.removeChild(eleExtn);
					}
				}
			}
			// OMNI-14859 : Start ACC Dev - Masking credit card numbers when TokenEx is down
			NodeList nlPaymentMethod = XPathUtil.getNodeList(inDoc,
					"/InvoiceDetail/InvoiceHeader/CollectionDetails/CollectionDetail/PaymentMethod[@PaymentReference3='"
							+ AcademyConstants.STR_PLCC_PAYMENT + "']");
			if (nlPaymentMethod.getLength() > 0) {
				maskPLCCCreditCardNo(inDoc);
			}
			//OMNI-14859 : End ACC Dev - Masking credit card numbers when TokenEx is down
		}
		catch (Exception e)
		{
			throw new YFSException(e.getMessage());
		}
		log.verbose("***TLOG***"+XMLUtil.getXMLString(inDoc));
		log.endTimer(" End of AcademyUpdateInvoiceForTLogsAPI ->updateMsg Api");
		return inDoc;
	}

	/**
	 * Method to compare the current invoice number with final invoice number,
	 * and update it. Also do the process for the each invoice.
	 * 
	 * @param env
	 *            YFSEnvironment
	 * @param currentInvoiceNo
	 *            String
	 */
	public Document updateInvoiceTransaction(YFSEnvironment env,
			Document inputDoc) {

		log
				.beginTimer(" Begin of AcademyUpdateInvoiceForTLogsAPI ->updateInvoiceTransaction Api");
		Document finalInvoiceDoc = this.getFinalInvoice(env, inputDoc);
		Document closeStringDoc = null;
		String regNoStr, transNoStr = null;
		String eodTransDateTime = "";
		String finalInvoiceNo = null;
		Element extnElement = null;
		String processed = AcademyConstants.STR_NO;
		DateFormat dbDateFormatter = new SimpleDateFormat("yyyyMMdd");
		DateFormat invoiceDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd");
		String currentInvoiceNo = null;

		log
				.verbose("Entering AcademyUpdateInvoiceForTLogsAPI : updateInvoiceTransaction");

		try {
			Element currentInvoiceHeader = (Element) inputDoc
					.getDocumentElement().getElementsByTagName("InvoiceHeader")
					.item(0);
			currentInvoiceNo = currentInvoiceHeader.getAttribute("InvoiceNo");
			log.verbose("Current Invoice No : " + currentInvoiceNo
					+ "\n get <Extn>");
			if (currentInvoiceHeader.getElementsByTagName("Extn").getLength() > 0) {
				log.verbose("existed <Extn> element at <InvoiceHeader>");
				extnElement = (Element) currentInvoiceHeader
						.getElementsByTagName("Extn").item(0);
				processed = extnElement.hasAttribute("ExtnProcessed") ? extnElement
						.getAttribute("ExtnProcessed")
						: AcademyConstants.STR_NO;
			}
			log.verbose("ExtnProcessed is :" + processed
					+ "\n look for final invoice details");

			if (finalInvoiceDoc != null) {
				log.verbose("Get Final Invoice details ..");
				eodTransDateTime = finalInvoiceDoc.getDocumentElement()
						.getAttribute("DateInvoiced");
				finalInvoiceNo = finalInvoiceDoc.getDocumentElement()
						.getAttribute("InvoiceNo");
				log.verbose("eodTransDateTime : " + eodTransDateTime
						+ " finalInvoiceNo : " + finalInvoiceNo);
			}

			/*
			 * if (currentInvoiceNo != null & currentInvoiceNo.length() > 0) {
			 * String[] tmp = currentInvoiceNo
			 * .split(AcademyConstants.WMS_NODE); if (tmp[1] != null &
			 * tmp[1].length() > 0) { regNoStr =
			 * currentInvoiceNo.substring(currentInvoiceNo .length() - 8,
			 * currentInvoiceNo.length() - 4); log.verbose("TerminalNo" +
			 * regNoStr); transNoStr =
			 * currentInvoiceNo.substring(currentInvoiceNo .length() - 4,
			 * currentInvoiceNo.length()); log.verbose("TransactionNo" +
			 * transNoStr);
			 * 
			 * closeStringDoc = XMLUtil .getDocument("<CloseString
			 * EodTransDateTime='" + eodTransDateTime + "' EodTransNo='" +
			 * transNoStr + "' StoreNo=\"00005\" RegisterNo='" + regNoStr + "'
			 * />"); } }
			 */

			/**
			 * If the order is already process, no action will be taken. If the
			 * invoice is matching to the final invoice number, then it'll be
			 * processed and updated the YFS_ORDER_INVOICE, as processed.
			 */
			log.verbose("Check wheather Final Invoice is current invoice");
			if ((finalInvoiceNo != null && finalInvoiceNo.length() > 0)
					&& (currentInvoiceNo != null && currentInvoiceNo.length() > 0)
					&& currentInvoiceNo.equalsIgnoreCase(finalInvoiceNo)) {
				if (AcademyConstants.STR_NO.equalsIgnoreCase(processed)) {
					log
							.verbose("This Invoice is Final Invoice. sending Close string..");
					log.verbose("Index for regNo is "
							+ (currentInvoiceNo.length() - 8));

					regNoStr = currentInvoiceNo.substring((currentInvoiceNo
							.length() - 8), (currentInvoiceNo.length() - 4));
					log.verbose("TerminalNo" + regNoStr
							+ "\n Index for transNo is : "
							+ (currentInvoiceNo.length() - 4));
					transNoStr = currentInvoiceNo.substring((currentInvoiceNo
							.length() - 4), currentInvoiceNo.length());
					log.verbose("TransactionNo" + transNoStr);
					String eodDate = eodTransDateTime != null ? dbDateFormatter
							.format(invoiceDateTimeFormatter
									.parse(eodTransDateTime.substring(0, 10)))
							: "";
					log.verbose("EOD date is " + eodDate);
					/*DateFormat dbDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
					String currDateTime = new YFCDate(new Date()).toString();
					DateFormat dbInvoiceDateTimeFormatter = new SimpleDateFormat("yyyyMMddHHmmss");*/
					closeStringDoc = XMLUtil
							.getDocument("<CloseString EodTransDateTime='"
									+ eodTransDateTime
									+ "' EodTransNo='"
									+ transNoStr
									+ "' StoreNo=\"00005\" RegisterNo='"
									+ regNoStr
									+ "' EodTransKey='"
									+ currentInvoiceHeader
											.getAttribute("OrderInvoiceKey")
									+ "' EodTransDate='" + eodDate 
									+ "' EodInvoiceNo='"+currentInvoiceHeader.getAttribute("InvoiceNo")+"'/>");
					log.verbose("CloseString XML is "
							+ XMLUtil.getXMLString(closeStringDoc)
							+ "\n update Final_Invoice_Details with flag is Y");
					// Update the final invoice
					updateFinalInvoiceDetail(env, finalInvoiceNo,
							AcademyConstants.STR_YES);

					// Publish the invoice to service -
					// AcademyPublishInvoiceDetailsService
					AcademyUtil
							.invokeService(
									env,
									AcademyConstants.SERVICE_ACADEMY_PUBLISH_INVOICE_DETAILS,
									closeStringDoc);

					// Update the YFS_Order_Invoice for the invoice number as
					// processed
					updateOrderInvoiceAsProcessed(env, currentInvoiceHeader
							.getAttribute("OrderInvoiceKey"),
							AcademyConstants.STR_YES);
				}
				log.verbose("Posted Close String to TLog");
			} else {
				log.verbose("Invoice is not a final invoice .. ");
				if (AcademyConstants.STR_NO.equalsIgnoreCase(processed)) {
					// Update the YFS_Order_Invoice for the invoice number as
					// processed, when it's not the current invoice number.
					updateOrderInvoiceAsProcessed(env, currentInvoiceHeader
							.getAttribute("OrderInvoiceKey"),
							AcademyConstants.STR_YES);
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		log
				.verbose("Exiting AcademyUpdateInvoiceForTLogsAPI : updateInvoiceTransaction");
		log
				.endTimer(" End of AcademyUpdateInvoiceForTLogsAPI ->updateInvoiceTransaction Api");
		return closeStringDoc;
	}

	/**
	 * Method to get the final invoice details.
	 * 
	 * @param env
	 *            YFSEnvironment
	 * @param invoiceNo
	 *            String
	 * @return output Document
	 */
	public Document getFinalInvoiceDetail(YFSEnvironment env, String invoiceNo) {
		Document output = null;
		Document input = null;
		log
				.beginTimer(" begin of AcademyUpdateInvoiceForTLogsAPI ->getFinalInvoiceDetail Api");
		log
				.verbose("Entering AcademyUpdateInvoiceForTLogsAPI : getFinalInvoiceDetail");

		try {
			input = XMLUtil
					.getDocument("<ACAD_FINAL_INVOICE_DETAIL InvoiceNo='"
							+ invoiceNo + "'/>");
			output = AcademyUtil.invokeService(env,
					"AcademyGetFinalInvoiceDetailSyncService", input);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		log
				.verbose("Exiting AcademyUpdateInvoiceForTLogsAPI : getFinalInvoiceDetail");
		log
				.endTimer(" End of AcademyUpdateInvoiceForTLogsAPI ->getFinalInvoiceDetail Api");
		return output;
	}

	/**
	 * Method to update the final invoice detail, processed to "Y".
	 * 
	 * @param env
	 *            YFSEnvironment
	 * @param invoiceNo
	 *            String
	 * @return output Document
	 */
	public Document updateFinalInvoiceDetail(YFSEnvironment env,
			String invoiceNo, String processedFlag) {
		Document output = null;
		Document inputDoc = null;
		log
				.beginTimer(" Begin of AcademyUpdateInvoiceForTLogsAPI ->updateFinalInvoiceDetail Api");
		log
				.verbose("Entering AcademyUpdateInvoiceForTLogsAPI : updateFinalInvoiceDetail");

		try {
			inputDoc = XMLUtil
					.getDocument("<ACAD_FINAL_INVOICE_DETAIL Processed = '"
							+ processedFlag + "' InvoiceNo='" + invoiceNo
							+ "'/>");
			output = AcademyUtil.invokeService(env,
					"AcademyUpdateFinalInvoiceDetailSyncService", inputDoc);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		log
				.verbose("Exiting AcademyUpdateInvoiceForTLogsAPI : updateFinalInvoiceDetail");
		log
				.endTimer(" End of AcademyUpdateInvoiceForTLogsAPI ->updateFinalInvoiceDetail Api");
		return output;
	}

	/**
	 * Method to get the final invoice number for current date.
	 * 
	 * @param env
	 *            YFSEnvironment
	 * @param inXML
	 *            Document

	 * @return outputList List
	 */
	public Document getFinalInvoice(YFSEnvironment env, Document inputDoc) {
		Document input = null;
		Document output = null;
		Document outputDoc = null;
		YFCDate yfcDate = new YFCDate(new Date());
		String invoiceNo = null;
		try {
			log
					.beginTimer(" Begin of AcademyUpdateInvoiceForTLogsAPI ->getFinalInvoice Api");
			log
					.debug("Entering AcademyUpdateInvoiceForTLogsAPI : getFinalInvoice ");

			/*
			 * env .setApiTemplate( "getOrderInvoiceList", XMLUtil
			 * .getDocument("<OrderInvoiceList><OrderInvoice><Extn/></OrderInvoice></OrderInvoiceList>"));
			 */
			input = XMLUtil
					.getDocument("<ACAD_FINAL_INVOICE_DETAIL DateInvoicedQryType=\"DATERANGE\" FromDateInvoiced='"
							+ yfcDate.toString().substring(0, 8)
							+ "' ToDateInvoiced='"
							+ yfcDate.toString().substring(0, 8)
							+ "' IgnoreOrdering=\"N\"><OrderBy><Attribute Name=\"DateInvoiced\" Desc=\"Y\"/></OrderBy></ACAD_FINAL_INVOICE_DETAIL>");
			// output = AcademyUtil.invokeAPI(env, "getOrderInvoiceList",
			// input);
			log.verbose("input to getACAD_FINAL_INVOICE_DETAILList is :"
					+ XMLUtil.getXMLString(input));
			output = AcademyUtil.invokeService(env,
					AcademyConstants.SERVICE_GET_FINAL_INVOICE_DETAIL_LIST,
					input);
			log.verbose("output of getACAD_FINAL_INVOICE_DETAILList is :"
					+ XMLUtil.getXMLString(output));
			Element eleFinalInvoice = (Element) output.getDocumentElement()
					.getElementsByTagName("ACAD_FINAL_INVOICE_DETAIL").item(0);
			if (eleFinalInvoice != null) {
				invoiceNo = eleFinalInvoice.getAttribute("InvoiceNo");
				log.verbose("Invoice No : " + invoiceNo);
				outputDoc = XMLUtil.getDocument("<OrderInvoice InvoiceNo='"
						+ invoiceNo + "' DateInvoiced='"
						+ eleFinalInvoice.getAttribute("DateInvoiced")
						+ "' FINAL_INVOICE_KEY='"
						+ eleFinalInvoice.getAttribute("FINAL_INVOICE_KEY")
						+ "'/>");
				log.verbose("Final Invoice Details :"
						+ XMLUtil.getXMLString(outputDoc));
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		log
				.endTimer(" End of AcademyUpdateInvoiceForTLogsAPI ->getFinalInvoice Api");
		log.debug("Exiting AcademyUpdateInvoiceForTLogsAPI : getFinalInvoice ");

		return outputDoc;
	}

	/**
	 * Method to update the YFS_ORDER_INVOICE, processed column to "Y" for the
	 * current invoice number, it's not already processed.
	 * 
	 * @param env
	 *            YFSEnvironment
	 * @param orderInvoiceKey
	 *            String
	 * @return outputDoc Document
	 */
	public Document updateOrderInvoiceAsProcessed(YFSEnvironment env,
			String orderInvoiceKey, String processedFlag) {
		Document inputDoc = null;
		Document outputDoc = null;
		YIFApi yifApi;
		YFSEnvironment envNew;
		try {
			log
					.beginTimer(" begin of AcademyUpdateInvoiceForTLogsAPI ->updateOrderInvoiceAsProcessed Api");
			/*
			 * yifApi = YIFClientFactory.getInstance().getLocalApi(); Document
			 * docEnv = XMLUtil.createDocument(AcademyConstants.ELE_ENV);
			 * docEnv.getDocumentElement().setAttribute(
			 * AcademyConstants.ATTR_USR_ID, env.getUserId());
			 * docEnv.getDocumentElement().setAttribute(
			 * AcademyConstants.ATTR_PROG_ID, env.getProgId()); envNew =
			 * yifApi.createEnvironment(docEnv);
			 */
			/*
			 * env.setApiTemplate("changeOrderInvoice", XMLUtil .getDocument("<OrderInvoice
			 * OrderInvoiceKey=\"\" />"));
			 */
			inputDoc = XMLUtil.getDocument("<OrderInvoice OrderInvoiceKey='"
					+ orderInvoiceKey + "'><Extn ExtnProcessed='"
					+ processedFlag + "' /></OrderInvoice>");
			log.verbose("Input to changeOrderInvoice is : "
					+ XMLUtil.getXMLString(inputDoc));
			outputDoc = AcademyUtil.invokeService(env,
					"AcademyProcessSendOrderInvoiceService", inputDoc);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		log
				.endTimer(" End of AcademyUpdateInvoiceForTLogsAPI ->updateOrderInvoiceAsProcessed Api");
		return outputDoc;
	}
	/**
	 * This method is implemented for #4243
	 * Method to compare the current store return invoice number with final invoice number,
	 * and update it. Also do the process for the each invoice.
	 * @param env
	 * @param inDoc
	 * @return
	 */
	public Document updateStorReturnInvoiceTransaction(YFSEnvironment env, Document inDoc){
		log.beginTimer(" Begin of AcademyUpdateInvoiceForTLogsAPI ->updateStorReturnInvoiceTransaction Api");
		if(env.getTxnObject("ReturnOrderName") != null && env.getTxnObject("ReturnOrderName").equals(AcademyConstants.STR_INSTORE_RETURN)){
			log.verbose("Current transaction is for "+env.getTxnObject("ReturnOrderName")+" Retrun Order");
			try{
				if(inDoc != null){
					log.verbose("Update ExtnProcessed flag as 'Y' ");
					updateOrderInvoiceAsProcessed(env, inDoc.getDocumentElement().getAttribute("OrderInvoiceKey"), "Y");
					env.setTxnObject("ReturnOrderName", "");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		log.verbose(" End of AcademyUpdateInvoiceForTLogsAPI ->updateStorReturnInvoiceTransaction Api" );
		return inDoc;
	}
	/** OMNI-14859 : ACC Dev - Masking credit card numbers when Token Ex is down
	 * This method is to mask the credit card number as 123456******7891 for the
	 * clear text PaymentMethod- 
	 * 
	 * @param inDoc
	 * @return inDoc
	 */
	public Document maskPLCCCreditCardNo(Document inDoc) {
		log.beginTimer(" Begin of AcademyUpdateInvoiceForTLogsAPI ->maskPLCCCreditCardNo Api");
		NodeList nlPaymentMethod;
		try {
			nlPaymentMethod = XPathUtil.getNodeList(inDoc,
					"/InvoiceDetail/InvoiceHeader/CollectionDetails/CollectionDetail/PaymentMethod[@PaymentReference3='"
							+ AcademyConstants.STR_PLCC_PAYMENT + "']");
			int numberOfPayments = nlPaymentMethod.getLength();
			for (int i = 0; i < numberOfPayments; i++) {
				Element elePaymentMethod = (Element) nlPaymentMethod.item(i);
				log.verbose("Payment Method is: " + XmlUtils.getString(elePaymentMethod));
				String strCreditCardNo = elePaymentMethod.getAttribute(AcademyConstants.ATTR_CREDIT_CARD_NO);
				String strMaskedCardNo = strCreditCardNo.replaceAll("\\b(\\d{6})\\d+(\\d{4})", "$1000000$2");
				log.verbose("Masked card No:" + strMaskedCardNo);
				elePaymentMethod.setAttribute(AcademyConstants.ATTR_CREDIT_CARD_NO, strMaskedCardNo);
			}
		} catch (Exception e) {
			log.info("Exception :: " + e.toString() + " Exception Stack Trace" + e.getStackTrace().toString());
		}
		log.verbose(" End of AcademyUpdateInvoiceForTLogsAPI ->maskPLCCCreditCardNo Api");
		return inDoc;
	}
}
