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
package com.aplana.dbmi.workflowbuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.aplana.dbmi.model.DataObject;

public class DataObjectUtils {
	public static Map collectionToIdMap(Collection collection) {
		Map map = new HashMap(collection.size());
		fillIdMapFromCollection(map, collection);
		return map;
	}
	
	public static void fillIdMapFromCollection(Map map, Collection collection) {
		Iterator i = collection.iterator();
		while (i.hasNext()) {
			DataObject obj = (DataObject)i.next();
			map.put(obj.getId().getId(), obj);
		}		
	}
}
