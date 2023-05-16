package com.academy.ecommerce.sterling.userexits;

// Other imports
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

//Sterling imports
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSExtnPaymentCollectionInputStruct;
import com.yantra.yfs.japi.YFSExtnPaymentCollectionOutputStruct;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSCollectionOthersUE;

//Package imports
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;


/**
 * 
 * @author psomashekar-tw
 *
 */

public class AcademyYFSCollectionOthersUEImpl implements YFSCollectionOthersUE {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyYFSCollectionOthersUEImpl.class);
	
	/**
	 * 
	 */
	public YFSExtnPaymentCollectionOutputStruct collectionOthers(
			YFSEnvironment env, YFSExtnPaymentCollectionInputStruct pcInputStruct)
			throws YFSUserExitException {
		Document outDoc = null;
		try {
			log.beginTimer(" Begining of AcademyYFSCollectionOthersUEImpl -> collectionOthers  Api");
			YFCDocument docInputForService = YFCDocument.createDocument("RefundTransactionReq");
			YFCElement eleInputForService = docInputForService.getDocumentElement();
			eleInputForService.setAttribute("orderNo", pcInputStruct.orderNo);
			
			if(YFSObject.isVoid(pcInputStruct.authorizationId)) {
				String apiName = "getOrderDetails";
				String outTemplate = "<Order OrderHeaderKey='' OrderNo=''>" +
						"<PaymentMethods><PaymentMethod PaymentKey='' PaymentType='' />" +
						"</PaymentMethods><ChargeTransactionDetails>" +
						"<ChargeTransactionDetail AuthorizationID='' ChargeTransactionKey='' ChargeType='' OrderHeaderKey='' PaymentKey='' Status=''/>" +
						"</ChargeTransactionDetails></Order>";
				
				YFCDocument outTemplateDoc =  YFCDocument.getDocumentFor(outTemplate);
				
				env.setApiTemplate(apiName, outTemplateDoc.getDocument());
				Document inDoc = YFCDocument.getDocumentFor("<Order OrderHeaderKey=''><PaymentMethods><PaymentMethod PaymentType='PAYPAL'/></PaymentMethods></Order>").getDocument();
				inDoc.getDocumentElement().setAttribute("OrderHeaderKey", pcInputStruct.orderHeaderKey);
				Document orderDetailOutDoc = AcademyUtil.invokeAPI(env, apiName, inDoc);
				env.clearApiTemplate(apiName);
				String paymentKey = XPathUtil.getString(orderDetailOutDoc, "/Order/PaymentMethods/PaymentMethod/@PaymentKey");
				Node chargeTranDetail = XPathUtil.getNode(orderDetailOutDoc, "/Order/ChargeTransactionDetails/ChargeTransactionDetail[@PaymentKey='"+ paymentKey + "']");
				pcInputStruct.authorizationId = ((Element)chargeTranDetail).getAttribute("AuthorizationID");
			}
			eleInputForService.setAttribute("authorizationId", pcInputStruct.authorizationId);
			eleInputForService.setAttribute("currency", pcInputStruct.currency);
			if(pcInputStruct.requestAmount < 0) {
				pcInputStruct.requestAmount = pcInputStruct.requestAmount * (-1);
			}
			eleInputForService.setAttribute("requestAmount", pcInputStruct.requestAmount);
			log.verbose("****************** Input Document :::::" + XMLUtil.getXMLString(docInputForService.getDocument()));
			
			outDoc = AcademyUtil.invokeService(env,	
					AcademyConstants.ACADEMY_PAYPAL_PAYMENT_SERVICE, docInputForService.getDocument());
			
			log.verbose("****************** Output Document after calling custom service :::::"
							+ XMLUtil.getXMLString(outDoc));

			YFSExtnPaymentCollectionOutputStruct pcOutputStruct = new YFSExtnPaymentCollectionOutputStruct();
			
			pcOutputStruct.authorizationId = outDoc.getDocumentElement().getAttribute("authorizationId");
			pcOutputStruct.authReturnMessage = outDoc.getDocumentElement().getAttribute("authReturnMessage");
			pcOutputStruct.tranAmount = Double.parseDouble(outDoc.getDocumentElement().getAttribute("tranAmount"));
			log.endTimer(" End of AcademyYFSCollectionOthersUEImpl -> collectionOthers  Api");
			return pcOutputStruct;
			
		} 
		catch (YFSUserExitException yfsue) {
			throw yfsue;
		}
		catch (Exception e) {
			throw getYFSUserExceptionWithTrace(e);
		}
	}
	

	
	/**
	 * Method creates YFSUserExitException object with error code and error description
	 * from the document 
	 * @param outDoc
	 * @return YFSUserExitException
	 */
	private static YFSUserExitException getYFSUserExitExceptionWithErrorCode(final Document outDoc) {
		final YFSUserExitException yfsUEException = new YFSUserExitException();
		yfsUEException.setErrorCode(outDoc.getDocumentElement().getAttribute("ErrorCode"));
		yfsUEException.setErrorDescription(outDoc.getDocumentElement().getAttribute("LongMessage"));
		return yfsUEException;
	}
	
	/**
	 * Method wraps Exception object to YFSUserExitException object
	 * @param e
	 * @return YFSUserExitException 
	 */
	private static YFSUserExitException getYFSUserExceptionWithTrace(Exception e) {
		YFSUserExitException yfsUEException = new YFSUserExitException();
		yfsUEException.setStackTrace(e.getStackTrace());
		return yfsUEException;
	}
}
