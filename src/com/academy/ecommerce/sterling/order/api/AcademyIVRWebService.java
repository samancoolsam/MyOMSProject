package com.academy.ecommerce.sterling.order.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;

import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author kparvath
 * 
 * Customer gives either Order No or Customer Phone No,
 * 	will get the Full order details as well as Shipment details.
 * 
 * Service Name: AcademyIVRWebService
 * 
 * Input XML:
 * 
 * 	<Order OrderNo="" />
 * 	
 * 			(OR)
 * 
 * 	<Order CustomerPhoneNo="" FromOrderDate="" OrderDateQryType="DATERANGE" ToOrderDate=""/>
 * 
 * Sample Ouput:
 * 
 *  <OrderList TotalOrderList="">
 *  	<Order CustomerFirstName="" CustomerLastName="" OrderDate="" OrderNo="" Status="" GuaranteeDate="" ReturnDate="" CancelDate="">
 *  		<OrderLines>
 *  			<OrderLine Status="">
 *  				<Shipment ActualDeliveryDate="" CarrierServiceCode="" ExpectedDeliveryDate="" ShipDate="" SCAC ="">
 *  					<Containers>
 *							<Container TrackingNo=""/>
 *						</Containers>
 *					</Shipment>
 *				</OrderLine>
 *			</OrderLines>
 *		</Order>
 * </OrderList>
 */
public class AcademyIVRWebService {

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory
	.instance(AcademyIVRWebService.class);

	public Document reqIVRWebService(YFSEnvironment env,Document inXml) throws Exception{

		log.debug("inxml -->AcademyIVRWebService :: requestMessageForIVRWebService"+inXml);

		Document getOrderListOutDoc = AcademyUtil.invokeService(env, "AcademyIVRReqGetOrderList", inXml);

		log.debug("getOrderListoutDoc" + XMLUtil.getXMLString(getOrderListOutDoc));

		NodeList orderNL = getOrderListOutDoc.getElementsByTagName("Order");

		NodeList orderLineNL = getOrderListOutDoc.getElementsByTagName("OrderLine");

		for(int i=0;i<orderNL.getLength();i++){

			NodeList orderDateNL = XMLUtil.getNodeListByXPath(XMLUtil.getDocumentForElement(getOrderListOutDoc.getDocumentElement()), "/OrderList/Order/OrderLines/OrderLine/OrderDates/OrderDate[@DateTypeId='ACADEMY_DELIVERY_DATE']");

			if(orderDateNL.getLength()>0){

				String strCommittedDate =((Element) orderDateNL.item(0)).getAttribute("CommittedDate");

				((Element) orderNL.item(i)).setAttribute("GuaranteeDate", strCommittedDate);
			}

			NodeList orderStatusNL = getOrderListOutDoc.getElementsByTagName("OrderStatus");

			log.verbose("orderStatusNL Length ::" + orderStatusNL.getLength());

			for(int j=0;j<orderStatusNL.getLength();j++){

				Element orderStatusEle = (Element) orderStatusNL.item(j);

				if(orderStatusEle.getAttribute("Status").equals("9000"))
				{
					String strCancelDate = orderStatusEle.getAttribute("StatusDate");
					((Element) orderNL.item(i)).setAttribute("CancelDate", strCancelDate);
				}

				else if(Float.parseFloat(orderStatusEle.getAttribute("Status")) > 3700)
				{
					String strReturnDate = orderStatusEle.getAttribute("StatusDate");
					((Element) orderNL.item(i)).setAttribute("ReturnDate", strReturnDate);
				}
			}

			Element orderEle = (Element) orderNL.item(i);

			String orderNo = orderEle.getAttribute("OrderNo");

			Document getShipmentListOutDoc = invokegetShipmentList(env,orderNo);

			NodeList shipmentNL = getShipmentListOutDoc.getElementsByTagName("Shipment");

			for(int k=0;k<orderLineNL.getLength();k++){

				String orderOLK=((Element)orderLineNL.item(k)).getAttribute("OrderLineKey");

				if(shipmentNL.getLength()!=0){

					for(int l=0;l<shipmentNL.getLength();l++){

						Element shipmentEle = (Element)shipmentNL.item(l);

						NodeList shipmentLinesEle = XMLUtil.getNodeListByXPath(XMLUtil.getDocumentForElement(shipmentEle), "/Shipment/ShipmentLines/ShipmentLine[@OrderLineKey='"+orderOLK+"']");

						if(shipmentLinesEle.getLength()!=0){

							Node importEle = getOrderListOutDoc.importNode(shipmentEle, true);

							Element rootEle = (Element) getOrderListOutDoc.getDocumentElement();

							rootEle.getElementsByTagName("OrderLine").item(k).appendChild(importEle);

						}

					}
				}
			}
		}
		Document formatedMergeDoc = formatMergedDocument(getOrderListOutDoc);

		if(Integer.parseInt(getOrderListOutDoc.getDocumentElement().getAttribute("TotalOrderList")) == 1)
		{

			Element orderEle = (Element) getOrderListOutDoc.getElementsByTagName("Order").item(0);
			
			Document finalDocument = XMLUtil.getDocumentForElement(orderEle);
			
			return finalDocument;
			
		}
		log.debug("Academy IVR Web Service response"+ XMLUtil.getXMLString(formatedMergeDoc));
		return formatedMergeDoc;

	}

