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
<%@ attribute name="node" required="true" rtexprvalue="true"  %>
<%@ attribute name="urlPrefix" required="true"  %>
<%@ attribute name="paramPrefix" required="true"  %>
<%@ attribute name="isList" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@ taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>
<%@ taglib prefix="dbmi" uri="http://aplana.com/dbmi/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="/WEB-INF/tags/treetag.tld" prefix="tree" %>
                    <c:set var="nodeId" >
                       <tree:nodeId node="${node}"/>
                    </c:set>
                           <c:set var="tree_down" value="false"></c:set>
                           <c:set var="tree_up" value="false"></c:set>                           
                           <c:set var="tree_right" value="false"></c:set>                                                      
                           <c:set var="tree_left" value="false"></c:set>                                                                                 
                           <tree:nodeMatch node="${node}" isLastChild="false" isRoot="false">
                               <c:set var="tree_down" value="true"></c:set>
                           </tree:nodeMatch>
                           <tree:nodeMatch  node="${node}" isFirstChild="false" isRoot="false">
                               <c:set var="tree_up" value="true"></c:set>
                           </tree:nodeMatch>
                           <tree:nodeMatch  node="${node}" isFirstChild="false" isRoot="false">
                               <c:set var="tree_right" value="${not isList}"></c:set>
                           </tree:nodeMatch>
                           <tree:nodeMatch  node="${node}" isAfterRoot="true" isRoot="false">
                               <c:set var="tree_left" value="${not isList}"></c:set>
                           </tree:nodeMatch>
                         <table cellpadding="0" cellspacing="0">
                               <tr>
                                   <td>
                                   <dbmi:linkimage enable="${tree_left and empty blockBean.referenceValue.realId}"
                                              urlPrefix="${urlPrefix}"
                                              enableUrl="/images/left.gif" disableUrl="/images/left_disable.gif" >
                                            <portlet:actionURL>
                                                <portlet:param name="${paramPrefix}left" value="${nodeId}"/>
                                            </portlet:actionURL>
                                    </dbmi:linkimage>                                   
                                    </td>
                               
                                   <td>
                                   <dbmi:linkimage enable="${tree_down and empty blockBean.referenceValue.realId}"
                                              urlPrefix="${urlPrefix}"
                                              enableUrl="/images/down.gif" disableUrl="/images/down_disable.gif" >
                                            <portlet:actionURL>
                                                <portlet:param name="${paramPrefix}down" value="${nodeId}"/>
                                            </portlet:actionURL>
                                    </dbmi:linkimage>                                   
                                    </td>
                                   <td>
                                   <dbmi:linkimage enable="${tree_up and empty blockBean.referenceValue.realId}"
                                              urlPrefix="${urlPrefix}"
                                              enableUrl="/images/up.gif" disableUrl="/images/up_disable.gif" >
                                            <portlet:actionURL>
                                                <portlet:param name="${paramPrefix}up" value="${nodeId}"/>
                                            </portlet:actionURL>
                                    </dbmi:linkimage>                                   
                                    </td>
                                   <td>
                                   <dbmi:linkimage enable="${tree_right and empty blockBean.referenceValue.realId}"
                                              urlPrefix="${urlPrefix}"
                                              enableUrl="/images/right.gif" disableUrl="/images/right_disable.gif" >
                                            <portlet:actionURL>
                                                <portlet:param name="${paramPrefix}right" value="${nodeId}"/>
                                            </portlet:actionURL>
                                    </dbmi:linkimage>                                   
                                    </td>
                               </tr>     
                            </table>        
