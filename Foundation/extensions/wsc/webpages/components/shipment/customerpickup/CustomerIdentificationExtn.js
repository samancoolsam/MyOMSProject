
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/customerpickup/CustomerIdentificationExtnUI","scbase/loader!dojo/_base/connect","scbase/loader!dojo/dom-attr","scbase/loader!ias/utils/ContextUtils", "scbase/loader!ias/utils/EventUtils", "scbase/loader!ias/utils/RepeatingScreenUtils", "scbase/loader!ias/utils/ScreenUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!ias/utils/WizardUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/RepeatingPanelUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!wsc/components/common/utils/CommonUtils", "scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils", "scbase/loader!wsc/components/shipment/customerpickup/ProductVerificationUI", "scbase/loader!sc/plat/dojo/Userprefs","scbase/loader!ias/utils/BaseTemplateUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnCustomerIdentificationExtnUI, dConnect, dDomAttr
			  ,
			  	_iasContextUtils, _iasEventUtils, _iasRepeatingScreenUtils, _iasScreenUtils, _iasUIUtils, _iasWizardUtils, _scBaseUtils, _scEventUtils, _scModelUtils, _scRepeatingPanelUtils, _scScreenUtils, _scWidgetUtils, _wscCommonUtils, _wscShipmentUtils, _wscProductVerificationUI, _scUserprefs, _iasBaseTemplateUtils
){ 
	return _dojodeclare("extn.components.shipment.customerpickup.CustomerIdentificationExtn", [_extnCustomerIdentificationExtnUI],{
	// custom code here
    maxAgeRestrictionValue: 0,
	isAgeRestrictedSet: false,
    
	extn_afterInitialize: function( event, bEvent, ctrl, args) {

        var fs = this.getWidgetByUId("cmbCustVerfMethod");
        var fs2 = this.getWidgetByUId("extn_filteringselect");
        this._addReadOnlyState();
        dConnect.connect(fs, "openDropDown", this, "_addReadOnlyState");
        dConnect.connect(fs, "closeDropDown", this, "_addReadOnlyState");
        dConnect.connect(fs2, "openDropDown", this, "_addReadOnlyState");
        dConnect.connect(fs2, "closeDropDown", this, "_addReadOnlyState");

		var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");
        var shipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentDetails);
        // Changes for Age verfication: Begin
        if (!_scBaseUtils.isVoid(shipmentLineList)) {
            this.maxAgeRestrictionValue=0;
            for (var i=0;i<shipmentLineList.length;i++) {
                // var additionalAttributeList = _scModelUtils.getStringValueFromPath("OrderLine.ItemDetails.AdditionalAttributeList.AdditionalAttribute", shipmentLineList[i]);
                // if (!_scBaseUtils.isVoid(additionalAttributeList)) {
                //     for (var j=0;j<additionalAttributeList.length;j++) {
                //         var restrictedVariable = _scModelUtils.getStringValueFromPath("Name", additionalAttributeList[j]);
                //         if (_scBaseUtils.equals(restrictedVariable, "IsAgeRestricted")) {
                //             var isAgeRestrictedValue = _scModelUtils.getStringValueFromPath("Value", additionalAttributeList[j]);
                //         }
                //         if (_scBaseUtils.equals(restrictedVariable, "MinAgeRestriction")) {
                //              var minAgeRestrictionValue = Number(_scModelUtils.getStringValueFromPath("Value", additionalAttributeList[j]));
                //         }
                //     }
                // }
                //BOPIS-1627: Age restriction information pop up getting displayed even after shorting all age restricted items during backroom pick: Begin
                var quantity = _scModelUtils.getNumberValueFromPath("OriginalQuantity", shipmentLineList[i]);
                var shortageQty = _scModelUtils.getNumberValueFromPath("ShortageQty", shipmentLineList[i]);
                if ((quantity - shortageQty) != 0 ) {
                    var ageRestrictedAttributes = _scModelUtils.getStringValueFromPath("OrderLine.Extn", shipmentLineList[i]);
                    if (!_scBaseUtils.isVoid(ageRestrictedAttributes)) {
                          var extnIsAgeRestricted = "";
                         var extnAgeRestrictionCode = 0;
                        var extnIsAgeRestricted = _scModelUtils.getStringValueFromPath("ExtnIsAgeRestricted", ageRestrictedAttributes);
                        var extnAgeRestrictionCode = _scModelUtils.getStringValueFromPath("ExtnAgeRestrictionCode", ageRestrictedAttributes);
                        //KER-16062: Age Validation Popup -Start
                         if (_scBaseUtils.equals(extnIsAgeRestricted, "Y")) {
                            if (extnAgeRestrictionCode.indexOf("Under") != -1) {
                                extnAgeRestrictionCode = Number(extnAgeRestrictionCode.substr(extnAgeRestrictionCode.length - 2));
                            }
                            else {
                                extnAgeRestrictionCode = Number(extnAgeRestrictionCode);
                            }
                            if (this.maxAgeRestrictionValue < extnAgeRestrictionCode) {
                                this.maxAgeRestrictionValue = extnAgeRestrictionCode;
                            }
                            if (_scBaseUtils.equals(extnIsAgeRestricted, "Y")) {
                                this.isAgeRestrictedSet = true;
                            }       
                        }
                        
                    }
                }   
            }
            //logic to calculate date : start
                    var currentDateTime = new Date();
                    var date = currentDateTime.toLocaleDateString();
                    var year = currentDateTime.getFullYear();
                    var restrictedYear = year - this.maxAgeRestrictionValue;
                    var indexOfYear = date.indexOf(year);
                    var dayAndMonth = date.substring(0, indexOfYear);
                    this.maxAgeRestrictionValue = dayAndMonth + restrictedYear;

            if (this.isAgeRestrictedSet) {
                _scScreenUtils.showConfirmMessageBox(this, 
                    "Items in the shipment require age validation. Is the customer born before " 
                    + this.maxAgeRestrictionValue + "?", "handleCloseConfirmation", null, null);
            _scEventUtils.stopEvent(bEvent);
            this.isAgeRestrictedSet = false;
            this.maxAgeRestrictionValue = 0;
             }
        }
        //KER-16062: Age Validation Popup -End

        


        var shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status", shipmentDetails);

            if(_scBaseUtils.equals(shipmentStatus, "1100.70.06.30.5") || _scBaseUtils.equals(shipmentStatus, "1100.70.06.30.7")) {

                var aDate = _scModelUtils.getStringValueFromPath("Shipment.AdditionalDates", shipmentDetails);
                if(!_scBaseUtils.isVoid(aDate.AdditionalDate)) {
                    for(var i=0; i < aDate.AdditionalDate.length; i++) {
                        var dateID = aDate.AdditionalDate[i].DateTypeId;
                        if(_scBaseUtils.equals(dateID, "ACADEMY_MAX_CUSTOMER_PICK_DATE")) {
                            if(!_scBaseUtils.isVoid(aDate.AdditionalDate[i])) {
                                var date = aDate.AdditionalDate[i].ActualDate;
                                var tmp = dojo.date.stamp.fromISOString(date,{selector: 'date'});
                                date = dojo.date.locale.format(tmp, {selector:'date',fullYear:'true'});
                                _scWidgetUtils.setValue(this, "extn_datalabel", date, false);
                                break;
                            }
                        }
                    }
                }
            }
	},

    //KER-16062: Age Validation Popup -Start
    handleCloseConfirmation: function(res) {
            if (_scBaseUtils.equals(res, "Ok")) {
                _scWidgetUtils.closePopup(
                    this, "CLOSE", false);
            }
           
             if (_scBaseUtils.equals(res, "Cancel")) {
            _iasScreenUtils.showInfoMessageBoxWithOk(this, "The customer does not meet the age requirements to purchase this item. Please cancel each age restricted item within the order by selecting 'Age Verification' as the short pick reason.", "infoCallBack", null);
        }

        },
        //KER-16062: Age Validation Popup -End

    _removeReadOnlyState: function() {
        var fs = this.getWidgetByUId("cmbCustVerfMethod");
        var fs2 = this.getWidgetByUId("extn_filteringselect");
        dDomAttr.remove(fs.textbox, "readonly");
        dDomAttr.remove(fs2.textbox, "readonly");
    },
    _addReadOnlyState: function() {
        var fs = this.getWidgetByUId("cmbCustVerfMethod");
        var fs2 = this.getWidgetByUId("extn_filteringselect");
        dDomAttr.set(fs.textbox, "readonly", true);
        dDomAttr.set(fs2.textbox, "readonly", true);
    },

	infoCallBack: function(res)	{
        
    },

    updateCustomerDropDown: function() {
        var sModel = _scScreenUtils.getModel(this, "ShipmentDetails");

        this.sKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", sModel);

        var oLine = _scModelUtils.getModelObjectFromPath("Shipment.ShipmentLines.ShipmentLine", sModel);
        var aCustomer = _scModelUtils.getStringValueFromPath("OrderLine.PersonInfoMarkFor.FirstName", oLine[0]);
         var isAltCustPresent = false;
         if (typeof aCustomer != 'undefined' && aCustomer)  {
         	 isAltCustPresent = true;
             aCustomer += " " + _scModelUtils.getStringValueFromPath("OrderLine.PersonInfoMarkFor.LastName", oLine[0]);
         }
        var oCustomer = _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.FirstName", sModel);
        oCustomer += " " + _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.LastName", sModel);

        var cusDropDown =  {"CustomerName":
        [
        {
            "Customer": oCustomer + " : P",
            "Customer_Name": oCustomer + " : P"
        }]
    }

        if(isAltCustPresent && !_scBaseUtils.equals(aCustomer, oCustomer)) {
            var sCustomer = {
                "Customer": aCustomer + " : A",
                "Customer_Name": aCustomer + " : A"
            }

            cusDropDown["CustomerName"].push(sCustomer);
        }
        _scScreenUtils.setModel(this, "extn_customerOptionNS", cusDropDown, null);
        //BOPIS-1432-BEGIN
        if (cusDropDown.CustomerName.length == 1){
            _scScreenUtils.setModel(this,"extn_DefaultCustomerName",cusDropDown,null);    
        }
        //BOPIS-1432-END
    },

    customerDropDown: function() {
        var ddValue = _scBaseUtils.getTargetModel(this,"extn_Test", null);
        var dvalue = ddValue["customerName"];
        if(!_scBaseUtils.isVoid(dvalue)) {
            var index = dvalue.indexOf(":");
            //fix for BOPIS-1751 : Start
            // var dropValue = dvalue.substring(0, index -1);
            var dropValue = dvalue.split(':')[1].trim();
             //fix for BOPIS-1751: end
            _scUserprefs.setProperty(this.sKey,dropValue);
        }
    },

    extn_afterScreenInit: function() {

 

        var sDetails = _scScreenUtils.getModel(this, "ShipmentDetails");
		
		/* OMNI- 5402 Mobile Web SOM Home Page: Ready For Curbside PickUp Section  - Start */

		var sCurbsideIdentifier = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted", sDetails);

		if (_iasContextUtils.isMobileContainer() && !_scBaseUtils.isVoid(sCurbsideIdentifier) && _scBaseUtils.equals(sCurbsideIdentifier,"Y")) {
	    _scWidgetUtils.showWidget(this,"extn_Curbsidelbl");
		}

    /* OMNI- 5402 Mobile Web SOM Home Page: Ready For Curbside PickUp Section  - END */ 
        var sLines = _scModelUtils.getModelObjectFromPath("Shipment.ShipmentLines.ShipmentLine", sDetails);

        var mCustomer = _scModelUtils.getStringValueFromPath("OrderLine.PersonInfoMarkFor.FirstName", sLines[0]);

        if(!_scBaseUtils.isVoid(mCustomer)) {
            var customerName1 = _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.FirstName", sDetails) + " " + _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.LastName", sDetails);
            _scWidgetUtils.setValue(this, "extn_label", customerName1 , null);
            _scWidgetUtils.setValue(this, "extn_label1", _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.DayPhone", sDetails) , null);
            _scWidgetUtils.setValue(this, "extn_label2", _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.EMailID", sDetails) , null);
        }
		
		var assignedToUserID = sDetails.Shipment.AssignedToUserId;
		var shipNodeExtn = sDetails.Shipment.ShipNode;
		if(!_scBaseUtils.isVoid(assignedToUserID))
		{
			var getUserListAPIInput = null;
                getUserListAPIInput = _scModelUtils.createNewModelObjectWithRootKey("User");
			_scModelUtils.setStringValueAtModelPath("User.DisplayUserID", assignedToUserID, getUserListAPIInput);
			_scModelUtils.setStringValueAtModelPath("User.OrganizationKey", shipNodeExtn, getUserListAPIInput);
			_iasUIUtils.callApi(
            this, getUserListAPIInput, "extn_getUserListByUserID", null);
		}
    },
	
	handleMashupCompletion: function(
         mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
         _iasBaseTemplateUtils.handleMashupCompletion(
           mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
         },
		 
	handleMashupOutput: function(
        mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) 
	{
		if ( _scBaseUtils.equals(mashupRefId, "extn_getUserListByUserID")) 
		{ 
			var userName = modelOutput.UserList.User[0].Username;
				
				var shipmentData = null;
				shipmentData = _scScreenUtils.getModel(this, "ShipmentDetails");
				_scModelUtils.setStringValueAtModelPath("Shipment.AssignedToUserId", userName, shipmentData);
				 _scScreenUtils.setModel(
                    this, "ShipmentDetails", shipmentData, null); 
        }
		
	},

    extn_getDefaultCustomerName: function(dataValue, screen, widget, namespace, modelObj, options) {
        var returnValue = modelObj.CustomerName[0].Customer_Name ;
        return returnValue;
    },

	// START - (OMNI - 1434)  : BOPIS Page Tagging/Reporting	
	extn_RecordStoreUserAction:function(event,bEvent,ctrl,args)
	{
		 var shipmentDetailModel = null;
         shipmentDetailModel = _scScreenUtils.getModel(this, "ShipmentDetails");
		 var sShipmentNo=_scModelUtils.getStringValueFromPath("Shipment.ShipmentNo",shipmentDetailModel);
		 var sOrderNo=_scModelUtils.getStringValueFromPath("Shipment.OrderNo",shipmentDetailModel);
		 var sDeliveryMethod=_scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod",shipmentDetailModel);
		 var sUserID=_scUserprefs.getUserId();
		 var recordStoreUserActionMashup= _scBaseUtils.getNewModelInstance();
		 _scModelUtils.setStringValueAtModelPath("AcadStoreActionData.ShipmentNo",sShipmentNo,recordStoreUserActionMashup);
		 if(_scBaseUtils.isVoid(sOrderNo)){
			 sOrderNo=_scModelUtils.getStringValueFromPath("Shipment.DisplayOrderNo",shipmentDetailModel);
		 }
		 _scModelUtils.setStringValueAtModelPath("AcadStoreActionData.OrderNo",sOrderNo,recordStoreUserActionMashup);
		 _scModelUtils.setStringValueAtModelPath("AcadStoreActionData.Delivery_Method",sDeliveryMethod,recordStoreUserActionMashup);
		 _scModelUtils.setStringValueAtModelPath("AcadStoreActionData.UserID",sUserID,recordStoreUserActionMashup);	 
		 _iasUIUtils.callApi(
            this, recordStoreUserActionMashup, "extn_RecordStoreUserActionMashup", null);		 
	}	
	// END - (OMNI - 1434)  : BOPIS Page Tagging/Reporting
});
});

