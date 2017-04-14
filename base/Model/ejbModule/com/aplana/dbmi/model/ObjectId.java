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
package com.aplana.dbmi.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;

/**
 * One of central classes in project.
 * Represents unique identifier of {@link DataObject object stored in database}.
 * Contains information about database object type (usually it is {@link DataObject} descendant)
 * and it's primary key in DB
 * <br>
 * Primary key information stored in Id property could be represented by using simple
 * types like {@link java.lang.Long} or {@link java.lang.String}
 * as well as complex user-defined objects encapsulating complex primary keys information
 * <br>
 * Sometimes it's useful to have set of predefined object identifiers of some system objects.
 * Such predefined constants could be defined in special objectids.properties file.
 * This file could be located in com/aplana/dbmi/model package, or in Portal configuration folder.
 * Such predefined identifiers could be accessed through static method {@link ObjectId#predefined(Class, String)}:
 */
public class ObjectId implements Serializable
{
	private static final long serialVersionUID = 1L;
	private Class<?> type;
	private Object id;

	/**
	 * Constructor
	 * @param type type of data object. Should be descendant of {@link DataObject}
	 * @param id object encapsulating information about object's primary key. 
	 */
	public ObjectId (Class<?> type, Object id)
	{
		this.type = type;
		this.id = id;
	}
	
	/**
	 * Simplified version of constructor that could be used for objects with numeric primary keys 
	 * @param type type of data object. Should be descendant of {@link DataObject}
	 * @param id numeric primary key
	 */
	public ObjectId (Class<?> type, long id)
	{
		this.type = type;
		this.id = new Long(id);
	}
	
	/**
	 * Gets identifier of object 
	 * @return identifier of object
	 */
	public Object getId() {
		return id;
	}

	/**
	 * Gets type of object
	 * @return type of object
	 */
	@SuppressWarnings("rawtypes")
	public Class getType() {
		return type;
	}

	/**
	 * Checks if this ObjectId is equal to given one.
	 * @param obj object to compare with
	 * @return true if type and identity information of compared objects is equals.
	 * Returns false if obj is not ObjectId instance or have different type or id 
	 */
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof ObjectId)) return false;
		final ObjectId other = (ObjectId) obj;
		return 	   ( (type != null) ? type.equals(other.getType()) :  null == other.getType())
				&& ( (id != null) ? id.equals(other.getId()) : null == other.getId());
	}

	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return 		( (type != null) ? type.hashCode() : 87126348) 
				 ^	( (id != null) ? id.hashCode() : 516386987);
	}

	/**
	 * Returns String representation of ObjectId in form of '<TYPE_NAME>:<ID>'
	 * @return String representation of ObjectId
	 */
	public String toString() {
		return type.getName() + ":" + id;
	}
	
	private static final String STANDARD_IDS = "com/aplana/dbmi/model/objectids.properties";
	private static final String CONFIG_FOLDER = "dbmi/";
	private static final String PREDEFINED_IDS = CONFIG_FOLDER+"objectids.properties";
	private static final String OTHER_PREDEFINED_IDS_PATTERN = "*-objectids.properties";
	private static Log logger = LogFactory.getLog(ObjectId.class);
	
	private static Properties stdIds = new Properties();
	static {
		try {
			InputStream in_standart = ObjectId.class.getClassLoader().getResourceAsStream(STANDARD_IDS);
			if (in_standart != null)
				stdIds.load(in_standart);
				
			InputStream in_predefined = Portal.getFactory().getConfigService().loadConfigFile(PREDEFINED_IDS);
			if (in_predefined != null)
				stdIds.load(in_predefined);
		
			List<InputStream> otherConfigs = Portal.getFactory().getConfigService().loadMultipleConfigFiles(CONFIG_FOLDER,OTHER_PREDEFINED_IDS_PATTERN);
			for(InputStream stream: otherConfigs){
				if (otherConfigs != null)
					stdIds.load(stream);
			}
		
		} catch (IOException e) {
			logger.warn("Where is problem by loading files with predefined Ids because: \"" + e.getMessage() + "\"");
		} catch (Exception e) {
			logger.error("Where is problem by loading predefined Ids", e);
		}
	}
	
	/**
	 * Returns predefined ObjectId by it's type and key name.
	 * Set of predefined identifiers is declared in objectids.properties file.
	 * This file could be located in com/aplana/dbmi/model package (i.e. included in jar-file),
	 * or in Portal configuration folder.
	 * <br> 
	 * objectids.properties file contains information about identifiers in form of key-value pairs. 
	 * For now it is possible to define identifiers with  String or Long types only.
	 * <br>
	 * <br>
	 * Key have the form of '&lt;CLASS_NAME&gt;.&lt;DATA_OBJECT_KEY&gt;', where <br>
	 * &lt;CLASS_NAME&gt; is a fully-qualified name of data-object type in lower-case,<br> 
	 * &lt;DATA_OBJECT_KEY&gt; is an unique string.<br>
	 * <br>
	 * <br>
	 * EXAMPLE of usage:
	 * <pre>
	 * If we have following line in objectids.properties:
	 * cardstate.draft=1
	 * 
	 * Then the following java code:
	 * ObjectId objId = ObjectId.predefined(CardState.class, "draft");
	 * is equals to:
	 * ObjectId objId = new ObjectId(CardState.class, new Long(1));
	 * </pre>
	 * @param type type of DataObject
	 * @param key key of predefined identifier without type information (&lt;DATA_OBJECT_KEY&gt;)
	 * @return predefined ObjectId instance or null if there is no predefined identifier with given key
	 */
	public static ObjectId predefined(Class<?> type, String key) {
		String resource = type.getName().substring(type.getName().lastIndexOf('.') + 1).toLowerCase() +
				"." + key;
		if (!stdIds.containsKey(resource))
			return null;
		String id = stdIds.getProperty(resource);
		try {
			return new ObjectId(type, Long.parseLong(id));
		} catch (NumberFormatException e) {
			return new ObjectId(type, id);
		}
	}
	
	public static ObjectId state(String predefinedKey) {
		return predefined(CardState.class, predefinedKey);
	}
	
	public static ObjectId template(String predefinedKey) {
		return predefined(Template.class, predefinedKey);
	}
	
	public static ObjectId workflow(String predefinedKey) {
		return predefined(Workflow.class, predefinedKey);
	}
	
	public static ObjectId workflowMove(String predefinedKey) {
		return predefined(WorkflowMove.class, predefinedKey);
	}
}
