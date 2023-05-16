package com.academy.ecommerce.sterling.interfaces.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

import com.academy.util.common.AcademyUtil;


public class AcademyProcessErrorsForChainInvLoadAPI implements YIFCustomApi {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyProcessErrorsForChainInvLoadAPI.class);
	Document docInput=null;

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	public Document processExceptions(YFSEnvironment env, Document inDoc) {
		Element eleErroredItems = null;
		Document docCreateExceptionOutput = null;
		log.beginTimer(" Begining of AcademyProcessErrorsForChainInvLoadAPI-> processExceptions Api");
		this.docInput=inDoc;
		eleErroredItems = (Element) inDoc.getDocumentElement()
				.getElementsByTagName(AcademyConstants.ELE_UNPROCESSED_ITEMS).item(0);
		docCreateExceptionOutput = createAlertForInventoryLoadFailure(eleErroredItems,env);
		log.endTimer(" Ending of AcademyProcessErrorsForChainInvLoadAPI-> processExceptions Api");
		return docCreateExceptionOutput;

	}

	private Document createAlertForInventoryLoadFailure(Element eleErrorItems,YFSEnvironment env) {
		Document docCreateExceptionInput = null;
		Document docCreateExceptionOutput = null;
		Element eleInboxRefList = null;
		Element eleInboxRef = null;
		String strYantraMsgGrpId=null;
		String strExceptionValue=null;
		try {
			strYantraMsgGrpId=this.docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_YANTRA_MSG_GRP_ID);
			strExceptionValue=XMLUtil.getElementXMLString(eleErrorItems);
			docCreateExceptionOutput = XMLUtil
					.createDocument(AcademyConstants.ELE_INBOX);
			docCreateExceptionOutput.getDocumentElement()
					.setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG,
							AcademyConstants.STR_YES);
			docCreateExceptionOutput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ENTERPRISE_KEY,AcademyConstants.PRIMARY_ENTERPRISE);
			docCreateExceptionOutput.getDocumentElement()
					.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE,
							AcademyConstants.STR_CHAININV_LOAD_EXCPTN_TYPE);
			eleInboxRefList = docCreateExceptionOutput
					.createElement(AcademyConstants.ELE_INBOX_REF_LIST);
			XMLUtil.appendChild(docCreateExceptionOutput.getDocumentElement(),
					eleInboxRefList);
			eleInboxRef = docCreateExceptionOutput
					.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			XMLUtil.appendChild(eleInboxRefList, eleInboxRef);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_INBOX_REFKEY,
					strYantraMsgGrpId);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE,
					AcademyConstants.STR_EXCPTN_REF_VALUE);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME,
					AcademyConstants.STR_CHAININV_EXCPTN_REF);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE,
					strExceptionValue);
			docCreateExceptionOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_CREATE_EXCEPTION, docCreateExceptionOutput);

		}

		catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		return docCreateExceptionOutput;

	}

}
