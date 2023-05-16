//package declaration
package com.academy.ecommerce.sterling.userexits;

//import statements

//java util import statements

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;

//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//academy import statements
import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
//yantra import statements
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfc.util.YFCDateUtils;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSBeforeCreateOrderUE;
import com.yantra.yfc.util.YFCCommon;

/** Description: Class AcademySFSBeforeCreateOrderUE stamps the Fulfillment Type
 *              for each orderline in the order depending on whether the item 
 *              is a bulk or non bulk item
 * @throws YFSUserExitException
 */
public class AcademySFSBeforeCreateOrderUE implements YFSBeforeCreateOrderUE 
{

    private static final YFCLogCategory log = YFCLogCategory
    .instance(AcademySFSBeforeCreateOrderUE.class);

    //It holds the properties configured in AcademySFSBeforeCreateOrderService Service
	private static Properties props;

	public void setProperties(Properties props) {
		this.props = props;
	}
	
    public String beforeCreateOrder(YFSEnvironment env, String inDoc) throws YFSUserExitException 
    {
        try 
        {
            return XMLUtil.getXMLString(beforeCreateOrder(env, XMLUtil.getDocument(inDoc)));
        } 
        catch (Exception e) 
        {
            throw( new YFSUserExitException(e.getMessage()));
        }
    }

