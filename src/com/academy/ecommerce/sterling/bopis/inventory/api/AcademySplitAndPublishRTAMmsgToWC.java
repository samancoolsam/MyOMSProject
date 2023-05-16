package com.academy.ecommerce.sterling.bopis.inventory.api;

import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @Author : Chiranthan(SapientRazorfish_)
 * @JIRA# : BOPIS-114 – OMS-WCS Inventory Management (RTAM)
 * @Date : Created on 25-07-2018
 * 
 * @Purpose : 
 * This class splits RTAM message into Global level & Node level messages & posts it to 2 different queues. 
 * 		- Global Level inventory msg : AvailabilityChange with Node='005' && DeliveryMethod='', goes to existing queue.
 * 		- Node Level inventory msg : AvailabilityChange elements with DeliveryMethod='PICK', goes to new queue.		
 *   	  
 **/

public class AcademySplitAndPublishRTAMmsgToWC {
	
	private static Logger log = Logger.getLogger(AcademySplitAndPublishRTAMmsgToWC.class.getName());
	private Properties props;
	public void setProperties(Properties props) throws Exception {		
		this.props = props;
	}
	
	/** This method clones the inDoc and removes all NodeLevel eleAvailabilityChange from inDoc & removes globalLevel eleAvailabilityChange from clonedDoc.
	 * @param env
	 * @param inDoc
	 */
	public void splitAndPublishMsgToWC(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Entering AcademySplitAndPublishRTAMmsgToWC.splitRTAMPublishMsg() :: "+XMLUtil.getXMLString(inDoc));		
		Document docNodeLevelAvailability = null;
		Element eleAvailabilityChange = null;
		Element eleAvailbltyChange = null;
		NodeList nlAvailabilityChange = null;
		NodeList nlAvailbltyChange = null;
		String strDeliveryMethod = null;
		String strNode = null;
		int iAvailabilityChangeLength = 0;
		int iAvailbltyChangeLength = 0;
		String strIsItemResvFlagUpdReq = null;
		//OMNI-6363 : begin
		Document docDCNodeLevelAvailability = null;
		Element eleAvailabilitChg = null;
		NodeList nlAvailabilityChg = null;
		int iAvailabilityChangeLen = 0;
		//OMNI-6363 : End
		try{
			docNodeLevelAvailability = cloneDocument(inDoc);
			//OMNI-6363 : Start
			docDCNodeLevelAvailability = cloneDocument(inDoc);
			//OMNI-6363 : End
			//Publish Global Level Inventory Information to WCS :
			//Loop through all 'AvailabilityChange' element & remove all node level AvailabilityChange elements to publish only global level inventory info
			nlAvailabilityChange = inDoc.getElementsByTagName(AcademyConstants.ATTR_AVAILABILITY_CHANGE);
			iAvailabilityChangeLength = nlAvailabilityChange.getLength();
			for(int i=0; i < iAvailabilityChangeLength; i++){
				eleAvailabilityChange = (Element) nlAvailabilityChange.item(i);
				strDeliveryMethod = eleAvailabilityChange.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
				
				if(AcademyConstants.STR_PICK_DELIVERY_METHOD.equals(strDeliveryMethod)
						//OMNI-6363 : begin
						|| (AcademyConstants.STR_SHIP_DELIVERY_METHOD.equals(strDeliveryMethod))){
						//OMNI-6363 : End
					eleAvailabilityChange.getParentNode().removeChild(eleAvailabilityChange);
					i--;
					iAvailabilityChangeLength--;
				}
			}
			log.verbose("After removing NodeLevel Availability elements :: "+XMLUtil.getXMLString(inDoc));
			
			if(inDoc.getElementsByTagName(AcademyConstants.ATTR_AVAILABILITY_CHANGE).getLength() > 0){
				AcademyUtil.invokeService(env, AcademyConstants.SERVICE_PUBLISH_GLOBAL_LEVEL_INVENTORY_TO_WCS, inDoc);
				log.verbose("Successfully sent Global Level inventory info to WCS");
								
			}
			
			//Publish Node Level Inventory Information to WCS :
			//Loop through all 'AvailabilityChange' element & remove Global level AvailabilityChange element to publish only node level inventory info
			nlAvailbltyChange = docNodeLevelAvailability.getElementsByTagName(AcademyConstants.ATTR_AVAILABILITY_CHANGE);
			iAvailbltyChangeLength = nlAvailbltyChange.getLength();
			for(int j=0; j < iAvailbltyChangeLength; j++){
				eleAvailbltyChange = (Element) nlAvailbltyChange.item(j);
				strDeliveryMethod = eleAvailbltyChange.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
				strNode = eleAvailbltyChange.getAttribute(AcademyConstants.ATTR_NODE);
				
				if(AcademyConstants.STR_GLOBAL_LEVEL_DEFAULT_NODE.equals(strNode) && YFCCommon.isVoid(strDeliveryMethod) 
						|| (AcademyConstants.STR_SHIP_DELIVERY_METHOD.equals(strDeliveryMethod))){					
					eleAvailbltyChange.getParentNode().removeChild(eleAvailbltyChange);
					j--;
					iAvailbltyChangeLength--;
				}
			}
			log.verbose("After removing GlovalLevel Availability element :: "+XMLUtil.getXMLString(docNodeLevelAvailability));	
			
			if(docNodeLevelAvailability.getElementsByTagName(AcademyConstants.ATTR_AVAILABILITY_CHANGE).getLength() > 0){
				AcademyUtil.invokeService(env, AcademyConstants.SERVICE_PUBLISH_NODE_LEVEL_INVENTORY_TO_WCS, docNodeLevelAvailability);
				log.verbose("Successfully sent Node Level inventory info to WCS");
			}			
			
			// Start OMNI-6363 STS Inventory Changes
			nlAvailabilityChg = docDCNodeLevelAvailability.getElementsByTagName(AcademyConstants.ATTR_AVAILABILITY_CHANGE);
			iAvailabilityChangeLen = nlAvailabilityChg.getLength();
			for(int k=0; k < iAvailabilityChangeLen; k++){
				eleAvailabilitChg = (Element) nlAvailabilityChg.item(k);
				strDeliveryMethod = eleAvailabilitChg.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
				strNode = eleAvailabilitChg.getAttribute(AcademyConstants.ATTR_NODE);
				
				if(AcademyConstants.STR_GLOBAL_LEVEL_DEFAULT_NODE.equals(strNode) && YFCCommon.isVoid(strDeliveryMethod)
						|| (AcademyConstants.STR_PICK_DELIVERY_METHOD.equals(strDeliveryMethod))){					
					eleAvailabilitChg.getParentNode().removeChild(eleAvailabilitChg);
					k--;
					iAvailabilityChangeLen--;
				}
			}
			log.verbose("After removing GlovalLevel and Pick NodeLevel Availability element :: "+XMLUtil.getXMLString(docDCNodeLevelAvailability));	
			
			if(docDCNodeLevelAvailability.getElementsByTagName(AcademyConstants.ATTR_AVAILABILITY_CHANGE).getLength() > 0){
				AcademyUtil.invokeService(env, AcademyConstants.SERVICE_PUBLISH_NODE_LEVEL_INVENTORY_TO_WCS, docDCNodeLevelAvailability);
				log.verbose("Successfully sent STS DC Level inventory info to WCS");
			}			
			// End OMNI-6363 
		}catch (Exception e){
			e.printStackTrace();			
			throw new YFSException(e.getMessage(),"RTAM0001", "Exception Ocuured while Splitting RTAM msg - AcademySplitAndPublishRTAMmsgToWC");
		}		
		log.verbose("Exiting AcademySplitAndPublishRTAMmsgToWC.splitRTAMPublishMsg()");
	}

	
	/** This method clones the inDoc & returns the cloned document.
	 * @param docIn
	 * @return clonedDocument
	 */
	private Document cloneDocument(Document docIn) throws Exception{
		log.verbose("Entering AcademySplitAndPublishRTAMmsgToWC.cloneDocument()"+XMLUtil.getXMLString(docIn));
		
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();		
	    Node originalRoot = docIn.getDocumentElement();
	    Document clonedDocument = docBuilder.newDocument();
	    Node copiedRoot = clonedDocument.importNode(originalRoot, true);
	    clonedDocument.appendChild(copiedRoot);

		log.verbose("Exiting AcademySplitAndPublishRTAMmsgToWC.cloneDocument() :: "+XMLUtil.getXMLString(clonedDocument));
		
	    return clonedDocument;
	}
}
