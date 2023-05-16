package com.academy.ecommerce.sterling.shipment;

//import statements
//java util import statements
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;


/**STL-1435
 * Class to send a Shipment acknowledge message to Exeter for Share Inventory Node. 
 * This class is executed with the static condition of Order status in OMS is "Ready To Ship" and the Node type is SharedInventoryDC .
  */

public class AcademyPrepareShipAckMsgToExeter implements YIFCustomApi
{
	// Set the logger
	private static final YFCLogCategory	log		= YFCLogCategory.instance(AcademyPrepareShipAckMsgToExeter.class);
	private static YIFApi				api		= null;
	
	
	static
	{
		try
		{
			api = YIFClientFactory.getInstance().getApi();
		} catch (YIFClientCreationException e)
		{
			e.printStackTrace();
		}
	}

	// Set the properties variable
	private Properties props = new Properties();

	public void setProperties(Properties props)
	{
		this.props = props;
	}

	public void PrepareShipAckMsgToExeter(YFSEnvironment env, Document inXML) throws Exception
	{
		log.verbose("AcademyPrepareShipAckMsgToExeter: PrepareShipAckMsgToExeter: inXML: " + XMLUtil.getXMLString(inXML));
		
		Document docOutGetShipmentList = null;
		Element eleShipment = null;
		String strShipmentType = null;
		
		if(YFSObject.isVoid(env.getTxnObject(AcademyConstants.STR_POST_ACK_MSG))){    
			log.verbose("POST_ACK_MSG is empty. Sterling should publish acknowledgement message" );
			docOutGetShipmentList = getShipmentList(env, inXML);	
			
			//OMNI-6615 : Begin
			eleShipment = (Element)docOutGetShipmentList.getDocumentElement()
					.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
			strShipmentType = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
			
			if(AcademyConstants.STR_SHIP_TO_STORE.equals(strShipmentType)) {
				//New logic to send STS shipments' details to Exeter
				//Publish acknowledgement message - STS
				PublishShipAckMsgToExeterSTS(env, docOutGetShipmentList);
			}else {			
				//Existing Logic to send messages to Exeter
				//Publish acknowledgement message - Regular
				PublishShipAckMsgToExeter(env,docOutGetShipmentList);
			}
			//OMNI-6615 : End
		}
		log.verbose("AcademyPrepareShipAckMsgToExeter: PrepareShipAckMsgToExeter: END: " );
	}

