scDefine([
	"dojo/text!./templates/ReceivedDetailsReportScreen.html",
	"scbase/loader!dojo/_base/declare",
	"scbase/loader!sc/plat/dojo/widgets/Screen",
	"scbase/loader!sc/plat/dojo/utils/WidgetUtils",
	"scbase/loader!sc/plat/dojo/utils/ScreenUtils",
	"scbase/loader!sc/plat/dojo/utils/BaseUtils",
	"scbase/loader!ias/utils/UIUtils",
	"scbase/loader!sc/plat/dojo/utils/ModelUtils",
	"scbase/loader!ias/utils/BaseTemplateUtils",
	"scbase/loader!wsc/mobile/home/utils/MobileHomeUtils",
	"scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils",
	"scbase/loader!sc/plat/dojo/utils/PlatformUIFmkImplUtils",
	"scbase/loader!ias/utils/ContextUtils"
], function (
	templateText,
	_dojodeclare,
	_scScreen,
	_scWidgetUtils,
	_scScreenUtils,
	_scBaseUtils,
	_iasUIUtils,
	_scModelUtils,
	_iasBaseTemplateUtils,
	_wscMobileHomeUtils,
	_wscShipmentUtils,
	_scPlatformUIFmkUtils,
	_iasContextUtils

) {
	return _dojodeclare("extn.mobile.home.STSContainer.detailedReceivedReport.ReceivedDetailsReportScreen", [_scScreen], {
		templateString: templateText,
		uId: "ReceivedDetailsReportScreen",
		packageName: "extn.mobile.home.STSContainer.detailedReceivedReport",
		className: "ReceivedDetailsReportScreen",
		title: "Detailed Receiving Report",
        screen_description: "Detailed Receiving Report",	
	namespaces: {


			sourceBindingNamespaces: [
				{
				description: 'The output to the getContinerDetails mashup.',
				value: 'getContainerDetails_output'
			}

			]			

		},

		subscribers: {
			local: [				
			{
                eventId: 'afterScreenInit',
                sequence: '32',
                handler: {
                    methodName: "initializeScreen"
                }
			},
			{
                eventId: 'extn_btnBack_onClick',
                sequence: '32',
                handler: {
                    methodName: "backOnClick"
                }
			},
			{
                eventId: 'extn_btnBack1_onClick',
                sequence: '32',
                handler: {
                    methodName: "backOnClick"
                }
			},
			{
                eventId: 'extn_btnClose_onClick',
                sequence: '32',
                handler: {
                    methodName: "closeOnClick"
                }
			},

			//**Start OMNI-102418
		{
                eventId: 'extn_StagingScreenbtn_onClick',
                sequence: '32',
                handler: {
                    methodName: "openStagingScreen"
                }
			},

			//**END OMNI-102418
			
	         {
                eventId: 'extn_ShipToStoreOrderSearch_onClick',
                sequence: '32',
                handler: {
                    methodName: "openSTSOrderSearchScreen"
                }
			},
		

			
			]
		},


		initializeScreen: function (event, bEvent, ctrl, args) {

			var inputModel = _scScreenUtils.getInitialInputData(this);
			var invokedFromContainerScreen = _scModelUtils.getStringValueFromPath("IsInvokedFromReceiveContainer", inputModel);
			var sessionModel = window.sessionStorage;
	       	    	 var ContainerModel = JSON.parse(window.sessionStorage.getItem("ContainersModel"));
			 if(_scBaseUtils.equals(invokedFromContainerScreen, "Y") && _scBaseUtils.isVoid(ContainerModel))
			 {
			 var scannedContainerModel = _scBaseUtils.getNewModelInstance();
			 var userId = _scPlatformUIFmkUtils.getUserId();
			 _scModelUtils.setStringValueAtModelPath("AcadSTSTOContainers.ClosedFlag","N",scannedContainerModel);
			_scModelUtils.setStringValueAtModelPath("AcadSTSTOContainers.Createuserid",userId,scannedContainerModel);
			_iasUIUtils.callApi(this, scannedContainerModel, "getScannedContainerCount", null);
			 }
			 else
			 {
				_scScreenUtils.setModel(this,"getContainerDetails_output",ContainerModel,null);
				 var updatedContainerModel = _scScreenUtils.getModel(this,"getContainerDetails_output");
				var partiallyReceivedCount = _scModelUtils.getNumberValueFromPath("AcadSTSTOContainersList.PartailReceivedCount",updatedContainerModel);
				if(partiallyReceivedCount == 0)
				{
				_scWidgetUtils.hideWidget(this,"extn_lblException1", false);
				}
        // OMNI-9418 Temporary Error Pop Up on Finish Receiving - START
				else
				{
				 _scWidgetUtils.showWidget(this,"extn_lblException1", false);
				}
          // OMNI-9418 Temporary Error Pop Up on Finish Receiving - END
			 }
			 
		},

		
	handleMashupOutput: function(mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
		
	},
	handleMashupCompletion: function(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
            _iasBaseTemplateUtils.handleMashupCompletion(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
	
	for(var i in mashupRefList)
	{
	var mashuprefOutput = _scModelUtils.getStringValueFromPath("mashupRefOutput",mashupRefList[i]);
	var outputModel = _scBaseUtils.getNewModelInstance();
		var containers = _scModelUtils.getStringValueFromPath("AcadSTSTOContainersList.PartaillyReceivedContainers.Container.0",mashuprefOutput);
		var containerNo = _scModelUtils.getStringValueFromPath("AcadSTSTOContainersList.PartaillyReceivedContainers.Container.ContainerNo",mashuprefOutput);
		if(_scBaseUtils.isVoid(containers) && !_scBaseUtils.isVoid(containerNo))
		{
			var ContainerModel = _scModelUtils.getStringValueFromPath("AcadSTSTOContainersList.PartaillyReceivedContainers.Container",mashuprefOutput);
  			var containerArray = _scBaseUtils.getNewArrayInstance();
			_scBaseUtils.appendToArray(containerArray,ContainerModel);
			
			 _scModelUtils.setStringValueAtModelPath("AcadSTSTOContainersList.PartaillyReceivedContainers.Container",containerArray,outputModel);

		    var partialContainerCount  =_scModelUtils.getStringValueFromPath("AcadSTSTOContainersList.PartailReceivedCount",mashuprefOutput);
			var TotalReceivedcount =_scModelUtils.getStringValueFromPath("AcadSTSTOContainersList.TotalReceivedContainers",mashuprefOutput);
			_scModelUtils.setStringValueAtModelPath("AcadSTSTOContainersList.PartailReceivedCount",partialContainerCount,outputModel);
			_scModelUtils.setStringValueAtModelPath("AcadSTSTOContainersList.TotalReceivedContainers",TotalReceivedcount,outputModel);

		}
		if(!_scBaseUtils.isVoid(outputModel))
		{
		_scScreenUtils.setModel(this,"getContainerDetails_output",outputModel,null);	
		}
		else
		{
	  	  _scScreenUtils.setModel(this,"getContainerDetails_output",mashupRefList[i].mashupRefOutput,null);
		}
	  var updatedContainerModel = _scScreenUtils.getModel(this,"getContainerDetails_output");
	  var partiallyReceivedCount = _scModelUtils.getNumberValueFromPath("AcadSTSTOContainersList.PartailReceivedCount",updatedContainerModel);
	  if(partiallyReceivedCount == 0)
	  {
		  _scWidgetUtils.hideWidget(this,"extn_lblException1", false);
	  }
      // OMNI-9418 Temporary Error Pop Up on Finish Receiving - START
	 else
	  {
		_scWidgetUtils.showWidget(this,"extn_lblException1", false);
	  }
      // OMNI-9418 Temporary Error Pop Up on Finish Receiving - END
	window.sessionStorage.setItem("ContainersModel",JSON.stringify(updatedContainerModel));
	}
	}
		
	,
	
	backOnClick: function(event, bEvent, ctrl, args) {
			
		_wscMobileHomeUtils.openScreen("extn.mobile.home.STSContainer.container.ReceiveContainer", "extn.mobile.editors.ReceiveContainerEditor");
			
	},
	
	closeOnClick: function(event, bEvent, ctrl, args) {
		
		_wscMobileHomeUtils.openScreen("wsc.mobile.home.MobileHome", "wsc.mobile.editors.MobileEditor");			
	},
	openTOSummaryScreen: function(){								
		_wscMobileHomeUtils.openScreen("extn.mobile.home.STSContainer.transferOrderShipmentSummary.TOShipmentSummary", "extn.mobile.editors.ReceiveContainerEditor");
	},
	
	//**Start OMNI-102418

	openStagingScreen : function(event, bEvent, ctrl, args) {
    var screenInput= _scBaseUtils.getNewModelInstance();		
    _scModelUtils.setStringValueAtModelPath("InvokedFrom","ReceivedDetailsReportScreen",screenInput);
    _wscMobileHomeUtils.openScreen("extn.mobile.home.STSStaging.backroompick.ReadyToStage", "extn.mobile.editors.ReceiveContainerEditor");
		
		
	},
	//**END OMNI-102418
	
	
  	//OMNI-6624 - Mobile Store UI - New Search screen customization for STS - START
	openSTSOrderSearchScreen: function(){
    // OMNI - 8777 - Session object is retaining in the Ship to store order search screen in the scenario which is mentioned in the steps - START
			var clearSessionObject = _scBaseUtils.getNewModelInstance();
			_iasContextUtils.addToContext("SearchCriteria", clearSessionObject);
    // OMNI - 8777 - Session object is retaining in the Ship to store order search screen in the scenario which is mentioned in the steps - END		
			_wscMobileHomeUtils.openScreen("extn.mobile.home.STSOrderSearch.OrderSearch.STSOrderSearchScreen", "extn.mobile.editors.ReceiveContainerEditor");
		},
  	//OMNI-6624 - Mobile Store UI - New Search screen customization for STS - END
	});
});
