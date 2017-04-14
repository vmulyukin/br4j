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
package com.aplana.dbmi.action;

import java.util.HashSet;
import java.util.Set;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;

/**
 * {@link Action} implementation used to make a copy of given {@link Card} object.
 * State of newly created cards will be set to value returned by 
 * {@link com.aplana.dbmi.model.Workflow#getInitialState()} method of
 * corresponding {@link  com.aplana.dbmi.model.Workflow} object.
 * <br>
 * Returns newly created card as result. 
 */
public class CloneCard implements ObjectAction
{
	private static final long serialVersionUID = 3L;
	private ObjectId origId;
	private ObjectId template;

	public ObjectId getTemplate() {
		return template;
	}

	public void setTemplate(ObjectId template) {
		this.template = template;
	}

	/**
	 * ����� ������� ����������� ����� ���������, ����������� ��� ��������������� 
	 * ����������� � ����������� �������� ({@link Attribute.TYPE_XXX}).
	 * ���������� �������: user, backLink, cardLinkm, typedLink, history, material.
	 */
	final protected Set/*<Object>*/ disabledTypes = new HashSet(); 
	{
			disabledTypes.add( Attribute.TYPE_PERSON);
			disabledTypes.add( Attribute.TYPE_BACK_LINK);
			disabledTypes.add( Attribute.TYPE_CARD_LINK); 
			disabledTypes.add( Attribute.TYPE_TYPED_CARD_LINK);
			disabledTypes.add( Attribute.TYPE_CARD_HISTORY);
			disabledTypes.add( Attribute.TYPE_MATERIAL);
	}

	/**
	 * ����� ������� ����������� ����� ���������, ����������� ��� ��������������� 
	 * ����������� � ����������� ��������. ��������� ����� ����� ��������� �������� 
	 * ����������� � ����������� ��������� ��� ��� ������ {@link #disabledTypes}.
	 * �.�. ��� ������ ���������� � ������ {@code disabledTypes}.
	 */
	final protected Set/*<ObjectId>*/ disabledAttrIds = new HashSet(); 

	/**
	 * ����� ������� ����������� ����� ���������, ����������� ��� ��������������� 
	 * ����������� � ����������� ��������. ��������� ����� ����� ��������� �������� 
	 * ����������� � ����������� ��������� ��� ��� ������ {@link #disabledTypes}. 
	 * ���� ���� �������, ������������ � ���� ������ ����� ���, ����������� � �����������,
	 * �� ����� �� ����� ����������. �.�. ��� ������ ���������� � ������ {@link #disabledTypes}.
	 */
	final protected Set/*<ObjectId>*/ enabledAttrIds = new HashSet(); 

	/**
	 * Default constructor
	 */
	public CloneCard() {
	}

	/**
	 * Creates action object and sets value of {@link #getOrigId() origId} property.
	 * @param card Card object identifier of which will be used as 
	 * value of {@link #getOrigId() origId} property.
	 * @throws IllegalArgumentException if given {@link Card} object is null or if
	 * it have an empty identifier.
	 */
	public CloneCard(Card card) {
		if (card == null || card.getId() == null)
			throw new IllegalArgumentException("Original card must exist");
		this.origId = card.getId();
	}
	
	/**
	 * Creates action object and sets value of {@link #getOrigId() origId} property. 
	 * @param cardId identifier of {@link Card} object
	 * @throws IllegalArgumentException if given identifier is not a {@link Card} identifier or if it is null
	 */
	public CloneCard(ObjectId cardId) {
		if (cardId == null || !Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card id");
		this.origId = cardId;
	}

	/**
	 * Gets identifier of card to be copied
	 * @return identifier of card to be copied
	 */
	public ObjectId getOrigId() {
		return origId;
	}

	/**
	 * Sets identifier of card to be copied
	 * @param origId identifier of card to be copied
	 */
	public void setOrigId(ObjectId origId) {
		this.origId = origId;
	}

	/**
	 * @see ObjectAction#getObjectId()
	 */
	public ObjectId getObjectId() {
		return origId;
	}

	/**
	 * @see Action#getResultType()
	 */
	public Class getResultType() {
		return Card.class;
	}

	/**
	 * @return ������ ����� ���������, ����������� ��� ���������������
	* ����������� � ����������� �������� ({@link Attribute.TYPE_XXX}).
	 * ���������� �������: backLink, cardLinkm, typedLink, history, material.
	 */
	public Set getDisabledTypes() {
		return this.disabledTypes;
	}

	/**
	 * @return ������ ����� ���������, ����������� ��� ���������������
	* ����������� � ����������� ��������. ��������� {@link #disabledTypes}
	 */
	public Set getDisabledAttrIds() {
		return disabledAttrIds;
	}

	/**
	 * @return ������ ����� ���������, ����������� ��� ���������������
	* ����������� � ����������� ��������. ������������� ���������� ��� 
	* ������ {@link #disabledTypes}.
	 */
	public Set getEnabledAttrIds() {
		return enabledAttrIds;
	}

}
