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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONWriter;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.service.DataServiceBean;

public abstract class DataGridAbstractServlet extends AbstractDBMIAjaxServlet {
	@Override
	protected void generateResponse(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			DataServiceBean serviceBean = getDataServiceBean(request);

			List<Map<String, String>> rows = (List<Map<String,String>>) serviceBean.doAction(getAction(request));
			
			JSONWriter writer = new JSONWriter(response.getWriter());
			writer.array();
			for(Map<String, String> row : rows){
				writer.object();
				for(Entry<String, String> cell: row.entrySet()){
					writer.key(cell.getKey()).value(cell.getValue());
				}
				writer.endObject();
			}
			writer.endArray();
		} catch (Exception e) {
			logger.error("Exception caught while loading card DataGrid", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	protected abstract Action getAction(HttpServletRequest request);

}