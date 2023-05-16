scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!extn/order/customerAppeasement/CustomerAppeasementOptionsExtnUI", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!isccs/utils/UIUtils", "scbase/loader!isccs/utils/ModelUtils", "scbase/loader!isccs/utils/OrderUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!isccs/utils/WidgetUtils", "scbase/loader!sc/plat/dojo/utils/PlatformUIFmkImplUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!isccs/utils/WizardUtils"],
	function (
		_dojodeclare,
		_extnCustomerAppeasementOptionsExtnUI,
		_scModelUtils,
		_scBaseUtils,
		_isccsUIUtils,
		_isccsModelUtils,
		_isccsOrderUtils,
		_scScreenUtils,
		_scWidgetUtils,
		_isccsWidgetUtils,
		_scPlatformUIFmkImplUtils,
		_scEventUtils,
		_isccsWizardUtils

	) {
		return _dojodeclare("extn.order.customerAppeasement.CustomerAppeasementOptionsExtn", [_extnCustomerAppeasementOptionsExtnUI], {
			// custom code here
			extn_handleBehaviorMashupInput: function (event, bEvent, crtl, args) {
				var mashuprefsList = _scModelUtils.getModelListFromPath("mashupRefs", args);
				for (var index in mashuprefsList) {
					var mashupRefID = _scModelUtils.getStringValueFromPath("mashupRefId", mashuprefsList[index]);
					if (_scBaseUtils.equals(mashupRefID, "getAppeasementOffersUE")) {
						var reasonDesc = _scModelUtils.getStringValueFromPath("AppeasementReason.ReasonDescription", _isccsUIUtils.getWizardModel(
							this, "appeaseReason_desc"));
						var reasonCodes = reasonDesc.split("_");
						var codeLongDesc = reasonCodes[2];
						_scModelUtils.setStringValueAtModelPath("Order.AppeasementReason.CodeLongDescription", codeLongDesc, mashuprefsList[index].mashupInputObject);
						var appeaseOrderUEModel = null;
						appeaseOrderUEModel = _isccsUIUtils.getWizardModel(this, "appeaseOrderUE_input");
						var PaymentModel = _scModelUtils.getStringValueFromPath("InvokeUE.XMLData.AppeasementOffers.Order.PaymentMethods.PaymentMethod", appeaseOrderUEModel);
						var vPreventImmAppeasementtForGCTender = "N";
						for (var j in PaymentModel) {
							var paymentType = _scModelUtils.getStringValueFromPath("PaymentType", PaymentModel[j]);
							if (_scBaseUtils.equals(paymentType, "GIFT_CARD")) {
								vPreventImmAppeasementtForGCTender = "Y";
								break;
							}
						}
						_scModelUtils.setStringValueAtModelPath("Order.AppeasementReason.PreventImmedeateAppeasementFromWC", vPreventImmAppeasementtForGCTender, mashuprefsList[index].mashupInputObject)
					}
					if (_scBaseUtils.equals(mashupRefID, "changeOrder")) {
						var appeaseOrderUEModel = null;
						appeaseOrderUEModel = _isccsUIUtils.getWizardModel(this, "appeaseOrderUE_input");
						appeaseOrderUEModel = _isccsUIUtils.getWizardModel(this, "appeaseOrderUE_input");
						var PaymentModel = _scModelUtils.getStringValueFromPath("InvokeUE.XMLData.AppeasementOffers.Order.PaymentMethods.PaymentMethod", appeaseOrderUEModel);
						/*for (var j in PaymentModel) {
							var paymentType = _scModelUtils.getStringValueFromPath("PaymentType", PaymentModel[j]);
							if (_scBaseUtils.equals(paymentType, "GIFT_CARD")) {
								hasGiftCard = "Y";
								break;
							}
						} */
						var selectedAppeasementOffer = this.getSelectedAppeasementOffer();
						var hasGiftCard = _scModelUtils.getStringValueFromPath("OfferType", selectedAppeasementOffer);
						_scModelUtils.setStringValueAtModelPath("Order.OrderNo", _scModelUtils.getStringValueFromPath("InvokeUE.XMLData.AppeasementOffers.Order.OrderNo", appeaseOrderUEModel), mashuprefsList[index].mashupInputObject)
						if (_scBaseUtils.equals(hasGiftCard, "PERCENT_ORDER")) {
						_scModelUtils.setStringValueAtModelPath("Order.IsGiftCard", "N", mashuprefsList[index].mashupInputObject);
						var orderlinelist = _scModelUtils.getModelListFromPath("OrderLines.OrderLine",selectedAppeasementOffer);
						 _scModelUtils.setStringValueAtModelPath("Order.Notes.Note.ContactUser", _scPlatformUIFmkImplUtils.getUserId(), mashuprefsList[index].mashupInputObject);
						 var ACADOrderAppeasementList= _scBaseUtils.getNewArrayInstance();
						 var changeOrderLinesModel =  _scBaseUtils.getNewArrayInstance();
						for(var j in orderlinelist)
						{
						var ACADOrderAppeasementModel = _scBaseUtils.getNewModelInstance();
						_scModelUtils.setStringValueAtModelPath("AcademyOrderHeaderKey", _scModelUtils.getStringValueFromPath("Order.OrderHeaderKey", mashuprefsList[index].mashupInputObject), ACADOrderAppeasementModel);
						_scModelUtils.setStringValueAtModelPath("AcademyOrderLineKey", _scModelUtils.getStringValueFromPath("OrderLineKey", orderlinelist[j]), ACADOrderAppeasementModel);
						_scModelUtils.setStringValueAtModelPath("AppeasementAmount", _scModelUtils.getStringValueFromPath("LineOfferAmount",  orderlinelist[j]), ACADOrderAppeasementModel);
						var reasoncodeModel = _isccsUIUtils.getWizardModel(this, "appeaseReason_desc");
						var reasonDesc = _scModelUtils.getStringValueFromPath("AppeasementReason.ReasonDescription",reasoncodeModel);
						var reasonCode=  _scModelUtils.getStringValueFromPath("InvokeUE.XMLData.AppeasementOffers.Order.AppeasementReason.ReasonCode",appeaseOrderUEModel);
						_scModelUtils.setStringValueAtModelPath("AppeasementDescription", reasonDesc, ACADOrderAppeasementModel);
						_scModelUtils.setStringValueAtModelPath("AppeasementPercent", _scModelUtils.getStringValueFromPath("DiscountPercent", selectedAppeasementOffer), ACADOrderAppeasementModel);
						_scModelUtils.setStringValueAtModelPath("AppeasementReason", _scModelUtils.getStringValueFromPath("ChargeName", orderlinelist[j]), ACADOrderAppeasementModel);
						_scModelUtils.setStringValueAtModelPath("OfferType",hasGiftCard,ACADOrderAppeasementModel);
						_scModelUtils.setStringValueAtModelPath("OrderNumber", _scModelUtils.getStringValueFromPath("Order.OrderNo", mashuprefsList[index].mashupInputObject), ACADOrderAppeasementModel);
						_scModelUtils.setStringValueAtModelPath("UserId", _scPlatformUIFmkImplUtils.getUserId(), ACADOrderAppeasementModel);
						_scModelUtils.setStringValueAtModelPath("UserName", _scPlatformUIFmkImplUtils.getUserName(),ACADOrderAppeasementModel);
						 _scModelUtils.addModelObjectToModelList(ACADOrderAppeasementModel, ACADOrderAppeasementList);
						 var changeOrderLineModel = _scBaseUtils.getNewModelInstance();
						 _scModelUtils.setStringValueAtModelPath("Action","MODIFY",changeOrderLineModel);
						 var completeorderlinelist = _scModelUtils.getModelListFromPath("InvokeUE.XMLData.AppeasementOffers.Order.OrderLines.OrderLine",appeaseOrderUEModel);
						 var ChargeName = _scModelUtils.getStringValueFromPath("ChargeName", orderlinelist[j]);
						 var existingLineDiscount = 0.0;
						 for(var k in completeorderlinelist)
						 {
						 	var cOrderLineKey = _scModelUtils.getStringValueFromPath("OrderLineKey",completeorderlinelist[k]);
						 	var OrderlineKey = _scModelUtils.getStringValueFromPath("OrderLineKey", orderlinelist[j]);
						 	if(_scBaseUtils.equals(cOrderLineKey,OrderlineKey))
						 	{
						 		_scModelUtils.setStringValueAtModelPath("OrderLineKey",OrderlineKey,changeOrderLineModel);
						 		_scModelUtils.setStringValueAtModelPath("MaxLineStatus",_scModelUtils.getStringValueFromPath("MaxLineStatus", completeorderlinelist[k]),changeOrderLineModel);
						 		_scModelUtils.setStringValueAtModelPath("MinLineStatus",_scModelUtils.getStringValueFromPath("MinLineStatus", completeorderlinelist[k]),changeOrderLineModel);
						 		var linecharges = _scModelUtils.getStringValueFromPath("LineCharges.LineCharge",completeorderlinelist[k]);
						 		for(var l in linecharges)
						 		{
						 			var vChargeName = _scModelUtils.getStringValueFromPath("ChargeName",linecharges[l]);
						 			var ChargeCategory = _scModelUtils.getStringValueFromPath("ChargeCategory",linecharges[l]);
						 			if(_scBaseUtils.equals(ChargeCategory,"CUSTOMER_APPEASEMENT") && _scBaseUtils.equals(ChargeName,vChargeName))
						 			{
						 			       var chargeAmount = _scModelUtils.getNumberValueFromPath("RemainingChargeAmount",linecharges[l]);
                                                                               existingLineDiscount = existingLineDiscount+chargeAmount;
						 			}

						 		}
						 		break;
						 	}
						 	
						 }
						  
						 _scModelUtils.setStringValueAtModelPath("LineCharges.LineCharge.ChargeCategory", _scModelUtils.getStringValueFromPath("ChargeCategory", orderlinelist[j]),changeOrderLineModel); 
						 _scModelUtils.setStringValueAtModelPath("LineCharges.LineCharge.ChargeName", _scModelUtils.getStringValueFromPath("ChargeName", orderlinelist[j]),changeOrderLineModel); 
						 var vLineOfferAmount =  _scModelUtils.getNumberValueFromPath("LineOfferAmount", orderlinelist[j]); 
                                                 var calculatedLineChargeAmount = existingLineDiscount+vLineOfferAmount;
						 _scModelUtils.setStringValueAtModelPath("LineCharges.LineCharge.ChargePerLine", calculatedLineChargeAmount+"",changeOrderLineModel);
						  _scModelUtils.setStringValueAtModelPath("LineCharges.LineCharge.Reference", reasonCode,changeOrderLineModel);  
						 _scModelUtils.addModelObjectToModelList(changeOrderLineModel, changeOrderLinesModel);
						}
						_scModelUtils.setStringValueAtModelPath("Order.Extn.ACADOrderAppeasementList.ACADOrderAppeasement",ACADOrderAppeasementList, mashuprefsList[index].mashupInputObject);
						_scModelUtils.setStringValueAtModelPath("Order.OrderLines.OrderLine",changeOrderLinesModel, mashuprefsList[index].mashupInputObject);
					

						}
					  else if(_scBaseUtils.equals(hasGiftCard, "PERCENT_FUTURE_ORDER"))
						{
						 _scModelUtils.setStringValueAtModelPath("Order.IsGiftCard", "Y", mashuprefsList[index].mashupInputObject);
                                           _scModelUtils.setStringValueAtModelPath("Order.Notes.Note.ContactUser", _scPlatformUIFmkImplUtils.getUserId(), mashuprefsList[index].mashupInputObject);
						}
					}

             if (_scBaseUtils.equals(mashupRefID, "sendFutureOrderCustomerAppeasementUE")) {
             			var inputModel = _scModelUtils.getStringValueFromPath("mashupInputObject",mashuprefsList[index]);
						var appeaseOrderUEModel = null;
						appeaseOrderUEModel = _isccsUIUtils.getWizardModel(this, "appeaseOrderUE_input");
						 var noteModel = null;
                         noteModel = _scScreenUtils.getTargetModel(
                         this, "createNotes_input", null);
                         var reasoncodeModel = _isccsUIUtils.getWizardModel(this, "appeaseReason_desc");
						var reasonDesc = _scModelUtils.getStringValueFromPath("AppeasementReason.ReasonDescription",reasoncodeModel);
						var reasonCode=  _scModelUtils.getStringValueFromPath("InvokeUE.XMLData.AppeasementOffers.Order.AppeasementReason.ReasonCode",appeaseOrderUEModel);
						var orderUEModel = _scBaseUtils.getNewModelInstance();
						var UEOrderLinesModel = _scBaseUtils.getNewArrayInstance();
						 var completeorderlinelist = _scModelUtils.getModelListFromPath("InvokeUE.XMLData.AppeasementOffers.Order.OrderLines.OrderLine",appeaseOrderUEModel);
						 for(var k in completeorderlinelist)
						 {
							var UEOrderLineModel = _scBaseUtils.getNewModelInstance();
							var orderHeaderKey =  _scModelUtils.getStringValueFromPath("OrderHeaderKey",completeorderlinelist[k]);
							var orderLineKey =  _scModelUtils.getStringValueFromPath("OrderLineKey",completeorderlinelist[k]);
							var personInfoShipTo =  _scModelUtils.getStringValueFromPath("PersonInfoShipTo",completeorderlinelist[k]);
							var lineCharge =  _scModelUtils.getStringValueFromPath("LineCharges.LineCharge.0",completeorderlinelist[k]);
							_scModelUtils.setStringValueAtModelPath("LineCharges.LineCharge.ChargeAmount", _scModelUtils.getStringValueFromPath("ChargeAmount", lineCharge),UEOrderLineModel); 
						    _scModelUtils.setStringValueAtModelPath("LineCharges.LineCharge.ChargeCategory", _scModelUtils.getStringValueFromPath("ChargeCategory", lineCharge),UEOrderLineModel);  
						    _scModelUtils.setStringValueAtModelPath("LineCharges.LineCharge.ChargeName", _scModelUtils.getStringValueFromPath("ChargeName", lineCharge),UEOrderLineModel);
						    _scModelUtils.setStringValueAtModelPath("LineCharges.LineCharge.ChargePerLine", _scModelUtils.getStringValueFromPath("ChargePerLine", lineCharge),UEOrderLineModel);
						    _scModelUtils.setStringValueAtModelPath("LineCharges.LineCharge.ChargePerUnit", _scModelUtils.getStringValueFromPath("ChargePerUnit", lineCharge),UEOrderLineModel);
							_scModelUtils.addModelToModelPath("PersonInfoShipTo",personInfoShipTo, UEOrderLineModel);
							_scModelUtils.setStringValueAtModelPath("OrderLineKey",orderLineKey,UEOrderLineModel);
							_scModelUtils.setStringValueAtModelPath("OrderHeaderKey",orderHeaderKey, UEOrderLineModel);
							_scModelUtils.setStringValueAtModelPath("LineOverallTotals.ExtendedPrice", _scModelUtils.getStringValueFromPath("LineOverallTotals.ExtendedPrice", completeorderlinelist[k]),UEOrderLineModel);
							_scModelUtils.setStringValueAtModelPath("LineOverallTotals.UnitPrice", _scModelUtils.getStringValueFromPath("LineOverallTotals.UnitPrice", completeorderlinelist[k]), UEOrderLineModel);
							_scModelUtils.addModelObjectToModelList(UEOrderLineModel, UEOrderLinesModel);
						 }
						 _scModelUtils.setStringValueAtModelPath("Order.OrderLines.OrderLine",UEOrderLinesModel,orderUEModel);
						 var note =  _scModelUtils.getStringValueFromPath("Order.Notes.Note",noteModel);
						_scModelUtils.addModelToModelPath("Order.Notes.Note",note,orderUEModel);
						_scModelUtils.setStringValueAtModelPath("Order.Notes.Note.ContactUser", _scPlatformUIFmkImplUtils.getUserId(), orderUEModel);
						var reasoncodeModel = _isccsUIUtils.getWizardModel(this, "appeaseReason_desc");
						var appeasementDescription = _scModelUtils.getStringValueFromPath("AppeasementReason.ReasonDescription",reasoncodeModel);
						var appeaseCategory=  _scModelUtils.getStringValueFromPath("InvokeUE.XMLData.AppeasementOffers.Order.AppeasementReason.CodeLongDescription",appeaseOrderUEModel);
						_scModelUtils.setStringValueAtModelPath("Order.AppeasementDescription",appeasementDescription, orderUEModel);
						_scModelUtils.setStringValueAtModelPath("Order.AppeasementCategory",appeaseCategory, orderUEModel);
						_scModelUtils.setStringValueAtModelPath("Order.DocumentType", _scModelUtils.getStringValueFromPath("Appeasement.Order.DocumentType", mashuprefsList[index].mashupInputObject), orderUEModel);
						_scModelUtils.setStringValueAtModelPath("Order.EnterpriseCode",_scModelUtils.getStringValueFromPath("Appeasement.Order.EnterpriseCode", mashuprefsList[index].mashupInputObject), orderUEModel);
						_scModelUtils.setStringValueAtModelPath("Order.OrderHeaderKey",_scModelUtils.getStringValueFromPath("Appeasement.Order.OrderHeaderKey", mashuprefsList[index].mashupInputObject), orderUEModel);
						_scModelUtils.setStringValueAtModelPath("Order.OrderNo",_scModelUtils.getStringValueFromPath("Appeasement.Order.OrderNo", mashuprefsList[index].mashupInputObject), orderUEModel);
						var personInfoBillTo =  _scModelUtils.getStringValueFromPath("Appeasement.Order.PersonInfoBillTo", mashuprefsList[index].mashupInputObject);
						_scModelUtils.addModelToModelPath("Order.PersonInfoBillTo",personInfoBillTo, orderUEModel);
						_isccsModelUtils.removeAttributeFromModel("Appeasement.Order",mashuprefsList[index].mashupInputObject);
						var appeasementModel1 =  _scModelUtils.getStringValueFromPath("Appeasement.AppeasementOffer", inputModel);
						_scModelUtils.addModelToModelPath("Appeasement",orderUEModel,mashuprefsList[index].mashupInputObject);
						_scModelUtils.addModelToModelPath("Appeasement.AppeasementOffer",appeasementModel1,mashuprefsList[index].mashupInputObject);
						//_scModelUtils.setStringValueAtModelPath("Appeasement.Order",orderUEModel, mashuprefsList[index].mashupInputObject);




             }
				}
			},

			setNoteDescription: function (
				selectedAppeasementOffer, variableStr) {
				var orderModel = null;
				orderModel = _scModelUtils.getModelObjectFromPath("InvokeUE.XMLData.AppeasementOffers", _isccsUIUtils.getWizardModel(
					this, "appeaseOrderUE_input"));
				var formattedNoteStr = null;
				formattedNoteStr = this.extn_getFormattedOptionString(
					selectedAppeasementOffer, orderModel, "_NoteText", variableStr);
				var appeasementNotesScreenObj = null;
				appeasementNotesScreenObj = _scScreenUtils.getChildScreen(
					this, "OrderNote");
				_scWidgetUtils.setValue(
					appeasementNotesScreenObj, "txtNoteText", formattedNoteStr, false);
				_isccsWidgetUtils.resizeWidget(
					appeasementNotesScreenObj, "txtNoteText");
			},

			extn_getFormattedOptionString: function (
				appeasementOfferModel, orderModel, textType, variableStr) {
				var formtString = "";
				var bundleKeyText = null;
				var inputArray = null;
				var reasonDesc = null;
				reasonDesc = _scModelUtils.getStringValueFromPath("AppeasementReason.ReasonDescription", _isccsUIUtils.getWizardModel(
					this, "appeaseReason_desc"));
				var reasonCode = reasonDesc.split("_");
				reasonDesc = reasonCode[1];
				var firstName = _scModelUtils.getStringValueFromPath("Order.PersonInfoBillTo.FirstName", orderModel);
				var lastName = _scModelUtils.getStringValueFromPath("Order.PersonInfoBillTo.LastName", orderModel);
				formtString = firstName + " " + lastName;
				formtString = _scBaseUtils.stringConcat(formtString, " requested appeasement because of ");
				formtString = _scBaseUtils.stringConcat(formtString, reasonDesc);
				formtString = _scBaseUtils.stringConcat(formtString, ". An appeasement of ");
				var strDesc = null;
				strDesc = _scModelUtils.getStringValueFromPath("Description", appeasementOfferModel);
				formtString = _scBaseUtils.stringConcat(formtString, strDesc);
				formtString = _scBaseUtils.stringConcat(formtString, " was given.");
				return formtString;
			},

			formatOptionsLabels: function (
				modelOutput) {
				var appeasementOfferList = null;
				appeasementOfferList = _scModelUtils.getModelListFromPath("InvokeUE.XMLData.AppeasementOffers.AppeasementOffer", modelOutput);
				var orderModel = null;
				orderModel = _scModelUtils.getModelObjectFromPath("InvokeUE.XMLData.AppeasementOffers", _isccsUIUtils.getWizardModel(
					this, "appeaseOrderUE_input"));
				for (
					var i = 0; i < _scBaseUtils.getAttributeCount(
						appeasementOfferList); i = i + 1) {
					var appeasementOfferModel = null;
					appeasementOfferModel = appeasementOfferList[
						i];
					var formattedOptStr = null;
					formattedOptStr = this.getFormattedOptionString(
						appeasementOfferModel, orderModel, "_OfferText", "");
					var offerType = _scModelUtils.getStringValueFromPath("OfferType", appeasementOfferModel);
					if (_scBaseUtils.equals(offerType, "PERCENT_ORDER")) {
						var offerAmount = _scModelUtils.getStringValueFromPath("OfferAmount", appeasementOfferModel);
						formattedOptStr = formattedOptStr + " ($" + offerAmount + ")";
					}
					else if(_scBaseUtils.equals(offerType, "PERCENT_FUTURE_ORDER"))
					{
						formattedOptStr = formattedOptStr + " via Gift Card";
						formattedOptStr = formattedOptStr.replace("order","Order");
					}

					_scModelUtils.setStringValueAtModelPath("Description", formattedOptStr, appeasementOfferModel);
				}
			},
			save: function(event, bEvent, ctrl, args) {
            var selectedAppeasementOffer = null;
            selectedAppeasementOffer = this.getSelectedAppeasementOffer();
            selectedAppeasementOffer = _scBaseUtils.cloneModel(
            selectedAppeasementOffer);
            var selectedAppeasementAmount= _scModelUtils.getNumberValueFromPath("OfferAmount",selectedAppeasementOffer);
            if (!(
            _scBaseUtils.isVoid(
            selectedAppeasementOffer))) {
             if(_scBaseUtils.equals(selectedAppeasementAmount,0))
               {
					var warningString = "Appeasement amount is $0. You cannot provide an appeasement for $0 amount";
					var textObj = null;
					textObj = {};
					var textOK = null;
					textOK = _scScreenUtils.getString(this, "OK");
					textObj["OK"] = textOK;
					_scScreenUtils.showErrorMessageBox(this, warningString, "waringCallback", textObj, null);
					_scEventUtils.stopEvent(bEvent);
               }
             else
			{
                var noteModel = null;
                noteModel = _scScreenUtils.getTargetModel(
                this, "createNotes_input", null);
                var appeasementModel = null;
                appeasementModel = _scScreenUtils.getTargetModel(
                this, "getAppeasementUE_input", null);
                _scModelUtils.addModelToModelPath("Appeasement.AppeasementOffer", selectedAppeasementOffer, appeasementModel);
                var orderModel = null;
                orderModel = _scModelUtils.getModelObjectFromPath("InvokeUE.XMLData.AppeasementOffers.Order", _isccsUIUtils.getWizardModel(
                this, "appeaseOrderUE_input"));
                _scModelUtils.setStringValueAtModelPath("Order.ModificationReasonCode", _scModelUtils.getStringValueFromPath("AppeasementReason.ReasonCode", orderModel), noteModel);
                _scModelUtils.addModelToModelPath("Appeasement.Order", orderModel, appeasementModel);
                var offerType = null;
                offerType = _scModelUtils.getStringValueFromPath("Appeasement.AppeasementOffer.OfferType", appeasementModel);
                var isFuture = null;
                isFuture = _scModelUtils.getStringValueFromPath("Appeasement.AppeasementOffer.IsFuture", appeasementModel);
                _scModelUtils.setStringValueAtModelPath("Order.OrderHeaderKey", _scModelUtils.getStringValueFromPath("Appeasement.Order.OrderHeaderKey", appeasementModel), noteModel);
                var mashupRefIdList = null;
                mashupRefIdList = [];
                var mashupInputModelList = null;
                mashupInputModelList = [];
                mashupInputModelList.push(
                appeasementModel);
                mashupInputModelList.push(
                noteModel);
                if (
                _scBaseUtils.equals(
                isFuture, "Y")) {
                    var isVariable = null;
                    isVariable = _scModelUtils.getStringValueFromPath("Appeasement.AppeasementOffer.IsVariable", appeasementModel);
                    if (
                    _scBaseUtils.equals(
                    isVariable, "Y")) {
                        _scModelUtils.setStringValueAtModelPath("Appeasement.AppeasementOffer.Description", _scScreenUtils.getString(
                        this, "FutureOrderDesc"), appeasementModel);
                    }
                    mashupRefIdList.push("sendFutureOrderCustomerAppeasementUE");
                } else {
                    mashupRefIdList.push("recordInvoiceCreation");
                }
                mashupRefIdList.push("changeOrder");
                _isccsUIUtils.callApis(
                this, mashupInputModelList, mashupRefIdList, null, null);
            }
          }
        },
		    //OMNI-4297: Customer Care: CSA group Appeasement Options chnages - Start
		    handleMashupOutput: function(
        mashupRefId, modelOutput, mashupInput, mashupContext) {
            if (
            _scBaseUtils.equals(
            mashupRefId, "getAppeasementOffersUE")) {
                if (
                _scBaseUtils.getAttributeCount(
                _scModelUtils.getModelListFromPath("InvokeUE.XMLData.AppeasementOffers.AppeasementOffer",  modelOutput)) > 0) {
					          //OMNI-4297: Added below part - Start
                    _isccsWizardUtils.enableConfirm(
                    _isccsUIUtils.getParentScreen(
                    this, true));
					          //OMNI-4297: End
                    this.formatOptionsLabels(
                    modelOutput);
                    _scScreenUtils.setModel(
                    this, "getAppeasementOffersUE_output", modelOutput, null);
                    this.handleGetAppeasementOffersUE(
                    modelOutput, mashupInput);
                } else {
                    _isccsWizardUtils.disableConfirm(
                    _isccsUIUtils.getParentScreen(
                    this, true));
                }
            } else if (
            _scBaseUtils.equals(
            mashupRefId, "recordInvoiceCreation") || _scBaseUtils.equals(
            mashupRefId, "sendFutureOrderCustomerAppeasementUE")) {
                _scEventUtils.fireEventToParent(
                this, "onSaveSuccess", null);
            }
        }
		    //OMNI-4297: Customer Care: CSA group Appeasement Optuons chnages - End
		});
	});
