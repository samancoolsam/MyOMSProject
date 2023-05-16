package com.academy.ecommerce.sterling.alertConsole.extn;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.XMLUtil;
import com.academy.ecommerce.sterling.util.XPathUtil;
import com.yantra.yfc.rcp.YRCEditorInput;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCXmlUtils;

public class AcademyAlertDetailsCompositeExtnBehavior extends YRCExtentionBehavior
{
	@Override
	public void postSetModel(String namespace)
	{
		if(namespace.equals("Inbox"))
		{
			Element inputModel = getModel(namespace);
			YRCPlatformUI.trace("********* Input : " + XMLUtil.getElementXMLString(inputModel));
			String createdByUserId = "";
			String associatedCustomerId = "";
			String creationDate="";
			try
			{
				createdByUserId = XPathUtil.getString(inputModel, "/Inbox/@Createuserid");
				associatedCustomerId = XPathUtil.getString(inputModel, "/Inbox/Extn/@ExtnAssociatedCustID");
				creationDate = XPathUtil.getString(inputModel, "/Inbox/@Createts");
				String s = creationDate.substring(0,19);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
				Date date = sdf.parse(s);
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
				setFieldValue("extn_lblAlertCreationDateValue", formatter.format(date));
			
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			YRCPlatformUI.trace("********* alert created by : " + createdByUserId);
			YRCPlatformUI.trace("********* associated customer id : " + associatedCustomerId);
			setFieldValue("extn_txtAlertCreatedByValue", createdByUserId);

			if(associatedCustomerId != null && !associatedCustomerId.trim().equals(""))
			{
				setControlVisible("extn_lblAssociatedCustId", true);
				setFieldValue("extn_lnkAssociatedCustID", associatedCustomerId);
			}
			else
			{
				setControlVisible("extn_lblAssociatedCustId", false);
			}

			setControlEditable("extn_txtAlertCreatedByValue", false);
			setControlEditable("extn_lnkAssociatedCustID", false);

		}

		super.postSetModel(namespace);
	}

	@Override
	public YRCValidationResponse validateLinkClick(String fieldName) {
		if("extn_lnkAssociatedCustID".equals(fieldName)){
			try {
				Element eleInbox = getModel("Inbox");
				String strCustomerKey="";
				strCustomerKey = XPathUtil.getString(eleInbox, "/Inbox/Extn/@ExtnCustomerKey");
				Element eInput = YRCXmlUtils.createDocument("Customer").getDocumentElement();
				eInput.setAttribute("CustomerKey", strCustomerKey);
				YRCEditorInput newInput = new YRCEditorInput(eInput,new String[] {"CustomerKey"}, 
				"YCD_TASK_CUSTOMER_DETAILS");
				YRCPlatformUI.openEditor("com.yantra.pca.ycd.rcp.editors.YCDCustomerEditor",newInput);
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}
		return super.validateLinkClick(fieldName);
	}
	
}
