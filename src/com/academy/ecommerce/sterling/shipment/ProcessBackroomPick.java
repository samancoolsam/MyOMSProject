//package declaration
package com.academy.ecommerce.sterling.shipment;

//import statements
//java util import statements
import java.util.Properties;

//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//academy import statements
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;

//yantra import statements
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.log.YFCLogCategory;

public class ProcessBackroomPick implements YIFCustomApi
{
	private static final YFCLogCategory	log	= YFCLogCategory.instance(ProcessBackroomPick.class);

	private static YIFApi api= null;

	static
	{
		try
		{
			api = YIFClientFactory.getInstance().getApi();
		}
		catch (YIFClientCreationException e)
		{
			e.printStackTrace();
		}
	}

	private Properties props;

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

		boolean isInventoryShortage = false;
		boolean isPickComplete = false;
		boolean itemPicked = false;
		Element inXMLRoot = inXML.getDocumentElement();

		if(inXMLRoot.getAttribute("IsCompletePick").equals("Y"))
		{
			isPickComplete = true;
		}

		if(inXMLRoot.getAttribute("radioSelection").equals("InventoryShortage"))
		{
			isInventoryShortage = true;
		}

		Document multiApiDoc = XMLUtil.createDocument("MultiApi");
		Element multiApiDocRoot = multiApiDoc.getDocumentElement();

		Element changeShipmentRoot = multiApiDoc.createElement("Shipment");
		multiApiDocRoot.appendChild(changeShipmentRoot);
		changeShipmentRoot.setAttribute("ShipmentKey", inXMLRoot.getAttribute("ShipmentKey"));

		Element shipmentLines = multiApiDoc.createElement("ShipmentLines");
		changeShipmentRoot.appendChild(shipmentLines);

		Element containers = multiApiDoc.createElement("Containers");

		Element container = multiApiDoc.createElement("Container");
		containers.appendChild(container);

		Element containerDetails = multiApiDoc.createElement("ContainerDetails");
		container.appendChild(containerDetails);

		NodeList shipmentLineList = inXMLRoot.getElementsByTagName("ShipmentLine");

		Element inventorySnapShotRoot = null;
		Element inventorySnapShotShipNode = null;

		if(isInventoryShortage)
		{
			changeShipmentRoot.setAttribute("Action", "Cancel");
			changeShipmentRoot.setAttribute("BackOrderRemovedQuantity", "Y");

			inventorySnapShotRoot = multiApiDoc.createElement("InventorySnapShot");
			inventorySnapShotRoot.setAttribute("ApplyDifferences", "Y");

			inventorySnapShotShipNode = multiApiDoc.createElement("ShipNode");
			inventorySnapShotRoot.appendChild(inventorySnapShotShipNode);
			inventorySnapShotShipNode.setAttribute("ShipNode", inXMLRoot.getAttribute("ShipNode"));
		}

