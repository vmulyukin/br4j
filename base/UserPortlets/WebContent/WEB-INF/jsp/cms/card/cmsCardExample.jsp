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
<%@page session="true" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>

<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%> 
<%@taglib tagdir="/WEB-INF/tags/dbmi/wrkst_card" prefix="wrkstCard"%>
<%@taglib prefix="btn" uri="http://aplana.com/dbmi/tags" %>

<%@page import="java.util.*"%>
<%@page import="javax.portlet.*"%>
<%@page import="com.aplana.dbmi.card.*"%>
<%@page import="com.aplana.dbmi.gui.*"%>
<%@page import="com.aplana.cms.card.WorkstationCardPortletCardInfo"%>
<%@page import="com.aplana.dbmi.model.TemplateBlock"%>
<%@page import="com.aplana.cms.card.AttributeBlockView"%>
<%@page import="com.aplana.cms.card.AttributeBlockView.Region"%>


<%@page import="com.aplana.dbmi.model.BlockViewParam"%>
<%@page import="com.aplana.dbmi.model.TabViewParam"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.model.PersonAttribute"%>
<%@page import="com.aplana.dbmi.model.ListAttribute"%>
<%@page import="com.aplana.dbmi.model.ReferenceValue"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.model.Template"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage"%>
<%@page import="com.aplana.dbmi.Portal"%>

<portlet:defineObjects/>

<%@ include file="cmsCardHeader.jspf" %>


<wrkstCard:messageArea card="${activeCard}"/>

<script type="text/javascript">
function initCardContainerHeight() {
	var windowHeight = document.documentElement.clientHeight == 0 ? document.body.clientHeight : document.documentElement.clientHeight;
	dojo.byId("fixedCardContainer").style.height = (windowHeight - (65 + dojo.byId("fixedCardHeader").clientHeight)) +"px"
}

dojo.connect(document,"DOMContentLoaded",function(){
	initCardContainerHeight();
});

dojo.connect(window,"resize",function(){
	initCardContainerHeight();
});
</script>

<form name="<%= CardPortlet.EDIT_FORM_NAME %>" method="post" action="<portlet:actionURL/>"> 
  <input type="hidden" name="<%= CardPortlet.ACTION_FIELD %>" value="">
  <input type="hidden" name="<%= CardPortlet.ATTR_ID_FIELD %>" value="">
  <input type="hidden" name="<%= CardPortlet.CARD_ID_FIELD %>" value="">
  <input type="hidden" name="<%= CardPortlet.PARAM_DOCLINK_TEMPLATE %>" value="">
  <input type="hidden" name="<%= CardPortlet.PARAM_DOCLINK_TYPE %>" value="">
  <input type="hidden" name="<%= LinkChooser.CONTENT_TYPE %>" value="">
  <input type="hidden" name="<%= CardPortlet.CARD_MODE %>" value="<%= CardPortlet.CARD_EDIT_MODE %>"/>
  <input type="hidden" name="<%=CardPortlet.COLLAPSE_ID_BLOCKS %>" id="<%=CardPortlet.COLLAPSE_ID_BLOCKS %>"/>
  <input type="hidden" name="<%= CardPortlet.FIELD_THIS_PAGE %>" value="<portlet:renderURL/>"/>
  <input type="hidden" name="<%= CardPortlet.DIALOG_ACTION_FIELD %>" value="">  
  <input type="hidden" name="<%= CardPortlet.DISABLE_DS %>" value="">  
  <input type="hidden" name="namespace" value='<%= renderResponse.getNamespace() %>'>
  <div id="fixedCardHeader">
	<table class="indexCardMain">
		<col Width="50%" />        
        <col Width="50%" />
        <c:choose>
          <c:when test="<%=CardPortlet.CARD_EDIT_MODE.equals(sessionBean.getActiveCardInfo().getMode())%>">
		    <tr>
		      <!--Заголовок-->
		      <td>
		      </td>
		      <td>
		          <div id="rightIcons" style="width: 100%; margin:0;" >
		            <jsp:include page="../../html/CardButtonPane.jsp"/>
		          </div>
		      </td>    
		    </tr>
		    <tr>
		      <!--Разделитель-->
		      <td colspan="4">
		        <hr/>
		      </td>
		    </tr>
          </c:when>
          <c:otherwise>
          	<%@include file="cmsCardViewButtonPane.jspf"%>
          </c:otherwise>
    </c:choose>
    </table>
  </div>
  <div id="fixedCardContainer">  
	  <table class="indexCardMain">
	    <col Width="50%" />
	    <col Width="50%" />
	    
	    <!--  Заголовок: название карточки -->
	    <tr>
	<%      final Attribute attributeCardName = card.getAttributeById(Attribute.ID_NAME);
	        final String headStr = (attributeCardName != null) 
	                ? attributeCardName.getStringValue()
	                : "";
	%>
	            <td colspan="4">
	                <div class="icHeader">
	                    <%= headStr %>
	                </div>
	            </td>
	        </tr>
	        <%@include file="../../html/CardFeatures.jspf"%>
	        <!-- Информационный заголовок: шаблон, статус, код карточки -->
	        <tr>
	          <td colspan="2">
	            <table width="100%">
	              <col width="*">
	              <col align="right" width="32px">
	              <tr>
	                <td>
	                  <div id="BODY_info_header" style="height: auto; display: none;">              
	                    <table width="100%">
	                      <tr>
	                        <td>
	                          <div class="divPadding">  <!-- Template name and card name -->
	                            <table class="content" >
	                              <col Width="45%"/>
	                              <col Width="55%"/>
	                              <tr>
	                                <td><fmt:message key="edit.page.template.label" /></td>
	                                <td><c:out value="${activeCard.templateName}"/></td>
	                              </tr>
	                              <tr>
	                                <td><fmt:message key="edit.page.status.label" /></td>
	                                <td><c:out value="${sessionBean.activeCardInfo.cardState.name}"/></td>
	                              </tr>
	                            </table>
	                          </div>
	                        </td>
	                        <td>
	                          <div class="divPadding">
	                            <table class="content" >
	                              <col Width="45%" />
	                              <col Width="55%"/>
	                              <tr>
	                                <td>
	                                  <!-- "Код карточки:" -->
	                                  <fmt:message key="edit.page.card.id.label" />
	                                </td>
	                                <td><%= cardId %>   </td>
	                              </tr>
	                              <tr>
	                                  <%-- "Отвественный редактор"  --%>
	                                <td> </td>
	                                <td> </td>
	                              </tr>
	                            </table>
	                          </div>
	                        </td>
	                      </tr>
	                    </table>
	                  </div>
	                </td>
	                <td>
	                  <a HREF="javascript:form_collapse('info_header')" class="noLine">  
	                      <span  class="arrow_up" id="ARROW_info_header">&nbsp;</span>
	                  </a>
	                </td>
	              </tr>
	            </table>
	          </td>
	        </tr>
	
	
	
	
	
	
			<tr class="cardContent"><!--Контент-->    
			  <td class="cont" colspan="2">
			   <table width="100%">
				<col Width="50%" />
				<col Width="50%"/>
				<tr>
				  <td>			<!-- Left column -->
				  
				  <c:set var="regionBlockViews" value="<%=workstationCardInfo.getAttributeBlockViewsByRegion(Region.LEFT)%>"/>
					<%@include file="cmsCardRegion.jsp"%>
				  </td>		
			    
				  <td>	<!-- Right column -->
					<c:set var="regionBlockViews" value="<%=workstationCardInfo.getAttributeBlockViewsByRegion(Region.RIGHT)%>"/>
					<%@include file="cmsCardRegion.jsp"%>
				  </td>	
				</tr>
		    	<tr>	<!-- Bottom column -->
				  <td colspan="2">
					<c:set var="regionBlockViews" value="<%=workstationCardInfo.getAttributeBlockViewsByRegion(Region.BOTTOM)%>"/>
					<%@include file="cmsCardRegion.jsp"%>
				  </td>	
				</tr>
			   </table>
			  </td>	
			</tr>	
	
	
	        
	        
	  </table>
	</div>
