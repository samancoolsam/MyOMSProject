package com.academy.ecommerce.sterling.alertConsole.extn;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;

public class AcademyAlertListExtnBehavior extends YRCExtentionBehavior {

	@Override
	public boolean preCommand(YRCApiContext apiContext) {
		String[] strApis = apiContext.getApiNames();
		for (int i = 0; i < strApis.length; i++) {
			if (strApis[i].equals("getExceptionList")) {
				Element eleInbox = (Element) apiContext.getInputXml()
						.getDocumentElement();

				Element extnElement = (Element) eleInbox.getElementsByTagName(
						AcademyConstants.ELE_EXTN).item(0);

				if (YRCPlatformUI.isVoid(extnElement)) {
					extnElement = eleInbox.getOwnerDocument().createElement(
							AcademyConstants.ELE_EXTN);
					eleInbox.appendChild(extnElement);
				}
				if (!YRCPlatformUI.isVoid(extnElement)) {
					String strFromExtnOrderTotal = extnElement
							.getAttribute("FromExtnOrderTotal");
					if (!YRCPlatformUI.isVoid(strFromExtnOrderTotal)) {
						extnElement.setAttribute("ExtnOrderTotalQryType",
								"BETWEEN");
					} else if (YRCPlatformUI.isVoid(strFromExtnOrderTotal)
							&& !YRCPlatformUI.isVoid(extnElement
									.getAttribute("ToExtnOrderTotal"))) {
						extnElement.setAttribute("FromExtnOrderTotal", "0.00");
						extnElement.setAttribute("ExtnOrderTotalQryType",
								"BETWEEN");
					}
				}
				/*
				 * Filter out Agent Exception and UI Exception from the search
				 * list result.
				 */
				extnElement.setAttribute("ExtnNonCCExceptionType", "N");
				filterOutClosedAlerts(eleInbox);

				YRCPlatformUI
						.trace("**** Final getExceptionList input is********"
								+ XMLUtil.getElementXMLString(eleInbox));

			}
		}
		return super.preCommand(apiContext);
	}

	private void filterOutClosedAlerts(Element eleInbox) {
		boolean filterClosedAlerts = false;
		String strStatus = eleInbox.getAttribute("Status");
		String strStatusQryType = eleInbox.getAttribute("StatusQryType");
		YRCPlatformUI.trace("**** Status is part of the alert search criteria"
				+ strStatus);
		if ("".equals(strStatus)) {
			YRCPlatformUI.trace("****Search is conducted for Status OPEN,WIP and CLOSED********");
			filterClosedAlerts = true;
		} else if ("CLOSED".equals(strStatus)
				&& ("EQ".equals(strStatusQryType))) {
			YRCPlatformUI.trace("****Search is conducted for Status CLOSED********");
			filterClosedAlerts = true;
		} else if ("OPEN".equals(strStatus) && ("NE".equals(strStatusQryType))) {
			YRCPlatformUI.trace("****Search is conducted for Status WIP and CLOSED********");
			filterClosedAlerts = true;
		} else if ("WIP".equals(strStatus) && ("NE".equals(strStatusQryType))) {
			YRCPlatformUI.trace("****Search is conducted for Status OPEN and CLOSED********");
			filterClosedAlerts = true;
		} else {
			filterClosedAlerts = false;
		}
		if (filterClosedAlerts) {
			YRCPlatformUI.trace("****Closed alert search is conducted, closed alerts beyond 90 days are determined.********");
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();
			YRCPlatformUI.trace("****Current Date is ******"
					+ format.format(cal.getTime()));
			cal.add(Calendar.DATE, -90);
			YRCPlatformUI.trace("******90 days from current date"
					+ format.format(cal.getTime()));
			String strClosedOn = format.format(cal.getTime());
			Element eleComplexQuery = eleInbox.getOwnerDocument()
					.createElement("ComplexQuery");
			eleComplexQuery.setAttribute("Operator", "And");
			eleInbox.appendChild(eleComplexQuery);
			Element eleOr = eleComplexQuery.getOwnerDocument().createElement(
					"Or");
			eleComplexQuery.appendChild(eleOr);
			Element eleExp = eleOr.getOwnerDocument().createElement("Exp");
			eleExp.setAttribute("Name", "ClosedOn");
			eleExp.setAttribute("Value", strClosedOn);
			eleExp.setAttribute("QryType", "GT");
			eleOr.appendChild(eleExp);
			if ("".equals(strStatus)) {
				Element eleExp1 = eleOr.getOwnerDocument().createElement("Exp");
				eleExp1.setAttribute("Name", "Status");
				eleExp1.setAttribute("Value", "OPEN");
				eleExp1.setAttribute("QryType", strStatusQryType);
				eleOr.appendChild(eleExp1);
				Element eleExp2 = eleOr.getOwnerDocument().createElement("Exp");
				eleExp2.setAttribute("Name", "Status");
				eleExp2.setAttribute("Value", "WIP");
				eleExp2.setAttribute("QryType", strStatusQryType);
				eleOr.appendChild(eleExp2);
			} else if (!"CLOSED".equals(strStatus)) {
				Element eleExp1 = eleOr.getOwnerDocument().createElement("Exp");
				eleExp1.setAttribute("Name", "Status");
				eleExp1.setAttribute("Value", strStatus);
				eleExp1.setAttribute("QryType", strStatusQryType);
				eleOr.appendChild(eleExp1);
			}

		}
	}
}
