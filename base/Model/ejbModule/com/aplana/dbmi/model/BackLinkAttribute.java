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
package com.aplana.dbmi.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.util.ObjectIdUtils;

/**
 * {@link Attribute} descendant used for pseudo-attributes representing
 * set of cards referencing given {@link Card}.<br>
 * One card could reference another one via attribute of type {@link CardLinkAttribute}.
 * {@link BackLinkAttribute} is used to display this relation from the referenced
 * card's point of view.
 * Attributes of this type is always read-only and its values is never stored in database.
 * I.e. there could be records for attributes of this type in 
 * ATTRIBUTE and TEMPLATE_ATTRIBUTE tables, but shouldn't be such records
 * in ATTRIBUTE_VALUE table
 */
public class BackLinkAttribute extends LinkAttribute
{
	private static final long serialVersionUID = 3L;
	private static final Log logger = LogFactory.getLog(BackLinkAttribute.class);

	private ObjectId linkSource;
	private Collection<ObjectId> linkSources = new ArrayList<ObjectId>();
	private ObjectId interimLink;
	private boolean linked;

	/**
	 * @return identifier of {@link CardLinkAttribute} which defines link between 
	 * cards displayed by this attribute
	 */
	public ObjectId getLinkSource() {
		return linkSource;
	}

	/**
	 * @return identifiers of {@link CardLinkAttribute} which defines link between 
	 * cards displayed by this attribute
	 */
	public Collection<ObjectId> getLinkSources() {
		return linkSources;
	}
	/**
	 * Sets identifier of {@link CardLinkAttribute} which defines link between 
	 * cards displayed by this attribute
	 * @param linkSource desired value of linkSource property
	 * @throws IllegalArgumentException if linkSource is not an 
	 * identifier of {@link CardLinkAttribute} object 
	 */
	public void setLinkSource(ObjectId linkSource) {
		if(linkSource == null) {
			this.linkSource = null;
			this.linkSources = new ArrayList<ObjectId>();
			return;
		}
		if (!CardLinkAttribute.class.equals(linkSource.getType()))
			throw new IllegalArgumentException("Not a card link attribute id");
		if (this.linkSource==null)
		this.linkSource = linkSource;
		else
			linkSources.add(linkSource);
	}

	public ObjectId getInterimLink() {
		return interimLink;
	}

	public void setInterimLink(ObjectId interimLink) {
		this.interimLink = interimLink;
	}

	/**
	 * @see Attribute#getType()
	 */
	@Override
	public Object getType() {
		return TYPE_BACK_LINK;
	}

	/**
	 * @see Attribute#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		return true;
	}

	/**
	 * This attribute have to be readOnly so
	 * any attempt to make it editable will cause IllegalArgumentException
	 * @param readOnly only true is allowed
	 * @throws IllegalArgumentException if readOnly = false
	 */
	@Override
	public void setReadOnly(boolean readOnly) {
		if (!readOnly)
			throw new IllegalArgumentException("Back link attribute cannot be writable");
	}

	/**
	 * @return true, if this card is linked from the other card by "LINK".
	 */
	public boolean isLinked() {
		return this.linked;
	}

	/**
	 * @param linked: true, if this card is linked from the other card by "LINK".
	 */
	public void setLinked(boolean linked) {
		this.linked = linked;
	}
	
	@Override
	public void setValueFromAttribute(Attribute attr){
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Checks if this attribute contains same set of links to other cards as given {@link CardLinkAttrbute}
	 * @param attr {@link Attribute} to compare with. Should be CardLinkAttribute instance.
	 * @throws  IllegalArgumentException if attr is not an CardLinkAttribute instance
	 */
	@Override
	public boolean equalValue(Attribute attr) {
		if (!BackLinkAttribute.class.isAssignableFrom(attr.getClass())) {
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		}
		List<ObjectId> orderids = getOrderIdsList();
		return ObjectIdUtils.isSameObjectIdsInSameOrder(orderids, ((BackLinkAttribute) attr).getOrderIdsList());

	}
	
	
	// TODO: ����������� ������ � CardLink � BackLink ���� ����� ���������� �� �������
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void setLinkedCardLabelText(ObjectId cardId, String value) 
	{
		if (cardId != null) {
			Map mapLabels = super.getLabelLinkedMap();
			mapLabels.put(cardId, value);
		}
	}

	// TODO: ����������� ������ � CardLink � BackLink ���� ����� ���������� �� �������
	@SuppressWarnings("rawtypes")
	@Override
	public void clear() {
		Map mapLabels = super.getLabelLinkedMap();
		mapLabels.clear();
	}
	
}
