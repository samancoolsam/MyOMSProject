scDefine([
    "dojo/text!./templates/TOShipmentSummary.html",
    "scbase/loader!dojo/_base/declare",
    "scbase/loader!sc/plat/dojo/widgets/Screen",
    "scbase/loader!sc/plat/dojo/utils/WidgetUtils",
    "scbase/loader!sc/plat/dojo/utils/ScreenUtils",
    "scbase/loader!sc/plat/dojo/utils/BaseUtils",
    "scbase/loader!ias/utils/UIUtils",
    "scbase/loader!sc/plat/dojo/utils/ModelUtils",
    "scbase/loader!ias/utils/BaseTemplateUtils",
    "scbase/loader!wsc/mobile/home/utils/MobileHomeUtils",
    "scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils",
    "scbase/loader!ias/utils/ContextUtils",
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
    _wscShipmentUtils,
    _iasContextUtils

) {
    return _dojodeclare("extn.mobile.home.STSContainer.transferOrderShipmentSummary.TOShipmentSummary", [_scScreen], {
        templateString: templateText,
        uId: "TOShipmentSummary",
        packageName: "extn.mobile.home.STSContainer.transferOrderShipmentSummary",
        className: "TOShipmentSummary",
        title: "Title_TO_Shipment_Summary",
        screen_description: "TO Shipment Summary",
        namespaces: {
            sourceBindingNamespaces: [
				{
                    description: 'The output to the getShipmentDetails mashup.',
                    value: 'getShipmentDetails_output'
                },
				{
                    description: 'The output to the getSOShipmentDetails mashup.',
                    value: 'mvalidShipment_output'
                },
    // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - START
				       {
                    description: 'This namespace has Cancelled items List',
                    value: 'canceledItemsList'
                }
    // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - END
            ]
        },

        subscribers: {
            local: [{
                    eventId: 'extn_btnBack_onClick',
                    sequence: '32',
                    handler: {
                        methodName: "backOnClick"
                    }
                },
                {
                    eventId: 'extn_btnClose1_onClick',
                    sequence: '32',
                    handler: {
                        methodName: "closeOnClick"
                    }
                },
                {
                    eventId: 'extn_btnClose_onClick',
                    sequence: '32',
                    handler: {
                        methodName: "closeOnClick"
                    }
                },
                //OMNI-72012
                {
                 eventId: 'extn_CompleteOnMyWaybutton_onClick',
                    sequence: '32',
                    handler: {
                        methodName: "extnOnCompleteClick"
                    }
                },
                //OMNI-72012
                {
                    eventId: 'afterScreenInit',
                    sequence: '32',
                    handler: {
                        methodName: "initializeScreen"
                    }
                },
         // OMNI-9303 Unable to reprint the pickup acknowledgment slip for STS orders - Start
	         	{
                    eventId: 'extn_btnPrintAck_onClick',
                    sequence: '32',
                    handler: {
                        methodName: "extn_reprintAcknowledgement"
                    }
                },
         // OMNI-9303 Unable to reprint the pickup acknowledgment slip for STS orders - End

            ]
        },


        initializeScreen: function(event, bEvent, ctrl, args) {
			var inputModel = _scScreenUtils.getInitialInputData(this);
			//_scScreenUtils.setModel(this, "inputModel", inputModel, null);
			//OMNI -6624 - START
			var invokedFrom = _scModelUtils.getStringValueFromPath("InvokedFrom", inputModel);
		// OMNI-56184 - Change HIP Printer for STS - START 
			var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", inputModel);
			var sessionObject = _scBaseUtils.getNewModelInstance();
			_scModelUtils.setStringValueAtModelPath("inputModel",inputModel,sessionObject);
			window.sessionStorage.setItem("ShipmentSessionObject",JSON.stringify(sessionObject));
		// OMNI-56184 - Change HIP Printer for STS - END 
      // OMNI - 9303 - START
			var status = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", inputModel);

			if(_scBaseUtils.equals(status,"1600.002") || _scBaseUtils.equals(status,"1400"))
			{
				_scWidgetUtils.showWidget(this, "extn_btnPrintAck", false);
			}
       // OMNI - 9303 - END

			if(_scBaseUtils.equals(invokedFrom, "SearchResults")){
				var shipmentModel = _scBaseUtils.getNewModelInstance();
				var strOrderHeaderKey = _scModelUtils.getStringValueFromPath("Shipment.OrderHeaderKey", inputModel);
				_scModelUtils.setStringValueAtModelPath("Shipment.OrderHeaderKey", strOrderHeaderKey, shipmentModel);
				_iasUIUtils.callApi(this, shipmentModel, "extn_getTOShipmentDetails", null);
			}else{
				_iasUIUtils.callApi(this, inputModel, "extn_getTOShipmentDetails", null);
			}
      
      //OMNI- 6624 - END
        },

        backOnClick: function(event, bEvent, ctrl, args) {
            _wscMobileHomeUtils.openScreen("extn.mobile.home.STSContainer.container.ReceiveContainer", "extn.mobile.editors.ReceiveContainerEditor");
        },

        closeOnClick: function(event, bEvent, ctrl, args) {
            var inputModel = _scScreenUtils.getInitialInputData(this);
            var sInvokedFrom = _scModelUtils.getStringValueFromPath("InvokedFrom", inputModel);
            var searchresultModel = _iasContextUtils.getFromContext("SearchCriteria");
			//Start OMNI-75388, OMNI-75389
			var sOnMyWayScreen= _scModelUtils.getStringValueFromPath("Shipment.IsOnMyWayScreen", inputModel);
            var sCurbsidePickupScreen= _scModelUtils.getStringValueFromPath("Shipment.IsCurbsidePickupScreen", inputModel);
            var OMWSearchCriteriaModel = _iasContextUtils.getFromContext("OMWSearchCriteria");
            var CurbsideSearchCriteriaModel = _iasContextUtils.getFromContext("CurbsidePickupSearchCriteria");
			//End OMNI-75388, OMNI-75389
            if(_scBaseUtils.equals(sInvokedFrom, "ReportScreen")) {
                _wscMobileHomeUtils.openScreen("extn.mobile.home.STSContainer.detailedReceivedReport.ReceivedDetailsReportScreen", "extn.mobile.editors.ReceiveContainerEditor");
            } else if(_scBaseUtils.equals(sInvokedFrom, "ReceiveContainer")) {
                _wscMobileHomeUtils.openScreen("extn.mobile.home.STSContainer.container.ReceiveContainer", "extn.mobile.editors.ReceiveContainerEditor");
            } else if(_scBaseUtils.equals(sInvokedFrom, "StageContainer")) {
                _wscMobileHomeUtils.openScreen("extn.mobile.home.STSStaging.backroompick.ReadyToStage", "extn.mobile.editors.ReceiveContainerEditor");
            }else if(_scBaseUtils.equals(sInvokedFrom, "SearchResults") && (_scBaseUtils.equals(sOnMyWayScreen, "Y"))) {
			_wscMobileHomeUtils.openScreenWithInputData("wsc.mobile.home.search.SearchResult", OMWSearchCriteriaModel, "wsc.mobile.editors.MobileEditor");
			}else if(_scBaseUtils.equals(sInvokedFrom, "SearchResults") && (_scBaseUtils.equals(sCurbsidePickupScreen, "Y"))) {
              _wscMobileHomeUtils.openScreenWithInputData("wsc.mobile.home.search.SearchResult", CurbsideSearchCriteriaModel, "wsc.mobile.editors.MobileEditor");
            }
            //*START OMNI-102418
            else if(_scBaseUtils.equals(sInvokedFrom, "SearchResults") && !_scBaseUtils.isVoid(searchresultModel)) {
              _wscMobileHomeUtils.openScreenWithInputData("wsc.mobile.home.search.SearchResult", searchresultModel, "wsc.mobile.editors.MobileEditor");
            }
            else if(_scBaseUtils.equals(sInvokedFrom, "SearchResults")) {
              _wscMobileHomeUtils.openScreen("wsc.mobile.home.MobileHome", "wsc.mobile.editors.MobileEditor");
            }
            //*END OMNI-102418
           },

        handleMashupOutput: function(mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
        
        //Start OMNI-72012
			if (
            _scBaseUtils.equals(
            mashupRefId, "extn_CompleteOnMyWayOrder")) {
                _scWidgetUtils.hideWidget(
                this, "extn_CompleteOMYPane", false);
            }
			//End OMNI-72012
			//OMNI-98331 - Start
			if (_scBaseUtils.equals(mashupRefId, "extn_readyToStage_changeSOShipmentStatus_ref")) {
					var shipmentDetailsModel = null;
		            shipmentDetailsModel = _scScreenUtils.getModel(this, "getShipmentDetails_output");
			       //var orderHeaderKey=  _scModelUtils.getStringValueFromPath("Shipment.OrderHeaderKey",shipmentDetailsModel);
					var inputModel = _scScreenUtils.getInitialInputData(this);
					var status = modelOutput.Shipment.Status;
					if(_scBaseUtils.equals(status,"1100.70.06.30.5")){
					//_scWidgetUtils.hideWidget(this, "extn_btnCompleteAssembly", false);
					_scModelUtils.setStringValueAtModelPath("Shipment.Status.Status", status, inputModel);
					_scModelUtils.setStringValueAtModelPath("Shipment.Status.Description", "Ready For Customer Pick Up", inputModel);
					}
                    //var data = _scBaseUtils.getNewModelInstance();
					//_scModelUtils.setStringValueAtModelPath("Shipment.OrderHeaderKey", orderHeaderKey ,data);
					_wscMobileHomeUtils.openScreenWithInputData("extn.mobile.home.STSContainer.transferOrderShipmentSummary.TOShipmentSummary", inputModel, "extn.mobile.editors.ReceiveContainerEditor");
			}
			//OMNI-98331 - End
        },
		
        handleMashupCompletion: function(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
            _iasBaseTemplateUtils.handleMashupCompletion(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);

            for (var i in mashupRefList) {
                var mashupRefId = _scModelUtils.getStringValueFromPath("mashupRefId", mashupRefList[i]);
                if (_scBaseUtils.equals(mashupRefId, "extn_getTOShipmentDetails")) {
					var inputModel = _scScreenUtils.getInitialInputData(this);
					var sInvokedFrom = _scModelUtils.getStringValueFromPath("InvokedFrom", inputModel);	
                    
					if(_scBaseUtils.equals(sInvokedFrom,"StageContainer"))
					{
					/*	var mvalidShipment = JSON.parse(window.sessionStorage.getItem("mvalidShipment"));
						if(!_scBaseUtils.isVoid(mvalidShipment)){
							//OMNI-6589: Start
							var shipmentStatus = _scModelUtils.getStringValueFromPath("Status.Status", mvalidShipment);
							if(_scBaseUtils.contains(shipmentStatus, "1100.70.06.10")) {
								_scModelUtils.setStringValueAtModelPath("Status.Description", "Ready To Stage", mvalidShipment); 
							}else if(_scBaseUtils.contains(shipmentStatus, "1100.70.06.20")) {
								_scModelUtils.setStringValueAtModelPath("Status.Description", "Staging in Progress", mvalidShipment); 
							}
							//OMNI-6589: End
							_scScreenUtils.setModel(this, "mvalidShipment_output", mvalidShipment, null);					
							_scWidgetUtils.showWidget(this, "lblTOShipmentNo", false);
							_scWidgetUtils.showWidget(this, "lblShipmentStatus", false);  
						} */
					}
					//OMNI - 6624 - START
					/*else if(_scBaseUtils.equals(sInvokedFrom,"SearchResults"))
					{
					  var inputModel = _scScreenUtils.getInitialInputData(this);
					   var sShipmentNo = _scModelUtils.getStringValueFromPath("Shipment.ShipmentNo", inputModel);
					  if(!_scBaseUtils.isVoid(sShipmentNo)){
							
							var shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", inputModel);
							var shipmentDesc = _scModelUtils.getStringValueFromPath("Shipment.Status.Description", inputModel);
							if(_scBaseUtils.equals(shipmentStatus, "1100.70.06.10")) {
								_scWidgetUtils.setValue(this, "lblShipmentStatus", "Ready To Stage", false);
							}else if(_scBaseUtils.equals(shipmentStatus, "1100.70.06.20")) {
								_scWidgetUtils.setValue(this, "lblShipmentStatus", "Staging in Progress", false); 
							}else if(_scBaseUtils.equals(shipmentStatus, "1400") || _scBaseUtils.equals(shipmentStatus, "1600.002"))  {
							       	_scWidgetUtils.setValue(this, "lblShipmentStatus", "Picked Up", false);
							}
							else {
									_scWidgetUtils.setValue(this, "lblShipmentStatus", shipmentDesc, false);
							}
							//var shipmentNo  = _scModelUtils.getStringValueFromPath("Shipment.ShipmentNo", inputModel);
							_scWidgetUtils.setValue(this, "lblTOShipmentNo", sShipmentNo, false);

							_scWidgetUtils.showWidget(this, "lblTOShipmentNo", false);
							_scWidgetUtils.showWidget(this, "lblShipmentStatus", false);
						}
						else
						{
  					    _scWidgetUtils.hideWidget(this,"lblTOShipmentNo",false);
					    _scWidgetUtils.hideWidget(this,"lblShipmentStatus",false);
						}

					} */				
					// OMNI -6624 - END					
					var shipmentList = _scModelUtils.getStringValueFromPath("Shipments.Shipment", mashupRefList[i].mashupRefOutput);
					var mashupRefOutput =  _scModelUtils.getStringValueFromPath("mashupRefOutput", mashupRefList[i]);
					var maxorderStatus= _scModelUtils.getStringValueFromPath("0.ShipmentLines.ShipmentLine.0.OrderLine.ChainedFromOrderLine.Order.MaxOrderStatus",shipmentList);
					var minorderStatus= _scModelUtils.getStringValueFromPath("0.ShipmentLines.ShipmentLine.0.OrderLine.ChainedFromOrderLine.Order.MinOrderStatus",shipmentList);
					var maxorderStatusNumber= _scModelUtils.getNumberValueFromPath("0.ShipmentLines.ShipmentLine.0.OrderLine.ChainedFromOrderLine.Order.MinOrderStatus",shipmentList);
				        // OMNI - 6624 - START
					var salesShipmentNumber = _scModelUtils.getStringValueFromPath("Shipments.Shipment.0.SOShipmentNo", mashupRefList[i].mashupRefOutput);
					var salesShipmentStatus = _scModelUtils.getStringValueFromPath("Shipments.Shipment.0.SOShipmentStatus", mashupRefList[i].mashupRefOutput);
					var salesShipmentstatusdesc = _scModelUtils.getStringValueFromPath("Shipments.Shipment.0.SOShipmentStatusDesc", mashupRefList[i].mashupRefOutput);
					//Start OMNI-75388, OMNI-75389
					var salesExtnIsOMYOpted = _scModelUtils.getStringValueFromPath("Shipments.Shipment.0.SOExtnIsOnMyWayOpted", mashupRefList[i].mashupRefOutput);
					var salesExtnIsCurbsidePickupOpted = _scModelUtils.getStringValueFromPath("Shipments.Shipment.0.SOExtnIsCurbsidePickupOpted", mashupRefList[i].mashupRefOutput);
					var sOnMyWayScreen= _scModelUtils.getStringValueFromPath("Shipment.IsOnMyWayScreen", inputModel);
                    var sCurbsidePickupScreen= _scModelUtils.getStringValueFromPath("Shipment.IsCurbsidePickupScreen",inputModel);
                    if(_scBaseUtils.equals(salesExtnIsOMYOpted, "Y") && (_scBaseUtils.equals(sOnMyWayScreen, "Y"))){
                    var shipmentSearchCriteriaModel="";
                    var sCurrentStore = _iasContextUtils.getFromContext("CurrentStore");
                    shipmentSearchCriteriaModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
                    _scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", sCurrentStore,shipmentSearchCriteriaModel);
                    _scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod", 'PICK',shipmentSearchCriteriaModel);
                    _scModelUtils.setStringValueAtModelPath("Shipment.Status",'1100.70.06.30.5',shipmentSearchCriteriaModel);
                    _scModelUtils.setStringValueAtModelPath("Shipment.OrderByAttribute",'AppointmentNo',shipmentSearchCriteriaModel);
                    _scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnOnMyWayOpted",'Y',shipmentSearchCriteriaModel);
                    _scModelUtils.setStringValueAtModelPath("Shipment.IsOnMyWayOrder", 'Y',shipmentSearchCriteriaModel);
                    _iasContextUtils.addToContext("OMWSearchCriteria", shipmentSearchCriteriaModel);
                    }else if(_scBaseUtils.equals(salesExtnIsCurbsidePickupOpted, "Y") && (_scBaseUtils.equals(sCurbsidePickupScreen, "Y"))){
                    var shipmentSearchCriteriaModel="";
                    var sCurrentStore = _iasContextUtils.getFromContext("CurrentStore");
                    shipmentSearchCriteriaModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
                    _scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", sCurrentStore,shipmentSearchCriteriaModel);
                   _scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod", 'PICK',shipmentSearchCriteriaModel);
                    _scModelUtils.setStringValueAtModelPath("Shipment.Status",'1100.70.06.30.5',shipmentSearchCriteriaModel);
                    _scModelUtils.setStringValueAtModelPath("Shipment.OrderByAttribute",'AppointmentNo',shipmentSearchCriteriaModel);
                    _scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",'Y',shipmentSearchCriteriaModel);
					_scModelUtils.setStringValueAtModelPath("Shipment.IsCurbsidePickupOrder", 'Y',shipmentSearchCriteriaModel);
                    _iasContextUtils.addToContext("CurbsidePickupSearchCriteria", shipmentSearchCriteriaModel);
                    }
					//End OMNI-75388, OMNI-75389
                    //OMNI-72012 Start
                    var onMyWaySessionModel = _iasContextUtils.getFromContext("OnMyWayFeatureToggle");
                        if(!_scBaseUtils.equals(onMyWaySessionModel, "Y")){
                             _scWidgetUtils.hideWidget(this, "extn_CompleteOMYPane", false);
                        }else if(!_scBaseUtils.equals(salesExtnIsOMYOpted, "Y")){
                            _scWidgetUtils.hideWidget(this, "extn_CompleteOMYPane", false);
                        }
                    //OMNI-72012 End
					 if((!_scBaseUtils.isVoid(salesShipmentNumber)) && (!_scBaseUtils.isVoid(salesShipmentStatus)) && (!_scBaseUtils.isVoid(salesShipmentstatusdesc))){
							
							//var shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", inputModel);
							//var shipmentDesc = _scModelUtils.getStringValueFromPath("Shipment.Status.Description", inputModel);
							if(_scBaseUtils.equals(salesShipmentStatus, "1100.70.06.10")) {
								_scWidgetUtils.setValue(this, "lblShipmentStatus", "Ready To Stage", false);
							}else if(_scBaseUtils.equals(salesShipmentStatus, "1100.70.06.20")) {
								_scWidgetUtils.setValue(this, "lblShipmentStatus", "Staging in Progress", false); 
							}else if(_scBaseUtils.equals(salesShipmentStatus, "1400") || _scBaseUtils.equals(salesShipmentStatus, "1600.002"))  {
							       	_scWidgetUtils.setValue(this, "lblShipmentStatus", "Picked Up", false);
							}
							else {
									_scWidgetUtils.setValue(this, "lblShipmentStatus", salesShipmentstatusdesc, false);
							}
							//var shipmentNo  = _scModelUtils.getStringValueFromPath("Shipment.ShipmentNo", inputModel);
							_scWidgetUtils.setValue(this, "lblTOShipmentNo", salesShipmentNumber, false);

							_scWidgetUtils.showWidget(this, "lblTOShipmentNo", false);
							_scWidgetUtils.showWidget(this, "lblShipmentStatus", false);
						}
						else
						{
  					    _scWidgetUtils.hideWidget(this,"lblTOShipmentNo",false);
					    _scWidgetUtils.hideWidget(this,"lblShipmentStatus",false);
						}
					// OMNI - 6624 - END

					if(!(_scBaseUtils.equals(maxorderStatus,"9000") && _scBaseUtils.equals(minorderStatus,"9000")))
					{
						_scWidgetUtils.hideWidget(this,"extn_lblOrderStatus",false);                   	
					}
    // OMNI - 9303 - START
					if(_scBaseUtils.equals(salesShipmentStatus,"1600.002") || _scBaseUtils.equals(salesShipmentStatus,"1400"))
					{
				     _scWidgetUtils.showWidget(this, "extn_btnPrintAck", false);
			    }
     // OMNI - 9303 - END
			
					/*if(maxorderStatusNumber>= 3350  && maxorderStatusNumber!=9000)
					{
						_scWidgetUtils.showWidget(this, "lblTOShipmentNo", false);
						_scWidgetUtils.showWidget(this, "lblShipmentStatus", false);
					} */                   
					var shipmetncount = shipmentList.length;
          // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - START
					var cancelledLinesArray =  _scBaseUtils.getNewArrayInstance();
          // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - END
                    if (shipmetncount == 1) {
                        var ShipmentModel = _scBaseUtils.getNewModelInstance();
                        shipmentList = _scModelUtils.getStringValueFromPath("Shipments.Shipment.0", mashupRefList[i].mashupRefOutput);
                        var container = _scModelUtils.getStringValueFromPath("Containers.Container", shipmentList);
                        // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - START
                        var cancellationCode = _scModelUtils.getStringValueFromPath("Extn.ExtnShortpickReasonCode", shipmentList);
		                  	var shipmentStatus = _scModelUtils.getStringValueFromPath("Status.Status", shipmentList);
                        // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - END
                        if (!_scBaseUtils.isVoid(container)) {
                            _scWidgetUtils.hideWidget(this, "noContainersPnl", false);
                            _scWidgetUtils.showWidget(this, "extn_lblReceivingStatus", true, "");
                            for(var c in container)
                            {
                              var zone = _scModelUtils.getStringValueFromPath("Zone", container[c]);
                              if(_scBaseUtils.equals(zone,"LOST"))
                               {
                                   cancelledLinesArray = this.extn_prepareCancelledLineList(container[c], cancelledLinesArray);
                                }	
                            }
                            
                        } else {
                            _scWidgetUtils.hideWidget(this, "extn_lblReceivingStatus", false);
                            _scWidgetUtils.showWidget(this, "noContainersPnl", true, "");
                           // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - START
                            cancelledLinesArray = this.extn_prepareCancelledLineList(shipmentList, cancelledLinesArray);
                          // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - END
                        }
	
                       // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - START
                        if(!_scBaseUtils.isVoid(cancellationCode))
                        {
                        	cancelledLinesArray = this.extn_prepareCancelledLineList(shipmentList, cancelledLinesArray);
                        }
			                 else if(!_scBaseUtils.equals(shipmentStatus, "9000"))
                        {
                            	var shipmentLines = _scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine",shipmentList);
                            	for(var j in shipmentLines)
                            	{
                            	   var slOriginalQty =_scModelUtils.getNumberValueFromPath("OriginalQuantity", shipmentLines[j]);
                            	   var slQuantity = _scModelUtils.getNumberValueFromPath("Quantity", shipmentLines[j]);
                            	    if(slOriginalQty != slQuantity)
                            	     {
                            		cancellationCode = "Partail NI Shortage";
                            		_scModelUtils.setStringValueAtModelPath("Extn.ExtnShortpickReasonCode", cancellationCode,shipmentList);
                            	        cancelledLinesArray = this.extn_prepareCancelledLineList(shipmentList, cancelledLinesArray);
                            	     }
                            	}
                            }

			
                       // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - END
                        _scModelUtils.setStringValueAtModelPath("Shipment", shipmentList, ShipmentModel);
                        _scScreenUtils.setModel(this, "getShipmentDetails_output", ShipmentModel, null);
                        var newModel = _scScreenUtils.getModel(this, "getShipmentDetails_output");
                    } else if (shipmetncount > 1) {
						
                        var hascontainers = false;
			var containersArray = _scBaseUtils.getNewArrayInstance();
			var emptyShipmentCount = 0;
			var hasCancelledShipments=0;
			 var cancellationCode = "";
			
					//var containerModel = _scBaseUtils.getNewModelInstance();
						/*
						var inputModel = _scScreenUtils.getModel(this,"inputModel");
						var sInvokedFrom = _scModelUtils.getStringValueFromPath("InvokedFrom", inputModel);
						if (_scBaseUtils.equals(sInvokedFrom, "StageContainer")) {
							_scModelUtils.setStringValueAtModelPath("InvokedFrom",sInvokedFrom,containerModel);
						}
						*/			
                        for (var i in shipmentList) {
                            var containers = _scModelUtils.getStringValueFromPath("Containers.Container", shipmentList[i]);
                            var shipmentStatus = _scModelUtils.getStringValueFromPath("Status.Status", shipmentList[i]);
                            // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - START
                            var extnShortPickReason = _scModelUtils.getStringValueFromPath("Extn.ExtnShortpickReasonCode", shipmentList[i]);
                            if(!_scBaseUtils.isVoid(extnShortPickReason))
                            {
                            	cancellationCode = extnShortPickReason;
                            	cancelledLinesArray = this.extn_prepareCancelledLineList(shipmentList[i], cancelledLinesArray);
                            }
                            else if(!_scBaseUtils.equals(shipmentStatus, "9000"))
                            {
                            	var shipmentLines = _scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine",shipmentList[i]);
                            	for(var j in shipmentLines)
                            	{
                            	   var slOriginalQty =_scModelUtils.getNumberValueFromPath("OriginalQuantity", shipmentLines[j]);
                            	   var slQuantity = _scModelUtils.getNumberValueFromPath("Quantity", shipmentLines[j]);
                            	    if(slOriginalQty != slQuantity)
                            	     {
                            		cancellationCode = "Partail NI Shortage";
                            		_scModelUtils.setStringValueAtModelPath("Extn.ExtnShortpickReasonCode", cancellationCode,shipmentList[i]);
                            	        cancelledLinesArray = this.extn_prepareCancelledLineList(shipmentList[i], cancelledLinesArray);
                            	     }
                            	}
                            }
                           // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - END
                            if (_scBaseUtils.isVoid(containers))  {
                              /*   var containerModel = _scBaseUtils.getNewModelInstance();
                                _scModelUtils.setStringValueAtModelPath("ContainerNo", "Not Shipped", containerModel);
                                _scModelUtils.setStringValueAtModelPath("IsReceived", "N", containerModel);
                                _scBaseUtils.appendToArray(containersArray, containerModel); */
                               // OMNI -9190  STS Order Search - Container Status - START
	                            if(_scBaseUtils.equals(shipmentStatus,"9000")){

	                            	hasCancelledShipments++;
                                // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - START
	                            	cancelledLinesArray = this.extn_prepareCancelledLineList(shipmentList[i], cancelledLinesArray);
                                 // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - END

	                            }
	                            else
	                            {
	                            	 emptyShipmentCount++;
	                            }
                               // OMNI -9190  STS Order Search - Container Status - END
				                       
                                if (!hascontainers) {
                                    _scWidgetUtils.hideWidget(this, "extn_lblReceivingStatus", false);
                                    _scWidgetUtils.showWidget(this, "noContainersPnl", true, "");
                                }
                            }else{
                                for (var j in containers) {									
                                    var containerModel = _scBaseUtils.getNewModelInstance();
                                    containerModel = _scModelUtils.getStringValueFromPath("", containers[j]);
                                    _scBaseUtils.appendToArray(containersArray, containerModel);
                                    hascontainers = true;
                                    var zone = _scModelUtils.getStringValueFromPath("Zone", containerModel);
                                    if(_scBaseUtils.equals(zone,"LOST"))
                                    {
                                      cancelledLinesArray = this.extn_prepareCancelledLineList(containerModel, cancelledLinesArray);
                                    }
                                }
                                _scWidgetUtils.hideWidget(this, "noContainersPnl", false);
                                _scWidgetUtils.showWidget(this, "extn_lblReceivingStatus", true, "");
                            }
                         }
			if(hascontainers){

		        for(var i= 1; i<=emptyShipmentCount; i++)
                        {
                        	 var containerModel = _scBaseUtils.getNewModelInstance();
                                _scModelUtils.setStringValueAtModelPath("ContainerNo", "Not Shipped", containerModel);
                                _scModelUtils.setStringValueAtModelPath("IsReceived", "N", containerModel);
                                _scBaseUtils.appendToArray(containersArray, containerModel);
                        }
                  // OMNI -9190  STS Order Search - Container Status - START
                 for(var i= 1; i<=hasCancelledShipments; i++)
                        {
                        	 var containerModel = _scBaseUtils.getNewModelInstance();
                                _scModelUtils.setStringValueAtModelPath("ContainerNo", "Cancelled", containerModel);
                                _scModelUtils.setStringValueAtModelPath("IsReceived", "N", containerModel);
                                _scBaseUtils.appendToArray(containersArray, containerModel);
                        }
                  // OMNI -9190  STS Order Search - Container Status - END
			 }

                        var shipmentModel = _scModelUtils.getStringValueFromPath("0", shipmentList);
                        var updatedShipmentModel = _scBaseUtils.getNewModelInstance();
                        _scModelUtils.setStringValueAtModelPath("Shipment", shipmentModel, updatedShipmentModel);
                        _scModelUtils.setStringValueAtModelPath("Shipment.Containers.Container", containersArray, updatedShipmentModel);
                        _scScreenUtils.setModel(this, "getShipmentDetails_output", updatedShipmentModel, null);                  
                    }
					var shipmentModel = _scScreenUtils.getModel(this,"getShipmentDetails_output");
					var orderdate = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.OrderLine.ChainedFromOrderLine.Order.OrderDate",shipmentModel);
					if(!_scBaseUtils.isVoid(orderdate))
					{
						 var tmp = dojo.date.stamp.fromISOString(orderdate,{selector: 'date'});
						 orderdate = dojo.date.locale.format(tmp, {selector:'date',fullYear:'true'});
						 _scWidgetUtils.setValue(this, "extn_lblOrderDate", orderdate, false);
					}
         
         // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - START
          var hasSOShipmentLines = _scModelUtils.getStringValueFromPath("Shipment.SalesOrderShipmentLines",shipmentModel);
          if(!_scBaseUtils.isVoid(hasSOShipmentLines))
          {
          	var SOShipmentLines = _scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine",hasSOShipmentLines);
          	for(var s in SOShipmentLines){
          		var shortageQty = _scModelUtils.getNumberValueFromPath("ShortageQty", SOShipmentLines[s]);
          		if(shortageQty > 0)
          		{
          			var orderLineKey= _scModelUtils.getStringValueFromPath("OrderLine.OrderLineKey", SOShipmentLines[s]);
          			var shipments = _scModelUtils.getStringValueFromPath("Shipments.Shipment", mashupRefOutput);
          			for(var l in shipments)
          			{
          				var shipment = _scModelUtils.getStringValueFromPath("", shipments[l]);
          				var toShipmentlines = _scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine", shipment);
          				for(var t in toShipmentlines)
          				{
          					var soOrderLineKey = _scModelUtils.getStringValueFromPath("OrderLine.ChainedFromOrderLine.OrderLineKey", toShipmentlines[t]);
          					if(_scBaseUtils.equals(soOrderLineKey, orderLineKey))
          					{
          						var ShipmentLineModel = _scModelUtils.getStringValueFromPath("", toShipmentlines[t]);
          						var toshipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", ShipmentLineModel);
          						var toOrderlineKey = _scModelUtils.getStringValueFromPath("OrderLine.OrderLineKey", ShipmentLineModel);
          						_scModelUtils.setStringValueAtModelPath("ShortageQty", toShipmentlines[t]);
          						if(!_scBaseUtils.isVoid(cancelledLinesArray))
 			                      {
 				                       for(var i in cancelledLinesArray)
 				                        {
 				                        	var cshipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", cancelledLinesArray[i]);
 					                        var cOrderLineKey = _scModelUtils.getStringValueFromPath("OrderLine.OrderLineKey", cancelledLinesArray[i]);
 					                        if((cshipmentLineKey == toshipmentLineKey) && (cOrderLineKey == toOrderlineKey))
 					                        {
 					                        	var extnShortPickReason = _scModelUtils.getStringValueFromPath("ExtnShortPickedReasonCode", cancelledLinesArray[i]);
 					                        	if(!_scBaseUtils.isVoid(extnShortPickReason)){
 					                        		_scModelUtils.setStringValueAtModelPath("ExtnShortPickedReasonCode",extnShortPickReason, ShipmentLineModel);
 					                        	}
 						                        _scBaseUtils.removeItemFromArray(cancelledLinesArray, cancelledLinesArray[i]);
 					                        }
 					                      
 				                        }
 			                      }
 			                         	 _scBaseUtils.appendToArray(cancelledLinesArray,ShipmentLineModel);
          					}
          				}
          			}
          		}

          	}
          }
          if(!_scBaseUtils.isVoid(cancelledLinesArray))
             {
					         var cancelledItemListModel = _scBaseUtils.getNewModelInstance();
                    _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine", cancelledLinesArray, cancelledItemListModel);
                    _scScreenUtils.setModel(this, "canceledItemsList", cancelledItemListModel, null);

				          	if(! (_scBaseUtils.equals(maxorderStatus,"9000") && _scBaseUtils.equals(minorderStatus,"9000")))
		          			{
						          _scWidgetUtils.showWidget(this,"extn_tpCancellationDetails",false);
				          	}
             }
          // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - END
		   //OMNI-98331 - Start
		   var isListScreen= _iasContextUtils.getFromContext("isRedirectFromListScreen");
			if(!_scBaseUtils.isVoid(isListScreen)&& _scBaseUtils.equals(isListScreen, "Y") &&
				_scBaseUtils.equals(salesShipmentStatus, "1100.70.06.10.5")){
				_iasContextUtils.addToContext("isRedirectFromListScreen", "N");   
				this.extnChangeShipmentStatus();
			}
			//OMNI-98331 - End
            }
            }

        },
		
        getFormattedNameDisplay: function(dataValue, screen, widget, namespace, modelObj, options) {
            var formattedName = "";
            var vFirstName = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.OrderLine.ChainedFromOrderLine.Order.PersonInfoShipTo.FirstName", modelObj);
            var vLastName = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.OrderLine.ChainedFromOrderLine.Order.PersonInfoShipTo.LastName", modelObj);
            var nameModel = {};
            _scModelUtils.setStringValueAtModelPath("FirstName", vFirstName, nameModel);
            _scModelUtils.setStringValueAtModelPath("LastName", vLastName, nameModel);
            formattedName = _wscShipmentUtils.getNameDisplay(
                this, nameModel);
            return formattedName;
        },

        getEmailID: function(dataValue, screen, widget, namespace, modelObj, options) {
            var vEmailId = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.OrderLine.ChainedFromOrderLine.Order.PersonInfoShipTo.EMailID", modelObj);
            return vEmailId;

        },
		
         //OMNI-72012 Start
	extnOnCompleteClick:function(dataValue,screen,widget,namespace,modelObj,options){
        var shipmentDetailModel = _scScreenUtils.getModel(this, "getShipmentDetails_output");
		 var sSOShipmentKey=_scModelUtils.getStringValueFromPath("Shipment.SOShipmentKey",shipmentDetailModel);
			var changeShipmentInputModel=_scBaseUtils.getNewModelInstance();
			_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey",sSOShipmentKey,changeShipmentInputModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnOnMyWayOpted",'A',changeShipmentInputModel);
			_iasUIUtils.callApi(this,changeShipmentInputModel,"extn_CompleteOnMyWayOrder",null);
		},
		//OMNI-72012 End
        //OMNI-98331 - Start
		extnChangeShipmentStatus: function(event, bEvent, ctrl, args) {
			 var shipmentDetailsModel = null;
				shipmentDetailsModel = _scScreenUtils.getModel(this, "getShipmentDetails_output");
				var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.SOShipmentKey",shipmentDetailsModel);
				var changeShpStatus = _scBaseUtils.getNewModelInstance();
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey ,changeShpStatus);
				_scModelUtils.setStringValueAtModelPath("Shipment.BaseDropStatus", "1100.70.06.30.5" ,changeShpStatus);
				_scModelUtils.setStringValueAtModelPath("Shipment.TransactionId", "YCD_BACKROOM_PICK" ,changeShpStatus);
				_iasUIUtils.callApi(this, changeShpStatus, "extn_readyToStage_changeSOShipmentStatus_ref", null);
		},
		//OMNI-98331 - End
		
        getDayPhone: function(dataValue, screen, widget, namespace, modelObj, options) {
            var vDayPhone = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.OrderLine.ChainedFromOrderLine.Order.PersonInfoShipTo.DayPhone", modelObj);
            return vDayPhone;
        },

        getReceivingStatus: function(dataValue, screen, widget, namespace, modelObj, options) {
            var formatedValue = "";
            var sMaxOrderStatus = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.OrderLine.Order.0.MaxOrderStatusDesc", modelObj);
	          var sMinOrderStatus = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.OrderLine.Order.0.MinOrderStatusDesc", modelObj);
            var iReceivedCount = 0;
            var iNotReceivedCount = 0;
        /*    for (var i in containersModel) {
                var isReceivedFlag = _scModelUtils.getStringValueFromPath("IsReceived", containersModel[i]);
                if (_scBaseUtils.equals(isReceivedFlag, "N")) {
                    iNotReceivedCount++;
                } else if (_scBaseUtils.equals(isReceivedFlag, "Y")) {
                    iReceivedCount++;
                }
            }

            if (iNotReceivedCount > 0 && iReceivedCount > 0) {
                formattedName = "Partially Received";
            } else if (iNotReceivedCount == 0 && iReceivedCount > 0) {
                formattedName = "Received";
            } else if (iNotReceivedCount > 0 && iReceivedCount == 0 && (!_scBaseUtils.isVoid(containersModel))) {
                formattedName = "Shipped";
            } else if (iNotReceivedCount == 0 && iReceivedCount == 0 && (_scBaseUtils.isVoid(containersModel))) {
                formattedName = "Not Packed";
            } */
           
            if(_scBaseUtils.equals(sMaxOrderStatus, "Receipt Closed"))
	          {
                sMaxOrderStatus ="Received In Store";
	          }
	         if(_scBaseUtils.equals(sMinOrderStatus, "Receipt Closed"))
	         {
	         	sMinOrderStatus ="Received In Store";
	         }

            if(_scBaseUtils.equals(sMaxOrderStatus, sMinOrderStatus)){
              formattedName = sMaxOrderStatus;
	          }
            else
	           {
	           	formattedName  = "Partially " +sMaxOrderStatus;
            }
		
           
           
             return formattedName;
        },

	/*
	getFormattedOrderDisplay:function(dataValue, screen, widget, namespace, modelObj, options) {
            var formatedValue = "";
	    if(!_scBaseUtils.isVoid(dataValue))
	     {
		//formatedValue = _scBaseUtils.convertToUserFormat(dataValue,"DATE");
		//var tmp = dojo.date.stamp.fromISOString(dataValue,{selector: 'date'});
		//formatedValue = dojo.date.locale.format(tmp, {selector:'date',fullYear:'true'});
	     }
         return "6/12/2020";

	}
	*/
//  OMNI-9303 Unable to reprint the pickup acknowledgment slip for STS orders - Start
	extn_reprintAcknowledgement: function(event, bEvent, ctrl, args)
	{
          var shipmentDetailsModel= _scScreenUtils.getModel(this, "getShipmentDetails_output");
	   var deliveryMethod = null;
            deliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", shipmentDetailsModel);
            var shipNode = null;
            shipNode = _scModelUtils.getStringValueFromPath("Shipment.Containers.Container.0.Shipment.ReceivingNode", shipmentDetailsModel);
            if (!(
            _scBaseUtils.equals(
            shipNode, _iasContextUtils.getFromContext("CurrentStore")))) {
                _iasBaseTemplateUtils.showMessage(
                this, _scScreenUtils.getString(
                this, "Message_AckCannotBePrintedDiffentShipNode"), "error", null);
            }  else {
                var shipmentStatus = null;
                shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.SOShipmentStatus", shipmentDetailsModel);
                if (
                _scBaseUtils.contains(shipmentStatus, "1400")|| _scBaseUtils.contains(shipmentStatus, "1600.002")) {
                  var printAckModel = null;
                  printAckModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
	                var inputModel = _scScreenUtils.getInitialInputData(this);
		              var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", inputModel);
		             if(_scBaseUtils.isVoid(shipmentKey)){
	                	 shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.SOShipmentKey", shipmentDetailsModel);
	                }
	                
	              if(!_scBaseUtils.isVoid(shipmentKey)){
	                	 _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, printAckModel);
	                	 _iasUIUtils.callApi(this, printAckModel, "extn_AcademySTSPrintAckSlip_ref", null);
	                }
	               
                } else {
                    _iasBaseTemplateUtils.showMessage(
                    this, _scScreenUtils.getString(
                    this, "Message_AckCannotBePrintedStatus"), "error", null);
                }
            }
	},
  // OMNI-9303 Unable to reprint the pickup acknowledgment slip for STS orders - End

// OMNI -9408 - STS - Order Search Result - Cancellation Product Information - START
extn_prepareCancelledLineList:function(shipment, cancelledLinesArray)
{
 //var cancelledItemListArray = _scBaseUtils.getNewArrayInstance();
// for(var i in shipmentList){
 	var extnShortPickReason = _scModelUtils.getStringValueFromPath("Extn.ExtnShortpickReasonCode", shipment);
 	var shipmentStatus = _scModelUtils.getStringValueFromPath("Status.Status", shipment);
 	var containersModel = _scModelUtils.getStringValueFromPath("Containers.Container", shipment);
 	var zone = _scModelUtils.getStringValueFromPath("Zone", shipment);
 	//if((!_scBaseUtils.isVoid(extnShortPickReason)) || (_scBaseUtils.equals(shipmentStatus,"9000") && _scBaseUtils.isVoid(containersModel)))
 	if(!_scBaseUtils.isVoid(extnShortPickReason))
 	{
 		var shipmentLines = _scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine", shipment);
 		for(var s in shipmentLines)
 		{
 			var shipmentLine = _scModelUtils.getStringValueFromPath("",shipmentLines[s]);
 			var sOriginalQty= _scModelUtils.getStringValueFromPath("OriginalQuantity",shipmentLine );
 			var sQty= _scModelUtils.getStringValueFromPath("Quantity",shipmentLine );
 			var shipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", shipmentLine);
 			var orderLineKey = _scModelUtils.getStringValueFromPath("OrderLine.OrderLineKey", shipmentLine);
 			//_scModelUtils.setStringValueAtModelPath("ExtnShortPickedReasonCode",extnShortPickReason, shipmentLine);
 			if(sOriginalQty!= sQty){
 			_scModelUtils.setStringValueAtModelPath("ExtnShortPickedReasonCode","Inventory Shortage", shipmentLine);
 			var orderedQty = _scModelUtils.getStringValueFromPath("OrderLine.ChainedFromOrderLine.OrderedQty", shipmentLine);
 			_scModelUtils.setStringValueAtModelPath("OrderLine.OrderedQty", orderedQty,shipmentLine);
 			if(!_scBaseUtils.isVoid(cancelledLinesArray))
 			{
 				for(var i in cancelledLinesArray)
 				{
 					var cshipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", cancelledLinesArray[i]);
 					var cOrderLineKey = _scModelUtils.getStringValueFromPath("OrderLine.OrderLineKey", cancelledLinesArray[i]);
 					if((cshipmentLineKey == shipmentLineKey) && (cOrderLineKey == orderLineKey))
 					{
 						_scBaseUtils.removeItemFromArray(cancelledLinesArray, cancelledLinesArray[i]);
 					}
 				}
 			}
 			_scBaseUtils.appendToArray(cancelledLinesArray,shipmentLine);
 			}
			
 		}
 	
 	}
 	else if (_scBaseUtils.equals(shipmentStatus,"9000") && _scBaseUtils.isVoid(containersModel))
 	{
 		var shipmentLines = _scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine", shipment);
 		for(var s in shipmentLines)
 		{
 			var shipmentLine = _scModelUtils.getStringValueFromPath("",shipmentLines[s]);
 			_scModelUtils.setStringValueAtModelPath("ExtnShortPickedReasonCode","Inventory Shortage", shipmentLine);
 			var shipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", shipmentLine);
 			var orderLineKey = _scModelUtils.getStringValueFromPath("OrderLine.OrderLineKey", shipmentLine);
 			if(!_scBaseUtils.isVoid(cancelledLinesArray))
 			{
 				for(var i in cancelledLinesArray)
 				{
 					var cshipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", cancelledLinesArray[i]);
 					var cOrderLineKey = _scModelUtils.getStringValueFromPath("OrderLine.OrderLineKey", cancelledLinesArray[i]);
 					if((cshipmentLineKey == shipmentLineKey) && (cOrderLineKey == orderLineKey))
 					{
 						_scBaseUtils.removeItemFromArray(cancelledLinesArray, cancelledLinesArray[i]);
 					}
 				}
 			}
 			_scBaseUtils.appendToArray(cancelledLinesArray,shipmentLine);
 		}
 	
 	}

    else if(_scBaseUtils.equals(zone,"LOST"))
      {
                                    	 
        var containerDetails = _scModelUtils.getStringValueFromPath("ContainerDetails.ContainerDetail", shipment);
         for (var c in containerDetails)
         {
             var shipmentLine = _scModelUtils.getStringValueFromPath("ShipmentLine",containerDetails[c]);
             var containerQty = _scModelUtils.getNumberValueFromPath("Quantity", containerDetails[c]);
             var orderLineQty = _scModelUtils.getNumberValueFromPath("ShipmentLine.OrderLine.ChainedFromOrderLine.OrderedQty", containerDetails[c]);
            // var calculatedQty = orderLineQty - containerQty;
            // _scModelUtils.setStringValueAtModelPath("OrderLine.OrderedQty", calculatedQty.toString(),shipmentLine);
	           _scModelUtils.setStringValueAtModelPath("OrderLine.OrderedQty", orderLineQty.toString() ,shipmentLine);
             _scModelUtils.setStringValueAtModelPath("ExtnShortPickedReasonCode","Inventory Shortage", shipmentLine);
             _scModelUtils.setStringValueAtModelPath("OrderLine.ItemDetails.DisplayUnitOfMeasure", "EACH", shipmentLine);
             _scModelUtils.setStringValueAtModelPath("OrderLine.ItemDetails.UOMDisplayFormat", "formattedQty", shipmentLine);
             var shipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", shipmentLine);
 			       var orderLineKey = _scModelUtils.getStringValueFromPath("OrderLine.OrderLineKey", shipmentLine);
             if(!_scBaseUtils.isVoid(cancelledLinesArray))
 			       {
 			        	for(var i in cancelledLinesArray)
 			        	{
 			         		var cshipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", cancelledLinesArray[i]);
 				        	var cOrderLineKey = _scModelUtils.getStringValueFromPath("OrderLine.OrderLineKey", cancelledLinesArray[i]);
 			         		if((cshipmentLineKey == shipmentLineKey) && (cOrderLineKey == orderLineKey))
 			        		{
 					        	_scBaseUtils.removeItemFromArray(cancelledLinesArray, cancelledLinesArray[i]);
 					        }
 			        	}
 		       	}
             _scBaseUtils.appendToArray(cancelledLinesArray,shipmentLine);
         }
      }
			                          
   
 //}
    /* var cancelledItemListModel = _scBaseUtils.getNewModelInstance();
    _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine", cancelledItemListArray, cancelledItemListModel);
    _scScreenUtils.setModel(this, "canceledItemsList", cancelledItemListModel, null);	*/

    return cancelledLinesArray;
	
},
// OMNI -9408 - STS - Order Search Result - Cancellation Product Information - END
    });
});