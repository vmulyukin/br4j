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
 * @version $Revision: 1.1.2.3 $ $Date: 2007/02/06 16:23:26 $
 * 
 */
public class JSFunction extends ScriptStringBase implements ScriptString {

	private String name;

	private List parameters = null;

	/**
	 * @param name
	 */
	public JSFunction(String name) {
		// TODO Auto-generated constructor stub
		this.name = name;
	}

	/**
	 * @param name
	 * @param parameters
	 */
	public JSFunction(String name, List parameters) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.parameters = parameters;
	}

	/**
	 * @param name
	 * @param parameters
	 */
	public JSFunction(String name, Object parameter) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.parameters = new ArrayList(1);
		this.parameters.add(parameter);
	}

	public JSFunction addParameter(Object parameter) {
		getParameters().add(parameter);
		return this;
	}

	public void appendScript(StringBuffer functionString)  {
		functionString.append(name).append('(');
		boolean first = true;
		List parameters = getParameters();
		if (null != parameters) {
			for (Iterator param = parameters.iterator(); param.hasNext();) {
				Object element = param.next();
				if (!first) {
					functionString.append(',');
				}
				if (null != element) {
                    try {
                        functionString.append(ScriptUtils.toScript(element));
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                } else {
					functionString.append("null");
				}
				first = false;
			}

		}
		functionString.append(")");
	}

	/**
	 * @return the parameters
	 */
	public List getParameters() {
		if (this.parameters == null) {
			this.parameters = new ArrayList();
		}
		return this.parameters;
	}
}
