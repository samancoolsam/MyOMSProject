	
/*
 * Created on Jul 13,2012
 *
 */
package com.academy.som.printpickticket.screens;

import java.util.ArrayList;

import javax.xml.xpath.XPathConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.som.util.AcademyPCAConstants;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.IYRCPanelHolder;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCXPathUtils;
import com.yantra.yfc.rcp.YRCXmlUtils;

/**
 * Custom External panel designed for the Store Supervisor to change
 * the carriers for the day as "Off Day" or "Working Day" for manifesting,
 * shown in the pageID : "com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.printpickticket.wizardpages.SOPOutboundPrintPickTicket"
 *
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 * Copyright © 2005, 2006 Sterling Commerce, Inc. All Rights Reserved.
 */

public class AcademyManifestingDayPanel extends Composite implements IYRCComposite {

	private Composite pnlRoot = null; //Root composite object
    private AcademyManifestingDayPanelBehavior myBehavior; //Current panel's behavior class
    public static final String FORM_ID = "com.academy.som.printpickticket.screens.AcademyManifestingDayPanel";
	private Group grpManifestingDay = null; //Group object that groups the tabulation
	private Label lblCalendarId = null; //Label object for "Carrier"
	private Composite btnComposite = null; //Composite that holds the "Save" and "Reset" buttons
	private Button btnSave = null; //Button object for "Save"
	private Button btnReset = null; //Button object for "Reset"
	private Element orgWorkingDaysElement = null;  //Target model object for storing the changes made  //  @jve:decl-index=0:
	private static Color COLOR_ODD = null;
	private static Color COLOR_EVEN = null;
	
	/**
	 * Constructor of the External Panel class
	 * @param parent
	 * 			<br/> - parent Composite
	 * @param style
	 * 			<br/> - style of Composite
	 * @param behavior
	 * 			<br/> - Extention Behavior class : "com.academy.sfs.printpickticket.screens.AcademyPrintPickTicketForItemExtnWizardBehavior"
	 */
	public AcademyManifestingDayPanel(Composite parent, int style, YRCExtentionBehavior behavior) {
		super(parent, style);
	initialize();
		
		
        setBindingForComponents();
        myBehavior = new AcademyManifestingDayPanelBehavior(this, FORM_ID);
        COLOR_ODD = new Color(null, 0, 128, 0);
        COLOR_EVEN = new Color(null, 0, 0, 255);
	}
	
