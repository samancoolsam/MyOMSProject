package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

//STL-1694 Begin : This service will update ExtnInvoiceNo for new PRO_FORMA invoice on shipment qty reduction from console (Will-pick scenario).
/**
 * @author <a href="mailto:ksunil.dora@cognizant.com">Sunil Dora</a>, Created on
 *         12/02/2016 as part of STL-1694. 
 *         This service is used to update yfs_order_invoice.extn_invoice_no from yfs_shipment.extn_invoice_no if its blank          
 * 		   
 */

public class AcademyUpdateExtnInvoiceNoService implements YIFCustomApi {
	// Set the logger
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademyUpdateExtnInvoiceNoService.class);

	// Set the properties variable
	private Properties props = new Properties();

	public void setProperties(Properties props)	{
		this.props = props;
	}
	/**
	 * This method receives CHANGE_SHIPMENT.ON_SUCCESS.xml as input. 
	 * @param env
	 *            Environment variable
	 * @param inDoc
	 *            Input document
	 * @return inDoc 
	 *         inDoc is returned as output document to continue the flow
	 * @throws Exception
	 *             Generic exception
	 */
	public Document updateExtnInvoiceNo(YFSEnvironment env, Document inDoc) throws Exception {		
		
		log.verbose("Begin AcademyUpdateExtnInvoiceNoService.updateExtnInvoiceNo() method::");
		String strExtnInvoiceNo = "";
		Element eleShip = null;
		Document docOutgetOrderInvoiceList = null;
		Document docGetOrderInvoiceList = null;
		Element eleGetOrderInvoiceList = null;
		
		if(AcademyConstants.STR_YES.equals(env.getTxnObject(AcademyConstants.STR_UPDATE_EXTN_INVOICE_NO))) {

			eleShip = (Element) inDoc.getDocumentElement();			
			strExtnInvoiceNo =  XPathUtil.getString(eleShip,AcademyConstants.XPATH_EXTN_INVOICE_NO);			
			
			log.verbose("Shipment Level ExtnInvoiceNo ::"+strExtnInvoiceNo); 
			//Call getOrderInvoiceList to get the current PRO-FORMA Invoice
			docGetOrderInvoiceList = XMLUtil.createDocument(AcademyConstants.ELE_ORDER_INVOICE);
			eleGetOrderInvoiceList = docGetOrderInvoiceList.getDocumentElement();
			eleGetOrderInvoiceList.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, eleShip.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
			eleGetOrderInvoiceList.setAttribute(AcademyConstants.ATTR_INVOICE_TYPE, AcademyConstants.STR_PRO_FORMA_INVOICE);
	
			log.verbose("AcademyUpdateExtnInvoiceNoService.updateExtnInvoiceNo getOrderInvoiceListDoc():: " 
					+ XMLUtil.getXMLString(docGetOrderInvoiceList));
			
			env.setApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_LIST, AcademyConstants.STR_TEMPLATEFILE_GET_ORDER_INVOICE_LIST);
			docOutgetOrderInvoiceList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_INVOICE_LIST, docGetOrderInvoiceList);
			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_LIST);
			
			log.verbose("AcademyUpdateExtnInvoiceNoService.updateExtnInvoiceNo getOrderInvoiceList API output: " 
					+ XMLUtil.getXMLString(docOutgetOrderInvoiceList));			
			
			updateOrderInvoiceWithExtnInvoiceNo(env,docOutgetOrderInvoiceList,strExtnInvoiceNo);			
		}
		
		log.verbose("End AcademyUpdateExtnInvoiceNoService.updateExtnInvoiceNo() method::");
		
		return inDoc;
	}
	
	/**
	 * This method will update ExtnInvoiceNo on new PRO_FORMA invoice if its blank
	 * @param env
	 *            Environment variable
	 * @param getOrderInvoiceListOPDoc
	 *            Input document
	 * @return void
	 *          
	 * @throws Exception
	 *             Generic exception
	 */
	private void updateOrderInvoiceWithExtnInvoiceNo(YFSEnvironment env,Document getOrderInvoiceListOPDoc,String extnInvoiceNo) throws Exception {
		log.verbose("Begin AcademyUpdateExtnInvoiceNoService.updateOrderInvoiceWithExtnInvoiceNo() method::");
		
		String strExtnInvoiceNo ="";
		Document docInChangeOrderInvoice = null;
		Element eleChangeOrderInvoiceIPDocRoot = null;
		Element eleChangeOrderInvoiceExtn = null;
		
		Element orderInvoiceElm = (Element) getOrderInvoiceListOPDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_ORDER_INVOICE).item(0);
		Element orderInvoiceExtnElm = (Element) orderInvoiceElm.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
		strExtnInvoiceNo = orderInvoiceExtnElm.getAttribute(AcademyConstants.ATTR_EXTN_INVOICE_NO);
		
		//Call changeOrderInvoice to update old ExtnInvoiceNo to the new PRO-FORMA Invoice
		if(YFCObject.isVoid(strExtnInvoiceNo)){
			docInChangeOrderInvoice = XMLUtil.createDocument(AcademyConstants.ELE_ORDER_INVOICE);			
			eleChangeOrderInvoiceIPDocRoot = docInChangeOrderInvoice.getDocumentElement();			
			eleChangeOrderInvoiceIPDocRoot.setAttribute(AcademyConstants.ATTR_ORDER_INVOICE_KEY, 
					orderInvoiceElm.getAttribute(AcademyConstants.ATTR_ORDER_INVOICE_KEY));			
			
			eleChangeOrderInvoiceExtn = docInChangeOrderInvoice.createElement(AcademyConstants.ELE_EXTN);
			eleChangeOrderInvoiceExtn.setAttribute(AcademyConstants.ATTR_EXTN_INVOICE_NO, extnInvoiceNo);
			eleChangeOrderInvoiceIPDocRoot.appendChild(eleChangeOrderInvoiceExtn);
			log.verbose("AcademyUpdateExtnInvoiceNoService.updateOrderInvoiceWithExtnInvoiceNo() changeOrderInvoice Input :" 
					+XMLUtil.getXMLString(docInChangeOrderInvoice));
			
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER_INVOICE, docInChangeOrderInvoice);
		}
		log.verbose("End AcademyUpdateExtnInvoiceNoService.updateOrderInvoiceWithExtnInvoiceNo() method::");
		
	}
}
//STL-1694 End