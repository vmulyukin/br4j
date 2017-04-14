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
package com.aplana.dbmi.card.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;

public class CardAttributesInitializer {

    private Log logger = LogFactory.getLog(getClass());

    private Card destinationCard;
    private Card sourceCard;
    private Set<Pair> copyingAttributes = new HashSet<Pair>();;

    public CardAttributesInitializer() {
    }

    public void addAttributesToCopy(ObjectId sourceId, ObjectId destinationId) {
        copyingAttributes.add(new Pair(sourceId, destinationId));
    }

    public void setSourceCard(Card card) {
        sourceCard = card;
    }

    public void initialize(Card card) {
        destinationCard = card;
        if (!copyingAttributes.isEmpty() && sourceCard == null) {
            throw new IllegalStateException("Source card is not defined");
        }

        logger.debug("Copying from " + sourceCard.getId() + " to "
                + destinationCard.getId() + " is started");
        for (Pair pair : copyingAttributes) {
            logger.debug("Trying to copy from " + pair.getSourceAttributeId()
                    + " to " + pair.getDestinationAttributeId());
            Attribute sourceAttribute = sourceCard.getAttributeById(pair
                    .getSourceAttributeId());
            Attribute destinationAttribute = destinationCard
                    .getAttributeById(pair.getDestinationAttributeId());
            doCopy(sourceAttribute, destinationAttribute);
        }
        logger.debug("Copying from " + sourceCard.getId() + " to "
                + destinationCard.getId() + " is finished");
    }

    private void doCopy(Attribute sourceAttribute,
            Attribute destinationAttribute) {
        if (sourceAttribute == null) {
            logger.debug("Source attribute is not found in card. Skip it");
            return;
        }

        if (destinationAttribute == null) {
            logger.debug("Destination attribute is not found in card. Skip it");
            return;
        }

        if (sourceAttribute instanceof CardLinkAttribute) {
            doCopyFromCardlink((CardLinkAttribute) sourceAttribute,
                    destinationAttribute);
        } else if (sourceAttribute instanceof ListAttribute) {
            doCopyFromList((ListAttribute) sourceAttribute,
                    destinationAttribute);
        } else {
            throw new UnsupportedOperationException("Copying from "
                    + sourceAttribute.getClass() + " + is not supported");
        }

    }

    private void doCopyFromCardlink(CardLinkAttribute sourceAttr,
            Attribute destAttr) {
        if (destAttr instanceof CardLinkAttribute) {
            if (destAttr.isMultiValued()) {
                List<ObjectId> values = sourceAttr.getIdsLinked();
                if (values == null) {
                    return;
                }
                ((CardLinkAttribute) destAttr).addIdsLinked(values);
            } else {
                ObjectId value = sourceAttr.getSingleLinkedId();
                if (value == null) {
                    return;
                }
                ((CardLinkAttribute) destAttr).addSingleLinkedId(value);
            }
        } else {
            throw new UnsupportedOperationException("Copying from cardlink to "
                    + destAttr.getClass() + " + is not supported");
        }

    }

    private void doCopyFromList(ListAttribute sourceAttribute,
            Attribute destAttr) {
        if (destAttr instanceof ListAttribute) {
            ((ListAttribute) destAttr).setValue(sourceAttribute.getValue());
        } else {
            throw new UnsupportedOperationException("Copying from list to "
                    + destAttr.getClass() + " + is not supported");
        }
    }

    private static class Pair {
        private final ObjectId sourceAttributeId;
        private final ObjectId destinationAttributeId;

        public Pair(ObjectId sourceAttributeId, ObjectId destinationAttributeId) {
            super();
            this.sourceAttributeId = sourceAttributeId;
            this.destinationAttributeId = destinationAttributeId;
        }

        public ObjectId getSourceAttributeId() {
            return this.sourceAttributeId;
        }

        public ObjectId getDestinationAttributeId() {
            return this.destinationAttributeId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime
                    * result
                    + ((this.destinationAttributeId == null) ? 0
                            : this.destinationAttributeId.hashCode());
            result = prime
                    * result
                    + ((this.sourceAttributeId == null) ? 0
                            : this.sourceAttributeId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final Pair other = (Pair) obj;
            if (this.destinationAttributeId == null) {
                if (other.destinationAttributeId != null)
                    return false;
            } else if (!this.destinationAttributeId
                    .equals(other.destinationAttributeId))
                return false;
            if (this.sourceAttributeId == null) {
                if (other.sourceAttributeId != null)
                    return false;
            } else if (!this.sourceAttributeId.equals(other.sourceAttributeId))
                return false;
            return true;
        }

    }

}
