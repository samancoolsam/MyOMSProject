package com.academy.ecommerce.server;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.dblayer.YFCContext;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class to identify the final invoice number for current date, and update
 * that into the custom table ACAD_FINAL_INVOICE_DETAIL.
 * 
 * @author mkandasamy-tw
 * 
 */
public class ACADFinalInvoiceAgent extends YCPBaseAgent {

	private final Logger logger = Logger.getLogger(ACADFinalInvoiceAgent.class
			.getName());
	DateFormat dbDateFormatter = new SimpleDateFormat("yyyyMMdd");
	DateFormat invoiceDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd");
	static String previousLastOrderInvoiceKey = "";
	static final String INVOICE_KEY="ORDER_INVOICE_KEY";
	static final String INVOICE_NO="INVOICE_NO";
	static final String INVOICE_TYPE="INVOICE_TYPE";
	static final String INVOICED_DATE="CREATETS";
	static final String EXTN_PROCESSED="EXTN_PROCESSED";
	static final String TABLENAME = "YFS_ORDER_INVOICE";

	/**
	 * Method to get the final invoice number for current date.
	 * 
	 * @param env
	 *            YFSEnvironment
	 * @param inXML
	 *            Document
	 * @return outputList List
	 */
	@Override
	public List<Document> getJobs(YFSEnvironment env, Document inXML) {
		List<Document> outputList = new ArrayList<Document>();
		YFCDate yfcDate = new YFCDate(new Date());
		
		try {
			
			/**
			 * *******  While OMS Regression Testing - Cycle 3, observed below issues
			 * 1. The API 'getOrderInvoiceList' fetches the list of invoice of the day by ordering in OrderHeaderKey and InvoiceNo combination
			 * 2. Default maxRecords is 5000 (according to yfs.properties definition) in input of 'getOrderInvoiceList' API and return first transactions instead of latest. 
			 * *******
			 * As per Academy Requirements, Academy can perform more than 10000 transaction in a day and Close String should always have last transaction of the day.
			 * Using the below SQL, fetch the last transaction of the day.
			 * 
			 * SELECT YFS_ORDER_INVOICE.* FROM YFS_ORDER_INVOICE YFS_ORDER_INVOICE WHERE YFS_ORDER_INVOICE.INVOICE_NO=( SELECT MAX(INVOICE_NO) FROM YFS_ORDER_INVOICE WHERE 
			 * ( ( YFS_ORDER_INVOICE.INVOICE_TYPE <> 'PRO_FORMA'  )  AND 
			 * (YFS_ORDER_INVOICE.CREATETS >= {ts 'yyyy-MM-dd 00:00:00'}  AND YFS_ORDER_INVOICE.CREATETS <= {ts 'yyyy-MM-dd 23:59:59'}  )))
			 */
			logger.verbose("Entering ACADFinalInvoiceAgent : getJobs ");
			String strDateFormat = "yyyy-MM-dd HH:mm:ss";
			SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
			String strDateFormat1 = "yyyyMMdd hhmmss";
			SimpleDateFormat sDateFormat1 = new SimpleDateFormat(strDateFormat1);
			logger.verbose("current date : "+yfcDate);
			String fromRange = yfcDate.toString().substring(0, 8)+" 000000";			
			fromRange=sDateFormat.format(sDateFormat1.parse(fromRange));
			String toRange = yfcDate.toString().substring(0, 8)+" 235959";
			toRange=sDateFormat.format(sDateFormat1.parse(toRange));
			logger.verbose("Date Range from : "+fromRange+" and to : "+toRange);
			
			String selectForFinalInvoice = "SELECT "+TABLENAME+"."+INVOICE_KEY+" FROM "+TABLENAME+" "+TABLENAME+" WHERE "+TABLENAME+"."+INVOICE_NO+"=" +
					"( SELECT MAX(INVOICE_NO) FROM "+TABLENAME+" WHERE ( ("+ INVOICE_TYPE+" <> 'PRO_FORMA'  )  " +
							"AND ("+INVOICED_DATE+" >= {ts '"+fromRange+"'}  AND "+INVOICED_DATE+" <= {ts '"+toRange+"'}  )))";
			logger.verbose("Select statement with where clause : \n"+selectForFinalInvoice);
			String lastOrderInvoiceKey = null;
			String lastInvoiceNo = null;
			String extnProcessedFlage = null; 
			Statement stmt = null;
			ResultSet rs = null;
			boolean foundLastInvoice = false;
			try{				
				YFCContext ctxt = (YFCContext)env;
				stmt = ctxt.getConnection().createStatement();
				rs = stmt.executeQuery(selectForFinalInvoice);
				if(rs.next()){
					logger.verbose("Got the Results");
					lastOrderInvoiceKey = rs.getString(INVOICE_KEY);
					foundLastInvoice = true;
				}
			}catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
				logger.verbose("Exception while fetching last invoice for the day is : "+sqlEx);
			}finally{
				if(rs != null)
					rs.close();
				if(stmt != null)
					stmt.close();
				rs = null;
				stmt = null;
			}
			logger.verbose("Got the last Invoice for the day : "+foundLastInvoice);
			if(!foundLastInvoice)
				return null;
			
			logger.verbose("Last Invoice Details : \n InvoiceKey : "+lastOrderInvoiceKey);
			lastOrderInvoiceKey = lastOrderInvoiceKey.trim();			
			logger.verbose("PreviousLastOrderInvoiceKey is :"
					+ previousLastOrderInvoiceKey);

			if ((lastOrderInvoiceKey != null && lastOrderInvoiceKey.equals(previousLastOrderInvoiceKey)) || lastOrderInvoiceKey == null) {
				return null;
			}			

			/**
			 * Get the invoice detail of the last order invoice key
			 */
			Document input = null;
			Document output = null;
			env.setApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_LIST,
					XMLUtil.getDocument("<OrderInvoiceList><OrderInvoice><Extn/></OrderInvoice></OrderInvoiceList>"));
			
