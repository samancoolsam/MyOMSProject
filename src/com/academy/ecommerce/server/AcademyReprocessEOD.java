package com.academy.ecommerce.server;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dblayer.YFCContext;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class to identify if the EOD message require reprocess
 * 
 * @author nkarthik
 * 
 */

public class AcademyReprocessEOD implements YIFCustomApi {
	/**
	 * Instance to store the properties configured in Configurator.
	 */
	private Properties props;
	
	static final String INVOICE_KEY = "ORDER_INVOICE_KEY";
	
	static final String INVOICE_NO = "INVOICE_NO";
	
	static final String INVOICE_TYPE = "INVOICE_TYPE";
	
	static final String INVOICED_DATE = "CREATETS";
	
	static final String TABLENAME = "YFS_ORDER_INVOICE";
	
	static final String MODIFIED_INVOICED_DATE = "MODIFYTS";
	
	String eodInvoiceKey = "";
	String maxInvoiceNo = "";
	DateFormat dbDateFormatter = new SimpleDateFormat("yyyyMMdd");
	DateFormat invoiceDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd");
	String invoiceKeyModifyDate = "";
	
	/*
	 * Instance of logger
	 */
	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyReprocessEOD.class);
	
	/*
	 * Setting properties attribute
	 */
	public void setProperties(Properties arg0) throws Exception {
		this.props = arg0;
	}
	
	/*
	 * Reprocess the EOD message
	 */
	public Document reprocessEODMessage(YFSEnvironment env, Document inDoc) throws Exception {
		
		logger.verbose("Entering AcademyReprocessEOD : reprocessEODMessage ");
		
		YFCContext ctxt = (YFCContext) env;
		YFCDate yfcDate = new YFCDate(new Date());
		Statement stmt1 = null;
		Statement stmt2 = null;
		ResultSet rs = null;
		boolean isReprocess;
		Document inputDocument = null;
		
		inputDocument = XMLUtil.getDocument("<ACAD_Final_Invoice_Detail><ComplexQuery><Exp "
				+ "Name=\"InvoiceNo\" Value=\"\" QryType=\"FLIKE\"/></ComplexQuery></ACAD_Final_Invoice_Detail>");
		String invoiceNo = calculateDate();
		
		Element rootElement = inputDocument.getDocumentElement();
		Element expElement = (Element) rootElement.getElementsByTagName("Exp").item(0);
		expElement.setAttribute("Value", invoiceNo);
		
		logger.verbose("Input Document  : getACADFinalInvoiceDetialList " + XMLUtil.serialize(rootElement));
		isReprocess = getACADFinalInvoiceDetialList(env, inputDocument);
		
		if (isReprocess) {
			String strDateFormat = "yyyy-MM-dd HH:mm:ss";
			SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
			String strDateFormat1 = "yyyyMMdd hhmmss";
			SimpleDateFormat sDateFormat1 = new SimpleDateFormat(strDateFormat1);
			
			logger.verbose("current date : " + yfcDate);
			
			String fromRange = yfcDate.toString().substring(0, 8) + " 000000";
			fromRange = sDateFormat.format(sDateFormat1.parse(fromRange));
			String toRange = yfcDate.toString().substring(0, 8) + " 235959";
			toRange = sDateFormat.format(sDateFormat1.parse(toRange));
			
			logger.verbose("Date Range from : " + fromRange + " and to : " + toRange);
			
			String selectForEODFinalInvoice = "SELECT MAX(INVOICE_NO) as INVOICE_NO FROM " + TABLENAME
					+ " WHERE " + INVOICE_TYPE + " <> 'PRO_FORMA'" + "AND " + INVOICED_DATE + " >= {ts '" + fromRange + "'}  AND "
					+ INVOICED_DATE + " <= {ts '" + toRange + "'} and status='01'";
			
			stmt1 = ctxt.getConnection().createStatement();
			rs = stmt1.executeQuery(selectForEODFinalInvoice);
			if (rs.next()) {
				logger.verbose("Got the Results");
				maxInvoiceNo = rs.getString(INVOICE_NO);
			}
			
			Document input = XMLUtil.getDocument("<OrderInvoice InvoiceNo=\""+maxInvoiceNo+"\"/>") ;
			env.setApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_LIST,
					XMLUtil.getDocument("<OrderInvoiceList><OrderInvoice OrderInvoiceKey=\"\" DateInvoiced=\"\" InvoiceNo=\"\"></OrderInvoice></OrderInvoiceList>"));
			
			logger.verbose("input to getOrderInvoiceList for last invoice is :"+ XMLUtil.getXMLString(input));
			Document output = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_INVOICE_LIST, input);
			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_LIST);
			logger.verbose("output of getOrderInvoiceList :"+ XMLUtil.getXMLString(output));
			
			Element orderInvoice = (Element)output.getDocumentElement().getElementsByTagName("OrderInvoice").item(0); 
			invoiceKeyModifyDate = orderInvoice.getAttribute("DateInvoiced");
			eodInvoiceKey = orderInvoice.getAttribute("InvoiceKey");
			
			this.executeCloseString(env, maxInvoiceNo, invoiceKeyModifyDate, eodInvoiceKey);	
		}
		
		return inputDocument;
	}
	
	
	private static String calculateDate() {
		YFCDate date = new YFCDate();
		String invoiceNo = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("HHmm");
		SimpleDateFormat invoiceDate = new SimpleDateFormat("yyyyMMdd");
		String hourMin = dateFormat.format(date);
		logger.verbose(hourMin);
		if (hourMin.startsWith("00")) {
			date.changeDate(-1);
		}
		
		invoiceNo = invoiceDate.format(date);
		
		return invoiceNo;
	}
	
	
	private boolean getACADFinalInvoiceDetialList(YFSEnvironment env, Document inputDocument) throws Exception {
		Document outDocument = AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_REPROCESS_EOD, inputDocument);
		Element acadInvoiceDetail = (Element) outDocument.getElementsByTagName("ACAD_FINAL_INVOICE_DETAIL").item(0);
		if (!acadInvoiceDetail.equals(null)) {
			String processed = acadInvoiceDetail.getAttribute("Processed");
			if (processed.equals("N")) {
				return true;
			}
		}
		return false;
	}
	
	
	public void executeCloseString(YFSEnvironment env, String currentInvoiceNo, String dateInvoiced, String invoiceKey) {
		Document closeStringDoc = null;
		String regNoStr, transNoStr = null;
		String eodTransDateTime = dateInvoiced != null ? dateInvoiced : "";
		
		try {
			if (currentInvoiceNo != null & currentInvoiceNo.length() > 0) {
				regNoStr = currentInvoiceNo.substring(currentInvoiceNo.length() - 8, currentInvoiceNo.length() - 4);
				logger.verbose("TerminalNo" + regNoStr);
				transNoStr = currentInvoiceNo.substring(currentInvoiceNo.length() - 4, currentInvoiceNo.length());
				logger.verbose("TransactionNo" + transNoStr);
				String eodDate = dateInvoiced != null ? dbDateFormatter.format(invoiceDateTimeFormatter.parse(dateInvoiced.substring(0, 10))) : "";
				/*DateFormat dbDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
				 String currDateTime = new YFCDate(new Date()).toString();
				 DateFormat dbInvoiceDateTimeFormatter = new SimpleDateFormat("yyyyMMddHHmmss");*/
				closeStringDoc = XMLUtil.getDocument("<CloseString EodTransDateTime='" + eodTransDateTime + "' EodTransNo='" + transNoStr
						+ "' StoreNo=\"00005\" RegisterNo='" + regNoStr + "' EodTransKey='" + invoiceKey.trim() + "' EodTransDate='" + eodDate
						+ "' EodInvoiceNo='" + currentInvoiceNo + "'/>");
				logger.verbose("CloseString XML is " + XMLUtil.getXMLString(closeStringDoc));
			}
			
			if (closeStringDoc != null) {
				// Publish the invoice to service -
				// AcademyPublishInvoiceDetailsService
				AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_PUBLISH_INVOICE_DETAILS, closeStringDoc);
				
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
		
	}
}
