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
package com.aplana.medo.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.w3c.dom.Document;

import com.aplana.medo.cards.CardException;
import com.aplana.medo.cards.DistributionItemCardHandler;

public class NotificationForOutcomeProcessor extends NotificationProcessor {

    private static final String SOURCE_KEY = "source";
    private static final String FOUNDATION_KEY = "foundation";
    private static final String DISTRIBUTION_ITEM_KEY = "distributionItem";

    public NotificationForOutcomeProcessor(Properties properties) {
	super(properties);
    }

    @Override
    protected void postProcess(long notificationId) throws ProcessorException {
	long distributionItemId;
	String distributionIdString = values.get(DISTRIBUTION_ITEM_KEY);
	// Determine card id
	if ("".equals(distributionIdString)) {
	    distributionItemId = calculateCardId();
	} else {
	    try {
		distributionItemId = Long.parseLong(distributionIdString);
	    } catch (NumberFormatException ex) {
		throw new ProcessorException(
			"jbr.medo.processor.notificationForOutcome.id.parse",
			new Object[] { distributionIdString }, ex);
	    }
	}

	try {
	    new DistributionItemCardHandler(distributionItemId)
		    .appendNotification(notificationId);
	} catch (CardException ex) {
	    throw new ProcessorException(
		    "jbr.medo.processor.notification.appendNotification", ex);
	}
    }

    /**
     * Calculates card id according to 'foundation' and 'source' elements of
     * notification. The 'foundation' is used to find source outcome document.
     * The 'source' is used to choose from distribution list one item that have
     * certain organization.
     *
     * @param values -
     *                table that contain read from XML values (here values of
     *                {@link #FOUNDATION_KEY} and {@link #SOURCE_KEY} keys are
     *                used)
     * @return
     * @see DistributionItemCardHandler#DistributionItemCardHandler(Long, Long)
     * @see DistributionItemCardHandler#getCardId()
     * @see #readValues(Document)
     * @throws ProcessorException
     */
    private long calculateCardId() throws ProcessorException {
	long cardId = -1;
	String foundationIdString = values.get(FOUNDATION_KEY);
	long foundationId;
	try {
	    foundationId = Long.parseLong(foundationIdString);
	} catch (NumberFormatException ex) {
	    throw new ProcessorException(
		    "jbr.medo.processor.notification.id.calculation",
		    new Object[] { FOUNDATION_KEY, foundationIdString }, ex);
	}
	String sourceIdString = values.get(SOURCE_KEY);
	long sourceId;
	try {
	    sourceId = Long.parseLong(sourceIdString);
	} catch (NumberFormatException ex) {
	    throw new ProcessorException(
		    "jbr.medo.processor.notification.id.calculation",
		    new Object[] { SOURCE_KEY, sourceIdString }, ex);
	}

	DistributionItemCardHandler cardHandler = new DistributionItemCardHandler(
		foundationId, sourceId);
	try {
	    cardId = cardHandler.getCardId();
	} catch (CardException ex) {
	    throw new ProcessorException(
		    "jbr.medo.processor.notification.findDistributionItem", ex);
	}
	return cardId;
    }

    @Override
    protected Collection<String> getRequiredInfoKeys() {
	Collection<String> infoKeys = new ArrayList<String>(super
		.getRequiredInfoKeys());
	infoKeys.addAll(Arrays.asList(new String[] { SOURCE_KEY,
		FOUNDATION_KEY, DISTRIBUTION_ITEM_KEY }));
	return infoKeys;
    }

}
