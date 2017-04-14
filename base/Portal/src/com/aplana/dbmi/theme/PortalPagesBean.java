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
package com.aplana.dbmi.theme;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.GetDelegateListByLogin;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.api.PortalRuntimeContext;
import org.jboss.portal.api.node.PortalNode;
import org.jboss.portal.api.node.PortalNodeURL;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.*;

/**
 * ������ ������������ � JSP ��� ��������� ������ ��������� � ����� �������� ��������
 * @author dsultanbekov
 */
public class PortalPagesBean {
	private final static String THEME_ID_KEY = "theme.id";
	//private final static String THEME_ID = "dbmi_theme";
	// ������� ���� ��� ������� ���������� ������ ��������� � ����� ��������
	private final static String THEME_ID_PREFIX = "dbmi_theme";
	private final static String HIDDEN_PAGE_PROPERTY = "Hidden";
	private final static String SHOW_DOCUMENT_COUNT = "showDocumentCount";
	private final static String DEFAULT_SEARCH = "defaultSearch";

    private static final ObjectId ROLE_BOSS_HELPER = ObjectId.predefined(SystemRole.class, "jbr.boss.helper");
    
    private static final Properties pagesVisibility = new Properties();
    private static final Log logger = LogFactory.getLog("PortalPagesBean");
    private static final String configFile = "dbmi/pagesVisibility.properties";
    
    static {
		try {
			InputStream in = Portal.getFactory().getConfigService().loadConfigFile(configFile);
			pagesVisibility.load(in);
		} catch (IOException e) {
			logger.error("Cannot load config file " + configFile);
		}
	}

	public class PageInfo {
		private PortalNode node;
		private PortalNodeURL link;
		private String title;
		private boolean showDocumentCount = false;
		private String index;

		public String getTitle() {
			return title;
		}
			
		public PortalNodeURL getLink() {
			return link;
		}

		public PortalNode getNode() {
			return node;
		}

		public boolean isShowDocumentCount() {
			return showDocumentCount;
		}

		//���������� ����� ���������� ��������
		//�� ��� ������������ id html ��������
		public String getIndex() {
			return index;
		}
		
		public List<PageInfo> getChildren() {
			List<PageInfo> result = new ArrayList<PageInfo>();
			int index = 0;
			if (node != null) {
				for (Object obj : node.getChildren()) {
					PortalNode n = (PortalNode) obj;
					if (n.getType() == PortalNode.TYPE_PAGE) {
						String overridenVisibility = pagesVisibility.getProperty(n.getName());
						if (overridenVisibility != null ? Boolean.parseBoolean(overridenVisibility) : !"true".equals(n.getProperties().get(HIDDEN_PAGE_PROPERTY))) {					
	                        PageInfo pi = new PageInfo(n, "true".equals(n.getProperties().get(SHOW_DOCUMENT_COUNT)), this.index + "_" + index++);
	                        result.add(pi);
	                    }
					}
				}
			}
			return result;
		}
		
		public PageInfo(PortalNode page, boolean showDocumentCount, String index) {
			node = page;
			this.showDocumentCount = showDocumentCount;
			title = node.getDisplayName(locale);
			this.index = index;
			
			PortalNode linkNode = PortalNodeUtils.getPageWithWindows(page);
			// ���� �� ����� ��������, � �� �� ����� �� � �������� ��� ���������,
			// �� ������ ����� �� ���� ��������
			if (linkNode == null) {
				linkNode = page;
			}

			if (showDocumentCount) {
				//���� ������� � ���������, �� ���������� ���� �������, ����� �����
				//��� � ���������� � ��������� ��������� (ajax)
				//������ � ������� JSON
				Map<String, String> jsonMap = new HashMap<String, String>();
				jsonMap.put("index", this.index);
				jsonMap.put("xml", (String)node.getProperties().get(DEFAULT_SEARCH));
				jsonMap.put("limit", (String)node.getProperties().get("titleCounterLimit"));
				JSONObject json = new JSONObject(jsonMap);
				PortalPagesBean.this.addCounter(json.toString());
			}
			link = linkNode.createURL(context);
		}
	}
	
	/**
	 * ������� ���� �������
	 */
	private PortalNode root;
	/**
	 * �������� ������ �������
	 */
	private PortalNode portal;

