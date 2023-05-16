package com.academy.ecommerce.sterling.shipment;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class is used to modify the flow of Pack slip printing for Sterling 9.5 instance.
 * @author Abhishek Aggarwal
 *
 */
public class AcademyBOPISModifyPackSlipFlowForStore 
{
	private static final YFCLogCategory	log		= YFCLogCategory.instance(AcademyBOPISModifyPackSlipFlowForStore.class);
	/**
	 * This method is used to make separate call to flow "AcademyGetDataForPackSlip" instead of using printDocumentSet
	 * for web store i.e. sterling 9.5 instance.
	 * If the printing is initiated from 9.2 instance, the class will directly call printDocumentSet api.
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document modifySFSPrintFlowForStore(YFSEnvironment env,Document inDoc) throws Exception
	{
		Document outDocPrintDocSet = null;
		String strIWebStoreFlow = (String) env.getTxnObject(AcademyConstants.A_IS_WEB_STORE_FLOW);
		String strIsWebStoreBulkFlow = inDoc.getDocumentElement().getAttribute("IsWebStoreBulkFlow");
		if(YFCCommon.equalsIgnoreCase("Y", strIWebStoreFlow) || YFCCommon.equalsIgnoreCase("Y",strIsWebStoreBulkFlow))
		{
			log.verbose("--------Pack Slip Printing from Web Store--------");
			YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
			if(log.isVerboseEnabled()){
			log.verbose("AcademyBOPISModifyPackSlipFlowForStore.modifySFSPrintFlowForStore : InDoc "+ yfcInDoc.toString());
			}
			YFCElement elePrintDocuments = yfcInDoc.getDocumentElement();
			if(!YFCCommon.isVoid(strIsWebStoreBulkFlow))
			{
				elePrintDocuments.removeAttribute("IsWebStoreBulkFlow");
			}
			YFCElement elePrintDocument = elePrintDocuments.getChildElement("PrintDocument");
			String strBeforeChildrenLabelFormatId = elePrintDocument.getAttribute("BeforeChildrenLabelFormatId");
			String strBeforeChildrenPrintDocumentId = elePrintDocument.getAttribute("BeforeChildrenPrintDocumentId");
			YFCElement elePrintPreference = elePrintDocument.getChildElement("PrinterPreference");
			String strWorkStationId = elePrintPreference.getAttribute("WorkStationId");
			YFCElement eleLabelPreference = elePrintDocument.getChildElement("LabelPreference");
			String strBuyerOrganizationCode = eleLabelPreference.getAttribute("BuyerOrganizationCode");
			
			
			
			YFCElement eleInputData = elePrintDocument.getChildElement("InputData");
			String strFlowName = eleInputData.getAttribute("FlowName");
			YFCElement eleContainer = eleInputData.getChildElement("Container");
			YFCDocument inDocFlow = YFCDocument.getDocumentFor(eleContainer.toString());
			if(log.isVerboseEnabled()){
			log.verbose("AcademyBOPISModifyPackSlipFlowForStore.modifySFSPrintFlowForStore : inDocFlow "+ inDocFlow.toString());
			}
			Document outDocFlow = AcademyUtil.invokeService(env,strFlowName, inDocFlow.getDocument());
			if(log.isVerboseEnabled()){
			log.verbose("AcademyBOPISModifyPackSlipFlowForStore.modifySFSPrintFlowForStore : outDocFlow "+ outDocFlow);
			}
			YFCDocument yfcOutDocFlow = YFCDocument.getDocumentFor(outDocFlow);
			YFCElement eleAcademyMergedDocument = yfcOutDocFlow.getDocumentElement();
			eleInputData.removeChild(eleContainer);
			eleInputData.importNode(eleAcademyMergedDocument);
			eleInputData.removeAttribute("FlowName");
			
			String strLabelFormatID = getValueFromXpath(strBeforeChildrenLabelFormatId,yfcOutDocFlow.getDocument());
			String strPrintDocumentId = getValueFromXpath(strBeforeChildrenPrintDocumentId,yfcOutDocFlow.getDocument());
			if(YFCCommon.isVoid(strPrintDocumentId) || strPrintDocumentId.contains("xml:/"))
			{
				YFCException ex = new YFCException("ACAD_INVALID_MANDATORY_PARAMETER");
				ex.setErrorDescription("Invalid BeforeChildrenPrintDocumentId." + strPrintDocumentId);
				throw ex;
			}
			String strWorkStId = getValueFromXpath(strWorkStationId,yfcOutDocFlow.getDocument());
			String strBuyerOrgCode = getValueFromXpath(strBuyerOrganizationCode,yfcOutDocFlow.getDocument());
			elePrintDocument.setAttribute("BeforeChildrenLabelFormatId",strLabelFormatID);
			elePrintDocument.setAttribute("BeforeChildrenPrintDocumentId",strPrintDocumentId);
			elePrintPreference.setAttribute("WorkStationId", strWorkStId);
			eleLabelPreference.setAttribute("BuyerOrganizationCode", strBuyerOrgCode);
			if(log.isVerboseEnabled()){
			log.verbose("AcademyBOPISModifyPackSlipFlowForStore.modifySFSPrintFlowForStore : yfcInDoc printDocumentSet "+ yfcInDoc.toString());
			}
			System.out.println("input to printDocumentSet from web store"+yfcInDoc.toString());
			outDocPrintDocSet = AcademyUtil.invokeAPI(env,"printDocumentSet", yfcInDoc.getDocument());
		}
		else
		{
			log.verbose("--------Pack Slip Printing from RCP--------");
			if(log.isVerboseEnabled()){
			log.verbose("AcademyBOPISModifyPackSlipFlowForStore.modifySFSPrintFlowForStore : yfcInDoc printDocumentSet "+ inDoc);
			}
			outDocPrintDocSet = AcademyUtil.invokeAPI(env,"printDocumentSet", inDoc);
		}
		return outDocPrintDocSet;
	}
	/**
	 * This method returns the xpath value of the document passed.
	 * @param strXpath
	 * @param inDoc
	 * @return
	 * @throws XPathExpressionException
	 */
	public String getValueFromXpath(String strXpath,Document inDoc) throws XPathExpressionException
	{
		if(strXpath.contains(":"))
		{
			String strValue = XMLUtil.getAttributeFromXPath(inDoc,splitStringForXpath(strXpath));
			if(YFCCommon.isVoid(strValue))
			{
				return strXpath;
			}
			else
			{
				return strValue;
			}
		}
		else
		{
			return strXpath;
		}
	}
	/**
	 * This method removes the string "xml:" from each xpath.
	 * @param strValue
	 * @return
	 */
	public String splitStringForXpath(String strValue)
	{
		String strSplitOnColon[] = strValue.split(":");
		return strSplitOnColon[1];
	}
}
