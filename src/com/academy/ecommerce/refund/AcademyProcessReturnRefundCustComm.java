package com.academy.ecommerce.refund;

import java.util.List;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @Author Everest
 * @Date Created 27/06/2022
 * 
 *       JIRAS - OMNI-66692 and OMNI-75378
 * @Purpose 1.Call manageTaskQueue - Creating taskq for sending Return Refund
 *          mail to customer. To follow the existing email process, we insert
 *          record into TaskQ table. ACADEMY_EMAIL_AGENT_SERVER sends out the
 *          emails
 * 
 *          Input to manageTaskQueue api
 * 
 *          <TaskQueue DataKey='201912190419552127203822' DataType="ShipmentKey"
 *          TransactionId='SEND_EMAIL_RET_REF.0003.ex'/>
 * 
 **/

public class AcademyProcessReturnRefundCustComm implements YIFCustomApi {

	private Properties props;
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyProcessReturnRefundCustComm.class);

	/**
	 * Preparing input for manageTaskQueue API
	 * 
	 * @param strTaskQDataKey
	 * @return docInManageTaskQueue
	 * @throws Exception
	 */
	public Document manageTaskQueueInput(YFSEnvironment env, Document inXML) throws Exception {
		log.verbose("AcademyProcessReturnRefundCustComm.manageTaskQueueInput: " + XMLUtil.getXMLString(inXML));
		Document docInManageTaskQueue = null;
		Element eleInManageTaskQueue = null;
		List<Element> eleInvoices = SCXmlUtil.getElements(
				SCXmlUtil.getXpathElement(inXML.getDocumentElement(), "InvoiceCollections"), "InvoiceCollection");
		for (Element eleInvoice : eleInvoices) {

			String sAmountCollected = eleInvoice.getAttribute("AmountCollected");
			String sTotalAmount = eleInvoice.getAttribute("TotalAmount");
			String sStrOrderInvoiceKey = SCXmlUtil.getXpathAttribute(eleInvoice, "OrderInvoice/@OrderInvoiceKey");
			String sAuthAmount = SCXmlUtil.getXpathAttribute(eleInvoice,
					"OrderInvoice/ChargeTransaction/CreditCardTransactions/CreditCardTransaction[@AuthReturnCode='DUMMY_SETTLEMENT']/@AuthAmount");
			String sInvoiceType = eleInvoice.getAttribute("InvoiceType");
			if ("RETURN".equals(sInvoiceType) && YFCCommon.isVoid(sAuthAmount)
					&& sTotalAmount.equals(sAmountCollected)) {
				docInManageTaskQueue = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
				eleInManageTaskQueue = docInManageTaskQueue.getDocumentElement();
				eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY, sStrOrderInvoiceKey);
				eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE,
						AcademyConstants.ATTR_ORDER_INVOICE_KEY);
				eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_TRANS_ID, "SEND_EMAIL_RET_REF.0003.ex");
				log.verbose("Manage TaskQueue input: " + XMLUtil.getXMLString(docInManageTaskQueue));
				AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_TASK_QUEUE, docInManageTaskQueue);

			}

		}
		return inXML;

	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
		this.props = props;
	}

}
