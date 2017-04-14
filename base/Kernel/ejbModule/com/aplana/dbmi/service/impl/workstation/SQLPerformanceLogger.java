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
package com.aplana.dbmi.service.impl.workstation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.aplana.dbmi.service.impl.workstation.Util.ENDL;
import static com.aplana.dbmi.service.impl.workstation.Util.toMillies;

/**
 * @author Denis Mitavskiy
 *         Date: 12.04.11
 */
public class SQLPerformanceLogger {
    private static Log sqlSummaryLogger = LogFactory.getLog( "WorkstationSQLSummary" );
    private static Log sqlTraceLogger = LogFactory.getLog( "WorkstationSQLTrace" );
    private static SQLPerformanceLogger _instance;

    private long _thresholdMillies = 100;
    private int _maxQueriesInLog = 50;
    private SQLLog _slowestSQLLogNew;
    private SQLLog _largestSQLResultsLogNew;
    private Hashtable<String, Integer> _callerExecutionsCount;
    private long _nextTimeToLogAndReset;

    public static final Object SLOWEST_SQL_LOCK = new Object();
    public static final Object LARGEST_SQL_RESULTS_LOCK = new Object();
    public static final Object CALLER_EXECUTIONS_COUNT_LOCK = new Object();
    public static final Object LOG_OUTPUT_LOCK = new Object();

    private boolean _enabled = true;

    private SQLPerformanceLogger() {
        reset();
    }

    private void reset() {
        _slowestSQLLogNew = new SQLLog( true );
        _largestSQLResultsLogNew = new SQLLog( false );
        _callerExecutionsCount = new Hashtable<String, Integer>( 30 );
        _nextTimeToLogAndReset = nextTimeToLogAndReset();
    }

    private long nextTimeToLogAndReset() {
        Calendar cal = Calendar.getInstance();
        cal.add( Calendar.DATE, 1 );
        cal.set( Calendar.HOUR_OF_DAY, 0 );
        cal.set( Calendar.MINUTE, 0 );
        cal.set( Calendar.SECOND, 0 );
        cal.set( Calendar.MILLISECOND, 0 );
        return cal.getTimeInMillis();
    }

    public static synchronized SQLPerformanceLogger getInstance() {
        if ( _instance == null ) {
            _instance = new SQLPerformanceLogger();
        }
        return _instance;
    }

    public void log( String caller, String sql, long nanoTime, long resultRows ) {
        try {
            if ( !_enabled ) {
                return;
            }

            synchronized ( LOG_OUTPUT_LOCK ) {
                if ( sqlSummaryLogger.isInfoEnabled() && System.currentTimeMillis() > _nextTimeToLogAndReset ) {
                    sqlSummaryLogger.info( this.toString() );
                    reset();
                }
            }

            synchronized ( SLOWEST_SQL_LOCK ) {
                _slowestSQLLogNew.add( new SQLLogEntry( sql, nanoTime, resultRows ) );
            }
            synchronized ( LARGEST_SQL_RESULTS_LOCK ) {
                _largestSQLResultsLogNew.add( new SQLLogEntry( sql, nanoTime, resultRows ) );
            }

            if ( sqlTraceLogger.isInfoEnabled() && ( toMillies( nanoTime ) > _thresholdMillies || _thresholdMillies <= 0 ) ) {
                StringBuilder log = new StringBuilder( 1000 );
                appendTimeAndResultRows( log, nanoTime, resultRows );
                log.append( sql );
                sqlTraceLogger.info( log );
            }

            Integer sqlExecutions = _callerExecutionsCount.get( caller );
            if ( sqlExecutions == null ) {
                sqlExecutions = 0;
            }
            synchronized ( CALLER_EXECUTIONS_COUNT_LOCK ) {
                _callerExecutionsCount.put( caller, sqlExecutions + 1 );
            }
        } catch ( Throwable e ) { // no way we should allow logger to break application
            sqlSummaryLogger.error( "Exception during log: " + e );
        }
    }

