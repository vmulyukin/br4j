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
package com.aplana.dbmi.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.aplana.dbmi.model.ObjectId;

public class SmartMap extends HashMap<Object, Object>
{
	private HashMap<Object, Object> undisclosed = new HashMap<Object, Object>();

	public HashMap<Object, Object> getUndisclosed() {
		return undisclosed;
	}

	public boolean containsKey(Object key) {
		if (!super.containsKey(key) && key != null)
			disclose(key);
		return super.containsKey(key);
	}

	public Object get(Object key) {
		if (!super.containsKey(key) && key!= null)
			disclose(key);
		return super.get(key);
	}

	public Object put(Object key, Object value) {
		if (key instanceof String)
			return undisclosed.put(key, value);
		return super.put(key, value);
	}

	private void disclose(Object key) {
		synchronized (undisclosed) {
			for (Iterator<?> itr = undisclosed.entrySet().iterator(); itr.hasNext(); ) {
				Entry<?, ?> entry = (Entry<?, ?>) itr.next();
				Object testKey = entry.getKey();
				/*if (key instanceof DataObject) {
					key = ((DataObject) key).getId();
				}*/
				if (key instanceof ObjectId) {
					testKey = ObjectId.predefined(((ObjectId) key).getType(), (String) entry.getKey());
					if (testKey == null)
						try {
							testKey = new ObjectId(((ObjectId) key).getType(),
									Long.parseLong((String) entry.getKey()));
						} catch (NumberFormatException e) {
							testKey = new ObjectId(((ObjectId) key).getType(), entry.getKey());
						}
				}
				else if (key instanceof Integer) {
					testKey = new Integer((String) entry.getKey());
				}
				if (key.equals(testKey)) {
					itr.remove();
					super.put(testKey, entry.getValue());
					return;
				}				
			}
		}
	}
}