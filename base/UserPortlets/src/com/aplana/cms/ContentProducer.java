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
package com.aplana.cms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.api.node.PortalNode;
import org.jboss.portlet.JBossActionRequest;
import org.jboss.portlet.JBossRenderRequest;
import org.jboss.util.stream.Streams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import com.aplana.cms.cache.CacheManager;
import com.aplana.cms.cache.CachingDataServiceBean;
import com.aplana.cms.tags.ListBase;
import com.aplana.cms.tags.ListTag;
import com.aplana.cms.view_template.ColumnSortAttributes;
import com.aplana.cms.view_template.SortViewAttribute;
import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.GetCardKeyword;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.SearchWithDelegatingAccess;
import com.aplana.dbmi.card.LinkedCardUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.search.SimpleSearchPortlet;
import com.aplana.dbmi.search.workstation.WorkstationAdvancedSearchPortlet;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.UserPrincipal;
import com.aplana.dbmi.showlist.MIShowListPortlet;
import com.aplana.dbmi.util.DataObjectLazyMap;
import com.aplana.dbmi.util.IdUtils;

public class ContentProducer
{
	public static final String TAG_CONTENT = "content";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_ID = "id";
	public static final String ATTR_FIELD = "field";
	public static final String ATTR_LINK = "link";
	public static final String ATTR_ITEM = "item";
	public static final String ATTR_NEXT_ITEM = "nextItem";
	public static final String ATTR_START = "start";
	public static final String ATTR_END = "end";
	public static final String ATTR_EMPTY = "empty";
	public static final String ATTR_ERROR = "error";
	public static final String TYPE_FILE = "file";
	public static final String TYPE_TEXT = "text";
	public static final String ATTR_KEYWORD = "keyword";
	
	public static final String VAR_TODAY = "today";
	public static final String VAR_USER = "user";
	public static final String VAR_AREA = "area";
	public static final String VAR_PARENT_AREA = "parent_area";
	public static final String VAR_DOCUMENT = "document";
	public static final String VAR_NEXT_DOC = "nextdoc";
	public static final String VAR_OPENED_DOCUMENT = "opened_document";
	public static final String VAR_ERROR = "error";
	public static final String VAR_FOUND = "found";
	public static final String VAR_ROW_NUM_IN_LIST = "row_number_in_list";
	public static final String VAR_SIMPLE_SEARCH_FILTER = "simple_search_filter";

	public static final String REQ_ATTR_ITEM = "cms.item";
	public static final String REQ_ATTR_NEXT_ITEM = "cms.nextItem";
	public static final String REQ_ATTR_CURRENT_CONTENT = "cms.currentContent";
	public static final String SESS_ATTR_AREA = "cms.area";
	public static final String SESS_ATTR_ERROR = "cms.error";
	public static final String SESS_ATTR_FOUND = "cms.found";
	public static final String SESS_ATTR_SORT_COLUMN_ID = "cms.sortColumnId";
	public static final String SESS_ATTR_STRAIGHT_ORDER = "cms.straightOrder";
	public static final String SESS_ATTR_FOUND_WHOLE_SIZE = "cms.foundWholeSize";
	public static final String APP_ATTR_SEARCH = MIShowListPortlet.SEARCH_BEAN;
	public static final String CLEAR_ATTR = "clear";
	
	private static final HashSet CONTENT_ATTRIBUTES = new HashSet(Arrays.asList(new Object[] {
			ATTR_TYPE, ATTR_ID, ATTR_FIELD, ATTR_LINK, ATTR_ITEM, ATTR_START, ATTR_END, ATTR_ERROR,
			"var", "page", "area", "parentArea", "xml", "view", "htmlid", "create", "proceed", "success", "failure", "value" }));

	private static final List<String> stateNames = Arrays.asList("������", "STATE", "STATUS");
	private static final List<String> templateNames = Arrays.asList("������", "TEMPLATE");
	private static final ObjectId _STATE = Card.ATTR_STATE;
	private static final ObjectId _TEMPLATE = Card.ATTR_TEMPLATE;

	protected Log logger = LogFactory.getLog(getClass());
	private AsyncDataServiceBean service;
	private ContentRequest request;
	private ApplicationContext applicationContext;
	private ContentResponse response;
	private HashMap variables = new HashMap();
	
	private boolean keepAdvancedSearch; // true - keep advanced search bean is session, false - remove it from session

    /**
     * store cards by id
     */
    private final Map<Long, Card> cardCashe = new HashMap<Long, Card>();

	
	private ContentDataServiceFacade contentDataServiceFacade = null;
	
	public ContentProducer(ContentRequest request, ContentResponse response, ApplicationContext context) {
		this(request, response, context, true);
	}
	
	public ContentProducer(ContentRequest request, ContentResponse response, ApplicationContext context, boolean initCurrentContent) {
		this(request, response, context, initCurrentContent, true);
	}

	public ContentProducer(ContentRequest request, ContentResponse response, ApplicationContext context, boolean initCurrentContent, boolean createDefaultSiteArea) {
		this.request = request;
		this.response = response;
		//this.service = PortletUtil.createService(request);
		this.service = CachingDataServiceBean.createBean(request);
		applicationContext = context;
		
		initContentDataServiceFacade();		
		
		processRequestParameters();
		
		ObjectId nextCard = getNextCardId();
		if(nextCard!=null){
			variables.put(VAR_NEXT_DOC, nextCard.getId().toString());
		}
		
		Card siteAreaCard = getCurrentSiteArea(createDefaultSiteArea);
		if (siteAreaCard != null){
			variables.put(VAR_AREA, siteAreaCard.getId().getId().toString());

			CardLinkAttribute parentAreaAttribute = siteAreaCard.getCardLinkAttributeById(ContentIds.ATTR_PARENT);
			ObjectId parentAreaId = null != parentAreaAttribute ? parentAreaAttribute.getSingleLinkedId() : null;
			if(null != parentAreaId) {
				variables.put(VAR_PARENT_AREA, parentAreaId.getId().toString());
			}
			/*
			String keyword = getCardKeyword(getCurrentSiteArea().getId());
            if (keyword != null) {
                variables.put(ATTR_KEYWORD, keyword);
            }
            */
		}

        if(initCurrentContent) {
            Card currentContent = getCurrentContent(true);
            if (currentContent != null) {
                variables.put(VAR_DOCUMENT, currentContent.getId().getId().toString());

                if(null != request.getAttribute(REQ_ATTR_ITEM)) {
                    variables.put(VAR_OPENED_DOCUMENT, currentContent.getId().getId().toString());
                }
            }
        }

		variables.put(VAR_TODAY, new java.util.Date());
		if (request.getUserPrincipal() != null)
			variables.put(VAR_USER, service.getPerson().getFullName().trim());
		
		variables.put(VAR_ERROR, new VarError());
		variables.put(VAR_FOUND, new VarFound());
	}
	
