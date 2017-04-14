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
package com.aplana.ireferent.actions;

import java.util.Collection;
import java.util.Collections;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.config.ConfigurationException;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOMPerson;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ReflectionUtils;
import com.aplana.ireferent.util.ServiceUtils;

public class CreateLinkCardAction extends CreateCardAction {

    private static final String SET_USER = "setUser";
    private static final String TYPE_PARAM = "objectType";
    private static final String CHK_ON_EXIST = "chkOnExist";
    private static final String CHK_ON_USER = "chkOnUser";
    private static final String CHK_ON_LINK = "chkOnLink";
    private static final String CHK_ON_STATE = "chkOnState";

    private Class<? extends WSObject> objectType;
    private boolean setUser;
    private boolean chkOnExist;
    private ObjectId chkOnUser;
    private ObjectId chkOnLink;
    private ObjectId chkOnState;
    
    public void setParameter(String key, Object value) {
    	if (TYPE_PARAM.equals(key)) {
    	    objectType = ReflectionUtils.initializeClass(WSObject.class,
    		    (String) value);
    	} else 
    	if (SET_USER.equals(key)) {
    		setUser = Boolean.parseBoolean((String)value);
    	}
    	else 
    	if (CHK_ON_EXIST.equals(key)) {
    		chkOnExist =  Boolean.parseBoolean((String)value);
    	} else
    	if (CHK_ON_USER.equals(key)) {
    		chkOnUser = ObjectIdUtils.getObjectId(PersonAttribute.class,
        		    (String) value, false);	
        } else
        if (CHK_ON_LINK.equals(key)) {
        	chkOnLink =  ObjectIdUtils.getObjectId(CardLinkAttribute.class,
        		    (String) value, false);		
        } else
        if (CHK_ON_STATE.equals(key)) {
        	chkOnState = ObjectIdUtils.getObjectId(CardState.class,
        		    (String) value, true);
        } else                	
    	    super.setParameter(key, value);
    }

    @Override
    public void doAction(DataServiceBean serviceBean, WSObject object)
	    throws IReferentException {
	    String cardIdDoc = object.getId();
	    if (null == cardIdDoc || cardIdDoc.isEmpty()) {
	    	throw new IReferentException("Card Id for action 'CreateLinkCardAction' is null. Type: "
	    		    + object.getType());
	    }
	    Person person = setUser ?serviceBean.getPerson():null;
	    if (chkOnExist) {
	    	Collection<Card> cardsExistLink = getCards(serviceBean, cardIdDoc, person);
	    	if (!cardsExistLink.isEmpty())
	    		return;
	    }
	    WSObject createdCardLinkOnDocObject = createObject(object, person);
	    super.doAction(serviceBean, createdCardLinkOnDocObject);
    }
    
    private Collection<Card> getCards(DataServiceBean serviceBean,
    	    String cardId, Person person) throws IReferentException {
    	Search search = new Search();
    	search.setByAttributes(true);
    	if (null != chkOnUser) {
    		if (null == person) {
    			throw new IReferentException("Person for action 'CreateLinkCardAction' is null.");
    		}
    		search.addPersonAttribute(chkOnUser, person.getId());
    	}
    	if (null != chkOnLink) {
    	    search.addCardLinkAttribute(chkOnLink, new ObjectId(Card.class, Long.parseLong(cardId)));
    	}
    	if (null != chkOnState) {
    		search.setStates(Collections.singletonList(chkOnState));
    	}

    	return ServiceUtils
    		.searchCards(serviceBean, search, null);
        }
    
    protected WSObject createObject(WSObject object, Person person) throws IReferentException {
		WSObject linkObject = createBaseObject();
		if (!(linkObject instanceof Linked)) {
		    throw new ConfigurationException("Object should implement "
			    + Linked.class.getName());
		}
	
		Linked linked = (Linked) linkObject;
		WSOCollection collectDoc = new WSOCollection();
		collectDoc.getData().add(object);
		linked.setLinkOnDoc(collectDoc);
		if (setUser) {
			if (null == person) {
				throw new IReferentException("Person for action 'CreateLinkCardAction' is null.");
			}
			WSOMPerson personObj = new WSOMPerson();
			personObj.setId(String.valueOf((Long)person.getCardId().getId()));
			personObj.setTitle(person.getFullName());
			linked.setUser(personObj);
		}
		return linkObject;
    }
    
    protected WSObject createBaseObject() {
    	if (objectType != null) {
    	    return ReflectionUtils.instantiateClass(objectType);
    	}
    	return new WSObject();
    }
}
