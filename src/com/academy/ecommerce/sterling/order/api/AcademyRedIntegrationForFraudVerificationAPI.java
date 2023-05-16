package com.academy.ecommerce.sterling.order.api;

/**
 * This Custom Api implemented to handle Fraud Validation for CC Orders and publishing the details (Red)Fraud Engine and resovling the 
 * Academy Fraud Order Hold and creating new AcademyAwaitFraud hold and it will resolved based on Red response code.
 * 
 *  vgummadidala
 */

import java.util.Properties;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;

// This Custom Api implemented for - fraud Verification of Orders and will send detail to Fraud Engine if its a FraudOrder.

public class AcademyRedIntegrationForFraudVerificationAPI implements YIFCustomApi {
	public void setProperties(Properties service) throws Exception {
		// TODO Auto-generated method stub
	}
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyRedIntegrationForFraudVerificationAPI.class);
	//private String outputTemp = "global/template/api/Avs_Fraud/AcademyYFSProcessOrderHoldTypeUEImpl.getOrderDetails.xml";
	private String outputTemp = "global/template/api/Avs_Fraud/AcademyYFSProcessOrderHoldTypeUEImpl.getOrderList.xml";
	
	public Document fraudVerification(YFSEnvironment env, Document inXML)
			throws Exception {
		boolean initialChecks=false,checkGiftCard=true;
		int countAuthRequests=0;
		String orderHeadekey;
		Document ordInputDoc,ordDetailOutDoc;
		Element ordrElem;
		log.beginTimer(" Begining of AcademyRedIntegrationForFraudVerificationAPI  fraudVerification()- Api");
		log
		.verbose("*************** Entering into Custom API - AcademyRedIntegrationForFraudVerificationAPI :: ************ "
				+ XMLUtil.getXMLString(inXML));
		
		orderHeadekey = inXML.getDocumentElement().getAttribute(
				AcademyConstants.ATTR_ORDER_HEADER_KEY);

		ordInputDoc = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		ordrElem = ordInputDoc.getDocumentElement();
		ordrElem.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
				orderHeadekey);
		// Modified the template format of getOrderList API as part of #4076, R026I
		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, outputTemp);
		ordDetailOutDoc = AcademyUtil.invokeAPI(env,
				AcademyConstants.API_GET_ORDER_LIST, ordInputDoc);
		