    @Override
    public String toString() {
        SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd" );
        StringBuilder str = new StringBuilder( 2000 );
        str.append( formatter.format( new Date() ) ).append( ENDL );
        str.append( "==========================================================" ).append( ENDL );
        str.append( "Slowest SQL" ).append( ENDL );
        str.append( "----------------------------------------------------------" ).append( ENDL );
        synchronized ( SLOWEST_SQL_LOCK ) {
            ArrayList<SQLLogEntry> sortedEntries = _slowestSQLLogNew.getSortedEntries();
            for ( SQLLogEntry entry : sortedEntries ) {
                appendTimeAndResultRows( str, entry.time, entry.resultRows );
                str.append( " " ).append( entry.query ).append( ENDL );
            }
        }
        str.append( ENDL );

        str.append( "Largest results" ).append( ENDL );
        str.append( "----------------------------------------------------------" ).append( ENDL );
        synchronized ( LARGEST_SQL_RESULTS_LOCK ) {
            ArrayList<SQLLogEntry> sortedEntries = _largestSQLResultsLogNew.getSortedEntries();
            for ( SQLLogEntry entry : sortedEntries ) {
                appendTimeAndResultRows( str, entry.time, entry.resultRows );
                str.append( " " ).append( entry.query ).append( ENDL );
            }
        }
        str.append( ENDL );

        str.append( "Invocations from Executors" ).append( ENDL );
        str.append( "----------------------------------------------------------" ).append( ENDL );
        ArrayList<Map.Entry<String, Integer>> executionsCountList;
        synchronized ( CALLER_EXECUTIONS_COUNT_LOCK ) {
            executionsCountList = new ArrayList<Map.Entry<String, Integer>>( _callerExecutionsCount.entrySet() );
        }
        if ( executionsCountList.size() > 0 ) {
            Collections.sort( executionsCountList, new Comparator<Map.Entry<String, Integer>>() {
                public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 ) {
                    return -o1.getValue().compareTo( o2.getValue() );
                }
            } );
        }
        for ( Map.Entry<String, Integer> entry : executionsCountList ) {
            str.append( entry.getValue() ).append( " " ).append( entry.getKey() ).append( ENDL );
        }
        str.append( ENDL );
        return str.toString();
    }

    private void appendTimeAndResultRows( StringBuilder log, long nanoTime, long resultRows ) {
        log.append( "[" ).append( toMillies( nanoTime ) ).append( "]" );
        log.append( "{" ).append( resultRows ).append( "}" );
    }

    public long getThresholdMillies() {
        return _thresholdMillies;
    }

    public void setThresholdMillies( long thresholdMillies ) {
        _thresholdMillies = thresholdMillies;
    }

    public int getMaxQueriesInLog() {
        return _maxQueriesInLog;
    }

    public void setMaxQueriesInLog( int maxQueriesInLog ) {
        _maxQueriesInLog = maxQueriesInLog;
    }

    private static class SQLLogEntry {
        public final String query;
        public final long time;
        public final long resultRows;

        private SQLLogEntry( String query, long time, long resultRows ) {
            this.query = query;
            this.time = time;
            this.resultRows = resultRows;
        }

        private static Comparator<SQLLogEntry> getTimeComparator() {
            return new Comparator<SQLLogEntry>() {
                public int compare( SQLLogEntry o1, SQLLogEntry o2 ) { // "back"-comparator. we need largest time to be the first
                    return o1.time < o2.time ? 1 : o1.time == o2.time ? 0 : -1;
                }
            };
        }

        private static Comparator<SQLLogEntry> getResultRowsComparator() {
            return new Comparator<SQLLogEntry>() {
                public int compare( SQLLogEntry o1, SQLLogEntry o2 ) { // "back"-comparator
                    return o1.resultRows < o2.resultRows ? 1 : o1.resultRows == o2.resultRows ? 0 : -1;
                }
            };
        }
    }

    private class SQLLog {
        private ArrayList<SQLLogEntry> _sortedEntries;
        private Comparator<SQLLogEntry> _comparator;
        private HashMap<String, Integer> _queryIndexMap;
        private boolean _isTimeLog;

        public SQLLog( boolean isTimeLog ) {
            _sortedEntries = new ArrayList<SQLLogEntry>( _maxQueriesInLog );
            _queryIndexMap = new HashMap<String, Integer>( _maxQueriesInLog );
            _comparator = isTimeLog ? SQLLogEntry.getTimeComparator() : SQLLogEntry.getResultRowsComparator();
            _isTimeLog = isTimeLog;
        }

        public ArrayList<SQLLogEntry> getSortedEntries() {
            return _sortedEntries;
        }

        public void add( SQLLogEntry newEntry ) {
            boolean reSort = false;
            Integer existingEntryIndex = _queryIndexMap.get( newEntry.query );
            if ( existingEntryIndex != null ) {
                if ( _isTimeLog && newEntry.time > _sortedEntries.get( existingEntryIndex ).time || !_isTimeLog && newEntry.resultRows > _sortedEntries.get( existingEntryIndex ).resultRows ) {
                    _sortedEntries.set( existingEntryIndex, newEntry ); // index is not changed, but sort order might be chagned
                    reSort = true;
                }
            } else if ( _sortedEntries.size() >= _maxQueriesInLog ) {
                if ( _sortedEntries.size() > _maxQueriesInLog ) { // max queries has been decreased from code
                    for ( int i = _sortedEntries.size() - 1; i >= _maxQueriesInLog; --i ) {
                        _sortedEntries.remove( i );
                    }
                }
                int lastIndex = _maxQueriesInLog - 1;
                SQLLogEntry entryWithMinValue = _sortedEntries.get( lastIndex );
                if ( _isTimeLog && newEntry.time > entryWithMinValue.time || !_isTimeLog && newEntry.resultRows > entryWithMinValue.resultRows ) {
                    _sortedEntries.set( lastIndex, newEntry );
                    _queryIndexMap.remove( entryWithMinValue.query );
                    _queryIndexMap.put( newEntry.query, lastIndex );
                    reSort = true;
                }
            } else {
                _sortedEntries.add( newEntry );
                _queryIndexMap.put( newEntry.query, _sortedEntries.size() - 1 );
                reSort = true;
            }
            if ( reSort ) {
                reSort();
            }
        }

        private void reSort() {
            Collections.sort( _sortedEntries, _comparator );
            for ( int i = 0, sortedEntriesSize = _sortedEntries.size(); i < sortedEntriesSize; i++ ) {
                SQLLogEntry entry = _sortedEntries.get( i );
                _queryIndexMap.put( entry.query, i );
            }
        }
    }
}
