package com.academy.ecommerce.sterling.orderSummary.extn;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.XMLUtil;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.IYRCPanelHolder;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCTextBindingData;

public class AcademyGCLinesComposite extends Composite implements IYRCComposite {
	public static String FORM_ID = "com.academy.ecommerce.sterling.orderSummary.extn.AcademyGCLinesComposite"; // @jve:decl-index=0:

	private Composite pnlRoot = null;

	private AcademyGCLinesCompositeBehavior myBehavior;

	public Text text1 = null;

	public Text text2 = null;

	private Text text3 = null;

	private Label label = null;

	private int i;

	public Text label3 = null;

	private int k = 0;

	public AcademyGCLinesComposite(Composite arg0, int arg1, Element inElm,
			Element eleShipmentLine) {
		super(arg0, arg1);
		initialize();
		YRCPlatformUI.trace("#######Inside Academy GC Lines Composite Class"
				+ XMLUtil.getElementXMLString(eleShipmentLine));
		setBindingForGCComponents();
		myBehavior = new AcademyGCLinesCompositeBehavior(this, FORM_ID, inElm,
				eleShipmentLine);
	}

	private void initialize() {
		this.setSize(new Point(921, 36));
		createComposite();
	}

	/**
	 * This method initializes composi text2.setLayoutData(gridData3);
	 * text1.setLayoutData(gridData2); text.setLayoutData(gridData1); te
	 * 
	 */
	private void createComposite() {
		GridData gridData11 = new GridData();
		gridData11.grabExcessHorizontalSpace = false;
		gridData11.verticalAlignment = GridData.CENTER;
		gridData11.widthHint = -1;
		gridData11.horizontalIndent = 0;
		gridData11.horizontalAlignment = GridData.FILL;
		GridData gridData26 = new GridData();
		gridData26.horizontalAlignment = GridData.END;
		gridData26.verticalAlignment = GridData.CENTER;
		gridData26.grabExcessHorizontalSpace = true;
		GridData gridData25 = new GridData();
		gridData25.horizontalAlignment = GridData.FILL;
		gridData25.widthHint = -1;
		gridData25.horizontalIndent = 0;
		gridData25.grabExcessVerticalSpace = false;
		gridData25.grabExcessHorizontalSpace = false;
		gridData25.verticalAlignment = GridData.CENTER;
		GridData gridData24 = new GridData();
		gridData24.horizontalAlignment = GridData.CENTER;
		gridData24.grabExcessHorizontalSpace = true;
		gridData24.widthHint = 80;
		gridData24.horizontalIndent = 0;
		gridData24.verticalAlignment = GridData.CENTER;
		GridLayout gridLayout7 = new GridLayout();
		gridLayout7.numColumns = 3;
		gridLayout7.marginWidth = 5;
		gridLayout7.horizontalSpacing = 5;
		gridLayout7.verticalSpacing = 0;
		gridLayout7.marginHeight = 8;
		gridLayout7.makeColumnsEqualWidth = true;
		GridData gridData16 = new GridData();
		gridData16.horizontalAlignment = GridData.CENTER;
		gridData16.heightHint = 15;
		gridData16.widthHint = 50;
		gridData16.verticalAlignment = GridData.CENTER;
		GridData gridData15 = new GridData();
		gridData15.horizontalAlignment = GridData.FILL;
		gridData15.verticalAlignment = GridData.CENTER;
		GridData gridData14 = new GridData();
		gridData14.horizontalAlignment = GridData.FILL;
		gridData14.verticalAlignment = GridData.CENTER;
		GridData gridData13 = new GridData();
		gridData13.horizontalAlignment = GridData.CENTER;
		gridData13.grabExcessHorizontalSpace = true;
		gridData13.heightHint = 15;
		gridData13.widthHint = 30;
		gridData13.verticalAlignment = GridData.CENTER;
		GridData gridData8 = new GridData();
		gridData8.horizontalAlignment = GridData.FILL;
		gridData8.grabExcessHorizontalSpace = true;
		gridData8.grabExcessVerticalSpace = false;
		gridData8.verticalAlignment = GridData.FILL;
		gridData8.heightHint = 30;
		GridLayout gridLayout4 = new GridLayout();
		gridLayout4.numColumns = 4;
		gridLayout4.makeColumnsEqualWidth = true;
		pnlRoot = new Composite(this, SWT.NONE);
		pnlRoot.setLayout(gridLayout7);
		pnlRoot.setBounds(new Rectangle(0, -1, 921, 36));
		text1 = new Text(getRootPanel(), SWT.BORDER | SWT.READ_ONLY);
		text1.setLayoutData(gridData24);
		text2 = new Text(getRootPanel(), SWT.BORDER | SWT.READ_ONLY);
		text2.setLayoutData(gridData25);
		text3 = new Text(getRootPanel(), SWT.BORDER | SWT.READ_ONLY);
		text3.setLayoutData(gridData11);
		text3.setLayoutData(gridData26);
		/*
		 * text1.addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
		 * public void focusLost(org.eclipse.swt.events.FocusEvent e) { String
		 * strStartGCNo = text1.getText(); String strEndGCNo = text2.getText();
		 * int iStartValue = 0; int iEndValue = 0;
		 * if(!YRCPlatformUI.isVoid(strStartGCNo)){ try{ iStartValue =
		 * Integer.parseInt(strStartGCNo); }catch (Exception ex){
		 * text1.setFocus(); text1.setText(""); } }
		 * if(!YRCPlatformUI.isVoid(strEndGCNo)){ try{ iEndValue =
		 * Integer.parseInt(strEndGCNo); }catch (Exception ex){
		 * text2.setFocus(); text2.setText(""); } } if(iStartValue > iEndValue){
		 * label3.setText("1"); }else if (iStartValue == 0){ label3.setText("");
		 * 
		 * }else{ label3.setText(String.valueOf(iEndValue - iStartValue +1)); } }
		 * 
		 * });
		 */

		/*
		 * text2.addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
		 * public void focusLost(org.eclipse.swt.events.FocusEvent e) { String
		 * strStartGCNo = text1.getText(); String strEndGCNo = text2.getText();
		 * int iStartValue = 0; int iEndValue = 0;
		 * if(!YRCPlatformUI.isVoid(strStartGCNo)){ try{ iStartValue =
		 * Integer.parseInt(strStartGCNo); }catch (Exception ex){
		 * text1.setFocus(); text1.setText(""); } }
		 * if(!YRCPlatformUI.isVoid(strEndGCNo)){ try{ iEndValue =
		 * Integer.parseInt(strEndGCNo); }catch (Exception ex){
		 * text2.setFocus(); text2.setText(""); } } if(iStartValue > iEndValue){
		 * label3.setText("1"); }else if (iStartValue == 0){ label3.setText("");
		 * }else{ label3.setText(String.valueOf(iEndValue - iStartValue +1)); } }
		 * });
		 */
	}

