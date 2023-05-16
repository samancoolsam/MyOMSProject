
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/common/address/display/AddressDisplayExtnUI","scbase/loader!isccs/utils/CustomerUtils", "scbase/loader!isccs/utils/SharedComponentUtils", "scbase/loader!isccs/utils/UIUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/EditorUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnAddressDisplayExtnUI
			,
			_isccsCustomerUtils, _isccsSharedComponentUtils, _isccsUIUtils, _scBaseUtils, _scEditorUtils, _scEventUtils, _scModelUtils, _scScreenUtils, _scWidgetUtils
){ 
	return _dojodeclare("extn.common.address.display.AddressDisplayExtn", [_extnAddressDisplayExtnUI],{	
	//Overridden the OOB method to display OrderLine ShipTo address in Summary screen for ShipToHome Orders.
	initializeScreen: function(event, bEvent, ctrl, args) {
            var eAddress = null;
            var isCustomerAddress = false;
            eAddress = _scScreenUtils.getModel(
            this, "Address");
            isCustomerAddress = _scModelUtils.getBooleanValueFromPath("CustomerAdditionalAddress.IsCustomerAddress", eAddress, false);
            if (_scModelUtils.isKeyPresentInModel("CustomerAdditionalAddress", eAddress)) {
                _scScreenUtils.setModel(this, "CustomerAddress", eAddress, null);
                var eNewModel = null;
                eNewModel = _scModelUtils.createNewModelObjectWithRootKey("PersonInfo");
                _scModelUtils.addModelToModelPath("PersonInfo", _scModelUtils.getModelObjectFromPath("CustomerAdditionalAddress.PersonInfo", eAddress), eNewModel);
                _scScreenUtils.setModel(this, "Address", eNewModel, null);
                eAddress = eNewModel;
                _isccsCustomerUtils.addCustomerModelFromParentScreen(this, "Enterprise");
            }
            if (_scModelUtils.getBooleanValueFromPath("PersonInfo.HasMultipleAddresses", eAddress, false)) {
				//OMNI-14551:  Ship to Address in Sterling WCC - Start
				//this.setMultipleAddresses(true);
				var parentScreen = null;
				var sOrderLineCount = 0;
				var sSHPLineCount = 0;
				
                parentScreen = _isccsUIUtils.getParentScreen(this, true);
                var orderDetailsModel = _scScreenUtils.getModel(parentScreen,"getCompleteOrderDetails_output");
				var orderLineList = _scModelUtils.getStringValueFromPath("Order.OrderLines.OrderLine", orderDetailsModel);
				sOrderLineCount = orderLineList.length;

				for(var i in orderLineList){
					var sDeliveryMethod= _scModelUtils.getStringValueFromPath("DeliveryMethod", orderLineList[i]);
					if(_scBaseUtils.equals(sDeliveryMethod,"SHP")){;
						sSHPLineCount += 1;
					}
				}
				if(sOrderLineCount == sSHPLineCount){
					_scModelUtils.setStringValueAtModelPath("Order.PersonInfoShipTo.FirstName", _scModelUtils.getStringValueFromPath("Order.OrderLines.OrderLine.0.PersonInfoShipTo.FirstName", orderDetailsModel), orderDetailsModel);
					_scModelUtils.setStringValueAtModelPath("Order.PersonInfoShipTo.LastName", _scModelUtils.getStringValueFromPath("Order.OrderLines.OrderLine.0.PersonInfoShipTo.LastName", orderDetailsModel), orderDetailsModel);
					_scModelUtils.setStringValueAtModelPath("Order.PersonInfoShipTo.AddressLine1", _scModelUtils.getStringValueFromPath("Order.OrderLines.OrderLine.0.PersonInfoShipTo.AddressLine1", orderDetailsModel), orderDetailsModel);
					_scModelUtils.setStringValueAtModelPath("Order.PersonInfoShipTo.AddressLine2", _scModelUtils.getStringValueFromPath("Order.OrderLines.OrderLine.0.PersonInfoShipTo.AddressLine2", orderDetailsModel), orderDetailsModel);
					_scModelUtils.setStringValueAtModelPath("Order.PersonInfoShipTo.City", _scModelUtils.getStringValueFromPath("Order.OrderLines.OrderLine.0.PersonInfoShipTo.City", orderDetailsModel), orderDetailsModel);
					_scModelUtils.setStringValueAtModelPath("Order.PersonInfoShipTo.State", _scModelUtils.getStringValueFromPath("Order.OrderLines.OrderLine.0.PersonInfoShipTo.State", orderDetailsModel), orderDetailsModel);
					_scModelUtils.setStringValueAtModelPath("Order.PersonInfoShipTo.Country", _scModelUtils.getStringValueFromPath("Order.OrderLines.OrderLine.0.PersonInfoShipTo.Country", orderDetailsModel), orderDetailsModel);					
					_scModelUtils.setStringValueAtModelPath("Order.PersonInfoShipTo.ZipCode", _scModelUtils.getStringValueFromPath("Order.OrderLines.OrderLine.0.PersonInfoShipTo.ZipCode", orderDetailsModel), orderDetailsModel);
					this.setMultipleAddresses(false);
				}else{
					this.setMultipleAddresses(true);
				}
				//OMNI-14551:  Ship to Address in Sterling WCC - End
            }
			
            if (!(
            _scBaseUtils.isVoid(
            this.multiSelect))) {
                if (_scBaseUtils.equals(
                this.multiSelect, "true")) {
                    if (_scBaseUtils.equals(
                    _scModelUtils.getStringValueFromPath("PersonInfo.Selected", eAddress), "Y")) {
                        this.markSelected(true);
                    } else {
                        _scWidgetUtils.showWidget(this, "btnUseThisAddress", true, null);
                    }
                }
            }
            if (
            _scBaseUtils.isVoid(
            _scModelUtils.getModelObjectFromPath("PersonInfo", eAddress))) {
                if (
                _scBaseUtils.equals(
                this.editAddress, "true")) {
                    _scWidgetUtils.setLinkText(
                    this, "lnkEditAddress", "Add_Address", true);
                    _scWidgetUtils.showWidget(
                    this, "pnlMessage", true, null);
                }
            }
            if (
            _scBaseUtils.equals(
            this.editAddress, "true") && _scModelUtils.getBooleanValueFromPath("PersonInfo.EditAddress", eAddress, true)) {
                _scWidgetUtils.showWidget(
                this, "lnkEditAddress", true, "inline");
                _isccsUIUtils.addClassToScreen(
                this, "accentPanel");
            }
            if (
            _scBaseUtils.equals(
            this.deleteAddress, "true")) {
                _scWidgetUtils.showWidget(
                this, "lnkDeleteAddress", true, "inline");
                _scWidgetUtils.showWidget(
                this, "pnlCustomerDefaults", true, "");
            }
            if (
            _scBaseUtils.equals(
            this.showContactInfo, true)) {
                var dayPhone = null;
                dayPhone = _scModelUtils.getStringValueFromPath("PersonInfo.DayPhone", eAddress);
                if (!(
                _scBaseUtils.isVoid(
                dayPhone))) {
                    _scWidgetUtils.showWidget(
                    this, "pnlPhoneHolder", true, null);
                }
                var emailID = null;
                emailID = _scModelUtils.getStringValueFromPath("PersonInfo.EMailID", eAddress);
                if (!(
                _scBaseUtils.isVoid(
                emailID))) {
                    _scWidgetUtils.showWidget(
                    this, "pnlEmailHolder", true, null);
                }
            }
            this.updateAddressTitle();
            if (
            _scBaseUtils.equals(
            isCustomerAddress, true)) {
                _scWidgetUtils.hideWidget(
                this, "lnkDeleteAddress", true);
                _scWidgetUtils.hideWidget(
                this, "lnkEditAddress", true);
                _scWidgetUtils.hideWidget(
                this, "lnkDefaultBillTo", true);
                _scWidgetUtils.hideWidget(
                this, "lnkDefaultShipTo", true);
                _scWidgetUtils.hideWidget(
                this, "lnkDefaultSoldTo", true);
                _scWidgetUtils.hideWidget(
                this, "lblDefaultBillTo", true);
                _scWidgetUtils.hideWidget(
                this, "lblDefaultShipTo", true);
                _scWidgetUtils.hideWidget(
                this, "lblDefaultSoldTo", true);
            }
        }
});
});

