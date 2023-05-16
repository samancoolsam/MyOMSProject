package com.academy.ecommerce.sterling.userexits;


import org.w3c.dom.Document;
import org.w3c.dom.Element;


import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.xml.XMLUtil;

import com.yantra.wms.japi.ue.WMSBeforePrepareForDepositUE;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;

import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.YFSUserExitException;

/* Sample Input XML to this class
 * getTaskDetails output
 */

public class AcademyBeforePrepareForDepositUEBackup implements WMSBeforePrepareForDepositUE {

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyBeforePrepareForDepositUE.class);

	public Document beforePrepareForDeposit(YFSEnvironment env,
			Document inputXML) throws YFSUserExitException {

		YFSException e = new YFSException();
		log
		.beginTimer(" Begining of AcademyBeforePrepareForDepositUE-> beforePrepareForDeposit Api");
		log.verbose("****************** Input Document :::::"
				+ XMLUtil.getXMLString(inputXML));
		
		// get data from input XML
		Element eleTask =inputXML.getDocumentElement();
				
		if (!YFCObject.isNull(eleTask)) {
			String strtaskKey =eleTask.getAttribute("TaskKey");
			Element eleTaskRef = (Element) eleTask.getElementsByTagName("TaskReferences").item(0);
			String strShipmentKey = eleTaskRef.getAttribute("ShipmentKey");
			String strtaskType = eleTask.getAttribute("TaskType");
			
			log.verbose("Task Key is" +strtaskKey);
			log.verbose("Task Type is" +strtaskType);
			log.verbose("Shipment Key is" +strShipmentKey);

			try {
				log.verbose("Inside Try block");
				/*Document inTask=XMLUtil.createDocument("Task");
				inTask.getDocumentElement().setAttribute("TaskKey", strtaskKey);
				log.verbose("InDoc is " +XMLUtil.getXMLString(inTask));
				Document outTempGetTaskList = YFCDocument
						.parse(
								"<TaskList><Task TaskKey=\"\" TaskType=\"\"><TaskReferences ShipmentKey=\"\" /></Task></TaskList>")
						.getDocument();
				env.setApiTemplate("getTaskList", outTempGetTaskList);
				
				log.verbose("Template is " +XMLUtil.getXMLString(outTempGetTaskList));

				Document outGetTaskListDoc = AcademyUtil.invokeAPI(env,
						"getTaskList", inTask);
				env.clearApiTemplates();

				log.verbose("****************** getTaskList output Document :::::: "
						+ XMLUtil.getXMLString(outGetTaskListDoc));
				String strtaskType = ((Element) outGetTaskListDoc.getDocumentElement().getElementsByTagName("Task").item(0)).getAttribute("TaskType");*/
				
				if("STO-Pick".equals(strtaskType))
				{
					//Element eleOutTask= (Element)outGetTaskListDoc.getDocumentElement().getElementsByTagName("Task").item(0);
					//Element eleTaskRef = (Element) eleOutTask.getElementsByTagName("TaskReferences").item(0);
					//String strShipmentKey= eleTaskRef.getAttribute("ShipmentKey");
					
					Document indocShip = XMLUtil.createDocument("Shipment");
					indocShip.getDocumentElement().setAttribute("ShipmentKey", strShipmentKey);
					
					Document outTempGetShipmentList = YFCDocument
					.parse(
							"<Shipments><Shipment ShipmentKey=\"\" ShipmentSortLocationId=\"\"/></Shipments>")
					.getDocument();
					env.setApiTemplate("getShipmentList", outTempGetShipmentList);
										
					Document outShipList = AcademyUtil.invokeAPI(env, "getShipmentList", indocShip);
					env.clearApiTemplates();
					
					Element outShipment= (Element)outShipList.getDocumentElement().getElementsByTagName("Shipment").item(0);
					String strShipSortLoc = outShipment.getAttribute("ShipmentSortLocationId");
					
					if ("STO-PACKLOC-MEZZ".equals(strShipSortLoc))
					{
						//String pack = props.getProperty("PACK-MEZZ");
						inputXML.getDocumentElement().setAttribute("TargetLocationId", strShipSortLoc);
						//inputXML.getDocumentElement().setAttribute("TargetLocationId","STO-PACKLOC-MEZZ" );
					}
					else
					if ("STO-PACKLOC-BULK".equals(strShipSortLoc))
					{
						//String pack = props.getProperty("PACK-BULK");
						inputXML.getDocumentElement().setAttribute("TargetLocationId", strShipSortLoc);
						//inputXML.getDocumentElement().setAttribute("TargetLocationId","STO-PACKLOC-BULK" );
					}		
					
					log
					.beginTimer(" End of AcademyBeforePrepareForDepositUE-> beforePrepareForDeposit Api");	
				}
					
		} catch (Exception ye) {
			if (!StringUtil.isEmpty(e.getErrorCode())) {
				throw e;
			} else {
				YFSUserExitException ex = new YFSUserExitException();
				throw ex;
			}
		} 
				}
		return inputXML;
		
	}
}	

	