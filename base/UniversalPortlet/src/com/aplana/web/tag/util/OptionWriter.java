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
package com.aplana.web.tag.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.JspException;


public class OptionWriter {

    private final Object optionSource;


    private final String valueProperty;

    private final String labelProperty;

    private final boolean htmlEscape;


    public OptionWriter(
            Object optionSource, String valueProperty, String labelProperty, boolean htmlEscape) {

        this.optionSource = optionSource;

        this.valueProperty = valueProperty;
        this.labelProperty = labelProperty;
        this.htmlEscape = htmlEscape;
    }

    /**
     * Write the '<code>option</code>' tags for the configured {@link #optionSource} to
     * the supplied {@link TagWriter}.
     */
    public void writeOptions(TagWriter tagWriter) throws JspException {
        if (this.optionSource.getClass().isArray()) {
            renderFromArray(tagWriter);
        } else if (this.optionSource instanceof Collection) {
            renderFromCollection(tagWriter);
        } else {
            throw new JspException(
                    "Type [" + this.optionSource.getClass().getName() + "] is not valid for option items");
        }
    }

    /**
     * Renders the inner '<code>option</code>' tags using the {@link #optionSource}.
     *
     * @see #doRenderFromCollection(java.util.Collection,TagWriter)
     */
    private void renderFromArray(TagWriter tagWriter) throws JspException {
        doRenderFromCollection(CollectionUtils.arrayToList(this.optionSource), tagWriter);
    }

    /**
     * Renders the inner '<code>option</code>' tags using the supplied
     * {@link Map} as the source.
     *
     * @see #renderOption(TagWriter,Object,Object,Object)
     */

    /**
     * Renders the inner '<code>option</code>' tags using the {@link #optionSource}.
     *
     * @see #doRenderFromCollection(java.util.Collection,TagWriter)
     */
    private void renderFromCollection(TagWriter tagWriter) throws JspException {
        doRenderFromCollection((Collection) this.optionSource, tagWriter);
    }

    /**
     * Renders the inner '<code>option</code>' tags using the supplied {@link Collection} of
     * objects as the source. The value of the {@link #valueProperty} field is used
     * when rendering the '<code>value</code>' of the '<code>option</code>' and the value of the
     * {@link #labelProperty} property is used when rendering the label.
     */
    private void doRenderFromCollection(Collection optionCollection, TagWriter tagWriter) throws JspException {
        for (Iterator it = optionCollection.iterator(); it.hasNext();) {
            Object item = it.next();
            Object value = null;
            Object label = null;

            if (item instanceof SelectedItem) {
                SelectedItem curItem = (SelectedItem) item;
                value = curItem.getValue();
                label = curItem.getLabel();
            } else {
                value = item;
                label = item;

            }
            renderOption(tagWriter, valueProperty, value, label);
        }

    }

    /**
     * Renders an HTML '<code>option</code>' with the supplied value and label. Marks the
     * value as 'selected' if either the item itself or its value match the bound value.
     */
    private void renderOption(TagWriter tagWriter, Object item, Object value, Object label) throws JspException {
        tagWriter.startTag("option");
        writeCommonAttributes(tagWriter);

        String valueDisplayString = getDisplayString(value);
        String labelDisplayString = getDisplayString(label);

        // allows render values to handle some strange browser compat issues.
        tagWriter.writeAttribute("value", valueDisplayString);

        if (isOptionSelected(item, value)) {
            tagWriter.writeAttribute("selected", "selected");
        }
        if (isOptionDisabled()) {
            tagWriter.writeAttribute("disabled", "disabled");
        }
        if (label != null) {
            tagWriter.appendValue(labelDisplayString);
            tagWriter.endTag();
        }
    }

    /**
     * Determines the display value of the supplied <code>Object</code>,
     * HTML-escaped as required.
     */
    private String getDisplayString(Object value) {
        return value != null ? value.toString() : null;
    }

    private boolean isOptionSelected(Object value, Object resolvedValue) {
        return SelectedValueComparator.isSelected(value, resolvedValue);
    }

    /**
     * Determine whether the option fields should be disabled.
     */
    protected boolean isOptionDisabled() {
        return false;
    }

    /**
     * Writes default attributes configured to the supplied {@link TagWriter}.
     */
    protected void writeCommonAttributes(TagWriter tagWriter) throws JspException {
	}

}
