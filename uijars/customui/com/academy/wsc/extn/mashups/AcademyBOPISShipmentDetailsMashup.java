package com.academy.wsc.extn.mashups;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.wsc.extn.common.constants.AcademyWSCConstants;
import com.ibm.wsc.mashups.utils.WSCMashupUtils;
import com.ibm.wsc.shipment.backroompick.mashups.ShipmentDetailsMashup;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.ui.web.framework.context.SCUIContext;
import com.sterlingcommerce.ui.web.framework.helpers.SCUILocalizationHelper;
import com.sterlingcommerce.ui.web.framework.helpers.SCUIMashupHelper;
import com.sterlingcommerce.ui.web.framework.mashup.SCUIMashupMetaData;
import com.sterlingcommerce.ui.web.framework.utils.SCUIUtils;
import com.yantra.yfc.ui.backend.util.APIManager;

public class AcademyBOPISShipmentDetailsMashup extends ShipmentDetailsMashup implements AcademyWSCConstants
{
	Element timeThresholdCodeListForShip = null; Element timeThresholdCodeListForPick = null;
	
	public AcademyBOPISShipmentDetailsMashup() {}
	
	public Element massageOutput(Element outEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext)
	{
	    Element eOutput = super.massageOutput(outEl, mashupMetaData, uiContext);
	    long currentTimeMillis = System.currentTimeMillis();
	    Element getCommonCodeListOutput = null;
	    String sDeliveryMethod = eOutput.getAttribute("DeliveryMethod");
	    boolean pick = false;boolean shipInPick = false;
	    if ((!SCUIUtils.isVoid(sDeliveryMethod)) && (SCUIUtils.equals("PICK", sDeliveryMethod)))
	    {
	      if (!pick) {
	        timeThresholdCodeListForPick = getTimingThresholdDataByShipmentTypeNew(uiContext, "PICK");
	        pick = true;
	      }
	      
	      getCommonCodeListOutput = timeThresholdCodeListForPick;
	    }
	    else if ((!SCUIUtils.isVoid(sDeliveryMethod)) && (SCUIUtils.equals("SHP", sDeliveryMethod)))
	    {
	      if (!shipInPick) {
	        timeThresholdCodeListForShip = getTimingThresholdDataByShipmentTypeNew(uiContext, "SHP");
	        shipInPick = true;
	      }
	      
	      getCommonCodeListOutput = timeThresholdCodeListForShip;
	    }
	    
	    if (SCUIUtils.equals("PICK", sDeliveryMethod)) {
	        Element additionalDates = SCXmlUtil.getChildElement(eOutput, "AdditionalDates");
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
			    	          eOutput.setAttribute("IsOverdue", "true");
			    	          eOutput.setAttribute("ImageAltText", formattedDueInTime);
			    	          eOutput.setAttribute("ImageUrl", "wsc/resources/css/icons/images/timeOverdue.png");
			    	        }
			    	        else {
			    	          eOutput.setAttribute("IsOverdue", "false");
			    	          
		
			    	          String[] imageAttributes = getImageAttributesArrayNew(getCommonCodeListOutput, minutesRemaining);
			    	          eOutput.setAttribute("ImageAltText", imageAttributes[0]);
			    	          eOutput.setAttribute("ImageUrl", imageAttributes[1]);
			    	        }
			    	        eOutput.setAttribute("TimeRemaining", formattedDueInTime);
			    	      }
		        	}
		        }
	        }
        }
	    
	    return eOutput;
	}
	
	private String[] getImageAttributesArrayNew(Element commonCodeList, int remainingMinutes)
	  {
	    String[] imageAttributes = new String[2];
	    List<Element> commonCodes = SCXmlUtil.getChildrenList(commonCodeList);
	    if ((commonCodes != null) && (commonCodes.size() > 0)) {
	      for (Element commonCode : commonCodes)
	      {
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
	
	  private Element getTimingThresholdDataByShipmentTypeNew(SCUIContext uiContext, String deliveryMethod)
	  {
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
	    try
	    {
	      return (Element)SCUIMashupHelper.invokeMashup("common_getCommonCodeListForDueInThresholds", commonCodeElement, uiContext);
	    } catch (APIManager.XMLExceptionWrapper e) {
	      throw e;
	    }
	  }
}
