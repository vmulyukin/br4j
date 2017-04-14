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

import java.util.Collection;

import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;

public class FillOgAuthorProcessor extends ProcessCard {
	private static final long serialVersionUID = 1L;
	
	public static final ObjectId AUTHORTYPE = ObjectId.predefined(CardLinkAttribute.class, "jbr.AuthType");
	public static final ObjectId LASTNAME = ObjectId.predefined(StringAttribute.class, "jbr.og.lastname");
	public static final ObjectId FIRSTNAME = ObjectId.predefined(StringAttribute.class, "jbr.InfOGFirstName");
	public static final ObjectId SECONDNAME = ObjectId.predefined(StringAttribute.class, "jbr.InfOGPatronimic");
	public static final ObjectId NOAUTHOR = new ObjectId(Card.class, 15921);
	public static final ObjectId COLLECTIVE = new ObjectId(Card.class, 15920);
	public static final ObjectId NAME = new ObjectId(StringAttribute.class, "NAME");
	
	@Override
	public Object process() throws DataException {
		Card card = getCard();
		
		//������� �����-�� ����� ������ �� ������ - �������������� �� �����
		for(ObjectId id : new ObjectId[]{FIRSTNAME, SECONDNAME, LASTNAME}) {
			 if(!card.getAttributeById(id).isEmpty()) return null;
		}
		
		CardLinkAttribute authorTypeCard = (CardLinkAttribute)card.getAttributeById(AUTHORTYPE);
		if(authorTypeCard != null && !authorTypeCard.isEmpty()) {
			Collection<ObjectId> authorType =  authorTypeCard.getIdsLinked();
			// � ������ ���� ��� ������ ��� ������� (���������) �� ���� ����������� ��� ������ ���������
			//if(authorType.contains(NOAUTHOR) || authorType.contains(COLLECTIVE)) {
			if(authorType.contains(COLLECTIVE)) {
				Card authorCard = super.loadCardById((ObjectId)authorType.iterator().next()); 
				StringAttribute name = (StringAttribute) authorCard.getAttributeById(NAME);
				setAuthorName(card, name.getValue());
			}
		}
		return null;
	}
	
	private void setAuthorName (Card card, String name) throws DataException{
		for(ObjectId id : new ObjectId[]{FIRSTNAME, SECONDNAME, LASTNAME}){
			StringAttribute attr = (StringAttribute) card.getAttributeById(id);
			attr.setValue(name);
		}
		// ��������� ������ ���������� ��������
		super.doOverwriteCardAttributes(card.getId(), card.getAttributeById(LASTNAME),
		                                              card.getAttributeById(FIRSTNAME),
		                                              card.getAttributeById(SECONDNAME));
	}
}