//Initial checks for the Hold Type on the Order	
		NodeList ordHoldTypesEle=ordDetailOutDoc.getElementsByTagName(AcademyConstants.ELE_ORDER_HOLD_TYPE);
		int ordHodLength=ordHoldTypesEle.getLength();
		for(int j=0;j<ordHodLength;j++)
		{ Element ordHoldTypeEle=(Element)ordHoldTypesEle.item(j);
			if(ordHoldTypeEle.getAttribute(AcademyConstants.ATTR_HOLD_TYPE).equalsIgnoreCase("AcademyFraudOrder"))
			{ 
		if(ordHoldTypeEle.getAttribute(AcademyConstants.ATTR_STATUS).equalsIgnoreCase(AcademyConstants.STR_HOLD_CREATED_STATUS))
			initialChecks=true;
			}
			
		}
		if(initialChecks)
		{
		NodeList paymentMethodsList=ordDetailOutDoc.getElementsByTagName(AcademyConstants.ELE_PAYMENT_METHOD);
		int payLength=paymentMethodsList.getLength();
		for(int i=0;i<payLength;i++)
		{
			// To get the Active Payment type from Order  
			 String suspendAnyMoreCharges = ((Element) paymentMethodsList.item(i))
			.getAttribute(AcademyConstants.ACADEMY_SUSPEND_CHARGES);
			
	if (suspendAnyMoreCharges
			.equalsIgnoreCase(AcademyConstants.STR_NO)) 
			{
		
		if(!((Element)(paymentMethodsList.item(i))).getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE).equalsIgnoreCase(AcademyConstants.STR_GIFT_CARD))
			{ checkGiftCard=false;
			
							log
							.verbose("*************** Inside the condition and setting the Flag changeOrder to True :: ************ "
									);
							//Not Publishing Details in case of createStatus even if its a Fraud Hold. as per Latest Details of CR# 54
							if(inXML.getDocumentElement().getAttribute("PaymentStatus").equalsIgnoreCase("AUTHORIZED"))
							{
								
					log
							.verbose("*************** AcademyRedIntegrationForFraudVerificationService after FraudHold Applied on Order  :: ************ ");
					// 
					if((!(inXML.getDocumentElement().getAttribute("Status").equalsIgnoreCase("Created")))){
						// Get the OrderDetails by calling getOrderList API and publish to ReD as part of #4076 R026I
						Element eleOrderDetails = (Element)ordDetailOutDoc.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);						
						AcademyUtil.invokeService(env,"AcademyPublishOrderDetailsToRedForFraudValidation",XMLUtil.getDocumentForElement(eleOrderDetails) );
					}	
					// Modified the method definision as part of #4076, R026I. Prevent changeOrder API call twice.
					resolveFraudVerificationHold(env, orderHeadekey, checkGiftCard);
					
					// Not using changeOrder API call second time. The above method will take care. 
					//As part of #4076 - Dead lock issue while Red integration, R026I
					//putAcademyFraudOrderOnHold(env, orderHeadekey);
				}
							
			}	
		}
			
	}
		
		if(checkGiftCard)
			resolveFraudVerificationHold(env,inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY),checkGiftCard);

		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
		log.verbose("*************** Exiting AcademyRedIntegrationForFraudVerificationAPI  :: ************ "+XMLUtil.getXMLString(inXML));
		log.endTimer(" End of AcademyRedIntegrationForFraudVerificationAPI  fraudVerification()- Api");
		}
			return inXML;
}

	
//	 This method implemented for Resolving the Fraud Verification Hold.
	void resolveFraudVerificationHold(YFSEnvironment env, String orderHeadekey, boolean isGCTender)
			throws Exception {
		log.beginTimer(" Begining of AcademyRedIntegrationForFraudVerificationAPI  resolveFraudVerificationHold()- Api");
		log
		.verbose("****** Inside resolveFraudVerificationHold() - to resolve AcademyFraudOrder Hold :::");
		Document ordHoldDoc = XMLUtil
				.createDocument(AcademyConstants.ELE_ORDER);
		Element orderElem, orderHoldTypesElem, orderHoldTypeElem;
		orderElem = ordHoldDoc.getDocumentElement();
		orderElem.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
				orderHeadekey);
		orderElem.setAttribute(AcademyConstants.ATTR_OVERRIDE,
				AcademyConstants.STR_YES);
		orderHoldTypesElem = ordHoldDoc
				.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPES);
		orderHoldTypeElem = ordHoldDoc
				.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPE);
		orderHoldTypeElem.setAttribute(AcademyConstants.ATTR_STATUS,
				AcademyConstants.HOLD_RESOLVE_STATUS);
		orderHoldTypeElem.setAttribute(AcademyConstants.ATTR_HOLD_TYPE,
				"AcademyFraudOrder");
		orderHoldTypesElem.appendChild((Node) orderHoldTypeElem);
		orderElem.appendChild((Node) orderHoldTypesElem);
		// As part of #4076, R026I
		if(!isGCTender){
			//Creating new Hold type on the Order so that it should get resolve based on Red/Fraud Engine response Code.
			orderHoldTypeElem = ordHoldDoc.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPE);
			orderHoldTypeElem.setAttribute(AcademyConstants.ATTR_STATUS,AcademyConstants.STR_HOLD_CREATED_STATUS);
			orderHoldTypeElem.setAttribute(AcademyConstants.ATTR_HOLD_TYPE,"ACADEMY_AWAIT_FRAUD");
			orderHoldTypesElem.appendChild((Node) orderHoldTypeElem);
			orderElem.appendChild((Node) orderHoldTypesElem);
		}
		log.verbose("****** Calling of changeOrder for Resolving the Fraud Hold and XML passing is :::"+ XMLUtil.getXMLString(ordHoldDoc));
		AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER,ordHoldDoc);
		log.endTimer(" End of AcademyRedIntegrationForFraudVerificationAPI  resolveFraudVerificationHold()- Api");
	}
//Creating new Hold type on the Order so that it should get resolve based on Red/Fraud Engine response Code.
	void putAcademyFraudOrderOnHold(YFSEnvironment env, String orderHeadekey)
			throws Exception {
		log.beginTimer(" Begin of AcademyRedIntegrationForFraudVerificationAPI  putAcademyFraudOrderOnHold()- Api");
		log
		.verbose("****** Inside putAcademyFraudOrderOnHold() - and creating new AcademyAwaitFraud HOld on Order :::");
		Document ordHoldDoc = XMLUtil
				.createDocument(AcademyConstants.ELE_ORDER);
		Element orderElem, orderHoldTypesElem, orderHoldTypeElem;
		orderElem = ordHoldDoc.getDocumentElement();
		orderElem.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
				orderHeadekey);
		orderElem.setAttribute(AcademyConstants.ATTR_OVERRIDE,
				AcademyConstants.STR_YES);
		orderHoldTypesElem = ordHoldDoc
				.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPES);
		orderHoldTypeElem = ordHoldDoc
				.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPE);
		orderHoldTypeElem.setAttribute(AcademyConstants.ATTR_STATUS,
				AcademyConstants.STR_HOLD_CREATED_STATUS);
		orderHoldTypeElem.setAttribute(AcademyConstants.ATTR_HOLD_TYPE,
				"ACADEMY_AWAIT_FRAUD");
		orderHoldTypesElem.appendChild((Node) orderHoldTypeElem);
		orderElem.appendChild((Node) orderHoldTypesElem);
		log
				.verbose("****** Calling of changeOrder for creation the Academy Fraud Order Hold and XML passing is :::"
						+ XMLUtil.getXMLString(ordHoldDoc));
		
		AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER,
				ordHoldDoc);
		log.endTimer(" End of AcademyRedIntegrationForFraudVerificationAPI  putAcademyFraudOrderOnHold()- Api");
	}

}
