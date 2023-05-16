package com.academy.ecommerce.sterling.dsv.shipment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.dsv.util.AcademyDsvUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class will be invoked on 
 * 'CREATE_SHMNT_INVOICE.0005.ON_INVOICE_CREATION’ event.
 * 
 * The custom logic will invoke getCompleteOrderDetails API with template
 * to get all the required attribute values to populate the Email component.
 * 
 * @author Manjusha V (215812)
 *
 */

public class AcademySendEmailForPOShipment implements YIFCustomApi {

	private static Logger log = Logger.getLogger(AcademySendEmailForPOShipment.class.getName());

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * This method will be invoked on'CREATE_SHMNT_INVOICE.0005.ON_INVOICE_CREATION’ 
	 * event. The custom logic invokes getCompleteOrderDetails API and also 
	 * fetches the required attributes from the
	 * event input xml and updates the API output with the fetched values.
	 * and sends to the Email component.
	 * 
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */	
	public Document sendEmailForPOShipment(YFSEnvironment env, Document inDoc)
			throws Exception {
		log.verbose("sendEmailOnPOShipConfirmation.sendEmailForPOShipment() starts");
		log.verbose("inDoc : -->" + XMLUtil.getXMLString(inDoc)+ "<----");

		Document getCompleteOrderDetailsOutDoc = null;
		Document getOrderListInDoc = null;
		Document getOrderListOutDoc = null;
		
		String strEstmtdDlvryDate="";
		String strOrdInvShpmntKey="";
		String strDateInvoiced="";
		String strShipmentType="";
		String strPOOrderNo="";
		String strSOOrderNo="";
		String strInvoiceNo="";	
		String strInvoiceType = "";
		String strShpShpmntKey="";
		String strPOOHK="";
		
		String strShipmentLineOLK= "";
		String strOrderLineOLK="";
		String strShipmentLineQty = "";
		
		//Start WN-697 : Sterling to consume special characters and include them in customer-facing emails, but to remove them before settlement.		
		Element elePersonInfoBillTo = null;
		Element	elePersonInfoShipTo = null;
		//End WN-697 : Sterling to consume special characters and include them in customer-facing emails, but to remove them before settlement.
		
		int iLeadDays = 0;
		
		
		YFCDocument inYFCDoc = YFCDocument.getDocumentFor(inDoc);
		
		/**
		 * Fetch the ShipmentKey, OrderHeaderKey, DateInvoiced & ShipmentType from the input document.
		 */
		strOrdInvShpmntKey = XMLUtil.getString(inDoc, "/Shipments/Shipment/@ShipmentKey");
		strShipmentType = XMLUtil.getString(inDoc, "/Shipments/Shipment/@ShipmentType");
		strPOOrderNo = XMLUtil.getString(inDoc, "/Shipments/Shipment/@OrderNo");
		strPOOHK = XMLUtil.getString(inDoc, "/Shipments/Shipment/@OrderHeaderKey");
		
		
		/**
		 * read only if the invoice type is SHIPMENT
		 */
		YFCNodeList<YFCElement>  orderInvoiceList = inYFCDoc.getElementsByTagName("OrderInvoice");
				
		if (orderInvoiceList.getLength() >0){
			log.verbose("<OrderInvoice> present");
			
			for (YFCElement orderInvoiceEle : orderInvoiceList){
				strInvoiceType = orderInvoiceEle.getAttribute("InvoiceType");
				
				if (AcademyConstants.DSV_ORDER_INVOICE_TYPE.equals(strInvoiceType)){
					log.verbose("invoice type is SHIPMENT.. fetch the attrs.");
					strInvoiceNo = orderInvoiceEle.getAttribute("InvoiceNo");
					strDateInvoiced = orderInvoiceEle.getAttribute("DateInvoiced");
					strSOOrderNo = orderInvoiceEle.getAttribute("OrderNo");
				}
				
				log.verbose("strInvoiceNo: "+strInvoiceNo);
				log.verbose("strDateInvoiced: "+strDateInvoiced);
				log.verbose("strSOOrderNo: "+strSOOrderNo);
				log.verbose("strPOOHK: "+strPOOHK);
				log.verbose("strOrdInvShpmntKey: "+strOrdInvShpmntKey);
				log.verbose("strShipmentType: "+strShipmentType);
				log.verbose("strPOOrderNo: "+strPOOrderNo);
				
			}
		}
				
		
		
		 /**
		  * invoke AcademyDsvUtil.getCommonCodeValueForCodeType (env,strShipmentType) method to get the lead days which is 
		  * configured in common codes. 
		  * 
		  * The lead time =15, for WG shipment and its 5 for NWG shipment.
		  */
		iLeadDays = AcademyDsvUtil.getCommonCodeValueForCodeType(env, strShipmentType);
		log.verbose("iLeadDays: "+iLeadDays);
		
		/**
		 * invoke AcademyDsvUtil.getParsedInvoiedDateAfterUpdation(strDateInvoiced, iLeadDays) to get the estimated delivery date
		 * which is DateInvoiced + Lead Time.
		 */
		if(null!=strDateInvoiced && strDateInvoiced.length() >0){
		strEstmtdDlvryDate = AcademyDsvUtil.getParsedInvoiedDateAfterUpdation(strDateInvoiced, iLeadDays);
		}else{
			log.verbose("DateInvoiced is coming as Empty. strDateInvoiced = "+ strDateInvoiced+" .");
		}
		log.verbose("Estimated Delivery Date is " + strEstmtdDlvryDate);

		
		/**
		 * invoke getOrderList with strPOOrderNo to get the OHK of the PO
		 * 
		 * and invoke the getCompleteOrderDetails API with the PO OHK
		 */
				
		/*getOrderListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		getOrderListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_NO, strPOOrderNo);
		getOrderListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE, "0005");
		getOrderListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.DSV_ENTERPRISE_CODE);
        
		Document outputTemplate = YFCDocument.getDocumentFor("<OrderList><Order OrderHeaderKey='' /></OrderList>").getDocument();
		
		log.verbose("Input document to getOrderList API -->"+ XMLUtil.getElementXMLString(getOrderListInDoc.getDocumentElement()));
		
		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, outputTemplate);
					
		getOrderListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, getOrderListInDoc);
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);

		log.verbose("getOrderList API Output document: --> "+ XMLUtil.getXMLString(getOrderListOutDoc)+ "<--");


		YFCDocument getOrderListOutYFCDoc = YFCDocument.getDocumentFor(getOrderListOutDoc);
		
		*//**
		 * read the OHK
		 **/
	/*	if (null!=getOrderListOutYFCDoc){
			log.verbose("OutDoc is not null");
			YFCNodeList<YFCElement>  orderList = getOrderListOutYFCDoc.getElementsByTagName("Order");
			
			if (orderList.getLength() >0){
				log.verbose("<Order> present");
				
				strPOOHK = XMLUtil.getString(getOrderListOutYFCDoc.getDocument(), "/OrderList/Order/@OrderHeaderKey");
				log.verbose("PO OHK is = " +strPOOHK);
				
			}*/
			
		/*}*/

		/**
		 * ends
		 */
		
		/**
		 * invoke getCompleteOrderDetailsToPopulateEmail method to prepare and invoke getCompleteOrderDetails API
		 * to get the attribute values required in the email component. OHK is the PO's OHK
		 */
		getCompleteOrderDetailsOutDoc = getCompleteOrderDetailsToPopulateEmail(env, strPOOHK);

		/**
		 * format the estimated delivery date in Month day, year (for eg: June 29, 2012) format.
		 * 
		 */
		SimpleDateFormat invoicedDateInFormatter = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat invoicedDateOutFormatter = new SimpleDateFormat("MMMM dd, yyyy");
		
		Date dEstmtdDlvryDate = invoicedDateInFormatter.parse(strEstmtdDlvryDate);
		String strFrmtdEstmtdDlvryDate =  invoicedDateOutFormatter.format(dEstmtdDlvryDate);
		
		log.verbose("The Estimated delivery date in MMMM dd, yyyy format: "+strFrmtdEstmtdDlvryDate);
		
		/**
		 * update the getCompleteOrderDetails API output by adding 
		 * 1. CurrentShipmentKey & SO OrderNo @ Order level and
		 * 2. InvoiceNo & EstimatedDeliveryDate @ Shipment level. 
		 */
		
		getCompleteOrderDetailsOutDoc.getDocumentElement().setAttribute("CurrentShipmentKey", strOrdInvShpmntKey);
		getCompleteOrderDetailsOutDoc.getDocumentElement().setAttribute("SalesOrderNo", strSOOrderNo);
		/**
		 * set the invoice no & estimated delivery date at <Shipment> level.
		 */
		YFCDocument getCompleteOrderDetailsOutYFCDoc =  YFCDocument.getDocumentFor(getCompleteOrderDetailsOutDoc);
		YFCNodeList<YFCElement>  shipmentList = getCompleteOrderDetailsOutYFCDoc.getElementsByTagName("Shipment");
					
		YFCNodeList<YFCElement>  orderLineList = getCompleteOrderDetailsOutYFCDoc.getElementsByTagName("OrderLine");
		/**
		 * if <Shipment> tags are present in the output of get complete order detail's api output.
		 */
		if (shipmentList.getLength()>0){
			
			log.verbose("<Shipment> tag (s) present in the xml");
		
			/**
			 * for all shipment tags, read the ShipmentKey and 
			 * if the <Shipment> tag's shipment key is equal to the shipment key present at the OrderInvoice level, then stamp the
			 * invoice number and expected delivery date at that <Shipment> node.
			 */
			
			for (YFCElement shipmentEle : shipmentList){
				strShpShpmntKey = shipmentEle.getAttribute("ShipmentKey");
				
				log.verbose("OrderInvoice Level Shipment Key: "+strOrdInvShpmntKey);
				log.verbose("Shipment Level Shipment Key: "+ strShpShpmntKey);
				
				if (strOrdInvShpmntKey.equals(strShpShpmntKey))
				{
					log.verbose(" OrderInvoice Level Shipment Key = Shipment Level Shipment Key ");
					
					shipmentEle.setAttribute("InvoiceNo", strInvoiceNo);
					shipmentEle.setAttribute("EstimatedDeliveryDate", strFrmtdEstmtdDlvryDate);
										
					YFCNodeList<YFCElement>  shipmentLineList = getCompleteOrderDetailsOutYFCDoc.getElementsByTagName("ShipmentLine");
					
					
					if ((shipmentLineList.getLength()>0) && (orderLineList.getLength()>0)){
						for (YFCElement orderLineEle : orderLineList) {
							strOrderLineOLK = orderLineEle.getAttribute("OrderLineKey");
							
							log.verbose("OrderLineOLK: "+strOrderLineOLK);
							
							for (YFCElement shipmentLineEle : shipmentLineList) {
								strShipmentLineOLK = shipmentLineEle.getAttribute("OrderLineKey");
								
								log.verbose("ShipmentLineOLK: "+strShipmentLineOLK);
								
								if (strOrderLineOLK.equals(strShipmentLineOLK)){
									log.verbose(strShipmentLineOLK + "  =  "+strOrderLineOLK);
									
									strShipmentLineQty = shipmentLineEle.getAttribute("Quantity");
									orderLineEle.setAttribute("ShippedQty", strShipmentLineQty);
									
									log.verbose("ShipmentLineQty => "+strShipmentLineQty);
									
								}//if (strOrderLineOLK.equals(strShipmentLineOLK)){
								else{
									log.verbose(strShipmentLineOLK + "  !=  "+strOrderLineOLK);
								}//else
							}//for (YFCElement shipmentLineEle : shipmentLineList) {
						}//	for (YFCElement orderLineEle : orderLineList) {
					}//if ((shipmentLineList.getLength()>0) && (orderLineList.getLength()>0)){
				}//if (strOrdInvShpmntKey.equals(strShpShpmntKey))
				else{
					log.verbose(" OrderInvoice Level Shipment Key and Shipment Level Shipment Key are NOT equal.");
				}//else
			}	//for (YFCElement shipmentEle : shipmentList){
		}//if (shipmentList.getLength()>0){
		else
		{
			log.verbose("<Shipment> tag(s) NOT present in the input.");
		}
		/**
		 * ends
		 */
		 
		//Start WN-697 : Sterling to consume special characters and include them in customer-facing emails, but to remove them before settlement.	
		log.verbose("Before WN-697 !!!");
		elePersonInfoBillTo = XMLUtil.getElementByXPath(getCompleteOrderDetailsOutDoc, AcademyConstants.XPATH_ORDER_PERSONINFOBILLTO);
		elePersonInfoShipTo = XMLUtil.getElementByXPath(getCompleteOrderDetailsOutDoc, AcademyConstants.XPATH_ORDERLINE_PERSONINFOSHIPTO);
		AcademyUtil.convertUnicodeToSpecialChar(env, elePersonInfoBillTo, elePersonInfoShipTo, false);
		//End WN-697 : Sterling to consume special characters and include them in customer-facing emails, but to remove them before settlement.
		
		log.verbose("Updated Output Document from sendEmailForPOShipment (): --> "
				+ XMLUtil.getXMLString(getCompleteOrderDetailsOutDoc) + "<---");
		log.verbose("sendEmailOnPOShipConfirmation.sendEmailForPOShipment() ends");
		
		return getCompleteOrderDetailsOutDoc;
	}

