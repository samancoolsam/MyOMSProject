package com.academy.ecommerce.sterling.fulfillmentSummary.extn;

/**
 * Created on May 26,2009
 *
 */

import java.util.ArrayList;

import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCXmlUtils;

/**
 * @author sahmed Copyright © 2005-2008 Sterling Commerce, Inc. All Rights
 *         Reserved.
 */
public class AcademyGiftShipPopUpExtnBehavior extends YRCExtentionBehavior {
	public static final String FORM_ID = AcademyPCAConstants.FORM_ID_GIFT_POP_UP;

	private String sRecipientName = null;

	private String sRecipientMessage = null;

	private String sCmbRecipientMessages = null;

	private Element InstructionType = null;

	/**
	 * This method initializes the behavior class.
	 */
	public void init() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				YRCDesktopUI.getCurrentPage().getShell().setSize(400, 250);
			}
		});
		setFieldValue(AcademyPCAConstants.ATTR_RAD_SET_RECIPIENT,
				AcademyPCAConstants.ATTR_Y);

		disableField(AcademyPCAConstants.ATTR_EXTN_TEXT_RECIPIENT_NAME);
		disableField(AcademyPCAConstants.ATTR_EXTN_TEXT_RECIPIENT_MESSAGE);
		disableField(AcademyPCAConstants.ATTR_EXTN_CMB_MESSAGES);
		setFieldValue(AcademyPCAConstants.ATTR_EXTN_CMB_MESSAGES, "");
		YRCApiContext ctx = new YRCApiContext();
		ctx.setApiName(AcademyPCAConstants.API_GET_COMMONCODELIST);
		ctx.setFormId(FORM_ID);
		Document getCommonCodeListInput = YRCXmlUtils
				.createDocument(AcademyPCAConstants.ATTR_COMMON_CODE);
		getCommonCodeListInput.getDocumentElement().setAttribute(
				AcademyPCAConstants.ATTR_CODE_TYPE,
				AcademyPCAConstants.ATTR_GIFT_MESSAGE);
		if (YRCPlatformUI.isTraceEnabled()) {
			YRCPlatformUI.trace("Common Code List For Code Type Gift Message"
					+ YRCXmlUtils.getString(getCommonCodeListInput));
		}
		ctx.setInputXml(getCommonCodeListInput);
		callApi(ctx);

		Document docInstructionTypeModel = YRCXmlUtils
				.createDocument(AcademyPCAConstants.ATTR_INSTRUCTION_TYPE);
		docInstructionTypeModel.getDocumentElement().setAttribute(
				AcademyPCAConstants.ATTR_INSTRUCTION_TYPE,
				AcademyPCAConstants.ATTR_GIFT);
		setExtentionModel(AcademyPCAConstants.ATTR_EXTN_TARGET_MODEL,
				docInstructionTypeModel.getDocumentElement());

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
		setFieldValue(AcademyPCAConstants.ATTR_EXTN_TEXT_RECIPIENT_MESSAGE,
				fieldValue);
		super.validateComboField(fieldName, fieldValue);
	}

	/**
	 * Method called when a button is clicked.
	 */
	public YRCValidationResponse validateButtonClick(String fieldName) {
		if (fieldName.equals(AcademyPCAConstants.ATTR_RAD_SET_RECIPIENT)) {
			YRCPlatformUI.trace("Check for Button Click: " + fieldName);
			if (AcademyPCAConstants.ATTR_Y
					.equals(getFieldValue(AcademyPCAConstants.ATTR_RAD_SET_RECIPIENT))) {
				enableField(AcademyPCAConstants.ATTR_EXTN_TEXT_RECIPIENT_NAME);
				enableField(AcademyPCAConstants.ATTR_EXTN_TEXT_RECIPIENT_MESSAGE);
				enableField("extn_CmbRecipientMessages");
				setFieldValue(
						AcademyPCAConstants.ATTR_EXTN_TEXT_RECIPIENT_NAME,
						sRecipientName);
				setFieldValue(
						AcademyPCAConstants.ATTR_EXTN_TEXT_RECIPIENT_MESSAGE,
						sRecipientMessage);
				setFieldValue(AcademyPCAConstants.ATTR_EXTN_CMB_MESSAGES,
						sCmbRecipientMessages);

			}
		} else {
			if (fieldName.equals(AcademyPCAConstants.ATTR_RAD_CLEAR_RECIPIENT)) {
				YRCPlatformUI.trace("Check for Button Click: " + fieldName);
				if (AcademyPCAConstants.ATTR_N
						.equals(getFieldValue(AcademyPCAConstants.ATTR_RAD_CLEAR_RECIPIENT))) {
					disableField(AcademyPCAConstants.ATTR_EXTN_TEXT_RECIPIENT_NAME);
					disableField(AcademyPCAConstants.ATTR_EXTN_TEXT_RECIPIENT_MESSAGE);
					disableField(AcademyPCAConstants.ATTR_EXTN_CMB_MESSAGES);
					sRecipientName = getFieldValue(AcademyPCAConstants.ATTR_EXTN_TEXT_RECIPIENT_NAME);
					sRecipientMessage = getFieldValue(AcademyPCAConstants.ATTR_EXTN_TEXT_RECIPIENT_MESSAGE);
					sCmbRecipientMessages = getFieldValue(AcademyPCAConstants.ATTR_EXTN_CMB_MESSAGES);
					setFieldValue(
							AcademyPCAConstants.ATTR_EXTN_TEXT_RECIPIENT_NAME,
							"");
					setFieldValue(
							AcademyPCAConstants.ATTR_EXTN_TEXT_RECIPIENT_MESSAGE,
							"");
					setFieldValue(AcademyPCAConstants.ATTR_EXTN_CMB_MESSAGES,
							"");
				}

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
		return super.getExtendedTableBindingData(tableName, tableColumnNames);
	}

	public void handleApiCompletion(YRCApiContext ctx) {
		if (AcademyPCAConstants.API_GET_COMMONCODELIST.equals(ctx.getApiName())) {
			Document commonCodeListDoc = ctx.getOutputXml();
			if (YRCPlatformUI.isTraceEnabled()) {
				YRCPlatformUI
						.trace("######Output for Gift Common Codes##########"
								+ YRCXmlUtils.getString(commonCodeListDoc));
			}
			setExtentionModel(AcademyPCAConstants.ATTR_EXTN_OUTPUT_COMMON_CODE,
					commonCodeListDoc.getDocumentElement());
		}

	}

	@Override
	public void postSetModel(String arg0) {
		if (arg0.equals(AcademyPCAConstants.ATTR_MODEL_FOR_INITIALIZE)) {
			registerStaticTargetBinding(
					"targetModel:OrderLine/Instructions/Instruction/@InstructionType",
					"Extn_targetModel:InstructionType/@InstructionType");
		}
		super.postSetModel(arg0);
	}

}
