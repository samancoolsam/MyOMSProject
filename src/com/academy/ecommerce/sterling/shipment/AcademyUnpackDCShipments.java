package com.academy.ecommerce.sterling.shipment;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AcademyUnpackDCShipments
{
  private static YFCLogCategory log = YFCLogCategory.instance(AcademyUnpackDCShipments.class);
  
  public Document unpackShipment(YFSEnvironment env, Document inDoc)
    throws Exception
  {
    Element inputEle = inDoc.getDocumentElement();
    String Shipment_no = inputEle.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
    String ShipNode = inputEle.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
    Document docInputGetShipmentDetail = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
    docInputGetShipmentDetail.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, Shipment_no);
    docInputGetShipmentDetail.getDocumentElement().setAttribute(AcademyConstants.ATTR_SELL_ORG_CODE, AcademyConstants.A_ACADEMY_DIRECT);
    docInputGetShipmentDetail.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIP_NODE, ShipNode);
    log.verbose("input is :" + XMLUtil.getXMLString(docInputGetShipmentDetail));
    
    env.setApiTemplate("getShipmentDetails", YFCDocument.getDocumentFor("<Shipment ShipmentKey=\"\" SellerOrganizationCode=\"\" Status=\"\" OrderHeaderKey=\"\"><ShipmentLines><ShipmentLine ShipmentLineKey=\"\" Quantity=\"\"  OrderLineKey=\"\" OrderReleaseKey=\"\" BackroomPickedQuantity=\"\"/></ShipmentLines><Containers><Container ShipmentContainerKey=\"\"><ContainerDetails><ContainerDetail Quantity=\"\" ShipmentLineKey=\"\" /></ContainerDetails></Container></Containers></Shipment>")
      .getDocument());
    Document shipmentDetailDocument = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_DETAILS, docInputGetShipmentDetail);
    Element eleShipmentDetail = shipmentDetailDocument.getDocumentElement();
    NodeList nm = shipmentDetailDocument.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
    if ((nm != null) && (nm.getLength() >= 1))
    {
      log.verbose("no. of containers:" + nm.getLength());
      String SellerOrganizationCode = eleShipmentDetail.getAttribute(AcademyConstants.ATTR_SELL_ORG_CODE);
      Document docUnpackShipmentInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
      Element eleUnpackShipmentInput = docUnpackShipmentInput.getDocumentElement();
      eleUnpackShipmentInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, Shipment_no);
      eleUnpackShipmentInput.setAttribute(AcademyConstants.ATTR_SELL_ORG_CODE, SellerOrganizationCode);
      eleUnpackShipmentInput.setAttribute(AcademyConstants.ATTR_SHIP_NODE, ShipNode);
      Element eleContainers = SCXmlUtil.createChild(eleUnpackShipmentInput, AcademyConstants.ELE_CONTAINERS);
      for (int i = 0; i < nm.getLength(); i++)
      {
        Node container = nm.item(i);
        if (container != null)
        {
          Element eleContainerNew = (Element)container;
          Element eleContainer = SCXmlUtil.createChild(eleContainers,"Container" );
          eleContainer.setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY, eleContainerNew.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY));
        }
      }
      Document docUndoBackRoomPickInput = XMLUtil.createDocument("UndoPick");
      Element eleUndoBackRoomPickInput = docUndoBackRoomPickInput.getDocumentElement();
      Element eleShipment = SCXmlUtil.createChild(eleUndoBackRoomPickInput, AcademyConstants.ELE_SHIPMENT);
      eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, Shipment_no);
      eleShipment.setAttribute(AcademyConstants.ATTR_SELL_ORG_CODE, SellerOrganizationCode);
      eleShipment.setAttribute(AcademyConstants.ATTR_SHIP_NODE, ShipNode);
      eleShipment.setAttribute(AcademyConstants.ATTR_TRANS_ID, "YCD_UNDO_BACKROOM_PICK");
      Element eleShipmentLines = SCXmlUtil.createChild(eleShipment, AcademyConstants.ELE_SHIPMENT_LINES);
      
      Document changeOrderStatusInput = XMLUtil.createDocument(AcademyConstants.ELE_ORD_STATUS_CHG);
      Element eleChangeOrderStatusInput = changeOrderStatusInput.getDocumentElement();
      eleChangeOrderStatusInput.setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS, AcademyConstants.VAL_SI_INCLUDEDINSHIPMENT_STATUS);
      eleChangeOrderStatusInput.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, SellerOrganizationCode);
      eleChangeOrderStatusInput.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
      eleChangeOrderStatusInput.setAttribute("IgnoreTransactionDependencies",AcademyConstants.STR_YES);
      eleChangeOrderStatusInput.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, eleShipmentDetail.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
      eleChangeOrderStatusInput.setAttribute(AcademyConstants.ATTR_TRANS_ID,AcademyConstants.SI_CONSOL_TO_SHIPMENT_TRAN_ID); // SI_CONSOL_TO_SHIPMENT.0001.ex 
      Element eleOrderLines = SCXmlUtil.createChild(eleChangeOrderStatusInput, AcademyConstants.ELE_ORDER_LINES);
      NodeList nShipmentLine = XMLUtil.getNodeList(shipmentDetailDocument, AcademyConstants.XPATH_SHIPMENT_LINE);
      for (int i = 0; i < nShipmentLine.getLength(); i++)
      {
        Node shipmentLine = nShipmentLine.item(i);
        Element eleShipmentLineNew = (Element)shipmentLine;
        String Quantity = eleShipmentLineNew.getAttribute(AcademyConstants.ATTR_QUANTITY);
        if (Double.parseDouble(Quantity) > 0.0D)
        {
          log.verbose("quantity of shipmentLine to unpack is:" + Quantity);
          Element eleShipmentLine = SCXmlUtil.createChild(eleShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE);
          Element eleOrderLine = SCXmlUtil.createChild(eleOrderLines, AcademyConstants.ELE_ORDER_LINE);
          eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, eleShipmentLineNew.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
          eleOrderLine.setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS, AcademyConstants.VAL_SI_INCLUDEDINSHIPMENT_STATUS);
          eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, eleShipmentLineNew.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
          eleOrderLine.setAttribute(AcademyConstants.ATTR_RELEASE_KEY, eleShipmentLineNew.getAttribute(AcademyConstants.ATTR_RELEASE_KEY));
          eleOrderLine.setAttribute(AcademyConstants.ATTR_QUANTITY, eleShipmentLineNew.getAttribute(AcademyConstants.ATTR_QUANTITY));
          eleOrderLine.setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS, AcademyConstants.VAL_SI_INCLUDEDINSHIPMENT_STATUS);
        }
        else
        {
          log.verbose("Invalid Input:Shipment Line qty is 0");
        }
      }
      log.verbose("input for undo backroom pick is : " + XMLUtil.getXMLString(docUndoBackRoomPickInput));
      log.verbose("input for changeOrderStatus is : " + XMLUtil.getXMLString(changeOrderStatusInput));
      log.verbose("input for unpack is : " + XMLUtil.getXMLString(docUnpackShipmentInput));
      AcademyUtil.invokeAPI(env, "undoBackRoomPick", docUndoBackRoomPickInput);
      AcademyUtil.invokeAPI(env, "unpackShipment", docUnpackShipmentInput);
      AcademyUtil.invokeAPI(env, AcademyConstants.API_CHG_ORD_STATUS, changeOrderStatusInput);
    }
    else
    {
      log.verbose("Invalid Input");
      YFSException yfsException = new YFSException();
      yfsException.setErrorDescription("YFS:Invalid ShipmentNo: Shipment doesn't have container to unpack");
      
      throw yfsException;
    }
    return inDoc;
  }
}
