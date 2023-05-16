package com.academy.wsc.extn.mashups;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.constants.AcademyConstants;
import com.academy.wsc.extn.common.constants.AcademyWSCConstants;
import com.ibm.wsc.common.mashups.WSCBasePaginatedMashup;
import com.ibm.wsc.mashups.utils.WSCMashupUtils;
import com.sterlingcommerce.baseutil.SCUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.ui.web.framework.context.SCUIContext;
import com.sterlingcommerce.ui.web.framework.helpers.SCUILocalizationHelper;
import com.sterlingcommerce.ui.web.framework.helpers.SCUIMashupHelper;
import com.sterlingcommerce.ui.web.framework.mashup.SCUIMashupMetaData;
import com.sterlingcommerce.ui.web.framework.utils.SCUIUtils;
import com.yantra.yfc.ui.backend.util.APIManager;
import com.yantra.yfc.core.YFCObject;

public class AcademyBOPISWSCShipmentSearchCustomMashup extends WSCBasePaginatedMashup implements AcademyWSCConstants {
	Element timeThresholdCodeListForShip = null;
	Element timeThresholdCodeListForPick = null;
	private static final String SHIP_DELIVERY_METHOD = "SHP";
	private static final String PICK_DELIVERY_METHOD = "PICK";

	public Element massageInput(Element inputEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext) {
		Element eInput = super.massageInput(inputEl, mashupMetaData, uiContext);

		Element apiInput = SCXmlUtil.getFirstChildElement(
				SCXmlUtil.getChildElement(SCXmlUtil.getChildElement(eInput, "API", true), "Input", true));

NodeList NLshipmentLine = apiInput.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

for (int l = 0; l < NLshipmentLine.getLength(); l++) 
{
	Element eleShipmentLine = (Element) NLshipmentLine.item(l);
	String strOrderNo= eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_NO);
	if (!YFCObject.isVoid(strOrderNo))
	{
	  Element EleOrder= (Element)eleShipmentLine.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
	//Element EleOrder = SCXmlUtil.createChild(eleShipmentLine,AcademyConstants.ELE_ORDER );
	EleOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO,strOrderNo );
	eleShipmentLine.removeAttribute(AcademyConstants.ATTR_ORDER_NO);
	}

}
		
		Element eStatusList = SCXmlUtil.getChildElement(apiInput, "StatusList");
		if (!SCUtil.isVoid(eStatusList)) {
			Element complexQuery = SCXmlUtil.createChild(apiInput, "ComplexQuery");
			int noOfStatus = SCXmlUtil.getChildren(eStatusList, "Status").size();
			Element expElement = null;
			Element orElement = null;
			for (Element statusElem : SCXmlUtil.getChildren(eStatusList, "Status")) {
				String value = statusElem.getChildNodes().item(0).getNodeValue();
				if (noOfStatus == 1) {
					expElement = SCXmlUtil.createChild(complexQuery, "Exp");
				} else {
					if (SCUtil.isVoid(orElement)) {
						orElement = SCXmlUtil.createChild(complexQuery, "Or");
					}
					expElement = SCXmlUtil.createChild(orElement, "Exp");
				}
				expElement.setAttribute("Name", "Status");
				expElement.setAttribute("Value", value);
			}
			apiInput.removeChild(eStatusList);
		}

		Element eleShipment = SCXmlUtil.getChildElement(apiInput, "StatusList");

		return eInput;
	}

	public void massageAPIOutput(Element apiOutput, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext) {
		super.massageAPIOutput(apiOutput, mashupMetaData, uiContext);
		List<Element> shipments = SCXmlUtil.getChildrenList(apiOutput);

		boolean pick = false;
		boolean shipInPick = false;
		Element getCommonCodeListOutput;
		long currentTimeMillis;
		if ((shipments != null) && (shipments.size() > 0)) {
			getCommonCodeListOutput = null;
			currentTimeMillis = System.currentTimeMillis();
			String userLocale = uiContext.getUserPreferences().getLocale().getLocaleCode();
			for (Element shipment : shipments) {
				String sStatus = shipment.getAttribute("Status");
				String sDeliveryMethod = shipment.getAttribute("DeliveryMethod");
				
				String assignedToUserID = shipment.getAttribute("AssignedToUserId");
				String shipNodeExtn = shipment.getAttribute("ShipNode");
				if(!SCUIUtils.isVoid(assignedToUserID))
				{
					Element getUserListOutput = null;
					Element getUserListInput = SCXmlUtil.createDocument("User").getDocumentElement();
					getUserListInput.setAttribute("DisplayUserID", assignedToUserID);
					getUserListInput.setAttribute("OrganizationKey", shipNodeExtn);
					getUserListOutput = (Element)SCUIMashupHelper.invokeMashup("extn_getUserListByUserID", getUserListInput, uiContext);
					List<Element> userList = SCXmlUtil.getChildrenList(getUserListOutput);
					for (Element user : userList) {
						String userName = user.getAttribute("Username");
						shipment.setAttribute("AssignedToUserId", userName);
					}
				}
				
				if ((!SCUIUtils.isVoid(sDeliveryMethod)) && (SCUIUtils.equals("PICK", sDeliveryMethod))) {
					if (!pick) {
						this.timeThresholdCodeListForPick = getTimingThresholdDataByShipmentType(uiContext, "PICK");
						pick = true;
					}
					getCommonCodeListOutput = this.timeThresholdCodeListForPick;
				} else if ((!SCUIUtils.isVoid(sDeliveryMethod)) && (SCUIUtils.equals("SHP", sDeliveryMethod))) {
					if (!shipInPick) {
						this.timeThresholdCodeListForShip = getTimingThresholdDataByShipmentType(uiContext, "SHP");
						shipInPick = true;
					}
					getCommonCodeListOutput = this.timeThresholdCodeListForShip;
				}
				if (SCUIUtils.equals("PICK", sDeliveryMethod)) {
					Element additionalDates = SCXmlUtil.getChildElement(shipment, "AdditionalDates");
					NodeList additionalDateList = additionalDates.getElementsByTagName("AdditionalDate");
					if (!SCUIUtils.isVoid(additionalDateList)) {
						for (int i = 0; i < additionalDateList.getLength(); i++) {
							Element additionalDate = (Element) additionalDateList.item(i);
							String dateType = additionalDate.getAttribute("DateTypeId");
							if (SCUIUtils.equals(ACADEMY_BOPIS_SLA_DATE, dateType)) {
								String expectedDate = additionalDate.getAttribute("ExpectedDate");
								if (!SCUIUtils.isVoid(expectedDate)) {
									int minutesRemaining = WSCMashupUtils
											.calculateRemainingMinutes(Long.valueOf(currentTimeMillis), expectedDate);
									String formattedDueInTime = WSCMashupUtils.getFormattedTime(minutesRemaining,
											uiContext);

									if (formattedDueInTime.equals("Overdue")) {
										formattedDueInTime = SCUILocalizationHelper.getString(uiContext, "Overdue");
										shipment.setAttribute("IsOverdue", "true");
										shipment.setAttribute("ImageAltText", formattedDueInTime);
										shipment.setAttribute("ImageUrl",
												"wsc/resources/css/icons/images/timeOverdue.png");
									} else {
										shipment.setAttribute("IsOverdue", "false");

										String[] imageAttributes = getImageAttributesArrayNew(getCommonCodeListOutput,
												minutesRemaining);
										shipment.setAttribute("ImageAltText", imageAttributes[0]);
										shipment.setAttribute("ImageUrl", imageAttributes[1]);
									}
									shipment.setAttribute("TimeRemaining", formattedDueInTime);
								}
							}
						}
					}
				}
			}
		}
	}

	private Element getTimingThresholdDataByShipmentType(SCUIContext uiContext, String deliveryMethod) {
		String codeType = null;

		switch (deliveryMethod) {
		case "PICK":
			codeType = "YCD_TIME_RMN_THRSH";
			break;

		case "SHP":
			codeType = "YCD_TIME_RMN_SFS";
		}

		Element commonCodeElement = SCXmlUtil.createDocument("CommonCode").getDocumentElement();
		commonCodeElement.setAttribute("CodeType", codeType);
		try {
			return (Element) SCUIMashupHelper.invokeMashup("common_getCommonCodeListForDueInThresholds",
					commonCodeElement, uiContext);
		} catch (APIManager.XMLExceptionWrapper e) {
			throw e;
		}
	}

	private String[] getImageAttributesArrayNew(Element commonCodeList, int remainingMinutes) {
		String[] imageAttributes = new String[2];
		List<Element> commonCodes = SCXmlUtil.getChildrenList(commonCodeList);
		if ((commonCodes != null) && (commonCodes.size() > 0)) {
			for (Element commonCode : commonCodes) {
				long codeValue = Long.valueOf(commonCode.getAttribute("CodeValue")).longValue();
				if (remainingMinutes <= codeValue) {
					imageAttributes[0] = commonCode.getAttribute("CodeShortDescription");
					imageAttributes[1] = commonCode.getAttribute("CodeLongDescription");
					break;
				}
			}
		}
		return imageAttributes;
	}

}