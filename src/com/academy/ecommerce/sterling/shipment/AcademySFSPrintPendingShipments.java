//package declaration
package com.academy.ecommerce.sterling.shipment;

//import statements
//java util import statements


import java.text.SimpleDateFormat;
import java.util.Calendar;
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
//BOPIS-1572::Begin
import com.sterlingcommerce.baseutil.SCXmlUtil;
//BOPIS-1572::End
//yantra import statements
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.core.YFCObject;

/**
 * Description: Class AcademySFSPrintPendingShipments gets the list of all
 * shipments that have IsPickTicketPrinted="N" and changes this attribute to "Y"
 *
 * @throws Exception
 */
public class AcademySFSPrintPendingShipments implements YIFCustomApi {
	// log set up
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySFSPrintPendingShipments.class);
	// Instance to store the properties configured for the condition in
	// Configurator
	private Properties props;

	// Stores property configured in configurator
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	// Api instance set up
	private static YIFApi api = null;
	static {
		try {
			api = YIFClientFactory.getInstance().getApi();
		} catch (YIFClientCreationException e) {
			e.printStackTrace();
		}
	}
	// BOPIS: 1303 : Begin
	// BOPIS-1175::begin - removed static
	String strIsWebStoreFlow = "N";
	String IsWebStoreBatch = "N";
	//BOPIS 2034- Start
	private int newIndex=1;
	private int totalNoOfPage=0;
	//BOPIS 2034- End
	// BOPIS-1175::end - removed static
	// BOPIS: 1303 : End




	// BOPIS-1175 - Start
	String sLoginID = "";

	// BOPIS-1175 - End
	/**
	 * Retrieves shipment list based on ShipNode and Statuses.
	 *
	 * @param env
	 *            Yantra Environment Context.
	 * @param inXML
	 *            Input Document Value.
	 * @return void
	 */
	public void getShipments(YFSEnvironment env, Document inXML) throws Exception {

		log.verbose("Input XML for getShipments" + XMLUtil.getXMLString(inXML));
		log.verbose("At the Start of getShipments : IsWebStoreBatch : " + IsWebStoreBatch);
		log.verbose("At the Start of getShipments : strIsWebStoreFlow : " + strIsWebStoreFlow);
		// START : STL-1440 Print pick ticket for MO device. generates batch number only
		// if required. for
		String strBatchNo = null;
		// String strBatchNo=generateBatchNumber(env,inXML);
		// END : STL-1440 Print pick ticket for MO device

		// Academy BOPIS : Print Management Change : Begin
		String strIsWebSOMbatch = "N";
		YFCDocument yfcInXML = YFCDocument.getDocumentFor(inXML);
		YFCElement eleRootYfcInXML = yfcInXML.getDocumentElement();
		log.verbose("Before checking Void : IsWebStoreBatch : " + IsWebStoreBatch);
		log.verbose("Before checking Void : strIsWebStoreFlow : " + strIsWebStoreFlow);
		String strIsStoreBatch = eleRootYfcInXML.getAttribute("IsStoreBatch");
		// BOPIS: 1303 : Begin
		String strIsWebStoreFlowCheck = eleRootYfcInXML.getAttribute(AcademyConstants.ATTR_WEB_STORE_FLOW);
		log.verbose("String strIsStoreBatch : " + strIsStoreBatch);
		log.verbose("String strIsWebStoreFlowCheck : " + strIsWebStoreFlowCheck);

		if (!YFCCommon.isVoid(strIsStoreBatch)) {
			IsWebStoreBatch = strIsStoreBatch;
		}
		// BOPIS-1472 : BEGIN
		if (!YFCCommon.isVoid(strIsWebStoreFlowCheck)) {
			strIsWebStoreFlow = strIsWebStoreFlowCheck;
		}
		// BOPIS: 1303 : End
		// BOPIS-1175 - Start
		sLoginID = eleRootYfcInXML.getElementsByTagName("Login").item(0).getAttribute("LoginID");
		// BOPIS-1175 - End

		log.verbose("Now Checking Initially : IsWebStoreBatch : " + IsWebStoreBatch);
		log.verbose("Now Checking Initially : strIsWebStoreFlow : " + strIsWebStoreFlow);
		log.verbose("Now Checking Initially : sLoginID : " + sLoginID);
		YFCDocument yfcInXmlShipList = null;
		if (YFCCommon.equalsIgnoreCase("Input", eleRootYfcInXML.getTagName())) {
			log.verbose("Within <Input> tag");
			strIsWebSOMbatch = "Y";
			YFCElement eleInDocPrint = eleRootYfcInXML.getChildElement("Print");

			// Changes made on 10/10/18 for BOPIS-1175 - Start
			strIsStoreBatch = eleInDocPrint.getAttribute("IsStoreBatch");
			if (!YFCCommon.isVoid(strIsStoreBatch)) {
				IsWebStoreBatch = strIsStoreBatch;
			}
			log.verbose("Now Checking Initially inside Input: IsWebStoreBatch : " + IsWebStoreBatch);
			// Changes made on 10/10/18 for BOPIS-1175 - End

			YFCElement eleInDocShipList = eleRootYfcInXML.getChildElement("Shipments");
			YFCDocument yfcInXmlPrint = YFCDocument.getDocumentFor(eleInDocPrint.toString());
			yfcInXmlShipList = YFCDocument.getDocumentFor(eleInDocShipList.toString());
			inXML = yfcInXmlPrint.getDocument();
			strBatchNo = eleInDocPrint.getAttribute("BatchNo");
		}
		// Academy BOPIS : Print Management Change : End

		// Fetch the root element of the input
		Element elePrint = inXML.getDocumentElement();
		// Fetch the attribute value of PickticketNo
		String pickticketNo = elePrint.getAttribute("PickticketNo").trim();
		// Fetch the attribute value of ShipNode
		String strShipNode = elePrint.getAttribute("ShipNode");
		// Fetch the attribute value of ShipmentKey
		String strShipmentKey = elePrint.getAttribute("ShipmentKey");
		// Fetch the attribute value of MAX_RECORD from the service argument
		String strMaximumRecords = elePrint.getAttribute("MaximumRecords");
		// Fetch the attribute value of PageNo

		// START : STL-1440 Print pick ticket for MO device
		if (YFCObject.isVoid(pickticketNo) && YFCObject.isVoid(strShipmentKey)
				&& !YFCCommon.equalsIgnoreCase("Y", strIsWebSOMbatch)) {
			// invoke method generateBatchNumber to generate a batch number
			// String strBatchNo=generateBatchNumber(env);
			strBatchNo = generateBatchNumber(env, inXML);
		}
		// END : STL-1440 Print pick ticket for MO device

		// SFS2.0 001 Printer Id start
		String strIsPickTicketPrinted = elePrint.getAttribute("PickTicketPrinted");
		env.setTxnObject("PickTicketPrinted", strIsPickTicketPrinted);
		log.verbose(" IsPickTicketPrinted is :" + strIsPickTicketPrinted);
		String strPrinterId = elePrint.getAttribute("PrinterID");
		String ShipmentPickTicketId = "";
		Document inCommonCodeDoc = XMLUtil.createDocument("CommonCode");
		Element inComElem = inCommonCodeDoc.getDocumentElement();
		inComElem.setAttribute("CodeType", strPrinterId);
		Document outCommDoc = AcademyUtil.invokeAPI(env, "getCommonCodeList", inCommonCodeDoc);
		if (!YFCObject.isVoid(outCommDoc)) {
			Element outCommElem = outCommDoc.getDocumentElement();
			NodeList CommonCodeList = XMLUtil.getNodeList(outCommElem, "CommonCode");
			if (!YFCObject.isVoid(CommonCodeList)) {
				int iLength = CommonCodeList.getLength();
				for (int k = 0; k < iLength; k++) {
					Element CommonCode = (Element) CommonCodeList.item(k);
					String codevalue = CommonCode.getAttribute("CodeValue");
					if (codevalue.contains("STORE_SHIP")) {
						ShipmentPickTicketId = CommonCode.getAttribute("CodeValue");
						env.setTxnObject("ShipmentPickTicketPrinterId", ShipmentPickTicketId);
						log.verbose(" ShipmentPickTicketPrinterId is :" + ShipmentPickTicketId);
					}
				}
			}
		}
		log.endTimer(" End of getCommonCodeList To get Printer Id-> getCommonCodeList Api");

		// SFS2.0 001 Printer Id end

		HashMap<String, String> pageNos = AcademySFSProcessPrintItemPickTickets
				.getPageNumbers(elePrint.getAttribute("PageNo"));

		// Check if ShipmentKey is blank
		if (!strShipmentKey.trim().equals("")) {
			log.verbose("Inside ShipmentKey not null condition");
			// Create Document
			Document getShipmentListOutputDoc = YFCDocument.getDocumentFor("<Shipments><Shipment /></Shipments>")
					.getDocument();
			// Fetch the element Shipment
			Element shipment = (Element) getShipmentListOutputDoc.getElementsByTagName("Shipment").item(0);
			// Set attribute value of ShipmentKey
			shipment.setAttribute("ShipmentKey", strShipmentKey);
			// Set attribute value of PickTicketPrinted
			if (!YFCCommon.isVoid(strIsPickTicketPrinted)) {
				shipment.setAttribute("PickTicketPrinted", strIsPickTicketPrinted);
			} else {
				shipment.setAttribute("PickTicketPrinted", "Y");
			}
			// Set attribute value of SingleShipment
			shipment.setAttribute("SingleShipment", "Y");
			// call methos processPrintShipment

			log.verbose("Invoking processPrintShipments IsWebStoreBatch : " + IsWebStoreBatch);
			log.verbose("Invoking processPrintShipments strIsWebStoreFlow : " + strIsWebStoreFlow);

			processPrintShipments(env, getShipmentListOutputDoc, strBatchNo, pageNos, strIsStoreBatch);
			return;
		}
		// Check for empty record
		if (strMaximumRecords.equals("") && pickticketNo.equals("")) {
			// Fetch the value of MaximunRecord from the service argument
			strMaximumRecords = props.getProperty(AcademyConstants.KEY_MAX_RECORD);
		}
		// Check if value is not empty
		if (!pickticketNo.equals("")) {
			// Set the value in a variable
			strBatchNo = pickticketNo;
		}

		// invoke method to fetch shipment list
		// Academy BOPIS : Print Management Change : Begin
		Document getShipmentListOutputDoc = null;
		if (!YFCCommon.equalsIgnoreCase("Y", strIsWebSOMbatch)) {
			// BOPIS-1175::Begin
			props.put("IsWebStoreBatch", IsWebStoreBatch);
			props.put("strIsWebStoreFlow", strIsWebStoreFlow);
			//// BOPIS-1175::End
			getShipmentListOutputDoc = getShipmentListOutput(env, strShipNode, pickticketNo, strMaximumRecords, props);
		} else {
			getShipmentListOutputDoc = yfcInXmlShipList.getDocument();
		}
		// Academy BOPIS : Print Management Change : End
		// invoke method processShipments to change the attribute
		// IsPickTicketPrinted="N"
		// Academy BOPIS : Print Management Change : Begin
		////BOPIS-1572::begin- Calling New method processPrintShipmentsForNewStoreBatch() for the potential new batch from Web store. 
		//This new method will print Item Pick Ticket and then Shipment Pick ticket for each shipment.
		if (pickticketNo.equals("") && AcademyConstants.STR_YES.equals(IsWebStoreBatch)){

			processPrintShipmentsForNewStoreBatch(env, getShipmentListOutputDoc, strBatchNo, pageNos, inXML);
		} else{
			//BOPIS-1572::end
			processPrintShipments(env, getShipmentListOutputDoc, strBatchNo, pageNos, strIsStoreBatch);
		}
		// Academy BOPIS : Print Management Change : End
		// Check for empty value
		//BOPIS-1572::Begin- Adding a condition is not a webstore batch as for a webstore 
		//new batch already item pick ticket is printed in processPrintShipmentsForNewStoreBatch()
		if (pickticketNo.equals("") && !AcademyConstants.STR_YES.equals(IsWebStoreBatch)) {
			////BOPIS-1572::End
			// Set the attribute value of PickticketNo
			elePrint.setAttribute("PickticketNo", strBatchNo);
			// Set the attribute value of MaximumRecord
			elePrint.setAttribute(AcademyConstants.ATTR_MAX_RECORD, "");
			// Invoke service
			log.verbose("Now Printing Item Pick ticket for potential new Batch");
			AcademyUtil.invokeService(env, AcademyConstants.PRINT_PICK_TICKETS_SERVICE, inXML);
		}


	}

	/**
	 * This Method is created as a part of BOPIS-1572. To reverse the sequence of printing. 
	 * This Method will be called only for a Web store New Batch. For existing Batch its taken care in another class. 
	 * This Method will loop through each Shipment and call changeShipment API. And then it calls Service for printing Item Pick Ticket. 
	 * And then Once again it loops through each shipment and calls getShipmentList to get the updated details and calls service to print Shipment Pick Ticket.
	 * @param env
	 * @param getShipmentListOutputDoc
	 * @param strBatchNo
	 * @param pageNos
	 * @param inXML
	 * @throws Exception
	 */
	private void processPrintShipmentsForNewStoreBatch(YFSEnvironment env,
			Document getShipmentListOutputDoc, String strBatchNo,
			HashMap<String, String> pageNos, Document inXML) throws Exception{

		log.verbose("Inside processPrintShipmentsForNewStoreBatch");
		log.verbose("Inside processPrintShipmentsForNewStoreBatch IsWebStoreBatch : " + IsWebStoreBatch);
		log.verbose("Inside processPrintShipmentsForNewStoreBatch strIsWebStoreFlow : " + strIsWebStoreFlow);

		// Fetch the node list Shipment
		NodeList nlShipmentList = getShipmentListOutputDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
		
		String strPageSize = props.getProperty("PageSize");
		Double dPageSizenew=Double.valueOf(strPageSize);
		// Loop through each Shipment element
		for (int listIndex = 0; listIndex < nlShipmentList.getLength(); listIndex++) {

			// START: Prepare input for changeShipment API
			// Fetch the element Shipment
			int noofShipmentLine=0;
			Element eleShipment = (Element) nlShipmentList.item(listIndex);

			// Changes made on 10/10/18 for BOPIS-1175 - Start
			String strShipmentKey = eleShipment.getAttribute("ShipmentKey");
			// Changes made on 10/10/18 for BOPIS-1175 - End
			log.verbose("ShipmentKey is" + strShipmentKey);
			// Set attribute IsPickTicketPrinted=Y


			// Set the template
			//			Document tempChangeShipDoc = YFCDocument.getDocumentFor(
			//					"<Shipment Status=\"\" CarrierServiceCode=\"\" PickTicketPrinted=\"\" PickticketNo=\"\" SCAC=\"\" ShipNode=\"\" ShipmentKey=\"\" ShipmentNo=\"\"><ShipmentLines>   <ShipmentLine OrderNo=\"\" Quantity=\"\" ShipmentLineKey=\"\" ShipmentLineNo=\"\" ><Extn ExtnPogId=\"\" ExtnDepartment=\"\" ExtnSection=\"\" ExtnPogNumber=\"\" ExtnLiveDate=\"\" ExtnPlanogramStatus=\"\" /><OrderLine OrderLineKey=\"\"><Item ItemID=\"\" ItemShortDesc=\"\" ProductClass=\"\" UnitOfMeasure=\"\"/><ItemDetails ItemID=\"\" UnitOfMeasure=\"\"><Extn ExtnStyle=\"\" ExtnSizeCodeDescription=\"\" ExtnVendorColorName=\"\" ExtnImageLocalPath=\"\" ExtnItemImageName=\"\" />      <PrimaryInformation DefaultProductClass=\"\" /></ItemDetails></OrderLine></ShipmentLine></ShipmentLines></Shipment>")
			//					.getDocument();
			Document tempChangeShipDoc = YFCDocument.getDocumentFor(
					"<Shipment ShipmentKey=\"\" ><ShipmentLines><ShipmentLine ShipmentLineKey=\"\" /></ShipmentLines></Shipment>")
					.getDocument();

			// Changes for BOPIS-1175 - End
			// setting the template
			env.setApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT, tempChangeShipDoc);
			// Set the attribute value of IsPickTicketPrinted
			eleShipment.setAttribute("PickTicketPrinted", "Y");
			// Set the attribute value of PickticketNo
			if (!YFCCommon.isVoid(strBatchNo)) {
				eleShipment.setAttribute(AcademyConstants.ATTR_PICK_TICKET_NO, strBatchNo);
			}
			// END: Prepare input for changeShipment API
			// invoking the API changeShipment
			Document changeShipmentOutdoc=api.changeShipment(env, XMLUtil.getDocumentForElement(eleShipment));
			
		   //BOPIS 2034- Start
			Element elechangeShipment=changeShipmentOutdoc.getDocumentElement();
			NodeList nlShipmentLine = elechangeShipment.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			noofShipmentLine=nlShipmentLine.getLength();
			double dnoofShipmentLine= Double.parseDouble(String.valueOf(noofShipmentLine));
			double dnoofpageeachShipment=Math.ceil(dnoofShipmentLine/dPageSizenew);
			int noofpageeachShipment=(int)dnoofpageeachShipment;

			totalNoOfPage=totalNoOfPage+noofpageeachShipment;
			//BOPIS 2034 -End
			
		}
		
		//strtotalNoOfPage=String.valueOf(totalNoOfPage);
		
		log.verbose("ChangeShipment api call done for all shipments in the Batch!");
		//BOPIS-1572::Begin- Printing Item Pick Ticket
		Element elePrint = inXML.getDocumentElement();
		elePrint.setAttribute("PickticketNo", strBatchNo);
		// Set the attribute value of MaximumRecord
		elePrint.setAttribute(AcademyConstants.ATTR_MAX_RECORD, "");
		// Invoke service
		log.verbose("Now Printing Item Pick ticket for potential new Batch");
		AcademyUtil.invokeService(env, AcademyConstants.PRINT_PICK_TICKETS_SERVICE, inXML);

		//Now loop through each shipment and print Shipment Pick Ticket.
		for (int listIndex = 0; listIndex < nlShipmentList.getLength(); listIndex++){
			// Academy BOPIS : Print Management Change : Begin
			// Invoke planogram service and change shipment.
			// BOPIS:1303 - Begin
			Element elechangeShipRoot = null;
			Element eleShipment = (Element) nlShipmentList.item(listIndex);
			Document tempGetShipLstDoc = YFCDocument.getDocumentFor(
					"<Shipments><Shipment Status=\"\" CarrierServiceCode=\"\" PickTicketPrinted=\"\" PickticketNo=\"\" SCAC=\"\" ShipNode=\"\" ShipmentKey=\"\" ShipmentNo=\"\"><ShipmentLines>   <ShipmentLine OrderNo=\"\" Quantity=\"\" ShipmentLineKey=\"\" ShipmentLineNo=\"\" ><Extn ExtnPogId=\"\" ExtnDepartment=\"\" ExtnSection=\"\" ExtnPogNumber=\"\" ExtnLiveDate=\"\" ExtnPlanogramStatus=\"\" /><OrderLine OrderLineKey=\"\"><Item ItemID=\"\" ItemShortDesc=\"\" ProductClass=\"\" UnitOfMeasure=\"\"/><ItemDetails ItemID=\"\" UnitOfMeasure=\"\"><Extn ExtnStyle=\"\" ExtnSizeCodeDescription=\"\" ExtnVendorColorName=\"\" ExtnImageLocalPath=\"\" ExtnItemImageName=\"\" />      <PrimaryInformation DefaultProductClass=\"\" /></ItemDetails></OrderLine></ShipmentLine></ShipmentLines></Shipment></Shipments>")
					.getDocument();
			env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, tempGetShipLstDoc);
			// Invoke the API getShipmentDetails
			Document getShipmentDetailsOutputDoc = api.getShipmentList(env, XMLUtil.getDocumentForElement(eleShipment));
			Document shipmentDetailsDoc = XMLUtil.getDocumentForElement(SCXmlUtil.getChildElement(getShipmentDetailsOutputDoc.getDocumentElement(), AcademyConstants.ELE_SHIPMENT));
			log.debug("AcademySFSPrintPendingShipments.WebStoreFlow" + strIsWebStoreFlow);
			log.debug("AcademySFSPrintPendingShipments.WebStoreFlow. Calling Planogram Service");
			shipmentDetailsDoc = callPlanogmAndChngeShipment(env, shipmentDetailsDoc, AcademyConstants.STR_YES);
			// BOPIS:1303 - End

			// Academy BOPIS : Print Management Change : End
			// Fetch the NodeList of Node ShipmentLine
			NodeList shipmentLineList = shipmentDetailsDoc
					.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

			// Academy BOPIS : Print Management Change : Begin
			int dShpLines = shipmentLineList.getLength();
			// Academy BOPIS : Print Management Change : End
			// Loop through each Shipment element
			for (int j = 0; j < shipmentLineList.getLength(); j++) {
				// Fetch the element ShipmentLine
				Element eleShipmentLine = (Element) shipmentLineList.item(j);
				// START: STL 1241

				// Fetch the element Item
				Element item = (Element) eleShipmentLine.getElementsByTagName(AcademyConstants.ITEM).item(0);
				/*
				 * //Set the attribute ItemBarCode item.setAttribute("ItemBarcode",
				 * generateBarcode(item.getAttribute("ItemID")));
				 */

				String strrItemID = item.getAttribute("ItemID");

				String itemAliasValue = AcademyUtil.getItemAliasValueForItem(strrItemID, env);
				if (!YFCObject.isVoid(itemAliasValue)) {
					log.verbose("************ALIAS VALUE IS NOT NULL***********************");
					item.setAttribute(AcademyConstants.ATTR_ITEM_BARCODE, itemAliasValue);
				} else {
					log.verbose("************ALIAS VALUE IS NULL***********************");
					item.setAttribute(AcademyConstants.ATTR_ITEM_BARCODE,
							AcademySFSPrintPendingShipments.generateBarcode(strrItemID));
				}
				// END STL 1241
			}
			// Fetch the root element of changeShipment API output
			elechangeShipRoot = shipmentDetailsDoc.getDocumentElement();
			// set the attribute PrinterId
			elechangeShipRoot.setAttribute(AcademyConstants.ATTR_PRINTER_ID,
					props.getProperty(AcademyConstants.KEY_PRINTER_ID_VALUE));
			// set the attribute BeforeChildrenPrintDocumentId
			elechangeShipRoot.setAttribute(AcademyConstants.ATTR_DOCUMENT_ID,
					props.getProperty(AcademyConstants.KEY_DOCUMENT_ID_VALUE));
			// Increment the index and store it to PageNo
			int pageNo = listIndex + 1;
			String strpageNo=newIndex +" of "+totalNoOfPage;
			// Check if value is equal to Y

			// Set the attribute value of PageNumber
			elechangeShipRoot.setAttribute("PageNumber", "" + strpageNo);
			log.verbose("Now Printing Shipment Pick Ticket for Shipment Key:"+ eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
			// Validation of the pageNos value
			if (pageNos.isEmpty() || pageNos.containsKey(pageNo + "")) {
				// Academy BOPIS : Print Management Change : Begin
				strPageSize = props.getProperty("PageSize");
				int dPageSize = Integer.parseInt(strPageSize);
				if (dShpLines > dPageSize) {
					// call method to create pagination for the input doc.
					callPrintServ(env, shipmentDetailsDoc, dShpLines, dPageSize);
				} else {
					AcademyUtil.invokeService(env, AcademyConstants.SERV_PRINT_PEND_SHIP, shipmentDetailsDoc);
					newIndex++;
				}
				// Academy BOPIS : Print Management Change : End
			}
		}
	}

	// BOPIS-1472 : BEGIN
	/**
	 * Retrieves shipment list based on ShipNode and pickticketNo and max records.
	 *
	 * @param env
	 *            Yantra Environment Context.
	 * @param strShipNode
	 *            ShipNode against which shipment exists.
	 * @param strMaximumRecords
	 *            Maximum shipment records required to be fetched.
	 * @return void
	 */
	public static Document getShipmentListOutput(YFSEnvironment env, String strShipNode, String pickticketNo,
			String strMaximumRecords, Properties props) throws Exception {
		// Declare Document variable
		Document getShipmentListInputDoc = null;
		int totalNoOfPageforexistingbatch=0;
		// Declare element variable
		Element eleRootElement = null;
		String strSortBy = null;
		// BOPIS-1175::Begin
		String IsWebStoreBatch = "N";
		if (!YFCCommon.isVoid(props.get("IsWebStoreBatch"))) {
			IsWebStoreBatch = props.getProperty("IsWebStoreBatch");
		}
		String strIsWebStoreFlow = "N";
		if (!YFCCommon.isVoid(props.get("strIsWebStoreFlow"))) {
			strIsWebStoreFlow = props.getProperty("strIsWebStoreFlow");
		}
		// BOPIS-1175::End
		// START: Preparing input document for getShipmentList API

		// XML structure for getShipmentList API
		// <Shipment MaximumRecords="" ShipNode="" Status="" IsPickTicketPrinted="N">
		// <Extn>
		// <OrderBy>
		// <Attribute Name="ExtnSCACPriority" />
		// </OrderBy>
		// </Extn>
		// </Shipment>

		// Create the root element Shipment
		getShipmentListInputDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleRootElement = getShipmentListInputDoc.getDocumentElement();
		// Setting the values for attribute Status, IsPickTicketPrinted and
		// ShipNode
		// BOPIS: 1303: Begin
		if (YFCCommon.equalsIgnoreCase("N", strIsWebStoreFlow) && YFCCommon.equalsIgnoreCase("N", IsWebStoreBatch)) {
			eleRootElement.setAttribute(AcademyConstants.ATTR_STATUS, AcademyConstants.VAL_SHIP_STATUS);
		}
		// BOPIS: 1303 : End
		// eleRootElement.setAttribute(AcademyConstants.ATTR_STATUS,
		// AcademyConstants.VAL_SHIP_STATUS);
		eleRootElement.setAttribute(AcademyConstants.SHIP_NODE, strShipNode);
		// Check for empty value
		if (pickticketNo.equals("")) {
			// Set the attribute MaximunRecord
			eleRootElement.setAttribute(AcademyConstants.ATTR_MAX_RECORD, strMaximumRecords);
			// Set the attribute IsPickTickjetPrinted
			eleRootElement.setAttribute("PickTicketPrinted", "Y");
			eleRootElement.setAttribute("PickTicketPrintedQryType", "NE");
		} else {
			// Set the attribute for IsPickTickjetPrinted
			eleRootElement.setAttribute(AcademyConstants.ATTR_PICK_TICKET_NO, pickticketNo);
		}

		// Creating element OrderBy
		Element eleOrderBy = getShipmentListInputDoc.createElement(AcademyConstants.ELE_ORDERBY);
		eleRootElement.appendChild(eleOrderBy);

		// Creating element Attribute
		Element eleAttribute = getShipmentListInputDoc.createElement(AcademyConstants.ELE_ATTRIBUTE);
		// Setting the attribute value of Name
		eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.COL_EXTN_SCAC_PRIORITY_SORT);
		// Append child element
		eleOrderBy.appendChild(eleAttribute);
		// Create element Attribute
		eleAttribute = getShipmentListInputDoc.createElement(AcademyConstants.ELE_ATTRIBUTE);
		// Setting the attribute value of Name

		// START STL-1722 Print Tickets should sort by order date not shipment date when
		// printed
		// eleAttribute.setAttribute(AcademyConstants.ATTR_NAME,
		// AcademyConstants.ATTR_SHIPMENT_NO);
		strSortBy = props.getProperty(AcademyConstants.STR_SORT_BY);
		eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, strSortBy);
		// END STL-1722 Print Tickets should sort by order date not shipment date when
		// printed

		// Append the child element
		eleOrderBy.appendChild(eleAttribute);
		// END: Preparing input document for getShipmentList API

		// creating the output template for getShipmentList API
		Document templateDoc = YFCDocument
				.getDocumentFor("<Shipments><Shipment ShipmentKey=\"\" PickTicketPrinted=\"\"><ShipmentLines><ShipmentLine ShipmentLineKey=\"\"/></ShipmentLines></Shipment></Shipments>")
				.getDocument();
		// setting the output template
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, templateDoc);
		// invoking the API getShipmentList
		log.debug("AcademySFSPrintPendingShipments.getShipmentListOutput().input" + getShipmentListInputDoc);
		Document getShipmentListOutputDoc = api.getShipmentList(env, getShipmentListInputDoc);
		log.debug("AcademySFSPrintPendingShipments.getShipmentListOutput().output" + getShipmentListOutputDoc);
		// return thr output shipment document
		//BOPIS 2034- Start
		Element elegetShipmentListOutputdoc=getShipmentListOutputDoc.getDocumentElement();
		if(!YFCObject.isVoid(elegetShipmentListOutputdoc)){
			int noofShipmentLine=0;
			String strPageSize = props.getProperty("PageSize");
			Double dPageSizenew=Double.valueOf(strPageSize);
			NodeList nlShipmentList = elegetShipmentListOutputdoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
			  for(int i=0; i<nlShipmentList.getLength();i++){
			    	Element eleShipmentList = (Element) nlShipmentList.item(i);
			    	 NodeList nlShipmentLineList = eleShipmentList.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			    	 noofShipmentLine=nlShipmentLineList.getLength();
			    	 double dnoofShipmentLine= Double.parseDouble(String.valueOf(noofShipmentLine));
					 double dnoofpageeachShipment=Math.ceil(dnoofShipmentLine/dPageSizenew);
					 int noofpageeachShipment=(int)dnoofpageeachShipment;
					 totalNoOfPageforexistingbatch=totalNoOfPageforexistingbatch+noofpageeachShipment;
					 SCXmlUtil.removeNode(SCXmlUtil.getChildElement(eleShipmentList, "ShipmentLines"));
			  }
			  
		}
		elegetShipmentListOutputdoc.setAttribute(AcademyConstants.ATTR_TOTAL_NO_OF_PAGES, String.valueOf(totalNoOfPageforexistingbatch));
		
		//BOPIS 2034 - End
		return getShipmentListOutputDoc;
	}

	/**
	 * /**Modified as part of STL-1440. Generates the batch number in the format
	 * "user login ID"+"_"+"yyyyMMddHHmm"
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 * @return sCurrentDate String which returns the date in the mentioned format
	 */
	public static String generateBatchNumber(YFSEnvironment env, Document inXml) {
		String strBatchNumber = null;
		Element eleLogin = (Element) inXml.getElementsByTagName(AcademyConstants.ELE_LOGIN).item(0);
		// specifying the format of the date
		String strBatchDateFormat = AcademyConstants.BATCH_DATE_FORMAT;
		SimpleDateFormat sDateFormat = new SimpleDateFormat(strBatchDateFormat);
		// initialize calender
		Calendar cal = Calendar.getInstance();
		// Fetch the current date in required Date format
		String sCurrentDate = sDateFormat.format(cal.getTime());
		if (!YFCObject.isVoid(eleLogin)) {
			String strLoginID = eleLogin.getAttribute(AcademyConstants.ATTR_LOGIN_ID);
			strBatchNumber = strLoginID.concat(AcademyConstants.STR_UNDERSCORE).concat(sCurrentDate);
		} else {
			strBatchNumber = sCurrentDate;
		}
		// Print the verbose log
		if (log.isVerboseEnabled()) {
			log.verbose("strBatchNumber-->" + strBatchNumber);
		}
		// Return date
		return strBatchNumber;
	}

	/**
	 * Generates the ItemBarCode starting with 4
	 * 
	 * @param String
	 *            ItemID.
	 * @return String value
	 */

	public static String generateBarcode(String itemId) {
		// Append 4 to the ItemID value
		// Append 40 to the ItemID value-Nexus STL-697
		itemId = "40" + itemId;
		// Fetch the length of the itemId.
		char[] itemDigits = itemId.toCharArray();
		int checkSum = 0;
		// Loop through each digits
		for (int i = 0; i < itemDigits.length; i += 2) {
			// Multiply by 3
			checkSum += Integer.parseInt("" + itemDigits[i]) * 3;
		}
		// Loop through itemdigit array
		for (int i = 1; i < itemDigits.length; i += 2) {
			// Sum the value
			checkSum += Integer.parseInt("" + itemDigits[i]);

		}
		checkSum = checkSum % 10;
		// check for value greater than zero
		if (checkSum > 0) {
			// Subtract by 10
			checkSum = 10 - checkSum;
		}
		// return the calculated sum
		return itemId + checkSum;
	}

	/**
	 * Changes the attribute IsPickTicketPrinted="Y" for each shipment by calling
	 * changeShipmentAPI
	 *
	 * @param env
	 *            Yantra Environment Context.
	 * @param getShipmentListOutputDoc
	 *            Document containing output of getShipmentList API which has a list
	 *            of shipments.
	 * @return void
	 */

	private void processPrintShipments(YFSEnvironment env, Document getShipmentListOutputDoc, String strBatchNo,
			HashMap<String, String> pageNos, String strIsStoreBatch) throws Exception {
		log.verbose("Inside processPrintShipments");
		log.verbose("Inside processPrintShipments IsWebStoreBatch : " + IsWebStoreBatch);
		log.verbose("Inside processPrintShipments strIsWebStoreFlow : " + strIsWebStoreFlow);

		// Declare document variable
		Document changeShipmentOutputDoc = null;
		String strPageSize = props.getProperty("PageSize");
		// Declare element variable
		Element eleShipment = null;
		Element elechangeShipRoot = null;
		/*OMNI-52430 Fix for Print Pick Ticket for STS shipment in search screen - START */
		String strShipmentDocumentType=null;
		/*OMNI-52430 Fix for Print Pick Ticket for STS shipment in search screen - END */

		// Fetch the node list Shipment
		//BOPIS 2034- Start
		String strtotalNoOfPage=getShipmentListOutputDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_TOTAL_NO_OF_PAGES);
		if(!YFCObject.isVoid(strtotalNoOfPage)){
			totalNoOfPage=Integer.valueOf(strtotalNoOfPage);
		}
		
		//BOPIS 2034 - End
		
		NodeList nlShipmentList = getShipmentListOutputDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);

		// Loop through each Shipment element
		for (int listIndex = 0; listIndex < nlShipmentList.getLength(); listIndex++) {
			int noofShipmentLine=0;
			// START: Prepare input for changeShipment API
			// Fetch the element Shipment
			eleShipment = (Element) nlShipmentList.item(listIndex);

			// Changes made on 10/10/18 for BOPIS-1175 - Start

			String strShipmentKey = eleShipment.getAttribute("ShipmentKey");

			// Changes made on 10/10/18 for BOPIS-1175 - End
			log.verbose("ShipmentKey is" + strShipmentKey);
			// Set attribute IsPickTicketPrinted=Y
			String isShipmentPrinted = eleShipment.getAttribute(AcademyConstants.ATTR_IS_PICKTICKET_PRINTED);
			log.verbose("isShipmentPrinted : " + isShipmentPrinted);
			// Set attribute SingleShipment=Y
			String singleShipment = eleShipment.getAttribute("SingleShipment");

			// Set the template
			Document tempChangeShipDoc = YFCDocument.getDocumentFor(
					"<Shipment Status=\"\" CarrierServiceCode=\"\" PickTicketPrinted=\"\" PickticketNo=\"\" SCAC=\"\" ShipNode=\"\" ShipmentKey=\"\" ShipmentNo=\"\" DocumentType=\"\"><ShipmentLines>   <ShipmentLine OrderNo=\"\" Quantity=\"\" ShipmentLineKey=\"\" ShipmentLineNo=\"\" ><Extn ExtnPogId=\"\" ExtnDepartment=\"\" ExtnSection=\"\" ExtnPogNumber=\"\" ExtnLiveDate=\"\" ExtnPlanogramStatus=\"\" /><OrderLine OrderLineKey=\"\"><Item ItemID=\"\" ItemShortDesc=\"\" ProductClass=\"\" UnitOfMeasure=\"\"/><ItemDetails ItemID=\"\" UnitOfMeasure=\"\"><Extn ExtnStyle=\"\" ExtnSizeCodeDescription=\"\" ExtnVendorColorName=\"\" ExtnImageLocalPath=\"\" ExtnItemImageName=\"\" />      <PrimaryInformation DefaultProductClass=\"\" /></ItemDetails></OrderLine></ShipmentLine></ShipmentLines></Shipment>")
					.getDocument();
			// check if value is equal to Y
			String strShipmentStatus = null;
			if (isShipmentPrinted.equals("Y")) {
				log.verbose("Inside isShipmentPrinted.equals(\"Y\")");
				log.verbose("IsWebStoreBatch : " + IsWebStoreBatch);
				log.verbose("strIsWebStoreFlow : " + strIsWebStoreFlow);
				// Remove the attribute IsPickTicketPrinted
				eleShipment.removeAttribute("PickTicketPrinted");
				// Set the template for getShipmentDetails API
				log.verbose("template doc>>>"+XMLUtil.getXMLString(tempChangeShipDoc));
				env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_DETAILS, tempChangeShipDoc);
				// Invoke the API getShipmentDetails
				changeShipmentOutputDoc = api.getShipmentDetails(env, XMLUtil.getDocumentForElement(eleShipment));
				log.verbose("shipment detail output doc>>>"+XMLUtil.getXMLString(changeShipmentOutputDoc));

				// Changes for BOPIS-1175 - Start
				// Check if this is for Individual Shipment and Not Batch
				if (IsWebStoreBatch.equalsIgnoreCase(AcademyConstants.STR_NO)
						&& strIsWebStoreFlow.equalsIgnoreCase(AcademyConstants.STR_YES)) {
					log.verbose("isShipmentPrinted.equals(\"Y\") Flag Passed");
					strShipmentStatus = changeShipmentOutputDoc.getDocumentElement().getAttribute("Status");
					//BOPIS 2034- Start
					Double dPageSizenew=Double.valueOf(strPageSize);
					Element elechangeShipmentOutputDoc=changeShipmentOutputDoc.getDocumentElement();
					NodeList nlShipmentLineList = elechangeShipmentOutputDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
					noofShipmentLine=nlShipmentLineList.getLength();
					double dnoofShipmentLine= Double.parseDouble(String.valueOf(noofShipmentLine));
					double dnoofpageeachShipment=Math.ceil(dnoofShipmentLine/dPageSizenew);
					int noofpageeachShipment=(int)dnoofpageeachShipment;
                    totalNoOfPage=totalNoOfPage+noofpageeachShipment;
					//BOPIS 2034- End
                    
					log.verbose("strShipmentStatus : " + strShipmentStatus);
					if (strShipmentStatus.equals(AcademyConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL)) {
						log.verbose("isShipmentPrinted.equals(\"Y\") Status Passed");
						env.setApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT, tempChangeShipDoc);
						// Set the attribute value of IsPickTicketPrinted
						eleShipment.setAttribute("PickTicketPrinted", "Y");
						log.verbose("strLoginID : " + sLoginID);
						if (!YFCCommon.isVoid(sLoginID)) {
							eleShipment.setAttribute(AcademyConstants.ATTR_ASSIGNED_TO_USER_ID, sLoginID);
						}
						// END: Prepare input for changeShipment API
						// invoking the API changeShipmentto update AssignUserID
						changeShipmentOutputDoc = api.changeShipment(env, XMLUtil.getDocumentForElement(eleShipment));

						log.verbose("ShipmentKey : " + strShipmentKey);

						Document docChangeShipmentStatus = XMLUtil.createDocument("Shipment");
						// Fetch the element Shipment
						Element elemShipment = (Element) docChangeShipmentStatus.getDocumentElement();
						elemShipment.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
						elemShipment.setAttribute(AcademyConstants.ATTR_SELL_ORG_CODE,
								AcademyConstants.ENTERPRISE_CODE_SHIPMENT);
						elemShipment.setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS, "1100.70.06.20");
						/*OMNI-52430 Fix for Print Pick Ticket for STS shipment in search screen - START */
						strShipmentDocumentType=elechangeShipmentOutputDoc.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
						log.verbose("document Type : " + strShipmentDocumentType);
						if (!YFCCommon.isVoid(strShipmentDocumentType)&& strShipmentDocumentType.equals(AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE)) {
							elemShipment.setAttribute(AcademyConstants.ATTR_TRANSID, AcademyConstants.TRAN_YCD_BACKROOM_PICK_IN_PROGRESS_0006);
						}
						else {
							elemShipment.setAttribute(AcademyConstants.ATTR_TRANSID, "YCD_BACKROOM_PICK_IN_PROGRESS");
						}
						/*OMNI-52430 Fix for Print Pick Ticket for STS shipment in search screen - END */

						Document tempChangeShipStatusDoc = YFCDocument.getDocumentFor(
								"<Shipment Status=\"\" CarrierServiceCode=\"\" PickTicketPrinted=\"\" PickticketNo=\"\" "
										+ "SCAC=\"\" ShipNode=\"\" ShipmentKey=\"\" ShipmentNo=\"\" />")
										.getDocument();

						// invoke changeShipmentStatus to update Status to Backroom pick in Progress.
						env.setApiTemplate(AcademyConstants.CHANGE_SHIPMENT_STATUS, tempChangeShipStatusDoc);
						api.changeShipmentStatus(env, docChangeShipmentStatus);
						log.verbose(XMLUtil.getXMLString(docChangeShipmentStatus));
						log.verbose(XMLUtil.getXMLString(docChangeShipmentStatus));
						env.clearApiTemplate(AcademyConstants.CHANGE_SHIPMENT_STATUS);

					}
				}
			} // Changes for BOPIS-1175 - End
			else {
				log.verbose("Inside PickTicketPrinted=N");
				log.verbose("Inside PickTicketPrinted=N");
				log.verbose("Inside PickTicketPrinted=N IsWebStoreBatch : " + IsWebStoreBatch);
				log.verbose("Inside PickTicketPrinted=N strIsWebStoreFlow : " + strIsWebStoreFlow);
				// Changes for BOPIS-1175 - Start
				// Check if this is for Individual Shipment and Not Batch

				log.verbose("IsWebStoreBatch : " + IsWebStoreBatch);
				log.verbose("strIsWebStoreFlow : " + strIsWebStoreFlow);
				if (IsWebStoreBatch.equalsIgnoreCase(AcademyConstants.STR_NO)
						&& strIsWebStoreFlow.equalsIgnoreCase(AcademyConstants.STR_YES)) {

					env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_DETAILS, tempChangeShipDoc);
					// Invoke the API getShipmentDetails
					Document getShipmentDetailsDoc = api.getShipmentDetails(env,
							XMLUtil.getDocumentForElement(eleShipment));

					strShipmentStatus = getShipmentDetailsDoc.getDocumentElement().getAttribute("Status");
					log.verbose("strShipmentStatus : " + strShipmentStatus);
					// Check if the Status is Ready for Backroom Pick
					if (strShipmentStatus.equals(AcademyConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL)) {

						env.setApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT, tempChangeShipDoc);
						// Set the attribute value of IsPickTicketPrinted
						eleShipment.setAttribute("PickTicketPrinted", "Y");
						// Set the attribute value of PickticketNo
						if (!YFCCommon.isVoid(strBatchNo)) {
							eleShipment.setAttribute(AcademyConstants.ATTR_PICK_TICKET_NO, strBatchNo);
						}
						log.verbose("strLoginID : " + sLoginID);
						if (!YFCCommon.isVoid(sLoginID)) {
							eleShipment.setAttribute(AcademyConstants.ATTR_ASSIGNED_TO_USER_ID, sLoginID);
						}
						// END: Prepare input for changeShipment API
						// invoking the API changeShipment to update PickticketNo and AssignUserID
						changeShipmentOutputDoc = api.changeShipment(env, XMLUtil.getDocumentForElement(eleShipment));
						log.verbose("ShipmentKey : " + strShipmentKey);
						Document docChangeShipmentStatus = XMLUtil.createDocument("Shipment");
						// Fetch the element Shipment
						Element elemShipment = (Element) docChangeShipmentStatus.getDocumentElement();
						elemShipment.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
						elemShipment.setAttribute(AcademyConstants.ATTR_SELL_ORG_CODE,
								AcademyConstants.ENTERPRISE_CODE_SHIPMENT);
						elemShipment.setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS, "1100.70.06.20");
						/*OMNI-52430 Fix for Print Pick Ticket for STS shipment in search screen - START */
						strShipmentDocumentType=changeShipmentOutputDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DOC_TYPE);
						if (!YFCCommon.isVoid(strShipmentDocumentType)&&strShipmentDocumentType.equals(AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE)) {
							elemShipment.setAttribute(AcademyConstants.ATTR_TRANSID, AcademyConstants.TRAN_YCD_BACKROOM_PICK_IN_PROGRESS_0006);
						}
						else {
							elemShipment.setAttribute(AcademyConstants.ATTR_TRANSID, "YCD_BACKROOM_PICK_IN_PROGRESS");
						}
						/*OMNI-52430 Fix for Print Pick Ticket for STS shipment in search screen - END */

						log.verbose(XMLUtil.getXMLString(docChangeShipmentStatus));
						log.verbose(XMLUtil.getXMLString(docChangeShipmentStatus));

						Document tempChangeShipStatusDoc = YFCDocument.getDocumentFor(
								"<Shipment Status=\"\" CarrierServiceCode=\"\" PickTicketPrinted=\"\" PickticketNo=\"\" "
										+ "SCAC=\"\" ShipNode=\"\" ShipmentKey=\"\" ShipmentNo=\"\" />")
										.getDocument();
						// Invoke changeShipmentStatus to change Status to Backtoom Pick in Progress
						env.setApiTemplate(AcademyConstants.CHANGE_SHIPMENT_STATUS, tempChangeShipStatusDoc);
						api.changeShipmentStatus(env, docChangeShipmentStatus);
						env.clearApiTemplate(AcademyConstants.CHANGE_SHIPMENT_STATUS);

					}
				} else {
					log.verbose("Inside PickTicketPrinted=N Else Part");
					// Changes for BOPIS-1175 - End
					// setting the template
					env.setApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT, tempChangeShipDoc);
					// Set the attribute value of IsPickTicketPrinted
					eleShipment.setAttribute("PickTicketPrinted", "Y");
					// Set the attribute value of PickticketNo
					if (!YFCCommon.isVoid(strBatchNo)) {
						eleShipment.setAttribute(AcademyConstants.ATTR_PICK_TICKET_NO, strBatchNo);
					}
					// END: Prepare input for changeShipment API
					// invoking the API changeShipment
					changeShipmentOutputDoc = api.changeShipment(env, XMLUtil.getDocumentForElement(eleShipment));
				}
			}
			// Academy BOPIS : Print Management Change : Begin
			// Invoke planogram service and change shipment.
			// BOPIS:1303 - Begin
			log.debug("AcademySFSPrintPendingShipments.WebStoreFlow" + strIsWebStoreFlow);
			log.debug("AcademySFSPrintPendingShipments.WebStoreFlow. Calling Planogram Service");
			changeShipmentOutputDoc = callPlanogmAndChngeShipment(env, changeShipmentOutputDoc, strIsStoreBatch);
			// BOPIS:1303 - End
			HashMap<String, Element> hmpShpLine = new HashMap<String, Element>();
			// Academy BOPIS : Print Management Change : End
			// Fetch the NodeList of Node ShipmentLine
			NodeList shipmentLineList = changeShipmentOutputDoc
					.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

			// Academy BOPIS : Print Management Change : Begin
			int dShpLines = shipmentLineList.getLength();
			// Academy BOPIS : Print Management Change : End
			// Loop through each Shipment element
			for (int j = 0; j < shipmentLineList.getLength(); j++) {
				// Fetch the element ShipmentLine
				Element eleShipmentLine = (Element) shipmentLineList.item(j);
				// START: STL 1241

				// Fetch the element Item
				Element item = (Element) eleShipmentLine.getElementsByTagName(AcademyConstants.ITEM).item(0);
				/*
				 * //Set the attribute ItemBarCode item.setAttribute("ItemBarcode",
				 * generateBarcode(item.getAttribute("ItemID")));
				 */

				String strrItemID = item.getAttribute("ItemID");

				String itemAliasValue = AcademyUtil.getItemAliasValueForItem(strrItemID, env);
				if (!YFCObject.isVoid(itemAliasValue)) {
					log.verbose("************ALIAS VALUE IS NOT NULL***********************");
					item.setAttribute(AcademyConstants.ATTR_ITEM_BARCODE, itemAliasValue);
				} else {
					log.verbose("************ALIAS VALUE IS NULL***********************");
					item.setAttribute(AcademyConstants.ATTR_ITEM_BARCODE,
							AcademySFSPrintPendingShipments.generateBarcode(strrItemID));
				}
				// END STL 1241
			}
			// Fetch the root element of changeShipment API output
			elechangeShipRoot = changeShipmentOutputDoc.getDocumentElement();
			// set the attribute PrinterId
			elechangeShipRoot.setAttribute(AcademyConstants.ATTR_PRINTER_ID,
					props.getProperty(AcademyConstants.KEY_PRINTER_ID_VALUE));
			// set the attribute BeforeChildrenPrintDocumentId
			elechangeShipRoot.setAttribute(AcademyConstants.ATTR_DOCUMENT_ID,
					props.getProperty(AcademyConstants.KEY_DOCUMENT_ID_VALUE));
			// Increment the index and store it to PageNo
			int pageNo = listIndex + 1;
			String strpageNo=newIndex +" of "+totalNoOfPage;
			// Check if value is equal to Y
			if (singleShipment.equals("Y")) {
				// Fetch the trimmed attribute value of PickticketNo
				String pickticketNo = elechangeShipRoot.getAttribute("PickticketNo").trim();
				// Check if value is blank
				if (pickticketNo.equals("")) {
					// Set the attribute value of PageNumber to blank value
					elechangeShipRoot.setAttribute("PageNumber", strpageNo);
				} else {
					// Fetch the trimmed attribute value of ShipNode
					String strShipNode = elechangeShipRoot.getAttribute("ShipNode").trim();
					// Fetch the trimmed attribute value of ShipmentKey
					String shipmentKey = elechangeShipRoot.getAttribute("ShipmentKey").trim();
					// invoke method to fetch the shipment list
					// BOPIS-1175::Begin
					props.put("strIsWebStoreFlow", strIsWebStoreFlow);
					props.put("IsWebStoreBatch", IsWebStoreBatch);
					// BOPIS-1175::End
					Document tempShipments = getShipmentListOutput(env, strShipNode, pickticketNo, "", props);
					// Fetch the NodeList of element Shipment
					NodeList tempShipmentList = tempShipments.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
					// Loop through each Shipment element
					for (int j = 0; j < tempShipmentList.getLength(); j++) {
						// Fetch the element Shipment
						Element tempShipment = (Element) tempShipmentList.item(j);
						// Fetch the attribute ShipmentKey
						String tempShipmentKey = tempShipment.getAttribute("ShipmentKey").trim();
						// Validate the ShipmentKey value
						if (tempShipmentKey.equals(shipmentKey)) {
							// Increment the counter value and store it in PageNo
							pageNo = j + 1;
						}
					}
					// Set the attribute value of PageNumber
					elechangeShipRoot.setAttribute("PageNumber", "" + strpageNo);
				}
			} else {
				// Set the attribute value of PageNumber
				elechangeShipRoot.setAttribute("PageNumber", "" + strpageNo);
			}
			// Validation of the pageNos value
			if (pageNos.isEmpty() || pageNos.containsKey(pageNo + "")) {
				// Academy BOPIS : Print Management Change : Begin
				strPageSize = props.getProperty("PageSize");
				int dPageSize = Integer.parseInt(strPageSize);
				if (dShpLines > dPageSize) {
					// call method to create pagination for the input doc.
					callPrintServ(env, changeShipmentOutputDoc, dShpLines, dPageSize);
				} else {
					AcademyUtil.invokeService(env, AcademyConstants.SERV_PRINT_PEND_SHIP, changeShipmentOutputDoc);
					newIndex++;
				}
				// Academy BOPIS : Print Management Change : End
			}
		}
	}

	/**
	 * This method creates pagination for PickShipment ticket.
	 * 
	 * @param env
	 * @param changeShipmentOutputDoc
	 * @param boolShpLines
	 * @throws Exception
	 */
	public void callPrintServ(YFSEnvironment env, Document changeShipmentOutputDoc, int dShpLines, int dPageSize)
			throws Exception {
		YFCDocument outDoc = YFCDocument.getDocumentFor(changeShipmentOutputDoc);
		//BOPIS-1572::Begin-- added below logger
		log.verbose("Input to callPrintServ()::" + outDoc.toString());
		//BOPIS-1572::End
		YFCElement eleRootOutDoc = outDoc.getDocumentElement();
		YFCNodeList<YFCElement> nlShpLines = eleRootOutDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

		YFCDocument pgedDoc = YFCDocument.getDocumentFor(eleRootOutDoc.toString());
		YFCElement eleRootPgedDoc = pgedDoc.getDocumentElement();
		eleRootPgedDoc.removeChild(eleRootPgedDoc.getChildElement(AcademyConstants.ELE_SHIPMENT_LINES));
		YFCElement eleShipmentLines = eleRootPgedDoc.createChild(AcademyConstants.ELE_SHIPMENT_LINES);
		int count = 0, pageNo = 0;
		for (int index = 0; index < dShpLines; index++) {
			YFCElement eleShpLine = nlShpLines.item(index);
			eleShipmentLines.importNode(eleShpLine);
			count++;
			if (count == dPageSize || index == (dShpLines - 1)) {
				pageNo++;
				String strpageNo=newIndex +" of "+totalNoOfPage;
				eleRootPgedDoc.setAttribute("PageNumber", strpageNo);

				AcademyUtil.invokeService(env, AcademyConstants.SERV_PRINT_PEND_SHIP, pgedDoc.getDocument());
				newIndex++;
				count = 0;
				eleRootPgedDoc.removeChild(eleRootPgedDoc.getChildElement(AcademyConstants.ELE_SHIPMENT_LINES));
				eleShipmentLines = eleRootPgedDoc.createChild(AcademyConstants.ELE_SHIPMENT_LINES);
			}
		}

	}

	/**
	 * 
	 * @param changeShipmentOutputDoc
	 * @return
	 * @throws Exception
	 */
	public Document callPlanogmAndChngeShipment(YFSEnvironment env, Document changeShipmentOutputDoc,
			String strIsStoreBatch) throws Exception {
		log.debug("AcademySFSPrintPendingShipments.callPlanogmAndChngeShipment().input" + changeShipmentOutputDoc);
		boolean boolPlanogramInvoked = true;
		
		//start OMNI-48554, OMNI-49032, OMNI-49033
		//Fulfillment- Update the Print Pick Ticket to display the Available stock on hand, Last Received Date & Item Price
		Document outDocSIMUpdate = getSIMUpdatesForBackroom(env, changeShipmentOutputDoc);
		//end OMNI-48554, OMNI-49032, OMNI-49033

		YFCDocument inDoc = YFCDocument.getDocumentFor(changeShipmentOutputDoc);
		YFCElement eleShip = inDoc.getDocumentElement();
		String strShipNode = eleShip.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
		YFCElement eleShpLns = eleShip.getChildElement("ShipmentLines");
		YFCNodeList<YFCElement> nlShpLne = eleShpLns.getElementsByTagName("ShipmentLine");
		for (YFCElement eleShpLne : nlShpLne) {
			// set the image Url and Long sku at item level for each shipment line.
			YFCElement eleOrderLine = eleShpLne.getChildElement("OrderLine");
			YFCElement eleItem = eleOrderLine.getChildElement("Item");
			YFCElement eleItemDetails = eleOrderLine.getChildElement("ItemDetails");
			YFCElement eleExtnItemDetails = eleItemDetails.getChildElement("Extn");
			String strExtnImageLocalPath = eleExtnItemDetails.getAttribute("ExtnImageLocalPath");
			String strExtnItemImageName = eleExtnItemDetails.getAttribute("ExtnItemImageName");
			String strExtnStyle = eleExtnItemDetails.getAttribute("ExtnStyle");
			String strExtnSizeCodeDescription = eleExtnItemDetails.getAttribute("ExtnSizeCodeDescription");
			String strExtnVendorColorName = eleExtnItemDetails.getAttribute("ExtnVendorColorName");

			String strItemId = eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID);
			String strProdClass = eleItem.getAttribute(AcademyConstants.ATTR_PROD_CLASS);
			String strUOM = eleItem.getAttribute(AcademyConstants.ATTR_UOM);
			String strTotalStock = getTotalStock(env, strItemId, strShipNode, strProdClass, strUOM);
			String strImageURL = strExtnImageLocalPath + strExtnItemImageName;
			String strLongSKU = strExtnStyle + "/" + strExtnSizeCodeDescription + "/" + strExtnVendorColorName;
			eleItem.setAttribute("ImageURL", strImageURL);
			eleItem.setAttribute("LongSKU", strLongSKU);
			eleItem.setAttribute("TotalStock", strTotalStock);
			
			//start OMNI-48554, OMNI-49032, OMNI-49033
			//Fulfillment- Update the Print Pick Ticket to display the Available stock on hand, Last Received Date & Item Price
			log.verbose("strSLItemID :: " +strItemId);
			
			if(!YFCObject.isVoid(outDocSIMUpdate)) {
				
				log.verbose("SIM RestAPI invocation for SFS & BOPIS Backroompick screen");
				YFCDocument yfcDocSIMUpdate = YFCDocument.getDocumentFor(outDocSIMUpdate);
				YFCElement eleRootJsonObject = yfcDocSIMUpdate.getDocumentElement();
				log.verbose("eleRootJsonObject.hasChildNodes() :: " +eleRootJsonObject.hasChildNodes());
				
				String strResErrorCode = eleRootJsonObject.getAttribute(AcademyConstants.STRI_CODE);
				log.verbose("SIM Response Error Code :: " +strResErrorCode);
				
				if(!YFCObject.isVoid(eleRootJsonObject) && eleRootJsonObject.getNodeName().equals(AcademyConstants.STR_JSON_OBJECT) 
						&& eleRootJsonObject.hasChildNodes() && YFCObject.isVoid(strResErrorCode)) {
					eleShpLne.setAttribute(AcademyConstants.STR_SIM_REST_API, AcademyConstants.STRI_SUCCESS);
					log.verbose("SIM RestAPI Enabled and API invocation is success for backroompick screen");
					YFCNodeList<YFCElement> eleSIMItemNodeList= eleRootJsonObject.getElementsByTagName(AcademyConstants.ITEM);
					
					for (YFCElement eleSIMItem : eleSIMItemNodeList){
						YFCElement eleChieldJsonObject = eleSIMItem.getChildElement(AcademyConstants.STR_JSON_OBJECT);
						String strResItemId = eleChieldJsonObject.getAttribute(AcademyConstants.STR_ITEM_ID);
						log.verbose("SIM Response ItemId :: " +strResItemId);
						
						if((!YFCObject.isVoid(strItemId) && !YFCObject.isVoid(strResItemId)) && strItemId.equals(strResItemId)) {
							
							eleShpLne.setAttribute(AcademyConstants.STR_AVL_STOCK_ON_HAND, eleChieldJsonObject.getAttribute(AcademyConstants.STRI_AVL_STOCK_ON_HAND));
							log.verbose("SIM Response AvailableStockOnHand :: " + eleChieldJsonObject.getAttribute(AcademyConstants.STRI_AVL_STOCK_ON_HAND));
							
							eleShpLne.setAttribute(AcademyConstants.STR_LAST_REC_DATE, eleChieldJsonObject.getAttribute(AcademyConstants.STRI_LAST_REC_DATE));
							log.verbose("SIM Response LastReceivedDate :: " + eleChieldJsonObject.getAttribute(AcademyConstants.STRI_LAST_REC_DATE));
							
							eleShpLne.setAttribute(AcademyConstants.STR_ITEM_PRICE, eleChieldJsonObject.getAttribute(AcademyConstants.STR_RETAIL_PRICE));
							log.verbose("SIM Response ItemPrice/retailPrice :: " + eleChieldJsonObject.getAttribute(AcademyConstants.STR_RETAIL_PRICE));
						}
					}
				} else {
					//log.verbose("SIM invocation is disabled at service level, IsSIMRestAPIEnabled shd be Y or RestAPI invocation is failed");
					//String strResErrormessage = eleRootJsonObject.getAttribute("message");
					log.verbose("Invalid SIM Response1 - outDocSIMUpdate :: \n" +SCXmlUtil.getString(outDocSIMUpdate));
					eleShpLne.setAttribute(AcademyConstants.STR_SIM_REST_API, AcademyConstants.STRI_FAILURE);
					eleShpLne.setAttribute(AcademyConstants.STR_AVL_STOCK_ON_HAND, "");
					eleShpLne.setAttribute(AcademyConstants.STR_LAST_REC_DATE, "");
					eleShpLne.setAttribute(AcademyConstants.STR_ITEM_PRICE, "");
				}					
			}else {
				//log.verbose("SIM RestAPI Disabled or RestAPI invocation is failed");
				log.verbose("Invalid SIM Response2 - outDocSIMUpdate :: \n" +SCXmlUtil.getString(outDocSIMUpdate));
				eleShpLne.setAttribute(AcademyConstants.STR_SIM_REST_API, AcademyConstants.STRI_FAILURE);
				eleShpLne.setAttribute(AcademyConstants.STR_AVL_STOCK_ON_HAND, "");
				eleShpLne.setAttribute(AcademyConstants.STR_LAST_REC_DATE, "");
				eleShpLne.setAttribute(AcademyConstants.STR_ITEM_PRICE, "");
			}
			//end OMNI-48554, OMNI-49032, OMNI-49033
			
			YFCElement eleExtn = eleShpLne.getChildElement("Extn");
			String strExtnPlngrmStatus = eleExtn.getAttribute("ExtnPlanogramStatus");
			if (YFCCommon.equalsIgnoreCase("NOT_INITIATED", strExtnPlngrmStatus)
					&& !YFCCommon.equalsIgnoreCase("Y", strIsStoreBatch)) {
				// BOPIS:1303 - Begin
				if (YFCCommon.equalsIgnoreCase("Y", strIsWebStoreFlow)) {
					log.debug("AcademySFSPrintPendingShipments.callPlanogmAndChngeShipment().WebStoreFlow");
					boolPlanogramInvoked = false;
				}
				// BOPIS:1303 - End
			} else if (!YFCCommon.equalsIgnoreCase("LOCATION_AVAILABLE", strExtnPlngrmStatus)) {
				eleExtn.setAttribute("ExtnPogId", " ");
				eleExtn.setAttribute("ExtnDepartment", " ");
				eleExtn.setAttribute("ExtnSection", " ");
				eleExtn.setAttribute("ExtnPogNumber", " ");
				eleExtn.setAttribute("ExtnLiveDate", " ");
			}

		}
		if (!boolPlanogramInvoked) {
			// invoke planogram service
			Document inDocPlanogramService = prepareInputForPlgrmService(changeShipmentOutputDoc);

			Document outDocPlanogramService = AcademyUtil.invokeService(env, "AcademyCallWebserviceForPlanogramDetails",
					inDocPlanogramService);

			HashMap<String, YFCElement> hmpPlngrmSerOutput = prepareHmpForOutDocPlngrmSrv(outDocPlanogramService);
			for (YFCElement eleShpLne : nlShpLne) {
				String strShipLineNo = eleShpLne.getAttribute(AcademyConstants.ATTR_SHIP_LINE_NO);
				YFCElement eleExtn = eleShpLne.getChildElement(AcademyConstants.ELE_EXTN);
				if (hmpPlngrmSerOutput.containsKey(strShipLineNo)) {
					YFCElement elePlgrmSerShpLne = hmpPlngrmSerOutput.get(strShipLineNo);
					YFCElement eleExtnPlgrmSerShpLne = elePlgrmSerShpLne.getChildElement(AcademyConstants.ELE_EXTN);
					String strIsPlanogramStatus = eleExtnPlgrmSerShpLne.getAttribute("ExtnPlanogramStatus");
					if (YFCCommon.equalsIgnoreCase("LOCATION_AVAILABLE", strIsPlanogramStatus)) {
						eleExtn.setAttribute("ExtnPogId", eleExtnPlgrmSerShpLne.getAttribute("ExtnPogId"));
						eleExtn.setAttribute("ExtnDepartment", eleExtnPlgrmSerShpLne.getAttribute("ExtnDepartment"));
						eleExtn.setAttribute("ExtnSection", eleExtnPlgrmSerShpLne.getAttribute("ExtnSection"));
						eleExtn.setAttribute("ExtnPogNumber", eleExtnPlgrmSerShpLne.getAttribute("ExtnPogNumber"));
						eleExtn.setAttribute("ExtnLiveDate", eleExtnPlgrmSerShpLne.getAttribute("ExtnLiveDate"));
					} else {
						eleExtn.setAttribute("ExtnPogId", " ");
						eleExtn.setAttribute("ExtnDepartment", " ");
						eleExtn.setAttribute("ExtnSection", " ");
						eleExtn.setAttribute("ExtnPogNumber", " ");
						eleExtn.setAttribute("ExtnLiveDate", " ");
					}
				}
			}
		}
		log.debug("AcademySFSPrintPendingShipments.callPlanogmAndChngeShipment().output" + changeShipmentOutputDoc);
		return changeShipmentOutputDoc;
	}

	/**
	 * 
	 * @param changeShipmentOutputDoc
	 * @return
	 */
	public Document prepareInputForPlgrmService(Document changeShipmentOutputDoc) {
		YFCDocument inDocChngeShpOutDoc = YFCDocument.getDocumentFor(changeShipmentOutputDoc);
		YFCElement eleShpChngeShpOutDoc = inDocChngeShpOutDoc.getDocumentElement();
		YFCElement eleShpLnsChngeShp = eleShpChngeShpOutDoc.getChildElement(AcademyConstants.ELE_SHIPMENT_LINES);
		YFCNodeList<YFCElement> nlEleShpLneChngeShp = eleShpLnsChngeShp
				.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

		YFCDocument inDocPlanogramService = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
		YFCElement eleShpInDoc = inDocPlanogramService.getDocumentElement();
		eleShpInDoc.setAttribute(AcademyConstants.ATTR_SHIP_NODE,
				eleShpChngeShpOutDoc.getAttribute(AcademyConstants.ATTR_SHIP_NODE));
		eleShpInDoc.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
				eleShpChngeShpOutDoc.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
		eleShpInDoc.setAttribute("StoreInDB", "Y");
		YFCElement eleShpLns = eleShpInDoc.createChild(AcademyConstants.ELE_SHIPMENT_LINES);

		for (YFCElement eleShpLneChngeShp : nlEleShpLneChngeShp) {
			YFCElement eleExtnShpLneChngeShp = eleShpLneChngeShp.getChildElement(AcademyConstants.ELE_EXTN);
			String strExtnPlngmStatus = eleExtnShpLneChngeShp.getAttribute("ExtnPlanogramStatus");
			if (YFCCommon.equalsIgnoreCase("NOT_INITIATED", strExtnPlngmStatus)) {
				YFCElement eleOrderLine = eleShpLneChngeShp.getChildElement(AcademyConstants.ELE_ORDER_LINE);
				YFCElement eleItem = eleOrderLine.getChildElement(AcademyConstants.ITEM);

				YFCElement eleShpLne = eleShpLns.createChild(AcademyConstants.ELE_SHIPMENT_LINE);
				eleShpLne.setAttribute(AcademyConstants.ATTR_SHIP_LINE_NO,
						eleShpLneChngeShp.getAttribute(AcademyConstants.ATTR_SHIP_LINE_NO));
				eleShpLne.setAttribute(AcademyConstants.ATTR_ITEM_ID,
						eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID));
			}

		}
		return inDocPlanogramService.getDocument();
	}

	/**
	 * 
	 * @param outDocPlanogramService
	 * @return
	 */
	public HashMap<String, YFCElement> prepareHmpForOutDocPlngrmSrv(Document outDocPlanogramService) {
		HashMap<String, YFCElement> hmpPlngrmSerOutput = new HashMap<String, YFCElement>();
		YFCDocument yfcOutDocPlanogramService = YFCDocument.getDocumentFor(outDocPlanogramService);
		YFCElement eleShp = yfcOutDocPlanogramService.getDocumentElement();
		YFCElement eleShpLns = eleShp.getChildElement(AcademyConstants.ELE_SHIPMENT_LINES);
		YFCNodeList<YFCElement> nlShpLne = eleShpLns.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		for (YFCElement eleShpLne : nlShpLne) {
			String strShpLneNo = eleShpLne.getAttribute(AcademyConstants.ATTR_SHIP_LINE_NO);
			if (!hmpPlngrmSerOutput.containsKey(strShpLneNo)) {
				hmpPlngrmSerOutput.put(strShpLneNo, eleShpLne);
			}
		}

		return hmpPlngrmSerOutput;
	}

	/**
	 * 
	 * @param env
	 * @param strItemId
	 * @param strShipNode
	 * @param strProdClass
	 * @param strUOM
	 * @return
	 * @throws Exception
	 */
	public static String getTotalStock(YFSEnvironment env, String strItemId, String strShipNode, String strProdClass,
			String strUOM) throws Exception {
		String strTotalStock = "";
		YFCDocument inDocGetInvSuply = YFCDocument.createDocument(AcademyConstants.ELE_INVENTORY_SUPPLY);
		YFCElement eleInvSupply = inDocGetInvSuply.getDocumentElement();
		eleInvSupply.setAttribute(AcademyConstants.ATTR_ITEM_ID, strItemId);
		eleInvSupply.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strShipNode);
		eleInvSupply.setAttribute(AcademyConstants.ORGANIZATION_CODE, AcademyConstants.DSV_ENTERPRISE_CODE);
		eleInvSupply.setAttribute(AcademyConstants.ATTR_PROD_CLASS, strProdClass);
		eleInvSupply.setAttribute(AcademyConstants.ATTR_SUPPLY_TYPE, AcademyConstants.STR_SUPP_TYPE_VAL);
		eleInvSupply.setAttribute(AcademyConstants.ATTR_UOM, strUOM);

		Document outDocInvSupply = AcademyUtil.invokeAPI(env, "getInventorySupply", inDocGetInvSuply.getDocument());

		YFCDocument yfcOutDocInvSupply = YFCDocument.getDocumentFor(outDocInvSupply);
		YFCElement eleRootOutDocInvSupply = yfcOutDocInvSupply.getDocumentElement();
		YFCNodeList<YFCElement> nlInvSupply = eleRootOutDocInvSupply
				.getElementsByTagName(AcademyConstants.ELE_INVENTORY_SUPPLY);
		for (YFCElement eleInvSupplyOutDoc : nlInvSupply) {
			if (YFCCommon.equalsIgnoreCase(AcademyConstants.STR_SUPP_TYPE_VAL,
					eleInvSupplyOutDoc.getAttribute(AcademyConstants.ATTR_SUPPLY_TYPE))) {
				strTotalStock = eleInvSupplyOutDoc.getAttribute(AcademyConstants.ATTR_QUANTITY);
			}
		}
		return strTotalStock;
	}
	
	//start OMNI-48554, OMNI-49032, OMNI-49033
	//Fulfillment- Update the Print Pick Ticket to display the Available stock on hand, Last Received Date & Item Price
	/**
	 * inDoc to Service:
	 * <Items StoreID="033"> 
	 * 		<Item ItemID="010035624"/> 
	 * 		<Item ItemID="010035625"/>
	 * </Items>
	 */
	private Document getSIMUpdatesForBackroom(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("getSIMUpdatesForBackroom - Start" + XMLUtil.getXMLString(inDoc));
		
		Document outDocService = null; 
		Document indocService= null;
		String strStoreID = null;
		
		indocService = XMLUtil.createDocument(AcademyConstants.ELE_ITEMS);
		Element eleItems = indocService.getDocumentElement();
		
		YFCDocument docInShipment = YFCDocument.getDocumentFor(inDoc);
		YFCElement eleInShipment = docInShipment.getDocumentElement();
		YFCElement eleGetShipmentLineList = eleInShipment.getChildElement(AcademyConstants.ELE_SHIPMENT_LINES);
		
		YFCNodeList<YFCElement> eleGetShipmentLineNodeList = eleGetShipmentLineList.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
				
		for (YFCElement eleShipmentLine : eleGetShipmentLineNodeList){		
			YFCElement eleOrderLine = eleShipmentLine.getChildElement("OrderLine");
			YFCElement eleItem = eleOrderLine.getChildElement("Item");
			String strItemID = eleItem.getAttribute(AcademyConstants.ITEM_ID);
			log.verbose("getSIMUpdatesForBackroom - strItemID :: " + strItemID);
			Element itemElement = XMLUtil.createElement(indocService, AcademyConstants.ITEM, true);
			itemElement.setAttribute(AcademyConstants.ITEM_ID, strItemID);
			eleItems.appendChild(itemElement);
			
		}
		strStoreID = eleInShipment.getAttribute(AcademyConstants.STR_SHIPNODE);
		eleItems.setAttribute(AcademyConstants.STR_STORE_ID, strStoreID);
		log.verbose("getSIMUpdatesForBackroom - strStoreID :: " + strStoreID);
		
		log.verbose("Start: Invoking the AcademySIMIntegrationRestAPIWebService, inXML: \n" + XMLUtil.getXMLString(indocService));
		//outDocService = AcademyUtil.invokeService(env, "AcademySIMIntegrationRestAPIService", indocService);
		outDocService = AcademyUtil.invokeService(env, "AcademySIMIntegrationRestAPIWebService", indocService);
		log.verbose("End: Invoking the AcademySIMIntegrationRestAPIWebService, outDoc: \n" + XMLUtil.getXMLString(outDocService));
		
		log.verbose("getSIMUpdatesForBackroom - End");
		
		return outDocService;
	}
	//end OMNI-48554, OMNI-49032, OMNI-49033
}