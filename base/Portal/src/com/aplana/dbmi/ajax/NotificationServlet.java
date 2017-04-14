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

import com.aplana.dbmi.model.Message;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.filter.MessageFilter;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.MessageServiceBean;
import com.aplana.dbmi.service.UserPrincipal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

public class NotificationServlet extends HttpServlet {
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	public static final String PARAM_START_AFTER = "startAfter";
	public static final String PARAM_MARK_READ = "markRead";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (req.getUserPrincipal() == null) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		
		MessageServiceBean service = getService(req);
		if (req.getParameter(PARAM_MARK_READ) != null) {
			long id = Long.parseLong(req.getParameter(PARAM_MARK_READ));
			try {
				service.markRead(new ObjectId(Message.class, id));
				return;
			} catch (Exception e) {
				logger.error("Error updating message " + id, e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		}
		
		Collection<Message> messages;
		try {
			//service.canCreate(Message.class);	//***** just to authenticate user
			
			MessageFilter filter = new MessageFilter();
			if (req.getParameter(PARAM_START_AFTER) != null) {
				long startAfter = Long.parseLong(req.getParameter(PARAM_START_AFTER));
				filter.setStartAfter(new Date(startAfter));
			}
			messages = service.listMessages(filter);
		} catch (Exception e) {
			logger.error("Error retrieving stored messages", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		
		resp.setContentType("text/plain");
		resp.setCharacterEncoding("UTF-8");
		
		try {
			JSONWriter writer = new JSONWriter(resp.getWriter());
			writer.array();
			for (Message message : messages) {
				writer.object();
				writer.key("id").value(message.getId().getId());
				writer.key("text").value(message.getText());
				writer.key("sendTime").value(message.getSendTime().getTime());
				writer.key("groupId").value(message.getGroup().getId().getId());
				writer.key("groupText").value(message.getGroup().getGroupText());
				writer.endObject();
			}
			writer.endArray();
		} catch (JSONException e) {
			logger.error("Error creating JSON object", e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	protected long getLastModified(HttpServletRequest req) {
		return super.getLastModified(req);
	}

	private MessageServiceBean getService(HttpServletRequest request) {
		MessageServiceBean service = new MessageServiceBean();
		String userName = (String)request.getSession().getAttribute(DataServiceBean.USER_NAME);
		if (userName != null) {
			service.setUser(new UserPrincipal(userName));
		} else {
			service.setUser(request.getUserPrincipal());
		}
		service.setAddress(request.getRemoteAddr());
		return service;
	}
}
