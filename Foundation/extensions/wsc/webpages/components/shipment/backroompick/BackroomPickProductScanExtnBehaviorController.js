


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/backroompick/BackroomPickProductScanExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnBackroomPickProductScanExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.shipment.backroompick.BackroomPickProductScanExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.shipment.backroompick.BackroomPickProductScanExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupRefId : 			'updateShipmentQuantityForPickAllLine'
,
		 mashupId : 			'backroomPick_updateShipmentQuantity'
,
		 extnType : 			'MODIFY'

	}
  	   //OMNI - 4017, 5888, 5885 BOPIS: Cancel Email consolidation at Order level for cancellations - start
,
	 		{
		 mashupRefId : 			'extn_SendBOPISCancelMailMashup'
,
		 mashupId : 			'extn_SendBOPISCancelMailMashup'
,
		 extnType : 			'ADD'

	},
  	    // OMNI - 4017, 5888, 5885 BOPIS: Cancel Email consolidation at Order level for cancellations - end

	{
		 mashupId : 			'getPrinterDevice'
,
		 mashupRefId : 			'extn_getPrinterDeviceMashupRefId'
,
		 extnType : 			'ADD'

	},
		//OMNI-90674 - Start
	{
		 mashupId : 			'extn_translateBarcode'
,
		 mashupRefId : 			'extn_translateBarcode_ref'
,
		 extnType : 			'ADD'

	}
	//OMNI-90674 - End
	]

}
);
});

