package com.academy.ecommerce.sterling.bopis.inventory;

import java.util.ArrayList;

import org.w3c.dom.Document;

import com.academy.util.constants.AcademyConstants;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class checks for the change shipment messages
 * from store for shortage and decide of we need to send the message to SIM for unreserve.
 * It decides using ExtnMsgToSIM attribute at order line level set in the BeforeChangeShipmentUE.
 * @author Abhishek Aggarwal
 * @Date 13/06/2018
 */
public class AcademyUnResMsgToSIM 
{
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyUnResMsgToSIM.class);
	
	/**
	 * This method contains the main logic for the class to send unreserve msg to SIM.
	 * It decides using ExtnMsgToSIM attribute at order line level set in the BeforeChangeShipmentUE.
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception 
	 */
	public Document UnReserveMsgToSIM(YFSEnvironment env,Document inDoc) throws Exception
	{
		boolean boolSendMsg = false;
		boolean boolSendMsgOnCancel = true;
		YFCDocument outDocSIM = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
		YFCElement eleOutDocShp = outDocSIM.getDocumentElement();
		YFCElement eleOutDocShipLns = eleOutDocShp.createChild(AcademyConstants.ELE_SHIPMENT_LINES);
		String strOrderNo = null;
		
		ArrayList<String> arrShpLneListUE = (ArrayList<String>) env.getTxnObject("arrShipmentLineListOfUE");
		YFCDocument yfsInDoc = YFCDocument.getDocumentFor(inDoc);
		log.debug("AcademyUnResMsgToSIM.java : UnReserveMsgToSIM() :inDoc"+yfsInDoc.toString());
		YFCElement eleInDocShp = yfsInDoc.getDocumentElement();
		String strInDocMsgType = eleInDocShp.getAttribute(AcademyConstants.ATTR_MESSAGE_TYPE);
		YFCNodeList<YFCElement> nlInDocShpLne = eleInDocShp.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		for(YFCElement eleInDocShpLne : nlInDocShpLne)
		{
			String strShipmentLineKey = eleInDocShpLne.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
			YFCElement eleInDocExtn = eleInDocShpLne.getChildElement(AcademyConstants.ELE_EXTN);
			YFCElement eleInOrderLine = eleInDocShpLne.getChildElement(AcademyConstants.ELEM_ORDER_LINE);
			String strExtnMsgToSIM = eleInDocExtn.getAttribute(AcademyConstants.ATTR_EXTN_MSG_TO_SIM);
			
			String strActualQuantity = eleInDocShpLne.getAttribute(AcademyConstants.ATTR_ACTUAL_QUANTITY);
			double dbActualQuantity = Double.parseDouble(strActualQuantity);
			String strInDocQuantity = eleInDocShpLne.getAttribute(AcademyConstants.ATTR_SHORTAGE_QTY);
			double dbShortageQty = Double.parseDouble(strInDocQuantity);
			String strExtnReasonCode = eleInDocExtn.getAttribute(AcademyConstants.ATTR_EXTN_REASON_CODE);
			String strFulfillmentType = eleInOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
			if(YFCCommon.equalsIgnoreCase(AcademyConstants.STR_YES,strExtnMsgToSIM) && arrShpLneListUE.contains(strShipmentLineKey))
			{
				boolSendMsg = true;
				log.debug("AcademyUnResMsgToSIM.java : UnReserveMsgToSIM() :Boolean-boolSendMsg"+boolSendMsg);
				YFCElement eleOutDocShpLne = eleOutDocShipLns.createChild(AcademyConstants.ELE_SHIPMENT_LINE);
				
				String strInDocItemID = eleInDocShpLne.getAttribute(AcademyConstants.ATTR_ITEM_ID);
				strOrderNo = eleInDocShpLne.getAttribute(AcademyConstants.ATTR_ORDER_NO);
				
				eleOutDocShpLne.setAttribute(AcademyConstants.ATTR_QUANTITY, strInDocQuantity);
				eleOutDocShpLne.setAttribute(AcademyConstants.ATTR_ITEM_ID, strInDocItemID);
				eleOutDocShpLne.setAttribute(AcademyConstants.ATTR_ACTION,strInDocMsgType);
				
			}
			
			//check if the quantity of any line is greater then 0.
			//This check is done to ensure full cancel msg does not go from changeShipmentOnSuccess
			//Code Changes for OMNI-99090
			if ((dbActualQuantity > 0 && boolSendMsgOnCancel)
					|| (AcademyConstants.STR_CUSTOMER_ABANDONED.equals(strExtnReasonCode)
							&& AcademyConstants.V_FULFILLMENT_TYPE_BOPIS.equals(strFulfillmentType))) {
				boolSendMsgOnCancel = false;
			}
			
		}
		
		String strInDocShipNode = eleInDocShp.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
		String strInDocShipmentNo = eleInDocShp.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		String strInDocResvType = eleInDocShp.getAttribute(AcademyConstants.ATTR_RESERVATION_TYPE);
		String strInDocUserID = eleInDocShp.getAttribute(AcademyConstants.ATTR_USER_ID);
		
		if(boolSendMsg && !boolSendMsgOnCancel)
		{
			eleOutDocShp.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strInDocShipNode);
			eleOutDocShp.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strInDocShipmentNo);
			eleOutDocShp.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
			eleOutDocShp.setAttribute(AcademyConstants.ATTR_RESERVATION_TYPE, strInDocResvType);
			eleOutDocShp.setAttribute(AcademyConstants.ATTR_USER_ID, strInDocUserID);
			
		}
		
		log.debug("AcademyUnResMsgToSIM.java : UnReserveMsgToSIM() : outDoc"+outDocSIM.toString());
		return outDocSIM.getDocument();
	}
}
