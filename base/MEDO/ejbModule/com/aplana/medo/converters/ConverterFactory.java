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
package com.aplana.medo.converters;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Factory that allows to create instance of <code>Converter</code> class
 * using its name.
 */
public class ConverterFactory {

    private static final String CONVERTER_KEY_PREFIX = "src.converter.";

    private static ConverterFactory instance = null;

    private Map<String, Constructor<?>> constructors = new HashMap<String, Constructor<?>>();

    private Log logger = LogFactory.getLog(getClass());

    private ConverterFactory() {
    }

    public static ConverterFactory instance() {
	if (instance == null) {
	    instance = new ConverterFactory();
	}
	return instance;
    }

    /**
     * Creates new instance of <code>Converter</code> class according to given
     * name
     * 
     * @param properties -
     *                configuration that should define class for given converter
     *                name: property with name like '{@link #CONVERTER_KEY_PREFIX}.&lt;name&gt;').
     *                Also this <code>properties</code> are used to create new
     *                instance of <code>Converter</code>
     * 
     * @param name -
     *                processor name
     * @return new instance of class created using properties
     * @throws ConverterException
     * @see Converter
     */
    public Converter createConverter(Properties properties, String name)
	    throws ConverterException {

	Constructor<?> constructor = null;

	String className = properties.getProperty(CONVERTER_KEY_PREFIX + name);

	if (className == null || "".equals(className))
	    return null;

	if (constructors.containsKey(className)) {
	    constructor = constructors.get(className);
	} else {
	    Class<?> clazz;
	    try {
		clazz = Class.forName(className);
	    } catch (ClassNotFoundException ex) {
		throw new ConverterException(ex);
	    }

	    if (!Converter.class.isAssignableFrom(clazz)) {
		logger.error(String.format(
			"Class %s is not instance of Converter", className));
		throw new ConverterException();
	    }

	    try {
		constructor = clazz.getConstructor(Properties.class,
			String.class);
	    } catch (SecurityException ex) {
		throw new ConverterException(ex);
	    } catch (NoSuchMethodException ex) {
		throw new ConverterException(ex);
	    }
	    constructors.put(className, constructor);
	}
	try {
	    return (Converter) constructor.newInstance(properties, name);
	} catch (IllegalArgumentException ex) {
	    throw new ConverterException(ex);
	} catch (InstantiationException ex) {
	    throw new ConverterException(ex);
	} catch (IllegalAccessException ex) {
	    throw new ConverterException(ex);
	} catch (InvocationTargetException ex) {
	    throw new ConverterException(ex);
	}
    }
}
