
/*
 * Created on Jun 29, 2020
 *
 */
package com.academy.som.shipmentTracking;

import org.eclipse.swt.layout.GridData;
import org.w3c.dom.Element;

import com.academy.som.util.AcademyPCAConstants;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.YRCBaseBehavior;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCXmlUtils;

/**
 * Custom Behavior class done for managing the 
 * com.yantra.pca.ycd.rcp.tasks.shipmentTracking.screens.YCDShipmentDetailsContainerPnl
 * added as part of OMNI-6616 to enable and disable few labels based on ShipmentType
 *
 */

public class AcademyShipmentDetailsContainerPnlExtnBehavior extends YRCExtentionBehavior {

	private static String CLASSNAME = "AcademyShipmentDetailsContainerPnlExtnBehavior";

	/**
	 * Superclass method called to modify the OOB screen models when set
	 * @param nameSpace - name of the OOB screen model
	 */
	public void postSetModel(String strNameSpace) {
		final String methodName = "postSetModel(nameSpace)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		if(strNameSpace.equals(AcademyPCAConstants.CONTAINER_ELEMENT))
		{
			Element eleContainer = getModel(strNameSpace);
			Element eleShipemnt = YRCXmlUtils.getChildElement(eleContainer, AcademyPCAConstants.SHIPMENT_ELEMENT);
			String strShipmentType = eleShipemnt.getAttribute(AcademyPCAConstants.SHIPMENT_TYPE_ATTR);
			
			AcademySIMTraceUtil.startMessage("strShipmentType :: "+strShipmentType, methodName);
			
			if(strShipmentType != null && strShipmentType.equals(AcademyPCAConstants.STR_STS)) {
				hideField("lblTrackingNo", this);
				hideField("txtTrackingNo", this);
			}
			else {
				hideField("extn_lblLocation", this);
				hideField("extn_txtLocation", this);
			}
		}
	}

	/**
	 * Method called to hide labels or fields in a given screen.
	 * @param fieldName - name field
	 */
	public static void hideField(String fieldName, Object extnBehavior) {
		GridData gridData = new GridData();
		gridData.exclude = true;
		YRCBaseBehavior parentBehavior = (YRCBaseBehavior) extnBehavior;
		parentBehavior.setControlVisible(fieldName, false);
		parentBehavior.setControlLayoutData(fieldName, gridData);
	}

}
