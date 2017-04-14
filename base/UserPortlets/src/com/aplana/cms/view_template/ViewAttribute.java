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
package com.aplana.cms.view_template;

import com.aplana.cms.AppContext;
import com.aplana.dbmi.model.workstation.AttributeValueCorrector;

/**
 * Represents single attribute of card for views
 * @author rmitenkov
 */
public class ViewAttribute {

    public static final String ATTRIBUTE_ID_PREFIX = "attr";

    private String code;
    private String linkedCode;
    private String linkedByPerson;
    private String nameRu;
    private String nameEn;
    private String type;
    private AttributeValueCorrector corrector;

	public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLinkedCode() {
        return linkedCode;
    }

    public void setLinkedCode(String linkedCode) {
        this.linkedCode = linkedCode;
    }

    public String isLinkedByPerson() {
        return linkedByPerson;
    }

    public void setLinkedByPerson(String linkedByPerson) {
        this.linkedByPerson = linkedByPerson;
    }

    public static ViewAttribute getBean(String attributeId){
        if (containsBean(attributeId)) {
            return (ViewAttribute) AppContext.getApplicationContext().getBean(buildViewId(attributeId));
        }
        return null;
    }

    public static boolean containsBean(String attributeId){
        if (AppContext.getApplicationContext() == null) return false;
        return AppContext.getApplicationContext().containsBean(buildViewId(attributeId));
    }


    private static String buildViewId(String viewId) {
        return ATTRIBUTE_ID_PREFIX + viewId;
    }
    
    public AttributeValueCorrector getCorrector() {
		return corrector;
	}

	public void setCorrector(AttributeValueCorrector corrector) {
		this.corrector = corrector;
	}
}

