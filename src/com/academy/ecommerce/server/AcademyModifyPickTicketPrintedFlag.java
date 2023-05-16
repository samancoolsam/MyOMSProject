package com.academy.ecommerce.server;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author <a href="mailto:Shruthi.Kenkarenarendrababu@cognizant.com">Shruthi KN</a>, Created on
 *         09/21/2015. This Class will pick the older shipments and flips the PickTicketPrinted attribute to N
 *         And PickticketNo to null
 */

public class AcademyModifyPickTicketPrintedFlag extends YCPBaseAgent {
	
	private final Logger logger = Logger.getLogger(AcademyModifyPickTicketPrintedFlag.class.getName());
	static int  iShipmentLength  = 0; //STL-1592 SFS - Printing Same Sheets
	private final String GET_SHIPMENT_LIST_OUTPUT_TEMPLATE = "<Shipments>" +
															 "<Shipment ShipmentKey=''/>" +
															 "</Shipments>";
	private final String STR_NO_OF_DAYS_TO_CREATETS = "NoOfDaysToCreatets";
	private final String STR_NO_OF_DAYS_FROM_CREATETS = "NoOfDaysFromCreatets";
	@Override
	public ArrayList<Document> getJobs(YFSEnvironment env, Document inXML) throws Exception {
		logger.verbose("Inside AcademyCancelShipmentAgent getJobs.The Input xml is : " + XMLUtil.getXMLString(inXML));
		
		Document docgetShipmentListOutput = null;
		Element eleCurrentShipment = null;
		String strNumRecordsToBuffer = null;
		ArrayList<Document> ShipmentList = new ArrayList<Document>();
		//START STL-1592 SFS - Printing Same Sheets
		Element eleInput=inXML.getDocumentElement();
		strNumRecordsToBuffer = eleInput.getAttribute(AcademyConstants.ATTR_NUM_RECORDS);
		int iNumRecordsToBuffer = Integer.parseInt(strNumRecordsToBuffer);
		logger.verbose("iNumRecordsToBuffer::"+iNumRecordsToBuffer);
		logger.verbose("iTotalNumOfRecs::"+iShipmentLength);
		logger.verbose("eleInput.hasChildNodes()::"+eleInput.hasChildNodes());

		if((eleInput.hasChildNodes() && (iNumRecordsToBuffer > iShipmentLength))){
			logger.verbose("Not invoking any API as all jobs are already processed ");
			return ShipmentList;
		}
		logger.verbose("iTotalNumOfRecs::"+iShipmentLength);
		//END STL-1592 SFS - Printing Same Sheets
		
		//prepare input and call getShipmentList API
		docgetShipmentListOutput = prepareInputAndCallGetShipmentList(env,inXML);
		//docgetShipmentListOutput.getElementsByTagName(AcademyConstants.ELE_SHIPMENT)
		NodeList nlShipmentList = docgetShipmentListOutput.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
		//int iShipmentLength = nlShipmentList.getLength();
		iShipmentLength = nlShipmentList.getLength(); //STL-1592 SFS - Printing Same Sheets
		for (int iShipmentCount = 0; iShipmentCount < iShipmentLength; iShipmentCount++) {
			
				eleCurrentShipment = (Element) nlShipmentList.item(iShipmentCount);
				ShipmentList.add(XMLUtil.getDocumentForElement(eleCurrentShipment));
			}		
		logger.verbose("Exiting AcademyCancelShipmentAgent : getJobs ");
		return ShipmentList;
	}


