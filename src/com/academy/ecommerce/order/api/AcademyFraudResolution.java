package com.academy.ecommerce.order.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.yantra.yfc.log.YFCLogCategory;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyFraudResolution {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyFraudResolution.class);

	/**
	 * This method check for the fraud order 
	 * 
	 * @param env
	 * @param inXML
	 * @return
	 * @throws Exception
	 */
	public Document checkFraudOrder(YFSEnvironment env,Document inXML) throws Exception {
		log.verbose("Begin of AcademyFraudCode.checkFraudOrder()  method...");
		
		Document docgetOrderListOutput = getOrderList(env,inXML);
		log.verbose(" Input XML :: "+XMLUtil.getXMLString(docgetOrderListOutput));
		NodeList nlOrder = docgetOrderListOutput.getElementsByTagName(AcademyConstants.ELE_ORDER);

		//Checks if order exits or not 
		if(nlOrder.getLength()>0) {
			NodeList nlHoldTypes = XPathUtil.getNodeList(docgetOrderListOutput.getDocumentElement(), 
			"/OrderList/Order/OrderHoldTypes/OrderHoldType[@HoldType='ACADEMY_FRAUD_REVIEW' and @Status='1100']");

			//checks if the order is fraud or not if it is fraud it calls ChangeOrder API.
			if(nlHoldTypes.getLength() > 0) {
				log.verbose("HoldType=ACADEMY_FRAUD_REVIEW and Status=1100 it call ChangeOrder API");
				AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_ORDER,inXML);                                  
			}
			else {
				log.verbose("Order does not have a valid open hold. Ignoring message");
			}
		} 
		//if order is not found, throw Exception
		else {
			log.verbose("Order does not exist throws exception");
			YFSException excep = null; 
			excep = new YFSException();
			excep.setErrorCode(AcademyConstants.ERR_CODE_11);
			excep.setErrorDescription(AcademyConstants.ERR_ORDER_NOT_FOUND);
		}
		return inXML;
	}


	/**
	 * This method gets required parameter from the input xml
	 * @param env
	 * @param inputXML - getting the selected parameters from inputXML 
	 * @return Document - docgetOrderListOutput output
	 * @throws Exception
	 */


	public Document getOrderList(YFSEnvironment env, Document inputXML) throws Exception {
		
		log.verbose("Begin of AcademyFraudCode.getOrderList() method");
		log.verbose("Input of  inputXML : "+ XMLUtil.getXMLString(inputXML));

		String strOrderNo= ((Element) inputXML.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0)).getAttribute(AcademyConstants.ATTR_ORDER_NO);
		Document docgetOrderListInput = XMLUtil.getDocument("<Order OrderNo=\""+strOrderNo+"\" />");
		Document docgetOrderListOutput = null;
		// get the attributes from the inputXML
		Document docgetOrderListTemplate = XMLUtil.getDocument("<OrderList>" +
				"<Order OrderHeaderKey=\"\" OrderNo=\"\"><OrderHoldTypes>" +
				"<OrderHoldType HoldType=\"\" Status=\"\"></OrderHoldType>" +
				"</OrderHoldTypes></Order></OrderList>"); 

		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, docgetOrderListTemplate);
		docgetOrderListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, docgetOrderListInput);
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
		
		log.verbose("output of  getOrderListOutput XML  : " + XMLUtil.getXMLString(docgetOrderListOutput) );
		return docgetOrderListOutput;
	}


}
