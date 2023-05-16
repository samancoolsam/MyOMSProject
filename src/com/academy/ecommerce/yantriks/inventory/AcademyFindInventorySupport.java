package com.academy.ecommerce.yantriks.inventory;

import org.w3c.dom.Document;

import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyFindInventorySupport {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyFindInventorySupport.class);

	// This method is to store the input doc in env obj to figure out the
	// fulfillment type in the UE code during findInventory call
	public Document storeInputDocForFindInventorySupport(YFSEnvironment env, Document doc) {
		log.verbose("Input Document to storeInputDocForFindInventorySupport:: " + SCXmlUtil.getString(doc));
		env.setTxnObject("ReservationInput", doc);
		log.verbose("Setting FindInventory Input Doc in the env obj");
		return doc;
	}

}
