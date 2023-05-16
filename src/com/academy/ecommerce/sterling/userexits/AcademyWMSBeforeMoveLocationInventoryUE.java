package com.academy.ecommerce.sterling.userexits;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.wms.japi.ue.WMSBeforeMoveLocationInventoryUE;

import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

/* Sample Input XML to this class
 * <MoveLocationInventory EnterpriseCode="Academy_Direct"
    IgnoreOrdering="Y" Node="005">
    <Source CaseId="000000000011000TEST2" PalletId="">
        <Inventory CountryOfOrigin="" FifoNo="201009"
            InventoryStatus="GOOD" InventoryTagKey="" Quantity="3.00"
            ReceiptHeaderKey="" ReceiptNo="" Segment="" SegmentType="" ShipByDate="">
            <InventoryItem ItemID="0017557521" ProductClass="GOOD" UnitOfMeasure="EACH"/>
        </Inventory>
    </Source>
    <Destination LocationId="PACKLOC"/>
    <Audit ReasonCode="BREAK-LPN" ReasonText=""/>
</MoveLocationInventory>
 * */

public class AcademyWMSBeforeMoveLocationInventoryUE implements WMSBeforeMoveLocationInventoryUE {

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyWMSBeforeMoveLocationInventoryUE.class);

	private void callGetTaskList(YFSEnvironment env,
			Document getTaskListInputXML) throws YFSUserExitException {
		try{
		Document docTaskListOutput = AcademyUtil.invokeAPI(env, "getTaskList", getTaskListInputXML);
		log.verbose("Output of getTaskList: " +XMLUtil.getXMLString(docTaskListOutput));
		
		Document docChangeTaskInput = null;
		Element eleChangeTaskReference = null;
		docChangeTaskInput = XMLUtil.createDocument("Task");
		eleChangeTaskReference = docChangeTaskInput.createElement("TaskReferences");
		docChangeTaskInput.getDocumentElement().appendChild(eleChangeTaskReference);
		
		Element eleTaskList = docTaskListOutput.getDocumentElement();
		if(!YFCObject.isVoid(eleTaskList)){
			NodeList nListTask = XMLUtil.getNodeList(eleTaskList, "Task");
			for(int i=0;i<nListTask.getLength();i++){
				Element eleTask = (Element) nListTask.item(i);
				String strTaskKey = eleTask.getAttribute("TaskKey");
				Element eleTaskRef = (Element) eleTask.getElementsByTagName("TaskReferences").item(0);
				String strContainerNo = eleTaskRef.getAttribute("ContainerNo");
				log.verbose("Container No : "+strContainerNo);
				if(YFCObject.isVoid(strContainerNo)){
					docChangeTaskInput.getDocumentElement().setAttribute("TaskKey", strTaskKey);
					docChangeTaskInput.getDocumentElement().setAttribute("InternalApp", "Y");
					eleChangeTaskReference.setAttribute("ContainerNo", "DUMMYCONTAINER");
					
					log.verbose("Input to changeTask API :" +XMLUtil.getXMLString(docChangeTaskInput));
					AcademyUtil.invokeAPI(env, "changeTask", docChangeTaskInput);
				}
			}
		}
		}catch(Exception e)
		{e.printStackTrace();}
	}

	public Document beforeMoveLocationInventory(YFSEnvironment env,
			Document inXML) throws YFSUserExitException {
		
		log.beginTimer(" Begining of AcademyWMSBeforeMoveLocationInventoryUE-> beforeRegisterTaskCompletion Api");
		log.verbose("Input to beforeMoveLocationInventory : "+XMLUtil.getXMLString(inXML));
		Element moveLocEle = inXML.getDocumentElement();
		String strTote = ((Element)moveLocEle.getElementsByTagName("Source").item(0)).getAttribute("CaseId");
		
		if(!YFCObject.isVoid(strTote)){
			Element eleMoveLoc = (Element)moveLocEle.getElementsByTagName("Audit").item(0);
			if(!YFCObject.isVoid(eleMoveLoc)){
				String strReasonCode = eleMoveLoc.getAttribute("ReasonCode");
				log.verbose("Reason Code : "+strReasonCode);
				if(strReasonCode.equals("BREAK-LPN")){
					log.verbose("Reason Code validation successful");
					try{
					Document docTaskListInput = XMLUtil.createDocument("Task");
					Element eleInventory = docTaskListInput.createElement("Inventory");
					Element eleTaskRef = docTaskListInput.createElement("TaskReferences");
					docTaskListInput.getDocumentElement().appendChild(eleInventory);
					docTaskListInput.getDocumentElement().appendChild(eleTaskRef);
	
					eleInventory.setAttribute("SourceCaseId", strTote);
					eleTaskRef.setAttribute("ContainerNo", "");
					
					log.verbose("Input to getTaskList with SourceCaseId: "+XMLUtil.getXMLString(docTaskListInput));
					callGetTaskList(env, docTaskListInput);
					
					docTaskListInput.getDocumentElement().setAttribute("TargetCaseId", strTote);
					docTaskListInput.getDocumentElement().removeChild(eleInventory);
					
					log.verbose("Input to getTaskList with TargetCaseId: "+XMLUtil.getXMLString(docTaskListInput));
					callGetTaskList(env, docTaskListInput);
					}catch (Exception e)
					{e.printStackTrace();}
				}
			}
		}
		log.endTimer(" End of AcademyWMSBeforeMoveLocationInventoryUE-> beforeRegisterTaskCompletion Api");

		return inXML;
	}

}
