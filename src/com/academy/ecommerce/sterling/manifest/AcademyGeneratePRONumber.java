package com.academy.ecommerce.sterling.manifest;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.ResourceUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.shared.ycp.YFSContext;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyGeneratePRONumber implements YIFCustomApi {
	Properties props;
	//String sStartRange = null;//STL-1690
	//String sEndRange = null;//STL-1690
	Document docGetPRONumberLisInput = null;
	Document docPROSeqNo = null;
	Document docCurrentPRONo = null;
	Element eleExtnPROSeqNo = null;
	String strCheckDigit = null;
	
	//private String strProNumber;

	public void setProperties(Properties props) {
		this.props = props;
	}

  /**
   	* Instance of logger
    */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyGeneratePRONumber.class);
	  
	public Document generatePRONumber(YFSEnvironment env, Document inDoc)
			throws Exception {
		
		Document docOutput = null;
		String strProNo = null;
		//STL-1690 Begin
		/* The PRONo is fetched from EXTN_CEVA_PRO_NO_SEQ which is a DB sequence. It's a 10 digit no. 
		 * CEVA provides us 9 digit start and end range , we increase it by 10 times and store in EXTN_CEVA_PRO_NO_SEQ sequence.
		 * Each generated PRONo would be in sequence
		 */
		try {
			log.beginTimer("Begining of AcademyGeneratePRONumber.generatePRONumber() ::");
			//sStartRange = props.getProperty("START_RANGE");
			//sEndRange = props.getProperty("END_RANGE");

			//if (!YFCObject.isVoid(sStartRange) && (!YFCObject.isVoid(sEndRange))) {
				
				//EFP-17 - Start
				String strScac = inDoc.getDocumentElement().getAttribute("SCAC");
			
				YFSContext oEnv = (YFSContext) env;
				
				if(AcademyConstants.CARRIER_CEVA.equalsIgnoreCase(strScac)){
					long lSeqCEVAProNo = oEnv.getNextDBSeqNo(AcademyConstants.STR_CEVA_PRO_NO_SEQ);
					strProNo = String.valueOf(lSeqCEVAProNo);
				}else if (AcademyConstants.CARRIER_EFW.equalsIgnoreCase(strScac)){
					long lSeqEFWProNo = oEnv.getNextDBSeqNo(AcademyConstants.STR_EFW_PRO_NO_SEQ);
					strProNo = String.valueOf(lSeqEFWProNo);
				}
				
				
				
				docOutput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
				docOutput.getDocumentElement().setAttribute(AcademyConstants.STR_CEVA_PRO_NO, strProNo);
			//}
			
/*				docGetPRONumberLisInput = XMLUtil
						.createDocument("ExtnPROSeqNo");
			docPROSeqNo = AcademyUtil.invokeService(env,
					"AcademyGetExtnPRONumberService", docGetPRONumberLisInput);
			Element eleExtnPROSeqNoList = docPROSeqNo.getDocumentElement();
			log.verbose("Element Retrieved is:"
					+ XMLUtil.getElementXMLString(eleExtnPROSeqNoList));
			if (eleExtnPROSeqNoList.hasChildNodes()) {
				eleExtnPROSeqNo = (Element) eleExtnPROSeqNoList
						.getElementsByTagName("ExtnPROSeqNo").item(0);
				String sExistingStartRange = eleExtnPROSeqNo
						.getAttribute("StartNo");
				String sExistingEndRange = eleExtnPROSeqNo
						.getAttribute("EndNo");
				int iExistingEndRange = Integer.valueOf(sExistingEndRange);
				if (sExistingStartRange.equals(sStartRange)) {
					String sCurrentPRONo = String.valueOf((Integer
							.valueOf(eleExtnPROSeqNo
									.getAttribute("CurrentPRONo")) + 1));
					if ((Integer.valueOf(eleExtnPROSeqNo
							.getAttribute("CurrentPRONo")) + 1) < iExistingEndRange) {
						docCurrentPRONo = persistRangeValues(env, sStartRange,
								sEndRange, sCurrentPRONo,
								AcademyConstants.STR_ACTION_MODIFY);
						calculatePRONumber(env, docCurrentPRONo);
						strProNumber = sCurrentPRONo.concat(strCheckDigit);
						checkBufferLimitForCurrentRangeNoAndRaiseAlert(env, sStartRange, sCurrentPRONo);
						docOutput = XMLUtil.createDocument("Shipment");
						docOutput.getDocumentElement().setAttribute(
								"ProNumber", strProNumber);
						return docOutput;
					}
				} else {
					String sCurrentPRONo = sStartRange;
					docCurrentPRONo = persistRangeValues(env, sStartRange,
							sEndRange, sCurrentPRONo,
							AcademyConstants.STR_ACTION_MODIFY);
					log.verbose("############# InvoiceNo document #### "
							+ XMLUtil.getXMLString(docCurrentPRONo));
					calculatePRONumber(env, docCurrentPRONo);
					strProNumber = sCurrentPRONo.concat(strCheckDigit);
					checkBufferLimitForCurrentRangeNoAndRaiseAlert(env, sStartRange, sCurrentPRONo);
					docOutput = XMLUtil.createDocument("Shipment");
					docOutput.getDocumentElement().setAttribute("ProNumber",
							strProNumber);
					return docOutput;
				}

			} else {
				String sCurrentPRONo = sStartRange;
				docCurrentPRONo = persistRangeValues(env, sStartRange,
						sEndRange, sCurrentPRONo,
						AcademyConstants.STR_ACTION_CREATE);
				log.verbose("############# InvoiceNo document #### "
						+ XMLUtil.getXMLString(docCurrentPRONo));
				calculatePRONumber(env, docCurrentPRONo);
				strProNumber = sCurrentPRONo.concat(strCheckDigit);
				docOutput = XMLUtil.createDocument("Shipment");
				docOutput.getDocumentElement().setAttribute("ProNumber",
						strProNumber);
				return docOutput;

			}
*/			//STL-1690 End
			log.endTimer("End of AcademyGeneratePRONumber.generatePRONumber() ::");
		} catch (Exception e) {
			
			throw new YFSException(e.getMessage());
		}
		return docOutput;

	}
