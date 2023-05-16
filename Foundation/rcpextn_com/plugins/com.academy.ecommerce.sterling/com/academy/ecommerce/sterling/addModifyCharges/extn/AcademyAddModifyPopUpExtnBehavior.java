/**
 * 
 */
package com.academy.ecommerce.sterling.addModifyCharges.extn;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.academy.ecommerce.sterling.util.XPathUtil;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCExtendedCellModifier;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCExtendedTableImageProvider;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCTblClmBindingData;
import com.yantra.yfc.rcp.YRCValidationResponse;

/**
 * @author sahmed
 * 
 */
public class AcademyAddModifyPopUpExtnBehavior extends YRCExtentionBehavior {

	@Override
	public YRCValidationResponse validateButtonClick(String fieldName) {
		YRCPlatformUI
				.trace("########Inside Validate Button Click Method########");
		if (fieldName.equals("applyBttn")) {
			Element eleChangeOrder = getTargetModel("changeOrder_input");
			YRCPlatformUI.trace("######Element inside Table Binging Data######"
					+ XMLUtil.getElementXMLString(eleChangeOrder));
			try {
				NodeList nLineCharges = XPathUtil.getNodeList(eleChangeOrder,
						"/OrderLine/LineCharges/LineCharge");
				if (!YRCPlatformUI.isVoid(nLineCharges)) {
					int iNoOfOrderLines = nLineCharges.getLength();
					for (int i = 0; i < iNoOfOrderLines; i++) {
						Element eleLineCharge = (Element) nLineCharges.item(i);
						String strIsNewCharge = eleLineCharge
								.getAttribute("IsNewCharge");
						if (YRCPlatformUI.isVoid(strIsNewCharge)) {
							String strChargeperLine = eleLineCharge
									.getAttribute("ChargePerLine");
							String strPreviousLineCharge = eleLineCharge
									.getAttribute("PreviousValue");

							int iChargePerLine = Integer
									.parseInt(strChargeperLine.substring(0,
											strChargeperLine.lastIndexOf(".")));
							int iPreviousValue = Integer
									.parseInt(strPreviousLineCharge.substring(
											0, strPreviousLineCharge
													.lastIndexOf(".")));
							if (iChargePerLine != iPreviousValue) {

								YRCPlatformUI
										.showError(
												AcademyPCAConstants.ATTR_ERROR,
												"Modification is not allowed for existing charges");
								return new YRCValidationResponse(
										YRCValidationResponse.YRC_VALIDATION_ERROR,
										"Modification is not allowed for existing charges");
							}
						}
					}

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return super.validateButtonClick(fieldName);
	}
}
// TODO Validation required for a Button control: applyBttn
