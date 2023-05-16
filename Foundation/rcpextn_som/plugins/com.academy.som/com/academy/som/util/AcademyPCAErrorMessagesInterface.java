package com.academy.som.util;

/**
 * This interface holds all the constants that serve as key to the error
 * messages that are to be extracted from the resource bundle property file.
 * 
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 *         Copyright © 2006-2009 Sterling Commerce, Inc. All Rights Reserved.
 */
public interface AcademyPCAErrorMessagesInterface {

	// Added Error Message Keys for Record Backroom Pick wizards :: START
	public final String CONTAINER_WEIGHT_VAL_ERR_MSG_KEY = "CONTAINER_WEIGHT_VAL_ERR_MSG_KEY";

	public final String CONTAINER_WEIGHT_TYPE_ERR_MSG_KEY = "CONTAINER_WEIGHT_TYPE_ERR_MSG_KEY";

	public final String UOM_NOT_PICKED_ERR_MSG_KEY = "UOM_NOT_PICKED_ERR_MSG_KEY";

	// Added Error Message Keys for Record Backroom Pick wizards :: END

	// Added Error Message Keys for Print Pick Ticket wizards :: START

	public final String BATCH_NO_VAL_ERR_MSG_KEY = "BATCH_NO_VAL_ERR_MSG_KEY";

	// Added Error Message Keys for Print Pick Ticket wizards :: END

	// Added Error Message Keys for Search Shipment wizards :: START

	public final String REPRINT_LABEL_ERR_MSG_KEY = "REPRINT_LABEL_ERR_MSG_KEY";

	public final String AUTO_CORRECT_MANIFEST_ERR_MSG_KEY = "AUTO_CORRECT_MANIFEST_ERR_MSG_KEY";

	// Added Error Message Keys for Search Shipment wizards :: END

	// Added Error Message Keys for Close Manifest wizards :: START

	public final String FUTURE_MANIFEST_ERR_MSG_KEY = "FUTURE_MANIFEST_ERR_MSG_KEY";

	public final String CLOSE_MANIFEST_BKRM_ERR_MSG_KEY = "CLOSE_MANIFEST_BKRM_ERR_MSG_KEY";

	// Added Error Message Keys for Close Manifest wizards :: END

	// Ship From DC Changes :: Start
	public final String VENDOR_PKG_CON_PICK_ERROR_MSG_KEY = "VENDOR_PKG_CON_PICK_ERROR_MSG_KEY";

	public final String VENDOR_PKG_MULTI_QTY_BLK_PICK_ERROR_MSG_KEY = "VENDOR_PKG_MULTI_QTY_BLK_PICK_ERROR_MSG_KEY";
	// Ship From DC Changes :: End
}
