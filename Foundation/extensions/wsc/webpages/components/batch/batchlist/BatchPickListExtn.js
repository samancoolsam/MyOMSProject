
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/batch/batchlist/BatchPickListExtnUI","scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!ias/utils/ScreenUtils","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!ias/utils/ContextUtils","scbase/loader!ias/utils/UIUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnBatchPickListExtnUI
			,
				_scWidgetUtils
			,
				_iasScreenUtils
			,
				_scScreenUtils
			,
				_scModelUtils
			,
				_scBaseUtils
			,
				_wscContextUtils
			,
				_iasUIUtils
){ 
	return _dojodeclare("extn.components.batch.batchlist.BatchPickListExtn", [_extnBatchPickListExtnUI],{
	// custom code here
	
	
	/* Being used for refreshing screen after reset batch functionality is completed in the child screen */
		/* This method has already been implemented in OOB JS Code with blank implementation */
	printBatchList : function()
	{
			this.refreshBatchListScreen();
	},
		
	/* This OOB method has been overridden to hide the OOB filter icon widget */
	initializeScreen : function(){
			_scWidgetUtils.showWidget(this, "pnlScreenDescription", true, null);
			_scWidgetUtils.showWidget(this, "lbl_ScreenTitle", true, null);
			_scWidgetUtils.showWidget(this, "lnkConfigureBatchList", true, null);
			
			/* The below line has been added to hide the filter option */
			_scWidgetUtils.hideWidget(this, "lnkFilterBatchList", true);
			/* Customization ends */
			
			_iasScreenUtils.updateEditorTitle(this, null, "TITLE_BatchPickListWizard");			
			/*_scModelUtils.setStringValueAtModelPath("Picked", "N", this);
			_scModelUtils.setStringValueAtModelPath("InProgress", "Y", this);
			_scModelUtils.setStringValueAtModelPath("NotStarted", "Y", this);*/
			var batchListModel = _scScreenUtils.getModel(this, "getBatchList_output");
			var filterOptionModel = _scModelUtils.getModelObjectFromPath("Page.Output.StoreBatchList.FilterOptions", batchListModel);
			this.checkForDefaultFilter(filterOptionModel);
	},
	
	/* This method is called in the afterScreenInit event before OOB method */
	extnBatchSortMethod : function()
	{
		var input = _scModelUtils.createNewModelObjectWithRootKey("UserUiState");
		_scModelUtils.setStringValueAtModelPath("UserUiState.Definition","SORT_AFTER_PICK",input);
		_iasUIUtils.callApi(this, input, "extn_manageUserUiState_batchSortMethod", null); 
	},
	
	/* This OOB method is overriden to handle the output of new mashup created */
	handleMashupOutput: function(
        mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
			if (
            _scBaseUtils.equals(
            mashupRefId, "batchPick_getBatchListForStore_behavior")) {
                if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "getBatchList_output", modelOutput, null);
                }
                this.checkForNoRecordsFound(
                modelOutput);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "extn_manageUserUiState_batchSortMethod")) {
                var sortMethod = null;
				 sortMethod = _scModelUtils.getStringValueFromPath("UserUiState.Definition", modelOutput);
				_wscContextUtils.addToContext("BATCH_SORT_METHOD", sortMethod);
				this.refreshBatchListScreen();
            }
        }
});
});

