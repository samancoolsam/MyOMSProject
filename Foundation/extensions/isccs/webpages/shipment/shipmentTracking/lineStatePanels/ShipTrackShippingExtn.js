
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/shipment/shipmentTracking/lineStatePanels/ShipTrackShippingExtnUI","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!isccs/utils/OrderUtils","scbase/loader!isccs/utils/UIUtils","scbase/loader!sc/plat/dojo/utils/ScreenUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnShipTrackShippingExtnUI,_scBaseUtils,_scModelUtils,_scWidgetUtils,_isccsOrderUtils,_isccsUIUtils, _scScreenUtils
){ 
	return _dojodeclare("extn.shipment.shipmentTracking.lineStatePanels.ShipTrackShippingExtn", [_extnShipTrackShippingExtnUI],{
	// custom code here
	
	//OMNI-52982 :Begin
	extn_getCarrierServiceCode:function(dataValue, screen, widget, namespace, modelObject, options) {
	var ordDate = null;
	var orderPromiseDate = null;
	var lineBreakupmodel = _scScreenUtils.getModel(this, "TrackingData");
	var CarrierServiceCode = _scModelUtils.getStringValueFromPath("ShipmentLine.Shipment.CarrierServiceName",lineBreakupmodel);
	var parentScreen = _isccsUIUtils.getParentScreen(this, false);
	var lineModel = parentScreen.getModel("getCompleteOrderLineDetails_Output");
	var isSignatureRequired = _scModelUtils.getStringValueFromPath("OrderLine.Extn.ExtnIsSignatureRequired", lineModel);
	if(!_scBaseUtils.isVoid(isSignatureRequired) && _scBaseUtils.equals(isSignatureRequired,"Y")) {
	return CarrierServiceCode+" - Sig. service req";
	}
	else{
	return CarrierServiceCode;
	}
	},
	//OMNI-52982 :End
	
	extn_PromisedDate:function(dataValue, screen, widget, namespace, modelObject, options) {

  		var ordDate = null;
  		var orderPromiseDate = null;
		var parentScreen = _isccsUIUtils.getParentScreen(this, false);
		var lineModel = parentScreen.getModel("getCompleteOrderLineDetails_Output"); 
		//OMNI-15007 Fix : Begin
		var fulfillmentType = _scModelUtils.getStringValueFromPath("OrderLine.FulfillmentType", lineModel);
		if(fulfillmentType!="DROP_SHIP" && fulfillmentType!="SOF" && fulfillmentType!="EGC"){
		    orderPromiseDate = _scModelUtils.getStringValueFromPath("OrderLine.LineTracking.LineBreakups.LineBreakup.0.ShipmentLine.Shipment.ShipmentLines.ShipmentLine.0.OrderLine.Extn.ExtnInitialPromiseDate", lineModel);		
		} else {
			orderPromiseDate = _scModelUtils.getStringValueFromPath("OrderLine.Extn.ExtnInitialPromiseDate", lineModel);
		}						
		//OMNI-15007 Fix : End
		//var ordDate = _isccsOrderUtils.getOrderDate(lineModel, "YCD_FTC_FIRST_PROMISE_DATE", "ActualDate");
		if(_scBaseUtils.isVoid(orderPromiseDate)){
			return null;
		}		
		return _scBaseUtils.formatDateToUserFormat(orderPromiseDate);
		
	},
      extn_getShipmentNo:function(dataValue, screen, widget, namespace, modelObject, options) {
	 var containerElem = null;
            containerElem = _scBaseUtils.getValueFromPath("ContainerDetail", modelObject);
            var shipLineElem = null;
            shipLineElem = _scBaseUtils.getValueFromPath("ShipmentLine", modelObject);
            var returnString = null;
            if (!(
            _scBaseUtils.isVoid(
            containerElem))) {
		if (!(
            _scBaseUtils.isVoid(
            shipLineElem))) {
		  returnString = _scModelUtils.getStringValueFromPath("ShipmentLine.Shipment.ShipmentNo", modelObject);
		}
              
            }
      return returnString;
      },

  extn_disableShipmentNo: function(event, bEvent, ctrl, args)
  {
     var shipmentNo= _scWidgetUtils.getValue(this,"extn_shipementnumber");
     if (!(_scBaseUtils.isVoid(shipmentNo))) {
	 _scWidgetUtils.showWidget(this, "extn_shipementnumber", false, null);

       }
      //  OMNI-1925 : Customer Care: Sterling Ship Node Details display - Start 
      var lineBreakupmodel = _scScreenUtils.getModel(this, "TrackingData");
      var ShipNode = _scModelUtils.getStringValueFromPath("ShipmentLine.Shipment.ShipNode",lineBreakupmodel);
      if(!_scBaseUtils.isVoid(ShipNode))
       {
  	    var inputModel = _scBaseUtils.getNewModelInstance();
        _scModelUtils.setStringValueAtModelPath("ShipNode.ShipNode", ShipNode,inputModel);
        _isccsUIUtils.callApi(this,inputModel,"extn_getShipNodeListMashup", null);
	     }
  },
extn_DisplayStoreAddress: function(event, bEvent, ctrl, args)
  {
	var model = args;
	var shipNodeListModel = _scScreenUtils.getModel(
            this, "extn_getShipNodeList_ns");
           var shipNodeModel= _scModelUtils.getStringValueFromPath("ShipNode.0",shipNodeListModel);
			var shipNodeValue = _scModelUtils.getStringValueFromPath("ShipNode", shipNodeModel);
			 var popupParams = null;
                var dialogParams = null;
                popupParams = {};
                dialogParams = {};
                popupParams["screenInput"] = shipNodeModel;
				_scModelUtils.setStringValueAtModelPath("ShipNode", shipNodeModel, popupParams);
                dialogParams["closeCallBackHandler"] = "onCancellationReasonSelection";
				var popupTitle= "Store Details: " +shipNodeValue;
		 _isccsUIUtils.openSimplePopup("extn.customPopup.StoreAddress", popupTitle, this, popupParams, dialogParams);		
 },

onCancellationReasonSelection: function(event,bEvent,ctrl,args)
	{
	},
extn_onMashupCompletion:function(evetn, bEvent, ctrl, args)
{
 var model = args;
 var mashupArray=_scModelUtils.getStringValueFromPath("mashupArray",args);
 for(var index in mashupArray)
 {
 	var mashupRefId= _scModelUtils.getStringValueFromPath("mashupRefId", mashupArray[index]);
 	if(_scBaseUtils.equals(mashupRefId,"extn_getShipNodeListMashup"))
 	{
 		var mashupOutput= _scModelUtils.getStringValueFromPath("mashupRefOutput.ShipNodeList", mashupArray[index]);
 		_scScreenUtils.setModel(this,"extn_getShipNodeList_ns",mashupOutput,null);
 	}
 } 
}
//  OMNI-1925 : Customer Care: Sterling Ship Node Details display - End
});
});