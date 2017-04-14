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
package org.displaytag.export;

import org.displaytag.model.TableModel;


/**
 * Interface for export classes. ExportViewFactory is responsible for registering and initialization of export views. A
 * default, no parameters constructor is required. The <code>setParameters()</code> method is guarantee to be called
 * before any other operation.
 * @author Fabrizio Giustina
 * @version $Revision: 720 $ ($Author: fgiust $)
 */
public interface ExportView
{

    /**
     * initialize the parameters needed for export. The method is guarantee be called before <code>doExport()</code>
     * and <code>getMimeType()</code>. Classes implementing ExportView should reset any instance field previously set
     * when this method is called, in order to support instance reusing.
     * @param tableModel TableModel to render
     * @param exportFullList boolean export full list?
     * @param includeHeader should header be included in export?
     * @param decorateValues should output be decorated?
     */
    void setParameters(TableModel tableModel, boolean exportFullList, boolean includeHeader, boolean decorateValues);

    /**
     * MimeType to return.
     * @return String mime type
     */
    String getMimeType();

}
