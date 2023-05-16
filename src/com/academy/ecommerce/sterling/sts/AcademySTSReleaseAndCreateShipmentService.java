package com.academy.ecommerce.sterling.sts;

/*##################################################################################
*
* Project Name                : STS
* Module                      : OMS
* Author                      : CTS
* Date                        : 21-July-2020 
* Description				  : This class releases STS OrderLines and creates Shipment
* 							    for the same released Lines
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       Remarks/Description                      
* ---------------------------------------------------------------------------------
* 21-JULY-2020		CTS  	 			  1.0           	Initial version
* ##################################################################################*/

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XPathUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySTSReleaseAndCreateShipmentService { 
	
	/**
	 * log variable.
	 */
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSReleaseAndCreateShipmentService.class);
	
	/**
	 * This method is be used process eligible STS lines. This method releases the Sales Order
	 * and also create shipment for the Released STS Lines
	 * 
	 * @param env,
	 * @param inXML
	 */
	public Document processSTSRelease(YFSEnvironment env, Document inDoc) throws Exception{
		
		log.verbose("Start - Inside AcademySTSReleaseAndCreateShipmentService.processSTSRelease()"); 
		String strHoldType = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_HOLD_TYPE);
		
		if(!YFCObject.isVoid(strHoldType) && strHoldType.equals(AcademyConstants.STR_STS_RELEASE_HOLD)){
			log.verbose("Order contianer STS_RELEASE_HOLD being created or resolved"); 
			NodeList nlResolvedHold = XPathUtil.getNodeList(inDoc, "/HoldType/OrderLines/OrderLine[@MinLineStatus != '9000']/OrderHoldType[@Status='1300']");
			
			log.verbose(" Resolved Hold Coount :: "+nlResolvedHold.getLength());			
			if(nlResolvedHold.getLength() > 0){
				log.verbose(" STS Hold is resolved. Release Sales Order");

				String strOrderHeaderKey = XPathUtil.getString(inDoc, "/HoldType/OrderLines/OrderLine/@OrderHeaderKey");
				releaseOrder(env, strOrderHeaderKey);
		
				Document docOrderReleaseList = getOrderReleaseList(env, strOrderHeaderKey);

				//Fetch Release Details and create Shipment for the same.
				NodeList nlOrderRelease = XPathUtil.getNodeList(docOrderReleaseList, "/OrderReleaseList/OrderRelease[@MinOrderReleaseStatus='3200' and " +
						"OrderLines/OrderLine/@FulfillmentType='STS']");
				
				log.verbose(" nlOrderRelease :: "+nlOrderRelease.getLength());			
				if(nlOrderRelease.getLength() > 0){
					for (int iOR = 0; iOR < nlOrderRelease.getLength(); iOR++) {
						Element eleOrderRelease = (Element) nlOrderRelease.item(iOR);
						consolidateToShipment(env, eleOrderRelease.getAttribute(AcademyConstants.ATTR_RELEASE_KEY));
					}
				}
			}
		}
				
		log.verbose("End - Inside AcademySTSReleaseAndCreateShipmentService.processSTSRelease()"); 		
		return inDoc;
	}

	
	/**
	 * This method is be used to release Sales Order
	 * 
	 * @param env,
	 * @param strOrderHeaderKey
	 */
	
	private void releaseOrder(YFSEnvironment env, String strOrderHeaderKey) throws Exception {
		log.verbose("Start - Inside AcademySTSReleaseAndCreateShipmentService.releaseOrder()"); 
	
		Document docReleaseOrder = XMLUtil.createDocument(AcademyConstants.ELE_RELEASE_ORDER);
		Element eleReleaseOrder = docReleaseOrder.getDocumentElement();
		eleReleaseOrder.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
		eleReleaseOrder.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		eleReleaseOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
		eleReleaseOrder.setAttribute(AcademyConstants.CHECK_INVENTORY, AcademyConstants.STR_NO);
		
		log.beginTimer("releaseOrder for STS Sales Order");
		AcademyUtil.invokeAPI(env, AcademyConstants.API_RELEASE_ORDER, docReleaseOrder);
		log.endTimer("releaseOrder for STS Sales Order");
		
		log.verbose("End - Inside AcademySTSReleaseAndCreateShipmentService.releaseOrder()"); 		
	}
	
	/**
	 * This method is be used to invoke getOrderReleaseList API and get release details for an order.
	 * 
	 * @param env,
	 * @param strOrderHeaderKey
	 */
	
	public Document getOrderReleaseList(YFSEnvironment env, String strOrderHeaderKey) throws Exception {
		log.verbose("Start - Inside AcademySTSReleaseAndCreateShipmentService.getOrderReleaseList()"); 
	
		Document docGetOrderReleaseListInp = XMLUtil.createDocument(AcademyConstants.ELE_ORD_RELEASE); 
		docGetOrderReleaseListInp.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
		
		Document docGetOrderReleaseListTemp = XMLUtil.getDocument("<OrderReleaseList><OrderRelease ReleaseNo='' OrderReleaseKey='' MinOrderReleaseStatus='' MaxOrderReleaseStatus='' >" +
				"<OrderLines><OrderLine OrderLineKey='' FulfillmentType='' DeliveryMethod='' /></OrderLines></OrderRelease></OrderReleaseList>  ");
		
		log.verbose("Input for getOrderReleaseList Api is "+XMLUtil.getXMLString(docGetOrderReleaseListInp));
		
		env.setApiTemplate(AcademyConstants.API_GET_ORDER_RELEASE_LIST, docGetOrderReleaseListTemp); 
		log.beginTimer("getOrderReleaseList for SO Release Details");
		Document docGetOrderReleaseListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_RELEASE_LIST, docGetOrderReleaseListInp);
		log.endTimer("getOrderReleaseList for SO Release Details");
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_RELEASE_LIST); 
		
		log.verbose("End - Inside AcademySTSReleaseAndCreateShipmentService.getOrderReleaseList()"); 		
		return docGetOrderReleaseListOut;
	}
	
	/**
	 * This method is be used to invoke consolidateToShipment API.
	 * 
	 * @param env,
	 * @param strOrderReleaseKey
	 */
	
	public void consolidateToShipment(YFSEnvironment env, String strOrderReleaseKey) throws Exception {
		log.verbose("Start - Inside AcademySTSReleaseAndCreateShipmentService.consolidateToShipment()"); 
	
		Document docReleaseOrder = XMLUtil.createDocument(AcademyConstants.ELE_RELEASE_ORDER);
		Element eleReleaseOrder = docReleaseOrder.getDocumentElement();
		eleReleaseOrder.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
		eleReleaseOrder.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		eleReleaseOrder.setAttribute(AcademyConstants.ATTR_RELEASE_KEY, strOrderReleaseKey);
		
		log.beginTimer("consolidateToShipment for STS Sales Order");
		AcademyUtil.invokeAPI(env, AcademyConstants.API_CONSOL_SHIP, docReleaseOrder);
		log.endTimer("consolidateToShipment for STS Sales Order");
		
		log.verbose("End - Inside AcademySTSReleaseAndCreateShipmentService.consolidateToShipment()"); 		
	}
	

}