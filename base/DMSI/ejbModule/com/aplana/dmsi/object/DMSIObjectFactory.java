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
package com.aplana.dmsi.object;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.config.FieldConfig;
import com.aplana.dmsi.config.Type;
import com.aplana.dmsi.config.TypesManager;
import com.aplana.dmsi.types.DMSIObject;
import com.aplana.dmsi.util.ServiceUtils;

public abstract class DMSIObjectFactory {

    private final static int INFINITY_LEVEL = -1;

    protected Log logger = LogFactory.getLog(getClass());
    protected DataServiceFacade serviceBean;
    private Type type;

    private int level = 0;
    protected Card parentCard;

    protected Card processingCard;

    private boolean isMObject = true;
    private boolean usingMObjects = true;
    private Map<String, UsingMObject> specificUsingMObjects = new HashMap<String, UsingMObject>();

    private Map<String, IgnoringField> ignoringFields = new HashMap<String, IgnoringField>();

    private Set<String> allowedFields = new HashSet<String>();
    private boolean isAllIgnored = false;

    private final static class UsingMObject {
	private final int level;
	private final boolean isUsing;

	public UsingMObject(int level, boolean isUsing) {
	    this.level = level;
	    this.isUsing = isUsing;
	}

	public int getLevel() {
	    return this.level;
	}

	public boolean isUsing() {
	    return this.isUsing;
	}
    }

    private static class IgnoringField {
	private final int startLevel;

	public IgnoringField(int startLevel) {
	    this.startLevel = startLevel;
	}

	public int getStartLevel() {
	    return this.startLevel;
	}
    }

    protected DMSIObjectFactory() {
    }

    public static DMSIObjectFactory newInstance(DataServiceFacade serviceBean,
	    String typeName) {
	return newInstance(serviceBean, TypesManager.instance().getTypeByName(
		typeName));
    }

    public static DMSIObjectFactory newInstance(DataServiceFacade serviceBean,
	    Type type) {
	Class<? extends DMSIObjectFactory> clazz = type.getFactory();
	DMSIObjectFactory factory;
	if (clazz == null) {
	    factory = new EntityFactory();
	} else {
	    factory = instantiateFactory(clazz);
	}
	factory.initialize(serviceBean, type);
	return factory;
    }

    // We cannot use ReflectionUtils because constructor is not public
    private static DMSIObjectFactory instantiateFactory(
	    Class<? extends DMSIObjectFactory> clazz) {
	try {
	    return clazz.newInstance();
	} catch (InstantiationException ex) {
	    throw new IllegalStateException("Error during instantiation of "
		    + clazz.getName(), ex);
	} catch (IllegalAccessException ex) {
	    throw new IllegalStateException(
		    "Accessing error during instantiation of "
			    + clazz.getName(), ex);
	}
    }

    protected void initialize(DataServiceFacade factoryServiceBean,
	    Type factoryType) {
	this.serviceBean = factoryServiceBean;
	this.type = factoryType;
    }

    public void setUsingMObjects(boolean useMObjects) {
	this.usingMObjects = useMObjects;
    }

    public void setUsingMObjectsForCurrentLevel(String fieldName,
	    boolean useMObjects) {
	setUsingMObjects(fieldName, useMObjects, this.level);
    }

    public void setUsingMObjectsAllLevels(String fieldName, boolean useMObjects) {
	setUsingMObjects(fieldName, useMObjects, INFINITY_LEVEL);
    }

    public void setUsingMObjects(String fieldName, boolean useMObjects,
	    int levelOfUsing) {
	if (fieldName != null && !"".equals(fieldName)) {
	    specificUsingMObjects.put(fieldName, new UsingMObject(levelOfUsing,
		    useMObjects));
	}
    }

    public void addIgnoredStartingLevel(String fieldName, int startLevel) {
	ignoringFields.put(fieldName, new IgnoringField(startLevel));
    }

    public void addIgnoredForAllLevels(String fieldName) {
	addIgnoredStartingLevel(fieldName, this.level);
    }

