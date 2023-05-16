package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.server.AcademyModifyPickTicketPrintedFlag;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author <a href="mailto:Shruthi.Kenkarenarendrababu@cognizant.com">Shruthi KN</a>, Created on
 *         03/28/2016 as part of STL-1493. This Class will get invoke from AcademySFSResetPickTickets service. This will pick the shipments and flips the 
 *         PickTicketPrinted attribute to N And PickticketNo to null for the list of shipments in respective store
 *                 
 *         Sample Input XMl to the Class
 * 		   <Shipment DisplayLocalizedFieldInLocale="en_US_EST" ShipNode="001"/>
 */

public class AcademyResetPickTicketPrintFlag {
	
	private Properties props;
	private final Logger logger = Logger.getLogger(AcademyModifyPickTicketPrintedFlag.class.getName());
	
	public void setProperties(Properties props) {
		this.props = props;
	}
	 
	public void resetPickTicketFlag(YFSEnvironment env, Document inDoc)
		throws Exception {
	logger.verbose("Inside method resetPickTicketFlag");
	logger.verbose("Input XML is::" + XMLUtil.getXMLString(inDoc));
	
	Document docgetShipmentListOutput = null;
	//prepare input and call getShipmentList API
	docgetShipmentListOutput = prepareInputAndCallGetShipmentList(env,inDoc);
	
	//Invoke method to reset the PickTicketPrinted Flag to N and PickticketNo to null
	resetPickTicketFlagForShipments(env,docgetShipmentListOutput);
	logger.verbose("Exiting method resetPickTicketFlag");
	}
	
	/**
	 * This method will prepare input to call changeShipment API to reset the PicketTicketPrinted 
	 * Flag to N and PickticketNo to null for the list of shipments in respective store
	 * @param env
	 * @param docgetShipmentListOutput
	 * @throws Exception
	 */
	private void resetPickTicketFlagForShipments(YFSEnvironment env,
			Document docgetShipmentListOutput) throws Exception {
		// TODO Auto-generated method stub
		logger.verbose("Inside method resetPickTicketFlagForShipments");
		Document docChangeShipmentInput = null;
		Document docdocChangeShipmentOutput =null;
		Element eleRootElement = null;
		Element eleCurrentShipment = null;		
		
		NodeList nlShipmentList = docgetShipmentListOutput.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
		int iShipmentLength = nlShipmentList.getLength();
		for (int iShipmentCount = 0; iShipmentCount < iShipmentLength; iShipmentCount++) {
			eleCurrentShipment = (Element) nlShipmentList.item(iShipmentCount);
			
			String strShipmentKey = eleCurrentShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			
			docChangeShipmentInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			eleRootElement = docChangeShipmentInput.getDocumentElement();
			
			eleRootElement.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY);
			eleRootElement.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			eleRootElement.setAttribute(AcademyConstants.ATTR_IS_PICKTICKET_PRINTED, AcademyConstants.STR_NO);
			eleRootElement.setAttribute(AcademyConstants.ATTR_PICK_TICKET_NO, "");
			
			logger.verbose("Input xml for changeShipment api:"+ com.academy.util.xml.XMLUtil.getXMLString(docChangeShipmentInput));
			docdocChangeShipmentOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipmentInput);
			logger.verbose("Output xml for changeShipment api:"+ com.academy.util.xml.XMLUtil.getXMLString(docdocChangeShipmentOutput));
			logger.verbose("Exiting method resetPickTicketFlagForShipments");
		}
		
	}

	/**
	 * @param env
	 * @return
	 * @throws Exception
	 * This method will prepare the input and invoke getShipmentList API
	 * 
	 * <Shipment FromCreatets="" ToCreatets="" CreatetsQryType="BETWEEN"
	 * ShipNode='<ShipNode of logged in User>' Status="1100.70.06.10" PickTicketPrinted="Y"/>
	 * 
	 * FromCreatets and ToCreatets attributes can be used if there is any requirement 
	 * to get the list of shipments between 'X' days. These two attributes are configured in the service AcademySFSResetPickTickets
	 */
	private Document prepareInputAndCallGetShipmentList(YFSEnvironment env, Document inDoc) throws Exception {
		// TODO Auto-generated method stub
		logger.verbose("Entering into method prepareInputAndCallGetShipmentList");
		Document docgetShipmentListOutput = null;
		Document docGetShipmentListInput = null;
		Element eleinXML = null;
		
		eleinXML = inDoc.getDocumentElement();
		String strShipNode = eleinXML.getAttribute(AcademyConstants.SHIP_NODE);
		
		docGetShipmentListInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		
		Element eleRoot = docGetShipmentListInput.getDocumentElement();
		eleRoot.setAttribute(AcademyConstants.ATTR_FROM_CREATETS, "");
		eleRoot.setAttribute(AcademyConstants.ATTR_TO_CREATETS, "");
		eleRoot.setAttribute(AcademyConstants.ATTR_CREATETS_QRY_TYPE, AcademyConstants.BETWEEN);
		eleRoot.setAttribute(AcademyConstants.SHIP_NODE, strShipNode);
		eleRoot.setAttribute(AcademyConstants.ATTR_STATUS, AcademyConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL);
		eleRoot.setAttribute(AcademyConstants.ATTR_IS_PICKTICKET_PRINTED, AcademyConstants.STR_YES);
		
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST,XMLUtil.getDocument(AcademyConstants.GET_SHIPMENT_LIST_OUTPUT_TEMPLATE));
		
		logger.verbose("Input xml for getShipmentList api:"+ com.academy.util.xml.XMLUtil.getXMLString(docGetShipmentListInput));
		docgetShipmentListOutput = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_SHIPMENT_LIST, docGetShipmentListInput);
		logger.verbose("Output xml for getShipmentList api:"+ com.academy.util.xml.XMLUtil.getXMLString(docgetShipmentListOutput));
		logger.verbose("End of method prepareInputAndCallGetShipmentList");
		return docgetShipmentListOutput;
	}
	
}
