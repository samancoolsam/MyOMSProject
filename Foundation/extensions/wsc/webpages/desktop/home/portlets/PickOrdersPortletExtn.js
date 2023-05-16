
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/desktop/home/portlets/PickOrdersPortletExtnUI","scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/utils/ControllerUtils","scbase/loader!ias/utils/UIUtils","scbase/loader!ias/utils/BaseTemplateUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnPickOrdersPortletExtnUI
			,
				_scWidgetUtils
			,
				_scModelUtils
			,
				_scScreenUtils
			,
				_scBaseUtils
			,
				_scControllerUtils
			,
				_iasUIUtils
			,
				_iasBaseTemplateUtils
){ 
	return _dojodeclare("extn.desktop.home.portlets.PickOrdersPortletExtn", [_extnPickOrdersPortletExtnUI],{
	// custom code here
	
	/* This OOB method is overridden to add the details required for adding label count by calling an api */
	initializeScreen: function(
        event, bEvent, ctrl, args) {
			
			var emptyShipment = null;
            emptyShipment = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
            _iasUIUtils.callApi(
            this, emptyShipment, "extn_SFSOrdersCount", null);
            var optionsBean = {};
            _scBaseUtils.setAttributeValue("shouldCallInitApi", true, optionsBean);
			
			//_scWidgetUtils.setAriaLabelForButton(this,"btnViewPickupOrders",_scScreenUtils.getString(this,"arialabel_PickProductInBatchesButton"));
			_scWidgetUtils.setAriaLabelForButton(this,"extn_btnViewPickupOrders",_scScreenUtils.getString(this,"arialabel_PickProductInBatchesButton"));
			
        },
		
		/* This method is called when any new mashup is called and after the mashup completes it function */
		/* If this method doesn't have any errors then handleMashupOutput method will be called */
		handleMashupCompletion: function(
			mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
         _iasBaseTemplateUtils.handleMashupCompletion(
           mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
         },
		 
		 /* This method is called after handleMashupCompletion method is called which check for errors if any */
		/* This method will not be defined or explicitly declared anywhere on the UI and it is OOB behavior to come into this method after handleMashupCompletion method is called */
		handleMashupOutput: function(
			mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) 
		{
			if ( _scBaseUtils.equals(mashupRefId, "extn_SFSOrdersCount")) 
			{ 
				 _scScreenUtils.setModel(
                    this, "extn_SFSOrdersOutput", modelOutput, null);
				this.setCountToLabels();
			}
			
		},
		
		
		/* This method is to set the label count for batch */
		setCountToLabels: function() {
			var labels = null;
            labels = _scModelUtils.createNewModelObjectWithRootKey("labels");
            var shipmentCountModel = null;
            shipmentCountModel = _scScreenUtils.getModel(
            this, "extn_SFSOrdersOutput");
            var numOfRecords = "0";
            if (
            _scModelUtils.getStringValueFromPath("Shipments.TotalNumberOfRecords", shipmentCountModel)) {
                numOfRecords = _scModelUtils.getStringValueFromPath("Shipments.TotalNumberOfRecords", shipmentCountModel);
            }
            var completeTasksTitle = null;
            completeTasksTitle = this.addCountToLabel(
            numOfRecords, "PickOrdersPortlet");
            _scModelUtils.setStringValueAtModelPath("labels.SFSCount", completeTasksTitle, labels);
			
			_scBaseUtils.setModel(
            this, "extn_labelsCount", labels, null);
		},
		
		/* This method is to return the button label for Batch */
		addCountToLabel: function(
        num, label) {
            var inputArray = null;
            inputArray = [];
            inputArray.push(
            num);
            var completePortletTitle = null;
            completePortletTitle = _scScreenUtils.getFormattedString(
            this, label, inputArray);
            return completePortletTitle;
        },
		
		/* This method is called on click of Refresh Link */
		extnRefreshLink : function(
        event, bEvent, ctrl, args) {
            var modelList = null;
            var mashupRefList = null;
            var emptyShipmentModel = null;
            var mashupContext = null;
            mashupContext = _scControllerUtils.getMashupContext(
            this);
            modelList = [];
            mashupRefList = [];
            emptyShipmentModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
            modelList.push(
            emptyShipmentModel);
            mashupRefList.push("extn_SFSOrdersCount");
            _iasUIUtils.callApis(
            this, modelList, mashupRefList, mashupContext, null);
        }
		
		
});
});

