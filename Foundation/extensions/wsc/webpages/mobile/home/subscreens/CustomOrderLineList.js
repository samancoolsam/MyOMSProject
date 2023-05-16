scDefine([
    "dojo/text!./templates/CustomOrderLineList.html",
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
    return _dojodeclare("extn.mobile.home.subscreens.CustomOrderLineList", [_scScreen], {
        templateString: templateText,
        uId: "customOrderLineListScreen",
        packageName: "extn.mobile.home.subscreens",
        className: "CustomOrderLineList",
        title: "Title_SearchResult",
        screen_description: "Custom OrderLineList Screen",

        namespaces: {


             targetBindingNamespaces: [


           
            ],

             sourceBindingNamespaces: [

            {
                value: 'OrderLine',
                description: "Order Line to be shown. This is passed from the parent screen"
            },

          

            ]


        },
        

         staticBindings: [

       

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
                eventId: 'itemdescriptionLink_onClick',
                sequence: '30',
                handler: {
                    methodName: "openLineDetails"
                }
            },

           
            {
                eventId: 'itemImage_onClick',
                sequence: '30',
                handler: {
                    methodName: "openLineDetails"
                }
            },

             {
                eventId: 'lnkVeiwLineDetails_onClick',
                sequence: '30',
                handler: {
                    methodName: "openLineDetails"
                }
            },

            

            {
                eventId: 'itemImage_onLoad',
                sequence: '30',
                listeningControlUId: 'itemImage',
                handler: {
                    methodName: "resetProductAspectRatio",
                    className: "ScreenUtils",
                    packageName: "ias.utils"
                }
            },

           
           


            ],
        },
        
        // custom code here
        initializeScreen: function(
        event, bEvent, ctrl, args) {

            var orderLineSrcModel = null;
            orderLineSrcModel = _scScreenUtils.getModel(
            this, "OrderLine");

            
        },

        openLineDetails: function(event, bEvent, ctrl, args) {

            var orderLineModel = null;
            orderLineModel = _scScreenUtils.getModel(this, "OrderLine");

            _wscMobileHomeUtils.openScreenWithInputData("extn.mobile.home.subscreens.CustomOrderLineDetails", orderLineModel, "wsc.mobile.editors.MobileEditor");


        },


        getDeliveryMethod: function(dataValue, screen, widget, namespace, modelObj, options) {
        var model = _scScreenUtils.getModel(this, "OrderLine");

        var deliveryMethod = _scModelUtils.getStringValueFromPath("DeliveryMethod", model);
        var sFulfillmentType = _scModelUtils.getStringValueFromPath("FulfillmentType", model);
        if(_scBaseUtils.equals(deliveryMethod, "PICK")) {
            var dMethod = "Pick-up"
            return dMethod;
        } 
        else if((_scBaseUtils.equals(deliveryMethod, "SHP")) && (_scBaseUtils.equals(sFulfillmentType, "SOF"))) {
            var dMethod = "Special Order";
            return dMethod;
        }
        else if((_scBaseUtils.equals(deliveryMethod, "SHP")) && (!(_scBaseUtils.equals(sFulfillmentType, "SOF")))) {
            var dMethod = "Ship To Home";
            return dMethod;
        }
    },

     getUOM: function(dataValue, screen, widget, namespace, modelObj, options) {
        if(_scBaseUtils.equals(dataValue, "EACH")) {
            var sUOM = "Each";
            return sUOM;
        }  else {
            return dataValue;
        }
     },
 



        
        
    });
});
