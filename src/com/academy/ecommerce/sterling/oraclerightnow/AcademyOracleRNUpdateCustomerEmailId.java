package com.academy.ecommerce.sterling.oraclerightnow;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyOracleRNUpdateCustomerEmailId {
	/**
	 * @author kgopal
	 * 
	 * Customer gives Order No to fetch return/exchange order details
	 * 
	 * Service Name: AcademyOracleRNUpdateCustomerEmailId
	 * 
	 * AcademyUpdatePersonInfoToOracle
	 * 
	 * Input XML:
	 * 
	 * 	 <Input FlowName="AcademyOracleRNUpdateCustomerEmailId"> 
	        <Login LoginID="admin" Password="password" /> 
	        <Order OldCustomerEMailID="" NewCustomerEMailID="" />
		</Input> 
	 * 	
	 * 		
	 */
	
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyOracleRNGetOrderDetails.class);
	private Properties props;
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}
	
	public Document oracleRNUpdateCustomerEmailId(YFSEnvironment env,Document inDoc) throws Exception{
		if (log.isVerboseEnabled()) {
		log.verbose("Input to method reqOracleCCGetReturnOrderDetails is: "+XMLUtil.getXMLString(inDoc));
		}
		Document docLoginOutput = null;
		Document docGetOrderListInput= null;
		Document docGetOrderListOutput = null;
		Document docChangeOrderInput = null;
		Document docChangeOrderOutput = null;
		
		Element eleOrderList = null;
		Element eleOrd = null;
		Element elePersonInfoBillTo = null;
		Element eleOrd1 = null;
		Element elePersonInfoBillTo1 = null;
		//String strOrderOnHold = "";
		
		// Getting the login element and validating the login credentials
		Element rootElement = inDoc.getDocumentElement();
		Element eleLogin = (Element)rootElement.getElementsByTagName("Login").item(0);
		String strLoginID = eleLogin.getAttribute("LoginID");
		String strPassword = eleLogin.getAttribute("Password");
		Document docResponse = AcademyUtil.validateLoginCredentials(env,strLoginID, strPassword);				
		if (YFCObject.isVoid(docResponse)) {
			docLoginOutput = XMLUtil.createDocument("Error");
			docLoginOutput.getDocumentElement().setAttribute("ErrorCode",
					AcademyConstants.LOGIN_ERROR_CODE);
			docLoginOutput.getDocumentElement().setAttribute(
					"ErrorDescription", AcademyConstants.LOGIN_ERROR_DESC);
			if (log.isVerboseEnabled()) {
			log.verbose("Error Output is :"+XMLUtil.getXMLString(docLoginOutput));
			}
			return docLoginOutput;
		} else 
		{
			if (log.isVerboseEnabled()) {
				log.verbose("User is authenticated successfully. Going to call getOrderDetails() API");
		}
			// Getting the Order date range from current date to last 30 days
			YFCDate yfcDate = new YFCDate(new Date());
			String strDateFormat = "yyyy-MM-dd";
			SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
			Calendar cal = Calendar.getInstance();
			int days = Integer.valueOf(props.getProperty("NO_OF_DAYS")).intValue();
			cal.add(Calendar.DATE, (-1 * days));
			
			String strToRange = sDateFormat.format(yfcDate) + "T23:59:59";
			String strFromRange = sDateFormat.format(cal.getTime())+ "T00:00:00";
			Element eleOrder = (Element)rootElement.getElementsByTagName("Order").item(0);
			String strOldCustomerEMailID = eleOrder.getAttribute("OldCustomerEMailID");
			String strNewCustomerEMailID = eleOrder.getAttribute("NewCustomerEMailID");
			
			
			docGetOrderListInput = XMLUtil.createDocument("Order");
			docGetOrderListInput.getDocumentElement().setAttribute("CustomerEMailID",strOldCustomerEMailID);
			docGetOrderListInput.getDocumentElement().setAttribute("DocumentType",AcademyConstants.SALES_DOCUMENT_TYPE);
			docGetOrderListInput.getDocumentElement().setAttribute("EnterpriseCode",AcademyConstants.ENTERPRISE_CODE_SHIPMENT);
			docGetOrderListInput.getDocumentElement().setAttribute(AcademyConstants.ORDER_DATE_FROM_RANGE,strFromRange);
			docGetOrderListInput.getDocumentElement().setAttribute( AcademyConstants.ORDER_DATE_TO_RANGE,strToRange);
			docGetOrderListInput.getDocumentElement().setAttribute("OrderDateQryType", AcademyConstants.ORDER_DATE_RANGE);
			docGetOrderListInput.getDocumentElement().setAttribute("DraftOrderFlag", AcademyConstants.STR_NO);
			docGetOrderListInput.getDocumentElement().setAttribute("ReadFromHistory", AcademyConstants.STR_NO);
			//Status is the order status and it should be lesser than Included in Shipment (3350)
			docGetOrderListInput.getDocumentElement().setAttribute("Status", props.getProperty("STATUS"));
			docGetOrderListInput.getDocumentElement().setAttribute("StatusQryType", AcademyConstants.VAL_STATUS_QRY);
			if (log.isVerboseEnabled()) {
				log.verbose("The input to getOrderList Is: "+XMLUtil.getXMLString(docGetOrderListInput));
			}
			
			docGetOrderListOutput = AcademyUtil.invokeService(env, "AcademyGetorderListForCustomerEmailId", docGetOrderListInput);
			if (log.isVerboseEnabled()) {
				log.verbose("output of getOrderList API is :"+XMLUtil.getXMLString(docGetOrderListOutput));
			}
		
			eleOrderList = docGetOrderListOutput.getDocumentElement();
			String strHold = props.getProperty("HOLD_TYPE");
			String[] strHoldCheck = strHold.split(",");
			if ((!YFCObject.isVoid(eleOrderList))&& (eleOrderList.hasChildNodes())){
				NodeList nlOrder = XMLUtil.getNodeList(eleOrderList,AcademyConstants.ELE_ORDER);				
				docChangeOrderInput = XMLUtil.createDocument(AcademyConstants.ELE_MULTIAPI);
				for (int i=0; i < nlOrder.getLength(); i++){
					
					Element eleOrderHoldTypes = null;
					Element eleOrderHoldType = null;
					double holdStatus = 0.00;
					Element eleAPI = null;
					Element eleInput = null;
					Element eleTemplate = null;
					Element eleTempOrder = null;
					
					String strName = AcademyConstants.API_CHANGE_ORDER;
					
					eleOrd = (Element) nlOrder.item(i);
					eleOrderHoldTypes = (Element)eleOrd.getElementsByTagName(AcademyConstants.ELE_ORDER_HOLD_TYPES).item(0);
					boolean doNotAddOrderToMultiApi = false;
					if ((!YFCObject.isVoid(eleOrderHoldTypes))&& (eleOrderHoldTypes.hasChildNodes())){
						NodeList nOrderHoldType = XMLUtil.getNodeList(eleOrderHoldTypes,AcademyConstants.ELE_ORDER_HOLD_TYPE);
					for (int j=0; j < nOrderHoldType.getLength(); j++){					
						eleOrderHoldType = (Element)eleOrderHoldTypes.getElementsByTagName(AcademyConstants.ELE_ORDER_HOLD_TYPE).item(j);
						String strHoldType = eleOrderHoldType.getAttribute(AcademyConstants.ATTR_HOLD_TYPE);
						String strHoldStatus = eleOrderHoldType.getAttribute(AcademyConstants.STATUS);
						holdStatus = Double.parseDouble(strHoldStatus);
						boolean holdFound = false;
						for (int k = 0; k < strHoldCheck.length; k++) {
							if (strHoldType.equalsIgnoreCase(strHoldCheck[k])) {
								holdFound = true;
								break;
							}
						}
						if (holdFound) {
							if (holdStatus < 1300) {
								doNotAddOrderToMultiApi = true;
								break;
							}
						}											
					}
					if (!doNotAddOrderToMultiApi) {
						log.verbose("no fraud chk hold type");
						eleOrd.removeChild(eleOrderHoldTypes);
						elePersonInfoBillTo = (Element) eleOrd.getElementsByTagName("PersonInfoBillTo").item(0);
						elePersonInfoBillTo.setAttribute("EMailID", strNewCustomerEMailID);															
						eleAPI = docChangeOrderInput.createElement("API");
						eleAPI.setAttribute("Name", strName);
						eleInput = docChangeOrderInput.createElement("Input");
						eleAPI.appendChild(eleInput);
						eleOrd1 = docChangeOrderInput.createElement("Order");	
						elePersonInfoBillTo1 = docChangeOrderInput.createElement("PersonInfoBillTo");
						XMLUtil.copyElement(docChangeOrderInput, eleOrd, eleOrd1);
						XMLUtil.copyElement(docChangeOrderInput, elePersonInfoBillTo, elePersonInfoBillTo1);
						eleInput.appendChild(eleOrd1);
						eleTemplate = docChangeOrderInput.createElement("Template");
						eleTempOrder = docChangeOrderInput.createElement("Order");
						eleTempOrder.setAttribute("OrderNo", "");
						eleTempOrder.setAttribute("CustomerEMailID", "");
						eleTemplate.appendChild(eleTempOrder);
						eleAPI.appendChild(eleTemplate);
						docChangeOrderInput.getDocumentElement().appendChild(eleAPI);
					}
						
				}
				}
				if (log.isVerboseEnabled()) {
					log.verbose("input to multi API is  :"+XMLUtil.getXMLString(docChangeOrderInput));
				}
				
				docChangeOrderOutput = AcademyUtil.invokeService(env, "AcademyCallMultiAiForCustomerEmailId", docChangeOrderInput);
				if (log.isVerboseEnabled()) {
					log.verbose("output to multi API is  :"+XMLUtil.getXMLString(docChangeOrderOutput));
				}
				
			}
			return docChangeOrderOutput;
		}
		
	}


}
