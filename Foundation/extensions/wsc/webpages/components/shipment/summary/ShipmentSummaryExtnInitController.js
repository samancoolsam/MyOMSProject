


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/summary/ShipmentSummaryExtn","scbase/loader!sc/plat/dojo/controller/ExtnScreenController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnShipmentSummaryExtn
			 ,
			    _scExtnScreenController
){

return _dojodeclare("extn.components.shipment.summary.ShipmentSummaryExtnInitController", 
				[_scExtnScreenController], {

			
			 screenId : 			'extn.components.shipment.summary.ShipmentSummaryExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 sourceBindingOptions : 			''
,
		 sequence : 			''
,
		 mashupId : 			'shipSummary_getShipmentDetails'
,
		 sourceNamespace : 			'getShipmentDetails_output'
,
		 mashupRefId : 			'getShipmentDetails'
,
		 extnType : 			'MODIFY'
,
		 callSequence : 			''

	}

	]

}
);
});

