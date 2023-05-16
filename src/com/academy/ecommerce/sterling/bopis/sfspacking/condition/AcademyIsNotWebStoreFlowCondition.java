package com.academy.ecommerce.sterling.bopis.sfspacking.condition;

import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;


import com.academy.util.constants.AcademyConstants;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class is used to check if its Web store flow. If its from Webs store, it returns true. As from this flow on the event Add to Manifest needs to be skipped.
 * This wil be called from ON_CONTAINER_PACK_COMPLETE event of ADD_TO_CONTAINER transaction and VERIFICATION_DONE event of VERIFY_PACK transaction.
 * @author rastaj1
 *
 */
public class AcademyIsNotWebStoreFlowCondition implements YCPDynamicConditionEx{
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademyIsNotWebStoreFlowCondition.class);
	
	private Map props;

	@Override
	public boolean evaluateCondition(YFSEnvironment env, String arg1,
			Map arg2, Document inDco) {
		log.beginTimer("AcademyIsWebStoreFlowCondition::evaluateCondition");
		String strIsWebStore = "N";
		if(!YFCCommon.isVoid(props)) {
			strIsWebStore = (String) props.get("UseIsWebStore");
		}
		Boolean isWebStoreFlow;
		if(strIsWebStore.equals("Y")) {
			isWebStoreFlow = false;
		} else {
			isWebStoreFlow = true;
		}
		
		if(!YFCCommon.isVoid(env.getTxnObject("IsWebstoreFlow")) && AcademyConstants.STR_YES.equals(env.getTxnObject("IsWebstoreFlow"))){
			log.verbose("This is called from Web store flow hence returning true");
			if(strIsWebStore.equals("Y")) {
				isWebStoreFlow = true;
			} else {
				isWebStoreFlow = false;
			}			
		}
		log.debug("isWebStoreFlow:::" + isWebStoreFlow);
		log.endTimer("AcademyIsWebStoreFlowCondition::evaluateCondition");
		return isWebStoreFlow;
	}

	@Override
	public void setProperties(Map props) {
		this.props = props;
	}

}
