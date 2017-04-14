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
<%@ attribute name="attribute" required="true" rtexprvalue="true" type="com.aplana.dbmi.model.Attribute" %>
<%@ attribute name="urlPrefix" required="true"  %>
<%@ attribute name="status" required="true" rtexprvalue="true" type="javax.servlet.jsp.jstl.core.LoopTagStatus" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@ taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>
<%@ taglib prefix="dbmi" uri="http://aplana.com/dbmi/tags" %>
                                     <table cellpadding="0" cellspacing="0">
                                         <tr>
                                             <td>       
                                    <dbmi:linkimage enable="${not status.last && empty blockBean.attribute.id.id}"
                                              urlPrefix="${urlPrefix}"
                                              enableUrl="/images/down.gif" disableUrl="/images/down_disable.gif">
                                            <portlet:actionURL>
                                                  <portlet:param name="attr_down_id" value="${attribute.id.id}" />
                                            </portlet:actionURL>
                                    </dbmi:linkimage>        

                                        </td>
                                        <td>
                                            
                                    <dbmi:linkimage enable="${not status.first && empty blockBean.attribute.id.id }"
                                              urlPrefix="${urlPrefix}"
                                              enableUrl="/images/up.gif" disableUrl="/images/up_disable.gif">
                                            <portlet:actionURL>
                                                  <portlet:param name="attr_up_id" value="${attribute.id.id}" />
                                            </portlet:actionURL>
                                    </dbmi:linkimage>        
                                    
                                        </td>
                                        <td>
                                    
                                    <dbmi:linkimage enable="${not attribute.system && empty blockBean.attribute.id.id}"
                                              urlPrefix="${urlPrefix}"
                                              enableUrl="/images/right.gif" disableUrl="/images/right_disable.gif">
                                            <portlet:actionURL>
                                                  <portlet:param name="attr_right_id" value="${attribute.id.id}" />
                                            </portlet:actionURL>
                                    </dbmi:linkimage>        
                                    </td>
                                    </tr>
                                    </table>
