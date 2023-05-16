package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;

import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

/* Sample Input XML to this class
 * <Container ActualFreightCharge="0.00" AppliedWeight="0.00"
    AppliedWeightUOM="" BasicFreightCharge="0.00"
    ContainerGrossWeight="2.15" ContainerGrossWeightUOM="LBS"
    ContainerHeight="3.00" ContainerHeightUOM="IN"
    ContainerLength="10.00" ContainerLengthUOM="IN"
    ContainerNetWeight="2.00" ContainerNetWeightUOM="LBS"
    ContainerNo="100004580" ContainerScm="00000000001000041802"
    ContainerSeqNo="0" ContainerType="Case" ContainerWidth="7.00"
    ContainerWidthUOM="IN" CustomsValue="0.00" DeclaredValue="0.00"
    DimmedFlag="" DiscountAmount="0.00" HasOtherContainers="N"
    IsHazmat="N" OldParentContainerNo="" OversizedFlag=""
    ParentContainerGroup="" ParentContainerNo="" RoutingCode=""
    ShipmentContainerKey="2012010202505020419079"
    ShipmentKey="2012010201201820417944" SpecialServicesSurcharge="0.00"
    SystemSuggested="Y" TrackingNo="" Ucc128code="" Zone="" isHistory="N">
    <SpecialServices TotalNumberOfRecords="0"/>
    <AdditionalAttributes TotalNumberOfRecords="0"/>
    <ContainerDetails TotalNumberOfRecords="1">
        <ContainerDetail ContainerDetailsKey="2012010202505020419082"
            ItemID="0002753267" OldQuantity="0.0" ProductClass="GOOD"
            Quantity="1.00"
            ShipmentContainerKey="2012010202505020419079"
            ShipmentKey="2012010201201820417944" UnitOfMeasure="EACH" isHistory="N">
            <ShipmentTagSerials TotalNumberOfRecords="0"/>
        </ContainerDetail>
    </ContainerDetails>
    <Instructions TotalNumberOfRecords="0"/>
    <ChildContainers TotalNumberOfRecords="0"/>
    <Notes/>
    <PackLocation LocationId="PACKLOC" Node="005" StationId="PACK10" StationType="PACK&amp;AUDIT"/>
</Container>
 * */

public class AcademyUpdateTaskContainerRefOnShipmentPack implements YIFCustomApi {

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyUpdateTaskContainerRefOnShipmentPack.class);

	private void callGetTaskList(YFSEnvironment env,
			Document getTaskListInputXML) throws YFSUserExitException {
		try{
		Document docTaskListOutput = AcademyUtil.invokeAPI(env, "getTaskList", getTaskListInputXML);
		if(log.isVerboseEnabled()){
		log.verbose("Output of getTaskList: " +XMLUtil.getXMLString(docTaskListOutput));
		}
		
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
					if(log.isVerboseEnabled()){
					log.verbose("Input to changeTask API :" +XMLUtil.getXMLString(docChangeTaskInput));
					}
					AcademyUtil.invokeAPI(env, "changeTask", docChangeTaskInput);
				}
			}
		}
		}catch(Exception e)
		{e.printStackTrace();}
	}

	public Document updateContainerNoReferenceOnPackComplete(YFSEnvironment env,
			Document inXML) throws Exception {
		
		log.beginTimer(" Begining of AcademyUpdateTaskContainerRefOnShipmentPack-> updateContainerNoReferenceOnPackCompletion Api");
		if(log.isVerboseEnabled()){
		log.verbose("Input to updateContainerNoReferenceOnPackCompletion : "+XMLUtil.getXMLString(inXML));
		}
		Element rootEl = inXML.getDocumentElement();
		String shipmentKey = rootEl.getAttribute("ShipmentKey");
		
		if(!YFCObject.isVoid(shipmentKey)){
					try{
					Document docTaskListInput = XMLUtil.createDocument("Task");
					Element eleTaskRef = docTaskListInput.createElement("TaskReferences");
					docTaskListInput.getDocumentElement().appendChild(eleTaskRef);
	
					eleTaskRef.setAttribute("ContainerNo", "");
					eleTaskRef.setAttribute("ContainerNoQryType", "VOID");
					eleTaskRef.setAttribute("ShipmentKey", shipmentKey);
					if(log.isVerboseEnabled()){
					log.verbose("Input to getTaskList with ShipmentKey: "+XMLUtil.getXMLString(docTaskListInput));
					}
					callGetTaskList(env, docTaskListInput);					
					}catch (Exception e)
					{e.printStackTrace();}
		}
		log.endTimer(" End of AcademyUpdateTaskContainerRefOnShipmentPack-> updateContainerNoReferenceOnPackCompletion Api");

		return inXML;
	}

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
