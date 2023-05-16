
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/mobile/home/search/AdvancedSearchExtnUI","scbase/loader!sc/plat/dojo/utils/ScreenUtils",
"scbase/loader!dojo/_base/connect","scbase/loader!dojo/dom-attr","scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnAdvancedSearchExtnUI
			,
				_scScreenUtils
			,
				dConnect
			, 
				dDomAttr
			,
				_scWidgetUtils
			,
				_scBaseUtils
){ 
	return _dojodeclare("extn.mobile.home.search.AdvancedSearchExtn", [_extnAdvancedSearchExtnUI],{
	// custom code here
	
	/* This OOB method has been overridden in order to override the OOB call for status dropdown */
	 setShipmentStatusList: function() {
            var getShipmentStatusList_output = null;
            getShipmentStatusList_output = this.getStatusModel(this);
            _scScreenUtils.setModel(
            this, "getShipmentStatusList_output", getShipmentStatusList_output, null);
        },
		
	/* This OOB method has been overridden in order to formulate the status displayed for mobile version */
	getStatusModel : function( screen ) 
		{
			var statusModel = { "StatusList" :
				{ "Status" : 
					[{"StatusCode":"1100.70.06.10", "StatusKey":"Option_ReadyForBackroomPickup"},
					 {"StatusCode":"1100.70.06.20", "StatusKey":"Option_PicksInProgress"},
					 {"StatusCode":"1100.70.06.30.5", "StatusKey":"Ready For Customer Pick Up"},
					 {"StatusCode":"1100.70.06.30.7", "StatusKey":"Paper Work Initiated"},
					 {"StatusCode":"1100.70.06.50", "StatusKey":"Option_ReadyForPacking"},
					 {"StatusCode":"1100.70.06.70", "StatusKey":"Option_PackingInProgress"},
					 {"StatusCode":"1100.70.06.30", "StatusKey":"Ready To Ship"},
					 //OMNI-98480--START
					 {"StatusCode":"1100.70.06.10.5", "StatusKey":"Assembly in Progress"},
					 //OMNI-98480--END
					 // {"StatusCode":"1400", "StatusKey":"Shipped from store or Picked up by Customer"}
					 ] 
				}
			}
			return statusModel;
		},
		
		/* BOPIS 1571 */
		initializeScreen: function(
        event, bEvent, ctrl, args) {
			
			var fs = this.getWidgetByUId("filteringSelectOrderStatus");
			var fs2 = this.getWidgetByUId("filteringSelectFullFillmentType");
		
			this._addReadOnlyState();
			dConnect.connect(fs, "openDropDown", this, "_addReadOnlyState");
			dConnect.connect(fs, "closeDropDown", this, "_addReadOnlyState");
			dConnect.connect(fs2, "openDropDown", this, "_addReadOnlyState");
			dConnect.connect(fs2, "closeDropDown", this, "_addReadOnlyState");
			
            var savedAdvSearchCriteriaModel = null;
            savedAdvSearchCriteriaModel = _scScreenUtils.getModel(
            this, "SavedAdvancedSearchCriteria");
			//OMNI-71126 Pick Up Order Grouping - Pick Up Order Grouping Order Search TC70 START
			if (!(_scBaseUtils.isVoid(savedAdvSearchCriteriaModel))) {
				var checkboxChecked = savedAdvSearchCriteriaModel.Shipment.ShowRelatedOrders;
				if(!(_scBaseUtils.isVoid(checkboxChecked)) && (_scBaseUtils.equals(checkboxChecked,"Y"))){
					_scWidgetUtils.disableWidget(this, "txt_SearchCustLastName", true);
					_scWidgetUtils.disableWidget(this, "txt_SearchCustFirstName", true);
					_scWidgetUtils.disableWidget(this, "txt_SearchCustPhoneNo", true);
					_scWidgetUtils.disableWidget(this, "filteringSelectOrderStatus", true);
					_scWidgetUtils.disableWidget(this, "filteringSelectFullFillmentType", true);
				}
			}
			//OMNI-71126 Pick Up Order Grouping - Pick Up Order Grouping Order Search TC70 END
            this.setShipmentStatusList();
            this.setDeliveryMethodList();
            this.setAllOptionOnFilteringSelect(
            savedAdvSearchCriteriaModel);
        },

		_removeReadOnlyState: function() {
			var fs = this.getWidgetByUId("filteringSelectOrderStatus");
			var fs2 = this.getWidgetByUId("filteringSelectFullFillmentType");
			dDomAttr.remove(fs.textbox, "readonly");
			dDomAttr.remove(fs2.textbox, "readonly");
		},
		_addReadOnlyState: function() {
			var fs = this.getWidgetByUId("filteringSelectOrderStatus");
			var fs2 = this.getWidgetByUId("filteringSelectFullFillmentType");
			dDomAttr.set(fs.textbox, "readonly", true);
			dDomAttr.set(fs2.textbox, "readonly", true);
		}
		/*BOPIS -1571 END */
});
});

