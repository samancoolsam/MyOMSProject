
scDefine(["dojo/text!./templates/MobileHomeExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/layout/ContentPane","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/Label","scbase/loader!sc/plat/dojo/widgets/Link"]
 , function(			 
			    templateText
			 ,
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojolang
			 ,
			    _dojotext
			 ,
			    _idxContentPane
			 ,
			    _scplat
			 ,
			    _scCurrencyDataBinder
			 ,
			    _scBaseUtils
			 ,
			    _scLabel
			 ,
			    _scLink
){
return _dojodeclare("extn.mobile.home.MobileHomeExtnUI",
				[], {
			templateString: templateText
	
	
	
	
	
	
	
					,	
	namespaces : {
		targetBindingNamespaces :
		[
		],
		sourceBindingNamespaces :
		[
			{
	  description: "Namespace for SFS Orders mobile count"
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_7'
						,
	  value: 'extn_SFSOrderCountMobile'
						
			}
			,
      // Start - OMNI-5402 : Curbside Pickup home screen 
			{
	  description: "Curbside Orders Count"
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_8'
						,
	  value: 'extn_CurbsidePickupOrders_output'
						
			}
	  // End - OMNI-5402 : Curbside Pickup home screen 
    
    /*  Start - OMNI-6579,6580 : STS home screen changes  */
    	,
			{
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_9'
						,
	  value: 'extn_STSReceiveContainersCount_output'
						
			}
			,
			{
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_10'
						,
	  value: 'extn_STSStageContainersCount_output'
						
			}
    /*  End - OMNI-6579,6580 : STS home screen changes  */
    
		
	//OMNI_71303 Begin
	,
			{
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_11'
						,
	  value: 'extn_OnMyWayOrdersCount_output'
						
			}
	//OMNI-72474 Begin
	,
	{
		scExtensibilityArrayItemId: 'extn_SourceNamespaces_12',
		value: 'extn_StoreWorkingHours_output'
	}
	//OMNI-72474 End

   ,

   // Start OMNI-102102 OMNI-102286

   {
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_13'
						,
	  value: 'extn_STSDeliveredContainersCount_output'
						
	}
	,

	{
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_14'
						,
	  value: 'extn_STSIntransitContainersCount_output'
						
	}, 
	// END OMNI-102102 OMNI-102286

     /*OMNI-105498 START*/       
{
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_15'
						,
	  value: 'extn_InStorePickupOrdersCount_output'
						
	}
	/*OMNI-105498 START*/





	
				]
	}
	//OMNI-71303 End
	,
	hotKeys: [ 
	]

,events : [
	]

,subscribers : {

local : [

{
	  eventId: 'extn_pnlSearchEComOrders_onClick'

,	  sequence: '51'

,	  description: 'Search eCom Orders'



,handler : {
methodName : "extn_SearchEComOrders"

 
}
}
,
{
	  eventId: 'extn_img_SearchEComOrdersArrow_scshow'

,	  sequence: '51'




,handler : {
methodName : "extn_SearchEComOrders"

 
}
},

{
	  eventId: 'extn_img_SearchEComOrdersArrow_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_SearchEComOrders"

 
}
},
// Start - OMNI-5403 : Curbside Pickup event 
{
	  eventId: 'extn_curbSidePickup_onClick'

,	  sequence: '51'

,	  description: 'Open curb side pick orders'



,handler : {
methodName : "openCurbSidePickOrder"

 
}
}
,
{
	  eventId: 'extn_pnlCurbsidePickupOrders_onClick'

,	  sequence: '51'

,	  description: 'Open Curbside pickup shipment list'



,handler : {
methodName : "openCurbSidePickOrder"

 
}
}
,
{
	  eventId: 'extn_link:img_CurbOrdersArrow_onClick'

,	  sequence: '51'

,	  description: 'Open Curbside pickup shipment list'



,handler : {
methodName : "openCurbSidePickOrder"

 
}
}
,
/*  Start - OMNI-6579,6580 : STS home screen changes  */
{
	  eventId: 'extn_STSReadytoStage_onClick'

,	  sequence: '51'

,	  description: 'Opens Stage Containers Screens'



,handler : {
methodName : "openSTSReadyToStageScreen"

 
}
}
,
{
	  eventId: 'extn_STSStageContainersArrow_onClick'

,	  sequence: '51'

,	  description: 'Opens Stage Container Screen'



,handler : {
methodName : "openSTSReadyToStageScreen"

 
}
}
,
{
	  eventId: 'extn_STSReceiveContainers_onClick'

,	  sequence: '51'

,	  description: 'Open Receive Container Screen'



,handler : {
methodName : "openSTSReceivingScreen"

 
}
}
,
{
	  eventId: 'extn_STSReceiveContainersArrow_onClick'

,	  sequence: '51'

,	  description: 'Open Receive Container Screen'



,handler : {
methodName : "openSTSReceivingScreen"

 
}
}
/*  End - OMNI-6579,6580 : STS home screen changes  */
/*  OMNI-6624 - Mobile Store UI - New Search screen customization for STS - START */
,
{
	  eventId: 'extn_SearchOrder_onClick'

,	  sequence: '51'

,	  description: 'extn_openSTSOrderSearch'



,handler : {
methodName : "extn_openSTSOrderSearch"

 
}
}
/*  OMNI-6624 - Mobile Store UI - New Search screen customization for STS - START  */
,
/*OMNI-66978 START*/
{
	  eventId: 'extn_AuditStaging_onClick'

,	  sequence: '51'

,	  description: 'extn_OpenAuditStagingStartScreen'



,handler : {
methodName : "extn_OpenAuditStagingStartScreen"

 
}
}
/*OMNI-66978 END*/
//OMNI-71303 Begin
,
{
	  eventId: 'extn_OnMyWayOrdersLnk_onClick'

,	  sequence: '51'




,handler : {
methodName : "openOnMyWayOrder"

 
}
}
,
{
	  eventId: 'extn_OnMyWayMainPane_onClick'

,	  sequence: '51'




,handler : {
methodName : "openOnMyWayOrder"

 
}
},
//OMNI-71303 End
/*  OMNI-82213 - Mobile Store UI - New Missed Shipment screen  - Start  */
 	 {
	  eventId: 'extn_MissedShipmentLink_onClick'

,	  sequence: '51'

,	  description: 'extn_MissedShipmentStartScreen'



,handler : {
methodName : "extn_MissedShipmentStartScreen"

 
}
},
 {
	  eventId: 'MissedShipmentArrow_onClick'

,	  sequence: '51'

,	  description: 'extn_MissedShipmentStartScreen'



,handler : {
methodName : "extn_MissedShipmentStartScreen"

 
}
}
,
{
	  eventId: 'extn_STSIntransitArrow_onClick'

,	  sequence: '51'




,handler : {
methodName : "openIntransitContainerList"

 
}
}
,
{
	  eventId: 'extn_STS_Delivered_contentpane_onClick'

,	  sequence: '51'




,handler : {
methodName : "openDeliveredContainerList"

 
}
}
,
{
	  eventId: 'extn_STS_Intransit_contentpane_onClick'

,	  sequence: '51'




,handler : {
methodName : "openIntransitContainerList"

 
}
}
,
{
	  eventId: 'extn_STSDeliveredArrow_onClick'

,	  sequence: '51'




,handler : {
methodName : "openDeliveredContainerList"

 
}
},
/*OMNI-105498 START*/
{
	  eventId: 'extn_InStorePickupLnk_onClick'

,	  sequence: '51'




,handler : {
methodName : "openInStorePickupOrder"
 
}
},
{
	  eventId: 'extn_InStorePickupMainPane_onClick'

,	  sequence: '51'




,handler : {
methodName : "openInStorePickupOrder"
 
}
}
/*OMNI-105498 END*/
]
}

});
});


