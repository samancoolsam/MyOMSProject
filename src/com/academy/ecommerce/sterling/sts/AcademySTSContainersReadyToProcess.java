package com.academy.ecommerce.sterling.sts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSConnectionHolder;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/*##################################################################################
*
* Project Name                : STS Project
* Module                      : OMS
* Author                      : CTS
* Date                        : 06-JUN-2020 
* Description				  : This class is to get the count of STS containers eligible
* 								for Receiving and Staging for a given Store and count is 
* 								displayed in Web SOM.
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       Remarks/Description                      
* ---------------------------------------------------------------------------------
* 06-JUN-2020		CTS  	 			  1.0           	Initial version
* 
* ##################################################################################*/

public class AcademySTSContainersReadyToProcess {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSContainersReadyToProcess.class);

	/**
	 * This method is used to retrieve the count of Containers  which are ready to be received 
	 * in last 45 days for a given Store and displayed in Web SOM
	 *  
	 * @param env,
	 * @param inXML
	 */
	public Document getReadyToReceiveContainerCount(YFSEnvironment env, Document inXML) throws Exception {
		log.verbose("Begin of AcademySTSContainersReadyToProcess.getReadyToReceiveContainerCount() method");

		Document docOutput = null;
		String strFromCreatets = getFromDate(AcademyConstants.STR_SIMPLE_DATE_PATTERN);

		inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_CREATETS, strFromCreatets);
		inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_CREATETS_QRY_TYPE, AcademyConstants.GT_QRY_TYPE);

		Document getGetShipmentContainerListTemplate = XMLUtil.getDocument("<Containers TotalNumberOfRecords=''/>");

		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST, getGetShipmentContainerListTemplate);

		log.beginTimer("STS ReadyToReceive Count");
		docOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST, inXML);
		log.endTimer("STS ReadyToReceive Count");

		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST);
		log.verbose("End of AcademySTSContainersReadyToProcess.getReadyToReceiveContainerCount() method");

		return docOutput;
	}


	/**
	 * This method is used to retrieve the count of Containers  which are ready to be received 
	 * in last 45 days for a given Store and displayed in Web SOM
	 *  
	 * @param env,
	 * @param inXML
	 */
	public Document getReadyToStageContainerCount(YFSEnvironment env, Document inXML) throws Exception {
		log.verbose("Begin of AcademySTSContainersReadyToProcess.getReadyToStageContainerCount() method");

		Document docOutput = XMLUtil.getDocument("<Containers TotalNumberOfRecords='0'/>");
		String strNoOfRecords = null;
		
		String strFromCreatets = getFromDate(AcademyConstants.DATE_YYYYMMDD_FORMAT);
		String strShipNode = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE);

		try {
			strNoOfRecords = getReadytoReciveContainers(env, strFromCreatets, strShipNode);
			docOutput.getDocumentElement().setAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS, strNoOfRecords);
		}
		catch (Exception exp) {
			log.info("Exception: getReadyToStageContainerCount() :: "+exp.getMessage());
			docOutput.getDocumentElement().setAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS, "ERR");
		}

		log.verbose("End of AcademySTSContainersReadyToProcess.getReadyToStageContainerCount() method");

		return docOutput;
	}

	/**
	 * This method will be used to fetch Count if TO containers receieved and ready for Staging
	 * @param env,
	 * @param strShipNode
	 * @return String ContainerNO
	 */
	private String getReadytoReciveContainers(YFSEnvironment env, String strFromCreatets, String strShipNode) throws Exception
	{	
		log.verbose("Begin of AcademySTSContainersReadyToProcess.getReadytoReciveContainers() method");
		
		String strContainerCount = "";

		String strQuery = "select count(distinct ysc.shipment_container_key) AS CONTAINER_COUNT " +
		"from STRLADM.YFS_SHIPMENT ysSO, STRLADM.YFS_ORDER_LINE yol, STRLADM.YFS_SHIPMENT ysTO, STRLADM.YFS_SHIPMENT_CONTAINER ysc " +
		"where ysSO.order_header_key = yol.chained_from_order_header_key " +
		"and yol.order_header_key = ysTO.order_header_key " +
		"and ysc.shipment_key = ysTO.shipment_key " +
		"and ysto.shipment_key > '" + strFromCreatets +"' " +
		"and ysSO.shipnode_key ='" + strShipNode + "' " +
		//Start : OMNI-10101 Performace Tuning
		"and ysc.shipment_key > '" + strFromCreatets +"' " +
		"and ysTO.receiving_node ='" + strShipNode + "' " +
		//End : OMNI-10101 Performace Tuning
		"and ysc.is_received='Y' " +
		"and (ysc.extn_is_so_cancelled = 'N' or ysc.extn_is_so_cancelled is null) " +
		"and ysTO.shipment_type='STS' " +
		"and ysSO.shipment_type='STS' " +
		"and ysc.zone = '' " +
		"and  yol.PACKLIST_TYPE <> 'FA' " + //OMNI-90536 - Do not display STS FA shipment count in 'readyToStage' in TC70
		"and ysSO.status in ('1100.70.06.10','1100.70.06.20') " ;

		Connection oracleConnection = null;
		PreparedStatement ps = null;
		ResultSet resultSet = null;

		log.verbose(" :: strQuery :: " + strQuery);
		
		try {
			log.verbose(" :: Inside try block  ::");
			log.beginTimer("STS ReadyToStage Count");
			YFSConnectionHolder connHolder = (YFSConnectionHolder) env;
			oracleConnection = connHolder.getDBConnection();
			ps = oracleConnection.prepareStatement(strQuery);
			resultSet = ps.executeQuery();
			log.endTimer("STS ReadyToStage Count");
			
			log.verbose(" :: Results ::");

			while (resultSet.next()) {
				strContainerCount = resultSet.getString("CONTAINER_COUNT");
				log.verbose(" :: strContainerCount ::"+strContainerCount);
			}
		}
		catch (Exception exp){
			log.verbose(" :: Inside Catch block  ::");
			log.info("Exception: getReadyToStageContainerCount() :: "+exp.getMessage());
			YFSException yfse = new YFSException();
			yfse.setErrorCode("EXTN_DB_ERR_01");
			yfse.setErrorDescription(exp.getMessage());
			throw yfse;
		}

		finally {
			log.verbose(" :: Inside Finally block ::");
			if(ps != null){
				ps.close();
				ps = null;
			}

			if(resultSet != null){
				resultSet.close();
				resultSet = null;
			}
		}
		
		log.verbose(":: Final strContainerCount :: " + strContainerCount);
		log.verbose("End of AcademySTSContainersReadyToProcess.getReadytoReciveContainers() method");
		
		return strContainerCount ;
	}


	/**
	 * This method will fetch the date based on the format and min days to consider
	 * @param env,
	 * @param strShipNode
	 * @return String ContainerNO
	 */
	private String getFromDate(String strDateTypeFormat) throws Exception
	{	
		String strFromDate = null;
		//Default number of days is set as 45
		int iNoOfDays = 45;

		String strMinNofOfDays = YFSSystem.getProperty(AcademyConstants.PROP_STS_MOBILE_DAYS_TO_CONSIDER);
		if(!YFCObject.isVoid(strMinNofOfDays)) {
			log.verbose("Overriding The no of days to consider with :: "+strMinNofOfDays);
			iNoOfDays = Integer.parseInt(strMinNofOfDays);
		}

		SimpleDateFormat sdf = new SimpleDateFormat(strDateTypeFormat);


		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -iNoOfDays);
		strFromDate = sdf.format(cal.getTime());

		log.verbose("Update From Date : " + strFromDate);

		return strFromDate;
	}