	private void processRequestParameters()
	{
		String param = request.getParameter(ContentRequest.PARAM_AREA);
		if (param != null && param.length() > 0) {
			request.setSessionAttribute(SESS_ATTR_AREA, param);	//***** global
			//request.removeSessionAttribute(ContentRequest.PARAM_ITEM);	//***** global
			
			// Clear simple search filter when switching folder
			request.removeSessionAttribute(SimpleSearchPortlet.APP_ATTR_SEARCH_BEAN);

			// Reset selected page when switching folder
			request.removeSessionAttribute(PagedList.ATTR_PAGE_CURRENT);
		}
		param = request.getParameter(ContentRequest.PARAM_ITEM);
		if (param != null && param.length() > 0)
			request.setAttribute(REQ_ATTR_ITEM, param);	//***** global
		
		param = request.getParameter(ContentRequest.PARAM_RESET_CACHE);
		if (param != null) {
			CacheManager.resetCache();
		}
		
		param = request.getParameter(ContentRequest.PARAM_NEXT_ITEM);
		if(param!=null){
			request.setAttribute(REQ_ATTR_NEXT_ITEM, param);
		}
		
		param = request.getParameter(WorkstationAdvancedSearchPortlet.KEEP_SEARCH_ALIVE);
		if(param != null) {
			this.keepAdvancedSearch = Boolean.parseBoolean(param);
		}
	}
	
	
	protected  void initContentDataServiceFacade() {
		
		contentDataServiceFacade = (ContentDataServiceFacade)applicationContext.getBean("contentDataServiceFacade");
		
		contentDataServiceFacade.setContetProducer(this);
		
		
	}

	public PagedListInterface getPagedList() {		
		return (PagedListInterface) applicationContext.getBean("pagedList");
	}

	public ContentDataServiceFacade getContentDataServiceFacade() {
		return contentDataServiceFacade;
	}

	public Card getCardById(ObjectId id) throws DataException, ServiceException {
		
		return (Card) service.getById(id);
		
	}
	
	public void writeDocListColumnsInfo(ObjectId folderId, PrintWriter out) {
        List<ColumnSortAttributes> columns = contentDataServiceFacade.getFolderSortColumns(folderId);
        if(null == columns) {
            return;
        }
        
        try {
            JSONArray columnsJson = new JSONArray();

            for(ColumnSortAttributes column : columns) {
                JSONObject columnObject = new JSONObject();
                boolean isAsc = true; // By default
                String sortAttrCode = ""; // By default
                if(null != column.getSortAttributes() && column.getSortAttributes().size() > 0) {
                    SortViewAttribute sortViewAttribute = column.getSortAttributes().get(0);
                    isAsc = sortViewAttribute.isAsc();
                    if (sortViewAttribute.getAttribute() != null) {
                    	sortAttrCode = sortViewAttribute.getAttribute().getCode();
                    }
                }
                
                columnObject.put("asc", isAsc);
                columnObject.put("sortAttrCode", sortAttrCode);
                columnObject.put("columnId", column.getColumnId());
                columnsJson.put(columnObject);
            }

            out.write("<script type=\"text/javascript\">\n");
            out.write(" var columnsSortData = ");
            columnsJson.write(out);
            out.write(";\n");
            out.write("</script>");
        } catch (JSONException e) {
            logger.error("Error occured when populating JSON array with columns sort data", e);
        }
    }
	
	public void writeContent(PrintWriter out, String html, Card item)
	{
		if (html == null)
			return;
		try {
			protectCallEnter(out);

			ArrayList commentBlockList = new ArrayList();
			Matcher commentStartMatcher = Pattern.compile("<!--", Pattern.CASE_INSENSITIVE).matcher(html);
			Matcher commentEndMatcher = Pattern.compile("-->", Pattern.CASE_INSENSITIVE).matcher(html);
			int matcherPos = 0;
			while (commentStartMatcher.find(matcherPos)) {
				matcherPos = commentStartMatcher.start();
				if (commentEndMatcher.find(matcherPos)) {
					Integer[] commentBlock = new Integer[] {matcherPos, commentEndMatcher.end()};
					matcherPos = commentEndMatcher.end();
					commentBlockList.add(commentBlock);
				}
				else {
					Integer[] commentBlock = new Integer[] {matcherPos, html.length()};
					matcherPos = html.length();
					commentBlockList.add(commentBlock);
				}
			}
			
			Matcher tagStart = Pattern.compile("<" + TAG_CONTENT, Pattern.CASE_INSENSITIVE).matcher(html);
			int start = 0;
			ObjectId areaId = new ObjectId(Card.class, getVariable(ListBase.ATTR_AREA));
			Card view = (Card)getVariable(ListTag.VAR_VIEW);
			ObjectId viewId = view == null ? null : view.getId();  
			
			while (true) {
				if (!tagStart.find(start))
					break;
				
				boolean isTagCommented = false;
				for (int i = 0; i < commentBlockList.size(); i++) {
					Integer[] commentBlock = (Integer[])commentBlockList.get(i);
					if (!isTagCommented && tagStart.start() >= commentBlock[0] && tagStart.start() < commentBlock[1]) {
						out.write(html, start, commentBlock[1] - start);
						start = commentBlock[1];
						isTagCommented = true;
					}
				}
				if (isTagCommented) {
					break;
				}
				
				out.write(html, start, tagStart.start() - start);
				start = tagStart.start();
				Tag tag = new Tag();
				try {
					start = parseTag(html, start, tag);
				} catch (ParseException e) {
					logger.error("Error parsing content tag", e);
					out.write(html.charAt(start++));
					continue;
				}
				Card card = item;
				String type = TYPE_FILE;
				if (tag.hasAttribute(ATTR_ID)) {
					try {
						
						long id = Long.parseLong(expandContent(tag.getAttribute(ATTR_ID), card));
                        if (id != (Long)card.getId().getId()){
                            if (cardCashe.containsKey(id)){
                                card = cardCashe.get(id);
                            } else {
                                // read only if another card needed
						        card = contentDataServiceFacade.getDataCardById(new ObjectId(Card.class, id));
                                cardCashe.put(id, card);
                            }
                        }
						
					} catch (NumberFormatException e) {
						logger.error("Wrong item id: " + tag.getAttribute(ATTR_ID));
						continue;
					} catch (Exception e) {
						logger.error("Error fetching content item " + tag.getAttribute(ATTR_ID), e);
						continue;
					}
				}
				if (tag.hasAttribute(ATTR_LINK)) {
				
					LinkAttribute attr = 
						contentDataServiceFacade.getCardLinkAttribute(tag.getAttribute(ATTR_FIELD), 
								tag.getAttribute(ATTR_LINK), card);
					
					if (attr == null) {
						continue;
					}
					
					if(tag.hasAttribute(ATTR_FIELD) && attr.getLabelAttrId() != null) {
						type = TYPE_TEXT;
						processTag(tag, type, card, out);
						continue;
					}
					
					Card found = findSingleLinkedCard(tag, card, attr);
					
					if (found == null) {
						continue;
					}
					
					try {
						
						card = contentDataServiceFacade.getLinkedCardByFound(areaId, viewId, found);
						
					} catch (DataException e) {
                        if (e instanceof DataException && "general.access".equals(((DataException) e).getMessageID())) {
                            logger.error("Error retrieving card " + found.getId().getId() + ": " + e.getMessage());
                        } else {
                            logger.error("Error retrieving card " + found.getId().getId(), e);
                        }
                        continue;
					}
                    
				}
				if (tag.hasAttribute(ATTR_FIELD))
					type = TYPE_TEXT;
				if (tag.hasAttribute(ATTR_TYPE))
					type = tag.getAttribute(ATTR_TYPE);
				processTag(tag, type, card, out);
			}
			out.write(html, start, html.length() - start);
		} catch (Exception e) {
			logger.error("Error writing content", e);
		} finally {
			protectCallLeave(out);
		}
	}

