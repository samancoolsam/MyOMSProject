package com.academy.ecommerce.sterling.itemDetails.screens;

import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCXmlUtils;



public class AcademyItemRestrictionsPanelBehavior extends YRCBehavior {
	

	private AcademyItemRestrictionsPanel view = null;

	private Element eleUpdateOrderDetails = null;

	public AcademyItemRestrictionsPanelBehavior(Composite ownerComposite,
			Composite parent, String formId) {
		super(ownerComposite, formId);
		this.view = (AcademyItemRestrictionsPanel) ownerComposite;
		//createItemRestrictionStatesList();
	}
	
	public void init()
	{
		if (YRCPlatformUI.isTraceEnabled()) {
		YRCPlatformUI.trace("########### Inside init method of Item Restrictions Panel Behavior #########");
		}
	}
		

	public Element getModel() {
		return eleUpdateOrderDetails;

	}
	
	public void createItemRestrictionStatesList(Element eleAdditionalAttributeList) {

		if (!YRCPlatformUI.isVoid(eleAdditionalAttributeList)) {
			if (YRCPlatformUI.isTraceEnabled()) {
				YRCPlatformUI.trace("#########Additional Attribute List from ###########",
						YRCXmlUtils.getString(eleAdditionalAttributeList));
			}
			createLabels(eleAdditionalAttributeList);
		}
	}


	private void createLabels(Element eleAdditionalAttributeList) {
		if(YRCPlatformUI.isTraceEnabled())
		{
			YRCPlatformUI.trace("#####Pricing Panel Item Restrictions#########",
					YRCXmlUtils.getString(eleAdditionalAttributeList));
		}
		
		int iNoOfAdditionalAttributes=0;
		
		/* Iterate through each additional attributes to create labels */

		if (!YRCPlatformUI.isVoid(eleAdditionalAttributeList)) {
			NodeList eleAdditionalAttribute = eleAdditionalAttributeList
					.getElementsByTagName(AcademyPCAConstants.ADDITIONAL_ATTRIBUTE);
			iNoOfAdditionalAttributes=eleAdditionalAttribute.getLength();
			for (int i = 0; i < iNoOfAdditionalAttributes; i++) {
				String strAdditionalAttribute = ((Element) eleAdditionalAttribute.item(i))
						.getAttribute(AcademyPCAConstants.ADDITIONAL_ATTRIBUTE_VALUE);
				view.createLabelForAdditionalAttributes(1, strAdditionalAttribute);
				}
			}
		}
	}
