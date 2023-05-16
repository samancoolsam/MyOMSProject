package com.academy.ecommerce.sterling.egc;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyBOPISUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/*##################################################################################
*
* Project Name                : POD March Release
* Module                      : OMS
* Author                      : CTS
* Date                        : 26-FEB-2021
* Description                 : This file implements the logic to, 
* 								1. Create task queue entry if an order contains EGC line.
* 								   This service will get invoked as part of Create Order - On Success Event.
*                               
*
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author                  Version#       Remarks/Description                     
* ---------------------------------------------------------------------------------
* 26-FEB-2021     CTS                      1.0            Initial version
* 27-DEC-2022     CTS                      2.0            Revised version (Suppress 
* 														  alert for EGC as free item)
* ##################################################################################*/

public class AcademyEGCCreateTaskQueueForAlerts implements YIFCustomApi {

	private static final YFCLogCategory logger = YFCLogCategory.instance(AcademyEGCCreateTaskQueueForAlerts.class);
	private Properties props;

	@Override
	public void setProperties(Properties props) throws Exception {
		this.props = props;

	}

	public Document manageTaskQueueForAlerts(YFSEnvironment env, Document docInp) throws Exception {

		logger.verbose(
				"Input - AcademyEGCManageTaskQForAlerts.manageTaskQForAlerts() :: " + XMLUtil.getXMLString(docInp));
		
		/* OMNI-94177 Start & End -- Added ExtnIsPromoItem='N' in nodelist to raise alert only for Non Free EGC Lines*/
		NodeList nlOrderLineEGC = XPathUtil.getNodeList(docInp,
				AcademyConstants.XPATH_NON_FREE_EGC_LINES);

		if (nlOrderLineEGC.getLength() > 0) {

			Document docOutGetCommonCodeList = AcademyBOPISUtil.getCommonCodeList(env,
					AcademyConstants.STR_EGC_PENDING_SHIP_CONFIRM_ALERT, AcademyConstants.PRIMARY_ENTERPRISE);

			String strReminderDuration = retrieveCodeShortDesc(docOutGetCommonCodeList,
					AcademyConstants.STR_EGC_PENDING_SHIP_CONFIRM_ALERT_SLA);

			String strCurrentTimestamp = getCurrentTimestampAsString();

			String strAvailableDate = addHoursToTime(strCurrentTimestamp, strReminderDuration);

			createTaskQueue(env, AcademyConstants.STR_EGC_SHIP_PENDING_TXN, strAvailableDate,
					docInp.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));

		}

		return docInp;
	}

	private void createTaskQueue(YFSEnvironment objEnv, String strTxnId, String strAvailDate,
			String strOrderHeaderKey) {
		try {

			Document docInManageTaskQueue = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
			Element eleInManageTaskQueue = docInManageTaskQueue.getDocumentElement();
			eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, strAvailDate);
			eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_TRANS_ID, strTxnId);
			eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY, strOrderHeaderKey);
			eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE, AcademyConstants.ATTR_ORDER_HEADER_KEY);

			logger.verbose("input - manageTaskQueue  API :: " + XMLUtil.getXMLString(docInManageTaskQueue));
			AcademyUtil.invokeAPI(objEnv, AcademyConstants.API_MANAGE_TASK_QUEUE, docInManageTaskQueue);

		} catch (Exception e) {
			logger.error("Exception caught -createTaskQueue() :: " + e);
		}
	}

	private String retrieveCodeShortDesc(Document docOutGetCommonCodeList, String strCodeValue) {

		Element eleCommonCode = null;

		String strCodeShortDesc = null;

		try {

			if (!YFCObject.isNull(docOutGetCommonCodeList)) {
				eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList,
						"/CommonCodeList/CommonCode[@CodeValue='" + strCodeValue + "']");
				strCodeShortDesc = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
			}
		} catch (Exception e) {
			logger.error("Exception caught - retrieveReminderInterval() :: " + e);
		}
		return strCodeShortDesc;
	}

	private String addHoursToTime(String strDate, String strHours) {
		logger.verbose("Begin - addHoursToTime() :: ");
		String strDateModified = null;
		try {
			if (!StringUtil.isEmpty(strDate)) {
				SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
				int iHours = Integer.parseInt(strHours);

				Date objDate = sdf.parse(strDate);

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(objDate);
				calendar.add(Calendar.HOUR_OF_DAY, iHours);

				strDateModified = sdf.format(calendar.getTime());
				logger.verbose("Modified date :: " + strDateModified);
			}

		} catch (Exception e) {
			logger.error("Exception caught - addHoursToTime() :: " + e);
		}
		logger.verbose("End - - addHoursToTime() :: ");
		return strDateModified;
	}

	private String getCurrentTimestampAsString() {

		String strCurrentTimestamp = null;
		try {

			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			strCurrentTimestamp = sdf.format(new Date());

		} catch (Exception e) {
			logger.error("Exception caught - getCurrentTimestampAsString() ::" + e);
		}

		return strCurrentTimestamp;
	}

}
