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
package com.aplana.dbmi.crypto;

import java.io.InputStream;
import java.io.PrintWriter;
import java.security.cert.Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.ContentUtils;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;

public class SignatureCheckTag implements TagProcessor
{
	public static final String ATTR_PERSON = "person";
	public static final String ATTR_SUCCESS = "success";
	public static final String ATTR_FAILURE = "failure";
	
	public static final String VAR_ERROR = "sign.error";
	
	protected final Log logger = LogFactory.getLog(getClass());
	private boolean success = false;
	private String message;

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception {
		try {
			HtmlAttribute attr = null;
			if (tag.hasAttribute(ContentProducer.ATTR_FIELD))
				attr = (HtmlAttribute) ContentUtils.getAttribute(item,
						tag.getAttribute(ContentProducer.ATTR_FIELD));
			else
				attr = (HtmlAttribute) item.getAttributeById(
						ObjectId.predefined(HtmlAttribute.class, "jbr.uzdo.signature"));
			if (attr == null || attr.getValue() == null || attr.getValue().length() == 0)
				return false;
			
			logger.debug("Signature: " + attr.getValue());
			
			SignatureData signData = new SignatureData(attr.getStringValue(), item);
			success = signData.verify(cms.getService(), true);
			message = signData.getMessage();
			
			//*************************************************************
		} catch (Exception e) {
			logger.error("Error during signature check", e);
			message = e.getMessage();
		}
		if (!success)
			message = ContextProvider.getContext().getLocaleMessage("signature.error.bad");
		return true;
	}
	
	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms) {
		cms.setVariable(VAR_ERROR, message);
		cms.writeContent(out, tag.getAttribute(success ? ATTR_SUCCESS : ATTR_FAILURE), item);
		cms.clearVariable(VAR_ERROR);
	}
}
