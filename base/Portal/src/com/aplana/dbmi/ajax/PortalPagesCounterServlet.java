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

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

public class PortalPagesCounterServlet extends HttpServlet {
	
	protected final Log logger = LogFactory.getLog(getClass());

	private static final String PARAM_XML = "xml";
	private static final String PARAM_LIMIT = "limit";
	private static final String CONFIG_FILE_PREFIX = "dbmi/";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (req.getUserPrincipal() == null) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		long limit = 0;
		if (req.getParameter(PARAM_LIMIT) != null) {
			limit = Long.parseLong(req.getParameter(PARAM_LIMIT));
		}

		String xml;
		if (req.getParameter(PARAM_XML) != null) {
			xml = req.getParameter(PARAM_XML);
		} else {
			logger.error("Wrong XML parameter. Xml = " + null);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		resp.setContentType("text/plain");
		resp.setCharacterEncoding("UTF-8");

		try {
			Search search = createSearch(xml);
			AsyncDataServiceBean service = createService(req);

			String actualCount;
			if (limit > 0){
				search.setSearchLimit(limit+1);
				search.setCountOnly(true);
				service.doAction(search);
				if (search.getFilter().getWholeSize() > limit){
					actualCount = limit + "+";
				} else {
					actualCount = String.valueOf(search.getFilter().getWholeSize());
				}
			} else {
				search.setCountOnly(true);
				service.doAction(search);
				actualCount = String.valueOf(search.getFilter().getWholeSize());
			}

			JSONWriter writer = new JSONWriter(resp.getWriter());
			writer.object();
			writer.key("count").value(actualCount);
			writer.endObject();
		} catch (Exception ex) {
			logger.error(ex);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private Search createSearch(String searchSettings) throws DataException, IOException {
		Search search = new Search();
		if (searchSettings != null) {
			InputStream inputStream = Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILE_PREFIX + searchSettings);
			search.initFromXml(inputStream);
			inputStream.close();
		}
		search.setDontFetch(true);
		if (search.getFilter()!=null)
			search.getFilter().setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
		return search;
	}

	private AsyncDataServiceBean createService(HttpServletRequest req) {
		AsyncDataServiceBean bean = ServletUtil.createService(req);
		return bean;
	}
}