</form>  


<c:if test="<%= renderRequest.getParameter(CardPortlet.SHOW_BARCODE_PRINT_DIALOG) != null%>">
	<script type="text/javascript">
		dojo.addOnLoad(function(){
      		dijit.byId('positionStamp').show();
  		});
	</script>
</c:if>   
<c:if test="<%= renderRequest.getParameter(CardPortlet.PRINT_BLANK) != null %>">
<%
	ObjectId attrNumReg = ObjectId.predefined(StringAttribute.class, "regnumber");
	ObjectId attrDateReg = ObjectId.predefined(DateAttribute.class, "regdate");
	String numReg = "S_"+((StringAttribute)card.getAttributeById(attrNumReg)).getStringValue();
	String dateReg = "D_";
	Date date = ((DateAttribute)card.getAttributeById(attrDateReg)).getValue();
	if (date != null) {
		dateReg += (new SimpleDateFormat(JasperReportServlet.DATE_FORMAT)).format(date);
	}
	
	String servletPath = request.getContextPath() + "/servlet/JasperReportServlet?";
	String urlBlank = servletPath+"nameConfig=reportChartBlank"+"&numReg="+numReg+"&dateReg="+dateReg;
%>
	<script type="text/javascript">
		dojo.addOnLoad(function() {
			window.open('<%=urlBlank%>')
		});
	</script>
</c:if>


<c:if test="<%= sessionBean.getActiveCardInfo().isPrintMode()%>" >
<% sessionBean.getActiveCardInfo().setPrintMode(false); %>
<script>

function showPrintPage() {

var datatable = document.getElementById('printTableDiv').innerHTML;
var NewWindow = window.open("about:blank",'_newtab');
var NewWinDoc = NewWindow.document;
NewWinDoc.writeln('<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"');
NewWinDoc.writeln('   "http://www.w3.org/TR/html4/loose.dtd">');

NewWinDoc.writeln('<html><head>');

NewWinDoc.writeln('<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">');
	

NewWinDoc.writeln('<LINK rel=\"stylesheet\" href=\"/DBMI-Portal/theme/dbmi_style.css\" type=\"text/css\">');

NewWinDoc.writeln('<style type=\"text/css\">');
NewWinDoc.writeln(' @import <%= renderResponse.encodeURL(renderRequest.getContextPath() + "/print/print.css") %> print; /* Стиль для печати */');
NewWinDoc.writeln('</style>');

NewWinDoc.writeln('</head><body>');
NewWinDoc.writeln(datatable);
NewWinDoc.writeln('</body></html>');
NewWinDoc.close();
}

</script>

<div style="display: none;" id="printTableDiv" > 
<jsp:include flush="true" page="../../cms/card/cmsPrintCardView.jsp" />    
</div>  

<script language="javascript">
	showPrintPage();
</script>

</c:if>


<%@ include file="cmsCardFooter.jspf" %>

