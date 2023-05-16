package com.academy.ecommerce.sterling.alertConsole.extn;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyConstants;
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.academy.ecommerce.sterling.util.XPathUtil;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCEditorInput;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCXmlUtils;
import com.yantra.yfc.rcp.internal.YRCApiCaller;

public class AcademyCreateAlertExtnBehavior extends YRCExtentionBehavior {
	
	private boolean isCsaGroup ;
	@Override
	public void init() {
		Document docGetCommonCodeListInput = YRCXmlUtils
				.createDocument(AcademyPCAConstants.ELE_COMMON_CODE);
		docGetCommonCodeListInput.getDocumentElement().setAttribute(
				AcademyPCAConstants.ATTR_CODE_TYPE, "MANUAL_EXPTN");
		Document commonCodeListDoc = invokeSyncAPI(docGetCommonCodeListInput,
				"getCommonCodeList");
		setExtentionModel(AcademyPCAConstants.ATTR_EXTN_COMMON_CODE_LIST,
				commonCodeListDoc.getDocumentElement());
		resetCreateAlertScreenFields();
		super.init();
	}
	
	@Override
	public void validateComboField(String fieldName, String fieldValue) {
		if("extn_AlertType".equalsIgnoreCase(fieldName)){
			String strAlertType = getFieldValue("extn_AlertType");
			Element eleCreateException = getModel("CreateException");
			if(!"".equals(strAlertType)&& fieldValue != null){
				eleCreateException.setAttribute("Priority", "2");
				repopulateModel("CreateException");
			}else{
				setFieldValue("txtPriority", "");
			}
		}
		
		super.validateComboField(fieldName, fieldValue);
	}
	
	@Override
	public YRCValidationResponse validateLinkClick(String fieldName) {
		if ("extn_AssociatedCustomerHyperLink".equals(fieldName)) {
			Element eInput = YRCXmlUtils.createDocument("Customer")
					.getDocumentElement();
			eInput.setAttribute("CustomerKey", "YCD001");
			YRCEditorInput newInput = new YRCEditorInput(eInput,
					new String[] { "CustomerKey" }, "YCD_TASK_CUSTOMER_SEARCH");
			YRCPlatformUI.openEditor(
					"com.yantra.pca.ycd.rcp.editors.YCDCustomerSearchEditor",
					newInput);
		}
		return super.validateLinkClick(fieldName);
	}

