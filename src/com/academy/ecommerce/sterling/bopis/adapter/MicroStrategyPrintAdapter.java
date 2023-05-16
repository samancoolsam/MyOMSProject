package com.academy.ecommerce.sterling.bopis.adapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.shared.ycp.print.YCPPrintAdapter;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * This class is used to invoke and handle MSTR Request / Responses for Print and Re-print functionalities of HIP Printer
 * 
 * @author Nandhini Selvaraj
 */

public class MicroStrategyPrintAdapter implements YCPPrintAdapter {

	private static final YFCLogCategory log = YFCLogCategory.instance(MicroStrategyPrintAdapter.class);

	/**
	 * This method is used to get the MSTR URL and invoke a URL connection with MSTR
	 */
	
	public void print(YFSEnvironment env, YFCDocument inDoc) throws YFSException {

		Document inXML = inDoc.getDocument();
		log.beginTimer(this.getClass() + ".print");
		log.verbose("Entering the method MicroStrategyPrintAdapter.print ");

		try {

			log.debug("Input XML for MicroStrategyPrintAdapter.print ==> " + XMLUtil.getXMLString(inXML));

			Node nlPrintDocuments = XPathUtil.getNode(inXML, AcademyConstants.ELE_PRINT);
			String strPrinterIP = XPathUtil.getString(nlPrintDocuments,
					AcademyConstants.XPATH_PRINT_PRINTERIP);

			String strValueAnswers = XPathUtil.getString(nlPrintDocuments,
					AcademyConstants.XPATH_PRINT_VALUE_ANSWERS);

			String strEndPointUrl = YFSSystem.getProperty(AcademyConstants.ATTR_MSTR_ENDPOINT_URL_KEY);

			if (!YFCCommon.isVoid(strEndPointUrl)) {
				strEndPointUrl = strEndPointUrl.replace(AcademyConstants.ATTR_MSTR_VALUE_ANSWERS, strValueAnswers);
				strEndPointUrl = strEndPointUrl.replace(AcademyConstants.ATTR_MSTR_PRINTER, strPrinterIP);
				postHttp(env, AcademyConstants.STR_POST, strEndPointUrl);
			} else {
				log.error("Error Code : " + AcademyConstants.STR_EXTN_ACADEMY_20
						+ "\nError Message : " + AcademyConstants.STR_MSTR_EMPTY_URL);
			}
		} catch (Exception e) {
			log.error("Error Code : " + AcademyConstants.STR_EXTN_ACADEMY_20
					+ "\nException : " + e);
			e.printStackTrace();
		}
		
		log.verbose("Exiting the method MicroStrategyPrintAdapter.print");
		log.endTimer(this.getClass() + ".print");
	}
	
	/**
	 * This method is used to post the HTTP request to MST and handle the response and exceptions
	 */

	private Document postHttp(YFSEnvironment env, String requestMethod, String strEndPointUrl) throws Exception {

		log.beginTimer(this.getClass() + ".postHttp");
		log.verbose("Entering the method MicroStrategyPrintAdapter.postHttp ");

		HttpURLConnection httpCon = null;
		PrintWriter prWriter = null;
		BufferedReader buffReader = null;
		String strOutput = null;
		Document docYfcOutput = null;

		int connectTimeOut = Integer.valueOf(YFSSystem.getProperty(AcademyConstants.ATTR_MSTR_CONNECT_TIMEOUT));
		int readTimeOut = Integer.valueOf(YFSSystem.getProperty(AcademyConstants.ATTR_MSTR_READ_TIMEOUT));

		URL url = null;

		try {
			url = new URL(strEndPointUrl);
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod(requestMethod);
			httpCon.setRequestProperty(AcademyConstants.STR_CONTENT_LENGTH, "100");
			httpCon.setConnectTimeout(connectTimeOut);
			httpCon.setReadTimeout(readTimeOut);

			prWriter = new PrintWriter(httpCon.getOutputStream());
			prWriter.println(requestMethod);
			prWriter.flush();

			int iResponseCode = httpCon.getResponseCode();

			if (iResponseCode == 200) {

				buffReader = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
				strOutput = getResponseData(buffReader);
				YFCDocument docOutput = YFCDocument.getDocumentFor(strOutput);
				Document MSTROutpDoc = docOutput.getDocument();
				Element eleOutput = MSTROutpDoc.getDocumentElement();
				String statusCode = eleOutput.getAttribute(AcademyConstants.ATTR_MSTR_STATUS_CODE);

				if (statusCode.equals(AcademyConstants.STATUS_CODE_200)) {
					log.verbose("MicroStrategyPrintAdapter.postHttp Request URL for MSTR : \n" + strEndPointUrl
							+ "\nMicroStrategyPrintAdapter.postHttp Response from MSTR : \n"
							+ XMLUtil.getXMLString(MSTROutpDoc));
				} else {
					log.error("MicroStrategyPrintAdapter.postHttp Request URL for MSTR : \n" + strEndPointUrl
							+ "\nMicroStrategyPrintAdapter.postHttp Response from MSTR : \n"
							+ XMLUtil.getXMLString(MSTROutpDoc) + "\nMicroStrategyPrintAdapter.postHttp Error Code : "
							+ AcademyConstants.STR_EXTN_ACADEMY_20);
				}
			} else {
				log.error("MicroStrategyPrintAdapter.postHttp Request URL for MSTR : \n" + strEndPointUrl
						+ "\nMicroStrategyPrintAdapter.postHttp Error Code : " + AcademyConstants.STR_EXTN_ACADEMY_20
						+ "\nError Message : " + httpCon.getResponseMessage());
			}
		} catch (Exception e) {
			log.error("MicroStrategyPrintAdapter.postHttp Request URL for MSTR : \n" + strEndPointUrl
					+ "\nMicroStrategyPrintAdapter.postHttp Error Code : " + AcademyConstants.STR_EXTN_ACADEMY_20
					+ "\nError Message : " + AcademyConstants.STR_MSTR_ERROR_MSG
					+ "\nException : " + e);
			e.printStackTrace();
			

		}
		
		finally {
			if (httpCon != null) {
				httpCon.disconnect();
				if (prWriter != null)
					prWriter.close();
				if (buffReader != null)
					buffReader.close();
			}
		}
		log.verbose("Exiting the method MicroStrategyPrintAdapter.postHttp ");

		log.endTimer(this.getClass() + ".postHttp");
		return docYfcOutput;

	}
	
	/**
	 * This method is used to get the response data in String format
	 */

	public String getResponseData(BufferedReader reader) throws IOException {

		log.beginTimer(this.getClass() + ".getResponseData");
		String strline;
		StringBuffer outputBuffer = new StringBuffer("");
		while ((strline = reader.readLine()) != null) {
			outputBuffer.append(strline);
		}

		log.endTimer(this.getClass() + ".getResponseData");
		return outputBuffer.toString();
	}
}
