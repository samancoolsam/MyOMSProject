package com.academy.ecommerce.sterling.order.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademySendReturnEmailForInvoice implements YIFCustomApi {

	private Document docInvoiceDetails=null;

	public void setProperties(Properties arg0) throws Exception {

	}

	private static final YFCLogCategory log = YFCLogCategory
			.instance(AcademySendReturnEmailForInvoice.class);

	public Document processReturnOrderInvoiceAndSendEmail(YFSEnvironment env,
			Document inDoc) {

		log.verbose("***** Inside AcademySendReturnEmailForInvoice......");
		if(!YFCObject.isVoid(inDoc)){
			log.verbose("***** Input document to AcademySendReturnEmailForInvoice is present......"+XMLUtil.getXMLString(inDoc));
			try {
				String strOrderInvoiceKey=XPathUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/@OrderInvoiceKey");
				log.verbose("****** Order Invoice Key************"+strOrderInvoiceKey);
				if(!YFCObject.isNull(strOrderInvoiceKey)){
					Document docGetOrderInvoiceDetail=XMLUtil.createDocument("GetOrderInvoiceDetails");
					Element eleGetOrderInvoiceDetail=docGetOrderInvoiceDetail.getDocumentElement();
					eleGetOrderInvoiceDetail.setAttribute("InvoiceKey",strOrderInvoiceKey);
					env.setApiTemplate("getOrderInvoiceDetails","global/template/api/getOrderInvoiceDetails_ReturnInvoiceTemplate.xml");
					docInvoiceDetails = AcademyUtil.invokeAPI(env,"getOrderInvoiceDetails", docGetOrderInvoiceDetail);
					env.clearApiTemplates();
					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				throw new YFSException(e.getMessage());
			}
			
			
		}
		return docInvoiceDetails;

	}
}
