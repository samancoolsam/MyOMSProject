package com.academy.ecommerce.sterling.condition;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.academy.util.xml.XPathUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcdemyIsShipmentSTSAmmoOrHazmat implements YCPDynamicConditionEx {
	private static YFCLogCategory log = YFCLogCategory.instance(AcdemyIsShipmentSTSAmmoOrHazmat.class);
	private Map propMap = null;

	public boolean evaluateCondition(YFSEnvironment env, String arg1, Map arg2, Document inputDoc) {
		log.beginTimer("Begining of AcdemyIsShipmentSTSAmmoOrHazmat-> evaluateCondition Api");

		boolean isSTSAmmoOrHazmat = false;
		try {

			NodeList nlAmmoOrHazmatLines = XPathUtil.getNodeList(inputDoc.getDocumentElement(),
					"/Container/ContainerDetails/ContainerDetail/ShipmentLine/OrderLine[@LineType='AMMO' or @LineType='HAZMAT']");

			if (null != nlAmmoOrHazmatLines && nlAmmoOrHazmatLines.getLength()>0) {
				isSTSAmmoOrHazmat = true;
			}

		} catch (Exception e) {
			log.error("Exception caught is :: " + e.getMessage());
		}
		
		log.verbose("Is AMMO or HAZMAT :: "+isSTSAmmoOrHazmat);
		
		return isSTSAmmoOrHazmat;
	}

	public void setProperties(Map propMap) {
		this.propMap = propMap;
	}
}
