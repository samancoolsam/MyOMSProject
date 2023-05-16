package com.academy.wsc.extn.mashups.shipment.batchpick;

import com.ibm.isccs.mashups.utils.SCCSMashupUtils;
import com.ibm.wsc.common.mashups.WSCBaseMashup;
import com.ibm.wsc.mashups.utils.WSCMashupUtils;
import com.sterlingcommerce.baseutil.SCUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.framework.utils.SCXmlUtils;
import com.sterlingcommerce.ui.web.framework.context.SCUIContext;
import com.sterlingcommerce.ui.web.framework.helpers.SCUIAuthorizationHelper;
import com.sterlingcommerce.ui.web.framework.helpers.SCUILocalizationHelper;
import com.sterlingcommerce.ui.web.framework.helpers.SCUIMashupHelper;
import com.sterlingcommerce.ui.web.framework.mashup.SCUIMashupMetaData;
import com.sterlingcommerce.ui.web.framework.utils.SCUIJSONUtils;
import com.sterlingcommerce.ui.web.framework.utils.SCUIUtils;
import com.yantra.yfc.ui.backend.util.APIManager;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfc.util.YFCDateUtils;
import java.io.InputStream;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class AcademyWSCSortBatchListOnTimeRemaining extends WSCBaseMashup{
	
	Element timeThresholdCodeListForShip = null;
	  Element timeThresholdCodeListForPick = null;
	  private static final String NOT_STARTED = "NotStarted";
	  private static final String IN_PROGRESS = "InProgress";
	  private static final String PICKED = "Picked";
	  private static final String SHIP_DELIVERY_METHOD = "SHP";
	  private static final String PICK_DELIVERY_METHOD = "PICK";
	  
	  public Element massageInput(Element inputEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext)
	  {
	    if (SCUIUtils.isVoid(inputEl)) {
	      inputEl = SCXmlUtil.createDocument("StoreBatch").getDocumentElement();
	    }
	    boolean backroomPickupPick = SCUIAuthorizationHelper.hasPermission(uiContext, "WSC000006");
	    
	    boolean backroomPickupShip = SCUIAuthorizationHelper.hasPermission(uiContext, "WSC000017");
	    if (!SCUIUtils.isVoid(inputEl))
	    {
	      Element elemShipmentLine = SCXmlUtil.getChildElement(inputEl, "ShipmentLine", true);
	      
	      Element elemShipment = SCXmlUtil.getChildElement(elemShipmentLine, "Shipment", true);
	      if ((backroomPickupPick) && (!backroomPickupShip)) {
	        elemShipment.setAttribute("DeliveryMethod", "PICK");
	      } else if ((!backroomPickupPick) && (backroomPickupShip)) {
	        elemShipment.setAttribute("DeliveryMethod", "SHP");
	      }
	    }
	    setFilterOptionInUIContext(inputEl, uiContext);
	    setInputBasedOnFilterOption(inputEl, uiContext);
	    setInputBasedOnDeliveryMethodMixRule(inputEl, uiContext);
	    Element behaviorInputEl = null;
	    int pageSize = getPageSize(uiContext);
	    int pageNum = getPageNumber(uiContext);
	    Element prevpageRecord = getPreviousPageRecord(uiContext);
	    SCCSMashupUtils.addAttributeToUIContext(uiContext, "pageSize", this, new Integer(pageSize));
	    
	    SCCSMashupUtils.addAttributeToUIContext(uiContext, "pageNum", this, new Integer(pageNum));
	    if (SCUIUtils.isVoid(inputEl))
	    {
	      inputEl = SCXmlUtil.createDocument("Page").getDocumentElement();
	      inputEl.setAttribute("PageNumber", String.valueOf(pageNum));
	      inputEl.setAttribute("PageSize", String.valueOf(pageSize));
	      if (!SCUIUtils.isVoid(prevpageRecord))
	      {
	        Element prevPageEle = SCXmlUtils.createChild(inputEl, "PreviousPage");
	        
	        prevPageEle.setAttribute("PageNumber", String.valueOf(pageNum - 1));
	        
	        SCXmlUtils.importElement(prevPageEle, prevpageRecord);
	      }
	    }
	    else
	    {
	      behaviorInputEl = SCXmlUtil.createDocument("Page").getDocumentElement();
	      behaviorInputEl.setAttribute("PageNumber", String.valueOf(pageNum));
	      behaviorInputEl.setAttribute("PageSize", String.valueOf(pageSize));
	      if (!SCUIUtils.isVoid(prevpageRecord))
	      {
	        Element prevPageEle = SCXmlUtils.createChild(behaviorInputEl, "PreviousPage");
	        
	        prevPageEle.setAttribute("PageNumber", String.valueOf(pageNum - 1));
	        
	        SCXmlUtils.importElement(prevPageEle, prevpageRecord);
	      }
	      Element apiElem = SCXmlUtil.getChildElement(behaviorInputEl, "API", true);
	      Element inputElem = SCXmlUtil.getChildElement(apiElem, "Input", true);
	      SCXmlUtils.importElement(inputElem, inputEl);
	      return behaviorInputEl;
	    }
	    return inputEl;
	  }
	  
	  /* The below method has been modified to remove the rule check condition and also add LOS at ShipmentLine */
	  private void setInputBasedOnDeliveryMethodMixRule(Element inputEl, SCUIContext uiContext)
	  {
	    Element configureBatchBy = SCXmlUtil.getChildElement(inputEl, "ConfigureBatchBy", true);
		inputEl.removeChild(configureBatchBy);
		
		configureBatchBy = SCXmlUtil.createChild(inputEl, "ConfigureBatchBy");
		createCriteria(configureBatchBy, "Shipment", "EnterpriseCode");
		createCriteria(configureBatchBy, "Shipment", "CarrierServiceCode");
		createCriteria(configureBatchBy, "Shipment", "DocumentType");
		SCCSMashupUtils.addAttributeToUIContext(uiContext, "DelMethodMixRule", this, "N");
	      
	  }
	  
	  private void createCriteria(Element configureBatchBy, String entity, String name)
	  {
	    Element attr = SCXmlUtil.createChild(configureBatchBy, "Attribute");
	    attr.setAttribute("Entity", entity);
	    attr.setAttribute("Name", name);
	  }
	  
	  public Element massageOutput(Element apiOutput, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext)
	  {
	    Element pageOutputEle = SCXmlUtil.getChildElement(apiOutput, "Output");
	    Element storeBatchListEle = SCXmlUtil.getChildElement(pageOutputEle, "StoreBatchList");
	    
	    Element lastRecEle = SCXmlUtil.createChild(apiOutput, "LastRecord");
	    Element elemLastNode = null;
	    if (!SCUtil.isVoid(SCXmlUtil.getLastChildElement(storeBatchListEle)))
	    {
	      elemLastNode = (Element)SCXmlUtil.getLastChildElement(storeBatchListEle).cloneNode(true);
	      
	      SCXmlUtil.importElement(lastRecEle, elemLastNode);
	    }
	    List<Element> batches = SCXmlUtil.getChildrenList(storeBatchListEle);
	    
	    addFilterOptionsToBatchListOutput(storeBatchListEle, uiContext);
	    
	    boolean pick = false;boolean shipInPick = false;
	    Element getCommonCodeListOutput;
	    long currentTimeMillis;
	    if ((batches != null) && (batches.size() > 0))
	    {
	      getCommonCodeListOutput = null;
	      currentTimeMillis = System.currentTimeMillis();
	      for (Element batch : batches)
	      {
	        stampDeliveryMethodOnBatch(batch);
	        String sStatus = batch.getAttribute("Status");
	        if ((!SCUIUtils.isVoid(sStatus)) && (!SCUIUtils.equals(sStatus, "3000")) && (!SCUIUtils.equals(sStatus, "9000")))
	        {
	          String sDeliveryMethod = batch.getAttribute("ShipmentDeliveryMethod");
	          String batchDelMethod = SCXmlUtil.getChildElement(batch, "BatchDeliveryMethod").getAttribute("DeliveryMethod");
	          if ((!SCUIUtils.isVoid(sDeliveryMethod)) && (SCUIUtils.equals("PICK", sDeliveryMethod)))
	          {
	            if (!pick)
	            {
	              this.timeThresholdCodeListForPick = getTimingThresholdDataByShipmentType(uiContext, "PICK");
	              pick = true;
	            }
	            getCommonCodeListOutput = this.timeThresholdCodeListForPick;
	          }
	          else if ((!SCUIUtils.isVoid(sDeliveryMethod)) && (SCUIUtils.equals("SHP", sDeliveryMethod)))
	          {
	            if (!shipInPick)
	            {
	              this.timeThresholdCodeListForShip = getTimingThresholdDataByShipmentType(uiContext, "SHP");
	              shipInPick = true;
	            }
	            getCommonCodeListOutput = this.timeThresholdCodeListForShip;
	          }
	          String sExpectedShipmentDate = batch.getAttribute("OldestExpShpDate");
	          if (!SCUIUtils.isVoid(sExpectedShipmentDate))
	          {
	            int minutesRemaining = WSCMashupUtils.calculateRemainingMinutes(Long.valueOf(currentTimeMillis), sExpectedShipmentDate);
	            String formattedDueInTime = WSCMashupUtils.getFormattedTime(minutesRemaining, uiContext);
	            if (formattedDueInTime.equals("Overdue"))
	            {
	              formattedDueInTime = SCUILocalizationHelper.getString(uiContext, "Overdue");
	              batch.setAttribute("IsOverdue", "true");
	              batch.setAttribute("ImageAltText", formattedDueInTime);
	              if (SCUtil.isVoid(batchDelMethod)) {
	                batch.setAttribute("ImageUrl", "wsc/resources/css/icons/images/timeOverdue.png");
	              } else {
	                batch.setAttribute("ImageUrl", "wsc/resources/css/icons/images/timeOverdue_overlay.png");
	              }
	            }
	            else
	            {
	              batch.setAttribute("IsOverdue", "false");
	              
	              String[] imageAttributes = getImageAttributesArray(getCommonCodeListOutput, minutesRemaining);
	              batch.setAttribute("ImageAltText", imageAttributes[0]);
	              if (SCUtil.isVoid(batchDelMethod)) {
	                batch.setAttribute("ImageUrl", imageAttributes[1]);
	              } else {
	                batch.setAttribute("ImageUrl", appendOverlayToImagePath(uiContext, imageAttributes[1]));
	              }
	            }
	            batch.setAttribute("TimeRemaining", formattedDueInTime);
	          }
	        }
	      }
	    }
	    return apiOutput;
	  }
	  
	  private String appendOverlayToImagePath(SCUIContext uiContext, String imageURL)
	  {
	    if (SCUtil.isVoid(imageURL)) {
	      return "";
	    }
	    int fileExtIndex = imageURL.lastIndexOf(".");
	    if (fileExtIndex < 0) {
	      return imageURL;
	    }
	    String overLayImageURL = imageURL.substring(0, fileExtIndex) + "_overlay" + imageURL.substring(fileExtIndex);
	    InputStream inputStream = uiContext.getSession().getServletContext().getResourceAsStream(overLayImageURL);
	    if (SCUtil.isVoid(inputStream)) {
	      return imageURL;
	    }
	    return overLayImageURL;
	  }
	  
	  private void stampDeliveryMethodOnBatch(Element batch)
	  {
	    Element batchDelMethod = SCXmlUtil.createChild(batch, "BatchDeliveryMethod");
	    NodeList storeBatchConfigNL = batch.getElementsByTagName("StoreBatchConfig");
	    for (int i = 0; i < storeBatchConfigNL.getLength(); i++)
	    {
	      Element sbConfig = (Element)storeBatchConfigNL.item(i);
	      if ("DeliveryMethod".equals(sbConfig.getAttribute("Name")))
	      {
	        String delMethod = sbConfig.getAttribute("Value");
	        batchDelMethod.setAttribute("DeliveryMethod", delMethod);
	        SCXmlUtil.setAttribute(batchDelMethod, "DelMethodImageUrl", "wsc/resources/css/icons/images");
	        if (SCUIUtils.equals(delMethod, "SHP"))
	        {
	          SCXmlUtil.setAttribute(batchDelMethod, "DelMethodImageId", "shipping_med.png"); break;
	        }
	        if (!SCUIUtils.equals(delMethod, "PICK")) {
	          break;
	        }
	        SCXmlUtil.setAttribute(batchDelMethod, "DelMethodImageId", "pickup_med.png"); break;
	      }
	    }
	  }
	  
	  private int getPageNumber(SCUIContext uiContext)
	  {
	    int pageNumber = 1;
	    if (!SCUIUtils.isVoid(uiContext.getRequest().getParameter("scControllerData")))
	    {
	      Element elem = SCUIJSONUtils.getXmlFromJSON(uiContext.getRequest().getParameter("scControllerData")).getDocumentElement();
	      
	      Element elemMashupRefs = SCXmlUtil.getChildElement(elem, "MashupRefs");
	      for (Element elemMashupRef : SCXmlUtil.getChildren(elemMashupRefs, "MashupRef")) {
	        if (!SCUtil.isVoid(elemMashupRef.getAttribute("scPageNumber")))
	        {
	          pageNumber = SCXmlUtil.getIntAttribute(elemMashupRef, "scPageNumber");
	          
	          break;
	        }
	      }
	    }
	    return pageNumber;
	  }
	  
	  private void addFilterOptionsToBatchListOutput(Element outEl, SCUIContext uiContext)
	  {
	    Element filterOptionElement = SCXmlUtil.createChild(outEl, "FilterOptions");
	    
	    HttpSession session = uiContext.getSession();
	    SCXmlUtil.setAttribute(filterOptionElement, "Picked", (String)session.getAttribute("Picked"));
	    
	    SCXmlUtil.setAttribute(filterOptionElement, "InProgress", (String)session.getAttribute("InProgress"));
	    
	    SCXmlUtil.setAttribute(filterOptionElement, "NotStarted", (String)session.getAttribute("NotStarted"));
	  }
	  
	  private void setFilterOptionInUIContext(Element inputEl, SCUIContext uiContext)
	  {
	    HttpSession session = uiContext.getSession();
	    if (!SCUIUtils.isVoid(inputEl.getAttribute("Picked"))) {
	      session.setAttribute("Picked", inputEl.getAttribute("Picked"));
	    }
	    if (!SCUIUtils.isVoid(inputEl.getAttribute("InProgress"))) {
	      session.setAttribute("InProgress", inputEl.getAttribute("InProgress"));
	    }
	    if (!SCUIUtils.isVoid(inputEl.getAttribute("NotStarted"))) {
	      session.setAttribute("NotStarted", inputEl.getAttribute("NotStarted"));
	    }
	    if (SCUIUtils.isVoid(session.getAttribute("Picked"))) {
	      session.setAttribute("Picked", "N");
	    }
	    if (SCUIUtils.isVoid(session.getAttribute("InProgress"))) {
	      session.setAttribute("InProgress", "Y");
	    }
	    if (SCUIUtils.isVoid(session.getAttribute("NotStarted"))) {
	      session.setAttribute("NotStarted", "Y");
	    }
	    inputEl.setAttribute("Picked", (String)session.getAttribute("Picked"));
	    inputEl.setAttribute("InProgress", (String)session.getAttribute("InProgress"));
	    inputEl.setAttribute("NotStarted", (String)session.getAttribute("NotStarted"));
	  }
	  
	  private void setInputBasedOnFilterOption(Element inputEl, SCUIContext uiContext)
	  {
	    if (!SCUIUtils.isVoid(inputEl))
	    {
	      long currentTimeMillis = System.currentTimeMillis();
	      YFCDate currDate = new YFCDate(currentTimeMillis);
	      YFCDateUtils.addHours(currDate, -24);
	      String currDateTimeString = currDate.getString();
	      String sPicked = SCXmlUtil.getAttribute(inputEl, "Picked");
	      String sInProgress = SCXmlUtil.getAttribute(inputEl, "InProgress");
	      String sNotStarted = SCXmlUtil.getAttribute(inputEl, "NotStarted");
	      if ((SCUIUtils.equals(sPicked, "Y")) && (SCUIUtils.equals(sInProgress, "N")) && (SCUIUtils.equals(sNotStarted, "N")))
	      {
	        inputEl.setAttribute("GetNewBatches", "N");
	        inputEl.setAttribute("GetExistingBatches", "Y");
	        Element eComplexQuery = SCXmlUtil.createChild(inputEl, "ComplexQuery");
	        
	        Element andElement = SCXmlUtil.createChild(eComplexQuery, "And");
	        
	        Element eExpression = SCXmlUtil.createChild(andElement, "Exp");
	        eExpression.setAttribute("Name", "Modifyts");
	        SCXmlUtil.setAttribute(eExpression, "Value", currDateTimeString);
	        
	        eExpression.setAttribute("QryType", "GT");
	        Element orElement = SCXmlUtil.createChild(andElement, "Or");
	        Element eExpression1 = SCXmlUtil.createChild(orElement, "Exp");
	        eExpression1.setAttribute("Name", "Status");
	        eExpression1.setAttribute("Value", "3000");
	        Element eExpression2 = SCXmlUtil.createChild(orElement, "Exp");
	        eExpression2.setAttribute("Name", "Status");
	        eExpression2.setAttribute("Value", "9000");
	      }
	      if ((SCUIUtils.equals(sPicked, "N")) && (SCUIUtils.equals(sInProgress, "Y")) && (SCUIUtils.equals(sNotStarted, "N")))
	      {
	        inputEl.setAttribute("GetNewBatches", "N");
	        inputEl.setAttribute("GetExistingBatches", "Y");
	        Element eComplexQuery = SCXmlUtil.createChild(inputEl, "ComplexQuery");
	        
	        Element orElement = SCXmlUtil.createChild(eComplexQuery, "Or");
	        Element eExpression1 = SCXmlUtil.createChild(orElement, "Exp");
	        eExpression1.setAttribute("Name", "Status");
	        eExpression1.setAttribute("Value", "2000");
	        Element eExpression2 = SCXmlUtil.createChild(orElement, "Exp");
	        eExpression2.setAttribute("Name", "Status");
	        eExpression2.setAttribute("Value", "1100");
	      }
	      if ((SCUIUtils.equals(sPicked, "N")) && (SCUIUtils.equals(sInProgress, "N")) && (SCUIUtils.equals(sNotStarted, "Y")))
	      {
	        inputEl.setAttribute("GetNewBatches", "Y");
	        inputEl.setAttribute("GetExistingBatches", "N");
	      }
	      if ((SCUIUtils.equals(sPicked, "N")) && (SCUIUtils.equals(sInProgress, "Y")) && (SCUIUtils.equals(sNotStarted, "Y")))
	      {
	        inputEl.setAttribute("GetNewBatches", "Y");
	        inputEl.setAttribute("GetExistingBatches", "Y");
	        Element eComplexQuery = SCXmlUtil.createChild(inputEl, "ComplexQuery");
	        
	        Element orElement = SCXmlUtil.createChild(eComplexQuery, "Or");
	        Element eExpression1 = SCXmlUtil.createChild(orElement, "Exp");
	        eExpression1.setAttribute("Name", "Status");
	        eExpression1.setAttribute("Value", "2000");
	        Element eExpression2 = SCXmlUtil.createChild(orElement, "Exp");
	        eExpression2.setAttribute("Name", "Status");
	        eExpression2.setAttribute("Value", "1100");
	      }
	      if ((SCUIUtils.equals(sPicked, "Y")) && (SCUIUtils.equals(sInProgress, "N")) && (SCUIUtils.equals(sNotStarted, "Y")))
	      {
	        inputEl.setAttribute("GetNewBatches", "Y");
	        inputEl.setAttribute("GetExistingBatches", "Y");
	        Element eComplexQuery = SCXmlUtil.createChild(inputEl, "ComplexQuery");
	        
	        Element andElement = SCXmlUtil.createChild(eComplexQuery, "And");
	        
	        Element eExpression = SCXmlUtil.createChild(andElement, "Exp");
	        eExpression.setAttribute("Name", "Modifyts");
	        SCXmlUtil.setAttribute(eExpression, "Value", currDateTimeString);
	        
	        eExpression.setAttribute("QryType", "GT");
	        Element orElement = SCXmlUtil.createChild(andElement, "Or");
	        Element eExpression1 = SCXmlUtil.createChild(orElement, "Exp");
	        eExpression1.setAttribute("Name", "Status");
	        eExpression1.setAttribute("Value", "3000");
	        Element eExpression2 = SCXmlUtil.createChild(orElement, "Exp");
	        eExpression2.setAttribute("Name", "Status");
	        eExpression2.setAttribute("Value", "9000");
	      }
	      if ((SCUIUtils.equals(sPicked, "Y")) && (SCUIUtils.equals(sInProgress, "Y")) && (SCUIUtils.equals(sNotStarted, "N")))
	      {
	        inputEl.setAttribute("GetNewBatches", "N");
	        inputEl.setAttribute("GetExistingBatches", "Y");
	        Element eComplexQuery = SCXmlUtil.createChild(inputEl, "ComplexQuery");
	        
	        Element parentOrElement = SCXmlUtil.createChild(eComplexQuery, "Or");
	        
	        Element andElement = SCXmlUtil.createChild(parentOrElement, "And");
	        
	        Element eExpression = SCXmlUtil.createChild(andElement, "Exp");
	        eExpression.setAttribute("Name", "Modifyts");
	        SCXmlUtil.setAttribute(eExpression, "Value", currDateTimeString);
	        
	        eExpression.setAttribute("QryType", "GT");
	        Element orElement = SCXmlUtil.createChild(andElement, "Or");
	        Element eExpression1 = SCXmlUtil.createChild(orElement, "Exp");
	        eExpression1.setAttribute("Name", "Status");
	        eExpression1.setAttribute("Value", "3000");
	        Element eExpression2 = SCXmlUtil.createChild(orElement, "Exp");
	        eExpression2.setAttribute("Name", "Status");
	        eExpression2.setAttribute("Value", "9000");
	        Element childOrElement = SCXmlUtil.createChild(parentOrElement, "Or");
	        
	        Element eExpression3 = SCXmlUtil.createChild(childOrElement, "Exp");
	        
	        eExpression3.setAttribute("Name", "Status");
	        eExpression3.setAttribute("Value", "2000");
	        Element eExpression4 = SCXmlUtil.createChild(childOrElement, "Exp");
	        
	        eExpression4.setAttribute("Name", "Status");
	        eExpression4.setAttribute("Value", "1100");
	      }
	      if ((SCUIUtils.equals(sPicked, "Y")) && (SCUIUtils.equals(sInProgress, "Y")) && (SCUIUtils.equals(sNotStarted, "Y")))
	      {
	        inputEl.setAttribute("GetNewBatches", "Y");
	        inputEl.setAttribute("GetExistingBatches", "Y");
	        Element eComplexQuery = SCXmlUtil.createChild(inputEl, "ComplexQuery");
	        
	        Element parentOrElement = SCXmlUtil.createChild(eComplexQuery, "Or");
	        
	        Element andElement = SCXmlUtil.createChild(parentOrElement, "And");
	        
	        Element eExpression = SCXmlUtil.createChild(andElement, "Exp");
	        eExpression.setAttribute("Name", "Modifyts");
	        SCXmlUtil.setAttribute(eExpression, "Value", currDateTimeString);
	        
	        eExpression.setAttribute("QryType", "GT");
	        Element orElement = SCXmlUtil.createChild(andElement, "Or");
	        Element eExpression1 = SCXmlUtil.createChild(orElement, "Exp");
	        eExpression1.setAttribute("Name", "Status");
	        eExpression1.setAttribute("Value", "3000");
	        Element eExpression2 = SCXmlUtil.createChild(orElement, "Exp");
	        eExpression2.setAttribute("Name", "Status");
	        eExpression2.setAttribute("Value", "9000");
	        Element childOrElement = SCXmlUtil.createChild(parentOrElement, "Or");
	        
	        Element eExpression3 = SCXmlUtil.createChild(childOrElement, "Exp");
	        
	        eExpression3.setAttribute("Name", "Status");
	        eExpression3.setAttribute("Value", "2000");
	        Element eExpression4 = SCXmlUtil.createChild(childOrElement, "Exp");
	        
	        eExpression4.setAttribute("Name", "Status");
	        eExpression4.setAttribute("Value", "1100");
	      }
	      inputEl.removeAttribute("Picked");
	      inputEl.removeAttribute("InProgress");
	      inputEl.removeAttribute("NotStarted");
	    }
	  }
	  
	  private int getPageSize(SCUIContext uiContext)
	  {
	    int pageSize = Integer.valueOf(WSCMashupUtils.getPageSizeByEntityType(uiContext, "StoreBatch")).intValue();
	    if (!SCUIUtils.isVoid(uiContext.getRequest().getParameter("scControllerData")))
	    {
	      Element elem = SCUIJSONUtils.getXmlFromJSON(uiContext.getRequest().getParameter("scControllerData")).getDocumentElement();
	      
	      Element elemMashupRefs = SCXmlUtil.getChildElement(elem, "MashupRefs");
	      for (Element elemMashupRef : SCXmlUtil.getChildren(elemMashupRefs, "MashupRef")) {
	        if (!SCUtil.isVoid(elemMashupRef.getAttribute("scPageSize")))
	        {
	          pageSize = SCXmlUtil.getIntAttribute(elemMashupRef, "scPageSize");
	          
	          break;
	        }
	      }
	    }
	    return pageSize;
	  }
	  
	  private Element getPreviousPageRecord(SCUIContext uiContext)
	  {
	    Element prevpageRecord = null;
	    Element clonePrevPage = null;
	    if (!SCUIUtils.isVoid(uiContext.getRequest().getParameter("scControllerData")))
	    {
	      Element elem = SCUIJSONUtils.getXmlFromJSON(uiContext.getRequest().getParameter("scControllerData")).getDocumentElement();
	      
	      Element elemMashupRefs = SCXmlUtil.getChildElement(elem, "MashupRefs");
	      for (Element elemMashupRef : SCXmlUtil.getChildren(elemMashupRefs, "MashupRef")) {
	        if (!SCUtil.isVoid(SCXmlUtils.getChildElement(elemMashupRef, "scPreviousPageRecord")))
	        {
	          prevpageRecord = SCXmlUtils.getFirstChildElement(SCXmlUtils.getChildElement(elemMashupRef, "scPreviousPageRecord"));
	          
	          break;
	        }
	      }
	    }
	    if (!SCUtil.isVoid(prevpageRecord))
	    {
	      clonePrevPage = SCXmlUtils.createDocument("StoreBatch").getDocumentElement();
	      clonePrevPage.setAttribute("BatchNo", prevpageRecord.getAttribute("BatchNo"));
	      clonePrevPage.setAttribute("StoreBatchKey", prevpageRecord.getAttribute("StoreBatchKey"));
	    }
	    return clonePrevPage;
	  }
	  
	  private Element getTimingThresholdDataByShipmentType(SCUIContext uiContext, String deliveryMethod)
	  {
	    String codeType = null;
	    int i = 1;
	    switch (deliveryMethod)
	    {
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
	    }
	    catch (APIManager.XMLExceptionWrapper e)
	    {
	      throw e;
	    }
	  }
	  
	  private String[] getImageAttributesArray(Element commonCodeList, int remainingMinutes)
	  {
	    String[] imageAttributes = new String[2];
	    List<Element> commonCodes = SCXmlUtil.getChildrenList(commonCodeList);
	    if ((commonCodes != null) && (commonCodes.size() > 0)) {
	      for (Element commonCode : commonCodes)
	      {
	        long codeValue = Long.valueOf(commonCode.getAttribute("CodeValue")).longValue();
	        if (remainingMinutes <= codeValue)
	        {
	          imageAttributes[0] = commonCode.getAttribute("CodeShortDescription");
	          imageAttributes[1] = commonCode.getAttribute("CodeLongDescription");
	          break;
	        }
	      }
	    }
	    return imageAttributes;
	  }

}
