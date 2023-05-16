package com.academy.ecommerce.sterling.carrierupdates;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

//EFP-21 Carrier Updates Consumption
public class AcademyCarrierStatusTracker {
	
	private static YFCLogCategory log = YFCLogCategory
		.instance(AcademyCarrierStatusTracker.class);

	public Document addCarrierStatusTracker (YFSEnvironment env, Document inDoc)
		throws Exception{
		log.debug("Start - AcademyCarrierStatusTracker.addCarrierStatusTracker() ##");
		Document stautsTrackerInDoc = null;
		Document stautsTrackerOutDoc = null;
	
		stautsTrackerInDoc = XMLUtil.createDocument("AcadTrackingUpdates");
		Element statusTrackerEle = stautsTrackerInDoc.getDocumentElement();
		Element inEle = inDoc.getDocumentElement();
		
		String strMasterStatus = inEle.getAttribute(AcademyConstants.ATTR_STATUS);
		if(!YFCObject.isNull(strMasterStatus) && !YFCObject.isVoid(strMasterStatus)){
			statusTrackerEle.setAttribute(AcademyConstants.ATTR_STATUS, 
					strMasterStatus);
		}
		
		String strProcessedFlag = inEle.getAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED);
		if(!YFCObject.isNull(strProcessedFlag) && !YFCObject.isVoid(strProcessedFlag)){
			statusTrackerEle.setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED, 
					strProcessedFlag);
		}
		
		String strTrackingNo = inEle.getAttribute(AcademyConstants.ATTR_TRACKING_NO);
		if(!YFCObject.isNull(strTrackingNo) && !YFCObject.isVoid(strTrackingNo)){
			statusTrackerEle.setAttribute(AcademyConstants.ATTR_TRACKING_NO, 
					strTrackingNo);
		}
		
		String strInvoiceNo = inEle.getAttribute(AcademyConstants.ATTR_INVOICE_NO);
		if(!YFCObject.isNull(strInvoiceNo) && !YFCObject.isVoid(strInvoiceNo)){
			statusTrackerEle.setAttribute(AcademyConstants.ATTR_INVOICE_NO, 
					strInvoiceNo);
		}
		
		String strShipmentNo = inEle.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		if(!YFCObject.isNull(strShipmentNo) && !YFCObject.isVoid(strShipmentNo)){
			statusTrackerEle.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, 
					strShipmentNo);
		}
		
		String strProNo = inEle.getAttribute(AcademyConstants.ATTR_PRO_NO);
		if(!YFCObject.isNull(strProNo) && !YFCObject.isVoid(strProNo)){
			statusTrackerEle.setAttribute(AcademyConstants.ATTR_PRO_NO, 
					strProNo);
		}
		
		String strStatusDate = inEle.getAttribute(AcademyConstants.ATTR_STATUS_DATE);
		if(!YFCObject.isNull(strStatusDate) && !YFCObject.isVoid(strStatusDate)){
			statusTrackerEle.setAttribute(AcademyConstants.ATTR_STATUS_DATE, 
					strStatusDate);
		}	
		
		String strStatusDesc = inEle.getAttribute(AcademyConstants.ATTR_STATUS_DESCRIPTION);
		if(!YFCObject.isNull(strStatusDesc) && !YFCObject.isVoid(strStatusDesc)){
			statusTrackerEle.setAttribute(AcademyConstants.ATTR_STATUS_DESCRIPTION, 
					strStatusDesc);
		}
		
		log.debug("Input to API::"+XMLUtil.getXMLString(stautsTrackerInDoc));
		
		stautsTrackerOutDoc = AcademyUtil.invokeService(env,
			"AcademyCreateTrackingUpdates", stautsTrackerInDoc);
	
		return stautsTrackerOutDoc;
	}
}
