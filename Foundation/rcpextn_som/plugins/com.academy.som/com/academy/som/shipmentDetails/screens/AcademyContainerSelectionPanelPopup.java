
/*
 * Created on Jul 04,2012
 *
 */
package com.academy.som.shipmentDetails.screens;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.w3c.dom.Element;

import com.academy.som.util.AcademyPCAConstants;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.IYRCPanelHolder;
import com.yantra.yfc.rcp.YRCComboBindingData;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCLabelBindingData;
import com.yantra.yfc.rcp.YRCPlatformUI;

/**
 * Custom Panel popup designed for selecting the container to 
 * reprint Packslip, Shipping and Return Labels in Shipment details screen, 
 * called by their related task actions.
 *
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 * Copyright © 2005, 2006 Sterling Commerce, Inc. All Rights Reserved.
 */

public class AcademyContainerSelectionPanelPopup extends Composite implements IYRCComposite {

	private Composite pnlRoot = null; //Root composite object
	private AcademyContainerSelectionPanelBehavior myBehavior; //Current panel's behavior class
	public static final String FORM_ID = "com.academy.som.shipmentDetails.screens.AcademyContainerSelectionPanelPopup";  //  @jve:decl-index=0:
	private Composite cmbComposite = null; //Composite object for holding the Container selection combo
	private Combo cmbShipmentCntr = null; //Combo object for showing the Containers
	private Composite btnComposite = null; //Composite object for holding the Button for selection confirm
	private Button btnSelect = null; //Button object for "Select"
	private String cntrSelected; //String object for storing the container value selected
	private String cntrSelected1; //String object for storing the container value selected
	private boolean isSelectBtnClicked = false; //flag for evaluating if "Select" button is clicked
	private Combo cmbPackStation = null;
	//private String strFlg = "";
	private Label lblPackStn = null;

	/**
	 * Composite of panel popup screen
	 * @param parent
	 * 			<br/> - parent Composite(Shell)
	 * @param style
	 * 			<br/> - style of Composite(SWT.NONE)
	 * @param shipmentDetails
	 * 			<br/> - Editor Input XML of Shipment details screen
	 */
	public AcademyContainerSelectionPanelPopup(Composite parent, int style, Element shipmentDetails) {
		super(parent, style);
		//strFlg = strFlag;
		initialize();
		setBindingForComponents();
		myBehavior = new AcademyContainerSelectionPanelBehavior(this, FORM_ID, shipmentDetails);
	}

	/**
	 * To initialize the composite Root panel
	 *
	 */
	private void initialize() {
		createRootPanel();
		this.setLayout(new FillLayout());
		setSize(new org.eclipse.swt.graphics.Point(300, 80));
	}

