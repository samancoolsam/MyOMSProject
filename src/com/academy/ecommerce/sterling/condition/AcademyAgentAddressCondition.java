package com.academy.ecommerce.sterling.condition;
/**
 * @author stummala
 */
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyAgentAddressCondition implements YCPDynamicConditionEx {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyAgentAddressCondition.class);
	private Map propMap = null;
	
	/**
	 *  This method evaluate agent address location is not empty for the outbound shipment or return order BOL message
	 */
	public boolean evaluateCondition(YFSEnvironment env, String sName,
			Map mapData, Document inDoc) {
		log.beginTimer("Begining of AcademyAgentAddressCondition-> evaluateCondition Api");
		boolean hasAgentAddress = false;
		try{
			log.verbose("sName is :: "+sName+" inDoc is :: "+XMLUtil.getXMLString(inDoc));
			Element rootEle = inDoc.getDocumentElement();
			if(rootEle.getElementsByTagName("AgentAddressLocation").getLength() > 0){
				Element agentAddressLocEle = (Element)rootEle.getElementsByTagName("AgentAddressLocation").item(0);
				log.verbose("Found the Element AgentAddress Location : "+XMLUtil.getElementXMLString(agentAddressLocEle));
				String strAgentCompName = agentAddressLocEle.getElementsByTagName("Company").getLength() > 0 ? agentAddressLocEle.getElementsByTagName("Company").item(0).getTextContent() : null;
				if(strAgentCompName == null){
					strAgentCompName = agentAddressLocEle.getElementsByTagName("CompanyName").getLength() > 0 ? agentAddressLocEle.getElementsByTagName("CompanyName").item(0).getTextContent() : null;
				}
				String strAgentStreet = agentAddressLocEle.getElementsByTagName("Street").getLength() > 0 ? agentAddressLocEle.getElementsByTagName("Street").item(0).getTextContent() : null;
				String strAgentPostalCode = agentAddressLocEle.getElementsByTagName("PostalCode").getLength() > 0 ? agentAddressLocEle.getElementsByTagName("PostalCode").item(0).getTextContent() : null;
				String strAgentCity = agentAddressLocEle.getElementsByTagName("City").getLength() > 0 ? agentAddressLocEle.getElementsByTagName("City").item(0).getTextContent() : null;
				String strAgentRegion = agentAddressLocEle.getElementsByTagName("Region").getLength() > 0 ? agentAddressLocEle.getElementsByTagName("Region").item(0).getTextContent() : null;
				if(strAgentCompName != null && strAgentCompName.trim().length() > 0 &&
						strAgentStreet != null && strAgentStreet.trim().length()>0 && 
						strAgentPostalCode != null && strAgentPostalCode.trim().length()>0 &&
						strAgentCity != null && strAgentCity.trim().length() >0 &&
						strAgentRegion != null && strAgentRegion.trim().length() > 0)
					hasAgentAddress = true;;
			}	
			log.verbose("Outbound Shipment or Return Order has an Agent Address Location Details : "+hasAgentAddress);
		}catch(Exception e){
			e.printStackTrace();
			log.verbose("Failed to evaluate the AcademyAgentAddressCondition is:\n "+e);
		}
		log.endTimer("End of AcademyAgentAddressCondition-> evaluateCondition Api");
		return hasAgentAddress;
	}

	public void setProperties(Map propMap) {
		this.propMap = propMap;
	}
	
}
