package com.academy.ecommerce.server;


import java.text.SimpleDateFormat;
import java.util.Date;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;

/**Added for STL-1346
 * This class is used to insert the EOD message into the custom table Acad_fin_eod
 *  
 * @author Netai Dey
 * 
 */
public class ACADPublishEODAgent extends YCPBaseAgent {
	
	private final Logger logger = Logger.getLogger(ACADPublishEODAgent.class.getName());
	


	/**
	 * Method to insert the EOD message into Acad_fin_eod,
	 * In case of any error, send email.
	 * 
	 * @param env
	 *            YFSEnvironment
	 * @param input
	 *            Document
	 */
	@Override
	public void executeJob(YFSEnvironment env, Document inputDoc) throws Exception {
		try {
			logger.verbose("Entering ACADPublishEODAgent : executeJob ");
			logger.verbose("input to ACADPublishEODAgent is : "	+ XMLUtil.getXMLString(inputDoc));
			
			Document output = null;
			Document AcademyRecordFinTranEODServiceInputDoc = null;
			Element eleAcademyRecordFinTranEODServiceInput = null;
			
			YFCDate yfcDate = new YFCDate(new Date());	
			SimpleDateFormat sDateFormat = new SimpleDateFormat(AcademyConstants.DATE_YYYYMMDD_FORMAT);
			SimpleDateFormat sDateTimeFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			
			AcademyRecordFinTranEODServiceInputDoc = XMLUtil.createDocument(AcademyConstants.ELE_ACAD_FIN_TRAN_EOD);
			eleAcademyRecordFinTranEODServiceInput=AcademyRecordFinTranEODServiceInputDoc.getDocumentElement();
			eleAcademyRecordFinTranEODServiceInput.setAttribute(AcademyConstants.ATTR_ACADEMY_EOD_TRAN_DATE, sDateFormat.format(yfcDate));
			eleAcademyRecordFinTranEODServiceInput.setAttribute(AcademyConstants.ATTR_ACADEMY_EOD_TRAN_DATE_TIME, sDateTimeFormat.format(yfcDate));
			eleAcademyRecordFinTranEODServiceInput.setAttribute(AcademyConstants.ATTR_ACADEMY_EOD_KEY,AcademyConstants.STR_INVOICE_SEQ_INITIAL_VALUE);
			
			logger.verbose("input to AcademyRecordFinTranEODService :"+ XMLUtil.getXMLString(AcademyRecordFinTranEODServiceInputDoc));
			output = AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_RECORD_ACAD_FIN_EOD, AcademyRecordFinTranEODServiceInputDoc);
			logger.verbose("output of AcademyRecordFinTranEODService :"+ XMLUtil.getXMLString(output));
		} catch (Exception e) {
			logger.verbose("Error while invoking AcademyRecordFinTranEODService. Sending EOD failure email");
			logger.verbose(e.getMessage());
			AcademyUtil.invokeService(env, AcademyConstants.RAISE_ALERT_WHEN_EOD_MESSAGE_NOT_PUBLISHED, inputDoc);
		} 
		logger.verbose("Exiting ACADFinalInvoiceAgent : executeJob ");
	} 

}


