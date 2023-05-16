package com.academy.ecommerce.sterling.bopis.order;
/* Sample input to the class
 * <?xml version="1.0" encoding="UTF-8"?>
<Order DocumentType="0001" EnterpriseCode="Academy_Direct"
    OrderHeaderKey="20180525145242197030" OrderNo="Order116" SellerOrganizationCode="Academy_Direct">
    <OrderLines>
        <OrderLine FulfillmentType="BULK" DeliveryMethod="PICK"
            OrderLineKey="20180525145242197031" OrderedQty="1.00"
            OriginalOrderedQty="1.00" PrimeLineNo="1" SubLineNo="2">
            <Item ItemID="102118262" ProductClass="GOOD" UnitOfMeasure="EACH"/>
            <Extn ExtnWCOrderItemIdentifier="580822239"/>
            <StatusBreakupForBackOrderedQty> 
                <BackOrderedFrom BackOrderedQuantity="1.00"
                    OrderLineScheduleKey="2018052514665242197035"
                    OrderReleaseStatusKey="2018052514655242197036"
                    Status="1100" StatusDate="2018-05-25T14:52:41-04:00" StatusDescription="Created">
                    <Details
                        ExpectedDeliveryDate="2018-05-25T14:52:41-04:00"
                        ExpectedShipmentDate="2018-05-25T14:52:41-04:00" TagNumber=""/>
                </BackOrderedFrom>
            </StatusBreakupForBackOrderedQty>
        </OrderLine>
    </OrderLines>
</Order>


 * */

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
/**
 * @author neeladha
 *
 */
public class AcademyCancelBackorderQtyForBOPIS {
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademyCancelBackorderQtyForBOPIS.class);
	//Define properties to fetch argument value from service configuration
    private Properties props;
	public void setProperties(Properties props) throws Exception {
		        this.props = props;
		    }
	/** This method will be called on ON_BACKORDER event of schedule/release transaction.It will 
	 * fetch the BOPIS lines which are in backordered status and to call change order API to cancel such lines.
	 * @param env
	 * @param inXML
	 */
	public  void  cancelBackorderQtyForBOPIS (YFSEnvironment env, Document inXML) 
	{ 
		log.beginTimer("AcademyCancelBackorderQtyForBOPIS::cancelBackorderQtyForBOPIS-Method");
		log.verbose("Entering the method AcademyCancelBackorderQtyForBOPIS.cancelBackorderQtyForBOPIS ");
		try 
		{  
		
		   Document docchangeOrderInput = null;
		   Element eleRootchangeOrderInput = null;
		   Element elechangeOrderOrderLines = null;
		   Element eleRootinXML = null;
			String strDeliveryMethod= null;
			String strOrderedQty = null;
			String strBackOrderedQuantity = null;
			String strOrderLineKey = null;
			String strPrimeLineNO = null;
			String strSubLineNo = null;
			String strNoteText = null;
			String strReasonCode=null;
			String strDocumentType=null;
			String strOrderHeaderKey= null;
			String strEnterpriseCode=null;
			String strOrderNo=null;
			Double dOrderedQty=0.00;
			Double dBackOrderedQuantity=0.00;
			
			strNoteText=props.getProperty(AcademyConstants.ATTR_NOTE_TEXT);
			strReasonCode = props.getProperty(AcademyConstants.ATTR_REASON_CODE);
			
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
				eleRootchangeOrderInput.setAttribute(AcademyConstants.ATTR_MOD_REASON_CODE,strReasonCode);
							
			elechangeOrderOrderLines = docchangeOrderInput.createElement(AcademyConstants.ELE_ORDER_LINES);
			eleRootchangeOrderInput.appendChild(elechangeOrderOrderLines);
			NodeList NLOrderLine = inXML.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
		    for (int i = 0; i < NLOrderLine.getLength(); i++) 
		    {  
		    	Element eleOrderLine = (Element) NLOrderLine.item(i);
		    	strDeliveryMethod =eleOrderLine.getAttribute(AcademyConstants.ATTR_DEL_METHOD);
		    	Element eleBackOrderedFrom =(Element)eleOrderLine.getElementsByTagName("BackOrderedFrom").item(0);
		    	
		    	 strBackOrderedQuantity=eleBackOrderedFrom.getAttribute("BackOrderedQuantity");
		    	
		    	if (!YFCObject.isVoid(strDeliveryMethod) && strDeliveryMethod.equalsIgnoreCase(AcademyConstants.STR_PICK)
		    			 && !YFCObject.isVoid(strBackOrderedQuantity))
		       	    { 
		    	    	strOrderedQty= eleOrderLine.getAttribute(
							AcademyConstants.ATTR_ORDERED_QTY);
		    	    	
		    	    	dOrderedQty = Double.parseDouble(strOrderedQty);
				     	dBackOrderedQuantity=Double.parseDouble(strBackOrderedQuantity);
				     	dOrderedQty=dOrderedQty-dBackOrderedQuantity;
				     	strOrderedQty= String.valueOf(dOrderedQty);
				     	//System.out.println("strOrderedQty after calculation"+strOrderedQty);
				     	log.verbose("strOrderedQty after calculation"+strOrderedQty);
				     	strOrderLineKey = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
									
						strPrimeLineNO= eleOrderLine.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO);
									
						strSubLineNo= eleOrderLine.getAttribute(AcademyConstants.SUB_LINE_NO);
								
						Element elechangeOrderOrderLine =docchangeOrderInput.createElement(AcademyConstants.ELE_ORDER_LINE); 
						elechangeOrderOrderLines.appendChild(elechangeOrderOrderLine);
						elechangeOrderOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,strOrderLineKey );
						elechangeOrderOrderLine.setAttribute(AcademyConstants.ATTR_PRIME_LINE_NO,strPrimeLineNO );
						elechangeOrderOrderLine.setAttribute(AcademyConstants.SUB_LINE_NO,strSubLineNo);
						elechangeOrderOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY,strOrderedQty);
						Element elechangeOrderNotes = docchangeOrderInput.createElement(AcademyConstants.ELE_NOTES);
						elechangeOrderOrderLine.appendChild(elechangeOrderNotes);
						Element elechangeOrderNote = docchangeOrderInput.createElement(AcademyConstants.ELE_NOTE);
						elechangeOrderNotes.appendChild(elechangeOrderNote);
						elechangeOrderNote.setAttribute(AcademyConstants.ATTR_OPERATION,AcademyConstants.STR_OPERATION_VAL_CREATE);
						elechangeOrderNote.setAttribute(AcademyConstants.ATTR_NOTE_TEXT,strNoteText);
						elechangeOrderNote.setAttribute(AcademyConstants.ATTR_REASON_CODE,strReasonCode);
		        	}
		    	
		    } 
		   
		    AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_ORDER, docchangeOrderInput);
    	 	log.verbose("Calling ChangeOrder API with input : =" +XMLUtil.getXMLString(docchangeOrderInput));
    	 //	System.out.println("Calling ChangeOrder API with input : =" +XMLUtil.getXMLString(docchangeOrderInput));	
			     	
			     	
			    
		}	
		
		catch (Exception e) {
			log.error(e);
			throw new YFSException("Exception in the method AcademyCancelBackorderQtyForBOPIS.cancelBackorderQtyForBOPIS" +e.getMessage());
		}
		log.endTimer("AcademyCancelBackorderQtyForBOPIS::cancelBackorderQtyForBOPIS-Method");
		
	}
	
}
