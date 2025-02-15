<%@ page language="java" import="java.util.*"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>


<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE html>
<html>
<head>
<head>
	<link rel="icon" href="images/favicon.ico" type="image/x-icon">
	<link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon">
    <base href="<%=basePath%>">
    <title><spring:message code="errors.unhandled.title" text="errors.unhandled.title" /> </title>   
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="0">    
    <meta http-equiv="description" content="Default error page">
    <link rel="stylesheet" type="text/css" href="<%=basePath%>css/bootstrap.css" />
    <script type="text/javascript" src="<%=basePath%>scripts/jquery-1.8.0.min.js"></script>
        
    <!-- Inline css --> 
    <style type="text/css">
    #header {
        margin-bottom: 1em;
        min-width: 970px;
        margin-bottom: 10px;
        padding: 10px 0px 0px 15px;
        height: 88px;
        overflow: visible;
        background: #486086; /* Old browsers */
        background: -moz-linear-gradient(top, #486086 0%, #557aaf 100%); /* FF3.6+ */
        background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,#486086), color-stop(100%,#557aaf)); /* Chrome,Safari4+ */
        background: -webkit-linear-gradient(top, #486086 0%,#557aaf 100%); /* Chrome10+,Safari5.1+ */
        background: -o-linear-gradient(top, #486086 0%,#557aaf 100%); /* Opera11.10+ */
        background: -ms-linear-gradient(top, #486086 0%,#557aaf 100%); /* IE10+ */
        filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#486086', endColorstr='#557aaf',GradientType=0 ); /* IE6-9 */
        background: linear-gradient(top, #486086 0%,#557aaf 100%); /* W3C */
    }
    #oe-logo {
        float: left;
        margin: 0 15px 0 0
    }
    #oe-title {
        font-size: 24px;
        line-height: 32px;
        color: #fff;
        font-weight: bold
    }
    .troubleshooting {
        font-family: Menlo, Monaco, Consolas, "Courier New", monospace;
        font-size: 12px;
    }
    </style>
</head>
  
<body id="defaultErrorPage">

<div id="header">
    <div id="oe-logo"><img src="<%=basePath%>images/openelis_logo.png" title="OpenELIS" alt="OpenELIS"></div>
    <div id="oe-title"><spring:message code="homePage.heading" text="homePage.heading"/></div>
</div>
      
<div class="container">

    <h1><spring:message code="errorpage.404.title" text="errorpage.404.title"/></h1>
    <br />

    <div class="well">	
        <div class="row">
            <div class="span2">
                <img src="<%=basePath%>images/icon-exclamation-warning.png"  title="<spring:message code='errorpage.404.title' text='errorpage.404.title'/>" alt="<spring:message code='errorpage.404.title' text='errorpage.404.title'/>" />
            </div>
            <div class="span9">
            	<h3><spring:message code="errorpage.404.lead" text="errorpage.404.lead"/></h3>
                
                <p class="troubleshooting" id="error-path"> </p>
				
				<p id="previous-path"> </p>

                <p><spring:message code="errorpage.404.instructions" text="errorpage.404.instructions"/></p>

                
                <br />
                <button class="btn" onclick="history.go( -1 );return true;"><i class="icon-chevron-left"></i> <spring:message code="errorpage.previous.button"/></button>
                <a href="Dashboard.do" class="btn" style="margin-left: 20px"><i class="icon-home"></i> <spring:message code="errorpage.home.button"/></a>
                <br /><br />

            </div>
        </div>
    </div>	

</div>
    
<script type="text/javascript">
$(document).ready(function () {
    // jquery functions for details on error
    // Currently not localized.
    var pathname = location.href;
    $("#error-path").text(pathname);
    var referrerPath = document.referrer;
    var referrer = '<spring:message code="errorpage.previous" text="errorpage.previous"/><br /><span class="troubleshooting">' + referrerPath + '"</span>"';
    if (referrerPath != '') {
            $("#previous-path").html(referrer);
            // To build mailto link - currenly not in use
            //var mailAdmin = '<a href="mailto:address_of_admin@uw.edu&subject=Page Error&body=A major error occurred in the OpenELIS system.%0D%0A%0D%0AError page: ' + pathname +'%0D%0AError Time: ' + myDate + '%0D%0AUser-agent header: ' + navigator.userAgent + '">system administrator</a>';
            //$("#admin-mail").html(mailAdmin);
    }
});	
</script>
    
</body>
</html>


