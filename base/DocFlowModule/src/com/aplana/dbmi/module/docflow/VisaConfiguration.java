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
package com.aplana.dbmi.module.docflow;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;

public class VisaConfiguration {
	public static final String MOVE_PROCEED = "document.wfm.proceed";
	public static final String MOVE_RETURN = "document.wfm.return";
	public static final String ATTR_VISAS = "document.attr.visas";
	public static final String ATTR_RETURN = "document.attr.return";
	public static final String VALUES_IMMED = "document.values.return.immediate";
	public static final String VALUES_STAGE = "document.values.return.stage";
	public static final String VALUES_END = "document.values.return.end";
	public static final String INFIX_TEMPLATE = ".template.";

	public static final String STATE_DRAFT = "visa.state.draft";
	public static final String STATE_ASSIGNED = "visa.state.assigned";
	// public static final String STATE_WAITING = "visa.state.waiting";
	public static final String STATES_WAITING = "visa.states.waiting";
	public static final String STATES_REJECTED = "visa.states.rejected";
	public static final String STATE_MISTAKE= "visa.state.mistake";
	public static final String STATES_AGREED = "visa.states.agreed";
	public static final String STATES_ASSIGN = "visa.states.assign";
	public static final String STATES_IGNORED = "visa.states.ignored";
	public static final String STATES_WAIT_ENCLOSED = "visa.states.waitEnclosed";
	public static final String ATTR_PERSON = "visa.attr.person";
	public static final String ATTR_ORDER = "visa.attr.order";
	public static final String ATTR_PARENT = "visa.attr.parent";
	public static final String ATTR_ENCLOSED_VISAS = "visa.attr.visas";
	public static final String ATTR_ENCLOSED_COMPLETE_DATE = "visa.attr.enclosedCompleteDate";
	public static final String ATTR_DECISION_HISTORY = "visa.attr.history";
	public static final String MOVE_ASSIGN = "visa.wfm.assign";
	public static final String MOVE_SEND = "visa.wfm.send";
	public static final String MOVE_RETURN_FROM_ENCLOSED = "visa.wfm.returnFromEnclosed";

	public static final String OPTION_RETURN = "document.return";
	public static final String OPTION_RETURN_IMMED = "immediate";
	public static final String OPTION_RETURN_STAGE = "stage";
	public static final String OPTION_RETURN_END = "end";
	public static final String OPTION_RETURN_ATTR = "attribute";
	public static final String OPTION_PROCESS_DOCUMENT_AT_FIRST_AGREE_VISA =
		"option.process.document.at.first.agree.visa";
	public static final String FLAG_VISA_MARK_PROCESS= "visa.mark.process";


	public static final String OPTION_ORDER = "visa.order";
	public static final String OPTION_ORDER_NORMAL = "normal";
	public static final String OPTION_ORDER_REVERSE = "reverse";

	protected Log logger = LogFactory.getLog(getClass());
	private Properties config = new Properties();

	private String name;
	public String getName() { return name; }

	public void setConfig(String path) {
		try {
			config.load(getClass().getResourceAsStream(path));
		} catch (IOException e) {
			logger.error("Configuration error", e);
		}
		name = path;
	}
	
	public ObjectId getObjectId(Class<?> type, String key) throws DataException {
		String value = config.getProperty(key);
		if (value == null)
			throw new DataException("docflow.config.noprop", new Object[] { key });
		//return createObjectId(type, value);
		return  IdUtils.smartMakeAttrId(value, type);
	}

	public Set<ObjectId> getObjectIdSet(Class<?> type, String key) throws DataException {
		String value = config.getProperty(key);
		if (value == null)
			throw new DataException("docflow.config.noprop", new Object[] { key });
		String[] ids = value.split(",");
		HashSet<ObjectId> result = new HashSet<ObjectId>(ids.length);
		for (int i = 0; i < ids.length; i++) {
			result.add(IdUtils.smartMakeAttrId(ids[i], type));
		}
		return result;
	}

	public Set<ObjectId> getObjectIdSet(Class<?> type, String key, boolean nullIfPropertyNotFound) throws DataException {
		Set<ObjectId> result = null;
		try{
			result = getObjectIdSet(type, key);
		}catch(DataException ex){
			if (!nullIfPropertyNotFound){
				//�� ���������� ������ � ������ ���������� � ������
				throw ex;
			}
		}
		return result;
	}

	public boolean isListedId(ObjectId id, String key) {
		if (!isPropertySet(key))
			return false;	//*****
		String values[] = config.getProperty(key).split(",");
		for (int i = 0; i < values.length; i++) {
			if (id.equals(createObjectId(id.getType(), values[i])))
				return true;
		}
		return false;
	}

	public Map<Object, ObjectId> getObjectIdMap(String prefix, Class<?> keyType, Class<?> valueType) throws DataException {
		HashMap<Object, ObjectId> map = new HashMap<Object, ObjectId>();
		for (Iterator<Object> itr = config.keySet().iterator(); itr.hasNext(); ) {
			String key = (String) itr.next();
			if (!key.startsWith(prefix))
				continue;
			String id = key.substring(prefix.length());
			map.put(createObjectId(keyType, id), getObjectId(valueType, key));
		}
		return map;
	}

	public Map<Object, Set<ObjectId>> getObjectIdSetMap(String prefix, Class<?> keyType, Class<?> valueType) throws DataException {
		HashMap<Object, Set<ObjectId>> map = new HashMap<Object, Set<ObjectId>>();
		for (Iterator<Object> itr = config.keySet().iterator(); itr.hasNext(); ) {
			String key = (String) itr.next();
			if (!key.startsWith(prefix))
				continue;
			String id = key.substring(prefix.length());
			map.put(createObjectId(keyType, id), getObjectIdSet(valueType, key));
		}
		return map;
	}

	public String getValue(String key) {
		return config.getProperty(key);
	}

	public boolean isPropertySet(String key) {
		return config.containsKey(key);
	}

	private ObjectId createObjectId(Class<?> type, String key) {
		ObjectId id = ObjectId.predefined(type, key);
		if (id == null)
			try {
				id = new ObjectId(type, Long.parseLong(key));
			} catch (NumberFormatException e) {
				id = new ObjectId(type, key);
			}
		return id;
	}
}
