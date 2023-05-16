package com.academy.ecommerce.sterling.userexits;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSGetOverriddenDGForItemUE;

public class AcademyGetOverriddenDGForItemUE implements YFSGetOverriddenDGForItemUE {

	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyGetOverriddenDGForItemUE.class);

	@Override
	public Document getOverriddenDGForItem(YFSEnvironment ObjYFSEnv, Document docInput) throws YFSUserExitException {

		String strExtnShipFromDC = null;
		String strExtnShipFromDQ = null;

		try {

			logger.verbose("Input to getOverriddenDGForItem :: " + XMLUtil.getXMLString(docInput));

			Element eleExtn = (Element) docInput.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_EXTN)
					.item(0);

			if (null != eleExtn) {

				strExtnShipFromDC = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_SHP_FROM_DC);
				strExtnShipFromDQ = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_SHP_FROM_DQ);

				if (AcademyConstants.STR_NO.equals(strExtnShipFromDC)
						&& AcademyConstants.STR_NO.equals(strExtnShipFromDQ)) {

					setNewEnterpriseLevelDG(docInput,
							AcademyConstants.STR_ENTERPRISE_LEVEL_MONITORING_DG_NON_SFDC_SFDQ);

				} else if (AcademyConstants.STR_YES.equals(strExtnShipFromDC)
						&& AcademyConstants.STR_NO.equals(strExtnShipFromDQ)) {

					setNewEnterpriseLevelDG(docInput, AcademyConstants.STR_ENTERPRISE_LEVEL_MONITORING_DG_NON_SFDQ);

				} else if (AcademyConstants.STR_NO.equals(strExtnShipFromDC)
						&& AcademyConstants.STR_YES.equals(strExtnShipFromDQ)) {

					setNewEnterpriseLevelDG(docInput, AcademyConstants.STR_ENTERPRISE_LEVEL_MONITORING_DG_NON_SFDC);
				}
			}

		} catch (Exception e) {
			logger.error("Exception in method getOverriddenDGForItem() :: " + e);
		}
		return docInput;
	}

	private void setNewEnterpriseLevelDG(Document docInput, String strDistRuleId) {

		try {
			logger.verbose("Input setNewEnterpriseLevelDG() :: " + XMLUtil.getXMLString(docInput));
			logger.verbose("Distribution Rule Id :: " + strDistRuleId);

			Element eleEnterpriseMonitoringDG = (Element) docInput.getDocumentElement()
					.getElementsByTagName(AcademyConstants.ELE_ENTERPRISE_LEVEL_MONITORING_DG).item(0);

			if (null != eleEnterpriseMonitoringDG) {
				eleEnterpriseMonitoringDG.setAttribute(AcademyConstants.ATTR_DISTRIBUTION_RULE_ID, strDistRuleId);
			}

			logger.verbose("Modified input setNewEnterpriseLevelDG() :: " + XMLUtil.getXMLString(docInput));
		} catch (Exception e) {
			logger.error("Exception in method setNewEnterpriseLevelDG() :: " + e);
		}
	}

}
