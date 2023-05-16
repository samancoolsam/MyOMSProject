package com.academy.ecommerce.sterling.bopis.order;
/*
 * Sample Input to the class
 * <?xml version="1.0" encoding="UTF-8"?>
<Order DocumentType="0001" EnterpriseCode="Academy_Direct"
    OrderHeaderKey="20180530114554192747" OrderNo="Order229">
    <OrderLines>
        <OrderLine DeliveryMethod="PICK"  ShipNode="" 
            OrderLineKey="20180530114554192748" OrderedQty="10.00"
            PrimeLineNo="1" SubLineNo="1">
            <Item ItemID=""/>
            <FromOrderReleaseStatuses>
                <FromOrderReleaseStatus DatesChanged="N" MovedQty="6.00" Status="3350">
                    <ToOrderReleaseStatuses>
                        <ToOrderReleaseStatus Status="1300"/>
                    </ToOrderReleaseStatuses>
                </FromOrderReleaseStatus>
                <FromOrderReleaseStatus DatesChanged="Y" MovedQty="6.00">
                    <ToOrderReleaseStatuses>
                        <ToOrderReleaseStatus Status="1400"/>
                    </ToOrderReleaseStatuses>
                </FromOrderReleaseStatus>
            </FromOrderReleaseStatuses>
        </OrderLine>
    </OrderLines>
</Order>
*/

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
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
public class AcademyCancelRecordShortageQtyForBOPIS {
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademyCancelRecordShortageQtyForBOPIS.class);
	//Define properties to fetch argument value from service configuration
    private Properties props;
	public void setProperties(Properties props) throws Exception {
		        this.props = props;
		    }
	/**
	 * This method will move BOPIS order lines to cancelled status using change order API which are backorederd as part of the ‘Record Shortage’, 
	 * reason code as ‘Inventory Shortage’. Along with this it will also mark the item + node combination as ‘dirty’ using manageInventoryNodeControl
	 * for both SF and BPOIS.
	 * @param env
	 * @param inXML
	 */
	public  void cancelRecordShortageQtyForBOPIS (YFSEnvironment env, Document inXML) 
	{		
		log.beginTimer("AcademyCancelRecordShortageQtyForBOPISy::cancelRecordShortageQtyForBOPIS");
		log.verbose("Entering the method AcademyCancelRecordShortageQtyForBOPIS.cancelRecordShortageQtyForBOPIS ");
		try {
			String sTxnobject = (String) env.getTxnObject("SettingTxnObjForRecordShortageForBOPIS");
			if ( YFCObject.isVoid(sTxnobject))
			{
			Document docManageInventoryNodeControl = null;
			Document docchangeOrderInput = null;
			Element eleRootchangeOrderInput = null;
			Element elechangeOrderOrderLines = null;
			Element eleRootinXML = null;
			String strDeliveryMethod= null;
			String strOrderedQty = null;	
			String strMovedQty = null;
			String strFromStatus= null;
			String strToStatus = null;
			String strOrderLineKey = null;
			String strPrimeLineNO = null;
			String strSubLineNo = null;
			String InvPictureIncorrectTillDateConstant = null;
			String strItemId = null;
			String strNode = null;
			Boolean bCallchangeOrder= false;
			String strDocumentType=null;
			String strOrderHeaderKey= null;
			String strEnterpriseCode=null;
			String strOrderNo=null;
						
			InvPictureIncorrectTillDateConstant = props.getProperty(AcademyConstants.STR_INV_PICTURE_INCORRECT_TILL_DATE_CONSTANT);
			eleRootinXML= inXML.getDocumentElement();
			strDocumentType=eleRootinXML.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
			strOrderHeaderKey=eleRootinXML.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
			strEnterpriseCode=eleRootinXML.getAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE);
			strOrderNo=eleRootinXML.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			
			docchangeOrderInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			eleRootchangeOrderInput = docchangeOrderInput.getDocumentElement();
			eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_DOC_TYPE, strDocumentType);
			eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,strOrderHeaderKey);
			eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,strEnterpriseCode);
			eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_ORDER_NO,strOrderNo);
						
			elechangeOrderOrderLines = docchangeOrderInput.createElement(AcademyConstants.ELE_ORDER_LINES);
			eleRootchangeOrderInput.appendChild(elechangeOrderOrderLines);
			NodeList NLOrderLine = inXML.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
			
		    for (int i = 0; i < NLOrderLine.getLength(); i++) 
		    {   
		    	Element eleOrderLine = (Element) NLOrderLine.item(i);
		    	 strDeliveryMethod =eleOrderLine.getAttribute(AcademyConstants.ATTR_DEL_METHOD);
		    	 strOrderedQty= eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
		    	 Element eleItem = (Element) eleOrderLine.getElementsByTagName(AcademyConstants.ITEM).item(0);
		        strItemId=eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID);
		    	 strNode= eleOrderLine.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
		    	 	      	
	        	//System.out.println("strOrderedQty original"+strOrderedQty);
		    	//System.out.println("strDeliveryMethod"+strDeliveryMethod);
		    	//System.out.println("strItemId"+strItemId);
		    	//System.out.println("strNode"+strNode);
		    	
		    	log.verbose("strOrderedQty original"+strOrderedQty);
		    	log.verbose("strDeliveryMethod"+strDeliveryMethod);
		    	log.verbose("strItemId"+strItemId);
		    	log.verbose("strNode"+strNode);
		    	
		    	  NodeList NLFromOrderReleaseStatus = inXML.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_FROM_ORDER_RELEASE_STATUS);
		    	  for (int j = 0; j < NLFromOrderReleaseStatus.getLength(); j++) 
		    	    {
		    	      Element eleFromOrderReleaseStatus = (Element) NLFromOrderReleaseStatus.item(j); 
		    		  strFromStatus= eleFromOrderReleaseStatus.getAttribute(AcademyConstants.ATTR_STATUS);
		    		  Element eleToOrderReleaseStatus =(Element)eleFromOrderReleaseStatus.getElementsByTagName("ToOrderReleaseStatus").item(0);
		    		  strToStatus= eleToOrderReleaseStatus.getAttribute(AcademyConstants.ATTR_STATUS);
		    		  if (!YFCObject.isVoid(strFromStatus) && !YFCObject.isVoid(strToStatus))
		    		    {
		    			     
		    		    	 if (strToStatus.equalsIgnoreCase(AcademyConstants.STR_BACKORDER_STATUS) )
		    		    			
		    		    	 {	
		    		    		 Integer iCancellationRequired = compareStatus(strFromStatus,AcademyConstants.STR_INCLUDE_IN_SHIPMENT_STATUS);
									if((iCancellationRequired == 0) || (iCancellationRequired > 0))
									
		    		    		 {
		    		    		 
		    		    		 if ( !YFCObject.isVoid(strDeliveryMethod) &&
		    		    				 strDeliveryMethod.equalsIgnoreCase(AcademyConstants.STR_PICK)) 
		    		    		 {	
		    		    		 strMovedQty=eleFromOrderReleaseStatus.getAttribute("MovedQty");
		    		    		 Double dOrderedQty = Double.parseDouble(strOrderedQty);
		    		    		 Double  dMovedQty = Double.parseDouble(strMovedQty);
		    			    	 dOrderedQty=dOrderedQty-dMovedQty;
		    			    	 strOrderedQty= String.valueOf(dOrderedQty);
		    			    	 strOrderLineKey = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
		 						strPrimeLineNO= eleOrderLine.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO);
		 						strSubLineNo= eleOrderLine.getAttribute(AcademyConstants.SUB_LINE_NO);
								//System.out.println("strOrderedQty after calculation"+strOrderedQty);
								//System.out.println("strMovedQty"+strMovedQty);
								//System.out.println("strOrderLineKey"+strOrderLineKey);
									
								log.verbose("strOrderedQty after calculation"+strOrderedQty);
								log.verbose("strMovedQty"+strMovedQty);
								log.verbose("strOrderLineKey"+strOrderLineKey);
								
								Element elechangeOrderOrderLine =docchangeOrderInput.createElement(AcademyConstants.ELE_ORDER_LINE); 
								elechangeOrderOrderLines.appendChild(elechangeOrderOrderLine);
								elechangeOrderOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,strOrderLineKey );
								elechangeOrderOrderLine.setAttribute(AcademyConstants.ATTR_PRIME_LINE_NO,strPrimeLineNO );
								elechangeOrderOrderLine.setAttribute(AcademyConstants.SUB_LINE_NO,strSubLineNo);
								elechangeOrderOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY,strOrderedQty);
								bCallchangeOrder= true;
								}
		    		    		
								docManageInventoryNodeControl=	manageInventoryNodeControlInput(InvPictureIncorrectTillDateConstant,strItemId,strNode);
							    log.verbose("callmanageInventoryNodeControl Input"+ XMLUtil.getXMLString(docManageInventoryNodeControl));
							 //   System.out.println("callmanageInventoryNodeControl Input"+ XMLUtil.getXMLString(docManageInventoryNodeControl));
							    AcademyUtil.invokeAPI(env,AcademyConstants.API_MANAGE_INVENTORY_NODE_CONTROL,docManageInventoryNodeControl);
		    		    	 }
		    		    	 }
		    		    } 
		    	     }		
		    	}
		    	
		     if (bCallchangeOrder)	
		     {
		    	env.setTxnObject("SettingTxnObjForRecordShortageForBOPIS","Y");
		    AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_ORDER, docchangeOrderInput);
    	 	log.verbose("Calling ChangeOrder API with input : =" +XMLUtil.getXMLString(docchangeOrderInput));
    	 //	System.out.println("Calling ChangeOrder API with input : =" +XMLUtil.getXMLString(docchangeOrderInput));	
		     } 
		     }  
	         }
		    	      		
		catch (Exception e) {
			log.error(e);
			throw new YFSException("Exception in the method AcademyCancelRecordShortageQtyForBOPIS.cancelRecordShortageQtyForBOPIS" +e.getMessage());
		}
		
		log.endTimer("AcademyCancelRecordShortageQtyForBOPISy::cancelRecordShortageQtyForBOPIS");
		
	}
      
	
	  /** This method prepares the input doc for manageInventoryNodeControl API 
	 * @param strInvPictureIncorrectTillDateConstant
	 * @param strItemId
	 * @param strNode
	 * @return
	 * @throws ParserConfigurationException
	 */
	
	public Document manageInventoryNodeControlInput (String strInvPictureIncorrectTillDateConstant,String strItemId,String strNode) throws ParserConfigurationException
	  {     log.beginTimer("AcademyCancelRecordShortageQtyForBOPISy::manageInventoryNodeControlInput");
	         /*Sample Input xml for manageInventoryNodeControl API
	          * <InventoryNodeControl InvPictureIncorrectTillDate="2018-05-28" 
		        ItemID="102118261" Node="116" NodeControlType="ON_HOLD" OrganizationCode="Academy_Direct" ProductClass="GOOD" StartDate="" UnitOfMeasure="EACH"/>
             */  	
	  Document docmanageInventoryNodeControl = XMLUtil.createDocument(AcademyConstants.INVENTORY_NODE_CONTROL);
		  	Element eleRootmanageInventoryNodeControl = docmanageInventoryNodeControl.getDocumentElement();
		  	Integer iInvPictureIncorrectTillDate = Integer.parseInt(strInvPictureIncorrectTillDateConstant);
		  	DateFormat dateFormat = new SimpleDateFormat(AcademyConstants.STR_SIMPLE_DATE_PATTERN);
		  	Calendar cal = Calendar.getInstance();
		  	cal.add(Calendar.DATE, iInvPictureIncorrectTillDate);
		    String InvPictureIncorrectTillDate = dateFormat.format(cal.getTime());
		   //System.out.println("SysDateDate+30="+InvPictureIncorrectTillDate);
		   	log.verbose("SysDateDate+30="+InvPictureIncorrectTillDate);
		   	
		  	eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_INV_PIC_INCORRECT_TILL_DATE,InvPictureIncorrectTillDate);
		  	eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_PROD_CLASS, AcademyConstants.PRODUCT_CLASS);
		  	eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_ITEM_ID, strItemId);
		  	eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_NODE,strNode);
		  	eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_NODE_CONTROL_TYPE,AcademyConstants.STR_ON_HOLD);
		  	eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ORGANIZATION_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		  	eleRootmanageInventoryNodeControl.setAttribute(AcademyConstants.ATTR_UOM,AcademyConstants.UNIT_OF_MEASURE);
		  //	System.out.println("Input doc for manageInventoryNodeControl API "+XMLUtil.getXMLString(docmanageInventoryNodeControl));
		  	log.endTimer("AcademyCancelRecordShortageQtyForBOPISy::manageInventoryNodeControlInput");
		  	return docmanageInventoryNodeControl ;
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


