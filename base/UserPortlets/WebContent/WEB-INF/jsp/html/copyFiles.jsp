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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>

<%@page import="java.util.*" %>
<%@page import="javax.portlet.*"%>
<%@page import="com.aplana.dbmi.card.*"%>
<%@page import="com.aplana.dbmi.card.actionhandler.jbr.UploadFilesForm"%>
<%@page import="com.aplana.dbmi.model.CardLinkAttribute"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.StringAttribute"%>
<%@page import="com.aplana.dbmi.model.HtmlAttribute"%>
<%@page import="com.aplana.dbmi.crypto.*"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.crypto.CryptoLayer"%>
<%@page import="com.aplana.crypto.CryptoApplet"%>
<%@page import="com.aplana.dbmi.Portal"%>
<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource" />
<portlet:defineObjects/>

<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource" scope="request"/>

<%
PortletURL backURL = renderResponse.createActionURL();
backURL.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.BACK_ACTION); 
backURL.setWindowState(WindowState.NORMAL);
ObjectId signatureAttributeId = ObjectId.predefined(
        HtmlAttribute.class, "jbr.uzdo.signature");
CardLinkAttribute attr = (CardLinkAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
CardPortletSessionBean sessionBean = (CardPortletSessionBean)renderRequest.getPortletSession().
                                    getAttribute(CardPortlet.SESSION_BEAN);
boolean isDsSupport = sessionBean.isDsSupport(renderRequest);
String forceDownload = request.getParameter("forceDownload");
String checkedCards = request.getParameter("checked.cards");
List checkedIds = null;
if (checkedCards != null && checkedCards.length()>0){
	checkedIds = Arrays.asList(checkedCards.split(";"));
}
CardLinkAttribute links_attr = (CardLinkAttribute)sessionBean.getActiveCard().getAttributeById(ObjectId.predefined(CardLinkAttribute.class, "jbr.files"));
ObjectId attrName = ObjectId.predefined(StringAttribute.class, "name");
ObjectId[] links = null;
Card[] linked_cards = null;
AttachInfo[] attInfo = null;
if( links_attr != null ) {
    links = links_attr.getIdsArray();
    if( links != null ) {
        linked_cards = new Card[links.length];
        attInfo = new AttachInfo[links.length];
        for( int i=0; i<links.length; i++ ) {
            linked_cards[i] = (Card) sessionBean.getServiceBean().getById( new ObjectId(Card.class, links[i].getId()) );
            AttachInfo attInfoObject= new AttachInfo();
            String attachName = ((StringAttribute) linked_cards[i].getAttributeById(attrName)).getValue();
            SignatureConfig sConf = new SignatureConfig(sessionBean.getServiceBean(), linked_cards[i]);
            SignatureData sData = new SignatureData(sConf, linked_cards[i]);
            HtmlAttribute signatureAttribute = (HtmlAttribute) linked_cards[i].getAttributeById(
                    signatureAttributeId);
            attInfoObject.setAttachText(attachName);
            attInfoObject.setAttId(links[i].getId().toString());
            attInfoObject.setAttrXML(sData.getAttrXML());
            attInfoObject.setHash(sData.getAttrValues(sessionBean.getServiceBean(), true, null));
            attInfoObject.setCurrentSignature(signatureAttribute.getStringValue());
            attInfo[i] = attInfoObject;
        }
    }
}
String message = sessionBean.getMessage();
if( message != null) {
    sessionBean.setMessage(null);
} else {
    message = renderRequest.getParameter(CardPortlet.MSG_PARAM_NAME);
}
%>
<script language="JavaScript" type="text/javascript">

dojo.require("dijit._base.popup");
dojo.require('dijit.form.Button');
dojo.require('dijit.Menu'); 
dojo.require('dijit.Dialog');

function setChecked(obj)
{
var check = document.getElementsByName("ids");
for (var i=0; i<check.length; i++)
   {
   check[i].checked = obj.checked;
   }
}

String.prototype.format = function() {
    var formatted = this;
    for (var i = 0; i < arguments.length; i++) {
        var regexp = new RegExp('\\{' + i + '\\}', 'gi');
        formatted = formatted.replace(regexp, arguments[i]);
    }
    return formatted;
};

function handleError(err) {
    var errorMessageFormat = '<fmt:message key="download.error.common"/>';
    var errorMessage;
    if (err.message) {
        errorMessage = err.message;
    } else {
        errorMessage = err;
    }
    alert(errorMessageFormat.format(errorMessage));
}
function buttonClick() {
	var check = document.getElementsByName("ids");
	var showalert = true;
    for (var i=0; i<check.length; i++){
        if (check[i].checked) showalert= false;
    }
    if (showalert) {
    	alert('<fmt:message key="choose.one.file.atleast"/>');
    } else {
        <%if (isDsSupport) {%>
    	dijit.byId('signQuery').show();
    	<%} else {%>
    	alert('<fmt:message key="need.certificate.material"/>');
    	copyFiles();
    	<%}%>
    }
}

function signDocument(){
    var ids = [];
    var hashesToSign = [];
    var attrXmls = [];
    var currentSignature = [];

    var check = document.getElementsByName("ids");
    var hashes = document.getElementsByName("hashes");
    var attrXML = document.getElementsByName("attrXMLs");
    var currentSignatures = document.getElementsByName("currentSignature");
    var index = 0;
    for (var i=0; i<check.length; i++){
        if (check[i].checked) {
            ids[index] = check[i].value;
            hashesToSign[index] = hashes[i].value;
            attrXmls[index] = attrXML[i].value;
            currentSignature[index] = currentSignatures[i].value;
            index++;
        }
    }
    var args = {
            stringsArray: hashesToSign, 
            signAttrXML: attrXmls
            };
    var msg = "";
    var signResult = cryptoGetSignature(args);
    if(signResult.success){ 
        var signs = "";
        var signIds = "";
        if(ids.length > 0) {
            for(i = 0; i < ids.length; i ++) {
                if (i > 0){
                    signs += ";";
                    signIds += ";";
                }
                if (currentSignature[i].length > 0){
                	signs += currentSignature[i];
                }
                signs += signResult.signature[i];
                signIds += ids[i];
            }
        }       
                
        submitForm_SignCard(signs, signIds);
    } else{
        if(signResult.msg == "noapplet"){           
            msg = "Апплет не инициализирован"
        } else if(signResult.msg == "nofields"){
            msg = "Нет подписываемых аттрибутов";
        } else {
            msg = signResult.msg;
        }
        if(msg && msg.length > 0) {
            alert(msg);
        }       
    }
}   

function submitForm_SignCard(value, signIds) {
    document.signForm.signature.value = value;
    document.signForm.signIds.value = signIds;
    document.signForm.submit();
}

function submitForm_Download(value) {       
    document.downloadForm.download.value = "download";
    document.downloadForm.checkedCards.value = value;
    document.downloadForm.submit();
}

function copyFiles() {
    var prompt = "<fmt:message key="download.select.directory.title"/>";
    var appl = dojo.byId("DownloadCardsApplet");
    var directoryFullPath = null;
    var showsplash = false;
    var check = document.getElementsByName("ids");
    for (var i=0; i<check.length; i++){
        if (check[i].checked) showsplash= true;
    }
    
    if (appl ) {
        try {
            appl.setLocale('<%=request.getLocale().getLanguage()%>', '<%=request.getLocale().getCountry()%>');
            directoryFullPath = appl.selectFolder(prompt);
        } catch (err) {
        	handleError(err);
        }
    }
    if (directoryFullPath) {
        if (showsplash) {            
        	dojo.byId('dbmiUploadingSplash').style.display = 'block';

            try {
                var cardDescr;
                var url = self.location.protocol + "//" + self.location.host + "<%=request.getContextPath()%>";
                appl.setContextUrl(url);
                appl.setNamespace('<portlet:namespace/>');
                var names = document.getElementsByName("names");
                var ids = "";
                var ind = 0;
                for (var i = 0; i < check.length; i++) {
                    if (check[i].checked) {
                    	appl.setWorkingDirectory(directoryFullPath + "\\_" + names[i].value);
                        appl.downloadFile(check[i].value, names[i].value);
                        appl.downloadSignatures(<%=sessionBean.getActiveCard().getId().getId()%>, check[i].value,<%if (forceDownload != null) {%>"false"<%}else{%>"true"<%}%>);
                        appl.writeHistory(check[i].value);
                        if (ind > 0){
                        	ids += ";";
                        }
                        ids += check[i].value;
                        ind++;
                    }
                }
                submitForm_Download(ids);
            } catch (err){
            	handleError(err);
            	dojo.byId('dbmiUploadingSplash').style.display = 'none';
            }
        }
    }

}
</script>

<div id="dbmiUploadingSplash" style="position:fixed; top: 0; left: 0;  width: 100%;  height: 120%; z-index:99; display: none; " >
        <div style="width:100%; height:100%; background-color: #ffffff;">
            <br />
            <br />
            <p style="text-align: center;">
            <center><img src="/DBMI-Portal/js/dbmiCustom/images/dbmi_loading.gif" border="0" alt=""></center>
            </p>
        </div>
</div>
<applet name="DownloadCardsApplet"  id="DownloadCardsApplet"
            codebase="<%=request.getContextPath()%>"
            archive="SJBCrypto.jar"
            code="com.aplana.crypto.DownloadCardsApplet.class"  WIDTH=1 HEIGHT=1>

            <param name="download.applet.file.name" value="<fmt:message key="download.applet.file.name"/>">
            <param name="download.applet.folder.name" value="<fmt:message key="download.applet.folder.name"/>">
            <param name="download.applet.folder" value="<fmt:message key="download.applet.folder"/>">
            <param name="download.applet.type" value="<fmt:message key="download.applet.type"/>">
            <param name="download.applet.cancel" value="<fmt:message key="download.applet.cancel"/>">
            <param name="download.applet.select" value="<fmt:message key="download.applet.select"/>">

            <H1>WARNING!</H1>
            The browser you are using is unable to load Java Applets!
</applet>
<%if (forceDownload != null) {%>
<script type="text/javascript">
dojo.addOnLoad(function() {
    dbmiHideLoadingSplash();
    copyFiles();
});
</script>
<%}%>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/crypto.js?open&4" ></script>   
<applet name="CryptoApplet" id="CryptoApplet"
            codebase="<%=request.getContextPath()%>"
            archive="SJBCrypto.jar" 
            code="com.aplana.crypto.CryptoApplet.class" WIDTH="1" HEIGHT="1">
            <param name="signOnLoad" value="true">
            <param name="crypto.layer" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER)%>">
            <param name="crypto.layer.params" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER_PARAMS)%>">
		<param name="timestamp.address" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_TIMESTAMP_ADDRESS)%>">
            <param name="<%=CryptoApplet.CURENT_USER_PARAMETER %>" value="<%=sessionBean.getServiceBean().getPerson().getId().getId().toString()%>">
            <PARAM name="separate_jvm" value="true">