	private List<PortalNode> routeToNode;

	private Locale locale;

	private PortalRuntimeContext context;
	
	private Principal principal;

    private boolean isDelegated;

	private String remoteAddress;

	private List<String> counters;

	public void setRequest(HttpServletRequest request) {
		locale = request.getLocale();
		if (locale == null) {
			locale = Locale.getDefault();
		}
		context = (PortalRuntimeContext)request.getAttribute("org.jboss.portal.api.PORTAL_RUNTIME_CONTEXT");
		counters = new ArrayList<String>();
		
		routeToNode = new ArrayList<PortalNode>();
		this.root = (PortalNode)request.getAttribute("org.jboss.portal.api.PORTAL_NODE");
		portal = root;
		
		while (portal.getType() != PortalNode.TYPE_PORTAL) {
			routeToNode.add(portal);
			portal = portal.getParent();
		}
		Collections.reverse(routeToNode);
		if (request.getUserPrincipal() != null) {
		    String userName = request.getParameter(DataServiceBean.USER_NAME);
            if (userName != null) {
            	AsyncDataServiceBean service = new AsyncDataServiceBean();
                service.setUser(new SystemUser());
                service.setAddress("localhost");
                GetDelegateListByLogin action = new GetDelegateListByLogin();
                action.setLogin(request.getUserPrincipal().getName());
                try {
                    List<String> list = service.doAction(action);
                    if (list.contains(userName)) {
                        request.getSession().setAttribute(DataServiceBean.USER_NAME, userName);
                    } else if (request.getUserPrincipal().getName().equals(userName)) {
                        request.getSession().removeAttribute(DataServiceBean.USER_NAME);
                    }
                } catch (Exception e) {
                	logger.error("Can't exec GetDelegateListByLogin", e);
                }
            }
		}
		if (request.getParameter("logged") != null) {
            request.getSession().removeAttribute(DataServiceBean.USER_NAME);
        }
		String userName = (String) request.getSession().getAttribute(DataServiceBean.USER_NAME);
        if (userName != null) {
            principal = new UserPrincipal(userName);
            isDelegated = true;
        } else {
            principal = request.getUserPrincipal();
        }
		
		remoteAddress = request.getRemoteAddr();
		request.setAttribute("undefinedCount", "(�� ����������)");
	}

