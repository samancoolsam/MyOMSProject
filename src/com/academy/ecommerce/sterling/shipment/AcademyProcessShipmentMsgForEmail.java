package com.academy.ecommerce.sterling.shipment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.academy.util.common.AcademyBOPISUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @Author Cognizant
 * @Date Created 20/01/2020
 * 
 * JIRAS - OMNI-2401 and OMNI-2402
 * @Purpose
 * 1.Call manageTaskQueue - Creating taskq for sending Shipment mail to customer. To follow the existing email process, we insert record into TaskQ table. ACADEMY_EMAIL_AGENT_SERVER sends out the emails
 * 
 * Input to manageTaskQueue api
 * 
 * <TaskQueue DataKey='201912190419552127203822' DataType="ShipmentKey" TransactionId='SEND_EMAIL_ON_INVOICE.0001.ex'/>
 * 
  **/

public class AcademyProcessShipmentMsgForEmail implements YIFCustomApi {

	private Properties props;
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyProcessShipmentMsgForEmail.class);
	
	/**
	 * Preparing input for manageTaskQueue API
	 * @param strTaskQDataKey
	 * @return docInManageTaskQueue
	 * @throws Exception
	 */
	public Document prepareManageTaskQueueInput(YFSEnvironment env, Document inXML)
			throws Exception {
		Document docInManageTaskQueue = null;
		Element eleInManageTaskQueue = null;
		String strTransactionKey = "";
		String strTaskQDataKey=null;
		strTaskQDataKey=inXML.getDocumentElement().getAttribute(AcademyConstants.SHIPMENT_KEY);
		//OMNI-40848 Start partial fulfillment email changes
		String strConsolidateAfterHr="";
		String strOrderHeaderKey=inXML.getDocumentElement().getAttribute(AcademyConstants.STR_ORDR_HDR_KEY);
		String sChainedOrderHeaderKey=XMLUtil.getAttributeFromXPath(inXML, "/OrderInvoice/Shipment/ShipmentLines/ShipmentLine/@ChainedFromOrderHeaderKey");
     	if(!YFCCommon.isVoid(sChainedOrderHeaderKey)){
     		log.verbose("DSV order-fetch SO order header key");
     		strOrderHeaderKey =sChainedOrderHeaderKey;
     	}
		String strTransactionID = props.getProperty(AcademyConstants.ATTR_TRANS_ID);
		log.verbose("Transaction ID *************::" + strTransactionID);
		docInManageTaskQueue = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
		eleInManageTaskQueue = docInManageTaskQueue.getDocumentElement();
		log.verbose("strOrderHeaderKey *************::" + strOrderHeaderKey);
		//OMNI-51288 start changes - check and filter  BOPIS shipment from partial fulfillment email
		 String sDeliveryMethod = XMLUtil.getString(inXML,"/OrderInvoice/Shipment/@DeliveryMethod");
		//if("Y".equals(sSTHPartialFulfillmentListrakEmailEnabled) && isPartialFulfillment(env, strOrderHeaderKey)) {
		//removed LISTRAK_EMAIL_CODES as part of OMNI-90636
		 if(isPartialFulfillment(env, strOrderHeaderKey) && (!YFCCommon.isVoid(sDeliveryMethod) && !sDeliveryMethod.equals(AcademyConstants.STR_PICK))) {
		//OMNI-51288 End changes - check and filter  BOPIS shipment from partial fulfillment email
			 eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE, AcademyConstants.STR_ORDR_HDR_KEY);
			eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY, strOrderHeaderKey);
			//set available date to future for partial fulfillment email to consolidate
			strConsolidateAfterHr = props.getProperty("PartialFulfilmentConsolidationTimeInHr");
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
			cal.add(Calendar.HOUR,Integer.parseInt(strConsolidateAfterHr));
			String strAvailableDate = sdf.format(cal.getTime());
			log.verbose("Available date for partial fulfillment "+strAvailableDate);
			eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, strAvailableDate);
		}else {
			eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY, strTaskQDataKey);
			eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE, AcademyConstants.SHIPMENT_KEY);
		}
		//eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY, strTaskQDataKey);
		//eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE, AcademyConstants.SHIPMENT_KEY);
		//OMNI-40848 End partial fulfillment email changes
		eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_TRANS_ID, strTransactionID);
		
		//eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_TRANS_KEY, AcademyConstants.SEND_EMAIL_ON_INVOICE_TRANID);
		
		log.verbose("Manage TaskQueue input: " +XMLUtil.getXMLString(docInManageTaskQueue));
		return docInManageTaskQueue;
		
	}

	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		this.props = props;
	}
	
	//OMNI-40848 Partial Fulfillment email changes 
	private boolean isPartialFulfillment(YFSEnvironment env, String sOrderHeaderKey) throws Exception {
		Document getShipmentListForOrderIndoc=XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		getShipmentListForOrderIndoc.getDocumentElement().setAttribute(AcademyConstants.STR_ORDR_HDR_KEY ,sOrderHeaderKey);
		Document templateDocGetShipmentListForOrder = XMLUtil.getDocument("<ShipmentList>\r\n"
				+ "<Shipment DocumentType=\"\" DeliveryMethod=\"\" ShipmentNo=\"\" Status=\"\">\r\n"
				+ "<ShipmentLines><ShipmentLine ShipmentLineKey=\"\"><OrderLine MaxLineStatus=\"\" MinLineStatus=\"\" FulfillmentType=\"\"></OrderLine></ShipmentLine></ShipmentLines>\r\n"
				+ "</Shipment>\r\n"
				+ "</ShipmentList>");
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENTLIST_FORORDER, templateDocGetShipmentListForOrder);	
		Document outXML = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENTLIST_FORORDER, getShipmentListForOrderIndoc);
		log.verbose("isPartialFulfillment - getShipmentListForOrder - " + XMLUtil.getXMLString(outXML));
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENTLIST_FORORDER);
		Element eleShipmentList=outXML.getDocumentElement();
		NodeList nlSOFShipmentLines = XPathUtil.getNodeList(eleShipmentList, "/ShipmentList/Shipment[./ShipmentLines/ShipmentLine[./OrderLine/@FulfillmentType='SOF']]");
 		if(nlSOFShipmentLines.getLength()>=1) {
 			log.verbose("SOF shipment exist on order, send confirm shipment email");
	    	return false;
 		}
	    NodeList nlShipments = XPathUtil.getNodeList(eleShipmentList, "/ShipmentList/Shipment[@DeliveryMethod='SHP' and @Status != '9000'  and (@DocumentType='0001' or @DocumentType='0005')]");
	    log.verbose("No of STH shipments " +nlShipments.getLength());
	    if(nlShipments.getLength()==0) {
	    	log.verbose("No STH shipments on order");
	    	return false;
	    }
	    if(nlShipments.getLength()==1) //order contain only 1 sth shipment
	        {
	    	Element eleShipment = (Element) nlShipments.item(0);
	    		String sShipemntNo=eleShipment.getAttribute("ShipmentNo");
	    		log.verbose(XMLUtil.getElementXMLString(eleShipment));
	     		NodeList nlShipmentLines = XPathUtil.getNodeList(eleShipment, "//Shipment[@ShipmentNo='"+sShipemntNo+"']/ShipmentLines/ShipmentLine[./OrderLine/@MinLineStatus!='3700' and ./OrderLine/@MinLineStatus!='3700.001']");
	     		if(nlShipmentLines.getLength()>0) { //order contain only 1 sth shipment but partially fulfilled
	    			log.verbose("Partial fulfillment::::::");
	    			return true;
	    		} else {
	    			log.verbose("Full Shipment");
	    			return false;
	    		}
	        }else {
	        	log.verbose("Order contain more than one valid STH shipments");
	      	   return true;
	        }
	}
		
		public String fetchCommonCodeShortDesc(Document docOutGetCommonCodeList,String listrakFlag) throws Exception {
			Element eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList,
					"/CommonCodeList/CommonCode[@CodeValue='" + listrakFlag + "']");
			String sCodeShortDesc = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
			log.verbose("codeShortDesc :: "+sCodeShortDesc);
			return sCodeShortDesc;
			
		}
		
}
