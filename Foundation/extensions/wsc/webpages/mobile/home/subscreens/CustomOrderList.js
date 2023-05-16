scDefine([
    "dojo/text!./templates/CustomOrderList.html",
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
    "scbase/loader!ias/utils/UIUtils"

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
    return _dojodeclare("extn.mobile.home.subscreens.CustomOrderList", [_scScreen], {
        templateString: templateText,
        uId: "customOrderListScreen",
        packageName: "extn.mobile.home.subscreens",
        className: "CustomOrderList",
        title: "Title_SearchResult",
        screen_description: "Custom OrderList Screen",

        namespaces: {


             targetBindingNamespaces: [

            {
                seq: '1',
                value: 'getOrderDetails_input',
                description: "input model used to make getOrderDetails custom mashup call."
            },



            ],

             sourceBindingNamespaces: [



            {
                value: 'Order',
                description: "The repeating element from the output of the getOrderList api"
            },
            

            ]


        },
        

         staticBindings: [

         {
            targetBinding: {
                path: 'Order.OrderHeaderKey',
                namespace: 'getOrderDetails_input'
            },
            sourceBinding: {
                path: 'Order.OrderHeaderKey',
                namespace: 'Order'
            }
        },



        ],



        events: [

        ],


        subscribers: {
            local: [

            {
                eventId: 'afterScreenInit',
                sequence: '30',
                handler: {
                    methodName: "initializeScreen"
                }
            },



            {
                eventId: 'lnk_VeiwOrderSummary_onClick',
                sequence: '30',
                description: '',
                listeningControlUId: 'lnk_VeiwOrderSummary',
                handler: {
                    methodName: "veiwOrderSummary",
                    description: ""
                }
            },


            {
                eventId: 'pnlDataHolder_onClick',
                sequence: '30',
                description: 'This subscriber loads the ready for packing screen',
                handler: {
                    methodName: "veiwOrderSummary"
                }
            },

            

           


            ],
        },
        
        // custom code here
        initializeScreen: function(
        event, bEvent, ctrl, args) {

           var orderModel = null;
            orderModel = _scScreenUtils.getModel(this, "Order");
/*
            this.initializeListScreen(orderModel);*/
            
        },

/*        initializeListScreen: function(
        orderModel) {
            var widgetUIdBean = null;
            widgetUIdBean = {};
            widgetUIdBean["RecordCustomerPickupWidgetUId"] = "lnk_RecordCustomerPickupAction"
            _wscMobileHomeUtils.showNextTask(
            this, orderModel, "Pick", "lnk_VeiwOrderSummary", "lbl_Status", widgetUIdBean);
        },*/

/*
        getOrderNo: function(
        dataValue, screen, widget, nameSpace, orderModel) {
            var custName = null;
            custName = _wscMobileHomeUtils.buildNameFromShipment(
            this, orderModel);
            return dataValue;
        },*/

/*
         openPickDetails: function(
        event, bEvent, ctrl, args) {
            var shipmentSummaryTargetModel = null;
            shipmentSummaryTargetModel = _scBaseUtils.getTargetModel(
            this, "shipmentSummaryWizard_input", null);
            var editorName = "wsc.mobile.editors.MobileEditor";
            _iasUIUtils.openWizardInEditor("wsc.components.shipment.summary.ShipmentSummaryWizard", shipmentSummaryTargetModel, editorName, this);
        },*/
 


        veiwOrderSummary: function(event, bEvent, ctrl, args){


            var orderModel = null;

            orderModel = _scScreenUtils.getTargetModel(this, "getOrderDetails_input", null);

            _wscMobileHomeUtils.openScreenWithInputData("extn.mobile.home.subscreens.CustomOrderDetailsScreen", orderModel, "wsc.mobile.editors.MobileEditor");

        },
        

        
        
    });
});
