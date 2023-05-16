
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/mobile/common/screens/shipment/picking/ShipmentPickDetailsExtnUI","scbase/loader!ias/utils/BaseTemplateUtils", "scbase/loader!ias/utils/ContextUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils", "scbase/loader!wsc/mobile/common/screens/shipment/picking/ShipmentPickDetailsUI", "scbase/loader!wsc/mobile/home/utils/MobileHomeUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/Userprefs"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnShipmentPickDetailsExtnUI
			 ,
			 _iasBaseTemplateUtils, _iasContextUtils, _iasUIUtils, _scBaseUtils, _scModelUtils, _scScreenUtils, _scWidgetUtils, _wscShipmentUtils, _wscShipmentPickDetailsUI, _wscMobileHomeUtils, _scEventUtils, _scUserprefs
){ 
	return _dojodeclare("extn.mobile.common.screens.shipment.picking.ShipmentPickDetailsExtn", [_extnShipmentPickDetailsExtnUI],{
	// custom code here
	//OMNI-85101 START
	recordCustomerPickupAction: function(
        event, bEvent, ctrl, args) {
            var targetModel = null;
            shipmentDetails = _scBaseUtils.getTargetModel(
            this, "common_getShipmentDetails_input", null);
			
			var shipmentModel = _scScreenUtils.getModel(this, "Shipment");		
			var vOrderNo =_scModelUtils.getStringValueFromPath("Shipment.OrderNo",shipmentModel);
			var vStatus =_scModelUtils.getStringValueFromPath("Shipment.Status.Status",shipmentModel);
			var vExtnIsCurbsidePickupOpted = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentModel);
			var vPackListType =_scModelUtils.getStringValueFromPath("Shipment.PackListType",shipmentModel);
			var vShipmentType =_scModelUtils.getStringValueFromPath("Shipment.ShipmentType",shipmentModel);
			var vIsCurbConsolEnabled = _iasContextUtils.getFromContext("CurbsideConsolidationToggle");
			var instoreConsFlag = _iasContextUtils.getFromContext("InStoreConsolidationToggle");
			//OMNI-105503 - Start
			var vIsInstorePickupOpted =  _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsInstorePickupOpted",shipmentModel);
			var vInStorePickupFlagEnabled = _iasContextUtils.getFromContext("InStorePickupFlagEnabled");
			//OMNI-105503 - End
			if(!_scBaseUtils.isVoid(vOrderNo))
				shipmentDetails.Shipment.OrderNo = vOrderNo;
			if(!_scBaseUtils.isVoid(vStatus))
				shipmentDetails.Shipment.Status = vStatus;
			if (_iasContextUtils.isMobileContainer())
				shipmentDetails.Shipment.IsMobile = "Y";
				shipmentDetails.Shipment.PackListType = vPackListType;
				shipmentDetails.Shipment.ShipmentType = vShipmentType;				
			if(!_scBaseUtils.isVoid(vExtnIsCurbsidePickupOpted))
				shipmentDetails.Shipment.ExtnIsCurbsidePickupOpted = vExtnIsCurbsidePickupOpted;				
			if(!_scBaseUtils.isVoid(vIsCurbConsolEnabled))
				shipmentDetails.Shipment.EnableCurbsideConsolidation = vIsCurbConsolEnabled;
			if(!_scBaseUtils.isVoid(instoreConsFlag))
				shipmentDetails.Shipment.EnableInstoreConsolidation = instoreConsFlag; //OMNI-101859			
			if(!_scBaseUtils.isVoid(vIsInstorePickupOpted))//OMNI-105503 - Start
				shipmentDetails.Shipment.ExtnIsInstorePickupOpted = vIsInstorePickupOpted;				
			if(!_scBaseUtils.isVoid(vInStorePickupFlagEnabled))
				shipmentDetails.Shipment.InStorePickupFlagEnabled = vInStorePickupFlagEnabled;//OMNI-105503 - End
            _iasUIUtils.openWizardInEditor("wsc.components.shipment.customerpickup.CustomerPickUpWizard", shipmentDetails, "wsc.desktop.editors.ShipmentEditor", this, false);
        },
	//OMNI-85101 END
	getDeliveryMethod: function(dataValue, screen, widget, namespace, modelObj, options) {
        var model = _scScreenUtils.getModel(this, "Shipment");

        var deliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", model);
        var ShipmentLines = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", model);
        var documentType = _scModelUtils.getStringValueFromPath("Shipment.DocumentType", model);
	

        if(!_scBaseUtils.isVoid(ShipmentLines)) {
            var shipmentLine = ShipmentLines[0];
            var fulfillmentType = _scModelUtils.getStringValueFromPath("OrderLine.FulfillmentType", shipmentLine);
        }

        if(_scBaseUtils.equals(deliveryMethod, "PICK")) {
            var dMethod = "Pick-up"
            return dMethod;
        } else if (_scBaseUtils.equals(deliveryMethod, "SHP") && _scBaseUtils.equals(fulfillmentType, "SOF")) {
            var dMethod = "Special Order";
            return dMethod;
        } //STS2.0 OMNI-48474 - Start
        else if (_scBaseUtils.equals(deliveryMethod, "SHP") && _scBaseUtils.equals(documentType, "0006")) {
	          var dMethod = "STS";
            return dMethod;
        } //STS2.0 OMNI-48474 - End
        else {
            var dMethod = "SFS";
            return dMethod;
        }
    },
	//START OMNI-102418 OMNI-102127
	receiveClick:function(event,bEvent,ctrl,args){
		window.sessionStorage.removeItem("ContainersModel");
		 	 window.sessionStorage.removeItem("LastScannedContainerModel");	
			_wscMobileHomeUtils.openScreen("extn.mobile.home.STSContainer.container.ReceiveContainer", "extn.mobile.editors.ReceiveContainerEditor");
			
		
	},
	//END OMNI-102418 OMNI-102127
	
	
	
	
   // OMNI-8711 Update Cognos: BOPIS IncompletePickup Shipment Report-start
 extn_RecordStoreUserAction:function(event,bEvent,ctrl,args)
	{
		 var shipmentDetailModel = null;
         shipmentDetailModel = _scScreenUtils.getModel(this, "Shipment");
		 var sShipmentNo=_scModelUtils.getStringValueFromPath("Shipment.ShipmentNo",shipmentDetailModel);
		 var sOrderNo=_scModelUtils.getStringValueFromPath("Shipment.OrderNo",shipmentDetailModel);
		 var sDeliveryMethod=_scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod",shipmentDetailModel);
		 var sUserID=_scUserprefs.getUserId();
		 //OMNI-79480 - Starts
		 var sExtnIsCurbsidePickupOpted =_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentDetailModel);
		 //OMNI-79480 - Ends
		 var sStatus = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", shipmentDetailModel); //OMNI-85083
		 var curbsideConsFlag = _iasContextUtils.getFromContext("CurbsideConsolidationToggle");//OMNI-85083
		 var recordStoreUserActionMashup= _scBaseUtils.getNewModelInstance();
		 _scModelUtils.setStringValueAtModelPath("AcadStoreActionData.ShipmentNo",sShipmentNo,recordStoreUserActionMashup);
		 if(_scBaseUtils.isVoid(sOrderNo)){
			 sOrderNo=_scModelUtils.getStringValueFromPath("Shipment.DisplayOrderNo",shipmentDetailModel);
		 }
		 _scModelUtils.setStringValueAtModelPath("AcadStoreActionData.OrderNo",sOrderNo,recordStoreUserActionMashup);
		 _scModelUtils.setStringValueAtModelPath("AcadStoreActionData.Delivery_Method",sDeliveryMethod,recordStoreUserActionMashup);
		 _scModelUtils.setStringValueAtModelPath("AcadStoreActionData.UserID",sUserID,recordStoreUserActionMashup);
		//OMNI-79480 - Starts
		 if(_scBaseUtils.equals(sExtnIsCurbsidePickupOpted, "Y")){
			_scModelUtils.setStringValueAtModelPath("AcadStoreActionData.NotifyStore",'N',recordStoreUserActionMashup);
		 }
		 //OMNI-79480 - Ends
		 //OMNI-85083 - Start
		if (_iasContextUtils.isMobileContainer() && !_scBaseUtils.isVoid(sExtnIsCurbsidePickupOpted) && _scBaseUtils.equals(sExtnIsCurbsidePickupOpted,"Y") &&
			!_scBaseUtils.isVoid(curbsideConsFlag) && _scBaseUtils.equals(curbsideConsFlag,"Y") && _scBaseUtils.equals(sStatus, "1100.70.06.30.5")){
			_iasUIUtils.callApi(
            this, recordStoreUserActionMashup, "extn_RecordStoreUserActionConsldCurbside", null);
		 }
		else{
		//OMNI-85083 - End
		 _iasUIUtils.callApi(
            this, recordStoreUserActionMashup, "extn_RecordStoreUserActionMashup", null);	
		}	 ;		 
	},	

	//OMNI-8711 Update Cognos: BOPIS IncompletePickup Shipment Report-End
    extn_afterInitializeScreen: function(event, bEvent, ctrl, args) {
    	var shipmentModel = _scScreenUtils.getModel(this, "Shipment");
    	var deliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", shipmentModel);
    	var status = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", shipmentModel);
		var DocumentType = _scModelUtils.getStringValueFromPath("Shipment.DocumentType", shipmentModel);
		var shipmentType = _scModelUtils.getStringValueFromPath("Shipment.ShipmentType", shipmentModel);
		var stsShipmentUIFetaureFlag = _iasContextUtils.getFromContext("globalSTSShipmentUIFeature");
		//**** //OMNI-108769 - Start
        var statusDate =_scModelUtils.getStringValueFromPath("Shipment.StatusDate", shipmentModel);
		var status=_scModelUtils.getStringValueFromPath("Shipment.Status.Status", shipmentModel);
	    var sdate = new Date(statusDate);	
         var assemblyOverdueHours = _iasContextUtils.getFromContext("ProductAssemblyOverdueHours");		
		var fsdate=sdate.setHours(sdate.getHours() + parseInt(assemblyOverdueHours));
		var currentDateTime = new Date();
		if(_scBaseUtils.dateGreaterThanOrEqual(fsdate,currentDateTime) && 
			  _scBaseUtils.equals(status, "1100.70.06.10.5")){
		        _scWidgetUtils.destroyWidget(this,"lbl_timeRemaining");
			    _scWidgetUtils.destroyWidget(this,"img_TimeRmnClock"); 
				_scWidgetUtils.destroyWidget(this,"extn_lbl_timeRemaining");
		 }
        //**** end This change is to hide overdue label and image //OMNI-108769 - End
		// OMNI-54147-Curbside SLA UI ChangesCurbside SLA---Start
		var CurbsideEnabled = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentModel);
		var InstoreEnabled = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsInstorePickupOpted",shipmentModel);//OMNI-105548
		//OMNI-79028 Changes--START
		if(_scBaseUtils.equals(deliveryMethod, "PICK") && (_scBaseUtils.equals(status, "1100.70.06.30.5") || _scBaseUtils.equals(status, "1100.70.06.30.7"))
		&& _scBaseUtils.equals(CurbsideEnabled,"Y")){
			this.extn_getCurbsideAttendByInfo();
		}
		//OMNI-79028 Start
		
		//OMNI-96066 START
		if(_scBaseUtils.equals(deliveryMethod, "PICK") && _scBaseUtils.equals(status, "1100.70.06.10.5")){
			_scWidgetUtils.showWidget(this,"extn_screenbase_link_Assembly", false);
			_scWidgetUtils.hideWidget(this,"lnk_ShipAction", false);
			_scWidgetUtils.hideWidget(this,"lnk_PickAction", false);
		}
		//OMNI-96066 END
		//OMNI-102104 START
		var stsShipmentUIFetaureFlag = _iasContextUtils.getFromContext("globalSTSShipmentUIFeature");
		if(!_scBaseUtils.isVoid(stsShipmentUIFetaureFlag) && _scBaseUtils.equals(stsShipmentUIFetaureFlag ,"Y")) {
			_scWidgetUtils.showWidget(this, "extn_lblShipmentSource", true);
		}
		else{
		_scWidgetUtils.hideWidget(this, "extn_lblShipmentSource", true);
		}
		//OMNI-102104 END
		//*Start OMNI-102418 OMNI-102127
		   var DeliveredContainerListFlag= _scModelUtils.getStringValueFromPath("Shipment.DeliveredContainerListFlag",shipmentModel);
           var IntransitContainerListFlag= _scModelUtils.getStringValueFromPath("Shipment.IntransitContainerListFlag",shipmentModel);
		   if (!_scBaseUtils.isVoid(DeliveredContainerListFlag) && _scBaseUtils.equals(DeliveredContainerListFlag ,"Y")){
               _scWidgetUtils.showWidget(this,"extn_Receive_link", false);
		     }if (!_scBaseUtils.isVoid(IntransitContainerListFlag) && _scBaseUtils.equals(IntransitContainerListFlag ,"Y")){
                    _scWidgetUtils.hideWidget(this,"extn_Receive_link", false);
		       }
		/*
             var containerModel = _scModelUtils.getStringValueFromPath("Shipment.Containers.Container", shipmentModel);
		  var receiveBtnFlag=false;
		  if(!_scBaseUtils.isVoid(containerModel)) {
				var lengthOfcontainerModel = containerModel.length;
				for(var i=0; i<lengthOfcontainerModel; i++) {
					var IsReceived = _scModelUtils.getStringValueFromPath("IsReceived", containerModel[i]);
					var trackingStatus= _scModelUtils.getStringValueFromPath("Extn.ExtnTrackingStatus", containerModel[i]);
					if(_scBaseUtils.equals(trackingStatus, "Delivered") && _scBaseUtils.equals(IsReceived, "N") ) {
						     receiveBtnFlag=true;
						     break;
					 }
					}
				}
		        if((!_scBaseUtils.isVoid(DocumentType) && _scBaseUtils.equals(DocumentType, "0006")) && (receiveBtnFlag) && 
		           (!_scBaseUtils.isVoid(stsShipmentUIFetaureFlag) && _scBaseUtils.equals(stsShipmentUIFetaureFlag ,"Y"))
			        && (_scBaseUtils.equals(status, "1400") || _scBaseUtils.equals(status, "1600"))){ 
		               // _iasContextUtils.addToContext("globalSTSReceivebtnclick","Y" );
			              _scWidgetUtils.showWidget(this,"extn_Receive_link", false);
		            }
                  */
		         //*End OMNI-102418 OMNI-102127
		
		//OMNI-105548
		if (_scBaseUtils.equals(deliveryMethod, "SHP") || _scBaseUtils.equals(status, "1600.002")|| _scBaseUtils.equals(status, "1100.70.06.30") || 
		(_scBaseUtils.equals(status, "1100.70.06.30.5") && (!_scBaseUtils.equals(CurbsideEnabled,"Y")) && (!_scBaseUtils.equals(InstoreEnabled,"Y")))
		 || _scBaseUtils.equals(status, "1100.70.06.30.7") || _scBaseUtils.equals(status, "9000")) {
			_scWidgetUtils.destroyWidget(this,"lbl_timeRemaining");
			_scWidgetUtils.removeClass(this,"extn_shipmentNo","extnInlineBlock");
			_scWidgetUtils.hideWidget(this, "img_TimeRmnClock", true);
		}
		// OMNI-54147-Curbside SLA UI ChangesCurbside SLA---End
		//Start OMNI-72008
	var onMyWayEnabled = _scModelUtils.getStringValueFromPath("Shipment.IsOnMyWayFlag",shipmentModel);
			if(_scBaseUtils.equals(onMyWayEnabled,"Y")){
				_scWidgetUtils.hideWidget(this,"lnk_RecordCustomerPickupAction",true);
			}
		//OMNI-81595-MSL: Last Scan date Changes-START		
		var lastScanDate = _scModelUtils.getStringValueFromPath("Shipment.LastScanDate",shipmentModel);
		if(_scBaseUtils.isVoid(lastScanDate)){
			_scWidgetUtils.hideWidget(this,"extn_Last_Scanned_On",true);
			_scWidgetUtils.hideWidget(this,"extn_STS_LastScan_Date",true);
		}
		//OMNI-81595-MSL: Last Scan date Changes-END
		//End OMNI-72008
		//OMNI-6585: Start
		var ShipmentLines = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentModel);		
    	if(!_scBaseUtils.isVoid(ShipmentLines)) {
            var shipmentLine = ShipmentLines[0];
            var fulfillmentType = _scModelUtils.getStringValueFromPath("OrderLine.FulfillmentType", shipmentLine);
        }
		if(_scBaseUtils.equals(fulfillmentType, "STS") || _scBaseUtils.equals(shipmentType, "STS")){
			//OMNI-6586: Start
			_scWidgetUtils.hideWidget(this,"lnk_PickAction", false);
			//OMNI-6586: End
			_scWidgetUtils.hideWidget(this,"pnlPickInfoCol2", false);
			_scWidgetUtils.showWidget(this,"extn_pnlSTSPickInfoCol2", false);
            _scWidgetUtils.hideWidget(this,"storeMsgPnl",true);
			if(_scBaseUtils.equals(status, "1100.70.06.30")){
				_scWidgetUtils.hideWidget(this,"lnk_RecordCustomerPickupAction",true);
			}
						 <!--Start: OMNI-56194 CURBSIDE UI Changes for STS--> //OMNI-105548
			if(_scBaseUtils.equals(status, "1100.70.06.30.5") && !_scBaseUtils.equals(CurbsideEnabled,"Y") && !_scBaseUtils.equals(InstoreEnabled,"Y")){
	        _scWidgetUtils.destroyWidget(this,"extn_lbl_timeRemaining");}
			 <!--End: OMNI-56194 CURBSIDE UI Changes for STS-->
		}else {
			_scWidgetUtils.showWidget(this,"pnlPickInfoCol2", false);
			_scWidgetUtils.hideWidget(this,"extn_pnlSTSPickInfoCol2", false);
		}
		//OMNI-6585: End

  // OMNI - 9236 - Ship to Store Order Search "customer pick up" - START 
		if(_scBaseUtils.equals(DocumentType, "0006")){
			var shipmentLine = ShipmentLines[0];
			for(var s in ShipmentLines)
			{
		    var maxLineStatus = _scModelUtils.getStringValueFromPath("OrderLine.ChainedFromOrderLine.MaxLineStatus",ShipmentLines[s]);
            var minLineStatus = _scModelUtils.getStringValueFromPath("OrderLine.ChainedFromOrderLine.MinLineStatus",ShipmentLines[s]);
			if((!_scBaseUtils.isVoid(maxLineStatus)) && (!_scBaseUtils.isVoid(minLineStatus)) && (_scBaseUtils.equals(maxLineStatus,"3350.400")) || _scBaseUtils.equals(minLineStatus,"3350.400"))
			{
                _scWidgetUtils.showWidget(this,"lnk_RecordCustomerPickupAction",true);
            	//*Start OMNI-102418 OMNI-102127    
			      	_scWidgetUtils.hideWidget(this,"extn_Receive_link", false);
        	  //*End OMNI-102418 OMNI-102127
			} else {
			     _scWidgetUtils.hideWidget(this,"lnk_RecordCustomerPickupAction",true);
			}
			}
		
		}
  // OMNI - 9236 - Ship to Store Order Search "customer pick up" - START 
    },
    //BOPIS-1612: Begin
    packDefaultAction: function(event, bEvent, ctrl, args) {
        var targetModel = null;
        targetModel = _scBaseUtils.getTargetModel(
        this, "common_getShipmentDetails_input", null);
        // _wscShipmentUtils.openPackingWizard(
        //     this, targetModel);
        _iasUIUtils.callApi(this, targetModel, "extn_getShipmentList_mobile", null);
    },
    extn_afterBehaviorMashupCall: function(event, bEvent, ctrl, args) {
        var mashupRefList = _scModelUtils.getModelObjectFromPath("mashupArray", args);
        if (!_scBaseUtils.isVoid(mashupRefList)) {
            for (var i = 0; i < mashupRefList.length; i++) {
                var mashupRefid = _scModelUtils.getStringValueFromPath("mashupRefId", mashupRefList[i]);
                if (_scBaseUtils.equals(mashupRefid, "extn_getShipmentList_mobile")) {
                    var modelOutput = mashupRefList[i].mashupRefOutput;
                    var shipments = _scModelUtils.getStringValueFromPath("Shipments.Shipment", modelOutput);
                    var shipment = shipments[0];
                    var status = _scModelUtils.getStringValueFromPath("Status", shipment);
                    var targetModel = null;
                    targetModel = _scBaseUtils.getTargetModel(this, "common_getShipmentDetails_input", null);
                    // if (_scBaseUtils.equals(status, "1100.70.06.50") || _scBaseUtils.equals(status, "1100.70.06.70")) {
                    //     _wscShipmentUtils.openPackingWizard(this, targetModel);
                    // }
                    // else {
                    //     _scWidgetUtils.hideWidget(this, "lnk_PackAction", true);
                       
                    // }
                    if (_scBaseUtils.equals(status, "1100.70.06.50")) {
                        _wscShipmentUtils.openPackingWizard(this, targetModel);
                    }
                    else if (_scBaseUtils.equals(status, "1100.70.06.70")) {
                        var val = _scScreenUtils.getWidgetByUId(this, "lnk_PackAction").value;
                        if (_scBaseUtils.equals(val, "Pack")) {
                            _scWidgetUtils.hideWidget(this, "lnk_PackAction", true);
                        }
                        else{
                           _wscShipmentUtils.openPackingWizard(this, targetModel); 
                        }
                    }
                    else {
                        _scWidgetUtils.hideWidget(this, "lnk_PackAction", true);
                        // _wscShipmentUtils.openPackingWizard(this, targetModel);
                        _iasUIUtils.callApi(_iasUIUtils.getParentScreen(this,true),null,"readyForPacking_getShipmentListInit",null);
                    }
                }
                // OMNI - 9236 - Ship to Store Order Search "customer pick up" - START 
                else if(_scBaseUtils.equals(mashupRefid, "extn_getSOShipmentList"))
                {
                	var modelOutput = mashupRefList[i].mashupRefOutput;
                    var shipments = _scModelUtils.getStringValueFromPath("Shipments.Shipment", modelOutput);
                    var totalNoOfRecords = _scModelUtils.getNumberValueFromPath("Shipments.TotalNumberOfRecords", modelOutput);
                    if(totalNoOfRecords ==1)
                    {
                    	var shipmentKey = _scModelUtils.getStringValueFromPath("0.ShipmentKey",shipments);
                    	var vShipmentModel = _scBaseUtils.getNewModelInstance();
						//OMNI-108854 - Start
						var vOrderNo =_scModelUtils.getStringValueFromPath("0.OrderNo",shipments);
						var vStatus =_scModelUtils.getStringValueFromPath("0.Status.Status",shipments);
						var vExtnIsCurbsidePickupOpted = _scModelUtils.getStringValueFromPath("0.Extn.ExtnIsCurbsidePickupOpted",shipments);
						var vPackListType =_scModelUtils.getStringValueFromPath("0.PackListType",shipments);
						var vShipmentType =_scModelUtils.getStringValueFromPath("0.ShipmentType",shipments);
						var vIsCurbConsolEnabled = _iasContextUtils.getFromContext("CurbsideConsolidationToggle");
						var instoreConsFlag = _iasContextUtils.getFromContext("InStoreConsolidationToggle");
						var vIsInstorePickupOpted =  _scModelUtils.getStringValueFromPath("0.Extn.ExtnIsInstorePickupOpted",shipments);
						var vInStorePickupFlagEnabled = _iasContextUtils.getFromContext("InStorePickupFlagEnabled");
						if(!_scBaseUtils.isVoid(vOrderNo))
							_scModelUtils.setStringValueAtModelPath("Shipment.OrderNo",vOrderNo ,vShipmentModel);
						if(!_scBaseUtils.isVoid(vStatus))
							_scModelUtils.setStringValueAtModelPath("Shipment.Status",vStatus ,vShipmentModel);
						if (_iasContextUtils.isMobileContainer())
							_scModelUtils.setStringValueAtModelPath("Shipment.IsMobile","Y" ,vShipmentModel);
							_scModelUtils.setStringValueAtModelPath("Shipment.PackListType",vPackListType ,vShipmentModel);
							_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentType",vShipmentType ,vShipmentModel);			
						if(!_scBaseUtils.isVoid(vExtnIsCurbsidePickupOpted))
							_scModelUtils.setStringValueAtModelPath("Shipment.ExtnIsCurbsidePickupOpted",vExtnIsCurbsidePickupOpted ,vShipmentModel);	
						if(!_scBaseUtils.isVoid(vIsCurbConsolEnabled))
							_scModelUtils.setStringValueAtModelPath("Shipment.EnableCurbsideConsolidation",vIsCurbConsolEnabled ,vShipmentModel);	
						if(!_scBaseUtils.isVoid(instoreConsFlag))
							_scModelUtils.setStringValueAtModelPath("Shipment.EnableInstoreConsolidation",instoreConsFlag ,vShipmentModel);
						if(!_scBaseUtils.isVoid(vIsInstorePickupOpted))
							_scModelUtils.setStringValueAtModelPath("Shipment.ExtnIsInstorePickupOpted",vIsInstorePickupOpted ,vShipmentModel);
						if(!_scBaseUtils.isVoid(vInStorePickupFlagEnabled))
							_scModelUtils.setStringValueAtModelPath("Shipment.InStorePickupFlagEnabled",vInStorePickupFlagEnabled ,vShipmentModel);
						//OMNI-108854 - End
						_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey",shipmentKey ,vShipmentModel);
                    	_iasUIUtils.openWizardInEditor("wsc.components.shipment.customerpickup.CustomerPickUpWizard", vShipmentModel, "wsc.desktop.editors.ShipmentEditor", this, false);
                    }
                    else if(totalNoOfRecords >1)
                    {
                    	for(var i in shipments)
                    	{
                            var status = _scModelUtils.getStringValueFromPath("Status.Status",shipments[i] );
                            if(_scBaseUtils.equals(status,"1100.70.06.30.5"))
                            {
                            	var shipmentKey = _scModelUtils.getStringValueFromPath("ShipmentKey",shipments[i]);
                                var vShipmentModel = _scBaseUtils.getNewModelInstance();
								//OMNI-108854 - Start
								var vOrderNo =_scModelUtils.getStringValueFromPath("OrderNo",shipments[i]);
								var vStatus =_scModelUtils.getStringValueFromPath("Status.Status",shipments[i]);
								var vExtnIsCurbsidePickupOpted = _scModelUtils.getStringValueFromPath("Extn.ExtnIsCurbsidePickupOpted",shipments[i]);
								var vPackListType =_scModelUtils.getStringValueFromPath("PackListType",shipments[i]);
								var vShipmentType =_scModelUtils.getStringValueFromPath("ShipmentType",shipments[i]);
								var vIsCurbConsolEnabled = _iasContextUtils.getFromContext("CurbsideConsolidationToggle");
								var instoreConsFlag = _iasContextUtils.getFromContext("InStoreConsolidationToggle");
								var vIsInstorePickupOpted =  _scModelUtils.getStringValueFromPath("Extn.ExtnIsInstorePickupOpted",shipments[i]);
								var vInStorePickupFlagEnabled = _iasContextUtils.getFromContext("InStorePickupFlagEnabled");
								if(!_scBaseUtils.isVoid(vOrderNo))
									_scModelUtils.setStringValueAtModelPath("Shipment.OrderNo",vOrderNo ,vShipmentModel);
								if(!_scBaseUtils.isVoid(vStatus))
									_scModelUtils.setStringValueAtModelPath("Shipment.Status",vStatus ,vShipmentModel);
								if (_iasContextUtils.isMobileContainer())
									_scModelUtils.setStringValueAtModelPath("Shipment.IsMobile","Y" ,vShipmentModel);
									_scModelUtils.setStringValueAtModelPath("Shipment.PackListType",vPackListType ,vShipmentModel);
									_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentType",vShipmentType ,vShipmentModel);			
								if(!_scBaseUtils.isVoid(vExtnIsCurbsidePickupOpted))
									_scModelUtils.setStringValueAtModelPath("Shipment.ExtnIsCurbsidePickupOpted",vExtnIsCurbsidePickupOpted ,vShipmentModel);	
								if(!_scBaseUtils.isVoid(vIsCurbConsolEnabled))
									_scModelUtils.setStringValueAtModelPath("Shipment.EnableCurbsideConsolidation",vIsCurbConsolEnabled ,vShipmentModel);	
								if(!_scBaseUtils.isVoid(instoreConsFlag))
									_scModelUtils.setStringValueAtModelPath("Shipment.EnableInstoreConsolidation",instoreConsFlag ,vShipmentModel);
								if(!_scBaseUtils.isVoid(vIsInstorePickupOpted))
									_scModelUtils.setStringValueAtModelPath("Shipment.ExtnIsInstorePickupOpted",vIsInstorePickupOpted ,vShipmentModel);
								if(!_scBaseUtils.isVoid(vInStorePickupFlagEnabled))
									_scModelUtils.setStringValueAtModelPath("Shipment.InStorePickupFlagEnabled",vInStorePickupFlagEnabled ,vShipmentModel);
								//OMNI-108854 - End
                    	        _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey",shipmentKey ,vShipmentModel );
                    	        _iasUIUtils.openWizardInEditor("wsc.components.shipment.customerpickup.CustomerPickUpWizard", vShipmentModel, "wsc.desktop.editors.ShipmentEditor", this, false);
                            }

                    	}
                    }
                }
                // OMNI - 9236 - Ship to Store Order Search "customer pick up" - END
            }
        }
    },
    //BOPIS-1612: End
	
	//OMNI-6585: Start - Overriden below OOB methods to restrict the store user to process STS order from search result list screen, if STS order in ReadyforBackroomPick/Backroom PickInProgress status.
	openPickDetails: function(event, bEvent, ctrl, args) {
		var shipmentModel = _scScreenUtils.getModel(this, "Shipment");
    	var status = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", shipmentModel);
    	var ShipmentLines = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentModel);		
    	if(!_scBaseUtils.isVoid(ShipmentLines)) {
            var shipmentLine = ShipmentLines[0];
            var fulfillmentType = _scModelUtils.getStringValueFromPath("OrderLine.FulfillmentType", shipmentLine);
			var shipmentType = _scModelUtils.getStringValueFromPath("Shipment.ShipmentType", shipmentModel);
        }
		//var SearchCriteria = _iasContextUtils.getFromContext("SearchCriteria");
		//var SearchType = _scModelUtils.getStringValueFromPath("Shipment.SearchType", SearchCriteria);
		//if (_scBaseUtils.equals(fulfillmentType, "STS") && (_scBaseUtils.equals(status, "1100.70.06.10") || _scBaseUtils.equals(status, "1100.70.06.20")) && _scBaseUtils.equals(SearchType, "OOBSearch")) {
		var DocumentType = _scModelUtils.getStringValueFromPath("Shipment.DocumentType", shipmentModel);
		//OMNI-69059 Changes - START
		var SOShipmentStatus=_scModelUtils.getStringValueFromPath("Shipment.Containers.Extn.SOShipmentStatus", shipmentModel);
		//OMNI-69059 Changes - END
		// OMNI - 6624 Start
    //if (_scBaseUtils.equals(fulfillmentType, "STS") && (_scBaseUtils.equals(status, "1100.70.06.10") || _scBaseUtils.equals(status, "1100.70.06.20"))){
		if ((_scBaseUtils.equals(fulfillmentType, "STS") && _scBaseUtils.equals(DocumentType, "0001"))||(_scBaseUtils.equals(shipmentType, "STS"))){
		    var strOrderHeaderKey = _scModelUtils.getStringValueFromPath("Shipment.Containers.Container.0.Shipment.OrderHeaderKey", shipmentModel);
			var ShipmentInputModel = _scBaseUtils.getNewModelInstance();
			_scModelUtils.setStringValueAtModelPath("Shipment.OrderHeaderKey",strOrderHeaderKey,ShipmentInputModel);
			_scModelUtils.setStringValueAtModelPath("InvokedFrom","SearchResults",ShipmentInputModel);
			
      //OMNI - 6624 - START
     	var shipmentno = _scModelUtils.getStringValueFromPath("Shipment.ShipmentNo", shipmentModel);
			var shipmentDesc = _scModelUtils.getStringValueFromPath("Shipment.Status.Description", shipmentModel);
			var ShipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", shipmentModel);
			var ShipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey",ShipmentKey,ShipmentInputModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.Status.Status",status,ShipmentInputModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentNo",shipmentno,ShipmentInputModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.Status.Description",shipmentDesc, ShipmentInputModel);
			//Start OMNI-75388, OMNI-75389
			var sOnMyWayScreen = _scModelUtils.getStringValueFromPath("Shipment.IsOnMyWayFlag",shipmentModel);
			var sCurbsidePickupScreen = _scModelUtils.getStringValueFromPath("Shipment.IsCurbsidePickupFlag",shipmentModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.IsOnMyWayScreen",sOnMyWayScreen, ShipmentInputModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.IsCurbsidePickupScreen",sCurbsidePickupScreen, ShipmentInputModel);
			//End OMNI-75388, OMNI-75389
			//OMNI-69059 Changes--START
      		_scModelUtils.setStringValueAtModelPath("Shipment.SOShipmentStatus",SOShipmentStatus,ShipmentInputModel);
	 		//OMNI-69059 Changes --END
	 //OMNI - 6624 - END
      if(!_scBaseUtils.isVoid(strOrderHeaderKey))
			{
			_wscMobileHomeUtils.openScreenWithInputData("extn.mobile.home.STSContainer.transferOrderShipmentSummary.TOShipmentSummary", ShipmentInputModel, "extn.mobile.editors.ReceiveContainerEditor");
			}
			//_iasBaseTemplateUtils.showMessage(
            //    this, "The pick process for STS order should be done in Stage Container screen.", "error", null);
		}else if((_scBaseUtils.equals(DocumentType, "0006")) && (_scBaseUtils.equals(fulfillmentType, "STS"))){
			  var strOrderHeaderKey = _scModelUtils.getStringValueFromPath("Shipment.OrderHeaderKey", shipmentModel);
			var ShipmentInputModel = _scBaseUtils.getNewModelInstance();
			_scModelUtils.setStringValueAtModelPath("Shipment.OrderHeaderKey",strOrderHeaderKey,ShipmentInputModel);
			_scModelUtils.setStringValueAtModelPath("InvokedFrom","SearchResults",ShipmentInputModel);
			//Start OMNI-75388, OMNI-75389
			var sOnMyWayScreen = _scModelUtils.getStringValueFromPath("Shipment.IsOnMyWayFlag",shipmentModel);
			var sCurbsidePickupScreen = _scModelUtils.getStringValueFromPath("Shipment.IsCurbsidePickupFlag",shipmentModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.IsOnMyWayScreen",sOnMyWayScreen, ShipmentInputModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.IsCurbsidePickupScreen",sCurbsidePickupScreen, ShipmentInputModel);
			//End OMNI-75388, OMNI-75389
			if(!_scBaseUtils.isVoid(strOrderHeaderKey))
			{
			_wscMobileHomeUtils.openScreenWithInputData("extn.mobile.home.STSContainer.transferOrderShipmentSummary.TOShipmentSummary", ShipmentInputModel, "extn.mobile.editors.ReceiveContainerEditor");
			}
      		// OMNI - 6624 END
			//_iasBaseTemplateUtils.showMessage(
              //  this, "The pick process for STS order should be done in Stage Container screen.", "error", null);
		}else {
			//OOB code: Start
			var shipmentSummaryTargetModel = null;
			shipmentSummaryTargetModel = _scBaseUtils.getTargetModel(
			this, "shipmentSummaryWizard_input", null);
			var editorName = "wsc.mobile.editors.MobileEditor";
			_iasUIUtils.openWizardInEditor("wsc.components.shipment.summary.ShipmentSummaryWizard", shipmentSummaryTargetModel, editorName, this);
			//OOB code: End
		}
	},
	pickDefaultAction: function(event, bEvent, ctrl, args) {
		var shipmentModel = _scScreenUtils.getModel(this, "Shipment");
		var status = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", shipmentModel);
    	var ShipmentLines = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentModel);
    	if(!_scBaseUtils.isVoid(ShipmentLines)) {
            var shipmentLine = ShipmentLines[0];
            var fulfillmentType = _scModelUtils.getStringValueFromPath("OrderLine.FulfillmentType", shipmentLine);
        }
		if (_scBaseUtils.equals(fulfillmentType, "STS") && (_scBaseUtils.equals(status, "1100.70.06.10") || _scBaseUtils.equals(status, "1100.70.06.20"))){
			_iasBaseTemplateUtils.showMessage(
                this, "Staging should be done using STS screens.", "error", null);
		}else {
			//OOB code: Start
			var mshipmentModel = null;
			mshipmentModel = _scScreenUtils.getTargetModel(this, "common_getShipmentDetails_input", null);
			_wscShipmentUtils.openBackroomPickWizard(this, mshipmentModel);
			//OOB code: End
		}
    },
	getNumberOfContainersLabel: function(
        dataValue, screen, widget, nameSpace, shipmentModel) {
		var returnValue = " ";
		if(_scBaseUtils.isVoid(dataValue)) {
				return returnValue;
			}else if(_scBaseUtils.equals(dataValue, "1")) {
				returnValue = _scScreenUtils.getString(this, "Label_OneContainer");
			}else{
				var inputArray = null;
				inputArray = [];
				inputArray.push(dataValue);
				returnValue = _scScreenUtils.getFormattedString(this, "Label_ContainerCount", inputArray);
			}
			return returnValue;
     },
	 getDisplayOrderNumberList: function(
        dataValue, screen, widget, nameSpace, shipmentModel) {
			var returSOOrderNo = " ";
			var DocumentType = _scModelUtils.getStringValueFromPath("Shipment.DocumentType", shipmentModel);
			if (_scBaseUtils.equals(DocumentType, "0006")){
				returSOOrderNo = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.OrderLine.ChainedFromOrderLine.Order.OrderNo", shipmentModel);
			}else{
				returSOOrderNo = _scModelUtils.getStringValueFromPath("Shipment.DisplayOrderNo", shipmentModel);
			}
			return returSOOrderNo;
		},
	//OMNI-6585: End
        // OMNI - 6624 - START
   extn_CustomerNameForSTSOrders: function(
        dataValue, screen, widget, nameSpace, shipmentModel) {
          var customerName="";
          var customerFirstName="";
          var customerLastName="";
        var sDocumentType = _scModelUtils.getStringValueFromPath("Shipment.DocumentType", shipmentModel);
        if (_scBaseUtils.equals(sDocumentType, "0006")){
  
         customerFirstName= _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.OrderLine.ChainedFromOrderLine.Order.PersonInfoShipTo.FirstName", shipmentModel);
         customerLastName = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.OrderLine.ChainedFromOrderLine.Order.PersonInfoShipTo.LastName", shipmentModel);
	 customerName = customerFirstName +" " +customerLastName;
        }
        else  if (_scBaseUtils.equals(sDocumentType, "0001")){
 	 customerFirstName= _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.FirstName", shipmentModel);
         customerLastName = _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.LastName", shipmentModel);
	 customerName = customerFirstName +" " +customerLastName;

	}
     return customerName;
 
   },
   
   // OMNI - 9236 - Ship to Store Order Search "customer pick up" - START  
    extn_startCustomerPickProcessForContainer: function(event, bEvent, ctrl, args)
    {
	var shipmentModel = _scScreenUtils.getModel(this, "Shipment");
	var documentType = _scModelUtils.getStringValueFromPath("Shipment.DocumentType", shipmentModel);
	var TotalNoOfRecords  = _scModelUtils.getNumberValueFromPath("Shipment.ShipmentLines.TotalNumberOfRecords", shipmentModel);

	if(TotalNoOfRecords >=1 && (_scBaseUtils.equals(documentType,"0006")))
	{
		var ShipmentLine = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0", shipmentModel);
		var vOrderHeaderKey = _scModelUtils.getStringValueFromPath("OrderLine.ChainedFromOrderLine.OrderHeaderKey", ShipmentLine);

		var getShipmentListIn = _scBaseUtils.getNewModelInstance();
		_scModelUtils.setStringValueAtModelPath("Shipment.OrderHeaderKey", vOrderHeaderKey, getShipmentListIn);
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentType", "STS", getShipmentListIn);

		_iasUIUtils.callApi(this, getShipmentListIn, "extn_getSOShipmentList", null);
		_scEventUtils.stopEvent(bEvent);


  	}
    },
   // OMNI - 9236 - Ship to Store Order Search "customer pick up" - END

