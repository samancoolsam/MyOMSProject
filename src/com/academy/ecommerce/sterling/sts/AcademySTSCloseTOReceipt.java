package com.academy.ecommerce.sterling.sts;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**************************************************************************
 * File Name		: AcademySTSCloseTOReceipt
 *
 * Description	    : OMNI-6581. The class starts receipt,receive the shipment lines and close the receipt
 * 
 * Input XML to the class:
 * 
 * <AcadSTSTOContainer BatchNo="1000000025" ClosedFlag="N" ContainerNo="134800004"
 *   MaximumRecords="5000" ShipmentContainerKey="202006020613312160698334"
 *  ShipmentKey="202006020609222160697949" Status="New" StoreNo="134" TOOrderNo="Y159767989"/>
 *  
 * ------------------------------------------------------------------------
 * 	Date           		Version   			Author               
 * -------------------------------------------------------------------------
 *  10-JUN-2020     	Initial				Cognizant			 	 
 *  07-APR-2021			Updated 			Cognizant (Radhakrishna Mediboina)
 * -------------------------------------------------------------------------
 **************************************************************************/
public class AcademySTSCloseTOReceipt implements YIFCustomApi {

	/**
	 * log variable.
	 */
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSCloseTOReceipt.class);
	
	//OMNI-32358: STS 2.0 - Start
	//Define properties to fetch service level argument values
    private Properties props;
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}
	//OMNI-32358: STS 2.0 - Start
	
	public Document processSTSTOReceipt(YFSEnvironment env, Document inXML) throws ParserConfigurationException{
		log.beginTimer("AcademySTSCloseTOReceipt.processSTSTOReceipt()");
		log.verbose("Start - Inside AcademySTSCloseTOReceipt.processSTSTOReceipt()");

		//Declarations		
		Document docGetShipmentContDetOut = null;
		Document outDocStartReceipt = null;
		Document outDocReceiveOrder = null;		
		Element eContainer = null;				
		String sContainerNo = "";
		String sShpCtnrKey ="";
		String sShpKey = "";

		if (!YFCObject.isVoid(inXML.getDocumentElement())) 
		{
			log.verbose("Input xml:: " + XMLUtil.getXMLString(inXML));	

			sContainerNo = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_CONTAINER_NO);
			sShpCtnrKey = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY);
			sShpKey =  inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);

			try {
				//fetch getShipmentContainerDetails
				docGetShipmentContDetOut = getTOShipmentContainerDetails(env, inXML);
				eContainer = docGetShipmentContDetOut.getDocumentElement();					
				String sExtnIsSoCancelled=XMLUtil.getAttributeFromXPath(docGetShipmentContDetOut, "/Container/Extn/@ExtnIsSOCancelled");
				String sShpStatus = XPathUtil.getString(eContainer, AcademyConstants.XPATH_SHIP_STATUS);		

				if (sShpStatus.equals("1400")) {
					//Form input and invoke startReceipt API
					outDocStartReceipt = startTOReceipt(env, docGetShipmentContDetOut);	
				}
				String sRcptHdrKey = XMLUtil.getString(outDocStartReceipt, AcademyConstants.XPATH_RECEIPT_RECEIPTHEADERKEY);

				//Form input and invoke receiveOrder API
				outDocReceiveOrder = receiveTOContainer(env, docGetShipmentContDetOut, sRcptHdrKey);

				//method invoked to update IsReceived flag in shipment container table.				
				// OMNI - 60804 & 60806 - added sExtnIsSoCancelled
				changeShipmentToReceive(env, sContainerNo, sShpCtnrKey, sShpKey, sExtnIsSoCancelled);
				
				//OMNI-51882: Start Change :STS 2.0 - Inventory - Demand Update on close receipt
				 String sNodeType=XMLUtil.getAttributeFromXPath(docGetShipmentContDetOut, "/Container/Shipment/ShipNode/@NodeType");
			     String sFulfillmentType=XMLUtil.getAttributeFromXPath(docGetShipmentContDetOut, "/Container/ContainerDetails/ContainerDetail/ShipmentLine/OrderLine/ChainedFromOrderLine/@FulfillmentType");
			        if (!YFCObject.isVoid(sNodeType) && sNodeType.equals("Store") && !YFCObject.isVoid(sFulfillmentType) &&  sFulfillmentType.equals("STS") ) {
						AcademyUtil.invokeService(env,"AcademyPostDemandUpdateToSIMOnCloseReceipt", docGetShipmentContDetOut);
			        }
				//OMNI-51882: End Change :STS 2.0 - Inventory - Demand Update on close receipt
								
			}catch (Exception e) {

				log.error("Exception Occured in fetching the container"
						+ e.getMessage());
				throw new YFSException(new YFCException(e).getXMLErrorBuf());
			}

		}

		log.endTimer("AcademySTSCloseTOReceipt.processSTSTOReceipt()");
		return inXML;

	}


	/**
	 * Method invoked from the service AcademySTSCloseTOReceipt to execute closeReceipt 
	 * for the TO shipment
	 * @param env
	 * @param inXML
	 * @param sRcptHdrKey
	 * @throws ParserConfigurationException
	 * @throws Exception
	 */
	public Document closeTOReceipt(YFSEnvironment env, Document inXML)
			throws Exception {
		log.beginTimer("AcademySTSCloseTOReceipt.closeTOReceipt()");
		log.verbose("Input xml:: " + XMLUtil.getXMLString(inXML));

		Document outDocCloseReceipt = null;
		String sTOMinOrderStatus;
		String sTOMaxOrderStatus = "";		
		String sTOMinOrderLineStatus;
		String sTOMaxOrderLineStatus = "";
		
		String strPackListType = "";
		int totalQuantity = 0;
		int totalReceiptQty = 0;
		String shipKey = "";
		try {
			//Order status 
			sTOMinOrderStatus = XPathUtil.getString(inXML, "/Receipt/ReceiptLines/ReceiptLine/OrderLine/Order/@MinOrderStatus");
			sTOMaxOrderStatus = XPathUtil.getString(inXML, "/Receipt/ReceiptLines/ReceiptLine/OrderLine/Order/@MaxOrderStatus");
			Double dbTOMinOrderStatus = Double.parseDouble(sTOMinOrderStatus);
			Double dbTOMaxOrderStatus = Double.parseDouble(sTOMaxOrderStatus);
			log.verbose("Min TO Status:: " + sTOMinOrderStatus); 
			log.verbose("Max TO Status:: " + sTOMaxOrderStatus); 
			//OrderLine status
			sTOMinOrderLineStatus = XPathUtil.getString(inXML, "/Receipt/ReceiptLines/ReceiptLine/OrderLine/@MinLineStatus");
			sTOMaxOrderLineStatus = XPathUtil.getString(inXML, "/Receipt/ReceiptLines/ReceiptLine/OrderLine/@MaxLineStatus");
			Double dbTOMinOrderLineStatus = Double.parseDouble(sTOMinOrderLineStatus);
			Double dbTOMaxOrderLineStatus = Double.parseDouble(sTOMaxOrderLineStatus);
			log.verbose("Min TO Line Status:: " + sTOMinOrderLineStatus); 
			log.verbose("Max TO Line Status:: " + sTOMaxOrderLineStatus); 

			Document inDocCloseReceipt = XMLUtil.createDocument(AcademyConstants.ELE_RECPT);
			Element eReceipt = inDocCloseReceipt.getDocumentElement();	
			eReceipt.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE);
			eReceipt.setAttribute(AcademyConstants.ATTR_RECEIPT_HDR_KEY, inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_RECEIPT_HDR_KEY));
			eReceipt.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
			eReceipt.setAttribute(AcademyConstants.ATTR_RECV_NODE, inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_RECV_NODE));
			log.verbose("***closeReceipt input***" +XMLUtil.getXMLString(inDocCloseReceipt));
			
			//OMNI-89303 - Changes to avoid closeReceipt for STS FA - if there is some qty yet to be received - start
			strPackListType = XPathUtil.getString(inXML, "/Receipt/Shipment/@PackListType");
			shipKey = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			if (AcademyConstants.STS_FA.equalsIgnoreCase(strPackListType)) {
				//OMNI-93544 - Creating reusable method 
				String finalQty = calculateReceiptQty(env, inXML, shipKey);
				String[] qty = finalQty.split(",");
				totalQuantity = Integer.parseInt(qty[0]);
				totalReceiptQty = Integer.parseInt(qty[1]);
				
				log.verbose(" totalQty is:: " + totalQuantity +
						"\n totalReceiptQty:: " + totalReceiptQty);
				
				if(totalQuantity == totalReceiptQty) {
					log.verbose("*****Condition statisfied to closeReceipt for STS FA *****");
					outDocCloseReceipt = AcademyUtil.invokeAPI(env, AcademyConstants.API_CLOSE_RCPT, inDocCloseReceipt);
				} else {
					log.verbose("Quantity is yet to be received for STS FA, So not going for closeReceipt");
				}
				//OMNI-89303 - Changes to avoid closeReceipt for STS FA - if there is some qty yet to be received - End
			} else if(((dbTOMinOrderLineStatus >= 3900 && dbTOMaxOrderLineStatus >= 3900)
					|| (dbTOMinOrderStatus >= 3900 && dbTOMaxOrderStatus >= 3900)) 
					&& !AcademyConstants.STS_FA.equalsIgnoreCase(strPackListType)) {
				log.verbose("*****Condition statisfied to closeReceipt*****");
				outDocCloseReceipt = AcademyUtil.invokeAPI(env, AcademyConstants.API_CLOSE_RCPT, inDocCloseReceipt);	
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//OMNI-32358: STS 2.0 - Start
		manageTaskQRecordForTOCloseShipment(env,inXML);
		//OMNI-32358: STS 2.0 - Start
		log.endTimer("AcademySTSCloseTOReceipt.closeTOReceipt()");
		return outDocCloseReceipt;
	}
	//OMNI-93544 - Creating reusable method 
	public static String calculateReceiptQty(YFSEnvironment env, Document receiveOutput, String shipKey) throws Exception {
		String strTotalQuantity = "";
		int intTotalQuantity = 0;
		int totalQuantity = 0;
		
		String strReceiptQty = "";
		int intReceiptQty = 0;
		int totalReceiptQty = 0;
		
		Document docGetReceiptListIn = null;
		Document docGetReceiptListOut = null;
		
		docGetReceiptListIn = XMLUtil.createDocument(AcademyConstants.ELE_RECEIPT);
		Element eleReceipt = docGetReceiptListIn.getDocumentElement();
		eleReceipt.setAttribute(AcademyConstants.SHIPMENT_KEY, shipKey);				
		docGetReceiptListOut = AcademyUtil.invokeService(env, AcademyConstants.SERV_ACAD_GET_RECEIPT_LST_STS, docGetReceiptListIn);
		
		NodeList nlShipLine = receiveOutput.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		for (int i=0; i < nlShipLine.getLength(); i++) {
			Element eleShipLine = (Element) nlShipLine.item(i);
			strTotalQuantity = eleShipLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
			intTotalQuantity = getIntegerValue(strTotalQuantity);
			totalQuantity = totalQuantity + intTotalQuantity;					
		}
		log.verbose("totalQty is:: " + totalQuantity);
		
		NodeList nlReceiptLine = docGetReceiptListOut.getElementsByTagName(AcademyConstants.RECEIPT_LINE);
		for (int j=0; j < nlReceiptLine.getLength(); j++) {
			Element eleReceiptLine = (Element) nlReceiptLine.item(j);
			strReceiptQty = eleReceiptLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
			intReceiptQty = getIntegerValue(strReceiptQty);
			totalReceiptQty = totalReceiptQty + intReceiptQty;
		}
		log.verbose(" totalQty is:: " + totalQuantity +
				"\n totalReceiptQty:: " + totalReceiptQty);
		StringBuilder finalQty = new StringBuilder();
		finalQty.append(totalQuantity);
		finalQty.append(",");
		finalQty.append(totalReceiptQty);
		return finalQty.toString();
		
	}
	
	private static int getIntegerValue(String value) {
		int intValue = 0;
		if(!YFCObject.isVoid(value)) {
			intValue = Double.valueOf(value).intValue();
		}
		return intValue;
	}
	

	//OMNI-32358: STS 2.0 - Start
	/**
	 * Method invoked to update AvailableDate for TO closeShipment record in TaskQ on Receipt closed.
	 * @param env
	 * @param inXML
	 * @throws Exception	
	 */
	private void manageTaskQRecordForTOCloseShipment(YFSEnvironment env, Document inXML) throws Exception {
		log.endTimer("AcademySTSCloseTOReceipt.manageTaskQRecordForTOCloseShipment() - Start");
		Document docInManageTaskQueue = null;
		Element eleInManageTaskQueue = null;
		SimpleDateFormat sdfDateFormat = null;
		String strTaskQDataKey = null;
		String strCloseTOShipmentSLADays = null;
		String strNextAvailDate = null;
					
		strCloseTOShipmentSLADays = props.getProperty(AcademyConstants.STR_CLOSE_SHIPMENT_SLA_DAYS);
		log.verbose("Service argument strCloseTOShipmentSLADays :: "+strCloseTOShipmentSLADays);
		//Get the SystemDate and Add SLA(configured value) as next available date.
		Calendar cal = Calendar.getInstance();
        sdfDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		log.verbose("manageTaskQRecordForTOCloseShipment() - SystemDate :: "+sdfDateFormat.format(cal.getTime()));
        cal.add(Calendar.DAY_OF_MONTH, Integer.parseInt(strCloseTOShipmentSLADays));
        strNextAvailDate = sdfDateFormat.format(cal.getTime());
		log.verbose("manageTaskQRecordForTOCloseShipment() - strNextAvailDate :: "+strNextAvailDate);
		strTaskQDataKey = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);		
		//manageTaskQueue API inDoc
		docInManageTaskQueue = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
		eleInManageTaskQueue = docInManageTaskQueue.getDocumentElement();
		eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, strNextAvailDate);
		eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY, strTaskQDataKey);
		eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE, AcademyConstants.ATTR_SHIPMENT_KEY);
		eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_TRANS_ID, AcademyConstants.ACAD_CLOSE_SHIPMENT_TRAN_ID);
		//Invoking the manageTaskQueue API
		log.verbose("Indoc to manageTaskQueue API :: "+XMLUtil.getXMLString(docInManageTaskQueue));					
		AcademyUtil.invokeAPI(env,AcademyConstants.API_MANAGE_TASK_QUEUE, docInManageTaskQueue);		
		log.endTimer("AcademySTSCloseTOReceipt.manageTaskQRecordForTOCloseShipment() - End");
	}
	//OMNI-32358: STS 2.0 - End

	
	/**
	 * Method invoked to update IsReceived flag in shipment container table.
	 * @param env
	 * @param inDocGetContainerList
	 * @param sContainerNo
	 * @param sContainerKey
	 * @param sShipmentKey
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError		
	 */
	private void changeShipmentToReceive(YFSEnvironment env, String sContainerNo,
			String sContainerKey, String sShipmentKey, String sExtnIsSOCancelled)
					throws ParserConfigurationException, FactoryConfigurationError {
		Document docChangeShipToReceive;
		docChangeShipToReceive = XmlUtils.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eShipment = docChangeShipToReceive.getDocumentElement();
		eShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, sShipmentKey);
		Element eContainers = XmlUtils.createChild(eShipment, AcademyConstants.ELE_CONTAINERS);
		Element eContainer = XmlUtils.createChild(eContainers, AcademyConstants.ELE_CONTAINER);
		eContainer.setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY, sContainerKey);
		eContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_NO, sContainerNo);
		eContainer.setAttribute(AcademyConstants.ATTR_IS_RECEIVED, AcademyConstants.ATTR_Y);
		//OMNI - 60806 & 60804 -Start- updating ExtnCancellationActionedAt to Receiving for cointaines whose sales orders are cancelled
		if(!YFSObject.isVoid(sExtnIsSOCancelled) && sExtnIsSOCancelled.equals(AcademyConstants.STR_YES)) {
			Element eExtn = XmlUtils.createChild(eContainer, AcademyConstants.ELE_EXTN);
			eExtn.setAttribute(AcademyConstants.ATTR_EXTN_CANCEL_ACTION, AcademyConstants.STR_RECEIVING);
		}
		//OMNI - 60806 & 60804 -End
		try {
			log.verbose("****change shipment to update received****" + XmlUtils.getString(docChangeShipToReceive));
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipToReceive);

		} catch (Exception e) {
			// TODO Auto-generated catch block		
			YFSException exception = new YFSException("Receiving failed.");
			exception.setErrorCode("Receiving failed.");
			throw exception;
		}
	}
	/**
	 * Method invoked to complete the receiving process for the TO shipment
	 * by invoking receiveOrder API.
	 * @param env
	 * @param inXML
	 * @param docGetShipmentContDetOut
	 * @param sRcptHdrKey
	 * @throws ParserConfigurationException
	 */
	public Document receiveTOContainer(YFSEnvironment env, Document docGetShipmentContDetOut,
			String sRcptHdrKey) throws ParserConfigurationException {
		log.beginTimer("AcademySTSCloseTOReceipt.receiveTOContainer()");

		Document outDocReceiveOrder = null;
		Element eReceipt = null;
		String sContainerQty = "";
		//Action is included to handle container lost scenario
		String sAction = "";

		Element eContainer = docGetShipmentContDetOut.getDocumentElement();
		Element eShipment = XmlUtils.getChildElement(eContainer, AcademyConstants.ELE_SHIPMENT);
		sAction = eContainer.getAttribute(AcademyConstants.ATTR_ACTION);

		Document inDocReceiveOrder = XMLUtil.createDocument(AcademyConstants.ELE_RECPT);
		eReceipt = inDocReceiveOrder.getDocumentElement();	
		eReceipt.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE);
		eReceipt.setAttribute(AcademyConstants.ATTR_RECEIPT_HDR_KEY, sRcptHdrKey);
		eReceipt.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, eShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
		eReceipt.setAttribute(AcademyConstants.ATTR_RECV_NODE, eShipment.getAttribute(AcademyConstants.ATTR_RECV_NODE));
		eReceipt.setAttribute(AcademyConstants.ATTR_PALLET_ID, eShipment.getAttribute(AcademyConstants.ATTR_ORDER_NO));

		Element eRcptLines = XmlUtils.createChild(eReceipt, AcademyConstants.RECEIPT_LINES);

		try {

			//Loop through containerDetail for receiptline attributes
			NodeList nlContainer = docGetShipmentContDetOut.getElementsByTagName(AcademyConstants.CONTAINER_DETL_ELEMENT);
			for(int i=0; i<nlContainer.getLength(); i++){
				Element eContainerDetl = (Element) nlContainer.item(i);
				sContainerQty = eContainerDetl.getAttribute(AcademyConstants.ATTR_QUANTITY);

				Element eRcptLine = XmlUtils.createChild(eRcptLines, AcademyConstants.RECEIPT_LINE);
				//Included to handle container lost scenario
				if(!YFSObject.isVoid(sAction) && sAction.equals(AcademyConstants.STR_CANCEL)) {
					eRcptLine.setAttribute(AcademyConstants.ATTR_DISPOSITION_CODE, AcademyConstants.STR_LOST);
				}
				//Included to handle container lost scenario
				eRcptLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, 
						XPathUtil.getString(eContainerDetl, AcademyConstants.XPATH_TO_SHIPLINE_SHPLINEKEY));
				eRcptLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, 
						XPathUtil.getString(eContainerDetl, AcademyConstants.XPATH_TO_SHIPLINE_ORDLINEKEY));
				eRcptLine.setAttribute(AcademyConstants.ATTR_QUANTITY, sContainerQty);
				Element eRcptLineExtn = XmlUtils.createChild(eRcptLine, AcademyConstants.ELE_EXTN);
				eRcptLineExtn.setAttribute(AcademyConstants.ATTR_EXTN_SHP_CONT_KEY, eContainerDetl.getAttribute(AcademyConstants.ATTR_SHIP_CONT_KEY));


			}
			log.verbose("***receiveOrder input***" +XMLUtil.getXMLString(inDocReceiveOrder));

			outDocReceiveOrder = AcademyUtil.invokeAPI(env, AcademyConstants.API_RECEIVE_ORDER, inDocReceiveOrder);
			log.verbose("***receiveOrder output***" +XMLUtil.getXMLString(outDocReceiveOrder));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.endTimer("AcademySTSCloseTOReceipt.receiveTOContainer()");
		return outDocReceiveOrder;
	}


	/**
	 * Method invokes the startReceipt API to start the receiving process
	 * for TO shipment
	 * @param env
	 * @param inXML
	 * @param sDCShipNode
	 * @throws ParserConfigurationException
	 * @throws Exception
	 */
	public Document startTOReceipt(YFSEnvironment env, Document docGetShipmentContDetOut)
			throws ParserConfigurationException {
		log.beginTimer("AcademySTSCloseTOReceipt.startTOReceipt()");
		
		Element eContainer = docGetShipmentContDetOut.getDocumentElement();
		Element eInShipment = XmlUtils.getChildElement(eContainer, AcademyConstants.ELE_SHIPMENT);
		
		Document outDocStartReceipt = null;
		Document inDocStartReceipt = XMLUtil.createDocument(AcademyConstants.ELE_RECPT);
		Element eReceipt = inDocStartReceipt.getDocumentElement();	
		eReceipt.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE);
		eReceipt.setAttribute(AcademyConstants.ATTR_OPEN_RECEIPT, "");
		eReceipt.setAttribute(AcademyConstants.ATTR_RECV_NODE, eInShipment.getAttribute(AcademyConstants.ATTR_RECV_NODE));
		
		Element eShipment = XmlUtils.createChild(eReceipt, AcademyConstants.ELE_SHIPMENT);
		eShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, eInShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
		eShipment.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		eShipment.setAttribute(AcademyConstants.ATTR_SHIP_NODE, eInShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE));

		log.verbose("***startReceipt input***" +XMLUtil.getXMLString(inDocStartReceipt));

		try {
			outDocStartReceipt = AcademyUtil.invokeAPI(env, AcademyConstants.API_START_RCPT, inDocStartReceipt);
			log.verbose("***startReceipt output***" +XMLUtil.getXMLString(outDocStartReceipt));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.endTimer("AcademySTSCloseTOReceipt.startTOReceipt()");
		return outDocStartReceipt;
	}


	/**
	 * Method is invoked to fetch the TO container details
	 * @param env
	 * @param inXML
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Document getTOShipmentContainerDetails(YFSEnvironment env, Document inXML) throws SAXException, IOException,
	ParserConfigurationException {
		log.beginTimer("AcademySTSCloseTOReceipt.getTOShipmentContainerDetails()");
		Document inDocGetContDet = null;
		Document docGetShipmentContDetOut = null;
		Element eContainer = null;
		try {
			inDocGetContDet = XMLUtil.createDocument(AcademyConstants.ELE_CONTAINER);

			eContainer = inDocGetContDet.getDocumentElement();				
			eContainer.setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY, 
					inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY));

			log.verbose("***getShipmentContainerDetails input***" +XMLUtil.getXMLString(inDocGetContDet));

			docGetShipmentContDetOut = AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACAD_GETSHIPMENT_CONT_DETAILS, inDocGetContDet);
			log.verbose("***getShipmentContainerDetails output template***" +XMLUtil.getXMLString(docGetShipmentContDetOut));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.endTimer("AcademySTSCloseTOReceipt.getTOShipmentContainerDetails()");
		return docGetShipmentContDetOut;
	}

	/**
	 * Method is invoked to fetch the TO shipment details
	 * @param env
	 * @param inXML
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws Exception
	 */
	private Document getTOShipmentDetails(YFSEnvironment env, Document inXML) throws SAXException, IOException,
	ParserConfigurationException {
		log.beginTimer("AcademySTSCloseTOReceipt.getTOShipmentDetails()");
		Document inDocGetDet = null;
		Document docGetShipmentDetOut = null;
		Element eShip = null;
		try {
			inDocGetDet = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);

			eShip = inDocGetDet.getDocumentElement();				
			eShip.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, 
					inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));


			String strGetShipmentDetTemplate = "<Shipment TotalQuantity='' ShipmentKey=''><Containers><Container IsReceived=''>"
					+ "<ContainerDetails><ContainerDetail Quantity='' ShipmentContainerKey=''>" 
					+ "<ShipmentLine OrderLineKey='' Quantity='' ShipmentLineKey=''/></ContainerDetail></ContainerDetails>"
					+ "</Container></Containers></Shipment>";


			Document docGetShipmentDetTemplate = XMLUtil.getDocument(strGetShipmentDetTemplate);

			env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_DETAILS, docGetShipmentDetTemplate);
			log.verbose("***getShipmentDetails input***" +XMLUtil.getXMLString(inDocGetDet));

			docGetShipmentDetOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_DETAILS, inDocGetDet);
			log.verbose("***getShipmentDetails output***" +XMLUtil.getXMLString(docGetShipmentDetOut));

			env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_DETAILS);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.endTimer("AcademySTSCloseTOReceipt.getTOShipmentDetails()");
		return docGetShipmentDetOut;
	}
	
}