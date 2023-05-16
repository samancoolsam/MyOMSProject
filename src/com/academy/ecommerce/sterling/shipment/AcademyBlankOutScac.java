package com.academy.ecommerce.sterling.shipment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyBlankOutScac 
{
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyBlankOutScac.class);
	public void blankOutScac(YFSEnvironment env, Document inXML) throws Exception
	{
		log.beginTimer(" Begin of AcademyBlankOutScac->blankOutScac ()");
		log.verbose("***inXML***" +XMLUtil.getXMLString(inXML));
		log.verbose("***outside APO FPO check***" +env.getTxnObject("RemoveScac"));
		
		Element eleToAddress = (Element) inXML.getElementsByTagName("ToAddress").item(0);
		log.verbose("eleToAddress - " +XMLUtil.getElementXMLString(eleToAddress));
		Element eleExtn1 = (Element) eleToAddress.getElementsByTagName("Extn").item(0);
		log.verbose("eleExtn1 - " +XMLUtil.getElementXMLString(eleExtn1));
		
		Boolean str = false;
		if(!YFCObject.isVoid(env.getTxnObject("RemoveScac")))
		{
			str = (Boolean)env.getTxnObject("RemoveScac");
		}
		if(str)
		{
			log.verbose("***inside APO FPO check***");
			
			if(!"Y".equals(eleExtn1.getAttribute("ExtnIsAPOFPO")) && !"Y".equals(eleExtn1.getAttribute("ExtnIsPOBOXADDRESS")))
			{
				env.setTxnObject("RemoveScac", true);
				log.verbose("***inside APO FPO check***");
				Document docShipment = XMLUtil.createDocument("Shipment");
				docShipment.getDocumentElement().setAttribute("ShipmentKey", inXML.getDocumentElement().getAttribute("ShipmentKey"));
				docShipment.getDocumentElement().setAttribute("RoutingSource", "");
				//docShipment.getDocumentElement().setAttribute("SCAC", "");
				//docShipment.getDocumentElement().setAttribute("CarrierServiceCode", "");
				log.verbose("***docShipment***" +XMLUtil.getXMLString(docShipment));
				Document outXML = AcademyUtil.invokeAPI(env, "changeShipment", docShipment);
				log.verbose("***outXML***" +XMLUtil.getXMLString(outXML));
			}			
		}
	}
}