//OMNI- 10393 STS Identifier on the customer pick up screen  - START
extn_getSTSLabel:function(dataValue, screen, widget, namespace, modelObj, options) {
        var model = _scScreenUtils.getModel(this, "Shipment");
      	//OMNI-87502, OMNI-87879 - Start
      	var isMSL = _scModelUtils.getStringValueFromPath("Shipment.MSL", model);
      	if((!_scBaseUtils.isVoid(isMSL)) && (_scBaseUtils.equals(isMSL,"Y"))) {
      		_scWidgetUtils.showWidget( this, "extn_STS_ShipmentNo", false);
      	}	
      	//OMNI-87502, OMNI-87879 - End
        var shipmentType = _scModelUtils.getStringValueFromPath("Shipment.ShipmentType", model);
        var ShipmentLines = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", model);

        if(!_scBaseUtils.isVoid(ShipmentLines)) {
            var shipmentLine = ShipmentLines[0];
            var fulfillmentType = _scModelUtils.getStringValueFromPath("OrderLine.FulfillmentType", shipmentLine);
        }
	 if(_scBaseUtils.equals(fulfillmentType, "STS") || _scBaseUtils.equals(shipmentType,"STS")) {
            var dMethod = "STS";
            return dMethod;
        }
},

//OMNI- 10393 STS Identifier on the customer pick up screen  - END
	// OMNI-72474, OMNI-79211 - On My Way Status Timer - Start
	getOnMyWayStatusTimer: function(dataValue, screen, widget, nameSpace, shipmentModel) {
		var onMyWayTimerModel = _iasContextUtils.getFromContext("OnMyWayTimerToggle");
		var statusTime = "";
		var onMyWayEnabled = _scModelUtils.getStringValueFromPath("Shipment.IsOnMyWayFlag",shipmentModel);		
		if((_scBaseUtils.equals(onMyWayTimerModel, "Y")) && (_scBaseUtils.equals(onMyWayEnabled,"Y"))){
			_scWidgetUtils.showWidget( this, "extn_OMWStatusTimer", false);
			statusTime = this.calculateOnMyWayStatusTimer(dataValue, screen, widget, nameSpace, shipmentModel);			
		_scModelUtils.setStringValueAtModelPath("Shipment.OMWStatusTimer",statusTime,shipmentModel);
		}
		return statusTime;
    },
	
	calculateOnMyWayStatusTimer: function(dataValue, screen, widget, nameSpace, shipmentModel) {
		var appt = _scModelUtils.getStringValueFromPath("Shipment.AppointmentNo",shipmentModel);
		var statusTime = "";
		if(!_scBaseUtils.isVoid(shipmentModel.Shipment.AppointmentNo)){
			var minutes = "";
			var hours = "";
			var currentDateTime = new Date();			
			var currentYear = currentDateTime.getFullYear();
			var currentMonth = currentDateTime.getMonth();
			var currentDay = currentDateTime.getDate();
			var currentHour = currentDateTime.getHours();

			var apptDate = appt.substr(0, 4) + "." + appt.substr(4, 2) + "." + appt.substr(6, 2) + " " + appt.substr(8, 2) + ":" + appt.substr(10,2) + ":" + appt.substr(12,2);
			var appointment = new Date(apptDate);
			
			var apptYear = appointment.getFullYear();
			var apptMonth = appointment.getMonth();
			var apptDay = appointment.getDate();
			var apptHour = appointment.getHours();
			var sLogin = _iasContextUtils.getFromContext("StoreLoginTime");
			var sLogoff = _iasContextUtils.getFromContext("StoreLogoffTime");
			
			var storeLogin = currentYear + "." + (currentMonth+1) + "." + currentDay + " " + sLogin;
			var storeLogoff = currentYear + "." + (currentMonth+1) + "." + currentDay + " " + sLogoff;
			var storeLoginTime = new Date(storeLogin);
			var storeLogoffTime = new Date(storeLogoff);
			
			var storeLoginHrs = storeLoginTime.getHours();
			var storeLogoffHrs = storeLogoffTime.getHours();
			
			if((currentYear == apptYear) && (currentMonth == apptMonth) && (currentDay == apptDay)){
				//todays orders
				if((currentDateTime < storeLoginTime) || (currentDateTime > storeLogoffTime) ||
					(appointment > currentDateTime)){
					//if the store user logs in odd hours
					statusTime = "0 hours";
					return statusTime;
				} else if ((appointment < storeLoginTime) && (currentDateTime >= storeLoginTime)){
					//if OMW request comes before 9AM today and store is loggedin after 9AM-
					//display the difference based on store login time 9AM
					minutes = ((currentDateTime - storeLoginTime) / 1000)/60;
				} else if ((appointment >= storeLoginTime) && (appointment < storeLogoffTime)){
					//if OMW request comes with in store hours today - display the difference based on APPT
					minutes = ((currentDateTime - appointment) / 1000)/60;
				}
			} else{
				//orders from Previous days
				if (((currentDateTime < storeLoginTime) || (currentDateTime > storeLogoffTime)) ||
					(appointment > currentDateTime)){
					// display 0 for previous days if store user logs in at odd hours other than 9AM-9PM
					statusTime = "0 hours";
					return statusTime;
				} else if ((currentDateTime >= storeLoginTime) && (currentDateTime < storeLogoffTime)){
					//display the timer only when the store associate is logged in between 9AM to 9PM
					minutes = ((currentDateTime - storeLoginTime) / 1000)/60;
				} 
			}
			minutes = Math.floor(minutes);
			if(minutes >= 120){
				hours = minutes/60;
				hours = Math.round(hours);
				statusTime = hours +" hours ago";
			} else {
				statusTime = minutes +" min ago";
			}
		}
		return statusTime;
	},
	//OMNI-96066 START
	click_Assembly_btn : function(event, bEvent, ctrl, args) {       
		 _scScreenUtils.showConfirmMessageBox(this, _scScreenUtils.getString(this, "extn_complete_assembly_confirmation"), "handleClickAssembly", null);//OMNI-98465   
   },
	//OMNI-98465
	handleClickAssembly : function(res, args) {
	if (_scBaseUtils.equals(res, "Ok")) {
		_iasContextUtils.addToContext("isRedirectFromListScreen", "Y");
        this.openPickDetails();
	} 
	else if (_scBaseUtils.equals(res, "Cancel")) {
		_scWidgetUtils.showWidget(this, "extn_button_Assembly");
	}
   },
	//OMNI-96066 END
	
	// OMNI-72474, OMNI-79211 - On My Way Status Timer - End
	//OMNI-79028 Curbside ACK Change-- START//
 extn_getCurbsideAttendByInfo:function() {
        var model = _scScreenUtils.getModel(this, "Shipment");
		var btnText="Team Member Assigned";
        var curbsideAttendedBy = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnCurbsideAttendedBy", model);
        var curbsideExtensionsSessionModel = _iasContextUtils.getFromContext("CurbsideExtensionsAllowed");
        if(!_scBaseUtils.isVoid(curbsideAttendedBy) && _scBaseUtils.equals(curbsideExtensionsSessionModel ,"Y")){
			_scWidgetUtils.setValue(this, "lnk_RecordCustomerPickupAction", btnText, null);
            
        }
	 	
}
//OMNI-79028 Curbside ACK Change-- END//


});
});