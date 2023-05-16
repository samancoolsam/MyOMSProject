scDefine([
	"dojo/text!./templates/CustomSearchResult.html",
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
	"scbase/loader!ias/utils/RepeatingScreenUtils"

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
	_iasRepeatingScreenUtils

	
) {
    return _dojodeclare("extn.mobile.home.subscreens.CustomSearchResult", [_scScreen], {
		templateString: templateText,
		uId: "customSearchResultScreen",
		packageName: "extn.mobile.home.subscreens",
		className: "CustomSearchResult",
		title: "Title_SearchResult",
		screen_description: "Custom Search Result Screen",

		 namespaces: {
            targetBindingNamespaces: [


            {
                value: 'getOrderSearch_input',
                description: "The search criteria of order search.."
            }



            ],
            sourceBindingNamespaces: [{
                value: 'getOrderSearch_output',
                description: "The output from getOrderList output init API"
            }]
        },


		staticBindings: [{
        }],
		events: [],
		subscribers: {
			local: [

			{
                eventId: 'afterScreenInit',
                sequence: '25',
                description: 'This methods contains the screen initialization tasks.',
                handler: {
                    methodName: "initializeScreen"
                }
            }, {
                eventId: 'backToSearchLink_onClick',
                sequence: '30',
                description: 'This method is used to go back to Order Search screen',
                listeningControlUId: 'backToSearchLink',
                handler: {
                    methodName: "returnToOrderSearch",
                    description: ""
                }
            },

            {
                eventId: 'afterScreenLoad',
                sequence: '25',
                description: 'Subscriber for after the screen loads',
                handler: {
                    methodName: "afterScreenLoad"
                }
            }

           /* {
                eventId: 'repeatingIdentifierScreen_afterPagingLoad',
                sequence: '25',
                description: 'This event is triggered to call the after page load',
                listeningControlUId: 'repeatingIdentifierScreen',
                handler: {
                    methodName: "hideNavigationForSinglePageResult",
                    className: "RepeatingScreenUtils",
                    packageName: "ias.utils"
                }
            },*/



            ],
		},
		
		// custom code here
        initializeScreen: function(
        event, bEvent, ctrl, args) {


            var orderListModel = null;
            orderListModel = _scScreenUtils.getModel(this, "getOrderList_output");

            var sLoadedFrom = _iasContextUtils.getFromContext("LoadedFrom");

            var sTotalNumberOfRecords = null;

            sTotalNumberOfRecords = _scModelUtils.getModelObjectFromPath("Page.Output.OrderList.TotalNumberOfRecords", orderListModel);

            if ((_scBaseUtils.equals(sTotalNumberOfRecords, "1")) & (_scBaseUtils.equals(sLoadedFrom, "SearchOrder"))){

                    var sOrderHeaderKey = null;

                    var sOrder = _scModelUtils.getModelObjectFromPath("Page.Output.OrderList.Order", orderListModel);
                    
                    sOrderHeaderKey = sOrder[0].OrderHeaderKey;

                    var orderInputModel = null;

                    orderInputModel = _scModelUtils.createNewModelObjectWithRootKey("Order");
                    _scModelUtils.setStringValueAtModelPath("Order.OrderHeaderKey", sOrderHeaderKey, orderInputModel);

                    _wscMobileHomeUtils.openScreenWithInputData("extn.mobile.home.subscreens.CustomOrderDetailsScreen", orderInputModel, "wsc.mobile.editors.MobileEditor");


            }

/*             if ((_scBaseUtils.equals(sTotalNumberOfRecords, "1")) & (_scBaseUtils.equals(sLoadedFrom, "OrderSummary"))){

                this.goToOrderSearchAfterLoad = true;
            }*/


		},



		// 
		afterScreenLoad: function(event, bEvent, ctrl, args) {	
            if(this.goToOrderSearchAfterLoad){

                this.returnToOrderSearch(event, bEvent, ctrl, args);

            }



    	},


        returnToOrderSearch: function(
        event, bEvent, ctrl, args) {

            _wscMobileHomeUtils.openScreenWithInputData("extn.mobile.home.subscreens.CustomSearchOrders", _iasContextUtils.getFromContext("SearchCriteria"), "wsc.mobile.editors.MobileEditor");

        },


       getIdentifierRepeatingScreenData: function(
        shipmentModel, screen, widget, namespace, modelObject) {
            var repeatingScreenId = "extn.mobile.home.subscreens.CustomOrderList";
            var returnValue = null;

            var additionalParamsBean = null;
            additionalParamsBean = {};

            var namespaceMapBean = null;
            namespaceMapBean = {};
            namespaceMapBean["parentnamespace"] = "getOrderList_output";
            namespaceMapBean["childnamespace"] = "Order";
            namespaceMapBean["parentpath"] = "OrderList";
            namespaceMapBean["childpath"] = "Order";
            returnValue = _iasScreenUtils.getRepeatingScreenData(
            repeatingScreenId, namespaceMapBean, additionalParamsBean);
            return returnValue;
        },


        extn_getRepetingScreen: function() {

         var returnValue = null;
		returnValue = _scBaseUtils.getNewBeanInstance();
		_scBaseUtils.addStringValueToBean("repeatingscreenID", "extn.mobile.home.subscreens.CustomOrderList", returnValue);
		var constructorData = null;
		constructorData = _scBaseUtils.getNewBeanInstance();
		_scBaseUtils.addBeanValueToBean("constructorArguments", constructorData, returnValue);
		return returnValue;


        }



			
		


		
		
	});
});