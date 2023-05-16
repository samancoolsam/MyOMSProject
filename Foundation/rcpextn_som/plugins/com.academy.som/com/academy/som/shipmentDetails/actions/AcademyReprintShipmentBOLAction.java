package com.academy.som.shipmentDetails.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.som.shipmentDetails.screens.AcademyContainerSelectionPanelPopup;
import com.academy.som.shipmentDetails.screens.AcademyShipmentDetailsExtnBehaviour;
import com.academy.som.util.AcademyPCAConstants;
import com.academy.som.util.AcademyPCAErrorMessagesInterface;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCDialog;
import com.yantra.yfc.rcp.YRCEditorInput;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCRelatedTask;
import com.yantra.yfc.rcp.YRCRelatedTaskAction;
import com.yantra.yfc.rcp.YRCWizard;
import com.yantra.yfc.rcp.YRCXmlUtils;


public class AcademyReprintShipmentBOLAction extends YRCRelatedTaskAction implements
IWorkbenchWindowActionDelegate {

	private static String CLASSNAME = "AcademyReprintShipmentBOLAction";

	/**
	 * Superclass method called internally when the 
	 * related Task "Reprint Packslip Label" is clicked
	 */
	public void executeTask(IAction action, YRCEditorInput editorInput,
			YRCRelatedTask rTask) {
		final String methodName="executeTask(action, editorInput, rTask)";
		//String strFlag="Y";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		YRCWizard currentWizard = (YRCWizard) YRCDesktopUI.getCurrentPage();//fetching the current page's wizard
		YRCBehavior beh = currentWizard
		.getChildBehavior("com.yantra.pca.ycd.rcp.tasks.shipmentTracking.screens.YCDShipmentDetails");
		YRCExtentionBehavior exbhvr = beh.getExtensionBehavior();

		if(exbhvr instanceof AcademyShipmentDetailsExtnBehaviour){
			AcademyShipmentDetailsExtnBehaviour wizardBehavior = (AcademyShipmentDetailsExtnBehaviour)exbhvr;

			Document getShipmentDetailsDoc = wizardBehavior.getOOBModel();
			Element shipmentDtlsElement = getShipmentDetailsDoc.getDocumentElement();
			
			NodeList containerNodeList = shipmentDtlsElement.getElementsByTagName(AcademyPCAConstants.CONTAINER_ELEMENT);

			if(containerNodeList.getLength() == 0){

				YRCPlatformUI.setMessage(AcademyPCAErrorMessagesInterface.REPRINT_LABEL_ERR_MSG_KEY, true); //displaying error in status line
				YRCPlatformUI.showError(AcademyPCAConstants.STRING_ERROR, 
						YRCPlatformUI.getString(AcademyPCAErrorMessagesInterface.REPRINT_LABEL_ERR_MSG_KEY));
			}else{
				//If containers present, then show the popup to select container for reprint
				AcademyContainerSelectionPanelPopup cntrPanel = new AcademyContainerSelectionPanelPopup(new Shell(Display.getDefault()),
						SWT.NONE, shipmentDtlsElement);
				YRCDialog cntrPanelDialog = new YRCDialog(cntrPanel, 200, 150, AcademyPCAConstants.CNTR_SEL_POPUP_TITLE, null);
				if(!YRCDialog.isDialogOpen()){//to open only one instance of popup
					cntrPanelDialog.open();
				}
				if(cntrPanel.getIsSelectBtnClicked()){ //if container is selected then call the processPackslipRequest() method of behavior class to reprint
					AcademySIMTraceUtil.logMessage("print the selected cntrl"+cntrPanel.getCntrSelected());
					wizardBehavior.processShipmentBOLRequest(shipmentDtlsElement, cntrPanel.getCntrSelected(), cntrPanel.getCntrSelectedForPackStn());
					}
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
