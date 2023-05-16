package com.academy.ecommerce.sterling.shipment;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AcademyGetShipmentListForUnpack
{
  private static YFCLogCategory log = YFCLogCategory.instance(AcademyGetShipmentListForUnpack.class);
  
  public Document getShipmentList(YFSEnvironment env, Document inDoc)
    throws Exception
  {
    log.verbose("AcademyGetShipmentList.getShipmentList() Input XML ::" + XMLUtil.getXMLString(inDoc));
    Document docOut = null;
    Element eleShipment = inDoc.getDocumentElement();
    String strShipmentNo = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
    try
    {
      if (!YFCObject.isVoid(strShipmentNo))
      {
        log.verbose("ShipmentNo is :" + strShipmentNo);
        String strGetShipmentListTemplate = "<Shipments> <Shipment ActualDeliveryDate=\"\" ActualShipmentDate=\"\" BuyerOrganizationCode=\"\" CarrierServiceCode=\"\" DeliveryPlanKey=\"\" DeliveryPlanNo=\"\" EnterpriseCode=\"\" ExpectedDeliveryDate=\"\" ExpectedShipmentDate=\"\" HazardousMaterialFlag=\"\" HoldFlag=\"\" IsPackProcessComplete=\"\" IsTagSerialRequested=\"\" OrderAvailableOnSystem=\"\" PickListNo=\"\" ReceivingNode=\"\" SCAC=\"\" ShipMode=\"\" ShipNode=\"\" ShipmentKey=\"\" ShipmentNo=\"\" ShipmentType=\"\" Status=\"\" TotalVolume=\"\" TotalVolumeUOM=\"\" TotalWeight=\"\" TotalWeightUOM=\"\"> <ScacAndService ScacAndServiceDesc=\"\"/> <ToAddress BuyerOrganizationCode=\"\" City=\"\" Country=\"\" State=\"\" ZipCode=\"\"/> <Status Description=\"\" ProcessTypeKey=\"\" Status=\"\" StatusKey=\"\" StatusName=\"\"/> </Shipment> </Shipments>";
        Document docGetShipmentListTemplate = XMLUtil.getDocument(strGetShipmentListTemplate);
        
        env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docGetShipmentListTemplate);
        docOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, inDoc);
        Element eleShipmentList = docOut.getDocumentElement();
        Element eleOutShipment = (Element)eleShipmentList.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
        if (YFCCommon.isVoid(eleOutShipment))
        {
          YFSException yfsException = new YFSException();
          yfsException.setErrorDescription("Invalid ShipmentNo");
          
          throw yfsException;
        }
      }
      else
      {
        YFSException yfsException = new YFSException();
        yfsException.setErrorDescription("Blank ShipmentNo: Please enter a Shipment Number to search");
        
        throw yfsException;
      }
    }
    catch (Exception yfsException)
    {
      throw yfsException;
    }
    log.verbose("AcademyGetShipmentList.getShipmentList() Output XML :: " + XMLUtil.getXMLString(docOut));
    return docOut;
  }
}
