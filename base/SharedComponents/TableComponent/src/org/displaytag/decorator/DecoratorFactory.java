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

import org.displaytag.exception.DecoratorInstantiationException;


/**
 * Factory for TableDecorator or ColumnDecorator object.
 * @author Fabrizio Giustina
 * @version $Id: DecoratorFactory.java 964 2005-12-29 20:56:22Z fgiust $
 */
public interface DecoratorFactory
{

    /**
     * <p>
     * Given a table decorator name, returns a <code>org.displaytag.decorator.TableDecorator</code> instance. The
     * method used to lookup decorator (direct instantiation, load from a pre-istantiated list or from the page context)
     * may vary between different implementations.
     * </p>
     * @param decoratorName String full decorator class name
     * @return instance of TableDecorator
     * @throws DecoratorInstantiationException if unable to load specified TableDecorator
     */
    TableDecorator loadTableDecorator(PageContext pageContext, String decoratorName)
        throws DecoratorInstantiationException;

    /**
     * <p>
     * Given a column decorator name, returns a <code>org.displaytag.decorator.DisplaytagColumnDecorator</code>
     * instance. The method used to lookup decorator (direct instantiation, load from a pre-istantiated list or from the
     * page context) may vary between different implementations.
     * </p>
     * @param decoratorName String full decorator class name
     * @return instance of DisplaytagColumnDecorator
     * @throws DecoratorInstantiationException if unable to load ColumnDecorator
     */
    DisplaytagColumnDecorator loadColumnDecorator(PageContext pageContext, String decoratorName)
        throws DecoratorInstantiationException;

}