package com.academy.ecommerce.order.api;

import java.io.File;
import java.io.FileInputStream;

import org.w3c.dom.Document;

import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyAVSStubImpl1 {
	
	/**
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyAVSStubImpl1.class);

	public Document AcademyAVSTest1(YFSEnvironment env, Document doc) {
		
		YFCDocument outDoc = null;
		try {
			// log.verbose("inXML to
			// getExternalPricesForItemList"+YFCDocument.getDocumentFor(doc));
			log.verbose("input Doc->StubImpl for AVS TEST CASES()1:InputDoc"+ XMLUtil.getXMLString(doc));
			outDoc = YFCDocument.parse(new FileInputStream(new File(
					"//dbdata//SterlingOMS//Backups//AcademyAVSBridgeResponse1.xml")));
			log.verbose("outPut doc:UE IMPL-"+ XMLUtil.getXMLString(outDoc.getDocument()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outDoc.getDocument();

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
