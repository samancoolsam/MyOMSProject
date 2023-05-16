scDefine([
    "dojo/text!./templates/ScanShipmentLabel.html",
    "scbase/loader!dojo/_base/declare",
    "scbase/loader!sc/plat/dojo/widgets/Screen",
    "scbase/loader!sc/plat/dojo/utils/WidgetUtils",
    "scbase/loader!sc/plat/dojo/utils/ScreenUtils",
    "scbase/loader!sc/plat/dojo/utils/BaseUtils",
    "scbase/loader!ias/utils/UIUtils",
    "scbase/loader!sc/plat/dojo/utils/ModelUtils",
    "scbase/loader!ias/utils/BaseTemplateUtils",
    "scbase/loader!wsc/mobile/home/utils/MobileHomeUtils",
    "scbase/loader!ias/utils/EventUtils",
    "scbase/loader!ias/utils/ContextUtils",
    "scbase/loader!dojo/_base/connect",
    "scbase/loader!dojo/dom-attr",
    "scbase/loader!ias/utils/ScreenUtils"
], function(
    templateText,
    _dojodeclare,
    _scScreen,
    _scWidgetUtils,
    _scScreenUtils,
    _scBaseUtils,
    _iasUIUtils,
    _scModelUtils,
    _iasBaseTemplateUtils,
    _wscMobileHomeUtils,
    _iasEventUtils,
    _iasContextUtils,
    dConnect,
    dDomAttr,
    _iasscScreenUtils
) {
    return _dojodeclare("extn.mobile.home.AuditStagedShipment.AuditScanScreen.ScanShipmentLabel", [_scScreen], {
        templateString: templateText,
        uId: "ScanShipmentLabel",
        packageName: "extn.mobile.home.AuditStagedShipment.AuditScanScreen",
        className: "ScanShipmentLabel",
        title: "Audit Staging Locations",
        screen_description: "Audit Staging Locations",
        namespaces: {
            targetBindingNamespaces: [{
                description: 'The input to the  mashup.',
                // value: 
            }],
            sourceBindingNamespaces: [{
                description: "The details of the mashup",
                // value: 
            }]
        },
        subscribers: {
            local: [{
                    eventId: 'afterScreenLoad',
                    sequence: '32',
                    handler: {
                        methodName: "extn_afterScreenLoad"
                    }
                },
                {
                    eventId: 'extn_scanLabelNo_onClick',
                    sequence: '32',
                    handler: {
                        methodName: "scanOnClick"
                    }
                },
                {
                    eventId: 'extn_txt_LabelNo_onKeyUp',
                    sequence: '32',
                    handler: {
                        methodName: "scanOnEnter"
                    }
                }, {
                    eventId: 'extn_btnFinish_onClick',
                    sequence: '32',
                    handler: {
                        methodName: "finishBtnOnClick"
                    }
                },
            ]
        },
        setInitialized: function(event, bEvent, ctrl, args) {
            this.isScreeninitialized = true;
        },
        extn_afterScreenLoad: function(event, bEvent, ctrl, args) {
            /* OMNI-69376 changes starts and ends here */
            this.scWidgetDirty = true;
            _iasContextUtils.addToContext("LoadedFromShipmentScanScreen", "AuditShipmentScanScreen");
            
            /* OMNI-72079 Fix - Start*/
		    _scWidgetUtils.setFocusOnWidgetUsingUid(this, "extn_txt_LabelNo");
		    /* OMNI-72079 Fix - End */
            
            /*OMNI-69643 & OMNI-69646 Shipment Count & Continue Changes - START*/
            /*OMNI-79207 Verbiage change TO "Shipments Ready for Customer Pick Up" - START*/
            var vIn_Progress_Session = _iasContextUtils.getFromContext("In_Progress_Session");
            if (!_scBaseUtils.isVoid(vIn_Progress_Session) && _scBaseUtils.equals(vIn_Progress_Session, 'Y')) {
                _scWidgetUtils.setValue(this, "extn_lastScannedElapsedTime", ": "+_iasContextUtils.getFromContext("BatchScanDuration") + " hrs.", null);
                _scWidgetUtils.setValue(this, "extn_lastScannedShipmentCount", _iasContextUtils.getFromContext("ShipmentsScanned"), null);
                _scWidgetUtils.setValue(this, "extn_lastScannedCancelledShipmentCount", _iasContextUtils.getFromContext("ShipmentsCancelled"), null);
                _scWidgetUtils.setValue(this, "extn_StagedShipmentsCount_Count", _iasContextUtils.getFromContext("StagedShipmentsCount"), null);
                _scWidgetUtils.setValue(this, "extn_StagedShipmentsCount", " Shipments Ready for Customer Pick Up", null);
				//this.setScreenDirty(this,parseInt(_iasContextUtils.getFromContext("ShipmentsScanned")));
            } else {
                _scWidgetUtils.setValue(this, "extn_lastScannedElapsedTime", ": "+"00:00:00 hrs.", null);
                _scWidgetUtils.setValue(this, "extn_lastScannedShipmentCount", "0", null);
                _scWidgetUtils.setValue(this, "extn_lastScannedCancelledShipmentCount", "0", null);
                _scWidgetUtils.setValue(this, "extn_StagedShipmentsCount_Count", _iasContextUtils.getFromContext("StagedShipmentsCount"), null);
                _scWidgetUtils.setValue(this, "extn_StagedShipmentsCount", " Shipments Ready for Customer Pick Up", null);
            }
            /*OMNI-69643 & OMNI-69646 Shipment Count & Continue Changes - END*/
            /*OMNI-79207 Verbiage change TO "Shipments Ready for Customer Pick Up" - END*/
        },
        initializeScreen: function(event, bEvent, ctrl, args) {},
        _removeReadOnlyState: function() {},
        scanOnEnter: function(event, bEvent, ctrl, args) {
            /* OMNI-72706 Fix - START */
            _scWidgetUtils.setFocusOnWidgetUsingUid(this, "extn_txt_LabelNo");
            /* OMNI-72706 Fix - END */
            if (_iasEventUtils.isEnterPressed(event)) {
                this.scanOnClick(event, bEvent, ctrl, args);
            }
        },
        scanOnClick: function(event, bEvent, ctrl, args) {
             /* OMNI-72706 Fix - START */
            _scWidgetUtils.setFocusOnWidgetUsingUid(this, "extn_txt_LabelNo");
            /* OMNI-72706 Fix - END */
            var targetModel = _scBaseUtils.getTargetModel(this, "LabelNo_Output");
            var currentNode = _iasContextUtils.getFromContext("CurrentStore");
            var AcadScanBatchHeaderKey = _iasContextUtils.getFromContext("AcadScanBatchHeaderKey");
			var isValidLabelNo = this.isValidLabelNo(targetModel);
            if (!_scBaseUtils.isVoid(targetModel) && _scBaseUtils.booleanEquals(isValidLabelNo,true) ) {
                var strLabelNo = _scModelUtils.getStringValueFromPath("ACADScanBatchDetails.LabelNo", targetModel);
                _scModelUtils.setStringValueAtModelPath("ACADScanBatchDetails.LabelNo", strLabelNo, targetModel);
                _scModelUtils.setStringValueAtModelPath("ACADScanBatchDetails.StoreNo", currentNode, targetModel);
                _scModelUtils.setStringValueAtModelPath("ACADScanBatchDetails.AcadScanBatchHeaderKey", AcadScanBatchHeaderKey, targetModel);
                _scWidgetUtils.setValue(this, "extn_txt_LabelNo", null, null);
                _iasUIUtils.callApi(this, targetModel, "getScannedLabelDetails", null);
            } else {
				_scWidgetUtils.setValue(this, "extn_txt_LabelNo", null , null);
				_scWidgetUtils.setFocusOnWidgetUsingUid(this, "extn_txt_LabelNo");
				_iasBaseTemplateUtils.showMessage(this, " Not a Valid Shipment. ", "error", null);
            }
        },
        infoCallBack: function() {},
        handleMashupCompletion: function(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
            //iasBaseTemplateUtils.handleMashupCompletion(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
            var mashupRefId = _scModelUtils.getStringValueFromPath("0.mashupRefId", inputData);
            if ("getScannedLabelDetails" == mashupRefId) {
                this.hideWidgets();
                var mashupOutput = _scModelUtils.getStringValueFromPath("0.mashupRefOutput", mashupRefList);
                var sShipmentNo = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.ShipmentNo", mashupOutput);
                var sOrderNo = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.OrderNo", mashupOutput);
                var shipmentStatus = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.IsCancelled", mashupOutput);
                //var shipmentStatus = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.Status", mashupOutput );
                // OMNI-69096 START
                var validLabelStatus = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.IsValidLabel", mashupOutput);
                // OMNI-69096 END
                if (!_scBaseUtils.isVoid(sShipmentNo) && _scBaseUtils.equals(shipmentStatus, 'N')) {
                    this.handleActiveShipmentLabel(mashupOutput);
                } else if (_scBaseUtils.equals(shipmentStatus, 'Y')) {
                    this.showCancelledPopup();
                    this.handleCancelledShipmentLabel(mashupOutput);
                } // OMNI-69096 START
                else if (_scBaseUtils.equals(validLabelStatus, 'N')) {
                    _iasBaseTemplateUtils.showMessage(this, "Not a Valid Shipment.", "error", null);
                }
                // OMNI-69096 END
            }
            _scWidgetUtils.setFocusOnWidgetUsingUid(this, "extn_txt_LabelNo");
            _scWidgetUtils.setValue(this, "extn_scanLabelNo", null, null);
			//this.setScreenDirty(this,parseInt(_scWidgetUtils.getValue(this, "extn_lastScannedShipmentCount")));
        },
        handleActiveShipmentLabel: function(args) {
            var mashupOutput = args;
	    //OMNI-73999 START
            this.showWidgets();
	    //OMNI-73999 END
            _scWidgetUtils.setValue(this, "extn_lastScannedShipmentNo", ": "+_scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.ShipmentNo", mashupOutput), null);
            _scWidgetUtils.setValue(this, "extn_lastScannedOrderNo",": "+ _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.OrderNo", mashupOutput), null);
            _scWidgetUtils.setValue(this, "extn_lastScannedElapsedTime",": "+ _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ElapsedTime", mashupOutput) + " hrs.", null);
            _scWidgetUtils.setValue(this, "extn_lastScannedShipmentCount", _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ScannedShipmentsCount", mashupOutput), null);
            _scWidgetUtils.setValue(this, "extn_lastScannedCancelledShipmentCount", _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.CancelledShipmentsCount", mashupOutput), null);
            _scWidgetUtils.setValue(this, "extn_scanLabelNo", null, null);
	    //OMNI-73999
	    _scWidgetUtils.setValue(this, "extn_FirstName", ": "+_scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.CustomerFirstName", mashupOutput), null);
            _scWidgetUtils.setValue(this, "extn_LastName", ": "+_scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.CustomerLastName", mashupOutput), null);
            _scWidgetUtils.setValue(this, "extn_AltPickup_FirstName", ": "+_scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.AlternateCustFirstName", mashupOutput), null);
            _scWidgetUtils.setValue(this, "extn_AltPickup_LastName", ": "+_scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.AlternateCustLastName", mashupOutput), null);
	   //OMNI-73999
            // OMNI-67004 - Active Shipment Label - START
            _scWidgetUtils.showWidget(this, "extn_ActiveShipment");
            _scWidgetUtils.showWidget(this, "extn_ActiveShipmentMessage");
            // OMNI-67004 - Active Shipment Label - END
        },
        showCancelledPopup: function(event, bEvent, ctrl, args) {
            //OMNI-67005- CancelledPopUp - START
            var bindings = null;
            bindings = {};
            var screenConstructorParams = null;
            screenConstructorParams = {};
            var popupParams = null;
            popupParams = {};
            popupParams["binding"] = bindings;
            popupParams["screenConstructorParams"] = screenConstructorParams;
            var dialogParams = null;
            dialogParams = {};
             /* OMNI-74370 Fix - Start*/
            dialogParams["closeCallBackHandler"] = "setFocusToScanLabel";
             /* OMNI-74370 Fix - End*/
            dialogParams["class"] = "popupTitleBorder fixedActionBarDialog";
            _iasUIUtils.openSimplePopup("extn.mobile.home.AuditStagedShipment.PopUp.CancelledPopUp.CancelledPopUp", "", this, popupParams, dialogParams);
            //OMNI-67005- CancelledPopUp - END
        },
         /* OMNI-74370 Fix - Start*/
         setFocusToScanLabel: function(args) {
            _scWidgetUtils.setFocusOnWidgetUsingUid(this, "extn_txt_LabelNo");
        },
         /* OMNI-74370 Fix - End*/
        handleCancelledShipmentLabel: function(args) {
            //OMNI-67006 - Cancelled Shipment Label - START
            var mashupOutput = args;
            this.showWidgets();
            _scWidgetUtils.setValue(this, "extn_lastScannedShipmentNo", ": "+_scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.ShipmentNo", mashupOutput), null);
            _scWidgetUtils.setValue(this, "extn_lastScannedOrderNo", ": "+_scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.OrderNo", mashupOutput), null);
            _scWidgetUtils.setValue(this, "extn_lastScannedElapsedTime", ": "+_scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ElapsedTime", mashupOutput) + " hrs.", null);
            _scWidgetUtils.setValue(this, "extn_lastScannedShipmentCount", _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ScannedShipmentsCount", mashupOutput), null);
            _scWidgetUtils.setValue(this, "extn_lastScannedCancelledShipmentCount", _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.CancelledShipmentsCount", mashupOutput), null);
            _scWidgetUtils.setValue(this, "extn_OrderDate", ": "+_scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.OrderDate", mashupOutput), null);
            _scWidgetUtils.setValue(this, "extn_PickDate", ": "+_scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.LastPickDate", mashupOutput), null);
            _scWidgetUtils.setValue(this, "extn_FirstName", ": "+_scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.CustomerFirstName", mashupOutput), null);
            _scWidgetUtils.setValue(this, "extn_LastName", ": "+_scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.CustomerLastName", mashupOutput), null);
            //OMNI-714779 - START
            _scWidgetUtils.setValue(this, "extn_AltPickup_FirstName", ": "+_scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.AlternateCustFirstName", mashupOutput), null);
			      _scWidgetUtils.setValue(this, "extn_AltPickup_LastName", ": "+_scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ACADScanBatchDetails.AlternateCustLastName", mashupOutput), null);
            //OMNI-71479 - END
	    //OMNI-73999 - START
            _scWidgetUtils.showWidget(this, "extn_OrderDate");
            _scWidgetUtils.showWidget(this, "extn_PickDate");
	    _scWidgetUtils.showWidget(this, "extn_OrderDate_Parent");
            _scWidgetUtils.showWidget(this, "extn_PickDate_Parent");
	    _scWidgetUtils.showWidget(this, "extn_CancelledShipment");
            _scWidgetUtils.showWidget(this, "extn_CancelledShipmentMessage");
	    //OMNI-73999 - END
            //OMNI-67006 - Cancelled Shipment Label - END
        },
        hideWidgets: function() {
            _scWidgetUtils.hideWidget(this, "extn_OrderDate_Parent");
            _scWidgetUtils.hideWidget(this, "extn_PickDate_Parent");
            _scWidgetUtils.hideWidget(this, "extn_FirstName_Parent");
            _scWidgetUtils.hideWidget(this, "extn_LastName_Parent");
            _scWidgetUtils.hideWidget(this, "extn_CancelledShipment");
            _scWidgetUtils.hideWidget(this, "extn_CancelledShipmentMessage");
            _scWidgetUtils.hideWidget(this, "extn_ActiveShipment");
            _scWidgetUtils.hideWidget(this, "extn_ActiveShipmentMessage");
            _scWidgetUtils.hideWidget(this, "extn_OrderDate");
            _scWidgetUtils.hideWidget(this, "extn_PickDate");
            _scWidgetUtils.hideWidget(this, "extn_FirstName");
            _scWidgetUtils.hideWidget(this, "extn_LastName");
            //OMNI-71479 - START
		        _scWidgetUtils.hideWidget(this, "extn_AltPickup_FirstName");
		        _scWidgetUtils.hideWidget(this, "extn_AltPickup_LastName");
			      _scWidgetUtils.hideWidget(this, "extn_AltPickup_FirstName_Parent");
			      _scWidgetUtils.hideWidget(this, "extn_AltPickup_LastName_Parent");
            //OMNI-71479 - END
        },
        showWidgets: function() {
            //_scWidgetUtils.showWidget(this, "extn_OrderDate_Parent");
            //_scWidgetUtils.showWidget(this, "extn_PickDate_Parent");
            _scWidgetUtils.showWidget(this, "extn_FirstName_Parent");
            _scWidgetUtils.showWidget(this, "extn_LastName_Parent");
           // _scWidgetUtils.showWidget(this, "extn_CancelledShipment");
           //_scWidgetUtils.showWidget(this, "extn_CancelledShipmentMessage");
           //_scWidgetUtils.showWidget(this, "extn_OrderDate");
           //_scWidgetUtils.showWidget(this, "extn_PickDate");
            _scWidgetUtils.showWidget(this, "extn_FirstName");
            _scWidgetUtils.showWidget(this, "extn_LastName");
            //OMNI-71479 - START
			      _scWidgetUtils.showWidget(this, "extn_AltPickup_FirstName");
			      _scWidgetUtils.showWidget(this, "extn_AltPickup_LastName");
			      _scWidgetUtils.showWidget(this, "extn_AltPickup_FirstName_Parent");
		 	      _scWidgetUtils.showWidget(this, "extn_AltPickup_LastName_Parent");
            //OMNI-71479 - END
        },
        finishBtnOnClick: function(event, bEvent, ctrl, args) {
            //_scScreenUtils.showConfirmMessageBox( this, "Do you want to finish the Scanning Stagged Items", "handleOkResponseFinal", null, null);
            //OMNI-69706 - Scan Summary PopUp - START
             /* OMNI-72706 Fix - START */
            _scWidgetUtils.setFocusOnWidgetUsingUid(this, "extn_txt_LabelNo");
            /* OMNI-72706 Fix - END */
            var popupParams = null;
            popupParams = {};
            var dialogParams = null;
            dialogParams = {};
             /* OMNI-74370 Fix - Start*/
             dialogParams["closeCallBackHandler"] = "setFocusToScanLabel";
               /* OMNI-74370 Fix - End*/
            _iasUIUtils.openSimplePopup("extn.mobile.home.AuditStagedShipment.PopUp.ScanSummaryPopUp.ScanSummaryPopUp", "", this, popupParams, dialogParams);
            //OMNI-69706 - Scan Summary PopUp - END
        },
        handleOkResponseFinal: function(
            res) {
            if (_scBaseUtils.equals(res, "Ok")) {
                _scWidgetUtils.closePopup(this, "CLOSE", false);
            }
        },
		setScreenDirty: function(screen, arg){
			if(_scBaseUtils.greaterThan(arg,0)){
				screen.scWidgetDirty = true;
			}else{
				screen.scWidgetDirty = false;
			}
		},
		isValidLabelNo: function(arg){
			var vLabelNo = _scModelUtils.getStringValueFromPath("ACADScanBatchDetails.LabelNo",arg);
			var regChars = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?A-Za-z\\w]+/;
			var vspace = ' ' ;
			var hasBlankSpace = vLabelNo.trim() === '';
			var vNumber = '[0-9]+';
			var hasSpecialCharacter = _scBaseUtils.contains(vLabelNo ,regChars );
			var hasSpace = _scBaseUtils.contains(vLabelNo ,vspace );
			var isNumber = _scBaseUtils.contains(vLabelNo ,vNumber );
			if( hasSpecialCharacter || hasSpace || hasBlankSpace ){ 
				return false;
			} else if(isNumber){
				return true;
			}
		 return false;
		},
    });
});
