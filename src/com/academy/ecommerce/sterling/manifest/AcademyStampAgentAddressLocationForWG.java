package com.academy.ecommerce.sterling.manifest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyStampAgentAddressLocationForWG {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyStampAgentAddressLocationForWG.class);
	public Document stampAgentAddLoc(YFSEnvironment env, Document inputXML) throws Exception {
	
		String companyName ="";
		String street ="";
		String street2 = "";
		String city = "";
		String region = "";
		String postalCode = "";
		String countryName = "";
		String fax = "";
		String fuCode = "";
		log.beginTimer(" Begining of AcademyStampAgentAddressLocationForWG-> stampAgentAddLoc Api");
		log.verbose("*******************************here*******************************");
		log.verbose("input xml value " + XMLUtil.getXMLString(inputXML));
		
		//Element shipmentEle = (Element) (inputXML.getDocumentElement().getElementsByTagName("Shipment").item(0));
		Element toAddressEle = (Element) inputXML.getDocumentElement().getElementsByTagName("ToAddress").item(0);
		//Document inputToWebservice = XMLUtil.getDocumentForElement(shipmentEle);
		Element shipEle = inputXML.getDocumentElement();
		
		log.verbose("*******************************here again *******************************");
		log.verbose("input xml value " + XMLUtil.getXMLString(inputXML));
		
		Document outputOfWebservice = AcademyUtil.invokeService(env, "AcademyCallAgileWebservice", inputXML);
		log.verbose("input xml value 00000000000" );

		Element docEle = outputOfWebservice.getDocumentElement();
				
		Element agentEle = (Element)docEle.getElementsByTagName("Agent").item(0);
		if(YFCObject.isVoid(agentEle))
		{
			log.verbose("Could not get the Agent Adddress element in the response.");
			throw new YFSException("Could not get the Agent Adddress from Webservices call.");
		}

		if (!YFCObject.isNull(agentEle) ) {

			Node compName = agentEle.getElementsByTagName("CompanyName").item(0);
			if (!YFCObject.isNull(compName)) {
				companyName = compName.getTextContent();
				shipEle.setAttribute("AgentCompany", companyName);
			}

			Node st = agentEle.getElementsByTagName("Street").item(0);
			if (!YFCObject.isNull(st)) {
				street = st.getTextContent();
				shipEle.setAttribute("AgentAddressLine1", street);
			}
			
			Node st2 = agentEle.getElementsByTagName("Street2").item(0);
			if (!YFCObject.isNull(st2)) {
				street2 = st2.getTextContent();
				shipEle.setAttribute("AgentAddressLine2", street2);
			}
			
			Node ct = agentEle.getElementsByTagName("City").item(0);
			if (!YFCObject.isNull(ct)) {
				city = ct.getTextContent();
				shipEle.setAttribute("AgentCity", city);
			}
			
			Node reg = agentEle.getElementsByTagName("Region").item(0);
			if (!YFCObject.isNull(reg)) {
				region = reg.getTextContent();
				shipEle.setAttribute("AgentState", region);
			}

			Node country = agentEle.getElementsByTagName("CountryName").item(0);
			if (!YFCObject.isNull(country)) {
				countryName = country.getTextContent();
				shipEle.setAttribute("AgentCountryName", countryName);
			}
			
			Node postCod = agentEle.getElementsByTagName("PostalCode").item(0);
			if (!YFCObject.isNull(postCod)) {
				postalCode = postCod.getTextContent();
				shipEle.setAttribute("AgentZipCode", postalCode);
			}
			
/*			Node fx = agentEle.getElementsByTagName("Fax").item(0);
			if (!YFCObject.isNull(fx)) {
				fax = fx.getTextContent();
				shipEle.setAttribute("Company", companyName);
			}
			
			Node fucd = agentEle.getElementsByTagName("FUCode").item(0);
			if (!YFCObject.isNull(fucd)) {
				fuCode = fucd.getTextContent();
				shipEle.setAttribute("Company", companyName);
			}
*/		
			return inputXML;
			} 
			else {
				log.verbose("Couldnot get the Agent Adddress from Webservices call.");
				throw new YFSException("Couldnot get the Agent Adddress from Webservices call.");
				
			}
		//log.endTimer(" End of AcademyStampAgentAddressLocationForWG-> stampAgentAddLoc Api");
		}

}
