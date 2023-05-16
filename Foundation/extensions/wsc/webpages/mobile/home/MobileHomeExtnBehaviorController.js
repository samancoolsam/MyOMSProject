


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/mobile/home/MobileHomeExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnMobileHomeExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.mobile.home.MobileHomeExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.mobile.home.MobileHomeExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupId : 			'extn_SFSOrdersCount'
,
		 mashupRefId : 			'extn_SFSOrderForBatches'
,
		 extnType : 			'ADD'

	}
,
  /* Start - OMNI-5402 : Curbside Pickup home screen - Radhakrishna Mediboina*/
	 		{
		 mashupId : 			'extn_CurbsidePickupOrders'
,
		 mashupRefId : 			'extn_curbsidePickupOrdersCount'
,
		 extnType : 			'ADD'

	}
  /* End - OMNI-5402 : Curbside Pickup home screen - Radhakrishna Mediboina*/
  
  /*  Start - OMNI-6579,6580 : STS home screen changes  */
,
	 		{
		 mashupId : 			'extn_STSReceiveContainersCount'
,
		 mashupRefId : 			'extn_STSReceiveContainers'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupId : 			'extn_STSStageContainersCount'
,
		 mashupRefId : 			'extn_STSStageContainers'
,
		 extnType : 			'ADD'

	}
,
/*  End - OMNI-6579,6580 : STS home screen changes  */
	//OMNI-67538-Feature Toggle Changes - START

	 		{
		 mashupId : 			'extn_AuditShipmentToggle'
,
		 mashupRefId : 			'extn_AuditShipmentFeatureToggle'
,
		 extnType : 			'ADD'
     }
	//OMNI-67538-Feature Toggle Changes - END
//OMNI-71303 Begin
,
	 		{
		 mashupId : 			'extn_OnMyWayOrdersCount'
,
		 mashupRefId : 			'extn_OnMyWayOrdersCount'
,
		 extnType : 			'ADD'

	},
//OMNI-71303 End
//OMNI-72164 Start
		{
	 mashupId : 			'extn_OnMyWayFeatureToggle'
,
		 mashupRefId : 			'extn_OnMyWayFeatureToggle'
,
		 extnType : 			'ADD'
//OMNI-72164 End

 	},
	//OMNI-72475 Start
	{
	 mashupId : 			'extn_OnMyWayTimerToggle'
,
		 mashupRefId : 			'extn_OnMyWayTimerToggle'
,
		 extnType : 			'ADD'
	//OMNI-72475 End

 	},
	//OMNI-72474 Start
	{
	 mashupId : 			'extn_StoreWorkingHours'
,
		 mashupRefId : 			'extn_StoreWorkingHours'
,
		 extnType : 			'ADD'
	//OMNI-72474 End

 	},
	//OMNI-81802 Start
	{
		 mashupId : 			'extn_CommonCodeMashup'
,
		 mashupRefId : 			'extn_MissedScanFeatureToggle'
,
		 extnType : 			'ADD'
     },
  //OMNI-81802 End
 
  //OMNI-83367 Start
	{
		mashupId : 			'extn_CurbsideExtensions'
,
		 mashupRefId : 			'extn_CurbsideExtensions_ref'
,
		 extnType : 			'ADD'
     },
	//OMNI-83367 End
 	
	//Start : OMNI-101859,OMNI-102131,OMNI-85177
	{
		 mashupId : 			'extn_DetermineUIToggle'
,
		 mashupRefId : 			'extn_DetermineUIToggle_ref'
,
		 extnType : 			'ADD'
    },
	//End : OMNI-101859,OMNI-102131,OMNI-85177

//Start OMNI-102102 OMNI-102286

   	{
		 mashupId : 			'extn_STSDeliveredContainersCount'
,
		 mashupRefId : 			'extn_STSDeliveredContainersCount_ref'
,
		 extnType : 			'ADD'
    },

  	{
		 mashupId : 			'extn_STSIntransitContainersCount'
,
		 mashupRefId : 			'extn_STSIntransitContainersCount_ref'
,
		 extnType : 			'ADD'
    },

//END OMNI-102102 OMNI-102286
//OMNI-105498 Begin
,
	 		{
		 mashupId : 			'extn_InStorePickupOrdersCount'
,
		 mashupRefId : 			'extn_InStorePickupOrdersCount'
,
		 extnType : 			'ADD'

	}
//OMNI-105498 End
	]

}
);
});

