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
package org.displaytag.pagination;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * Object representing a page.
 * @author Fabrizio Giustina
 * @version $Revision: 851 $ ($Author: fgiust $)
 */
public class NumberedPage
{

    /**
     * page number.
     */
    private int number;

    /**
     * is the page selected?
     */
    private boolean selected;

    /**
     * Creates a new page with the specified number.
     * @param pageNumber page number
     * @param isSelected is the page selected?
     */
    public NumberedPage(int pageNumber, boolean isSelected)
    {
        this.number = pageNumber;
        this.selected = isSelected;
    }

    /**
     * Returns the page number.
     * @return the page number
     */
    public int getNumber()
    {
        return this.number;
    }

    /**
     * is the page selected?
     * @return true if the page is slected
     */
    public boolean getSelected()
    {
        return this.selected;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
            .append("selected", this.selected) //$NON-NLS-1$
            .append("number", this.number) //$NON-NLS-1$
            .toString();
    }
}