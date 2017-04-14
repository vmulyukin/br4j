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
package com.aplana.dbmi.archive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import com.aplana.dbmi.action.AddToArchive;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;

/**
 * ����� ���������� �� ��������� ������� ��� �������� � ����.
 * ���������� ArchiveConfigReader ��� �������� �������
 * � ���� AddToArchive ��� ���������� �������� ������.
 * @author ppolushkin
 * @since 19.12.2014
 */
public class CardArchiver {
	
	private QueryFactory queryFactory;
	private Database database;
	private UserData user;
	
	private Long rootCardId;
	private Set<Long> cardsToDelete;
	
	public CardArchiver(QueryFactory queryFactory, Database database,
			UserData user) {
		this.queryFactory = queryFactory;
		this.database = database;
		this.user = user;
	}

	public Long getRootCardId() {
		return rootCardId;
	}

	public void setRootCardId(Long rootCardId) {
		this.rootCardId = rootCardId;
	}

	public Set<Long> getCardsToDelete() {
		return cardsToDelete;
	}

	public void setCardsToDelete(Set<Long> cardsToDelete) {
		this.cardsToDelete = cardsToDelete;
	}
	
	public void archive() throws DataException {
		if(CollectionUtils.isEmpty(cardsToDelete)) {
			return;
		}
		
		final Set<ArchiveConfig> archiveConfig = ArchiveConfigReader.getArchiveConfigSet();
		
		if(CollectionUtils.isEmpty(archiveConfig)) {
			return;
		}
		
		Search templateSearch = CardUtils.getFetchAction(String.valueOf(rootCardId), new ObjectId[]{Card.ATTR_ID, Card.ATTR_TEMPLATE});
		
		final List<Card> rootCard = CardUtils.execSearchCards(templateSearch, queryFactory, database, user);
		
		Map<Long, Set<ObjectId>> cardAttrMap = null;
		
		for(ArchiveConfig ac : archiveConfig) {
			if(!CollectionUtils.isEmpty(rootCard) 
					&& ac.getTemplate() != null 
					&& ac.getTemplate().equals(rootCard.get(0).getTemplate().getId())) {
				
				cardAttrMap = new HashMap<Long, Set<ObjectId>>();
				
				cardAttrMap.put(rootCardId, ac.getAttributes());
				
				templateSearch = CardUtils.getFetchAction(ObjectIdUtils.numericIdsToCommaDelimitedString(cardsToDelete), 
						new ObjectId[]{Card.ATTR_ID, Card.ATTR_TEMPLATE});
				
				final List<Card> cards = CardUtils.execSearchCards(templateSearch, queryFactory, database, user);
				
				for(Card card : cards) {
					if(ac.getChildren().keySet().contains(card.getTemplate().getId())) {
						cardAttrMap.put((Long) card.getId().getId(), ac.getChildren().get(card.getTemplate().getId()));
					}
				}
			}
			
			AddToArchive action = new AddToArchive();
			action.setCopyDBValues(cardAttrMap);
			ActionQueryBase query = queryFactory.getActionQuery(action);
			query.setAction(action);
			database.executeQuery(user, query);
		}
		
	}

}
