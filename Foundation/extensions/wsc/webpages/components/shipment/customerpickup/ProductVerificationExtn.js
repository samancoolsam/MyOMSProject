
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!wsc/mobile/home/utils/MobileHomeUtils","scbase/loader!extn/components/shipment/customerpickup/ProductVerificationExtnUI","scbase/loader!ias/utils/ContextUtils", "scbase/loader!ias/utils/EventUtils", "scbase/loader!ias/utils/RepeatingScreenUtils", "scbase/loader!ias/utils/ScreenUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!ias/utils/WizardUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/RepeatingPanelUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!wsc/components/common/utils/CommonUtils", "scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils", "scbase/loader!wsc/components/shipment/customerpickup/ProductVerificationUI","scbase/loader!ias/utils/BaseTemplateUtils",
"scbase/loader!sc/plat/dojo/Userprefs",		
"scbase/loader!sc/plat/dojo/utils/ControllerUtils", "scbase/loader!wsc/components/shipment/customerpickup/utils/CustomerPickUtils"]
,
function(			 
			    _dojodeclare
			 ,_wscMobileHomeUtils,
			    _extnProductVerificationExtnUI
			 ,
			 _iasContextUtils, _iasEventUtils, _iasRepeatingScreenUtils, _iasScreenUtils, _iasUIUtils, _iasWizardUtils, _scBaseUtils, _scEventUtils,
			  _scModelUtils, _scRepeatingPanelUtils, _scScreenUtils, _scWidgetUtils, _wscCommonUtils, _wscShipmentUtils, _wscProductVerificationUI, 
			  _iasBaseTemplateUtils, _scUserprefs, _scControllerUtils, _wscCustomerPickUtils
){ 
	return _dojodeclare("extn.components.shipment.customerpickup.ProductVerificationExtn", [_extnProductVerificationExtnUI],{
	// custom code here
    isAgeRestrictedSet: false,
    maxAgeRestrictionValue: 0,
    isCalledFromPickAll: false,
    isFullLineShorted: false,
    allShipmentLinesNotShorted: false,
	isCustomerPickedUp: false,
	isShorted: false,
	curbsideTimeDelays: 0, // OMNI-79565
    afterBehaviorMashupCall: function(event, bEvent, ctrl, args) {
            var mashupArrayList = _scModelUtils.getModelObjectFromPath("mashupArray", args);
            var mashupArrayListLength = Object.keys(mashupArrayList).length;
			// checking status for paper work button - Begin
            for(var iCount = 0; iCount < mashupArrayListLength; iCount++) {
                var mashupArray = mashupArrayList[iCount];
                var mashupRefId = _scModelUtils.getModelObjectFromPath("mashupRefId", mashupArray);
				//OMNI-72389 Curbside customer pick up screen UI – Notification of related orders  START
				 if(_scBaseUtils.equals(mashupRefId, "extn_NotifyRelatedOrders")) {
					   var mashupOutputObject = _scModelUtils.getModelObjectFromPath("mashupRefOutput", mashupArray);
					   var notifyRelatedOrders = _scModelUtils.getStringValueFromPath("Shipment.ShowRelatedOrders", mashupOutputObject);
					    if(!_scBaseUtils.isVoid(notifyRelatedOrders) && _scBaseUtils.equals(notifyRelatedOrders,"Y")) {
							_scWidgetUtils.showWidget(this, 'extn_lblRelatedOrders');
						}
           
             //OMNI-90544 START
            var shipmentDetailModel = _scScreenUtils.getModel(this, "ShipmentDetails");
		    var state=_scModelUtils.getStringValueFromPath("Shipment.FromAddress.State",shipmentDetailModel);
		    var shipmentType=_scModelUtils.getStringValueFromPath("Shipment.ShipmentType",shipmentDetailModel);
			var packListType=_scModelUtils.getStringValueFromPath("Shipment.PackListType",shipmentDetailModel);
			var ccDisableFPBtnFlag = _scModelUtils.getStringValueFromPath("Shipment.CodeShortDescription", mashupOutputObject);	
            var ccRestrictStateList = _scModelUtils.getStringValueFromPath("Shipment.CodeLongDescription", mashupOutputObject);

			var isRestrictedstate=true;	
			//OMNI-93398 START -Disabling complete order button for BOPIS firearm
				if(!_scBaseUtils.isVoid(ccRestrictStateList) &&	!_scBaseUtils.isVoid(state) && _scBaseUtils.equals(ccDisableFPBtnFlag,"Y")){
                    var rState = ccRestrictStateList.split(",");
                    for(var k = 0 ;k < rState.length; k++){
				        var check=rState[k];
                     if(_scBaseUtils.equals(state,check)){
                             isRestrictedstate=false ;
						      break;
					 }}
				  if(((!_scBaseUtils.isVoid(packListType) && _scBaseUtils.equals(packListType,"FA"))|| (_scBaseUtils.equals(shipmentType,"SOF"))) 
					   && (isRestrictedstate)) {
						_scWidgetUtils.hideWidget(this, 'extn_FinishPickupButton');
						_scWidgetUtils.hideWidget(this, 'extn_FinishPickUpButton1');
						_scWidgetUtils.hideWidget(this, "pnlMessage", true);
						_scWidgetUtils.hideWidget(this, 'extn_FinishPickuButton');
						_scWidgetUtils.hideWidget(this, 'extn_FinishPickupButton1');
						//OMNI-91122 Disabe Pick-Up Button for STS FA WEBSOM (Except FL, IL) - Start
						if(!_iasContextUtils.isMobileContainer()) {
							_scWidgetUtils.hideWidget(this, 'extn_FinishPick_button');
							_scWidgetUtils.hideWidget(this, 'extn_FinishPick_button2new');
						}
						//OMNI-91122 Disabe Pick-Up Button for STS FA WEBSOM (Except FL, IL) - End
			     }
				}
			//OMNI-90544 END
           //OMNI-93398 END
				 }
				 //OMNI-72389 Curbside customer pick up screen UI – Notification of related orders  END
                if(_scBaseUtils.equals(mashupRefId, "getShortageShipmentLineListBehv")) {
                    //OMNI-88832 - Show 'Begin FireArm' button until all RFCP are moved to PWI - Start
					var curbsideConsFlag =  _iasContextUtils.getFromContext("CurbsideConsolidationToggle");
					var shipmentDetailModel = _scScreenUtils.getModel(this, "ShipmentDetails");
					var vExtnIsCurbsidePickupOpted = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentDetailModel);
					if(!_scBaseUtils.isVoid(curbsideConsFlag) && _scBaseUtils.equals(curbsideConsFlag,"Y") && _iasContextUtils.isMobileContainer() && (_scBaseUtils.equals(vExtnIsCurbsidePickupOpted, "Y"))) {
						var shipmentLines =_scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentDetailModel);
						var atleastOneRFCP = "";
						for (var i=0; i < shipmentLines.length; i++) {
							var Shipment = shipmentLines[i].Shipment[0];
							var status = _scModelUtils.getStringValueFromPath("Status", Shipment);
							if(_scBaseUtils.equals(status,"1100.70.06.30.5")) {
								atleastOneRFCP = "Y";
							}
						}
						if(_scBaseUtils.equals(atleastOneRFCP, "Y")) {
                                _scWidgetUtils.showWidget(this, 'extn_customer_piickup_button');
						}
					} else {
						var mashupOutputObject = _scModelUtils.getModelObjectFromPath("mashupRefOutput", mashupArray);
						var output = _scModelUtils.getModelObjectFromPath("Page.Output.ShipmentLines.ShipmentLine" ,mashupOutputObject);
						
						if(!_scBaseUtils.isVoid(output)) {
							var outputLength = output.length;

							for(var i=0; i < outputLength; i++) {
								var Shipment = output[i].Shipment[0];
								// var isFireArm = _scModelUtils.getStringValueFromPath("Extn.ExtnIsBOPISFirearm", Shipment);
								var status = _scModelUtils.getStringValueFromPath("Status", Shipment);
								if(_scBaseUtils.equals(status,"1100.70.06.30.5")) {
									_scWidgetUtils.showWidget(this, 'extn_customer_piickup_button');
								}
							}
						}
					}
                    //OMNI-88832 - Show 'Begin FireArm' button until all RFCP are moved to PWI - End
                }
				// checking status for paper work button - End

				//Paper Work Service -begin
                if(_scBaseUtils.equals(mashupRefId, 'extn_startPaperWork_ref')) {
                    var mashupOutputObject = _scModelUtils.getModelObjectFromPath("mashupRefOutput", mashupArray);
                    var shipment = _scModelUtils.getStringValueFromPath("Shipment", mashupOutputObject);

                    if(shipment.ShipmentKey && shipment.ShipmentNo) {
                        _scWidgetUtils.hideWidget(this, 'extn_customer_piickup_button');
                    }

                    _iasScreenUtils.showInfoMessageBoxWithOk(this, "Paper work Initiated", "infoCallBack", null);
                }
				//Paper Work Service -begin
				//Age Verfication Changes : Begin
                if(_scBaseUtils.equals(mashupRefId, 'updateShipmentLineForShortage') || _scBaseUtils.equals(mashupRefId, 'shortRemainingShipmentLines') || _scBaseUtils.equals(mashupRefId, 'extn_changeShipmentForCurbsideConsoldiation') ) {
                    var mashupOutputObject = _scModelUtils.getModelObjectFromPath("mashupRefOutput", mashupArray);
                    var shortedShipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", mashupOutputObject);
                    var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");
                    var shipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentDetails);
                    if (!_scBaseUtils.isVoid(shortedShipmentLineList)) {
                        for (var i=0;i<shortedShipmentLineList.length;i++) {
                            var shortedShipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", shortedShipmentLineList[i]);
                            var quantity = _scModelUtils.getNumberValueFromPath("Quantity", shortedShipmentLineList[i]);
                            var customerPickedQuantity = _scModelUtils.getNumberValueFromPath("CustomerPickedQuantity", shortedShipmentLineList[i]);
                            var shortageQty = _scModelUtils.getNumberValueFromPath("ShortageQty", shortedShipmentLineList[i]);
                            if (!_scBaseUtils.isVoid(shipmentLineList)) {
                                for (var j=0;j<shipmentLineList.length;j++) {
                                    var shipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", shipmentLineList[j]);
                                    if (_scBaseUtils.equals(shipmentLineKey, shortedShipmentLineKey)) {
                                        _scModelUtils.setStringValueAtModelPath("CustomerPickedQuantity", customerPickedQuantity, shipmentLineList[j]);
                                        _scModelUtils.setStringValueAtModelPath("ShortageQty", shortageQty, shipmentLineList[j]);
                                        if ((quantity - shortageQty) == 0) {
                                            this.isFullLineShorted = true
                                            _scModelUtils.setStringValueAtModelPath("isFullLineShorted", "Y", shipmentLineList[j]);
                                        }
                                    }
                                }
                                if (this.isFullLineShorted) {
                                    _scScreenUtils.setModel(this, "ShipmentDetails", shipmentDetails, null);
                                    this.allShipmentLinesNotShorted = true;
                                }
                            }
                        }
                    }
                }
                 if(_scBaseUtils.equals(mashupRefId, 'updateShipmentLine')) {
                    var mashupOutputObject = _scModelUtils.getModelObjectFromPath("mashupRefOutput", mashupArray);
                    var shortedShipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", mashupOutputObject);
                    var shortedShipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", shortedShipmentLineList[0]);
                    var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");
                    var shipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentDetails);
                    if (!_scBaseUtils.isVoid(shipmentLineList)) {
                        for (var i=0;i<shipmentLineList.length;i++) {
                            var shipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", shipmentLineList[i]);
                            if (_scBaseUtils.equals(shipmentLineKey, shortedShipmentLineKey)) {
                                var quantity = _scModelUtils.getNumberValueFromPath("Quantity", shortedShipmentLineList[0]);
                                var customerPickedQuantity = _scModelUtils.getNumberValueFromPath("CustomerPickedQuantity", shortedShipmentLineList[0]);
                                if ((quantity - customerPickedQuantity) >= 0) {
                                    this.isFullLineShorted = false;
                                    _scModelUtils.setStringValueAtModelPath("isFullLineShorted", "N", shipmentLineList[i]);
                                }
                            }
                        }
                        if (!this.isFullLineShorted) {
                            _scScreenUtils.setModel(this, "ShipmentDetails", shipmentDetails, null)
                            this.maxAgeRestrictionValue = 0;
                            this.allShipmentLinesNotShorted = true;
                        }
                    }
                }
				// Age Verification Changes: End
				//BOPIS-2033_CR: Change the BOPIS order pickup flow - begin
				if (_scBaseUtils.equals(mashupRefId, "extn_customerpickup_recordCustomerPickup_Ref")) {
					var modelOutput = _scModelUtils.getModelObjectFromPath("mashupRefOutput", mashupArray);
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
				
				if (_scBaseUtils.equals(mashupRefId, "extn_AgeVerifiedChangeShipment_ref")) {
                    var modelOutput = _scModelUtils.getModelObjectFromPath("mashupRefOutput", mashupArray);

                     this.validateShipmentOnNext();
                }
				//BOPIS-2033_CR: Change the BOPIS order pickup flow - end
            }
       },

       infoCallBack: function(res) {
        
       },
	  //OMNI-85081 - Start - Overriding OOB method
	     allItems_onShow: function(
        event, bEvent, ctrl, args) {
            var shipmentLineModel = null;
            shipmentLineModel = _scModelUtils.createNewModelObjectWithRootKey("ShipmentLine");
			var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");		
            this.activeRepeatingPanelUId = "readyForPickupLineList";
            
			var isCurbsideOrder=_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentDetails);
			var curbsideConsFlag = _iasContextUtils.getFromContext("CurbsideConsolidationToggle");
			var instoreConsFlag = _iasContextUtils.getFromContext("InStoreConsolidationToggle");
			var orderNo = shipmentDetails.Shipment.OrderNo;					
			var shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status", shipmentDetails);
			//OMNI-87912 - Added PWI Status in below if condition
			/**START**/
			var vPackListType = _scModelUtils.getStringValueFromPath("Shipment.PackListType",shipmentDetails);
			var vShipmentType = _scModelUtils.getStringValueFromPath("Shipment.ShipmentType",shipmentDetails);
			if (_iasContextUtils.isMobileContainer() && (_scBaseUtils.equals(shipmentStatus, "1100.70.06.30.5") || _scBaseUtils.equals(shipmentStatus, "1100.70.06.30.7"))){
				if((!_scBaseUtils.isVoid(vPackListType) && _scBaseUtils.equals(vPackListType,"FA")) || (!_scBaseUtils.isVoid(vShipmentType) && _scBaseUtils.equals(vShipmentType,"SOF")) || (!_scBaseUtils.isVoid(instoreConsFlag) && _scBaseUtils.equals(instoreConsFlag,"N") && !_scBaseUtils.isVoid(curbsideConsFlag) && _scBaseUtils.equals(curbsideConsFlag,"N"))){
					var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentDetails);
					_scModelUtils.setStringValueAtModelPath("ShipmentLine.ShipmentKey", shipmentKey ,shipmentLineModel);
				}else if(!_scBaseUtils.isVoid(instoreConsFlag) && _scBaseUtils.equals(instoreConsFlag,"Y")){
						shipmentLineModel.ShipmentLine = {};
						shipmentLineModel.ShipmentLine.Shipment = {};
						shipmentLineModel.ShipmentLine.OrderNo = orderNo;
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery={};
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Operator='AND';
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or=[];
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0]={};
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp=[];					
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[0]={};
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[0].Value='1100.70.06.30.5';	
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[0].Name='Status';						
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[0].QryType='EQ';	
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[1]={};
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[1].Name='Status';						
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[1].QryType='EQ';						
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[1].Value='1100.70.06.30.7';
						shipmentLineModel.ShipmentLine.Shipment.PackListType='FA';
						shipmentLineModel.ShipmentLine.Shipment.PackListTypeQryType='NE';
						shipmentLineModel.ShipmentLine.Shipment.ShipmentType='SOF';
						shipmentLineModel.ShipmentLine.Shipment.ShipmentTypeQryType='NE';				
				}else if(!_scBaseUtils.isVoid(curbsideConsFlag) && _scBaseUtils.equals(curbsideConsFlag,"Y")
								&& !_scBaseUtils.isVoid(isCurbsideOrder) && _scBaseUtils.equals(isCurbsideOrder,"Y")){
						shipmentLineModel.ShipmentLine = {};
						shipmentLineModel.ShipmentLine.Shipment = {};
						shipmentLineModel.ShipmentLine.OrderNo = orderNo;
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery={};
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Operator='AND';
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or=[];
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0]={};
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp=[];					
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[0]={};
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[0].Value='1100.70.06.30.5';	
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[0].Name='Status';						
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[0].QryType='EQ';	
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[1]={};
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[1].Name='Status';						
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[1].QryType='EQ';						
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[1].Value='1100.70.06.30.7';
					_scModelUtils.setStringValueAtModelPath("ShipmentLine.Shipment.Extn.ExtnIsCurbsidePickupOpted",'Y' , shipmentLineModel);
				}else{
					var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentDetails);
					_scModelUtils.setStringValueAtModelPath("ShipmentLine.ShipmentKey", shipmentKey ,shipmentLineModel);
				}
			}else{
					var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentDetails);
					_scModelUtils.setStringValueAtModelPath("ShipmentLine.ShipmentKey", shipmentKey ,shipmentLineModel);
			}				
			/**END */
            _scRepeatingPanelUtils.startPaginationUsingUId(
            this, "readyForPickupLineList", "getReadyForPickLines", shipmentLineModel, "getShipmentLineListBehv");
        },
		//OMNI-85081 - End
		//OMNI-85085 - Start
		shortItems_onShow: function(
        event, bEvent, ctrl, args) {
            this.activeRepeatingPanelUId = "unpickedLineList";
            var shipmentLineModel = null;
            shipmentLineModel = _scModelUtils.createNewModelObjectWithRootKey("ShipmentLine");
			var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");	
			var isCurbsideOrder=_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentDetails);
			var curbsideConsFlag = _iasContextUtils.getFromContext("CurbsideConsolidationToggle");
			var instoreConsFlag = _iasContextUtils.getFromContext("InStoreConsolidationToggle");
			var shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status", shipmentDetails);
			var orderNo = shipmentDetails.Shipment.OrderNo;
			/**START**/
			var vPackListType = _scModelUtils.getStringValueFromPath("Shipment.PackListType",shipmentDetails);
			var vShipmentType = _scModelUtils.getStringValueFromPath("Shipment.ShipmentType",shipmentDetails);
			if (_iasContextUtils.isMobileContainer() && (_scBaseUtils.equals(shipmentStatus, "1100.70.06.30.5") || _scBaseUtils.equals(shipmentStatus, "1100.70.06.30.7"))){
				if((!_scBaseUtils.isVoid(vPackListType) && _scBaseUtils.equals(vPackListType,"FA")) || (!_scBaseUtils.isVoid(vShipmentType) && _scBaseUtils.equals(vShipmentType,"SOF")) || (!_scBaseUtils.isVoid(instoreConsFlag) && _scBaseUtils.equals(instoreConsFlag,"N") && !_scBaseUtils.isVoid(curbsideConsFlag) && _scBaseUtils.equals(curbsideConsFlag,"N"))){
					var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentDetails);
					_scModelUtils.setStringValueAtModelPath("ShipmentLine.ShipmentKey", shipmentKey ,shipmentLineModel);
				}else if(!_scBaseUtils.isVoid(instoreConsFlag) && _scBaseUtils.equals(instoreConsFlag,"Y")){
						shipmentLineModel.ShipmentLine = {};
						shipmentLineModel.ShipmentLine.Shipment = {};
						shipmentLineModel.ShipmentLine.OrderNo = orderNo;
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery={};
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Operator='AND';
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or=[];
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0]={};
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp=[];					
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[0]={};
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[0].Value='1100.70.06.30.5';	
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[0].Name='Status';						
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[0].QryType='EQ';	
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[1]={};
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[1].Name='Status';						
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[1].QryType='EQ';						
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[1].Value='1100.70.06.30.7';
						shipmentLineModel.ShipmentLine.Shipment.PackListType='FA';
						shipmentLineModel.ShipmentLine.Shipment.PackListTypeQryType='NE';
						shipmentLineModel.ShipmentLine.Shipment.ShipmentType='SOF';
						shipmentLineModel.ShipmentLine.Shipment.ShipmentTypeQryType='NE';				
				}else if(!_scBaseUtils.isVoid(curbsideConsFlag) && _scBaseUtils.equals(curbsideConsFlag,"Y")
								&& !_scBaseUtils.isVoid(isCurbsideOrder) && _scBaseUtils.equals(isCurbsideOrder,"Y")){
						shipmentLineModel.ShipmentLine = {};
						shipmentLineModel.ShipmentLine.Shipment = {};
						shipmentLineModel.ShipmentLine.OrderNo = orderNo;
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery={};
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Operator='AND';
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or=[];
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0]={};
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp=[];					
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[0]={};
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[0].Value='1100.70.06.30.5';	
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[0].Name='Status';						
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[0].QryType='EQ';	
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[1]={};
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[1].Name='Status';						
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[1].QryType='EQ';						
						shipmentLineModel.ShipmentLine.Shipment.ComplexQuery.Or[0].Exp[1].Value='1100.70.06.30.7';
					_scModelUtils.setStringValueAtModelPath("ShipmentLine.Shipment.Extn.ExtnIsCurbsidePickupOpted",'Y' , shipmentLineModel);
				}else{
					var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentDetails);
					_scModelUtils.setStringValueAtModelPath("ShipmentLine.ShipmentKey", shipmentKey ,shipmentLineModel);
				}
			}else{
					var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentDetails);
					_scModelUtils.setStringValueAtModelPath("ShipmentLine.ShipmentKey", shipmentKey ,shipmentLineModel);
			}	
			/**END */
            _scRepeatingPanelUtils.startPaginationUsingUId(
            this, "unpickedLineList", "getShortageLines", shipmentLineModel, "getShortageShipmentLineListBehv");
        },
		//OMNI-85085 - End
	//Paper Work button on-click -Begin
    extn_customer_piickup_button_onClick: function() {
		
		var argBean = null;
		argBean = {};
		var msg = _scScreenUtils.getString(this, "extn_BeginFireArm_Confirmation");
		_scScreenUtils.showConfirmMessageBox(this, msg, "extn_ConfirmBeginFireArm", null, argBean);
	},
	//Paper Work button on-click -End

	/* This method is called after clicking on Yes/No options in Begin FireArm Paper work message box */
	extn_ConfirmBeginFireArm: function(result, args) 
	{
		if (_scBaseUtils.equals(result, "Ok"))
		{
			var shipmentDetailModel = _scScreenUtils.getModel(this, "ShipmentDetails");
			var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentDetailModel);
			var inputToMashup = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
			//OMNI-85830 START
			var curbsideShipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.CurbsideShipmentKey",shipmentDetailModel);
			//OMNI-102404--START
			var consolidateShipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ConsolidatedShipmentKey",shipmentDetailModel);			
			if(!_scBaseUtils.isVoid(consolidateShipmentKey)){
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey",consolidateShipmentKey,inputToMashup);
				}
			else if(!_scBaseUtils.isVoid(curbsideShipmentKey)){
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey",curbsideShipmentKey,inputToMashup);
				}
			if(_iasContextUtils.isMobileContainer() && !_scBaseUtils.isVoid(curbsideShipmentKey) || !_scBaseUtils.isVoid(consolidateShipmentKey)){
				//OMNI-88832 - For RFCP + PWI combo, do 'start paper work' only for RFCP shipments. - Start
				if(_scBaseUtils.contains(curbsideShipmentKey,",") || _scBaseUtils.contains(consolidateShipmentKey,",")) {
					var shipmentLines =_scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentDetailModel);
					var consolidateShipmentKeyFinal = "";
					var shipKey = "";
					for (var i=0; i < shipmentLines.length; i++) {
						var Shipment = shipmentLines[i].Shipment[0];
						var status = _scModelUtils.getStringValueFromPath("Status", Shipment);
						shipKey = _scModelUtils.getStringValueFromPath("ShipmentKey", Shipment);
						if(_scBaseUtils.equals(status,"1100.70.06.30.5") && !_scBaseUtils.contains(consolidateShipmentKeyFinal,shipKey)) {
							consolidateShipmentKeyFinal = consolidateShipmentKeyFinal + shipKey + ",";
						}
					}
					var length = consolidateShipmentKeyFinal.length;
					curbsideShipmentKey = consolidateShipmentKeyFinal.substring(0, length -1);
				}
				//OMNI-88832 - Process 'Begin FireArm' only for RFCP shipments - End
				//OMNI-102404---END
			}else{			
			_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey ,inputToMashup);
			}
			//OMNI-85830 END
			_iasUIUtils.callApi(this, inputToMashup, "extn_startPaperWork_ref", null);
		}
	}, 
  //Combining Customer Pickup screen OMNI-8660 -Start
	popupCloseConfirmation: function(res) {
            if (_scBaseUtils.equals(res, "Ok")) {
                _scWidgetUtils.closePopup(
                    this, "CLOSE", false);
            }
          
             if (_scBaseUtils.equals(res, "Cancel")) {
            _iasScreenUtils.showInfoMessageBoxWithOk(this, "The customer does not meet the age requirements to purchase this item. Please cancel each age restricted item within the order by selecting 'Age Verification' as the short pick reason.", "infoCallBack", null);
        }
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
  //Combining Customer Pickup screen OMNI-8660 - END
    extn_afterScreenInit: function(event, bEvent, ctrl, args) {
        var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");
		//OMNI-72389 Curbside customer pick up screen UI – Notification of related orders  START
		_scWidgetUtils.hideWidget(this, 'extn_lblRelatedOrders');
		_scWidgetUtils.hideWidget(this, 'extn_customer_piickup_button');
		var shipmentNo = shipmentDetails.Shipment.ShipmentNo;
		var orderNo = shipmentDetails.Shipment.OrderNo;
		var shipNode = shipmentDetails.Shipment.ShipNode;
		var shipmentStatus = shipmentDetails.Shipment.Status;
		var emailID = shipmentDetails.Shipment.BillToAddress.EMailID;
		var getRelatedOrdersInput = {};				
		getRelatedOrdersInput = _scModelUtils.createModelObjectFromKey("Shipment", getRelatedOrdersInput);
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentNo", shipmentNo , getRelatedOrdersInput);
		_scModelUtils.setStringValueAtModelPath("Shipment.OrderNo",orderNo , getRelatedOrdersInput);
		_scModelUtils.setStringValueAtModelPath("Shipment.EmailID", emailID , getRelatedOrdersInput);
		_scModelUtils.setStringValueAtModelPath("Shipment.Status",shipmentStatus , getRelatedOrdersInput);
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipNode",shipNode , getRelatedOrdersInput);
		_iasUIUtils.callApi(this, getRelatedOrdersInput, "extn_NotifyRelatedOrders", null); 
		//OMNI-72389 Curbside customer pick up screen UI – Notification of related orders  END
        var shipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentDetails);
        var shipmentLine = shipmentLineList[0];
        var paymentInfo = _scModelUtils.getStringValueFromPath("Order.PaymentStatus", shipmentLine);
		var isCurbsideOrder=_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentDetails);
		
		//OMNI-85179 Start
        //OMNI-79668 - Curbside Initiated At Date Time Display - Start 
		if(!_scBaseUtils.isVoid(isCurbsideOrder) && _scBaseUtils.equals(isCurbsideOrder,"Y")){
			var appointmentNo = _scModelUtils.getStringValueFromPath("Shipment.AppointmentNo",shipmentDetails);
			if(!_scBaseUtils.isVoid(appointmentNo)) {
				var date = new Date(appointmentNo.replace(/^(\d{4})(\d\d)(\d\d)(\d\d)(\d\d)(\d\d)$/,'$4:$5:$6 $2/$3/$1'));
				var dateInLocaleString = date.toLocaleTimeString([], {year: 'numeric', month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit'});
				_scWidgetUtils.showWidget(this,"extn_CurbsideInitiatedAt");	
				_scWidgetUtils.setValue(this, "extn_CurbsideInitiatedAt", dateInLocaleString, false);	
			}			
		}	
		//OMNI-79668 - Curbside Initiated At Date Time Display - End
        
		this.curbsideTimeDelays = _scScreenUtils.getModel(this, "extn_CurbsideCommomCodeList"); // OMNI-79565
		//OMNI-85079 Start
         var curbsideShipmentlineDetails = _scScreenUtils.getModel(this, "extn_curbside");
        var curbsideConsFlag =  _iasContextUtils.getFromContext("CurbsideConsolidationToggle");
		var instoreConsFlag = _iasContextUtils.getFromContext("InStoreConsolidationToggle");
		var curbsideConsoldOutFlag = _scModelUtils.getStringValueFromPath("ShipmentLines.CurbsideConsolidationFlag",curbsideShipmentlineDetails);
		var instoreConsoldOutFlag = _scModelUtils.getStringValueFromPath("ShipmentLines.InstoreConsolidationFlag",curbsideShipmentlineDetails);
        //OMNI-83422 - Assign Team Member - Curbside Consolidation - Start
		var attendedBy =_scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine.0.Shipment.0.Extn.ExtnCurbsideAttendedBy", curbsideShipmentlineDetails);
		if(!_scBaseUtils.isVoid(attendedBy)) {
			_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnCurbsideAttendedBy", attendedBy, shipmentDetails);
			attendedBy =_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnCurbsideAttendedBy", shipmentDetails);
		}
		if(!_scBaseUtils.isVoid(attendedBy) && _iasContextUtils.isMobileContainer()) {
			_scWidgetUtils.setValue(this, "extn_Attended_by", attendedBy, null);
		}
		//OMNI-83422 - Assign Team Member - Curbside Consolidation - End
		if(!_scBaseUtils.isVoid(curbsideConsFlag) && _scBaseUtils.equals(curbsideConsFlag,"Y") && !_scBaseUtils.isVoid(isCurbsideOrder) &&
		_scBaseUtils.equals(isCurbsideOrder,"Y") && !_scBaseUtils.isVoid(curbsideConsoldOutFlag) && _scBaseUtils.equals(curbsideConsoldOutFlag,"Y") || 
		(!_scBaseUtils.isVoid(instoreConsFlag) && _scBaseUtils.equals(instoreConsFlag,"Y") && !_scBaseUtils.isVoid(instoreConsoldOutFlag) && 
		_scBaseUtils.equals(instoreConsoldOutFlag,"Y"))){
         /* OMNI-81395 - Start */
        var consolidatedShipmentKey =_scModelUtils.getStringValueFromPath("ShipmentLines.CurbsideShipmentKey", curbsideShipmentlineDetails);
		if(!_scBaseUtils.isVoid(consolidatedShipmentKey)) {
			_scModelUtils.setStringValueAtModelPath("Shipment.ConsolidatedShipmentKey", consolidatedShipmentKey , shipmentDetails);
		}else{
			consolidatedShipmentKey =_scModelUtils.getStringValueFromPath("ShipmentLines.ConsolidatedShipmentKey", curbsideShipmentlineDetails);
			_scModelUtils.setStringValueAtModelPath("Shipment.ConsolidatedShipmentKey", consolidatedShipmentKey , shipmentDetails);
		}
         var shipmentLines =_scModelUtils.getStringValueFromPath("ShipmentLines",curbsideShipmentlineDetails);
         if(!_scBaseUtils.isVoid(shipmentLineList))
             {
                for (var i=0;i<shipmentLineList.length;i++)
                {
                    var shipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", shipmentLineList[i]);
                    _scBaseUtils.removeItemFromArray(shipmentLineList, shipmentLineList[i]);
               }
            }                      
            _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines", shipmentLines , shipmentDetails);
            shipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentDetails);
        }
		// OMNI-83819, 85145 Curbside Consolidation - Start
		_scWidgetUtils.hideWidget(this, 'extn_AlternatePickupDetails');
		_scWidgetUtils.hideWidget(this,"extn_APP_Name");
		_scWidgetUtils.hideWidget(this,"extn_APP_Email");
		if(_iasContextUtils.isMobileContainer()){
			var sLines = _scModelUtils.getModelObjectFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentDetails);
			if(!_scBaseUtils.isVoid(sLines)){
				var sAPPName =  _scModelUtils.getStringValueFromPath("OrderLine.PersonInfoMarkFor.FirstName", sLines[0]);
				if (typeof sAPPName != 'undefined' && !_scBaseUtils.isVoid(sAPPName))  {
					sAPPName += " " + _scModelUtils.getStringValueFromPath("OrderLine.PersonInfoMarkFor.LastName", sLines[0]);
				}
				_scModelUtils.setStringValueAtModelPath("Shipment.Extn.APPName", sAPPName, shipmentDetails);
				_scWidgetUtils.showWidget(this, 'extn_AlternatePickupDetails');
				_scWidgetUtils.showWidget(this,"extn_APP_Name");
				var sAPPEmail = _scModelUtils.getStringValueFromPath("OrderLine.PersonInfoMarkFor.EMailID", sLines[0]);
				_scModelUtils.setStringValueAtModelPath("Shipment.Extn.APPEmail", sAPPEmail, shipmentDetails);
				_scWidgetUtils.showWidget(this,"extn_APP_Email");
			}
			_scWidgetUtils.hideWidget(this, 'extn_lblShipmentNo');
			//OMNI-85035 - Add count of number of shipment lines (UI) - Start
			var totalShipLines =  _scModelUtils.getStringValueFromPath("ShipmentLines.TotalShipmentLines", curbsideShipmentlineDetails);
			_scModelUtils.setStringValueAtModelPath("Shipment.TotalShipmentLines", totalShipLines, shipmentDetails);
			_scWidgetUtils.showWidget(this, 'extn_Total_ShipLines');
			//OMNI-85035 - Add count of number of shipment lines (UI) - End
		} 
		// OMNI-83819, 85145 Curbside Consolidation - End
		
		//OMNI-8710, 8717: UI Align and Payment Info - Start
		if (_iasContextUtils.isMobileContainer() && !_scBaseUtils.isVoid(isCurbsideOrder) && _scBaseUtils.equals(isCurbsideOrder,"Y")){			
			/*OMNI-79056-Curbside Estimated Time Delay Changes - START
			_scWidgetUtils.showWidget(this,"extn_ExtendInfoPane");
			_scWidgetUtils.showWidget(this,"extn_CurbsideTimeDelay");	
      _scWidgetUtils.showWidget(this,"extn_Attended_by");
			//OMNI-79056-Curbside Estimated Time Delay Changes - END */
			var sCurbsidePickupInfo = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnCurbsidePickupInfo", shipmentDetails);
			//_scWidgetUtils.showWidget(this,"extn_Curbsidelbl");
			if(!_scBaseUtils.isVoid(sCurbsidePickupInfo)){
                _scWidgetUtils.addClass(this,"extn_lnkEmail","extn_DisplayEmail");
				var arrPickupInfo = [];
				arrPickupInfo = sCurbsidePickupInfo.split(':');
                var smake = arrPickupInfo[0];
                _scModelUtils.setStringValueAtModelPath("Shipment.Extn.VehicleMake", smake, shipmentDetails);
                _scWidgetUtils.showWidget(this,"extn_VehicleMake");
                var scolor = arrPickupInfo[1];
                _scModelUtils.setStringValueAtModelPath("Shipment.Extn.VehicleColor", scolor, shipmentDetails);
                _scWidgetUtils.showWidget(this,"extn_VehicleColor");
                var sPSpot = arrPickupInfo[2];
                //if(!_scBaseUtils.isVoid(sPSpot)){
				_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ParkingSpot", sPSpot, shipmentDetails);
				_scWidgetUtils.showWidget(this,"extn_ParkingSpot");
                //}  
                /* START - (OMNI - 71887)*/
				var stype = arrPickupInfo[3];
                _scModelUtils.setStringValueAtModelPath("Shipment.Extn.VehicleType", stype, shipmentDetails);
                _scWidgetUtils.showWidget(this,"extn_VehicleType");
				var suploadplace = arrPickupInfo[4];
                _scModelUtils.setStringValueAtModelPath("Shipment.Extn.UploadPlace", suploadplace, shipmentDetails);
                _scWidgetUtils.showWidget(this,"extn_UploadPlace");
				/* END - (OMNI - 71887)*/
			}			
		}
        //OMNI-8710, 8717: UI Align and Payment Info - End
        /*OMNI-79056-Curbside Estimated Time Delay Changes - START
            else{
		        _scWidgetUtils.hideWidget(this, 'extn_ExtendInfoPane');
		        _scWidgetUtils.hideWidget(this,"extn_CurbsideTimeDelay");
			      _scWidgetUtils.hideWidget(this,"extn_Attended_by");
		    }
		//OMNI-79056-Curbside Estimated Time Delay Changes - END */
    /* OMNI-79563 Estimated Curbside Delay Timer Count Changes - START*/
		/* OMNI-79562 Curbside time delay : Extensions Allowed Feature Changes - START*/
		//fetching the common code type and invoking API when either one of the context value is void
			var curbsideSessionModel = _iasContextUtils.getFromContext("CurbsideDelayMaxCounter");
            var curbsideExtensionsSessionModel = _iasContextUtils.getFromContext("CurbsideExtensionsAllowed");
			
			var inStorePickupFlagEnabled = _iasContextUtils.getFromContext("InStorePickupFlagEnabled");//OMNI-105502
			 
			var curbsideDefaultMins = _iasContextUtils.getFromContext("CurbsideDefaultMins"); //OMNI-79883
			var instoreDefaultMins = _iasContextUtils.getFromContext("InstoreDefaultMins"); //OMNI-79883
      var sms1 = _iasContextUtils.getFromContext("sms1");//OMNI-82369
			var sms2 = _iasContextUtils.getFromContext("sms2");
			var sms3 = _iasContextUtils.getFromContext("sms3");
			var curbsideDelayCount= _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnCurbsideDelayCount",shipmentDetails);
            if((_scBaseUtils.isVoid(sms1)) || (_scBaseUtils.isVoid(sms2)) ||(_scBaseUtils.isVoid(sms3)) || (_scBaseUtils.isVoid(curbsideSessionModel)) || (_scBaseUtils.isVoid(curbsideExtensionsSessionModel)) || (_scBaseUtils.isVoid(curbsideDefaultMins))){ 
				var inputModel = _scModelUtils.createNewModelObjectWithRootKey("CommonCode");
				_scModelUtils.setStringValueAtModelPath("CommonCode.CodeType", "CURBSIDE_EXTENSIONS", inputModel);			
				_iasUIUtils.callApi(this, inputModel, "extn_CurbsideExtensions_ref", null);		
				curbsideSessionModel = _iasContextUtils.getFromContext("CurbsideDelayMaxCounter");
                curbsideExtensionsSessionModel = _iasContextUtils.getFromContext("CurbsideExtensionsAllowed");
				curbsideDefaultMins = _iasContextUtils.getFromContext("CurbsideDefaultMins");
				instoreDefaultMins = _iasContextUtils.getFromContext("InstoreDefaultMins");
				}
      //commented the before requirement of diable the time extensions came with new pop up requirement
			/*if (_scBaseUtils.greaterThanOrEqual(parseInt(curbsideDelayCount),parseInt(curbsideSessionModel))){
				_scWidgetUtils.disableWidget(this, "extn_EstimatedDelay1");
				_scWidgetUtils.disableWidget(this, "extn_EstimatedDelay2");
				_scWidgetUtils.disableWidget(this, "extn_EstimatedDelay3");
				_scWidgetUtils.disableWidget(this, "extn_EstimatedDelay4");
			} */
			this.extn_Curbside_Toggle(curbsideExtensionsSessionModel);
			this.extn_Instore_Toggle(inStorePickupFlagEnabled);
			   /* OMNI-79562 Curbside time delay : Extensions Allowed Feature Changes - END*/	
			/*OMNI-79563 Estimated Curbside Delay Timer Count Changes - END */
			//OMNI-79883 Complete Curbside Order By
			if(!_scBaseUtils.isVoid(curbsideDefaultMins) && !_scBaseUtils.isVoid(curbsideExtensionsSessionModel)){
				this.extn_CurbsideOrderDeliveryTime(curbsideExtensionsSessionModel);
			}
			//OMNI-105502 - Start
			if(!_scBaseUtils.isVoid(instoreDefaultMins) && !_scBaseUtils.isVoid(inStorePickupFlagEnabled)){
				this.extn_InstoreOrderDeliveryTime(inStorePickupFlagEnabled);
			}
			//OMNI-105502 - End
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
			var shipmentModel = _scScreenUtils.getModel(
               this, "ShipmentDetails");
            var PaymentStatus = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.Order.PaymentStatus", shipmentModel);
             
                if ((this.isAgeRestrictedSet)  && (_scBaseUtils.equals(PaymentStatus,"AUTHORIZED")) ) {
                _scScreenUtils.showConfirmMessageBox(this, 
                    "Items in the shipment require age validation. Is the customer born before " 
                    + this.maxAgeRestrictionValue + "?", "popupCloseConfirmation", null, null);
                _scEventUtils.stopEvent(bEvent);
                this.isAgeRestrictedSet = false;
                this.maxAgeRestrictionValue = 0;
                 }
            
        }
        //KER-16062: Age Validation Popup -End

	
        if(_scBaseUtils.equals(paymentInfo,"AWAIT_PAY_INFO") ||  _scBaseUtils.equals(paymentInfo,"AWAIT_AUTH")) {
            _scWidgetUtils.disableWidget(this, "scanProductIdTxt");
        }
		
		//OMNI-88832 - Show 'Begin FireArm' button until all RFCP are moved to PWI - Start
		var curbsideConsFlag =  _iasContextUtils.getFromContext("CurbsideConsolidationToggle");
		var shipmentDetailModel = _scScreenUtils.getModel(this, "ShipmentDetails");
		var vExtnIsCurbsidePickupOpted=_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentDetailModel);
		if(!_scBaseUtils.isVoid(curbsideConsFlag) && _scBaseUtils.equals(curbsideConsFlag,"Y") && (_scBaseUtils.equals(vExtnIsCurbsidePickupOpted, "Y")) && _iasContextUtils.isMobileContainer()) {
			var shipmentLines =_scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentDetailModel);
			var atleastOneRFCP = "";
			for (var i=0; i < shipmentLines.length; i++) {
				var Shipment = shipmentLines[i].Shipment[0];
				var status = _scModelUtils.getStringValueFromPath("Status", Shipment);
				if(_scBaseUtils.equals(status,"1100.70.06.30.5")) {
					atleastOneRFCP = "Y";
				}
			}
			if(_scBaseUtils.equals(atleastOneRFCP, "Y")) {
					_scWidgetUtils.showWidget(this, 'extn_customer_piickup_button');
			}
		} else {
			//BOPIS-2051: Paper Work Initiation button is removed upon closing the customer pickup screen and coming back - begin
			if(_scBaseUtils.equals(shipmentDetails.Shipment.Status,"1100.70.06.30.5")) {
				_scWidgetUtils.showWidget(this, 'extn_customer_piickup_button');
			}
			//BOPIS-2051: Paper Work Initiation button is removed upon closing the customer pickup screen and coming back - end
		}
		//OMNI-88832 - Show 'Begin FireArm' button until all RFCP are moved to PWI - End
		
        // Changes for Age verfication: Begin
        if (!_scBaseUtils.isVoid(shipmentLineList)) {
            for (var i=0;i<shipmentLineList.length;i++) {
                var quantity = _scModelUtils.getNumberValueFromPath("OriginalQuantity", shipmentLineList[i]);
                var customerPickedQuantity = _scModelUtils.getNumberValueFromPath("CustomerPickedQuantity", shipmentLineList[i]);
                var shortageQty = _scModelUtils.getNumberValueFromPath("ShortageQty", shipmentLineList[i]);
                if ((quantity - shortageQty) != 0 || (quantity - customerPickedQuantity) == 0) {
                    this.allShipmentLinesNotShorted = true;
                    // break;
                }
                if ((quantity - customerPickedQuantity) != 0 && (quantity - shortageQty) == 0 ) {
                    _scModelUtils.setStringValueAtModelPath("isFullLineShorted", "Y", shipmentLineList[i]);
                }
            }
           _scScreenUtils.setModel(this, "ShipmentDetails", shipmentDetails, null); 
        }
		//BOPIS-2031_CR: BOPIS Default Pick - begin
		this.extnDefaultPickOnScreenLoad(shipmentDetails);
		//BOPIS-2031_CR: BOPIS Default Pick - end
  //Combining Customer Pickup screen OMNI-8660 -Start
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
                                _scWidgetUtils.setValue(this, "extn_datalabel3", date, false);
                                break;
                            }
                        }
                    }
                }
            }
    },
	//OMNI-105502 - Start
	extn_InstoreOrderDeliveryTime: function(isInstoreEnabled){
		var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");
		var isInstoreOrder=_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsInstorePickupOpted",shipmentDetails);
		if(!_scBaseUtils.isVoid(isInstoreOrder) && _scBaseUtils.equals(isInstoreOrder,"Y") && !_scBaseUtils.isVoid(isInstoreEnabled) && _scBaseUtils.equals(isInstoreEnabled,"Y")){
			var strAppointmentNo = _scModelUtils.getStringValueFromPath("Shipment.AppointmentNo",shipmentDetails);
			var intInstoreDefaultMins = parseInt(_iasContextUtils.getFromContext("InstoreDefaultMins"));
			var appointmentDate = new Date(strAppointmentNo.replace(/^(\d{4})(\d\d)(\d\d)(\d\d)(\d\d)(\d\d)$/,'$4:$5:$6 $2/$3/$1'));
			var actualInstoreDeliveryDate = new Date(appointmentDate.getTime() + intInstoreDefaultMins*60000);
			var actualInstoreTime = actualInstoreDeliveryDate.toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
			_scWidgetUtils.setValue(this, "extn_finishInstoreOrderBy", actualInstoreTime, false);
		}
	},
	//OMNI-105502 - End
     extn_getDefaultCustomerName: function(dataValue, screen, widget, namespace, modelObj, options) {
        var returnValue = modelObj.CustomerName[0].Customer_Name ;
        return returnValue;
    },
           getFormattedNameDisplay: function(
        dataValue, screen, widget, namespace, modelObj, options) {
            var nameModel = null;
            var formattedName = null;
            nameModel = {};
            _scModelUtils.setStringValueAtModelPath("FirstName", _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.FirstName", modelObj), nameModel);
            _scModelUtils.setStringValueAtModelPath("LastName", _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.LastName", modelObj), nameModel);
            formattedName = _wscShipmentUtils.getNameDisplay(
            this, nameModel);
            return formattedName;
        },
  //Combining Customer Pickup screen OMNI-8660 -Start
    extn_beforeSave: function(event, bEvent, ctrl, args) {
        var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");
        var shipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentDetails);
		var instoreConsFlag = _iasContextUtils.getFromContext("InStoreConsolidationToggle");
		
        if (this.allShipmentLinesNotShorted) {
            if (!_scBaseUtils.isVoid(shipmentLineList)) {
                for (var i=0;i<shipmentLineList.length;i++) {
                    var isFullLineShort = _scModelUtils.getStringValueFromPath("isFullLineShorted", shipmentLineList[i]);
                    var extnIsAgeRestricted = "";
                    var extnAgeRestrictionCode = 0;
                    var ageRestrictedAttributes = _scModelUtils.getStringValueFromPath("OrderLine.Extn", shipmentLineList[i]);
                    if (!_scBaseUtils.isVoid(ageRestrictedAttributes)) {
                        extnIsAgeRestricted = _scModelUtils.getStringValueFromPath("ExtnIsAgeRestricted", ageRestrictedAttributes);
                        extnAgeRestrictionCode = _scModelUtils.getStringValueFromPath("ExtnAgeRestrictionCode", ageRestrictedAttributes);
                        if (!_scBaseUtils.equals(isFullLineShort, "Y") && _scBaseUtils.equals(extnIsAgeRestricted, "Y")) {
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
                //logic to calculate date : start
                    //var currentDateTime = new Date();
                    //var date = currentDateTime.toLocaleDateString();
                    //var year = currentDateTime.getFullYear();
                    //var restrictedYear = year - this.maxAgeRestrictionValue;
                    //var indexOfYear = date.indexOf(year);
                    //var dayAndMonth = date.substring(0, indexOfYear);
                    //this.maxAgeRestrictionValue = dayAndMonth + restrictedYear;
            }
        }
        if (this.isAgeRestrictedSet) {
            //KER-16062: Age Validation Popup -Start
            //_scScreenUtils.showConfirmMessageBox(this, "Items in the shipment require age validation. Is the customer born before " + this.maxAgeRestrictionValue + "?", "handleCloseConfirmation", null, null);
            //_scEventUtils.stopEvent(bEvent);
            //this.isAgeRestrictedSet = false;
            //this.maxAgeRestrictionValue = 0;
            var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");
            var shipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentDetails);
			var vCurbConsolFlag = shipmentDetails.Shipment.ShipmentLines.CurbsideConsolidationFlag;
			var vInstoreConsolFlag = shipmentDetails.Shipment.ShipmentLines.InstoreConsolidationFlag;
            var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentDetails);
            var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
            _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, inputModel);
            _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine", [], inputModel);
            var tempShipmentLines = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", inputModel);		
			//OMNI-102472 - Start
			var consolidatedShipmentKeyFinal = "";
			var consolidatedShipmentKey ="";
			var shipKey = "";
			var isApplicableForConsolidation = "";
			if(_iasContextUtils.isMobileContainer() && (((!_scBaseUtils.isVoid(vCurbConsolFlag)) && _scBaseUtils.equals(vCurbConsolFlag,"Y")) ||
				((!_scBaseUtils.isVoid(vInstoreConsolFlag)) && _scBaseUtils.equals(vInstoreConsolFlag,"Y")))){
				isApplicableForConsolidation = "Y";
			}
			//OMNI-102472 - End
            if (!_scBaseUtils.isVoid(shipmentLineList)) {
                var j = 0;
                for (var i=0;i<shipmentLineList.length;i++) {
                    var tempShipmentLine = {};
                    var extnIsAgeRestricted = "";
                    var ageRestrictedAttributes = _scModelUtils.getStringValueFromPath("OrderLine.Extn", shipmentLineList[i]);
                    if (!_scBaseUtils.isVoid(ageRestrictedAttributes)) {
                        var isFullLineShorted = _scModelUtils.getStringValueFromPath("isFullLineShorted", shipmentLineList[i]);
                        extnIsAgeRestricted = _scModelUtils.getStringValueFromPath("ExtnIsAgeRestricted", ageRestrictedAttributes);
                        if (_scBaseUtils.equals(extnIsAgeRestricted, "Y") && (_scBaseUtils.equals(isFullLineShorted, "N")||_scBaseUtils.isVoid(isFullLineShorted))) {
                            _scModelUtils.setStringValueAtModelPath("ShipmentLineKey", _scModelUtils.getStringValueFromPath("ShipmentLineKey", shipmentLineList[i]), tempShipmentLine);
                            _scModelUtils.setStringValueAtModelPath("Extn.ExtnAgeVerified", "Y", tempShipmentLine);	
							_scModelUtils.setStringValueAtModelPath("Action", "Modify", tempShipmentLine);								
							shipKey = _scModelUtils.getStringValueFromPath("ShipmentKey", shipmentLineList[i]);
							 _scModelUtils.setStringValueAtModelPath("ShipmentKey", _scModelUtils.getStringValueFromPath("ShipmentKey", shipmentLineList[i]), tempShipmentLine);
							if(!_scBaseUtils.contains(consolidatedShipmentKeyFinal,shipKey)) {
							consolidatedShipmentKeyFinal = consolidatedShipmentKeyFinal + shipKey + ",";
						}
                        }
                    }
                    if (_scBaseUtils.equals(extnIsAgeRestricted, "Y") && (_scBaseUtils.equals(isFullLineShorted, "N")||_scBaseUtils.isVoid(isFullLineShorted))) {
                        tempShipmentLines[j++] = tempShipmentLine;
                    }
                }
				var length = consolidatedShipmentKeyFinal.length;
				consolidatedShipmentKey = consolidatedShipmentKeyFinal.substring(0, length -1);
            }
            if(!_scBaseUtils.isVoid(tempShipmentLines)) {
				//OMNI-102472
				if(!_scBaseUtils.isVoid(isApplicableForConsolidation) && !_scBaseUtils.isVoid(isApplicableForConsolidation)){
					_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey",consolidatedShipmentKey,inputModel);
				}
                _iasUIUtils.callApi(this, inputModel, "extn_AgeVerifiedChangeShipment_ref", null);
            }
        }
        //KER-16062: Age Validation Popup -End
		else{
			this.save();
		}
    },
	//OMNI-85085 - Start
	updateShortageForShipmentLines: function(
        event, bEvent, ctrl, args) {
            var shipmentModel = null;
            shipmentModel = _scScreenUtils.getTargetModel(
            this, "updateShipmentPickQuantity_input", null);
			var curbsideConsFlag = _iasContextUtils.getFromContext("CurbsideConsolidationToggle");
			var instoreConsFlag = _iasContextUtils.getFromContext("InStoreConsolidationToggle");
			var isEligibleforConsolidation = "";
            var shortedShipmentLineModel = null;
            shortedShipmentLineModel = _scBaseUtils.getModelValueFromBean("inputData", args);
            var markAllShortLineWithShortage = null;
            markAllShortLineWithShortage = _scModelUtils.getStringValueFromPath("MarkAllShortLineWithShortage", shortedShipmentLineModel);
            var shortedShipmentModel = null;
            shortedShipmentModel = _wscShipmentUtils.importShipmentLineToShipment(shipmentModel, shortedShipmentLineModel);
			var consoldshpKey = _scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentKey",shortedShipmentLineModel)
			if(_iasContextUtils.isMobileContainer() && !_scBaseUtils.isVoid(consoldshpKey) && ((!_scBaseUtils.isVoid(curbsideConsFlag) && _scBaseUtils.equals(curbsideConsFlag,"Y")) || (!_scBaseUtils.isVoid(instoreConsFlag) && _scBaseUtils.equals(instoreConsFlag,"Y")))){
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey",consoldshpKey, shortedShipmentModel);	
				isEligibleforConsolidation = "Y";//OMNI-85085
			}
            if (!(_scBaseUtils.equals(markAllShortLineWithShortage, "Y"))) {
                _iasUIUtils.callApi(this, shortedShipmentModel, "updateShipmentLineForShortage", null);
            } else {
				//OMNI-85085 - Start
				if (_scBaseUtils.equals(isEligibleforConsolidation, "Y")) {
					//OMNI-102218 - Start
					var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");
					var instoreConsldFlag = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.InstoreConsolidationFlag", shipmentDetails);
					if(!_scBaseUtils.isVoid(instoreConsldFlag) && _scBaseUtils.equals(instoreConsldFlag,"Y")){
						var consolidatedShipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ConsolidatedShipmentKey", shipmentDetails);
						_scModelUtils.setStringValueAtModelPath("Shipment.ConsolidatedShipmentKey", consolidatedShipmentKey, shortedShipmentModel);
					}//OMNI-102218 - End 
					else {
						var curbsideShipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.CurbsideShipmentKey", shipmentDetails);
						_scModelUtils.setStringValueAtModelPath("Shipment.CurbsideShipmentKey", curbsideShipmentKey, shortedShipmentModel);
					}
					_iasUIUtils.callApi(this, shortedShipmentModel, "extn_changeShipmentForCurbsideConsoldiation", null);
				//OMNI-85085 - End
				}else{
					_iasUIUtils.callApi(this, shortedShipmentModel, "shortRemainingShipmentLines", null);
				}
            }
        },
	//OMNI-85085 - End
    handleCloseConfirmation: function(res) {
        if (_scBaseUtils.equals(res, "Ok")) {
            //changes fro BOPIS-1506:Age Verification Indicator is missing in DB: Begin
            var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");
            var shipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentDetails);
            var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentDetails);
            var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
            _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, inputModel);
            _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine", [], inputModel);
            var tempShipmentLines = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", inputModel);
            if (!_scBaseUtils.isVoid(shipmentLineList)) {
				var j = 0;
                for (var i=0;i<shipmentLineList.length;i++) {
                    var tempShipmentLine = {};
                    var extnIsAgeRestricted = "";
                    var ageRestrictedAttributes = _scModelUtils.getStringValueFromPath("OrderLine.Extn", shipmentLineList[i]);
                    if (!_scBaseUtils.isVoid(ageRestrictedAttributes)) {
                        var isFullLineShorted = _scModelUtils.getStringValueFromPath("isFullLineShorted", shipmentLineList[i]);
                        extnIsAgeRestricted = _scModelUtils.getStringValueFromPath("ExtnIsAgeRestricted", ageRestrictedAttributes);
                        if (_scBaseUtils.equals(extnIsAgeRestricted, "Y") && (_scBaseUtils.equals(isFullLineShorted, "N")||_scBaseUtils.isVoid(isFullLineShorted))) {
                            _scModelUtils.setStringValueAtModelPath("ShipmentLineKey", _scModelUtils.getStringValueFromPath("ShipmentLineKey", shipmentLineList[i]), tempShipmentLine);
                            _scModelUtils.setStringValueAtModelPath("Extn.ExtnAgeVerified", "Y", tempShipmentLine);
                        }
                    }
                    if (_scBaseUtils.equals(extnIsAgeRestricted, "Y") && (_scBaseUtils.equals(isFullLineShorted, "N")||_scBaseUtils.isVoid(isFullLineShorted))) {
                        tempShipmentLines[j++] = tempShipmentLine;
                    }
                }
            }
            if(!_scBaseUtils.isVoid(tempShipmentLines)) {
                _iasUIUtils.callApi(this, inputModel, "extn_AgeVerifiedChangeShipment_ref", null);
            }
            //changes fro BOPIS-1506:Age Verification Indicator is missing in DB: End
            if(this.isCalledFromPickAll) {
                _iasWizardUtils.gotoNextScreen(this);
                this.isCalledFromPickAll = false;
            }
        }
        if (_scBaseUtils.equals(res, "Cancel")) {
            _iasScreenUtils.showInfoMessageBoxWithOk(this, "The customer does not meet the age requirements to purchase this item. Please cancel each age restricted item within the order by selecting 'Age Verification' as the short pick reason.", "infoCallBack", null);
        }
    },
    handlePickAllConfirmation: function(
        res) {
            var lastScannedProductPanelScreen = null;
            lastScannedProductPanelScreen = _scScreenUtils.getChildScreen(
            this, "lastProductScannedDetailsScreenRef");
            if (!(
            _scBaseUtils.isVoid(
            lastScannedProductPanelScreen))) {
                _scScreenUtils.clearScreen(
                lastScannedProductPanelScreen, "lastProductScanned_output");
                _scWidgetUtils.hideWidget(
                this, "lastProductScannedDetailsScreenRef", false);
            }
            if (
            _scBaseUtils.equals(
            res, "Ok")) {
                // Changes for Age verfication: Begin
                if (this.isAgeRestrictedSet){
                    _scScreenUtils.showConfirmMessageBox(this, "Items in the shipment require age validation. Is the customer born before " + this.maxAgeRestrictionValue + " ?", "handleCloseConfirmation", null, null);
                    this.isCalledFromPickAll = true;
                }
                else{
                    _iasWizardUtils.gotoNextScreen(
                this); 
                }
               // Changes for Age verfication: end
            } else {
                this.nextView = "shortItems";
                var eDef = null;
                eDef = {};
                var eArgs = null;
                eArgs = {};
                _scEventUtils.fireEventInsideScreen(
                this, "reloadSelectView", eDef, eArgs);
            }
    },
    infoCallBack: function() {

    },
    // Changes for Age verfication: end
    //BOPIS-1641- BEGIN
    updateShipmentLineDetails: function(
        event, bEvent, ctrl, args) {
            var shipmentModel = null;
            shipmentModel = _scBaseUtils.getModelValueFromBean("inputData", args);
            var shipmentLines = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentModel);
            var shipmentLine = shipmentLines[0]; 
            var quantity = Number(_scModelUtils.getStringValueFromPath("OriginalQuantity", shipmentLine));
            var CustomerPickedQuantity = Number(_scModelUtils.getStringValueFromPath("CustomerPickedQuantity", shipmentLine));
            var shortageQuantity = quantity - CustomerPickedQuantity;
            _scModelUtils.setStringValueAtModelPath("ShortageQty",shortageQuantity,shipmentLine);
            _scModelUtils.setStringValueAtModelPath("OriginalQuantity",null,shipmentLine);
            _scBaseUtils.removeBlankAttributes(shipmentModel);
            _iasUIUtils.callApi(
            this, shipmentModel, "updateShipmentLine", null);
    },
    //BOPIS-1641- END
    extn_afterScreenLoad: function(event, bEvent, ctrl, args) {
        _scWidgetUtils.setFocusOnWidgetUsingUid(this, "scanProductIdTxt");
		
		//BOPIS-2033_CR: Change the BOPIS order pickup flow - begin
		var wizardInstance = false;
        wizardInstance = _iasUIUtils.getParentScreen(
        this, true);
        _iasWizardUtils.hideNavigationalWidget(
        wizardInstance, "nextBttn", false);
        _iasWizardUtils.hideNavigationalWidget(
        wizardInstance, "nextBttn2", false);
		_iasWizardUtils.hideNavigationalWidget(
        wizardInstance, "prevBttn", false);
		_iasWizardUtils.hideNavigationalWidget(
        wizardInstance, "prevBttn2", false);
		_iasWizardUtils.hideNavigationalWidget(
        wizardInstance, "closeBttn", false);
		_iasWizardUtils.hideNavigationalWidget(
        wizardInstance, "closeBttn2", false);
		
		if (_iasContextUtils.isMobileContainer()) {
   		// START - (OMNI - 1409)  : BOPIS: Finish Pickup Button for BOPIS orders 	
      //_scWidgetUtils.hideWidget(this, "extn_FinishPick_button", false);
      //_scWidgetUtils.hideWidget(this, "extn_FinishPick_button2new", false);
  		_scWidgetUtils.hideWidget(this, "extn_FinishPickMobile_link", false);			
      // END - (OMNI - 1409)  : BOPIS: Finish Pickup Button for BOPIS orders 
			_scWidgetUtils.hideWidget(this, "extn_Previous_button", false);
			_scWidgetUtils.hideWidget(this, "extn_Previous_button2new", false);
			_scWidgetUtils.hideWidget(this, "extn_Close_button", false);
			_scWidgetUtils.hideWidget(this, "extn_Close_button2new", false);
			_scWidgetUtils.hideWidget(this, "extn_FinishPick_button", false);
			_scWidgetUtils.hideWidget(this, "extn_FinishPick_button2new", false);
			   var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");
            var isCurbsideOrder=_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentDetails);
      //Combining Customer Pickup screen OMNI-8660 -Start
			if(_scBaseUtils.equals(isCurbsideOrder,"Y"))
            {
	        _scWidgetUtils.hideWidget(this, 'extn_FinishPickuButton');
	        _scWidgetUtils.hideWidget(this, 'extn_FinishPickupButton1');
            } else {
	        _scWidgetUtils.hideWidget(this, 'extn_CurbsideReset');
	        _scWidgetUtils.hideWidget(this, 'extn_CurbsideResetDownButton');
	        _scWidgetUtils.hideWidget(this,'extn_FinishPickUpButton1');
	        _scWidgetUtils.hideWidget(this,'extn_FinishPickupButton');
            //OMNI - 80092 - Start
			      //_scWidgetUtils.hideWidget(this, 'extn_assigncurbsideorder');
		      	//OMNI - 80092 - End
            }
              //Combining Customer Pickup screen OMNI-8660 -End
			 			
        }
		else if(!_iasContextUtils.isMobileContainer()){
			_scWidgetUtils.hideWidget(this, "extn_FinishPickMobile_link", false);
			//Hide reset curbside button for desktop mode
			 _scWidgetUtils.hideWidget(this, "extn_CurbsideReset",false);
	        _scWidgetUtils.hideWidget(this, "extn_CurbsideResetDownButton",false);		
	        _scWidgetUtils.hideWidget(this, "extn_FinishPickuButton",false);
	        _scWidgetUtils.hideWidget(this, "extn_FinishPickupButton1",false);
            //OMNI - 80092 - Start
            //_scWidgetUtils.hideWidget(this, "extn_assigncurbsideorder",false);
			      //OMNI - 80092 - End	
            	
		}
		//BOPIS-2033_CR: Change the BOPIS order pickup flow - end
    },
    
	//BOPIS-2031_CR: BOPIS Default Pick - begin
     extnDefaultPickOnScreenLoad: function(shipmentDetails) {
		var changeShipmentInputModel={};
		changeShipmentInputModel.Shipment={};
		changeShipmentInputModel.Shipment.ShipmentKey=_scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentDetails);
		changeShipmentInputModel.Shipment.ShipmentLines={};
		changeShipmentInputModel.Shipment.ShipmentLines.ShipmentLine=[];
		
		//START - OMNI-85081 CURBSIDE CONSOLIDATION / OMNI-101862
		var vCurbConsolFlag = shipmentDetails.Shipment.ShipmentLines.CurbsideConsolidationFlag;
		var vInstoreConsolFlag = shipmentDetails.Shipment.ShipmentLines.InstoreConsolidationFlag;
		var vCurbOrInstoreConsolModel={};
		vCurbOrInstoreConsolModel.ShipmentLines={};
		vCurbOrInstoreConsolModel.ShipmentLines.ShipmentLine=[];
		//END - OMNI-85081 CURBSIDE CONSOLIDATION

		//var remainingQty=null;
		//var isQtyRem=false;

		for(var i=0;i<shipmentDetails.Shipment.ShipmentLines.ShipmentLine.length;i++){
			//BOPIS-2044: Shorted QTY showing up even when complete qty is picked during customer pickup - begin
			
			var shipmentLinesModel={};
			//BOPIS-2045: Shorted/Partially picked QTY not retaining upon closing the customer pick up screen
			if(_scBaseUtils.isVoid(shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].ShortageResolutionReason) && 
			_scBaseUtils.isVoid(shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].CustomerPickedQuantity) &&
			!_scBaseUtils.equals(shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].Order.PaymentStatus, "AWAIT_PAY_INFO") && 
			!_scBaseUtils.equals(shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].Order.PaymentStatus, "AWAIT_AUTH")){
                //Below Line is commented as part of OMNI-48311
				//shipmentLinesModel.CustomerPickedQuantity=Number(shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].BackroomPickedQuantity);
				//OMNI-85081 CURBSIDE CONSOLIDATION
				shipmentLinesModel.ShipmentKey = shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].ShipmentKey;
				shipmentLinesModel.Quantity=shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].Quantity;
                //OMNI-48311 - Starts - This has to be revisited if Partial quantity cancellation of a line is enabled
				if((Number(shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].Quantity)) > 0 ){
				    shipmentLinesModel.CustomerPickedQuantity=Number(shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].BackroomPickedQuantity);
				}
				else{
				    shipmentLinesModel.CustomerPickedQuantity=Number(shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].Quantity);
				}
				//OMNI-48311 - Ends
				shipmentLinesModel.ShipmentLineKey=shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].ShipmentLineKey;

				var OriginalQuantity = Number(shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].OriginalQuantity);
				var ShortageQuantity = OriginalQuantity - (shipmentLinesModel.CustomerPickedQuantity);
				shipmentLinesModel.ShortageQty=ShortageQuantity;

				changeShipmentInputModel.Shipment.ShipmentLines.ShipmentLine.push(shipmentLinesModel);
				//OMNI-85081 CURBSIDE CONSOLIDATION
				vCurbOrInstoreConsolModel.ShipmentLines.ShipmentLine.push(shipmentLinesModel);
			}
			//BOPIS-2062:Handling payment exception scenario- Start
			if(_scBaseUtils.equals(shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].Order.PaymentStatus, "AWAIT_PAY_INFO") || 
			_scBaseUtils.equals(shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].Order.PaymentStatus, "AWAIT_AUTH")){
				//OMNI-85081 CURBSIDE CONSOLIDATION
				shipmentLinesModel.ShipmentKey = shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].ShipmentKey;
				shipmentLinesModel.CustomerPickedQuantity=0;
				shipmentLinesModel.Quantity=shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].Quantity;
				shipmentLinesModel.ShipmentLineKey=shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].ShipmentLineKey;
				shipmentLinesModel.CancelReason="";
				shipmentLinesModel.ShortageResolutionReason="";

				var OriginalQuantity = Number(shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].OriginalQuantity);
				var ShortageQuantity = OriginalQuantity - (shipmentLinesModel.Quantity);
				shipmentLinesModel.ShortageQty=ShortageQuantity;

				changeShipmentInputModel.Shipment.ShipmentLines.ShipmentLine.push(shipmentLinesModel);
				//OMNI-85081 CURBSIDE CONSOLIDATION
				vCurbOrInstoreConsolModel.ShipmentLines.ShipmentLine.push(shipmentLinesModel);
			}
			//BOPIS-2062:Handling payment exception scenario- End
			//remainingQty = Number(shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].Quantity) - Number(shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].CustomerPickedQuantity);
			//	if(!_scBaseUtils.isVoid(remainingQty) && !_scBaseUtils.equals(remainingQty, 0) && _scBaseUtils.isVoid(shipmentDetails.Shipment.ShipmentLines.ShipmentLine[i].ShortageResolutionReason)){
			//		isQtyRem=true;
			//	}
			
			//BOPIS-2044: Shorted QTY showing up even when complete qty is picked during customer pickup - end
		}
		/*var eventDefn = null;
		eventDefn = {};
		_scBaseUtils.setAttributeValue("inputData", changeShipmentInputModel, eventDefn);
		_scEventUtils.fireEventInsideScreen(this, "updateShipmentLineDetails", null, eventDefn);*/
		
		//BOPIS-2045: Shorted/Partially picked QTY not retaining upon closing the customer pick up screen
		//OMNI-85081 CURBSIDE CONSOLIDATION
		if(!_scBaseUtils.isVoid(changeShipmentInputModel.Shipment.ShipmentLines.ShipmentLine) && (_scBaseUtils.isVoid(vCurbConsolFlag) || _scBaseUtils.equals(vCurbConsolFlag,"N")) 
		&& (_scBaseUtils.isVoid(vInstoreConsolFlag) || !_scBaseUtils.equals(vInstoreConsolFlag,"Y"))){
			_iasUIUtils.callApi(this, changeShipmentInputModel, "extn_customerpickup_updateShipmentQuantity_Ref", null);	
		}else if((!_scBaseUtils.isVoid(vCurbConsolFlag) && !_scBaseUtils.isVoid(vCurbConsolFlag)) || 
		(!_scBaseUtils.isVoid(vInstoreConsolFlag) && !_scBaseUtils.isVoid(vCurbOrInstoreConsolModel))){
			_iasUIUtils.callApi(this, vCurbOrInstoreConsolModel, "extn_customerpickup_updateShipmentLinesForCurbConsolidation_ref", null);
		}
		//else if(!isQtyRem){ 
		else{
			this.nextView = "allItems";
			var eDef = null;
			eDef = {};
			var eArgs = null;
			eArgs = {};
			_scEventUtils.fireEventInsideScreen(
			this, "reloadSelectView", eDef, eArgs);
		}
    },

    handleMashupCompletion: function(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
        _iasBaseTemplateUtils.handleMashupCompletion(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);		
    },
	
	//BOPIS-2043: After reducing the picked qty and trying to scan item again to pick, picked qty is not showing up in UI - begin
    handleMashupOutput: function(mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
		if (
		_scBaseUtils.equals(
		mashupRefId, "pickAllShipmentLines")) {
			this.handlePickAllShipmentLines(
			modelOutput);
		}
		//OMNI-85085 - added new mashup
		if (
		_scBaseUtils.equals(
		mashupRefId, "shortRemainingShipmentLines") || _scBaseUtils.equals(
		mashupRefId, "extn_changeShipmentForCurbsideConsoldiation") ) {
			this.handleShortRemainingShipmentLines(
			modelOutput);
		}
		if (
		_scBaseUtils.equals(
		mashupRefId, "getPendingShipmentLineList")) {
			this.handlePendingShipmentLineList(
			modelOutput);
		}
		if (
		_scBaseUtils.equals(
		mashupRefId, "translateBarCode")) {
			if (!(
			_scBaseUtils.equals(
			false, applySetModel))) {
				var shipmentDetailModel = _scScreenUtils.getModel(this, "ShipmentDetails");
				var updatedShipmentLine = modelOutput.BarCode.Shipment.ShipmentLine;
				var oldShipmentLine = {};

				for(var i=0; i<shipmentDetailModel.Shipment.ShipmentLines.ShipmentLine.length;i++){
					if(_scBaseUtils.equals(updatedShipmentLine.ShipmentLineKey, shipmentDetailModel.Shipment.ShipmentLines.ShipmentLine[i].ShipmentLineKey)){
						oldShipmentLine=shipmentDetailModel.Shipment.ShipmentLines.ShipmentLine[i];
						updatedShipmentLine = _scBaseUtils.mergeModel(oldShipmentLine, updatedShipmentLine, false);
						//_scModelUtils.addModelToModelPath("Shipment.ShipmentLines.ShipmentLine[i]", updatedShipmentLine, shipmentDetailModel);
						shipmentDetailModel.Shipment.ShipmentLines.ShipmentLine[i]=updatedShipmentLine;
					}
				}
				//var shipmentDetailModel1 = _scScreenUtils.getModel(this, "ShipmentDetails");
				
				_scScreenUtils.setModel(
				this, "translateBarCode_output", modelOutput, null);
			}
			this.handleProductScan(
			modelOutput);
		}
		if (
		_scBaseUtils.equals(
		mashupRefId, "updateShipmentLine")) {
			var shipmentDetailModel = _scScreenUtils.getModel(this, "ShipmentDetails");
			var updatedShipmentLine = modelOutput.Shipment.ShipmentLines.ShipmentLine[0];
			var oldShipmentLine = {};
			
			for(var i=0; i<shipmentDetailModel.Shipment.ShipmentLines.ShipmentLine.length;i++){
				if(_scBaseUtils.equals(updatedShipmentLine.ShipmentLineKey, shipmentDetailModel.Shipment.ShipmentLines.ShipmentLine[i].ShipmentLineKey)){
					oldShipmentLine=shipmentDetailModel.Shipment.ShipmentLines.ShipmentLine[i];
					updatedShipmentLine = _scBaseUtils.mergeModel(oldShipmentLine, updatedShipmentLine, false);
					//_scModelUtils.addModelToModelPath("Shipment.ShipmentLines.ShipmentLine[i]", updatedShipmentLine, shipmentDetailModel);
					shipmentDetailModel.Shipment.ShipmentLines.ShipmentLine[i]=updatedShipmentLine;
				}
			}
			//var shipmentDetailModel1 = _scScreenUtils.getModel(this, "ShipmentDetails");
			
			this.handleUpdateShipmentLine(
			modelOutput);
		}
		if (
		_scBaseUtils.equals(
		mashupRefId, "updateShipmentLineForShortage")) {
			var shipmentDetailModel = _scScreenUtils.getModel(this, "ShipmentDetails");
			var updatedShipmentLine = modelOutput.Shipment.ShipmentLines.ShipmentLine[0];
			var oldShipmentLine = {};
			
			for(var i=0; i<shipmentDetailModel.Shipment.ShipmentLines.ShipmentLine.length;i++){
				if(_scBaseUtils.equals(updatedShipmentLine.ShipmentLineKey, shipmentDetailModel.Shipment.ShipmentLines.ShipmentLine[i].ShipmentLineKey)){
					oldShipmentLine=shipmentDetailModel.Shipment.ShipmentLines.ShipmentLine[i];
					updatedShipmentLine = _scBaseUtils.mergeModel(oldShipmentLine, updatedShipmentLine, false);
					//_scModelUtils.addModelToModelPath("Shipment.ShipmentLines.ShipmentLine[i]", updatedShipmentLine, shipmentDetailModel);
					shipmentDetailModel.Shipment.ShipmentLines.ShipmentLine[i]=updatedShipmentLine;
				}
			}
			//var shipmentDetailModel1 = _scScreenUtils.getModel(this, "ShipmentDetails");

			this.handleUpdateShipmentLineForShortage(
			modelOutput);
		}
		//OMNI-85081 CURBSIDE CONSOLIDATION
		if (_scBaseUtils.equals(mashupRefId, "extn_customerpickup_updateShipmentQuantity_Ref") || _scBaseUtils.equals(mashupRefId,"extn_customerpickup_updateShipmentLinesForCurbConsolidation_ref")) {
			//this.allItems_onShow();	
			/*var args={};
			args.screen=this;
			args.screen.currentView="shortItems";
			var bEvent={};
			_iasScreenUtils.changeView(event,bEvent,"allItems",args);*/

			/*var shipmentModel={};
			shipmentModel.Shipment={};
			shipmentModel.Shipment.ShipmentKey=modelOutput.Shipment.ShipmentKey;
			_iasUIUtils.callApi(
			this, shipmentModel, "getShipmentDetails", null);*/

			this.nextView = "allItems";
			var eDef = null;
			eDef = {};
			var eArgs = null;
			eArgs = {};
			_scEventUtils.fireEventInsideScreen(
			this, "reloadSelectView", eDef, eArgs);
		}
		if (_scBaseUtils.equals(mashupRefId, "extn_stampInvoiceNoOnBOPISOrders_ref") || _scBaseUtils.equals(mashupRefId, "extn_recordCustomerPickForCurbsideConsolidation")) { 
			this.extn_gotoNextScreen(modelOutput);
		}
		if (_scBaseUtils.equals(mashupRefId, "extn_UnResrvMsgToSIMOnShrtg")) {
			this.extn_callRecordCustPick(modelOutput);
		}
		/*if (_scBaseUtils.equals(mashupRefId, "getShipmentDetails")) {
		_scScreenUtils.setModel(this, "getShipmentDetails_output", modelOutput, null);

			this.nextView = "allItems";
			var eDef = null;
			eDef = {};
			var eArgs = null;
			eArgs = {};
			_scEventUtils.fireEventInsideScreen(
			this, "reloadSelectView", eDef, eArgs);
		}*/
		/* OMNI-79898 TM requesting delay without assigning to themselves - No TM associated with curbside -UI Changes - START*/
		//when assigned to or counter updated set those values in the base model
        if (
            _scBaseUtils.equals(mashupRefId, "extn_CurbsideChangeShipmentForAssignee_ref")) {
			    this.handleAttendedByUpdate(modelOutput);
				/*OMNI-88938-UI-Prevent additional time extensions in case of Concurrent users-START*/
				_scWidgetUtils.setValue(this, "extn_finishCurbsideOrderBy", _iasContextUtils.getFromContext("finishCurbsideTimeUpdated"), false);
				/*OMNI-88938-UI-Prevent additional time extensions in case of Concurrent users-END*/
			}
		/* OMNI-79898 TM requesting delay without assigning to themselves - No TM associated with curbside -UI Changes - END*/
      	/* OMNI-79563 Estimated Curbside Delay Timer Count Changes - START*/
       	 /* OMNI-79562 Curbside time delay : Extensions Allowed Feature Changes - START*/	
	 //when context value is void fetching the common code value and that value is setting it to context and based on value Enabling/Hiding the Curbside Feature
			if(_scBaseUtils.equals(mashupRefId, "extn_CurbsideExtensions_ref")){
				var commoncodes = _scModelUtils.getStringValueFromPath("CommonCodeList.CommonCode", modelOutput);			
               			 var curbsideDelayMaxCounter=null;
				 var curbsideExtensionsAllowed=null;
				  var curbsideDefaultMins=null; //OMNI-79883
          var sms1=null;
				  var sms2=null;//OMNI-82169
          var sms3=null;
				for(var i in commoncodes)
				{
					var codeValue = _scModelUtils.getStringValueFromPath("CodeValue", commoncodes[i]);
					if(_scBaseUtils.equals(codeValue,"MAX_EXTENSIONS_ALLOWED")){
						curbsideDelayMaxCounter=_scModelUtils.getStringValueFromPath("CodeShortDescription", commoncodes[i]);
					}		
					if(_scBaseUtils.equals(codeValue,"EXTENSIONS_ALLOWED")){						
						curbsideExtensionsAllowed=_scModelUtils.getStringValueFromPath("CodeShortDescription", commoncodes[i]);
				} 
					if(_scBaseUtils.equals(codeValue,"DEFAULT_MINS")){	//OMNI-79883
						curbsideDefaultMins=_scModelUtils.getStringValueFromPath("CodeShortDescription", commoncodes[i]);
					} 
          //OMNI-82169 -Start
					if(_scBaseUtils.equals(codeValue,"SMS_MSG1")){	
						sms1=_scModelUtils.getStringValueFromPath("CodeLongDescription", commoncodes[i]);
			  	} 
					if(_scBaseUtils.equals(codeValue,"SMS_MSG2")){	
						sms2=_scModelUtils.getStringValueFromPath("CodeLongDescription", commoncodes[i]);
					}
            if(_scBaseUtils.equals(codeValue,"SMS_MSG3")){	
						sms3=_scModelUtils.getStringValueFromPath("CodeLongDescription", commoncodes[i]);
					}
					//OMNI-82169 -End
				if(_scBaseUtils.equals(codeValue,"DEFAULT_INSTORE_MINS")){	//OMNI-105502
						instoreDefaultMins=_scModelUtils.getStringValueFromPath("CodeShortDescription", commoncodes[i]);
					} 
				} 
				var shipmentModel = _scScreenUtils.getModel(this, "ShipmentDetails");
				var curbsideDelayCount= _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnCurbsideDelayCount",shipmentModel);
				_iasContextUtils.addToContext("CurbsideDelayMaxCounter",curbsideDelayMaxCounter);
               			 _iasContextUtils.addToContext("CurbsideExtensionsAllowed",curbsideExtensionsAllowed);
				_iasContextUtils.addToContext("CurbsideDefaultMins",curbsideDefaultMins); //OMNI-79883
        //OMNI-82169 -Start
        _iasContextUtils.addToContext("sms1", sms1);
				 _iasContextUtils.addToContext("sms2", sms2);
				 _iasContextUtils.addToContext("sms3", sms3);
         //OMNI-82169 -End
        //commented the before requirement of diable the time extensions came with new pop up requirement
        /*if (_scBaseUtils.greaterThanOrEqual(parseInt(curbsideDelayCount),parseInt(curbsideDelayMaxCounter))){
				_scWidgetUtils.disableWidget(this, "extn_EstimatedDelay1");
				_scWidgetUtils.disableWidget(this, "extn_EstimatedDelay2");
				_scWidgetUtils.disableWidget(this, "extn_EstimatedDelay3");
				_scWidgetUtils.disableWidget(this, "extn_EstimatedDelay4");
				}*/
				this.extn_Curbside_Toggle(curbsideExtensionsAllowed);
				var inStorePickupFlagEnabled = _iasContextUtils.getFromContext("InStorePickupFlagEnabled");
				this.extn_Instore_Toggle(inStorePickupFlagEnabled)
				/* OMNI-79562 Curbside time delay : Extensions Allowed Feature Changes - END*/
				this.extn_CurbsideOrderDeliveryTime(curbsideExtensionsAllowed);
				//OMNI-105502 - Start
				var inStorePickupFlagEnabled = _iasContextUtils.getFromContext("InStorePickupFlagEnabled");
				if(!_scBaseUtils.isVoid(instoreDefaultMins) && !_scBaseUtils.isVoid(inStorePickupFlagEnabled)){
					this.extn_InstoreOrderDeliveryTime(inStorePickupFlagEnabled);
				}
				//OMNI-105502 - End				
			}
			/*OMNI-79563 Estimated Curbside Delay Timer Count Changes - END */
    },
	//BOPIS-2043: After reducing the picked qty and trying to scan item again to pick, picked qty is not showing up in UI - end
	//BOPIS-2031_CR: BOPIS Default Pick - end
   	 //OMNI-79562 & OMNI-84734-Based on Curbside Extensions Allowed Feature Enabling/Hiding the curbside screen on Mobile/desktop UI
		extn_Curbside_Toggle: function(toggleValue){
		var shipmentModel = _scScreenUtils.getModel(this, "ShipmentDetails");
		var isCurbsideOrder=_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentModel);
		if(_scBaseUtils.equals(toggleValue ,"Y") && _iasContextUtils.isMobileContainer() && !_scBaseUtils.isVoid(isCurbsideOrder) && _scBaseUtils.equals(isCurbsideOrder,"Y")){
					_scWidgetUtils.showWidget(this, "extn_ExtendInfoPane");
					_scWidgetUtils.showWidget(this,"extn_CurbsideTimeDelay");	
					_scWidgetUtils.showWidget(this,"extn_assigncurbsideorder");
					_scWidgetUtils.showWidget(this,"extn_Attended_by");
					_scWidgetUtils.showWidget(this,"extn_completeCurbsideOrderBy"); //OMNI-79883
					}
					else{
						_scWidgetUtils.hideWidget(this, "extn_ExtendInfoPane");
						_scWidgetUtils.hideWidget(this,"extn_CurbsideTimeDelay");	
						_scWidgetUtils.hideWidget(this,"extn_assigncurbsideorder");
						_scWidgetUtils.hideWidget(this,"extn_Attended_by");
						_scWidgetUtils.hideWidget(this,"extn_completeCurbsideOrderBy"); //OMNI-79883
					}
		},
	//OMNI -80092 - Assign Curbside Order- Start
	/* OMNI-79883 - Complete Curbside Order By - START	*/
	extn_CurbsideOrderDeliveryTime: function(isCurbsideEnabled){
		var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");
		var isCurbsideOrder=_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentDetails);
		var strCurbsideDelayMins = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnCurbsideDelayMins",shipmentDetails);
		if(!_scBaseUtils.isVoid(isCurbsideOrder) && _scBaseUtils.equals(isCurbsideOrder,"Y") && !_scBaseUtils.isVoid(isCurbsideEnabled) && _scBaseUtils.equals(isCurbsideEnabled,"Y") && _scBaseUtils.equals(strCurbsideDelayMins,"0")){
			var strAppointmentNo = _scModelUtils.getStringValueFromPath("Shipment.AppointmentNo",shipmentDetails);
			var intCurbsideDefaultMins = parseInt(_iasContextUtils.getFromContext("CurbsideDefaultMins"));
			var appointmentDate = new Date(strAppointmentNo.replace(/^(\d{4})(\d\d)(\d\d)(\d\d)(\d\d)(\d\d)$/,'$4:$5:$6 $2/$3/$1'));
			var actualCurbsideDeliveryDate = new Date(appointmentDate.getTime() + intCurbsideDefaultMins*60000);
			var actualCurbsideTime = actualCurbsideDeliveryDate.toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
			//_scWidgetUtils.showWidget(this,"extn_completeCurbsideOrderBy");	
			_scWidgetUtils.setValue(this, "extn_finishCurbsideOrderBy", actualCurbsideTime, false);
		}
		else if(!_scBaseUtils.isVoid(isCurbsideOrder) && _scBaseUtils.equals(isCurbsideOrder,"Y") && !_scBaseUtils.isVoid(isCurbsideEnabled) && _scBaseUtils.equals(isCurbsideEnabled,"Y") && !_scBaseUtils.equals(strCurbsideDelayMins,"0")){
			var strFinialCurbsideTime = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnCurbsideExpectedDeliveryTS",shipmentDetails);
			var finialCurbsideTimeInTS = new Date(strFinialCurbsideTime);
			var finalCurbsideTime = finialCurbsideTimeInTS.toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
			//_scWidgetUtils.showWidget(this,"extn_completeCurbsideOrderBy");	
			_scWidgetUtils.setValue(this, "extn_finishCurbsideOrderBy", finalCurbsideTime, false);
		}
	},
		/* OMNI-79883 - Complete Curbside Order By - END	*/
	//OMNI-105502 - Start
	extn_Instore_Toggle: function(toggleValue){
		var shipmentModel = _scScreenUtils.getModel(this, "ShipmentDetails");
		var isInstoreOrder=_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsInstorePickupOpted",shipmentModel);
		var isCurbside =_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentModel);
		
		if(_scBaseUtils.equals(toggleValue ,"Y") && _scBaseUtils.isVoid(isInstoreOrder) && _scBaseUtils.isVoid(isCurbside)){
		isInstoreOrder = toggleValue;

		//OMNI-105857 - Start
		var IsInstoreConsolidationEnabled = _iasContextUtils.getFromContext("InStoreConsolidationToggle")//OMNI-108946
		if(_scBaseUtils.equals(IsInstoreConsolidationEnabled ,"Y")){//OMNI-108946
		var appointmentNo = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.Shipment.0.AppointmentNo",shipmentModel);
		}
		else {//OMNI-108946
			var instoreShipmentOutput = _scScreenUtils.getModel(this, "extn_curbside");//OMNI-108946
			var appointmentNo = _scModelUtils.getStringValueFromPath("ShipmentLines.Shipment.0.AppointmentNo",instoreShipmentOutput);//OMNI-108946
		}//OMNI-108946
		_scModelUtils.setStringValueAtModelPath("Shipment.AppointmentNo", appointmentNo, shipmentModel);
		_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnIsInstorePickupOpted", isInstoreOrder, shipmentModel);
		_scScreenUtils.setModel(this, "ShipmentDetails", shipmentModel, null);
		//OMNI-105857 - End
		}

		if(_scBaseUtils.equals(toggleValue ,"Y") && _iasContextUtils.isMobileContainer() && !_scBaseUtils.isVoid(isInstoreOrder) && _scBaseUtils.equals(isInstoreOrder,"Y")){
			_scWidgetUtils.showWidget(this,"extn_completeInstoreOrderBy");
			_scWidgetUtils.showWidget(this,"extn_InstoreReset");//OMNI-105500
			_scWidgetUtils.showWidget(this, "extn_InStoreAttendedBy"); //OMNI-105503 - Start/End
			//OMNI-105578 - Start
            var instoreAssigneeUpdateOutput = _scScreenUtils.getModel(this, "extn_curbside");
            this.extn_instoreOrderAssignee(instoreAssigneeUpdateOutput, shipmentModel);
            //OMNI-105578 - End

		}
		else{
			_scWidgetUtils.hideWidget(this,"extn_completeInstoreOrderBy");
			_scWidgetUtils.hideWidget(this,"extn_InstoreReset");//OMNI-105500
			_scWidgetUtils.hideWidget(this, "extn_InStoreAttendedBy"); //OMNI-105503 - Start/End
		}
	},
	//OMNI-105502 - End
	//OMNI-105578 - Start
    extn_instoreOrderAssignee: function(instoreAssigneeUpdateOutput, shipmentModel){         
        var IsInstoreConsolidationEnabled = _iasContextUtils.getFromContext("InStoreConsolidationToggle");
        if(_scBaseUtils.equals(IsInstoreConsolidationEnabled, "Y")){
            var updatedAttendee=_scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine.0.Shipment.0.Extn.ExtnInstoreAttendedBy",instoreAssigneeUpdateOutput);
        } else {
            var updatedAttendee=_scModelUtils.getStringValueFromPath("ShipmentLines.Shipment.0.Extn.ExtnInstoreAttendedBy",instoreAssigneeUpdateOutput);
        }
        
        var shipmentDetailsAttendee=_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnInstoreAttendedBy",shipmentModel);
            
        if(_scBaseUtils.isVoid(shipmentDetailsAttendee) || (!(_scBaseUtils.equals(shipmentDetailsAttendee ,updatedAttendee)) && 
        !(_scBaseUtils.isVoid(updatedAttendee))) ){

            _scWidgetUtils.setValue(this, "extn_InStoreAttendedBy", updatedAttendee, false);
            _scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnInstoreAttendedBy", updatedAttendee, shipmentModel);
            _scScreenUtils.setModel(this, "ShipmentDetails", shipmentModel, null);
	}
 },
    //OMNI-105578 - End
	extn_onassigncurbsideorder_button_onClick: function(event, bEvent, ctrl, args) {
		var shipmentModel = _scScreenUtils.getModel(this, "ShipmentDetails");
		var loginId = _iasContextUtils.getFromContext("Loginid");
		var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentModel);
		var curbsideAttendedBy = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnCurbsideAttendedBy",shipmentModel); 
		
		if (_scBaseUtils.isVoid(curbsideAttendedBy)){
			var loginId = _iasContextUtils.getFromContext("Loginid");
			var rootElement = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
			_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, rootElement);
			_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnCurbsideAttendedBy", loginId , rootElement);
			_iasUIUtils.callApi(this, rootElement, "extn_CurbsideChangeShipmentForAssignee_ref", null);
		 }
		else {
		 var curbsideAttendedByUserId= (curbsideAttendedBy.match(/\((.*)\)/).pop());
			//OMNI-79640- User already assigned popup - Start
			if (!_scBaseUtils.equals(loginId,curbsideAttendedByUserId)){
				this.extn_alreadyAssignedPopup(event,bEvent,ctrl,args,curbsideAttendedBy)	
			}
		//OMNI-80183-CurbsideSameUserAssignedPopUp - START
      //this function is for once assignee is assigned,again trying to assign same user displaying the pop-up
				else if (_scBaseUtils.equals(curbsideAttendedByUserId,loginId)){
				var bindings = null;
				bindings = {};
				bindings["CurbsideAttendedBy"] = curbsideAttendedBy;
				var screenConstructorParams = null;
				screenConstructorParams = {};
				var popupParams = null;
				popupParams = {};
				popupParams["binding"] = bindings;
				popupParams["screenConstructorParams"] = screenConstructorParams;
				var dialogParams = null;
				dialogParams = {};
				dialogParams["closeCallBackHandler"] = "extnCurbsideSameUserAssignedPopUp";
				dialogParams["class"] = "popupTitleBorder fixedActionBarDialog"; 
				_iasUIUtils.openSimplePopup("extn.components.shipment.customerpickup.PopUp.CurbsideSameUserAssignedPopUp.CurbsideSameUserAssignedPopUp", "", this, popupParams, dialogParams);
				}
				//OMNI-80183-CurbsideSameUserAssignedPopUp - END
		}
	},
	// This method is for showing pop-up when user clicks on assign to me button estimatted delay buttons while the shipemnt is already assigned to other user
	extn_alreadyAssignedPopup: function(event, bEvent, ctrl, args, curbsideAttendedBy) {
	var bindings = null;
			bindings = {};
			bindings["UserName"] = curbsideAttendedBy;
			var screenConstructorParams = null;
			screenConstructorParams = {};
			var popupParams = null;
			popupParams = {};
			popupParams["binding"] = bindings;
			popupParams["screenConstructorParams"] = screenConstructorParams;
			var dialogParams = null;
			dialogParams = {};
			dialogParams["closeCallBackHandler"] = "extn_CurbsideAlreadyAssignedPopupOnSelect";
			dialogParams["class"] = "popupTitleBorder fixedActionBarDialog";
		_iasUIUtils.openSimplePopup("extn.components.shipment.customerpickup.PopUp.CurbsideAlreadyAssignedPopup.CurbsideAlreadyAssignedPopup","" , this, popupParams, dialogParams);
	},

