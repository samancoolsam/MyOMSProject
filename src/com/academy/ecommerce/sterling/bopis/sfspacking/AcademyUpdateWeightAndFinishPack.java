package com.academy.ecommerce.sterling.bopis.sfspacking;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
/**
 * BOPIS-1576-This Class is used as a wrapper to call update weight and finish pack process service.
 * @author rastaj1
 * Sample input from UI for this class is 
 * <Shipment 
SCAC="" ShipmentKey="" OrderNo="">
 <Containers>
  <Container ActualWeight="" ActualWeightUOM=""
  ShipmentContainerKey="" ContainerGrossWeight="" ContainerScm="" ContainerType="" ContainerTypeKey=""
   />

  <Container ActualWeight="" ActualWeightUOM=""
  ShipmentContainerKey="" ContainerGrossWeight="" ContainerScm="" ContainerType="" 
  ContainerTypeKey=""
   /> 
 </Containers>
</Shipment>
 *
 */
public class AcademyUpdateWeightAndFinishPack {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyUpdateWeightAndFinishPack.class);
	/**
	 * 
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document updateWeightAndFinishPack(YFSEnvironment env, Document inDoc) throws Exception{
		log.beginTimer("AcademyUpdateWeightAndFinishPack::updateWeightAndFinishPack");
		Document updateWeightInDoc = formInputForUpdateWeight(inDoc);

		AcademyUtil.invokeService(env, AcademyConstants.ACAD_UPDATE_CONTAINER_WEIGHT_SERVICE, updateWeightInDoc);
		log.verbose("Update Weight Successfull! Now Calling Finish Pack Process");
		Document processFinishPackDoc = formInputForProcessFinishPack(inDoc);

		Document outDoc = AcademyUtil.invokeService(env, AcademyConstants.ACAD_PROCESS_FINISH_PACK_SERVICE, processFinishPackDoc );
		log.verbose("Finish Process Pack Service successful");
		log.endTimer("AcademyUpdateWeightAndFinishPack::updateWeightAndFinishPack");
		return outDoc;

	}
	/**
	 * This method creates input for AcademyUpdateContainerWeight service in the below format.
	 * Sample input
	 * <Shipment
			
		SCAC="" ShipmentKey="">
		<Containers>
			<Container ActualWeight="" ActualWeightUOM=""
					ShipmentContainerKey=""/> 
		</Containers>
	</Shipment>
	 * @param inDoc
	 * @return
	 */
	private Document formInputForUpdateWeight(Document inDoc) {
		log.beginTimer("AcademyUpdateWeightAndFinishPack::formInputForUpdateWeight");
		Document outDoc = SCXmlUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleShipment = inDoc.getDocumentElement();
		String shipmentKey= eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		String scac = eleShipment.getAttribute(AcademyConstants.ATTR_SCAC);
		Element eleShipmentOut = outDoc.getDocumentElement();
		eleShipmentOut.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, shipmentKey);
		eleShipmentOut.setAttribute(AcademyConstants.ATTR_SCAC, scac);
		Element eleContainers = SCXmlUtil.createChild(eleShipmentOut, AcademyConstants.ELE_CONTAINERS);
		NodeList containerNL = eleShipment.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
		log.verbose("No of Containers in the Shipment:: "+ containerNL.getLength());
		for(int contCount=0;contCount<containerNL.getLength();contCount++){
			Element eleContainer = (Element)containerNL.item(contCount);
			String shipContainerKey= eleContainer.getAttribute(AcademyConstants.ATTR_SHIP_CONT_KEY);
			String actualWeight = eleContainer.getAttribute(AcademyConstants.ATTR_ACTUAL_WEIGHT);
			String actualWeightUOM = eleContainer.getAttribute(AcademyConstants.ATTR_ACTUAL_WEIGHT_UOM);

			Element eleContainerOut = SCXmlUtil.createChild(eleContainers, AcademyConstants.ELE_CONTAINER);
			eleContainerOut.setAttribute(AcademyConstants.ATTR_SHIP_CONT_KEY, shipContainerKey);
			eleContainerOut.setAttribute(AcademyConstants.ATTR_ACTUAL_WEIGHT, actualWeight);
			eleContainerOut.setAttribute(AcademyConstants.ATTR_ACTUAL_WEIGHT_UOM, actualWeightUOM);
		}
		log.verbose("Input Created for AcademyUpdateContainerWeight Service is:: "+ SCXmlUtil.getString(outDoc));
		log.endTimer("AcademyUpdateWeightAndFinishPack::formInputForUpdateWeight");
		return outDoc;
	}
	/**
	 * This Method Creates input for AcademySFSProcessFinishPackService in the below format.
	 * @param inDoc
	 * @return
	 * Sample Input
	 * <Shipments>
		<Shipment ContainerGrossWeight="" ContainerScm="" ContainerType="" ContainerTypeKey=""
			IsWebStoreFlow="Y" 
			ShipmentContainerKey="" ShipmentKey="" OrderNo=""/>	
	  </Shipments>
	 */
	private Document formInputForProcessFinishPack(Document inDoc) {
		log.beginTimer("AcademyUpdateWeightAndFinishPack::formInputForProcessFinishPack");

		Element eleShipment = inDoc.getDocumentElement();
		String shipmentKey= eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		String orderNo= eleShipment.getAttribute(AcademyConstants.ATTR_ORDER_NO);

		Document outDoc = SCXmlUtil.createDocument(AcademyConstants.ELE_SHIPMENTS);
		Element eleShipmentsOut = outDoc.getDocumentElement();

		NodeList containerNL = eleShipment.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
		for(int contCount=0;contCount<containerNL.getLength();contCount++){
			Element eleContainer = (Element)containerNL.item(contCount);
			String shipContainerKey= eleContainer.getAttribute(AcademyConstants.ATTR_SHIP_CONT_KEY);
			String contGrossWeight = eleContainer.getAttribute(AcademyConstants.ATTR_CONTAINER_GROSS_WEIGHT);
			String containerSCM = eleContainer.getAttribute(AcademyConstants.A_CONATINER_SCM);
			String contType = eleContainer.getAttribute(AcademyConstants.ATTR_CONATINER_TYPE);
			String contTypeKey = eleContainer.getAttribute(AcademyConstants.ATTR_CONTAINER_TYPE_KEY);

			Element eleShipmentOut = SCXmlUtil.createChild(eleShipmentsOut, AcademyConstants.ELE_SHIPMENT);
			eleShipmentOut.setAttribute(AcademyConstants.ATTR_SHIP_CONT_KEY, shipContainerKey);
			eleShipmentOut.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, shipmentKey);
			eleShipmentOut.setAttribute(AcademyConstants.ATTR_ORDER_NO, orderNo);
			eleShipmentOut.setAttribute(AcademyConstants.A_IS_WEB_STORE_FLOW, AcademyConstants.STR_YES);
			eleShipmentOut.setAttribute(AcademyConstants.ATTR_CONTAINER_GROSS_WEIGHT, contGrossWeight);
			eleShipmentOut.setAttribute(AcademyConstants.A_CONATINER_SCM, containerSCM);
			eleShipmentOut.setAttribute(AcademyConstants.ATTR_CONATINER_TYPE, contType);
			eleShipmentOut.setAttribute(AcademyConstants.ATTR_CONTAINER_TYPE_KEY, contTypeKey);

		}
		log.verbose("Input Created for AcademySFSProcessFinishPackService Service is:: "+ SCXmlUtil.getString(outDoc));
		log.endTimer("AcademyUpdateWeightAndFinishPack::formInputForProcessFinishPack");
		return outDoc;
	}
}
