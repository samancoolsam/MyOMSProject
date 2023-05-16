


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/batch/batchpick/common/BatchPickupProductScanExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnBatchPickupProductScanExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.batch.batchpick.common.BatchPickupProductScanExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.batch.batchpick.common.BatchPickupProductScanExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupId : 			'extn_ItemPropertiesService'
,
		 mashupRefId : 			'extn_ItemPropertiesService'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupId : 			'extn_BatchCycleCount'
,
		 mashupRefId : 			'extn_BatchCycleCount'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupId : 			'extn_CloseCancelledBatch'
,
		 mashupRefId : 			'extn_CloseCancelledBatchRef'
,
		 extnType : 			'ADD'

	}

	]

}
);
});

