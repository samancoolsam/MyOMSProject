


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/batch/batchpick/common/BatchPickupProductScanExtn","scbase/loader!sc/plat/dojo/controller/ExtnScreenController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnBatchPickupProductScanExtn
			 ,
			    _scExtnScreenController
){

return _dojodeclare("extn.components.batch.batchpick.common.BatchPickupProductScanExtnInitController", 
				[_scExtnScreenController], {

			
			 screenId : 			'extn.components.batch.batchpick.common.BatchPickupProductScanExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 sourceBindingOptions : 			''
,
		 sequence : 			''
,
		 mashupId : 			'batchPickup_getToBePickedStoreBatchLines'
,
		 sourceNamespace : 			'StoreBatchLines'
,
		 mashupRefId : 			'getStoreBatchLinesListInit'
,
		 extnType : 			'MODIFY'
,
		 callSequence : 			''

	}

	]

}
);
});

