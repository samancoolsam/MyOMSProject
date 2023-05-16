
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/batch/batchpick/common/BatchPickupProductScanExtnUI", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!ias/utils/ContextUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!ias/utils/ScreenUtils", "scbase/loader!ias/utils/WizardUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!sc/plat/dojo/utils/EditorUtils", "scbase/loader!sc/plat/dojo/utils/RepeatingPanelUtils","scbase/loader!ias/utils/ModelUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnBatchPickupProductScanExtnUI
			,
				_scScreenUtils
			,
				_iasContextUtils
			,
				_scBaseUtils
			,
				_scEventUtils
			,
				_scWidgetUtils
			,
				_iasScreenUtils
			,
				_iasWizardUtils
			,
				_scModelUtils
			,
				_iasUIUtils
			,
				_scEditorUtils
			,
				_scRepeatingPanelUtils
			,
				_iasModelUtils
){ 
	return _dojodeclare("extn.components.batch.batchpick.common.BatchPickupProductScanExtn", [_extnBatchPickupProductScanExtnUI],{
	// custom code here
	
	/* This OOB method has been overridden in order to hide the Assign Staging Location functionality */
	initializeScreen: function(
        event, bEvent, ctrl, args) {
        	
        	var batchLinesList = _scScreenUtils.getModel(this, "StoreBatchLines");
            //console.log(batchLinesList);
        	
        	var eventBean = {};
			_scBaseUtils.setAttributeValue("argumentList", {}, eventBean);
			if(!_scBaseUtils.isVoid(batchLinesList)) {
				 _scBaseUtils.setAttributeValue("argumentList.Batch", batchLinesList.Page.Output, eventBean);
			}
			//OMNI-61489 and OMNI-68791 Starts
        	var totalNumberOfRecords=  eventBean.argumentList.Batch.StoreBatch.StoreBatchLines.TotalNumberOfRecords;
			var cancelBatch = "Y";
			var storeBatchLine = eventBean.argumentList.Batch.StoreBatch.StoreBatchLines.StoreBatchLine;
            for(var i in storeBatchLine){
				var vQuantity = _scModelUtils.getStringValueFromPath("Quantity", storeBatchLine[i]);
				if((Number(vQuantity)) > 0 ){
					cancelBatch = "N";
					break;
				}
			}
			if((!_scBaseUtils.isVoid(totalNumberOfRecords)) && (_scBaseUtils.equals(totalNumberOfRecords,"0")) || _scBaseUtils.equals(cancelBatch,"Y")){
			var cancelStoreBatchInput = {};
        	cancelStoreBatchInput = _scModelUtils.createModelObjectFromKey("StoreBatch", cancelStoreBatchInput);
        	_scModelUtils.setStringValueAtModelPath("StoreBatch.StoreBatchKey", _scModelUtils.getStringValueFromPath("Page.Output.StoreBatch.StoreBatchKey", batchLinesList) , cancelStoreBatchInput);
			_scModelUtils.setStringValueAtModelPath("StoreBatch.Action", "Modify" , cancelStoreBatchInput);
			_scModelUtils.setStringValueAtModelPath("StoreBatch.Status", "9000" , cancelStoreBatchInput);
			_iasUIUtils.callApi(this, cancelStoreBatchInput, "extn_CloseCancelledBatchRef", null);  
			}
			//OMNI-61489 and OMNI-68791 Ends
			
			else{
        	if(_iasContextUtils.isMobileContainer()) {    		
                _scEventUtils.fireEventToParent(this, "setWizardDescription", eventBean);
				//OMNI-8715 : START - Appending 'Express' to Btach name if SCAC is Standard Overnight/2 Day
				var storeBatchLine = eventBean.argumentList.Batch.StoreBatch.StoreBatchLines.StoreBatchLine;
                for(var i in storeBatchLine){
                	var ShipmentLine = _scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine", storeBatchLine[i]);
                	for(var j in ShipmentLine){
                		var sSCAC = _scModelUtils.getStringValueFromPath("Shipment.0.CarrierServiceCode", ShipmentLine[j]);
						if(!(_scBaseUtils.isVoid(sSCAC)) && _scBaseUtils.equals(sSCAC,"Standard Overnight") || _scBaseUtils.equals(sSCAC,"2 Day")){
							var parentScreen = _iasUIUtils.getParentScreen(this, true);	
							_iasWizardUtils.setLabelOnNavigationalWidget(parentScreen, "lblBatchNo", _scScreenUtils.getString(this, "Label_ExpressBatchNumber"));
							_iasWizardUtils.addClassToNavigationalWidget(parentScreen, "lblBatchNo", "extn_Express");
						}
                	}
                }
				//OMNI-8715 : End
            }else {
            	//OMNI-8715 : START - Appending 'Express' to Btach name if SCAC is Standard Overnight/2 Day
				var storeBatchLine = eventBean.argumentList.Batch.StoreBatch.StoreBatchLines.StoreBatchLine;
                for(var i in storeBatchLine){
                	var ShipmentLine = _scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine", storeBatchLine[i]);
                	for(var j in ShipmentLine){
                		var sSCAC = _scModelUtils.getStringValueFromPath("Shipment.0.CarrierServiceCode", ShipmentLine[j]);
						if(!(_scBaseUtils.isVoid(sSCAC)) && _scBaseUtils.equals(sSCAC,"Standard Overnight") || _scBaseUtils.equals(sSCAC,"2 Day")){						
							var BatchNo = _scModelUtils.getStringValueFromPath("Page.Output.StoreBatch.BatchNo", batchLinesList);
							BatchNo = "Express Batch "+BatchNo;
							_scWidgetUtils.setValue(this,"extn_datalabel1", BatchNo);
							_scWidgetUtils.showWidget(this,"extn_datalabel1",false, null);
							_scWidgetUtils.hideWidget(this,"BatchNolbl",false, null);
						}else{
							_scWidgetUtils.showWidget(this,"BatchNolbl",false, null);
							_scWidgetUtils.hideWidget(this,"extn_datalabel1",false, null);
						}
                	}
                }				
				//OMNI-8715 : End
            }
        	
        	//Update Editor title
        	_iasScreenUtils.updateEditorTitle(this, _scModelUtils.getStringValueFromPath("Page.Output.StoreBatch.BatchNo", batchLinesList), "Title_BatchNo");
        	
        	//Change Next label to Assign Staging Location
        	var wizardScreen = _iasUIUtils.getParentScreen(this,true);
			
			/* Assign Staging Location button hiidden at Wizard level */
        	//_iasWizardUtils.setLabelOnNavigationalWidget(wizardScreen,"nextBttn",_scScreenUtils.getString(this, "Action_Assign_Staging_Location"));
        	_iasWizardUtils.disableNavigationalWidget(wizardScreen,"nextBttn");					
        	
        	//Set "To be picked" as default filter selected
        	this.setDefaultFilterSelection("toBePickedLines");
        	
        	//Set StoreBatch model with only StoreBatchKey in Screen and Editor initial input
        	var storeBatchModel = {};
        	storeBatchModel = _scModelUtils.createModelObjectFromKey("StoreBatch", storeBatchModel);
        	_scModelUtils.setStringValueAtModelPath("StoreBatch.StoreBatchKey", _scModelUtils.getStringValueFromPath("Page.Output.StoreBatch.StoreBatchKey", batchLinesList) , storeBatchModel);
        	_scModelUtils.setStringValueAtModelPath("StoreBatch.BatchNo", _scModelUtils.getStringValueFromPath("Page.Output.StoreBatch.BatchNo", batchLinesList) , storeBatchModel);
        	_scModelUtils.setStringValueAtModelPath("StoreBatch.Status", _scModelUtils.getStringValueFromPath("Page.Output.StoreBatch.Status", batchLinesList) , storeBatchModel);
        	_scScreenUtils.setInitialInputData(this,storeBatchModel);
        	_scScreenUtils.setInitialInputData(_iasUIUtils.getWizardForScreen(this),storeBatchModel);
        	_scScreenUtils.setInitialInputData(_scEditorUtils.getCurrentEditor(),storeBatchModel);
			
			// The below code has been added to call a new service which returns the item details 
			var itemDetailsServiceInput = {};
        	itemDetailsServiceInput = _scModelUtils.createModelObjectFromKey("StoreBatch", itemDetailsServiceInput);
        	_scModelUtils.setStringValueAtModelPath("StoreBatch.StoreBatchKey", _scModelUtils.getStringValueFromPath("Page.Output.StoreBatch.StoreBatchKey", batchLinesList) , itemDetailsServiceInput);
			_scModelUtils.setStringValueAtModelPath("StoreBatch.StoreBatchLine.BackroomPickComplete", "N" , itemDetailsServiceInput);
			//Removing this service call since it is handled in massage output of AcademyWSCGetStoreBatchLineList as a part of BOPIS-1567,68,69
			// _iasUIUtils.callApi(this, itemDetailsServiceInput, "extn_ItemPropertiesService", null);       	
        }
		},
		
		/* This OOB method is overridden to modify the pop up which appears after refreshStoreBatchLine method is called */
		showBatchPickingCompletionSuccess:function() {
				var textObj = {};			
				//This has been changed as per requirement
				textObj["OK"] = _scScreenUtils.getString(this, "Action_Finish");
	           // textObj["CANCEL"] = _scScreenUtils.getString(this, "Action_Finish");
	            var msg = _scScreenUtils.getString(this, "Message_AllBatchLinesPicked");
	            var argsObj = {type:"success",text:_scScreenUtils.getString(this,"textSuccess"),info:msg,iconClass:"messageSuccessIcon"};
	            _scScreenUtils.showInfoMessageBox(this, argsObj, "handleAssignStagingLocation", textObj, {});
        },
		
		/* This OOB method is modified accordingly for staging location hiding requirement */
		handleAssignStagingLocation:function(res) {
	        if (_scBaseUtils.equals(res, "Ok")) {
	            this.handleFinshBatchPickup();
	        } else {
	        	//this.handleFinshBatchPickup();
	        } 
        },
		
		/* This OOB method is modified accordingly for staging location hiding requirement */
		refreshStoreBatchLine:function(storeBatchModel) {
        	var storeBatchLineModel = {};
    		storeBatchLineModel.StoreBatchLine = {};
    		
        	if(!_scBaseUtils.isVoid(storeBatchModel)) {
        		
        		if(!_scBaseUtils.isVoid(storeBatchModel.StoreBatch.StoreBatchLines)){
        			storeBatchLineModel.StoreBatchLine = storeBatchModel.StoreBatch.StoreBatchLines.StoreBatchLine[0];
        		}else if(!_scBaseUtils.isVoid(storeBatchModel.StoreBatch.StoreBatchLine)){
        			storeBatchLineModel.StoreBatchLine = storeBatchModel.StoreBatch.StoreBatchLine;
        		}
        		
        		if(!_scBaseUtils.isVoid(storeBatchLineModel)){
        			var itemID = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemID",storeBatchLineModel);
            		var uom = _scModelUtils.getStringValueFromPath("StoreBatchLine.UnitOfMeasure",storeBatchLineModel);
            		var uniqueId = itemID+"|"+uom;
            		var repPanelUId = _scRepeatingPanelUtils.returnUIdOfIndividualRepeatingPanel(this,"unpickedLineList",uniqueId);
            		var repPanelScreen = _iasScreenUtils.getRepeatingPanelScreenWidget(this, repPanelUId);
                    if (_scBaseUtils.isVoid(repPanelScreen)) {} else {
                        
                    	repPanelScreen.setModel("BatchLine",storeBatchLineModel);
                    	_scScreenUtils.clearScreen(repPanelScreen);
                    	 var eventBean = {};
                         _scBaseUtils.setAttributeValue("BatchLine", storeBatchLineModel, eventBean);
                    	_scEventUtils.fireEventInsideScreen(repPanelScreen,"refreshBatchLine",{},eventBean);
                    	
                    	 _iasScreenUtils.scrollTopWithFixedHeader(repPanelScreen, "batchLineDetailsPanel", 0, 218);
                    }
                    
                    this.handlePanelHighlight(uniqueId);
                    
                    this.setFocusOnWidgetBasedOnMode(uniqueId);
                    
                    var status = _scModelUtils.getStringValueFromPath("StoreBatch.Status", storeBatchModel);
                    if(_scBaseUtils.contains(status,"2000")) {
                    	var wizardScreen = _iasUIUtils.getParentScreen(this,true);
						
						//Disabled as per Staging Location requirement
                    	//_iasWizardUtils.enableNavigationalWidget(wizardScreen,"nextBttn");
                    	this.showBatchPickingCompletionSuccess();
                    	//_scScreenUtils.showConfirmMessageBox(this, _scScreenUtils.getString(this, "Message_AllBatchLinesPicked"), "handleAssignStagingLocation", null, null);
                    } else if (_scBaseUtils.contains(status,"9000")) {
                    	this.handleBatchLineCancelled();
                    }
        		}
        		
        	}
        },
		
		
		/* This method has been overridden in order to include the output behavior of new mashups created */
		 handleMashupOutput: function(
        mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
            if (
            _scBaseUtils.equals(
            mashupRefId, "registerBatchPickManualModeSAP")) {
                if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "registerBatchPickManualModeSAP_output", modelOutput, null);
                }
                this.registerBatchPickManualModeSAP(
                modelOutput);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "registerBatchPickScanModeSAP")) {
                if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "registerBatchPickScanModeSAP_output", modelOutput, null);
                }
                this.registerBatchPickManualModeSAP(
                modelOutput);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "registerBatchPickScanMode")) {
                if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "registerBatchPickScanMode_output", modelOutput, null);
                }
                this.registerBatchPickScanMode(
                modelOutput);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "registerBatchPickManualMode")) {
                if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "registerBatchPickScanMode_output", modelOutput, null);
                }
                this.registerBatchPickManualMode(
                modelOutput);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "getStoreBatchLineDetailsByItemId")) {
                if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "getStoreBatchLineDetailsByItemId_output", modelOutput, null);
                }
                this.refreshStoreBatchLine(
                modelOutput);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "finishBatchPickup")) {
                if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "finishBatchPickup_output", modelOutput, null);
                }
                this.afterFinishBatchPickup(
                modelOutput);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "getStoreBatchLineLocationDetailsByItemId")) {
                if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "getStoreBatchLineLocationDetailsByItemId_output", modelOutput, null);
                }
                this.openCurrentItemDetails(
                modelOutput);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "recordShortageForBatchPick")) {
                if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "recordShortageForBatchPick_output", modelOutput, null);
                }
                this.extnCycleCountService();
            }
			if (
            _scBaseUtils.equals(
            mashupRefId, "extn_ItemPropertiesService")) {
                
                this.extnRefreshBatchLine(modelOutput);
            }
			if (
            _scBaseUtils.equals(
            mashupRefId, "extn_BatchCycleCount")) {
                var recordShortageForBatchPickOutput = _scScreenUtils.getModel(this, "recordShortageForBatchPick_output");	
                this.handleShortageForBatchLine(
                recordShortageForBatchPickOutput);
            }
			if (
            _scBaseUtils.equals(
            mashupRefId, "extn_CloseCancelledBatchRef")) {             
				_iasScreenUtils.showInfoMessageBoxWithOk(this,_scScreenUtils.getString(this,"All the products in this batch are already shorted"),"closeWizard",{});
            }
        },
		
		/* This method is called on the handlemashupoutput method with mashuprefid='extn_ItemPropertiesService' */
		extnRefreshBatchLine:function(storeBatchModel)
		{
			var storeBatchLineModel = {};
    		storeBatchLineModel.StoreBatchLine = {};
			if(!_scBaseUtils.isVoid(storeBatchModel)) 
			{
				if(!_scBaseUtils.isVoid(storeBatchModel.StoreBatch.StoreBatchLines))
				{
					var storeBatchLine = storeBatchModel.StoreBatch.StoreBatchLines.StoreBatchLine;
					for(var storeBatchLineIterator in storeBatchLine)
					{
						storeBatchLineModel.StoreBatchLine = storeBatchLine[storeBatchLineIterator];
						if(!_scBaseUtils.isVoid(storeBatchLineModel))
						{
							var itemID = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemID",storeBatchLineModel);
							var uom = _scModelUtils.getStringValueFromPath("StoreBatchLine.UnitOfMeasure",storeBatchLineModel);
							var uniqueId = itemID+"|"+uom;
							var repPanelUId = _scRepeatingPanelUtils.returnUIdOfIndividualRepeatingPanel(this,"unpickedLineList",uniqueId);
							var repPanelScreen = _iasScreenUtils.getRepeatingPanelScreenWidget(this, repPanelUId);
																					
							if (_scBaseUtils.isVoid(repPanelScreen)) {} 
							else 
							{
								repPanelScreen.setModel("extn_BatchLine",storeBatchLineModel);
								_scScreenUtils.clearScreen(repPanelScreen);
								 var eventBean = {};
								 _scBaseUtils.setAttributeValue("extn_BatchLine", storeBatchLineModel, eventBean);
								_scEventUtils.fireEventInsideScreen(repPanelScreen,"refreshBatchLine",{},eventBean);
								
								 _iasScreenUtils.scrollTopWithFixedHeader(repPanelScreen, "batchLineDetailsPanel", 0, 218);
							}
							this.setFocusOnWidgetBasedOnMode(uniqueId);
						}
					}
				}
			}
		},
		
		/* This method has been overridden in order to create a new model which doesnt include the shortageQty sent from shortagepopup and send it to the API */
		updateShortageForBatchLine:function(event, bEvent, ctrl, args) 
		{
        	
        	var batchLineModel = _scBaseUtils.getValueFromPath("BatchLineShortedModel", args);	
			_scScreenUtils.setModel(this,"extn_BatchShortageOutputNS",batchLineModel, null);
			var newBatchLineModel = {};
        	newBatchLineModel = _scModelUtils.createModelObjectFromKey("StoreBatch", newBatchLineModel);
			
			_scModelUtils.setStringValueAtModelPath("StoreBatch.Item.ItemID", _scModelUtils.getStringValueFromPath("StoreBatch.Item.ItemID", batchLineModel), newBatchLineModel);
			_scModelUtils.setStringValueAtModelPath("StoreBatch.Item.UnitOfMeasure", _scModelUtils.getStringValueFromPath("StoreBatch.Item.UnitOfMeasure", batchLineModel), newBatchLineModel);
			_scModelUtils.setStringValueAtModelPath("StoreBatch.Item.ShortageReason", _scModelUtils.getStringValueFromPath("StoreBatch.Item.ShortageReason", batchLineModel), newBatchLineModel);
				
        	_scModelUtils.setStringValueAtModelPath("StoreBatch.StoreBatchKey", _scModelUtils.getStringValueFromPath("StoreBatch.StoreBatchKey", _scScreenUtils.getInitialInputData(_scEditorUtils.getCurrentEditor())), newBatchLineModel);
        	_iasUIUtils.callApi(this, newBatchLineModel, "recordShortageForBatchPick", null);
        	
        },
		
		/* This method is called on completion of recordShortageForBatchPick api call */
		extnCycleCountService : function()
		{
			var batchLineModel = _scScreenUtils.getModel(this, "extn_BatchShortageOutputNS");	
			_scModelUtils.setStringValueAtModelPath("StoreBatch.StoreBatchKey", _scModelUtils.getStringValueFromPath("StoreBatch.StoreBatchKey", _scScreenUtils.getInitialInputData(_scEditorUtils.getCurrentEditor())), batchLineModel);
			_iasUIUtils.callApi(this, batchLineModel, "extn_BatchCycleCount", null);
		},
		
		/* This OOB method has been overridden in order to accomodate changes for BOPIS - 793 */
		setFocusOnWidgetBasedOnMode:function(uniqueId) {
        	
        	if(_scBaseUtils.equals("SCAN",this.getCurrentBatchLineMode())) {
        		
        		if(!_iasContextUtils.isMobileContainer()) {
               	 _scWidgetUtils.setFocusOnWidgetUsingUid(this, "scanProductIdTxt");
               } else {
                // commenting below OOTB line as a part of BOPIS:1249 fix
            	   // _scWidgetUtils.setFocusOnWidgetUsingUid(this, "filterLink"); 
               }
        		
        	} else if (_scBaseUtils.equals("SHORTAGE",this.getCurrentBatchLineMode())) {
        		var repScreen = this.getRepeatingScreenByUniqueId(uniqueId);
        		if(!_scBaseUtils.isVoid(repScreen)) {
        			if(_scWidgetUtils.isWidgetDisabled(repScreen,"shortageResolutionLink")){
        				_scWidgetUtils.setFocusOnWidgetUsingUid(repScreen, "itemdescriptionLink");
        			} else {
        				_scWidgetUtils.setFocusOnWidgetUsingUid(repScreen, "shortageResolutionLink");
        			}
        		}
        	} else {
        		
        		var batchLinesList = _scScreenUtils.getModel(this, "StoreBatchLines");
            	var batchType = _scModelUtils.getStringValueFromPath("Page.Output.StoreBatch.BatchType", batchLinesList);
            	
            	if(_scBaseUtils.equals("SORT_WHILE_PICK",batchType)) {
            		
            		if(_scBaseUtils.equals("SCAN",this.getCurrentBatchLineMode())) {
                		
                		if(!_iasContextUtils.isMobileContainer()) {
                       	 _scWidgetUtils.setFocusOnWidgetUsingUid(this, "scanProductIdTxt");
                       } else {
                    	   _scWidgetUtils.setFocusOnWidgetUsingUid(this, "filterLink"); 
                       }
                		
                	} else if(_scBaseUtils.equals("MANUAL",this.getCurrentBatchLineMode())) {
                		
                		var repScreen = this.getRepeatingScreenByUniqueId(uniqueId);
                		if(!_scBaseUtils.isVoid(repScreen)) {
                			if(_scWidgetUtils.isWidgetVisible(repScreen,"pickButton")){
                				_scWidgetUtils.setFocusOnWidgetUsingUid(repScreen, "pickButton");
                			} else {
                				_scWidgetUtils.setFocusOnWidgetUsingUid(repScreen, "itemdescriptionLink");
                			}
                			 
                		}
                	} else if(_scBaseUtils.equals("EDIT",this.getCurrentBatchLineMode())) {
                		
                		var repScreen = this.getRepeatingScreenByUniqueId(uniqueId);
                		if(!_scBaseUtils.isVoid(repScreen)) {
                			if(_scWidgetUtils.isWidgetDisabled(repScreen,"editLink")){
                				_scWidgetUtils.setFocusOnWidgetUsingUid(repScreen, "itemdescriptionLink");
                			} else {
                				_scWidgetUtils.setFocusOnWidgetUsingUid(repScreen, "editLink");
                			}
                		}
                	} 
            		
            	} else if(_scBaseUtils.equals("SORT_AFTER_PICK",batchType)) {
            		
            		if(_scBaseUtils.equals("MANUAL-PICK",this.getCurrentBatchLineMode())) {
                		
                		var repScreen = this.getRepeatingScreenByUniqueId(uniqueId);
                		if(!_scBaseUtils.isVoid(repScreen)) {
                			if(_scWidgetUtils.isWidgetDisabled(repScreen,"addQtyLink")){
                				_scWidgetUtils.setFocusOnWidgetUsingUid(repScreen, "removeQtyLink");
                			} else {
                				_scWidgetUtils.setFocusOnWidgetUsingUid(repScreen, "addQtyLink");
                			}
                			 
                		}
                	} else if(_scBaseUtils.equals("MANUAL-UNDOPICK",this.getCurrentBatchLineMode())) {
                		
                		var repScreen = this.getRepeatingScreenByUniqueId(uniqueId);
                		if(!_scBaseUtils.isVoid(repScreen)) {
                			if(_scWidgetUtils.isWidgetDisabled(repScreen,"removeQtyLink")){
                				_scWidgetUtils.setFocusOnWidgetUsingUid(repScreen, "addQtyLink");
                			} else {
                				_scWidgetUtils.setFocusOnWidgetUsingUid(repScreen, "removeQtyLink");
                			}
                		}
                	}
					/* The below code has been added in order to accomodate changes for BOPIS - 793 */
                	else
                	{
                       	 _scWidgetUtils.setFocusOnWidgetUsingUid(this, "scanProductIdTxt");
                	}
					/* Customization ends */
            	}
        	}
        	
        	this.lastSAPAction = null;
        	this.setCurrentBatchLineMode(null);
        },
        extn_afterScreenLoad: function(event, bEvent, ctrl, args) {
            //BOPIS-1625 Cursor not defaulted to item id field in batch pick screen : Begin
            _scWidgetUtils.setFocusOnWidgetUsingUid(this, "scanProductIdTxt");  
            //BOPIS-1625 Cursor not defaulted to item id field in batch pick screen : End
        },
        //BOPIS-1625 Cursor not defaulted to item id field in batch pick screen : Begin
        setFocusOnFirstItem:function() {
             var batchLinesList = _scScreenUtils.getModel(this, "StoreBatchLines");
             var batchStatus = _scModelUtils.getModelListFromPath("Page.Output.StoreBatch.Status", batchLinesList);
             var itemList = _scModelUtils.getModelListFromPath("Page.Output.StoreBatch.StoreBatchLines.StoreBatchLine", batchLinesList);
             if (!_scBaseUtils.contains(batchStatus, "2000") && !( _scBaseUtils.isVoid(itemList))) {
                 var firstItem = itemList[0];
                 var uniqueKey = _scModelUtils.getStringValueFromPath("ItemID", firstItem)
                                +"|"+_scModelUtils.getStringValueFromPath("UnitOfMeasure", firstItem);
                 this.handlePanelHighlight(uniqueKey);
             }
             //commenting below code as a part of BOPIS-1625 Cursor not defaulted to item id field in batch pick screen
             // if(_iasContextUtils.isMobileContainer()) {
             //     _scWidgetUtils.setFocusOnWidgetUsingUid(this, "addProductButton"); 
             // }
            
        }
        //BOPIS-1625 Cursor not defaulted to item id field in batch pick screen : End
		
		
});
});