	/**
	 * To initialize the composite Root panel
	 *
	 */
	private void initialize() {
		createRootPanel();
		this.setLayout(new FillLayout());
		setSize(new org.eclipse.swt.graphics.Point(300,200));
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
        pnlRoot.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "Composite");
        createGrpManifestingDay();
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
	 * This method initializes grpManifestingDay	
	 * which holds the tabulation data generated dynamically
	 */
	private void createGrpManifestingDay() {
		
		GridLayout gridLayoutGrpMDay = new GridLayout();
		gridLayoutGrpMDay.numColumns = 2;
		GridData gridDataGrpMDay = new GridData();
		gridDataGrpMDay.horizontalAlignment = GridData.FILL;
		gridDataGrpMDay.grabExcessHorizontalSpace = true;
		gridDataGrpMDay.grabExcessVerticalSpace = false;
		gridDataGrpMDay.verticalAlignment = GridData.CENTER;
		
		grpManifestingDay = new Group(getRootPanel(), SWT.NONE);
		grpManifestingDay.setLayoutData(gridDataGrpMDay);
		grpManifestingDay.setLayout(gridLayoutGrpMDay);
		grpManifestingDay.setText(YRCPlatformUI.getString(AcademyPCAConstants.MANIFEST_DAY_GRP_TITLE));
		grpManifestingDay.setData(YRCConstants.YRC_CONTROL_NAME, "grpManifestingDay");
		grpManifestingDay.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "Group");
		
	}

	/**
	 * This method initializes btnComposite	
	 * which holds the Buttons for "Save" and "Reset"
	 */
	private void createBtnComposite() {
		GridData gridData7 = new GridData();
		gridData7.horizontalAlignment = GridData.END;
		gridData7.grabExcessHorizontalSpace = true;
		gridData7.verticalAlignment = GridData.CENTER;
		GridData gridData6 = new GridData();
		gridData6.horizontalAlignment = GridData.END;
		gridData6.grabExcessHorizontalSpace = false;
		gridData6.verticalAlignment = GridData.CENTER;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		GridData gridData5 = new GridData();
		gridData5.grabExcessHorizontalSpace = true;
		gridData5.horizontalAlignment = GridData.FILL;
		gridData5.verticalAlignment = GridData.CENTER;
		gridData5.grabExcessVerticalSpace = false;
		btnComposite = new Composite(getRootPanel(), SWT.NONE);
		btnComposite.setData(YRCConstants.YRC_CONTROL_NAME, "btnComposite");
		btnComposite.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "Composite");
		btnComposite.setLayoutData(gridData5);
		btnComposite.setLayout(gridLayout1);
		btnSave = new Button(btnComposite, SWT.NONE);
		btnSave.setText(YRCPlatformUI.getString(AcademyPCAConstants.BTN_SAVE_MANIFEST_DAY));
		btnSave.setData(YRCConstants.YRC_CONTROL_NAME, "btnSave");
		btnSave.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "Button");
		btnSave.setLayoutData(gridData7);
		btnSave.addSelectionListener(new SelectionAdapter(){
			//adding Selection Listener to button "Save"
			public void widgetSelected(SelectionEvent e) {
				//calling doSaveAction() method to save the changes done to the Carriers Working today
				myBehavior.doSaveAction(orgWorkingDaysElement);
			}
		});
		btnReset = new Button(btnComposite, SWT.NONE);
		btnReset.setText(YRCPlatformUI.getString(AcademyPCAConstants.BTN_RESET_MANIFEST_DAY));
		btnReset.setData(YRCConstants.YRC_CONTROL_NAME, "btnReset");
		btnReset.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "Button");
		btnReset.setLayoutData(gridData6);
		btnReset.addSelectionListener(new SelectionAdapter(){
			//adding Selection Listener to button "Reset"
			public void widgetSelected(SelectionEvent e) {
				//calling doResetAction() method to reset the changes done to the Carriers Working today
				myBehavior.doResetAction();
			}
		});
	}

	/**
	 * Method called by this panel's Behavior class to generate the 
	 * tabulation data dynamically
	 * @param orgWorkingDaysElement
	 * 			<br/> - "GetCalendarWorkingDays" command output that gives the
	 * 					working day stats for all Carriers in a store
	 */
	public void createWorkingCalendarTable(Element orgWorkingDaysElement) {
		this.orgWorkingDaysElement = YRCXmlUtils.getCopy(orgWorkingDaysElement);
		
		createCarrierComposite();
		Composite workDaysComposite = createWorkDaysComposite();
		
		Label lblCalendarDay = null; //Label object for showing the Date of the Working Info
		Button rdbWorkingDay = null; //Radio Button object for storing Working day info
		Button rdbNonWorkingDay = null; //Radio Button object for storing Non Working day info
		
		GridData gridDataRadio = new GridData(); //Grid Data for Radio buttons
		gridDataRadio.horizontalAlignment = GridData.CENTER;
		gridDataRadio.grabExcessHorizontalSpace = true;
		gridDataRadio.verticalAlignment = GridData.CENTER;
		
		GridData gridDataLblDay = new GridData(); //Grid Data for Work Day Labels
		gridDataLblDay.horizontalAlignment = GridData.CENTER;
		gridDataLblDay.grabExcessHorizontalSpace = true;
		gridDataLblDay.verticalAlignment = GridData.CENTER;
		gridDataLblDay.horizontalSpan = 2;
		
		GridLayout gridLayoutRdbComp = new GridLayout(); //Grid Layout for the composite that groups the Radio buttons for each Carrier
		gridLayoutRdbComp.numColumns = 2;
		gridLayoutRdbComp.makeColumnsEqualWidth = true;
		GridData gridDataRdbComp = new GridData(); //Grid Data for the composite that groups the Radio buttons for each Carrier
		gridDataRdbComp.horizontalAlignment = GridData.FILL;
		gridDataRdbComp.grabExcessHorizontalSpace = true;
		gridDataRdbComp.grabExcessVerticalSpace = false;
		gridDataRdbComp.horizontalSpan = 2;
		gridDataRdbComp.verticalAlignment = GridData.CENTER;
		
		//Grid Layout for the composite that groups the Radio buttons for each Carrier's Work day
		GridLayout gridLayoutDayComp = new GridLayout();
		gridLayoutDayComp.numColumns = 2;
		gridLayoutDayComp.makeColumnsEqualWidth = true;
		//Grid Data for the composite that groups the Radio buttons for each Carrier's Work day
		GridData gridDataDayComp = new GridData();
		gridDataDayComp.horizontalSpan = 2;
		gridDataDayComp.verticalAlignment = GridData.CENTER;
		gridDataDayComp.horizontalAlignment = GridData.FILL;
		gridDataDayComp.grabExcessHorizontalSpace = true;
		gridDataDayComp.grabExcessVerticalSpace = false;
		
		NodeList orgWorkingDayNL = this.orgWorkingDaysElement.getElementsByTagName(AcademyPCAConstants.ORG_WORKING_DAY_ELEMENT);
		
		for(int listIndex = 0; listIndex < orgWorkingDayNL.getLength(); listIndex++){
			Element orgWorkingDayElement = (Element)orgWorkingDayNL.item(listIndex);
			NodeList workingCalNL = orgWorkingDayElement.getElementsByTagName(AcademyPCAConstants.WORKING_CALENDAR_ELEMENT);
			
			final String dateStr = orgWorkingDayElement.getAttribute(AcademyPCAConstants.DATE_ATTR);
			
			Composite dayComposite = new Composite(workDaysComposite, SWT.NONE); //Composite that houses the Working info for the day
			//Setting the control Name dynamically
			dayComposite.setData(YRCConstants.YRC_CONTROL_NAME, (new StringBuilder()).append("compDay").append(dateStr).toString());
			dayComposite.setLayout(gridLayoutDayComp);
			dayComposite.setLayoutData(gridDataDayComp);
			dayComposite.setBackground(YRCPlatformUI.getBackGroundColor("Composite"));
			dayComposite.setForeground(YRCPlatformUI.getForeGroundColor("Composite"));
			
			lblCalendarDay = new Label(dayComposite, SWT.READ_ONLY);
			//Setting the control Name dynamically
			lblCalendarDay.setData(YRCConstants.YRC_CONTROL_NAME, (new StringBuilder()).append("lbl").append(dateStr).toString());
			lblCalendarDay.setLayoutData(gridDataLblDay);
			lblCalendarDay.setText((myBehavior.getDiffInDaysFromCurrentDay(dateStr)==0)?
										"    TODAY" : myBehavior.getUserLocaleDateFormat(dateStr));
			lblCalendarDay.setFont(YRCPlatformUI.getFont("Label"));
			lblCalendarDay.setBackground(YRCPlatformUI.getBackGroundColor("Label"));
			lblCalendarDay.setForeground(YRCPlatformUI.getForeGroundColor("Label"));
			
			for(int carrListIndex = 0; carrListIndex < workingCalNL.getLength(); carrListIndex++){
				Element workingCalElement = (Element)workingCalNL.item(carrListIndex);
				final String calendarIdStr = workingCalElement.getAttribute(AcademyPCAConstants.CALENDAR_ID_ATTR);
				String workingDayFlag = workingCalElement.getAttribute(AcademyPCAConstants.WORKING_DAY_FLAG_ATTR);
				
				Composite rdbComposite = new Composite(dayComposite, SWT.NONE); //Composite that groups the Radio buttons for each Carrier
				//Setting the control Name dynamically
				rdbComposite.setData(YRCConstants.YRC_CONTROL_NAME, 
								(new StringBuilder()).append("compRdb").append(calendarIdStr).append(dateStr).toString());
				rdbComposite.setBackground(YRCPlatformUI.getBackGroundColor("Composite"));
				rdbComposite.setForeground(YRCPlatformUI.getForeGroundColor("Composite"));
				rdbComposite.setLayoutData(gridDataRdbComp);
				rdbComposite.setLayout(gridLayoutRdbComp);
				
				rdbWorkingDay = new Button(rdbComposite, SWT.RADIO); 
				//Setting the control Name dynamically
				rdbWorkingDay.setData(YRCConstants.YRC_CONTROL_NAME, 
								(new StringBuilder()).append("rdb").append(calendarIdStr).append("WorkingDay").append(dateStr).toString());
				if(workingDayFlag.equals(AcademyPCAConstants.STRING_Y)){
					//Setting to true if Today is Working
					rdbWorkingDay.setSelection(true);
				}
				rdbWorkingDay.setLayoutData(gridDataRadio);
				rdbWorkingDay.setText(YRCPlatformUI.getString(AcademyPCAConstants.WORKING_DAY_LABEL_TEXT));
				rdbWorkingDay.setFont(YRCPlatformUI.getFont("Button"));
				//rdbWorkingDay.setBackground(YRCPlatformUI.getBackGroundColor("Button"));
				rdbWorkingDay.setBackground(YRCPlatformUI.getBackGroundColor("Label"));
				rdbWorkingDay.setForeground((carrListIndex%2==0)? COLOR_ODD : COLOR_EVEN);
				rdbWorkingDay.addSelectionListener(new SelectionAdapter(){
					//adding Selection listener to Radio button
					public void widgetSelected(SelectionEvent e) {
						//to update the selection done to the target model
						modifyOrgWorkingDay(calendarIdStr, AcademyPCAConstants.STRING_Y, dateStr);
					}
				});
				
				rdbNonWorkingDay = new Button(rdbComposite, SWT.RADIO);
				//Setting the control Name dynamically
				rdbNonWorkingDay.setData(YRCConstants.YRC_CONTROL_NAME, 
									(new StringBuilder()).append("rdb").append(calendarIdStr).append("NonWorkingDay").append(dateStr).toString());
				if(workingDayFlag.equals(AcademyPCAConstants.STRING_N)){
					//Setting to true if Today is Off
					rdbNonWorkingDay.setSelection(true);
				}
				rdbNonWorkingDay.setLayoutData(gridDataRadio);
				rdbNonWorkingDay.setText(YRCPlatformUI.getString(AcademyPCAConstants.NON_WORKING_DAY_LABEL_TEXT));
				rdbNonWorkingDay.setFont(YRCPlatformUI.getFont("Button"));
				//rdbNonWorkingDay.setBackground(YRCPlatformUI.getBackGroundColor("Button"));
				rdbNonWorkingDay.setBackground(YRCPlatformUI.getBackGroundColor("Label"));
				rdbNonWorkingDay.setForeground((carrListIndex%2==0)? COLOR_ODD : COLOR_EVEN);
				rdbNonWorkingDay.addSelectionListener(new SelectionAdapter(){
					//adding Selection listener to Radio button
					public void widgetSelected(SelectionEvent e) {
						//to update the selection done to the target model
						modifyOrgWorkingDay(calendarIdStr, AcademyPCAConstants.STRING_N, dateStr);
					}
				});
			}	
		}
	}

	/**
	 * Method to initialize and return the workDaysComposite that houses all
	 * the dayComposite containing the working info for each day
	 * @return Composite
	 * 			<br/> - returns composite workDaysComposite
	 */
	private Composite createWorkDaysComposite() {
		// TODO Auto-generated method stub
		NodeList orgWorkingDayNL = this.orgWorkingDaysElement.getElementsByTagName(AcademyPCAConstants.ORG_WORKING_DAY_ELEMENT);
		
		//Grid Layout for the composite that houses workDaysComposite for each day
		GridLayout gridLayoutWDComp = new GridLayout();
		gridLayoutWDComp.numColumns = orgWorkingDayNL.getLength()*2;
		//Grid Data for the composite that houses workDaysComposite for each day
		GridData gridDataWDComp = new GridData();
		gridDataWDComp.horizontalAlignment = GridData.FILL;
		gridDataWDComp.verticalAlignment = GridData.CENTER;
		gridDataWDComp.grabExcessHorizontalSpace = true;
		gridDataWDComp.grabExcessVerticalSpace = false;
		
		Composite workDaysComposite = new Composite(grpManifestingDay, SWT.NONE);
		workDaysComposite.setLayout(gridLayoutWDComp);
		workDaysComposite.setLayoutData(gridDataWDComp);
		workDaysComposite.setData(YRCConstants.YRC_CONTROL_NAME, "compWorkDays");
		workDaysComposite.setBackground(YRCPlatformUI.getBackGroundColor("Composite"));
		workDaysComposite.setForeground(YRCPlatformUI.getForeGroundColor("Composite"));
		
		return workDaysComposite;
	}

	/**
	 * Method to initialize the carrierComposite that houses the
	 * carriers of the store
	 *
	 */
	private void createCarrierComposite() {
		Text txtCarrier = null; //Text object for showing Carrier Name
		
		NodeList orgWorkingDayNL = this.orgWorkingDaysElement.getElementsByTagName(AcademyPCAConstants.ORG_WORKING_DAY_ELEMENT);
		NodeList workingCalNL = ((Element)orgWorkingDayNL.item(0)).getElementsByTagName(AcademyPCAConstants.WORKING_CALENDAR_ELEMENT);
		
		GridData gridDataTxtCarrier = new GridData(); //Grid Data for text fields
		gridDataTxtCarrier.horizontalAlignment = GridData.FILL;
		gridDataTxtCarrier.verticalAlignment = GridData.BEGINNING;
		gridDataTxtCarrier.grabExcessHorizontalSpace = true;
		gridDataTxtCarrier.grabExcessVerticalSpace = true;
		gridDataTxtCarrier.verticalSpan = workingCalNL.getLength();
		
		GridData gridDataLblCal = new GridData(); //Grid Data for Haeder carrier Label
		gridDataLblCal.grabExcessHorizontalSpace = true;
		gridDataLblCal.horizontalAlignment = GridData.FILL;
		gridDataLblCal.grabExcessVerticalSpace = true;
		gridDataLblCal.verticalAlignment = GridData.CENTER;
		gridDataLblCal.verticalSpan = workingCalNL.getLength();
		
		//Grid Layout for the composite that houses the carriers of the store
		GridLayout gridLayoutCompCarrier = new GridLayout();
		gridLayoutCompCarrier.numColumns = 1; 
		//Grid Data for the composite that houses the carriers of the store
		GridData gridDataCompCarrier = new GridData();
		gridDataCompCarrier.horizontalAlignment = GridData.FILL;
		gridDataCompCarrier.grabExcessHorizontalSpace = true;
		gridDataCompCarrier.grabExcessVerticalSpace = true;
		gridDataCompCarrier.verticalAlignment = GridData.FILL;
		
		//Initializing the carrierComposite that groups the Labels, Text for each Carrier
		Composite carrierComposite = new Composite(grpManifestingDay, SWT.NONE); 
		carrierComposite.setData(YRCConstants.YRC_CONTROL_NAME, "compCarrier");
		carrierComposite.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "Composite");
		carrierComposite.setLayoutData(gridDataCompCarrier);
		carrierComposite.setLayout(gridLayoutCompCarrier);
		
		lblCalendarId = new Label(carrierComposite, SWT.READ_ONLY);
		lblCalendarId.setText(YRCPlatformUI.getString(AcademyPCAConstants.CALENDAR_ID_LABEL_TEXT));
		lblCalendarId.setLayoutData(gridDataLblCal);
		lblCalendarId.setData(YRCConstants.YRC_CONTROL_NAME, "lblCalendarId");
		lblCalendarId.setFont(YRCPlatformUI.getFont("Label"));
		lblCalendarId.setBackground(YRCPlatformUI.getBackGroundColor("Label"));
		lblCalendarId.setForeground(YRCPlatformUI.getForeGroundColor("Label"));
		
		for(int listIndex = 0; listIndex < workingCalNL.getLength(); listIndex++){
			Element workingCalElement = (Element)workingCalNL.item(listIndex);
			String calendarIdStr = workingCalElement.getAttribute(AcademyPCAConstants.CALENDAR_ID_ATTR);
			
			txtCarrier = new Text(carrierComposite, SWT.READ_ONLY);
			txtCarrier.setText(calendarIdStr);
			//Setting the control Name dynamically
			txtCarrier.setData(YRCConstants.YRC_CONTROL_NAME, 
									(new StringBuilder()).append("txt").append(AcademyPCAConstants.CALENDAR_ID_ATTR).append(calendarIdStr).toString());
			txtCarrier.setLayoutData(gridDataTxtCarrier);
			txtCarrier.setFont(YRCPlatformUI.getFont("Text"));
			txtCarrier.setForeground((listIndex%2==0)? COLOR_ODD : COLOR_EVEN);
			txtCarrier.setBackground(YRCPlatformUI.getBackGroundColor("Text"));
		}
	}

	/**
	 * Method to update the Target model for the Working Day selection changed
	 * @param calendarIdStr
	 * 			<br/> - points to the element having this "CalendarId" attribute that needs to be modified
	 * @param dayFlag
	 * 			<br/> - value for the "WorkingDay" flag attribute that needs to be updated
	 * @param dateStr
	 * 			<br/> - date in format "yyyy-MM-dd" representing the Working Day that is changed 
	 */
	private void modifyOrgWorkingDay(final String calendarIdStr, final String dayFlag, final String dateStr) {
		YRCPlatformUI.setMessage(AcademyPCAConstants.EMPTY_STRING); //to clear status message if already set
		String bindingElementString = (new StringBuilder())
											.append("/")
											.append(AcademyPCAConstants.ORG_WORKING_DAYS_ELEMENT).append("/")
											.append(AcademyPCAConstants.ORG_WORKING_DAY_ELEMENT).append("[@")
											.append(AcademyPCAConstants.DATE_ATTR).append("='").append(dateStr).append("']/")
											.append(AcademyPCAConstants.WORKING_CALENDAR_ELEMENT).append("[@")
											.append(AcademyPCAConstants.CALENDAR_ID_ATTR).append("='").append(calendarIdStr)
											.append("']").toString();
		Element workingCalElement = (Element)YRCXPathUtils.evaluate(this.orgWorkingDaysElement, bindingElementString, XPathConstants.NODE);
		workingCalElement.setAttribute(AcademyPCAConstants.WORKING_DAY_FLAG_ATTR, dayFlag);
	}
	
	/**
	 * Method to reset the selection done to the current Working Day stats for Carriers
	 * @param orgWorkingDaysElement
	 * 			<br/> - "GetCalendarWorkingDays" command output that gives the
	 * 					working day stats for all Carriers in a store
	 */
	public void resetGrpManifestingDay(Element orgWorkingDaysElement){
		this.orgWorkingDaysElement = YRCXmlUtils.getCopy(orgWorkingDaysElement);
		
		//arraylist to store the radio buttons in the Group "grpManifestingDay"
		ArrayList<Button> rdbBtnList = new ArrayList<Button>(); 
		//Calling the method to fetch all the radio buttons in Group "grpManifestingDay" into the arraylist
		getRadioBtnControls(grpManifestingDay.getChildren(), rdbBtnList);
		
		for(Button rdbBtn : rdbBtnList){ //iterating through the radio button arraylist
			String ctrlName = (String)rdbBtn.getData(YRCConstants.YRC_CONTROL_NAME); //fetching the control name
			if(ctrlName.contains("NonWorkingDay")){
				//for Radio buttons appearing in column "NO"
				String calIdStr = ctrlName.substring(3, ctrlName.indexOf("NonWorkingDay")); //fetching the carrier name
				//fetching the Working Day's Date
				String dateStr = ctrlName.substring(ctrlName.indexOf("NonWorkingDay")+"NonWorkingDay".length()); 
				String workingDayFlagXpath = (new StringBuilder())
											.append("/")
											.append(AcademyPCAConstants.ORG_WORKING_DAYS_ELEMENT).append("/")
											.append(AcademyPCAConstants.ORG_WORKING_DAY_ELEMENT).append("[@")
											.append(AcademyPCAConstants.DATE_ATTR).append("='").append(dateStr).append("']/")
											.append(AcademyPCAConstants.WORKING_CALENDAR_ELEMENT).append("[@")
											.append(AcademyPCAConstants.CALENDAR_ID_ATTR).append("='").append(calIdStr)
											.append("']/@").append(AcademyPCAConstants.WORKING_DAY_FLAG_ATTR).toString();
				//fetching the "WorkingDay" flag attribute value
				String workingDayFlag = (String)YRCXPathUtils.evaluate(this.orgWorkingDaysElement, workingDayFlagXpath, XPathConstants.STRING);
				if(workingDayFlag.equals(AcademyPCAConstants.STRING_N)){
					//if original value of "WorkingDay" flag is N, then set the radio button to true
					rdbBtn.setSelection(true);
				}else{
					rdbBtn.setSelection(false);
				}
			}else if(ctrlName.contains("WorkingDay")){
				//for Radio buttons appearing in column "YES"
				String calIdStr = ctrlName.substring(3, ctrlName.indexOf("WorkingDay")); //fetching the carrier name
				//fetching the Working Day's Date
				String dateStr = ctrlName.substring(ctrlName.indexOf("WorkingDay")+"WorkingDay".length()); 
				String workingDayFlagXpath = (new StringBuilder())
											.append("/")
											.append(AcademyPCAConstants.ORG_WORKING_DAYS_ELEMENT).append("/")
											.append(AcademyPCAConstants.ORG_WORKING_DAY_ELEMENT).append("[@")
											.append(AcademyPCAConstants.DATE_ATTR).append("='").append(dateStr).append("']/")
											.append(AcademyPCAConstants.WORKING_CALENDAR_ELEMENT).append("[@")
											.append(AcademyPCAConstants.CALENDAR_ID_ATTR).append("='").append(calIdStr)
											.append("']/@").append(AcademyPCAConstants.WORKING_DAY_FLAG_ATTR).toString();
				//fetching the "WorkingDay" flag attribute value
				String workingDayFlag = (String)YRCXPathUtils.evaluate(this.orgWorkingDaysElement, workingDayFlagXpath, XPathConstants.STRING);
				if(workingDayFlag.equals(AcademyPCAConstants.STRING_Y)){
					//if original value of "WorkingDay" flag is Y, then set the radio button to true
					rdbBtn.setSelection(true);
				}else{
					rdbBtn.setSelection(false);
				}
			}
		}
	}

	/**
	 * Method to fetch all the radio buttons in Group "grpManifestingDay" into the arraylist
	 * whose control names starts with "rdb"
	 * @param childCtrls
	 * 			<br/> - Child controls of Group and/(or) composite
	 * @param rdbBtnList
	 * 			<br/> - Arraylist for storing the Radio buttons
	 */
	private void getRadioBtnControls(Control[] childCtrls, ArrayList<Button> rdbBtnList) {
		for(Control fieldCtrl: childCtrls){
			String ctrlName = (String)fieldCtrl.getData(YRCConstants.YRC_CONTROL_NAME);
			if((fieldCtrl instanceof Button) && ctrlName!=null && ctrlName.startsWith("rdb")){
				rdbBtnList.add((Button)fieldCtrl);
			}
			if(fieldCtrl instanceof Composite){
				getRadioBtnControls(((Composite)fieldCtrl).getChildren(), rdbBtnList);
			}
		}
	}
}
