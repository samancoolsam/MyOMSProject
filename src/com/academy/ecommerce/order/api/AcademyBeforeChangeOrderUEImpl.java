package com.academy.ecommerce.order.api;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSBeforeChangeOrderUE;

public class AcademyBeforeChangeOrderUEImpl implements YFSBeforeChangeOrderUE {
	/**
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyBeforeChangeOrderUEImpl.class);
    
	public Document beforeChangeOrder(YFSEnvironment env, Document doc)
			throws YFSUserExitException {
		
		log.verbose("inXML to AcademyBeforeChangeOrderUE"+YFCDocument.getDocumentFor(doc));
		log.verbose("beforeChangeOrder");
		Document outDoc = null;
		try{ 
			outDoc = AcademyUtil.invokeService(env, AcademyConstants.YCD_BEFORE_CHANGE_ORDER_SERVICE, doc); 
				//YFCDocument.parse(new FileInputStream(new File("E:\\Academy80\\Interfaces\\beforeChangeOrder.xml")));
		}catch (Exception e){
			e.printStackTrace();
		}
		if (outDoc == null){
			return doc;
		}
		return outDoc;
	}

}
