package com.academy.ecommerce.sterling.order.api;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyPricingAndPromotionUtil;
import com.academy.ecommerce.sterling.util.AcademyServiceUtil;
import com.academy.util.common.AcademyUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSGetOrderNoUE;

public class AcademyGetSalesOrderNumberUEImpl implements YFSGetOrderNoUE {

	/**
     * log variable.
     */
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyGetSalesOrderNumberUEImpl.class);
	
	/**
	 * Creates an order in WCS system without any lines and returns the order number from the 
	 * response. If error throws YFSException
	 */
	public String getOrderNo(YFSEnvironment env, Map map) throws YFSUserExitException {
		
		log.verbose("******** Inside getOrderNo() of AcademyGetSalesOrderNumberUEImpl ********");
		log.beginTimer(" Begining of AcademyGetSalesOrderNumberUEImpl-> getOrderNo Api");
		try {
			Document inDoc = AcademyServiceUtil.getSOAPMsgTemplateForWCCreateOrderWithoutLines();
			inDoc = AcademyPricingAndPromotionUtil.setUserCredentialsForWCRequest(inDoc);
			Document outDoc = AcademyUtil.invokeService(env, "AcademyWCCreateGuestOrderService", inDoc);
			AcademyPricingAndPromotionUtil.hasError(outDoc);
			Element orderEle = (Element)outDoc.getDocumentElement().getElementsByTagName("_ord:OrderIdentifier").item(0);
			String orderNumber = orderEle.getElementsByTagName("_wcf:UniqueID").item(0).getTextContent();
			log.verbose("******** New OrderNumber ::" + orderNumber);
			log.endTimer(" End of AcademyGetSalesOrderNumberUEImpl-> getOrderNo Api");
			return orderNumber;
		} catch (YFSException yfsException) {
			log.error(yfsException);
			throw yfsException;
		}
		 catch (Exception e) {
			log.error(e);
			throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
		}
	}
}