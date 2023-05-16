
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/container/pack/ContainerPackExtnUI","scbase/loader!ias/utils/BaseTemplateUtils", "scbase/loader!ias/utils/ContextUtils", "scbase/loader!ias/utils/ScreenUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!ias/utils/WizardUtils", "scbase/loader!sc/plat/dojo/utils/EditorUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!wsc/components/common/utils/CommonUtils", "scbase/loader!wsc/components/shipment/container/pack/ContainerPackUI", "scbase/loader!wsc/components/shipment/container/pack/ContainerPackUtils","scbase/loader!wsc/mobile/home/utils/MobileHomeUtils","scbase/loader!sc/plat/dojo/utils/RepeatingPanelUtils","scbase/loader!ias/utils/BaseTemplateUtils","scbase/loader!sc/plat/dojo/utils/WizardUtils"]
,
function(            
                _dojodeclare
             ,
                _extnContainerPackExtnUI
            ,
                _iasBaseTemplateUtils, _iasContextUtils, _iasScreenUtils, _iasUIUtils, _iasWizardUtils, _scEditorUtils, _scBaseUtils, _scEventUtils, _scModelUtils, _scScreenUtils, _scWidgetUtils, _wscCommonUtils, _wscContainerPackUI, _wscContainerPackUtils,_wscMobileHomeUtils, _scRepeatingPanelUtils, _iasBaseTemplateUtils,_scWizardUtils
){ 
    return _dojodeclare("extn.components.shipment.container.pack.ContainerPackExtn", [_extnContainerPackExtnUI],{
    // custom code here

    //BOPIS-1576: Remove manual "Enter" hit after container weight input - begin
    //ToInvokeFinishPack:true,
    isInvalidWeightGlobal:false,
    //BOPIS-1576: Remove manual "Enter" hit after container weight input - end

    afterScreenLoad: function(
    event, bEvent, ctrl, args) {
        
            var currentScreen = null;
            currentScreen = _scScreenUtils.getChildScreen(
            this, this.currentView);
            _scWidgetUtils.setFocusOnWidgetUsingUid(
            currentScreen, "txtScanField");
//auto focus for the first time -BEGIN
            if(_scBaseUtils.isVoid(currentScreen)) {
                var that = this;
                setTimeout(function() {
                    currentScreen = _scScreenUtils.getChildScreen(
                    that, that.currentView);
                    _scWidgetUtils.setFocusOnWidgetUsingUid(
                    currentScreen, "txtScanField");
                },500);
            }
//auto focus for the first time -END
        
        var wizardInstance = false;
        wizardInstance = _iasUIUtils.getParentScreen(
        this, true);
        _iasWizardUtils.hideNavigationalWidget(
        wizardInstance, "prevBttn", false);
		//Hiding finish pack button from wizard : new finish pack changes: Begin
        _iasWizardUtils.hideNavigationalWidget(wizardInstance, "confirmBttn", false);
        if (_iasContextUtils.isMobileContainer()) {
            _scWidgetUtils.hideWidget(this, "extn_customFinishPack2", false);
            _scWidgetUtils.addClass(this, "extn_customFinishPack", "extn_FinishPackMobile");
        }
		 //Hiding finish pack button from wizard : new finish pack changes: Begin
        
    },

    extn_afterInitializeScreen: function() {
        _scWidgetUtils.hideWidget(this, "img_TimeRmnClock", false);
        var parentScreen = _iasUIUtils.getParentScreen(this);
        _scWidgetUtils.hideWidget(parentScreen, "img_TimeRmnClock", false);
        var getShipmentContainerList_output = _scScreenUtils.getModel(
            this, "getShipmentContainerList_output");
            var containerList = _scModelUtils.getModelListFromPath("Containers.Container", getShipmentContainerList_output);
            var containerCount = _scBaseUtils.getAttributeCount(
            containerList);
            if (
            _scBaseUtils.equals(
            containerCount, 0)) {
                _scModelUtils.setStringValueAtModelPath("Container.ContainerNo", "New Container", activeContainerInfo);
            }
    },

    getContainerCount: function(
        dataValue, screen, widget, nameSpace, model) {
        var dataValue = dataValue;
        var packageCount = 0;
        if ( _scBaseUtils.isVoid( dataValue)) {
            dataValue = "0";
        }
        packageCount = _wscContainerPackUtils.parseInteger( dataValue);
        var inputArray = [];
        inputArray.push(dataValue);
        var msgKey = "";
        if ( _scBaseUtils.equals( packageCount, 1)) {
            /*_scWidgetUtils.setLabel(
            this, "lblpackages", _scScreenUtils.getString(
            this, "Label_Package"));*/
            msgKey="ContainerCountSingle";
        } else {
            /*_scWidgetUtils.setLabel(
            this, "lblpackages", _scScreenUtils.getString(
            this, "Label_Packages"));*/
            msgKey="ContainerCount";
        }
        
        var packageMsg = _scScreenUtils.getFormattedString(this, msgKey, inputArray);

        return  packageMsg.replace("Package", "Container");
    },

    handleMashupOutput: function(
        mashupRefId, modelOutput, modelInput, mashupContext) {
            if (
            _scBaseUtils.equals(
            mashupRefId, "containerPack_getShipmentContainerList_NoScac") || _scBaseUtils.equals(
            mashupRefId, "containerPack_getShipmentContainerList_Scac")) {
                var shipmentContainerizedFlag = 0;
                shipmentContainerizedFlag = _scModelUtils.getNumberValueFromPath("Containers.ShipmentContainerizedFlag", modelOutput);
                if (
                _scBaseUtils.equals(
                shipmentContainerizedFlag, 3)) {
                    var totalNumberOfRecords = 0;
                    totalNumberOfRecords = _scModelUtils.getNumberValueFromPath("Containers.TotalNumberOfRecords", modelOutput);
                    if (
                    totalNumberOfRecords > 0) {
                        if (
                        _scBaseUtils.equals(
                        this.scacIntegrationReqd, "N")) {
                            var argsBean = null;
                            argsBean = {};
                            var goToPackagesView = true;
                            _scBaseUtils.setAttributeValue("goToPackagesView", goToPackagesView, argsBean);
                            _scScreenUtils.showConfirmMessageBox(
                            this, _scScreenUtils.getString(
                            this, "Message_NotAllContainersWeighed"), "openShipmentSummaryOnConfirm", null, argsBean);
                        } else if (
                        _scBaseUtils.equals(
                        this.scacIntegrationReqd, "Y")) {
                            //var argsBean = null;
                            //argsBean = {};
                            //var goToPackagesView = true;
                            //_scBaseUtils.setAttributeValue("goToPackagesView", goToPackagesView, argsBean);
                            //_scScreenUtils.showConfirmMessageBox(
                            //this, _scScreenUtils.getString(
                            //this, "Message_NotAllContainersTracked"), "openShipmentSummaryOnConfirm", null, argsBean);
				
                            //BOPIS-1576: Remove manual "Enter" hit after container weight input - begin
            				//if(this.ToInvokeFinishPack){
            				//	this.extn_callCustomFinishPackService();
            				//}	
					this.extn_containerWeightSaveBeforeFinishPack();		
                            //BOPIS-1576: Remove manual "Enter" hit after container weight input - end
                        }
                    } else {
                        // var targetModel = null;
                        //targetModel = _scBaseUtils.getTargetModel(
                        //this, "changeShipmentStatus_input", null);
                        //var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", targetModel);
                        //var inputToMashup = _scModelUtils.createNewModelObjectWithRootKey("Container");
                        //_scModelUtils.setStringValueAtModelPath("Container.ShipmentKey", shipmentKey ,inputToMashup);
                        //_iasUIUtils.callApi(
                        //this, inputToMashup, "extn_BeforeChangeShipmentOnFinishPack_ref", null);

                        //BOPIS-1576: Remove manual "Enter" hit after container weight input - begin
            			//if(this.ToInvokeFinishPack){
                                //    	this.extn_callCustomFinishPackService();
            			//}
				this.extn_containerWeightSaveBeforeFinishPack();
                        //BOPIS-1576: Remove manual "Enter" hit after container weight input - end
                    }
                } else {
                    _scScreenUtils.showConfirmMessageBox(
                    this, _scScreenUtils.getString(
                    this, "Message_NotAllLinesPacked"), "openShipmentSummaryOnConfirm", null);
                }
            }
			else if(_scBaseUtils.equals(
            mashupRefId, "extn_BeforeChangeShipmentOnFinishPack_ref")) {
                var targetModel = null;
                targetModel = _scBaseUtils.getTargetModel(
                this, "changeShipmentStatus_input", null);
                _iasUIUtils.callApi(
                this, targetModel, "containerPacking_changeShipment", null);
            }
            else if (
            _scBaseUtils.equals(
            mashupRefId, "containerPacking_changeShipment")) {  
                // this.openShipmentSummary("Yes");
            //call extn_stampInvoiceNoOnBOPISOrders_ref
            var inputToMashup = _scModelUtils.createNewModelObjectWithRootKey("Shipment");

            var targetModel = _scBaseUtils.getTargetModel(
                        this, "changeShipmentStatus_input", null);
            var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", targetModel);

            _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey ,inputToMashup);
            _scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod", "SHP" ,inputToMashup);


            _iasUIUtils.callApi(
                this, targetModel, "extn_stampInvoiceNoOnBOPISOrders_ref", null);


            } else if(_scBaseUtils.equals(
            mashupRefId, "extn_stampInvoiceNoOnBOPISOrders_ref")) {
                this.openShipmentSummary("Yes");
            }
            //BOPIS-1576: Remove manual "Enter" hit after container weight input - begin
			else if(_scBaseUtils.equals(mashupRefId, "extn_AcademyUpdateWeightAndFinishPack_Ref")) {
                this.openShipmentSummary("Yes");
            }
	        /*else if(_scBaseUtils.equals(
            mashupRefId, "extn_FinishPackCustomized")) {
                this.openShipmentSummary("Yes");
            }
            else if(_scBaseUtils.equals(
            mashupRefId, "extn_containerPack_changeShipmentForWeight_Ref")) {
				if(this.ToInvokeFinishPack){
                    this.extn_callCustomFinishPackService();
            	}

                var eventDefn = null;
                var blankModel = null;
                eventDefn = {};
                blankModel = {};
                _scBaseUtils.setAttributeValue("argumentList", blankModel, eventDefn);
                //_scEventUtils.fireEventToParent(this, "saveCurrentPage", eventDefn);
                _scEventUtils.fireEventInsideScreen(this, "saveCurrentPage", null, eventDefn);
            }*/
            //BOPIS-1576: Remove manual "Enter" hit after container weight input - end
        },
        // overring OOTB method to change the 'TotalNumberOfRecords' mapping
        getShipmentLineCount: function(
        dataValue, screen, widget, nameSpace, model) {
            var shipmentLines = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines", model);
//          dataValue = dataValue;
            var dataValue = _scModelUtils.getStringValueFromPath("TotalNumberOfRecords", shipmentLines[1]);
            var productCount = 0;
            if (
            _scBaseUtils.isVoid(
            dataValue)) {
                dataValue = "0";
            }
            productCount = _wscContainerPackUtils.parseInteger(
            dataValue);
            var inputArray = [];
            inputArray.push(dataValue);
            var msgKey = "";
            if (
            _scBaseUtils.equals(
            productCount, 1)) {
                /*  _scWidgetUtils.setLabel(
                this, "lblProducts", _scScreenUtils.getString(
                this, "lblProduct")); */
                msgKey = "Label_ProductInPackage";
            } else {
                /*  _scWidgetUtils.setLabel(
                this, "lblpackages", _scScreenUtils.getString(
                this, "Label_Products")); */
                msgKey = "Label_ProductCount";
            }
            return _scScreenUtils.getFormattedString(this, msgKey, inputArray);
        },
        
        /*save: function(event, bEvent, ctrl, args) {
            var getShipmentDetailsModel = _scScreenUtils.getModel(
            this, "getShipmentDetails_output");
            var orderNo = getShipmentDetailsModel.Shipment.DisplayOrderNo;
            var childScreen = _scScreenUtils.getChildScreen(
            this, "ContainerPackContainerView");
            if(_scBaseUtils.isVoid(childScreen)) {
                var targetModel = null;
                targetModel = _scBaseUtils.getTargetModel(
                this, "getShipmentContainerList_input", null);
                if (
                _scBaseUtils.equals(
                this.scacIntegrationReqd, "N")) {
                    _iasUIUtils.callApi(
                    this, targetModel, "containerPack_getShipmentContainerList_NoScac", null);
                } else if (
                _scBaseUtils.equals(
                this.scacIntegrationReqd, "Y")) {
                    _iasUIUtils.callApi(
                    this, targetModel, "containerPack_getShipmentContainerList_Scac", null);
                }
            } else {
                var containerModel = childScreen.ContainerItemDetails;
                var totalContainerModel = _scScreenUtils.getModel(
                this, "getShipmentContainerList_output");
                var length = containerModel.length;
                var totalContainerModelLength = totalContainerModel.Containers.Container.length;
                var dummyVariable = 0;
                for(var j=0;j<totalContainerModelLength;j++)
                {
                    var newContainerNo = totalContainerModel.Containers.Container[j].ContainerNo;
                    for(var k=0;k<length;k++)
                    {
                        var newContainerNo2 =  containerModel[k].ContainerNo;
                        if(_scBaseUtils.equals(newContainerNo, newContainerNo2))
                        {
                            dummyVariable++;
                            break;
                        }
                    }
                }

                if(_scBaseUtils.equals(
                    dummyVariable, 0))
                {
                    var message = "Please select the Container ID and Weight for all the Containers";
                    _iasBaseTemplateUtils.showMessage(this, message, "error", null);
                }
                else if(!_scBaseUtils.equals(
                    dummyVariable, totalContainerModelLength))
                {
                    var message = "Please select the Container ID and Weight for remaining Containers";
                    _iasBaseTemplateUtils.showMessage(this, message, "error", null);
                }
                else if(_scBaseUtils.equals(
                    dummyVariable, totalContainerModelLength))
                {
                    var shipments = _scModelUtils.createNewModelObjectWithRootKey("Shipments");
                    var shipment = [];
                    for(var j=0;j<totalContainerModelLength;j++)
                    {
                        var newContainerNo = totalContainerModel.Containers.Container[j].ContainerNo;
                        for(var k=0;k<length;k++)
                        {
                            var newContainerNo2 =  containerModel[k].ContainerNo;
                            if(_scBaseUtils.equals(newContainerNo, newContainerNo2))
                            {
                                var actualWeight = containerModel[k].ActualWeight;
                                var containerType = containerModel[k].ContainerType;
                                var containerID = containerModel[k].ContainerNo;
                                var containerScm = containerModel[k].ContainerScm;
                                var containerTypeKey = containerModel[k].ContainerTypeKey;
                                var shipmentContainerKey = containerModel[k].ShipmentContainerKey;
                                var shipmentKey = containerModel[k].ShipmentKey;
                                shipment.push(
                                    {
                                            ContainerGrossWeight : actualWeight,
                                            ContainerScm : containerScm,
                                            ContainerType : containerType,
                                            ContainerTypeKey : containerTypeKey,
                                            ShipmentContainerKey : shipmentContainerKey,
                                            ShipmentKey : shipmentKey,
                                            OrderNo : orderNo
                                    }
                                );
                            }
                        }
                    }
                    //_scModelUtils.addModelObjectAsChildToModelObject("Shipments", shipment, shipments);
                    _scModelUtils.addModelToModelPath("Shipments.Shipment", shipment, shipments);
                    _iasUIUtils.callApi(
                        this, shipments, "extn_FinishPackCustomized", null);
                    
                }
            }
        }*/

        /*extn_callCustomFinishPackService: function() {

            var getShipmentDetailsModel = _scScreenUtils.getModel(this, "getShipmentDetails_output");
            var orderNo = getShipmentDetailsModel.Shipment.DisplayOrderNo;
            var childScreen = _scScreenUtils.getChildScreen(this, "ContainerPackContainerView");

            var containerModel = childScreen.ContainerItemDetails;
            var totalContainerModel = _scScreenUtils.getModel(this, "getShipmentContainerList_output");
        
            var totalContainerModel1 = _scModelUtils.getStringValueFromPath("Containers.Container", totalContainerModel);

            var totalCMlength = totalContainerModel1.length;

            for(var k = 0; k < totalCMlength; k ++) {
                var cntr = totalContainerModel.Containers.Container[k];

                var key = _scModelUtils.getStringValueFromPath("ShipmentContainerKey", cntr);
                if(_scBaseUtils.isVoid(key)) {
                   totalContainerModel.Containers.Container.splice(k,1); 
                }
            }

            var length = containerModel.length;
            var totalContainerModelLength = totalContainerModel.Containers.Container.length;
            var dummyVariable = 0;
            for(var j=0;j<totalContainerModelLength;j++)
            {
                var newContainerNo = totalContainerModel.Containers.Container[j].ContainerNo;
                for(var k=0;k<length;k++)
                {
                    var newContainerNo2 =  containerModel[k].ContainerNo;
                    if(_scBaseUtils.equals(newContainerNo, newContainerNo2))
                    {
                        dummyVariable++;
                        break;
                    }
                }
            }

            if(_scBaseUtils.equals(
                dummyVariable, 0)){
                var message = "Please select the Container ID and Weight for all the Containers";
                _iasBaseTemplateUtils.showMessage(this, message, "error", null);
            }
            else if(!_scBaseUtils.equals(
                dummyVariable, totalContainerModelLength))
            {
                var message = "Please select the Container ID and Weight for remaining Containers";
                _iasBaseTemplateUtils.showMessage(this, message, "error", null);
            }
            else if(_scBaseUtils.equals(
                dummyVariable, totalContainerModelLength)){
                //var bol = false;
                var shipments = _scModelUtils.createNewModelObjectWithRootKey("Shipments");
                var shipment = [];
                for(var j=0;j<totalContainerModelLength;j++)
                {
                    var newContainerNo = totalContainerModel.Containers.Container[j].ContainerNo;
                    for(var k=0;k<length;k++)
                    {
                        var newContainerNo2 =  containerModel[k].ContainerNo;
                        if(_scBaseUtils.equals(newContainerNo, newContainerNo2))
                        {
                            var actualWeight = containerModel[k].ActualWeight;
                            var containerType = containerModel[k].ContainerType;
                            //BOPIS-1576: Remove manual "Enter" hit after container weight input - begin
                            //if(_scBaseUtils.equals(actualWeight, "0.00") && !_scBaseUtils.equals(containerType,"VendorPackage")) {
                            //    bol = true;
                            //    break;
                            //}
                            //BOPIS-1576: Remove manual "Enter" hit after container weight input - end
                            var containerID = containerModel[k].ContainerNo;
                            var containerScm = containerModel[k].ContainerScm;
                            var containerTypeKey = containerModel[k].ContainerTypeKey;
                            var shipmentContainerKey = containerModel[k].ShipmentContainerKey;
                            var shipmentKey = containerModel[k].ShipmentKey;
                            shipment.push(
                                {
                                        ContainerGrossWeight : actualWeight,
                                        ContainerScm : containerScm,
                                        ContainerType : containerType,
                                        ContainerTypeKey : containerTypeKey,
                                        ShipmentContainerKey : shipmentContainerKey,
                                        ShipmentKey : shipmentKey,
                                        OrderNo : orderNo
                                }
                            );
                        }
                    }
                }
                //_scModelUtils.addModelObjectAsChildToModelObject("Shipments", shipment, shipments);

                //BOPIS-1576: Remove manual "Enter" hit after container weight input - begin
                //if(bol) {
                //    var message = "Please hit 'Enter' after inputting the container weight.";
                //   _iasBaseTemplateUtils.showMessage(this, message, "error", null);
                //} else {
                //BOPIS-1576: Remove manual "Enter" hit after container weight input - end
                    _scModelUtils.addModelToModelPath("Shipments.Shipment", shipment, shipments);
                    _iasUIUtils.callApi(
                    this, shipments, "extn_FinishPackCustomized", null);
                //}
            }


        },*/

        //BOPIS-1576: Remove manual "Enter" hit after container weight input - begin
	    extn_containerWeightSaveBeforeFinishPack: function(event, bEvent, ctrl, args) {

            //Validate container weight - begin

            var containerPackContainerViewScreen = _scScreenUtils.getChildScreen(this, "ContainerPackContainerView");
            var shpContListOutputModel = _scScreenUtils.getModel(this, "getShipmentContainerList_output", null);

            var isInvalidWeightLocal=false;

            var containerWeightInputModel={};
             containerWeightInputModel.Shipment={};
			containerWeightInputModel.Shipment.Containers={};
			containerWeightInputModel.Shipment.Containers.Container=[];
           
            var getShipmentDetailsModel = _scScreenUtils.getModel(this, "getShipmentDetails_output");
            var orderNo = getShipmentDetailsModel.Shipment.DisplayOrderNo;
            var childScreen = _scScreenUtils.getChildScreen(this, "ContainerPackContainerView");
            var containerModel = childScreen.ContainerItemDetails;

			
			//if(!(_scBaseUtils.isVoid(containerModel))){
				//If block to check if no container type & weight selected - begin


						
					//if(!(_scBaseUtils.isVoid(shpContListOutputModel.Containers)) && !(_scBaseUtils.isVoid(shpContListOutputModel.Containers.Container)) ){

					//if(_scBaseUtils.equals(shpContListOutputModel.Containers.Container.length, containerModel.length)){
						//if block to check remaining containers weighed - begin



						for(var i=0; i<shpContListOutputModel.Containers.Container.length; i++){

						var SCK = shpContListOutputModel.Containers.Container[i].ShipmentContainerKey;
						var repPanelUId = _scRepeatingPanelUtils.returnUIdOfIndividualRepeatingPanel(containerPackContainerViewScreen, "containerListRepPanel",SCK);
						var repPanelScreen = _iasScreenUtils.getRepeatingPanelScreenWidget(containerPackContainerViewScreen, repPanelUId);
						var isValid = _scScreenUtils.validate(repPanelScreen);

						if (_scBaseUtils.equals(isValid, true)) {
							var container_Src = null;
							var numOfShipmentLines = 0;
							container_Src = _scScreenUtils.getModel(
							repPanelScreen, "container_Src");
							numOfShipmentLines = _scModelUtils.getNumberValueFromPath("Container.ContainerDetails.TotalNumberOfRecords",container_Src);
							if (_scBaseUtils.equals(numOfShipmentLines, 0)) {
								_wscContainerPackUtils.highlightWeightError(repPanelScreen, "Message_EmptyContainer", "errorMsgPnl", "activeRepeatingPanelUId");
								isInvalidWeightLocal=true;
								break;
							} 

							var currentContainerKey=container_Src.Container.ShipmentContainerKey;
							for(var j=0; j<containerModel.length;j++)
							{
								  var newContainerKey =  containerModel[j].ShipmentContainerKey;
								  if(_scBaseUtils.equals(currentContainerKey, newContainerKey))
								  {	//If block to match current and ContainerItemDetails ContShpKey - begin
		

										
									if(!(_scBaseUtils.isVoid(containerModel)) && !_scBaseUtils.equals(containerModel[j].ContainerType, "VendorPackage")){


										var targetModel = null;
										var packageWeight = 0;
										targetModel = _scBaseUtils.getTargetModel(repPanelScreen, "changeShipment_input", null);
										packageWeight = _scModelUtils.getNumberValueFromPath("Shipment.Containers.Container.ActualWeight", targetModel);

										if (!(_scModelUtils.hasAttributeInModelPath("Shipment.Containers.Container.ActualWeight", targetModel))) {
											var msg = null;
											msg = _scScreenUtils.getString(repPanelScreen, "NoPackageWeight");
											_wscContainerPackUtils.highlightWeightError(repPanelScreen, "NoPackageWeight", "errorMsgPnl", "activeRepeatingPanelUId");
											isInvalidWeightLocal=true;
											break;
										} 
										else if (_scBaseUtils.numberLessThan(packageWeight, 0)) {
											_wscContainerPackUtils.highlightWeightError(
											repPanelScreen, "NegativePackageWeight", "errorMsgPnl", "activeRepeatingPanelUId");
											isInvalidWeightLocal=true;
											break;
										}
										else if (_scBaseUtils.equals(packageWeight, 0)) {
											_wscContainerPackUtils.highlightWeightError(
											repPanelScreen, "Invalid Weight. Container weight Cannot be zero.", "errorMsgPnl", "activeRepeatingPanelUId");
											isInvalidWeightLocal=true;
											break;
										}
										/*else if (_scBaseUtils.equals(packageWeight, _scModelUtils.getNumberValueFromPath("Container.ActualWeight", container_Src))) {
											_iasScreenUtils.toggleHighlight(
											_iasUIUtils.getParentScreen(
											repPanelScreen, true), repPanelScreen, "activeRepeatingPanelUId", "errorMsgPnl", "information", "PackageWeightNotChanged");
											isInvalidWeightLocal=true;
											break;
										}*/ 
										else {
											if (_scBaseUtils.equals(_wscContainerPackUtils.getScacIntegrationReqd(repPanelScreen), "N")) {
												_scModelUtils.setStringValueAtModelPath("Shipment.Containers.Container.IsPackProcessComplete", "Y", targetModel);
											} 
											else {
												var trackingNo = null;
												trackingNo = _scModelUtils.getStringValueFromPath("Container.TrackingNo", container_Src);
												if (!(_scBaseUtils.isVoid(trackingNo))) {
													_scModelUtils.setStringValueAtModelPath("Shipment.CallVoidTrackingNo", "Y", targetModel);
												}
											}
											/*var eventDefn = null;
											var blankModel = null;
											eventDefn = {};
											blankModel = {};
											_scBaseUtils.setAttributeValue("argumentList", blankModel, eventDefn);
											_scBaseUtils.setAttributeValue("argumentList.changeShipment_input", targetModel, eventDefn);
											_scBaseUtils.setAttributeValue("argumentList.getShipmentContainerDetails_input", _scBaseUtils.getTargetModel(
											this, "getShipmentContainerDetails_input", null), eventDefn);
											_scEventUtils.fireEventToParent(
											this, "recordContainerWeight", eventDefn);*/

											//Save container weight & Finish Pack in single transaction changes

											
											containerWeightInputModel.Shipment.ShipmentKey=container_Src.Container.ShipmentKey;
											containerWeightInputModel.Shipment.OrderNo=orderNo;
											containerWeightInputModel.Shipment.SCAC=getShipmentDetailsModel.Shipment.SCAC;

											var existingContainerKey=targetModel.Shipment.Containers.Container.ShipmentContainerKey;
											for(var k=0; k<containerModel.length;k++)
											{
												  var newContainerKey =  containerModel[k].ShipmentContainerKey;
												  if(_scBaseUtils.equals(existingContainerKey, newContainerKey))
												  {

													targetModel.Shipment.Containers.Container.ContainerGrossWeight=packageWeight;
													targetModel.Shipment.Containers.Container.ContainerScm=containerModel[k].ContainerScm;
													targetModel.Shipment.Containers.Container.ContainerType=containerModel[k].ContainerType;
													targetModel.Shipment.Containers.Container.ContainerTypeKey=containerModel[k].ContainerTypeKey;
												  }
											}
											containerWeightInputModel.Shipment.Containers.Container.push(targetModel.Shipment.Containers.Container);
										}


									}
									else if (!(_scBaseUtils.isVoid(containerModel)) && _scBaseUtils.equals(containerModel[j].ContainerType, "VendorPackage")) {
										 //Vendor Package start

										  var targetModel = null;
										var packageWeight = 0;
										targetModel = _scBaseUtils.getTargetModel(repPanelScreen, "changeShipment_input", null);
										packageWeight = _scModelUtils.getNumberValueFromPath("Shipment.Containers.Container.ActualWeight", targetModel);

										if(_scBaseUtils.isVoid(targetModel.Shipment.Containers.Container.ActualWeight)) {
												targetModel.Shipment.Containers.Container.ActualWeight=0;
										}

												if (_scBaseUtils.equals(_wscContainerPackUtils.getScacIntegrationReqd(repPanelScreen), "N")) {
												_scModelUtils.setStringValueAtModelPath("Shipment.Containers.Container.IsPackProcessComplete", "Y", targetModel);
											} 
											else {
												var trackingNo = null;
												trackingNo = _scModelUtils.getStringValueFromPath("Container.TrackingNo", container_Src);
												if (!(_scBaseUtils.isVoid(trackingNo))) {
													_scModelUtils.setStringValueAtModelPath("Shipment.CallVoidTrackingNo", "Y", targetModel);
												}
											}

											//Save container weight & Finish Pack in single transaction changes
											containerWeightInputModel.Shipment.ShipmentKey=container_Src.Container.ShipmentKey;
											containerWeightInputModel.Shipment.OrderNo=orderNo;
											containerWeightInputModel.Shipment.SCAC=getShipmentDetailsModel.Shipment.SCAC;

											var existingContainerKey=targetModel.Shipment.Containers.Container.ShipmentContainerKey;
											for(var k=0; k<containerModel.length;k++)
											{
												  var newContainerKey =  containerModel[k].ShipmentContainerKey;
												  if(_scBaseUtils.equals(existingContainerKey, newContainerKey))
												  {

													targetModel.Shipment.Containers.Container.ContainerGrossWeight=targetModel.Shipment.Containers.Container.ActualWeight;
													targetModel.Shipment.Containers.Container.ContainerScm=containerModel[k].ContainerScm;
													targetModel.Shipment.Containers.Container.ContainerType=containerModel[k].ContainerType;
													targetModel.Shipment.Containers.Container.ContainerTypeKey=containerModel[k].ContainerTypeKey;
												  }
											}
											containerWeightInputModel.Shipment.Containers.Container.push(targetModel.Shipment.Containers.Container);


										 //Vendor Package end   
									}
		


								  }//If block to match current and ContainerItemDetails ContShpKey - end
							}

							
						} 
						else {_wscContainerPackUtils.highlightWeightError(
							repPanelScreen, "NoPackageWeight", "errorMsgPnl", "activeRepeatingPanelUId");
						}

						if(!isInvalidWeightLocal){
							_scWidgetUtils.hideWidget(repPanelScreen, "errorMsgPnl", false);
						}
					}//for end 

							//_scModelUtils.addModelToModelPath("Shipment.Containers.Container", containerModel, containerWeightInputModel);

						if(!isInvalidWeightLocal){
							this.isInvalidWeightGlobal=false;
							//_iasBaseTemplateUtils.hideMessage(repPanelScreen);
							
								if(!(_scBaseUtils.isVoid(containerWeightInputModel.Shipment.Containers.Container)) && _scBaseUtils.equals(containerWeightInputModel.Shipment.Containers.Container.length, shpContListOutputModel.Containers.Container.length)){
									_iasUIUtils.callApi(this, containerWeightInputModel, "extn_AcademyUpdateWeightAndFinishPack_Ref", null);        	
								}
								else{
									_iasBaseTemplateUtils.showMessage(this, "Select type and weight for all containers.", "error", null);

								}        	
							
						}
						else if(isInvalidWeightLocal){
							this.isInvalidWeightGlobal=true;
						}
						//Validate container weight - end




						//if block to check remaining containers weighed - end
					//}
					//else{
					//	_iasBaseTemplateUtils.showMessage(this, "Select Container Type and Weight for remaining Containers.", "error", null);
					//}


				//}

						


				//If block to check if no container type & weight selected - end
			//}
			//else{
			//	_iasBaseTemplateUtils.showMessage(this, "Select Container Type and Weight for all containers.", "error", null);
			//}


			
            

           
        },
        //BOPIS-1576: Remove manual "Enter" hit after container weight input - end

        //BOPIS-1576: Remove manual "Enter" hit after container weight input - begin
	    handleMashupCompletion: function(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
            _iasBaseTemplateUtils.handleMashupCompletion(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);

            /*if(hasError && _scBaseUtils.equals(inputData[0].mashupRefId, "extn_containerPack_changeShipmentForWeight_Ref")){
                this.ToInvokeFinishPack=false;
            }
	        else if(!hasError && _scBaseUtils.equals(inputData[0].mashupRefId, "extn_containerPack_changeShipmentForWeight_Ref")){
                this.ToInvokeFinishPack=true;
            }*/		
        },
        //BOPIS-1576: Remove manual "Enter" hit after container weight input - end

	    save: function(event, bEvent, ctrl, args) {
            //BOPIS-1576: Remove manual "Enter" hit after container weight input - begin        
		    //if(!this.isInvalidWeightGlobal){
                var targetModel = null;
                targetModel = _scBaseUtils.getTargetModel(
                this, "getShipmentContainerList_input", null);
                if (
                _scBaseUtils.equals(
                this.scacIntegrationReqd, "N")) {
                    _iasUIUtils.callApi(
                    this, targetModel, "containerPack_getShipmentContainerList_NoScac", null);
                } else if (
                _scBaseUtils.equals(
                this.scacIntegrationReqd, "Y")) {
                    _iasUIUtils.callApi(
                    this, targetModel, "containerPack_getShipmentContainerList_Scac", null);
                }        
            //}
            //BOPIS-1576: Remove manual "Enter" hit after container weight input - end
        },
		
		ShowConfirmBeforeChangingActiveView: function(
        childScreen, contentPneUidToaddCss, args) {
            var argsBean = null;
            argsBean = {};
            _scBaseUtils.setAttributeValue("args", args, argsBean);
            _scBaseUtils.setAttributeValue("childScreen", childScreen, argsBean);
            _scBaseUtils.setAttributeValue("contentPneUidToaddCss", contentPneUidToaddCss, argsBean);
            var isScreenDirty = false;
            var currentScreen = null;
            currentScreen = _scScreenUtils.getChildScreen(
            this, this.currentView);


			/*if(_scBaseUtils.equals(currentScreen.className, "ContainerPackContainerView")){
				var getShipmentContainerList_output = _scScreenUtils.getModel(this, "getShipmentContainerList_output");

				var weightModel={};
						 weightModel.ContainerWeights={};
						 weightModel.ContainerWeights.ContainerWeight=[];
				
				for(var i=0; i<getShipmentContainerList_output.Containers.Container.length; i++){
			
					var childScreenRepPanelUId = _scRepeatingPanelUtils.returnUIdOfIndividualRepeatingPanel(currentScreen, "containerListRepPanel", getShipmentContainerList_output.Containers.Container[i].ShipmentContainerKey);
					var childScreenRepPanelScreen = _iasScreenUtils.getRepeatingPanelScreenWidget(currentScreen, childScreenRepPanelUId);

					var containerWeight = null;
					 containerWeight = _scWidgetUtils.getValue(childScreenRepPanelScreen, "containerWeight");

					 weightModel.ContainerWeights.ContainerWeight.push(
						{
							ShipmentContainerKey : getShipmentContainerList_output.Containers.Container[i].ShipmentContainerKey,
							ContainerWeight : containerWeight
						}
					 );
				}
				_scScreenUtils.setModel(this, "ExtnWeightModel", weightModel);		
			}*/

            isScreenDirty = _scScreenUtils.isDirty(
            currentScreen, null, true);
            if (
            isScreenDirty) {
				//BOPIS-1576: Modified OOB method to suppress confirmation popup on switching tabs - begin        
                //_scScreenUtils.showConfirmMessageBox(
                //this, _scScreenUtils.getString(
                //this, "DirtyConfirmMessage"), "ChangeActiveView", null, argsBean);
                this.ChangeActiveView("Ok", argsBean);
				//BOPIS-1576: Modified OOB method to suppress confirmation popup on switching tabs - end
            } else {
                this.ChangeActiveView("Ok", argsBean);
            }
        },
        //changes for hiding OOTB finish pack button from wizard and adding custom finish pack button in screen : Begin 
        extnHandleFinishPack: function(event, bEvent, ctrl, args) {
            var parentScreen = _iasUIUtils.getParentScreen(this, true);
            var wizscreen = _scWizardUtils.getCurrentPage(parentScreen);
            if (_scScreenUtils.validate(wizscreen)) {
                this.save(event, bEvent, ctrl, args);
            }
            else {
                _iasBaseTemplateUtils.showMessage(this, "Message_screenHasErrors", "error", null);
            }
        },
        customEntityExists: function() {
            return false;
        },
        handleWizardCloseConfirmation: function(res, args) {
            var parentScreen = _iasUIUtils.getParentScreen(this, true);
            var argumentList = null;
            var closeTab = false;
            argumentList = _scBaseUtils.getAttributeValue("argumentList", false, args);
            if (!(_scBaseUtils.isVoid(argumentList))) {
                closeTab = _scBaseUtils.getAttributeValue("closeTab", false, argumentList);
            }
            if ( _scBaseUtils.equals(res, "Ok") && _scBaseUtils.or(this.customEntityExists(), closeTab)) {
                _scWizardUtils.closeWizard(this);
            } else {
                //_scScreenUtils.clearScreen(this, null);
                _iasWizardUtils.handleWizardCloseConfirmation(parentScreen, res, args);
            }
        }
         //changes for hiding OOTB finish pack button from wizard and adding custom finish pack button in screen : End



});
});

