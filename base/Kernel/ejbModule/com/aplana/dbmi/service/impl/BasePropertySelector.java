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
package com.aplana.dbmi.service.impl;

import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.WeakHashMap;

/**
 * {@link Selector} implementation which compares value
 * of given object property with given constant value. <br>
 * Used in {@link QueryFactory}.
 */
public class BasePropertySelector implements BeanFactoryAware, Selector {

	final protected Log logger = LogFactory.getLog(getClass());
	protected String propName;
	protected String value;
	protected boolean load = false;
	protected boolean operEquals = true;
	protected BeanFactory beanFactory;
	public static final ObjectId AUTHOR = ObjectId.predefined(PersonAttribute.class, "author");
	
	private static final WeakHashMap<Object, HashSet<String>> context = new WeakHashMap<Object, HashSet<String>>();

	/**
	 * Creates new PropertySelector instance
	 * @param propName name of object property to be checked
	 * @param value desired value of object property
	 */
	public BasePropertySelector(String propName, String value) {
		this.propName = propName;
		this.value = value;
	}

	protected BasePropertySelector() {
		super();
	}
	
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * @return true, ���� �������� �������� �� ��������� � false - �� �����������.
	 */
	public boolean isOperEquals() {
		return this.operEquals;
	}

	/**
	 * @param operEquals ������ ���� �� ��������� �������� ��������� 
	 * �� ��������� (true) ��� ����������� (false).
	 */
	public void setOperEquals(boolean operEquals) {
		this.operEquals = operEquals;
	}

	public boolean isLoad() {
		return load;
	}

	public void setLoad(boolean load) {
		this.load = load;
	}

	/**
	 * �������� �� ������� �� ����� �������� ��� �������. 
	 * @param baseObj: ������, � ������� ����������� ��������.
	 * @param name: ��� (�������� ���������: card.id) �������� ��� ��������.
	 * @return value of property
	 * @throws Exception 
	 */
	protected Object getValueByName(Object baseObj, String name) throws Exception { 
		return PropertyUtils.getProperty(baseObj, name);
	}

	/**
	 * Checks if value of given object property matches desired value of this property.
	 * <br>
	 * If value of property is a {@link DataObject} or {@link ObjectId} instance then
	 * desired value is interpreted as {@link ObjectId identifier} and comparison
	 * of two {@link ObjectId} instances is performed.
	 * <br>
	 * Otherwise simple string comparison is performed. 
	 * @return true is value of given object property is found to be the same as
	 * desired value, false otherwise.
	 */
	@Override
	public boolean satisfies(Object object) /*throws DataException*/ {
		try {
			if (propName.matches("flag:[\\w]+")) {
				String name = propName.replaceAll("flag:", "");
				String propValue = WorkflowProperties.getProperty(name);
				return !value.equals("null") ? value.equals(propValue) == operEquals : (propValue == null) == operEquals;
			}
			if (load) {
				HashSet<String> loadedProps = context.get(object);
				if (loadedProps == null) {
					loadedProps = new HashSet<String>();
					context.put(object, loadedProps);
				}
				if (!loadedProps.contains(propName)) {
					loadIntermediateObjects(object);
					loadedProps.add(propName);
				}
			}
			Object propValue = getValueByName(object, propName);
			if (value.equalsIgnoreCase("null"))
				return (propValue == null) == operEquals;
			if (value.equalsIgnoreCase("author")){
				final Card c = (Card) object;
				final Attribute attr = c.getAttributeById(AUTHOR); 
				if (attr == null)
				{
					logger.warn("Attribute author is Null");
					return false;
				}
				try {
					if (attr instanceof PersonAttribute)
						// personId as String
						return (((PersonAttribute)attr).getPerson().getId().getId().toString().equals(propValue)) == operEquals;
				} catch(NullPointerException x) {
					return false;
				}
					
			}			
			if (propValue == null)
				return !operEquals;

			if (propValue instanceof DataObject)
				propValue = ((DataObject) propValue).getId();
			if (propValue instanceof ObjectId) {
				Class<?> type = ((ObjectId) propValue).getType(); 
				ObjectId id = ObjectId.predefined(type, value);
				if (id == null)
					try {
						id = new ObjectId(type, Long.parseLong(value));
					} catch (NumberFormatException e) {
						id = new ObjectId(type, value);
					}
				return propValue.equals(id) == operEquals;
			}
			return value.equals(propValue) == operEquals;
		/*} catch (DataException e) {
			throw e;*/
		} catch (Exception e) {
			logger.warn("Error retrieving property " + propName +
					" of object " + object.getClass().getName(), e);
			return false;
		}
	}
	
	private void loadIntermediateObjects(Object object) throws DataException {
		String[] parts = propName.split("\\.");
		String prop = parts[0];
		for (int i = 0; i < parts.length - 1; i++) {
			Object value;
			try {
				value = PropertyUtils.getProperty(object, prop);
			} catch (Exception e) {
				logger.warn("Error retrieving property " + prop +
						" of object " + object.getClass().getName(), e);
				return;
			}
			if (!(value instanceof DataObject))
				return;
			DataObject intermediate = (DataObject) value;
			if (intermediate.getId() == null)
				return;
			ObjectQueryBase query = getQueryFactory().getFetchQuery(intermediate.getId().getType());
			query.setId(intermediate.getId());
			intermediate = getDatabase().executeQuery(getSystemUser(), query);
			try {
				PropertyUtils.setProperty(object, prop, intermediate);
			} catch (Exception e) {
				logger.warn("Error modifying property " + prop +
						" of object " + object.getClass().getName(), e);
				return;
			}
			
			prop += "." + parts[i + 1];
		}
	}
	
	protected QueryFactory getQueryFactory() {
		return (QueryFactory) beanFactory.getBean(DataServiceBean.BEAN_QUERY_FACTORY);
	}
	
	protected Database getDatabase() {
		return (Database) beanFactory.getBean(DataServiceBean.BEAN_DATABASE);
	}
	
	protected UserData getSystemUser() throws DataException {
		UserData user = new UserData();
		user.setPerson(getDatabase().resolveUser(Database.SYSTEM_USER));
		user.setAddress("internal");
		return user;
	}

	/** 
	 * @see Object#equals(Object)
	 * @return true if obj is a PropertySelector instance and obj have same values
	 * of propName and value properties as this PropertySelector instance 
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof BasePropertySelector))
			return false;
		BasePropertySelector other = (BasePropertySelector) obj;
		return propName.equals(other.propName) && value.equals(other.value);
	}

	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return propName.hashCode() ^ value.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return MessageFormat.format( "?({0}{1}{2})", propName, (operEquals ? "=" : "!="), value);
	}
}
