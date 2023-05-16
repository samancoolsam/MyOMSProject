/**##############################################################################################################################
 *
 * Project Name                : ASO
 * Module                      : OMNI-105142
 * Author                      : CTS
 * Author Group				   : CTS - POD
 * Date                        : 13-APR-2023
 * Description				   : This class is invoked in "AcademyManageOutboundConveyUpdates" Service. 
 * 								 It sends Confirm Shipment Updates to Convey for SFS,SFDC,DSV,SOF,STS2 type of Shipments.
 * 								 Invokes "Academy" Service for Sending Updates to Convey. 	
 * 								
 * ------------------------------------------------------------------------------------------------------------------------------
 * Date            	Author         		Version#       		Remarks/Description                      
 * ------------------------------------------------------------------------------------------------------------------------------
 * 13-APR-2023		CTS  	 			 1.0           		Initial version
 *
 * ###############################################################################################################################*/

package com.academy.ecommerce.sterling.shipment;

import java.util.Arrays;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;

import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyManageOutboundConveyUpdates {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyManageOutboundConveyUpdates.class.getName());

	private Properties props;

	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	/**
	 * This method validates whether to Publish Confirm Shipment Updates to Convey.
	 * 
	 * @param env
	 * @param docInputXML
	 * @returns finalOutputDoc
	 * @throws Exception
	 */
	public Document manageConveyUpdates(YFSEnvironment env, Document docInputXML) throws Exception {
		if (!YFCObject.isVoid(docInputXML)) {
			log.beginTimer("START :: AcademyManageOutboundConveyUpdates::manageConveyUpdates :: START");
			log.verbose(
					"AcademyManageOutboundConveyUpdates::manageConveyUpdates() Input XML ::" + XMLUtil.getXMLString(docInputXML));
			Document finalOutputDoc = docInputXML;
			Document outDocGetShipmentList = null;
			String strShipmentKey = null;
			boolean bSendConveyUpdate = false;
			try {
				bSendConveyUpdate = isConveyUpdateRequired(docInputXML);
				log.verbose("Boolean value bSendConveyUpdate :: " + bSendConveyUpdate);

				if (bSendConveyUpdate) {
					strShipmentKey = docInputXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
					outDocGetShipmentList = getShipmentList(env, docInputXML);
					NodeList nodeListShipment = XMLUtil.getNodeListByXPath(outDocGetShipmentList, AcademyConstants.XPATH_SHIPMENT);
					if (!YFCObject.isVoid(nodeListShipment) && nodeListShipment.getLength() == 1) {
						log.verbose("Shipment is Eligible for Convey Update!!");
						Document updateMsgToConvey = prepareConveyMessage(outDocGetShipmentList);
						log.verbose("Sending Ship Confirm Update to Convey!!");
						sendMessageToConvey(env,updateMsgToConvey);
						finalOutputDoc = updateMsgToConvey;
					} else {
						log.error("Shipment with ShipmentKey : " + strShipmentKey + " could not be identified :: "
								+ XMLUtil.getXMLString(outDocGetShipmentList));
						YFSException e = new YFSException();
						e.setErrorCode(AcademyConstants.ERR_CODE_58);
						e.setErrorDescription("Shipment with ShipmentKey : " + strShipmentKey + " could not be identified :: ");
						throw e;
					}

				} else {
					log.verbose("Shipment is Not Eligible for Convey Update!!");
				}
				
				log.verbose("AcademyManageOutboundConveyUpdates::manageConveyUpdates() Output XML ::"+ XMLUtil.getXMLString(finalOutputDoc));
				return finalOutputDoc;
			} catch (YFSException exp) {
				log.verbose("Exception occured in AcademyManageOutboundConveyUpdates.manageConveyUpdates() method");
				throw exp;
			} catch (Exception exp) {
				log.verbose("Exception occured in AcademyManageOutboundConveyUpdates.manageConveyUpdates() method");
				YFSException e = new YFSException(exp.getMessage());
				throw e;
			}finally {
				log.endTimer("END :: AcademyManageOutboundConveyUpdates::manageConveyUpdates :: END");
			}
		} else {
			log.error("Input Document to AcademyManageOutboundConveyUpdates.manageConveyUpdates() is NULL.");
			YFSException e = new YFSException();
			e.setErrorCode(AcademyConstants.ERR_CODE_58);
			e.setErrorDescription("Input Document to AcademyManageOutboundConveyUpdates.manageConveyUpdates is NULL");
			throw e;
		}
		
	}

	/*
	 * @param docInputXML
	 * @returns bSendConveyUpdate
	 * @throws Exception
	 * If Shipment is Eligible for Convey Update it returns true, else it returns false.
	 */
	private boolean isConveyUpdateRequired(Document docInputXML) throws Exception {
		log.verbose("AcademyManageOutboundConveyUpdates.isConveyUpdateRequired() method START");
		boolean bSendConveyUpdate = false;
		Element eleInputXML = docInputXML.getDocumentElement();
		String strDeliveryMethod = eleInputXML.getAttribute(AcademyConstants.ATTR_DEL_METHOD);
		String strDocumentType = eleInputXML.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
		String strShipmentType = eleInputXML.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
		String strNodeType = XMLUtil.getString(docInputXML, AcademyConstants.XPATH_NODE_TYPE);

		log.verbose("DeliveryMethod : " + strDeliveryMethod + ", DocumentType : " + strDocumentType
				+ ", ShipmentType : " + strShipmentType + ", NodeType : " + strNodeType);

		if (YFCObject.isVoid(strDeliveryMethod) && YFCObject.isVoid(strShipmentType)
				&& YFCObject.isVoid(strDocumentType) && YFCObject.isVoid(strNodeType)) {
			log.error(
					"Either DocumentType/DeliveryMethod/ShipmentType/NodeType Attributes are NULL in the input document of AcademyManageOutboundConveyUpdates class");
			YFSException e = new YFSException();
			e.setErrorCode(AcademyConstants.ERR_CODE_58);
			e.setErrorDescription("Mandatory parameters are missing in the input of AcademyManageOutboundConveyUpdates class");
			throw e;

		} else {
			if ((AcademyConstants.SALES_DOCUMENT_TYPE).equalsIgnoreCase(strDocumentType)) {
					if ((AcademyConstants.STR_SHIP_DELIVERY_METHOD).equalsIgnoreCase(strDeliveryMethod)
							&& !(AcademyConstants.STR_SPECIAL_ORDER_FIREARMS).equalsIgnoreCase(strShipmentType)) {
							bSendConveyUpdate = true;
					}
			} else if ((AcademyConstants.DOCUMENT_TYPE_PO).equalsIgnoreCase(strDocumentType)) {
							bSendConveyUpdate = true;
			} else if ((AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE).equalsIgnoreCase(strDocumentType)) {
					if ((AcademyConstants.STR_STORE).equalsIgnoreCase(strNodeType)) {
							bSendConveyUpdate = true;
					}
			}
		}
		log.verbose("AcademyManageOutboundConveyUpdates.isConveyUpdateRequired() method END");
		return bSendConveyUpdate;
	}

	private Document getShipmentList(YFSEnvironment env, Document docInputXML) throws Exception{
		log.verbose("AcademyManageOutboundConveyUpdates.getShipmentList() method START");
		String strShipmentKey = docInputXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		Document outDocGetShipmentList = null;
		if (!YFCObject.isVoid(strShipmentKey)) {
			String strGetShipmentList = "<Shipment ShipmentKey='" + strShipmentKey + "'/>";
			try {
				Document inDocGetShipmentList = XMLUtil.getDocument(strGetShipmentList);
				//env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST,
					//	AcademyConstants.STR_API_TEMPLATE_GETSHIPMENTLIST_FOR_CONVEY);
				log.verbose("In Document getShipmentList :: " + XMLUtil.getXMLString(inDocGetShipmentList));
				outDocGetShipmentList = AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_GET_SHIPMENT_LIST,
						inDocGetShipmentList);
				//env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
				log.verbose("Out Document getShipmentList :: " + XMLUtil.getXMLString(outDocGetShipmentList));
			} catch (YFSException exp) {
				log.verbose("Exception occured in AcademyManageOutboundConveyUpdates.getShipmentList() method");
				throw exp;
			} catch (Exception exp) {
				log.verbose("Exception occured in AcademyManageOutboundConveyUpdates.getShipmentList() method");
				YFSException e = new YFSException(exp.getMessage());
				throw e;
			}
		} else {
			log.error("ShipmentKey is NULL. Hence not invoking getShipmentList API");
			YFSException e = new YFSException();
			e.setErrorCode(AcademyConstants.ERR_CODE_58);
			e.setErrorDescription(
					"Mandatory attribute ShipmentKey is missing in the input of AcademyManageOutboundConveyUpdates class");
			throw e;
		}
		log.verbose("AcademyManageOutboundConveyUpdates.getShipmentList() method END");
		return outDocGetShipmentList;
	}
	

	/*
	 * @param outDocGetShipmentList
	 * @throws Exception
	 * @returns updateMsgToConvey
	 * From the outDocGetShipmentList we read the Shipment as element and create a new Document using that,
	 * and returns the new Document
	 */
	private Document prepareConveyMessage(Document outDocGetShipmentList) throws Exception{
		log.verbose("AcademyManageOutboundConveyUpdates.prepareConveyMessage() method START");
		Element eleShipment = XMLUtil.getElementByXPath(outDocGetShipmentList, AcademyConstants.XPATH_SHIPMENT);
		Document updateMsgToConvey = XMLUtil.getDocumentForElement(eleShipment);
		log.verbose("AcademyManageOutboundConveyUpdates.prepareConveyMessage() method END");
		return updateMsgToConvey;
	}
	
	/*
	 * @param updateMsgToConvey
	 * @throws Exception
	 * Invokes "AcademySendUpdatesToConvey" Service to Publish Ship Confirm Updates to Convey.
	 */
	private void sendMessageToConvey(YFSEnvironment env, Document updateMsgToConvey) {
		log.verbose("AcademyManageOutboundConveyUpdates.sendMessageToConvey() method START");
		try {
			log.verbose("Invoking AcademySendOutboundUpdatesToConvey Service with input :: "
					+ XMLUtil.getXMLString(updateMsgToConvey));
			AcademyUtil.invokeService(env, AcademyConstants.SERV_ACADEMY_SEND_OUTBOUND_UPDATES_TO_CONVEY, updateMsgToConvey);
		} catch (YFSException e) {
			e.setErrorCode(AcademyConstants.ERR_CODE_59);
			e.setErrorDescription("Exception occured while Posting Update Message to Convey Queue!!");
			log.info("Convey Outbound  Exception :: Exception occurred while posting message to Queue :: Error Code : "
					+ AcademyConstants.ERR_CODE_59 + " Exception Stack Trace : " + Arrays.toString(e.getStackTrace()));
			throw e;
		} 
		catch (Exception exp) {
			YFSException e = new YFSException(exp.getMessage());
			e.setErrorCode(AcademyConstants.ERR_CODE_59);
			e.setErrorDescription("Exception occured while Posting Update Message to Convey Queue!!");
			log.info("Convey Outbound  Exception :: Exception occurred while posting message to Queue :: Error Code : "
					+ AcademyConstants.ERR_CODE_59 + " Exception Stack Trace : " + Arrays.toString(e.getStackTrace()));
			throw e;
		} finally {
			log.verbose("AcademyManageOutboundConveyUpdates.sendMessageToConvey() method END");
		}
	}
}
