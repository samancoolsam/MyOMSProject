package com.academy.ecommerce.sterling.orderSummary.screens;

import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

//import com.yantra.pca.ycd.rcp.tasks.alert.common.IYCDAlertConstants;
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCXmlUtils;
import com.yantra.yfc.rcp.YRCDesktopUI;


public class AcademyTaxIdPopUpBehavior extends YRCBehavior 
{

	AcademyTaxIdPopUp view = null;
	String taxExemptID=null;
	String OrderHeaderKey=null;
    Composite parentComposite = null;
	public AcademyTaxIdPopUpBehavior(Composite ownerComposite, String formId) {
        super(ownerComposite, formId);
        
        this.view = (AcademyTaxIdPopUp) ownerComposite;
        initializeModel();

    }
	/* The code below is to form a model for the tax pop up. */
	public void initializeModel()
	{
		Document docExtnTaxExempt=YRCXmlUtils.createDocument(AcademyPCAConstants.ATTR_TAX_DETAILS);
		docExtnTaxExempt.getDocumentElement().setAttribute(AcademyPCAConstants.ATTR_TAX_EXEMPT_ID,this.taxExemptID);
		docExtnTaxExempt.getDocumentElement().setAttribute(AcademyPCAConstants.ATTR_ORDER_HEADER_KEY,this.OrderHeaderKey);
		setModel(AcademyPCAConstants.ATTR_TAX_EXEMPT_DETAILS,docExtnTaxExempt.getDocumentElement());
		
	}
	/* initialize the setTaxExemptID method*/
	public void setTaxExemptID(String taxExemptId, String OrderHeaderKey)
	{
		this.taxExemptID=taxExemptId;
		this.OrderHeaderKey=OrderHeaderKey;
	}
	
	
	public void  close()
	{
		view.getShell().close();
	}	
	
	
	public Document doc(){
		return getTargetModel(AcademyPCAConstants.ATTR_TAX_EXEMPT_DETAILS).getOwnerDocument();
	}
	
	public void callApi(YRCApiContext context) {
		super.callApi(context);
	}
	

	public void handleApplyButtonSelected() {

	}
}
