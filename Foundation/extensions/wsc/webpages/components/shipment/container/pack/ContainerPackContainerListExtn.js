
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/container/pack/ContainerPackContainerListExtnUI","scbase/loader!ias/utils/PrintUtils", "scbase/loader!ias/utils/RepeatingScreenUtils", "scbase/loader!ias/utils/ContextUtils", "scbase/loader!ias/utils/ScreenUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/RepeatingPanelUtils", "scbase/loader!sc/plat/dojo/utils/ResourcePermissionUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!wsc/components/common/utils/CommonUtils", "scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils", "scbase/loader!wsc/components/shipment/customerpickup/SummaryUI", "scbase/loader!sc/plat/dojo/Userprefs","scbase/loader!ias/utils/EventUtils","scbase/loader!ias/utils/BaseTemplateUtils","scbase/loader!wsc/components/shipment/container/pack/ContainerPackUtils","scbase/loader!sc/plat/dojo/utils/ControllerUtils","scbase/loader!dojo/_base/connect","scbase/loader!dojo/dom-attr"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnContainerPackContainerListExtnUI
			 ,
			 	_iasPrintUtils, _iasRepeatingScreenUtils, _iasContextUtils, _iasScreenUtils, _iasUIUtils, _scBaseUtils, _scEventUtils, _scModelUtils, _scRepeatingPanelUtils, _scResourcePermissionUtils, _scScreenUtils, _scWidgetUtils, _wscCommonUtils, _wscShipmentUtils, _wscSummaryUI, _scUserprefs, _iasEventUtils, _iasBaseTemplateUtils, _wscContainerPackUtils, _scControllerUtils, dConnect, dDomAttr

){ 
	return _dojodeclare("extn.components.shipment.container.pack.ContainerPackContainerListExtn", [_extnContainerPackContainerListExtnUI],{
	// custom code here
	// global variable to handle focus on containerWeight
	isFocusedOnWightOnEnter: false,

	ExtnhandleBarcodeScan: function() {
		if (
            _iasEventUtils.isEnterPressed(
            event)) {
			// var that = this;
			// setTimeout(function() {
				var targetModel = _scScreenUtils.getTargetModel(this,"extn_containerBarCode", null);
				var targetModel = targetModel[Object.keys(targetModel)[0]]
				var inputToMashup = _scModelUtils.createNewModelObjectWithRootKey("Item");
				_scModelUtils.setStringValueAtModelPath("Item.ItemAliasList.ItemAlias", {} ,inputToMashup);
				// _scModelUtils.setStringValueAtModelPath("Item.ItemAliasList.AliasName", targetModel ,inputToMashup);
				// _scModelUtils.setStringValueAtModelPath("Item.ItemAliasList.ItemAlias", {} ,inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Item.ItemAliasList.ItemAlias.AliasValue", targetModel.toUpperCase() ,inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Item.ItemAliasList.ItemAlias.ComplexQuery.Or.Exp.Value", targetModel.toLowerCase(), inputToMashup);

				_iasUIUtils.callApi(this, inputToMashup, "extn_getContainerDetail_ref", null);
			// },500);
            }
	},

	    saveContainerWeightOnEnter: function(
        event, bEvent, ctrl, args) {
        	if(this.isFocusedOnWightOnEnter){
        		this.isFocusedOnWightOnEnter = false;
        	}
            else if (
            _iasEventUtils.isEnterPressed(
            event)) {
                this.saveContainerWeight();
            }
			
			else {
                var targetModel = null;
                var packageWeight = 0;
                targetModel = _scBaseUtils.getTargetModel(
                this, "changeShipment_input", null);
                packageWeight = _scModelUtils.getNumberValueFromPath("Shipment.Containers.Container.ActualWeight", targetModel);
                var containerModel = null;
                var sourcePackageWeight = 0;
                containerModel = _scScreenUtils.getModel(
                this, "container_Src");
                sourcePackageWeight = _scModelUtils.getNumberValueFromPath("Container.ActualWeight", containerModel);
                if (
                _scBaseUtils.equals(
                sourcePackageWeight, packageWeight)) {
                    _scWidgetUtils.hideWidget(
                    this, "saveButton", true);
                } else {
                    _scWidgetUtils.hideWidget(
                    this, "saveButton", false, "");
                }
            }
        },

	handleMoreLink: function(
        event, bEvent, ctrl, args) {
            var container_Src = null;
            var popupParams = null;
            var bindings = null;
            var dialogParams = null;
            dialogParams = {};
            dialogParams["closeCallBackHandler"] = "onCloseCallSelection";
            dialogParams["class"] = "popupTitleBorder popupDialogFooterGap";
            container_Src = _scScreenUtils.getModel(
            this, "container_Src");
            bindings = {};
            _scBaseUtils.setAttributeValue("container_Src", container_Src, bindings);
            _scBaseUtils.setAttributeValue("scacIntegrationReqd", _wscContainerPackUtils.getScacIntegrationReqd(
            this), bindings);
            popupParams = {};
            _scBaseUtils.setAttributeValue("binding", bindings, popupParams);
            var title = null;
            var containerNo = null;
            containerNo = _scModelUtils.getStringValueFromPath("Container.ContainerNo", container_Src);
            var inputArray = null;
            inputArray = [];
            inputArray.push(
            containerNo);
            title = _scScreenUtils.getFormattedString(
            this, "extn_container_more", inputArray);
            _iasUIUtils.openSimplePopup("wsc.components.shipment.container.pack.ContainerPackMoreOption", title, this, popupParams, dialogParams);
        },

	infoCallBack: function() {

	},

	beforeScreenInit: function() {
		// var that = this;
		// setTimeout(function() {
		// _scWidgetUtils.setFocusOnWidgetUsingUid(this, "extn_textfield");
		var currentShipmentContainerKey = _scScreenUtils.getModel(this.ownerScreen,"activeContainerModel").Container.ShipmentContainerKey;
		var firstChildUID = "ContainerPackContainerList_"+currentShipmentContainerKey;
		_scWidgetUtils.setFocusOnWidgetUsingUid(_scScreenUtils.getChildScreen(this.ownerScreen,firstChildUID), "extn_textfield");
		//BOPIS-1576_CR: BOPIS-1992 - save weight on UI changes - begin
		//_scWidgetUtils.setValue(this, "containerWeight", "", null);
		//BOPIS-1576_CR: BOPIS-1992 - save weight on UI changes - end
			var parent = _iasUIUtils.getParentScreen(this, true);
			var model = _scScreenUtils.getModel(parent, "extn_dropDownValues");
			_scScreenUtils.setModel(this, "extn_containerIDs", model, null);
			var ddValues = _scScreenUtils.getModel(this, "extn_containerIDs");
			//OMNI-68037 begin

			var Flag=_scModelUtils.getStringValueFromPath("ItemList.EnableContainerDropDown",ddValues);
			console.log("Switch is " +Flag);
			if(_scBaseUtils.equals("N",Flag))
			{
				var varVendor={ItemList:{Item:[{ItemID: "VendorPackage",ItemKey: "",PrimaryInformation:{Description: "Vendor Package",ItemType: "AmmoContainer"}}]}};
				_scScreenUtils.setModel(this, "extn_containerIDs", varVendor, null);
			}
			//OMNI-68037 END
			
		// }, 500);
		// Containerization changes to hide containerWeght: Begin
		var containerModel = _scScreenUtils.getModel(this, "container_Src");
		var trackingNo = _scModelUtils.getStringValueFromPath("Container.TrackingNo", containerModel);
		if (!(_scBaseUtils.isVoid(trackingNo))) {
			_scWidgetUtils.disableWidget(this, "extn_textfield", false);
			_scWidgetUtils.disableWidget(this, "extn_filteringselect", false);
		}
		else {
			_scWidgetUtils.enableWidget(this, "extn_textfield", false);
			_scWidgetUtils.enableWidget(this, "extn_filteringselect", false);
		}
		// Containerization changes to hide containerWeght: End	

		//setting drop down with what selected
		var parentScreen = _iasUIUtils.getParentScreen(this, true);
		var parentScreenInitNS = _scScreenUtils.getModel(parentScreen, "getContainerList_Out_Src_Pg");
		var containerDetails = _scModelUtils.getStringValueFromPath("Page.Output.Containers.Container", parentScreenInitNS);
		var currentContainer = _scScreenUtils.getModel(this, "container_Src");
		var containerNo = _scModelUtils.getStringValueFromPath("Container.ContainerNo", currentContainer);

		if(!_scBaseUtils.isVoid(parentScreen.ContainerItemDetails)){
		for(var i = 0; i < parentScreen.ContainerItemDetails.length; i++) {
			var cDetails = parentScreen.ContainerItemDetails[i];

			var contNo = _scModelUtils.getStringValueFromPath("ContainerNo", cDetails);

			if(_scBaseUtils.equals(contNo, containerNo)) {
				var item = _scModelUtils.getStringValueFromPath("ContainerItem", cDetails);
				var actualWeight = _scModelUtils.getStringValueFromPath("ActualWeight", cDetails);

				if(!_scBaseUtils.isVoid(item)) {
					_scWidgetUtils.setValue(this, "extn_filteringselect", item, null);
					//BOPIS-1576_CR: BOPIS-1992 - save weight on UI changes - begin
					//_scWidgetUtils.setValue(this, "containerWeight", actualWeight, null);
					//BOPIS-1576_CR: BOPIS-1992 - save weight on UI changes - end
					//this.onDropDownChnageWithOut(item);
				}
				if(!_scBaseUtils.isVoid(item) && item!="VendorPackage") {
					_scWidgetUtils.enableWidget(this, "containerWeight");
					//this.onDropDownChnageWithOut(item);
				}
				var ddValues = _scScreenUtils.getModel(this, "extn_containerIDs");
				var allItems = _scModelUtils.getStringValueFromPath("ItemList.Item", ddValues);
				
				if(!_scBaseUtils.isVoid(allItems)) {
					for(var i = 0; i < allItems.length; i++) {
						if(_scBaseUtils.equals(allItems[i].ItemID, item)) {
						_scWidgetUtils.setValue(this, "extn_label", item + " - " + allItems[i].PrimaryInformation.Description , null);
						}
					}
				}
			}
		}
	}
	// fix for BOPIS-1256: Weight -> clear out default '0.00' in weight text box and keep it empty: Begin
    var containerActualWeight = _scModelUtils.getStringValueFromPath("Container.ActualWeight", containerModel);
    if(_scBaseUtils.equals(containerActualWeight, "0.00")) {
		//BOPIS-1576_CR: BOPIS-1992 - save weight on UI changes - begin
       // _scWidgetUtils.setValue(this, "containerWeight", "", false);
		//BOPIS-1576_CR: BOPIS-1992 - save weight on UI changes - end
    }
    // fix for BOPIS-1256: End
	
	//OMNI-73463,OMNI-73465, OMNI-73467
	var contRecomModel= _scScreenUtils.getModel(parent, "extn_ContainerRecomToggle");
	if(!_scBaseUtils.isVoid(contRecomModel)) {
		var commonCodeList = _scModelUtils.getModelListFromPath("CommonCodeList.CommonCode", contRecomModel);
		if(!_scBaseUtils.isVoid(commonCodeList)) {
		_scModelUtils.addModelToModelPath("Container.CommonCodeOutput", contRecomModel, currentContainer);
		_iasUIUtils.callApi(this, currentContainer, "extn_getAcadDirectPackLookup_ref", null);	
		}
	}			
	},

	onDropDownChnageWithOut: function(item) {
		var tM = _scScreenUtils.getTargetModel(this,"extn_dropDownTM", null);
		var tMValue = tM[Object.keys(tM)[0]];

		if(_scBaseUtils.isVoid(tMValue)) {
			_scWidgetUtils.setWidgetNonMandatory(this, "extn_filteringselect");
			_scWidgetUtils.setValue(this, "extn_filteringselect", item, null);
			_scWidgetUtils.setWidgetMandatory(this, "extn_filteringselect");
			var tM = _scScreenUtils.getTargetModel(this,"extn_dropDownTM", null);
			var tMValue = tM[Object.keys(tM)[0]];
		}

		var ddValues = _scScreenUtils.getModel(this, "extn_containerIDs");
		var allItems = _scModelUtils.getStringValueFromPath("ItemList.Item", ddValues);

		if(!_scBaseUtils.isVoid(allItems)) {
			for(var i = 0; i < allItems.length; i++) {
				if(_scBaseUtils.equals(allItems[i].ItemID, tMValue)) {
					_scWidgetUtils.setValue(this, "extn_label", tMValue + " - " + allItems[i].PrimaryInformation.Description , null);
					
					// this.ItemKeyForTracking = allItems[i].ItemKey;

					if(!_scBaseUtils.equals(tMValue, "VendorPackage")) {
						_scWidgetUtils.setWidgetNonMandatory(
		                    this, "extn_textfield");
						//_scWidgetUtils.setWidgetMandatory(this, "containerWeight")
						_scWidgetUtils.setValue(this, "containerWeight", "", null);
						_scWidgetUtils.enableWidget(this, "containerWeight");
						break;
					} else {
						_scWidgetUtils.setWidgetNonMandatory(
		                    this, "extn_textfield");
						//_scWidgetUtils.setWidgetNonMandatory(
		                  //  this, "containerWeight");
						  _scWidgetUtils.setValue(this, "containerWeight", "", null);
						_scWidgetUtils.disableWidget(this, "containerWeight");
						break;
					}
				}
			}
		}
	},
	//code changes to accommodate OMNI-23764 changes, Vendor Package is used instead of Hazmat Vendor Package 
	onDropDownChange: function() {
		var tM = _scScreenUtils.getTargetModel(this,"extn_dropDownTM", null);
		var tMValue = tM[Object.keys(tM)[0]];

		var shipmentDetails = _scScreenUtils.getModel(_iasUIUtils.getParentScreen(this, true), "getShipmentDetails_output");

		var shipmentType = _scModelUtils.getStringValueFromPath("Shipment.ShipmentType", shipmentDetails);

		var ddValues = _scScreenUtils.getModel(this, "extn_containerIDs");
		var allItems = _scModelUtils.getStringValueFromPath("ItemList.Item", ddValues);

		for(var i = 0; i < allItems.length; i++) {
			if(_scBaseUtils.equals(allItems[i].ItemID, tMValue)) {

				var containerType = allItems[i].PrimaryInformation.ItemType;

				var isShippingContainerAmmo = false;
			
				if(_scBaseUtils.equals(shipmentType, "AMMO") || _scBaseUtils.equals(shipmentType, "HAZMAT")) {
					isShippingContainerAmmo = true;
					
				}

			
				var isContainerTypeAmmo = false;
				if(_scBaseUtils.equals(containerType, "AmmoContainer")) {
					isContainerTypeAmmo = true;
				}
				

				if((isShippingContainerAmmo == isContainerTypeAmmo)||(isContainerTypeAmmo)) {
					_scWidgetUtils.setValue(this, "extn_label", tMValue + " - " + allItems[i].PrimaryInformation.Description , null);
					
					this.ItemKeyForTracking = allItems[i].ItemKey;

                    if(!isShippingContainerAmmo && isContainerTypeAmmo && !_scBaseUtils.equals(tMValue, "VendorPackage")) {
						_iasScreenUtils.showInfoMessageBoxWithOk(this, "Ammo containers can only be used to ship ammo.", "infoCallBack", null);
						_scWidgetUtils.disableWidget(this, "containerWeight");
						//OMNI-71047 Begin
						_scWidgetUtils.setValue(this, "extn_textfield", "", null);
						//OMNI-71047 End
						break;
					}
					else if( (!_scBaseUtils.equals(tMValue, "VendorPackage"))) {
						_scWidgetUtils.setWidgetNonMandatory(
		                    this, "extn_textfield");
						_scWidgetUtils.setWidgetMandatory(this, "containerWeight")
						_scWidgetUtils.enableWidget(this, "containerWeight");
						_scWidgetUtils.setFocusOnWidgetUsingUid(this, "containerWeight");
						break;
					} else {
						_scWidgetUtils.setWidgetNonMandatory(
		                    this, "extn_textfield");
						_scWidgetUtils.setWidgetNonMandatory(
		                    this, "containerWeight"); 
						 _scWidgetUtils.setValue(this, "containerWeight", "", null);
						_scWidgetUtils.disableWidget(this, "containerWeight");
						this.ownerScreen.ownerScreen.isDirtyCheckRequired = false;
						this.ownerScreen.isDirtyCheckRequired = false;
						break;
					}
				} else {
					if(isShippingContainerAmmo) {
						_iasScreenUtils.showInfoMessageBoxWithOk(this, "Select an ammo container as the order contains AMMO/HAZMAT item(s).", "infoCallBack", null);
						_scWidgetUtils.disableWidget(this, "containerWeight");
						//OMNI-72000 Begin
						_scWidgetUtils.setValue(this, "extn_textfield", "", null);
						//OMNI-72000 End
					} 
				}
			}
		}
		this.storeValue(tMValue);
		//OMNI-73463,OMNI-73465 - Starts
		this.storeManualContainerTypeValue(tMValue);
		//OMNI-73463,OMNI-73465 - Ends
		// _scModelUtils.setModel(this, "extn_dropDownSourceName", tMValue, null);
		// var parentScreenInitNS = _scScreenUtils.getModel(parentScreen, "getContainerList_Out_Src_Pg");
		// var containerDetails = _scModelUtils.getStringValueFromPath("Page.Output.Containers.Container", parentScreenInitNS);
		// var currentContainer = _scScreenUtils.getModel(this, "container_Src");
		// var containerNo = _scModelUtils.getStringValueFromPath("Container.ContainerNo", currentContainer);

		// for(var i = 0; i < containerDetails.length; i++) {
		// 	var cDetails = containerDetails[i];

		// 	var contNo = _scModelUtils.getStringValueFromPath("ContainerNo", cDetails);

		// 	if(_scBaseUtils.equals(contNo, containerNo)) {
		// 		// _scModelUtils.setStringValueAtModelPath("containerItem", tMValue ,cDetails)
		// 		if(!_scBaseUtils.isVoid(parentScreen.ContainerItemDetails)) {
		// 			for(var j = 0; j < parentScreen.ContainerItemDetails.length; j++) {
		// 				if(parentScreen.ContainerItemDetails[i].ContainerNo == contNo) {}
		// 			}
		// 		}
		// 	}
		// 		var data = {};
		// 		data.ContainerNo = contNo;
		// 		data.ContainerItem = tMValue;

		// 		parentScreen.ContainerItemDetails.push(data);
		// 	}
		

	},

	storeValue: function(tMValue) {
		var parentScreen = _iasUIUtils.getParentScreen(this, true);
		var currentContainer = _scScreenUtils.getModel(this, "container_Src");
		var containerNo = _scModelUtils.getStringValueFromPath("Container.ContainerNo", currentContainer);
		var bValue = false;
		if(!_scBaseUtils.isVoid(parentScreen.ContainerItemDetails)) {
			for(var i = 0; i < parentScreen.ContainerItemDetails.length; i++) {
				if(parentScreen.ContainerItemDetails[i].ContainerNo == containerNo) {
			 		parentScreen.ContainerItemDetails[i].ContainerItem = tMValue;
			 		if(parentScreen.ContainerItemDetails[i].ContainerItem == "VendorPackage") {
			 			parentScreen.ContainerItemDetails[i].ActualWeight = "0";
			 		} else {
			 			parentScreen.ContainerItemDetails[i].ActualWeight = currentContainer.Container.ActualWeight;
			 		}
			 		parentScreen.ContainerItemDetails[i].ContainerScm = currentContainer.Container.ContainerScm;
			 		parentScreen.ContainerItemDetails[i].ShipmentContainerKey = currentContainer.Container.ShipmentContainerKey;
			 		parentScreen.ContainerItemDetails[i].ShipmentKey = currentContainer.Container.Shipment.ShipmentKey;
					parentScreen.ContainerItemDetails[i].ContainerTypeKey = this.ItemKeyForTracking;
					parentScreen.ContainerItemDetails[i].ContainerType = tMValue;
			 		bValue = true;	
				}
			}
			if(!bValue) {
				var data = {};
 				data.ContainerNo = containerNo;
	 			data.ContainerItem = tMValue;
	 			if(data.ContainerItem == "VendorPackage") {
	 				data.ActualWeight = "0";
	 			} else {
	 				data.ActualWeight = currentContainer.Container.ActualWeight;
	 			}
				data.ContainerScm = currentContainer.Container.ContainerScm;
				data.ShipmentContainerKey = currentContainer.Container.ShipmentContainerKey;
				data.ShipmentKey = currentContainer.Container.Shipment.ShipmentKey;
				data.ContainerTypeKey = this.ItemKeyForTracking;
				data.ContainerType = tMValue;
				parentScreen.ContainerItemDetails.push(data);
			}
		} else {
			var data = {};
			data.ContainerNo = containerNo;
 			data.ContainerItem = tMValue;
 			if(data.ContainerItem == "VendorPackage") {
 				data.ActualWeight = "0";
 			} else {
 				data.ActualWeight = currentContainer.Container.ActualWeight;
 			}
			data.ContainerScm = currentContainer.Container.ContainerScm;
			data.ShipmentContainerKey = currentContainer.Container.ShipmentContainerKey;
			data.ShipmentKey = currentContainer.Container.Shipment.ShipmentKey;
			data.ContainerTypeKey = this.ItemKeyForTracking;
			data.ContainerType = tMValue;
			parentScreen.ContainerItemDetails.push(data);
		}
	},
	//OMNI-73463,OMNI-73465
	storeManualContainerTypeValue: function(tMValue) {
		var parentScreen = _iasUIUtils.getParentScreen(this, true);
		var currentContainer = _scScreenUtils.getModel(this, "container_Src");
		var containerNo = _scModelUtils.getStringValueFromPath("Container.ContainerNo", currentContainer);
		var bContValue = false;
		if(!_scBaseUtils.isVoid(parentScreen.ManualContainerTypeDetails)) {
			for(var i = 0; i < parentScreen.ManualContainerTypeDetails.length; i++) {
				if(parentScreen.ManualContainerTypeDetails[i].ContainerNo == containerNo) {
			 		parentScreen.ManualContainerTypeDetails[i].ManualContainerType = tMValue;
					bContValue = true;
				}
			}
			if(!bContValue) {
				var data = {};
				data.ContainerNo = containerNo;
				data.ManualContainerType = tMValue;
				parentScreen.ManualContainerTypeDetails.push(data);
			}
		}
		else if(_scBaseUtils.isVoid(parentScreen.ManualContainerTypeDetails)){
			var data = {};
 			data.ContainerNo = containerNo;
 			data.ManualContainerType = tMValue;
			parentScreen.ManualContainerTypeDetails.push(data);
		}
	},
	
	afterBehaviousMashup: function(event, bEvent, ctrl, args) {
		var mashupArrayList = _scModelUtils.getModelObjectFromPath("mashupArray", args);
            var mashupArrayListLength = Object.keys(mashupArrayList).length;
            for(var iCount = 0; iCount < mashupArrayListLength; iCount++) {
                var mashupArray = mashupArrayList[iCount];
                var mashupRefId = _scModelUtils.getModelObjectFromPath("mashupRefId", mashupArray);
                if(_scBaseUtils.equals(mashupRefId, "extn_getContainerDetail_ref")) {
                	var mashupOutputObject = _scModelUtils.getModelObjectFromPath("mashupRefOutput", mashupArray);
                	var Items = _scModelUtils.getStringValueFromPath("ItemList.Item", mashupOutputObject);
                	if(!_scBaseUtils.isVoid(Items)){
                		var itemid = _scModelUtils.getStringValueFromPath("ItemID", Items[0]);
                	}
                	if(_scBaseUtils.isVoid(Items)){
                		 _iasScreenUtils.showInfoMessageBoxWithOk(this, "Invalid container information", "infoCallBack", null);
                		 _scWidgetUtils.setValue(this, "extn_textfield", "", null);
                	} else if (_scBaseUtils.equals(_scModelUtils.getStringValueFromPath("IsShippingCntr", Items[0]), "N") && !_scBaseUtils.equals(itemid, "VendorPackage")) {
                		_iasScreenUtils.showInfoMessageBoxWithOk(this, "Please Enter/Scan a valid container.", "infoCallBack", null);
						_scWidgetUtils.setValue(this, "containerWeight", "", null);
						_scWidgetUtils.disableWidget(this, "containerWeight");
                	} else {
                	var shipmentDetails = _scScreenUtils.getModel(_iasUIUtils.getParentScreen(this, true), "getShipmentDetails_output");
					var shipmentType = _scModelUtils.getStringValueFromPath("Shipment.ShipmentType", shipmentDetails);

					var containerType = _scModelUtils.getStringValueFromPath("PrimaryInformation.ItemType", Items[0]);
                		
                		var isShippingContainerAmmo = false;
						if(_scBaseUtils.equals(shipmentType, "AMMO") || _scBaseUtils.equals(shipmentType, "HAZMAT")) {
							isShippingContainerAmmo = true;
						}

						var isContainerTypeAmmo = false;
						if(_scBaseUtils.equals(containerType, "AmmoContainer")) {
							isContainerTypeAmmo = true;
						}



						if(isShippingContainerAmmo == isContainerTypeAmmo) {

	                		var itemDesc = _scModelUtils.getStringValueFromPath("PrimaryInformation.Description", Items[0]);
	                		this.ItemKeyForTracking = _scModelUtils.getStringValueFromPath("ItemKey", Items[0]);

	                		var sDesc = itemid + " - " + itemDesc;
	                		this.storeValue(itemid);
							//OMNI-73463,OMNI-73465 - Starts
							this.storeManualContainerTypeValue(itemid);	
							//OMNI-73463,OMNI-73465	- Ends	
	                		if(!_scBaseUtils.equals(itemid, "VendorPackage")) {
		                		_scWidgetUtils.setWidgetNonMandatory(
		                    this, "extn_textfield");
								_scWidgetUtils.setValue(this, "containerWeight", "", null);
		                		_scWidgetUtils.enableWidget(this, "containerWeight");
		                		_scWidgetUtils.setValue(this, "extn_label", sDesc , null);
		                		_scWidgetUtils.setValue(this, "extn_textfield", "", null);
		                		_scWidgetUtils.setFocusOnWidgetUsingUid(this, "containerWeight");
		                		this.isFocusedOnWightOnEnter = true;
		                		//_scWidgetUtils.setWidgetMandatory(this, "containerWeight");
		                		//fix for BOPIS-1275:Container dropdown to be cleared when the container id is scanned/text is entered 
		                		var selectedDropDown = _scScreenUtils.getWidgetByUId(this, "extn_filteringselect").displayedValue;
		                		if(!_scBaseUtils.equals(selectedDropDown, itemid)) {
		                			_scWidgetUtils.setValue(this, "extn_filteringselect", "", null);
		                		}
	                		} else if(_scBaseUtils.equals(itemid, "VendorPackage")) {
	                			_scWidgetUtils.setWidgetNonMandatory(this, "extn_textfield");
	                			//_scWidgetUtils.setWidgetNonMandatory(
			                    //this, "containerWeight");
								_scWidgetUtils.setValue(this, "containerWeight", "", null);
								_scWidgetUtils.disableWidget(this, "containerWeight");
								this.ItemKeyForTracking = "";
								_scWidgetUtils.setValue(this, "extn_label", sDesc , null);
		                		_scWidgetUtils.setValue(this, "extn_textfield", "", null);
		                		//fix for BOPIS-1275:Container dropdown to be cleared when the container id is scanned/text is entered 
		                		var selectedDropDown = _scScreenUtils.getWidgetByUId(this, "extn_filteringselect").displayedValue;
		                		if(!_scBaseUtils.equals(selectedDropDown, itemid)) {
		                			_scWidgetUtils.setValue(this, "extn_filteringselect", "", null);
		                		}
	                		}
	                	} else {
	                		if(isShippingContainerAmmo) {
								_iasScreenUtils.showInfoMessageBoxWithOk(this, "Select an ammo container as the order contains AMMO/HAZMAT item(s).", "infoCallBack", null);
								_scWidgetUtils.setValue(this, "containerWeight", "", null);
								//OMNI-72000 Begin
								_scWidgetUtils.setValue(this, "extn_textfield", "", null);
								//OMNI-72000 End
								_scWidgetUtils.disableWidget(this, "containerWeight");
							} else {
								_iasScreenUtils.showInfoMessageBoxWithOk(this, "Ammo containers can only be used to ship ammo.", "infoCallBack", null);
								_scWidgetUtils.setValue(this, "containerWeight", "", null);
								//OMNI-71047 Begin
								_scWidgetUtils.setValue(this, "extn_textfield", "", null);
								//OMNI-71047 End
								_scWidgetUtils.disableWidget(this, "containerWeight");
							}
	                	}
                	}
                }
				//OMNI-73463,OMNI-73465
				else if (_scBaseUtils.equals(mashupRefId, "extn_getAcadDirectPackLookup_ref")) {
					var mashupOutputObject = _scModelUtils.getModelObjectFromPath("mashupRefOutput", mashupArray);
					if(!_scBaseUtils.isVoid(mashupOutputObject)){
						var finalRecomContainer = "";
						var recommendedContType = _scModelUtils.getStringValueFromPath("Container.RecommendedContainer", mashupOutputObject);	
						var containerNo = _scModelUtils.getStringValueFromPath("Container.ContainerNo", mashupOutputObject);										
						var parentScreen = _iasUIUtils.getParentScreen(this, true);
						var ddValues = _scScreenUtils.getModel(parentScreen, "extn_dropDownValues");
						var allItems = _scModelUtils.getStringValueFromPath("ItemList.Item", ddValues);
						var manualContainerTypeDetails = parentScreen.ManualContainerTypeDetails;
						if(!_scBaseUtils.isVoid(manualContainerTypeDetails)) {
							for(var j = 0; j < manualContainerTypeDetails.length; j++) {
								var contRecomArrContainerNo = _scModelUtils.getStringValueFromPath("ContainerNo", manualContainerTypeDetails[j]);
								if(_scBaseUtils.equals(contRecomArrContainerNo,containerNo)) {
									var contRecomArrContainerType = _scModelUtils.getStringValueFromPath("ManualContainerType", manualContainerTypeDetails[j]);
									finalRecomContainer = contRecomArrContainerType;								
								}
							}
						}
						if(_scBaseUtils.isVoid(finalRecomContainer)){
							finalRecomContainer = recommendedContType;
						}
						if (!_scBaseUtils.isVoid(allItems)) {
							for (var i = 0; i < allItems.length; i++) {
								if (_scBaseUtils.equals(allItems[i].ItemID, finalRecomContainer)) {
									_scWidgetUtils.setValue(this, "extn_label", finalRecomContainer + " - " + allItems[i].PrimaryInformation.Description, null);
									_scWidgetUtils.setWidgetMandatory(this, "containerWeight")
									_scWidgetUtils.enableWidget(this, "containerWeight");
									_scWidgetUtils.setFocusOnWidgetUsingUid(this, "containerWeight");
									this.storeValue(finalRecomContainer);
									_scWidgetUtils.setValue(this, "extn_filteringselect", "", null);
									break;
								} else if (_scBaseUtils.isVoid(finalRecomContainer)) {
									_scWidgetUtils.setValue(this, "extn_label", "", null);
									_scWidgetUtils.setValue(this, "extn_filteringselect", "", null);
									_scWidgetUtils.disableWidget(this, "containerWeight");
								}
							}
						}
					}
				}              	
            	
            }
	},

    getTrackingNoAndPrintLabel: function(
    event, bEvent, ctrl, args) {
        var changeShipment_input = null;
        var packageWeight = null;
        changeShipment_input = _scBaseUtils.getTargetModel(
        this, "changeShipment_input", null);
        packageWeight = _scModelUtils.getStringValueFromPath("Shipment.Containers.Container.ActualWeight", changeShipment_input);
        var numberPackageWeight = 0;
        numberPackageWeight = _scModelUtils.getNumberValueFromPath("Shipment.Containers.Container.ActualWeight", changeShipment_input);
        if (
        _scBaseUtils.equals(
        numberPackageWeight, 0) || _scBaseUtils.isVoid(
        packageWeight)) {
        	var tM = _scScreenUtils.getWidgetByUId(this, "extn_label").value;
	        var tMValue = tM.split('-');
	        var tMValue = tMValue[0].trim();
        	if(_scBaseUtils.equals(tMValue, "VendorPackage")) {
        		this.getTrackingNoAndPrintLabelForContainer();
        	} else {
	            var msg = null;
	            msg = _scScreenUtils.getString(
	            this, "containerNotweighed");
	            _iasBaseTemplateUtils.showMessage(
	            this, msg, "error", null);
        	}
        } else {
            this.getTrackingNoAndPrintLabelForContainer();
        }
    },

	getTrackingNoAndPrintLabelForContainer: function() {
        var itemIdAndDesc = _scScreenUtils.getWidgetByUId(this, "extn_label").value;
        if (_scBaseUtils.isVoid(itemIdAndDesc)) {
        	_iasBaseTemplateUtils.showMessage(this, "Please scan or select conatainer Id from drop down", "error", null);
        }
        else {
        	var itemId = itemIdAndDesc.split('-')[0].trim();
        	_scUserprefs.setProperty("ItemId", itemId);
	        var targetModel = null;
	        targetModel = _scBaseUtils.getTargetModel(
	        this, "getTrackingNoAndPrintLabel_input", null);

	       	var tM = _scScreenUtils.getWidgetByUId(this, "extn_label").value;
	        var tMValue = tM.split('-');

	        var tMValue = tMValue[0].trim();

	        _scModelUtils.setStringValueAtModelPath("Container.ContainerType", tMValue, targetModel);
	        _scModelUtils.setStringValueAtModelPath("Container.ContainerTypeKey", this.ItemKeyForTracking, targetModel);

	        var weight = _scScreenUtils.getWidgetByUId(this, "containerWeight").value;

	        _scModelUtils.setStringValueAtModelPath("Container.ContainerGrossWeight", weight, targetModel);

	        var eventDefn = null;
	        var blankModel = null;
	        eventDefn = {};
	        blankModel = {};
	        _scBaseUtils.setAttributeValue("argumentList", blankModel, eventDefn);
	        _scBaseUtils.setAttributeValue("argumentList.getTrackingNoAndPrintLabel_input", targetModel, eventDefn);
	        _scBaseUtils.setAttributeValue("argumentList.getShipmentContainerDetails_input", _scBaseUtils.getTargetModel(
	        this, "getShipmentContainerDetails_input", null), eventDefn);
	        _scEventUtils.fireEventToParent(
	        this, "getTrackingNoAndPrintLabel", eventDefn);
	    }
    },
	
	handleShowOrHideWidgets: function() {
			
			/* BOPIS 1571 */
			var fs = this.getWidgetByUId("extn_filteringselect");
			this._addReadOnlyState();
			dConnect.connect(fs, "openDropDown", this, "_addReadOnlyState");
			dConnect.connect(fs, "closeDropDown", this, "_addReadOnlyState");
			
			//BOPIS-1576_CR: BOPIS-1992 - save weight on UI changes - begin
			var parentScreen = _iasUIUtils.getParentScreen(this, true);
			var weightModel = _scScreenUtils.getModel(parentScreen, "ExtnWeightModel");

			var currentContainer = _scScreenUtils.getModel(this, "container_Src");
			var currentContKey = _scModelUtils.getStringValueFromPath("Container.ShipmentContainerKey", currentContainer);

			if(!_scBaseUtils.isVoid(weightModel)){
				for(var i = 0; i < weightModel.ContainerWeights.ContainerWeight.length; i++) {

					var containerKey = weightModel.ContainerWeights.ContainerWeight[i].ShipmentContainerKey;

					if(_scBaseUtils.equals(currentContKey, containerKey)) {
						var containerWeight = weightModel.ContainerWeights.ContainerWeight[i].ContainerWeight;

						if(!_scBaseUtils.isVoid(containerWeight)) {
							_scWidgetUtils.setValue(this, "containerWeight", containerWeight, null);
							
							//BOPIS-1576_CR: BOPIS-1992 - saving the 1256 fix
							// fix for BOPIS-1256: Weight -> clear out default '0.00' in weight text box and keep it empty: Begin
							
							if(_scBaseUtils.equals(containerWeight, 0)) {
								
								_scWidgetUtils.setValue(this, "containerWeight", "", false);
								
							}
							// fix for BOPIS-1256: End
							//BOPIS-1576_CR: BOPIS-1992 - saving the 1256 fix
							
						}
						//BOPIS-1576_CR: BOPIS-2006 - Blanking out container weight if no value entered before - begin
						else if(_scBaseUtils.isVoid(containerWeight)){
							_scWidgetUtils.setValue(this, "containerWeight", "", false);
						}
						//BOPIS-1576_CR: BOPIS-2006 - Blanking out container weight if no value entered before - end
					}
				}
			}
			//BOPIS-1576_CR: BOPIS-2006 - Blanking out container weight if no value entered before - begin
			else if(_scBaseUtils.isVoid(weightModel)){
				_scWidgetUtils.setValue(this, "containerWeight", "", false);
			}
			//BOPIS-1576_CR: BOPIS-2006 - Blanking out container weight if no value entered before - end
			
			//BOPIS-1576_CR: BOPIS-1992 - save weight on UI changes - end

            _scScreenUtils.clearScreen(
            this);
            if (
            _iasContextUtils.isMobileContainer()) {
                _scWidgetUtils.showWidget(
                this, "lbl_containerWeightUOM", false, "");
                var cssClass = null;
                cssClass = [];
                cssClass.push("a11yHiddenLabelMandatory");
                _scWidgetUtils.addClass(
                this, "containerWeight", cssClass);
            }
            _scScreenUtils.isDirty(
            _iasUIUtils.getParentScreen(
            this, true), null, true);
            if (
            _scBaseUtils.equals(
            _wscContainerPackUtils.getScacIntegrationReqd(
            this), "N")) {
                _scWidgetUtils.hideWidget(
                this, "printLink", false);
                _scWidgetUtils.hideWidget(
                this, "lbl_trackingNo", false);
                var containerModel = null;
                var packageWeight = null;
                containerModel = _scScreenUtils.getModel(
                this, "container_Src");
                packageWeight = _scModelUtils.getStringValueFromPath("Container.ActualWeight", containerModel);
                _scWidgetUtils.setValue(this, "containerWeight", "", null);
                var numberPackageWeight = 0;
                numberPackageWeight = _scModelUtils.getNumberValueFromPath("Container.ActualWeight", containerModel);
                if (
                _scBaseUtils.equals(
                numberPackageWeight, 0) || _scBaseUtils.isVoid(
                packageWeight)) {
                    _scWidgetUtils.hideWidget(
                    this, "imgPackComplete", false);
                    _scWidgetUtils.hideWidget(
                    this, "lblReady", false);
                } else {
                    _scWidgetUtils.showWidget(
                    this, "imgPackComplete", false, "");
                    _scWidgetUtils.showWidget(
                    this, "lblReady", false, "");
                }
            } else {
                var containerModel = null;
                var trackingNo = null;
                var containerModel = null;
                var packageWeight = null;
                containerModel = _scScreenUtils.getModel(
                this, "container_Src");
                var containerNo = containerModel.Container.ContainerNo;
                var parentScreen = _iasUIUtils.getParentScreen(this, true);
                var parentScreenContainerDetails = parentScreen.ContainerItemDetails;
                for(var i=0;i<parentScreenContainerDetails.length;i++)
                {
                        var containerNoNew = parentScreenContainerDetails[i].ContainerNo;
                        var containerItem = parentScreenContainerDetails[i].ContainerItem;
                        if(_scBaseUtils.equals(containerNo, containerNoNew) && _scBaseUtils.equals(containerItem, "VendorPackage")  )
                        {
                                _scWidgetUtils.setValue(this, "containerWeight", "", null); 
                        }

                }
                trackingNo = _scModelUtils.getStringValueFromPath("Container.TrackingNo", containerModel);
                if (!(
                _scBaseUtils.isVoid(
                trackingNo))) {
                    _scWidgetUtils.disableWidget(
                    this, "printLink", false);
                    _scWidgetUtils.showWidget(
                    this, "imgPackComplete", false, "");
                    _scWidgetUtils.showWidget(
                    this, "lblReady", false, "");
                    if (!(
                    _scWidgetUtils.isWidgetVisible(
                    this, "lbl_trackingNo"))) {
                        _scWidgetUtils.showWidget(
                        this, "lbl_trackingNo", false, "");
                    }
                } else {
                    _scWidgetUtils.enableWidget(
                    this, "printLink", false, null);
                    _scWidgetUtils.hideWidget(
                    this, "imgPackComplete", false);
                    _scWidgetUtils.hideWidget(
                    this, "lblReady", false);
                }
            }
        },

		_removeReadOnlyState: function() {
			var fs = this.getWidgetByUId("extn_filteringselect");
			dDomAttr.remove(fs.textbox, "readonly");
		},
		_addReadOnlyState: function() {
			var fs = this.getWidgetByUId("extn_filteringselect");
			dDomAttr.set(fs.textbox, "readonly", true);
		},
		/*BOPIS -1571 END */
		
		//BOPIS-1576: Remove manual "Enter" hit after container weight input - begin
		extn_beforeSaveContainerWeightOnEnter: function(event, bEvent, ctrl, args) {
			_scEventUtils.stopEvent(bEvent);
		}
		//BOPIS-1576: Remove manual "Enter" hit after container weight input - end
});
});

