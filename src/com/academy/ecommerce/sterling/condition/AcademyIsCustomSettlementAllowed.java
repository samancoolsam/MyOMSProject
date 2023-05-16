package com.academy.ecommerce.sterling.condition;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;

import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/*##################################################################################
*
* Project Name                : EGC Release
* Module                      : OMS
* Author                      : CTS
* Date                        : 11-DEC-2020
* Description                 : This file implements the logic to 
* 								1. Check whether the input XML having fulfillment type same as 
*                                  configured in the condition parameter.
*                                  
* JIRA                        : OMNI-15425
*
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author                  Version#       Remarks/Description                     
* ---------------------------------------------------------------------------------
* 11-DEC-2020     CTS                      1.0            Initial version
* ###################################################################################*/

public class AcademyIsCustomSettlementAllowed implements YCPDynamicConditionEx {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyIsCustomSettlementAllowed.class);
	private Map<String, String> propMap = null;

	public void setProperties(Map propMap) {
		this.propMap = propMap;
		log.verbose("setup the PropMap");
	}

	public boolean evaluateCondition(YFSEnvironment env, String sName, Map mapData, Document inDoc) {

		boolean isCustomSettlementAllowed = false;

		try {

			log.debug("Start - AcademyIsCustomSettlementAllowed ###");

			log.verbose("AcademyIsCustomSettlementAllowed.evaluateCondition() - Input to condition :: "
					+ XMLUtil.getXMLString(inDoc));

			String strEligibleFulfillmentTypes = (String) propMap.get(AcademyConstants.STR_ELIGIBLE_FULFILLMENT_TYPES);

			NodeList nlOrderLine = inDoc.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);

			if (null != nlOrderLine && nlOrderLine.getLength() > 0) {

				for (int i = 0; i < nlOrderLine.getLength(); i++) {

					Element eleOrderLine = (Element) nlOrderLine.item(i);

					String strFulfillmentType = eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
					
					log.verbose("Before condition check :: ");
					
					if (!StringUtil.isEmpty(strEligibleFulfillmentTypes)
							&& strEligibleFulfillmentTypes.contains(strFulfillmentType)) {
						
						log.verbose("Setting flag to TRUE :: ");
						
						isCustomSettlementAllowed = true;
						break;
					}

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error while evaluating dynamic condition in "
					+ "AcademyIsCustomSettlementAllowed.evaluateCondition() is:  " + e);
		}
		return isCustomSettlementAllowed;
	}

}
