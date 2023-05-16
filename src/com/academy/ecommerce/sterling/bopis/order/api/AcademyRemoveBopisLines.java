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
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @Author : Chiranthan(SapientRazorfish_)
 * @JIRA# : BOPIS-11
 * @Date : Created on 08-June-2018
 * 
 * @Purpose : 
 * To maintain a separate order flow for BOPIS order, we need to skip few service invocation for BOPIS orders.
 * In case of mixed cart orders, we filter out BOPIS lines and follow as-is flow for Non-BOPIS lines.
 * 
 **/

public class AcademyRemoveBopisLines implements YIFCustomApi {

	private static Logger log = Logger.getLogger(AcademyRemoveBopisLines.class.getName());

	private Properties props;

	public void setProperties(Properties props) throws Exception {
		
		this.props = props;
	}
	
	public Document removeBOPISOrderLines(YFSEnvironment env, Document inDoc) throws Exception {		
		log.verbose("Entering AcademyRemoveBopisLines.removeBOPISOrderLines() :: "+XMLUtil.getXMLString(inDoc));
		
		Element eleOrderLine = null;
		Element eleOrderLines = null;
		Element eleOrderStatus = null;
		String strDeliveryMethod = null;
		String strOrderLineKey = null;
		int iNlOrderLineLength = 0;
		
		//Setting ENV object for AcademyReleaseIntServer : BEGIN
		/**
		 * Below env object is set because this java class removes the BOPIS lines from the input and returns the modified document.
		 *  The actions following this action get the modified document as input hence resulting in wrong input for processing.
		 *  This env object will be used by the actions to operate on the original onsuccess document
		 */
		log.verbose("Setting TXN Object for release");
		Document docClone=XMLUtil.cloneDocument(inDoc);
		env.setTxnObject(AcademyConstants.STR_SCH_SUCCESS_DOC, docClone);
		//Setting ENV object for AcademyReleaseIntServer : END
			
		
		NodeList nlOrderLine = inDoc.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
		iNlOrderLineLength = nlOrderLine.getLength();
		
		for (int i = 0; i < iNlOrderLineLength; i++) {			
			eleOrderLine = (Element) nlOrderLine.item(i);			
			strDeliveryMethod = eleOrderLine.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
			strOrderLineKey = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			log.verbose("Removing BOPIS orderline with OrderLineKey :: "+strOrderLineKey);
			
			if(AcademyConstants.STR_PICK_DELIVERY_METHOD.equals(strDeliveryMethod)){	
				//Remove BOPIS <OrderLine> element
				eleOrderLine.getParentNode().removeChild(eleOrderLine);
				i--;
				iNlOrderLineLength--;
				
				eleOrderStatus = (Element) XPathUtil.getNode(inDoc, "/Order/OrderStatuses/OrderStatus[@OrderLineKey='" + strOrderLineKey + "']");
				
				if (!YFCObject.isVoid(eleOrderStatus)){
					//Remove BOPIS <OrderStatus> element	
					eleOrderStatus.getParentNode().removeChild(eleOrderStatus);
				}				
			}
		}
		
		eleOrderLines = (Element) inDoc.getElementsByTagName(AcademyConstants.ELE_ORDER_LINES).item(0);
		//To get the count of Non-BOPIS Orderlines
		eleOrderLines.setAttribute(AcademyConstants.ATTR_NO_OF_ORDERLINES, Integer.toString(iNlOrderLineLength));
		
		log.verbose("Exiting AcademyRemoveBopisLines.removeBOPISOrderLines() :: "+XMLUtil.getXMLString(inDoc));
		
		return inDoc;
	}
	
	
}
