package com.academy.ecommerce.sterling.bopis.inventory;


/*##################################################################################
*
* Project Name                : Release 3
* Module                      : OMS - OMNI - 1090
* Author                      : CTS - POD
* Date                        : 13-SEP-2019 
* Description				  : This class does following 
* 								  1. Updates the Shortage Qty with BackroomPickedQuantity
* 								  2. changeShipment Api is called to update ExtnMSGToSIM
* 									 to Y
* 								  
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       Remarks/Description                      
* ---------------------------------------------------------------------------------
* 13-SEP-2019	 CTS - POD	 			  1.0            Initial version
* ##################################################################################*/

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.util.List;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyUpdateBOPISShipmentDetailsForMsgToSIM {
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyUpdateBOPISShipmentDetailsForMsgToSIM.class);
	
	public Document updateShortageQty(YFSEnvironment env,Document inDoc) throws Exception
	{
		log.verbose("Begin AcademyUpdateBOPISShipmentDetailsForMsgToSIMNew.updateShortageQty() :: "+XMLUtil.getXMLString(inDoc));
		Document outDoc = null;
		Element eleShipment = inDoc.getDocumentElement(); 
		Element eleShipmentLine = null;
		String strFulfillmentType = null;
		String strPackListType = null;
		Document cloneInDoc = XMLUtil.cloneDocument(inDoc);
		
		//OMNI-41168 - Check for distinguishing Order and Line Level Cancellations from WCS - END			
		
		Document changeShipmentInputDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleShipmentInput = changeShipmentInputDoc.getDocumentElement();
		Element eleShipmentLines = changeShipmentInputDoc.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
		eleShipmentInput.appendChild(eleShipmentLines);
		Element eleShipmentLineInput = null;
		Element eleExtn = null;
		eleShipmentInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
		eleShipmentInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
		eleShipmentInput.setAttribute(AcademyConstants.ATTR_ACTION_ON_SIM_FLAG,AcademyConstants.STR_UPDATE_SIM_FLAG_VALUE);
		
		//NodeList ndList = XPathUtil.getNodeList(eleShipment, AcademyConstants.XPATH_SHORTAGE_QTY_SHIPMENT_LINE);
		
		/* OMNI-25651 : START - Changes to post BOPIS unreserve messages for line level cancellations to SIM */
		//ndList contains the shipment lines eligible for cancellation
		NodeList ndList = XPathUtil.getNodeList(eleShipment, AcademyConstants.XPATH_SHIPMENTLINE_CANCELLED);
		
		//ndListRemove contains the shipment lines that are already cancelled or not eligible for cancellation. This is to avoid sending 
		//duplicate messages to SIM
		NodeList ndListRemove = XPathUtil.getNodeList(eleShipment, AcademyConstants.XPATH_SHIPMENTLINE_TO_BE_REMOVED);

		for (int i=0;i<ndListRemove.getLength();i++) {
			Element eleShipmentLinesToRemove= (Element) ndListRemove.item(i);
			log.verbose("Shipment Line element to be removed :: " + XMLUtil.getElementXMLString(eleShipmentLinesToRemove));
			eleShipmentLinesToRemove.getParentNode().removeChild(eleShipmentLinesToRemove);
		}
		/* OMNI-25651 : END - Changes to post BOPIS unreserve messages for line level cancellations to SIM */
		int iShipmentLineCount = ndList.getLength();
		List <String> lShipmentLine = new ArrayList<String>();
		for(int iCount = 0; iCount<iShipmentLineCount;iCount++ ){	
			
			eleShipmentLine= (Element) ndList.item(iCount);
			String strBackroomPickQty = eleShipmentLine.getAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY);	
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHORTAGE_QTY,strBackroomPickQty);
			Element elemExtn = (Element) eleShipmentLine.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
			String strExtnMsgToSIM = elemExtn.getAttribute(AcademyConstants.ATTR_EXTN_MSG_TO_SIM);
			
			eleShipmentLineInput = changeShipmentInputDoc.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
			eleShipmentLineInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
			lShipmentLine.add(eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
			/* OMNI-25651 : START - Changes to post BOPIS unreserve messages for line level cancellations to SIM */
			eleShipmentLineInput.setAttribute(AcademyConstants.ATTR_SHORTAGE_QTY, strBackroomPickQty);
			/* OMNI-25651 : END - Changes to post BOPIS unreserve messages for line level cancellations to SIM */

			eleExtn  = changeShipmentInputDoc.createElement(AcademyConstants.ELE_EXTN);
			eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_MSG_TO_SIM, strExtnMsgToSIM);
			
			eleShipmentLineInput.appendChild(eleExtn);
			eleShipmentLines.appendChild(eleShipmentLineInput);	
		}
		
		strFulfillmentType = XPathUtil.getString(cloneInDoc,"/Shipment/ShipmentLines/ShipmentLine/OrderLine/@FulfillmentType");
		strPackListType = XPathUtil.getString(cloneInDoc,"/Shipment/ShipmentLines/ShipmentLine/OrderLine/@PackListType");
		if(!YFCCommon.isVoid(strFulfillmentType) && AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE.equals(strFulfillmentType) 
				&& !YFCCommon.isVoid(strPackListType) && AcademyConstants.STS_FA.equals(strPackListType)) {
			
			NodeList nlShipmentLine = XMLUtil.getNodeListByXPath(cloneInDoc, "Shipment/ShipmentLines/ShipmentLine");		
			for(int i = 0; i<nlShipmentLine.getLength();i++ ) {
				Element eleShipmentLineClone= (Element) nlShipmentLine.item(i);
				String strShipmentLineKey = eleShipmentLineClone.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);	
				String strActualQty = eleShipmentLineClone.getAttribute(AcademyConstants.ATTR_ACTUAL_QUANTITY);
				log.verbose("Checking ShipmentTagSerial");
				
				Element eleShipmentTagSerial = (Element) eleShipmentLineClone.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_TAG_SERIAL).item(0);
				if(("0.00").equalsIgnoreCase(strActualQty) && !YFCObject.isNull(eleShipmentTagSerial)) {
					
					log.verbose("ActualQuantity :: "+strActualQty + "   eleShipmentTagSerials is not Null");
					if(lShipmentLine.contains(strShipmentLineKey)) {
						
						log.verbose("ShipmentKey is present in lShipmentLine list");
						Element eleShipLineChangeShipment = XMLUtil.getElementByXPath(changeShipmentInputDoc, 
								"Shipment/ShipmentLines/ShipmentLine[@ShipmentLineKey='"+strShipmentLineKey+"']");
						Element eleShipmentTagSerials = changeShipmentInputDoc.createElement(AcademyConstants.ELE_SHIPMENT_TAG_SERIALS);
						eleShipmentTagSerials.setAttribute(AcademyConstants.ATTR_REPLACE, AcademyConstants.STR_YES);
						eleShipLineChangeShipment.appendChild(eleShipmentTagSerials);	
					} else { 
						
						log.verbose("ShipmentKey is not present in lShipmentLine list");
						Element eleShipLineChangeShipment = changeShipmentInputDoc.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
						eleShipLineChangeShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, strShipmentLineKey);
						Element eleShipmentTagSerials = changeShipmentInputDoc.createElement(AcademyConstants.ELE_SHIPMENT_TAG_SERIALS);
						eleShipmentTagSerials.setAttribute(AcademyConstants.ATTR_REPLACE, AcademyConstants.STR_YES);
						eleShipLineChangeShipment.appendChild(eleShipmentTagSerials);
						eleShipmentLines.appendChild(eleShipLineChangeShipment);
					}
				} 
			}
		}
				
		Document changeShimentApiTemplate = XMLUtil.getDocument("<Shipment ShipmentKey='' ShipmentNo=''>"
				+ "<ShipmentLines>"
				+ "<ShipmentLine ShipmentLineKey=''> "
				+ "<Extn ExtnMsgToSIM=''/>"
				+ "</ShipmentLine>"
				+ "</ShipmentLines>"
				+ "</Shipment>");
		env.setApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT, changeShimentApiTemplate);
		log.verbose("Input to the changeShipmentAPI "+XMLUtil.getXMLString(changeShipmentInputDoc));


		outDoc = AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_SHIPMENT, changeShipmentInputDoc);

		env.clearApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT);
		log.verbose("Output to the changeShipmentAPI "+XMLUtil.getXMLString(outDoc));

		log.verbose("End of AcademyUpdateBOPISShipmentDetailsForMsgToSIMNew.updateShortageQty() :: "+XMLUtil.getXMLString(inDoc));
		return inDoc;
	}

}