		for (int i = 0; i < shipmentLineList.getLength(); i++)
		{
			Element shipmentLine = (Element) shipmentLineList.item(i);
			double quantity = Double.parseDouble(shipmentLine.getAttribute("Quantity"));
			double backroomPickedQuantity = 0;
			double pickedQuantity = 0;
			double shortageQuantity = 0;

			if(!shipmentLine.getAttribute("ShortageQuantity").trim().equals(""))
			{
				shortageQuantity = Double.parseDouble(shipmentLine.getAttribute("ShortageQuantity"));
			}

			if(!shipmentLine.getAttribute("BackroomPickedQuantity").trim().equals(""))
			{
				backroomPickedQuantity = Double.parseDouble(shipmentLine.getAttribute("BackroomPickedQuantity"));
			}

			if(!shipmentLine.getAttribute("PickedQuantity").trim().equals(""))
			{
				pickedQuantity = Double.parseDouble(shipmentLine.getAttribute("PickedQuantity"));
			}

			if(pickedQuantity == 0)
			{
				if(!shipmentLine.getAttribute("PickedQuantity1").trim().equals(""))
				{
					pickedQuantity = Double.parseDouble(shipmentLine.getAttribute("PickedQuantity1"));
				}
			}

			double newBackroomPickedQuantity = backroomPickedQuantity + pickedQuantity;

			Element changeShipmentLine = multiApiDoc.createElement("ShipmentLine");
			shipmentLines.appendChild(changeShipmentLine);

			changeShipmentLine.setAttribute("ShipmentLineKey", shipmentLine.getAttribute("ShipmentLineKey"));
			changeShipmentLine.setAttribute("BackroomPickedQuantity", ""+newBackroomPickedQuantity);

			if(pickedQuantity>0)
			{
				itemPicked = true;
				Element containerDetail = multiApiDoc.createElement("ContainerDetail");
				containerDetails.appendChild(containerDetail);
				containerDetail.setAttribute("ShipmentLineKey",shipmentLine.getAttribute("ShipmentLineKey"));
				containerDetail.setAttribute("Quantity",""+pickedQuantity);
			}

			if(isInventoryShortage)
			{
				changeShipmentLine.setAttribute("Quantity", ""+newBackroomPickedQuantity);

				if(newBackroomPickedQuantity>0)
				{
					changeShipmentRoot.setAttribute("Action", "");
					isPickComplete = true;
				}

				if(shortageQuantity>0)
				{
					Element item = (Element)shipmentLine.getElementsByTagName("Item").item(0);

					Element inventorySnapShotItem = multiApiDoc.createElement("Item");
					inventorySnapShotShipNode.appendChild(inventorySnapShotItem);
					inventorySnapShotItem.setAttribute("UnitOfMeasure", item.getAttribute("UnitOfMeasure"));
					inventorySnapShotItem.setAttribute("ProductClass", item.getAttribute("ProductClass"));
					inventorySnapShotItem.setAttribute("ItemID", item.getAttribute("ItemID"));
					inventorySnapShotItem.setAttribute("InventoryOrganizationCode", "DEFAULT");

					Element inventorySnapShotSupplyDetails = multiApiDoc.createElement("SupplyDetails");
					inventorySnapShotItem.appendChild(inventorySnapShotSupplyDetails);
					inventorySnapShotSupplyDetails.setAttribute("SupplyType", "ONHAND");
					inventorySnapShotSupplyDetails.setAttribute("Quantity", "0");
				}
			}
		}

		String containerScm = "";

		if(itemPicked)
		{
			containerScm = getContainerScm(env);
			setContainerVolumeDetails(env, inXMLRoot, container);

			container.setAttribute("SCAC",inXMLRoot.getAttribute("SCAC"));
			container.setAttribute("CarrierServiceCode",inXMLRoot.getAttribute("CarrierServiceCode"));
			container.setAttribute("ContainerGrossWeight",inXMLRoot.getAttribute("ContainerGrossWeight"));
			container.setAttribute("ContainerNetWeight",inXMLRoot.getAttribute("ContainerGrossWeight"));
			container.setAttribute("ContainerType","Case");
			container.setAttribute("IsPackProcessComplete","Y");

			container.setAttribute("ContainerScm",containerScm);

			Document multiApiDoc2 = XMLUtil.createDocument("Shipment");
			Element multiApiDocRoot2 = multiApiDoc2.getDocumentElement();
			multiApiDocRoot2.appendChild(multiApiDoc2.importNode(containers, true));
			multiApiDocRoot2.setAttribute("ShipmentKey", inXMLRoot.getAttribute("ShipmentKey"));

			log.verbose("ProcessBackroomPick: performBackroomPick: containers: " + XMLUtil.getXMLString(multiApiDoc2));
			changeShipmentRoot.appendChild(containers);
		}

		includeInMultiApi(multiApiDoc, changeShipmentRoot, "changeShipment");

		if(isPickComplete)
		{
			Element changeShipmentStatusRoot = multiApiDoc.createElement("Shipment");
			changeShipmentStatusRoot.setAttribute("ShipmentKey", inXMLRoot.getAttribute("ShipmentKey"));
			changeShipmentStatusRoot.setAttribute("TransactionId", "SOP_BACKROOM_PICK");

			includeInMultiApi(multiApiDoc, changeShipmentStatusRoot, "changeShipmentStatus");
		}

		if(isInventoryShortage)
		{
			includeInMultiApi(multiApiDoc, inventorySnapShotRoot, "getInventoryMismatch");
		}

		if (log.isVerboseEnabled())
		{
			log.verbose("ProcessBackroomPick: performBackroomPick: multiApiDoc: " + XMLUtil.getXMLString(multiApiDoc));
		}

