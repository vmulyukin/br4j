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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;



/**
 * Represents cache for Area
 * @author skashanski
 *
 */
public class AreaCache {
	
	
    private static final long ENTRY_LIFE_TIME = 30000;

    private Map<Key, CachedObject> cachedMap = Collections.synchronizedMap( new HashMap<Key, CachedObject>() );
    
    
    
    public Object getValue( String folderid, int personId, long[] permissions ) {
        Arrays.sort( permissions );
        CachedObject entry = cachedMap.get( new Key( folderid, personId, permissions ) );
        if ( entry == null ) {
            return null;
        }
        long timeSet = entry.getLifeTime();
        
        if ( System.currentTimeMillis() - timeSet > getEntryLifeTime() ) {
            return null;
        }
        return entry.getCachedObject();
    }

    public void setValue( String folderid, int personId, long[] permissions, Object value ) {
        Arrays.sort( permissions );
        cachedMap.put( new Key( folderid, personId, permissions ), new CachedObject( value, System.currentTimeMillis() ) );
    }
    
    public void clearValue(String folderid, int personId, long[] permissions) {
    	Arrays.sort( permissions );
        cachedMap.remove( new Key( folderid, personId, permissions ) );
    }
    
    protected long getEntryLifeTime() {
    	return ENTRY_LIFE_TIME;
    }

    private class CachedObject {
    	
    	private Object cachedObject;
    	
    	private long  lifeTime;
    	

		public CachedObject(Object cachedObject, long lifeTime) {
			super();
			this.cachedObject = cachedObject;
			this.lifeTime = lifeTime;
		}

		public Object getCachedObject() {
			return cachedObject;
		}

		public void setCachedObject(Object cachedObject) {
			this.cachedObject = cachedObject;
		}

		public long getLifeTime() {
			return lifeTime;
		}

		public void setLifeTime(long lifeTime) {
			this.lifeTime = lifeTime;
		}
    	
    	
    }
    
    protected static class Key {
        public final String folderId;
        public final int personId;
        public final long[] permissions;

        public Key( String folderId, int personId, long[] permissions ) {
            this.folderId = folderId;
            this.personId = personId;
            this.permissions = permissions;
        }

        @Override
        public boolean equals( Object o ) {
            if ( this == o ) {
                return true;
            }
            if ( o == null || getClass() != o.getClass() ) {
                return false;
            }

            Key key = ( Key ) o;

            if ( personId != key.personId ) {
                return false;
            }
            if ( !folderId.equals( key.folderId ) ) {
                return false;
            }
            if ( !Arrays.equals( permissions, key.permissions ) ) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() { // ignore permissions in hash code for performance - mostly they'll be always the same
            int result = folderId.hashCode();
            result = 31 * result + personId;
            return result;
        }
    }
    
	

}
