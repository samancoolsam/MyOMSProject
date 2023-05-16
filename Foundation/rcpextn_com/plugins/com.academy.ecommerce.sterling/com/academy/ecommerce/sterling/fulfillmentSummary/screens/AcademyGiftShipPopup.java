/**
 * 
 */
package com.academy.ecommerce.sterling.fulfillmentSummary.screens;

// Eclipse Imports
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Element; // Sterling Imports
import com.academy.ecommerce.sterling.common.IYCDObserver;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.IYRCPanelHolder;
import com.yantra.yfc.rcp.YRCButtonBindingData;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCPlatformUI;

// Project Imports
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;

/**
 * @author sahmed
 * 
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.common.IYCDObserver;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.IYRCPanelHolder;
import com.yantra.yfc.rcp.YRCButtonBindingData;
import com.yantra.yfc.rcp.YRCComboBindingData;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCTextBindingData;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author sahmed
 * 
 */

public class AcademyGiftShipPopup extends Composite implements IYRCComposite {

	private Composite pnlRoot = null;
	private Composite pnlGiftShipContainer;
	private ScrolledComposite scrolledComposite;
	private Composite pnlGiftShipContainerChild;
	private AcademyGiftShipPopupBehavior myBehavior;
	// private static final String FORM_ID = "";
	private static final String FORM_ID = "com.academy.ecommerce.sterling.fulfillmentSummary.screens.AcademyiftShipPopUp";

	private Button rbtnSetRecipient = null;

	private Button rbtnClearRecipient = null;

	private boolean isGiftRecipient;

	private Button btnApply = null;
	private Button btnClose = null;
	private Element targetModel;
	private Composite rbtnComposite = null;
	private Composite btnComposite = null;
	private boolean isClearBtnSelected;
	private IYCDObserver observer;
	private Label label_GiftMessage = null;
	private Text textArea_GiftMessage = null;
	private Composite pnlGiftMsg = null;
	private String GiftMessage;
	private String GiftName;
	private Label label_GiftName;
	private Text textArea_GiftName;
	private Combo combo_GiftMessage;
	private String ComboGiftMessage;

	public AcademyGiftShipPopup(Composite parent, int style, Object input) {
		super(parent, style);
		initialize();
		setBindingforComponents();
		boolean giftFlag = true;
		myBehavior = new AcademyGiftShipPopupBehavior(this, FORM_ID, giftFlag,
				input);
		myBehavior.callCommonCodes();
		
	}

	private void initialize() {
		createRootPanel();
		this.setLayout(new FillLayout());
		setSize(new Point(220, 115));
	}

	public String getFormId() {
		return FORM_ID;
	}

	public String getHelpId() {
		return null;
	}

	public IYRCPanelHolder getPanelHolder() {
		return null;
	}

	public Composite getRootPanel() {
		return pnlRoot;
	}

