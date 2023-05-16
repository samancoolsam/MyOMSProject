
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/container/pack/ContainerPackItemScanExtnUI","scbase/loader!ias/utils/PrintUtils", "scbase/loader!ias/utils/RepeatingScreenUtils", "scbase/loader!ias/utils/ContextUtils", "scbase/loader!ias/utils/ScreenUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/RepeatingPanelUtils", "scbase/loader!sc/plat/dojo/utils/ResourcePermissionUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!wsc/components/common/utils/CommonUtils", "scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils", "scbase/loader!wsc/components/shipment/customerpickup/SummaryUI", "scbase/loader!sc/plat/dojo/Userprefs","scbase/loader!ias/utils/EventUtils","scbase/loader!ias/utils/BaseTemplateUtils","scbase/loader!wsc/components/shipment/container/pack/ContainerPackUtils","scbase/loader!dojo/dom-attr"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnContainerPackItemScanExtnUI
			    ,
			   _iasPrintUtils, _iasRepeatingScreenUtils, _iasContextUtils, _iasScreenUtils, _iasUIUtils, _scBaseUtils, _scEventUtils, _scModelUtils, _scRepeatingPanelUtils, _scResourcePermissionUtils, _scScreenUtils, _scWidgetUtils, _wscCommonUtils, _wscShipmentUtils, _wscSummaryUI, _scUserprefs, _iasEventUtils, _iasBaseTemplateUtils, _wscContainerPackUtils, dDomAttr
){ 
	return _dojodeclare("extn.components.shipment.container.pack.ContainerPackItemScanExtn", [_extnContainerPackItemScanExtnUI],{
	// custom code here
	
		extnFocusOnTextBox : function(
        event, bEvent, ctrl, args) {
			_scWidgetUtils.setFocusOnWidgetUsingUid(
                this, "txtScanField");
                
       		//OMNI-69797 START
        	var getCommonCodeInput = {};
			getCommonCodeInput = _scModelUtils.createModelObjectFromKey("CommonCode", getCommonCodeInput);
			_scModelUtils.setStringValueAtModelPath("CommonCode.CodeType", "TGL_RCP_WEB_SOM_UI" , getCommonCodeInput);
			_scModelUtils.setStringValueAtModelPath("CommonCode.CodeValue", "NON_EDITABLE_ADDQTY_SCANNEDQTY" , getCommonCodeInput);
			_iasUIUtils.callApi(this, getCommonCodeInput, "extn_getFlagToDisableAddQtyAndReadOnlyScannedQty", null);  
			//OMNI-69797 END
            
		},
		
		afterScreenLoad: function(
        event, bEvent, ctrl, args) {
            _scWidgetUtils.setFocusOnWidgetUsingUid(
                this, "txtScanField");
        },
		
        refreshItemScanScreenData: function(
        event, bEvent, ctrl, args) {
            this.clearItemFilter();
            var containerList = null;
            var activeContainerModel = null;
            _wscContainerPackUtils.refreshCommonData(
            this, args);
            _scWidgetUtils.hideWidget(
            this, "tpLastProductScan", false);
            if (
            _scWidgetUtils.isWidgetVisible(
            this, "errorMsgPnl")) {
                _scWidgetUtils.hideWidget(
                this, "errorMsgPnl", false);
            }
            _scWidgetUtils.showWidget(
            this, "noProductScanPnl", false, "");
            activeContainerModel = _scBaseUtils.getValueFromPath("activeContainerInfo", args);
            var newPackage = _scModelUtils.getStringValueFromPath("Container.ContainerNo", activeContainerModel);

            if(_scBaseUtils.equals(newPackage, "New package")) {
            	_scModelUtils.setStringValueAtModelPath("Container.ContainerNo", "New Container" ,activeContainerModel);
            }
            _scWidgetUtils.setFocusOnWidgetUsingUid(
                this, "txtScanField");
            this.addOrRemoveCssToLPpanel();
            if (
            _scBaseUtils.equals(
            this.isProductsPainted, "Y")) {
                this.showProducts();
            }
        },

        handleMashupOutput: function(
        mashupRefId, modelOutput, modelInput, mashupContext) {
        //OMNI-69797 START
         if(_scBaseUtils.equals(mashupRefId, "extn_getFlagToDisableAddQtyAndReadOnlyScannedQty")){
					 var isManualEntry = modelOutput.CommonCodeList.CommonCode[0].CodeShortDescription;
					  if(!(_scBaseUtils.isVoid(isManualEntry)) && (_scBaseUtils.equals(isManualEntry,"Y"))){
							 _scWidgetUtils.disableWidget(this, "packQtyLink",true);
							var fs = this.getWidgetByUId("lpstxtQty");
							dDomAttr.set(fs.textbox, "readonly", true);			
					 }						
			} 
			//OMNI-69797 END
			else if (
            _scBaseUtils.equals(
            mashupRefId, "containerPack_generateSCM")) {
                _wscContainerPackUtils.handleGenerateScmCall(
                this, mashupRefId, modelOutput, modelInput, mashupContext);
            } else if (
            _scBaseUtils.equals(
            mashupRefId, "containerPack_packAll")) {
                _scScreenUtils.clearScreen(
                this, null);
                if (!(
                _scBaseUtils.isVoid(
                _scModelUtils.getStringValueFromPath("Shipment.AlreadyPacked", modelOutput)))) {
                    _iasBaseTemplateUtils.showMessage(
                    this, "MessageAllLinesPacked", "Error", null);
                } else {
                    var shipmentContainerKey = null;
                    shipmentContainerKey = _scModelUtils.getStringValueFromPath("Shipment.Containers.Container.ShipmentContainerKey", modelInput);
                    if (
                    _scBaseUtils.isVoid(
                    shipmentContainerKey)) {
                        _wscContainerPackUtils.setDraftContainerFlag(
                        this, "N");
                        _wscContainerPackUtils.updateDraftContainerInfo(
                        this, mashupRefId, modelOutput, modelInput, mashupContext);
                    }
                    if (
                    _scBaseUtils.equals(
                    _scModelUtils.getNumberValueFromPath("Shipment.ShipmentContainerizedFlag", modelOutput), 3)) {
                        var argsBean = null;
                        argsBean = {};
                        _scBaseUtils.setAttributeValue("modelOutput", modelOutput, argsBean);
                        var textObj = null;
                        textObj = {};
                        textObj["OK"] = _scScreenUtils.getString(
                        this, "Ok");
						this.handlePackCompletion();
                       /* _scScreenUtils.showSuccessMessageBox(
                        this, _scScreenUtils.getString(
                        this, "Message_PackCompleted"), "handlePackCompletion", textObj); */
                    }
                }
            } else if (
            _scBaseUtils.equals(
            mashupRefId, "containerPack_registerBarCodeForPacking")) {
                var shipmentContainerKey = null;
                shipmentContainerKey = _scModelUtils.getStringValueFromPath("BarCode.ShipmentContextualInfo.ShipmentContainerKey", modelInput);
                if (
                _scBaseUtils.isVoid(
                shipmentContainerKey)) {
                    _wscContainerPackUtils.setDraftContainerFlag(
                    this, "N");
                    _wscContainerPackUtils.updateDraftContainerInfo(
                    this, mashupRefId, modelOutput, modelInput, mashupContext);
                }
                var tempModel = null;
                var shipmentLineModel = null;
                tempModel = _scModelUtils.getModelObjectFromPath("BarCode.Shipment.ShipmentLine", modelOutput);
                shipmentLineModel = {};
                _scModelUtils.addModelToModelPath("ShipmentLine", tempModel, shipmentLineModel);
                _scScreenUtils.setModel(
                this, "lastProductScanned_output", shipmentLineModel, null);
                _scScreenUtils.clearScreen(
                this, null);
                tmpModel = _scBaseUtils.cloneModel(
                shipmentLineModel);
                _scScreenUtils.setModel(
                this, "lastProdScannedQuantity_model", tmpModel, null);
                this.hideOrShowPackCompletionIcon(
                tmpModel);
                var packInstructionModel = null;
                packInstructionModel = _scModelUtils.getModelObjectFromPath("ShipmentLine.Instructions.Instruction", tmpModel);
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
                }
                _scWidgetUtils.hideWidget(
                this, "noProductScanPnl", false);
                _scWidgetUtils.hideWidget(
                this, "errorMsgPnl", false);
                if (!(
                _scWidgetUtils.isWidgetVisible(
                this, "tpLastProductScan"))) {
                    var inputArray = null;
                    var packageId = null;
                    var activePackageInfo = null;
                    activePackageInfo = _scScreenUtils.getModel(
                    this, "activeContainerModel");
                    packageId = _scModelUtils.getStringValueFromPath("Container.ContainerNo", activePackageInfo);
                    inputArray = [];
                    inputArray.push(
                    packageId);
                    _scWidgetUtils.setTitle(
                    this, "tpLastProductScan", _scScreenUtils.getFormattedString(
                    this, "extn_lastScannedProductTitle", inputArray), false);
                    _scWidgetUtils.showWidget(
                    this, "tpLastProductScan", false, "");
                    if (!(
                    _scResourcePermissionUtils.hasPermission("WSC000024"))) {
                        _scWidgetUtils.hideWidget(
                        this, "quantityPanel", false);
                        _scWidgetUtils.hideWidget(
                        this, "lblQtyInContainer", false);
                        _scWidgetUtils.showWidget(
                        this, "QtyInPackage", false, "");
                    }
                }
                this.showItemVariationCP();
                this.addOrRemoveCssToLPpanel("add");
                _scWidgetUtils.showWidget(
                this, "lastProductScannedQtyCP", false, "");
                if (
                _scBaseUtils.equals(
                _scModelUtils.getNumberValueFromPath("BarCode.Shipment.ShipmentContainerizedFlag", modelOutput), 3)) {
                    var argsBean = null;
                    argsBean = {};
                    _scBaseUtils.setAttributeValue("modelOutput", modelOutput, argsBean);
                    var textObj = null;
                    textObj = {};
                    textObj["OK"] = _scScreenUtils.getString(
                    this, "Ok");
					this.handlePackCompletion();
                    /*_scScreenUtils.showSuccessMessageBox(
                    this, _scScreenUtils.getString(
                    this, "Message_PackCompleted"), "handlePackCompletion", textObj); */
                }
            } else if (
            _scBaseUtils.equals(
            mashupRefId, "containerPack_changeShipmentCall")) {
                if (
                _scBaseUtils.equals(
                _scModelUtils.getNumberValueFromPath("Shipment.ShipmentContainerizedFlag", modelOutput), 3)) {
                    var argsBean = null;
                    argsBean = {};
                    _scBaseUtils.setAttributeValue("modelOutput", modelOutput, argsBean);
                    var textObj = null;
                    textObj = {};
                    textObj["OK"] = _scScreenUtils.getString(
                    this, "Ok");
					this.handlePackCompletion();
                    /*_scScreenUtils.showSuccessMessageBox(
                    this, _scScreenUtils.getString(
                    this, "Message_PackCompleted"), "handlePackCompletion", textObj); */
                } else {
                    _iasBaseTemplateUtils.displaySingleMessage(
                    this, "extn_Message_unpackUpdateSuccess", "success", null, "errorMsgPnl");
                    if (
                    _scWidgetUtils.isWidgetVisible(
                    this, "updateButton")) {
                        _scWidgetUtils.hideWidget(
                        this, "updateButton", false);
                    }
                    var tempModel = null;
                    var shipmentLineModel = null;
                    tempModel = _scModelUtils.getModelObjectFromPath("Shipment.ShipmentLine", modelOutput);
                    shipmentLineModel = {};
                    _scModelUtils.addModelToModelPath("ShipmentLine", tempModel, shipmentLineModel);
                    _scScreenUtils.setModel(
                    this, "lastProdScannedQuantity_model", shipmentLineModel, null);
                    this.hideOrShowPackCompletionIcon(
                    shipmentLineModel);
                }
                _scScreenUtils.clearScreen(
                this, null);
            }
        },
		
		showOrHideScanPanel: function(
        res, argsBean) {
            if (
            _scBaseUtils.equals(
            res, "Ok")) {
                if (
                _scBaseUtils.equals(
                this.isScanPanelPainted, "Y")) {
                    _scWidgetUtils.hideWidget(
                    this, "scanDataForm", false);
                    _scWidgetUtils.setLinkImageSrc(
                    this, "expandOrCollapseLnk", "wsc/resources/css/icons/images/arrowDown.png");
                    this.isScanPanelPainted = "N";
                } else {
                    if (
                    _scBaseUtils.equals(
                    this.isProductsPainted, "Y")) {
                        this.hideProducts();
                    }
                    _scWidgetUtils.showWidget(
                    this, "scanDataForm", false, "");
                    _scWidgetUtils.setLinkImageSrc(
                    this, "expandOrCollapseLnk", "wsc/resources/css/icons/images/arrowUp.png");
                    this.isScanPanelPainted = "Y";
                    _scWidgetUtils.setFocusOnWidgetUsingUid(
						this, "txtScanField");
                }
            }
        }
});
});

