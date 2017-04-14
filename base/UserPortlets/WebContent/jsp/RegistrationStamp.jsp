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
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.text.SimpleDateFormat"%>
    
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.StringAttribute"%>
<%@page import="com.aplana.dbmi.model.IntegerAttribute"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean"%>
<%@page import="com.aplana.dbmi.model.Template"%>
<%@page import="com.aplana.dbmi.model.TextAttribute"%>

<%
	String namespace = request.getParameter("namespace");
	CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request, namespace);
	Card card = sessionBean.getActiveCard();
	
	Long cardId = (Long)card.getId().getId();
	
	String regnum = "";
	StringAttribute attrRegnum = (StringAttribute)card.getAttributeById(ObjectId.predefined(StringAttribute.class, "regnumber"));
	if (attrRegnum != null && attrRegnum.getValue() != null) {
		regnum = attrRegnum.getValue();
	}
	
	String date = "";
	//String time = "";
	DateAttribute attrDatereg = (DateAttribute)card.getAttributeById(ObjectId.predefined(DateAttribute.class, "regdate"));
	if (attrDatereg != null && attrDatereg.getValue() != null) {
		date = (new SimpleDateFormat("dd.MM.yyyy")).format(attrDatereg.getValue());
		//time = (new SimpleDateFormat("HH:mm:ss")).format(attrDatereg.getValue());
	}
	/*
	IntegerAttribute attrQPaper= (IntegerAttribute)card.getAttributeById(ObjectId.predefined(IntegerAttribute.class, "jbr.original.quantitypaper"));
	int qPaper = attrQPaper.getValue();
	
	IntegerAttribute attrQCopy= (IntegerAttribute)card.getAttributeById(ObjectId.predefined(IntegerAttribute.class, "jbr.original.quantitycopy"));
	int qCopy = attrQCopy.getValue();
	
	IntegerAttribute attrQAttachPaper= (IntegerAttribute)card.getAttributeById(ObjectId.predefined(IntegerAttribute.class, "jbr.original.attach.quantitypaper"));
	int qAttachPaper = attrQAttachPaper.getValue();
	
	String comments = "";
	ObjectId tmpIncoming  = ObjectId.predefined(Template.class, "jbr.incoming");
	ObjectId tmpOG = ObjectId.predefined(Template.class, "jbr.incomingpeople");
	ObjectId tmpIZ = ObjectId.predefined(Template.class, "jbr.informationrequest");
	ObjectId tmpCurrent = card.getTemplate();
	if (tmpCurrent.equals(tmpIncoming) ||
		tmpCurrent.equals(tmpOG) ||
		tmpCurrent.equals(tmpIZ)) {
		TextAttribute attrComments = (TextAttribute)card.getAttributeById(ObjectId.predefined(TextAttribute.class, "jbr.original.commentOnApp"));
		comments = attrComments.getStringValue();
	}
	
	if (comments.trim().length() > 0) {
		comments = "+ " + comments;
	}
	*/
	
	String serverPath = (request.isSecure() ? "https" : "http") + "://" + request.getServerName() + ":" + request.getServerPort();

	/* +--+--+--+--+
	** |1 |2 |3 |4 |
	** +--+--+--+--+
	** |5 |6 |7 |8 |
	** +--+--+--+--+
	*/
	String v = "";
	String h = "";
	String parPosition = request.getParameter("position");
	if (parPosition == null) {
		v = "bottom: 0";
		h = "right: 0";
	} else {
		int position = Integer.parseInt(parPosition);
		switch (position) {
			case 1: v = "top: 0"; h="left: 0"; break;
			case 2: v = "top: 0"; h="left: 25%"; break;
			case 3: v = "top: 0"; h="left: 50%"; break;
			case 4: v = "top: 0"; h="right: 0"; break;
			case 5: v = "bottom: 0"; h="left: 0"; break;
			case 6: v = "bottom: 0"; h="left: 25%"; break;
			case 7: v = "bottom: 0"; h = "left: 50%"; break;
			case 8: v = "bottom: 0"; h = "right: 0"; break; 
		}	
	}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="com.aplana.dbmi.model.DateAttribute"%>
<%@page import="java.text.SimpleDateFormat;"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<style type="text/css">
    .regionPrint { 
        position: absolute;
        <%=v%>;
        <%=h%>;
        font-family: 'Times New Roman';
     	font-size: 10pt;
    }
 </style>
<title>Регистрационный штамп</title>

<script type="text/javascript">
function printpage()
  {
  window.print()
  }
</script>
</head>
<body onLoad="printpage()">
	<div class="regionPrint">
		<img src="<%=response.encodeURL(serverPath+request.getContextPath()+"/BarCodeServlet?cardId="+cardId+"&size=42")%> align="center"/>
		<br>
		<table border="0" width="189" id="table2" align="center">
			<td align="center"><b><%=regnum%></b></td>
			<td align="center"><b><%=date%></b></td>
		</table>
	</div>
</body>
</html>