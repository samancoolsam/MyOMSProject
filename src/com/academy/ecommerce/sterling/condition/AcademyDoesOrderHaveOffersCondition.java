package com.academy.ecommerce.sterling.condition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.ycp.japi.YCPDynamicCondition;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
/*
 * <?xml version="1.0" encoding="UTF-8"?>
<AcademyMergedDocument>
    <InputDocument>
        <Order BillToID="100000050" CarrierServiceCode=""
            CustomerEMailID="vipin364@gmail.com" MaximumRecords="5000"
            OrderNo="Y100000120" OrderPurpose="" SCAC="">
            <OrderLines>
                <OrderLine CarrierServiceCode="" DeliveryMethod="DEL"
                    OrderedQty="1.00" PrimeLineNo="1" SCAC=""
                    ShipNode="" SubLineNo="1">
                    <LinePriceInfo IsPriceLocked="N" LineTotal="0.00"
                        ListPrice="0.00" TaxableFlag="N" UnitPrice=""/>
                </OrderLine>
                <OrderLine CarrierServiceCode="" DeliveryMethod="PICK"
                    OrderedQty="1.00" PrimeLineNo="2" SCAC=""
                    ShipNode="" SubLineNo="1">
                    <LinePriceInfo IsPriceLocked="N" LineTotal="0.00"
                        ListPrice="0.00" TaxableFlag="N" UnitPrice=""/>
                </OrderLine>
                <OrderLine CarrierServiceCode="" DeliveryMethod="PICK"
                    OrderedQty="1.00" PrimeLineNo="3" SCAC=""
                    ShipNode="" SubLineNo="1">
                    <LinePriceInfo/>
                </OrderLine>
                <OrderLine CarrierServiceCode="" DeliveryMethod="PICK"
                    OrderedQty="1.00" PrimeLineNo="4" SCAC=""
                    ShipNode="" SubLineNo="1">
                    <LinePriceInfo/>
                </OrderLine>
            </OrderLines>
        </Order>
    </InputDocument>
</AcademyMergedDocument>

 */
public class AcademyDoesOrderHaveOffersCondition implements YCPDynamicCondition{
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyDoesOrderHaveOffersCondition.class);
	private Properties props;	
	   public void setProperties(Properties props) {
	        this.props = props;
	    }
	public boolean evaluateCondition(YFSEnvironment env, String sName,
			Map mapData, String sXMLData) {
		log.beginTimer(" Begining of AcademyDoesOrderHaveOffersCondition -> evaluateCondition Api");
		HashMap<String, String> hsOrigOrderLine = new HashMap();
		HashMap<String, String> hsWebOrderLine = new HashMap();
		//to check if input XML is from web. Web order will have isNewOrder as Y
		YFCDocument inXml = YFCDocument.getDocumentFor(sXMLData);
		if(!YFCObject.isVoid(inXml)){
			Document inDoc = inXml.getDocument();
			Element einElem = inDoc.getDocumentElement();
			
			if(!YFCObject.isVoid(einElem))
			
{
				Element einputElem = (Element) einElem.getElementsByTagName("InputDocument").item(0);
				if(!YFCObject.isVoid(einputElem)){
					Element eOrderElem = (Element) einputElem.getElementsByTagName("Order").item(0);
					if(!YFCObject.isVoid(eOrderElem)){
						List nl = XMLUtil.getSubNodeList(eOrderElem, "OrderLine");
						int n1size = nl.size();
						for(int i= 0; i < n1size; i++){
							Element eOrderline = (Element) nl.get(i);
							String sPrimeLineNo = eOrderline.getAttribute("PrimeLineNo");
							String sQty = eOrderline.getAttribute("OrderedQty");
							hsOrigOrderLine.put(sPrimeLineNo, sQty);
							
						}
					}
				}
				
				Element eEnvElem = (Element) einElem.getElementsByTagName("EnvironmentDocument").item(0);
				if(!YFCObject.isVoid(eEnvElem)){
					Element eEnvOrderElem = (Element) eEnvElem.getElementsByTagName("Order").item(0);
					if(!YFCObject.isVoid(eEnvOrderElem)){
						List n2 = XMLUtil.getSubNodeList(eEnvOrderElem, "OrderLine");
						int n2size = n2.size();
						for(int i= 0; i < n2size; i++){
							Element eEnvOrderline = (Element) n2.get(i);
							String sPrimeLineNo = eEnvOrderline.getAttribute("PrimeLineNo");
							String sQty = eEnvOrderline.getAttribute("OrderedQty");
							hsWebOrderLine.put(sPrimeLineNo, sQty);
							
						}
					}
				}
			
				if (hsWebOrderLine.size()!= hsOrigOrderLine.size()||!(hsWebOrderLine.equals(hsOrigOrderLine))){
					log.endTimer(" Ending of AcademyDoesOrderHaveOffersCondition -> evaluateCondition Api");
					return true;
				}
}
		}		
		log.endTimer(" Ending of AcademyDoesOrderHaveOffersCondition -> evaluateCondition Api");
		return false;
		
	}
	private boolean comparelineswithQty(HashMap<String, String> hsWebOrderLine,
			HashMap<String, String> hsOrigOrderLine) {
		// TODO Auto-generated method stub
		
		int isize = hsWebOrderLine.size();
		for(int i= 0; i < isize; i++){
		
			if (hsWebOrderLine.equals(hsOrigOrderLine))
			return true;
		}
		return false;
	}

}


