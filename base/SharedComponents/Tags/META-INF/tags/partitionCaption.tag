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
<%@ tag body-content="empty" pageEncoding="UTF-8"%>
<%@ attribute name="messageKey" %>
<%@ attribute name="message" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<table class="partitionContainer">
    <tr>
        <td>
			<table class="partition">
			    <tr>
			        <td class="partition_left">
			        </td>
			        <td class="partition_middle">
			            <c:choose>
			                <c:when test="${not empty messageKey}"><fmt:message key="${messageKey}" /></c:when>
			                <c:when test="${not empty message}">${message}</c:when>
                        </c:choose>
			        </td>
			        <td class="partition_right">
			        </td>
			    </tr>
			</table>
        </td>
    </tr>
</table>
