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

import com.aplana.dbmi.card.extra.ExtraJavascriptBuilder;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.search.ext.RouteSearchObjectId;
import com.aplana.dbmi.model.search.ext.RouteSearchObjectId.RouteSearchNode;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.UserPrincipal;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.portlet.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class JspAttributeEditor implements AttributeEditor, Parametrized
{
	protected static final String PARAM_JSP = "jsp";
	protected static final String PARAM_INIT_JSP = "initJsp";
	protected static final String PARAM_FULL_RENDERING = "fullRendering";

	public static final String ATTR_ATTRIBUTE = Attribute.class.getName();
	public static final String ATTR_EXTRA_JAVASCRIPT = ExtraJavascriptBuilder.class.getName();

	protected Log logger = LogFactory.getLog(getClass());
	private String jspPath;
	private String initJspPath;
	protected boolean fullRendering = false;
	private List<ExtraJavascriptInfo> extraJavascriptInfoList;

	private boolean removeReferenceData = true;

	/**
	 * �����, ��������������� ��� ��������� ����������� ��������������, ������� �����
	 * ������������ � HTML-�������� ��� ����������� ���������, ��������� � ������ ���������
	 * @param attr �������
	 * @return ���������� ��� ��������, ��������� ��� ������������� � HTML
	 */
	public static String getAttrHtmlId(Attribute attr) {
		String st = null;
		if(attr.getId() instanceof RouteSearchObjectId) {
			st = getRouteAttrHtmlId((RouteSearchObjectId) attr.getId());
		} else {
			st = (String)attr.getId().getId();	
		}
		return getAttrHtmlId(st);
	}

	/**
	 * �����, ��������������� ��� ��������� ����������� ��������������, ������� �����
	 * ������������ � HTML-�������� ��� ����������� ���������, ��������� � ������ ���������
	 * @param attrId ������������� ��������
	 * @return ���������� ��� ��������, ��������� ��� ������������� � HTML
	 */
	public static String getAttrHtmlId(String attrId) {
		return "attr_" + new String(Hex.encodeHex(attrId.getBytes()));
	}

	private static String getRouteAttrHtmlId(RouteSearchObjectId routeId) {
		String result = "";

			List<List<RouteSearchNode>> fullList =  routeId.getRoutes();
			if (fullList != null) {
				for (List<RouteSearchNode> subList : fullList) {
					if (subList != null) {
						for (RouteSearchNode obj : subList) {
							if (obj != null) {
								if (obj.getLinkAttr() != null) {
									if (!result.isEmpty()) {
										result += "_";
									}
									result += obj.getLinkAttr().getId();
								}
							}
						}
					}
				}
			}

		return result;
	}

	protected CardPortletSessionBean getCardPortletSessionBean(PortletRequest request) {
	    PortletSession session = request.getPortletSession();
        if (session == null) {
            logger.warn("Portlet session is not exists yet.");
            return null;
        }
        CardPortletSessionBean sessionBean = (CardPortletSessionBean) session.getAttribute(CardPortlet.SESSION_BEAN);
        String userName = (String) request.getPortletSession().getAttribute(DataServiceBean.USER_NAME, PortletSession.APPLICATION_SCOPE);
        if (userName != null) {
            sessionBean.getServiceBean().setUser(new UserPrincipal(userName));
            sessionBean.getServiceBean().setIsDelegation(true);
            sessionBean.getServiceBean().setRealUser(request.getUserPrincipal());
        } else {
            sessionBean.getServiceBean().setUser(request.getUserPrincipal());
            sessionBean.getServiceBean().setIsDelegation(false);
        }
        return sessionBean;
	}

	public void setExtraJavascriptInfoList(List<ExtraJavascriptInfo> extraJavascriptInfoList) {
		this.extraJavascriptInfoList = extraJavascriptInfoList;
	}

	public void setRemoveReferenceData(boolean removeReferenceData) {
		this.removeReferenceData = removeReferenceData;
	}

	public void writeCommonCode(RenderRequest request, RenderResponse response)
			throws IOException, PortletException {
		if (initJspPath == null)
			return;
		//response.flushBuffer();
		//request.setAttribute(ATTR_EDITOR, this);
		PortletRequestDispatcher rd = request.getPortletSession().getPortletContext()
				.getRequestDispatcher(initJspPath);
		rd.include(request, response);
	}

	public void writeEditorCode(RenderRequest request, RenderResponse response,
			Attribute attr) throws IOException, PortletException {
		final Map<String, Object> referenceData = getReferenceData(attr, request);
		try {
			if (referenceData != null) {
				for (Map.Entry<String, Object> entry: referenceData.entrySet()) {
					final String key = entry.getKey();
					if (request.getAttribute(key) != null) {
						throw new RuntimeException("Attribute with name " + key + " is already defined in request object");
					}
					request.setAttribute(key, entry.getValue());
				}
			}
			request.setAttribute(ATTR_ATTRIBUTE, attr);

			String extraJavaScript = formExtraJavaScript(request, attr);
			if (extraJavaScript.length() > 0) {
				request.setAttribute(ATTR_EXTRA_JAVASCRIPT, extraJavaScript);
			}

			PortletRequestDispatcher rd = request.getPortletSession().getPortletContext()
					.getRequestDispatcher(jspPath);
			rd.include(request, response);
		} finally {
			if (referenceData != null && removeReferenceData) {
				for (String key: referenceData.keySet()) {
					request.removeAttribute(key);
				}
			}
		}
	}

	protected String formExtraJavaScript(RenderRequest request, Attribute attr) throws PortletException,
			IllegalArgumentException {
		return formExtraJavaScriptFromList(attr, extraJavascriptInfoList);
	}

	protected String formExtraJavaScriptFromList(Attribute attr, List<ExtraJavascriptInfo> extraJavascriptList) throws PortletException,
			IllegalArgumentException {
		StringBuilder stringBuilder = new StringBuilder();
		if (extraJavascriptList != null) {
			for (ExtraJavascriptInfo extraJavascriptInfo : extraJavascriptList) {
				ExtraJavascriptBuilder.newInstance(extraJavascriptInfo).addJavascript(attr, stringBuilder);
			}
		}
		return stringBuilder.toString();
	}

	/**
	 * ��-��������� ������� ���������
	 */
    public boolean isCollapsedByDefault() {
        return false;
    }

    public void setParameter(String name, String value) {
		if (PARAM_JSP.equalsIgnoreCase(name)) {
			jspPath = value;
		} else if (PARAM_INIT_JSP.equalsIgnoreCase(name)) {
			initJspPath = value;
		} else if (PARAM_FULL_RENDERING.equalsIgnoreCase(name)) {
				fullRendering = "true".equalsIgnoreCase(value);
		} else {
			// throw new IllegalArgumentException("Unknown parameter name: " + name);
			logger.warn( "Unknown parameter '" + name + "="+ value + "'");
		}
	}

	abstract public boolean gatherData(ActionRequest request, Attribute attr) throws DataException;

	public boolean processAction(ActionRequest request,
			ActionResponse response, Attribute attr) throws DataException {
		return false;
	}

	public void initEditor(PortletRequest request, Attribute attr) throws DataException {
	}

	public boolean doesFullRendering(Attribute attr) {
		return fullRendering;
	}

	public boolean isValueCollapsable() {
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !getClass().equals(obj.getClass()))
			return false;
		JspAttributeEditor other = (JspAttributeEditor) obj;
		return
			(jspPath == null ? other.jspPath == null : jspPath.equals(other.jspPath)) &&
			(initJspPath == null ? other.initJspPath == null : initJspPath.equals(other.initJspPath));
	}

	@Override
	public int hashCode() {
		return
			(jspPath == null ? 0 : jspPath.hashCode()) ^
			(initJspPath == null ? 0 : initJspPath.hashCode());
	}

	protected Map<String, Object> getReferenceData(Attribute attr,
			@SuppressWarnings("unused") PortletRequest request)
			throws PortletException
	{
		final Map<String, Object> result = new HashMap<String, Object>();
		result.put("attrCode", attr.getId().getId());
		return result;
	}

	public void loadAttributeValues(Attribute attr, PortletRequest request) {

	}


	public String getInitJspPath(){return initJspPath;}
}