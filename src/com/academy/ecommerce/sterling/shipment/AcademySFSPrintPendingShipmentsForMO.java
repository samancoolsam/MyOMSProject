package com.academy.ecommerce.sterling.shipment;
//import statements
//java util import statements
import java.util.Properties;
import java.util.HashMap;
//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
//academy import statements
import com.academy.ecommerce.sterling.item.api.AcademySFSProcessPrintItemPickTickets;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
//yantra import statements
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.core.YFCObject;


/**STL-1501. Created by Netai Dey
 * Description: Class AcademySFSPrintPendingShipmentsForMO gets the list of all
 * shipments that have IsPickTicketPrinted="N" and changes this attribute to "Y"
 * This file is used for More Option application.
 * This class have same logic as AcademySFSPrintPendingShipments. except stamping printer ID.
 *
 * @throws Exception
 */
public class AcademySFSPrintPendingShipmentsForMO implements YIFCustomApi
{
	//log set up
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademySFSPrintPendingShipmentsForMO.class);
	// Instance to store the properties configured for the condition in
	// Configurator
	private Properties	props;

	// Stores property configured in configurator
	public void setProperties(Properties props) throws Exception
	{
		this.props = props;
	}
	//Api instance set up
	private static YIFApi	api	= null;
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
	/**
	 * Retrieves shipment list based on ShipNode and Statuses.
	 *
	 * @param env
	 *            Yantra Environment Context.
	 * @param inXML
	 *            Input Document Value.
	 * @return void
	 */
	public Document getShipments(YFSEnvironment env, Document inXML) throws Exception
	{
		log.verbose("Inside getShipments()");
		String STR_GET_SHIPMENT_LIST_TEMPLATE = "<Shipments><Shipment /></Shipments>";
		Document docItemPickTicketOut = null;
		Element eleItemPickTicketOut = null;
		String strBatchNo=null;
		Document printPickTicDoc = XMLUtil.createDocument(AcademyConstants.ELE_PRINT_PICK_TICKET);
		
		// Fetch the root element of the input
		Element elePrint = inXML.getDocumentElement();
	
		//Fetch the attribute value of PickticketNo
		String pickticketNo = elePrint.getAttribute(AcademyConstants.ATTR_PICK_TICKET_NO).trim();
		//Fetch the attribute value of ShipNode
		String strShipNode = elePrint.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
		//Fetch the attribute value of ShipmentKey
		String strShipmentKey = elePrint.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		//Fetch the attribute value of MAX_RECORD from the service argument
		String strMaximumRecords = elePrint.getAttribute(AcademyConstants.ATTR_MAX_RECORD);
		// Fetch the attribute value of PageNo

		//START : STL-1440 Print pick ticket for MO device
		if(YFSObject.isVoid(pickticketNo) && YFSObject.isVoid(strShipmentKey)){
			//invoke method generateBatchNumber to generate a batch number			
			//String strBatchNo=generateBatchNumber(env);
			strBatchNo=AcademySFSPrintPendingShipments.generateBatchNumber(env,inXML);			
		}
		
		//SFS2.0 001 Printer Id start
		String strIsPickTicketPrinted = elePrint.getAttribute(AcademyConstants.ATTR_IS_PICKTICKET_PRINTED);
		log.verbose(" IsPickTicketPrinted is :" + strIsPickTicketPrinted);
		
		//Check if ShipmentKey is blank
		if (!strShipmentKey.trim().equals(AcademyConstants.STR_EMPTY_STRING))
		{
			//Create Document
			Document getShipmentListOutputDoc = YFCDocument.getDocumentFor(STR_GET_SHIPMENT_LIST_TEMPLATE).getDocument();
			//Fetch the element Shipment
			Element shipment = (Element)getShipmentListOutputDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
			//Set attribute value of ShipmentKey
			shipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			//Set attribute value of PickTicketPrinted
			shipment.setAttribute(AcademyConstants.ATTR_IS_PICKTICKET_PRINTED, AcademyConstants.STR_YES);
			//Set attribute value of SingleShipment
			shipment.setAttribute(AcademyConstants.ATTR_SINGLE_SHIPMENT, AcademyConstants.STR_YES);
			//call methos processPrintShipment
			processPrintShipments(env, getShipmentListOutputDoc,strBatchNo, printPickTicDoc);
			return printPickTicDoc;
		}
		//Check for empty record
		if(YFSObject.isVoid(strMaximumRecords) && YFSObject.isVoid(pickticketNo))
		{
			//Fetch the value of MaximunRecord from the service argument
			strMaximumRecords = props.getProperty(AcademyConstants.KEY_MAX_RECORD);
		}
		//Check if value is not empty
		if(!YFSObject.isVoid(pickticketNo))
		{
			//Set the value in a variable
			strBatchNo = pickticketNo;
		}
		
		// invoke method to fetch shipment list
		//STL-1722 Added props as a method variable getShipmentListOutputDoc.
		Document getShipmentListOutputDoc = AcademySFSPrintPendingShipments.getShipmentListOutput(env, strShipNode, pickticketNo, strMaximumRecords, props);
		// invoke method processShipments to change the attribute
		// IsPickTicketPrinted="N"
		log.verbose("getShipmentListOutputDoc"+XMLUtil.getXMLString(getShipmentListOutputDoc));
		processPrintShipments(env, getShipmentListOutputDoc,strBatchNo, printPickTicDoc);
		log.verbose("Shipment pick ticket \n"+XMLUtil.getXMLString(printPickTicDoc));
		//Check for empty value
		if(YFSObject.isVoid(pickticketNo))
		{
			//Set the attribute value of PickticketNo
			elePrint.setAttribute(AcademyConstants.ATTR_PICK_TICKET_NO, strBatchNo);
			//Set the attribute value of MaximumRecord
			elePrint.setAttribute(AcademyConstants.ATTR_MAX_RECORD, AcademyConstants.STR_EMPTY_STRING);
			//Invoke service
			log.verbose("Invoking AcademySFSPrintItemPickTickets service inXML" + XMLUtil.getXMLString(inXML));
			docItemPickTicketOut = AcademyUtil.invokeService(env, AcademyConstants.PRINT_PICK_TICKETS_SERVICE_MO, inXML);
			
			if(!YFSObject.isVoid(docItemPickTicketOut)){
				eleItemPickTicketOut = (Element) printPickTicDoc.importNode(docItemPickTicketOut.getDocumentElement(), true);
				printPickTicDoc.getDocumentElement().appendChild(eleItemPickTicketOut);
				log.verbose("Shipment + Item pick ticket \n"+XMLUtil.getXMLString(printPickTicDoc));				
			}
		}
		return printPickTicDoc;
	}
		
	/**
	 * Changes the attribute IsPickTicketPrinted="Y" for each shipment by
	 * calling changeShipmentAPI
	 *
	 * @param env
	 *            Yantra Environment Context.
	 * @param getShipmentListOutputDoc
	 *            Document containing output of getShipmentList API which has a
	 *            list of shipments.
	 * @return void
	 */
	private void processPrintShipments(YFSEnvironment env, Document getShipmentListOutputDoc, String strBatchNo,
			Document printPickTicDoc) throws Exception
	{
		// Declare document variable
		Document changeShipmentOutputDoc = null;
		// Declare element variable
		Element eleShipment = null;
		Element elechangeShipRoot = null;
		Element chngShpmntOutImpEle = null;
		String STR_GET_SHIPMENT_DETAILS_TEMPLATE = "<Shipment Createts=\"\" PickticketNo=\"\" ShipmentNo=\"\" SCAC=\"\" CarrierServiceCode=\"\" ShipNode=\"\" ShipmentKey=\"\"><ShipmentLines><ShipmentLine BackroomPickedQuantity=\"\" OrderNo=\"\" Quantity=\"\"><OrderLine OrderLineKey=\"\"><Item ItemID=\"\" ItemShortDesc=\"\"/></OrderLine></ShipmentLine></ShipmentLines></Shipment>";
		// Fetch the node list Shipment
		NodeList nlShipmentList = getShipmentListOutputDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);

		Element elePrintPickTic = printPickTicDoc.getDocumentElement();
		Element eleShipmentPickTic = printPickTicDoc.createElement(AcademyConstants.ELE_SHIPMENT_PICK_TICKET);
		elePrintPickTic.appendChild(eleShipmentPickTic);
		// Loop through each Shipment element
		for (int listIndex = 0; listIndex < nlShipmentList.getLength(); listIndex++)
		{
			// START: Prepare input for changeShipment API
			// Fetch the element Shipment
			eleShipment = (Element) nlShipmentList.item(listIndex);
			// Set attribute IsPickTicketPrinted=Y
			String isShipmentPrinted = eleShipment.getAttribute(AcademyConstants.ATTR_IS_PICKTICKET_PRINTED);
			
			//Set the template
			Document tempChangeShipDoc = YFCDocument.getDocumentFor(STR_GET_SHIPMENT_DETAILS_TEMPLATE).getDocument();
			//check if value is equal to Y
			if(isShipmentPrinted.equals(AcademyConstants.STR_YES))
			{
				//Remove the attribute IsPickTicketPrinted
				eleShipment.removeAttribute(AcademyConstants.ATTR_IS_PICKTICKET_PRINTED);
				//Set the template for getShipmentDetails API
				env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_DETAILS, tempChangeShipDoc);
				//Invoke the API getShipmentDetails
				changeShipmentOutputDoc = api.getShipmentDetails(env, XMLUtil.getDocumentForElement(eleShipment));
			}
			else
			{
				// setting the template
				env.setApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT, tempChangeShipDoc);
				//Set the attribute value of IsPickTicketPrinted
				eleShipment.setAttribute(AcademyConstants.ATTR_IS_PICKTICKET_PRINTED, AcademyConstants.STR_YES);
				//Set the attribute value of PickticketNo
				eleShipment.setAttribute(AcademyConstants.ATTR_PICK_TICKET_NO,strBatchNo);
				// END: Prepare input for changeShipment API
				// invoking the API changeShipment
				changeShipmentOutputDoc = api.changeShipment(env, XMLUtil.getDocumentForElement(eleShipment));
			}

			//Fetch the NodeList of Node ShipmentLine
			NodeList shipmentLineList = changeShipmentOutputDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			//Loop through each Shipment element
			for (int j = 0; j < shipmentLineList.getLength(); j++)
			{
				//Fetch the element ShipmentLine
				Element eleShipmentLine = (Element) shipmentLineList.item(j);
				//START: STL 1241

				//Fetch the element Item
				Element item = (Element)eleShipmentLine.getElementsByTagName(AcademyConstants.ITEM).item(0);
				/*//Set the attribute ItemBarCode
				item.setAttribute("ItemBarcode", generateBarcode(item.getAttribute("ItemID")));*/

				String strItemID = item.getAttribute(AcademyConstants.ATTR_ITEM_ID);

				String itemAliasValue = AcademyUtil.getItemAliasValueForItem(strItemID, env);
				if(!YFCObject.isVoid(itemAliasValue))
				{
					log.verbose("************ALIAS VALUE IS NOT NULL***********************");
					item.setAttribute(AcademyConstants.ATTR_UPC_CODE,itemAliasValue );
				}
				else
				{
					log.verbose("************ALIAS VALUE IS NULL***********************");
					item.setAttribute(AcademyConstants.ATTR_UPC_CODE, AcademySFSPrintPendingShipments.generateBarcode(strItemID));	
				}
				//END STL 1241
			}
			// Fetch the root element of changeShipment API output
			elechangeShipRoot = changeShipmentOutputDoc.getDocumentElement();
			// append the Shipment document to final response
			chngShpmntOutImpEle = (Element) printPickTicDoc.importNode(changeShipmentOutputDoc.getDocumentElement(), true);
			eleShipmentPickTic.appendChild(chngShpmntOutImpEle);
		}
	}
}