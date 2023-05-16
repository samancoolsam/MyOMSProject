


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/backroompick/UpdateHoldLocationExtn","scbase/loader!sc/plat/dojo/controller/ExtnScreenController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnUpdateHoldLocationExtn
			 ,
			    _scExtnScreenController
){

return _dojodeclare("extn.components.shipment.backroompick.UpdateHoldLocationExtnInitController", 
				[_scExtnScreenController], {

			
			 screenId : 			'extn.components.shipment.backroompick.UpdateHoldLocationExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 sourceBindingOptions : 			''
,
		 sequence : 			''
,
		 mashupId : 			'getPrinterDevice'
,
		 sourceNamespace : 			'extn_getPrinterDevice_output1'
,
		 mashupRefId : 			'getPrinterDeviceInit1'
,
		 extnType : 			''
,
		 callSequence : 			''

	}

	]

}
);
});

