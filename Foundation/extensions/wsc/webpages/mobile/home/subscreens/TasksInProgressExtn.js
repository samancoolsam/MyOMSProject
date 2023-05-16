
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/mobile/home/subscreens/TasksInProgressExtnUI", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!ias/utils/ScreenUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnTasksInProgressExtnUI
			,
				_scModelUtils
			,
				_scBaseUtils
			,
				_iasScreenUtils
){ 
	return _dojodeclare("extn.mobile.home.subscreens.TasksInProgressExtn", [_extnTasksInProgressExtnUI],{
	// custom code here
	
	getIdentifierRepeatingScreenData: function(
        shipmentModel, screen, widget, namespace, modelObject) {
            var repeatingScreenId = "wsc.mobile.common.screens.shipment.picking.ShipmentPickDetails";
            var returnValue = null;
            var identifierId = null;
            var shipmentStatus = null;
            var deliveryMethod = null;
            shipmentStatus = _scModelUtils.getStringValueFromPath("Status.Status", shipmentModel);
            deliveryMethod = _scModelUtils.getStringValueFromPath("DeliveryMethod", shipmentModel);
            if (
            _scBaseUtils.equals(
            deliveryMethod, "PICK") && _scBaseUtils.contains(shipmentStatus, "1100.70.06.20")) 
			{
                identifierId = "Pick";
            } 
			else if (
            _scBaseUtils.equals(
            deliveryMethod, "SHP") && _scBaseUtils.contains(shipmentStatus, "1100.70.06.20")) 
			{
                identifierId = "Ship";
            }
			else if (
			_scBaseUtils.equals(
            deliveryMethod, "SHP") && _scBaseUtils.contains(shipmentStatus, "1100.70.06.70")) 
			{
                identifierId = "Pack";
            }
            else if (
			_scBaseUtils.equals(
            deliveryMethod, "SHP") && _scBaseUtils.contains(shipmentStatus, "1100.70.06.50")) 
			{
                identifierId = "Pack";
            }
            if(!_scBaseUtils.isVoid(
            identifierId))
            {
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
            }
           
        }
});
});

