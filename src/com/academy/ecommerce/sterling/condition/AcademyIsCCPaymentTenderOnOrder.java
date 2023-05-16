package com.academy.ecommerce.sterling.condition;

import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.YCPDynamicCondition;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyIsCCPaymentTenderOnOrder implements YCPDynamicCondition{
	
private Properties props;
    
    /* This Custom Condition class implemented to verify on CallCentre orders and returns true if Order Contains atlease one CC type
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyIsCCInfoEncrypted.class);
    /**
     * 
     */
    public void setProperties(Properties props) {
        this.props = props;
    }
    
	public boolean evaluateCondition(YFSEnvironment env, String sName, Map mapData, String sXMLData) 
	{
		String orderHeadekey;
		Document ordInputDoc,ordDetailOutDoc;
		Element ordrElem;
	
		try
		{
			log.beginTimer(" Begining of AcademyIsCCPaymentTenderOnOrder->evaluateCondition Api");
			log.verbose("Entering Into AcademyIsCCPaymentTenderOnOrder->evaluateCondition() ");
			YFCDocument inXML = YFCDocument.getDocumentFor(sXMLData);

			orderHeadekey = inXML.getElementsByTagName("OrderLine").item(0).getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
			log.verbose("inside the AcademyIsCCPaymentTenderOnOrder- OrderHeaderKey="+orderHeadekey);
			
			ordInputDoc = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			ordrElem = ordInputDoc.getDocumentElement();
			ordrElem.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, orderHeadekey);
			String templateStr = "<OrderList> <Order OrderHeaderKey='' OrderNo=''><PaymentMethods><PaymentMethod " +
			"SuspendAnyMoreCharges='' PaymentType=''/></PaymentMethods></Order> </OrderList>";
			Document orderListOutputTemplate = YFCDocument.getDocumentFor(templateStr).getDocument();
			env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, orderListOutputTemplate);
			ordDetailOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, ordInputDoc);
			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
			log.verbose("order list " + XMLUtil.getXMLString(ordDetailOutDoc));
			
			NodeList paymentMethodsList=ordDetailOutDoc.getElementsByTagName(AcademyConstants.ELE_PAYMENT_METHOD);
			for(int i=0;i<paymentMethodsList.getLength();i++)
			{
				// To get the Active Payment type from Order  
				 String suspendAnyMoreCharges = ((Element) paymentMethodsList.item(i)).getAttribute(AcademyConstants.ACADEMY_SUSPEND_CHARGES);
				 if (suspendAnyMoreCharges.equalsIgnoreCase(AcademyConstants.STR_NO)) 
				 {
					 if(((Element)(paymentMethodsList.item(i))).getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE).equalsIgnoreCase(AcademyConstants.CREDIT_CARD))
					 { 
						 log.verbose("returning True if Order Contains atleast one CC type");
						 return true;
					 }
				}
			}
		}
		catch(Exception e)
		{	e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		log.beginTimer(" Ending of AcademyIsCCPaymentTenderOnOrder->evaluateCondition Api");
		log.verbose("Exiting - returning False if Order Contains only GC type");
		return false;
	}
}
