package com.academy.ecommerce.yantriks.general;

import org.w3c.dom.Document;

import com.academy.util.constants.AcademyConstants;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySetTxnObjectForScheduleAndRelease {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySetTxnObjectForScheduleAndRelease.class);

	public Document setTxnObject(YFSEnvironment env, Document docInXml) {

		log.beginTimer("setTxnObject");
		log.verbose("Setting Txn Object");

		/**
		 * Adding the transaction object to identify if the schedule has happened. There
		 * is a scenario where SCHEDULE.0001 and RELEASE.0001 agents processes the taskQ
		 * entry without actually scheduling/releasing the order and ON_SUCCESS event
		 * gets triggered. To identify that scenario, this txn object is used.
		 * 
		 * If transaction object is set, that means genuine schedule/release has
		 * happened. This transaction object is also set at different places so that the check will happen before publishing demand
		 * update to Yantriks
		 */
		env.setTxnObject(AcademyConstants.ATTR_SCH_REL_PROCESSED, AcademyConstants.STR_YES);

		log.verbose("Txn Object Set Successfully");
		log.endTimer("setTxnObject");
		return docInXml;

	}
}
