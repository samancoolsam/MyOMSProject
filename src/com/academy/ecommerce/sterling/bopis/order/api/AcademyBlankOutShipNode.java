package com.academy.ecommerce.sterling.bopis.order.api;

import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @Author : Chiranthan(SapientRazorfish_)
 * @JIRA# : BOPIS-11
 * @Date : Created on 08-June-2018
 * 
 * @Purpose : 
 * This class loops through all the orderlines and blanks out shipNode for orderlines other than BOPIS & SOF.
 * Retaining ShipNode for BOPIS & SOF orderlines, Since customer wants to pickup the item from that preDefined Node.
 * 
 **/
public class AcademyBlankOutShipNode implements YIFCustomApi {

	private static Logger log = Logger.getLogger(AcademyBlankOutShipNode.class.getName());

	private Properties props;

	public void setProperties(Properties props) throws Exception {
		
		this.props = props;
	}
	
	public Document blankShipNodeForNonPickUpOrderLines(YFSEnvironment env, Document inDoc) throws Exception {		
		log.verbose("Entering AcademyBlankOutShipNode.blankShipNodeForNonPickUpOrderLines() :: "+XMLUtil.getXMLString(inDoc));
		
		Element eleOrderLine = null;
		String strDeliveryMethod = null;
		String strFulfillmentType = null;
		//Start OMNI-5001
		String strLineType= null;
		//End Start OMNI-5001
		NodeList nlOrderLine = inDoc.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);

		for (int i = 0; i < nlOrderLine.getLength(); i++) {
			eleOrderLine = (Element) nlOrderLine.item(i);			
			
			strDeliveryMethod = eleOrderLine.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
			strFulfillmentType = eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
			//Start OMNI-5001
			strLineType = eleOrderLine.getAttribute(AcademyConstants.ATTR_LINE_TYPE);
			//End Start OMNI-5001
			
			//For Orderlines other than BOPIS/SOF/EGC(OMNI-5001), blanking out the ShipNode
			if(!(AcademyConstants.STR_PICK_DELIVERY_METHOD.equals(strDeliveryMethod) || AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equals(strFulfillmentType)
					 || AcademyConstants.STR_E_GIFT_CARD.equals(strLineType) )){
				eleOrderLine.setAttribute(AcademyConstants.ATTR_SHIP_NODE, AcademyConstants.STR_EMPTY_STRING);
				log.verbose("Blanked out ShipNode");
			}
		}							
		log.verbose("Exiting AcademyBlankOutShipNode.blankShipNodeForNonPickUpOrderLines() ::"+XMLUtil.getXMLString(inDoc));
		
		return inDoc;
	}
}
