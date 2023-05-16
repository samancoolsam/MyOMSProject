
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/desktop/home/HomeExtnUI","scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!sc/plat/dojo/utils/ResourcePermissionUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/utils/ScreenUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnHomeExtnUI
			,
				_scWidgetUtils
			,
				_scResourcePermissionUtils
				,_scBaseUtils
				,_scScreenUtils
				
){ 
	return _dojodeclare("extn.desktop.home.HomeExtn", [_extnHomeExtnUI],{
	// custom code here
	
	/* This OOB method has been overridden in order to hide the Customer Service Portlet from the Home Screen */
	initializeScreen: function(
        event, bEvent, ctrl, args) {
            this.checkQueryString();
            if (
            _scResourcePermissionUtils.hasPermission("WSC000001")) {
                _scWidgetUtils.showWidget(
                this, "customerPickPortlet", true, null);
                this.showPortlet("customerPickPortlet");
            }
            if (
            _scResourcePermissionUtils.hasPermission("WSC000006") || _scResourcePermissionUtils.hasPermission("WSC000017")) {
                _scWidgetUtils.showWidget(
                this, "backroomPickPortlet", true, null);
                this.showPortlet("backroomPickPortlet");
            }
            if (_scResourcePermissionUtils.hasPermission("WSC000028")) {
                _scWidgetUtils.showWidget(
                this, "pickOrdersPortlet", true, null);
                this.showPortlet("pickOrdersPortlet");
            }
            if (
            _scResourcePermissionUtils.hasPermission("WSC000019")) {
                _scWidgetUtils.showWidget(
                this, "packPortlet", true, null);
                this.showPortlet("packPortlet");
            }
            if (
            _scResourcePermissionUtils.hasPermission("WSC000020")) {
                _scWidgetUtils.showWidget(
                this, "confirmShipmentPortlet", true, null);
                this.showPortlet("confirmShipmentPortlet");
            }
            if (_scResourcePermissionUtils.hasPermission("WSC000031") || _scResourcePermissionUtils.hasPermission("WSC000033")) {
				//The below code has been modified to hide the Customer Service Portlet 
				/*_scWidgetUtils.showWidget(
                this, "orderCapturePortlet", true, null); */
                _scWidgetUtils.hideWidget(
                this, "orderCapturePortlet", true); 
               //this.showPortlet("orderCapturePortlet");
            }
            
        },

	//BOPIS-1574_CR: Single Home Screen refresh button - begin
	extn_refreshHomeScreen_method: function(event, bEvent, ctrl, args) {
		this.reInitializeHomeScreen_OnRefresh(event, bEvent, ctrl, args);
	},

	reInitializeHomeScreen_OnRefresh: function(event, bEvent, ctrl, args) {
		this.checkQueryString();
		if (
		_scResourcePermissionUtils.hasPermission("WSC000001")) {
			_scWidgetUtils.hideWidget(
			this, "customerPickPortlet", true); 

			this.reShowHomeScreenPortlets_OnRefresh("customerPickPortlet");
			_scWidgetUtils.showWidget(
			this, "customerPickPortlet", true, null);
		}
		if (
		_scResourcePermissionUtils.hasPermission("WSC000006") || _scResourcePermissionUtils.hasPermission("WSC000017")) {
			_scWidgetUtils.hideWidget(
			this, "backroomPickPortlet", true);
			this.reShowHomeScreenPortlets_OnRefresh("backroomPickPortlet");
			//_scWidgetUtils.showWidget(
			//this, "backroomPickPortlet", true, null);
		}
		if (_scResourcePermissionUtils.hasPermission("WSC000028")) {
			_scWidgetUtils.hideWidget(
			this, "pickOrdersPortlet", true);
			this.reShowHomeScreenPortlets_OnRefresh("pickOrdersPortlet");
			_scWidgetUtils.showWidget(
			this, "pickOrdersPortlet", true, null);
		}
		if (
		_scResourcePermissionUtils.hasPermission("WSC000019")) {
			_scWidgetUtils.hideWidget(
			this, "packPortlet", true);
			this.reShowHomeScreenPortlets_OnRefresh("packPortlet");
			_scWidgetUtils.showWidget(
			this, "packPortlet", true, null);
		}
		if (
		_scResourcePermissionUtils.hasPermission("WSC000020")) {
			_scWidgetUtils.hideWidget(
			this, "confirmShipmentPortlet", true);
			this.reShowHomeScreenPortlets_OnRefresh("confirmShipmentPortlet");
			_scWidgetUtils.showWidget(
			this, "confirmShipmentPortlet", true, null);
		}                    
    },

	reShowHomeScreenPortlets_OnRefresh: function(refUID) {
		var optionsBean = null;
		optionsBean = {};
		_scBaseUtils.setAttributeValue("shouldCallInitApi", true, optionsBean);

		var controllerUId = refUID+"_controller";
		var controllerWidget = this["uIdMap"][controllerUId];
		controllerWidget._scRendered=null;

		_scScreenUtils.showChildScreen(
		this, refUID, null, "", optionsBean, null);
	}
	//BOPIS-1574_CR: Single Home Screen refresh button - end

});
});

