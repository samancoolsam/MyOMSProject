
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/order/details/OrderSummaryLinesExtnUI","scbase/loader!sc/plat/dojo/utils/BaseUtils",
"scbase/loader!sc/plat/dojo/utils/EventUtils","scbase/loader!isccs/utils/UIUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!isccs/utils/OrderUtils","scbase/loader!sc/plat/dojo/utils/EventUtils","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnOrderSummaryLinesExtnUI,scBaseUtils,scEventUtils,isccsUIUtils,scModelUtils,isccsOrderUtils,_scEventUtils,_scScreenUtils,_scModelUtils,_scBaseUtils
){ 
	return _dojodeclare("extn.order.details.OrderSummaryLinesExtn", [_extnOrderSummaryLinesExtnUI],{
	// custom code here 

		//Start - OMNI-63467 - Customer Care - Updates to WCC Homepage
       getShipmentNo:function(gridReference, rowIndex, columnIndex, gridRowJSON, unformattedValue) {
                var orderDisplayModel = null;
                orderDisplayModel = _scScreenUtils.getModel(this, "getCompleteOrderLineList_output");
                var orderLine = _scModelUtils.getStringValueFromPath("Page.Output.OrderLineList.OrderLine", orderDisplayModel);
                var minLineStatus = _scModelUtils.getStringValueFromPath("MinLineStatus", orderLine[rowIndex]);
                var shipmentNumbers = "";
                if (!_scBaseUtils.equals(minLineStatus, "9000")) {
                	var shipmentLines = _scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine", orderLine[rowIndex]);
                	for (var i in shipmentLines) {
	                    var documentType = _scModelUtils.getStringValueFromPath("DocumentType", shipmentLines[i].Shipment[0]);
	                    //OMNI-65872 - Show Shipment for DSV Order
	                    if (_scBaseUtils.equals(documentType, "0001") || _scBaseUtils.equals(documentType, "0005")) {
	                        var shipmentNo = _scModelUtils.getStringValueFromPath("ShipmentNo", shipmentLines[i].Shipment[0]);
	                        shipmentNumbers = shipmentNumbers + "\n" +shipmentNo ;
	                    }
                	}
                }
                return shipmentNumbers;
            },
			
		getShipNode:function(gridReference, rowIndex, columnIndex, gridRowJSON, unformattedValue) {
				var orderDisplayModel = null;
                orderDisplayModel = _scScreenUtils.getModel(this, "getCompleteOrderLineList_output");
                var orderLine = _scModelUtils.getStringValueFromPath("Page.Output.OrderLineList.OrderLine", orderDisplayModel);
                var shipNodes = "";
                var minLineStatus = _scModelUtils.getStringValueFromPath("MinLineStatus", orderLine[rowIndex]);
                if (!_scBaseUtils.equals(minLineStatus, "9000")) {
                	var shipmentLines = _scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine", orderLine[rowIndex]);
	                for (var i in shipmentLines) {
	                    var documentType = _scModelUtils.getStringValueFromPath("DocumentType", shipmentLines[i].Shipment[0]);
	                    //OMNI-65872 - Show Ship Node for DSV Order
	                    if (_scBaseUtils.equals(documentType, "0001") || _scBaseUtils.equals(documentType, "0005")) {
	                        var shipNode = _scModelUtils.getStringValueFromPath("ShipNode", shipmentLines[i].Shipment[0]);
	                        shipNodes = shipNodes + "\n" + shipNode;
	                    }
	                }
                }
                return shipNodes;
			},
            //End - OMNI-63467 - Customer Care - Updates to WCC Homepage      
		getCarrierService:function(gridReference,rowIndex,columnIndex,gridRowJSON,unformattedValue){								
					var orderDisplayModel = null;
            				orderDisplayModel = _scScreenUtils.getModel(this, "getCompleteOrderLineList_output");
            				var orderLine = _scModelUtils.getStringValueFromPath("Page.Output.OrderLineList.OrderLine", orderDisplayModel);
             				var isSignatureRequired= _scModelUtils.getStringValueFromPath("Extn.ExtnIsSignatureRequired", orderLine[rowIndex]);
                				if(!_scBaseUtils.isVoid(isSignatureRequired) && _scBaseUtils.equals(isSignatureRequired,"Y")) {
                    					return unformattedValue+" - Sig. service req";
                 					 }
                					else{
                    					return unformattedValue;
                					}
			},
				
		extn_cellClick : function(event, bEvent, ctrl, args){
			var cellJson = scBaseUtils.getAttributeValue("cellJson",false,args);
			var screen = scEventUtils.getScreenFromEventArguments(args);
			//setting additional data  for link image fields
			var cellJsonData = scBaseUtils.getAttributeValue("cellJsonData",false,args);
			var itemData = scBaseUtils.getAttributeValue("item",false,args);
			if (!scBaseUtils.isVoid(itemData) && scBaseUtils.isVoid(cellJsonData))
				scBaseUtils.setAttributeValue("cellJsonData", itemData, args);
				var uniqueRowId = scBaseUtils.getAttributeValue("uniqueRowId",false,args);
				var rowIndex = scBaseUtils.getAttributeValue("rowIndex",false,args);
				if (!scBaseUtils.isVoid(rowIndex) && scBaseUtils.isVoid(uniqueRowId))
					scBaseUtils.setAttributeValue("uniqueRowId", rowIndex, args);
					
				if (!(scBaseUtils.isVoid(cellJson))){
					var item = scBaseUtils.getAttributeValue("item",false,args);
					if (scBaseUtils.equals("OriginalOrderNo", scBaseUtils.getAttributeValue("colField",false,cellJson))){
						var eDerivedOrder = scModelUtils.createNewModelObjectWithRootKey("Order");
						scModelUtils.addModelToModelPath("Order",scModelUtils.getModelObjectFromPath("DerivedFromOrder", item), eDerivedOrder);
						isccsOrderUtils.openOrder(screen, eDerivedOrder);
						} 
					else if (scBaseUtils.equals("ExpectedOn", scBaseUtils.getAttributeValue("colField",false,cellJson))){
						if(!args.linkUId){
						var eOrder = scModelUtils.createNewModelObjectWithRootKey("Order");
						scModelUtils.addModelToModelPath("Order", item, eOrder);
						isccsUIUtils.openWizardInEditor("isccs.shipment.wizards.shipmentTracking.ShipmentTrackingWizard", eOrder, "isccs.editors.OrderEditor", screen);
						}else{
						if(scBaseUtils.equals("ExpectedOn", args.linkUId)){
							this.extn_openExpectedOn(event, bEvent, ctrl, args);
						} else if(scBaseUtils.equals("TrackingNo", args.linkUId)){
							this.extn_openTrackingNo(event, bEvent, ctrl, args);							
						} else if(scBaseUtils.equals("BundleUnitPtice", args.linkUId)){
							screen.openBundlePopup(event, bEvent, ctrl, args);
						}
						}
						} else if (scBaseUtils.equals("TrackingNo", scBaseUtils.getAttributeValue("colField", false, cellJson)))
						{
							this.openTrackingNo(screen, item);
						} else if (scBaseUtils.equals("DeleteOrderLine", scBaseUtils.getAttributeValue("colField", false, cellJson)) || 
							scBaseUtils.equals("Delete", scBaseUtils.getAttributeValue("colField", false, cellJson)) ){
							screen.OLL_handleCancel(event, bEvent, ctrl, args);
						} else if(scBaseUtils.equals("UnitPrice", scBaseUtils.getAttributeValue("colField", false, cellJson))){
							screen.openBundlePopup(event, bEvent, ctrl, args);
						} else if(scBaseUtils.equals("InvoiceDisplayDescription", scBaseUtils.getAttributeValue("colField", false, cellJson))){
							screen.openInvoiceList(event, bEvent, ctrl, args);
						} else if(scBaseUtils.equals("MultiPriceMatchReason", scBaseUtils.getAttributeValue("colField", false, cellJson))){
							screen.askToOverride(event, bEvent, ctrl, args);
						} else {
							screen.LST_DoubleClickHandler(event, bEvent, ctrl, args);
						}
					} else {
						screen.LST_DoubleClickHandler(event, bEvent, ctrl, args);
					}
				},
		extn_openTrackingNo : function(event, bEvent, ctrl, args) {
			var cellJson = scBaseUtils.getAttributeValue("cellJsonData",false,args);
			if (cellJson._dataItem) {
				cellJson = cellJson._dataItem;
				}
			if(!cellJson){
				cellJson = scBaseUtils.getAttributeValue("item",false,args);
				}
			if(cellJson) {
				var screen = scEventUtils.getScreenFromEventArguments(args);
				
				//var Url = cellJson.TrackingInfoList.TrackingInfo[0].TrackingUrl;
				//OMNI 12937 -This method is commented to skip the navigation from Order summery screen to shipment tracking screen. 
				//this.openTrackingNo(screen, cellJson);
				}
			},
		//OMNI 12937 -This method is commented to skip the navigation from Order summery screen to shipment tracking screen. 
		/*openTrackingNo : function(screen, item) {
			if (item.TrackingInfoList && item.TrackingInfoList.TrackingInfo){
				var length = item.TrackingInfoList.TrackingInfo.length;
				if (length == 1){					
					var eOrder = scModelUtils.createNewModelObjectWithRootKey("Order");
					scModelUtils.addModelToModelPath("Order", item, eOrder);
					//isccsUIUtils.openWizardInEditor("isccs.shipment.wizards.shipmentTracking.ShipmentTrackingWizard", eOrder, "isccs.editors.OrderEditor", screen);
						//this.openTrackingUrl: function(event,bEventr,ctrl,args);
					} else if (length > 1) {
						var eOrder = scModelUtils.createNewModelObjectWithRootKey("Order");
						scModelUtils.addModelToModelPath("Order", item, eOrder);
						//isccsUIUtils.openWizardInEditor("isccs.shipment.wizards.shipmentTracking.ShipmentTrackingWizard", eOrder, "isccs.editors.OrderEditor", screen);
					}
				}
			},*/
		extn_openExpectedOn : function(event, bEvent, ctrl, args) {
			var cellJson = scBaseUtils.getAttributeValue("cellJsonData",false,args);
				if (cellJson._dataItem) {
					cellJson = cellJson._dataItem;
					}
				if(!cellJson){
					cellJson = scBaseUtils.getAttributeValue("item",false,args);
					}
					if(cellJson) {
						var screen = scEventUtils.getScreenFromEventArguments(args);
						var eOrder = scModelUtils.createNewModelObjectWithRootKey("Order");
						scModelUtils.addModelToModelPath("Order", cellJson, eOrder);
						isccsUIUtils.openWizardInEditor("isccs.shipment.wizards.shipmentTracking.ShipmentTrackingWizard", eOrder, "isccs.editors.OrderEditor", screen);
					}
			},
      //OMNI-30154-WCC: Display Save The Sale -Start
			getSaveTheSale : function(gridReference,rowIndex,columnIndex,gridRowJSON,unformattedValue) {
          			 var orderDisplayModel = null;
            				orderDisplayModel = _scScreenUtils.getModel(this, "getCompleteOrderLineList_output");
            				var alternateStore = _scModelUtils.getStringValueFromPath("Page.Output.OrderLineList.Order.Extn.ExtnASShipNode", orderDisplayModel);
            				var orderLine = _scModelUtils.getStringValueFromPath("Page.Output.OrderLineList.OrderLine", orderDisplayModel);
             				var saveTheSale= _scModelUtils.getStringValueFromPath("Extn.ExtnOriginalFulfillmentType", orderLine[rowIndex]);
             				 var alternateStoreFlag= _scModelUtils.getStringValueFromPath("Extn.ExtnIsASP", orderLine[rowIndex]);
             				 //Start OMNI-105580 save the sale for alternate store
                				if((!_scBaseUtils.isVoid(saveTheSale) && _scBaseUtils.equals(saveTheSale,"BOPIS"))|| 
				               (!_scBaseUtils.isVoid(alternateStore) && !_scBaseUtils.isVoid(alternateStoreFlag) && _scBaseUtils.equals(alternateStoreFlag,"Y"))) {
                    					return "Y";
                 					 }
                					else{
                    					return "N";
                					}
           		},
           //Start OMNI-105580 save the sale for alternate store

      //OMNI-30154-WCC: Display Save The Sale -End    

      //OMNI-105580 -: Display Alternate Store -Start
      getAlternateStore : function(gridReference,rowIndex,columnIndex,gridRowJSON,unformattedValue) {
          	var orderDisplayModel = _scScreenUtils.getModel(this, "getCompleteOrderLineList_output");
			var retAlternateStore=null;
	        var alternateStore = _scModelUtils.getStringValueFromPath("Page.Output.OrderLineList.Order.Extn.ExtnASShipNode", orderDisplayModel);
	        var OrderLine=_scModelUtils.getStringValueFromPath("Page.Output.OrderLineList.OrderLine",orderDisplayModel); 
            var alternateStoreFlag= _scModelUtils.getStringValueFromPath("Extn.ExtnIsASP", OrderLine[rowIndex]);
            if(!_scBaseUtils.isVoid(alternateStore) && !_scBaseUtils.isVoid(alternateStoreFlag) && _scBaseUtils.equals(alternateStoreFlag,"Y")) {
			  retAlternateStore=alternateStore;
                          return retAlternateStore;
                 }else{
                    	return retAlternateStore;
                	}
                	 }

              //OMNI-105580 -: Display Alternate Store -End




});
});

