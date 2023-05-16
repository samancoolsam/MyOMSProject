package com.academy.ecommerce.yantriks.general;

import org.w3c.dom.Document;

import com.academy.util.constants.AcademyConstants;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySetTxnObjectForDemandUpdate {
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademySetTxnObjectForDemandUpdate.class);
	
	public Document setTxnObject(YFSEnvironment env, Document docInXml) {
		log.beginTimer("AcademySetTxnObjectForDemandUpdate.setTxnObject()");
		
		// Set a generic transaction object to mark that demand update needs to
		// be sent to Yantriks
		log.debug("Setting transaction Object for Demand Update in AcademySetTxnObjectForDemandUpdate ");
		env.setTxnObject(AcademyConstants.DEMAND_UPDATE_NEEDED, AcademyConstants.STR_YES);
		log.debug("Transaction Object set successfully");
		
		log.endTimer("AcademySetTxnObjectForDemandUpdate.setTxnObject()");
		return docInXml;
	}
} 
