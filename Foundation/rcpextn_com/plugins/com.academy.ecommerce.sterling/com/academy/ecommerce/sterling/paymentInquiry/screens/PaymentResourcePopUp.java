package com.academy.ecommerce.sterling.paymentInquiry.screens;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.yantra.yfc.rcp.IYRCBrowserHandler;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.IYRCPanelHolder;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCWebAppStatus;
import com.yantra.yfc.rcp.YRCWebAppUtils;
import com.yantra.yfc.rcp.YRCXmlUtils;
import com.yantra.yfc.rcp.internal.YRCWebAppManager;
import com.yantra.yfc.rcp.YRCWebAppLoginInfo;

public class PaymentResourcePopUp extends Composite implements IYRCComposite {
	
private Composite pnlRoot = null;
private PaymentResourcePopUpBehavior myBehavior;
public static final String FORM_ID = AcademyPCAConstants.FORM_ID_PAYMENTINQUIRY;
boolean canClose = false;
private Browser browser;
private String sOrderHeaderKey;


public  PaymentResourcePopUp(Composite parent, int style,String url, boolean lookupMode) 
{
	
	super(parent, style);
	initialize();
    myBehavior = new PaymentResourcePopUpBehavior(this, FORM_ID);
        
    Element localElement = YRCWebAppManager.getWebAppConfigElement("AcadWebAppHandler"); 
    String str4 = YRCXmlUtils.getAttribute(localElement, "Protocol"); 
    String str5 = YRCXmlUtils.getAttribute(localElement, "BaseUrl"); 
    String str6 = YRCXmlUtils.getAttribute(localElement, "PortNumber"); 
    String str7 = YRCXmlUtils.getAttribute(localElement, "WebAppContext"); 
    String str8 = str4 + "://" + str5 + ":" + str6 + str7;    
    //this.browser.setUrl(str8 + url);
	//OMS 9.5 Upgrade -Added as part of Sterling upgrade to handle loadbalancer logic so that this works for both 9.2.1 and 9.5
	this.browser.setText(getBrowserText(str8, url));
    canClose = true;
}

	private void initialize() {
	FillLayout fillLayout = new FillLayout();
	createRootPanel();
	this.setLayout(fillLayout);
	setSize(new org.eclipse.swt.graphics.Point(298,227));
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
    pnlRoot.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE, "TaskComposite");

}


public IYRCPanelHolder getPanelHolder() {
    return null;
}

public String getHelpId() {
	return "ycdRCP0905";
}

/**
 * This method is introduced as part of Sterling Upgrade project which will help the RCP COM payment transaction POP up to work with both 9.2.1 and 9.5
 *
 */

private String getBrowserText(String str8, String url)
{
	url = str8 + url;
	url = url.replaceAll(":/smcfs", "/smcfs");

                                YRCWebAppLoginInfo info = YRCWebAppManager.getWebAppLoginInfo("AcadWebAppHandler");
								try
								{
									url = YRCWebAppUtils.appendCsrfParamToUrl(url, info) ;
								}
								catch(Exception e)
								{
								}
								//the below code will display the SMCFS screen as POP up instead of entire Sterling console with menus
								//url=url+ "&Popup=Y";

 

                                String[] urls = url.split("\\?");

                                String[] data = urls[1].split("&");

                                String text = "<HTML><HEAD><script>function submitForm(){document.forms['loginForm'].submit();}</script></HEAD><BODY onLoad='submitForm()'><form id='loginForm' method='post' action='" + urls[0] + "'>" ;

 

                                for(int i = 0; i < data.length; i++)

                                {

                                                String[] hiddenData = data[i].split("=");

 

                                                if(hiddenData.length == 2)

                                                {

                                                                try

                                                                {

                                                                                text = text + "<input type='hidden' name='" + hiddenData[0] + "' value='" +  java.net.URLDecoder.decode(hiddenData[1], "UTF-8") + "'/>" ;

                                                                }

                                                                catch(Exception e){}

                                                }

                                }

 

                                text = text + "</form></BODY></HTML>";

                                

                                return text;

                }


/**
 * This method initializes grpMain	
 *
 */



public PaymentResourcePopUpBehavior getMyBehavior() {
	return myBehavior;
}

/**
 * This method initializes compositeParent	
 *
 */
public void showBrowserContents(GridLayout gridLayout)
{
	GridData gridData4 = new org.eclipse.swt.layout.GridData();
	gridData4.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
	gridData4.grabExcessHorizontalSpace = true;
	gridData4.grabExcessVerticalSpace = true;
	gridData4.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
	gridData4.heightHint = 80;
	gridData4.widthHint = 80;
	browser = new Browser(getRootPanel(), SWT.NONE);
	browser.setLayoutData(gridData4);
	  YRCWebAppUtils.setupBrowser("AcadWebAppHandler", this.browser, new IYRCBrowserHandler() 
	    { 
	     
		@Override
		public void execute(YRCWebAppStatus paramAnonymousYRCWebAppStatus) {
			// TODO Auto-generated method stub
			
		} 
	    });
}
}
