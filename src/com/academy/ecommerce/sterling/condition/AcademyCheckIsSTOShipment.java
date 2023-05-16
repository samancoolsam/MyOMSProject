package com.academy.ecommerce.sterling.condition;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.yantra.ycp.japi.YCPDynamicCondition;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

import com.academy.util.xml.XMLUtil;
import com.academy.util.common.AcademyUtil;;


/*
 * File used in Condition "IsSTOShipment"
 * Dynamic Condition for STO-Enhancement. On_Success of pack verify, this condition will check if the container being pack-verified belongs to a sales shipment
 * or STO Shipment by checking for SCAC. SCAC for STO-Shipments will be STO-SCAC
 * 
 * Input xml to this condition is on_success xml of PACK VERIFY :
 * 
 * <?xml version="1.0" encoding="UTF-8"?>
 * <Container ContainerLocation="AUBLOC"
 *  ContainerScm="00000000001000011539" ContainerType="Case"  PipelineKey="CONTAINER-PACK-80"
 *  ShipmentContainerKey="201011112324213392439" Status="1300" value="ShipmentContainerDetails"/>
 * 
 * * author @muchil
 */

public class AcademyCheckIsSTOShipment implements YCPDynamicCondition{
	
    
    /*
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyCheckIsSTOShipment.class);
	private boolean b=false;

    
    /* to check if variance value is greater than 50 on second count, if yes then 
     * return true to condition component in AcademyRaiseAlertForAcceptVariance
     * */
	public boolean evaluateCondition(YFSEnvironment env, String sName,
			Map mapData, String sXMLData) {
		log.beginTimer(" Begining of AcademyCheckIsSTOShipment-> evaluateCondition Api");
		log.verbose("Input Xml to evaluateCondition API is" + sXMLData);

		YFCDocument inXml = YFCDocument.getDocumentFor(sXMLData);
		if (!YFCObject.isVoid(inXml))
		{
			YFCElement eleContainer = inXml.getDocumentElement();
			String strShipContrKey = eleContainer.getAttribute("ShipmentContainerKey");
			
			//Prepare input document for AcademyGetShipmentListForSTO service to get the SCAC of the shipment
			Document inDoc=null;
			try
			{
				inDoc = XMLUtil.createDocument("Shipment");
				Element eleShipment = inDoc.getDocumentElement();
				Element eleContainers = inDoc.createElement("Containers");
				eleShipment.appendChild(eleContainers);
				Element eleCont = inDoc.createElement("Container");
				eleContainers.appendChild(eleCont);
				eleCont.setAttribute("ShipmentContainerKey", strShipContrKey);
				
				log.verbose("Input xml to AcademyGetShipmentListForSTO is " + XMLUtil.getXMLString(inDoc));
				
				Document outDoc = AcademyUtil.invokeService(env, "AcademyGetShipmentListForSTO", inDoc);
				log.verbose("Output xml of AcademyGetShipmentListForSTO is " + XMLUtil.getXMLString(outDoc));
				
				Element eleShipments = outDoc.getDocumentElement();
				Element eleOutShipment = (Element) eleShipments.getElementsByTagName("Shipment").item(0);
				
				String strSCAC = eleOutShipment.getAttribute("SCAC");
				
				if ("STO-SCAC".equals(strSCAC))
				{
					b=true;
					log.verbose("SCAC is STO-SCAC");
					return b;
				}

			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException("Failed dynamic Condition  in AcademyCheckIsSTOShipment : "+e);
			}
			
			
			

		}
		log.verbose("SCAC is not STO-SCAC");
		log.endTimer("End of AcademyCheckIsSTOShipment-> evaluateCondition Api");
		return b;
	}
}
