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
package com.aplana.util;

import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 17.02.11
 *         Time: 17:39
 */
public class Table implements Cloneable {
    public static final String ENDL = System.getProperty( "line.separator" );
    public static final String TAB = "\t";

    private HashMap<String, Integer> _columnIndex;
    private ArrayList<String> _columnsSequence;
    private ArrayList<ArrayList<Object>> _arrays;
    private int _initialCapacity;

    public Table() {
        _arrays = new ArrayList<ArrayList<Object>>();
        _columnIndex = new HashMap<String, Integer>();
        _columnsSequence = new ArrayList<String>();
        _initialCapacity = 10;
    }

    public Table( int initialCapacity ) {
        this();
        if ( initialCapacity < 0 ) {
            throw new IllegalArgumentException( "Illegal Capacity: " + initialCapacity );
        }
        _initialCapacity = initialCapacity;
    }

    /**
     * This operation returns array representing a table column. This can be used for optimizing table/column scan operations
     * Array retrieved should never be modified, for table data modification set-operations should be used
     * @param column Column to retrieve
     * @return Table column
     */
    public ArrayList<Object> getColumn( String column ) {
        return _arrays.get( getColumnIndex( column ) );
    }

    public int getColumnIndex( String column ) {
        Integer columnIndex = _columnIndex.get( column );
        if ( columnIndex == null ) {
            throw new IllegalArgumentException( "Illegal Column Name: " + column );
        } else {
            return columnIndex;
        }
    }

    public int add( String column ) {
        int columnIndex = _columnsSequence.size();
        _columnsSequence.add( column );

        int curSize = size();
        ArrayList<Object> newArray = new ArrayList<Object>( curSize != 0 ? curSize : _initialCapacity );
        for ( int i = 0; i < curSize; ++i ) {
            newArray.add( null );
        }
        _arrays.add( newArray );
        _columnIndex.put( column, columnIndex );
        return columnIndex;
    }

    public void set( String column, Object value, int index ) {
        addRowsIfNeeded( index );
        getColumn( column ).set( index, value );
    }

    public void set( int columnIndex, Object value, int index ) {
        addRowsIfNeeded( index );
        _arrays.get( columnIndex ).set( index, value );
    }

    public Object get( String column, int index ) {
        return getColumn( column ).get( index );
    }

    public Object get( int colIndex, int index ) {
        return _arrays.get( colIndex ).get( index );
    }

    public void remove( String column ) {
        Integer columnIndex = _columnIndex.get( column );
        if ( columnIndex == null ) {
            throw new IllegalArgumentException( "Illegal Column Name: " + column );
        }
        _columnsSequence.remove( column );
        _arrays.remove( columnIndex.intValue() );
        _columnIndex = new HashMap<String, Integer>( _columnsSequence.size() );
        for ( int i = 0; i < _columnsSequence.size(); ++i ) {
            _columnIndex.put( _columnsSequence.get( i ), i );
        }
    }

    public int size() {
        return _arrays.size() == 0 ? 0 : _arrays.get( 0 ).size();
    }

    public Table trimToSize() {
        for ( ArrayList<Object> _array : _arrays ) {
            _array.trimToSize();
        }
        return this;
    }

    public String toString() {
        return toString( false, _columnsSequence, 500 );
    }

    public String toString( List<String> columns ) {
        return toString( columns, 0 );
    }

    public String toString( List<String> columns, int maxRows ) {
        return toString( false, columns, maxRows );
    }

    public String toString( boolean header ) {
        return toString( header, 0 );
    }

    public String toString( boolean header, int maxRows ) {
        return toString( header, _columnsSequence, maxRows );
    }

    public String toString( boolean header, List<String> columnsInOrder ) {
        return toString( header, columnsInOrder, 0 );
    }

    public String toString( boolean header, List<String> columnsInOrder, int maxRows ) {
        StringBuilder result = new StringBuilder();
        if ( header ) {
            for ( String column : columnsInOrder ) {
                result.append( column ).append( TAB );
            }
            result.append( ENDL );
        }

        int size = maxRows == 0 || maxRows > size() ? size() : maxRows;
        for ( int row = 0; row < size; ++row ) {
            result.append( rowToString( row, columnsInOrder ) );
            if ( row != size - 1 ) {
                result.append( ENDL );
            }
        }
        return result.toString();
    }

    public String rowToString( int row, List<String> columnsInOrder ) {
        StringBuilder result = new StringBuilder();
        int columnsQty = columnsInOrder.size();
        for ( int col = 0; col < columnsQty; ++col ) {
            final String column = columnsInOrder.get( col );
            Object object = get( column, row );
            result.append( object == null ? "" : object.toString() );
            if ( col != columnsQty - 1 ) {
                result.append( TAB );
            }
        }
        return result.toString();
    }

    public Table getCopy() {
        return getCopy( this._columnIndex.keySet() );
    }

