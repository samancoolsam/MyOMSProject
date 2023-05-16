package com.academy.ecommerce.sterling.condition;

import java.util.Map;

import org.w3c.dom.Document;

import com.academy.ecommerce.yantriks.general.AcademyCheckScheduleReleaseProcessed;
import com.academy.util.constants.AcademyConstants;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.academy.util.constants.AcademyConstants;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyGetTxnObjectForScheduleCancelSTS2 implements YCPDynamicConditionEx {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyGetTxnObjectForScheduleCancelSTS2.class);

	@Override
	public boolean evaluateCondition(YFSEnvironment env, String str1, Map map1, Document docInXml) {
		log.beginTimer("AcademyGetTxnObjectForScheduleCancelSTS2.evaluateCondition()");

		/**
		 * This java class returns true or false based on the transaction object set in
		 * different places to identify if the cancellation. Confirm assignment also
		 * sets this transaction object release was genuine.
		 * 
		 */
		log.verbose("Reading Txn Object");

		String strTxnObject = (String) env.getTxnObject(AcademyConstants.STS2_SCHEDULE_CANCEL);
		if (!YFCObject.isVoid(strTxnObject) && AcademyConstants.STR_YES.equals(strTxnObject)) {
			log.verbose("Transaction object is set. Returning true");
			return true;
		}
		log.verbose("Transaction object is not set. Returning false");
		log.endTimer("AcademyGetTxnObjectForScheduleCancelSTS2.evaluateCondition()");
		return false;
	}

	@Override
	public void setProperties(Map arg0) {

	}
}