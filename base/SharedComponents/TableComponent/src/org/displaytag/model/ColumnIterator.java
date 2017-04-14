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
/**
 * Licensed under the Artistic License; you may not use this file
 * except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://displaytag.sourceforge.net/license.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.displaytag.model;

import java.util.Iterator;
import java.util.List;


/**
 * Iterator on columns.
 * @author Fabrizio Giustina
 * @version $Revision: 720 $ ($Author: fgiust $)
 */
public class ColumnIterator
{

    /**
     * current row.
     */
    private Row parentRow;

    /**
     * Internal iterator on header cells.
     */
    private Iterator headerIterator;

    /**
     * Internal iterator on cells.
     */
    private Iterator cellIterator;

    /**
     * Creates a new ColumnIterator given a list of column and a row.
     * @param columns List containing column objects
     * @param row current Row
     */
    public ColumnIterator(List columns, Row row)
    {
        this.headerIterator = columns.iterator();
        this.cellIterator = row.getCellList().iterator();
        this.parentRow = row;
    }

    /**
     * Are there more columns?
     * @return boolean <code>true</code> if there are more columns
     */
    public boolean hasNext()
    {
        return this.headerIterator.hasNext();
    }

    /**
     * Returns the next column.
     * @return Column next column
     */
    public Column nextColumn()
    {
        HeaderCell header = (HeaderCell) this.headerIterator.next();

        Cell cell = Cell.EMPTY_CELL;

        // if cells is not present simply return an empty cell.
        // this is needed for automatic properties discovery
        if (this.cellIterator.hasNext())
        {
            cell = (Cell) this.cellIterator.next();
        }

        // create a new column using the next value in the header and cell iterators and returns it
        return new Column(header, cell, this.parentRow);
    }

}