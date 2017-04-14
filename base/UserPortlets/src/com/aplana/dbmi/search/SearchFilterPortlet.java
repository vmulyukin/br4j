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
import com.aplana.dbmi.card.AttributeEditor;
import com.aplana.dbmi.gui.BlockSearchView;
import com.aplana.dbmi.gui.SearchAttributeView;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.web.WebSearchBean;
import com.aplana.dbmi.search.init.SearchFilterInitializer;
import com.aplana.dbmi.service.*;
import com.aplana.web.tag.util.StringUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.portlet.context.PortletApplicationContextUtils;

import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Represents Portlet for handling advanced search
 *
 * @author skashanski
 *
 */
public class SearchFilterPortlet extends GenericPortlet {

	private static final String SEARCH_FORM_DESCRIPTION = "SearchFormDescription";

	protected static final String SEARCH_BEAN = "SEARCH_BEAN";

	public static final String SEARCH_DESC = "SEARCH_DESC";

	public static final String SEARCH_NAME = "SEARCH_NAME";

	public static final String SEARCH_ID = "SEARCH_ID";
	
	public static final String CLEAR_ATTR = "clear";

	public static final String CONFIG_FILE_PREFIX = "dbmi/";

	/**
	 * variables
	 */
	protected PortletService portletService = null;

	/**
	 * actions
	 */
	public static final String ACTION_FIELD = "ACTION_FIELD";
	public static final String SEARCH_ACTION = "SEARCH";
	public static final String LOAD_PERSON_SEARCH_ACTION = "LOAD_SEARCH";
	public static final String SAVE_PERSON_SEARCH_ACTION = "SAVE_SEARCH";
	public static final String DELETE_PERSON_SEARCH_ACTION = "DELETE_SEARCH";
	/* returns to simple previous screen(simple search) */
	public static final String BACK_ACTION = "BACK";
	public static final String CLEAR_ACTION = "CLEAR";
	
	public static final String CLEAR_SORT_ATTR = "CLEAR_SORT";


	public static final String RESOURCE_BUNDLE_NAME = "search";

	public static final String SEARCH_WORDS = "SEARCH_WORDS";

	public static final String SEARCH_FULL_TEXT = "SEARCH_FULL_TEXT";
	public static final String SEARCH_STRICT_TEXT = "SEARCH_STRICT_TEXT";

	public static final String BACK_URL_FIELD = "MI_BACK_URL_FIELD";

	public static final String SEARCH_ATTR_VIEWS = "SEARCH_ATTR_VIEWS";

	public static final String SEARCH_FORM_NAME = "SearchFormName";

	public static final String SESSION_BEAN = "SearchFilterPortletSessionBean";

	private static final String SEARCH_FILTER_JSP = "searchFilter";

	public static final String JSP_FOLDER = "/WEB-INF/jsp/";

	private static Log logger = LogFactory.getLog(SearchFilterPortlet.class);

	public static final ObjectId DOCLINKS = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");

	@Override
	public void init() throws PortletException {
		super.init();
		portletService = Portal.getFactory().getPortletService();
	}

	protected SearchFilterPortletSessionBean createSessionBean(
			PortletRequest request) {

		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);

		if (sessionBean != null)
			return sessionBean;

		PortletSession session = request.getPortletSession(true);
		sessionBean = new SearchFilterPortletSessionBean();

		AsyncDataServiceBean serviceBean = PortletUtil.createService(request);
		sessionBean.setServiceBean(serviceBean);
		sessionBean.setResourceBundle(getResourceBundle(request.getLocale()));

		session.setAttribute(SESSION_BEAN, sessionBean);

