package com.academy.som.closemanifest.contributors;

import org.eclipse.swt.widgets.Composite;

import com.academy.som.util.AcademyPCAConstants;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.IYRCRelatedTasksExtensionContributor;
import com.yantra.yfc.rcp.YRCEditorInput;
import com.yantra.yfc.rcp.YRCRelatedTask;
/**
 * Custom Related Task Extension Contributor class done for managing the 
 * newly created related tasks within the editor : "com.academy.som.closemanifest.editors.AcademyCloseManifestEditor" 
 * 
 * @author <a href="mailto:Kruthi.KM@cognizant.com">Kruthi KM</a>
 * Copyright © 2006-2009 Sterling Commerce, Inc. All Rights Reserved.
 */
public class AcademyCloseManifestTasksExtensionContributor implements
IYRCRelatedTasksExtensionContributor {
	private static String CLASSNAME = "AcademyCloseManifestTasksExtensionContributor";
	/**
	 * Superclass method called internally when the
	 * editor "com.academy.som.closemanifest.editors.AcademyCloseManifestEditor" 
	 * is opened. This method is implemented to control the custom related tasks to be shown.
	 * @param editorInput
	 * 			<br/> - Editor Input Object of Shipment Details screen
	 * @param rTask
	 * 			<br/> - Related Tasks of the editor
	 * @return boolean
	 * 			<br/> - returns <b>False</b> to hide the particular Group or Task /(or) all Groups 
	 * 			<br/> - returns <b>True</b> to show the particular Group or Task /(or) all Groups
	 */
	public boolean acceptTask(YRCEditorInput editorInput, YRCRelatedTask rTask) {
		// TODO Auto-generated method stub
		final String methodName = "acceptTask(editorInput, rTask)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		//Displays only "ACAD_InStore_Tasks" group in Print Pick Ticket Screen.
		if (rTask.getGroupId().equals(AcademyPCAConstants.PRINT_PICK_TICKET_GROUP_ID)) {

			return true;
		}
		return false;
	}

	@Override
	public boolean canExecuteNewTask(YRCEditorInput arg0, YRCRelatedTask arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Composite createPartControl(Composite arg0, YRCEditorInput arg1,
			YRCRelatedTask arg2) {
		// TODO Auto-generated method stub
		return null;
	}

}
