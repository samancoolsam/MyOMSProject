//package declaration

package com.academy.ecommerce.sterling.shipment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;


/**
 * Class to perform RecordBackRoom Pick operation for a shipment.
 * @version 1.0
 * @author <a href="mailto:Kiran.Vernekar@cognizant.com">Vernekar, Kiran</a> * 
 * Copyright © 2006-2009 Sterling Commerce, Inc. All Rights Reserved.
 * 
 * @version 1.1
 * @author 250626
 * STL -244 (ShortPicked cancels the current Proforma Invoice and new Proforma Invoice will be created for the shipments. 
 * ExtnInvoiceNo was printed on all labels and will be used as a reference by customer and also all financial 
 * settlement will be done against this No. On short Picked due to new performa invoice creation old invoice 
 * information is getting lost including the ExtnInvoiceNo.This code change updates the old ExtnInvoiceNo to the current Proforma Invoice.)
 */

public class AcademyProcessBackroomPick implements YIFCustomApi
{
	// Set the logger
	private static final YFCLogCategory	log		= YFCLogCategory.instance(AcademyProcessBackroomPick.class);
	private static YIFApi				api		= null;
	
	//START: SHIN-15
	
	String strShipNode = "";
	
	// Declare document variable
	Document getShipNodeListInputDoc = null;
	Document getShipNodeListOutputDoc = null;
	Document templateDoc = null;
	// Declare element variables
	Element eleRootElement = null;
	Element eleOutShipNode = null;	
	String strNodeType="";

	
	//END: SHIN-15
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
	private Properties					props	= new Properties();

	public void setProperties(Properties props)
	{
		this.props = props;
	}

	public void performBackroomPick(YFSEnvironment env, Document inXML) throws Exception
	{
		if (log.isVerboseEnabled())
		{
			log.verbose("ProcessBackroomPick: performBackroomPick: inXML: " + XMLUtil.getXMLString(inXML));
			
		}
		
		// Declare the boolean variable
		boolean isInventoryShortage = false;
		boolean isPickComplete = false;
		boolean itemPicked = false;
		String strIWebStoreFlow = ""; 
		
		//Start containerization 
		strIWebStoreFlow = (String) env.getTxnObject(AcademyConstants.A_IS_WEB_STORE_FLOW);
		if(YFCObject.isVoid(strIWebStoreFlow)){
			strIWebStoreFlow =AcademyConstants.STR_NO;
		}
	
		//End containerizaion
		
		// Fetch the root element
		Element inXMLRoot = inXML.getDocumentElement();
		
		//STL- 987 :: Start
		String strStaus = ((Element) inXML.getElementsByTagName("Shipment").item(0)).getAttribute("Status"); 
		//STL - 987 :: End
		// START: SHIN-10
		strShipNode = ((Element) inXML.getElementsByTagName("Shipment")
				.item(0)).getAttribute("ShipNode");
		// END: SHIN-10
		
		//EFP-shn
		boolean isMultiBox = false;
		//EFP-shn
		
		// Start STL-244
		// Start - Changes made for PERF-395 to handle null pointer exception in a one of scenario.		
		String extnInvoiceNo = "";
		NodeList shipmentExtnElemList = inXMLRoot.getElementsByTagName(AcademyConstants.ELE_EXTN);
		if (shipmentExtnElemList != null && shipmentExtnElemList.getLength() > 0) {
			Element extnShipmentElm = (Element) shipmentExtnElemList.item(0);
			extnInvoiceNo = extnShipmentElm.getAttribute("ExtnInvoiceNo");
		} else {
			YFSException e = new YFSException();
			e.setErrorCode("EXTN_ACAD003");
			e.setErrorDescription("System Error. Unable to fetch Custom Shipment Information");
			throw e;
		}	
		// End - Changes made for PERF-395 to handle null pointer exception in a one of scenario. 
		// End STL-244

		// Check if IsCompletePcik=Y
		if (inXMLRoot.getAttribute("IsCompletePick").equals("Y"))
		{
			// Set isPickComplete=true
			isPickComplete = true;
		}
		
		//EFP-shn
		String strIsMultiBox = XMLUtil.getAttributeFromXPath(inXML, 
				"//Shipment/ShipmentLines/ShipmentLine/OrderLine/Item/Extn/@ExtnMultibox");
		if(AcademyConstants.STR_YES.equalsIgnoreCase(strIsMultiBox)){
			isMultiBox = true;
			env.setTxnObject("isMultibox", "Y");
			
		}
		//EFP-shn
		
		// Check for InventoryStorage
		if (inXMLRoot.getAttribute("radioSelection").equals("BP_Inventory_Shortage")) // SOM Conversion :: changed the name from "InventoryShortage" to "BP_Inventory_Shortage"
		{
			// Set isInventoryShortage to true
			isInventoryShortage = true;
			//Start OMNI-7887 Creating Inv Node Control record
			inXMLRoot.setAttribute(AcademyConstants.ATTR_SOURCE, AcademyConstants.STR_SOURCE_RCP);
			AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACAD_CREATE_INV_NODE_CONTROL, inXML);
			//End OMNI-7887 
		}
		
		
		//SFS2.0 001 Printer Id Start
		
		String PackSlipPrinterId = "";
		String BOLPrinterId = "";
		String ShippingLabelPrinterId = "";
		String STSShippingLabelPrinterId = "";
		String ReturnLabelPrinterId = "";
		String ORMDLabelPrinterId = "";		//Added for STL-737
		
		Element eleShipment = inXML.getDocumentElement();
		String strShipmentKey = eleShipment.getAttribute("ShipmentKey");
		String strPrintPackId = eleShipment.getAttribute("PrintPackId");
		//String strPrintLabelId = eleShipment.getAttribute("PrinterLabelId");
		
		/*Calling getShipmentList to get the shipment type
		Input of getShipmentList is:
			<Shipment ShipmentKey="" />*/
						
		Document inShipmentListDoc = XMLUtil.createDocument("Shipment");		
		Element inShipmentElem = inShipmentListDoc.getDocumentElement();
		inShipmentElem.setAttribute("ShipmentKey", strShipmentKey);
		env.setApiTemplate(AcademyConstants.GET_SHIPMENT_LIST_API,
				YFCDocument.getDocumentFor("<Shipments> <Shipment ShipmentKey='' ShipmentType=''/> </Shipments>").getDocument());
		Document outShipmentDoc = AcademyUtil.invokeAPI(env, "getShipmentList",
				inShipmentListDoc);
		env.clearApiTemplate(AcademyConstants.GET_SHIPMENT_LIST_API);
		Element eleShipmentElem = outShipmentDoc.getDocumentElement();
		Element eleShipElem = (Element)eleShipmentElem.getElementsByTagName("Shipment").item(0);
		String strShipmentType1= eleShipElem.getAttribute("ShipmentType");	
		
		Document inCommonCodeDoc = XMLUtil.createDocument("CommonCode");
	
		/*If The CommonCode Type is called for selecting Pack slip printer
		Input to getCommonCodeList is:
			<CommonCode CodeType="" />*/
	
		Element inComElem = inCommonCodeDoc.getDocumentElement();
		inComElem.setAttribute("CodeType", strPrintPackId);
	
		Document outCommDoc = AcademyUtil.invokeAPI(env, "getCommonCodeList",
				inCommonCodeDoc);
		
		if (!YFCObject.isVoid(outCommDoc)) {
			Element outCommElem = outCommDoc.getDocumentElement();
			NodeList CommonCodeList = XMLUtil.getNodeList(outCommElem,
					"CommonCode");
		String strCodeValueCheck = "";	
		if(strShipmentType1.equals("BLP") || strShipmentType1.equals("BNLP") || strShipmentType1.equals("BULKOVNT")){
			strCodeValueCheck = "BULK_PRINTER";
		}
		else if(strShipmentType1.contains("WG")){
			strCodeValueCheck = "WG_PRINTER";
		}
		// Start STL-737 Changes: For AMMO shipment also, use NON_BULK_PACK
         //Start :Changes made for STL-934 Checkout Funnel
		 //Start : Changes made for STL-1320 Checkout Funnel OVNT
		//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
		//Start WN-2980 GC Activation and fulfillment for SI DCs
		else if(strShipmentType1.contains("CON") || 
					strShipmentType1.contains(AcademyConstants.STR_GC_ONLY_SHIP_TYPE) || 
					strShipmentType1.contains(AcademyConstants.STR_GC_SHIP_TYPE) || 
					strShipmentType1.equals(AcademyConstants.AMMO_SHIPMENT_TYPE) ||
					AcademyConstants.HAZMAT_SHIPMENT_TYPE.equals(strShipmentType1) ||
					strShipmentType1.equals(AcademyConstants.ATTR_CSA_SHIPMENT_TYPE) ||  
					strShipmentType1.equals(AcademyConstants.ATTR_CSA_OVNT_SHIPMENT_TYPE)){
			strCodeValueCheck = "NON_BULK_PACK";
		}
		//End WN-2980 GC Activation and fulfillment for SI DCs
		//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
		//End : Changes made for STL-1320 Checkout Funnel OVNT
		// End STL-737 Changes
       //End : Changes made for STL-934 Checkout Funnel
			if (!YFCObject.isVoid(CommonCodeList)) {
				int iLength2 = CommonCodeList.getLength();
				for (int k = 0; k < iLength2; k++) {
					Element CommonCode = (Element) CommonCodeList.item(k);
					String codevalue = CommonCode.getAttribute("CodeValue");
					if (codevalue.contains(strCodeValueCheck)) {
						PackSlipPrinterId = codevalue;
						env.setTxnObject("PackSlipPrinterId", PackSlipPrinterId);
						log.verbose(" PackSlip Printer Id is :" + PackSlipPrinterId);
					} 
					if(codevalue.contains("BOL") && strCodeValueCheck.equals("WG_PRINTER")){
						BOLPrinterId = codevalue;
						env.setTxnObject("BOLPrinterId", BOLPrinterId);
						log.verbose("BOL Printer Id is :" + BOLPrinterId);
					}
					//STL-1600 All shipments including WG will have shipping label
					//if(codevalue.contains("SHIPPING_LABEL") && (strCodeValueCheck.equals("BULK_PRINTER") || strCodeValueCheck.equals("NON_BULK_PACK"))){
						if(codevalue.contains("SHIPPING_LABEL")){
							ShippingLabelPrinterId = codevalue;
						    env.setTxnObject("ShippingLabelPrinterId", ShippingLabelPrinterId);
                        log.verbose(" Shipping Label PrinterId is :" + ShippingLabelPrinterId);						
                  }
                  if(codevalue.contains("RETURN_LABEL") && (strCodeValueCheck.equals("BULK_PRINTER") || strCodeValueCheck.equals("NON_BULK_PACK"))){
                        ReturnLabelPrinterId = codevalue;
                        env.setTxnObject("ReturnLabelPrinterId", ReturnLabelPrinterId);
                        log.verbose(" Return Label PrinterId is :" + ReturnLabelPrinterId);
                  }
                  //Start STL-737 changes: ORMD label for ammo shipment
                  if(codevalue.contains("ORMD_LABEL")){
                	  ORMDLabelPrinterId = codevalue;
                      env.setTxnObject("ORMDLabelPrinterId", ORMDLabelPrinterId);
                      log.verbose(" ORMD Label PrinterId is :" + ORMDLabelPrinterId);
                      log.verbose(" ORMD Label PrinterId is :" + ORMDLabelPrinterId);
                  }
                  //End STL-737 changes
 				//STS2.0 Printer issue
                  if(codevalue.contains(AcademyConstants.STR_STS_SHIPPING_LABEL_PRINTER_NAME)){
                	  String strCodeShortDesc = CommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
                	  log.verbose("codevalue::" + codevalue + "::Short Desc::" +strCodeShortDesc );
                	  env.setTxnObject(AcademyConstants.STR_STS_SHIPPING_LABEL_PRINTER_ID, strCodeShortDesc);
					    log.verbose("STS Shipping Label PrinterId is :" + strCodeShortDesc);						
                  }
				}
			}
		}
		log.endTimer(" End of getCommonCodeList To get Printer Id-> getCommonCodeList Api");
	
		//SFS2.0 001 Printer Id end
		// Create root element MultiApi
		Document multiApiDoc = XMLUtil.createDocument("MultiApi");
		Element multiApiDocRoot = multiApiDoc.getDocumentElement();
		// Create element Shipment
		Element changeShipmentRoot = multiApiDoc.createElement("Shipment");
		// Append the child element
		// multiApiDocRoot.appendChild(changeShipmentRoot);

		// Create Extn Attribute
		Element extn = multiApiDoc.createElement("Extn");
		changeShipmentRoot.appendChild(extn);
		extn.setAttribute("ExtnSopModifyts", AcademyUtil.getDate());
		
		//STL-679: Storing the Short Reason Code in YFS_SHIPMENT table
		//containerization
		if(!YFCCommon.equals(AcademyConstants.STR_YES, strIWebStoreFlow)){
		extn.setAttribute(AcademyConstants.ATTR_EXTN_SHORTPICK_REASON_CODE, inXMLRoot.getAttribute(AcademyConstants.ATTR_EXTN_SHORTPICK_REASON_CODE));
	}
		
