package com.academy.ecommerce.sterling.sto;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.interfaces.api.AcademyGetItemDetailsAndUpdateInputAPI;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
public class StoShipment {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyGetItemDetailsAndUpdateInputAPI.class);

	public static YIFApi api = null;

	{
		try {
			api = YIFClientFactory.getInstance().getApi();
		} catch (YIFClientCreationException e) {
			e.printStackTrace();
		}

	}

	public void itemTypeList(YFSEnvironment env, Document inDoc) throws Exception {

		Element rootele = inDoc.getDocumentElement();
		String enterpriseCode = rootele.getAttribute("EnterpriseCode");
		String buyerName = rootele.getAttribute("BuyerName");
		String shipNode = rootele.getAttribute("ShipNode");

		String receivingNode = rootele.getAttribute("ReceivingNode");

		String sellerOrganizationCode = rootele.getAttribute("SellerOrganizationCode");

		/* for conveyable items */

		Document conDoc = XMLUtil.createDocument("Shipment");
		Element eleConDoc = conDoc.getDocumentElement();
		eleConDoc.setAttribute("DocumentType", "0006");
		eleConDoc.setAttribute("EnterpriseCode", enterpriseCode);
		eleConDoc.setAttribute("BuyerName", buyerName);
		eleConDoc.setAttribute("ShipNode", shipNode);
		eleConDoc.setAttribute("ReceivingNode", receivingNode);
		eleConDoc.setAttribute("SellerOrganizationCode", sellerOrganizationCode);
		Element shipmentLines = conDoc.createElement("ShipmentLines");
		eleConDoc.appendChild(shipmentLines);

		/* for non conveyable items */

		Document bulkDoc = XMLUtil.createDocument("Shipment");
		Element eleBulkDoc = bulkDoc.getDocumentElement();
		eleBulkDoc.setAttribute("DocumentType", "0006");
		eleBulkDoc.setAttribute("EnterpriseCode", enterpriseCode);
		eleBulkDoc.setAttribute("BuyerName", buyerName);
		eleBulkDoc.setAttribute("ShipNode", shipNode);
		eleBulkDoc.setAttribute("ReceivingNode", receivingNode);
		eleBulkDoc.setAttribute("SellerOrganizationCode", sellerOrganizationCode);
		Element shipmentLinesBulk = bulkDoc.createElement("ShipmentLines");
		eleBulkDoc.appendChild(shipmentLinesBulk);

		NodeList getItemList = inDoc.getElementsByTagName("ShipmentLine");
		int l = getItemList.getLength();
		String itemID = null;
		String quantity = null;
		for (int k = 0; k < l; k++) {
			Element getShipmentLineListElement = (Element) getItemList.item(k);

			itemID = getShipmentLineListElement.getAttribute("ItemID");

			quantity = getShipmentLineListElement.getAttribute("Quantity");

			String shipmentType = callShipmentGroupID(env, itemID, enterpriseCode);
			log.verbose("ShipmentType " + shipmentType);

			if (shipmentType.equals("BLP")) {

				Element shipmentLine1 = bulkDoc.createElement("ShipmentLine");
				shipmentLine1.setAttribute("ProductClass", "GOOD");
				shipmentLine1.setAttribute("UnitOfMeasure", "EACH");
				shipmentLine1.setAttribute("ItemID", itemID);
				shipmentLine1.setAttribute("Quantity", quantity);
				shipmentLine1.setAttribute("shipmentType", "BULK");
				shipmentLinesBulk.appendChild(shipmentLine1);

			} else {

				Element shipmentLine = conDoc.createElement("ShipmentLine");
				shipmentLine.setAttribute("ProductClass", "GOOD");
				shipmentLine.setAttribute("UnitOfMeasure", "EACH");
				shipmentLine.setAttribute("ItemID", itemID);
				shipmentLine.setAttribute("Quantity", quantity);
				shipmentLine.setAttribute("shipmentType", "CON");
				shipmentLines.appendChild(shipmentLine);
			}

		}
		AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_STO_SHIPMENT, conDoc);
		AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_STO_SHIPMENT, bulkDoc);
		log.verbose("*** Input XML for the STO creation *** ");
		log.verbose("*** printing inDoc *** " + XMLUtil.getXMLString(conDoc));
		log.verbose("*** printing inDoc *** " + XMLUtil.getXMLString(bulkDoc));

	}

	public String callShipmentGroupID(YFSEnvironment env, String itemID, String enterpriseCode) throws Exception {

		Document getItemDetails = XMLUtil.createDocument("Item");
		Element eleGetItemDetails = getItemDetails.getDocumentElement();
		eleGetItemDetails.setAttribute("ItemID", itemID);
		eleGetItemDetails.setAttribute("OrganizationCode", enterpriseCode);
		eleGetItemDetails.setAttribute("UnitOfMeasure", "EACH");
		log.verbose(" getItemDetails input " + XMLUtil.getXMLString(getItemDetails));

		Document getItemDetailsTemplate = XMLUtil.getDocument("<Item >" + "<ClassificationCodes  StorageType=\"\" >" + "</ClassificationCodes>" + "</Item>");
		env.setApiTemplate("getItemDetails", getItemDetailsTemplate);
		Document getItemDetailsOutput = api.getItemDetails(env, getItemDetails);
		env.clearApiTemplates();

		Element ClassificationCodes = (Element) getItemDetailsOutput.getElementsByTagName("ClassificationCodes").item(0);

		String storageType = ClassificationCodes.getAttribute("StorageType");
		String shipmentType = null;
		if ((storageType.equals("NCONLP")) || (storageType.equals("NCONNLP"))) {
			shipmentType = "BLP";
			log.verbose("inside BULK");

		} else {
			shipmentType = "CON";
		}
		return shipmentType;

	}
}
