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
package org.displaytag.util;

import java.util.Map;


/**
 * A RequestHelper object is used to read parameters from the request. Main features are handling of numeric parameters
 * and the ability to create Href objects from the current request.
 * @author Fabrizio Giustina
 * @version $Revision: 720 $ ($Author: fgiust $)
 */
public interface RequestHelper
{

    /**
     * return the current Href for the request (base url and parameters).
     * @return Href
     */
    Href getHref();

    /**
     * Reads a String parameter from the request.
     * @param key String parameter name
     * @return String parameter value
     */
    String getParameter(String key);

    /**
     * Reads an Integer parameter from the request.
     * @param key String parameter name
     * @return Integer parameter value or null if the parameter is not found or it can't be transformed to an Integer
     */
    Integer getIntParameter(String key);

    /**
     * Returns a Map containing all the parameters in the request.
     * @return Map
     */
    Map getParameterMap();

}