//This method excuted when user clicks on yes/no buttons on CurbsideAlreadyAssignedPopup
	extn_CurbsideAlreadyAssignedPopupOnSelect: function(event, bEvent, ctrl, args) {
	var datalabelValue = _iasContextUtils.getFromContext("ConfirmAssignment");
		var clearSessionObject = _scBaseUtils.getNewModelInstance();
		if ( _scBaseUtils.equals(datalabelValue, "Y")) 
		{ 
		var shipmentModel = _scScreenUtils.getModel(this, "ShipmentDetails");	
		var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentModel);
		var loginId = _iasContextUtils.getFromContext("Loginid");
		var rootElement = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, rootElement);
		_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnCurbsideAttendedBy", loginId , rootElement);
		_iasUIUtils.callApi(this, rootElement, "extn_CurbsideChangeShipmentForAssignee_ref", null);
		}
		_iasContextUtils.addToContext("ConfirmAssignment", clearSessionObject);	
	},
	//OMNI -80092 - Assign Curbside Order- End
	//BOPIS-2033_CR: Change the BOPIS order pickup flow - begin
	extn_onFinishPickButtonClick: function(event, bEvent, ctrl, args) {
       //Combining Customer Pickup screen OMNI-8660 -Start
      	var shipmentModel = _scScreenUtils.getModel(
               this, "ShipmentDetails");
		    var customerVerificationNotesModel = null;
            customerVerificationNotesModel = _scScreenUtils.getTargetModel(
            this, "extn_CustomerVerificationNotes_Input", null);
            
            var PickedByCustomermodel = null;
            PickedByCustomermodel = _scScreenUtils.getTargetModel(
            this, "extn_Test", null);
            var PaymentStatus = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.Order.PaymentStatus", shipmentModel);
       
            var shipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentModel);
            var CustomerPickedQuantity = null;
            var TotalCustomerPickedQuantity = null;
        if (!_scBaseUtils.isVoid(shipmentLineList)) {
            for (var i=0;i<shipmentLineList.length;i++) {
                var quantity = _scModelUtils.getNumberValueFromPath("OriginalQuantity", shipmentLineList[i]);
                CustomerPickedQuantity = _scModelUtils.getNumberValueFromPath("CustomerPickedQuantity", shipmentLineList[i]);
                if(!_scBaseUtils.equals(Number(CustomerPickedQuantity), 0.00)) {                   
                   break;
                }
                var shortageQty = _scModelUtils.getNumberValueFromPath("ShortageQty", shipmentLineList[i]);
            }
        }
              if((_scBaseUtils.isVoid(customerVerificationNotesModel)) && 
              (_scBaseUtils.equals(PaymentStatus,"AUTHORIZED")) 
              && !_scBaseUtils.equals(Number(CustomerPickedQuantity), 0.00)) {
              	    _iasBaseTemplateUtils.showMessage(this, "Customer Verification method is not selected. Please select valid Customer Verification method.", "error", null);
                     _scEventUtils.stopEvent(bEvent);
					  isExtnStagingLocationNotAssigned = true;   
                } else if((_scBaseUtils.isVoid(PickedByCustomermodel)) && (_scBaseUtils.equals(PaymentStatus,"AUTHORIZED")) 
                  && !_scBaseUtils.equals(Number(CustomerPickedQuantity), 0.00)) {
                	_iasBaseTemplateUtils.showMessage(this, "Picked up by field is not selected. Please select valid Picked up by person Name.", "error", null);
                    _scEventUtils.stopEvent(bEvent);
					 isExtnStagingLocationNotAssigned = true;   
                }
                          

             else {
  //Combining Customer Pickup screen OMNI-8660 -End      

        var isAgeRestricted=false;
        var lShipmentLine = _scModelUtils.getModelObjectFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentModel);
		var sLength = lShipmentLine.length;
		for(var i=0; i < sLength; i++) {
			var shipmentLine = lShipmentLine[i];
			if(_scBaseUtils.equals("Y", shipmentLine.OrderLine.Extn.ExtnIsAgeRestricted)){
				isAgeRestricted=true;
					break;
			}
		}
		if(!isAgeRestricted){
			
              this.validateShipmentOnNext();
		}
		else{
			 if((_scBaseUtils.equals(PaymentStatus,"AWAIT_PAY_INFO")) ||(_scBaseUtils.equals(PaymentStatus,"AWAIT_AUTH")) ) {
			 	
			 	  this.save();
			 } else{ 
			 this.extn_beforeSave();
			 }
		}
            
            }
	},

	handlePendingShipmentLineList: function(modelOutput) {
		var shortageLines = 0;
		shortageLines = _scModelUtils.getNumberValueFromPath("ShipmentLines.TotalNumberOfRecords", modelOutput);
		if (
		shortageLines >= 1) {
			_scScreenUtils.showConfirmMessageBox(
			this, _scScreenUtils.getString(
			this, "Message_CustomerPickPendingLines"), "handleResolvePendingShipmentLineList", null, null);
		} else {
			this.extn_beforeRecordCustPickCall(modelOutput);
		}
	},

	extn_beforeRecordCustPickCall: function(modelOutput){
		var shipmentModel = _scScreenUtils.getModel(
                this, "ShipmentDetails");
		var rootElement = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
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
		var isEligibleforConsolidation = ""; //OMNI-85831
		
		//OMNI-85179 curbside cons - complete button changes -Start
		var curbsideShipmentlineDetails = _scScreenUtils.getModel(this, "extn_curbside");
		var curbsideConsFlag = _iasContextUtils.getFromContext("CurbsideConsolidationToggle");
		var instoreConsToggle = _iasContextUtils.getFromContext("InStoreConsolidationToggle");
		var instoreConsFlag = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.InstoreConsolidationFlag", shipmentModel);
		var isCurbsideOrder=_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentModel);
		if(_iasContextUtils.isMobileContainer() && (_scBaseUtils.equals(shipmentStatus, "1100.70.06.30.5") || _scBaseUtils.equals(shipmentStatus, "1100.70.06.30.7"))){
			//OMNI-102218 - Start
			if(!_scBaseUtils.isVoid(instoreConsToggle) && _scBaseUtils.equals(instoreConsToggle,"Y") && !_scBaseUtils.isVoid(instoreConsFlag) && _scBaseUtils.equals(instoreConsFlag,"Y")) { 
				var consldShipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ConsolidatedShipmentKey", shipmentModel);
				_scModelUtils.setStringValueAtModelPath("Shipment.ConsolidatedShipmentKey", consldShipmentKey, rootElement);
				var instoreConsldToggle = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.InstoreConsolidationFlag", shipmentModel);
				_scModelUtils.setStringValueAtModelPath("Shipment.InstoreConsToggle", instoreConsldToggle, rootElement);
				isEligibleforConsolidation = "Y"; //OMNI-85831
			}
			//OMNI-102218 - End
			else if(!_scBaseUtils.isVoid(curbsideConsFlag) && _scBaseUtils.equals(curbsideConsFlag,"Y") && !_scBaseUtils.isVoid(isCurbsideOrder) && _scBaseUtils.equals(isCurbsideOrder,"Y")) { 
				var curbsideShipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.CurbsideShipmentKey", shipmentModel);
				_scModelUtils.setStringValueAtModelPath("Shipment.CurbsideShipmentKey", curbsideShipmentKey, rootElement);
				var curbsideConsToggle = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.CurbsideConsolidationFlag", shipmentModel);
				_scModelUtils.setStringValueAtModelPath("Shipment.CurbsideConsToggle", curbsideConsToggle, rootElement);
				isEligibleforConsolidation = "Y"; //OMNI-85831
			}
		}
        //OMNI-85179 curbside cons - complete button changes -End
		var lShipmentLine = _scModelUtils.getModelObjectFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentModel);
		var sLength = lShipmentLine.length;

		var aShipmentLine = [];

		for(var i=0; i < sLength; i++) {
			var tempShipmentLine={};
			var qty=0;
			var shipmentLine = lShipmentLine[i];
			var itemId = _scModelUtils.getStringValueFromPath("ItemID", shipmentLine);
			var customerPickedQuantity = _scModelUtils.getStringValueFromPath("CustomerPickedQuantity", shipmentLine);
			var quantity = _scModelUtils.getStringValueFromPath("Quantity", shipmentLine);
			var orderNo = _scModelUtils.getStringValueFromPath("OrderNo", shipmentLine);
			var cancelReason = _scModelUtils.getStringValueFromPath("CancelReason", shipmentLine);
			var shipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", shipmentLine);
			var shipmentKey = _scModelUtils.getStringValueFromPath("ShipmentKey", shipmentLine); //OMNI-85179 curbside cons - complete button changes
			//BOPIS-2060 Start
			var PaymentSts = _scModelUtils.getStringValueFromPath("PaymentStatus", shipmentLine.Order);
			//BOPIS-2060 End

			var strCPQ=null;
			if(!_scBaseUtils.isVoid(customerPickedQuantity)){
				strCPQ=customerPickedQuantity.toString();
			}	
			if(_scBaseUtils.equals("NaN", strCPQ)){
				customerPickedQuantity=0;
			}


			if (!_scBaseUtils.isVoid(customerPickedQuantity)){
				var qty = Number(quantity) - Number(customerPickedQuantity);
			}
			//BOPIS-2060 Start
			else if(_scBaseUtils.equals(PaymentSts, "AWAIT_AUTH")||_scBaseUtils.equals(PaymentSts, "AWAIT_PAY_INFO")||!_scBaseUtils.isVoid(cancelReason)){
				customerPickedQuantity=0;
			}
			//BOPIS-2060 End
			else{
				customerPickedQuantity=quantity;
			}

			_scModelUtils.setStringValueAtModelPath("ItemID", itemId, tempShipmentLine);
			_scModelUtils.setStringValueAtModelPath("CustomerPickedQuantity", customerPickedQuantity, tempShipmentLine);
			if (!_scBaseUtils.isVoid(customerPickedQuantity) && !_scBaseUtils.equals(Number(customerPickedQuantity), 0.00)) {
				this.isCustomerPickedUp = true;
			}
			_scModelUtils.setStringValueAtModelPath("Quantity", quantity, tempShipmentLine);
			_scModelUtils.setStringValueAtModelPath("OrderNo", orderNo, tempShipmentLine);
			_scModelUtils.setStringValueAtModelPath("ActualQuantity", quantity, tempShipmentLine);
			_scModelUtils.setStringValueAtModelPath("ShipmentLineKey", shipmentLineKey, tempShipmentLine);
			_scModelUtils.setStringValueAtModelPath("ShipmentKey", shipmentKey, tempShipmentLine); //OMNI-85179 curbside cons - complete button changes
			// _scModelUtils.setStringValueAtModelPath("ShortageQty", qty, tempShipmentLine);
			_scModelUtils.setStringValueAtModelPath("Extn", {}, tempShipmentLine);
			//_scModelUtils.setStringValueAtModelPath("Extn.ExtnReasonCode", cancelReason, tempShipmentLine);

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
				//BOPIS-2065 API Error- Start
				_scModelUtils.setStringValueAtModelPath("Extn.ExtnReasonCode", cancelReason, tempShipmentLine);
				//BOPIS-2065 API Error- End
				this.isShorted = true;
			}
			aShipmentLine[i]=tempShipmentLine;
		}
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines", {}, rootElement);
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine", aShipmentLine, rootElement);
		//OMNI-85831 - Start
		if((_scBaseUtils.equals(PaymentSts, "AWAIT_AUTH")||_scBaseUtils.equals(PaymentSts, "AWAIT_PAY_INFO"))
			&& _scBaseUtils.equals(isEligibleforConsolidation, "Y")) {
				_scModelUtils.setStringValueAtModelPath("Shipment.ConsolidatedShipmentKey", _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", rootElement),
					rootElement);
		}
		//OMNI-85831 - End
		if(this.isShorted) {
			_iasUIUtils.callApi(this, rootElement, "extn_UnResrvMsgToSIMOnShrtg", null);
			//_scEventUtils.stopEvent(bEvent);
		}
		else{
			this.extn_callRecordCustPick(modelOutput);
		}
	},
	
	extn_callRecordCustPick: function(modelOutput){
		var shipmentModel = null;
		shipmentModel = _scScreenUtils.getTargetModel(
			this, "recordCustomerPick_input", null);
        var customerVerification = null;
		customerVerification = _iasUIUtils.getWizardModel(
			this, "customerVerificationModel");

		var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");
		var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentDetails);
		var pickedUpCustomer = _scUserprefs.getProperty(shipmentKey);
		// OMNI-3637 - BOPIS: Automate Authorization Failure - START
		var PaymentStatus = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.Order.PaymentStatus", shipmentDetails);
   // OMNI-3637 - BOPIS: Automate Authorization Failure - START
  //Combining Customer Pickup screen OMNI-8660 -Start
		var customerVerificationNotesModel = null;
            customerVerificationNotesModel = _scScreenUtils.getTargetModel(
            this, "extn_CustomerVerificationNotes_Input", null);
      
            shipmentDetailModel = _scScreenUtils.getModel(
            this, "ShipmentDetails");
            var customerVerificationModel = null;            
            if(_scBaseUtils.isVoid(customerVerificationModel)) {
            	var customerVerificationModel = {};
			customerVerificationModel.Shipment={};
			customerVerificationModel.Shipment.Notes = {};
			customerVerificationModel.Shipment.Notes.Note = {};
			customerVerificationModel.Shipment.Notes.Note.ReasonCode = "YCD_CUSTOMER_VERIFICATION";
			customerVerificationModel.Shipment.Notes.Note.ContactUser = _scUserprefs.getUserId();
			var FirstName = shipmentDetailModel.Shipment.BillToAddress.FirstName;
			var LastName = shipmentDetailModel.Shipment.BillToAddress.LastName;
			var customerName = FirstName +" "+ LastName;
            var verificationMethodValue = _scModelUtils.getStringValueFromPath("Notes.Note.NoteText", customerVerificationNotesModel);
			var noteText = customerName + " came to pick up products and was verified by " +verificationMethodValue+ ".";
            customerVerificationModel.Shipment.Notes.Note.NoteText= noteText;
            }
            		shipmentModel = _scBaseUtils.mergeModel(
		shipmentModel, customerVerificationModel, false);
    //Combining Customer Pickup screen OMNI-8660 -End


		_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnShipmentPickedBy", pickedUpCustomer, shipmentModel);
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, shipmentModel);
		//OMNI-85172 Start
			var curbsideShipmentlineDetails = _scScreenUtils.getModel(this, "extn_curbside");
			var curbsideConsFlag = _iasContextUtils.getFromContext("CurbsideConsolidationToggle");
			var isCurbsideOrder=_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentDetails);
			var shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status", shipmentDetails);
			var instoreConsToggle = _iasContextUtils.getFromContext("InStoreConsolidationToggle"); //OMNI-102403
			var instoreConsFlag = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.InstoreConsolidationFlag", shipmentDetails); //OMNI-102403
			var isEligibleForConsolidation = "";
			if(_iasContextUtils.isMobileContainer() && (_scBaseUtils.equals(shipmentStatus, "1100.70.06.30.5") || _scBaseUtils.equals(shipmentStatus, "1100.70.06.30.7"))){
				//OMNI-102403 - Start
				if(!_scBaseUtils.isVoid(instoreConsToggle) && _scBaseUtils.equals(instoreConsToggle,"Y") && !_scBaseUtils.isVoid(instoreConsFlag) && _scBaseUtils.equals(instoreConsFlag,"Y")) { 
					var consolidatedShipmentCons = _scModelUtils.getStringValueFromPath("ShipmentLines.ConsolidatedShipment",curbsideShipmentlineDetails);
					_scModelUtils.setStringValueAtModelPath("Shipment.ConsolidatedShipment",consolidatedShipmentCons,shipmentModel);
					isEligibleForConsolidation = "Y";
				}//OMNI-102403 - End			
				else if(!_scBaseUtils.isVoid(curbsideConsFlag) && _scBaseUtils.equals(curbsideConsFlag,"Y") && !_scBaseUtils.isVoid(isCurbsideOrder) && _scBaseUtils.equals(isCurbsideOrder,"Y")) { 
					var curbsideShipmentCons = _scModelUtils.getStringValueFromPath("ShipmentLines.CurbsideShipment",curbsideShipmentlineDetails);
					_scModelUtils.setStringValueAtModelPath("Shipment.CurbsideShipment",curbsideShipmentCons,shipmentModel);
					isEligibleForConsolidation = "Y";
				}
			}
		
		if(this.isCustomerPickedUp) {
			if(!_scBaseUtils.isVoid(isEligibleForConsolidation) && _scBaseUtils.equals(isEligibleForConsolidation,"Y")){
				_iasUIUtils.callApi(this, shipmentModel, "extn_recordCustomerPickForCurbsideConsolidation", null);
			}
			else{
			_iasUIUtils.callApi(this, shipmentModel, "extn_customerpickup_recordCustomerPickup_Ref", null);
            }
        }
		//OMNI-85172 End
     // OMNI-3637 - BOPIS: Automate Authorization Failure - START
		else if((_scBaseUtils.equals(PaymentStatus,"AWAIT_PAY_INFO")) ||(_scBaseUtils.equals(PaymentStatus,"AWAIT_AUTH")) )
		 {
		  var targetModel = {};
		  targetModel.Shipment={};
	          var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");
	          targetModel.Shipment.ShipmentKey=shipmentDetails.Shipment.ShipmentKey;
		  _iasUIUtils.openWizardInEditor("wsc.components.shipment.summary.ShipmentSummaryWizard", targetModel, "wsc.desktop.editors.ShipmentEditor", this, null);
			
		}
     // OMNI-3637 - BOPIS: Automate Authorization Failure - END
		else {
			_iasScreenUtils.showInfoMessageBoxWithOk(this, _scScreenUtils.getString(this, "Message_RecordCustomerPickCancel"), "extn_gotoNextScreen", null);
		}
	},
	resetCurbsideOrder: function(event, bEvent, ctrl, args) {
	var argBean = null;
		argBean = {};
		var msg = _scScreenUtils.getString(this, "extn_Reset_CurbSide_Order");
		_scScreenUtils.showConfirmMessageBox(this, msg, "extn_confirmResetCurbsideOrder", null, argBean);
	},
