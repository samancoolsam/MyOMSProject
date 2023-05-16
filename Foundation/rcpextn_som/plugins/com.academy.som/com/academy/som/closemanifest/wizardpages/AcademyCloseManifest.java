package com.academy.som.closemanifest.wizardpages;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.IYRCPanelHolder;
import com.yantra.yfc.rcp.YRCButtonBindingData;
import com.yantra.yfc.rcp.YRCLabelBindingData;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCWizardBehavior;

public class AcademyCloseManifest
extends Composite
implements IYRCComposite
{
private Composite pnlRoot = null;
public static final String FORM_ID = "com.academy.som.closemanifest.wizardpages.AcademyCloseManifest";
private YRCWizardBehavior wizBehavior;
public AcademyCloseManifestBehavior myBehavior;
private Composite pnlCloseManifest = null;
private Label lblNoOfManifests = null;
private Label lblManifestInstruction = null;
private ScrolledComposite pnlScroll = null;
public Composite pnlManifestList = null;
private Composite pnlButtons = null;
public Button btnClose = null;

public AcademyCloseManifest(Composite parent, int style)
{
  super(parent, style);
  initialize();
  setBindingForComponents();
  setControlNames();
  this.myBehavior = new AcademyCloseManifestBehavior(this, "com.academy.som.closemanifest.wizardpages.AcademyCloseManifest");
}

private void setControlNames()
{
  this.lblManifestInstruction.setData("name", "lblManifestInstruction");
  
  this.pnlManifestList.setData("name", "pnlManifestList");
  this.pnlButtons.setData("name", "pnlButtons");
  this.pnlCloseManifest.setData("name", "pnlCloseManifest");
  this.pnlRoot.setData("name", "pnlRoot");
  this.pnlScroll.setData("name", "pnlScroll");
}

private void setBindingForComponents()
{
  YRCLabelBindingData noOfManifestBindingData = new YRCLabelBindingData();
  noOfManifestBindingData.setName("lblNoOfManifests");
  noOfManifestBindingData.setSourceBinding("OpenManifestList:/Manifests/@TotalNumberOfRecords");
  this.lblNoOfManifests.setData("YRCLabelBindingDefinition", noOfManifestBindingData);
  
  setBindingForButtons();
}

private void setBindingForButtons()
{
  YRCButtonBindingData btnCloseData = new YRCButtonBindingData();
  btnCloseData.setActionId("Close_Manifest_Confirm_Action");
  btnCloseData.setName("btnClose");
  this.btnClose.setData("YRCButtonBindingDefination", btnCloseData);
}

private void initialize()
{
  createRootPanel();
  setLayout(new FillLayout());
  setSize(new Point(466, 296));
}

public String getFormId()
{
  return "com.academy.som.closemanifest.wizardpages.AcademyCloseManifest";
}

public Composite getRootPanel()
{
  return this.pnlRoot;
}

public YRCWizardBehavior getWizardBehavior()
{
  return this.wizBehavior;
}

public void setWizBehavior(YRCWizardBehavior wizBehavior)
{
  this.wizBehavior = wizBehavior;
}

private void createRootPanel()
{
  GridLayout gridLayout = new GridLayout();
  gridLayout.numColumns = 1;
  gridLayout.marginHeight = 5;
  gridLayout.marginWidth = 5;
  this.pnlRoot = new Composite(this, 0);
  this.pnlRoot.setData("yrc:customType", "TaskComposite");
  this.pnlRoot.setLayout(gridLayout);
  createPnlManifestList1();
  createPnlButtons();
}

public boolean canFlipToNextPage()
{
  return false;
}

public IYRCPanelHolder getPanelHolder()
{
  return null;
}

public String getHelpId()
{
  return "";
}

private void createPnlManifestList1()
{
  GridLayout gridLayout1 = new GridLayout();
  gridLayout1.numColumns = 3;
  gridLayout1.marginHeight = 1;
  GridData gridData = new GridData();
  gridData.horizontalAlignment = 4;
  gridData.grabExcessHorizontalSpace = true;
  gridData.grabExcessVerticalSpace = true;
  gridData.heightHint = 200;
  gridData.verticalAlignment = 1;
  this.pnlCloseManifest = new Composite(getRootPanel(), 0);
  addPanelHeader(this.pnlCloseManifest, "PanelHeader_Close_Manifest", "PnlHdr_closeManifest", null, 3);
  this.pnlCloseManifest.setLayoutData(gridData);
  this.pnlCloseManifest.setLayout(gridLayout1);
  this.lblNoOfManifests = new Label(this.pnlCloseManifest, 0);
  this.lblNoOfManifests.setText("");
  this.lblManifestInstruction = new Label(this.pnlCloseManifest, 0);
  this.lblManifestInstruction.setText("lbl_Manifest_Found_Instruction");
  createPnlManifestList();
}

private void createPnlManifestList()
{
		GridData gridData1 = new GridData();
		gridData1.horizontalSpan = 3;
		gridData1.verticalAlignment = 4;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.grabExcessVerticalSpace = true;
		gridData1.horizontalAlignment = 4;
		this.pnlScroll = new ScrolledComposite(this.pnlCloseManifest, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
		this.pnlScroll.setLayout(new GridLayout());
		createPnlTest();
		this.pnlScroll.setLayoutData(gridData1);
		this.pnlScroll.setContent(this.pnlManifestList);


		//this.pnlManifestList = new Composite(this.pnlCloseManifest, SWT.NONE);
		//this.pnlScroll.setLayoutData(gridData1);
		//createPnlTest();
		//this.pnlManifestList.setLayoutData(gridData1);
		//this.pnlScroll.setContent(this.pnlManifestList);
		//this.pnlScroll.setExpandHorizontal(true);
		//this.pnlScroll.setExpandVertical(true);
}

public void createRadioButtonsForManifests(String[] radioDisplayTexts, String[] manifestKeys)
{
  clearEarlierButtons();
  for (int buttonCount = 0; buttonCount < radioDisplayTexts.length; buttonCount++)
  {
    String radioInstruction = radioDisplayTexts[buttonCount];
    String manifestKey = manifestKeys[buttonCount];
    
    Button rbnNewManifest = new Button(this.pnlManifestList, 16);
    rbnNewManifest.setData("yrc:customType", "DisplayText");
    rbnNewManifest.setText(radioInstruction);
	
	GridData gridDataRdb = new GridData();
    gridDataRdb.grabExcessHorizontalSpace = true;
    gridDataRdb.widthHint=400;
    rbnNewManifest.setLayoutData(gridDataRdb);
	
    YRCButtonBindingData rbnButtonBindingData = new YRCButtonBindingData();
    rbnButtonBindingData.setName(manifestKey);
    rbnButtonBindingData.setCheckedBinding(manifestKey);
    rbnButtonBindingData.setTargetBinding("SelectedManifestForCloseOperation:/Manifest/@SelectedManifest");
    rbnNewManifest.setData("YRCButtonBindingDefination", rbnButtonBindingData);
    
    this.myBehavior.addAndBindNewControl(this.pnlManifestList);
  }
		this.pnlScroll.layout(true, true);
		this.pnlRoot.layout(true, true);

		this.pnlScroll.setExpandHorizontal(true);
		this.pnlScroll.setExpandVertical(true);
		this.pnlScroll.setMinSize(this.pnlManifestList.computeSize(SWT.DEFAULT, SWT.DEFAULT));
}

private void clearEarlierButtons()
{
  Control[] radiobuttons = this.pnlManifestList.getChildren();
  for (int buttonCount = 0; buttonCount < radiobuttons.length; buttonCount++) {
    radiobuttons[buttonCount].dispose();
  }
}

private void createPnlTest()
{
  this.pnlManifestList = new Composite(this.pnlScroll, SWT.NONE);
  this.pnlManifestList.setLayout(new GridLayout());
}

private void createPnlButtons()
{
  GridData gridData3 = new GridData();
  gridData3.grabExcessHorizontalSpace = true;
  gridData3.verticalAlignment = 2;
  gridData3.horizontalAlignment = 3;
  GridData gridData2 = new GridData();
  gridData2.horizontalAlignment = 4;
  gridData2.grabExcessHorizontalSpace = true;
  gridData2.verticalAlignment = 2;
  this.pnlButtons = new Composite(getRootPanel(), 0);
  this.pnlButtons.setData("yrc:customType", "TaskComposite");
  this.pnlButtons.setLayout(new GridLayout());
  this.pnlButtons.setLayoutData(gridData2);
  this.btnClose = new Button(this.pnlButtons, 0);
  this.btnClose.setData("yrc:customType", "TaskComposite");
  this.btnClose.setText("btn_Close_Manifest");
  this.btnClose.setLayoutData(gridData3);
  SelectionAdapter closeAdapter = new SelectionAdapter()
  {
    public void widgetSelected(SelectionEvent e)
    {
      YRCPlatformUI.fireAction("Close_Manifest_Confirm_Action");
    }
  };
  this.btnClose.addSelectionListener(closeAdapter);
  this.btnClose.setData("yrc:validationSet", closeAdapter);
}

public Composite getPnlManifestList()
{
  return this.pnlManifestList;
}

public AcademyCloseManifestBehavior getMyBehavior()
{
  return this.myBehavior;
}

public static void addPanelHeader(final Composite composite, String sPanelHeader, String lblName, String sPanelImageTheme, int colSpan)
{
  GridData gridData = new GridData();
  gridData.horizontalIndent = 5;
  gridData.heightHint = 17;
  gridData.horizontalAlignment = 4;
  gridData.horizontalSpan = colSpan;
  gridData.grabExcessHorizontalSpace = true;
  
  final Label lblPanelTitle = new Label(composite, 0);
  lblPanelTitle.setData("yrc:customType", "PanelHeader");
  lblPanelTitle.setData("name", lblName);
  
  lblPanelTitle.setText(sPanelHeader);
  lblPanelTitle.setLayoutData(gridData);
  
  composite.addPaintListener(new PaintListener()
  {
    public void paintControl(PaintEvent e)
    {
      //GC gc = new GC(this.val$composite);
      GC gc = new GC(composite);
      //Rectangle r = this.val$composite.getClientArea();
      Rectangle r = composite.getClientArea();
      
      Rectangle r1 = lblPanelTitle.getBounds();
      gc.setBackground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
      gc.fillRectangle(r.x, r.y, r.width, r1.height + 1);
      
      gc.setForeground(Display.getCurrent().getSystemColor(15));
      
      gc.drawLine(r.x, r1.y + r1.height, r.x + r.width - 2, r1.y + r1.height);
      
      gc.setForeground(YRCPlatformUI.getBackGroundColor("TaskComposite"));
      gc.drawLine(r.x + r.width - 1, r.y, r.x + r.width - 1, r.y + r.height - 1);
      gc.drawLine(r.x, r.y + r.height - 1, r.x + r.width - 1, r.y + r.height - 1);
      gc.drawLine(r.x, r.y, r.x + r.width, r.y);
      gc.drawLine(r.x, r.y, r.x, r.y + r.height - 1);
      gc.dispose();
    }
  });
}

}
