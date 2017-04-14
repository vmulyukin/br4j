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



package com.jenkov.prizetags.base;

import com.jenkov.prizetags.util.ClassUtil;
import com.jenkov.prizetags.util.RequestUtil;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;


public class BaseTag extends TagSupport{

    protected String debug = null;

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }

    protected boolean isDebugOn(){
        return "true".equals(getDebug());
    }

    public void debug(String text) throws JspException{
        if(isDebugOn()){
            System.out.print("Debug: ");
            System.out.println(text);
            System.out.flush();
        }
    }

    protected Tag getAncestor(Class type){
        Tag parent = getParent();
        while(parent != null && !type.equals(parent.getClass())){
            parent = parent.getParent();
        }
        return parent;
    }

    protected void write(String text) throws JspException{
       if(text == null) return;
       try{
           pageContext.getOut().print(text);
       } catch (IOException e) {
           throw new JspException("Could not write to JspWriter", e);
       }
    }

    protected void write(long number) throws JspException{
       try{
           pageContext.getOut().print(number);
       } catch (IOException e) {
           throw new JspException("Could not write to JspWriter", e);
       }
    }

    /**
     * Returns the given property on the given object, if present.
     * @param object    The owner of the property to get
     * @param property  The property to get.
     * @return The bean property if found.
     * @throws JspException If an error occurs while trying to find property.
     */
    protected Object getBeanProperty(Object object, String property) throws JspException {
        try {
            return ClassUtil.getBean(object, property);
        } catch (IllegalAccessException e) {
            throw new JspException("Error getting bean: ", e);
        } catch (InvocationTargetException e) {
            throw new JspException("Error getting bean: ", e);
        }
    }

    /**
     * Returns the given bean or bean property located in the given scope.
     * @param name      The key by which the bean is stored.
     * @param property  The name of the property of the bean to return. Fx. getName, or just name
     *                  Null if the bean itself is to be returned.
     * @param scope     The scope where the bean is located. Null if you want to search all scopes.
     *                  Valid scopes are request, session, page and application
     * @return          The bean or bean property if found. Null if not found.
     * @throws JspException If anything goes wrong while trying to locate the bean.
     */
    protected Object getBean(String name, String property, String scope) throws JspException {
        try {
            return RequestUtil.findObject(pageContext, name, property, scope);
        } catch (IllegalAccessException e) {
            throw new JspException("Error finding bean: ", e);
        } catch (InvocationTargetException e) {
            throw new JspException("Error finding bean: ", e);
        }
    }

    /**
     * Returns the given bean or bean property located in the given scope. If the bean located by
     * the name / property is a java.util.Map instance, and the key parameter is not null,
     * then the key will be used to lookup the bean in the map. If the key parameter is not
     * null and the bean found by the name and property is not a Map an exception is thrown.
     *
     * If key parameter is null and the bean located by the name / property is not a Map,
     * the bean itself will be returned.
     * If property is null, the bean located by name only will be checked for being a Map instance.
     *
     * @param name      The key by which the bean is stored.
     * @param property  The name of the property of the bean to return. Fx. getName, or just name
     *                  Null if the bean itself is to be returned.
     * @param key       The key of the bean to locate in the Map instance located by name / property.
     * @param scope     The scope where the bean is located. Null if you want to search all scopes.
     *                  Valid scopes are request, session, page and application
     * @return          The bean or bean property if found. Null if not found.
     * @throws JspException If anything goes wrong while trying to locate the bean.
     */
    protected Object getBean(String name, String property, Object key, String scope) throws JspException {
        Object bean = getBean(name, property, scope);
        if(key != null & bean != null){
            if(!(bean instanceof Map))
                throw new JspException("Bean located at name=" + name + " and property=" +
                    property + " is not an instance of java.util.Map. Bean was: " + bean ==  null ?
                    " null" : bean.toString() + " (" + bean.getClass().getName() +")");
            return ((Map)bean).get(key);
        }
        return bean;
    }

    /**
     * Returns the given bean or bean property located in the given scope. If the bean located by
     * the name / property is a java.util.Map instance, and the key parameter is not null,
     * then the key will be used to lookup the bean in the map. If the key parameter is not
     * null and the bean found by the name and property is not a Map an exception is thrown.
     *
     * If key parameter is null and the bean located by the name / property is not a Map,
     * the bean itself will be returned.
     * If property is null, the bean located by name only will be checked for being a Map instance.
     *
     * @param name      The key by which the bean is stored.
     * @param property  The name of the property of the bean to return. Fx. getName, or just name
     *                  Null if the bean itself is to be returned.
     * @param key       The key of the bean to locate in the Map instance located by name / property.
     * @param scope     The scope where the bean is located. Null if you want to search all scopes.
     *                  Valid scopes are request, session, page and application
     * @return          The bean or bean property if found. Null if not found.
     * @throws JspException If anything goes wrong while trying to locate the bean.
     */
    protected Object getBean(String name, String property, Object key, String keyedObjectProperty, String scope) throws JspException {
        Object bean = getBean(name, property, key, scope);
        if(bean == null) return null;
        if(keyedObjectProperty == null) return bean;
        try {
            return getProperty(keyedObjectProperty, bean);
        } catch (NoSuchMethodException e) {
            throw new JspException("No such property " + keyedObjectProperty + " on object stored by key "
                    + key + ". Object was: " + bean.toString(), e);
        } catch (IllegalAccessException e) {
            throw new JspException("Error getting property " + keyedObjectProperty + " on object stored by key "
                    + key + ". Object was: " + bean.toString(), e);
        } catch (InvocationTargetException e) {
            throw new JspException("Error getting property " + keyedObjectProperty + " on object stored by key "
                    + key + ". Object was: " + bean.toString(), e);
        }
    }

    protected Object getProperty(String keyedObjectProperty, Object bean) throws NoSuchMethodException, JspException, IllegalAccessException, InvocationTargetException {
        String keyedObjectPropertyGetter = null;
        if(keyedObjectProperty.startsWith("get")){
            keyedObjectPropertyGetter = keyedObjectProperty;
        } else {
            keyedObjectPropertyGetter = "get" + keyedObjectProperty.substring(0,1).toUpperCase() + keyedObjectProperty.substring(1);
        }
        Method method = bean.getClass().getMethod(keyedObjectPropertyGetter, null);
        return method.invoke(bean, null);
    }

    /**
     * Stores the given bean by the given name in the given scope, meaning the bean is stored using
     * the name as key in either request, session, page, or application context attributes.
     * @param name   The name (key) to store the bean by.
     * @param scope  The scope to store the bean in. If null, defaultScope will be used. Valid scopes
     *               are "request", "session", "page" and "application".
     * @param defaultScope The defaultScope to store the bean in. If null, defaults to "request".
     * @param bean   The bean to store. If bean is null, nothing happens.
     * @throws JspException If name is null.
     */
    protected void setBean(String name, String scope, String defaultScope, Object bean) throws JspException{
        if(bean == null) return;

        if(name == null){
            throw new JspException("Cannot store bean in any scope. Name was null");
        }

        String theScope = scope;
        if(theScope == null){
            theScope = defaultScope;
        }
        if(theScope == null){
            theScope = "request";
        }

        if("request".equals(theScope)){ pageContext.getRequest().setAttribute(name, bean); }
        else if("session".equals(theScope)){ pageContext.getSession().setAttribute(name, bean); }
        else if("page"   .equals(theScope)){ pageContext.setAttribute(name, bean); }
        else if("application".equals(theScope)){ pageContext.getServletContext().setAttribute(name, bean); }
        else {
            throw new JspException("Could not set bean in any scope. Invalid scope: " +
                    theScope + ". Valid scopes are 'request', 'session', 'page', and 'application'.");
        }
    }

}
