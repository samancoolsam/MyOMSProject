/**
 * 
 */
package com.academy.ecommerce.sterling.quickAccess.extn;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.academy.ecommerce.sterling.util.XMLUtil;
//import com.yantra.pca.ycd.rcp.exposed.YCDExtensionUtils;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;

/**
 * @author agauba
 *
 */
public class AcademyQuickAccessExtnBehivor extends YRCWizardExtensionBehavior {

	/**
	 * 
	 */
	public boolean firesearch=false;
	public boolean fireordersearch=false;
	public boolean firecustomersearch=false;
	public AcademyQuickAccessExtnBehivor() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.yantra.yfc.rcp.YRCWizardExtensionBehavior#createPage(java.lang.String)
	 */
	@Override
	public IYRCComposite createPage(String arg0) {
		// TODO Auto-generated method stub
	
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.yantra.yfc.rcp.YRCWizardExtensionBehavior#pageBeingDisposed(java.lang.String)
	 */
	@Override
	public void pageBeingDisposed(String arg0) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void initPage(String arg0) {
		/**
		 * Setting the value of Order No or Phone No from the 
		 * ClipBoard.
		 */	
		String st=copyfromClipBoard();
		setSearchCriteria(st);
		
		//Document order=createOrder(st);
			
		//YCDExtensionUtils.launchTaskInEditor("YCD_TASK_VIEW_ORDER_SUMMARY", order.getDocumentElement());
		
		super.initPage(arg0);
	}

	
	@Override
	public YRCValidationResponse validateButtonClick(String fieldname) {
		if(fieldname.equals("extn_CustomerSearchBtn")){
			YRCPlatformUI.fireAction("com.academy.ecommerce.sterling.quickAccess.extn.actions.AcademyCustomerSearchAction");
		}
		return super.validateButtonClick(fieldname);
	}

	private Document createOrder(String st) {
		Document order=null;
		try {
			order = XMLUtil.createDocument("Order");
			Element inputElem=order.getDocumentElement();
			inputElem.setAttribute("BillToID", "");
			inputElem.setAttribute("BuyerOrganizationCode", "");
			inputElem.setAttribute("CustomerFirstName", "");
			inputElem.setAttribute("CustomerLastName", "");
			inputElem.setAttribute("OrderHeaderKey", "");
			inputElem.setAttribute("SellerOrganizationCode", "");
			inputElem.setAttribute("isHistory", "");
			inputElem.setAttribute("OrderNo",st);
			inputElem.setAttribute("DocumentType", "0001");
			inputElem.setAttribute("EnterpriseCode", "Academy_Direct");
			Element priceInfo=order.createElement("PriceInfo");
			priceInfo.setAttribute("Currency", "USD");
			inputElem.appendChild(priceInfo);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return order;
	}

	@Override
	public void postSetModel(String arg0) {
		if(arg0.equals("customer_input")){
			Display.getDefault().asyncExec(new Runnable(){
				  public void run() {
					  					if(fireordersearch){
				                         YRCPlatformUI.fireAction("com.yantra.pca.ycd.rcp.tasks.quickAccess.actions.YCDQuickAccessFindOrderAction");
					  					}else{
					  						if(firecustomersearch){
					  							YRCPlatformUI.fireAction("com.yantra.pca.ycd.rcp.tasks.quickAccess.actions.YCDQuickAccessFindCustomerAction");
					  						}else{
					  							YRCPlatformUI.setMessage("Customer Order number and phone number were not present on the clipboard");
					  						}
					  					}
				                   }

				 

				             });


		}
		super.postSetModel(arg0);
	}
	
	/**
	 * This method copies the latest value from the Clipborad 
	 * and sets the value into  the txtOrderNo which is the field value in the 
	 * for Order Search.
	 * 
	 * @return
	 */
	
	private String copyfromClipBoard() {

        Display display = YRCPlatformUI.getShell().getDisplay();
        final Clipboard cb = new Clipboard(display);
        TextTransfer transfer = TextTransfer.getInstance();
        String data = (String)cb.getContents(transfer);
        
        return data;

}
	
	private void setSearchCriteria(String clipboard){
		//For Order No.Check If its order No.
		/**
		 * Assumption is that clipboard will have a
		 * an XML which will have 2 attributes as below.
		 * <CustomerCallData OrderNo=”….” CustomerPhoneNo=”….” CallContext=”Order/Customer/Item/Availability.”/>
		 * Either one of these will come at a time
		 * and the parameter which is coming can be 
		 * set for searching .
		 */
		
		//setFieldValue("txtOrderNo", clipboard);
		/**
		 * Following code needs to be uncommented and modified when
		 * the final xml for clipboard is published .
		 */
	  try {
		  if(!YRCPlatformUI.isVoid(clipboard)){
		  if(clipboard.substring(0, 1).equals("<")){
		Document clipDoc= XMLUtil.getDocument(clipboard);
		if(!(YRCPlatformUI.isVoid(clipDoc))){
			if(clipDoc.getDocumentElement().getNodeName().equals("CustomerCallData")){
		String ordNo=clipDoc.getDocumentElement().getAttribute("OrderNo");
		String phoneNo=clipDoc.getDocumentElement().getAttribute("CustomerPhoneNo");
		/*if(YRCPlatformUI.isVoid(ordNo))
			setFieldValue("txtPhoneNo",phoneNo);
		if(YRCPlatformUI.isVoid(phoneNo)){
			setFieldValue("txtOrderNo", ordNo);
		}*/
		if(!YRCPlatformUI.isVoid(ordNo)){
			setFieldValue("txtOrderNo", ordNo);
			fireordersearch=true;
		}else{
			fireordersearch=false;
		}
			if(!YRCPlatformUI.isVoid(phoneNo)){
				setFieldValue("txtDayPhoneNo",phoneNo);
				firecustomersearch=true;
			}else{
				firecustomersearch=false;
			}
		
		}
		}
		  }
		  }
	} catch (ParserConfigurationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (SAXException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
		
		
	
		
		
	}

	public void fireCustomerSearch() {
		// TODO Auto-generated method stub
		
	}

	public void copyFromClipBoardAndFireSearch() {
		String st=copyfromClipBoard();
		setSearchCriteria(st);
		if(fireordersearch){
            YRCPlatformUI.fireAction("com.yantra.pca.ycd.rcp.tasks.quickAccess.actions.YCDQuickAccessFindOrderAction");
		}else{
			if(firecustomersearch){
					YRCPlatformUI.fireAction("com.yantra.pca.ycd.rcp.tasks.quickAccess.actions.YCDQuickAccessFindCustomerAction");
				}else{
					YRCPlatformUI.setMessage("Customer Order number and phone number were not present on the clipboard");
				}
		}
	}
	
	


	
	
	
  
   
	 
	
	
}
//TODO Validation required for a Button control: extn_CustomerSearchBtn