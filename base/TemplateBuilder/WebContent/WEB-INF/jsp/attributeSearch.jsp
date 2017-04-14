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
<%@ page contentType="text/html"  pageEncoding="UTF-8" %>
<%@ page import="com.aplana.dbmi.model.*" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="dbmi" uri="http://aplana.com/dbmi/tags" %>
<%@ taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>
<%@ taglib prefix="tree" uri="/WEB-INF/tags/treetag.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<portlet:defineObjects/>

<fmt:setBundle basename="${bundleBasename}"/>
<!-- Attribute search -->

<% 
ContextProvider.getContext().setLocale(renderResponse.getLocale());
%>
        <tr>
        <td colspan="2" align="left">
            <a href="#" onClick="return <portlet:namespace/>OnAttributeSearch('')"><fmt:message key="select.other.patterns.label" /></a>
            <br>
        </td>
        </tr>

        <tr>
        	<td style="text-align: left" valign="top">
<c:forEach items="${searchBean.viewBlocks1}" var="viewBlock">
    <dbmi:partitionCaption message="${viewBlock.name}" />
		<div class="divPadding">
			<ap:attributes  templateAttributes="${viewBlock.attributes}"   path="webSearchBean.attributes" urlPrefix="<%= renderRequest.getContextPath() %>"/>                         
        </div>                
</c:forEach>
           
        </td>
        <td style="text-align: left" valign="top">
<c:forEach items="${searchBean.viewBlocks2}" var="viewBlock">
    <dbmi:partitionCaption message="${viewBlock.name}" />
		<div class="divPadding">
        	<ap:attributes  templateAttributes="${viewBlock.attributes}"   path="webSearchBean.attributes" urlPrefix="<%= renderRequest.getContextPath() %>"/>                         
        </div>                
</c:forEach>
        	</td>
    	</tr>	
<!-- /Attribute search -->
