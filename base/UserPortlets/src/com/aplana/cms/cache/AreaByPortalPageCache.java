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
package com.aplana.cms.cache;

import com.aplana.dbmi.model.ObjectId;

public class AreaByPortalPageCache extends AreaCache {
	
	private static final long ENTRY_LIFE_TIME = 200000;
    private static final Object CREATION_LOCK = new Object();
    
    private static AreaByPortalPageCache _instance;
	
    private AreaByPortalPageCache() {

    }

    public static AreaByPortalPageCache instance() {
        synchronized ( CREATION_LOCK ) {
            if ( _instance == null ) {
                _instance = new AreaByPortalPageCache();
            }
        }
        return _instance;
    }
    
    public ObjectId getAreaIdByPortalPage(String pageName, String navigatorName, int personId, long[] permissions ) {
    	
    	Object value = getValue(getKey(pageName, navigatorName), personId, permissions);
    	
    	if (value == null)
    		return null;
    	
    	return (ObjectId) value; 
    	
    }
    
    public void setAreaIdByPortalPage(String pageName, String navigatorName, int personId, 
    		long[] permissions, ObjectId areaId ) {
    	
    	setValue(getKey(pageName, navigatorName), personId, permissions, areaId);
	
    }  
    
    protected long getEntryLifeTime() {
    	return ENTRY_LIFE_TIME;
    }

    private String getKey(String pageName, String navigatorName) {
    	return pageName + ":" + navigatorName;
    }
}
