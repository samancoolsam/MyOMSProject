package com.academy.ecommerce.sterling.bopis.monitor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @Author : Chiranthan(SapientRazorfish_)
 * @JIRA# : BOPIS-<>
 * @Date : Created on 20-Jun-2018
 * 
 * @Purpose : 
 *This class is a dynamic Condition, invoked as part of 'BOPIS store SLA reminder Monitoring Rule'
 *
 *if BOPIS Shipment && (Current Date < (RBP StatusDate + Escalation SLA(2hrs)))
 *	bWithinEscalationSLA = true;
 *
 **/

public class AcademyBOPISEscalationSLACondition implements YCPDynamicConditionEx {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyBOPISEscalationSLACondition.class);	
	private Map argMap;
	public void setProperties(Map map) {
		argMap = map;
	}	
	
	public boolean evaluateCondition(YFSEnvironment env, String sName, Map mapData, Document inDoc) {
		log.beginTimer("Entering AcademyBOPISEscalationSLACondition.evaluateCondition() ::"+XMLUtil.getXMLString(inDoc));
		
		String strEscalationSLA = null;
		String strDeliveryMethod = null;
		String strShipmentStatusDate = null;
		boolean bWithinEscalationSLA = false;
		Date dtCurrentDate = null;
		Date dtShipmentStatusDate = null;
		Date dtEscalationDate = null;
		Element eleInDoc = inDoc.getDocumentElement();
		
		try
		{
			if (!YFCObject.isVoid(inDoc))
			{
				strEscalationSLA = (String)argMap.get(AcademyConstants.STR_BOPIS_PICK_ESCALATION_SLA);
				strDeliveryMethod = eleInDoc.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
				strShipmentStatusDate = eleInDoc.getAttribute(AcademyConstants.ATTR_STATUS_DATE);
				
				if(AcademyConstants.STR_PICK_DELIVERY_METHOD.equals(strDeliveryMethod)){
					SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
					Calendar cal = Calendar.getInstance();
					dtCurrentDate = cal.getTime();
					log.verbose("Current System Date :: "+dtCurrentDate);
					
					dtShipmentStatusDate = (Date)sdf.parse(strShipmentStatusDate);
					log.verbose("Shipment Status Date :: "+dtShipmentStatusDate);
					cal.setTime(dtShipmentStatusDate);
					cal.add(Calendar.MINUTE, Integer.parseInt(strEscalationSLA));
					dtEscalationDate = cal.getTime();
					log.verbose("Escalation SLA Date :: "+dtEscalationDate);
					
					//If Current System Date < (Shipment RBP ‘StatusDate’ + SLA), meaning Escalation SLA is not reached yet!
					if(dtCurrentDate.before(dtEscalationDate))
					{
						log.verbose("Escalation SLA not reached yet");
						bWithinEscalationSLA = true;
					}else{
						log.verbose("Crossed Escalation SLA - Time to notify manager!");
						bWithinEscalationSLA = false;
					}
				}else{
					//Not a BOPIS Shipment, hence return false
					bWithinEscalationSLA = false;
				}
				
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log.error(e);			
			throw new YFSException(e.getMessage(), 
					"MTR0001", "Failure occurring while calculating the 1st BOPIS SLA");
		}
		
		log.endTimer("Exiting AcademyBOPISEscalationSLACondition.evaluateCondition() ::"+bWithinEscalationSLA);
		return bWithinEscalationSLA;
	}		
	}