    public Table getCopy( Set<String> columnsToCopy ) {
        Table result = new Table( this.size() );
        result._columnIndex = new HashMap<String, Integer>( columnsToCopy.size() );
        result._columnsSequence = new ArrayList<String>( columnsToCopy.size() );
        result._arrays = new ArrayList<ArrayList<Object>>( columnsToCopy.size() );
        for ( String column : _columnsSequence ) {
            List<Object> array = this.getColumn( column );
            ArrayList<Object> newArray = new ArrayList<Object>( array );
            result._arrays.add( newArray );
            result._columnsSequence.add( column );
            result._columnIndex.put( column, result._arrays.size() - 1 );
        }
        return result;
    }

    @Override
    /**
     * This is a quick clone() operation - it doesn't perform cloning of objects inside arrays
     */
    public Object clone() throws CloneNotSupportedException {
        return getCopy();
    }

    public TableComparator getComparator() {
        return new TableComparator();
    }

    public void sort( String column, boolean ascending ){
        TableComparator comparator = getComparator();
        comparator.addSortingOrder( column, ascending );
        sort( comparator );
    }

    public void sort( TableComparator comparator ) {
        int size = size();
        Integer[] rowIndexes = new Integer[ size ];
        for ( int i = 0; i < size; ++i ) {
            rowIndexes[ i ] = i;
        }
        Arrays.sort( rowIndexes, comparator );
        Table copy = getCopy();
        for ( int i = 0; i < size; ++i ) {
            Integer newRowIndex = rowIndexes[ i ];
            copyRow( copy, newRowIndex, i );
        }
    }

    private void copyRow( Table from, int fromRow, int toRow ) {
        for ( int i = 0, arraysSize = _arrays.size(); i < arraysSize; ++i ) {
            ArrayList<Object> fromColumn = from._arrays.get( i );
            ArrayList<Object> toColumn = this._arrays.get( i );
            toColumn.set( toRow, fromColumn.get( fromRow ) );
        }
    }

    private void addRowsIfNeeded( int index ) {
        int currentSize = size();
        if ( index < currentSize ) {
            return;
        }

        int newSize = index + 1;
        for ( ArrayList<Object> oldArray : _arrays ) {
            oldArray.ensureCapacity( newSize );
            for ( int i = currentSize; i < newSize; ++i ) {
                oldArray.add( null );
            }
        }
    }

    public class TableComparator implements Comparator<Integer> {
        private ArrayList<SortOrder> _sortingOrder;
        private ArrayList<ArrayList<Object>> _columnsToSort;

        private TableComparator() {
            _sortingOrder = new ArrayList<SortOrder>();
            _columnsToSort = new ArrayList<ArrayList<Object>>();
        }

        public void addSortingOrder( String column, boolean ascending ) {
            _columnsToSort.add( getColumn( column ) );
            _sortingOrder.add( new SortOrder( _columnsToSort.size() - 1, ascending ) );
        }

        public int compare( Integer o1, Integer o2 ) {
            for ( SortOrder sortOrder : _sortingOrder ) {
                int columnIndex = sortOrder.columnIndex;
                Object value1 = _columnsToSort.get( columnIndex ).get( o1 );
                Object value2 = _columnsToSort.get( columnIndex ).get( o2 );
                int comparisonResult = compare( value1, value2, sortOrder );
                if ( comparisonResult != 0 ) {
                    return comparisonResult;
                }
            }
            return 0;
        }

        private int compare( Object o1, Object o2, SortOrder sortOrder ) {
            if ( o1 == null || o2 == null ) {
                if ( o1 == null && o2 == null ) {
                    return 0;
                }
                if ( o1 == null ) { // o2 != null ==> o1 < o2
                    return sortOrder.ascending ? -1 : 1;
                }
                return sortOrder.ascending ? 1 : -1; // o1 != null, o2 == null ==> o1 > o2
            }
            int result = ( ( Comparable ) o1 ).compareTo( o2 );
            return sortOrder.ascending ? result : -result;
        }
    }

    private static class SortOrder {
        public final int columnIndex;
        public final boolean ascending;

        public SortOrder( int columnIndex, boolean ascending ) {
            this.ascending = ascending;
            this.columnIndex = columnIndex;
        }
    }

    private static void test() {
        Table table = new Table( 300 );
        table.sort( table.getComparator() );
        System.out.println(table);
        System.out.println("======================");
        long t1 = System.currentTimeMillis();
        table.add( "Col_1" );
        for ( int i = 100; i < 250; ++i ) {
            table.set( "Col_1", i, i );
        }
        table.add( "Col_2" );
        for ( int i = 170; i < 200; ++i ) {
            table.set( "Col_2", "col_2" + i, i );
        }
        table.add( "Col_3" );
        for ( int i = 0; i < 300; ++i ) {
            table.set( "Col_3", "col_3" + i, i );
        }
        //table.remove( "Col_3" );

        TableComparator comparator = table.getComparator();
        comparator.addSortingOrder( "Col_2", false );
        comparator.addSortingOrder( "Col_1", false );

        table.sort( comparator );

        long t2 = System.currentTimeMillis();
        System.out.println( table.toString( false, 1000 ) );
        System.out.println( "Time: " + ( t2 - t1 ) );
    }

    public static void main( String[] args ) {
        Table.test();
    }
}
