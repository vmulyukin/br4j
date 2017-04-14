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
/*
 * BR4J00000867: 23.11.2010 - N.Zhegalin
 */
package com.aplana.dbmi.card;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.actionhandler.descriptor.ActionHandlerDescriptor;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptor;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptorReader;
import com.aplana.dbmi.card.actionhandler.jbr.ValuesGetter;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;

/**
 * Editor for text attributes that can have a set of predefined templates to
 * fill value of the attribute. A set of elements that allows to add template is
 * configure like 'actions'. The {@link #USER_ROLES_PARAMETER} parameter of
 * action can be used to define system roles, users of that can use this
 * element. Handler of action is used to define {@link ValuesGetter} that allows
 * of getting list of predefined templates.
 */
public class TextWithTemplatesAttributeEditor extends JspAttributeEditor {
    public static String CONFIG_PARAM = "config";

    public static String ACTIONS = "actions";
    public static String VALUES = "values";

    CardPortletSessionBean sessionBean = null;
    private String config = null;

    private static final String USER_ROLES_PARAMETER = "user-roles";
    private Collection<ObjectId> currentUserRoles = null;

    /*
     * (non-Javadoc)
     * 
     * @see com.aplana.dbmi.card.JspAttributeEditor#initEditor(javax.portlet.PortletRequest,
     *      com.aplana.dbmi.model.Attribute)
     */
    @Override
    public void initEditor(PortletRequest request, Attribute attr)
	    throws DataException {
	super.initEditor(request, attr);
	sessionBean = getCardPortletSessionBean(request);
	if (sessionBean == null)
	    throw new DataException(new NullPointerException(
		    "sessionBean was not resolved"));
	try {
	    ActionsDescriptor descr = loadConfig();
	    if (descr == null) {
		throw new IllegalStateException("Config is not defined");
	    }
	    Set<String> actionIds = descr.getActionIds();
	    Map<String, ActionHandlerDescriptor> actionsMap = new HashMap<String, ActionHandlerDescriptor>();
	    Map<String, List<String>> valuesMap = new HashMap<String, List<String>>();
	    for (String actionId : actionIds) {
		ActionHandlerDescriptor actionHandler = descr
			.getActionHandlerDescriptor(actionId);

		if (!isVisible(actionHandler))
		    continue;
		actionsMap.put(actionId, actionHandler);
		Class<?> clazz = actionHandler.getHandlerClass();
		if (!ValuesGetter.class.isAssignableFrom(clazz)) {
		    logger.warn(String
			    .format("Error of '%s' action: "
				    + "Handler should implement ValuesGetter",
				    actionId));
		    continue;
		}
		try {
		    ValuesGetter instance = (ValuesGetter) clazz.newInstance();
		    List<String> values = instance.getValues(sessionBean
			    .getServiceBean());
		    valuesMap.put(actionHandler.getId(), values);
		} catch (Exception e) {
		    logger.warn(String.format(
			    "Unable to load values for '%s' action", actionId),
			    e);
		}
	    }
	    sessionBean.getActiveCardInfo().setAttributeEditorData(
		    attr.getId(), ACTIONS, actionsMap);
	    sessionBean.getActiveCardInfo().setAttributeEditorData(
		    attr.getId(), VALUES, valuesMap);
	} catch (Exception e) {
	    logger.error("Failed to initialize editor", e);
	}
    }

    /**
     * Read configuration from file. File defined by the {@link #CONFIG_PARAM}
     * parameter.
     * 
     * @return actions descriptor - instance of {@link ActionsDescriptor}
     * @throws Exception
     */
    protected ActionsDescriptor loadConfig() throws Exception {
	if (config != null) {
	    final InputStream stream = Portal.getFactory().getConfigService()
		    .loadConfigFile(
			    AttributeEditorFactory.CONFIG_FOLDER + config);
	    ActionsDescriptorReader reader = new ActionsDescriptorReader();
	    return reader.readFromFile(stream, sessionBean.getServiceBean());
	}
	return null;
    }

    /**
     * Checks whether current user can use element defined by given
     * <code>actionHandler</code>.
     * 
     * @param actionHandler
     *                instance of {@link ActionHandlerDescriptor} that define
     *                element for checking
     * @return whether current user can use element
     * @throws Exception
     */
    private boolean isVisible(ActionHandlerDescriptor actionHandler)
	    throws Exception {
	if (currentUserRoles == null) {
	    Person user;
	    user = (Person) sessionBean.getServiceBean().getById(
		    Person.ID_CURRENT);
	    @SuppressWarnings("unchecked")
	    Collection<Role> actualRoles = user.getRoles();
	    currentUserRoles = new ArrayList<ObjectId>();
	    for (Role actualRole : actualRoles) {
		currentUserRoles.add(actualRole.getSystemRole().getId());
	    }
	}
	boolean isVisible = true;
	if (actionHandler.getParameters().containsKey(USER_ROLES_PARAMETER)) {
	    String userRoles = actionHandler.getParameters().get(
		    USER_ROLES_PARAMETER);
	    @SuppressWarnings("unchecked")
	    List<ObjectId> roles = ObjectIdUtils.commaDelimitedStringToIds(
		    userRoles, SystemRole.class);
	    isVisible = ObjectIdUtils.isIntersectionDataObjects(roles,
		    currentUserRoles);
	}
	return isVisible;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aplana.dbmi.card.JspAttributeEditor#setParameter(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void setParameter(String name, String value) {
	if (CONFIG_PARAM.equals(name)) {
	    config = value;
	} else {
	    super.setParameter(name, value);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aplana.dbmi.card.JspAttributeEditor#gatherData(javax.portlet.ActionRequest,
     *      com.aplana.dbmi.model.Attribute)
     */
    @Override
    public boolean gatherData(ActionRequest request, Attribute attr)
	    throws DataException {
	String value = request.getParameter(JspAttributeEditor
		.getAttrHtmlId(attr));
	if (value == null)
	    return false;
	((TextAttribute) attr).setValue(value);
	return true;
    }

    /*
     * F(non-Javadoc)
     * 
     * @see com.aplana.dbmi.card.JspAttributeEditor#isValueCollapsable()
     */
    @Override
    public boolean isValueCollapsable() {
	return true;
    }
}
