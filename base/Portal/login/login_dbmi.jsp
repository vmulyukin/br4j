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
<%@ page pageEncoding="utf-8" %>
<%@page import="java.util.Properties"%>
<%
	String stRequestedUrl = (String)request.getAttribute( "javax.servlet.forward.request_uri");
	System.out.println("Request for secured resource: " + stRequestedUrl);
	if (stRequestedUrl != null) {		
		String stPrefix = request.getAttribute( "javax.servlet.forward.context_path").toString() + 
			request.getAttribute( "javax.servlet.forward.servlet_path").toString() + 
			"/portal/";
		String[] affectedPortals = null;
		String dbmiLoginPath = null;
		try {
			Properties dbmiProp = new Properties();
			dbmiProp.load(this.getClass().getResourceAsStream("/conf/dbmiLogin.properties"));
			dbmiLoginPath = dbmiProp.getProperty("dbmiLoginPath");
			String st = dbmiProp.getProperty("dbmiLoginAffectedPortals");
			affectedPortals = st.split(",");
		} catch (Exception e) {
			System.out.println("Couldn't read DBMI login properties");
			e.printStackTrace();
		}
		if (affectedPortals != null && dbmiLoginPath != null) {
			for (int i = 0; i < affectedPortals.length; ++i) {
				String pattern = stPrefix + affectedPortals[i].trim();
				if (stRequestedUrl.startsWith(pattern + '/') || stRequestedUrl.equals(pattern)) {
					System.out.println("Redirecting to DBMI login page for resource: " + stRequestedUrl);
					response.sendRedirect(dbmiLoginPath+"?affectedPortal="+affectedPortals[i].trim());
					return;
				}
			}
		}		
	}
	System.out.println("Using standard login page for resource: " + stRequestedUrl);
%>
<%@ include file="./login.jsp"%> 
