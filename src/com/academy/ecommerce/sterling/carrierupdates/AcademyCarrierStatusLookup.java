package com.academy.ecommerce.sterling.carrierupdates;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

//EFP-21 Carrier Updates Consumption
public class AcademyCarrierStatusLookup {
	
	private static YFCLogCategory log = YFCLogCategory
		.instance(AcademyCarrierStatusLookup.class);
	
	public static Document getCarrierStatusDetails (YFSEnvironment env, String strStatus, String strScac)
		throws Exception{
		log.debug("Start - AcademyGetCarrierStatusLookup.getCarrierStatusDetails() ##");
		Document stautsLookupInDoc = null;
		Document stautsLookupOutDoc = null;
		
		stautsLookupInDoc = XMLUtil.createDocument("AcadCarrierStatusLookup");
		stautsLookupInDoc.getDocumentElement().setAttribute("StatusCode",
				strStatus);
		stautsLookupInDoc.getDocumentElement().setAttribute("SCAC",
				strScac);
		log.debug("Input to API::"+XMLUtil.getXMLString(stautsLookupInDoc));
		stautsLookupOutDoc = AcademyUtil.invokeService(env,
				"AcademyGetCarrierStatusLookup", stautsLookupInDoc);
		
		return stautsLookupOutDoc;
	}

}
