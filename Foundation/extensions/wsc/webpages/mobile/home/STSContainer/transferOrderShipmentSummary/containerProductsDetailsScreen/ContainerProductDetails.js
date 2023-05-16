scDefine([
	"dojo/text!./templates/ContainerProductDetails.html",
	"scbase/loader!dojo/_base/declare",
	"scbase/loader!sc/plat/dojo/widgets/Screen",
	"scbase/loader!sc/plat/dojo/utils/WidgetUtils",
	"scbase/loader!sc/plat/dojo/utils/ScreenUtils",
	"scbase/loader!sc/plat/dojo/utils/BaseUtils",
	"scbase/loader!ias/utils/UIUtils",
	"scbase/loader!sc/plat/dojo/utils/ModelUtils",
	"scbase/loader!ias/utils/BaseTemplateUtils",
	"scbase/loader!wsc/mobile/home/utils/MobileHomeUtils",
	"scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils"
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
	_wscShipmentUtils

) {
	return _dojodeclare("extn.mobile.home.STSContainer.transferOrderShipmentSummary.containerProductsDetailsScreen.ContainerProductDetails", [_scScreen], {
		templateString: templateText,
		uId: "ContainerProductDetails",
		packageName: "extn.mobile.home.STSContainer.transferOrderShipmentSummary.containerProductsDetailsScreen",
		className: "ContainerProductDetails",
		title: "",
        screen_description: "The repeating shipment line detail screen used in the Products section of each Container.",	
	namespaces: {


			sourceBindingNamespaces: [
				{
				description: 'The output to the getShipmentDetails mashup.',
				value: 'containerDetail_Src'
			},
      // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - START
				{
				description: 'The cancel lines model.',
				value: 'cancelLine_Src'
			}
      // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - END
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

			
			]
		},


		initializeScreen: function (event, bEvent, ctrl, args) {

			var inputModel = _scScreenUtils.getInitialInputData(this);
      // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - START
			var cancelModel = _scScreenUtils.getModel(this,"cancelLine_Src");
			 if(!_scBaseUtils.isVoid(cancelModel))
			  {
		      _scWidgetUtils.showWidget(this,"CancelproductDetailsPanel", false);
			    _scWidgetUtils.showWidget(this,"imagePanel", false);
			    _scWidgetUtils.hideWidget(this,"productDetailsPanel", true);
			    var extnShortPickReasonCode = _scModelUtils.getStringValueFromPath("ShipmentLine.ExtnShortPickedReasonCode",cancelModel );
			    if(_scBaseUtils.isVoid(extnShortPickReasonCode))
			    {
			   	 _scWidgetUtils.hideWidget(this, "lbl_ReasonCode", false);
			    }
			  }
       // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - END
			//_iasUIUtils.callApi(this,inputModel, "extn_getTOShipmentDetails", null);			
		},
        
        // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - START
	        getOriginalQuantityWithUOM: function(dataValue, screen, widget, nameSpace, shipmentLineModel, options) {
      	     var retValue = null;
        	   // retValue = _scModelUtils.getStringValueFromPath("ShipmentLine.OriginalQuantity", shipmentLineModel);
        	    retValue = _wscShipmentUtils.getFormattedDisplayQuantity(
        	    dataValue, this, widget, nameSpace, shipmentLineModel, options);
       		    return retValue;
   	        },
        
        getCurrentQuantityWithUOM: function(dataValue, screen, widget, nameSpace, shipmentLineModel, options) {
        var retValue = null;
		    var pScreen = _iasUIUtils.getParentScreen(this, true);
		    var parentShipmentModel = _scScreenUtils.getModel(pScreen,"getShipmentDetails_output");
		    var SOShipmenLines = null;
        SOShipmenLines =  _scModelUtils.getStringValueFromPath("Shipment.SalesOrderShipmentLines.ShipmentLines.ShipmentLine", parentShipmentModel);
		    retValue = _wscShipmentUtils.getFormattedDisplayQuantity(dataValue, this, widget, nameSpace, shipmentLineModel, options);
		    if(!_scBaseUtils.isVoid(SOShipmenLines))
		    {
		      var orderLineKeySrc = _scModelUtils.getStringValueFromPath("ShipmentLine.OrderLine.ChainedFromOrderLine.OrderLineKey",shipmentLineModel);
		      for(var i in SOShipmenLines)
		      {
			    var orderLineKey = _scModelUtils.getStringValueFromPath("OrderLine.OrderLineKey", SOShipmenLines[i]);
			    if(orderLineKey==orderLineKeySrc)
			    {
			     var shortageQty = _scModelUtils.getNumberValueFromPath("ShortageQty",SOShipmenLines[i]);
			     if(shortageQty > 0)
			      {
			    	 dataValue= _scModelUtils.getStringValueFromPath("ShipmentLine.OrderLine.ChainedFromOrderLine.OrderedQty", shipmentLineModel);
			       _scModelUtils.setStringValueAtModelPath("ShipmentLine.OrderLine.OrderedQty",dataValue, shipmentLineModel);
			       retValue = _wscShipmentUtils.getFormattedDisplayQuantity(dataValue, this, widget, nameSpace, shipmentLineModel, options);
             }
			      }
		       }
	         }
                 return retValue;
   	        },



	getSOCancelReason: function(dataValue, screen, widget, nameSpace, shipmentLineModel, options) {
		var pScreen = _iasUIUtils.getParentScreen(this, true);
		var parentShipmentModel = _scScreenUtils.getModel(pScreen,"getShipmentDetails_output");
		var SOShipmenLines = null;
    SOShipmenLines =  _scModelUtils.getStringValueFromPath("Shipment.SalesOrderShipmentLines.ShipmentLines.ShipmentLine", parentShipmentModel);
		
		var shipmentLineKeySrc = _scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentLineKey", shipmentLineModel);
		var orderLineKeySrc = _scModelUtils.getStringValueFromPath("ShipmentLine.OrderLine.ChainedFromOrderLine.OrderLineKey",shipmentLineModel);
		//var shortageQty = shpLineSrcModel.ShipmentLine.ShortageQty;
		//var shipmentLines = shipmentModel.Shipment.ShipmentLines.ShipmentLine;
		
		for(var i in SOShipmenLines)
		{
			var shipmentLineKey =_scModelUtils.getStringValueFromPath("ShipmentLineKey", SOShipmenLines[i]);
			var orderLineKey = _scModelUtils.getStringValueFromPath("OrderLine.OrderLineKey", SOShipmenLines[i]);
			var shortageQty = _scModelUtils.getStringValueFromPath("ShortageQty", SOShipmenLines[i]);
			if(orderLineKey==orderLineKeySrc)
			  {
			   if(shortageQty>=1)
		     {
			    var notes = _scModelUtils.getStringValueFromPath("OrderLine.Notes", SOShipmenLines[i]);
			    var numberOfNotes = _scModelUtils.getStringValueFromPath("NumberOfNotes",notes);
			    if(!_scBaseUtils.equals(numberOfNotes, "0"))
			     {
				    var note = _scModelUtils.getStringValueFromPath("Note", notes);
				    var notesLength = note.length;
				    var noteText = note[notesLength-1].NoteText;
				    var noteTextSplit = noteText.split(":");
				    if (noteTextSplit.length == 1) {
				      noteTextSplit = noteTextSplit[0];
			    	}
				    else {
					    noteTextSplit = noteTextSplit[1];
				    }
				    _scWidgetUtils.showWidget(this, "lbl_SOReasonCode", false, "");
				    return noteTextSplit;
			  	}
			   }
			  }
			}
	},

  // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - END

	});
});
