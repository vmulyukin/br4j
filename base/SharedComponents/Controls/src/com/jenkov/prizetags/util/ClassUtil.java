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

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public class ClassUtil {

    public static Object getBean(Object owner, String propertyName)
    throws IllegalAccessException, InvocationTargetException {
        if(owner == null){
            throw new NullPointerException("Object (owner) to find property in was null");
        }
        Method property = null;
        property = getProperty(owner, propertyName);

        //if first property name guess failed, try again with "get".
        if(property == null){
            property = getProperty(owner, toGetterName(propertyName, "get")) ;
        }

        //if first two property guesses failed, try again with "is"
        if(property == null){
            property = getProperty(owner, toGetterName(propertyName, "is"));
        }

        if(property == null){
            throw new IllegalArgumentException("Object of class " + owner.getClass() +
                    " does not have a property called " + propertyName);
        }

        return property.invoke(owner, null);
    }

    private static Method getProperty(Object owner, String propertyName) {
        try {
            return owner.getClass().getMethod(propertyName, null);
        } catch (NoSuchMethodException e) {
            //ignore, we will try to add "get" in front of the property and try again
        }
        return null;
    }

    private static String toGetterName(String propertyName, String prefix) {
        return prefix + propertyName.substring(0,1).toUpperCase() + propertyName.substring(1);
    }
}
