package com.academy.ecommerce.server;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;


/**
 * @author <a href="mailto:Netai.dey@academy.com">Netai Dey</a>, Created on
 *         05/25/2015 for STL-1339. This agent will change the suspended card to active for all orders which are stuck in "Await payment info" status 
 *         and shipments are already Shipped. This is done to allow system to do settlement and publish the TLog message.
 */
public class AcademyActivateSuspendedPaymentAgent extends YCPBaseAgent {

	private final Logger logger = Logger.getLogger(AcademyActivateSuspendedPaymentAgent.class.getName());
	private final String STR_TEMPLATEFILE_GET_CHARGE_TRANSACTION_LIST = "global/template/api/AcadGetChargeTransactionList.xml";
	
	@Override
	public List<Document> getJobs(YFSEnvironment env, Document inXML) throws Exception {
		logger.verbose("Inside AcademyActivateSuspendedPaymentAgent getJobs.The Input xml is : \n" + XMLUtil.getXMLString(inXML));
		List<Document> outputList = new ArrayList<Document>();
		ArrayList<String> alPayment = new ArrayList<String>();
		Document docGetChargeTransactionListOutput = null;		
		Element elePaymentMethod = null;	
		String strPaymentKey = null;
		String strSuspendAnyMoreCharges = null;
		
		String strChargeType = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_CHARGE_TYPE);
		String strNoOfDays = inXML.getDocumentElement().getAttribute(AcademyConstants.STR_No_Of_DAYS);		
		
		//get list of order which are stuck in "Await payment info" status and shipments are already Shipped.		
		docGetChargeTransactionListOutput = getChargeTransactionList(env, strNoOfDays,strChargeType);
		
