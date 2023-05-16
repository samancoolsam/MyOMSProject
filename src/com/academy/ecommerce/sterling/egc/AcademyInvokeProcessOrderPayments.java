package com.academy.ecommerce.sterling.egc;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.los.XMLUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/*##################################################################################
*
* Project Name                : EGC Release
* Module                      : OMS
* Author                      : CTS
* Date                        : 11-DEC-2020
* Description                 : This file implements the logic to 
* 								1. Invoke API processOrderPayments to settle EGC invoices.
* JIRA                        : OMNI-15425
*
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author                  Version#       Remarks/Description                     
* ---------------------------------------------------------------------------------
* 11-DEC-2020     CTS                      1.0            Initial version
* ###################################################################################*/

public class AcademyInvokeProcessOrderPayments implements YIFCustomApi {

	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyInvokeProcessOrderPayments.class);

	@Override
	public void setProperties(Properties arg0) throws Exception {

	}

	public Document invokeProcessOrderPayments(YFSEnvironment envObj, Document docInput) {

		try {

			logger.verbose("Begin - AcademyInvokeProcessOrderPayments :: ");

			logger.verbose("invokeProcessOrderPayments() - Input document :: " + XMLUtil.getString(docInput));

			String strOrderHeaderKey = docInput.getDocumentElement()
					.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);

			Document docProcessOrderPaymentsInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			Element eleOrder = docProcessOrderPaymentsInput.getDocumentElement();

			eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
			eleOrder.setAttribute(AcademyConstants.ATTR_IGNORE_COLLECTION_DATE, AcademyConstants.STR_YES);

			logger.verbose(
					"processOrderPayments API - Input document :: " + XMLUtil.getString(docProcessOrderPaymentsInput));

			AcademyUtil.invokeAPI(envObj, AcademyConstants.API_PROCESS_ORDER_PAYMENTS, docProcessOrderPaymentsInput);

			logger.verbose("End - AcademyInvokeProcessOrderPayments :: ");

		} catch (Exception e) {

			logger.error("Exception caught :: invokeProcessOrderPayments() :: " + e.getMessage());
		}

		return docInput;

	}

}
