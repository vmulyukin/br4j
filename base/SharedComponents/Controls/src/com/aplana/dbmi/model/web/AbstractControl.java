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
package com.aplana.dbmi.model.web;

import java.io.Serializable;

import com.aplana.dbmi.model.ContextProvider;

public abstract class AbstractControl implements Serializable {

    private String label;

    private String name;

    private String labelRu;

    private String labelEn;

    public AbstractControl() {

    }

    public AbstractControl(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public String getLabel() {
        return label != null ? label : ContextProvider.getContext().getLocaleString(labelRu, labelEn);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Boolean getIsCheckboxControl() {
        boolean condition = this instanceof CheckboxControl;
        return Boolean.valueOf(condition);
    }

    public Boolean getIsComboboxControl() {
        boolean condition = this instanceof ComboboxControl;
        return Boolean.valueOf(condition);
    }

    public Boolean getIsTreeControl() {
        boolean condition = this instanceof TreeControl;
        return Boolean.valueOf(condition);
    }

    public Boolean getIsTextControl() {
        return Boolean.valueOf(this instanceof TextControl);
    }

    public Boolean getIsTextareaControl() {
        return Boolean.valueOf(this instanceof TextControl);
    }

    public Boolean getIsCalendarControl() {
        return Boolean.valueOf(this instanceof CalendarControl);
    }

    public Boolean getIsIntegerControl() {
        return Boolean.valueOf(this instanceof IntegerControl);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabelEn() {
        return labelEn;
    }

    public void setLabelEn(String labelEn) {
        this.labelEn = labelEn;
    }

    public String getLabelRu() {
        return labelRu;
    }

    public void setLabelRu(String labelRu) {
        this.labelRu = labelRu;
    }
}
