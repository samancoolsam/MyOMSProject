scDefine([
	"dojo/text!./templates/StoreAddress.html",
	"scbase/loader!dojo/_base/declare",
	"scbase/loader!sc/plat/dojo/widgets/Screen",
	"scbase/loader!sc/plat/dojo/utils/WidgetUtils",
	"scbase/loader!sc/plat/dojo/utils/ScreenUtils",
	"scbase/loader!sc/plat/dojo/utils/BaseUtils",
	"scbase/loader!isccs/utils/UIUtils",
	"scbase/loader!sc/plat/dojo/utils/ModelUtils",
], function (
	templateText,
	_dojodeclare,
	_scScreen,
	_scWidgetUtils,
	_scScreenUtils,
	_scBaseUtils,
	_isccsUIUtils,
	_scModelUtils


) {
	return _dojodeclare("extn.customPopup.StoreAddress", [_scScreen], {
		templateString: templateText,
		uId: "StoreAddress",
		packageName: "extn.customPopup",
		className: "StoreAddress",


		namespaces: {


			targetBindingNamespaces: [


			],

			sourceBindingNamespaces: [

				{
					value: 'getShipNopdeAddress_output',
					description: "This namespace contains the reason code list"
				},

			]


		},


		subscribers: {
			local: [

				{
					eventId: 'afterScreenInit',
					sequence: '30',
					description: 'This method is used to perform screen initialization tasks.',
					handler: {
						methodName: "initializeScreen"
					}
				},

				{
					eventId: 'Popup_btnCancel_onClick',
					sequence: '25',
					description: 'Cancel/Close Button Action',
					listeningControlUId: 'Popup_btnCancel',
					handler: {
						methodName: "onPopupClose",
						description: ""
					}
				},


			]
		},


		initializeScreen: function (event, bEvent, ctrl, args) {

			var screenInput = null;
			screenInput = _scScreenUtils.getInitialInputData(
				this);
			var storeId = _scModelUtils.getStringValueFromPath("ShipNode", screenInput);
			storeId = "Store: " + storeId;
			_scWidgetUtils.setValue(this, "lblStoreID", storeId, false);
			var shipNodeInput = _scBaseUtils.getNewModelInstance();
			_scScreenUtils.setModel(this, "getShipNopdeAddress_output".model1, null);
			var firstName = _scModelUtils.getStringValueFromPath("ShipNodePersonInfo.FirstName", screenInput);
			_scWidgetUtils.setValue(this, "lblName", firstName, false);
			var addressLine1 = _scModelUtils.getStringValueFromPath("ShipNodePersonInfo.AddressLine1", screenInput);
			var addressLine2 = _scModelUtils.getStringValueFromPath("ShipNodePersonInfo.AddressLine2", screenInput);
			var addressLine3 = _scModelUtils.getStringValueFromPath("ShipNodePersonInfo.AddressLine3", screenInput);
			var address1 = addressLine1;
			if ((!_scBaseUtils.isVoid(addressLine2)) || (!_scBaseUtils.isVoid(addressLine3))) {
				if (!_scBaseUtils.isVoid(addressLine2)) {
					address1 += " ," + addressLine2;
				}
				if (!_scBaseUtils.isVoid(addressLine3)) {
					address1 += " ," + addressLine3;
				}

			}
			//var str = "<div> " +firstName +" </div><br/><div> " +addressLine1+"</div>" ;
			_scWidgetUtils.setValue(this, "lblAddressLine1", address1, false);
			var addressLine4 = _scModelUtils.getStringValueFromPath("ShipNodePersonInfo.AddressLine4", screenInput);
			var addressLine5 = _scModelUtils.getStringValueFromPath("ShipNodePersonInfo.AddressLine5", screenInput);
			var addressLine6 = _scModelUtils.getStringValueFromPath("ShipNodePersonInfo.AddressLine6", screenInput);
			var address2 = null;
			if ((!_scBaseUtils.isVoid(addressLine4)) || (!_scBaseUtils.isVoid(addressLine5)) || (!_scBaseUtils.isVoid(addressLine6))) {
				if (!_scBaseUtils.isVoid(addressLine4)) {
					address2 = addressLine4;
				}
				if (!_scBaseUtils.isVoid(addressLine5)) {
					address2 += " ," + addressLine5;
				}
				if (!_scBaseUtils.isVoid(addressLine6)) {
					address2 += " ," + addressLine6;
				}

			}
			_scWidgetUtils.setValue(this, "lblAddressLine2", address2, false);
			var city = _scModelUtils.getStringValueFromPath("ShipNodePersonInfo.City", screenInput);
			var state = _scModelUtils.getStringValueFromPath("ShipNodePersonInfo.State", screenInput);
			var zipCode = _scModelUtils.getStringValueFromPath("ShipNodePersonInfo.ZipCode", screenInput);
			var cityStatezipCode = city + " " + state + " " + zipCode;
			_scWidgetUtils.setValue(this, "lblCityStateZip", cityStatezipCode, false);
			var country = _scModelUtils.getStringValueFromPath("ShipNodePersonInfo.Country", screenInput);
			_scWidgetUtils.setValue(this, "lblCountry", country, false);
			var phoneNo = _scModelUtils.getStringValueFromPath("ShipNodePersonInfo.DayPhone", screenInput);
			_scWidgetUtils.setValue(this, "lblDayPhone", phoneNo, false);

		},

		confirmCancellation: function (event, bEvent, ctrl, args) {
			var selectedReasonCodeModel = null;
			selectedReasonCodeModel = _scScreenUtils.getTargetModel(this, "selectedReasonCode", null);

			this.isApplyClicked = true;
			_scWidgetUtils.closePopup(
				this, "APPLY", false);

		},

		onPopupClose: function (
			event, bEvent, ctrl, args) {
			this.isApplyClicked = false;
			_scWidgetUtils.closePopup(
				this, "CLOSE", false);
		},

		getPopupOutput: function (
			event, bEvent, ctrl, args) {
			//    var getSelectedReason = null;
			//   getSelectedReason = _scBaseUtils.getTargetModel(this, "selectedReasonCode", null);
			//  return getSelectedReason;
		},


	});
});
