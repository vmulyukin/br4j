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

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

public abstract class AbstractTag extends TagSupport {

    public static final String ID_ATTRIBUTE = "id";
    public static final String NAME_ATTRIBUTE = "name";

    public static final String CLASS_ATTRIBUTE = "class";

    public static final String STYLE_ATTRIBUTE = "style";

    public static final String LANG_ATTRIBUTE = "lang";

    public static final String TITLE_ATTRIBUTE = "title";

    public static final String DIR_ATTRIBUTE = "dir";

    public static final String TABINDEX_ATTRIBUTE = "tabindex";

    public static final String ONCLICK_ATTRIBUTE = "onclick";

    public static final String ONDBLCLICK_ATTRIBUTE = "ondblclick";

    public static final String ONMOUSEDOWN_ATTRIBUTE = "onmousedown";

    public static final String ONMOUSEUP_ATTRIBUTE = "onmouseup";

    public static final String ONMOUSEOVER_ATTRIBUTE = "onmouseover";

    public static final String ONMOUSEMOVE_ATTRIBUTE = "onmousemove";

    public static final String ONMOUSEOUT_ATTRIBUTE = "onmouseout";

    public static final String ONKEYPRESS_ATTRIBUTE = "onkeypress";

    public static final String ONKEYUP_ATTRIBUTE = "onkeyup";

    public static final String ONKEYDOWN_ATTRIBUTE = "onkeydown";

    private String id;
    private String name;


    private String cssClass;

    private String cssErrorClass;

    private String cssStyle;

    private String lang;

    private String title;

    private String dir;

    private String tabindex;

    private String onclick;

    private String ondblclick;

    private String onmousedown;

    private String onmouseup;

    private String onmouseover;

    private String onmousemove;

    private String onmouseout;

    private String onkeypress;

    private String onkeyup;

    private String onkeydown;

    private String disabled;


    /**
     * Set the value of the '<code>class</code>' attribute.
     * May be a runtime expression.
     */
    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    /**
     * Get the value of the '<code>class</code>' attribute.
     * May be a runtime expression.
     */
    protected String getCssClass() {
        return this.cssClass;
    }

    /**
     * The CSS class to use when the field bound to a particular tag has errors.
     * May be a runtime expression.
     */
    public void setCssErrorClass(String cssErrorClass) {
        this.cssErrorClass = cssErrorClass;
    }

    /**
     * The CSS class to use when the field bound to a particular tag has errors.
     * May be a runtime expression.
     */
    protected String getCssErrorClass() {
        return this.cssErrorClass;
    }

    /**
     * Set the value of the '<code>style</code>' attribute.
     * May be a runtime expression.
     */
    public void setCssStyle(String cssStyle) {
        this.cssStyle = cssStyle;
    }

    /**
     * Get the value of the '<code>style</code>' attribute.
     * May be a runtime expression.
     */
    protected String getCssStyle() {
        return this.cssStyle;
    }

    /**
     * Set the value of the '<code>lang</code>' attribute.
     * May be a runtime expression.
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * Get the value of the '<code>lang</code>' attribute.
     * May be a runtime expression.
     */
    protected String getLang() {
        return this.lang;
    }

    /**
     * Set the value of the '<code>title</code>' attribute.
     * May be a runtime expression.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the value of the '<code>title</code>' attribute.
     * May be a runtime expression.
     */
    protected String getTitle() {
        return this.title;
    }

    /**
     * Set the value of the '<code>dir</code>' attribute.
     * May be a runtime expression.
     */
    public void setDir(String dir) {
        this.dir = dir;
    }

    /**
     * Get the value of the '<code>dir</code>' attribute.
     * May be a runtime expression.
     */
    protected String getDir() {
        return this.dir;
    }

    /**
     * Set the value of the '<code>tabindex</code>' attribute.
     * May be a runtime expression.
     */
    public void setTabindex(String tabindex) {
        this.tabindex = tabindex;
    }

    /**
     * Get the value of the '<code>tabindex</code>' attribute.
     * May be a runtime expression.
     */
    protected String getTabindex() {
        return this.tabindex;
    }

    /**
     * Set the value of the '<code>onclick</code>' attribute.
     * May be a runtime expression.
     */
    public void setOnclick(String onclick) {
        this.onclick = onclick;
    }

    /**
     * Get the value of the '<code>onclick</code>' attribute.
     * May be a runtime expression.
     */
    protected String getOnclick() {
        return this.onclick;
    }

    /**
     * Set the value of the '<code>ondblclick</code>' attribute.
     * May be a runtime expression.
     */
    public void setOndblclick(String ondblclick) {
        this.ondblclick = ondblclick;
    }

    /**
     * Get the value of the '<code>ondblclick</code>' attribute.
     * May be a runtime expression.
     */
    protected String getOndblclick() {
        return this.ondblclick;
    }

    /**
     * Set the value of the '<code>onmousedown</code>' attribute.
     * May be a runtime expression.
     */
    public void setOnmousedown(String onmousedown) {
        this.onmousedown = onmousedown;
    }

    /**
     * Get the value of the '<code>onmousedown</code>' attribute.
     * May be a runtime expression.
     */
    protected String getOnmousedown() {
        return this.onmousedown;
    }

    /**
     * Set the value of the '<code>onmouseup</code>' attribute.
     * May be a runtime expression.
     */
    public void setOnmouseup(String onmouseup) {
        this.onmouseup = onmouseup;
    }

    /**
     * Get the value of the '<code>onmouseup</code>' attribute.
     * May be a runtime expression.
     */
    protected String getOnmouseup() {
        return this.onmouseup;
    }