	/**
	 * @param env
	 * @return
	 * @throws Exception
	 * This method will prepare the input and invoke getShipmentList API
	 * 
	 * <Shipment FromCreatets="201-09-13T15:03:52" ToCreatets="2015-09-11T15:03:52" CreatetsQryType="BETWEEN" 
	 *	Status="1100.70.06.10" PickTicketPrinted="Y"/>
	 *
	 */
	private Document prepareInputAndCallGetShipmentList(YFSEnvironment env, Document inXML) throws Exception {
		// TODO Auto-generated method stub
		logger.verbose("Entering into method prepareInputAndCallGetShipmentList : getJobs ");
		Document docgetShipmentListOutput = null;
		Document docGetShipmentListInput = null;
		String strCurrentDate = "";
		String strToCreatetsDate = "";	
		String strFromCreatetsDate = "";
		String strNoOfDaysToCreatets = "";
		String strNoOfDaysFromCreatets = "";
		Element eleinXML = null;
		
		eleinXML = inXML.getDocumentElement();
		docGetShipmentListInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleRoot = docGetShipmentListInput.getDocumentElement();
	   
		strNoOfDaysToCreatets = eleinXML.getAttribute(STR_NO_OF_DAYS_TO_CREATETS);
		strNoOfDaysFromCreatets = eleinXML.getAttribute(STR_NO_OF_DAYS_FROM_CREATETS);
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		strCurrentDate = sdf.format(cal.getTime());		
		cal.add(Calendar.DATE, (-Integer.parseInt(strNoOfDaysToCreatets)));
		strToCreatetsDate = sdf.format(cal.getTime());
		
		cal.add(Calendar.DATE, (-Integer.parseInt(strNoOfDaysFromCreatets)));
		strFromCreatetsDate = sdf.format(cal.getTime());

	    logger.verbose("Current system Date is:" + strCurrentDate);
	    logger.verbose("To Createts Date is:" + strToCreatetsDate);
	    logger.verbose("From Createts Date is:" + strFromCreatetsDate);

	    eleRoot.setAttribute(AcademyConstants.ATTR_FROM_CREATETS, strFromCreatetsDate);
		eleRoot.setAttribute(AcademyConstants.ATTR_TO_CREATETS, strToCreatetsDate);
		eleRoot.setAttribute(AcademyConstants.ATTR_CREATETS_QRY_TYPE, AcademyConstants.BETWEEN);
		eleRoot.setAttribute(AcademyConstants.ATTR_STATUS, AcademyConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL);
		eleRoot.setAttribute(AcademyConstants.ATTR_IS_PICKTICKET_PRINTED, AcademyConstants.STR_YES);
		
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST,GET_SHIPMENT_LIST_OUTPUT_TEMPLATE);
		
		logger.verbose("Input xml for getShipmentList api:"+ com.academy.util.xml.XMLUtil.getXMLString(docGetShipmentListInput));
		docgetShipmentListOutput = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_SHIPMENT_LIST, docGetShipmentListInput);
		logger.verbose("Output xml for getShipmentList api:"+ com.academy.util.xml.XMLUtil.getXMLString(docgetShipmentListOutput));
		logger.verbose("End of method prepareInputAndCallGetShipmentList : getJobs ");
		return docgetShipmentListOutput;
	}



	@Override
	public void executeJob(YFSEnvironment env, Document input) throws Exception {
		// TODO Auto-generated method stub
		logger.verbose("Entering into AcademyCancelShipmentAgent executeJob");
		
		Document docChangeShipmentInput = null;
		Document docdocChangeShipmentOutput = null;
		Element eleShipment = null;
		Element eleRootElement = null;
		
		docChangeShipmentInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleRootElement = docChangeShipmentInput.getDocumentElement();
		
		eleShipment = input.getDocumentElement();
		String strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		eleRootElement.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY);
		eleRootElement.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
		eleRootElement.setAttribute(AcademyConstants.ATTR_IS_PICKTICKET_PRINTED, AcademyConstants.STR_NO);
		eleRootElement.setAttribute(AcademyConstants.ATTR_PICK_TICKET_NO, "");
		
		logger.verbose("Input xml for changeShipment api:"+ com.academy.util.xml.XMLUtil.getXMLString(docChangeShipmentInput));
		docdocChangeShipmentOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipmentInput);
		logger.verbose("Output xml for changeShipment api:"+ com.academy.util.xml.XMLUtil.getXMLString(docdocChangeShipmentOutput));
		logger.verbose("Exiting AcademyCancelShipmentAgent : executeJob");
	}
}

