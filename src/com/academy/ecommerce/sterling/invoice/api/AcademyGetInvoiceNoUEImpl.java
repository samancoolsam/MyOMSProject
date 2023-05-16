
package com.academy.ecommerce.sterling.invoice.api;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.ycp.core.YCPContext;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfc.util.YFCLocale;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.OMPGetInvoiceNoUE;

public class AcademyGetInvoiceNoUEImpl implements OMPGetInvoiceNoUE
{

	String				strInvoiceShipDate	= null;

	String				strInvoiceShipNode	= null;

	YFCLocale			yfcLocale			= null;

	Document			docOutput			= null;

	// Set the properties variable
	private Properties	props				= new Properties();
	private String		tempDate			= null;

	public void setProperties(Properties props)
	{
		this.props = props;

		if (props != null)
		{
			tempDate = props.getProperty("TEMP_DATE");
		}
	}

	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademyGetInvoiceNoUEImpl.class);

	public Document getInvoiceNo(YFSEnvironment env, Document inDoc) throws YFSUserExitException
	{
		log.beginTimer("AcademyGetInvoiceNoUEImpl : getInvoiceNo started");
		String sInvoiceDate = null;
		String strShipmentType = null;//Start WN-2037 SOF SO Shipment Invoicing and Settlement
		
		try
		{
			yfcLocale = ((YCPContext) env).getYFCLocale();
			if (YFCObject.isVoid(yfcLocale))
			{
				log.verbose("########## yfcLocale is NULL ########");
			}

			log.verbose("########## Document input is ########" + XMLUtil.getXMLString(inDoc));

			docOutput = XMLUtil.createDocument(AcademyConstants.ELE_INVOICE_HDR);
			// code update for Tax CR: get the Proforma Invoice #

			Element eleInvoiceNoUE = inDoc.getDocumentElement();
			Element invoiceHeader = (Element) eleInvoiceNoUE.getElementsByTagName("InvoiceHeader").item(0);

			String invoiceType = YFCObject.isVoid(invoiceHeader) ? " " : invoiceHeader.getAttribute("InvoiceType");
			log.verbose("invoice Type....." + invoiceType);

			if ("PRO_FORMA".equals(invoiceType))
			{
				long invoiceNo = AcademyGetInvoiceNoUEImpl.getNextDBSeqno(env, "SEQ_YFS_INVOICE_NO");
				// invoiceNo = invoiceNo + 1;
				docOutput.getDocumentElement().setAttribute(AcademyConstants.ATTR_INVOICE_NO, Long.toString(invoiceNo));
				if (env.getTxnObject("InvoiceNo") == null)
				{
					env.setTxnObject("InvoiceNo", Long.toString(invoiceNo));
				}
				log.verbose("Invoice No for Proforma Invoice " + invoiceNo);
				return docOutput;
			}

			//Start WN-2037 SOF SO Shipment Invoicing and Settlement
			if (AcademyConstants.STR_ORDER_INVOICE_TYPE.equals(invoiceType) ){
				log.verbose("Inside SHIPMENT Invoice ");
				strShipmentType = XPathUtil.getString(inDoc.getDocumentElement(), AcademyConstants.XPATH_INVOICE_SHIPMENT_TYPE);
				
				if(AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equals(strShipmentType)){
					log.verbose("Inside SOF SHIPMENT Invoice ");
					generateInvoiceNo(env, sInvoiceDate, invoiceHeader);
					return docOutput;
				}
			}
			//END WN-2037 SOF SO Shipment Invoicing and Settlement
			
			/* CR 26 changes to generate Invoice No */
			String strDocumentType = XPathUtil.getString(inDoc.getDocumentElement(), "InvoiceHeader/Order/@DocumentType");
			String strCallInvoiceUEFromPrintFLow = XMLUtil.getString(inDoc.getDocumentElement(), "InvoiceHeader/Shipment/@CallInvoiceUEFromPrintFLow");
			//DSVCHANGE
			String strCallInvoiceUEFromDSVConfirmShipmentOnSuccess = XMLUtil.getString(inDoc.getDocumentElement(), "InvoiceHeader/Shipment/@CallInvoiceUEFromDSVConfirmShipmentOnSuccess");

			String strDelMethod = XMLUtil.getString(inDoc.getDocumentElement(), "InvoiceHeader/Shipment/@DeliveryMethod");
			if ("Y".equals(strCallInvoiceUEFromPrintFLow) || ("0003".equals(strDocumentType)) || "CREDIT_MEMO".equals(invoiceType) || "Y".equals(strCallInvoiceUEFromDSVConfirmShipmentOnSuccess))
			{
			//DSVCHANGE
				strInvoiceShipDate = XPathUtil.getString(inDoc.getDocumentElement(), AcademyConstants.XPATH_INVOICE_HDR_SHIPDATE);
				strInvoiceShipNode = XPathUtil.getString(inDoc.getDocumentElement(), AcademyConstants.XPATH_INVOICE_HDR_SHIPNODE);
				// log.verbose("##### Current Date, Time in Local Format is
				// #####"+ strCurrentDateTime);
				String strDateFormat = AcademyConstants.STR_SIMPLE_DATE_PATTERN;
				SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
				Calendar cal = Calendar.getInstance();
				sInvoiceDate = sDateFormat.format(cal.getTime());

				Element shipment = (Element) eleInvoiceNoUE.getElementsByTagName("Shipment").item(0);
				// Start change for STL-244
				// Changed for nullPointerException while choosing carrier
				// calander
				String strCalendarKey = "";
				// End change for STL-244
				String strScac = "";
				String strShipmentKey = "";
				String shipNode = "";
				
				//START WN-63 Tlogs getting created with future date InvoiceNos
				String strManifestDate = "";
				Element eleManifest = null;
				//END WN-63 Tlogs getting created with future date InvoiceNos
					
				if (!YFCObject.isVoid(shipment))
				{
					strScac = shipment.getAttribute("SCAC");
					strShipmentKey = shipment.getAttribute("ShipmentKey");
					shipNode = shipment.getAttribute("ShipNode");
				}
				log.verbose("Shipment SCAC: " + strScac);
				log.verbose("Shipment Key: " + strShipmentKey);
				if ((YFCObject.isVoid(strScac) || YFCObject.isVoid(shipNode)) && !YFCObject.isVoid(strShipmentKey))
				{
					Document docGetShipList = XMLUtil.createDocument("Shipment");
					docGetShipList.getDocumentElement().setAttribute("ShipmentKey", strShipmentKey);
					
					//START WN-63 Tlogs getting created with future date InvoiceNos
					Document outTempGetShipList = YFCDocument.parse("<Shipments> <Shipment SCAC=\"\" ShipNode=\"\" ShipmentKey=\"\" > " +
							" <Manifest ManifestDate=\"\" /> </Shipment>  " +
									"</Shipments>").getDocument();
					log.verbose("getShipmentList API Template: " + XMLUtil.getXMLString(outTempGetShipList));
					//END WN-63 Tlogs getting created with future date InvoiceNos
					
					env.setApiTemplate("getShipmentList", outTempGetShipList);
					Document docOutputGetShipList = AcademyUtil.invokeAPI(env, "getShipmentList", docGetShipList);
					env.clearApiTemplates();

					log.verbose("getShipmentList API output: " + XMLUtil.getXMLString(docOutputGetShipList));
					Element eleShipment = (Element) docOutputGetShipList.getDocumentElement().getElementsByTagName("Shipment").item(0);
					strScac = eleShipment.getAttribute("SCAC");
					shipNode = eleShipment.getAttribute("ShipNode");
					
					//START WN-63 Tlogs getting created with future date InvoiceNos
					eleManifest = (Element) docOutputGetShipList.getDocumentElement().getElementsByTagName(AcademyConstants.ELEM_MANIFEST).item(0);
					if(!YFCObject.isVoid(eleManifest))
					{
						strManifestDate = eleManifest.getAttribute(AcademyConstants.ATTR_MANIFEST_DATE);
						log.verbose("ManifestDate from getShipmentList Output :: "+strManifestDate); 
					}
					//END WN-63 Tlogs getting created with future date InvoiceNos
					
				}
				//DSVCHANGE for bypassing code which finds next working day based on SCAC Calender
				log.verbose("just before if condition for bypassing sfs code");
                
				if(YFCObject.isVoid(strCallInvoiceUEFromDSVConfirmShipmentOnSuccess)){

				if ((!YFCObject.isVoid(strScac)) && (!YFCCommon.isVoid(strDelMethod) && !strDelMethod.equals(AcademyConstants.STR_PICK)))
				{
					if (!YFCObject.isVoid(shipNode))
					{
						// SFS: getting calendar from store for the carrier if
						// available
						// Start: Process API getOrganizationHierarchy
						// Create root element Organization
						Document docGetOrgDtls = XMLUtil.createDocument("Organization");
						// Fetch attribute value for OrganizationCode
						docGetOrgDtls.getDocumentElement().setAttribute("OrganizationCode", shipNode);
						// Set the template for getOrganizationHierarchy
						Document outTempGetOrgDtls = YFCDocument.parse("<Organization OrganizationCode=\"\"><Calendars> <Calendar CalendarKey=\"\" CalendarId=\"\"/></Calendars></Organization>").getDocument();
						env.setApiTemplate("getOrganizationHierarchy", outTempGetOrgDtls);
						// Invoke Api getOrganizationHierarchy
						Document docOutputOrgDtls = AcademyUtil.invokeAPI(env, "getOrganizationHierarchy", docGetOrgDtls);
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
					}
				}

				if (!YFCObject.isVoid(strCalendarKey))
				{ 
					if (tempDate != null)
					{	
						sInvoiceDate = tempDate;
					} 
					//START WN-63 Tlogs getting created with future date InvoiceNos
					else if (!YFCObject.isVoid(strManifestDate)){
							sInvoiceDate = strManifestDate;
							log.verbose("InvoiceDate being stamped from Manifest Date :: " + strManifestDate);
						}
					//END WN-63 Tlogs getting created with future date InvoiceNos
					else
					{	
						sInvoiceDate = getInvoiceDate(env, strCalendarKey);
						// sInvoiceDate = sDateFormat.format(strInvoiceDate);
					}
				}
				
				}
				//DSVCHANGE
				
				log.verbose("sInvoiceDate....: " + sInvoiceDate);

				//Start WN-2037 SOF SO Shipment Invoicing and Settlement
				/**
				 * Start changes for #4070 As per Suresh and Sathya discussion,
				 * implementing the below logic.
				 * 
				 * 1) Enter a dummy record in the table that will permanently
				 * stay in the table. 2) Every thread will first call the
				 * select.. for update to obtain the dummy record. Each thread
				 * will have to lock this record first before they can perform
				 * the regular logic to either modify the record for the day or
				 * create new record in case of future manifest. 3) After
				 * performing the regular logic (post obtaining lock on the
				 * dummy record), the thread will timeout the lock.
				 *//*
				// Step 1 - create a new environment
				YIFApi yifApi;
				YFSEnvironment envNew;
				yifApi = YIFClientFactory.getInstance().getLocalApi();
				Document docEnv = XMLUtil.createDocument(AcademyConstants.ELE_ENV);
				docEnv.getDocumentElement().setAttribute(AcademyConstants.ATTR_USR_ID, env.getUserId());
				docEnv.getDocumentElement().setAttribute(AcademyConstants.ATTR_PROG_ID, env.getProgId());
				envNew = yifApi.createEnvironment(docEnv);
				Document docForInvoiceSeq = XMLUtil.createDocument(AcademyConstants.ELE_INVOICE_HDR);
				docForInvoiceSeq.getDocumentElement().setAttribute("InvoicedDate", sInvoiceDate);
				// Step 2 - Call the service to lock dummy record and update
				// invoice no
				Document docCurrentInvoiceSeqNo = yifApi.executeFlow(envNew, "AcademyLockRecordForInvoice", docForInvoiceSeq);

				// Step 3 - Release the environment
				yifApi.releaseEnvironment(envNew);

				//DSVCHANGE InvoiceHeader Element added as an argument
				prepareInvoiceNumber(env, docCurrentInvoiceSeqNo, invoiceHeader);*/
				generateInvoiceNo(env, sInvoiceDate, invoiceHeader);
				//END WN-2037 SOF SO Shipment Invoicing and Settlement
				//DSVCHANGE

				// End Fix for #4070
			}
			/*
			 * If GetInvoiceUE isn't invoked from prints flow, then retrieve the
			 * existing invoice no
			 */
			else
			{
				Document docInput = XMLUtil.createDocument("OrderInvoice");
				docInput.getDocumentElement().setAttribute("InvoiceType", "PRO_FORMA");
				docInput.getDocumentElement().setAttribute("ShipmentKey", XMLUtil.getString(inDoc.getDocumentElement(), "InvoiceHeader/Shipment/@ShipmentKey"));
				Document outDoc = AcademyUtil.invokeAPI(env, "getOrderInvoiceList", docInput);
				String strInvoiceNo = XMLUtil.getString(outDoc.getDocumentElement(), "OrderInvoice/Extn/@ExtnInvoiceNo");
				log.verbose("Shipment Invoice No:" + strInvoiceNo);
				
				//DSVCHANGE
				if (env.getTxnObject("VendorNetShipConfirmationMessage") != null) {
                    
				log.verbose("Inside If check of VendorNetShipConfirmationMessage txn obj for Validating InvoiceNo ::");
                    
				String strOrderInvoiceKey = XMLUtil.getString(outDoc.getDocumentElement(), "OrderInvoice/@OrderInvoiceKey");
                    
				Element shipmentEle = (Element) inDoc.getElementsByTagName("Shipment").item(0);
				Document inDocToValidateInvoiceNo = XMLUtil.getDocumentForElement(shipmentEle);
                    
				inDocToValidateInvoiceNo.getDocumentElement().setAttribute("ExtnInvoiceNo", strInvoiceNo);
				inDocToValidateInvoiceNo.getDocumentElement().setAttribute("OrderInvoiceKey", strOrderInvoiceKey);
				inDocToValidateInvoiceNo.getDocumentElement().setAttribute("IPPurchaseOrderDate", (String) env.getTxnObject("VendorNetShipConfirmationMessage"));
				log.verbose("Input To AcademyValidateInvoiceNumberProcessAPI :: \n"+XMLUtil.getXMLString(inDocToValidateInvoiceNo));
				Document OutDocAfterValidateInvoiceNo = AcademyUtil.invokeService(env,"AcademyValidateInvoiceNo", inDocToValidateInvoiceNo);
				log.verbose("Output From AcademyValidateInvoiceNumberProcessAPI :: \n"+XMLUtil.getXMLString(OutDocAfterValidateInvoiceNo));
				return OutDocAfterValidateInvoiceNo;
                    
			}
			//DSVCHANGE
				
				docOutput = XMLUtil.createDocument("InvoiceHeader");
				docOutput.getDocumentElement().setAttribute("InvoiceNo", strInvoiceNo);
				return docOutput;
			}

		} catch (Exception e)
		{
			e.printStackTrace();
			throw new YFSUserExitException(e.getMessage());
		}

		return docOutput;
	}
