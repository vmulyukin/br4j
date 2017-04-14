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
package com.aplana.dbmi.gui;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.card.CardPortlet;

/**
 * Generate the necessary codes to allow an external JSP 
 * page to be called from a portlet manager.
 *  
 * @see {@link CardPortlet}
 * @author Mnagni
 * */
public interface IListEditor {

	public static final String FIELD_ACTION = "action";
	public static final String ACTION_PREFIX = "LE_";
	public static final String ACTION_SAVE = ACTION_PREFIX + "save";
	public static final String ACTION_COMPLETE = ACTION_PREFIX + "complete";
	public static final String ACTION_CLOSE = ACTION_PREFIX + "close";
	public static final String ACTION_SEARCH = ACTION_PREFIX + "search";
	public static final String ATTR_MESSAGE = "message";
	public static final String ATTR_INSTANCE = "IListEditor";

	/**
	 * Sets the {@link ListDataProvider} which managed the data
	 * available to the Portlet
	 * @param dataProvider a <code>ListDataProvider</code> implementation
	 * */
	public abstract void setDataProvider(ListDataProvider dataProvider);

	/**
	 * Implements a Portlet's <code>doView</code> method. 
	 * It is supposed to be called from an external, real, portlet.
	 * It returns a <code>boolean</code> value which could be used to 
	 * notify the calling portlet that this class
	 * has accomplish its task.
	 * 
	 * @param request the calling portlet <code>RenderRequest</code> object
	 * @param render the calling portlet <code>RenderResponse</code> object
	 * @return <code>true</code> if the class is still active, <code>false</code>
	 * otherwise 
	 **/
	public abstract boolean doView(RenderRequest request,
			RenderResponse response) throws PortletException, IOException;

	/**
	 * Implements a Portlet's <code>processAction</code> method. 
	 * It is supposed to be called from an external, real, portlet.
	 * It returns a <code>boolean</code> value which could be used to 
	 * notify the calling portlet that this class
	 * has accomplish its task.
	 * 
	 * @param request the calling portlet <code>ActionRequest</code> object
	 * @param render the calling portlet <code>ActionResponse</code> object
	 * @return <code>true</code> if the class is still active, <code>false</code>
	 * otherwise 
	 **/
	public abstract boolean processAction(ActionRequest request,
			ActionResponse response);

	public abstract ListDataProvider getDataProvider();
}