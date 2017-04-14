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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONWriter;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Reference;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataServiceBean;

public class TreeServlet extends AbstractDBMIAjaxServlet {
	static final long serialVersionUID = 1L;

	private void writeReferenceValueCollection(JSONWriter jw, Collection refValues, boolean topNodes, Collection<ObjectId> referenceValueIds) throws JSONException {
		Iterator iterRefVal = refValues.iterator();
		while(iterRefVal.hasNext()) {
			ReferenceValue refVal = (ReferenceValue)iterRefVal.next();
			if (!refVal.isActive()) continue; // Skip inactive values
			if (referenceValueIds!=null&&!referenceValueIds.isEmpty()&&!referenceValueIds.contains(refVal.getId())) continue; // Skip values wich not in referenceValueIds
			jw.object();
			jw.key("id").value(refVal.getId().getId().toString());
			jw.key("name").value(refVal.getValueRu());
			if (topNodes) 
				jw.key("type").value("topNodes");
			if (refVal.getChildren() != null) {
				jw.key("children");
				jw.array();
				Iterator iterCh = refVal.getChildren().iterator();
				while (iterCh.hasNext()) {
					jw.object();
					jw.key("_reference").value(((ReferenceValue)iterCh.next()).getId().getId().toString());
					jw.endObject();
				}
				jw.endArray();
			}
			jw.endObject();
			if (refVal.getChildren() != null) {
				writeReferenceValueCollection(jw, refVal.getChildren(), false, referenceValueIds);
			}
		}
	}

	protected void generateResponse(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		DataServiceBean dataService = getDataServiceBean(request);

		Object idRef = request.getParameter("idRef");
		if (idRef == null) {
			throw new ServletException("be missing request parameter \"idRef\"");
		}
		String filterRef = request.getParameter("filterRef");
		List<ObjectId> referenceValueIds = null;
		if (filterRef!=null&&!filterRef.isEmpty()){
			referenceValueIds = ObjectIdUtils.commaDelimitedStringToNumericIds(filterRef, ReferenceValue.class);
		}
		ObjectId objIdRef = new ObjectId(Reference.class, idRef);
		try {
			Collection refValues = dataService.listChildren(objIdRef, ReferenceValue.class);

			JSONWriter jw = new JSONWriter(response.getWriter());
			jw.object();
			jw.key("identifier").value("id");
			jw.key("label").value("name");
			jw.key("items");
			jw.array();
			writeReferenceValueCollection(jw, refValues, true, referenceValueIds);
			jw.endArray();
			jw.endObject();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}