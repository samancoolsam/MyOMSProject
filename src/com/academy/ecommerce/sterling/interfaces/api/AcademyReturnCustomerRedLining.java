/**
 * 
 */
package com.academy.ecommerce.sterling.interfaces.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;

/**
 * @author JParameswaran
 * 
 * Logic is performed in this class to get the exact Drop Ship item for which
 * inventory is going to be loaded based on GTIN and also to prepare the input
 * to call necessary Sterling APIs to process inventory data
 */
public class AcademyReturnCustomerRedLining implements YIFCustomApi {

	/**
	 * Variable to store input document
	 */
	Document docInput = null;

	/**
	 * Variable to store output document that'll be returned
	 */
	Document docOutput = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yantra.interop.japi.YIFCustomApi#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyReturnCustomerRedLining.class);

	public void updateRedLiningCustomers(YFSEnvironment env,
			Document UpdateRedLiningDoc) {
		log
				.beginTimer(" Begining of AcademyReturnCustomerRedLining-> updateRedLiningCustomers Api");
		try {
			log
					.verbose("*************** Inside AcademyReturnCustomerRedLining->updateRedLiningCustomers of Return Interface & Input XML is :"
							+ XMLUtil.getXMLString(UpdateRedLiningDoc));

			String emilId = UpdateRedLiningDoc.getDocumentElement()
					.getAttribute("EmailId");
			Document customerDoc = XMLUtil
					.createDocument(AcademyConstants.ELE_CUSTOMER);
			Document manageCustomerDoc = XMLUtil
					.createDocument(AcademyConstants.ELE_CUSTOMER);
			Element eleCustContactList = customerDoc
					.createElement(AcademyConstants.ELE_CUSTOMER_CONTACT_LIST);
			Element eleCustContact = customerDoc
					.createElement(AcademyConstants.ELE_CUSTOMER_CONTACT);
			eleCustContact.setAttribute("EmailID", emilId);
			Element eleCustomerDoc = customerDoc.getDocumentElement();
			eleCustContactList.appendChild((Node) eleCustContact);
			eleCustomerDoc.appendChild((Node) eleCustContactList);
			env.setApiTemplate(AcademyConstants.API_GET_CUST_LIST,
					"global/template/api/getCustomerList.xml");
			if (!YFCObject.isVoid(customerDoc))
				log
						.verbose("*************** Calling getCustomerList API with input************ "
								+ XMLUtil.getXMLString(customerDoc));
			Document customerListDoc = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_CUST_LIST, customerDoc);

			Element eleCustomerList = (Element) customerListDoc
					.getElementsByTagName(AcademyConstants.ELE_CUSTOMER)
					.item(0);
			Element rootElement = manageCustomerDoc.getDocumentElement();
			XMLUtil
					.copyElement(manageCustomerDoc, eleCustomerList,
							rootElement);
			String extnCustRedLineCount = ((Element) customerListDoc
					.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0))
					.getAttribute("ExtnCustRedLineCount");
			int increaseExtnCustRedLineCount = (int) (Double
					.parseDouble(extnCustRedLineCount));
			increaseExtnCustRedLineCount = increaseExtnCustRedLineCount + 1;
			((Element) manageCustomerDoc.getElementsByTagName(
					AcademyConstants.ELE_EXTN).item(0)).setAttribute(
					"ExtnCustRedLineCount", Integer
							.toString(increaseExtnCustRedLineCount));

			Element eleNoteList = ((Element) manageCustomerDoc
					.getElementsByTagName("NoteList").item(0));
			Element eleNote = manageCustomerDoc
					.createElement(AcademyConstants.ELE_NOTE);

			String notesText = "ReturnOrderNo : "
					+ UpdateRedLiningDoc.getDocumentElement().getAttribute(
							"ExistedReturnOrdNo")
					+ "       OriginalReturnReasonCode: "
					+ UpdateRedLiningDoc.getDocumentElement().getAttribute(
							"ExistedReturnReasonCode")
					+ "     CLSReturnReasonCode : "
					+ UpdateRedLiningDoc.getDocumentElement().getAttribute(
							"ClsReturnReason");
			eleNote.setAttribute("NoteText", notesText);
			log
					.verbose(" Notes Text which will get added to Customer profile as part of RedLining is "
							+ notesText);
			int seqNo = Integer.parseInt((eleNoteList
					.getAttribute("TotalNumberOfRecords")));
			eleNote.setAttribute("SequenceNo", Integer.toString(seqNo + 1));
			eleNoteList.appendChild((Node) eleNote);
			env.clearApiTemplate(AcademyConstants.API_GET_CUST_LIST);
			Document docGetRedLineCodesInput = XMLUtil
					.createDocument(AcademyConstants.ELE_COMMON_CODE);
			docGetRedLineCodesInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_CODE_TYPE, "REDLINE_LIMIT");
			Document docGetRedLineCodesOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_COMMON_CODELIST,
					docGetRedLineCodesInput);
			log.verbose(" output of CommonCode List APi is "
					+ XMLUtil.getXMLString(docGetRedLineCodesOutput));
			String redLineThresholdLimit = ((Element) docGetRedLineCodesOutput
					.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE)
					.item(0))
					.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
			if (increaseExtnCustRedLineCount >= Integer
					.parseInt(redLineThresholdLimit))
				((Element) manageCustomerDoc.getElementsByTagName(
						AcademyConstants.ELE_EXTN).item(0)).setAttribute(
						"ExtnIsRedLined", AcademyConstants.STR_YES);
			log
					.verbose("*************** before calling manage Customer Api  input XML is :"
							+ XMLUtil.getXMLString(manageCustomerDoc));

			AcademyUtil.invokeAPI(env, "manageCustomer", manageCustomerDoc);
			log
					.verbose("*************** exiting AcademyReturnCustomerRedLining->updateRedLiningCustomers of Return Interface ************ ");

		}

		catch (Exception e) {
			e.printStackTrace();
			// Currently if we doesn't find any corresponding Customer Profile
			// for Redlining we are not throwing any Exception & to avoid
			// failing return Receiving Process.
			// throw new YFSException(e.getMessage());
		}
		log
				.endTimer(" End of AcademyReturnCustomerRedLining-> updateRedLiningCustomers Api");
	}

	/**
	 * 
	 * @param env -
	 *            Environment variable for the transaction
	 * 
	 * This method will get the exact item information from GTIN obtained from
	 * input and will prepare the output document in a format required by
	 * Sterling APIs for Inventory load
	 */

}
