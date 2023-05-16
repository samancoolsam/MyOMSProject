
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/common/screens/ShortageReasonPopupExtnUI","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!ias/utils/ContextUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnShortageReasonPopupExtnUI
			,
				_scScreenUtils
			,
				_scBaseUtils
			,
				_scModelUtils
			,	
				_iasContextUtils
){ 
	return _dojodeclare("extn.components.shipment.common.screens.ShortageReasonPopupExtn", [_extnShortageReasonPopupExtnUI],{
	// custom code here
	/*This method has been overridden to send ShortageQty and Extended attributes in the output of this method */

	getPopupOutput: function(
        event, bEvent, ctrl, args) {
            var shortageReasonTargetModel = null;
            var shipmentLineModel = null;
            var shortageReasonCode = null;
            shortageReasonTargetModel = {};
            shipmentLineModel = _scScreenUtils.getModel(
            this, "ShipmentLine");
            var shortageReasonModel = null;
            shortageReasonModel = _scBaseUtils.getTargetModel(
            this, "getShortageReasonOutput", null);
			var shortageReasonExtn = _scModelUtils.getStringValueFromPath("ShortageReason", shortageReasonModel)
            var shortedShipmentLineModel = null;
            var isCancelReasonSet = false;
			var curbsideConsFlag = _iasContextUtils.getFromContext("CurbsideConsolidationToggle");//OMNI-85085
			var instoreConsFlag = _iasContextUtils.getFromContext("InStoreConsolidationToggle");//OMNI-102218
            shortedShipmentLineModel = {};
            shortedShipmentLineModel = _scModelUtils.createModelObjectFromKey(this.entity, shortedShipmentLineModel);
            _scModelUtils.setStringValueAtModelPath(
            this.shortageReasonPath, shortageReasonExtn, shortedShipmentLineModel);
			/* The below line has been added to send the shortage quantity to the popup output model */
			if(_scBaseUtils.equals("StoreBatchLine",this.entity))
			{
				_scModelUtils.setStringValueAtModelPath(
				"StoreBatchLine.DisplayShortQty", shipmentLineModel.StoreBatchLine.DisplayShortQty, shortedShipmentLineModel);
			}			
            if(!_scBaseUtils.equals("StoreBatchLine",this.entity)) {
            	_scModelUtils.setStringValueAtModelPath("ShipmentLine.ShipmentLineKey", _scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentLineKey", shipmentLineModel), shortedShipmentLineModel);
				/* BOPIS 188 has been resolved */
				var markAllShortLineWithShortage = _scModelUtils.getStringValueFromPath("MarkAllShortLineWithShortage", shortageReasonModel);
				if( (_scBaseUtils.equals(markAllShortLineWithShortage, "N")) || (_scBaseUtils.isVoid(markAllShortLineWithShortage)) )
				{
					_scModelUtils.setStringValueAtModelPath("ShipmentLine.ShortageQty", _scModelUtils.getStringValueFromPath("ShipmentLine.DisplayShortQty", shipmentLineModel), shortedShipmentLineModel);
					if (_scBaseUtils.equals(this.flowName, "ContainerPack")) {
						_scModelUtils.setStringValueAtModelPath("ShipmentLine.PackedQty", _scModelUtils.getStringValueFromPath("ShipmentLine.DisplayQty", shipmentLineModel), shortedShipmentLineModel);
					}
					if (_scBaseUtils.equals(this.flowName, "CustomerPick")) {
						/*var cancelReasonModel = null;
						cancelReasonModel = _scBaseUtils.getTargetModel(
						this, "getCancellationReasonCodeOutput", null);
						var cancelReasonCode = _scModelUtils.getStringValueFromPath("CancelReasonCode", cancelReasonModel); */
						_scModelUtils.setStringValueAtModelPath(
						this.cancelReasonPath, shortageReasonExtn, shortedShipmentLineModel);
						isCancelReasonSet = true;
						//OMNI-85085,OMNI-102218 - Start
						if(_iasContextUtils.isMobileContainer() && !_scBaseUtils.isVoid(curbsideConsFlag) && _scBaseUtils.equals(curbsideConsFlag,"Y") || 
						(!_scBaseUtils.isVoid(instoreConsFlag) && _scBaseUtils.equals(instoreConsFlag,"Y"))){
						_scModelUtils.setStringValueAtModelPath("ShipmentLine.ShipmentKey", _scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentKey", shipmentLineModel), 
						shortedShipmentLineModel);
						}
						//OMNI-85085,OMNI-102218 - End
						//removing below code since Extn attributes are not required for customer pick screen
						// if (_scBaseUtils.equals(this.flowName, "CustomerPick")) 
						// {
						// 	var cancelReasonModel = null;
						// 	cancelReasonModel = _scBaseUtils.getTargetModel(
						// 	this, "getCancellationReasonCodeOutput", null);
						// 	var cancelReasonCode = _scModelUtils.getStringValueFromPath("CancelReasonCode", cancelReasonModel);
						// 	_scModelUtils.setStringValueAtModelPath(
						// 	this.cancelReasonPath, cancelReasonCode, shortedShipmentLineModel);
						// 	_scModelUtils.setStringValueAtModelPath("ShipmentLine.Extn.ExtnReasonCode", cancelReasonCode, shortedShipmentLineModel);
						// }
						// else
						// {
						// 	_scModelUtils.setStringValueAtModelPath("ShipmentLine.Extn.ExtnReasonCode", shortageReasonExtn, shortedShipmentLineModel);
						// }
					}
					else {
						_scModelUtils.setStringValueAtModelPath("ShipmentLine.Extn.ExtnMsgToSIM", "N", shortedShipmentLineModel);
						_scModelUtils.setStringValueAtModelPath("ShipmentLine.Extn.ExtnReasonCode", shortageReasonExtn, shortedShipmentLineModel);
					}
				}
				// set the cancelReason when markAllShortLineWithShortage is Y
				if(!isCancelReasonSet && _scBaseUtils.equals(this.flowName, "CustomerPick")) {
					/*var cancelReasonModel = null;
					cancelReasonModel = _scBaseUtils.getTargetModel(
					this, "getCancellationReasonCodeOutput", null);
					var cancelReasonCode = _scModelUtils.getStringValueFromPath("CancelReasonCode", cancelReasonModel); */
					_scModelUtils.setStringValueAtModelPath(
					this.cancelReasonPath, shortageReasonExtn, shortedShipmentLineModel);
					//OMNI-85085,OMNI-102218 - Start
					if(_iasContextUtils.isMobileContainer() && !_scBaseUtils.isVoid(curbsideConsFlag) && _scBaseUtils.equals(curbsideConsFlag,"Y") || 
						(!_scBaseUtils.isVoid(instoreConsFlag) && _scBaseUtils.equals(instoreConsFlag,"Y"))){
					_scModelUtils.setStringValueAtModelPath("ShipmentLine.ShipmentKey", _scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentKey", shipmentLineModel), 
						shortedShipmentLineModel);
					}
					//OMNI-85085,OMNI-102218 - End
				}
				_scModelUtils.setStringValueAtModelPath(
                this.markAllBindingPath, markAllShortLineWithShortage, shortedShipmentLineModel);
            }
            /* The below OOB code scenario has been covered in the above code 
			if (
            _scBaseUtils.equals(
            this.flowName, "CustomerPick")) {
                var cancelReasonModel = null;
                cancelReasonModel = _scBaseUtils.getTargetModel(
                this, "getCancellationReasonCodeOutput", null);
                _scModelUtils.setStringValueAtModelPath(
                this.cancelReasonPath, _scModelUtils.getStringValueFromPath("CancelReasonCode", cancelReasonModel), shortedShipmentLineModel);
            } */
            return shortedShipmentLineModel;
        }
});
});
