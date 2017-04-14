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
package com.aplana.dbmi.module.docflow;

import java.util.Iterator;
import java.util.Map;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.ProcessorBase;

public class VisaTrigger extends ProcessorBase implements Parametrized {
	private static final long serialVersionUID = 1L;
	
	public static final String PARAM_PROCESSOR_BEAN = "processorBean";
	public static final String PARAM_CONFIG_BEAN = "configBean";
	
	private DocumentProcessor processor;
	private VisaConfiguration config;
	
	public void setParameter(String name, String value) {
		if (PARAM_PROCESSOR_BEAN.equalsIgnoreCase(name))
			processor = (DocumentProcessor) getBeanFactory().getBean(value);
		else if (PARAM_CONFIG_BEAN.equalsIgnoreCase(name))
			config = (VisaConfiguration) getBeanFactory().getBean(value);
		else
			throw new IllegalArgumentException("Unknown parameter: " + name);
	}

	public Object process() throws DataException {
		if (config == null)
			throw new IllegalStateException("Config parameter must be set");
		if (processor == null)
			throw new IllegalStateException("Processor bean must be set");
		
		processor.setDocumentId(getParentCard());
		processor.process();
		return getResult();
	}

	private ObjectId getParentCard() throws DataException {
		ChangeState move = (ChangeState) getAction();
		Map<Object, ObjectId> ids = config.getObjectIdMap(VisaConfiguration.ATTR_PARENT + VisaConfiguration.INFIX_TEMPLATE,
				Template.class, BackLinkAttribute.class);
		final Object DEFAULT = new Long(0);
		if (config.getValue(VisaConfiguration.ATTR_PARENT) != null)
			ids.put(DEFAULT, config.getObjectId(BackLinkAttribute.class, VisaConfiguration.ATTR_PARENT));
		ObjectId linkAttrId = null;
		if (move.getCard().getTemplate() != null) {
			linkAttrId = ids.get(move.getCard().getTemplate());
			if (linkAttrId == null)
				linkAttrId = ids.get(DEFAULT);
		}
		for (Iterator<ObjectId> itr = ids.values().iterator(); itr.hasNext(); ) {
			ObjectId id = itr.next();
			if (linkAttrId != null && !linkAttrId.equals(id))
				continue;
			if(BackLinkAttribute.class.isAssignableFrom(id.getType())) {
				ListProject search = new ListProject();
				search.setAttribute(id);
				search.setCard(move.getCard().getId());
				ActionQueryBase query = getQueryFactory().getActionQuery(search);
				query.setAction(search);
				SearchResult result = (SearchResult) getDatabase().executeQuery(getSystemUser(), query);
				if (result.getCards().size() == 0)
					continue;
				if (result.getCards().size() > 1)
					throw new DataException("docflow.visa.document",
							new Object[] { move.getCard().getId().getId() });
				return ((Card) result.getCards().iterator().next()).getId();
			} else if(CardLinkAttribute.class.isAssignableFrom(id.getType())) {
				ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
				cardQuery.setId(move.getCard().getId());
				Card card = (Card) getDatabase().executeQuery(getSystemUser(), cardQuery);
				CardLinkAttribute cAttr = (CardLinkAttribute) card.getAttributeById(id);
				if (cAttr.getIdsLinked().size() == 0)
					continue;
				if (cAttr.getIdsLinked().size() > 1)
					throw new DataException("docflow.visa.document",
							new Object[] { move.getCard().getId().getId() });
				return cAttr.getIdsLinked().get(0);
			} else
				throw new IllegalArgumentException("Attribute " + id.getId() + " must be CardLink or BackLink");
		}
		throw new DataException("docflow.visa.document", new Object[] { move.getCard().getId().getId() });
	}
}