		//Start : STL 246
		// Set the attribute value of DocumentType
		//changeShipmentRoot.setAttribute("DocumentType", inXMLRoot.getAttribute("DocumentType"));
		//End : STL 246
		// Set attribute value of ShipmentKey
		changeShipmentRoot.setAttribute("ShipmentKey", inXMLRoot.getAttribute("ShipmentKey"));
		
		
		//STL-1737 setting ShipmentNo as this is must to make Vertex call inside changeShipment UE. 
		changeShipmentRoot.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, inXMLRoot.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
		
		// Create element ShipmentLines
		Element shipmentLines = multiApiDoc.createElement("ShipmentLines");
		// Append the child element
		changeShipmentRoot.appendChild(shipmentLines);
		// Create element Containers
		Element containers = multiApiDoc.createElement("Containers");
		// Create element Container
		Element container = multiApiDoc.createElement("Container");
		//Create element ContainerDetails
		Element containerDetails = multiApiDoc.createElement("ContainerDetails");
		
		Element containersMultiBox = null;
		
		
		if(!isMultiBox){
				
		// Append the child element
			containers.appendChild(container);
			
			// Append the child element
		container.appendChild(containerDetails);
		}
		
		
		// Fetch NodeList of Node ShipmentLine from the input xml
		NodeList shipmentLineList = inXMLRoot.getElementsByTagName("ShipmentLine");
		// Declare the element variable
		Element inventorySnapShotRoot = null;
		Element inventorySnapShotShipNode = null;

		// Declare the hashmap
		HashMap<String, Double> itemQuantityMap = new HashMap<String, Double>();
		
		
		// If inventory shortage
		if (isInventoryShortage)
		{
		    // Set the attribute value Action
			changeShipmentRoot.setAttribute("Action", "Cancel");
			// Set the attribute value BackOrderRemovedQuantity
			changeShipmentRoot.setAttribute("BackOrderRemovedQuantity", "Y");
			
			// START:SHIN-10
			/*
			 * Commenting as part of SHIN-10 // Create element InventorySnapShot
			 * inventorySnapShotRoot =
			 * multiApiDoc.createElement("InventorySnapShot"); // Set attribute
			 * value ApplyDifference
			 * inventorySnapShotRoot.setAttribute("ApplyDifferences", "Y"); //


			 * Create element ShipNode inventorySnapShotShipNode =
			 * multiApiDoc.createElement("ShipNode"); // Append child element


			 * inventorySnapShotRoot.appendChild(inventorySnapShotShipNode); //
			 * Set attribute value of ShipNode
			 * inventorySnapShotShipNode.setAttribute("ShipNode",
			 * inXMLRoot.getAttribute("ShipNode"));
			 */

			// END: SHIN-10
		}
		// Loop through the NodeList record
		//containerization
		if(!YFCCommon.equals(AcademyConstants.STR_YES, strIWebStoreFlow)){
			
			if(isMultiBox){				
				//Create element Containers
				containersMultiBox = multiApiDoc.createElement("Containers");
			}
			
		for (int i = 0; i < shipmentLineList.getLength(); i++)
		{
			// Fetch the element ShipmentLine
			Element shipmentLine = (Element) shipmentLineList.item(i);
			// Fetch the attribute value of Quantity
			double quantity = Double.parseDouble(shipmentLine.getAttribute("Quantity"));
			// Fethc the value of ItemID
			String itemId = shipmentLine.getAttribute("ItemID");
			// Declare the double variable
			double backroomPickedQuantity = 0;
			double pickedQuantity = 0;
			double shortageQuantity = 0;
			// Check for empty value
			if (!shipmentLine.getAttribute("ShortageQty").trim().equals("")) // SOM Conversion -> in SIM attribute name is :ShortageQuantity
			{
				// Set the variable with attribute value of ShortageQuantity
				shortageQuantity = Double.parseDouble(shipmentLine.getAttribute("ShortageQty")); //SOM Conversion -> in SIM attribute name is :ShortageQuantity
			}
			// Check for empty value
			if (!shipmentLine.getAttribute("BackroomPickedQuantity").trim().equals(""))
			{
				// Set the variable with attribute value of
				// BackroomPickedQuantity
				backroomPickedQuantity = Double.parseDouble(shipmentLine.getAttribute("BackroomPickedQuantity"));
			}
			// Check for empty value
			if (!shipmentLine.getAttribute("PickQuantity").trim().equals("")) //SOM Conversion -> in SIM attribute name is :PickedQuantity
			{
				// Set the variable with attribute value of PickedQuantity
				pickedQuantity = Double.parseDouble(shipmentLine.getAttribute("PickQuantity")); //SOM Conversion -> in SIM attribute name is :PickedQuantity
			}
			// Check if variable pickedQuantity value is zero
			if (pickedQuantity == 0)
			{
				// Check for empty value
				if (!shipmentLine.getAttribute("PickedQuantity1").trim().equals(""))
				{
					// Set the variable with attribute value of PickedQuantity1
					pickedQuantity = Double.parseDouble(shipmentLine.getAttribute("PickedQuantity1"));
				}
			}
			// Add the value of backroomPickedQuantity and pickedQuantity
			double newBackroomPickedQuantity = backroomPickedQuantity + pickedQuantity;
			// Create element ShipmentLine
			Element changeShipmentLine = multiApiDoc.createElement("ShipmentLine");
			// Append the child element
			shipmentLines.appendChild(changeShipmentLine);
			
			// Set the attribute value of ShipmentKey
			changeShipmentLine.setAttribute("ShipmentLineKey", shipmentLine.getAttribute("ShipmentLineKey"));
			// Set the attribute value of BackroomPickedQuantity
			changeShipmentLine.setAttribute("BackroomPickedQuantity", "" + newBackroomPickedQuantity);
			// Check if picked qty is greater than zero
			if (pickedQuantity > 0)
			{
				// Set flag itemPicked
				log.verbose("Inside the condition: pickedQuantity > 0");
				itemPicked = true;
				log.verbose("i'temPicked flag is set to true");
				
				if(isMultiBox){
					
					log.verbose("Inside the condition: isMultiBox");
					//logic to fetch details from lookup table
					///env.setTxnObject("ShipQuantity", pickedQuantity);
					log.verbose("setting the shipquantity in transaction object to "+ pickedQuantity);
					Document multiBoxLookupInpDoc = XMLUtil.createDocument("AcadMultiboxLookup");
					Element multiBoxLookupInpEle = multiBoxLookupInpDoc.getDocumentElement();
					multiBoxLookupInpEle.setAttribute("ItemID",	XMLUtil.getAttributeFromXPath(inXML, "//Shipment/ShipmentLines/ShipmentLine/OrderLine/Item/@ItemID"));
					Document multiBoxLookupOutDoc = AcademyUtil.invokeService(env, "AcademyGetMultiboxLookupList", multiBoxLookupInpDoc);
//					 Check if ItemID is present in the hashmap
					if (itemQuantityMap.containsKey(itemId))
					{
					log.verbose("incrementing the quantity of existing itemid" +itemId);
						// Update ItemID and quantity
						itemQuantityMap.put(itemId, pickedQuantity + itemQuantityMap.get(itemId));
					} else
					{ // Add item ID and picked qty
					   log.verbose("Adding the new itemid to map" + itemId);
						itemQuantityMap.put(itemId, pickedQuantity);
					}
					log.verbose("End of the condition: pickedQuantity > 0");
					
					
					if(null != multiBoxLookupOutDoc){
						
						///String  strMBfactor = multiBoxLookupOutDoc.getDocumentElement().getAttribute("MbFactor");
						//Mbfactor will be equal to number of AcadMultiboxLookup  elements in the AcademyGetMultiboxLookupList output.hence using that
						
						NodeList acadMultiboxLookupList = multiBoxLookupOutDoc.getElementsByTagName("AcadMultiboxLookup");
						int intMbFactor 		= acadMultiboxLookupList.getLength();
						int intCounter 			= 0;
						double totalPckQty = 0.00;
						
						for(int k=0; k < pickedQuantity ; k++){
						for(int j=0;j<intMbFactor;j++){												
							intCounter++;
							double containerQty = 1.00;							
							int numberOfContainers  =  (int) (intMbFactor*pickedQuantity);
							double partsQty     =  roundDecimal((containerQty/numberOfContainers), 4);
							Element multiboxLookupElement = (Element) acadMultiboxLookupList.item(j);  ///(Element) acadMultiboxLookupList.item(j);
							// Create element Container
							Element containerMultiBox = multiApiDoc.createElement("Container");					
							containersMultiBox.appendChild(containerMultiBox);
							
							//Create element ContainerDetails
							Element containerDetailsMultiBox = multiApiDoc.createElement("ContainerDetails");
							
//							Create element ContainerDetail
							Element containerDetailMultiBox = multiApiDoc.createElement("ContainerDetail");
							// Append the child element
							containerDetailsMultiBox.appendChild(containerDetailMultiBox);
							
							//Set the attribute value ShipmentLineKey
							containerDetailMultiBox.setAttribute("ShipmentLineKey", shipmentLine.getAttribute("ShipmentLineKey"));
							
							//Start WN-2980 GC Activation and fulfillment for SI DCs
							Element eleShipmentTagSerials = (Element) shipmentLine.getElementsByTagName(AcademyConstants.ELE_SHIPEMNT_TAG_SERIALS).item(0);
							if (!YFCObject.isVoid(eleShipmentTagSerials)) {					
								containerDetailMultiBox.appendChild((multiApiDoc).importNode(eleShipmentTagSerials, true));
							}
							//End WN-2980 GC Activation and fulfillment for SI DCs
							if(intCounter<numberOfContainers){     //if(intCounter<intMbFactor)
//								 Set the attribute value of Quantity
								containerDetailMultiBox.setAttribute("Quantity", "" + partsQty);
								totalPckQty = roundDecimal((totalPckQty+partsQty), 4);
							}else if (intCounter==numberOfContainers){   //// if (intCounter==intMbFactor)
								double strRes = roundDecimal((containerQty-totalPckQty), 4);
								containerDetailMultiBox.setAttribute("Quantity", "" + strRes);
							}
							containerMultiBox.appendChild(containerDetailsMultiBox);
							String containerScmMultiBox = null;
							
							// Invoke generateScm method and set the value of container scm
							//containerization
							if(YFCCommon.equals(AcademyConstants.STR_YES, strIWebStoreFlow)){
								containerScmMultiBox = inXMLRoot.getAttribute(AcademyConstants.A_CONATINER_SCM);
							}
							else{
								containerScmMultiBox = getContainerScm(env);
							//shn-generate container scm based on shipment line
							}
						
							// Ship From DC Changes :: Start
							// If the Container Type is "VendorPackage" then set the container
							// dimensions as the item dimensions picked else do the regular
							// process

							// Fetch the attribute value of ContainerType
							String strContainerType = inXMLRoot.getAttribute("ContainerType")
									.trim();
							log.verbose("ContainerType --------> " + strContainerType);
							if ("VendorPackage".equalsIgnoreCase(strContainerType)) {
								setContainerVolumeDetailsVndrPkg(env, inXMLRoot, containerMultiBox);
							}
							// Ship From DC Changes :: End
							//Commenting out below else block to add the new dimension calculation logic.
//							else 
//							{
//								// Invoke methof to fetch the volume details of the container
//								Element containerVolumePrimaryInformation = setContainerVolumeDetails(env, inXMLRoot, containerMultiBox);
//								
//								
//								// Fetch the value of containerGrossWeight
//								///Handling the expetion if ContainerGrossWeight is null
//								String containerGrossWeightString = inXMLRoot.getAttribute("ContainerGrossWeight");
//				                double containerGrossWeight = (containerGrossWeightString.isEmpty() || containerGrossWeightString == null) ? 0 : Double.parseDouble(containerGrossWeightString) ;
//								///double containerGrossWeight = Double.parseDouble(inXMLRoot.getAttribute("ContainerGrossWeight"));
//								// Map the value of containerGrossWeight
//								double containerNetWeight = containerGrossWeight;
//								// Check if containerGrossWeight is zero
//								if (containerGrossWeight == 0)
//								{
//									log.verbose("Calling containerNetWeight method from conditio :isMultiBox");
//									// Invoke method to fetch the container Net Weight
//									containerNetWeight = getNetWeightOfItems(env, itemQuantityMap);
//									// Set the container Gross Weight =containerNetWeight +
//									// UnitWeight
//									containerGrossWeight = containerNetWeight + Double.parseDouble(containerVolumePrimaryInformation.getAttribute("UnitWeight"));
//								}
//								// Set the value of ContainerGrossWeight
//								containerMultiBox.setAttribute("ContainerGrossWeight", containerGrossWeight + "");
//								// Set the value of ContainerNetWeight
//								containerMultiBox.setAttribute("ContainerNetWeight", containerNetWeight + "");
//							}
							
							///Adding new weight and dimension calculation logic for Mutibox, have to confirm which to use
							else{
								
								setContainerVolumeDetailsForMultibox(env, inXMLRoot, containerMultiBox, multiboxLookupElement);
							}
							// Set the value of SCAC
							containerMultiBox.setAttribute("SCAC", inXMLRoot.getAttribute("SCAC"));
							// Set the value of CarrierServiceCode
							containerMultiBox.setAttribute("CarrierServiceCode", inXMLRoot.getAttribute("CarrierServiceCode"));
							
							// Set the value of ContainerType
							containerMultiBox.setAttribute("ContainerType", "Case");
							// Set the value of IsPackProcessComplete
							containerMultiBox.setAttribute("IsPackProcessComplete", "Y");
							// Set the value of ContainerScm
						
							
							if(YFCCommon.equals(AcademyConstants.STR_YES, strIWebStoreFlow)){
								containerMultiBox.setAttribute("ShipmentContainerKey", inXMLRoot.getAttribute("ShipmentContainerKey"));
							} else {
								containerMultiBox.setAttribute("ContainerScm", containerScmMultiBox);
							}
							//
							//Duplication the container element in case pickquantity is more than 1
//							for(int k=0; k < pickedQuantity -1 ; k++){
//								Node newContainerNode = containerMultiBox.cloneNode(true);
//								containersMultiBox.appendChild(newContainerNode);
//							}
						}
						}
					}
					
					
					log.verbose("end of the condition: isMultiBox");
				}else{
					log.verbose("AcademyGetMultiboxLookupList returned zero elements");
				// Create element ContainerDetail
				Element containerDetail = multiApiDoc.createElement("ContainerDetail");
				// Append the child element
				containerDetails.appendChild(containerDetail);
				// Set the attribute value ShipmentLineKey
				containerDetail.setAttribute("ShipmentLineKey", shipmentLine.getAttribute("ShipmentLineKey"));
				
				//Start WN-2980 GC Activation and fulfillment for SI DCs
				Element eleShipmentTagSerials = (Element) shipmentLine.getElementsByTagName(AcademyConstants.ELE_SHIPEMNT_TAG_SERIALS).item(0);
				if (!YFCObject.isVoid(eleShipmentTagSerials)) {					
					containerDetail.appendChild((multiApiDoc).importNode(eleShipmentTagSerials, true));
				}
				//End WN-2980 GC Activation and fulfillment for SI DCs
				
				// Set the attribute value of Quantity
				containerDetail.setAttribute("Quantity", "" + pickedQuantity);
				}
				
				
			}
			
			// Check for Inventory Shortage
			if (isInventoryShortage)
			{    
				//Start : STL 397 fix
                if (env.getTxnObject("CallChargesUEFromPrintFlow") == null) {
                        
                        log.verbose("***inside if check for CallChargesUEFromPrintFlow***");
                        String strCallChargesUEFromPrintFlow = "Y";
                        env.setTxnObject("CallChargesUEFromPrintFlow", strCallChargesUEFromPrintFlow);
                        log.verbose("CallChargesUEFromPrintFlow Txn Obj is set");
                    }
                //End : STL 397 fix
				
			    //Start : STL 246
	            // Set the attribute value of DocumentType
	            changeShipmentRoot.setAttribute("DocumentType", inXMLRoot.getAttribute("DocumentType"));
	            //End : STL 246
				// Set the attribute value of Quantity
				changeShipmentLine.setAttribute("Quantity", "" + newBackroomPickedQuantity);
				// Check for new BackRoom Picked qty is greater than zero
				if (newBackroomPickedQuantity > 0)
				{
					// Set the attribute Action
					changeShipmentRoot.setAttribute("Action", "");
					// Set the flag
					isPickComplete = true;
				}

				// START: SHIN-10
				// Commented as part of SHIN-10

				// Check if Shortage Qty is greater than zero
				/*
				 * if (shortageQuantity > 0) { // Fetch the element Item Element
				 * item = (Element)
				 * shipmentLine.getElementsByTagName("Item").item(0); // Create
				 * element Item Element inventorySnapShotItem =
				 * multiApiDoc.createElement("Item"); // Append the child
				 * inventorySnapShotShipNode.appendChild(inventorySnapShotItem); //
				 * Set the attribute value UnitOfMeasure
				 * inventorySnapShotItem.setAttribute("UnitOfMeasure",
				 * item.getAttribute("UnitOfMeasure")); // Set the attribute
				 * value ProductClass
				 * inventorySnapShotItem.setAttribute("ProductClass",
				 * item.getAttribute("ProductClass")); // Set the attribute
				 * value of ItemID inventorySnapShotItem.setAttribute("ItemID",
				 * item.getAttribute("ItemID")); // Set the attribute value of
				 * InventoryOrganizationCode
				 * inventorySnapShotItem.setAttribute("InventoryOrganizationCode",
				 * "DEFAULT"); // Create the element SupplyDetails Element
				 * inventorySnapShotSupplyDetails =
				 * multiApiDoc.createElement("SupplyDetails"); // Append the
				 * child element
				 * inventorySnapShotItem.appendChild(inventorySnapShotSupplyDetails); //
				 * Set the attribute SupplyType
				 * inventorySnapShotSupplyDetails.setAttribute("SupplyType",
				 * "ONHAND"); // Set the attribute Quantity
				 * inventorySnapShotSupplyDetails.setAttribute("Quantity", "0"); }
				 */

				// END: SHIN-10
		
				

			}
		}
		}
		//containerization
		else{
			itemPicked=true;
		}
		// Declare string variable
	
