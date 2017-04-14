<%--

      Licensed to the Apache Software Foundation (ASF) under one or more
      contributor license agreements.  See the NOTICE file distributed with
      this work for additional information regarding copyright ownership.
      The ASF licenses this file to you under the Apache License, Version 2.0
      (the "License"); you may not use this file except in compliance with
      the License.  You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.

--%>
<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="tree" required="true" rtexprvalue="true" %>
<%@ attribute name="node" required="true" rtexprvalue="true" %>
<%@ attribute name="includeRootNode" %>
<%@ attribute name="paramPrefix" rtexprvalue="true" %>
<%@ attribute name="urlPrefix" rtexprvalue="true" %>
<%@ attribute name="path" required="true" rtexprvalue="true" %>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="/WEB-INF/tags/treetag.tld" prefix="tree" %>

<input type="hidden" id="my${paramPrefix}select" name="my${paramPrefix}select" />
<input type="hidden" id="my${paramPrefix}expand" name="my${paramPrefix}expand" />
<input type="hidden" id="my${paramPrefix}collapse" name="my${paramPrefix}collapse" />

<script>
function <portlet:namespace/>${paramPrefix}OnExpand(id){
    document.getElementById("my${paramPrefix}expand").value=id;
    <portlet:namespace/>SubmitSearchForm();
    return false;
}

function <portlet:namespace/>${paramPrefix}OnCollapse(id){
    document.getElementById("my${paramPrefix}collapse").value=id;
    <portlet:namespace/>SubmitSearchForm();
    return false;
}

</script>

<table cellspacing="0" cellpadding="0" border="0">
<tree:tree tree="${tree}" node="${node}" includeRootNode="${includeRootNode}" paramPrefix="${paramPrefix}">
    <c:set var="nodeId" >
       <tree:nodeId node="${node}"/>
    </c:set>
    <tr><td>
    <table cellspacing="0" cellpadding="0" border="0">
    <tr><td><tree:nodeIndent    node="${node}" indentationType="type"><tree:nodeIndentVerticalLine indentationType="type" ><img src="${urlPrefix}/images/verticalLine.gif"></tree:nodeIndentVerticalLine><tree:nodeIndentBlankSpace   indentationType="type" ><img src="${urlPrefix}/images/blankSpace.gif"></tree:nodeIndentBlankSpace></tree:nodeIndent></td>
    <tree:nodeMatch    node="${node}" expanded="false" hasChildren="true"  isLastChild="false"><td><a href="#" onClick="return <portlet:namespace/>${paramPrefix}OnExpand('${nodeId}')">
      <img src="${urlPrefix}/images/collapsedMidNode.gif" border="0"></a></td></tree:nodeMatch>
    <tree:nodeMatch    node="${node}" expanded="true"  hasChildren="true"  isLastChild="false"><td><a href="#" onClick="return <portlet:namespace/>${paramPrefix}OnCollapse('${nodeId}')">
    <img src="${urlPrefix}/images/expandedMidNode.gif"  border="0"></a></td></tree:nodeMatch>
    <tree:nodeMatch    node="${node}" expanded="false" hasChildren="true"  isLastChild="true" ><td><a href="#" onClick="return <portlet:namespace/>${paramPrefix}OnExpand('${nodeId}')">
    <img src="${urlPrefix}/images/collapsedLastNode.gif"  border="0"></a></td></tree:nodeMatch>
    <tree:nodeMatch    node="${node}" expanded="true"  hasChildren="true"  isLastChild="true" ><td><a href="#" onClick="return <portlet:namespace/>${paramPrefix}OnCollapse('${nodeId}')">
    <img src="${urlPrefix}/images/expandedLastNode.gif" border="0"></a></td></tree:nodeMatch>
    <tree:nodeMatch    node="${node}" expanded="false" hasChildren="false" isLastChild="false"><td><img src="${urlPrefix}/images/noChildrenMidNode.gif"></td></tree:nodeMatch>
    <tree:nodeMatch    node="${node}" expanded="false" hasChildren="false" isLastChild="true" ><td><img src="${urlPrefix}/images/noChildrenLastNode.gif"></td></tree:nodeMatch>

    <td valign="top">
    <tree:nodeMatch node="${node}" >
        <tree:detachNodeObject node="${node}" detachedObject="nodeControl"/>
    <c:if test="${not empty nodeControl}">    
        <ap:treecontrols path="${path}" urlPrefix="${urlPrefix}" control="${nodeControl}" /> 
    </c:if>
    <span><tree:nodeName node="${node}"/></span></tree:nodeMatch>
    </td>
    </tr>
    </table></td></tr>
</tree:tree>
</table>
