package com.academy.ecommerce.sterling.orderEntry.addItems.extn;

/**
 * Created on May 10,2009
 *
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.eclipse.jface.viewers.deferred.SetModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList; // Sterling Imports
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCWizard;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;
import com.yantra.yfc.rcp.YRCXPathUtils;
import com.yantra.yfc.rcp.YRCXmlUtils;
import com.yantra.yfc.rcp.internal.YRCApiCaller;

// Package Imports
import com.academy.ecommerce.sterling.orderEntry.addItems.screens.AcademyPricingPromoPanelBehavior;
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;

/**
 * @author sahmed Copyright © 2005-2008 Sterling Commerce, Inc. All Rights
 *         Reserved.
 */
public class AcademyAddItemsExtnWizardBehavior extends
		YRCWizardExtensionBehavior {

	private Element eleOrderDetails = null;

	private Element eleItemList;

	private Element eleUpdateOrderDetails = null;

	private Composite parent;

	private int style;

	private AcademyPricingPromoPanelBehavior pricingPromoPanelBehavior = null;

	private boolean b=false;

	/**
	 * This method initializes the behavior class.
	 */
	public void init() {
		Document docItemListModel = YRCXmlUtils
				.createDocument(AcademyPCAConstants.ATTR_ORDER);
		Document docUpdateOrderCall = YRCXmlUtils
				.createDocument(AcademyPCAConstants.ATTR_ORDER);
		setExtentionModel(AcademyPCAConstants.ATTR_EXTN_ORDER_DETAILS,
				docItemListModel.getDocumentElement());
		setExtentionModel(AcademyPCAConstants.ATTR_EXTN_UPDATE_ORDER_DETAILS,
				docUpdateOrderCall.getDocumentElement());
	}

	public String getExtnNextPage(String currentPageId) {
		return null;
	}

	public IYRCComposite createPage(String pageIdToBeShown) {
		return null;
	}

	public void pageBeingDisposed(String pageToBeDisposed) {
	}

	/**
	 * Called when a wizard page is about to be shown for the first time.
	 * 
	 */
	public void initPage(String pageBeingShown) {
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
		if (fieldName.equals("btnUpdateOrder")) {
			b= true;
			Element eleUpdateOrderDetails = getModel("OrderDetails");
/*			YRCWizard currentPage = (YRCWizard) YRCDesktopUI.getCurrentPage();
			YRCBehavior childBehavior = (YRCBehavior) currentPage
					.getChildBehavior(AcademyPCAConstants.FORM_ID_ADD_ITEM_PROMOTION_PANEL);
			if (!YRCPlatformUI.isVoid(childBehavior)) {
				if (childBehavior instanceof AcademyPricingPromoPanelBehavior) {
					((AcademyPricingPromoPanelBehavior) childBehavior)
							.callExternalPanelBehaviorFromAddItemScreen(eleUpdateOrderDetails);
				}
			}*/
		}
		return super.validateButtonClick(fieldName);
	}

	@Override
	public void postSetModel(String strModel) {
		YRCPlatformUI
				.trace("########### Model in Order Entry Extension Behavior is ###"
						+ strModel);
		if ((AcademyPCAConstants.ATTR_ITEM_DETAILS).equals(strModel)) {
			YRCPlatformUI.trace("###########Inside Post Set Model....... ###"
					+ strModel);
		}
		if ((AcademyPCAConstants.ATTR_ORDER_DETAILS).equals(strModel)) {
			eleOrderDetails = getModel(AcademyPCAConstants.ATTR_ORDER_DETAILS);
			YRCPlatformUI.trace("###########Inside Post Set Model....... ###"
					+ eleOrderDetails);
		}

		if ((AcademyPCAConstants.ATTR_ORDER_LINES).equals(strModel)) {
			YRCPlatformUI.trace("########### Inside Post Set Model....... ###"
					+ strModel);
		}
		super.postSetModel(strModel);
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
		return super.getExtendedTableBindingData(tableName, tableColumnNames);
	}

	@Override
	public boolean preCommand(YRCApiContext apiContext) {

		String strApiName = apiContext.getApiName();

		/*
		 * to validate order line address to ensure all orderlines possess the
		 * same address
		 */
		if (strApiName.equals(AcademyPCAConstants.ATTR_GET_SALES_ORDER_DETAILS)) {
			Document inputDoc = apiContext.getInputXml();
			Element eleGetSalesOrderDetails = inputDoc.getDocumentElement();
			YRCPlatformUI.trace(
					"########Input XML to getSalesOrderDetails is:#####",
					XMLUtil.getElementXMLString(eleGetSalesOrderDetails));
		}
		/*
		 * Intercept changeOrderForPaymentMethod call, replace CreditCardNo
		 * value with Wallet ID (encrypted value)
		 */
		if (strApiName
				.equals(AcademyPCAConstants.CHANGE_ORDER_FOR_PAYMENT_METHOD)) {

			/*
			 * apiContext.getInputXmls() return input xml of MultiAPI call.
			 * First API being changeOrder and second api being
			 * confirmDraftOrder.
			 * 
			 */
			Document[] inputDoc = apiContext.getInputXmls();
			Document inputDocForEncriptService = inputDoc[0];
			/*
			 * Test input Doc, if has Credit_CARD as payment Type, then only
			 * invoke AcademyEncriptCreditCardInformation Service.
			 * 
			 */
			Node creditCardPaymentType = (Node) YRCXPathUtils
					.evaluate(
							inputDocForEncriptService.getDocumentElement(),
							"/Order/PaymentMethods/PaymentMethod[@PaymentType='CREDIT_CARD']",
							XPathConstants.NODE);
			if (!YRCPlatformUI.isVoid(creditCardPaymentType)) {
				YRCApiContext context = new YRCApiContext();
				YRCApiCaller syncApiCaller = new YRCApiCaller(context, true);
				context
						.setApiName(AcademyPCAConstants.ACADEMY_ENCRIPT_CC_COMMAND);
				context
						.setFormId(AcademyPCAConstants.ACADEMY_ENCRIPT_CC_COMMAND_FORMID);
				context.setInputXml(inputDocForEncriptService);
				syncApiCaller.invokeApi();
				inputDoc[0] = context.getOutputXml();
				apiContext.setInputXmls(inputDoc);
			}
		}

		if (strApiName.equals("changeOrder")) {

			Element orderDetails = getModel("OrderDetails");
			Document inputDoc = apiContext.getInputXml();
			if (isDirty()) {
				inputDoc.getDocumentElement().setAttribute("ExtnIsPageDirty",
						"Y");
			}
			String formID = apiContext.getFormId();
			if ("com.yantra.pca.ycd.rcp.tasks.orderEntry.wizards.YCDOrderEntryWizard"
					.equals(formID)) {
				inputDoc.getDocumentElement().setAttribute("OrderNo",
						orderDetails.getAttribute("OrderNo"));
				inputDoc.getDocumentElement().setAttribute(
						"AcademyInvokedFrom", "ADD_LINE_ITEMS");
			} else if ("com.yantra.pca.ycd.rcp.tasks.common.fulfillmentSummary.screens.YCDFulfillmentSummaryScreen"
					.equals(formID)) {
				inputDoc.getDocumentElement().setAttribute("ExtnIsPageDirty",
						"Y");
				inputDoc.getDocumentElement().setAttribute("OrderNo",
						orderDetails.getAttribute("OrderNo"));
				inputDoc.getDocumentElement().setAttribute(
						"AcademyInvokedFrom", "FULFILLMENT_SUMMARY");
			}
		}
		return super.preCommand(apiContext);
	}

	@Override
	public void postCommand(YRCApiContext ctx) {
		String[] strApiNames = ctx.getApiNames();
		for (int i = 0; i < strApiNames.length; i++) {
			YRCPlatformUI.trace("##### Inside Post Command #####"
					+ strApiNames[i]);
			if (strApiNames[i].equals(AcademyPCAConstants.ATTR_CHANGE_ORDER)) {
				Element eleUpdateOrderDetails = ctx.getOutputXml()
						.getDocumentElement();
				YRCPlatformUI
						.trace(
								"######This is the element model xml for changeOrder##########",
								YRCXmlUtils.getString(eleUpdateOrderDetails));
				setExtentionModel(
						AcademyPCAConstants.ATTR_EXTN_UPDATE_ORDER_DETAILS,
						eleUpdateOrderDetails);
				if(b){
					YRCWizard currentPage = (YRCWizard) YRCDesktopUI.getCurrentPage();
					YRCBehavior childBehavior = (YRCBehavior) currentPage
							.getChildBehavior(AcademyPCAConstants.FORM_ID_ADD_ITEM_PROMOTION_PANEL);
					if (!YRCPlatformUI.isVoid(childBehavior)) {
						if (childBehavior instanceof AcademyPricingPromoPanelBehavior) {
							((AcademyPricingPromoPanelBehavior) childBehavior)
									.callExternalPanelBehaviorFromAddItemScreen(eleUpdateOrderDetails);
						}
					}
					b=false;
				}
						}

		}
		super.postCommand(ctx);

	}

	public Element getUpdateModel() {
		repopulateModel(AcademyPCAConstants.ATTR_EXTN_UPDATE_ORDER_DETAILS);
		return eleUpdateOrderDetails;
	}

	public void setPricingPromoPanelBehavior(
			AcademyPricingPromoPanelBehavior myBehavior) {
		this.pricingPromoPanelBehavior = myBehavior;

	}

}
