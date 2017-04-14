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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author shura (latest modification by $Author: alexsmirnov $)
 * @version $Revision: 1.1.2.2 $ $Date: 2007/01/24 13:22:31 $
 *
 */
public class JSFunctionDefinition extends ScriptStringBase implements ScriptString
{


    
    private List parameters = new ArrayList();
    
    private StringBuffer body = new StringBuffer();
    
    private String name ;


	public JSFunctionDefinition(){
    }

    public JSFunctionDefinition( List parameters)
    {

        this.parameters = parameters;
    }

    /**
     * @param body
     * @param parameters
     */
    public JSFunctionDefinition( Object parameter)
    {
        this.parameters.add(parameter);
    }


    public JSFunctionDefinition addParameter(Object parameter)
    {
        this.parameters.add(parameter);
        return this;
    }
    
    public JSFunctionDefinition addToBody(Object body)
    {
        this.body.append(body);
        return this;
    }
    /* (non-Javadoc)
     * @see org.ajax4jsf.components.renderkit.scriptutils.ScriptString#appendScript(java.lang.StringBuffer)
     */
    public void appendScript(StringBuffer functionString)
    {
    	functionString.append("function");
    	if(null != name){
    		functionString.append(" ").append(name);
    	}
    	functionString.append("(");
        boolean first = true;
        for (Iterator param = parameters.iterator(); param.hasNext();)
        {
            Object element =  param.next();
            if(!first){
                functionString.append(',');
            }
            functionString.append(element.toString());
            first = false;
        }
        functionString.append("){").append(body).append("}");
    }

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
