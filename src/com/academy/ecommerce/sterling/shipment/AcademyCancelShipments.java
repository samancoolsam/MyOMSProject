package com.academy.ecommerce.sterling.shipment;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.cts.sterling.custom.accelerators.util.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCIterable;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dblayer.YFCContext;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * Created for STL-685 Cancel the Shipments in Ready To Ship and Ready for
 * Backroom Pick from SOM shipment details screen.
 * 
 * 
 */

/*
 * Sample XML Structure to the class from SOM.
 * 
 * <Shipment BolNo="" CarrierAccountNo=""
    CarrierServiceCode="Home Delivery" CarrierType="PARCEL"
    Currency="USD" DeliveryMethod="SHP"
    DisplayLocalizedFieldInLocale="en_US_EST" DocumentType="0001"
    EnterpriseCode="Academy_Direct"
    ExpectedDeliveryDate="2014-01-23T08:49:00-05:00"
    ExpectedShipmentDate="2014-01-23T08:49:00-05:00" IsSingleOrder="Y"
    ManifestKey="" ManifestNo="" NumOfCartons="1"
    OrderHeaderKey="201401230835012364750316" OrderNo="10418909"
    RequestedShipmentDate="2014-01-23T08:49:00-05:00" SCAC="FEDX"
    ScacAndService="FedEx Home Delivery"
    ScacAndServiceKey="2004121615594445144" ScacIntegrationRequired="Y"
    SellerOrganizationCode="Academy_Direct" ShipNode="116"
    ShipmentKey="201401230853082364770247" ShipmentNo="102799728"
    ShipmentType="CON" Status="1100.70.06.10"
    ToAddressKey="201401230835022364750364" isHistory="N">
    <ToAddress AddressLine1="20610 barkston ct" AddressLine2=""
        AddressLine3="" AddressLine4="" AddressLine5="" AddressLine6=""
        AlternateEmailID="" Beeper="" City="Katy" Company=""
        Country="US" Createprogid="AcademyCreateOrderInterfaceServer"
        Createts="2014-01-23T08:35:02-05:00"
        Createuserid="AcademyCreateOrderInterfaceServer" DayFaxNo=""
        DayPhone="8323724028" Department="" EMailID="abcd@efgh.com"
        ErrorTxt="" EveningFaxNo="" EveningPhone="" FirstName="Sheryl"
        HttpUrl="" IsAddressVerified="Y" JobTitle="" LastName="Mancey"
        Lockid="0" MiddleName="" MobilePhone=""
        Modifyprogid="AcademyCreateOrderInterfaceServer"
        Modifyts="2014-01-23T08:35:02-05:00"
        Modifyuserid="AcademyCreateOrderInterfaceServer" OtherPhone=""
        PersonID="" PersonInfoKey="201401230835022364750364"
        PreferredShipAddress="" State="TX" Suffix="" Title=""
        UseCount="0" VerificationStatus="" ZipCode="77450-4222" isHistory="N"/>
    <BillToAddress AddressLine1="20610 barkston ct" AddressLine2=""
        AddressLine3="" AddressLine4="" AddressLine5="" AddressLine6=""
        City="Katy" Country="US" DayPhone="8323724028"
        EMailID="abcd@efgh.com" FirstName="Sheryl" LastName="Mancey"
        State="TX" ZipCode="77450-4222"/>
    <ShipNode ShipnodeKey="116">
        <ShipNodePersonInfo AddressLine1="7600 Westheimer Rd"
            AddressLine2="" AddressLine3="" AddressLine4=""
            AddressLine5="" AddressLine6="" AlternateEmailID=""
            City="Houston" Company="" Country="US" DayFaxNo=""
            DayPhone="7132684300" Department="" EMailID=""
            EveningFaxNo="" EveningPhone="7132684300"
            FirstName="Store - 116" JobTitle="" LastName=""
            MiddleName="" MobilePhone="7132684300" PersonID=""
            PersonInfoKey="20120812110930352980544"
            PreferredShipAddress="" State="TX" Title="" ZipCode="77063"/>
    </ShipNode>
    <FromAddress AddressLine1="7600 Westheimer Rd" AddressLine2=""
        AddressLine3="" AddressLine4="" AddressLine5="" AddressLine6=""
        AlternateEmailID="" City="Houston" Company="" Country="US"
        DayFaxNo="" DayPhone="7132684300" Department="" EMailID=""
        EveningFaxNo="" EveningPhone="7132684300"
        FirstName="Store - 116" JobTitle="" LastName="" MiddleName=""
        MobilePhone="7132684300" OtherPhone="" PersonID=""
        PersonInfoKey="20120812110930352980544" PreferredShipAddress=""
        State="TX" Title="" ZipCode="77063"/>
    <Containers TotalNumberOfRecords="0"/>
    <Status Description="Ready For Backroom Pick"/>
    <ShipmentLines>
        <ShipmentLine ActualQuantity="1.00" DocumentType="0001"
            ItemDesc="Nike Breast Cancer Awareness Lanyard"
            ItemID="022379747" KitCode=""
            OrderHeaderKey="201401230835012364750316"
            OrderLineKey="201401230835022364750358" OrderNo="10418909"
            OrderReleaseKey="201401230849002364762938" Quantity="1.00"
            ReceivedQuantity="0.00"
            ShipmentLineKey="201401230853052364770177"
            ShipmentLineNo="2" ShipmentSubLineNo="0"
            UnitOfMeasure="EACH" isHistory="N">
            <OrderLine DeliveryMethod="SHP" GiftFlag="N"
                IsBundleParent="N" OrderLineKey="201401230835022364750358">
                <Item ItemID="022379747" ItemShortDesc="Nike Breast Cancer Awareness Lanyard"/>
                <LinePriceInfo UnitPrice="7.99"/>
                <PersonInfoShipTo Country="US" DayPhone="8323724028"
                    EMailID="abcd@efgh.com" EveningPhone=""
                    FirstName="Sheryl" LastName="Mancey" MiddleName=""
                    PersonInfoKey="201401230835022364750364" State="TX"/>
                <ChildOrderLineRelationships/>
                <ParentOrderLineRelationships/>
                <ItemDetails ItemID="022379747">
                    <PrimaryInformation DefaultProductClass="GOOD"
                        Description="Nike Breast Cancer Awareness Lanyard"
                        ExtendedDescription="" ShortDescription="Nike Breast Cancer Awareness Lanyard"/>
                </ItemDetails>
                <ComputedPrice UnitPrice="7.99"/>
            </OrderLine>
            <Order DocumentType="0001" EnterpriseCode="Academy_Direct"/>
        </ShipmentLine>
        <ShipmentLine ActualQuantity="1.00" DocumentType="0001"
            ItemDesc="Tervis Fight Like a Girl 24 oz. Tumbler"
            ItemID="023333883" KitCode=""
            OrderHeaderKey="201401230835012364750316"
            OrderLineKey="201401230835022364750357" OrderNo="10418909"
            OrderReleaseKey="201401230849002364762938" Quantity="1.00"
            ReceivedQuantity="0.00"
            ShipmentLineKey="201401230853052364770176"
            ShipmentLineNo="1" ShipmentSubLineNo="0"
            UnitOfMeasure="EACH" isHistory="N">
            <OrderLine DeliveryMethod="SHP" GiftFlag="N"
                IsBundleParent="N" OrderLineKey="201401230835022364750357">
                <Item ItemID="023333883" ItemShortDesc="Tervis Fight Like a Girl 24 oz. Tumbler"/>
                <LinePriceInfo UnitPrice="18.99"/>
                <PersonInfoShipTo Country="US" DayPhone="8323724028"
                    EMailID="abcd@efgh.com" EveningPhone=""
                    FirstName="Sheryl" LastName="Mancey" MiddleName=""
                    PersonInfoKey="201401230835022364750364" State="TX"/>
                <ChildOrderLineRelationships/>
                <ParentOrderLineRelationships/>
                <ItemDetails ItemID="023333883">
                    <PrimaryInformation DefaultProductClass="GOOD"
                        Description="Tervis Fight Like a Girl 24 oz. Tumbler"
                        ExtendedDescription="Take your favorite beverage along with you wherever you go in the Tervis Fight Like a Girl 24 oz. Tumbler, which is great for both hot and cold beverages and designed to reduce condensation and sweating. The tumbler is decorated with the phrase Fight Like a Girl and the Ribbon &amp; Gloves logo and comes with a pink travel lid. Microwave, freezer and dishwasher safe. Fits in most size cupholders. Made in USA." ShortDescription="Tervis Fight Like a Girl 24 oz. Tumbler"/>
                </ItemDetails>
                <ComputedPrice UnitPrice="18.99"/>
            </OrderLine>
            <Order DocumentType="0001" EnterpriseCode="Academy_Direct"/>
        </ShipmentLine>
    </ShipmentLines>
</Shipment>
 */

