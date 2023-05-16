package com.academy.ecommerce.sterling.orderSummary.extn;

import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
import com.yantra.yfc.rcp.IYRCRefreshable;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCTextBindingData;
import com.yantra.yfc.rcp.internal.YRCApiCaller;

public class AcademyGiftCardLoad extends Composite implements IYRCComposite,
		IYRCRefreshable {
	public AcademyGiftCardLoadBehavior myBehavior;

	private Composite pnlRoot = null;

	private Composite cmpRecipient = null;

	private Element inElm = null; // @jve:decl-index=0:

	public static final String FORM_ID = "com.academy.ecommerce.sterling.orderSummary.extn.AcademyGiftCardLoad"; // @jve:decl-index=0:

	private Composite OrderHeaderPnlComposite = null;

	private Composite OrderDetailsComposite = null;

	private Label LblOrderNo = null;

	private Label LblOrderStatus = null;

	private ScrolledComposite scrolledComposite = null;

	private AcademyGiftCardChildComposite objChildComposite = null;

	public ArrayList objOrderCollection = new ArrayList(); // @jve:decl-index=0:

	private Composite cmpAddDynamicPnlForOrderLine = null;

	String SHIPMENT_PANEL = "Y"; // @jve:decl-index=0:

	int i = 0;

	private Label label1 = null;

	String CONSUMER_ORDER = "Y";

	private int iNoOfShipments;

	private NodeList nListofShipment;

	private NodeList nListOfShipmentLine; // @jve:decl-index=0:

	private int iNoOfShipmentLines;

	private boolean bGiftItem;

	private Text txtOrderNo;

	private Text txtEnterpriseCode;

	private boolean bNotEligibleForActivation;

	public AcademyGiftCardLoad(Composite parent, int style, Element inElm) {
		super(parent, style);
		this.inElm = inElm;
		YRCPlatformUI
				.trace("####### Input Element into AcademyGiftCardLoad Screen is:"
						+ XMLUtil.getElementXMLString(inElm));

		myBehavior = new AcademyGiftCardLoadBehavior(this, FORM_ID, inElm);
		initialize();
		setBindingForComponents();
		myBehavior = new AcademyGiftCardLoadBehavior(this, FORM_ID, inElm);
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

	private void createPnlRoot() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 5;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 5;
		gridLayout.makeColumnsEqualWidth = false;
		pnlRoot = new Composite(this, SWT.NONE);
		pnlRoot.setLayout(gridLayout);
		pnlRoot.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "TaskComposite");
		pnlRoot.setBackground(new Color(null, 255, 255, 255));
		createOrderHeaderPnlComposite();
		Label filler = new Label(pnlRoot, SWT.NONE);
		Label filler1 = new Label(pnlRoot, SWT.NONE);
		createCompositeForScroll();
	}

	private void createCompositeForScroll() {
		GridLayout gridLayout4 = new GridLayout();
		gridLayout4.horizontalSpacing = 0;
		gridLayout4.marginWidth = 0;
		gridLayout4.numColumns = 4;
		gridLayout4.marginHeight = 0;
		gridLayout4.verticalSpacing = 0;
		GridData gridData19 = new GridData();
		gridData19.horizontalAlignment = GridData.FILL;
		gridData19.grabExcessHorizontalSpace = true;
		gridData19.grabExcessVerticalSpace = true;
		gridData19.horizontalSpan = 4;
		gridData19.verticalAlignment = GridData.FILL;
		cmpRecipient = new Composite(pnlRoot, SWT.NONE);
		cmpRecipient.setLayoutData(gridData19);
		cmpRecipient.setLayout(gridLayout4);
		createScrolledComposite();

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
		return this;
	}

	/**
	 * This method initializes OrderHeaderPnlComposite
	 * 
	 */
	private void createOrderHeaderPnlComposite() {
		GridLayout gridLayout5 = new GridLayout();
		gridLayout5.marginWidth = 0;
		gridLayout5.numColumns = 1;
		gridLayout5.makeColumnsEqualWidth = true;
		gridLayout5.horizontalSpacing = 0;
		gridLayout5.verticalSpacing = 0;
		gridLayout5.marginHeight = 0;
		GridData gridData5 = new org.eclipse.swt.layout.GridData();
		gridData5.grabExcessHorizontalSpace = true;
		gridData5.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		gridData5.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridData gridData = new org.eclipse.swt.layout.GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.grabExcessVerticalSpace = false;
		gridData.horizontalAlignment = GridData.FILL;
		gridLayout5.makeColumnsEqualWidth = true;
		OrderHeaderPnlComposite = new Composite(pnlRoot, SWT.NONE);
		OrderHeaderPnlComposite.setLayoutData(gridData);
		OrderHeaderPnlComposite.setLayout(gridLayout5);
		AcademyScreenPanelUtils.addGradientPanelHeader(OrderHeaderPnlComposite,
				"Order Details");
		createOrderDetailsComposite();
	}

	/**
	 * This method initializes OrderDetailsComposite
	 * 
	 */
	private void createOrderDetailsComposite() {
		GridData gridData8 = new GridData();
		gridData8.horizontalAlignment = GridData.CENTER;
		gridData8.grabExcessHorizontalSpace = false;
		gridData8.verticalAlignment = GridData.CENTER;
		GridData gridData30 = new GridData();
		gridData30.horizontalAlignment = GridData.FILL;
		gridData30.verticalAlignment = GridData.CENTER;
		GridData gridData26 = new GridData();
		gridData26.horizontalAlignment = GridData.BEGINNING;
		gridData26.widthHint = 55;
		gridData26.verticalAlignment = GridData.CENTER;
		GridData gridData24 = new GridData();
		gridData24.horizontalAlignment = GridData.FILL;
		gridData24.grabExcessVerticalSpace = false;
		gridData24.grabExcessHorizontalSpace = false;
		gridData24.widthHint = -1;
		gridData24.verticalAlignment = GridData.CENTER;
		GridData gridData18 = new GridData();
		gridData18.horizontalAlignment = GridData.FILL;
		gridData18.grabExcessHorizontalSpace = false;
		gridData18.verticalAlignment = GridData.CENTER;
		GridData gridData20 = new GridData();
		gridData20.horizontalAlignment = GridData.CENTER;
		gridData20.grabExcessHorizontalSpace = true;
		gridData20.grabExcessVerticalSpace = true;
		gridData20.verticalAlignment = GridData.CENTER;
		GridLayout gridLayout8 = new GridLayout();
		gridLayout8.numColumns = 5;
		gridLayout8.marginWidth = 0;
		gridLayout8.marginHeight = 10;
		gridLayout8.horizontalSpacing = 5;
		gridLayout8.verticalSpacing = 0;
		gridLayout8.makeColumnsEqualWidth = true;
		GridData gridData19 = new GridData();
		gridData19.horizontalAlignment = GridData.FILL;
		gridData19.verticalAlignment = GridData.CENTER;
		gridData19.grabExcessVerticalSpace = false;
		gridData19.heightHint = -1;
		gridData19.grabExcessHorizontalSpace = true;
		OrderDetailsComposite = new Composite(OrderHeaderPnlComposite,
				SWT.BORDER);
		OrderDetailsComposite.setLayoutData(gridData19);
		OrderDetailsComposite.setLayout(gridLayout8);
		LblOrderNo = new Label(OrderDetailsComposite, SWT.NONE);
		LblOrderNo.setText("Order #");
		LblOrderNo.setLayoutData(gridData20);
		txtOrderNo = new Text(OrderDetailsComposite, SWT.NONE | SWT.READ_ONLY);
		txtOrderNo.setLayoutData(gridData18);
		label1 = new Label(OrderDetailsComposite, SWT.NONE);
		label1.setText("");
		label1.setLayoutData(gridData8);
		LblOrderStatus = new Label(OrderDetailsComposite, SWT.NONE);
		LblOrderStatus.setText("EnterpriseCode");
		LblOrderStatus.setLayoutData(gridData30);
		txtEnterpriseCode = new Text(OrderDetailsComposite, SWT.NONE
				| SWT.READ_ONLY);
		txtEnterpriseCode.setLayoutData(gridData24);
	}

	private void setBindingForComponents() {
		// Text Bindings
		YRCTextBindingData textBindingData = new YRCTextBindingData();
		textBindingData.setSourceBinding("Order:Order/@OrderNo");
		textBindingData.setName("txtOrderNo");
		txtOrderNo.setData(YRCConstants.YRC_TEXT_BINDING_DEFINATION,
				textBindingData);

		textBindingData = new YRCTextBindingData();
		textBindingData.setSourceBinding("Order:Order/@EnterpriseCode");
		textBindingData.setName("txtEnterpriseCode");
		txtEnterpriseCode.setData(YRCConstants.YRC_TEXT_BINDING_DEFINATION,
				textBindingData);

	}

	/**
	 * This method initializes scrolledComposite
	 * 
	 */
	private void createScrolledComposite() {
		GridData gridData6 = new GridData();
		gridData6.grabExcessHorizontalSpace = true;
		gridData6.verticalAlignment = GridData.FILL;
		gridData6.grabExcessVerticalSpace = true;
		gridData6.horizontalAlignment = GridData.FILL;
		gridData6.grabExcessVerticalSpace = true;

		GridLayout gridLayout1 = new GridLayout(1, true);
		scrolledComposite = new ScrolledComposite(cmpRecipient, SWT.V_SCROLL
				| SWT.BORDER);
		scrolledComposite.setData(YRCConstants.YRC_CONTROL_NAME,
				"cmpOrderLineScrolled");
		scrolledComposite.setContent(cmpAddDynamicPnlForOrderLine);
		scrolledComposite.setLayoutData(gridData6);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setAlwaysShowScrollBars(false);
		createcmpAddDynamicPnlForOrderLine();
		scrolledComposite.setLayout(gridLayout1);
		scrolledComposite.setContent(cmpAddDynamicPnlForOrderLine);
	}

	private void createcmpAddDynamicPnlForOrderLine() {
		GridLayout gridLayout6 = new GridLayout();
		gridLayout6.horizontalSpacing = 0;
		gridLayout6.marginWidth = 0;
		gridLayout6.marginHeight = 0;
		gridLayout6.verticalSpacing = 0;
		GridData gridData2 = new GridData();
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.grabExcessVerticalSpace = true;
		cmpAddDynamicPnlForOrderLine = new Composite(scrolledComposite,
				SWT.NONE);
		cmpAddDynamicPnlForOrderLine.setLayout(gridLayout6);
		cmpAddDynamicPnlForOrderLine.setLayoutData(gridData2);
		addBlankLinesToOrderPanel();
		cmpAddDynamicPnlForOrderLine.setSize(cmpAddDynamicPnlForOrderLine
				.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolledComposite.setMinSize(cmpAddDynamicPnlForOrderLine.computeSize(
				SWT.DEFAULT, SWT.DEFAULT));
	}

	private void addBlankLinesToOrderPanel() {
		YRCPlatformUI
				.trace("#####Inside addBlankLinesToOrderPanel method###############");
		Element eleShipmentModel = myBehavior.getShipmentModel();
		YRCPlatformUI.trace("##########Shipment Model is###############"
				+ XMLUtil.getElementXMLString(eleShipmentModel));
		try {
			nListofShipment = XPathUtil.getNodeList(eleShipmentModel,
					"Shipment");
			if (!YRCPlatformUI.isVoid(nListofShipment))
				iNoOfShipments = ((NodeList) nListofShipment).getLength();
			YRCPlatformUI
					.trace("###### Total Number of Shipments for this Order are###########"
							+ iNoOfShipments);
			YRCPlatformUI
					.setMessage("Gift Card Screen displays all Bulk Gift Card Shipments");
			boolean bHasNonBulkGC = false;
			for (int i = 0; i < iNoOfShipments; i++) {
				bNotEligibleForActivation = false;
				Element eleShipment = (Element) ((NodeList) nListofShipment)
						.item(i);
				YRCPlatformUI.trace("####Shipment details######", eleShipment);
				String strShipmentType = eleShipment
						.getAttribute("ShipmentType");
				YRCPlatformUI.trace("####Shipment Type for Shipment is"
						+ strShipmentType);
				String strStatus = eleShipment.getAttribute("Status")
						.substring(0, 4);
				int iStatus = Integer.parseInt(strStatus);
				if (iStatus < 1400) {
					bNotEligibleForActivation = true;
				}
				if ("GC".equals(strShipmentType)) {
					bHasNonBulkGC = determineNoOfShipmentLinesWithBulkGC(
							eleShipment, bNotEligibleForActivation);
				}
			}
			if (!bHasNonBulkGC) {
				//YRCPlatformUI
						//.showError(AcademyPCAConstants.ATTR_ERROR,
								//"None of the Shipments in this order are Bulk GC Shipment(s)");
				YRCPlatformUI.setMessage("This Order has no Bulk GC Shipment(s)");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean determineNoOfShipmentLinesWithBulkGC(Element eleShipment,
			boolean bNotEligibleForActivation) {
		boolean bHasBulkGC = false;
		try {
			nListOfShipmentLine = XPathUtil.getNodeList(eleShipment,
					"ShipmentLines/ShipmentLine");
			if (!YRCPlatformUI.isVoid(nListOfShipmentLine))
				iNoOfShipmentLines = ((NodeList) nListOfShipmentLine)
						.getLength();
			for (int i = 0; i < iNoOfShipmentLines; i++) {
				bGiftItem = false;
				Element eleShipmentLine = (Element) ((NodeList) nListOfShipmentLine)
						.item(i);
				YRCPlatformUI.trace("####Shipment Line Element details######",
						eleShipmentLine);
				String strQty = eleShipmentLine
						.getAttribute(AcademyPCAConstants.ATTR_QUANTITY);
				int iQuantity = Integer.parseInt(strQty.substring(0, strQty
						.lastIndexOf(".")));
				String strItemID = eleShipmentLine
						.getAttribute(AcademyPCAConstants.ATTR_ITEM_ID);
				callItemListApiToDetermineItem(strItemID);
				if ((bGiftItem) && (iQuantity > 50)) {
					createLineForOrder(eleShipment, bNotEligibleForActivation);
					bHasBulkGC = true;
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return bHasBulkGC;
	}

	/*
	 * This method is called in order to determine if the item is gift card item
	 * for the particular shipment line
	 */
	private boolean callItemListApiToDetermineItem(String strItemID) {
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

	private AcademyGiftCardChildComposite createLineForOrder(
			Element eleShipment, boolean bNotEligibleForActivation) {
		objChildComposite = new AcademyGiftCardChildComposite(
				cmpAddDynamicPnlForOrderLine, SWT.NONE, this, SHIPMENT_PANEL,
				eleShipment, bNotEligibleForActivation);
		objChildComposite.setData(YRCConstants.YRC_CONTROL_NAME,
				"objChildComposite");
		objOrderCollection.add(objChildComposite);
		return objChildComposite;
	}

	public AcademyGiftCardLoadBehavior getBehavior() {
		return myBehavior;
	}

	public void callSetBindingForComponents() {
		setBindingForComponents();
	}

	public void refresh() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				YRCPlatformUI
						.fireAction("com.yantra.yfc.rcp.internal.YRCRefreshScreenAction");
			}
		});
	}
}
