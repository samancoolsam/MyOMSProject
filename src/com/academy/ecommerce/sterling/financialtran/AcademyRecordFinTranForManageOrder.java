package com.academy.ecommerce.sterling.financialtran;

import java.util.Properties;    

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyRecordFinTranForManageOrder implements YIFCustomApi {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyRecordFinTranForManageOrder.class);
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}

	/**
	 * Method to prepare input for AcademyRecordFinTranService service.
	 * 
	 * @param env
	 * @param inDoc -
	 *            Input from ON_SUCCESS event of createOrder and
	 *            ConfirmDraftOrder
	 * @return Document
	 * @throws YFSException
	 */
	public Document manageCreateOrderFinTran(YFSEnvironment env, Document inDoc)
			throws Exception {
		log.beginTimer(" Begining of AcademyRecordFinTranForManageOrder-> manageCreateOrderFinTran Api");
		log.verbose("*********** CREATE ORDER input doc : " +  XMLUtil.getXMLString(inDoc));
		/* Out doc will be input for multiApi call
		 * <MultiApi> 
		 * 		<API IsExtendedDbApi="Y" Name="createACADFinTrans" Version="">
		 * 			<Input> 
		 * 				<ACADFinTrans AcademyFinTranCode="005"
		 * 				AcademyFinTranEntityCode="00005" AcademyFinTranDate=""
		 * 				AcademyFinTranSourceKey="OrderNo" AcademyFinTranOrderNo="OrderNo"
		 * 				AcademyFinTranExtendedAmount="" AcademyFinTranGCRedemptionAmount=""
		 * 				AcademyFinTranShipLineFlag="N" AcademyFinTranCustNo="CustomerID"
		 * 				AcademyFinTranTenderType="GIFT_CARD" AcademyFinTranOperator="00000001"
		 * 				AcademyFinTranTLogTerminal="001" AcademyFinTranTLogTranId="" /> 
		 * 			</Input>
		 * 		</API> 
		 * 		<API IsExtendedDbApi="Y" Name="createACADFinTrans" Version="">
		 * 			<Input> 
		 * 				<ACADFinTrans AcademyFinTranCode="011"
		 * 				AcademyFinTranEntityCode="00005" AcademyFinTranDate=""
		 * 				AcademyFinTranSourceKey="OrderNo" AcademyFinTranOrderNo="OrderNo"
		 * 				AcademyFinTranExtendedAmount="" AcademyFinTranGCRedemptionAmount=""
		 * 				AcademyFinTranShipLineFlag="N" AcademyFinTranCustNo="CustomerID"
		 * 				AcademyFinTranTenderType="GIFT_CARD" AcademyFinTranOperator="00000001"
		 * 				AcademyFinTranTLogTerminal="001" AcademyFinTranTLogTranId="" /> 
		 * 			</Input>
		 * 		</API> 
		 * </MultiApi>
		 */

		
		NodeList gcPayments = XMLUtil.getNodeList(inDoc, 
				"/Order/PaymentMethods/PaymentMethod[@PaymentType='GIFT_CARD']");
		double totalGCPayments = 0.0;
		for(int i = 0; i < gcPayments.getLength(); i++ ) {
			Element gcPaymentElement = (Element) gcPayments.item(i);
			totalGCPayments += Double.parseDouble(gcPaymentElement.getAttribute("TotalCharged"));
		}		
		String orderNo = XMLUtil.getString(inDoc, "/Order/@OrderNo");
		String custId = YFCObject.isVoid(XMLUtil.getString(inDoc, "/Order/@BillToID")) ?
			XMLUtil.getString(inDoc, "/Order/@CustomerEMailID") : XMLUtil.getString(inDoc, "/Order/@BillToID");
		String gcPayentTotal = String.valueOf(totalGCPayments);
		//String orderDate = XMLUtil.getString(inDoc, "/Order/@OrderDate");
		//Fix for Deferred GC on 2013-08-23		
		//get the current date
		YFCDate y = new YFCDate();
		//format to get 'yyyyMMdd'
		String currentDate = y.getString("yyyyMMdd");

		YFCDocument acadFinTranDoc = YFCDocument.createDocument("MultiApi");
		YFCElement acadFinTranEle = acadFinTranDoc.getDocumentElement();
		createInputForCreateOrderRecord(env, acadFinTranEle, true, orderNo, custId, gcPayentTotal, currentDate);
		createInputForCreateOrderRecord(env, acadFinTranEle, false, orderNo, custId, gcPayentTotal, currentDate);
		log.endTimer(" End of AcademyRecordFinTranForManageOrder-> manageCreateOrderFinTran Api");
		return acadFinTranDoc.getDocument();
	}

	/**
	 * 
	 * @param apiIndoc
	 * @param isRedemption
	 * @param orderNo
	 * @param custId
	 * @param gcAmount
	 * @param orderDate
	 * @throws YFSException
	 */
	private static void createInputForCreateOrderRecord(YFSEnvironment env, YFCElement apiIndoc, 
			boolean isRedemption, String orderNo, String custId, 
			String gcAmount, String currentDate) throws YFSException {
		log.beginTimer(" Begin of AcademyRecordFinTranForManageOrder-> createInputForCreateOrderRecord Api");
		YFCElement acadFinTranEle = AcademyRecordFinTranUtil.getFinTranApiInput(apiIndoc);
		
		if(isRedemption) {
			acadFinTranEle.setAttribute("AcademyFinTranCode", 
					AcademyRecordFinTranUtil.getAcademyFinTranCode(env, AcademyConstants.ORDER_ENTRY_GC_REDEMPTION));
			acadFinTranEle.setAttribute("AcademyFinTranGCRedemptionAmount", gcAmount);
		} else {
			acadFinTranEle.setAttribute("AcademyFinTranCode", 
					AcademyRecordFinTranUtil.getAcademyFinTranCode(env, AcademyConstants.ORDER_ENTRY_DEFERRED_REVENUE));
			acadFinTranEle.setAttribute("AcademyFinTranDeferredRevenue", gcAmount);
		}
		
		acadFinTranEle.setAttribute("AcademyFinTranEntityCode", "00005");
		acadFinTranEle.setAttribute("AcademyFinTranDate", currentDate);
		acadFinTranEle.setAttribute("AcademyFinTranSourceKey", orderNo);
		acadFinTranEle.setAttribute("AcademyFinTranOrderNo", orderNo);
		acadFinTranEle.setAttribute("AcademyFinTranExtendedAmount", gcAmount);
		acadFinTranEle.setAttribute("AcademyFinTranShipLineFlag", "N");
		acadFinTranEle.setAttribute("AcademyFinTranCustNo", custId);
		acadFinTranEle.setAttribute("AcademyFinTranTenderType", "GIFT_CARD");
		acadFinTranEle.setAttribute("AcademyFinTranOperator", "00000001");
		acadFinTranEle.setAttribute("AcademyFinTranTLogTerminal", "0001");
		acadFinTranEle.setAttribute("AcademyFinTranTLogTranId", "0001");
		
		// FIX FOR DEFECT #3251 - set 2000-01-01 12:00:00 as the default DBR_PROCESSED_DATETIME
		acadFinTranEle.setAttribute("AcademyFinTranProcessTime", "2000-01-01T12:00:00");

		log.verbose("*********** createInputForCreateOrderRecord bean value - " + acadFinTranEle.toString());
		log.endTimer(" End of AcademyRecordFinTranForManageOrder-> createInputForCreateOrderRecord Api");
	}
	
	
	/**
	 * Method to prepare input for AcademyRecordFinTranService service.
	 * 
	 * @param env
	 * @param inDoc -
	 *            Input from ON_CANCEL event of changeOrder and
	 *            ConfirmDraftOrder
	 * @return Document
	 * @throws YFSException
	 */
	public Document manageCancelOrderFinTran(YFSEnvironment env, Document inDoc)
			throws Exception {
		log.beginTimer(" begin of AcademyRecordFinTranForManageOrder-> manageCancelOrderFinTran Api");
		log.verbose("*********** CANCEL ORDER input doc : " +  XMLUtil.getXMLString(inDoc));
		String orderNo = XMLUtil.getString(inDoc, "/Order/@OrderNo");
		String custId = XMLUtil.getString(inDoc, "/Order/@CustomerEMailID");
		YFCDate y = new YFCDate(); 
		//format to get 'yyyyMMdd'
		String strCurrentDate = y.getString("yyyyMMdd");
		String defferedAmount = XMLUtil.getString(inDoc, "/Order/PriceInfo/@ChangeInTotalAmount");
		YFCDocument acadFinTranDoc = YFCDocument.createDocument("MultiApi");
		YFCElement acadFinTranEle = acadFinTranDoc.getDocumentElement();
		// Current Date will be stamped as the Financial Transaction Date.
		createInputForCancelOrderRecord(env, acadFinTranEle, true, orderNo, custId, defferedAmount, strCurrentDate);
		//Start GCD-125 Defect GCD-214 **Commenting the below code to avoid deffered sale entry fin tran table. 
		/*createInputForCancelOrderRecord(env, acadFinTranEle, false, orderNo, custId, defferedAmount, strCurrentDate);*/
		//End GCD-125 Defect GCD-214
		log.endTimer(" End of AcademyRecordFinTranForManageOrder-> manageCancelOrderFinTran Api");
		return acadFinTranDoc.getDocument();
	}
	
	
	/** This method will return current date in yyyyMMdd format
	 * @param currentDate 
	 * @return
	 */
	/*public String currentDate(){
		YFCDate y = new YFCDate();
		//format to get 'yyyyMMdd'
		String strCurrentDate = y.getString("yyyyMMdd");
		return strCurrentDate;
	}*/
	/**
	 * 
	 * @param apiIndoc
	 * @param isDefferedCustCredit
	 * @param orderNo
	 * @param custId
	 * @param gcAmount
	 * @param orderDate
	 * @throws YFSException
	 */
	private static void createInputForCancelOrderRecord(YFSEnvironment env, YFCElement apiIndoc, 
			boolean isDefferedCustCredit, String orderNo, String custId, 
			String gcAmount, String strCurrentDate) throws YFSException {
		log.beginTimer("begin of AcademyRecordFinTranForManageOrder-> createInputForCancelOrderRecord Api");
		YFCElement acadFinTranEle = AcademyRecordFinTranUtil.getFinTranApiInput(apiIndoc);
		if(isDefferedCustCredit) {
			//START : GCD-157 and GCD-162
			/*acadFinTranEle.setAttribute("AcademyFinTranCode", 
					AcademyRecordFinTranUtil.getAcademyFinTranCode(env, AcademyConstants.ORDER_ENTRY_GC_BALANCE));
			acadFinTranEle.setAttribute("AcademyFinTranDefferedCustCredit", gcAmount);*/
			acadFinTranEle.setAttribute("AcademyFinTranCode", 
					AcademyRecordFinTranUtil.getAcademyFinTranCode(env, AcademyConstants.ORDER_ENTRY_GC_REDEMPTION));
			acadFinTranEle.setAttribute("AcademyFinTranDefferedCustCredit", gcAmount);
			acadFinTranEle.setAttribute("AcademyFinTranDeferredRevenue",gcAmount);
			//END : GCD-157 and GCD-162
		} else {
			acadFinTranEle.setAttribute("AcademyFinTranCode", 
					AcademyRecordFinTranUtil.getAcademyFinTranCode(env, AcademyConstants.ORDER_ENTRY_DEFERRED_REVENUE));
			acadFinTranEle.setAttribute("AcademyFinTranDeferredRevenue", gcAmount);
		}
		
		// Current Date will be stamped as the Financial Transaction Date.
		acadFinTranEle.setAttribute("AcademyFinTranDate", AcademyRecordFinTranUtil.getTranDate(strCurrentDate));
		acadFinTranEle.setAttribute("AcademyFinTranSourceKey", orderNo);
		acadFinTranEle.setAttribute("AcademyFinTranEntityCode", "00005");
		acadFinTranEle.setAttribute("AcademyFinTranOrderNo", orderNo);
		acadFinTranEle.setAttribute("AcademyFinTranExtendedAmount", gcAmount);
		acadFinTranEle.setAttribute("AcademyFinTranShipLineFlag", "N");
		acadFinTranEle.setAttribute("AcademyFinTranCustNo", custId);
		acadFinTranEle.setAttribute("AcademyFinTranTenderType", "GIFT_CARD");
		acadFinTranEle.setAttribute("AcademyFinTranOperator", "00000001");
		acadFinTranEle.setAttribute("AcademyFinTranTLogTerminal", "0001");
		acadFinTranEle.setAttribute("AcademyFinTranTLogTranId", "0020");
		
		// FIX FOR DEFECT #3251 - set 2000-01-01 12:00:00 as the default DBR_PROCESSED_DATETIME
		acadFinTranEle.setAttribute("AcademyFinTranProcessTime", "2000-01-01T12:00:00");
		log.verbose("*********** createInputForCancelOrderRecord bean value - " + acadFinTranEle.toString());
		log.endTimer(" End of AcademyRecordFinTranForManageOrder-> createInputForCancelOrderRecord Api");
	}
	
	
	/**
	 * Method to prepare input for AcademyRecordFinTranService service.
	 * 
	 * @param env
	 * @param inDoc
	 * @return Document
	 * @throws YFSException
	 */
	public Document manageDeliveryConfFinTran(YFSEnvironment env, Document inDoc)
			throws Exception {
		log.beginTimer(" begin of AcademyRecordFinTranForManageOrder-> manageDeliveryConfFinTran Api");
		log.verbose("*********** DELIVERY CONFIRMATION input doc : " +  XMLUtil.getXMLString(inDoc));
		String orderNo = XMLUtil.getString(inDoc, "/Shipment/OrderInvoiceList/OrderInvoice/@OrderNo");
		String custId = XMLUtil.getString(inDoc, "/Shipment/OrderInvoiceList/OrderInvoice/@PersonID");
		String invoiceDate = "";
		String invoiceNo = "";
		String orderDeliveryDate = "";
		String shipmentKey = XMLUtil.getString(inDoc, "/Shipment/@ShipmentKey");
		
		Node invoiceDetail = XPathUtil.getNode(inDoc, 
				"/Shipment/OrderInvoiceList/OrderInvoice[@InvoiceType='SHIPMENT' and @ShipmentKey='" + shipmentKey + "']");
		if(!YFCObject.isVoid(invoiceDetail)) {
			invoiceNo = XMLUtil.getString(invoiceDetail, "@InvoiceNo");
			invoiceDate = XMLUtil.getString(invoiceDetail, "@DateInvoiced");
			orderDeliveryDate = XMLUtil.getString(invoiceDetail, "../../@StatusDate");
			
			log.verbose("Invoice No - " + invoiceNo + " InvoiceDate - " + invoiceDate + " DeliveryDate - " + orderDeliveryDate);
		}
		
		YFCDocument acadFinTranDoc = YFCDocument.createDocument("MultiApi");
		YFCElement acadFinTranEle = acadFinTranDoc.getDocumentElement();
		
		//FIXED DEFECT - FIN_TRAN_DATE could should have order delivery date (NOT Invoice Date)
		//createInputForDelConfRecord(env, acadFinTranEle, orderNo, custId, invoiceNo, invoiceDate);
		createInputForDelConfRecord(env, acadFinTranEle, orderNo, custId, invoiceNo, orderDeliveryDate);
		
		log.endTimer(" End of AcademyRecordFinTranForManageOrder-> manageDeliveryConfFinTran Api");
		return acadFinTranDoc.getDocument();
	}
	
	/**
	 * 
	 * @param apiIndoc
	 * @param orderNo
	 * @param custId
	 * @param invoiceNo
	 * @param orderDate
	 * @throws YFSException
	 */
	private static void createInputForDelConfRecord(YFSEnvironment env, YFCElement apiIndoc, String orderNo, String custId, 
			String invoiceNo, String orderDate) throws YFSException {
		log.beginTimer(" Begin of AcademyRecordFinTranForManageOrder-> createInputForDelConfRecord Api");
		YFCElement acadFinTranEle = AcademyRecordFinTranUtil.getFinTranApiInput(apiIndoc);
		acadFinTranEle.setAttribute("AcademyFinTranCode", AcademyRecordFinTranUtil.getAcademyFinTranCode(env, AcademyConstants.DELIVERY_CONFIRMATION));
		acadFinTranEle.setAttribute("AcademyFinTranEntityCode", "00005");
		acadFinTranEle.setAttribute("AcademyFinTranDate", AcademyRecordFinTranUtil.getTranDate(orderDate));
		acadFinTranEle.setAttribute("AcademyFinTranSourceKey", invoiceNo);
		acadFinTranEle.setAttribute("AcademyFinTranOrderNo", orderNo);
		acadFinTranEle.setAttribute("AcademyFinTranShipLineFlag", "N");
		acadFinTranEle.setAttribute("AcademyFinTranCustNo", custId);
		acadFinTranEle.setAttribute("AcademyFinTranOperator", "00000001");
		acadFinTranEle.setAttribute("AcademyFinTranTLogTerminal", "0001");
		acadFinTranEle.setAttribute("AcademyFinTranTLogTranId", "0031");

		// FIX FOR DEFECT #3251 - set 2000-01-01 12:00:00 as the default DBR_PROCESSED_DATETIME
		acadFinTranEle.setAttribute("AcademyFinTranProcessTime", "2000-01-01T12:00:00");
		
		log.verbose("*********** createInputForDelConfRecord bean value - " + acadFinTranEle.toString());
		log.endTimer(" End of AcademyRecordFinTranForManageOrder-> createInputForDelConfRecord Api");

	}
}
 