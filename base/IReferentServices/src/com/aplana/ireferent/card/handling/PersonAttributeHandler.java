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
package com.aplana.ireferent.card.handling;

import java.util.Collection;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.util.ServiceUtils;

class PersonAttributeHandler extends AttributeHandler {

    private DataServiceBean serviceBean;

    public PersonAttributeHandler(Attribute attribute,
    	DataServiceBean serviceBean) {
        super(attribute);
        this.serviceBean = serviceBean;
    }

    @Override
    public Object getAttributeValue() {
        @SuppressWarnings("unchecked")
        Collection<Person> persons = ((PersonAttribute) attribute)
    	    .getValues();
        Collection<ObjectId> personCards = ServiceUtils.getPersonCards(
    	    serviceBean, persons);
        return personCards.toArray(new ObjectId[personCards.size()]);
    }

    @Override
    public void setAttributeValue(Object value) {
        ObjectId[] cardIds;
        if (value instanceof ObjectId) {
    	cardIds = new ObjectId[] { (ObjectId) value };
        } else if (value instanceof ObjectId[]) {
    	cardIds = (ObjectId[]) value;
        } else {
    	cardIds = new ObjectId[0];
        }
        ((PersonAttribute) attribute).setValues(ServiceUtils
    	    .getPersonsByCards(serviceBean, cardIds));
    }
}