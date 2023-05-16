/**
 * 
 */
package com.academy.ecommerce.order.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author AbKrishna
 * 
 */
public class AcademyGetOrderInvoiceDetailsForOrderStatus implements
		YIFCustomApi {

	private static Logger log=Logger.getLogger(AcademyGetOrderInvoiceDetailsForOrderStatus.class.getName());

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	public Document getOrderInvoiceNos(YFSEnvironment yfsenv, Document inXML) throws Exception {

		if (!YFCObject.isVoid(inXML.getDocumentElement())) 
		{
			Element inputXML = inXML.getDocumentElement();
			
			Element dataAreaEle = (Element)inputXML.getElementsByTagName("DataArea").item(0);
			log.verbose("********* Data Area Element " + XMLUtil.getElementXMLString(dataAreaEle));

			Element orderShippingNode = (Element)dataAreaEle.getElementsByTagName("OrderShipping").item(0);
			log.verbose("********* Order shipping Element " + XMLUtil.getElementXMLString(orderShippingNode));
			
			NodeList orderStatusItemList = orderShippingNode.getElementsByTagName("OrderStatusItem");
				
			if(orderStatusItemList != null)
			{
				int numberOfNodes = orderStatusItemList.getLength();
				
				for(int i=0; i<numberOfNodes; i++)
				{
					Element orderStatusItem = (Element)orderStatusItemList.item(i);
					log.verbose("********* Order Status Element " + XMLUtil.getElementXMLString(orderStatusItem));

					Node customerFieldElement = orderStatusItem.getElementsByTagName("CustomerField1").item(0);
					Element customerField1 = (Element) customerFieldElement;
					log.verbose("********* Customer Field Element " + XMLUtil.getElementXMLString(customerField1));
					
					Node orderHeaderKeyNode = customerField1.getElementsByTagName("OrderHeaderKey").item(0);
					Node orderLineKeyNode = customerField1.getElementsByTagName("OrderLineKey").item(0);
					
					String strOrderHeaderKey = orderHeaderKeyNode.getTextContent();
					log.verbose("********** Order header Key " + strOrderHeaderKey);

					String strOrderLineKey = orderLineKeyNode.getTextContent();
					log.verbose("********** Order Line Key " + strOrderLineKey);
					
					//String strOrderHeaderKey = XPathUtil.getString(inputXML, "DataArea/OrderShipping/OrderStatusItem/CustomerField1/OrderHeaderKey");
					//String orderLineKey = XPathUtil.getString(inputXML, "DataArea/OrderShipping/OrderStatusItem/CustomerField1/OrderLineKey");
					String invoiceNo = "";
					if (!StringUtil.isEmpty(strOrderHeaderKey)) 
					{
						Document getOrderInvoiceListInputXML;
						getOrderInvoiceListInputXML = XMLUtil.createDocument("OrderInvoice");

						getOrderInvoiceListInputXML.getDocumentElement().setAttribute("OrderHeaderKey", strOrderHeaderKey);
						Document getOrderInvoiceListOutputXML = AcademyUtil.invokeService(yfsenv,"AcademyGetOrderInvoiceListService",getOrderInvoiceListInputXML);
						log.verbose("********** Order Invoice List Output " + XMLUtil.getXMLString(getOrderInvoiceListOutputXML));
						
						NodeList orderInvoiceNL = getOrderInvoiceListOutputXML.getElementsByTagName("OrderInvoice");
						int numberOfOrderInvoices = orderInvoiceNL.getLength();
						log.verbose("************** number of invoices : " + numberOfOrderInvoices);
						
						for (int j = 0; j < numberOfOrderInvoices ; j++) 
						{
							Element OrderInvoiceElement = (Element) orderInvoiceNL.item(j);
							
							  NodeList lineDetailsNL=OrderInvoiceElement.getElementsByTagName("LineDetail");
							  int lineDetailLength= lineDetailsNL.getLength();
								log.verbose("********** Looping through each LineDetails ele to match to lineKey ");
							  for(int m=0;m<lineDetailLength;m++)
							  {
								 Element lineDetailsEle=(Element)lineDetailsNL.item(m);
								 String orderLineKey_invoice=lineDetailsEle.getAttribute("OrderLineKey");
							  
							  
							log.verbose("********** Order Invoice Elements " + XMLUtil.getElementXMLString(OrderInvoiceElement));
							
							//String orderLineKey_invoice = XPathUtil.getString(OrderInvoiceElement, "LineDetails/LineDetail/@OrderLineKey");
							log.verbose("********** Order Line Key : " + strOrderLineKey);
							log.verbose("********** Order Line Key from Invoice List : " + orderLineKey_invoice);
							
							if (strOrderLineKey.equals(orderLineKey_invoice)) 
							{
								String invoiceType = OrderInvoiceElement.getAttribute("InvoiceType");
								log.verbose("********** invoice type " + invoiceType);

								if(invoiceType != null && invoiceType.equals("PRO_FORMA"))
								{
									// case when there is only pro forma invoice, then get the invoice number from extn field
									if(numberOfOrderInvoices >= 1)
									{
										String extnInvoiceNumber = XPathUtil.getString(OrderInvoiceElement, "Extn/@ExtnInvoiceNo");
										log.verbose("********** Only PRO FORMA Invoice available, Extn invoice number  & adding those to shipment confirm msg accordingly **" + extnInvoiceNumber);
										if(invoiceNo.length()==0)
										{
											invoiceNo+=extnInvoiceNumber;
										}
										else
										invoiceNo +=","+extnInvoiceNumber;
									}
								
								}
								/* Commenting this part - as its not required. since we can get the invoice no from extnInvoiceNo of pro-forma invoice references 
								 * else
								{
									if(invoiceNo.length()==0)
									{
										invoiceNo += OrderInvoiceElement.getAttribute("InvoiceNo");
									}
									else
									{
										invoiceNo += "," + OrderInvoiceElement.getAttribute("InvoiceNo") ;
									}
								}*/

							}log.verbose("********** invoice number " + invoiceNo);
							}
						}
						
						log.verbose("************ FINAL Invoice Number - " + invoiceNo);
						customerField1.removeChild(orderHeaderKeyNode);
						customerField1.removeChild(orderLineKeyNode);
						customerFieldElement.setTextContent(invoiceNo);

						log.verbose("************* Customer Field Element : " + XMLUtil.getElementXMLString(customerField1));
						inXML.renameNode(customerField1, "", "CustomerField");
					}
				}
			}
		}
		return inXML;
	}

}
