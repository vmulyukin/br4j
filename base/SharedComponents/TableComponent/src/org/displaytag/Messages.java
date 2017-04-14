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
package org.displaytag;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Helper class for message bundle access.
 * @author Fabrizio Giustina
 * @version $Revision: 737 $ ($Author: fgiust $)
 */
public final class Messages
{

    /**
     * Base name for the bundle.
     */
    private static final String BUNDLE_NAME = "org.displaytag.messages"; //$NON-NLS-1$

    /**
     * Loaded ResourceBundle.
     */
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Don't instantiate.
     */
    private Messages()
    {
        // unused
    }

    /**
     * Returns a message from the resource bundle.
     * @param key Message key.
     * @return message String.
     */
    public static String getString(String key)
    {
        try
        {
            return RESOURCE_BUNDLE.getString(key);
        }
        catch (MissingResourceException e)
        {
            return '!' + key + '!';
        }
    }

    /**
     * Reads a message from the resource bundle and format it using java MessageFormat.
     * @param key Message key.
     * @param parameters Parameters to pass to MessageFormat.format()
     * @return message String.
     */
    public static String getString(String key, Object[] parameters)
    {
        String baseMsg;
        try
        {
            baseMsg = RESOURCE_BUNDLE.getString(key);
        }
        catch (MissingResourceException e)
        {
            return '!' + key + '!';
        }

        return MessageFormat.format(baseMsg, parameters);
    }

    /**
     * Reads a message from the resource bundle and format it using java MessageFormat.
     * @param key Message key.
     * @param parameter single parameter to pass to MessageFormat.format()
     * @return message String.
     */
    public static String getString(String key, Object parameter)
    {
        return getString(key, new Object[]{parameter});
    }
}