			input = XMLUtil.getDocument("<OrderInvoice OrderInvoiceKey=\""+lastOrderInvoiceKey+"\"/>") ;
			
			logger.verbose("input to getOrderInvoiceList for last invoice is :"+ XMLUtil.getXMLString(input));
			output = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_INVOICE_LIST, input);
			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_LIST);
			logger.verbose("output of getOrderInvoiceList :"+ XMLUtil.getXMLString(output));
			
			Element eleLastInvoice = (Element)output.getDocumentElement().getFirstChild();
			if(eleLastInvoice == null)
				return null;
			lastInvoiceNo = eleLastInvoice.getAttribute("InvoiceNo");
			Element eleExtn = (Element)eleLastInvoice.getFirstChild();
			if(eleExtn != null && eleExtn.hasAttribute("ExtnProcessed"))
				extnProcessedFlage =  eleExtn.getAttribute("ExtnProcessed");
			else
				extnProcessedFlage = "N";
			
			logger.verbose("********* \n Final invoice on " + yfcDate.toString()
					+ " is " + lastInvoiceNo
					+ " \n Posted to TLog/Matra " + extnProcessedFlage+"***********");
			logger.verbose("Final Invoiced Date is "+ eleLastInvoice.getAttribute("DateInvoiced"));
			if (AcademyConstants.STR_NO.equalsIgnoreCase(extnProcessedFlage)) {
				outputList
						.add(XMLUtil
								.getDocument("<ACAD_FINAL_INVOICE_DETAIL Processed = \"N\" InvoiceNo='"
										+ lastInvoiceNo
										+ "' DateInvoiced='"
										+ eleLastInvoice.getAttribute("DateInvoiced")+ "'/>"));
										
										//start of Fix for STL-456
										AcademyUtil.invokeService(env, AcademyConstants.RAISE_ALERT_WHEN_EOD_MESSAGE_NOT_PUBLISHED, inXML);
										AcademyUtil.invokeService(env, AcademyConstants.CALL_SERVICE_WHEN_EOD_MESSAGE_NOT_PUBLISHED, inXML);
										
										//End of Fix for STL-456
			} else {
				// Publish the invoice if it's already processed
				this.executeCloseString(env, lastInvoiceNo,
						eleLastInvoice.getAttribute("DateInvoiced"),
						lastOrderInvoiceKey);
				outputList
						.add(XMLUtil
								.getDocument("<ACAD_FINAL_INVOICE_DETAIL Processed = \"Y\" InvoiceNo='"
										+ lastInvoiceNo
										+ "' DateInvoiced='"
										+ eleLastInvoice.getAttribute("DateInvoiced")
										+ "'/>"));
			}
			previousLastOrderInvoiceKey = lastOrderInvoiceKey;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.verbose("Exiting ACADFinalInvoiceAgent : getJobs ");

		return outputList;
	}

	/**
	 * Method to execute the public close string
	 * 
	 * Sample invoice no - 20100802045757000500010003
	 * 
	 * @param env
	 *            YFSEnvironment
	 * @param currentInvoiceNo
	 *            String
	 * 
	 */
	public void executeCloseString(YFSEnvironment env, String currentInvoiceNo,
			String dateInvoiced, String invoiceKey) {
		Document closeStringDoc = null;
		String regNoStr, transNoStr = null;
		String eodTransDateTime = dateInvoiced != null ? dateInvoiced : "";

		try {
			if (currentInvoiceNo != null & currentInvoiceNo.length() > 0) {
				regNoStr = currentInvoiceNo.substring(
						currentInvoiceNo.length() - 8, currentInvoiceNo
								.length() - 4);
				logger.verbose("TerminalNo" + regNoStr);
				transNoStr = currentInvoiceNo.substring(currentInvoiceNo
						.length() - 4, currentInvoiceNo.length());
				logger.verbose("TransactionNo" + transNoStr);
				String eodDate = dateInvoiced != null ? dbDateFormatter
						.format(invoiceDateTimeFormatter.parse(dateInvoiced
								.substring(0, 10))) : "";
				/*DateFormat dbDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
				String currDateTime = new YFCDate(new Date()).toString();
				DateFormat dbInvoiceDateTimeFormatter = new SimpleDateFormat("yyyyMMddHHmmss");*/
				closeStringDoc = XMLUtil
						.getDocument("<CloseString EodTransDateTime='"
								+ eodTransDateTime + "' EodTransNo='"
								+ transNoStr
								+ "' StoreNo=\"00005\" RegisterNo='" + regNoStr
								+ "' EodTransKey='" + invoiceKey.trim()+"' EodTransDate='" + eodDate 
								+ "' EodInvoiceNo='"+currentInvoiceNo+"'/>");
				logger.verbose("CloseString XML is "
						+ XMLUtil.getXMLString(closeStringDoc));
			}

			if (closeStringDoc != null) {
				// Publish the invoice to service -
				// AcademyPublishInvoiceDetailsService
				AcademyUtil
						.invokeService(
								env,
								AcademyConstants.SERVICE_ACADEMY_PUBLISH_INVOICE_DETAILS,
								closeStringDoc);

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

	/**
	 * Method to insert the final invoice number in ACAD_FINAL_INVOICE_DETAIL,
	 * if it already exist, ignores it.
	 * 
	 * @param env
	 *            YFSEnvironment
	 * @param input
	 *            Document
	 */
	@Override
	public void executeJob(YFSEnvironment env, Document input) throws Exception {
		Document outputDoc = null;

		try {
			logger.verbose("Entering ACADFinalInvoiceAgent : executeJob ");
			logger.verbose("input to FinInvoiceDetail is : "
					+ XMLUtil.getXMLString(input));
			// Verify if this Invoice number is already exist
			outputDoc = AcademyUtil.invokeService(env,
					AcademyConstants.SERVICE_GET_FINAL_INVOICE_DETAIL_LIST,
					input);
			// Create the new entry for the final invoice number
			if (outputDoc.getElementsByTagName("ACAD_FINAL_INVOICE_DETAIL")
					.getLength() == 0) {
				AcademyUtil.invokeService(env,
						"AcademyCreateFinalInvoiceDetailSyncService", input);
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
		logger.verbose("Exiting ACADFinalInvoiceAgent : executeJob ");
	}
}