	/**
	 * @param env
	 * @param inXML
	 * @return
	 * @throws ParserConfigurationException
	 * @throws Exception
	 */
	private Document getShipmentList(YFSEnvironment env, Document inXML)throws Exception {
		log.verbose("AcademyPrepareShipAckMsgToExeter: getShipmentList: inXML: " + XMLUtil.getXMLString(inXML));
		
		Document docInGetShipmentList;
		Document docOutGetShipmentList;
		Element eleRootShipment;
		String strShipmentKey;
		
		eleRootShipment = inXML.getDocumentElement();
		strShipmentKey = eleRootShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		docInGetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		docInGetShipmentList.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);

		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, AcademyConstants.TEMPLATE_GET_SHIPMENT_LIST_SI_ACK_MSG);
		log.verbose("Input to getShipmentList API for SharedInventory::" + XMLUtil.getXMLString(docInGetShipmentList));
		docOutGetShipmentList=AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, docInGetShipmentList);
		log.verbose("Output of getShipmentList API for SharedInventory::" + XMLUtil.getXMLString(docOutGetShipmentList));
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		
		log.verbose("AcademyPrepareShipAckMsgToExeter: getShipmentList: inXML: " );
		return docOutGetShipmentList;
	}
	
	private void PublishShipAckMsgToExeter(YFSEnvironment env,Document getShipmentListOutDoc) throws Exception
	{
		log.verbose("AcademyPrepareShipAckMsgToExeter: prepareShipAckMsgToExeter: inXML: "  + XMLUtil.getXMLString(getShipmentListOutDoc));
		
		Element eleShipmentLines = null;
		Element eleShipments = null;
		Element eleExtn= null;
		Element eleShipment = null;
		Element eleExtnOutDoc = null; 
		Element eleShipmentLine = null;
		Element eleShipAckMsgToExeter = null;
		Document shipAckMsgToExeterDoc = null;
		NodeList shipmentLineNL = null;
		String strDateFormat = "";
		
		// Start STL-1677
		NodeList containersNL = null;
		Element eleTrackingLines = null;
		Element eleTrackingLine = null;
		Element eleContainer = null;
		//End STL-1677
		
		eleShipments = getShipmentListOutDoc.getDocumentElement();
		eleExtn= (Element)eleShipments.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
		eleShipment = (Element)eleShipments.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);

		shipAckMsgToExeterDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleShipAckMsgToExeter = shipAckMsgToExeterDoc.getDocumentElement();
		eleShipAckMsgToExeter.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
		eleShipAckMsgToExeter.setAttribute(AcademyConstants.ATTR_SHIP_NODE, eleShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE));

		eleExtnOutDoc = shipAckMsgToExeterDoc.createElement(AcademyConstants.ELE_EXTN);
		eleExtnOutDoc.setAttribute(AcademyConstants.ATTR_CONTAINER_ID, eleExtn.getAttribute(AcademyConstants.ATTR_EXETER_CONTAINER_ID));
		eleExtnOutDoc.setAttribute(AcademyConstants.ATTR_RMS_TRANSFER_NO, eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_RMS_TRANSFER_NO));
		eleExtnOutDoc.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, AcademyConstants.STR_SHIP_ACK);

		YFCDate yfcDate = new YFCDate(new Date());
		strDateFormat = AcademyConstants.STR_DATE_FORMAT;
		SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
		StringBuilder strDate = new StringBuilder(sDateFormat.format(yfcDate));
		strDate.insert(22, AcademyConstants.STR_COLON);		
		eleExtnOutDoc.setAttribute(AcademyConstants.ATTR_RECIEPT_DATE, String.valueOf(strDate));		
		eleShipAckMsgToExeter.appendChild(eleExtnOutDoc);

		shipmentLineNL = XPathUtil.getNodeList(eleShipments, AcademyConstants.XPATH_SHIPMENTlINE);
		eleShipmentLines = shipAckMsgToExeterDoc.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
		eleShipAckMsgToExeter.appendChild(eleShipmentLines);
		
		for(int i=0;i<shipmentLineNL.getLength();i++)
		{
			eleShipmentLine = (Element)shipmentLineNL.item(i);	
			shipAckMsgToExeterDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES).item(0).appendChild(shipAckMsgToExeterDoc.importNode(eleShipmentLine,true));				
		}
		
		//STL-1677 Begin Shipment Ack - Capture Carrier and Tracking #
		containersNL = XPathUtil.getNodeList(eleShipments, AcademyConstants.XPATH_CONTAINER_PATH);
		eleShipAckMsgToExeter.setAttribute(AcademyConstants.CARRIER_SERVICE_CODE, eleShipment.getAttribute(AcademyConstants.CARRIER_SERVICE_CODE));
		eleShipAckMsgToExeter.setAttribute(AcademyConstants.ATTR_SCAC, eleShipment.getAttribute(AcademyConstants.ATTR_SCAC));
		eleTrackingLines = shipAckMsgToExeterDoc.createElement(AcademyConstants.ELE_TRACKING_LINES);
		eleShipAckMsgToExeter.appendChild(eleTrackingLines);
		
		if( containersNL != null ) {
			for(int iContCount = 0 ; iContCount < containersNL.getLength() ; iContCount++) {
				eleTrackingLine = shipAckMsgToExeterDoc.createElement(AcademyConstants.ELE_TRACKING_LINE);
				eleContainer = (Element) containersNL.item(iContCount);
				eleTrackingLine.setAttribute(AcademyConstants.ATTR_TRACKING_NO, eleContainer.getAttribute(AcademyConstants.ATTR_TRACKING_NO));			
				eleTrackingLines.appendChild(eleTrackingLine);
			}
		}
		//STL-1677 End Shipment Ack - Capture Carrier and Tracking #
		
		log.verbose("Acknowledgement Msg posted to Exeter::" + XMLUtil.getXMLString(shipAckMsgToExeterDoc));
		AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_PUBLISH_SHIP_ACK_MSG, shipAckMsgToExeterDoc);		
		log.verbose("AcademyPrepareShipAckMsgToExeter: prepareShipAckMsgToExeter: END: ");		
	}
	
	//OMNI-6615 : Begin
	private void PublishShipAckMsgToExeterSTS(YFSEnvironment env,Document getShipmentListOutDoc) throws Exception{
		
		log.verbose("AcademyPrepareShipAckMsgToExeterSTS: inputXML :: "  + XMLUtil.getXMLString(getShipmentListOutDoc));
		
		Element eleShipmentLines = null;
		Element eleShipments = null;
		Element eleExtn= null;
		Element eleShipment = null;
		Element eleExtnOutDoc = null; 
		Element eleShipmentLine = null;
		Element eleShipAckMsgToExeter = null;
		Document shipAckMsgToExeterDoc = null;
		NodeList shipmentLineNL = null;
		String strDateFormat = "";		
		NodeList containersNL = null;
		Element eleContainers = null;
		Element eleTrackingLine = null;
		Element eleContainer = null;
		
		eleShipments = getShipmentListOutDoc.getDocumentElement();
		eleExtn= (Element)eleShipments.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
		eleShipment = (Element)eleShipments.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);

		shipAckMsgToExeterDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleShipAckMsgToExeter = shipAckMsgToExeterDoc.getDocumentElement();
		eleShipAckMsgToExeter.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
		eleShipAckMsgToExeter.setAttribute(AcademyConstants.ATTR_SHIP_NODE, eleShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE));

		eleExtnOutDoc = shipAckMsgToExeterDoc.createElement(AcademyConstants.ELE_EXTN);
		eleExtnOutDoc.setAttribute(AcademyConstants.ATTR_CONTAINER_ID, eleExtn.getAttribute(AcademyConstants.ATTR_EXETER_CONTAINER_ID));
		eleExtnOutDoc.setAttribute(AcademyConstants.ATTR_RMS_TRANSFER_NO, eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_RMS_TRANSFER_NO));
		eleExtnOutDoc.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, AcademyConstants.STR_SHIP_ACK);

		YFCDate yfcDate = new YFCDate(new Date());
		strDateFormat = AcademyConstants.STR_DATE_FORMAT;
		SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
		StringBuilder strDate = new StringBuilder(sDateFormat.format(yfcDate));
		strDate.insert(22, AcademyConstants.STR_COLON);		
		eleExtnOutDoc.setAttribute(AcademyConstants.ATTR_RECIEPT_DATE, String.valueOf(strDate));		
		eleShipAckMsgToExeter.appendChild(eleExtnOutDoc);
		
		shipmentLineNL = XPathUtil.getNodeList(eleShipments, AcademyConstants.XPATH_SHIPMENTlINE);
		eleShipmentLines = shipAckMsgToExeterDoc.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
		eleShipAckMsgToExeter.appendChild(eleShipmentLines);
		
		for(int i=0;i<shipmentLineNL.getLength();i++)
		{
			eleShipmentLine = (Element)shipmentLineNL.item(i);	
			shipAckMsgToExeterDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES).item(0).appendChild(shipAckMsgToExeterDoc.importNode(eleShipmentLine,true));				
		}
		
		containersNL = XPathUtil.getNodeList(eleShipments, AcademyConstants.XPATH_CONTAINER_PATH);
		eleShipAckMsgToExeter.setAttribute(AcademyConstants.CARRIER_SERVICE_CODE, eleShipment.getAttribute(AcademyConstants.CARRIER_SERVICE_CODE));
		eleShipAckMsgToExeter.setAttribute(AcademyConstants.ATTR_SCAC, eleShipment.getAttribute(AcademyConstants.ATTR_SCAC));
		eleContainers = shipAckMsgToExeterDoc.createElement(AcademyConstants.ELE_CONTAINERS);
		eleShipAckMsgToExeter.appendChild(eleContainers);
		
		if( containersNL != null ) {
			for(int iContCount = 0 ; iContCount < containersNL.getLength() ; iContCount++) {
				
				Element eleCntainer = (Element)containersNL.item(iContCount);
				Element eleTempContainer = (Element) shipAckMsgToExeterDoc
						.importNode(eleCntainer, true);
				
				eleTempContainer.removeAttribute(AcademyConstants.ATTR_TRACKING_NO);
				
				eleContainers.appendChild(eleTempContainer);
			}
		}
		
		log.verbose("Acknowledgement Msg posted to Exeter::" + XMLUtil.getXMLString(shipAckMsgToExeterDoc));
		AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_PUBLISH_SHIP_ACK_MSG, shipAckMsgToExeterDoc);		
		log.verbose("AcademyPrepareShipAckMsgToExeter: prepareShipAckMsgToExeterSTS: END: ");	
		
	}
	//OMNI-6615 : End
}


