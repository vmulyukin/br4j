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
package org.aplana.br4j.dynamicaccess.rule_attribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.xmldef.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class FindAttributeName
{
    protected final Log logger = LogFactory.getLog(getClass());

    private Map<String, String> attributeNames;

    public FindAttributeName(Map<String, String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    public FindAttributeName(AccessConfig config) {
        attributeNames = new HashMap<String, String>();
        Enumeration enumTemplate = config.enumerateTemplate();
        while (enumTemplate.hasMoreElements()) {
            Template template = (Template) enumTemplate.nextElement();
            Enumeration enumAttribute = template.enumerateAttributeRule();
            while (enumAttribute.hasMoreElements()) {
                AttributeRule attribute = (AttributeRule) enumAttribute.nextElement();
                String attributeCode = attribute.getAttribute_code();
                if (!attributeNames.containsKey(attributeCode)) {
                    attributeNames.put(attributeCode, attribute.getAttr_name_rus());
                }
            }
        }
    }

    public String getAttributeName(String attrCode){
        if (attrCode == null) return null;
        String attributeName = attributeNames.get(attrCode);
        if (attributeName == null) {
            logger.debug("Unknown attribute code " + attrCode);
        }
        return attributeName == null ? attrCode : attributeName;
    }

}
