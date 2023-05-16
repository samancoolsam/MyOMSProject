package com.academy.ecommerce.sterling.sts;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


import com.academy.ecommerce.sterling.los.XMLUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XPathUtil;
import com.comergent.api.xml.XMLUtils;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySTSFetchSOShipmentDetails {
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademySTSFetchSOShipmentDetails.class);

	/*
	 * This method is used to get sales order shipments
	 * for the TO order to display Shipment number and 
	 * Shipment Status 
	 * 
	 * @param env
	 * @param inDoc
	 * 
	 * @throws Exception
	 */
	public Document fetchSOShipmentsForSTSOrders(YFSEnvironment env, Document inDoc) throws Exception{
		
		log.verbose("Start of AcademySTSFetchSOShipmentDetails::fetchSOShipmentsForSTSOrders");

		log.verbose("Entering AcademySTSFetchSOShipmentDetails.fetchSOShipmentsForSTSOrders() with Input :: "
				+ XMLUtil.getString(inDoc));
		
		NodeList nlShipments = XPathUtil.getNodeList(inDoc, AcademyConstants.XPATH_SHIPMENT);
			//	inDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
		Element eleTOShipment = (Element) nlShipments.item(0);

		String strExtnSOShipmentNo = null;
		if(nlShipments.getLength() >0) {
			
			Element eleExtn = (Element) XPathUtil.getNodeList(inDoc, AcademyConstants.XPATH_CONTAINER_ELE).item(0);
			if(!YFCCommon.isVoid(eleExtn)) {
				
				//OMNI-32370 - START - Added logic to add the list of shipment lines that are not packed to dummy Container to display the product details in WebSOM
				Element eleContainer = null;
				for(int i=0;i<nlShipments.getLength();i++) {
					
					Element eleShip = (Element) nlShipments.item(i);
					Boolean isNewContainerRequired = false;
					Element eleContainers  = XmlUtils.getChildElement(eleShip, AcademyConstants.ELE_CONTAINERS);
					
					Element eleShipLines = XmlUtils.getChildElement(eleShip, AcademyConstants.ELE_SHIPMENT_LINES);
					NodeList nlShipLines = eleShipLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
					
					eleContainer  = inDoc.createElement(AcademyConstants.ELE_CONTAINER);
					eleContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_NO, AcademyConstants.STR_NOT_SHIPPED);
					eleContainer.setAttribute(AcademyConstants.ATTR_IS_RECEIVED, AcademyConstants.STR_NO);
					Element eleContainerDetails = inDoc.createElement(AcademyConstants.ELE_CONTAINER_DTLS);

					for(int j=0;j<nlShipLines.getLength(); j++) {
						Element eleShpLine = (Element) nlShipLines.item(j);
						String strBackroomPickQty = eleShpLine.getAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY);
						String strQuantity = eleShpLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
						Double dUnpickedQty = 0.00;

						if(!strQuantity.equals(strBackroomPickQty)) {
							if(!YFCCommon.isVoid(strBackroomPickQty) && !YFCCommon.isVoid(strQuantity)) {
								dUnpickedQty = Double.parseDouble(strQuantity) - Double.parseDouble(strBackroomPickQty);

							} else if(YFCCommon.isVoid(strBackroomPickQty)) {
								dUnpickedQty = Double.parseDouble(strQuantity);
							}
							if(dUnpickedQty>0) {
								
								Element eleContainerDetail = inDoc.createElement(AcademyConstants.CONTAINER_DETL_ELEMENT);
								eleContainerDetail.setAttribute(AcademyConstants.ATTR_QUANTITY, String.format(AcademyConstants.VAL_2DIGIT_DECIMAL_FORMAT,dUnpickedQty));
								Element eleShipmentLine = inDoc.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
								Element eleOrderLine = inDoc.createElement(AcademyConstants.ELE_ORDER_LINE);
								Element eleItem = inDoc.createElement(AcademyConstants.ITEM);
								
								Element eleItemInfo = XMLUtils.getElementByName(eleShpLine, AcademyConstants.ITEM);
								eleItem.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleItemInfo.getAttribute(AcademyConstants.ATTR_ITEM_ID));
								eleItem.setAttribute(AcademyConstants.ATTR_ITEM_DESC, eleItemInfo.getAttribute(AcademyConstants.ATTR_ITEM_DESC));
								eleOrderLine.appendChild(eleItem);
								eleShipmentLine.appendChild(eleOrderLine);
								eleContainerDetail.appendChild(eleShipmentLine);
								eleContainerDetails.appendChild(eleContainerDetail);
								eleContainer.appendChild(eleContainerDetails);
								isNewContainerRequired = true;
							}
						}
					}
				
					if(isNewContainerRequired) {
							eleContainers.appendChild(eleContainer);
					}
					
				}
				
				//OMNI-32370 - END Added logic to add the list of shipment lines that are not packed to dummy Container to display the product details in WebSOM
				
				strExtnSOShipmentNo = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_SO_SHIPMENT_NO); 
				if(!YFCCommon.isVoid(strExtnSOShipmentNo)) {
					Element eleContainers = (Element) eleTOShipment.getElementsByTagName(AcademyConstants.ELE_CONTAINERS).item(0);
					eleTOShipment.removeChild(eleContainers);

					for(int  i=1; i<nlShipments.getLength(); i++) {
						Element  eleShip = (Element) nlShipments.item(i);			
						eleShip.getParentNode().removeChild(eleShip);

					}
				
					Document docGetShipmentContainerList = getShipmentContainerListWithSOShipmentNo(env,strExtnSOShipmentNo);
					Element eleGetShipmentContainerList = docGetShipmentContainerList.getDocumentElement();
					Element newShipmentContainers = inDoc.createElement(AcademyConstants.ELE_CONTAINERS);

					newShipmentContainers = (Element) inDoc.importNode(eleGetShipmentContainerList, true);
					eleTOShipment.appendChild(newShipmentContainers);
					log.verbose("GetShipment list: " +XMLUtil.getString(inDoc));
				}
				
				//OMNI-32370 End - Display Containers based on SO Shipment Level / TO OrderHeader level
			}
		}
		
	    //Element eleTOShipmentLine =(Element) eleTOShipment.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE).item(0);
		Element eleTOShipmentLine =(Element) XPathUtil.getNodeList(inDoc, "/Shipments/Shipment/ShipmentLines/ShipmentLine").item(0);
	    Element eleTOOrderLine = (Element) eleTOShipmentLine.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINE).item(0);
	    Element eleChainedFromOrderLine =(Element) eleTOOrderLine.getElementsByTagName(AcademyConstants.ELE_CHAINED_FROM_ORDER_LINE).item(0);
	    String strSOOrderHeaderKey = eleChainedFromOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
	    
	    Document docShipmentListIn = null;
	    //prepare Input to getShipment List with Sales order header key
	    if(!YFCCommon.isVoid(strExtnSOShipmentNo)) {
	    		    	
	    	docShipmentListIn = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		    Element eleShipmentListIn = docShipmentListIn.getDocumentElement(); 
		    eleShipmentListIn.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strExtnSOShipmentNo);
		    eleShipmentListIn.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.STR_SHIP_TO_STORE);
		   
	    } else if (!YFCObject.isVoid(strSOOrderHeaderKey)) {
	    
		    docShipmentListIn = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		    Element eleShipmentListIn = docShipmentListIn.getDocumentElement(); 
		    eleShipmentListIn.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strSOOrderHeaderKey);
		    eleShipmentListIn.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.STR_SHIP_TO_STORE);
		}
	   
	    log.verbose("Get Shipment List Input: " + XMLUtil.getString(docShipmentListIn));
		// OMNI-9303 Unable to reprint the pickup acknowledgment slip for STS orders - START
	    Document shipmentListOutputTemplate = YFCDocument.getDocumentFor("<Shipments> <Shipment OrderNo='' ShipmentNo='' ShipmentKey=''> <Extn ExtnOnMyWayOpted='' ExtnIsCurbsidePickupOpted=''/> <ShipmentLines> <ShipmentLine ShortageQty='' ShipmentLineKey=''>"
				+ "<OrderLine OrderLineKey=''> <Notes/> </OrderLine> </ShipmentLine> </ShipmentLines> <Status Status='' Description=''/> </Shipment> </Shipments>").getDocument();
		// OMNI-9303 Unable to reprint the pickup acknowledgment slip for STS orders - END
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, shipmentListOutputTemplate);
		Document shipmentListOutputDocument = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, docShipmentListIn);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		
		log.verbose("Get Shipment List Output: " + XMLUtil.getString(shipmentListOutputDocument));
		
		if(!YFCObject.isVoid(shipmentListOutputDocument))
		{
			Element eleShipmentListOut = shipmentListOutputDocument.getDocumentElement();
			NodeList nlShipmentsOut = eleShipmentListOut.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
		     
			if(nlShipmentsOut.getLength() >0)
			{
				Element eleShipmentOut = (Element) nlShipmentsOut.item(0);
				String strShipmentNo = eleShipmentOut.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
				Element eleStatus  = (Element) eleShipmentOut.getElementsByTagName(AcademyConstants.STATUS).item(0);
				String strSOStatus = eleStatus.getAttribute(AcademyConstants.ATTR_STATUS);
				String strSOStatusDescrition = eleStatus.getAttribute(AcademyConstants.ATTR_DESCRIPTION);
				//OMNI-72012 Start
				Element eleExtn  = (Element) eleShipmentOut.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
				String strExtnIsOMYOpted = eleExtn.getAttribute(AcademyConstants.EXTN_ON_MY_WAY_OPTED);
				//OMNI-72012 End
				//Start OMNI-75388
				String strExtnIsCurbsidePickupOpted = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_CURSIDE_PICK_OPTED);
				//End OMNI-75388
			//	OMNI-9303 Unable to reprint the pickup acknowledgment slip for STS orders - START		
				String strShipmentKey = eleShipmentOut.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			// OMNI-9303 Unable to reprint the pickup acknowledgment slip for STS orders - END
			//OMNI- 9408 - STS - Order Search Result - Cancellation Product Information - Start
				Element eleSalesShipmentLines = (Element) eleShipmentOut.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES).item(0);
				Element eleSaleOrderShipmentLines = inDoc.createElement(AcademyConstants.ELE_SO_SHIPMENT_LINES);
				Element eleSOShipmentLines = (Element) inDoc.importNode(eleSalesShipmentLines, true);
				eleSaleOrderShipmentLines.appendChild(eleSOShipmentLines);
				eleTOShipment.appendChild(eleSaleOrderShipmentLines);
		   //OMNI - 9408 - STS - Order Search Result - Cancellation Product Information - END
				if(!YFCCommon.isVoid(strExtnSOShipmentNo)) {					
					eleTOShipment.setAttribute(AcademyConstants.ATTR_SO_SHIPMENT_NO, strShipmentNo);
					eleTOShipment.setAttribute(AcademyConstants.ATTR_SO_SHIPMENT_STATUS, strSOStatus);
					eleTOShipment.setAttribute(AcademyConstants.ATTR_SO_SHIPMENT_STATUS_DESC, strSOStatusDescrition);
				} else {
					eleTOShipment.setAttribute(AcademyConstants.ATTR_SO_SHIPMENT_NO, "");
					eleTOShipment.setAttribute(AcademyConstants.ATTR_SO_SHIPMENT_STATUS, "");
					eleTOShipment.setAttribute(AcademyConstants.ATTR_SO_SHIPMENT_STATUS_DESC, "");
				}
				//	OMNI-9303 Unable to reprint the pickup acknowledgment slip for STS orders - START
				eleTOShipment.setAttribute(AcademyConstants.ATTR_SO_SHIPMENT_KEY, strShipmentKey);
			   // OMNI-9303 Unable to reprint the pickup acknowledgment slip for STS orders - END
			   //OMNI-72012 Start
			   eleTOShipment.setAttribute(AcademyConstants.SO_EXTN_ON_MY_WAY_OPTED, strExtnIsOMYOpted);
			   //OMNI-72012 End
			 //Start OMNI-75388
			  eleTOShipment.setAttribute(AcademyConstants.SO_EXTN_IS_CURBSIDE_PICKUP_OPTED, strExtnIsCurbsidePickupOpted);
			  //End OMNI-75388
				log.verbose("Final Output: " + XMLUtil.getString(inDoc));
			}
	
	   }
	  
	
		   
	    return inDoc;

	}

	public Document getShipmentContainerListWithSOShipmentNo(YFSEnvironment env, String strSOShipmentNo)
			throws Exception {

		log.beginTimer("AcademySTSFetchSOShipmentDetails.getShipmentContainerListWithSOShipmentNo");
		Document docGetShipmentContainerListOut = null;

		Document docGetShipmentContainerListIn = XMLUtil.createDocument(AcademyConstants.ELE_CONTAINER);
		Element eleGetShipmentContainerListIn = docGetShipmentContainerListIn.getDocumentElement();
		Element eleExtn = docGetShipmentContainerListIn.createElement(AcademyConstants.ELE_EXTN);
		eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_SO_SHIPMENT_NO, strSOShipmentNo);
		eleGetShipmentContainerListIn.appendChild(eleExtn);

		System.out.println("Get Shipment Container List Input: " + XMLUtil.getString(docGetShipmentContainerListIn));

		docGetShipmentContainerListOut = AcademyUtil.invokeService(env,
				AcademyConstants.SERV_ACADEMY_GET_STS_CONTAINERS_WITH_SO_SHIPMENT_NO, docGetShipmentContainerListIn);
		
		log.verbose("Output of AcademySTSFetchSOShipmentDetails.getShipmentContainerListWithSOShipmentNo() :: " + XMLUtil.getString(docGetShipmentContainerListOut));

		log.endTimer("AcademySTSFetchSOShipmentDetails.getShipmentContainerListForTOOrders");

		return docGetShipmentContainerListOut;

	}

}

   
