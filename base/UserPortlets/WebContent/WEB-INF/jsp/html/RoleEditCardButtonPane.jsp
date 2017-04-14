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
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib prefix="btn" uri="http://aplana.com/dbmi/tags"%>

<%@page import="com.aplana.dbmi.roleadmin.RoleAdminPortlet"%>

			<div class="buttonPanel" style="float: right">
				<ul>
					<c:set var="onClickCallback">submitForm('<%=RoleAdminPortlet.CLOSE_EDIT_MODE_ACTION%>')</c:set>
					<btn:button onClick="${onClickCallback}" textKey="role.admin.close.btn" />
						
					<c:set var="onClickCallback">submitForm('<%=RoleAdminPortlet.STORE_ROLE_ACTION%>')</c:set>
					<btn:button onClick="${onClickCallback}" textKey="role.admin.save.btn" />
				</ul>
			</div>