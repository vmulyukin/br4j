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

import com.aplana.dbmi.gui.SearchAttributeView;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.web.tag.util.StringUtils;

import javax.portlet.PortletRequest;
import java.util.*;

public class SearchStateAttributeViewCreator extends SearchAttributeViewCreator<StateSearchAttribute> {
	
	public SearchStateAttributeViewCreator(StateSearchAttribute attribute) {
		super(attribute);
	}

	@Override
	public SearchAttributeView create(PortletRequest request,
			SearchFilterPortletSessionBean sessionBean)
			throws ServiceException, DataException {
		initSearchAttribute(attribute, sessionBean);
		SearchAttributeView attributeView = createAndInitSearchAttributeView(request);
		return attributeView;
	}
	
	@Override
	protected void initSearchAttribute(Attribute attributeDef, SearchFilterPortletSessionBean sessionBean) throws ServiceException, DataException {
		Collection<CardState> obtainedCardStates = getCardStatesFromTemplates(sessionBean);
		Collection<CardState> predCardStates = getPredefinedCardStates(attribute);
		Collection<CardState> cardStates = new ArrayList<CardState>();
		
		for (CardState predefCS : predCardStates) {
			for (CardState obtainCS : obtainedCardStates) {
				if (obtainCS.getId().equals(predefCS.getId())) {
					cardStates.add(obtainCS);
					break;
				}
			}
		}
		if (predCardStates.isEmpty()) {
			cardStates = obtainedCardStates;
		}
		
		attribute.setCardStates(cardStates);
	}
	
	/**
	 * �������� ������ � ����������� ���������
	 * Returns available card states for passed template  
	 */
	private Collection<CardState> getCardStatesFromTemplates(SearchFilterPortletSessionBean sessionBean) {
		DataServiceBean serviceBean = sessionBean.getServiceBean();
		List<Template> templates = sessionBean.getSearchPageTemplates();
		List<CardState> cardStates = new ArrayList<CardState>(5);
		if (templates.isEmpty()) {
			return cardStates;
		}
		
		Iterator<Template> iterator = templates.iterator();
		Template template;
		while (iterator.hasNext()) {
			template = iterator.next();
			try {
				template = serviceBean.getById(template.getId());
				Collection<CardState> fromStates = serviceBean.listChildren(template.getWorkflow(), CardState.class);
				addUniqueStatus(cardStates, fromStates);
			} catch (DataException e) {
				sessionBean.setMessage(e.getMessage());
			} catch (ServiceException e) {
				sessionBean.setMessage(e.getMessage());
			}
		}
		return cardStates;		
	}
	
	private void addUniqueStatus(List<CardState> toStates, Collection<CardState> fromStates){		
		Iterator<CardState> iterator = fromStates.iterator();
		CardState cardState;
		while (iterator.hasNext()) {
			cardState = iterator.next();
			if (!toStates.contains(cardState)) {
				toStates.add(cardState);
			}
		}
	}
	
	/**
	 * �������� ������ � ����� ��������
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
		} catch (ServiceException e) {
			sessionBean.setMessage(e.getMessage());
		}
		
		return Collections.emptyList();
	}
	
	private Collection<CardState> getPredefinedCardStates(StateSearchAttribute stateSearchAttribute){
		String states = stateSearchAttribute.getPredefCardStatesString();
		if (StringUtils.hasLength(states)) {
			final String[] statesVal = states.split(",");
			ArrayList<CardState> realCardStates = new ArrayList<CardState>(1);
			for (String aStatesVal : statesVal) {
				CardState cs = new CardState();
				cs.setId(ObjectId.predefined(CardState.class, aStatesVal.trim()));
				realCardStates.add(cs);
			}
			return realCardStates;
		} else {
			return Collections.emptyList();
		}
	}
}