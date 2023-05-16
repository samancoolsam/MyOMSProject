<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/console/jsp/currencyutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<%	String manifestClosedFlag = ""; %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script><script language="javascript">	function closeFutureManifestCheck(sViewID, chkBoxName, action, screenType){		var myObject = new Object();		myObject.currentWindow = window;			var trailerNo = "";			if(screenType == 'List'){			if(chkForSingleCorrectManifest(chkBoxName, action)){							var eleArray = document.getElementsByName(chkBoxName);				for ( var i =0; i < eleArray.length; i++ ) {							   if(eleArray.item(i).checked)					{					   var manifestDate = eleArray[i].getAttribute("manifestDate");					   var tempDate = new Date(manifestDate);					   var today = new Date();					   if(tempDate>today){						   alert("Close manifest can only be performed for current or previous dates");						   return false;					   }					   trailerNo = eleArray[i].getAttribute("trailerNo");		  			   					}				}					}else{				return false;			}		}else if(screenType == 'Detail'){			var manifestClosedFlag = document.all("xml:/Manifest/@ManifestClosedFlag").value;			if(manifestClosedFlag == 'Y'){				alert(YFCMSG025);  //YFCMSG025="Please select Open Manifest for closing.";				return false;			}else{				var manifestDate = document.all("xml:/Manifest/@ManifestDate").value;				   var tempDate = new Date(manifestDate);				   var today = new Date();				   if(tempDate>today){					   alert("Close manifest can only be performed for current or previous dates");					   return false;				   }				trailerNo = document.all("xml:/Manifest/@TrailerNo").value;			}		}			if(trailerNo==''){					myObject.trailerNo=document.all("xml:/Manifest/@TrailerNo");			yfcShowDetailPopup(sViewID, "", "450","250", myObject,"manifest","<Manifest />");			var retVal = myObject["EMReturnValue"];				var returnValue = myObject["OKClicked"];			if ( "YES" == returnValue ) 			{				window.document.documentElement.setAttribute("OKClicked", "false");					return (retVal);			}else			{				window.document.documentElement.setAttribute("OKClicked", "false");				return (false);			}			}else{			return true;		}	}</script>
<div style="height:250px;overflow:auto">
<table class="table" editable="false" width="100%" cellspacing="0">
    <thead> 
        <tr>
            <td sortable="no" class="checkboxheader">
				<input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
				<yfc:makeXMLInput name="manifestKey">
					<yfc:makeXMLKey binding="xml:/Manifest/@ManifestKey" value="xml:closedManifest:/Manifest/@ManifestKey" />
				</yfc:makeXMLInput>
				<input type="hidden" name="hidManifestEntityKey" value='<%=getParameter("manifestKey")%>' />
				<input type="hidden" <%=getTextOptions("hidManifestKey","xml:closedManifest:/Manifest/@ManifestKey")	%> />
            </td>
			<td class="tablecolumnheader"><yfc:i18n>Manifest_#</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Manifest_Date</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>SCAC</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Trailer_#</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Manifest_Closed</yfc:i18n></td>
			<td class="tablecolumnheader"><yfc:i18n>Manifest_Status</yfc:i18n></td>
        </tr>
    </thead>
    <tbody>
        <yfc:loopXML binding="xml:/Manifests/@Manifest" id="Manifest">
            <tr>
				<yfc:makeXMLInput name="manifestKey">
                    <yfc:makeXMLKey binding="xml:/Manifest/@ManifestKey" value="xml:/Manifest/@ManifestKey" />
                    <yfc:makeXMLKey binding="xml:/Manifest/@Scac" value="xml:/Manifest/@Scac" />
                    <yfc:makeXMLKey binding="xml:/Manifest/@ShipNode" value="xml:/Manifest/@ShipNode" />
					<%
					if(!isVoid(resolveValue("xml:/Manifest/@TrailerNo"))){
					%>
					<yfc:makeXMLKey binding="xml:/Manifest/@TrailerNo" value="xml:/Manifest/@TrailerNo" />
					<%
					}
					%>
					<yfc:makeXMLKey binding="xml:/Print/Manifest/@IsHazmat" value="xml:/Manifest/@IsHazmat" />
                </yfc:makeXMLInput>
				<yfc:makeXMLInput name="manifestPrintKey">
					<yfc:makeXMLKey binding="xml:/Print/Manifest/@ManifestKey" value="xml:/Manifest/@ManifestKey" />
					<yfc:makeXMLKey binding="xml:/Print/Manifest/@ShipNode" value="xml:/Manifest/@ShipNode" />
					<yfc:makeXMLKey binding="xml:/Print/Manifest/@SCAC" value="xml:/Manifest/@Scac" />
					<yfc:makeXMLKey binding="xml:/Print/Manifest/@IsHazmat" value="xml:/Manifest/@IsHazmat" />				
				</yfc:makeXMLInput>
                <td class="checkboxcolumn"> 
                    <input type="checkbox" value='<%=getParameter("manifestKey")%>' name="chkEntityKey" manifestClosedFlag= '<%=getValue("Manifest","xml:/Manifest/@ManifestClosedFlag")%>' trailerNo= '<%=getValue("Manifest","xml:/Manifest/@TrailerNo")%>'  manifestDate='<%=getValue("Manifest","xml:Manifest:/Manifest/@ManifestDate")%>' PrintEntityKey='<%=getParameter("manifestPrintKey")%>'/>
			    </td>
                <td class="tablecolumn">
                    <a <%=getDetailHrefOptions("L01", getParameter("manifestKey"),"")%> >
                        <yfc:getXMLValue binding="xml:/Manifest/@ManifestNo"/>
                    </a>					
                </td>
                <td class="tablecolumn" sortValue="<%=getDateValue("xml:Manifest:/Manifest/@ManifestDate")%>">
                    <yfc:getXMLValue binding="xml:/Manifest/@ManifestDate"/>
                </td>
                <td class="tablecolumn">
                    <yfc:getXMLValue binding="xml:/Manifest/@Scac"/>
                </td>
                <td class="tablecolumn">
                    <yfc:getXMLValue binding="xml:/Manifest/@TrailerNo"/>
                </td>
                <td class="tablecolumn">
                    <yfc:getXMLValue binding="xml:/Manifest/@ManifestClosedFlag"/>
				</td>
				<td class="tablecolumn">
                    <yfc:getXMLValueI18NDB binding="xml:/Manifest/Status/@Description"/>
				</td>
            </tr>
        </yfc:loopXML> 
				<input type="hidden" name="xml:/Manifest/@DataElementPath" value="xml:/Manifest"/>
				<input type="hidden" name="xml:/Manifest/@ApiName" value="getManifestDetails"/>
				<input type="hidden" name="xml:/Manifest/@TrailerNo"/>						
   </tbody>
</table>
</div>
