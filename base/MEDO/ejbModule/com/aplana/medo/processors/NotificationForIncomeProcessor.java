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

import com.aplana.medo.cards.CardException;
import com.aplana.medo.cards.NotificationCardHandler;

public class NotificationForIncomeProcessor extends NotificationProcessor {

    private static final String INCOME_DOC_KEY = "incomeId";

    public NotificationForIncomeProcessor(Properties properties) {
	super(properties);
    }

    @Override
    protected void postProcess(long notificationId) throws ProcessorException {
	long incomeDocId;
	String incomeDocIdString = values.get(INCOME_DOC_KEY);

	try {
	    incomeDocId = Long.parseLong(incomeDocIdString);
	} catch (NumberFormatException ex) {
	    throw new ProcessorException(
		    "jbr.medo.processor.notificationForIncome.id.parse",
		    new Object[] { incomeDocIdString }, ex);
	}

	try {
	    new NotificationCardHandler(notificationId).linkIncomeDocument(incomeDocId);
	} catch (CardException ex) {
	    throw new ProcessorException(
		    "jbr.medo.processor.notification.appendNotification", ex);
	}
    }

    @Override
    protected Collection<String> getRequiredInfoKeys() {
	Collection<String> infoKeys = new ArrayList<String>(super
		.getRequiredInfoKeys());
	infoKeys.addAll(Arrays.asList(new String[] { INCOME_DOC_KEY }));
	return infoKeys;
    }

}
