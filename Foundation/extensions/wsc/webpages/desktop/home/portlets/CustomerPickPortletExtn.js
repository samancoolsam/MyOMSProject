
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/desktop/home/portlets/CustomerPickPortletExtnUI"
	,"scbase/loader!ias/utils/EventUtils"
	,"scbase/loader!sc/plat/dojo/utils/WidgetUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnCustomerPickPortletExtnUI
				,_iasEventUtils
				,_scWidgetUtils
){ 
	return _dojodeclare("extn.desktop.home.portlets.CustomerPickPortletExtn", [_extnCustomerPickPortletExtnUI],{
	// custom code here

	//Barcode Scan-able input field recognition - starts
	/*extn_onOrderNoTxtBoxKeyDown_pickUpOrderSearchActionIfPermitted: function(event, bEvent, ctrl, args) {
	  	if (_iasEventUtils.isEnterPressed(event)) {
		    this.pickUpOrderSearchActionIfPermitted(event, bEvent, ctrl, args);
		}
	}, */

	extn_BarcodeIconOnClick_pickUpOrderSearchActionIfPermitted: function(event, bEvent, ctrl, args) {
		    this.pickUpOrderSearchActionIfPermitted(event, bEvent, ctrl, args);
	},
	//Barcode Scan-able input field recognition - ends

	//Barcode Scan-able input field set focus on Home screen init - starts
	extn_initScreenHandler: function(event, bEvent, ctrl, args) {
        _scWidgetUtils.setFocusOnWidgetUsingUid(this, "txtOrderNo");
    }
    //Barcode Scan-able input field set focus on Home screen init - ends
});
});

