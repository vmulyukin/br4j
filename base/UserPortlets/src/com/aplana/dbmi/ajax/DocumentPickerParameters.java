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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.cms.ContentUtils;
import com.aplana.dbmi.action.SearchByTemplatePeriodNameAction;
import com.aplana.dbmi.action.SearchRelatedDocsForReport;
import com.aplana.dbmi.action.Search.Filter;
import com.aplana.dbmi.actionhandler.ActionsManager;
import com.aplana.dbmi.card.CardLinkPickerAttributeEditor;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerVariantDescriptor;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.web.tag.util.StringUtils;

/**
 * Represents Parameters for DocumentPicker
 * 
 * @author skashanski
 *
 */
public class DocumentPickerParameters implements
		CardHierarchyServletParameters {

	private final Log logger = LogFactory.getLog(getClass());

	public static final String CALLER = "cardLinkPickerDialogHierarchy";

	public static final String PARAM_ACTIVE_VARIANT = "activeVariant";

	public static final String PARAM_FILTER_SCOPE = "filterScope";
	
	public static final String PARAM_FILTER_YEAR = "filterYear";

	public static final String PARAM_FILTER_TEMPLATE = "filterTemplate";
	public static final String PARAM_FILTER_QUERY = "filterQuery";

	public static final String PARAM_FILTER_ID = "filterId";
	
	public static final String PARAM_FILTER_REGNUM = "filterRegNum";
	public static final String PARAM_REGNUM_STRICT_SEARCH = "regNumStrictSearch";
	
	public static final String PARAM_FILTER_PROJECTNUMBER = "filterProjectNumber";
	public static final String PARAM_PROJECTNUMBER_STRICT_SEARCH = "projectNumberStrictSearch";
	
	public static final String PARAM_FILTER_OGAUTHOR = "filterOGAuthor";
	public static final String PARAM_OGAUTHOR_STRICT_SEARCH = "OGAuthorStrictSearch";
	
	public static final String PARAM_FILTER_OUTNUMBER = "filterOutNumber";
	public static final String PARAM_OUTNUMBER_STRICT_SEARCH = "outNumberStrictSearch";
	
	public static final String PARAM_BASE_CARD_ID = "baseCardId";

	final private static ObjectId incomeId = ObjectId.predefined(Template.class, "jbr.incoming");
	final private static ObjectId outcomeId = ObjectId.predefined(Template.class, "jbr.outcoming");
	final private static ObjectId interId = ObjectId.predefined(Template.class, "jbr.interndoc");

	final private static ObjectId ordId = ObjectId.predefined(Template.class, "jbr.ord");
	final private static ObjectId ogId = ObjectId.predefined(Template.class, "jbr.incomingpeople");
	final private static ObjectId npaId = ObjectId.predefined(Template.class, "jbr.npa");
	//final private static ObjectId izId = ObjectId.predefined(Template.class, "jbr.infreq");

	final private static ObjectId[] MAINDOC_IDS = { incomeId, outcomeId, interId, ordId, ogId, npaId};


	//limit size data to return due to optimization
	//it doesn't make sense to display more then 300 records in dialog
	public static int DATA_SIZE = 300;

	private String requestType = null;
	private CardPortletCardInfo cardInfo;
	private ObjectId attrId;
	private CardLinkPickerVariantDescriptor variantDescriptor;
	private DataServiceBean serviceBean;
	private HierarchyConnection hconn;
	private String hierarchyKey;
	private ObjectId baseCardId;

	final private SearchByTemplatePeriodNameAction action = new SearchByTemplatePeriodNameAction(); 

	public ActionsManager getActionsManager() {
		return CardLinkPickerAttributeEditor.getActionsManager(attrId, variantDescriptor, cardInfo);
	}

	public HierarchyConnection getHierarchyConnection() {
		// Returns stored connection in case we adding Items in Tree as we need to get NEXT following items
		//otherwise returns null as we need always filter by Year and Template
		if (CardHierarchyServlet.REQUEST_ADD_ITEMS.equalsIgnoreCase(requestType) 
				|| CardHierarchyServlet.REQUEST_ALL_ITEMS.equalsIgnoreCase(requestType))
			return hconn;
		return null;
	}

	public HierarchyDescriptor getHierarchyDescriptor() {
		return variantDescriptor.getHierarchyDescriptor();
	}

	public Collection<ObjectId> getStoredCards() {
		try {
			return (Collection<ObjectId>)serviceBean.doAction(action);
		} catch (Exception e) {
			logger.error("Failed to load stored cards", e);
			return new ArrayList<ObjectId>(0);
		}
	}


	public void storeHierarchyConnection(HierarchyConnection aconn) {
		if (hierarchyKey != null) {
			cardInfo.setAttributeEditorData(attrId, hierarchyKey, aconn);
		}
	}


	public void init(HttpServletRequest request) throws ServletException {
		initializeVariables(request);
		parseFilterTemplate(request);
		parseFilterScope(request);
		parseFilterYear(request);
		parseFilterCard(request);
		parseFilterRegNum(request);
		parseFilterProjectNumber(request);
		parseFilterOutNumber(request);
		parseFilterOGAuthor(request);
		parseIgnoredCards();
	}
	
	private void parseIgnoredCards(){
		Collection<ObjectId> cardIds = new HashSet<ObjectId>();
		if(baseCardId!=null){
			cardIds.add(baseCardId);
		}
		action.setIgnoredCards(cardIds);
	}
	
	private void parseFilterScope(HttpServletRequest request){
		String filterScope = request.getParameter(PARAM_FILTER_SCOPE);
		if(!StringUtils.hasText(filterScope) || !filterScope.equals("1") && !filterScope.equals("2")) return;
		else {
			SearchRelatedDocsForReport filterAction = new SearchRelatedDocsForReport();
			action.setFilterAction(filterAction);
			filterAction.setCard(cardInfo.getCard());
			if(filterScope.equals("1")){
				filterAction.setScope(SearchRelatedDocsForReport.Scope.SUBREPORTS);
			}
			else if(filterScope.equals("2")){
				filterAction.setScope(SearchRelatedDocsForReport.Scope.WHOLE_DOCUMENT);
			}
		}
	}
	
	private void parseFilterYear(HttpServletRequest request)
			throws ServletException {
		// parse year
		String filterYear = request.getParameter(PARAM_FILTER_YEAR);

		if (!StringUtils.hasText(filterYear))
			return;

		try {
			int intFilterYear = Integer.valueOf(filterYear);
			Date startDate = getStartDate(intFilterYear);

			Date endDate = getEndDate(intFilterYear);

			action.setStartPeriod(startDate);
			action.setEndPeriod(endDate);

		} catch (NumberFormatException e) {
			throw new ServletException("Invalid filter year value", e);
		}

	}
	private void parseFilterRegNum(HttpServletRequest request)
			throws ServletException {
		
		final String filterRegNumVal = request.getParameter(PARAM_FILTER_REGNUM);
		if(!StringUtils.hasText(filterRegNumVal)){			
			return;
		}
		List<String> regNums = Arrays.asList(filterRegNumVal.trim().split("\\s*[,;]\\s*"));
		for(Iterator<String> it = regNums.iterator(); it.hasNext();) if(it.next().length() == 0) it.remove();

		action.setRegNums(regNums);	
		action.setRegNumStrictSearch(Boolean.parseBoolean(request.getParameter(PARAM_REGNUM_STRICT_SEARCH)));
		
	}
	
	private void parseFilterProjectNumber(HttpServletRequest request){	
		String unparsed = request.getParameter(PARAM_FILTER_PROJECTNUMBER);
		if(!StringUtils.hasText(unparsed)){			
			return;
		}
		String[] parsed = unparsed.split("[^0-9]{1,}");
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		for(String s : parsed) try {
			numbers.add(Integer.parseInt(s));
		} catch(NumberFormatException e){}
		action.setProjectNumbers(numbers);
		action.setProjectNumbersStrictSearch(Boolean.parseBoolean(request.getParameter(PARAM_PROJECTNUMBER_STRICT_SEARCH)));
	}
	
	
	private void parseFilterOutNumber(HttpServletRequest request){	
		final String filterOutNumberVal = request.getParameter(PARAM_FILTER_OUTNUMBER);
		if(!StringUtils.hasText(filterOutNumberVal)){			
			return;
		}
		List<String> outNumbers = Arrays.asList(filterOutNumberVal.trim().split("\\s*[,;]\\s*"));
		for(Iterator<String> it = outNumbers.iterator(); it.hasNext();) if(it.next().length() == 0) it.remove();

		action.setOutNumbers(outNumbers);
		action.setOutNumbersStrictSearch(Boolean.parseBoolean(request.getParameter(PARAM_OUTNUMBER_STRICT_SEARCH)));
	}
	
	
	private void parseFilterOGAuthor(HttpServletRequest request){	
		final String filterOGAuthor = request.getParameter(PARAM_FILTER_OGAUTHOR);
		if(!StringUtils.hasText(filterOGAuthor)){			
			return;
		}
		action.setOGAuthor(filterOGAuthor);
		action.setOGAuthorStrictSearch(Boolean.parseBoolean(request.getParameter(PARAM_OGAUTHOR_STRICT_SEARCH)));
	}

	private void parseFilterTemplate(HttpServletRequest request)
			throws ServletException {

		final List<ObjectId> filterTemplates = new ArrayList<ObjectId>();

		final String filterTemplateVal = request.getParameter(PARAM_FILTER_TEMPLATE);
		if (StringUtils.hasText(filterTemplateVal)) {
			filterTemplates.add( getFilterTemplateId(filterTemplateVal) );
		} else { // by default: use "maindoc" templates ...
			filterTemplates.addAll( Arrays.asList(MAINDOC_IDS));
		}

		action.setTemplates(filterTemplates);
	}

	private void parseFilterCard(HttpServletRequest request)
			throws ServletException {
		final String filterIdVal = request.getParameter(PARAM_FILTER_ID);
		if (!StringUtils.hasText(filterIdVal))
			return;
		final Card filterCard = getFilterCard(filterIdVal);
		action.setCard(filterCard);
	}
	
	private void initializeVariables(HttpServletRequest request)
			throws ServletException {
		final String namespace = request.getParameter(PARAM_NAMESPACE);
		final CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request, namespace);

		serviceBean = sessionBean.getServiceBean();
		cardInfo = sessionBean.getActiveCardInfo();

		hierarchyKey = request.getParameter(PARAM_HIERARCHY_KEY);
		requestType = request.getParameter(CardHierarchyServlet.PARAM_REQUEST_TYPE); 

		final String attrCode = request.getParameter(PARAM_ATTR_CODE);
		Card card = retrieveCard(request);
		baseCardId = card.getId();
		
		final Attribute attr = AttrUtils.getAttributeByCode(attrCode, card);
		if (attr == null) {
			throw new ServletException("Couldn't find attribute with code '" + attrCode + "' in card");
		}
		attrId = attr.getId();

		hconn = (HierarchyConnection)cardInfo.getAttributeEditorData(attrId, hierarchyKey);
		CardLinkPickerDescriptor d = (CardLinkPickerDescriptor)cardInfo.getAttributeEditorData(attrId, CardLinkPickerAttributeEditor.KEY_DESCRIPTOR);

		final String paramAlias = request.getParameter(PARAM_ACTIVE_VARIANT);
		variantDescriptor = d.getVariantDescriptor(paramAlias);
		if (variantDescriptor == null) {
			throw new ServletException("Couldn't find variant descriptor with alias: '" + paramAlias + "'");
		}

		final String filterQuery = request.getParameter(PARAM_FILTER_QUERY);
		initSearchAction(filterQuery);
	}

	private Card retrieveCard(HttpServletRequest request) {
		Card card = cardInfo.getCard();
		if(card.getId() == null) {
			// retrieve card by id from request, if cardInfo contains empty card
			final String baseCardId = request.getParameter(PARAM_BASE_CARD_ID);
			if (StringUtils.hasLength(baseCardId)) {
				try {
					card = (Card) serviceBean.getById(new ObjectId(Card.class, Long.parseLong(baseCardId)));
				} catch (NumberFormatException e) {
					logger.error("Error initialize variables in DocumentPickerParameters:", e);
				} catch (DataException e) {
					logger.error("Error initialize variables in DocumentPickerParameters:", e);
				} catch (ServiceException e) {
					logger.error("Error initialize variables in DocumentPickerParameters:", e);
				}
			}
		}
		return card;
	}

	private void initSearchAction(String filterQuery) {
		action.setPage(1);
		action.setPageSize(DATA_SIZE);
		action.setName(filterQuery);

		final long[] permissionTypesArray = ContentUtils.getPermissionTypes(variantDescriptor.getRequiredPermissions());
		action.setPermissionTypes(permissionTypesArray);		
		
		if(!variantDescriptor.getRequiredPermissions().equals(Filter.CU_DONT_CHECK_PERMISSIONS)) {
			action.setCheckPermission(true);
		}
	}


	public SearchByTemplatePeriodNameAction getAction() {
		return action;
	}

	private ObjectId getFilterTemplateId(String filterTemplate)
			throws ServletException {
			try {
				final long filterTemplateId = Long.parseLong(filterTemplate);
				return new ObjectId(Template.class, filterTemplateId);
			} catch (NumberFormatException e) {
				throw new ServletException("Invalid filter template value", e );
			}
	}

	private Card getFilterCard(String filterId)
			throws ServletException {
			try {
				final long filterIdLong = Long.parseLong(filterId);
				final ObjectId filterIdObject = new ObjectId(Card.class, filterIdLong);
				return (Card)Card.createFromId(filterIdObject);
			} catch (NumberFormatException e) {
				throw new ServletException("Invalid filter id value", e );
			}
	}

	private Date getEndDate(int intFilterYear) {

		final Calendar calendar = Calendar.getInstance();
		//Clear all fields
		calendar.clear();
		calendar.set(Calendar.YEAR, intFilterYear);

		calendar.set(Calendar.MONTH, Calendar.DECEMBER);
		int lastDate = calendar.getActualMaximum(Calendar.DATE);

		calendar.set(Calendar.DATE, lastDate); 
		int lastDay = calendar.get(Calendar.DAY_OF_MONTH);

		calendar.set(Calendar.DAY_OF_MONTH, lastDay);

		return calendar.getTime();
	}

	private Date getStartDate(int filterYear) {

		final Calendar calendar = Calendar.getInstance();

		//Clear all fields
		calendar.clear();
		calendar.set(Calendar.YEAR, filterYear);
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 1);

		return  calendar.getTime();
	}

	public Collection<ObjectId> getOrganization() {
		return null;
	}

	/**
	 * @return ��� ��������, ��� �������� ���������
	 */
	public ObjectId getAttrId() {
		return attrId;
	}
}
