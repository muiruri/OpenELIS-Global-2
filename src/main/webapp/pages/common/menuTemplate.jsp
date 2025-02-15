<!DOCTYPE html>
<%@ page language="java"
	contentType="text/html; charset=utf-8"
	import="org.openelisglobal.common.action.IActionConstants,
			org.openelisglobal.common.util.ConfigurationProperties,
			org.openelisglobal.common.util.Versioning" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ taglib prefix="ajax" uri="/tags/ajaxtags" %> 

<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<html>
<%!
String path = "";
String basePath = "";
%>
<%
path = request.getContextPath();
basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";

String form = (String)request.getAttribute(IActionConstants.FORM_NAME);

if (form == null) {
	form = "n/a";
}

  int startingRecNo = 1;
   
  if (request.getAttribute("startingRecNo") != null) {
       startingRecNo = Integer.parseInt((String)request.getAttribute("startingRecNo"));
  }
   request.setAttribute("ctx", request.getContextPath());  
    
%>
<head>
	<link rel="icon" href="images/favicon.ico" type="image/x-icon">
	<link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon">
<link rel="stylesheet" type="text/css" href="<%=basePath%>css/openElisCore.css?ver=<%= Versioning.getBuildNumber() %>" />
<script type="text/javascript" src="<%=basePath%>scripts/jquery-1.8.0.min.js?ver=<%= Versioning.getBuildNumber() %>"></script>
<script type="text/javascript" src="<%=basePath%>scripts/jquery.dataTables.min.js?ver=<%= Versioning.getBuildNumber() %>"></script>
<script type="text/javascript" src="<%=basePath%>scripts/bootstrap.min.js?ver=<%= Versioning.getBuildNumber() %>"></script>
<script type="text/javascript" src="<%=basePath%>scripts/tabs.js"></script> 
<script type="text/javascript" src="<%=basePath%>scripts/additional_utilities.js"></script> 
<script type="text/javascript" src="<%=basePath%>scripts/prototype-1.5.1.js?ver=<%= Versioning.getBuildNumber() %>"></script>
<script type="text/javascript" src="<%=basePath%>scripts/scriptaculous.js?ver=<%= Versioning.getBuildNumber() %>"></script>
<script type="text/javascript" src="<%=basePath%>scripts/overlibmws.js?ver=<%= Versioning.getBuildNumber() %>"></script>
<script type="text/javascript" src="<%=basePath%>scripts/ajaxtags-1.2.js?ver=<%= Versioning.getBuildNumber() %>"></script>
<script type="text/javascript" src="<%=basePath%>scripts/treeScript.js?ver=<%= Versioning.getBuildNumber() %>"></script>
<script type="text/javascript" src="<%=basePath%>scripts/Tooltip-0.6.0.js?ver=<%= Versioning.getBuildNumber() %>"></script>
<script type="text/javascript" src="<%=basePath%>scripts/lightbox.js?ver=<%= Versioning.getBuildNumber() %>"></script>



<script>

function init() {
    check_width();
    initMenu();
}

if (document.addEventListener) {  
  document.addEventListener("DOMContentLoaded", init, false);
} else {
  window.onload = init; 
}

function setMenuAction(button, form, action, validate, parameters) {
  
  var fieldObj = document.getElementById("menuForm").elements['selectedIDs'];
  var ID = '';
  var searchString = '';
  var doSearch = '<%=request.getAttribute(IActionConstants.IN_MENU_SELECT_LIST_HEADER_SEARCH)%>';
  
  if (fieldObj != null) {
    //If only one checkbox
    if (fieldObj[0] == null) {
       if (fieldObj.value != null && fieldObj.checked == true) {
         ID = encodeURIComponent(fieldObj.value);
       }
   } else {
      for (var i = 0; i < fieldObj.length; i++) {
         if (fieldObj[i].checked == true) {
            ID = encodeURIComponent(fieldObj[i].value);
            break;
         }
       }
    }
  }
  
  var sessionid = getSessionFromURL(form.action);
  var context = '<%= request.getContextPath() %>';
  var formName = form.name; 
 
  var parsedFormName;
  
  if (button.name == 'previous' || button.name == 'next' || button.name == 'cancel'|| button.name=='search' || button.name == 'searchString') {
     parsedFormName = formName.substring(1, formName.length - 4);
     parsedFormName = formName.substring(0,1).toUpperCase() + parsedFormName;
     //the action relates to a specific item on the menu form
  } else if (button.name == 'add' || button.name == 'edit' || button.name == 'deactivate') {
     parsedFormName = formName.substring(1, formName.length - 8);
     parsedFormName = formName.substring(0,1).toUpperCase() + parsedFormName;
  } else {
  //EXCEPTION!!!!
  //in case no button was pressed 
  //selection from drop down was made (e.g. analyteTestResultMenu)
     parsedFormName = formName.substring(1, formName.length - 4);
     parsedFormName = formName.substring(0,1).toUpperCase() + parsedFormName;
  }
	
   if (button.name == 'edit' || button.name == 'deactivate') { 
      parameters += ID;
   }

 if (button.name == 'previous' || button.name == 'next') {
      if (doSearch != null) {
       
         if ( doSearch == "true")
         {  
            parameters += "&search=Y"; 
         }
    }
  }
  
  if (button.name != 'cancel' && button.name != 'search' && button.name != 'searchString') {
        parameters += '&startingRecNo=<%= startingRecNo %>';
  }
  
  if (button.name == 'search' || button.name == 'searchString')  {
  
    parameters += "&startingRecNo=1";
     
    var searchForm = document.getElementById("searchForm");
    if (searchForm != null) {
        var fieldObj = searchForm.elements['searchString'];
        searchString = fieldObj.value;
        parameters += "&searchString=" + searchString; 
    }
  }
   

  form.action = context + '/' + action + parsedFormName + '.do' + sessionid + parameters;
  
  if ((button.name == 'edit' && ID == '') || (button.name=='search' && searchString == '') ||(button.name=='searchString' && searchString == '') ) {
  } else if (button.name == 'deactivate'){
      form.submit();
  } else {
	  window.location.href = form.action;
  }
   
       
}
</script>
<%
	if (request.getAttribute("cache") != null && request.getAttribute("cache").toString().equals("false")) 
	{
%>	
		<meta http-equiv="Cache-Control" content="no-cache, no-store, proxy-revalidate, must-revalidate"/> <%-- HTTP 1.1 --%>
		<meta http-equiv="Pragma" content="no-cache"/> <%-- HTTP 1.0 --%>
		<meta http-equiv="Expires" content="0"/> 
<%
	}
%>
	
	<title>
		<c:out value="${title}"/>
	</title>
<tiles:insertAttribute name="banner" />
<tiles:insertAttribute name="login" />
</head>


<%--html:errors/--%>

<body onLoad="onLoad()" >

	<table cellpadding="0" cellspacing="1" width="100%">
			<tr>
				<td>
					<tiles:insertAttribute name="error"/>
				</td>
			</tr>
			<tr>
				<td>
					<tiles:insertAttribute name="header"/>
				</td>
			</tr>
			<tr>
				<td>
					<tiles:insertAttribute name="body"/>
				</td>
			</tr>
			<tr>
				<td>
					<tiles:insertAttribute name="footer"/>
				</td>
			</tr>

	</table>

</body>



</html>

