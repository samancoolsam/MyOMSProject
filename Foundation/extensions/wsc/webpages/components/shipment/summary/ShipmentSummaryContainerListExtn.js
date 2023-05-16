
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/summary/ShipmentSummaryContainerListExtnUI","scbase/loader!ias/utils/PrintUtils", "scbase/loader!ias/utils/RepeatingScreenUtils", "scbase/loader!ias/utils/ContextUtils", "scbase/loader!ias/utils/ScreenUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/RepeatingPanelUtils", "scbase/loader!sc/plat/dojo/utils/ResourcePermissionUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!wsc/components/common/utils/CommonUtils", "scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils", "scbase/loader!wsc/components/shipment/customerpickup/SummaryUI", "scbase/loader!sc/plat/dojo/Userprefs","scbase/loader!ias/utils/EventUtils","scbase/loader!ias/utils/BaseTemplateUtils","scbase/loader!wsc/components/shipment/container/pack/ContainerPackUtils", "scbase/loader!sc/plat/dojo/utils/ControllerUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnShipmentSummaryContainerListExtnUI,
                  _iasPrintUtils, _iasRepeatingScreenUtils, _iasContextUtils, _iasScreenUtils, _iasUIUtils, _scBaseUtils, _scEventUtils, _scModelUtils, _scRepeatingPanelUtils, _scResourcePermissionUtils, _scScreenUtils, _scWidgetUtils, _wscCommonUtils, _wscShipmentUtils, _wscSummaryUI, _scUserprefs, _iasEventUtils, _iasBaseTemplateUtils, _wscContainerPackUtils, _scControllerUtils
){ 
	return _dojodeclare("extn.components.shipment.summary.ShipmentSummaryContainerListExtn", [_extnShipmentSummaryContainerListExtnUI],{
	// custom code here
	handleMoreLink: function(
        event, bEvent, ctrl, args) {
            var container_Src = null;
            var popupParams = null;
            var bindings = null;
            var dialogParams = null;
            dialogParams = {};
            dialogParams["closeCallBackHandler"] = "onCloseCallSelection";
            dialogParams["class"] = "popupTitleBorder popupDialogFooterGap";
            container_Src = _scScreenUtils.getModel(
            this, "container_Src");
            bindings = {};
            _scBaseUtils.setAttributeValue("container_Src", container_Src, bindings);
            var shipmentSrcModel = null;
            shipmentSrcModel = _scScreenUtils.getModel(
            this, "parentShipmentModel");
            _scBaseUtils.setAttributeValue("parentShipmentModel", shipmentSrcModel, bindings);
            popupParams = {};
            _scBaseUtils.setAttributeValue("binding", bindings, popupParams);
            var title = null;
            var containerNo = null;
            containerNo = _scModelUtils.getStringValueFromPath("Container.ContainerNo", container_Src);
            var inputArray = null;
            inputArray = [];
            inputArray.push(
            containerNo);
            title = _scScreenUtils.getFormattedString(
            this, "extn_container_more", inputArray);
            _iasUIUtils.openSimplePopup("wsc.components.shipment.summary.ShipmentSummaryContainerMoreOption", title, this, popupParams, dialogParams);
        },
//OMNI-6583 Mobile Store - STS - TO Detailed Receiving Report Screen Start
		 extn_openTOSummaryScreen: function(event, bEvent, ctrl, args)
	{
	var screenInput= _scBaseUtils.getNewModelInstance();
	var containerModel = _scScreenUtils.getModel(this,"container_Src");
	var OrderHeaderKey=_scModelUtils.getStringValueFromPath("Container.Shipment.OrderHeaderKey",containerModel);
	//var OrderHeaderKey ="202006010550541985105634";
	_scModelUtils.setStringValueAtModelPath("Shipment.OrderHeaderKey",OrderHeaderKey,screenInput);
	//_wscMobileHomeUtils.openScreen("extn.mobile.home.STSContainer.transferOrderShipmentSummary.TOShipmentSummary", "extn.mobile.editors.ReceiveContainerEditor");
	_scModelUtils.setStringValueAtModelPath("InvokedFrom","ReportScreen",screenInput);
	_scControllerUtils.openScreenInEditor("extn.mobile.home.STSContainer.transferOrderShipmentSummary.TOShipmentSummary", screenInput, null, {}, {}, "extn.mobile.editors.ReceiveContainerEditor");


	},

	extn_intializescreen: function(event, bEvent, ctrl, args)
	{
	var pScreen = _iasUIUtils.getParentScreen(
                this, true);
       var className=_scModelUtils.getStringValueFromPath("className",pScreen);
       if(_scBaseUtils.equals(className,"ReceivedDetailsReportScreen"))
       {
	_scWidgetUtils.hideWidget(this,"cp_ContainerDetails", false);
       //	_scWidgetUtils.hideWidget(this,"extn_containerCP", false);
	_scWidgetUtils.showWidget(this,"extn_PendingContainerLink", true,"");
       }
  	 }
//OMNI-6583 Mobile Store - STS - TO Detailed Receiving Report Screen END
});
});

