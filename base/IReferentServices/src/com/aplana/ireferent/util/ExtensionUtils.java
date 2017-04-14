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
package com.aplana.ireferent.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOItem;
import com.aplana.ireferent.types.WSObject;

public class ExtensionUtils {

    private ExtensionUtils() {
    }

    public static List<WSOItem> getExtensions(WSObject object) {
	WSOCollection extension = object.getExtension();
	if (extension == null) {
	    return Collections.emptyList();
	}
	List<Object> extensionObjects = extension.getData();
	List<WSOItem> extensionItems = new ArrayList<WSOItem>();
	for (Object extensionObject : extensionObjects) {
	    if (extensionObject instanceof WSOItem) {
		extensionItems.add((WSOItem) extensionObject);
	    }
	}
	return extensionItems;
    }

    public static WSOItem createItem(String id, Object... values) {
	WSOItem item = new WSOItem();
	item.setId(id);

	WSOCollection itemValues = new WSOCollection();
	List<Object> items = itemValues.getData();
	items.addAll(Arrays.asList(values));
	item.setValues(itemValues);

	return item;
    }

    public static List<Object> getExtensionValues(WSOItem item) {
	WSOCollection itemValues = item.getValues();
	if (itemValues == null) {
	    return Collections.emptyList();
	}
	return itemValues.getData();
    }

    public static void setExtensionValues(WSOItem item,
	    Collection<?> values) {
	WSOCollection itemValues = new WSOCollection();
	itemValues.getData().addAll(values);
	item.setValues(itemValues);
    }

    public static void addExtensions(WSObject object, WSOItem... itemValues) {
	WSOCollection extension = object.getExtension();
	if (extension == null) {
	    extension = new WSOCollection();
	    object.setExtension(extension);
	}
	List<Object> items = extension.getData();
	items.addAll(Arrays.asList(itemValues));
    }

    public static void setExtensions(WSObject object, WSOItem... itemValues) {
	object.setExtension(null);
	addExtensions(object, itemValues);
    }
}
