
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/container/pack/ContainerPackProductListExtnUI","scbase/loader!ias/utils/RepeatingScreenUtils", "scbase/loader!ias/utils/ScreenUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/ControllerUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/RepeatingPanelUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!wsc/components/shipment/container/pack/ContainerPackProductListUI", "scbase/loader!wsc/components/shipment/container/pack/ContainerPackUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnContainerPackProductListExtnUI
			 ,
			 _iasRepeatingScreenUtils, _iasScreenUtils, _iasUIUtils, _scBaseUtils, _scControllerUtils, _scEventUtils, _scModelUtils, _scRepeatingPanelUtils, _scScreenUtils, _scWidgetUtils, _wscContainerPackProductListUI, _wscContainerPackUtils
){ 
	return _dojodeclare("extn.components.shipment.container.pack.ContainerPackProductListExtn", [_extnContainerPackProductListExtnUI],{
	// custom code here
	//overriding method to invoke custom backend service 'AcademyStoreRecordShortageService'
	callRecordShortageForPack: function(
        event, bEvent, ctrl, args) {
            var shortedShipmentLineModel = null;
            var targetModel = null;
            shortedShipmentLineModel = _scBaseUtils.getValueFromPath("shortedShipmentLineModel", args);
            var shipmentDetailModel = _scScreenUtils.getModel(this, "getShipmentDetails_output");
            var shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status", shipmentDetailModel);
            if (!(
            _scBaseUtils.isVoid(
            shortedShipmentLineModel))) {
                var mashupContext = null;
                mashupContext = _scControllerUtils.getMashupContext(
                this);
                targetModel = _scBaseUtils.getTargetModel(
                this, "recordShortageForPack_input", null);
                _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine.ShipmentLineKey", _scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentLineKey", shortedShipmentLineModel), targetModel);
                // _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine.ShortageReason", _scModelUtils.getStringValueFromPath("ShipmentLine.ShortageReasonCode", shortedShipmentLineModel), targetModel);
                var activeContainerInfo = null;
                activeContainerInfo = _scScreenUtils.getModel(
                this, "activeContainerModel");
                shipmentContainerKey = _scModelUtils.getStringValueFromPath("Container.ShipmentContainerKey", activeContainerInfo);
                if (!(
                _scBaseUtils.isVoid(
                shipmentContainerKey))) {
                    _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentContainerKey", shipmentContainerKey, targetModel);
                }
                // _iasUIUtils.callApi(
                // this, targetModel, "containerPack_recordShortageForPack", mashupContext);
                var quantity = _scModelUtils.getStringValueFromPath("ShipmentLine.PackedQty", shortedShipmentLineModel);
                var shortageQty = _scModelUtils.getStringValueFromPath("ShipmentLine.ShortageQty", shortedShipmentLineModel);
                var extnReasonCode = _scModelUtils.getStringValueFromPath("ShipmentLine.ShortageReasonCode", shortedShipmentLineModel);
               	_scModelUtils.setStringValueAtModelPath("Shipment.BackOrderRemovedQuantity", "Y", targetModel);
               	_scModelUtils.setStringValueAtModelPath("Shipment.Status", shipmentStatus, targetModel);
                _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine.Quantity", quantity, targetModel);
                _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine.ShortageQty", shortageQty, targetModel);
                _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine.Extn.ExtnMsgToSIM", "Y", targetModel);
                _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine.Extn.ExtnReasonCode", extnReasonCode, targetModel);
                _iasUIUtils.callApi(this, targetModel, "extn_recordShortageForPack_ref", null);

            }
        },
        extn_afterBehaviorMashupCall: function(event, bEvent, ctrl, args) {
        	var mashupRefList = _scModelUtils.getModelObjectFromPath("mashupArray", args);
        	if (!_scBaseUtils.isVoid(mashupRefList)) {
            	for (var i = 0; i < mashupRefList.length; i++) {
	                var mashupRefid = _scModelUtils.getStringValueFromPath("mashupRefId", mashupRefList[i]);
	                if (_scBaseUtils.equals(mashupRefid, "extn_recordShortageForPack_ref")) {
	                	var modelOutput = mashupRefList[i].mashupRefOutput;
	                	var status = null;
		                status = _scModelUtils.getStringValueFromPath("Shipment.Status", modelOutput);
		                if (_scBaseUtils.equals(status, "9000")) {
		                    var textObj = null;
		                    textObj = {};
		                    textObj["OK"] = _scScreenUtils.getString(
		                    this, "Ok");
		                    _scScreenUtils.showInfoMessageBox(
		                    this, _scScreenUtils.getString(
		                    this, "Message_ShipmentCancelled"), "OpenShipmentSummaryWizard", textObj);
		                }
                        //BOPIS-1431-BEGIN
                        /*else if (
                        _scBaseUtils.equals(
                        _scModelUtils.getNumberValueFromPath("Shipment.ShipmentContainerizedFlag", modelOutput), 3)) {
                            var argsBean = null;
                            argsBean = {};
                            _scBaseUtils.setAttributeValue("modelOutput", modelOutput, argsBean);
                            var textObj = null;
                            textObj = {};
                            textObj["OK"] = _scScreenUtils.getString(
                            this, "Ok");
                            _scScreenUtils.showSuccessMessageBox(
                            this, _scScreenUtils.getString(
                            this, "Message_PackCompleted"), "handlePackCompletion", textObj);
                        } */
                        //BOPIS-1431-END
                        else {
		                    this.UpdateIndividualRepeatingPanel(
		                    this, modelOutput);
		                }
			        }
	            }
	        }
        },
        handleMashupOutput: function(
        mashupRefId, modelOutput, modelInput, mashupContext) {
            if (
            _scBaseUtils.equals(
            mashupRefId, "containerPack_changeShipmentCall") || _scBaseUtils.equals(
            mashupRefId, "pack_changeShipmentForDraftContainer")) {
                var shipmentContainerKey = null;
                shipmentContainerKey = _scModelUtils.getStringValueFromPath("Shipment.Containers.Container.ShipmentContainerKey", modelInput);
                if (
                _scBaseUtils.isVoid(
                shipmentContainerKey)) {
                    _wscContainerPackUtils.setDraftContainerFlag(
                    _iasUIUtils.getParentScreen(
                    this, true), "N");
                    _wscContainerPackUtils.updateDraftContainerInfo(
                    this, mashupRefId, modelOutput, modelInput, mashupContext);
                }
                if (
                _scBaseUtils.equals(
                _scModelUtils.getNumberValueFromPath("Shipment.ShipmentContainerizedFlag", modelOutput), 3)) {
                    var argsBean = null;
                    argsBean = {};
                    _scBaseUtils.setAttributeValue("modelOutput", modelOutput, argsBean);
                    var textObj = null;
                    textObj = {};
                    textObj["OK"] = _scScreenUtils.getString(
                    this, "Ok");
					this.handlePackCompletion();
                    /* _scScreenUtils.showSuccessMessageBox(
                    this, _scScreenUtils.getString(
                    this, "extn_Message_PackCompleted"), "handlePackCompletion", textObj); */
                } else {
                    this.UpdateIndividualRepeatingPanel(
                    this, modelOutput);
                }
            } else if (
            _scBaseUtils.equals(
            mashupRefId, "containerPack_recordShortageForPack")) {
                var status = null;
                status = _scModelUtils.getStringValueFromPath("Shipment.Status", modelOutput);
                if (
                _scBaseUtils.equals(
                status, "9000")) {
                    var textObj = null;
                    textObj = {};
                    textObj["OK"] = _scScreenUtils.getString(
                    this, "Ok");
                    _scScreenUtils.showInfoMessageBox(
                    this, _scScreenUtils.getString(
                    this, "Message_ShipmentCancelled"), "OpenShipmentSummaryWizard", textObj);
                } else {
                    this.UpdateIndividualRepeatingPanel(
                    this, modelOutput);
                }
            }
        }
});
});

