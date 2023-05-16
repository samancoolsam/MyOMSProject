scDefine([
	"dojo/text!./templates/CustomOrderLineDetails.html",
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
	_iasUIUtils

	
) {
    return _dojodeclare("extn.mobile.home.subscreens.CustomOrderLineDetails", [_scScreen], {
		templateString: templateText,
		uId: "customOrderLineDetailsScreen",
		packageName: "extn.mobile.home.subscreens",
		className: "CustomOrderLineDetails",
		title: "TITLE_Shipment_Summary",
		screen_description: "Custom Order Line Details Screen",

		 namespaces: {
            targetBindingNamespaces: [

                {
	                value: 'OrderLine',
	                description: "The search criteria of shipment search.."
	            }    

            ],
            sourceBindingNamespaces: [

            		
            		{
		                value: 'OrderLine',
		                description: "The namespace to show OrderLine Details."
		            }

            ]
        },


		staticBindings: [



        ],
		events: [],



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
                eventId: 'extn_tpOL_Overview_onShow',
                sequence: '30',
                description: 'This subscriber populates the OrderLine Information when the Overview titlepane is expanded',
                handler: {
                    methodName: "extn_ShowOrderLineOverview"
                }
            },

            {
                eventId: 'extn_tpOL_ShipmentInfo_onShow',
                sequence: '30',
                description: 'This subscriber populates the OrderLine Information when the Overview titlepane is expanded',
                handler: {
                    methodName: "extn_ShowOrderLineShipmentInfo"
                }
            },




            ],
		},
		
		// custom code here
        initializeScreen: function(event, bEvent, ctrl, args) {

        	//Fetch Order Line input set in Editor from OrderLineList Screen
			var currentOpenEditorOrderLineInput = _scScreenUtils.getInitialInputData(this);
			var eComOrder_OrderLine_input={};
			eComOrder_OrderLine_input.OrderLine=currentOpenEditorOrderLineInput;
			_scScreenUtils.setModel(this, "eComOrder_OrderLine_input", eComOrder_OrderLine_input, null);

            this.dynamicDataLabelList = [];
			//Expand Overview Tab on screen load 
			var eDef = null;
                eDef = {};
            var eArgs = null;
            	eArgs = {};
            _scEventUtils.fireEventInsideScreen(this, "extn_tpOL_Overview_onShow", eDef, eArgs);
            
            //Expand Shipment Information Tab on screen load 
			var eDef = null;
                eDef = {};
            var eArgs = null;
            	eArgs = {};
            _scEventUtils.fireEventInsideScreen(this, "extn_tpOL_ShipmentInfo_onShow", eDef, eArgs);
            
		},

		extn_ShowOrderLineOverview: function(event, bEvent, ctrl, args) {
            //Populate Overview Tab with OrderLine model set from OrderLineList Screen
            var extn_getCompOLDetailsInputModel = null;
            extn_getCompOLDetailsInputModel = _scScreenUtils.getModel(this, "eComOrder_OrderLine_input");
            _scScreenUtils.setModel(this, "eComOrder_getCompleteOrderLineDetails_output", extn_getCompOLDetailsInputModel, null);

            this.extn_displayDynamicOrderLineQtyLabels(extn_getCompOLDetailsInputModel);

        },

        extn_ShowOrderLineShipmentInfo: function(event, bEvent, ctrl, args) {
        	//Create getShipmentListForOrder input to populate Shipment Information Tab
            var extn_getShipmentListInputModel = {};
            var extn_OrderLineInputModel = _scScreenUtils.getModel(this, "eComOrder_OrderLine_input");

            extn_getShipmentListInputModel.OrderLineDetail={};
            extn_getShipmentListInputModel.OrderLineDetail.OrderHeaderKey=extn_OrderLineInputModel.OrderLine.OrderHeaderKey;
            extn_getShipmentListInputModel.OrderLineDetail.OrderLineKey=extn_OrderLineInputModel.OrderLine.OrderLineKey;
            _iasUIUtils.callApi(this, extn_getShipmentListInputModel, "eComOrder_getShipmentListForOrder_Ref", null);
        },

        handleMashupCompletion: function(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
            _iasBaseTemplateUtils.handleMashupCompletion(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
        },

        handleMashupOutput: function(mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
            //Handle getShipmentListForOrder output-to populate shipment list repeating screen
            if (_scBaseUtils.equals(mashupRefId, "eComOrder_getShipmentListForOrder_Ref")) {
                if (!(_scBaseUtils.equals(false, applySetModel))) {
                    if(_scBaseUtils.isVoid(modelOutput.ShipmentList)){
                        _scWidgetUtils.showWidget(this, "noShipmentMsg", true, null);
                    } 
                    else {
                        var sSelectedOrderLineKey =   _scScreenUtils.getInitialInputData(this).OrderLineKey;
                        
                        var arrayShipment = _scModelUtils.getModelObjectFromPath("ShipmentList.Shipment", modelOutput);
                        var arrayShipmentLength = Object.keys(arrayShipment).length;
                        for (var iCount = 0; iCount < arrayShipmentLength; iCount++) {
                            var objShipment = arrayShipment[iCount];
                            _scModelUtils.setStringValueAtModelPath("SelectedOrderLineKey", sSelectedOrderLineKey, objShipment);
                            // fix for BOPIS-1606 : Begin
                            var documentType = _scModelUtils.getStringValueFromPath("DocumentType", objShipment);
                            if(!_scBaseUtils.equals(documentType, "0001")) {
                                arrayShipment.splice(iCount, 1); 
                            }
                            // BOPIS-1606: End
                        }
                    }
                    _scScreenUtils.setModel(this, "eComOrder_getShipmentListForOrder_output", modelOutput, null);
                }
            }
            
        },

        extn_showShipmentRepeatingScreen: function(){
        	//Repeating screen generator method
            var returnValue = null;
            returnValue = _scBaseUtils.getNewBeanInstance();
            _scBaseUtils.addStringValueToBean("repeatingscreenID", "extn.mobile.home.subscreens.CustomOrderLineShipmentInfo", returnValue);
            var constructorData = null;
            constructorData = _scBaseUtils.getNewBeanInstance();
            _scBaseUtils.addBeanValueToBean("constructorArguments", constructorData, returnValue);
            return returnValue;
        },

        dynamicDataLabelList:[],
        uniqueDynamicDataLabelList:[],

        extn_displayDynamicOrderLineQtyLabels: function(modelOutput){

        	//To generate dynamic data labels corresponding to OrderLine Statuses
            if (!_scBaseUtils.isVoid(modelOutput)) {
                if (!(_scBaseUtils.isVoid(modelOutput.OrderLine.OrderStatuses.OrderStatus))) {

                    for (var i = 0; i < modelOutput.OrderLine.OrderStatuses.OrderStatus.length; i++) {

                        var strStatusDescription = modelOutput.OrderLine.OrderStatuses.OrderStatus[i].StatusDescription;
                        var strStatusQty = modelOutput.OrderLine.OrderStatuses.OrderStatus[i].StatusQty;

                        var strDataLabelUID="extn_"+strStatusDescription+"_Lbl";   

						if(_scBaseUtils.isVoid(this.uniqueDynamicDataLabelList)){
							if(!_scBaseUtils.equals(strStatusDescription, "Backordered From Node")){
                            	var strDynamicLabel=strStatusDescription+" Qty: ";

                                var configParams = {
                                    "DataLabel": {
                                        "uId": strDataLabelUID,
                                        "label": strDynamicLabel,
                                        "value": strStatusQty,
                                        "style":"width:1350em; padding-left:50px;"
                                    }
                                };

                                var newDataLabel=_scWidgetUtils.createDataLabel("DataLabel",this,strDataLabelUID,configParams);
                                _scWidgetUtils.placeAt(this, "extn_dynamicDataLabel_cp", newDataLabel, null);

                                 this.dynamicDataLabelList.push(strDataLabelUID);
                         		 this.uniqueDynamicDataLabelList=[new Set(this.dynamicDataLabelList)]; 
                            }
						}
                        else if(!this.uniqueDynamicDataLabelList.includes(strDataLabelUID)){
                            if(!_scBaseUtils.equals(strStatusDescription, "Backordered From Node")){
                            	var strDynamicLabel=strStatusDescription+" Qty: ";

                                var configParams = {
                                    "DataLabel": {
                                        "uId": strDataLabelUID,
                                        "label": strDynamicLabel,
                                        "value": strStatusQty,
                                        "style":"width:90em; padding-left:50px;"
                                    }
                                };

                                var newDataLabel=_scWidgetUtils.createDataLabel("DataLabel",this,strDataLabelUID,configParams);
                                _scWidgetUtils.placeAt(this, "extn_dynamicDataLabel_cp", newDataLabel, null);

                                 this.dynamicDataLabelList.push(strDataLabelUID);
                         		 this.uniqueDynamicDataLabelList=[new Set(this.dynamicDataLabelList)]; 
                            }
                        }
                    }
                }
            }
        },

        getDeliveryMethod: function(dataValue, screen, widget, namespace, modelObj, options) {

            var deliveryMethod = _scModelUtils.getStringValueFromPath("OrderLine.DeliveryMethod", modelObj);
            var sFulfillmentType = _scModelUtils.getStringValueFromPath("OrderLine.FulfillmentType", modelObj);
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
        }
			




			
		


		
		
	});
});