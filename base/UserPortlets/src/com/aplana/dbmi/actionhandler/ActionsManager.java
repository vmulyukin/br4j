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
package com.aplana.dbmi.actionhandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.actionhandler.descriptor.ActionHandlerDescriptor;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptor;
import com.aplana.dbmi.card.ExtraJavascriptInfo;
import com.aplana.dbmi.card.Parametrized;
import com.aplana.dbmi.card.PortletFormManager;
import com.aplana.dbmi.card.PortletFormManagerAware;
import com.aplana.dbmi.card.extra.ExtraJavascriptBuilder;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;

/**
 * ActionsManager - ��������������� �����, ��������������� ��� ���������� ������ � ���������
 * ActionHandler. ��������� ��� �������:
 * 1) ���������� ������ ��������, ������� �������� �������� ������������, ��. 
 * ����� {@link #getActiveActionIds()}
 * 2) ���������� ������������� ActionHandler'� (����� ��������� ���� ������� � ���������� �����
 * ����������� ��������), ��. ������ {@link #initializeInstance(ActionHandler)}
 * 
 * ������ ���������� ����� - ���� ��� �������� ���������� ����������
 * @see {@link com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionsManager}
 * @author DSultanbekov
 */
public abstract class ActionsManager {
	protected final Log logger = LogFactory.getLog(getClass());
	
	private ActionsDescriptor actionsDescriptor;
	private List<String> activeActionIds;
	private AsyncDataServiceBean serviceBean;
	private PortletFormManager portletFormManager;

	public PortletFormManager getPortletFormManager() {
		return portletFormManager;
	}

	public void setPortletFormManager(PortletFormManager portletFormManager) {
		this.portletFormManager = portletFormManager;
	}

	protected AsyncDataServiceBean getServiceBean() {
		return serviceBean;
	}

	public void setServiceBean(AsyncDataServiceBean serviceBean) {
		this.serviceBean = serviceBean;
	}

	public ActionsDescriptor getActionsDescriptor() {
		return actionsDescriptor;
	}
	
	public void setActionsDescriptor(ActionsDescriptor ad) {
		this.actionsDescriptor = ad;
		activeActionIds = null;
	}

	/**
	 * @return ������ id ��������, ������������� �� ��������������� ��������.
	 */
	public List<String> getActiveActionIds() {
		if (activeActionIds == null) {
			activeActionIds = new ArrayList<String>();
			for( String actionId : actionsDescriptor.getSortedActionIds()) {
				try {
					final ActionHandler h = createInstance(actionId);
					if (h.isApplicableForUser()) {
						activeActionIds.add(actionId);
					} else {
						logger.info( h.toString() + " is not applicable for the current user");
					}
				} catch (Exception e) {
					logger.error("Error occured during verification of actionHandler with id: " + actionId, e);
				}
			}
		}
		return activeActionIds;
	}

	public List<ActionHandlerDescriptor> getActiveActionDescriptors() {
		final List<String> activeIdsList = getActiveActionIds();
		final List<ActionHandlerDescriptor> result = new ArrayList<ActionHandlerDescriptor>(activeIdsList.size());
		for( String actionId : activeIdsList) {
			final ActionHandlerDescriptor d = actionsDescriptor.getActionHandlerDescriptor(actionId);
			result.add(d);
		}
		return result;
	}

	public final ActionHandler createInstance(String actionId) {
		final ActionHandlerDescriptor d = actionsDescriptor.getActionHandlerDescriptor(actionId);
		ActionHandler result = null; 
		try {
			result = d.getHandlerClass().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Failed to instantiate ActionHandler class:" + e.getMessage(), e);
		}

		if (result instanceof Parametrized) {
			final Parametrized p = (Parametrized) result;
			for (Map.Entry<String, String> entry: d.getParameters().entrySet()) { 
				p.setParameter(entry.getKey(), entry.getValue()); 
			}
		} else if (d.getParameters() != null && d.getParameters().size() > 0){
			logger.warn(d.getHandlerClass().getName() + " is not Parametrized descendant. Parameters will be ignored");
		}
		
		result.setCondition(d.getCondition());
		result.setServiceBean(serviceBean);
		if (result instanceof PortletFormManagerAware) {
			((PortletFormManagerAware)result).setPortletFormManager(portletFormManager);
		}
		initializeInstance(result);
		return result;
	}

	public JSONArray getActionsJSON(Attribute attr) {
		final JSONArray result = new JSONArray();
		try {
			for( String actionId : getActiveActionIds()) {
				final ActionHandlerDescriptor ad = getActionsDescriptor().getActionHandlerDescriptor(actionId);
				JSONObject ja = new JSONObject();
				ja.put("title", ad.getTitle().getValue());
				ja.put("id", ad.getId());
				ja.put("selectionType", ad.getSelectionType().toString());

                if (ad.getExtraJavascriptInfo() != null && attr != null) {

                    ExtraJavascriptBuilder aJSBuilder =
                            ExtraJavascriptBuilder.newInstance(ad.getExtraJavascriptInfo());
                    String entryPoint = aJSBuilder.getEntryPoint(attr);
                    if (entryPoint != null ) {
                        ja.put("jsEntryPoint", aJSBuilder.getEntryPoint(attr));
                    }

                }

				if (ad.isNeedConfirmation()) {
					ja.put("confirmation", ad.getConfirmation().toString());	
				}
				result.put(ja);
			}
		} catch (JSONException e) {
			logger.error("Failed to serialize actions to JSON string", e);
			return new JSONArray();
		} catch (PortletException pe){
            logger.error("Failed to extract ExtraJavascriptBuilder", pe);
            return new JSONArray();
        }
		return result;
	}
	
	public JSONArray getActionsJSON() {
		return getActionsJSON(null);
	}

    public List<ExtraJavascriptInfo> getActionsExtraJavascriptInfo() {
        List<ExtraJavascriptInfo> result = new ArrayList<ExtraJavascriptInfo>();

        for (String actionId : getActiveActionIds()) {
            final ActionHandlerDescriptor ad = getActionsDescriptor().getActionHandlerDescriptor(actionId);
            if (ad.getExtraJavascriptInfo() != null) {
                result.add(ad.getExtraJavascriptInfo());
            }
        }
        return result;
    }


    public abstract boolean processAction(ActionRequest request, ActionResponse response) throws DataException;

	protected abstract void initializeInstance(ActionHandler handler);

}
