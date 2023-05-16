//package declaration
package com.academy.ecommerce.server;
//java util import statements
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
//academy util import statements
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
//yantra import statements
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * Description: Class AcademySFSPurgeForRescheduleStore has two methods getJobs
 * and executeJob. The getJobs method returns the list of records that has
 * CancelStatus=N and Modifyts<RetentionDays. 
 * The executeJob method will delete
 * each of the document as passed by the getJobs service in a list
 * 
 * @throws Exception
 */
public class AcademySFSPurgeForRescheduleStore extends YCPBaseAgent
{

	public List getJobs(YFSEnvironment env, Document inXML) throws Exception
	{
		//Declare List variable
		List aRescheduleStoretList = new ArrayList();
		List aElementsList = null;
		//Declare Document variable
		Document docInXMLacaRescheduleStore=null;
		Document docOutXMLacaRescheduleStore=null;
		Document docChildRecord=null;
		//Declare Element variable
		Element eleMessageXml=null;
		Element eleRoot=null;
		Element eleRootOutXML =null;
		//Declare String variable
		String strDiffDate="";
		//Declare integer varibale
		int iPurgeNo=0;
		
		//Fetch the root element of the input xml
		eleMessageXml = inXML.getDocumentElement();
		//Start: Create document for getACARescheduleStore
		/*
		 * <ACARescheduleStore MaximumRecords="" CancelStatus="N" ModifytsQryType="LT" Modifyts="2012-06-26T00:53:15-04:00"/>
		 */
		// Creating root node
		docInXMLacaRescheduleStore = XMLUtil.createDocument(AcademyConstants.ELE_ACS_RESCHEDULE_STORE);		
		eleRoot = docInXMLacaRescheduleStore.getDocumentElement();
		// Set the attribute Value of CancelStatus
		eleRoot.setAttribute(AcademyConstants.ATTR_CANCEL_STATUS, AcademyConstants.STR_NO);
		// Set the attribute Value of ModifytsQryType
		eleRoot.setAttribute(AcademyConstants.MODIFYTS_QRY_TYPE, AcademyConstants.LT_QRY_TYPE);
		// Set the attribute Value of MaximumRecords
		eleRoot.setAttribute(AcademyConstants.ATTR_MAX_RECORD, eleMessageXml.getAttribute(AcademyConstants.PURGE_MAX_RECORDS));
		//Fetch the value of RETENTION_DAYS
		iPurgeNo = Integer.parseInt(eleMessageXml.getAttribute(AcademyConstants.PURGE_RETENTION_DAYS));
		//Set the Sterling Date Format
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		//fetching System Date
		Date date = new Date();
		//Setting the calender instance
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		// Subtracting retention days from system date
		cal.add(Calendar.DATE, -iPurgeNo);
		//Set the date format according th sterling date
		strDiffDate = sdf.format(cal.getTime());
		//Map the calculated date into attribute Modifyts
		eleRoot.setAttribute(AcademyConstants.ATTR_MODIFY_TS, strDiffDate);
		//End: Create document for getACARescheduleStore
		//Invoke service to the fetch the matching record from ACA_RESCHEDULE_STORE table
		docOutXMLacaRescheduleStore = AcademyUtil.invokeService(env, AcademyConstants.SERV_CUSTOM_GET, docInXMLacaRescheduleStore);
		//Fetch the root element of the output
		eleRootOutXML = docOutXMLacaRescheduleStore.getDocumentElement();
		// Fetch the child element
		aElementsList = XMLUtil.getSubNodeList(eleRootOutXML, AcademyConstants.ELE_ACS_RESCHEDULE_STORE);
		// Iterate through the aElementsList
		for (Iterator itEleRecord = aElementsList.iterator(); itEleRecord.hasNext();)
		{
			//Fetch the child record
			docChildRecord = XMLUtil.getDocumentForElement((Element) itEleRecord.next());
			//Add into the arraylist
			aRescheduleStoretList.add(docChildRecord);
		}
		//Return the arraylist
		return aRescheduleStoretList;
	}

	public void executeJob(YFSEnvironment env, Document docinXML) throws Exception
	{
		//Invoke service to delete the record from ACA_RESCHEDULE_STORE table
		AcademyUtil.invokeService(env, AcademyConstants.SERV_CUSTOM_DELETE, docinXML);

	}
}
