package com.academy.ecommerce.sterling.bopis.api;

import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**#########################################################################################
*
* Project Name                : OMS_Nov_08_2019_Rel1
* Module                      : OMNI-488
* Author                      : Radhakrishna Mediboina(C0015568)
* Author Group				  : CTS-POD
* Date                        : 23-Oct-2019 
* Description				  : This class publish auto BOPIS push notifications(Reminder & Escalation) to ESB queue.
* 								 
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       		Remarks/Description                      
* ---------------------------------------------------------------------------------
* 23-Oct-2019		CTS  	 			  1.0           	Updated version
*
* #########################################################################################*/

public class AcademySendPushNotification implements YIFCustomApi {
private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademySendPushNotification.class);
	
	//Define properties to fetch argument value from service configuration
    private Properties props;
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}
	
	/**
	 * This method will send an add/remove push notification to the MQQT through ESB.
	 * @param env
	 * @param inXML
	 * @return pushDoc
	 */
	public Document sendAddRemoveNotification (YFSEnvironment env, Document inXML) throws ParserConfigurationException   {

		log.beginTimer("AcademySendPushNotification::sendAddRemoveNotification()");
		log.verbose("Entering the method AcademySendPushNotification.sendAddRemoveNotification()");
			
		Element eleShipment = null;
		Element eleShipmentLine = null;
		//Element eleOrderLine = null;
		String strShipNode = null;
		String strShipmentNo = null;
		String strOrderNo = null;
		
		eleShipment = inXML.getDocumentElement();
		//eleShipment = (Element) ele.getElementsByTagName("Shipment");
		strShipNode= eleShipment.getAttribute(AcademyConstants.SHIP_NODE);
		strShipmentNo= eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		eleShipmentLine = (Element) eleShipment.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE).item(0);
		strOrderNo = eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_NO) ;
			
		String notType = props.getProperty(AcademyConstants.ELE_SHIPEMNT_NOT_TYPE);
		Document pushDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleRoot=pushDoc.getDocumentElement();
		eleRoot.setAttribute(AcademyConstants.SHIP_NODE, strShipNode);
		eleRoot.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		eleRoot.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		eleRoot.setAttribute(AcademyConstants.ELE_SHIPEMNT_CONTENT_TITLE, notType );
		
		log.verbose("The message send to pushDoc :: " +XMLUtil.getXMLString(pushDoc));
		log.endTimer("AcademySendPushNotification::sendAddRemoveNotification()");
		
		return pushDoc;
	
	}
	
	/**
	 * This method will send an Reminder/Escalation push notification to the MQQT through ESB.
	 * @param env
	 * @param inXML
	 * @return pushDoc
	 */	
	public Document sendReminderEscalationNotification (YFSEnvironment env,Document inDoc) throws ParserConfigurationException{
		
		log.beginTimer("AcademySendPushNotification::sendReminderEscalationNotification()");
		log.verbose("Begin the method AcademySendPushNotification.sendReminderEscalationNotification()");
		
		Element eleShipment = null;
		String strShipNode = null;
		String strShipmentNo = null;
		String strOrderNo = null;
		Document pushDoc = null;
		
		//OMNI-488 :: Start
		log.verbose("Indoc Received for sendReminderEscalationNotification metthod: "+XMLUtil.getXMLString(inDoc));
		eleShipment = (Element) inDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
		strShipNode = eleShipment.getAttribute(AcademyConstants.SHIP_NODE);
		strShipmentNo = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		strOrderNo = eleShipment.getAttribute(AcademyConstants.ATTR_ORDER_NO) ;
		//OMNI-488 :: End
		String notType = props.getProperty(AcademyConstants.ELE_SHIPEMNT_NOT_TYPE);
		
		//Prepare push document
		pushDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleRoot = pushDoc.getDocumentElement();
		eleRoot.setAttribute(AcademyConstants.SHIP_NODE, strShipNode);
		eleRoot.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		eleRoot.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		eleRoot.setAttribute(AcademyConstants.ELE_SHIPEMNT_CONTENT_TITLE, notType);
		
		log.verbose("The message send to ESB Queue :: "+XMLUtil.getXMLString(pushDoc));
		
		log.endTimer("AcademySendPushNotification::sendReminderEscalationNotification()");
		log.verbose("End the method AcademySendPushNotification.sendReminderEscalationNotification()");
		
		return pushDoc;
	} 
}