//Start WN-2037 SOF SO Shipment Invoicing and Settlement 
	/**Generate Invoice number for the given date
	 * @param env
	 * @param sInvoiceDate
	 * @param invoiceHeader
	 * @throws YIFClientCreationException
	 * @throws ParserConfigurationException
	 * @throws RemoteException
	 */
	private void generateInvoiceNo(YFSEnvironment env, String sInvoiceDate,
			Element invoiceHeader) throws Exception {
		/**
		 * Start changes for #4070 As per Suresh and Sathya discussion,
		 * implementing the below logic.
		 * 
		 * 1) Enter a dummy record in the table that will permanently
		 * stay in the table. 2) Every thread will first call the
		 * select.. for update to obtain the dummy record. Each thread
		 * will have to lock this record first before they can perform
		 * the regular logic to either modify the record for the day or
		 * create new record in case of future manifest. 3) After
		 * performing the regular logic (post obtaining lock on the
		 * dummy record), the thread will timeout the lock.
		 */
		// Step 1 - create a new environment
		log.verbose("Inside generateInvoiceNo()");
		YIFApi yifApi;
		YFSEnvironment envNew;
		yifApi = YIFClientFactory.getInstance().getLocalApi();
		Document docEnv = XMLUtil.createDocument(AcademyConstants.ELE_ENV);
		docEnv.getDocumentElement().setAttribute(AcademyConstants.ATTR_USR_ID, env.getUserId());
		docEnv.getDocumentElement().setAttribute(AcademyConstants.ATTR_PROG_ID, env.getProgId());
		envNew = yifApi.createEnvironment(docEnv);
		Document docForInvoiceSeq = XMLUtil.createDocument(AcademyConstants.ELE_INVOICE_HDR);
		docForInvoiceSeq.getDocumentElement().setAttribute("InvoicedDate", sInvoiceDate);
		// Step 2 - Call the service to lock dummy record and update
		// invoice no
		Document docCurrentInvoiceSeqNo = yifApi.executeFlow(envNew, "AcademyLockRecordForInvoice", docForInvoiceSeq);

		// Step 3 - Release the environment
		yifApi.releaseEnvironment(envNew);

		//DSVCHANGE InvoiceHeader Element added as an argument
		prepareInvoiceNumber(env, docCurrentInvoiceSeqNo, invoiceHeader);
		log.verbose("Exit generateInvoiceNo()");
	}
	//END WN-2037 SOF SO Shipment Invoicing and Settlement

	/*
	 * private boolean verifyForDateRollOver(String lastModifiedTime) { String
	 * currentTime = null; // String strDateFormate = yfcLocale.getDateFormat();
	 * String strDateFormate = AcademyConstants.STR_DATE_TIME_PATTERN; /* d =
	 * new YFCDate(); yfcLocale = ((YCPContext) env).getYFCLocale();
	 * strCurrentDateTime = d.getString( AcademyConstants.STR_DATE_TIME_PATTERN,
	 * yfcLocale);
	 */
	/*
	 * try { Calendar cal = Calendar.getInstance(); SimpleDateFormat sdf = new
	 * SimpleDateFormat(strDateFormate); currentTime =
	 * sdf.format(cal.getTime()); Date yfcDate = sdf.parse(lastModifiedTime);
	 * lastModifiedTime = sdf.format(yfcDate); YFCDate modDate = new
	 * YFCDate(yfcDate, true); YFCDate currDateTime = new YFCDate(cal.getTime(),
	 * true); if (currDateTime.after(modDate)) { return true; } else return
	 * false; } catch (Exception e) { e.printStackTrace(); throw new
	 * YFSException(e.getMessage()); } }
	 */

	private void prepareInvoiceNumber(YFSEnvironment env, Document docCurrentInvoiceSeqNo, Element invoiceHeader)
	{
		String strInvoiceNo = null;
		Document docGetInvoiceSeqNo = null;
		Document docApiInput = null;
		String strTransNo = null;
		String strRegNo = null;
		String strLastModTime = null;
		try
		{
			/*
			 * docApiInput = XMLUtil.createDocument("ExtnInvoiceSeqNo");
			 * docGetInvoiceSeqNo = AcademyUtil.invokeService(env,
			 * "AcademyGetExtnInvoiceSeqNoListService", docApiInput);
			 */

			log.verbose("######## docCurrentInvoiceSeqNo in prepareInvoiceNumber ###" + XMLUtil.getXMLString(docCurrentInvoiceSeqNo));
			strTransNo = docCurrentInvoiceSeqNo.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURR_TRANS_NO);
			/* Start Bug Fix 9029 */
			strTransNo = prefixWithRequiredZeroes(strTransNo);
			/* End Bug Fix 9029 */

			strRegNo = docCurrentInvoiceSeqNo.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURR_REG_NO);

			/* Start Bug Fix 11601 */
			strRegNo = prefixWithRequiredZeroes(strRegNo);
			String strStoreNo = prefixWithRequiredZeroes(AcademyConstants.WMS_NODE);
			/* End Bug Fix 11601 */

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

			// Fix for Defect #4155 - Seconds value in Invoice date should
			// always be zero
			String invoiceDate = sdf.format(yfcDate);
			invoiceDate = invoiceDate.substring(0, invoiceDate.length() - 2) + "00";
			
			//DSVCHANGE extra logic for adding ShipNode instead of RegNo to the InvoiceNo
            
			String strShipNode = prefixWithRequiredZeroes(XMLUtil.getString(invoiceHeader,"Shipment/@ShipNode"));
            
			String strCallInvoiceUEFromDSVConfirmShipmentOnSuccess = XMLUtil.getString(invoiceHeader,"Shipment/@CallInvoiceUEFromDSVConfirmShipmentOnSuccess");
			if(strCallInvoiceUEFromDSVConfirmShipmentOnSuccess != null && !strCallInvoiceUEFromDSVConfirmShipmentOnSuccess.equals("") && "Y".equals(strCallInvoiceUEFromDSVConfirmShipmentOnSuccess) ){
			strInvoiceNo = invoiceDate + strStoreNo + strShipNode + strTransNo;
			log.verbose("----------- Invoice Numberr : " + strInvoiceNo);
			}
			else{

			strInvoiceNo = invoiceDate + strStoreNo + strRegNo + strTransNo;
			log.verbose("----------- Invoice Number : " + strInvoiceNo);
			}
			//DSVCHANGE

			docOutput.getDocumentElement().setAttribute(AcademyConstants.ATTR_INVOICE_NO, strInvoiceNo);
			log.verbose("Invoice Number : " + strInvoiceNo);
			log.verbose("############# InvoiceNo document #### " + XMLUtil.getXMLString(docOutput));

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

	private String prefixWithRequiredZeroes(String strNo)
	{
		String mask = AcademyConstants.MASKED_NUMBER;
		String updateNo = mask.substring(0, mask.length() - strNo.length()) + strNo;
		return updateNo;
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

	private Document updateValuesForInvoiceSequenceAndTransaction(YFSEnvironment env, String strInvoiceDate, String sequenceKey, String strTransNo, String strRegNo, String action)
	{
		Document docCreateInvoiceSeqNoInput = null;
		Document docCreateInvoiceSeqNoOutput = null;

		/*
		 * YIFApi yifApi; YFSEnvironment envNew;
		 */

		try
		{

			/*
			 * yifApi = YIFClientFactory.getInstance().getLocalApi(); Document
			 * docEnv = XMLUtil.createDocument(AcademyConstants.ELE_ENV);
			 * docEnv.getDocumentElement().setAttribute(
			 * AcademyConstants.ATTR_USR_ID, env.getUserId());
			 * docEnv.getDocumentElement().setAttribute(
			 * AcademyConstants.ATTR_PROG_ID, env.getProgId()); envNew =
			 * yifApi.createEnvironment(docEnv);
			 */

			docCreateInvoiceSeqNoInput = XMLUtil.createDocument(AcademyConstants.DOC_EXTN_INVOICE_SEQNO);
			docCreateInvoiceSeqNoInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CURR_REG_NO, strRegNo);
			docCreateInvoiceSeqNoInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CURR_TRANS_NO, strTransNo);
			docCreateInvoiceSeqNoInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIP_DATE, this.strInvoiceShipDate);
			if (!YFCObject.isVoid(sequenceKey))
				docCreateInvoiceSeqNoInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_SEQ_KEY, sequenceKey);
			log.verbose("#############docCreateInvoiceSeqNoInput for update #### " + XMLUtil.getXMLString(docCreateInvoiceSeqNoInput));
			if ((AcademyConstants.STR_ACTION_CREATE).equals(action))
			{
				docCreateInvoiceSeqNoInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_SEQ_DATE, strInvoiceDate);
				docCreateInvoiceSeqNoOutput = AcademyUtil.invokeService(env, AcademyConstants.SERVICE_CREATEEXTN_INVOICESEQNO, docCreateInvoiceSeqNoInput);
			} else
			{
				docCreateInvoiceSeqNoOutput = AcademyUtil.invokeService(env, AcademyConstants.SERVICE_CHANGEEXTN_INVOICESEQNO, docCreateInvoiceSeqNoInput);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

		return docCreateInvoiceSeqNoOutput;
	}

	/**
	 * Method getNextDBSeqno:: This is a method which reads the
	 * seq_inv_snap_shot sequence and returns the same.
	 * 
	 * @param YFSEnvironment
	 * @param String
	 * @return long seqNo
	 */
	public static long getNextDBSeqno(YFSEnvironment yfsEnv, String dbSeqName)
	{

		// Get the Database Sequnce Number from the seq_inv_snap_shot sequence
		long seqNo = ((YCPContext) yfsEnv).getNextDBSeqNo(dbSeqName);

		// Return the Database Sequnce Number
		return seqNo;

	}
}
