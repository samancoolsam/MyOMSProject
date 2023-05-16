package com.academy.ecommerce.sterling.shipmentInquiry.extn;

/**
 * Created on Mar 07,2012
 *
 */

import java.util.ArrayList;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;
import com.yantra.yfc.rcp.YRCXPathUtils;
import com.yantra.yfc.rcp.YRCXmlUtils;

/**
 * @author 279651 Copyright © 2005-2008 Sterling Commerce, Inc. All Rights
 *         Reserved.
 */
public class AcademyShipmentInquiryWizardExtnBehavior extends
		YRCWizardExtensionBehavior {

	private Element carrierListElem;

	/**
	 * This method initializes the behavior class.
	 */
	public void init() {
		// TODO: Write behavior init here.
	}

	public String getExtnNextPage(String currentPageId) {
		// TODO
		return null;
	}

	public IYRCComposite createPage(String pageIdToBeShown) {
		// TODO
		return null;
	}

	public void pageBeingDisposed(String pageToBeDisposed) {
		// TODO
	}

	/**
	 * Called when a wizard page is about to be shown for the first time.
	 * 
	 */
	public void initPage(String pageBeingShown) {
		// TODO
	}

	/**
	 * Method for validating the text box.
	 */
	public YRCValidationResponse validateTextField(String fieldName,
			String fieldValue) {
		// TODO Validation required for the following controls.

		// TODO Create and return a response.
		return super.validateTextField(fieldName, fieldValue);
	}

	/**
	 * Method for validating the combo box entry.
	 */
	public void validateComboField(String fieldName, String fieldValue) {
		// TODO Validation required for the following controls.

		// TODO Create and return a response.
		super.validateComboField(fieldName, fieldValue);
	}

	/**
	 * Method called when a button is clicked.
	 */
	public YRCValidationResponse validateButtonClick(String fieldName) {
		// TODO Validation required for the following controls.

		// TODO Create and return a response.
		return super.validateButtonClick(fieldName);
	}

	@Override
	public void postCommand(YRCApiContext arg0) {
		if ("getSalesOrderDetails".equals(arg0.getApiName())) {
			if (!YRCPlatformUI.isVoid(arg0.getOutputXmls()[3]
					.getDocumentElement())) {
				this.carrierListElem = arg0.getOutputXmls()[3]
						.getDocumentElement();
			}
		}

		super.postCommand(arg0);
	}

	@Override
	public void postSetModel(String arg0) {
		// TODO Auto-generated method stub
		if ("Order".equalsIgnoreCase(arg0)) {
			Element eleOrder = this.getModel("Order");
			if (!YRCPlatformUI.isVoid(eleOrder)) {
				NodeList nlContainerList = eleOrder
						.getElementsByTagName("Container");
				for (int i = 0; i < nlContainerList.getLength(); i++) {
					Element eleContainer = (Element) nlContainerList.item(i);
					if (!YRCPlatformUI.isVoid(eleContainer)) {
						Element eleShipment = YRCXmlUtils.getChildElement(
								eleContainer, "Shipment");
						if (!YRCPlatformUI.isVoid(eleShipment)) {
							String carrier = eleShipment
									.getAttribute("CarrierServiceCode");
							String scac = eleShipment.getAttribute("SCAC");
							carrier = (String) YRCXPathUtils
									.evaluate(
											this.carrierListElem,
											(new StringBuilder())
													.append(
															"/CarrierServiceList/CarrierService[@CarrierServiceCode=\"")
													.append(carrier)
													.append(
															"\"]/@CarrierServiceDesc")
													.toString(),
											XPathConstants.STRING);
							String scacNCarrier = "";
							if (!YRCPlatformUI.isVoid(scac)) {
								scacNCarrier = ((new StringBuilder()).append(
										scac).append(" - ").append(carrier))
										.toString();
							} else {
								scacNCarrier = carrier;
							}
							eleContainer.setAttribute("ScacNCarrier",
									scacNCarrier);
						}
					}
				}
			}
		}
		super.postSetModel(arg0);
	}

	/**
	 * Method called when a link is clicked.
	 */
	public YRCValidationResponse validateLinkClick(String fieldName) {
		// TODO Validation required for the following controls.

		// TODO Create and return a response.
		return super.validateLinkClick(fieldName);
	}

	/**
	 * Create and return the binding data for advanced table columns added to
	 * the tables.
	 */
	public YRCExtendedTableBindingData getExtendedTableBindingData(
			String tableName, ArrayList tableColumnNames) {
		// Create and return the binding data definition for the table.

		// The defualt super implementation does nothing.
		return super.getExtendedTableBindingData(tableName, tableColumnNames);
	}
}
