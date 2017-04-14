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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.action.Search.Interval;
import com.aplana.dbmi.action.Search.DatePeriod;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;

public class StrictSearch implements Action<List<ObjectId>> {
	private static final long serialVersionUID = 2L;
	protected Map<Object,Object> attributes = new HashMap<Object,Object>();
	protected Collection<? extends DataObject> templates = new HashSet<DataObject>();

	/**
	 * Adds string attribute criteria for search.
	 * <em>Note, </em> that string values are stored in
	 * {@link #getAttributes()} using inner class <code>StringValue</code>
	 * @param id object ID of string attribute
	 * @param value required value of attribute
	 * @param isCaseSensitive whether search should be case sensitive or not
	 */
	public void addStringAttribute(ObjectId id, String value, boolean isCaseSensitive){
		if (!StringAttribute.class.equals(id.getType()) &&
				!TextAttribute.class.equals(id.getType()))
				throw new IllegalArgumentException("Not a string or text attribute");
		if (value == null)
			throw new IllegalArgumentException(" Given attribute has NULL value");
		this.attributes.put(id.getId(), new StringValue(value, isCaseSensitive));
	}

	/**
	 * Adds string attribute criteria for search. Search will be case
	 * sensitive for this attribute
	 * <em>Note, </em> that string values are stored in
	 * {@link #getAttributes()} using inner class <code>StringValue</code>
	 * @param id object ID of string attribute
	 * @param value required value of attribute
	 */
	public void addStringAttribute(ObjectId id, String value){
		addStringAttribute(id, value, true);
	}

	public void addIntegerAttribute(ObjectId id, int min, int max) {
		if (!IntegerAttribute.class.equals(id.getType()))
			throw new IllegalArgumentException("Not an integer attribute");
		attributes.put(id.getId(), new Interval(min, max));
	}
	
	public void addDateAttribute(ObjectId id, Date valueStart, Date valueEnd) {
		if (!DateAttribute.class.equals(id.getType()))
				throw new IllegalArgumentException("Not a date attribute");
		if (valueStart == null || valueEnd == null)
			throw new IllegalArgumentException(" Given attribute has NULL value");
		this.attributes.put(id.getId(), new DatePeriod(valueStart, valueEnd));
	}

	public void addPersonAttribute(ObjectId id, ObjectId person) {
		if (!PersonAttribute.class.equals(id.getType()))
			throw new IllegalArgumentException("Not a person attribute");
		if (!Person.class.equals(person.getType()))
			throw new IllegalArgumentException("Not a person");
		if (Person.ID_CURRENT.equals(person))
			attributes.put(id.getId(), person);
		else
			attributes.put(id.getId(),
					new Interval(((Long) person.getId()).intValue(), ((Long) person.getId()).intValue()));
	}

	public void addCardLinkAttribute(ObjectId id, ObjectId cardId) {
		if (!CardLinkAttribute.class.equals(id.getType()))
			throw new IllegalArgumentException("Not a card link attribute");
		if (!Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card");
		attributes.put(id.getId(),
				new Interval(((Long) cardId.getId()).intValue(), ((Long) cardId.getId()).intValue()));
	}

	public void removeAttribute(ObjectId id){
		attributes.remove(id);
	}

	/**
	 * Clears all attribute values constraints
	 */
	public void clearAttributes() {
		attributes = new HashMap<Object,Object>();
	}

	/**
	 * Gets collection of attribute values constraints defined in this Search object.
	 * Each element in the returned collection is a <tt>Map.Entry</tt>,
	 * key contains string identifier of attribute, value contains object definin
	 * values constraint.
	 * @return collection of attribute values constraints defined in this Search object
	 */
	public Collection<Map.Entry<Object,Object>> getAttributes() {
		return attributes.entrySet();
	}

	@SuppressWarnings("unchecked")
	public <T extends DataObject> Collection<T> getTemplates() {
		return (Collection<T>)templates;
	}

	public void setTemplates(Collection<? extends DataObject> templates) {
		this.templates = templates;
	}

	public void addTemplate(Template id) {
		getTemplates().add(id);
	}

	public void removeTemplate(Template id) {
		templates.remove(id);
	}

	public Class<?> getResultType() {
		return List.class;
	}

	/**
	 * Inner class that used to store string values. It stores information
	 * about string value and whether search should be case sensitive or not
	 */
	public class StringValue {
		private String value;
		private boolean isCaseSensitive = true;

		public StringValue(String value, boolean isCaseSensitive) {
			this.value = value;
			this.isCaseSensitive = isCaseSensitive;
		}

		public StringValue(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}

		public boolean isCaseSensitive() {
			return this.isCaseSensitive;
		}
	}
}
