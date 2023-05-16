package com.academy.ecommerce.sterling.egc;

import java.util.Properties;

/*##################################################################################
*
* Project Name                : EGC
* Module                      : OMS
* Author                      : CTS
* Date                        : 13-OCT-2020 
* Description				  : This class does following 
* 								1.ORDER_CREATE ON SUCCESS, the EGC lines(Full Order 
								XML) dropped into the queue is processed by this Service
* 								to prepare input to call confirm Shipment 
* 
* 
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       Remarks/Description                      
* ---------------------------------------------------------------------------------
* 13-OCT-2020	  CTS-POD 	 			  1.0           Initial version
* ##################################################################################*/

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.los.XMLUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyConfirmShipmentInputForEGCLines {
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyConfirmShipmentInputForEGCLines.class);
	
	private static Properties props;

	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}
	public Document prepareEGCShipmentInput(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer("Begin of AcademyConfirmShipmentInputForEGCLines - prepareEGCShipmentInput api");
		log.verbose("*** Entering AcademyConfirmShipmentInputForEGCLines.prepareEGCShipmentInput() ***");

		
		Document outDoc = prepareConfirmShipmentInputForEGC(env, inDoc);		
		
		log.verbose("*** Exiting AcademyConfirmShipmentInputForEGCLines.prepareEGCShipmentInput() ***");
		log.endTimer("End of AcademyConfirmShipmentInputForEGCLines - prepareEGCShipmentInput api");
		return outDoc;
	}

	private Document prepareConfirmShipmentInputForEGC(YFSEnvironment env, Document inDoc) throws Exception {
		
		log.verbose("*** Entering AcademyConfirmShipmentInputForEGCLines.prepareConfirmShipmentInputForEGC() ***");
		log.verbose("Input to the AcademyConfirmShipmentInputForEGCLines.prepareConfirmShipmentInputForEGC() is " +  XmlUtils.getString(inDoc));
		Document docshipInput = null;
		Document docShipOutput = null;
		Element eleInDoc = null;
		Element eleShpInp = null;
		Element eleShpLinesInp = null;

		String strOrderNo = null;
		String strShipNode = null;
		String strOrderHeaderKey = null;
		
		
		eleInDoc = inDoc.getDocumentElement(); 
		docshipInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		strOrderNo = eleInDoc.getAttribute(AcademyConstants.ATTR_ORDER_NO);
		strShipNode = eleInDoc.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
		strOrderHeaderKey = eleInDoc.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
		
		log.verbose("OrderNo is " + strOrderNo);
		log.verbose("ShipNode is " + strShipNode);
		
				
		eleShpInp = docshipInput.getDocumentElement();
		eleShpInp.setAttribute(AcademyConstants.ATTR_ORDER_NO,strOrderNo);
		eleShpInp.setAttribute(AcademyConstants.ATTR_DOC_TYPE, eleInDoc.getAttribute(AcademyConstants.ATTR_DOC_TYPE));
		eleShpInp.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, eleInDoc.getAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE));
		eleShpInp.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
		
		eleShpLinesInp = XmlUtils.createChild(eleShpInp, AcademyConstants.ELE_SHIPMENT_LINES);
		
		NodeList nl = XPathUtil.getNodeList(inDoc, "/Order/OrderLines/OrderLine[@FulfillmentType='EGC']");
		int nlength = nl.getLength();
		for(int i=0; i<nlength ; i++) {
			Element eleOrderLine = (Element)nl.item(i);
			Element eleItem = XmlUtils.getChildElement(eleOrderLine, AcademyConstants.ELEM_ITEM);
			
			Element eleShpLineInp =  XmlUtils.createChild(eleShpLinesInp, AcademyConstants.ELE_SHIPMENT_LINE);
			
			eleShpLineInp.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
			eleShpLineInp.setAttribute(AcademyConstants.ATTR_PRIME_LINE_NO, eleOrderLine.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO));
			eleShpLineInp.setAttribute(AcademyConstants.SUB_LINE_NO, eleOrderLine.getAttribute(AcademyConstants.SUB_LINE_NO));
			eleShpLineInp.setAttribute(AcademyConstants.ATTR_QUANTITY, eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY));
			eleShpLineInp.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
			eleShpLineInp.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
			
			eleShpLineInp.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID));
			eleShpLineInp.setAttribute(AcademyConstants.ATTR_PROD_CLASS, eleItem.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
			eleShpLineInp.setAttribute(AcademyConstants.ATTR_UOM, eleItem.getAttribute(AcademyConstants.ATTR_UOM));
			eleShpInp.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE));
			eleShpInp.setAttribute(AcademyConstants.ATTR_SHIP_NODE, eleOrderLine.getAttribute(AcademyConstants.ATTR_SHIP_NODE));
		}
		
		log.verbose("Output to the AcademyConfirmShipmentInputForEGCLines.prepareConfirmShipmentInputForEGC() is " +  XmlUtils.getString(docshipInput));
	
				
		log.verbose("AcademyConfirmShipmentForEGCService service Input is "+ XmlUtils.getString(docshipInput));
				
		docShipOutput = AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_CONFIRM_SHIPMENT_FOR_EGC_SERVICE,
					docshipInput);
		log.verbose("AcademyConfirmShipmentForEGCService service Output is "+ XmlUtils.getString(docShipOutput));
		
		log.verbose("*** Exiting AcademyConfirmShipmentInputForEGCLines.prepareConfirmShipmentInputForEGC() ***");
		return docshipInput;
	}

}

