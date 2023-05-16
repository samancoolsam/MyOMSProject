package com.academy.wsc.extn.mashups;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.wsc.extn.common.constants.AcademyWSCConstants;
import com.ibm.wsc.mashups.utils.WSCMashupUtils;
import com.ibm.wsc.shipment.details.WSCShipmentSummaryCustomMashup;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.ui.web.framework.context.SCUIContext;
import com.sterlingcommerce.ui.web.framework.helpers.SCUILocalizationHelper;
import com.sterlingcommerce.ui.web.framework.mashup.SCUIMashupMetaData;
import com.sterlingcommerce.ui.web.framework.utils.SCUIUtils;

public class AcademyBOPISWSCShipmentSummaryCustomMashup extends WSCShipmentSummaryCustomMashup implements AcademyWSCConstants
{
	Element timeThresholdCodeListForPick = null;
	Element timeThresholdCodeListForShip = null; 
	public AcademyBOPISWSCShipmentSummaryCustomMashup() {}
	
	public Element massageOutput(Element outEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext) 
	{
		Element newOutEl = super.massageOutput(outEl, mashupMetaData, uiContext);
		Element eShipmentLines = SCXmlUtil.getChildElement(newOutEl, "ShipmentLines");
	    List<Element> shipmentLines = SCXmlUtil.getChildrenList(eShipmentLines);
	    String sDeliveryMethod = SCXmlUtil.getAttribute(newOutEl, "DeliveryMethod");
	    long currentTimeMillis = System.currentTimeMillis();
	    // get the commonCodeSLAListOutput value from super class
	    Element commonCodeSLAListOutput = null;
	    Element commonCodeInputElement = SCXmlUtil.createDocument("CommonCode").getDocumentElement();
	    boolean pick = false;boolean shipInPick = false;
	    if ((!SCUIUtils.isVoid(sDeliveryMethod)) && (SCUIUtils.equals("PICK", sDeliveryMethod)))
	    {
	      if (!pick)
	      {


	        commonCodeInputElement.setAttribute("CodeType", "YCD_TIME_RMN_THRSH");
	        timeThresholdCodeListForPick = WSCMashupUtils.getCommonCodeSLAListByCodeType(uiContext, commonCodeInputElement);
	        pick = true;
	      }
	      
	      commonCodeSLAListOutput = timeThresholdCodeListForPick;
	    }
	    else if ((!SCUIUtils.isVoid(sDeliveryMethod)) && (SCUIUtils.equals("SHP", sDeliveryMethod)))
	    {
	      if (!shipInPick) {
	        commonCodeInputElement.setAttribute("CodeType", "YCD_TIME_RMN_SFS");
	        timeThresholdCodeListForShip = WSCMashupUtils.getCommonCodeSLAListByCodeType(uiContext, commonCodeInputElement);
	        shipInPick = true;
	      }
	      
	      commonCodeSLAListOutput = timeThresholdCodeListForShip;
	    }
	    if (SCUIUtils.equals("PICK", sDeliveryMethod)) {
	        Element additionalDates = SCXmlUtil.getChildElement(outEl, "AdditionalDates");
	        NodeList additionalDateList = additionalDates.getElementsByTagName("AdditionalDate");
	        if (!SCUIUtils.isVoid(additionalDateList)) {
		        for (int i = 0; i < additionalDateList.getLength(); i++) {
		        	Element additionalDate = (Element) additionalDateList.item(i);
		        	String dateType = additionalDate.getAttribute("DateTypeId");
		        	if (SCUIUtils.equals(ACADEMY_BOPIS_SLA_DATE, dateType)) {
			        	String expectedDate = additionalDate.getAttribute("ExpectedDate");
			        	if (!SCUIUtils.isVoid(expectedDate)) {
			      	      int minutesRemaining = WSCMashupUtils.calculateRemainingMinutes(Long.valueOf(currentTimeMillis), expectedDate);
			      	      String formattedDueInTime = WSCMashupUtils.getFormattedTime(minutesRemaining, uiContext);
			      	      
			      	      if (formattedDueInTime.equals("Overdue")) {
			      	        formattedDueInTime = SCUILocalizationHelper.getString(uiContext, "Overdue");
			      	        outEl.setAttribute("IsOverdue", "true");
			      	        outEl.setAttribute("ImageAltText", formattedDueInTime);
			      	        outEl.setAttribute("ImageUrl", "wsc/resources/css/icons/images/timeOverdue.png");
			      	      }
			      	      else {
			      	        outEl.setAttribute("IsOverdue", "false");
		
			      	        String[] imageAttributes = WSCMashupUtils.getSLAImageAttributesArray(commonCodeSLAListOutput, minutesRemaining);
			      	        outEl.setAttribute("ImageAltText", imageAttributes[0]);
			      	        outEl.setAttribute("ImageUrl", imageAttributes[1]);
			      	      }
			      	      outEl.setAttribute("TimeRemaining", formattedDueInTime);
			      	    }
		        	}
		        }
	        }
        }
	    
		return newOutEl;
	}
}
