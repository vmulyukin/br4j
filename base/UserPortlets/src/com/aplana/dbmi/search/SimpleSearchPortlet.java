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
package com.aplana.dbmi.search;

import com.aplana.cms.PagedList;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import org.springframework.validation.BindException;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.SimpleFormController;

import javax.portlet.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class SimpleSearchPortlet extends SimpleFormController {
	//private static final String PREFERENCES_SEARCH_VIEW_TYPE = "searchViewType";
	//private static final String PREFERENCES_EXTENDED_SEARCH_PATH = "extendedSearchPath";
	//private static final String PREFERENCES_SUBMIT_SEARCH_PATH = "submitSearchPath";
	private static final String PREFERENCES_REDIRECT = "redirect";
	public static final String SEARCH_PORTLET_ACTION = "simpleSearch";

	private final static ObjectId INCOMING_TEMPLATE_ID = ObjectId.predefined(
			Template.class, "jbr.incoming");
	private final static ObjectId OUTCOMING_TEMPLATE_ID = ObjectId.predefined(
			Template.class, "jbr.outcoming");

	private final static ObjectId TEMPLATE_ID_ORD = ObjectId.predefined(
			Template.class, "jbr.ord");
	private final static ObjectId TEMPLATE_ID_INTERNAL = ObjectId.predefined(
			Template.class, "jbr.interndoc");

	private final static ObjectId TEMPLATE_ID_OG = ObjectId.predefined(
			Template.class, "jbr.incomingpeople");
	private final static ObjectId TEMPLATE_ID_IZ = ObjectId.predefined(
			Template.class, "jbr.informationrequest");

	private final static ObjectId TEMPLATE_ID_OGOUT = ObjectId.predefined(
			Template.class, "jbr.infreq.answer");
	private final static ObjectId TEMPLATE_ID_IZOUT = ObjectId.predefined(
			Template.class, "jbr.informationrequest");

	private final static ObjectId INCOMING_DATEREG_ID = ObjectId.predefined(
			DateAttribute.class, "regdate"); // dateattribute.regdate=JBR_REGD_DATEREG
	private final static ObjectId OUTCOMING_DATESIGN_ID = ObjectId.predefined(
			DateAttribute.class, "jbr.outcoming.signdate"); // dateattribute.jbr.outcoming.signdate=JBR_INFD_DATESIGN

	private final static ObjectId DOCUMENT_TYPE_ID = new ObjectId(
			CardLinkAttribute.class, "JBR_INFD_TYPEDOC");
	private final static ObjectId DOCUMENT_SUMMARY_ID = new ObjectId(
			TextAttribute.class, "JBR_INFD_SHORTDESC");
	private final static ObjectId DOCUMENT_EXECUTOR_ID = new ObjectId(
			PersonAttribute.class, "JBR_OUT_RESPONSIBLE");

	// TODO: (2010/12/06, RuSA) ����� ������� � ����� ��������
	private final static ObjectId[] REQUESTED_ATTRIBUTES = new ObjectId[] {
			Card.ATTR_TEMPLATE, DOCUMENT_TYPE_ID, DOCUMENT_SUMMARY_ID,
			DOCUMENT_EXECUTOR_ID };

	public static final String CONFIG_FILE_PREFIX = "dbmi/";
	public static final String INIT_SEARCH_PARAM_KEY = "defaultSearch";
	public static final String APP_ATTR_SEARCH_BEAN = "SIMPLE_SEARCH_BEAN";	
	
	private Map<Template, ObjectId> templates = new HashMap<Template, ObjectId>(8);

	@Override
	protected ModelAndView handleRenderRequestInternal(RenderRequest request,
			RenderResponse response) throws Exception {
		ContextProvider.getContext().setLocale(request.getLocale());
		return super.handleRenderRequestInternal(request, response);
	}

	// TODO: (2010/12/06, RuSA) ���� ������� � ����� ������ �������� ����������
	/**
	 *  ������������ ������� ��� ������ ����������.
	 *  � ������� ���� id: 
	 *  [0] id ������� � [1] id �������� � ���� ������� � ��������� �����.
	 */
	private static final ObjectId[][] SUPPORTED_TEMPLATES = {
				//  id �������				id �������� � ����� ����
				{ INCOMING_TEMPLATE_ID,		INCOMING_DATEREG_ID},
				{ OUTCOMING_TEMPLATE_ID,	OUTCOMING_DATESIGN_ID},
				{ TEMPLATE_ID_ORD,			INCOMING_DATEREG_ID},

				{ TEMPLATE_ID_INTERNAL,		INCOMING_DATEREG_ID},
				{ TEMPLATE_ID_OG,			OUTCOMING_DATESIGN_ID},
				{ TEMPLATE_ID_IZ,			OUTCOMING_DATESIGN_ID},

				{ TEMPLATE_ID_OGOUT,		INCOMING_DATEREG_ID},
				{ TEMPLATE_ID_IZOUT,		INCOMING_DATEREG_ID}
	};

	// ��������� ���� ������� ��-���������: ���� ���������
	private static final ObjectId DEFAULT_DATE_ATTR = Attribute.ID_CHANGE_DATE;

	@Override
	protected Object formBackingObject(PortletRequest request) throws Exception {
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale());

		SimpleSearchBean searchBean = null;
		try {
			if (isSessionForm()
					&& request.getPortletSession().getAttribute(
							getFormSessionAttributeName()) != null) {
				searchBean = (SimpleSearchBean) request.getPortletSession()
						.getAttribute(getFormSessionAttributeName());
			} else {
				searchBean = new SimpleSearchBean();
				final DataServiceBean serviceBean = PortletUtil.createService(request);
				prepareTemplates(serviceBean);
				searchBean.setTemplates(templates.keySet());
			}
			final String formAttrName = getFormSessionAttributeName(request);
			request.getPortletSession().setAttribute(formAttrName, searchBean);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		// PortletPreferences pp = request.getPreferences();

		return searchBean;
	}

	private void prepareTemplates(DataServiceBean serviceBean) 
			throws Exception 
	{
		templates.clear();
		// templates.put( (Template) serviceBean.getById(INCOMING_TEMPLATE_ID), INCOMING_DATEREG_ID);
		// templates.put( (Template) serviceBean.getById(OUTCOMING_TEMPLATE_ID), OUTCOMING_DATESIGN_ID);
		for (final ObjectId[] info : SUPPORTED_TEMPLATES) {
			templates.put((Template) serviceBean.getById(info[0]), info[1]);
		}
	}

	@Override
	public void onSubmitAction(ActionRequest request, ActionResponse response,
			Object command, BindException errors) throws Exception {

		super.onSubmitAction(request, response, command, errors);
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale());

		final SimpleSearchBean searchBean = (SimpleSearchBean) command;
		try {
			if (isSessionForm()) {
				String formAttrName = getFormSessionAttributeName(request);
				request.getPortletSession().setAttribute(formAttrName, command);
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		final Search search = new Search();
		search.setByAttributes(true);
		search.setStates(Collections.EMPTY_LIST);
		search.setWords(searchBean.getSearchQuery());

		// �������� ������� ��������� ������ ...
		final Template templ = findSelectedTemplate(searchBean);
		// ���� ������ �� ����� ����� - ���������� ���� ��-���������
		ObjectId templateDateAttrId;
		if (templ != null) {
			search.setTemplates(Collections.singletonList(templ));
			templateDateAttrId = templates.get(templ);
		} else {
			search.setTemplates( new ArrayList<Template>(searchBean.getTemplates() ));
			templateDateAttrId = DEFAULT_DATE_ATTR;
			// ��������� ��� ���� �� ���� ��������...
			// for( final ObjectId attrId : getUniqueDateIds(SUPPORTED_TEMPLATES) )
			//	search.addDateAttribute( attrId, searchBean.getDateFrom(), searchBean.getDateTo());
		}

		// ���� ��������� � �������� ...
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		searchBean.setDateTo( safeGetDate(df, request.getParameter("to_date"), null));
		searchBean.setDateFrom( safeGetDate(df, request.getParameter("from_date"), null));
		if (templateDateAttrId != null && 
			(searchBean.getDateFrom() != null || searchBean.getDateTo() != null)
			) {
			search.addDateAttribute( templateDateAttrId, searchBean.getDateFrom(), searchBean.getDateTo());
		}

		// �.�. 13.01.2010
		final List<SearchResult.Column> columns = 
				new ArrayList<SearchResult.Column>(REQUESTED_ATTRIBUTES.length);
		search.setColumns( columns);
		for (int i = 0; i < REQUESTED_ATTRIBUTES.length; i++) {
			final SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(REQUESTED_ATTRIBUTES[i]);
			columns.add(col);
		}
		// �.�. 13.01.2010

		request.getPortletSession().setAttribute(APP_ATTR_SEARCH_BEAN, search, PortletSession.APPLICATION_SCOPE);
		// for new search reset selected page in ARM folder
		request.getPortletSession().removeAttribute(PagedList.ATTR_PAGE_CURRENT, PortletSession.APPLICATION_SCOPE);
		
        searchBean.setSearchQuery("");

		//BR4J00040083: ����� ��������. ��� ������������� ��� ������������.
		/*
		final String redirect = request.getPreferences().getValue(PREFERENCES_REDIRECT, null);
		if (redirect != null) {
			response.sendRedirect(redirect);
		}
		*/

	}

	/**
	 * ��������� ���������� id ��������� ��� (������ ������� � infoTemplates),
	 * ��������� � ��������� (������ ������� infoTemplates) 
	 * @param infoTemplates
	 * @return
	 */
	final Set<ObjectId> getUniqueDateIds(ObjectId[][] infoTemplates) {
		final Set<ObjectId> result = new HashSet<ObjectId>(2);
		for (int i = 0; i < infoTemplates.length; i++) {
			final ObjectId id = infoTemplates[i][1];
			result.add(id);
		}
		return result;
	}

	/**
	 * ����� ��������� ������ � ��������� ����.
	 * @param searchBean
	 * @return null, ���� ��� ������ (��� ������� "���")
	 */
	private Template findSelectedTemplate(SimpleSearchBean searchBean) {
		final String selectedTemplateName = (String) searchBean.getTemplId();
		if (selectedTemplateName != null) {
			for (Template tl : searchBean.getTemplates() ) {
				if (selectedTemplateName.equals(tl.getId().getId().toString()))
					return tl;
			} // for 
		}
		return null;
	}

	/**
	 * ��������� ������������� ������ � ����. ��� ������� ������������ ���������.
	 */
	public static Date safeGetDate( SimpleDateFormat df, String data, Date defaultDate) 
	{
		try {
			return df.parse( data);
		} catch (Exception e1) {
			return defaultDate;
		}
	}

}
