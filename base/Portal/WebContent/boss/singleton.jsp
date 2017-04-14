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
<%@ taglib uri="/WEB-INF/lib/portal-layout.tld" prefix="p"%>
<%@page import="java.util.ResourceBundle"%>
<%
	ResourceBundle rb = ResourceBundle.getBundle("nls.dbmi", request.getLocale());
	String title = rb.getString("title.boss");
	String copyright = rb.getString("copyright");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="<%=request.getLocale()%>"><%@ include file="./head.jspf" %>
<body id="body" style="background-color:white">
	<p:region regionName="main" regionID="regionMain"/>
</body>
</html>