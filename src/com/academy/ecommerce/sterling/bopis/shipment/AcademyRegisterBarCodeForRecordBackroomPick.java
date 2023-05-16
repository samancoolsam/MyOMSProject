package com.academy.ecommerce.sterling.bopis.shipment;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.util.YFCUtils;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;

/** On serialNo Barcode scan of an item in the BackroomPickProcess of BOPIS FireArm,
 * serialNo will be stored at
 * /Shipment/ShipmentLines/ShipmentLine/ShipmentTagSerials/ShipmentTagSerial level.
 * (YFS_SHIPMENT_TAG_SERIAL table)
 * OMNI-91418
 */
public class AcademyRegisterBarCodeForRecordBackroomPick {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyRegisterBarCodeForRecordBackroomPick.class);

	/**Sample input:
	 * <BarCode BarCodeData="020697637" BarCodeType="Item"
    	DisplayLocalizedFieldInLocale="en_US_CST" IgnoreOrdering="Y">
    	<ContextualInfo EnterpriseCode="Academy_Direct" OrganizationCode="033"/>
    	<ShipmentContextualInfo SellerOrganizationCode="Academy_Direct"
        	SerialNo="0206976377" ShipNode="033" ShipmentKey="202211140720495722296300"/>
	</BarCode>
	
	Sample Output:
	 <BarCode BarCodeData="020697637" BarCodeType="Item">
	    <Translations BarCodeTranslationSource="ItemID" TotalNumberOfRecords="1">
	        <Translation>
	            <ContextualInfo EnterpriseCode="Academy_Direct"
	                InventoryOrganizationCode="Academy_Direct" OrganizationCode="DEFAULT"/>
	            <ItemContextualInfo InventoryUOM="EACH" ItemID="020697637"
	                KitCode="" Quantity="1.0"/>
	            <ShipmentContextualInfo
	                SellerOrganizationCode="Academy_Direct" ShipNode="033"
	                ShipmentKey="202211140720495722296300"
	                ShipmentLineKey="202211140720495722296299" ShipmentNo="100564229"/>
	        </Translation>
	    </Translations>
	    <Shipment EnterpriseCode="Academy_Direct"
	        SellerOrganizationCode="Academy_Direct" ShipNode="033"
	        ShipmentKey="202211140720495722296300" ShipmentNo="100564229">
	        <ShipmentLine BackroomPickedQuantity="1.0" ItemID="020697637"
	            KitCode="" OrderHeaderKey="202211140433135722157323"
	            OrderLineKey="202211140433135722157324"
	            OrderNo="BPSFA111403" OriginalQuantity="4.00"
	            Quantity="4.00" ShipmentKey="202211140720495722296300"
	            ShipmentLineKey="202211140720495722296299"
	            ShipmentLineNo="1" ShipmentSubLineNo="0" ShortageQty="0.00" UnitOfMeasure="EACH">
	            <ShipmentTagSerials>
	                <ShipmentTagSerial Quantity="1.00" SerialNo="020697637" ShipmentLineKey="202211140720495722296299"/>
	                <ShipmentTagSerial Quantity="1.00" SerialNo="0206976377" ShipmentLineKey="202211140720495722296299"/>
	                <ShipmentTagSerial Quantity="1.00" SerialNo="123456789" ShipmentLineKey="202211140720495722296299"/>
	            </ShipmentTagSerials>
	            <OrderLine IsBundleParent="N" ItemGroupCode="PROD">
	                <ItemDetails ItemGroupCode="PROD" ItemID="020697637" UnitOfMeasure="EACH">
	                    <PrimaryInformation
	                        ExtendedDisplayDescription="Traditions Buckstalker .50CAL Black/Blued Muzzleloader (020697637)"
	                        ImageID="10167314" ImageLocation="https://s7d2.scene7.com/is/image/academy/"/>
	                    <ClassificationCodes Model="201038374"/>
	                </ItemDetails>
	            </OrderLine>
	        </ShipmentLine>
	    </Shipment>
	</BarCode>
	 */
	public Document translateBarCodeData(YFSEnvironment env, Document inDoc) throws Exception {
		Document docTranslateBarCodeDataOutput = null;
		try {
			log.beginTimer("AcademyRegisterBarCodeForRecordBackroomPick..translateBarCodeData");
			log.verbose("Entering AcademyRegisterBarCodeForRecordBackroomPick.translateBarCodeData() :: " + XMLUtil.getXMLString(inDoc));
			
			validateInputDocument(inDoc);
			Element inputElem = inDoc.getDocumentElement();
			Element shipmentElem = (Element) inDoc.getElementsByTagName(AcademyConstants.SHIPMENT_CONTEXTUAL_INFO).item(0);
			
			String strSerialNo = shipmentElem.getAttribute(AcademyConstants.ATTR_SERIAL_NO).trim();
			
			Document docOutGetShipmentList = invokeGetShipmentList(env, shipmentElem);
			Document docError = throwErrorIfDuplicateSerial(docOutGetShipmentList, strSerialNo);
			if (!YFCObject.isVoid(docError)) {
				return docError;
			}
			Document docOutTranslateBarcode = invokeTranslateBarcode(env, inDoc);
			//OMNI-93022 - Start
			if (!YFCObject.isVoid(docOutTranslateBarcode) && 
					docOutTranslateBarcode.getDocumentElement().getNodeName().equals(AcademyConstants.ELE_ERROR)) {
				return docOutTranslateBarcode;
			}
			//OMNI-93022 - End
			validateTranslateBarcodeOutput(docOutTranslateBarcode, inputElem);
			Element eleMatchingShipLine = checkForMatchingShipLine(docOutGetShipmentList, docOutTranslateBarcode);
			String shipmentKey = XMLUtil.getAttributeFromXPath(docOutGetShipmentList, "Shipments/Shipment/@ShipmentKey");
			if (!YFCObject.isVoid(eleMatchingShipLine)) {
				log.verbose("Found a matching shipment line > go for changeShipment and update Output");
				Document changeShipmentOutDoc = callChangeShipment(env, shipmentKey, 
						eleMatchingShipLine, strSerialNo);			
				docTranslateBarCodeDataOutput = prepareOutputDocument(docOutTranslateBarcode, docOutGetShipmentList, 
						eleMatchingShipLine, changeShipmentOutDoc);
				return docTranslateBarCodeDataOutput;
			}			
		} finally {
			log.endTimer("AcademyRegisterBarCodeForRecordBackroomPick.translateBarCodeData");
		}
		log.endTimer("AcademyRegisterBarCodeForRecordBackroomPick.translateBarCodeData");
		return docTranslateBarCodeDataOutput;
	}
	
	/** Checks if any duplicate serial number is scanned. Throws exception if yes.
	 * Sample input: getShipmentList output: (Output of invokeGetShipmentList method)
	 * 
	 * Sample Output: 
	 * Null if not duplicate serial number.
	 * Below Error message if duplicate serial number scanned:
	 * <Error ErrorMessage="Duplicate serial number scanned"/>
	 */
	private Document throwErrorIfDuplicateSerial(Document docOutGetShipmentList, String strSerialNo) {
		Document docError = null;
		log.verbose("Inside throwErrorIfDuplicateSerial :: \n");
		try {
			log.verbose("docOutGetShipmentList:: " + XMLUtil.getXMLString(docOutGetShipmentList));
			NodeList nlShipmentSerial = XPathUtil.getNodeList(docOutGetShipmentList,
					"/Shipments/Shipment/ShipmentLines/ShipmentLine/ShipmentTagSerials/ShipmentTagSerial");
			for (int j = 0; j < nlShipmentSerial.getLength(); j++) {
				Element eleShipSerial = (Element) nlShipmentSerial.item(j);
				if (eleShipSerial.getAttribute(AcademyConstants.ATTR_SERIAL_NO).equalsIgnoreCase(strSerialNo)) {
					docError = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
					Element eleError = docError.getDocumentElement();
					eleError.setAttribute(AcademyConstants.ATTR_ERROR_MESSAGE,
							AcademyConstants.DUPLICATE_SERIAL_NO_SCANNED);
				}
			}
		} catch (Exception t) {
			t.printStackTrace();
			log.verbose("Exception in throwErrorIfDuplicateSerial");
			log.info("Error Trace:: " + t.toString());
		}
		return docError;
	}
	
	/** Validates the translateBarCode api output by checking the TotalNumberOfRecords.
	 * @param docOutTranslateBarcode
	 * @param inputElem
	 */
	public static void validateTranslateBarcodeOutput(Document docOutTranslateBarcode, Element inputElem) {
		try {
			log.verbose("Inside validateTranslateBarcodeOutput :: \n" );
			String totalNoRecords = XMLUtil.getAttributeFromXPath(docOutTranslateBarcode, "BarCode/Translations/@TotalNumberOfRecords");
			Double totalRecords = Double.parseDouble(totalNoRecords);
			if (totalRecords > 1.0D) {      
				YFCException ex = new YFCException("YCD00064");      
				ex.setAttribute(AcademyConstants.ATTR_BAR_CODE_DATA, 
						inputElem.getAttribute(AcademyConstants.ATTR_BAR_CODE_DATA));      
				ex.setAttribute(AcademyConstants.ATTR_BAR_CODE_TYPE, 
						inputElem.getAttribute(AcademyConstants.ATTR_BAR_CODE_TYPE));      
				throw ex;    
			}
			log.verbose("TotalNumberOfRecords" + totalRecords);
		} catch (Exception v) {
			v.printStackTrace(); 
			log.verbose("Exception in validateTranslateBarcodeOutput");
			log.info("Error Trace::" + v.toString());
		}		
	}
	
	/** Invokes getShipmentList api based on the below part of input:
	 * <ShipmentContextualInfo SellerOrganizationCode="Academy_Direct"
        ShipNode="033" ShipmentKey="202209131306275680724794" SerialNo="123456789"/>
	 * Sample input:
	 * <Shipment SellerOrganizationCode="Academy_Direct" SerialNo="0206976377"
    	ShipNode="033" ShipmentKey="202211140720495722296300"/>
    	
    	Sample Output:
    	<Shipments>
		    <Shipment EnterpriseCode="Academy_Direct"
		        SellerOrganizationCode="Academy_Direct" ShipNode="033"
		        ShipmentKey="202211140720495722296300" ShipmentNo="100564229">
		        <ShipmentLines>
		            <ShipmentLine BackroomPickedQuantity="1.00"
		                ItemID="020697637" KitCode=""
		                OrderHeaderKey="202211140433135722157323"
		                OrderLineKey="202211140433135722157324"
		                OrderNo="BPSFA111403" OriginalQuantity="4.00"
		                Quantity="4.00" ShipmentKey="202211140720495722296300"
		                ShipmentLineKey="202211140720495722296299"
		                ShipmentLineNo="1" ShipmentSubLineNo="0"
		                ShortageQty="0.00" UnitOfMeasure="EACH">
		                <ShipmentTagSerials>
		                    <ShipmentTagSerial Quantity="1.00"
		                        SerialNo="020697637" ShipmentLineKey="202211140720495722296299"/>	                    
		                    <ShipmentTagSerial Quantity="1.00"
		                        SerialNo="0206976377" ShipmentLineKey="202211140720495722296299"/>
		                </ShipmentTagSerials>
		                <OrderLine IsBundleParent="N" ItemGroupCode="PROD">
		                    <ItemDetails ItemGroupCode="PROD" ItemID="020697637" UnitOfMeasure="EACH">
		                        <PrimaryInformation
		                            ExtendedDisplayDescription="Traditions Buckstalker .50CAL Black/Blued Muzzleloader (020697637)"
		                            ImageID="10167314" ImageLocation="https://s7d2.scene7.com/is/image/academy/"/>
		                        <ClassificationCodes Model="201038374"/>
		                    </ItemDetails>
		                </OrderLine>
		            </ShipmentLine>
		        </ShipmentLines>
		    </Shipment>
		</Shipments>
	 */
	private Document invokeGetShipmentList(YFSEnvironment env, Element shipmentElem) throws Exception {
		Document docOutGetShipmentList = null;
		try {
			log.verbose("Inside invokeGetShipmentList :: \n" );
			Document inDocGetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element eleShipment = inDocGetShipmentList.getDocumentElement();
			XMLUtil.copyElement(inDocGetShipmentList, shipmentElem, eleShipment);
			
			log.verbose("getShipmentList Input:: \n" + XMLUtil.getXMLString(inDocGetShipmentList));
			Document docShipmentListTemplate = YFCDocument.getDocumentFor(
					AcademyConstants.GET_SHIPMENT_LIST_SERIAL_NO).getDocument();
			env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docShipmentListTemplate);
			
			docOutGetShipmentList = AcademyUtil.invokeAPI(env, 
					AcademyConstants.API_GET_SHIPMENT_LIST, inDocGetShipmentList);
			env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
			log.verbose("getShipmentList Output:: \n" + XMLUtil.getXMLString(docOutGetShipmentList));
		} finally {
			log.endTimer("AcademyRegisterBarCodeForRecordBackroomPick.getShipment");
		}
		return docOutGetShipmentList;
	}

	/** Invokes translateBarCode api to translate barcodes and tie them to items/ cartons.
	 * Sample Input:
	 * <BarCode BarCodeData="020697637" BarCodeType="Item">
		    <ContextualInfo EnterpriseCode="Academy_Direct" OrganizationCode="033"/>
		</BarCode>
		
		Sample Output:
		<BarCode BarCodeData="020697637" BarCodeType="Item">
		    <Translations BarCodeTranslationSource="ItemID" TotalNumberOfRecords="1">
		        <Translation>
		            <ContextualInfo EnterpriseCode="Academy_Direct"
		                InventoryOrganizationCode="Academy_Direct" OrganizationCode="DEFAULT"/>
		            <ItemContextualInfo InventoryUOM="EACH" ItemID="020697637" KitCode=""/>
		        </Translation>
		    </Translations>
		</BarCode>
	 * @throws ParserConfigurationException 
	 */
	private Document invokeTranslateBarcode(YFSEnvironment env, Document inDoc) {
		Document docOutTranslateBarcode = null;
		try {
			log.verbose("Inside invokeTranslateBarcode :: \n" );
			Element inputElem = inDoc.getDocumentElement();
			Element eleContextualInfoInput = (Element) inDoc.getElementsByTagName(AcademyConstants.ELE_BAR_CONTEXT_INFO).item(0);
			Element eleItemContextualInfoInput = (Element) inDoc.getElementsByTagName(AcademyConstants.ELE_CONTEXT_INFO).item(0);
			
			Document inDocTranslateBarcode = XMLUtil.createDocument(AcademyConstants.ELE_BAR_CODE);
			Element eleTranslateBarcode = inDocTranslateBarcode.getDocumentElement();
			eleTranslateBarcode.setAttribute(AcademyConstants.ATTR_BAR_CODE_DATA, 
					inputElem.getAttribute(AcademyConstants.ATTR_BAR_CODE_DATA));
			eleTranslateBarcode.setAttribute(AcademyConstants.ATTR_BAR_CODE_TYPE, 
					inputElem.getAttribute(AcademyConstants.ATTR_BAR_CODE_TYPE));
			Element eleContextualInfo = inDocTranslateBarcode.createElement(AcademyConstants.ELE_BAR_CONTEXT_INFO);
			eleTranslateBarcode.appendChild(eleContextualInfo);
			
			Element eleItemContextualInfo = inDocTranslateBarcode.createElement(AcademyConstants.ELE_CONTEXT_INFO);
			eleTranslateBarcode.appendChild(eleContextualInfo);
			
			if (!YFCObject.isVoid(eleContextualInfoInput)) {
				XMLUtil.copyElement(inDocTranslateBarcode, eleContextualInfoInput, eleContextualInfo);
			}
			if (!YFCObject.isVoid(eleItemContextualInfoInput)) {
				XMLUtil.copyElement(inDocTranslateBarcode, eleItemContextualInfoInput, eleItemContextualInfo);
			}			
			log.verbose("translateBarcode Input:: \n" + XMLUtil.getXMLString(inDocTranslateBarcode));
			
			Document translateBarcodeTemp = YFCDocument.getDocumentFor(
					AcademyConstants.TRANSLATE_BARCODE).getDocument();
			env.setApiTemplate(AcademyConstants.API_TRANSLATE_BARCODE, translateBarcodeTemp);
			log.verbose("invoking api \n" + AcademyConstants.API_TRANSLATE_BARCODE);
			docOutTranslateBarcode = AcademyUtil.invokeAPI(env, 
					AcademyConstants.API_TRANSLATE_BARCODE, inDocTranslateBarcode);
			env.clearApiTemplate(AcademyConstants.API_TRANSLATE_BARCODE);
			log.verbose("translateBarcode OutputDocument:: \n" + XMLUtil.getXMLString(docOutTranslateBarcode));			
			
		}catch (Exception t) {
			//OMNI-93022 - Start
			try {
				docOutTranslateBarcode = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
				Element eleError = docOutTranslateBarcode.getDocumentElement();
				eleError.setAttribute(AcademyConstants.ATTR_ERROR_DESC,AcademyConstants.ERROR_DESC_NOT_AVAILABLE);
				log.verbose("translateBarcode Output Error Document:: \n" + XMLUtil.getXMLString(docOutTranslateBarcode));
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			//OMNI-93022 - End
			t.printStackTrace(); 
			log.verbose("Exception in callTranslateBarcode");
			log.info("Error Trace::::" + t.toString());
		}
		finally {
			log.endTimer("AcademyRegisterBarCodeForRecordBackroomPick.callTranslateBarcode");
		}
		return docOutTranslateBarcode;
	}

	/** Checks for the matching shipmentLine (from the getShipmentList output)
	 * by validating itemID, UOM, KitCode.
	 * 
	 * Sample Output:
	 	<ShipmentLine BackroomPickedQuantity="1.0" ItemID="022121032" KitCode=""
		    OrderHeaderKey="202210030237035685449887"
		    OrderLineKey="202210030237035685449888" OrderNo="2022100302"
		    OriginalQuantity="1.00" Quantity="1.00"
		    ShipmentKey="202210030240275685451398"
		    ShipmentLineKey="202210030240275685451397" ShipmentLineNo="1"
		    ShipmentSubLineNo="0" ShortageQty="0.00" UnitOfMeasure="EACH">
		    <ShipmentTagSerials>
		        <ShipmentTagSerial Quantity="1.00" SerialNo="020697637" ShipmentLineKey="202211140720495722296299"/>
		        <ShipmentTagSerial Quantity="1.00" SerialNo="0206976374" ShipmentLineKey="202211140720495722296299"/>
		    </ShipmentTagSerials>
		    <OrderLine IsBundleParent="N" ItemGroupCode="PROD">
		        <ItemDetails ItemGroupCode="PROD" ItemID="022121032" UnitOfMeasure="EACH">
		            <PrimaryInformation
		                ExtendedDisplayDescription="Stevens 320 Security 12GA/18.5Pump Shotgun:12 Gauge:18.5- Length:Right (022121032)"
		                ImageID="10200280" ImageLocation="https://s7d2.scene7.com/is/image/academy/"/>
		            <ClassificationCodes Model="201205527"/>
		        </ItemDetails>
		    </OrderLine>
		</ShipmentLine>
	 * 
	 * @param docOutGetShipmentList
	 * @param docOutTranslateBarcode
	 * @return
	 * @throws ParserConfigurationException
	 */
	private Element checkForMatchingShipLine(Document docOutGetShipmentList,
			Document docOutTranslateBarcode) throws ParserConfigurationException {
		log.beginTimer("AcademyRegisterBarCodeForRecordBackroomPick.checkForMatchingShipLine");
		Document docMatchingShipLine = null;
		Element eleMatchingShipLine = null;
		Element eleShipLine = null;
		String itemId = "";
		String uom = "";
		String kitcode = "";
		String backroomPickedQty = "";
		String qty = "";
		Double dQty = 0.00;
		try {
			Element eleTranslation = (Element) docOutTranslateBarcode.getElementsByTagName(
					AcademyConstants.ELE_TRANSLATION).item(0);
			Element eleItemContextualInfo = (Element) eleTranslation.getElementsByTagName(
					AcademyConstants.ELE_CONTEXT_INFO).item(0);
			String sItemId = eleItemContextualInfo.getAttribute(AcademyConstants.ITEM_ID);
			String sUom = eleItemContextualInfo.getAttribute(AcademyConstants.ATTR_INV_UOM);
			String sKitCode = eleItemContextualInfo.getAttribute(AcademyConstants.KIT_CODE);
			String sqty = eleItemContextualInfo.getAttribute(AcademyConstants.ATTR_QUANTITY);
			if (!YFCObject.isVoid(sqty)) {
				dQty = Double.parseDouble(sqty);
			}
			if (!YFCObject.isVoid(dQty) && (dQty <= 0.0D)) {
				dQty = 1.0D;
				eleItemContextualInfo.setAttribute(AcademyConstants.ATTR_QUANTITY, dQty.toString());
			}			
			NodeList nShipmentLines = docOutGetShipmentList.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			for (int i=0; i < nShipmentLines.getLength(); i++) {
				eleShipLine = (Element) nShipmentLines.item(i);
				log.verbose("Current ShipmentLine element :: " + XMLUtil.getElementXMLString(eleShipLine));
				itemId = eleShipLine.getAttribute(AcademyConstants.ITEM_ID);
				uom = eleShipLine.getAttribute(AcademyConstants.ATTR_UOM);
				kitcode = eleShipLine.getAttribute(AcademyConstants.KIT_CODE);
				if (!itemId.equalsIgnoreCase(sItemId) || !uom.equalsIgnoreCase(sUom) || 
					!kitcode.equalsIgnoreCase(sKitCode))
					continue;
				docMatchingShipLine = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT_LINE);
				eleMatchingShipLine = docMatchingShipLine.getDocumentElement();
				XMLUtil.copyElement(docMatchingShipLine, eleShipLine, eleMatchingShipLine);
				backroomPickedQty = eleMatchingShipLine.getAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY);
				qty = eleMatchingShipLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
				Double d = (YFCObject.isVoid(backroomPickedQty)) ? 0.0D : Double.parseDouble(backroomPickedQty);
				Double finalQty = d + dQty;
				log.verbose("Quantities to be checked ::: \n Quantity on current ship line qty: " + qty + 
						"\n BackroomPickedQuantity on current ship line d: " + d + 
						"\n Qty from Barcode ItemContextualInfo dQty: " + dQty);
				if (Double.parseDouble(qty) >= finalQty) {
					eleMatchingShipLine.setAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY, finalQty.toString());
					appendShipmentLineInformationToOutput(eleMatchingShipLine, docOutTranslateBarcode, docOutGetShipmentList);
					return eleMatchingShipLine;
				}			
			}
			if (eleMatchingShipLine == null) {
				YFCException yFCException = new YFCException("YCD00066");
				yFCException.setAttribute(AcademyConstants.ITEM_ID, sItemId);
				yFCException.setAttribute(AcademyConstants.ATTR_UOM, sUom);
				throw yFCException;
			}
			YFCException ex = new YFCException("YCD00067");
			ex.setAttribute(AcademyConstants.ITEM_ID, sItemId);
			ex.setAttribute(AcademyConstants.ATTR_UOM, sUom);
			ex.setAttribute("ScannedQUantity", Double.toString(dQty));
			backroomPickedQty = eleMatchingShipLine.getAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY);
			Double dBackroomPickedQty = (YFCObject.isVoid(backroomPickedQty)) ? 0.0D : Double.parseDouble(backroomPickedQty);
			ex.setAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY, Double.toString(dBackroomPickedQty));
			ex.setAttribute(AcademyConstants.ATTR_QUANTITY, eleMatchingShipLine.getAttribute(AcademyConstants.ATTR_QUANTITY));
			throw ex;
		} finally {
			log.endTimer("AcademyRegisterBarCodeForRecordBackroomPick.findMatchingShipmentLine");
		}		
	}

	/** Appends ShipmentContextualInfo to the Barcode Translation.
	 * Sample Output:
	 * <Translation>
		    <ContextualInfo EnterpriseCode="Academy_Direct"
		        InventoryOrganizationCode="Academy_Direct" OrganizationCode="DEFAULT"/>
		    <ItemContextualInfo InventoryUOM="EACH" ItemID="022121032"
		        KitCode="" Quantity="1.0"/>
		    <ShipmentContextualInfo SellerOrganizationCode="Academy_Direct"
		        ShipNode="033" ShipmentKey="202210030240275685451398"
		        ShipmentLineKey="202210030240275685451397" ShipmentNo="100555487"/>
		</Translation>
	 * @param eleMatchingShipLine
	 * @param docOutTranslateBarcode
	 * @param docOutGetShipmentList
	 */
	private void appendShipmentLineInformationToOutput(Element eleMatchingShipLine, Document docOutTranslateBarcode, Document docOutGetShipmentList) {
		try {
			log.verbose("Inside appendShipmentLineInformationToOutput :: \n" );
			Element eleShipmentContextualInfo = docOutTranslateBarcode.createElement(AcademyConstants.SHIPMENT_CONTEXTUAL_INFO); 
			Element eleTranslation = (Element) docOutTranslateBarcode.getElementsByTagName(
					AcademyConstants.ELE_TRANSLATION).item(0);
			Element eleShipment = (Element) XPathUtil.getNode(docOutGetShipmentList, AcademyConstants.XPATH_SHIPMENT);
			eleShipmentContextualInfo.setAttribute(AcademyConstants.ATTR_SELL_ORG_CODE,
					eleShipment.getAttribute(AcademyConstants.ATTR_SELL_ORG_CODE));
			eleShipmentContextualInfo.setAttribute(AcademyConstants.ATTR_SHIP_NODE,
					eleShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE));
			eleShipmentContextualInfo.setAttribute(AcademyConstants.SHIPMENT_KEY,
					eleShipment.getAttribute(AcademyConstants.SHIPMENT_KEY));
			eleShipmentContextualInfo.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO,
					eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
			eleShipmentContextualInfo.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY,
					eleMatchingShipLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
			eleTranslation.appendChild(eleShipmentContextualInfo);
		} catch (Exception a) {
			a.printStackTrace(); 
			log.verbose("Exception in appendShipmentLineInformationToOutput");
			log.info("error Trace :: " + a.toString());
		}		
	}

	/** Invokes changeShipment api to update the serialNo at 
	 * /Shipment/ShipmentLines/ShipmentLine/ShipmentTagSerials/ShipmentTagSerial level.
	 * changeShipment api sample input:
	 	<Shipment ShipmentKey="202210030240275685451398">
		    <ShipmentLines>
		        <ShipmentLine BackroomPickedQuantity="1.0" ShipmentLineKey="202210030240275685451397">
		            <ShipmentTagSerials>
		                <ShipmentTagSerial Quantity="1" SerialNo="123456789" ShipmentLineKey="202210030240275685451397"/>
		            </ShipmentTagSerials>
		        </ShipmentLine>
		    </ShipmentLines>
		</Shipment>
		
		changeShipment api sample output:
		<Shipment ShipmentKey="202210030240275685451398">
		    <ShipmentLines>
		        <ShipmentLine ShipmentLineKey="202210030240275685451397">
		            <ShipmentTagSerials>
		                <ShipmentTagSerial Quantity="1.00" SerialNo="020697637" ShipmentLineKey="202211140720495722296299"/>
		                <ShipmentTagSerial Quantity="1.00" SerialNo="0206976377" ShipmentLineKey="202211140720495722296299"/>
		                <ShipmentTagSerial Quantity="1.00" SerialNo="123456789" ShipmentLineKey="202210030240275685451397"/>
		            </ShipmentTagSerials>
		        </ShipmentLine>
		    </ShipmentLines>
		</Shipment>
		
	 * @param env
	 * @param shipmentKey
	 * @param oMatchingLine
	 * @param strSerialNo
	 * @return
	 * @throws Exception
	 */
	private Document callChangeShipment(YFSEnvironment env, String shipmentKey, Element oMatchingLine, String strSerialNo) throws Exception {
		Document changeShipmentOutDoc = null;
		try {
			log.beginTimer("AcademyRegisterBarCodeForRecordBackroomPick.callChangeShipment");
			Document changeShipmentInputDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element changeShipmentElem = changeShipmentInputDoc.getDocumentElement();
			changeShipmentElem.setAttribute(AcademyConstants.SHIPMENT_KEY, shipmentKey);
			
			Element shipLines = changeShipmentInputDoc.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
			Element lineElem = changeShipmentInputDoc.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
			lineElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY,
					oMatchingLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
			lineElem.setAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY, 
					oMatchingLine.getAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY));
			
			Element shipTagSerials = changeShipmentInputDoc.createElement(AcademyConstants.ELE_SHIPEMNT_TAG_SERIALS);
			Element shipTagSerialElem = changeShipmentInputDoc.createElement(AcademyConstants.ELE_SHIPMENT_TAG_SERIAL);
			shipTagSerialElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, 
					oMatchingLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
			shipTagSerialElem.setAttribute(AcademyConstants.ATTR_QUANTITY, AcademyConstants.STR_ONE);
			shipTagSerialElem.setAttribute(AcademyConstants.ATTR_SERIAL_NO, strSerialNo);
			
			shipTagSerials.appendChild(shipTagSerialElem);
			lineElem.appendChild(shipTagSerials);
			shipLines.appendChild(lineElem);
			changeShipmentElem.appendChild(shipLines);
			
			Document changeShipTempl = YFCDocument.getDocumentFor(
					AcademyConstants.CHANGE_SHIPMENT_SERIAL_NO_TEMP).getDocument();
			env.setApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT, changeShipTempl);
			
			log.verbose("changeShipment Input Document \n" + XMLUtil.getXMLString(changeShipmentInputDoc));
			changeShipmentOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, changeShipmentInputDoc);
			log.verbose("changeShipment Output \n" + XMLUtil.getXMLString(changeShipmentOutDoc));
			
			env.clearApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT);
		} finally {
			log.endTimer("AcademyRegisterBarCodeForRecordBackroomPick.callChangeShipment");
		}
		return changeShipmentOutDoc;
	}


	/** Prepares final output by appending the matchingShipLine to the Barcode xml.
	 * 
	 * @param docOutTranslateBarcode
	 * @param oShipment
	 * @param oMatchingLine
	 * @param changeShipmentOutDoc
	 * @return
	 */
	private Document prepareOutputDocument(Document docOutTranslateBarcode, Document oShipment, 
			Element oMatchingLine, Document changeShipmentOutDoc) {
		try {
			log.verbose("Inside prepareOutputDocument :: \n" );
			Element eleShipment = (Element) XPathUtil.getNode(oShipment, AcademyConstants.XPATH_SHIPMENT);
			Element eleShipLinesToBeRemoved = (Element) oShipment.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES).item(0);
			
			eleShipment.removeChild(eleShipLinesToBeRemoved);
			Element eleNewShipLine = oShipment.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
			XMLUtil.copyElement(oShipment, oMatchingLine, eleNewShipLine);
			eleShipment.appendChild(eleNewShipLine);
			
			String shipmentLineKey = oMatchingLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
			Element eleShipmentSerials = (Element) oShipment.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_TAG_SERIALS).item(0);
			Element eleShpLine = (Element) oShipment.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE).item(0);
			NodeList nlChangeShipSerial = XPathUtil.getNodeList(changeShipmentOutDoc, 
					"/Shipment/ShipmentLines/ShipmentLine");
			Element eleShipLines = (Element) changeShipmentOutDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES).item(0);
			
			if (eleShpLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY).equalsIgnoreCase(shipmentLineKey)) {
				eleShpLine.removeChild(eleShipmentSerials);
				Element eleNewShipmentSerial = oShipment.createElement(AcademyConstants.ELE_SHIPMENT_TAG_SERIALS);
				eleShpLine.appendChild(eleNewShipmentSerial);
				for (int j = 0; j < nlChangeShipSerial.getLength(); j++) {
					Element eleChangeSHip = (Element) nlChangeShipSerial.item(j);
					if (eleChangeSHip.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY).equalsIgnoreCase(shipmentLineKey)) {
						Element eleChangeShipSerial = (Element) changeShipmentOutDoc.getElementsByTagName(
								AcademyConstants.ELE_SHIPMENT_TAG_SERIALS).item(0);
						XMLUtil.copyElement(oShipment, eleChangeShipSerial, eleNewShipmentSerial);
						log.verbose("Shipment Element to be appended to Barcode:: " + XMLUtil.getElementXMLString(eleShipment));
					} else {
						eleShipLines.removeChild(eleChangeSHip);
					}
				}
			}			
			Element eleBarcode = docOutTranslateBarcode.getDocumentElement();
			Element eleBarcodeShipment = docOutTranslateBarcode.createElement(AcademyConstants.ELE_SHIPMENT);
			XMLUtil.copyElement(docOutTranslateBarcode, eleShipment, eleBarcodeShipment);		
			eleBarcode.appendChild(eleBarcodeShipment);
			log.verbose("Final Barcode output Doc::\n " + XMLUtil.getXMLString(docOutTranslateBarcode));
		} catch (Exception p) {
			p.printStackTrace(); 
			log.verbose("Exception in prepareOutputDocument");
			log.info("Error---Trace :: " + p.toString());
		}        		
        return docOutTranslateBarcode;
    }
	
	/** Validates if the input has the below fields.
	 * <ShipmentContextualInfo SellerOrganizationCode="Academy_Direct"
        ShipNode="033" ShipmentKey="202209131306275680724794"/>
	 * @param inDoc
	 * @throws Exception
	 */
	private void validateInputDocument(Document inDoc) throws Exception {
		log.verbose("Inside validateInputDocument :: \n" );
	    Element inDocElem = null;
	    	if (inDoc != null)
	  	      inDocElem = inDoc.getDocumentElement(); 
	  	    if (inDoc == null || inDocElem == null || !YFCCommon.equals(inDocElem.getTagName(), "BarCode")) {
	  	      throw new YFCException("YCP0073");
	  	    } 
	  	    Element oContextualInfo = (Element) XMLUtil.getNode(inDocElem, "ContextualInfo");
	  	    if (oContextualInfo == null || YFCUtils.isVoid(oContextualInfo.getAttribute("OrganizationCode"))) {
	  	      throw new YFCException("YFS10513");
	  	    } 
	  	    if (YFCUtils.isVoid(inDocElem.getAttribute("BarCodeType"))) {
	  	      throw new YFCException("YCP0186");
	  	    } 
	  	    if (YFCUtils.isVoid(inDocElem.getAttribute("BarCodeData"))) {
	  	      throw new YFCException("YCP0187");
	  	    } 
	  	    Element shipmentElem = (Element) XMLUtil.getNode(inDocElem, "ShipmentContextualInfo");
	  	    if (shipmentElem == null) {
	  	      throw new YFCException("YDM00065");
	  	    } 
	  	    String sShipmentKey = shipmentElem.getAttribute(AcademyConstants.SHIPMENT_KEY);
	  	    String sShipmentNo = shipmentElem.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
	  	    String sShipNode = shipmentElem.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
	  	    String sSellerOrg = shipmentElem.getAttribute(AcademyConstants.ATTR_SELL_ORG_CODE);
	  	    if (YFCObject.isVoid(sShipmentKey) && 
	  	      YFCObject.isVoid(sShipmentNo) && YFCObject.isVoid(sSellerOrg) && YFCObject.isVoid(sShipNode)) {
	  	      throw new YFCException("YDM00065");
	  	    }
	  }
}