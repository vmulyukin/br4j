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

import static com.aplana.medo.cards.CardXMLBuilder.CODE_ATTRIBUTE;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import com.aplana.medo.converters.ConverterException;
import com.aplana.medo.converters.ListAttributeConverter;


public class TypeDocNPAConverter extends ListAttributeConverter {

    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Creates a new instance from a properties configuration and a tag name of
     * input DOM element.
     *
     * @param properties -
     *                configuration that should contain additional property
     * @param name -
     *                tag name of input DOM element. Used to get concrete
     *                property from appropriate set of properties
     * @see ListConverter(Properties, String)
     */
    public TypeDocNPAConverter(Properties properties, String name) {
	super(properties, name);
    }

    /**
     * Perform processing of read values
     *
     * @throws ConverterException
     *
     * @see com.aplana.medo.converters.ListAttributeConverter#processValues()
     */
    @Override
    protected long processValues(Element element) throws ConverterException {
    	String docTypeAttr = getPredefinedParam("npa.docType");
    	String codeValue = element.getAttribute(CODE_ATTRIBUTE);
    	if ( !docTypeAttr.equals(codeValue)) {
    	    return -1;
    	}
	    long result = Long.parseLong(getPredefinedParam("npa.no"));
	    Set<String> docTypesNpa = new HashSet<String>();
	    docTypesNpa.add(getPredefinedParam("npa.typeValue1"));
	    docTypesNpa.add(getPredefinedParam("npa.typeValue2"));
	    docTypesNpa.add(getPredefinedParam("npa.typeValue3"));
	    docTypesNpa.add(getPredefinedParam("npa.typeValue4"));

	    String valueDocType = (null == element.getTextContent())? null : element.getTextContent().trim();
	    if (valueDocType != null && !valueDocType.isEmpty()) {
	    	if(docTypesNpa.contains(valueDocType))
	    		result = Long.parseLong(getPredefinedParam("npa.yes"));
	    }
	return result;
    }
}
