package com.academy.ecommerce.sterling.condition;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.xml.XMLUtil;

//yantra import statements
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;


public class AcademyDynCondFutureDatedInvoice implements YCPDynamicConditionEx
{
	//Set the logger
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyDynCondFutureDatedInvoice.class);

	public boolean evaluateCondition(YFSEnvironment env, String arg1, Map arg2, Document inXML) {

		log.debug("evaluateCondition method in AcademyDynamicConditionForFutureDatedInvoice");

		log.debug("Input MXL coming to evaluateCondition" + XMLUtil.getXMLString(inXML));
		
		Element eleInvoiceHeader;
		try {
			eleInvoiceHeader = (Element)XMLUtil.getElementByXPath(inXML, "/InvoiceDetail/InvoiceHeader");
			String strInvoiceNo = eleInvoiceHeader.getAttribute("InvoiceNo");			
			
			if(!YFSObject.isVoid(strInvoiceNo)){
			//get First 8 characters that is YYYYMMDD
			String strInvoiceDate = strInvoiceNo.substring(0,8);
			
			//getCurrent date in YYYYMMDD format
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			Date date = new Date();
			String strCurrentDate = dateFormat.format(date);
			
			log.debug("Current Date :\t"+strCurrentDate);
			log.debug("InvoiceDate :\t"+strInvoiceDate);			
			
			if(!YFSObject.isVoid(strInvoiceDate)) {
				int iCorrentDate = Integer.parseInt(strCurrentDate);
				int iInvoiceDate = Integer.parseInt(strInvoiceDate);
				if(iInvoiceDate > iCorrentDate){
					log.debug("returning TRUE");
					return true;
				}				
			}
			}
		} catch (Exception e) {
			log.errordtl("Error while comparing InvoiceDate with current date");
			e.printStackTrace();
		}		   
		log.debug("returning FALSE");
		return false;
	}

	public void setProperties(Map arg0) {
		
	}

}
