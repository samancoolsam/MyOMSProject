/**
 * 
 */
package com.academy.ecommerce.sterling.quickAccess.extn;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyActionIdConstants;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.IYRCPanelHolder;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCTextBindingData;

/**
 * @author sahmed
 * 
 */
public class AcademyGiftBalChk extends Composite implements IYRCComposite {

	public static final String FORM_ID = "com.academy.ecommerce.sterling.quickAccess.extn.AcademyGiftBalChk"; // @jve:decl-index=0:

	private Element inElm = null;

	private AcademyGiftBalChkBehavior myBehavior = null;

	private Composite pnlRoot = null;

	private Composite GCPnlComposite;

	private Label GCPINLabel;

	private Text GCPINText;

	private Label GCNumberLabel;

	private Text GCNumberText;

	private Label BalanceLabel;

	private Text BalanceText;

	private Button button = null;

	public AcademyGiftBalChk(Composite parent, int style, Element inElm) {
		super(parent, style);
		this.inElm = inElm;
		initialize();
		setBindingForComponents();
		myBehavior = new AcademyGiftBalChkBehavior(this, FORM_ID, inElm);
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setLayout(new FillLayout());
		this.setSize(new Point(870, 270));
		createPnlRoot();
	}

	public AcademyGiftBalChkBehavior getBehavior() {
		return myBehavior;
	}

	private void createPnlRoot() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		pnlRoot = new Composite(this, SWT.NONE);
		pnlRoot.setLayout(gridLayout);
		pnlRoot.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "TaskComposite");
		createGCPnlComposite();
	}

	private void createGCPnlComposite() {
		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = GridData.FILL;
		gridData11.verticalAlignment = GridData.CENTER;
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.verticalAlignment = GridData.CENTER;
		GridData gridData1 = new GridData();
		gridData1.horizontalSpan = 3;
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.verticalAlignment = GridData.CENTER;
		gridData1.grabExcessVerticalSpace = false;
		gridData1.heightHint = -1;
		gridData1.grabExcessHorizontalSpace = true;
		GridLayout gridLayout5 = new GridLayout();
		gridLayout5.marginWidth = 10;
		gridLayout5.numColumns = 4;
		gridLayout5.makeColumnsEqualWidth = true;
		gridLayout5.horizontalSpacing = 10;
		gridLayout5.verticalSpacing = 10;
		gridLayout5.marginHeight = 10;
		GridData gridData5 = new org.eclipse.swt.layout.GridData();
		gridData5.grabExcessHorizontalSpace = true;
		gridData5.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		gridData5.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridData gridData = new org.eclipse.swt.layout.GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = false;
		gridData.verticalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridLayout5.makeColumnsEqualWidth = true;
		GCPnlComposite = new Composite(pnlRoot, SWT.NONE);
		GCPnlComposite.setLayoutData(gridData);
		GCPnlComposite.setLayout(gridLayout5);
		// AcademyScreenPanelUtils.addGradientPanelHeader(GCPnlComposite, "Gift
		// Card Balance Check");
		GridData gridData20 = new GridData();
		gridData20.horizontalAlignment = GridData.CENTER;
		gridData20.grabExcessHorizontalSpace = false;
		gridData20.grabExcessVerticalSpace = true;
		gridData20.verticalAlignment = GridData.CENTER;
		GCNumberLabel = new Label(GCPnlComposite, SWT.NONE);
		GCNumberLabel.setText("GC #");
		GCNumberLabel.setLayoutData(gridData20);
		GCNumberText = new Text(GCPnlComposite, SWT.BORDER);
		GCNumberText.setLayoutData(gridData2);
		Label filler1 = new Label(GCPnlComposite, SWT.NONE);
		Label filler3 = new Label(GCPnlComposite, SWT.NONE);
		GCPINLabel = new Label(GCPnlComposite, SWT.NONE);
		GCPINLabel.setText("PIN #");
		GCPINLabel.setLayoutData(gridData20);
		GCPINText = new Text(GCPnlComposite, SWT.BORDER);
		Label filler = new Label(GCPnlComposite, SWT.NONE);
		Label filler2 = new Label(GCPnlComposite, SWT.NONE);
		Label filler4 = new Label(GCPnlComposite, SWT.NONE);
		button = new Button(GCPnlComposite, SWT.NONE);
		button.setText("Check Gift Card Balance");
		button.setLayoutData(gridData11);
		button
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						YRCPlatformUI
								.fireAction(AcademyActionIdConstants.ACA_CHECK_GIFT_CARD_BALANCE_ACTION_ID);
					}
				});
		Label filler5 = new Label(GCPnlComposite, SWT.NONE);
		Label filler6 = new Label(GCPnlComposite, SWT.NONE);
		BalanceLabel = new Label(GCPnlComposite, SWT.NONE);
		BalanceLabel.setText("Balance");
		BalanceLabel.setLayoutData(gridData20);
		BalanceText = new Text(GCPnlComposite, SWT.BORDER | SWT.READ_ONLY);
		BalanceText.setLayoutData(gridData1);
	}

	private void setBindingForComponents() {
		// Text Bindings
		YRCTextBindingData textBindingData = new YRCTextBindingData();
		textBindingData.setSourceBinding("PaymentMethod:PaymentMethod/@SvcNo");
		textBindingData.setTargetBinding("PaymentMethod:PaymentMethod/@SvcNo");
		textBindingData.setName("GCNumberText");
		GCNumberText.setData(YRCConstants.YRC_TEXT_BINDING_DEFINATION,
				textBindingData);

		YRCTextBindingData textBindingData1 = new YRCTextBindingData();
		textBindingData1
				.setSourceBinding("PaymentMethod:PaymentMethod/@PaymentReference1");
		textBindingData1
				.setTargetBinding("PaymentMethod:PaymentMethod/@PaymentReference1");
		textBindingData1.setName("GCPINText");
		GCPINText.setData(YRCConstants.YRC_TEXT_BINDING_DEFINATION,
				textBindingData1);

		YRCTextBindingData textBindingData2 = new YRCTextBindingData();
		textBindingData2
				.setSourceBinding("PaymentMethod:PaymentMethod/@Balance");
		textBindingData2.setName("BalanceText");
		BalanceText.setData(YRCConstants.YRC_TEXT_BINDING_DEFINATION,
				textBindingData2);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yantra.yfc.rcp.IYRCComposite#getFormId()
	 */
	public String getFormId() {
		// TODO Auto-generated method stub
		return FORM_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yantra.yfc.rcp.IYRCComposite#getHelpId()
	 */
	public String getHelpId() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yantra.yfc.rcp.IYRCComposite#getPanelHolder()
	 */
	public IYRCPanelHolder getPanelHolder() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yantra.yfc.rcp.IYRCComposite#getRootPanel()
	 */
	public Composite getRootPanel() {
		// TODO Auto-generated method stub
		return pnlRoot;
	}

}