		if(!YFSObject.isVoid(docGetChargeTransactionListOutput)){
			// Fetch the Payment element
			NodeList nlPaymentMethod = docGetChargeTransactionListOutput.getElementsByTagName(AcademyConstants.ELE_PAYMENT_METHOD);
			// Iterate through the aElementsList. nlIntegrationError will be null if common code(REP_ERR_CODE) is not configured
			for (int iEleCount = 0; (iEleCount < nlPaymentMethod.getLength()); iEleCount++) {
				// Return the document for the fetched Payment element
				elePaymentMethod = (Element) nlPaymentMethod.item(iEleCount);
				strPaymentKey = elePaymentMethod.getAttribute(AcademyConstants.ATTR_PAYMENT_KEY);
				strSuspendAnyMoreCharges = elePaymentMethod.getAttribute(AcademyConstants.ACADEMY_SUSPEND_CHARGES);
				// Add the response document into the array list
				if((!alPayment.contains(strPaymentKey)) && (AcademyConstants.STR_YES.equals(strSuspendAnyMoreCharges))){
					outputList.add(XMLUtil.getDocumentForElement(elePaymentMethod));
					alPayment.add(strPaymentKey);
				}
			}
		}
		logger.verbose("Exiting AcademyActivateSuspendedPaymentAgent : getJobs ");
		return outputList;
	}

	
	

	/**
	 * @param docGetCommonCodeListOutput
	 * @return
	 * @throws Exception
	 */
	private Document getChargeTransactionList(YFSEnvironment env, String strNoOfDays, String strChargeType) throws Exception {
		logger.verbose("Entering into AcademyActivateSuspendedPaymentAgent : getOrderList() ");
		logger.verbose("strNoOfDays:\t"+strNoOfDays);
		
		Document docGetOrderListOutput = null;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_SIMPLE_DATE_PATTERN);//"yyyy-MM-dd"
		SimpleDateFormat sdf1 = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		String strToRange = sdf1.format(cal.getTime());		
		cal.add(Calendar.DATE, (-Integer.parseInt(strNoOfDays)));
		String strFromRange = sdf.format(cal.getTime());
				
		/*<ChargeTransactionDetail ChargeType="SHIPMENT" CreatetsQryType="BETWEEN"
    		FromCreatets="2015-05-29" ToCreatets="2015-05-06T05:52:03">
    		<Order PaymentStatus="AWAIT_PAY_INFO">
        		<PaymentMethods>
           	 		<PaymentMethod SuspendAnyMoreCharges="Y"/>
        		</PaymentMethods>
        		<OrderInvoiceList>
            		<OrderInvoice InvoiceType="SHIPMENT" Status="00"/>
        		</OrderInvoiceList>
    		</Order>
		</ChargeTransactionDetail>
		 */
	
		Document docgetChargeTransactionListInput = XMLUtil.createDocument(AcademyConstants.ELE_CHARGE_TRANS);
		Element elegetChargeTransactionListInput = docgetChargeTransactionListInput.getDocumentElement();
		elegetChargeTransactionListInput.setAttribute(AcademyConstants.ATTR_CHARGE_TYPE, AcademyConstants.STR_ORDER_INVOICE_TYPE);	
		elegetChargeTransactionListInput.setAttribute(AcademyConstants.ATTR_FROM_CREATETS, strFromRange);
		elegetChargeTransactionListInput.setAttribute(AcademyConstants.ATTR_TO_CREATETS, strToRange);
		elegetChargeTransactionListInput.setAttribute(AcademyConstants.ATTR_CREATETS_QRY_TYPE, AcademyConstants.BETWEEN);	
		Element eleOrder = docgetChargeTransactionListInput.createElement(AcademyConstants.ELE_ORDER);
		eleOrder.setAttribute(AcademyConstants.ATTR_PAYMENT_STATUS, AcademyConstants.STR_AWAIT_PAY_INFO);
		elegetChargeTransactionListInput.appendChild(eleOrder);
		
		Element elePaymentMethods = docgetChargeTransactionListInput.createElement(AcademyConstants.ELE_PAYMENT_METHODS);
		Element elePaymentMethod = docgetChargeTransactionListInput.createElement(AcademyConstants.ELE_PAYMENT_METHOD);
		elePaymentMethod.setAttribute(AcademyConstants.ACADEMY_SUSPEND_CHARGES, AcademyConstants.STR_YES);
		elePaymentMethods.appendChild(elePaymentMethod);
		eleOrder.appendChild(elePaymentMethods);
		
		Element eleOrderInvoiceList = docgetChargeTransactionListInput.createElement(AcademyConstants.ELE_ORDER_INVOICE_LIST);
		Element eleOrderInvoice = docgetChargeTransactionListInput.createElement(AcademyConstants.ELE_ORDER_INVOICE);
		eleOrderInvoice.setAttribute(AcademyConstants.ATTR_INVOICE_TYPE, AcademyConstants.STR_ORDER_INVOICE_TYPE);
		eleOrderInvoice.setAttribute(AcademyConstants.ATTR_STATUS, AcademyConstants.STR_ORDER_INVOICE_STATUS);
		eleOrderInvoiceList.appendChild(eleOrderInvoice);
		eleOrder.appendChild(eleOrderInvoiceList);
		
		env.setApiTemplate(AcademyConstants.API_GET_CHARGE_TRANSACTION_LIST,STR_TEMPLATEFILE_GET_CHARGE_TRANSACTION_LIST);
		logger.verbose("Input of getChargeTransactionList : \n" + XMLUtil.getXMLString(docgetChargeTransactionListInput));
		docGetOrderListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_CHARGE_TRANSACTION_LIST, docgetChargeTransactionListInput);
		logger.verbose("Output of getChargeTransactionList : \n" + XMLUtil.getXMLString(docGetOrderListOutput));
		logger.verbose("Exiting AcademyActivateSuspendedPaymentAgent : getOrderList() ");
		return docGetOrderListOutput;
	}

	/*Invoke changeOrder to activate the suspended card
	 <Order OrderHeaderKey="${ls_OHK}" Override="Y">
    	<PaymentMethods>
        	<PaymentMethod PaymentKey="${ls_PaymentKey}" SuspendAnyMoreCharges="N" />
    	</PaymentMethods>
	</Order>
	 * */
	@Override
	public void executeJob(YFSEnvironment env, Document docPaymentMethodInput) throws Exception{
		logger.verbose("Entering into AcademyActivateSuspendedPaymentAgent executeJob with input xml : " + XMLUtil.getXMLString(docPaymentMethodInput));
		/*sample docPaymentMethodInput xml : 
		<PaymentMethod OrderHeaderKey="201506020516482402063959" PaymentKey="201506020529164802063961" 
		PaymentType="CREDIT_CARD" SuspendAnyMoreCharges="Y"/>*/
		Element elePaymentMethodInput = docPaymentMethodInput.getDocumentElement();
		Document docGetChangeOrderInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		Element eleGetChangeOrderInput = docGetChangeOrderInput.getDocumentElement();
		eleGetChangeOrderInput.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, elePaymentMethodInput.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));	
		eleGetChangeOrderInput.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);	
		
		Element elePaymentMethods = docGetChangeOrderInput.createElement(AcademyConstants.ELE_PAYMENT_METHODS);
		Element elePaymentMethod = docGetChangeOrderInput.createElement(AcademyConstants.ELE_PAYMENT_METHOD);
		elePaymentMethod.setAttribute(AcademyConstants.ATTR_PAYMENT_KEY, elePaymentMethodInput.getAttribute(AcademyConstants.ATTR_PAYMENT_KEY));
		elePaymentMethod.setAttribute(AcademyConstants.ACADEMY_SUSPEND_CHARGES, AcademyConstants.STR_NO);
		elePaymentMethods.appendChild(elePaymentMethod);
		eleGetChangeOrderInput.appendChild(elePaymentMethods);
		try {
			logger.verbose("Input to ChangeOrder API :\n" + XMLUtil.getXMLString(docGetChangeOrderInput));
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, docGetChangeOrderInput);
			
		} catch (Exception e) {
			logger.verbose("Exception inside AcademyActivateSuspendedPaymentAgent : executeJob ");
			e.printStackTrace();
		}

		logger.verbose("Exiting AcademyActivateSuspendedPaymentAgent : executeJob");
	}
}
