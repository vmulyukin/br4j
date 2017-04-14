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
<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="text" required="true"  rtexprvalue="true" %>
<%@ attribute name="enable"%>
<c:choose>
<c:when test="${not empty enable and not enable}">
<li class="disabled"><a class="disabled" href="javascript:return false;">${text}</a></li>
</c:when>
<c:otherwise>
<li onClick="followLink(this)" onmousedown="downButton(this)" onmouseup="upButton(this)" onmouseout="upButton(this)"><a href="<jsp:doBody />">${text}</a></li>
</c:otherwise>
</c:choose>
