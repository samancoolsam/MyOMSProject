/**************************************************************************
  * Description	    : This class is invoked by on success event of Create Shipment transaction for 0006 document type
 * --------------------------------
 * 	Date             Author               
 * --------------------------------
 *  26-May-2021      Cognizant			 	 
 * 
 * -------------------------------------------------------------------------
 **************************************************************************/

package com.academy.ecommerce.yantriks.general;

import org.w3c.dom.Document;

import com.academy.util.constants.AcademyConstants;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySetTxnObjectForTOCreateShipmentOnSuccess {
	private static final YFCLogCategory log = YFCLogCategory
			.instance(AcademySetTxnObjectForTOCreateShipmentOnSuccess.class);

	public Document setTxnObjectForTOCreateShipmentOnSuccess(YFSEnvironment env, Document docInXml) {

		log.beginTimer("SetTxnObjectForTOCreateShipmentOnSuccess");
		log.verbose("Setting Txn Object For TO Create Shipment On Success");

		/**
		 * Adding the transaction object to identify if the TO order is moved to
		 * Included in shipment status on create shipment on success of TO. To identify
		 * this scenario, this txn object is used on the service used for posting the
		 * demand update msg to integration queue on changeReleaseStatus event of
		 * changeOrder transaction for 0001 document type.
		 */
		env.setTxnObject(AcademyConstants.IS_STS_SHIPMENT, AcademyConstants.STR_YES);
		log.verbose("Txn Object Set Successfully for For TO Create Shipment On Success");
		log.endTimer("SetTxnObjectForTOCreateShipmentOnSuccess");
		return docInXml;

	}
}
