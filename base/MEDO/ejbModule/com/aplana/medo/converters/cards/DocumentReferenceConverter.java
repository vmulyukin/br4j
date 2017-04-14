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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.aplana.medo.cards.CardException;
import com.aplana.medo.cards.OutcomeCardHandler;
import com.aplana.medo.converters.ConverterException;

public class DocumentReferenceConverter extends CardConverter {

    private static final String REG_DATE_FORMAT = "yyyy-MM-dd";

    public DocumentReferenceConverter(Properties properties, String name) {
	super(properties, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aplana.medo.converters.cards.CardConverter#processValues()
     */
    @Override
    protected long processValues() throws ConverterException {
	String regNum = getValueOfTagByKey("documentReference.regnum");
	String regDateString = getValueOfTagByKey("documentReference.regdate");

	Date regDate = null;
	if (!"".equals(regDateString)) {
	    DateFormat dateFormat = new SimpleDateFormat(REG_DATE_FORMAT);
	    try {
		regDate = dateFormat.parse(regDateString);
	    } catch (ParseException ex) {
		throw new ConverterException(
			"jbr.medo.converter.documentReference.registrationDate",
			new Object[] { name, REG_DATE_FORMAT, regDateString },
			ex);
	    }
	}

	try {
	    return new OutcomeCardHandler(regNum, regDate).getCardId();
	} catch (CardException ex) {
	    throw new ConverterException(
		    "jbr.medo.converter.documentReference.findOutcome",
		    new Object[] { name }, ex);
	}
    }
}
