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
package com.aplana.ireferent;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.config.FieldConfig;
import com.aplana.ireferent.config.Type;
import com.aplana.ireferent.config.TypesManager;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ServiceUtils;

public abstract class WSObjectFactory {

    private final static int ONLY_CURRENT_LEVEL = 0;
    private final static int INFINITY_LEVEL = Integer.MAX_VALUE;
    private final static int MINUS_INFINITY_LEVEL = Integer.MIN_VALUE;

    protected Log logger = LogFactory.getLog(getClass());
    protected DataServiceBean serviceBean;
    private Type type;

    private int level = 0;

    protected String setId;
 
    protected Card parentCard;

    protected Card processingCard;

    private boolean isMObject = true;
    private boolean usingMObjects = true;
    private Map<String, UsingMObject> specificUsingMObjects = new HashMap<String, UsingMObject>();

    private Map<String, Set<IgnoringField>> ignoringFields = new HashMap<String,  Set<IgnoringField>>();

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
	private final int stopLevel;

	public IgnoringField(int startLevel, int stopLevel) {
	    this.startLevel = startLevel;
	    this.stopLevel = stopLevel;
	}

	
	public int getStartLevel() {
	    return this.startLevel;
	}
	
	public int getStopLevel() {
	    return this.stopLevel;
	}
    }

    protected WSObjectFactory() {
    }
    
	public String getSetId() {
	    return this.setId;
	}
	
	public void setSetId(String setId) {
	    this.setId = setId;
	}

    public static WSObjectFactory newInstance(DataServiceBean serviceBean,
	    String typeName) {
	return newInstance(serviceBean, TypesManager.instance().getTypeByName(
		typeName));
    }

    public static WSObjectFactory newInstance(DataServiceBean serviceBean,
	    Type type) {
	Class<? extends WSObjectFactory> clazz = type.getFactory();
	WSObjectFactory factory;
	if (clazz == null) {
	    factory = new EntityFactory();
	} else {
	    factory = instantiateFactory(clazz);
	}
	factory.initialize(serviceBean, type);
	return factory;
    }
    
    public static WSObjectFactory newInstance(DataServiceBean serviceBean) {
    	WSObjectFactory factory = new SimpleObjectFactory();
    	factory.serviceBean = serviceBean;
    	return factory;
    }
    
	public static WSObjectFactory createPagedSearchObjectFactory(
			DataServiceBean serviceBean, String typeName, int page,
			int pageSize, String keyword) {
		Type type = TypesManager.instance().getTypeByName(typeName);
		Class<? extends WSObjectFactory> clazz = type.getFactory();
		WSObjectFactory factory;
		if (clazz == null) {
			factory = new PagedSearchObjectFactory(page, pageSize, keyword);
		} else {
			factory = instantiateFactory(clazz);
		}
		factory.initialize(serviceBean, type);
		return factory;
	}

    // We cannot use ReflectionUtils because constructor is not public
    private static WSObjectFactory instantiateFactory(
	    Class<? extends WSObjectFactory> clazz) {
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

    protected void initialize(DataServiceBean factoryServiceBean,
	    Type factoryType) {
	this.serviceBean = factoryServiceBean;
	this.type = factoryType;
    }

    public void setUsingMObjects(boolean useMObjects) {
	this.usingMObjects = useMObjects;
    }

    public void setUsingMObjectsForCurrentLevel(String fieldName,
	    boolean useMObjects) {
	setUsingMObjects(fieldName, useMObjects, ONLY_CURRENT_LEVEL);
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
    
    public void addIgnoredLevels(String fieldName, int startLevel, int stopLevel) {
    	if (ignoringFields.containsKey(fieldName)) {
	    	ignoringFields.get(fieldName).add(new IgnoringField(startLevel, stopLevel));
    	} else {
    		Set<IgnoringField> ignoringFieldSet = new HashSet<IgnoringField>();
    		ignoringFieldSet.add(new IgnoringField(startLevel, stopLevel));
	    	ignoringFields.put(fieldName, ignoringFieldSet);
    	}
    }

    public void addIgnoredStartingLevel(String fieldName, int startLevel) {
    	addIgnoredLevels(fieldName, startLevel, INFINITY_LEVEL);
    }
    
    public void addIgnoredBeforeLevel(String fieldName, int stopLevel) {
    	addIgnoredLevels(fieldName, MINUS_INFINITY_LEVEL, stopLevel);
    }

    public void addIgnoredForCurrentLevel(String fieldName) {
    	addIgnoredLevels(fieldName, ONLY_CURRENT_LEVEL, ONLY_CURRENT_LEVEL);
    }
    
    public void addIgnoredAllLevel(String fieldName) {
        	addIgnoredLevels(fieldName, MINUS_INFINITY_LEVEL, INFINITY_LEVEL);
    }
    
    public void addIgnoredAllExceptCurrentLevel(String fieldName) {
    	addIgnoredLevels(fieldName, MINUS_INFINITY_LEVEL, -1);
    	addIgnoredLevels(fieldName, 1, INFINITY_LEVEL);
    }

    public void setMObject(boolean isMObject) {
	this.isMObject = isMObject;
    }

    public Object newWSObject(ObjectId cardId) throws IReferentException {
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
	return newWSObject(cards.iterator().next());
    }

    protected abstract Object newWSObject(Card card) throws IReferentException;
    
    public WSOCollection newWSOCollection(ObjectId[] cardIds)
	    throws IReferentException {
	Collection<ObjectId> requiredAttributes = getRequiredAttributes();
	logger.info("newWSOCollection(fetch):fetchCards begin:");
	logger.info("newWSOCollection(fetch):fetchCards length = " + cardIds.length + "; size attr = " + requiredAttributes.size());
	Collection<Card> collectionCards = ServiceUtils.fetchCards(serviceBean, cardIds,
			requiredAttributes);
	logger.info("newWSOCollection(fetch):fetchCards end.");
	logger.info("newWSOCollection(fetch):create begin:");
	logger.info("newWSOCollection(fetch):create size = " + collectionCards.size());
	WSOCollection wsCollection = newWSOCollection(collectionCards);
	logger.info("newWSOCollection(fetch):create end.");
	return wsCollection;
    }
    

	public WSOCollection newWSOCollection(Search search)
	    throws IReferentException {
	Collection<ObjectId> requiredAttributes = getRequiredAttributes();
	logger.info("newWSOCollection(search):searchCards begin:");
	
	Collection<Card> collectionCards =  ServiceUtils.searchCards(serviceBean, search,
			requiredAttributes);
	logger.info("newWSOCollection(search):searchCards end.");
	logger.info("newWSOCollection(search):create begin:");
	logger.info("newWSOCollection(search):create size = " + collectionCards.size());
	WSOCollection wsCollection = newWSOCollection(collectionCards);
	logger.info("newWSOCollection(search):create end.");
	return wsCollection;
    }

    protected <T extends WSObject> WSOCollection newWSOCollection(
	    Collection<Card> cards) throws IReferentException {
	WSOCollection wsoCollection = new WSOCollection();
	List<Object> data = wsoCollection.getData();
	Person person = serviceBean.getPerson();
	if ( person != null){
		wsoCollection.setUserId( person.getCardId().getId().toString() );
	}
	for (Card card : cards) {
	    data.add(newWSObject(card));
	}
	return wsoCollection;
    }

    protected boolean isFieldIgnored(String fieldName) {
    	boolean result = false;
    	if (this.ignoringFields.containsKey(fieldName)) {
	    	Set<IgnoringField> ignoringFieldSet = this.ignoringFields.get(fieldName);
	    	for(IgnoringField ignoringField : ignoringFieldSet) {
	    		result = (this.level >= ignoringField.startLevel && this.level <= ignoringField.stopLevel);
	    		if (result)
	    			return result;
	    	}
    	}
    	return result;
    }

    protected WSObjectFactory createFactoryForField(FieldConfig fieldConfig) {
	WSObjectFactory newFactory = newInstance(this.serviceBean, fieldConfig
		.getTypeName());
	newFactory.setMObject(isFieldMObject(fieldConfig.getFieldName()));
	newFactory.initializeFromParentFactory(this, fieldConfig.isParent());
	return newFactory;
    }

    protected void initializeFromParentFactory(WSObjectFactory parentFactory, boolean isParent) {
	if (!isParent) {
		level = parentFactory.level + 1;
	} else {
		level = parentFactory.level - 1;
	}
	for (Entry<String, UsingMObject> fieldUsingMObject : parentFactory.specificUsingMObjects
		.entrySet()) {
	    final String fieldName = fieldUsingMObject.getKey();
	    final UsingMObject usingMObject = fieldUsingMObject.getValue();
	    int entryLevel = usingMObject.getLevel();
	    if (Math.abs(level) <= entryLevel || entryLevel == INFINITY_LEVEL) 
	    {
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

    protected Class<? extends WSObject> getObjectClassByTemlate(
	    ObjectId templateId) {
	return isMObject ? type.getMetaTypeByTemplate(templateId) : type
		.getTypeByTemplate(templateId);

    }
}
