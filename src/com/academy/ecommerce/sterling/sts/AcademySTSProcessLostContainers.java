package com.academy.ecommerce.sterling.sts;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
//import com.academy.ecommerce.sterling.sts.AcademySTSReceiveTOContainer;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;

import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**************************************************************************
 * File Name		: AcademySTSProcessLostContainers
 *
 * Description	    : OMNI-8091. The class handles the container lost scenarios from
 * the WEB SOM
 *checks whether container is received or not. If received then will unreceive the
 *container and mark it as lost. If container is not lost then that container is marked
 *as lost. Also if Sales order is not cancelled then invoking AcademyCancelSalesOrderService
 *service to cancel the associated sales line/qty.
 *
 * --------------------------------
 * 	Date             Author               
 * --------------------------------
 *  10-Jun-2020      Cognizant			 	 
 * 
 * -------------------------------------------------------------------------
 **************************************************************************/

public class AcademySTSProcessLostContainers {
	
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSProcessLostContainers.class);
	
    /*
     * This method is used to mark the container as lost and cancel the SO quantity 
     * by invoking AcademyCancelSalesOrderService service
     * @param env
     * @param inDoc
     * 
     */
	public Document processLostContainers(YFSEnvironment env, Document inDoc)
	{
		log.beginTimer("AcademySTSProcessLostContainers.processLostContainers()");
		log.verbose("Input to processLostContainers method: " +XMLUtil.getXMLString(inDoc));
		
		Boolean bUpdateIsReceivedFlag = false;
		
		Element eleContainer = inDoc.getDocumentElement();
		
		Element eleShipment = XmlUtils.getChildElement(eleContainer, AcademyConstants.ELE_SHIPMENT);
		
		Element eleStatus = XmlUtils.getChildElement(eleShipment, AcademyConstants.STATUS);
		
		String strShipmentStatus = eleStatus.getAttribute(AcademyConstants.ATTR_STATUS);
		String strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		
		Element eleExtn = XmlUtils.getChildElement(eleContainer,AcademyConstants.ELE_EXTN);
		String strIsSOCancelled = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_IS_SO_CANCELLED);
		String strExtnArrivedAtStore = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_ARRIVED_ATSTORE);
		String strIsReceived  = eleContainer.getAttribute(AcademyConstants.ATTR_IS_RECEIVED);
		
		Integer iShipmentStatus = Integer.parseInt(strShipmentStatus.substring(0,4));
		
		if((!strExtnArrivedAtStore.equals(AcademyConstants.STR_YES)) && (!strIsReceived.equals(AcademyConstants.STR_YES)) && (!strIsSOCancelled.equals(AcademyConstants.STR_YES)) && (strShipmentStatus.equals(AcademyConstants.VAL_READY_TO_SHIP_TO_STORE_STATUS)))
		{
			log.verbose("Inside Ready TO Ship To Store check");
			AcademySTSReceiveTOContainer stsReceiveTOContainer = new AcademySTSReceiveTOContainer();
			try {
				
				stsReceiveTOContainer.confirmTOShipment(env, strShipmentKey);
				
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FactoryConfigurationError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			receiveOrderAsLost(env, eleContainer);
			
			//Code Changes for OMNI-66585 -- Start
			sendShrinkUpdateToSIM(env, inDoc);
			//Code Changes for OMNI-66585 -- End
		}
		
		//if((strExtnArrivedAtStore.equals("Y")) && (!strIsReceived.equals("Y")) && (!strIsSOCancelled.equals("Y")) && (strShipmentStatus.equals("1400")))
		if((!strIsSOCancelled.equals(AcademyConstants.STR_YES)) && (iShipmentStatus >= 1400 && iShipmentStatus <=1600))
		{
			log.verbose("Inside Status < 1600");
			if(!strIsReceived.equals(AcademyConstants.STR_YES))
			{
				receiveOrderAsLost(env, eleContainer);
				
				//Code Changes for OMNI-66585 -- Start
				sendShrinkUpdateToSIM(env, inDoc);
				//Code Changes for OMNI-66585 -- End
			}
			else {
				bUpdateIsReceivedFlag = true;
				if(!strShipmentStatus.equals("1600.001"))
				{ 
					NodeList nlContainerDetails = eleContainer.getElementsByTagName(AcademyConstants.CONTAINER_DETL_ELEMENT);
					// Element eleContainerDetail = (Element) nlContainerDetails.item(0);
					String strReceivingNode = eleShipment.getAttribute(AcademyConstants.ATTR_RECV_NODE);
				   
					try {
						unreceiveOrder(env, nlContainerDetails, strShipmentKey, strReceivingNode);
						prepareAndInvokeChageOrderStatus(env, nlContainerDetails);
						if(nlContainerDetails.getLength() >1) {
							for(int index=0; index< nlContainerDetails.getLength(); index++) {
								Element eleContainerDetail = (Element)nlContainerDetails.item(index);
								Element eleContainerShipment = (Element)eleContainer.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
								receiveLineAsLost(env, eleContainerDetail, eleContainerShipment);
							}
							
						}
						else
						{
							receiveOrderAsLost(env, eleContainer);
							//Code Changes for OMNI-66585 -- Start
							sendShrinkUpdateToSIM(env, inDoc);
							//Code Changes for OMNI-66585 -- End
						}
						 
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
				
		}
		
		if((!strIsSOCancelled.equals(AcademyConstants.STR_YES)) && (!strShipmentStatus.equals(AcademyConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL)))
		{
			log.verbose("Inside Cancel");
			try {
				cancelSOLine(env, eleContainer, bUpdateIsReceivedFlag);
				
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		log.beginTimer("AcademySTSProcessLostContainers.processLostContainers()");
		
		
		return inDoc;
	}
	
	/*
     * This method is used to invoke reeiveOrder api in order to
     * mark the container as lost
     * @param env
     * @param eleContainer
     * 
     */
	public void receiveOrderAsLost(YFSEnvironment env , Element eleContainer)
	{
		log.beginTimer("AcademySTSProcessLostContainers.receiveOrderAsLost()");
		log.verbose("Input to receiveOrderAsLost: " + XMLUtil.getElementXMLString(eleContainer));
		try {
			Document docGetContainerDetail =  eleContainer.getOwnerDocument();
					//XmlUtils.createDocument(AcademyConstants.ELE_CONTAINER);
			Element eleGetContainerDetail = docGetContainerDetail.getDocumentElement();
			//Element eleShipmentContainer =  docGetContainerList.createElement(AcademyConstants.ELE_CONTAINER);
			//eleShipmentContainer =(Element) docGetContainerList.importNode(eleContainer, true);
			eleGetContainerDetail.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_CANCEL);
			//eleGetContainerList.appendChild(eleShipmentContainer);
			log.verbose("Input to receive Order code:" +XMLUtil.getXMLString(docGetContainerDetail));
			
			AcademySTSCloseTOReceipt stsCloseTOReceipt = new AcademySTSCloseTOReceipt();
			stsCloseTOReceipt.receiveTOContainer(env, docGetContainerDetail, "");
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.endTimer("AcademySTSProcessLostContainers.receiveOrderAsLost()");
	}
	
	/*
     * This method is used to invoke unreceiveOrder api in order to
     * unreceive the container in case of any container is received
     * @param env
     * @param nlContainerDetails
     * @param strShipmentKey
     * @param strReceivingNode
     * @throws Exception
     * 
     */
	public void unreceiveOrder(YFSEnvironment env, NodeList nlContainerDetails, String strShipmentKey, String strReceivingNode) throws Exception
	{
		log.beginTimer("AcademySTSProcessLostContainers.unreceiveOrder()");
		
		Document docUnreceiveOrderInput = XmlUtils.createDocument(AcademyConstants.ELE_RECPT);
		Element eleUnreceiveOrderInput = docUnreceiveOrderInput.getDocumentElement();
		Element eleUnreceiveReceiptLines = docUnreceiveOrderInput.createElement(AcademyConstants.RECEIPT_LINES);
		try {
				for(int c=0; c<nlContainerDetails.getLength(); c++) {
					
					Element eleContainerDetail = (Element) nlContainerDetails.item(c);
					log.verbose("Container detail element : " +XMLUtil.getElementXMLString(eleContainerDetail));
					String strShipmentLineKey = eleContainerDetail.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
					String strShipmentContainerKey = eleContainerDetail.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY);
					Document docReceiptlineList = getReceiptLineDetails(env, strShipmentLineKey, strReceivingNode);
					NodeList nlReceiptLineList = docReceiptlineList.getElementsByTagName(AcademyConstants.RECEIPT_LINE);
					
					for (int index=0; index<nlReceiptLineList.getLength(); index++) {
						
						Element eleReceiptLine = (Element) nlReceiptLineList.item(index);
						Element eleExtnReceiptLine = XmlUtils.getChildElement(eleReceiptLine, AcademyConstants.ELE_EXTN);
						String strExtnShipmentContainerKey = eleExtnReceiptLine.getAttribute(AcademyConstants.ATTR_EXTN_SHP_CONT_KEY);
						String strReceiptLineKey = eleReceiptLine.getAttribute(AcademyConstants.ATTR_RECEIPT_LINE_KEY);
						String strRLShipmentLineKey = eleReceiptLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
						String strQty = eleContainerDetail.getAttribute(AcademyConstants.ATTR_QUANTITY);	
						log.verbose(strRLShipmentLineKey +" " + strShipmentLineKey);
						log.verbose(strExtnShipmentContainerKey +" " + strShipmentContainerKey);
						if(strRLShipmentLineKey.equals(strShipmentLineKey))
						{
							if(!YFCObject.isVoid(strExtnShipmentContainerKey) && (strShipmentContainerKey.equals(strExtnShipmentContainerKey))) {
								 log.verbose("Inside Container Shipment Key" +strExtnShipmentContainerKey);
									Element eleUnreceiveReceiptLine = docUnreceiveOrderInput.createElement(AcademyConstants.RECEIPT_LINE);
									eleUnreceiveReceiptLine.setAttribute(AcademyConstants.ATTR_RECEIPT_LINE_KEY, strReceiptLineKey);
									eleUnreceiveReceiptLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, strShipmentLineKey);
									eleUnreceiveReceiptLine.setAttribute(AcademyConstants.ATTR_UNRECEIVE_QUANTITY, strQty);
									eleUnreceiveReceiptLines.appendChild(eleUnreceiveReceiptLine);					
							}
					   		
						}
				}
			}
	       eleUnreceiveOrderInput.appendChild(eleUnreceiveReceiptLines);
			NodeList nlReceiptLineIn = docUnreceiveOrderInput.getElementsByTagName(AcademyConstants.RECEIPT_LINE);
			log.verbose("Receipt Line List: " +nlReceiptLineIn.getLength());
			
			if(nlReceiptLineIn.getLength()>0)
			{
				eleUnreceiveOrderInput.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE);
				eleUnreceiveOrderInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
				eleUnreceiveOrderInput.setAttribute(AcademyConstants.ATTR_RECV_NODE, strReceivingNode);
				eleUnreceiveOrderInput.setAttribute("OverrideModificationRules", AcademyConstants.ATTR_Y);
				
				
				log.verbose("unReceiveOrder: "+XMLUtil.getXMLString(docUnreceiveOrderInput));
				AcademyUtil.invokeAPI(env, AcademyConstants.API_UNRECEIVE_ORDER, docUnreceiveOrderInput);
				
			}
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	log.endTimer("AcademySTSProcessLostContainers.unreceiveOrder()");	
	}

    /*
     * This method is used to invoke getReceiptLineList api to get the receiptLineKey
     * which is used during unreceiveOrder api call
     * @param env
     * @param strShipmentLineKey
     * @param strReceivingNode
     * @throws FactoryConfigurationError, Exception
     * 
     */
	public Document getReceiptLineDetails(YFSEnvironment env, String strShipmentLineKey, String strReceivingNode) throws FactoryConfigurationError, Exception
	{
		log.beginTimer("AcademySTSProcessLostContainers.getReceiptLineDetails()");
		Document docReceiptLineListIn = XmlUtils.createDocument("ReceiptLine");
		Element eleReceiptLineListIn = docReceiptLineListIn.getDocumentElement();
		eleReceiptLineListIn.setAttribute(AcademyConstants.ATTR_RECV_NODE, strReceivingNode);
		eleReceiptLineListIn.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, strShipmentLineKey);
		log.verbose("Get Receipt Line List Input: " + XMLUtil.getXMLString(docReceiptLineListIn));
		
		Document receiptLineListOutputTemplate = YFCDocument.getDocumentFor("<ReceiptLineList> <ReceiptLine ReceiptLineKey='' OrderLineKey='' ShipmentLineKey=''> <Extn ExtnShipmentContainerKey=''/> </ReceiptLine> </ReceiptLineList>").getDocument();
		env.setApiTemplate("getReceiptLineList", receiptLineListOutputTemplate);
		Document receiptLineListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_RECEIPT_LINE_LIST, docReceiptLineListIn);
		env.clearApiTemplate("getReceiptLineList");
		log.verbose("Get Receipt Line List Output: " +XMLUtil.getXMLString(receiptLineListOutput));
		log.endTimer("AcademySTSProcessLostContainers.getReceiptLineDetails()");
		return receiptLineListOutput;
	
	}

	public void updateContainerIsReceivedFlag(YFSEnvironment env, Element eleContainerIn)
	{
		log.beginTimer("AcademySTSProcessLostContainers.updateContainerIsReceivedFlag()");
		try {
			Document docChangeShipmentIn = XmlUtils.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element eleChangeShipment = docChangeShipmentIn.getDocumentElement();
			Element	eleContainers = docChangeShipmentIn.createElement(AcademyConstants.ELE_CONTAINERS);
			Element eleContainer = docChangeShipmentIn.createElement(AcademyConstants.ELE_CONTAINER);
			
			Element eleShipment = XmlUtils.getChildElement(eleContainerIn, AcademyConstants.ELE_SHIPMENT);
			String strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			
			String strContainerNo = eleContainerIn.getAttribute(AcademyConstants.ATTR_CONTAINER_NO);
			
			Element eleContainerDetail = (Element) eleContainerIn.getElementsByTagName(AcademyConstants.CONTAINER_DETL_ELEMENT).item(0);
			
			String strShipmentContainerKey= eleContainerDetail.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY);
			
			eleChangeShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			eleContainer.setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY, strShipmentContainerKey);
			eleContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_NO, strContainerNo);
			eleContainer.setAttribute(AcademyConstants.ATTR_IS_RECEIVED, AcademyConstants.ATTR_Y);
			
			
			eleContainers.appendChild(eleContainer);
			eleChangeShipment.appendChild(eleContainers);
			
			log.verbose("changeShipment api input:" +XMLUtil.getXMLString(docChangeShipmentIn));
			
			AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipmentIn);
			
			
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.endTimer("AcademySTSProcessLostContainers.updateContainerZoneAsLost()");
	}
	
	/*
	 * This method is used to invoke changeOrderStatus api to move
	 * the orderline status from unreceived to temp unreceived
	 * @param env
	 * @param nlContainerDetails
	 * @throws Exception
	 * 
	 */
	public void prepareAndInvokeChageOrderStatus(YFSEnvironment env, NodeList nlContainerDetails) throws Exception
	{
		log.beginTimer("AcademySTSProcessLostContainers.prepareAndInvokeChageOrderStatus()");
		String strOrderHeaderKey="";
		try {
			Document docChangeOrderStatus  = XmlUtils.createDocument(AcademyConstants.ELE_ORD_STATUS_CHG);
			Element eleChangeOrderStatus = docChangeOrderStatus.getDocumentElement();
			Element eleChangeOrderStatusLines = docChangeOrderStatus.createElement(AcademyConstants.ELEM_ORDER_LINES);
			for(int index=0; index< nlContainerDetails.getLength(); index++)
			{
				Element eleContainerDetail = (Element) nlContainerDetails.item(index);
				log.verbose("Container Detail element: " + XMLUtil.getElementXMLString(eleContainerDetail));
				
				Element eleContainerShipmentLine = XmlUtils.getChildElement(eleContainerDetail, AcademyConstants.ELE_SHIPMENT_LINE);
				
				Element eleContainerOrderLine = XmlUtils.getChildElement(eleContainerShipmentLine, AcademyConstants.ELE_ORDER_LINE);
				String strOrderLineKey = eleContainerOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
				String strOrderReleaseKey= eleContainerShipmentLine.getAttribute(AcademyConstants.ATTR_RELEASE_KEY);
				String strQty = eleContainerDetail.getAttribute(AcademyConstants.ATTR_QUANTITY);
				strOrderHeaderKey = eleContainerDetail.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
				log.verbose("Create Change Order line");
				Element eleChangeOrderStatusLine = docChangeOrderStatus.createElement(AcademyConstants.ELE_ORDER_LINE);
				eleChangeOrderStatusLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, strOrderLineKey);
				eleChangeOrderStatusLine.setAttribute(AcademyConstants.ATTR_RELEASE_KEY, strOrderReleaseKey);
				eleChangeOrderStatusLine.setAttribute(AcademyConstants.ATTR_QUANTITY, strQty);
				
				eleChangeOrderStatusLines.appendChild(eleChangeOrderStatusLine);
			}
			eleChangeOrderStatus.appendChild(eleChangeOrderStatusLines);
			NodeList nlChangeOrderStatusLine = eleChangeOrderStatus.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
			log.verbose("Change Order Status Lines: " +nlChangeOrderStatusLine.getLength());
			if(nlChangeOrderStatusLine.getLength() >0)
			{
				eleChangeOrderStatus.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
				eleChangeOrderStatus.setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS, "3700.050");
				eleChangeOrderStatus.setAttribute(AcademyConstants.ATTR_TRANS_ID, AcademyConstants.STR_TEMP_UNRECEIVED_0006_EX);
				
				log.verbose("Change Order Status:" +XMLUtil.getXMLString(docChangeOrderStatus));
				AcademyUtil.invokeAPI(env,AcademyConstants.API_CHG_ORD_STATUS, docChangeOrderStatus);
				
			}
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.endTimer("AcademySTSProcessLostContainers.prepareAndInvokeChageOrderStatus()");
	}
	
	/*
	 * This is method is used to invoke AcademyCancelSalesOrderService service
	 * to cancel the Sales order quantity
	 * Input to the AcademyCancelSalesOrderService service
	 * <Container ShipmentContainerKey>
	 *	<SalesOrder OrderHeaderKey="" >
	 *	<OrderLines>
	 *	<OrderLine OrderLineKey="" ChangeInOrderedQty="" />
	 *	</OrderLines>
	 *	</SalesOrder>
	 *	</Container>
	 * @param  env
	 * @param  eleContainer
	 * @param  bUpdateIsReceivedFlag
	 * @throws ParserConfigurationException, Exception
	 */
	public void cancelSOLine(YFSEnvironment env, Element eleContainer, Boolean bUpdateIsReceivedFlag) throws ParserConfigurationException, Exception
	{
		log.beginTimer("AcademySTSProcessLostContainers.cancelSOLine()");
		Document docSOCancellationIn = XmlUtils.createDocument(AcademyConstants.ELE_CONTAINER);
		Element eleContainerIn = docSOCancellationIn.getDocumentElement();
		Element eleSalesOrder = docSOCancellationIn.createElement(AcademyConstants.ELE_SALES_ORDER);
		Element eleOrderLines = docSOCancellationIn.createElement(AcademyConstants.ELEM_ORDER_LINES);
		String strShipmentContainerKey = "";
		String strOrderHeaderKey="";
		String strShipmentKey="";
		NodeList nlContainerDetailsList = eleContainer.getElementsByTagName(AcademyConstants.CONTAINER_DETL_ELEMENT);
	    Document docGetOrderListOut = null;
		for(int index=0; index<nlContainerDetailsList.getLength(); index++)
		{
			Element eleContainerDetail = (Element) nlContainerDetailsList.item(index);
	        Element eleContainerShipmentLine = XmlUtils.getChildElement(eleContainerDetail, AcademyConstants.ELE_SHIPMENT_LINE);
	        Element eleContainerOrderLine = XmlUtils.getChildElement(eleContainerShipmentLine, AcademyConstants.ELEM_ORDER_LINE);
	        Element eleChainedFromOrderLine = XmlUtils.getChildElement(eleContainerOrderLine, AcademyConstants.ELE_CHAINED_FROM_ORDER_LINE);
	        strOrderHeaderKey = eleChainedFromOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
	        strShipmentContainerKey = eleContainerDetail.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY);
	        strShipmentKey = eleContainerDetail.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
	        String strOrderLineKey = eleChainedFromOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
	        String strContainerQty = eleContainerDetail.getAttribute(AcademyConstants.ATTR_QUANTITY);
	        if(YFCObject.isVoid(docGetOrderListOut)) {
	        	
	        	docGetOrderListOut = prepareAndInvokeGetOrderList(env, strOrderHeaderKey);
	        	
	        }
	        NodeList nlOrderLines = docGetOrderListOut.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINE);
	        for(int ol =0 ; ol< nlOrderLines.getLength(); ol++)
	        {
	        	Element eleOrderListOrderLine = (Element) nlOrderLines.item(ol);
	        	String strOrderListOrderLineKey = eleOrderListOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
	        	if(strOrderListOrderLineKey.equals(strOrderLineKey))
	        	{
	        		log.verbose("Order Line Keys: " +strOrderLineKey +" List: " +strOrderListOrderLineKey);
	        		String strOrderedQty = eleOrderListOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
	    	        
	    	        Double dContainerQty = Double.parseDouble(strContainerQty);
	    	        Double dOrderedQty = Double.parseDouble(strOrderedQty);
	    	        Double dChangedInOrderedQty = dOrderedQty - dContainerQty;
	    	        log.verbose("OrderedQty: "+ strOrderedQty +" ContainerQty: " +strContainerQty + " ChangedInOrderedQty: " + dChangedInOrderedQty);
	    	        String strChangedInOrderedQty = dChangedInOrderedQty.toString();
	    	        Element eleOrderLine = docSOCancellationIn.createElement(AcademyConstants.ELE_ORDER_LINE);
	    	        eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, strOrderListOrderLineKey);
	    	        eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, strChangedInOrderedQty);
	    	        log.verbose("Order Line: " +XMLUtil.getElementXMLString(eleOrderLine));
	    	        eleOrderLines.appendChild(eleOrderLine);
	    	       
	        		
	        	}
	        }
	        
		}
		
		eleSalesOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
		eleContainerIn.setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY, strShipmentContainerKey);
		eleContainerIn.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
		eleSalesOrder.appendChild(eleOrderLines);
		eleContainerIn.appendChild(eleSalesOrder);
		
		if(bUpdateIsReceivedFlag)
		{
			eleContainerIn.setAttribute(AcademyConstants.ATTR_IS_RECEIVED, AcademyConstants.STR_YES);
		}
		
		log.verbose("Input for SO Cancellation Service " +XMLUtil.getXMLString(docSOCancellationIn));
		AcademyUtil.invokeService(env,AcademyConstants.ACADEMY_CANCEL_SALES_ORDER_SERVICE, docSOCancellationIn);
		log.endTimer("AcademySTSProcessLostContainers.cancelSOLine()");
		
	}
	
	
	/*
	 * This method is used to invoke getOrderList api inorder to get
	 * correct OrderedQty for each order line
	 * @param env
	 * @param strOrderHeaderKey
	 * @throws ParserConfigurationException, Exception
	 */
	public Document prepareAndInvokeGetOrderList(YFSEnvironment env, String strOrderHeaderKey) throws ParserConfigurationException, Exception
	{
		log.beginTimer("AcademySTSProcessLostContainers.prepareAndInvokeGetOrderList()");
		Document docGetOrderListOut  = null;
		Document docGetOrderListIn = XmlUtils.createDocument(AcademyConstants.ELE_ORDER);
		Element eleGetOrderListIn = docGetOrderListIn.getDocumentElement();
		eleGetOrderListIn.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
		Document receiptLineListOutputTemplate = YFCDocument.getDocumentFor("<OrderList> <Order OrderHeaderKey=''> <OrderLines> <OrderLine OrderedQty='' OrderLineKey=''/> </OrderLines> </Order> </OrderList>").getDocument();
		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, receiptLineListOutputTemplate);
		 docGetOrderListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, docGetOrderListIn);
		 env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
		 log.verbose("Input for getOrderList api " +XMLUtil.getXMLString(docGetOrderListOut));
	
		log.endTimer("AcademySTSProcessLostContainers.prepareAndInvokeGetOrderList()");
		return docGetOrderListOut;
	}
	
	/*
	 * This method is used to invoke receiveOrder api for each line
	 * and mark that line as lost
	 * @param env
	 * @param eleContainerDetail
	 * @param eleShipment
	 * @throws ParserConfigurationException, Exception
	 * 
	 */
	public void receiveLineAsLost(YFSEnvironment env, Element eleContainerDetail, Element eleShipment) throws ParserConfigurationException, Exception{
		
		log.beginTimer("AcademySTSProcessLostContainers.receiveLineAsLost()");
		Document docReceiveOrderInput =  XmlUtils.createDocument(AcademyConstants.ELE_RECPT);
		Element eleReceipt = docReceiveOrderInput.getDocumentElement();
		
		
		eleReceipt.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE);
		eleReceipt.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
		eleReceipt.setAttribute(AcademyConstants.ATTR_RECV_NODE, eleShipment.getAttribute(AcademyConstants.ATTR_RECV_NODE));
		eleReceipt.setAttribute(AcademyConstants.ATTR_PALLET_ID, eleShipment.getAttribute(AcademyConstants.ATTR_ORDER_NO));

		Element eleReceiptLines = XmlUtils.createChild(eleReceipt, AcademyConstants.RECEIPT_LINES);
		Element eleReceiptLine = XmlUtils.createChild(eleReceiptLines, AcademyConstants.RECEIPT_LINE);

		eleReceiptLine.setAttribute(AcademyConstants.ATTR_DISPOSITION_CODE, AcademyConstants.STR_LOST);
		
		String strQty = eleContainerDetail.getAttribute(AcademyConstants.ATTR_QUANTITY);
		String strShipmentContainerKey = eleContainerDetail.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY);
		Element eleShipmentLine = (Element)eleContainerDetail.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE).item(0);
		log.verbose("Shipment Line element: " + XMLUtil.getElementXMLString(eleShipmentLine));
		if(!YFCObject.isVoid(eleShipmentLine))
		{
			String strShipmentLineKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
			String strOrderLineKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			
			eleReceiptLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, strShipmentLineKey);
			eleReceiptLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, strOrderLineKey);
			eleReceiptLine.setAttribute(AcademyConstants.ATTR_QUANTITY, strQty);
			Element eleReceiptLineExtn = XmlUtils.createChild(eleReceiptLine, AcademyConstants.ELE_EXTN);
			eleReceiptLineExtn.setAttribute(AcademyConstants.ATTR_EXTN_SHP_CONT_KEY, strShipmentContainerKey);
			log.verbose("***receiveOrder input***" +XMLUtil.getXMLString(docReceiveOrderInput));

			Document docReceiveOrderOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_RECEIVE_ORDER, docReceiveOrderInput);
			log.verbose("***receiveOrder output***" +XMLUtil.getXMLString(docReceiveOrderOutput));
			
		}
		
		log.endTimer("AcademySTSProcessLostContainers.receiveLineAsLost()");
		
	}
	
	// Code Changes for OMNI-66585 -- Start
	private void sendShrinkUpdateToSIM(YFSEnvironment env, Document inDoc) {
		Element eleContainer = inDoc.getDocumentElement();
		AcademySTS2LostContainerUpdatesToSIM sendUpdatesToSIM = new AcademySTS2LostContainerUpdatesToSIM();
		try {
			String strLostReason = eleContainer.getAttribute("AstraCode");
			if (!YFCObject.isVoid(strLostReason)) {
				sendUpdatesToSIM.sts2LostContainerUpdatesToSIM(env, inDoc);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// Code Changes for OMNI-66585 -- End
	

}
