package com.academy.ecommerce.sterling.userexits;

import org.w3c.dom.Document;

import com.academy.util.xml.XMLUtil;
import com.yantra.pca.ycd.japi.ue.YCDGetTrackingNumberURLUE;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

public class AcademyGetTrackingNumberURLUE implements YCDGetTrackingNumberURLUE
{
	/**
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyGetTrackingNumberURLUE.class);

	public Document getTrackingNumberURL(YFSEnvironment env, Document inDoc) throws YFSUserExitException
	{
		log.verbose("we are here");
		log.verbose(XMLUtil.getXMLString(inDoc));
		return inDoc;
	}

}
