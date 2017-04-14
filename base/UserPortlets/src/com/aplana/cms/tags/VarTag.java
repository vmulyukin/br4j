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
import com.aplana.cms.ContentUtils;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.model.Card;

public class VarTag implements TagProcessor
{
	public static final String ATTR_VAR = "var";
	public static final String ATTR_SET = "set";
	
	protected Log logger = LogFactory.getLog(getClass());
	private Object value = null;

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		if(!tag.hasAttribute(ATTR_VAR))
			throw new Exception("No variable defined in tag");
		if (tag.hasAttribute(ATTR_SET)) {
			cms.setVariable(tag.getAttribute(ATTR_VAR),
					cms.expandContent(tag.getAttribute(ATTR_SET), item));
			value = "";
			return true;
		}
		value = cms.getVariable(tag.getAttribute(ATTR_VAR));
		if (value == null) {
			logger.warn("Variable " + tag.getAttribute(ATTR_VAR) + " not defined");
			return false;
		}
		return true;
	}
	
	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{
		ContentUtils.writeText(out, value.toString());
	}
}
