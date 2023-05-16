package com.academy.ecommerce.sterling.bopis.batch.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
/**
 * BOPIS-1244. This class is developed as a part of BOPIS-1244 Defect.
 * This class will Stamp OrderDate at ShipmentLine/Extn/ExtnOrderDate for SHP Shipments. Later this date will be used in Batch Sorting logic.
 * @author rastaj1
 *
 */
public class AcademyStampOrderDateAtShipLine {
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyStampOrderDateAtShipLine.class);
	public Document stampOrderDate(YFSEnvironment env, Document inDoc) throws Exception{
		log.beginTimer("AcademyStampOrderDateAtShipLine::stampOrderDate()");
		String delMethod = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
		if(inDoc.getElementsByTagName("ShipmentLine").getLength()>0 && !AcademyConstants.STR_PICK_DELIVERY_METHOD.equals(delMethod)){
			Element eleShipLine=(Element) inDoc
					.getElementsByTagName("ShipmentLine").item(0);
			String orderHeaderKey = eleShipLine.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
			
			if(!YFCCommon.isVoid(orderHeaderKey)) {
				Document getOrderListInDoc = XMLUtil.getDocument("<Order OrderHeaderKey='"+orderHeaderKey+"' />");
				log.verbose("Calling getOrderList to fetch the orderDate");
				Document getOrderListOutDoc = AcademyUtil.invokeService(env, "AcademyBOPISGetOrderDate", getOrderListInDoc);
				String orderDate = XMLUtil.getAttributeFromXPath(getOrderListOutDoc, "OrderList/Order/@OrderDate");
				if(!YFCCommon.isVoid(orderDate)) {
					setOrderDateAtEachShipmentLine(orderDate,inDoc);
				}
			}
		}
		log.endTimer("AcademyStampOrderDateAtShipLine::stampOrderDate()");
		return inDoc;
	}
	private void setOrderDateAtEachShipmentLine(String orderDate, Document inDoc) {
		log.beginTimer("AcademyStampOrderDateAtShipLine::setOrderDateAtEachShipmentLine()");
		NodeList shipmentLineNL = inDoc.getElementsByTagName("ShipmentLine");
		for(int i=0;i<shipmentLineNL.getLength();i++){
			Element eleShipmentLine = (Element) shipmentLineNL.item(i);
			Element eleExtn = SCXmlUtil.getChildElement(eleShipmentLine, AcademyConstants.ELE_EXTN, true);
			log.verbose("ExtnOrderDate is set to " + orderDate);
			eleExtn.setAttribute("ExtnOrderDate", orderDate);
		}
		log.endTimer("AcademyStampOrderDateAtShipLine::setOrderDateAtEachShipmentLine()");
	}


}
