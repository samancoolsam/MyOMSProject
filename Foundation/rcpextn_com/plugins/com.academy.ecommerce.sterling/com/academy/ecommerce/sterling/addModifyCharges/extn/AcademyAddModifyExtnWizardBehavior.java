/**
 * 
 */
package com.academy.ecommerce.sterling.addModifyCharges.extn;

import org.w3c.dom.Element;

import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCValidationResponse;

/**
 * @author sahmed
 * 
 */
public class AcademyAddModifyExtnWizardBehavior extends YRCExtentionBehavior {

	@Override
	public void postSetModel(String arg0) {
		
		// Defect:11518, Add/Modify Charge at header level in COM is not required
		// Fix: Made an entry "headerModifyChargesLnk" in com.academy.ecommerce.sterling_extn.yuix file and made it invisible 
		setControlVisible("headerModifyChargesLnk", false);
		
		if (arg0.equals("LineDetails")) {
			Element eleLineDetails = getModel("LineDetails");
			String strMinLineStatus = eleLineDetails
					.getAttribute("MinLineStatus");
			int iMinLineStatus = Integer.parseInt(strMinLineStatus.substring(0,
					4));
			if (iMinLineStatus < 3700) {
				setControlVisible("lineModifyChargesLnk", true);

			} else {
				setControlVisible("lineModifyChargesLnk", false);
			}
		}
		super.postSetModel(arg0);
	}

	@Override
	public YRCValidationResponse validateLinkClick(String arg0) {
		return super.validateLinkClick(arg0);
	}

	@Override
	public YRCValidationResponse validateButtonClick(String fieldName) {
		// TODO Auto-generated method stub
		return super.validateButtonClick(fieldName);
	}

}
// TODO Validation required for a Link control: lineModifyChargesLnk
