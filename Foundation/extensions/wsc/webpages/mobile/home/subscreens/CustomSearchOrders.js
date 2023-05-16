scDefine([
    "dojo/text!./templates/CustomSearchOrders.html",
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
    "scbase/loader!ias/utils/EventUtils",
    "scbase/loader!ias/utils/UIUtils",

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
    _iasEventUtils,
    _iasUIUtils

    
) {
    return _dojodeclare("extn.mobile.home.subscreens.CustomSearchOrders", [_scScreen], {
        templateString: templateText,
        uId: "customSearchOrdersScreen",
        packageName: "extn.mobile.home.subscreens",
        className: "CustomSearchOrders",
        title: "Title_Search",
        screen_description: "Custom Search Orders Screen",

        namespaces: {


             targetBindingNamespaces: [


            {
                value: 'getOrderSearch_input',
                description: "The search criteria of shipment search.."
            }

            ],

             sourceBindingNamespaces: [

            {
                value: 'SavedSearchCriteria',
                description: "This namespace is used to retain the previously searched criteria."
            },
            {
                value: 'commonCodeModel',
                description: "This namespace is used to store configured days."
            }

            ]


        },
        


        events: [],
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
                eventId: 'afterScreenLoad',
                sequence: '51',
                description: 'This method is used to perform screen initialization tasks.',
                handler: {
                    methodName: "afterScreenLoad"
                }
            },

            {
                eventId: 'btnSearchOrders_onClick',
                sequence: '25',
                description: 'Subscriber to get orders based on the carrier selected',
                listeningControlUId: 'btnSearchOrders',
                handler: {
                    methodName: "searchOrders",
                    description: ""
                }
            },

            {
                eventId: 'txt_SearchOrderNo_onKeyUp',
                sequence: '30',
                description: 'This method is used to handle Key Up event of OrderNo text field.',
                handler: {
                    methodName: "searchOrdersOnEnter"
                }
            },
            {
                eventId: 'txt_SearchCustLastName_onKeyUp',
                sequence: '30',
                description: 'This method is used to handle Key Up event of OrderNo text field.',
                handler: {
                    methodName: "searchOrdersOnEnter"
                }
            },
            {
                eventId: 'txt_SearchCustFirstName_onKeyUp',
                sequence: '30',
                description: 'This method is used to handle Key Up event of OrderNo text field.',
                handler: {
                    methodName: "searchOrdersOnEnter"
                }
            },
            {
                eventId: 'txt_SearchCustEmail_onKeyUp',
                sequence: '30',
                description: 'This method is used to handle Key Up event of OrderNo text field.',
                handler: {
                    methodName: "searchOrdersOnEnter"
                }
            },
             {
                eventId: 'txt_SearchCustPhoneNo_onKeyUp',
                sequence: '30',
                description: 'This method is used to handle Key Up event of OrderNo text field.',
                handler: {
                    methodName: "searchOrdersOnEnter"
                }
            },
            {
                eventId: 'afterBehaviorMashupCall',
                sequence: '32',
                handler: {
                    methodName: "extn_afterBehaviorMashupCall"
                }
            },




            ],
        },
        
        // custom code here
        initializeScreen: function(
        event, bEvent, ctrl, args) {

            var commonCodeModel = null;
            commonCodeModel = _scScreenUtils.getModel(this, "eComOrderSearch_getNumberofDays_output");
            _scScreenUtils.setModel(this, "commonCodeModel", commonCodeModel, null);
            _iasBaseTemplateUtils.displaySingleMessage(this,"Enter at least one criteria for a successful order search!","information",null,"systemMessagePanelPopup");

            var sLoadedFrom = _iasContextUtils.getFromContext("LoadedFrom");

            if (!(_scBaseUtils.equals(sLoadedFrom, "MobileHome"))){

            var savedSearchCriteriaModel = null;
            savedSearchCriteriaModel = _iasContextUtils.getFromContext("SearchCriteria");

            if (!(_scBaseUtils.isVoid(savedSearchCriteriaModel))) {

                _scScreenUtils.setModel(this, "SavedSearchCriteria", savedSearchCriteriaModel, null);

            }
        }
            
        },



        // To focus OrderNO textbox
        afterScreenLoad: function(event, bEvent, ctrl, args) { 



        _scWidgetUtils.setFocusOnWidgetUsingUid(this, "txt_SearchOrderNo");

        },



        // On Click of enter


        searchOrdersOnEnter: function(
        event, bEvent, ctrl, args) {
            if (
            _iasEventUtils.isEnterPressed(event)) {
                this.searchOrders();
            }
        },



        // On Click of Search Order

         searchOrders: function(
        event, bEvent, ctrl, args) {


            var orderSearchCriteriaModel = {};

            orderSearchCriteriaModel.Order = {}; 

            orderSearchCriteriaModel = _scBaseUtils.getTargetModel(this, "getOrderSearch_input", null);

            _scBaseUtils.removeBlankAttributes(orderSearchCriteriaModel);
            
            
            var order = _scModelUtils.getStringValueFromPath("Order", orderSearchCriteriaModel);

            if ((!_scBaseUtils.isVoid(order))) {
                //To add Order date filter
                var commonCodeList =_scScreenUtils.getModel(this, "commonCodeModel");
                var numberOfDays = commonCodeList.CommonCodeList.CommonCode[0].CodeShortDescription;
                _scModelUtils.setStringValueAtModelPath("Order.NumberOfDays", numberOfDays, orderSearchCriteriaModel);

                _iasContextUtils.addToContext("SearchCriteria", orderSearchCriteriaModel);
                _iasContextUtils.addToContext("LoadedFrom", "SearchOrder");

                 _scScreenUtils.setModel(this, "SavedSearchCriteria",orderSearchCriteriaModel,null);


                 /*_iasUIUtils.callApi(this,orderSearchCriteriaModel,"getOrderListOnSearchOrder",null);*/

                _wscMobileHomeUtils.openScreenWithInputData("extn.mobile.home.subscreens.CustomSearchResult", orderSearchCriteriaModel, "wsc.mobile.editors.MobileEditor");


            } else {

                //If Search criteria has not met


            _iasBaseTemplateUtils.displaySingleMessage(this,"Enter at least one criteria for a successful order search!","error",null,"systemMessagePanelPopup");

               

            
            }


         },



       /*  // To Handle After Behaviour Mashup Call

         extn_afterBehaviorMashupCall: function(event, bEvent, ctrl, args) {
            var mashupArrayList = _scModelUtils.getModelObjectFromPath("mashupArray", args);
            var mashupArrayListLength = Object.keys(mashupArrayList).length;

            for(var iCount = 0; iCount < mashupArrayListLength; iCount++) {
                var mashupArray = mashupArrayList[iCount];
                var mashupRefId = _scModelUtils.getModelObjectFromPath("mashupRefId", mashupArray);
                if(mashupRefId == "getOrderListOnSearchOrder") {

                    var mashupOutputObject = _scModelUtils.getModelObjectFromPath("mashupRefOutput", mashupArray);

                    var orderArrayList = _scModelUtils.getModelObjectFromPath("OrderList.Order", mashupOutputObject);

                    var orderArrayListLength = Object.keys(orderArrayList).length;

                     if(orderArrayListLength >= 2){

                        _iasContextUtils.addToContext("OrderList_Input", mashupOutputObject);

                        _wscMobileHomeUtils.openScreenWithInputData("extn.mobile.home.subscreens.CustomSearchResult", mashupOutputObject, "wsc.mobile.editors.MobileEditor");



                     } 



                }
            }


         }*/
            
        


        
        
    });
});
