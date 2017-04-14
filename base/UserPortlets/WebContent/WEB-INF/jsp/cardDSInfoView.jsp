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
<%@page session="false" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>


<%@page import="com.aplana.dbmi.card.CardDSInfoViewerServlet"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="com.aplana.dbmi.card.CertificateInfo"%>
<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource" scope="request"/>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<style type="text/css">
	.active {
		color: green;
		font-weight:bold;
	}
	
	.inactive {
		color: green;
	}
	.invalid {
		color: red;
		font-weight:bold;
	}
	.bold {
		font-weight:bold;
	}
	.dsTableHeader{
		font-size:12px;
	}
	.dsDocHeader{
		font-size:12px;
	}
</style>

<link rel="stylesheet" type="text/css" id="dbmi_css" href="/DBMI-Portal/theme/dbmi_style.css" />
<link rel="stylesheet" type="text/css" id="dbmiButtons_css" href="/DBMI-Portal/theme/dbmiButtons_style.css" />

<title><fmt:message key="ds.showinfo.title"/></title>
</head>
<body>
<table class="indexCardMain">
	<col Width="50%" />
	<col Width="50%" />
	<tr>
	  <td>
	  </td>
	  <td>
	      <div class="buttonPanel" >
	       <fmt:message key="edit.page.cancel.btn" var="choiceClose" />
	       <ul>
	         <ap:button text="${choiceClose}"  onClick="window.close()" />
	       </ul>
	      </div>
	  </td>    
	</tr>
</table>
    
