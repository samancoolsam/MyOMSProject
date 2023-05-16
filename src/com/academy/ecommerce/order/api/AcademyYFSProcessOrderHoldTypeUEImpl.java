package com.academy.ecommerce.order.api;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSProcessOrderHoldTypeUE;

public class AcademyYFSProcessOrderHoldTypeUEImpl implements
		YFSProcessOrderHoldTypeUE {
	private String outputTemp = "global/template/api/Avs_Fraud/AcademyYFSProcessOrderHoldTypeUEImpl.getOrderDetails.xml";

	// This is userExit implemented for Fraud Verification & has following
	// business logic - Resolve the hold for the orders for which payment type
	// is not a credit card(Debit Card) based on RISK_SCORE value. if its not
	// with in the Configured range then put the Order remains on Hold

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyYFSProcessOrderHoldTypeUEImpl.class);

	public Document processOrderHoldType(YFSEnvironment env, Document inXML)
			throws YFSUserExitException {
		Document ordInputDoc, ordDetailOutDoc, commonCodeDoc, commonCodeOutDoc;
		Element ordrElem,codeElem, codeOutEle, firstCommonCodeEle, lastCommonChildEle, paymentMethodEle = null,creditTransEle=null;
		Date sysDate,currentSysDate,authDate;
		String orderHeadekey, paymentType = null, risk_score = null, DATE_FORMAT =AcademyConstants.STR_SIMPLE_DATE_PATTERN,currentDay,authrizationExpDate;
		SimpleDateFormat dateFormat;
		Calendar calndr = Calendar.getInstance(); 
		
		int salesOrderRiskScore, minRiskScore, maxRiskScore;
		env.setApiTemplate(AcademyConstants.API_GET_ORDER_DETAILS, outputTemp);
		// TODO Auto-generated method stub
		try {
			log
					.verbose("****** Entering into AcademyYFSProcessOrderHoldTypeUEImpl -> processOrderHoldType() and input XML is:::"
							+ XMLUtil.getXMLString(inXML));

			// get the Payment Type of the Order
			orderHeadekey = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);

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

			NodeList paymentList = ordDetailOutDoc.getElementsByTagName(AcademyConstants.ELE_PAYMENT_METHOD);
			for (int k = 0; k < paymentList.getLength(); k++) 
			{
				String suspendAnyMoreCharges = ((Element) paymentList.item(k)).getAttribute(AcademyConstants.ACADEMY_SUSPEND_CHARGES);
				if (suspendAnyMoreCharges.equalsIgnoreCase(AcademyConstants.STR_NO)) 
				{
					paymentMethodEle = (Element) paymentList.item(k);
					paymentType = paymentMethodEle.getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE);
				}
			}

			
			// if payment Type is not Credit or Debit card then resolve the
			// Fraud Verification Hold
			if (!(paymentType.equalsIgnoreCase(AcademyConstants.CREDIT_CARD) || (paymentType
					.equalsIgnoreCase(AcademyConstants.DEBIT_CARD)))) {
				resolveFraudVerificationHold(env, orderHeadekey);
			} else {
				/*sysDate =calndr.getTime(); 
				dateFormat = new java.text.SimpleDateFormat(DATE_FORMAT);
				  currentDay = dateFormat.format(sysDate);
				  currentSysDate = dateFormat.parse(currentDay);
				authrizationExpDate=((Element) paymentMethodEle
						.getElementsByTagName(AcademyConstants.ELE_CHARGE_TRANS)
						.item(0)).getAttribute(AcademyConstants.ATTR_AUTH_EXP_DATE);
						 authDate = dateFormat.parse(authrizationExpDate);*/
						 log
							.verbose("****** Output of paymentMethodElement  is:::"
									+ XMLUtil.getElementXMLString(paymentMethodEle));
				//if (currentSysDate.before(authDate)||currentSysDate.equals(authDate)) {
					
					 creditTransEle= (Element)paymentMethodEle
							.getElementsByTagName(AcademyConstants.ELE_CREDIT_TRANS)
							.item(0);
					 if(creditTransEle!=null)
						{
						 risk_score= (creditTransEle.getAttribute("AuthAvs"));
						}
					 
					if (creditTransEle==null || risk_score == null || risk_score.equalsIgnoreCase("")) {
						salesOrderRiskScore = 0;
					} else {
						
						salesOrderRiskScore = Integer.parseInt(risk_score);
					}
					// Calling getCommonCodeList to get the RiskScore
					commonCodeDoc = XMLUtil
							.createDocument(AcademyConstants.ELE_COMMON_CODE);
					codeElem = commonCodeDoc.getDocumentElement();
					codeElem.setAttribute(AcademyConstants.ATTR_CODE_TYPE,
							AcademyConstants.RISK_SCORE);
					codeElem.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
							AcademyConstants.SALES_DOCUMENT_TYPE);
					codeElem.setAttribute(AcademyConstants.ORGANIZATION_CODE,
							AcademyConstants.PRIMARY_ENTERPRISE);
					commonCodeOutDoc = AcademyUtil.invokeAPI(env,
							AcademyConstants.API_GET_COMMONCODE_LIST,
							commonCodeDoc);
					log
							.verbose("****** Output of getCommonCodeList Api() is:::"
									+ XMLUtil.getXMLString(commonCodeOutDoc));
					codeOutEle = commonCodeOutDoc.getDocumentElement();
					NodeList codeList = codeOutEle
							.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
					firstCommonCodeEle = (Element) codeList.item(0);
					lastCommonChildEle = (Element) codeList.item(codeList
							.getLength() - 1);
					minRiskScore = Integer
							.parseInt(firstCommonCodeEle
									.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE));
					maxRiskScore = Integer
							.parseInt(lastCommonChildEle
									.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE));

					// if RISK SCORE Falls with in the configured range then
					// resolve
					// the hold,otherwise put the order remains on Hold
					if ((salesOrderRiskScore >= minRiskScore && salesOrderRiskScore <= maxRiskScore)) {
						log
								.verbose("****** Resolving Fraud Hold as Risk Score falls with in the Range :::");
						resolveFraudVerificationHold(env, orderHeadekey);
					} else {

						Document inputFraudDoc = XMLUtil
								.createDocument(AcademyConstants.ELE_ORDER);
						Element inOrderEle = inputFraudDoc.getDocumentElement();
						inOrderEle.setAttribute(
								AcademyConstants.ATTR_ORDER_HEADER_KEY,
								orderHeadekey);
						inOrderEle.setAttribute(
								AcademyConstants.ATTR_ENTERPRISE_CODE,
								AcademyConstants.PRIMARY_ENTERPRISE);
						inOrderEle.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
								AcademyConstants.SALES_DOCUMENT_TYPE);
						inOrderEle.setAttribute(AcademyConstants.ATTR_TRANS_ID,
								AcademyConstants.YCD_FRAUD_CHECK_TRANS);
						// resolve the Fraud Check hold and create Academy Fraud
						// Order on hold and raise an Alert.
						resolveFraudVerificationHold(env, orderHeadekey);
						putAcademyFraudOrderOnHold(env, orderHeadekey);
						// Raising an Alert for Academy Fraudlent Orders
						AcademyUtil
								.invokeService(
										env,
										AcademyConstants.ACADEMY_FRAUDLENT_ORDER_SERVICE,
										inputFraudDoc);

					}

				//}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSUserExitException();
		}
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_DETAILS);
		log
				.verbose("****** Exiting  of AcademyYFSProcessOrderHoldTypeUEImpl :::");
		return inXML;

	}

	// This method implemented for Resolving the Fraud Verification Hold.
	void resolveFraudVerificationHold(YFSEnvironment env, String orderHeadekey)
			throws Exception {

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
				AcademyConstants.YCD_FRAUD_CHECK);
		orderHoldTypesElem.appendChild((Node) orderHoldTypeElem);
		orderElem.appendChild((Node) orderHoldTypesElem);
		log
				.verbose("****** Calling of changeOrder for Resolving the Fraud Hold and XML passing is :::"
						+ XMLUtil.getXMLString(ordHoldDoc));
		AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER,
				ordHoldDoc);
	}

	void putAcademyFraudOrderOnHold(YFSEnvironment env, String orderHeadekey)
			throws Exception {
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
				AcademyConstants.ACADEMY_FRAUD_ORDER);
		orderHoldTypesElem.appendChild((Node) orderHoldTypeElem);
		orderElem.appendChild((Node) orderHoldTypesElem);
		log
				.verbose("****** Calling of changeOrder for creation the Academy Fraud Order Hold and XML passing is :::"
						+ XMLUtil.getXMLString(ordHoldDoc));

		AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER,
				ordHoldDoc);
	}

	
	public static void main(String[] args)
	{
		Document doc = YFCDocument.getDocumentFor(new File("C://input.xml")).getDocument();

		NodeList paymentList = doc.getElementsByTagName(AcademyConstants.ELE_PAYMENT_METHOD);
		for (int k = 0; k < paymentList.getLength(); k++) 
		{
			String suspendAnyMoreCharges = ((Element) paymentList.item(k)).getAttribute(AcademyConstants.ACADEMY_SUSPEND_CHARGES);
			if (suspendAnyMoreCharges.equalsIgnoreCase(AcademyConstants.STR_NO)) 
			{
				Element paymentMethodEle = (Element) paymentList.item(k);
				log.verbose(paymentMethodEle.getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE));
			}
		}

	}
}
