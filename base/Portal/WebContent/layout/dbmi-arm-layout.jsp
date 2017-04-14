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
	<div id="sizer" style="margin-left=5mm;">
		<div id="expander">
			<div align="center">
				<p:region regionName='top' regionID='regionNORTH'/>
			</div>
			<table width="100%" style="table-layout: fixed;">
				<tr height="15px">
					<td width="16px" id="left-top"/>
					<td id="top-line"/>
					<td width="40px" id="top-center"/>
					<td id="top-line"/>
					<td width="16px" id="right-top"/>
				</tr>
				<tr height="50%">
					<td id="left-line"/>
					<td class="listnote">
						<p:region regionName='left-top' regionID='regionWEST_NORTH'/>
					</td>
					<td id="center"/>
					<td class="listnote">
						<p:region regionName='right-top' regionID='regionEAST_NORTH'/>
					</td>
					<td id="right-line"/>						
				</tr>
				<tr height="50%">
					<td id="left-line"/>
					<td class="listnote">
						<p:region regionName='left-bottom' regionID='regionWEST_SOUTH'/>
					</td>
					<td id="center"/>
					<td class="listnote">
						<p:region regionName='right-bottom' regionID='regionEAST_SOUTH'/>
					</td>
					<td id="right-line"/>						
				</tr>
				<tr height="15px">
					<td width="16px" id="left-bottom"/>
					<td id="bottom-line"/>
					<td width="40px" id="bottom-center"/>
					<td id="bottom-line"/>
					<td width="16px" id="right-bottom"/>						
				</tr>
			</table>
			<div align="center">
				<p:region regionName='bottom' regionID='regionSOUTH'/>
			</div>
		</div>
	</div>
</div>


</body>
</html>