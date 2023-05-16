package com.academy.ecommerce.sterling.bopis.sfspacking.api;

import java.util.HashSet;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySFSBeforeCreateContainersAndPrintService implements YIFCustomApi{
	
	private Properties props;

	@Override
	public void setProperties(Properties props) throws Exception {
		this.props = props;
		
	}
	
	private static YFCLogCategory log = 
			YFCLogCategory.instance(AcademySFSBeforeCreateContainersAndPrintService.class);
	
	public Document beforeCreateContainersAndPrintService(YFSEnvironment env, Document inDoc) throws Exception {		
		log.beginTimer("Begining of AcademySFSBeforeCreateContainersAndPrintService-> beforeCreateContainersAndPrintService Api");
		if(log.isVerboseEnabled() ) {
			log.verbose("Input to beforeCreateContainersAndPrintService : "+XMLUtil.getXMLString(inDoc));
		}
		
		Element inDocEle = inDoc.getDocumentElement();
		env.setTxnObject("IsWebstoreFlow", "Y");

		Document getShipmentListInDoc = XMLUtil.createDocument("Shipment");
		String strShipmentKey = inDocEle.getAttribute("ShipmentKey");
		getShipmentListInDoc.getDocumentElement().setAttribute("ShipmentKey", strShipmentKey);

		env.setApiTemplate("getShipmentList", "global/template/api/getShipmentList_B4SFSPackingFlow.xml");
		Document outShipmentListDoc = AcademyUtil.invokeAPI(env, "getShipmentList", getShipmentListInDoc);
		env.clearApiTemplate("getShipmentList");

		Document shipmentDoc = prepareOutputDoc(outShipmentListDoc, inDocEle);
		log.beginTimer(
				"End of AcademySFSBeforeCreateContainersAndPrintService-> beforeCreateContainersAndPrintService Api");
		if(log.isVerboseEnabled() ) {
			log.verbose("Output to beforeCreateContainersAndPrintService : " + XMLUtil.getXMLString(shipmentDoc));
		}
		return shipmentDoc;	
	}

	private Document prepareOutputDoc(Document outShipmentListDoc, Element inDocEle) throws Exception {
		
//		Fetch the attributes from the inputEle to be stamped at the shipment level of the outputDoc
		String strContainerType = inDocEle.getAttribute("ContainerType");
		String strContainerTypeKey = inDocEle.getAttribute("ContainerTypeKey");
		String strDisplayLocalizedFieldInLocale = inDocEle.getAttribute("DisplayLocalizedFieldInLocale");
		String strContainerGrossWeight = inDocEle.getAttribute("ContainerGrossWeight");
		String strOrderNo = inDocEle.getAttribute("OrderNo");
//		Fetch the attributes from the Service Value Argument to be set at the shipment level of the outputDoc
		String strPrintPackId = props.getProperty("PrintPackId");
		
		Element outShipmentListDocShpmntEle = outShipmentListDoc.getDocumentElement();
		Element shipmentEle = XMLUtil.getElementByXPath(outShipmentListDoc, "Shipments/Shipment");
		Document outPutDoc = XMLUtil.getDocumentForElement(shipmentEle);
		Element outPutEle = outPutDoc.getDocumentElement();
		
//		Update the shipment header of the output document
		outPutEle.setAttribute("ContainerType", strContainerType);
		if(strContainerType.equals(AcademyConstants.STR_VENDOR_PACKAGE)) {
			strContainerGrossWeight="1.00";
		}
		outPutEle.setAttribute("OrderNo", strOrderNo);
		outPutEle.setAttribute("ContainerGrossWeight", strContainerGrossWeight);
		
		if(!YFCObject.isVoid(strContainerTypeKey)){
		outPutEle.setAttribute("ContainerTypeKey", strContainerTypeKey);
		}
		outPutEle.setAttribute("DisplayLocalizedFieldInLocale", strDisplayLocalizedFieldInLocale);
		outPutEle.setAttribute("PrintPackId", strPrintPackId);
		outPutEle.setAttribute("IsCompletePick", AcademyConstants.STR_YES);
		outPutEle.setAttribute("BackroomPickRequired", AcademyConstants.STR_YES);
		
//		Update the shipment line level details of the output document
		String strContainerSCM = inDocEle.getAttribute("ContainerScm");
		
		if (!YFCCommon.isVoid(strContainerSCM)) {
			outPutEle.setAttribute("ContainerScm", strContainerSCM);
			updateShipmentLine(strContainerSCM, outPutDoc);
		}		
		return outPutDoc;
	}

	private void updateShipmentLine(String strContainerSCM, Document outPutDoc) throws Exception {
		
//		Updating the Shipment/ShipmentLine Level attributes of the output document
		Element containerEle = XMLUtil.getElementByXPath(outPutDoc, 
				"Shipment/Containers/Container[@ContainerScm='"+strContainerSCM.trim()+"']");
		if(YFCCommon.isVoid(containerEle)) {
			return ;
		}
		
		String strshipmentContainerKey = containerEle.getAttribute("ShipmentContainerKey");
		outPutDoc.getDocumentElement().setAttribute("ShipmentContainerKey", strshipmentContainerKey);
		
		NodeList containerDetailNL = XMLUtil.getNodeList(containerEle, "ContainerDetails/ContainerDetail");
		int containerDetailsLen = containerDetailNL.getLength();
		
		HashSet<String> shipmentLineKeySet = new HashSet();
		
		for(int i=0; i<containerDetailsLen; i++) {
			Element containerDetailEle = (Element) containerDetailNL.item(i);
			String strShipmentLineKey = containerDetailEle.getAttribute("ShipmentLineKey");
			shipmentLineKeySet.add(strShipmentLineKey);
			String strQuantity = containerDetailEle.getAttribute("Quantity");
			String strUOM = containerDetailEle.getAttribute("UnitOfMeasure");
			
			Element shipmentLineEle = XMLUtil.getElementByXPath(outPutDoc,
					"Shipment/ShipmentLines/ShipmentLine[@ShipmentLineKey='" + strShipmentLineKey + "']");
			if (!YFCCommon.isVoid(shipmentLineEle)) {
				shipmentLineEle.setAttribute("PickQuantity", strQuantity);
				shipmentLineEle.setAttribute("PickQtyUOM", strUOM);
				updateItemDetailsOfShipmentLine(outPutDoc, shipmentLineEle);
			}
			
		}
		System.out.println();
		
		NodeList shipmentLineNL = XMLUtil.getNodeListByXPath(outPutDoc, "Shipment/ShipmentLines/ShipmentLine");
		int shipmentLineNLSize = shipmentLineNL.getLength();
		for(int i=0; i<shipmentLineNLSize; i++) {
			Element newShipmentLineEle = (Element)shipmentLineNL.item(i);
			String shipmentLineKey = newShipmentLineEle.getAttribute("ShipmentLineKey");
			if(!shipmentLineKeySet.contains(shipmentLineKey)) {
				Node shipmentLinesEle = newShipmentLineEle.getParentNode();
				shipmentLinesEle.removeChild(newShipmentLineEle);
			}
		}
		
		
//		Remove the Containers Element from the output document
//		as the element is not required in the next API component of the service
		Node containersEle = containerEle.getParentNode();
		Node containersEleparent = containersEle.getParentNode();
		containersEleparent.removeChild(containersEle);
		
	}

	private void updateItemDetailsOfShipmentLine(Document outputDoc, Element shipmentLineEle) {
		// TODO Auto-generated method stub
		Element itemEle = (Element) XMLUtil.getElementsByTagName(shipmentLineEle,"Item").get(0);
		Element primaryInfomEle = (Element) XMLUtil.getElementsByTagName(shipmentLineEle,"PrimaryInformation").get(0);
		Element itemDetailsExtnEle = (Element) ((Element) primaryInfomEle.getParentNode()).getElementsByTagName("Extn").item(0);
		Element itemPrimaryInfoEle = XMLUtil.createElement(outputDoc, "PrimaryInformation", true);
		Element itemExtn = XMLUtil.createElement(outputDoc, "Extn", true);
		XMLUtil.copyElement(outputDoc, itemDetailsExtnEle, itemExtn);
		XMLUtil.copyElement(outputDoc,primaryInfomEle, itemPrimaryInfoEle);
		itemEle.appendChild(itemExtn);
		itemEle.appendChild(itemPrimaryInfoEle);		
	}

}
