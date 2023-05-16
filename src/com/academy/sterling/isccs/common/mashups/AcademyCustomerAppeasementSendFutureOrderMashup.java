package com.academy.sterling.isccs.common.mashups;

import com.yantra.yfc.log.YFCLogCategory;
import com.sterlingcommerce.ui.web.framework.context.SCUIContext;
import com.sterlingcommerce.ui.web.framework.context.SCUIUserPreferences;
import com.sterlingcommerce.ui.web.framework.extensions.ISCUILocale;
import com.sterlingcommerce.ui.web.framework.helpers.SCUIMashupHelper;
import com.sterlingcommerce.ui.web.framework.mashup.SCUIMashupMetaData;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.academy.util.xml.XMLUtil;
import com.ibm.isccs.order.mashups.SCCSCustomerAppeasementSendFutureOrderMashup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AcademyCustomerAppeasementSendFutureOrderMashup extends SCCSCustomerAppeasementSendFutureOrderMashup{

	  private static YFCLogCategory cat = YFCLogCategory.instance(AcademyCustomerAppeasementSendFutureOrderMashup.class.getName());

      public Element massageInput(Element inputEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext) {
             String sMethodName = "AcademyCustomerAppeasementSendFutureOrderMashup.massageInput() ";
             cat.beginTimer(sMethodName);
             cat.debug(sMethodName + " Input xml is -----> " +  SCXmlUtil.getString(inputEl));
             Element sendFuturemassageInEle = super.massageInput(inputEl, mashupMetaData, uiContext);
             
             Element eleXMLDatafromWCC =  SCXmlUtil.getChildElement(inputEl, "XMLData");
             Element eleOrderfromWCC =  SCXmlUtil.getChildElement(inputEl, "Order");
             Element eleXMLDataUEIn = SCXmlUtil.getChildElement(sendFuturemassageInEle, "XMLData");
             Element eleOrderUEIn = SCXmlUtil.getChildElement(eleXMLDataUEIn, "Order");
             String strAppeasementCategory =SCXmlUtil.getAttribute(eleOrderfromWCC, "AppeasementCategory"); 
             String strAppeasementDescription =SCXmlUtil.getAttribute(eleOrderfromWCC, "AppeasementDescription"); 
             SCXmlUtil.setAttribute(eleOrderUEIn, "AppeasementCategory", strAppeasementCategory);
             SCXmlUtil.setAttribute(eleOrderUEIn, "AppeasementDescription", strAppeasementDescription);

             Element eleOrderlinesfromWCC = SCXmlUtil.getChildElement(eleOrderfromWCC, "OrderLines");
             cat.debug(sMethodName + " orderLines is -----> " +  SCXmlUtil.getString(eleOrderlinesfromWCC));
             Element eleOrderNotesfromWCC = SCXmlUtil.getChildElement(eleOrderfromWCC, "Notes");
             cat.debug(sMethodName + " Notes is -----> " +  SCXmlUtil.getString(eleOrderNotesfromWCC));
             XMLUtil.importElement(eleOrderUEIn, eleOrderlinesfromWCC); 
             XMLUtil.importElement(eleOrderUEIn, eleOrderNotesfromWCC); 
             cat.debug(sMethodName + " Final Input  xml to AcademyCreateGCAppeasementService service is -----> "
                     + SCXmlUtil.getString(sendFuturemassageInEle));

        cat.endTimer(sMethodName);
        return sendFuturemassageInEle;


}
}
