package com.academy.ecommerce.order.api;

import java.io.File;
import java.io.FileInputStream;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSOrderRepricingUE;

public class AcademyOrderRepricingUEImpl implements YFSOrderRepricingUE{
	/**
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyOrderRepricingUEImpl.class);

	public Document orderReprice(YFSEnvironment env, Document doc) throws YFSUserExitException {
		log.verbose("inXML to AcademyOrderRepricingUE"+YFCDocument.getDocumentFor(doc));
		log.verbose("orderReprice");
		YFCDocument outDoc = null;
		try{ 
			
			YFCElement elem = YFCDocument.createDocument("Order").getDocumentElement();
			elem.setAttribute("OrderHeaderKey", doc.getDocumentElement().getAttribute("OrderHeaderKey"));
			
			
			
			Document d1 = AcademyUtil.invokeAPI(env, "getOrderDetails", elem.getOwnerDocument().getDocument());
			
			log.verbose("outXML from getOrderDetails is "+YFCDocument.getDocumentFor(d1));
			
			outDoc = YFCDocument.parse(new FileInputStream(new File("E:\\Academy80\\Interfaces\\orderReprice.xml")));
		}catch (Exception e){
			e.printStackTrace();
		}
		if (outDoc == null){
			return doc;
		}
		return outDoc.getDocument();
	}

}
