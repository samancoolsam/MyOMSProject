package com.academy.ecommerce.sterling.fedex.express.notification;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/*########################################################################################################
* 
* Project Name                : POD MAY Release
* Module                      : OMS
* Author                      : CTS
* Date                        : 08-MAY-2020
* Description                 : This file implements the logic to create pickup requests for parked
* 								records in ACAD_FEDX_EXPRESS_PING. The details are given below.
* 								
* 								getJobs() :
* 								----------- 
* 								1. Fetch records of current day  from ACAD_FEDX_EXPRESS_PING table 
* 								   having IS_PROCESSED = 'N'.
*                               2. Prepares Document Shipment with attributes ShipmentNo, ShipNode
*                                  and return it.
*                               
*                               executeJobs() :
*                               ---------------   
*                               1. Read ShipNode from input and invoke getShipNodeList API to fetch
*                                  store address.	
*								2. Update input with ShipNode details and invoke service to
*								   create pickup request for the day.	
*
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author                  Version#       Remarks/Description                     
* ---------------------------------------------------------------------------------
* 08-MAY-2020     CTS                      1.0            Initial version
*########################################################################################################*/

public class AcademyFedexExpressParkedShipmentNotification extends YCPBaseAgent {

	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyFedexExpressParkedShipmentNotification.class);

	public List<Document> getJobs(YFSEnvironment envObj, Document docInput, Document docLastMessage) throws Exception {

		List<Document> alAcadFedxExpPing = new ArrayList<Document>();

		Document docAcadFedxExpPingList = null;

		NodeList nlAcadFedxExpPing = null;

		String strCurrentdate = null;

		try {

			logger.verbose("Input - AcademyFedexExpressParkedShipmentNotification.getJobs() :: "
					+ XMLUtil.getXMLString(docInput));

			strCurrentdate = AcademyFedexExpressShipmentNotificationUtils.getCurrentdate();

			if (null != docLastMessage) {

				logger.verbose("last Message doc :: " + XMLUtil.getXMLString(docLastMessage));

				String strStoreNo = docLastMessage.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE);

				docAcadFedxExpPingList = AcademyFedexExpressShipmentNotificationUtils
						.getListAcadFedxExpressPingStoreQry(envObj, strCurrentdate, strStoreNo);
			} else {

				logger.verbose("last Message doc is null :: ");

				docAcadFedxExpPingList = AcademyFedexExpressShipmentNotificationUtils.getListAcadFedxExpressPing(envObj,
						strCurrentdate, AcademyConstants.STR_BLANK);
			}

			logger.verbose("Output Document :: " + XMLUtil.getXMLString(docAcadFedxExpPingList));

			nlAcadFedxExpPing = docAcadFedxExpPingList.getElementsByTagName(AcademyConstants.ELE_ACAD_FEDX_EXP_PING);

			if (null != nlAcadFedxExpPing && nlAcadFedxExpPing.getLength() > 0) {

				logger.verbose("Parked entries are present :: ");

				for (int i = 0; i < nlAcadFedxExpPing.getLength(); i++) {
					/*
					 * Preparing input for service that will be invoked in executeJob. The same
					 * service is being used in an integration server as well. Hence forming input.
					 */
					Document docShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);

					Element eleAcadFedxExpPing = (Element) nlAcadFedxExpPing.item(i);

					docShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO,
							eleAcadFedxExpPing.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));

					docShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIP_NODE,
							eleAcadFedxExpPing.getAttribute(AcademyConstants.ATTR_STORE_NO));

					logger.verbose("Document - Shipment :: " + XMLUtil.getXMLString(docShipment));

					alAcadFedxExpPing.add(docShipment);
				}

			}

		} catch (Exception e) {
			logger.error("Exception :: AcademyFedexExpressParkedShipmentNotification.getJobs() :: " + e);
		}

		return alAcadFedxExpPing;
	}

	@Override
	public void executeJob(YFSEnvironment envObj, Document docInput) throws Exception {

		Document docShipNodeList = null;
		Element eleShipNode = null;
		Element eleShipment = null;

		String strShipNode = null;

		try {

			logger.verbose("Input - AcademyFedexExpressParkedShipmentNotification.executeJob() :: "
					+ XMLUtil.getXMLString(docInput));

			eleShipment = docInput.getDocumentElement();
			strShipNode = eleShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
			docShipNodeList = AcademyFedexExpressShipmentNotificationUtils.invokeGetShipNodeListApi(envObj,
					strShipNode);

			if (null != docShipNodeList) {

				eleShipNode = (Element) docShipNodeList.getElementsByTagName(AcademyConstants.ELE_SHIP_NODE).item(0);

				if (null != eleShipNode) {

					Element eleTempShipNode = (Element) docInput.importNode(eleShipNode, true);
					docInput.getDocumentElement().appendChild(eleTempShipNode);
					/*
					 * Transaction object is being set in order to execute specific logic applicable
					 * only for parked requests.
					 */
					envObj.setTxnObject(AcademyConstants.STR_IS_PARKED_REQUEST, AcademyConstants.STR_YES);

					logger.verbose("Input - Create Pickup Request :: " + XMLUtil.getXMLString(docInput));

					AcademyUtil.invokeService(envObj, AcademyConstants.SERVICE_FEDEX_EXPRESS_SHIPMENT_NOTIFICATION,
							docInput);
				}

			}

		} catch (Exception e) {
			logger.error("Exception :: AcademyFedexExpressParkedShipmentNotification.executeJob() :: " + e);
		}
	}

}
