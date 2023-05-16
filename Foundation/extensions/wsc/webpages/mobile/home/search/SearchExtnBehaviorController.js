


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/mobile/home/search/SearchExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnSearchExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.mobile.home.search.SearchExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.mobile.home.search.SearchExtn'
			//OMNI-71289 Pick Up Order Grouping - Flag Based START
			,
			 mashupRefs : 	[
		 		{
					 mashupId : 			'extn_getFlagToDisableAddQtyAndReadOnlyScannedQty',
					 mashupRefId : 			'extn_getFlagToDisableAddQtyAndReadOnlyScannedQty',
					 extnType : 			'ADD'
				}
			]
			//OMNI-71289 Pick Up Order Grouping - Flag Based END		
			
}
);
});

