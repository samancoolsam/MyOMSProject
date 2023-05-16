package com.academy.ecommerce.sterling.sts;

import java.io.IOException;
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
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;


/**************************************************************************
 * File Name		: AcademySTSProcessTOSIMReceipt
 *
 * Description	    : OMNI-8004. The class consumes STS SIM Receipts and make
 * the required changes on the STS transfer order
 * 
 * Input xml to the class
 * <Container ContainerNo="">
 * <Shipment ShipNode="" ReceivingNode="">
 *  <ContainerDetails>
 *  <ContainerDetail ItemID="" Quantity="" />
 * 	</ContainerDetails>
 * </Shipment>
 *</Container>
 * --------------------------------
 * 	Date             Author               
 * --------------------------------
 *  7-Jul-2020      Cognizant			 	 
 * 
 * -------------------------------------------------------------------------
 **************************************************************************/
public class AcademySTSProcessTOSIMReceipt implements YIFCustomApi {

	/**
	 * log variable.
	 */
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSProcessTOSIMReceipt.class);

	public Document processTOSIMReceipt(YFSEnvironment env, Document inXML) throws Exception{
		log.beginTimer("====AcademySTSProcessTOSIMReceipt====");
		log.verbose("Start Inside AcademySTSProcessTOSIMReceipt.processTOSIMReceipt()");

		//Declarations						
		Element eleContainer, eleShipment = null;
		String sShpStatus, sShipmentKey,  sShipmentContKey = "";
		String sArrivedAtStore, sIsReceived = "";	
		Document docGetShipmentContListOut, outChangeOrdStatus = null;

		if (!YFCObject.isVoid(inXML.getDocumentElement())) 
		{
			log.verbose("Input xml:: " + XMLUtil.getXMLString(inXML));

			AcademySTSReceiveTOContainer confirmShipmentForSIMReceipt = new AcademySTSReceiveTOContainer(); 

			String inContainerNo = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_CONTAINER_NO);
			//handling duplicate containers
			// OMNI-10249  STS - Container ID Belongs To a Different Store - START 
			Element eleContainerShipment = (Element) inXML.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
			String sReceivingNode = eleContainerShipment.getAttribute(AcademyConstants.ATTR_RECV_NODE);
			// 	 OMNI-10249  STS - Container ID Belongs To a Different Store - END

			try {
			//handling duplicate containers
			// OMNI-10249  STS - Container ID Belongs To a Different Store - START
				docGetShipmentContListOut = getShipmentContainerList(env, inContainerNo, sReceivingNode);
			// OMNI-10249  STS - Container ID Belongs To a Different Store - END
				if(!YFCObject.isVoid(docGetShipmentContListOut))
				{
				Element eleShipContListOut = docGetShipmentContListOut.getDocumentElement();
				eleContainer = (Element) XPathUtil.getNode(eleShipContListOut, "Container[@ContainerNo='"+inContainerNo+"']");
				if(!YFCObject.isVoid(eleContainer))
				{
				sArrivedAtStore = XPathUtil.getString(eleContainer, AcademyConstants.XPATH_TO_ARRIVED_ATSTORE);
				sIsReceived = eleContainer.getAttribute(AcademyConstants.ATTR_IS_RECEIVED);
				sShipmentContKey = eleContainer.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY);				
											
				eleShipment = XmlUtils.getChildElement(eleContainer, AcademyConstants.ELE_SHIPMENT);
				sShpStatus = eleShipment.getAttribute(AcademyConstants.ATTR_STATUS);
				sShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
				/**
				 * OMNI30547, OMNI30546
				 * Setting the transaction objects to identify the status change in sales order and control 
				 * the yantriks updates based on these objects
				 */
				log.verbose("Setting transaction objects as Received and "+sShipmentKey);
				env.setTxnObject(AcademyConstants.ATTR_STATUS, AcademyConstants.STR_RECEIVED);
				env.setTxnObject(AcademyConstants.ATTR_SHIPMENT_KEY,sShipmentKey);
				env.setTxnObject(AcademyConstants.ATTR_CONTAINER_NO,inContainerNo);
				log.verbose("Txn Objects set successfully");
				//OMNI30547, OMNI30546 END

				//if arrivedAtStore is not present or not Y
				if((YFSObject.isVoid(sArrivedAtStore)) || (!sArrivedAtStore.equals(AcademyConstants.ATTR_Y))) {

					String sShipNodeType=XPathUtil.getString(eleShipment, "ShipNode/@NodeType");
					String sProcureFromNode = XPathUtil.getString(eleShipment, "ShipmentLines/ShipmentLine/OrderLine/ChainedFromOrderLine/@ProcureFromNode");

					// method invoked to update ExtnArrivedAtStore flag as Y and ExtnTrackingStatus as Delivered in shipment container table.
					changeShipmentForSIMReceipt(env, inContainerNo, sShipmentContKey, sShipmentKey, sIsReceived, sShipNodeType, sProcureFromNode);
				}

					//confirm TO Shipment - only if shipment is not confirmed by exeter, will not get 
					//invoked if shipment is already confirmed via exeter or SOM UI
					if((!YFSObject.isVoid(sShpStatus)) && sShpStatus.equals("1100.70.06.30")) {

						confirmShipmentForSIMReceipt.confirmTOShipment(env, sShipmentKey);
					}

					//Only if SOM UI receiving did not happen, changeOrderStatus invoked
					if(sIsReceived.equals(AcademyConstants.STR_NO)) {
						//method to invoke changeOrderStatus - to move intransit inventory to onhand of store
						outChangeOrdStatus = stsChangeOrderStatus(env, docGetShipmentContListOut);

					}				
				 }
				}			  
			} catch (Exception e) {

				if(e instanceof YFSException) {
					throw e;
				}

				log.error("Exception Occured in fetching the SIM receipt"
						+ e.getMessage());
				throw new YFSException(new YFCException(e).getXMLErrorBuf());
			}

		}
		log.verbose("End of AcademySTSProcessTOSIMReceipt.processTOSIMReceipt()");
		log.endTimer("====AcademySTSProcessTOSIMReceipt====");
		return outChangeOrdStatus;

	}


	/**
	 * @param env
	 * @param sContainerQty
	 * @param sReleaseKey
	 * @param sOrdLineKey
	 * @throws ParserConfigurationException
	 * @throws Exception
	 * Method invoked to move the TO order status from Shipped to Store to Arrived At Store
	 * This changeOrderStatus invokation makes sure the Intransit inventory is moved 
	 * to ONHAND of the Store
	 */
	private Document stsChangeOrderStatus(YFSEnvironment env, Document docGetShipmentContListOut) 
			throws ParserConfigurationException
	{
		log.verbose("Start of AcademySTSProcessTOSIMReceipt.stsChangeOrderStatus()");
		Document inChangeOrdStatus, outChangeOrdStatus = null;
		Element eleContDtl = null;	
		String sOrdHdrKey = "";
		
		Element eleOrdLine = null;
		try {
			Element eleShipContListOut = docGetShipmentContListOut.getDocumentElement();			
			sOrdHdrKey = XPathUtil.getString(eleShipContListOut, "Container/ContainerDetails/ContainerDetail/@OrderHeaderKey");
			
			inChangeOrdStatus = XMLUtil.createDocument(AcademyConstants.ELE_ORD_STATUS_CHG);

			Element eOrder = inChangeOrdStatus.getDocumentElement();
			eOrder.setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS, "3700.100");
			eOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, sOrdHdrKey);
			eOrder.setAttribute(AcademyConstants.ATTR_TRANS_ID, AcademyConstants.TRXN_ARRIVED_ATSTORE);
			Element eOrdLines = XmlUtils.createChild(eOrder, AcademyConstants.ELE_ORDER_LINES);
			//for each orderline of the container
			Element eleFirstContainer = (Element) docGetShipmentContListOut.getElementsByTagName(AcademyConstants.ELE_CONTAINER).item(0);
			NodeList nlContainer = eleFirstContainer.getElementsByTagName(AcademyConstants.CONTAINER_DETL_ELEMENT);
			for(int i=0; i<nlContainer.getLength(); i++){
				eleContDtl = (Element) nlContainer.item(i);
				eleOrdLine = XmlUtils.getChildElement(eleContDtl, AcademyConstants.ELE_ORDER_LINE);
								
				Element eOrdLine = XmlUtils.createChild(eOrdLines, AcademyConstants.ELE_ORDER_LINE);
				eOrdLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, eleOrdLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
				eOrdLine.setAttribute(AcademyConstants.ATTR_RELEASE_KEY, eleContDtl.getAttribute(AcademyConstants.ORDER_RELEASE_KEY));
				eOrdLine.setAttribute(AcademyConstants.ATTR_QUANTITY, eleContDtl.getAttribute(AcademyConstants.ATTR_QUANTITY));
			}
			
			log.verbose("Input to ChangeOrderStatus::" + XMLUtil.getXMLString(inChangeOrdStatus));
			
			outChangeOrdStatus = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHG_ORD_STATUS, inChangeOrdStatus);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.verbose("End of AcademySTSProcessTOSIMReceipt.stsChangeOrderStatus()");
		return outChangeOrdStatus;
	}


	/**
	 * @param env
	 * @param inContainerNo
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws Exception
	 */
	public Document getShipmentContainerList(YFSEnvironment env, String inContainerNo, String sReceivingNode)
			throws Exception {
		log.verbose("Start of AcademySTSProcessTOSIMReceipt.getShipmentContainerList()");
		Document inDocGetShipContainerList;
		Document docGetShipmentContListOut = null;
		Element eContainer, eleOrderBy, eleAttribute, eleShipment;

		inDocGetShipContainerList = XMLUtil.createDocument(AcademyConstants.ELE_CONTAINER);

		if(!YFSObject.isVoid(inContainerNo)){

			eContainer = inDocGetShipContainerList.getDocumentElement();				
			eContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_NO, inContainerNo);
			
			//handle duplicate containerno
			eleOrderBy = XmlUtils.createChild(eContainer, AcademyConstants.ELE_ORDERBY);
			eleAttribute = XmlUtils.createChild(eleOrderBy, AcademyConstants.ELE_ATTRIBUTE);
			eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_SHIP_CONT_KEY);
			eleAttribute.setAttribute(AcademyConstants.ATTR_DESC_SHORT, AcademyConstants.ATTR_Y);
			//handling duplicate containers
			// OMNI-10249  STS - Container ID Belongs To a Different Store - START
			eleShipment = XmlUtils.createChild(eContainer, AcademyConstants.ELE_SHIPMENT);
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.STR_SHIP_TO_STORE);
            eleShipment.setAttribute(AcademyConstants.ATTR_RECV_NODE, sReceivingNode);
			// OMNI-10249  STS - Container ID Belongs To a Different Store - END

			log.verbose("***getShipmentContainerList input***" +XMLUtil.getXMLString(inDocGetShipContainerList));

			docGetShipmentContListOut = AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACAD_GETSHIPMENT_CONT_LIST, inDocGetShipContainerList);
			log.verbose("***getShipmentContainerList output***" + XMLUtil.getXMLString(docGetShipmentContListOut));

		}else {

			YFSException exception = new YFSException("Mandatory Parameters Missing");
			exception.setErrorCode("Container ID is null.");
			throw exception;
		}
		log.verbose("End of AcademySTSProcessTOSIMReceipt.getShipmentContainerList()");
		return docGetShipmentContListOut;
	}


	/**
	 * @param env
	 * @param sContainerNo
	 * @param sContainerKey
	 * @param sShipmentKey
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError
	 * @throws Exception
	 * method invoked to update ExtnHasArrivedAtStore flag in shipment container table.
	 * Update IsReceived flag as "Y" if it is Y already as part of SOM UI	
	 * OMNI-105929 - Update  ExtnTrackingStatus="Delivered" 
	 */
	private void changeShipmentForSIMReceipt(YFSEnvironment env, String sContainerNo,
			String sContainerKey, String sShipmentKey, String sIsReceived, String sShipNodeType, String sProcureFromNode)
					throws ParserConfigurationException, FactoryConfigurationError {
		Document docChangeShipToSIMReceipt=null;
		docChangeShipToSIMReceipt = XmlUtils.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eShipment = docChangeShipToSIMReceipt.getDocumentElement();
		eShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, sShipmentKey);
		Element eContainers = XmlUtils.createChild(eShipment, AcademyConstants.ELE_CONTAINERS);
		Element eContainer = XmlUtils.createChild(eContainers, AcademyConstants.ELE_CONTAINER);
		eContainer.setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY, sContainerKey);
		eContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_NO, sContainerNo);

		if(sIsReceived.equals(AcademyConstants.ATTR_Y)) {
			eContainer.setAttribute(AcademyConstants.ATTR_IS_RECEIVED, sIsReceived);	
		}

		Element eContainerExtn = XmlUtils.createChild(eContainer, AcademyConstants.ELE_EXTN);
		eContainerExtn.setAttribute(AcademyConstants.ATTR_EXTN_ARRIVED_ATSTORE, AcademyConstants.ATTR_Y);
		
		//OMNI-105929 Start
		if (!YFSObject.isVoid(sShipNodeType) && AcademyConstants.ATTR_VAL_SHAREDINV_DC.equalsIgnoreCase(sShipNodeType) 
				&& (!YFCCommon.isVoid(sProcureFromNode)) )  {
		log.verbose("Updating EXTN_TRACKING_STATUS as Delivered since the ShipNodeType is DC");
		eContainerExtn.setAttribute(AcademyConstants.ATTR_EXTN_TRACKING_STATUS, AcademyConstants.STR_DELIVERED);
		}
		//OMNI-105929 End
		
		try {
			log.verbose("****changeShipment input to update ExtnArrivedAtStore & ExtnTrackingStatus ****" + XmlUtils.getString(docChangeShipToSIMReceipt));
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipToSIMReceipt);

		} catch (Exception e) {
			// TODO Auto-generated catch block		
			YFSException exception = new YFSException("SIM Receipt update failed.");
			exception.setErrorCode("SIM Receipt update failed.");
			throw exception;
		}
	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

}