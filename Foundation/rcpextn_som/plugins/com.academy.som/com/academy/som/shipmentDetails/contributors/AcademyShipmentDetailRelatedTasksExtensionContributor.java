package com.academy.som.shipmentDetails.contributors;

import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.som.util.AcademyPCAConstants;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.IYRCRelatedTasksExtensionContributor;
import com.yantra.yfc.rcp.YRCEditorInput;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCRelatedTask;
import com.yantra.yfc.rcp.YRCXmlUtils;

/**
 * Custom Related Task Extension Contributor class done for managing the 
 * newly created related tasks within the editor : "com.yantra.pca.ycd.rcp.editors.YCDShipmentEditor" 
 * 
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 * Copyright © 2006-2009 Sterling Commerce, Inc. All Rights Reserved.
 */
public class AcademyShipmentDetailRelatedTasksExtensionContributor implements
IYRCRelatedTasksExtensionContributor {

	private static String CLASSNAME = "AcademyShipmentDetailRelatedTasksExtensionContributor";

	/**
	 * Superclass method called internally when the
	 * editor "com.yantra.pca.ycd.rcp.editors.YCDShipmentEditor" 
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
		final String methodName="acceptTask(editorInput, rTask)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		if(rTask.getGroupId().equals(AcademyPCAConstants.SHIPDTL_EXCEPTION_TASKS_GRP_ID)){
			AcademySIMTraceUtil.logMessage("Evaluating for Related Task Group ID: "+rTask.getGroupId()+" :: START");
			Element shipmentDtlsElement = editorInput.getXml();			
			String shipmentStatus = shipmentDtlsElement.getAttribute("Status");
			
			//Start STL-737 Changes
			String shipmentType = shipmentDtlsElement.getAttribute("ShipmentType");
			AcademySIMTraceUtil.logMessage("Shipment Type"+ shipmentType);
			//End STL-737 Changes
			
			
			//OMNI-7980 : Begin			
			boolean isSTSAmmoOrHazmatShipment = isSTSAmmoOrHazmatShipment(shipmentDtlsElement);
			boolean isWGHazmat = isWGHazmat(shipmentDtlsElement);
			if(AcademyPCAConstants.STR_STS.equals(shipmentType)){
				
				AcademySIMTraceUtil.logMessage("STS Specific :: ");
				
				//For Task Name : "Reprint Packslip Label"
				if(rTask.getId().equals(AcademyPCAConstants.SHIPDTL_REPRINT_PACKSLIP_TASK_ID)){
					AcademySIMTraceUtil.logMessage("False returned for Related Task: "+rTask.getName());
					return false; //returning false to hide this particular task
				}

				//For Task Name : "Reprint Shipment Invoice"
				if(rTask.getId().equals(AcademyPCAConstants.SHIPDTL_REPRINT_INVOICE_TASK_ID)){
					AcademySIMTraceUtil.logMessage("False returned for Related Task: "+rTask.getName());
					return false; //returning false to hide this particular task
				}

				//For Task Name : "Reprint Shipment Pick Ticket"
				if(rTask.getId().equals(AcademyPCAConstants.SHIPDTL_REPRINT_SHIPMENT_TASK_ID)){
					AcademySIMTraceUtil.logMessage("False returned for Related Task: "+rTask.getName());
					return false; //returning false to hide this particular task
				}
				//For Task Name: Auto Correct Manifest
				if(rTask.getId().equals(AcademyPCAConstants.SHIPDTL_AUTO_CORRECT_MANIFEST_TASK_ID)) {
					AcademySIMTraceUtil.logMessage("False returned for Related Task: "+rTask.getName());
					return false; //returning false to hide this particular task
				}
				
				//For Task Name: Auto Correct Shipment
				if(rTask.getId().equals(AcademyPCAConstants.SHIPDTL_AUTO_CORRECT_SHIPMENT_TASK_ID)) {
					AcademySIMTraceUtil.logMessage("False returned for Related Task: "+rTask.getName());
					return false; //returning false to hide this particular task
				}
				
				//For Task Name: Reprint Pick Ticket
				if(rTask.getId().equals(AcademyPCAConstants.SHIPDTL_REPRINT_SHIPMENT_PICK_TICKET_TASK_ID)) {
					AcademySIMTraceUtil.logMessage("False returned for Related Task: "+rTask.getName());
					return false; //returning false to hide this particular task
				}
				
				//For Task Name: Reprint Shipment BOL
				if(rTask.getId().equals(AcademyPCAConstants.SHIPDTL_REPRINT_SHIPMENT_BOL_TASK_ID)) {
					AcademySIMTraceUtil.logMessage("False returned for Related Task: "+rTask.getName());
					return false; //returning false to hide this particular task
				}
		
			}
			//OMNI-7980 : End

			else if(!(shipmentStatus.equals(AcademyPCAConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL)) && 
					!(shipmentStatus.equals(AcademyPCAConstants.STATUS_READY_FOR_CUSTOMER_VAL))){
				
				//For Task Name : "Reprint Shipping Label"
				if(rTask.getId().equals(AcademyPCAConstants.SHIPDTL_REPRINT_SHIPPING_TASK_ID)){
					AcademySIMTraceUtil.logMessage("False returned for Related Task: "+rTask.getName());
					return false; //returning false to hide this particular task
				}
				//For Task Name : "Reprint Packslip Label"
				if(rTask.getId().equals(AcademyPCAConstants.SHIPDTL_REPRINT_PACKSLIP_TASK_ID)){
					AcademySIMTraceUtil.logMessage("False returned for Related Task: "+rTask.getName());
					return false; //returning false to hide this particular task
				}

				//For Task Name : "Reprint Shipment Invoice"
				if(rTask.getId().equals(AcademyPCAConstants.SHIPDTL_REPRINT_INVOICE_TASK_ID)){
					AcademySIMTraceUtil.logMessage("False returned for Related Task: "+rTask.getName());
					return false; //returning false to hide this particular task
				}

				//For Task Name : "Reprint Shipment Pick Ticket"
				if(rTask.getId().equals(AcademyPCAConstants.SHIPDTL_REPRINT_SHIPMENT_TASK_ID)){
					AcademySIMTraceUtil.logMessage("False returned for Related Task: "+rTask.getName());
					return false; //returning false to hide this particular task
				}
				//Start STL-737 Changes Task Name: Reprint ORMD Label
				if(rTask.getId().equals(AcademyPCAConstants.SHIPDTL_REPRINT_ORMD_TASK_ID)) {
					AcademySIMTraceUtil.logMessage("False returned for Related Task: "+rTask.getName());
					return false; //returning false to hide this particular task
				}
				//End STL-737 Changes
			}
			//Start STL-737 Changes: Do not show Reprint ORMD Label for non-AMMO Shipments
			
			//OMNI-7980 : Begin - added boolean 'isSTSAmmoOrHazmatShipment'
			//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
			if(rTask.getId().equals(AcademyPCAConstants.SHIPDTL_REPRINT_ORMD_TASK_ID) &&
					!(AcademyPCAConstants.AMMO_SHIPMENT_TYPE.equals(shipmentType) 
							|| AcademyPCAConstants.HAZMAT_SHIPMENT_TYPE.equals(shipmentType)
							|| isSTSAmmoOrHazmatShipment || isWGHazmat)) {
				return false;
			}
			//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
			//OMNI-7980 : End
			
			//End STL-737 Changes
			
			
			AcademySIMTraceUtil.logMessage("Evaluating for Related Task Group ID: "+rTask.getGroupId()+" :: END");
		}

		if(rTask.getGroupId().equals(AcademyPCAConstants.SHIPDTL_INSTORE_TASKS_GRP_ID)){
			//For Task Name : "Advance Shipment Search"
			if(rTask.getId().equals(AcademyPCAConstants.SHIPDTL_ADVSHP_SEARCH_TASKS_ID)){
				AcademySIMTraceUtil.logMessage("False returned for Related Task: "+rTask.getName());
				return false; //returning false to hide this particular task
			}
			
			//For Task Name : "Print Pick Ticket"
			if(rTask.getId().equals(AcademyPCAConstants.SHIPDTL_PICK_TICKET_TASKS_ID)){
				AcademySIMTraceUtil.logMessage("False returned for Related Task: "+rTask.getName());
				return false; //returning false to hide this particular task
			}
			
			//Start : OMNI-6616 : STS Changes
			//For Task Name : "Record Backroom Pick"
			if(rTask.getId().equals(AcademyPCAConstants.STR_BACKROOM_PICK_TASK)){
				AcademySIMTraceUtil.logMessage("True returned for RecordBackroom Pick Task: "+rTask.getName());
				return true; //returning true to show this particular task
			}
			//End : OMNI-6616 : STS Changes
		}
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return true; //returning true for other Groups and Related Tasks
	}

	public boolean canExecuteNewTask(YRCEditorInput editorInput, YRCRelatedTask rTask) {
		// TODO Auto-generated method stub
		return false;
	}

	public Composite createPartControl(Composite comp, YRCEditorInput editorInput,
			YRCRelatedTask rTask) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
		// START OMNI-65673
	private boolean isWGHazmat(Element eleShipment){
       	AcademySIMTraceUtil.logMessage("Begin: isWGHazmat : ");
		boolean isWGHazmatItem = false;
	
			try{
			
			if(!YRCPlatformUI.isVoid(eleShipment)){
				AcademySIMTraceUtil.logMessage("Shipment Details :: "+YRCXmlUtils.getString(eleShipment));
				
				String shipmentType = eleShipment.getAttribute("ShipmentType");
				AcademySIMTraceUtil.logMessage("Shipment Type"+ shipmentType);
				
			
			 if(AcademyPCAConstants.WG_SHIPMENT_TYPE.equals(shipmentType)){
								
				NodeList nlOrderLines = eleShipment.getElementsByTagName(AcademyPCAConstants.ELE_ORDER_LINE);
					
					for(int iOL = 0;iOL<nlOrderLines.getLength();iOL++){
						AcademySIMTraceUtil.logMessage("Order Line - "+iOL);
						
						Element eleOrderLine = (Element)nlOrderLines.item(iOL);
						String strLineType = eleOrderLine.getAttribute(AcademyPCAConstants.ATTR_LINE_TYPE);
						
						if(AcademyPCAConstants.HAZMAT_SHIPMENT_TYPE.equals(strLineType)){
							isWGHazmatItem = true;
							AcademySIMTraceUtil.logMessage("set 'isWGHazmatItem' to TRUE");
						}
						
					}
					
					
				}		
			
			}
			
		}catch(Exception e){
			AcademySIMTraceUtil.logMessage("Exception caught :: "+e);
		}
		
		AcademySIMTraceUtil.logMessage("End: isWHHazmat : ");
		return isWGHazmatItem;
	
	}
	// END  OMNI-65673
	
	
	
	//OMNI-7980 : Begin
	private boolean isSTSAmmoOrHazmatShipment(Element eleShipment){
		
		AcademySIMTraceUtil.logMessage("Begin: isSTSAmmoOrHazmatShipment : ");
		boolean isAmmoHazmatShipment = false;
		
		try{
			
			if(!YRCPlatformUI.isVoid(eleShipment)){
				AcademySIMTraceUtil.logMessage("Shipment Details :: "+YRCXmlUtils.getString(eleShipment));
				
				String shipmentType = eleShipment.getAttribute("ShipmentType");
				AcademySIMTraceUtil.logMessage("Shipment Type"+ shipmentType);
				
				if(AcademyPCAConstants.STR_STS.equals(shipmentType)){
					
					NodeList nlOrderLines = eleShipment.getElementsByTagName(AcademyPCAConstants.ELE_ORDER_LINE);
					
					for(int iOL = 0;iOL<nlOrderLines.getLength();iOL++){
						AcademySIMTraceUtil.logMessage("Order Line - "+iOL);
						
						Element eleOrderLine = (Element)nlOrderLines.item(iOL);
						String strLineType = eleOrderLine.getAttribute(AcademyPCAConstants.ATTR_LINE_TYPE);
						
						if(AcademyPCAConstants.AMMO_SHIPMENT_TYPE.equals(strLineType) 
								|| AcademyPCAConstants.HAZMAT_SHIPMENT_TYPE.equals(strLineType)){
							isAmmoHazmatShipment = true;
							AcademySIMTraceUtil.logMessage("set 'isAmmoHazmatShipment' to TRUE");
						}
						
					}
					
				}
			
			}
			
		}catch(Exception e){
			AcademySIMTraceUtil.logMessage("Exception caught :: "+e);
		}
		
		AcademySIMTraceUtil.logMessage("End: isSTSAmmoOrHazmatShipment : ");
		return isAmmoHazmatShipment;
	}
	//OMNI-7980 : End

}
