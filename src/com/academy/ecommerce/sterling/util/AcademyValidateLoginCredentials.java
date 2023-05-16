package com.academy.ecommerce.sterling.util;


import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.academy.ecommerce.sterling.util.AcademyServiceUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyValidateLoginCredentials implements YIFCustomApi {
	private Properties props;

	/**Create by Netai Dey
	 * STL-1499. This class is use to validate login details. This method will return the same input doc if login success, else return Error message.
	 * Instance of logger
	 * 
	 */
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyValidateLoginCredentials.class);

	public void setProperties(Properties props) {
		this.props = props;
	}

	/**This method is use to validate login details. This method will return the same input doc if login success, else return Error message.
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document validateLoginCredentials(YFSEnvironment env, Document inDoc)
	throws Exception {
		log.verbose("*** AcademyValidateLoginCredentials.validateLoginCredentials() call with input:" + XMLUtil.getXMLString(inDoc));

		Document docResponse = null;
		String strLoginID = null;
		String strPassword = null;
		Element rootElement = inDoc.getDocumentElement();

		Element eleLogin = (Element) rootElement.getElementsByTagName(AcademyConstants.ELE_LOGIN).item(0);
		if(!YFSObject.isVoid(eleLogin)){
			strLoginID = eleLogin.getAttribute(AcademyConstants.ATTR_LOGIN_ID);
			strPassword = eleLogin.getAttribute(AcademyConstants.ATTR_PASSWORD);
			docResponse=AcademyUtil.validateLoginCredentials(env,strLoginID, strPassword);
		}
		
		if (YFCObject.isVoid(docResponse)) {
			docResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
			docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_ERROR_CODE,AcademyConstants.LOGIN_ERROR_CODE);
			docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_ERROR_DESC, AcademyConstants.LOGIN_ERROR_DESC);
		}
		else{
			docResponse = inDoc;
		}
		log.verbose("*** AcademyValidateLoginCredentials.validateLoginCredentials() Output *** " +	XMLUtil.getXMLString(docResponse));

		return docResponse;
	}
}
