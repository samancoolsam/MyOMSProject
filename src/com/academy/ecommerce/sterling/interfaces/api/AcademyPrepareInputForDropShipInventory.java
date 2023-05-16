/**
 * 
 */
package com.academy.ecommerce.sterling.interfaces.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;

/**
 * @author JParameswaran 
 * 		   
 * 		   Logic is performed in this class to get the exact Drop
 *         Ship item for which inventory is going to be loaded based on GTIN and
 *         also to prepare the input to call necessary Sterling APIs to process
 *         inventory data
 */
public class AcademyPrepareInputForDropShipInventory implements YIFCustomApi {

	/**
	 * Variable to store input document
	 */
	Document docInput = null;

	/**
	 * Variable to store output document that'll be returned
	 */
	Document docOutput = null;
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyPrepareInputForDropShipInventory.class);
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yantra.interop.japi.YIFCustomApi#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * @param env
	 *            Environment variable for the transaction
	 * @param inDoc
	 *            Input document passed to the API
	 * @return docOutput Output document that can be used to load invetory using
	 *         Sterling APIs
	 * @throws Exception
	 *             Generic Exception
	 * 
	 * This method is the main method that gets called which would call other
	 * methods within to get item details and prepare the output document
	 * 
	 */

	public Document prepareInputForDropShipInventoryLoad(YFSEnvironment env,
			Document inDoc) throws Exception {
		log.beginTimer(" Begining of AcademyPrepareInputForDropShipInventory-> prepareInputForDropShipInventoryLoad Api");
		this.docInput = inDoc;
		if (YFCObject.isVoid(this.docInput)) {
			YFSException excDocInput = new YFSException(
					AcademyConstants.ERR_INVALID_NULL_DOC);
			excDocInput.setErrorCode(AcademyConstants.ERR_CODE_03);
			throw excDocInput;
		}
		getItemDetailsAndPrepareOutput(env);
		log.endTimer(" End of AcademyPrepareInputForDropShipInventory-> prepareInputForDropShipInventoryLoad Api");
		return docOutput;
	}

	/**
	 * 
	 * @param env -
	 *            Environment variable for the transaction
	 * 
	 * This method will get the exact item information from GTIN obtained from
	 * input and will prepare the output document in a format required by
	 * Sterling APIs for Inventory load
	 */

	private void getItemDetailsAndPrepareOutput(YFSEnvironment env) {
		NodeList nListItems = null;
		Element eleCurrentItem = null;
		Element eleItem = null;
		Element eleItem1 = null;
		int iNoOfItems = 0;
		String strGlobalItemId = null;
		String strCurrentItemId = null;
		Document docItemDetails = null;

		try {
			nListItems = XMLUtil.getNodeList(
					this.docInput.getDocumentElement(), AcademyConstants.ITEM);
			iNoOfItems = nListItems.getLength();
			for (int i = 0; i < iNoOfItems; i++) {
				eleCurrentItem = (Element) nListItems.item(i);
				strGlobalItemId = eleCurrentItem
						.getAttribute(AcademyConstants.ATTR_GLOB_ITEM_ID);
				docItemDetails = getDetailsOfItem(strGlobalItemId, env);
				if (!YFCObject.isVoid(docItemDetails)) {
					strCurrentItemId = XPathUtil.getString(docItemDetails
							.getDocumentElement(), "Item[@GlobalItemID='"
							+ strGlobalItemId + "']/@ItemID");
					// strCurrentItemId =
					// XPathUtil.getString(docItemDetails.getDocumentElement(),"Item[/@ItemID");
					if (YFCObject.isVoid(strCurrentItemId)) {
						throw new YFSException(
								"Item does not exist for Global Item " + "\""
										+ strGlobalItemId + "\"");
					}
					if (YFCObject.isNull(docOutput)) {
						docOutput = XMLUtil
								.createDocument(AcademyConstants.ELE_ITEMS);
						eleItem = docOutput
								.createElement(AcademyConstants.ITEM);
						eleItem.setAttribute(AcademyConstants.ATTR_ADJUST_TYPE,
								AcademyConstants.STR_ADJUSTMENT);
						eleItem.setAttribute(AcademyConstants.ATTR_ITEM_ID,
								strCurrentItemId);
						eleItem
								.setAttribute(
										AcademyConstants.ATTR_UOM,
										eleCurrentItem
												.getAttribute(AcademyConstants.ATTR_UOM));
						eleItem
								.setAttribute(
										AcademyConstants.ATTR_PROD_CLASS,
										eleCurrentItem
												.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
						eleItem.setAttribute(
								AcademyConstants.ORGANIZATION_CODE,
								AcademyConstants.PRIMARY_ENTERPRISE);
						eleItem.setAttribute(AcademyConstants.SHIP_NODE,
								this.docInput.getDocumentElement()
										.getAttribute(
												AcademyConstants.SHIP_NODE));
						eleItem
								.setAttribute(
										AcademyConstants.ATTR_QUANTITY,
										eleCurrentItem
												.getAttribute(AcademyConstants.ATTR_AVLBL_QUANTITY));
						eleItem.setAttribute(AcademyConstants.STR_AVAILABILITY,
								AcademyConstants.STR_AVAILABILITY_INFINITE);
						eleItem.setAttribute(AcademyConstants.STR_AVAILABILITY,
								AcademyConstants.STR_AVAILABILITY_INFINITE);
						eleItem.setAttribute(AcademyConstants.ATTR_REASON_CODE,
								AcademyConstants.STR_REASON_CODE_VAL);
						eleItem.setAttribute(AcademyConstants.ATTR_REASON_TEXT,
								AcademyConstants.STR_REASON_CODE_VAL);
						eleItem.setAttribute(AcademyConstants.ATTR_SUPPLY_TYPE,
								AcademyConstants.STR_SUPP_TYPE_VAL);
						Element eleChild = (Element) docOutput.importNode(
								eleItem, true);
						docOutput.getDocumentElement().appendChild(eleChild);
						eleItem1 = docOutput
								.createElement(AcademyConstants.ITEM);
						eleItem1.setAttribute(
								AcademyConstants.ATTR_ADJUST_TYPE,
								AcademyConstants.STR_ADJUSTMENT);
						eleItem1.setAttribute(AcademyConstants.ATTR_ITEM_ID,
								strCurrentItemId);
						eleItem1
								.setAttribute(
										AcademyConstants.ATTR_UOM,
										eleCurrentItem
												.getAttribute(AcademyConstants.ATTR_UOM));
						eleItem1
								.setAttribute(
										AcademyConstants.ATTR_PROD_CLASS,
										eleCurrentItem
												.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
						eleItem1.setAttribute(
								AcademyConstants.ORGANIZATION_CODE,
								AcademyConstants.PRIMARY_ENTERPRISE);
						eleItem1.setAttribute(AcademyConstants.SHIP_NODE,
								this.docInput.getDocumentElement()
										.getAttribute(
												AcademyConstants.SHIP_NODE));
						eleItem1
								.setAttribute(
										AcademyConstants.ATTR_QUANTITY,
										eleCurrentItem
												.getAttribute(AcademyConstants.ATTR_AVLBL_QUANTITY));
						eleItem1.setAttribute(
								AcademyConstants.STR_AVAILABILITY,
								AcademyConstants.STR_AVAILABILITY_TRCK);
						eleItem1.setAttribute(
								AcademyConstants.ATTR_REASON_CODE,
								AcademyConstants.STR_REASON_CODE_VAL);
						eleItem1.setAttribute(
								AcademyConstants.ATTR_REASON_TEXT,
								AcademyConstants.STR_REASON_CODE_VAL);
						eleItem1.setAttribute(
								AcademyConstants.ATTR_SUPPLY_TYPE,
								AcademyConstants.STR_SUPP_TYPE_VAL);
						Element eleChild1 = (Element) docOutput.importNode(
								eleItem1, true);
						docOutput.getDocumentElement().appendChild(eleChild1);

					} else {
						eleItem = docOutput
								.createElement(AcademyConstants.ITEM);
						eleItem.setAttribute(AcademyConstants.ATTR_ADJUST_TYPE,
								AcademyConstants.STR_ADJUSTMENT);
						eleItem.setAttribute(AcademyConstants.ATTR_ITEM_ID,
								strCurrentItemId);
						eleItem
								.setAttribute(
										AcademyConstants.ATTR_UOM,
										eleCurrentItem
												.getAttribute(AcademyConstants.ATTR_UOM));
						eleItem
								.setAttribute(
										AcademyConstants.ATTR_PROD_CLASS,
										eleCurrentItem
												.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
						eleItem.setAttribute(
								AcademyConstants.ORGANIZATION_CODE,
								AcademyConstants.PRIMARY_ENTERPRISE);
						eleItem.setAttribute(AcademyConstants.SHIP_NODE,
								this.docInput.getDocumentElement()
										.getAttribute(
												AcademyConstants.SHIP_NODE));
						eleItem
								.setAttribute(
										AcademyConstants.ATTR_QUANTITY,
										eleCurrentItem
												.getAttribute(AcademyConstants.ATTR_AVLBL_QUANTITY));
						eleItem.setAttribute(AcademyConstants.STR_AVAILABILITY,
								AcademyConstants.STR_AVAILABILITY_INFINITE);
						eleItem.setAttribute(AcademyConstants.ATTR_REASON_CODE,
								AcademyConstants.STR_REASON_CODE_VAL);
						eleItem.setAttribute(AcademyConstants.ATTR_REASON_TEXT,
								AcademyConstants.STR_REASON_CODE_VAL);
						eleItem.setAttribute(AcademyConstants.ATTR_SUPPLY_TYPE,
								AcademyConstants.STR_SUPP_TYPE_VAL);
						Element eleChild = (Element) docOutput.importNode(
								eleItem, true);
						docOutput.getDocumentElement().appendChild(eleChild);
						eleItem1 = docOutput.createElement("Item");
						eleItem1.setAttribute(
								AcademyConstants.ATTR_ADJUST_TYPE,
								AcademyConstants.STR_ADJUSTMENT);
						eleItem1.setAttribute(AcademyConstants.ATTR_ITEM_ID,
								strCurrentItemId);
						eleItem1
								.setAttribute(
										AcademyConstants.ATTR_UOM,
										eleCurrentItem
												.getAttribute(AcademyConstants.ATTR_UOM));
						eleItem1
								.setAttribute(
										AcademyConstants.ATTR_PROD_CLASS,
										eleCurrentItem
												.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
						eleItem1.setAttribute(
								AcademyConstants.ORGANIZATION_CODE,
								AcademyConstants.PRIMARY_ENTERPRISE);
						eleItem1.setAttribute(AcademyConstants.SHIP_NODE,
								this.docInput.getDocumentElement()
										.getAttribute(
												AcademyConstants.SHIP_NODE));
						eleItem1
								.setAttribute(
										AcademyConstants.ATTR_QUANTITY,
										eleCurrentItem
												.getAttribute(AcademyConstants.ATTR_AVLBL_QUANTITY));
						eleItem1.setAttribute(
								AcademyConstants.STR_AVAILABILITY,
								AcademyConstants.STR_AVAILABILITY_TRCK);
						eleItem1.setAttribute(
								AcademyConstants.ATTR_REASON_CODE,
								AcademyConstants.STR_REASON_CODE_VAL);
						eleItem1.setAttribute(
								AcademyConstants.ATTR_REASON_TEXT,
								AcademyConstants.STR_REASON_CODE_VAL);
						eleItem1.setAttribute(
								AcademyConstants.ATTR_SUPPLY_TYPE,
								AcademyConstants.STR_SUPP_TYPE_VAL);
						Element eleChild1 = (Element) docOutput.importNode(
								eleItem1, true);
						docOutput.getDocumentElement().appendChild(eleChild1);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
	}

	/**
	 * 
	 * @param strGlobalItemId -
	 *            Global Item Id or GTIN passed in Input
	 * @param env -
	 *            Environment variable
	 * @return docOutputGetItemDetails - Document containing the details of the
	 *         passed item
	 * 
	 * This method will make the necessary API calls based on GTIN and will
	 * return the exact item information required to load inventory using
	 * Sterling APIs
	 */

	private Document getDetailsOfItem(String strGlobalItemId, YFSEnvironment env) {
		Document docInputGetItemDetails = null;
		Document docOutputGetItemDetails = null;
		Element eleInputGetItemDetails = null;
		try {
			docInputGetItemDetails = XMLUtil
					.createDocument(AcademyConstants.ITEM);
			eleInputGetItemDetails = docInputGetItemDetails
					.getDocumentElement();
			eleInputGetItemDetails.setAttribute(
					AcademyConstants.ATTR_GLOB_ITEM_ID, strGlobalItemId);
			eleInputGetItemDetails.setAttribute(
					AcademyConstants.ATTR_READ_UNCOMMITED,
					AcademyConstants.STR_YES);
			docOutputGetItemDetails = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_ITEM_LIST, docInputGetItemDetails);

		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

		return docOutputGetItemDetails;

	}

}
