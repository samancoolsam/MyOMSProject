package com.academy.ecommerce.sterling.orderSummary.actions;

import org.eclipse.jface.action.IAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.orderSearch.editor.AcademyGiftCardLoadEditor;
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.yantra.yfc.rcp.IYRCApiCallbackhandler;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCEditorInput;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCRelatedTask;
import com.yantra.yfc.rcp.YRCRelatedTaskAction;
import com.yantra.yfc.rcp.YRCXmlUtils;
import com.yantra.yfc.rcp.internal.YRCApiCaller;

public class AcademyGiftCardLoadAction extends YRCRelatedTaskAction implements
		IYRCApiCallbackhandler {

	private YRCRelatedTask relTask = null;

	int iCounter = 0;

	public void executeTask(IAction arg0, YRCEditorInput editorInput,
			YRCRelatedTask relatedTask) {
		YRCPlatformUI.closeEditor(editorInput,
				"com.yantra.pca.ycd.rcp.editors.YCDOrderEditor", true);
		this.relTask = relatedTask;
		createInputForGiftCardLoadScreen(editorInput);
	}

	@Override
	protected boolean checkForErrors() {
		return false;
	}

	@Override
	protected boolean checkForModifications() {
		return false;
	}

	private void createInputForGiftCardLoadScreen(YRCEditorInput edInput) {
		Element eleInput = (Element) edInput.getXml();
		YRCPlatformUI
				.trace(
						"###########Input To Gift Card Load Action Class##############",
						eleInput);
		YRCEditorInput edInp = new YRCEditorInput(eleInput, edInput,
				new String[] { "OrderHeaderKey", "OrderNo" }, relTask.getId());
		YRCPlatformUI.openEditor(AcademyGiftCardLoadEditor.ID_Editor, edInp);
	}

	private Document invokeSyncAPI(Document docShipmentLine, String strAPIName) {
		YRCApiContext context = new YRCApiContext();
		YRCApiCaller syncapiCaller = new YRCApiCaller(context, true);
		context.setApiName(strAPIName);
		context
				.setFormId("com.academy.ecommerce.sterling.orderSummary.extn.AcademyGiftCardLoad");
		context.setInputXml(docShipmentLine);
		syncapiCaller.invokeApi();
		Document outputXML = context.getOutputXml();
		return outputXML;
	}

	public void handleApiCompletion(YRCApiContext arg0) {
		// TODO Auto-generated method stub

	}
}
