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

/**
 * Create reference to JavaScript variable with optional index.
 * @author asmirnov@exadel.com (latest modification by $Author: alexsmirnov $)
 * @version $Revision: 1.1.2.1 $ $Date: 2007/01/09 18:58:30 $
 *
 */
public class JSReference extends ScriptStringBase {
	
	public static final JSReference THIS = new JSReference("this");

	public static final JSReference TRUE = new JSReference("true");
	public static final JSReference FALSE = new JSReference("false");
	public static final JSReference NULL = new JSReference("null");

	private String name;
	private Object index=null;
	
	/**
	 * @param name
	 */
	public JSReference(String name) {
		// TODO Auto-generated constructor stub
		this.name = name;
	}

	/**
	 * @param name
	 * @param index
	 */
	public JSReference(String name, Object index) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.index = index;
	}

	/* (non-Javadoc)
	 * @see org.ajax4jsf.javascript.ScriptString#appendScript(java.lang.StringBuffer)
	 */
	public void appendScript(StringBuffer functionString) {
		functionString.append(name);
		if (null != index) {
			functionString.append("[").append(ScriptUtils.toScript(index)).append("]");
		}

	}

}
