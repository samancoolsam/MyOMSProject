package com.academy.ecommerce.sterling.email.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.email.util.AcademyEmailUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyReturnPushNotification implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyReturnPushNotification.class);
	private Properties props;
	/**
	 * This method customizes input xml to post Push Notification xml message to ESB queue
	 * 
	 * @param inDoc
	 * @return inDoc
	 * @throws Exception
	 */
	public Document preparePNContent(YFSEnvironment env, Document inDoc) {
		log.verbose("AcademyReturnPushNotification.preparePNContent() start :" + XMLUtil.getXMLString(inDoc));
		try {
			String strMsgIdProd = props.getProperty(AcademyConstants.RETURN_PN_MSG_ID_PROD);
            String strMsgIdStage = props.getProperty(AcademyConstants.RETURN_PN_MSG_ID_STAGE);
            String strMsgType = props.getProperty(AcademyConstants.RETURN_PN_MSG_TYPE);
			
			Element eleIndoc = inDoc.getDocumentElement();
			//Code Changes for OMNI-83687 -- Start
			String strSOOrderNo=SCXmlUtil.getXpathAttribute(eleIndoc, "OrderLines/OrderLine/DerivedFromOrder/@OrderNo");
			eleIndoc.setAttribute(AcademyConstants.ATTR_ORDER_NO, strSOOrderNo);
			//Code Changes for OMNI-83687 -- End
			AcademyEmailUtil.updateMessageRef(env, eleIndoc, strMsgIdProd, strMsgIdStage, strMsgType);
		} catch (Exception e) {
			e.printStackTrace();
			log.verbose("Exception in AcademyReturnPushNotification " + XMLUtil.getXMLString(inDoc));
		}
		log.verbose("AcademyReturnPushNotification.preparePNContent() end :" + XMLUtil.getXMLString(inDoc));
		return inDoc;

	}	

	public void setProperties(Properties props) {
		this.props = props;
	}
}
