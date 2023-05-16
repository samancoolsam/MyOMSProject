package com.academy.ecommerce.sterling.orderSummary.screens;

//Eclipse Imports
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

//Sterling Imports
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.IYRCPanelHolder;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCTextBindingData;

//Project Imports
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;

//Misc Imports -NONE

public class AcademyTaxIdPopUp extends Composite implements IYRCComposite {
	
private Composite pnlRoot = null;
private AcademyTaxIdPopUpBehavior myBehavior;
public static final String TAX_FORM_ID = AcademyPCAConstants.FORM_ID_CUSTOM_TAXIDPOPUP;
public static final String FORM_ID = AcademyPCAConstants.FORM_ID_ORDERSUMMARY;
private static final String Order = null;
boolean canClose = false;
public Label lblNotesHeader = null;
private String sTaxExemptionCertificate;
private String sOrderHeaderKey;
private Composite userEntryPanel = null;
private Label tax_label = null;
private Text tax_text_value = null;
private Composite submit_buttons_panel = null;
private Button submit_button = null;
private Button cancel_button = null;
public boolean clickofcancel=false;
public boolean clickofSubmit=false;
YRCTextBindingData bDTxtTaxId=null;
private boolean lookupMode;
private Shell shell;

public  AcademyTaxIdPopUp(Composite parent, int style,String data, String orderHeaderKey, boolean lookupMode)
{
	super(parent, style);
	this.lookupMode = lookupMode;
   	initialize();
    myBehavior = new AcademyTaxIdPopUpBehavior(this, FORM_ID);
    this.sTaxExemptionCertificate=data;
    if(!YRCPlatformUI.isVoid(sTaxExemptionCertificate)){
    	this.sTaxExemptionCertificate=data;
    	this.sOrderHeaderKey=orderHeaderKey;
    	myBehavior.setTaxExemptID(this.sTaxExemptionCertificate,this.sOrderHeaderKey);
    }else{
    	String strsTaxExemptionCertificate =tax_text_value.getText();
    	this.sTaxExemptionCertificate= sTaxExemptionCertificate;
    	this.sOrderHeaderKey=orderHeaderKey;
    	myBehavior.setTaxExemptID(this.sTaxExemptionCertificate,this.sOrderHeaderKey);
    }

	
    myBehavior.initializeModel();
    YRCPlatformUI.trace("#######Value of Tax Exempt Certificate:##############\n");
    shell=this.getShell();
  
}

private void initialize() {
	createRootPanel();
	this.setLayout(new FillLayout());
	setSize(new org.eclipse.swt.graphics.Point(300, 200));

}

public String getFormId() {
	return TAX_FORM_ID;
}

public Composite getRootPanel() {
	return pnlRoot;
}

private void createRootPanel() {
	GridLayout gridLayout = new GridLayout();
	gridLayout.numColumns = 1;
	pnlRoot = new Composite(this, SWT.NONE);
	pnlRoot.setLayout(gridLayout);
	createUserEntryPanel();
	createAction_buttons_panel();
	addListenersToButtons();
}


private void createUserEntryPanel() {
	GridData gridData2 = new GridData();
	gridData2.grabExcessHorizontalSpace = true;
	gridData2.horizontalAlignment = GridData.FILL;
	gridData2.verticalAlignment = GridData.CENTER;
	gridData2.grabExcessVerticalSpace = true;
	gridData2.heightHint = 16;
	gridData2.widthHint = 50;
	GridData gridData1 = new GridData();
	GridLayout gridLayout1 = new GridLayout();
	gridLayout1.marginHeight = 5;
	gridLayout1.marginWidth = 5;
	gridLayout1.numColumns = 2;
	GridData gridData = new GridData();
	gridData.verticalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.grabExcessVerticalSpace = true;
	gridData.horizontalAlignment = GridData.FILL;
	userEntryPanel = new Composite(getRootPanel(), SWT.NONE);
	userEntryPanel.setLayoutData(gridData);
	userEntryPanel.setLayout(gridLayout1);
	tax_label = new Label(userEntryPanel, SWT.NONE);
	tax_label.setText(AcademyPCAConstants.ATTR_TAX_ID);
	tax_label.setLayoutData(gridData1);
	tax_text_value = new Text(userEntryPanel, SWT.BORDER);
	tax_text_value.setLayoutData(gridData2);	
	bDTxtTaxId = new YRCTextBindingData();
	bDTxtTaxId.setName("Tax Exempt ID");
	bDTxtTaxId.setSourceBinding("TaxExemptDetails:TaxDetails/@TaxExemptId");
	bDTxtTaxId.setTargetBinding("TaxExemptDetails:TaxDetails/@TaxExemptId");
	tax_text_value.setData(YRCConstants.YRC_TEXT_BINDING_DEFINATION,this.bDTxtTaxId);
	}
public IYRCPanelHolder getPanelHolder() {
    return null;
}
/**
 * This method initializes submit_buttons_panel
 * 
 */
private void createAction_buttons_panel() {
	GridData gridData4 = new GridData();
	gridData4.horizontalAlignment = GridData.END;
	gridData4.verticalAlignment = GridData.CENTER;
	gridData4.grabExcessHorizontalSpace = false;
	gridData4.grabExcessVerticalSpace = true;
	GridData gridData3 = new GridData();
	gridData3.horizontalAlignment = GridData.END;
	gridData3.grabExcessHorizontalSpace = false;
	gridData3.grabExcessVerticalSpace = true;
	gridData3.verticalAlignment = GridData.CENTER;
	GridLayout gridLayout2 = new GridLayout();
	gridLayout2.numColumns = 2;
	gridLayout2.marginHeight = 2;
	gridLayout2.marginWidth = 2;
	submit_buttons_panel = new Composite(getRootPanel(), SWT.NONE);
	submit_buttons_panel.setLayout(gridLayout2);
	submit_buttons_panel.setLayoutData(gridData3);
	submit_button = new Button(submit_buttons_panel, SWT.NONE);
	submit_button.setText("Submit");
	cancel_button = new Button(submit_buttons_panel, SWT.NONE);
	cancel_button.setText("Cancel");
	cancel_button.setLayoutData(gridData4);
	}

private void addListenersToButtons() {
	submit_button.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(org.eclipse.swt.events.SelectionEvent arg0) {
			clickofSubmit=true;
			clickofcancel=false;
			YRCPlatformUI.fireAction(AcademyPCAConstants.ACADEMY_TAX_ID_SUBMIT_ACTION);
		}
	});

	cancel_button.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {	
			clickofSubmit=false;
			clickofcancel=true;
			myBehavior.close();
			
		}
	});
}
public String getHelpId() {
	return "ycdRCP0905";
}

/**
 * This method initializes AcademyTaxIdPopUpBehavior	
 *
 */
public AcademyTaxIdPopUpBehavior getMyBehavior() {
	return myBehavior;
}

public String getTax_text_value() {
		return tax_text_value.getText();
}

public String getOrderHeaderKey() {
	return sOrderHeaderKey;
}



}


