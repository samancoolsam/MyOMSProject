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
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;


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

public class AcademyCheckIsContainerManifested implements YCPDynamicCondition{
	
    
    /*
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyCheckIsContainerManifested.class);
	private boolean b=false;

    
    /* to check if variance value is greater than 50 on second count, if yes then 
     * return true to condition component in AcademyRaiseAlertForAcceptVariance
     * */
	public boolean evaluateCondition(YFSEnvironment env, String sName,
			Map mapData, String sXMLData) {
		log.beginTimer(" Begining of AcademyCheckIsContainerManifested-> evaluateCondition Api");
		log.verbose("Input Xml to evaluateCondition API is" + sXMLData);

		YFCDocument inXml = YFCDocument.getDocumentFor(sXMLData);
		if (!YFCObject.isVoid(inXml))
		{
			YFCElement eleContainer = inXml.getDocumentElement();
			String strShipContrKey = eleContainer.getAttribute("ShipmentContainerKey");
			
			//Prepare input document for AcademyGetShipmentContainerListForSTO service to get the manifest for the container
			Document inDoc=null;
			try
			{
				inDoc = XMLUtil.createDocument("Container");
				Element eleInContainer = inDoc.getDocumentElement();
				eleInContainer.setAttribute("ShipmentContainerKey", strShipContrKey);
				
				log.verbose("Input xml to AcademyGetShipmentListForSTO is " + XMLUtil.getXMLString(inDoc));
				
				Document outDoc = AcademyUtil.invokeService(env, "AcademyGetShipmentContainerListForSTO", inDoc);
				log.verbose("Output xml of AcademyGetShipmentContainerListForSTO is " + XMLUtil.getXMLString(outDoc));
				
				Element eleContainers = outDoc.getDocumentElement();
				Element eleOutContainer = (Element) eleContainers.getElementsByTagName("Container").item(0);
				
				String strManifestNo = eleOutContainer.getAttribute("ManifestNo");
				String strSCAC = eleOutContainer.getAttribute("SCAC");
				if ("STO-SCAC".equals(strSCAC))
				{
					log.verbose("Container is STO Container");
					if (!YFCObject.isVoid(strManifestNo)|| !StringUtil.isEmpty(strManifestNo))
					{
						b=true;
						log.verbose("Container is added to Manifest");
						return b;
					}
					
				}
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			

		}
		log.verbose("Container is not added to Manifest");
		log.endTimer("End of AcademyCheckIsContainerManifested-> evaluateCondition Api");
		return b;
		
		
		
	}
}