		env.setApiTemplate("changeShipmentStatus", YFCDocument.getDocumentFor("<Shipment ShipmentNo=\"\" ShipmentType=\"\" ShipmentKey=\"\"/>").getDocument());
		env.setApiTemplate("changeShipment", YFCDocument.getDocumentFor("<Shipment ShipNode=\"\" ShipmentNo=\"\" ShipmentType=\"\" ShipmentKey=\"\"><Containers><Container ShipmentContainerKey=\"\" ContainerScm=\"\"/></Containers></Shipment>").getDocument());

		Document multiApiOutputDoc = AcademyUtil.invokeService(env, "AcademySFSProcessBackroomMultiApi", multiApiDoc);
		//Document multiApiOutputDoc = api.multiApi(env, multiApiDoc);

		if (log.isVerboseEnabled())
		{
			log.verbose("ProcessBackroomPick: performBackroomPick: multiApiOutputDoc: " + XMLUtil.getXMLString(multiApiOutputDoc));
		}

		String shipmentContainerKey = "";

		if(!containerScm.equals(""))
		{
			NodeList createdContainerList = multiApiOutputDoc.getElementsByTagName("Container");
			Element createdContainer = null;

			for (int i = 0; i < createdContainerList.getLength(); i++)
			{
				createdContainer = (Element) createdContainerList.item(i);
				String newContainerScm = createdContainer.getAttribute("ContainerScm").trim();

				if(newContainerScm.equals(containerScm))
				{
					shipmentContainerKey = createdContainer.getAttribute("ShipmentContainerKey").trim();
					break;
				}
			}

			Element shipment = (Element)createdContainer.getParentNode().getParentNode();
			String strShipmentType = shipment.getAttribute("ShipmentType");
			String shipNode = shipment.getAttribute("ShipNode");

			if(strShipmentType.equals("CON") || strShipmentType.equals("CONOVNT") || strShipmentType.equals("GC") || strShipmentType.equalsIgnoreCase(AcademyConstants.STR_GC_ONLY_SHIP_TYPE))
			{
				processContainerDetailsForNonBulk(env, shipmentContainerKey, shipNode) ;
			}
			else
			{
				processContainerDetailsForBulk(env, shipmentContainerKey, shipNode) ;
			}
		}
	}

	public void setContainerVolumeDetails(YFSEnvironment env, Element inXMLRoot, Element container) throws Exception
	{
		String containerItemID = inXMLRoot.getAttribute("ContainerType").trim();
		String containerItemKey = inXMLRoot.getAttribute("ContainerTypeKey").trim();

		Document docItemListInput = XMLUtil.createDocument("Item");
		Element docItemListInputRoot = docItemListInput.getDocumentElement();
		docItemListInputRoot.setAttribute("MaximumRecords","1");

		if(!containerItemKey.equals(""))
		{
			docItemListInputRoot.setAttribute("ItemKey",containerItemKey);
		}
		else
		{
			docItemListInputRoot.setAttribute("ItemID",containerItemID);
		}

		Document docItemListOutput = AcademyUtil.invokeService(env, "AcademySFSGetContainerVolumeDetails", docItemListInput);
		Element containerVolumeItem = (Element)docItemListOutput.getElementsByTagName("Item").item(0);
		Element containerVolumePrimaryInformation = (Element)docItemListOutput.getElementsByTagName("PrimaryInformation").item(0);

		container.setAttribute("CorrugationItemKey",containerVolumeItem.getAttribute("ItemKey"));

		container.setAttribute("ContainerHeight",containerVolumePrimaryInformation.getAttribute("UnitHeight"));
		container.setAttribute("ContainerHeightUOM",containerVolumePrimaryInformation.getAttribute("UnitHeightUOM"));
		container.setAttribute("ContainerLength",containerVolumePrimaryInformation.getAttribute("UnitLength"));
		container.setAttribute("ContainerLengthUOM",containerVolumePrimaryInformation.getAttribute("UnitLengthUOM"));
		container.setAttribute("ContainerWidth",containerVolumePrimaryInformation.getAttribute("UnitWidth"));
		container.setAttribute("ContainerWidthUOM",containerVolumePrimaryInformation.getAttribute("UnitWidthUOM"));
		container.setAttribute("ContainerGrossWeightUOM",containerVolumePrimaryInformation.getAttribute("UnitWeightUOM"));
		container.setAttribute("ContainerNetWeightUOM",containerVolumePrimaryInformation.getAttribute("UnitWeightUOM"));
	}

	public String getContainerScm(YFSEnvironment env) throws Exception
	{
		Document generateSCMInput = XMLUtil.createDocument("generateSCM");
		Element generateSCMInputRoot = generateSCMInput.getDocumentElement();
		generateSCMInputRoot.setAttribute("ContainerType","Case");
		generateSCMInputRoot.setAttribute("NumScmsRequested","1");
		Document generateSCMOutput = api.generateSCM(env, generateSCMInput);
		return ((Element)generateSCMOutput.getElementsByTagName("SCM").item(0)).getAttribute("SCM");
	}

	public void processContainerDetailsForNonBulk(YFSEnvironment env, String shipmentContainerKey, String shipNode) throws Exception
	{
		Document getShipmentContainerDetailsOutputDoc  = getContainerDetailsForNonBulk(env, shipmentContainerKey);
		Element getShipmentContainerDetailsOutputDocRoot = getShipmentContainerDetailsOutputDoc.getDocumentElement();
		getShipmentContainerDetailsOutputDocRoot.setAttribute("PrintBatchPackSlip","Y");
		getShipmentContainerDetailsOutputDocRoot.setAttribute("PrintBatchShippingLabel","Y");
		getShipmentContainerDetailsOutputDocRoot.setAttribute("IsOnboardedStore","Y");

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

		AcademyUtil.invokeService(env, "AcademySFSProcessNonBulkContainer", raiseEventInputDoc);
	}

	public boolean getBooleanPropertyValue(String property)
	{
		String propertyValue = props.getProperty(property);

		if(propertyValue != null && propertyValue.equalsIgnoreCase("Y"))
		{
			return true;
		}

		return false;
	}

	public void processContainerDetailsForBulk(YFSEnvironment env, String shipmentContainerKey, String shipNode) throws Exception
	{
		boolean addToManifestFirst = getBooleanPropertyValue("ADD_TO_MANIFEST_FIRST");
		boolean skipAddToManifest = getBooleanPropertyValue("SKIP_ADD_TO_MANIFEST");

		if(addToManifestFirst && !skipAddToManifest)
		{
			addContainerToManifestForBulk(env, shipmentContainerKey, shipNode);
		}

		Document getShipmentContainerDetailsOutputDoc  = getContainerDetailsForBulk(env, shipmentContainerKey);
		Element getShipmentContainerDetailsOutputDocRoot = getShipmentContainerDetailsOutputDoc.getDocumentElement();

		Document docContainerList = XMLUtil.createDocument("ContainerList");
		Element docContainerListRoot = docContainerList.getDocumentElement();
		Element eleContainer = (Element) docContainerList.importNode(getShipmentContainerDetailsOutputDocRoot, true);
		docContainerListRoot.appendChild(eleContainer);

		docContainerListRoot.setAttribute("IsOnboardedStore","Y");
		eleContainer.setAttribute("IsOnboardedStore","Y");

		if (log.isVerboseEnabled())
		{
			log.verbose("ProcessBackroomPick: performBackroomPick: getShipmentContainerDetailsOutputDoc: " + XMLUtil.getXMLString(getShipmentContainerDetailsOutputDoc));
		}

		//String raiseEventPart1 = "<RaiseEvent TransactionId=\"PRINT_JASPER\" EventId=\"ON_SUCCESS\"><DataType>1</DataType><XMLData><![CDATA[ ";
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

		AcademyUtil.invokeService(env, "AcademySFSProcessBulkContainer", raiseEventInputDoc);

		if(!addToManifestFirst && !skipAddToManifest)
		{
			addContainerToManifestForBulk(env, shipmentContainerKey, shipNode);
		}
	}

	public void addContainerToManifestForBulk(YFSEnvironment env, String shipmentContainerKey, String shipNode) throws Exception
	{
		Document getShipmentContainerDetailsOutputDoc  = getContainerDetailsForNonBulk(env, shipmentContainerKey);
		Element getShipmentContainerDetailsOutputDocRoot = getShipmentContainerDetailsOutputDoc.getDocumentElement();
		getShipmentContainerDetailsOutputDocRoot.setAttribute("IsOnboardedStore","Y");

		if (log.isVerboseEnabled())
		{
			log.verbose("ProcessBackroomPick: performBackroomPick: getShipmentContainerDetailsOutputDoc: " + XMLUtil.getXMLString(getShipmentContainerDetailsOutputDoc));
		}

		String raiseEventPart1 = "<RaiseEvent TransactionId=\"VERIFY_PACK\" EventId=\"VERIFICATION_DONE\"><DataType>1</DataType><XMLData><![CDATA[ ";
		String raiseEventPart2 = " ]]></XMLData></RaiseEvent>";
		Document raiseEventInputDoc = YFCDocument.getDocumentFor(raiseEventPart1 + XMLUtil.getXMLString(getShipmentContainerDetailsOutputDoc) + raiseEventPart2).getDocument();
		Element raiseEventInputDocRoot = raiseEventInputDoc.getDocumentElement();
		raiseEventInputDocRoot.setAttribute("ShipNode", shipNode);
		raiseEventInputDocRoot.setAttribute("IsOnboardedStore", "Y");

		if (log.isVerboseEnabled())
		{
			log.verbose("ProcessBackroomPick: performBackroomPick: raiseEventInputDoc: " + XMLUtil.getXMLString(raiseEventInputDoc));
		}

		AcademyUtil.invokeService(env, "AcademySFSProcessBulkContainerAddToManifest", raiseEventInputDoc);
	}

	public Document getContainerDetailsForNonBulk(YFSEnvironment env, Document inXML) throws Exception
	{
		return getContainerDetailsForNonBulk(env, inXML.getDocumentElement().getAttribute("ShipmentContainerKey"));
	}

	public Document getContainerDetailsForNonBulk(YFSEnvironment env, String shipmentContainerKey) throws Exception
	{
		env.setApiTemplate("getShipmentContainerDetails", "global/template/event/ADD_TO_CONTAINER.ON_CONTAINER_PACK_PROCESS_COMPLETE.xml");

		Document getShipmentContainerDetailsInputDoc = XMLUtil.createDocument("Container");
		Element getShipmentContainerDetailsInputDocRoot = getShipmentContainerDetailsInputDoc.getDocumentElement();
		getShipmentContainerDetailsInputDocRoot.setAttribute("ShipmentContainerKey",shipmentContainerKey);

		if (log.isVerboseEnabled())
		{
			log.verbose("ProcessBackroomPick: performBackroomPick: getShipmentContainerDetailsInputDoc: " + XMLUtil.getXMLString(getShipmentContainerDetailsInputDoc));
		}

		return api.getShipmentContainerDetails(env, getShipmentContainerDetailsInputDoc);
	}

	public Document getContainerDetailsForBulk(YFSEnvironment env, Document inXML) throws Exception
	{
		return getContainerDetailsForBulk(env, inXML.getDocumentElement().getAttribute("ShipmentContainerKey"));
	}

	public Document getContainerDetailsForBulk(YFSEnvironment env, String shipmentContainerKey) throws Exception
	{
		YFCDocument templateTemp = YFCDocument.parse(this.getClass().getResourceAsStream("/global/template/event/CONTAINERIZE_WAVE.ON_SUCCESS.xml") );
		Element templateTempRoot = templateTemp.getDocument().getDocumentElement();
		Document getShipmentContainerDetailsTemplate = XMLUtil.getDocumentForElement((Element)templateTempRoot.getElementsByTagName("Container").item(0));

		env.setApiTemplate("getShipmentContainerDetails", getShipmentContainerDetailsTemplate);

		Document getShipmentContainerDetailsInputDoc = XMLUtil.createDocument("Container");
		Element getShipmentContainerDetailsInputDocRoot = getShipmentContainerDetailsInputDoc.getDocumentElement();
		getShipmentContainerDetailsInputDocRoot.setAttribute("ShipmentContainerKey",shipmentContainerKey);

		if (log.isVerboseEnabled())
		{
			log.verbose("ProcessBackroomPick: performBackroomPick: getShipmentContainerDetailsInputDoc: " + XMLUtil.getXMLString(getShipmentContainerDetailsInputDoc));
		}

		return api.getShipmentContainerDetails(env, getShipmentContainerDetailsInputDoc);
	}

	private static void includeInMultiApi(Document multiApiDoc, Element apiInput, String apiName)
	{
		Element multiApiDocRoot = multiApiDoc.getDocumentElement();
		Element api = multiApiDoc.createElement("API");
		multiApiDocRoot.appendChild(api);
		api.setAttribute("Name", apiName);

		Element input = multiApiDoc.createElement("Input");
		api.appendChild(input);

		input.appendChild(apiInput);
	}
}