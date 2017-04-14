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
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@ page import="com.aplana.dbmi.card.*" %>
<%@ page import="com.aplana.dbmi.model.ObjectId" %>
<%@ page import="com.aplana.dbmi.model.Template" %>
<%@taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt"%>
<%@taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource" />

<%
    Attribute attr = (Attribute)request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);

    String attrCode = (String)attr.getId().getId();
%>
<c:set var="attrHtmlId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>"/>

<script language="JavaScript" type="text/javascript">

dojo.require("dijit._base.popup");

function ${attrHtmlId}_getOnLoadByActionName(actionName) {
    if (${attrHtmlId}_startonload) {
        for (var key in ${attrHtmlId}_startonload) {
            if (${attrHtmlId}_startonload[key].name && ${attrHtmlId}_startonload[key].name == actionName) {
                return ${attrHtmlId}_startonload[key].value;
            }
        }
    }
    return null;
}

function ${attrHtmlId}_dbmiShowLoadingSplash() {
    dojo.byId('dbmiUploadingSplash').style.display = 'block';
}

function ${attrHtmlId}_dbmiHideLoadingSplash() {
    dojo.byId('dbmiUploadingSplash').style.display = 'none';
}

String.prototype.format = function() {
    var formatted = this;
    for (var i = 0; i < arguments.length; i++) {
        var regexp = new RegExp('\\{' + i + '\\}', 'gi');
        formatted = formatted.replace(regexp, arguments[i]);
    }
    return formatted;
};

function handleError(err, errorFromApplet) {
    var errorMessageFormat = '<fmt:message key="download.error.common"/>';

    var errorMessage = null;

    if (errorFromApplet) {
        errorMessage = errorFromApplet;
    } else {
        errorMessage = err;
    }

    if (errorMessage.message) {
        errorMessage = err.message;
    }

    alert(errorMessageFormat.format(errorMessage));
}

function ${attrHtmlId}_downloadCardToFile(downloadvalues) {

    var prompt = "<fmt:message key="download.select.directory.title"/>";
    var appl = dojo.byId("DownloadCardsApplet");
    var directoryFullPath = null;

    if (appl ) {
        try {
            appl.setLocale('<%=request.getLocale().getLanguage()%>', '<%=request.getLocale().getCountry()%>');
            directoryFullPath = appl.selectFolder(prompt);
        } catch (err) {
            handleError(err, appl.getErrorMessage());
        }
    }
    if (directoryFullPath) {
        if (downloadvalues) {            
            if (${attrHtmlId}_dbmiShowLoadingSplash) {
                ${attrHtmlId}_dbmiShowLoadingSplash();
            }            

            try {
                var cardDescr;
                var url = self.location.protocol + "//" + self.location.host + "<%=request.getContextPath()%>";
                appl.setContextUrl(url);
                appl.setNamespace('<portlet:namespace/>');
                appl.setWorkingDirectory(directoryFullPath);
                
                for (var i = 0; i < downloadvalues.length; i++) {
                    cardDescr = downloadvalues[i];
                    appl.exportCardToFile(cardDescr.cardid, cardDescr.recipientid, 'passport.xml');
                }
            } catch (err){
                handleError(err, appl.getErrorMessage());
            } finally{
                if (${attrHtmlId}_dbmiHideLoadingSplash) {
                    ${attrHtmlId}_dbmiHideLoadingSplash();
                }
            }
        }
    }

}

function ${attrHtmlId}_DownloadCards(ref){

    ref.parent.store.fetch({
        query: {cardId: '?*', checked: true},
        queryOptions: {deep: true},
        actionId: this.jsId,
        hierarchicalList: ref.parent,
        onComplete: function(items, request) {
            var sel = [];
            var store = request.hierarchicalList.store;
            for (var i = 0; i < items.length; ++i) {
                sel[sel.length] = store.getValue(items[i], 'cardId');
            }
            var selectedItems = sel.join(',');

            var xhrArgs = {
                    url: "<%=request.getContextPath()+"/DownloadCardsServlet"%>",
                    content: {
                               '<%=DistributionHierarchicalCardLinkAttributeViewer.PARAM_ATTR_CODE%>': '<%=attrCode%>',
                               '<%=DistributionHierarchicalCardLinkAttributeViewer.PARAM_NAMESPACE%>': '<portlet:namespace/>',
                               '${attrHtmlId}_selectedItems' : selectedItems
                    },
                    handleAs: "json",
                    load: function(data) {

                        ${attrHtmlId}_downloadCardToFile(data.items);

                        ref.parent.store.fetch({
                                        query: {cardId: '?*', checked: true},
                                        queryOptions: {deep: true},
                                        onComplete: dojo.hitch(function(items, request) {
                                               dojo.forEach(items,function(item){
                                                   var newValue = false;
                                                   var currStore = ref.parent.model.store;
						                           currStore.setValue(item,"checked",newValue);
					                           });
                                        })
                        });

                    },
                    error: function(error) {
                        alert("<fmt:message key="download.error.unexpected"/>"); 
                    }
                };

                dojo.xhrGet(xhrArgs);

        }
    });
}

</script>

<%

    final ObjectId jbrOutcomming = ObjectId.predefined(Template.class, "jbr.outcoming");
    String namespace = renderResponse.getNamespace();
	CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request, namespace);

    final Long currentTemplate = (Long)sessionBean.getActiveCard().getTemplate().getId();
    if (currentTemplate != null && jbrOutcomming != null && jbrOutcomming.getId().equals(currentTemplate))
    {

%>

    <div id="dbmiUploadingSplash" style="position:fixed; top: 0; left: 0;  width: 100%;  height: 120%; z-index:99; display: none; " >
        <div style="width:100%; height:100%; background-color: #ffffff;">
            <br />
            <br />
            <p style="text-align: center;">
            <center><img src="/DBMI-Portal/js/dbmiCustom/images/dbmi_loading.gif" border="0" alt=""></center>
            </p>
        </div>
    </div>

    <applet name="DownloadCardsApplet"	id="DownloadCardsApplet"
            codebase="<%=request.getContextPath()%>"
            archive="SJBCrypto.jar"
            code="com.aplana.crypto.DownloadCardsApplet.class"	WIDTH=1	HEIGHT=1>

            <param name="download.applet.file.name" value="<fmt:message key="download.applet.file.name"/>">
            <param name="download.applet.folder.name" value="<fmt:message key="download.applet.folder.name"/>">
            <param name="download.applet.folder" value="<fmt:message key="download.applet.folder"/>">
            <param name="download.applet.type" value="<fmt:message key="download.applet.type"/>">
            <param name="download.applet.cancel" value="<fmt:message key="download.applet.cancel"/>">
            <param name="download.applet.select" value="<fmt:message key="download.applet.select"/>">

            <H1>WARNING!</H1>
            The browser you are using is unable to load Java Applets!
    </applet>

<%
}
%>

<jsp:include page="HierarchicalCardListView.jsp"/>
