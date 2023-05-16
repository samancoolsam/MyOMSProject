/**
 * 
 */
package com.academy.ecommerce.sterling.condition;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author stummala
 *
 */
public class AcademyIsWhiteGloveCarrierCondition implements
		YCPDynamicConditionEx {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyIsWhiteGloveCarrierCondition.class);
	private Map propMap = null;
	/* (non-Javadoc)
	 * @see com.yantra.ycp.japi.YCPDynamicConditionEx#evaluateCondition(com.yantra.yfs.japi.YFSEnvironment, java.lang.String, java.util.Map, org.w3c.dom.Document)
	 */
	public boolean evaluateCondition(YFSEnvironment env, String sName,
			Map mapData, Document inDoc) {
		boolean isWGCarrier = false;
		try{
			Document docWGSCACLst = getWhiteGloveScacList(env);			
			if(docWGSCACLst != null){
				Element parentEle = inDoc.getDocumentElement();
				// Check - Parent Element Name 
				log.verbose("****** Parent Node Name is : "+parentEle.getNodeName()+"****** \n");
				if(parentEle.getNodeName().equals(AcademyConstants.ELE_CONTAINERS)){
					// which means the condition is invoking from event 'VERIFICATION_DONE' on Container Pack Verification transaction
					NodeList lstContainer = parentEle.getElementsByTagName(AcademyConstants.ELE_CONTAINER); 
					for(int i=0;i<lstContainer.getLength(); i++){
						String curScac = ((Element)lstContainer.item(i)).getAttribute(AcademyConstants.ATTR_SCAC);
						Node nCommonCode = XPathUtil.getNode(docWGSCACLst, "CommonCodeList/CommonCode[@CodeValue='" + curScac + "']");
						if(nCommonCode != null)
							isWGCarrier = true;						
					}					
				}else if(parentEle.getNodeName().equals(AcademyConstants.ELE_SHIPMENTS)){
					// the condition is invoking from event 'VERIFICATION_DONE' on Container Pack Verification transaction
					NodeList lstShipment = parentEle.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
					for(int i=0;i<lstShipment.getLength(); i++){
						String curScac = ((Element)lstShipment.item(i)).getAttribute(AcademyConstants.ATTR_SCAC);
						Node nCommonCode = XPathUtil.getNode(docWGSCACLst, "CommonCodeList/CommonCode[@CodeValue='" + curScac + "']");
						if(nCommonCode != null)
							isWGCarrier = true;						
					}
				}else if(parentEle.getNodeName().equals(AcademyConstants.ELE_TRACKING_NUMBERS)){
					// Invoking from YCDGetTrackingNumberURLUE implementation
					NodeList lstTrackingNumber = parentEle.getElementsByTagName(AcademyConstants.ELE_TRACKING_NUMBER);
					for(int i=0;i<lstTrackingNumber.getLength(); i++){
						String curScac = ((Element)lstTrackingNumber.item(i)).getAttribute(AcademyConstants.ATTR_SCAC);
						Node nCommonCode = XPathUtil.getNode(docWGSCACLst, "CommonCodeList/CommonCode[@CodeValue='" + curScac + "']");
						if(nCommonCode != null)
							isWGCarrier = true;						
					}
				}else if(parentEle.getNodeName().equals(AcademyConstants.ELE_CONTAINER) || parentEle.getNodeName().equals(AcademyConstants.ELE_SHIPMENT)){
					String curScac = parentEle.getAttribute(AcademyConstants.ATTR_SCAC);
					if(curScac != null && curScac.length()>0){
						Node nCommonCode = XPathUtil.getNode(docWGSCACLst, "CommonCodeList/CommonCode[@CodeValue='" + curScac + "']");
						if(nCommonCode != null)
							isWGCarrier = true;	
					}
				}else if(parentEle.getNodeName().equals(AcademyConstants.ELE_ORDER)){
					// invoking either from Send Email agent to send Shipment Confirmation or X
					String curShipmentKey = parentEle.getAttribute("CurrentShipmentKey");
					if(curShipmentKey != null && curShipmentKey.length()>0){
						// Invoking from Send Email Agent to sent Shipment Confirmation
						String curScac = XPathUtil.getString(parentEle, "Shipments/Shipment[@ShipmentKey='"+curShipmentKey+"']/@SCAC");
						Node nCommonCode = XPathUtil.getNode(docWGSCACLst, "CommonCodeList/CommonCode[@CodeValue='" + curScac + "']");
						if(nCommonCode != null)
							isWGCarrier = true;	
					}else{
						// Invoking from ON_SUCCESS of Draft Order Creation transaction
						String curScac = XPathUtil.getString(parentEle, "OrderLines/OrderLine/@SCAC");
						if(curScac != null && curScac.length()>0){
							Node nCommonCode = XPathUtil.getNode(docWGSCACLst, "CommonCodeList/CommonCode[@CodeValue='" + curScac + "']");
							if(nCommonCode != null)
								isWGCarrier = true;
						}
					}
					
				}
			}			
		}catch(Exception e){
			e.printStackTrace();
			log.verbose("Error while evaluating dynamic condition in AcademyIsWhiteGloveCarrierCondition.evaluateCondition() is:  "+e);
		}
		return isWGCarrier;
	}

	private Document getWhiteGloveScacList(YFSEnvironment env) throws Exception{
		Document docWGSCACLst = null;
		try{
			log.verbose("invoke Common Code in getWhiteGloveScacList() start .."); 
			Document docScacCodeInput = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			String acadWGCarrier = AcademyConstants.STR_WG_SCAC_CODE;
			log.verbose("default Common Code type is : "+acadWGCarrier);
			if(propMap!=null)
				log.verbose("Has something in propMap");
			else
				log.verbose("propMap is null");
			if(propMap!=null && propMap.containsKey("AcadWGCarrier"))
				acadWGCarrier = (String)propMap.get("AcadWGCarrier");
			log.verbose("AcadWGCarrier : " + acadWGCarrier);
			docScacCodeInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, acadWGCarrier);
			docScacCodeInput.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
			env.setApiTemplate(AcademyConstants.API_GET_COMMON_CODELIST, "global/template/api/getCommonCodeList.IsWhiteGloveSCAC.xml");
			log.verbose("Input to getCommonCodeList API is : "+XMLUtil.getXMLString(docScacCodeInput));
			docWGSCACLst = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_COMMON_CODELIST,docScacCodeInput);
			env.clearApiTemplate(AcademyConstants.API_GET_COMMON_CODELIST);
		}catch(Exception e){
			e.printStackTrace();
			log.verbose("Failed to invoke getCommonCodeList API : "+e);
			throw e;
		}
		return docWGSCACLst;
	}

	/* (non-Javadoc)
	 * @see com.yantra.ycp.japi.YCPDynamicConditionEx#setProperties(java.util.Map)
	 */
	public void setProperties(Map propMap) {
		this.propMap=propMap;
		log.verbose("setup the PropMap");
	}

}
