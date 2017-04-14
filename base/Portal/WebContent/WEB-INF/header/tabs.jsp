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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="portalPages" class="com.aplana.dbmi.theme.PortalPagesBean" scope="session"/>
<jsp:setProperty name="portalPages" property="request" value='<%=request%>'/>
<c:choose>
	<c:when test="${portalPages.dbmiPortal}">
	  <c:if test="${not portalPages.selectedPageHidden}">
		<div id="mainmenu">
			<table class="toplevel" cellpadding=0 cellspacing=0 border=0>
				<tr>	
					<td class="spacer" width="27"></td>
					<c:forEach items="${portalPages.firstLevelPages}" var="item">
						<td>
							<c:choose>
								<c:when test="${item.node==portalPages.selectedFirstLevelPage}">
									<table class="select"><tr><td class="left"></td><td class="center">
									<a href="${item.link}">${item.title}</a>
									</td><td class="right"></td></tr></table>								
								</c:when>
								<c:otherwise>
									<a href="${item.link}">${item.title}</a>
								</c:otherwise>
							</c:choose>
						</td>
					</c:forEach>
				</tr>
			</table>
			
			<div class="sublevel"><div class="css"><ul class="secondLevel">
				<c:forEach items="${portalPages.secondLevelPages}" var="item">
					<li  class="secondLevel hoverOff" onmouseover="this.className='secondLevel hoverOn'" onmouseout="this.className='secondLevel hoverOff'">
						<c:choose>
							<c:when test="${item.node==portalPages.selectedSecondLevelPage}">
								<span>
                                    <a href="${item.link}" id="counter_${item.index}">
                                        ${item.title} <c:if test="${item.showDocumentCount}">(...)</c:if>
                                        <!--img style="height:10px;" src='/DBMI-Portal/theme/images/ajax-loader.gif' /-->
                                    </a>
                                </span>
							</c:when>
							<c:otherwise>
                                <a href="${item.link}" id="counter_${item.index}">
                                    ${item.title} <c:if test="${item.showDocumentCount}">(...)</c:if>
                                    <!--img style="height:10px;" src='/DBMI-Portal/theme/images/ajax-loader.gif' /-->
                                </a>
							</c:otherwise>
						</c:choose>
						<ul class="thirdLevel">
							<c:forEach items="${item.children}" var="item3">
								<li class="thirdLevel hoverOff" onmouseover="this.className='thirdLevel hoverOn'" onmouseout="this.className='thirdLevel hoverOff'">
                                    <a href="${item3.link}" id="counter_${item3.index}">
                                        ${item3.title} <c:if test="${item3.showDocumentCount}">(...)</c:if>
                                        <!--img style="height:10px;" src='/DBMI-Portal/theme/images/ajax-loader.gif' /-->
                                    </a>
                                    <ul class="fourthLevel">
										<c:forEach items="${item3.children}" var="item4">
										    <li class="fourthLevel">
                                                <a href="${item4.link}" id="counter_${item4.index}">
                                                    ${item4.title} <c:if test="${item4.showDocumentCount}">(...)</c:if>
                                                    <!--img style="height:10px;" src='/DBMI-Portal/theme/images/ajax-loader.gif' /-->
                                                </a>
										    </li>
										</c:forEach>
									</ul>	
								</li>						
							</c:forEach>
						</ul>
					</li>
				</c:forEach>

                <script>
                    jQuery(function() {
                        var counters = [${portalPages.counters}];

                        function loadCounter() {
                            if (counters.length) {
                                var counter = counters.shift();

                                jQuery.ajax({
                                    type: "POST",
                                    url: "/DBMI-Portal/ajax/pagesCounter",
                                    data: {
                                        xml : counter.xml,
                                        limit : counter.limit
                                    },
                                    complete: function(data, status) {
                                        var placeForCount = jQuery('#counter_'+counter.index);
                                        var oldText = placeForCount.text();
                                        var newText;
                                        if (status == "success") {
                                            var resp = data.responseJSON;
                                            newText = '(' + resp.count + ')';
                                        } else {
                                            newText = '${undefinedCount}';
                                        }
                                        placeForCount.text(oldText.replace('(...)', newText));

                                        loadCounter();
                                    },
                                    dataType: "json"
                                });
                            }
                        }

                        loadCounter();
                    });
                </script>

				<div style="overflow:hidden; clear:both; height:0;"></div>
            </ul></div></div>
		</div>
	  </c:if>
		<div id="clear"></div>		
	</c:when>
	<c:otherwise>
		<jsp:include page="tabs_orig.jsp"/>
	</c:otherwise>
</c:choose>
