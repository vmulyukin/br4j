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
package com.aplana.dbmi.portlet;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.validation.BindException;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.SimpleFormController;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.action.CreateCardByExample;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.util.CardAttrComparator;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.model.CardState;

public class CreateCardByExampleController extends SimpleFormController implements MessageSourceAware {
	public static final String ACTION_CREATE = "create";
	public static final String PARAM_ACTION = "submittedAction";
	public static final String PARAM_THIS_URL = "thisUrl";
	private static final String ATTR_EXAMPLE_TEMPLATE_ID = "CreateCardByExampleController.exampleTemplateId";
	private static final String ATTR_EXAMPLE_STATES_ID = "CreateCardByExampleController.exampleStateId";
	private static final String SHOW_ERROR = "show-error";
	private static final String TRUE = Boolean.TRUE.toString();

	private MessageSource messageSource;
	protected final Log logger = LogFactory.getLog(getClass());

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}    

	protected Object formBackingObject(PortletRequest request) throws Exception {
		ContextProvider.getContext().setLocale(request.getLocale());
		CreateCardByExampleCommandBean bean = (CreateCardByExampleCommandBean) super.formBackingObject(request);
		try {			
			DataServiceBean dataService = PortletUtil.createService(request);

			Search search = new Search();
			search.setByAttributes(true);
			List templates = new ArrayList(1);
			templates.add(DataObject.createFromId((ObjectId)request.getAttribute(ATTR_EXAMPLE_TEMPLATE_ID)));
			String idsState = (String)request.getAttribute(ATTR_EXAMPLE_STATES_ID);
			List<ObjectId> states = ObjectIdUtils.commaDelimitedStringToIds(idsState, CardState.class);
			search.setTemplates(templates);
			search.setStates(states);
			SearchResult.Column nameColumn = new SearchResult.Column();
			nameColumn.setAttributeId(Attribute.ID_NAME);
			nameColumn.setSorting(SearchResult.Column.SORT_ASCENDING);
			List columns = new ArrayList(1);
			columns.add(nameColumn);
			search.setColumns(columns);

			SearchResult searchResult = (SearchResult) dataService.doAction(search);

			List examples = new ArrayList(searchResult.getCards());
			Collections.sort(examples, new CardAttrComparator(Attribute.ID_NAME, false));
			bean.setExamples(examples);
		} catch (Exception e) {
			logger.error("Error forming backing object:", e);
			bean.setMessage(e.getMessage());
		}
		return bean;
	}

	protected void processFormSubmission(ActionRequest request,
			ActionResponse response, Object command, BindException errors)
			throws Exception {
		super.processFormSubmission(request, response, command, errors);
		CreateCardByExampleCommandBean bean = (CreateCardByExampleCommandBean)command;
		if (bean.getMessage() != null) {
			// ���� �������� ��������� �� ������, �� ������ �� ��� ���������� ��������
			// � ����� ������� ���������� ��������� ��� ���������� �����
			setFormSubmit(response);
		}
	}

	protected ModelAndView handleRenderRequestInternal(RenderRequest request, RenderResponse response) throws Exception {
		ObjectId exampleTemplateId = ObjectIdUtils.getObjectId(
			Template.class, 
			Portal.getFactory().getPortletService().getPageProperty("exampleTemplateId", request, response), 
			true
		);
		request.setAttribute(ATTR_EXAMPLE_TEMPLATE_ID, exampleTemplateId);
		String idsStates = Portal.getFactory().getPortletService().getPageProperty("exampleStateId", request, response);
		request.setAttribute(ATTR_EXAMPLE_STATES_ID, idsStates);
		if (isErrorAttr(request)) {
			cleanErrorAttr(request);
			return super.handleRenderRequestInternal(request, response);
		}
		//Retrieve actual data and show new form by default
		return showNewForm(request, response);
	}

	protected void onSubmitAction(ActionRequest request, ActionResponse response, Object command, BindException errors) {
		ContextProvider.getContext().setLocale(request.getLocale());
		CreateCardByExampleCommandBean bean = (CreateCardByExampleCommandBean)command;
		String action = request.getParameter(PARAM_ACTION);
		bean.setMessage(null);
		if (ACTION_CREATE.equals(action)) {
			ObjectId exampleId = bean.getSelectedExampleCardId();
			if (exampleId == null) {
				bean.setMessage(messageSource.getMessage("message.exampleIsNotSelected", null, request.getLocale()));
				setErrorAttr(request);
				return;
			}
			CreateCardByExample createAction = new CreateCardByExample();        	
			createAction.setExampleId(exampleId);
			DataServiceBean dataService = PortletUtil.createService(request);
			try {
				ObjectId cardId = (ObjectId)dataService.doAction(createAction);
				PortletService ps = Portal.getFactory().getPortletService();
				HashMap urlParams = new HashMap();
	        	urlParams.put( CardPortlet.EDIT_CARD_ID_FIELD, cardId.getId());
				urlParams.put(CardPortlet.BACK_URL_FIELD, URLEncoder.encode(request.getParameter(PARAM_THIS_URL), "UTF-8"));
				String cardLink = ps.generateLink("dbmi.Card", "dbmi.Card.w.Card", urlParams, request, response);
				response.sendRedirect(cardLink);
			} catch (Exception e) {
				bean.setMessage(e.getMessage());
			}
		}
	}

	protected ModelAndView onSubmitRender(RenderRequest request,
			RenderResponse response, Object command, BindException errors) throws Exception {
		CreateCardByExampleCommandBean bean = (CreateCardByExampleCommandBean)command;
		if (bean.getMessage() != null) {
			return showForm(request, response, errors);
		} else {
			return super.onSubmitRender(request, response, command, errors);
		}
	}

	private void cleanErrorAttr(PortletRequest request) {
		request.getPortletSession().setAttribute(SHOW_ERROR, null);
	}

	private void setErrorAttr(PortletRequest request) {
		request.getPortletSession().setAttribute(SHOW_ERROR, TRUE);
	}

	private boolean isErrorAttr(PortletRequest request) {
		String errorAttr = (String) request.getPortletSession().getAttribute(SHOW_ERROR);
		return TRUE.equals(errorAttr);
	}
}
