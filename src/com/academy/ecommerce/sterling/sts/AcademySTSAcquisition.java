package com.academy.ecommerce.sterling.sts;

import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
/**************************************************************************
 * File Name		: AcademySTSAcquisition
 *
 * Description	    : OMNI-75596. OMNI-89303 The class receives the acquisition message from GSM for STS FA,
 * do startReceipt, receiveOrder and update Shipment with IsReceived.
 *  
 * ------------------------------------------------------------------------
 * 	Date           		Version   			Author               
 * -------------------------------------------------------------------------
 *  24-JUN-2022     	Initial				Everest Technologies			 	 
 * -------------------------------------------------------------------------
 */
public class AcademySTSAcquisition implements YIFCustomApi {
	
	/**
	 * log variable.
	 */
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSAcquisition.class);
	String strReceiptHdrKey="";
	String strAcqItemID = "";
	String strTOOrderNo = "";
	String strAcqQty = "";
	boolean bIsUpdateContainer = false;
	/**
	 * 
	 * <Shipment CustomerOrderNo="2022061406" ItemID="113815863" Quantity="1"
    	SerialNo="JNK5967" ShipNode="033" Status="A"
    	SterlingPONo="Y112756474" TrackingNo="273993993288"/>
	 * @param env
	 * @param docIn
	 * @return
	 * @throws ParserConfigurationException
	 */
	public Document processSTSAcquisitionFromGSM(YFSEnvironment env, Document docIn) {
		log.beginTimer("AcademySTSAcquisition.processSTSAcquisitionFromGSM()");
		log.verbose("Start - Inside AcademySTSAcquisition.processSTSAcquisitionFromGSM()" + XMLUtil.getXMLString(docIn));

		String strReceiptLineKey = "";
		String strReceiptStatus = "";
		String strReceiptQty = "";
		boolean bStartReceipt = false;
		Document docStartReceiptOut = null;
		Element eleReceipt = null;
		try {
			// invoke OrderList to get documentType, OHK, OLK
			Element eleShipmentListOutput = XMLUtil.getElementByXPath(docIn, "/Shipment/ShipmentListOutput/Shipment");
			Element eleShipments = XMLUtil.getElementByXPath(docIn, "/Shipment/ShipmentListOutput");
			String strDocumentType = eleShipmentListOutput.getAttribute(AcademyConstants.STR_DOCUMENT_TYPE);
			String strOHK = eleShipmentListOutput.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
			strAcqItemID = docIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_ITEM_ID);
			strAcqQty = docIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_QUANTITY);

			log.verbose("Document Type is:: " + strDocumentType + " and OHK is :: " + strOHK);
			if (AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE.equals(strDocumentType)) {
				docIn.getDocumentElement().setAttribute(AcademyConstants.STS_ORDER_TYPE, strDocumentType);
				Document docgetShipmentListForOrderOut = XMLUtil.createDocument(AcademyConstants.ATTR_SHIPMENT_LIST);
				Element eleGetShipmentList = docgetShipmentListForOrderOut.getDocumentElement();
				XMLUtil.copyElement(docgetShipmentListForOrderOut, eleShipments, eleGetShipmentList);

				Element eleShipmentLst = docgetShipmentListForOrderOut.getDocumentElement();
				Element eleShipment = XMLUtil.getFirstElementByName(eleShipmentLst, AcademyConstants.ELE_SHIPMENT);
				String strShipStatus = eleShipment.getAttribute(AcademyConstants.STATUS);
				String strShipmentKey = eleShipment.getAttribute(AcademyConstants.SHIPMENT_KEY);
				log.verbose("Status is:: " + strShipStatus + " and ShipmentKey is :: " + strShipmentKey);

				// invoke getReceiptList with ShipmentKey, OrderLineKey
				Document docgetReceiptLstOut = invokeGetReceiptList(env, strShipmentKey);
				if (docgetReceiptLstOut!= null) {
				Element eleReceiptLst = docgetReceiptLstOut.getDocumentElement();
				Element eleReceiptIn = XMLUtil.getFirstElementByName(eleReceiptLst, AcademyConstants.ELE_RECEIPT);
				if (!YFCObject.isVoid(eleReceiptIn)) {
					strReceiptStatus = eleReceiptIn.getAttribute(AcademyConstants.ATTR_STATUS);
					if (AcademyConstants.STS_RCPT_INPROGRESS.equals(strReceiptStatus) ||
							AcademyConstants.STS_RCPT_STARTED.equals(strReceiptStatus)) {
						log.verbose("Status is ReceiptInProgress/ ReceiptStarted, so ReceiveOrder");
						strReceiptHdrKey = eleReceiptIn.getAttribute(AcademyConstants.ATTR_RECPT_HEADERKEY);
						strReceiptLineKey = XMLUtil.getAttributeFromXPath(docgetReceiptLstOut,
								AcademyConstants.XPATH_RECEIPT_LINE_KEY);
						strReceiptQty = XMLUtil.getAttributeFromXPath(docgetReceiptLstOut,
								AcademyConstants.XPATH_RECEIPT_LINE_QUANTITY);
						log.verbose("Receipt Status:: " + strReceiptStatus + " and ReceiptHdrKey :: " + strReceiptHdrKey
								+ " and ReceiptLineKey ::" + strReceiptLineKey + " and strReceiptQty is :: "
								+ strReceiptQty);
						eleShipment.setAttribute(AcademyConstants.ATTR_RECEIPT_HDR_KEY, strReceiptHdrKey);
						eleShipment.setAttribute(AcademyConstants.ATTR_RECEIPT_LINE_KEY, strReceiptLineKey);
					} else if (AcademyConstants.STS_RCPT_RECEIVED.equals(strReceiptStatus)) {
						log.verbose(
								"Receipt exists and is received. So not going for > startReceipt and receiveOrder :: ");
						return docIn;
					} else {
						log.verbose("Receipt exists and not in ReceiptInProgress - 1300 - status: start Receipt");
						bStartReceipt = true;
					}
				} else if (AcademyConstants.STATUS_SHIPPED_TO_STORE.equalsIgnoreCase(strShipStatus)
						|| YFCObject.isVoid(eleReceiptIn)) {
					log.verbose("No receipt exists, so StartReceipt and ReceiveOrder:: " + strShipStatus);
					bStartReceipt = true;
				}
				if (bStartReceipt) {
					log.verbose("Status Receipt - Start");
					docStartReceiptOut = invokeStartReceipt(env, docgetShipmentListForOrderOut);
					if(docStartReceiptOut!=null) {
					eleReceipt = docStartReceiptOut.getDocumentElement();
					log.verbose("docStartReceiptOut:: \n" + XMLUtil.getXMLString(docStartReceiptOut));
					strReceiptHdrKey = eleReceipt.getAttribute(AcademyConstants.ATTR_RECPT_HEADERKEY);
					log.verbose("strReceiptHdrKey From startReceipt Output:: \n" + strReceiptHdrKey);
					}
				}
				invokeReceiveOrder(env, docgetShipmentListForOrderOut);
			} 
				}else {
				docIn.getDocumentElement().setAttribute(AcademyConstants.STS_ORDER_TYPE, strDocumentType);
			}
		} catch (Exception e) {
			log.error("Exception caught  :: " + e);
			e.printStackTrace();
		}
		return docIn;
	}
	
	/* Sample input to getReceiptList api
	 * <Receipt  ShipmentKey="202210180121035697947171"/>
    
    Sample output from getReceiptList api
    
    <ReceiptList TotalNumberOfRecords="1">
	    <Receipt ArrivalDateTime="2022-10-20T07:24:03-05:00" DocumentType="0006" DriverName=""
	        EnterpriseCode="Academy_Direct" NumOfCartons="0" NumOfPallets="0" OpenReceiptFlag="Y"
	        ReceiptDate="2022-10-20T07:24:03-05:00" ReceiptHeaderKey="202210200724035699898442"
	        ReceiptNo="100560197-1" ReceivingDock="" ReceivingNode="033" ShipmentKey="202210200721435699897256" 
	        Status="1300" TrailerLPNNo="" isHistory="N">
	        <ReceiptLines>
	            <ReceiptLine Quantity="1.00" ReceiptLineKey="202210200724035699898447"/>
	        </ReceiptLines>
	    </Receipt>
	</ReceiptList>    
    */
	private Document invokeGetReceiptList(YFSEnvironment env, String strShipmentKey) {	
		log.verbose("Begin - Inside AcademySTSAcquisition.invokeGetReceiptList() with ShipmentKey:: " + strShipmentKey);
	
		Document docGetReceiptListIn = null;
		Document docGetReceiptListOut = null;
		try {
			docGetReceiptListIn = XMLUtil.createDocument(AcademyConstants.ELE_RECEIPT);
			Element eleReceipt = docGetReceiptListIn.getDocumentElement();
			eleReceipt.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
			
			docGetReceiptListOut = AcademyUtil.invokeService(env,AcademyConstants.SERV_ACAD_GET_RECEIPT_LST_STS, docGetReceiptListIn);
		} catch(Exception e) {
			e.printStackTrace();
		}
		log.verbose("End - Inside AcademySTSAcquisition.invokeGetReceiptList()");
		log.verbose("invokeGetReceiptList Output:: " + XMLUtil.getXMLString(docGetReceiptListOut));
		return docGetReceiptListOut;
	}
	
	/*
	 * Sample input to startReceipt api:
	 * <Receipt DocumentType="0006" OpenReceiptFlag="" ReceivingNode="033">
    		<Shipment EnterpriseCode="Academy_Direct" ShipNode="001" ShipmentKey="202210190722385699072791"/>
		</Receipt>
		
		Sample output from startReceipt api:
		<Receipt ArrivalDateTime="2022-10-20T04:24:46-05:00" DriverName=""
		    NumOfCartons="0" NumOfPallets="0" OpenReceiptFlag="Y" ReceiptDate="2022-10-20T04:24:46-05:00"
		    ReceiptHeaderKey="202210200424465699783551" ReceiptNo="100559988-1" ReceivingDock="" ReceivingNode="033" 
		    ShipmentKey="202210190722385699072791" Status="1200" TrailerLPNNo="">
		    <Instructions/>
		</Receipt>
	 */

	private Document invokeStartReceipt(YFSEnvironment env, Document docIn) {
		log.verbose("Begin - Inside AcademySTSAcquisition.invokeStartReceipt()" + XMLUtil.getXMLString(docIn));		
		Document docstartReceiptIn = null;
		Document docstartReceiptOut = null;
		try {
			docstartReceiptIn = prepareStartReceiptInput(docIn);
			docstartReceiptOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_START_RCPT, docstartReceiptIn);
			Element eleReceiptOut = docstartReceiptOut.getDocumentElement();
			String strReceiptHeaderKey = eleReceiptOut.getAttribute(AcademyConstants.ATTR_RECEIPT_HDR_KEY);
			Element eleShipmentLst = docIn.getDocumentElement();
			Element eleShipment = XMLUtil.getFirstElementByName(eleShipmentLst,AcademyConstants.ELE_SHIPMENT);
			eleShipment.setAttribute(AcademyConstants.ATTR_RECEIPT_HDR_KEY,strReceiptHeaderKey);
		} catch(Exception e) {
			e.printStackTrace();
		}
		log.verbose("End - Inside AcademySTSAcquisition.invokeStartReceipt()");
		log.verbose("invokeStartReceipt Output:: " + XMLUtil.getXMLString(docstartReceiptOut));
		
		return docstartReceiptOut;
	}

	private Document prepareStartReceiptInput(Document docIn) {	
		log.verbose("Begin - Inside AcademySTSAcquisition.prepareStartReceiptInput()");
		log.verbose("prepareStartReceiptInput method Input" + XMLUtil.getXMLString(docIn));
	
		Document docOut=null;
		try {
			String strReceivingNode = XMLUtil.getAttributeFromXPath(docIn, AcademyConstants.STS_RECEIVING_NODE_XPATH);
			String strShipNode = XMLUtil.getAttributeFromXPath(docIn, AcademyConstants.STS_SHIP_NODE_XPATH);
			String strEnterpriseCode = XMLUtil.getAttributeFromXPath(docIn, AcademyConstants.STS_ENTERPRISE_KEY_XPATH);
			String strShipmentey = XMLUtil.getAttributeFromXPath(docIn, AcademyConstants.STS_SHIPMENT_KEY_XPATH);
			docOut=XMLUtil.createDocument(AcademyConstants.ELE_RECEIPT);
			Element eleReceipt = docOut.getDocumentElement();
			eleReceipt.setAttribute(AcademyConstants.STR_DOCUMENT_TYPE, AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE);
			eleReceipt.setAttribute(AcademyConstants.ATTR_RECV_NODE, strReceivingNode);
			eleReceipt.setAttribute(AcademyConstants.ATTR_OPEN_RECEIPT, AcademyConstants.STR_EMPTY_STRING);
			Element eleShipment = docOut.createElement(AcademyConstants.ELE_SHIPMENT);
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentey);
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strShipNode);
			eleShipment.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, strEnterpriseCode);
			eleReceipt.appendChild(eleShipment);

		} catch(Exception e) {
			e.printStackTrace();
		}
		log.verbose("End - Inside AcademySTSAcquisition.prepareStartReceiptInput()");
		log.verbose("prepareStartReceiptInput method Output:: " + XMLUtil.getXMLString(docOut));
		
		return docOut;
	}
	/*
	 receiveOrder input: 
	<Receipt DocumentType="0006" ReceiptHeaderKey="202210200424465699783551"
	    ReceivingNode="033" ShipmentKey="202210190722385699072791">
	    <ReceiptLines>
	        <ReceiptLine OrderLineKey="202210170850095697435341"
	            Quantity="1" ShipmentLineKey="202210190722385699072789">
	            <Extn ExtnShipmentContainerKey="202210190741535699091589"/>
	        </ReceiptLine>
	    </ReceiptLines>
	</Receipt>
	
	receiveOrder output:
	<Receipt ArrivalDateTime="2022-10-20T04:24:46-05:00" DriverName="" NumOfCartons="0" NumOfPallets="0" OpenReceiptFlag="Y"
	    ReceiptDate="2022-10-20T04:24:46-05:00" ReceiptHeaderKey="202210200424465699783551" ReceiptNo="100559988-1"
	    ReceivingDock="" ReceivingNode="033" ShipmentKey="202210190722385699072791" Status="1300" TrailerLPNNo="">
	    <Shipment BuyerOrganizationCode="Academy_Direct" CarrierAccountNo=""
	        CarrierServiceCode="STS" DocumentType="0006" EnterpriseCode="Academy_Direct" OrderNo="Y169336021"
	        ReleaseNo="0" SCAC="LOCAL" SellerOrganizationCode="Academy_Direct" ShipNode="001" ShipmentNo="100559988"/>
	    <ReceiptLines>
	        <ReceiptLine BatchNo=" " CaseId="" DispositionCode=" " EnterpriseCode="Academy_Direct" FifoNo="0" InspectedBy=" "
	            InspectionComments=" " InspectionDate="2022-10-20T04:24:47-05:00"
	            ItemID="019267202" LocationId=" " LotAttribute1=" " LotAttribute2=" " LotAttribute3=" " LotKeyReference=" "
	            LotNumber=" " NetWeight="0.00" NetWeightUom=" " OrderHeaderKey="202210170850095697435343"
	            OrderLineKey="202210170850095697435341" OrderNo="Y169336021" OrderReleaseKey="202210170854175697438569" PalletId=""
	            PrimeLineNo="3" ProductClass="GOOD" Quantity="1.00" ReceiptLineKey="202210200424465699783556" ReceiptLineNo="1"
	            ReleaseNo="2" RevisionNo=" " SerialNo=" " ShipmentLineKey="202210190722385699072789"
	            ShipmentLineNo="2" ShipmentSubLineNo="0" SubLineNo="1" UnitOfMeasure="EACH"/>
	    </ReceiptLines>
	</Receipt>

	 */

	private Document invokeReceiveOrder(YFSEnvironment env, Document docgetShipmentListForOrderOut) {
		log.verbose("Begin - Inside AcademySTSAcquisition.invokeReceiveOrder()");
		log.verbose("ShipmentList Input document:: "+ XMLUtil.getXMLString(docgetShipmentListForOrderOut));
		
		Document docinvokeReceiptIn = null;
		Document docinvokeReceiptOut = null;
		try {
			docinvokeReceiptIn = prepareReceiveOrderInput(docgetShipmentListForOrderOut);
			
			//Code Changes for OMNI-102315-- Start 
				// code changes for OMNI-96161
				//postSupplyAndDemandUpdateToYantriks(env,docgetShipmentListForOrderOut);
			//Code Changes for OMNI-102315 --End 
			
			docinvokeReceiptOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_RECEIVE_ORDER, docinvokeReceiptIn);
			
		} catch(Exception r) {
			r.printStackTrace();
			log.verbose("Exception in - AcademySTSAcquisition.invokeReceiveOrder()" + r);
		}
		log.verbose("End - Inside AcademySTSAcquisition.invokeReceiveOrder()");
		log.verbose("invokeReceiveOrder method Output::"+ XMLUtil.getXMLString(docinvokeReceiptOut));
		
		return docinvokeReceiptOut;
	}

	private Document prepareReceiveOrderInput(Document docgetShipmentListForOrderOut) {
		log.verbose("Begin - Inside AcademySTSAcquisition.prepareReceiveOrderInput()");
		log.verbose("ReceiptHeaderKey:: " + strReceiptHdrKey);
		log.verbose("ShipmentList Input document:: \n" + XMLUtil.getXMLString(docgetShipmentListForOrderOut));
		Document docOut=null;
		try {
			Element eleShipmentLst = docgetShipmentListForOrderOut.getDocumentElement();
			Element eleShipment = XMLUtil.getFirstElementByName(eleShipmentLst, AcademyConstants.ELE_SHIPMENT);
			strTOOrderNo = eleShipment.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			String strOLKey = XMLUtil.getAttributeFromXPath(docgetShipmentListForOrderOut,
					"ShipmentList/Shipment/ShipmentLines/ShipmentLine[@ItemID="+strAcqItemID+"]/OrderLine/@OrderLineKey");
			String strSLKey = XMLUtil.getAttributeFromXPath(docgetShipmentListForOrderOut,
					"ShipmentList/Shipment/ShipmentLines/ShipmentLine[@ItemID="+strAcqItemID+"]/@ShipmentLineKey");
			String strContainerkey = XMLUtil.getAttributeFromXPath(docgetShipmentListForOrderOut,
					"ShipmentList/Shipment/Containers/Container[ContainerDetails/ContainerDetail/@ItemID="+strAcqItemID+"]/@ShipmentContainerKey");
			
			log.verbose("OLK:: " + strOLKey + "\n SLK:: " + strSLKey + "\n Containerkey to be received:: " + strContainerkey);
			if (YFCObject.isVoid(strReceiptHdrKey)) {
				strReceiptHdrKey = eleShipment.getAttribute(AcademyConstants.ATTR_RECEIPT_HDR_KEY);
				log.verbose("ReceiptHdrKey From Shipment :: \n" + strReceiptHdrKey);
			}			
			
			docOut = XMLUtil.createDocument(AcademyConstants.ELE_RECPT);			
			Element eleReceipt = docOut.getDocumentElement();
			eleReceipt.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
					AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE);
			eleReceipt.setAttribute(AcademyConstants.ATTR_PALLET_ID, strTOOrderNo);
			eleReceipt.setAttribute(AcademyConstants.ATTR_RECEIPT_HDR_KEY,
					strReceiptHdrKey);
			eleReceipt.setAttribute(AcademyConstants.ATTR_RECV_NODE, 
					eleShipment.getAttribute(AcademyConstants.ATTR_RECV_NODE));
			eleReceipt.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, 
					eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
			Element eleReceiptLines = docOut.createElement(AcademyConstants.RECEIPT_LINES);
			Element eleReceiptLine = docOut.createElement(AcademyConstants.RECEIPT_LINE);
			eleReceiptLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, strOLKey);
			eleReceiptLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, strSLKey);
			log.verbose("Quantity to be received:::: " + strAcqQty);
			eleReceiptLine.setAttribute(AcademyConstants.ATTR_QUANTITY, strAcqQty);
			Element eleExtn = docOut.createElement(AcademyConstants.ELE_EXTN);
			eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_SHP_CONT_KEY, strContainerkey);
			eleReceiptLine.appendChild(eleExtn);
			eleReceiptLines.appendChild(eleReceiptLine);
			eleReceipt.appendChild(eleReceiptLines);
			
		} catch(Exception e) {
			e.printStackTrace();
			log.verbose("Exception :: " + e);
		}
		log.verbose("Input document to ReceiveOrder::\n" + XMLUtil.getXMLString(docOut));
		return docOut;
	}
	
	/**
	 * 
	 * This Method Created to fix OMNI-96161, to set the environment object so that demand and supply details can be published to 
	 * yantriks
	 * @param env
	 * @param docIn
	 * @return
	 * @throws XPathExpressionException
	 */
	private void postSupplyAndDemandUpdateToYantriks(YFSEnvironment env,Document docgetShipmentListForOrderOut) throws XPathExpressionException {
		log.verbose("Begin - Inside AcademySTSAcquisition.postSupplyAndDemandUpdateToYantriks()");
		log.verbose("Setting Transaction objects if status is Shipped To Store");
		String shipmentKey = XMLUtil.getAttributeFromXPath(docgetShipmentListForOrderOut,
				"ShipmentList/Shipment[Containers/Container[ContainerDetails/ContainerDetail/@ItemID="+strAcqItemID+"]]/@ShipmentKey");
		env.setTxnObject(AcademyConstants.ATTR_STATUS, AcademyConstants.STR_RECEIVED);
		env.setTxnObject(AcademyConstants.IS_STS_SHIPMENT, AcademyConstants.STR_YES);
		env.setTxnObject(AcademyConstants.ATTR_SHIPMENT_KEY,shipmentKey);
		//env.setTxnObject(AcademyConstants.ATTR_CONTAINER_NO,containerNo);
		//code changes for OMNI-96161
		env.setTxnObject(AcademyConstants.ITEM_ID,strAcqItemID);
		env.setTxnObject(AcademyConstants.ATTR_QUANTITY,strAcqQty);
		
		log.debug("Setting transaction Object for Demand Update in AcademySetTxnObjectForDemandUpdate ");
		//env.setTxnObject(AcademyConstants.DEMAND_UPDATE_NEEDED, AcademyConstants.STR_YES);
		log.debug("Transaction Object set successfully");
		log.verbose("Status::" + AcademyConstants.STR_RECEIVED);
		log.verbose("IsSTS::" + AcademyConstants.STR_YES);
		log.verbose("shipmentKey::" + shipmentKey);
		log.verbose("strAcqQty::" + strAcqItemID);
		log.verbose("strAcqQty::" + strAcqQty);
		
		
	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		log.error("setProperties:: ");
	}


}
