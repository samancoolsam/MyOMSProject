/**
 * 
 */
package com.academy.ecommerce.sterling.quickAccess.extn;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyActionIdConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.IYRCPanelHolder;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCTextBindingData;
import com.yantra.yfc.rcp.internal.YRCApiCaller;

/**
 * @author sahmed
 * 
 */
public class AcademyCognos extends Composite implements IYRCComposite {

	public static final String FORM_ID = "com.academy.ecommerce.sterling.quickAccess.extn.AcademyCognos"; // @jve:decl-index=0:

	private Element inElm = null;

	private AcademyCognosBehavior myBehavior = null;

	private Composite pnlRoot = null;
	private Browser browser =null;

	public AcademyCognos(Composite parent, int style, Element inElm) {
		super(parent, style);
		this.inElm = inElm;
		initialize();
		//setBindingForComponents();
		myBehavior = new AcademyCognosBehavior(this, FORM_ID, inElm);
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

	public AcademyCognosBehavior getBehavior() {
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
		GridData gridData = new org.eclipse.swt.layout.GridData();
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.heightHint = 80;
		gridData.widthHint = 80;
		browser = new Browser(getRootPanel(), SWT.NONE);
		browser.setLayoutData(gridData);
		openUrl();
	}

	public void openUrl() {
		try {
			Document doc = XMLUtil.createDocument("GetProperty");
			Element ele = doc.getDocumentElement();
			ele.setAttribute("PropertyName", "analytics.reportnet.url");
			Document outGetProperty=invokeSyncAPI(doc);
			YRCPlatformUI.trace("****Output of getProperty API: *********" + XMLUtil.getXMLString(outGetProperty));
			String strURL= outGetProperty.getDocumentElement().getAttribute("PropertyValue");
			browser.setUrl(strURL);
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		
	}

	private Document invokeSyncAPI(Document doc) {
		YRCApiContext context = new YRCApiContext();
		YRCApiCaller syncapiCaller = new YRCApiCaller(context, true);
		String strAPIName="getProperty";
		context.setApiName(strAPIName);
		context.setFormId("com.academy.ecommerce.sterling.quickAccess.extn.AcademyCognos");
		context.setInputXml(doc);
		syncapiCaller.invokeApi();
		Document outputXML = context.getOutputXml();
		return outputXML;
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