		String containerScm = "";
		if(YFCCommon.equals(AcademyConstants.STR_YES, strIWebStoreFlow)){
			containerScm = inXMLRoot.getAttribute(AcademyConstants.A_CONATINER_SCM);
		}
		
		// Check if Item Picked
		if (itemPicked)
		{
			log.verbose("Inside itemPicked condition");
			if(!isMultiBox){
				log.verbose("Inside !isMultiBox condition");
			// Invoke generateScm method and set the value of container scm
			//containerization
			if(YFCCommon.equals(AcademyConstants.STR_YES, strIWebStoreFlow)){
				containerScm = inXMLRoot.getAttribute(AcademyConstants.A_CONATINER_SCM);
			}
			else{
			containerScm = getContainerScm(env);
			}
		
			// Ship From DC Changes :: Start
			// If the Container Type is "VendorPackage" then set the container
			// dimensions as the item dimensions picked else do the regular
			// process

			// Fetch the attribute value of ContainerType
			String strContainerType = inXMLRoot.getAttribute("ContainerType")
					.trim();
			log.verbose("ContainerType --------> " + strContainerType);
			if ("VendorPackage".equalsIgnoreCase(strContainerType)) {
				setContainerVolumeDetailsVndrPkg(env, inXMLRoot, container);
			}
			// Ship From DC Changes :: End
			else 
			{
				// Invoke methof to fetch the volume details of the container
				Element containerVolumePrimaryInformation = setContainerVolumeDetails(env, inXMLRoot, container);
				// Fetch the value of containerGrossWeight
				//Fix to handle the exception if ContainergrossWeight is empty
				String containerGrossWeightString = inXMLRoot.getAttribute("ContainerGrossWeight");
				double containerGrossWeight = (containerGrossWeightString.isEmpty() || containerGrossWeightString == null) ? 0 : Double.parseDouble(containerGrossWeightString);
				// Map the value of containerGrossWeight
				double containerNetWeight = containerGrossWeight;
				// Check if containerGrossWeight is zero
				if (containerGrossWeight == 0){
					// Invoke method to fetch the container Net Weight
					log.verbose("Calling containerNetWeight method from conditio :NOT isMultiBox");
					containerNetWeight = getNetWeightOfItems(env, itemQuantityMap);
					// Set the container Gross Weight =containerNetWeight +
					// UnitWeight
					containerGrossWeight = containerNetWeight + Double.parseDouble(containerVolumePrimaryInformation.getAttribute("UnitWeight"));
				}
				// Set the value of ContainerGrossWeight
				container.setAttribute("ContainerGrossWeight", containerGrossWeight + "");
				// Set the value of ContainerNetWeight
				container.setAttribute("ContainerNetWeight", containerNetWeight + "");
				
				
				String strIWebStore = (String) env.getTxnObject(AcademyConstants.A_IS_WEB_STORE_FLOW);
				if (YFCCommon.equalsIgnoreCase(strIWebStore, AcademyConstants.STR_YES)){
					container.setAttribute(AcademyConstants.ATTR_ACTUAL_WEIGHT, containerNetWeight + "");
				}
			}
			// Set the value of SCAC
			container.setAttribute("SCAC", inXMLRoot.getAttribute("SCAC"));
			// Set the value of CarrierServiceCode
			container.setAttribute("CarrierServiceCode", inXMLRoot.getAttribute("CarrierServiceCode"));
			
			// Set the value of ContainerType
			container.setAttribute("ContainerType", "Case");
			// Set the value of IsPackProcessComplete
			container.setAttribute("IsPackProcessComplete", "Y");
			// Set the value of ContainerScm
		
			
				
			if(YFCCommon.equals(AcademyConstants.STR_YES, strIWebStoreFlow)){
			container.setAttribute("ShipmentContainerKey", inXMLRoot.getAttribute("ShipmentContainerKey"));
			} else {
				container.setAttribute("ContainerScm", containerScm);
				}		
				
				log.verbose("End of !isMultibox condtion");
				
			}
			
			// Create the root element shipment
			Document multiApiDoc2 = XMLUtil.createDocument("Shipment");
			Element multiApiDocRoot2 = multiApiDoc2.getDocumentElement();
			//Set attribute value of ShipmentKey
			multiApiDocRoot2.setAttribute("ShipmentKey", inXMLRoot.getAttribute("ShipmentKey"));
			if(isMultiBox){
				
				//Append child elemet Containers
				multiApiDocRoot2.appendChild(multiApiDoc2.importNode(containersMultiBox, true));
				changeShipmentRoot.appendChild(containersMultiBox);
				if (log.isVerboseEnabled()) {
					log.verbose("ProcessBackroomPick: performBackroomPick: containers: " + 
						XMLUtil.getXMLString(multiApiDoc2));
				}
			}else{
				
				//Append child elemet Containers
				multiApiDocRoot2.appendChild(multiApiDoc2.importNode(containers, true));
				changeShipmentRoot.appendChild(containers);
				if (log.isVerboseEnabled()) {
					log.verbose("ProcessBackroomPick: performBackroomPick: containers: " + 
						XMLUtil.getXMLString(multiApiDoc2));
				}
				
			}
			
			
			

		}

		if (isInventoryShortage && !isPickComplete)
		{
			// in case of cancel make another change shipment call
			changeShipmentOnCancel(multiApiDoc, inXMLRoot);
		}

		// Invoke method to append the changeShipmet element to MutiApi
		includeInMultiApi(multiApiDoc, changeShipmentRoot, "changeShipment");

		// If Pick completed
		//STL 987: Added the status condition also for doing changeShipmentStatus
		if (isPickComplete && strStaus.equals("1100.70.06.10"))
		{
			// Create root element Shipment
			Element changeShipmentStatusRoot = multiApiDoc.createElement("Shipment");
			// Set the attribute value of ShipmentKEy
			changeShipmentStatusRoot.setAttribute("ShipmentKey", inXMLRoot.getAttribute("ShipmentKey"));
			
			if(!YFCCommon.equals(AcademyConstants.STR_YES, strIWebStoreFlow)){
				changeShipmentStatusRoot.setAttribute("BaseDropStatus", "1100.70.06.30");
			} else {
				changeShipmentStatusRoot.setAttribute("BaseDropStatus", "1100.70.06.30.5");
			}
			// Set the attribute value of TransactionId
			changeShipmentStatusRoot.setAttribute("TransactionId", "YCD_BACKROOM_PICK");//SOM Conversion-> in SIM this is SOP_BACKROOM_PICK
			// Invoke method to append the changeShipmentStatus element to
			// MultiApi
			includeInMultiApi(multiApiDoc, changeShipmentStatusRoot, "changeShipmentStatus");
			
			//START STL-1435 Packing in SI DCs doesn't generate ShipAckMsg to Exeter
			//Set env obj POST_ACK_MSG="N". Tracking no is not yet generated.
			log.verbose("setting the Transaction obj POST_ACK_MSG = N");
			env.setTxnObject(AcademyConstants.STR_POST_ACK_MSG, AcademyConstants.STR_NO);
			//END STL-1435 Packing in SI DCs doesn't generate ShipAckMsg to Exeter
		}
		
		//START: SHIN-10
		//Commented as part of SHIN-10

		/*// If Inventory Shortage
		if (isInventoryShortage)
		{
			// Invoke method to append InventorySnapShot element to MultiApi
			includeInMultiApi(multiApiDoc, inventorySnapShotRoot, "getInventoryMismatch");
		}*/
		
		//END: SHIN-10
		// Print the verbose log
		if (log.isVerboseEnabled())
		{
			log.verbose("ProcessBackroomPick: performBackroomPick: multiApiDoc: " + XMLUtil.getXMLString(multiApiDoc));
		}
		// Set the template for changeShipmentStatus API
		env.setApiTemplate("changeShipmentStatus", YFCDocument.getDocumentFor("<Shipment ShipNode=\"\" ShipmentNo=\"\" ShipmentType=\"\" ShipmentKey=\"\" Status=\"\"/>").getDocument());
		// Set the template for changeShipment API
		env.setApiTemplate("changeShipment", YFCDocument.getDocumentFor("<Shipment Status=\"\" ShipNode=\"\" ShipmentNo=\"\" ShipmentType=\"\" ShipmentKey=\"\"><Containers><Container ShipmentContainerKey=\"\" ContainerScm=\"\" IsManifested=\"\"/></Containers></Shipment>").getDocument());
		// Invoke Service
		
		Document multiApiOutputDoc = AcademyUtil.invokeService(env, "AcademySFSProcessBackroomMultiApi", multiApiDoc);


		
		// START:SHIN-10
		if (isInventoryShortage ) {

			/*
			 * invoking multiApi demand summary as part of partial short pick
			 * and complete short pick
			 */
			log
			.verbose("**************invoking multiApi Demand Summary*********");
			Document getDemandSummaryOutDoc = invokeMultiApiDemandSummary(env,
					inXML, strShipNode);

			/*
			 * invoking getInventoryMismatch API as part of partial short pick
			 * and complete short pick
			 */
			

			log.verbose("**************invoking getInventoryMismatch*********");
			invokeGetInventoryMismatch(env, strShipNode, getDemandSummaryOutDoc);

		}

