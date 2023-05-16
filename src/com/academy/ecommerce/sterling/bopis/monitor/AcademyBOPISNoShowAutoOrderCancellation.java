package com.academy.ecommerce.sterling.bopis.monitor;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @Author : Neeti(SapientRazorfish_)
 * @JIRA# : BOPIS-<>
 * @Date : Created on 26-Jun-2018
 * 
 * @Purpose : 
 *This class is invoked as part of monitoring rule to form the input and send it to.
 *
 *MR4 : ‘Auto Cancellation with Regular SLA Monitoring Rule’ 
 *MR5 : ‘Auto Cancellation with Extended SLA Monitoring Rule’  
 **/


public class AcademyBOPISNoShowAutoOrderCancellation implements YIFCustomApi {

	private static Logger log = Logger.getLogger(AcademyBOPISNoShowAutoOrderCancellation.class.getName());

	private Properties props;

	public void setProperties(Properties props) throws Exception {
		
		this.props = props;
	}
	
	public Document processOrderCancellation (YFSEnvironment env, Document inDoc) throws Exception {		
		log.verbose("Entering AcademyBOPISNoShowAutoOrderCancellation.processOrderCancellation() :: "+XMLUtil.getXMLString(inDoc));
		
		
		String strDeliveryMethod= null;
		String strShipNode= null;
		String strShipmentNo=null;
		String strStatus=null;
		String strShipmentKey=null;
		Document ReturnDoc= XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleRoot = ReturnDoc.getDocumentElement();
		Element EleShipmentLines= ReturnDoc.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
		eleRoot.appendChild(EleShipmentLines);
		
		try{			
			strShipmentKey = XPathUtil.getString(inDoc.getDocumentElement(), AcademyConstants.XPATH_MONITOR_SHIPMENT_SHIPMENTKEY);
			strDeliveryMethod=	XPathUtil.getString(inDoc.getDocumentElement(),AcademyConstants.XPATH_MONITOR_SHIPMENT_DELIVERY_METHOD);
			strShipNode= XPathUtil.getString(inDoc.getDocumentElement(),AcademyConstants.XPATH_MONITOR_SHIPMENT_SHIPNODE);
			strShipmentNo= XPathUtil.getString(inDoc.getDocumentElement(),AcademyConstants.XPATH_MONITOR_SHIPMENT_SHIPMENT_NO);
			strStatus=XPathUtil.getString(inDoc.getDocumentElement(),AcademyConstants.XPATH_MONITOR_SHIPMENT_STATUS);
			strShipmentKey=XPathUtil.getString(inDoc.getDocumentElement(),AcademyConstants.XPATH_MONITOR_SHIPMENT_SHIPMENTKEY);
			
			
			
		//	System.out.println("strShipmentKey=" + strShipmentKey);
   		    log.verbose("strShipmentKey=" + strShipmentKey);
   	//	    System.out.println("strDeliveryMethod=" + strDeliveryMethod);
		    log.verbose("strDeliveryMethod=" + strDeliveryMethod);
   		 
	//	    System.out.println("strShipNode=" + strShipNode);
		    log.verbose("strShipNode=" + strShipNode);
	//	    System.out.println("strShipmentNo=" + strShipmentNo);
		    log.verbose("strShipmentNo=" + strShipmentNo);
	//	    System.out.println("strStatus=" + strStatus);
		    log.verbose("strStatus=" + strStatus);
	//	    System.out.println("strShipmentKey=" + strShipmentKey);
		    log.verbose("strShipmentKey=" + strShipmentKey);
   		 
			eleRoot.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,strShipmentKey);
			eleRoot.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD,strDeliveryMethod);
			eleRoot.setAttribute(AcademyConstants.ATTR_SHIP_NODE,strShipNode);
			eleRoot.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO,strShipmentNo);
			eleRoot.setAttribute(AcademyConstants.ATTR_STATUS,strStatus);
			
			NodeList nlShipmentLine = inDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			for (int i=0;i < nlShipmentLine.getLength(); i++)
			{
				Element EleNLShipmentLine = (Element) nlShipmentLine.item(i);
				String strquantity=EleNLShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
				
				Double dquantity =  Double.parseDouble(strquantity);
				if (dquantity>0.00){
				String strShipmentLineKey=EleNLShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
				 
				Element EleShipmentLine= ReturnDoc.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
				EleShipmentLines.appendChild(EleShipmentLine);
				EleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, strquantity);
				EleShipmentLine.setAttribute(AcademyConstants.ATTR_SHORTAGE_QTY,strquantity);
				EleShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, strShipmentLineKey);
				Element EleExtn= ReturnDoc.createElement(AcademyConstants.ELE_EXTN);
				EleShipmentLine.appendChild(EleExtn);
				EleExtn.setAttribute(AcademyConstants.ATTR_EXTN_MSG_TO_SIM, AcademyConstants.STR_YES);
				EleExtn.setAttribute(AcademyConstants.ATTR_EXTN_REASON_CODE,props.getProperty(AcademyConstants.ATTR_REASON_CODE));
				}
			}
		//	 System.out.println("ReturnDoc=" + XMLUtil.getXMLString(ReturnDoc));
			    log.verbose("ReturnDoc=" + XMLUtil.getXMLString(ReturnDoc)); 
			AcademyUtil.invokeService(env,AcademyConstants.ACADEMY_STORE_RECORD_SHORTAGE_SERVICE,ReturnDoc);
					
		}
		catch (Exception e){
			e.printStackTrace();			
			throw new YFSException(e.getMessage(), 
					"MTR0002", "Failure occurred while cancelling the order");
		}
		log.verbose("Exiting AcademyBOPISNoShowAutoOrderCancellation.processOrderCancellation() :: "+XMLUtil.getXMLString(inDoc));
		
		return ReturnDoc;
	}
	

	
}
