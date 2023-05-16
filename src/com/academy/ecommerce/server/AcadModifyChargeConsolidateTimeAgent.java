package com.academy.ecommerce.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.dblayer.YFCContext;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;

public class AcadModifyChargeConsolidateTimeAgent extends YCPBaseAgent {
	private static YFCLogCategory log = YFCLogCategory.instance(AcadModifyChargeConsolidateTimeAgent.class);


/*public List getJobs(YFSEnvironment env, Document inXML, Document lastMessage)
	throws Exception {

List aList = new ArrayList();

return aList;

}*/


public void executeJob(YFSEnvironment env, Document inDoc) throws Exception {
	
	log.verbose("Input to executeJob()  : " + XMLUtil.getXMLString(inDoc) );
	//OMNI-90680 TECHDEBT-SonarQube Code Cleanup-Finally clause - START
	YFCContext ctxt = (YFCContext) env;
	Statement stmt1 = null;
	//OMNI-90680 TECHDEBT-SonarQube Code Cleanup-Finally clause - END
	try{	
		
		//get consolidate window from common code
		String strConsolidationwindow = getConsolidateWindow(env);
		log.verbose("Charge Consolidation window:\t"+strConsolidationwindow);
		
		//KER-12036 : Payment Migration Changes to support new Payment Type
		String strUpdateQuery = "UPDATE YFS_PAYMENT_TYPE set CHARGE_CONSOL_ALLOWED='Y', MIN_WAIT_TIME_FOR_CONSOL='"+strConsolidationwindow+"' where payment_type in ('CREDIT_CARD','Credit_Card') and Organization_code='Academy_Direct'";
		log.verbose("Update Query:\t"+strUpdateQuery);

		Connection connection = ctxt.getConnection();
		stmt1 = connection.createStatement();

		int iResponseValue = stmt1.executeUpdate(strUpdateQuery);
		log.verbose("Got executeUpdate Results: value\t"+iResponseValue);

		if(iResponseValue>0){	
			//clear catch for YFS_PAYMENT_TYPE table
			clearCache(env);
		}
		else{
			sendEmailNotification(env);
		}

	}
	catch (Exception e) {
		sendEmailNotification(env);
		throw e;
	}
	//OMNI-90680 TECHDEBT-SonarQube Code Cleanup-Finally clause - START
	finally {
		if (stmt1 != null)
			stmt1.close();
		stmt1 = null;
	}//OMNI-90680 TECHDEBT-SonarQube Code Cleanup-Finally clause - END
}


/**
 * @param env
 * @param iCurrentHour
 * @return
 * @throws ParserConfigurationException
 * @throws SAXException
 * @throws IOException
 * @throws Exception
 */
private String getConsolidateWindow(YFSEnvironment env) throws Exception {
	
	YFCDate yfcDate = new YFCDate(new Date());
	int iCurrentHour = yfcDate.getHours();
	//String strCurrentHour = Integer.toString(iCurrentHour);
	log.verbose("current date : " + yfcDate);
	log.verbose("current Hours : " + iCurrentHour);
	
	String strGetCommonCodeListInput = "<CommonCode CodeValue='"+iCurrentHour+"' CodeType='CHARGE_CONSOLIDATE'/>";
	Document docGetCommonCodeListInput = XMLUtil.getDocument(strGetCommonCodeListInput);
	log.verbose("docGetCommonCodeListInput \n"+XMLUtil.getXMLString(docGetCommonCodeListInput));
	Document docGetCommonCodeListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST, docGetCommonCodeListInput);	
	log.verbose("docGetCommonCodeListOutput \n"+XMLUtil.getXMLString(docGetCommonCodeListOutput));

	String strConsolidationwindow = XPathUtil.getString(docGetCommonCodeListOutput, "/CommonCodeList/CommonCode/" + "@CodeShortDescription");
	return strConsolidationwindow;
}


/**Send failure notification email
 * @param env
 * @throws DOMException
 * @throws Exception
 */
private void sendEmailNotification(YFSEnvironment env) throws Exception {
	Document  docPrepareInputForEmail= XMLUtil.createDocument("Email");
	Element elePrepareInputForEmai = docPrepareInputForEmail.getDocumentElement();
	SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Date now = new Date();
	String strDate = sdfDate.format(now);
	
	elePrepareInputForEmai.setAttribute("CurrentTime", strDate);
	elePrepareInputForEmai.setAttribute("EmailSubject", "UPDATE FAIL : Charge consolidation window not changed at "+strDate);
	log.verbose("docPrepareInputForEmail:\n"+docPrepareInputForEmail);
	
	AcademyUtil.invokeService(env, "AcademyChargeCondolidateStatusEmail", docPrepareInputForEmail);
}


/**
 * @param env
 * @throws ParserConfigurationException
 * @throws SAXException
 * @throws IOException
 * @throws Exception
 */
private void clearCache(YFSEnvironment env) throws ParserConfigurationException, SAXException, IOException, Exception {
	String strModifyChcheInput = "<CachedGroups> " +
			"<CachedGroup Name='Database' >" +
			"<CachedObject Action='CLEAR'  Class='com.yantra.shared.dbclasses.YFS_Payment_TypeDBCacheHome'/>" +
			"</CachedGroup>" +
			"</CachedGroups>";
	Document docModifyChcheInput = XMLUtil.getDocument(strModifyChcheInput);
	log.verbose("docModifyChcheInput \n"+XMLUtil.getXMLString(docModifyChcheInput));
	AcademyUtil.invokeAPI(env, AcademyConstants.API_MODIFY_CACHE, docModifyChcheInput);
}
}