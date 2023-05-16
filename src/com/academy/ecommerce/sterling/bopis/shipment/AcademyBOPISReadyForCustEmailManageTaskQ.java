package com.academy.ecommerce.sterling.bopis.shipment;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.sof.shipment.AcademySOFAcquisitionDisposition;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;


/**
 * @Author Sumit Arora
 * @Date Created JULY 16th 2018
 * 
 * @Purpose
 * 1.Call manageTaskQueue - For BOPIS Shipment, we will have to send 'Ready for Pickup Email'. To follow the existing email process, we insert record into TaskQ table. ACADEMY_EMAIL_AGENT_SERVER sends out the emails
  **/

public class AcademyBOPISReadyForCustEmailManageTaskQ implements YIFCustomApi{

	
	/**
	 * Preparing input for manageTaskQueue API
	 * @param strTaskQDataKey
	 * @return docInManageTaskQueue
	 * @throws Exception
	 */
	public Document prepareManageTaskQueueInput(YFSEnvironment env, Document inXML)
			throws Exception {
		Document docInManageTaskQueue = null;
		Element eleInManageTaskQueue = null;
		String strTransactionKey = null;
		String strTaskQDataKey=null;
		
		strTaskQDataKey=inXML.getDocumentElement().getAttribute("ShipmentNo");

		strTransactionKey = new AcademySOFAcquisitionDisposition().getTransactionKey(env, AcademyConstants.SOF_RDYFORCUST_PICKUP_EMAIL_TRAN_ID);

		docInManageTaskQueue = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
		eleInManageTaskQueue = docInManageTaskQueue.getDocumentElement();
		eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY, strTaskQDataKey);
		eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE, AcademyConstants.ATTR_SHIPMENT_NO);
		eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_TRANS_ID, AcademyConstants.SOF_RDYFORCUST_PICKUP_EMAIL_TRAN_ID);
		eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_TRANS_KEY, strTransactionKey);
		
		return docInManageTaskQueue;
		
	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
