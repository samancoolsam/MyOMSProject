scDefine([
	"dojo/text!./templates/StagingParent.html",
	"scbase/loader!dojo/_base/declare",
	"scbase/loader!sc/plat/dojo/widgets/Screen",
	"scbase/loader!ias/utils/BaseTemplateUtils", "scbase/loader!ias/utils/ContextUtils", "scbase/loader!ias/utils/EventUtils", "scbase/loader!ias/utils/ScreenUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!ias/utils/WizardUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!wsc/components/common/utils/CommonUtils", "scbase/loader!wsc/components/shipment/backroompick/UpdateHoldLocationUI", "scbase/loader!wsc/components/shipment/backroompick/utils/BackroomPickUpUtils", "scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils","scbase/loader!sc/plat/dojo/utils/WidgetUtils"
], function(
	templateText, _dojodeclare, _scScreen, _iasBaseTemplateUtils, _iasContextUtils, _iasEventUtils, _iasScreenUtils, _iasUIUtils, _iasWizardUtils, _scBaseUtils, _scEventUtils, _scModelUtils, _scScreenUtils, _wscCommonUtils, _wscUpdateHoldLocationUI, _wscBackroomPickUpUtils, _wscShipmentUtils, _scWidgetUtils
) {
return _dojodeclare("extn.customScreen.stagingParent.StagingParent", [_scScreen], {
	templateString: templateText,
	uId: "StagingParent",
	packageName: "extn.customScreen.stagingParent",
	className: "StagingParent",

	subscribers: {
            
            local: [{
                eventId: 'addHoldLocationButton_satging_onClick',
                sequence: '32',
                handler: {
                    methodName: "assignOnClick"
                }
			},
            {
                  eventId: 'holdLocationTxtField_satging_onKeyDown'

            ,     sequence: '51'




            ,handler : {
            methodName : "ExtnStagingKeyDown"

             
            }
            },
			{
                eventId: 'afterBehaviorMashupCall',
                sequence: '32',
                handler: {
                    methodName: "extn_afterBehaviorMashupCall"
                }
			},

            {
                eventId: 'afterScreenInit',
                sequence: '32',
                handler: {
                    methodName: "screenInit"
                }
            }	
			,
            {
                eventId: 'afterScreenLoad',
                sequence: '32',
                handler: {
                    methodName: "extn_afterScreenLoad"
                }
            }	
            ]
			},

	assignOnClick: function() {
		var pScreen = _iasUIUtils.getParentScreen(
                this, true);
		shipmentLinePickedModel = _scScreenUtils.getModel(
                    pScreen, "ShipmentModel");
		var sKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentLinePickedModel);
		var lKey = _scScreenUtils.getWidgetByUId(this, "hiddenShipmentKey").value;
		this.shipmentLineKey = lKey;
		var sLocation = _scScreenUtils.getTargetModel(
            this, "HoldLocation_Add_staging", null);

		if (!_scBaseUtils.isVoid(sLocation)){
		 		var inputToMashup = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", sKey ,inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine", {}, inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine.ShipmentLineKey", lKey, inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine.Extn", {}, inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine.Extn.ExtnStagingLocation", _scModelUtils.getStringValueFromPath("HoldLocation", sLocation), inputToMashup);
				_iasUIUtils.callApi(
								this, inputToMashup, "saveHoldLocation_ref", null);
		 }
		 else if(_scBaseUtils.isVoid(sLocation)){
		 	_iasBaseTemplateUtils.showMessage(this, "Staging location is not assigned. Please assign staging location to proceed.", "error", null);
		 }
	},

    ExtnStagingKeyDown: function() {
        if (
            _iasEventUtils.isEnterPressed(
            event)) {
                this.assignOnClick();
        }
    },

    screenInit: function() {
        qty = _scScreenUtils.getWidgetByUId(this, "itemdescriptionLink_test_1").value;
    // OMNI- 3676 BOPIS: Apply All Staging Locations START 
	var pScreen = _iasUIUtils.getParentScreen(this, true);
         var locationModel = _scBaseUtils.getTargetModel(pScreen,"extn_StagingLocationAll");
         var locationfromParentscreen = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.Extn.ExtnStagingLocation",locationModel);
         if(!_scBaseUtils.isVoid(locationfromParentscreen))
         {
         	   this.ownerScreen.isDirtyCheckRequired = false;
            _scWidgetUtils.setWidgetNonMandatory(this, "holdLocationTxtField_satging");
         	 _scWidgetUtils.setValue(this, "holdLocationTxtField_satging", "", null);
         	 if(!_scBaseUtils.equals(qty, "0.00")) {
         	 _scWidgetUtils.setValue(this, "Hold_shipmentLine", "Location:" + locationfromParentscreen, false);
         	 }
         } 
         else
         {
         	var that = this;
        setTimeout(function(){
            if(that.uId == "StagingParent0"){
				// OMNI 3676 BOPIS: Apply All Staging Locations - START commented below code to set focus on the staging location of the parent screen
                // _scWidgetUtils.setFocusOnWidgetUsingUid(that, "holdLocationTxtField_satging");
				// OMNI 3676 BOPIS: Apply All Staging Locations - END
            }
        }, 500);
         }
        if(_scBaseUtils.equals(qty, "0.00")) {
            _scWidgetUtils.disableWidget(this, "addHoldLocationButton_satging");
            _scWidgetUtils.disableWidget(this, "holdLocationTxtField_satging");
        }
    // OMNI- 3676 BOPIS: Apply All Staging Locations END 
    },

	extn_afterBehaviorMashupCall: function(event, bEvent, ctrl, args) {
	var mashupArrayList = _scModelUtils.getModelObjectFromPath("mashupArray", args);
        var mashupArrayListLength = Object.keys(mashupArrayList).length;
        var pScreen = _iasUIUtils.getParentScreen(this, true);
        var shipmentLinePickedModel = _scScreenUtils.getModel(pScreen, "ShipmentModel");
        var parentShipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentLinePickedModel);
        for(var iCount = 0; iCount < mashupArrayListLength; iCount++) {
            var mashupArray = mashupArrayList[iCount];
            var mashupRefId = _scModelUtils.getModelObjectFromPath("mashupRefId", mashupArray);
            if(_scBaseUtils.equals(mashupRefId, "saveHoldLocation_ref")) {
            	var mashupOutputObject = _scModelUtils.getModelObjectFromPath("mashupRefOutput", mashupArray);
                var output = _scModelUtils.getModelObjectFromPath("Shipment" ,mashupOutputObject);
                var sLine = _scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine", output);
                var outputLength = sLine.length;

                for(var i=0; i < outputLength; i++) {
                    var Shipment = sLine[i];
                    var sLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", Shipment);
                    if(_scBaseUtils.equals(this.shipmentLineKey, sLineKey)) {
                    	var HoldLocation = _scModelUtils.getStringValueFromPath("Extn.ExtnStagingLocation", Shipment);
                    	_scWidgetUtils.setValue(this, "Hold_shipmentLine", "Location:" + HoldLocation, false);
						
						//BOPIS-1653: Auto-focus Staging Location - start
						var repeatingScreenlist = this.ownerScreen._allChildScreens;
						var currentScreenIndex = Number(this.uId.split('StagingParent')[1]);
                        var nextScreen = repeatingScreenlist[currentScreenIndex + 1]
                        if (!_scBaseUtils.isVoid(nextScreen)){
							_scWidgetUtils.setFocusOnWidgetUsingUid(nextScreen, "holdLocationTxtField_satging");
                        }
						//BOPIS-1653: Auto-focus Staging Location - end
						
                    	break;
                    }
            }
            for(var i=0;i<parentShipmentLineList.length;i++) {
                var parentShipmentLineKey  = _scModelUtils.getStringValueFromPath("ShipmentLineKey", parentShipmentLineList[i]);
                if(_scBaseUtils.equals(this.shipmentLineKey, parentShipmentLineKey)) {
                    _scModelUtils.setStringValueAtModelPath("ExtnStagingLocation", HoldLocation, parentShipmentLineList[i]);
                }
            }
            this.ownerScreen.isDirtyCheckRequired = false;
            _scWidgetUtils.setWidgetNonMandatory(
                    this, "holdLocationTxtField_satging");
            _scWidgetUtils.setValue(this, "holdLocationTxtField_satging", "", null);
        }
	}
}
,extn_afterScreenLoad: function(event, bEvent, ctrl, args) {
    if(this.uId == "StagingParent0") {
			// OMNI 3676 BOPIS: Apply All Staging Locations - START commented below code to set focus on the staging location of the parent screen
       // _scWidgetUtils.setFocusOnWidgetUsingUid(this, "holdLocationTxtField_satging");
	       // OMNI 3676 BOPIS: Apply All Staging Locations - END 
    }
}

});
});
