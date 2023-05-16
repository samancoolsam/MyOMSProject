package com.academy.ecommerce.order.api;

import java.io.File;
import java.io.FileInputStream;

import org.w3c.dom.Document;

import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSGetExternalPriceListForOrderingUE;

public class AcademyGetExternalPriceListForOrderingUEImpl implements YFSGetExternalPriceListForOrderingUE{
	
	/**
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyGetExternalPriceListForOrderingUEImpl.class);

	public Document getExternalPriceListForOrdering(YFSEnvironment env,
			Document doc) throws YFSUserExitException {
		log.verbose("inXML to AcademyGetExternalPriceListForOrderingUEImpl"+YFCDocument.getDocumentFor(doc));
		log.verbose("getExternalPriceListForOrdering");
		YFCDocument outDoc = null;
		try{ 
			outDoc = YFCDocument.parse(new FileInputStream(new File("E:\\Academy80\\Interfaces\\getExternalPriceListForOrdering.xml")));
		}catch (Exception e){
			e.printStackTrace();
		}
		if (outDoc == null){
			return doc;
		}
		return outDoc.getDocument();
	}

}
