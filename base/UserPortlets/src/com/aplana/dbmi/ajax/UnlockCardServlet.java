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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.aplana.dbmi.action.UnlockClosedObjects;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class UnlockCardServlet extends AbstractDBMIAjaxServlet {
	
	public static final String PARAM_CARD_ID = "id";

	@Override
	protected void generateResponse(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		//response.setHeader("Cache-control", "no-cache");
		//response.setHeader("Pragma", "no-cache");
		
		String id = request.getParameter(PARAM_CARD_ID);
		
		if(StringUtils.isEmpty(id)) {
			signout(request, response);
			return;
		}
		
		ObjectId cardId = new ObjectId(Card.class, Long.parseLong(id));
		DataServiceBean service = getDataServiceBean(request);
		UnlockClosedObjects action = new UnlockClosedObjects(cardId);
		try {
			service.doAction(action);
		} catch (DataException e) {
			logger.error(e);
		} catch (ServiceException e) {
			logger.error(e);
		} finally {
			signout(request, response);
		}
	}
	
	private void signout(HttpServletRequest request, HttpServletResponse response) {
		try {
			//response.sendRedirect(request.getRequestURL().substring(0,request.getRequestURL().indexOf(request.getRequestURI())) + "/portal/signout/");
			response.sendRedirect(request.getRequestURL().toString().replaceAll(request.getRequestURI(), "/portal/signout/"));
		} catch (IOException e) {
			logger.error(e);
		}
	}

}
