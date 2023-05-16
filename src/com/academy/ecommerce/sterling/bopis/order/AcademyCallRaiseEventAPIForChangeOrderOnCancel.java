package com.academy.ecommerce.sterling.bopis.order;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
/**
 * @author neeladha
 *
 */
public class AcademyCallRaiseEventAPIForChangeOrderOnCancel {
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademyCallRaiseEventAPIForChangeOrderOnCancel.class);
	/** This method will process the message from the JMS queue and will call raiseEvent api to call change order on cancel event
	 * @param env
	 * @param inXML
	 * @return
	 */
	public  Document raiseEventForChangeOrderOnCancel (YFSEnvironment env, Document inXML) 
	   {
		log.beginTimer("AcademyCallRaiseEventAPIForChangeOrderOnCancel::raiseEventForChangeOrderOnCancel");
		log.verbose("Entering the method AcademyCallRaiseEventAPIForChangeOrderOnCancel.raiseEventForChangeOrderOnCancel ");
		try
		{
			String strOrderHeaderKey = null;
			Document docgetOrderListOutput = null;
			Document docgetOrderListInput =null;
			Document docRaiseEventInput = null;
			Element eleRaiseEvent = null;
			Element elegetOrderListInput = null;
			strOrderHeaderKey= inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
			docgetOrderListInput= XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			elegetOrderListInput = docgetOrderListInput.getDocumentElement();
			elegetOrderListInput.setAttribute(AcademyConstants.ATTR_DOC_TYPE,AcademyConstants.SALES_DOCUMENT_TYPE);
			elegetOrderListInput.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,AcademyConstants.PRIMARY_ENTERPRISE);
			elegetOrderListInput.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
			log.verbose("Calling AcademyCallgetOrderListAPI Service with input : =" +XMLUtil.getXMLString(docgetOrderListInput));
			//System.out.println("Calling AcademyCallgetOrderListAPI Service with input : =" +XMLUtil.getXMLString(docgetOrderListInput));
			docgetOrderListOutput = AcademyUtil.invokeService(env,AcademyConstants.ACADEMY_CALL_GET_ORDER_LIST_SERVICE,docgetOrderListInput);
			Element elegetOrderListOutput=(Element) docgetOrderListOutput.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
			
			String raiseEventPart1 = "<RaiseEvent TransactionId=\"ORDER_CHANGE\" EventId=\"ON_CANCEL\"><DataType>1</DataType><XMLData><![CDATA[";
			String raiseEventPart2 = "]]></XMLData></RaiseEvent>";
			docRaiseEventInput= XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
	    	eleRaiseEvent = docRaiseEventInput.getDocumentElement();
	    	XMLUtil.copyElement(docRaiseEventInput, elegetOrderListOutput, eleRaiseEvent);
	    	//System.out.println("eleRaiseEvent"+XMLUtil.getElementXMLString(eleRaiseEvent));
	    	log.verbose("eleRaiseEvent"+XMLUtil.getElementXMLString(eleRaiseEvent));
	    	Document raiseEventInputDoc = XMLUtil.getDocument(raiseEventPart1 + XMLUtil.getXMLString(docRaiseEventInput) + raiseEventPart2);
	    	
	    	log.verbose("Calling raiseEvent API  with input : =" +XMLUtil.getXMLString(raiseEventInputDoc));
		//	System.out.println("Calling raiseEvent API  with input : =" +XMLUtil.getXMLString(raiseEventInputDoc));
	    	AcademyUtil.invokeAPI(env,AcademyConstants.API_ACADEMY_RAISE_EVENT,raiseEventInputDoc);
				   	
	    	
		}
		catch (Exception e) {
			log.error(e);
			throw new YFSException("Exception in the method AcademyCallRaiseEventAPIForChangeOrderOnCancel.raiseEventForChangeOrderOnCancel-Method" +e.getMessage());
		}
		log.endTimer("AcademyCallRaiseEventAPIForChangeOrderOnCancel::raiseEventForChangeOrderOnCancel");
		return inXML;
	   }

}
