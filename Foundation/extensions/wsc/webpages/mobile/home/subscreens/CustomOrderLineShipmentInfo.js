scDefine([
    "dojo/text!./templates/CustomOrderLineShipmentInfo.html",
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
    return _dojodeclare("extn.mobile.home.subscreens.CustomOrderLineShipmentInfo", [_scScreen], {
        templateString: templateText,
        uId: "customOrderLineShipmentInfoScreen",
        packageName: "extn.mobile.home.subscreens",
        className: "CustomOrderLineShipmentInfo",
        title: "Title_OrderLineShipmentInfo",
        screen_description: "Custom Order Line Shipment Information Screen",

        namespaces: {


             targetBindingNamespaces: [


           
            ],

             sourceBindingNamespaces: [

        

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

           


            ],
        },
        
        // custom code here
        initializeScreen: function(event, bEvent, ctrl, args) {

        },

        getDeliveryMethod: function(dataValue, screen, widget, namespace, modelObj, options) {
            
            var deliveryMethod = _scModelUtils.getStringValueFromPath("DeliveryMethod", modelObj);
            var sFulfillmentType = null;

            var extn_OrderLineInputModel = sc.plat.dojo.utils.ScreenUtils.getModel(this.ownerScreen, "eComOrder_OrderLine_input");
			var sSelectedOrderLineKey = _scModelUtils.getModelObjectFromPath("OrderLine.OrderLineKey", extn_OrderLineInputModel);
            var arrayShipmentLine = _scModelUtils.getModelObjectFromPath("ShipmentLines.ShipmentLine", modelObj);
            var arrayShipmentLineLength = Object.keys(arrayShipmentLine).length;
            for (var iCount = 0; iCount < arrayShipmentLineLength; iCount++) {
                    var ShipmentLineObj = arrayShipmentLine[iCount];
                    var sOrderLineKey = _scModelUtils.getModelObjectFromPath("OrderLineKey", ShipmentLineObj);
                    if(_scBaseUtils.equals(sSelectedOrderLineKey, sOrderLineKey)) {
                        sFulfillmentType=  _scModelUtils.getModelObjectFromPath("OrderLine.FulfillmentType", ShipmentLineObj);
                    }
            }     
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

        getShipmentLineQuantity : function(dataValue, screen, widget, namespace, modelObj, options) {
            var extn_OrderLineInputModel = sc.plat.dojo.utils.ScreenUtils.getModel(this.ownerScreen, "eComOrder_OrderLine_input");
			var sSelectedOrderLineKey = _scModelUtils.getModelObjectFromPath("OrderLine.OrderLineKey", extn_OrderLineInputModel);
            var arrayShipmentLine = _scModelUtils.getModelObjectFromPath("ShipmentLines.ShipmentLine", modelObj);
            var arrayShipmentLineLength = Object.keys(arrayShipmentLine).length;
            for (var iCount = 0; iCount < arrayShipmentLineLength; iCount++) {
                    var ShipmentLineObj = arrayShipmentLine[iCount];
                    var sOrderLineKey = _scModelUtils.getModelObjectFromPath("OrderLineKey", ShipmentLineObj);
                    if(_scBaseUtils.equals(sSelectedOrderLineKey, sOrderLineKey)) {
                        return _scModelUtils.getModelObjectFromPath("OriginalQuantity", ShipmentLineObj);
                    }
            }          
        },

        getShipmentTrackingNo: function(dataValue, screen, widget, namespace, modelObj, options) {
            var ContainersList = _scModelUtils.getModelObjectFromPath("Containers.Container", modelObj);
            var strTrackingNo = "";
            var arr=[];
            var strNew = "";
            if (!_scBaseUtils.isVoid(ContainersList)) {
                for(var i=0;i<ContainersList.length;i++){
                    strTrackingNo+= ContainersList[i].TrackingNo + "| ";
                    arr.push(ContainersList[i].TrackingNo);
                    strNew=arr.join('| ');
                }
            }
            return strNew;
        }


        
        
    });
});
