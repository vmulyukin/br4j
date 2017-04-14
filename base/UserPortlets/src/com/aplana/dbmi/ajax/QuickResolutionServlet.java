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
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.portlet.QuickResolutionPortlet;
import com.aplana.dbmi.portlet.QuickResolutionPortletSessionBean;

public class QuickResolutionServlet extends AbstractDBMIAjaxServlet {
	public final static String PARAM_NAMESPACE = "namespace";
	private static final long serialVersionUID = 1L;

	protected void generateResponse(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String namespace = request.getParameter(PARAM_NAMESPACE);
		QuickResolutionPortletSessionBean bean = QuickResolutionPortlet.getSessionBean(request, namespace);
		InputStream data;
		if (bean.getGrapFile() != null) {
			data = bean.getGrapFile().getInputStream();
		} else {
			Long cardId = bean.getGrapResId();
			if (cardId != null) {
				DownloadFile action = new DownloadFile();
				action.setCardId(new ObjectId(Card.class, cardId));
				try {
					Material material = (Material)getDataServiceBean(request).doAction(action);
					data = material.getData();
				} catch (Exception e) {
					data = getClass().getResourceAsStream("/blank.jpg");	
				}				
			} else {
				data = getClass().getResourceAsStream("/blank.jpg");
			}
		}
		response.setContentType("application/octet-stream");
		OutputStream out = response.getOutputStream();
		byte[] buffer = new byte[4 * 1024];
		while (true) {
			int read = data.read(buffer);
			if (read == -1)
				break;
			out.write(buffer, 0, read);
		}
		out.flush();
		out.close();
		data.close();
	}
}
