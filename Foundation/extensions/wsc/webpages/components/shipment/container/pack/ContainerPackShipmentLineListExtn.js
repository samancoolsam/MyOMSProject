
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/container/pack/ContainerPackShipmentLineListExtnUI","scbase/loader!ias/utils/EventUtils","scbase/loader!ias/utils/BaseTemplateUtils", "scbase/loader!ias/utils/RepeatingScreenUtils", "scbase/loader!ias/utils/ScreenUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/ControllerUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/RepeatingPanelUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!wsc/components/shipment/container/pack/ContainerPackContainerViewUI", "scbase/loader!wsc/components/shipment/container/pack/ContainerPackUtils","scbase/loader!sc/plat/dojo/Userprefs","scbase/loader!sc/plat/dojo/utils/ResourcePermissionUtils","scbase/loader!dojo/dom-attr"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnContainerPackShipmentLineListExtnUI
			 ,
			 	_iasEventUtils
			 , 
			 	_iasBaseTemplateUtils, _iasRepeatingScreenUtils, _iasScreenUtils, _iasUIUtils, _scBaseUtils, _scControllerUtils, _scEventUtils, _scModelUtils, _scRepeatingPanelUtils, _scScreenUtils, _scWidgetUtils, _wscContainerPackContainerViewUI, _wscContainerPackUtils,_scUserprefs,_scResourcePermissionUtils, dDomAttr
){ 
	return _dojodeclare("extn.components.shipment.container.pack.ContainerPackShipmentLineListExtn", [_extnContainerPackShipmentLineListExtnUI],{
	// custom code here
	extn_afterInitializeScreen: function(event, bEvent, ctrl, args) {
    	//OMNI-66083 START
		var getCommonCodeInput = {};				
		getCommonCodeInput = _scModelUtils.createModelObjectFromKey("CommonCode", getCommonCodeInput);
		_scModelUtils.setStringValueAtModelPath("CommonCode.CodeType", "TGL_RCP_WEB_SOM_UI" , getCommonCodeInput);
		_scModelUtils.setStringValueAtModelPath("CommonCode.CodeValue", "NON_EDITABLE_ADDQTY_SCANNEDQTY" , getCommonCodeInput);
		_iasUIUtils.callApi(this, getCommonCodeInput, "extn_getFlagToDisableAddQtyAndReadOnlyScannedQty", null); 
		//OMNI-66083 END
		var aParentScreen = _iasUIUtils.getParentScreen(this, true);
		var bParentScreen = _iasUIUtils.getParentScreen(aParentScreen, true);
		var aparentShipmenDetailsModel = _scScreenUtils.getModel(aParentScreen, "getShipmentDetails_output");
		var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", aparentShipmenDetailsModel);
		var activeContainerModel = _scScreenUtils.getModel(bParentScreen, "activeContainerModel")
		var activeShipmentContainerKey = _scModelUtils.getStringValueFromPath("Container.ShipmentContainerKey", activeContainerModel);
		// var getContainerListModel = _scUserprefs.getProperty("getContainerList_Out");
		var getContainerListModel = _scUserprefs.getProperty(shipmentKey);
		var getContainerList = _scModelUtils.getStringValueFromPath("Page.Output.Containers.Container", getContainerListModel);
		if (!_scBaseUtils.isVoid(getContainerList)) {
	        for (var j=0;j<getContainerList.length;j++) {
	            var shipmentContainerKey = _scModelUtils.getStringValueFromPath("ShipmentContainerKey", getContainerList[j]);
	            if (_scBaseUtils.equals(shipmentContainerKey, activeShipmentContainerKey)) {
	                var trackingNo = _scModelUtils.getStringValueFromPath("TrackingNo", getContainerList[j]);
	                if (!_scBaseUtils.isVoid(trackingNo)) {
	                	var totalQuantityRemaining = _scScreenUtils.getWidgetByUId(this, "remainingQty").value;
                        if(!_scBaseUtils.equals(totalQuantityRemaining,"0.00 EACH")) {
                        	_iasScreenUtils.showInfoMessageBoxWithOk(this, "Please add new container or select other container to proceed.", "infoCallBack", null);
	                    }
	                    _scWidgetUtils.disableWidget(this, "unpackQtyLink", false);
	                    _scWidgetUtils.disableWidget(this, "manualQtyInContainerTxtBox", false);
	                    _scWidgetUtils.disableWidget(this, "packQtyLink", false);
	                }
	                else {
                        _scWidgetUtils.enableWidget(this, "unpackQtyLink", false);
	                    _scWidgetUtils.enableWidget(this, "manualQtyInContainerTxtBox", false);
	                    _scWidgetUtils.enableWidget(this, "packQtyLink", false); 
	                }
	            }
	        }
	    }
	},
		//OMNI-66083 START
	    extn_AfterBehaviorMashupCall: function(event, bEvent, ctrl, args){
			var mashupRefList = _scModelUtils.getModelObjectFromPath("mashupArray", args);
			if (!_scBaseUtils.isVoid(mashupRefList)) {
				for (var i = 0; i < mashupRefList.length; i++) {
					var mashupRefid = _scModelUtils.getStringValueFromPath("mashupRefId", mashupRefList[i]);
						if (_scBaseUtils.equals(mashupRefid, "extn_getFlagToDisableAddQtyAndReadOnlyScannedQty")) {
							var modelOutput = mashupRefList[i].mashupRefOutput;
							var isManualEntry = modelOutput.CommonCodeList.CommonCode[0].CodeShortDescription;
								if(!(_scBaseUtils.isVoid(isManualEntry)) && (_scBaseUtils.equals(isManualEntry,"Y"))){
									_scWidgetUtils.disableWidget(this, "packQtyLink",true);
									var fs = this.getWidgetByUId("manualQtyInContainerTxtBox");
									dDomAttr.set(fs.textbox, "readonly", true);						 
								 }
						}
				}
			}
			
		},
		//OMNI-66083 END
	/* This OOB method has been overridden to nullify the on click event of image link in products tab */
	openProductDetails: function(
        event, bEvent, ctrl, args) {
            //do nothing
        },

     hideOrShowShortageRelatedWidgets: function(
        event, bEvent, ctrl, args) {
            _scScreenUtils.clearScreen(
            this);
            if (!(
            _scResourcePermissionUtils.hasPermission("WSC000024"))) {
                _scWidgetUtils.hideWidget(
                this, "divQuantity", false);
                _scWidgetUtils.showWidget(
                this, "QtyInContainer", false, "");
            }
            if (
            _scWidgetUtils.isWidgetVisible(
            this, "updateButton")) {
                _scWidgetUtils.hideWidget(
                this, "updateButton", false);
            }
            _scScreenUtils.isDirty(
            _iasUIUtils.getParentScreen(
            this, true), null, true);
            var shipmentLine_Src = null;
            var isPackComplete = null;
            shipmentLine_Src = _scScreenUtils.getModel(
            this, "shipmentLine_Src");
            isPackComplete = _scModelUtils.getStringValueFromPath("ShipmentLine.IsPackComplete", shipmentLine_Src);
            shortageQty = _scModelUtils.getNumberValueFromPath("ShipmentLine.ShortageQty", shipmentLine_Src);
            if (
            _scBaseUtils.numberGreaterThan(
            shortageQty, 0)) {
                _scWidgetUtils.showWidget(
                this, "shortagelbl", false, "");
            }
            if (!(
            _scBaseUtils.isVoid(
            isPackComplete))) {
                _scWidgetUtils.hideWidget(
                //Start - OMNI-3680 BOPIS:Record Shortage Button
                //this, "shortageResolutionLink", false);
				        this, "extn_button", false);
                //End - OMNI-3680 BOPIS:Record Shortage Button
                cssClass = [];
                cssClass.push("singleAction");
                _scWidgetUtils.addClass(
                this, "link_packInstruction", cssClass);
                if (
                _scBaseUtils.equals(
                _scModelUtils.getNumberValueFromPath("ShipmentLine.Quantity", shipmentLine_Src), 0)) {
                    _scWidgetUtils.showWidget(
                    this, "productShortedImage", false, "");
                } else {
                    _scWidgetUtils.showWidget(
                    this, "imgScanComplete", false, "");
                    _scWidgetUtils.showWidget(
                    this, "lblPacked", false, "");
                }
            } else {
                _scWidgetUtils.showWidget(
                //Start - OMNI-3680 BOPIS:Record Shortage Button
                //this, "shortageResolutionLink", false, "");
				        this, "extn_button", false, "");
                //End - OMNI-3680 BOPIS:Record Shortage Button
                _scWidgetUtils.hideWidget(
                this, "imgScanComplete", false);
                _scWidgetUtils.hideWidget(
                this, "lblPacked", false);
                cssClass = [];
                cssClass.push("singleAction");
                _scWidgetUtils.removeClass(
                this, "link_packInstruction", cssClass);
            }
            /*var packInstructionModel = null;
            packInstructionModel = _scModelUtils.getModelObjectFromPath("ShipmentLine.Instructions.Instruction", shipmentLine_Src);
            if (
            _scBaseUtils.isVoid(
            packInstructionModel)) {
                _scWidgetUtils.hideWidget(
                this, "link_packInstruction", false);
            } else {
                if (!(
                _scWidgetUtils.isWidgetVisible(
                this, "link_packInstruction"))) {
                    _scWidgetUtils.showWidget(
                    this, "link_packInstruction", false, "");
                }
            }*/
            //BOPIS-1776-BEGIN
			_scWidgetUtils.hideWidget(this, "link_packInstruction", false);
			//BOPIS-1776-END
        }
});
});

