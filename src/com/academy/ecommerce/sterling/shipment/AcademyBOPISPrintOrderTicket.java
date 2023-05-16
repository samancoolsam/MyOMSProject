package com.academy.ecommerce.sterling.shipment;

import java.text.DecimalFormat;
import java.util.Properties;

import org.w3c.dom.Document;

import com.academy.util.xml.XMLUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class is used to print Order Ticket.
 * @author Abhishek Aggarwal
 *
 */
public class AcademyBOPISPrintOrderTicket implements YIFCustomApi
{
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademyBOPISPrintOrderTicket.class);
	private Properties props;
	@Override
	public void setProperties(Properties props) throws Exception
	{
		this.props = props;
	}
	public Document printOrderTicket(YFSEnvironment env,Document inDoc) throws Exception
	{
		log.verbose("AcademyBOPISPrintOrderTicket.java:printOrderTicket():InDoc"+inDoc);
		//The input contains the input for getShipmentList.
		Document outDocGetShpLst = AcademyUtil.invokeService(env,"AcademyBOPISOrderTicketShpListService",XMLUtil.getXMLString(inDoc));
		
		log.verbose("AcademyBOPISPrintOrderTicket.java:printOrderTicket():outDoc getShipmentList"+XMLUtil.getXMLString(outDocGetShpLst));
		//Fetch the elements from getShipmentList Output.
		YFCDocument yfcOutDocShpList = YFCDocument.getDocumentFor(outDocGetShpLst);
		YFCElement eleOutDocShpListShpmts = yfcOutDocShpList.getDocumentElement();
		YFCElement eleOutDocShpListShpmt = eleOutDocShpListShpmts.getChildElement(AcademyConstants.ELE_SHIPMENT);
		YFCElement eleShipNode = eleOutDocShpListShpmt.getChildElement(AcademyConstants.ATTR_SHIP_NODE);
		YFCElement eleBillToAddress = eleOutDocShpListShpmt.getChildElement(AcademyConstants.ELE_BILL_TO_ADDRESS);
		YFCElement eleShipmentLines = eleOutDocShpListShpmt.getChildElement(AcademyConstants.ELE_SHIPMENT_LINES);
		YFCNodeList<YFCElement> nlShipmentLine = eleShipmentLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		YFCNodeList<YFCElement> nlShipStatusAudit = eleOutDocShpListShpmt.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_STATUS_AUDIT);
		int totalShipLne = nlShipmentLine.getLength();
		
		//fetch printer name ,Comment , Header and document id from arguments.
		String strDocumtID = props.getProperty(AcademyConstants.KEY_DOCUMENT_ID_VALUE);
		String strPrinterId = props.getProperty(AcademyConstants.KEY_PRINTER_ID_VALUE);
		String strHeader = props.getProperty(AcademyConstants.KEY_HEADER_VALUE);
		String strComment = props.getProperty(AcademyConstants.KEY_COMMENT_VALUE);
		String strPickUpDate = fetchPickUpDate(nlShipStatusAudit);

		//Create input document for printDocument Set.
		YFCDocument outDocPrint = YFCDocument.createDocument(AcademyConstants.ELE_PRINT_DOCUMENTS);
		YFCElement elePrintDocumts = outDocPrint.getDocumentElement();
		elePrintDocumts.setAttribute(AcademyConstants.ATTR_FLUSH_TO_PRINTER,AcademyConstants.STR_YES);
		elePrintDocumts.setAttribute(AcademyConstants.ATTR_IS_PICKTICKET_PRINTED,AcademyConstants.STR_YES);
		elePrintDocumts.setAttribute(AcademyConstants.ATTR_PRINT_NAME,AcademyConstants.VAL_ORDER_TICKET_PRINT);
		
		YFCElement elePrintDocumt = elePrintDocumts.createChild(AcademyConstants.ELE_PRINT_DOCUMENT);
		elePrintDocumt.setAttribute(AcademyConstants.ATTR_DOCUMENT_ID, strDocumtID);
		elePrintDocumt.setAttribute(AcademyConstants.ATTR_DATA_ELEMENT_PATH, AcademyConstants.VAL_DATA_ELEMENT_PATH);
		
		YFCElement elePrinterPref = elePrintDocumt.createChild(AcademyConstants.ELE_PRINT_PREFERENCE);
		elePrinterPref.setAttribute(AcademyConstants.ORGANIZATION_CODE, eleShipNode.getAttribute(AcademyConstants.ATTR_SHIP_NODE));
		elePrinterPref.setAttribute(AcademyConstants.ATTR_PRINTER_ID, strPrinterId);
		
		YFCElement eleInputData = elePrintDocumt.createChild(AcademyConstants.ELE_INPUT_DATA);
		YFCElement eleShipmnt = eleInputData.createChild(AcademyConstants.ELE_SHIPMENT);
		eleShipmnt.setAttribute(AcademyConstants.ATTR_HEADER, strHeader);
		eleShipmnt.setAttribute(AcademyConstants.ATTR_COMMENT, strComment);
		eleShipmnt.setAttribute(AcademyConstants.STR_STORE, eleShipNode.getAttribute(AcademyConstants.ATTR_SHIP_NODE));
		// Start Changes for BOPIS-1989
		//Concatenating  FisrstName and Last Name together with a sapce in between and setting it as first name. 
		// The last name is set with blank value 
		String strFirstName = eleBillToAddress.getAttribute(AcademyConstants.ATTR_FNAME)+" ";
		String strLastname =eleBillToAddress.getAttribute(AcademyConstants.ATTR_LNAME);
		strFirstName=strFirstName+strLastname;
		eleShipmnt.setAttribute(AcademyConstants.ATTR_FNAME,strFirstName);
		eleShipmnt.setAttribute(AcademyConstants.ATTR_LNAME, "");
		// End Changes for BOPIS-1989  
		// Concatenating the FisrstName and Last Name together with a sapce in between and setting it as first name. 
		//The last name is set with blank value 
		eleShipmnt.setAttribute(AcademyConstants.ATTR_PICKUP_DATE, strPickUpDate);
		
		//Create Item element for each shipment Line quantity and call print document Set.
		YFCElement eleItemOutDoc = eleShipmnt.createChild(AcademyConstants.ITEM);
		Double dQty=0.0;
		String strOrderNo="";
		//Fetch total quantity and Order No from Shipment Line.
		for(YFCElement eleShipmentLine: nlShipmentLine)
		{
			strOrderNo=eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			String strQuantity = eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
			dQty = dQty+Double.parseDouble(strQuantity);
		}
		DecimalFormat format = new DecimalFormat("0.#");
		eleShipmnt.setAttribute(AcademyConstants.ATTR_TOTAL_PAGE_COUNT, format.format(dQty));
		eleShipmnt.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		
		int intPageCount = 0;
		String alternateFirstName= null;
		String alternateLastName=null;
		for(YFCElement eleShipmentLine: nlShipmentLine)
		{
			YFCElement eleOrderLine = eleShipmentLine.getChildElement(AcademyConstants.ELE_ORDER_LINE);
			//Start Changes for BOPIS-1989 Adding  Alternate Pickup Person name on the ticket 
		 
			  YFCElement eleShipmentLineForAlternatePickUp = eleOrderLine.getChildElement(AcademyConstants.PERSON_INFO_MARK_FOR);
			  if(!YFSObject.isVoid(eleShipmentLineForAlternatePickUp))
			   {
				log.verbose("The element PersonInfoMarkFor is present");
				 alternateFirstName= eleShipmentLineForAlternatePickUp.getAttribute(AcademyConstants.ATTR_FNAME);
				 alternateLastName = eleShipmentLineForAlternatePickUp.getAttribute(AcademyConstants.ATTR_LNAME);
				if (alternateFirstName != null && !YFCObject.isVoid(alternateFirstName) &&
						alternateLastName != null &&!YFCObject.isVoid(alternateLastName))
				{   
					log.verbose("Alternate PickUp Person's First name is: "+alternateFirstName);
					log.verbose("Alternate PickUp Person's Last name is: "+alternateLastName);
				
					alternateFirstName=alternateFirstName+" "+alternateLastName;
					log.verbose("The Aleternate Pick Up Person Detail: "+alternateFirstName);
					eleShipmnt.setAttribute(AcademyConstants.ALTERNATE_PICKUP_PERSON,alternateFirstName);
			    }
			}
			
			//End Changes for BOPIS-1989 Adding Alternate Pickup Person name on the ticket 
			 
						
			YFCElement eleIteminDoc = eleOrderLine.getChildElement(AcademyConstants.ITEM);
			String strQty  = eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
			double dShpLneQty = Double.parseDouble(strQty); 
			if(dShpLneQty>0)
			{
				String strItemID = eleIteminDoc.getAttribute(AcademyConstants.ATTR_ITEM_ID);
				String strUPC = AcademyUtil.getItemAliasValueForItem(strItemID, env);
				eleItemOutDoc.setAttribute(AcademyConstants.ATTR_UPC,strUPC);
				eleItemOutDoc.setAttribute(AcademyConstants.ATTR_ITEM_ID,strItemID);
				eleItemOutDoc.setAttribute(AcademyConstants.ATTR_ITEM_SHORT_DESC, eleIteminDoc.getAttribute(AcademyConstants.ATTR_ITEM_SHORT_DESC));
				for(int qtyCount=0;qtyCount<dShpLneQty;qtyCount++)
				{
					intPageCount++;
					eleItemOutDoc.setAttribute(AcademyConstants.ATTR_PAGE_COUNT, intPageCount);
					log.verbose("AcademyBOPISPrintOrderTicket.java:printOrderTicket():outDoc printDocumentSet: "+outDocPrint.toString());;
					AcademyUtil.invokeService(env,"AcademyBOPISPrintOrderTicket", outDocPrint.getDocument());
				}
				
			}
		}
		return inDoc;
	}
	/**
	 * This method fetches the pick update.
	 * @param nlShipStatusAudit
	 * @return
	 */
	public String fetchPickUpDate(YFCNodeList<YFCElement> nlShipStatusAudit)
	{
		
		String strPickUpDate="";
		for(YFCElement eleShipAudit : nlShipStatusAudit)
		{
			String strNewStatus = eleShipAudit.getAttribute(AcademyConstants.ATTR_NEW_STATUS);
			if(YFCCommon.equalsIgnoreCase("1100.70.06.30.5",strNewStatus))
			{
				String strStatusDate = eleShipAudit.getAttribute("NewStatusDate");
				String[] strDate = strStatusDate.split("T");
				String[] strYYYYMMDD =  strDate[0].split("-");		
				//BOPIS-1693 Change Date Format - Start
				strPickUpDate = strYYYYMMDD[1]+"/"+strYYYYMMDD[2]+"/"+strYYYYMMDD[0];
				//BOPIS-1693 Change Date Format - End
				break;
			}
		}
		log.verbose("AcademyBOPISPrintOrderTicket.fetchPickUpDate():PickUpdate: "+strPickUpDate);
		return strPickUpDate;
	}
}