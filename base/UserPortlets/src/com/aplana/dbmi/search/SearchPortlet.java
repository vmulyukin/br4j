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

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.action.GetPersonSearchByNameAndArea;
import com.aplana.dbmi.action.ListStoredSearchsByArea;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.Search.DatePeriod;
import com.aplana.dbmi.action.Search.Interval;
import com.aplana.dbmi.action.StrictSearch;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.filter.TemplateForSearchFilter;
import com.aplana.dbmi.model.web.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.showlist.MIShowListPortlet;
import com.aplana.dbmi.util.DateUtils;
import com.aplana.dbmi.util.TemplateComparator;
import com.aplana.web.tag.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.SimpleFormController;

import javax.portlet.*;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class SearchPortlet extends SimpleFormController {
	private static Log logger = LogFactory.getLog(SearchPortlet.class);

	private static final String PREFERENCES_SEARCH_VIEW_TYPE = "searchViewType";
	private static final String PREFERENCES_EXTENDED_SEARCH_PATH = "extendedSearchPath";

	public static final String EXTENDED_SEARCH_FORM = "extendedSearchForm";
	public static final String EXTENDED_SEARCH_LINK_HIDE = "extendedSearchLinkHide";

	public static final String RESOLUTION_SEARCH_FORM = "independentResolutionSearchForm";

	public static final String DEFAULT_SEARCH_FORM = "defaultSearchForm";

	private static final String PREFERENCES_SUBMIT_SEARCH_PATH = "submitSearchPath";

	public static final String RESOURCE_BUNDLE_NAME = "search";

	public static final String CONFIG_FILE_PREFIX = "dbmi/";

	public static final String INIT_SEARCH_PARAM_KEY = "defaultSearch";

	public static final String SEARCH_PORTLET_ACTION = "search";

	public static final String FILE_NAME_PROPERTY = "FILE_NAME";

	private static final String SEARCH_ATTRIBUTE_NAME = "initSearch";
	
	// Personal search
	public static final String SEARCH_NAME = "SEARCH_NAME";
	public static final String SEARCH_ID = "SEARCH_ID";
	public static final String PERSONAL_SEARCH_ID = "personalSearchId";
	public static final String MSG_PARAM = "MSG_PARAM";

	private static final String CAN_USE_WHOLE_BASE = "canUseWholeBase";
	private static final String VISIBLE_CURRENT_YEAR = "visibleCurrentYear";
	private static final String SEARCH_BY_REGNUM_ATTRS = "searchByRegNum";

	private static final String CHECKED_CURRENT_YEAR = "checkedCurrentYear";
	private static final String PREFERENCES_ALLOW_STATES = "allowedStates";

	private static final ObjectId ordTemplateId = ObjectId.predefined(Template.class, "jbr.ord");
	private static final ObjectId npaTemplateId = ObjectId.predefined(Template.class, "jbr.npa");
	private static final ObjectId projectNumber = ObjectId.predefined(IntegerAttribute.class, "jbr.projectNumber");
	private static final ObjectId registerNumber = ObjectId.predefined(StringAttribute.class, "regnumber");
	private static final ObjectId registerNumberOut = ObjectId.predefined(StringAttribute.class, "jbr.incoming.outnumber");
	private static final ObjectId fileTemplateId = ObjectId.predefined(Template.class, "jbr.file");
	private static final String ACTION_REQUEST = "ACTION_REQUEST";
	private static final String TRUE = Boolean.TRUE.toString();

	protected ModelAndView handleRenderRequestInternal(RenderRequest request,
			RenderResponse response) throws Exception {
		ContextProvider.getContext().setLocale(request.getLocale());
		WebSearchBean searchBean = (WebSearchBean)request.getPortletSession().getAttribute(getFormSessionAttributeName());
		boolean isSearchSessionForm = isSessionForm() && null != searchBean;
		if (!isSearchSessionForm || (isSearchSessionForm && 
				searchBean.getIsExtendedSearch() && !isActionRequest(request))
				|| isOpenExtSearchAction(request) || isCloseExtSearchAction(request)) {
			Search search = createSearch(request, response);
			request.setAttribute(SEARCH_ATTRIBUTE_NAME, search);
		}

		return super.handleRenderRequestInternal(request, response);
	}
	
	@Override
	protected WebSearchBean formBackingObject(PortletRequest request) throws Exception {
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale());

		WebSearchBean searchBean = (WebSearchBean) request.getPortletSession()
				.getAttribute(getFormSessionAttributeName());

		try {
			if (null != searchBean && isSessionForm()) {
				String searchText = searchBean.getSearchText();
				if (searchBean.getIsExtendedSearch()) {
					if (!isActionRequest(request)) {
						searchBean = initDefaultBean(request);
						searchBean.setSearchText(searchText);
					} else {
						if (isOpenExtSearchAction(request)) {
							searchBean = initDefaultBean(request);
							searchBean.setIsExtendedSearch(true);
							searchBean.setSearchText(searchText);
						} else if (request.getParameter(PERSONAL_SEARCH_ID) != null) {
							searchBean = personalSearchActionHandler(request);
						}
						loadPersonalSearches(request, searchBean);
						collapseBlockHandler(request, searchBean);
					}
				} else if (isCloseExtSearchAction(request)){
					searchBean = initDefaultBean(request);
					searchBean.setSearchText(searchText);
				}
			} else {
				searchBean = initDefaultBean(request);
			}

			clearRequest(searchBean, request);
		} catch (Exception e) {
			logger.error("Error forming backing object", e);
			if (searchBean != null) {
				searchBean.setMessage(e.getMessage());
			}
		}

		PortletPreferences pp = request.getPreferences();
		searchBean.setSearchViewType(pp.getValue(PREFERENCES_SEARCH_VIEW_TYPE, null));

		searchBean.setExtendedSearchPath(pp.getValue(PREFERENCES_EXTENDED_SEARCH_PATH, null));
		searchBean.setSubmitSearchPath(pp.getValue(PREFERENCES_SUBMIT_SEARCH_PATH, null));
		final PortletService psrvc = Portal.getFactory().getPortletService();

		String extendedSearchFormName = psrvc.getPageProperty(
				EXTENDED_SEARCH_FORM, request, null);// TODO do we need to pass
														// response?
		String resolutionSearchFormName = psrvc.getPageProperty(
				RESOLUTION_SEARCH_FORM, request, null);// TODO do we need to
														// pass response?
		Collection<ObjectId> allowedStates = parseAllowedStates(psrvc.getPageProperty(
				PREFERENCES_ALLOW_STATES, request, null));
		searchBean.setExtendedSearchForm(extendedSearchFormName);
		searchBean.setResolutionSearchForm(resolutionSearchFormName);
		searchBean.setStates(allowedStates);

		return searchBean;
	}
	
	private void clearRequest(Object command, PortletRequest request) {
		String formAttrName = getFormSessionAttributeName(request);
		request.getPortletSession().setAttribute(formAttrName, command);
		clearIsActionRequest(request);
		clearOpenExtSearchAction(request);
		clearCloseExtSearchAction(request);
	}

	private boolean parseBooleanParameter(PortletService psrvc,
			PortletRequest request, String paramName) {
		String paramValue = psrvc.getPageProperty(paramName, request, null);
		boolean booleanValue = Boolean.parseBoolean(paramValue);
		return booleanValue;
	}
	
	private int parseIntegerParameter(PortletService psrvc,
			PortletRequest request, String paramName) {
		String paramValue = psrvc.getPageProperty(paramName, request, null);
		if (paramValue == null) {
			return 0;
		}
		int intValue = Integer.parseInt(paramValue);
		return intValue;
	}

	private Search createSearch(PortletRequest request, PortletResponse response)
			throws DataException, IOException {
		String initSearchFile = Portal.getFactory().getPortletService()
				.getPageProperty(INIT_SEARCH_PARAM_KEY, request, response);
		logger.debug("SearchPortlet.createSearch: initSearchFile="+initSearchFile);
		Search search = new Search();
		if (initSearchFile != null) {
			// InputStream inputStream = loadDefaultSearch(initSearchFile);
			InputStream inputStream = Portal.getFactory().getConfigService()
					.loadConfigFile(CONFIG_FILE_PREFIX + initSearchFile);
			search.initFromXml(inputStream);
			inputStream.close();
		}
		return search;
	}

	private WebSearchBean personalSearchActionHandler(PortletRequest request)
			throws Exception {
		WebSearchBean searchBean = initBean(request);
		DataServiceBean dataServiceBean = PortletUtil.createService(request);

		String personalSearchId = request.getParameter(PERSONAL_SEARCH_ID);
		PersonalSearch personalSearch = dataServiceBean
				.getById(new ObjectId(PersonalSearch.class, personalSearchId));

		initializeFromSearch(personalSearch.getSearch(), searchBean);

		searchBean.setName(personalSearch.getName());
		searchBean.setDescription(personalSearch.getDescription());
		searchBean.setId(Long.valueOf(personalSearchId));
		searchBean.setIsPersonalSearch(Boolean.TRUE);
		searchBean.setIsExtendedSearch(Boolean.TRUE);

		searchBean.setPersonalSearch(personalSearch);	
		return searchBean;

	}

	private void collapseBlockHandler(PortletRequest request,
			WebSearchBean searchBean) {
		if ("TEMPLATES".equals(request.getParameter("block_id"))) {
			searchBean.setShowTemplates(!searchBean.getShowTemplates());
		} else if ("MAIN_COMMON".equals(request.getParameter("block_id"))) {
			searchBean.getViewMainBlock().setShow(!searchBean.getViewMainBlock().getShow());
		} else if (request.getParameter("block_id") != null
				&& !"".equals(request.getParameter("block_id"))) {

			WebBlock block = findBlock(searchBean, new ObjectId(
					TemplateBlock.class, request.getParameter("block_id")));
			block.setShow(!block.getShow());
		}
	}

	private WebSearchBean initBean(PortletRequest request)
			throws Exception {

		WebSearchBean searchBean = (WebSearchBean) super.formBackingObject(request);

		DataServiceBean dataServiceBean = PortletUtil.createService(request);
		List<Template> templates = (List<Template>)dataServiceBean.filter(Template.class,
				new TemplateForSearchFilter());
		Collections.sort(templates, new TemplateComparator());
		searchBean.setDbTemplates(templates);

		List<CheckboxControl> controlTemplates = new ArrayList<CheckboxControl>();

		for (Template template : templates) {
			CheckboxControl control = new CheckboxControl(template.getId()
					.getId().toString(), template.getName(), template.getId()
					.getId().toString());
			control.setLabelEn(template.getNameEn());
			control.setLabelRu(template.getNameRu());
			controlTemplates.add(control);
		}
		searchBean.setViewTemplates(controlTemplates);
		List<AbstractControl> mainAttributes = new ArrayList<AbstractControl>();
		AttributeBlock attributeBlock = dataServiceBean.getById(AttributeBlock.ID_COMMON);
		searchBean.setDbAttributes(attributeBlock.getAttributes());
		searchBean.getAttributes().clear();
		for (Attribute attribute : attributeBlock.getAttributes()) {
			AbstractControl control = ControlUtils.initializeControl(attribute, dataServiceBean);
			if (control != null) {
				mainAttributes.add(control);
			}
			if (attribute instanceof StringAttribute) { /* || attribute instanceof TextAttribute) { always true */
				// searchBean.getAttributes().put(attribute.getId().getId(),
				// attribute.getId().getId());
				searchBean.getAttributes().put(attribute.getId().getId(), "");
			}
		}

		WebBlock mainBlock = new WebBlock();
		mainBlock.setName(attributeBlock.getName());
		mainBlock.setNameEn(attributeBlock.getNameEn());
		mainBlock.setNameRu(attributeBlock.getNameRu());
		mainBlock.setId(attributeBlock.getId());
		mainBlock.setAttributes(mainAttributes);
		searchBean.setViewMainBlock(mainBlock);
		return searchBean;
	}

	private WebSearchBean initDefaultBean(PortletRequest request) throws Exception {
		final PortletService psrvc = Portal.getFactory().getPortletService();
		WebSearchBean searchBean = initBean(request);
		Search search = (Search) request.getAttribute(SEARCH_ATTRIBUTE_NAME);
		if (search == null) {
			search = createSearch(request, null);
		}
		initializeFromSearch(search, searchBean);
		String canUseWholeBase = psrvc.getPageProperty(CAN_USE_WHOLE_BASE, request, null);
	
		boolean isCanUseWholeBase = Boolean.parseBoolean(canUseWholeBase);
	
		if (isCanUseWholeBase) {
			searchBean.setCanUseWholeBase(true);
		}
		searchBean.setCurrentTabSearch(search);
		searchBean.setVisibleCurrentYear(parseBooleanParameter(psrvc,
				request, VISIBLE_CURRENT_YEAR));
		searchBean.setSearchByRegnum(parseIntegerParameter(psrvc,
				request, SEARCH_BY_REGNUM_ATTRS));
		searchBean.setSearchCurrentYear(parseBooleanParameter(psrvc,
				request, CHECKED_CURRENT_YEAR));
		
		searchBean.setHideExtendedLink(parseBooleanParameter(psrvc,
				request, EXTENDED_SEARCH_LINK_HIDE));
		return searchBean;
	}

	private void addCreatedCurrentAttribute(Search search) {
		ObjectId objectId = ObjectId.predefined(DateAttribute.class, "created");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		int year = calendar.get(Calendar.YEAR);
		search.addDateAttribute(objectId, DateUtils.beginOfYear(year), DateUtils.endOfYear(year));
	}

	private void searchActionHandler(ActionRequest request,
			ActionResponse response, WebSearchBean searchBean)
			throws DataException, ServiceException,
			ParseException, IOException {
		DataServiceBean dataServiceBean = PortletUtil.createService(request);
		searchBean.setValuesDate(new HashMap());

		final Search search;
		final String searchText = removeSpace(searchBean.getSearchText());
		searchBean.setSearchText(searchText); // ������ ���, ����� ���������� �� ������ ��������

		//���� �� ������� ������� ����� ������ �� ���� ����,
		//�� ��� ������� �� �������� (�� ��������� ������� � checkbox),
		//�� ���� ������ �� ������� ������� (��������� ������� search)
		if (searchBean.getCanUseWholeBase() && !searchBean.getWholeBase()) {
			if (searchBean.getCurrentTabSearch() != null) {
				search = searchBean.getCurrentTabSearch().makeCopy();
			} else {
				search = createSearch(request, response);
			}
			if (searchBean.getRegisternumber() != null && searchBean.getRegisternumber()) {
				String searchTextForRegnum = StringUtils.trimBoth(searchText, '*', '*');

				search.addAttribute(registerNumber, true);
				if (searchBean.getSearchByRegnum() == 2) {
					search.setWords(Search.SearchTag.TAG_SEARCH_TWO_REGNUM + searchTextForRegnum);
					search.addAttribute(registerNumberOut, true);
				} else if (searchBean.getSearchByRegnum() == 1) {
					search.setWords(Search.SearchTag.TAG_SEARCH_REGNUM + searchTextForRegnum);
				}
			} else if (searchBean.getById() != null && searchBean.getById()) {
				search.setWords(Search.SearchTag.TAG_SEARCH_ID + searchText);
			} else {
				search.setWords(Search.SearchTag.TAG_SEARCH_FULL_TEXT + searchText);
				search.setStrictWords(searchBean.getSearchStrictWords());
			}
			if (searchBean.getSearchCurrentYear()) {
				addCreatedCurrentAttribute(search);
			}
			request.getPortletSession().setAttribute(MIShowListPortlet.SEARCH_BEAN, search,
					PortletSession.APPLICATION_SCOPE);
			return;
		}

		//� ��� ���� ���� �� ���� ����
		search = new Search();
		if (searchBean.getSearchCurrentYear()) {
			addCreatedCurrentAttribute(search);
		}

		if (searchBean.getRegisternumber() != null && searchBean.getRegisternumber()) {
			String searchTextForRegnum = StringUtils.trimBoth(searchText, '*', '*');
			
			search.addAttribute(registerNumber, true);
			if (searchBean.getSearchByRegnum() == 2) {
				search.setWords(Search.SearchTag.TAG_SEARCH_TWO_REGNUM + searchTextForRegnum);
				search.addAttribute(registerNumberOut, true);
			} else if (searchBean.getSearchByRegnum() == 1) {
				search.setWords(Search.SearchTag.TAG_SEARCH_REGNUM + searchTextForRegnum);
			}
		} else if (searchBean.getNumber()) {
			StrictSearch strictSearch = new StrictSearch();
			int integ = Integer.parseInt(searchBean.getSearchText());
			strictSearch.addIntegerAttribute(projectNumber, integ, integ);

			List<ObjectId> col = dataServiceBean.doAction(strictSearch);

			StringBuilder words = new StringBuilder();
			Iterator<ObjectId> i = col.iterator();
			while (i.hasNext()) {
				words.append(i.next()).append(i.hasNext() ? ", " : "");
			}
			search.setWords(words.toString());
			ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, request.getLocale());
			search.setNameEn(bundle.getString("searchProjectNumber") + searchBean.getSearchText());
			search.setNameRu(bundle.getString("searchProjectNumber") + searchBean.getSearchText());
			search.setByCode(true);
		} else if (searchBean.getById() != null && searchBean.getById()) {
			search.setWords(searchBean.getSearchText());
			ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, request.getLocale());
			search.setNameEn(bundle.getString("searchCardId") + searchBean.getSearchText());
			search.setNameRu(bundle.getString("searchCardId") + searchBean.getSearchText());
			search.setByCode(true);
		} else {
			// ���� ����� �� �� ��� ������,
			// �� ���
			search.setByCode(searchBean.getNumber());
			search.setByAttributes(searchBean.getProperty());
			search.setByMaterial(searchBean.getFullText());
			search.setSqlXmlName(searchBean.getCurrentTabSearch().getSqlXmlName());

			search.setWords(Search.SearchTag.TAG_SEARCH_FULL_TEXT + searchText);
		}
		search.setMaterialTypes(searchBean.getMaterialTypes());
		// ����� �� ���������: id ��������, ������� ��������
		if (!search.isByAttributes() && !search.isByCode() && !search.isByMaterial()) {
			search.setByAttributes(true);
		}
		search.setStates(searchBean.getStates());
		search.setTemplates(new ArrayList<Template>());

		if (!searchBean.getIsExtendedSearch() && !searchBean.getNumber()) {
			// ���� �� ����������� ����� � �� "�� ������"
			// -> �������� ������ ������� �� ����������� xml-��������...
			search.setColumns(searchBean.getColumns());
		}

		for (final Template template : (Collection<Template>) searchBean.getDbTemplates()) {
			final String sId = template.getId().getId().toString();
			if (searchBean.getTemplates().containsKey(sId)
					&& searchBean.getTemplates().get(sId) != null
					&& !"".equals(searchBean.getTemplates().get(sId))) {
				search.getTemplates().add(template);
			}
		}

		List dbAttributes = new ArrayList();
		dbAttributes.addAll(searchBean.getDbAttributes());
		Map webAttMap = new HashMap();
		webAttMap.putAll(searchBean.getAttributes());
		for (Object dbAttribute : dbAttributes) {
			Attribute attribute = (Attribute) dbAttribute;
			String attrIdStr = attribute.getId().getId().toString();
			// �������� DateAttribute �������� �� ���������� request-�,
			// ������� ���� ���������� �������� ��� ��� �� ������
			if (!(attribute instanceof DateAttribute)) {
				if ((!webAttMap.containsKey(attrIdStr)
						|| webAttMap.get(attrIdStr) == null
						|| "".equals(webAttMap.get(attrIdStr)) || "-1"
						.equals(webAttMap.get(attrIdStr)))
						&& !(attribute instanceof TreeAttribute)
						&& !(attribute instanceof ListAttribute)
						&& !(attribute instanceof IntegerAttribute)) {
					continue;
				}
			}

			if (attribute instanceof TextAttribute) {
				search.addStringAttribute(attribute.getId());
			} else if (attribute instanceof StringAttribute) {
				search.addStringAttribute(attribute.getId());
			} else if (attribute instanceof IntegerAttribute) {
				String tmpIntFrom = (String) webAttMap.get(attrIdStr + "_from");
				String tmpIntTo = (String) webAttMap.get(attrIdStr + "_to");

				Integer intFromO;
				int intFrom;
				if (tmpIntFrom != null && tmpIntFrom.length() > 0) {
					intFromO = Integer.valueOf(tmpIntFrom);
					intFrom = intFromO;
				} else {
					intFromO = null;
					intFrom = Integer.MIN_VALUE;
				}

				Integer intToO;
				int intTo;
				if (tmpIntTo != null && tmpIntTo.length() > 0) {
					intToO = Integer.valueOf(tmpIntTo);
					intTo = intToO;
				} else {
					intToO = null;
					intTo = Integer.MAX_VALUE;
				}

				if (intFromO != null || intToO != null) {
					search.addIntegerAttribute(attribute.getId(), intFrom,
							intTo);
				}
			} else if (attribute instanceof DateAttribute) {
				String tmpDateFrom;
				String tmpDateTo;

				if (!searchBean.getIsExtendedSearch()) {
					tmpDateFrom = request.getParameter(attrIdStr);
					tmpDateTo = request.getParameter(attrIdStr + "_new");
				} else {
					tmpDateFrom = (String) webAttMap.get(attrIdStr);
					tmpDateTo = (String) webAttMap.get(attrIdStr + "_new");
				}
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date dateFrom;

				if (tmpDateFrom != null && !"".equals(tmpDateFrom)) {
					searchBean.addValueDate(attrIdStr, tmpDateFrom);
					dateFrom = dateFormat.parse(tmpDateFrom);
				} else {
					dateFrom = null;
				}
				Date dateTo;
				if (tmpDateTo != null && !"".equals(tmpDateTo)) {
					searchBean.addValueDate(attrIdStr + "_new", tmpDateTo);
					dateTo = dateFormat.parse(tmpDateTo);
					dateTo = setDateInclusive(dateTo);

				} else {
					dateTo = null;
				}
				search.addDateAttribute(attribute.getId(), dateFrom, dateTo);
			} else if (attribute instanceof TreeAttribute) {
				TreeAttribute treeAttribute = (TreeAttribute) attribute;
				Collection rootValues = dataServiceBean.listChildren(
						treeAttribute.getReference(), ReferenceValue.class);
				List referenceValues = new ArrayList();
				ControlUtils.getTreeReferenceValues(referenceValues,
						rootValues, webAttMap);
				if (referenceValues.size() > 0) {
					search.addListAttribute(treeAttribute.getId(),
							referenceValues);
				}
			} else if (attribute instanceof ListAttribute) {
				ListAttribute listAttribute = (ListAttribute) attribute;
				Collection rootValues = dataServiceBean.listChildren(
						listAttribute.getReference(), ReferenceValue.class);
				List referenceValues = new ArrayList();
				ControlUtils.getListReferenceValues(referenceValues, rootValues,
						webAttMap);
				if (referenceValues.size() > 0) {
					search.addListAttribute(listAttribute.getId(),
							referenceValues);

				}
			}
		}
		search.setStrictWords(searchBean.getSearchStrictWords());
		if (search.isByAttributes() && search.getTemplates().isEmpty()) {
			// TODO ����� ���������� ���������� �������� ������������ ������ � ������.
			// �������� ��������� �������� ���� � DoSearch ��� �������� ��������, �.�. ��-�� 
			// ���������� �������� ������������ ������ ��������� ������ ����� ���� �� ������.
			// ��������� ��� ��������� �������� �������� � ������ ������ � ������,
			// � ������ ��� ������� �������� �������������, ������� ����� ���� ������ 
			// ����� ����� ����, ������� ������������.
			search.getFilter().getTemplatesWithoutPermCheck().add(fileTemplateId);
		}
		request.getPortletSession().setAttribute(MIShowListPortlet.SEARCH_BEAN, search,
				PortletSession.APPLICATION_SCOPE);
		if (searchBean.getSubmitSearchPath() != null) {
			response.sendRedirect(searchBean.getSubmitSearchPath());
		}
	}

	private String removeSpace(String value) {
		if (value != null && !value.isEmpty()) {
			return value.trim();
		}
		return value;
	}

	private void attrSearchActionHandler(PortletRequest request,
			WebSearchBean searchBean) throws DataException, ServiceException,
			RemoteException, ParseException {
		DataServiceBean dataServiceBean = PortletUtil.createService(request);

		Map searchTemplates = searchBean.getTemplates();
		String templateId = null;
		for (Object o : searchTemplates.entrySet()) {
			Entry entry = (Entry) o;
			String value = entry.getValue() == null ? null : entry.getValue()
					.toString();
			if (value != null && !"".equals(value)) {
				templateId = value;
				break;
			}
		}
		Collection blocks;
		if (templateId != null) {
			blocks = dataServiceBean.listChildren(new ObjectId(Template.class,
					templateId), TemplateBlock.class);
		} else {
			blocks = new ArrayList();
		}
		List webBlocks1 = new ArrayList();
		List webBlocks2 = new ArrayList();
		List dbAttributes = new ArrayList();
		for (Object block : blocks) {
			TemplateBlock templateBlock = (TemplateBlock) block;
			/*
			 * if(AttributeBlock.ID_COMMON.equals(templateBlock.getId())){
			 * continue; }
			 */
			WebBlock webBlock = new WebBlock();
			webBlock.setName(templateBlock.getName());
			webBlock.setNameEn(templateBlock.getNameEn());
			webBlock.setNameRu(templateBlock.getNameRu());
			webBlock.setId(templateBlock.getId());
			List webAttributes = new ArrayList();
			for (Attribute attribute : templateBlock.getAttributes()) {
				ObjectId attributeId = attribute.getId();
				dbAttributes.add(attribute);
				AbstractControl abstractControl = ControlUtils
						.initializeControl(attribute, dataServiceBean);
				if (abstractControl != null) {
					webAttributes.add(abstractControl);
				}
				if (attribute instanceof StringAttribute) {
						/* || attribute instanceof TextAttribute) { always true */
					if (searchBean.getIsPersonalSearch()) {
						PersonalSearch personalSearch = searchBean
								.getPersonalSearch();
						Search search = personalSearch.getSearch();
						boolean attributeValue = search
								.hasAttribute(attributeId)
								&& ((Boolean) search.getAttribute(attributeId));
						searchBean.getAttributes().put(
								attributeId.getId().toString(),
								attributeValue ? attributeId.getId().toString()
										: "");
					} else {
						searchBean.getAttributes().put(
								attribute.getId().getId(),
								attribute.getId().getId());
					}
				}
			}
			webBlock.setAttributes(webAttributes);
			webBlocks1.add(webBlock);
			// TODO ���-�� �������� ����������� Ace
			/*
			 * if (templateBlock.getColumn() == 0) { webBlocks1.add(webBlock); }
			 * else { webBlocks2.add(webBlock); }
			 */
		}
		searchBean.setViewBlocks1(webBlocks1);
		searchBean.setViewBlocks2(webBlocks2);
		searchBean.setDbAttributes(dbAttributes);

	}

	public void onSubmitAction(ActionRequest request, ActionResponse response,
			Object command, BindException errors) throws Exception {
		super.onSubmitAction(request, response, command, errors);
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale());

		WebSearchBean searchBean = (WebSearchBean) command;
		setIsActionRequest(request);

		try {
			if (WebSearchBean.SEARCH_ACTION.equals(searchBean.getAction())) {
				searchActionHandler(request, response, searchBean);
			} else if (WebSearchBean.ATTRIBUTE_SEARCH_ACTION.equals(searchBean
					.getAction())) {
				attrSearchActionHandler(request, searchBean);
			} else if (WebSearchBean.SAVE_PERSONAL_SEARCH_ACTION.equals(searchBean
					.getAction())){
				savePersonalSearchActionHandler(request, searchBean);
			} else if (WebSearchBean.DELETE_PERSONAL_SEARCH_ACTION.equals(searchBean
					.getAction())){
				deletePersonalSearchActionHandler(request, searchBean);
			} else if (WebSearchBean.LOAD_PERSONAL_SEARCH_ACTION.equals(searchBean
					.getAction())){
				response.setRenderParameter(PERSONAL_SEARCH_ID, request.getParameter(SEARCH_ID));
			}else if (WebSearchBean.CLOSE_EXTENDED_SEARCH_ACTION.equals(searchBean.getAction()) 
						|| WebSearchBean.OPEN_EXTENDED_SEARCH_ACTION.equals(searchBean.getAction())){
				setExtSearchAction(request, searchBean.getAction());
			} else {
				moveToRenderParameters(request, response);
			}
			// ���� ��� ����� "�����" � � �������� ������ ���� ��� ��������� ��
			// ������� �� ������ �����
			// �� �� �� ����� ������������� Render-���������, ��� ��� ���
			// �������� ������� ��������
			if (!(WebSearchBean.SEARCH_ACTION.equals(searchBean.getAction()) && searchBean
					.getSubmitSearchPath() != null)) {
				response.setRenderParameter("portlet_action",
						SEARCH_PORTLET_ACTION);
			}
			searchBean.setAction("");
			if (isSessionForm()) {
				String formAttrName = getFormSessionAttributeName(request);
				request.getPortletSession().setAttribute(formAttrName, command);
			}
		} catch (Exception e) {
			logger.error("Error on submit action", e);
			searchBean.setMessage(e.getMessage());
		}
	}

	public void moveToRenderParameters(ActionRequest request,
			ActionResponse response) {

		Set parameterEntries = request.getParameterMap().entrySet();
		for (Object parameterEntry : parameterEntries) {
			Entry entry = (Entry) parameterEntry;
			if (entry.getKey() != null
					&& entry.getKey().toString().indexOf("my") >= 0
					&& entry.getValue() != null
					&& ((String[]) entry.getValue()).length > 0
					&& !"".equals(((String[]) entry.getValue())[0])

					) {
				response.setRenderParameter(entry.getKey().toString()
						.substring(2), ((String[]) entry.getValue())[0]);
			}
		}
	}

	private void initializeFromSearch(Search search, WebSearchBean webSearchBean) {
		webSearchBean.setSearchText(search.getWords());
		webSearchBean.setFullText(search.isByMaterial());
		webSearchBean.setNumber(search.isByCode());
		webSearchBean.setProperty(search.isByAttributes());
		webSearchBean.setById(search.isByCode());
		webSearchBean.setIsAllTemplates(Boolean.TRUE);
		webSearchBean.setMaterialTypes(search.getMaterialTypes());
		webSearchBean.setColumns(search.getColumns());
		if (search.getTemplates() != null) {
			for (Object o : search.getTemplates()) {
				webSearchBean.setIsAllTemplates(Boolean.FALSE);
				Template template = (Template) o;
				if ((template.getId().equals(npaTemplateId))
						|| (template.getId().equals(ordTemplateId))) {
					webSearchBean.setShowProjectNumberSearch(true);
				}
				String templateId = template.getId().getId().toString();
				webSearchBean.getTemplates().put(templateId, templateId);
			}
		}
		if (webSearchBean.getDbAttributes() != null) {
			for (Object o : webSearchBean.getDbAttributes()) {
				Attribute attribute = (Attribute) o;
				ObjectId attributeId = attribute.getId();
				if (!search.hasAttribute(attributeId)) {
					continue;
				}
				if (attribute instanceof StringAttribute) {
						/* || attribute instanceof TextAttribute) { always true */
					boolean attributeValue = ((Boolean) search.getAttribute(attributeId));
					webSearchBean.getAttributes().put(
							attributeId.getId().toString(),
							attributeValue ? attributeId.getId().toString()
									: "");
				} else if (attribute instanceof DateAttribute) {
					DatePeriod datePeriod = search
							.getDateAttributePeriod(attributeId);
					DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
					DateFormat ISO8601dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					String startDate = null;
					String startDateIso = null;
					if (datePeriod.start != null) {
						startDate = dateFormat.format(datePeriod.start);
						startDateIso = ISO8601dateFormat.format(datePeriod.start);
					}
					webSearchBean.getAttributes().put(
							attributeId.getId().toString(), startDate);
					webSearchBean.addValueDate(
							attributeId.getId().toString(), startDateIso);
					String endDate = null;
					String endDateIso = null;
					if (datePeriod.end != null) {
						endDate = dateFormat.format(datePeriod.end);
						endDateIso = ISO8601dateFormat.format(datePeriod.end);
					}
					webSearchBean.getAttributes().put(
							attributeId.getId().toString() + "_new", endDate);
					webSearchBean.addValueDate(
							attributeId.getId().toString() + "_new", endDateIso);
				} else if (attribute instanceof IntegerAttribute) {
					Interval integerAttributeInterval = search
							.getIntegerAttributeInterval(attributeId);
					webSearchBean.getAttributes().put(
							attributeId.getId().toString() + "_from",
							Long.toString(integerAttributeInterval.min));
					webSearchBean.getAttributes().put(
							attributeId.getId().toString() + "_to",
							Long.toString(integerAttributeInterval.max));
				} else if (attribute instanceof ListAttribute
						|| attribute instanceof TreeAttribute) {
					Collection referenceValues = search
							.getListAttributeValues(attributeId);
					for (Iterator itRef = referenceValues.iterator(); itRef
							.hasNext(); ) {
						ReferenceValue referenceValue = (ReferenceValue) itRef
								.next();
						webSearchBean.getAttributes().put(
								referenceValue.getId().getId().toString(),
								referenceValue.getId().getId().toString());
					}
				}
			}
		}
	}

	private WebBlock findBlock(WebSearchBean searchBean, ObjectId id) {
		List<WebBlock> blocks = new ArrayList<WebBlock>();
		blocks.addAll(searchBean.getViewBlocks1());
		blocks.addAll(searchBean.getViewBlocks2());
		for (WebBlock block : blocks) {
			if (id.getId().equals(block.getId().getId())) {
				return block;
			}

		}
		return null;

	}

	private Collection<ObjectId> parseAllowedStates(String inputString) {
		Collection<ObjectId> result = new ArrayList<ObjectId>();
		if (inputString != null) {
			for (String s : inputString.split(",")) {
				try {
					result.add(new ObjectId(CardState.class, new Long(s)));
				} catch (Exception e) {
				}
			}
		}
		return result;
	}

	/**
	 * Store personal search action handler
	 * @param request
	 * @param searchBean
	 * @throws DataException, ServiceException, RemoteException, ParseException
	 */
	private void savePersonalSearchActionHandler(PortletRequest request, WebSearchBean searchBean) 
			throws DataException, ServiceException, RemoteException, ParseException {
		DataServiceBean dataServiceBean = PortletUtil.createService(request);

		PersonalSearch newPersonalSearch = new PersonalSearch();
		String personalSearchNameStr = request.getParameter(SEARCH_NAME);
		if (!StringUtils.hasText(personalSearchNameStr)) {
			//@TODo add message that search name was not passed
			return;
		}

		final PortletService psrvc = Portal.getFactory().getPortletService();
		newPersonalSearch.setArea(psrvc.getParentPageName(request));
		newPersonalSearch.setName(personalSearchNameStr);
		
		PersonalSearch personalSearch = findPersonalSearchByNameAndArea(newPersonalSearch,
				dataServiceBean);
		if (personalSearch != null)//if there is a personal search with the same name we have to overwrite all personal search parameters
				newPersonalSearch.setId(personalSearch.getId());

		Search search = SearchUtils.getSearch(dataServiceBean, searchBean);
		newPersonalSearch.setSearch(search);


		ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME,
				request.getLocale());
		String msg = bundle
				.getString("search.person.search.store.successfully");

		ObjectId personalSearchId;
		try {
			personalSearchId = dataServiceBean.saveObject(newPersonalSearch);
			newPersonalSearch.setId(personalSearchId);
		} catch (Exception e) {
			msg = e.getLocalizedMessage();
		}
		searchBean.setMessage(msg);
		
		initializeFromSearch(newPersonalSearch.getSearch(), searchBean);
	}

	/**
	 * Delete personal search action handler
	 * @param request
	 * @param searchBean
	 */
	private void deletePersonalSearchActionHandler(PortletRequest request, WebSearchBean searchBean) {
		DataServiceBean dataServiceBean = PortletUtil.createService(request);
		String personalSearchIdStr = request.getParameter(SEARCH_ID);
		if (!StringUtils.hasText(personalSearchIdStr)) {
			//TODO add message that search id was not passed
			return;
		}
		ObjectId personalSearchId = new ObjectId(PersonalSearch.class, Long.parseLong(personalSearchIdStr));

		try {

			dataServiceBean.deleteObject(personalSearchId);

		} catch (ServiceException e) {
			logger.error(e.toString(), e);
			searchBean.setMessage(e.getLocalizedMessage());
		} catch (DataException e) {
			logger.error(e.toString(), e);
			searchBean.setMessage(e.getLocalizedMessage());
		}
	}

	/**
	 * Load personal searches by area
	 * @param request
	 * @param searchBean
	 * @throws DataException, ServiceException
	 */
	private void loadPersonalSearches(PortletRequest request, WebSearchBean searchBean) 
			throws DataException, ServiceException {

		final DataServiceBean dataServiceBean = PortletUtil.createService(request);
		final ListStoredSearchsByArea listStoredSearches = new ListStoredSearchsByArea();
		final PortletService psrvc = Portal.getFactory().getPortletService();
		listStoredSearches.setSearchArea(psrvc.getParentPageName(request));

		List<PersonalSearch> personalSearchList = dataServiceBean.doAction(listStoredSearches);

		searchBean.setPersonalSearches(personalSearchList);
	}

	/**
	 * Find personal search by name and area
	 * @param personalSearch
	 * @param dataServiceBean
	 * @throws DataException, ServiceException
	 */
	private PersonalSearch findPersonalSearchByNameAndArea(PersonalSearch personalSearch,
			DataServiceBean dataServiceBean) throws DataException, ServiceException {

		final GetPersonSearchByNameAndArea action = new GetPersonSearchByNameAndArea();
		action.setSearchArea(personalSearch.getArea());
		action.setSearchName(personalSearch.getName());
		final List<PersonalSearch> personalSearches = dataServiceBean.doAction(action);
		if (!personalSearches.isEmpty())
			return personalSearches.iterator().next();

		return  null;
	}
	
	private Date setDateInclusive(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		return calendar.getTime();
	}
	
	private void clearIsActionRequest(PortletRequest request) {
		request.getPortletSession().removeAttribute(ACTION_REQUEST);
	}
	
	private void setIsActionRequest(PortletRequest request) {
		request.getPortletSession().setAttribute(ACTION_REQUEST, TRUE);
	}

	private boolean isActionRequest(PortletRequest request) {
		String isActionReqAttr = (String) request.getPortletSession().getAttribute(ACTION_REQUEST);
		return TRUE.equals(isActionReqAttr);
	}
	
	private void clearOpenExtSearchAction(PortletRequest request) {
		request.getPortletSession().removeAttribute(WebSearchBean.OPEN_EXTENDED_SEARCH_ACTION);
	}
	
	private void clearCloseExtSearchAction(PortletRequest request) {
		request.getPortletSession().removeAttribute(WebSearchBean.CLOSE_EXTENDED_SEARCH_ACTION);
	}

	private void setExtSearchAction(PortletRequest request, String action) {
		request.getPortletSession().setAttribute(action, TRUE);
	}

	private boolean isOpenExtSearchAction(PortletRequest request) {
		String isAtionReqAttr = (String) request.getPortletSession().getAttribute(WebSearchBean.OPEN_EXTENDED_SEARCH_ACTION);
		return TRUE.equals(isAtionReqAttr);
	}
	
	private boolean isCloseExtSearchAction(PortletRequest request) {
		String isAtionReqAttr = (String) request.getPortletSession().getAttribute(WebSearchBean.CLOSE_EXTENDED_SEARCH_ACTION);
		return TRUE.equals(isAtionReqAttr);
	}
}
