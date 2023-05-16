package com.academy.util.common;
/**#########################################################################################
*
* Project Name                : STS
* Module                      : 
* Author                      : CTS
* Author Group				  : CTS - POD
* Date                        : 03-Sep-2021 
* Description				  : This class is a dynamicCondition to validated TransactionObject							  								 
* ------------------------------------------------------------------------------------------------------------------------
* Date            	Author 		        			Version#       	Remarks/Description                      
* ------------------------------------------------------------------------------------------------------------------------
* 03-Sep-2021		CTS	 	 			 1.0           		Initial version
*
* #########################################################################################################################*/

import java.util.Map;
import org.w3c.dom.Document;

import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;




public class AcademyTransObject implements YCPDynamicConditionEx { 
	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyTransObject.class);
	private Map args;
	
	
	/**
	 * TrnxObj,ValidateFor needs to be passed in dynamic condition
	 * TrnxObj=transactionObjedt & ValidateFor=Y/N
	 */
	@Override
	public boolean evaluateCondition(YFSEnvironment env, String arg1, Map arg2, Document arg3) {
		logger.beginTimer("START Dynamic Condition AcademyTransObject.evaluateCondition");
		
		Boolean bvalue= false;
		String strTxnObject="";
		String sTrnxObj = (String)args.get("TrnxObj");
		String sValidateFor = (String)args.get("ValidateFor");
		if(!YFCCommon.isVoid(sTrnxObj)&& !YFCCommon.isVoid(sValidateFor) ) {
			 strTxnObject = (String) env.getTxnObject(sTrnxObj);
			if(YFCCommon.equalsIgnoreCase(sValidateFor, strTxnObject)) {
				bvalue= true;
			}else {
				bvalue= false;
			}
		}
		
       if(logger.isVerboseEnabled()) {
			logger.debug("evaluateCondition for " + strTxnObject +"=="+sValidateFor+ " is "+bvalue);
		}
       logger.beginTimer("END Dynamic Condition AcademyTransObject.evaluateCondition");
       return bvalue;
	}
	
	@Override
	public void setProperties(Map args) {
		this.args =  args;
		
	}
	

}
