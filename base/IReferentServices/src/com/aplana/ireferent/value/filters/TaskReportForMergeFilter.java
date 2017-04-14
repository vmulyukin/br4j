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
package com.aplana.ireferent.value.filters;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.ireferent.config.ConfigurationException;
import com.aplana.ireferent.Parametrized;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOMPerson;
import com.aplana.ireferent.types.WSOTaskReportForMerge;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ReflectionUtils;

public class TaskReportForMergeFilter implements CollectionFilter, Parametrized {

    private static final String STATES_PARAM = "states";
    private static final String SKIP_ARM_VIEWED_STATES_PARAM = "skipArmViewedStates";
    private static final String DONE_DAYS_MAX_AGE_PARAM = "doneDaysMaxAge";
    private static final String CURRENT_USER_PARAM = "currentUser";
    
    private List<Long> stateIds = new ArrayList<Long>();
    private List<Long> skipArmViewedStateIds = new ArrayList<Long>();
    private Field currentUserField;
    private Integer doneDaysMaxAge;
    
    private static String ARM_VIEWED_FLAG = "1";
    
    public void setParameter(String key, Object value) {

	    if (STATES_PARAM.equals(key)) {
	    	stateIds.addAll(convertToStateIds((String)value));
	    }
	    else if (SKIP_ARM_VIEWED_STATES_PARAM.equals(key)) {
	    	skipArmViewedStateIds.addAll(convertToStateIds((String)value));
	    }
	    else if (DONE_DAYS_MAX_AGE_PARAM.equals(key)) {
	    	try {
	    		doneDaysMaxAge = Integer.valueOf((String)value);
	    	}
	    	catch (NumberFormatException e) {
	    		throw new ConfigurationException(
                        "Value of "
	    				+ DONE_DAYS_MAX_AGE_PARAM
	    				+ " should be integer");
	    	}
	    }
	    else if (CURRENT_USER_PARAM.equals(key)) {
	    	Collection<Field> fields = ReflectionUtils.getFields(WSOTaskReportForMerge.class);
	    	for (Field field : fields) {
				if (field.getName().equals((String)value)
						&& (field.getType().isAssignableFrom(WSOMPerson.class) || field.getType().isAssignableFrom(WSOCollection.class))) {
					currentUserField = field;
					break;
				}
	    	}
	    	if (currentUserField == null) {
	    		throw new ConfigurationException(
	    				"Value of "
	    				+ CURRENT_USER_PARAM
	    				+ " should be an existing Person field in TaskReportForMerge");
	    	}
    	}
    }

    public void filterCollection(WSOCollection collection) {
    	for (Iterator<?> iter = collection.getData().iterator(); iter.hasNext();) 
    	{
    		Object taskReport = iter.next();

    		if (!(taskReport instanceof WSOTaskReportForMerge))
    			continue;
    		
    		if (!stateIds.isEmpty() 
    				&& !stateIds.contains(((WSObject) taskReport).getState())) {
    			iter.remove();
    			continue;
    		}
    		else if (!skipArmViewedStateIds.isEmpty()
    				&& ((WSOTaskReportForMerge)taskReport).getArmViewed() != null
    				&& ((WSOTaskReportForMerge)taskReport).getArmViewed().equals(ARM_VIEWED_FLAG)
    				&& skipArmViewedStateIds.contains(((WSObject) taskReport).getState())) {
    			iter.remove();
    			continue;
    		}
    		else if (currentUserField != null) {
    			try {
					if (currentUserField.get(taskReport) instanceof WSOMPerson) {
						WSOMPerson user = null;
						try {
							user = (WSOMPerson)currentUserField.get(taskReport);
						} catch (Exception e) {
							throw new ConfigurationException(
									MessageFormat.format("Error during "
									+ "parameter {0}:{1} value get from taskReport {2}", CURRENT_USER_PARAM, 
									currentUserField.getName(), ((WSObject)taskReport).getId().toString() ), e);
						}
						if (user != null && collection.getUserId()!= null
								&& !user.getId().equals(collection.getUserId())) {
							iter.remove();
							continue;
						}
					} else 
					if (currentUserField.get(taskReport) instanceof WSOCollection) {
						WSOCollection users = null;
						try {
							users = (WSOCollection)currentUserField.get(taskReport);
						} catch (Exception e) {
							throw new ConfigurationException(
									MessageFormat.format("Error during "
									+ "parameter {0}:{1} value get from taskReport {2}", CURRENT_USER_PARAM, 
									currentUserField.getName(), ((WSObject)taskReport).getId().toString() ), e);
						}
						List<Object> listUsers = users.getData();
						boolean isExistUser = false;
						for(Object user : listUsers) {
							WSOMPerson person = null;
							person = (WSOMPerson) user;
							if (person != null && collection.getUserId()!= null
									&& person.getId().equals(collection.getUserId())) {
								isExistUser = true;
							}
						}
						if (!isExistUser) {
							iter.remove();
							continue;
						}
					}
				} catch (Exception e) {
					throw new ConfigurationException(
							MessageFormat.format("Illegal argument. " +
									"Parameter {0}:{1} value from taskReport {2} - can not be instance of WSOCollection or WSOMPerson.", CURRENT_USER_PARAM, 
									currentUserField.getName(), ((WSObject)taskReport).getId().toString()), e);
				}
    		}
    		
    		if (doneDaysMaxAge != null) {
    			Date doneDate = ((WSOTaskReportForMerge)taskReport).getDoneDate();
    			if (doneDate != null) {
    				Date currentDate = new Date();
    				long daysDiff = TimeUnit.MILLISECONDS.toDays(currentDate.getTime() -
    						doneDate.getTime());
    				if (daysDiff >= doneDaysMaxAge) {
    					iter.remove();
    					continue;
    				}
    			}
    		}
    	}
    }

    public ObjectId[] filterIds(ObjectId[] ids) {
    	return ids;
    }

    public Object selectOneObject(WSOCollection collection) {
        filterCollection(collection);
        if (collection.getData().iterator().hasNext())
            return collection.getData().iterator().next();
        return null;
    }
    
    private List<Long> convertToStateIds(String value) {
    	List<ObjectId> stateObjectIds = ObjectIdUtils
		    .commaDelimitedStringToNumericIds(value,
		    CardState.class);
    	List<Long> stateIds = new ArrayList<Long>(stateObjectIds.size());
    	for (ObjectId stateObjectId : stateObjectIds) {
    		stateIds.add((Long) stateObjectId.getId());
    	}
    	return stateIds;
    }
}