package com.academy.ecommerce.sterling.changeFulfillmentOptions.extn;

/**
 * Created on May 28,2009
 *
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.orderEntry.addItems.screens.AcademyPricingPromoPanelBehavior;
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.academy.ecommerce.sterling.util.XPathUtil;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCXmlUtils;

// Misc Imports - NONE

/**
 * @author sahmed Copyright © 2005-2008 Sterling Commerce, Inc. All Rights
 *         Reserved.
 */
public class AcademyChangeFulfillmentOptionsExtnBehavior extends
		YRCExtentionBehavior {

	NodeList nListOrderLines = null;

	int iNoOfOrderLines = 0;

	Element eleCurrentOrderLine = null;

	private AcademyPricingPromoPanelBehavior pricingPromoPanelBehavior1;

	public static String flag = "None";

	/**
	 * This method initializes the behavior class.
	 */
	public void init() {
		flag = AcademyPCAConstants.ATTR_ON_LOAD;
		super.init();
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
		if (fieldName.equals(AcademyPCAConstants.ATTR_CMB_SHIP_TO_ADDRESS)
				&& fieldValue != null) {
			evaluateLOS(fieldValue);
		}

		super.validateComboField(fieldName, fieldValue);
	}

	private void evaluateLOS(String fieldValue) {

		Collection current = null;
		Collection previous = null;
		try {
			Element personInfoListElem = getModel(AcademyPCAConstants.ATTR_ADDRESS_LIST);
			YRCPlatformUI.trace("PersonInfoList Output", YRCXmlUtils
					.getString(personInfoListElem));
			Element personInfoElem = (Element) XPathUtil.getNode(
					personInfoListElem, "PersonInfo[@PersonInfoKey='"
							+ fieldValue + "']");
			if(!YRCPlatformUI.isVoid(personInfoElem))
			{
				Element salesOrderModelElement = getModel(AcademyPCAConstants.ATTR_GET_SALES_ORDER_DETAILS);
				YRCPlatformUI.trace("Sales Order o/p", YRCXmlUtils
						.getString(salesOrderModelElement));
				NodeList listOfSelectedLines = XPathUtil
						.getNodeList(
								salesOrderModelElement,
								"OrderLines/OrderLine[@Checked='Y' and ( not(BundleParentLine/@BundleFulfillmentMode) or (BundleParentLine/@BundleFulfillmentMode!='01' and BundleParentLine/@BundleFulfillmentMode!='02'))]");
				int lenOfSelectedLines = listOfSelectedLines.getLength();
				if (lenOfSelectedLines > 0) {
					for (int i = 0; i < lenOfSelectedLines; i++) {
						String isAPOFPO = "";
						String isPOBOX = "";
						String isRegAddress = "";
						Element currentSelectedOrderLine = (Element) listOfSelectedLines
								.item(i);
						String itemType = getItemType(currentSelectedOrderLine);
						Element extnElemForPersonInfo = (Element) personInfoElem
								.getElementsByTagName(AcademyPCAConstants.ATTR_EXTN)
								.item(0);
						if (!YRCPlatformUI.isVoid(extnElemForPersonInfo)) {
							isAPOFPO = extnElemForPersonInfo
									.getAttribute(AcademyPCAConstants.ATTR_APO_FPO);
							isPOBOX = extnElemForPersonInfo
									.getAttribute(AcademyPCAConstants.ATTR_IS_POBOX_ADDRESS);

							if (isAPOFPO.equals(AcademyPCAConstants.ATTR_N)
									&& isPOBOX.equals(AcademyPCAConstants.ATTR_N)) {
								isRegAddress = AcademyPCAConstants.ATTR_Y;
							} else {
								isRegAddress = AcademyPCAConstants.ATTR_N;
							}
						} else {
							isRegAddress = AcademyPCAConstants.ATTR_Y;
							isAPOFPO = AcademyPCAConstants.ATTR_N;
							isPOBOX = AcademyPCAConstants.ATTR_N;
						}
						String xpath = "ACADLosRest[@IsAPOFPO='" + isAPOFPO
								+ "' and @IsPOBOX='" + isPOBOX
								+ "' and @IsRegularAddress='" + isRegAddress
								+ "' and @ShipmentType='" + itemType + "']";
						NodeList listOfCarriersForCurrentLine = XPathUtil
								.getNodeList(
										getModel(AcademyPCAConstants.ATTR_EXTN_LOS_RESTRICTION_MODEL),
										xpath);
						YRCPlatformUI.trace("Los Nodes", (YRCXmlUtils
								.getString((Element) listOfCarriersForCurrentLine
										.item(0))));
						// Obtain the valid LOS's for this line
						HashMap h1 = new HashMap();
						for (int j = 0; j < listOfCarriersForCurrentLine
								.getLength(); j++) {
							Element currNode = (Element) listOfCarriersForCurrentLine
									.item(j);
							if (h1 == null
									|| !h1
											.containsKey(currNode
													.getAttribute(AcademyPCAConstants.ATTR_LOS_VALUE))) {
								h1
										.put(
												currNode
														.getAttribute(AcademyPCAConstants.ATTR_LOS_VALUE),
												currNode
														.getAttribute(AcademyPCAConstants.ATTR_LOS_VALUE));
							}
						}
						current = h1.values();
						if (i > 0) {
							current.retainAll(previous);
						}
						previous = h1.values();
					}
					int finalNoOfElements = current.size();
					String[] e = new String[finalNoOfElements];
					current.toArray(e);
					Document docCarrierServiceList = XMLUtil
							.createDocument(AcademyPCAConstants.ELE_ACAD_LOS_REST_LIST);
					for (int k = 0; k < finalNoOfElements; k++) {

						if (YRCPlatformUI.isVoid(docCarrierServiceList)) {
							docCarrierServiceList = XMLUtil
									.createDocument(AcademyPCAConstants.ELE_ACAD_LOS_REST_LIST);
						}
						// Element temp = (Element) docCarrierServiceList
						// .importNode(e[k], true);
						Element temp = docCarrierServiceList
								.createElement(AcademyPCAConstants.ELE_ACAD_LOS_REST);
						temp.setAttribute(AcademyPCAConstants.ATTR_LOS_VALUE, e[k]);
						docCarrierServiceList.getDocumentElement()
								.appendChild(temp);
					}
					setExtentionModel(
							AcademyPCAConstants.ATTR_EXTN_LIST_CARRIER_SERVICE,
							docCarrierServiceList.getDocumentElement());
				} else {
					Document docCarrierServiceList = XMLUtil
							.createDocument(AcademyPCAConstants.ELE_ACAD_LOS_REST_LIST);
					setExtentionModel(
							AcademyPCAConstants.ATTR_EXTN_LIST_CARRIER_SERVICE,
							docCarrierServiceList.getDocumentElement());
				}
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getItemType(Element currentSelectedOrderLine) {

		Element itemElem, itemExtnElem, itemPrimaryInfo = null;
		itemElem = (Element) currentSelectedOrderLine.getElementsByTagName(
				AcademyPCAConstants.ATTR_ITEM_DETAILS).item(0);
		if (itemElem != null && itemElem.hasChildNodes()) {
			itemExtnElem = (Element) itemElem.getElementsByTagName(
					AcademyPCAConstants.ATTR_EXTN).item(0);
			if (itemExtnElem != null && itemExtnElem.hasAttributes()) {

				if ((AcademyPCAConstants.ATTR_Y)
						.equals(itemExtnElem
								.getAttribute(AcademyPCAConstants.ATTR_ITEM_IS_GIFT_CARD))) {
					return "GIFT-CARD";
				}
				if ((AcademyPCAConstants.ATTR_Y)
						.equals(itemExtnElem
								.getAttribute(AcademyPCAConstants.ATTR_ITEM_IS_WHITE_GLOVE))) {
					return "WHITE-GLOVE";
				}
				if ((AcademyPCAConstants.ATTR_Y).equals(itemExtnElem
						.getAttribute(AcademyPCAConstants.ATTR_ITEM_IS_KIT))) {
					return "BULK-KIT";
				}
				if ((AcademyPCAConstants.ATTR_Y)
						.equals(itemExtnElem
								.getAttribute(AcademyPCAConstants.ATTR_ITEM_SHIP_ALONE))
						&& (AcademyPCAConstants.ATTR_N)
								.equals(itemExtnElem
										.getAttribute(AcademyPCAConstants.ATTR_ITEM_IS_CONVEYABLE))) {
					return "BULK-KIT";
				}
				if ((AcademyPCAConstants.ATTR_Y).equals(itemExtnElem
						.getAttribute(AcademyPCAConstants.ATTR_ITEM_IS_CONVEYABLE))) {
					return "CONVEYABLE";
				}
			}

			itemPrimaryInfo = (Element) itemElem.getElementsByTagName(
					AcademyPCAConstants.ATTR_ITEM_PRIMARY_INFO).item(0);
			if (itemPrimaryInfo != null && itemPrimaryInfo.hasAttributes()) {
				if ((AcademyPCAConstants.ATTR_Y).equals(itemPrimaryInfo
						.getAttribute(AcademyPCAConstants.ATTR_ITEM_IS_HAZMAT))) {
					return "HAZMAT";
				}
			}
		}

		return "";
	}

	/**
	 * Method called when a button is clicked.
	 */
	public YRCValidationResponse validateButtonClick(String fieldName)
	{
		if(fieldName.equals(AcademyPCAConstants.ATTR_BTN_APPLY_DELIVERY))
		{
			setFieldValue("radDelivery", true);
			try
			{
				Element eleOrderDetails = getModel(AcademyPCAConstants.ATTR_GET_SALES_ORDER_DETAILS);
				YRCPlatformUI.trace("Sales Order Details : " + XMLUtil.getElementXMLString(eleOrderDetails));
				YRCPlatformUI.trace("Sales Order Details : " + XMLUtil.getElementXMLString(eleOrderDetails));
				NodeList listOfSelectedOrderLines = XPathUtil.getNodeList(eleOrderDetails, AcademyPCAConstants.XPATH_SELECTED_ORDERLINES);

				String carrSvcCode = getFieldValue(AcademyPCAConstants.EXTN_COMBO_NAME);
				if(!YRCPlatformUI.isVoid(carrSvcCode))
				{
					if(!YRCPlatformUI.isVoid(listOfSelectedOrderLines))
					{
						int noOfSelectedOrderLines = listOfSelectedOrderLines.getLength();
						if(noOfSelectedOrderLines > 0)
						{
							for(int i = 0; i < noOfSelectedOrderLines; i++)
							{
								Element currentOrderLine = (Element)listOfSelectedOrderLines.item(i);
								currentOrderLine.setAttribute(AcademyPCAConstants.ATTR_CARRIER_SERVICE_CODE, carrSvcCode);
							}
						}
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return super.validateButtonClick(fieldName);
	}

	/**
	 * Method called when a link is clicked.
	 */
	public YRCValidationResponse validateLinkClick(String fieldName) {
		return super.validateLinkClick(fieldName);
	}

	/**
	 * Create and return the binding data for advanced table columns added to
	 * the tables.
	 */
	public YRCExtendedTableBindingData getExtendedTableBindingData(
			String tableName, ArrayList tableColumnNames) {
		YRCExtendedTableBindingData tblBindingData = new YRCExtendedTableBindingData();
		return tblBindingData;
	}

	@Override
	public void postSetModel(String namespace) {

		if (namespace.equals(AcademyPCAConstants.ATTR_GET_CARRIER_SERVICE_LIST)) {
			Element eleOrderDetails = getModel(AcademyPCAConstants.ATTR_GET_SALES_ORDER_DETAILS);
			try {
				nListOrderLines = XPathUtil.getNodeList(eleOrderDetails,
						AcademyPCAConstants.XPATH_SELECTED_ORDERLINES);
				if (!YRCPlatformUI.isVoid(nListOrderLines)) {
					iNoOfOrderLines = nListOrderLines.getLength();
				}
				if (iNoOfOrderLines > 0) {
					if (!YRCPlatformUI
							.isVoid(getFieldValue(AcademyPCAConstants.ATTR_CMB_SHIP_TO_ADDRESS))) {
						evaluateLOS(getFieldValue(AcademyPCAConstants.ATTR_CMB_SHIP_TO_ADDRESS));
					}
				}
			}

			catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (namespace.equals(AcademyPCAConstants.ATTR_GET_SALES_ORDER_DETAILS)) {
			if (flag.equals(AcademyPCAConstants.ATTR_ON_LOAD)) {
				Document inputXML;
				try {
					inputXML = XMLUtil
							.createDocument(AcademyPCAConstants.ELE_ACAD_LOS_REST);
					YRCApiContext context = new YRCApiContext();
					context
							.setApiName(AcademyPCAConstants.API_GET_LOS_RESTRICTION_LIST_SERVICE);
					context
							.setFormId(AcademyPCAConstants.FORM_ID_FULFILLMENT_OPTIONS_CONTEXT);
					context.setInputXml(inputXML);
					callApi(context);
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}

			}

		}
	}

	@Override
	public void handleApiCompletion(YRCApiContext ctx) {
		if (ctx.getApiName().equals(
				AcademyPCAConstants.API_GET_LOS_RESTRICTION_LIST_SERVICE)) {
			Document outputXML = ctx.getOutputXml();
			setExtentionModel(
					AcademyPCAConstants.ATTR_EXTN_LOS_RESTRICTION_MODEL,
					outputXML.getDocumentElement());
			flag = "None";
			Element eleOrderDetails = getModel(AcademyPCAConstants.ATTR_GET_SALES_ORDER_DETAILS);
			pricingPromoPanelBehavior1
					.callExternalPanelFromCFOScreen(eleOrderDetails);
		}
	}

	public boolean preCommand(YRCApiContext apiContext) {

		String strApiName = apiContext.getApiName();
		Element eleChangeOrderInput = null;
		Element eleCurrentOrderLine = null;
		Element eleOrigSalesOrderLine = null;
		NodeList nListOrderLines = null;
		String strShipAlone = null;
		String strExtnWhiteGloveEligible = null;
		String strItemId = null;
		String strCurrrentOrderLineKey = null;
		String strPrimeLineNo = null;
		int iNoOfOrderLines = 0;

		YRCPlatformUI.trace("########  Form id inside preCommand ########"
				+ this.getFormId());

		//YRCPlatformUI.trace("########  API Name is ########" + strApiName);

		// Start fix for 4504 - Prevent unScheduleOrder API call
		String[] apiNames0 = apiContext.getApiNames();
		int arrLength = 0;
		boolean reqInputChanges = false;		
		for(int i=0; i<apiNames0.length; i++){
			if(!apiNames0[i].equals("unScheduleOrder"))
				arrLength++;
			if(apiNames0[i].equals("unScheduleOrder"))
				reqInputChanges = true;
		}
		/*
		 * Start Fix Bug 8436
		 */
		String[] apiNames = apiContext.getApiNames();
		String[] updateApiNames = new String[arrLength]; // changeOrder API and manageStopDeliveryRequest API
		Document[] updateInputDocs = new Document[arrLength];
		int j=0;
		for(int indx=0; indx<apiNames.length; indx++){
			String apiName = apiNames[indx];
			YRCPlatformUI.trace(" API Name at index : "+indx+" is : "+apiName);
			if(!apiName.equals("unScheduleOrder")){
				if(apiName.equals(AcademyPCAConstants.ATTR_CHANGE_ORDER)){


					if (YRCPlatformUI.isTraceEnabled())
						YRCPlatformUI
								.trace("########  Inside preCommand for changeOrder"
										+ apiName);
					Document changeOrderDoc = apiContext.getInputXmls()[indx];
					eleChangeOrderInput = changeOrderDoc.getDocumentElement();
					if (isDirty()) {
						eleChangeOrderInput.setAttribute("ExtnIsPageDirty", "Y");
					}
					eleChangeOrderInput.setAttribute("AcademyInvokedFrom",
							"FULFILLMENT_OPTIONS");

					if (YRCPlatformUI.isTraceEnabled()) {
						if (!YRCPlatformUI.isVoid(eleChangeOrderInput))
							YRCPlatformUI
									.trace("########  Inside preCommand changeOrder input is"
											+ YRCXmlUtils
													.getString(eleChangeOrderInput));
					}
					Element eleSalesOrderDetails = getModel("getSalesOrderDetails");
					if (!YRCPlatformUI.isVoid(eleSalesOrderDetails)) {
						if (YRCPlatformUI.isTraceEnabled()) {
							if (!YRCPlatformUI.isVoid(eleSalesOrderDetails))
								YRCPlatformUI
										.trace("########  Inside preCommand SalesOrderDetails model"
												+ YRCXmlUtils
														.getString(eleSalesOrderDetails));
						}
					}
					if (!YRCPlatformUI.isVoid(eleChangeOrderInput)) {
						if (YRCPlatformUI.isTraceEnabled())
							YRCPlatformUI
									.trace("########  Inside preCommand changeOrder input is"
											+ YRCXmlUtils
													.getString(eleChangeOrderInput));

						try {
							nListOrderLines = XPathUtil.getNodeList(
									eleChangeOrderInput,
									AcademyPCAConstants.XPATH_ORDERLINES_ORDERLINE);
							if (!YRCPlatformUI.isVoid(nListOrderLines))
								iNoOfOrderLines = nListOrderLines.getLength();
							for (int i = 0; i < iNoOfOrderLines; i++) {
								eleCurrentOrderLine = (Element) nListOrderLines.item(i);
								eleCurrentOrderLine.removeAttribute("ShipNode");
								eleCurrentOrderLine.removeAttribute("CarrierServiceCode");
								strCurrrentOrderLineKey = eleCurrentOrderLine
										.getAttribute(AcademyPCAConstants.ATTR_ORDER_LINE_KEY);
								eleOrigSalesOrderLine = (Element) XPathUtil.getNode(
										eleSalesOrderDetails,
										"OrderLines/OrderLine[@OrderLineKey='"
												+ strCurrrentOrderLineKey + "']");
								strItemId = XPathUtil.getString(eleOrigSalesOrderLine,
										AcademyPCAConstants.XPATH_ITEM_ITEMID);
								strPrimeLineNo = eleOrigSalesOrderLine
										.getAttribute(AcademyPCAConstants.ATTR_PRIME_LINE_NO);
								if (!YRCPlatformUI.isVoid(eleOrigSalesOrderLine)) {
									strShipAlone = XPathUtil.getString(
											eleOrigSalesOrderLine,
											AcademyPCAConstants.XPATH_ITEM_SHIP_ALONE);
									strExtnWhiteGloveEligible = XPathUtil.getString(
											eleOrigSalesOrderLine,
											"ItemDetails/Extn/@ExtnWhiteGloveEligible");
								}
								if (!YRCPlatformUI.isVoid(strShipAlone)) {
									/** *****Start CR 38 ******** */
									if (!YRCPlatformUI
											.isVoid(strExtnWhiteGloveEligible)) {
										if ((strShipAlone
												.equals(AcademyPCAConstants.ATTR_Y))
												&& (strExtnWhiteGloveEligible
														.equals(AcademyPCAConstants.ATTR_N))) {
											/** *****End CR 38 ******** */
											if (!YRCPlatformUI.isVoid(strPrimeLineNo))
												eleCurrentOrderLine
														.setAttribute(
																AcademyPCAConstants.ATTR_PACK_LIST_TYPE,
																strPrimeLineNo);
										}
									}
									else {
										YRCPlatformUI
												.trace("######  WhiteGloveEligible Attribute is null or blank #####");
									}
								} else {
									YRCPlatformUI
											.trace("######  ShipAlone Attribute is null or blank #####");
								}

							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						/* Bug Fix - Zubair */
						/*
						 * Loop through getSalesOrderDetails to validate the person ship
						 * to key. If the orderlines have different PersonInfoKey then
						 * error needs to be thrown
						 */
						try {
							boolean validAddress = evaluateShippingAddresses(eleSalesOrderDetails,apiContext);
							if(!validAddress)
								return validAddress;
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						/* Bug Fix - Zubair */
					}
					YRCPlatformUI.trace("final input xml to changeOrder API is :\n"+YRCXmlUtils.getString(changeOrderDoc));
					if(reqInputChanges){
						updateApiNames[j]=apiNames[indx];
						updateInputDocs[j]=changeOrderDoc;
						j=j+1;
					}					
				}else if(reqInputChanges){
					updateApiNames[j]=apiNames[indx];
					updateInputDocs[j]=apiContext.getInputXmls()[indx];
					j=j+1;
				}				
			}
		}
		if(reqInputChanges){
			apiContext.setApiNames(updateApiNames);
			apiContext.setInputXmls(updateInputDocs);
		}
		// End of #4504
		/*
		 * End Fix Bug 8436
		 */
		return super.preCommand(apiContext);
	}

	private boolean evaluateShippingAddresses(Element eleGetSalesOrderDetails,
			YRCApiContext ctx) throws Exception {
		try {

			NodeList nListOrderLines = XPathUtil.getNodeList(
					eleGetSalesOrderDetails,
					AcademyPCAConstants.XPATH_ORDERLINES_ORDERLINE);
			String strPersonInfoKey = null;
			int iNoOfOrderLines = 0;
			if (!YRCPlatformUI.isVoid(nListOrderLines))
				iNoOfOrderLines = ((NodeList) nListOrderLines).getLength();
			Set<String> hSet = new HashSet<String>();
			boolean callEvaluate = false;
			for (int j = 0; j < iNoOfOrderLines; j++) {
				Element eleCurrentOrderLine = (Element) ((NodeList) nListOrderLines)
						.item(j);
				// As paret of #4504 fix - Prevent the cancelled Line Item for same Ship To Address
				if(eleCurrentOrderLine.getAttribute("Status").equals("Cancelled"))
					continue;
				
				YRCPlatformUI.trace(XMLUtil.getElementXMLString(eleCurrentOrderLine));
				strPersonInfoKey = XPathUtil.getString(eleCurrentOrderLine,
						AcademyPCAConstants.XPATH_PERSONINFO_PERSONINFOKEY);
				String strGiftCardItem = XPathUtil.getString(
						eleCurrentOrderLine,
						AcademyPCAConstants.XPATH_ITEM_GIFT_CARD);
				String strExtnIsPOBoxAddress = XPathUtil.getString(
						eleCurrentOrderLine,
						AcademyPCAConstants.XPATH_PERSONINFO_PO_BOX);
				String strCarrierServiceCode = XPathUtil.getString(
						eleCurrentOrderLine, "@CarrierServiceCode");

				if ("".equals(strPersonInfoKey)) {
					YRCPlatformUI.showError(AcademyPCAConstants.ATTR_ERROR,
							"Make sure all orderlines have same address");
					return false;
				}
				hSet.add(strPersonInfoKey);

				if (((AcademyPCAConstants.ATTR_Y).equals(strGiftCardItem))
						&& (AcademyPCAConstants.ATTR_Y)
								.equals(strExtnIsPOBoxAddress)) {
					YRCPlatformUI
							.showError(AcademyPCAConstants.ATTR_ERROR,
									"Order containing Gift Card Item cannot be shipped to PO BOX Address");
					return false;
				}

				if ("".equals(strCarrierServiceCode)) {
					eleCurrentOrderLine.setAttribute("Checked",
							AcademyPCAConstants.ATTR_Y);
					callEvaluate = true;
				}
			}
			/*
			 * Check if the person info key is unique across orderlines, if not
			 * throw error
			 */
			if (hSet.size() > 1) {
				YRCPlatformUI.showError(AcademyPCAConstants.ATTR_ERROR,
						"Make sure all orderlines have same address");
				return false;
			}

			repopulateModel(AcademyPCAConstants.ATTR_GET_SALES_ORDER_DETAILS);
			setFieldValue(AcademyPCAConstants.ATTR_CMB_SHIP_TO_ADDRESS,
					strPersonInfoKey);

			/*
			 * check if order lines that are selected have a valid Level Of
			 * service. If yes, ask the user to select the LOS
			 */
			if (callEvaluate == true) {
				evaluateLOS(strPersonInfoKey);
				YRCPlatformUI
						.setMessage("Please Click on Apply to select the Level Of Service");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public void setPricingPromoPanelBehavior(
			AcademyPricingPromoPanelBehavior pricingPromoPanelBehavior1) {
		this.pricingPromoPanelBehavior1 = pricingPromoPanelBehavior1;

	}
}
