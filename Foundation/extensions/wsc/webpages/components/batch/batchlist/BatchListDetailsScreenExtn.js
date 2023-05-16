
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/batch/batchlist/BatchListDetailsScreenExtnUI","scbase/loader!ias/utils/UIUtils","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!sc/plat/dojo/utils/EventUtils","scbase/loader!ias/utils/PrintUtils","scbase/loader!sc/plat/dojo/utils/ControllerUtils","scbase/loader!ias/utils/BaseTemplateUtils","scbase/loader!ias/utils/ScreenUtils","scbase/loader!ias/utils/ContextUtils","scbase/loader!sc/plat/dojo/utils/PlatformUIFmkImplUtils","scbase/loader!sc/plat/dojo/utils/WidgetUtils"]
,
function(			 
			    _dojodeclare
			,
			    _extnBatchListDetailsScreenExtnUI
			,
				_iasUIUtils
			,
				_scScreenUtils
			,
				_scBaseUtils
			,
				_scModelUtils
			,
				_scEventUtils
			,
				_iasPrintUtils
			,
				_scControllerUtils
			,
				_iasBaseTemplateUtils
			,
				_iasScreenUtils
			,
				_iasContextUtils
			,
				_scPlatformUIFmkImplUtils
			,
				_scWidgetUtils
){ 
	return _dojodeclare("extn.components.batch.batchlist.BatchListDetailsScreenExtn", [_extnBatchListDetailsScreenExtnUI],{
	// custom code here
	
	/* This OOB method is overridden to modify Batch Lable Name based on SCAC */
	initializeScreen: function(){
		_scWidgetUtils.hideWidget(
		this, "TopContent", false);
	  //  if (!_iasContextUtils.isMobileContainer()) {
		//}else {
			// _scWidgetUtils.hideWidget(this, "lbl_AssignedToUser", true);
		//}
		var batchModel = null;
		var status = null;
		batchModel = _scScreenUtils.getModel(this, "getBatchList_output");
		
		var assignedToUserName = _scModelUtils.getStringValueFromPath("AssignedToUsername", batchModel);
		if(assignedToUserName){
			_scWidgetUtils.showWidget(this, "lbl_AssignedToUser", true, null);
		} else {
			_scWidgetUtils.hideWidget(this, "lbl_AssignedToUser",false);
		}
		
		var deptDesc = _scModelUtils.getStringValueFromPath("DepartmentDisplayDesc", batchModel);
		if(deptDesc){
			_scWidgetUtils.showWidget(this, "departmentPanel", true, null);
		} else {
			_scWidgetUtils.hideWidget(this, "departmentPanel",false);
		}
		
		status = _scModelUtils.getStringValueFromPath("Status", batchModel);
		if (status === "1000") {
			_scWidgetUtils.showWidget(this, "lnk_PickAction", true, null);
			//OMNI-8715 : START
			var StoreBatchConfig = batchModel.StoreBatchConfigList.StoreBatchConfig;
			for(var i in StoreBatchConfig){
				var sAttr = _scModelUtils.getStringValueFromPath("Name", StoreBatchConfig[i]);
				var sSCAC = _scModelUtils.getStringValueFromPath("Value", StoreBatchConfig[i]);
				if(_scBaseUtils.equals(sAttr,"CarrierServiceCode")&&!(_scBaseUtils.isVoid(sSCAC)) && _scBaseUtils.equals(sSCAC,"Standard Overnight") || _scBaseUtils.equals(sSCAC,"2 Day")){
					_scWidgetUtils.setLabel(this,"lbl_batchNo",_scScreenUtils.getString(this, "Label_NewExpressBatchNumber"));
					_scWidgetUtils.addClass(this, "lbl_batchNo", "extn_Express");
					break;
				}else{
					_scWidgetUtils.setLabel(this,"lbl_batchNo",_scScreenUtils.getString(this, "Label_NewBatchNumber"));
				}
			}
			//_scWidgetUtils.setLabel(this,"lbl_batchNo",_scScreenUtils.getString(this, "Label_NewBatchNumber"));
			//OMNI-8715 : END
		}else if (status === "1100" || status === "2000") {
			_scWidgetUtils.showWidget(this, "lnk_InProgress", true, null);
			//OMNI-8715 : START
			var StoreBatchConfig = batchModel.StoreBatchConfigList.StoreBatchConfig;
			for(var i in StoreBatchConfig){
				var sAttr = _scModelUtils.getStringValueFromPath("Name", StoreBatchConfig[i]);
				var sSCAC = _scModelUtils.getStringValueFromPath("Value", StoreBatchConfig[i]);
				if(_scBaseUtils.equals(sAttr,"CarrierServiceCode")&&!(_scBaseUtils.isVoid(sSCAC)) && _scBaseUtils.equals(sSCAC,"Standard Overnight") || _scBaseUtils.equals(sSCAC,"2 Day")){
					_scWidgetUtils.setLabel(this,"lbl_batchNo",_scScreenUtils.getString(this, "Label_ExpressBatchNumber"));
					_scWidgetUtils.addClass(this, "lbl_batchNo", "extn_Express");
					break;
				}else{
					_scWidgetUtils.setLabel(this,"lbl_batchNo",_scScreenUtils.getString(this, "Label_BatchNumber"));
				}
			}
			//_scWidgetUtils.setLabel(this,"lbl_batchNo",_scScreenUtils.getString(this, "Label_BatchNumber"));
			//OMNI-8715 : END
			_scWidgetUtils.showWidget(this, "lblStatus", true, null);
		}else if (status === "3000") {
			_scWidgetUtils.showWidget(this, "batchPickStatusImage", true, null);
		}else if(status === "9000") {
			_scWidgetUtils.showWidget(this, "batchPickCancelledStatusImage", true, null);
		}
		
		var urlString = null;
		urlString = _scModelUtils.getStringValueFromPath("ImageUrl", batchModel);
		if (!(_scBaseUtils.isVoid(urlString))) {
			var imageUrlModel = null;
			imageUrlModel = _scModelUtils.createNewModelObjectWithRootKey("CommonCode");
			_scModelUtils.setStringValueAtModelPath("CommonCode.CodeLongDescription", urlString, imageUrlModel);
			_scModelUtils.setStringValueAtModelPath("CommonCode.CodeShortDescription", _scModelUtils.getStringValueFromPath("ImageAltText", batchModel), imageUrlModel);
			_scScreenUtils.setModel(this, "clockImageBindingValues", imageUrlModel, null);
			_scWidgetUtils.changeImageTitle(
					this, "img_TimeRmnClock", _scModelUtils.getStringValueFromPath("ImageAltText", batchModel));
		}
		
		var labelDepartment = _scScreenUtils.getWidgetByUId(this,"lbl_Department");
		var labelDepartmentNode = labelDepartment.domNode;
		var childDomNode = this.customGetElementsByClassName("dijitInputContainer",labelDepartmentNode)[0] ;
		if(childDomNode.offsetWidth < childDomNode.scrollWidth)
		{
			 _scWidgetUtils.showWidget(this, "lnk_MoreLinkForDepartment", true, null);
		}
	},
	
	/*This method is called on click of Reset Batch button on store UI */
	extnResetBatch : function(event, bEvent, ctrl, args)
	{
		var argBean = null;
		argBean = {};
		
		var batchListModel = _scScreenUtils.getModel(this, "getBatchList_output");
		var storeBatchKey = _scModelUtils.getStringValueFromPath("StoreBatchKey", batchListModel);
		
		if(_scBaseUtils.isVoid(storeBatchKey))
		{
			var voidMsg = _scScreenUtils.getString(this, "extn_Batch_Reset_Not_Created");
			_iasScreenUtils.showInfoMessageBoxWithOk(this,voidMsg,null,argBean);
		}
		else
		{
			var msg = _scScreenUtils.getString(this, "extn_Batch_Reset_Success");
			_scScreenUtils.showConfirmMessageBox(this, msg, "extn_ConfirmResetOutput", null, argBean);
		}
	},
	
	
	/*This method is called after clicking on Yes/No options in Reset Batch message box */
	extn_ConfirmResetOutput : function(result, args) 
	{
		if (_scBaseUtils.equals(result, "Ok"))
		{
			var apiInputForReset = {};
			apiInputForReset = _scModelUtils.createModelObjectFromKey("StoreBatch", apiInputForReset);
			
			var batchListModel = _scScreenUtils.getModel(this, "getBatchList_output");
			_scModelUtils.setStringValueAtModelPath("StoreBatch.Status",  _scModelUtils.getStringValueFromPath("Status", batchListModel), apiInputForReset);
			_scModelUtils.setStringValueAtModelPath("StoreBatch.StoreBatchKey",  _scModelUtils.getStringValueFromPath("StoreBatchKey", batchListModel), apiInputForReset);
			_scModelUtils.setStringValueAtModelPath("StoreBatch.BatchNo",  _scModelUtils.getStringValueFromPath("BatchNo", batchListModel), apiInputForReset);
			_scModelUtils.addModelToModelPath("StoreBatch.ShipmentLines", _scModelUtils.getModelObjectFromPath("ShipmentLines",batchListModel),apiInputForReset);
			_scModelUtils.addModelToModelPath("StoreBatch.StoreBatchConfigList", _scModelUtils.getModelObjectFromPath("StoreBatchConfigList",batchListModel),apiInputForReset);
			
			_iasUIUtils.callApi(this, apiInputForReset, "extn_ResetBatch", null); 
		}
		else{}
	},
	
	/* This method is called after handleMashupCompletion method is called which check for errors if any */
	/* This method will not be defined or explicitly declared anywhere on the UI and it is OOB behavior to come into this method after handleMashupCompletion method is called */
	handleMashupOutput: function(
        mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) 
	{
		if ( _scBaseUtils.equals(mashupRefId, "extn_ResetBatch")) 
		{ 
			this.resetMethodForCallingParentScreen();
        }
		if ( (_scBaseUtils.equals(mashupRefId, "extn_PrintBatch")) || (_scBaseUtils.equals(mashupRefId, "extn_PrintBatchNew")) ) 
		{ 
			var msg = _scScreenUtils.getString(this, "extn_Print_Success");
			_iasScreenUtils.showInfoMessageBoxWithOk(this,msg,"onCloseOfPrintSuccess",{});
		}
	},
	
	/* This method is called after handling the output of mashup with mashuprefid="extn_PrintBatch/extn_PrintBatchNew" */
	onCloseOfPrintSuccess : function(result, args)
	{
		if (_scBaseUtils.equals(result, "Ok")) 
		{
			this.resetMethodForCallingParentScreen();
		}
	},
	
	/* This method is called when any new mashup is called and after the mashup completes it function */
	/* If this method doesn't have any errors then handleMashupOutput method will be called */
	handleMashupCompletion: function(
         mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
         _iasBaseTemplateUtils.handleMashupCompletion(
           mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
         },
	
	/* This method is called after handling the output of mashup */
	resetMethodForCallingParentScreen : function()
	{
		_scEventUtils.fireEventToParent(this,"lnk_PrintBatchList_onClick",{});
	},
	
	/*This method is called on click of Print Batch button in Batch */
	extnPrintBatch : function(event, bEvent, ctrl, args)
	{
		var argBean = null;
		argBean = {};
		
		var batchListModel = _scScreenUtils.getModel(this, "getBatchList_output");		
		var storeBatchKey =  _scModelUtils.getStringValueFromPath("StoreBatchKey", batchListModel);
		
		if(_scBaseUtils.isVoid(storeBatchKey))
		{
			var msg = _scScreenUtils.getString(this, "extn_Print_Confirmation_New");
			_scScreenUtils.showConfirmMessageBox(this, msg, "extn_ConfirmPrintBatch", null, argBean);
		}
		else
		{
			var msg = _scScreenUtils.getString(this, "extn_Print_Confirmation");
			_scScreenUtils.showConfirmMessageBox(this, msg, "extn_ConfirmPrintBatch", null, argBean);
		}
	},
	
	/*This method is called after clicking on Yes/No options in Print Batch message box */
	extn_ConfirmPrintBatch: function(result, args) 
	{
		if (_scBaseUtils.equals(result, "Ok"))
		{
			var apiInputForPrintBatch = {};
			var batchListModel = _scScreenUtils.getModel(this, "getBatchList_output");		
			var storeBatchKey =  _scModelUtils.getStringValueFromPath("StoreBatchKey", batchListModel);
			
			var shipmentLinesListModel={};
			var shipmentLineModel = {};
					shipmentLineModel = _scModelUtils.createModelObjectFromKey("ShipmentLine", shipmentLinesListModel);

			if(_scBaseUtils.isVoid(storeBatchKey))
			{
				apiInputForPrintBatch = _scModelUtils.createModelObjectFromKey("StoreBatch", apiInputForPrintBatch);

				var shipmentLine =  _scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine", batchListModel);
				var shipmentLineKeyModel = [];
				for(var shipmentLineIteration in shipmentLine)
				{
					var shipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", shipmentLine[shipmentLineIteration]);
					shipmentLineKeyModel.push({
                         'ShipmentLineKey': shipmentLineKey
                     });
				}
				_scModelUtils.addModelToModelPath("ShipmentLine", shipmentLineKeyModel,shipmentLineModel);
				_scModelUtils.setStringValueAtModelPath("StoreBatch.BatchType", "SORT_AFTER_PICK", apiInputForPrintBatch);
				_scModelUtils.setStringValueAtModelPath("StoreBatch.BatchNo",  _scModelUtils.getStringValueFromPath("BatchNo", batchListModel), apiInputForPrintBatch);
				_scModelUtils.addModelToModelPath("StoreBatch.ShipmentLines", shipmentLineModel,apiInputForPrintBatch);
				// Start OMNI-8715
				_scModelUtils.addModelToModelPath("StoreBatch.StoreBatchConfigList", _scModelUtils.getModelObjectFromPath("StoreBatchConfigList",batchListModel),apiInputForPrintBatch);
				// End OMNI-8715
				_iasUIUtils.callApi(this, apiInputForPrintBatch, "extn_PrintBatchNew", null);
			}
			else
			{
				apiInputForPrintBatch = _scModelUtils.createModelObjectFromKey("Print", apiInputForPrintBatch);
				_scModelUtils.setStringValueAtModelPath("Print.PickticketNo",  _scModelUtils.getStringValueFromPath("BatchNo", batchListModel), apiInputForPrintBatch);
				_scModelUtils.setStringValueAtModelPath("Print.PageNo",  "", apiInputForPrintBatch);
				_scModelUtils.setStringValueAtModelPath("Print.PickTicketPrinted",  "Y", apiInputForPrintBatch);
				_scModelUtils.setStringValueAtModelPath("Print.PrinterID",  "PCK_TICKET_STN1", apiInputForPrintBatch);
				_iasUIUtils.callApi(this, apiInputForPrintBatch, "extn_PrintBatch", null); 
			}
		}
		else{}
	},
	// code changes for SLA indicator : Begin
	extn_afterInitializeScreen: function(event, bEvent, ctrl, args) {
	  	_scWidgetUtils.hideWidget(this, "lbl_timeRemaining",false);
	  	_scWidgetUtils.hideWidget(this, "img_TimeRmnClock",false);
 	}
 	// code changes for SLA indicator : End
});
});




