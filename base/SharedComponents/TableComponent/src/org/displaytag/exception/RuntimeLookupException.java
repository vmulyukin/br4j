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
package org.displaytag.exception;

import org.displaytag.Messages;


/**
 * runtime exception thrown during sorting when a checked exception can't be used.
 * @author Fabrizio Giustina
 * @version $Revision: 720 $ ($Author: fgiust $)
 */
public class RuntimeLookupException extends RuntimeException
{

    /**
     * D1597A17A6.
     */
    private static final long serialVersionUID = 899149338534L;

    /**
     * @param sourceClass class where the exception is thrown
     * @param property object property who caused the exception
     * @param cause previous (checked) exception
     */
    public RuntimeLookupException(Class sourceClass, String property, BaseNestableJspTagException cause)
    {
        super(Messages.getString("RuntimeLookupException.msg", //$NON-NLS-1$
            new Object[]{property, cause.getMessage()}));
    }

}