</applet>
<form name="signForm" method="post" action="<portlet:actionURL/>"> 
    <input id="signature" type="hidden" name="signature"/>
    <input id="signIds" type="hidden" name="signIds" <%if (checkedCards != null) { %> value="<%=checkedCards%>"<% }%>/>
</form>
<form name="downloadForm" method="post" action="<portlet:actionURL/>"> 
    <input id="download" type="hidden" name="download"/>
    <input id="checkedCards" type="hidden" name="checkedCards" <%if (checkedCards != null) { %> value="<%=checkedCards%>"<% }%>/>
</form>
<div id="signQuery" dojoType="dijit.Dialog" title="<fmt:message key="upload.apply.ds.query.title"/>" style="width: 320px; height: 96px">
        <div style="text-align: left;"><fmt:message key="download.apply.ds.query.message"/></div>
        <div style="float:right; clear: both;" id="dialogButtons">
            <button dojoType="dijit.form.Button" type="button">
                <fmt:message key="upload.apply.ds.query.yes"/>
                <script type="dojo/method" event="onClick" args="evt">
                    dijit.byId('signQuery').hide();
                    signDocument();
                </script>       
            </button>
            <button dojoType="dijit.form.Button" type="button">
                <fmt:message key="upload.apply.ds.query.no"/>
                <script type="dojo/method" event="onClick" args="evt">
                    dijit.byId('signQuery').hide();
                    copyFiles();
                </script>       
            </button>
        </div>
    </div>  

    <dbmi:message text="<%= message %>"/>

    <table>
    <tr>
        <td style="padding-bottom: 20px;">
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
        </td>
    </tr>
    <tr>
        <td>
            <div id="selectedArea">
                <table>
                    <tr>
                        <td>
                                <table id="filesTable" style="width: 300px;" class="tableDownloadFile">
                                        <col width="280px"/>
                                        <col width="20px"/>
                                        <tr>
                                            <td colspan="2" class="rowHeadDownloadFile"><h3><fmt:message key="form.download.file.selected"/></h3></td>
                                        </tr>
                                        <tr>
                                            <th><h3><fmt:message key="form.download.file.name"/></h3></th>
                                            <th><input type="checkbox" onClick="setChecked(this)"/></th>
                                        </tr>
                                        <%if( links != null) {%>
                                            <%for( int i=0; i<links.length; i++ ) {%>
						                    <tr>
						                      
						                        <td><%=linked_cards[i].getFileName()%></td>
						                        <td>
						                          <input type="checkbox" value="<%=links[i].getId().toString()%>" <%if (checkedIds != null && checkedIds.contains(links[i].getId().toString())) { %>checked = "checked" <% } %>name="ids"></input>
						                          <input type="hidden" value="<%=linked_cards[i].getFileName()%>" name="names"></input>
						                          <input type="hidden" value="<%=attInfo[i].getHash()%>" name="hashes"></input>
						                          <input type="hidden" value='<%=attInfo[i].getAttrXML()%>' name="attrXMLs"></input>
						                          <input type="hidden" value='<%=attInfo[i].getCurrentSignature()%>' name="currentSignature"></input>
						                        </td>
						                      
						                    </tr>
						                    <%}%>
						                <%}%>
                                </table>
                        </td>
                    </tr>
                    <%if( links != null) {%>
                    <tr>
                        <td>
                            <div class="buttonPanel">
                                <ul>
                                    <li class=""
                                        onmouseout="upButton(this)" 
                                        onmouseup="upButton(this)" 
                                        onmousedown="downButton(this)" 
                                        onclick="buttonClick()">
                                        <a href="#" class=""><fmt:message key="form.download.file.download"/></a>
                                    </li>
                                </ul>
                            </div>
                        </td>
                    </tr>
                    <%}%>
                    
                </table>
            </div>
        </td>
    </tr>
    </table>
    