//	STL-1690 Begin
	/*
	private void checkBufferLimitForCurrentRangeNoAndRaiseAlert(YFSEnvironment env, String endRange, String currentPRONo) 
	{
		log.beginTimer(" Begining of AcademyGeneratePRONumber-> checkBufferLimitForCurrentRangeNoAndRaiseAlert Api");

		String sAlertStartAt = props.getProperty("ALERT_START_AT");
		int iAlertStartAt = Integer.valueOf(sAlertStartAt);

		Document docExceptionInput = null;
		Element eleConsolidationTemplt = null;
		Element eleInboxConsolidationTemplt = null;
		//int iStartRange = Integer.valueOf(startRange);
		int iEndRange = Integer.valueOf(endRange);
		int icurrentPRONo = Integer.valueOf(currentPRONo);
		if (icurrentPRONo >= (iEndRange - iAlertStartAt)) //It was icurrentPRONo >= (iStartRange + iAlertStartAt)
		{
			try 
			{
				docExceptionInput = XMLUtil.createDocument(AcademyConstants.ELE_INBOX);
				docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG, AcademyConstants.STR_YES);
				docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CONSOLIDATE, AcademyConstants.STR_YES);
				docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE, "ACADEMY_PRO_NUMBER_BUFFER_LIMIT_EXCEEDED");
				eleConsolidationTemplt = docExceptionInput.createElement(AcademyConstants.ELE_CONSOLIDATE_TEMPLT);
				XMLUtil.appendChild(docExceptionInput.getDocumentElement(), eleConsolidationTemplt);
				eleInboxConsolidationTemplt = docExceptionInput.createElement(AcademyConstants.ELE_INBOX);
				//eleInboxConsolidationTemplt.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE, "");
				eleInboxConsolidationTemplt.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE, "ACADEMY_PRO_NUMBER_BUFFER_LIMIT_EXCEEDED");
				XMLUtil.appendChild(eleConsolidationTemplt, eleInboxConsolidationTemplt);

				// invoking service to raise an alert
				AcademyUtil.invokeService(env, AcademyConstants.RAISE_ALERT_ON_PRO_NO_RANGE_EXCEED_SERVICE, docExceptionInput);
			} 
			catch (Exception e) 
			{
				throw new YFSException(e.getMessage());
			}
		}
		log.endTimer(" End of AcademyGeneratePRONumber-> checkBufferLimitForCurrentRangeNoAndRaiseAlert Api");
	}

	private Document persistRangeValues(YFSEnvironment env, String startRange,
			String endRange, String currentPRONo, String action) {
		Document docCreatePRONoInput = null;
		Document docCreatePRONoOutput = null;

		YIFApi yifApi;
		YFSEnvironment envNew;
		try {
			log.beginTimer(" Begining of AcademyGeneratePRONumber-> persistRangeValues Api");
			yifApi = YIFClientFactory.getInstance().getLocalApi();
			Document docEnv = XMLUtil.createDocument(AcademyConstants.ELE_ENV);
			docEnv.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_USR_ID, env.getUserId());
			docEnv.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_PROG_ID, env.getProgId());
			envNew = yifApi.createEnvironment(docEnv);
			docCreatePRONoInput = XMLUtil.createDocument("ExtnPROSeqNo");
			docCreatePRONoInput.getDocumentElement().setAttribute("StartNo",
					startRange);
			docCreatePRONoInput.getDocumentElement().setAttribute("EndNo",
					endRange);
			docCreatePRONoInput.getDocumentElement().setAttribute(
					"CurrentPRONo", currentPRONo);
			
			if (AcademyConstants.STR_ACTION_CREATE.equals(action)) {
				docCreatePRONoOutput = AcademyUtil.invokeService(envNew,
						"AcademyCreateExtnPRONumberService",
						docCreatePRONoInput);
			} else {

				docCreatePRONoOutput = AcademyUtil.invokeService(envNew,
						"AcademyChangeExtnPRONumberService",
						docCreatePRONoInput);
			}
			log.endTimer(" End of AcademyGeneratePRONumber-> persistRangeValues Api");
		} catch (Exception e) {
						throw new YFSException(e.getMessage());
		}

		return docCreatePRONoOutput;

	}

	private String calculatePRONumber(YFSEnvironment env,
			Document docCurrentPRONo) {
		log.beginTimer(" Begining of AcademyGeneratePRONumber-> calculatePRONumber Api");
		Element eleCurrentPRONo = docCurrentPRONo.getDocumentElement();
		String sCurrentPRONo = eleCurrentPRONo.getAttribute("CurrentPRONo");
		double iCurrentPRONo = Double.valueOf(sCurrentPRONo);
		double iModCurrentPRONo = iCurrentPRONo / 7.00;
		long decCheckDigit = (long) ((iModCurrentPRONo - Math
				.floor(iModCurrentPRONo)) * 100);
		log.verbose("$$$$$$$$$$$ decCheckDigit " + decCheckDigit);
		String strdecCheckDigit = String.valueOf(decCheckDigit);
		Map<String, String> hm = (HashMap<String, String>) ResourceUtil.getCommonCodeList(env);
		strCheckDigit = hm.get(strdecCheckDigit);
		log.verbose("Check Digit Is:-" + strCheckDigit);
		log.endTimer(" End of AcademyGeneratePRONumber-> calculatePRONumber Api");
		return strCheckDigit;
	}
	*/
//	STL-1690 End
}
