package com.academy.ecommerce.sterling.shipment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.academy.util.logger.Logger;
import com.yantra.yfs.japi.YFSConnectionHolder;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSException;
import org.w3c.dom.Document;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfs.japi.YFSEnvironment;
import java.sql.SQLException;
import com.academy.util.common.AcademyUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;


public class AcademyFedexIntegrationAgent extends YCPBaseAgent {
	private final Logger logger = Logger.getLogger(AcademyFedexIntegrationAgent.class
			.getName());
	private  final YFCLogCategory log = YFCLogCategory.instance(AcademyFedexIntegrationAgent.class);

	public String getTrackingNos(YFSEnvironment env, Document inXML) throws SQLException {

		//To fetch List of tracking#'s from STS exemption query
		String strTrackingNo = "";
		String strQuery = "select distinct tsc.tracking_no AS Tracking_No " +
				"from STRLADM.YFS_SHIPMENT ts, STRLADM.YFS_ORDER_LINE yol,strladm.yfs_person_info pi, strladm.yfs_shipment_line tsl, strladm.yfs_item i,strladm.yfs_order_header toh,strladm.yfs_order_header soh, strladm.yfs_order_header tol,strladm.yfs_order_header sol,  STRLADM.YFS_SHIPMENT_CONTAINER tsc " +
				"where toh.ORDER_HEADER_KEY= tol.ORDER_HEADER_KEY and tol.chained_from_order_line_key=sol.order_line_key" +
				"and tsc.SHIPMENT_KEY=ts.SHIPMENT_KEY " +
				"and tsl.shipment_key=ts.shipment_key " +
				"and ts.order_no=toh.ORDER_NO " +
				"and toh.ORDER_NAME=soh.ORDER_NO " +
				"and soh.ORDER_HEADER_KEY= sol.ORDER_HEADER_KEY " +
				"and toh.ORDER_HEADER_KEY= tol.ORDER_HEADER_KEY  " +
				"and tol.chained_from_order_line_key=sol.order_line_key "+
				"and sol.item_id=i.item_id "+
				"and ts.TO_ADDRESS_KEY=pi.person_info_key "+
				"and ts.shipment_key > '20211225' " +
				"and tsl.item_id=sol.item_id "+
				"and ts.SHIPMENT_TYPE='STS' " +
				"and ts.DOCUMENT_TYPE='0006' " +
				"and ts.STATUS IN ('1100.70.06.10', '1100.70.06.30', '1400', '1600.001')";

		Connection oracleConnection = null;
		PreparedStatement ps = null;
		ResultSet resultSet = null;

		log.verbose(" :: strQuery :: " + strQuery);

		try {
			log.verbose(" :: Inside try block  ::");
			log.beginTimer("STS tracking numbers ");
			YFSConnectionHolder connHolder = (YFSConnectionHolder) env;
			oracleConnection = connHolder.getDBConnection();
			ps = oracleConnection.prepareStatement(strQuery);
			resultSet = ps.executeQuery();
			log.endTimer("STS Tracking  numbers");

			log.verbose(" :: Results ::");

			while (resultSet.next()) {
				strTrackingNo = resultSet.getString("Tracking_No");
				log.verbose(" :: strTrackingNo ::"+strTrackingNo);
			}
		}	
		catch (Exception exp){
			log.verbose(" :: Inside Catch block  ::");
			log.info("Exception: "+exp.getMessage());
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

		log.verbose(":: Final strTrackingNo :: " + strTrackingNo);
		return strTrackingNo ;
	}




	/* Method to insert Tracking# in ACAD_TRACKING_UPDATES TABLE
	 */
	public void executeJob(YFSEnvironment env, Document input) throws Exception {
		Document outputDoc = null;



		try {
			logger.verbose("Entering ACADFedexIntegAgent : executeJob ");
			logger.verbose("input is : "
					+ XMLUtil.getXMLString(input));

			outputDoc = AcademyUtil.invokeService(env,
					AcademyConstants.SERVICE_GET_TRACKING_NO_LIST,
					input);
			if (outputDoc.getElementsByTagName("ACAD_TRACKING_UPDATES")
					.getLength() == 0) {
				AcademyUtil.invokeService(env,
						"AcademyGetTrackingNoSyncService", input);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 

		logger.verbose("Exiting ACADFedexIntegAgent : executeJob ");
	}
}