    /**
     * Set the value of the '<code>onmouseover</code>' attribute.
     * May be a runtime expression.
     */
    public void setOnmouseover(String onmouseover) {
        this.onmouseover = onmouseover;
    }

    /**
     * Get the value of the '<code>onmouseover</code>' attribute.
     * May be a runtime expression.
     */
    protected String getOnmouseover() {
        return this.onmouseover;
    }

    /**
     * Set the value of the '<code>onmousemove</code>' attribute.
     * May be a runtime expression.
     */
    public void setOnmousemove(String onmousemove) {
        this.onmousemove = onmousemove;
    }

    /**
     * Get the value of the '<code>onmousemove</code>' attribute.
     * May be a runtime expression.
     */
    protected String getOnmousemove() {
        return this.onmousemove;
    }

    /**
     * Set the value of the '<code>onmouseout</code>' attribute.
     * May be a runtime expression.
     */
    public void setOnmouseout(String onmouseout) {
        this.onmouseout = onmouseout;
    }
    /**
     * Get the value of the '<code>onmouseout</code>' attribute.
     * May be a runtime expression.
     */
    protected String getOnmouseout() {
        return this.onmouseout;
    }

    /**
     * Set the value of the '<code>onkeypress</code>' attribute.
     * May be a runtime expression.
     */
    public void setOnkeypress(String onkeypress) {
        this.onkeypress = onkeypress;
    }

    /**
     * Get the value of the '<code>onkeypress</code>' attribute.
     * May be a runtime expression.
     */
    protected String getOnkeypress() {
        return this.onkeypress;
    }

    /**
     * Set the value of the '<code>onkeyup</code>' attribute.
     * May be a runtime expression.
     */
    public void setOnkeyup(String onkeyup) {
        this.onkeyup = onkeyup;
    }

    /**
     * Get the value of the '<code>onkeyup</code>' attribute.
     * May be a runtime expression.
     */
    protected String getOnkeyup() {
        return this.onkeyup;
    }

    /**
     * Set the value of the '<code>onkeydown</code>' attribute.
     * May be a runtime expression.
     */
    public void setOnkeydown(String onkeydown) {
        this.onkeydown = onkeydown;
    }

    /**
     * Get the value of the '<code>onkeydown</code>' attribute.
     * May be a runtime expression.
     */
    protected String getOnkeydown() {
        return this.onkeydown;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    protected void writeOptionalAttributes(TagWriter tagWriter) throws JspException {
        tagWriter.writeOptionalAttributeValue(ID_ATTRIBUTE, getId());
        tagWriter.writeOptionalAttributeValue(NAME_ATTRIBUTE, getName());
        tagWriter.writeOptionalAttributeValue(CLASS_ATTRIBUTE, getCssClass());
        tagWriter.writeOptionalAttributeValue(STYLE_ATTRIBUTE,
                ObjectUtils.getDisplayString(evaluate("cssStyle", getCssStyle())));
        writeOptionalAttribute(tagWriter, LANG_ATTRIBUTE, getLang());
        writeOptionalAttribute(tagWriter, TITLE_ATTRIBUTE, getTitle());
        writeOptionalAttribute(tagWriter, DIR_ATTRIBUTE, getDir());
        writeOptionalAttribute(tagWriter, TABINDEX_ATTRIBUTE, getTabindex());
        writeOptionalAttribute(tagWriter, ONCLICK_ATTRIBUTE, getOnclick());
        writeOptionalAttribute(tagWriter, ONDBLCLICK_ATTRIBUTE, getOndblclick());
        writeOptionalAttribute(tagWriter, ONMOUSEDOWN_ATTRIBUTE, getOnmousedown());
        writeOptionalAttribute(tagWriter, ONMOUSEUP_ATTRIBUTE, getOnmouseup());
        writeOptionalAttribute(tagWriter, ONMOUSEOVER_ATTRIBUTE, getOnmouseover());
        writeOptionalAttribute(tagWriter, ONMOUSEMOVE_ATTRIBUTE, getOnmousemove());
        writeOptionalAttribute(tagWriter, ONMOUSEOUT_ATTRIBUTE, getOnmouseout());
        writeOptionalAttribute(tagWriter, ONKEYPRESS_ATTRIBUTE, getOnkeypress());
        writeOptionalAttribute(tagWriter, ONKEYUP_ATTRIBUTE, getOnkeyup());
        writeOptionalAttribute(tagWriter, ONKEYDOWN_ATTRIBUTE, getOnkeydown());
    }




    protected TagWriter createTagWriter() {
        return new TagWriter(pageContext);
    }
    protected abstract int writeTagContent(TagWriter tagwriter) throws Exception;

    public final int doStartTag() throws JspException {
        try {
            return writeTagContent(createTagWriter());
        }
        catch (JspException ex) {
            throw ex;
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new JspTagException(ex.getMessage());
        }
    }

    protected Object evaluate(String attributeName, Object value) throws JspException {
/*  50*/        if(value instanceof String)
/*  51*/            return ExpressionEvaluationUtils.evaluate(attributeName, (String)value, pageContext);
/*  54*/        else
/*  54*/            return value;
    }


    protected final void writeOptionalAttribute(TagWriter tagWriter, String attributeName, String value) throws JspException {
/*  69*/        if(value != null)
/*  70*/            tagWriter.writeOptionalAttributeValue(attributeName, evaluate(attributeName, value).toString());
    }
    protected String convertToString(Object obj) {
/* 165*/        return obj != null ? obj.toString() : "";
    }

    public String getDisabled() {
        return disabled;
    }

    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }
}
