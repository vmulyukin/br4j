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
package com.aplana.cms.tags;

import java.io.PrintWriter;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.NavigationPortlet;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class SwitchNavigatorButtonTag implements TagProcessor {
	
	private static final String INCOMING_NAVIGATOR = "navigator";
	private static final String CONTROL_NAVIGATOR = "navigator2";
	
	private static final ObjectId ROLE_MINISTR = ObjectId.predefined(SystemRole.class, "jbr.minister");

	protected Log logger = LogFactory.getLog(getClass());

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		try {
			final Person curPerson = (Person) cms.getService().getById(Person.ID_CURRENT);
			for(Role role : (Collection<Role>) curPerson.getRoles() ) {
				if (ROLE_MINISTR.equals(role.getSystemRole().getId())) {
					return false;
				}
			}
		} catch (DataException e) {
			return false;
		} catch (ServiceException e) {
			return false;
		}
		
		return true;
	}
	
	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{
		String currentNavigator = 
			(String) cms.getRequest().getSessionAttribute(NavigationPortlet.ATTR_CURRENT_NAVIGATOR_NAME);
		String newNavigator = 
			(String) cms.getRequest().getSessionAttribute(NavigationPortlet.ATTR_NEW_NAVIGATOR_NAME);
		
		if(null == currentNavigator) {
			writeControlButton(out);
			return;
		}
		
		if(INCOMING_NAVIGATOR.equals(newNavigator) && !newNavigator.equals(currentNavigator)) {
			writeControlButton(out);
			return;
		}
		
		if(CONTROL_NAVIGATOR.equals(newNavigator) && !newNavigator.equals(currentNavigator)) {
			writeIncomingButton(out);
			return;
		}
		
		if(INCOMING_NAVIGATOR.equals(currentNavigator)) {
			writeControlButton(out);
		} else {
			writeIncomingButton(out);
		}
	}
	
	private void writeControlButton(PrintWriter out) {
		out.write("<a href=\"/portal/auth/portal/boss/folder/LeftMenu?action=1&navigatorName=navigator2\" class=\"button shield\"></a>");
	}
	
	private void writeIncomingButton(PrintWriter out) {
		out.write("<a href=\"/portal/auth/portal/boss/folder/LeftMenu?action=1&navigatorName=navigator\" class=\"button incoming\"></a>");
	}
}