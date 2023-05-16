//package declaration
package com.academy.ecommerce.server;

//import statements

//java util import statements
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//academy util import statements
import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;

//yantra import statements
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * Description: Class AcademySFSCustomPurgeAgent has two methods getJobs and
 * executeJob. The getJobs method returns the list of records to be purged.
 * Executes the job in the input XML as a service for each of the document 
 * as passed by the getJobs service in a list
 * 
 * @throws Exception
 */
public class AcademySFSCustomPurgeAgent extends YCPBaseAgent
{

	/**
	 * Retrieves list of records to be purged from the table
	 * ACA_STORE_PRINT_MSG table based on PurgeData="Y". Stores each record
	 * into array list
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 * @param inXML
	 *            Input Document.
	 * @param lastMessage 
	 * 			  last document returned in the previous call to getJobs         
	 * @return List
	 */
	public List getJobs(YFSEnvironment env, Document inXML,Document lastMessage)throws Exception{

		//Declaring list variable
		List StorePrintMsgList = new ArrayList();

		//START: Preparing input document for service GetDataService 

		//XML structure for service GetDataService
		//<ACADStorePrintMsg PurgeData="Y"/>
		//END: Preparing input document for service GetDataService

		//Create Document
		Document getStorePrintMsgListInputDoc=XMLUtil.createDocument(AcademyConstants.ELE_STORE_PRINT_MSG);
		//Creating root node
		Element storePrintMsg = getStorePrintMsgListInputDoc.getDocumentElement();
		//Set the attribute PurgeData
		storePrintMsg.setAttribute(AcademyConstants.ATTR_PURGE_DATA, AcademyConstants.ATTR_Y);
		//CInvoke service GetDataService
		Document getStorePrintMsgListOutputDoc=AcademyUtil.invokeService(env,AcademyConstants.SERV_GET_DATA, getStorePrintMsgListInputDoc);

		//Check if lastMessage is null
		if(lastMessage==null){
			// if yes, add the response document into the array list
			StorePrintMsgList.add(getStorePrintMsgListOutputDoc);
		}

		return StorePrintMsgList;
	}

	/**
	 * Executes the job in the input XML as a service for each of the element
	 * StorePrintMsg as passed by the getJobs service in a list.
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 * @param getStorePrintMsgListOutputDoc
	 *            Input Document.
	 * @return void
	 */
	public void executeJob(YFSEnvironment env, Document getACADStorePrintMsgListOutputDoc) throws Exception{

		//Declaring String variables
		String strCodeType = "PURGE";
		String strOrganizationCode  = AcademyConstants.PRIMARY_ENTERPRISE;

		//calling method getCommonCodeList to get the value of common code value type PURGE
		Document CommonCodeDoc = AcademyCommonCode.getCommonCodeList(env, strCodeType, strOrganizationCode);
		NodeList nl=CommonCodeDoc.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
		Element ele=(Element)nl.item(0); 
		String strPurgeNo=ele.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);

		//Converting string to integer value
		int iPurgeNo=Integer.parseInt(strPurgeNo);

		//Check if input document is null
		if(getACADStorePrintMsgListOutputDoc!=null)
		{
			//Fetch the nodelist ACADStorePrintMsg
			NodeList nlElement=getACADStorePrintMsgListOutputDoc.getElementsByTagName(AcademyConstants.ELE_STORE_PRINT_MSG);
			//Looping through nodelist ACADStorePrintMsg 
			for(int i=0;i<nlElement.getLength();i++){
				//Fetching teh element ACADStorePrintMsg
				Element eleAcadStorePrintMsg=(Element)nlElement.item(i);
				//fetching the value of attribute Modifyts
				String sModifyts=eleAcadStorePrintMsg.getAttribute(AcademyConstants.ATTR_MODIFY_TS);
				//Converting string value to date
				SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
				Date dModifyts = sdf.parse( sModifyts );

				//fetching System Date
				Date date=new Date();
				Calendar cal = Calendar.getInstance();
				cal.setTime(date) ; 
				//Subtracting retention days from system date  
				cal.add(Calendar.DATE, -iPurgeNo);
				String strDiffDate=sdf.format(cal.getTime());
				//Converting string to date
				Date dDiffDate= sdf.parse(strDiffDate);

				//Check if Modifyts date is lesser than system date- retention days
				if (dModifyts.getTime()<dDiffDate.getTime()){
					//If yes, Convert element to document 
					Document deleteStorePrintMsgInputDoc=XMLUtil.getDocumentForElement(eleAcadStorePrintMsg);
					//Invoke service GetDataService
					AcademyUtil.invokeService(env,AcademyConstants.SERV_EXECUTE_DATA, deleteStorePrintMsgInputDoc);

				}
			}
		}
	}
}
