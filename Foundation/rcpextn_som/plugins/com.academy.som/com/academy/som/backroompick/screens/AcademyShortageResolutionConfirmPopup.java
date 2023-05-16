	
/*
 * Created on Jun 26,2012
 *
 */
package com.academy.som.backroompick.screens;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.academy.som.util.AcademyPCAConstants;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.IYRCPanelHolder;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCPlatformUI;

/**
 * Custom panel popup class done for confirming the Inventory 
 * Shortage resolution. 
 *
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 * Copyright © 2005, 2006 Sterling Commerce, Inc. All Rights Reserved.
 */

public class AcademyShortageResolutionConfirmPopup extends Composite implements IYRCComposite {

	private Composite pnlRoot = null; //Root composite object
    public static final String FORM_ID = "com.academy.som.backroompick.screens.AcademyShortageResolutionConfirmPopup";  //  @jve:decl-index=0:
	private Composite dialogComposite = null; //composite that holds the resolution message and the confirm buttons
	private Composite msgComposite = null; //composite that holds the message
	private Composite btnComposite = null; //composite that holds the buttons
	private Text resolutionText = null; //Text object of resolution message
	private Button btnCancelResolution = null; //Button object for cancel
	private Button btnConfirmResolution = null; //Button object for confirm
	private boolean isSelectionConfirm = false; //flag to check if "Confirm" button is pressed

	/**
	 * Constructor of the panel class
	 * @param parent
	 * 			<br/> - parent Composite(Shell)
	 * @param style
	 * 			<br/> - style of Composite(SWT.NONE)
	 */
	public AcademyShortageResolutionConfirmPopup(Composite parent, int style) {
		super(parent, style);
		initialize();
        setBindingForComponents();
	}
	
	/**
	 * To initialize the composite Root panel
	 *
	 */
	private void initialize() {
		createRootPanel();
		this.setLayout(new FillLayout());
		setSize(new org.eclipse.swt.graphics.Point(300,120));
	}
	
    private void setBindingForComponents() {
        //TODO: set all bindings here
    }
    
    /**
     * Getter method for FORM_ID
     * @return String
     * 			<br/> - returns the panel FORM_ID
     */
    public String getFormId() {
        return FORM_ID;
    }
    
    /**
     * Getter method to get the Root Panel
     * @return String
     * 			<br/> - returns the panel's root composite
     */
    public Composite getRootPanel() {
        return pnlRoot;
    }

	/**
	 * Method to create the composite's root panel
	 *
	 */
    private void createRootPanel() {
		pnlRoot = new Composite(this, SWT.NONE);
        pnlRoot.setLayout(new GridLayout());
        pnlRoot.setData(YRCConstants.YRC_CONTROL_NAME, "pnlRoot");
        pnlRoot.setBackground(YRCPlatformUI.getBackGroundColor("TaskComposite"));
        pnlRoot.setForeground(YRCPlatformUI.getForeGroundColor("TaskComposite"));
        createDialogComposite();
	}
	
	public IYRCPanelHolder getPanelHolder() {
        // TODO Complete getPanelHolder
        return null;
    }
    
    public String getHelpId() {
		// TODO Complete getHelpId
		return null;
	}

	/**
	 * This method initializes dialogComposite	
	 * which holds the msgComposite and btnComposite
	 */
	private void createDialogComposite() {
		GridData gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		dialogComposite = new Composite(getRootPanel(), SWT.NONE);
		dialogComposite.setLayout(new GridLayout());
		dialogComposite.setData(YRCConstants.YRC_CONTROL_NAME, "dialogComposite");
		dialogComposite.setBackground(YRCPlatformUI.getBackGroundColor("TaskComposite"));
		dialogComposite.setForeground(YRCPlatformUI.getForeGroundColor("TaskComposite"));
		dialogComposite.setLayoutData(gridData);
		createMsgComposite();
		createBtnComposite();
	}

