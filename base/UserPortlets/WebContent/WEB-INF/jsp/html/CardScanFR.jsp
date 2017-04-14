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
<%@page session="true" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"  %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@ taglib prefix="btn" uri="http://aplana.com/dbmi/tags" %>

<%@page import="javax.portlet.*"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="java.net.URL" %>

<portlet:defineObjects/>
<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource" scope="request"/>
<%
	CardPortletSessionBean sessionBean =
		(CardPortletSessionBean)renderRequest.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
	Long cardId = (Long)sessionBean.getActiveCard().getId().getId();
	
	PortletURL backURL = renderResponse.createActionURL();
	backURL.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.BACK_ACTION);
	
	String uploadUrl = new URL(
					renderRequest.getScheme(), renderRequest.getServerName(),
					renderRequest.getServerPort(),
					renderRequest.getContextPath() + "/servlet/scanner-upload").toString();
%>
<div class="buttonPanel">
	<ul>
		<li class="back"
			onmousedown="downBackBut(this)" 
			onmouseup="upBackButton(this)" 
			onmouseover="overBackButton(this)" 
			onmouseout="upBackButton(this)">
			<a href="<%= backURL.toString() %>">
				<div class="ico_back img">&nbsp;</div>
					<p><fmt:message key="view.page.back.link" /></p>
			</a>
		</li>	
	</ul>
</div>
<div>
<FORM name="ScanForm">
<div class="icHeader">
	<SCRIPT language="JavaScript">
	if(dojo.isIE){
		document.write('Идет сканирование. Подождите...');
	}else{
		document.write('Функция доступна только в Internet Explorer!');
	}	
	</SCRIPT>
</div>
 <input type="hidden" name="ScannedFile" value="">
  
 <applet name="CryptoApplet"	id="CryptoApplet" 	
		codebase="<%=request.getContextPath()%>"
		archive="SJBCrypto.jar" 
		code="com.aplana.crypto.CryptoApplet.class"	WIDTH="1" HEIGHT="1">
		<PARAM name="separate_jvm" value="true">
	<H1>WARNING!</H1>
	The browser you are using is unable to load Java Applets!
	</applet>
</FORM>

	<SCRIPT language="VBScript">
	dim frApp , frPages, frBatch, folder, objFSO, tempDir,tfolder, i, filename, fileIndex
	

	Set objFSO = CreateObject("Scripting.FileSystemObject")
	Set tfolder = objFSO.GetSpecialFolder(2)
    tempDir = tfolder.Path    
	set frApp = CreateObject("FineReader6.Application")
    if isObject(frApp) = false then
		msgbox "Не удалось запустить Fine Reader 6"
	end if
	folder = tempDir+"\ITSCANBATCH"

	frApp.Visible = true
	
	Set frBatch = frApp.Batch

   If Not frApp.IsBatchFolder(folder) Then
      frBatch.Create folder, 0
   End If

   frBatch.Open folder

   Set frPagesColl=frApp.CreateBatchPagesCollection()	
	if frApp.Batch.Pages.Count > 0 then
		For i=0 To frApp.Batch.Pages.Count-1
			frPagesColl.Add frApp.Batch.Pages.Item(i)
		Next
	end if

	If frPagesColl.Count>0 Then 
		'msgbox "нашли " & cstr(frPagesColl.Count)
		frBatch.DeletePages frPagesColl 
		'msgbox "удалили"
	end if

	'Set frBatchOption=frBatch.Options
	'frBatchOption.UseFineScanInterface=True

'	msgbox "start scanning"
	frBatch.ScanMultiplePages -1, 1

	call setTimeout("saveFile", 10, "vbscript")

sub saveFile
	if frApp.IsScanning then		
		call setTimeout("saveFile", 1000, "vbscript")
		exit sub
	end if


	frApp.State=1
	Set frPagesColl=frApp.CreateBatchPagesCollection()	
	select case Msgbox("Отсканировано страниц: " & frBatch.Pages.Count & "." & chr(10) & "Учитывать четные страницы?", 3, "Сканирование")		
	case 7
		For i=1 To frBatch.Pages.Count Step 2
			frPagesColl.Add frBatch.Pages.Item(i-1)
		Next
	case 6			
		For i=0 To frApp.Batch.Pages.Count-1
			frPagesColl.Add frApp.Batch.Pages.Item(i)
		Next
	case 2
		msgbox "Действие отменено"
		frApp.Quit
		call window.location.replace("<%= backURL.toString() %>")
		exit sub
	End select
	
	fileIndex = 0 	
	filename = folder+"\document.pdf"	
	If (objFSO.FileExists(filename)) Then
		on error resume next
		call objFSO.deleteFile(filename, true)
		do while err = 70
			err = 0
			fileIndex = fileIndex + 1			
			filename = folder+"\document" & fileIndex & ".pdf"			
			If (objFSO.FileExists(filename)) Then
				call objFSO.deleteFile(filename, true)
			end if
			'msgbox filename & " (" & err & ")"
		loop
		on error goto 0
	end if
    
	If frPagesColl.Count>0 Then
		'tif: frBatch.SaveImagesToFiles frPagesColl, 16, filename, True 
		frBatch.ExportPagesToFile frPagesColl, 3, filename  
	end if

	frApp.Quit
   
  	ScanForm.ScannedFile.value = filename
	window.uploadScan()
end sub

</SCRIPT>


<SCRIPT language="JavaScript">
function uploadScan(){
 var filepath = document.forms["ScanForm"].ScannedFile.value;
 if(filepath == "") return;
 var url = self.location.protocol + "//" + self.location.host + "<portlet:actionURL/>";
 
 //document.applets[0].setNameSpace("<portlet:namespace/>");
 var res = document.applets[0].postMaterialCard(url, document.cookie, filepath, "");
 if(res){
    window.location.replace('<%= backURL.toString() %>');
   }else{
  	alert("Ошибка загрузки")
 }
}

</SCRIPT>
</div>