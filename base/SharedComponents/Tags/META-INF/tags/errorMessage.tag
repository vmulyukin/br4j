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
<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="message" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty message}">
<table class="msg">
    <tr  class="tr1"><td class="td_11"/><td class="td_12"/><td class="td_13"/></tr>
    <tr class="tr2"><td class="td_21"/><td class="td_22">${message}</td><td class="td_23"/></tr>
    <tr class="tr3"><td class="td_31"/><td class="td_32"/><td class="td_33"/></tr>
</table>
</c:if>