		return sessionBean;
	}

	private static String getSessionBeanAttrNameForServlet(String namespace) {
		return SESSION_BEAN + '.' + namespace;
	}

	public static SearchFilterPortletSessionBean getSessionBean(HttpServletRequest request, String namespace) {
		return (SearchFilterPortletSessionBean)request.getSession(false).getAttribute(getSessionBeanAttrNameForServlet(namespace));
	}

	protected void externalRequestHandler(PortletRequest request) {
		SearchFilterPortletSessionBean sessionBean = createSessionBean(request);
		String backURL = portletService.getUrlParameter(request, BACK_URL_FIELD);
		if (StringUtils.hasLength(backURL))
			sessionBean.setBackURL(backURL);
	}

	SearchFormDescription globalSearchFormDescription = null;

	@Override
	protected void doView(RenderRequest request, RenderResponse response)
			throws PortletException, PortletSecurityException, IOException {

		externalRequestHandler(request);

		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);

		if (!checkSessionBean(response, sessionBean))
			return;

		// Set the MIME type for the render response
		initContentType(request, response);
		// Set user locale
		setLocale(request);

		globalSearchFormDescription = getSearchFormDescription(request);

		//stores SearchBean initialized by default values if it is new search page request
		//user click for example on Extended Search Link inside any page
		initDefaultSearchPage(request, sessionBean, globalSearchFormDescription, getParentTemplate(request));

		loadPersonalSearches(sessionBean);


		initSearchBlockViews(request, globalSearchFormDescription);

		//stores session bean for SearchCardServlet
		saveSessionBeanForServlet(request, response, sessionBean);

		// render jsp
		renderJsp(request, response);
	}

	private String getAreaName(SearchFilterPortletSessionBean sessionBean ) {
		String searchFormName = sessionBean.getExtendedSearchFormName();
		String result = searchFormName;

		if (searchFormName.endsWith(SEARCH_FORM_DESCRIPTION)) {
			int charCount = searchFormName.length() - (SEARCH_FORM_DESCRIPTION).length();
			return result.substring(0, charCount);
		}

		return result;
	}

	/**
	 * Loads stored personal searches for current user
	 * and stored it in SessionBean
	 */
	protected void loadPersonalSearches(SearchFilterPortletSessionBean sessionBean) throws PortletException {
		DataServiceBean dataServiceBean = sessionBean.getServiceBean();
		ListStoredSearchsByArea listStoredSearches = new ListStoredSearchsByArea();
		listStoredSearches.setSearchArea(getAreaName(sessionBean));
        List<PersonalSearch> personalSearchList;
		try {
			personalSearchList = dataServiceBean.doAction(listStoredSearches);
		} catch (DataException e) {
			throw new PortletException(e);
		} catch (ServiceException e) {
			throw new PortletException(e);
		}

        sessionBean.setPersonalSearches(personalSearchList);
	}

	protected void setLocale(RenderRequest request) {
		ContextProvider.getContext().setLocale(request.getLocale());
	}

	protected void initContentType(RenderRequest request,
			RenderResponse response) {
		response.setContentType(request.getResponseContentType());
	}

	protected String getSearchFilterJsp() {
		String jspFile = SEARCH_FILTER_JSP;
		return jspFile;
	}

	protected boolean checkSessionBean(RenderResponse response,
			SearchFilterPortletSessionBean sessionBean) throws IOException {

		if (sessionBean == null) {
			response.getWriter().println("<b>PORTLET SESSION IS NOT INITIALIZED YET</b>");
			return false;
		}
		return true;
	}

	private void initDefaultSearchPage(RenderRequest request,
			SearchFilterPortletSessionBean sessionBean,
			SearchFormDescription searchFormDescription,
			String parentTemplate) {

		Search search = createDefaultSearch(request, searchFormDescription.getDefaultSearchConfigFile(), parentTemplate);

		sessionBean.setExtendedSearchFormName(searchFormDescription.getName());
		sessionBean.setParentTemplateId(parentTemplate);

		if ((search.getTemplates()!=null) && (!search.getTemplates().isEmpty())) {
			sessionBean.setSearchPageTemplates((List<Template>)search.getTemplates());
		}

		if (!isNewSearchPageRequest(request))
			return;
	}

	/**
	 * Returns search form description according to current page(income,outcome and etc)
	 */
	private SearchFormDescription getSearchFormDescription(PortletRequest request) {

		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);

		String extendedSearchForm = portletService.getUrlParameter(request, WebSearchBean.EXTENDED_SEARCH_FORM_NAME);

		if (!StringUtils.hasText(extendedSearchForm)) {
			extendedSearchForm = sessionBean.getExtendedSearchFormName();
		}

		if (!StringUtils.hasText(extendedSearchForm)) {
			extendedSearchForm = portletService.getPageProperty(SearchPortlet.DEFAULT_SEARCH_FORM, request, null);
		}

		if (!StringUtils.hasText(extendedSearchForm))
				throw new RuntimeException("Parameter extendedSearchForm was not passed.Impossible to get SearchForm configuration!");

		return getSpringBean(extendedSearchForm);
	}

	private String getParentTemplate(PortletRequest request) {
		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);
		String parentTemplate = portletService.getUrlParameter(request, Search.PARENT_TEMPLATE);

		if (!StringUtils.hasText(parentTemplate)) {
			parentTemplate = sessionBean.getParentTemplateId();
		}

		return parentTemplate;
	}

	@SuppressWarnings("unchecked")
	protected <T> T getSpringBean(String beanName) {
		PortletContext pc = this.getPortletContext();
		ApplicationContext wac = PortletApplicationContextUtils.getWebApplicationContext(pc);
		return (T) wac.getBean(beanName);
	}

	/**
	 * Checks if passed request is new Search page request..User go to another search page
	 */
	private boolean isNewSearchPageRequest(PortletRequest request) {
		String extendedSearchForm = portletService.getUrlParameter(request, WebSearchBean.EXTENDED_SEARCH_FORM_NAME);
		return (StringUtils.hasText(extendedSearchForm));
	}

	/**
	 * Creates and returns default search
	 */
	private Search createDefaultSearch(PortletRequest request, String defaultSearchConfigFile, String parentTemplate) {

		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);
		Search defaultSearch = new Search();

		if (!StringUtils.hasText(defaultSearchConfigFile))
			return defaultSearch;
		InputStream inputStream = null;
		try {
			inputStream = Portal.getFactory().getConfigService()
					.loadConfigFile(CONFIG_FILE_PREFIX + defaultSearchConfigFile);
			defaultSearch.initFromXml(inputStream);
		} catch (IOException e) {
			try {
				inputStream.close();
			} catch (Exception e2) {}
			sessionBean.setMessage(e.getMessage());
		} catch (DataException e) {
			try {
				inputStream.close();
			} catch (Exception e2) {}
			sessionBean.setMessage(e.getMessage());
		}
		return defaultSearch;
	}

	protected void initSearchBlockViews(PortletRequest request, SearchFormDescription searchFormDescription)
			throws PortletException  {

		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);

		if (!isNewSearchPageRequest(request) && (!sessionBean.getSearchBlockViews().isEmpty()) ) {
			return;
		}

		if (searchFormDescription.getTitle() !=null)
			sessionBean.setHeader(searchFormDescription.getTitle());

		//generate attribute views if it is a new search page request
		sessionBean.getSearchBlockViews().clear();
		sessionBean.clearCommonParameters();

		for (SearchBlockDescription searchBlockDesc : searchFormDescription.getSearchBlockDescriptions()) {
			BlockSearchView blockSearchView = new BlockSearchView();
			blockSearchView.setId(searchBlockDesc.getId());
			blockSearchView.setName(searchBlockDesc.getName());
			blockSearchView.setDivClass(searchBlockDesc.getDivClass());
			blockSearchView.setColumnsNumber(searchBlockDesc.getColumnsNumber());
			List<SearchAttributeView> searchAttrViews = generateSearchAttributeViews(request, searchBlockDesc.getSearchAttributes(), searchBlockDesc.getSpanedAttributeIds());
			blockSearchView.setSearchAttributes(searchAttrViews);
			sessionBean.getSearchBlockViews().add(blockSearchView);
		}
	}

	private void saveSessionBeanForServlet(RenderRequest request, RenderResponse response, SearchFilterPortletSessionBean sessionBean) {
		PortletSession session = request.getPortletSession();
		String namespace = response.getNamespace();
		session.setAttribute(getSessionBeanAttrNameForServlet(namespace), sessionBean, PortletSession.APPLICATION_SCOPE);
	}

	private List<SearchAttributeView> generateSearchAttributeViews ( PortletRequest request,
			List<Attribute> searchAttributes, List<String> spanedAttributesIds) throws PortletException {

		List<SearchAttributeView> result = new ArrayList<SearchAttributeView>();

		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);

		SearchAttributeViewCreatorFactory factory = SearchAttributeViewCreatorFactory.getFactory();

		for (Attribute searchAttributeId : searchAttributes) {
			//clear all fields at first
			searchAttributeId.clear();
			SearchAttributeView attributeView = createAndInitSearchAtrributeView(
					request, sessionBean, factory, searchAttributeId);
			if (spanedAttributesIds != null && spanedAttributesIds.contains(attributeView.getAttribute().getId().getId())) {
				attributeView.setSpanedView(true);
			}
			result.add(attributeView);
		}
		return result;
	}

	private SearchAttributeView createAndInitSearchAtrributeView(
			PortletRequest request, SearchFilterPortletSessionBean sessionBean,
			SearchAttributeViewCreatorFactory factory,
			Attribute searchAttributeId) throws PortletException {

		SearchAttributeViewCreator searchAttributeViewCreator = factory.getSearchAttributeViewCreator(searchAttributeId);
		SearchAttributeView attributeView;
		try {
			attributeView = searchAttributeViewCreator.create(request,
					sessionBean);
		} catch (Exception e) {
			throw new PortletException(e);
		}
		return attributeView;
	}

	private void initializeNames(Attribute searchAttributeId,
			Attribute searchAttribute) {

		if (StringUtils.hasText(searchAttributeId.getNameRu()))
			searchAttribute.setNameRu(searchAttributeId.getNameRu());

		if (StringUtils.hasText(searchAttributeId.getNameEn()))
			searchAttribute.setNameEn(searchAttributeId.getNameEn());
	}

	/**
	 * Returns available card states for passed template
	 */
	private Collection<CardState> getCardStates(SearchFilterPortletSessionBean sessionBean) {
		DataServiceBean serviceBean = sessionBean.getServiceBean();
		if (sessionBean.getSearchPageTemplate() == null)
			return Collections.emptyList();
		try {
			Template fullTemplate = serviceBean.getById(sessionBean.getSearchPageTemplate().getId());
			return serviceBean.listChildren(fullTemplate.getWorkflow(), CardState.class);
		} catch (DataException e) {
			sessionBean.setMessage(e.getMessage());
			logger.error(e.toString(), e);
		} catch (ServiceException e) {
			sessionBean.setMessage(e.getMessage());
			logger.error(e.toString(), e);
		}
		return Collections.emptyList();
	}

	private Collection<CardState> getPredefinedCardStates(StateSearchAttribute stateSearchAttribute){
		String states = stateSearchAttribute.getPredefCardStatesString();
		if(StringUtils.hasLength(states)){
			final String[] statesVal = states.split(",");
			ArrayList<CardState> realCardStates = new ArrayList<CardState>(1);
			for (String aStatesVal : statesVal) {
				CardState cs = new CardState();
				cs.setId(ObjectId.predefined(CardState.class, aStatesVal.trim()));
				realCardStates.add(cs);
			}
			return realCardStates;
		} else
			return Collections.emptyList();
	}

	/**
	 * Copies all properties values from passed attribute
	 * @param attributeCloneTo clone to copy to
	 * @param attrCloneFrom clone to copy from
	 */
	public void copyProperties(Attribute attributeCloneTo, Attribute attrCloneFrom) {
		try {
			PropertyUtils.copyProperties(attributeCloneTo, attrCloneFrom);
		} catch (NoSuchMethodException  e) {
			throw new RuntimeException(e);
		} catch(InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}


	protected void renderJsp(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		String jspFile = getSearchFilterJsp();
		// Invoke the JSP to render
		PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(
				getJspFilePath(request, jspFile));
		rd.include(request, response);
	}

	/**
	 * Returns JSP file path.
	 *
	 * @param request
	 *            Render request
	 * @param jspFile
	 *            JSP file name
	 * @return JSP file path
	 */
	private static String getJspFilePath(RenderRequest request, String jspFile) {
		return JSP_FOLDER + jspFile + ".jsp";
	}

	/**
	 * Process an action request.
	 *
	 * @see javax.portlet.Portlet#processAction(javax.portlet.ActionRequest,
	 *      javax.portlet.ActionResponse)
	 */
	@Override
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, PortletSecurityException, IOException {

		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale());

		String action = request.getParameter(ACTION_FIELD);

		clearMessage(request);

		if (SEARCH_ACTION.equals(action)) {
			searchActionHandler(request, response);
		} else if (LOAD_PERSON_SEARCH_ACTION.equals(action)){
			loadPersonSearchActionHandler(request, response);
		} else if (SAVE_PERSON_SEARCH_ACTION.equals(action)){
			savePersonSearchActionHandler(request, response);
		} else if (DELETE_PERSON_SEARCH_ACTION.equals(action)){
			deletePersonSearchActionHandler(request, response);
		} else if (BACK_ACTION.equals(action)) {
			backActionHandler(request, response);
		} else if (CLEAR_ACTION.equals(action)) {
			clearActionHandler(request, response);
		} else
			throw new UnsupportedOperationException();
	}

	private void clearMessage(ActionRequest request) {
		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);
		sessionBean.clearMessage();
	}

	protected void searchActionHandler(ActionRequest request,
			ActionResponse response) throws IOException {
		collapseAllBlocks(request);
		searchActionHandler(request);
		copyRequestParameters(request, response);
	}

	/**
	 * Loads person search action handler
	 */
	protected void loadPersonSearchActionHandler(ActionRequest request,
			ActionResponse response) throws IOException {

		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);

		String personalSearchId = request.getParameter(SEARCH_ID);
		if (!StringUtils.hasText(personalSearchId)) {
			//@TODo add message that search name was not passed
			return;
		}

		PersonalSearch personalSearch = getPersonalSearchById(sessionBean, personalSearchId);

		if (personalSearch != null)
			initSearchBeanFromPersonalSearch(personalSearch, request);

		//clear search bean to avoid redundant search in MIShowListPortlet
		clearSearchBean(request);
	}

	private PersonalSearch getPersonalSearchById(
			SearchFilterPortletSessionBean sessionBean, String personalSearchId) {
		try {
			return (PersonalSearch) sessionBean
					.getServiceBean()
					.getById(new ObjectId(PersonalSearch.class, personalSearchId));
		} catch (DataException e) {
			sessionBean.setMessage(e.getMessage());
			logger.error(e.toString(), e);
			return null;
		} catch (ServiceException e) {
			sessionBean.setMessage(e.getMessage());
			logger.error(e.toString(), e);
			return null;
		}
	}

	protected void initSearchBeanFromPersonalSearch(PersonalSearch personSearch, ActionRequest request) {
		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);
		Search personalSearch = initSearchFromXml(personSearch.getSearchXml(), sessionBean);

		SearchFilterInitializer searchFilterInitializer = getSpringBean("searchFilterInitializer");
		if (searchFilterInitializer == null)
			throw new RuntimeException("SearchFilterInitializer was not defined!");

		searchFilterInitializer.setSearch(personalSearch);
		searchFilterInitializer.setSessionBean(sessionBean);
		searchFilterInitializer.initialize();
	}

	private Search initSearchFromXml(String searchXml, SearchFilterPortletSessionBean bean) {
		Search personalSearch = new Search();
		try {
			personalSearch.initFromXml(new ByteArrayInputStream(searchXml.getBytes("UTF-8")));
		} catch (DataException e) {
			bean.setMessage(e.getMessage());
			logger.error(e.toString(), e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.toString(), e);
			throw new RuntimeException(e);
		}
		return personalSearch;
	}

	/**
	 * Removes person search action handler
	 */
	protected void deletePersonSearchActionHandler(ActionRequest request,
			ActionResponse response) throws IOException {

		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);

		String personalSearchIdStr = request.getParameter(SEARCH_ID);
		if (!StringUtils.hasText(personalSearchIdStr)) {
			//@TODo add message that search name was not passed
			return;
		}
		ObjectId personSearchId = new ObjectId(PersonalSearch.class, Long.parseLong(personalSearchIdStr));

		removePersonSearch(sessionBean, personSearchId);

		//clear search bean to avoid redundant search in MIShowListPortlet
		clearSearchBean(request);
	}

	/**
	 * Removes personal search data from the store
	 * @param personalSearchId personal search identifier to remove
	 */
	private void removePersonSearch(SearchFilterPortletSessionBean sessionBean,
			ObjectId personalSearchId) {
		try {
			sessionBean.getServiceBean().deleteObject(personalSearchId);
		} catch (ServiceException e) {
			logger.error(e.toString(), e);
			sessionBean.setMessage(e.getLocalizedMessage());
		} catch (DataException e) {
			logger.error(e.toString(), e);
			sessionBean.setMessage(e.getLocalizedMessage());
		}
	}

	/**
	 * Store person search action handler
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	protected void savePersonSearchActionHandler(ActionRequest request,
			ActionResponse response) throws IOException {

		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);
		sessionBean.setSaveMode(true);
		
		ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, request.getLocale());
		String msg = bundle.getString("search.person.search.store.successfully");

		try {		
			PersonalSearch newPersonalSearch = new PersonalSearch();
			parseSavePersonalSearchParameters(request, newPersonalSearch);
			newPersonalSearch.setArea(getAreaName(sessionBean));
			
			DataServiceBean dataServiceBean = sessionBean.getServiceBean();
			PersonalSearch personalSearch = findPersonalSearchByNameAndArea(newPersonalSearch,
																						dataServiceBean);
			if (personalSearch != null)//if there is a personal search with the same name we have to overwrite all personal search parameters
				newPersonalSearch.setId(personalSearch.getId());

			dataServiceBean.saveObject(newPersonalSearch);
			//clear search bean to avoid redundant search in MIShowListPortlet
			clearSearchBean(request);
		} catch (DataException e) {
			logger.error(e.toString(), e);
			sessionBean.setMessage(e.getLocalizedMessage());

		} catch (ServiceException e) {
			logger.error(e.toString(), e);
			sessionBean.setMessage(e.getLocalizedMessage());
		} finally{
			sessionBean.setSaveMode(false);
		}
		sessionBean.setMessage(msg);
	}

	private PersonalSearch findPersonalSearchByNameAndArea(PersonalSearch newPersonalSearch,
				DataServiceBean dataServiceBean) throws DataException, ServiceException {

		GetPersonSearchByNameAndArea action = new GetPersonSearchByNameAndArea();
		action.setSearchArea(newPersonalSearch.getArea());
		action.setSearchName(newPersonalSearch.getName());
		ArrayList<PersonalSearch> personalSearches = (ArrayList<PersonalSearch>)dataServiceBean.doAction(action);
		if (!personalSearches.isEmpty())
			return  personalSearches.iterator().next();

		return null;
	}

	private void parseSavePersonalSearchParameters(ActionRequest request,
			PersonalSearch newPersonalSearch) {

		String searchName = request.getParameter(SEARCH_NAME);
		if (!StringUtils.hasText(searchName)) {
			//@TODo add message that search name was not passed
			return;
		}

		String searchDesc = request.getParameter(SEARCH_DESC);

		//firstly call method to create Search object from UI
		Search newSearch = createAndInitSearch(request);

		newPersonalSearch.setName(searchName);
		newPersonalSearch.setDescription(searchDesc);

		newPersonalSearch.setSearch(newSearch);
	}

	private void copyRequestParameters(ActionRequest request,
			ActionResponse response) {

		HashMap rParams = new HashMap(request.getParameterMap());
		response.setRenderParameters(rParams);
	}

	private Attribute getAttributeById(	SearchFilterPortletSessionBean portletSessionBean,
			Attribute searchAttr) {

		if (searchAttr == null)
			return null;

		Attribute attribute = null;

		try {
			attribute = portletSessionBean.getServiceBean().getById(searchAttr.getId());
		} catch (ServiceException e) {
			// log with attribute TYPE & CODE
			logger.error("Invalid attribute id = " + searchAttr.getId(), e);
		} catch (DataException e) {
			// log with attribute TYPE & CODE
			logger.error("Invalid attribute id = " + searchAttr.getId(), e);
		}

		return attribute;
	}

	/**
	 * Returns SessionBean data
	 *
	 * @param request
	 * @return
	 */
	public static SearchFilterPortletSessionBean getSessionBean(
			PortletRequest request) {

		PortletSession session = request.getPortletSession();
		if (session == null) {
			logger.warn("Portlet session is not exists yet.");
			return null;
		}
		SearchFilterPortletSessionBean sessionBean = (SearchFilterPortletSessionBean) session
        .getAttribute(SESSION_BEAN);
		if (sessionBean == null)
			return null;
        String userName = (String) request.getPortletSession().getAttribute(DataServiceBean.USER_NAME, PortletSession.APPLICATION_SCOPE);
        if (userName != null) {
            sessionBean.getServiceBean().setUser(new UserPrincipal(userName));
            sessionBean.getServiceBean().setIsDelegation(true);
            sessionBean.getServiceBean().setRealUser(request.getUserPrincipal());
        } else {
            sessionBean.getServiceBean().setUser(request.getUserPrincipal());
            sessionBean.getServiceBean().setIsDelegation(false);
        }

		return sessionBean;
	}

	/**
	 * Parses search parameters and store them into attributes
	 *
	 * @param request
	 */
	protected void parseSearchRequest(ActionRequest request,List<SearchAttributeView> searchAttrViews) {

		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);

		for( SearchAttributeView searchAttrView : searchAttrViews ) {

			AttributeEditor editor = searchAttrView.getEditor();
			if (editor == null)
				continue;

			try {

				editor.gatherData(request, searchAttrView.getAttribute());

			} catch (DataException e) {
				sessionBean.setMessage(e.getMessage());
				logger.error(e.toString(), e);
			}
		}

	}

	protected void searchActionHandler(ActionRequest request) {

		Search search = createAndInitSearch(request);
		
		request.getPortletSession().setAttribute(CLEAR_SORT_ATTR, "true",
				PortletSession.APPLICATION_SCOPE);

		// stores created Search in request to pass this search parameters to
		// MIShowListPortlet
		storeSearchBean(request, search);

	}

	private Search createAndInitSearch(ActionRequest request) {

		SearchFormDescription searchFormDescription = getSearchFormDescription(request);
		String parentTemplate = getParentTemplate(request);

		Search search = createDefaultSearch(request, searchFormDescription.getDefaultSearchConfigFile(), parentTemplate);

		search.setByAttributes(true);

		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);

		parseSearchParameters(request, search);

		parseSearchAttributes(request, search, sessionBean);

		initSearchAttributes(search, sessionBean);

		return search;
	}

	private void parseSearchAttributes(ActionRequest request, Search search,
			SearchFilterPortletSessionBean sessionBean) {

		for(BlockSearchView blockSearchView : sessionBean.getSearchBlockViews()) {
			//collect data form UI into attributes
			parseSearchRequest(request, blockSearchView.getSearchAttributes());
		}
	}

	private void initSearchAttributes(Search search,
			SearchFilterPortletSessionBean sessionBean) {

		SearchInitializer searchInitializer = getSpringBean("searchInitializer");

		if (searchInitializer == null)
			throw new RuntimeException("SearchInitializer was not defined!");

		searchInitializer.setSearch(search);
		searchInitializer.setSessionBean(sessionBean);
		searchInitializer.initialize(sessionBean.isSaveMode());

	}

	/**
	 * Parses search parameters : searchWords, search full Text
	 */
	protected void parseSearchParameters(ActionRequest request, Search search) {


		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);

		String searchWords = request.getParameter(SEARCH_WORDS);
		boolean searchStrictText=convertStringToBoolean(request.getParameter(SEARCH_STRICT_TEXT));

		if (StringUtils.hasText(searchWords)) {
			sessionBean.setSearchWords(searchWords.trim());
			sessionBean.setSearchStrictWords(searchStrictText);
		}

		if ( (!StringUtils.hasText(searchWords)) && StringUtils.hasText(sessionBean.getSearchWords())) {
			sessionBean.setSearchWords("");
			sessionBean.setSearchStrictWords(searchStrictText);
		}

		String searchFullText = request.getParameter(SEARCH_FULL_TEXT);


		boolean searchFullTextBool = Boolean.parseBoolean(searchFullText);

		sessionBean.setByMaterial(searchFullTextBool);

	}


	/**
	 * Clears all search fields
	 */
	protected void clearActionHandler(ActionRequest request, ActionResponse response) throws IOException {

		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);

		sessionBean.clearAttributeEditorsData();

		sessionBean.clearSearchBlockViews();

		sessionBean.clearCommonParameters();

		//clear search bean to avoid redundant search in MIShowListPortlet
		clearSearchBean(request);

		request.getPortletSession().setAttribute(CLEAR_ATTR, "true", PortletSession.APPLICATION_SCOPE);
		
		String extendedSearchForm = globalSearchFormDescription.getName();
		// ���� ������� ����������� ����� ���������, �� ��������� � ���� ������
		if ("searchFormDescriptionWS".equals(extendedSearchForm)){
			String backURL = portletService.generateLink("cleanAdvancedSearchPath", null, null, request, response) + "?extendedSearchForm="+extendedSearchForm;
			response.sendRedirect(backURL);
		}
	}

	protected void backActionHandler(ActionRequest request, ActionResponse response) throws IOException {

		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);

		String backURL = sessionBean.getBackURL();

		clearSearchBean(request);

		if (StringUtils.hasLength(backURL))

			response.sendRedirect(backURL);
		else
			redirectToPortalDefaultPage(request, response);


	}

	protected void redirectToPortalDefaultPage(ActionRequest request, ActionResponse response) throws IOException {

		String backURL = portletService.generateLink("dbmi.defaultPage", null, null, request, response);

		response.sendRedirect(backURL);

	}


	/**
	 * Stores created Search bean in portlet session for MIShowListPortlet
	 * @param search created Search object
	 */
	protected void storeSearchBean(PortletRequest request, Search search) {

		request.getPortletSession().setAttribute(SEARCH_BEAN, search,
				PortletSession.APPLICATION_SCOPE);

	}


	/**
	 * Removes search bean from given request
	 */
	protected void clearSearchBean(PortletRequest request) {

		request.getPortletSession().setAttribute(SEARCH_BEAN, null, PortletSession.APPLICATION_SCOPE);

	}

	protected boolean convertStringToBoolean(String value){
		boolean result=false;
		if(value!=null){
			result=value.equals("on")?true:Boolean.parseBoolean(value);
		}
		return result;
	}

	private void collapseAllBlocks(ActionRequest request){
		for(BlockSearchView blockSearchView: getSessionBean(request).getSearchBlockViews()){
			blockSearchView.setCurrentState(BlockViewParam.COLLAPSE);
		}
	}

}
