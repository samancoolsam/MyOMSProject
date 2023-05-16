package com.academy.ecommerce.sterling.interfaces.api;

import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyPublishReturnBOLInformation {
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyPublishReturnBOLInformation.class);

	private Properties props;

	private String derivedFromOrdHdrKey;

	public void setProperties(Properties props) {
		this.props = props;
	}

	public void publishReturnBOL(YFSEnvironment env, Document inDoc) {
		try {
			log.beginTimer(" Begining of AcademyPublishReturnBOLInformation-> publishReturnBOL Api");
			log
					.verbose("*************** Inside publishReturnBOL of Return Interface ************ ");
			Document outPRODoc = null;
			//Document outAgentAddressDoc = null;
			Document outToAddressDoc = null;
			Document outGetInvoiceList = null;
			//Fix for issue#3301 , appending NMFCDescription at OrderLine/Item level and OrderLine/Item/Extn level
			inDoc = stampItemExtn(env, inDoc);
			log.verbose("outputXML : " + XMLUtil.getXMLString(inDoc));
			
			Element orderElem = inDoc.getDocumentElement();
			Element orderLineElem = (Element) orderElem.getElementsByTagName("OrderLine").item(0);
			if (!YFCObject.isVoid(orderLineElem)) 
			{
				log.verbose("order Line element : " + orderLineElem.toString());
				derivedFromOrdHdrKey = orderLineElem.getAttribute("DerivedFromOrderHeaderKey");
				log.verbose("*************** derivedFromOrdHdrKey --  : " + derivedFromOrdHdrKey);
				/**
				 * CR - New White Carrier and Prorate White Glove Shipping charges and tax
				 * Get the derived order details from template
				 */
				if(orderLineElem.getElementsByTagName("DerivedFromOrder").getLength()>0){
					orderElem.setAttribute("SalesOrderNo",((Element)orderLineElem.getElementsByTagName("DerivedFromOrder").item(0)).getAttribute(AcademyConstants.ATTR_ORDER_NO));
				}else{
					Document orderDocument = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
					orderDocument.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, derivedFromOrdHdrKey);
					env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, "<OrderList> <Order OrderNo=''/> </OrderList>" );
					Document orderListDocument = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, orderDocument);
					env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
					Element orderListElement = orderListDocument.getDocumentElement();
					log.verbose("order list Element - " + XMLUtil.getElementXMLString(orderListElement));
					Element orderElement = (Element) XPathUtil.getNode(orderListElement, "/OrderList/Order");
					if(!YFCObject.isVoid(orderElement))
					{
						orderElem.setAttribute("SalesOrderNo", orderElement.getAttribute(AcademyConstants.ATTR_ORDER_NO));
					}
				}				
			}
			/**
			 * CR - New White Carrier and Prorate White Glove Shipping charges and tax
			 * Get the Pro# from template
			 */
			if(orderElem.getElementsByTagName(AcademyConstants.ELE_EXTN).getLength()>0){
				orderElem.setAttribute("ProNo",((Element)orderElem.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0)).getAttribute(AcademyConstants.ATTR_EXTN_PRO_NO));
			}else{
				outPRODoc = getPRONumber(env);
				log.verbose("*************** pro number : "
						+ XMLUtil.getXMLString(outPRODoc));
				orderElem.setAttribute("ProNo", outPRODoc.getDocumentElement()
						.getAttribute("ProNumber"));
			}
			//outAgentAddressDoc = getAgentAddressLocation(env, inDoc);
			
			//Begin - To Fetch To Address for WG Carrier Change CR
			log.verbose("Calling getToAddress");
			outToAddressDoc = getToAddress(env);
			log.verbose("*************** To Address : "
					+ XMLUtil.getXMLString(outToAddressDoc));
			Element eleOrganization = (Element)outToAddressDoc.getDocumentElement().getElementsByTagName("Organization").item(0);
			Element eleToAddress = (Element)eleOrganization.getElementsByTagName("CorporatePersonInfo").item(0);
			XMLUtil.importElement(orderElem, eleToAddress);
			
			log.verbose("*************** eleToAddress : "
					+ XMLUtil.getElementXMLString(eleToAddress));
			
			//End - To Fetch To Address for WG Carrier Change CR
			
			outGetInvoiceList = getOrderInvoiceList(env, inDoc);
			log.verbose("*************** outGetInvoiceList : "
					+ XMLUtil.getXMLString(outGetInvoiceList));
			/*Element addressElem = (Element) outAgentAddressDoc
					.getDocumentElement().cloneNode(true);
			/XMLUtil.importElement(orderElem, addressElem);*/
			Element invoiceElem = (Element) outGetInvoiceList
					.getDocumentElement().cloneNode(true);
			XMLUtil.importElement(orderElem, invoiceElem);
			Document publishDocument = removeNonWhiteGloveLines(inDoc);
			// Check for Valid WhiteGlove OrderLines
			if(publishDocument.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_ORDER_LINE).getLength() > 0){
				// Publish Return BOL Message only for WhiteGlove OrderLines
				log
						.verbose("*************** invoking service AcademyPublishBOLForReturn -- input doc : "
								+ XMLUtil.getXMLString(publishDocument));
				AcademyUtil.invokeService(env, "AcademyPublishBOLForReturn",
						publishDocument);
			}
			log.endTimer(" End of AcademyPublishReturnBOLInformation-> publishReturnBOL Api");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Document getToAddress(YFSEnvironment env) throws Exception
	{
		// TODO Auto-generated method stub
		log.verbose("Inside getToAddress");
		String strOrgCode=props.getProperty("OrganizationCode");
		log.verbose("Organization Code is "+ strOrgCode);
		Document indocOrgList = XMLUtil.createDocument("Organization");
		indocOrgList.getDocumentElement().setAttribute("OrganizationCode", strOrgCode);
		log.verbose("Calling getOrganizationList with input " +XMLUtil.getXMLString(indocOrgList));
		Document outdocOrgList = AcademyUtil.invokeAPI(env, "getOrganizationList", indocOrgList);
		log.verbose("Output of getOrganizationList " +XMLUtil.getXMLString(outdocOrgList));
		log.verbose("Returning to main method");
		return outdocOrgList;
	}

	private Document removeNonWhiteGloveLines(Document inDoc) {
		log.beginTimer(" Begining of AcademyPublishReturnBOLInformation-> removeNonWhiteGloveLines Api");
		log.verbose("***** removing not white glove lines *******");
		Element eleOrderLines = (Element) inDoc.getElementsByTagName(
				"OrderLines").item(0);
		
		NodeList nList = inDoc.getElementsByTagName("OrderLine");
		if (!YFCObject.isVoid(nList)) {
			int len = nList.getLength();
			for (int i = 0; i < len; i++) {
				Element currOrderLine = (Element) nList.item(i);
				Element itemElem = (Element) currOrderLine
						.getElementsByTagName("Item").item(0);
				if (itemElem != null && itemElem.hasChildNodes()) {
					Element itemExtnElem = (Element) itemElem
							.getElementsByTagName("Extn").item(0);
					if (itemExtnElem != null && itemExtnElem.hasAttributes()) {

						if (("N").equals(itemExtnElem
								.getAttribute("ExtnWhiteGloveEligible"))) {
							XMLUtil.removeChild(eleOrderLines, currOrderLine);
						}
					}
				}
			}
		}
		log.endTimer(" End of AcademyPublishReturnBOLInformation-> removeNonWhiteGloveLines Api");
		return inDoc;

	}

	private Document getOrderInvoiceList(YFSEnvironment env, Document inDoc) {
		Document outDoc = null;
		try {
			log.verbose("***** Inside getOrderInvoiceList  *****");
			Document inputDoc = XMLUtil.createDocument("OrderInvoice");
			inputDoc.getDocumentElement().setAttribute("OrderHeaderKey",
					derivedFromOrdHdrKey);
			inputDoc.getDocumentElement().setAttribute("InvoiceType",
					"SHIPMENT");
			log
					.verbose("***** invoking api getOrderInvoiceList -- input doc : "
							+ XMLUtil.getXMLString(inputDoc));
			outDoc = AcademyUtil
					.invokeAPI(env, "getOrderInvoiceList", inputDoc);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return outDoc;
	}

	/*private Document getAgentAddressLocation(YFSEnvironment env, Document inDoc)
			throws Exception {
		log
				.verbose("*** invoking callAgileInterfaceWebservice with input doc *** "
						+ XMLUtil.getXMLString(inDoc));

		env.setTxnObject("AgileMethodName", "InvokeAgileWebserviceForReturn");

		Document outDoc = AcademyUtil.invokeService(env,
				"AcademyAgentAddressSOAPCall", inDoc);

		return outDoc;

	}*/

	private Document getPRONumber(YFSEnvironment env) {
		Document outDoc = null;
		Document inputToPROSvc = null;
		try {
			inputToPROSvc = XMLUtil.createDocument("Order");
			outDoc = AcademyUtil.invokeService(env, "AcademyGeneratePRONumber",
					inputToPROSvc);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return outDoc;
	}
	
	private Document stampItemExtn(YFSEnvironment env, Document inDoc) {
		
		Element orderElement = inDoc.getDocumentElement();
		NodeList orderLineNL = orderElement.getElementsByTagName("OrderLine");
		
		try {
			
		for (int orderLineNLCount = 0; orderLineNLCount < orderLineNL.getLength(); orderLineNLCount++) {
			
			Element orderLineEle = (Element) orderLineNL.item(orderLineNLCount);
			Element itemEle = (Element) orderLineEle.getElementsByTagName("Item").item(0);
			String itemID = itemEle.getAttribute("ItemID");
			//String NMFCDesc = itemEle.getAttribute("NMFCDescription");
			
			Document inputGetItemList = XMLUtil.createDocument("Item");
			inputGetItemList.getDocumentElement().setAttribute("ItemID", itemID);
			Document outGetItemList = AcademyUtil.invokeService(env, "AcademyGetItemExtnForBOL", inputGetItemList);
			Element outputItemEle = outGetItemList.getDocumentElement();
			Element extnItemEle = (Element)outputItemEle.getElementsByTagName("Extn").item(0);
			String NMFCDesc = extnItemEle.getAttribute("ExtnNMFCDescription");
			itemEle.setAttribute("NMFCDescription", NMFCDesc);
			
			Node dup = inDoc.importNode(extnItemEle, false);
			itemEle.appendChild(dup);

		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inDoc; 
	}	
	
}
