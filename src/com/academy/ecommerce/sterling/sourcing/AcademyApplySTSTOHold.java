package com.academy.ecommerce.sterling.sourcing;
/**#########################################################################################
*
* Project Name                : STS
* Module                      : OMNI-45614
* Author                      : CTS
* Author Group				  : CTS - POD
* Date                        : 13-Aug -2021 
* Description				  : This class is to  traverse through on success event XML of schedule Order and apply Academy_STS_TO hold for STS2.0  
* 								 lines which is getting sourced from more than one node										  								 
* ------------------------------------------------------------------------------------------------------------------------
* Date            	Author 		        			Version#       	Remarks/Description                      
* ------------------------------------------------------------------------------------------------------------------------
* 13-Aug-2021		CTS	 	 			 1.0           		Initial version
*
* #########################################################################################################################*/
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyApplySTSTOHold implements YIFCustomApi {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyApplySTSTOHold.class);
	private Properties props;

	public Document applySTSTOHold(YFSEnvironment env, Document inDoc) {
		log.debug("Input to AcademyApplySTSTOHold" + XMLUtil.getElementXMLString(inDoc.getDocumentElement()));
		try {
			Element inEle = inDoc.getDocumentElement();
			//finding all the STS lines on the order
			List<Node> nlOrderLine = XMLUtil.getElementListByXpath(inDoc,AcademyConstants.STR_STS20_XPATH_ALL_STS_ORDERLINE);
			//finding all the STS 1.0 lines on the order
			List<Node> nlOrderLineSTS1 = XMLUtil.getElementListByXpath(inDoc,AcademyConstants.STR_STS20_XPATH_STS10_ORDERLINE_ONLY);
			//removing STS 1.0 lines from STS lines
			nlOrderLine.removeAll(nlOrderLineSTS1);
			
			if (nlOrderLine.size() > 0) {
				String sOrderHeaderKey = inEle.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
				String sOrderNo = inEle.getAttribute(AcademyConstants.ATTR_ORDER_NO);
				Document docInChangeOdr = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
				Element eleInChangeOdr = docInChangeOdr.getDocumentElement();
				eleInChangeOdr.setAttribute(AcademyConstants.ATTR_SELECT_METHOD, AcademyConstants.STR_WAIT);
				eleInChangeOdr.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);
				eleInChangeOdr.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, sOrderHeaderKey);

				Element eleChangeOdrOrderLines = docInChangeOdr.createElement(AcademyConstants.ELE_ORDER_LINES);
				for (int iCount = 0; iCount < nlOrderLine.size(); iCount++) {
					Element eleOrderLine = (Element) nlOrderLine.get(iCount);
					String sProcureFromNode = eleOrderLine.getAttribute(AcademyConstants.ATTR_PROCURE_FROM_NODE);
					String sFulfillmentType = eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
					String sOrderLineKey = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);

					if (YFCCommon.isVoid(sProcureFromNode) && AcademyConstants.STR_SHIP_TO_STORE.equalsIgnoreCase(sFulfillmentType)) {

						Element eleOrderLineStatuses = (Element) eleOrderLine.getElementsByTagName(AcademyConstants.ELE_ORDER_STATUSES).item(0);
						
						log.debug("eleOrderLineStatuses:" + XMLUtil.getElementXMLString(eleOrderLineStatuses));
						
						
						String seleOrderStatuses = XMLUtil.getElementXMLString(eleOrderLineStatuses);
						Document docOrderStatuses = YFCDocument.getDocumentFor(seleOrderStatuses).getDocument();
						log.debug("docOrderStatuses:" + XMLUtil.getElementXMLString(docOrderStatuses.getDocumentElement()));
						
						/*verifying if the line has more than 1 statuses in 2160 and ProcureFromNode At Status is NOT BLANK,prepare input
						apply hold for that order line  */
						if ((XMLUtil.getElementListByXpath(docOrderStatuses,
								AcademyConstants.STR_STS20_XPATH_ORDERLINE_STATUS)).size() > 1) {
							
							Element eleChangeOrderOrderLine = docInChangeOdr.createElement(AcademyConstants.ELE_ORDER_LINE);
							eleChangeOrderOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, sOrderLineKey);
							Element eleChangeOrderOrderLineHoldType = docInChangeOdr
									.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPES);
							Element eleChangeOdrOrderLineHt = docInChangeOdr
									.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPE);
							eleChangeOrderOrderLineHoldType.appendChild(eleChangeOdrOrderLineHt);
							eleChangeOdrOrderLineHt.setAttribute(AcademyConstants.ATTR_HOLD_TYPE, AcademyConstants.STR_STS20_HOLD_TYPE);
							eleChangeOdrOrderLineHt.setAttribute(AcademyConstants.ATTR_STATUS, AcademyConstants.STR_HOLD_CREATED_STATUS);
							log.debug("eleChangeOrderOrderLineHoldType:" + XMLUtil.getElementXMLString(eleChangeOrderOrderLineHoldType));
							eleChangeOrderOrderLine.appendChild(eleChangeOrderOrderLineHoldType);
							log.debug("eleChangeOrderOrderLine :" + XMLUtil.getElementXMLString(eleChangeOrderOrderLine));
							eleChangeOdrOrderLines.appendChild(eleChangeOrderOrderLine);
						}

					}
				}
				eleInChangeOdr.appendChild(eleChangeOdrOrderLines);
				log.debug("Input to Change Order API for Applying STS2.0 HOLD" + XMLUtil.getXMLString(docInChangeOdr));
				List<Node> nlchgOrderLine = XMLUtil.getElementListByXpath(docInChangeOdr,AcademyConstants.STR_STS20_XPATH_APPLY_HOLD_STS_ORDERLINE);
				if (nlchgOrderLine.size() > 0) {
					log.debug("Invoking Change Order API for Applying STS2.0 HOLD" + XMLUtil.getXMLString(docInChangeOdr));
					AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, docInChangeOdr);
				    /** inPut doc to queue**/
					Document inDocToQueue = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
					Element eleInToQueue = inDocToQueue.getDocumentElement();
					eleInToQueue.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
					eleInToQueue.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.A_ACADEMY_DIRECT);
					eleInToQueue.setAttribute(AcademyConstants.ATTR_ORDER_NO, sOrderNo);
					eleInToQueue.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, sOrderHeaderKey);
					log.debug("Input to queue" + XMLUtil.getXMLString(inDocToQueue));
					AcademyUtil.invokeService(env,"AcademySTSSourceControlInQ", inDocToQueue);
				}
			}

		} catch ( Exception e) {
			e.printStackTrace();
		}
		log.debug("Output to AcademyApplySTSTOHold" + XMLUtil.getElementXMLString(inDoc.getDocumentElement()));
		return inDoc;
	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

}
