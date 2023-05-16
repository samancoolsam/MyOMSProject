
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/batch/batchpick/scanlist/BatchLineDetailsExtnUI","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!ias/utils/UIUtils","scbase/loader!ias/utils/BaseTemplateUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!ias/utils/ContextUtils","scbase/loader!sc/plat/dojo/utils/EventUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnBatchLineDetailsExtnUI
			,
				_scScreenUtils
			,
				_scModelUtils
			,
				_iasUIUtils
			,
				_iasBaseTemplateUtils
			,
				_scBaseUtils
			,
				_scWidgetUtils
			,
				_iasContextUtils
			,
				_scEventUtils
				
){ 
	return _dojodeclare("extn.components.batch.batchpick.scanlist.BatchLineDetailsExtn", [_extnBatchLineDetailsExtnUI],{
	// custom code here
	
	/* This method is called in the source binding of item details widget to display the item properties */
	extnGetItemDetails : function(dataValue, screen, widget, namespace, modelObj, options)
	{
		var extnBatchLineModel = _scScreenUtils.getModel(this, "extn_BatchLine");
		var itemDetails = null;
		var itemSize = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.Extn.ExtnSizeCodeDescription",extnBatchLineModel);
		var itemStyle = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.Extn.ExtnStyle",extnBatchLineModel);
		var itemColor = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.Extn.ExtnVendorColorName",extnBatchLineModel);
		
		if( _scBaseUtils.isVoid(itemSize) && _scBaseUtils.isVoid(itemStyle) && _scBaseUtils.isVoid(itemColor) )
		{
			itemDetails = "";
		}
		else
		{
			itemDetails = itemSize + " | " + itemStyle + " | " + itemColor;
		}
		return itemDetails;
	},
	
	/* This method is called in the source binding of item planogram widget to display the item planogram properties */
	extnGetPlanogramDetails : function(dataValue, screen, widget, namespace, modelObj, options)
	{
		var extnBatchLineModel = _scScreenUtils.getModel(this, "extn_BatchLine");
		var extnPlanogramDetails = null;
		var extnDept = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.Extn.ExtnDepartmentPlano",extnBatchLineModel);
		var extnPogId = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.Extn.ExtnPogId",extnBatchLineModel);
		var extnPogNumber = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.Extn.ExtnPogNumber",extnBatchLineModel);
		var extnSection = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.Extn.ExtnSection",extnBatchLineModel);
		
		if( _scBaseUtils.isVoid(extnDept) && _scBaseUtils.isVoid(extnPogId) && _scBaseUtils.isVoid(extnPogNumber) && _scBaseUtils.isVoid(extnSection) )
		{
			extnPlanogramDetails = "";
		}
		else
		{
			extnPlanogramDetails = extnPogId + " | " + extnDept + " | " + extnSection + " | " + extnPogNumber;
		}
		return extnPlanogramDetails;
	},
	
	/* This method is called in the source binding of item Onhand widget to display the item stock on hand details */
	extnGetItemOnhandStock : function(dataValue, screen, widget, namespace, modelObj, options)
	{
		var extnBatchLineModel = _scScreenUtils.getModel(this, "extn_BatchLine");
		var extnOnhandStock = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.OnhandInventory",extnBatchLineModel);
		extnOnhandStock = "Total Stock On Hand: " + " " + extnOnhandStock;
		return extnOnhandStock;
	},
	
	/* This method is called in the source binding of item LiveDate widget to display the item LiveDate from the Planogram Details obtained */
	extnGetItemLiveDate: function(dataValue, screen, widget, namespace, modelObj, options)
	{
		var extnBatchLineModel = _scScreenUtils.getModel(this, "extn_BatchLine");
		var extnLiveDate = null;
		extnLiveDate = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.Extn.ExtnLiveDate",extnBatchLineModel);
		
		if( _scBaseUtils.isVoid(extnLiveDate))
		{
			extnLiveDate = "";
		}
		else
		{
			extnLiveDate = extnLiveDate.substring(0,10);
			extnLiveDate = "Live Date: " + " " + extnLiveDate;
		}
		return extnLiveDate;
	},

	/* This method has been overridden to send the shortage quantity to the popup output model */
	onShortageReasonSelection : function(actionPerformed,
								model, popupParams) 
	{
		if (!(_scBaseUtils.equals(actionPerformed, "CLOSE"))) {
			var shortedShipmentLineModel = _scScreenUtils
					.getModel(this, "ShortedBatchLineModel");
			console.log("shortedShipmentLineModel : ",
					shortedShipmentLineModel);
			var shipmentLine = _scScreenUtils
					.getModel(this, "ShipmentLine");

			var shortedBatchLineModel = _scScreenUtils
					.getTargetModel(this,
							"BatchLineForRecordShortage",
							null);
			_scModelUtils
					.setStringValueAtModelPath(
							"StoreBatch.Item.ShortageReason",
							_scModelUtils
									.getStringValueFromPath(
											"StoreBatchLine.ShortageResolutionReason",
											shortedShipmentLineModel),
							shortedBatchLineModel);
			/* ShortageQty is being sent in the output model */
			_scModelUtils
					.setStringValueAtModelPath(
							"StoreBatch.Item.ShortageQty",
							_scModelUtils
									.getStringValueFromPath(
											"StoreBatchLine.DisplayShortQty",
											shortedShipmentLineModel),
							shortedBatchLineModel);

			var eventBean = {};
			_scBaseUtils.setAttributeValue("argumentList",
					{}, eventBean);
			_scBaseUtils.setAttributeValue(
					"argumentList.BatchLineShortedModel",
					shortedBatchLineModel, eventBean);
			_scEventUtils
					.fireEventToParent(this,
							"updateShortageForBatchLine",
							eventBean);
		}
	},
	
	/*Hardcoded the value of CallingOrganizationCode as "Academy_Direct" unlike OOB code where current store is being sent*/
	/* BOPIS - 792 has been resolved here */
	openRecordShortagePopup : function() 
	{
		var screenInputModel = null;
		var codeType = "YCD_PICK_SHORT_RESOL";
		screenInputModel = _scScreenUtils.getTargetModel(
				this, "getShortageReasonCommonCode", null);
				
		_scModelUtils.setStringValueAtModelPath(
				"CommonCode.CodeType", codeType,
				screenInputModel);
		/*Hardcoded the value of CallingOrganizationCode as "Academy_Direct" unlike OOB code where current store is being sent*/
		_scModelUtils.setStringValueAtModelPath(
				"CommonCode.CallingOrganizationCode",
				"Academy_Direct",
				screenInputModel);

		var storeBatchLineModel = this.getBatchLineModel();

		var zero = 0;
		var quantity = _scModelUtils
				.getNumberValueFromPath(
						"StoreBatchLine.Quantity",
						storeBatchLineModel);
		var bpPickedQuantity = _scModelUtils
				.getNumberValueFromPath(
						"StoreBatchLine.BackroomPickedQuantity",
						storeBatchLineModel);

		quantity = _iasUIUtils.isValueNumber(quantity) ? quantity
				: zero;
		bpPickedQuantity = _iasUIUtils
				.isValueNumber(bpPickedQuantity) ? bpPickedQuantity
				: zero;
		var shortageQuantity = quantity - bpPickedQuantity;
		_scModelUtils.setNumberValueAtModelPath(
				"StoreBatchLine.DisplayQty",
				bpPickedQuantity, storeBatchLineModel);
		_scModelUtils.setNumberValueAtModelPath(
				"StoreBatchLine.DisplayShortQty",
				shortageQuantity, storeBatchLineModel);

		var bindings = null;
		bindings = {};
		var screenConstructorParams = null;
		screenConstructorParams = {};
		screenConstructorParams["shortageReasonPath"] = "StoreBatchLine.ShortageResolutionReason";
		screenConstructorParams["entity"] = "StoreBatchLine";
		bindings["ShipmentLine"] = storeBatchLineModel;
		var popupParams = null;
		popupParams = {};
		popupParams["screenInput"] = screenInputModel;
		popupParams["outputNamespace"] = "ShortedBatchLineModel";
		popupParams["binding"] = bindings;
		popupParams["screenConstructorParams"] = screenConstructorParams;
		var dialogParams = null;
		dialogParams = {};
		dialogParams["closeCallBackHandler"] = "onShortageReasonSelection";
		dialogParams["class"] = "popupTitleBorder fixedActionBarDialog";
		_iasUIUtils
				.openSimplePopup(
						"wsc.components.shipment.common.screens.ShortageReasonPopup",
						"Title_ShortageReason", this,
						popupParams, dialogParams);
	},
	
	/* This OOB method has been overridden to rectify the issue being faced with "1.00 of 2.00 qty" where "1.00 of" was not being populated when "extnRefreshBatchLine" method is called in parent screen */
	refreshBatchLine : function(event, bEvent, ctrl, args) 
	{
		var batchLineModel = _scBaseUtils.getValueFromPath(
				"BatchLine", args);
		//OMNI-66083 START
		var controllersModel = _scBaseUtils.getValueFromPath("screen.behaviorControllers", args);
		var mashupArray = _scBaseUtils.getValueFromPath("0.mashupContext.mashupArray", controllersModel);
		for(var index in mashupArray){
			var mashupRefId= _scModelUtils.getStringValueFromPath("mashupRefId", mashupArray[index]);
			if(_scBaseUtils.equals(mashupRefId,"extn_getFlagToDisableAddQtyAndReadOnlyScannedQty")){
				var mashupOutput= _scModelUtils.getStringValueFromPath("mashupRefOutput.CommonCodeList", mashupArray[index]);
				var isManualEntry = mashupOutput.CommonCode[0].CodeShortDescription;
					if(!(_scBaseUtils.isVoid(isManualEntry)) && (_scBaseUtils.equals(isManualEntry,"Y"))){
						_scModelUtils.setStringValueAtModelPath("StoreBatchLine.isManualEntry" ,"Y", batchLineModel);
					}			 
			}
		}
		//OMNI-66083 END
		
		if(_scBaseUtils.isVoid(batchLineModel))
		{
			batchLineModel = _scScreenUtils.getModel(this,"BatchLine");
			var extnBatchLineModel = _scBaseUtils.getValueFromPath("extn_BatchLine", args);
			var backroomPickedQty = _scModelUtils.getNumberValueFromPath(
				"StoreBatchLine.BackroomPickedQuantity",
				batchLineModel);
			_scModelUtils.setNumberValueAtModelPath("StoreBatchLine.BackroomPickedQuantity",backroomPickedQty,extnBatchLineModel);
			this.initBatchLinePanel(extnBatchLineModel);
		}
		else
		{
			this.initBatchLinePanel(batchLineModel);
		}
	},
	//OMNI:66083 START
	initializeScreen : function(event, bEvent, ctrl, args) {		
		var getCommonCodeInput = {};				
		getCommonCodeInput = _scModelUtils.createModelObjectFromKey("CommonCode", getCommonCodeInput);
		_scModelUtils.setStringValueAtModelPath("CommonCode.CodeType", "TGL_RCP_WEB_SOM_UI" , getCommonCodeInput);
		_scModelUtils.setStringValueAtModelPath("CommonCode.CodeValue", "NON_EDITABLE_ADDQTY_SCANNEDQTY" , getCommonCodeInput);
		_iasUIUtils.callApi(this, getCommonCodeInput, "extn_getFlagToDisableAddQtyAndReadOnlyScannedQty", null); 	
		this.initBatchLinePanel(this.getBatchLineModel());
	},
	
	disableEnableMinusPlusLinks : function(batchLineModel) {
					var isBPComplete = _scModelUtils
							.getStringValueFromPath(
									"StoreBatchLine.BackroomPickComplete",
									batchLineModel);
					var bPQty = _scModelUtils.getNumberValueFromPath(
							"StoreBatchLine.BackroomPickedQuantity",
							batchLineModel);
					bPQty = _iasUIUtils.isValueNumber(bPQty) ? bPQty
							: 0;
					if (_scBaseUtils.equals(bPQty, 0)) {
						_scWidgetUtils.disableWidget(this,
								"removeQtyLink", false);
					} else {
						_scWidgetUtils.enableWidget(this,
								"removeQtyLink");
					}
					var isManualEntry = _scModelUtils.getStringValueFromPath("StoreBatchLine.isManualEntry",batchLineModel);
					if(!(_scBaseUtils.isVoid(isManualEntry)) && (_scBaseUtils.equals(isManualEntry,"Y"))){
					_scWidgetUtils.disableWidget(this,"addQtyLink", false);					
					}
					else{
						_scWidgetUtils.enableWidget(this,
					"addQtyLink", false);
					}
				},
		handleMashupOutput: function(mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
				var isManualEntry = null;
					if(_scBaseUtils.equals(mashupRefId,"validateShortageForBatchLine")) {
						if (!(_scBaseUtils.equals(false, applySetModel))) {
							_scScreenUtils.setModel(this,"validateShortageForBatchLine_output",modelOutput, null);
						}						
						this.canBatchLineBeShorted(modelOutput);
					}else if(_scBaseUtils.equals(mashupRefId,"extn_getFlagToDisableAddQtyAndReadOnlyScannedQty")){
						 var commonCodeModel = modelOutput;
						 var batchLineModel = null;						
						 isManualEntry = modelOutput.CommonCodeList.CommonCode[0].CodeShortDescription;
						 batchLineModel = _scScreenUtils.getModel(this,"BatchLine");						
						  if(!(_scBaseUtils.isVoid(isManualEntry)) && (_scBaseUtils.equals(isManualEntry,"Y"))){
							  _scModelUtils.setStringValueAtModelPath("StoreBatchLine.isManualEntry" ,"Y", batchLineModel);	
								_scWidgetUtils.disableWidget(this, "addQtyLink",true);}
					}
				},
		//OMNI:66083 END	
	/* This OOB method has been overridden to nullify the on click event of image link in products panel */
	openItemDetails: function(
	event, bEvent, ctrl, args) {
		// do nothing
	},
	extn_afterInitializeScreen: function(event, bEvent, ctrl, args) {
		//Changes for BOPIS-1567,68,69 : Begin
		var batchLineModel = _scScreenUtils.getModel(this,"BatchLine");
		/*OMNI-31380 Changes -- Start*/
		var yantriksEnabled = _scModelUtils.getStringValueFromPath("StoreBatchLine.YantriksEnabled", batchLineModel);
		if (yantriksEnabled == "N")
		{
		_scWidgetUtils.showWidget(this, "extn_ItemOnHandStock");
		}
		/*OMNI-31380 Changes -- End*/
		var itemSize = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.Extn.ExtnSizeCodeDescription",batchLineModel);
		if(_scBaseUtils.isVoid(itemSize)) {
				itemSize = "  ";
		} else {
				itemSize += " | "
		}
		var itemStyle = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.Extn.ExtnStyle",batchLineModel);
		if(_scBaseUtils.isVoid(itemStyle)) {
				itemStyle = "  ";
		} else {
				itemStyle += " | "
		}
		var itemColor = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.Extn.ExtnVendorColorName",batchLineModel);
		if(_scBaseUtils.isVoid(itemColor)) {
				itemColor = "  ";
		} else {
				itemColor += " | "
		}
		_scWidgetUtils.setValue(this, "extn_ItemDetails", itemSize +  itemStyle + itemColor, false);
		var extnPlanogramDetails = null;
		var extnDept = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.Extn.ExtnDepartmentPlano",batchLineModel);
		var extnPogId = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.Extn.ExtnPogId",batchLineModel);
		var extnPogNumber = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.Extn.ExtnPogNumber",batchLineModel);
		var extnSection = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.Extn.ExtnSection",batchLineModel);
		
		if( _scBaseUtils.isVoid(extnDept) && _scBaseUtils.isVoid(extnPogId) && _scBaseUtils.isVoid(extnPogNumber) && _scBaseUtils.isVoid(extnSection) )
		{
			extnPlanogramDetails = "";
		}
		else
		{
			extnPlanogramDetails = extnPogId + " | " + extnDept + " | " + extnSection + " | " + extnPogNumber;
		}
		_scWidgetUtils.setValue(this, "extn_ItemPlanogram", extnPlanogramDetails, false);
		var extnOnhandStock = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.OnhandInventory",batchLineModel);
		extnOnhandStock = "Total Stock On Hand: " + " " + extnOnhandStock;
		_scWidgetUtils.setValue(this, "extn_ItemOnHandStock", extnOnhandStock, false);
		var extnLiveDate = null;
		extnLiveDate = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemDetails.Extn.ExtnLiveDate",batchLineModel);
		if( _scBaseUtils.isVoid(extnLiveDate))
		{
			extnLiveDate = "";
		}
		else
		{
			extnLiveDate = extnLiveDate.substring(0,10);
			extnLiveDate = "Live Date: " + " " + extnLiveDate;
		}
		_scWidgetUtils.setValue(this, "extn_ItemLiveDate", extnLiveDate, false);
		//Changes for BOPIS-1567,68,69 : End
    
    
   		//OMNI-45645: SIM Integration -- Start
		//this.getSIMUpdates();		
		var strAvlStockOnHnd = null;
		var strLastRecDate = null;
		var strItemPrice = null;
		
		strAvlStockOnHnd = _scModelUtils.getStringValueFromPath("StoreBatchLine.AvailableStockOnHand", batchLineModel);
		strLastRecDate = _scModelUtils.getStringValueFromPath("StoreBatchLine.LastReceivedDate", batchLineModel);
		strItemPrice = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemPrice", batchLineModel);
		
		if(!_scBaseUtils.isVoid(strAvlStockOnHnd) || !_scBaseUtils.isVoid(strLastRecDate) || !_scBaseUtils.isVoid(strItemPrice)){
			strAvlStockOnHnd = "Available stock on hand: " + " " + strAvlStockOnHnd;
			_scWidgetUtils.setValue(this, "extn_AvlStockOnHand", strAvlStockOnHnd, false);
			
			strLastRecDate = "Last received date: " + " " + strLastRecDate;
			_scWidgetUtils.setValue(this, "extn_ItemLRD", strLastRecDate, false);
			
			strItemPrice = "Item price: " + strItemPrice;
			_scWidgetUtils.setValue(this, "extn_ItemPriceLabel", strItemPrice, false);
		}else{
			strAvlStockOnHnd = "Available stock on hand:";
			_scWidgetUtils.setValue(this, "extn_AvlStockOnHand", strAvlStockOnHnd, false);
			
			strLastRecDate = "Last received date:";
			_scWidgetUtils.setValue(this, "extn_ItemLRD", strLastRecDate, false);
			
			strItemPrice = "Item price:";
			_scWidgetUtils.setValue(this, "extn_ItemPriceLabel", strItemPrice, false);
		}
	
	/*	
	getSIMUpdates: function() {
		var strItemid = null;
		//var strStoreId = "033";

        var batchLineModel = _scScreenUtils.getModel(this,"BatchLine");
		strItemid = _scModelUtils.getStringValueFromPath("StoreBatchLine.ItemID",batchLineModel);
		var mashupInput = _scBaseUtils.getNewModelInstance();
		if (!_scBaseUtils.isVoid(strItemid)){
			_scModelUtils.setStringValueAtModelPath("Item.ItemId", strItemid ,mashupInput);
			_scModelUtils.setStringValueAtModelPath("Item.StoreId", "" ,mashupInput);				
			_iasUIUtils.callApi(this, mashupInput, "extn_AcademySIMIntegrationRestAPI", null);
		}
		
	},
	
	handleMashupOutput: function(mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
		 
		if(_scBaseUtils.equals(mashupRefId,"validateShortageForBatchLine")) {
			
			if (!(_scBaseUtils.equals(false, applySetModel))) {
				_scScreenUtils.setModel(this,"validateShortageForBatchLine_output",modelOutput, null);
			}
			this.canBatchLineBeShorted(modelOutput);

		}else if(_scBaseUtils.equals(mashupRefId, "extn_AcademySIMIntegrationRestAPI")) {

            if(_scModelUtils.hasAttributeInModelPath("Order.Item.LastReceivedDate", modelOutput)){
            	var strLastRecDate = null;          	
                strLastRecDate = _scModelUtils.getStringValueFromPath("Order.Item.LastReceivedDate", modelOutput);
				strLastRecDate = "Last Received Date: " + " " + strLastRecDate;
                _scWidgetUtils.setValue(this, "extn_ItemLRD", strLastRecDate, false);
            }else{
                _scWidgetUtils.setValue(this, "extn_ItemLRD", "", false);
            }			
		}
	}
	*/
	//OMNI-45645: SIM Integration -- End
    
    
	}
});
});

