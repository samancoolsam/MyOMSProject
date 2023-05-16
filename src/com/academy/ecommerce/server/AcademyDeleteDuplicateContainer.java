package com.academy.ecommerce.server;


import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.dblayer.YFCContext;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author <a href="mailto:Shruthi.Kenkarenarendrababu@cognizant.com">Shruthi KN</a>, Created on
 *         10/16/2015. 
 *        
 */

public class AcademyDeleteDuplicateContainer extends YCPBaseAgent {
	
	private final String ATTR_REPLACE = "Replace"; 
	private final String ACTION_DELETE = "Delete";
	
	private final Logger logger = Logger.getLogger(AcademyDeleteDuplicateContainer.class.getName());
	
	private final String GET_SHIPMENT_LIST_OUT_TEMP = "<Shipments>" +
																"<Shipment ShipmentKey='' ShipmentNo='' ShipmentType='' Status='' >" +
																	"<Containers TotalNumberOfRecords=''>" +
																		"<Container ShipmentContainerKey='' TrackingNo='' ShipmentKey=''>" +
																			"<ContainerDetails>" +
																				"<ContainerDetail Quantity='' ItemID='' />" +
																			"</ContainerDetails>" +
																			 "<ContainerActivities>" +
																			 	"<ContainerActivity Createts='' ShipmentContainerKey=''/>" +
																			 "</ContainerActivities>" +
																		"</Container>" +
																	"</Containers>" +
																	"<ShipmentLines>" +
																		"<ShipmentLine ActualQuantity='' OverShipQuantity='' Quantity=''/>" +
																	"</ShipmentLines>" +
																"</Shipment>" +
															"</Shipments>";
	
	@Override
	public ArrayList<Document> getJobs(YFSEnvironment env, Document inXML) throws Exception {
		logger.verbose("Inside AcademyCancelShipmentAgent getJobs.The Input xml is : " + XMLUtil.getXMLString(inXML));
		
		Document docgetShipmentListOutput = null;
		ArrayList<Document> getContainerList = new ArrayList<Document>();
		
		//prepare input and call getShipmentList API
		docgetShipmentListOutput = prepareInputAndCallGetShipmentList(env,inXML);
		
		//Method to get the duplicate container for the shipment
		getContainerList = getDuplicateContainerFromShipment(env,docgetShipmentListOutput);
			
		logger.verbose("Exiting AcademyCancelShipmentAgent : getJobs ");
		return getContainerList;
	}