	/**
	 * Method to set the Source and Target bindings
	 * for Components.  
	 *
	 */
	private void setBindingForComponents() {
		//setting the bindings for Container Combo object
		YRCComboBindingData cmbBindingData = new YRCComboBindingData(); //creating the binding data object for combo
		cmbBindingData.setName("cmbShipmentCntr"); //setting the name of the Container Combo
		cmbBindingData.setThemeName("Combo"); //setting the theme name of Combo
		//setting the source binding for default value selection on load of the popup
		cmbBindingData.setSourceBinding("Shipment:Shipment/Containers/Container/@ShipmentContainerKey");
		//setting the List binding for combo to display a list of containers available
		cmbBindingData.setListBinding("Shipment:Shipment/Containers/Container");
		cmbBindingData.setDescriptionBinding("ContainerNo"); //setting the description to be shown for every list item
		cmbBindingData.setCodeBinding("ShipmentContainerKey"); //setting the value to be bound for the corresponding list item
		cmbShipmentCntr.setData(YRCConstants.YRC_COMBO_BINDING_DEFINATION, cmbBindingData); //setting the binding data object to Container combo
		//if (strFlg.equals("Y")){
			 YRCLabelBindingData packStnBindingData = new YRCLabelBindingData();
			 packStnBindingData.setName("lblPackStn");
			 //packStnBindingData.setSourceBinding("OpenManifestList:/Manifests/@TotalNumberOfRecords");
			  this.lblPackStn.setData("YRCLabelBindingDefinition", packStnBindingData);
			  
			//setting the bindings for PackStation Combo object
			YRCComboBindingData cmbBindingData1 = new YRCComboBindingData(); //creating the binding data object for combo
			cmbBindingData1.setName("cmbPackStation"); //setting the name of the Container Combo
			cmbBindingData1.setThemeName("Combo"); //setting the theme name of Combo
			//setting the source binding for default value selection on load of the popup
			cmbBindingData1.setSourceBinding("Extn_PackSlip_PrinterID_Output:CommonCodeList/CommonCode/@CodeValue");
			//setting the List binding for combo to display a list of containers available
			cmbBindingData1.setListBinding("Extn_PackSlip_PrinterID_Output:/CommonCodeList/CommonCode");
			cmbBindingData1.setDescriptionBinding("CodeValue"); //setting the description to be shown for every list item
			cmbBindingData1.setCodeBinding("CodeValue"); //setting the value to be bound for the corresponding list item
			cmbPackStation.setData(YRCConstants.YRC_COMBO_BINDING_DEFINATION, cmbBindingData1);
			//setting the binding data object to Container combo
		
		
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
		pnlRoot.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "TaskComposite");
		createCmbComposite();
		createBtnComposite();
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
	 * This method initializes cmbComposite	
	 * which holds the Container combo
	 */
	private void createCmbComposite() {
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.horizontalAlignment = GridData.FILL;
		cmbComposite = new Composite(getRootPanel(), SWT.NONE);
		cmbComposite.setLayout(new GridLayout());
		createCmbShipmentCntr();
		cmbComposite.setLayoutData(gridData);
		cmbComposite.setData(YRCConstants.YRC_CONTROL_NAME, "cmbComposite");
		cmbComposite.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "Composite");
	}

	/**
	 * This method initializes cmbShipmentCntr	
	 * which is the Container combo for container selection
	 */
	private void createCmbShipmentCntr() {
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.CENTER;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.verticalAlignment = GridData.CENTER;
		gridData1.widthHint=100;
		cmbShipmentCntr = new Combo(cmbComposite, SWT.READ_ONLY);
		cmbShipmentCntr.setLayoutData(gridData1);
		
		//if(strFlg.equals("Y")){
			this.lblPackStn = new Label(this.cmbComposite, 0);
			  this.lblPackStn.setText("Pack Station");
			  lblPackStn.setLayoutData(gridData1);
			cmbPackStation = new Combo(cmbComposite, SWT.READ_ONLY);
			cmbPackStation.setLayoutData(gridData1);
			
		
		
	}

	/**
	 * This method initializes btnComposite	
	 * which holds the "Select" button for confirming the selection
	 */
	private void createBtnComposite() {
		GridData gridData3 = new GridData();
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.verticalAlignment = GridData.CENTER;
		gridData3.horizontalAlignment = GridData.END;
		GridData gridData2 = new GridData();
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.verticalAlignment = GridData.CENTER;
		gridData2.horizontalAlignment = GridData.FILL;
		btnComposite = new Composite(getRootPanel(), SWT.NONE);
		btnComposite.setLayout(new GridLayout());
		btnComposite.setLayoutData(gridData2);
		btnComposite.setData(YRCConstants.YRC_CONTROL_NAME, "btnComposite");
		btnComposite.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "TaskComposite");

		btnSelect = new Button(btnComposite, SWT.NONE); //creating the button object in the btnComposite
		btnSelect.setText(YRCPlatformUI.getString(AcademyPCAConstants.CUSTOM_BTN_CNTR_SELECT_VAL));
		btnSelect.setLayoutData(gridData3);
		btnSelect.setData(YRCConstants.YRC_CONTROL_NAME, "btnSelect");
		btnSelect.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "Button");
		btnSelect.addSelectionListener(new SelectionAdapter(){
			//adding Selection Listener to button "Select"
			public void widgetSelected(SelectionEvent event) {
				//fetching the value of Container selected
				cntrSelected = (String) cmbShipmentCntr.getData("YRCComboOptionIndex"+cmbShipmentCntr.getSelectionIndex());
				AcademySIMTraceUtil.logMessage("print cntrol selected for popup::"+cntrSelected);
				cntrSelected1 = myBehavior.getComboPackStation();
				//cmbPackStation.ge ("YRCComboOptionIndex"+cmbShipmentCntr.getSelection())
				AcademySIMTraceUtil.logMessage("print cntrol selected for popup::"+cntrSelected1);
				isSelectBtnClicked = true; //setting the flag as true if "Select" button is clicked
				closePopup(); //close the popup panel
				super.widgetSelected(event);
			}
		});
	}

	/**
	 * Method to dispose/close the popup panel
	 *
	 */
	private void closePopup() {
		this.getShell().close();
	}

	/**
	 * Getter method for retrieving the value of Container selected
	 * @return String
	 * 			<br/> - returns the cntrSelected value
	 */
	public String getCntrSelected(){
		return cntrSelected;
	}

	/**
	 * Getter method for retrieving the button Selection confirm flag
	 * @return boolean
	 * 			<br/> - returns the flag isSelectBtnClicked
	 */
	public boolean getIsSelectBtnClicked(){
		return isSelectBtnClicked;
	}

	public String getCntrSelectedForPackStn() {
		// TODO Auto-generated method stub
		return cntrSelected1;
	}
}
