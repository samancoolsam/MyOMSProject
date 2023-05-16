scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!extn/order/customerAppeasement/CustomerAppeasementSelectionExtnUI", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/PlatformUIFmkImplUtils", "scbase/loader!isccs/utils/WidgetUtils", "scbase/loader!isccs/utils/UIUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/GridxUtils"],
	function (
		_dojodeclare,
		_extnCustomerAppeasementSelectionExtnUI,
		_scScreenUtils,
		_scModelUtils,
		_scBaseUtils,
		_scPlatformUIFmkImplUtils,
		_isccsWidgetUtils,
		_isccsUIUtils,
		_scEventUtils,
		_scGridxUtils

	) {
		return _dojodeclare("extn.order.customerAppeasement.CustomerAppeasementSelectionExtn", [_extnCustomerAppeasementSelectionExtnUI], {
			// custom code here
			extn_SetAppeasementReasonCodes: function (event, bEvent, ctrl, args) {
				var reasonCodeModel = _scScreenUtils.getModel(this, "getCustomerAppeasementReasonCodes_output");
				var reasonCodes = _scModelUtils.getModelListFromPath("CommonCodeList.CommonCode", reasonCodeModel);
				for (var index in reasonCodes) {
					var vCodeValue = _scModelUtils.getStringValueFromPath("CodeValue", reasonCodes[index]);
					var vCodeShortDesc = _scModelUtils.getStringValueFromPath("CodeShortDescription", reasonCodes[index]);
					var vCodeLongDesc = _scModelUtils.getStringValueFromPath("CodeLongDescription", reasonCodes[index]);
					var vReasonCodeupdated = vCodeValue + "_" + vCodeShortDesc + "_" + vCodeLongDesc;
					_scModelUtils.setStringValueAtModelPath("CodeShortDescription", vReasonCodeupdated, reasonCodes[index]);
				}
				_scScreenUtils.setModel(this, "getCustomerAppeasementReasonCodes_output", reasonCodeModel, null);
				this.initializeScreen(event, bEvent, ctrl, args);
				_isccsWidgetUtils.resizeWidget(this, "cmbReasoncode");
			},

			extn_validateAppeasementOnOrder: function (event, bEvent, ctrl, args) {
				var model = args;
				var userid = _scPlatformUIFmkImplUtils.getUserId();
				var selectedreasconCodeModel = _scBaseUtils.getTargetModel(this, "getAppeasementProp_input");
				var selectedReaseCodeValue = _scModelUtils.getStringValueFromPath("Order.AppeasementReason.ReasonCode", selectedreasconCodeModel);
				var reasonCodesModel = _scScreenUtils.getModel(this, "getCustomerAppeasementReasonCodes_output");
				var reasonCodesList = _scModelUtils.getModelListFromPath("CommonCodeList.CommonCode", reasonCodesModel);
				var vCodeLongDesc = "";
				var selectedreasonCode = _scModelUtils.getModelFromListByValue(reasonCodesList, "CodeValue", selectedReaseCodeValue);
				vCodeLongDesc = _scModelUtils.getStringValueFromPath("CodeLongDescription", selectedreasonCode);
				var errorFlag = this.extn_validateOrderLineStatus(event, bEvent, ctrl, args);
				if(errorFlag)
				{
				  return true;
				}

				if (_scBaseUtils.equals(vCodeLongDesc,'Shipping')) {
					var flag = this.extn_validateWhiteGloveAndShippingLines(event, bEvent, ctrl, args);
					if(flag)
					{
					  return true;
					}
					
				}
				var existingAppeasementListModel = _scScreenUtils.getModel(this, "extn_getExistingAppeasementList_output");
				var existingAppeasementList = _scModelUtils.getModelListFromPath("ACADOrderAppeasementList.ACADOrderAppeasement", existingAppeasementListModel);
				var hasMerchandise = false;
				var hasShipping = false;
				var shippingAppeasementAmount = 0;
				for (var j in existingAppeasementList) {
					var useraddedAppeasement = _scModelUtils.getStringValueFromPath("UserId", existingAppeasementList[j]);
					var appeasementReason = _scModelUtils.getStringValueFromPath("AppeasementReason", existingAppeasementList[j]);
					if (_scBaseUtils.equals(useraddedAppeasement, userid)) {
						var appeasementType = _scModelUtils.getStringValueFromPath("Order.AppeasementType", selectedreasconCodeModel);
						if (_scBaseUtils.equals(appeasementReason, 'Merchandise') && _scBaseUtils.equals(appeasementReason, vCodeLongDesc)) {
							if (_scBaseUtils.equals(appeasementType, "01")) {
								
								hasMerchandise = true;
							}
							var appeaseOrderListScreenObj = _scScreenUtils.getChildScreen(this, "customerAppeasementOrderLineList");
							var selectedOrderLineList = _scGridxUtils.getSelectedTargetRecordsUsingUId(appeaseOrderListScreenObj, "OLST_listGrid");
							var selectOrderLineList = _scModelUtils.getModelListFromPath("OrderLineList.OrderLine", selectedOrderLineList);
							for (var o in selectOrderLineList) {
								var selectedOrderlinekey = _scModelUtils.getStringValueFromPath("OrderLineKey", selectOrderLineList[o]);
								var AcademyOrderLineKey = _scModelUtils.getStringValueFromPath("AcademyOrderLineKey", existingAppeasementList[j]);
								if (_scBaseUtils.equals(selectedOrderlinekey, AcademyOrderLineKey)) {									
										hasMerchandise = true; 
										break;									
									
								}

							}
							continue;
						} else if (_scBaseUtils.equals(appeasementReason, 'Shipping') && _scBaseUtils.equals(appeasementReason, vCodeLongDesc)) {
							var AppeasementPercent = _scModelUtils.getStringValueFromPath("AppeasementPercent", existingAppeasementList[j]);
							var appeasementType = _scModelUtils.getStringValueFromPath("Order.AppeasementType", selectedreasconCodeModel);
							if (_scBaseUtils.equals(appeasementType, "01")) {
								if (_scBaseUtils.equals(AppeasementPercent, '100.00')) {
									hasShipping = true;
									continue;
								}
							}
							var appeaseOrderListScreenObj = _scScreenUtils.getChildScreen(this, "customerAppeasementOrderLineList");
							var selectedOrderLineList = _scGridxUtils.getSelectedTargetRecordsUsingUId(appeaseOrderListScreenObj, "OLST_listGrid");
							var selectOrderLineList = _scModelUtils.getModelListFromPath("OrderLineList.OrderLine", selectedOrderLineList);
							for (var o in selectOrderLineList) {
								var selectedOrderlinekey = _scModelUtils.getStringValueFromPath("OrderLineKey", selectOrderLineList[o]);
								var AcademyOrderLineKey = _scModelUtils.getStringValueFromPath("AcademyOrderLineKey", existingAppeasementList[j]);
								if (_scBaseUtils.equals(selectedOrderlinekey, AcademyOrderLineKey)) {
									if (_scBaseUtils.equals(AppeasementPercent, '100.00')) {
										hasShipping = true;
										break;
									}
								}

							}
							continue;

						}

					}
				}
				if (hasMerchandise && _scBaseUtils.equals(vCodeLongDesc, "Merchandise")) {
					var warningString = "An appeasement of category " + vCodeLongDesc + " has already been given on the Order by this user. Please select a different line or a different appeasement category.";
					var textObj = null;
					textObj = {};
					var textOK = null;
					textOK = _scScreenUtils.getString(this, "OK");
					textObj["OK"] = textOK;
					_scScreenUtils.showErrorMessageBox(this, warningString, "waringCallback", textObj, null);
					_scEventUtils.stopEvent(bEvent);
				} else if (hasShipping && _scBaseUtils.equals(vCodeLongDesc, "Shipping")) {
					var warningString = "An appeasement of category " + vCodeLongDesc + " has already been given on the Order. Please select a different line or a different appeasement category.";
					var textObj = null;
					textObj = {};
					var textOK = null;
					textOK = _scScreenUtils.getString(this, "OK");
					textObj["OK"] = textOK;
					_scScreenUtils.showErrorMessageBox(this, warningString, "waringCallback", textObj, null);
					_scEventUtils.stopEvent(bEvent);

				}
			},

			extn_validateOrderLineStatus: function (event, bEvent, ctrl, args) {
				// if(_scBaseUtils.isVoid(appeasementType) && _scBaseUtils.equals(appeasementType,"02"))
				var appeaseOrderListScreenObj = _scScreenUtils.getChildScreen(this, "customerAppeasementOrderLineList");
				var completeOrderLineList = _scScreenUtils.getModel(appeaseOrderListScreenObj, "getCompleteOrderLineList_output");
				var selectedOrderLineList = _scGridxUtils.getSelectedTargetRecordsUsingUId(appeaseOrderListScreenObj, "OLST_listGrid");
				var selectedOrderLinesModel = _scModelUtils.getStringValueFromPath("OrderLineList.OrderLine", selectedOrderLineList);
				var eligibleShipmentLines = 0;
				var isSOFOrder = false;
				//var orderLineListModel= _scScreenUtils.getModel(appeaseOrderListScreenObj, "getCompleteOrderLineList_output");
				var orderLines = _scModelUtils.getStringValueFromPath("OrderLineList.OrderLine", completeOrderLineList);
				var notEligible = false;
				if (_scBaseUtils.isVoid(selectedOrderLineList)) {
					for (var index in orderLines) {
						var lineType = _scModelUtils.getStringValueFromPath("LineType", orderLines[index]);
						if (_scBaseUtils.equals(lineType, 'SOF')) {
							isSOFOrder = true;
						}
						var ShipmentLinesModel = _scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine", orderLines[index]);
						if (_scBaseUtils.isObjectEmpty(ShipmentLinesModel)) {
							notEligible = true;
						}
						for (var j in ShipmentLinesModel) {
							var status = _scModelUtils.getStringValueFromPath("Shipment.0.Status", ShipmentLinesModel[j]);
							
									if (!_scBaseUtils.equals(status, "9000")) {
										// Allowing the appeasment for the order in "Included In Shipment" as we have proforma invoice on Shipment creation
										//var minLineStatus = _scModelUtils.getStringValueFromPath("MinLineStatus", orderLines[index]);
										/*var parsedStatus  =parseFloat(status);
										if (isSOFOrder && parsedStatus  < 1600) {
											notEligible = true;
										} else if (!isSOFOrder && parsedStatus  < 1600.002) {
											notEligible = true;
										} */
										continue;

							} else {
								var maxLineStatus = _scModelUtils.getStringValueFromPath("MaxLineStatus", orderLines[index]);
								if (_scBaseUtils.equals(maxLineStatus, "1300") || _scBaseUtils.equals(maxLineStatus, "9000")||_scBaseUtils.equals(maxLineStatus, "3200")) {
									notEligible = true;
								}
							}

						}
					}
				} else {
					for (var i in selectedOrderLinesModel) {

						var selectedOrderlinekey = _scModelUtils.getStringValueFromPath("OrderLineKey", selectedOrderLinesModel[i]);
						for (var index in orderLines) {
							var orderlineKey = _scModelUtils.getStringValueFromPath("OrderLineKey", orderLines[index]);
							var lineType = _scModelUtils.getStringValueFromPath("LineType", orderLines[index]);
							if (_scBaseUtils.equals(lineType, 'SOF')) {
								isSOFOrder = true;
							}
							if (_scBaseUtils.equals(selectedOrderlinekey, orderlineKey)) {
								var ShipmentLinesModel = _scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine", orderLines[index]);
								if (_scBaseUtils.isObjectEmpty(ShipmentLinesModel)) {
									notEligible = true;
								}
								for (var j in ShipmentLinesModel) {
									var status = _scModelUtils.getStringValueFromPath("Shipment.0.Status", ShipmentLinesModel[j]);
									if (!_scBaseUtils.equals(status, "9000")) {
										//// Allowing the appeasment for the order in "Included In Shipment" as we have proforma invoice on Shipment creation
										//var minLineStatus = _scModelUtils.getStringValueFromPath("MinLineStatus", orderLines[index]);
										/*var parsedStatus  =parseFloat(status);
										if (isSOFOrder && parsedStatus  < 1600) {
											notEligible = true;
										} else if (!isSOFOrder && parsedStatus  < 1600.002) {
											notEligible = true;
										} 
										eligibleShipmentLines++; */
										continue;
									} else {
										var maxLineStatus = _scModelUtils.getStringValueFromPath("MaxLineStatus", orderLines[index]);
										if (_scBaseUtils.equals(maxLineStatus, "1300") || _scBaseUtils.equals(maxLineStatus, "9000")||_scBaseUtils.equals(maxLineStatus, "3200")) {
									      notEligible = true;
							        	}
									}

								}

							}
						}
					}


				}
				if (notEligible) {
					var warningString = "One or more order line(s) have not been shipped. Please select only shipped lines to provide appeasement";
					var textObj = null;
					textObj = {};
					var textOK = null;
					textOK = _scScreenUtils.getString(this, "OK");
					textObj["OK"] = textOK;
					_scScreenUtils.showErrorMessageBox(this, warningString, "waringCallback", textObj, null);
					_scEventUtils.stopEvent(bEvent);
					return true;
				}
			},

			extn_validateWhiteGloveAndShippingLines: function(event, bEvent, ctrl, args) {

				var appeaseOrderListScreenObj = _scScreenUtils.getChildScreen(this, "customerAppeasementOrderLineList");
				var completeOrderLineList = _scScreenUtils.getModel(appeaseOrderListScreenObj, "getCompleteOrderLineList_output");
				var selectedOrderLineList = _scGridxUtils.getSelectedTargetRecordsUsingUId(appeaseOrderListScreenObj, "OLST_listGrid");
				var selectedOrderLinesModel = _scModelUtils.getStringValueFromPath("OrderLineList.OrderLine", selectedOrderLineList);	
				var nonwhiteglovelines = 0;
				var whiteglovelines = 0;
				var shippedlines = 0;
				var nonshippedlines = 0;
			//	var orderLineListModel = orderLineListModel = _scScreenUtils.getModel(appeaseOrderListScreenObj, "getCompleteOrderLineList_output");
			   var orderLines = _scModelUtils.getStringValueFromPath("OrderLineList.OrderLine", completeOrderLineList);
				if (_scBaseUtils.isVoid(selectedOrderLineList)) {
					
					for (var index in orderLines) {
						var MaxLineStatus = _scModelUtils.getStringValueFromPath("MaxLineStatus", orderLines[index]);
						var whiteGloveLine = _scModelUtils.getStringValueFromPath("ItemDetails.Extn.ExtnWhiteGloveEligible", orderLines[index]);
						if (MaxLineStatus >= 3700) {
							shippedlines++;
						} else {
							nonshippedlines++;
						}
						if (_scBaseUtils.equals(whiteGloveLine, 'Y')) {
							whiteglovelines++;
						} else {
							nonwhiteglovelines++;
						}


					}

				} else {
					for (var i in selectedOrderLinesModel) {
						var selectedOrderlinekey = _scModelUtils.getStringValueFromPath("OrderLineKey", selectedOrderLinesModel[i]);
						for (var index in orderLines) {
							var orderlineKey = _scModelUtils.getStringValueFromPath("OrderLineKey", orderLines[index]);
							if (_scBaseUtils.equals(selectedOrderlinekey, orderlineKey)) {
								var MaxLineStatus = _scModelUtils.getStringValueFromPath("MaxLineStatus", orderLines[index]);
								var whiteGloveLine = _scModelUtils.getStringValueFromPath("ItemDetails.Extn.ExtnWhiteGloveEligible", orderLines[index]);
								if (MaxLineStatus >= 3700) {
									shippedlines++;
								} else {
									nonshippedlines++;
								}
								if (_scBaseUtils.equals(whiteGloveLine, 'Y')) {
									whiteglovelines++;
								} else {
									nonwhiteglovelines++;
								}
							}

						}
					}
				}


				if (nonwhiteglovelines > 0 && whiteglovelines > 0) {
					var warningString = "Please select only white-glove lines or only non white-glove lines";
					var textObj = null;
					textObj = {};
					var textOK = null;
					textOK = _scScreenUtils.getString(this, "OK");
					textObj["OK"] = textOK;
					_scScreenUtils.showErrorMessageBox(this, warningString, "waringCallback", textObj, null);
					_scEventUtils.stopEvent(bEvent);
					return true;
				}
				if (shippedlines > 0 && nonshippedlines> 0) {
					var warningString = "You cannot select Shipped and UnShipped lines together";
					var textObj = null;
					textObj = {};
					var textOK = null;
					textOK = _scScreenUtils.getString(this, "OK");
					textObj["OK"] = textOK;
					_scScreenUtils.showErrorMessageBox(this, warningString, "waringCallback", textObj, null);
					_scEventUtils.stopEvent(bEvent);
					return true;
				}


			}


		});
	});
