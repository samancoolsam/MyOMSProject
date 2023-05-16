package com.academy.ecommerce.sterling.bopis.sfspacking.api;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class will be invoked on 'ReadyToShipFromWebStore.ON_SUCCESS for Store /YCD_BACKROOM_PICK.ON_SUCCESS for DC' event to add
 * an entry in yfs_task_q table for the packed shipment, with available_date = Current date + Ship Interval.
 */

public class AcademyCreateTaskQForPackedShipments {
	
	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyCreateTaskQForPackedShipments.class);

	/**
	  * The below method to invoke manageTaskQueue API to insert a record for the packed shipment with 
	  * available_date = Current date + Ship Interval for that shipment.
	  */
	public Document createTaskQRecord(YFSEnvironment env, Document inDoc) throws Exception {
		
		logger.beginTimer(" Begining of AcademyCreateTaskQForPackedShipments-> createTaskQRecord");
		logger.verbose("AcademyCreateTaskQForPackedShipments : createTaskQRecord input xml::" + XmlUtils.getString(inDoc));

		Element eleShipment = inDoc.getDocumentElement();
		String strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		String strOrganizationCode = AcademyConstants.PRIMARY_ENTERPRISE;

		 /**
		  * invoke getCommonList API to get the Ship Interval configured 
		  * in common codes for 'SHIP_INTERVAL' 
		  */
		Document commonCodeOutDoc = AcademyCommonCode.getCommonCodeList(env,AcademyConstants.ATTR_SHIP_INTERVAL , strOrganizationCode);
		NodeList nl = commonCodeOutDoc.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
		int nlLength = nl.getLength();
		if (nlLength > 0 && (!YFCObject.isVoid(strShipmentKey))) {
			Element ele = (Element) nl.item(0);
			String strMinute = ele.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
			logger.verbose("CommonCode output : " + commonCodeOutDoc);
			if (!YFCObject.isVoid(strMinute) && !AcademyConstants.ATTR_ZERO.equals(strMinute)) {
				// Converting string to integer value
				int iStrMinute = Integer.parseInt(strMinute);

	        // To calculate Available date and time
				SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MINUTE, iStrMinute);
				String strAvailableDate = sdf.format(calendar.getTime());
				logger.verbose("availableDate : " + strAvailableDate);

	       // Preparing i/p and invoking manageTaskQueue API
				Document docTaskQueue = XMLUtil.createDocument("TaskQueue");
				Element eleTaskQueue = docTaskQueue.getDocumentElement();
				eleTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY, strShipmentKey);
				eleTaskQueue.setAttribute(AcademyConstants.ATTR_OPERATION, AcademyConstants.ATTR_MANAGE);
				eleTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE, AcademyConstants.ATTR_SHIPMENT_KEY);
				eleTaskQueue.setAttribute(AcademyConstants.ATTR_TRANSID, AcademyConstants.ACAD_CONFIRM_SHIPMENT_TRANS);
				eleTaskQueue.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, strAvailableDate);
				logger.verbose("Input xml to manageTaskQueue API ::" + XmlUtils.getString(docTaskQueue));
				AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_TASK_QUEUE, docTaskQueue);
			}
		}
		  logger.endTimer(" End of AcademyCreateTaskQForPackedShipments-> createTaskQRecord");
		return inDoc;
	}


}