	@Override
	public boolean preCommand(YRCApiContext apiContext) {
		String apiName = apiContext.getApiName();
		if (apiName.equals("createException")) {
			Element inputElement = apiContext.getInputXml()
					.getDocumentElement();
			YRCPlatformUI.trace("########## input ########"
					+ XMLUtil.getElementXMLString(inputElement));
			/*** Fix for bug # 4607***Irrespective of what value is entered by the user on the Follow up date field, 
			 * on click of create button we will stamp the value as sysdate +1
			 */
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 1);
			String pattern = "yyyy-MM-dd";
			SimpleDateFormat formattedDate = new SimpleDateFormat(pattern);
			inputElement.setAttribute("FollowupDate", formattedDate.format(cal.getTime()));
			
			/*** End of fix for 4607***/

			if (!YRCPlatformUI.isVoid(inputElement)) {
				String associatedCustID = getFieldValue("extn_txtAssociatedCustomer");
				String strOrderHeaderKey = inputElement
						.getAttribute("OrderHeaderKey");
				/*
				 * If Customer Id field is populated then Stamp the value in the
				 * input to createException API
				 */
				if (!"".equals(associatedCustID)) {
					try {
						Document docCustomerList = getCustomerList(associatedCustID);
						Element eleCustomerList = docCustomerList
								.getDocumentElement();
						NodeList nCustomer = XPathUtil.getNodeList(docCustomerList, "CustomerList/Customer");
						int iCustomer = ((NodeList)nCustomer).getLength();
						if (iCustomer == 0) {
							YRCPlatformUI.trace("There is no valid customer");
							YRCPlatformUI.showError(AcademyPCAConstants.ATTR_ERROR,
									"Please enter valid Customer ID");
							return false;
						}
						
						String strCustomerKey = XPathUtil.getString(
								docCustomerList,
								"CustomerList/Customer/@CustomerKey");
						String strCustomerEmailID = XPathUtil
								.getString(docCustomerList,
										"CustomerList/Customer/CustomerContactList/CustomerContact/@EmailID");
						String strCustomerFName = XPathUtil
								.getString(docCustomerList,
										"CustomerList/Customer/CustomerContactList/CustomerContact/@FirstName");
						String strCustomerLName = XPathUtil
								.getString(docCustomerList,
										"CustomerList/Customer/CustomerContactList/CustomerContact/@LastName");
						Element extnElement = (Element) inputElement
								.getElementsByTagName(AcademyConstants.ELE_EXTN)
								.item(0);
						if (YRCPlatformUI.isVoid(extnElement)) {
							extnElement = inputElement.getOwnerDocument()
									.createElement(AcademyConstants.ELE_EXTN);
						}

						extnElement.setAttribute(
								AcademyConstants.ATTR_EXTN_ASSOCIATED_CUST_ID,
								associatedCustID);
						extnElement.setAttribute("ExtnCustomerKey",
								strCustomerKey);
						extnElement.setAttribute("ExtnCustomerEmailID",
								strCustomerEmailID);
						extnElement.setAttribute("ExtnCustomerFName",
								strCustomerFName);
						extnElement.setAttribute("ExtnCustomerLName",
								strCustomerLName);
						inputElement.appendChild(extnElement);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				/*
				 * if Order Header Key value is present, that means user has
				 * either associated Sales Order or Return Order to the alert.
				 * Hence, call getOrderList to find the SCAC on the order. If
				 * scac is Overnight or 2nd day set the priority to 1, else set
				 * the priority to 2
				 */
				int iPriorityLines = 0;
				int iGroundLines = 0;
				String strTotalAmount = "";
				if (!"".equals(strOrderHeaderKey)) {
					try {
						Document docGetOrderLineList = XMLUtil
								.createDocument("OrderLine");
						docGetOrderLineList.getDocumentElement().setAttribute(
								"OrderHeaderKey", strOrderHeaderKey);
						Document outGetOrderLineList = invokeSyncAPI(
								docGetOrderLineList, "getOrderLineList");
						YRCPlatformUI.trace("****Output of getOrderLineList is"
								+ XMLUtil.getXMLString(outGetOrderLineList));
						if (!YRCPlatformUI.isVoid(outGetOrderLineList)) {
							Element eleOrderLineList = outGetOrderLineList
									.getDocumentElement();
							strTotalAmount = getOrderTotal(eleOrderLineList);
							NodeList nOrderLine = eleOrderLineList
									.getElementsByTagName("OrderLine");
							for (int j = 0; j < nOrderLine.getLength(); j++) {
								Element eleOrderLine = (Element) nOrderLine
										.item(j);
								String strCarrierServiceCode = eleOrderLine
										.getAttribute("CarrierServiceCode");
								/*
								 * Check if the LOS is overnight or 2nd Day and
								 * if yes stamp the priority on the alert as 1
								 */
								if ((strCarrierServiceCode
										.contentEquals("2nd Day Air"))
										|| (strCarrierServiceCode
												.contentEquals("Next Day Air"))) {
									iPriorityLines = iPriorityLines + 1;
								}
								/*
								 * Check if the LOS is Ground and if yes stamp
								 * the priority on the alert as 2
								 */
								else if (strCarrierServiceCode
										.contentEquals("Ground")) {
									iGroundLines = iGroundLines + 1;
								}
							}
							if (iPriorityLines > 0) {
								inputElement.setAttribute("Priority", "1");
							} else {
								if ((iPriorityLines == 0) && (iGroundLines > 0)) {
									inputElement.setAttribute("Priority", "2");
								}
							}
						}
						/*
						 * Stamp the order total on the alert, for Price range
						 * functionality
						 */
						Element extnElement = (Element) inputElement
								.getElementsByTagName(AcademyConstants.ELE_EXTN)
								.item(0);
						if (YRCPlatformUI.isVoid(extnElement)) {
							extnElement = inputElement.getOwnerDocument()
									.createElement(AcademyConstants.ELE_EXTN);
						}
						extnElement.setAttribute("ExtnOrderTotal",
								strTotalAmount);
						inputElement.appendChild(extnElement);
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
					}
				}
				YRCPlatformUI
						.trace("*****Final createException Input is********"
								+ XMLUtil.getElementXMLString(inputElement));
			}

			resetCreateAlertScreenFields();
			YRCPlatformUI.trace("########## output Document ########"
					+ XMLUtil.getElementXMLString(inputElement));
		}

		return super.preCommand(apiContext);
	}

	private Document getCustomerList(String associatedCustID) {
		Document docCustomerList = null;
		try {
			Document docCustomer = XMLUtil.createDocument("Customer");
			docCustomer.getDocumentElement().setAttribute("CustomerID",
					associatedCustID);
			docCustomerList = invokeSyncAPI(docCustomer, "getCustomerList");
			YRCPlatformUI.trace("****Output of getCustomerList is *************"
					+ XMLUtil.getXMLString(docCustomerList));
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return docCustomerList;
		
	}

	private String getOrderTotal(Element eleOrderLineList) {
		String strTotalAmount = "";
		Element eleOrderLine = (Element) eleOrderLineList.getElementsByTagName(
				"OrderLine").item(0);
		Element eleOrder = (Element) eleOrderLine.getElementsByTagName("Order")
				.item(0);
		Element elePriceInfo = (Element) eleOrder.getElementsByTagName(
				"PriceInfo").item(0);
		strTotalAmount = elePriceInfo.getAttribute("TotalAmount");
		YRCPlatformUI.trace("*****Order Total is******" + strTotalAmount);
		return strTotalAmount;
	}

	@Override
	public void postCommand(YRCApiContext apiContext) {
		String[] strApiNames = apiContext.getApiNames();
		for (int i = 0; i < strApiNames.length; i++) {
			YRCPlatformUI
					.trace("***************Inside Post Command***************"
							+ strApiNames[i]);
			// System.out.println("API" + strApiNames[i]);
			if (strApiNames[i].equals("createException")) {
				Element eleCreateException = apiContext.getOutputXml()
						.getDocumentElement();
				YRCPlatformUI.trace("*****Output of createException********",
						XMLUtil.getElementXMLString(eleCreateException));
				Element eleExtn = (Element) eleCreateException
						.getElementsByTagName(AcademyPCAConstants.ATTR_EXTN)
						.item(0);
				String strAlertId = eleExtn.getAttribute("ExtnAlertKey");
				if (!"".equals(strAlertId)) {
					YRCPlatformUI.showInformation("Alert ID",
							"An alert has been created with alert id = '"
									+ strAlertId + "'", strAlertId);
				}
			}
		}
		super.postCommand(apiContext);
	}

	private void resetCreateAlertScreenFields() {
		setFieldValue("extn_txtAssociatedCustomer", "");
		disableField("extn_txtAssociatedCustomer");
		disableField("extn_AssociatedCustomerHyperLink");
		disableField("txtAssociatedReturnOrder");
		disableField("lnkAssociatedReturnOrder");
		enableField("txtAssociatedOrder");
		enableField("lnkAssociatedOrder");
		setFieldValue("btnAssociatedOrder", true);
		setFieldValue("extn_AlertType", "");
	}

	@Override
	public YRCValidationResponse validateButtonClick(String fieldName) {
		YRCPlatformUI.trace("Selected Field : " + fieldName);
		if (fieldName.equals("btnCreateAlert")) {
			String orderFieldValue = getFieldValue("txtAssociatedOrder");
			String returnOrderFieldValue = getFieldValue("txtAssociatedReturnOrder");
			String customerFieldValue = getFieldValue("extn_txtAssociatedCustomer");

			// Either one of Sales Order, Return Order or Customer should be
			// mandatory while creating an Alert
			if (orderFieldValue.trim().equals("")
					&& returnOrderFieldValue.trim().equals("")
					&& customerFieldValue.trim().equals("")) {
				String errorMessage = "Please enter the mandatory field(s).";
				YRCPlatformUI.showError(AcademyPCAConstants.ATTR_ERROR,
						errorMessage);
				return new YRCValidationResponse(
						YRCValidationResponse.YRC_VALIDATION_ERROR,
						errorMessage);
			}
		} else if (fieldName.equals("extn_btnAssociatedCustomer")) {
			enableField("extn_txtAssociatedCustomer");
			enableField("extn_AssociatedCustomerHyperLink");
			setFieldValue("txtAssociatedOrder", "");
			disableField("txtAssociatedOrder");
			disableField("lnkAssociatedOrder");
			setFieldValue("txtAssociatedReturnOrder", "");
			disableField("txtAssociatedReturnOrder");
			disableField("lnkAssociatedReturnOrder");
		} else {
			setFieldValue("extn_txtAssociatedCustomer", "");
			disableField("extn_txtAssociatedCustomer");
			disableField("extn_AssociatedCustomerHyperLink");
		}

		return super.validateButtonClick(fieldName);
	}

	private Document invokeSyncAPI(Document doc, String strAPIName) {
		YRCApiContext context = new YRCApiContext();
		YRCApiCaller syncapiCaller = new YRCApiCaller(context, true);
		context.setApiName(strAPIName);
		context.setFormId(getFormId());
		context.setInputXml(doc);
		syncapiCaller.invokeApi();
		Document outputXML = context.getOutputXml();
		return outputXML;
	}
	
	@Override
	public void postSetModel(String namespace) {
		// TODO Auto-generated method stub

		if (namespace.equals("CreateException")) {

			Element inputModel = getModel(namespace);
			YRCPlatformUI.trace("********* Input : "
					+ XMLUtil.getElementXMLString(inputModel));

			try {
				// Fetching the current date and incrementing 24 hrs (or next
				// day) and then setting back in the model
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, 1);
				String pattern = "MM-dd-yyyy";
				SimpleDateFormat formattedDate = new SimpleDateFormat(pattern);
				inputModel.setAttribute("FollowupDate", formattedDate
						.format(cal.getTime()));
				repopulateModel(namespace);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if ("UserNameSpace".equalsIgnoreCase(namespace)) {
			Element inputModel = getModel(namespace);
			YRCPlatformUI.trace("********* Input : "
					+ XMLUtil.getElementXMLString(inputModel));
			NodeList groupList = inputModel.getElementsByTagName("UserGroup");
			for (int i = 0; i < groupList.getLength(); i++) {
				Element userGroup = (Element) groupList.item(i);
				YRCPlatformUI.trace("Element is "
						+ XMLUtil.getElementXMLString(userGroup));
				String userGroupId = userGroup.getAttribute("UsergroupId");
				YRCPlatformUI.trace("userGroupId " + userGroupId);
				if ("CSA".equalsIgnoreCase(userGroupId)) {
					isCsaGroup = true;
					break;
				}
			}
		}

		if (isCsaGroup) {
			Element inputModel = getModel("QueueList");
			if (inputModel != null) {
				NodeList queueList = inputModel.getElementsByTagName("Queue");
				if (queueList != null) {
					for (int i = 0; i < queueList.getLength(); i++) {
						Element queue = (Element) queueList.item(i);
						String queueId = queue.getAttribute("QueueId");
						if (!"ACADEMY_ASSOCIATE_QUEUE"
								.equalsIgnoreCase(queueId)) {
							queue.getParentNode().removeChild(queue);
							i--;
						}
					}
					repopulateModel(namespace);
				}
			}
		}
		super.postSetModel(namespace);
	}
}
// TODO Validation required for a Radio control: extn_btnAssociatedCustomer
// TODO Validation required for a Combo control: extn_AlertType
// TODO Validation required for a Link control: extn_AssociatedCustomerHyperLink
// TODO Validation required for a Button control: btnCreateAlert
// TODO Validation required for a Text control: extn_txtAssociatedCustomer
// TODO Validation required for a Radio control: btnAssociatedOrder
// TODO Validation required for a Radio control: btnAssociatedReturnOrder
