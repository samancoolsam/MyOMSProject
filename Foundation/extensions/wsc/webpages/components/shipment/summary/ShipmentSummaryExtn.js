
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/summary/ShipmentSummaryExtnUI","scbase/loader!ias/utils/BaseTemplateUtils", "scbase/loader!ias/utils/ContextUtils", "scbase/loader!ias/utils/PrintUtils", "scbase/loader!ias/utils/RepeatingScreenUtils", "scbase/loader!ias/utils/ScreenUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/EditorUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/RepeatingPanelUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils", "scbase/loader!wsc/components/shipment/container/pack/ContainerPackUtils", "scbase/loader!wsc/components/shipment/container/unpack/UnpackShipmentUtils", "scbase/loader!wsc/components/shipment/summary/ShipmentSummaryUI", "scbase/loader!wsc/components/shipment/summary/ShipmentSummaryUtils","scbase/loader!ias/utils/ContextUtils","scbase/loader!ias/utils/RelatedTaskUtils", "scbase/loader!sc/plat/dojo/utils/ResourcePermissionUtils", "scbase/loader!sc/plat/dojo/Userprefs", "scbase/loader!extn/components/shipment/common/utils/ShipmentUtilsExtn"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnShipmentSummaryExtnUI
			 ,
			 	_iasBaseTemplateUtils, _iasContextUtils, _iasPrintUtils, _iasRepeatingScreenUtils, _iasScreenUtils, _iasUIUtils, _scBaseUtils, _scEditorUtils, _scEventUtils, _scModelUtils, _scRepeatingPanelUtils, _scScreenUtils, _scWidgetUtils, _wscShipmentUtils, _wscContainerPackUtils, _wscUnpackShipmentUtils, _wscShipmentSummaryUI, _wscShipmentSummaryUtils, iasContextUtils, _iasRelatedTaskUtils, _scResourcePermissionUtils, _scUserprefs, _extnShipmentUtilsExtn
){ 
	return _dojodeclare("extn.components.shipment.summary.ShipmentSummaryExtn", [_extnShipmentSummaryExtnUI],{
	//OMNI-85101 START
		handle_getShipmentDetailsForRecordCustomerPick: function(
        mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
			var vIsCurbConsolEnabled = _iasContextUtils.getFromContext("CurbsideConsolidationToggle");
			var instoreConsFlag = _iasContextUtils.getFromContext("InStoreConsolidationToggle");
			var vExtnIsCurbsidePickupOpted = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",modelOutput);
			var vPackListType = _scModelUtils.getStringValueFromPath("Shipment.PackListType",modelOutput);
			var vShipmentType = _scModelUtils.getStringValueFromPath("Shipment.ShipmentType",modelOutput);
			var vIsInstorePickupOpted =  _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsInstorePickupOpted",modelOutput);//OMNI-105503 - Start
			var vInStorePickupFlagEnabled = _iasContextUtils.getFromContext("InStorePickupFlagEnabled");//OMNI-105503 - End
			if (_iasContextUtils.isMobileContainer())
				modelOutput.Shipment.IsMobile = "Y";
				modelOutput.Shipment.PackListType = vPackListType;	
				modelOutput.Shipment.ShipmentType = vShipmentType;	
			if(!_scBaseUtils.isVoid(vIsCurbConsolEnabled))
				modelOutput.Shipment.EnableCurbsideConsolidation = vIsCurbConsolEnabled;
			if(!_scBaseUtils.isVoid(vExtnIsCurbsidePickupOpted))
				modelOutput.Shipment.ExtnIsCurbsidePickupOpted = vExtnIsCurbsidePickupOpted;
			if(!_scBaseUtils.isVoid(instoreConsFlag))
				modelOutput.Shipment.EnableInstoreConsolidation = instoreConsFlag;//OMNI-101859
			if(!_scBaseUtils.isVoid(vIsInstorePickupOpted))//OMNI-105503 - Start
			modelOutput.Shipment.ExtnIsInstorePickupOpted = vIsInstorePickupOpted;				
			if(!_scBaseUtils.isVoid(vInStorePickupFlagEnabled))
			modelOutput.Shipment.InStorePickupFlagEnabled = vInStorePickupFlagEnabled;//OMNI-105503 - End
            _wscShipmentUtils.handleValidationOutput(
            this, mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel);
        },
	//OMNI-85101 END
	  //Start - OMNI-48473 STS2.0 - Overridden initialization mashup to use custom util for displaying next task
     initializeScreen: function(
        event, bEvent, ctrl, args) {
            var shipmentData = null;
            shipmentData = _scScreenUtils.getModel(
            this, "getShipmentDetails_output");
			 var shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", shipmentData);
	        	var documentType = _scModelUtils.getStringValueFromPath("Shipment.DocumentType", shipmentData);
				//OMNI-72013 Begin
				var sExtnOnMyWay=_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnOnMyWayOpted",shipmentData);
				var onMyWaySessionModel = _iasContextUtils.getFromContext("OnMyWayFeatureToggle");
				if(!_scBaseUtils.equals(onMyWaySessionModel, "Y")){
 					_scWidgetUtils.hideWidget(this, "extn_CompleteOMYPane", false);
				}else if(!_scBaseUtils.equals(sExtnOnMyWay, "Y")){
					_scWidgetUtils.hideWidget(this, "extn_CompleteOMYPane", false);
				}
				 //OMNI-72013 End
              if( _scBaseUtils.equals(documentType, "0006")){
                	_extnShipmentUtilsExtn.showNextTask(
                  this, shipmentData, "Shipment.ShipNode.ShipNode", "lnkBRP", "lnkBRP", "", "lnkPack", "lnkPack", "lnkStartCustomerPickup");
              }
               //OMNI-96066 START
              if(!_scBaseUtils.equals(shipmentStatus, "1100.70.06.10.5")){
                 _wscShipmentUtils.showNextTask(
                 this, shipmentData, "Shipment.ShipNode.ShipNode", "lnkBRP", "lnkBRP", "", "lnkPack", "lnkPack", "lnkStartCustomerPickup");
              }
			 //OMNI-96066 END
			  
            if (
            _iasContextUtils.isMobileContainer()) {
                this.fireSetWizardDescEvent(
                shipmentData);
                _scWidgetUtils.hideWidget(
                this, "img_TimeRmnClockWeb", false);
            } else {
                this.updateScreenTitle(
                this, shipmentData);
                this.initializeSLAwidget(
                shipmentData);
            }
            status = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", shipmentData);
            if (!(
            _wscShipmentUtils.showSLA(
            status))) {
                _scWidgetUtils.hideWidget(
                this, "img_TimeRmnClockWeb", true);
            }
            _wscShipmentUtils.showHideHoldLocation(
            this, shipmentData, "lblHoldLocation");
            this.handleStoreAddressPanel(
            shipmentData);
            var deliveryMethod = null;
            deliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", shipmentData);
            if (
            _scBaseUtils.equals(
            deliveryMethod, "PICK")) {
                this.initializeScreenPickupInStore(
                shipmentData);
            } else if (
            _scBaseUtils.equals(
            deliveryMethod, "SHP")) {
                this.initializeScreenShipFromStore(
                shipmentData);
            }

       //OMNI-96066 START
      
        var isListScreen= _iasContextUtils.getFromContext("isRedirectFromListScreen");
        if(!_scBaseUtils.isVoid(isListScreen)&& _scBaseUtils.equals(isListScreen, "Y") &&
            _scBaseUtils.equals(shipmentStatus, "1100.70.06.10.5")){
		    _iasContextUtils.addToContext("isRedirectFromListScreen", "N");   
		    this.extnChangeShipmentStatus();
		}else{
            this.enableAssemblyComplete(shipmentData);
        }
        ////OMNI-96066 END              

      },
		//OMNI-96066 START
		enableAssemblyComplete: function(shipmentData) {
         var documentType = _scModelUtils.getStringValueFromPath("Shipment.DocumentType", shipmentData);
		 var shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", shipmentData);
		 var recordBackroombtn=this.uIdMap.lnkBRP.isHidden;
		 var customerPickbtn=this.uIdMap.lnkBRP.isHidden;
		 if (_scBaseUtils.equals(shipmentStatus, "1100.70.06.10.5") 
			&& _scBaseUtils.equals(documentType, "0001")) {
		      _scWidgetUtils.showWidget(this, "extn_button_Assembly");
		      _scWidgetUtils.hideWidget(this, "lnkStartCustomerPickup");
		      _scWidgetUtils.hideWidget(this, "lnkBRP");	 
			
			}
	    },
		
        extnCompleteAssemblyClick: function(event, bEvent, ctrl, args) {
		 _scScreenUtils.showConfirmMessageBox(this, _scScreenUtils.getString(this, "extn_complete_assembly_confirmation"), "handleConfirm", null);//OMNI-98466
		},
		
		handleConfirm: function(res, args) {
            if (_scBaseUtils.equals(res, "Ok")) {
               this.extnChangeShipmentStatus();
            } 
			else if (_scBaseUtils.equals(res, "Cancel")) {
				_scWidgetUtils.showWidget(this, "extn_button_Assembly");
			}
        },
		
		extnChangeShipmentStatus: function(event, bEvent, ctrl, args) {
			var shipmentDetailsModel = null;
			shipmentDetailsModel = _scScreenUtils.getModel(this, "getShipmentDetails_output");
			var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey",shipmentDetailsModel);
			var changeShpStatus = _scBaseUtils.getNewModelInstance();
			_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey ,changeShpStatus);
			_scModelUtils.setStringValueAtModelPath("Shipment.BaseDropStatus", "1100.70.06.30.5" ,changeShpStatus);
			_scModelUtils.setStringValueAtModelPath("Shipment.TransactionId", "YCD_BACKROOM_PICK" ,changeShpStatus);
			_iasUIUtils.callApi(this, changeShpStatus, "extn_CompleteAssemblyChangeShipment_Ref", null);
		},
   //OMNI-96066 END
   

  //End - OMNI-48473 STS2.0 - Overridden initialization mashup to use custom util for displaying next task
	// custom code here
	// code changes for SLA indicator : Begin
	extn_afterInitializeScreen: function(event, bEvent, ctrl, args) {
		var shipmentData = null;
        shipmentData = _scScreenUtils.getModel(this, "getShipmentDetails_output");
		
		var assignedToUserID = shipmentData.Shipment.AssignedToUserId;
		var shipNodeExtn = shipmentData.Shipment.ShipNode.ShipNode;
		if(!_scBaseUtils.isVoid(assignedToUserID))
		{
			var getUserListAPIInput = null;
                getUserListAPIInput = _scModelUtils.createNewModelObjectWithRootKey("User");
			_scModelUtils.setStringValueAtModelPath("User.DisplayUserID", assignedToUserID, getUserListAPIInput);
			_scModelUtils.setStringValueAtModelPath("User.OrganizationKey", shipNodeExtn, getUserListAPIInput);
			_iasUIUtils.callApi(
            this, getUserListAPIInput, "extn_getUserListByUserID", null);
		}
		
		var deliveryMethod = null;
        deliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", shipmentData);
        var shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", shipmentData);
		
		var shipmentStore = _scModelUtils.getStringValueFromPath("Shipment.ShipNode.ShipNode", shipmentData);
		var currentStore = iasContextUtils.getFromContext("CurrentStore");
		_iasRelatedTaskUtils.hideTaskInWebAndMobile(
                this, "extn_CancelShipmentOtherStoresSummary", false);
		if ((!_scBaseUtils.equals(shipmentStatus, "1100.70.06.10") && !_scBaseUtils.equals(shipmentStatus, "1100.70.06.20") )) {
        	_scWidgetUtils.hideWidget(this, "img_TimeRmnClockWeb", false);
		 	_scWidgetUtils.hideWidget(this, "extn_timeRemaining", false);
		 	_scWidgetUtils.hideWidget(this, "extn_contentpane1", false);
		 	if (_iasContextUtils.isMobileContainer()) {
				var parentScreen = _iasUIUtils.getParentScreen(this, true);
				_scWidgetUtils.hideWidget(parentScreen, "img_TimeRmnClock", false);
			}
        }
        if (_scBaseUtils.equals(deliveryMethod, "SHP")) {
		 	_scWidgetUtils.hideWidget(this, "img_TimeRmnClockWeb", false);
		 	_scWidgetUtils.hideWidget(this, "extn_timeRemaining", false);
		 	_scWidgetUtils.hideWidget(this, "extn_contentpane1", false);
			// Hide sla indicator for sfs orders in mobile
			if (_iasContextUtils.isMobileContainer()) {
				var parentScreen = _iasUIUtils.getParentScreen(this, true);
				_scWidgetUtils.hideWidget(parentScreen, "img_TimeRmnClock", false);
			}
		}
    // code changes for SLA indicator : End
     
		/* The below code takes care such that Cancel Other Store Shipment button is displayed only if orders of store other than which is logged in are opened and if the statuses is Ready For Backroom Pick/Backroom Pick in Progress/Ready for Customer Pick */
		if(_scBaseUtils.equals(deliveryMethod, "PICK") && (!_scBaseUtils.equals(currentStore, shipmentStore)))
		{
			if( _scBaseUtils.equals(shipmentStatus, "1100.70.06.10") || _scBaseUtils.equals(shipmentStatus, "1100.70.06.20") || _scBaseUtils.equals(shipmentStatus, "1100.70.06.30.5") )
			{
				if (_scResourcePermissionUtils.hasPermission("WSCACA01"))
				{
					_iasRelatedTaskUtils.showTaskInWebAndMobile(this, "extn_CancelShipmentOtherStoresSummary", false);
					_iasRelatedTaskUtils.enableTaskInWebAndMobile(this, "extn_CancelShipmentOtherStoresSummary", false);
				}
			}
		}
		
		//code changes for pick dates
		if(_scBaseUtils.equals(shipmentStatus, "1100.70.06.30.5") || _scBaseUtils.equals(shipmentStatus, "1100.70.06.30.7")) 
		{
			var aDate = _scModelUtils.getStringValueFromPath("Shipment.AdditionalDates", shipmentData);
			if(!_scBaseUtils.isVoid(aDate.AdditionalDate)) {
			for(var i=0; i < aDate.AdditionalDate.length; i++) {
					var dateID = aDate.AdditionalDate[i].DateTypeId;
					if(_scBaseUtils.equals(dateID, "ACADEMY_MAX_CUSTOMER_PICK_DATE")) {
						if(!_scBaseUtils.isVoid(aDate.AdditionalDate[i])) {
							var date = aDate.AdditionalDate[i].ActualDate;
							var tmp = dojo.date.stamp.fromISOString(date,{selector: 'date'});
							date = dojo.date.locale.format(tmp, {selector:'date',fullYear:'true'});
							_scWidgetUtils.setValue(this, "extn_datalabel1", date, false);
							break;
						}
					}
				}
			}
			_scWidgetUtils.hideWidget(this, "extn_datalabel2");
			_scWidgetUtils.showWidget(this, "extn_datalabel1");
			_scWidgetUtils.hideWidget(this, "extn_timeRemaining", false);
		}
		//BOPIS-2056 Added Shipment Invoice check for PickUpDate display- Start
		if(_scBaseUtils.equals(deliveryMethod, "PICK") && (_scBaseUtils.equals(shipmentStatus, "1400")) || _scBaseUtils.equals(shipmentStatus, "1600.002")) 
		//BOPIS-2056 Added Shipment Invoice check for PickUpDate display- End
		{
			//Code changes for alternate pickup customer: Begin
			_scWidgetUtils.showWidget(this, "extn_pickedUpBy", false);
			//Code changes for alternate pickup customer: End
			var aDate = _scModelUtils.getStringValueFromPath("Shipment.AdditionalDates", shipmentData);
			if(!_scBaseUtils.isVoid(aDate.AdditionalDate)) {
				for(var i=0; i < aDate.AdditionalDate.length; i++) {
					var dateID = aDate.AdditionalDate[i].DateTypeId;
					if(_scBaseUtils.equals(dateID, "ACADEMY_CUSTOMER_PICKEDUP_DATE")) {
						if(!_scBaseUtils.isVoid(aDate.AdditionalDate[i])) {
							var date = aDate.AdditionalDate[i].ActualDate;
							var tmp = dojo.date.stamp.fromISOString(date,{selector: 'date'});
							date = dojo.date.locale.format(tmp, {selector:'date',fullYear:'true'});
							_scWidgetUtils.setValue(this, "extn_datalabel2", date, false);
							break;
						}
					}
				}
			}
			_scWidgetUtils.hideWidget(this, "extn_datalabel1");
			_scWidgetUtils.showWidget(this, "extn_datalabel2");
			_scWidgetUtils.hideWidget(this, "extn_timeRemaining", false);
		}
		//Code changes to expand Products Tab on screen load - starts
			var eDef = null;
                eDef = {};
            var eArgs = null;
            	eArgs = {};
            _scEventUtils.fireEventInsideScreen(this, "tpShipmentLineDetails_onShow", eDef, eArgs);
        //Code changes to expand Products Tab on screen load - ends
		// hide pack button when status is ready to ship as part of BOPIS-1094: start
        if (_scBaseUtils.equals(shipmentStatus, "1100.70.06.30")) {
            _scWidgetUtils.hideWidget(this,"lnkPack",false);
        } else if (_scBaseUtils.equals(shipmentStatus, "1100.70.06.50")) {
        	            _scWidgetUtils.showWidget(this,"lnkPack",false);
        }
        // hide pack button when status is ready to ship as part of BOPIS-1094: end
	},
	//Code changes for alternate pickup customer: Begin
	extn_getPickedUpCustomer: function(dataValue, screen, widget, namespace, modelObj, options) {
		var shipmentData = _scScreenUtils.getModel(this, "getShipmentDetails_output");
		var sLines = _scModelUtils.getModelObjectFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentData);
		var selectedCustomerName = _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnShipmentPickedBy", shipmentData);
		if (_scBaseUtils.equals(selectedCustomerName, "Alternate")) {
			var pickedUpByCustomer = _scModelUtils.getStringValueFromPath("OrderLine.PersonInfoMarkFor.FirstName", sLines[0])+" "+_scModelUtils.getStringValueFromPath("OrderLine.PersonInfoMarkFor.LastName", sLines[0]);
		} else {
			var pickedUpByCustomer = _scModelUtils.getStringValueFromPath("Shipment.BillToAddress.FirstName", shipmentData)+" "+_scModelUtils.getStringValueFromPath("Shipment.BillToAddress.LastName", shipmentData);
		}
		return pickedUpByCustomer;
	},
	//Code changes for alternate pickup customer: End
	
	/* This method formulates the input for Cancel Shipment from other stores popup */
	extnCancelShipmentForOtherStores : function(event, bEvent, ctrl, args) 
		{
			var screenInputModel = null;
            var shipmentInputModel = null;
            var codeType = null;
            screenInputModel = _scModelUtils.createNewModelObjectWithRootKey("CommonCode");
            _scModelUtils.setStringValueAtModelPath("CommonCode.CallingOrganizationCode", "Academy_Direct", screenInputModel);
            _scModelUtils.setStringValueAtModelPath("CommonCode.CodeType", "YCD_SHORT_RESOLU", screenInputModel);
            var bindings = null;
            bindings = {};
            bindings["ShipmentLine"] = screenInputModel;
            var popupParams = null;
            popupParams = {};
            popupParams["screenInput"] = screenInputModel;
            popupParams["outputNamespace"] = "ShortedShipmentLineModel";
            popupParams["binding"] = bindings;
            var screenConstructorParams = null;
            screenConstructorParams = {};
            screenConstructorParams["shortageReasonPath"] = "ShipmentLine.ShortageResolutionReason";
			screenConstructorParams["flowName"] = "CancelShipmentOtherStores";
            screenConstructorParams["cancelReasonPath"] = "ShipmentLine.CancelReason";
            popupParams["screenConstructorParams"] = screenConstructorParams;
            var dialogParams = null;
            dialogParams = {};
            dialogParams["closeCallBackHandler"] = "onShortageReasonSelection";
            dialogParams["class"] = "popupTitleBorder fixedActionBarDialog";
            _iasUIUtils.openSimplePopup("extn.components.shipment.summary.ExtnCancelShipmentOtherStores", "extn_Cancel_Shipment_from_other_stores_", this, popupParams, dialogParams);
		},
		 //OMNI-72013 Begin
	 extnOnCompleteClick:function(dataValue,screen,widget,namespace,modelObj,options)
	    {
        	var shipmentDetailModel = _scScreenUtils.getModel(this, "getShipmentDetails_output");
		    var sShipmentNo=_scModelUtils.getStringValueFromPath("Shipment.ShipmentKey",shipmentDetailModel);
			var chnageShipmentInputModel=_scBaseUtils.getNewModelInstance();
			_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey",sShipmentNo,chnageShipmentInputModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnOnMyWayOpted",'A',chnageShipmentInputModel);
			_iasUIUtils.callApi(this,chnageShipmentInputModel,"extn_CompleteOnMyWayOrder",null);
		},
	    //OMNI-72013 End
	// OMNI-8711 Update Cognos: BOPIS IncompletePickup Shipment Report-start
 extn_RecordStoreUserAction:function(event,bEvent,ctrl,args)
	{
		 var shipmentDetailModel = null;
         shipmentDetailModel = _scScreenUtils.getModel(this, "getShipmentDetails_output");
		 var sShipmentNo=_scModelUtils.getStringValueFromPath("Shipment.ShipmentNo",shipmentDetailModel);
		 var sOrderNo=_scModelUtils.getStringValueFromPath("Shipment.OrderNo",shipmentDetailModel);
		 var sDeliveryMethod=_scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod",shipmentDetailModel);
		 var sStatus = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", shipmentDetailModel);//OMNI-85083
		 //OMNI-79480 - Starts
		 var sExtnIsCurbsidePickupOpted =_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentDetailModel);
		 //OMNI-79480 - Ends
		 var sUserID=_scUserprefs.getUserId();
		 var curbsideConsFlag = _iasContextUtils.getFromContext("CurbsideConsolidationToggle"); //OMNI-85083
		 var recordStoreUserActionMashup= _scBaseUtils.getNewModelInstance();
		 _scModelUtils.setStringValueAtModelPath("AcadStoreActionData.ShipmentNo",sShipmentNo,recordStoreUserActionMashup);
		 if(_scBaseUtils.isVoid(sOrderNo)){
			 sOrderNo=_scModelUtils.getStringValueFromPath("Shipment.DisplayOrderNo",shipmentDetailModel);
		 }
		 _scModelUtils.setStringValueAtModelPath("AcadStoreActionData.OrderNo",sOrderNo,recordStoreUserActionMashup);
		 _scModelUtils.setStringValueAtModelPath("AcadStoreActionData.Delivery_Method",sDeliveryMethod,recordStoreUserActionMashup);
		 //OMNI-79480 - Starts
		 if(_scBaseUtils.equals(sExtnIsCurbsidePickupOpted, "Y")){
			_scModelUtils.setStringValueAtModelPath("AcadStoreActionData.NotifyStore",'N',recordStoreUserActionMashup);
		 }
		 //OMNI-79480 - Ends
		 _scModelUtils.setStringValueAtModelPath("AcadStoreActionData.UserID",sUserID,recordStoreUserActionMashup);	
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
		}			 
	},	

	//OMNI-8711 Update Cognos: BOPIS IncompletePickup Shipment Report-End
		
		/* This method is to handle the output of ExtnCancelShipmentOtherStores popup */
		onShortageReasonSelection: function(actionPerformed,model, popupParams) 
		{
			if (!(_scBaseUtils.equals(actionPerformed, "CLOSE")))
			{
				var shortedShipmentLineModel = _scScreenUtils
					.getModel(this, "ShortedShipmentLineModel");
				var cancelReason = _scModelUtils.getStringValueFromPath("ShipmentLine.ShortageResolutionReason",shortedShipmentLineModel);
				var shipmentDetailsModel = null;
				shipmentDetailsModel = _scScreenUtils.getModel(
				this, "getShipmentDetails_output");
				var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey",shipmentDetailsModel);
				
				var cancelShipmentInput = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, cancelShipmentInput);
				_scModelUtils.setStringValueAtModelPath("Shipment.ExtnReasonCode", cancelReason, cancelShipmentInput);
				_iasUIUtils.callApi(
                    this, cancelShipmentInput, "extn_CancelShipmentOtherStoreMashupSummary", null);
				
			}
		},
		
		/* This OOB method is overridden to handle the output of the new mashup */
		handleMashupOutput: function(
        mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
			//OMNI-72013 Begin
			if (_scBaseUtils.equals(mashupRefId, "extn_CompleteOnMyWayOrder"))
			 	{
              	 _scWidgetUtils.hideWidget(
               	 this, "extn_CompleteOMYPane", false);
        		}
			//OMNI-72013 End
            if (
            _scBaseUtils.equals(
            mashupRefId, "unpack_getNumReasonCodes_refid")) {
                this.handle_unpack_getNumReasonCodes_refid(
                mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "unpack_deleteContainer_refid")) {
                this.handle_unpack_deleteContainer_refid(
                mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "getShipmentLineList")) {
                if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "getShipmentLineList_output", modelOutput, null);
                }
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "getShipmentContainerList")) {
                if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "getShipmentContainerList_output", modelOutput, null);
                }
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "getShipmentMoreDetails")) {
                this.handleAdditionalInformation(
                modelOutput);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "containerPack_StorePackSlip_94")) {
                this.handle_containerPack_StorePackSlip_94(
                mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "containerPack_StoreLabelReprint_94")) {
                this.handle_containerPack_StoreLabelReprint_94(
                mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "getShipmentDetailsForRecordCustomerPick")) {
                this.handle_getShipmentDetailsForRecordCustomerPick(
                mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel);
            }
			if (
            _scBaseUtils.equals(
            mashupRefId, "extn_CancelShipmentOtherStoreMashupSummary")) {
				_scScreenUtils.setModel(
                    this, "extn_CancelOtherStoreShipmentModel", modelOutput, null);
				 var shipmentModel = null;
                shipmentModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
                 _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", modelOutput), shipmentModel);
				 var errorMessage = _scModelUtils.getStringValueFromPath("Shipment.ErrorMessage", modelOutput);
				 if(_scBaseUtils.isVoid(errorMessage))
				 {
					  _iasUIUtils.callApi(
						this, shipmentModel, "extn_getShipmentDetailsAfterCancellation", null);
				 }
				 else
				 {
					_iasScreenUtils.showInfoMessageBoxWithOk(this,errorMessage,"onCloseOfOtherStoreShipment",{}); 
				 }
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "extn_getShipmentDetailsAfterCancellation")) {
				_scModelUtils.setStringValueAtModelPath("Shipment.ExtnShipNode", _scModelUtils.getStringValueFromPath("Shipment.ShipNode.ShipNode", modelOutput), modelOutput);
				 _scScreenUtils.setModel(
                    this, "getShipmentDetails_output", modelOutput, null);
				this.reloadPrimaryDetails(modelOutput);
               _scEventUtils.fireEventInsideScreen(this, "afterScreenInit", null, {});	
            }
			if (
            _scBaseUtils.equals(
            mashupRefId, "extn_getUserListByUserID")) {
				
				var userName = modelOutput.UserList.User[0].Username;
				
				var shipmentData = null;
				shipmentData = _scScreenUtils.getModel(this, "getShipmentDetails_output");
				_scModelUtils.setStringValueAtModelPath("Shipment.AssignedToUserId", userName, shipmentData);
				 _scScreenUtils.setModel(
                    this, "getShipmentDetails_output", shipmentData, null); 
				
            }

         //OMNI-96066 START
			if (_scBaseUtils.equals(mashupRefId, "extn_CompleteAssemblyChangeShipment_Ref")) {
					var shipmentDetailsModel = null;
		            shipmentDetailsModel = _scScreenUtils.getModel(this, "getShipmentDetails_output");
		            var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey",shipmentDetailsModel);
			        var OrderNo=  _scModelUtils.getStringValueFromPath("Shipment.OrderNo",shipmentDetailsModel);
                    var data = _scBaseUtils.getNewModelInstance();
					_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey ,data);
					_scModelUtils.setStringValueAtModelPath("Shipment.DisplayOrderNo", OrderNo ,data);
					var editorName = "wsc.mobile.editors.MobileEditor";
					_iasUIUtils.openWizardInEditor("wsc.components.shipment.summary.ShipmentSummaryWizard", data, editorName, this);
			}
		//OMNI-96066 END
		//OMNI-98329 - Start
			if(_scBaseUtils.equals(mashupRefId, "extn_getCommonCodeListForProductRegistration_ref")){
				var productRegistrationEnabled = _scModelUtils.getStringValueFromPath("CommonCodeList.CommonCode.0.CodeShortDescription", modelOutput);
				if ((!(_scBaseUtils.isVoid(productRegistrationEnabled))) && (_scBaseUtils.equals(productRegistrationEnabled, "Y"))) {
			
					var model = _scScreenUtils.getModel(this, "getShipmentDetails_output");
					var shipmentLines = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", model);
					var isBMTItem = "N";
					if(!_scBaseUtils.isVoid(shipmentLines)) {
						for(var i=0; i < shipmentLines.length; i++)
						{	
							var sShortageQty = Number(_scModelUtils.getStringValueFromPath("ShortageQty", shipmentLines[i]));
							var sQuantity = Number(_scModelUtils.getStringValueFromPath("Quantity", shipmentLines[i]));
							var sExtnBMTComplianceRequired = _scModelUtils.getStringValueFromPath("OrderLine.ItemDetails.Extn.ExtnBMTComplianceRequired", shipmentLines[i]);
							if (((!(_scBaseUtils.isVoid(sExtnBMTComplianceRequired))) && (_scBaseUtils.equals(sExtnBMTComplianceRequired, "Y"))) &&
								(((!_scBaseUtils.isVoid(sShortageQty)) && (Number(sShortageQty) == 0) && (!_scBaseUtils.isVoid(sQuantity)) && (Number(sQuantity) > 0))
								|| ((!_scBaseUtils.isVoid(sShortageQty)) && (Number(sShortageQty) > 0) && (!_scBaseUtils.isVoid(sQuantity)) && (Number(sQuantity) > 0)))) {
								isBMTItem = "Y";
								break;
							}
						}
					}
					if(_scBaseUtils.equals(isBMTItem, "Y")) {
						var initialInput = null;
						initialInput = _scScreenUtils.getInitialInputData(this);
						_scModelUtils.setStringValueAtModelPath("Shipment.IsFromProductVerification", "", initialInput);
						_scScreenUtils.setInitialInputData(
						_scEditorUtils.getCurrentEditor(), initialInput);
						_scScreenUtils.setInitialInputData(
						_iasUIUtils.getParentScreen(this, true), initialInput);
						_iasScreenUtils.showInfoMessageBoxWithOk(this,_scScreenUtils.getString(this,"Product Requires Registration. Ensure the sale is entered in BMT Database"),"closeWizard",{});
					}
				}
			}
			//OMNI-98329 - End
     },
		
		/* This method is called after handling the output of mashup with mashuprefid="extn_CancelShipmentOtherStoreMashupSummary" and when there is errorMessage to be handled */
		onCloseOfOtherStoreShipment : function(result, args)
		{
			if (_scBaseUtils.equals(result, "Ok")) 
			{
				var cancellationModel = _scScreenUtils.getModel(this, "extn_CancelOtherStoreShipmentModel");
				 var shipmentModel = null;
                shipmentModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
                 _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", cancellationModel), shipmentModel);
				  _iasUIUtils.callApi(
						this, shipmentModel, "extn_getShipmentDetailsAfterCancellation", null);
			}
		},
		
    //overriden method to hide print pack slip confirmation popup : Begin
	afterScreenLoad: function(event, bEvent, ctrl, args) {
        var initialInput = null;
        initialInput = _scScreenUtils.getInitialInputData(
        this);
        var isPackSlipPrintRequired = null;
        var errorDescription = null;
        isPackSlipPrintRequired = _scModelUtils.getStringValueFromPath("Shipment.isPackSlipPrintRequired", initialInput);
        errorDescription = _scModelUtils.getStringValueFromPath("Shipment.ErrorDescription", initialInput);
		var isFromProductVerification = _scModelUtils.getStringValueFromPath("Shipment.IsFromProductVerification", initialInput);//OMNI-98329
        if (!(
        _scBaseUtils.isVoid(
        isPackSlipPrintRequired))) {
            if (
            _scBaseUtils.equals(
            isPackSlipPrintRequired, "Yes")) {
                _scModelUtils.setStringValueAtModelPath("Shipment.isPackSlipPrintRequired", "", initialInput);
                _scScreenUtils.setInitialInputData(
                _scEditorUtils.getCurrentEditor(), initialInput);
                _scScreenUtils.setInitialInputData(
                _iasUIUtils.getParentScreen(
                this, true), initialInput);
                // _scScreenUtils.showConfirmMessageBox(
                // this, _scScreenUtils.getString(
                // this, "Message_PackComplete"), "printPackSlipOnPackCompletion", null);
            }
        }
        if (!(
        _scBaseUtils.isVoid(
        errorDescription))) {
            if (
            _scBaseUtils.equals(
            errorDescription, "NoPermission")) {
                _iasBaseTemplateUtils.showMessage(
                this, _scScreenUtils.getString(
                this, "Label_NoPermission"), "error", null);
            }
        }
		//OMNI-98329 - Start
		if ((!(_scBaseUtils.isVoid(isFromProductVerification))) && (_scBaseUtils.equals(isFromProductVerification, "Y"))){
			var inputModels = _scModelUtils.createNewModelObjectWithRootKey("CommonCode");
			_scModelUtils.setStringValueAtModelPath("CommonCode.CodeType", "TGL_RCP_WEB_SOM_UI", inputModels);
            _scModelUtils.setStringValueAtModelPath("CommonCode.CodeValue", "ENABLE_PRODUCT_REGISTRATION", inputModels);
            _iasUIUtils.callApi(this, inputModels, "extn_getCommonCodeListForProductRegistration_ref", null);
		}
		//OMNI-98329 - End
    },
    //overriden method to hide print pack slip confirmation popup : End
	getDeliveryMethod: function(dataValue, screen, widget, namespace, modelObj, options) {
		var model = _scScreenUtils.getModel(this, "getShipmentDetails_output");

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
    	}
    	else if (_scBaseUtils.equals(deliveryMethod, "SHP") && _scBaseUtils.equals(fulfillmentType, "SOF")) {
            var dMethod = "Special Order";
            return dMethod;
        }else if (_scBaseUtils.equals(deliveryMethod, "SHP") && _scBaseUtils.equals(documentType, "0006")) {
            var dMethod = "STS";
            return dMethod;
        } 
    	else {
    		var dMethod = "SFS";
    		return dMethod;
    	}
	}
});
});

