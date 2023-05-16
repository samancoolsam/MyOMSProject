package com.academy.ecommerce.sterling.quickAccess.extn.actions;

import org.eclipse.jface.action.IAction;

import com.yantra.yfc.rcp.IYRCApiCallbackhandler;
import com.yantra.yfc.rcp.YRCAction;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCEditorInput;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCRelatedTask;
import com.yantra.yfc.rcp.YRCRelatedTaskAction;
import com.yantra.yfc.rcp.YRCXmlEditorInput;
import com.yantra.yfc.rcp.YRCXmlUtils;

public class AcademyCongnosReportAction extends YRCRelatedTaskAction implements
IYRCApiCallbackhandler {


	private YRCRelatedTask relTask=null;

	@Override
	public void executeTask(IAction arg0, YRCEditorInput editorInput,
			YRCRelatedTask relatedTask) {
		/*YRCPlatformUI.closeEditor(editorInput,
				"com.yantra.pca.ycd.rcp.editors.YCDQuickAccessEditor", true);*/
		this.relTask = relatedTask;
		YRCPlatformUI.openEditor("com.academy.ecommerce.sterling.quickAccess.extn.editor.AcademyCognosReportEditor",
				new YRCEditorInput(YRCXmlUtils.createFromString("<Order/>").getDocumentElement(),null,null,relatedTask.getId()));
	}

	public void handleApiCompletion(YRCApiContext arg0) {
		// TODO Auto-generated method stub
		
	}
}