    /**
     * Checks if there are any orderlines and processes the orderlines 
     * @param env Yantra Environment Context.
     * @param inDoc Input Document.
     * @return inDoc
     */ 
    public Document beforeCreateOrder(YFSEnvironment env, Document inDoc) throws YFSUserExitException 
    {
        try 
        {
            log.verbose("inDoc"+XMLUtil.getXMLString(inDoc));
            //declaring the variables
            String strCodeType = AcademyConstants.ITEM_FULFILLMENT_TYPE;
            String strOrganizationCode = AcademyConstants.PRIMARY_ENTERPRISE;
            String strDocType = inDoc.getDocumentElement().getAttribute("DocumentType");
            

            //declaring hashmaps
            HashMap<String, String> hmCommonCodeList = null;
            HashMap<String, String> hmItemFT = null;

            //Stamping the sourcingClassification at the order header level
            Element eleOrder = inDoc.getDocumentElement();
            eleOrder.setAttribute("SourcingClassification", AcademyConstants.SOURCING_CLASSIFICATION);

            //getting the list of item elements
            NodeList nlItem = XMLUtil.getNodeList(inDoc.getDocumentElement(),"OrderLines/OrderLine/Item");

            if(nlItem.getLength() > 0)
            {
                //calling method getCommonCodeList to get the hashmap[Item, FT]
                hmCommonCodeList = AcademyCommonCode.getCommonCodeListAsHashMap(env, strCodeType, strOrganizationCode);

                //calling method GetItemList to form the complex query input to getItemList API and then get the output of the API
                Document getItemListOutputDoc = AcademySFSBeforeChangeOrderUE.getItemList(env,nlItem);          

                //calling method compareHashMap to compare the hashmaps
                
                //Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
                //hmItemFT = AcademySFSBeforeChangeOrderUE.compareHashMap(getItemListOutputDoc, hmCommonCodeList);
                hmItemFT = AcademySFSBeforeChangeOrderUE.compareHashMap(env, getItemListOutputDoc, hmCommonCodeList);
                //End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
                
                //calling method stampFulfillmentType to stamp the fulfillment type
                AcademySFSBeforeChangeOrderUE.stampFulfillmentType(hmItemFT,nlItem);
            }
            
            // Start Change as part of STL - 245 
            
            NodeList nlOrderLines = XMLUtil.getNodeList(inDoc.getDocumentElement(),AcademyConstants.XPATH_ORDERLINE);
            DecimalFormat twoDForm = new DecimalFormat("#.000");
            
            /* OMNI-47442 -STS 2.0 order allocation - prevent from ship node -START */
            
            for(int i = 0; i < nlOrderLines.getLength(); i++)
            {
                Element currentOrderLineElm = (Element) nlOrderLines.item(i);
                String qty= currentOrderLineElm.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
                
                String strFulfillmentType= currentOrderLineElm.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
                String strShipNode = currentOrderLineElm.getAttribute(AcademyConstants.SHIP_NODE);
                String strProcureFromNode = currentOrderLineElm.getAttribute("ProcureFromNode");
                if(strFulfillmentType.equals(AcademyConstants.STR_SHIP_TO_STORE) && YFCObject.isVoid(strProcureFromNode)) {
                	currentOrderLineElm.setAttribute("IsProcurementAllowed", "Y");
                	Element eleOrderLineSourcingCntrl = (Element) currentOrderLineElm.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE_SOURCING_CNTRL).item(0);
                	eleOrderLineSourcingCntrl.setAttribute("Node", strShipNode);
                	eleOrderLineSourcingCntrl.setAttribute("SuppressSourcing", "N");
                	eleOrderLineSourcingCntrl.setAttribute("InventoryCheckCode", "NOINV");
            }          
             /* OMNI-47442 -STS 2.0 order allocation - prevent from ship node -END */
               
                //STS2.0-POC
                
                NodeList nlLineCharge= currentOrderLineElm.getElementsByTagName(AcademyConstants.ELE_LINE_CHARGE);
                for(int j = 0; j < nlLineCharge.getLength(); j++)
                {
                    Element currentLineChargeElm = (Element) nlLineCharge.item(j);
                    //Start fix STL-397 : negative chargeperline issue
                    Double newCPU = Math.floor((Double.valueOf(currentLineChargeElm.getAttribute(AcademyConstants.ATTR_CHARGES_PER_LINE)))/Double.valueOf(qty)*100)/100 ;
                    log.verbose("NEW CPU:::::" + newCPU);
                    //End fix STL-397 : negative chargeperline issue
                    
                    
                    Double newMod=(Double.valueOf(currentLineChargeElm.getAttribute(AcademyConstants.ATTR_CHARGES_PER_LINE)) )-(newCPU * Double.valueOf(qty));
                    log.verbose("newCPU :: " + newCPU);
                    log.verbose("newMod :: " + newMod);
                    currentLineChargeElm.setAttribute(AcademyConstants.ATTR_CHARGES_PER_LINE, String.valueOf(newMod));
                    currentLineChargeElm.setAttribute(AcademyConstants.ATTR_CHARGES_PER_UNIT, String.valueOf(newCPU));
                    currentLineChargeElm.setAttribute(AcademyConstants.ATTR_REFERENCE, AcademyConstants.STR_YES);
                }
            }
           
            // Start : OMNI-14990 and OMNI-14991: Logging Orders created with ClearText and
            // cleanup
            List lPLCCPaymentMethod = XMLUtil.getElementListByXpath(inDoc,
            		"/Order/PaymentMethods/PaymentMethod[@PaymentReference3='PLCC']");
            if (lPLCCPaymentMethod.size() > 0) {
            	String strOrderNo = eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO);
            	log.info("PLCC ClearText Order Created. Manually tokenize the  Order : " + strOrderNo);
            }
            // End : OMNI-14990 and OMNI-14991: Logging Orders created with ClearText and
            // cleanup            
            
