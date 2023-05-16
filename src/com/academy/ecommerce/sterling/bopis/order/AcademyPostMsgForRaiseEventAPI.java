package com.academy.ecommerce.sterling.bopis.order;
/* Sample input to the class
 * <? xml version="1.0" encoding="UTF-8"?>
<Order DocumentType="0001" EnterpriseCode="Academy_Direct"
    OrderHeaderKey="20180528163401185108" OrderNo="Order216" SellerOrganizationCode="Academy_Direct">
    <PriceInfo ChangeInTotalAmount="-24.99" Currency="USD"
        EnterpriseCurrency="USD"
        ReportingConversionDate="2018-05-28T16:34:00-04:00"
        ReportingConversionRate="1.00" TotalAmount="24.99"/>
    <OrderLines>
        <OrderLine ChangeInOrderedQty="-1.00"
            OrderLineKey="20180528163401185110" OrderedQty="0.00"
            PrimeLineNo="1" SubLineNo="2" DeliveryMethod="PICK" >
            <Item ItemID="102118261" ProductClass="GOOD" UnitOfMeasure="EACH"/>
            <LinePriceInfo ChangeInLineTotal="-24.99" LineTotal="0.00"/>
            <StatusBreakupForCanceledQty>
                <CanceledFrom 
                    OrderLineScheduleKey="2018052816523441185141"
                    OrderReleaseKey="20180528163441185138"
                    OrderReleaseStatusKey="2018052816375216185714"
                    Quantity="1.00" Status="3350.300"
                    StatusDate="2018-05-28T16:52:15-04:00" StatusDescription="Ready To Ship">
                    <Details
                        ExpectedDeliveryDate="2018-05-28T16:34:41-04:00"
                        ExpectedShipmentDate="2018-05-28T16:34:41-04:00"
                        ShipNode="116" TagNumber=""/>
                </CanceledFrom>
            </StatusBreakupForCanceledQty>
        </OrderLine>
    </OrderLines>
</Order>
*/
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @author neeladha
 *
 */
public class AcademyPostMsgForRaiseEventAPI {
private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademyPostMsgForRaiseEventAPI.class);
	
	/** This method will post the message in queue JMS queue if its BOPIS shipment getting cancelled 
	 * after Ready for customer pickup status.
	 * @param env
	 * @param inXML
	 * @return
	 */
	public  Document postMsgForRaiseEvent (YFSEnvironment env, Document inXML) 
	   {
		log.beginTimer("AcademyPostMsgForRaiseEventAPI::postMsgForRaiseEvent-Method");
		log.verbose("Entering the method AcademyPostMsgForRaiseEventAPI.postMsgForRaiseEvent ");
		System.out.println("Input XML for AcademyPostMsgForRaiseEventAPI"+XMLUtil.getXMLString(inXML));
		try
	    	{ 
			NodeList NLOrderLine=inXML.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
	    	
	    	 for (int i = 0; i < NLOrderLine.getLength(); i++) 
			    {   
			    	Element eleOrderLine = (Element) NLOrderLine.item(i);
			    	String strdeliveryMethod= eleOrderLine.getAttribute(AcademyConstants.ATTR_DEL_METHOD);
			    	 Element eleCanceledFrom =(Element)eleOrderLine.getElementsByTagName("CanceledFrom").item(0);
			    	String strStatus = eleCanceledFrom.getAttribute(AcademyConstants.ATTR_STATUS);
							
			    	if( !YFCObject.isVoid(strdeliveryMethod) && !YFCObject.isVoid(strStatus) ) 
			    	{   
			    		// Double dStatus = Double.parseDouble(strStatus);
			    		 
			    		 if (strdeliveryMethod.equalsIgnoreCase(AcademyConstants.STR_PICK) )
			    				 
			    				//dStatus>=Double.parseDouble(AcademyConstants.STR_INCLUDE_IN_SHIPMENT_STATUS))
			    			{
			    			 Integer iPostmsg = compareStatus(strStatus, AcademyConstants.STR_INCLUDE_IN_SHIPMENT_STATUS);
								
								if((iPostmsg == 0) || (iPostmsg > 0))
								{
//									log.verbose("Calling AcademyRaiseOnCancelEventAsyncService Service with input : =" +XMLUtil.getXMLString(inXML));
						    		//System.out.println("Calling AcademyPostRaiseEventMsgForCancellation Service with input : =" +XMLUtil.getXMLString(inXML));
						    	    AcademyUtil.invokeService(env,AcademyConstants.ACADEMY_POST_RAISE_EVENT_MSG_FOR_CANCELLATION,inXML);
						    	    	break; 
								}
										    		
			    			}
			    		
			    		 
			    	}
			    }
			
	    	}
		catch (Exception e) {
			log.error(e);
			throw new YFSException("Exception in the method AcademyPostMsgForRaiseEventAPI.postMsgForRaiseEvent-Method" +e.getMessage());
		}
		log.endTimer("AcademyPostMsgForRaiseEventAPI::postMsgForRaiseEvent-Methodd");
		return inXML;
		}
	/**
	 * THis method compares statuses without using the == operator.
	 * @param status1 yfs_status code
	 * @param status2 yfs_status code
	 * @return
	 * if status1 == status2, it returns 0
	 * else if status1 > status2, it returns positive integer
	 * else if status1 < status2, it returns negative integer
	 */
	public static Integer compareStatus(String status1, String status2) {
		int comparisonNumber = YFCCommon.compareStrings(status1, status2);
		if(comparisonNumber == 0){
			return 0;
		}else if(!YFCCommon.isVoid(status1) && YFCCommon.isVoid(status2)){
			return 1;
		}else if(YFCCommon.isVoid(status1) && !YFCCommon.isVoid(status2)){
			return -1;
		}
		
		String[] splittedStatus1 = {status1};
		if(status1.indexOf('.') > 0){
			splittedStatus1 = status1.split("\\.");
		}
		
		String[] splittedStatus2 = {status2};
		if(status2.indexOf('.') > 0){
			splittedStatus2 = status2.split("\\.");
		}
		
		int minSubStatusNo = Math.min(splittedStatus1.length, splittedStatus2.length);
		int i = 0;
		for(;i < minSubStatusNo; i++){
			comparisonNumber = Integer.parseInt(splittedStatus1[i]) - Integer.parseInt(splittedStatus2[i]);
			if(comparisonNumber != 0){
				return comparisonNumber;
			}
		}
		
		if(splittedStatus1.length == splittedStatus2.length){
			return 0;
		}else if(splittedStatus1.length > splittedStatus2.length){
			return 1;
		}else if(splittedStatus1.length < splittedStatus2.length){
			return -1;
		}
			
		return null;
	}
	
	}
	


