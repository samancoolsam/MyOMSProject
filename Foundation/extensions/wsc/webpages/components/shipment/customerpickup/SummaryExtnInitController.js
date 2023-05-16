


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/customerpickup/SummaryExtn","scbase/loader!sc/plat/dojo/controller/ExtnScreenController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnSummaryExtn
			 ,
			    _scExtnScreenController
){

return _dojodeclare("extn.components.shipment.customerpickup.SummaryExtnInitController", 
				[_scExtnScreenController], {

			
			 screenId : 			'extn.components.shipment.customerpickup.SummaryExtn'
,			
	//OMNI-85085 - Start	
			 mashupRefs : 	[
{
		 sourceBindingOptions : 			''
,
		 sequence : 			''
,
		 mashupId : 			'extn_getCurbsideGetShipmentLine'
,
		 sourceNamespace : 			'extn_curbside'
,
		 mashupRefId : 			'extn_getCurbsideGetShipmentLine'
,
		 extnType : 			'ADD'
,
		 callSequence : 			''

	}
	]
	//OMNI-85085 - End		
}
);
});