	/**
	 * This method invokes the getCompleteOrderDetails API for a given OrderHeaderKey
	 * @param env
	 * @param orderHeaderKey
	 * @return
	 */
	private Document getCompleteOrderDetailsToPopulateEmail(YFSEnvironment env,
			String orderHeaderKey) {
		log.verbose("sendEmailOnPOShipConfirmation.callGetCompleteOrderDetails() starts");

		Document getCompleteOrderDetailsInDoc = null;
		Document getCompleteOrderDetailsOutDoc = null;

		try {
			getCompleteOrderDetailsInDoc = XMLUtil
					.createDocument(AcademyConstants.ELE_ORDER);
			getCompleteOrderDetailsInDoc.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_ORDER_HEADER_KEY, orderHeaderKey);
			getCompleteOrderDetailsInDoc.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_DOC_TYPE, "0005");
			env
					.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
							"global/template/api/getCompleteOrderDetails.ToSendEmailForDSV.xml");

			log.verbose("Input document to getCompleteOrderDetails API -->"
					+ XMLUtil.getElementXMLString(getCompleteOrderDetailsInDoc
							.getDocumentElement()));

			getCompleteOrderDetailsOutDoc = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
					getCompleteOrderDetailsInDoc);
			env.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS);
			log.verbose("getCompleteOrderDetails API Output document: --> "+ XMLUtil.getXMLString(getCompleteOrderDetailsOutDoc)+ "<--");
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.verbose("sendEmailOnPOShipConfirmation.callGetCompleteOrderDetails() ends");
		
		return getCompleteOrderDetailsOutDoc;
	}
}