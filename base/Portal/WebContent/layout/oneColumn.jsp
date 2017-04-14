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
<%@ page session="false"%>
<%@ taglib uri="/WEB-INF/lib/portal-layout.tld" prefix="p"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="<%=request.getLocale()%>"><%@ include file="./head.jspf" %>
<body id="body" class="tundra">
<div class="redbg"><div id="top"><div id="header">
	<%@ include file="./banner.jspf" %> 
	<p:region regionName='navigation' regionID='navigation' />
</div></div></div>
<div id="portal-container">
	<%@ include file="./loading.jspf" %> 
	<div id="sizer" style="display: none">
		<div id="expander">
			<div id="content-container">
				<table width="100%"><tr>
					<td valign="top"><p:region regionName='center' regionID='regionB' /></td>
				</tr></table>
				<hr class="cleaner" />
			</div>	
		</div>
	</div>
</div>
<p:region regionName='bottom' regionID='regionC'/>

<!--
<p:region regionName='dashboardnav' regionID='dashboardnav' />
-->

<%@ include file="./loadingfooter.jspf" %>

</body>
</html>