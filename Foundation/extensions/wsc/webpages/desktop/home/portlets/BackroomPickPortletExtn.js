
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/desktop/home/portlets/BackroomPickPortletExtnUI","scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!sc/plat/dojo/utils/ResourcePermissionUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!sc/plat/dojo/utils/ControllerUtils","scbase/loader!ias/utils/UIUtils","scbase/loader!sc/plat/dojo/utils/ScreenUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnBackroomPickPortletExtnUI
			,
				_scWidgetUtils
			,
				_scResourcePermissionUtils
			,
				_scBaseUtils
			,
				_scModelUtils
			,
				_scControllerUtils
			,
				_iasUIUtils
			,
				_scScreenUtils
){ 
	return _dojodeclare("extn.desktop.home.portlets.BackroomPickPortletExtn", [_extnBackroomPickPortletExtnUI],{
	// custom code here
	
	/* This OOB method has been overridden in order to hide "Print Shipping Orders" and "Print All buttons" from Home Screen Backroom Pick Portlet */
	initializeScreen: function(
        event, bEvent, ctrl, args) {
            this.setCountToLabels();
            if (
            _scResourcePermissionUtils.hasPermission("WSC000009") || _scResourcePermissionUtils.hasPermission("WSC000021")) {
                _scWidgetUtils.showWidget(
                this, "lnkRefresh", false, null);
                _scWidgetUtils.showWidget(
                this, "pnlPrintButtons", false, null);
				_scWidgetUtils.hideWidget(
                this, "btnPrintShipping", true);
                _scWidgetUtils.hideWidget(
                this, "btnPrintAll", true); 
            }
            if (
            _scResourcePermissionUtils.hasPermission("WSC000009") && _scResourcePermissionUtils.hasPermission("WSC000021")) {
               /* _scWidgetUtils.showWidget(
                this, "btnPrintAll", false, null);*/
				_scWidgetUtils.hideWidget(
                this, "btnPrintShipping", true);
                _scWidgetUtils.hideWidget(
                this, "btnPrintAll", true);
            } 
               

        },
		
		/*  This OOB method has been overridden to input and call a mashup where OOB service has been replaced with a new configured service when "Print Pickup Order" button is clicked from UI */
		printOrders: function(
        res, args) {
            if (
            _scBaseUtils.equals(
            res, "Ok")) {
                var printModel = null;
                printModel = _scModelUtils.createNewModelObjectWithRootKey("Print");
				var shipmentCountModel = null;
				shipmentCountModel = _scScreenUtils.getModel(
				this, "pickCount_output");
				
               /* _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine.OrderLine.DeliveryMethod", _scModelUtils.getStringValueFromPath("Print.DeliveryMethod", args), shipmentModel);
                _scModelUtils.setStringValueAtModelPath("Shipment.MaximumRecords", "1", shipmentModel); 
                var mashupContext = null;
                mashupContext = _scControllerUtils.getMashupContext(
                this);
                _scBaseUtils.setAttributeValue("Action", _scModelUtils.getStringValueFromPath("Print.Action", args), mashupContext);
                _iasUIUtils.callApi(
                this, shipmentModel, "printPickTicket", mashupContext);*/
				var shipmentModel =  _scModelUtils.getStringValueFromPath("Shipments.Shipment", shipmentCountModel);
				var shipmentKey = _scModelUtils.getStringValueFromPath("ShipmentKey", shipmentModel[0]);
				_scModelUtils.setStringValueAtModelPath("Print.ShipmentKey", shipmentKey, printModel);
				var mashupContext = null;
                mashupContext = _scControllerUtils.getMashupContext(
                this);
                _scBaseUtils.setAttributeValue("Action", _scModelUtils.getStringValueFromPath("Print.Action", args), mashupContext);
				_iasUIUtils.callApi(
                this, printModel, "printPickTicket", mashupContext);
            }
        },
		
		/* This OOB method has be overridden in order to enable silent printing */
		printAndRefresh: function(
        modelOutput, mashupContext) {
            var action = null;
            /*_iasPrintUtils.printHtmlOutput(
            modelOutput);*/
            action = _scBaseUtils.getAttributeValue("Action", false, mashupContext);
            var shipmentModel = null;
            shipmentModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
            if (
            _scBaseUtils.equals(
            action, "PrintPick")) {
                _iasUIUtils.callApi(
                this, shipmentModel, "getShipmentListReadyForPick", mashupContext);
            } else if (
            _scBaseUtils.equals(
            action, "PrintShip")) {
                _iasUIUtils.callApi(
                this, shipmentModel, "getShipmentListReadyForShip", mashupContext);
            } else if (
            _scBaseUtils.equals(
            action, "PrintAll")) {
                var eventArgs = null;
                var eventDefn = null;
                eventDefn = {};
                eventArgs = {};
                _scEventUtils.fireEventInsideScreen(
                this, "lnkRefresh_onClick", eventDefn, eventArgs);
            }
        }
});
});

