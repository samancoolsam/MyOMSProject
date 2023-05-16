package com.academy.ecommerce.sterling.fulfillmentSummary.extn;

/**
 * Created on May 11,2009
 *
 */
//Java Imports
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//Sterling Imports
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCExtendedCellModifier;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCTblClmBindingData;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCXPathUtils;
import com.yantra.yfc.rcp.YRCXmlUtils;

//Project Imports
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.academy.ecommerce.sterling.util.XPathUtil;

//Misc -NONE
/**
 * @author sahmed Copyright © 2005-2008 Sterling Commerce, Inc. All Rights
 *         Reserved.
 */
public class AcademyShipmentPanelExtnBehavior extends YRCExtentionBehavior {

	private NodeList nListOrderLines = null;
	private int iNoOfOrderLines = 0;
	private Element eleCurrentOrderLine = null;

	/**
	 * This method initializes the behavior class.
	 */
	public void init() {
	}

	/**
	 * Method for validating the text box.
	 */
	public YRCValidationResponse validateTextField(String fieldName,
			String fieldValue) {
		return super.validateTextField(fieldName, fieldValue);
	}

	/**
	 * Method for validating the combo box entry.
	 */
	public void validateComboField(String fieldName, String fieldValue) {
		super.validateComboField(fieldName, fieldValue);
	}

	/**
	 * Method called when a button is clicked.
	 */
	public YRCValidationResponse validateButtonClick(String fieldName) {
		return super.validateButtonClick(fieldName);
	}

	/**
	 * Method called when a link is clicked.
	 */
	public YRCValidationResponse validateLinkClick(String fieldName) {
		return super.validateLinkClick(fieldName);
	}

	public YRCExtendedTableBindingData getExtendedTableBindingData(
			String tableName, ArrayList tableColumnNames) {
		// Create and return the binding data definition for the table.
		YRCPlatformUI
				.trace("#####  Inside getExtendedTableBindingData ########");
		YRCExtendedTableBindingData tblBindingData = new YRCExtendedTableBindingData();
		createTblColBindindData(tblBindingData);

		/**
		 * validate the check box on the shipment panel to identify if the item
		 * is drop ship item and if drop ship item, do not allow the user to
		 * check box for gift options
		 */
		YRCExtendedCellModifier myCellModifier = new YRCExtendedCellModifier() {
			@Override
			public boolean allowModify(String property, String value,
					Element element) {
				if (property.equals(AcademyPCAConstants.ATTR_CHECKED)) {
					try {
						if(element.getAttribute("Checked").equals(AcademyPCAConstants.ATTR_Y))
							{
									Element eleOrder=getModel(AcademyPCAConstants.ATTR_ORDER);
									YRCPlatformUI.trace("allowModify", XMLUtil.getElementXMLString(eleOrder));
									NodeList listOfOrderLines = XPathUtil.getNodeList(eleOrder,"OrderLines/OrderLine");
									int lenOfSelectedLines = listOfOrderLines.getLength();
									for (int i = 0; i < lenOfSelectedLines; i++) {
										Element currentSelectedOrderLine = (Element) listOfOrderLines.item(i);
										currentSelectedOrderLine.setAttribute("Checked", AcademyPCAConstants.ATTR_Y);
										repopulateModel(AcademyPCAConstants.ATTR_ORDER);
									} 	
									 YRCPlatformUI.setMessage("Once lines are marked as Gift, Receipient Name/Message will be copied to all orderlines");
						}
					} 
					 catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					YRCPlatformUI.trace("#########OrderLine Elem is :###############", YRCXmlUtils
							.getString(element));
					Element extnElem = (Element) element.getElementsByTagName(
							AcademyPCAConstants.ATTR_EXTN).item(0);
					if (extnElem.getAttribute(
							AcademyPCAConstants.ATTR_DROP_SHIP_FLAG).equals(
							AcademyPCAConstants.ATTR_Y)) {
						return false;
					}
				}
				return true;
			}

			@Override
			public String getModifiedValue(String arg0, String arg1,
					Element arg2) {
				return null;
			}

			@Override
			public YRCValidationResponse validateModifiedValue(String property,
					String value, Element element) {
				return null;
			}

		};

		tblBindingData.setCellModifier(myCellModifier);
		return tblBindingData;
	}

