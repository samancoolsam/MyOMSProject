
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/container/pack/ContainerPackContainerViewExtnUI", "scbase/loader!ias/utils/EventUtils","scbase/loader!ias/utils/BaseTemplateUtils", "scbase/loader!ias/utils/RepeatingScreenUtils", "scbase/loader!ias/utils/ScreenUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/ControllerUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/RepeatingPanelUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!wsc/components/shipment/container/pack/ContainerPackContainerViewUI", "scbase/loader!wsc/components/shipment/container/pack/ContainerPackUtils","scbase/loader!sc/plat/dojo/Userprefs"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnContainerPackContainerViewExtnUI
			 ,
			 	_iasEventUtils
			 , 
			 	_iasBaseTemplateUtils, _iasRepeatingScreenUtils, _iasScreenUtils, _iasUIUtils, _scBaseUtils, _scControllerUtils, _scEventUtils, _scModelUtils, _scRepeatingPanelUtils, _scScreenUtils, _scWidgetUtils, _wscContainerPackContainerViewUI, _wscContainerPackUtils,_scUserprefs
){ 
	return _dojodeclare("extn.components.shipment.container.pack.ContainerPackContainerViewExtn", [_extnContainerPackContainerViewExtnUI],{
	// custom code here
	// Overriding method to invoke backend service 'AcademyUpdateContainerWeight' : Begin
	ContainerItemDetails: [],
	ManualContainerTypeDetails: [],
	recordContainerWeight: function(event, bEvent, ctrl, args) {
        var lastUpdatedrepPanel = null;
        lastUpdatedrepPanel = _iasScreenUtils.getRepeatingPanelScreenWidget(
        this, this.activeRepeatingPanelUId);
        if (!(
        _scBaseUtils.isVoid(
        lastUpdatedrepPanel))) {
            _scScreenUtils.removeClass(
            lastUpdatedrepPanel, "glowAndFadeout");
        }
        var changeShipment_input = null;
        changeShipment_input = _scBaseUtils.getValueFromPath("changeShipment_input", args);
        var targetModel = null;
        targetModel = _scBaseUtils.getTargetModel(
        this, "changeShipment_input", null);
        changeShipment_input = _scBaseUtils.mergeModel(
        targetModel, changeShipment_input, false);
		
        // Changes for invoking backend service 'AcademyUpdateContainerWeight' : Begin
        var getShipmentDetails_output = _scScreenUtils.getModel(this, "getShipmentDetails_output");
        var scac = _scModelUtils.getStringValueFromPath("Shipment.SCAC", getShipmentDetails_output);
        _scModelUtils.setStringValueAtModelPath("Shipment.SCAC", scac, changeShipment_input);
        // Changes for invoking backend service 'AcademyUpdateContainerWeight' : End
        var getShipmentContainerDetails_input = null;
        getShipmentContainerDetails_input = _scBaseUtils.getValueFromPath("getShipmentContainerDetails_input", args);
        var getShipmentContainerDetailsInputModel = null;
        getShipmentContainerDetailsInputModel = _scBaseUtils.getTargetModel(
        this, "getShipmentContainerDetails_input", null);
        getShipmentContainerDetails_input = _scBaseUtils.mergeModel(
        getShipmentContainerDetailsInputModel, getShipmentContainerDetails_input, false);
        var refId = null;
        refId = [];
        refId.push("containerPack_changeShipmentForWeight");
        refId.push("pack_getShipmentContainerDetails");
        var target = null;
        target = [];
        target.push(
        changeShipment_input);
        target.push(
        getShipmentContainerDetails_input);
        var mashupContext = null;
        mashupContext = _scControllerUtils.getMashupContext(
        this);
        mashupContext["ShipmentContainerKey"] = _scModelUtils.getStringValueFromPath("Shipment.Containers.Container.ShipmentContainerKey", changeShipment_input);
        _iasUIUtils.callApis(
        this, target, refId, mashupContext, null);
    },
    // Overriding method to invoke backend service 'AcademyUpdateContainerWeight' : End
    extn_afterInitializeScreen: function(event, bEvent, ctrl, args) {
        var bEvent = _scModelUtils.getStringValueFromPath("_id", bEvent);
    	var getContainerListModel = _scScreenUtils.getModel(this, "getContainerList_Out_Src_Pg");
    	var getContainerList = _scModelUtils.getStringValueFromPath("Page.Output.Containers.Container", getContainerListModel);
    	if (!_scBaseUtils.isVoid(getContainerList)) {
    		var isTrackingNoNotSet = false;
    		for (var i=0;i<getContainerList.length;i++) {
    			var trackingNo = _scModelUtils.getStringValueFromPath("TrackingNo", getContainerList[i]);
    			if (_scBaseUtils.isVoid(trackingNo)) {
    				isTrackingNoNotSet =  true;
    				break;
    			}
    		}
    		
    	}
        // Code changes for hiding AddNewContainer button after all the products are packed: Begin
        if(!_scBaseUtils.equals(bEvent, "afterBehaviorMashupCall")) {
            this.hideAddNewContainer();
        }
        // Code changes for hiding AddNewContainer button after all the products are packed: End
    },

    getTrackingNoAndPrintLabel: function(
    event, bEvent, ctrl, args) {
        var getTrackingNoAndPrintLabel_input = null;
        getTrackingNoAndPrintLabel_input = _scBaseUtils.getValueFromPath("getTrackingNoAndPrintLabel_input", args);
        var targetModel = null;
        targetModel = _scBaseUtils.getTargetModel(
        this, "getTrackingNoAndPrintLabel_input", null);
        getTrackingNoAndPrintLabel_input = _scBaseUtils.mergeModel(
        targetModel, getTrackingNoAndPrintLabel_input, false);
        // fix for BOPIS-1256: Begin
        var containerGrossWeight = _scModelUtils.getStringValueFromPath("Container.ContainerGrossWeight", getTrackingNoAndPrintLabel_input);
        if(_scBaseUtils.isVoid(containerGrossWeight) || Number.isNaN(containerGrossWeight)){
           _scModelUtils.setStringValueAtModelPath("Container.ContainerGrossWeight", "0.00", getTrackingNoAndPrintLabel_input);
        }
        // fix for BOPIS-1256: End
        var containerModel = _scScreenUtils.getModel(this, "activeContainerModel");
        var containerScm = _scModelUtils.getStringValueFromPath("Container.ContainerScm", containerModel);
        _scModelUtils.setStringValueAtModelPath("Container.ContainerScm", containerScm, getTrackingNoAndPrintLabel_input);

        var getShipmentContainerDetails_input = null;
        getShipmentContainerDetails_input = _scBaseUtils.getValueFromPath("getShipmentContainerDetails_input", args);
        var getShipmentContainerDetailsInputModel = null;
        getShipmentContainerDetailsInputModel = _scBaseUtils.getTargetModel(
        this, "getShipmentContainerDetails_input", null);
        getShipmentContainerDetails_input = _scBaseUtils.mergeModel(
        getShipmentContainerDetailsInputModel, getShipmentContainerDetails_input, false);
        var refId = null;
        refId = [];
        refId.push("containerPack_StoreContainerLabel_94");
        refId.push("pack_getShipmentContainerDetails");
        var target = null;
        target = [];
        target.push(
        getTrackingNoAndPrintLabel_input);
        target.push(
        getShipmentContainerDetails_input);
        var mashupContext = null;
        mashupContext = _scControllerUtils.getMashupContext(
        this);
        mashupContext["IsPrintRequested"] = "Yes";
        _iasUIUtils.callApis(
        this, target, refId, mashupContext, null);
    },

    beforeScreenInit: function() {
        var inputToMashup = _scModelUtils.createNewModelObjectWithRootKey("Item");
        _iasUIUtils.callApi(this, inputToMashup, "extn_dropDownValues_ref", null);
		//OMNI-73467 Start
		var getCommonCodeInput = {};                
        getCommonCodeInput = _scModelUtils.createModelObjectFromKey("CommonCode", getCommonCodeInput);
        _scModelUtils.setStringValueAtModelPath("CommonCode.CodeType", "CONTAINER_THRESHOLD" , getCommonCodeInput);
        _iasUIUtils.callApi(this, getCommonCodeInput, "extn_ContainerRecommToggle_ref", null);
		//OMNI-73467 End
    },

    extn_afterBehaviorMashupCall: function(event, bEvent, ctrl, args) {
    	var mashupRefList = _scModelUtils.getModelObjectFromPath("mashupArray", args);
    	if (!_scBaseUtils.isVoid(mashupRefList)) {
        	for (var i = 0; i < mashupRefList.length; i++) {
                var mashupRefid = _scModelUtils.getStringValueFromPath("mashupRefId", mashupRefList[i]);
                if (_scBaseUtils.equals(mashupRefid, "pack_getShipmentContainerDetails")) {
                	var modelOutput = mashupRefList[i].mashupRefOutput;
                	var trackingNo = _scModelUtils.getStringValueFromPath("Container.TrackingNo", modelOutput);
                	var UpdatedShipmentContainerKey = _scModelUtils.getStringValueFromPath("Container.ShipmentContainerKey", modelOutput);
                	var getContainerListModel = _scScreenUtils.getModel(this, "getContainerList_Out_Src_Pg");
    				var getContainerList = _scModelUtils.getStringValueFromPath("Page.Output.Containers.Container", getContainerListModel);
    				if (!_scBaseUtils.isVoid(getContainerList)) {
						for (var j=0;j<getContainerList.length;j++) {
							var shipmentContainerKey = _scModelUtils.getStringValueFromPath("ShipmentContainerKey", getContainerList[j]);
							if (_scBaseUtils.equals(shipmentContainerKey, UpdatedShipmentContainerKey)) {
								_scModelUtils.setStringValueAtModelPath("TrackingNo", trackingNo, getContainerList[j]);
							}
						}
					}
					// _scScreenUtils.setModel(this, "getContainerList_Out_Src_Pg", getContainerListModel);
                    var shipmentKey = "";
                    shipmentKey = _scModelUtils.getStringValueFromPath("Container.Shipment.ShipmentKey", modelOutput);
//                  _scUserprefs.setProperty("getContainerList_Out", getContainerListModel);
                    _scUserprefs.setProperty(shipmentKey, getContainerListModel);
					this.extn_afterInitializeScreen(event, bEvent, ctrl, args);
                    // Code changes for hiding AddNewContainer button after all the products are packed: Begin
                    var getShipmentDetailsInitModel = _scScreenUtils.getModel(this, "getShipmentDetails_output");
                    var shipmentLineList1 = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines", getShipmentDetailsInitModel);
                    var initModelShipmentLineList = _scModelUtils.getStringValueFromPath("ShipmentLine", shipmentLineList1[0]);
                    var shipmentLineList = _scModelUtils.getStringValueFromPath("Container.Shipment.ShipmentLines.ShipmentLine", modelOutput);
                    if (!_scBaseUtils.isVoid(shipmentLineList)) {
                        var isPackNotCompleted = false;
                        for (var k=0;k<shipmentLineList.length;k++) {
                            var shipmenLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", shipmentLineList[k]);
                            var isPackComplete = _scModelUtils.getStringValueFromPath("IsPackComplete", shipmentLineList[k]);
                            if (_scBaseUtils.isVoid(isPackComplete)) {
                                isPackNotCompleted = true ;
                                break;
                            }
                            else if (_scBaseUtils.equals(isPackComplete, "Y")){
                                isPackNotCompleted = false;
                                if (!_scBaseUtils.isVoid(initModelShipmentLineList)) {
                                    for (var l=0;l<initModelShipmentLineList.length;l++) {
                                        var initModelShipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", initModelShipmentLineList[l]);
                                        if(_scBaseUtils.equals(shipmenLineKey, initModelShipmentLineKey)) {
                                            _scModelUtils.setStringValueAtModelPath("IsPackComplete", isPackComplete, initModelShipmentLineList[l]);
                                        }
                                    }
                                }
                            }
                        }
                        
                    }
                    // Code changes for hiding AddNewContainer button after all the products are packed: End
                }
                if (_scBaseUtils.equals(mashupRefid, "containerPack_StoreContainerLabel_94")) {
                	var modelOutput1 = mashupRefList[i].mashupRefOutput;
                	var updatedContainerDetails = {}
                	for (var k = 0; k < mashupRefList.length; k++) {
                		var mashupRefid = _scModelUtils.getStringValueFromPath("mashupRefId", mashupRefList[k]);
                		if (_scBaseUtils.equals(mashupRefid, "pack_getShipmentContainerDetails")) {
                			updatedContainerDetails = mashupRefList[k].mashupRefOutput;
                		}
                	}
	                if (!_scModelUtils.hasAttributeInModelPath("Output.Failed", modelOutput1)) {
	                	var childScreenRepPanelUId = _scRepeatingPanelUtils.returnUIdOfIndividualRepeatingPanel(this, "containerListRepPanel", _scModelUtils.getStringValueFromPath("Container.ShipmentContainerKey", updatedContainerDetails));
						var childScreenRepPanelScreen = _iasScreenUtils.getRepeatingPanelScreenWidget(this, childScreenRepPanelUId);
						_scWidgetUtils.disableWidget(childScreenRepPanelScreen, "extn_textfield", false);
						_scWidgetUtils.disableWidget(childScreenRepPanelScreen, "extn_filteringselect", false);
						_scWidgetUtils.disableWidget(childScreenRepPanelScreen, "containerWeight", false);
	                	var getShipmentDetails_output = _scScreenUtils.getModel(this, "getShipmentDetails_output");
	                    var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
	                    var itemId = _scUserprefs.getProperty("ItemId");
	                    var itemKey = "";
	                    var itemListModel = _scScreenUtils.getModel(this, "extn_dropDownValues");
	                    var itemList = _scModelUtils.getStringValueFromPath("ItemList.Item", itemListModel);
		                if (!_scBaseUtils.isVoid(itemList)) {
		                    for (var count=0;count<itemList.length;count++) {
		                    	var newItemId = _scModelUtils.getStringValueFromPath("ItemID", itemList[count]);
		                    	if (_scBaseUtils.equals(itemId, newItemId)) {
		                    		itemKey = _scModelUtils.getStringValueFromPath("ItemKey", itemList[count]);
		                    	}
		                    }
		                }
	                    var orderNo = _scModelUtils.getStringValueFromPath("Shipment.DisplayOrderNo", getShipmentDetails_output);
                        var containerGrossWeight = _scModelUtils.getStringValueFromPath("Container.ActualWeight", updatedContainerDetails);
                        var shipmentKey = _scModelUtils.getStringValueFromPath("Container.Shipment.ShipmentKey", updatedContainerDetails);
                        var shipmentContainerKey = _scModelUtils.getStringValueFromPath("Container.ShipmentContainerKey", updatedContainerDetails);
                        var containerScm = _scModelUtils.getStringValueFromPath("Container.ContainerScm", updatedContainerDetails);
                        _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, inputModel);
                        _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentContainerKey", shipmentContainerKey, inputModel);
                        _scModelUtils.setStringValueAtModelPath("Shipment.ContainerScm", containerScm, inputModel);
                        if(_scBaseUtils.equals(itemId, "VendorPackage")) {
                            _scModelUtils.setStringValueAtModelPath("Shipment.ContainerGrossWeight", "0.00", inputModel);
                        }
                        else {
                            _scModelUtils.setStringValueAtModelPath("Shipment.ContainerGrossWeight", containerGrossWeight, inputModel);
                        }

                        _scModelUtils.setStringValueAtModelPath("Shipment.OrderNo", orderNo, inputModel);

                        // _scModelUtils.setStringValueAtModelPath("Shipment.ContainerType", "000007575", inputModel);
                        // _scModelUtils.setStringValueAtModelPath("Shipment.ContainerTypeKey", "201807201250243055852", inputModel);
                        _scModelUtils.setStringValueAtModelPath("Shipment.ContainerType", itemId, inputModel);

                        _scModelUtils.setStringValueAtModelPath("Shipment.ContainerTypeKey", itemKey, inputModel);
                        _iasUIUtils.callApi(this, inputModel, "extn_SFSBeforeCreateContainersAndPrint_ref", null);
                    } 
	            }
				//OMNI-73467 Start
				if(_scBaseUtils.equals(mashupRefid, "extn_ContainerRecommToggle_ref")) {
                    var mashupOutputObject = mashupRefList[i].mashupRefOutput;
					_scScreenUtils.setModel(this, "extn_ContainerRecomToggle", mashupOutputObject, null);
				}
				//OMNI-73467 End
                if(_scBaseUtils.equals(mashupRefid, "extn_dropDownValues_ref")) {
                    var mashupOutputObject = mashupRefList[i].mashupRefOutput;

                    mashupOutputObject.ItemList.Item.sort(function compare(a,b) {
                      if (a.ItemID < b.ItemID)
                        return -1;
                      if (a.ItemID > b.ItemID)
                        return 1;
                      return 0;
                    });
					//OMNI-23764 Vendor Package can be used for AmmoContainer Type
                    //hard code vendor package - Begin
                    var vandorPackage = {
                        ItemID: "VendorPackage",
                        ItemKey: "",
                        PrimaryInformation: {
                            Description: "Vendor Package",
                            ItemType: "AmmoContainer"
                        }
                    }

                    mashupOutputObject.ItemList.Item.push(vandorPackage);
                    //hard code vendor package - End
				

                    _scScreenUtils.setModel(this, "extn_dropDownValues", mashupOutputObject, null);
                    var getContainerListModel = _scScreenUtils.getModel(this, "getContainerList_Out_Src_Pg");
                    var getContainerList = _scModelUtils.getStringValueFromPath("Page.Output.Containers.Container", getContainerListModel);

                    for(var i=0; i < getContainerList.length; i++) {
                        var container = getContainerList[i];
                        var shipmentContainerkey = _scModelUtils.getStringValueFromPath("ShipmentContainerKey", container);
                        _scRepeatingPanelUtils.setModelForIndividualRepeatingPanel(
                        this, "containerListRepPanel", shipmentContainerkey, "extn_containerIDs", mashupOutputObject, null);
                    }
					 var targetModel = null;
						targetModel = _scBaseUtils.getTargetModel(
						this, "getShipmentContainerList_input", null);
						_scRepeatingPanelUtils.startPaginationUsingUId(
						this, "containerListRepPanel", "getContainerList_Out_Src_Pg", targetModel);
                }
                if(_scBaseUtils.equals(mashupRefid, "containerPack_getShipmentContainerList_pg")) {
					var getContainerListModel = mashupRefList[i].mashupRefOutput;
					var getContainerList = _scModelUtils.getStringValueFromPath("Page.Output.Containers.Container", getContainerListModel);

					//BOPIS-1576_CR: BOPIS-1992 - save weight on UI changes - begin
					var weightModel={};
						 weightModel.ContainerWeights={};
						 weightModel.ContainerWeights.ContainerWeight=[];
						 
					for(var i=0; i<getContainerList.length; i++){
							var childScreenRepPanelUId = _scRepeatingPanelUtils.returnUIdOfIndividualRepeatingPanel(this, "containerListRepPanel", getContainerList[i].ShipmentContainerKey);
							var childScreenRepPanelScreen = _iasScreenUtils.getRepeatingPanelScreenWidget(this, childScreenRepPanelUId);

							var containerWeight = null;
							 containerWeight = _scWidgetUtils.getValue(childScreenRepPanelScreen, "containerWeight");

							 
							 weightModel.ContainerWeights.ContainerWeight.push(
								{
									ShipmentContainerKey : getContainerList[i].ShipmentContainerKey,
									ContainerWeight : containerWeight
								}
							 );
					}
					 _scScreenUtils.setModel(this, "ExtnWeightModel", weightModel);	
					//BOPIS-1576_CR: BOPIS-1992 - save weight on UI changes - end
					
					var shipmentKey = "";
                    if (!_scBaseUtils.isVoid(getContainerList)) {
						var isTrackingNoNotSet = false;
						for (var i=0;i<getContainerList.length;i++) {
							var trackingNo = _scModelUtils.getStringValueFromPath("TrackingNo", getContainerList[i]);
                            shipmentKey = _scModelUtils.getStringValueFromPath("ShipmentKey", getContainerList[i]);
							if (_scBaseUtils.isVoid(trackingNo)) {
								isTrackingNoNotSet =  true;
								break;
							}
						}
						
					}
					
					// _scUserprefs.setProperty("getContainerList_Out", getContainerListModel);
                    _scUserprefs.setProperty(shipmentKey, getContainerListModel);
          // Code changes for hiding AddNewContainer button after all the products are packed: Begin
                if (!_scBaseUtils.isVoid(getContainerList)) {
                    var shipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", getContainerList[0]);
                    if (!_scBaseUtils.isVoid(shipmentLineList)) {
                        var isPackNotCompleted = false;
                        for (var k=0;k<shipmentLineList.length;k++) {
                            var shipmenLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", shipmentLineList[k]);
                            var isPackComplete = _scModelUtils.getStringValueFromPath("IsPackComplete", shipmentLineList[k]);
                            if (_scBaseUtils.isVoid(isPackComplete)) {
                                isPackNotCompleted = true ;
                                break;
                            }
                            else if (_scBaseUtils.equals(isPackComplete, "Y")){
                                isPackNotCompleted = false;
                            }
                        }
                        if (!isPackNotCompleted) {
                            _scWidgetUtils.disableWidget(this, "addNewContainerButton", false);
							//BOPIS-1576_CR: BOPIS-1992 - save weight on UI changes - begin
                            //_scWidgetUtils.setValue(childScreenRepPanelScreen, "containerWeight", linkText, false);
							//BOPIS-1576_CR: BOPIS-1992 - save weight on UI changes - end
                        }
                        else {
                            _scWidgetUtils.enableWidget(this, "addNewContainerButton", false);
                        }
                        
                    }
                }
                // this.hideAddNewContainer();
          // Code changes for hiding AddNewContainer button after all the products are packed: End
                }
            }
        }
    },
    handleMashupOutput: function(
        mashupRefId, modelOutput, modelInput, mashupContext) {
            if (
            _scBaseUtils.equals(
            mashupRefId, "pack_deleteContainer")) {
                var containerList = null;
                var containerListModel = null;
                var shipmentContainerizedFlag = 0;
                containerListModel = {};
                containerList = _scModelUtils.getModelListFromPath("Shipment.Containers.Container", modelOutput);
                _scModelUtils.addListToModelPath("Containers.Container", containerList, containerListModel);
                shipmentContainerizedFlag = _scModelUtils.getNumberValueFromPath("Shipment.ShipmentContainerizedFlag", modelOutput);
                if (
                _scBaseUtils.equals(
                shipmentContainerizedFlag, 1)) {
                    if (
                    _scBaseUtils.equals(
                    _wscContainerPackUtils.getDraftContainerFlag(
                    this), "Y")) {
                        var draftContainerInfo = null;
                        draftContainerInfo = _wscContainerPackUtils.selectActiveContainer("", _scScreenUtils.getModel(
                        this, "getShipmentContainerList_output"));
                        _wscContainerPackUtils.setActiveContainerModel(
                        this, draftContainerInfo);
                        containerList = [];
                        var tempModel = null;
                        tempModel = _scModelUtils.getModelObjectFromPath("Container", draftContainerInfo);
                        _scModelUtils.addModelObjectToModelList(
                        tempModel, containerList);
                        _scModelUtils.addListToModelPath("Containers.Container", containerList, containerListModel);
                    } else {
                        var activeContainerModel = null;
                        var activeContainerInfo = null;
                        activeContainerInfo = containerList[
                        0];
                        activeContainerModel = {};
                        _scModelUtils.addModelToModelPath("Container", activeContainerInfo, activeContainerModel);
                        _scModelUtils.setStringValueAtModelPath("Container.ShipmentContainerKey", "", activeContainerModel);
                        _scModelUtils.setStringValueAtModelPath("Container.ContainerNo", _scScreenUtils.getString(
                        this, "DraftContainer"), activeContainerModel);
                        _wscContainerPackUtils.setActiveContainerModel(
                        this, activeContainerModel);
                        _wscContainerPackUtils.setDraftContainerFlag(
                        this, "Y");
                    }
                } else {
                    if (
                    _scBaseUtils.getAttributeValue("isActiveContainer", false, mashupContext)) {
                        var containerCount = 0;
                        var activeContainerModel = null;
                        containerCount = _scBaseUtils.getAttributeCount(
                        containerList);
                        if (
                        containerCount > 0) {
                            activeContainerInfo = containerList[
                            0];
                            activeContainerModel = {};
                            _scModelUtils.addModelToModelPath("Container", activeContainerInfo, activeContainerModel);
                            _wscContainerPackUtils.setActiveContainerModel(
                            this, activeContainerModel);
                        }
                    }
                }
                _scModelUtils.setStringValueAtModelPath("Containers.TotalNumberOfRecords", _scModelUtils.getStringValueFromPath("Shipment.Containers.TotalNumberOfRecords", modelOutput), containerListModel);
                var eventDefn = null;
                var blankModel = null;
                eventDefn = {};
                blankModel = {};
                _scBaseUtils.setAttributeValue("argumentList", blankModel, eventDefn);
                _scBaseUtils.setAttributeValue("argumentList.containerList", containerListModel, eventDefn);
                _scEventUtils.fireEventToParent(
                this, "updateContainerCount", eventDefn);
                this.refreshContainerViewScreenData();
            } else if (
            _scBaseUtils.equals(
            mashupRefId, "containerPack_generateSCM")) {
                _wscContainerPackUtils.handleGenerateScmCall(
                this, mashupRefId, modelOutput, modelInput, mashupContext);
                this.goToProductsView();
            } else if (
            _scBaseUtils.equals(
            mashupRefId, "containerPack_changeShipmentForWeight")) {
            	// var childScreenRepPanelUId = _scRepeatingPanelUtils.returnUIdOfIndividualRepeatingPanel(this, "containerListRepPanel", _scBaseUtils.getAttributeValue("ShipmentContainerKey", false, mashupContext));
            	// var childScreenRepPanelScreen = _iasScreenUtils.getRepeatingPanelScreenWidget(this, childScreenRepPanelUId);
            	// _scWidgetUtils.enableWidget(childScreenRepPanelScreen, "extn_textfield", false);
            	// _scWidgetUtils.enableWidget(childScreenRepPanelScreen, "extn_filteringselect", false);
                this.handleContainerSave(
                modelOutput, modelInput, mashupContext);
            } else if (
            _scBaseUtils.equals(
            mashupRefId, "pack_getShipmentContainerDetails")) {
                var isPrintRequested = null;
                isPrintRequested = _scBaseUtils.getAttributeValue("IsPrintRequested", false, mashupContext);
                if (
                _scBaseUtils.equals(
                isPrintRequested, "Yes")) {
                    var trackingNo = null;
                    trackingNo = _scModelUtils.getStringValueFromPath("Container.TrackingNo", modelOutput);
                    if (!(
                    _scBaseUtils.isVoid(
                    trackingNo))) {
                        this.refreshContainerInformation(
                        mashupRefId, modelOutput, modelInput, mashupContext);
                    }
                } else {
                    this.refreshContainerInformation(
                    mashupRefId, modelOutput, modelInput, mashupContext);
                }
            } else if (
            _scBaseUtils.equals(
            mashupRefId, "containerPack_StoreLabelReprint_94")) {
                if (
                _scModelUtils.hasAttributeInModelPath("Output.out", modelOutput)) {
                    _wscContainerPackUtils.decodeShippingLabelURL(
                    modelOutput);
                    var shipmentContainerkey = null;
                    shipmentContainerkey = _scModelUtils.getStringValueFromPath("Container.ShipmentContainerKey", modelInput);
                    var repPanelUId = null;
                    repPanelUId = _scRepeatingPanelUtils.returnUIdOfIndividualRepeatingPanel(
                    this, "containerListRepPanel", shipmentContainerkey);
                    var repPanelScreen = null;
                    repPanelScreen = _iasScreenUtils.getRepeatingPanelScreenWidget(
                    this, repPanelUId);
                    if (!(
                    _scBaseUtils.isVoid(
                    repPanelScreen))) {
                        _wscContainerPackUtils.highlightErrorOrSuccess(
                        repPanelScreen, "success");
                    }
                } else {
                    this.handlePrintFailure(
                    mashupRefId, modelOutput, modelInput, mashupContext);
                }
            } else if (
            _scBaseUtils.equals(
            mashupRefId, "containerPack_StoreContainerLabel_94")) {
                if (
                _scModelUtils.hasAttributeInModelPath("Output.out", modelOutput)) {
                    // _wscContainerPackUtils.decodeShippingLabelURL(
                    // modelOutput);
                } else {
                    this.handlePrintFailure(
                    mashupRefId, modelOutput, modelInput, mashupContext);
                }
            }
        },
    hideAddNewContainer: function () {
        var getShipmentDetailsModel = _scScreenUtils.getModel(this, "getShipmentDetails_output");
		var shipmentLineList1 = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines", getShipmentDetailsModel);
        var shipmentLineList = _scModelUtils.getStringValueFromPath("ShipmentLine", shipmentLineList1[0]);
        if (!_scBaseUtils.isVoid(shipmentLineList)) {
            var isPackNotCompleted = false;
            for (var k=0;k<shipmentLineList.length;k++) {
                var isPackComplete = _scModelUtils.getStringValueFromPath("IsPackComplete", shipmentLineList[k]);
                if (_scBaseUtils.isVoid(isPackComplete)) {
                    isPackNotCompleted = true ;
                    break;
                }
                else if (_scBaseUtils.equals(isPackComplete, "Y")){
                    isPackNotCompleted = false;
                }
            }
            if (!isPackNotCompleted) {
                _scWidgetUtils.disableWidget(this, "addNewContainerButton", false);
            }
            else {
                _scWidgetUtils.enableWidget(this, "addNewContainerButton", false);
            }
        }
    },
	
	refreshContainerInformation: function(
        mashupRefId, modelOutput, modelInput, mashupContext) {
            var shipmentContainerkey = null;
            shipmentContainerkey = _scModelUtils.getStringValueFromPath("Container.ShipmentContainerKey", modelOutput);
            _scRepeatingPanelUtils.setModelForIndividualRepeatingPanel(
            this, "containerListRepPanel", shipmentContainerkey, "container_Src", modelOutput, null);
            var repPanelUId = null;
            repPanelUId = _scRepeatingPanelUtils.returnUIdOfIndividualRepeatingPanel(
            this, "containerListRepPanel", shipmentContainerkey);
            var repPanelScreen = null;
            repPanelScreen = _iasScreenUtils.getRepeatingPanelScreenWidget(
            this, repPanelUId);
            var containerDetails = this.ContainerItemDetails;
			var containerNo = modelOutput.Container.ContainerNo;
			for(var i=0;i<containerDetails.length;i++)
            {
              var containerNoNew = containerDetails[i].ContainerNo;
              if( _scBaseUtils.equals(containerNo, containerNoNew))  
			  {
					containerDetails[i].ActualWeight = modelOutput.Container.ActualWeight;
			  }
            }
            if (!(
            _scBaseUtils.isVoid(
            repPanelScreen))) {
                _scEventUtils.fireEventToChild(
                this, repPanelUId, "refreshContainerInformation", null);
            }
        },
		
		initializeScreen: function(
        event, bEvent, ctrl, args) {
            var scacIntegrationReqd = null;
            var getShipmentDetails_output = null;
            getShipmentDetails_output = _scScreenUtils.getModel(
            this, "getShipmentDetails_output");
            scacIntegrationReqd = _scModelUtils.getStringValueFromPath("Shipment.ScacIntegrationRequired", getShipmentDetails_output);
            this.scacIntegrationReqd = scacIntegrationReqd;
            var parentScreen = _iasUIUtils.getParentScreen(this,true);
            if(parentScreen.showContainerViewOnLoad){
            	_iasBaseTemplateUtils.showMessage(parentScreen, "Message_PackCompleted", "information", null);
				_scWidgetUtils.disableWidget(
                    this, "addNewContainerButton", false);	
            	parentScreen.showContainerViewOnLoad = false; //to show it only for the first time
            }
        }
		
		
  
});
});
