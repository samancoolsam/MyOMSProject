package com.academy.ecommerce.sterling.api;

//Java Import
import java.util.List;
import java.util.Properties;

// Misc import
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

//Sterling import
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

//Package import
import com.academy.ecommerce.sterling.util.AcademyPaymentProcessingUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;

public class AcademyEncriptMultipleCCInfo implements YIFCustomApi {


	/**
     * Log variable.
     */
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyEncriptMultipleCCInfo.class);
	
	/**
     * Instance to store the properties configured for the condition in Configurator.
     */
    private Properties props;
	
    /**
     * Method to set props variable. Inherited from YIFCustomApi interface
     */
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}
	
	public Document encriptMultipleCC(YFSEnvironment env, Document inDoc) throws YFSException {
		log.verbose("****************** Input Document :::::" + XMLUtil.getXMLString(inDoc));
		log.beginTimer(" Begining of AcademyEncriptMultipleCCInfo  - >encriptMultipleCC() Api");
		Element inputElement = inDoc.getDocumentElement();
		List<Element> paymentMethodList = XMLUtil.getElementsByTagName(inputElement, 
				AcademyConstants.ELE_PAYMENT_METHOD);
		for(Element elePaymentMethod: paymentMethodList) {
			if(AcademyConstants.CREDIT_CARD.equalsIgnoreCase(elePaymentMethod.getAttribute(
					AcademyConstants.ATTR_PAYMENT_TYPE))) {
				try {
					Document docEncriptedCCInfo = AcademyUtil.invokeService(env, 
							AcademyConstants.ACADEMY_ENCRYPT_CC, XMLUtil.getDocumentForElement(elePaymentMethod));
					if(!YFSObject.isVoid(docEncriptedCCInfo) && 
							"Y".equals(XPathUtil.getString(docEncriptedCCInfo, "PaymentMethod/@HasError"))) {
						throw AcademyPaymentProcessingUtil.getYFSExceptionWithErrorCode(docEncriptedCCInfo);
					}
					elePaymentMethod.setAttribute(AcademyConstants.CREDIT_CARD_NO, 
							docEncriptedCCInfo.getDocumentElement().getAttribute(
									AcademyConstants.ATTRIBUTE_WALLET_ID));
				} catch (DOMException exception) {
					throw AcademyPaymentProcessingUtil.getYFSExceptionWithTrace(exception);
				} catch (YFSException yfsException) {
					throw yfsException;
				} catch (Exception exception) {
					throw AcademyPaymentProcessingUtil.getYFSExceptionWithTrace(exception);
				}
			}
		}
		log.endTimer(" End of AcademyEncriptMultipleCCInfo  - >encriptMultipleCC() Api");
		return inDoc;
	}
}
