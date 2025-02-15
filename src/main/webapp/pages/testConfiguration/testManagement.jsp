<%@ page language="java"
         contentType="text/html; charset=utf-8"
         import="org.openelisglobal.internationalization.MessageUtil"
        %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ taglib prefix="ajax" uri="/tags/ajaxtags" %>

<%--
  ~ The contents of this file are subject to the Mozilla Public License
  ~ Version 1.1 (the "License"); you may not use this file except in
  ~ compliance with the License. You may obtain a copy of the License at
  ~ http://www.mozilla.org/MPL/
  ~
  ~ Software distributed under the License is distributed on an "AS IS"
  ~ basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  ~ License for the specific language governing rights and limitations under
  ~ the License.
  ~
  ~ The Original Code is OpenELIS code.
  ~
  ~ Copyright (C) ITECH, University of Washington, Seattle WA.  All Rights Reserved.
  --%>

<form id="mainForm">
    <script type="text/javascript">

        function submitAction(target) {
            var form = document.getElementById("mainForm");
            form.action = target;
            form.submit();
        }


    </script>
    <br>
    <input type="button" value="<%= MessageUtil.getMessage("banner.menu.administration") %>"
           onclick="submitAction('MasterListsPage.do');"
           class="textButton"/> &rarr; <%=MessageUtil.getMessage( "configuration.test.management" )%>

    <h3><spring:message code="configuration.test.management.spelling" /></h3>
    <ul>
        <li><input type="button" value="<%= MessageUtil.getMessage("configuration.test.rename") %>"
                   onclick="submitAction('TestRenameEntry.do');"
                   class="textButton"/><br>
            &nbsp;&nbsp;&nbsp;&nbsp;<%=MessageUtil.getMessage("configuration.test.rename.explain")%></li>
        <li><input type="button" value="<%= MessageUtil.getMessage("configuration.panel.rename") %>"
                   onclick="submitAction('PanelRenameEntry.do');"
                   class="textButton"/><br>
            &nbsp;&nbsp;&nbsp;&nbsp;<%=MessageUtil.getMessage("configuration.panel.rename.explain")%></li>
        <li><input type="button" value="<%= MessageUtil.getMessage("configuration.type.rename") %>"
                   onclick="submitAction('SampleTypeRenameEntry.do');"
                   class="textButton"/><br>
            &nbsp;&nbsp;&nbsp;&nbsp;<%=MessageUtil.getMessage("configuration.type.rename.explain")%></li>
        <li><input type="button" value="<%= MessageUtil.getMessage("configuration.testSection.rename") %>"
                   onclick="submitAction('TestSectionRenameEntry.do');"
                   class="textButton"/><br>
            &nbsp;&nbsp;&nbsp;&nbsp;<%=MessageUtil.getMessage("configuration.testSection.rename.explain")%></li>   
        <li><input type="button" value="<%= MessageUtil.getMessage("configuration.uom.rename") %>"
                   onclick="submitAction('UomRenameEntry.do');"
                   class="textButton"/><br>
            &nbsp;&nbsp;&nbsp;&nbsp;<%=MessageUtil.getMessage("configuration.uom.rename.explain")%></li>           
    </ul>
    <h3><spring:message code="configuration.test.management.organization" /></h3>
    <ul>
        <li><input type="button" value="<%= MessageUtil.getMessage("configuration.test.catalog") %>"
                   onclick="submitAction('TestCatalog.do');"
                   class="textButton"/><br>
            &nbsp;&nbsp;&nbsp;&nbsp;<%=MessageUtil.getMessage("configuration.test.catalog.explain")%></li>
        <li><input type="button" value="<%= MessageUtil.getMessage("configuration.test.add") %>"
                   onclick="submitAction('TestAdd.do');"
                   class="textButton"/><br>
            &nbsp;&nbsp;&nbsp;&nbsp;<%=MessageUtil.getMessage("configuration.test.add.explain")%></li>
        <li><input type="button" value="<%= MessageUtil.getMessage("configuration.test.modify") %>"
                   onclick="submitAction('TestModifyEntry.do');"
                   class="textButton"/><br>
            &nbsp;&nbsp;&nbsp;&nbsp;<%=MessageUtil.getMessage("configuration.test.modify.explain")%></li>
        <li><input type="button" value="<%= MessageUtil.getMessage("configuration.test.activate") %>"
                   onclick="submitAction('TestActivation.do');"
                   class="textButton"/><br>
            &nbsp;&nbsp;&nbsp;&nbsp;<%=MessageUtil.getMessage("configuration.test.activate.explain")%></li>
        <li><input type="button" value="<%= MessageUtil.getMessage("configuration.test.orderable") %>"
                   onclick="submitAction('TestOrderability.do');"
                   class="textButton"/><br>
            &nbsp;&nbsp;&nbsp;&nbsp;<%=MessageUtil.getMessage("configuration.test.orderable.explain")%></li>
        <li><input type="button" value="<%= MessageUtil.getMessage("configuration.testUnit.manage") %>"
                   onclick="submitAction('TestSectionManagement.do');"
                   class="textButton"/><br>
            &nbsp;&nbsp;&nbsp;&nbsp;<%=MessageUtil.getMessage("configuration.testUnit.manage.explain")%></li>
        <li><input type="button" value="<%= MessageUtil.getMessage("configuration.sampleType.manage") %>"
                   onclick="submitAction('SampleTypeManagement.do');"
                   class="textButton"/><br>
            &nbsp;&nbsp;&nbsp;&nbsp;<%=MessageUtil.getMessage("configuration.sampleType.manage.explain")%></li>
        <li><input type="button" value="<%= MessageUtil.getMessage("configuration.uom.manage") %>"
                   onclick="submitAction('UomManagement.do');"
                   class="textButton"/><br>
            &nbsp;&nbsp;&nbsp;&nbsp;<%=MessageUtil.getMessage("configuration.uom.manage.explain")%></li>
        <li><input type="button" value="<%= MessageUtil.getMessage("configuration.panel.manage") %>"
                   onclick="submitAction('PanelManagement.do');"
                   class="textButton"/><br>
            &nbsp;&nbsp;&nbsp;&nbsp;<%=MessageUtil.getMessage("configuration.panel.manage.explain")%></li>    
    </ul>


</form>