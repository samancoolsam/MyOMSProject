package com.academy.ecommerce.sterling.dsv.shipment;

import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
 * 'CREATE_CONFIRM_SHIPMENT.0005.ON_SUCCESS’ event.
 * 
 * The custom logic will invoke getCompleteOrderDetails API with template
 * to get all the required attribute values to populate the Email component.
 * 
 * @author Manjusha V (215812)
 *
 */

public class AcademySendEmailToBusinessGrpForPOShipment implements YIFCustomApi {

	private static Logger log = Logger.getLogger(AcademySendEmailToBusinessGrpForPOShipment.class.getName());

	public void setProperties(Properties arg0) throws Exception {
	}

	/**
	 * This method will be invoked on'CREATE_CONFIRM_SHIPMENT.0005.ON_SUCCESS’ 
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
		log.verbose("AcademySendEmailToBusinessGrpForPOShipment.sendEmailForPOShipment() starts");
		log.verbose("inDoc : -->" + XMLUtil.getXMLString(inDoc)+ "<----");

		Document getCompleteOrderDetailsOutDoc = null;
		/*Document getOrderListInDoc = null;
		Document getOrderListOutDoc = null;*/
		
		String strPOOrderNo="";
		String strShpmntKey="";
		String strPOOHK="";
		
		/**
		 * Fetch the ShipmentKey & OrderNo from the input document.
		 */
		strShpmntKey = XMLUtil.getString(inDoc, "/Shipments/Shipment/@ShipmentKey");
		strPOOrderNo = XMLUtil.getString(inDoc, "/Shipments/Shipment/@OrderNo");
		strPOOHK = XMLUtil.getString(inDoc, "/Shipments/Shipment/@OrderHeaderKey");
		
		log.verbose("ShipmentKey : " + strShpmntKey);
		log.verbose("strPOOrderNo : " + strPOOrderNo);
		log.verbose("strPOOHK : " + strPOOHK);
		
		/**
		 * get PO's OHK by invoking getOrderList
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
		 * read the PO's OHK
		 *//*
		if (null!=getOrderListOutYFCDoc){
			log.verbose("OutDoc is not null");
			YFCNodeList<YFCElement>  orderList = getOrderListOutYFCDoc.getElementsByTagName("Order");
			
			if (orderList.getLength() >0){
				log.verbose("<Order> present");
				
				strPOOHK = XMLUtil.getString(getOrderListOutYFCDoc.getDocument(), "/OrderList/Order/@OrderHeaderKey");
				log.verbose("PO OHK is = " +strPOOHK);
				
			}
			
		}*/

		/**
		 * ends
		 */
		
	
		/**
		 * invoke getCompleteOrderDetailsToPopulateEmail method to prepare and invoke getCompleteOrderDetails API
		 * to get the attribute values required in the email component. OHK is the PO's OHK
		 */
		getCompleteOrderDetailsOutDoc = getCompleteOrderDetailsToPopulateEmail(env, strPOOHK);

		/**
		 * update the getCompleteOrderDetails API output by adding CurrentShipmentKey @ Order level and
		 * 
		 */
		
		getCompleteOrderDetailsOutDoc.getDocumentElement().setAttribute("CurrentShipmentKey",
				strShpmntKey);
			
		log.verbose("Updated Output Document from sendEmailForPOShipment (): --> "
				+ XMLUtil.getXMLString(getCompleteOrderDetailsOutDoc) + "<---");
		log.verbose("AcademySendEmailToBusinessGrpForPOShipment.sendEmailForPOShipment() ends");
		
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