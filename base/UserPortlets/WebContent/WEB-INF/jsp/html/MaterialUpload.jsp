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
<%@page session="false" contentType="text/html" pageEncoding="UTF-8"  %>

<%@page import="javax.portlet.*"%>
<%@page import="com.aplana.dbmi.card.*"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.model.StringAttribute"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.crypto.SignatureData"%>
<%@page import="com.aplana.dbmi.model.HtmlAttribute"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map.Entry"%>

<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%> 

<portlet:defineObjects/>
<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource"/>

<%
	CardPortletSessionBean sessionBean = (CardPortletSessionBean)renderRequest.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);

  	String message = sessionBean.getMessage();
	if( message != null) {
  		sessionBean.setMessage(null);
  	} else {
  		message = renderRequest.getParameter(CardPortlet.MSG_PARAM_NAME);
  	}	
	Card card = sessionBean.getActiveCard();
	HtmlAttribute attrSign = (HtmlAttribute) card.getAttributeById(ObjectId.predefined(HtmlAttribute.class, "jbr.uzdo.signature"));
	boolean needsSign = false;
	Object flag = null;
	
	if(attrSign != null){
		//устанавливается в SignatureEdit.jsp
		flag = sessionBean.getAttributeEditorData(attrSign.getId(), com.aplana.dbmi.crypto.SignatureData.AED_NEEDSSIGNATTACH);	
		if(flag != null){
			if((boolean)flag.equals(true)){
				needsSign = true;				
			}
		}
	}

	PortletURL backURL = renderResponse.createActionURL();
	backURL.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.BACK_ACTION); 			

%>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/crypto.js" ></script>
<script Type ="text/javascript" language="javascript">
     
    function submitMaterialUploadForm() { 
    	var filePath = document.getElementById('<%= MaterialAttributeEditor.FILE_PATH_FIELD %>');
    	
    	if (filePath.value.length == null 
    		|| filePath.value.length <= 0) {
    		alert("<fmt:message key="upload.page.nothingselect.msg" />");
    	} else {
    	<%		
    		if(needsSign == true){
		%>
		
		var args = {
			attachFilePath: filePath.value,	
			storeName: "<%= SignatureData.PARAM_CERTSTORE %>",
			userName: "lissi-csp" 
		};
		var signResult = cryptoGetSignature(args);
		
		if(signResult.success){			
				dojo.byId("fileSignature").value = signResult.signature;
				postMaterial(signResult.signature)		
		}else{
			var msg = "Сбой при подписании"
			if(signResult.msg == "noapplet"){			
				msg = "Апплет не инициализирован"
			}else if(signResult.msg == "nofields"){
				msg = "нет подписываемых аттрибутов";
			}		
			alert(msg)
		}
		    
    	<%
    		}else{
    			%>document.MaterialUploadForm.submit();<%
    		}
    	%>
	    	
	    }     
	}
	
	function selectFile() {
          var fileName=document.applets[0].getFileName();
          dojo.byId("<%= MaterialAttributeEditor.FILE_PATH_FIELD %>").value = fileName;          
    }
    
    function postMaterial(signature) {
        var res;
        var url = self.location.protocol + "//" + self.location.host + "<portlet:actionURL/>";
        var filepath = dojo.byId("<%= MaterialAttributeEditor.FILE_PATH_FIELD %>").value;
        res = document.applets[0].postMaterialCard(url, document.cookie, filepath, signature);
        if(res){
        	window.location.replace('<%= backURL.toString() %>');
        }else{
        	alert("Ошибка загрузки")
        }
    } 
	
</script>

<dbmi:message text="<%= message %>"/>
                                         
 
    <table width="30%" >
        <tr>
        	<td style="text-align: left;">
            	<a HRef="<%= backURL.toString() %>" style="text-decoration: none;" ><span class="back"><fmt:message key="upload.page.back.link" /></span></a>
           </td>
        </tr>
        <tr>
        	<td>&nbsp;</td>
        </tr>
        <tr><!--Контент-->
            <td >
                <div class="divPadding">
            	<dbmi:blockHeader id="file" displayed="true" savestate="false">
            		<fmt:message key="upload.page.title" />
            	</dbmi:blockHeader> 
	            <br>              
<form method="post" id="BODY_file" name="MaterialUploadForm" action="<portlet:actionURL/>" enctype="multipart/form-data" accept-charset="UTF-8" > 
                <table class="content" id="content"> 
                     <tr >
                        <td>
                        <%	if(needsSign == true){	%>
                        	<input readonly style="width: 100%" type="text" name="<%= MaterialAttributeEditor.FILE_PATH_FIELD %>" id="<%= MaterialAttributeEditor.FILE_PATH_FIELD %>" >
                                               
	                        </td><td>	        
							<div class="buttonPanel" >
				  				<ul>
				  				<li class="empty"><div>&nbsp;</div></li>
				    			<li onClick="selectFile()" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
				    				<a href="#"><fmt:message key="upload.page.browse.btn"/></a>
				    			</li>			   
							  	</ul>
							</div>              
                        
                        <%	}else{%>
							<input style="width: 100%" type="file" name="<%= MaterialAttributeEditor.FILE_PATH_FIELD %>" id="<%= MaterialAttributeEditor.FILE_PATH_FIELD %>" >
						<%	}%>	
                        </td>                       
                    </tr>
                </table>
	<%				
			if(needsSign == true){
				%>
					<applet name="CryptoApplet"	id="CryptoApplet"
					codebase="<%=request.getContextPath()%>"
					archive="SJBCrypto.jar" 
					code="com.aplana.crypto.CryptoApplet.class"	WIDTH=1	HEIGHT=1>
					<PARAM name="separate_jvm" value="true">
						<H1>WARNING!</H1>The browser you are using is unable to load Java Applets!
					</applet>
					<input type="hidden" id="fileSignature" name="fileSignature">
				<%
			}
	
%>				
				
</form>                
                </div>
            </td>
                        
        </tr>
        <tr>
        <td>	        
			<div class="buttonPanel" >
  				<ul>
    			<li onClick="submitMaterialUploadForm()" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
    				<a href="#"><fmt:message key="upload.page.ok.bnt"/></a>
    			</li>
			    <li class="empty"><div>&nbsp;</div></li>
			    <li onClick="window.location.replace('<%= backURL.toString() %>')" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
			    	<a href="#"><fmt:message key="upload.page.close.btn"/></a></li>
			  	</ul>
			</div>        
        </td>
        </tr>
    </table>
	<jsp:include page="CardPageFunctions.jsp"/>
