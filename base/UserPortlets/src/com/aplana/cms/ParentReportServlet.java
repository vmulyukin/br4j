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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.action.ParentReportAction;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class ParentReportServlet extends AbstractSimpleServlet {
	
	
	public static final String PARAM_NAME = "cards";
	
	public ParentReportServlet(){
		super();
	}

	@Override
	public void process(ServletContentRequest wrappedReq,
			ServletContentResponse wrappedResp) throws ServletException,
			IOException {
		String strCardIds = (String) wrappedReq.getParameter(PARAM_NAME);
		if(strCardIds!=null && !strCardIds.isEmpty()){
			List<ObjectId> cardIds = ObjectIdUtils.commaDelimitedStringToNumericIds(strCardIds, Card.class);
			ParentReportAction parentReportAction = new ParentReportAction();
			parentReportAction.setChildResolutionCardIds(cardIds);
			List<Long> reportIds = null;
			try {
				
				reportIds = (List<Long>) doAction(parentReportAction);
				contentPrinter(createJsonText(reportIds));
			
			} catch (DataException e) {				
				e.printStackTrace();
			} catch (ServiceException e) {				
				e.printStackTrace();
			} catch (JSONException e) {				
				e.printStackTrace();
			}
		}
		logger.info(strCardIds);
	}
	
	
	private String createJsonText(List<Long> reportIds) throws JSONException{
		JSONObject mainObject = new JSONObject();
		JSONArray cardIds = new JSONArray();
		for (Long id : reportIds) {
			cardIds.put(id);
		}
		mainObject.put(PARAM_NAME, cardIds);
		
		return mainObject.toString();
	}
	


}
