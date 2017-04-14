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
/*
    Copyright 2004 Jenkov Development

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/



package com.jenkov.prizetags.bean.impl;

import com.jenkov.prizetags.base.NamePropertyTag;
import com.jenkov.prizetags.html.impl.HtmlEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * A tag that is capable of writing the contents of a bean (bean.toString()) to the JSP output stream.
 * You can set the name, property and scope attributes which dictate which bean or bean property
 * to write, and the scope attribute that tells in which scope to find the bean.
 *
 * <br/><br/>
 * Valid scopes are request, session, page and application, which coresponds to the request
 * attributes, session attributes, page attributes and servlet context attributes.
 *
 * <br/><br/>
 * The format attribute specifies the format to use when writing the bean value out. Valid formats
 * are those accepted by the SimpleDateFormat class and the DecimalFormat class.
 *
 * <br/><br/>
 * The textStyle attribute specifies whether the text should have html tags displayed as html,
 * or encoded so they are displayed as regular text. 
 *
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public class BeanWriteTag extends NamePropertyTag{

    protected Locale locale        = null;
    protected String format        = null;
    protected String textStyle     = null;
    protected String parameter     = null;
    protected String cookie        = null;
    protected String maxLength     = null;
    protected int    maxLengthInt  = -1;
    protected String tooLongSuffix = null;

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getTextStyle() {
        return textStyle;
    }

    public void setTextStyle(String textStyle) {
        this.textStyle = textStyle;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(String maxLength) {
        this.maxLength = maxLength;
        this.maxLengthInt = Integer.parseInt(maxLength);
    }

    public String getTooLongSuffix() {
        return tooLongSuffix;
    }

    public void setTooLongSuffix(String tooLongSuffix) {
        this.tooLongSuffix = tooLongSuffix;
    }

    public int doStartTag() throws JspException {

        Object bean = null;
        if(getName() != null){
            bean = getBean(name, property, key, keyedObjectProperty, scope);
        } else if(getParameter() != null){
            bean = pageContext.getRequest().getParameter(getParameter());
        } else if(getCookie() != null){
            bean = getCookie(bean);
        }

        if(bean != null){
            if(this.format != null){
                writeFormatted(bean);
            } else {
                writeEncoded(bean.toString());
            }
        }
        return SKIP_BODY;
    }

    private Object getCookie(Object bean) {
        Cookie[] cookies = ((HttpServletRequest) pageContext.getRequest()).getCookies();
        for(int i=0; i < cookies.length; i++){
            if(getCookie().equals(cookies[i].getName())){
                bean = cookies[i].getValue();
                break;
            }
        }
        return bean;
    }

    private void writeFormatted(Object bean) throws JspException {
        if(isDate(bean)){
            SimpleDateFormat formatter = createDateFormatter();
            writeEncoded(formatter.format(bean));
        } else if(isNumber(bean)){
            NumberFormat formatter = createNumberFormatter();
            writeEncoded(formatter.format(bean));
        }
    }

    private SimpleDateFormat createDateFormatter() {
        if(this.locale != null && this.format != null){
            return new SimpleDateFormat(this.format, this.locale);
        }
        return new SimpleDateFormat(this.format);
    }

    private DecimalFormat createNumberFormatter() {
        if(this.locale != null && this.format != null){
            return new DecimalFormat(this.format, new DecimalFormatSymbols(this.locale));
        }
        return new DecimalFormat(this.format);
    }

    private void writeEncoded(String text) throws JspException {
        if("true".equals(this.textStyle)){
             write(adjustToMaxLength(new HtmlEncoder().textStyleEncode(text)));
        } else {
            write(adjustToMaxLength(text));
        }
    }


    private boolean isNumber(Object bean) {
        return (bean instanceof Byte)    ||
               (bean instanceof Short)   ||
               (bean instanceof Integer) ||
               (bean instanceof Long)    ||
               (bean instanceof Float)   ||
               (bean instanceof Double)  ||
               (bean instanceof BigDecimal) ||
               (bean instanceof BigInteger);
    }

    private boolean isDate(Object bean) {
        return (bean instanceof java.util.Date) || (bean instanceof java.sql.Date)
                || (bean instanceof java.sql.Timestamp);
    }

    private String adjustToMaxLength(String message){
        if(message.length() > this.maxLengthInt && this.maxLengthInt > -1) {
            if(getTooLongSuffix() != null){
                if(this.maxLengthInt - getTooLongSuffix().length() > 0){
                    return message.substring(0, this.maxLengthInt - getTooLongSuffix().length()) + getTooLongSuffix();
                }
                return getTooLongSuffix();
            } else {
                return message.substring(0, this.maxLengthInt);
            }
        }
        return message;
    }
}