	private void createRootPanel() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.makeColumnsEqualWidth = true;
		pnlRoot = new Composite(this, SWT.NONE);
		pnlRoot.setLayout(gridLayout);
		pnlRoot.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "TaskComposite");
		createComposite();
	}

	private void createComposite() {
		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.FILL;
		gridData5.verticalAlignment = GridData.FILL;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 1;
		gridLayout1.horizontalSpacing = 0;
		gridLayout1.verticalSpacing = 0;
		gridLayout1.marginWidth = 0;
		gridLayout1.marginHeight = 0;
		GridData gridData16 = new org.eclipse.swt.layout.GridData();
		gridData16.horizontalAlignment = GridData.FILL;
		gridData16.grabExcessHorizontalSpace = true;
		gridData16.grabExcessVerticalSpace = true;
		gridData16.verticalSpan = 1;
		gridData16.horizontalIndent = 0;
		gridData16.verticalAlignment = GridData.FILL;
		pnlGiftShipContainer = new Composite(getRootPanel(), SWT.NONE);
		pnlGiftShipContainer.setLayoutData(gridData16);
		pnlGiftShipContainer.setLayout(gridLayout1);
		pnlGiftShipContainer.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE,
				"TaskComposite");
		createScrolledComposite();
		createComposite1();
	}

	private void createScrolledComposite() {
		GridLayout gridlayout2 = new GridLayout();
		gridlayout2.marginHeight = 0;
		gridlayout2.marginWidth = 0;
		gridlayout2.verticalSpacing = 0;
		gridlayout2.horizontalSpacing = 0;
		gridlayout2.numColumns = 1;
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		scrolledComposite = new ScrolledComposite(pnlGiftShipContainer,
				SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayout(gridlayout2);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setLayoutData(gridData);
		scrolledComposite.setVisible(true);
		scrolledComposite.setData(YRCConstants.YRC_CONTROL_NAME,
				"scrolledComposite");
		createPnlGiftShipContainerChild();
		scrolledComposite.setContent(pnlGiftShipContainerChild);
		scrolledComposite.setMinSize(pnlGiftShipContainerChild.computeSize(
				SWT.DEFAULT, SWT.DEFAULT, true));
	}

	private void createPnlGiftShipContainerChild() {

		GridLayout gridLayout7 = new GridLayout();
		gridLayout7.marginWidth = 2;
		gridLayout7.marginHeight = 2;
		gridLayout7.marginWidth = 2;
		gridLayout7.verticalSpacing = 5;
		gridLayout7.numColumns = 1;
		gridLayout7.horizontalSpacing = 5;
		GridData gridData16 = new org.eclipse.swt.layout.GridData();
		gridData16.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData16.grabExcessHorizontalSpace = true;
		gridData16.grabExcessVerticalSpace = true;
		gridData16.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		pnlGiftShipContainerChild = new Composite(scrolledComposite, SWT.NONE);
		pnlGiftShipContainerChild.setLayoutData(gridData16);
		pnlGiftShipContainerChild.setLayout(gridLayout7);
		createComposite2();
		// Label filler1 = new Label(pnlGiftShipContainerChild, SWT.NONE);

	}

	public void setBindingforComponents() {
		pnlRoot.setData(YRCConstants.YRC_CONTROL_NAME, "pnlRoot");
		pnlGiftShipContainer.setData(YRCConstants.YRC_CONTROL_NAME,
				"pnlGiftShipContainer");
		scrolledComposite.setData(YRCConstants.YRC_CONTROL_NAME,
				"giftShipScrolledComposite");
		pnlGiftShipContainerChild.setData(YRCConstants.YRC_CONTROL_NAME,
				"pnlGiftShipContainerChild");
		btnComposite.setData(YRCConstants.YRC_CONTROL_NAME, "btnComposite");
		rbtnComposite.setData(YRCConstants.YRC_CONTROL_NAME, "dataComposite");
		label_GiftMessage.setData(YRCConstants.YRC_CONTROL_NAME,
				"lblGiftMessage");
		// checkBoxGiftWrap.setData(YRCConstants.YRC_CONTROL_NAME,"checkBoxGiftWrap");
		pnlGiftMsg.setData(YRCConstants.YRC_CONTROL_NAME, "pnlGiftMsg");
		// lblGiftWrapError.setData(YRCConstants.YRC_CONTROL_NAME,"lblGiftWrapError");

		YRCButtonBindingData bbd = new YRCButtonBindingData();
		bbd.setTargetBinding("targetModel:/OrderLine/@GiftFlag");
		bbd.setName("rbtSetRecipient");
		bbd.setCheckedBinding(AcademyPCAConstants.ATTR_Y);
		bbd.setUnCheckedBinding(AcademyPCAConstants.ATTR_N);
		rbtnSetRecipient.setData(YRCConstants.YRC_BUTTON_BINDING_DEFINATION,
				bbd);

		bbd = new YRCButtonBindingData();
		bbd.setTargetBinding("targetModel:/OrderLine/@GiftFlag");
		bbd.setName("rbtnClearRecipient");
		bbd.setCheckedBinding(AcademyPCAConstants.ATTR_N);
		bbd.setUnCheckedBinding(AcademyPCAConstants.ATTR_Y);
		rbtnClearRecipient.setData(YRCConstants.YRC_BUTTON_BINDING_DEFINATION,
				bbd);

		YRCButtonBindingData bbd1 = new YRCButtonBindingData();
		bbd1.setName("btnApply");
		bbd1.setActionId(AcademyPCAConstants.GIFT_SHIP_APPLY_ACTIONID);
		bbd1.setActionHandlerEnabled(true);
		btnApply.setData(YRCConstants.YRC_BUTTON_BINDING_DEFINATION, bbd1);

		bbd1 = new YRCButtonBindingData();
		bbd1.setName("btnClose");
		bbd1.setActionId(AcademyPCAConstants.GIFT_SHIP_CLOSE_ACTIONID);
		bbd1.setActionHandlerEnabled(true);
		btnClose.setData(YRCConstants.YRC_BUTTON_BINDING_DEFINATION, bbd1);

		YRCTextBindingData txttextArea_GiftName = new YRCTextBindingData();
		txttextArea_GiftName.setName("textArea_GiftName");
		txttextArea_GiftName
				.setTargetBinding("targetModel:/OrderLine/Instructions/Instruction/Extn/@ExtnInstructionName;targetModel:/OrderLine/Notes/Note/@ContactUser");
		txttextArea_GiftName.setDataType("PromotionId");
		txttextArea_GiftName
				.setSourceBinding("ModelForInitialize:OrderLine/Instructions/Instruction/@ExtnInstructionName;OrderLine/Notes/Note/@ContactUser");
		textArea_GiftName.setData(YRCConstants.YRC_TEXT_BINDING_DEFINATION,
				txttextArea_GiftName);

		YRCTextBindingData txttextArea_GiftMessage = new YRCTextBindingData();
		txttextArea_GiftMessage.setName("textArea_GiftMessage");
		txttextArea_GiftMessage
				.setTargetBinding("targetModel:/OrderLine/Instructions/Instruction/@InstructionText;targetModel:/OrderLine/Notes/Note/@NoteText");
		txttextArea_GiftMessage.setDataType("PromotionId");
		txttextArea_GiftMessage
				.setSourceBinding("ModelForInitialize:OrderLine/Instructions/Instruction/@InstructionText;ModelForInitialize:OrderLine/Notes/Note/@NoteText");
		textArea_GiftMessage.setData(YRCConstants.YRC_TEXT_BINDING_DEFINATION,
				txttextArea_GiftMessage);

		YRCComboBindingData comboGiftMessages = new YRCComboBindingData();
		comboGiftMessages.setName("comboGiftMessages");
		comboGiftMessages
				.setSourceBinding(AcademyPCAConstants.XPATH_COMMON_CODE_VALUE);
		comboGiftMessages.setListBinding(AcademyPCAConstants.XPATH_COMMON_CODE);
		comboGiftMessages
				.setCodeBinding(AcademyPCAConstants.ATTR_CODE_SHORT_DESCRIPTION);
		comboGiftMessages.setBundleDriven(true);
		comboGiftMessages
				.setDescriptionBinding(AcademyPCAConstants.ATTR_CODE_LONG_DESCRIPTION);
		comboGiftMessages
				.setTargetBinding(AcademyPCAConstants.XPATH_COMMON_CODE_VALUE);
		combo_GiftMessage.setFocus();
		// combo_Title.select(1);
		combo_GiftMessage.setData(YRCConstants.YRC_COMBO_BINDING_DEFINATION,
				comboGiftMessages);
		combo_GiftMessage
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					private String ComboMessage;

					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						{
							ComboMessage = combo_GiftMessage.getText();
							textArea_GiftMessage.setText(ComboMessage);

						}
					}

				});
	}

	protected void setGiftItemsModel(Element targetModel) {
		this.targetModel = targetModel;
	}

	public Element getGiftItemsModel() {
		return this.targetModel;
	}

	public AcademyGiftShipPopupBehavior getBehavior() {
		return myBehavior;
	}

	/**
	 * This method initializes composite
	 * 
	 */
	private void createComposite2() {
		GridLayout gridLayout3 = new GridLayout();
		gridLayout3.numColumns = 1;
		GridData gridData7 = new GridData();
		gridData7.horizontalIndent = 5;
		GridData gridData4 = new GridData();
		gridData4.verticalAlignment = GridData.CENTER;
		gridData4.grabExcessHorizontalSpace = true;
		gridData4.grabExcessVerticalSpace = false;
		gridData4.horizontalAlignment = GridData.FILL;
		gridData4.verticalIndent = 4;
		GridData gridData1 = new GridData();
		gridData1.horizontalIndent = 5;
		rbtnComposite = new Composite(pnlGiftShipContainerChild, SWT.NONE);
		rbtnComposite.setLayoutData(gridData4);
		rbtnComposite.setLayout(gridLayout3);
		rbtnClearRecipient = new Button(rbtnComposite, SWT.RADIO);
		rbtnClearRecipient.setText(AcademyPCAConstants.ATTR_CLEAR_ITEM_AS_GIFT);
		rbtnClearRecipient.setLayoutData(gridData1);
		rbtnClearRecipient
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {

					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						if (rbtnClearRecipient.getSelection()) {
							GiftName = textArea_GiftName.getText();
							GiftMessage = textArea_GiftMessage.getText();
							ComboGiftMessage = combo_GiftMessage.getText();
							textArea_GiftName.setText("");
							textArea_GiftMessage.setText("");
							combo_GiftMessage.setText("");

						/*	if (checkBoxGiftWrap.isVisible()) {
								checkBoxGiftWrap.setSelection(false);
							}*/
							enableFields(pnlGiftMsg, false);
						}
					}

				});

		rbtnSetRecipient = new Button(rbtnComposite, SWT.RADIO);
		rbtnSetRecipient.setText(AcademyPCAConstants.ATTR_SET_ITEM_AS_GIFT);
		rbtnSetRecipient.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE,
				"TaskWhiteComposite");
		rbtnSetRecipient.setLayoutData(gridData7);
		rbtnSetRecipient.setEnabled(true);
		rbtnSetRecipient
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						if (rbtnSetRecipient.getSelection()) {
							enableFields(pnlGiftMsg, true);
							
							 textArea_GiftMessage.setText(GiftMessage);
							 textArea_GiftName.setText(GiftName);
							 combo_GiftMessage.setText(ComboGiftMessage);
							 //checkBoxGiftWrap.setEnabled(!giftWrapDisabled);
							 
						}
					}
				});
		createPnlGiftMsg();
		rbtnClearRecipient.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE,
				"TaskWhiteComposite");
		rbtnClearRecipient.setEnabled(true);
	}

	/**
	 * This method initializes composite1
	 * 
	 */
	private void createComposite1() {
		GridData gridData6 = new GridData();
		gridData6.horizontalAlignment = GridData.FILL;
		gridData6.verticalAlignment = GridData.CENTER;
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 2;
		gridLayout2.verticalSpacing = 5;
		gridLayout2.marginWidth = 5;
		gridLayout2.marginHeight = 5;
		gridLayout2.horizontalSpacing = 5;
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.FILL;
		gridData3.grabExcessHorizontalSpace = false;
		gridData3.verticalAlignment = GridData.END;
		gridData3.widthHint = 80;
		gridData3.heightHint = 25;
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.END;
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.verticalAlignment = GridData.END;
		gridData2.widthHint = 80;
		gridData2.heightHint = 25;
		btnComposite = new Composite(pnlGiftShipContainer, SWT.NONE);
		btnComposite.setLayout(gridLayout2);
		btnComposite.setLayoutData(gridData6);
		btnComposite.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE,
				"TaskComposite");
		btnApply = new Button(btnComposite, SWT.NONE);
		btnApply.setText("Apply");
		btnApply.setLayoutData(gridData2);
		btnClose = new Button(btnComposite, SWT.NONE);
		btnClose.setText("Close");
		btnClose.setLayoutData(gridData3);

	}

	protected boolean isSetGiftButtonSelected() {
		if (rbtnSetRecipient.getSelection()) {
			isGiftRecipient = true;
		} else
			isGiftRecipient = false;
		return isGiftRecipient;
	}

	protected boolean isClearGiftBbuttonSelected() {
		if (rbtnClearRecipient.getSelection()) {
			isClearBtnSelected = true;
		} else
			isClearBtnSelected = false;
		return isClearBtnSelected;

	}

	public void setObserver(IYCDObserver observer) {
		this.observer = observer;
	}

	public void setElement(Element element) {
		if (!YRCPlatformUI.isVoid(observer)) {
			observer.setElement(element);
		}
	}

	public Button getGiftRadioButton() {
		return rbtnSetRecipient;
	}

	/**
	 * This method initializes pnlGiftMsg
	 * 
	 */
	private void createPnlGiftMsg() {
		GridData gridData9 = new GridData();
		gridData9.horizontalIndent = 8;
		GridData gridData8 = new GridData();
		gridData8.horizontalAlignment = GridData.FILL;
		gridData8.grabExcessHorizontalSpace = true;
		gridData8.grabExcessVerticalSpace = true;
		gridData8.verticalAlignment = GridData.FILL;
		pnlGiftMsg = new Composite(rbtnComposite, SWT.NONE);
		pnlGiftMsg.setLayout(new GridLayout());
		pnlGiftMsg.setLayoutData(gridData8);
		GridData gridData22 = new GridData();
		gridData22.horizontalIndent = 8;
		GridData gridData23 = new GridData();
		gridData23.horizontalIndent = 9;
		gridData23.verticalIndent = 1;
		gridData23.grabExcessHorizontalSpace = true;
		gridData23.grabExcessVerticalSpace = false;
		gridData23.horizontalAlignment = GridData.FILL;
		gridData23.verticalAlignment = GridData.CENTER;
		gridData23.heightHint = 15;
		label_GiftName = new Label(pnlGiftMsg, SWT.NONE);
		label_GiftName.setText(AcademyPCAConstants.ATTR_RECIPIENT_NAME);
		label_GiftName.setLayoutData(gridData22);
		textArea_GiftName = new Text(pnlGiftMsg, SWT.NONE | SWT.BORDER);
		textArea_GiftName.setLayoutData(gridData23);
		GridData gridData21 = new GridData();
		gridData21.horizontalIndent = 135;
		gridData21.verticalIndent = 1;
		gridData21.grabExcessHorizontalSpace = true;
		gridData21.grabExcessVerticalSpace = false;
		gridData21.horizontalAlignment = GridData.FILL;
		gridData21.verticalAlignment = GridData.CENTER;
		gridData21.horizontalSpan = 3;
		gridData21.heightHint = 20;
		combo_GiftMessage = new Combo(pnlGiftMsg, SWT.NONE);
		combo_GiftMessage.setLayoutData(gridData21);
		GridData gridData18 = new GridData();
		gridData18.horizontalIndent = 8;
		GridData gridData19 = new GridData();
		gridData19.horizontalIndent = 9;
		gridData19.verticalIndent = 1;
		gridData19.grabExcessHorizontalSpace = true;
		gridData19.grabExcessVerticalSpace = false;
		gridData19.horizontalAlignment = GridData.FILL;
		gridData19.verticalAlignment = GridData.CENTER;
		gridData19.heightHint = 60;
		label_GiftMessage = new Label(pnlGiftMsg, SWT.NONE);
		label_GiftMessage.setText(AcademyPCAConstants.ATTR_RECIPIENT_MESSAGE);
		label_GiftMessage.setLayoutData(gridData18);
		textArea_GiftMessage = new Text(pnlGiftMsg, SWT.MULTI | SWT.WRAP
				| SWT.V_SCROLL | SWT.BORDER);
		textArea_GiftMessage.setLayoutData(gridData19);
/*		GridData gridData20 = new GridData();
		gridData20.verticalSpan = 0;
		gridData20.horizontalIndent = 8;
		checkBoxGiftWrap = new Button(pnlGiftMsg, SWT.CHECK);
		checkBoxGiftWrap.setText("Gift_Wrap_Item");
		checkBoxGiftWrap.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE,
				"TaskWhiteComposite");
		checkBoxGiftWrap.setLayoutData(gridData20);
		excludeField(checkBoxGiftWrap, true);
		lblGiftWrapError = new Label(pnlGiftMsg, SWT.NONE);
		lblGiftWrapError.setText("");
		lblGiftWrapError.setLayoutData(gridData9);
		lblGiftWrapError.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "Table");
		lblGiftWrapError.setVisible(false);
		excludeField(lblGiftWrapError, true);*/
	}

	private void enableFields(Composite composite, boolean enable) {
		if (!YRCPlatformUI.isVoid(composite)) {
			Control[] controls = composite.getChildren();
			if (!YRCPlatformUI.isVoid(controls))
				for (int i = 0; i < controls.length; i++) {
					if (controls[i] instanceof Composite) {
						enableFields((Composite) controls[i], enable);
					} else {
						controls[i].setEnabled(enable);
					}
				}

		}

	}

	void excludeField(Control ctrl, boolean exclude) {
		if (ctrl != null) {
			Object obj = ctrl.getLayoutData();
			if (obj == null) {
				obj = new GridData();
				ctrl.setLayoutData(obj);
			}
			ctrl.setVisible(!exclude);
			if (obj instanceof GridData) {
				((GridData) obj).exclude = exclude;
			} else if (obj instanceof RowData) {
				((RowData) obj).exclude = exclude;
			}
		}
	}
}
