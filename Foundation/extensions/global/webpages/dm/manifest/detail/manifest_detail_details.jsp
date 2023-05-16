<%@include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script><script language="javascript">function closeFutureManifestCheck(sViewID, chkBoxName, action, screenType){	var myObject = new Object();	myObject.currentWindow = window;		var trailerNo = "";	if(screenType == 'List'){		if(chkForSingleCorrectManifest(chkBoxName, action)){						var eleArray = document.getElementsByName(chkBoxName);			for ( var i =0; i < eleArray.length; i++ ) {						   if(eleArray.item(i).checked)				{				   var manifestDate = eleArray[i].getAttribute("manifestDate");				   var tempDate = new Date(manifestDate);				   var today = new Date();				   if(tempDate>today){					   alert("Close manifest can only be performed for current or previous dates");					   return false;				   }				   trailerNo = eleArray[i].getAttribute("trailerNo");		  			   				}			}				}else{			return false;		}	}else if(screenType == 'Detail'){		var manifestClosedFlag = document.all("xml:/Manifest/@ManifestClosedFlag").value;		if(manifestClosedFlag == 'Y'){			alert(YFCMSG025);  //YFCMSG025="Please select Open Manifest for closing.";			return false;		}else{			var manifestDate = document.all("xml:/Manifest/@ManifestDate").value;			   var tempDate = new Date(manifestDate);			   var today = new Date();			   if(tempDate>today){				   alert("Close manifest can only be performed for current or previous dates");				   return false;			   }			trailerNo = document.all("xml:/Manifest/@TrailerNo").value;		}	}	if(trailerNo==''){				myObject.trailerNo=document.all("xml:/Manifest/@TrailerNo");		yfcShowDetailPopup(sViewID, "", "450","250", myObject,"manifest","<Manifest />");		var retVal = myObject["EMReturnValue"];			var returnValue = myObject["OKClicked"];		if ( "YES" == returnValue ) 		{			window.document.documentElement.setAttribute("OKClicked", "false");				return (retVal);		}else		{			window.document.documentElement.setAttribute("OKClicked", "false");			return (false);		}		}else{		return true;	}}</script>
<table width="100%" class="view">
    <tr>
        <td class="detaillabel">
            <yfc:i18n>Manifest_#</yfc:i18n> 
        </td>
        <td class="protectedtext">
            <yfc:getXMLValue binding="xml:/Manifest/@ManifestNo"/>
        </td>		
        <td class="detaillabel">
            <yfc:i18n>Manifest_Date</yfc:i18n> 
        </td>
        <td class="protectedtext">
            <yfc:getXMLValue binding="xml:/Manifest/@ManifestDate"/>            <input type="hidden" <%=getTextOptions("xml:/Manifest/@ManifestDate")%>/>
        </td>		        
    </tr>
    <tr>
        <td class="detaillabel">
            <yfc:i18n>Carrier</yfc:i18n> 
        </td>
        <td class="protectedtext">
            <yfc:getXMLValue binding="xml:/Manifest/@Scac"/>
        </td>		
		<td class="detaillabel">
            <yfc:i18n>Manifest_Status</yfc:i18n> 
        </td>
        <td class="protectedtext" >
            <yfc:getXMLValueI18NDB binding="xml:/Manifest/Status/@Description"/>
		</td>
    </tr>
    <tr>
        <td class="detaillabel">
            <yfc:i18n>Trailer_#</yfc:i18n> 
        </td>
        <td class="protectedtext" >
            <yfc:getXMLValue binding="xml:/Manifest/@TrailerNo"/>	    
            <input type="hidden" <%=getTextOptions("xml:/Manifest/@TrailerNo")%>/>
        </td>		
		<td class="detaillabel" nowrap="true" >
			<yfc:i18n>Has_Hazardous_Item(s)</yfc:i18n>
		</td>
		<td class="protectedtext">
			<yfc:getXMLValue binding="xml:/Manifest/@IsHazmat"/>
		</td>
    </tr>    
</table>
