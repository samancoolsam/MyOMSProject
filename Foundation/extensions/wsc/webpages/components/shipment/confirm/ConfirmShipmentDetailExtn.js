
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/confirm/ConfirmShipmentDetailExtnUI","scbase/loader!ias/utils/PrintUtils", "scbase/loader!ias/utils/RepeatingScreenUtils", "scbase/loader!ias/utils/ContextUtils", "scbase/loader!ias/utils/ScreenUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/RepeatingPanelUtils", "scbase/loader!sc/plat/dojo/utils/ResourcePermissionUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!wsc/components/common/utils/CommonUtils", "scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils", "scbase/loader!wsc/components/shipment/customerpickup/SummaryUI", "scbase/loader!sc/plat/dojo/Userprefs","scbase/loader!ias/utils/EventUtils","scbase/loader!ias/utils/BaseTemplateUtils","scbase/loader!wsc/components/shipment/container/pack/ContainerPackUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnConfirmShipmentDetailExtnUI
			 ,
			 _iasPrintUtils, _iasRepeatingScreenUtils, _iasContextUtils, _iasScreenUtils, _iasUIUtils, _scBaseUtils, _scEventUtils, _scModelUtils, _scRepeatingPanelUtils, _scResourcePermissionUtils, _scScreenUtils, _scWidgetUtils, _wscCommonUtils, _wscShipmentUtils, _wscSummaryUI, _scUserprefs, _iasEventUtils, _iasBaseTemplateUtils, _wscContainerPackUtils
){ 
	return _dojodeclare("extn.components.shipment.confirm.ConfirmShipmentDetailExtn", [_extnConfirmShipmentDetailExtnUI],{
	// custom code here
        processConfirmShipmentOutput: function(
        shipmentContainerList) {
            var failureCount = 0;
            failureCount = _scModelUtils.getNumberValueFromPath("FailedContainers.TotalFailures", shipmentContainerList);
            if (
            _iasUIUtils.isValueNumber(
            failureCount)) {
                if (
                failureCount > 0) {
                    this.confirmShipmentError = "Message_ThereAreFewFailedContainers";
                } else if (
                _scBaseUtils.equals(
                failureCount, 0)) {
                    if (
                    _scBaseUtils.equals(
                    _scModelUtils.getStringValueFromPath("FailedContainers.CloseManifestSuccess", shipmentContainerList), "Y")) {
                        this.confirmShipmentSuccess = "extn_Message_ShipmentConfirmedSuccessfully";
                    } else {
                        this.confirmShipmentError = "Message_ShipmentConfirmedFailed";
                    }
                }
            } else {
                failureCount = _scModelUtils.getNumberValueFromPath("Shipments.TotalFailures", shipmentContainerList);
                if (
                _iasUIUtils.isValueNumber(
                failureCount)) {
                    if (
                    failureCount > 0) {
                        this.confirmShipmentError = "Message_ShipmentConfirmedFailed";
                    } else if (
                    _scBaseUtils.equals(
                    failureCount, 0)) {
                        this.confirmShipmentSuccess = "extn_Message_ShipmentConfirmedSuccessfully";
                    }
                }
            }
            this.validateScacAndCallApi("getShipmentContainerListBeh", "Refresh");
            if (!(
            _scBaseUtils.isVoid(
            this.confirmShipmentError))) {
                _iasBaseTemplateUtils.showMessage(
                this, this.confirmShipmentError, "error", null);
                this.confirmShipmentError = null;
            }
            if (!(
            _scBaseUtils.isVoid(
            this.confirmShipmentSuccess))) {
                _iasBaseTemplateUtils.showMessage(
                this, this.confirmShipmentSuccess, "success", null);
                this.confirmShipmentSuccess = null;
            }
        },
		
		/* This OOB method has been overridden in order to disable the Confirm button even though the no. of packages is not zero */
		updateContainerCount: function(
        shipmentContainerList) {
            this.hideContainerDetailsView();
            this.updateCarrierSelectedModel();
            var numOfPackages = 0;
            numOfPackages = _scModelUtils.getNumberValueFromPath("Containers.TotalNumberOfRecords", shipmentContainerList);
            if (
            _scBaseUtils.equals(
            numOfPackages, 0)) {
                _scWidgetUtils.disableWidget(
                _iasUIUtils.getParentScreen(
                this, true), "confirmBttn2", false);
                _scWidgetUtils.disableWidget(
                _iasUIUtils.getParentScreen(
                this, true), "confirmBttn", false);
            } else {
               /* _scWidgetUtils.enableWidget(
                _iasUIUtils.getParentScreen(
                this, true), "confirmBttn2");
                _scWidgetUtils.enableWidget(
                _iasUIUtils.getParentScreen(
                this, true), "confirmBttn"); */

                 _scWidgetUtils.disableWidget(
                _iasUIUtils.getParentScreen(
                this, true), "confirmBttn2", false);
                _scWidgetUtils.disableWidget(
                _iasUIUtils.getParentScreen(
                this, true), "confirmBttn", false);

            }
            _scWidgetUtils.setValue(
            this, "viewDetailsLink", _scScreenUtils.getString(
            this, "Action_view_details"), false);
            _scScreenUtils.setModel(
            this, "getShipmentContainerList_output", shipmentContainerList, null);
        }
});
});