		// END:SHIN-10
		String strStatusFromMultiApiOut = "";
		if(!AcademyConstants.STR_YES.equals(strIWebStoreFlow)){
			String strShipmentKeyFromMultiApiOut = XPathUtil.getString(multiApiOutputDoc, "/MultiApi/API/Output/Shipment/@ShipmentKey");
			String strShipNodeFromMultiApiOut = XPathUtil.getString(multiApiOutputDoc, "/MultiApi/API/Output/Shipment/@ShipNode");
			//String strStatusFromMultiApiOut = XPathUtil.getString(multiApiOutputDoc, "/MultiApi/API/Output/Shipment/@Status");
			strStatusFromMultiApiOut = (String) XPathUtil.getString(
					multiApiOutputDoc, "MultiApi/API[@Name='changeShipmentStatus']/Output/Shipment/@Status");
			
			//START: SHIN-15
			/*The logic to form the input to getShipNode API and invoke. 
			From the Output XML fetch the attribute NodeType and condition check whether it is SharedInventoryDC*/
			
			getShipNodeListInputDoc = XMLUtil.createDocument(AcademyConstants.SHIP_NODE);
			eleRootElement = getShipNodeListInputDoc.getDocumentElement();
	
			// setting the value for attribute ShipNode
			
			eleRootElement.setAttribute(AcademyConstants.SHIP_NODE, strShipNode);
			if (log.isVerboseEnabled()) {
				log.verbose("getShipNodeList API inXML:"+ XMLUtil.getXMLString(getShipNodeListInputDoc));
			}
			
			// Set the template for getShipNodeList API
			
			templateDoc = YFCDocument.getDocumentFor("<ShipNodeList><ShipNode ShipNode=\"\" NodeType=\"\" /></ShipNodeList>").getDocument();
			env.setApiTemplate(AcademyConstants.API_GET_SHIP_NODE_LIST,templateDoc);
	
			// invoking the API getShipNodeList
			getShipNodeListOutputDoc = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_SHIP_NODE_LIST,getShipNodeListInputDoc);
			env.clearApiTemplate(AcademyConstants.API_GET_SHIP_NODE_LIST);
	
			if (log.isVerboseEnabled()) {
				log.verbose("getShipNodeList API outXML:"+ XMLUtil.getXMLString(getShipNodeListOutputDoc));
			}
	
			// Fetch the attribute ShipNode from the output xml
			eleOutShipNode = (Element) getShipNodeListOutputDoc.getElementsByTagName(AcademyConstants.SHIP_NODE).item(0);
	
			if (eleOutShipNode != null && !YFCObject.isVoid(eleOutShipNode)) {
	
				// Fetch NodeType Value
				strNodeType = eleOutShipNode.getAttribute(AcademyConstants.ATTR_NODE_TYPE);
				}
	
			
		    //Changed the condition as part of SHIN-15
			//if(strShipNodeFromMultiApiOut.equals(AcademyConstants.ATTR_SI_SHIP_NODE) && strStatusFromMultiApiOut.equals("1100.70.06.30") ){
			//STL-1677 Begin : This code is moved down as TrackingNo is not available here.
			/*
			if(AcademyConstants.ATTR_VAL_SHAREDINV_DC.equals(strNodeType) && strStatusFromMultiApiOut.equals("1100.70.06.30") ){
				  
			//END: SHIN-15
			
			Document getShipmentListInDoc = XMLUtil.createDocument("Shipment");
			getShipmentListInDoc.getDocumentElement().setAttribute("ShipmentKey", strShipmentKeyFromMultiApiOut);
			env.setApiTemplate("getShipmentList", "global/template/api/getShipmentListForSharedInventoryShipAckMsg.xml");
			log.verbose("Input to getShipmentList API for SharedInventory::" + XMLUtil.getXMLString(getShipmentListInDoc));
			Document outDocgetShipmentList=AcademyUtil.invokeAPI(env, "getShipmentList", getShipmentListInDoc);
			log.verbose("Output of getShipmentList API for SharedInventory::" + XMLUtil.getXMLString(outDocgetShipmentList));
			env.clearApiTemplate("getShipmentList");
			prepareShipAckMsgToExeter(env,outDocgetShipmentList);
			
			}
			*/
			//STL-1677 End
	
			if (log.isVerboseEnabled())
			{
				log.verbose("ProcessBackroomPick: performBackroomPick: multiApiOutputDoc: " + XMLUtil.getXMLString(multiApiOutputDoc));
			}
			
			log.verbose("ProcessBackroomPick: performBackroomPick: isInventoryShortage: " + isInventoryShortage + " extnInvoiceNo :" + extnInvoiceNo);
			
