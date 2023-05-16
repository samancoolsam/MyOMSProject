package com.academy.ecommerce.sterling.dsv.order;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * 
 * Validating the Cancel Msg coming from the VendorNet.
 *
 */
public class AcademyCancelMsgValidation {
	private static YFCLogCategory log = YFCLogCategory
	.instance(AcademyCancelMsgValidation.class);
	public Document cancelMessageValidation(YFSEnvironment env, Document inXML)
	{
		ValidateCancelMSGXML(env,inXML);
		
		return inXML;
		
	}

	public boolean ValidateCancelMSGXML(YFSEnvironment env, Document inXML)
	{
		log.verbose("inside the validate"+XMLUtil.getXMLString(inXML));
		String ErrorMsg="";
		String documentType=inXML.getDocumentElement().getAttribute("DocumentType");
		String enterpriseCode=inXML.getDocumentElement().getAttribute("EnterpriseCode");
		String orderNo=inXML.getDocumentElement().getAttribute("OrderNo");
		String orderReleaseKey=inXML.getDocumentElement().getAttribute("OrderReleaseKey");
		
		
		if(isEmptyOrNull(documentType))
        {
              ErrorMsg = ErrorMsg + "DocumentType is missing";
        }
        
        if(isEmptyOrNull(enterpriseCode))
        {
              ErrorMsg = ErrorMsg + "EnterpriseCode is missing \n";
        }
        
        if(isEmptyOrNull(orderNo))
        {
              ErrorMsg = ErrorMsg + "OrderNo is missing \n";
        }
        if(isEmptyOrNull(orderReleaseKey))
        {
              ErrorMsg = ErrorMsg + "OrderReleaseKey is missing \n";
        }
        
             /* Retriving the OrderLine from the inXML  */
		NodeList orderLinesList=inXML.getElementsByTagName("OrderLine");
		
		for(int i=0;i<orderLinesList.getLength();i++)
		{
			
		Element orderLineEle=(Element) orderLinesList.item(i);
		String action=orderLineEle.getAttribute("Action");
		String changeInQuantity=orderLineEle.getAttribute("ChangeInQuantity");
		String statusQuantity=orderLineEle.getAttribute("StatusQuantity");
		String orderLineKey=orderLineEle.getAttribute("OrderLineKey");
		String primeLineNo=orderLineEle.getAttribute("PrimeLineNo");
		String subLineNo=orderLineEle.getAttribute("SubLineNo");
		
		
		if(isEmptyOrNull(action))
        {
			
              ErrorMsg = ErrorMsg + "Action is missing \n";
        }
		
		if(isEmptyOrNull(orderLineKey)){
			boolean primeLineNoMissing = false;
            boolean subLineNoMissing = false;
            if(isEmptyOrNull(primeLineNo))
            {
                  primeLineNoMissing = true;
                  //ErrorMsg = ErrorMsg+ Line+ " \n PrimeLineNo is missing";              
            }
            if(isEmptyOrNull(subLineNo))
            {
                  subLineNoMissing = true;
                  //ErrorMsg = ErrorMsg +Line+ " \n SubLineNo is missing";
            }
            
            if(primeLineNoMissing && !subLineNoMissing)
            {
                  ErrorMsg = ErrorMsg+  "PrimeLineNo is missing \n";    
            }
            else if(!primeLineNoMissing && subLineNoMissing)
            {
                  ErrorMsg = ErrorMsg + "SubLineNo is missing \n";
            }
            else if(primeLineNoMissing && subLineNoMissing)
            {
                  ErrorMsg = ErrorMsg + "PrimeLineNo,SubLineNo and OrderLineKey are missing \n";
            }
            
                        
      
                        
      }
		if(isEmptyOrNull(changeInQuantity))
		{
			if(isEmptyOrNull(statusQuantity))
			{
				ErrorMsg = ErrorMsg + "ChangeInQuantity and StatusQuantity are missing \n";
			}
		}
		
		}
		
		boolean flag  = true;
		
		 if(!isEmptyOrNull(ErrorMsg))
        {
              flag = false;
              
              //Creating a info log to for Splunk alerts 
    			log.info("DSV Inbound Cancellation Error :: Mandatory Parameters missing for OrderNo:: " 
    					+ inXML.getDocumentElement().getAttribute("OrderNo") + " . Error Message :: "+ ErrorMsg);

    			//Throw custom Exception to roll back the transaction
    			YFSException yfsExcep = new YFSException(ErrorMsg);
    			yfsExcep.setErrorCode("Invalid Inforamtion");
    			yfsExcep.setErrorDescription("Mandatory Fields are missing");
    			throw yfsExcep;
        }
		 log.verbose("Document is Validated...."); 
      	return flag;
		
	}

	public boolean isEmptyOrNull(final String argS) {
		if (null==argS) { return true; }
        for (int ln=0; ln < argS.length() && !Character.isWhitespace(argS.charAt(ln)); ln++) {
              return false;
        }
        return true;
  }
}
