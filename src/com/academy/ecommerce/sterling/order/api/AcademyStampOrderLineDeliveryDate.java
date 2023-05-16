package com.academy.ecommerce.sterling.order.api;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfc.util.YFCDateUtils;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyStampOrderLineDeliveryDate
{
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyStampOrderLineDeliveryDate.class);
	public Document stampDeliveryDate(YFSEnvironment env, Document inXML)
	{
		//Start : WN-802 Delayed Release/Pre Order
		String strItemID = null; 
		String strInventoryAvailableDate = null;
		String strCalendarFromDate = null;
		String strGetItemListTemplate = "<ItemList><Item ItemKey=''><Extn ExtnInvAvlDate=''/></Item></ItemList>";
		Date orderdate = null;
		Date inventoryAvailableDate = null;
		SimpleDateFormat sdf = null;
		Element eleItemExtn = null;
		Document docInGetItemList = null;
		Document docOutputTemplate = null;
		Document docOutGetItemList = null;
		//End : WN-802 Delayed Release/Pre Order
		
		try
		{
			String expectedDeliveryDate="";
			Element inputXML = inXML.getDocumentElement();
			log.verbose("******* INPUT - " + XMLUtil.getElementXMLString(inputXML));

			String strOrderDate = inputXML.getAttribute("OrderDate");
			log.verbose("********* shipment element : " + strOrderDate);

			NodeList orderLineList = XPathUtil.getNodeList(inputXML, "/Order/OrderLines/OrderLine");
			if (orderLineList != null && !YFCObject.isVoid(orderLineList))
			{
				// int tempDaysCount = -1;
				int numberOfNodes = orderLineList.getLength();
				log.verbose("********* number of nodes : " + numberOfNodes);

				for (int i = 0; i < numberOfNodes; i++)
				{
					Element eleOrderLine = (Element) orderLineList.item(i);					
					//Start BOPIS-1583 -SFS:: STH and SFS mixed card order failure due to delivery date calculation
					String strFulfillmentType = eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
					log.verbose("strFulfillmentType - " + strFulfillmentType);
					//OMNI-6448 : STS Order Creation similar to BOPIS
					if (!AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equals(strFulfillmentType)
							&& !AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE.equals(strFulfillmentType)
							&& !AcademyConstants.STR_SHIP_TO_STORE.equals(strFulfillmentType) 
					//Start::OMNI-5001 Skipping for EGC
							&& !AcademyConstants.STR_E_GIFT_CARD.equals(strFulfillmentType)){
					//End::OMNI-5001 Skipping for EGC
					log.verbose("Stamp ACADEMY_DELIVERY_DATE only for STH lines. Skip this logic for BOPIS/STS/SOF lines!");
					//End BOPIS-1583 -SFS:: STH and SFS mixed card order failure due to delivery date calculation
						
					Element eleOrderDates = inXML.createElement("OrderDates");
					eleOrderLine.appendChild(eleOrderDates);
					String scac = eleOrderLine.getAttribute("SCAC");
					String carrierServiceCode = eleOrderLine.getAttribute("CarrierServiceCode");
					log.verbose("scac - " + scac);
					log.verbose("ScacAndService - " + carrierServiceCode);
					
					//Start : WN-802 Delayed Release/Pre Order
					strCalendarFromDate = strOrderDate;
					strItemID = XPathUtil.getString(eleOrderLine,AcademyConstants.XPATH_ITEM_ITEMID);
					
					docInGetItemList = prepareGetItemListInput(strItemID);
					
					docOutputTemplate = YFCDocument.getDocumentFor(strGetItemListTemplate).getDocument();
					env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, docOutputTemplate);
					
					//To verify if ExtnInvAvlDate is set to a future date, so as to schedule this line on a later date by setting EarliestScheduleDate
					docOutGetItemList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST, docInGetItemList);
					
					env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
					log.verbose("getItemlist output - " + XMLUtil.getXMLString(docOutGetItemList));

					if (!YFCObject.isVoid(docOutGetItemList)){
						
						eleItemExtn = (Element) docOutGetItemList.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
						strInventoryAvailableDate = eleItemExtn.getAttribute(AcademyConstants.ATTR_EXTN_INV_AVL_DATE);
						log.verbose("ExtnInvAvlDate : " + strInventoryAvailableDate);
						
						if (!YFCObject.isVoid(strInventoryAvailableDate)){
							
							sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
							inventoryAvailableDate = sdf.parse(strInventoryAvailableDate);
							orderdate = sdf.parse(strOrderDate);
							
							//If the orderDate < YFS_ITEM.extn_inv_avl_date, then delay scheduling of such order lines
							if(orderdate.before(inventoryAvailableDate)){
								log.verbose("PRE-ORDER :: Scheduling of OrderLine, for Order# "+inputXML.getAttribute(AcademyConstants.ATTR_ORDER_NO)+" and line with Item ID "+strItemID+", will be delayed to -> "+strInventoryAvailableDate);
								strCalendarFromDate = strInventoryAvailableDate;
								eleOrderLine.setAttribute(AcademyConstants.ATTR_EARLIEST_SCHEDULE_DATE, strInventoryAvailableDate);
							}	
						}
					}
					//End : WN-802 Delayed Release/Pre Order
					
					if(!scac.equals("CEVA"))
					{
						log.verbose("SCAC is not CEVA " );
						int numberOfBusinessDays = getValueFromCommonCodes(env, carrierServiceCode);
						YFCDocument inDocDoc = YFCDocument.getDocumentFor(inXML);
						YFCElement eleRoot = inDocDoc.getDocumentElement();
						YFCDate orderDate = eleRoot.getDateTimeAttribute("OrderDate");
						YFCDate cutOffTime = new YFCDate();
						YFCDateUtils dateUtil = new YFCDateUtils();
						dateUtil.removeTimeComponent(cutOffTime);

						Document getCommonCodeListInputXML = XMLUtil.createDocument("CommonCode");
						getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeType", "CUT_OF_TIME");
						Document outXML = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML);
						log.verbose("********* outXML for getCommonCodeList of cut of time : " +XMLUtil.getXMLString(outXML));
						/*
						 * Element eleCommonCode = outXML.getDocumentElement();
						 * String strCodeShortDescription = eleCommonCode.getAttribute("CodeShortDescription");
						 * int cutoff = Integer.getInteger(strCodeShortDescription);
						 */

						NodeList commonCodeList1 = outXML.getElementsByTagName("CommonCode");
						String strCodeShortDescription = "";

						if (commonCodeList1 != null && !YFCObject.isVoid(commonCodeList1))
						{

							log.verbose("CommonCodeList for SHP_DELIVRY_DAY : " + commonCodeList1.toString());
							int iLength1 = commonCodeList1.getLength();
							for (int k = 0; k < iLength1; k++)
							{
								Element commonCode = (Element) commonCodeList1.item(k);
								String codeValue = commonCode.getAttribute("CodeValue");
								if ("CutOf".equals(codeValue))
								{
									strCodeShortDescription = commonCode.getAttribute("CodeShortDescription");
									log.verbose("number Of business days  : " + numberOfBusinessDays);
								}
							}
						}

						int cutoff = Integer.parseInt(strCodeShortDescription);
						log.verbose("**cutoff**" + cutoff);

						dateUtil.addHours(cutOffTime, cutoff);

						if (dateUtil.GTE(orderDate, cutOffTime, true))
						{
							numberOfBusinessDays = numberOfBusinessDays + 1;
						}

						log.verbose("No of Business Days : " + numberOfBusinessDays);

						if (numberOfBusinessDays > 0)
						{
							log.verbose("numberOfBusinessDays > 0");

							//Start WN-802 : If its a Pre-Order line then FromDate = YFS_ITEM.extn_inv_avl_date, else FromDate = OrderDate
							//Document getCalendarDayDetailsInDoc = setCommonAttributesForGetCalDayDtls(env,strOrderDate,scac);
							log.verbose("Calendar FROM date :: " + strCalendarFromDate);
							Document getCalendarDayDetailsInDoc = setCommonAttributesForGetCalDayDtls(env,strCalendarFromDate,scac);
							//End WN-802 : If its a Pre-Order line then FromDate = YFS_ITEM.extn_inv_avl_date, else FromDate = OrderDate
							
							if(scac.equals("FEDX"))
							{
								log.verbose("SCAC:: FEDX");
								if(carrierServiceCode.equals("Home Delivery"))
								{

									log.verbose("carrierServiceCode:: HD");
									getCalendarDayDetailsInDoc.getDocumentElement().setAttribute(AcademyConstants.CALENDAR_ID_ATTR, AcademyConstants.FDX_HD_DEL_CAL_ID);

								}
								else if (carrierServiceCode.equals("Ground") || carrierServiceCode.equals("2 Day") || carrierServiceCode.equals("Standard Overnight"))
								{
									log.verbose("carrierServiceCode:: NHD");
									getCalendarDayDetailsInDoc.getDocumentElement().setAttribute(AcademyConstants.CALENDAR_ID_ATTR, AcademyConstants.FDX_NHD_DEL_CAL_ID);

								}
							}
							else if (scac.equals("USPS"))
							{
								log.verbose("SCAC:: USPS");
								getCalendarDayDetailsInDoc.getDocumentElement().setAttribute(AcademyConstants.CALENDAR_ID_ATTR, AcademyConstants.USPS_DEL_CAL_ID);
							}
							else if (scac.equals("USPS-Letter"))
							{
								log.verbose("SCAC:: USPS Letter ");
								getCalendarDayDetailsInDoc.getDocumentElement().setAttribute(AcademyConstants.CALENDAR_ID_ATTR, AcademyConstants.USPS_LETTER_DEL_CAL_ID);
							}
							else if (scac.equals("USPS-Endicia"))
							{
								log.verbose("SCAC:: USPS Endicia");
								getCalendarDayDetailsInDoc.getDocumentElement().setAttribute(AcademyConstants.CALENDAR_ID_ATTR, AcademyConstants.USPS_ENDICIA_DEL_CAL_ID);
							}
							else if (scac.equals("SmartPost"))
							{
								log.verbose("SCAC:: Smart Post");
								getCalendarDayDetailsInDoc.getDocumentElement().setAttribute(AcademyConstants.CALENDAR_ID_ATTR, AcademyConstants.SMARTPOST_DEL_CAL_ID);
							}

							expectedDeliveryDate = calculateExpectedDeliveryDate(env,getCalendarDayDetailsInDoc,inXML,numberOfBusinessDays);
							log.verbose("stampDeliveryDate :: Expected Delivery date : " + expectedDeliveryDate);
							String deliveryDate = formatDate(expectedDeliveryDate, AcademyConstants.STR_SIMPLE_DATE_PATTERN, AcademyConstants.STR_DATE_TIME_PATTERN);
							Element eleOrderDate = inXML.createElement("OrderDate");
							log.verbose("Created the Element OrderDate");
							eleOrderDates.appendChild(eleOrderDate);
							log.verbose("Append child OrderDate");
							eleOrderDate.setAttribute("CommittedDate", deliveryDate);
							log.verbose("setting commited date");
							eleOrderDate.setAttribute("DateTypeId", "ACADEMY_DELIVERY_DATE");
							log.verbose("setting data type id");
							log.verbose("Order Line element after setting the Delivery Date :: "+XMLUtil.getElementXMLString(eleOrderLine));

						}
					}
				}
				}
			}
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		log.verbose("************ output xml : " + XMLUtil.getXMLString(inXML));

		return inXML;
	}

	// method to get number of business days from common code for give scacAndServic

	public int getValueFromCommonCodes(YFSEnvironment env, String scacAndService)
	{
		int numberOfBusinessDays = 0;
		try
		{
			// get number of business days need to deliver shipment from common code list
			Document getCommonCodeListInputXML = XMLUtil.createDocument("CommonCode");
			getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeType", "SHP_DELVRY_DAYS");
			Document outXML = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML);

			if (outXML != null)
			{
				NodeList commonCodeList = outXML.getElementsByTagName("CommonCode");

				if (commonCodeList != null && !YFCObject.isVoid(commonCodeList))
				{
					log.verbose("CommonCodeList for SHP_DELIVRY_DAY : " + commonCodeList.toString());
					int iLength = commonCodeList.getLength();
					for (int i = 0; i < iLength; i++)
					{
						Element commonCode = (Element) commonCodeList.item(i);
						String codeValue = commonCode.getAttribute("CodeValue");
						log.verbose("SCACAndService : " + scacAndService);
						log.verbose("Code value : " + codeValue);

						if (scacAndService.equals(codeValue))
						{
							numberOfBusinessDays = Integer.parseInt(commonCode.getAttribute("CodeShortDescription"));
							log.verbose("number Of business days  : " + numberOfBusinessDays);
						}
					}
				}
			}
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return numberOfBusinessDays;
	}

	// method to change the format of date
	private String formatDate(String date, String fromDatePattern, String toDatePattern)
	{
		try
		{
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fromDatePattern);
			Date parsedDate = simpleDateFormat.parse(date);
			simpleDateFormat.applyPattern(toDatePattern);
			date = simpleDateFormat.format(parsedDate);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		return date;

	}
	
	//Start : WN-802 Delayed Release/Pre Order
	/**
	 * Prepares input to GetItemList API call
	 * @param itemID
	 * @return docInGetItemList
	 * @throws ParserConfigurationException
	 */
	private Document prepareGetItemListInput(String strItemId) throws ParserConfigurationException{
		
		Document docInGetItemList = XMLUtil.createDocument(AcademyConstants.ITEM);
		docInGetItemList.getDocumentElement().setAttribute(AcademyConstants.ATTR_ITEM_ID, strItemId);
		log.verbose("getItemlist input - " + XMLUtil.getXMLString(docInGetItemList));
		
		return docInGetItemList;
	}
	//End : WN-802 Delayed Release/Pre Order	
	
	private Document setCommonAttributesForGetCalDayDtls(YFSEnvironment env, String strOrderDate, String scac) throws Exception
	{
		log.beginTimer("Begin:: setCommonAttributesForGetCalDayDtls");
		//forming the input xml for getCalendarDayDetails
		YFCDocument getCalendarDayDetailsInDoc = YFCDocument.createDocument(AcademyConstants.CALENDAR_ELEMENT);

//		// Configuraed the Calendars at Carrier level..START

//		if (scac.equals("FEDX"))
//		{

//		}
//		//END

		getCalendarDayDetailsInDoc.getDocumentElement().setAttribute(AcademyConstants.ORG_CODE_ATTR, scac);
		//START::Dates Calculation
		String strFormatedOrderDate = formatDate(strOrderDate, AcademyConstants.STR_DATE_TIME_PATTERN, AcademyConstants.STR_SIMPLE_DATE_PATTERN);

		// getting list of next 15 days
		String strDatePattern = AcademyConstants.STR_SIMPLE_DATE_PATTERN;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(strDatePattern);

		// actual shipment date
		Date currentDate = simpleDateFormat.parse(strFormatedOrderDate);
		log.verbose("currentDate (Order Date as From Date) :: "+ currentDate);

		// find next 15 days from current shipment date
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		int futureNoOfDays = 15;
		cal.add(Calendar.DATE, futureNoOfDays);
		String strFutureDate = simpleDateFormat.format(cal.getTime());
		Date futureDate = simpleDateFormat.parse(strFutureDate);
		log.verbose("futureDate (To Date) :: "+ futureDate);

		getCalendarDayDetailsInDoc.getDocumentElement().setDateTimeAttribute(AcademyConstants.FROM_DATE_ATTR, new YFCDate(currentDate));
		getCalendarDayDetailsInDoc.getDocumentElement().setDateTimeAttribute(AcademyConstants.TO_DATE_ATTR, new YFCDate(futureDate));

		log.verbose("getCalendarDayDetailsInDoc :: "+ XMLUtil.getXMLString(getCalendarDayDetailsInDoc.getDocument()));
		log.verbose("End:: setCommonAttributesForGetCalDayDtls");
		return getCalendarDayDetailsInDoc.getDocument();
		//END::Dates Calculation


	}

	private String calculateExpectedDeliveryDate(YFSEnvironment env, Document getCalendarDayDetailsInDoc, Document inXML,int numberOfBusinessDays) throws Exception
	{

		log.beginTimer("BEGIN:: calculateExpectedDeliveryDate");
		log.verbose("calculateExpectedDeliveryDate ::getCalendarDayDetailsInDoc"+ XMLUtil.getXMLString(getCalendarDayDetailsInDoc));
		log.verbose("invoking api for calendar day details");

		Document getCalendarDayDetailsOutDoc = AcademyUtil.invokeAPI(env, "getCalendarDayDetails", getCalendarDayDetailsInDoc);

		log.verbose("********** calendar details : " + XMLUtil.getXMLString(getCalendarDayDetailsOutDoc));
		Element datesElement = getCalendarDayDetailsOutDoc.getDocumentElement();
		NodeList dateNodesList = XPathUtil.getNodeList(datesElement, "/Calendar/Dates/Date");
		int tempDaysCount = -1;
		String expectedDeliveryDate = "";
		if (dateNodesList != null && !YFCObject.isVoid(dateNodesList))
		{
			log.verbose("dateNodesList is not NULL");
			int numberOfNodes1 = dateNodesList.getLength();
			log.verbose("date NodeList Length :: "+ numberOfNodes1);
			for (int j = 0; j < numberOfNodes1; j++)
			{
				Element dateElement = (Element) dateNodesList.item(j);

				NodeList shiftsNodeList = dateElement.getElementsByTagName("Shifts");
				Element shiftsElement = (Element) shiftsNodeList.item(0);
				NodeList shiftNodeList = shiftsElement.getElementsByTagName("Shift");
				String isValid = ((Element) shiftNodeList.item(0)).getAttribute("ValidForDay");

				if (isValid.equalsIgnoreCase("Y"))
				{
					tempDaysCount++;
				}

				if (tempDaysCount == numberOfBusinessDays)
				{
					expectedDeliveryDate = dateElement.getAttribute("Date");
					log.verbose("Expected Delivery date : " + expectedDeliveryDate);
//					Element eleOrderDates = inXML.createElement("OrderDates");
//					String deliveryDate = formatDate(expectedDeliveryDate, AcademyConstants.STR_SIMPLE_DATE_PATTERN, AcademyConstants.STR_DATE_TIME_PATTERN);
//					Element eleOrderDate = inXML.createElement("OrderDate");
//					eleOrderDates.appendChild(eleOrderDate);
//					eleOrderDate.setAttribute("CommittedDate", deliveryDate);
//					eleOrderDate.setAttribute("DateTypeId", "ACADEMY_DELIVERY_DATE");

					break;
				}
			}
		}
		return expectedDeliveryDate;


	}

}
