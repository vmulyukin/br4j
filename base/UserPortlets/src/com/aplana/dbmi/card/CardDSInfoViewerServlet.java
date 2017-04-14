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
package com.aplana.dbmi.card;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.crypto.CardDsInfoReader;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.UserPrincipal;
import com.aplana.dbmi.showlist.MIShowListPortlet;
import com.aplana.dbmi.showlist.MIShowListPortletSessionBean;

public class CardDSInfoViewerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static final String CERTIFICATES_INFO_ATTR = "CERTIFICATES_INFO";
	public static final String VISA_ATTR = "VISA_CERT_INFO";
	public static final String SIGNER_ATTR = "SIGNER_CERT_INFO";
	public static final String EXAMINER_ATTR = "EXAMINER_CERT_INFO";
	public static final String REPORT_ATTR = "REPORT_CERT_INFO";
	public static final String AUTHOR_ATTR = "AUTHOR_CERT_INFO";
	public static final String FILES_ATTR = "FILES_CERT_INFO";
	public static final String ERROR_MESSAGE_ATTR = "ERROR_MESSAGE";
	public static final String R_HEADER = "HEADER";
	public static final String R_VALUE = "VALUE";

	private static final String NAMESPACE_PARAM_KEY = "namespace";
	private static final String CARD_ID_PARAM_KEY = "cardId";

	private static final String VIEW = "/WEB-INF/jsp/cardDSInfoView.jsp";

	private Log logger = LogFactory.getLog(getClass());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String namespace = request.getParameter(NAMESPACE_PARAM_KEY);
		String cardId = request.getParameter(CARD_ID_PARAM_KEY);
		Card activeCard = null;
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request, namespace);
		DataServiceBean serviceBean;
		ResourceBundle bundle;
		MIShowListPortletSessionBean miSessionBean;
		if (sessionBean != null) {
			serviceBean = sessionBean.getServiceBean();
			bundle = sessionBean.getResourceBundle();
			if (cardId == null) {
				activeCard = sessionBean.getActiveCard();
			} else {
				try {
					activeCard = serviceBean.getById(new ObjectId(Card.class, Long.parseLong(cardId)));
				} catch (Exception e) {
					logger.error(e);
				}
			}
		} else {
			miSessionBean = MIShowListPortlet.getSessionBean(request, namespace);
			serviceBean = miSessionBean.getServiceBean(request, namespace);
			bundle = miSessionBean.getResourceBundle();
			try {
				activeCard = serviceBean.getById(new ObjectId(Card.class, Long.parseLong(cardId)));
			} catch (Exception e) {
				logger.error(e);
			}
		}
		DateFormat dateFormat = DateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM, request.getLocale());

		try {
			String sessionId = serviceBean.getSessionId();
			serviceBean = new AsyncDataServiceBean(sessionId);
			serviceBean.setAddress(request.getRemoteAddr());
			serviceBean.setUser(new UserPrincipal("__system__"));

			CardDsInfoReader infoReader = CardDsInfoReader.getInstance(serviceBean, activeCard, bundle);
			Map result = infoReader.getCardDsInfo();
			request.setAttribute(CERTIFICATES_INFO_ATTR, result);
			
			//new CardDSInfoReport().getData(Long.parseLong(cardId));


		} catch (DataException e) {
			logger.error(e);
			request.setAttribute(ERROR_MESSAGE_ATTR, bundle.getString("db.side.error.msg"));
		}

		request.getRequestDispatcher(VIEW).forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	private void fillCertificationInfo(Map result, String attributeName, CardLinkAttribute cardLinkAttribute, ObjectId headerAttribute,
			DataServiceBean serviceBean, ResourceBundle bundle, DateFormat dateFormat) throws DataException, ServiceException {
		if(null == cardLinkAttribute) {
			return;
		}

		Collection linkedIds = cardLinkAttribute.getIdsLinked();
		if(null == linkedIds || linkedIds.isEmpty()) {
			return;
		}

		List certificatesInfo = (List) result.get(attributeName);
		if(null == certificatesInfo) {
			certificatesInfo = new ArrayList();
			result.put(attributeName, certificatesInfo);
		}

		Iterator iterator = linkedIds.iterator();
		while(iterator.hasNext()) {
			ObjectId cardId = (ObjectId) iterator.next();
			Card card = (Card) serviceBean.getById(cardId);
			List<CertificateInfo> certInfo = CertificateInfo.readCertificateInfo(card, serviceBean, bundle, dateFormat);
			if(certInfo.size()>0){
				Map tableRecord = new HashMap();
				if(headerAttribute!=null){
					tableRecord.put(R_HEADER, card.getAttributeById(headerAttribute).getStringValue());
				}
				tableRecord.put(R_VALUE, certInfo);
				if(null != certInfo) {
					certificatesInfo.add(tableRecord);
				}
			}
		}
	}

}