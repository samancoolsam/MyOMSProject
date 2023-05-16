package com.academy.ecommerce.sterling.util.webservice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class InvokeRateShopService
{
	private static YFCLogCategory log = YFCLogCategory.instance(InvokeRateShopService.class);
	private Properties props;
	public void setProperties(Properties props)
	{
		this.props = props;

	}

	public Document invokeRateShopService(YFSEnvironment env, Document inDoc)
			throws Exception {
		log.beginTimer("invokeRateShopService");
		log.verbose("*** invoking RateShopService with input doc *** "
				+ XMLUtil.getXMLString(inDoc));
		String strNodeType = null;
		String strURL = null;

        // Codes changes for the Agile Upgrade. 
		//This logic is use to obtain the nodeType and retrieve the updated URL from properties file and invoke webservice. 
								
		//Start : OMNI-324/OMNI-326 : Changes for Agile Stabilization 
        Element eleNodeType = XMLUtil.getElementByXPath(inDoc, AcademyConstants.XPATH_PIERBRIDGE_RATE_SHOP_REQUEST_NODE_TYPE);
        if (eleNodeType != null) {
            strNodeType = eleNodeType.getTextContent();
        }
        log.verbose("NodeType :: " + strNodeType);
		if (!YFCObject.isVoid(strNodeType) && strNodeType.equals(AcademyConstants.ATTR_VAL_SHAREDINV_DC)) {
        	strURL = props.getProperty("URL_DC");
            log.verbose("URL for SharedInventoryDC :: " + strURL);
        }
	    else {
            strURL = props.getProperty("URL_Store");
            log.verbose("URL for Store :: " + strURL);
    	}
        if (inDoc.getDocumentElement().hasAttribute(AcademyConstants.ATTR_NODE_TYPE)) {
			inDoc.getDocumentElement().removeAttribute(AcademyConstants.ATTR_NODE_TYPE);
		}
		// End : OMNI-324/OMNI-326 : Changes for Agile Stabilization
		
		String xmlString = XMLUtil.getXMLString(inDoc);
		URL url1 = new URL(strURL);
		URLConnection conn = url1.openConnection();
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(xmlString);
		wr.flush();
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line1 = "";
		String line = "";
		while ((line = rd.readLine()) != null) {
			line1 = line1 + line;
		}
		wr.close();
		rd.close();

		Document outDoc = XMLUtil.getDocument(line1);
		log.verbose("*** output of RateShopService is *** "	+ XMLUtil.getXMLString(outDoc));
		log.endTimer("invokeRateShopService");
		return outDoc;
	}
}
