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
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="<%=request.getLocale()%>"><%@ include file="./head.jspf" %>
<body id="body">
<table width="100%">
	<tr><td>
		<div id="logo">&nbsp;</div>
		<p:region regionName="upperRight" regionID="regionUpperRight"/>
	</td></tr>
	<tr><td>
		<p:region regionName="top" regionID="regionTop"/>
	</td></tr>
	<tr><td>
		<table width="100%">
			<tr>		
				<td width="260px" valign="top">
					<table width="100%" border="0" cellpadding="0">
						<tr>
							<td class="regionLeft-top"/>
							<td class="regionLeft-topRight"/>
						</tr>
						<tr>
							<td class="regionLeft-content"><p:region regionName="left" regionID="regionLeft"/></td>
							<td class="regionLeft-right"/>
						</tr>
						<tr>
							<td class="regionLeft-bottom"/>
							<td class="regionLeft-bottomRight"/>
						</tr>
						<tr>
							<td class="regionLowerLeft-top"/>
							<td class="regionLowerLeft-topRight"/>
						</tr>
						<tr>
							<td class="regionLowerLeft-content"><p:region regionName="lowerLeft" regionID="regionLowerLeft"/></td>
							<td class="regionLowerLeft-right"/>
						</tr>
						<tr>
							<td class="regionLowerLeft-bottom"/>
							<td class="regionLowerLeft-bottomRight"/>
						</tr>						
					</table>
				</td>
				<td valign="top">
					<table border="0" cellpadding="0" width="100%">
						<tr>
							<td class="regionCenter-topLeft"/>
							<td class="regionCenter-top"/>
						</tr>
						<tr>
							<td class="regionCenter-left"/>
							<td width="*" valign="top" class="regionCenter-content"><p:region regionName="center" regionID="regionCenter"/></td>
						</tr>
						<tr>
							<td class="regionCenter-bottomLeft"/>
							<td class="regionCenter-bottom"/>
						</tr>
					</table>
				</td>			
			</tr>
		</table>
	</td></tr>
	<tr><td align="center">
		<p:region regionName="bottom" regionID="regionBottom"/>
	</td></tr>
</table>
</body>
</html>