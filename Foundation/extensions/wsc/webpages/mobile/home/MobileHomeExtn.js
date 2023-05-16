
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/mobile/home/MobileHomeExtnUI","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!sc/plat/dojo/utils/ResourcePermissionUtils","scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!sc/plat/dojo/utils/ControllerUtils","scbase/loader!ias/utils/UIUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!wsc/mobile/home/utils/MobileHomeUtils","scbase/loader!ias/utils/ContextUtils",]
,
function(			 
			    _dojodeclare
			 ,
			    _extnMobileHomeExtnUI
			,
				_scModelUtils
			,
				_scResourcePermissionUtils
			,
				_scWidgetUtils
			,
				_scControllerUtils
			,
				_iasUIUtils
			,
				_scBaseUtils
			,
				_scScreenUtils
			,	
				_wscMobileHomeUtils
            ,
                _iasContextUtils 
){ 
	return _dojodeclare("extn.mobile.home.MobileHomeExtn", [_extnMobileHomeExtnUI],{
	// custom code here
	//To Search eCom Orders
	extn_SearchEComOrders: function() {
            _iasContextUtils.addToContext("LoadedFrom", "MobileHome");
            _wscMobileHomeUtils.openScreen("extn.mobile.home.subscreens.CustomSearchOrders", "wsc.mobile.editors.MobileEditor");
        },
		//Start OMNI-5403 - To open shipment list for waiting curbside shipments
		openCurbSidePickOrder: function() {
			   		           				  
        var shipmentSearchCriteriaModel="";
			  var sCurrentStore = _iasContextUtils.getFromContext("CurrentStore");
        shipmentSearchCriteriaModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
			  _scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", sCurrentStore,shipmentSearchCriteriaModel);
        _scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod", 'PICK',shipmentSearchCriteriaModel);
        _scModelUtils.setStringValueAtModelPath("Shipment.Status",'1100.70.06.30.5',shipmentSearchCriteriaModel);
        _scModelUtils.setStringValueAtModelPath("Shipment.OrderByAttribute",'AppointmentNo',shipmentSearchCriteriaModel);
        _scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",'Y',shipmentSearchCriteriaModel);	
		//Start OMNI-75388
		_scModelUtils.setStringValueAtModelPath("Shipment.IsCurbsidePickupOrder", 'Y',shipmentSearchCriteriaModel);
		//End OMNI-75388
		//OMNI-89343 - Start	
		var sCurbsideConsolidationToggle = _iasContextUtils.getFromContext("CurbsideConsolidationToggle");
		_scModelUtils.setStringValueAtModelPath("Shipment.CurbsideConsolidationToggle", sCurbsideConsolidationToggle,shipmentSearchCriteriaModel);
		//OMNI-89343 - End			
        _wscMobileHomeUtils.openScreenWithInputData("wsc.mobile.home.search.SearchResult", shipmentSearchCriteriaModel, "wsc.mobile.editors.MobileEditor");
			            			
        },
		//End OMNI-5403 - To open shipment list for waiting curbside shipments
		//OMNI-105498 Begin
		openInStorePickupOrder: function() {
			   		           				  
        var shipmentSearchCriteriaModel="";
			  var sCurrentStore = _iasContextUtils.getFromContext("CurrentStore");
        shipmentSearchCriteriaModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
			  _scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", sCurrentStore,shipmentSearchCriteriaModel);
        _scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod", 'PICK',shipmentSearchCriteriaModel);
        _scModelUtils.setStringValueAtModelPath("Shipment.Status",'1100.70.06.30.5',shipmentSearchCriteriaModel);
        _scModelUtils.setStringValueAtModelPath("Shipment.OrderByAttribute",'AppointmentNo',shipmentSearchCriteriaModel);
        _scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnIsInstorePickupOpted",'Y',shipmentSearchCriteriaModel);	
		_scModelUtils.setStringValueAtModelPath("Shipment.IsInStorePickupOrder", 'Y',shipmentSearchCriteriaModel);
        _wscMobileHomeUtils.openScreenWithInputData("wsc.mobile.home.search.SearchResult", shipmentSearchCriteriaModel, "wsc.mobile.editors.MobileEditor");
			            			
        },
		//OMNI-105498 End
		//Start OMNI-71703 - To open shipment list for waiting curbside shipments
		openOnMyWayOrder: function() {
			   		           				  
        var shipmentSearchCriteriaModel="";
			  var sCurrentStore = _iasContextUtils.getFromContext("CurrentStore");
        shipmentSearchCriteriaModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
			  _scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", sCurrentStore,shipmentSearchCriteriaModel);
        _scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod", 'PICK',shipmentSearchCriteriaModel);
        _scModelUtils.setStringValueAtModelPath("Shipment.Status",'1100.70.06.30.5',shipmentSearchCriteriaModel);
      	 //OMNI-75709 - Updating the Order By Attribute as AppointmentNo
        _scModelUtils.setStringValueAtModelPath("Shipment.OrderByAttribute",'AppointmentNo',shipmentSearchCriteriaModel);
        _scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnOnMyWayOpted",'Y',shipmentSearchCriteriaModel);
		_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",'Y',shipmentSearchCriteriaModel);
		_scModelUtils.setStringValueAtModelPath("Shipment.Extn.ExtnIsCurbsidePickupOptedQryType",'NE',shipmentSearchCriteriaModel);
		_scModelUtils.setStringValueAtModelPath("Shipment.IsOnMyWayOrder", 'Y',shipmentSearchCriteriaModel);
        _wscMobileHomeUtils.openScreenWithInputData("wsc.mobile.home.search.SearchResult", shipmentSearchCriteriaModel, "wsc.mobile.editors.MobileEditor");
			            			
        },
		//End OMNI-71703 - To open shipment list for waiting curbside shipments
		
		//OMNI-6581 Start: STS Receiving Screen
		openSTSReceivingScreen: function(){	
		 
		 //clearing session objects
			 window.sessionStorage.removeItem("ContainersModel");
		 	 window.sessionStorage.removeItem("LastScannedContainerModel");	
			_wscMobileHomeUtils.openScreen("extn.mobile.home.STSContainer.container.ReceiveContainer", "extn.mobile.editors.ReceiveContainerEditor");
		},
		//OMNI-6581 End: STS Receiving Screen

		//OMNI-6588 Start: STS Ready To Stage
		openSTSReadyToStageScreen: function(){		
			//_wscMobileHomeUtils.openScreen("extn.mobile.home.STSStaging.backroompick.ReadyToStage", "extn.mobile.editors.ReceiveContainerEditor");
			window.sessionStorage.removeItem("LastScannedContainerModel");
			window.sessionStorage.removeItem("mvalidContainer");
			window.sessionStorage.removeItem("mvalidShipment");
			window.sessionStorage.removeItem("mScannedContainerID");
			var screenInput= _scBaseUtils.getNewModelInstance();
			_scModelUtils.setStringValueAtModelPath("InvokedFrom","HomeScreen",screenInput);
			_scControllerUtils.openScreenInEditor("extn.mobile.home.STSStaging.backroompick.ReadyToStage", screenInput, null, {}, {}, "extn.mobile.editors.ReceiveContainerEditor");
		},
		//OMNI-6588 End: STS Ready To Stage
		//**OMNI-102102 OMNI-102286  Start

	openDeliveredContainerList:  function() {
		var stsShipmentUIFetaureFlag = _iasContextUtils.getFromContext("globalSTSShipmentUIFeature");
		if(!_scBaseUtils.isVoid(stsShipmentUIFetaureFlag) && _scBaseUtils.equals(stsShipmentUIFetaureFlag ,"Y")) {	
		   
		   var oldSearchcriteria= _iasContextUtils.getFromContext("SearchCriteria");
         if(!_scBaseUtils.isVoid(oldSearchcriteria)){
            _iasContextUtils.addToContext("SearchCriteria","");
				
			}
		     var shipmentSearchCriteriaModel="";
			var sCurrentStore = _iasContextUtils.getFromContext("CurrentStore");
            shipmentSearchCriteriaModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
			_scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", sCurrentStore,shipmentSearchCriteriaModel);
        _scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod", 'PICK',shipmentSearchCriteriaModel);
       _scModelUtils.setStringValueAtModelPath("Shipment.Status",'1400',shipmentSearchCriteriaModel);
           _scModelUtils.setStringValueAtModelPath("Shipment.DeliveredContainerListFlag",'Y',shipmentSearchCriteriaModel);	
		_wscMobileHomeUtils.openScreenWithInputData("wsc.mobile.home.search.SearchResult", shipmentSearchCriteriaModel, "wsc.mobile.editors.MobileEditor");
			            	
       }
		},
	
		openIntransitContainerList:  function() {
		var stsShipmentUIFetaureFlag = _iasContextUtils.getFromContext("globalSTSShipmentUIFeature");
		if(!_scBaseUtils.isVoid(stsShipmentUIFetaureFlag) && _scBaseUtils.equals(stsShipmentUIFetaureFlag ,"Y")) {	
		
		     var oldSearchcriteria= _iasContextUtils.getFromContext("SearchCriteria");
         if(!_scBaseUtils.isVoid(oldSearchcriteria)){
            _iasContextUtils.addToContext("SearchCriteria","");
				
			}
	        var shipmentSearchCriteriaModel="";
			var sCurrentStore = _iasContextUtils.getFromContext("CurrentStore");
            shipmentSearchCriteriaModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
			_scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", sCurrentStore,shipmentSearchCriteriaModel);
        _scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod", 'PICK',shipmentSearchCriteriaModel);
       _scModelUtils.setStringValueAtModelPath("Shipment.Status",'1400',shipmentSearchCriteriaModel);
           _scModelUtils.setStringValueAtModelPath("Shipment.IntransitContainerListFlag",'Y',shipmentSearchCriteriaModel);	
		_wscMobileHomeUtils.openScreenWithInputData("wsc.mobile.home.search.SearchResult", shipmentSearchCriteriaModel, "wsc.mobile.editors.MobileEditor");
			            	
       }
		},
		
			//**OMNI-102102 OMNI-102286  END
		
		
		
		
		
		
	/* This OOB method has been overriden in order to hide the "Pick Order for Shipping" and "Ship Packages" tab in the home screen */
	/* New mashup call for batch count is also made */
	initializeScreen: function(
        event, bEvent, ctrl, args) {
            this.checkQueryString();
            var mashupInputModelList = null;
            mashupInputModelList = [];
            var mashupRefIdList = null;
            mashupRefIdList = [];
            var emptyInput = null;
			_scWidgetUtils.hideWidget(this, "extn_pnlSearchEComOrders", true);
            emptyInput = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
            if (
            _scResourcePermissionUtils.hasPermission("WSC000020")) {
               /* _scWidgetUtils.showWidget(
                this, "pnlConfirmOrders", false, null); */
            }
            if (
            _scResourcePermissionUtils.hasPermission("WSC000011")) {
                mashupRefIdList.push("tasks_getShipmentListCount");
                mashupInputModelList.push(
                emptyInput);
            }
            if (
            _scResourcePermissionUtils.hasPermission("WSC000012")) {
                mashupRefIdList.push("openPicks_getShipmentListCount");
                mashupInputModelList.push(
                emptyInput);
            }
            if (
            _scResourcePermissionUtils.hasPermission("WSC000015")) {
              /*  mashupRefIdList.push("pickShip_getShipmentListCount");
                mashupInputModelList.push(
                emptyInput); */
            }
            if (
            _scResourcePermissionUtils.hasPermission("WSC000019")) {
                mashupRefIdList.push("pack_getShipmentListCount");
                mashupInputModelList.push(
                emptyInput);
            }
			
			/* The below code has been added for retrieving the SFS orders count for displaying alongside batches */
			mashupRefIdList.push("extn_SFSOrderForBatches");
            mashupInputModelList.push(emptyInput);
      /* Code ends */
      
			/* Commenting out OMNI-89343 - Start */
			/* Start: OMNI-5402 Curbside Pickup Orders */
			//mashupRefIdList.push("extn_curbsidePickupOrdersCount");
            //mashupInputModelList.push(emptyInput);
			/* End: OMNI-5402 Curbside Pickup Orders */
	  		/* Commenting out OMNI-89343 - End */
			
       /* Start: OMNI-71303 On My Way Orders */
			mashupRefIdList.push("extn_OnMyWayOrdersCount");
            mashupInputModelList.push(emptyInput);
     	 /* End: OMNI-71303 On My Way Orders */
			/* Start: OMNI-6579 STS Receive Containers */
			mashupRefIdList.push("extn_STSReceiveContainers");
				mashupInputModelList.push(emptyInput);
			/* End: OMNI-6579 STS Receive Containers */
                 
			/* Start: OMNI-6580 : STS Stage Containers */
			mashupRefIdList.push("extn_STSStageContainers");
				mashupInputModelList.push(emptyInput);
			/* End: OMNI-6580 : STS Stage Containers */


             //** START OMNI-102102 OMNI-102286 
               mashupRefIdList.push("extn_STSDeliveredContainersCount_ref");
				mashupInputModelList.push(emptyInput);

				mashupRefIdList.push("extn_STSIntransitContainersCount_ref");
				mashupInputModelList.push(emptyInput);


             //** END OMNI-102102 OMNI-102286 

			
			if (_scResourcePermissionUtils.hasPermission("WSC000028")) {
				 _scWidgetUtils.showWidget( this, "pnlBatchPick", false, null);
            }
			if (_scResourcePermissionUtils.hasPermission("WSC000031") || _scResourcePermissionUtils.hasPermission("WSC000033")) {
				 _scWidgetUtils.showWidget( this, "pnlOrderCapture", false, null);
           }
		  /*OMNI-67538 Feature Toggle Changes - START */
		  //fetching the common code type and invoking API when context value is void
			var auditSessionModel = _iasContextUtils.getFromContext("AuditShipmentFeatureToggle");
			if(_scBaseUtils.isVoid(auditSessionModel)){	
				var inputModel = _scModelUtils.createNewModelObjectWithRootKey("CommonCode");
				_scModelUtils.setStringValueAtModelPath("CommonCode.CodeType", "AUDITSHIPMENT_TOGGLE", inputModel);			
				_iasUIUtils.callApi(this, inputModel, "extn_AuditShipmentFeatureToggle", null);		
				auditSessionModel = _iasContextUtils.getFromContext("AuditShipmentFeatureToggle");
			}
			if (_scBaseUtils.equals(auditSessionModel ,"Y")){
				_scWidgetUtils.showWidget(this, "extn_AuditStagingLocation");
			}
			else if (_scBaseUtils.equals(auditSessionModel ,"N")){
				_scWidgetUtils.hideWidget(this, "extn_AuditStagingLocation");
				_scWidgetUtils.showWidget(this, "extn_AuditStagingLocation1");
			}
      /*OMNI-67538 Feature Toggle Changes - END*/
	  /*OMNI-81802 Missed Scan Feature Toggle Changes - START*/
	        var missedSessionModel = _iasContextUtils.getFromContext("MissedScanFeatureToggle");
			if(_scBaseUtils.isVoid(missedSessionModel)){	
				var inputModels = _scModelUtils.createNewModelObjectWithRootKey("CommonCode");
				_scModelUtils.setStringValueAtModelPath("CommonCode.CodeType", "AUDIT_MISSED_SHIP", inputModels);
                            _scModelUtils.setStringValueAtModelPath("CommonCode.CodeValue", "MISSED_SHIPMENT_ENABLED", inputModels);
                           _iasUIUtils.callApi(this, inputModels, "extn_MissedScanFeatureToggle", null);		
				missedSessionModel = _iasContextUtils.getFromContext("MissedScanFeatureToggle");
			}
                          if (_scBaseUtils.equals(missedSessionModel,"Y")){
                         _scWidgetUtils.showWidget(this, "extn_MissedShipment");                           
                      				}
				else if (_scBaseUtils.equals(missedSessionModel ,"N")){
               _scWidgetUtils.hideWidget(this, "extn_MissedShipment");
			_scWidgetUtils.showWidget(this, "extn_MissedShipment1");
	 					}
	 /*OMNI-81802 Missed Scan Feature Toggle Changes - END*/
	  /*OMNI-72164 Feature Toggle Changes - START */
	 
			var onMyWaySessionModel = _iasContextUtils.getFromContext("OnMyWayFeatureToggle");
			if(_scBaseUtils.isVoid(onMyWaySessionModel)){	
				var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
				//_scModelUtils.setStringValueAtModelPath("CommonCode.CodeType", "AUDITSHIPMENT_TOGGLE", inputModel);			
				_iasUIUtils.callApi(this, inputModel, "extn_OnMyWayFeatureToggle", null);		
				onMyWaySessionModel = _iasContextUtils.getFromContext("OnMyWayFeatureToggle");
			}
			if (_scBaseUtils.equals(onMyWaySessionModel ,"Y")){
				_scWidgetUtils.showWidget(this, "extn_OnMyWayMainPane");
			}
			else if (_scBaseUtils.equals(onMyWaySessionModel ,"N")){
				//_scWidgetUtils.hideWidget(this, "extn_OnMyWayLabel");
				_scWidgetUtils.hideWidget(this, "extn_OnMyWayMainPane");
				
			}
			
			
      /*OMNI-72164 Feature Toggle Changes - END*/
		  /*START : OMNI-101859,OMNI-102131,OMNI-85177 */
		var stsShipmentUIFetaureFlag = _iasContextUtils.getFromContext("globalSTSShipmentUIFeature");
		var vInStoreConsolidationToggle = _iasContextUtils.getFromContext("InStoreConsolidationToggle");
		var curbsideConsToggleEnabled = _iasContextUtils.getFromContext("CurbsideConsolidationToggle");
		var vInStorePickupFlagEnabled = _iasContextUtils.getFromContext("InStorePickupFlagEnabled");//OMNI-105498
		if(_scBaseUtils.isVoid(stsShipmentUIFetaureFlag) || _scBaseUtils.isVoid(vInStoreConsolidationToggle) || _scBaseUtils.isVoid(curbsideConsToggleEnabled) || _scBaseUtils.isVoid(vInStorePickupFlagEnabled)){
			var getToggleModel = _scModelUtils.createNewModelObjectWithRootKey("CommonCode");
			_iasUIUtils.callApi(this, getToggleModel, "extn_DetermineUIToggle_ref", null);
		}else if ((_scBaseUtils.equals(curbsideConsToggleEnabled ,"Y"))){
						var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
						inputModel.Shipment = {};
						inputModel.Shipment.ComplexQuery={};
						inputModel.Shipment.ComplexQuery.Operator='AND';
						inputModel.Shipment.ComplexQuery.Or=[];
						inputModel.Shipment.ComplexQuery.Or[0]={};
						inputModel.Shipment.ComplexQuery.Or[0].Exp=[];					
						inputModel.Shipment.ComplexQuery.Or[0].Exp[0]={};
						inputModel.Shipment.ComplexQuery.Or[0].Exp[0].Value='1100.70.06.30.5';	
						inputModel.Shipment.ComplexQuery.Or[0].Exp[0].Name='Status';						
						inputModel.Shipment.ComplexQuery.Or[0].Exp[0].QryType='EQ';	
						inputModel.Shipment.ComplexQuery.Or[0].Exp[1]={};
						inputModel.Shipment.ComplexQuery.Or[0].Exp[1].Name='Status';						
						inputModel.Shipment.ComplexQuery.Or[0].Exp[1].QryType='EQ';						
						inputModel.Shipment.ComplexQuery.Or[0].Exp[1].Value='1100.70.06.30.7';
						_iasUIUtils.callApi(this, inputModel, "extn_curbsidePickupOrdersCount", null);	
						
					}	else if (_scBaseUtils.equals(curbsideConsToggleEnabled ,"N")){
						var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
						_scModelUtils.setStringValueAtModelPath("Shipment.Status", "1100.70.06.30.5", inputModel);
						_iasUIUtils.callApi(this, inputModel, "extn_curbsidePickupOrdersCount", null);					
					}
		if ((_scBaseUtils.equals(vInStorePickupFlagEnabled ,"Y"))){//OMNI-105498 start
					var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
					inputModel.Shipment = {};
					inputModel.Shipment.ComplexQuery={};
					inputModel.Shipment.ComplexQuery.Operator='AND';
					inputModel.Shipment.ComplexQuery.Or=[];
					inputModel.Shipment.ComplexQuery.Or[0]={};
					inputModel.Shipment.ComplexQuery.Or[0].Exp=[];					
					inputModel.Shipment.ComplexQuery.Or[0].Exp[0]={};
					inputModel.Shipment.ComplexQuery.Or[0].Exp[0].Value='1100.70.06.30.5';	
					inputModel.Shipment.ComplexQuery.Or[0].Exp[0].Name='Status';						
					inputModel.Shipment.ComplexQuery.Or[0].Exp[0].QryType='EQ';	
					inputModel.Shipment.ComplexQuery.Or[0].Exp[1]={};
					inputModel.Shipment.ComplexQuery.Or[0].Exp[1].Name='Status';						
					inputModel.Shipment.ComplexQuery.Or[0].Exp[1].QryType='EQ';						
					inputModel.Shipment.ComplexQuery.Or[0].Exp[1].Value='1100.70.06.30.7';
					_iasUIUtils.callApi(this, inputModel, "extn_InStorePickupOrdersCount", null);	
			
					}
			if(_scBaseUtils.equals(vInStorePickupFlagEnabled ,"Y")) {
                 _scWidgetUtils.showWidget(this, "extn_InStorePickupMainPane");
             }
             if(_scBaseUtils.equals(vInStorePickupFlagEnabled ,"N")) {
                  _scWidgetUtils.hideWidget(this, "extn_InStorePickupMainPane");
             }
	   /*END : OMNI-101859,OMNI-102131,OMNI-85177, OMNI-105498 */
	  
	   // **Start OMNI-102418 OMNI-102127

         var stsShipmentUIFetaureFlag  = _iasContextUtils.getFromContext("globalSTSShipmentUIFeature");
         if (_scBaseUtils.equals(stsShipmentUIFetaureFlag ,"Y")) {
				_scWidgetUtils.showWidget(this, "extn_STS_Intransit_contentpane");
				_scWidgetUtils.showWidget(this, "extn_STS_Delivered_contentpane");   
			}
			else if (_scBaseUtils.equals(stsShipmentUIFetaureFlag ,"N")){
				_scWidgetUtils.hideWidget(this, "extn_STS_Intransit_contentpane");
				_scWidgetUtils.hideWidget(this, "extn_STS_Delivered_contentpane");
				
			}

          // **End OMNI-102418 OMNI-102127


/*OMNI-72475 Timer Toggle Changes - END*/
			var onMyWayTimerModel = _iasContextUtils.getFromContext("OnMyWayTimerToggle");
			if(_scBaseUtils.isVoid(onMyWayTimerModel)){	
					var inpModel = _scModelUtils.createNewModelObjectWithRootKey("OnMyWayTimer");
					_iasUIUtils.callApi(this, inpModel, "extn_OnMyWayTimerToggle", null);		
					onMyWayTimerModel = _iasContextUtils.getFromContext("OnMyWayTimerToggle");
			}
			/*OMNI-72475 Timer Toggle Changes - END*/
			/*OMNI-72474 Store Working Hours for OMW - START */
			var storeLogin = _iasContextUtils.getFromContext("StoreLoginTime");
			var storeLogoff = _iasContextUtils.getFromContext("StoreLogoffTime");
			if(_scBaseUtils.isVoid(storeLogin) || _scBaseUtils.isVoid(storeLogoff)){	
				var inpModel = _scModelUtils.createNewModelObjectWithRootKey("CommonCode");
				_iasUIUtils.callApi(this, inpModel, "extn_StoreWorkingHours", null);		
				storeLogin = _iasContextUtils.getFromContext("StoreLoginTime");
				storeLogoff = _iasContextUtils.getFromContext("StoreLogoffTime");
			}
			/*OMNI-72474 Store Working Hours for OMW - END */
			/* OMNI-83367 Start */
			var curbsideSessionModel = _iasContextUtils.getFromContext("CurbsideDelayMaxCounter");
			var curbsideExtensionsSessionModel = _iasContextUtils.getFromContext("CurbsideExtensionsAllowed");
			//OMNI- 79883
			var curbsideDefaultMins = _iasContextUtils.getFromContext("CurbsideDefaultMins"); 
			var sms1 = _iasContextUtils.getFromContext("sms1");
			var sms2 = _iasContextUtils.getFromContext("sms2");
			var sms3 = _iasContextUtils.getFromContext("sms3");
			if((_scBaseUtils.isVoid(sms1)) || (_scBaseUtils.isVoid(sms2)) || (_scBaseUtils.isVoid(sms3))|| (_scBaseUtils.isVoid(curbsideSessionModel)) || (_scBaseUtils.isVoid(curbsideExtensionsSessionModel)) || 
			(_scBaseUtils.isVoid(curbsideDefaultMins))){ 
				var inputModel = _scModelUtils.createNewModelObjectWithRootKey("CommonCode");
				_scModelUtils.setStringValueAtModelPath("CommonCode.CodeType", "CURBSIDE_EXTENSIONS", inputModel);			
				_iasUIUtils.callApi(this, inputModel, "extn_CurbsideExtensions_ref", null);		
				curbsideSessionModel = _iasContextUtils.getFromContext("CurbsideDelayMaxCounter");
                curbsideExtensionsSessionModel = _iasContextUtils.getFromContext("CurbsideExtensionsAllowed");
			}
			/* OMNI-83367 End */
            var mashupContext = null;
            mashupContext = _scControllerUtils.getMashupContext(
            this);
            _iasUIUtils.callApis(
            this, mashupInputModelList, mashupRefIdList, mashupContext, null);
        },
		
		/* This OOB method has been overridden to handle behavior of new mashup added */
		handleMashupOutput: function(
        mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
            if (
            _scBaseUtils.equals(
            mashupRefId, "getShipmentList")) {
                this.handle_getShipmentList(
                mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "tasks_getShipmentListCount")) {
                if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "tasks_getShipmentListCount_output", modelOutput, null);
                }
                this.handle_tasks_getShipmentListCount(
                mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "openPicks_getShipmentListCount")) {
                if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "openPicks_getShipmentListCount_output", modelOutput, null);
                }
                this.handle_openPicks_getShipmentListCount(
                mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "pickShip_getShipmentListCount")) {
                if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "pickShip_getShipmentListCount_output", modelOutput, null);
                }
                this.handle_pickShip_getShipmentListCount(
                mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "pack_getShipmentListCount")) {
                if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "pack_getShipmentListCount_output", modelOutput, null);
                }
                this.handle_pack_getShipmentListCount(
                mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel);
            }
			if (
            _scBaseUtils.equals(
            mashupRefId, "extn_SFSOrderForBatches")) {
				if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "extn_SFSOrderCountMobile", modelOutput, null);
                }
			}
			/* Start: OMNI-5402 Curbside Pickup Orders */
			if (
            _scBaseUtils.equals(
            mashupRefId, "extn_curbsidePickupOrdersCount")) {
				if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "extn_CurbsidePickupOrders_output", modelOutput, null);
                }
			}
			/* End: OMNI-5402 Curbside Pickup Orders */
			/* Start: OMNI-71303 Curbside Pickup Orders */
			if (
            _scBaseUtils.equals(
            mashupRefId, "extn_OnMyWayOrdersCount")) {
				if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "extn_OnMyWayOrdersCount_output", modelOutput, null);
                }
			}
			/* End: OMNI-71303 Curbside Pickup Orders */
			/* Start: OMNI-6579 STS Receive Containers */
			if (
            _scBaseUtils.equals(
            mashupRefId, "extn_STSReceiveContainers")) {
				if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "extn_STSReceiveContainersCount_output", modelOutput, null);
                }
			}
			/* End: OMNI-6579 STS Receive Containers */
			
			/* Start: OMNI-6580 : STS Stage Containers */
			if (
            _scBaseUtils.equals(
            mashupRefId, "extn_STSStageContainers")) {
				if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "extn_STSStageContainersCount_output", modelOutput, null);
                }
			}
			/* End: OMNI-6580 : STS Stage Containers */

             // OMNI-102102 OMNI-102286  ***START***

             if (
            _scBaseUtils.equals(
            mashupRefId, "extn_STSDeliveredContainersCount_ref")) {
				if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "extn_STSDeliveredContainersCount_output", modelOutput, null);
                }
			}

			if (
            _scBaseUtils.equals(
            mashupRefId, "extn_STSIntransitContainersCount_ref")) {
				if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "extn_STSIntransitContainersCount_output", modelOutput, null);
                }
			}

           // OMNI-102102 OMNI-102286  ***END***
		//OMNI-105498 Start
		if (
            _scBaseUtils.equals(
            mashupRefId, "extn_InStorePickupOrdersCount")) {
				if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "extn_InStorePickupOrdersCount_output", modelOutput, null);
                }
			}
		//OMNI-105498 End

           /* OMNI-67538 Feature Toggle Changes - START */
			//when context value is void fetching the common code value and that value is setting it to context
			if(_scBaseUtils.equals(mashupRefId, "extn_AuditShipmentFeatureToggle")){
				var auditFeatureToggleEnabled = _scModelUtils.getStringValueFromPath("CommonCodeList.CommonCode.0.CodeShortDescription", modelOutput);
				_iasContextUtils.addToContext("AuditShipmentFeatureToggle",auditFeatureToggleEnabled );						
				if (_scBaseUtils.equals(auditFeatureToggleEnabled ,"Y")){
				_scWidgetUtils.showWidget(this, "extn_AuditStagingLocation");
				}
				else{
				_scWidgetUtils.hideWidget(this, "extn_AuditStagingLocation");
				_scWidgetUtils.showWidget(this, "extn_AuditStagingLocation1");
				}
			}
			/* OMNI-67538 Feature Toggle Changes - END */
			/*OMNI-81802 Missed Scan Feature Toggle Changes - START*/
			 if(_scBaseUtils.equals(mashupRefId, "extn_MissedScanFeatureToggle")){
				var missedFeatureToggleEnabled = _scModelUtils.getStringValueFromPath("CommonCodeList.CommonCode.0.CodeShortDescription", modelOutput);
				_iasContextUtils.addToContext("MissedScanFeatureToggle",missedFeatureToggleEnabled );						
				if (_scBaseUtils.equals(missedFeatureToggleEnabled ,"Y")){
                         _scWidgetUtils.showWidget(this, "extn_MissedShipment");
                                        				}
				else if (_scBaseUtils.equals(missedFeatureToggleEnabled ,"N")){
                      _scWidgetUtils.hideWidget(this, "extn_MissedShipment");
			_scWidgetUtils.showWidget(this, "extn_MissedShipment1");
	 					}
			}
			 /*OMNI-81802 Missed Scan Feature Toggle Changes - END*/
			/*OMNI-72164 Feature Toggle Changes - START */
	 
			if(_scBaseUtils.equals(mashupRefId, "extn_OnMyWayFeatureToggle")){
				var onMyWayToggleEnabled = _scModelUtils.getStringValueFromPath("Shipment.IsOnMyWayEnabled", modelOutput);
				_iasContextUtils.addToContext("OnMyWayFeatureToggle",onMyWayToggleEnabled );						
				if (_scBaseUtils.equals(onMyWayToggleEnabled ,"Y")){
				_scWidgetUtils.showWidget(this, "extn_OnMyWayMainPane");
				}
				else{
				_scWidgetUtils.hideWidget(this, "extn_OnMyWayMainPane");
								
				}
			}
     		 /*OMNI-72164 Feature Toggle Changes - END*/
			
			/*OMNI-72475 Timer Toggle Changes - START */

			if(_scBaseUtils.equals(mashupRefId, "extn_OnMyWayTimerToggle")){
				var onMyWayTimerEnabled = _scModelUtils.getStringValueFromPath("OnMyWayTimer.IsOnMyWayTimerEnabled", modelOutput);
				if ((_scBaseUtils.equals(onMyWayTimerEnabled ,"Y"))){
					_iasContextUtils.addToContext("OnMyWayTimerToggle",onMyWayTimerEnabled );						
				}
			}
			/*OMNI-72475 Timer Toggle Changes - END */
			
		
  /*START : OMNI-101859,OMNI-102131,OMNI-85177 */
   if(_scBaseUtils.equals(mashupRefId, "extn_DetermineUIToggle_ref")) {
	var CommonCodeList = _scModelUtils.getStringValueFromPath("CommonCodeList.CommonCode", modelOutput);
		for(var i in CommonCodeList){
			var sCodeValue = _scModelUtils.getStringValueFromPath("CodeValue", CommonCodeList[i]);
			if(_scBaseUtils.equals(sCodeValue,"STS_SHIPMENT_UI_FEATURES")){
				var stsShipmentUIFetaureFlag = _scModelUtils.getStringValueFromPath("CodeShortDescription", CommonCodeList[i]);
				if(!_scBaseUtils.isVoid(stsShipmentUIFetaureFlag)){
				   _iasContextUtils.addToContext("globalSTSShipmentUIFeature",stsShipmentUIFetaureFlag );
				}
        // Start OMNI-102102 OMNI-102286
			if(_scBaseUtils.equals(stsShipmentUIFetaureFlag ,"Y")) {
		 _scWidgetUtils.showWidget(this, "extn_STS_Intransit_contentpane");
		 _scWidgetUtils.showWidget(this, "extn_STS_Delivered_contentpane");
 		}
			if(_scBaseUtils.equals(stsShipmentUIFetaureFlag ,"N")) {
		 _scWidgetUtils.hideWidget(this, "extn_STS_Intransit_contentpane");
		 _scWidgetUtils.hideWidget(this, "extn_STS_Delivered_contentpane");
		}
       // End OMNI-102102 OMNI-102286
			}else if(_scBaseUtils.equals(sCodeValue,"ENABLE_INSTORE_PICKUP")){//OMNI-105498 Begin
				var enableInStorePickupFlag = _scModelUtils.getStringValueFromPath("CodeShortDescription", CommonCodeList[i]);
				if(!_scBaseUtils.isVoid(enableInStorePickupFlag)){
				   _iasContextUtils.addToContext("InStorePickupFlagEnabled",enableInStorePickupFlag );
				   if(_scBaseUtils.equals(enableInStorePickupFlag ,"Y")) {
					_scWidgetUtils.showWidget(this, "extn_InStorePickupMainPane");
					}
					if(_scBaseUtils.equals(enableInStorePickupFlag ,"N")) {
					_scWidgetUtils.hideWidget(this, "extn_InStorePickupMainPane");
					}
					if ((_scBaseUtils.equals(enableInStorePickupFlag ,"Y"))){
					var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
					inputModel.Shipment = {};
					inputModel.Shipment.ComplexQuery={};
					inputModel.Shipment.ComplexQuery.Operator='AND';
					inputModel.Shipment.ComplexQuery.Or=[];
					inputModel.Shipment.ComplexQuery.Or[0]={};
					inputModel.Shipment.ComplexQuery.Or[0].Exp=[];					
					inputModel.Shipment.ComplexQuery.Or[0].Exp[0]={};
					inputModel.Shipment.ComplexQuery.Or[0].Exp[0].Value='1100.70.06.30.5';	
					inputModel.Shipment.ComplexQuery.Or[0].Exp[0].Name='Status';						
					inputModel.Shipment.ComplexQuery.Or[0].Exp[0].QryType='EQ';	
					inputModel.Shipment.ComplexQuery.Or[0].Exp[1]={};
					inputModel.Shipment.ComplexQuery.Or[0].Exp[1].Name='Status';						
					inputModel.Shipment.ComplexQuery.Or[0].Exp[1].QryType='EQ';						
					inputModel.Shipment.ComplexQuery.Or[0].Exp[1].Value='1100.70.06.30.7';
					_iasUIUtils.callApi(this, inputModel, "extn_InStorePickupOrdersCount", null);	
			
					}
				}
			}//OMNI-105498 End
			//OMNI-108769 - Start
			else if(_scBaseUtils.equals(sCodeValue,"ASSEMBLY_OVRDUE_HRS")) {
		
				var assemblyOverdueHours = _scModelUtils.getStringValueFromPath("CodeShortDescription", CommonCodeList[i]);
				if(!_scBaseUtils.isVoid(assemblyOverdueHours)){
				   _iasContextUtils.addToContext("ProductAssemblyOverdueHours",assemblyOverdueHours );
				}
		}
//OMNI-108769 - End
			else if(_scBaseUtils.equals(sCodeValue,"ENABLE_INSTORE_CONSOLIDATION")){
				var isInstoreConsolidationEnabled =  _scModelUtils.getStringValueFromPath("CodeShortDescription", CommonCodeList[i]);
				if(!_scBaseUtils.isVoid(isInstoreConsolidationEnabled)){
						_iasContextUtils.addToContext("InStoreConsolidationToggle",isInstoreConsolidationEnabled);						
					}
			}else if(_scBaseUtils.equals(sCodeValue,"ENABLE_CURBSIDE_CONSOLIDATION")){
				var curbsideConsToggleEnabled = _scModelUtils.getStringValueFromPath("CodeShortDescription", CommonCodeList[i]);
				if(!_scBaseUtils.isVoid(curbsideConsToggleEnabled)){
					_iasContextUtils.addToContext("CurbsideConsolidationToggle",curbsideConsToggleEnabled );
					 /*OMNI-89343 - Start */
					if ((_scBaseUtils.equals(curbsideConsToggleEnabled ,"Y"))){
						var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
						inputModel.Shipment = {};
						inputModel.Shipment.ComplexQuery={};
						inputModel.Shipment.ComplexQuery.Operator='AND';
						inputModel.Shipment.ComplexQuery.Or=[];
						inputModel.Shipment.ComplexQuery.Or[0]={};
						inputModel.Shipment.ComplexQuery.Or[0].Exp=[];					
						inputModel.Shipment.ComplexQuery.Or[0].Exp[0]={};
						inputModel.Shipment.ComplexQuery.Or[0].Exp[0].Value='1100.70.06.30.5';	
						inputModel.Shipment.ComplexQuery.Or[0].Exp[0].Name='Status';						
						inputModel.Shipment.ComplexQuery.Or[0].Exp[0].QryType='EQ';	
						inputModel.Shipment.ComplexQuery.Or[0].Exp[1]={};
						inputModel.Shipment.ComplexQuery.Or[0].Exp[1].Name='Status';						
						inputModel.Shipment.ComplexQuery.Or[0].Exp[1].QryType='EQ';						
						inputModel.Shipment.ComplexQuery.Or[0].Exp[1].Value='1100.70.06.30.7';
						_iasUIUtils.callApi(this, inputModel, "extn_curbsidePickupOrdersCount", null);	
						
					}	else if (_scBaseUtils.equals(curbsideConsToggleEnabled ,"N")){
						var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
						_scModelUtils.setStringValueAtModelPath("Shipment.Status", "1100.70.06.30.5", inputModel);
						_iasUIUtils.callApi(this, inputModel, "extn_curbsidePickupOrdersCount", null);					
					}	
					/*OMNI-89343 - End */
				}
			}
		}
   	}//OMNI-106494 (Bracket Misplaced)
  /*END : OMNI-101859,OMNI-102131,OMNI-85177 */
			
			
          
			/*OMNI-72474 Store Working Hours for OMW - START */
			if(_scBaseUtils.equals(mashupRefId, "extn_StoreWorkingHours")){
				var extn_StoreWorkingHours_output = _scScreenUtils.getModel(this, "modelOutput", null);
				var storeHours = _scModelUtils.getStringValueFromPath("CommonCodeList.CommonCode", modelOutput);
				
				var storeLoginTime = _scModelUtils.getModelFromListByValue(storeHours, "CodeValue", "StoreLoginTime");
				storeLogin = _scModelUtils.getStringValueFromPath("CodeShortDescription", storeLoginTime);
				
				var storeLogoffTime = _scModelUtils.getModelFromListByValue(storeHours, "CodeValue", "StoreLogoffTime");
				storeLogoff = _scModelUtils.getStringValueFromPath("CodeShortDescription", storeLogoffTime);
				_iasContextUtils.addToContext("StoreLoginTime", storeLogin);				
				_iasContextUtils.addToContext("StoreLogoffTime", storeLogoff);
				
			}
			/*OMNI-72474 Store Working Hours for OMW - END */
			// OMNI-83367 start
			if(_scBaseUtils.equals(mashupRefId, "extn_CurbsideExtensions_ref")){
				var commoncodes = _scModelUtils.getStringValueFromPath("CommonCodeList.CommonCode", modelOutput);				
                var curbsideDelayMaxCounter=null;
                var curbsideExtensionsAllowed=null;
				var curbsideDefaultMins=null; //OMNI- 79883
				var sms1 =null; //OMNI-82169
				var sms2 =null;	 
				var sms3 = null;
				for(var i in commoncodes)
				{
					var codeValue = _scModelUtils.getStringValueFromPath("CodeValue", commoncodes[i]);
					if(_scBaseUtils.equals(codeValue,"MAX_EXTENSIONS_ALLOWED")){
						curbsideDelayMaxCounter=_scModelUtils.getStringValueFromPath("CodeShortDescription", commoncodes[i]);
					} 
					if(_scBaseUtils.equals(codeValue,"EXTENSIONS_ALLOWED")){						
						curbsideExtensionsAllowed=_scModelUtils.getStringValueFromPath("CodeShortDescription", commoncodes[i]);
					}
					if(_scBaseUtils.equals(codeValue,"DEFAULT_MINS")){    //OMNI-79883
                        curbsideDefaultMins=_scModelUtils.getStringValueFromPath("CodeShortDescription", commoncodes[i]);
                    }
					if(_scBaseUtils.equals(codeValue,"DEFAULT_INSTORE_MINS")){    //OMNI-105502
                        instoreDefaultMins=_scModelUtils.getStringValueFromPath("CodeShortDescription", commoncodes[i]);
                    }
					 //OMNI-82169- Start
					if(_scBaseUtils.equals(codeValue,"SMS_MSG1")){	
						sms1=_scModelUtils.getStringValueFromPath("CodeLongDescription", commoncodes[i]);
					}
					if(_scBaseUtils.equals(codeValue,"SMS_MSG2")){	
						sms2=_scModelUtils.getStringValueFromPath("CodeLongDescription", commoncodes[i]);
					}
					if(_scBaseUtils.equals(codeValue,"SMS_MSG3")){	
						sms3=_scModelUtils.getStringValueFromPath("CodeLongDescription", commoncodes[i]);
					}
					//OMNI-82169- End
				}
				_iasContextUtils.addToContext("CurbsideDelayMaxCounter",curbsideDelayMaxCounter);
                _iasContextUtils.addToContext("CurbsideExtensionsAllowed",curbsideExtensionsAllowed);
				_iasContextUtils.addToContext("CurbsideDefaultMins",curbsideDefaultMins); //OMNI-79883
				_iasContextUtils.addToContext("InstoreDefaultMins",instoreDefaultMins);//OMNI-105502
				_iasContextUtils.addToContext("sms1", sms1); //OMNI-82169
				 _iasContextUtils.addToContext("sms2", sms2); //OMNI-82169
				 _iasContextUtils.addToContext("sms3", sms3);
			}
			//OMNI-83367 End
        },
		  
		/* This method is to handle the batch count widget source namespace display */
		extn_batchCountBindingFunc: function(
        dataValue, screen, widget, nameSpace, shipmentModel) {
            if (
            _scBaseUtils.equals("0", dataValue)) {
                _scWidgetUtils.addClass(
                this, "pnlBatchPick", "zeroCount");
            }
            return dataValue;
        },
				
		/* Start: OMNI-5402 Curbside Pickup Orders */
		extn_curbCountBindingFunc: function(
        dataValue, screen, widget, nameSpace, shipmentModel) {
            if (
            _scBaseUtils.equals("0", dataValue)) {
                _scWidgetUtils.addClass(
                this, "extn_pnlCurbsidePickupOrders", "zeroCount");
            }
            return dataValue;
        },
		/* End: OMNI-5402 Curbside Pickup Orders */
		/* Start: OMNI-71303 On My Way Orders */
		extn_OnMyWayFunction: function(
        dataValue, screen, widget, nameSpace, shipmentModel) {
            if (
            _scBaseUtils.equals("0", dataValue)) {
                _scWidgetUtils.addClass(
                this, "extn_OnMyWayMainPane", "zeroCount");
            }
            return dataValue;
        },
		/* End: OMNI-71303 On My Way Orders */
		/* Start: OMNI-105498 In Store Pickup Orders */
		extn_InStorePickupFunction: function(
        dataValue, screen, widget, nameSpace, shipmentModel) {
            if (
            _scBaseUtils.equals("0", dataValue)) {
                _scWidgetUtils.addClass(
                this, "extn_InStorePickupCount", "zeroCount");
            }
            return dataValue;
        },
		/* End: OMNI-105498 In Store Pickup Orders */
		/*Start: OMNI-6579 STS Receive Containers */
		STSReceviveContainers: function(
        dataValue, screen, widget, nameSpace, shipmentModel) {
            if (
            _scBaseUtils.equals("0", dataValue)) {
                _scWidgetUtils.addClass(
                this, "extn_STSReceiveContainersCount", "zeroCount");
            }
            return dataValue;
        },
		/* End: OMNI-6579 STS Receive Containers */
		
		/*Start: OMNI-6580 : STS Stage Containers */
		STSReadyToStage: function(
        dataValue, screen, widget, nameSpace, shipmentModel) {
            if (
            _scBaseUtils.equals("0", dataValue)) {
                _scWidgetUtils.addClass(
                this, "extn_STSStageContainersCount", "zeroCount");
            }
            return dataValue;
        },

   // ***START  OMNI-102102 OMNI-102286
     STSDeliveredContainers: function(
        dataValue, screen, widget, nameSpace, shipmentModel) {
            if (
            _scBaseUtils.equals("0", dataValue)) {
                _scWidgetUtils.addClass(
                this, "extn_STSDeliveredCount", "zeroCount");
            }
            return dataValue;
        },

      STSIntransitContainers: function(
        dataValue, screen, widget, nameSpace, shipmentModel) {
            if (
            _scBaseUtils.equals("0", dataValue)) {
                _scWidgetUtils.addClass(
                this, "extn_STSIntransitCount", "zeroCount");
            }
            return dataValue;
        },    
    
    // *** END  OMNI-102102 OMNI-102286
   
     /* End: OMNI-6580 : STS Stage Containers */
	/* 	OMNI-6624 - Mobile Store UI - New Search screen customization for STS - START */
 extn_openSTSOrderSearch: function(event, bEvent, ctrl, args){
 // OMNI - 8777 - Session object is retaining in the Ship to store order search screen in the scenario which is mentioned in the steps - START
	var clearSessionObject = _scBaseUtils.getNewModelInstance();
	_iasContextUtils.addToContext("SearchCriteria", clearSessionObject);
 // OMNI - 8777 - Session object is retaining in the Ship to store order search screen in the scenario which is mentioned in the steps - END
	_wscMobileHomeUtils.openScreen("extn.mobile.home.STSOrderSearch.OrderSearch.STSOrderSearchScreen", "extn.mobile.editors.ReceiveContainerEditor");
		/* OMNI-6624 - Mobile Store UI - New Search screen customization for STS - END*/	
     },
	     
     	//OMNI-66978 START
	 	 extn_OpenAuditStagingStartScreen: function(event, bEvent, ctrl, args){
		var clearSessionObject = _scBaseUtils.getNewModelInstance();
		_iasContextUtils.addToContext("SearchCriteria", clearSessionObject);
		_iasContextUtils.addToContext("LoadedFromShipmentScanScreen", clearSessionObject);
		_wscMobileHomeUtils.openScreen("extn.mobile.home.AuditStagedShipment.AuditHomeScreen.AuditShipmentHomeScreen", "extn.mobile.editors.ReceiveContainerEditor");
	 },
	//OMNI-66978 End 
	//OMNI-82213 START
	extn_MissedShipmentStartScreen: function() {
	// OMNI-81388 START 
    var shipmentSearchCriteriaModel="";
	var sCurrentStore = _iasContextUtils.getFromContext("CurrentStore");
    shipmentSearchCriteriaModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
	_scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", sCurrentStore,shipmentSearchCriteriaModel);
    _scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod", 'PICK',shipmentSearchCriteriaModel);
	_scModelUtils.setStringValueAtModelPath("Shipment.Status",'1100.70.06.30.5',shipmentSearchCriteriaModel);
	_scModelUtils.setStringValueAtModelPath("Shipment.OrderByAttribute",'ShipmentNo',shipmentSearchCriteriaModel);	
	_scModelUtils.setStringValueAtModelPath("Shipment.MissedScanFeatureEnabled", 'Y',shipmentSearchCriteriaModel);	
    _wscMobileHomeUtils.openScreenWithInputData("wsc.mobile.home.search.SearchResult", shipmentSearchCriteriaModel, "wsc.mobile.editors.MobileEditor"); 
	// OMNI-81388 END
	}
	//OMNI-82213 END
});
});
