package com.academy.ecommerce.yantriks.inventory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.constants.AcademyConstants;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyStoreEnvObjForFindInventory {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyStoreEnvObjForFindInventory.class);

	// This method is to store the fulfillment to env obj to figure oout the
	// fulfillment type at the findInventory call
	public Document storeFulfillmentTypeForfindInventory(YFSEnvironment env, Document doc) {
		log.verbose("Input Document :: " + SCXmlUtil.getString(doc));
		Element elePromise = doc.getDocumentElement();
		String strFulfillmentType = elePromise.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
		if (!YFCCommon.isVoid(strFulfillmentType)) {
			log.info("Setting the environment object with Fulfillment type of FindInventory Input Service");
			env.setTxnObject(AcademyConstants.ATTR_FULFILLMENT_TYPE, strFulfillmentType);
		}
		return doc;
	}

}
