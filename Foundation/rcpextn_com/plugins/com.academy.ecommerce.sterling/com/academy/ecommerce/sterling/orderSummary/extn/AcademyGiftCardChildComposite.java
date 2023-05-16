package com.academy.ecommerce.sterling.orderSummary.extn;

import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.AcademyScreenPanelUtils;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.academy.ecommerce.sterling.util.XPathUtil;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.IYRCPanelHolder;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCTextBindingData;
import com.yantra.yfc.rcp.internal.YRCApiCaller;

public class AcademyGiftCardChildComposite extends Composite implements
		IYRCComposite {

	public ArrayList objGCCollection = new ArrayList(); // @jve:decl-index=0:

	private AcademyGiftCardLoad parentComposite = null;

	private AcademyGiftCardChildCompositeBehavior myBehavior;

	public static final String FORM_ID = "com.academy.ecommerce.sterling.orderSumary.extn.AcademyGiftCardChildComposite"; // @jve:decl-index=0:

	private Element inElm = null;

	private Composite pnlRoot = null;

	private Composite cmpDynamicLines = null;

	private ScrolledComposite cmpDynamicOrderLineScrolled = null;

	private Composite cmpAddDynamicPnlWithinOrderLine = null;

	private Composite cmpOrderLineDetails = null;

	private Text text = null;

	private Label label3 = null;

	private Text text1 = null;

	private Label label4 = null;

	public Text text2 = null;

	private Label label5 = null;

	private Label label6 = null;

	public Text txtActivatedQty = null;

	private Composite composite3 = null;

	private Composite composite4 = null;

	private Label label8 = null;

	private Label label9 = null;

	private Label label10 = null;

	private ScrolledComposite scrolledComposite1 = null;

	private Composite cmpAddDynamicGCPnlWithinOrderLine = null;

	private AcademyGCLinesComposite objGCLinesComposite = null;

	private Composite composite = null;

	org.eclipse.swt.widgets.Button activateButton;

	private boolean bNotEligibleForActivation;

	private int nNoOfShipmentLines; // @jve:decl-index=0:

	Text txtActivate;

	private Text lblActivate;

	private boolean bIsGiftItem;

	private boolean bGiftItem;

	private Composite composite1 = null;

	Text lblBulkGCCompletion;

	private Text text5;

	private Text text6;

	public AcademyGiftCardChildComposite(Composite parent, int style,
			AcademyGiftCardLoad parentComposite, String panel, Element inElm,
			boolean bNotEligibleForActivation) {
		super(parent, style);
		this.parentComposite = parentComposite;
		this.inElm = inElm;
		this.bNotEligibleForActivation = bNotEligibleForActivation;
		YRCPlatformUI
				.trace("#########Input to AcademyGiftCardChildComposite Class############"
						+ XMLUtil.getElementXMLString(inElm));
		initialize();
		setBindingForComponents();
		myBehavior = new AcademyGiftCardChildCompositeBehavior(this, FORM_ID,
				inElm);
	}

	public AcademyGiftCardChildCompositeBehavior getBehavior() {
		return myBehavior;

	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setSize(new Point(1048, 260));
		createRootPanel();
	}

	private void createRootPanel() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.numColumns = 1;
		gridLayout.verticalSpacing = 0;
		pnlRoot = new Composite(this, SWT.NONE);
		pnlRoot.setSize(new Point(1048, 260));
		pnlRoot.setLayout(gridLayout);
		createcmpDynamicLines();
	}

	private void createcmpDynamicLines() {
		GridLayout gridLayout7 = new GridLayout();
		gridLayout7.horizontalSpacing = 0;
		gridLayout7.marginWidth = 0;
		gridLayout7.marginHeight = 0;
		gridLayout7.numColumns = 3;
		gridLayout7.verticalSpacing = 0;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		gridData.heightHint = -1;
		gridData.verticalAlignment = GridData.FILL;
		cmpDynamicLines = new Composite(pnlRoot, SWT.NONE);
		cmpDynamicLines.setLayoutData(gridData);
		cmpDynamicLines.setLayout(gridLayout7);
		AcademyScreenPanelUtils.addGradientPanelHeader(cmpDynamicLines,
				"Shipment Details");
		createScrolledCompositeForDynamicLines();
	}

	private void createScrolledCompositeForDynamicLines() {
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.grabExcessVerticalSpace = true;
		gridData1.horizontalSpan = 3;
		gridData1.verticalAlignment = GridData.FILL;

		GridLayout gridLayout1 = new GridLayout(1, true);
		cmpDynamicOrderLineScrolled = new ScrolledComposite(cmpDynamicLines,
				SWT.V_SCROLL);
		cmpDynamicOrderLineScrolled.setData(YRCConstants.YRC_CONTROL_NAME,
				"cmpDynamicOrderLineScrolled");
		cmpDynamicOrderLineScrolled.setContent(cmpAddDynamicPnlWithinOrderLine);
		cmpDynamicOrderLineScrolled.setLayoutData(gridData1);
		cmpDynamicOrderLineScrolled.setExpandHorizontal(true);
		cmpDynamicOrderLineScrolled.setExpandVertical(true);
		cmpDynamicOrderLineScrolled.setAlwaysShowScrollBars(true);
		createcmpAddDynamicPnlWithinOrderLine();
		cmpDynamicOrderLineScrolled.setLayout(gridLayout1);
		cmpDynamicOrderLineScrolled.setContent(cmpAddDynamicPnlWithinOrderLine);
	}

	private void createcmpAddDynamicPnlWithinOrderLine() {
		GridLayout gridLayout6 = new GridLayout();
		gridLayout6.horizontalSpacing = 0;
		gridLayout6.marginWidth = 0;
		gridLayout6.marginHeight = 0;
		gridLayout6.numColumns = 1;
		gridLayout6.verticalSpacing = 0;
		GridData gridData2 = new GridData();
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.verticalAlignment = GridData.FILL;
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.grabExcessVerticalSpace = true;
		cmpAddDynamicPnlWithinOrderLine = new Composite(
				cmpDynamicOrderLineScrolled, SWT.BORDER);
		cmpAddDynamicPnlWithinOrderLine.setLayout(gridLayout6);
		cmpAddDynamicPnlWithinOrderLine.setLayoutData(gridData2);
		createCmpOrderLineDetails();
		createOrderLineShipToAddress();
		createComposite3();
		cmpAddDynamicPnlWithinOrderLine.setSize(cmpAddDynamicPnlWithinOrderLine
				.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		Label filler1 = new Label(cmpAddDynamicPnlWithinOrderLine, SWT.NONE);
		createComposite();
		cmpDynamicOrderLineScrolled.setMinSize(cmpAddDynamicPnlWithinOrderLine
				.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void createComposite3() {
		GridLayout gridLayout5 = new GridLayout();
		gridLayout5.horizontalSpacing = 0;
		gridLayout5.marginWidth = 0;
		gridLayout5.marginHeight = 0;
		gridLayout5.numColumns = 1;
		gridLayout5.verticalSpacing = 0;
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = GridData.FILL;
		gridData4.grabExcessHorizontalSpace = true;
		gridData4.grabExcessVerticalSpace = true;
		gridData4.verticalSpan = 2;
		gridData4.verticalAlignment = GridData.FILL;
		composite3 = new Composite(cmpAddDynamicPnlWithinOrderLine, SWT.NONE);
		createComposite4();
		composite3.setLayoutData(gridData4);
		composite3.setLayout(gridLayout5);
		createScrolledComposite1();
	}

	private void createScrolledComposite1() {
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.grabExcessVerticalSpace = true;
		gridData1.heightHint = -1;
		gridData1.verticalAlignment = GridData.FILL;
		GridLayout gridLayout1 = new GridLayout(1, true);
		scrolledComposite1 = new ScrolledComposite(composite3, SWT.V_SCROLL);
		scrolledComposite1.setData(YRCConstants.YRC_CONTROL_NAME,
				"scrolledComposite1");
		scrolledComposite1.setContent(cmpAddDynamicGCPnlWithinOrderLine);
		scrolledComposite1.setLayoutData(gridData1);
		scrolledComposite1.setExpandHorizontal(true);
		scrolledComposite1.setExpandVertical(true);
		scrolledComposite1.setAlwaysShowScrollBars(false);
		createcmpAddDynamicGCPnlWithinOrderLine();
		scrolledComposite1.setLayout(gridLayout1);
		scrolledComposite1.setContent(cmpAddDynamicGCPnlWithinOrderLine);
	}

	private void createcmpAddDynamicGCPnlWithinOrderLine() {
		GridLayout gridLayout6 = new GridLayout();
		gridLayout6.horizontalSpacing = 0;
		gridLayout6.marginWidth = 0;
		gridLayout6.marginHeight = 0;
		gridLayout6.numColumns = 1;
		gridLayout6.verticalSpacing = 0;
		GridData gridData2 = new GridData();
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.heightHint = -1;
		gridData2.grabExcessVerticalSpace = true;
		cmpAddDynamicGCPnlWithinOrderLine = new Composite(scrolledComposite1,
				SWT.NONE);
		cmpAddDynamicGCPnlWithinOrderLine.setLayout(gridLayout6);
		cmpAddDynamicGCPnlWithinOrderLine.setLayoutData(gridData2);
		determineNoOfShipmentLines();
		cmpAddDynamicGCPnlWithinOrderLine
				.setSize(cmpAddDynamicGCPnlWithinOrderLine.computeSize(
						SWT.DEFAULT, SWT.DEFAULT));
		scrolledComposite1.setMinSize(cmpAddDynamicGCPnlWithinOrderLine
				.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void determineNoOfShipmentLines() {
		try {
			NodeList nShipmentLine = XPathUtil.getNodeList(inElm,
					"ShipmentLines/ShipmentLine");
			if (!YRCPlatformUI.isVoid(nShipmentLine)) {
				nNoOfShipmentLines = ((NodeList) nShipmentLine).getLength();
				for (int i = 0; i < nNoOfShipmentLines; i++) {
					Element eleShipmentLine = (Element) nShipmentLine.item(i);
					String strItemID = eleShipmentLine
							.getAttribute(AcademyPCAConstants.ATTR_ITEM_ID);
					String strQty = eleShipmentLine
							.getAttribute(AcademyPCAConstants.ATTR_QUANTITY);
					int iQuantity = Integer.parseInt(strQty.substring(0, strQty
							.lastIndexOf(".")));
					bIsGiftItem = callItemListApiToDetermineItem(strItemID);
					if ((bIsGiftItem) && (iQuantity > 50)) {
						createGCLinesForOrder(eleShipmentLine);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public boolean callItemListApiToDetermineItem(String strItemID) {
		bGiftItem = false;
		try {
			Document docInput = XMLUtil
					.createDocument(AcademyPCAConstants.ATTR_ITEM);
			Element eleItem = docInput.getDocumentElement();
			eleItem.setAttribute(AcademyPCAConstants.ATTR_ITEM_ID, strItemID);
			Document outDoc = invokeSyncAPI(docInput, "getItemList");
			Element outElem = outDoc.getDocumentElement();
			try {
				String strExtnIsGiftCard = XPathUtil.getString(outElem,
						"Item/Extn/@ExtnIsGiftCard");
				if ((AcademyPCAConstants.ATTR_Y).equals(strExtnIsGiftCard)) {
					bGiftItem = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return bGiftItem;

	}

	private AcademyGCLinesComposite createGCLinesForOrder(
			Element eleShipmentLine) {
		objGCLinesComposite = new AcademyGCLinesComposite(
				cmpAddDynamicGCPnlWithinOrderLine, SWT.NONE, inElm,
				eleShipmentLine);

		objGCLinesComposite.setData(YRCConstants.YRC_CONTROL_NAME,
				"objGCLinesComposite");
		objGCCollection.add(objGCLinesComposite);
		return objGCLinesComposite;

	}

	/**
	 * This method initializes composite4
	 * 
	 */
	private void createComposite4() {
		GridData gridData12 = new GridData();
		gridData12.horizontalAlignment = GridData.BEGINNING;
		gridData12.grabExcessHorizontalSpace = false;
		gridData12.widthHint = -1;
		gridData12.horizontalIndent = 120;
		gridData12.verticalAlignment = GridData.CENTER;
		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = GridData.BEGINNING;
		gridData11.verticalAlignment = GridData.CENTER;
		gridData11.grabExcessHorizontalSpace = true;
		gridData11.horizontalIndent = 80;
		GridData gridData10 = new GridData();
		gridData10.horizontalAlignment = GridData.BEGINNING;
		gridData10.grabExcessHorizontalSpace = true;
		gridData10.horizontalIndent = 90;
		gridData10.verticalAlignment = GridData.CENTER;
		GridLayout gridLayout3 = new GridLayout();
		gridLayout3.numColumns = 3;
		gridLayout3.horizontalSpacing = 0;
		gridLayout3.verticalSpacing = 0;
		gridLayout3.marginWidth = 0;
		gridLayout3.marginHeight = 0;
		gridLayout3.makeColumnsEqualWidth = true;
		GridData gridData7 = new GridData();
		gridData7.horizontalAlignment = GridData.FILL;
		gridData7.grabExcessHorizontalSpace = true;
		gridData7.verticalAlignment = GridData.CENTER;
		composite4 = new Composite(composite3, SWT.NONE);
		composite4.setLayoutData(gridData7);
		composite4.setLayout(gridLayout3);
		label8 = new Label(composite4, SWT.NONE);
		label8.setText("Bulk GC Shipment Line#");
		label8.setLayoutData(gridData10);
		label9 = new Label(composite4, SWT.NONE);
		label9.setText("Item ID");
		label9.setLayoutData(gridData11);
		label10 = new Label(composite4, SWT.NONE);
		label10.setText("Qty on ShipmentLine");
		label10.setLayoutData(gridData12);

	}

	private void createCmpOrderLineDetails() {
		GridData gridData16 = new GridData();
		gridData16.horizontalAlignment = GridData.END;
		gridData16.verticalAlignment = GridData.CENTER;
		GridData gridData15 = new GridData();
		gridData15.horizontalAlignment = GridData.END;
		gridData15.verticalAlignment = GridData.CENTER;
		GridData gridData14 = new GridData();
		gridData14.horizontalAlignment = GridData.END;
		gridData14.horizontalIndent = 0;
		gridData14.verticalAlignment = GridData.CENTER;
		GridData gridData22 = new GridData();
		gridData22.widthHint = -1;
		gridData22.verticalAlignment = GridData.CENTER;
		gridData22.horizontalAlignment = GridData.FILL;
		GridData gridData21 = new GridData();
		gridData21.widthHint = -1;
		gridData21.verticalAlignment = GridData.CENTER;
		gridData21.horizontalAlignment = GridData.BEGINNING;
		GridData gridData20 = new GridData();
		gridData20.widthHint = -1;
		gridData20.verticalAlignment = GridData.CENTER;
		gridData20.grabExcessHorizontalSpace = true;
		gridData20.horizontalAlignment = GridData.FILL;
		GridData gridData19 = new GridData();
		gridData19.horizontalAlignment = GridData.FILL;
		gridData19.grabExcessHorizontalSpace = false;
		gridData19.horizontalSpan = 2;
		gridData19.verticalAlignment = GridData.CENTER;
		GridData gridData18 = new GridData();
		gridData18.widthHint = -1;
		gridData18.horizontalAlignment = GridData.FILL;
		gridData18.verticalAlignment = GridData.CENTER;
		gridData18.horizontalSpan = 2;
		gridData18.grabExcessHorizontalSpace = true;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 7;
		gridLayout1.horizontalSpacing = 5;
		gridLayout1.verticalSpacing = 5;
		gridLayout1.marginWidth = 0;
		gridLayout1.marginHeight = 5;
		gridLayout1.makeColumnsEqualWidth = false;
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.FILL;
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.grabExcessVerticalSpace = false;
		gridData3.verticalAlignment = GridData.CENTER;
		cmpOrderLineDetails = new Composite(cmpAddDynamicPnlWithinOrderLine,
				SWT.BORDER);
		cmpOrderLineDetails.setLayoutData(gridData3);
		cmpOrderLineDetails.setLayout(gridLayout1);
		label3 = new Label(cmpOrderLineDetails, SWT.NONE);
		label3.setText("Shipment#");
		label3.setLayoutData(gridData14);
		text = new Text(cmpOrderLineDetails, SWT.NONE | SWT.READ_ONLY);
		text.setLayoutData(gridData18);
		label4 = new Label(cmpOrderLineDetails, SWT.NONE);
		label4.setText("No.of ShipmentLine");
		text2 = new Text(cmpOrderLineDetails, SWT.NONE | SWT.READ_ONLY);
		text2.setLayoutData(gridData20);
		createComposite1();
		Label filler = new Label(cmpOrderLineDetails, SWT.NONE);
		label5 = new Label(cmpOrderLineDetails, SWT.NONE);
		label5.setText("SCAC");
		label5.setLayoutData(gridData15);
		text1 = new Text(cmpOrderLineDetails, SWT.NONE | SWT.READ_ONLY);
		text1.setLayoutData(gridData19);
		label6 = new Label(cmpOrderLineDetails, SWT.NONE);
		label6.setText("Activation Code");
		label6.setLayoutData(gridData16);
		txtActivatedQty = new Text(cmpOrderLineDetails, SWT.NONE
				| SWT.READ_ONLY);
		txtActivatedQty.setLayoutData(gridData22);
	}

	private void setBindingForComponents() {
		// Text Bindings
		YRCTextBindingData textBindingData = new YRCTextBindingData();
		textBindingData.setSourceBinding("Shipment:Shipment/@ShipmentNo");
		textBindingData.setName("TxtShipmentNo");
		text.setData(YRCConstants.YRC_TEXT_BINDING_DEFINATION, textBindingData);

		textBindingData = new YRCTextBindingData();
		textBindingData.setSourceBinding("Shipment:Shipment/@SCAC");
		textBindingData.setName("TxtItemID");
		text1
				.setData(YRCConstants.YRC_TEXT_BINDING_DEFINATION,
						textBindingData);

		textBindingData = new YRCTextBindingData();
		textBindingData
				.setSourceBinding("Shipment:Shipment/ShipmentLines/@TotalNumberOfRecords");
		textBindingData.setName("No of Shipment Lines");
		text2
				.setData(YRCConstants.YRC_TEXT_BINDING_DEFINATION,
						textBindingData);

		textBindingData = new YRCTextBindingData();
		textBindingData
				.setSourceBinding("Shipment:Shipment/Extn/@ExtnGCActivationCode");
		textBindingData.setName("Total Quantity Scanned");
		txtActivatedQty.setData(YRCConstants.YRC_TEXT_BINDING_DEFINATION,
				textBindingData);

		textBindingData = new YRCTextBindingData();
		textBindingData
				.setSourceBinding("Shipment:Shipment/ToAddress/@AddressLine1;Shipment:Shipment/ToAddress/@AddressLine2;Shipment:Shipment/ToAddress/@City;Shipment:Shipment/ToAddress/@State;Shipment:Shipment/ToAddress/@ZipCode;Shipment:Shipment/ToAddress/@Country");
		textBindingData.setName("Ship To Address");
		textBindingData.setKey("Key_To_Display_The_Sequence_Of_Binding");
		text5
				.setData(YRCConstants.YRC_TEXT_BINDING_DEFINATION,
						textBindingData);
		
		textBindingData = new YRCTextBindingData();
		textBindingData
				.setSourceBinding("Shipment:Shipment/ToAddress/@Title;Shipment:Shipment/ToAddress/@FirstName;Shipment:Shipment/ToAddress/@LastName");
		textBindingData.setName("Ship To Address");
		textBindingData.setKey("Key_To_Display_The_Sequence_Of_Binding1");
		text6
				.setData(YRCConstants.YRC_TEXT_BINDING_DEFINATION,
						textBindingData);

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

	/**
	 * This method initializes composite5
	 * 
	 */
	private void createOrderLineShipToAddress() {
		GridData gridData6 = new GridData();
		gridData6.horizontalAlignment = GridData.FILL;
		gridData6.grabExcessHorizontalSpace = true;
		gridData6.heightHint = 90;
		gridData6.widthHint = 120;
		gridData6.verticalAlignment = GridData.CENTER;
		GridData gridData5 = new GridData();
		gridData5.verticalSpan = 2;
		gridData5.horizontalAlignment = GridData.FILL;
		gridData5.verticalAlignment = GridData.CENTER;
		gridData5.grabExcessHorizontalSpace = true;
		gridData5.widthHint = -1;
		gridData5.grabExcessVerticalSpace = false;
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.horizontalSpacing = 0;
		gridLayout2.marginWidth = 0;
		gridLayout2.marginHeight = 2;
		gridLayout2.makeColumnsEqualWidth = false;
		gridLayout2.numColumns = 1;
		gridLayout2.verticalSpacing = 2;
	}

	/**
	 * This method initializes composite
	 * 
	 */
	private void createComposite() {
		GridData gridData23 = new GridData();
		gridData23.horizontalAlignment = GridData.FILL;
		gridData23.grabExcessHorizontalSpace = true;
		gridData23.verticalAlignment = GridData.CENTER;
		GridData gridData9 = new GridData();
		gridData9.grabExcessVerticalSpace = false;
		gridData9.verticalAlignment = GridData.FILL;
		gridData9.grabExcessHorizontalSpace = false;
		gridData9.horizontalIndent = 0;
		gridData9.widthHint = -1;
		gridData9.horizontalAlignment = GridData.BEGINNING;
		GridLayout gridLayout4 = new GridLayout();
		gridLayout4.numColumns = 4;
		gridLayout4.makeColumnsEqualWidth = false;
		GridData gridData13 = new GridData();
		gridData13.horizontalAlignment = GridData.BEGINNING;
		gridData13.grabExcessHorizontalSpace = false;
		gridData13.grabExcessVerticalSpace = false;
		gridData13.widthHint = -1;
		gridData13.verticalAlignment = GridData.CENTER;
		GridData gridData8 = new GridData();
		gridData8.horizontalAlignment = GridData.FILL;
		gridData8.grabExcessHorizontalSpace = true;
		gridData8.verticalAlignment = GridData.CENTER;
		composite = new Composite(cmpAddDynamicPnlWithinOrderLine, SWT.BORDER);
		composite.setLayoutData(gridData8);
		composite.setLayout(gridLayout4);
		lblActivate = new org.eclipse.swt.widgets.Text(composite, SWT.NONE);
		lblActivate.setText("Enter Activation Code Here:");
		txtActivate = new org.eclipse.swt.widgets.Text(composite, SWT.BORDER);
		txtActivate.setLayoutData(gridData9);
		activateButton = new org.eclipse.swt.widgets.Button(composite, SWT.NONE);
		activateButton.setLayoutData(gridData13);
		activateButton.setText("Activate GC For Shipment");
		lblBulkGCCompletion = new Text(composite, SWT.NONE);
		lblBulkGCCompletion.setLayoutData(gridData23);
		lblBulkGCCompletion.setEnabled(false);
		lblBulkGCCompletion.setEditable(false);
		lblBulkGCCompletion.setVisible(false);

		if (bNotEligibleForActivation) {
			activateButton.setEnabled(false);
			lblBulkGCCompletion
					.setText("This shipment has not been shipped, so activation process cannot start");
			lblBulkGCCompletion.setVisible(true);
		} else {
			String strGCBulkActivationFlag;

			try {
				strGCBulkActivationFlag = XPathUtil.getString(inElm,
						"Extn/@ExtnGCBulkActivationFlag");
				if ("Y".equals(strGCBulkActivationFlag)) {
					activateButton.setEnabled(false);
					lblBulkGCCompletion
							.setText("The GC Lines belonging to this shipment has already been sent for activation");
					lblBulkGCCompletion.setVisible(true);
					lblBulkGCCompletion.setEnabled(true);
				} else {
					activateButton
							.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
								public void widgetSelected(
										org.eclipse.swt.events.SelectionEvent e) {
									myBehavior.formMessageForBulkGCShipment();
								}

							});
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private Document invokeSyncAPI(Document docCustomer, String strAPIName) {
		YRCApiContext context = new YRCApiContext();
		YRCApiCaller syncapiCaller = new YRCApiCaller(context, true);
		context.setApiName(strAPIName);
		context.setFormId(AcademyPCAConstants.FORM_ID_LOAD_GIFT_CARD_PARENT);
		context.setInputXml(docCustomer);
		syncapiCaller.invokeApi();
		Document outDoc = context.getOutputXml();
		return outDoc;
	}

	/**
	 * This method initializes composite1
	 * 
	 */
	private void createComposite1() {
		GridData gridData24 = new GridData();
		gridData24.horizontalAlignment = GridData.FILL;
		gridData24.grabExcessHorizontalSpace = true;
		gridData24.horizontalSpan = 3;
		gridData24.grabExcessVerticalSpace = true;
		gridData24.widthHint = 120;
		gridData24.verticalAlignment = GridData.FILL;
		GridLayout gridLayout8 = new GridLayout();
		gridLayout8.makeColumnsEqualWidth = false;
		gridLayout8.horizontalSpacing = 0;
		gridLayout8.verticalSpacing = 0;
		gridLayout8.marginWidth = 0;
		gridLayout8.marginHeight = 0;
		gridLayout8.numColumns = 3;
		GridData gridData17 = new GridData();
		gridData17.horizontalAlignment = GridData.FILL;
		gridData17.grabExcessVerticalSpace = true;
		gridData17.grabExcessHorizontalSpace = true;
		gridData17.verticalSpan = 4;
		gridData17.verticalAlignment = GridData.FILL;
		composite1 = new Composite(cmpOrderLineDetails, SWT.NONE | SWT.BORDER);
		composite1.setLayoutData(gridData17);
		composite1.setLayout(gridLayout8);
		AcademyScreenPanelUtils.addGradientPanelHeader(composite1,
				"ShipTo Address");
		text6 = new Text(composite1, SWT.NONE | SWT.READ_ONLY | SWT.WRAP);
		text6.setLayoutData(gridData24);
		text5 = new Text(composite1, SWT.NONE | SWT.READ_ONLY | SWT.WRAP);
		text5.setLayoutData(gridData24);
	}
} // @jve:decl-index=0:visual-constraint="10,10"
