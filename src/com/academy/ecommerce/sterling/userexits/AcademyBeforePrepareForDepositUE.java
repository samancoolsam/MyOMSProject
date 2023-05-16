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

public class AcademyBeforePrepareForDepositUE implements WMSBeforePrepareForDepositUE {

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
			String strAssignedUser = env.getUserId();
			String strBatchNo = eleTaskRef.getAttribute("BatchNo");
			
			log.verbose("Task Key is " +strtaskKey);
			log.verbose("Task Type is " +strtaskType);
			log.verbose("Shipment Key is " +strShipmentKey);
			log.verbose("Batch No is " + strBatchNo);
			log.verbose("Current User Id is " + strAssignedUser);

			try {
				log.verbose("Inside Try block");
							
				if("STO-Pick".equals(strtaskType))
				{
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
				}
				else if ("CBP".equals(strtaskType))
				{
					Document docTask=XMLUtil.createDocument("Task");
					docTask.getDocumentElement().setAttribute("AssignedToUserId", strAssignedUser);
					docTask.getDocumentElement().setAttribute("TaskStatus","1200");
					Element eleTask2=docTask.createElement("TaskReferences");
					docTask.getDocumentElement().appendChild(eleTask2);
					eleTask2.setAttribute("BatchNo", strBatchNo);
					
					log.verbose("InDoc is " +XMLUtil.getXMLString(docTask));
					Document outTempGetTaskList = YFCDocument
							.parse(
									"<TaskList TotalNumberOfRecords=\"\"><Task TaskKey=\"\"></Task></TaskList>")
							.getDocument();
					env.setApiTemplate("getTaskList", outTempGetTaskList);
					
					log.verbose("Template is " +XMLUtil.getXMLString(outTempGetTaskList));

					Document outGetTaskListDoc = AcademyUtil.invokeAPI(env,
							"getTaskList", docTask);
					env.clearApiTemplates();

					log.verbose("****************** getTaskList output Document :::::: "
							+ XMLUtil.getXMLString(outGetTaskListDoc));
					String strTaskRecords = outGetTaskListDoc.getDocumentElement().getAttribute("TotalNumberOfRecords");
                    Integer iTaskRecords = Integer.valueOf(Integer.parseInt(strTaskRecords));

					
					if (iTaskRecords > 0)
					{
						log.verbose("Batch has " +iTaskRecords+ " assigned tasks to the user " + strAssignedUser);
						/*log.verbose("Passing input to rejectTasks API to usassign Tasks from current user");
						Document docRejectTasks = XMLUtil.createDocument("TaskList");
						for(int i=0; i < iTaskRecords; i++)
						{
						Element outTask = (Element)outGetTaskListDoc.getDocumentElement().getElementsByTagName("Task").item(i);
						String strOutTaskKey=outTask.getAttribute("TaskKey");
						
						Element eleTask1=docRejectTasks.createElement("Task");
						docRejectTasks.getDocumentElement().appendChild(eleTask1);
						eleTask1.setAttribute("TaskKey", strOutTaskKey);
						}*/
						
						log.verbose("Invoking rejectTasks API with output of getTaskList " +XMLUtil.getXMLString(outGetTaskListDoc));
						Document docOutRejectTasks=AcademyUtil.invokeAPI(env, "rejectTasks", outGetTaskListDoc);
						log.verbose("Output of Reject Tasks API is "+ docOutRejectTasks);
						
					}	
					else
					{
					    log.verbose("Batch has no suggested/assigned tasks for user "+strAssignedUser);
					}
					
				}
				
				log
				.beginTimer(" End of AcademyBeforePrepareForDepositUE-> beforePrepareForDeposit Api");	
					
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

	