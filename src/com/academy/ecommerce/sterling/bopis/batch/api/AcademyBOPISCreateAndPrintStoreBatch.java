package com.academy.ecommerce.sterling.bopis.batch.api;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.academy.util.xml.XPathUtil;
import com.academy.ecommerce.sterling.los.XMLUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.ibm.icu.util.Calendar;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class serves two purpose: 1: create a new Store Batch for a Potential
 * and print Shipment/item pick ticket 2. Print shipment/item pick ticket for
 * the existing store batch.
 * 
 * @author johkhakh2
 *
 */
public class AcademyBOPISCreateAndPrintStoreBatch implements YIFCustomApi {

	/**
	 * Used to store the service value arguments
	 */
	private Properties props;
	
	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyBOPISCreateAndPrintStoreBatch.class);

	@Override
	public void setProperties(Properties arg0) throws Exception {
		this.props = arg0;
	}	

	/**
	 * This method create a new store batch against the potential batch and prints the pick tickets
	 * This method is also used to print pick tickets for the existing batch.
	 * @param env - YFSEnvironment variable used to call the service in the same transaction boundary.
	 * @param inDoc - input to the service 
					  <StoreBatch AssignedToUserId="str033" BatchType="SORT_WHILE_PICK"
				    		DisplayLocalizedFieldInLocale="en_US_EST" IgnoreInvalidLines="N"
				    		IgnoreOrdering="Y" OrganizationCode="033" SkipValidations="Y">
						    <ShipmentLines>
						        <ShipmentLine ShipmentLineKey="20180629183427213219"/>
						    </ShipmentLines>
						    <StoreBatchConfigList>
						        <StoreBatchConfig Entity="Shipment" Name="LevelOfService"/>
						        <StoreBatchConfig Entity="Shipment" Name="EnterpriseCode" Value="Academy_Direct"/>
						    </StoreBatchConfigList>
					  </StoreBatch>
	 * @return updated input doc
	 * @throws Exception - Exception details.
	 */
	public Document createAndPrintStoreBatch(YFSEnvironment env, Document inDoc) throws Exception {

		YFCDocument yfsInDoc = YFCDocument.getDocumentFor(inDoc);
		log.debug("AcademyBOPISCreateAndPrintStoreBatch.java : createAndPrintStoreBatch() :inDoc"+yfsInDoc.toString());
		
		YFCElement yfsInDocEle = yfsInDoc.getDocumentElement();
		String rootEleName = yfsInDocEle.getNodeName();
	

		// RootElement="StoreBatch" means print request is for a potential batch.
		// Else print request is for the existing batch.

		if (AcademyConstants.ELE_STORE_BATCH.equals(rootEleName)) {

			
			YFCDocument yfsCreateStoreBatchOutDoc = YFCDocument.getDocumentFor(createStoreBatchWithBatchNo(env, inDoc));
			
			// Prepare the input and call the print tickets service.
			YFCDocument yfsShpmtPickTicketPrintInDoc = getInputForPrintTickets(yfsInDocEle, yfsCreateStoreBatchOutDoc);
			
			log.debug("AcademyBOPISCreateAndPrintStoreBatch.java : AcademySFSPrintShipmentPickTickets :inDoc" + yfsShpmtPickTicketPrintInDoc.toString());
			
			AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_SFS_PRINT_SHIPMENT_PICK_TICKETS_SERVICE,
					yfsShpmtPickTicketPrintInDoc.getDocument());
			// ************************************************************************

		} else {
			// Prepare the input and call print tickets services
			
			Boolean isExistingBatchPrinted = processUnPrintedExistingBatch(env, inDoc);
			
			if(!isExistingBatchPrinted){
			yfsInDocEle.setAttribute(AcademyConstants.IS_STORE_BATCH_FLAG, AcademyConstants.STR_YES);
			//BOPIS-1572::Begin- As per this story, Reversing the sequence of execution for below services so that item pick ticket is printed first
				//and then shipment pick tickets. Since this is for an existing batch. Just changing the Sequence will work. 
			log.debug("AcademyBOPISCreateAndPrintStoreBatch.java : AcademySFSPrintItemPickTickets :inDoc" + yfsInDoc.toString());
			AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_SFS_PRINT_ITEM_PICK_TICKETS_SERVICE, yfsInDoc.getDocument());
			log.debug("AcademyBOPISCreateAndPrintStoreBatch.java : AcademySFSPrintShipmentPickTickets :inDoc" + yfsInDoc.toString());
			AcademyUtil.invokeService(env, "AcademySFSPrintShipmentPickTickets", yfsInDoc.getDocument());
				//BOPIS-1572::End
			// ************************************************************************
			}
		}

		return inDoc;
	}

	public Document createStoreBatchWithBatchNo(YFSEnvironment env, Document inDoc) throws Exception {
		YFCDocument yfsInDoc = YFCDocument.getDocumentFor(inDoc);
		log.debug("AcademyBOPISCreateAndPrintStoreBatch.java : createAndPrintStoreBatch() :inDoc"+yfsInDoc.toString());
		Element inputEle=inDoc.getDocumentElement();
		Node inputNode=(Node)inputEle;
		NodeList nm = XPathUtil.getNodeList(inputNode,AcademyConstants.XPATH_STS2_DOC_TYPE);
		log.debug("node List length is :" + nm.getLength());
		YFCElement yfsInDocEle = yfsInDoc.getDocumentElement();
		Boolean pickTicketprintedOnce=true;
		
		// Stamp BatchNo in the input and call service to create the store batch.
		String strShipNode = yfsInDocEle.getAttribute(AcademyConstants.ORGANIZATION_CODE);
		String timeStamp = new SimpleDateFormat(AcademyConstants.STR_BATCH_DATE_TIME_PATTERN_NEW).format(Calendar.getInstance().getTime());
		String strBatchNo=null;
		if(nm.getLength()==1) {
		 strBatchNo = AcademyConstants.STR_STS_PREFIX+strShipNode + AcademyConstants.STR_UNDERSCORE + timeStamp;
		}
		else {
			 strBatchNo = strShipNode + AcademyConstants.STR_UNDERSCORE + timeStamp;
		}
		yfsInDocEle.setAttribute(AcademyConstants.BATCH_NO, strBatchNo);
		
		log.debug("AcademyBOPISCreateAndPrintStoreBatch.java : AcademyCreateStoreBatchService :inDoc" + yfsInDoc.toString());
		Document yfsCreateStoreBatchOutDoc = AcademyUtil.invokeService(env,
				AcademyConstants.ACADEMY_BOPIS_CREATE_STORE_BATCH_SERVICE, yfsInDoc.getDocument());
		log.debug("AcademyBOPISCreateAndPrintStoreBatch.java : AcademyCreateStoreBatchService :outDoc" + yfsCreateStoreBatchOutDoc.toString());
		// ************************************************************************
		return yfsCreateStoreBatchOutDoc;
	}


	private Boolean processUnPrintedExistingBatch(YFSEnvironment env, Document inDoc) throws Exception {
		
		YFCDocument yfcinDoc = YFCDocument.getDocumentFor(inDoc);
		YFCElement elePrint=yfcinDoc.getDocumentElement();
		YFCElement eleLogin = elePrint.getChildElement(AcademyConstants.ELE_LOGIN);
		String strPrintTicketNo=elePrint.getAttribute(AcademyConstants.ATTR_PICK_TICKET_NO);
		
		YFCDocument yfcgetStoreBatchListIP = YFCDocument.createDocument(AcademyConstants.ELE_STORE_BATCH);
		YFCElement elegetStoreBatchListIP = yfcgetStoreBatchListIP.getDocumentElement();
		elegetStoreBatchListIP.setAttribute(AcademyConstants.BATCH_NO, strPrintTicketNo);
		Document getStoreBatchListOP = AcademyUtil.invokeService(env, AcademyConstants.SER_GET_BATCH_STORE_LIST , yfcgetStoreBatchListIP.getDocument());
		YFCDocument yfcgetStoreBatchListOP=YFCDocument.getDocumentFor(getStoreBatchListOP);
		YFCElement elegetStoreBatchListOP=yfcgetStoreBatchListOP.getDocumentElement();
		YFCElement eleStoreBatch = elegetStoreBatchListOP.getChildElement(AcademyConstants.ELE_STORE_BATCH);
		String strShipNode=eleStoreBatch.getAttribute(AcademyConstants.ORGANIZATION_CODE);
		String strBatchNo=eleStoreBatch.getAttribute(AcademyConstants.BATCH_NO);
		
		YFCNodeList<YFCElement> elegetStoreBatchListShipmentLinenl= yfcgetStoreBatchListOP.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		
		HashSet<String> shipmentKeyListForUnPrintedBatch = new HashSet<String>();
		boolean pickTicketprintedOnce=true;
		boolean printResult=false;
		for(YFCElement elegetStoreBatchListShipmentLine : elegetStoreBatchListShipmentLinenl)
		{
			shipmentKeyListForUnPrintedBatch.add(elegetStoreBatchListShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
			YFCElement eleShipment = elegetStoreBatchListShipmentLine.getChildElement(AcademyConstants.ELE_SHIPMENT);
			String strPickticketNo=eleShipment.getAttribute(AcademyConstants.ATTR_PICK_TICKET_NO);
			
			if(YFCCommon.isVoid(strPickticketNo))
			{
				pickTicketprintedOnce=false;
			}
		}
		
		
		if(!pickTicketprintedOnce)
		{
		YFCDocument yfsShpmtPickTicketPrintForUnprintedBatch = YFCDocument.getDocumentFor(XMLUtil.getDocument(AcademyConstants.INPUT_TO_PRINT_SERVICE_TEMPLATE));
		YFCElement yfcPrintInDocEle = yfsShpmtPickTicketPrintForUnprintedBatch.getElementsByTagName(AcademyConstants.ELE_PRINT).item(0);
		yfcPrintInDocEle.setAttribute(AcademyConstants.DISPLAY_LOCALIZED_FIELD_IN_LOCALE,
				elePrint.getAttribute(AcademyConstants.DISPLAY_LOCALIZED_FIELD_IN_LOCALE));
		yfcPrintInDocEle.setAttribute(AcademyConstants.NEW_PRINTER_ID, props.getProperty(AcademyConstants.NEW_PRINTER_ID));
		yfcPrintInDocEle.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strShipNode);
		yfcPrintInDocEle.setAttribute(AcademyConstants.ATTR_MAX_RECORD, shipmentKeyListForUnPrintedBatch.size());
		yfcPrintInDocEle.setAttribute(AcademyConstants.BATCH_NO, strBatchNo);

		YFCElement yfcLoginEle = yfsShpmtPickTicketPrintForUnprintedBatch.getElementsByTagName(AcademyConstants.ELE_LOGIN).item(0);
		yfcLoginEle.setAttribute(AcademyConstants.ELE_LOGIN, eleLogin.getAttribute(AcademyConstants.ATTR_LOGIN_ID));
		yfcPrintInDocEle.appendChild(yfcLoginEle);

		YFCElement yfcShipmentsEle = yfsShpmtPickTicketPrintForUnprintedBatch.getElementsByTagName(AcademyConstants.ELE_SHIPMENTS).item(0);

		for (String shipmentKey : shipmentKeyListForUnPrintedBatch) {
			YFCElement yfcShipmentEle = yfsShpmtPickTicketPrintForUnprintedBatch.createElement(AcademyConstants.ELE_SHIPMENT);
			yfcShipmentEle.setAttribute(AcademyConstants.ATTR_IS_PICKTICKET_PRINTED, AcademyConstants.STR_NO);
			yfcShipmentEle.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, shipmentKey);
			yfcShipmentsEle.appendChild(yfcShipmentEle);
		}
		AcademyUtil.invokeService(env, "AcademySFSPrintShipmentPickTickets", yfsShpmtPickTicketPrintForUnprintedBatch.getDocument());
	     printResult=true;
		}
		return printResult;
	}

	/**
	 * This method will be used to prepare the input for the for print ticket service
	 * @param yfsInDocEle - Service input doc element
	 * @param yfsCreateStoreBatchOutDoc - Output of createStoreBatch API
	 * @return - input to print services
	 * @throws Exception - Exception Details
	 */
	private YFCDocument getInputForPrintTickets(YFCElement yfsInDocEle, YFCDocument yfsCreateStoreBatchOutDoc) throws Exception {

		// ****************************************************************************
		// Get the list of all the Shipment Key include in the batch, which will be used
		// to prepare the input to the print ticket service.
		// ****************************************************************************
		YFCNodeList<YFCElement> yfcShipmentLineNodeList = yfsCreateStoreBatchOutDoc
				.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		YFCElement yfcCreateStoreBatchOutDoc=yfsCreateStoreBatchOutDoc.getDocumentElement();
		String strShipNode=yfcCreateStoreBatchOutDoc.getAttribute(AcademyConstants.ORGANIZATION_CODE);
		String strBatchNo=yfcCreateStoreBatchOutDoc.getAttribute(AcademyConstants.BATCH_NO);
		

		HashSet<String> shipmentKeyList = new HashSet<String>();

		for (YFCElement yfcShipmentLine : yfcShipmentLineNodeList) {
			shipmentKeyList.add(yfcShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
		}
		// ******************************Ends******************************************

		// ****************************************************************************
		// Preparing input for the print tickets service
		// ****************************************************************************
		YFCDocument yfsShpmtPickTicketPrintInDoc = YFCDocument.getDocumentFor(XMLUtil.getDocument(AcademyConstants.INPUT_TO_PRINT_SERVICE_TEMPLATE));
		YFCElement yfcPrintInDocEle = yfsShpmtPickTicketPrintInDoc.getElementsByTagName(AcademyConstants.ELE_PRINT).item(0);
		yfcPrintInDocEle.setAttribute(AcademyConstants.DISPLAY_LOCALIZED_FIELD_IN_LOCALE,
				yfsInDocEle.getAttribute(AcademyConstants.DISPLAY_LOCALIZED_FIELD_IN_LOCALE));
		yfcPrintInDocEle.setAttribute(AcademyConstants.NEW_PRINTER_ID, props.getProperty(AcademyConstants.NEW_PRINTER_ID));
		yfcPrintInDocEle.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strShipNode);
		yfcPrintInDocEle.setAttribute(AcademyConstants.ATTR_MAX_RECORD, shipmentKeyList.size());
		yfcPrintInDocEle.setAttribute(AcademyConstants.BATCH_NO, strBatchNo);

		YFCElement yfcLoginEle = yfsShpmtPickTicketPrintInDoc.getElementsByTagName(AcademyConstants.ELE_LOGIN).item(0);
		yfcLoginEle.setAttribute(AcademyConstants.ELE_LOGIN, yfsInDocEle.getAttribute(AcademyConstants.ATTR_ASSIGNED_TO_USER_ID));
		yfcPrintInDocEle.appendChild(yfcLoginEle);

		YFCElement yfcShipmentsEle = yfsShpmtPickTicketPrintInDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENTS).item(0);

		for (String shipmentKey : shipmentKeyList) {
			YFCElement yfcShipmentEle = yfsShpmtPickTicketPrintInDoc.createElement(AcademyConstants.ELE_SHIPMENT);
			yfcShipmentEle.setAttribute(AcademyConstants.ATTR_IS_PICKTICKET_PRINTED, AcademyConstants.STR_NO);
			yfcShipmentEle.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, shipmentKey);
			yfcShipmentsEle.appendChild(yfcShipmentEle);
		}
		// *******************************Ends*********************************************

		return yfsShpmtPickTicketPrintInDoc;
	}

}
