
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/customerpickup/SummaryExtnUI","scbase/loader!ias/utils/PrintUtils", "scbase/loader!ias/utils/RepeatingScreenUtils", "scbase/loader!ias/utils/ContextUtils", "scbase/loader!ias/utils/ScreenUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/RepeatingPanelUtils", "scbase/loader!sc/plat/dojo/utils/ResourcePermissionUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!wsc/components/common/utils/CommonUtils", "scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils", "scbase/loader!wsc/components/shipment/customerpickup/SummaryUI", "scbase/loader!sc/plat/dojo/Userprefs","scbase/loader!sc/plat/dojo/utils/ControllerUtils","scbase/loader!ias/utils/BaseTemplateUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnSummaryExtnUI
			 ,_iasPrintUtils, _iasRepeatingScreenUtils, _iasContextUtils, _iasScreenUtils, _iasUIUtils, _scBaseUtils, _scEventUtils, _scModelUtils, _scRepeatingPanelUtils, _scResourcePermissionUtils, _scScreenUtils, _scWidgetUtils, _wscCommonUtils, _wscShipmentUtils, _wscSummaryUI, _scUserprefs,_scControllerUtils, _iasBaseTemplateUtils
){ 
	return _dojodeclare("extn.components.shipment.customerpickup.SummaryExtn", [_extnSummaryExtnUI],{
	// custom code here
	isCustomerPickedUp: false,
	isShorted: false,
	extn_before_save: function(event, bEvent, ctrl, args) {
		var shipmentModel = _scScreenUtils.getModel(
                this, "ShipmentDetails");
		var rootElement = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
		//OMNI-85085 Start
		var isCurbsideOrder=_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentModel);
		var shipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentModel);
		var curbsideShipmentlineDetails = _scScreenUtils.getModel(this, "extn_curbside");
		var curbsideConsFlag =  _iasContextUtils.getFromContext("CurbsideConsolidationToggle");
		var instoreConsFlag = _iasContextUtils.getFromContext("InStoreConsolidationToggle");
		if(_iasContextUtils.isMobileContainer() && (!_scBaseUtils.isVoid(curbsideConsFlag) && _scBaseUtils.equals(curbsideConsFlag,"Y") &&
		!_scBaseUtils.isVoid(isCurbsideOrder) && _scBaseUtils.equals(isCurbsideOrder,"Y")) || (!_scBaseUtils.isVoid(instoreConsFlag) && _scBaseUtils.equals(instoreConsFlag,"Y"))){
		 var shipmentLines =_scModelUtils.getStringValueFromPath("ShipmentLines",curbsideShipmentlineDetails);
		 if(!_scBaseUtils.isVoid(shipmentLineList))
             {
                for (var i=0;i<shipmentLineList.length;i++) 
                {
                    var shipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", shipmentLineList[i]);
					_scBaseUtils.removeItemFromArray(shipmentLineList, shipmentLineList[i]); 

                }
            }				
			_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines", shipmentLines , shipmentModel);
			shipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentModel);
			//OMNI-102218 - Start
			if (!_scBaseUtils.isVoid(instoreConsFlag) && _scBaseUtils.equals(instoreConsFlag,"Y")){
				var consolidatedShipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ConsolidatedShipmentKey", shipmentModel);
				_scModelUtils.setStringValueAtModelPath("Shipment.ConsolidatedShipmentKey", consolidatedShipmentKey, rootElement);
				var instoreConsToggle = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.InstoreConsolidationFlag", shipmentModel);
				_scModelUtils.setStringValueAtModelPath("Shipment.InstoreConsToggle", instoreConsToggle, rootElement);
			}
			//OMNI-102218 - End
			else if(!_scBaseUtils.isVoid(curbsideConsFlag) && _scBaseUtils.equals(curbsideConsFlag,"Y") && !_scBaseUtils.isVoid(isCurbsideOrder) && _scBaseUtils.equals(isCurbsideOrder,"Y")){
				var curbsideShipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.CurbsideShipmentKey", shipmentModel);
				_scModelUtils.setStringValueAtModelPath("Shipment.CurbsideShipmentKey", curbsideShipmentKey, rootElement);
				var curbsideConsToggle = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.CurbsideConsolidationFlag", shipmentModel);
				_scModelUtils.setStringValueAtModelPath("Shipment.CurbsideConsToggle", curbsideConsToggle, rootElement);
			}
		}
		//OMNI-85085 End
		var shipNode = _scModelUtils.getStringValueFromPath("Shipment.ShipNode", shipmentModel);
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", shipNode, rootElement);
		var shipmentNo = _scModelUtils.getStringValueFromPath("Shipment.ShipmentNo", shipmentModel);
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentNo", shipmentNo, rootElement);
		var shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status", shipmentModel);
		_scModelUtils.setStringValueAtModelPath("Shipment.Status", shipmentStatus, rootElement);
		var deliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", shipmentModel);
		_scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod", deliveryMethod, rootElement);
		var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentModel);
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, rootElement);
		var lShipmentLine = _scModelUtils.getModelObjectFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentModel);
		var sLength = lShipmentLine.length;

		var aShipmentLine = [];

		for(var i=0; i < sLength; i++) {
			var tempShipmentLine={};
			var shipmentLine = lShipmentLine[i];
			var itemId = _scModelUtils.getStringValueFromPath("ItemID", shipmentLine);
			var customerPickedQuantity = _scModelUtils.getStringValueFromPath("CustomerPickedQuantity", shipmentLine);
			var quantity = _scModelUtils.getStringValueFromPath("Quantity", shipmentLine);
			var orderNo = _scModelUtils.getStringValueFromPath("OrderNo", shipmentLine);
			var cancelReason = _scModelUtils.getStringValueFromPath("CancelReason", shipmentLine);
			var shipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", shipmentLine);
			var shipmentKey = _scModelUtils.getStringValueFromPath("ShipmentKey", shipmentLine); //OMNI-85085
			
			var qty = Number(quantity) - Number(customerPickedQuantity);

			_scModelUtils.setStringValueAtModelPath("ItemID", itemId, tempShipmentLine);
			_scModelUtils.setStringValueAtModelPath("CustomerPickedQuantity", customerPickedQuantity, tempShipmentLine);
			if (!_scBaseUtils.isVoid(customerPickedQuantity) && !_scBaseUtils.equals(customerPickedQuantity, "0.00")) {
				this.isCustomerPickedUp = true;
			}
			_scModelUtils.setStringValueAtModelPath("Quantity", quantity, tempShipmentLine);
			_scModelUtils.setStringValueAtModelPath("OrderNo", orderNo, tempShipmentLine);
			_scModelUtils.setStringValueAtModelPath("ActualQuantity", quantity, tempShipmentLine);
			_scModelUtils.setStringValueAtModelPath("ShipmentLineKey", shipmentLineKey, tempShipmentLine);
			_scModelUtils.setStringValueAtModelPath("ShipmentKey", shipmentKey, tempShipmentLine); //OMNI-85085
			// _scModelUtils.setStringValueAtModelPath("ShortageQty", qty, tempShipmentLine);
			_scModelUtils.setStringValueAtModelPath("Extn", {}, tempShipmentLine);
			_scModelUtils.setStringValueAtModelPath("Extn.ExtnReasonCode", cancelReason, tempShipmentLine);

			if(Number(customerPickedQuantity) > 0){
				_scModelUtils.setStringValueAtModelPath("Extn.ExtnMsgToSIM", "N", tempShipmentLine);
				_scModelUtils.setStringValueAtModelPath("ShortageQty", qty.toString(), tempShipmentLine);
			} else {
				_scModelUtils.setStringValueAtModelPath("Extn.ExtnMsgToSIM", "Y", tempShipmentLine);
				_scModelUtils.setStringValueAtModelPath("ShortageQty", quantity, tempShipmentLine);
			}
			if((Number(customerPickedQuantity) > 0) && qty > 0) {
				_scModelUtils.setStringValueAtModelPath("Extn.ExtnMsgToSIM", "Y", tempShipmentLine);
			}
			if(_scModelUtils.getNumberValueFromPath("ShortageQty", tempShipmentLine) > 0) {
				this.isShorted = true;
			}
			aShipmentLine[i]=tempShipmentLine;
		}
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines", {}, rootElement);
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine", aShipmentLine, rootElement);
		if(this.isShorted) {
			_iasUIUtils.callApi(this, rootElement, "extn_UnResrvMsgToSIMOnShrtg", null);
			_scEventUtils.stopEvent(bEvent);
		}
	},

	getFormattedNameDisplay: function(
        dataValue, screen, widget, namespace, modelObj, options) {

	        var sLines = _scModelUtils.getModelObjectFromPath("Shipment.ShipmentLines.ShipmentLine", modelObj);

	        var sKey = _scModelUtils.getModelObjectFromPath("Shipment.ShipmentKey", modelObj);

			var selectedName = _scUserprefs.getProperty(sKey);
			//fix for BOPIS-1751 : Begin
			// if(_scBaseUtils.equals(selectedName,_scModelUtils.getStringValueFromPath("OrderLine.PersonInfoMarkFor.FirstName", sLines[0]))) {
			if (_scBaseUtils.equals(selectedName, "A")) {
			//1751: end
				var nameModel = null;
	            var formattedName = null;
	            nameModel = {};
	            _scModelUtils.setStringValueAtModelPath("FirstName", _scModelUtils.getStringValueFromPath("OrderLine.PersonInfoMarkFor.FirstName", sLines[0]), nameModel);
	            _scModelUtils.setStringValueAtModelPath("LastName", _scModelUtils.getStringValueFromPath("OrderLine.PersonInfoMarkFor.LastName", sLines[0]), nameModel);
	            formattedName = _wscShipmentUtils.getNameDisplay(
	            this, nameModel);
	            formattedName += '  :A';
	            return formattedName;
			} else {
				var nameModel = null;
	            var formattedName = null;
	            nameModel = {};
	            _scModelUtils.setStringValueAtModelPath("FirstName", _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.FirstName", modelObj), nameModel);
	            _scModelUtils.setStringValueAtModelPath("LastName", _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.LastName", modelObj), nameModel);
	            formattedName = _wscShipmentUtils.getNameDisplay(
	            this, nameModel);
	            formattedName += '  :P';
	            return formattedName;
			}
        },

        getDayPhone: function(
        dataValue, screen, widget, namespace, modelObj, options) {
            
        	var sLines = _scModelUtils.getModelObjectFromPath("Shipment.ShipmentLines.ShipmentLine", modelObj);

	        var sKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", modelObj);

			var selectedName = _scUserprefs.getProperty(sKey);
			//fix for BOPIS-1751 : Begin
			// if(_scBaseUtils.equals(selectedName,_scModelUtils.getStringValueFromPath("OrderLine.PersonInfoMarkFor.FirstName", sLines[0]))) {
			if (_scBaseUtils.equals(selectedName, "A")) {
			//fix for BOPIS-1751 : End
				var dataValue = _scModelUtils.getStringValueFromPath("OrderLine.PersonInfoMarkFor.DayPhone", sLines[0]);
				
			} else {
				var dataValue = _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.DayPhone", modelObj);
				
			}

            if (
            _scBaseUtils.isVoid(
            dataValue)) {
                _scWidgetUtils.hideWidget(
                this, "pnlPhoneHolder", true);
            }
            return dataValue;
        },
        getEmailID: function(
        dataValue, screen, widget, namespace, modelObj, options) {

        	var sLines = _scModelUtils.getModelObjectFromPath("Shipment.ShipmentLines.ShipmentLine", modelObj);

	        var sKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", modelObj);

			var selectedName = _scUserprefs.getProperty(sKey);
			//fix for BOPIS-1751 : Begin
			// if(_scBaseUtils.equals(selectedName,_scModelUtils.getStringValueFromPath("OrderLine.PersonInfoMarkFor.FirstName", sLines[0]))) {
			if (_scBaseUtils.equals(selectedName, "A")) {	
			//fix for BOPIS-1751 : End
				dataValue = _scModelUtils.getStringValueFromPath("OrderLine.PersonInfoMarkFor.EMailID", sLines[0]);
				// _scWidgetUtils.setValue(this, "pnlPhoneHolder", EMailID, null);
			} else {
				dataValue = _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.EMailID", modelObj);
				// _scWidgetUtils.setValue(this, "pnlPhoneHolder", EMailID, null)
			}

            if (
            _scBaseUtils.isVoid(
            dataValue)) {
                _scWidgetUtils.hideWidget(
                this, "pnlEmailHolder", true);
            }
            return dataValue;
        },
        save: function(
        event, bEvent, ctrl, args) {
            var shipmentModel = null;
            shipmentModel = _scScreenUtils.getTargetModel(
            this, "recordCustomerPick_input", null);
            shipmentModel = _scBaseUtils.cloneModel(
            shipmentModel);
            var customerVerification = null;
            customerVerification = _iasUIUtils.getWizardModel(
            this, "customerVerificationModel");
            var shipmentDetailModel = _scScreenUtils.getModel(this, "ShipmentDetails");
            var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentDetailModel);
			//OMNI-85085 - Start
			var isCurbsideOrder=_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentDetailModel);
			var curbsideConsToggle = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.CurbsideConsolidationFlag", shipmentDetailModel);
			//OMNI-85085 - End
			var instoreConsToggle = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.InstoreConsolidationFlag", shipmentDetailModel);//OMNI-102218
			
            var pickedUpCustomer = _scUserprefs.getProperty(shipmentKey);
            shipmentModel = _scBaseUtils.mergeModel(
            shipmentModel, customerVerification, false);
            _scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnShipmentPickedBy", pickedUpCustomer, shipmentModel);
            if(this.isCustomerPickedUp) {
				//OMNI-102218 - Start
				if (!_scBaseUtils.isVoid(instoreConsToggle) && _scBaseUtils.equals(instoreConsToggle,"Y")) {
					var consolidatedShipment = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ConsolidatedShipment", shipmentDetailModel);
					_scModelUtils.setStringValueAtModelPath("Shipment.ConsolidatedShipment", consolidatedShipment, shipmentModel);	
					_iasUIUtils.callApi(this, shipmentModel, "extn_recordCustomerPickForCurbsideConsolidation", null);
					}
				//OMNI-102218 - End
				//OMNI-85085 - Start
				else if (!_scBaseUtils.isVoid(curbsideConsToggle) && _scBaseUtils.equals(curbsideConsToggle,"Y")) {
					var curbsideShipment = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.CurbsideShipment", shipmentDetailModel);
					_scModelUtils.setStringValueAtModelPath("Shipment.CurbsideShipment", curbsideShipment, shipmentModel);	
					_iasUIUtils.callApi(this, shipmentModel, "extn_recordCustomerPickForCurbsideConsolidation", null);
				//OMNI-85085 - End
					} else{					
						_iasUIUtils.callApi(this, shipmentModel, "recordCustomerPick", null);
					}
            }
            else {
            	_iasScreenUtils.showInfoMessageBoxWithOk(this, _scScreenUtils.getString(this, "Message_RecordCustomerPickCancel"), "gotoNextScreen", null);
            }
        },
        extn_afterBehaviorMashupCall: function(event, bEvent, ctrl, args) {
        	var mashupRefList = _scModelUtils.getModelObjectFromPath("mashupArray", args);
        	if (!_scBaseUtils.isVoid(mashupRefList)) {
            	for (var i = 0; i < mashupRefList.length; i++) {
	                var mashupRefid = _scModelUtils.getStringValueFromPath("mashupRefId", mashupRefList[i]);
	                if (_scBaseUtils.equals(mashupRefid, "recordCustomerPick")) {
	                	var modelOutput = mashupRefList[i].mashupRefOutput;
	                	// Changes for invoking 'stampInvoiceNoOnBOPISOrders': Begin
	                	var refId = null;
				        refId = [];
				        refId.push("extn_changeShipment_ref");
				        refId.push("extn_stampInvoiceNoOnBOPISOrders_ref");
	                	var sLines = _scModelUtils.getModelObjectFromPath("Shipment.ShipmentLines.ShipmentLine", modelOutput);
	                	var changeShipment_inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
	                	var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", modelOutput);
						_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, changeShipment_inputModel);
						var selectedCustomerName = _scUserprefs.getProperty(shipmentKey);
						if(!_scBaseUtils.isVoid(sLines)) {
							//fix for BOPIS-1751 : Begin
							// if (_scBaseUtils.equals(selectedCustomerName,_scModelUtils.getStringValueFromPath("OrderLine.PersonInfoMarkFor.FirstName", sLines[0]))) {
							if (_scBaseUtils.equals(selectedCustomerName, "A")) {
							//fix for BOPIS-1751 : End
								_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnShipmentPickedBy", "Alternate", changeShipment_inputModel);
							} else {
								_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnShipmentPickedBy", "Primary", changeShipment_inputModel);
							}
						}
						var date = new Date();
						  var yyyy = date.getFullYear().toString();
						  var mm = (date.getMonth()+1).toString();
						  var dd  = date.getDate().toString();

						  var mmChars = mm.split('');
						  var ddChars = dd.split('');

						  date = yyyy + '-' + (mmChars[1]?mm:"0"+mmChars[0]) + '-' + (ddChars[1]?dd:"0"+ddChars[0]);
						_scModelUtils.setStringValueAtModelPath("Shipment.AdditionalDates.AdditionalDate.ActualDate", date, changeShipment_inputModel);
						// _iasUIUtils.callApi(this, inputModel, "extn_changeShipment_ref", null);
						var stampInvoiceNo_inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
						_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, stampInvoiceNo_inputModel);
						_scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod", _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", modelOutput), stampInvoiceNo_inputModel);
						var target = null;
				        target = [];
				        target.push(
				        changeShipment_inputModel);
				        target.push(
				        stampInvoiceNo_inputModel);
				        var mashupContext = null;
				        mashupContext = _scControllerUtils.getMashupContext(this);
				        _iasUIUtils.callApis(this, target, refId, mashupContext, null);
				        // Changes for invoking 'stampInvoiceNoOnBOPISOrders': Begin
	                }
	                if (_scBaseUtils.equals(mashupRefid, "extn_UnResrvMsgToSIMOnShrtg")) {
	                	// _iasScreenUtils.showInfoMessageBoxWithOk(this, _scScreenUtils.getString(this, "Message_RecordCustomerPickCancel"), "gotoNextScreen", null);
	                	this.save();
	                }
	                //BOPIS-1186: Begin
	                if (_scBaseUtils.equals(mashupRefid, "extn_AcademyBOPISPrintAckSlip_ref")) {
	                	this.gotoNextScreen();
	                }
	                //BOPIS-1186: End
	            }
	        }
        },

        afterScreenInit: function() {
        	var sDetails = _scScreenUtils.getModel(this, "ShipmentDetails");

			var shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status", sDetails);
			if(_scBaseUtils.equals(shipmentStatus, "1100.70.06.30.5") || _scBaseUtils.equals(shipmentStatus, "1100.70.06.30.7")) {

				var aDate = _scModelUtils.getStringValueFromPath("Shipment.AdditionalDates", sDetails);
				if(!_scBaseUtils.isVoid(aDate.AdditionalDate)){
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
		
		 
	handleMashupOutput: function(
        mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
           //OMNI-85085 - Added new mashup
            if (_scBaseUtils.equals(mashupRefId, "recordCustomerPick") ||  _scBaseUtils.equals(
            mashupRefId, "extn_recordCustomerPickForCurbsideConsolidation")) {
                this.handleRecordCustomerPick(
                modelOutput);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "printAcknowledgement")) {
                this.handlePrintAcknowledgement(
                modelOutput);
            }
            if (_scBaseUtils.equals(mashupRefId, "extn_getUserListByUserID")) {
	                	var userName = modelOutput.UserList.User[0].Username;
						var shipmentData = null;
						shipmentData = _scScreenUtils.getModel(this, "ShipmentDetails");
						_scModelUtils.setStringValueAtModelPath("Shipment.AssignedToUserId", userName, shipmentData);
						 _scScreenUtils.setModel(
							this, "ShipmentDetails", shipmentData, null); 
	                }
        },
    //overriden OOTB method to invoke custom print ack service as a part of BOPIS-1186: Begin
    printPickupAcknowledgement: function(res, args) {
        if (_scBaseUtils.equals(res, "Ok")) {
            var printAckModel = null;
            printAckModel = _scScreenUtils.getTargetModel(
            this, "printAcknowledgement_input", null);
            // _iasUIUtils.callApi(
            // this, printAckModel, "printAcknowledgement", null);
            var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
            var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", printAckModel);
            _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, inputModel);
            _iasUIUtils.callApi(this, inputModel, "extn_AcademyBOPISPrintAckSlip_ref", null);
        } else {
            this.gotoNextScreen();
        }
    },
    //BOPIS-1186: end
	
	//BOPIS-1630: Overridden this OOB method to hide empty dropdown in Products Panel in Customer Pick flow - begin
	initializeProductTabs : function(event, bEvent, ctrl, args){
		var productTabsModel = { "Tabs":{"Tab":[]}};			
		var producttabs = _scScreenUtils.getWidgetByUId(this,"tabPnl").getChildren();
		for(var i=0; i<producttabs.length;i++){
			var tab = {
				"TabUId" : producttabs[i].uId,
				"TabDescription" : producttabs[i].value					
			};
			if(!producttabs[i].isHidden){
				productTabsModel.Tabs.Tab.push(tab);
			}			
		}
		//BOPIS-1630: added OR condition below to hide empty dropdown in case all products were shorted during customer pickup
		if(productTabsModel.Tabs.Tab.length===1 || productTabsModel.Tabs.Tab.length===0){
			_scWidgetUtils.addClass(this,"tabFilterSelectContainer","singleTab");
		}	
		_scScreenUtils.setModel(this, "ProductTabs", productTabsModel, null);
		_scScreenUtils.setModel(this, "currentProductTab", {"initTabUId":this.currentView}, null);						
	},
	//BOPIS-1630: Overridden this OOB method to hide empty dropdown in Products Panel in Customer Pick flow - end
	
});
});