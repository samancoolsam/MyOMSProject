/**
 * 
 */
package com.academy.ecommerce.sterling.invoice.api;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.ResourceUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dblayer.YFCContext;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @author nsrivastava
 * 
 */
public class AcademyLockRecordForInvoiceNoAPI implements YIFCustomApi {

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyLockRecordForInvoiceNoAPI.class);

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}

	public Document lockRecordForInvoiceNo(YFSEnvironment envNew, Document inDoc) {
		log
				.beginTimer(" begin of AcademyLockRecordForInvoiceNoAPI ->lockRecordForInvoiceNo Api");
		log.verbose("******* input doc ********" + XMLUtil.getXMLString(inDoc));
		Document docCurrentInvoiceSeqNo = null;
		
		try {
			String strDateFormat = AcademyConstants.STR_SIMPLE_DATE_PATTERN;
			SimpleDateFormat sDateFormat = new SimpleDateFormat(
					strDateFormat);
			Calendar cal = Calendar.getInstance();
			String sInvoiceDate = inDoc.getDocumentElement().getAttribute("InvoicedDate");
			Document docInvoiceSeqNo = null;
			AcademyGetInvoiceNoUEImpl  invoiceUEImpl = new AcademyGetInvoiceNoUEImpl();
			
			// Step 1
			if(!ResourceUtil.getHasDummyInvoiceNoSeq()){
				// Avoid to check Dummy Record for every record.
				// This block executes only once
				Document docInputForDummyRow = XMLUtil.createDocument(AcademyConstants.DOC_EXTN_INVOICE_SEQNO);
				docInputForDummyRow.getDocumentElement().setAttribute(AcademyConstants.ATTR_SEQ_KEY, AcademyConstants.STR_INVOICE_SEQ_KEY);
				Document docOutputOfDummyRow = AcademyUtil.invokeService(envNew,AcademyConstants.SERVICE_GETEXTN_INVOICESEQNO,docInputForDummyRow);
				if(docInputForDummyRow == null || (docInputForDummyRow != null&& !docOutputOfDummyRow.getDocumentElement().hasChildNodes())){
					Document docDummyRecord = updateValuesForInvoiceSequenceAndTransaction(envNew, sInvoiceDate, AcademyConstants.STR_INVOICE_SEQ_KEY, 
								AcademyConstants.STR_INVOICE_SEQ_INITIAL_VALUE, AcademyConstants.STR_REGISTER_SEQ_INITIAL_VALUE,AcademyConstants.STR_ACTION_CREATE);
				}
				ResourceUtil.setHasDummyInvoiceNoSeq(true);
			}
			
			// Step 2
			Statement stmt = null;
			ResultSet rs = null;
			Document docGetInvoiceListInput = null;
			Element eleExtnInvoiceSeqNoList = null;
			Element eleExtnInvoiceSeqNo = null;
			String strTransNo = null;
			String strRegNo = null;
			
			String tableName = "EXTN_INVOICENO_SEQ";				
			String selectForUpdate = "SELECT "+tableName+".* FROM "+tableName+" "+tableName+" WHERE "+tableName+".SEQ_KEY='"+AcademyConstants.STR_INVOICE_SEQ_KEY+"' FOR UPDATE";
			log.beginTimer("..... Locking dummy record started .....");
			log.verbose("Set transcation isolation to 'TRANSACTION_REPEATABLE_READ'");
			YFCContext ctxt = (YFCContext)envNew;
			ctxt.getConnection().setTransactionIsolation(4);
			stmt = ctxt.getConnection().createStatement();
			log.verbose("Create a lock for dummy record by executing the query \n"+selectForUpdate);
			rs = stmt.executeQuery(selectForUpdate);
			if(rs.next()){
				log.verbose("Found record for update");						
			}
			log.verbose("close the stmt and rs");
			rs.close();
			stmt.close();
			log.verbose("Set transcation isolation to 'TRANSACTION_READ_COMMITTED'");
			ctxt.getConnection().setTransactionIsolation(2);
			log.verbose("generated lock successfully");
			
			// Start update/create for actual register and transaction numbers
			docGetInvoiceListInput = XMLUtil
					.createDocument(AcademyConstants.DOC_EXTN_INVOICE_SEQNO);
			docGetInvoiceListInput.getDocumentElement().setAttribute(
					"ToSeqDate", sInvoiceDate);
			docGetInvoiceListInput.getDocumentElement().setAttribute(
					"FromSeqDate", sInvoiceDate);
			docGetInvoiceListInput.getDocumentElement().setAttribute(
					"SeqDateQryType", "DATERANGE");

			docInvoiceSeqNo = AcademyUtil.invokeService(envNew,
					AcademyConstants.SERVICE_GETEXTN_INVOICESEQNO,
					docGetInvoiceListInput);
			log.verbose("GetExtnInvoiceNo API output: "
					+ XMLUtil.getXMLString(docInvoiceSeqNo));
			eleExtnInvoiceSeqNoList = docInvoiceSeqNo.getDocumentElement();
			if (eleExtnInvoiceSeqNoList.hasChildNodes()) {
				eleExtnInvoiceSeqNo = (Element) eleExtnInvoiceSeqNoList
						.getElementsByTagName(
								AcademyConstants.DOC_EXTN_INVOICE_SEQNO)
						.item(0);
				
				/*
				 * strLastModTime = eleExtnInvoiceSeqNo
				 * .getAttribute(AcademyConstants.ATTR_MODIFY_TS);
				 * bDateRollOver = verifyForDateRollOver(strLastModTime); if
				 * (bDateRollOver) { log.verbose(" ######## Date Roll Over
				 * ########"); docCurrentInvoiceSeqNo =
				 * updateValuesForInvoiceSequenceAndTransaction(env,
				 * AcademyConstants.STR_INVOICE_SEQ_INITIAL_VALUE,
				 * AcademyConstants.STR_REGISTER_SEQ_INITIAL_VALUE,
				 * AcademyConstants.STR_ACTION_MODIFY);
				 * prepareInvoiceNumber(env, docCurrentInvoiceSeqNo);
				 *  } else {}
				 */
				String sSeqDate = eleExtnInvoiceSeqNo.getAttribute("SeqDate");
				String sequenceKey = eleExtnInvoiceSeqNo.getAttribute("SequenceKey");					
				strTransNo = eleExtnInvoiceSeqNo.getAttribute(AcademyConstants.ATTR_CURR_TRANS_NO);
				strRegNo = eleExtnInvoiceSeqNo.getAttribute(AcademyConstants.ATTR_CURR_REG_NO);
				if (AcademyConstants.STR_MAX_TRANS_SEQ_VALUE
						.equals(strTransNo)) {
					log
							.verbose("*********   Transaciton number reached max value - "
									+ strTransNo);
					strTransNo = AcademyConstants.STR_INVOICE_SEQ_INITIAL_VALUE;

					// If register number reaches to 0299, it should be
					// reset to 0201
					if (AcademyConstants.STR_MAX_REGISTER_SEQ_VALUE
							.equals(strRegNo)) {
						log
								.verbose("*********   Register number reached max value - "
										+ strRegNo);
						strRegNo = AcademyConstants.STR_REGISTER_SEQ_INITIAL_VALUE;
					} else {
						strRegNo = String.valueOf(Integer
								.parseInt(strRegNo) + 1);
					}
				} else {
					strTransNo = String.valueOf((Integer
							.parseInt(strTransNo) + 1));
				}
				log.verbose("Seq Date : " + sSeqDate);
				log.verbose("Transaction number : " + strTransNo);
				log.verbose("Register number : " + strRegNo);
				docCurrentInvoiceSeqNo = updateValuesForInvoiceSequenceAndTransaction(
						envNew, sSeqDate, sequenceKey, strTransNo, strRegNo,
						AcademyConstants.STR_ACTION_MODIFY);
				
				log
						.verbose("############ Output after updating docCurrentInvoiceSeqNo ### "
								+ XMLUtil
										.getXMLString(docCurrentInvoiceSeqNo));
				// }
			} else {
				docCurrentInvoiceSeqNo = updateValuesForInvoiceSequenceAndTransaction(
						envNew, sInvoiceDate, "",
						AcademyConstants.STR_INVOICE_SEQ_INITIAL_VALUE,
						AcademyConstants.STR_REGISTER_SEQ_INITIAL_VALUE,
						AcademyConstants.STR_ACTION_CREATE);
				log
				.verbose("############ Output after creating docCurrentInvoiceSeqNo ### "
						+ XMLUtil
								.getXMLString(docCurrentInvoiceSeqNo));
			}	
			log.verbose("Release the lock from dummy record.....");
			// Release the lock
			stmt = ctxt.getConnection().createStatement();
			stmt.execute("SET CURRENT LOCK TIMEOUT NULL");
			stmt.close();
			log.endTimer("Lock released successfully");			
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		log.endTimer(" Ending of AcademyLockRecordForInvoiceNoAPI ->lockRecordForInvoiceNo Api");				
		return docCurrentInvoiceSeqNo;
	}
		
	
	private Document updateValuesForInvoiceSequenceAndTransaction(
			YFSEnvironment env, String strInvoiceDate, String sequenceKey,
			String strTransNo, String strRegNo, String action) {
		Document docCreateInvoiceSeqNoInput = null;
		Document docCreateInvoiceSeqNoOutput = null;

		/*YIFApi yifApi;
		YFSEnvironment envNew;*/

		try {

			/*yifApi = YIFClientFactory.getInstance().getLocalApi();
			Document docEnv = XMLUtil.createDocument(AcademyConstants.ELE_ENV);
			docEnv.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_USR_ID, env.getUserId());
			docEnv.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_PROG_ID, env.getProgId());
			envNew = yifApi.createEnvironment(docEnv);*/

			docCreateInvoiceSeqNoInput = XMLUtil
					.createDocument(AcademyConstants.DOC_EXTN_INVOICE_SEQNO);
			docCreateInvoiceSeqNoInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_CURR_REG_NO, strRegNo);
			docCreateInvoiceSeqNoInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_CURR_TRANS_NO, strTransNo);
			//docCreateInvoiceSeqNoInput.getDocumentElement().setAttribute(
			//		AcademyConstants.ATTR_SHIP_DATE, this.strInvoiceShipDate);
			if(!YFCObject.isVoid(sequenceKey))
				docCreateInvoiceSeqNoInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_SEQ_KEY, sequenceKey);
			log
					.verbose("#############docCreateInvoiceSeqNoInput for update #### "
							+ XMLUtil.getXMLString(docCreateInvoiceSeqNoInput));
			if ((AcademyConstants.STR_ACTION_CREATE).equals(action)) {
				docCreateInvoiceSeqNoInput.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_SEQ_DATE, strInvoiceDate);
				docCreateInvoiceSeqNoOutput = AcademyUtil.invokeService(env,
						AcademyConstants.SERVICE_CREATEEXTN_INVOICESEQNO,
						docCreateInvoiceSeqNoInput);
			} else {				
				docCreateInvoiceSeqNoOutput = AcademyUtil.invokeService(env,
						AcademyConstants.SERVICE_CHANGEEXTN_INVOICESEQNO,
						docCreateInvoiceSeqNoInput);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

		return docCreateInvoiceSeqNoOutput;
	}


	
}
