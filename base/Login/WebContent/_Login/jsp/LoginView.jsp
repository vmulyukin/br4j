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
<%@page session="false" contentType="text/html" pageEncoding="UTF-8" import="com.aplana.dbmi.login.*" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 

<%@page import="javax.portlet.PortletURL"%>
<%@page import="javax.portlet.PortletMode"%>

<fmt:setBundle basename="com.aplana.dbmi.login.nl.LoginPortletResource"/>

<portlet:defineObjects/>

<%
	String lang = "en";
	if ("ru".equals(lang)) lang = "en"; else lang = "ru";
	LoginPortletSessionBean bean = (LoginPortletSessionBean)renderRequest.getPortletSession().getAttribute(LoginPortlet.SESSION_BEAN);
%>
 
<LINK rel="stylesheet" href="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/css/login_style.css") %>" type="text/css">

<table>
<tr style="height:25%">
    <td></td>
    <td style="width:610px"></td>
    <td></td>
</tr>
<tr>
    <td></td>
    <td >
    <div class=rect>
        <table style="position:relative; width:500px; margin-top: 24px; margin-left: 50px" align="center">
<%--
            <tr style="height:110px;">
                <td>
                    <table id="loginheader">
                        <tr>
                            <td id="loginlogo">
                            	<img alt="<fmt:message key="login.form.image.logo.alt"/>" src='<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/logo-big.gif") %>' >                            
                            </td>
                            <td id="logintitle">
								<fmt:message key="login.form.label.title_1"/><br/><fmt:message key="login.form.label.title_2"/> 
							</td>
                        </tr>

                    </table>
                </td>
            </tr>
--%>
            <tr>
                <td>
                    <table style="height:227px" >
                        <tr style="height:38px;vertical-align:top;">
                            <td >
                                <table style="font-weight: bold; font-size: 22px;">
                                    <tr style="vertical-align:top;">
                                        <td style="width:25px; "></td>
                                        <td style="width:267px;text-align:left; font-size: 22px;">
											<fmt:message key="login.form.label.login"/>
										</td>
                                        <td style="width:175px;text-align:right">
<%--                                        
			<a style="color: rgb(171,171,171);" href="<portal-navigation:url command="ChangeLanguage"><portal-navigation:urlParam name="locale" value="<%= lang %>"/></portal-navigation:url>">
<%
	if ("ru".equals(lang)) {
%>				
					<fmt:message key="login.form.label.ru"/>				
<%	} else { %>
					<fmt:message key="login.form.label.en"/>
<%  }  %>	
					</a>
--%>
                                        </td>
                                        <td style="width:25px; "></td>
                                    </tr>
                                </table>

                            </td>
                        </tr>
                        <tr style="height:1px;">
                            <td style="vertical-align:top;" >
                                <table >
                                    <tr >
                                        <td style="width:25px"></td> <td style="background-color:black;"></td><td style="width:25px"></td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr style="height:37px;">
                            <td>
                            </td>
                        </tr>
<%
	if (renderRequest.getParameter(LoginPortlet.AUTH_ERROR_ATTR_NAME) != null) {
%>				

                        <tr style="height: 43px;">
                            <td >
                                <table style="font-size: 17px;">
                                    <tr>
									<td align="center" style="color: black;">
										<fmt:message key="msg.auth.error"/>
									</td>
                                    </tr>
                                </table>
                            </td>
                        </tr>

<%
	}
%>          
                        
<form method="POST" name="MILoginForm" action="<portlet:actionURL/>">
                        
                        <tr style="height: 43px;">
                            <td >
                                <table style="font-size: 17px;">
                                    <tr>
                                        <td style="width:25px; "></td>
                                        <td style="width:190px;text-align:right; color: black;">
                                        	<span><fmt:message key="login.form.field.user"/></span>
                                        </td>
                                        <td style="width:40px;"></td>
                                        <td style="width:220px;">
                                        	<input type="text" name="<%=LoginPortlet.LOGIN_FIELD_NAME%>" size="23"  style="border-right: #7b9ebd 1px solid; border-top: #7b9ebd 1px solid; border-left: #7b9ebd 1px solid; border-bottom: #7b9ebd 1px solid;width:210px;">
                                       	</td>
                                        <td style="width:25px; "></td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr style="height: 43px;">
                            <td>
                                <table style="font-size: 17px;">
                                    <tr>
                                        <td style="width:25px;"></td>
                                        <td style="width:190px;text-align:right; color: black;">
                                        	<span><fmt:message key="login.form.field.psw"/></span>
                                        	
                                        </td>
                                        <td style="width:40px;"></td>
                                        <td style="width:220px">
                                        <input type="password" name="<%=LoginPortlet.PSW_FIELD_NAME%>" size="23" style="border-right: #7b9ebd 1px solid; border-top: #7b9ebd 1px solid; border-left: #7b9ebd 1px solid; border-bottom: #7b9ebd 1px solid;width:210px;"></td>
                                        <td style="width:25px;"></td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr style="height: 15px;">
                            <td style="text-align:right; vertical-align:top;">
                                <table style="font-size: 17px;">
                                    <tr>
                                        <td style="width:25px;"></td>
<%
			PortletURL helpURL = renderResponse.createActionURL();
			helpURL.setPortletMode(PortletMode.HELP); 			
 %> 	                                         
                                        <td style="width:190px;text-align:left;"><%-- <a style="color: rgb(171,171,171);" href="<%= helpURL %>" style="font-size:10px; color: black;"><fmt:message key="login.form.reminder.link"/></a> --%></td>
                                        <td style="width:40px;"></td>
                                        <td style="width:220px;text-align:right;"><input type="submit" name="LoginBtn"  style="width:75px;height:25px;" value="<fmt:message key="login.form.btn.login"/>"></td>
                                        <td style="width:25px;"></td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
</form>
                        <tr style="height:15px;">
                            <td  >
                            </td>
                        </tr>
                        <tr style="height:1px;">
                            <td style="vertical-align:top;" >
                                <table >
                                    <tr >
                                        <td style="width:25px"></td> <td style="background-color:black;"></td><td style="width:25px"></td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr style="height:19px;">
							<td style="vertical-align:top; font-size:11px; text-align:center;  color: black;"  ><br/>&nbsp;<!--<fmt:message key="login.form.label.creator"/>--></td>
                        </tr>
						<tr style="height:16px;">
							<td>
								<table>
									<td style="vertical-align:top; font-size:10px; text-align:right;  color: black;">
										<fmt:message key="login.form.label.build"/>
										<%= bean.getBuildInfo() %>
									</td>
								</table>
							</td>
						</tr>                        
                    </table>
                </td>
            </tr>
        </table>
        </div>
    </td>
    <td></td>
</tr>

</table>
