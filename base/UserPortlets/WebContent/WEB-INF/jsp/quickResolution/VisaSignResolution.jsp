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
<%@page session="false" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt" %> 
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %> 
<%@taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>
<%@page import="com.aplana.dbmi.portlet.QuickResolutionPortlet"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.ajax.QuickResolutionSearchPersonParameters"%>
<%@page import="com.aplana.dbmi.ajax.SearchCardServlet"%>

<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.aplana.dbmi.portlet.QuickResolutionPortletSessionBean"%>

<fmt:setBundle basename="com.aplana.dbmi.portlet.nl.QuickResolutionPortlet"/>
<portlet:defineObjects/>
<%
	QuickResolutionPortletSessionBean sessionBean = 
		(QuickResolutionPortletSessionBean)renderRequest.getPortletSession().
		getAttribute(QuickResolutionPortlet.SESSION_BEAN);
%>

<script type="text/javascript" src="<%=request.getContextPath()%>/js/crypto.js" ></script>

<script type="text/javascript">
	dojo.addOnLoad(function() {
		dojo.require("dijit.form.Button");
		dojo.require("dojox.data.QueryReadStore");
		dojo.require("dijit.form.FilteringSelect");	
  		dojo.require("dijit.layout.BorderContainer");
  		dojo.require("dijit.layout.TabContainer");
  		dojo.require("dijit.layout.ContentPane");
	  	dojo.require("dojo.parser");
	});	
	
	function <portlet:namespace/>_submitForm(action) {
		var form = dojo.byId('<portlet:namespace/>_form');
		dojo.byId('<portlet:namespace/>_action').value = action;
		form.submit();
		return false;
	}	

	function <portlet:namespace/>_setFocus(idDiv) {
		var textarea = document.getElementById('<portlet:namespace/>_text_res');
		var div = dojo.byId(idDiv);
		var text = div.textContent || div.innerText;
		if (textarea.value == '') {
			textarea.value = text;
		} else {
			textarea.value = textarea.value + '\n' + text;
		}
	}
	
	function <portlet:namespace/>_clearEditor() {
		var editor = dojo.byId('<portlet:namespace/>_text_res');
		editor.value = '';
	}
</script>

<div dojoType="dijit.layout.BorderContainer" id="layoutBC" persist="true" liveSplitters="false" style="height:900px; width: 100%">	

<form id="<portlet:namespace/>_form" method="post" action="<portlet:actionURL/>">
	<input id="<portlet:namespace/>_action" type="hidden" name="<%= QuickResolutionPortlet.FIELD_ACTION %>" value=""/>
	<input type="hidden" name="<%= QuickResolutionPortlet.FIELD_NAMESPACE %>" value="<portlet:namespace/>"/>	
	
							<!-- begin мастер -->
							<div class="wizard">
								<div style="clear:left">&nbsp;</div>	
								<div style="width: 950px; height: 350px">
									<!-- begin ввод текстовой резолюции -->
										<!-- begin редактор -->
										<div class="editor">
											<p class="label"><fmt:message key="visasign.resolution"/></p>
											<div style="clear:left">&nbsp;</div>
											<textarea name="<%=QuickResolutionPortlet.FIELD_TEXT_RESOLUTION%>" cols="30" rows="10" class="input_area" 
													  id="<portlet:namespace/>_text_res"><%if(sessionBean.getResolutionText()!=null) {%><%=sessionBean.getResolutionText()%><%}%></textarea>
											<a href="#" class="clear" onclick="<portlet:namespace/>_clearEditor()">&nbsp;</a>
										</div>
										<!-- end редактор -->
										<div class="clear">&nbsp;</div>
									<!-- end ввод текстовой резолюции -->
								</div>
								<div class="clear">&nbsp;</div>
							</div>
							<!-- end мастер -->
							
							<!-- begin панель инструментов снизу -->
							<div class="tools">
							<div class="corn">
								
								<div style="margin:0 auto; width:550px">									
									<a class="cancel" href="#"
										onclick="lockScreen(); <portlet:namespace/>_submitForm('<%= QuickResolutionPortlet.ACTION_CANCEL %>')"  > </a>
									<a class="exec" href="#"
										onclick="lockScreen(); <portlet:namespace/>_submitForm('<%= QuickResolutionPortlet.ACTION_DONE %>')"  > </a>								
								</div>
								
							</div>
							</div>
							<!-- end панель инструментов снизу -->
</form>		

</div>	
</div>						