            //Start OMNI 45551 STS 2.0 Rate carrier look up, Stamping ACADEMY_DELIVERY_DATE and initial promise date to TO order line 
            if("0006".equalsIgnoreCase(strDocType) ) {
            	Element orderLineElement1 = (Element) nlOrderLines.item(0);
            	String strShipNode1 = orderLineElement1.getAttribute(AcademyConstants.SHIP_NODE);
            	log.verbose("Orderline Shipnode :"+strShipNode1);
            	Boolean nodeTypeIsStore = AcademyUtil.checkNodeTypeIsStore(env, strShipNode1);
            	//proceeding if ship node is Store
            	if(nodeTypeIsStore == true) {
            		log.verbose("Node Type Is Store ");
            		Document getOrderListTemplate = null;
            		String strExtnInitialPromiseDate= null;
            		String strGetOrderListTemplate = "<OrderList><Order OrderDate='' ><OrderLines><OrderLine OrderLineKey='' ><Extn ExtnInitialPromiseDate='' /> </OrderLine></OrderLines></Order></OrderList>";
            		String strOrderHeaderKey = orderLineElement1.getAttribute(AcademyConstants.ATTR_CHAINED_FROM_ORDER_HEADER_KEY);
            		
            		//Get sales order date and initial promise date from order line
            		getOrderListTemplate = YFCDocument.getDocumentFor(strGetOrderListTemplate).getDocument();
            		Document getOrderListsInputXML = XMLUtil.createDocument("Order");
            		getOrderListsInputXML.getDocumentElement().setAttribute("OrderHeaderKey", strOrderHeaderKey );
            		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, getOrderListTemplate);
            		Document getOrderListOutputXML = AcademyUtil.invokeAPI(env, "getOrderList", getOrderListsInputXML);
            		env.clearApiTemplates(); 
            		log.verbose("getOrderListOutputXML output:" +XMLUtil.getXMLString(getOrderListOutputXML) );
            		String strOrderDate = XPathUtil.getString(getOrderListOutputXML.getDocumentElement(),"//OrderList/Order/@OrderDate");
            		log.verbose("Sales order Date :: " +strOrderDate);
            		

            		
            		for(int i = 0; i < nlOrderLines.getLength(); i++)
            		{	
            			log.verbose("Inside for loop" );
            			YFCDocument yfcOrderdoc = YFCDocument.getDocumentFor(getOrderListOutputXML);
                		YFCElement eleGetOrderListRoot = yfcOrderdoc.getDocumentElement();
                		YFCElement strOrderele = eleGetOrderListRoot.getChildElement("Order");
                		YFCDate salesOrderDate = strOrderele.getDateTimeAttribute("OrderDate");
            			Element orderLineElement = (Element) nlOrderLines.item(i);
            			Element eleOrderDates = inDoc.createElement("OrderDates");
            			orderLineElement.appendChild(eleOrderDates);
            			String expectedDeliveryDate="";
            			String carrierServiceCode =AcademyConstants.FEDX_GROUND; //Assigning scac as  FEDEX and service as Ground for sts 2.0 orders
            			String scac = AcademyConstants.SCAC_FEDX;
            			String strCalendarFromDate =null;
            			YFCDate cutOffTime = new YFCDate();
            			
            			/* OMNI-46031 START */
            			Element elePersonInfoShipTo = XmlUtils.getChildElement(orderLineElement,
            					AcademyConstants.ELEM_PERSON_INFO_SHIP_TO);
            			log.verbose("Stamping Company as SHIP_TO_STORE for STS 2.0 Shipments");
            			elePersonInfoShipTo.setAttribute(AcademyConstants.ATTR_COMPANY, AcademyConstants.STR_STS_COMPANY);

            			/* OMNI-46031 END */
          
            			String strOrderLineKey = orderLineElement.getAttribute(AcademyConstants.ATTR_CHAINED_FROM_ORDER_LINE_KEY);
            			
            			NodeList nlsalesOrderLines = XMLUtil.getNodeList(getOrderListOutputXML.getDocumentElement(),"//OrderList/Order/OrderLines/OrderLine[@OrderLineKey='" + strOrderLineKey + "']/Extn");
            			log.verbose("Length of sales order lines" +nlsalesOrderLines.getLength());
            			for (int m = 0 ; m < nlsalesOrderLines.getLength() ; m++) {
            				Element eleExtnElement = (Element) nlsalesOrderLines.item(0);
            				strExtnInitialPromiseDate = eleExtnElement.getAttribute("ExtnInitialPromiseDate");
            				log.verbose("strExtnInitialPromiseDate" +strExtnInitialPromiseDate);
            				
            			}
            			
            			//get number of business days
            			log.verbose("calling getValueFromCommonCodes to get business dates " );
            			int numberOfBusinessDays = getValueFromCommonCodes(env, carrierServiceCode);
            			log.verbose("num of business days " +numberOfBusinessDays);

            			
            			//To set AcademyDelivery Date

            			//Get cut of time from common code
            			YFCDateUtils dateUtil = new YFCDateUtils();
            			dateUtil.removeTimeComponent(cutOffTime);
            			Document getCommonCodeListInputXML = XMLUtil.createDocument("CommonCode");
            			getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeType", "CUT_OF_TIME");
            			Document outXML = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML);
            			log.verbose("********* outXML for getCommonCodeList of cut of time : " +XMLUtil.getXMLString(outXML));
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
            			if (dateUtil.GTE(salesOrderDate, cutOffTime, true))
            			{
            				numberOfBusinessDays = numberOfBusinessDays + 1;
            			}
            			
            			strCalendarFromDate = strOrderDate;
            			if (numberOfBusinessDays > 0)
            			{
            				log.verbose("numberOfBusinessDays > 0");
            				log.verbose("Calendar FROM date :: " + strCalendarFromDate);
            				Document getCalendarDayDetailsInDoc = setCommonAttributesForGetCalDayDtls(env,strCalendarFromDate,scac);
            				//calender id for  Ground = NHD
            				log.verbose("calender if for ground:: NHD");
            				getCalendarDayDetailsInDoc.getDocumentElement().setAttribute(AcademyConstants.CALENDAR_ID_ATTR, AcademyConstants.FDX_NHD_DEL_CAL_ID);
            				//sending i/p to calculateExpectedDeliveryDate()
            				expectedDeliveryDate = calculateExpectedDeliveryDate(env,getCalendarDayDetailsInDoc,numberOfBusinessDays);
            				log.verbose("stampDeliveryDate :: Expected Delivery date : " + expectedDeliveryDate);
            				String deliveryDate = formatDate(expectedDeliveryDate, AcademyConstants.STR_SIMPLE_DATE_PATTERN, AcademyConstants.STR_DATE_TIME_PATTERN);
            				Element eleOrderDate = inDoc.createElement("OrderDate");
            				log.verbose("Created the Element OrderDate");
            				eleOrderDates.appendChild(eleOrderDate);
            				log.verbose("Append child OrderDate");
            				eleOrderDate.setAttribute("CommittedDate", deliveryDate);
            				eleOrderDate.setAttribute("DateTypeId", "ACADEMY_DELIVERY_DATE");

            				if (!YFCObject.isVoid(strExtnInitialPromiseDate) ) {
            					log.verbose("setting inital promise date"  );
            					Element eleExtn = inDoc.createElement("Extn");
            					orderLineElement.appendChild(eleExtn);
            					eleExtn.setAttribute("ExtnInitialPromiseDate", strExtnInitialPromiseDate);
            					log.verbose("Order Line element after setting the Initial Promise Date :: "+XMLUtil.getElementXMLString(orderLineElement));

            				}
            			}
            		}

            	}
            }

           
            log.verbose("outDOC"+XMLUtil.getXMLString(inDoc));
            // End  Change as part of STL - 245 
        } 
        catch (Exception e) 
        {
            if(e instanceof YFSUserExitException) 
            {
                throw (YFSUserExitException)e;              
            }

            throw( new YFSUserExitException(e.getMessage()));
        }

        //printing output of UE
        if(log.isVerboseEnabled())
        {
            log.verbose("Output of UE:" + XMLUtil.getXMLString(inDoc));
        }

        return inDoc;
    }
    
    //This method gives number of business days required for a scacandservice from common code
    public int getValueFromCommonCodes(YFSEnvironment env, String scacAndService)
	{
		int numberOfBusinessDays = 0;
		try
		{
			
			log.verbose("inside getValueFromCommonCodes"); 
			// get number of business days need to deliver shipment from common code list
			Document getCommonCodeListInputXML = XMLUtil.createDocument("CommonCode");
			getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeType", "SHP_DELVRY_DAYS");
			Document outXML = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML);
			log.verbose("Before if outXML not null");
			if (outXML != null)
			{	
				log.verbose("inside if outXML not nulls");
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
    
    
    private Document setCommonAttributesForGetCalDayDtls(YFSEnvironment env, String strOrderDate, String scac) throws Exception
	{
		log.beginTimer("Begin:: setCommonAttributesForGetCalDayDtls");
		//forming the input xml for getCalendarDayDetails
		YFCDocument getCalendarDayDetailsInDoc = YFCDocument.createDocument(AcademyConstants.CALENDAR_ELEMENT);

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
    
    private String calculateExpectedDeliveryDate(YFSEnvironment env, Document getCalendarDayDetailsInDoc,int numberOfBusinessDays) throws Exception
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
					break;
				}
			}
		}
		return expectedDeliveryDate;


	}
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
   
}