	/**
	 * @param env
	 * @return
	 * @throws Exception
	 * This method will prepare the input and invoke getShipmentList API
	 * and returns the list of shipment which are in Ready To Ship Status
	 * <Shipment Status="1100.70.06.30"/>
	 *
	 */
	private Document prepareInputAndCallGetShipmentList(YFSEnvironment env, Document inXML) throws Exception {
		// TODO Auto-generated method stub
		logger.verbose("Entering into method prepareInputAndCallGetShipmentList : getJobs ");
		Document docgetShipmentListOutput = null;
		Document docGetShipmentListInput = null;
		
		docGetShipmentListInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleRoot = docGetShipmentListInput.getDocumentElement();	 
		eleRoot.setAttribute(AcademyConstants.ATTR_STATUS, AcademyConstants.VAL_READY_TO_SHIP_STATUS);
		
		//env.setApiTemplate("getShipmentList",GET_SHIPMENT_LIST_OUT_TEMP);
		env.setApiTemplate("getShipmentList",XMLUtil.getDocument(GET_SHIPMENT_LIST_OUT_TEMP));
		logger.verbose("Template XML is"+ GET_SHIPMENT_LIST_OUT_TEMP);
		
		logger.verbose("Input xml for getShipmentList api:"+ com.academy.util.xml.XMLUtil.getXMLString(docGetShipmentListInput));
		docgetShipmentListOutput = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_SHIPMENT_LIST, docGetShipmentListInput);
		logger.verbose("Output xml for getShipmentList api:"+ com.academy.util.xml.XMLUtil.getXMLString(docgetShipmentListOutput));
		env.clearApiTemplate("getShipmentList");
		logger.verbose("End of method prepareInputAndCallGetShipmentList : getJobs ");
		return docgetShipmentListOutput;
	}




	/**
	 * @param env
	 * @param docgetShipmentListOutput
	 * @return
	 * @throws ParserConfigurationException 
	 */
	private ArrayList<Document> getDuplicateContainerFromShipment(YFSEnvironment env,Document docgetShipmentListOutput) throws ParserConfigurationException {
		// TODO Auto-generated method stub
		logger.verbose("Start of method getDuplicateContainerFromShipment : getJobs ");
		ArrayList<Document> ContainerList = new ArrayList<Document>();
		Document docgetLastContainerNo = null;
		Element eleShipment = null;
		String strShipmentNo = "";
		
	
		
		NodeList nlShipmentList = docgetShipmentListOutput.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
		for (int iShipment = 0,iShipmentcount=nlShipmentList.getLength(); iShipment < iShipmentcount; iShipment++) {
			double packedQuantity = 0;
			double shipmentActualQuantity = 0;
			logger.verbose("Getting shipment");
			
			eleShipment = (Element) nlShipmentList.item(iShipment);
			strShipmentNo = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			logger.verbose("ShipmentNo : '"+ strShipmentNo +"'");	
			
			NodeList  ContainerDetailList = eleShipment.getElementsByTagName(AcademyConstants.CONTAINER_DETL_ELEMENT);
			for (int iContainerDetail= 0; iContainerDetail < ContainerDetailList.getLength(); iContainerDetail++) {
				Element eleContainerDetail = (Element) ContainerDetailList.item(iContainerDetail);
				String strQuantity =  eleContainerDetail.getAttribute(AcademyConstants.ATTR_QUANTITY);
				
				if (!YFSObject.isVoid(strQuantity))
				{
					packedQuantity = packedQuantity + Double.valueOf(strQuantity);
				}
			}
			
			NodeList  shipmentLineList = eleShipment.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			for (int iContainer= 0,iShipmentLineCount=shipmentLineList.getLength(); iContainer < iShipmentLineCount; iContainer++) {
				Element eleShipmentLine = (Element) shipmentLineList.item(iContainer);
				String strActualQuantity = eleShipmentLine.getAttribute(AcademyConstants.ATTR_ACTUAL_QUANTITY);
				if (!YFSObject.isVoid(strActualQuantity))
					{
						shipmentActualQuantity = shipmentActualQuantity + Double.valueOf(strActualQuantity);
					}
					logger.verbose("shipmentActualQuantity : " + shipmentActualQuantity);
			}
			logger.verbose("shipmentActualQuantity : " + shipmentActualQuantity + ", Packed Quantity : "  + packedQuantity);
			
			if (packedQuantity > shipmentActualQuantity) {
				
				logger.verbose("Condition Satisfied");
				NodeList nlContainer = eleShipment.getElementsByTagName("Container");
				Element eleContainerLast = (Element) nlContainer.item(nlContainer.getLength() - 1);
				
				docgetLastContainerNo = XMLUtil.createDocument("DeleteContainer");
				docgetLastContainerNo.getDocumentElement().setAttribute(AcademyConstants.ATTR_TRACKING_NO,eleContainerLast.getAttribute(AcademyConstants.ATTR_TRACKING_NO));
				docgetLastContainerNo.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY,eleContainerLast.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY));
				docgetLastContainerNo.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,eleContainerLast.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
				logger.verbose("XML to publish to executejobs : " + XMLUtil.getXMLString(docgetLastContainerNo));
				logger.verbose("End of method getDuplicateContainerFromShipment : getJobs ");
				ContainerList.add(docgetLastContainerNo);				
			}		
		}
		return ContainerList;		
	}


	@Override
	public void executeJob(YFSEnvironment env, Document input) throws Exception {
		// TODO Auto-generated method stub
		logger.verbose("Entering into AcademyDeleteDuplicateContainer executeJob");
		
		String strShipmentContainerKey = "";
		String strTrackingNo = "";
		String strShipmentKey = "";
		
		Element eleRoot = input.getDocumentElement();
		strTrackingNo = eleRoot.getAttribute(AcademyConstants.ATTR_TRACKING_NO);
		strShipmentContainerKey = eleRoot.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY);
		strShipmentKey = eleRoot.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		
		logger.verbose("Value of Tracking No: " + strTrackingNo);
		logger.verbose("Value of ShipmentContainerKey: " + strShipmentContainerKey);
		logger.verbose("Value of ShipmentKey: " + strShipmentKey);
		
		
		String strvoidTrackingQry = "UPDATE " + AcademyConstants.TABLE_YFS_SHIPMENT_CONT + " set " + "TRACKING_NO='' where SHIPMENT_CONTAINER_KEY='" + strShipmentContainerKey + "'";
		logger.verbose("Query to void the Tracking No: \n " + strvoidTrackingQry);

		String strUpdateManifestUpsDtl = "UPDATE " + AcademyConstants.TABLE_YCS_MANIFEST_UPS_DTL + " set " + "SHIP_ID='DUMMY',MANIFEST_NUMBER='DUMMY' where PACKAGE_TRACKING_NUMBER='" + strTrackingNo + "'";
		logger.verbose("Query to Update YCS_MANIFEST_UPS_DTL: \n " + strUpdateManifestUpsDtl);
		Statement stmt = null;
		try {
			YFCContext ctxt = (YFCContext) env;
			stmt = ctxt.getConnection().createStatement();
			int hasUpdated = stmt.executeUpdate(strvoidTrackingQry);
			
			int hasUpdated2 = stmt.executeUpdate(strUpdateManifestUpsDtl);
			if (hasUpdated > 0) {
				logger.verbose("Tracking No has been blanked out.");
			}
			
			if (hasUpdated2 > 0) {
				logger.verbose("Updated the ShipmentNo and ManifestNo as DUMMY.");
			}
		} catch (SQLException sqlEx) {
			logger.verbose("Error occured on removing shipment from manifest");
			sqlEx.printStackTrace();
			throw sqlEx;
		} finally {
			if (stmt != null)
				stmt.close();
			stmt = null;
		}
		
		//Sample XML to delete duplicate container with API changeShipment
		
		/*<Shipment Action="Modify" IgnoreOrdering="Y" ShipmentContainerKey="201510160520412751434245" ShipmentKey="201510160519412751433434">
	    	<Containers Replace="N">
	        	<Container Action="Delete" ShipmentContainerKey="201510160520412751434245"/>
	    	</Containers>
		  </Shipment>*/
		
		//Preparing input to call changeShipment to delete the duplicate container
		Document docChangeShipmentInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element ShipmentElement = docChangeShipmentInput.getDocumentElement();
		ShipmentElement.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY);
		ShipmentElement.setAttribute(AcademyConstants.ATTR_IGNORE_ORDERING, AcademyConstants.STR_YES);
		ShipmentElement.setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY, strShipmentContainerKey);
		ShipmentElement.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
		
		Element ContainersElement = docChangeShipmentInput.createElement(AcademyConstants.ELE_CONTAINERS);
		ContainersElement.setAttribute(ATTR_REPLACE, AcademyConstants.STR_NO);
		
		Element ContainerElement = docChangeShipmentInput.createElement(AcademyConstants.ELE_CONTAINER);
		ContainerElement.setAttribute(AcademyConstants.ATTR_ACTION, ACTION_DELETE);
		ContainerElement.setAttribute("ShipmentContainerKey", strShipmentContainerKey);
		
		ContainersElement.appendChild(ContainerElement);
		ShipmentElement.appendChild(ContainersElement);
		logger.verbose("Input XML for changeShipment API is : " + XMLUtil.getXMLString(docChangeShipmentInput));
		
		AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipmentInput);
		logger.verbose("Exiting AcademyDeleteDuplicateContainer : executeJob");
		
	}
		
}

