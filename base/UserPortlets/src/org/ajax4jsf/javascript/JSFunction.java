/**
 * License Agreement.
 *
 * Rich Faces - Natural Ajax for Java Server Faces (JSF)
 *
 * Copyright (C) 2007 Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
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
