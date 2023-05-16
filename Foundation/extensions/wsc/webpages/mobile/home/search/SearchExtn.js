
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/mobile/home/search/SearchExtnUI",
	"scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils",
	"scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!dojo/_base/connect",
	"scbase/loader!dojo/dom-attr","scbase/loader!ias/utils/ContextUtils","scbase/loader!wsc/mobile/home/utils/MobileHomeUtils","scbase/loader!ias/utils/UIUtils","scbase/loader!ias/utils/EventUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnSearchExtnUI
					
			,
				_scWidgetUtils
			,
				_scScreenUtils
			,
				_scBaseUtils
			,
				_scModelUtils
			,
				dConnect
			, 
				dDomAttr
			,
				_iasContextUtils
			,
				_wscMobileHomeUtils
			,
				_iasUIUtils
			,
				_iasEventUtils
){ 
	return _dojodeclare("extn.mobile.home.search.SearchExtn", [_extnSearchExtnUI],{
	// custom code here
	//Barcode Scan-able input field set focus on Search screen init - starts
	extn_afterScreenLoad: function(event, bEvent, ctrl, args) {	
        _scWidgetUtils.setFocusOnWidgetUsingUid(this, "extn_ship_or_order_no");
		//OMNI-71289 Pick Up Order Grouping - Flag Based START
		_scWidgetUtils.hideWidget(this, "extn_relatedOrders_checkbox");	
		//OMNI-71289 Pick Up Order Grouping - Flag Based  END
    },
    //Barcode Scan-able input field set focus on Search screen init - ends
    
	/* BOPIS 1571 */
	initializeScreen: function(
        event, bEvent, ctrl, args) {
		     
			var fs = this.getWidgetByUId("filteringSelectCarrier");
			this._addReadOnlyState();
			dConnect.connect(fs, "openDropDown", this, "_addReadOnlyState");
			dConnect.connect(fs, "closeDropDown", this, "_addReadOnlyState");
			
            var savedSearchCriteriaModel = null;
            savedSearchCriteriaModel = _scScreenUtils.getInitialInputData(
            this);
            if (!(
            _scBaseUtils.isVoid(
            savedSearchCriteriaModel))) {
				//OMNI-71126 Pick Up Order Grouping - Flag Based START
				var checkboxChecked = savedSearchCriteriaModel.Shipment.ShowRelatedOrders;
				if (!(_scBaseUtils.isVoid(savedSearchCriteriaModel))) {
					this.disableWidgets(event, bEvent, ctrl, args);
				}
				//OMNI-71126 Pick Up Order Grouping - Flag Based END
                _scScreenUtils.setModel(
                this, "SavedSearchCriteria", savedSearchCriteriaModel, null);
                if (
                _scBaseUtils.isVoid(
                _scModelUtils.getStringValueFromPath("Shipment.SCAC", savedSearchCriteriaModel))) {
                    this.setAllOptionOnFilteringSelect(
                    savedSearchCriteriaModel);
                }
            } else {
                this.setAllOptionOnFilteringSelect(
                savedSearchCriteriaModel);
            }
            var orderByAttribute = null;
            orderByAttribute = _scModelUtils.getStringValueFromPath("Shipment.OrderByAttribute", savedSearchCriteriaModel);
            if (!(
            _scBaseUtils.isVoid(
            orderByAttribute))) {
                this.orderByAttribute = orderByAttribute;
            }
			//KER-16059
			var optionsBean = null;
            optionsBean = {};
            _scBaseUtils.setAttributeValue("shouldCallInitApi", true, optionsBean);
            _scScreenUtils.showChildScreen(
            this, "advancedSearchPanel", null, "", optionsBean, null);
			//OMNI-71126 Pick Up Order Grouping - Flag Based END		
			var getCommonCodeInput = {};				
			getCommonCodeInput = _scModelUtils.createModelObjectFromKey("CommonCode", getCommonCodeInput);
			_scModelUtils.setStringValueAtModelPath("CommonCode.CodeType", "TGL_RCP_WEB_SOM_UI" , getCommonCodeInput);
		//	_scModelUtils.setStringValueAtModelPath("CommonCode.CodeValue", "ENABLE_SHOW_RELATED_ORDERS" , getCommonCodeInput);
			_iasUIUtils.callApi(this, getCommonCodeInput, "extn_getFlagToDisableAddQtyAndReadOnlyScannedQty", null); 
			//OMNI-71126 Pick Up Order Grouping - Flag Based END
			
	        },

		_removeReadOnlyState: function() {
			var fs = this.getWidgetByUId("filteringSelectCarrier");
			dDomAttr.remove(fs.textbox, "readonly");
		},
		_addReadOnlyState: function() {
			var fs = this.getWidgetByUId("filteringSelectCarrier");
			dDomAttr.set(fs.textbox, "readonly", true);
		},
		/*BOPIS -1571 END */
		//OMNI-71126 Pick Up Order Grouping - Flag Based START
			handleMashupOutput: function(mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
				  if(_scBaseUtils.equals(mashupRefId,"extn_getFlagToDisableAddQtyAndReadOnlyScannedQty")) {
                 var CommonCodeList = _scModelUtils.getStringValueFromPath("CommonCodeList.CommonCode", modelOutput);
                    if(!(_scBaseUtils.isVoid(CommonCodeList))) {
                       for(var i in CommonCodeList){
                       var sCodeValue = _scModelUtils.getStringValueFromPath("CodeValue", CommonCodeList[i]);
                    if(_scBaseUtils.equals(sCodeValue,"ENABLE_SHOW_RELATED_ORDERS")) {
                       var enableShowRelatedOrder = modelOutput.CommonCodeList.CommonCode[i].CodeShortDescription;
                        if(!(_scBaseUtils.isVoid(enableShowRelatedOrder)) && (_scBaseUtils.equals(enableShowRelatedOrder,"Y"))) {
                            _scWidgetUtils.showWidget(this, "extn_relatedOrders_checkbox");                        
                        }                    
                }    if(_scBaseUtils.equals(sCodeValue,"ENABLE_SEARCH_BY_SKU")) {
                       var enableSkuSearch = modelOutput.CommonCodeList.CommonCode[i].CodeShortDescription;
                        if(!(_scBaseUtils.isVoid(enableSkuSearch)) && (_scBaseUtils.equals(enableSkuSearch,"N"))) {
                     _scWidgetUtils.hideWidget(this, "extn_ScanProductIDTextBox");                        
                  } 
				  } 
				  }
                } 
				}
			},

			disableWidgets : function(event, bEvent, ctrl, args){
			var checkboxChecked = null;
			var inputModel = null;
			var savedSearchCriteriaModel = null;
	        savedSearchCriteriaModel = _scScreenUtils.getInitialInputData(this);
			inputModel = _scBaseUtils.getTargetModel(this, "getShipmentSearch_input", null);
	        if (!(_scBaseUtils.isVoid(savedSearchCriteriaModel) && _scBaseUtils.isVoid(inputModel))){
				checkboxChecked = inputModel.Shipment.ShowRelatedOrders ;
			}else if (!(_scBaseUtils.isVoid(savedSearchCriteriaModel))) {
				checkboxChecked = savedSearchCriteriaModel.Shipment.ShowRelatedOrders;
			}else{			
				checkboxChecked = inputModel.Shipment.ShowRelatedOrders ;
			}
			 var childAdvancedScreenObj = _scScreenUtils.getChildScreen(this, "advancedSearchPanel");
			 if(!(_scBaseUtils.isVoid(checkboxChecked)) && (_scBaseUtils.equals(checkboxChecked,"Y"))){
				_scWidgetUtils.disableWidget(childAdvancedScreenObj, "txt_SearchCustLastName", true);
				_scWidgetUtils.disableWidget(childAdvancedScreenObj, "txt_SearchCustFirstName", true);
				_scWidgetUtils.disableWidget(childAdvancedScreenObj, "txt_SearchCustPhoneNo", true);
				_scWidgetUtils.disableWidget(childAdvancedScreenObj, "filteringSelectOrderStatus", true);
				_scWidgetUtils.disableWidget(childAdvancedScreenObj, "filteringSelectFullFillmentType", true);
				_scWidgetUtils.disableWidget(childAdvancedScreenObj, "includeOrdersPicked", true);
				_scWidgetUtils.disableWidget(this, "filteringSelectCarrier");	
				_scWidgetUtils.disableWidget(this, "extn_ScanProductIDTextBox");
			}else{
				_scWidgetUtils.enableWidget(childAdvancedScreenObj, "txt_SearchCustLastName", true);
				_scWidgetUtils.enableWidget(childAdvancedScreenObj, "txt_SearchCustFirstName", true);
				_scWidgetUtils.enableWidget(childAdvancedScreenObj, "txt_SearchCustPhoneNo", true);
				_scWidgetUtils.enableWidget(childAdvancedScreenObj, "filteringSelectOrderStatus", true);
				_scWidgetUtils.enableWidget(childAdvancedScreenObj, "filteringSelectFullFillmentType", true);
				_scWidgetUtils.enableWidget(childAdvancedScreenObj, "includeOrdersPicked", true);
				_scWidgetUtils.enableWidget(this, "filteringSelectCarrier");
				_scWidgetUtils.enableWidget(this, "extn_ScanProductIDTextBox");
			}
			
		},
		//OMNI-71126 Pick Up Order Grouping - Flag Based END
		
		scanProductOnEnter : function(event, bEvent, ctrl, args){
			var itemID = null;
			var inputModelsku = null;
			inputModelsku = _scBaseUtils.getTargetModel(this, "getShipmentSearch_input", null);
	        if (!(_scBaseUtils.isVoid(inputModelsku))){
				itemID = inputModelsku.Shipment.itemID;
			}
			 var childAdvancedScreenObj = _scScreenUtils.getChildScreen(this, "advancedSearchPanel");
			 if(!(_scBaseUtils.isVoid(itemID))){
				
				_scWidgetUtils.disableWidget(this,"extn_relatedOrders_checkbox", true);
				_scWidgetUtils.disableWidget(this,"extn_ship_or_order_no", true);
				_scWidgetUtils.disableWidget(childAdvancedScreenObj, "txt_SearchCustLastName", true);
				_scWidgetUtils.disableWidget(childAdvancedScreenObj, "txt_SearchCustEmail", true);
				_scWidgetUtils.disableWidget(childAdvancedScreenObj, "txt_SearchCustFirstName", true);
				_scWidgetUtils.disableWidget(childAdvancedScreenObj, "txt_SearchCustPhoneNo", true);
				_scWidgetUtils.disableWidget(childAdvancedScreenObj, "filteringSelectOrderStatus", true);
				_scWidgetUtils.disableWidget(childAdvancedScreenObj, "filteringSelectFullFillmentType", true);
				_scWidgetUtils.disableWidget(childAdvancedScreenObj, "includeOrdersPicked", true);
				_scWidgetUtils.disableWidget(this, "filteringSelectCarrier");			
			}else if(_scBaseUtils.isVoid(itemID)){
				_scWidgetUtils.enableWidget(this, "extn_relatedOrders_checkbox", true);
				_scWidgetUtils.enableWidget(this, "extn_ship_or_order_no", true);
				_scWidgetUtils.enableWidget(childAdvancedScreenObj, "txt_SearchCustLastName", true);
				_scWidgetUtils.enableWidget(childAdvancedScreenObj, "txt_SearchCustEmail", true);
				_scWidgetUtils.enableWidget(childAdvancedScreenObj, "txt_SearchCustFirstName", true);
				_scWidgetUtils.enableWidget(childAdvancedScreenObj, "txt_SearchCustPhoneNo", true);
				_scWidgetUtils.enableWidget(childAdvancedScreenObj, "filteringSelectOrderStatus", true);
				_scWidgetUtils.enableWidget(childAdvancedScreenObj, "filteringSelectFullFillmentType", true);
				_scWidgetUtils.enableWidget(childAdvancedScreenObj, "includeOrdersPicked", true);
				_scWidgetUtils.enableWidget(this, "filteringSelectCarrier");
			}
			if(_iasEventUtils.isEnterPressed(event)) {
                this.searchOrders();
            }
		},
		
    //OMNI-6586: Start
		searchOrders: function(event, bEvent, ctrl, args) {

            if (this.isSearchCriteriaValid()) {

                var shipmentSearchCriteriaModel = null;
                var includeOtherStoresModel = null;
                shipmentSearchCriteriaModel = _scBaseUtils.getTargetModel(this, "getShipmentSearch_input", null);
				//OMNI-71126 Pick Up Order Grouping - Pick Up Order Grouping Order Search TC70  START
				var showRelatedOrders = null;
				var orderNo = null;
				var emailID = null
				var invalidSearch = "N";
				var itemID = null;
				showRelatedOrders = shipmentSearchCriteriaModel.Shipment.ShowRelatedOrders;
				orderNo = shipmentSearchCriteriaModel.Shipment.refNo;
				itemID = shipmentSearchCriteriaModel.Shipment.itemID;
				if(!_scBaseUtils.isVoid(shipmentSearchCriteriaModel.Shipment.BillToAddress)){
					emailID = shipmentSearchCriteriaModel.Shipment.BillToAddress.EMailID;
				}
				 if(!(_scBaseUtils.isVoid(showRelatedOrders)) && (_scBaseUtils.equals(showRelatedOrders,"Y"))){
					  if((_scBaseUtils.isVoid(orderNo)) && (_scBaseUtils.isVoid(emailID))){
						  invalidSearch = "Y";											 
					}
				 }
				 
				 if(_scBaseUtils.equals(invalidSearch,"Y")){
					_scScreenUtils.showErrorMessageBox(this, "Please enter either Order number or Email ID", null, null, null);
				 }else{
				//OMNI-71126 Pick Up Order Grouping - Pick Up Order Grouping Order Search TC70  END
					if (_scBaseUtils.isVoid(shipmentSearchCriteriaModel)) {
						shipmentSearchCriteriaModel = _scModelUtils.createModelObjectFromKey("Shipment", shipmentSearchCriteriaModel);
					}

					includeOtherStoresModel = _scBaseUtils.getTargetModel(this, "IncludeOrdersPickedInOtherStore", null);

					if (_scBaseUtils.equals(_scModelUtils.getStringValueFromPath("isChecked", includeOtherStoresModel), "Y")) {
						_scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", " ", shipmentSearchCriteriaModel);
					}else {
						_scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", _iasContextUtils.getFromContext("CurrentStore"), shipmentSearchCriteriaModel);
					}

					_scModelUtils.setStringValueAtModelPath("Shipment.OrderByAttribute", this.orderByAttribute, shipmentSearchCriteriaModel);
					_scModelUtils.setStringValueAtModelPath("Shipment.SearchType", "OOBSearch", shipmentSearchCriteriaModel);

					_scBaseUtils.removeBlankAttributes(shipmentSearchCriteriaModel);
					_iasContextUtils.addToContext("SearchCriteria", shipmentSearchCriteriaModel);
					_wscMobileHomeUtils.openScreenWithInputData("wsc.mobile.home.search.SearchResult", shipmentSearchCriteriaModel, "wsc.mobile.editors.MobileEditor");
                }
            } else {
                _iasBaseTemplateUtils.showMessage(
                this, "InvalidSearchCriteria", "error", null);
            }
        }
        //OMNI-6586: End
});
});

