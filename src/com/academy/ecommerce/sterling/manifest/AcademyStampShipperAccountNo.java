package com.academy.ecommerce.sterling.manifest;

import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyStampShipperAccountNo implements YIFCustomApi  {
	  private Properties props;
	  
	  public void setProperties(Properties props) {
	        this.props = props;
	    }
	
	
	  /**
 * Instance of logger
 */
private static YFCLogCategory log = YFCLogCategory.instance(AcademyStampShipperAccountNo.class);

public Document stampShipperAccountNo(YFSEnvironment env, Document inDoc) throws Exception {
	log.beginTimer(" Begining of AcademyStampShipperAccountNo->stampShipperAccountNo Api");
	Element inElem = inDoc.getDocumentElement();
	String sPrinterType = "Response";
	Element PackageDetail = XMLUtil.getFirstElementByName(inElem, "PackageLevelDetail");
	if(!YFCObject.isVoid(PackageDetail)){
		PackageDetail.setAttribute("PBShipperReference", PackageDetail.getAttribute("ShipID"));
		String sShipperAccountNo = PackageDetail.getAttribute("ShipperAccountNumber");
		String sShipmentKey = PackageDetail.getAttribute("ShipmentKey");
		if(!YFCObject.isVoid(sShipmentKey)){
			//Call getTaskList API to check if any open outbound picking task is of type pack while pick.
		/*	//Input <Task>
			<TaskReferences ShipmentKey="20090819172153273044"/>
			<TaskType ActivityGroupID="OUTBOUND_PICKING" PackWhilePick="N"/>
			</Task>*/
			
			Document outTaskDoc = CallgetTaskList(env,sShipmentKey);
			
			if(!YFCObject.isVoid(outTaskDoc)){
				Element outTaskDocElem = outTaskDoc.getDocumentElement();
				String sTotalRecords = outTaskDocElem.getAttribute("TotalNumberOfRecords");
				
				//If total number of records = 0 it means shipment is of type conveyable and print required is client.
				if(sTotalRecords.equals("0")){
					sPrinterType = "Client";	
					sShipperAccountNo = "vipin_client";
					PackageDetail.setAttribute("ShipperAccountNumber",sShipperAccountNo);
				}
			}
			
		}
		
	}
	
	log.endTimer(" End of AcademyStampShipperAccountNo->stampShipperAccountNo Api");
	return inDoc;
	
}

private Document CallgetTaskList(YFSEnvironment env, String shipmentKey) throws Exception {
	log.beginTimer(" Begining of AcademyStampShipperAccountNo->CallgetTaskList Api");
	Document inDoc = XMLUtil.createDocument("Task");
	Element inElem = inDoc.getDocumentElement();
	Element TaskReferences = XMLUtil.createElement(inDoc, "TaskReferences",null);
	inElem.appendChild(TaskReferences);
	TaskReferences.setAttribute("ShipmentKey", shipmentKey);
	Element TaskType = XMLUtil.createElement(inDoc, "TaskType",null);
	inElem.appendChild(TaskType);
	TaskType.setAttribute("ActivityGroupID", "OUTBOUND_PICKING");
	TaskType.setAttribute("PackWhilePick", "Y");
	Document outTaskDoc = AcademyUtil.invokeService(env, "AcademyGetTaskList", inDoc);
	log.endTimer(" Ending of AcademyStampShipperAccountNo->CallgetTaskList Api");
	return outTaskDoc;
}
}
