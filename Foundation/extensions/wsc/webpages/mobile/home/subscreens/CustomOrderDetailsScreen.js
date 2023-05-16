scDefine([
        "dojo/text!./templates/CustomOrderDetailsScreen.html",
        "scbase/loader!dijit/form/Button",
        "scbase/loader!dojo/_base/declare",
        "scbase/loader!idx/form/TextBox",
        "scbase/loader!idx/layout/ContentPane",
        "scbase/loader!sc/plat/dojo/utils/BaseUtils",
        "scbase/loader!sc/plat/dojo/utils/EventUtils",
        "scbase/loader!sc/plat/dojo/widgets/Label",
        "scbase/loader!sc/plat/dojo/widgets/Link",
        "scbase/loader!sc/plat/dojo/widgets/Screen",
        "scbase/loader!ias/utils/BaseTemplateUtils",
        "scbase/loader!sc/plat/dojo/utils/ModelUtils",
        "scbase/loader!wsc/mobile/home/utils/MobileHomeUtils",
        "scbase/loader!sc/plat/dojo/utils/WidgetUtils",
        "scbase/loader!sc/plat/dojo/utils/ScreenUtils",
        "scbase/loader!ias/utils/ContextUtils",
        "scbase/loader!ias/utils/ScreenUtils",
        "scbase/loader!ias/utils/RepeatingScreenUtils",
        "scbase/loader!ias/utils/UIUtils",
        "scbase/loader!sc/plat/dojo/utils/EditorUtils"

    ],
    function(
        templateText,
        _dijitButton,
        _dojodeclare,
        _idxTextBox,
        _idxContentPane,
        _scBaseUtils,
        _scEventUtils,
        _scLabel,
        _scLink,
        _scScreen,
        _iasBaseTemplateUtils,
        _scModelUtils,
        _wscMobileHomeUtils,
        _scWidgetUtils,
        _scScreenUtils,
        _iasContextUtils,
        _iasScreenUtils,
        _iasRepeatingScreenUtils,
        _iasUIUtils,
        _EditorUtils



    ) {
        return _dojodeclare("extn.mobile.home.subscreens.CustomOrderDetailsScreen", [_scScreen], {
            templateString: templateText,
            uId: "customOrderDetailsScreen",
            packageName: "extn.mobile.home.subscreens",
            className: "CustomOrderDetailsScreen",
            title: "TITLE_Shipment_Summary",
            screen_description: "Custom Order Details Screen",

            namespaces: {
                targetBindingNamespaces: [

                    {
                        value: 'getOrderDetails_input',
                        description: "The search criteria of shipment search.."
                    }

                ],
                sourceBindingNamespaces: [

                    {
                        value: 'repeatingScreenInput',
                        description: "The list of organizations the user has access to find orders for."
                    },

                    {
                        value: 'getOrderDetails_output',
                        description: "The list of organizations the user has access to find orders for."
                    }



                ]
            },


            staticBindings: [



            ],
            events: [

            {
            name: 'onShow',
            originatingControlUId: 'tpAddressPanel'
            },



            ],



            subscribers: {
                local: [

                    {
                        eventId: 'afterScreenInit',
                        sequence: '10',
                        description: 'This methods contains the screen initialization tasks.',
                        handler: {
                            methodName: "initializeScreen"
                        }
                    },

                    {
                        eventId: 'tpAddressPanel_onShow',
                        sequence: '30',
                        description: 'This subscriber populates the orderlines details screen when the Products titlepane is expanded',
                        handler: {
                            methodName: "titlePaneAddressEvent"
                        }
                    },

                    {
                        eventId: 'tpOrderLineDetails_onShow',
                        sequence: '30',
                        description: 'This subscriber populates the shipmentlines details screen when the Products titlepane is expanded',
                        handler: {
                            methodName: "titlePaneProductsEvent"
                        }
                    },

                    {
                        eventId: 'btnCancelOrder_onClick',
                        sequence: '30',
                        description: 'This subscriber opens popup to  cancel the order',
                        handler: {
                            methodName: "cancelOrder"
                        }
                    },


                    {
                        eventId: 'afterBehaviorMashupCall',
                        sequence: '50',
                        handler: {
                            methodName: "extn_afterBehaviorMashupCall"
                        }
                    },

                    {
                        eventId: 'afterScreenLoad',
                        sequence: '25',
                        description: 'Subscriber for after the screen loads',
                        handler: {
                            methodName: "extn_afterScreenLoad"
                        }
                    },


                ],
            },

            // custom code here
            initializeScreen: function(
                event, bEvent, ctrl, args) {
                _iasContextUtils.addToContext("LoadedFrom", "OrderSummary");
                var orderDetailsModel = null;

                orderDetailsModel = _scScreenUtils.getModel(this, "getOrderDetails_output");

                _scScreenUtils.setModel(this, "repeatingScreenInput", orderDetailsModel, null);
                
                var personInfoBillToCountryDesc = null;

                var personInfoBillToCountry = null;

                personInfoBillToCountryDesc = _scModelUtils.getModelObjectFromPath("Order.PersonInfoBillTo.CountryDesc", orderDetailsModel);

                personInfoBillToCountry = _scModelUtils.getModelObjectFromPath("Order.PersonInfoBillTo.Country", orderDetailsModel);

                if ((_scBaseUtils.isVoid(personInfoBillToCountryDesc))) {

                    _scModelUtils.setStringValueAtModelPath("Order.PersonInfoBillTo.CountryDesc", personInfoBillToCountry, orderDetailsModel);

                }

                var personInfoShipToCountryDesc = null;

                var personInfoShipToCountry = null;

                personInfoShipToCountryDesc = _scModelUtils.getModelObjectFromPath("Order.PersonInfoShipTo.CountryDesc", orderDetailsModel);

                personInfoShipToCountry = _scModelUtils.getModelObjectFromPath("Order.PersonInfoShipTo.Country", orderDetailsModel);

                if ((_scBaseUtils.isVoid(personInfoShipToCountryDesc))) {

                    _scModelUtils.setStringValueAtModelPath("Order.PersonInfoShipTo.CountryDesc", personInfoShipToCountry, orderDetailsModel);

                }


                _scScreenUtils.setModel(this, "getOrderDetails_output", orderDetailsModel, null);

                var sMaxOrderStatus = null;
                 sMaxOrderStatus = _scModelUtils.getNumberValueFromPath("Order.MaxOrderStatus", orderDetailsModel);
                
                if (sMaxOrderStatus >= "3700"){

                    _scWidgetUtils.disableWidget(this, "btnCancelOrder", false);

                }
            

            },


            extn_afterScreenLoad: function(event, bEvent, ctrl, args){

                //Code changes to expand Address Tab on screen load - starts
            var eDef = null;
                eDef = {};
            var eArgs = null;
                eArgs = {};
            _scEventUtils.fireEventInsideScreen(this, "tpAddressPanel_onShow", eDef, eArgs);
            //Code changes to expand Address Tab on screen load - ends

            },


            titlePaneAddressEvent: function(event, bEvent, ctrl, args) {

                _scWidgetUtils.showWidget(this, "pnlBillToAddress", false, "");

                _scWidgetUtils.showWidget(this, "pnlShipToAddress", false, "");

            },

            titlePaneProductsEvent: function(event, bEvent, ctrl, args) {

                _scWidgetUtils.showWidget(this, "productsRepeatingScreen", false, "");

            },



            cancelOrder: function(event, bEvent, ctrl, args) {

                var popupParams = null;
                var dialogParams = null;
                popupParams = {};
                dialogParams = {};
                dialogParams["closeCallBackHandler"] = "onCancellationReasonSelection";

                _iasUIUtils.openSimplePopup("extn.mobile.home.subscreens.CustomCancelOrderPopUp", "extn_Title_CancelOrderPopUp", this, popupParams, dialogParams);
              
                /*//BOPIS-1747- BEGIN
                var orderDetailsModel = null;

                    orderDetailsModel = _scScreenUtils.getModel(this, "getOrderDetails_output");

                    var orderNo = null;

                    orderNo = _scModelUtils.getModelObjectFromPath("Order.OrderNo", orderDetailsModel);

                    var reasonCode = null;

                    reasonCode = "Customer Requested- Sterling Web Store";

                    var apiInputForCancel = _scModelUtils.createNewModelObjectWithRootKey("Order");

                    _scModelUtils.setStringValueAtModelPath("Order.OrderNo", orderNo, apiInputForCancel);

                    _scModelUtils.setStringValueAtModelPath("Order.ReasonCode", reasonCode, apiInputForCancel);


                    _iasUIUtils.callApi(this, apiInputForCancel, "cancelOrderFromOrderSummaryScreen", null);

                    //BOPIS-1747- END*/


            },

            onCancellationReasonSelection: function(actionPerformed, model, popupParams) {

                if (!(_scBaseUtils.equals(actionPerformed, "CLOSE"))) {

                    var orderDetailsModel = null;

                    orderDetailsModel = _scScreenUtils.getModel(this, "getOrderDetails_output");

                    var orderNo = null;

                    orderNo = _scModelUtils.getModelObjectFromPath("Order.OrderNo", orderDetailsModel);

                    var reasonCode = null;

                    reasonCode = _scModelUtils.getModelObjectFromPath("Reason.value", model);

                    var apiInputForCancel = _scModelUtils.createNewModelObjectWithRootKey("Order");

                    _scModelUtils.setStringValueAtModelPath("Order.OrderNo", orderNo, apiInputForCancel);

                    _scModelUtils.setStringValueAtModelPath("Order.ReasonCode", reasonCode, apiInputForCancel);


                    _iasUIUtils.callApi(this, apiInputForCancel, "cancelOrderFromOrderSummaryScreen", null);
                }

            },


            extn_afterBehaviorMashupCall: function(event, bEvent, ctrl, args) {
                var mashupArrayList = _scModelUtils.getModelObjectFromPath("mashupArray", args);
                var mashupArrayListLength = Object.keys(mashupArrayList).length;

                for (var iCount = 0; iCount < mashupArrayListLength; iCount++) {
                    var mashupArray = mashupArrayList[iCount];
                    var mashupRefId = _scModelUtils.getModelObjectFromPath("mashupRefId", mashupArray);
                    if (mashupRefId == "cancelOrderFromOrderSummaryScreen") {

                        var mashupOutputObject = _scModelUtils.getModelObjectFromPath("mashupRefOutput", mashupArray);

                        var sResult = _scModelUtils.getStringValueFromPath("Result", mashupOutputObject);

                        if (!(_scBaseUtils.isVoid(sResult))) {
                            var inputModel = _scScreenUtils.getModel(this, "getOrderDetails_output");
                            var sOrderHeaderKey = _scModelUtils.getModelObjectFromPath("Order.OrderHeaderKey", inputModel);
                            var orderInputModel = null;
                            orderInputModel = _scModelUtils.createNewModelObjectWithRootKey("Order");
                            _scModelUtils.setStringValueAtModelPath("Order.OrderHeaderKey", sOrderHeaderKey, orderInputModel);
                            _iasUIUtils.callApi(this, orderInputModel, "getOrderDetailsReload", null);
                            _iasBaseTemplateUtils.displaySingleMessage(this, "Order is Cancelled Successfully.", "information", null, "systemMessagePanelPopup");

                        } else if (!(_scBaseUtils.isVoid(_scModelUtils.getStringValueFromPath("Order", mashupOutputObject)))) {

                            _iasBaseTemplateUtils.displaySingleMessage(this, "There are some errors, order could not be cancelled!.", "error", null, "systemMessagePanelPopup");
                        }
                    }

                    if (mashupRefId == "getOrderDetailsReload") {
                        var mashupOutputObject = _scModelUtils.getModelObjectFromPath("mashupRefOutput", mashupArray);
                        _scScreenUtils.setModel(this, "getOrderDetails_output", mashupOutputObject, null);
                        this.initializeScreen();
                    }

                }
            },


    handleMashupCompletion: function(
        mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
            _iasBaseTemplateUtils.handleMashupCompletion(
            mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
        },


        });
    });