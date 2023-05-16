package com.academy.ecommerce.yantriks.general;

import org.w3c.dom.Document;

import com.academy.util.constants.AcademyConstants;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySetTxnObjectForChangeOrderOnCancel {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySetTxnObjectForChangeOrderOnCancel.class);

	public Document setTxnObjectForChangeOrderOnCancel(YFSEnvironment env, Document docInXml) {

		log.beginTimer("setTxnObjectForChangeOrderOnCancel");
		log.verbose("Setting Txn Object For Change Order On Cancel");

		/**
		 * Adding the transaction object to identify if the change Order On cancel event
		 * is invoked. During shortage at the time of customer pick up in SOM
		 * , if change Order On cancel event is invoked and if the status is ready for
		 * customer pick up then we need to clean up the demand reservation at Yantriks side. To identify
		 * this scenario on demand update service, this txn object is used.
		 */
		env.setTxnObject(AcademyConstants.INVOKED_ON_CHANGEORDER_CANCEL, AcademyConstants.STR_YES);
		log.verbose("Txn Object Set Successfully for Change Order On Cancel");
		log.endTimer("setTxnObjectForChangeOrderOnCancel");
		return docInXml;

	}
}
