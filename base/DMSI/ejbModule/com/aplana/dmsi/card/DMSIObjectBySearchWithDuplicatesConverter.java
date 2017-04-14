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
package com.aplana.dmsi.card;

import java.util.Collection;
import java.util.HashSet;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;

public class DMSIObjectBySearchWithDuplicatesConverter extends
        DMSIObjectBySearchConverter {

    private final static String ORIGINIAL_ATTR_PARAM = "originalAttr";
    private final static String DUPLICATE_STATE_PARAM = "duplicateStates";
    private ObjectId originalAttr;
    private Collection<ObjectId> duplicateStateIds = new HashSet<ObjectId>();

    @SuppressWarnings("unchecked")
    @Override
    public void setParameter(String key, Object value) {
        if (ORIGINIAL_ATTR_PARAM.equals(key)) {
            originalAttr = ObjectIdUtils.getObjectId(CardLinkAttribute.class,
                    (String) value, false);
        } else if (DUPLICATE_STATE_PARAM.equals(key)) {
            String statesDescription = ((String) value).trim();
            if ("".equals(statesDescription))
                return;
            String[] idDescriptions = statesDescription.split("\\s*,\\s*");
            for (String idDescription : idDescriptions) {
                ObjectId stateId = ObjectIdUtils.getObjectId(CardState.class,
                        idDescription, true);
                addStateId(stateId);
                addDuplicateStateId(stateId);
            }
        } else {
            super.setParameter(key, value);
        }
    }

    private void addDuplicateStateId(ObjectId stateId) {
        duplicateStateIds.add(stateId);
    }

    @Override
    protected Collection<ObjectId> getRequiredAttributes() {
        Collection<ObjectId> requiredAttributes = new HashSet<ObjectId>();
        requiredAttributes.addAll(super.getRequiredAttributes());
        requiredAttributes.add(originalAttr);
        return requiredAttributes;
    }

    @Override
    protected ObjectId getCardId(Card card) {
        ObjectId cardState = card.getState();
        if (!duplicateStateIds.contains(cardState)) {
            return super.getCardId(card);
        }
        CardLinkAttribute attr = (CardLinkAttribute) card
                .getAttributeById(originalAttr);
        if (attr == null) {
            return null;
        }
        return attr.getSingleLinkedId();
    }

}
