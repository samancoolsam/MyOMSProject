
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/common/screens/PickorPackWarningDialogExtnUI","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils","scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!ias/utils/BaseTemplateUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!ias/utils/UIUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnPickorPackWarningDialogExtnUI
			,
				_scBaseUtils
			,
				_scScreenUtils
			,
				_wscShipmentUtils
			,
				_scWidgetUtils
			,
				_iasBaseTemplateUtils
			,
				_scModelUtils
			,
				_iasUIUtils
){ 
	return _dojodeclare("extn.components.shipment.common.screens.PickorPackWarningDialogExtn", [_extnPickorPackWarningDialogExtnUI],{
	// custom code here
	initializeScreen: function(
        event, bEvent, ctrl, args) {
            var warningString = null;
            var shipmentModel = null;
			var setModelOptions = null;
            var context = null;
            context = _scBaseUtils.getAttributeValue("screen.params.binding.Action", false, args);
			setModelOptions = {};
			
			var contextNameSpaceModel = null;
					contextNameSpaceModel = _scModelUtils.createNewModelObjectWithRootKey("Context");
			_scModelUtils.setStringValueAtModelPath("Context.ContextValue", context, contextNameSpaceModel);
			
			_scScreenUtils.setModel(
                this, "extn_ContextNamespace", contextNameSpaceModel, setModelOptions);
			
			
            shipmentModel = _scScreenUtils.getModel(
            this, "Shipment");
			
			var assignedToUserID = shipmentModel.Shipment.AssignedToUserId;
			var shipNodeExtn = shipmentModel.Shipment.ShipNode;
			if(!_scBaseUtils.isVoid(assignedToUserID))
			{
				var getUserListAPIInput = null;
					getUserListAPIInput = _scModelUtils.createNewModelObjectWithRootKey("User");
				_scModelUtils.setStringValueAtModelPath("User.DisplayUserID", assignedToUserID, getUserListAPIInput);
				_scModelUtils.setStringValueAtModelPath("User.OrganizationKey", shipNodeExtn, getUserListAPIInput);
				_iasUIUtils.callApi(
				this, getUserListAPIInput, "extn_getUserListByUserID", null);
			}
			
            
        },
		
		handleMashupCompletion: function(
         mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
         _iasBaseTemplateUtils.handleMashupCompletion(
           mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
         },
		 
		 handleMashupOutput: function(
        mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) 
		{
			if ( _scBaseUtils.equals(mashupRefId, "extn_getUserListByUserID")) 
			{ 
				var userName = modelOutput.UserList.User[0].Username;
				
				var shipmentData = null;
				shipmentData = _scScreenUtils.getModel(this, "Shipment");
				_scModelUtils.setStringValueAtModelPath("Shipment.AssignedToUserId", userName, shipmentData);
				 _scScreenUtils.setModel(
                    this, "Shipment", shipmentData, null); 
					
				var contextData = null;
				contextData = _scScreenUtils.getModel(this, "extn_ContextNamespace");				
				var context = contextData.Context.ContextValue;
				
				var warningString = null;
				var shipmentData = null;
				shipmentModel = _scScreenUtils.getModel(
					this, "Shipment");
				 warningString = _wscShipmentUtils.getInProgressWarningString(
				this, shipmentModel, context);
				_scWidgetUtils.setValue(
				this, "confirmationMessage", warningString, false);
				_scWidgetUtils.hideWidget(
				this, "Popup_navigationPanel", false);
			}
		}
});
});

