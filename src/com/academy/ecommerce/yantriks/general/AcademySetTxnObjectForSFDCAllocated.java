package com.academy.ecommerce.yantriks.general;

import org.w3c.dom.Document;

import com.academy.util.constants.AcademyConstants;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySetTxnObjectForSFDCAllocated {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySetTxnObjectForSFDCAllocated.class);

	public Document setTxnObjectForSFDCAllocated(YFSEnvironment env, Document docInXml) {

		log.beginTimer("setTxnObjectForSFDCAllocated");
		log.verbose("Setting Txn Object for SFDC on Consolidate To Shipment ");

		/**
		 * Adding the transaction object to identify if shipment is created for SFDC order.There
		 * is a scenario where multiple shipments are created for the same order and hence ending up sending ALLOCATED demand updates  
		 * for the multi lines with the same update time stamp as we are stamping modifyts value which results in Yantriks accepting only the first 
		 * demand update and ignoring the rest as they are of the same timestamp.
		 * 
		 * This transaction object is used in demand update code to determine if we need to use modifyts or JVM time in the demand update request.
		 * If transaction object is set, we will not use modifyts and will be stamping the JVM time on update time stamp as part of OMNI-48549 
		 */
		env.setTxnObject(AcademyConstants.IS_ALLOCATED_CREATED, AcademyConstants.STR_YES);

		log.verbose("Txn Object Set Successfully for SFDC on Consolidate To Shipment ");
		log.endTimer("setTxnObjectForSFDCAllocated");
		return docInXml;

	}
}