public class AcademyCancelShipments implements YIFCustomApi {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyCancelShipments.class);
	private String changeShipmentOutputTemplate = "global/template/api/AcademyCancelShipmentService_changeShipment.xml";

	public Document cancelShipment(YFSEnvironment env, Document inXML) throws Exception {

		boolean sendSTOMessage = false;
		Document changeShipmentOut = null;

		log.verbose("AcademyCancelShipments_cancelShipment_InXML:" + XMLUtil.getString(inXML));

		String strShipmentKey = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		String strShipmentNo = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		
		//START: SHIN-11
		//Getting the attribute IsSharedInventoryDC from the input xml
		String strIsSharedInvNode = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_IS_SHAREDINV_DC);
		//END: SHIN-11
		
		Document cancelShipmentInDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		cancelShipmentInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
		cancelShipmentInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.VAL_CANCEL);
		
		//START: SHIN-11
		log.verbose("IsSharedInventoryDC1:" +strIsSharedInvNode);
		
		/*Condition to validate IsSharedInventoryDC is not null and the value equals Y
		Then set the attribute of BackOrderRemovedQuantity ='Y'*/
		
		if (!YFCObject.isVoid(strIsSharedInvNode)
				&& AcademyConstants.ATTR_Y.equals(strIsSharedInvNode)) {
			log.verbose("IsSharedInventoryDC:" +strIsSharedInvNode);
			cancelShipmentInDoc.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_BACK_ORD_REM_QTY, AcademyConstants.ATTR_Y);
		}
		//END: SHIN-11
		

		log.verbose("cancelShipmentInDoc::"+ XMLUtil.getString(cancelShipmentInDoc));


		YFCElement shipmentElem = getShipmentElem(strShipmentKey, inXML);

		if (shipmentElem != null && !YFCObject.isVoid(strShipmentKey)) {
			// Shipment is in Ready to Ship Status. Shipment needs to be
			// removed from Manifest.
			log.verbose("AcademyCancelShipments_shipmentElem:" + shipmentElem.getString());
			removeShipmentFromManifest(env, strShipmentKey, strShipmentNo);
			log.verbose("The status of the Shipment is Ready to Ship");
			sendSTOMessage = true;
		}

		env.setApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT, changeShipmentOutputTemplate);
		log.verbose("The template Document is" + changeShipmentOutputTemplate);

		try {
			changeShipmentOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, cancelShipmentInDoc);
		} catch (Exception ex) {
			ex.printStackTrace();
			YFCException yfcEx = null;
			String errorCodeDesc = null;
			String strManifestErrorCode = "YDM00210";
			String strManifestErrorMessage = "Shipment contains a partial pick";
			if (ex instanceof YFCException) {
				errorCodeDesc = ((YFCException) ex).getAttribute(YFCException.ERROR_CODE);
			} else if (ex instanceof YFSException) {
				errorCodeDesc = ((YFSException) ex).getErrorCode();
			}

			if (errorCodeDesc != null) {
				if (errorCodeDesc.equals("YDM00210")) {
					yfcEx = new YFCException();
					log.info("The errorCode is a code" + errorCodeDesc);

					log.info("Exception Message: " + ex.getMessage());
					yfcEx.setAttribute(YFCException.ERROR_CODE, strManifestErrorCode);
					log.info("Error Code has been set in attribute" + strManifestErrorCode);
					yfcEx.setAttribute(YFCException.ERROR_DESCRIPTION, strManifestErrorMessage);
					log.info("Error Description has been set" + strManifestErrorMessage);
					yfcEx.setErrorDescription(strManifestErrorMessage);
					log.info("Error Description has been set using method setErrorDescription " + strManifestErrorMessage);
				}
			}
			throw yfcEx;
		}


		env.clearApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT);

		// Publish Reversal Stock Transfer Message to RMS.
		log.verbose("STO message to RMS:" + sendSTOMessage);
		if (sendSTOMessage) {
			stampMissingAttributes(env, shipmentElem, changeShipmentOut);
			log.verbose("STO Message sent to AcademyPublishReverseSTOService:" + XMLUtil.getString(changeShipmentOut));
			AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_PUBLISH_REVERSE_STO_MSG, changeShipmentOut);
		}


		log.verbose("AcademyCancelShipments_changeShipmentOut:" + XMLUtil.getString(changeShipmentOut));
		return changeShipmentOut;
	}

	/**
	 * Gets the shipment element which has status as Ready to Ship by iterating
	 * through each and every shipment element. OOB input from SIM has multiple
	 * shipment elements appended under root shipment element based on different
	 * shipment status.
	 * 
	 * @param strShipmentKey -
	 *            shipment key of the root shipment element.
	 * @param inXML -
	 *            Input document to this custom API
	 * 
	 * @return - shipment element which has status as Ready to Ship, else it
	 *         will be null.
	 */

	private YFCElement getShipmentElem(String strShipmentKey, Document inXML) {

		String status = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_STATUS);
		log.verbose("status::"+ status);
		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inXML);
		YFCElement shipmentElem = yfcInDoc.getDocumentElement();
		if (!YFCObject.isVoid(status) && status.equalsIgnoreCase(AcademyConstants.VAL_READY_TO_SHIP_STATUS)) {
			log.verbose("Status is ReadyTo Ship");
			return shipmentElem;
		}

		return null;
	}

	/**
	 * Used to execute the DB queries to update the yfs_shipment and the
	 * yfs_shipment_container tables
	 * 
	 */

	private void removeShipmentFromManifest(YFSEnvironment env, String strShipmentKey, String strShipmentNo) throws SQLException {

		String strvoidTrackingQry = "UPDATE " + AcademyConstants.TABLE_YFS_SHIPMENT_CONT + " set " + "TRACKING_NO='' where SHIPMENT_KEY='" + strShipmentKey + "'";
		log.verbose("Query to void the Tracking No: \n " + strvoidTrackingQry);
		String strRemoveShipFromManifestQry = "UPDATE " + AcademyConstants.TABLE_YFS_SHIPMENT + " set " + "MANIFEST_NO='', MANIFEST_KEY='' where SHIPMENT_KEY='" + strShipmentKey + "'";
		log.verbose("Query to remove shipment from manifest: \n " + strRemoveShipFromManifestQry);
		String strUpdateManifestUpsDtl = "UPDATE " + AcademyConstants.TABLE_YCS_MANIFEST_UPS_DTL + " set " + "SHIPMENT_NUMBER='DUMMY',MANIFEST_NUMBER='DUMMY' where SHIPMENT_NUMBER='" + strShipmentNo + "'";
		log.verbose("Query to Update YCS_MANIFEST_UPS_DTL: \n " + strUpdateManifestUpsDtl);
		Statement stmt = null;
		try {
			YFCContext ctxt = (YFCContext) env;
			stmt = ctxt.getConnection().createStatement();
			int hasUpdated = stmt.executeUpdate(strvoidTrackingQry);
			int hasUpdated1 = stmt.executeUpdate(strRemoveShipFromManifestQry);
			int hasUpdated2 = stmt.executeUpdate(strUpdateManifestUpsDtl);
			if (hasUpdated > 0) {
				log.verbose("Tracking No has been blanked out.");
			}
			if (hasUpdated1 > 0) {
				log.verbose("Updated the ManifestNo and ManifestKey.");
			}
			if (hasUpdated2 > 0) {
				log.verbose("Updated the ShipmentNo and ManifestNo as DUMMY.");
			}
		} catch (SQLException sqlEx) {
			log.verbose("Error occured on removing shipment from manifest");
			sqlEx.printStackTrace();
			throw sqlEx;
		} finally {
			if (stmt != null)
				stmt.close();
			stmt = null;
		}
	}

	/**
	 * Updates quantity and itemUPC code in the message that needs to be sent to
	 * RMS.
	 * 
	 */

	private void stampMissingAttributes(YFSEnvironment env, YFCElement shipmentElem, Document changeShipmentOut) throws Exception {

		log.verbose("To Stamp missing attributes before sending to RMS!");
		HashMap<String, String> quantityMap = new HashMap<String, String>();
		HashMap<String, String> itemUPCMap = new HashMap<String, String>();
		// To Stamp the shipment line quantities from the input document to the
		// output document as the Quantities will be zero when shipment has been
		// cancelled.

		YFCElement shipmentLinesElem = shipmentElem.getChildElement(AcademyConstants.ELE_SHIPMENT_LINES);
		if (shipmentLinesElem != null) {
			log.verbose("shipmentLinesElem:" + shipmentLinesElem.getString());
			YFCIterable<YFCElement> shipmentLineItr = shipmentLinesElem.getChildren(AcademyConstants.ELE_SHIPMENT_LINE);
			while (shipmentLineItr.hasNext()) {
				YFCElement shipmentLineElem = shipmentLineItr.next();
				quantityMap.put(shipmentLineElem.getAttribute(AcademyConstants.ATTR_ITEM_ID), shipmentLineElem.getAttribute(AcademyConstants.ATTR_ACTUAL_QUANTITY));
			}
		}

		// Stamp the quantities in the change shipment output document.
		if (quantityMap != null) {
			log.verbose("quantityMap:" + quantityMap.size());
			itemUPCMap = getItemUPCCode(env, quantityMap);
			stampDetailsInOutput(quantityMap, itemUPCMap, changeShipmentOut);
		}
	}

	/**
	 * Gets global item id of each and every item and returns a map with item id
	 * as the key with Global item ids as values.
	 * 
	 */

	private HashMap getItemUPCCode(YFSEnvironment env, HashMap quantityMap) throws Exception {

		HashMap<String, String> itemUPCMap = new HashMap<String, String>();
		Set keySet = quantityMap.keySet();

		log.verbose("keySet Size:" + keySet.size());

		YFCDocument getItemListInDoc = YFCDocument.createDocument(AcademyConstants.ELEM_ITEM);
		YFCElement complexQueryElem = getItemListInDoc.getDocumentElement().createChild(AcademyConstants.COMPLEX_QRY_ELEMENT);
		YFCElement orElem = complexQueryElem.createChild(AcademyConstants.COMPLEX_OR_ELEMENT);
		Iterator<String> itemIDItr = keySet.iterator();
		while (itemIDItr.hasNext()) {
			String itemID = itemIDItr.next();
			YFCElement expElem = orElem.createChild(AcademyConstants.COMPLEX_EXP_ELEMENT);
			expElem.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_ITEM_ID);
			expElem.setAttribute(AcademyConstants.ATTR_VALUE, itemID);
		}

		log.verbose("getItemUPCCode()_getItemListInDoc:" + XMLUtil.getString(getItemListInDoc.getDocument()));
		// Call getItemList API to fetch the global item ID details.
		Document docGetItemListTemplate = YFCDocument.getDocumentFor("<ItemList><Item ItemID='' GlobalItemID='' /></ItemList>").getDocument();
		env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, docGetItemListTemplate);
		Document getItemListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST, getItemListInDoc.getDocument());
		env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);

		Element ItemListElem = getItemListOutDoc.getDocumentElement();
		NodeList itemList = ItemListElem.getElementsByTagName(AcademyConstants.ELEM_ITEM);
		int itemListSize = itemList.getLength();
		for (int itemCounter = 0; itemCounter < itemListSize; itemCounter++) {
			Element itemElem = (Element) itemList.item(itemCounter);
			String globalItemID = itemElem.getAttribute(AcademyConstants.ATTR_GLOB_ITEM_ID);
			if (!YFCObject.isVoid(globalItemID)) {
				itemUPCMap.put(itemElem.getAttribute(AcademyConstants.ATTR_ITEM_ID), globalItemID);
			}
		}

		return itemUPCMap;
	}

	/**
	 * Updates quantity and itemUPC code in the message that needs to be sent to
	 * RMS.
	 * 
	 */

	private void stampDetailsInOutput(HashMap<String, String> quantityMap, HashMap<String, String> itemUPCMap, Document changeShipmentOut) {

		YFCDocument yfcChangeShipmentOutDoc = YFCDocument.getDocumentFor(changeShipmentOut);
		YFCElement shipmentElem = yfcChangeShipmentOutDoc.getDocumentElement();

		//Start of TL - 927
		// Modify the Shipment No without changing the no. of characters,for RMS
		// to consume the message.---- STL - 927
		String shipmentNo = shipmentElem.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		String truncatedShipmentNo = shipmentNo.substring(1);
		StringBuilder modifiedShipmentNo = new StringBuilder("C");
		modifiedShipmentNo.append(truncatedShipmentNo);
		shipmentElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, modifiedShipmentNo.toString());

		//End of TL - 927
		
		YFCElement shipmentLinesElem = shipmentElem.getChildElement(AcademyConstants.ELE_SHIPMENT_LINES);
		if (shipmentLinesElem != null) {
			YFCIterable<YFCElement> shipmentLineItr = shipmentLinesElem.getChildren(AcademyConstants.ELE_SHIPMENT_LINE);
			while (shipmentLineItr.hasNext()) {
				YFCElement shipmentLineElem = shipmentLineItr.next();
				String itemID = shipmentLineElem.getAttribute(AcademyConstants.ATTR_ITEM_ID);
				if (quantityMap.containsKey(itemID)) {
					shipmentLineElem.setAttribute(AcademyConstants.ATTR_ACTUAL_QUANTITY, quantityMap.get(itemID));
				}
				if (itemUPCMap.containsKey(itemID)) {
					shipmentLineElem.setAttribute(AcademyConstants.ATTR_GLOB_ITEM_ID, itemUPCMap.get(itemID));
				}
			}
			// This method is used to remove the Shipment line from the message
			// sent to RMS,for a shipment that is short picked.
			removeShipmentLineElem(changeShipmentOut);
		}
		log.verbose("stampDetailsInOutput()_yfcChangeShipmentOutDoc:" + yfcChangeShipmentOutDoc.getString());
	}

	private static Document removeShipmentLineElem(Document changeShipmentOut) {
		YFCDocument yfcChangeShipmentOut = YFCDocument.getDocumentFor(changeShipmentOut);
		YFCElement shipmentElem = yfcChangeShipmentOut.getDocumentElement();
		YFCElement shipmentLinesElem = shipmentElem.getChildElement(AcademyConstants.ELE_SHIPMENT_LINES);
		if (shipmentLinesElem != null) {
			YFCIterable<YFCElement> shipmentLineItr = shipmentLinesElem.getChildren(AcademyConstants.ELE_SHIPMENT_LINE);
			while (shipmentLineItr.hasNext()) {
				YFCElement shipmentLineElem = shipmentLineItr.next();
				double Quantity = Double.parseDouble(shipmentLineElem.getAttribute(AcademyConstants.ATTR_ACTUAL_QUANTITY));
				log.verbose("Quantity" + Quantity);
				if (Quantity == 0.0) {
					shipmentLinesElem.removeChild(shipmentLineElem);
				}
			}

		}
		log.verbose("Change shipment out is:" + yfcChangeShipmentOut.getString());
		return changeShipmentOut;
	}

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}
}
