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
package org.displaytag.decorator;

import javax.servlet.jsp.PageContext;

import org.displaytag.exception.DecoratorException;
import org.displaytag.properties.MediaTypeEnum;


/**
 * <p>
 * Interface for simple column decorators.
 * </p>
 * <p>
 * A column decorator is called after the object has been retrieved and it can "transform" the object before the
 * rendering.
 * </p>
 * <p>
 * The <code>DisplaytagColumnDecorator</code> interface has been introduced in displaytag 1.1 and replaces the
 * previous <code>ColumnDecorator</code> interface, adding the pageContext and media parameters, and changing the
 * return type to object to allow decorator chaining.
 * </p>
 * @author Fabrizio Giustina
 * @version $Id$
 * @since 1.1
 */
public interface DisplaytagColumnDecorator
{

    /**
     * Called after the object has been retrieved from the bean contained in the list. The decorate method is
     * responsible for transforming the object into a string to render in the page.
     * @param columnValue Object to decorate
     * @param pageContext jsp page context
     * @param media current media (html, pdf, excel...)
     * @return Object decorated object
     * @throws DecoratorException wrapper exception for any exception thrown during decoration
     */
    Object decorate(Object columnValue, PageContext pageContext, MediaTypeEnum media) throws DecoratorException;

}
