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
package com.aplana.dbmi.model.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;

public class ReferenceValueUtils {
	public static Collection referenceValueWithChildrenToCollection(ReferenceValue referenceValue) {
		List result = new ArrayList();
		result.add(referenceValue);
		addChildrenToCollection(referenceValue.getChildren(), result);
		return result;
	}
	
	private static void addChildrenToCollection(Collection children, Collection collection) {
		if (children == null) {
			return;
		}
		collection.addAll(children);
		Iterator i = children.iterator();
		while (i.hasNext()) {
			ReferenceValue child = (ReferenceValue)i.next();
			addChildrenToCollection(child.getChildren(), collection);
		}
	}

	public static ReferenceValue findReferenceValueInHierarchicalCollection(Collection values, ObjectId refId) {
		if (values == null) {
			return null;
		}
		Iterator i = values.iterator();
		while (i.hasNext()) {
			ReferenceValue ref = (ReferenceValue)i.next();
			if (refId.equals(ref.getId())) {
				return ref;
			}
			ref = findReferenceValueInHierarchicalCollection(ref.getChildren(), refId);
			if (ref != null) {
				return ref;
			}
		}
		return null;
	}
	
	public static List<ReferenceValue> makeReferenceValues(String ids) {
		final List<ObjectId> objectIds = ObjectIdUtils.commaDelimitedStringToIds(ids, ReferenceValue.class);
		if(objectIds == null || objectIds.isEmpty())
			return new ArrayList<ReferenceValue>(0);
		List<ReferenceValue> result = new ArrayList<ReferenceValue>(objectIds.size());
		for(final ObjectId value : objectIds) {
			final ReferenceValue referenceValue = new ReferenceValue();
			referenceValue.setId((Long) value.getId());
			result.add(referenceValue);
		}
		return result;
	}
}
