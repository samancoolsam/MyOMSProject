package com.academy.ecommerce.sterling.util.webservice;

import java.util.Properties;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyValidateCustomerData implements YIFCustomApi {

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public Document validateCustomerData(YFSEnvironment env,
			Document inDoc) {
		
		YFCDocument outDoc = YFCDocument.getDocumentFor(
			"<AcademyValidateResponse ValidationResult='FAILED' ErrorCode='' ErrorDescription='' />");
		
		try {
			String orderNo = XPathUtil.getString(inDoc, "/AcademyValidateRequest/Order/@OrderNo");
			if(!YFCObject.isNull(orderNo)) {
				YFCDocument getOrderListInDoc = YFCDocument.createDocument("Order");
				YFCElement getOrderListInDocEle = getOrderListInDoc.getDocumentElement();
				getOrderListInDocEle.setAttribute("OrderNo", orderNo);
				getOrderListInDocEle.setAttribute("EnterpriseCode", "Academy_Direct");
				getOrderListInDocEle.setAttribute("DocumentType", "0001");
				
				YFCDocument getOrderListOutPutTemplate = YFCDocument.getDocumentFor(
						"<OrderList TotalOrderList='' ><Order OrderHeaderKey='' /></OrderList>");
				env.setApiTemplate("getOrderList", getOrderListOutPutTemplate.getDocument());
				Document getOrderLisoutDoc = AcademyUtil.invokeAPI(env, "getOrderList", getOrderListInDoc.getDocument());
				env.clearApiTemplate("getOrderList");
				if(Integer.parseInt(XPathUtil.getString(getOrderLisoutDoc, "/OrderList/@TotalOrderList")) > 0) {
					outDoc.getDocumentElement().setAttribute("ValidationResult", "PASSED");
				}
			} else if(!YFCObject.isNull(XPathUtil.getString(inDoc, "/AcademyValidateRequest/Customer/@PhoneNo"))) {
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			outDoc.getDocumentElement().setAttribute("ErrorCode", "AcademyWSError");
			outDoc.getDocumentElement().setAttribute("ErrorDescription", e.getMessage());
		}
		return outDoc.getDocument();
	}
}