<c:set var="certificatesInfo" value="${requestScope.CERTIFICATES_INFO}"/>
<c:if test="${!empty certificatesInfo}">    
<c:set var="authorInfo" value="${certificatesInfo.AUTHOR_CERT_INFO}"/>
<c:set var="filesInfo" value="${certificatesInfo.FILES_CERT_INFO}"/>
<c:set var="visaInfo" value="${certificatesInfo.VISA_CERT_INFO}"/>
<c:set var="signerInfo" value="${certificatesInfo.SIGNER_CERT_INFO}"/>
<c:set var="examinerInfo" value="${certificatesInfo.EXAMINER_CERT_INFO}"/>
<c:set var="reportInfo" value="${certificatesInfo.REPORT_CERT_INFO}"/>
    
   <table width="99%" style="margin: 10px 0px;">
      <tr>
         <td style="vertical-align: top;">
               <div class="divCaption"></div>
              	<table table width="100%" style="margin: 10px;">
              		<thead>
              			<tr>
	              			<th align="left" class="dsTableHeader"><fmt:message key="ds.showinfo.time"/></th>
	              			<th align="left" class="dsTableHeader"><fmt:message key="ds.showinfo.name"/></th>
	              			<th align="left" class="dsTableHeader"><fmt:message key="ds.showinfo.cc"/></th>
	              			<th align="left" class="dsTableHeader"><fmt:message key="ds.showinfo.validityperiod"/></th>
	              			<th align="left" class="dsTableHeader"><fmt:message key="ds.showinfo.serialnumber"/></th>
	              			<th align="left" class="dsTableHeader"><fmt:message key="ds.showinfo.keystate"/></th>
	            			<th align="left" class="dsTableHeader"><fmt:message key="ds.showinfo.sign.state"/></th>
              			</tr>
              			<tr>
              				<td colspan="7"><hr></td>
              			</tr>
              		</thead>
              		<tbody> 
              			<c:if test="${!empty authorInfo}">  
							<tr>
		            			<td colspan="7">&nbsp;</td>
		            		</tr>
							<tr>
		            			<td colspan="5" class="dsDocHeader"><fmt:message key="ds.title.base.doc"/></td>
		            		</tr>             		
		            		<tr>         		
								<td colspan="5"><hr></td>
							</tr>
		            		<c:forEach var="signObjects" items="${authorInfo}">
		            			<c:set var="signValues" value="${signObjects.VALUE}"/>
		            			<c:if test="${signObjects.HEADER  ne null}">
			            			<tr>
			            				<td colspan="5" class="bold">${signObjects.HEADER}</td>
			            			</tr>
		            			</c:if>
		            			<tr>
		            				<td colspan="5"><fmt:message key="ds.showinfo.author"/></td>
		            			</tr>
		            			<c:forEach var="item" items="${signValues}">
				            		<tr>
				            			<td>${item.time}</td>
				            			<td class="bold">${item.owner}
				            				<c:if test="${!empty(item.organization)}">,<br/>${item.organization}</c:if>
				            				<c:if test="${!empty(item.organizationUnit)}">,<br/>${item.organizationUnit}</c:if>
				            				<c:if test="${!empty(item.organizationPost)}">,<br/>${item.organizationPost}</c:if>
				            			</td>
				            			<td>${item.certificationCenter}</td>
				            			<td><fmt:message key="ds.showinfo.from"/> ${item.validFromDate} <fmt:message key="ds.showinfo.to"/> ${item.validToDate}</td>
				            			<td>${item.serialNumber}</td>
				            			<td class="${item.stateStyle}">${item.state}</td>
				            			<td class="${item.signStyle}">${item.signState}</td>
				            		</tr>
				            	</c:forEach>
				            	<tr>
		            				<td colspan="7">&nbsp;</td>
		            			</tr>
		            		</c:forEach>
	              		</c:if>	
	              		<c:if test="${!empty filesInfo}">  
							<tr>
		            			<td colspan="7">&nbsp;</td>
		            		</tr>
							<tr>
		            			<td colspan="5" class="dsDocHeader"><fmt:message key="ds.showinfo.files"/></td>
		            		</tr>
		            		<tr>         		
								<td colspan="5"><hr></td>
							</tr>
		            		<c:forEach var="signObjects" items="${filesInfo}">
		            			<c:set var="signValues" value="${signObjects.VALUE}"/>
		            			<c:if test="${signObjects.HEADER  ne null}">
			            			<tr>
			            				<td colspan="5" class="bold">${signObjects.HEADER}</td>
			            			</tr>
		            			</c:if>
		            			<c:forEach var="item" items="${signValues}">
				            		<tr>
				            			<td>${item.time}</td>
				            			<td class="bold">${item.owner}
				            				<c:if test="${!empty(item.organization)}">,<br/>${item.organization}</c:if>
				            				<c:if test="${!empty(item.organizationUnit)}">,<br/>${item.organizationUnit}</c:if>
				            				<c:if test="${!empty(item.organizationPost)}">,<br/>${item.organizationPost}</c:if>
				            			</td>
				            			<td>${item.certificationCenter}</td>
				            			<td><fmt:message key="ds.showinfo.from"/> ${item.validFromDate} <fmt:message key="ds.showinfo.to"/> ${item.validToDate}</td>
				            			<td>${item.serialNumber}</td>
				            			<td class="${item.stateStyle}">${item.state}</td>
				            			<td class="${item.signStyle}">${item.signState}</td>
				            		</tr>
				            	</c:forEach>
				            	<tr>
		            				<td colspan="7">&nbsp;</td>
		            			</tr>	
		            		</c:forEach>
	              		</c:if>	
         		
						<c:if test="${!empty visaInfo}">  
							<tr>
		            			<td colspan="7">&nbsp;</td>
		            		</tr>         		
		            		<tr>
		            			<td colspan="5" class="dsDocHeader"><fmt:message key="ds.showinfo.coordinators"/></td>
		            		</tr>
		            		<tr>         		
								<td colspan="5"><hr></td>
							</tr>
		            		<c:forEach var="signObjects" items="${visaInfo}">
		            			<c:set var="signValues" value="${signObjects.VALUE}"/>
		            			<c:if test="${signObjects.HEADER  ne null}">
			            			<tr>
			            				<td colspan="5" class="bold">${signObjects.HEADER}</td>
			            			</tr>
		            			</c:if>
		            			<c:forEach var="item" items="${signValues}">
				            		<tr>
				            			<td>${item.time}</td>
				            			<td class="bold">${item.owner}
				            				<c:if test="${!empty(item.organization)}">,<br/>${item.organization}</c:if>
				            				<c:if test="${!empty(item.organizationUnit)}">,<br/>${item.organizationUnit}</c:if>
				            				<c:if test="${!empty(item.organizationPost)}">,<br/>${item.organizationPost}</c:if>
				            			</td>
				            			<td>${item.certificationCenter}</td>
				            			<td><fmt:message key="ds.showinfo.from"/> ${item.validFromDate} <fmt:message key="ds.showinfo.to"/> ${item.validToDate}</td>
				            			<td>${item.serialNumber}</td>
				            			<td class="${item.stateStyle}">${item.state}</td>
				            			<td class="${item.signStyle}">${item.signState}</td>
				            		</tr>
				            	</c:forEach>
				            	<tr>
		            				<td colspan="7">&nbsp;</td>
		            			</tr>
		            		</c:forEach>
	              		</c:if>	
	              		
	              		<c:if test="${!empty signerInfo}">     
							<tr>
		            			<td colspan="7">&nbsp;</td>
		            		</tr>         		
		            		<tr>
		            			<td colspan="5" class="dsDocHeader"><fmt:message key="ds.showinfo.signers"/></td>
		            		</tr>
		            		<tr>         		
								<td colspan="5"><hr></td>
							</tr>            		
		            		<c:forEach var="signObjects" items="${signerInfo}">
		            			<c:set var="signValues" value="${signObjects.VALUE}"/>
		            			<c:if test="${signObjects.HEADER  ne null}">
			            			<tr>
			            				<td colspan="5" class="bold">${signObjects.HEADER}</td>
			            			</tr>
		            			</c:if>
		            			<c:forEach var="item" items="${signValues}">
				            		<tr>
				            			<td>${item.time}</td>
				            			<td class="bold">${item.owner}
				            				<c:if test="${!empty(item.organization)}">,<br/>${item.organization}</c:if>
				            				<c:if test="${!empty(item.organizationUnit)}">,<br/>${item.organizationUnit}</c:if>
				            				<c:if test="${!empty(item.organizationPost)}">,<br/>${item.organizationPost}</c:if>
				            			</td>
				            			<td>${item.certificationCenter}</td>
				            			<td><fmt:message key="ds.showinfo.from"/> ${item.validFromDate} <fmt:message key="ds.showinfo.to"/> ${item.validToDate}</td>
				            			<td>${item.serialNumber}</td>
				            			<td class="${item.stateStyle}">${item.state}</td>
				            			<td class="${item.signStyle}">${item.signState}</td>
				            		</tr>
				            	</c:forEach>
				            	<tr>
		            				<td colspan="7">&nbsp;</td>
		            			</tr>
		            		</c:forEach>
	              		</c:if>	
	              		<c:if test="${!empty examinerInfo}">     
							<tr>
		            			<td colspan="7">&nbsp;</td>
		            		</tr>         		
		            		<tr>
		            			<td colspan="5" class="dsDocHeader"><fmt:message key="ds.showinfo.examiners"/></td>
		            		</tr>
		            		<tr>         		
								<td colspan="5"><hr></td>
							</tr>          		
		            		<c:forEach var="signObjects" items="${examinerInfo}">
		            			<c:set var="signValues" value="${signObjects.VALUE}"/>
								<c:if test="${signObjects.HEADER  ne null}">
			            			<tr>
			            				<td colspan="5" class="bold">${signObjects.HEADER}</td>
			            			</tr>
		            			</c:if>
		            			<c:forEach var="item" items="${signValues}">
				            		<tr>
				            			<td>${item.time}</td>
				            			<td class="bold">${item.owner}
				            				<c:if test="${!empty(item.organization)}">,<br/>${item.organization}</c:if>
				            				<c:if test="${!empty(item.organizationUnit)}">,<br/>${item.organizationUnit}</c:if>
				            				<c:if test="${!empty(item.organizationPost)}">,<br/>${item.organizationPost}</c:if>
				            			</td>
				            			<td>${item.certificationCenter}</td>
				            			<td><fmt:message key="ds.showinfo.from"/> ${item.validFromDate} <fmt:message key="ds.showinfo.to"/> ${item.validToDate}</td>
				            			<td>${item.serialNumber}</td>
				            			<td class="${item.stateStyle}">${item.state}</td>
				            			<td class="${item.signStyle}">${item.signState}</td>
				            		</tr>
				            	</c:forEach>
				            	<tr>
		            				<td colspan="7">&nbsp;</td>
		            			</tr>  
		            		</c:forEach>
	              		</c:if>	
	              		<c:if test="${!empty reportInfo}">   
							<tr>
		            			<td colspan="7">&nbsp;</td>
		            		</tr>         		
		            		<tr>
		            			<td colspan="5" class="dsDocHeader"><fmt:message key="ds.showinfo.reports"/></td>
		            		</tr>                  		
		            		<tr>         		
								<td colspan="5"><hr></td>
							</tr>  		            		
		            		<c:forEach var="signObjects" items="${reportInfo}">
		            			<c:set var="signValues" value="${signObjects.VALUE}"/>
		            			<c:if test="${signObjects.HEADER  ne null}">
			            			<tr>
			            				<td colspan="5" class="bold">${signObjects.HEADER}</td>
			            			</tr>
		            			</c:if>
								<c:forEach var="item" items="${signValues}">
									<tr>
										<td>${item.time}</td>
										<td class="bold">${item.owner}
				            				<c:if test="${!empty(item.organization)}">,<br/>${item.organization}</c:if>
				            				<c:if test="${!empty(item.organizationUnit)}">,<br/>${item.organizationUnit}</c:if>
				            				<c:if test="${!empty(item.organizationPost)}">,<br/>${item.organizationPost}</c:if>
				            			</td>
										<td>${item.certificationCenter}</td>
										<td><fmt:message key="ds.showinfo.from"/> ${item.validFromDate} <fmt:message key="ds.showinfo.to"/> ${item.validToDate}</td>
										<td>${item.serialNumber}</td>
										<td class="${item.stateStyle}">${item.state}</td>
										<td class="${item.signStyle}">${item.signState}</td>
									</tr>
								</c:forEach>
								<tr>
		            				<td colspan="7">&nbsp;</td>
		            			</tr>  
		            		</c:forEach>
	              		</c:if>	
              		</tbody>
              	</table>          
           </td>
       </tr>
</table>	
</c:if>
</body>
</html>