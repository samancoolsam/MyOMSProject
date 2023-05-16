package com.academy.ecommerce.sterling.shipment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * This class prints the Acknowledgement slip for BOPIS orders through loftware(Silent Printing).
 * @author Abhishek Aggarwal
 *
 */
public class AcademyBOPISPrintAckSlip implements YIFCustomApi
{
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademyBOPISPrintAckSlip.class);
	Properties props;
	boolean isPersonInfoMarkForReq = false;
	/**
	 * This method gets the shipment information and prepares the input for loftware print service.
	 * @param env
	 * @param inDoc
	 * @return
	 */
	public Document printAckSlip(YFSEnvironment env,Document inDoc)
	{
		log.beginTimer("AcademyBOPISPrintAckSlip.class:printAckSlip() : START");
		log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip() : inDoc"+inDoc);
		try
		{
			YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
			YFCElement eleInputShpmt = yfcInDoc.getDocumentElement();
			String strShipmentKey = eleInputShpmt.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip(): GetShipmentList Api call: ShipmentKey "+strShipmentKey);
			log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip(): GetShipmentList Api call: inDoc"+inDoc);
			log.beginTimer("AcademyBOPISPrintAckSlip.class:printAckSlip(): GetShipmentList Api call: START");
			Document outDocShpLst = AcademyUtil.invokeService(env,AcademyConstants.FLOW_GET_SHP_LIST_ACKSLIP, inDoc);
			log.endTimer("AcademyBOPISPrintAckSlip.class:printAckSlip(): GetShipmentList Api call: END");
			log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip(): GetShipmentList Api call: outDoc"+outDocShpLst);
			YFCDocument yfcOutDocShpLst = YFCDocument.getDocumentFor(outDocShpLst);

			YFCElement eleOutDocShpListShp = yfcOutDocShpLst.getDocumentElement().getChildElement(AcademyConstants.ELE_SHIPMENT);
			YFCNodeList<YFCElement> nlShipmentLineShpLst = eleOutDocShpListShp.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

			log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip(): Generate Print Document : Start");
			YFCDocument outDocPrint = YFCDocument.createDocument(AcademyConstants.ELE_PRINT_DOCUMENTS);
			YFCElement elePrintDocuments = outDocPrint.getDocumentElement();
			elePrintDocuments.setAttribute(AcademyConstants.ATTR_FLUSH_TO_PRINTER,"Y");
			elePrintDocuments.setAttribute(AcademyConstants.ATTR_PRINT_NAME,AcademyConstants.ATTR_PRINT_NAME_ACKSLIP);

			YFCElement elePrintDocument = elePrintDocuments.createChild(AcademyConstants.ELE_PRINT_DOCUMENT);
			String strDocumentID = props.getProperty(AcademyConstants.KEY_DOCUMENT_ID_VALUE);
			String strPrinter = props.getProperty(AcademyConstants.KEY_PRINTER_ID_VALUE);
			log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip(): DocumentID/PrinterId "+strDocumentID+"/"+strPrinter);
			elePrintDocument.setAttribute(AcademyConstants.ATTR_DOCUMENT_ID,strDocumentID);
			elePrintDocument.setAttribute(AcademyConstants.ATTR_DATA_ELEMENT_PATH,AcademyConstants.VAL_DATA_ELEMENT_PATH);

			String strShipNode = eleOutDocShpListShp.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
			YFCElement elePrinterPref = elePrintDocument.createChild(AcademyConstants.ELE_PRINT_PREFERENCE);
			elePrinterPref.setAttribute(AcademyConstants.ORGANIZATION_CODE,strShipNode);
			elePrinterPref.setAttribute(AcademyConstants.ATTR_PRINTER_ID,strPrinter);

			String strHeader = props.getProperty(AcademyConstants.KEY_HEADER_VALUE);
			String strDisclaimer = props.getProperty(AcademyConstants.ATTR_DISCLAIMER);
			log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip(): HEADER/DISCLAIMER "+strHeader+"/"+strDisclaimer);
			YFCElement eleInputData = elePrintDocument.createChild(AcademyConstants.ELE_INPUT_DATA);
			YFCElement elePrintShipment = eleInputData.createChild(AcademyConstants.ELE_SHIPMENT);
			elePrintShipment.setAttribute(AcademyConstants.KEY_HEADER_VALUE, strHeader);
			elePrintShipment.setAttribute(AcademyConstants.ATTR_DISCLAIMER,strDisclaimer);

			String strPageSize = props.getProperty(AcademyConstants.ATTR_PAGE_SIZE);
			log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip(): PageSize-"+strPageSize);
			YFCElement eleBillToAddress = eleOutDocShpListShp.getChildElement(AcademyConstants.ELE_BILL_TO_ADDRESS);
			elePrintShipment = stampPickdUpDate(elePrintShipment,eleOutDocShpListShp);
			//Stamp Customer Name in single attribute for loftware alignment.
			eleBillToAddress = updateCustName(eleBillToAddress);
			elePrintShipment.importNode(eleBillToAddress);
			elePrintShipment = stampPickedupPerson(elePrintShipment,nlShipmentLineShpLst);

			HashMap<String,YFCElement> hmpShipLineForPrint = stampShpLineForPrint(nlShipmentLineShpLst);
			
			callPrintServer(env,outDocPrint,strPageSize,hmpShipLineForPrint);


		}
		catch(Exception e)
		{
			String strError = e.getMessage();
			YFSException ex = new YFSException(strError);
			ex.setErrorCode("ACK_SLIP_001");
			ex.setErrorDescription("Acknowledgement Slip printing caused the exception");
			ex.printStackTrace();
			throw ex;
		}
		//Return success if the process got completed without exceptions.
		YFCDocument Success = YFCDocument.createDocument("Success");
		return Success.getDocument();
	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		props = arg0;
		
	}
	
	/**
	 * This method stamps first name and last name of bill to address as customer name for loftware alingment.
	 * @param eleBillToAddress
	 * @return
	 */
	public YFCElement updateCustName(YFCElement eleBillToAddress)
	{
		log.beginTimer("AcademyBOPISPrintAckSlip.class:printAckSlip():printAckSlip():updateCustName()   START");
		log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():printAckSlip():updateCustName()  in BillToAddress"+eleBillToAddress);
		String strCustomerName = eleBillToAddress.getAttribute(AcademyConstants.ATTR_FNAME)+" "+ eleBillToAddress.getAttribute(AcademyConstants.ATTR_LNAME);
		eleBillToAddress.setAttribute(AcademyConstants.ATTR_CUST_NAME, strCustomerName);
		log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():printAckSlip():updateCustName()  out BillToAddress"+eleBillToAddress);
		log.endTimer("AcademyBOPISPrintAckSlip.class:printAckSlip():printAckSlip():updateCustName()   END");
		return eleBillToAddress;
	}
	
	/**
	 * This method stamps the picked up date in the print document and set boolean used to set the pickup person 
	 * name from PersonInfoMarkFor.
	 * @param elePrintShipment
	 * @param eleOutDocShpListShp
	 * @return
	 * @throws ParseException 
	 */
	public YFCElement stampPickdUpDate(YFCElement elePrintShipment,YFCElement eleOutDocShpListShp) throws ParseException
	{
		log.beginTimer("AcademyBOPISPrintAckSlip.class:printAckSlip():stampPickdUpDate():   START");
		log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():stampPickdUpDate() - IN PrintShipment" + elePrintShipment.toString());
		log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():stampPickdUpDate() - ShpList Shipment" + eleOutDocShpListShp.toString());
		YFCElement eleAdditionalDates = eleOutDocShpListShp.getChildElement(AcademyConstants.E_ADDITIONAL_DATES);
		YFCNodeList<YFCElement> nlAdditionalDate = eleAdditionalDates.getElementsByTagName(AcademyConstants.E_ADDITIONAL_DATE);
		for(YFCElement eleAdditionalDate : nlAdditionalDate)
		{
			String strDateTypeID = eleAdditionalDate.getAttribute(AcademyConstants.A_DATE_TYPE_ID);
			if(YFCCommon.equalsIgnoreCase(AcademyConstants.DATE_TYPE_PICKUP_DATE_ACKSLIP, strDateTypeID))
			{
				//format data format before sending to loftware.
				String strPickUpDate =  eleAdditionalDate.getAttribute(AcademyConstants.ATTR_ACTUAL_DATE);
				//format pickup date.
				if(!YFCCommon.isVoid(strPickUpDate))
				{
					//Start BOPIS-1508	BOPIS:: Pick-Up Acknowledgement Slip Updates
					//strPickUpDate= strPickUpDate.split("T")[0]+" "+strPickUpDate.split("T")[1];
					SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_SIMPLE_DATE_PATTERN);
					Date dtPickUpDate = sdf.parse(strPickUpDate.split("T")[0]);		
					sdf = new SimpleDateFormat(AcademyConstants.STR_PRINT_SHIPPING_LABEL_DATE);
					strPickUpDate = sdf.format(dtPickUpDate);
					log.debug("strPickUpDate Formatted "+strPickUpDate);
					//End BOPIS-1508	BOPIS:: Pick-Up Acknowledgement Slip Updates
				}
				elePrintShipment.setAttribute(AcademyConstants.ATTR_PICKEDUP_DATE,strPickUpDate);
				break;
			}
		}
		
		//Stamp PickUpCustomer Name at shipment level.
		YFCElement eleExtn = eleOutDocShpListShp.getChildElement(AcademyConstants.ELE_EXTN);
		String strShipmentPickedBy = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_SHIP_PICKEDBY);
		if(YFCCommon.equalsIgnoreCase(AcademyConstants.ATTR_VAL_ALTERNATE, strShipmentPickedBy))
		{
			log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():stampPickdUpDate(): SHipmentPickedBy "+strShipmentPickedBy);
			//using boolean to fetch the info from the Order Line level as person info mark for is at 
			//ShipmentLine/OrderLine level.
			isPersonInfoMarkForReq = true;
		}
		log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():stampPickdUpDate() -OUT PrintShipment" + elePrintShipment.toString());
		log.endTimer("AcademyBOPISPrintAckSlip.class:printAckSlip():stampPickdUpDate():   END");
		return elePrintShipment;
	}
	
	/**
	 * This method creates a map with non zero QTY shipment lines.
	 * @param nlShipmentLineShpLst
	 * @return
	 */
	public HashMap<String, YFCElement> stampShpLineForPrint(YFCNodeList<YFCElement> nlShipmentLineShpLst)
	{
		log.beginTimer("AcademyBOPISPrintAckSlip.class:printAckSlip():stampShpLineForPrint() - START");
		HashMap<String, YFCElement> hmpShipLineForPrint = new HashMap<String, YFCElement>();
		for(YFCElement eleShipmentLine: nlShipmentLineShpLst)
		{
			String strShipmentLineKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
			String strQty = eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
			double dQty = Double.parseDouble(strQty);
			log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():stampShpLineForPrint() - ShpLineKey/Qty "+strShipmentLineKey+"/"+dQty);
			if(dQty!=0 && !hmpShipLineForPrint.containsKey(strShipmentLineKey))
			{
				//Stamp Shipment line level attributes.
				eleShipmentLine = updateShpLneLevelAttributes(eleShipmentLine);
				hmpShipLineForPrint.put(strShipmentLineKey, eleShipmentLine);
			}
		}
		log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():stampShpLineForPrint() - Map with non zero Qty ShipmentLine"+ hmpShipLineForPrint);
		log.endTimer("AcademyBOPISPrintAckSlip.class:printAckSlip():stampShpLineForPrint() - END");
		return hmpShipLineForPrint;
	}
	
	/**
	 * This method stamps the pickup person name based on the boolean isPersonInfoMarkForReq.
	 * @param elePrintShipment
	 * @param nlShipmentLineShpLst
	 * @return
	 */
	public YFCElement stampPickedupPerson(YFCElement elePrintShipment,YFCNodeList<YFCElement> nlShipmentLineShpLst)
	{
		log.beginTimer("AcademyBOPISPrintAckSlip.class:printAckSlip():stampPickedupPerson():   START");
		log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():stampPickedupPerson(): IN Print Shipment "+elePrintShipment.toString());
		YFCElement eleBillTo = elePrintShipment.getChildElement(AcademyConstants.ELE_BILL_TO_ADDRESS);
		YFCElement eleShipmentLine = nlShipmentLineShpLst.item(0);
		YFCElement eleOrderLine = eleShipmentLine.getChildElement(AcademyConstants.ELE_ORDER_LINE);
		YFCElement elePersonInfoMarkFor = eleOrderLine.getChildElement(AcademyConstants.ELE_PERSON_INFO_MARK_FOR);
		String strPickupPerson = null;
		if(isPersonInfoMarkForReq)
		{
			strPickupPerson = elePersonInfoMarkFor.getAttribute(AcademyConstants.ATTR_FNAME)+" "+elePersonInfoMarkFor.getAttribute(AcademyConstants.ATTR_LNAME);
			log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():stampPickedupPerson():Pickup Person(Mark For) "+ strPickupPerson);
			elePrintShipment.setAttribute(AcademyConstants.ATTR_PICKUP_PERSON,strPickupPerson);
		}
		else
		{
			strPickupPerson = eleBillTo.getAttribute(AcademyConstants.ATTR_FNAME)+" "+eleBillTo.getAttribute(AcademyConstants.ATTR_LNAME);
			log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():stampPickedupPerson():Pickup Person(Bill to) "+ strPickupPerson);
			elePrintShipment.setAttribute(AcademyConstants.ATTR_PICKUP_PERSON,strPickupPerson);
		}
		log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():stampPickedupPerson(): Out Print Shipment "+elePrintShipment.toString());
		log.endTimer("AcademyBOPISPrintAckSlip.class:printAckSlip():stampPickedupPerson():   END");
		return elePrintShipment;
	}
	
	/**
	 * This method updates the shipment line level attributes.
	 * @param eleShipmentLine
	 * @return
	 */
	public YFCElement updateShpLneLevelAttributes(YFCElement eleShipmentLine)
	{
		log.beginTimer("AcademyBOPISPrintAckSlip.class:printAckSlip():stampShpLineForPrint():updateShpLneLevelAttributes()   START");
		log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():stampPickedupPerson(): In ShipmentLine "+eleShipmentLine.toString());
		//update Item Short description to have Item ID.
		YFCElement eleOrderLine = eleShipmentLine.getChildElement(AcademyConstants.ELE_ORDER_LINE);
		YFCElement eleItem = eleOrderLine.getChildElement(AcademyConstants.ITEM);
		String strItemShortDesc = eleItem.getAttribute(AcademyConstants.ATTR_ITEM_SHORT_DESC);
		String strItemId = eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID);
		strItemShortDesc = strItemShortDesc+" ("+strItemId+")";
		eleItem.setAttribute(AcademyConstants.ATTR_ITEM_SHORT_DESC,strItemShortDesc);
		
		//Update OrderLine/LinePrice - Unit price to incorporate dollar sign.
		YFCElement eleLinePriceInfo = eleOrderLine.getChildElement(AcademyConstants.ELE_LINEPRICE_INFO);
		String strUnitPrice = eleLinePriceInfo.getAttribute(AcademyConstants.ATTR_UNIT_PRICE);
		strUnitPrice = "$ "+strUnitPrice;
		eleLinePriceInfo.setAttribute(AcademyConstants.ATTR_UNIT_PRICE,strUnitPrice);
		
		//Update City/state/Zipcode of Shipment Line for loftware alignment.
		YFCElement elePersonInfoShipTo = eleOrderLine.getChildElement(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO);
		String strLocation = elePersonInfoShipTo.getAttribute(AcademyConstants.ATTR_CITY)+" "
		+elePersonInfoShipTo.getAttribute(AcademyConstants.ATTR_STATE)+" "+elePersonInfoShipTo.getAttribute(AcademyConstants.ZIP_CODE);
		elePersonInfoShipTo.setAttribute(AcademyConstants.ATTR_LOCATION,strLocation);
		
		log.endTimer("AcademyBOPISPrintAckSlip.class:printAckSlip():stampShpLineForPrint():updateShpLneLevelAttributes()   END");
		log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():stampPickedupPerson(): Out ShipmentLine "+eleShipmentLine.toString());
		return eleShipmentLine;
	}
	
	/**
	 * This method performs pagination and call print service to send the input to loftware and hence print the document.
	 * @param env
	 * @param outDocPrint
	 * @param strPageSize
	 * @param hmpShipLineForPrint
	 */
	public void callPrintServer(YFSEnvironment env,YFCDocument outDocPrint,String strPageSize,HashMap<String, YFCElement> hmpShipLineForPrint)
	{
		log.beginTimer("AcademyBOPISPrintAckSlip.class:printAckSlip():callPrintServer():   START");
		log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():callPrintServer():outDocPrint "+ outDocPrint.toString());
		String strPrintService = props.getProperty(AcademyConstants.KEY_PRINT_FLOW);
		log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():callPrintServer(): PrintService "+strPrintService);
		int intPageNo=0;
		double dPageSize = Double.parseDouble(strPageSize);
		double dTotalShpLines = hmpShipLineForPrint.size();
		double dTotalNoOfPages = Math.ceil(dTotalShpLines/dPageSize);
		String strTotalPageNo = String.valueOf(dTotalNoOfPages).split("\\.")[0];
		log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():callPrintServer():TapptalPageCount "+ strTotalPageNo);
		YFCNodeList<YFCElement> nlPrintShipment = outDocPrint.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
		YFCElement elePrintShipment = nlPrintShipment.item(0);
		elePrintShipment.setAttribute(AcademyConstants.ATTR_TOTAL_PAGE_COUNT,strTotalPageNo);
		YFCElement eleShipmentLines = elePrintShipment.createChild(AcademyConstants.ELE_SHIPMENT_LINES);
		try
		{
			if(hmpShipLineForPrint.size()>dPageSize)
			{
				int count=0,pageNo=0,mapSize=0;
				//Pagination is required.
				for(YFCElement elePrintDocShpLine: hmpShipLineForPrint.values())
				{
					count++;
					mapSize++;
					elePrintDocShpLine.setAttribute(AcademyConstants.ATTR_SERIAL_NO,mapSize);
					eleShipmentLines.importNode(elePrintDocShpLine);
					if(count==dPageSize || hmpShipLineForPrint.size()==mapSize)
					{
						pageNo++;
						elePrintShipment.setAttribute(AcademyConstants.ATTR_PAGE_NO,pageNo);
						log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():callPrintServer(): PageNo"+pageNo);
						
						log.beginTimer("AcademyBOPISPrintAckSlip.class:printAckSlip():callPrintServer(): PrintService :START");
						log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():callPrintServer(): PrintService input"+outDocPrint.toString());
						AcademyUtil.invokeService(env,strPrintService, outDocPrint.getDocument());
						log.endTimer("AcademyBOPISPrintAckSlip.class:printAckSlip():callPrintServer(): PrintService :END");
						count=0;
						elePrintShipment.removeChild(elePrintShipment.getChildElement(AcademyConstants.ELE_SHIPMENT_LINES));
						eleShipmentLines = elePrintShipment.createChild(AcademyConstants.ELE_SHIPMENT_LINES);
					}
					
				}
			}
			else
			{
				int mapSize =0;
				//Send all the shipment lines of the map in one document.
				elePrintShipment.setAttribute(AcademyConstants.ATTR_PAGE_NO,strTotalPageNo);
				for(YFCElement elePrintDocShpLine: hmpShipLineForPrint.values())
				{
					mapSize++;
					elePrintDocShpLine.setAttribute(AcademyConstants.ATTR_SERIAL_NO,mapSize);
					eleShipmentLines.importNode(elePrintDocShpLine);
				}
				log.beginTimer("AcademyBOPISPrintAckSlip.class:printAckSlip():callPrintServer(): PrintService Without pagination Logic:START");
				log.debug("AcademyBOPISPrintAckSlip.class:printAckSlip():callPrintServer(): PrintService input"+outDocPrint.toString());
				AcademyUtil.invokeService(env,strPrintService, outDocPrint.getDocument());
				log.endTimer("AcademyBOPISPrintAckSlip.class:printAckSlip():callPrintServer(): PrintService Without pagination Logic:END");
			}
		}
		catch(Exception e)
		{
			String strError = e.getMessage();
			YFSException ex = new YFSException(strError);
			ex.setErrorCode("ACK_SLIP_001");
			ex.setErrorDescription("Acknowledgement Slip printing caused the exception");
			ex.printStackTrace();
			throw ex;
		}
		
		log.endTimer("AcademyBOPISPrintAckSlip.class:printAckSlip():callPrintServer():   END");
	}
}