	private Document invokegetShipmentList(YFSEnvironment env, String orderNo) throws Exception {


		Document getShipmentListinDoc = XMLUtil.createDocument("Shipment");
		Element orderInvoiceListEle = getShipmentListinDoc.createElement("OrderInvoiceList");
		Element orderInvoiceEle = getShipmentListinDoc.createElement("OrderInvoice");
		orderInvoiceEle.setAttribute("OrderNo", orderNo);
		getShipmentListinDoc.getDocumentElement().appendChild(orderInvoiceListEle);
		orderInvoiceListEle.appendChild(orderInvoiceEle);

		log.debug("get Shipment List input Doc"+ XMLUtil.getXMLString(getShipmentListinDoc));

		Document getShipmentListOutDoc = AcademyUtil.invokeService(env, "AcademyIVRReqGetShipmentList", getShipmentListinDoc);

		log.debug("get Shipment List output Doc"+ XMLUtil.getXMLString(getShipmentListOutDoc));

		return getShipmentListOutDoc;
	}

	public Document formatMergedDocument(Document mergedDoc){

		log.debug("formatMergedDocument:: Merged Xml before formatting"+ XMLUtil.getXMLString(mergedDoc));

		NodeList shipmentNL = mergedDoc.getElementsByTagName("Shipment");
		if(shipmentNL.getLength()!=0){

			for(int j=0;j<shipmentNL.getLength();j++){
				Element ele = (Element) shipmentNL.item(j);
				Element ship = (Element) ele.getElementsByTagName("ShipmentLines").item(0);
				ship.getParentNode().removeChild(ship);

			}
		}
		NodeList orderLineNL = mergedDoc.getElementsByTagName("OrderLine");
		for(int j=0;j<orderLineNL.getLength();j++){
			Element ele = (Element) orderLineNL.item(j);

			ele.removeAttribute("OrderLineKey");
		}

		mergedDoc.getDocumentElement().removeAttribute("LastOrderHeaderKey");
		mergedDoc.getDocumentElement().removeAttribute("LastRecordSet");
		mergedDoc.getDocumentElement().removeAttribute("ReadFromHistory");

		NodeList orderLineNList = mergedDoc.getElementsByTagName("OrderLine");
		if(orderLineNList.getLength()!=0){

			for(int j=0;j<orderLineNList.getLength();j++){
				Element orderLineEle = (Element) orderLineNList.item(j);

				Element orderDatesEle = (Element) orderLineEle.getElementsByTagName("OrderDates").item(0);
				orderDatesEle.getParentNode().removeChild(orderDatesEle);

				Element orderStatusesEle = (Element) orderLineEle.getElementsByTagName("OrderStatuses").item(0);
				orderStatusesEle.getParentNode().removeChild(orderStatusesEle);
			}
		}

		return mergedDoc;
	}

}
