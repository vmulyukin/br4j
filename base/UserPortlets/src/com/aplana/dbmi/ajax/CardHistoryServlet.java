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

import com.aplana.dbmi.action.GetCardHistory;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardHistoryRecord;
import com.aplana.dbmi.model.ObjectId;
import org.json.JSONException;
import org.json.JSONWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;

public class CardHistoryServlet extends AbstractDBMIAjaxServlet {

	private static final String PARAM_LIMIT = "limit";
	private static final String PARAM_OFFSET = "offset";
	private static final String PARAM_CARD_ID = "cardId";

	@Override
	protected void generateResponse(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long limit = 1000; //default limit
		long offset = 0; //initial offset
		long cardId = Long.parseLong(request.getParameter(PARAM_CARD_ID));

		if (request.getParameter(PARAM_LIMIT) != null) {
			limit = Long.parseLong(request.getParameter(PARAM_LIMIT));
		}
		if (request.getParameter(PARAM_OFFSET) != null) {
			offset = Long.parseLong(request.getParameter(PARAM_OFFSET));
		}

		List<CardHistoryRecord> records;
		try {
			GetCardHistory action = new GetCardHistory();
			action.setCard(new ObjectId(Card.class, cardId));
			action.setLimit(limit);
			action.setOffset(offset);
			records = getDataServiceBean(request).doAction(action);
		} catch (Exception e) {
			logger.error("Error retrieving stored messages", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		try {
			JSONWriter writer = new JSONWriter(response.getWriter());
			writer.array();
			for (CardHistoryRecord rec : records) {
				writer.object();
				writer.key("actorFullName").value(rec.getActorFullName());
				writer.key("date").value(sdf.format(rec.getDate()));
				writer.key("comment").value(rec.getComment());
				String versionId = rec.getVersionId();
				if (versionId == null || "".equals(versionId)) {
					writer.key("link").value(rec.getActionName());
				} else {
					String href = "<a href=\"" + request.getContextPath() + "/servlet/JasperReportServlet?"
							+ "nameConfig=reportChartCardChanges"
							+ "&cardId=L_" + cardId
							+ "&versionId=L_" + versionId
							+ "&actor=S_" + URLEncoder.encode(rec.getActorFullName(), "UTF-8")
							+ "&date=S_" + URLEncoder.encode(sdf.format(rec.getDate()), "UTF-8")
							+ "&action=S_" + URLEncoder.encode(rec.getActionName(), "UTF-8")
							+ "&attrExist=S_" + URLEncoder.encode(rec.getVisibleAttr(), "UTF-8")
							+"\" target=\"_blank\">" + rec.getActionName()
							+ "</a>";
					writer.key("link").value(href);
				}
				writer.endObject();
			}
			writer.endArray();
		} catch (JSONException e) {
			logger.error("Error creating JSON object", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
