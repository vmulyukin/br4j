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

import com.aplana.cms.FolderDocumentsQuantities;


/**
 * @author Denis Mitavskiy
 *         Date: 18.04.11
 *         Time: 15:29
 */
public class CounterCache extends AreaCache {
	
    private static final Object CREATION_LOCK = new Object();
    
    private static CounterCache _instance;
	
    private CounterCache() {

    }

    public static CounterCache instance() {
        synchronized ( CREATION_LOCK ) {
            if ( _instance == null ) {
                _instance = new CounterCache();
            }
        }
        return _instance;
    }
    
    public FolderDocumentsQuantities getCount(String folderid, int personId, long[] permissions ) {
    	Object value = getValue(folderid, personId, permissions);
    	return (FolderDocumentsQuantities) value; 
    	
    }
    
    public void setCount(String folderid, int personId, long[] permissions, FolderDocumentsQuantities count ) {
    	
    	setValue(folderid, personId, permissions, count);
    	
    }    
    
    public void clearCount(String folderid, int personId, long[] permissions ) {
    	clearValue(folderid, personId, permissions);
    }

}
