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
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>

<%@page import="java.util.*" %>
<%@page import="javax.portlet.*"%>
<%@page import="com.aplana.dbmi.card.*"%>
<%@page import="com.aplana.dbmi.card.actionhandler.jbr.PasswordChangeForm"%>

<%@page session="true" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"  %>

<portlet:defineObjects/>

<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource" scope="request"/>

<portlet:actionURL var="backUrl">
	<portlet:param name="<%=CardPortlet.ACTION_FIELD%>" value="<%=CardPortlet.BACK_ACTION%>"></portlet:param>
</portlet:actionURL>
<%
CardPortletSessionBean sessionBean = (CardPortletSessionBean)renderRequest.getPortletSession().
getAttribute(CardPortlet.SESSION_BEAN);
String message = sessionBean.getMessage();
if( message != null) {
	sessionBean.setMessage(null);
} else {
	message = renderRequest.getParameter(CardPortlet.MSG_PARAM_NAME);
}
%>
<dbmi:message text="<%= message %>"/>

<% boolean isValidationRequired = (Boolean) request.getAttribute(PasswordChangeForm.VALIDATE_PASS_ATTRIBUTE); %>

<script type="text/javascript">
	var isValidationRequired = <%=isValidationRequired%>;

	<fmt:message key="password.change.empty" var="alertEmpty"/>
	<fmt:message key="password.change.length" var="alertLength"/>
	<fmt:message key="password.change.match" var="alertMatch"/>

	function f5press(e) {
		//запрещаем нажатие F5 и Ctrl+r 
		if(e.keyCode == 116 || (e.keyCode == 82 && e.ctrlKey)) {
			return false;
		}
	}

	window.onkeydown = function (event) { 
		return f5press(event);
	}

	function functionSubmit() {
		var newPass = dojo.byId("new_pass").value;
        var confirmPass = dojo.byId("confirm_pass").value;
       
        dojo.byId("confirm_pass_error").innerHTML = "";
        dojo.byId("new_pass_error").innerHTML = "";
        
        if (isValidationRequired){
        	dojo.byId("cur_pass_error").innerHTML = "";
        	var curPass = dojo.byId("cur_pass").value;
        	if (curPass == "") {
				dojo.byId("cur_pass_error").innerHTML =  "${alertEmpty}";
        	}
	 	}
		if (newPass == "") {
			dojo.byId("new_pass_error").innerHTML =  "${alertEmpty}";
		} else if (newPass.length < 6 ) {
			dojo.byId("new_pass_error").innerHTML =  "${alertLength}";
        } else if (newPass != confirmPass) {
            dojo.byId("confirm_pass_error").innerHTML =  "${alertMatch}";
        } else {
            dojo.byId("passwordChangeForm").submit();
        }
    }
	
	function chg_type(passId, passImg, passPlace) {
	    // Получить имеющееся поле ввода
	    var old_field=dojo.byId(passId);
	    // Создать новое поле ввода
	    var new_field=document.createElement('input');
	    new_field.name=passId;
	    new_field.id=passId;
		new_field.className='password';
	    // Переключение типа поля "текст"<->"пароль"
	    new_field.type=(old_field.type=='text')?'password':'text';
	    // Сохранить уже введенный текст
	    new_field.value=old_field.value;
	    // Заменить имеющееся поле ввода новым
	   dojo.byId(passPlace).replaceChild(new_field,old_field);
	    // Поменять картинку
	    var img_field=dojo.byId(passImg);
	    img_field.src=(old_field.type=='text')?"<%= renderRequest.getContextPath() %>/images/lock.png":"<%= renderRequest.getContextPath() %>/images/unlock.png";
	}
</script>
<script src="/DBMI-UserPortlets/js/blockscroll.js"></script>

<table>
	<tr>
		<td>
			<form name="passwordChangeForm" id="passwordChangeForm" method="post" action="<portlet:actionURL/>">
				<input type="hidden" name="<%=CardPortlet.ACTION_FIELD%>" value="<%=PasswordChangeForm.ACTION_CHANGE%>" />
				<table class ="passwordChangeTable">
				<% if (isValidationRequired){ %>
					<tr>
						<td>
							<label for="cur_pass"><fmt:message key="password.change.current"></fmt:message></label>
						</td>
						<td>
							<div id="cur_pass_place" class="passplace">
								<input type="password" class="password"
										name="cur_pass"
										id="cur_pass"
										value="" />
								<!-- img src="<%= renderRequest.getContextPath() %>/images/lock.png" id="cur_pass_img" class="pass_img" onclick="chg_type('cur_pass', 'cur_pass_img', 'cur_pass_place');"-->
							</div>
						</td>
						<td>
							<span class="portlet-msg-error" id="cur_pass_error"></span>
						</td>
					</tr>
				<% } %>
					<tr>
						<td>
							<label for="new_pass"><fmt:message key="password.change.new"></fmt:message></label>
						</td>
						<td>
							<div id="new_pass_place" class="passplace">
								<input type="password" class="password"
										name="new_pass"
										id="new_pass"
										value="" />
								<!-- img src="<%= renderRequest.getContextPath() %>/images/lock.png" id="new_pass_img"  class="pass_img" onclick="chg_type('new_pass', 'new_pass_img', 'new_pass_place');"-->
							</div>
						</td>
						<td>
							<span class="portlet-msg-error" id="new_pass_error"></span>
						</td>
					</tr>
					<tr>
						<td>
							<label for="confirm_pass"><fmt:message key="password.change.confirm"></fmt:message></label>
						</td>
						<td>
							<div id="confirm_pass_place" class="passplace">
								<input type="password" class="password"
										name="confirm_pass"
										id="confirm_pass"
										value=""/>
								<!-- img src="<%= renderRequest.getContextPath() %>/images/lock.png" id="confirm_pass_img"  class="pass_img" onclick="chg_type('confirm_pass', 'confirm_pass_img', 'confirm_pass_place');"-->
							</div>
						</td>
						<td>
							<span class="portlet-msg-error" id="confirm_pass_error"></span>
						</td>
					</tr>
				</table>
			</form>
		</td>
	</tr>
		<tr>
		<td style="padding-top: 20px;">
			<div class="buttonPanel">
				<ul>
					<li>
						<a onclick="functionSubmit();" href="#"><fmt:message key="password.change.ok.btn"/></a>
					</li>
					<li>
						<a href="${backUrl}"><fmt:message key="password.change.cancel.btn" /> 
						</a>
					</li>
				</ul>
			</div>
		</td>
	</tr>
	
</table>