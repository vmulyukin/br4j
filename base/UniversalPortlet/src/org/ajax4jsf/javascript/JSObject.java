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
package org.ajax4jsf.javascript;

import java.util.List;

/**
 * @author shura (latest modification by $Author: alexsmirnov $)
 * @version $Revision: 1.1.2.1 $ $Date: 2007/01/09 18:58:30 $
 *
 */
public class JSObject extends JSFunction
{

    /**
     * @param name
     */
    public JSObject(String name)
    {
        super(name);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param name
     * @param parameters
     */
    public JSObject(String name, List parameters)
    {
        super(name, parameters);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param name
     * @param parameter
     */
    public JSObject(String name, Object parameter)
    {
        super(name, parameter);
        // TODO Auto-generated constructor stub
    }

    public void appendScript(StringBuffer functionString)
    {
        functionString.append("new ");
        super.appendScript(functionString);
    }

}