	/**
	 * This method initializes msgComposite	
	 * which holds the resolution Text object
	 */
	private void createMsgComposite() {
		GridData gridData3 = new GridData();
		gridData3.verticalAlignment = GridData.CENTER;
		gridData3.verticalSpan = 2;
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.grabExcessVerticalSpace = true;
		gridData3.horizontalAlignment = GridData.CENTER;
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.grabExcessVerticalSpace = false;
		gridData1.verticalSpan = 10;
		gridData1.verticalAlignment = GridData.FILL;
		msgComposite = new Composite(dialogComposite, SWT.NONE);
		msgComposite.setLayout(new GridLayout());
		msgComposite.setData(YRCConstants.YRC_CONTROL_NAME, "msgComposite");
		msgComposite.setBackground(YRCPlatformUI.getBackGroundColor("Composite"));
		msgComposite.setForeground(YRCPlatformUI.getForeGroundColor("Composite"));
		msgComposite.setLayoutData(gridData1);
		resolutionText = new Text(msgComposite, SWT.NONE | SWT.CENTER | SWT.WRAP | SWT.READ_ONLY);
		resolutionText.setData(YRCConstants.YRC_CONTROL_NAME, "txtResolution");
		resolutionText.setText(YRCPlatformUI.getString(AcademyPCAConstants.CNF_RESOLUTION_TXT_MSG));
		resolutionText.setFont(YRCPlatformUI.getFont("Text"));
		resolutionText.setBackground(YRCPlatformUI.getBackGroundColor("Text"));
		resolutionText.setForeground(YRCPlatformUI.getForeGroundColor("Text"));
		resolutionText.setLayoutData(gridData3);
	}

	/**
	 * This method initializes btnComposite	
	 * which holds the buttons for "Confirm" and "Cancel"
	 */
	private void createBtnComposite() {
		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.END;
		gridData5.grabExcessHorizontalSpace = true;
		gridData5.grabExcessVerticalSpace = false;
		gridData5.verticalAlignment = GridData.CENTER;
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = GridData.END;
		gridData4.grabExcessHorizontalSpace = false;
		gridData4.verticalAlignment = GridData.CENTER;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		GridData gridData2 = new GridData();
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.verticalAlignment = GridData.FILL;
		gridData2.grabExcessVerticalSpace = false;
		gridData2.verticalSpan = 7;
		gridData2.horizontalAlignment = GridData.FILL;
		btnComposite = new Composite(dialogComposite, SWT.NONE);
		btnComposite.setLayoutData(gridData2);
		btnComposite.setData(YRCConstants.YRC_CONTROL_NAME, "btnComposite");
		btnComposite.setBackground(YRCPlatformUI.getBackGroundColor("TaskComposite"));
		btnComposite.setForeground(YRCPlatformUI.getForeGroundColor("TaskComposite"));
		btnComposite.setLayout(gridLayout);
		btnConfirmResolution = new Button(btnComposite, SWT.NONE);
		btnConfirmResolution.setText(YRCPlatformUI.getString(AcademyPCAConstants.CUSTOM_BTN_CNF_RESO_VAL));
		btnConfirmResolution.setData(YRCConstants.YRC_CONTROL_NAME, "btnConfirm");
		btnConfirmResolution.setBackground(YRCPlatformUI.getBackGroundColor("Button"));
		btnConfirmResolution.setForeground(YRCPlatformUI.getForeGroundColor("Button"));
		btnConfirmResolution.setLayoutData(gridData5);
		btnConfirmResolution.addSelectionListener(new SelectionAdapter(){
			//adding button click listener to "Confirm" button
			public void widgetSelected(SelectionEvent event) {
				isSelectionConfirm = true; //set the flag as true, if Confirm button clicked
				closePopup(); //close the popup panel
			}
		});
		btnCancelResolution = new Button(btnComposite, SWT.NONE);
		btnCancelResolution.setText(YRCPlatformUI.getString(AcademyPCAConstants.CUSTOM_BTN_CNL_RESO_VAL));
		btnCancelResolution.setData(YRCConstants.YRC_CONTROL_NAME, "btnCancel");
		btnCancelResolution.setBackground(YRCPlatformUI.getBackGroundColor("Button"));
		btnCancelResolution.setForeground(YRCPlatformUI.getForeGroundColor("Button"));
		btnCancelResolution.setLayoutData(gridData4);
		btnCancelResolution.addSelectionListener(new SelectionAdapter(){
			//adding button click listener to "Cancel" button
			public void widgetSelected(SelectionEvent event) {
				isSelectionConfirm = false; //set the flag as false, if Cancel button clicked
				closePopup(); //close the popup panel
			}
		});
	}
  
	/**
	 * Method to dispose/close the popup panel
	 *
	 */
	private void closePopup() {
		this.getShell().dispose();
	}
	
	/**
	 * Getter method for retrieving the Confirm flag
	 * @return boolean
	 * 			<br/> - returns the flag isSelectionConfirm
	 */
	public boolean getSelectionConfirm(){
		return isSelectionConfirm;
	}
}
