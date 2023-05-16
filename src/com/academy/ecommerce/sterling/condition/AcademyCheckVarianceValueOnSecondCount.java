package com.academy.ecommerce.sterling.condition;

import java.util.Map;

import com.yantra.ycp.japi.YCPDynamicCondition;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;


/*
 * Dynamic Condition for Issue#3221
 * Called in Service AcademyRaiseAlertForAcceptVariance
 * author @muchil
 */

public class AcademyCheckVarianceValueOnSecondCount implements YCPDynamicCondition{
	
    
    /*
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyCheckVarianceValueOnSecondCount.class);
	private boolean b=false;

    
    /* to check if variance value is greater than 50 on second count, if yes then 
     * return true to condition component in AcademyRaiseAlertForAcceptVariance
     * */
	public boolean evaluateCondition(YFSEnvironment env, String sName,
			Map mapData, String sXMLData) {
		log.beginTimer(" Begining of AcademyCheckVarianceValueOnSecondCount-> evaluateCondition Api");
		log.verbose("Input Xml to evaluateCondition API is" + sXMLData);

		YFCDocument inXml = YFCDocument.getDocumentFor(sXMLData);
		if (!YFCObject.isVoid(inXml))
		{
			YFCElement eleCountRequest = inXml.getDocumentElement();
			YFCElement eleCountResultList = eleCountRequest.getChildElement("CountResultList");
			// log.verbose(""+XMLUtil.getElementXMLString((Element) eleCountResultList));
			YFCNodeList nodeCountResultList = eleCountResultList.getElementsByTagName("CountResult");
			int intNodeListLength = nodeCountResultList.getLength();

			for (int i = 0; i < intNodeListLength; i++)
			{
				YFCElement eleCountResult = (YFCElement) nodeCountResultList.item(i);
				String sTaskType = eleCountResult.getAttribute("TaskType");
				String strVarianceValue =eleCountResult.getAttribute("VarianceValue");
				log.verbose("Variance Value is"+strVarianceValue);
				Double dVarianceValue = Math.abs(Double.valueOf(strVarianceValue));
				log.verbose("Absolute Variance Value is"+dVarianceValue);
				//Float fVarianceValue = Float.valueOf(strVarianceValue);
				//log.verbose("Variance Value is"+fVarianceValue);
				if (("SeconCount".equals(sTaskType)) && (dVarianceValue > 50.00))
				{
					b=true;
					log.verbose("Accept variance alert Should be raised");
					return b;
				}

			}

		}
		log.verbose("Accept variance alert Should not be raised");
		log.endTimer(" End of AcademyCheckVarianceValueOnSecondCount-> evaluateCondition Api");
		return b;
	}
}
