package com.academy.ecommerce.sterling.sourcing;
/**#########################################################################################
*
* Project Name                : STS
* Module                      : OMNI-45614
* Author                      : CTS
* Author Group				  : CTS - POD
* Date                        : 13-Aug -2021 
* Description				  : This class is to Traverse through order line shedules and canclled least fulfill qty order lines schedules
*								for STS2.0  order
* 								Cancel all the least possible order line schedules qty to prevent having more than 1 TO
* 								 Resolve the hold							  								 
* ------------------------------------------------------------------------------------------------------------------------
* Date            	Author 		        			Version#       	Remarks/Description                      
* ------------------------------------------------------------------------------------------------------------------------
* 13-Aug-2021		CTS	 	 			 1.0           		Initial version
*
* #########################################################################################################################*/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyProcessSTSTOSourcing implements YIFCustomApi{

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyProcessSTSTOSourcing.class);
	
	
	public void processSTSTOSourcing(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("START -- AcademyProcessSTSTOSourcing.processSTSTOSourcing " + SCXmlUtil.getString(inDoc));
		log.beginTimer("END -- AcademyProcessSTSTOSourcing.processSTSTOSourcing");
		String sInOrderHeaderKey = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
		if(!YFCCommon.isVoid(sInOrderHeaderKey)) {
		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, AcademyConstants.TEMP_GETORDERLIST_STS2_SOURCECONTROL);
		Document outGetOrderList = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_ORDER_LIST, inDoc);
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
		Map<String, Integer> mpSchLineKey_Qty = new HashMap<String, Integer>();
		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(outGetOrderList);
		YFCElement yfcInEle = yfcInDoc.getDocumentElement();
		YFCElement yfcInOrder = yfcInEle.getChildElement(AcademyConstants.ELE_ORDER);
		String sOrderHeaderKey = yfcInOrder.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);

		YFCDocument yfcInChangeOdr = YFCDocument.createDocument("Order");
		YFCElement yfcEleInChangeOdr = yfcInChangeOdr.getDocumentElement();
		yfcEleInChangeOdr.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);
		yfcEleInChangeOdr.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.A_ACADEMY_DIRECT);
		yfcEleInChangeOdr.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY_UPPR);
		yfcEleInChangeOdr.setAttribute(AcademyConstants.ATTR_SELECT_METHOD, AcademyConstants.STR_WAIT);
		yfcEleInChangeOdr.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, sOrderHeaderKey);

		YFCElement yfcOrderLines = yfcInOrder.getChildElement(AcademyConstants.ELEM_ORDER_LINES);
		for (Iterator<YFCElement> ite = yfcOrderLines.getChildren(AcademyConstants.ELEM_ORDER_LINE).iterator(); ite.hasNext();) {
			YFCElement yfcOrderLine = ite.next();

			boolean bAcademy_STS_TOLine = verifyOrderLinehold(yfcOrderLine);
			if (bAcademy_STS_TOLine) {
				YFCElement yfcSchedules = yfcOrderLine.getChildElement(AcademyConstants.ELE_SCHEDULES);
				for (Iterator<YFCElement> iteSch = yfcSchedules.getChildren(AcademyConstants.ELE_SCHEDULE).iterator(); iteSch.hasNext();) {
					YFCElement yfcSchedule = iteSch.next();
					String sOrderLineScheduleKey = yfcSchedule.getAttribute(AcademyConstants.ATTR_ORDER_LINE_SCHEDULE_KEY);
					String sQuantity = yfcSchedule.getAttribute(AcademyConstants.ATTR_QUANTITY);
					double dQuantity = Double.parseDouble(sQuantity);
					int iQuantity = (int) dQuantity;
					mpSchLineKey_Qty.put(sOrderLineScheduleKey, iQuantity);
				}
				prepareDocforcancellation(yfcOrderLine, mpSchLineKey_Qty, yfcEleInChangeOdr);
				mpSchLineKey_Qty.clear();
			}

		}
		
		/* OMNI-50491 Reason Code Inventory Shortage - Start */
		yfcEleInChangeOdr.setAttribute(AcademyConstants.ATTR_MOD_REASON_CODE,AcademyConstants.LACK_OF_INVENTORY) ;
		/* OMNI-50491 Reason Code Inventory Shortage - End */
		try {
			log.verbose("Final changeOrder input :: " + XMLUtil.getXMLString(yfcInChangeOdr.getDocument()));
			// START Setting a transaction object for STS2.0 order OMNI_50491
			env.setTxnObject(AcademyConstants.STS2_SCHEDULE_CANCEL,AcademyConstants.STR_YES);
			// END Setting a transaction object for STS2.0 order OMNI_50491
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, yfcInChangeOdr.getDocument());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 
		log.endTimer("END -- AcademyProcessSTSTOSourcing.processSTSTOSourcing");
	}
	
	
	private boolean verifyOrderLinehold(YFCElement yfcOrderLine) {
		YFCElement yfcOrderHoldTypes = yfcOrderLine.getChildElement(AcademyConstants.ELE_ORDER_HOLD_TYPES);
		for (Iterator<YFCElement> iteHoldTypes = yfcOrderHoldTypes.getChildren(AcademyConstants.ELE_ORDER_HOLD_TYPE).iterator(); iteHoldTypes
				.hasNext();) {
			YFCElement yfcLineHolds = iteHoldTypes.next();
			if (AcademyConstants.STR_STS20_HOLD_TYPE.equalsIgnoreCase(yfcLineHolds.getAttribute(AcademyConstants.ATTR_HOLD_TYPE))
				&&(AcademyConstants.STR_HOLD_CREATED_STATUS.equalsIgnoreCase(yfcLineHolds.getAttribute(AcademyConstants.STATUS)))) {
				return true;
			}
			/**
			if (AcademyConstants.STR_STS20_HOLD_TYPE.equalsIgnoreCase(yfcLineHolds.getAttribute(AcademyConstants.ATTR_HOLD_TYPE))) {
				return true;
			}
				**/
		}
		return false;
	}


	private YFCElement prepareDocforcancellation(YFCElement yfcOrderLine, Map<String, Integer> mpSchLineKey_Qty,YFCElement yfcEleInChangeOdr) {
		log.verbose("START -- AcademyProcessSTSTOSourcing.prepareDocforcancellation");
		   List<Entry<String, Integer>> list_asc = new ArrayList<>(mpSchLineKey_Qty.entrySet());
		   log.verbose("before sorting"+ list_asc);
		   list_asc.sort(Entry.comparingByValue());
		   list_asc.remove(list_asc.size()-1);
		   log.verbose("after sorting"+ list_asc);
		log.verbose("sorted in asc :: "+ list_asc);
		String sOrderLineKey = yfcOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
		String sOrderedQty = yfcOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);

		double doOrderedQty = Double.parseDouble(sOrderedQty);
		YFCElement yfcEleInChangeOdrLines;
		if (!yfcEleInChangeOdr.hasChildNodes()) {
			yfcEleInChangeOdrLines = yfcEleInChangeOdr.createChild(AcademyConstants.ELE_ORDER_LINES);
		} else {
			yfcEleInChangeOdrLines = yfcEleInChangeOdr.getChildElement(AcademyConstants.ELE_ORDER_LINES);

		}
		
		YFCElement yfcEleInChangeOdrLine = yfcEleInChangeOdrLines.createChild(AcademyConstants.ELE_ORDER_LINE);	
		
		YFCElement yfcEleSchedules = yfcEleInChangeOdrLine.createChild(AcademyConstants.ELE_SCHEDULES);
		for(int iCount=0;iCount<list_asc.size();iCount++) {
			Entry<String, Integer> sData1 = list_asc.get(iCount);
			String sOrderLineScheduleKey = sData1.getKey();
			Integer iQuantity = sData1.getValue();
			double dQuantity = Double.valueOf(iQuantity);
			doOrderedQty = doOrderedQty- dQuantity;
			YFCElement yfcEleSchedule  = yfcEleSchedules.createChild(AcademyConstants.ELE_SCHEDULE);
			yfcEleSchedule.setAttribute(AcademyConstants.ATTR_ORDER_LINE_SCHEDULE_KEY, sOrderLineScheduleKey);
			yfcEleSchedule.setAttribute(AcademyConstants.ATTR_CHANGE_IN_QTY, -((int)dQuantity));
		}
		
		yfcEleInChangeOdrLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, sOrderLineKey);
		yfcEleInChangeOdrLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, (int)doOrderedQty);
		YFCElement yfcEleOrderHoldType = yfcEleInChangeOdrLine.createChild(AcademyConstants.ELE_ORDER_HOLD_TYPES).createChild(AcademyConstants.ELE_ORDER_HOLD_TYPE);
		yfcEleOrderHoldType.setAttribute(AcademyConstants.ATTR_HOLD_TYPE, AcademyConstants.STR_STS20_HOLD_TYPE);
		yfcEleOrderHoldType.setAttribute(AcademyConstants.ATTR_STATUS, AcademyConstants.HOLD_RESOLVE_STATUS);
		
		return yfcEleInChangeOdr;
	}


	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}


}
