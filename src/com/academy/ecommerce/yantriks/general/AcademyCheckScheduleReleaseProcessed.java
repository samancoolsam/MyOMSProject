package com.academy.ecommerce.yantriks.general;

import java.util.Map;

import org.w3c.dom.Document;

import com.academy.util.constants.AcademyConstants;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyCheckScheduleReleaseProcessed implements YCPDynamicConditionEx {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyCheckScheduleReleaseProcessed.class);

	@Override
	public boolean evaluateCondition(YFSEnvironment env, String str1, Map map1, Document docInXml) {
		log.beginTimer("AcademyCheckScheduleReleaseProcessed.evaluateCondition()");

		/**
		 * This java class returns true or false based on the transaction object set in
		 * different places to identify if the schedule. Confirm assignment also sets this transaction object
		 * release was genuine.
		 */
		log.verbose("Reading Txn Object");

		String strTxnObject = (String) env.getTxnObject(AcademyConstants.ATTR_SCH_REL_PROCESSED);
		if (!YFCObject.isVoid(strTxnObject) && AcademyConstants.STR_YES.equals(strTxnObject)) {
			log.verbose("Transaction object is set. Returning true");
			return true;
		}
		log.verbose("Transaction object is not set. Returning false");
		log.endTimer("AcademyCheckScheduleReleaseProcessed.evaluateCondition()");
		return false;
	}

	@Override
	public void setProperties(Map arg0) {

	}

}
