package com.academy.ecommerce.sterling.itemDetails.screens;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.browser.Browser; 
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.IYRCPanelHolder;
import com.yantra.yfc.rcp.YRCButtonBindingData;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCPlatformUI;

public class AcademyItemPopUp extends Composite implements IYRCComposite {

	private Composite pnlRoot = null;
	private AcademyItemPopUpBehavior myBehavior;
	public static final String FORM_ID = AcademyPCAConstants.FORM_ID_ITEM_DETAILS;
	boolean canClose = false;
	private Composite compositeParent = null;
	private Composite compositeButtons = null;
	private Button Btn_Close = null;
	private Browser browser;
	private boolean lookupMode;
	private Shell shell = null;
	private String str=null;
	private String strWebsiteURl;

/*	public AcademyItemPopUp(Composite parent, int style,boolean lookupmode)
	{
		this(parent, style, "",lookupmode);
		canClose = true;
	}
*/
	public AcademyItemPopUp(Composite parent, int style, String str,
			boolean lookupMode) {
		super(parent, style);
		strWebsiteURl= str;
		initialize();
		setBindingForComponents();
		this.lookupMode = lookupMode;
		myBehavior = new AcademyItemPopUpBehavior(this, FORM_ID);
		shell = this.getShell();

	}

	private void initialize() {
		FillLayout fillLayout = new FillLayout();
		createRootPanel();
		this.setLayout(fillLayout);
		setSize(new org.eclipse.swt.graphics.Point(298, 227));
	}

	private void setBindingForComponents() {

	}

	public String getFormId() {
		return FORM_ID;
	}

	public Composite getRootPanel() {
		return pnlRoot;
	}

	private void createRootPanel() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		pnlRoot = new Composite(this, SWT.NONE);
		pnlRoot.setLayout(gridLayout);
		showBrowserContents(new GridLayout());
		createCompositeButtons();
		pnlRoot.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "TaskComposite");

	}

	public IYRCPanelHolder getPanelHolder() {
		return null;
	}

	public String getHelpId() {
		return "ycdRCP0905";
	}

	/**
	 * This method initializes grpMain
	 * 
	 */

	public AcademyItemPopUpBehavior getMyBehavior() {
		return myBehavior;
	}

	/**
	 * This method initializes compositeParent
	 * 
	 */

	public void showBrowserContents(GridLayout gridLayout) {
		GridData gridData4 = new org.eclipse.swt.layout.GridData();
		gridData4.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData4.grabExcessHorizontalSpace = true;
		gridData4.grabExcessVerticalSpace = true;
		gridData4.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData4.heightHint = 80;
		gridData4.widthHint = 80;
		browser = new Browser(getRootPanel(), SWT.NONE);
		browser.setLayoutData(gridData4);
		String url = strWebsiteURl;
		browser.setUrl(url);

	}

	/**
	 * This method initializes compositeButtons
	 * 
	 */
	private void createCompositeButtons() {
		GridData gridData2 = new org.eclipse.swt.layout.GridData();
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.heightHint = 25;
		gridData2.widthHint = 80;
		gridData2.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		GridData gridData1 = new org.eclipse.swt.layout.GridData();
		gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.grabExcessVerticalSpace = false;
		gridData1.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		compositeButtons = new Composite(getRootPanel(), SWT.NONE);
		compositeButtons.setLayout(new GridLayout());
		compositeButtons.setLayoutData(gridData1);
		compositeButtons.setData(YRCConstants.YRC_CONTROL_NAME,
				"compositeButtons");
		compositeButtons.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE,
				"TaskComposite");

		Btn_Close = new Button(compositeButtons, SWT.NONE);
		Btn_Close.setText("Btn_Close");
		Btn_Close.setLayoutData(gridData2);
		Btn_Close.setData(YRCConstants.YRC_CONTROL_NAME, "button");
		Btn_Close
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						myBehavior.close();
					}
				});

		YRCButtonBindingData buttonBindingData = new YRCButtonBindingData();
		buttonBindingData.setActionHandlerEnabled(true);
		buttonBindingData.setName("btnClose");
		Btn_Close.setData(YRCConstants.YRC_BUTTON_BINDING_DEFINATION,
				buttonBindingData);

	}

}
