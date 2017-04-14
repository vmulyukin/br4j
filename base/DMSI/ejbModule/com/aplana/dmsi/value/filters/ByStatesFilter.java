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
package com.aplana.dmsi.value.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dmsi.Parametrized;
import com.aplana.dmsi.config.ConfigurationException;
import com.aplana.dmsi.types.DMSIObject;
import com.aplana.dmsi.util.ReflectionUtils;

public class ByStatesFilter implements CollectionFilter, Parametrized {

    private static final String STATES_PARAM = "states";

    private Map<Class<? extends DMSIObject>, List<Long>> states = new HashMap<Class<? extends DMSIObject>, List<Long>>();

    public void setParameter(String key, Object value) {
        if (STATES_PARAM.equals(key)) {
            String[] classStatesPair = ((String) value).split(":");
            if (classStatesPair.length != 2) {
                throw new ConfigurationException(
                        "Value of "
                                + STATES_PARAM
                                + " should be like '<class>:<state1_id>,<state2_id>,...,<staten_id>'");
            }
            String className = classStatesPair[0];
            Class<? extends DMSIObject> clazz = ReflectionUtils
                    .initializeClass(DMSIObject.class, className);
            @SuppressWarnings("unchecked")
            List<ObjectId> stateObjectIds = ObjectIdUtils
                    .commaDelimitedStringToNumericIds(classStatesPair[1],
                            CardState.class);
            List<Long> stateIds = new ArrayList<Long>(stateObjectIds.size());
            for (ObjectId stateObjectId : stateObjectIds) {
                stateIds.add((Long) stateObjectId.getId());
            }
            states.put(clazz, stateIds);
        }
    }

    public void filterCollection(Collection<?> collection) {
        for (Iterator<?> iter = collection.iterator(); iter.hasNext();) {
            Object obj = iter.next();
            if (states.containsKey(obj.getClass())
                    && !states.get(obj.getClass()).contains(
                            ((DMSIObject) obj).getState())) {
                iter.remove();
            }
        }
    }

    public ObjectId[] filterIds(ObjectId[] ids) {
        return ids;
    }

    public Object selectOneObject(Collection<?> collection) {
        filterCollection(collection);
        if (collection.iterator().hasNext())
            return collection.iterator().next();
        return null;
    }

}