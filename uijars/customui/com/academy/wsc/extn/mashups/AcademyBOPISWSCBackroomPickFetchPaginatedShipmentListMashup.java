package com.academy.wsc.extn.mashups;

//import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.wsc.extn.common.constants.AcademyWSCConstants;
import com.ibm.wsc.mashups.utils.WSCMashupUtils;
import com.ibm.wsc.shipment.backroompick.mashups.WSCBackroomPickFetchPaginatedShipmentListMashup;
import com.sterlingcommerce.baseutil.SCUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.ui.web.framework.context.SCUIContext;
import com.sterlingcommerce.ui.web.framework.helpers.SCUILocalizationHelper;
import com.sterlingcommerce.ui.web.framework.helpers.SCUIMashupHelper;
import com.sterlingcommerce.ui.web.framework.mashup.SCUIMashupMetaData;
import com.sterlingcommerce.ui.web.framework.utils.SCUIUtils;
import com.yantra.yfc.ui.backend.util.APIManager;

public class AcademyBOPISWSCBackroomPickFetchPaginatedShipmentListMashup
		extends WSCBackroomPickFetchPaginatedShipmentListMashup implements AcademyWSCConstants {
	public AcademyBOPISWSCBackroomPickFetchPaginatedShipmentListMashup() {
	}

	Element timeThresholdCodeListForShip = null;
	Element timeThresholdCodeListForPick = null;
	// OMNI-54147-Curbside SLA UI ChangesCurbside SLA ... defined Element---Start
	Element timeThresholdCodeListForCurbside = null;
	Element timeThresholdCodeListForInstore = null;//OMNI-105548
	Element getSlaTimeForCurbside = null;
	Element getSlaTimeForInstore = null;//OMNI-105548

	// OMNI-54147-Curbside SLA UI ChangesCurbside SLA ...defined Element ---End
	public Element massageInput(Element inputEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext) {
		System.out.println("AcademyBOPISWSCBackroomPickFetchPaginatedShipmentListMashup : massageInput:");
		Element eInput = super.massageInput(inputEl, mashupMetaData, uiContext);
		Element eCommonCodeOutput;

		Element apiInput = SCXmlUtil.getFirstChildElement(
				SCXmlUtil.getChildElement(SCXmlUtil.getChildElement(eInput, "API", true), "Input", true));
		System.out.println("AcademyBOPISWSCBackroomPickFetchPaginatedShipmentListMashup : massageInput: apiInput:"
				+ SCXmlUtil.getString(apiInput));
		String strShipmentNo = apiInput.getAttribute("ShipmentNo");
		// OMNI-6624 - Mobile Store UI - New Search screen customization for STS - START
		String strStatus = apiInput.getAttribute("Status");
		String strSearchType = apiInput.getAttribute("SearchType");
		// OMNI-6624 - Mobile Store UI - New Search screen customization for STS - END
		String strOrderNo = SCXmlUtil.getXpathAttribute(apiInput, "ShipmentLines/ShipmentLine/Order/@OrderNo");
		if ((!SCUtil.isVoid(strShipmentNo) || !SCUtil.isVoid(strOrderNo))) {
			System.out.println(
					"AcademyBOPISWSCBackroomPickFetchPaginatedShipmentListMashup : massageInput: OrderNo or ShipmentNo is entered");
			System.out.println(
					"AcademyBOPISWSCBackroomPickFetchPaginatedShipmentListMashup : massageInput: setting Status=''");
			// OMNI-6624 - Mobile Store UI - New Search screen customization for STS - START
			if ((!SCUtil.isVoid(strSearchType)) && (!SCUIUtils.equals(strSearchType, "STSOrderSearch"))) {
				apiInput.setAttribute("Status", "");
			}
			// OMNI-6624 - Mobile Store UI - New Search screen customization for STS - END
			return eInput;
		}

		// OMNI-6624 - Mobile Store UI - New Search screen customization for STS - START
		if ((!SCUtil.isVoid(strSearchType)) && SCUIUtils.equals(strSearchType, "STSOrderSearch")) {
			String strContainerNo = SCXmlUtil.getXpathAttribute(apiInput, "Containers/Container/@ContainerNo");
			System.out.println("ContainerNo " + strContainerNo);
			if (!SCUtil.isVoid(strContainerNo)) {
				apiInput.setAttribute("DocumentType", "0006");
				System.out.println("Updating Status to blank");
				apiInput.setAttribute("Status", "");
				return eInput;
				// eInput.removeAttribute("DocumentType");
			}
			System.out.println(
					"AcademyBOPISWSCBackroomPickFetchPaginatedShipmentListMashup : massageInput: STSSearchapiInput:"
							+ SCXmlUtil.getString(eInput));
		}
		// OMNI-6624 - Mobile Store UI - New Search screen customization for STS - END

		Element eComplexQuery = SCXmlUtil.getChildElement(apiInput, "ComplexQuery");
		if (SCUtil.isVoid(strStatus) && SCUtil.isVoid(eComplexQuery)) {
			System.out.println(
					"AcademyBOPISWSCBackroomPickFetchPaginatedShipmentListMashup : massageInput: Calling Method getDefaultStatusForMobileHomeSearch()");
			// OMNI-6624 - Mobile Store UI - New Search screen customization for STS - START
			if ((!SCUtil.isVoid(strSearchType)) && SCUIUtils.equals(strSearchType, "STSOrderSearch")) {
				eCommonCodeOutput = getDefaultStatusForSTSOrderSearch(uiContext);
			} else {
				// OMNI-6624 - Mobile Store UI - New Search screen customization for STS - END
				eCommonCodeOutput = getDefaultStatusForMobileHomeSearch(uiContext);
				// OMNI-6624 - Mobile Store UI - New Search screen customization for STS - START
			}
			// OMNI-6624 - Mobile Store UI - New Search screen customization for STS - END
			Element complexQuery = SCXmlUtil.createChild(apiInput, "ComplexQuery");
			int noOfStatus = SCXmlUtil.getChildren(eCommonCodeOutput, "CommonCode").size();
			Element expElement = null;
			Element orElement = null;
			for (Element commonCodeElem : SCXmlUtil.getChildren(eCommonCodeOutput, "CommonCode")) {
				String value = commonCodeElem.getAttribute("CodeShortDescription");
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

		}
		System.out.println("AcademyBOPISWSCBackroomPickFetchPaginatedShipmentListMashup : massageInput: output"
				+ SCXmlUtil.getString(eInput));
		return eInput;
	}

	private Element getDefaultStatusForMobileHomeSearch(SCUIContext uiContext) {
		String codeType = "ASO_MOB_SRCH_STATUS";

		Element commonCodeElement = SCXmlUtil.createDocument("CommonCode").getDocumentElement();
		commonCodeElement.setAttribute("CodeType", codeType);
		try {
			return (Element) SCUIMashupHelper.invokeMashup("mobileHomeSearch_getDefaultStatuses", commonCodeElement,
					uiContext);
		} catch (APIManager.XMLExceptionWrapper e) {
			throw e;
		}
	}

	// OMNI-6624 - Mobile Store UI - New Search screen customization for STS - START
	private Element getDefaultStatusForSTSOrderSearch(SCUIContext uiContext) {
		String codeType = "ASO_STS_SRCH_STATUS";

		Element commonCodeElement = SCXmlUtil.createDocument("CommonCode").getDocumentElement();
		commonCodeElement.setAttribute("CodeType", codeType);
		try {
			return (Element) SCUIMashupHelper.invokeMashup("mobileHomeSearch_getDefaultStatuses", commonCodeElement,
					uiContext);
		} catch (APIManager.XMLExceptionWrapper e) {
			throw e;
		}
	}

	// OMNI-6624 - Mobile Store UI - New Search screen customization for STS - END
	public void massageAPIOutput(Element apiOutput, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext) {
		super.massageAPIOutput(apiOutput, mashupMetaData, uiContext);
		List<Element> shipments = SCXmlUtil.getChildrenList(apiOutput);

		boolean pick = false;
		boolean shipInPick = false;
		Element getCommonCodeListOutput;
		Element getCommonCodeListOutputNew;
		long currentTimeMillis;
	
		if ((shipments != null) && (shipments.size() > 0)) {
			getCommonCodeListOutput = null;
			getCommonCodeListOutputNew = null;
			currentTimeMillis = System.currentTimeMillis();
			String userLocale = uiContext.getUserPreferences().getLocale().getLocaleCode();

			for (Element shipment : shipments) {

				Element shipmentLines = SCXmlUtil.getChildElement(shipment, "ShipmentLines");
				if (!SCUIUtils.isVoid(shipmentLines)) {
					String numOfProducts = shipmentLines.getAttribute("TotalNumberOfRecords");
					shipment.setAttribute("TotalLines", numOfProducts);
				}
				String sDeliveryMethod = shipment.getAttribute("DeliveryMethod");
				// OMNI-54147-Curbside SLA UI ChangesCurbside SLA...getting Required
				// Attributes---Start
				String strCurbsideEnable = null;
				String sCurbsideDate = null;
				String sInstorePickupEnabled = null;//OMNI-105548
				
				Element sExtn = SCXmlUtil.getChildElement(shipment, "Extn");
				if (!SCUIUtils.isVoid(sExtn)) {
					strCurbsideEnable = sExtn.getAttribute("ExtnIsCurbsidePickupOpted");
					sCurbsideDate = shipment.getAttribute("AppointmentNo");
					sInstorePickupEnabled = sExtn.getAttribute("ExtnIsInstorePickupOpted");//OMNI-105548
				}
				String hyp = "-";
				String colon = ":";
				if ((!SCUIUtils.isVoid(sDeliveryMethod)) && (SCUIUtils.equals("PICK", sDeliveryMethod))) {
					if ((!SCUtil.isVoid(strCurbsideEnable)) && SCUIUtils.equals(strCurbsideEnable, "Y")) {
						timeThresholdCodeListForCurbside = timeThresholdCodeListForCurbside(uiContext, "Y");
						getSlaTimeForCurbside = getSlaTimeForCurbsideMethod(uiContext, "Y");

						getCommonCodeListOutput = timeThresholdCodeListForCurbside;
						getCommonCodeListOutputNew = getSlaTimeForCurbside;
					}
					//OMNI-105548 - Start
					else if ((!SCUtil.isVoid(sInstorePickupEnabled)) && SCUIUtils.equals(sInstorePickupEnabled, "Y")) {
						timeThresholdCodeListForInstore = timeThresholdCodeListForInstore(uiContext, "Y");
						getSlaTimeForInstore = getSlaTimeForInstoreMethod(uiContext, "Y");

						getCommonCodeListOutput = timeThresholdCodeListForInstore;
						getCommonCodeListOutputNew = getSlaTimeForInstore;
					}
					//OMNI-105548 - End
					if (SCUtil.isVoid(strCurbsideEnable) && SCUtil.isVoid(sInstorePickupEnabled)) {
						// OMNI-54147-Curbside SLA UI ChangesCurbside SLA...getting Required
						// Attributes---End
						if (!pick) {
							timeThresholdCodeListForPick = getTimingThresholdDataByShipmentTypeNew(uiContext, "PICK");
							pick = true;
						}

						getCommonCodeListOutput = timeThresholdCodeListForPick;
					}
				} else if ((!SCUIUtils.isVoid(sDeliveryMethod)) && (SCUIUtils.equals("SHP", sDeliveryMethod))) {
					if (!shipInPick) {
						timeThresholdCodeListForShip = getTimingThresholdDataByShipmentTypeNew(uiContext, "SHP");
						shipInPick = true;
					}

					getCommonCodeListOutput = timeThresholdCodeListForShip;
				}

				if (SCUIUtils.equals("PICK", sDeliveryMethod)) {
					if (SCUIUtils.isVoid(strCurbsideEnable)) {
						Element additionalDates = SCXmlUtil.getChildElement(shipment, "AdditionalDates");
						if (!SCUIUtils.isVoid(additionalDates)) {
							NodeList additionalDateList = additionalDates.getElementsByTagName("AdditionalDate");
							if (!SCUIUtils.isVoid(additionalDateList)) {
								for (int i = 0; i < additionalDateList.getLength(); i++) {
									Element additionalDate = (Element) additionalDateList.item(i);
									String dateType = additionalDate.getAttribute("DateTypeId");
									if (SCUIUtils.equals(ACADEMY_BOPIS_SLA_DATE, dateType)) {
										String expectedDate = additionalDate.getAttribute("ExpectedDate");
										System.out.println("Current Time in MilliSeconds : " + currentTimeMillis);
										System.out.println("Expected Date For RFBP SLA TIME : " + expectedDate);
										if (!SCUIUtils.isVoid(expectedDate)) {
											int minutesRemaining = WSCMashupUtils.calculateRemainingMinutes(
													Long.valueOf(currentTimeMillis), expectedDate);
											System.out
													.println(" Minutes Remaining Method OutPut :: " + minutesRemaining);
											String formattedDueInTime = WSCMashupUtils
													.getFormattedTime(minutesRemaining, uiContext);
											
												
											if (formattedDueInTime.equals("Overdue")) {
												
												formattedDueInTime = SCUILocalizationHelper.getString(uiContext,
														"Overdue");
												shipment.setAttribute("IsOverdue", "true");
												shipment.setAttribute("ImageAltText", formattedDueInTime);
												shipment.setAttribute("ImageUrl",
														"wsc/resources/css/icons/images/timeOverdue.png");
														
											} else {
												shipment.setAttribute("IsOverdue", "false");

												String[] imageAttributes = getImageAttributesArrayNew(
														getCommonCodeListOutput, minutesRemaining);
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

					// OMNI-54147-Curbside SLA UI ChangesCurbside SLA
					if (!SCUIUtils.isVoid(strCurbsideEnable) || !SCUIUtils.isVoid(sInstorePickupEnabled)) {
						if (SCUIUtils.equals("Y", strCurbsideEnable) || SCUIUtils.equals("Y", sInstorePickupEnabled)) {
							String year = sCurbsideDate.substring(0, 4);
							String mnt = sCurbsideDate.substring(4, 6);
							String date = sCurbsideDate.substring(6, 8);
							String hr = sCurbsideDate.substring(8, 10);
							String min = sCurbsideDate.substring(10, 12);
							String sec = sCurbsideDate.substring(12, 14);
							String ExpectedDateFormat = year + hyp + mnt + hyp + date + " " + hr + colon + min + colon
									+ sec;
							long sCurbActual = LocalDateTime.parse(ExpectedDateFormat.replace(" ", "T"))
									.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
							String dueTimeMinutes = getDueTimeInMinutes(getCommonCodeListOutputNew);
							int dueTime = Integer.parseInt(dueTimeMinutes);
							int CIMS = 60000;
							int dueTimeSec = dueTime * CIMS;
							long dueTimeMs = dueTimeSec;
							long sCurbExpected = dueTimeMs + sCurbActual;
							System.out.println("Due Time in MS : " + sCurbExpected);
							System.out.println(" Current Time in Milliseconds : " + currentTimeMillis);
							int minutesRemainings = calculateRemainingMinutesForCurbside(
									Long.valueOf(currentTimeMillis), Long.valueOf(sCurbExpected));
							String formattedDueInTime = WSCMashupUtils.getFormattedTime(minutesRemainings, uiContext);
							System.out.println("Remaining Minutes  : " + minutesRemainings);
							if (formattedDueInTime.equals("Overdue")) {
								
								
								formattedDueInTime = SCUILocalizationHelper.getString(uiContext, "Overdue");
								shipment.setAttribute("IsOverdue", "true");
								shipment.setAttribute("ImageAltText", formattedDueInTime);
								shipment.setAttribute("ImageUrl", "wsc/resources/css/icons/images/timeOverdue.png");
								
							} else {
								shipment.setAttribute("IsOverdue", "false");

								String[] imageAttributes = getImageAttributesArrayNew(getCommonCodeListOutput,
										minutesRemainings);
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

	private Element getTimingThresholdDataByShipmentTypeNew(SCUIContext uiContext, String deliveryMethod) {
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

	// OMNI-54147-Curbside SLA UI Changes ...Methods ---Start
	// Method to get a commoncode elements with the Code Type
	private Element timeThresholdCodeListForCurbside(SCUIContext uiContext, String strCurbsideEnable) {
		String codeType = null;
		if (strCurbsideEnable.equalsIgnoreCase("Y")) {

			codeType = "TIME_RMN_CURBSIDE";
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
	
	//OMNI-105548 - Start
	private Element timeThresholdCodeListForInstore(SCUIContext uiContext, String strInstoreEnable) {
		String codeType = null;
		if (strInstoreEnable.equalsIgnoreCase("Y")) {

			codeType = "TIME_RMN_INSTORE";
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
	//OMNI-105548 - End

	// Method to get a commoncode elements with the Code Type
	private Element getSlaTimeForCurbsideMethod(SCUIContext uiContext, String strCurbsideEnable) {
		String codeType = null;
		if (strCurbsideEnable.equalsIgnoreCase("Y")) {

			codeType = "SLA_TIME_CURBSIDE";
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
	//OMNI-105548 - Start
	private Element getSlaTimeForInstoreMethod(SCUIContext uiContext, String strInstoreEnable) {
		String codeType = null;
		if (strInstoreEnable.equalsIgnoreCase("Y")) {

			codeType = "SLA_TIME_INSTORE";
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
	//OMNI-105548 - End
	// Method to get a String for SLA Time of Curbside from a commoncode
	private String getDueTimeInMinutes(Element commonCodeList) {
		String dueTimeInMinutes = (String) null;
		List<Element> commonCodes = SCXmlUtil.getChildrenList(commonCodeList);
		for (Element commonCode : commonCodes) {
			long codeValue = Long.valueOf(commonCode.getAttribute("CodeValue")).longValue();
			if (codeValue > 0) {
				dueTimeInMinutes = commonCode.getAttribute("CodeShortDescription");
				break;
			}
		}
		return dueTimeInMinutes;
	}

//calculate remaining minutes for curbside
	private int calculateRemainingMinutesForCurbside(Long currentTimeMillis, Long sCurbExpected) {
		long millisDifference = sCurbExpected.longValue() - currentTimeMillis.longValue();
		long minutesDifference = millisDifference / 60000L;

		return (int) minutesDifference;
	}

	// OMNI-54147-Curbside SLA UI ChangesCurbside SLA ...Methods---End

	private static final String READY_FOR_BACKROOM_PICK_STATUS = "1100.70.06.10";

	private static final String SHIP_DELIVERY_METHOD = "SHP";

	private static final String PICK_DELIVERY_METHOD = "PICK";

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
