/**
 * 
 */
package com.academy.ecommerce.sterling.order.api;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @author C0027894
 *
 */
public class AcademyManageFreeItemTaskQueue implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyManageFreeItemTaskQueue.class.getName());

	@Override
	public void setProperties(Properties arg0) throws Exception {
	}

	public Document manageFreeItem(YFSEnvironment env, Document inDoc) {
		log.beginTimer("AcademyManageFreeItemTaskQueue :: manageFreeItem");

		try {
			Object strTxnObject = env.getTxnObject(AcademyConstants.FREE_ITEM_HOLD_AGENT_TXN_OBJ);
			if (!YFCCommon.isVoid(strTxnObject)) {
				Document inDocMngTaskq = (Document) strTxnObject;
				log.verbose("AcademyManageFreeItemTaskQueue :: manageFreeItem : TxnObject : "
						+ XMLUtil.getXMLString(inDocMngTaskq));
				AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_POST_FREEITEM_TASKQMSG_TO_Q,inDocMngTaskq);
			}

			Object strTxnObjectCancelOrder = env
					.getTxnObject(AcademyConstants.FREE_ITEM_HOLD_AGENT_CHANGE_ORDER_TXN_OBJ);
			if (!YFCCommon.isVoid(strTxnObjectCancelOrder)) {
				Document inDocCancelOrder = (Document) strTxnObjectCancelOrder;
				log.verbose("AcademyManageFreeItemTaskQueue::manageFreeItem : ChangeOrderTxnObject : "
						+ XMLUtil.getXMLString(inDocCancelOrder));
				AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_POST_FREEITEM_CANCEL_TO_Q,
						inDocCancelOrder);
			}

		} catch (YFSException e) {
			throw e;
		} catch (Exception e) {
			YFSException e1 = new YFSException(e.getMessage());
			throw e1;
		}

		log.endTimer("AcademyManageFreeItemTaskQueue :: manageFreeItem");
		return inDoc;
	}

	// OMNI-98085 Starts
	public static Document manageTaskQueueForCCReturn(YFSEnvironment env, Document inDoc) {

		log.beginTimer("AcademyManageFreeItemTaskQueue :: manageTaskQueueForCCReturn");
		log.verbose("Input to callManageTaskQ: " + XMLUtil.getXMLString(inDoc));
		// OMNI-98744 Starts
		String strOrderheaderKey = null;
		try {
			String strOrderType = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_TYPE);
			log.verbose("Order Type " + strOrderType);
			if (!YFCCommon.isVoid(strOrderType) && strOrderType.equals(AcademyConstants.STR_INSTORE_RETURN)) {
				strOrderheaderKey = XPathUtil.getString(inDoc.getDocumentElement(),
						AcademyConstants.XPATH_MONITOR_ORDER_HEADER_KEY_DERIVEDFROM);
			} else {
				strOrderheaderKey = XPathUtil.getString(inDoc.getDocumentElement(),
					AcademyConstants.XPATH_MONITOR_DERIVEDORDER_HEADER_KEY);
			}
			log.verbose("Order header key " + strOrderheaderKey);
			// OMNI-98744 ends
			if (!YFCObject.isVoid(strOrderheaderKey)) {
				log.verbose("Order header key " + strOrderheaderKey);
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
				String strCurrentDate = sdf.format(cal.getTime());

				Document inDocManageTaskQueue = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
				Element eleManageTaskQueue = inDocManageTaskQueue.getDocumentElement();
				eleManageTaskQueue.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, strCurrentDate);
				eleManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY, strOrderheaderKey);
				eleManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE, AcademyConstants.STR_ORDR_HDR_KEY);
				eleManageTaskQueue.setAttribute(AcademyConstants.ATTR_TRANS_ID,
						AcademyConstants.TRAN_ID_FREE_ITEM_HOLD);

				log.verbose("Input to the AcademyUpdateFreeItemHoldTaskqInQ Service: "
						+ XMLUtil.getXMLString(inDocManageTaskQueue));
				AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_POST_FREEITEM_TASKQMSG_TO_Q,
						inDocManageTaskQueue);
			}
		} catch (YFSException e) {
			throw e;
		} catch (Exception e) {
			YFSException e1 = new YFSException(e.getMessage());
			throw e1;
		}
		log.endTimer("AcademyManageFreeItemTaskQueue :: manageTaskQueueForCCReturn");
		return inDoc;

	}
}
