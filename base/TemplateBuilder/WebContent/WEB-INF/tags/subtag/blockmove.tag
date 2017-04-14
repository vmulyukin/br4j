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
<%@ attribute name="block" required="true" rtexprvalue="true"  type="com.aplana.dbmi.model.AttributeBlock" %>
<%@ attribute name="block_number" required="true"  %>
<%@ attribute name="urlPrefix" required="true"  %>
<%@ attribute name="status" required="true"  rtexprvalue="true" type="javax.servlet.jsp.jstl.core.LoopTagStatus" %>
<%@ attribute name="position" required="true"  %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@ taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>
<%@ taglib prefix="dbmi" uri="http://aplana.com/dbmi/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
                                            <table cellpadding="0" cellspacing="0">
                                               <tr>
                                               <td>

											<c:choose>
											<c:when test="${1 == position}">
                                            <dbmi:linkimage enable="true"
                                              urlPrefix="${urlPrefix}"
                                              enableUrl="/images/ddown.gif" disableUrl="/images/ddown_disable.gif"
                                              >
                                              <portlet:actionURL>
                                                  <portlet:param   name="block_left_id" value="${block.id.id}" />
                                                  <portlet:param   name="block_column" value="${position}" />                  
                                              </portlet:actionURL>
                                              </dbmi:linkimage>
                                            </c:when>
											<c:when test="${2 == position}">
                                            <dbmi:linkimage enable="true"
                                              urlPrefix="${urlPrefix}"
                                              enableUrl="/images/left.gif" disableUrl="/images/left_disable.gif"
                                              >
                                              <portlet:actionURL>
                                                  <portlet:param   name="block_left_id" value="${block.id.id}" />
                                                  <portlet:param   name="block_column" value="${position}" />                  
                                              </portlet:actionURL>
                                              </dbmi:linkimage>
                                            </c:when>
											<c:when test="${3 == position}">
                                            <dbmi:linkimage enable="true"
                                              urlPrefix="${urlPrefix}"
                                              enableUrl="/images/lup.gif" disableUrl="/images/lup_disable.gif"
                                              >
                                              <portlet:actionURL>
                                                  <portlet:param   name="block_left_id" value="${block.id.id}" />
                                                  <portlet:param   name="block_column" value="${position}" />                  
                                              </portlet:actionURL>
                                              </dbmi:linkimage>
                                            </c:when>
                                            </c:choose>
                                              </td>
                                              <td>


                                            <dbmi:linkimage enable="${not status.last}"
                                              urlPrefix="${urlPrefix}"
                                              enableUrl="/images/down.gif" disableUrl="/images/down_disable.gif"
                                              >
                                              <portlet:actionURL>
                                                  <portlet:param   name="block_down_id" value="${block.id.id}" />
                                                  <portlet:param   name="block_column" value="${block_number}" />                  
                                              </portlet:actionURL>
                                              
                                              </dbmi:linkimage>
                                              </td>
                                              <td>
                                            <dbmi:linkimage enable="${not status.first}"
                                              urlPrefix="${urlPrefix}"
                                              enableUrl="/images/up.gif" disableUrl="/images/up_disable.gif"
                                              >
                                              <portlet:actionURL>
                                                  <portlet:param   name="block_up_id" value="${block.id.id}" />
                                                  <portlet:param   name="block_column" value="${block_number}" />                  
                                              </portlet:actionURL>
                                              
                                              </dbmi:linkimage>
                                              </td>
                                              <td>
                                            <c:choose>
                                            <c:when test="${1 == position}">
                                            <dbmi:linkimage enable="true"
                                              urlPrefix="${urlPrefix}"
                                              enableUrl="/images/right.gif" disableUrl="/images/right_disable.gif">
                                              <portlet:actionURL>
                                                  <portlet:param   name="block_right_id" value="${block.id.id}" />
                                                  <portlet:param   name="block_column" value="${position}" />                  
                                              </portlet:actionURL>
                                              </dbmi:linkimage>
                                            </c:when>
                                            <c:when test="${2 == position}">
                                            <dbmi:linkimage enable="true"
                                              urlPrefix="${urlPrefix}"
                                              enableUrl="/images/ddown.gif" disableUrl="/images/ddown_disable.gif">
                                              <portlet:actionURL>
                                                  <portlet:param   name="block_right_id" value="${block.id.id}" />
                                                  <portlet:param   name="block_column" value="${position}" />                  
                                              </portlet:actionURL>
                                              </dbmi:linkimage>
                                            </c:when>
                                            <c:when test="${3 == position}">
                                            <dbmi:linkimage enable="true"
                                              urlPrefix="${urlPrefix}"
                                              enableUrl="/images/rup.gif" disableUrl="/images/rup_disable.gif">
                                              <portlet:actionURL>
                                                  <portlet:param   name="block_right_id" value="${block.id.id}" />
                                                  <portlet:param   name="block_column" value="${position}" />                  
                                              </portlet:actionURL>
                                              </dbmi:linkimage>
                                            </c:when>
                                            </c:choose>
                                              </td>
                                              </tr>
                                             </table>