	private String computeIndex(Composite composite) {
		Object obj[] = composite.getParent().getChildren();
		if (!YRCPlatformUI.isVoid(obj)) {
			return String.valueOf(obj.length);
		}
		return "";
	}

	public void setBindingForGCComponents() {
		YRCTextBindingData textBindingData = new YRCTextBindingData();
		textBindingData = new YRCTextBindingData();
		textBindingData
				.setTargetBinding("ShipmentLine:ShipmentLine/@PrimeLineNo");
		textBindingData.setName("Row#");

		textBindingData = new YRCTextBindingData();
		textBindingData
				.setSourceBinding("ShipmentLine:ShipmentLine/@PrimeLineNo");
		textBindingData.setName("text1");
		text1
				.setData(YRCConstants.YRC_TEXT_BINDING_DEFINATION,
						textBindingData);

		textBindingData = new YRCTextBindingData();
		textBindingData.setSourceBinding("ShipmentLine:ShipmentLine/@ItemID");
		textBindingData.setName("text2");
		text2
				.setData(YRCConstants.YRC_TEXT_BINDING_DEFINATION,
						textBindingData);

		textBindingData = new YRCTextBindingData();
		textBindingData.setSourceBinding("ShipmentLine:ShipmentLine/@Quantity");
		textBindingData.setName("text3");
		text3
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

	public AcademyGCLinesCompositeBehavior getBehavior() {
		return myBehavior;

	}

} // @jve:decl-index=0:visual-constraint="13,74"