/**
	 * This method is used to retrieve the count of Containers  which are in Delivered and open status 
	 * in last 45 days for a given Store and displayed in Web SOM
	 *  
	 * @param env,
	 * @param inXML
	 */
	public Document getDeliveredContainerCount(YFSEnvironment env, Document inXML) throws Exception {
		log.verbose("Begin of AcademySTSContainersReadyToProcess.getDeliveredContainerCount() method");

		Document docOutput = null;
		String strFromCreatets = getFromDate(AcademyConstants.STR_SIMPLE_DATE_PATTERN);

		inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_CREATETS, strFromCreatets);
		inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_CREATETS_QRY_TYPE, AcademyConstants.GT_QRY_TYPE);

		Document getGetShipmentContainerListTemplate = XMLUtil.getDocument("<Containers TotalNumberOfRecords=''/>");

		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST, getGetShipmentContainerListTemplate);

		log.beginTimer("STS Delivered Count");
		docOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST, inXML);
		log.endTimer("STS Delivered Count");

		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST);
		log.verbose("End of AcademySTSContainersReadyToProcess.getDeliveredContainerCount() method");

		return docOutput;
	}
	
	/**
	 * This method is used to retrieve the count of Containers  which are in InTransit 
	 * in last 45 days for a given Store and displayed in Web SOM
	 *  
	 * @param env,
	 * @param inXML
	 */
	public Document getInTransitContainerCount(YFSEnvironment env, Document inXML) throws Exception {
		log.verbose("Begin of AcademySTSContainersReadyToProcess.getInTransitContainerCount() method");

		Document docOutput = null;
		String strFromCreatets = getFromDate(AcademyConstants.STR_SIMPLE_DATE_PATTERN);

		inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_CREATETS, strFromCreatets);
		inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_CREATETS_QRY_TYPE, AcademyConstants.GT_QRY_TYPE);

		Document getGetShipmentContainerListTemplate = XMLUtil.getDocument("<Containers TotalNumberOfRecords=''/>");

		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST, getGetShipmentContainerListTemplate);

		log.beginTimer("STS InTransit Count");
		docOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST, inXML);
		log.endTimer("STS InTransit Count");

		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST);
		log.verbose("End of AcademySTSContainersReadyToProcess.getInTransitContainerCount() method");

		return docOutput;
	}

}
