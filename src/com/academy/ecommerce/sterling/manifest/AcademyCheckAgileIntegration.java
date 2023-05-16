package com.academy.ecommerce.sterling.manifest;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyCheckAgileIntegration {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyCheckAgileIntegration.class);
	public Document checkAgileIntegration(YFSEnvironment env, Document inputXML) throws Exception{
	
		Document webserviceOutput = null;
		log.beginTimer("Begining of AcademyCheckAgileIntegration");

		log.verbose("The INPUT xml for Agile " + XMLUtil.getXMLString(inputXML));
		
		String strShipmentKey = inputXML.getDocumentElement().getAttribute("ShipmentKey");
		log.verbose("Shipment Key: "+strShipmentKey);
		Document docGetShipList = XMLUtil.createDocument("Shipment");
		docGetShipList.getDocumentElement().setAttribute("ShipmentKey", strShipmentKey);

		Document shipmentOutputXML = AcademyUtil.invokeService(env,"AcademyGetShipmentDetailsForBOL", docGetShipList);
		log.verbose("Input to AgileWebService call: "+XMLUtil.getXMLString(shipmentOutputXML));
		
		try {
			webserviceOutput = AcademyUtil.invokeService(env, "AcademyCallAgileWebservice", shipmentOutputXML);
			log.verbose("AgileCall succeeded");
		} catch (Exception e) {
			log.verbose("invocation of Agile Call failed");
			e.printStackTrace();
		}
		log.verbose("Obtained output from Agile");
		Element docEle = webserviceOutput.getDocumentElement();
		Element agentEle = (Element)docEle.getElementsByTagName("Agent").item(0);

		if (YFCObject.isVoid(agentEle) ) {
			log.verbose("AgileCall failed");
			YFSException agentException = new YFSException();
			agentException.setErrorCode("EXTN_AGENT_ADD_FAILURE");
			agentException.setErrorDescription("Pack Verify failed because the Agent Address couldnot be obtained from Agile.");
			agentException.printStackTrace();
			log.verbose("Error message should appear now");
			throw agentException;
		}
		log.endTimer("End of AcademyCheckAgileIntegration");
		return inputXML;
	}

}
