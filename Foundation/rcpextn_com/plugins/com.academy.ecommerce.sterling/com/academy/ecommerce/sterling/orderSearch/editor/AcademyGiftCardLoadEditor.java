package com.academy.ecommerce.sterling.orderSearch.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.orderSummary.extn.AcademyGiftCardLoad;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCEditorInput;
import com.yantra.yfc.rcp.YRCEditorPart;
import com.yantra.yfc.rcp.YRCPlatformUI;

public class AcademyGiftCardLoadEditor extends YRCEditorPart {

	public static final String ID_Editor = "com.academy.ecommerce.sterling.orderSearch.editor.AcademyGiftCardLoadEditor";

	public static final String titleKey = "AcademyGiftCardLoad";

	private Composite pnlroot = null;

	@Override
	public Composite createPartControl(Composite parent, String arg1) {
		Element inElm = ((YRCEditorInput) getEditorInput()).getXml();
		pnlroot = new AcademyGiftCardLoad(parent, SWT.NONE, inElm);
		pnlroot.setData(YRCConstants.YRC_OWNERPART, this);
		setPartName(YRCPlatformUI.getString(titleKey));
		return pnlroot;
	}

	public void postSetFocus() {
		pnlroot.setFocus();
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isSaveAsAllowed() {

		return false;
	}

}