	public LinkAttribute getCardLinkAttribute(String attrCode, Card card) {
		Attribute result = ContentUtils.getAttribute(card, expandContent(attrCode, card));
		
		if(result instanceof LinkAttribute) {
			return (LinkAttribute) result;
		} else {
			return null;
		}
	}
	
	public Card getLinkedCardByFound(Card found) throws ServiceException, DataException  {
		
		return  (Card)service.getById(found.getId());
		
	}
	
	public String expandContent(String html, Card item)
	{
		StringWriter buffer = new StringWriter();
		PrintWriter writer = new PrintWriter(buffer);
		writeContent(writer, html, item);
		writer.flush();
		return buffer.toString();
	}

	public void writeHtmlAttributes(PrintWriter out, Tag tag, Card item) {
		for (Iterator itr = tag.getAttributes().entrySet().iterator(); itr.hasNext(); ) {
			Map.Entry attr = (Map.Entry) itr.next();
			if (!CONTENT_ATTRIBUTES.contains(attr.getKey())) {
				String value = (String) attr.getValue();
				out.write(" " + attr.getKey() + "=" + (value.contains("'") ? "\"" : "'"));
				writeContent(out, value, item);
				out.write(value.contains("'") ? "\"" : "'");
			}
		}
	}

	public AsyncDataServiceBean getService() {
	    if (request.getSessionAttribute(DataServiceBean.USER_NAME) != null) {
	        service.setUser(new UserPrincipal((String) request.getSessionAttribute(DataServiceBean.USER_NAME)));
	        service.setIsDelegation(true);
	        service.setRealUser(request.getUserPrincipal());
	    } else {
	        service.setUser(request.getUserPrincipal());
	        service.setIsDelegation(false);
	    }
		return service;
	}

	public ContentRequest getRequest() {
		return request;
	}

	public ContentResponse getResponse() {
		return response;
	}
	
	
	public ObjectId findAreaIdByPortalPageName(String pageId, String navigator) throws DataException, ServiceException {
		
		Search search = new Search();
		search.setByAttributes(true);
		if (pageId != null && pageId.length() > 0) {
			search.addStringAttribute(ContentIds.ATTR_PAGEID);
			search.setWords(pageId);
		}
		
		if(null != navigator) {
			search.addStringAttribute(ContentIds.ATTR_NAVIGATOR, navigator, Search.TextSearchConfigValue.EXACT_MATCH);
		}
		
		search.setTemplates(new ArrayList());
		search.getTemplates().add(Template.createFromId(ContentIds.TPL_AREA));
		search.getFilter().setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
	
		SearchResult result = (SearchResult) service.doAction(search);
		
		if (result.getCards().size() < 1) {
			logger.error("No area object defined for page " + pageId);
			return null;
		}
		
		ObjectId areaId = ((Card) result.getCards().iterator().next()).getId();
		if (result.getCards().size() > 1)
			logger.warn("Multiple area objects found for page " + pageId +
					"; " + areaId.getId() + " will be used");
		
		return areaId;
		
	}
	
	public Card getSiteArea(ObjectId areaId) throws DataException, ServiceException {
		
		return (Card)service.getById(areaId);
		
	}
	
	/**
	 * Returns configuration view card 
	 * @param id configuration view identifier
	 */
	public Card getViewCardById(ObjectId id) throws DataException, ServiceException {
		
		return (Card)service.getById(id);
		
	}
	
	/**
	 * Returns area documents by given area identifier 
	 * @param areaId area identifier
	 */
	public List listAreaDocuments(ObjectId areaId) throws Exception
	{
		//try {
			Search search = new Search();
			search.setByAttributes(true);
			search.setWords("");
			search.setTemplates(new ArrayList());
			search.addCardLinkAttribute(ContentIds.ATTR_AREA, areaId);
			search.addDateAttribute(ContentIds.ATTR_PUBLICATION_DATE, new Date(Long.MIN_VALUE), new Date());
			search.addDateAttribute(ContentIds.ATTR_EXPIRATION_DATE, new Date(), new Date(Long.MAX_VALUE));
			search.setColumns(new ArrayList());
			SearchResult.Column column = new SearchResult.Column();
			column.setAttributeId(new ObjectId(ListAttribute.class, "_TEMPLATE"));
			search.getColumns().add(column);
			column = new SearchResult.Column();
			column.setAttributeId(ContentIds.ATTR_PUBLICATION_DATE);
			column.setSorting(SearchResult.Column.SORT_DESCENGING);
			search.getColumns().add(column);
			//SearchResult result = (SearchResult) service.doAction(search);
			return (List) searchCards(search);
		/*} catch (Exception e) {
			logger.error("Error searching site area's documents", e);
			return null;
		}*/
	}
	
	public List searchCards(Search search) throws Exception
	{
		search.getFilter().setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
		//try {
			return ContentUtils.extendSearchResult((SearchResult) service.doAction(search));
		/*} catch (Exception e) {
			logger.error("Error retrieving documents", e);
			return null;
		}*/
	}
	
	
	public ObjectId getNextCardId(){
		String id = (String) request.getAttribute(REQ_ATTR_NEXT_ITEM);	
		ObjectId cardId = null;			
		if (id != null && !"null".equals(id) && id.length() > 0) {
			try {
				cardId = new ObjectId(Card.class, Long.parseLong(id));
			} catch (Exception e) {
				logger.error("Error parsing next ID", e);
			}
		}
		return cardId;
	}
	
