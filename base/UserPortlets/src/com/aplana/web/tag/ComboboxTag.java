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
package com.aplana.web.tag;


import com.aplana.web.tag.util.TagWriter;
import com.aplana.web.tag.util.AbstractTag;
import com.aplana.web.tag.util.ObjectUtils;
import com.aplana.web.tag.util.OptionWriter;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Collection;
import java.util.Map;

public class ComboboxTag extends AbstractTag {

    private static final Object EMPTY = new Object();
    private Object items;


    /**
     * The name of the property mapped to the '<code>value</code>' attribute
     * of the '<code>option</code>' tag.
     */
    private String itemValue;

    /**
     * The name of the property mapped to the inner text of the
     * '<code>option</code>' tag.
     */
    private String itemLabel;

    /**
     * The value of the HTML '<code>size</code>' attribute rendered
     * on the final '<code>select</code>' element.
     */
    private String size;

    /**
     * Indicates whether or not the '<code>select</code>' tag allows
     * multiple-selections.
     */

    private TagWriter tagWriter;


    /**
     * Set the {@link Collection}, {@link Map} or array of objects used to
     * generate the inner '<code>option</code>' tags.
     * <p>Required when wishing to render '<code>option</code>' tags from
     * an array, {@link Collection} or {@link Map}.
     * <p>Typically a runtime expression.
     *
     * @param items the items that comprise the options of this selection
     */
    public void setItems(Object items) {
        this.items = (items != null ? items : EMPTY);
    }

    /**
     * Get the value of the '<code>items</code>' attribute.
     * <p>May be a runtime expression.
     */
    protected Object getItems() {
        return this.items;
    }

    /**
     * Set the name of the property mapped to the '<code>value</code>'
     * attribute of the '<code>option</code>' tag.
     * <p>Required when wishing to render '<code>option</code>' tags from
     * an array or {@link Collection}.
     * <p>May be a runtime expression.
     */
    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    /**
     * Get the value of the '<code>itemValue</code>' attribute.
     * <p>May be a runtime expression.
     */
    protected String getItemValue() {
        return this.itemValue;
    }

    /**
     * Set the name of the property mapped to the label (inner text) of the
     * '<code>option</code>' tag.
     * <p>May be a runtime expression.
     */
    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    /**
     * Get the value of the '<code>itemLabel</code>' attribute.
     * <p>May be a runtime expression.
     */
    protected String getItemLabel() {
        return this.itemLabel;
    }

    /**
     * Set the value of the HTML '<code>size</code>' attribute rendered
     * on the final '<code>select</code>' element.
     * <p>May be a runtime expression.
     *
     * @param size the desired value of the '<code>size</code>' attribute
     */
    public void setSize(String size) {
        this.size = size;
    }

    /**
     * Get the value of the '<code>size</code>' attribute.
     * <p>May be a runtime expression.
     */
    protected String getSize() {
        return this.size;
    }


    protected int writeTagContent(TagWriter tagWriter) throws JspException {
        tagWriter.startTag("select");
        writeOptionalAttributes(tagWriter);
        tagWriter.writeOptionalAttributeValue("size", getSize());

        Object items = getItems();
        if (items != null) {
            // Items specified, but might still be empty...
            if (items != EMPTY) {
                Object itemsObject = (items instanceof String ? evaluate("items", (String) items) : items);
                if (itemsObject != null) {
                    String valueProperty = (getItemValue() != null ?
                            ObjectUtils.getDisplayString(evaluate("itemValue", getItemValue())) : null);
                    String labelProperty = (getItemLabel() != null ?
                            ObjectUtils.getDisplayString(evaluate("itemLabel", getItemLabel())) : null);
                    OptionWriter optionWriter =
                            new OptionWriter(itemsObject, valueProperty, labelProperty, false);
                    optionWriter.writeOptions(tagWriter);
                }
            }
            tagWriter.endTag(true);
        }
        return EVAL_PAGE;
    }
}