			// Start change for STL-244
			if (isInventoryShortage && (!StringUtil.isEmpty(extnInvoiceNo)))
			{
				//Call Methode to Update current Invoice No to have the old ExtnInvoiceNo
				log.verbose("ProcessBackroomPick: performBackroomPick: Inside  (isInventoryShortage && (!StringUtil.isEmpty(extnInvoiceNo)) Check ");
				updateExtnInvoiceNo(env, inXMLRoot, extnInvoiceNo);
			}		
			// End Change for STL-244
		}
		if(isMultiBox){
			if(null != multiApiOutputDoc){  /// isPickComplete
				
				//Fetch the NodeList of node Container from the output document of
				// multiApi call
				NodeList createdContainerList = multiApiOutputDoc.getElementsByTagName("Container");
				// Declate element variable
				Element createdContainer = null;
				String  isManifestedFlag = null;
				
				String shipmentContainerKey = "";
				// Loop through the NodeList record
				for (int i = 0; i < createdContainerList.getLength(); i++)
				{
					// Fetch the element Container
					createdContainer = (Element) createdContainerList.item(i);
					isManifestedFlag = createdContainer.getAttribute("IsManifested");
					
					if(!isManifestedFlag.equalsIgnoreCase("Y")){
						
						// Fetch the attribute value ContainerScm
						String newContainerScm = createdContainer.getAttribute("ContainerScm").trim();
							
						// Fetch the value of ShipmentContainerKey
						shipmentContainerKey = createdContainer.getAttribute("ShipmentContainerKey").trim();
						
						//Fetch the element Shipment
						Element shipment = (Element) createdContainer.getParentNode().getParentNode();
						// Fetch the attribute ShipmentType
						String strShipmentType = shipment.getAttribute("ShipmentType");
						// Fetch the attribute value of ShipNode
						String shipNode = shipment.getAttribute("ShipNode");
						// Check for conveyable shipmenttype
						//STL-810: Condition added for ammo shipment type
						//Start :Changes made for STL-934 Checkout Funnel
						//Start : Changes made for STL-1320 Checkout Funnel OVNT
						//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
						if (strShipmentType.equals("CON") || strShipmentType.equals("CONOVNT") || strShipmentType.equals("GC") || 
								strShipmentType.equals(AcademyConstants.STR_GC_ONLY_SHIP_TYPE) ||
								strShipmentType.equals(AcademyConstants.AMMO_SHIPMENT_TYPE)|| strShipmentType.equals(AcademyConstants.ATTR_CSA_SHIPMENT_TYPE)
								|| AcademyConstants.HAZMAT_SHIPMENT_TYPE.equals(strShipmentType) || strShipmentType.equals(AcademyConstants.ATTR_CSA_OVNT_SHIPMENT_TYPE))
							//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
							//End : Changes made for STL-1320 Checkout Funnel OVNT  
							//End :Changes made for STL-934 Checkout Funnel
						{
							// Invoke method to process container details for conveyable
							processContainerDetailsForNonBulk(env, shipmentContainerKey, shipNode);
						} else
						{
							// Invoke method to process container details for bulk
							processContainerDetailsForBulk(env, shipmentContainerKey, shipNode);
						}
						
						
					}
					

				}
				
			}
			
			
		}else{
			
		// Declare string variable
		String shipmentContainerKey = "";
		// Check if the value is not empty
		if (!containerScm.equals(""))
		{
			
			// Fetch the NodeList of node Container from the output document of
			// multiApi call
			NodeList createdContainerList = multiApiOutputDoc.getElementsByTagName("Container");
			// Declate element variable
			Element createdContainer = null;
			// Loop through the NodeList record
			for (int i = 0; i < createdContainerList.getLength(); i++)
			{
				// Fetch the element Container
				createdContainer = (Element) createdContainerList.item(i);
				// Fetch the attribute value ContainerScm
				String newContainerScm = createdContainer.getAttribute("ContainerScm").trim();
				// check the value
				if (newContainerScm.equals(containerScm))
				{
					
					// Fetch the value of ShipmentContainerKey
					shipmentContainerKey = createdContainer.getAttribute("ShipmentContainerKey").trim();
					break;
				}
			}
			// Fetch the element Shipment
			Element shipment = (Element) createdContainer.getParentNode().getParentNode();
			// Fetch the attribute ShipmentType
			String strShipmentType = shipment.getAttribute("ShipmentType");
			// Fetch the attribute value of ShipNode
			String shipNode = shipment.getAttribute("ShipNode");
			// Check for conveyable shipmenttype
			//STL-810: Condition added for ammo shipment type
			//Start :Changes made for STL-934 Checkout Funnel
			//Start : Changes made for STL-1320 Checkout Funnel OVNT
			//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
			if (strShipmentType.equals("CON") || strShipmentType.equals("CONOVNT") || strShipmentType.equals("GC") || 
					strShipmentType.equals(AcademyConstants.STR_GC_ONLY_SHIP_TYPE) ||
					strShipmentType.equals(AcademyConstants.AMMO_SHIPMENT_TYPE)|| strShipmentType.equals(AcademyConstants.ATTR_CSA_SHIPMENT_TYPE)
					|| AcademyConstants.HAZMAT_SHIPMENT_TYPE.equals(strShipmentType) || strShipmentType.equals(AcademyConstants.ATTR_CSA_OVNT_SHIPMENT_TYPE))
				//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
				//End : Changes made for STL-1320 Checkout Funnel OVNT  
				//End :Changes made for STL-934 Checkout Funnel
			{
				// Invoke method to process container details for conveyable
				processContainerDetailsForNonBulk(env, shipmentContainerKey, shipNode);
			} else
			{
				// Invoke method to process container details for bulk
				processContainerDetailsForBulk(env, shipmentContainerKey, shipNode);
			}
		}
		
		

			// Start change for STL-244
			// This code chagne will update shipment with the current
			// ExtnInvoiceNo only when Shipment is getting picked first time. This will be used later to update ExtnInvoiceNo on
			// newly created Order Invoice.
			if (StringUtil.isEmpty(extnInvoiceNo))
			{
				String extnInvoiceNoFromTxn = (String) env.getTxnObject("SFS_ExtnInvoiceNo");
				log.verbose("extnInvoiceNo from TxnObj: " + extnInvoiceNoFromTxn);
				
				if (!StringUtil.isEmpty(extnInvoiceNoFromTxn))
				{
					
						// Keeping the ExtnInvoiceNo in Shipment extended coloumn.
						// Call ChangeShipment to store extnInvoiceNo From TransactionObject.
						Document changeShipmentForExtnInvoice = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
						Element changeShipmentForExtnInvoiceRootElm = changeShipmentForExtnInvoice.getDocumentElement();
						changeShipmentForExtnInvoiceRootElm.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, inXMLRoot.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
						changeShipmentForExtnInvoiceRootElm.setAttribute(AcademyConstants.ATTR_ACTION, "");
						Element changeShipmentForExtnInvoiceExtnElm = changeShipmentForExtnInvoice.createElement(AcademyConstants.ELE_EXTN);
						changeShipmentForExtnInvoiceExtnElm.setAttribute(AcademyConstants.ATTR_EXTN_INVOICE_NO, extnInvoiceNoFromTxn);
						changeShipmentForExtnInvoiceRootElm.appendChild(changeShipmentForExtnInvoiceExtnElm);
						if (log.isVerboseEnabled()) {
							log.verbose("ProcessBackroomPick: performBackroomPick: changeShipmentForExtnInvoice: " + XMLUtil.getXMLString(changeShipmentForExtnInvoice));
						}
						AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, changeShipmentForExtnInvoice);					
				}
			}
			// End Change for STL-244
			
			//Start - Commented out the code as a part of WN-366  
			/*//STL-1677 Begin : Publish the Ship ack msg with TrackingNo			
			if(AcademyConstants.ATTR_VAL_SHAREDINV_DC.equals(strNodeType) && strStatusFromMultiApiOut.equals("1100.70.06.30") ){
				//START STL-1435 Packing in SI DCs doesn't generate ShipAckMsg to Exeter
					Document getShipmentListInDoc = XMLUtil.createDocument("Shipment");
				getShipmentListInDoc.getDocumentElement().setAttribute("ShipmentKey", strShipmentKeyFromMultiApiOut);
				env.setApiTemplate("getShipmentList", "global/template/api/getShipmentListForSharedInventoryShipAckMsg.xml");
				log.verbose("Input to getShipmentList API for SharedInventory::" + XMLUtil.getXMLString(getShipmentListInDoc));
				Document outDocgetShipmentList=AcademyUtil.invokeAPI(env, "getShipmentList", getShipmentListInDoc);
				log.verbose("Output of getShipmentList API for SharedInventory::" + XMLUtil.getXMLString(outDocgetShipmentList));
				env.clearApiTemplate("getShipmentList");
				prepareShipAckMsgToExeter(env,outDocgetShipmentList);
				
				log.verbose("setting the Transaction obj POST_ACK_MSG to blank");
				env.setTxnObject(AcademyConstants.STR_POST_ACK_MSG, AcademyConstants.STR_EMPTY_STRING);				
				AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_PREPARE_SHIP_ACK_MSG_TO_EXETER_FOR_SI, inShipmentListDoc);
				//END STL-1435 Packing in SI DCs doesn't generate ShipAckMsg to Exeter
				
			}
			//STL-1677 End 	
*/			//End - Commented out the code as a part of WN-366 
			
			}
		
		//Start - Moved the code out of the loop as a part of WN-366 
		//STL-1677 Begin : Publish the Ship ack msg with TrackingNo			
		if(AcademyConstants.ATTR_VAL_SHAREDINV_DC.equals(strNodeType) && strStatusFromMultiApiOut.equals(AcademyConstants.VAL_READY_TO_SHIP_STATUS) ){
			
			//START STL-1435 Packing in SI DCs doesn't generate ShipAckMsg to Exeter
			/*	Document getShipmentListInDoc = XMLUtil.createDocument("Shipment");
			getShipmentListInDoc.getDocumentElement().setAttribute("ShipmentKey", strShipmentKeyFromMultiApiOut);
			env.setApiTemplate("getShipmentList", "global/template/api/getShipmentListForSharedInventoryShipAckMsg.xml");
			log.verbose("Input to getShipmentList API for SharedInventory::" + XMLUtil.getXMLString(getShipmentListInDoc));
			Document outDocgetShipmentList=AcademyUtil.invokeAPI(env, "getShipmentList", getShipmentListInDoc);
			log.verbose("Output of getShipmentList API for SharedInventory::" + XMLUtil.getXMLString(outDocgetShipmentList));
			env.clearApiTemplate("getShipmentList");
			prepareShipAckMsgToExeter(env,outDocgetShipmentList);
			*/
			log.verbose("setting the Transaction obj POST_ACK_MSG to blank");
			env.setTxnObject(AcademyConstants.STR_POST_ACK_MSG, AcademyConstants.STR_EMPTY_STRING);				
			AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_PREPARE_SHIP_ACK_MSG_TO_EXETER_FOR_SI, inShipmentListDoc);
			//END STL-1435 Packing in SI DCs doesn't generate ShipAckMsg to Exeter
			
		}
		//STL-1677 End 
		//End - Moved the code out of the loop as a part of WN-366
		
		//EFP-17 EFW Carrier Integration - Start
		if(null != multiApiOutputDoc){
			
			try{
				String strShipmentType = XPathUtil.getString(multiApiOutputDoc, 
						"/MultiApi/API[@Name='changeShipmentStatus']/Output/Shipment/@ShipmentType");
				String strShipmentStatus = XPathUtil.getString(multiApiOutputDoc, 
						"/MultiApi/API[@Name='changeShipmentStatus']/Output/Shipment/@Status");
				
				String strBolOnPack = props.getProperty("AcademySendBOLOnPack");
				
				if(AcademyConstants.SHIPMENT_TYPE_WG.equalsIgnoreCase(strShipmentType) 
						&& AcademyConstants.VAL_READY_TO_SHIP_STATUS.equals(strShipmentStatus)
						&& AcademyConstants.STR_YES.equalsIgnoreCase(strBolOnPack)){
					
					String strShipmentKeyNew = XPathUtil.getString(multiApiOutputDoc, "/MultiApi/API/Output/Shipment/@ShipmentKey");
					Document docShipmentDetails = getShipmentDetails(env, strShipmentKeyNew);
					
					if(null != docShipmentDetails){
						
						Element shipmentEle = XMLUtil
							.getFirstElementByName(docShipmentDetails.getDocumentElement(), "Shipment");
						String raiseEventPart1 = "<RaiseEvent TransactionId=\"ADD_TO_CONTAINER\" EventId=\"ON_SHIPMENT_PACK_COMPLETE\"><DataType>1</DataType><XMLData><![CDATA[ ";
						String raiseEventPart2 = " ]]></XMLData></RaiseEvent>";
						Document raiseEventInputDoc = YFCDocument.getDocumentFor(raiseEventPart1 + XMLUtil.getElementXMLString(shipmentEle) + raiseEventPart2).getDocument();
										
						if (log.isVerboseEnabled())
						{
							log.verbose("ProcessBackroomPick: on pack shipment: raiseEventInputDoc: " + XMLUtil.getXMLString(raiseEventInputDoc));
						}						
						AcademyUtil.invokeAPI(env, "raiseEvent", raiseEventInputDoc);
					}
				}		
				
			}catch(Exception e){
				log.verbose("Exception during raiseEvent for WG:::" +e);
			}
		}
		//EFP-17 EFW Carrier Integration - End
		
		
	}
	
	//START STL-1435 Commenting out as this method is moved to new class AcademyPrepareShipAckMsgToExeter
	/*public void prepareShipAckMsgToExeter(YFSEnvironment env,Document getShipmentListOutDoc) throws Exception
	{
		Element eleShipmentLines = null;
		//STL-1677 Begin  Shipment Ack - Capture Carrier and Tracking #
		Element eleTrackingLines = null;
		Element eleTrackingLine = null;
		Element eleContainer = null;
		//STL-1677 End

		Element eleShipments = getShipmentListOutDoc.getDocumentElement();
		Element eleExtn1= (Element)eleShipments.getElementsByTagName("Extn").item(0);
		Element eleShipment = (Element)eleShipments.getElementsByTagName("Shipment").item(0);
		Document shipAckMsgToExeterDoc = XMLUtil.createDocument("Shipment");
		shipAckMsgToExeterDoc.getDocumentElement().setAttribute("ShipmentNo", eleShipment.getAttribute("ShipmentNo"));
		shipAckMsgToExeterDoc.getDocumentElement().setAttribute("ShipNode", eleShipment.getAttribute("ShipNode"));

		//STL-1677 Begin  Shipment Ack - Capture Carrier and Tracking #
		shipAckMsgToExeterDoc.getDocumentElement().setAttribute(AcademyConstants.CARRIER_SERVICE_CODE, eleShipment.getAttribute(AcademyConstants.CARRIER_SERVICE_CODE));
		shipAckMsgToExeterDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SCAC, eleShipment.getAttribute(AcademyConstants.ATTR_SCAC));
		//STL-1677 End

		Element eleExtn = shipAckMsgToExeterDoc.createElement("Extn");
		eleExtn.setAttribute("ContainerId", eleExtn1.getAttribute("ExtnExeterContainerId"));
		eleExtn.setAttribute("RMSTransferNo", eleExtn1.getAttribute("ExtnRMSTransferNo"));
		eleExtn.setAttribute("MessageType", "ShpAck");
		YFCDate yfcDate = new YFCDate(new Date());
		String strDateFormat = "yyyy-MM-dd'T'HH:mm:ssZ";
		SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
		StringBuilder str = new StringBuilder(sDateFormat.format(yfcDate));
		str.insert(22, ":");
		eleExtn.setAttribute("Receipt_date", String.valueOf(str));
		shipAckMsgToExeterDoc.getDocumentElement().appendChild(eleExtn);
		NodeList shipmentLineNL = XPathUtil.getNodeList(eleShipments, "/Shipments/Shipment/ShipmentLines/ShipmentLine");
		NodeList containersNL = XPathUtil.getNodeList(eleShipments, AcademyConstants.XPATH_CONTAINER_PATH);//STL-1677
		eleShipmentLines = shipAckMsgToExeterDoc.createElement("ShipmentLines");
		eleTrackingLines = shipAckMsgToExeterDoc.createElement(AcademyConstants.ELE_TRACKING_LINES);//STL-1677
		shipAckMsgToExeterDoc.getDocumentElement().appendChild(eleShipmentLines);
		shipAckMsgToExeterDoc.getDocumentElement().appendChild(eleTrackingLines);//STL-1677
		for(int i=0;i<shipmentLineNL.getLength();i++){
			Element eleShipmentLine = (Element)shipmentLineNL.item(i);			
			//docOutPublishXML.getDocumentElement().appendChild(docOutPublishXML.importNode(docOutgetShipmentList.getDocumentElement(), true));
			shipAckMsgToExeterDoc.getElementsByTagName("ShipmentLines").item(0).appendChild(shipAckMsgToExeterDoc.importNode(eleShipmentLine,true));
		}
		//STL-1677 Begin Shipment Ack - Capture Carrier and Tracking #
		if( containersNL != null ) {
			for(int iContCount = 0 ; iContCount < containersNL.getLength() ; iContCount++) {
				eleTrackingLine = shipAckMsgToExeterDoc.createElement(AcademyConstants.ELE_TRACKING_LINE);
				eleContainer = (Element) containersNL.item(iContCount);
				eleTrackingLine.setAttribute(AcademyConstants.ATTR_TRACKING_NO, eleContainer.getAttribute(AcademyConstants.ATTR_TRACKING_NO));			
				shipAckMsgToExeterDoc.getElementsByTagName(AcademyConstants.ELE_TRACKING_LINES).item(0).appendChild(eleTrackingLine);
			}
		}
		//STL-1677 End Shipment Ack - Capture Carrier and Tracking #
		log.verbose("Acknowledgement Msg posted to Exeter::" + XMLUtil.getXMLString(shipAckMsgToExeterDoc));
		AcademyUtil.invokeService(env, "AcademyPublishShipAckMsg", shipAckMsgToExeterDoc);
		
	}*/
	//END STL-1435 Commenting out as this method is moved to new class AcademyPrepareShipAckMsgToExeter
	/**
	 * Added as part of STL-244 
	 * Update ExtnInvoiceNo to the current Porforma Invoice.
	 * @param env
	 * 			<br/> - YFSEnvironment
	 * @param inXMLRoot
	 * 			<br/> - Element
	 * @param extnInvoiceNo
	 * 			<br/> - String
	 */

	private void updateExtnInvoiceNo(YFSEnvironment env, Element inXMLRoot, String extnInvoiceNo) throws Exception
	{
		// Call getOrderInvoiceList to get the current ProForma Invoice
		Document getOrderInvoiceListDoc = XMLUtil.createDocument(AcademyConstants.ELE_ORDER_INVOICE);
		Element getOrderInvoiceListDocElm = getOrderInvoiceListDoc.getDocumentElement();
		getOrderInvoiceListDocElm.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, inXMLRoot.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
		getOrderInvoiceListDocElm.setAttribute(AcademyConstants.ATTR_INVOICE_TYPE, AcademyConstants.STR_PRO_FORMA_INVOICE);
		Document getOrderInvoiceListOPTemplate = YFCDocument.parse("<OrderInvoiceList TotalOrderInvoiceList=\"\"><OrderInvoice InvoiceNo=\"\" InvoiceType=\"\" OrderInvoiceKey=\"\"  ShipmentKey=\"\" > </OrderInvoice> </OrderInvoiceList>").getDocument();
		if (log.isVerboseEnabled()) {
			log.verbose("ProcessBackroomPick: performBackroomPick: getOrderInvoiceListDoc: "
					+ XMLUtil.getXMLString(getOrderInvoiceListDoc));
		}
		env.setApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_LIST, getOrderInvoiceListOPTemplate);
		Document getOrderInvoiceListOPDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_INVOICE_LIST, getOrderInvoiceListDoc);
		env.clearApiTemplates();
		if (log.isVerboseEnabled()) {
			log.verbose("getOrderInvoiceList API output: " + XMLUtil.getXMLString(getOrderInvoiceListOPDoc));
		}
		// Call ChangeOrderInvoice to update old ExtnInvoiceNo to the current Proforma Invoice
		Document changeOrderInvoiceIPDoc = XMLUtil.createDocument(AcademyConstants.ELE_ORDER_INVOICE);
		Element orderInvoiceElm = (Element) getOrderInvoiceListOPDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_ORDER_INVOICE).item(0);
		Element changeOrderInvoiceIPDocRootElm = changeOrderInvoiceIPDoc.getDocumentElement();
		changeOrderInvoiceIPDocRootElm.setAttribute(AcademyConstants.ATTR_ORDER_INVOICE_KEY, orderInvoiceElm.getAttribute(AcademyConstants.ATTR_ORDER_INVOICE_KEY));
		Element changeOrderInvoiceExtnElm = changeOrderInvoiceIPDoc.createElement(AcademyConstants.ELE_EXTN);
		changeOrderInvoiceExtnElm.setAttribute(AcademyConstants.ATTR_EXTN_INVOICE_NO, extnInvoiceNo);
		changeOrderInvoiceIPDocRootElm.appendChild(changeOrderInvoiceExtnElm);
		if (log.isVerboseEnabled()) {
			log.verbose("ProcessBackroomPick: performBackroomPick: changeOrderInvoiceIPDoc: "
					+ XMLUtil.getXMLString(changeOrderInvoiceIPDoc));
		}
		AcademyUtil.invokeAPI(env, "changeOrderInvoice", changeOrderInvoiceIPDoc);
	}

	private void changeShipmentOnCancel(Document multiApiDoc, Element inXMLRoot)
	{
		Element changeShipmentRoot = multiApiDoc.createElement("Shipment");
		// Set attribute value of ShipmentKey
		changeShipmentRoot.setAttribute("ShipmentKey", inXMLRoot.getAttribute("ShipmentKey"));
		Element extn = multiApiDoc.createElement("Extn");
		changeShipmentRoot.appendChild(extn);
		extn.setAttribute("ExtnSopModifyts", AcademyUtil.getDate());
		//STL-679: Storing the Short Reason Code in YFS_SHIPMENT table
		extn.setAttribute(AcademyConstants.ATTR_EXTN_SHORTPICK_REASON_CODE, inXMLRoot.getAttribute(AcademyConstants.ATTR_EXTN_SHORTPICK_REASON_CODE));
		// Invoke method to append the changeShipmet element to MutiApi
		includeInMultiApi(multiApiDoc, changeShipmentRoot, "changeShipment");
	}

	/**
	 * Fetch the NetWeigt of the item
	 * 
	 * @param env
	 * @hashmap of itemQuanty
	 * @return netweight
	 */
	public double getNetWeightOfItems(YFSEnvironment env, HashMap<String, Double> itemQuantityMap) throws Exception{
	   log.verbose("****The input itemList to getNetWeightOfItems****");
	    int size =  itemQuantityMap.size();
	    log.verbose("Size of the itemQuantityMap is "+ size);
		double netWeight = 0;
		Document getItemListOutputDoc = null;
		int itemCount = 1;
		String itemId = "";
		// Iterate through the hashmap entry
		Iterator iterator = itemQuantityMap.keySet().iterator();
		while(iterator.hasNext()){
		   log.verbose("Iterating through items in iemQuantityMap");
			itemId = (String) iterator.next();
			System.out.println("Item id in the map is" + itemId);
			break;
		}
		// creating the input(complex query) for getItemList API
		// Create root node Item
		Document getItemListInputDoc = YFCDocument.createDocument("Item").getDocument();
		Element eleRootElement = getItemListInputDoc.getDocumentElement();
		// setting the values
		eleRootElement.setAttribute("OrganizationCode", AcademyConstants.HUB_CODE);
		eleRootElement.setAttribute("UnitOfMeasure", AcademyConstants.UNIT_OF_MEASURE);
		eleRootElement.setAttribute("ItemID", itemId);
		// Create element ComplexQuery
		Element eleComplexQuery = getItemListInputDoc.createElement("ComplexQuery");
		// setting the value
		eleComplexQuery.setAttribute("Operator", "OR");
		Element eleAnd = getItemListInputDoc.createElement("And");
		eleComplexQuery.appendChild(eleAnd);
		Element eleOr = getItemListInputDoc.createElement("Or");
		eleAnd.appendChild(eleOr);
		// Loop till the record is present in the hashmap
		while (iterator.hasNext())
		{
			// Increment the ItemCount
			itemCount++;
			// Fetch the itemId
			itemId = (String) iterator.next();
			// Create element Exp
			Element eleExp = getItemListInputDoc.createElement("Exp");
			// Setting the attribute
			eleExp.setAttribute("Name", "ItemID");
			eleExp.setAttribute("Value", itemId);
			eleOr.appendChild(eleExp);
		}
		if(itemCount ==1){
			log.verbose("Dint go inside while loop");
		}
		
		// If Item count is greater than 1
		if (itemCount > 1)
		{
			 System.out.println("**ItemCount greater than 1:Item count is" + itemCount);
			// Append the element ComplexQuery to root node
			eleRootElement.appendChild(eleComplexQuery);
		}
		// Print the verbose log
		if (log.isVerboseEnabled())
		{
			log.verbose("getItemlist input :" + XMLUtil.getXMLString(getItemListInputDoc));
		}
		// Invoke the service
		Document docItemListOutput = AcademyUtil.invokeService(env, "AcademySFSGetContainerVolumeDetails", getItemListInputDoc);
		// Fetch the NodeList of Node PrimaryInformation
		NodeList containerVolumePrimaryInformationList = docItemListOutput.getElementsByTagName("PrimaryInformation");
		// Loop through the NodeList record
		for (int i = 0; i < containerVolumePrimaryInformationList.getLength(); i++)
		{
			// Fetch the element
			Element containerVolumePrimaryInformation = (Element) containerVolumePrimaryInformationList.item(i);
			Element containerVolumeItem = (Element) containerVolumePrimaryInformation.getParentNode();
			// Fetch the element ItemId
			itemId = containerVolumeItem.getAttribute("ItemID");
			// Fetch the attribute UnitWeight
			String itemWeight = containerVolumePrimaryInformation.getAttribute("UnitWeight").trim();
			// Check if the va;ue is not empty
			if (!itemWeight.equals("") && itemQuantityMap.containsKey(itemId))
			{
				// Calculate the netWeight
				netWeight += itemQuantityMap.get(itemId) * Double.parseDouble(itemWeight);
			}
		}
		// return thr netWeight value
		log.verbose("net weight from  the method is " + netWeight);
		return netWeight;
	}

	/**
	 * This method will set the container volume details
	 * 
	 * @param env,
	 *            inXML, element container
	 * @return element PrimaryInformation
	 */
	public Element setContainerVolumeDetails(YFSEnvironment env, Element inXMLRoot, Element container) throws Exception
	{
		// Fetch the attribute value of ContainerType
		if (log.isVerboseEnabled()) {
			log.verbose("Inside setContainerVolumeDetails with Container Element" +XMLUtil.getElementXMLString(container));
		}
		String containerItemID = inXMLRoot.getAttribute("ContainerType").trim();
		// Fetch the attribute value of ContainerTypeKey
		String containerItemKey = inXMLRoot.getAttribute("ContainerTypeKey").trim();
		// Creat root element Item
		Document docItemListInput = XMLUtil.createDocument("Item");
		Element docItemListInputRoot = docItemListInput.getDocumentElement();
		// Set the MaximumRecords
		docItemListInputRoot.setAttribute("MaximumRecords", "1");
		// check for empty value
		if (!containerItemKey.equals(""))
		{
			// set attribute ItemKey
			docItemListInputRoot.setAttribute("ItemKey", containerItemKey);
		} else
		{
			// Set the ItemID
			docItemListInputRoot.setAttribute("ItemID", containerItemID);
		}
		// Invoke service
		Document docItemListOutput = AcademyUtil.invokeService(env, "AcademySFSGetContainerVolumeDetails", docItemListInput);
		if (log.isVerboseEnabled()) {
			log.verbose("Output of AcademySFSGetContainerVolumeDetails" + XMLUtil.getXMLString(docItemListOutput));
		}
		// Fetch the element Item
		Element containerVolumeItem = (Element) docItemListOutput.getElementsByTagName("Item").item(0);
		// Fetch the element PrimaryInformation
		Element containerVolumePrimaryInformation = (Element) docItemListOutput.getElementsByTagName("PrimaryInformation").item(0);
		// Set attribute values
		container.setAttribute("CorrugationItemKey", containerVolumeItem.getAttribute("ItemKey"));
		container.setAttribute("ContainerHeight", containerVolumePrimaryInformation.getAttribute("UnitHeight"));
		container.setAttribute("ContainerHeightUOM", containerVolumePrimaryInformation.getAttribute("UnitHeightUOM"));
		container.setAttribute("ContainerLength", containerVolumePrimaryInformation.getAttribute("UnitLength"));
		container.setAttribute("ContainerLengthUOM", containerVolumePrimaryInformation.getAttribute("UnitLengthUOM"));
		container.setAttribute("ContainerWidth", containerVolumePrimaryInformation.getAttribute("UnitWidth"));
		container.setAttribute("ContainerWidthUOM", containerVolumePrimaryInformation.getAttribute("UnitWidthUOM"));
		container.setAttribute("ContainerGrossWeightUOM", containerVolumePrimaryInformation.getAttribute("UnitWeightUOM"));
		container.setAttribute("ContainerNetWeightUOM", containerVolumePrimaryInformation.getAttribute("UnitWeightUOM"));
		container.setAttribute("ActualWeight", containerVolumePrimaryInformation.getAttribute("ActualWeight"));
		container.setAttribute("ActualWeightUOM", containerVolumePrimaryInformation.getAttribute("ActualWeightUOM"));
		
		// Return element PrimaryInformation
		return containerVolumePrimaryInformation;
	}

	/**
	 * This method will invoke generateSCM API
	 * 
	 * @param env,
	 * @return String SCM
	 */
	public String getContainerScm(YFSEnvironment env) throws Exception
	{
		// Create element generateSCM
		Document generateSCMInput = XMLUtil.createDocument("generateSCM");
		Element generateSCMInputRoot = generateSCMInput.getDocumentElement();
		// Set attributes
		generateSCMInputRoot.setAttribute("ContainerType", "Case");
		generateSCMInputRoot.setAttribute("NumScmsRequested", "1");
		// Invoke generateSCM API
		Document generateSCMOutput = api.generateSCM(env, generateSCMInput);
		// Return attribute SCM
		return ((Element) generateSCMOutput.getElementsByTagName("SCM").item(0)).getAttribute("SCM");
	}

	/**
	 * This method will process the container details for NonBulk
	 * 
	 * @param Yantra
	 *            Environment
	 * @param String
	 *            ShipmentContainerKey
	 * @param String
	 *            ShipNode
	 */
	public void processContainerDetailsForNonBulk(YFSEnvironment env, String shipmentContainerKey, String shipNode) throws Exception
	{
		// Invoke the method to fetch the container dtails for Non bulk
		Document getShipmentContainerDetailsOutputDoc = getContainerDetailsForNonBulk(env, shipmentContainerKey);
		Element getShipmentContainerDetailsOutputDocRoot = getShipmentContainerDetailsOutputDoc.getDocumentElement();
		// Set the attribute values
		getShipmentContainerDetailsOutputDocRoot.setAttribute("PrintBatchPackSlip", "Y");
		getShipmentContainerDetailsOutputDocRoot.setAttribute("PrintBatchShippingLabel", "Y");
		getShipmentContainerDetailsOutputDocRoot.setAttribute("IsOnboardedStore", "Y");
		if (log.isVerboseEnabled())
		{
			log.verbose("ProcessBackroomPick: performBackroomPick: getShipmentContainerDetailsOutputDoc: " + XMLUtil.getXMLString(getShipmentContainerDetailsOutputDoc));
		}

		String raiseEventPart1 = "<RaiseEvent TransactionId=\"ADD_TO_CONTAINER\" EventId=\"ON_CONTAINER_PACK_PROCESS_COMPLETE\"><DataType>1</DataType><XMLData><![CDATA[ ";
		String raiseEventPart2 = " ]]></XMLData></RaiseEvent>";
		Document raiseEventInputDoc = YFCDocument.getDocumentFor(raiseEventPart1 + XMLUtil.getXMLString(getShipmentContainerDetailsOutputDoc) + raiseEventPart2).getDocument();
		Element raiseEventInputDocRoot = raiseEventInputDoc.getDocumentElement();
		raiseEventInputDocRoot.setAttribute("ShipNode", shipNode);
		raiseEventInputDocRoot.setAttribute("IsOnboardedStore", "Y");
		if (log.isVerboseEnabled())
		{
			log.verbose("ProcessBackroomPick: performBackroomPick: raiseEventInputDoc: " + XMLUtil.getXMLString(raiseEventInputDoc));
		}
		// Invoke the service to process the NonBulk container
		AcademyUtil.invokeService(env, "AcademySFSProcessNonBulkContainer", raiseEventInputDoc);
	}

	/**
	 * This method will fetch the boolean value
	 * 
	 * @param String
	 *            properties value
	 * @return true or false
	 */

	public boolean getBooleanPropertyValue(String property)
	{
		// Fetch the property value
		String propertyValue = props.getProperty(property);
		// Check if value is not null and is Y
		if (propertyValue != null && propertyValue.equalsIgnoreCase("Y"))
		{
			// return true
			return true;
		}
		// return false
		return false;
	}

	/**
	 * This method will process container details for Bulk
	 * 
	 * @param String
	 *            ShipmentContainerKey
	 * @param Yantra
	 *            Environment
	 * @param String
	 *            ShipNode
	 */
	public void processContainerDetailsForBulk(YFSEnvironment env, String shipmentContainerKey, String shipNode) throws Exception
	{
		// Fetch the property value
		boolean addToManifestFirst = getBooleanPropertyValue("ADD_TO_MANIFEST_FIRST");
		boolean skipAddToManifest = getBooleanPropertyValue("SKIP_ADD_TO_MANIFEST");
		// Check the flag
		if (addToManifestFirst && !skipAddToManifest)
		{
			// Invoke method to add the container into manifest
			addContainerToManifestForBulk(env, shipmentContainerKey, shipNode);
		}
		// Invoke method to fetch container details for Bulk
		Document getShipmentContainerDetailsOutputDoc = getContainerDetailsForBulk(env, shipmentContainerKey);
		// Fetch the root node
		Element getShipmentContainerDetailsOutputDocRoot = getShipmentContainerDetailsOutputDoc.getDocumentElement();
		// Create the element ContainerList
		Document docContainerList = XMLUtil.createDocument("ContainerList");
		Element docContainerListRoot = docContainerList.getDocumentElement();
		// Import the element of output xml of invoked method
		// getContainerDetailsForBulk
		Element eleContainer = (Element) docContainerList.importNode(getShipmentContainerDetailsOutputDocRoot, true);
		// Append the child element
		docContainerListRoot.appendChild(eleContainer);
		// Set the attriobute
		docContainerListRoot.setAttribute("IsOnboardedStore", "Y");
		eleContainer.setAttribute("IsOnboardedStore", "Y");
		if (log.isVerboseEnabled())
		{
			log.verbose("ProcessBackroomPick: performBackroomPick: getShipmentContainerDetailsOutputDoc: " + XMLUtil.getXMLString(getShipmentContainerDetailsOutputDoc));
		}

		String raiseEventPart1 = "<RaiseEvent TransactionId=\"CONTAINERIZE_WAVE\" EventId=\"ON_SUCCESS\"><DataType>1</DataType><XMLData><![CDATA[ ";
		String raiseEventPart2 = " ]]></XMLData></RaiseEvent>";
		Document raiseEventInputDoc = YFCDocument.getDocumentFor(raiseEventPart1 + XMLUtil.getXMLString(docContainerList) + raiseEventPart2).getDocument();
		Element raiseEventInputDocRoot = raiseEventInputDoc.getDocumentElement();
		raiseEventInputDocRoot.setAttribute("ShipNode", shipNode);
		raiseEventInputDocRoot.setAttribute("IsOnboardedStore", "Y");
		if (log.isVerboseEnabled())
		{
			log.verbose("ProcessBackroomPick: performBackroomPick: raiseEventInputDoc: " + XMLUtil.getXMLString(raiseEventInputDoc));
		}
		// Invoke the service
		AcademyUtil.invokeService(env, "AcademySFSProcessBulkContainer", raiseEventInputDoc);
		// Check the flag
		if (!addToManifestFirst && !skipAddToManifest)
		{
			// invoke the method to add container to manifest for bulk
			addContainerToManifestForBulk(env, shipmentContainerKey, shipNode);
		}
	}

	/**
	 * This method will add Container To ManifestForBulk
	 * 
	 * @param Yantra
	 *            Environment
	 * @param String
	 *            ShipmentContainerKey
	 * @param String
	 *            ShipNode
	 */
	public void addContainerToManifestForBulk(YFSEnvironment env, String shipmentContainerKey, String shipNode) throws Exception
	{
		// Invoke method getContainerDetailsForNonBulk
		Document getShipmentContainerDetailsOutputDoc = getContainerDetailsForNonBulk(env, shipmentContainerKey);
		// Fetch the root element
		Element getShipmentContainerDetailsOutputDocRoot = getShipmentContainerDetailsOutputDoc.getDocumentElement();
		// set attribute IsOnboardedStore=Y
		getShipmentContainerDetailsOutputDocRoot.setAttribute("IsOnboardedStore", "Y");

		if (log.isVerboseEnabled())
		{
			log.verbose("ProcessBackroomPick: performBackroomPick: getShipmentContainerDetailsOutputDoc: " + XMLUtil.getXMLString(getShipmentContainerDetailsOutputDoc));
		}

		String raiseEventPart1 = "<RaiseEvent TransactionId=\"VERIFY_PACK\" EventId=\"VERIFICATION_DONE\"><DataType>1</DataType><XMLData><![CDATA[ ";
		String raiseEventPart2 = " ]]></XMLData></RaiseEvent>";
		Document raiseEventInputDoc = YFCDocument.getDocumentFor(raiseEventPart1 + XMLUtil.getXMLString(getShipmentContainerDetailsOutputDoc) + raiseEventPart2).getDocument();
		Element raiseEventInputDocRoot = raiseEventInputDoc.getDocumentElement();
		// Set attribute
		raiseEventInputDocRoot.setAttribute("ShipNode", shipNode);
		raiseEventInputDocRoot.setAttribute("IsOnboardedStore", "Y");

		if (log.isVerboseEnabled())
		{
			log.verbose("ProcessBackroomPick: performBackroomPick: raiseEventInputDoc: " + XMLUtil.getXMLString(raiseEventInputDoc));
		}
		// Invoke service
		AcademyUtil.invokeService(env, "AcademySFSProcessBulkContainerAddToManifest", raiseEventInputDoc);
	}

	/**
	 * This method will invoke getShipmentContainerDetails for Non Bulk
	 * container
	 * 
	 * @param Yantra
	 *            Environment
	 * @param Document
	 *            InXML
	 * @return output xml of getShipmentContainerDetails API
	 */
	public Document getContainerDetailsForNonBulk(YFSEnvironment env, Document inXML) throws Exception
	{
		// Invoke method to fetch container details for the specified
		// ShipmentContainerKey
		return getContainerDetailsForNonBulk(env, inXML.getDocumentElement().getAttribute("ShipmentContainerKey"));
	}

	/**
	 * This method will invoke getShipmentContainerDetails for NonBulk container
	 * 
	 * @param Yantra
	 *            Environment
	 * @param ShipmentContainerKey
	 * @return output xml of getShipmentContainerDetails API
	 */

	public Document getContainerDetailsForNonBulk(YFSEnvironment env, String shipmentContainerKey) throws Exception
	{
		String strIsWebStoreFlow = (String) env.getTxnObject(AcademyConstants.A_IS_WEB_STORE_FLOW);
		// Set the template for getShipmentContainerDetails API
		if(YFCCommon.equalsIgnoreCase("Y", strIsWebStoreFlow))
		{
			//removing backslash for Sterling 9.5 version.
			env.setApiTemplate("getShipmentContainerDetails", "global/template/api/AcadOnContainerPackProcessComplete.xml"); 
		}
		else
		{
			env.setApiTemplate("getShipmentContainerDetails", "/global/template/event/ADD_TO_CONTAINER.ON_CONTAINER_PACK_PROCESS_COMPLETE.xml"); //SOM Conversion-> Changed the xml file path
		}
		// Create the root element Container
		Document getShipmentContainerDetailsInputDoc = XMLUtil.createDocument("Container");
		Element getShipmentContainerDetailsInputDocRoot = getShipmentContainerDetailsInputDoc.getDocumentElement();
		// Set the attribute ShipmentContainerKey
		getShipmentContainerDetailsInputDocRoot.setAttribute("ShipmentContainerKey", shipmentContainerKey);
		if (log.isVerboseEnabled())
		{
			log.verbose("ProcessBackroomPick: performBackroomPick: getShipmentContainerDetailsInputDoc: " + XMLUtil.getXMLString(getShipmentContainerDetailsInputDoc));
		}
		// Invoke getShipmentContainerDetails API and return the output document
		return api.getShipmentContainerDetails(env, getShipmentContainerDetailsInputDoc);
	}

	/**
	 * This method will invoke getShipmentContainerDetails for Bulk container
	 * 
	 * @param Yantra
	 *            Environment
	 * @param Document
	 *            InXML
	 * @return output xml of getShipmentContainerDetails API
	 */

	public Document getContainerDetailsForBulk(YFSEnvironment env, Document inXML) throws Exception
	{
		// Invoke method to fetch container details for the specified
		// ShipmentContainerKey
		return getContainerDetailsForBulk(env, inXML.getDocumentElement().getAttribute("ShipmentContainerKey"));
	}

	/**
	 * This method will invoke getShipmentContainerDetails for Bulk container
	 * 
	 * @param Yantra
	 *            Environment
	 * @param ShipmentContainerKey
	 * @return output xml of getShipmentContainerDetails API
	 */
	public Document getContainerDetailsForBulk(YFSEnvironment env, String shipmentContainerKey) throws Exception
	{
		// Set the template for getShipmentContainerDetails API
		YFCDocument templateTemp = YFCDocument.parse(this.getClass().getResourceAsStream("/global/template/event/CONTAINERIZE_WAVE.ON_SUCCESS.xml"));//SOM Conversion-> Changed the xml file path
		//YFCDocument templateTemp = YFCDocument.parse(this.getClass().getResourceAsStream("/global/template/event/CONTAINERIZE_WAVE.ON_SUCCESS.xml"));//SOM Conversion-> Changed the xml file path
		Element templateTempRoot = templateTemp.getDocument().getDocumentElement();
		Document getShipmentContainerDetailsTemplate = XMLUtil.getDocumentForElement((Element) templateTempRoot.getElementsByTagName("Container").item(0));
		env.setApiTemplate("getShipmentContainerDetails", getShipmentContainerDetailsTemplate);
		// Create element Container
		Document getShipmentContainerDetailsInputDoc = XMLUtil.createDocument("Container");
		Element getShipmentContainerDetailsInputDocRoot = getShipmentContainerDetailsInputDoc.getDocumentElement();
		// Set attribute ShipmentContainerKey
		getShipmentContainerDetailsInputDocRoot.setAttribute("ShipmentContainerKey", shipmentContainerKey);
		if (log.isVerboseEnabled())
		{
			log.verbose("ProcessBackroomPick: performBackroomPick: getShipmentContainerDetailsInputDoc: " + XMLUtil.getXMLString(getShipmentContainerDetailsInputDoc));
		}
		// Invoke getShipmentContainerDetails API and return the output
		return api.getShipmentContainerDetails(env, getShipmentContainerDetailsInputDoc);
	}

	/**
	 * This method will set APIName, input for multiAPI
	 * 
	 * @param input
	 *            Document of multiApi
	 * @param element
	 *            to append under Input element
	 * @param ApiName
	 */
	private static void includeInMultiApi(Document multiApiDoc, Element apiInput, String apiName)
	{
		// Fetch the root element
		Element multiApiDocRoot = multiApiDoc.getDocumentElement();
		// Create element API
		Element api = multiApiDoc.createElement("API");
		multiApiDocRoot.appendChild(api);
		// set attribute Name
		api.setAttribute("Name", apiName);
		// Create element Input
		Element input = multiApiDoc.createElement("Input");
		api.appendChild(input);
		// START: SHIN-10
		Node importEle = multiApiDoc.importNode(apiInput, true);
		input.appendChild(importEle);
		if (log.isVerboseEnabled()) {
			log.verbose("MultiApi Doc prepared ::"
				+ XMLUtil.getXMLString(multiApiDoc));
		}
		// END: SHIN-10
	}
	
	// Ship From DC Changes :: Start
	/**
	 * This method will set the container volume details to the item details in
	 * case when the conatiner type = "VendorPackage"
	 * 
	 * @param env,
	 *            inXML, element container
	 * @return element PrimaryInformation
	 */
	public void setContainerVolumeDetailsVndrPkg(YFSEnvironment env,
			Element inXMLRoot, Element container) throws Exception {
		
		// Start - Changes made for PERF-395 to handle null pointer exception in a one of scenario.
		if (log.isVerboseEnabled()) {
			log.verbose("setContainerVolumeDetailsVndrPkg: inXMLRoot Elem: " + XMLUtil.getElementXMLString(inXMLRoot));		
		}
		
		// Fetch the element Item
		NodeList itemList = inXMLRoot.getElementsByTagName("Item");
		if (itemList != null && itemList.getLength() > 0) {
			Element containerVolumeItem = (Element) itemList.item(0);
			//	Set attribute values
			container.setAttribute("CorrugationItemKey", containerVolumeItem.getAttribute("ItemKey"));
			
			// Fetch the element PrimaryInformation		
			NodeList primaryInfoList = containerVolumeItem.getElementsByTagName("PrimaryInformation");
			if (primaryInfoList != null && primaryInfoList.getLength() > 0) {
				Element containerVolumePrimaryInformation = (Element) primaryInfoList.item(0);
				
				String unitHeight = containerVolumePrimaryInformation.getAttribute("UnitHeight");
				if (!YFCObject.isVoid(unitHeight)) {
					container.setAttribute("ContainerHeight",unitHeight);
				}
				
				String unitHeightUOM = containerVolumePrimaryInformation.getAttribute("UnitHeightUOM");
				if (!YFCObject.isVoid(unitHeightUOM)) {
					container.setAttribute("ContainerHeightUOM",unitHeightUOM);
				}
				
				String unitLength = containerVolumePrimaryInformation.getAttribute("UnitLength");
				if (!YFCObject.isVoid(unitLength)) {
					container.setAttribute("ContainerLength",unitLength);
				}
				
				String unitLengthUOM = containerVolumePrimaryInformation.getAttribute("UnitLengthUOM");
				if (!YFCObject.isVoid(unitLengthUOM)) {
					container.setAttribute("ContainerLengthUOM",unitLengthUOM);
				}
				
				String unitWidth = containerVolumePrimaryInformation.getAttribute("UnitWidth");
				if (!YFCObject.isVoid(unitWidth)) {
					container.setAttribute("ContainerWidth",unitWidth);
				}

				String unitWidthUOM = containerVolumePrimaryInformation.getAttribute("UnitWidthUOM");
				if (!YFCObject.isVoid(unitWidthUOM)) {
					container.setAttribute("ContainerWidthUOM",unitWidthUOM);
				}
				
				String unitWeight = containerVolumePrimaryInformation.getAttribute("UnitWeight");
				String actualWeight = containerVolumePrimaryInformation.getAttribute("UnitWeight");
				String strIWebStore = (String) env.getTxnObject(AcademyConstants.A_IS_WEB_STORE_FLOW);
				if (!YFCObject.isVoid(unitWeight)) {
					container.setAttribute("ContainerGrossWeight",unitWeight);
					container.setAttribute("ContainerNetWeight",unitWeight);
					if (YFCCommon.equalsIgnoreCase(strIWebStore, AcademyConstants.STR_YES)){
						container.setAttribute("ActualWeight",actualWeight);
					}
				}
				
				String unitWeightUOM = containerVolumePrimaryInformation.getAttribute("UnitWeightUOM");
				if (!YFCObject.isVoid(unitWeightUOM)) {
					container.setAttribute("ContainerGrossWeightUOM",unitWeightUOM);
					container.setAttribute("ContainerNetWeightUOM",unitWeightUOM);
					container.setAttribute("ActualWeightUOM",unitWeightUOM);
					
				}				
			} else {												
				// Throw custom exception as primary information of an item element cannot be missing.
				YFSException e = new YFSException();
				e.setErrorCode("EXTN_ACAD004");
				e.setErrorDescription("System Error. Unable to fetch primary information for the item.");
				throw e;					
			}
		}
		if (log.isVerboseEnabled()) {
			log.verbose("setContainerVolumeDetailsVndrPkg: container Elem: " + XMLUtil.getElementXMLString(container));
		}
        // End - Changes made for PERF-395 to handle null pointer exception in a one of scenario.
	}
	
	// Ship From DC Changes :: End
	
	// START: SHIN-10
	
	/*
	 * The invokeMultiApiDemandSummary method has a logic for preparing the
	 * input for multi api demand summary and with the input invoking the
	 * multiApi and also has the logic for preparing the input for
	 * getInventoryMismatch API and invoking the getInventory Mismatch API
	 */

	private Document invokeMultiApiDemandSummary(YFSEnvironment env,
			Document inXML, String strShipNode) throws Exception {

		// Preparing the input for multiApi getDemandSummary

		Document multiApiDoc = prepareInputForMultiApiGetDemandSummary(inXML,
				strShipNode);

		// Invoking multiApi for getDemandSummary

		Document getDemandSummaryOutDoc = AcademyUtil.invokeAPI(env,
				AcademyConstants.API_MULTI_API, multiApiDoc);
		if (log.isVerboseEnabled()) {
			log.verbose("multiApiOutputDoc Output::"
				+ XMLUtil.getXMLString(getDemandSummaryOutDoc));
		}

		return getDemandSummaryOutDoc;

	}

	// END: SHIN-10
	
	// START: SHIN-10
	
	/*
	 * This invokeGetInventoryMismatch()will prepare the input and invoke the
	 * getInventoryMismatch API.
	 */

	private Document invokeGetInventoryMismatch(YFSEnvironment env,
			String strShipNode,  Document getDemandSummaryOutDoc)
	throws Exception {

		// preparing the input for getInventoryMismatch
		Document getInventoryMismatchInDoc = prepareInputForGetInventoryMismatch(
				strShipNode, getDemandSummaryOutDoc);

		Document getInventoryMismatchOutDoc = AcademyUtil.invokeAPI(env,
				AcademyConstants.GET_INVENTORY_MISMATCH,
				getInventoryMismatchInDoc);
		if (log.isVerboseEnabled()) {
			log.verbose("getInvMismatch Output::"
				+ XMLUtil.getXMLString(getInventoryMismatchOutDoc));
		}

		return getInventoryMismatchOutDoc;

	}

	// END: SHIN-10
	
	// START: SHIN-10
	
	/*
	 * Prepared the input to invoke getDemandSummary API for each item in InDoc
	 * XML from EXETER
	 */

	private Document prepareInputForMultiApiGetDemandSummary(Document inXML,
			String strShipNode) throws Exception {

		String strItemID = "";
		double shortageQuantity = 0;
		// MultiApi document is created

		Document multiApiDoc = XMLUtil.createDocument("MultiApi");

		/*
		 * NodeList is created to fetch the item id from total number of
		 * shipment lines
		 */

		NodeList nlInDocShipmentLine = inXML
				.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

		for (int i = 0; i < nlInDocShipmentLine.getLength(); i++) {

			
			shortageQuantity = 0;
			
			/*
			 * Created element eleInDocShipmentLine to fetch the item id for
			 * each line
			 */

			Element eleInDocShipmentLine = (Element) nlInDocShipmentLine
					.item(i);

			if (!("").equals(eleInDocShipmentLine.getAttribute("ShortageQty").trim())) {

				shortageQuantity = Double.parseDouble(eleInDocShipmentLine
						.getAttribute("ShortageQty"));
			}
			
			// Only if the shortage qty is greater than 0 then it will go inside
			// the loop
			if (shortageQuantity > 0) {

				log
						.verbose("************Shortage qty is greater than 0**************");

				// Getting the item id for corresponding shipment line
				strItemID = eleInDocShipmentLine
						.getAttribute(AcademyConstants.ATTR_ITEM_ID);

				log.verbose("ItemID :::: " + strItemID);

				// if the shortage qty is not null then fetch the shortage qty

				

				log.verbose("Shortage qty :::: " + shortageQuantity);

				// Created the input document for getDemandSummary API

				Document getDemandSummaryInDoc = XMLUtil
						.createDocument(AcademyConstants.ELE_DEMAND_SUMMARY);
				Element elegetDemandSummaryInDoc = getDemandSummaryInDoc
						.getDocumentElement();

				/*
				 * setting the item id for the corresponding item id in each
				 * shipment line
				 */

				elegetDemandSummaryInDoc.setAttribute(
						AcademyConstants.ATTR_ITEM_ID, strItemID);

				elegetDemandSummaryInDoc.setAttribute(
						AcademyConstants.ORGANIZATION_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
				elegetDemandSummaryInDoc.setAttribute(
						AcademyConstants.ATTR_SHIP_NODE, strShipNode);
				elegetDemandSummaryInDoc.setAttribute(
						AcademyConstants.ATTR_PROD_CLASS,
						AcademyConstants.PRODUCT_CLASS);
				elegetDemandSummaryInDoc.setAttribute(
						AcademyConstants.ATTR_UOM,
						AcademyConstants.UNIT_OF_MEASURE);

				includeInMultiApi(multiApiDoc, getDemandSummaryInDoc
						.getDocumentElement(),
						AcademyConstants.GET_DEMAND_SUMMARY);

			}
			if (log.isVerboseEnabled()) {
				log.verbose("getDemandSummaryInDoc for Short Pick::"
						+ XMLUtil.getXMLString(multiApiDoc));
			}

		}

		return multiApiDoc;
	}

	// END: SHIN-10
	
	// START: SHIN-10

	private Document prepareInputForGetInventoryMismatch(String strShipNode,
			Document getDemandSummaryOutDoc) throws Exception {

		String strItemID = "";
		String strProductClass = "";
		String strUOM = "";
		String strDemandAllocatedQuantity = "";
		String strDemandExtnAllocatedQuantity = "";
		String strDemandType = "";
		Document getInventoryMismatchInDoc = XMLUtil
		.createDocument(AcademyConstants.ELE_INV_SNAPSHOT);

		// Setting the attribute ApplyDifferences flag as Y

		getInventoryMismatchInDoc.getDocumentElement().setAttribute(
				AcademyConstants.ATTR_APPLY_DIFF, "Y");

		// Creating the element ShipNode

		Element eleShipNode = getInventoryMismatchInDoc
		.createElement("ShipNode");
		eleShipNode.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strShipNode);
		getInventoryMismatchInDoc.getDocumentElement().appendChild(eleShipNode);

		// Creating a nodelist from getDemandSummaryOutDoc

		NodeList nlDemandSummary = getDemandSummaryOutDoc
		.getElementsByTagName(AcademyConstants.ELE_DEMAND_SUMMARY);

		for (int i = 0; i < nlDemandSummary.getLength(); i++) {

			/*
			 * Created element eleInDocShipmentLine to fetch the item id for
			 * each line and fetching the attributes
			 */
			Element eleDemandSummary = (Element) nlDemandSummary.item(i);
			strItemID = eleDemandSummary.getAttribute(AcademyConstants.ITEM_ID);
			strProductClass = eleDemandSummary
			.getAttribute(AcademyConstants.ATTR_PROD_CLASS);
			strUOM = eleDemandSummary.getAttribute(AcademyConstants.ATTR_UOM);

			Element eleItem = getInventoryMismatchInDoc.createElement("Item");
			eleItem.setAttribute(AcademyConstants.ITEM_ID, strItemID);
			eleItem.setAttribute(AcademyConstants.INVENTORY_ORGANIZATION_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			eleItem.setAttribute(AcademyConstants.ATTR_PROD_CLASS,
					strProductClass);
			eleItem.setAttribute(AcademyConstants.ATTR_UOM, strUOM);
			eleShipNode.appendChild(eleItem);

			// creating a nodelist
			NodeList nlDemand = eleDemandSummary
			.getElementsByTagName(AcademyConstants.ELE_DEMAND);

			for (int j = 0; j < nlDemand.getLength(); j++) {

				Element eleDemand = (Element) nlDemand.item(j);
				// Getting the attribute DemandType from getDemandSummary Output
				// xml
				strDemandType = eleDemand
				.getAttribute(AcademyConstants.ATTR_DEMAND_TYPE);

				// Creating the element SupplyDetails

				Element eleSupplyDetails = getInventoryMismatchInDoc
				.createElement(AcademyConstants.SUPPLY_DETAILS);
				// appending the child

				eleItem.appendChild(eleSupplyDetails);

				/*
				 * if the demand type is allocated then get the quantity from
				 * demand element and set it to the supply details element for
				 * the Supply type="ONHAND" if the demand type is allocated.ex
				 * then get the quantity from demand element and set it to the
				 * supply details element for the Supply type="ONHAND.ex"
				 */

				if ("ALLOCATED".equals(strDemandType)) {
					strDemandAllocatedQuantity = eleDemand
					.getAttribute(AcademyConstants.ATTR_QUANTITY);
					eleSupplyDetails.setAttribute(
							AcademyConstants.ATTR_QUANTITY,
							strDemandAllocatedQuantity);
					
					eleSupplyDetails.setAttribute(
							AcademyConstants.ATTR_SUPPLY_TYPE, "ONHAND");
					
				} else if ("ALLOCATED.ex".equals(strDemandType)) {
					strDemandExtnAllocatedQuantity = eleDemand
					.getAttribute(AcademyConstants.ATTR_QUANTITY);
					eleSupplyDetails.setAttribute(
							AcademyConstants.ATTR_QUANTITY,
							strDemandExtnAllocatedQuantity);
					eleSupplyDetails.setAttribute(
							AcademyConstants.ATTR_SUPPLY_TYPE, "ONHAND.ex");
					
				}

			}
		}
		if (log.isVerboseEnabled()) {
			log.verbose("getInventoryMismatchInDoc for Short Pick::"
				+ XMLUtil.getXMLString(getInventoryMismatchInDoc));
		}

		return getInventoryMismatchInDoc;

	}
	// END:SHIN-10
	
	//EFP-17 Start
	private Document getShipmentDetails (YFSEnvironment env, String strShipmentKey){	
		Document docShipmentListOut = null;
		try{		
			
			if (!YFCObject.isVoid(strShipmentKey) 
					&& !YFCObject.isNull(strShipmentKey)) {
				Document getShipmentListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
				getShipmentListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);				
				env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST,AcademyConstants.STR_GET_SHIPMENT_LIST_PACK);
				if (log.isVerboseEnabled()) {
					log.verbose("getShipmentList API input - "+ XMLUtil.getXMLString(getShipmentListInDoc));
				}
				docShipmentListOut = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_SHIPMENT_LIST,getShipmentListInDoc);
				env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
				if (log.isVerboseEnabled()) {
					log.verbose("getShipmentList API output - "+ XMLUtil.getXMLString(docShipmentListOut));
				}
								
			}
		}catch(Exception e){
			log.verbose("Exception inside YCSshipCartonUserExitImpl.shipCarton()");
			log.verbose("Exception while APO-FPO Identification");
			e.printStackTrace();
		}
		return docShipmentListOut;		
	}
	//
	
	//Multbox SKU
	
	private void setContainerVolumeDetailsForMultibox(YFSEnvironment env, Element inXMLRoot,Element container,Element multiboxLookupElement) throws Exception{
		
//		log.verbose("Inside setContainerVolumeDetailsForMultibox with Container Element");
		
		String itemID = 	XMLUtil.getAttributeFromXPath(inXMLRoot.getOwnerDocument(),"//Shipment/ShipmentLines/ShipmentLine/OrderLine/Item/@ItemID");
		String itemKey = 	XMLUtil.getAttributeFromXPath(inXMLRoot.getOwnerDocument(),"//Shipment/ShipmentLines/ShipmentLine/OrderLine/Item/@ItemKey");
//		
//		Document inputDoc             = XMLUtil.createDocument("AcadMultiboxLookup");
//        inputDoc.getDocumentElement().setAttribute("ItemID", itemID);
//        Element multiBoxLookupOutput =  AcademyUtil.invokeService(env, "getAcadMultiboxLookup", inputDoc).getDocumentElement();
//		log.verbose("Output of getAcadMultiboxLookup" + XMLUtil.getXMLString(multiBoxLookupOutput.getOwnerDocument()));
		
		// Fetch the element Item
	
		String strHeight = multiboxLookupElement.getAttribute("Height");
        String strLength = multiboxLookupElement.getAttribute("Width");
        String strWidth  = multiboxLookupElement.getAttribute("Length");
        String strWeight = multiboxLookupElement.getAttribute("Weight");
        
        container.setAttribute("CorrugationItemKey",itemKey);
		container.setAttribute("ContainerHeight", strHeight);
		container.setAttribute("ContainerLength", strLength);
		container.setAttribute("ContainerWidth", strWidth);
		container.setAttribute("ContainerGrossWeight",strWeight);
		container.setAttribute("ContainerNetWeight",strWeight);	

	}
	
	private double roundDecimal(double dValue, int iPlaces) {
	    double dScale = Math.pow(10, iPlaces);
	    return Math.round(dValue * dScale) / dScale;
	}
	
}