//Paper Work button on-click -End

	/* This method is called after clicking on Yes/No options in Begin FireArm Paper work message box */
	extn_confirmResetCurbsideOrder: function(result, args) 
	{
		if (_scBaseUtils.equals(result, "Ok"))
		{
				var ShipmentDetailsModel = null;
				ShipmentDetailsModel = _scScreenUtils.getModel(this, "ShipmentDetails");
				var deliveryMethod=_scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod",ShipmentDetailsModel);
				var isCurbsidePickupOpted=_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",ShipmentDetailsModel);
		
						var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", ShipmentDetailsModel);
						var shipmentNo = _scModelUtils.getStringValueFromPath("Shipment.ShipmentNo", ShipmentDetailsModel);
						var OrderNumber = _scModelUtils.getStringValueFromPath("Shipment.OrderNo", ShipmentDetailsModel);
						var sStatus = _scModelUtils.getStringValueFromPath("Shipment.Status", ShipmentDetailsModel);//OMNI-87872
                       
					//	var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
					//	_scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod",deliveryMethod,inputModel);
					//	_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentNo", shipmentNo, inputModel);
					//	_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLine.OrderNo", OrderNumber, inputModel);
					//  _scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",isCurbsidePickupOpted,inputModel);
					//	_iasUIUtils.callApi(this, inputModel, "deleteNotification_ref", null);
    
						//var inputModel1 = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
						//_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, inputModel1);
						//_scModelUtils.setStringValueAtModelPath("Shipment.OrderNo", OrderNumber, inputModel1);
						//_scModelUtils.setStringValueAtModelPath("Shipment.ResetCurbside", "Y", inputModel1);
						//_scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod",deliveryMethod,inputModel1);
						
						//_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnCurbsidePickupInfo", "", inputModel1);
						//_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnIsCurbsidePickupOpted", "", inputModel1);
						var inputModel1 = _scModelUtils.createNewModelObjectWithRootKey("Order");
						_scModelUtils.setStringValueAtModelPath("Order.ResetCurbside", "Y", inputModel1);
						_scModelUtils.setStringValueAtModelPath("Order.OrderNo", OrderNumber, inputModel1);
						_scModelUtils.setStringValueAtModelPath("Order.ShipmentKey", shipmentKey, inputModel1);
						_scModelUtils.setStringValueAtModelPath("Order.ShipmentNo",shipmentNo,inputModel1);
						
						//OMNI-85172 Start
                        var curbsideShipmentlineDetails = _scScreenUtils.getModel(this, "extn_curbside");
                        var curbsideConsFlag =  _iasContextUtils.getFromContext("CurbsideConsolidationToggle");
						var instoreConsFlag =  _iasContextUtils.getFromContext("InStoreConsolidationToggle");
						var sCurbsideConsolidationFlag = _scModelUtils.getStringValueFromPath("ShipmentLines.CurbsideConsolidationFlag",curbsideShipmentlineDetails); //OMNI-87872
						var sInstoreConsolidationFlag = _scModelUtils.getStringValueFromPath("ShipmentLines.InstoreConsolidationFlag",curbsideShipmentlineDetails); //OMNI-87872
						
						//Reset Curbside During Instore "Y" - Fix
                        if (!_scBaseUtils.isVoid(instoreConsFlag) && _scBaseUtils.equals(instoreConsFlag,"Y") && (!_scBaseUtils.isVoid(sInstoreConsolidationFlag) &&
						_scBaseUtils.equals(sInstoreConsolidationFlag,"Y") && (_scBaseUtils.equals(sStatus, "1100.70.06.30.5") || 
						_scBaseUtils.equals(sStatus, "1100.70.06.30.7")))){	
							var consolidatedShipmentKey = _scModelUtils.getStringValueFromPath("ShipmentLines.ConsolidatedShipmentKey",curbsideShipmentlineDetails);
							_scModelUtils.setStringValueAtModelPath("Order.ShipmentKey",consolidatedShipmentKey,inputModel1);
						}						
						else if(!_scBaseUtils.isVoid(curbsideConsFlag) && _scBaseUtils.equals(curbsideConsFlag,"Y") && !_scBaseUtils.isVoid(isCurbsidePickupOpted) && 
						_scBaseUtils.equals(isCurbsidePickupOpted,"Y")){
							//OMNI-87872 - added below if 
							//OMNI-88612 - START Added Changes for Shipment in PaperWork initiated status
							if(!_scBaseUtils.isVoid(sCurbsideConsolidationFlag) && _scBaseUtils.equals(sCurbsideConsolidationFlag,"Y") &&
							(_scBaseUtils.equals(sStatus, "1100.70.06.30.5") || _scBaseUtils.equals(sStatus, "1100.70.06.30.7"))){	
								var curbsideShipmentKey = _scModelUtils.getStringValueFromPath("ShipmentLines.CurbsideShipmentKey",curbsideShipmentlineDetails);
								_scModelUtils.setStringValueAtModelPath("Order.ShipmentKey",curbsideShipmentKey,inputModel1);
							}
                        } 
						//OMNI-88612 - END
                        //OMNI-85172 End						
						_iasUIUtils.callApi(this, inputModel1, "extn_resetCurbsideOrders_ref", null);
						this.extn_curbsideresetpopup();				
			}
	   },
	   
	extn_curbsideresetpopup: function(event, bEvent, ctrl, args){
        _wscMobileHomeUtils.openScreen("wsc.mobile.home.MobileHome", "wsc.mobile.editors.MobileEditor");			
	},
	
	//OMNI-105500,OMNI-105674 - Start
	resetInstoreOrder: function(event, bEvent, ctrl, args) {
	var argBean = null;
		argBean = {};
		var msg = _scScreenUtils.getString(this, "extn_Reset_Instore_Order");
		_scScreenUtils.showConfirmMessageBox(this, msg, "extn_confirmResetInstoreOrder", null, argBean);
	},
	extn_confirmResetInstoreOrder: function(result, args) 
	{
		if (_scBaseUtils.equals(result, "Ok"))
		{
				var ShipmentDetailsModel = null;
				ShipmentDetailsModel = _scScreenUtils.getModel(this, "ShipmentDetails");
				var deliveryMethod=_scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod",ShipmentDetailsModel);
		
				var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", ShipmentDetailsModel);
				var shipmentNo = _scModelUtils.getStringValueFromPath("Shipment.ShipmentNo", ShipmentDetailsModel);
				var OrderNumber = _scModelUtils.getStringValueFromPath("Shipment.OrderNo", ShipmentDetailsModel);
				var sStatus = _scModelUtils.getStringValueFromPath("Shipment.Status", ShipmentDetailsModel);
				var inputModel1 = _scModelUtils.createNewModelObjectWithRootKey("Order");
				_scModelUtils.setStringValueAtModelPath("Order.ResetInstorePickUp", "Y", inputModel1);
				_scModelUtils.setStringValueAtModelPath("Order.OrderNo", OrderNumber, inputModel1);
				_scModelUtils.setStringValueAtModelPath("Order.ShipmentKey", shipmentKey, inputModel1);
				_scModelUtils.setStringValueAtModelPath("Order.ShipmentNo",shipmentNo,inputModel1);
				var consdShipmentlineDetails = _scScreenUtils.getModel(this, "extn_curbside");
				
				var instoreConsFlag =  _iasContextUtils.getFromContext("InStoreConsolidationToggle");
				var sInstoreConsolidationFlag = _scModelUtils.getStringValueFromPath("ShipmentLines.InstoreConsolidationFlag",consdShipmentlineDetails);
				
				if (!_scBaseUtils.isVoid(instoreConsFlag) && _scBaseUtils.equals(instoreConsFlag,"Y") && (!_scBaseUtils.isVoid(sInstoreConsolidationFlag) &&
						_scBaseUtils.equals(sInstoreConsolidationFlag,"Y") && (_scBaseUtils.equals(sStatus, "1100.70.06.30.5") ||
						_scBaseUtils.equals(sStatus, "1100.70.06.30.7")))){	
							var consolidatedShipmentKey = _scModelUtils.getStringValueFromPath("ShipmentLines.ConsolidatedShipmentKey",consdShipmentlineDetails);
							_scModelUtils.setStringValueAtModelPath("Order.ShipmentKey",consolidatedShipmentKey,inputModel1);
				}	
				
				_iasUIUtils.callApi(this, inputModel1, "extn_resetInstoreOrders_ref", null);
				this.extn_instoreResetPopup();				
		}
	},
	   
	extn_instoreResetPopup: function(event, bEvent, ctrl, args){
        _wscMobileHomeUtils.openScreen("wsc.mobile.home.MobileHome", "wsc.mobile.editors.MobileEditor");			
	},
	//OMNI-105500,OMNI-105674 - End
	
	extn_gotoNextScreen: function(modelOutput){
		var targetModel = {};
		targetModel.Shipment={};
		if (!_scBaseUtils.isVoid(modelOutput) && !_scBaseUtils.isVoid(modelOutput.Shipment) && !_scBaseUtils.isVoid(modelOutput.Shipment.ShipmentKey)){
			targetModel.Shipment.ShipmentKey=modelOutput.Shipment.ShipmentKey;
		}
		else{
			var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");
			targetModel.Shipment.ShipmentKey=shipmentDetails.Shipment.ShipmentKey;
		}
		targetModel.Shipment.IsFromProductVerification = "Y"; //OMNI-99079
		_iasUIUtils.openWizardInEditor("wsc.components.shipment.summary.ShipmentSummaryWizard", targetModel, "wsc.desktop.editors.ShipmentEditor", this, null);
	},
	
	extn_onPreviousButtonClick: function(event, bEvent, ctrl, args) {
		_scEventUtils.fireEventToParent(this, "closeBttn_onClick", null);
	},
	
	extn_onCloseButtonClick: function(event, bEvent, ctrl, args) {
		_scEventUtils.fireEventToParent(this, "closeBttn_onClick", null);
	},
	//BOPIS-2033_CR: Change the BOPIS order pickup flow - end
	
	//OMNI-8710, 8717: UI Align and Payment Info - Start
	getCityStateZip: function(
        dataValue, screen, widget, namespace, modelObject, options) {
		var Address = null;
		var returnValue = "";
		Address = _scModelUtils.getModelObjectFromPath("Shipment.BillToAddress", modelObject);
		if (!(_scBaseUtils.isVoid(Address))) {
			var sCountry = "";
			sCountry = _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.Country", modelObject);
			var sAddressKey = "";
			sAddressKey = _scBaseUtils.stringConcat(sCountry, "_CityStateZip");
			returnValue = _scScreenUtils.getFormattedString(screen, sAddressKey, Address);
			if (_scBaseUtils.equals(returnValue, sAddressKey)) {
				returnValue = _scScreenUtils.getFormattedString(screen, "CityStateZip", Address);
			}
		}
		return returnValue;
	},
	
	getPaymentType: function(
        dataValue, screen, widget, namespace, modelObject, options) {
		var PaymentMethods = null;
		var returnValue = "";
		var sPaymentType = "";
		var sCCType = "";
		var sPaymentRef5="";
		PaymentMethods = _scModelUtils.getModelObjectFromPath("Shipment.ShipmentLines.ShipmentLine.0.Order.PaymentMethods.PaymentMethod", modelObject);
		//PaymentMethods = modelObject.Shipment.ShipmentLines.ShipmentLine.0.Order;
		for(var i in PaymentMethods)
		{
			sPaymentType = _scModelUtils.getStringValueFromPath("PaymentType", PaymentMethods[i]);
			sCCType = _scModelUtils.getStringValueFromPath("CreditCardType", PaymentMethods[i]);
			sPaymentRef5=_scModelUtils.getStringValueFromPath("PaymentReference5",PaymentMethods[i]);
			if(!_scBaseUtils.isVoid(sCCType)){
				sCCType = sCCType.replace(/ +/g, "").toUpperCase();
			}		
			<!--OMNI-65894 Begin-->
            if(!_scBaseUtils.isVoid(sPaymentType)&&(_scBaseUtils.equals(sPaymentType,"Klarna")||_scBaseUtils.equals(sPaymentRef5,"Klarna")))
			{
				returnValue="Klarna";
				_scWidgetUtils.showWidget(this,"extn_PaymentMethod", false);
			}
            <!--OMNI-65894 End-->
		  else if(!_scBaseUtils.isVoid(sPaymentType) && _scBaseUtils.equals(sPaymentType,"Credit_Card") || _scBaseUtils.equals(sPaymentType,"CREDIT_CARD")){
				
                if(!_scBaseUtils.isVoid(sCCType) && _scBaseUtils.equals(sCCType,"VISA")){
                	if(_scBaseUtils.equals(widget,"extn_Visa")){
						returnValue = _scModelUtils.getStringValueFromPath("DisplayCreditCardNo", PaymentMethods[i]);
						_scWidgetUtils.showWidget(this,"extn_Visa", false);
					}
                }else if(!_scBaseUtils.isVoid(sCCType) && _scBaseUtils.equals(sCCType,"AMERICANEXPRESS")){
                	if(_scBaseUtils.equals(widget,"extn_AmericanExpress")){
						returnValue = _scModelUtils.getStringValueFromPath("DisplayCreditCardNo", PaymentMethods[i]);
						_scWidgetUtils.showWidget(this,"extn_AmericanExpress", false);
					}
                }else if(!_scBaseUtils.isVoid(sCCType) && _scBaseUtils.equals(sCCType,"DISCOVER")){
                	if(_scBaseUtils.equals(widget,"extn_Discover")){
						returnValue = _scModelUtils.getStringValueFromPath("DisplayCreditCardNo", PaymentMethods[i]);
						_scWidgetUtils.showWidget(this,"extn_Discover", false);
					}
                }else if(!_scBaseUtils.isVoid(sCCType) && _scBaseUtils.equals(sCCType,"MASTERCARD")){
                	if(_scBaseUtils.equals(widget,"extn_MasterCard")){
						returnValue = _scModelUtils.getStringValueFromPath("DisplayCreditCardNo", PaymentMethods[i]);
						_scWidgetUtils.showWidget(this,"extn_MasterCard", false);
					}
                }																
		    }else if(!_scBaseUtils.isVoid(sPaymentType) &&  _scBaseUtils.equals(sPaymentType,"PLCC")){
				if(_scBaseUtils.equals(widget,"extn_PLCC")){
					returnValue = _scModelUtils.getStringValueFromPath("DisplayCreditCardNo", PaymentMethods[i]);
					_scWidgetUtils.showWidget(this,"extn_PLCC", false);
				}
		    }else if(!_scBaseUtils.isVoid(sPaymentType) && _scBaseUtils.equals(sPaymentType,"GIFT_CARD")){
				if(_scBaseUtils.equals(widget,"extn_GiftCard")){
					returnValue = _scModelUtils.getStringValueFromPath("DisplaySvcNo", PaymentMethods[i]);
					_scWidgetUtils.showWidget(this,"extn_GiftCard", false);
				}
		    }else{
				if(_scBaseUtils.equals(widget,"extn_PaymentMethod")){
					if(!_scBaseUtils.isVoid(sPaymentType) && _scBaseUtils.equals(sPaymentType,"Apple_Pay")){
						returnValue = "ApplePay";
					}else if(!_scBaseUtils.isVoid(sPaymentType) && _scBaseUtils.equals(sPaymentType,"Google_Pay")){
						returnValue = "GooglePay";
					}else if(!_scBaseUtils.isVoid(sPaymentType) && _scBaseUtils.equals(sPaymentType,"PayPal") || _scBaseUtils.equals(sPaymentType,"Paypal")){
						returnValue = "PayPal";
					}
					_scWidgetUtils.showWidget(this,"extn_PaymentMethod", false);
				}
		    }
		}
		return returnValue;
	},
    //OMNI-8710, 8717: UI Align and Payment Info - End
    /* OMNI-79565 Curbside Delay Confirmation Start */
    //On Click Event Methods of four Time Delay Buttons
	extn_EstimatedDelay1_onClick: function(event, bEvent, ctrl, args) {
		var curbsideDelay1 = _scModelUtils.getModelObjectFromPath("CommonCodeList.CommonCode.0.CodeShortDescription" ,this.curbsideTimeDelays);
		//OMNI-81479 changes start
		this.popUpForDelaybtn(event,bEvent,ctrl,args,curbsideDelay1);
		//OMNI-81479 changes end 
	},
	extn_EstimatedDelay2_onClick: function(event, bEvent, ctrl, args) {
		var curbsideDelay2 = _scModelUtils.getModelObjectFromPath("CommonCodeList.CommonCode.1.CodeShortDescription" ,this.curbsideTimeDelays);
		//OMNI-81479 changes start
		this.popUpForDelaybtn(event,bEvent,ctrl,args,curbsideDelay2);
		//OMNI-81479 changes end
	},
	extn_EstimatedDelay3_onClick: function(event, bEvent, ctrl, args) {
		var curbsideDelay3 = _scModelUtils.getModelObjectFromPath("CommonCodeList.CommonCode.2.CodeShortDescription" ,this.curbsideTimeDelays);
		//OMNI-81479 changes start
		this.popUpForDelaybtn(event,bEvent,ctrl,args,curbsideDelay3);
		//OMNI-81479 changes end 
	},
	extn_EstimatedDelay4_onClick: function(event, bEvent, ctrl, args) {
		var curbsideDelay4 = _scModelUtils.getModelObjectFromPath("CommonCodeList.CommonCode.3.CodeShortDescription" ,this.curbsideTimeDelays);
		//OMNI-81479 changes start
		this.popUpForDelaybtn(event,bEvent,ctrl,args,curbsideDelay4);
		//OMNI-81479 changes end 
	},
    // Method For Opening the Curbside Delay Confirm PopUp.
	delayOnClick: function(event, bEvent, ctrl, args, curbsideDelay) {
		var strCurbsideDelay = curbsideDelay + " mins";
		var maxDelayCounter = _iasContextUtils.getFromContext("CurbsideDelayMaxCounter"); //OMNI-80059 change
		var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");
		var curbsideDelayCount= _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnCurbsideDelayCount",shipmentDetails); //OMNI-80059 change
		_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnCurbsideDelayMins", curbsideDelay, shipmentDetails);//OMNI-81683
		var bindings = null;
			bindings = {};
			bindings["CurbsideDelay"] = strCurbsideDelay;
			bindings["MaxDelayCounter"] = maxDelayCounter; //OMNI-80059
			bindings["DelayCount"] = curbsideDelayCount; //OMNI-80059
			var screenConstructorParams = null;
			screenConstructorParams = {};
			var popupParams = null;
			popupParams = {};
			popupParams["binding"] = bindings;
			popupParams["screenConstructorParams"] = screenConstructorParams;
			var dialogParams = null;
			dialogParams = {};
			dialogParams["closeCallBackHandler"] = "extn_CurbsideDelayConfirmPopUp";
			dialogParams["class"] = "popupTitleBorder fixedActionBarDialog";
      //OMNI-84586-No Further Time Extensions on Order-START
			if(curbsideDelayCount<maxDelayCounter){ 
			_iasUIUtils.openSimplePopup("extn.components.shipment.customerpickup.PopUp.CurbsideDelayConfirmPopUp.CurbsideDelayConfirmPopUp", "", this, popupParams, dialogParams);           
				}
			else{
			var screenConstructorParams = null;
			screenConstructorParams = {};
			var popupParams = null;
			popupParams = {};
			popupParams["binding"] = bindings;
			popupParams["screenConstructorParams"] = screenConstructorParams;
			var dialogParams = null;
			dialogParams = {};
			dialogParams["closeCallBackHandler"] = "extnNoExtensionsAllowedPopUp";
			dialogParams["class"] = "popupTitleBorder fixedActionBarDialog";
			_iasUIUtils.openSimplePopup("extn.components.shipment.customerpickup.PopUp.CurbsideNoExtensionsAllowedPopUp.CurbsideNoExtensionsAllowedPopUp", "", this, popupParams, dialogParams);			
			}
      //OMNI-84586-No Further Time Extensions on Order-END
	},
	//OMNI-81479 changes for deciding Which popup to display - START
	popUpForDelaybtn: function(event, bEvent, ctrl, args, curbsideDelay) {
        var shipmentModel = _scScreenUtils.getModel(this, "ShipmentDetails");
        var loginId = _iasContextUtils.getFromContext("Loginid");
        var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentModel);
        var curbsideAttendedBy = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnCurbsideAttendedBy",shipmentModel); 
        var curbsideAttendedByUserId="";
        if(!_scBaseUtils.isVoid(curbsideAttendedBy)){
	//To trim the userId from the string for comparing
            curbsideAttendedByUserId= (curbsideAttendedBy.match(/\((.*)\)/).pop());
            if (_scBaseUtils.equals(loginId,curbsideAttendedByUserId)){
                this.delayOnClick(event,bEvent,ctrl,args,curbsideDelay);
            }
            else{
                this.extn_alreadyAssignedPopup(event,bEvent,ctrl,args,curbsideAttendedBy);
            }
        }
        else{
                this.delayOnClick(event,bEvent,ctrl,args,curbsideDelay);
            }
    },
    //OMNI-81479 changes - End 
	extn_CurbsideDelayConfirmPopUp: function(event, bEvent, ctrl, args) {
		var datalabelValue = _iasContextUtils.getFromContext("ConfirmDelay");
		var clearSessionObject = _scBaseUtils.getNewModelInstance();
		if ( _scBaseUtils.equals(datalabelValue, "Y")) 
		{ 
			var shipmentModel = _scScreenUtils.getModel(this, "ShipmentDetails");
			var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentModel);			
			var Loginid = _iasContextUtils.getFromContext("Loginid");
			var extninputcount = null;
			var delaycounter = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnCurbsideDelayCount", shipmentModel);
			var prevattentedby = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnCurbsideAttendedBy", shipmentModel);			
			var curbsideDelayMins = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnCurbsideDelayMins", shipmentModel); //OMNI-81683
			var maxcount=_iasContextUtils.getFromContext("CurbsideDelayMaxCounter");
            if (_scBaseUtils.lessThan(parseInt(delaycounter),parseInt(maxcount)))
            {
				var curbsideFinalDeliveryTS = this.calculateCurbsideFinalDeliveryTS(curbsideDelayMins); //OMNI-79883 
				extninputcount = Number(delaycounter)+1;
				var inputToMashup = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnCurbsideDelayCount", extninputcount, inputToMashup);
				if(!prevattentedby)
				{
					_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnCurbsideAttendedBy", Loginid, inputToMashup);
				}	
				if(curbsideDelayMins){ // pushing the Curbside Delay Minutes to DB if 'curbsideDelayMins' value is not zero.
					_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnCurbsideDelayMins", curbsideDelayMins, inputToMashup); //OMNI-81683
        //OMNI-82169 - Setting all the values requied for sending SMS on delay
				var orderNo = _scModelUtils.getStringValueFromPath("Shipment.OrderNo", shipmentModel);
				var shipNode = _scModelUtils.getStringValueFromPath("Shipment.ShipNode", shipmentModel);
				var billToPhone =_scModelUtils.getStringValueFromPath("Shipment.BillToAddress.DayPhone" , shipmentModel);
				var zipCode =_scModelUtils.getStringValueFromPath("Shipment.BillToAddress.ZipCode" , shipmentModel);
				var oLine1 = _scModelUtils.getModelObjectFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentModel);	
				var markForPhone= _scModelUtils.getStringValueFromPath("OrderLine.PersonInfoMarkFor.DayPhone", oLine1[0]);
				var sms1 =  _iasContextUtils.getFromContext("sms1");
				var sms2 = 	_iasContextUtils.getFromContext("sms2");
                var sms3 = 	_iasContextUtils.getFromContext("sms3");
				_scModelUtils.setStringValueAtModelPath("Shipment.OrderNo", orderNo, inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", shipNode, inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Shipment.BillToDayPhone", billToPhone, inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Shipment.BillToZipCode", zipCode, inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Shipment.MarkForDayPhone", markForPhone, inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Shipment.Msg1", sms1, inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Shipment.Msg2", sms2, inputToMashup);
        _scModelUtils.setStringValueAtModelPath("Shipment.Msg3", sms3, inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Shipment.NotifyDelayToCustFlag", "Y", inputToMashup);
				//OMNI-82169		    
				}
				_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnCurbsideExpectedDeliveryTS", curbsideFinalDeliveryTS, inputToMashup); //OMNI-79883
                /* OMNI-81395 - Start */
				var consolidatedShipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ConsolidatedShipmentKey" , shipmentModel);
				if(!_scBaseUtils.isVoid(consolidatedShipmentKey)) {
					_scModelUtils.setStringValueAtModelPath("Shipment.ConsolidatedShipmentKey", consolidatedShipmentKey, inputToMashup);
					_scModelUtils.setStringValueAtModelPath("Shipment.CurbsideConsolidationToggle", "Y", inputToMashup);
				}
                /* OMNI-81395 - End */
				_scModelUtils.setStringValueAtModelPath("Shipment.CurbsideDelayMaxCounter",maxcount, inputToMashup);//OMNI-88938
				_iasUIUtils.callApi(this, inputToMashup, "extn_CurbsideChangeShipmentForAssignee_ref", null);
			}
      //commented the before requirement of diable the time extensions came with new pop up requirement
      /* OMNI-79563 Estimated Curbside Delay Timer Count Changes - START
            if(delaycounter>=maxcount-1){
				_scWidgetUtils.disableWidget(this, "extn_EstimatedDelay1");
				_scWidgetUtils.disableWidget(this, "extn_EstimatedDelay2");
				_scWidgetUtils.disableWidget(this, "extn_EstimatedDelay3");
				_scWidgetUtils.disableWidget(this, "extn_EstimatedDelay4");
			}
             OMNI-79563 Estimated Curbside Delay Timer Count Changes - END*/
            
            _iasContextUtils.addToContext("ConfirmDelay", clearSessionObject);
		}	
	},
	handleAttendedByUpdate: function(mashupOutput) {
        var shipmentModel = _scScreenUtils.getModel(
                this, "ShipmentDetails");
        var assinged = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnCurbsideAttendedBy",mashupOutput);
        var count = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnCurbsideDelayCount",mashupOutput);
         if(assinged)
         {    
            _scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnCurbsideAttendedBy", assinged, shipmentModel);
            _scWidgetUtils.setValue(this, "extn_Attended_by",_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnCurbsideAttendedBy", mashupOutput), null);
            //_scWidgetUtils.showWidget(this, "extn_Attended_by");
         }
        _scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnCurbsideDelayCount", count, shipmentModel);      
    },
	/* OMNI-79898 TM requesting delay without assigning to themselves - No TM associated with curbside -UI Changes - END*/
	/* OMNI-79883 Complete Curbside Order By - START */
	calculateCurbsideFinalDeliveryTS: function(curbsideDelayMins) {
		var curbsideDelayMinutes = parseInt(curbsideDelayMins);
		var shipmentDetails = _scScreenUtils.getModel(this, "ShipmentDetails");
		var strAppointmentNo = _scModelUtils.getStringValueFromPath("Shipment.AppointmentNo",shipmentDetails);
		var intCurbsideDefaultMins = parseInt(_iasContextUtils.getFromContext("CurbsideDefaultMins"));
		var appointmentDate = new Date(strAppointmentNo.replace(/^(\d{4})(\d\d)(\d\d)(\d\d)(\d\d)(\d\d)$/,'$4:$5:$6 $2/$3/$1'));
		var actualCurbsideDeliveryDate = new Date(appointmentDate.getTime() + intCurbsideDefaultMins*60000);
		var currentSystemTS = new Date(); // calculating system current TS.
		var currSysTSInMilli = currentSystemTS.getTime();
		var actualCurbTSInMilli = actualCurbsideDeliveryDate.getTime();
		if (_scBaseUtils.lessThanOrEqual(parseInt(currSysTSInMilli),parseInt(actualCurbTSInMilli))){
			var finalCurbsideDeliveryTS = new Date(actualCurbTSInMilli + curbsideDelayMinutes*60000);
		}else{
			var finalCurbsideDeliveryTS = new Date(currSysTSInMilli + curbsideDelayMinutes*60000);
		}
		var finalCurbsideTime = finalCurbsideDeliveryTS.toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
		//_scWidgetUtils.showWidget(this,"extn_completeCurbsideOrderBy");	
		/*OMNI-88938-UI-Prevent additional time extensions in case of Concurrent users-START*/
		//_scWidgetUtils.setValue(this, "extn_finishCurbsideOrderBy", finalCurbsideTime, false);
		 _iasContextUtils.addToContext("finishCurbsideTimeUpdated",finalCurbsideTime);	
        /*OMNI-89338-UI-Prevent additional time extensions in case of Concurrent users-END*/
		var finalCurbsideTStoDB = finalCurbsideDeliveryTS.toLocaleString();
		return finalCurbsideTStoDB;
	}
	/* OMNI-79883 Complete Curbside Order By - END */
});
});
