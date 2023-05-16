package com.academy.ecommerce.sterling.quickAccess.extn.actions;

import org.eclipse.jface.action.IAction;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.orderSearch.editor.AcademyGiftCardLoadEditor;
import com.yantra.yfc.rcp.YRCEditorInput;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCRelatedTask;
import com.yantra.yfc.rcp.YRCRelatedTaskAction;
import com.yantra.yfc.rcp.YRCXmlUtils;

public class AcademyGiftCrdBalChkAction extends YRCRelatedTaskAction {

	private YRCRelatedTask relTask;

	@Override
	public void executeTask(IAction arg0, YRCEditorInput editorInput,
			YRCRelatedTask relatedTask) {
		/*YRCPlatformUI.closeEditor(editorInput,
				"com.yantra.pca.ycd.rcp.editors.YCDQuickAccessEditor", true);*/
		this.relTask = relatedTask;
		createInputforOrderSearch(editorInput);

	}

	private void createInputforOrderSearch(YRCEditorInput editorInput) {
		YRCPlatformUI.trace("Input to the editor", editorInput);
		YRCPlatformUI
				.openEditor(
						"com.academy.ecommerce.sterling.quickAccess.extn.editor.AcademyGiftBalChkEditor",
						new YRCEditorInput(YRCXmlUtils.createFromString("<Order OrderHeaderKey=''/>").getDocumentElement(),null,null,relTask.getId()));
	}

	@Override
	protected boolean checkForErrors() {
		return false;
	}

	@Override
	protected boolean checkForModifications() {
		return false;
	}
}
