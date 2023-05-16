package com.academy.ecommerce.sterling.quickAccess.extn.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.quickAccess.extn.AcademyCognos;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCEditorInput;
import com.yantra.yfc.rcp.YRCEditorPart;
import com.yantra.yfc.rcp.YRCPlatformUI;

public class AcademyCognosReportEditor extends YRCEditorPart {
	public static final String ID_Editor = "com.academy.ecommerce.sterling.quickAccess.extn.editor.AcademyCognosReportEditor";
	public static final String titleKey = "AcademyCognosReport";
	private Composite pnlroot = null;
	
	public AcademyCognosReportEditor() {
	}

	@Override
	public Composite createPartControl(Composite parent, String arg1) {
		Element inElm = ((YRCEditorInput)getEditorInput()).getXml();		
		pnlroot = new AcademyCognos(parent, SWT.NONE,inElm);
		pnlroot.setData(YRCConstants.YRC_OWNERPART, this);
		setPartName(YRCPlatformUI.getString(titleKey));
		return pnlroot;
	}

}
