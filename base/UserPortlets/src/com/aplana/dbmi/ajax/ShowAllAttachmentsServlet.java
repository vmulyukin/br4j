/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to you under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.aplana.dbmi.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONWriter;

import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.support.action.GetDocumentAttachmentsList;

/**
 * Servlet implementation class cardDSParamsServlet
 */
public class ShowAllAttachmentsServlet extends AbstractDBMIAjaxServlet {
	private static final long serialVersionUID = 1L;
       
	private static final String CARD_ID_PARAM_KEY = "cardId";
	private ResourceBundle bundle;
	
	protected GetDocumentAttachmentsList getAction(HttpServletRequest request) {
		String card = request.getParameter(CARD_ID_PARAM_KEY);
		if(card == null || card.isEmpty()){
			logger.error("There is no cardId to get attachments");
			throw new RuntimeException("Bad request. No cardId.");
		} 
		Long cardIdLong = Long.parseLong(card);
		GetDocumentAttachmentsList attachmentsList = new GetDocumentAttachmentsList();
		attachmentsList.setObjectId(new ObjectId(Card.class, cardIdLong));
		return attachmentsList;
	}

	@Override
	protected void generateResponse(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			bundle = ResourceBundle.getBundle("com.aplana.dbmi.card.nl.CardPortletResource", request.getLocale());
			
			DataServiceBean serviceBean = getDataServiceBean(request);
			List<Map<String, String>> rows = serviceBean.doAction(getAction(request));
			List<Map<String, String>> files = new ArrayList<Map<String,String>>();
			List<Map<String, String>> materials = new ArrayList<Map<String,String>>();
			Iterator<Map<String, String>> i = rows.iterator();
			while(i.hasNext()){
				Map<String, String> row = i.next();
				Long id = Long.parseLong(((String)row.get(GetDocumentAttachmentsList.ATTR_ID)));
				try {
					serviceBean.getById(new ObjectId(Card.class, id));
				} catch (DataException e) {
					continue;
				}
				if(Boolean.parseBoolean(row.get(GetDocumentAttachmentsList.ATTR_ISFILE))){
					files.add(row);
				} else {
					materials.add(row);
				}
			}
			JSONWriter writer = new JSONWriter(response.getWriter());
			writer.object();
			writer.key("label").value(bundle.getString("attachmentsDialog.label"));
			writer.key("html").value(createContent(files,materials));
			writer.endObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private String createContent(List<Map<String, String>> files, List<Map<String, String>> materials){
		StringBuilder htmlBufCode = new StringBuilder();
		htmlBufCode.append("<table class=\"res\">");
		htmlBufCode.append(getBlockContent(files, "attachmentsDialog.files"));
		htmlBufCode.append(getBlockContent(materials, "attachmentsDialog.materials"));
		htmlBufCode.append("</table>");	
		return htmlBufCode.toString(); 
	}
	
	private String getBlockContent(List<Map<String, String>> rows, String titleKey){
		if(rows.isEmpty()){
			return "";
		} else {
			StringBuilder builder = new StringBuilder();
			builder.append("<tr class=\"bold verylightGray\">");
			builder.append(getCell(bundle.getString(titleKey)));
			builder.append(getCell(""));
			builder.append("</tr>");
			for(Map<String, String> row : rows){
				builder.append("<tr>");
				builder.append(getCell(row.get(GetDocumentAttachmentsList.ATTR_NAME)));
				builder.append(getCell(getMaterialLink(row.get(GetDocumentAttachmentsList.ATTR_ID), row.get(GetDocumentAttachmentsList.ATTR_FILE))));
				builder.append("</tr>");
			}
			return builder.toString();
		}
	}
	
	private String getCell(String value){
		return "<td>" + value + "</td>";
	}
	
	protected String getMaterialLink(String cardId, String label) {
		if (cardId != null) { 

			StringBuilder sb = new StringBuilder();
			sb.append("<a href=\"/DBMI-UserPortlets/MaterialDownloadServlet?")
			   .append(CardPortlet.CARD_ID_FIELD)
			   .append("="+cardId+"\">")
			   .append(label)
			   .append("</a>");
			 return sb.toString();

		}
		return null;
	}
}
