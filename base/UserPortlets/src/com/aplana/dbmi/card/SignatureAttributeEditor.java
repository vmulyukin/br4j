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
package com.aplana.dbmi.card;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.portlet.PortletRequest;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.util.AttributeUtil;

public class SignatureAttributeEditor extends StringAttributeEditor
{
	
	private static final ObjectId CARDSTATE_FINAL = ObjectId.predefined(
			CardState.class, "draft");
	
	private CardPortletSessionBean sBean = null;
	
	
	
	
	
	
	
	
	public void initEditor(PortletRequest request, Attribute attr) throws DataException {		
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		
		try {	
			//readConfig(sessionBean);	
			//sessionBean.getActiveCardInfo().setAttributeEditorData(attr.getId(), "FieldsToSign", resultMap);			
		} catch (Exception e) {
			logger.error("error in initEditor", e);
		}
	}
	
	public SignatureAttributeEditor() {		
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/SignatureEdit.jsp");
	}
	
}
