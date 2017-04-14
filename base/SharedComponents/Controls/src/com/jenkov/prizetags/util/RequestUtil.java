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



package com.jenkov.prizetags.util;

import javax.servlet.jsp.PageContext;
import java.lang.reflect.InvocationTargetException;

public class RequestUtil {


    public static Object findObject(PageContext pageContext, String name, String property, String scope) throws IllegalAccessException, InvocationTargetException {
        Object bean = findObject(pageContext, name, scope);
        if(property != null){
            return ClassUtil.getBean(bean, property);
        }
        return bean;
    }

    /**
     * Searches request, session, page and application attributes for an object stored under the
     * given name (name = attribute key). If one is found it is returned. If scope is set to
     * null, all four attribute sets will be searched. If scope is not null only the attribute
     * collection it represents will searched.
     * @param pageContext
     * @param name
     * @param scope null, or "request", "session", "page", "application" for specific attribute sets.
     *              "attribute" means servlet context attributes.
     * @return The first found object stored by a key matching the given name.
     */
    public static Object findObject(PageContext pageContext, String name, String scope){
        Object o = null;
        if(scope == null){
            o = pageContext.getRequest().getAttribute(name);
            if(o != null) return o;

            o = pageContext.getSession().getAttribute(name);
            if(o != null) return o;

            o = pageContext.getAttribute(name);
            if(o != null) return o;

            o = pageContext.getServletContext().getAttribute(name);
            if(o != null) return o;

            return null;
        }
        if(scope.equals("request")){
            return pageContext.getRequest().getAttribute(name);
        }
        if(scope.equals("session")){
            return pageContext.getSession().getAttribute(name);
        }
        if(scope.equals("page")){
            return pageContext.getAttribute(name);
        }
        if(scope.equals("application")){
            return pageContext.getServletContext().getAttribute(name);
        }

        return null;
    }

    public static void storeObject(PageContext pageContext, String name, String scope, Object object){
        if(scope == null || scope.equals("request")){
            pageContext.getRequest().setAttribute(name, object);
            return;
        }
        if(scope.equals("session")){
            pageContext.getSession().setAttribute(name, object);
            return;
        }
        if(scope.equals("page")){
            pageContext.setAttribute(name, object);
            return;
        }
        if(scope.equals("application")){
            pageContext.getServletContext().setAttribute(name, object);
        }
    }

    public static void removeObject(PageContext pageContext, String name, String scope){
        if(scope == null || scope.equals("request")){
            pageContext.getRequest().removeAttribute(name);
            return;
        }
        if(scope.equals("session")){
            pageContext.getSession().removeAttribute(name);
            return;
        }
        if(scope.equals("page")){
            pageContext.removeAttribute(name);
            return;
        }
        if(scope.equals("application")){
            pageContext.removeAttribute(name);
            return;
        }
    }


}
