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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.gui.FilteredCards;
import com.aplana.dbmi.gui.IListEditor;
import com.aplana.dbmi.gui.LinkChooser;
import com.aplana.dbmi.gui.ListDataProvider;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.service.DataException;

public class HtmlAttributeEditor extends JspAttributeEditor
{
	public static final String LINK_FILE = "file";
	public static final String LINK_IMAGE = "image";
	public static final String LINK_CARD = "card";
	
	public static final String CHOOSE_LINK_ACTION = "chooseLink";
	
	protected Log logger = LogFactory.getLog(getClass());
	
	public HtmlAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/Html.jsp");
		setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/HtmlInclude.jsp");
	}

	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		String value = request.getParameter(CardPortlet.getAttributeFieldName(attr));
		if (value == null)
			return false;
		((HtmlAttribute) attr).setValue(value);
		return true;
	}

	public boolean processAction(ActionRequest request, ActionResponse response,
			Attribute attr) throws DataException {
        String action = request.getParameter(CardPortlet.ACTION_FIELD);
        if (!CHOOSE_LINK_ACTION.equals(action))
        	return false;
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
    	try {
			String linkType = request.getParameter(LinkChooser.CONTENT_TYPE);
			ListDataProvider adapter = new FilteredCards(sessionBean.getServiceBean(), loadFilter(linkType), "");
			IListEditor editor = new LinkChooser();
			/*sets the selected editor in order to warn the code that a "CHOOSE_LINK_ACTION"
				is on the run (better code would use properties in the HTMLAttribute as for 
				the CardLinkAttribute of EDIT_LINKS_ACTION). This property is set to empty
				when the javascript in the page retrieve the necessary informations.
			 */					
			String attrId = request.getParameter(CardPortlet.ATTR_ID_FIELD);
			sessionBean.getLinkChooserBean().setSelectedEditor(attrId);
			sessionBean.getLinkChooserBean().setLinkType(linkType);
			editor.setDataProvider(adapter);
			//sessionBean.setExternalEditor(editor);
			sessionBean.openForm(new ListEditForm(editor));
		} catch (Exception e) {
			logger.error("Link editing form constructing error", e);
            //sessionBean.setMessage(getMessage(request, "db.side.error.msg") + e.getMessage());
			throw new DataException("db.side.error.msg", e);
		}
		return true;
	}
	
	private Search loadFilter(String type) {
		Search filter = new Search();
		filter.setTemplates(new ArrayList());
		String file = null;
		if (LINK_FILE.equals(type))
			file = "dbmi/allFiles.xml";
		else if (LINK_IMAGE.equals(type))
			file = "dbmi/allImages.xml";
		if (file != null) {
			try {
				InputStream xml = Portal.getFactory().getConfigService().loadConfigFile(file);
				filter.initFromXml(xml);
				xml.close();
			} catch (Exception e) {
				logger.error("Error initializing search ", e);
			}
		}
		return filter;
	}
	
	public boolean isValueCollapsable() {
		return true;
	}	
}
