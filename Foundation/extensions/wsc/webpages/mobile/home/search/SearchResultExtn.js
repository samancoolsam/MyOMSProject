
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/mobile/home/search/SearchResultExtnUI","scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!ias/utils/ScreenUtils","scbase/loader!ias/utils/ContextUtils","scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!wsc/mobile/home/utils/MobileHomeUtils","scbase/loader!sc/plat/dojo/utils/ScreenUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnSearchResultExtnUI
			,	
				_scModelUtils
			,
				_scBaseUtils
			,	
				_iasScreenUtils
			, 	
				_iasContextUtils
			,
				_scWidgetUtils
            ,
               _wscMobileHomeUtils
			, 	
			_scScreenUtils
){ 
	return _dojodeclare("extn.mobile.home.search.SearchResultExtn", [_extnSearchResultExtnUI],{
	// custom code here
	
	// Start - OMNI- 5402
	getIdentifierRepeatingScreenData: function(
        shipmentModel, screen, widget, namespace, modelObject) {
			
			var sloadIdentifier= _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted", screen.scEditorInput);
			var sInstoreloadIdentifier= _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsInstorePickupOpted", screen.scEditorInput);//OMNI-105499
			var sonMyWayIdentifier= _scModelUtils.getStringValueFromPath("Shipment.IsOnMyWayOrder", screen.scEditorInput);
			var sCurbsidePickupIdentifier= _scModelUtils.getStringValueFromPath("Shipment.IsCurbsidePickupOrder", screen.scEditorInput);
			var sInstorePickupIdentifier= _scModelUtils.getStringValueFromPath("Shipment.IsInStorePickupOrder", screen.scEditorInput);//OMNI-105499
			// START OMNI-81813
            var missedScanFeatureEnabled = _scModelUtils.getStringValueFromPath("Shipments.MissedScanFeatureEnabled", modelObject);
			// END OMNI-81813
			var repeatingScreenId = "wsc.mobile.common.screens.shipment.picking.ShipmentPickDetails";
            var returnValue = null;
            var identifierId = "Ship";
            var shipmentStatus = null;
            var deliveryMethod = null;
            shipmentStatus = _scModelUtils.getStringValueFromPath("Status.Status", shipmentModel);
            deliveryMethod = _scModelUtils.getStringValueFromPath("DeliveryMethod", shipmentModel);
			// START OMNI-81813 handles non MSL
			if (! _scBaseUtils.equals(missedScanFeatureEnabled, "Y")){
             if (
            _scBaseUtils.contains(shipmentStatus, "1100.70.06.50")) {
                identifierId = "Pack";
            } else if (
            _scBaseUtils.contains(shipmentStatus, "1100.70.06.70")) {
                identifierId = "Pack";
            }
			// Start - OMNI- 5402 Validation for curbside identifier to display the status description
			else if(!_scBaseUtils.isVoid(sloadIdentifier) && (_scBaseUtils.equals(sloadIdentifier, "Y"))){
					//OMNI-80192 - Start
					var sloadIdentifierQryType= _scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOptedQryType", screen.scEditorInput);
                    if(!_scBaseUtils.isVoid(sloadIdentifierQryType) && _scBaseUtils.equals(sloadIdentifierQryType, "NE")) {
                        identifierId = "Pick";
                    }
                    else if(_scBaseUtils.contains(shipmentStatus, "1100.70.06.30.5")) {
						identifierId = "Pick";
						_scModelUtils.setStringValueAtModelPath("Status.Description", "Waiting for Curbside Pick Up", shipmentModel); 
					}
					//OMNI-80192 - End
			}
			//OMNI-105499 - Start
			else if(!_scBaseUtils.isVoid(sInstoreloadIdentifier) && (_scBaseUtils.equals(sInstoreloadIdentifier, "Y"))){
					if(_scBaseUtils.contains(shipmentStatus, "1100.70.06.30.5")) {
						identifierId = "Pick";
						_scModelUtils.setStringValueAtModelPath("Status.Description", "Waiting for In Store Pick Up", shipmentModel); 
					}
			}
			//OMNI-105499 - End
			else if(_scBaseUtils.contains(shipmentStatus, "1100.70.06.30.5") || _scBaseUtils.equals(shipmentStatus, "1100.70.06.10.5")) { 
				identifierId = "Pick";
			}						
			//End - OMNI- 5402
			//Start- OMNI-72008
		if(!_scBaseUtils.isVoid(sonMyWayIdentifier)){
				if (_scBaseUtils.equals(sonMyWayIdentifier, "Y")){
					_scModelUtils.setStringValueAtModelPath("IsOnMyWayFlag", "Y", shipmentModel);
					}
			}
		//End - OMNI- 72008
		//Start OMNI-75388
		if(!_scBaseUtils.isVoid(sCurbsidePickupIdentifier) && (_scBaseUtils.equals(sCurbsidePickupIdentifier, "Y"))){
			_scModelUtils.setStringValueAtModelPath("IsCurbsidePickupFlag", "Y", shipmentModel);
			}
		//End OMNI-75388
		//Start- OMNI-105499
		if(!_scBaseUtils.isVoid(sInstorePickupIdentifier)){
				if (_scBaseUtils.equals(sInstorePickupIdentifier, "Y")){
					_scModelUtils.setStringValueAtModelPath("IsInstorePickupFlag", "Y", shipmentModel);
					}
			}
		//End - OMNI- 105499
			// Start: OMNI-6585
			var DocumentType = _scModelUtils.getStringValueFromPath("DocumentType", shipmentModel);
			var ShipmentLines = _scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine", shipmentModel);
			var shipmentType = _scModelUtils.getStringValueFromPath("ShipmentType", shipmentModel);		
	
			if(!_scBaseUtils.isVoid(ShipmentLines)) {
				var shipmentLine = ShipmentLines[0];
				var fulfillmentType = _scModelUtils.getStringValueFromPath("OrderLine.FulfillmentType", shipmentLine);
			}
			if (_scBaseUtils.equals(fulfillmentType, "STS") || _scBaseUtils.equals(DocumentType, "0006")){
				if(_scBaseUtils.equals(shipmentType, "STS")) {
					if(_scModelUtils.hasAttributeInModelPath("Containers.Container", shipmentModel)){
						var sTotalContainers = _scModelUtils.getStringValueFromPath("Containers.TotalNumberOfRecords", shipmentModel);
						_scModelUtils.setStringValueAtModelPath("TotalContainers", sTotalContainers, shipmentModel);
					}else {
						var sTotalLines = _scModelUtils.getStringValueFromPath("TotalLines", shipmentModel);
						var vTotalContainers = _scModelUtils.getNumberValueFromPath("Containers.TotalNumberOfRecords", shipmentModel);
						if(vTotalContainers>0)
						{
							var totalContainers = _scModelUtils.getStringValueFromPath("Containers.TotalNumberOfRecords", shipmentModel);
							_scModelUtils.setStringValueAtModelPath("TotalContainers", totalContainers, shipmentModel);
						}
						else{
							_scModelUtils.setStringValueAtModelPath("TotalContainers", vTotalContainers.toString(), shipmentModel);
						}

					}
					identifierId = "Pick";
				} 
			}
			// End: OMNI-6585
		 // START OMNI-81813 else handles MSL
		 } else if (_scBaseUtils.equals(missedScanFeatureEnabled, "Y") && 
		   ( _scBaseUtils.contains(shipmentStatus, "1100.70.06.30.7") || _scBaseUtils.contains(shipmentStatus, "1100.70.06.30.5")) ){
				identifierId = "Pick";
		 }
		 // END  OMNI-81813
            var additionalParamsBean = null;
            additionalParamsBean = {};
            additionalParamsBean["identifierId"] = identifierId;
            var namespaceMapBean = null;
            namespaceMapBean = {};
            namespaceMapBean["parentnamespace"] = "getShipmentList_output";
            namespaceMapBean["childnamespace"] = "Shipment";
            namespaceMapBean["parentpath"] = "Shipments";
            namespaceMapBean["childpath"] = "Shipment";
            returnValue = _iasScreenUtils.getRepeatingScreenData(
            repeatingScreenId, namespaceMapBean, additionalParamsBean);
            return returnValue;
        },
		// End - OMNI- 5402
		//OMNI-6624 - Mobile Store UI - New Search screen customization for STS - START
		returnToOrderSearch: function(event, bEvent, ctrl, args) {
			var searchCriteria = _iasContextUtils.getFromContext("SearchCriteria");
			var searchType = _scModelUtils.getStringValueFromPath("Shipment.SearchType",searchCriteria);
			if(_scBaseUtils.equals(searchType,"STSOrderSearch")){
				_wscMobileHomeUtils.openScreenWithInputData("extn.mobile.home.STSOrderSearch.OrderSearch.STSOrderSearchScreen", searchCriteria , "extn.mobile.editors.ReceiveContainerEditor");
			}else{
				_wscMobileHomeUtils.openScreenWithInputData("wsc.mobile.home.search.Search", searchCriteria, "wsc.mobile.editors.MobileEditor");
			}
        },
        	//OMNI-6624 - Mobile Store UI - New Search screen customization for STS - END
			 // OMNI-69635 MSL Change to show NO Missed Shipment --START
	initializeScreen: function(event, bEvent, ctrl, args) {
       var screen = this;
       var shipmentModel = null;
           shipmentModel = _scScreenUtils.getModel(screen, "getShipmentList_output");
       var missedShipmentEnabled= _scModelUtils.getStringValueFromPath("Page.Output.Shipments.MissedScanFeatureEnabled", shipmentModel);
       var endDate = _scModelUtils.getStringValueFromPath("Page.Output.Shipments.EndDate", shipmentModel); 
	   var noOfRecords= _scModelUtils.getStringValueFromPath("Page.Output.Shipments.TotalNumberOfRecords", shipmentModel);
           if(!_scBaseUtils.isVoid(missedShipmentEnabled) && _scBaseUtils.equals(missedShipmentEnabled, 'Y')){
	
			_scWidgetUtils.hideWidget(this,"lbl_ScreenTitle",true);// Change to hide searchOrder screen title in case of missed shipment
			_scWidgetUtils.hideWidget(this,"backToSearchLink",true);// Change to hide back to search link in case of missed shipment
		//OMNI-82408- Setting value for label 
			if (!_scBaseUtils.isVoid(endDate)){
				
    	_scWidgetUtils.setValue(this, "extn_label_missedshipment_batchdate","Audit Staging Date: "+endDate, false);
				_scWidgetUtils.showWidget(this,"extn_label_missedshipment_batchdate");	
				
			}
			//OMNI-82408 - Show Audit Staging End Date -END

			//OMNI-69635 changes only specific to no Missed shipment START

			if ( _scBaseUtils.equals(noOfRecords, "0")){
				
				_scWidgetUtils.hideWidget(this, "repeatingIdentifierScreen", true);
				_scWidgetUtils.showWidget(this, "extn_lblNoMissedShipment", true);
				_scWidgetUtils.hideWidget(this,"extn_label_missedshipment_batchdate", true);
		
			}
	//OMNI-69635 changes only specific to no Missed shipment END
}
        
}

//// OMNI-69635 MSL Change -- END
	
});
});

