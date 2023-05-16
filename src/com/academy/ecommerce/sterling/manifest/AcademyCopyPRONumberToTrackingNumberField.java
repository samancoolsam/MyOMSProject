package com.academy.ecommerce.sterling.manifest;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author Dipanshu 
 * 
 * This class gets invoked by AcademyCopyPRONumberToTrackingNumberField service on SUCCESS event of 
 * Pack Verify. It copies ProNo to TrackingNo field in case of White Glove SCAC
 * 
 */
public class AcademyCopyPRONumberToTrackingNumberField implements YIFCustomApi
{
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyCopyPRONumberToTrackingNumberField.class);

	public void setProperties(Properties arg0) throws Exception
	{

	}

	public Document copyProNoToTrackingNoField(YFSEnvironment env, Document inDoc)
	{
		log.verbose("**** Input to copyProNoToTrackingNoField ****" + XMLUtil.getXMLString(inDoc));
		Element eleContainer = (Element) inDoc.getDocumentElement();
		String strScac = "";
		String proNo = "";
		String strShipmentContainerKey = eleContainer.getAttribute("ShipmentContainerKey");
		String strShipmentKey = "";
		
		if(!YFCObject.isVoid(strShipmentContainerKey))
		{
			log.verbose("Inside Bulk Flow");	
			try
			{
				Document docGetShipContList = XMLUtil.createDocument("Container");
				docGetShipContList.getDocumentElement().setAttribute("ShipmentContainerKey", strShipmentContainerKey);
				Document outTempGetShipContList = YFCDocument.parse("<Containers> <Container ShipmentContainerKey=\"\" ShipmentKey=\"\" /> </Containers>").getDocument();
				env.setApiTemplate("getShipmentContainerList", outTempGetShipContList);
				Document docOutputGetShipContList = AcademyUtil.invokeAPI(env,"getShipmentContainerList", docGetShipContList);
				env.clearApiTemplates();
				
				log.verbose("getShipmentContainerList API output: "+ XMLUtil.getXMLString(docOutputGetShipContList));
				strShipmentKey = ((Element) docOutputGetShipContList.getDocumentElement().getElementsByTagName("Container").item(0)).getAttribute("ShipmentKey");
				log.verbose("shipment key:"+strShipmentKey);
				
				if(!YFCObject.isVoid(strShipmentKey))
				{
					log.verbose("Finding SCAC and ProNo for the shipment");
					Document docGetShipList = XMLUtil.createDocument("Shipment");
					docGetShipList.getDocumentElement().setAttribute("ShipmentKey", strShipmentKey);
					Document outTempGetShipList = YFCDocument.parse("<Shipments> <Shipment SCAC=\"\" ShipmentKey=\"\" ProNo=\"\" /> </Shipments>").getDocument();
					env.setApiTemplate("getShipmentList", outTempGetShipList);
					Document docOutputGetShipList = AcademyUtil.invokeAPI(env,"getShipmentList", docGetShipList);
					env.clearApiTemplates();
					
					log.verbose("getShipmentList API output: "+ XMLUtil.getXMLString(docOutputGetShipList));
					strScac = ((Element) docOutputGetShipList.getDocumentElement().getElementsByTagName("Shipment").item(0)).getAttribute("SCAC");
					proNo = ((Element) docOutputGetShipList.getDocumentElement().getElementsByTagName("Shipment").item(0)).getAttribute("ProNo");

					log.verbose("**** ProNo ****" + proNo);
					log.verbose("**** SCAC ****" + strScac);
				
					if (!YFCObject.isVoid(strScac) && isWGCarrier(env,strScac))
					{
						//Start of Change For SFS2.0 WG ship from store requirement
			
	
						// change shipment
						Document docShipment = XMLUtil.createDocument("Shipment");
						Element eleShipment = docShipment.getDocumentElement();
						eleShipment.setAttribute("ShipmentKey", strShipmentKey);
						eleShipment.setAttribute("TrackingNo", proNo);
						Element eleContainers = docShipment.createElement("Containers");
						eleShipment.appendChild(eleContainers);
						Element eleCon = docShipment.createElement("Container");
						eleContainers.appendChild(eleCon);
						eleCon.setAttribute("TrackingNo", proNo);
						eleCon.setAttribute("ShipmentContainerKey", strShipmentContainerKey);	
						log.verbose("**** calling changeShipment API : Input - " + XMLUtil.getXMLString(docShipment));
						AcademyUtil.invokeAPI(env,	"changeShipment", docShipment);						
						
						
						//End of Change For SFS2.0 WG ship from store requirement					
	
					}
				}
			}
			catch (ParserConfigurationException e)
			{
				e.printStackTrace();
			}
			catch (SAXException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return inDoc;
	}
	
	private boolean isWGCarrier(YFSEnvironment env, String curScac) throws Exception{
		Document docWGSCACLst = getWhiteGloveScacList(env);			
		if(docWGSCACLst != null){
			Node nCommonCode = XPathUtil.getNode(docWGSCACLst, "CommonCodeList/CommonCode[@CodeValue='" + curScac + "']");
			if(nCommonCode != null)
				return true;	
		}
		return false;
	}

	private Document getWhiteGloveScacList(YFSEnvironment env) throws Exception{
		Document docWGSCACLst = null;
		try{
		Document docScacCodeInput = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		docScacCodeInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, AcademyConstants.STR_WG_SCAC_CODE);
		docScacCodeInput.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		env.setApiTemplate(AcademyConstants.API_GET_COMMON_CODELIST, "global/template/api/getCommonCodeList.IsWhiteGloveSCAC.xml");
		docWGSCACLst = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_COMMON_CODELIST,docScacCodeInput);
		env.clearApiTemplate(AcademyConstants.API_GET_COMMON_CODELIST);
		}catch(Exception e){
			e.printStackTrace();
			log.verbose("Failed to invoke getCommonCodeList API : "+e);
			throw e;
		}
		return docWGSCACLst;
	}

	public static void main(String[] args) throws Exception
	{
		Document doc = YFCDocument.getDocumentFor(new File("C://input.xml")).getDocument();
		Element docElement = doc.getDocumentElement();
		String strShipmentContainerKey = docElement.getAttribute("ShipmentContainerKey");
		String strScac = docElement.getAttribute("SCAC");
		Element shipment = (Element) XPathUtil.getNode(docElement, "/Container/Shipment");
		log.verbose(XMLUtil.getElementXMLString(shipment));
		String proNo = shipment.getAttribute("ProNo");
	}

}
