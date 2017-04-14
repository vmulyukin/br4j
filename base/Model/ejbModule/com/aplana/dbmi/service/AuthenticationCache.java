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
package com.aplana.dbmi.service;

import java.security.Principal;
import java.util.*;

/**
 * @author Denis Mitavskiy
 */
public class AuthenticationCache {
    private static final long ENTRY_LIFE_TIME = 60000;
    private static final long CLEAN_PERIOD = 3600000;
    private static final Object CREATION_LOCK = new Object();
    private static final Object CLEAN_LOCK = new Object();

    private static AuthenticationCache _instance;
    private Map<Key, Object[]> _cache;
    private long _nextClean;

    private AuthenticationCache() {
        _cache = Collections.synchronizedMap( new HashMap<Key, Object[]>() );
        _nextClean = System.currentTimeMillis() + CLEAN_PERIOD;
    }

    public static AuthenticationCache instance() {
        synchronized ( CREATION_LOCK ) {
            if ( _instance == null ) {
                _instance = new AuthenticationCache();
            }
        }
        return _instance;
    }

    public User getUser( Principal principal, String address ) {
        Object[] entry = _cache.get( new Key( principal, address ) );
        if ( entry == null ) {
            return null;
        }
        if ( oldEntry( entry ) ) {
            return null;
        }
        return (User) entry[ 0 ];
    }

    public void setUser( Principal principal, String address, User user ) {
        synchronized ( CLEAN_LOCK ) {
            _cache.put( new Key( principal, address ), new Object[] { user, System.currentTimeMillis() } );
        }
        cleanOldEntries();
    }

    private boolean oldEntry( Object[] entry ) {
        long timeSet = ( Long ) entry[ 1 ];
        return System.currentTimeMillis() - timeSet > ENTRY_LIFE_TIME;
    }

    private void cleanOldEntries() {
        if ( System.currentTimeMillis() < _nextClean ) {
            return;
        }
        synchronized ( CLEAN_LOCK ) {
            Set<Key> keySet = _cache.keySet();
            ArrayList<Key> keys = new ArrayList<Key>( keySet ); // we'd like to avoid changes in key set as we'll modify the map
            for ( Key key : keys ) {
                if ( oldEntry( _cache.get( key ) ) ) {
                    _cache.remove( key );
                }
            }
        }
        _nextClean = System.currentTimeMillis() + CLEAN_PERIOD;
    }

    private static class Key {
        public final Principal principal;
        public final String address;

        public Key( Principal principal, String address ) {
            this.principal = principal;
            this.address = address;
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

            if ( address != null ? !address.equals( key.address ) : key.address != null ) {
                return false;
            }
            if ( !principal.equals( key.principal ) ) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() { // no need in address in hash code
            return principal.hashCode();
        }
    }
}
