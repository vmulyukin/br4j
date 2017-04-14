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

import javax.portlet.PortletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.aplana.dbmi.card.AttributeEditor;
import com.aplana.dbmi.card.AttributeEditorFactory;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeViewParam;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;

public class AttributeView {
	
	protected Attribute attribute;
	private AttributeEditor editor;
	private boolean readOnly  = true;
	private boolean visible   = true;
	private boolean mandatory = false;
	protected final Log logger = LogFactory.getLog(getClass());
	
	public AttributeView(Attribute attribute){
		if (attribute == null) {
			throw new NullPointerException();
		}
		this.attribute = attribute;
		readOnly  = attribute.isReadOnly();
		visible   = !attribute.isHidden();
		mandatory = attribute.isMandatory();
	}
	
	public void applyViewParameters(AttributeViewParam avp) {
		try {
			readOnly  = avp.isReadOnly();
			visible   = !avp.isHidden();
			mandatory = avp.isMandatory();
		} catch (NullPointerException error) {
			logger.error("An 'NullPointerException' error occurred while performing the method 'applyViewParameters'", error);
		}
	}
	
	public void initEditor(PortletRequest request){
		CardPortletSessionBean sessionBean = (CardPortletSessionBean)request.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
		if ((!readOnly) && (CardPortlet.CARD_EDIT_MODE.equals(sessionBean.getActiveCardInfo().getMode()))) {
			editor = AttributeEditorFactory.getFactory().getEditor(attribute, sessionBean);
		} else {
			editor = AttributeEditorFactory.getFactory().getViewer(attribute, sessionBean);
		}
		try {
			if (editor != null) {
				editor.initEditor(request, attribute);
			}
		} catch (DataException error) {
			logger.error("An 'DataException' error occurred while performing the method 'initEditor'", error);
		}
	}
	
	public Attribute getAttribute() {
		return attribute;
	} 
	
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
		if (attribute != null) {
			readOnly = attribute.isReadOnly();
			visible = !attribute.isHidden();
			mandatory = attribute.isMandatory();
		}
	}
	
	public AttributeEditor getEditor() {
		return editor;
	}
	
	public void setEditor(AttributeEditor editor) {
		this.editor = editor;
	}
	
	public ObjectId getId() {
		return attribute.getId();
	}

	public String getName() {
		return attribute.getName();
	}

	public boolean isEmpty() {
		return attribute.isEmpty();
	}

	public boolean isSystem() {
		return attribute.isSystem();
	}

	public boolean isActive() {
		return attribute.isActive();
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public int getSearchOrder() {
		return (attribute != null) ? attribute.getSearchOrder() : 0;
	}

	public String getStringValue() {
		return (attribute != null) ? attribute.getStringValue() : null;
	}

	public Object getType() {
		return (attribute != null) ? attribute.getType() : null;
	}

}