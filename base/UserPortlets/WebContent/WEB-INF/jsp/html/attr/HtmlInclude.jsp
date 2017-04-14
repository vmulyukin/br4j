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
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean"%>
<%@page import="com.aplana.dbmi.card.HtmlAttributeEditor"%>
<%@page import="com.aplana.dbmi.gui.LinkChooser"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<% CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(renderRequest); %>

<script language="javascript" src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/tiny_mce/tiny_mce.js") %>">
</script>
<%--<script type="text/javascript" src="tiny_mce/tiny_mce.js" language="javascript"></script>--%>
<script type="text/javascript" language="javascript">
    var contentCardLinkType = "link";
    var contentFileLinkType = "file";
    var contentImageLinkType = "image";

    var contentTypeVisualClassMap = {};
    contentTypeVisualClassMap[contentCardLinkType] = "cardLink";
    contentTypeVisualClassMap[contentFileLinkType] = "fileLink";
    contentTypeVisualClassMap[contentImageLinkType] = "imageLink";

    var contentTagName = "content";
    var contentTypeAttributeName = "type";
    var contentIdAttributeName = "id";
    var markerSpanId = "marker";
    var dbmiContentAttributeName = "dbmiContent"; // Used just as marker attribute
    var insertLinkCommandName = "InsertDBMIContentLink";

	function myCustomSetupContent(ed, data) {
		var editor = ed;
		var dataAvailable = <%= sessionBean.getLinkChooserBean().isDataAvailable() %>;
		if (dataAvailable == true) {		
			var editorName = '<%= sessionBean.getLinkChooserBean().getSelectedEditor() %>';				  		
			if((editorName != null) && (editor.id == editorName)) {
				//-------------substiture the marker tag									
				var linkType = '<%= sessionBean.getLinkChooserBean().getLinkType() %>';
				//This call have to be the last beacuse after the getSelectedEditor the data are no more available				
				var editorValue = '<%= sessionBean.getLinkChooserBean().getSelected() %>';		
                var contentElt = editor.dom.create(contentTagName);
                editor.dom.setAttrib(contentElt, contentTypeAttributeName, linkType);
                editor.dom.setAttrib(contentElt, contentIdAttributeName, editorValue);
                
                var markers = editor.dom.select("span#" + markerSpanId, data.node);
                editor.dom.replace(contentElt, markers);
			}
		}
		replaceContent(ed, data);		
	}

    function copyAttributes(dom, fromElt, toElt) {
        tinymce.each(fromElt.attributes, function(attr){
            if (attr.value && attr.value != null && attr.specified)
                dom.setAttrib(toElt, attr.name, attr.value);
        });
    }

    function replaceContent(ed, o) {
        var contentElts = ed.dom.select(contentTagName, o.node);
        tinymce.each(contentElts, function(contentElt, index) {
            var imgElt = ed.dom.create("span");
            ed.dom.setAttrib(imgElt, dbmiContentAttributeName, "content");
            copyAttributes(ed.dom, contentElt, imgElt);
            var linkType = ed.dom.getAttrib(contentElt, contentTypeAttributeName);
            var visualClass = contentTypeVisualClassMap[linkType];
            if (visualClass == null) {
                visualClass = "";
            }
            ed.dom.addClass(imgElt, visualClass);
            
            ed.dom.replace(imgElt, contentElt, true);
        });
    }

    function replaceContentBack(ed, o) {
        var contentImgElts = ed.dom.select("span", o.node);
        tinymce.each(contentImgElts, function(contentImgElt, index) {
            if (ed.dom.getAttrib(contentImgElt, dbmiContentAttributeName, null) == null)
                return;
            var contentElt = ed.dom.create(contentTagName);
            copyAttributes(ed.dom, contentImgElt, contentElt);
            ed.dom.setAttrib(contentElt, dbmiContentAttributeName, null);

            var linkType = ed.dom.getAttrib(contentImgElt, contentTypeAttributeName);
            var visualClass = contentTypeVisualClassMap[linkType];
            if (visualClass == null) {
                visualClass = "";
            }
            ed.dom.removeClass(contentElt, visualClass);
            ed.dom.replace(contentElt, contentImgElt, true);
        });
    }

	function submitFormChooseLink(attributeId, contentType) {
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = '<%= HtmlAttributeEditor.CHOOSE_LINK_ACTION %>';
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ATTR_ID_FIELD %>.value = attributeId;
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= LinkChooser.CONTENT_TYPE %>.value = contentType;         
		document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();
	}


tinyMCE.init({
	// General options
	mode : "specific_textareas",
	theme : "advanced",
	editor_selector : "mceEditor",
	
	plugins: "safari,spellchecker,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,imagemanager,filemanager",
//	plugins: "safari,spellchecker,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,imagemanager,filemanager",

	// Theme options
	theme_advanced_buttons1 : "contentFile,contentImage,contentLink,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,|,styleselect,formatselect,fontselect,fontsizeselect",
	theme_advanced_buttons2 : "cut,copy,paste,pastetext,pasteword,|,search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,cleanup,help,code,|,insertdate,inserttime,preview,|,forecolor,backcolor",
	theme_advanced_buttons3 : "tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,emotions,iespell,media,advhr,|,print,|,ltr,rtl,|,fullscreen",
	//theme_advanced_buttons4 : "insertlayer,moveforward,movebackward,absolute,|,styleprops,spellchecker,|,cite,abbr,acronym,del,ins,attribs,|,visualchars,nonbreaking,template,blockquote,pagebreak,|,insertfile,insertimage",
	theme_advanced_toolbar_location : "top",
	theme_advanced_toolbar_align : "left",
	theme_advanced_statusbar_location : "bottom",	
	theme_advanced_resizing : true,
	extended_valid_elements : contentTagName + "[*],span[*]",
//	custom_elements : "~content",
	content_css : "/DBMI-NSI-Portal/themeCMS/dbmi_style.css",
//	content_css : "/MosSvet-Portal/themeCMS/dbmi_style.css",

	cleanup_on_startup : true,
	
	setup : function(ed) {
	
      ed.onPreProcess.add(function(ed, o) {
          if (o.set)
           	 myCustomSetupContent(ed, o);
          else if (o.get)
             replaceContentBack(ed, o);
      });
	
      // Register example command
      ed.addCommand(insertLinkCommandName, function(ui, type){
		  var marker = '<span id="'+ markerSpanId + '">&nbsp;</span>';
			tinyMCE.activeEditor.selection.setContent(marker);
			submitFormChooseLink(tinyMCE.activeEditor.id, type);
		});
		
		// Add a custom button
		ed.addButton("contentLink", {
			title : "Insert content link",
			image : '<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/pencil.gif") %>',
			cmd : insertLinkCommandName,
			value : contentCardLinkType
		});
		
		// Add a custom button
		ed.addButton("contentFile", {
			title : "Insert content file link",
			image : '<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/pencil.gif") %>',
			cmd : insertLinkCommandName,
			value : contentFileLinkType					
		});
		
		// Add a custom button
		ed.addButton("contentImage", {
			title : "Insert content image link",
			image : '<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/pencil.gif") %>',
			cmd : insertLinkCommandName,
            value: contentImageLinkType					
		});
	}
});
//--></script>
