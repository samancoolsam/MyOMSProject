package com.academy.som.shipmentDetails.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.som.shipmentDetails.screens.AcademyShipmentDetailsExtnBehaviour;
import com.academy.som.util.AcademyPCAConstants;
import com.academy.som.util.AcademyPCAErrorMessagesInterface;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCEditorInput;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCRelatedTask;
import com.yantra.yfc.rcp.YRCRelatedTaskAction;
import com.yantra.yfc.rcp.YRCWizard;

/**
 * Custom Related Task Action class done for Manifest correction
 * in wizard PageID : "com.yantra.pca.ycd.rcp.tasks.shipmentDetails.wizardpages.YCDShipmentDetailsScreen"
 * 
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 * Copyright © 2006-2009 Sterling Commerce, Inc. All Rights Reserved.
 */
public class AcademyAutoCorrectManifestAction extends YRCRelatedTaskAction
implements IWorkbenchWindowActionDelegate {

	private static String CLASSNAME = "AcademyAutoCorrectManifestAction";

	/**
	 * Superclass method called internally when the 
	 * related Task "Auto Correct Manifest Error" is clicked
	 */
	public void executeTask(IAction action, YRCEditorInput editorInput,
			YRCRelatedTask rTask) {
		final String methodName="executeTask(action, editorInput, rTask)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		YRCWizard currentWizard = (YRCWizard) YRCDesktopUI.getCurrentPage(); //fetching the current page's wizard
		YRCBehavior beh = currentWizard
		.getChildBehavior("com.yantra.pca.ycd.rcp.tasks.shipmentTracking.screens.YCDShipmentDetails");
		YRCExtentionBehavior exbhvr = beh.getExtensionBehavior();

		if(exbhvr instanceof AcademyShipmentDetailsExtnBehaviour){
			AcademyShipmentDetailsExtnBehaviour wizardBehavior = (AcademyShipmentDetailsExtnBehaviour)exbhvr;

			Document getShipmentDetailsDoc = wizardBehavior.getOOBModel();
			Element shipmentDtlsElement = getShipmentDetailsDoc.getDocumentElement();

			if(YRCPlatformUI.isVoid(shipmentDtlsElement.getAttribute("ManifestKey"))){
				//If ManifestKey is empty, then show an error message
				YRCPlatformUI.setMessage(AcademyPCAErrorMessagesInterface.AUTO_CORRECT_MANIFEST_ERR_MSG_KEY, true);
				YRCPlatformUI.showError(AcademyPCAConstants.STRING_ERROR, 
						YRCPlatformUI.getString(AcademyPCAErrorMessagesInterface.AUTO_CORRECT_MANIFEST_ERR_MSG_KEY));
			}else{
				/**If ManifestKey present, then call processAutoManifestCorrectionRequest() method
				of wizard extension behavior to correct the manifest**/
				wizardBehavior.processAutoManifestCorrectionRequest(shipmentDtlsElement);
			}
		}
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * This method is used to check if the editor has to be checked for errors.
	 * 
	 * @return boolean
	 * 			<br/> - returns <b>False</b> to skip the check
	 */
	protected boolean checkForErrors() {
		return false;
	}

	/**
	 * This method is used to check if the editor has to be checked for
	 * modifications.
	 * 
	 * @return boolean
	 * 			<br/> - returns <b>False</b> to skip the check
	 */
	protected boolean checkForModifications() {
		return false;
	}

}
