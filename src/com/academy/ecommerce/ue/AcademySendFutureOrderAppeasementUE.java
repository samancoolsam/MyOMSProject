package com.academy.ecommerce.ue;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.pca.ycd.japi.ue.YCDSendFutureOrderCustomerAppeasementUE;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.util.YFCDoubleUtils;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

public class AcademySendFutureOrderAppeasementUE implements
		YCDSendFutureOrderCustomerAppeasementUE {

	public Document sendFutureOrderCustomerAppeasement(YFSEnvironment env,
			Document inDoc) throws YFSUserExitException {
		try {
			//double discountPercent=0;
			AcademyUtil.invokeService(env, "AcademyCreateGCAppeasementService", inDoc);
		/*Element inputElem=inDoc.getDocumentElement();
		inputElem.setAttribute("UserId", env.getUserId());
		Document changeOrderInputXML=XMLUtil.createDocument("Order");
		Element rootElem=changeOrderInputXML.getDocumentElement();
		rootElem.setAttribute("OrderHeaderKey", inputElem.getAttribute("OrderHeaderKey"));
		rootElem.setAttribute("Action", "MODIFY");
		Element appeasementOffer=(Element) inputElem.getElementsByTagName("AppeasementOffer").item(0);
		String discount=appeasementOffer.getAttribute("DiscountPercent");
		if(discount.trim()!=null && !discount.equalsIgnoreCase(""))
    		discountPercent = Double.parseDouble(discount);
		appendAppeasementRecords(env,changeOrderInputXML,inputElem,discountPercent);
		AcademyUtil.invokeAPI(env, "changeOrder", changeOrderInputXML);
		
		
			Document createOrderInputXML=XMLUtil.createDocument("Order");
			Element orderElem=createOrderInputXML.getDocumentElement();
			orderElem.setAttribute(AcademyConstants.ATTR_DOC_TYPE,inputElem.getAttribute(AcademyConstants.ATTR_DOC_TYPE));
			orderElem.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, inputElem.getAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE));
			createPriceInfoElem(createOrderInputXML);
			createPersonInfo(createOrderInputXML,inputElem);
			
			Element orderLinesElem=createOrderInputXML.createElement("OrderLines");
			orderElem.appendChild(orderLinesElem);
			Element orderLineElem=createOrderLine(createOrderInputXML,orderLinesElem);
			Element itemElem=createItemElem(createOrderInputXML,orderLineElem);
			String offerAmount=getOfferAmount(inputElem);
			Element linepriceInfo=createLinePriceInfo(createOrderInputXML,orderLineElem,inputElem,offerAmount);
			Element lineCharges=createOrderInputXML.createElement("LineCharges");
			orderElem.appendChild(lineCharges);
			createLineCharge(createOrderInputXML,lineCharges,offerAmount);
			AcademyUtil.invokeAPI(env, "createOrder", createOrderInputXML);*/
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return inDoc;
	}

	/*private void appendAppeasementRecords(YFSEnvironment env, Document changeOrderInputXML, Element inputElem, Double discountPercent) {
		
		 * <ACADOrderAppeasementList>
		 * <ACADOrderAppeasement AcademyOrderHeaderKey="" AcademyOrderLineKey="" UserId="" AppeasementReason="" AppeasementPercent="" AppeasementAmount=""/>
		 * </ACADOrderAppeasementList>
		 
		Element rootElem=changeOrderInputXML.getDocumentElement();
		Element extnElem=changeOrderInputXML.createElement("Extn");
		rootElem.appendChild(extnElem);
		String userId=env.getUserId();
		NodeList listofSelectedLines=inputElem.getElementsByTagName("OrderLine");
		if(listofSelectedLines!=null){
			int len=listofSelectedLines.getLength();
			if(len>0){
				Element acadOrderAppListElem=changeOrderInputXML.createElement("ACADOrderAppeasementList");
				extnElem.appendChild(acadOrderAppListElem);
				for(int i=0;i<len;i++){
					Element currOrderLine=(Element) listofSelectedLines.item(i);
					Element acadOrderAppeasementElem=changeOrderInputXML.createElement("ACADOrderAppeasement");
					acadOrderAppeasementElem.setAttribute("AcademyOrderHeaderKey", inputElem.getAttribute("OrderHeaderKey"));
					acadOrderAppeasementElem.setAttribute("AcademyOrderLineKey", currOrderLine.getAttribute("OrderLineKey"));
					acadOrderAppeasementElem.setAttribute("UserId", userId);
					acadOrderAppeasementElem.setAttribute("AppeasementReason", inputElem.getAttribute("AppeasementCategory"));
					//acadOrdAppeasement.setAttribute("AppeasementAmount",lineamnt);
				
		if(inputElem.getAttribute("AppeasementCategory").equals("Shipping")){
			
			 * get the Line charges element
			 
			try {
				Element eShipChargeElem=(Element) XPathUtil.getNode(currOrderLine, "LineCharge[@ChargeCategory='Shipping']");
				if(eShipChargeElem!=null && eShipChargeElem.hasAttributes()){
					double lineGrandTotal = Double.parseDouble(eShipChargeElem.getAttribute("ChargeAmount"));
		    		double lineOfferAmount = (lineGrandTotal*0.01)*discountPercent;
		    		acadOrderAppeasementElem.setAttribute("AppeasementAmount",Double.toString(YFCDoubleUtils.roundOff(lineOfferAmount,2)));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			
			 * get the Line overall totals element
			 
			Element eLineOverallTotals = (Element) currOrderLine.getElementsByTagName(AcademyConstants.ELE_LINE_OVERALL_TOTALS).item(0);
    		double lineGrandTotal = Double.parseDouble(eLineOverallTotals.getAttribute(AcademyConstants.ATTR_LINE_TOTAL));
    		double lineOfferAmount = (lineGrandTotal*0.01)*discountPercent;
    		acadOrderAppeasementElem.setAttribute("AppeasementAmount",Double.toString(YFCDoubleUtils.roundOff(lineOfferAmount,2)));
		}
		acadOrderAppListElem.appendChild(acadOrderAppeasementElem);
		}
		}
		}
	}

	private void createLineCharge(Document createOrderInputXML, Element lineCharges, String offerAmount) {
		Element lineCharge=createOrderInputXML.createElement("LineCharge");
		lineCharge.setAttribute("ChargeCategory", "CUSTOMER_APPEASEMENT");
		lineCharge.setAttribute("ChargeName", "CUSTOMER_APPEASEMENT");
		lineCharge.setAttribute("ChargePerLine", offerAmount);
		lineCharges.appendChild(lineCharge);
		
	}

	private String getOfferAmount(Element inputElem) {
		Element appeasementOfferElem=(Element) inputElem.getElementsByTagName("AppeasementOffer").item(0);
		String offerAmount=appeasementOfferElem.getAttribute("OfferAmount");
		return offerAmount;
	}

	private Element createLinePriceInfo(Document createOrderInputXML, Element orderLineElem, Element inputElem, String offerAmount) {
		
		Element linePriceInfoElem=createOrderInputXML.createElement("LinePriceInfo");
		linePriceInfoElem.setAttribute("DisplayUnitPrice", offerAmount);
		linePriceInfoElem.setAttribute("UnitPrice", offerAmount);
		orderLineElem.appendChild(linePriceInfoElem);
		return linePriceInfoElem;
	}

	private Element createItemElem(Document createOrderInputXML, Element orderLineElem) {
		Element itemElem=createOrderInputXML.createElement("Item");
		itemElem.setAttribute("ItemID", AcademyConstants.GC_APPEASEMENT_ITEM);
		itemElem.setAttribute("UnitOfMeasure", "EACH");
		orderLineElem.appendChild(itemElem);
		return itemElem;
	}

	private Element createOrderLine(Document createOrderInputXML, Element orderLinesElem) {
		Element orderLineElem=createOrderInputXML.createElement("OrderLine");
		orderLineElem.setAttribute("PrimeLineNo", "1");
		orderLineElem.setAttribute("SubLineNo", "1");
		orderLineElem.setAttribute("OrderedQty", "1");
		orderLineElem.setAttribute("DeliveryMethod", "SHP");
		orderLineElem.setAttribute("CarrierServiceCode", "GROUND");
		orderLinesElem.appendChild(orderLineElem);
		return orderLineElem;
	}

	private void createPersonInfo(Document createOrderInputXML, Element inputElem) {
		Element newPersonInfoElem=createOrderInputXML.createElement("PersonInfoBillTo");
		createOrderInputXML.getDocumentElement().appendChild(newPersonInfoElem);
		//orderElem.setAttribute("IsNewOrder", "Y");
		Element personInfoBillToElem=(Element) inputElem.getElementsByTagName("PersonInfoBillTo").item(0);
		XMLUtil.copyElement(createOrderInputXML, personInfoBillToElem, newPersonInfoElem);
		
	}

	private void createPriceInfoElem(Document createOrderInputXML) {
		Element priceInfo=createOrderInputXML.createElement("PriceInfo");
		priceInfo.setAttribute("Currency", "USD");
		createOrderInputXML.getDocumentElement().appendChild(priceInfo);
		
	}*/

}