	public Card getCurrentSiteArea(boolean createDefault) {
		try {
			String area = (String) request.getSessionAttribute(SESS_ATTR_AREA);	//***** global
			if(area == null && !createDefault)
				return null;
			ObjectId areaId = null;			
			if (area != null && area.length() > 0) {
				try {
					areaId = new ObjectId(Card.class, Long.parseLong(area));
				} catch (Exception e) {
					logger.error("Error parsing area ID", e);
				}
			}	
			String pageId = getPageId(request);
			if("/boss/folder".equals(pageId)) {
				String currentNavigatorName = (String) request.getSessionAttribute(NavigationPortlet.ATTR_CURRENT_NAVIGATOR_NAME);
				String newNavigatorName = (String) request.getSessionAttribute(NavigationPortlet.ATTR_NEW_NAVIGATOR_NAME);		
				
				if(null != newNavigatorName && !newNavigatorName.equals(currentNavigatorName)) {
					areaId = contentDataServiceFacade.findAreaIdByPageId(pageId, newNavigatorName);

					// Reset selected page when switching folder
					request.removeSessionAttribute(PagedList.ATTR_PAGE_CURRENT);
					//NavigatorConfig navigator = (NavigatorConfig) AppContext.getApplicationContext().getBean(newNavigatorName);
					//area = navigator.getArea();
				} else if(null == area || area.length() == 0) { 
					String navigatorName = null != currentNavigatorName ? currentNavigatorName : NavigationPortlet.DEFAULT_NAVIGATOR;
					areaId = contentDataServiceFacade.findAreaIdByPageId(pageId, navigatorName);
					//NavigatorConfig navigator = (NavigatorConfig) AppContext.getApplicationContext().getBean(navigatorName);
					//area = navigator.getArea();
				}
			}
			
			if (areaId == null) {
				areaId = contentDataServiceFacade.findAreaIdByPageId(pageId, null); 	
				request.setSessionAttribute(SESS_ATTR_AREA, areaId.getId().toString());	//***** global
			}
			Card card = contentDataServiceFacade.getSiteArea(areaId);
			
			while (true) {
				CardLinkAttribute attr = (CardLinkAttribute) card.getAttributeById(
						ContentIds.ATTR_DEFAULT_ITEM);
				if (attr.getLinkedCount() > 0) {
					
					Card def = contentDataServiceFacade.getViewCardById(attr.getSingleLinkedId());
					
					if (ContentIds.TPL_AREA.equals(def.getTemplate())) {
						card = def;
						request.setSessionAttribute(SESS_ATTR_AREA, card.getId().getId().toString());	//***** global
						continue;
					}
				}
				request.setSessionAttribute(SESS_ATTR_AREA, card.getId().getId().toString());
				return card;
			}
		} catch (IllegalStateException ise) {
			logger.warn("Site area object not found for this page", ise);
			request.removeSessionAttribute(SESS_ATTR_AREA);
			return null;
		} catch (Exception e) {
			logger.error("Site area object not found for this page", e);
			request.removeSessionAttribute(SESS_ATTR_AREA);
			return null;
		}
	}
	
	public Card getCurrentSiteArea() {
		return getCurrentSiteArea(true);
	}

	
	public Card getContent(ObjectId contentId) throws DataException, ServiceException {
		
		return (Card) service.getById(contentId);
		
	}
	public String getCardKeyword(ObjectId cardId) {
	    try {
    	    GetCardKeyword action = new GetCardKeyword();
    	    action.setCardId(cardId);
            String keyword = (String) service.doAction(action);
            return keyword;
	    } catch (Exception e) {
	        logger.error("error getting keyword for card" + cardId.getId());
	        return null;
	    }
	}

	public Card getCurrentContent(boolean idOnly)
	{
		/*Card doc = (Card) request.getAttribute(REQ_ATTR_ITEM);
		if (doc != null) {
			return doc;
		}*/
		//String item = (String) request.getSessionAttribute(ContentPortlet.PARAM_ITEM);	//***** global
		String item = (String) request.getAttribute(REQ_ATTR_ITEM);		//***** local
		
		if (null == item) {
			item = (String) request.getAttribute(REQ_ATTR_CURRENT_CONTENT);
		}
		
		if (null == item) {
			Card doc = getDefaultContent(getCurrentSiteArea());
			request.setAttribute(REQ_ATTR_CURRENT_CONTENT, doc != null ? doc.getId().getId().toString() : "");
			return doc;
		} else if (item.length() == 0)
			return null;
		else {
			try {
	            ObjectId itemId = new ObjectId(Card.class, new Long(item));
	            Card card;
	            if (idOnly) {
	                card = new Card();
	                card.setId(itemId);
	            } else {
	                card = contentDataServiceFacade.getContent(itemId);
	            }
	            return card;
	        } catch (Exception e) {
	            logger.error("Error retrieving current content item", e);
	            return null;
	        }
        }
	}

	public Card getDefaultContentBySearchAttribute(TextAttribute attrSearch) throws UnsupportedEncodingException, DataException, ServiceException {
		
		Search search = contentDataServiceFacade.initFromXml(attrSearch.getValue());
		search.getFilter().setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
		
		ObjectId areaId = new ObjectId(Card.class, getVariable(ListBase.ATTR_AREA));
		String sortColumnId = (String) request.getSessionAttribute(ContentProducer.SESS_ATTR_SORT_COLUMN_ID);
		Boolean straightOrder = (Boolean) request.getSessionAttribute(ContentProducer.SESS_ATTR_STRAIGHT_ORDER);
		String simpleSearchFilter = (String) getVariable(ContentProducer.VAR_SIMPLE_SEARCH_FILTER);
		
		return contentDataServiceFacade.getDefaultContentBySearch(search, areaId, simpleSearchFilter, sortColumnId, straightOrder);
		
		
	}
	
	public Search initFromXml(String xml) throws DataException , UnsupportedEncodingException  {
		
		Search search = new Search();
		search.initFromXml(new ByteArrayInputStream(xml.getBytes("UTF-8")));
		
		return search;
	}

	public Card getDefaultContentBySearch(Search search)
			throws DataException, ServiceException {
		
		Collection found = ((SearchResult) service.doAction(search)).getCards();
		if (found.size() == 0) {
			return null;
		}
		
		return  ((Card) found.iterator().next());
	}
	
	public Card getDefaultContent(Card area)
	{
		try {
			ObjectId contentId = null;
			CardLinkAttribute attr = (CardLinkAttribute) area.getAttributeById(ContentIds.ATTR_DEFAULT_ITEM);
			if (attr.getLinkedCount() < 1) {
				ListAttribute attrType = (ListAttribute) area.getAttributeById(ContentIds.ATTR_AUTO_ITEM);
				if (attrType.getValue() == null) {
					// check whether it is the advanced search case
					Card contentCard = getAdvancedSearchDefaultContent();
					if (contentCard != null) {
						return contentCard;
					}
					
					// otherwise
					logger.warn("No default content for area " + area.getId().getId() + " defined");
					return null;
				}
				if (ContentIds.VAL_AUTO_LINKED.equals(attrType.getValue().getId())) {
					Collection<Card> linkedCards = contentDataServiceFacade.getLinkedCards(area, (BackLinkAttribute)area.getAttributeById(ContentIds.ATTR_LINKED), null);
					List docs = (List)linkedCards;
					if (docs == null || docs.size() == 0) {
						logger.warn("Area " + area.getId().getId() + " has no linked content");
						return null;
					}
					contentId = ((Card) docs.iterator().next()).getId();
				} else if (ContentIds.VAL_AUTO_SEARCH.equals(attrType.getValue().getId())) {
					TextAttribute attrSearch = (TextAttribute) area.getAttributeById(ContentIds.ATTR_SEARCH);
					if (attrSearch == null || attrSearch.getValue() == null || attrSearch.getValue().length() == 0) {
						logger.error("No search for area " + area.getId().getId() + " defined");
						return null;
					}
					
					Card contentCard =  getDefaultContentBySearchAttribute(attrSearch);
					if (contentCard == null) {
						logger.warn("No documents found in area " + area.getId().getId());
						return null;
					}
					return contentCard;
						
					
				}
			}
			else {
				contentId = attr.getSingleLinkedId();
				if (attr.getLinkedCount() > 1)
					logger.warn("Multiple default content items defined for area " + area.getId().getId());
			}
			
			return getContent(contentId);
			
		} catch (Exception e) {
			logger.error("Error retrieving default content for area " + 
					( (area == null || area.getId() == null) 
							? null 
							: area.getId().getId()
					)
				);
			return null;
		}
	}
	
