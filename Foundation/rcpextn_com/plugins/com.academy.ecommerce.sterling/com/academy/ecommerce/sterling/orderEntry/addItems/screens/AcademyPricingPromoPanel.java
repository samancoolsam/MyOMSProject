package com.academy.ecommerce.sterling.orderEntry.addItems.screens;

import java.awt.Color;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.changeFulfillmentOptions.extn.AcademyChangeFulfillmentOptionsExtnBehavior;
import com.academy.ecommerce.sterling.orderEntry.addItems.extn.AcademyAddItemsExtnWizardBehavior;
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.AcademyScreenPanelUtils;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.IYRCPanelHolder;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCLabelBindingData;
import com.yantra.yfc.rcp.YRCPlatformUI;

public class AcademyPricingPromoPanel extends Composite implements
		IYRCComposite {

	public static final String FORM_ID = AcademyPCAConstants.FORM_ID_ADD_ITEM_PROMOTION_PANEL;
	private AcademyPricingPromoPanelBehavior myBehavior;
	ScrolledComposite scrllCmpst;
	private Label label1=null;
	private Composite root=null;
	
	
	public AcademyPricingPromoPanel(final Composite parent, int style,
			final YRCExtentionBehavior behavior) {
		
		super(parent, SWT.BORDER);
		createPnl(parent);
		myBehavior = new AcademyPricingPromoPanelBehavior(this, parent,FORM_ID,behavior);
		if(behavior instanceof AcademyAddItemsExtnWizardBehavior) {
			((AcademyAddItemsExtnWizardBehavior)behavior).setPricingPromoPanelBehavior(myBehavior);
		}
		
		if(behavior instanceof AcademyChangeFulfillmentOptionsExtnBehavior) {
			((AcademyChangeFulfillmentOptionsExtnBehavior)behavior).setPricingPromoPanelBehavior(myBehavior);
		}
	}

	public AcademyPricingPromoPanelBehavior getMyBehavior() {
		return myBehavior;
	}
	

	void createPnl(Composite parent) {
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.marginHeight=0;
		gridLayout1.marginWidth=0;
		gridLayout1.marginLeft=0;
		gridLayout1.marginRight=0;
		gridLayout1.marginBottom=0;
		this.setLayout(gridLayout1);
		Object x = this.getLayoutData();
		if(x instanceof GridData) {
			((GridData)x).widthHint=-1;
		}
		AcademyScreenPanelUtils.addGradientPanelHeader(this, "Promotions");
		scrllCmpst = new ScrolledComposite(this,SWT.V_SCROLL);
		scrllCmpst.setData(YRCConstants.YRC_CONTROL_NAME, "scrllCmpst");
		scrllCmpst.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "TaskWhiteComposite");

		GridData data1 = new GridData();
		data1.horizontalAlignment = SWT.FILL;
		data1.verticalAlignment = SWT.FILL;
		data1.grabExcessHorizontalSpace = true;
		data1.grabExcessVerticalSpace = true;
		scrllCmpst.setLayoutData(data1);
		scrllCmpst.setAlwaysShowScrollBars(true);

		scrllCmpst.setExpandHorizontal(true);
		scrllCmpst.setExpandVertical(true);

	    root = new Composite(scrllCmpst, SWT.NONE);
		root.setData(YRCConstants.YRC_CONTROL_NAME, "myRoot");
		root.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "TaskWhiteComposite");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns =2;
		gridLayout.marginHeight=0;
		gridLayout.marginWidth=0;
		gridLayout.marginBottom=0;
		gridLayout.marginLeft=0;
		root.setLayout(gridLayout);
		GridData data2 = new GridData();
		data2.grabExcessHorizontalSpace = true;
		data2.grabExcessVerticalSpace=true;
		data2.horizontalAlignment = GridData.CENTER;
		root.setData(data2);
		scrllCmpst.setContent(root);
		parent.layout(true, true);
		scrllCmpst.setMinSize(0, 400);
		parent.layout(true, true);
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
		return null;
	}

	public void createLabelForPromotions(int length, String str, String strItem) {
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment=GridData.FILL;
		data.verticalAlignment=GridData.CENTER;
		data.verticalSpan= 1;
		data.horizontalSpan=1;
		for (int loopIndex = 0; loopIndex < length; loopIndex++) {
			Label label = new Label(root, SWT.NONE);
			label.setLayoutData(data);
			label.setText(strItem+":"+str);
			label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE));
			label.setFont(Display.getCurrent().getSystemFont());
			label.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "Label");
		}
	}

}