	/**
	 * This method is used to set column binding data for each of the advance
	 * custom columns . This is achieved by creating a hash map with multiple
	 * instances of YRCTblClmBindingData for each of the columns and finally
	 * setting the hash map against the instance of
	 * 'YRCExtendedTableBindingData'
	 * 
	 * @param tblBindingData
	 *            Table Binding Data object that stores data for custom columns
	 */
	private void createTblColBindindData(
			YRCExtendedTableBindingData tblBindingData) {
		HashMap<String, YRCTblClmBindingData> clmBindDataMap = new HashMap<String, YRCTblClmBindingData>();
		YRCTblClmBindingData clmBindData = new YRCTblClmBindingData();
		clmBindData.setAttributeBinding(AcademyPCAConstants.ATTR_CONFIRM);
		clmBindData.setName(AcademyPCAConstants.ATTR_ITEM_CHECK);
		clmBindDataMap.put(AcademyPCAConstants.ATTR_EXTN_ITEM_CHECK,
				clmBindData);
		tblBindingData.setTableColumnBindingsMap(clmBindDataMap);
		tblBindingData.setCellTypes(getCellTypes());

	}

	private HashMap<String, String> getCellTypes() {
		HashMap<String, String> cellTypesMap = new HashMap<String, String>();
		cellTypesMap.put(AcademyPCAConstants.ATTR_EXTN_ITEM_CHECK,
				YRCConstants.YRC_LABEL_BINDING_DEFINITION);
		return cellTypesMap;
	}

	public Document getSelectedLinesModel() {
		Document orderModel = getModel(AcademyPCAConstants.ATTR_ORDER)
				.getOwnerDocument();
		return orderModel;
	}

	public void setSelectedModel(Element documentElement) {
		repopulateModel(AcademyPCAConstants.ATTR_ORDER);
		setGiftIcon(documentElement);

	}

	private void setGiftIcon(Element documentElement) {
		NodeList nodeList = (NodeList) YRCXPathUtils.evaluate(documentElement,
				"/Order/OrderLines/OrderLine[@GiftFlag='Y']",
				XPathConstants.NODESET);
		if (nodeList.getLength() > 0) {

		}
	}

	@Override
	public void postSetModel(String namespace) {
		Element eleOrderDetails = getModel(AcademyPCAConstants.ATTR_ORDER);
		if (!YRCPlatformUI.isVoid(eleOrderDetails)) {
			YRCPlatformUI.trace("######Order Model is:############",
					eleOrderDetails);
			try {
				nListOrderLines = XPathUtil.getNodeList(eleOrderDetails,
						AcademyPCAConstants.XPATH_ORDERLINES_ORDERLINE);
				if (!YRCPlatformUI.isVoid(nListOrderLines))
					iNoOfOrderLines = nListOrderLines.getLength();
				for (int i = 0; i < iNoOfOrderLines; i++) {
					eleCurrentOrderLine = (Element) nListOrderLines.item(i);
					String strReqShipDate = eleCurrentOrderLine
							.getAttribute(AcademyPCAConstants.ATTR_REQ_SHIP_DATE);
					eleCurrentOrderLine.setAttribute(
							AcademyPCAConstants.ATTR_PROD_SHIP_DATE,
							strReqShipDate);
					eleCurrentOrderLine.setAttribute(
							"ProductAvailDate",
							strReqShipDate);
					NodeList nlAssignment = eleCurrentOrderLine
					.getElementsByTagName(AcademyPCAConstants.ATTR_ASSIGNMENT);
					for (int j = 0; j < nlAssignment.getLength(); j++) {
						Element eleAssignment = (Element) nlAssignment.item(j);
						eleAssignment.setAttribute(AcademyPCAConstants.ATTR_PROD_AVAIL_DATE, strReqShipDate);
						eleAssignment.setAttribute(AcademyPCAConstants.ATTR_SHIP_DATE, strReqShipDate);
							
					}
				}
				repopulateModel(AcademyPCAConstants.ATTR_ORDER);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.postSetModel(namespace);
	}

}
//TODO Create and return the binding oldData definition for Table column: extn_tblGiftClm
// refer to javadoc of method getExtendedTableBindingData(String , ArrayList) for more information.
//TODO Create and return the binding oldData definition for Table column: extn_Item_CheckBox
// refer to javadoc of method getExtendedTableBindingData(String , ArrayList) for more information.