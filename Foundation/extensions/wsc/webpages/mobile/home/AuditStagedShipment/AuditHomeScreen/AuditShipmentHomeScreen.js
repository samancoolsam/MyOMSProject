scDefine([
    "dojo/text!./templates/AuditShipmentHomeScreen.html",
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
    return _dojodeclare("extn.mobile.home.AuditStagedShipment.AuditHomeScreen.AuditShipmentHomeScreen", [_scScreen], {
        templateString: templateText,
        uId: "ScanShipmentStartScreen",
        packageName: "extn.mobile.home.AuditStagedShipment.AuditHomeScreen",
        className: "ScanShipmentStartScreen",
        title: "Title_ShipmentScan",
        screen_description: "Shipment Scan",

        namespaces: {



        },

        subscribers: {
            local: [
			{
                    eventId: 'afterScreenLoad',
                    sequence: '32',
                    handler: {
                        methodName: "extn_afterScreenLoad"
                    }
                },

		  {
                eventId: 'extn_btnStart_onClick',
                sequence: '32',
                handler: {
                    methodName: "scanOnStart"
                	}
		  },
    
            ]
        },

        setInitialized: function( event, bEvent, ctrl, args) {
            this.isScreeninitialized = true;
        },

        extn_afterScreenLoad: function(event, bEvent, ctrl, args) {
            _scWidgetUtils.setFocusOnWidgetUsingUid(this, "extn_txt_LabelNo");
		var clearSessionObject = _scBaseUtils.getNewModelInstance();
			var vloadedFromScreen = _iasContextUtils.getFromContext("LoadedFromShipmentScanScreen");
			if(!_scBaseUtils.isVoid(vloadedFromScreen) && _scBaseUtils.equals(vloadedFromScreen, 'AuditShipmentScanScreen')){
				var targetModel = _scBaseUtils.getTargetModel(this, "Start_Abort");
				var sAcadScanBatchHeaderKey = _iasContextUtils.getFromContext("AcadScanBatchHeaderKey");
				var currentNode = _iasContextUtils.getFromContext("CurrentStore");
				var Loginid = _iasContextUtils.getFromContext("Loginid");
				_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.AcadScanBatchHeaderKey", sAcadScanBatchHeaderKey ,targetModel);
				_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.BatchScanStatus", "ABORTED" ,targetModel);
				_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.Action", "ABORTED" ,targetModel);
				_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.StoreNo", currentNode  ,targetModel);
				_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.UserID", Loginid ,targetModel);
				_iasUIUtils.callApi(this, targetModel, "abortBatchScanProcess", null);
				_iasContextUtils.addToContext("AcadScanBatchHeaderKey", clearSessionObject);
				_iasContextUtils.addToContext("LoadedFromShipmentScanScreen", clearSessionObject);
			}
			/*OMNI-71659 Bugfix Changes - START*/
			this.clear_Session();
        },

		clear_Session : function(){
			var clearSessionObject = _scBaseUtils.getNewModelInstance();
			_iasContextUtils.addToContext("AcadScanBatchHeaderKey", clearSessionObject);
			_iasContextUtils.addToContext("LoadedFromShipmentScanScreen", clearSessionObject);
			_iasContextUtils.addToContext("BatchScanDuration", clearSessionObject);
			_iasContextUtils.addToContext("In_Progress_Session", clearSessionObject);	
			_iasContextUtils.addToContext("ShipmentsScanned", clearSessionObject);	
			_iasContextUtils.addToContext("ShipmentsCancelled", clearSessionObject);
			_iasContextUtils.addToContext("StagedShipmentsCount", clearSessionObject);
		},
		/*OMNI-71659 Bugfix Changes - END*/
		
        initializeScreen: function(event, bEvent, ctrl, args) {

        },
	scanOnStart: function(event, bEvent, ctrl, args){
		var clearSessionObject = _scBaseUtils.getNewModelInstance();
		_iasContextUtils.addToContext("SearchCriteria", clearSessionObject);
        _iasContextUtils.addToContext("AcadScanBatchHeaderKey ", clearSessionObject);
		 var targetModel = _scBaseUtils.getTargetModel(this, "Start_Output");
 		var currentNode = _iasContextUtils.getFromContext("CurrentStore");
		var Loginid = _iasContextUtils.getFromContext("Loginid");
		_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.Action", "INPROGRESS" ,targetModel);
		_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.StoreNo", currentNode  ,targetModel);
		_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.UserID", Loginid ,targetModel);
		_iasUIUtils.callApi(this, targetModel, "startBatchScanProcess", null);
	 },

	handleMashupOutput: function(mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
		if (_scBaseUtils.equals(mashupRefId, "startBatchScanProcess")) {
			var sAcadScanBatchHeaderKey = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.AcadScanBatchHeaderKey", modelOutput);
			var sStagedShipmentsCount = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.StagedShipmentsCount", modelOutput);
			_iasContextUtils.addToContext("StagedShipmentsCount", sStagedShipmentsCount );
			_iasContextUtils.addToContext("AcadScanBatchHeaderKey", sAcadScanBatchHeaderKey );
			var sHasActiveBatch = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.HasActiveBatch", modelOutput);
			var sUserID = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.UserID", modelOutput);
			var Loginid = _iasContextUtils.getFromContext("Loginid");
			if(!_scBaseUtils.isVoid(sHasActiveBatch) && _scBaseUtils.equals(sHasActiveBatch, 'Y') && !_scBaseUtils.isVoid(sUserID)){
				if(_scBaseUtils.equals(Loginid,sUserID)) {
					this.showBatchPopup(modelOutput);
				}
				else if (!_scBaseUtils.equals(Loginid,sUserID)){ //OMNI-68379- AbortPopUpPopUp Change
					this.showAbortPopup(this,this,this,modelOutput);
			}
		}
			else {
				_wscMobileHomeUtils.openScreen("extn.mobile.home.AuditStagedShipment.AuditScanScreen.ScanShipmentLabel", "extn.mobile.editors.ReceiveContainerEditor");
			}
		}
	 },

    handleMashupCompletion: function(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
         _iasBaseTemplateUtils.handleMashupCompletion(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
	 },
	
		showBatchPopup: function(args)
		{
			//OMNI-67009 - Batch PopUp  - START
			var sUserID = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.UserID", args);
			var sUserID="("+sUserID+")";
			var sLastScannedTime = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.LastScan", args);
			var tmp = dojo.date.stamp.fromISOString(sLastScannedTime,{selector: 'date'});
			sLastScannedTime = dojo.date.locale.format(tmp, {selector:'date',fullYear:'true'});	
			sLastScannedTime1= dojo.date.locale.format(tmp, {selector:'time', timePattern : 'hh:mm:ss'});
			sLastScannedTime= sLastScannedTime+" " + sLastScannedTime1;
			var BatchHeaderDataModel = [];
			BatchHeaderDataModel.push({
				'UserID' : sUserID,
				'LastScannedTime': sLastScannedTime
			});
			var bindings = null;
			bindings = {};
			bindings["BatchUserData"] = BatchHeaderDataModel;
			var screenConstructorParams = null;
			screenConstructorParams = {};
			var popupParams = null;
			popupParams = {};
			popupParams["binding"] = bindings;
			popupParams["screenConstructorParams"] = screenConstructorParams;
			var dialogParams = null;
			dialogParams = {};
			dialogParams["closeCallBackHandler"] = "BatchPopUp";
			dialogParams["class"] = "popupTitleBorder fixedActionBarDialog";
			_iasUIUtils.openSimplePopup("extn.mobile.home.AuditStagedShipment.PopUp.BatchPopUp.BatchPopUp", "", this, popupParams, dialogParams);
			//OMNI-67009 - Batch PopUp  - END
		},
		
		BatchPopUp: function(args){
			
		},
		//OMNI-68379- AbortPopUpPopUp - START
		showAbortPopup: function(event, bEvent, ctrl, args){
		 var sUserID = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.UserID", args);
		 var sUserName = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.UserName", args);
		 var sLastScannedTime = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.LastScan", args);
		var tmp = dojo.date.stamp.fromISOString(sLastScannedTime,{selector: 'date'});
		sLastScannedTime = dojo.date.locale.format(tmp, {selector:'date',fullYear:'true'});	
		sLastScannedTime1= dojo.date.locale.format(tmp, {selector:'time', timePattern : 'hh:mm:ss'});
		 sLastScannedTime=sLastScannedTime+" " + sLastScannedTime1;
		 
				var BatchHeaderDataModel = [];
			
				BatchHeaderDataModel.push({
                         'UserID' : sUserID,
						 'UserName' : sUserName,
						 'LastScannedTime': sLastScannedTime
                     });
		var bindings = null;
		bindings = {};
		bindings["BatchHeaderData"] = BatchHeaderDataModel;
		var screenConstructorParams = null;
		screenConstructorParams = {};
		var popupParams = null;
		popupParams = {};
		popupParams["binding"] = bindings;
		popupParams["screenConstructorParams"] = screenConstructorParams;
		var dialogParams = null;
		dialogParams = {};
		dialogParams["closeCallBackHandler"] = "AbortPopUp";
		dialogParams["class"] = "popupTitleBorder fixedActionBarDialog";
		_iasUIUtils.openSimplePopup("extn.mobile.home.AuditStagedShipment.PopUp.AbortPopUp.AbortPopUp", "", this, popupParams, dialogParams);
		//OMNI-68379- CancelledPopUp - END
	},
	AbortPopUp: function(args){
},
//OMNI-68379- AbortPopUpPopUp - END
		
	infoCallBack: function() {

	}

    });
});