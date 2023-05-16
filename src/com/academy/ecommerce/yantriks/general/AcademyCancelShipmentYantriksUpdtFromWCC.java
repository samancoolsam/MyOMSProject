/**************************************************************************
 * Description	    : This class is invoked on ORDER_CHANGE.ON_SUCCESS
 * --------------------------------
 * 	Date             Author               
 * --------------------------------
 *  20-August-2021      Cognizant			 	 
 * 
 * -------------------------------------------------------------------------
 **************************************************************************/
package com.academy.ecommerce.yantriks.general;


import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import static com.academy.util.constants.AcademyConstants.*;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyCancelShipmentYantriksUpdtFromWCC {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyCancelShipmentYantriksUpdtFromWCC.class);

	public Document publishCancelShipmentYantriksUpdtMsgToQ(YFSEnvironment env, Document docInXml) throws Exception {

		log.beginTimer("publishCancelShipmentYantriksUpdtMsgToQ");
		log.verbose("Input Doc to AcademyCancelShipmentYantriksUpdtFromWCC: "+SCXmlUtil.getString(docInXml));
		log.verbose("Fetching Txn Object For BOPIS Cancellation From Call Center");
		/*
		 * Here we fetch the transaction object which is set in AcademySetTxnObjCancelShipmentFromWCC (com.academy.ecommerce.yantriks.general)  
		 * for BOPIS cancellations from Call Center and if set then we publish the demand message to Integ server Queue
		 * as part of OMNI-49970.
		 * */
		String isBOPISCancelledFromWCC=(String) env.getTxnObject(BOPIS_CANCEL_FROM_CALL_CENTER);
		log.verbose("isBOPISCancelledFromWCC:"+isBOPISCancelledFromWCC);
		if (!YFCObject.isVoid(isBOPISCancelledFromWCC) && STR_YES.equals(isBOPISCancelledFromWCC)) {
			log.verbose("Invoke ShipmentDemandUpdate service to publishDemandToQ for BOPIS Cancellation From Call Center");
			AcademyUtil.invokeService(env, SERVICE_YAN_SHIPMENT_DEMAND_UPDATE, docInXml);
		}

		log.endTimer("publishCancelShipmentYantriksUpdtMsgToQ");
		return docInXml;

	}
}
