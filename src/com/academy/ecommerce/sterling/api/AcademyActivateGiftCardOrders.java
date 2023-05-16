package com.academy.ecommerce.sterling.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyActivateGiftCardOrders extends YCPBaseTaskAgent {



	@Override
	public Document executeTask(YFSEnvironment env, Document arg1)
			throws Exception {
		
		String taskQKey = arg1.getDocumentElement().getAttribute("TaskQKey");
		//input to execute task is as follows:
		/*<<TaskQueue TaskQKey="200911180316241261832">
    <TransactionFilters Action="Get" DocumentParamsKey="0001"
        DocumentType="0001" NumRecordsToBuffer="5000"
        ProcessType="ORDER_DELIVERY" ProcessTypeKey="ORDER_DELIVERY"
        TransactionId="GIFT_CARD_ACTIVATION.0001.ex" TransactionKey="20090803112615115720"/>
</TaskQueue>
*/
		Document outDoc = AcademyUtil.invokeService(env, "AcademyLoadGiftCardValidationService" , arg1);
		if (!YFCObject.isVoid(outDoc)){
			
			//call change shipment status to change the status of shipment.
			Document inShipment=XMLUtil.createDocument("Shipment");

	        Element rootElemShipment=inShipment.getDocumentElement();

	        rootElemShipment.setAttribute("AcceptOutOfSequenceUpdates", "Y");
	        rootElemShipment.setAttribute("TaskQKey",taskQKey );
	        AcademyUtil.invokeAPI(env, "changeShipmentStatus", inShipment);
			Document inXML=XMLUtil.createDocument(AcademyConstants.ELE_REG_PROCESS_COMP_INPUT);

	        Element rootElem=inXML.getDocumentElement();

	        rootElem.setAttribute(AcademyConstants.ATTR_KEEP_TASK_OPEN, "N");

	        Element currTaskElem=inXML.createElement(AcademyConstants.ELE_CURR_TASK);

	        currTaskElem.setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, taskQKey);

	        rootElem.appendChild(currTaskElem);

	        AcademyUtil.invokeAPI(env, AcademyConstants.API_REGISTER_PROCESS_COMPLETION, inXML);
	        
			return outDoc;
		}
		
		return arg1;
	}

}
