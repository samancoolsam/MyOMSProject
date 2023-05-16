package com.academy.wsc.extn.mashups;

import org.elasticsearch.common.joda.time.LocalDateTime;
import org.w3c.dom.Element;

import com.ibm.wsc.common.mashups.WSCBasePaginatedMashup;
import com.sterlingcommerce.baseutil.SCUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.ui.web.framework.context.SCUIContext;
import com.sterlingcommerce.ui.web.framework.mashup.SCUIMashupMetaData;

public class AcademyeComOrderSearchMashup extends WSCBasePaginatedMashup {
	public Element massageInput(Element elemInput, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext) {
		Element eleInput = super.massageInput(elemInput, mashupMetaData, uiContext);
		if (SCUtil.isVoid(eleInput)) {
			return null;
		} else {

			Element eleOrder = SCXmlUtil.getXpathElement(eleInput, "API/Input/Order");
			String sNumberOfDays = eleOrder.getAttribute("NumberOfDays");
			int iDays = Integer.parseInt(sNumberOfDays);
			eleOrder.removeAttribute("NumberOfDays");
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime then = now.minusDays(iDays);
			String[] dateStringArray = new String[2];
			dateStringArray[0] = now.toString();
			dateStringArray[1] = then.toString();

				String sFromOrderDate = eleOrder.getAttribute("FromOrderDate");
				String sToOrderDate = eleOrder.getAttribute("ToOrderDate");
				if (SCUtil.isVoid(sFromOrderDate) && SCUtil.isVoid(sToOrderDate)) {
					eleOrder.setAttribute("FromOrderDate", dateStringArray[1]);
					eleOrder.setAttribute("ToOrderDate", dateStringArray[0]);
					SCXmlUtil.getString(eleInput);
				}
		}

		return eleInput;
	}

}
