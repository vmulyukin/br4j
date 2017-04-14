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
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.ServletUtil;

/**
 * A servlet that searches linked file card by provided search criteria (templates, states, attribute value)
 * and sends request to MaterialDownloadServlet to download that file
 * 
 * Only one card ought to be found
 */
public class MaterialSearchDownloadServlet extends MaterialDownloadServlet {
	
	public static final String PARAM_SEARCH_TEMPLATE_ID = "SEARCH_TEMPLATE_ID";
	public static final String PARAM_SEARCH_STATUS_ID = "SEARCH_STATUS_ID";
	public static final String PARAM_SEARCH_ATTR_ID = "SEARCH_ATTR_ID";
	public static final String PARAM_SEARCH_ATTR_VALUE = "SEARCH_ATTR_VALUE";
	
	private static final String ATTR_TYPE_DELIMITER = ":";
	
	private static final ObjectId filesAttrId = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	
	private Search search;
	
	public MaterialSearchDownloadServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		try {
			DataServiceBean serviceBean = ServletUtil.createService(request);
			
			prepareSearch(request, serviceBean);
			
			if(search != null) {
				SearchResult result = serviceBean.doAction(search);
				List<Card> foundCards = result.getCards();
				if(foundCards == null || foundCards.isEmpty()) {
					logger.error("No card has been found by given criteria!");
					throw new DataException("material.download.card.not.found");
				}
				if(foundCards.size() > 1) {
					logger.error("More than one card has been found by given criteria!");
					throw new DataException("material.download.card.found.too.many");
				}
				
				CardLinkAttribute attr = foundCards.get(0).getAttributeById(filesAttrId);
				if(attr == null) {
					logger.error("No doclink attribute has been found!");
					throw new DataException("material.download.card.not.found");
				}
				List<ObjectId> linked = attr.getIdsLinked();
				if(linked != null && !linked.isEmpty() && linked.get(0).getId() != null)
					response.sendRedirect(request.getContextPath() + "/MaterialDownloadServlet?"+PARAM_CARD_ID+"="+linked.get(0).getId());
			}
		} catch (AccessControlException e) {
			logger.error(e.getMessage());
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		} catch (DataException e) {
			sendError(request, response, e);
		} catch (Exception e) {
			logger.error(e.getMessage());
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * fills search object by given criteria
	 * @param request
	 * @param service
	 * @throws DataException
	 * @throws ServiceException
	 */
	private void prepareSearch(HttpServletRequest request, final DataServiceBean service) throws DataException, ServiceException {
		search = new Search();
		search.setByAttributes(true);
		search.setColumns(new ArrayList<Column>(){
			{
				add(new Column(){
					{
						setAttributeId(filesAttrId);
					}
				});
			}
		});
		
		final String templateId = request.getParameter(PARAM_SEARCH_TEMPLATE_ID);
		if(templateId != null && !templateId.equals("")) {
			String[] ids = templateId.split(",");
			final List<ObjectId> templates = new ArrayList<ObjectId>(ids.length);
			for(String id : ids) {
				ObjectId tempId = ObjectIdUtils.getObjectId(Template.class, id.trim(), true);
				templates.add(tempId);
			}
			search.setTemplates(templates);
		}
		final String statusId = request.getParameter(PARAM_SEARCH_STATUS_ID);
		if(statusId != null && !statusId.equals("")) {
			String[] ids = statusId.split(",");
			final List<ObjectId> states = new ArrayList<ObjectId>(ids.length);
			for(String id : ids) {
				ObjectId stateId = ObjectIdUtils.getObjectId(CardState.class, id.trim(), true);
				states.add(stateId);
			}
			search.setStates(states);
		}
		final String attrId = request.getParameter(PARAM_SEARCH_ATTR_ID);
		final String attrValue = request.getParameter(PARAM_SEARCH_ATTR_VALUE);
		if(attrId != null && !attrId.equals("")
				&& attrValue != null && !attrValue.equals("")) {
			final ObjectId attributeId = ObjectIdUtils.getAttrObjectId(attrId.trim(), ATTR_TYPE_DELIMITER);
			search.addAttributes(new ArrayList<Attribute>(){{
												add(careateAttribute(attributeId, attrValue, service));
								}});
		}
	}
	
	/**
	 * Creates attribute with provided id and value
	 * 
	 * By now only list attribute is supported
	 * 
	 * @param attrId attribute id
	 * @param value	attribute value
	 * @param service
	 * @return created attribute
	 * @throws DataException
	 * @throws ServiceException
	 */
	private Attribute careateAttribute(ObjectId attrId, Object value, DataServiceBean service) throws DataException, ServiceException {
		Attribute attr = Attribute.createFromId(attrId);
		if(attr instanceof ListAttribute) {
			ReferenceValue refVal = service.getById(ObjectIdUtils.getObjectId(ReferenceValue.class, value.toString(), true));
			((ListAttribute) attr).setValue(refVal);
		} else throw new DataException("Unsupported attribute type in given search criteria");
		return attr;
	}
}
