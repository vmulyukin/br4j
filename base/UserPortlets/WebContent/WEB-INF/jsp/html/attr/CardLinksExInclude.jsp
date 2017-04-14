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
<%@ page session="false" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.aplana.dbmi.card.CardPortlet"%>
<%@ page import="com.aplana.dbmi.card.CardLinkExAttributeEditor"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<fmt:setBundle basename="com.aplana.dbmi.gui.nl.CardLinkEditResource"/>
<input type="hidden" name="<%= CardLinkExAttributeEditor.FIELD_LINKED_ID %>" value=""/>
<script Type ="text/javascript" language="javascript"><!--
	function submitFormCardLinksExRemove(attributeId, linkId) {
		if (!confirm('<fmt:message key="confirm.remove"/>'))
			return false;
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = '<%= CardLinkExAttributeEditor.ACTION_REMOVE %>';
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ATTR_ID_FIELD %>.value = attributeId;
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardLinkExAttributeEditor.FIELD_LINKED_ID %>.value = linkId;
		document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();
	}
//--></script>
<script type="text/javascript">
	dojo.addOnLoad(function(){
		dojo.require("dijit.form.Button");
	});
</script>