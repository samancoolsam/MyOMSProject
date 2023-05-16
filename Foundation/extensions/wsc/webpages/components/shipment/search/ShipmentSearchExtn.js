
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/search/ShipmentSearchExtnUI","scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!ias/utils/BaseTemplateUtils","scbase/loader!ias/utils/ContextUtils","scbase/loader!sc/plat/dojo/utils/EventUtils","scbase/loader!ias/utils/UIUtils","scbase/loader!ias/utils/ScreenUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnShipmentSearchExtnUI
			 ,
				_scWidgetUtils
			,
				_scBaseUtils
			,
				_scModelUtils
			,
				_scScreenUtils
			,
				_iasBaseTemplateUtils
			,
				_iasContextUtils
			,
				_scEventUtils
			,
				_iasUIUtils
			,
				_iasScreenUtils

){ 
	return _dojodeclare("extn.components.shipment.search.ShipmentSearchExtn", [_extnShipmentSearchExtnUI],{
	// custom code here
	afterScreenLoad: function(event, bEvent, ctrl, args) {
		var selected = null;
		selectedTargetModel = _scBaseUtils.getTargetModel(
		this, "pickShipOrBoth", null);
		selected = _scModelUtils.getStringValueFromPath("Option", selectedTargetModel);
		if( _scBaseUtils.equals(selected, "PICK"))
		{
			_scWidgetUtils.setFocusOnWidgetUsingUid(this, "txt_orderNo");
		}
		else
		{
			_scWidgetUtils.setFocusOnWidgetUsingUid(this, "txt_shipmentNo");
		}
	},
	
	/* This OOB method has been overridden in order to stop the status formation from Search Utils and call our customized method for status formation */
	fulfillmentOnChange: function() {
		var selectedTargetModel = null;
		var selected = null;
		selectedTargetModel = _scBaseUtils.getTargetModel(
		this, "pickShipOrBoth", null);
		selected = _scModelUtils.getStringValueFromPath("Option", selectedTargetModel);
		var shipmentStatusList = null;
		shipmentStatusList = this.getStatusModel(
		selected);
		_scScreenUtils.setModel(
		this, "getShipmentStatusList_output", shipmentStatusList, null);
		var screenInput = null;
		screenInput = _scScreenUtils.getInitialInputData(
		this);
		_scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod", selected, screenInput);
		screenInput = this.setShipmentStatusDefaultValues(
		screenInput);
		_scScreenUtils.setModel(
		this, "screenInput", screenInput, null);

		//BOPIS-1435: Advanced Search performance consideration changes - starts
		_scScreenUtils.setModel(this, "shipmentStatus", screenInput, null);
		//BOPIS-1435: Advanced Search performance consideration changes - ends

		var returnScreenInput = null;
		returnScreenInput = this.toggleFields(
		screenInput);
		if( _scBaseUtils.equals(selected, "PICK"))
		{
			_scWidgetUtils.setFocusOnWidgetUsingUid(this, "txt_orderNo");
		}
		else
		{
			_scWidgetUtils.setFocusOnWidgetUsingUid(this, "txt_shipmentNo");
		}
		return returnScreenInput;
    },
	
	/* This OOB method has been overridden in order to formulate the status displayed for desktop version */
	getStatusModel : function( pickShipOrBoth ) {
			if ( pickShipOrBoth === " " || pickShipOrBoth === "" ) {
				var statusModel = { "StatusList" :
					{ "Status" : 
						[{"StatusCode":"1100.70.06.10", "StatusKey":"Option_ReadyForBackroomPickup"},
						 {"StatusCode":"1100.70.06.20", "StatusKey":"Option_PicksInProgress"},
						 {"StatusCode":"1100.70.06.30.5", "StatusKey":"Ready for customer pick up"},
						 {"StatusCode":"1100.70.06.30.7", "StatusKey":"Paper work initiated"},
						 {"StatusCode":"1100.70.06.50", "StatusKey":"Option_ReadyForPacking"},
						 {"StatusCode":"1100.70.06.70", "StatusKey":"Option_PackingInProgress"},
						 {"StatusCode":"1100.70.06.30", "StatusKey":"Ready to ship"},
						 //OMNI-98480--START
						 {"StatusCode":"1100.70.06.10.5", "StatusKey":"Assembly in Progress"},
						 //OMNI-98480--END
						 // {"StatusCode":"1400", "StatusKey":"Shipped from store or Picked up by customer"}
						 ] 
					}
				}
				return statusModel;
			} else if ( pickShipOrBoth === "SHP" ) {
				var statusModel = { "StatusList" :
				{ "Status" : 
					[{"StatusCode":"1100.70.06.10", "StatusKey":"Option_ReadyForBackroomPickup"},
					 {"StatusCode":"1100.70.06.20", "StatusKey":"Option_PicksInProgress"},
					 {"StatusCode":"1100.70.06.50", "StatusKey":"Option_ReadyForPacking"},
					 {"StatusCode":"1100.70.06.70", "StatusKey":"Option_PackingInProgress"},
					 {"StatusCode":"1100.70.06.30", "StatusKey":"Ready to ship"},
					 //OMNI-98480--START
					 {"StatusCode":"1100.70.06.10.5", "StatusKey":"Assembly in Progress"},
					 //OMNI-98480--END
					 ] 
				}
			}
			return statusModel;
			} else if ( pickShipOrBoth === "PICK" ) {
				var statusModel = { "StatusList" :
				{ "Status" : 
					[{"StatusCode":"1100.70.06.10", "StatusKey":"Option_ReadyForBackroomPickup"},
					 {"StatusCode":"1100.70.06.20", "StatusKey":"Option_PicksInProgress"},
					 {"StatusCode":"1100.70.06.30.5", "StatusKey":"Ready for customer pick up"},
					 {"StatusCode":"1100.70.06.30.7", "StatusKey":"Paper work initiated"},
					 //OMNI-98480--START
					 {"StatusCode":"1100.70.06.10.5", "StatusKey":"Assembly in Progress"},
					 //OMNI-98480--END
					 //BOPIS-1435: Advanced Search performance consideration changes - starts
					 //{"StatusCode":"1400", "StatusKey":"Picked up by customer"}
					 //BOPIS-1435: Advanced Search performance consideration changes - ends
					 ] 
				}
			}
			return statusModel;
			}			
		},
		
		SST_search: function() {
			var bIsValidBlindSearch = true;
            var targetModel = null;
            var tempModel = null;
            if (!(
            _scScreenUtils.isValid(
            this, this.SST_getSearchNamespace()))) {
                _iasBaseTemplateUtils.showMessage(
                this, "InvalidSearchCriteria", "error", null);
                return;
            } else {
                _iasBaseTemplateUtils.hideMessage(
                this);
                targetModel = _scBaseUtils.getTargetModel(
                this, this.SST_getSearchNamespace(), null);
                //BOPIS-1595 BEGIN
                var orderno = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.OrderNo",targetModel);
                var shipmentno = _scModelUtils.getStringValueFromPath("Shipment.ShipmentNo",targetModel);
                var statusList = _scModelUtils.getStringValueFromPath("Shipment.StatusList",targetModel);
                if(_scBaseUtils.isVoid(orderno) & _scBaseUtils.isVoid(shipmentno) & _scBaseUtils.isVoid(statusList)){
                	bIsValidBlindSearch = false;
                }
                //BOPIS-1595 END
                var billToAddress = targetModel.Shipment.BillToAddress;
                if(!_scBaseUtils.isVoid(billToAddress))
                {
                	var email = billToAddress.EMailID;
					if(!_scBaseUtils.isVoid(email))
					{
						email = email.toLowerCase();
						targetModel.Shipment.BillToAddress.EMailID = email;
					}
                }
                if (
                _scBaseUtils.equals(
                this.SST_getSearchNamespace(), "getAdvancedShipmentListWithGiftRct_input")) {
                    tempModel = _scModelUtils.getModelObjectFromPath("Shipment.OrderBy", _scScreenUtils.getTargetModel(
                    this, "getAdvancedShipmentList_input", null));
                    _scModelUtils.addModelToModelPath("Shipment.OrderBy", tempModel, targetModel);
                }
            }
            var assignedTo = _scModelUtils.getStringValueFromPath("Shipment.AssignedToUserId", targetModel);



            if(bIsValidBlindSearch){



            	if(_scBaseUtils.isVoid(assignedTo)){
		            var includeOtherStores = null;
		            includeOtherStores = _scBaseUtils.getTargetModel(
		            this, "IncludeOrdersPickedInOtherStore", null);
		            //BOPIS-1595 BEGIN
		            if(!_scBaseUtils.isVoid(orderno) ||  !_scBaseUtils.isVoid(shipmentno)){
		            targetModel.Shipment.StatusList = {};
		            }
		            //BOPIS-1595 END
		            if (
		            _scBaseUtils.equals(
		            _scModelUtils.getStringValueFromPath("isChecked", includeOtherStores), "Y")) {
		                _scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", " ", targetModel);
		            } else {
		                _scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", _iasContextUtils.getFromContext("CurrentStore"), targetModel);
		            }
		            _scBaseUtils.removeBlankAttributes(
		            targetModel);
		            var eventDefn = null;
		            var args = null;
		            eventDefn = {};
		            args = {};
		            _scBaseUtils.setAttributeValue("inputData", targetModel, args);
		            _scBaseUtils.setAttributeValue("argumentList", args, eventDefn);
		            _scEventUtils.fireEventToChild(
		            this, "shipmentSearchResult", "callListApi", eventDefn);
		        } else {
		        	var includeOtherStores = _scBaseUtils.getTargetModel(
		            this, "IncludeOrdersPickedInOtherStore", null);
		        	if (
		            _scBaseUtils.equals(
		        	_scModelUtils.getStringValueFromPath("isChecked", includeOtherStores), "Y")) {
		        		_iasScreenUtils.showInfoMessageBoxWithOk(this, "Please clear the 'Assigned to' field or un-check the search option: 'Include orders being fulfilled by other stores.'", "infoCallBack", null);
		        	} else {
			        	var user = _scModelUtils.createNewModelObjectWithRootKey("User");
			        	_scModelUtils.setStringValueAtModelPath("User.Username", assignedTo , user);

			        	_iasUIUtils.callApi(this, user, "extn_getUserIDFromUsername_ref", null);
			        }
		        }



            }
            else if(_scBaseUtils.isVoid(targetModel.Shipment.StatusList)){
				_iasBaseTemplateUtils.showMessage(this, "Select at least one status checkbox to proceed!", "error", null);
			}




        
        },

        infoCallBack: function(res) {
        
       	},

        afterBehaviourMashupCall: function(event, bEvent, ctrl, args) {
         var mashupArrayList = _scModelUtils.getModelObjectFromPath("mashupArray", args);
            var mashupArrayListLength = Object.keys(mashupArrayList).length;
            for(var iCount = 0; iCount < mashupArrayListLength; iCount++) {
                var mashupArray = mashupArrayList[iCount];
                var mashupRefId = _scModelUtils.getModelObjectFromPath("mashupRefId", mashupArray);
                if(_scBaseUtils.equals(mashupRefId, "extn_getUserIDFromUsername_ref")) {
                 var userid = null;
                    var mashupOutputObject = _scModelUtils.getModelObjectFromPath("mashupRefOutput", mashupArray);
                    if(!_scBaseUtils.isVoid(mashupOutputObject.UserList)) {
                     var userArray = mashupOutputObject.UserList.User;
                     var userArrayLength = userArray.length;
                     for(var iUser = 0; iUser < userArrayLength; iUser++) {
                      if(_scBaseUtils.equals(_iasContextUtils.getFromContext("CurrentStore"),userArray[iUser].OrganizationKey)){
                       userid = _scModelUtils.getStringValueFromPath("Loginid", userArray[iUser]);
                      }
                     }
                     
                     var targetModel = _scBaseUtils.getTargetModel(
                this, this.SST_getSearchNamespace(), null);
                     _scModelUtils.setStringValueAtModelPath("Shipment.AssignedToUserId", userid ,targetModel);
                  var includeOtherStores = null;
               includeOtherStores = _scBaseUtils.getTargetModel(
               this, "IncludeOrdersPickedInOtherStore", null);
               //BOPIS-1595 BEGIN
                var orderno = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.OrderNo",targetModel);
                var shipmentno = _scModelUtils.getStringValueFromPath("Shipment.ShipmentNo",targetModel);
               if(!_scBaseUtils.isVoid(orderno) ||  !_scBaseUtils.isVoid(shipmentno)){
		            targetModel.Shipment.StatusList = {};
		       }
		       //BOPIS-1595 END
               if (
               _scBaseUtils.equals(
               _scModelUtils.getStringValueFromPath("isChecked", includeOtherStores), "Y")) {
                   _scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", " ", targetModel);
               } else {
                   _scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", _iasContextUtils.getFromContext("CurrentStore"), targetModel);
               }
               _scBaseUtils.removeBlankAttributes(
               targetModel);
               var eventDefn = null;
               var args = null;
               eventDefn = {};
               args = {};
               _scBaseUtils.setAttributeValue("inputData", targetModel, args);
               _scBaseUtils.setAttributeValue("argumentList", args, eventDefn);
               _scEventUtils.fireEventToChild(
               this, "shipmentSearchResult", "callListApi", eventDefn);
                 } else {
                  _iasScreenUtils.showInfoMessageBoxWithOk(this, "Not a valid Username", "infoCallBack", null);
                 }

                }
            }
        },

        //BOPIS-1435: Overrid below method to check all status checkboxes on screen load 
        setShipmentStatusDefaultValues: function(screenInput) {
            var shipmentElem = null;
            if (
            _scBaseUtils.isVoid(
            screenInput)) {
                screenInput = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
            }
            shipmentElem = _scModelUtils.getModelObjectFromKey("Shipment", screenInput);
            var isSearchOnInit = _scModelUtils.getStringValueFromPath("DoSearchOnInit", shipmentElem);
            var defaultOptions = null;
            defaultOptions = _scModelUtils.createModelListFromKey("Status", shipmentElem);
            var deliveryMethod = null;
            deliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", screenInput);
            var fromCustomerPick = null;
            fromCustomerPick = _scModelUtils.getStringValueFromPath("Shipment.fromCustomerPick", screenInput);
            var noDefaultStatuses = null;
            noDefaultStatuses = _scModelUtils.getStringValueFromPath("Shipment.NoDefaultStatuses", screenInput);
            if (
            _scBaseUtils.equals(
            noDefaultStatuses, "true") && _scBaseUtils.equals("Y", this.getFirstLookFlag())) {
                this.setFirstLookFlag("N");
            } else {
                if (
                _scBaseUtils.equals(
                deliveryMethod, " ")) {
                    defaultOptions.push("1100.70.06.10");
                    defaultOptions.push("1100.70.06.20");
                    //BOPIS-1435: Advanced Search performance consideration changes - starts
                    defaultOptions.push("1100.70.06.30");
                    defaultOptions.push("1100.70.06.30.5");
                    defaultOptions.push("1100.70.06.30.7");
                    defaultOptions.push("1100.70.06.50");
                    defaultOptions.push("1100.70.06.70");
					//OMNI-98480--START
					defaultOptions.push("1100.70.06.10.5");
					//OMNI-98480--END
                    //BOPIS-1435: Advanced Search performance consideration changes - ends
                } else if (
                _scBaseUtils.equals(
                deliveryMethod, "PICK")) {
                    if (
                    _scBaseUtils.equals(
                    fromCustomerPick, "true")) {
                        defaultOptions.push("1100.70.06.30");
						//BOPIS-1435: Advanced Search performance consideration changes - starts
                        //defaultOptions.push("1400");
						defaultOptions.push("1100.70.06.10");
                        defaultOptions.push("1100.70.06.20");
                        defaultOptions.push("1100.70.06.30.5");
                        defaultOptions.push("1100.70.06.30.7");
						//OMNI-98480--START
						defaultOptions.push("1100.70.06.10.5");
						//OMNI-98480--END
						//BOPIS-1435: Advanced Search performance consideration changes - ends
                    } else {
                        defaultOptions.push("1100.70.06.10");
                        defaultOptions.push("1100.70.06.20");
                        defaultOptions.push("1100.70.06.30");
                        //BOPIS-1435: Advanced Search performance consideration changes - starts
                        defaultOptions.push("1100.70.06.30.5");
                        defaultOptions.push("1100.70.06.30.7");
						//OMNI-98480--START
						defaultOptions.push("1100.70.06.10.5");
						//OMNI-98480--END
                        //BOPIS-1435: Advanced Search performance consideration changes - ends
                    }
                } else if (
                _scBaseUtils.equals(
                deliveryMethod, "SHP")) {
                    //fix for BOPIS-1586:Clicking on SFS orders to pack pulls all the orders instead of only pulling the orders that are "Ready to pack" and "Pack In-progress"
                	if(!_scBaseUtils.isVoid(isSearchOnInit) && isSearchOnInit) {
                		defaultOptions.push("1100.70.06.50");
                    	defaultOptions.push("1100.70.06.70");
                	}
                	else {
	                    defaultOptions.push("1100.70.06.50");
	                    defaultOptions.push("1100.70.06.70");
	                    //BOPIS-1435: Advanced Search performance consideration changes - starts
	                    defaultOptions.push("1100.70.06.10");
	                    defaultOptions.push("1100.70.06.20");
	                    defaultOptions.push("1100.70.06.30");
						//OMNI-98480--START
						defaultOptions.push("1100.70.06.10.5");
						//OMNI-98480--END
	                    //BOPIS-1435: Advanced Search performance consideration changes - ends
	                }
	                //BOPIS-1586: End
                }
            }
            return screenInput;
        },

        //BOPIS-1435: Overrid below method to check all status checkboxes on screen load 
        initializeScreen: function(event, bEvent, ctrl, args) {
            this.setFirstLookFlag("Y");

      		//BOPIS-1435: Advanced Search performance consideration changes - starts
            var screenInput = _scScreenUtils.getInitialInputData(this);
            screenInput = this.setShipmentStatusDefaultValues(screenInput);
            _scScreenUtils.setModel(this, "screenInput", screenInput, null);
            //BOPIS-1435: Advanced Search performance consideration changes - ends
                    
            this.setShipmentStatusList();
            this.toggleRecipient();
            var screenInput = null;
            screenInput = this.fulfillmentOnChange();
            this.setOtherStore(screenInput);
            if (!( _scBaseUtils.isVoid(screenInput))) {
            	 if (_scBaseUtils.isVoid(_scModelUtils.getStringValueFromPath("Shipment.SCAC", screenInput))) {
            		 _scModelUtils.setStringValueAtModelPath("Shipment.SCAC", "", screenInput);
                 }
                _scScreenUtils.setModel(
                this, "screenInput", screenInput, null);
                _scScreenUtils.setModel(
                this, "shipmentStatus", screenInput, null);
                var hidePickShip = null;
                hidePickShip = _scModelUtils.getStringValueFromPath("Shipment.HidePickShipOption", screenInput);
                if (
                _scBaseUtils.equals(
                hidePickShip, "true")) {
                    _scWidgetUtils.hideWidget(
                    this, "rad_pickShipOrBoth", false);
                } else {
                    _scWidgetUtils.showWidget(
                    this, "rad_pickShipOrBoth", false, null);
                }
            }
            var doSearchOnInit = null;
            doSearchOnInit = _scModelUtils.getStringValueFromPath("Shipment.DoSearchOnInit", screenInput);
            if (
            _scBaseUtils.equals(
            doSearchOnInit, "true")) {
                this.SST_search();
            }
        }

});
});

