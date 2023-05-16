scDefine([
    "dojo/text!./templates/STSOrderSearchScreen.html",
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
    "scbase/loader!dojo/dom-attr"
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
    dDomAttr


) {
    return _dojodeclare("extn.mobile.home.STSOrderSearch.OrderSearch.STSOrderSearchScreen", [_scScreen], {
        templateString: templateText,
        uId: "STSOrderSearchScreen",
        packageName: "extn.mobile.home.STSOrderSearch.OrderSearch",
        className: "STSOrderSearchScreen",
        title: "Title_STSOrderSearch",
        screen_description: "STS Order Search Screen",

        namespaces: {

            targetBindingNamespaces: [{
                description: 'The input to the getContainerList mashup.',
                value: 'getShipmentSearch_input'
            }],
            sourceBindingNamespaces: [{
                description: "The details of the container",
                value: 'getShipmentStatusList_output'
            }]

        },

        subscribers: {
            local: [

                {
                    eventId: 'afterScreenLoad',
                    sequence: '25',
                    description: 'Subscriber for after the screen loads',
                    handler: {
                        //methodName: "updateEditorHeader"
                    }
                },
                {
                    eventId: 'afterScreenInit',
                    sequence: '32',
                    handler: {
                        methodName: "initializeScreen"
                    }
                },
                {
                    eventId: 'afterScreenLoad',
                    sequence: '32',
                    handler: {
                        methodName: "extn_afterScreenLoad"
                    }
                },

                {
                    eventId: 'extn_btnSearch_onClick',
                    sequence: '32',
                    handler: {
                        methodName: "searchSTSOrders"
                    }
                },
                {
                    eventId: 'txt_SearchContiainerNo_onKeyUp',
                    sequence: '32',
                    handler: {
                        methodName: "extn_searchSTSOrders_OnEnter"
                    }
                },
                {
                    eventId: 'txt_SearchOrderNo_onKeyUp',
                    sequence: '32',
                    handler: {
                        methodName: "extn_searchSTSOrders_OnEnter"
                    }
                },
                {
                    eventId: 'txt_SearchCustLastName_onKeyUp',
                    sequence: '32',
                    handler: {
                        methodName: "extn_searchSTSOrders_OnEnter"
                    }
                },
                {
                    eventId: 'txt_SearchCustFirstName_onKeyUp',
                    sequence: '32',
                    handler: {
                        methodName: "extn_searchSTSOrders_OnEnter"
                    }
                },
                {
                    eventId: 'txt_SearchCustEmail_onKeyUp',
                    sequence: '32',
                    handler: {
                        methodName: "extn_searchSTSOrders_OnEnter"
                    }
                },
                {
                    eventId: 'txt_SearchCustPhoneNo_onKeyUp',
                    sequence: '32',
                    handler: {
                        methodName: "extn_searchSTSOrders_OnEnter"
                    }
                },
		            {
                    eventId: 'filteringSelectOrderStatus_onKeyUp',
                    sequence: '32',
                    handler: {
                        methodName: "extn_searchSTSOrders_OnEnter"
                    }
                },


            ]
        },

        setInitialized: function(
            event, bEvent, ctrl, args) {
            this.isScreeninitialized = true;
        },

        extn_afterScreenLoad: function(event, bEvent, ctrl, args) {
            _scWidgetUtils.setFocusOnWidgetUsingUid(this, "txt_SearchContiainerNo");
        },


        searchSTSOrders: function(event, bEvent, ctrl, args) {
            var ShipmentCriteriaModel = _scBaseUtils.getNewModelInstance();
            var targetModel = _scBaseUtils.getTargetModel(this, "getShipmentSearch_input");
            var currentNode = _iasContextUtils.getFromContext("CurrentStore");
            if (!_scBaseUtils.isVoid(targetModel)) {
                // Container search
                var strContainerNo = _scModelUtils.getStringValueFromPath("Shipment.Containers.Container.ContainerNo", targetModel);
                if (!_scBaseUtils.isVoid(strContainerNo)) {
                    _scModelUtils.setStringValueAtModelPath("Shipment.ReceivingNode", currentNode, targetModel);
                    _scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod", "SHP", targetModel);
                }
                // Status Check
                var strStatus = _scModelUtils.getStringValueFromPath("Shipment.Status", targetModel);
               if ((!_scBaseUtils.isVoid(strStatus)) && (_scBaseUtils.isVoid(strContainerNo))) {
                    if (_scBaseUtils.equals(strStatus, '1400_SHP') || _scBaseUtils.equals(strStatus, '1600') || _scBaseUtils.equals(strStatus, '1100.70.06.30')) {
                        _scModelUtils.setStringValueAtModelPath("Shipment.ReceivingNode", currentNode, targetModel);
                        _scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod", "SHP", targetModel);
                    } else {
                        _scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", currentNode, targetModel);
                        _scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod", "PICK", targetModel);

                    }
                }
                else if(_scBaseUtils.isVoid(strStatus) && _scBaseUtils.isVoid(strContainerNo))
                {
                  _scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", currentNode, targetModel);  
                }
                
                var customerDetails = _scModelUtils.getStringValueFromPath("Shipment.BillToAddress", targetModel);
                if((!_scBaseUtils.isVoid(strContainerNo)) && (!_scBaseUtils.isVoid(customerDetails))){
                     _iasBaseTemplateUtils.showMessage(this, "Invalid Search Criteria. Please Search with Sales Order number and Customer Details", "error", null);
                     return true;
                }
		// Order search
                var strOrderNo= _scModelUtils.getStringValueFromPath("Shipment.refNo", targetModel);
                if(!_scBaseUtils.isVoid(strOrderNo))
		{
                   _scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", currentNode, targetModel);	
                }

                if ((!_scBaseUtils.isVoid(strContainerNo)) && (!_scBaseUtils.isVoid(strOrderNo)))
                {
                     _iasBaseTemplateUtils.showMessage(this, "Invalid Search Criteria. Please Search either with Container number or Order number", "error", null);
                     return true;
	 
                }
                
                //OMNI-101572, OMNI-101645 Start
				var strNodeType = _scModelUtils.getStringValueFromPath("Shipment.NodeType", targetModel);
				 if ((!_scBaseUtils.isVoid(strNodeType)) && (!_scBaseUtils.isVoid(strStatus))) {
                     if(!((_scBaseUtils.equals(strStatus, '1400_SHP')) || (_scBaseUtils.equals(strStatus, '1600')) || (_scBaseUtils.equals(strStatus, '1100.70.06.30')))) {
						 if (_scBaseUtils.equals(strNodeType, 'Store')) {
                        _scModelUtils.setStringValueAtModelPath("Shipment.ProcureFromNodeQryType", "ISNULL", targetModel);
                        }
						else if (_scBaseUtils.equals(strNodeType, 'SharedInventoryDC')) {
                        _scModelUtils.setStringValueAtModelPath("Shipment.ProcureFromNodeQryType", "NOTNULL", targetModel);
						_scModelUtils.setStringValueAtModelPath("Shipment.NodeType", "Store", targetModel);
                        }
                }
				}
				if ((!_scBaseUtils.isVoid(strNodeType)) && (_scBaseUtils.isVoid(strStatus)))
                {
                     _iasBaseTemplateUtils.showMessage(this, "Invalid Search Criteria: Please Select an Order Status before selecting a Shipment Source", "error", null);
                     return true;

                }
			   //OMNI-101572, OMNI-101645 End
               
                _scModelUtils.setStringValueAtModelPath("Shipment.OrderByAttribute", "ExpectedShipmentDate-Y", targetModel);
                _scModelUtils.setStringValueAtModelPath("Shipment.SearchType", "STSOrderSearch", targetModel);
                _iasContextUtils.addToContext("SearchCriteria", targetModel);

                _wscMobileHomeUtils.openScreenWithInputData("wsc.mobile.home.search.SearchResult", targetModel, "wsc.mobile.editors.MobileEditor");

            }
          else {
           _iasBaseTemplateUtils.showMessage(this, "Invalid Search Criteria. Please Search either with Container number or Order number and Customer details", "error", null);
	 
	   }

        },

        getStatusModel: function(screen) {
            var statusModel = {
                "StatusList": {
                    "Status": [{
                            "StatusCode": "1100.70.06.30",
                            "StatusKey": "Ready To Ship To Store"
                        },

                        {
                            "StatusCode": "1400_SHP",
                            "StatusKey": "Shipped To Store"
                        },
                        {
                            "StatusCode": "1600",
                            "StatusKey": "Receiving In Progress"
                        },
                        {
                            "StatusCode": "1100.70.06.10",
                            "StatusKey": "Ready To Stage"
                        },
                        {
                            "StatusCode": "1100.70.06.20",
                            "StatusKey": "Staging In Progress"
                        },
                        {
                            "StatusCode": "1100.70.06.30.5",
                            "StatusKey": "Ready For Customer Pick Up"
                        },
                        {
                            "StatusCode": "1400_PICK",
                            "StatusKey": "Picked Up By Customer"
                        },
                        //OMNI-95719 START
                        {
                            "StatusCode": "1100.70.06.10.5",
                            "StatusKey": "Assembly in Progress"
                        },
                        //OMNI-95719 END

                        // {"StatusCode":"1400", "StatusKey":"Shipped from store or Picked up by Customer"}
                    ]
                }
            }
            return statusModel;
        },
        //OMNI-101572 Start
		 getShipmentTypeModel: function(screen) {
            var shipmentTypeModel = {
                "ShipmentList": {
                    "NodeType": [{
                            "TypeCode": "Store",
                            "TypeKey": "Shipped from store"
                        },

                        {
                             "TypeCode": "SharedInventoryDC",
                            "TypeKey": "Shipped from DC"
                        },
                    ]
                }
            }
            return shipmentTypeModel;
        },
		//OMNI-101572 End
        initializeScreen: function(event, bEvent, ctrl, args) {
            var savedSearchCriteriaModel = null;
	   
	   //OMNI - 8777 - Session object is retaining in the Ship to store order search screen in the scenario which is mentioned in the steps - Start
             savedSearchCriteriaModel =  _iasContextUtils.getFromContext("SearchCriteria");
            //_scScreenUtils.getInitialInputData(
               // this);
	   //OMNI - 8777 - Session object is retaining in the Ship to store order search screen in the scenario which is mentioned in the steps - End

            var fs = this.getWidgetByUId("filteringSelectOrderStatus");
            this._addReadOnlyState();
            dConnect.connect(fs, "openDropDown", this, "_addReadOnlyState");
            dConnect.connect(fs, "closeDropDown", this, "_addReadOnlyState");
            if (!(
                    _scBaseUtils.isVoid(
                        savedSearchCriteriaModel))) {
                _scScreenUtils.setModel(
                    this, "SavedSearchCriteria", savedSearchCriteriaModel, null);
                 this.validateAndDisableFeilds();
            }

            this.setShipmentStatusList();
            this.setAllOptionOnFilteringSelect(
                savedSearchCriteriaModel);
            //OMNI-101572 Start
			var fst = this.getWidgetByUId("extn_filtering_select_shipment_type");
            this._addReadOnlyState();
            dConnect.connect(fst, "openDropDown", this, "_addReadOnlyState");
            dConnect.connect(fst, "closeDropDown", this, "_addReadOnlyState");
            if (!(
                    _scBaseUtils.isVoid(
                        savedSearchCriteriaModel))) {
                _scScreenUtils.setModel(
                    this, "SavedSearchCriteria", savedSearchCriteriaModel, null);
                 this.validateAndDisableFeilds();
            }

            this.setShipmentTypeList();
			 this.setAllOptionOnFilteringSelectForShipmentType(
                savedSearchCriteriaModel);
			//OMNI-101572 End
             _scWidgetUtils.setFocusOnWidgetUsingUid(this, "txt_SearchContiainerNo");
        },

        _removeReadOnlyState: function() {
            var fs = this.getWidgetByUId("filteringSelectOrderStatus");
            dDomAttr.remove(fs.textbox, "readonly");
        },
        _addReadOnlyState: function() {
            var fs = this.getWidgetByUId("filteringSelectOrderStatus");
            dDomAttr.set(fs.textbox, "readonly", true);
        },

        setAllOptionOnFilteringSelect: function(savedAdvSearchCriteriaModel) {
            if (
                _scBaseUtils.isVoid(
                    _scModelUtils.getStringValueFromPath("Shipment.Status", savedAdvSearchCriteriaModel))) {
                _scModelUtils.setStringValueAtModelPath("Shipment.Status", "", savedAdvSearchCriteriaModel);
            }
            _scScreenUtils.setModel(
                this, "SavedAdvancedSearchCriteria", savedAdvSearchCriteriaModel, null);
        },
        setShipmentStatusList: function() {
            var getShipmentStatusList_output = null;
            getShipmentStatusList_output = this.getStatusModel(this);
            _scScreenUtils.setModel(
                this, "getShipmentStatusList_output", getShipmentStatusList_output, null);
        },
        //OMNI-101572 Start
		 setShipmentTypeList: function() {
            var getShipmentTypeList_output = null;
            getShipmentTypeList_output = this.getShipmentTypeModel(this);
            _scScreenUtils.setModel(
                this, "getShipmentTypeList_output", getShipmentTypeList_output, null);
        },
		
		  setAllOptionOnFilteringSelectForShipmentType: function(savedAdvSearchCriteriaModel) {

            if (
                _scBaseUtils.isVoid(
                    _scModelUtils.getStringValueFromPath("Shipment.NodeType", savedAdvSearchCriteriaModel))) {
                _scModelUtils.setStringValueAtModelPath("Shipment.NodeType", "", savedAdvSearchCriteriaModel);
            }else if( !_scBaseUtils.isVoid(
                    _scModelUtils.getStringValueFromPath("Shipment.ProcureFromNodeQryType", savedAdvSearchCriteriaModel)) &&
					_scBaseUtils.equals( _scModelUtils.getStringValueFromPath("Shipment.ProcureFromNodeQryType", savedAdvSearchCriteriaModel) ,"NOTNULL") ) {
					_scModelUtils.setStringValueAtModelPath("Shipment.NodeType", "SharedInventoryDC", savedAdvSearchCriteriaModel);
			}
            _scScreenUtils.setModel(
                this, "SavedAdvancedSearchCriteria", savedAdvSearchCriteriaModel, null);
        },
		//OMNI-101572 End

         extn_searchSTSOrders_OnEnter: function(event, bEvent, ctrl, args) {
           //OMNI- 6624 - START
            this.validateAndDisableFeilds();
            //OMNI- 6624 - END
            
            if (
                _iasEventUtils.isEnterPressed(
                    event)) {
                this.searchSTSOrders();
            }

        },

       validateAndDisableFeilds: function() 
       {
    var targetModel = _scBaseUtils.getTargetModel(this, "getShipmentSearch_input");
    var sContainerNo = _scModelUtils.getStringValueFromPath("Shipment.Containers.Container.ContainerNo", targetModel);
    var sOrderNo = _scModelUtils.getStringValueFromPath("Shipment.refNo", targetModel);
    var sFirstName = _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.FirstName", targetModel);
    var sLastName = _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.LastName", targetModel);
    var sEmail = _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.EMailID", targetModel);
    var sPhoneNo = _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.DayPhone", targetModel);
    //OMNI-101572 Start
	var stsShipmentUIFetaureFlag = _iasContextUtils.getFromContext("globalSTSShipmentUIFeature");
		if(!_scBaseUtils.isVoid(stsShipmentUIFetaureFlag) && _scBaseUtils.equals(stsShipmentUIFetaureFlag ,"Y")) {
			 _scWidgetUtils.showWidget(this, "extn_filtering_select_shipment_type", true);
		}
	//OMNI-101572 End
    if (!_scBaseUtils.isVoid(sContainerNo)) {
        _scWidgetUtils.disableWidget(this, "txt_SearchOrderNo", true);
        _scWidgetUtils.disableWidget(this, "txt_SearchCustLastName", true);
        _scWidgetUtils.disableWidget(this, "txt_SearchCustFirstName", true);
        _scWidgetUtils.disableWidget(this, "txt_SearchCustEmail", true);
        _scWidgetUtils.disableWidget(this, "txt_SearchCustPhoneNo", true);

    } else if ((!_scBaseUtils.isVoid(sOrderNo)) || (!_scBaseUtils.isVoid(sFirstName)) || (!_scBaseUtils.isVoid(sLastName)) || (!_scBaseUtils.isVoid(sEmail)) || (!_scBaseUtils.isVoid(sPhoneNo))) {
        _scWidgetUtils.disableWidget(this, "txt_SearchContiainerNo", true);
    } else {
        _scWidgetUtils.enableWidget(this, "txt_SearchOrderNo");
        _scWidgetUtils.enableWidget(this, "txt_SearchCustLastName");
        _scWidgetUtils.enableWidget(this, "txt_SearchCustFirstName");
        _scWidgetUtils.enableWidget(this, "txt_SearchCustEmail");
        _scWidgetUtils.enableWidget(this, "txt_SearchCustPhoneNo");
        _scWidgetUtils.enableWidget(this, "txt_SearchContiainerNo");
    }
}

    });
});