	/**
	 * �������� ��� ��������� ��������. ����� ���������� �� � jsp
	 * @return  ��� �������� � ���� JSON �������� ����� �������
	 *
	 */
	public String getCounters() {
		if (counters == null || counters.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String ct : counters) {
			sb.append(ct).append(",");
		}
		//������� ��������� ����������� �������
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * ��������� ������� ��� ����������� �������
	 * @param id ������������� �������� �� �������� html
	 */
	public void addCounter(String id) {
		if (counters != null) {
			counters.add(id);
		}
	}

	public PortalNode getRoot() {
		return root;
	}

	public PortalNode getPortal() {
		return portal;
	}

	public PortalNode getSelectedFirstLevelPage() {		
		if (routeToNode == null || routeToNode.isEmpty()) {
			return null;
		} else {
			return routeToNode.get(0);
		}
	}

	public PortalNode getSelectedSecondLevelPage() {
		if (routeToNode == null || routeToNode.size() < 2) {
			return null;
		} else {
			return routeToNode.get(1);
		}		
	}
	
	@SuppressWarnings("unchecked")
	public List<PageInfo> getFirstLevelPages() {
		final List<PageInfo> pages = getChildPages(portal);

        if(null == pages || pages.size() == 0) {
            return pages;
        }

        boolean alreadyHaveCabinetAssistant = false;
        for(PageInfo page : pages) {
            if(page.getNode().getName().equals("cabinet-assistent")) {
                alreadyHaveCabinetAssistant = true;
                break;
            }
        }

        if(isDelegated && !alreadyHaveCabinetAssistant) {
            try {
            	AsyncDataServiceBean service = createService();
                service.getById(new ObjectId(Person.class, 0));
                Person person = service.getById(service.getPerson().getId());

                for(Role role : (Collection<Role>)  person.getRoles() ) {
                    if (ROLE_BOSS_HELPER.equals(role.getSystemRole().getId())) {
                        PortalNode node = new CabinetAssistantPortalNode(pages.get(0).getNode());
                        pages.add(pages.size() - 2, new PageInfo(node, false, "0"));
                        break;
                    }
                }
            } catch (DataException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }

        return pages;
	}

	public List<PageInfo> getSecondLevelPages() {
		PortalNode parent = getSelectedFirstLevelPage();
		return getChildPages(parent);
	}

	@SuppressWarnings("unused")
	public List<PageInfo> getThirdLevelPages() {
		PortalNode parent = getSelectedSecondLevelPage();
		return getChildPages(parent);
	}
	
	/**
	 * ���������� ������ �������� ���� PageInfo, �������������� ��������, ��� �������
	 * ������ ���� �������� ������������ �� ����������� �������, � ������� ��������
	 * Hidden ���������� � true.
	 */
	private List<PageInfo> getChildPages(PortalNode parent) {
		List<PageInfo> result = new ArrayList<PageInfo>();
		int index = 0;
		if (parent != null) {
			for (Object obj : parent.getChildren()) {
				PortalNode n = (PortalNode) obj;
				if (n.getType() == PortalNode.TYPE_PAGE) {
					String overridenVisibility = pagesVisibility.getProperty(n.getName());
					if (overridenVisibility != null ? Boolean.parseBoolean(overridenVisibility) : !"true".equals(n.getProperties().get(HIDDEN_PAGE_PROPERTY))) {
						final PageInfo pi = new PageInfo(n, "true".equals(n.getProperties().get(SHOW_DOCUMENT_COUNT)), String.valueOf(index++));
						result.add(pi);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * ���������, ������������� �� ������ ��������, ����������� � ������� ����
	 * ��� ����� ����������� ����, ������������ ������� ���������.
	 */
	public boolean isDbmiPortal() {
		String themeId = (String)root.getProperties().get(THEME_ID_KEY);
		return themeId.startsWith(THEME_ID_PREFIX);
	}
	
	/**
	 * ��������� �������� �� ������� �������� �������.
	 */
	public boolean isSelectedPageHidden() {
		String overridenVisibility = pagesVisibility.getProperty(root.getName());
	    return overridenVisibility != null ? !Boolean.parseBoolean(overridenVisibility) : "true".equals(root.getProperties().get(HIDDEN_PAGE_PROPERTY));
	}

    private AsyncDataServiceBean createService() {
    	AsyncDataServiceBean bean = new AsyncDataServiceBean();
        bean.setAddress(remoteAddress);
        bean.setUser(principal);
        return bean;
    }

    private class CabinetAssistantPortalNode implements PortalNode{
        private PortalNode firstLevelNode;

        CabinetAssistantPortalNode(PortalNode firstLevelNode) {
            this.firstLevelNode = firstLevelNode;
        }

        public int getType() {
            return firstLevelNode.getType();
        }

        public org.jboss.portal.api.node.PortalNode getRoot() {
            return firstLevelNode.getRoot();
        }

        public org.jboss.portal.api.node.PortalNode getParent() {
            return firstLevelNode.getParent();
        }

        public String getName() {
            return "cabinet-assistent";
        }

        public String getDisplayName(Locale locale) {
            return "������� ���������";
        }

        public PortalNode getChild(String s) {
            return null;
        }

        public Collection<?> getChildren() {
            return new ArrayList<Object>();
        }

        public PortalNode resolve(String s) {
            return null;
        }

        public Map<String,String> getProperties() {
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(HIDDEN_PAGE_PROPERTY, Boolean.FALSE.toString());
            return properties;
        }

        public PortalNodeURL createURL(final PortalRuntimeContext portalRuntimeContext) {
            return new PortalNodeURL() {
                public void setParameter(String s, String s1) throws IllegalArgumentException {}
                public void setParameter(String s, String[] strings) throws IllegalArgumentException {}
                public void setAuthenticated(Boolean aBoolean) {}
                public void setSecure(Boolean aBoolean) {}
                public void setRelative(boolean b) {}
                public String toString() {
                    return "/portal/auth/portal/dbmi/cabinet-assistent/cabinet-assistent-boss/doc-income";
                }
            };
        }
    }
}