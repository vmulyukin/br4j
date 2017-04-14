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
package com.aplana.cms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.aplana.cms.cache.CacheManager;
import com.aplana.cms.cache.CachingDataServiceBean;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.ServiceQuery;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataServiceBean;

public class ContentServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	public static final String PARAM_VIEWS = "views";
	private static final String PARAM_STRICT = "strict"; // represents whether we need to use the first found view card provided in PARAM_VIEWS parameter
	
	protected Log logger = LogFactory.getLog(getClass());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException 
	{
		resp.setContentType("text/html");
		resp.setCharacterEncoding("UTF-8");

		ServletContentRequest wrappedReq = new ServletContentRequest(req);
		ServletContentResponse wrappedResp = new ServletContentResponse(req, resp);
		ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext()); 
		ContentProducer cms = new ContentProducer(wrappedReq, wrappedResp, applicationContext);
		Card area = cms.getCurrentSiteArea();
		if (area == null)
			return;
        // reading ID only
		Card content = cms.getCurrentContent(true);
		if (content == null)
			return;
        // reading template for content card
		cms.readContentTemplateAndStatus(content);

		Card viewCard = cms.getContentPresentationCard(content, area, getDefaultViewIds(wrappedReq), isStrict(wrappedReq));
        if (viewCard == null) return;

        content = cms.fetchContent(content, viewCard);
        if (content == null) return;


        String view = cms.getContentPresentation(viewCard);
		cms.writeContent(resp.getWriter(), view, content);

		// Writing to log collected statistics
		try {
			cms.getService().doAction(new ServiceQuery("cacheStats"));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ServletProcessRequest wrappedReq = new ServletProcessRequest(req);
		ServletProcessResponse wrappedResp = new ServletProcessResponse(resp);
		if (wrappedReq.getParameter(ProcessRequest.PARAM_FORM) == null) {
			logger.warn("Action request without form!");
			return;
		}
		Object proc = TagFactory.getProcessor(wrappedReq.getParameter(ProcessRequest.PARAM_FORM));
		if (proc == null || !(proc instanceof FormProcessor)) {
			logger.error("Processor for form " + proc + " not found");
			return;
		}
		boolean result = ((FormProcessor) proc).processForm(wrappedReq, wrappedResp,
				CachingDataServiceBean.createBean(wrappedReq));
		if (!result) {//if there is a Failure
			writeError(req, resp);
		}
	}

	private void writeError(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		try {
			
			resp.setCharacterEncoding("UTF-8");
			
			String error = (String)req.getSession().getAttribute(ContentProducer.SESS_ATTR_ERROR);
		
			StringBuffer buffer = new StringBuffer("error=");
			buffer.append(error);
			
			resp.getWriter().write(buffer.toString());
			
		} catch (IOException e) {
			throw new ServletException(e);
		}	
	}
	
	private String getDefaultViewIds(ContentRequest request) {
		return request.getParameter(PARAM_VIEWS);
	}
	
	private boolean isStrict(ContentRequest request) {
		return Boolean.parseBoolean(request.getParameter(PARAM_STRICT));
	}
}