    public void setAllIgnoredExcept(String... nonIgnoredFields) {
    	this.isAllIgnored = true;
    	this.allowedFields.addAll(Arrays.asList(nonIgnoredFields));
    }

    public void setMObject(boolean isMObject) {
	this.isMObject = isMObject;
    }

    public Object newDMSIObject(ObjectId cardId) throws DMSIException {
	if (cardId == null)
	    return null;

	Collection<ObjectId> requiredAttributes = getRequiredAttributes();

	Collection<Card> cards = ServiceUtils.fetchCards(serviceBean,
		new ObjectId[] { cardId }, requiredAttributes);
	if (cards.isEmpty()) {
	    logger.info(String.format("Card with %s id was not found", cardId
		    .getId()));
	    return null;
	}
	if (cards.size() > 1) {
	    logger.warn("More than one card was found with ID="
		    + cardId.getId());
	}
	return newDMSIObject(cards.iterator().next());
    }

    protected abstract Object newDMSIObject(Card card) throws DMSIException;

    public Collection<?> newCollection(ObjectId[] cardIds) throws DMSIException {
	Collection<ObjectId> requiredAttributes = getRequiredAttributes();
	return newCollection(ServiceUtils.fetchCards(serviceBean, cardIds,
		requiredAttributes));
    }

    public Collection<?> newCollection(Search search) throws DMSIException {
	Collection<ObjectId> requiredAttributes = getRequiredAttributes();
	return newCollection(ServiceUtils.searchCards(serviceBean, search,
		requiredAttributes));
    }

    protected Collection<?> newCollection(Collection<Card> cards)
	    throws DMSIException {
	List<Object> data = new ArrayList<Object>();
	for (Card card : cards) {
	    data.add(newDMSIObject(card));
	}
	return data;
    }

    protected boolean isFieldIgnored(Field field) {
	String fieldName = field.getName();

	if (this.isAllIgnored) {
		return !this.allowedFields.contains(fieldName);
	}
	return this.ignoringFields.containsKey(fieldName)
		&& this.level >= this.ignoringFields.get(fieldName)
			.getStartLevel();
    }

    protected DMSIObjectFactory createFactoryForField(FieldConfig fieldConfig) {
	DMSIObjectFactory newFactory = newInstance(this.serviceBean,
		fieldConfig.getTypeName());
	newFactory.initializeFromParentFactory(this);
	newFactory.setMObject(isFieldMObject(fieldConfig.getFieldName()));
	for (String ignoringField : fieldConfig.getIgnoringFields()) {
	    newFactory.addIgnoredForAllLevels(ignoringField);
	}
	return newFactory;
    }

    protected void initializeFromParentFactory(DMSIObjectFactory parentFactory) {
	level = parentFactory.level + 1;
	for (Entry<String, UsingMObject> fieldUsingMObject : parentFactory.specificUsingMObjects
		.entrySet()) {
	    final String fieldName = fieldUsingMObject.getKey();
	    final UsingMObject usingMObject = fieldUsingMObject.getValue();
	    int entryLevel = usingMObject.getLevel();
	    if (level <= entryLevel || entryLevel == INFINITY_LEVEL) {
		specificUsingMObjects.put(fieldName, usingMObject);
	    }
	}
	ignoringFields = parentFactory.ignoringFields;
	parentCard = parentFactory.processingCard;
    }

    protected boolean isFieldMObject(String fieldName) {
	boolean isUsingMObject = usingMObjects;
	if (specificUsingMObjects.containsKey(fieldName)) {
	    isUsingMObject = specificUsingMObjects.get(fieldName).isUsing();
	}
	return isUsingMObject;
    }

    protected Collection<ObjectId> getRequiredAttributes() {
	return isMObject ? type.getMetaTypeRequiredAttributes() : type
		.getTypeRequiredAttributes();
    }

    protected boolean canCardBeProcessed(Card card) {
	return type.hasEntityOfTemplate(card.getTemplate());
    }

    protected Class<? extends DMSIObject> getObjectClassByTemlate(
	    ObjectId templateId) {
	return isMObject ? type.getMetaTypeByTemplate(templateId) : type
		.getTypeByTemplate(templateId);
    }
}
