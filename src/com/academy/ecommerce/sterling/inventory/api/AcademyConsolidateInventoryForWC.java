package com.academy.ecommerce.sterling.inventory.api;

//Java imports
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// Academy imports 
import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.academy.util.constants.AcademyConstants;

//Yantra imports
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyConsolidateInventoryForWC implements YIFCustomApi {

	/*
	 * 
	 * Log Variable
	 * 
	 */
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyConsolidateInventoryForWC.class);
	public void setProperties(Properties props) {

	}

	/*
	 * Input xml to method consolidateInventory <Update_Academy_InventoryMonitor
	 * version="1.0"> <ControlArea> <Verb value=""/> <Noun value=""/>
	 * </ControlArea> <DataArea> <InventoryMonitor> <AvailabilityChange
	 * AlertRaisedOn="" FirstFutureAvailableDate="" FutureAvailableQuantity=""
	 * MonitorOption="" Node="" OnhandAvailableDate=""
	 * OnhandAvailableQuantity=""> <Item ItemID=""/> </AvailabilityChange>
	 * </InventoryMonitor> </DataArea> </Update_Academy_InventoryMonitor>
	 */

	public static Document consolidateInventory(YFSEnvironment env,
			Document inXML) throws Exception {
		try {
			Document docgetItemListOutput = callGetItemList(env, inXML);
			Document docCommonCodeListOutput = callGetCommonCodeList(env,
					docgetItemListOutput);

			// Merging the getCommonCodeList xml to the getItemList xml
			Element eleCommonCodeListOutput = (Element) docgetItemListOutput
					.importNode(docCommonCodeListOutput.getDocumentElement(),
							true);
			docgetItemListOutput.getDocumentElement().appendChild(
					eleCommonCodeListOutput);

			// calling the findInventory API
			Document docFindInventoryOutput = callFindInventory(env,
					docgetItemListOutput);

			// Merging the findInventory output to the input xml
			Element eleFindInventoryOutput = (Element) inXML.importNode(
					docFindInventoryOutput.getDocumentElement(), true);
			inXML.getDocumentElement().appendChild(eleFindInventoryOutput);
		} catch (Exception e) {
			log.error(e);
		}
		return inXML;

		/*
		 * Output Format: ************* <Update_Academy_InventoryMonitor
		 * version="1.0"> <ControlArea> <Verb value=""/> <Noun value=""/>
		 * </ControlArea> <DataArea> <InventoryMonitor> <AvailabilityChange
		 * AlertRaisedOn="" FirstFutureAvailableDate=""
		 * FutureAvailableQuantity="" MonitorOption="" Node=""
		 * OnhandAvailableDate="" OnhandAvailableQuantity=""> <Item ItemID=""/>
		 * </AvailabilityChange> </InventoryMonitor> </DataArea> Promise
		 * CheckInventory="Y" EnterpriseCode="" FulfillmentType=""
		 * OrganizationCode=""> <SuggestedOption> <Option
		 * AvailableFromUnplannedInventory="" FirstDate=""
		 * HasAnyUnavailableQty="" .........> <PromiseLines
		 * TotalNumberOfRecords="1"> <PromiseLine
		 * AutoReplacedByPreferredSubstitute="" FulfillmentType=""
		 * IsBundleParent="" ItemID="" NewItemID="" ProductClass=""
		 * RequiredQty="" UnitOfMeasure=""> <Assignments
		 * TotalNumberOfRecords=""> <Assignment
		 * AvailableFromUnplannedInventory="" DeliveryDate="" ProcuredQty="0.00"
		 * ProductAvailDate="" Quantity="" ReservedQty="" SCAC=""
		 * SegmentChangeQty="" ShipDate="" ShipNode="" SubstitutedQty=""
		 * .........> <SubstituteItems /> <Procurements /> </Assignment> . .
		 * <Assignment EmptyAssignmentReason="NOT_ENOUGH_PRODUCT_CHOICES" ....>
		 * </Assignments> . . </Promise>
		 * 
		 * </Update_Academy_InventoryMonitor>
		 */

	}

	private static Document callFindInventory(YFSEnvironment env,
			Document docItem) throws Exception {
		Document docFindInvReturn = null;
		try {
			Document docfindInventoryInput = XMLUtil
					.createDocument(AcademyConstants.PROMISE);
			Element elePromise = docfindInventoryInput.getDocumentElement();
			NodeList nlItem = docItem.getDocumentElement()
					.getElementsByTagName("Item");
			Element eleItem = (Element) nlItem.item(0);
			NodeList nlCommonCode = docItem.getDocumentElement()
					.getElementsByTagName("CommonCode");
			Element eleCommonCode = (Element) nlCommonCode.item(0);
			elePromise.setAttribute(AcademyConstants.CHECK_INVENTORY,
					AcademyConstants.STR_YES);
			elePromise.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			elePromise.setAttribute(AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			elePromise
					.setAttribute(
							AcademyConstants.ATTR_FULFILLMENT_TYPE,
							eleCommonCode
									.getAttribute(AcademyConstants.CODE_LONG_DESC));
			Element elePromiseLines = docfindInventoryInput
					.createElement(AcademyConstants.PROMISE_LINES);
			Element elePromiseLine = docfindInventoryInput
					.createElement(AcademyConstants.PROMISE_LINE);
			elePromiseLine
					.setAttribute(
							AcademyConstants.ATTR_FULFILLMENT_TYPE,
							eleCommonCode
									.getAttribute(AcademyConstants.CODE_LONG_DESC));
			elePromiseLine.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleItem
					.getAttribute(AcademyConstants.ATTR_ITEM_ID));
			elePromiseLine.setAttribute(AcademyConstants.ATTR_PROD_CLASS,
					AcademyConstants.PRODUCT_CLASS);
			elePromiseLine.setAttribute(AcademyConstants.REQUIRED_QTY,
					AcademyConstants.QTY_100000);
			elePromiseLine.setAttribute(AcademyConstants.ATTR_UOM,
					AcademyConstants.UNIT_OF_MEASURE);

			XMLUtil.appendChild(elePromise, elePromiseLines);
			XMLUtil.appendChild(elePromiseLines, elePromiseLine);

			docFindInvReturn = AcademyUtil.invokeAPI(env,
					AcademyConstants.FIND_INVENTORY, docfindInventoryInput);
		} catch (Exception e) {
			log.error(e);
		}
		return docFindInvReturn;

		/*
		 * Output XML format: ***************** Standard output xml for
		 * findInventory API. Refer Java docs
		 * 
		 */

	}

	private static Document callGetItemList(YFSEnvironment env,
			Document inputDoc) throws Exception {
		Document docGetItemListReturn = null;
		try {
			Document getItemListInput = XMLUtil
					.createDocument(AcademyConstants.ITEM);
			Element eleRootElement = getItemListInput.getDocumentElement();
			NodeList nlItem = inputDoc.getDocumentElement()
					.getElementsByTagName("Item");
			Element eleItem = (Element) nlItem.item(0);
			// eleRootElement.setAttribute(AcademyConstants.ORGANIZATION_CODE,
			// AcademyConstants.PRIMARY_ENTERPRISE);

			eleRootElement.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleItem
					.getAttribute(AcademyConstants.ATTR_ITEM_ID));
			docGetItemListReturn = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_ITEM_LIST, getItemListInput);
		} catch (Exception e) {
			log.error(e);
		}
		return docGetItemListReturn;

		/*
		 * Output XML format: ***************** <CommonCodeList> <CommonCode
		 * CodeLength="" CodeLongDescription="" CodeName=""
		 * CodeShortDescription="" CodeSymbol="" CodeType="" CodeValue=""
		 * CommonCodeKey="" DocumentType="" MeantForEnterprise=""
		 * MeantForInternal="" MeantForSupplier="" OrganizationCode=""
		 * SystemDefinedCode="" /> </CommonCodeList>
		 * 
		 */

	}

	private static Document callGetCommonCodeList(YFSEnvironment env,
			Document inDoc) throws Exception {
		Document docCommonCodeReturn = null;
		try {
			Document getCommonCodeInput = XMLUtil
					.createDocument(AcademyConstants.ELE_COMMON_CODE);
			Element eleRootElement = getCommonCodeInput.getDocumentElement();
			NodeList nlClassificationCodes = inDoc.getDocumentElement()
					.getElementsByTagName("ClassificationCodes");
			Element eleClassificationCodes = (Element) nlClassificationCodes
					.item(0);

			// set attribute value for OrganizationCode
			eleRootElement.setAttribute(AcademyConstants.ATTR_CODE_TYPE,
					AcademyConstants.ITEM_FULFILLMENT_TYPE);
			// creating the output template for getCommonCodeList API
			eleRootElement.setAttribute(
					AcademyConstants.ATTR_COMMON_CODE_VALUE,
					eleClassificationCodes
							.getAttribute(AcademyConstants.ATTR_STORAGE_TYPE));
			docCommonCodeReturn = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_COMMONCODE_LIST,
					getCommonCodeInput);
		} catch (Exception e) {
			log.error(e);
		}

		return docCommonCodeReturn;

		/*
		 * 
		 * Output XML format **************** <ItemList LastItemKey=""
		 * .........> <Item GlobalItemID="" ItemGroupCode="" ItemID=""
		 * OrganizationCode="" UnitOfMeasure=""> . . <Extn ............/>
		 * <ClassificationCodes StorageType="" ......../> </Item> </ItemList>
		 * 
		 */

	}

}
