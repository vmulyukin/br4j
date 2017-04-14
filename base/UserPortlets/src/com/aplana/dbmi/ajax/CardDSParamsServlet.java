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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.cms.ServletProcessResponse;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.MaterialDownloadServlet;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.util.DigitalSignatureUtil;

/**
 * Servlet implementation class cardDSParamsServlet
 */
public class CardDSParamsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private static final String NAMESPACE_PARAM_KEY = "namespace";
	private static final String CARD_ID_PARAM_KEY = "cardId";
	private static final String WORKFLOW_MOVE_APPLY_DS = "apply_ds";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CardDSParamsServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String postDataString = request.getReader().readLine();
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(postDataString);
			String namespace =jsonObject.getString(NAMESPACE_PARAM_KEY);
			String cardId =jsonObject.getString(CARD_ID_PARAM_KEY);
			Integer apply_ds = Integer.parseInt(jsonObject.getString(WORKFLOW_MOVE_APPLY_DS));
			CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request, namespace);
			DataServiceBean serviceBean = sessionBean.getServiceBean();
			Card activeCard = sessionBean.getActiveCard();
			if(apply_ds>2){
				apply_ds = apply_ds - 2;
			}
			ArrayList<String> signatureParams = DigitalSignatureUtil.prepareSignatureParams(serviceBean, activeCard, apply_ds==2, response.encodeURL(request.getContextPath()  + "/MaterialDownloadServlet?" + MaterialDownloadServlet.PARAM_CARD_ID + "="));
			JSONArray jsonArray = new JSONArray(signatureParams);
			response.setContentType("text/xml");
			response.getWriter().write(jsonArray.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			response.setStatus(500);
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
	}

}
