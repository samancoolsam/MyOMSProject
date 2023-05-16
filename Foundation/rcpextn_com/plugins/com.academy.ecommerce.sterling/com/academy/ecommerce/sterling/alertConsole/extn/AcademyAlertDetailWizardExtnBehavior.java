package com.academy.ecommerce.sterling.alertConsole.extn;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.academy.ecommerce.sterling.util.XPathUtil;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;

public class AcademyAlertDetailWizardExtnBehavior extends
		YRCWizardExtensionBehavior {

	private String prevStatus=null;	
	
	
		@Override
	public void postSetModel(String namespace) {
			YRCPlatformUI.trace(" Name Sapce is : "+namespace);
		if ("INBOX".equalsIgnoreCase(namespace) && getModel(namespace) != null) {
			String newStatus = getModel(namespace).getAttribute("Status");
			if (prevStatus == null
					|| (prevStatus != null && !prevStatus.equals(newStatus))) {
				prevStatus = newStatus;
			}
		}
		
		setControlEditable("extn_txtAlertID", false);
		setControlEditable("extn_txtAlertType",false);
		super.postSetModel(namespace);
	}

	@Override
	public boolean preCommand(YRCApiContext apiContext) {
		
		String apiName = apiContext.getApiName();
		YRCPlatformUI.trace(" API Name is :"+apiName);
		if (apiName.equals("getExceptionDetails")) {
			Element inputXML = apiContext.getInputXml().getDocumentElement();
			inputXML.setAttribute("Title", "Alert Detail");
			YRCPlatformUI.trace(XMLUtil.getElementXMLString(inputXML));
		} else if (apiName.equals("changeException")) {
			Element inputXML = apiContext.getInputXml().getDocumentElement();
			YRCPlatformUI.trace(XMLUtil.getElementXMLString(inputXML));
			String updatedStatus = inputXML.getAttribute("Status");
			YRCPlatformUI.trace("Processing updated Exception status : "+updatedStatus);
			if (AcademyConstants.OPEN_ALERT.equals(prevStatus)
					&& AcademyConstants.WIP_ALERT.equals(updatedStatus)) {				
				//Element notes = XMLUtil.getChildElement(inputXML, "/Inbox/InboxNotesList/InboxNotes");
				try {
					NodeList nNotes = XPathUtil.getNodeList(inputXML, "/Inbox/InboxNotesList/InboxNotes");
					int iNotes = nNotes.getLength();
					if(iNotes<1){
						YRCPlatformUI.showError("Error","Please complete the notes field");
						return false;
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}

		}else if(apiName.equals("getExceptionDetailsRefresh")){
			YRCPlatformUI.trace(" Sucessfully committed chnages.. ");
			Element targetInbox = getTargetModel("Inbox");
			if(targetInbox != null){
				prevStatus = targetInbox.getAttribute("Status");
				YRCPlatformUI.trace("The current status of Exception is : "+prevStatus);
			}
		}

		return super.preCommand(apiContext);
	}

	@Override
	public IYRCComposite createPage(String arg0) {
		return null;
	}

	@Override
	public void pageBeingDisposed(String arg0) {

	}
	
	@Override
	public void validateComboField(String fieldName, String fieldValue) {
		YRCPlatformUI.trace(" Combo FieldName is : "+fieldName);
		if(fieldName.equals("cmbAlertStatus") && fieldValue != null){
			YRCPlatformUI.trace("Status while loading the page is : "+prevStatus);
			if(fieldValue.trim().length() > 0  && (prevStatus != null && fieldValue.equals("WIP"))){
				YRCPlatformUI.trace(" Modifying the of status to : "+fieldValue);
				/**
				 * populate the follow up date to  "Current date + 1 day"
				 */
				try {
					String prevFollowUp = getFieldValue("txtFollowUpDate");
					YRCPlatformUI.trace("Current Followup Date is : "+prevFollowUp);
					String pattern = "MM/dd/yyyy";
					SimpleDateFormat formattedDate = new SimpleDateFormat(pattern);
					if(!formattedDate.parse(prevFollowUp).after(new Date())){
						YRCPlatformUI.trace("Follow up date is previous or current date. Therfore, reset it to Current Date +24 hours ");
						Calendar cal = Calendar.getInstance();
						cal.add(Calendar.DATE, 1);
						YRCPlatformUI.trace(formattedDate.format(cal.getTime()));
						setFieldValue("txtFollowUpDate", formattedDate.format(cal.getTime()));
					}
					
				} catch (Exception e) {
					e.printStackTrace();					
				}
			}
		}
		super.validateComboField(fieldName, fieldValue);
	}	
	
}