	public Card getAdvancedSearchDefaultContent()  throws DataException, ServiceException {
		Card res = null;
		Search sessionSearch = (Search) request.getSessionAttribute(WorkstationAdvancedSearchPortlet.ADVANCED_SEARCH_BEAN);
		String clearAttr = (String) request.getSessionAttribute(CLEAR_ATTR);
		request.removeSessionAttribute(CLEAR_ATTR);
		if ("true".equals(clearAttr))
			sessionSearch = null;
		if (sessionSearch != null) {
			Search search = sessionSearch.makeCopy();
			if (search.getFilter() == null) {
				search.clearFilter();
			}
			search.getFilter().setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
			// with pageSize == 1 in filter query returns wrong card, 
			// therefore full search should be performed 
			res = getDefaultContentBySearch(search);
		}
		return res;
	}
	
	public Card getDefaultContentBySearch222(Search search) throws DataException, ServiceException {

		Collection found = ((SearchResult) service.doAction(search)).getCards();
		if (found.size() == 0) {
			return null;
		}

		return ((Card) found.iterator().next());
	}
	
	public String getMaterial(ObjectId cardId)
	{
		Material file = getMaterialObject(cardId);
		if (file == null) {
			return null;
		}
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream(file.getLength());
			Streams.copy(file.getData(), buffer);
			return buffer.toString("UTF-8");
		} catch (Exception e) {
			logger.error("Error retrieving material " + cardId.getId(), e);
			return null;
		}
	}
	
	public Material getMaterialObject(ObjectId cardId)
	{
		try {
			if (cardId == null || !Card.class.equals(cardId.getType()))
				throw new IllegalArgumentException("Not a card id");
			DownloadFile download = new DownloadFile();
			download.setCardId(cardId);
			return (Material) service.doAction(download);
		} catch (Exception e) {
			logger.error("Error retrieving material " + cardId.getId(), e);
			return null;
		}
	}
	
	public List getLinkedCards(Card card, Attribute attr)
	{
		return getLinkedCards(card, attr, null);
	}
	
	
	public Collection<Card> getLinkedCards(LinkAttribute attr, Search filter) throws DataException, ServiceException {
		
		Search search = null;
		if (filter != null) {
			search = filter.makeCopy();
		}
		else {
			search = new Search();
			search.setByAttributes(false);
			search.setNameEn(CachingDataServiceBean.SEARCH_CACHE_PREFIX + attr.getId().getId());
			search.getFilter().setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
		}
		search.setByCode(true);
		search.setWords(attr.getLinkedIds());
		
		
		if(attr instanceof CardLinkAttribute) {
			CardLinkAttribute links = (CardLinkAttribute) attr;
		if (links.getFilter() != null && links.getFilter().getColumns() != null)
			search.setColumns(links.getFilter().getColumns());
		}

		LinkedCardUtils.ensureLabelAttr((LinkAttribute) attr, search);

		// TODO ����� ���������� ���������� �������� ������������ ������ � ������.
		SearchResult res = (SearchResult) service.doAction(new SearchWithDelegatingAccess(search));

		return ContentUtils.extendSearchResult(res);
	}
	
	
	/*public Collection<Card> getLinkedCards(Card card, BackLinkAttribute attr) throws DataException, ServiceException {
		
		ListProject search = new ListProject();
		
		search.setAttribute(attr.getId());
		search.setCard(card.getId());
		
		return ContentUtils.extendSearchResult((SearchResult) service.doAction(search));
		
	}*/
		
	public Collection<Card> getLinkedCards(Card card, TypedCardLinkAttribute attr) throws DataException, ServiceException {
		Search search = new Search();
		search.setByCode(true);
		search.setByAttributes(false);
		search.setWords(attr.getLinkedIds());
		search.setNameEn(CachingDataServiceBean.SEARCH_CACHE_PREFIX + attr.getId().getId());
		search.getFilter().setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);

		// >>> (2010/03, RuSA)
		LinkedCardUtils.ensureLabelAttr(attr, search);
		final SearchResult res = service.doAction(search);
		//if (res != null)
		//	links.setIdsLinked(res.getCards());
		// <<< (2010/03, RuSA)

		List<Card> result = ContentUtils.sortCardsById(ContentUtils.extendSearchResult(res),
				attr.getIdsLinked());
		
		return result;
		
	}
	
	public <T extends DataObject> Collection<T> getChildren(ObjectId id, Class<T> type) throws DataException, ServiceException {
		
		return getService().listChildren(id, type);
		
	}
	
	
	public Collection<Card> getLinkedCards(PersonAttribute attr, Search filter) throws DataException, ServiceException {
		
		StringBuilder ids = new StringBuilder();
		
		for (Iterator<Person> itr = ((PersonAttribute) attr).getValues().iterator(); itr.hasNext(); ) {
			Person person = itr.next();
			
			if (person.getCardId() == null) {
				logger.warn("Person " + person.getFullName() + " doesn't have associated card");
				continue;
			}
			if (ids.length() != 0)
				ids.append(",");
			ids.append(person.getCardId().getId());
		}
		
		if (ids.length() == 0)
			return Collections.emptyList();
		
		Search search = new Search();
		if (filter != null)
			search = filter.makeCopy();
		search.setByCode(true);
		search.setWords(ids.toString());
		search.setNameEn(CachingDataServiceBean.SEARCH_CACHE_PREFIX + attr.getId().getId());
		search.getFilter().setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
		SearchResult searchResult = (SearchResult) service.doAction(search);
		
		List<Card> persons = ContentUtils.extendSearchResult(searchResult);
		
		return ContentUtils.sortCardsById(persons,
				ObjectIdUtils.commaDelimitedStringToNumericIds(search.getWords(), Card.class));
		
	}
	
	public List<Card> getLinkedCards(Card card, Attribute attr, Search filter)
	{
        Card view = (Card)getVariable(ListTag.VAR_VIEW);
		ObjectId viewId = view == null ? null : view.getId();

        return getLinkedCards(card, attr, filter, viewId);
    }

    public List<Card> getLinkedCards(Card card, Attribute attr, Search filter, ObjectId viewId) {
        try {
            if (Attribute.TYPE_CARD_LINK.equals(attr.getType())
            		|| Attribute.TYPE_BACK_LINK.equals(attr.getType())) {

                LinkAttribute links = (LinkAttribute) attr;

                ObjectId areaId = new ObjectId(Card.class, getVariable(ListBase.ATTR_AREA));

                List linkedCards = (List)contentDataServiceFacade.getLinkedCards(card, links, filter, areaId, viewId);

                return ContentUtils.sortCardsById(linkedCards, links.getIdsLinked());
            }
            /*if (Attribute.TYPE_BACK_LINK.equals(attr.getType())) {
                if (filter != null)
                    throw new UnsupportedOperationException("Not implemented yet");

                List linkedCards = (List)contentDataServiceFacade.getLinkedCards(card, (BackLinkAttribute) attr, viewId);

                return linkedCards;
            }*/
            if (Attribute.TYPE_TYPED_CARD_LINK.equals(attr.getType())) {
                if (filter != null)
                    throw new UnsupportedOperationException("Not implemented yet");

                TypedCardLinkAttribute links = (TypedCardLinkAttribute) attr;
                List result = null;

                if (links.getLinkedCount() > 0) {

                    result = (List)contentDataServiceFacade.getLinkedCards(card, (TypedCardLinkAttribute)links, viewId);

                    Collection<ReferenceValue> chidren = contentDataServiceFacade.getChildren(links.getReference(), ReferenceValue.class);

                    Map values = new DataObjectLazyMap<ReferenceValue>(chidren);
                    for (Iterator itr = result.iterator(); itr.hasNext(); ) {
                        Card item = (Card) itr.next();
                        // (YNikitin, 2011/04/03) ��� ������� ���� ����� ������ �� ������
                        if (links.getTypes().get(item.getId().getId())==null)
                            continue;
                        ObjectId valueId = new ObjectId(ReferenceValue.class,
                                links.getTypes().get(item.getId().getId()));
                        ReferenceValue value = (ReferenceValue) values.get(valueId);
                        item.getAttributes().add(new ContentUtils.LinkTypeAttribute(value));
                        if (links.getLabelAttrId() != null)
                            links.addLabelLinkedCard(item); // (2010/03, RuSA)
                    }
                }
                return result;
            }
            if (Attribute.TYPE_PERSON.equals(attr.getType())) {

                return  (List)contentDataServiceFacade.getLinkedCards((PersonAttribute) attr, filter, viewId );
            }
            throw new IllegalArgumentException("Attribute doesn't contain linked cards: " + attr.getId().getId());
        } catch (Exception e) {
            logger.error("Error retrieving cards linked to card " + card.getId().getId() +
                    " through an attribute " + attr.getId().getId(), e);
            return Collections.emptyList();
        }
    }

    public Object getVariable(String name)
	{
		Object var = variables.get(name.toLowerCase());
		if (var instanceof Variable)
			return ((Variable) var).getValue();
		return var;
	}
	
	public void setVariable(String name, Object value)
	{
		variables.put(name.toLowerCase(), value);
	}
	
	public void clearVariable(String name)
	{
		variables.remove(name.toLowerCase());
	}
	
	public Object replaceVariable(String name, Object value)
	{
		Object var = variables.get(name.toLowerCase());
		if (value != null)
			variables.put(name.toLowerCase(), value);
		else
			variables.remove(name.toLowerCase());
		return var;
	}
	
	public void setError(String error)
	{
		request.setSessionAttribute(SESS_ATTR_ERROR, error);	//***** local
	}

	public int parseTag(String html, int start, Tag tag) throws ParseException
	{
		int pos;
		if (html.charAt(start++) != '<')
			throw new ParseException("Not a tag", --start);
		for (pos = start; Character.isLetter(html.charAt(pos)) || html.charAt(pos) == '-'; pos++)
			;
		tag.setName(html.substring(start, pos).toLowerCase());
		start = pos;
		boolean hasContent = true;
		while (true) {
			while (Character.isWhitespace(html.charAt(start)))
				start++;
			if (html.charAt(start) == '>')
				break;
			if (html.charAt(start) == '/') {
				if (html.charAt(++start) != '>')
					throw new ParseException("Expected > after /", start);
				hasContent = false;
				break;
			}
			for (pos = start; Character.isLetter(html.charAt(pos)) || html.charAt(pos) == '-'; pos++)
				;
			String attr = html.substring(start, pos).toLowerCase();
			while (Character.isWhitespace(html.charAt(start)))
				start++;
			start = pos;
			if (html.charAt(start++) != '=')
				throw new ParseException("Unexpected character '" + html.charAt(--start) +
						"' in tag " + tag.getName(), start);
			while (Character.isWhitespace(html.charAt(start)))
				start++;
			char quote = 0;
			if (html.charAt(start) == '\'' || html.charAt(start) == '\"')
				quote = html.charAt(start++);
			for (pos = start; quote == 0 ? Character.isLetter(html.charAt(pos)) : html.charAt(pos) != quote; pos++)
				;
			tag.addAttribute(attr, html.substring(start, pos));
			if (quote != 0)
				pos++;
			start = pos;
		}
		start++;
		if (hasContent) {
			Matcher closing = Pattern.compile("<\\/" + tag.getName() + "\\s*>", Pattern.CASE_INSENSITIVE).matcher(html);
			boolean found = closing.find(start);
			pos = start;
			while (found) {
				Matcher nested = Pattern.compile("<" + tag.getName() + "([^>'\"\\/]|('[^']*')|(\"[^\"]*\"))*>",
						Pattern.CASE_INSENSITIVE).matcher(html.substring(pos, closing.start()));
				if (!nested.find())
					break;		// No nested tag - closing OK
				pos += nested.end();
				found = closing.find();
			}
			if (!found)
				throw new ParseException("No closing tag found for " + tag.getName(), start);
			tag.setContent(html.substring(start, closing.start()));
			start = closing.end();
		}
		return start;
	}
	
	public Card getContentPresentationCard(Card content, Card area, String viewIds) {
		return getContentPresentationCard(content, area, viewIds, false);
	}

	/**
	 * Gets presentation card
	 * @param content
	 * @param area
	 * @param viewIds
	 * @param strict
	 * 	true - means no filtration by current content card is needed, retrieve the first found view card in provided viewIds list
	 *  false - using filtration by current content card template
	 * @return
	 */
    public Card getContentPresentationCard(Card content, Card area, String viewIds, boolean strict) {
        Card view = null;

        try {
            Collection<Card> defaultViews = contentDataServiceFacade.getDefaultViews(viewIds);
            if(strict && defaultViews != null && !defaultViews.isEmpty())
            	return defaultViews.iterator().next();
            
            if (defaultViews != null) {
                view = findContentPresentation(content, defaultViews);
            }

            if (view == null) {
                while (area != null) {
                    CardLinkAttribute views = (CardLinkAttribute) area.getAttributeById(ContentIds.ATTR_VIEW_LIST);
                    if (views == null || views.getIdsLinked() == null) {
                        logger.error("No presentations defined for area " + area.getId().getId());
                        return null;
                    }
                    view = findContentPresentation(content, getLinkedCards(area, views));
                    if (view != null)
                        break;
                    ObjectId parentId = ContentUtils.getParentAreaId(area);
                    if (parentId == null)
                        break;
                    area = contentDataServiceFacade.getSiteArea(parentId);
                }
            }
            if (view == null) {
                logger.error("No presentation defined for template " + content.getTemplate().getId() +
                        " in area " + ((area == null) ? " null" : area.getId().getId()));
                return null;
            }
            return view;
        } catch (DataException e) {
            logger.error("Error retrieving content presentation for template " + content.getTemplate().getId() +
                    " in area " + ((area == null) ? " null" : area.getId().getId()), e);
			return null;
        } catch (ServiceException e) {
            logger.error("Error retrieving content presentation for template " + content.getTemplate().getId() +
                    " in area " + ((area == null) ? " null" : area.getId().getId()), e);
			return null;
        }
    }
	
	public String getContentPresentation(Card aView)
	{
        try {
            Card view = contentDataServiceFacade.getViewCardById(aView.getId());
            return ((TextAttribute) view.getAttributeById(ContentIds.ATTR_HTML)).getValue();
        } catch (DataException e) {
            logger.error("Error retrieving content presentation for view card " + aView.getId(), e);
			return null;
        } catch (ServiceException e) {
            logger.error("Error retrieving content presentation for view card " + aView.getId(), e);
			return null;
        }
    }
	
	public Collection<Card> getDefaultViews(String viewIds) {
		if (viewIds == null || viewIds.trim().length() == 0) {
			return null;
		}
		
		try {
			Search search = new Search();
			search.setByCode(true);
			search.setWords(viewIds);
			//add cache by given viewIds
			search.setNameEn(CachingDataServiceBean.SEARCH_CACHE_PREFIX + ContentIds.ATTR_VIEW_LIST.getId().toString());
			search.setColumns(new ArrayList(1));
			SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(ContentIds.ATTR_TEMPLATEID);
			search.getColumns().add(col);
			search.getFilter().setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
			SearchResult result = (SearchResult) service.doAction(search);
			return result.getCards();
		} catch (Exception e) {
			logger.error("Error retrieving default content presentations", e);
			return null;
		}
	}
	
	private Card findContentPresentation(Card content, Collection<Card> views) {
        Collection<Template> templates = contentDataServiceFacade.allTemplates();

		for (Iterator<Card> itr = views.iterator(); itr.hasNext(); ) {
			Card item = itr.next();
			final StringAttribute name = (StringAttribute) item.getAttributeById(ContentIds.ATTR_TEMPLATEID);
			if (name == null || name.getValue() == null)
				continue;
			String id = name.getValue().trim();
            ObjectId templateId = findTemplate(id, templates);
			if (id.equals(content.getTemplate().getId().toString()) ||
					id.equalsIgnoreCase(content.getTemplateNameRu()) ||
					id.equalsIgnoreCase(content.getTemplateNameEn()) ||
                    (templateId != null && templateId.equals(content.getTemplate()))
                    ) {
				return item;
			}
		}
		return null;
	}
	
	private void processTag(Tag tag, String type, Card card, PrintWriter out) {
		TagProcessor processor = TagFactory.getProcessor(type);
		if (processor == null) {
			logger.error("Unknown content type: " + type);
			//out.write(html.charAt(start++));
			if (tag.hasAttribute(ATTR_ERROR))
				writeContent(out, tag.getAttribute(ATTR_ERROR), card);
			return;
		}
		try {
			if (processor.prepareData(tag, card, this)) {
				writeContent(out, tag.getAttribute(ATTR_START), card);
				processor.writeHtml(out, tag, card, this);
				writeContent(out, tag.getAttribute(ATTR_END), card);
			}
			else if (tag.hasAttribute(ATTR_EMPTY))
				writeContent(out, tag.getAttribute(ATTR_EMPTY), card);
		} catch (Exception e) {
			logger.error("Error processing tag " + type, e);
			if (tag.hasAttribute(ATTR_ERROR))
				writeContent(out, tag.getAttribute(ATTR_ERROR), card);
		}
	}

	protected String getPageId(ContentRequest request)
	{
		if (!(request instanceof PortletContentRequest)) {
			logger.warn("Impossible to determine portal page from servlet request");
			return null;
		}
		PortletRequest baseReq = ((PortletContentRequest) request).getPortletRequest();
		PortalNode node = null;
		if (baseReq instanceof JBossActionRequest)
			node = ((JBossActionRequest) baseReq).getPortalNode();
		else if (baseReq instanceof JBossRenderRequest)
			node = ((JBossRenderRequest) baseReq).getPortalNode();
		StringBuffer name = new StringBuffer();
		while (node != null) {
			if (node.getType() == PortalNode.TYPE_PAGE || node.getType() == PortalNode.TYPE_PORTAL)
				name.insert(0, "/" + node.getName());
			node = node.getParent();
		}
		return name.toString();
	}

    List<Card> getChildrenCards(ObjectId areaId){
        try {
            ListProject query = new ListProject(areaId);
            query.setAttribute(ContentIds.ATTR_CHILDREN);
            SearchResult children = (SearchResult) service.doAction(query);
            List cards = (List) children.getCards();
            fillAttributeNames(cards, (List) children.getColumns());
            return cards;
        } catch (Exception e) {
            logger.error("Error retrieving children for area " + areaId.getId(), e);
            return new ArrayList();
        }
    }
    
    private Card findSingleLinkedCard(Tag tag, Card card, LinkAttribute attr) {		
		String[] values = null;
		String[] names = null;
		
		if (tag.hasAttribute(ATTR_ITEM)) {
			values = tag.getAttribute(ATTR_ITEM).split("&");
			names = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				String[] parts = values[i].split("=", 2);
				if (parts.length == 1)
					values[i] = expandContent(parts[0], card).trim();
				else {
					names[i] = parts[0].trim();
					values[i] = expandContent(parts[1], card).trim();
				}
			}
		}
		
		return contentDataServiceFacade.findSingleLinkedCard(card, attr, names, values);
    }
    
    public Card findSingleLinkedCard(Card card, LinkAttribute attr, String[] names, String[] values) {    	
    	List linked = getLinkedCards(card, attr);
    	
    	if(null == names || null == values) {
    		if(null == linked || linked.isEmpty()) {
    			return null;
    		} else {
    			if (linked.size() > 1) {
    				logger.warn("No item selector defined for link attribute; using first retrieved");
    			}
    			return (Card) linked.iterator().next();
    		}
    	}
    	
    	for (Iterator itr = linked.iterator(); itr.hasNext(); ) {
			Card link = (Card) itr.next();
			int satisfied = 0;
			for (int i = 0; i < names.length; i++) {
				if (names[i] != null) {
					Attribute linkAttr = ContentUtils.getAttribute(link, names[i]);
					// (GoRik, 2011/04/04) ����� �� ������� � ������ ��������� ��������
					if (linkAttr != null && values[i].equalsIgnoreCase((linkAttr.getStringValue()!=null?linkAttr.getStringValue().trim():null)))
						satisfied++;
				} else {	// Not an optimal solution, but choosing item by any field seems to be hardly used
					for (Iterator itrAttr = link.getAttributes().iterator(); itrAttr.hasNext(); ) {
						Attribute linkAttr = (Attribute) itrAttr.next();
						if (values[i].equalsIgnoreCase(linkAttr.getStringValue().trim()))
							satisfied++;
					}
				}
			}
			if (satisfied == values.length) {
				return link;
			}
		}
    	
    	return null;
    }

    
    public Attribute getStringValueAttr(Tag tag, Card card, ObjectId field, ObjectId item) {
    	String[] values = null;
		String[] names = null;
		
		if (tag.hasAttribute(ATTR_ITEM)) {
			values = tag.getAttribute(ATTR_ITEM).split("&");
			names = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				String[] parts = values[i].split("=", 2);
				if (parts.length == 1)
					values[i] = expandContent(parts[0], card).trim();
				else {
					names[i] = parts[0].trim();
					values[i] = expandContent(parts[1], card).trim();
				}
			}
		}
		
		return getStringValueAttr(card, field, item, names, values);
    }
    
    private Attribute getStringValueAttr(Card card, ObjectId field, ObjectId item, String[] names, String[] values) {
    	
    	if(null == names || null == values)
    		return null;
    	int satisfied = 0;
		for (int i = 0; i < names.length; i++) {
			if (names[i] != null && stateNames.contains(names[i].toUpperCase())) {
				ListAttribute stateAttr = (ListAttribute) card.getAttributeById(_STATE);
				if(stateAttr.getValue() != null
						&& stateAttr.getValue().getValue() != null
						&& stateAttr.getValue().getValue().equalsIgnoreCase(values[i])) {
					satisfied++;
					continue;
				}
			}
			if (names[i] != null && templateNames.contains(names[i].toUpperCase())) {
				ListAttribute templateAttr = (ListAttribute) card.getAttributeById(_TEMPLATE);
				if(templateAttr.getValue() != null
						&& templateAttr.getValue().getValue() != null
						&& templateAttr.getValue().getValue().equalsIgnoreCase(values[i])) {
					satisfied++;
					continue;
				}
			}
			if (names[i] != null) {
				ObjectId name = IdUtils.smartMakeAttrId(names[i], PersonAttribute.class, false);
				Attribute attr = card.getAttributeById(name);
				if(attr != null && attr instanceof PersonAttribute
						&& ((PersonAttribute)attr).getPersonName() != null
						&& ((PersonAttribute)attr).getPersonName().equalsIgnoreCase(values[i])) {
					satisfied++;
					continue;
				}
			}
		}
		
		if (satisfied == values.length) {
			return card.getAttributeById(field);
		}
		
    	return null;
    }

    private void fillAttributeNames(List cards, List columns) {
        if (cards.size() == 0)
            return;
        HashMap map = new HashMap();
        for (Iterator itr = columns.iterator(); itr.hasNext();) {
            SearchResult.Column col = (SearchResult.Column) itr.next();
            map.put(col.getAttributeId(), col);
        }
        for (Iterator iCard = cards.iterator(); iCard.hasNext();) {
            Card card = (Card) iCard.next();
            for (Iterator iAttr = card.getAttributes().iterator(); iAttr.hasNext();) {
                Attribute attr = (Attribute) iAttr.next();    // shouldn't be blocks here
                SearchResult.Column col = (SearchResult.Column) map.get(attr.getId());
                if (col == null)
                    continue;
                attr.setNameEn(col.getNameEn());
                attr.setNameRu(col.getNameRu());
            }
        }
    }

    Collection<Template> allTemplates(){
        Collection<Template> templates;
            try {
                templates = service.listAll(Template.class);
            } catch (Exception e) {
                logger.error("Error retrieving list of templates", e);
                throw new IllegalStateException("Error retrieving list of templates", e);
            }
        return templates;
    }

    public ObjectId findTemplate(String name, Collection<Template> templates) {
        if (name != null) {
            name = name.trim();

            for (Iterator<Template> itr = templates.iterator(); itr.hasNext();) {
                Template template = itr.next();
                if (name.equalsIgnoreCase(template.getNameRu()) ||
                        name.equalsIgnoreCase(template.getNameEn()) ||
                        name.equals(template.getId().getId().toString()))
                    return template.getId();
            }
        }
        logger.error("Template " + name + " not found");
        return null;    //*****
    }

    void readContentTemplateAndStatus(Card content) {
        try {
            contentDataServiceFacade.readCardTemplateIdAndStatusId(content);
        } catch (DataException e) {
            logger.error("Error reading content template for card " + content.getId(), e);
            throw new RuntimeException(e);
        } catch (ServiceException e) {
            logger.error("Error reading content template for card " + content.getId(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * read content for current card with attributes defined in viewTemplateApplicationContext.xml
     *
     * @param content
     * @param viewCard
     * @return
     */
    public Card fetchContent(Card content, Card viewCard) {
        try {
            Card card = contentDataServiceFacade.getCardPresentationByViewId(content, viewCard.getId());
            if (card != null) {
                cardCashe.put((Long) card.getId().getId(), card);
                return card;
            } else {
                throw new IllegalStateException("Card " + content.getId() + " not found!");
            }
        } catch (DataException e) {
            logger.error("Error fetching content for card " + content.getId() + " and view " + viewCard.getId(), e);
            return null;
        } catch (ServiceException e) {
            logger.error("Error fetching content for card " + content.getId() + " and view " + viewCard.getId(), e);
            return null;
        }
    }





    public static final int MAX_NESTED_CALLS = 50;
	private HashMap writers = new HashMap();
	
	synchronized private void protectCallEnter(PrintWriter out) throws Exception
	{
		if (!writers.containsKey(out)) {
			writers.put(out, new Integer(1));
			return;
		}
		Integer calls = (Integer) writers.get(out);
		if (calls.intValue() >= MAX_NESTED_CALLS)
			throw new Exception("Nested calls limit exceeded");
		writers.put(out, new Integer(calls.intValue() + 1));
	}

	synchronized private void protectCallLeave(PrintWriter out)
	{
		Integer calls = (Integer) writers.get(out);
		if (calls.intValue() <= 1)
			writers.remove(out);
		else
			writers.put(out, new Integer(calls.intValue() - 1));
	}


    private class VarError implements Variable
	{
		public Object getValue()
		{
			Object value = request.getSessionAttribute(SESS_ATTR_ERROR);	//***** local
			if (value != null)
				request.removeSessionAttribute(SESS_ATTR_ERROR);	//***** local
			return value;
		}
	}
	
	private class VarFound implements Variable
	{
		public Object getValue()
		{   
			Search search = (Search) request.getSessionAttribute(APP_ATTR_SEARCH);	//***** global
			if(!keepAdvancedSearch) {
				request.removeAttribute(WorkstationAdvancedSearchPortlet.ADVANCED_SEARCH_BEAN);
			} else {
				search = (Search) request.getSessionAttribute(WorkstationAdvancedSearchPortlet.ADVANCED_SEARCH_BEAN);
			}
			if (search != null) {
				if (search.getFilter() == null)
					search.clearFilter();
				search.getFilter().setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
				List<?> cards = null;
				try {
					cards = ContentUtils.extendSearchResult((SearchResult) service.doAction(search), search.getFilter().getOrderedColumns().size() > 0);
				} catch (Exception e) {
					logger.error("Error searching cards", e);
					setError(e.getMessage());
					return null;
				}
				request.setSessionAttribute(SESS_ATTR_FOUND, cards);	//***** local
				request.setSessionAttribute(SESS_ATTR_FOUND_WHOLE_SIZE, search.getFilter().getWholeSize());
				request.removeSessionAttribute(APP_ATTR_SEARCH);	//***** local
			}
			return request.getSessionAttribute(SESS_ATTR_FOUND);	//***** local
		}
	}
}
