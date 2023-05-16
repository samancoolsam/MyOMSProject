package com.academy.ecommerce.sterling.userexits;

// Misc imports
import org.w3c.dom.Document;

// Package imports
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;

//Sterling imports
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSGetFundsAvailableUE;

/**
 * Class invokes external interface to get the Funds available against a Gift Card.
 * This implements YFSGetFundsAvailableUE user exit.
 * @author psomashekar-tw
 * 
 */

public class AcademyYFSGetFundsAvailableUEImpl implements YFSGetFundsAvailableUE {

	/**
     * Log variable.
     */
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyYFSGetFundsAvailableUEImpl.class);
	
	/**
	 * Method invokes AcademyGiftCardLookupService and gets funds available against
	 * a gift card.
	 * @param env
	 * @param inDoc
	 * @return Document
	 */
	public Document getFundsAvailable(YFSEnvironment env, Document inDoc) throws YFSUserExitException {
		Document outDoc;
		log.beginTimer(" Begining of AcademyYFSGetFundsAvailableUEImpl -> getFundsAvailable  Api");
		log.verbose("****************** Input Document :::::" + XMLUtil.getXMLString(inDoc));
		try {	
			outDoc = AcademyUtil.invokeService(env,	AcademyConstants.ACADEMY_GIFT_CARD_LOOKUP_SERVICE, inDoc);
		} catch (Exception e) {
			log.error(e);
			throw getYFSUserExceptionWithTrace(e);
		}
		log.verbose("****************** Output Document after calling custom service :::::"
				+ XMLUtil.getXMLString(outDoc));

		/**
		 * If service response has error code and error description, throw YFSUserExitException.
		 * These error code and description will be displayed in RCP UI and also logged. 
		 */
		if(!YFCObject.isVoid(outDoc.getDocumentElement().getAttribute("ErrorCode"))) {
			log.verbose(AcademyConstants.ACADEMY_GIFT_CARD_LOOKUP_SERVICE + " service returned " +
					"with Error Code:" + outDoc.getDocumentElement().getAttribute("ErrorCode") + 
					" and Error Description " + 
					outDoc.getDocumentElement().getAttribute("ErrorDescription"));
			throw getYFSExceptionWithErrorCode(outDoc);
		}
		log.endTimer(" End of AcademyYFSGetFundsAvailableUEImpl -> getFundsAvailable  Api");
		return outDoc;
	}
	
	/**
	 * Method wraps Exception object to YFSUserExitException object
	 * @param e
	 * @return YFSUserExitException 
	 */
	private static YFSException getYFSUserExceptionWithTrace(final Exception exception) {
		final YFSException yfsException = new YFSException();
		yfsException.setStackTrace(exception.getStackTrace());
		return yfsException;
	}

	/**
	 * Method creates YFSUserExitException object with error code and error description
	 * from the document 
	 * @param outDoc
	 * @return YFSUserExitException
	 */
	private static YFSException getYFSExceptionWithErrorCode(final Document outDoc) {
		final YFSException yfsException = new YFSException();
		yfsException.setErrorCode(outDoc.getDocumentElement().getAttribute("ErrorCode"));
		yfsException.setErrorDescription(outDoc.getDocumentElement().getAttribute("ErrorDescription"));
		return yfsException;
	}
}
