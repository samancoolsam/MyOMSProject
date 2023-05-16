/*
 * Licensed Materials - Property of IBM
 * IBM Call Center for Commerce (5725-P82)
 * (C) Copyright IBM Corp. 2013 , 2015 All Rights Reserved. , 2015 All Rights Reserved.
 * US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */
scDefine([
          "scbase/loader!dojo/_base/lang",
          "scbase/loader!dojox/timing",
          "scbase/loader!dojo/date",        
          "scbase/loader!dojo/date/locale",
          "scbase/loader!dojo/dom-class",
          "scbase/loader!dojo/_base/array",
          "scbase/loader!dojo/currency",
          "scbase/loader!dijit/form/Button",
          "scbase/loader!dojox/layout/ContentPane",
          "scbase/loader!dijit/layout/TabContainer",
          "scbase/loader!isccs",
          "scbase/loader!sc/plat/dojo/utils/BaseUtils",
          "scbase/loader!sc/plat/dojo/utils/ScreenUtils",
          "scbase/loader!sc/plat/dojo/utils/GridxUtils",
          "scbase/loader!sc/plat/dojo/utils/EventUtils",
          "scbase/loader!sc/plat/dojo/utils/BundleUtils",
          "scbase/loader!sc/plat/dojo/utils/ControllerUtils",
          "scbase/loader!sc/plat/dojo/info/ApplicationInfo",
          "scbase/loader!sc/plat/dojo/utils/ModelUtils",
          "scbase/loader!sc/plat/dojo/utils/WidgetUtils",
          "scbase/loader!sc/plat/dojo/utils/EditorUtils",
          "scbase/loader!isccs/utils/UIUtils",
          "scbase/loader!isccs/utils/ModelUtils",
          "scbase/loader!isccs/utils/WidgetUtils",
          "scbase/loader!isccs/utils/UOMUtils",
          "scbase/loader!isccs/utils/ContextUtils",
          "scbase/loader!isccs/utils/BaseTemplateUtils",
          "scbase/loader!sc/plat/dojo/Userprefs",
          "scbase/loader!sc/plat/dojo/utils/WizardUtils",
		  "scbase/loader!sc/plat/dojo/utils/PaginationUtils",
          "scbase/loader!sc/plat/dojo/utils/LogUtils",
		  "scbase/loader!sc/plat/dojo/utils/ResourcePermissionUtils"
          ],
          function(dLang,
        		  dTimer,
        		  dDate,
        		  dLocale,
        		  dDomClass,
        		  dArray,
        		  dCurrency,
        		  dButton,
        		  dContentPane,
        		  dTabContainer,
        		  isccs,
        		  scBaseUtils,
        		  scScreenUtils,
        		  scGridUtils,
        		  scEventUtils,
        		  scBundleUtils,
        		  scControllerUtils,
        		  scApplicationInfo,
        		  scModelUtils,
        		  scWidgetUtils,
        		  scEditorUtils,
        		  isccsUIUtils,
        		  isccsModelUtils,
        		  isccsWidgetUtils,
        		  isccsUOMUtils,
        		  isccsContextUtils,
        		  isccsBaseTemplateUtils,
        		  scUserprefs,
        		  scWizardUtils,
				  paginationUtils,
        		  scLogUtils,
        		  scResourcePermissionUtils)
        		  	
        		{

	var customUtils = dLang.getObject("extn.utils.customUtils", true,
			null);
		customUtils.getOrderNoLink = function(grid, rowIndex, colIndex, modelObject, namespace){
            var screen = null;
            screen = isccsWidgetUtils.getOwnerScreen(
             grid);
            var inputArray = null;
            inputArray = [];
            var sDisplayPaymentType = null;
            sDisplayPaymentType = scModelUtils.getStringValueFromPath("PaymentMethod.DisplayPaymentType", modelObject);
            inputArray.push(
            sDisplayPaymentType);
            var sDisplayPaymentAccount = null;
            sDisplayPaymentAccount = scModelUtils.getStringValueFromPath("PaymentMethod.DisplayPaymentAccount", modelObject);
            inputArray.push(
            sDisplayPaymentAccount);
            var returnValue = "";
           
            var TransferOrder=  scModelUtils.getStringValueFromPath("TransferToOrder", modelObject);
            if(!scBaseUtils.isVoid(TransferOrder))
            {
            	 var TransferOrderNo=  scModelUtils.getStringValueFromPath("OrderNo", TransferOrder);
		       //var value = _extnCustomUtils.getOrderNoLink("Transfer Towards Order" +TransferOrderNo);
            	 returnValue= "Transfer Towards Order";  
              var str = '<div>';
		        str+= '<a href="javascript:void(0)">' +returnValue +'</a>';
		      str+='</div>';  
		      return str;        
	        }
	         else 
	         {
	         	 returnValue = scScreenUtils.getFormattedString(
            screen, "Payment_Reference_Format", inputArray);
			 return returnValue;
	         }
           
	};
	return customUtils;
});

