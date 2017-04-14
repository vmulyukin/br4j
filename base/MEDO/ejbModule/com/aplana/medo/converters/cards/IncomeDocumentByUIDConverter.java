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
package com.aplana.medo.converters.cards;

import java.util.Properties;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.medo.cards.CardException;
import com.aplana.medo.cards.IncomeCardHandler;
import com.aplana.medo.converters.ConverterException;

public class IncomeDocumentByUIDConverter extends CardConverter {

    public IncomeDocumentByUIDConverter(Properties properties, String name) {
	super(properties, name);
    }

    protected final Log logger = LogFactory.getLog(getClass());

    @Override
    protected long processValues() throws ConverterException {
	String uidValue = getValueOfTagByKey("cardId.notification.uid");
	long id = -1;
	if (!"".equals(uidValue)) {
	    UUID uid = null;
	    try {
		uid = UUID.fromString(uidValue);
		id = new IncomeCardHandler(uid).getCardId();
	    } catch (IllegalArgumentException ex) {
		logger
			.warn("UID was read but have illegal format. It will be ignored");
	    } catch (CardException ex) {
		logger
			.warn("The error was occurred while card was searching by uid");
	    }
	}
	return id;
    }

}
