
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/desktop/home/portlets/PackPortletExtnUI","scbase/loader!sc/plat/dojo/utils/WidgetUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnPackPortletExtnUI
			,
				_scWidgetUtils
){ 
	return _dojodeclare("extn.desktop.home.portlets.PackPortletExtn", [_extnPackPortletExtnUI],{
	// custom code here

	//Barcode Scan-able input field recognition - starts
	extn_BarcodeIconOnClick_packIfPermitted: function(event, bEvent, ctrl, args) {
		    this.packIfPermitted(event, bEvent, ctrl, args);
	},
	//Barcode Scan-able input field recognition - ends
	
	extn_initScreenHandlerPack: function(event, bEvent, ctrl, args) {
        _scWidgetUtils.setFocusOnWidgetUsingUid(this, "txtOrderNumber");
    }
});
});

