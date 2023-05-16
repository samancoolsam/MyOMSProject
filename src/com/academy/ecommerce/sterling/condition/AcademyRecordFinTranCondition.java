package com.academy.ecommerce.sterling.condition;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyRecordFinTranCondition implements YCPDynamicConditionEx {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyRecordFinTranCondition.class);
	@SuppressWarnings("unchecked")
	private Map propMap = null;
 	
	@SuppressWarnings("unchecked")
	public void setProperties(Map propMap) {
		this.propMap = propMap;
	}
	
	@SuppressWarnings("unchecked")
	public boolean evaluateCondition(YFSEnvironment env, String sName,
			Map mapData, Document inDoc) {
		log.beginTimer(" Begining of AcademyRecordFinTranCondition-> evaluateCondition Api");
		try {
			String acadFinTranCond = (String)propMap.get("AcademyFinTranCondition");
			log.verbose("AcademyFinTranCondition : " + acadFinTranCond);
			
			/*
			 * If called from ON_SUCCESS event of CREAT_ORDER and if order has
			 * GC as one of the payment tender, return true
			 */
			if("CREATE_ORDER".equalsIgnoreCase(acadFinTranCond)) {
				NodeList gcPaymentList = XMLUtil.getNodeList(inDoc, 
					"/Order/PaymentMethods/PaymentMethod[@PaymentType='GIFT_CARD']");
				log.verbose("# of GCs in Order as tender :" + gcPaymentList.getLength());
				if(gcPaymentList.getLength() > 0){
					return true;
				}
			} 
			/*
			 * If called from ON_CANCEL event of CHANGE_ORDER and if amount collected
			 * through GC is greater than order total then return true
			 */
			else if ("CANCEL_ORDER".equalsIgnoreCase(acadFinTranCond)) 
			{
				String getOrderDetailsTemplate = "<OrderList> <Order OrderHeaderKey=''><PaymentMethods>" +
				"<PaymentMethod FundsAvailable='' PaymentReference1='' PaymentReference2='' PaymentType='' SvcNo='' " +
				"TotalCharged='' /></PaymentMethods></Order> </OrderList>";
				Document getOrderDetailsTemplateDoc = YFCDocument.getDocumentFor(getOrderDetailsTemplate).getDocument();
				Document getOrderDetailsInDoc = YFCDocument.createDocument("Order").getDocument();
				getOrderDetailsInDoc.getDocumentElement().setAttribute("OrderHeaderKey", inDoc.getDocumentElement().getAttribute("OrderHeaderKey"));
				env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, getOrderDetailsTemplateDoc);
				Document getOrderListOutputDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, getOrderDetailsInDoc);
				env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
				log.verbose("getOrderlist output :" + XMLUtil.getXMLString(getOrderListOutputDoc));
				
				NodeList gcPayments = XMLUtil.getNodeList(getOrderListOutputDoc, "OrderList/Order/PaymentMethods/PaymentMethod[@PaymentType='GIFT_CARD']");
				double totalGCPayments = 0.0;
				for(int i = 0; i < gcPayments.getLength(); i++ )
				{
					Element gcPaymentElement = (Element) gcPayments.item(i);
					//START : GCD-162
					log.verbose("Is GC Post Cut Over Order :: "+AcademyConstants.STR_GC_POST_CUTOVER.equals(gcPaymentElement.getAttribute(AcademyConstants.ATTR_PAYMENT_REF_2)));
					if(AcademyConstants.STR_GC_POST_CUTOVER.equals(gcPaymentElement.getAttribute(AcademyConstants.ATTR_PAYMENT_REF_2))){
						log.verbose("For Post Cut Over Orders we need not record in ACAD_FIN_TRANSACT table");
						return false;
					}
					//END : GCD-162
					
					totalGCPayments += Double.parseDouble(gcPaymentElement.getAttribute("TotalCharged"));
				}
				log.verbose("Total amount paid through GC tender :" + totalGCPayments);
				double orderTotal = Double.parseDouble(XMLUtil.getString(inDoc, "Order/PriceInfo/@TotalAmount"));
				log.verbose("Order total after Cancel :" + orderTotal);
				if(totalGCPayments > orderTotal) {
					return true;
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		log.endTimer(" End of AcademyRecordFinTranCondition-> evaluateCondition Api");
		return false;
	}
}
