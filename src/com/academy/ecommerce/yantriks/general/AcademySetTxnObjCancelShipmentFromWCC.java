/**************************************************************************
 * Description	    : This class is invoked on CHANGE_SHIPMENT.ON_LINE_QTY_REDUCTION
 * --------------------------------
 * 	Date             Author               
 * --------------------------------
 *  20-August-2021      Cognizant			 	 
 * 
 * -------------------------------------------------------------------------
 **************************************************************************/
package com.academy.ecommerce.yantriks.general;

import org.w3c.dom.Document;

import static com.academy.util.constants.AcademyConstants.*;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySetTxnObjCancelShipmentFromWCC {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySetTxnObjCancelShipmentFromWCC.class);

	public Document setTxnObjectForCancelShipmentFromWCC(YFSEnvironment env, Document docInXml) {

		log.beginTimer("setTxnObjectForCancelShipmentFromWCC");
		 /*
		 *This transaction object is set, to determine if the BOPIS Order is cancelled from Call Center.
		 * If this transaction object is set , then we will publish the message to the async integ server queue for posting demand update to Yantriks
		 * as part of OMNI-49970.
		 */
		env.setTxnObject(BOPIS_CANCEL_FROM_CALL_CENTER,STR_YES);
		log.verbose("Txn Object Set Successfully for BOPIS cancellation from Call Center");
		log.endTimer("setTxnObjectForCancelShipmentFromWCC");
		return docInXml;

	}
}
