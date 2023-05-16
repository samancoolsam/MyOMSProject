//package Declaration

package com.academy.ecommerce.sterling.userexits;

//import statements
//w3c import statements
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.ue.OMPConfirmRefundDistributionUE;

/**
 * 
 * Class to perform the Refund Order Process for Return Orders for PayPal.
 * 
 * If OrderDate>365 days, then while returning,
 *  Refund should be through GIFT CARD not through for PayPal account.
 *  
 *  So, overriding the Payment Information.
 * 
 *
 */
public class AcademyConfirmRefundDistributionUEImpl implements OMPConfirmRefundDistributionUE
{
	
	//Set the logger
	private static YFCLogCategory log = YFCLogCategory
	.instance(AcademyConfirmRefundDistributionUEImpl.class);

	
	public Document confirmRefundDistribution(YFSEnvironment env, Document inXML) {
		log.beginTimer("confirmRefundDistribution");
		log.debug("inXML for confirmRefundDistribution :: " +XMLUtil.getXMLString(inXML));
		
		Document outDoc =null;
		try {
				
				//Create the new Document with Order as root Element
				outDoc = XMLUtil.createDocument("Order");
				
				//set attribute OrderHeaderKey to root Element
				outDoc.getDocumentElement().setAttribute("OrderHeaderKey", inXML.getDocumentElement().getAttribute("OrderHeaderKey"));
				
				//create Element RefundPaymentMethods
				Element eRefundPaymentMethods = outDoc.createElement("RefundPaymentMethods");
				
				//create Element RefundPaymentMethod
				Element eRefundPaymentMethod = outDoc.createElement("RefundPaymentMethod");
				
				//set attribute PaymentType
				eRefundPaymentMethod.setAttribute("PaymentType", "GIFT_CARD");
				
				//get attribute RefundAmount from InXML
				
				// Start - Changes made for PERF-205 - Optimize PAYMENT_COLLECTION Agent
				String refundAmount = "";
				String paymentKey = "";
				NodeList refundPaymentMethodList = inXML.getElementsByTagName("RefundPaymentMethod");
				if (refundPaymentMethodList != null && refundPaymentMethodList.getLength() > 0) {
					// Refund Payment Method exist
					refundAmount = ((Element) refundPaymentMethodList.item(0)).getAttribute("RefundAmount");										
					paymentKey = ((Element) refundPaymentMethodList.item(0)).getAttribute("PaymentKey");					
				} else {					
					// Refund Payment Method does not exist. Nothing to refund because customer appeasement would have been made against the order.
					// Scenario - Customer Appeasement has been given for the order before the Return Order has been invoiced. 
					// This process has been followed to satisfy the customer.
					log.debug("Refund has already been made on the Order.");
					return inXML; 					
				}
				
				log.debug("Refund Amount: "+ refundAmount);
				log.debug("PaymentKey: "+ paymentKey);
				
				// End - Changes made for PERF-205 - Optimize PAYMENT_COLLECTION Agent
				
				//set attribute PaymentType
				eRefundPaymentMethod.setAttribute("RefundAmount", refundAmount);
				
				//get attribute BillToKey from InXML
				String billToKey = inXML.getDocumentElement().getAttribute("BillToKey");
				log.verbose("Bill to Key is" + billToKey);
				
				//set attribute BillToKey
				eRefundPaymentMethod.setAttribute("BillToKey",  billToKey);
				
				//set attribute SvcNo
				eRefundPaymentMethod.setAttribute("SvcNo", "123456789");
				
				//create Element RefundFulfillmentDetails
				Element eRefundFulfillmentDetails = outDoc.createElement("RefundFulfillmentDetails");
				
				//set attribute ItemID
				eRefundFulfillmentDetails.setAttribute("ItemID", "019588342");
				
				//set attribute UnitOfMeasure
				eRefundFulfillmentDetails.setAttribute("UnitOfMeasure", "EACH");
				
				//create Element RefundForPaymentMethods
				Element eleRefundForPaymentMethods = outDoc.createElement("RefundForPaymentMethods");
				
				//create Element RefundForPaymentMethod
				Element eleRefundForPaymentMethod = outDoc.createElement("RefundForPaymentMethod");	
				
				//set attribute PaymentKey
				eleRefundForPaymentMethod.setAttribute("PaymentKey", paymentKey);
				
				//set attribute RefundAmount
				eleRefundForPaymentMethod.setAttribute("RefundAmount", refundAmount);
				
				
				//Appending the all Elements to OutDoc
				eleRefundForPaymentMethods.appendChild(eleRefundForPaymentMethod);
				eRefundPaymentMethod.appendChild(eleRefundForPaymentMethods);	
				eRefundPaymentMethod.appendChild(eRefundFulfillmentDetails);	
				eRefundPaymentMethods.appendChild(eRefundPaymentMethod);
				
				outDoc.getDocumentElement().appendChild(eRefundPaymentMethods);
				
				log.debug("confirmRefundDistribution Out Doc in if part is " + XMLUtil.getXMLString(outDoc));
				
				
				
				
		} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return outDoc;	
		}

	
}
