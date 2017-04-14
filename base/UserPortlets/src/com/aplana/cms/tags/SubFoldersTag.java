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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.NavigationPortlet;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.model.Card;

public class SubFoldersTag implements TagProcessor {
	
	public static final String ATTR_NAVIGATOR = "navigator";

	protected Log logger = LogFactory.getLog(getClass());
	
	private Card navigator;

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		if(!tag.hasAttribute(ATTR_NAVIGATOR)) {
			return false;
		}
		
		String navigatorId = tag.getAttribute(ATTR_NAVIGATOR);
		navigator = NavigationPortlet.getNavigator(cms, navigatorId);
		
		return null != navigator;
	}
	
	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{
		NavigationPortlet.writeSubFolders(out, cms, navigator, true);
	}
}
