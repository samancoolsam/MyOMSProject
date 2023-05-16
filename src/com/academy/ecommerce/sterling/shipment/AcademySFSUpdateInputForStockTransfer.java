
package com.academy.ecommerce.sterling.shipment;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class prepares the input for StockTransfer message service
 * on adding containers to manifest.
 * 
 * @author <a href="mailto:pramod.p@cognizant.com">Pramod P</a>
 * @author <a href="mailto:KaunshikN.Sanji@cognizant.com">Kaushik N</a>
 */
public class AcademySFSUpdateInputForStockTransfer
{
	public static final String CLASS_NAME = AcademySFSUpdateInputForStockTransfer.class.getName();
	private static YFCLogCategory log = YFCLogCategory.instance(AcademySFSUpdateInputForStockTransfer.class);
	
	/**
	 * Method that prepares and returns the input for Stock Transfer message service.
	 * @param env
	 * @param inXML
	 * 			<br/> - Input XML from AcademyAddContainerToManifest service
	 * 			{@code
	 * 				<Container>
	 * 					<SpecialServices/>
	 * 					<AdditionalAttributes/>
	 * 					<ContainerDetails>
	 * 						<ContainerDetail>
	 * 							<ShipmentTagSerials/>
	 * 							<ShipmentLine/>
	 * 						</ContainerDetail>
	 * 						.
	 * 						.
	 * 					</ContainerDetails>
	 * 					<Shipment>
	 * 						<ToAddress/>
	 * 						<ShipNode>
	 * 							<ShipNodePersonInfo/>
	 * 						</ShipNode>
	 * 						<ScacAndService/>
	 * 					</Shipment>
	 * 				</Container>
	 * 			}
	 * @return Document
	 * 			<br/> - Input for Stock Transfer message service
	 * 			{@code
	 * 				<Shipment ShipNode="" ShipmentNo="">
	 * 					<ShipmentLines>
	 * 						<ShipmentLine ItemID="" ProductClass="GOOD"
	 * 							Quantity="" UnitOfMeasure="EACH">
	 * 							<OrderLine>
	 * 								<LinePriceInfo UnitPrice=""/>
	 * 							</OrderLine>
	 * 							<Item GlobalItemID=""/>
	 * 						</ShipmentLine>
	 * 						.
	 * 						.
	 * 						.
	 * 					</ShipmentLines>
	 * 				</Shipment>
	 * 			}
	 * @throws Exception
	 */
	public Document prepareInputForStockTransfer(YFSEnvironment env, Document inXML) throws Exception
	{	
		log.debug(CLASS_NAME+" :prepareInputForStockTransfer:Entry");
		if (log.isVerboseEnabled())
			log.verbose("\nInput XML to AcademySFSUpdateInputForStockTransfer API:\n"+YFCDocument.getDocumentFor(inXML));

		// Variable declarations
		Document outXML = null;
		Element eleShipment = null;
		Element eleShipmentLines = null;

		// Create Shipment Element
		outXML = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleShipment = outXML.getDocumentElement();
		eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, ((Element) inXML.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0)).getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
		eleShipment.setAttribute(AcademyConstants.SHIP_NODE, ((Element) inXML.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0)).getAttribute(AcademyConstants.SHIP_NODE));
		
		// Create ShipmentLines Element and appending it to shipment node
		eleShipmentLines = outXML.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
		eleShipment.appendChild(eleShipmentLines);
		
		NodeList containerDetailNL = inXML.getElementsByTagName(AcademyConstants.CONTAINER_DETL_ELEMENT);
		
		// Prepare getItemList inputDoc
		Document getItemListInputDoc = prepareGetItemListInput(containerDetailNL);

		// Call getItemList API
		Document getItemListOutputDoc = callGetItemListAPI(env, getItemListInputDoc);
		
		prepareShipmentLineElements(outXML, eleShipmentLines, containerDetailNL, getItemListOutputDoc);
		
		if (log.isVerboseEnabled())
			log.verbose("\nOutput XML of AcademySFSUpdateInputForStockTransfer API:\n"+YFCDocument.getDocumentFor(outXML));
		log.debug(CLASS_NAME+" :prepareInputForStockTransfer:Exit");
		return outXML;
	}

	/**
	 * Method to prepare the ShipmentLine Elements for the output
	 * @param outXML
	 * 			<br/> - Output document of the service
	 * @param eleShipmentLines
	 * 			<br/> - ShipmentLines Element of outXML
	 * @param containerDetailNL
	 * 			<br/> - NodeList of ContainerDetail Elements from the inXML
	 * @param getItemListOutputDoc
	 * 			<br/> - Output of getItemList API call
	 * @throws XPathExpressionException
	 */
	private void prepareShipmentLineElements(Document outXML, Element eleShipmentLines, 
			NodeList containerDetailNL, Document getItemListOutputDoc) throws XPathExpressionException {
		log.debug(CLASS_NAME+" :prepareShipmentLineElements:Entry");
		
		for(int listIndex=0; listIndex<containerDetailNL.getLength(); listIndex++){
			Element containerDtlElement = (Element)containerDetailNL.item(listIndex);
			String itemID = containerDtlElement.getAttribute(AcademyConstants.ATTR_ITEM_ID);
			
			// Create Element ShipmentLine
			Element eleShipmentLine = outXML.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_ITEM_ID, itemID);
			
			// Get the attribute Quantity and stamp in OutXML
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, containerDtlElement.getAttribute(AcademyConstants.ATTR_QUANTITY));
			double doubleItemQty = Double.parseDouble(containerDtlElement.getAttribute(AcademyConstants.ATTR_QUANTITY));
			
			// Stamp UnitOfMeasure and ProductClass in OutXML
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_UOM, containerDtlElement.getAttribute(AcademyConstants.ATTR_UOM));
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_PROD_CLASS, containerDtlElement.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
			
			String globalItemIdXpathStr = (new StringBuilder()).append(AcademyConstants.ITEMLIST_ITEM_ELE_XPATH)
											.append("[@").append(AcademyConstants.ATTR_ITEM_ID)
											.append("='").append(itemID).append("']/@")
											.append(AcademyConstants.ATTR_GLOB_ITEM_ID).toString();
			String unitCostXpathStr = (new StringBuilder()).append(AcademyConstants.ITEMLIST_ITEM_ELE_XPATH)
											.append("[@").append(AcademyConstants.ATTR_ITEM_ID)
											.append("='").append(itemID).append("']/")
											.append(AcademyConstants.ELE_PRIMARY_INFO)
											.append("/@").append(AcademyConstants.ATTR_UNIT_COST).toString();
			
			Element eleShipmentLineItem = outXML.createElement(AcademyConstants.ITEM);
			eleShipmentLineItem.setAttribute(AcademyConstants.ATTR_GLOB_ITEM_ID, 
						XMLUtil.getAttributeFromXPath(getItemListOutputDoc, globalItemIdXpathStr));
			
			Element eleOrderLineElm = outXML.createElement(AcademyConstants.ELEM_ORDER_LINE);
			Element eleOrderLineItemLPInfoElm = outXML.createElement(AcademyConstants.ELE_LINEPRICE_INFO);
			String unitCost = XMLUtil.getAttributeFromXPath(getItemListOutputDoc, unitCostXpathStr);
			// Calculating unitprice by multiplying quantity*unit cost
			Double unitPrice = (Double.parseDouble(unitCost)) * doubleItemQty;
			eleOrderLineItemLPInfoElm.setAttribute(AcademyConstants.ATTR_UNIT_PRICE, String.valueOf(unitPrice));
			
			eleOrderLineElm.appendChild(eleOrderLineItemLPInfoElm);
			eleShipmentLine.appendChild(eleOrderLineElm);
			eleShipmentLine.appendChild(eleShipmentLineItem);
			
			eleShipmentLines.appendChild(eleShipmentLine);
		}
		
		log.debug(CLASS_NAME+" :prepareShipmentLineElements:Exit");
	}

	/**
	 * Calling getItemList API to get attributes GlobalItemID and UnitCost
	 * 
	 * @param env
	 * @param getItemListInputDoc
	 * 			<br/> - Input document for getItemList API 
	 * @return Document
	 * 			<br/> - Output of getItemList API call
	 * 			{@code
	 * 				<ItemList>
	 * 					<Item ItemID="" GlobalItemID="" >
	 * 						<PrimaryInformation  UnitCost="" />
	 * 					</Item>
	 * 					.
	 * 					.
	 * 					.
	 * 				</ItemList>
	 * 			}
	 * @throws Exception
	 */
	private Document callGetItemListAPI(YFSEnvironment env, Document getItemListInputDoc) throws Exception
	{
		log.debug(CLASS_NAME+" :callGetItemListAPI:Entry");
		
		// Prepare getItemList output template
		Document getItemListOutputTemplate = YFCDocument.getDocumentFor("<ItemList> <Item ItemID='' GlobalItemID='' ><PrimaryInformation  UnitCost='' /></Item> </ItemList>").getDocument();
		env.setApiTemplate("getItemList", getItemListOutputTemplate);
		// Calling getItemList API
		Document getItemListOutputDoc = AcademyUtil.invokeAPI(env, "getItemList", getItemListInputDoc);
		// Clearing API template
		env.clearApiTemplate("getItemList");
		
		if (log.isVerboseEnabled())
			log.verbose("\nOutput XML of getItemList API Call:\n"+(getItemListOutputDoc!=null?YFCDocument.getDocumentFor(getItemListOutputDoc):"NULL"));
		log.debug(CLASS_NAME+" :callGetItemListAPI:Exit");
		return getItemListOutputDoc;
	}

	/**
	 * Method that prepares and returns the Input document for 
	 * getItemList API call
	 *  
	 * @param containerDetailNL
	 * 			<br/> - NodeList of ContainerDetail Elements from the inXML
	 * @return Document
	 * 			<br/> - Input document for getItemList API
	 *			{@code
	 *				<!-- when only one item is picked -->
	 *				<Item OrganizationCode="DEFAULT" UnitOfMeasure="EACH" ItemID="" />
	 *
	 *				<!-- when more than one item is picked -->
	 *				<Item OrganizationCode="DEFAULT" UnitOfMeasure="EACH">
	 *					<ComplexQuery Operator="AND">
	 *						<And>
	 *							<Or>
	 *								<Exp Name="ItemID" Value="0018459842"/>
	 *								<Exp Name="ItemID" Value=""/>
	 *								.
	 *								.
	 *							</Or>
	 *						</And>
	 *					</ComplexQuery>
	 *				</Item>
	 *			} 
	 * @throws ParserConfigurationException
	 */
	private Document prepareGetItemListInput(NodeList containerDetailNL) throws ParserConfigurationException
	{
		log.debug(CLASS_NAME+" :prepareGetItemListInput:Entry");
		
		Document getItemListInputDoc = XMLUtil.createDocument(AcademyConstants.ITEM);
		Element rootElement = getItemListInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyConstants.ORGANIZATION_CODE, AcademyConstants.HUB_CODE);
		rootElement.setAttribute(AcademyConstants.ATTR_UOM, AcademyConstants.UOM_EACH_VAL);
		
		if(containerDetailNL.getLength() == 0){
			//avoiding ComplexQuery element if only one item picked to be manifested
			Element containerDtlElement = (Element)containerDetailNL.item(0);
			rootElement.setAttribute(AcademyConstants.ATTR_ITEM_ID, containerDtlElement.getAttribute(AcademyConstants.ATTR_ITEM_ID));
		}else{
			//creating ComplexQuery element if more than one item is picked
			Element complexQryElement = getItemListInputDoc.createElement(AcademyConstants.COMPLEX_QRY_ELEMENT);
			complexQryElement.setAttribute(AcademyConstants.COMPLEX_OPERATOR_ATTR, AcademyConstants.COMPLEX_OPERATOR_AND_VAL);
			Element complexAndElement = getItemListInputDoc.createElement(AcademyConstants.COMPLEX_AND_ELEMENT);
			Element complexOrElement = getItemListInputDoc.createElement(AcademyConstants.COMPLEX_OR_ELEMENT);
			
			for(int listIndex = 0; listIndex<containerDetailNL.getLength(); listIndex++){
				Element containerDtlElement = (Element)containerDetailNL.item(listIndex);
				Element expElement = getItemListInputDoc.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
				expElement.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_ITEM_ID);
				expElement.setAttribute(AcademyConstants.ATTR_VALUE, containerDtlElement.getAttribute(AcademyConstants.ATTR_ITEM_ID));
				complexOrElement.appendChild(expElement);
			}
			
			complexAndElement.appendChild(complexOrElement);
			complexQryElement.appendChild(complexAndElement);
			rootElement.appendChild(complexQryElement);
		}
		
		if (log.isVerboseEnabled())
			log.verbose("\nInput XML to getItemList API Call:\n"+YFCDocument.getDocumentFor(getItemListInputDoc));
		log.debug(CLASS_NAME+" :prepareGetItemListInput:Exit");
		return getItemListInputDoc;
	}
}