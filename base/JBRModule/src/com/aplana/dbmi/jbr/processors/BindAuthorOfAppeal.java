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
package com.aplana.dbmi.jbr.processors;

import java.util.Collections;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;

import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;

/**
 * ���������, ����������� �������� �������� ������ ��������� � �������� �� �� ��������� ������ ����� "���������� �� ������ ���������".
 * ��� ���������� ���������� �������� ������ ��� ��������� ��������.
 * @author erentsov
 * 
 * 
 */
public class BindAuthorOfAppeal extends ProcessCard {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final static ObjectId authorInformationBlockId = ObjectId.predefined(AttributeBlock.class, "jbr.authorOfAppealInfo");
	public final static ObjectId authorAttributeId = ObjectId.predefined(CardLinkAttribute.class, "jbr.ReqAuthor");
	public final static ObjectId authorTemplateId = ObjectId.predefined(Template.class, "jbr.medo_og.requestAuthor");

	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException {
		Card card = loadCardById(getCard().getId());	// �������� ��������, �.�. �� ����� ���� ������� ���������
		Card authorCard = null;
		AttributeBlock authorInformationBlock = card.getAttributeBlockById(authorInformationBlockId);
		CardLinkAttribute authorAttribute = (CardLinkAttribute) card.getAttributeById(authorAttributeId);
		if(authorInformationBlock == null || authorAttribute == null) {
			logger.info("No author information block or author attribute. Exiting");
			return null;
		}
		// ����� ���� ������������ �������: ���� �������� �����, ����� � ��������� ���� ������ ����������� �������� � �� �� ������ ������������, � �� ���� �� ���� �������
		if(authorAttribute.getIdsLinked() != null && !authorAttribute.getIdsLinked().isEmpty()){
			logger.info("Author of appeal was set manually. Exiting.");
			return null;
		}
			
		Search search = new Search();
		search.setTemplates(Collections.singleton(authorTemplateId));
		search.setByAttributes(true);
		search.addAttributes(authorInformationBlock.getAttributes());
		SearchResult result = (SearchResult) execAction(search);
		
		if(!result.getCards().isEmpty()){
			if(result.getCards().size() > 1) logger.warn("Multiple matches. First one will be used.");
			authorCard = (Card) result.getCards().iterator().next();		
		} else{
			logger.info("No suitable authors found. New one will be created.");
			CreateCard create = new CreateCard();
			create.setTemplate(authorTemplateId);
			authorCard = (Card) execAction(create);
			authorCard.getAttributeBlockById(authorInformationBlockId).setAttributes(authorInformationBlock.getAttributes());
			saveCard(authorCard, getSystemUser());
			execAction(new UnlockObject(authorCard.getId()));
		}
		
		authorAttribute.addSingleLinkedId(authorCard.getId());
		doOverwriteCardAttributes(card.getId(), authorAttribute);
		reloadCard(getSystemUser());
		
		logger.info("Card with id = " + authorCard.getId().getId() + " was binded successfully.");
		return card;
	}

}
