package com.academy.ecommerce.sterling.inventory.api;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;

import com.yantra.interop.japi.YIFCustomApi;

import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;

import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class AcademyAdjustLocationInventoryAPI implements YIFCustomApi {

	
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyAdjustLocationInventoryAPI.class);

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}

	/**
	 * This method calls OOB adjustLocationInventory API and returns input document
	 * 
	 * @param env
	 *            Environment variable
	 * @param inDoc
	 *            Input document
	 * 
	 * @throws Exception
	 *             Generic exception
	 */
	public Document adjustLocationInventory(YFSEnvironment env, Document inDoc)
			throws Exception {
		log.beginTimer(" Begin of AcademyAdjustLocationInventory ->adjustLocationInventory Api");
		
		Element auditEl = (Element) inDoc.getDocumentElement().getElementsByTagName("Audit").item(0); 
		String reasonCode = auditEl.getAttribute(AcademyConstants.INV_REASON_CODE);
		
	    log.verbose("Reason code used for adjustments: " + reasonCode);
	    
	    Element inventoryEl = (Element) inDoc.getDocumentElement().getElementsByTagName("Inventory").item(0); 
		String quantity = inventoryEl.getAttribute("Quantity");
		
		log.verbose("Adjustment quantity: " + quantity);
		
        if(chkValidInvAdjustmentRsnCode(env, reasonCode)) {
        	Document docOutputAdjLocInventory = AcademyUtil.invokeAPI(env, "adjustLocationInventory", inDoc);
        	if(docOutputAdjLocInventory != null) {
        		log.verbose("adjustLocation Inventory output: " + XMLUtil.getXMLString(docOutputAdjLocInventory));
        	}
    		
    		inDoc.getDocumentElement().setAttribute("ExtnTimeStamp", AcademyUtil.getCurrentXMLGMTTime()); 
    		inventoryEl.setAttribute("Quantity", quantity);
    		
    		log.verbose("InDoc after stamping timestamp: " + XMLUtil.getXMLString(inDoc));
        	
        } else {
        	YFSException e = new YFSException();
        	e.setErrorCode("ACAD_INVALID_REASON_CODE");
        	e.setErrorDescription("Invalid Reason Code used for Manual Inventory Adjustments");
        	throw e;
        }
			
		log.endTimer(" End of AcademyAdjustLocationInventory ->adjustLocationInventory Api");
		return inDoc;		
	}
	
	
	
	/**
	 *  Get Financial Transaction code for given transaction type
	 * @param env
	 * @param finTransType
	 * @throws YFSException
	 */
	private boolean chkValidInvAdjustmentRsnCode(YFSEnvironment env, String reasonCode) throws YFSException
	{
		YFCDocument commonCodeInDoc = YFCDocument.createDocument("CommonCode");
		commonCodeInDoc.getDocumentElement().setAttribute("CodeType", AcademyConstants.INV_ADJ_RSN);
		commonCodeInDoc.getDocumentElement().setAttribute("CodeValue", reasonCode);
		Document commonCodeOutDoc = null;
		try
		{
			commonCodeOutDoc = AcademyUtil.invokeAPI(env, "getCommonCodeList", commonCodeInDoc.getDocument());
			//commonCodeOutDoc = YFCDocument.getDocumentFor(new File("D://Document2.xml")).getDocument();
		}
		catch (Exception e)
		{
			throw new YFSException("Error Invoking getCommonCodeList API");
		}

		NodeList nList = commonCodeOutDoc.getElementsByTagName("CommonCode");
		if (nList.getLength() > 0)
		{
			return true;
			//Element commonCodeEl = (Element) nList.item(0);
			//finTranCode = commonCodeEl.getAttribute("CodeShortDescription");
		}

		return false;
	}
	
	public static void main(String args[]) {
    	//log.verbose("getCurrentXMLGMTTime:  " + AcademyUtil.getCurrentXMLGMTTime()); 
		String sCurrentGMTTime="";
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date currentDate = new Date();
        Calendar here = Calendar.getInstance();
        int gmtoffset = here.get(Calendar.DST_OFFSET)
        + here.get(Calendar.ZONE_OFFSET);
        Date GMTDate = new Date(currentDate.getTime() - gmtoffset);
        sCurrentGMTTime = formatter.format(GMTDate);
        
        log.verbose("Datetime is:" + sCurrentGMTTime);
    	
    }

	
}
