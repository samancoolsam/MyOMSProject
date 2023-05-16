//package Declaration

package com.academy.ecommerce.sterling.condition;

//import statements
//java util import statements
import java.util.Map;

//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//academy import statements
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;

//yantra import statements
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.date.YTimestamp;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * 
 * Class to check the dynamic conditions like
 * 	PaymentType is 'PayPal' and OrderDate is 365 days OLD.
 *
 */
public class AcademyDynamicConditionForPayPalRefundOrder implements YCPDynamicConditionEx
{

	//Set the logger
	private static YFCLogCategory log = YFCLogCategory
	.instance(AcademyDynamicConditionForPayPalRefundOrder.class);

	public boolean evaluateCondition(YFSEnvironment env, String arg1, Map arg2, Document inXML) {

		log.debug("evaluateCondition method in AcademyDynamicConditionForPayPalRefundOrder");

		log.debug("Input MXL coming to evaluateCondition" + XMLUtil.getXMLString(inXML));
		
		// Fetch NodeList of Node PaymentMethod from the input xml
		NodeList paymentMethodList = (NodeList) inXML.getElementsByTagName("PaymentMethod");
		
		//If payment Method NodeList is there
		if(paymentMethodList.getLength()!= 0)
		{
			log.debug("paymentMethod Element is present");
			// Fetch the element PaymentMethod
			Element elePaymentMethod = (Element) paymentMethodList.item(0);
			
			// Fetch the value of PaymentType
			String paymentType = elePaymentMethod.getAttribute("PaymentType");
			log.debug("Payment type is ::"+ paymentType);
			
			// Fetch the value of OrderDate
			String orderDate = inXML.getDocumentElement().getAttribute("OrderDate");
			log.debug("OrderDate From inXML"+orderDate);
			
			//Getting the Current Date through YTimestamp
			YTimestamp currentDate  = YTimestamp.newMutableTimestamp();
			log.debug("Current Date :: "+currentDate);
			
			//converting the OrderDate to Sterling Format
			YTimestamp orderDateNew =YTimestamp.newTimestamp(orderDate);

			//Finding the Different days between "CurrentDate" and "Orderdate"
			int diffdays = orderDateNew.diffDays(currentDate);
			log.debug("Difference days Between two dates " + diffdays);
			
			//if PaymentType is PayPal and Diffrent days are >365 then returning TRUE
			//KER-11461 consider new PaymentType Paypal
			if(diffdays>365 && (paymentType .equals("PayPal") || AcademyConstants.STR_PAYMENT_TYPE_PAYPAL_NEW.equals(paymentType))){
				log.debug("returning TRUE");
				return true;
			}
			//otherwise returning FALSE
			else{
				log.debug("returning FALSE");
				return false;
			}

		}
		log.debug("returning FALSE");
		return false;
	}

	public void setProperties(Map arg0) {
		
	}

}
