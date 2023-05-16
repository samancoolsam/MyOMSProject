package com.academy.ecommerce.yantriks.inventory;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyUpdateWCSOnResourcing {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyUpdateWCSOnResourcing.class.getName());
	public Document invokeUpdateToWCSOnResourcing(YFSEnvironment env,Document docInXML)
	{
		String strIsResourcedOrder = (String)env.getTxnObject("IsResourcedOrder");
		
		log.verbose("Transaction Object"+strIsResourcedOrder);
		
		if(AcademyConstants.STR_YES.equals(strIsResourcedOrder))
		{
			try {
				AcademyUtil.invokeService(env, "AcademySendUpdateToWCSOnResourcing", docInXML);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
		}
		return docInXML;
	}

}
