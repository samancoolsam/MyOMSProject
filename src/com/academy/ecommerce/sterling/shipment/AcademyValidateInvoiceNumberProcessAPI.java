
package com.academy.ecommerce.sterling.shipment;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.date.YTimestamp;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyValidateInvoiceNumberProcessAPI implements YIFCustomApi
{
	private static YFCLogCategory	log					= YFCLogCategory.instance(AcademyValidateInvoiceNumberProcessAPI.class);
	private Document				outDoc				= null;
	private String					strOrderInvoiceKey	= null;

	public Document validateInvoiceNumber(YFSEnvironment env, Document docShipment) throws Exception
	{
		log.beginTimer("Begining of AcademyValidateInvoiceNumberProcessAPI-> validateInvoiceNumber Api");
		String strCalendarKey = "";
		String sInvoiceDate = "";
		String strDocumentType = ""; //Added as a part of WN-63 Tlogs getting created with future date InvoiceNos
		Element eleShipment = docShipment.getDocumentElement();
		
		
		String ipPoDate = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");
        
		if( eleShipment.hasAttribute("IPPurchaseOrderDate")){
		    sInvoiceDate = eleShipment.getAttribute("IPPurchaseOrderDate");
		    if(!YFCObject.isVoid(sInvoiceDate) && !YFCObject.isVoid(sInvoiceDate)){
		        log.verbose("IPPODATE is : " + sInvoiceDate );
		        ipPoDate = sInvoiceDate;
	            sInvoiceDate = sInvoiceDate.substring(0, 10);
	            log.verbose("After change IPPODATE is : " + sInvoiceDate );
	            sInvoiceDate = dateFormat.format(dateFormat.parse(sInvoiceDate));
	            log.verbose("***IPPO Date for comparison is ready*****" +sInvoiceDate);
		    }
		}
		String strScac = eleShipment.getAttribute("SCAC");
		strOrderInvoiceKey = eleShipment.getAttribute("OrderInvoiceKey");
		log.verbose("***OrderInvoiceKey is" + strOrderInvoiceKey);
		String strExtnInvoiceNo = eleShipment.getAttribute("ExtnInvoiceNo");
		log.verbose("***ExtnInvoiceNo is" + strExtnInvoiceNo);
		String strExistingInvoiceDate = strExtnInvoiceNo.substring(0, 8);

		

		strExistingInvoiceDate = dateFormat.format(dateFormat1.parse(strExistingInvoiceDate));
		log.verbose("***Formatted ExtnInvoiceNo for comparison is ready*****" + strExistingInvoiceDate);
		String strShipmentKey = eleShipment.getAttribute("ShipmentKey");
		log.verbose("Shipment SCAC: " + strScac);
		log.verbose("Shipment Key: " + strShipmentKey);
		
		//START WN-63 Tlogs getting created with future date InvoiceNos	
		strDocumentType = eleShipment.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
		log.verbose("Document Type : " + strDocumentType);
		//END WN-63 Tlogs getting created with future date InvoiceNos	
		
		// Start Change for STL-244
		String strShipNode = eleShipment.getAttribute("ShipNode");
		log.verbose("Shipment ShipNode: " + strShipNode);
	//DSVCHANGE
	if(!eleShipment.hasAttribute("IPPurchaseOrderDate")){
		// if (!YFCObject.isVoid(strScac) && !YFCObject.isVoid(strShipmentKey))
		// {
		if (!YFCObject.isVoid(strScac) && !YFCObject.isVoid(strShipmentKey))
		{
			strCalendarKey = getBusinessCalendarKey(env, strScac, strShipNode);
		}
		// End Change
		if (!YFCObject.isVoid(strCalendarKey))
		{
			sInvoiceDate = getInvoiceDate(env, strCalendarKey);
			log.verbose("sInvoiceDate....: " + sInvoiceDate);
		}
	}
	//DSVCHANGE END
		/* Format the invoice data and call invoice Number UE logic */
		if (sInvoiceDate.equals(strExistingInvoiceDate))
		{
			log.verbose("***Invoice Number needn't be regenerated as its within the cut-off time");
			outDoc = XMLUtil.createDocument("InvoiceHeader");
			
			//DSVCHANGE For adding the IPPurchaseOrderDate TS to the existing ExtnInvoiceNo 
			if(eleShipment.hasAttribute("IPPurchaseOrderDate")){
			    //Prepare new ExtnInvoiceNo with IPPurchaseOrder Date and Time
			    strExtnInvoiceNo = prepareExtnInvoiceNo(strExtnInvoiceNo, ipPoDate);
			    
			    outDoc.getDocumentElement().setAttribute(
	                    AcademyConstants.ATTR_INVOICE_NO, strExtnInvoiceNo);
	            outDoc.getDocumentElement().setAttribute(
	                    "RegeneratedInvoiceNumber", "Y");
			}
			else{
				log.verbose("***Invoice Number as same existing invoice ::" +strExtnInvoiceNo);
			    outDoc.getDocumentElement().setAttribute(
	                    AcademyConstants.ATTR_INVOICE_NO, strExtnInvoiceNo);
	            outDoc.getDocumentElement().setAttribute(
	                    "RegeneratedInvoiceNumber", "N");
			}
			//DSVCHANGE END
			return outDoc;
		} 
		
		//START WN-63 Tlogs getting created with future date InvoiceNos		
		else if (!YFCObject.isVoid(strDocumentType) && strDocumentType.equals(AcademyConstants.SALES_DOCUMENT_TYPE) ) {
		outDoc = XMLUtil.createDocument(AcademyConstants.ELE_INVOICE_HDR);
		outDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_INVOICE_NO, strExtnInvoiceNo);
        outDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_REGENERATED_INVOICE_NO, AcademyConstants.STR_NO);
        return outDoc;
        //END WN-63 Tlogs getting created with future date InvoiceNos	
		}
		
		else
		{
			YIFApi yifApi;
			YFSEnvironment envNew;
			yifApi = YIFClientFactory.getInstance().getLocalApi();
			Document docEnv = XMLUtil.createDocument(AcademyConstants.ELE_ENV);
			docEnv.getDocumentElement().setAttribute(AcademyConstants.ATTR_USR_ID, env.getUserId());
			docEnv.getDocumentElement().setAttribute(AcademyConstants.ATTR_PROG_ID, env.getProgId());
			envNew = yifApi.createEnvironment(docEnv);
			Document docForInvoiceSeq = XMLUtil.createDocument(AcademyConstants.ELE_INVOICE_HDR);
			docForInvoiceSeq.getDocumentElement().setAttribute("InvoicedDate", sInvoiceDate);
			// Step 2 - Call the service to lock dummy record and update invoice
			// no
			Document docCurrentInvoiceSeqNo = yifApi.executeFlow(envNew, "AcademyLockRecordForInvoice", docForInvoiceSeq);

			// Step 3 - Release the environment
			yifApi.releaseEnvironment(envNew);
			//DSVCHANGE
			prepareInvoiceNumber(env, docCurrentInvoiceSeqNo , docShipment);
			//DSVCHANGE END
		}
		log.endTimer("End of AcademyValidateInvoiceNumberProcessAPI-> validateInvoiceNumber Api");
		return outDoc;
	}

	private String prepareExtnInvoiceNo(String strExtnInvoiceNo, String ipPoDate) {
        // TODO Auto-generated method stub
	    YTimestamp ipPoDateTimeStamp= YTimestamp.newTimestamp(ipPoDate);// newTimestamp(dateWithTime);
        String ipPoDateTime =  ipPoDateTimeStamp.getString("yyyyMMddHHmmss");
        ipPoDateTime = ipPoDateTime.substring(0, ipPoDateTime.length()-2) + "00";
        log.verbose("ipPoDateTime  = "  + ipPoDateTime);
        strExtnInvoiceNo = ipPoDateTime  + strExtnInvoiceNo.substring(14, strExtnInvoiceNo.length());
        log.verbose("strExtnInvoiceNo edited =====> " + strExtnInvoiceNo);
        return strExtnInvoiceNo;
    }

    private String getBusinessCalendarKey(YFSEnvironment env, String strScac, String strShipNode)
			throws ParserConfigurationException, SAXException, IOException,
			Exception {
		log.verbose("**** Inside getBusinessCalendarKey method ******");
		String strCalendarKey = "";
		// Start Change for STL-244
		if (!YFCObject.isVoid(strShipNode))
		{ // getting calendar from store for the carrier if available
			// Start: Process API getOrganizationHierarchy
			// Create root element Organization
			Document docGetOrgDtls = XMLUtil.createDocument("Organization");
			// Fetch attribute value for OrganizationCode
			docGetOrgDtls.getDocumentElement().setAttribute("OrganizationCode", strShipNode);
			// Set the template for getOrganizationHierarchy
			Document outTempGetOrgDtls = YFCDocument.parse("<Organization OrganizationCode=\"\"><Calendars> <Calendar CalendarKey=\"\" CalendarId=\"\"/></Calendars></Organization>").getDocument();
			env.setApiTemplate("getOrganizationHierarchy", outTempGetOrgDtls);
			// Invoke Api getOrganizationHierarchy
			Document docOutputOrgDtls = AcademyUtil.invokeAPI(env, "getOrganizationHierarchy", docGetOrgDtls);
			log.verbose("getOrgHierarchy API output for : " + strShipNode + " " + XMLUtil.getXMLString(docOutputOrgDtls));
			// Clear template
			env.clearApiTemplates();
			// End: Process API getOrganizationHierarchy
			// Fetch the NodeList of element Calender
			NodeList calendarList = docOutputOrgDtls.getDocumentElement().getElementsByTagName("Calendar");
			// Loop through the NodeList record
			for (int i = 0; i < calendarList.getLength(); i++)
			{
				// Fetch the element Calender
				Element calendar = (Element) calendarList.item(i);
				// Fetch the attribute value of CalendarId
				String calendarId = calendar.getAttribute("CalendarId");
				// Check if the value of CalendarId =SCAC value
				if (calendarId.equalsIgnoreCase(strScac))
				{
					// Fetch the attribute value of CalendarKey
					strCalendarKey = calendar.getAttribute("CalendarKey");
					log.verbose("CalendarKey : " + strCalendarKey);
					break;
				}
			}
		}
		// Check if CalendarKey is blank
		if (strCalendarKey.equals(""))
		{
			Document docGetOrgDtls = XMLUtil.createDocument("Organization");
			docGetOrgDtls.getDocumentElement().setAttribute("OrganizationCode", strScac);

			Document outTempGetOrgDtls = YFCDocument.parse("<Organization OrganizationCode=\"\" BusinessCalendarKey=\"\" />").getDocument();
			env.setApiTemplate("getOrganizationHierarchy", outTempGetOrgDtls);
			Document docOutputOrgDtls = AcademyUtil.invokeAPI(env, "getOrganizationHierarchy", docGetOrgDtls);
			env.clearApiTemplates();

			// obtaining CalendarKey from Organization, if any
			log.verbose("getOrgHierarchy API output: " + XMLUtil.getXMLString(docOutputOrgDtls));
			strCalendarKey = docOutputOrgDtls.getDocumentElement().getAttribute("BusinessCalendarKey");
			log.verbose("CalendarKey : " + strCalendarKey);
		}

		// End Change for STL-244
		return strCalendarKey;
	}

	private String getInvoiceDate(YFSEnvironment env, String sCalendarKey) throws ParserConfigurationException, Exception
	{
		String invoiceDate = "";

		try
		{
			String strDateFormat = AcademyConstants.STR_SIMPLE_DATE_PATTERN;
			SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
			Calendar cal = Calendar.getInstance();
			String sCurrentDate = sDateFormat.format(cal.getTime());
			// obtaining the current Date in YYYY-MM-DD format

			int currentHr = cal.get(Calendar.HOUR_OF_DAY);
			int futureNoOfDays = 7; // set to days of the week
			cal.add(Calendar.DATE, futureNoOfDays);
			String sFutureDate = sDateFormat.format(cal.getTime());
			// setting the future date to a week after current date

			Date currentDate = sDateFormat.parse(sCurrentDate);
			Date futureDate = sDateFormat.parse(sFutureDate);

			YFCDocument docCalendarDayDtls = YFCDocument.createDocument("Calendar");
			docCalendarDayDtls.getDocumentElement().setAttribute("CalendarKey", sCalendarKey);
			docCalendarDayDtls.getDocumentElement().setDateTimeAttribute("FromDate", new YFCDate(currentDate));
			docCalendarDayDtls.getDocumentElement().setDateTimeAttribute("ToDate", new YFCDate(futureDate));

			Document docOutputCalendarDayDtls = AcademyUtil.invokeAPI(env, "getCalendarDayDetails", docCalendarDayDtls.getDocument());

			log.verbose("**** Output of getCalendarDayDetails API ****" + XMLUtil.getXMLString(docOutputCalendarDayDtls));

			NodeList nListDates = docOutputCalendarDayDtls.getElementsByTagName("Date");
			int iListDates = nListDates.getLength();

			String sShiftDate = "";
			String sShiftCurrentDate = "";
			String sShiftCurrentType = "";
			String sShiftStartTime = "";
			String sShiftEndTime = "";
			String sShiftStartHr = "";
			String sShiftEndHr = "";
			String sShiftType = "";
			Element eleDate = null;
			Element eleCurrentShift = null;

			// checking the current date details
			Element eleCurrentDate = (Element) nListDates.item(0);
			sShiftCurrentType = eleCurrentDate.getAttribute("Type");
			sShiftCurrentDate = eleCurrentDate.getAttribute("Date");
			invoiceDate = sShiftCurrentDate; // setting InvoiceDate to
			// current date by default

			if ("1".equals(sShiftCurrentType) || "2".equals(sShiftCurrentType))
			{
				NodeList nListCurrentShifts = eleCurrentDate.getElementsByTagName("Shift");
				int iListCurrentShifts = nListCurrentShifts.getLength();

				for (int p = 0; p < iListCurrentShifts; p++)
				{
					eleCurrentShift = (Element) nListCurrentShifts.item(p);
					sShiftStartTime = eleCurrentShift.getAttribute("ShiftStartTime");
					sShiftEndTime = eleCurrentShift.getAttribute("ShiftEndTime");

					sShiftStartHr = sShiftStartTime.substring(0, 2);
					sShiftEndHr = sShiftEndTime.substring(0, 2);

					// int iStartHr = Integer.parseInt(sShiftStartHr);
					int iEndHr = Integer.parseInt(sShiftEndHr);
					if (iEndHr > currentHr)
					{
						return invoiceDate;
					}
				}
			}
			for (int i = 1; i < iListDates; i++)
			{
				eleDate = (Element) nListDates.item(i);
				sShiftDate = eleDate.getAttribute("Date");
				sShiftType = eleDate.getAttribute("Type"); // for Type=1:
				// Calendar day is
				// valid
				if ("1".equals(sShiftType) || "2".equals(sShiftType))
				{
					invoiceDate = sShiftDate; // InvoiceDate set to next valid
					// calendar shift date
					return invoiceDate;
				}
			}
			log.verbose("**** InvoiceDate from getCalendarDayDetails API ****" + invoiceDate);
			return invoiceDate;
		} catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}

	private void prepareInvoiceNumber(YFSEnvironment env, Document docCurrentInvoiceSeqNo, Document docShipment) 
	{

		String strInvoiceNo = null;
		Document docGetInvoiceSeqNo = null;
		Document docApiInput = null;
		String strTransNo = null;
		String strRegNo = null;
		String strLastModTime = null;
		String ipPoDate = null;
		try {
			log
					.verbose("######## docCurrentInvoiceSeqNo in prepareInvoiceNumber ###"
							+ XMLUtil.getXMLString(docCurrentInvoiceSeqNo));
			//DSVCHANGE
			Element eleShipment = docShipment.getDocumentElement();
			if( eleShipment.hasAttribute("IPPurchaseOrderDate")){
			    ipPoDate = docShipment.getDocumentElement().getAttribute("IPPurchaseOrderDate");
			}
			//DSVCHANGE END
			strTransNo = docCurrentInvoiceSeqNo.getDocumentElement()
					.getAttribute(AcademyConstants.ATTR_CURR_TRANS_NO);
			strTransNo = prefixWithRequiredZeroes(strTransNo);

			strRegNo = docCurrentInvoiceSeqNo.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURR_REG_NO);

			strRegNo = prefixWithRequiredZeroes(strRegNo);
			String strStoreNo = prefixWithRequiredZeroes(AcademyConstants.WMS_NODE);

			strLastModTime = docCurrentInvoiceSeqNo.getDocumentElement().getAttribute(AcademyConstants.ATTR_SEQ_DATE);
			log.verbose("Invoice Date obtained" + strLastModTime);

			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_SIMPLE_DATE_PATTERN);
			String sCurrentDate = sdf.format(cal.getTime());
			Date currDateTime = sdf.parse(sCurrentDate);
			Date yfcDate = sdf.parse(strLastModTime);
			log.verbose("Current Date : " + sdf.format(currDateTime));
			log.verbose("Invoice Date : " + sdf.format(yfcDate));

			if (!yfcDate.after(currDateTime))
			{
				log.verbose("Invoice Date = Current Date");
				strLastModTime = docCurrentInvoiceSeqNo.getDocumentElement().getAttribute(AcademyConstants.ATTR_MODIFY_TS);
				log.verbose("Stamped Modify TS as Invoice Date");
				sdf.applyPattern(AcademyConstants.STR_DATE_TIME_PATTERN);
				yfcDate = sdf.parse(strLastModTime);
				log.verbose("yfcDate configured");
			}
			sdf.applyPattern(AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
			log.verbose("SeqDate used in Invoice Number: " + sdf.format(yfcDate));

			String invoiceDate = sdf.format(yfcDate);
			
			//DSVCHANGE To add TS to the InvoiceDate ie IP PurchaseOrder Date
			if(eleShipment.hasAttribute("IPPurchaseOrderDate")){
    			YTimestamp ipPoDateTimeStamp= YTimestamp.newTimestamp(ipPoDate);
    			String ipPoDateOnlyTime =  ipPoDateTimeStamp.getString("HHmmss");
    			invoiceDate = invoiceDate.substring(0, 8) + ipPoDateOnlyTime;
    			log.verbose("Invoice Date updated with IP PODate time == > " + invoiceDate);
			}
	        //DSVCHANGE END
			
			invoiceDate = invoiceDate.substring(0, invoiceDate.length() - 2)
					+ "00";
			
			//DSVCHANGE To add ShipNode instead of RegNo for DSV Orders
			String strShipmentConfirmationFlag = null;
            String strShipNode =docShipment.getDocumentElement().getAttribute("ShipNode");
			
			strShipNode = prefixWithRequiredZeroes(strShipNode);
			
            if (env.getTxnObject("ShipmentConfirmationFlag") != null){
                 strShipmentConfirmationFlag = (String) env.getTxnObject("ShipmentConfirmationFlag");
            }
            
            if(strShipmentConfirmationFlag != null && 
                    !strShipmentConfirmationFlag.equals("") &&
                        "Y".equals(strShipmentConfirmationFlag) ){
               
                strInvoiceNo = invoiceDate + strStoreNo + strShipNode + strTransNo;
                log.verbose("----------- Invoice Numberr after Validating : " + strInvoiceNo);
            }
            else{
                strInvoiceNo = invoiceDate + strStoreNo + strRegNo + strTransNo;
                log.verbose("----------- Invoice Number after Validating : " + strInvoiceNo);
            }
          //DSVCHANGE END
               
            
           log.verbose("----------- Invoice Numberrrr after Validating : " + strInvoiceNo);
			outDoc = XMLUtil.createDocument("InvoiceHeader");
			log.verbose("############# InvoiceHeader document #### " + XMLUtil.getXMLString(outDoc));
			outDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_INVOICE_NO, strInvoiceNo);
			outDoc.getDocumentElement().setAttribute("RegeneratedInvoiceNumber", "Y");
			log.verbose("############# InvoiceHeader document #### " + XMLUtil.getXMLString(outDoc));
			callChangeOrderInvoice(env, strOrderInvoiceKey, strInvoiceNo);
			log.verbose("Invoice Number : " + strInvoiceNo);

			// / code change for Tax Recalculation
			if (env.getTxnObject("InvoiceNo") == null)
			{
				env.setTxnObject("InvoiceNo", strInvoiceNo);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
	}

	private void callChangeOrderInvoice(YFSEnvironment env, String OrderInvoiceKey, String strInvoiceNo) throws Exception
	{
		log.verbose("**** changeOrderInvoice API logic call*****");
		Document docInput = XMLUtil.createDocument("OrderInvoice");
		docInput.getDocumentElement().setAttribute("OrderInvoiceKey", OrderInvoiceKey);
		Element eleExtn = XMLUtil.createElement(docInput, "Extn", null);
		docInput.getDocumentElement().appendChild(eleExtn);
		eleExtn.setAttribute("ExtnInvoiceNo", strInvoiceNo);
		env.setApiTemplate("changeOrderInvoice", "global/template/api/changeOrderInvoice.PrintFlow.xml");
		Document outdoc = AcademyUtil.invokeAPI(env, "changeOrderInvoice", docInput);
		env.clearApiTemplate("changeOrderInvoice");
		log.verbose("*********** output of change order Invoice api : " + XMLUtil.getXMLString(outdoc));

	}

	private String prefixWithRequiredZeroes(String strNo)
	{
		String mask = AcademyConstants.MASKED_NUMBER;
		String updateNo = mask.substring(0, mask.length() - strNo.length()) + strNo;
		return updateNo;
	}

	public void setProperties(Properties arg0) throws Exception
	{

	}

}
