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
package com.aplana.dbmi.linksportlet;

import java.io.*;
import java.util.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.portlet.*;
import javax.transaction.UserTransaction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.domapi.XPathEvaluatorImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.xpath.XPathEvaluator;
import org.w3c.dom.xpath.XPathResult;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.aplana.dbmi.ConfigService;
import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.ContextProvider;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.jboss.portal.identity.UserModule;
import org.jboss.portal.identity.RoleModule;
import org.jboss.portal.identity.MembershipModule;
import org.jboss.portal.identity.UserProfileModule;
import org.jboss.portal.identity.User;
import org.jboss.portal.identity.Role;


/**
 * A sample portlet based on GenericPortlet
 */
public class LinksPortlet extends GenericPortlet {

    public static final String JSP_FOLDER = "/WEB-INF/jsp/"; // JSP folder name
    public static final String VIEW_JSP = "LinksPortletView"; // JSP file name to be rendered on the view mode
    public static final String H_VIEW_JSP = "LinksPortletHView"; // JSP file name to be rendered on the horisontal view mode
    public static final String SESSION_BEAN = "LinksPortletSessionBean"; // Bean name for the portlet session
//    public static final String FORM_SUBMIT = "LinksPortletFormSubmit"; // Action name for submit form
//    public static final String FORM_TEXT = "LinksPortletFormText"; // Parameter name for the text input
    
    public static final String HORISONTAL_ATTRIBUTE_VALUE = "H";
    
    private String portletDescriptionResourceName;
    
    /**
     * Map from language to caption.
     */
    private HashMap captions; 
    
    /**
     * Map from language to link lists.
     */
    private HashMap linkLists; 
    
    /**
     * Is portlet orientation horisontal?
     */
    private boolean horisontalOrientation = false;
    
    private static UserModule userModule;
    private static RoleModule roleModule;
    private static MembershipModule membershipModule;
    private static SessionFactory identitySessionFactory;
    private static Configuration conf;
    static{initStaticMembers();}
    
    protected static Log logger = LogFactory.getLog(LinksPortlet.class);
    /**
     * @see javax.portlet.Portlet#init()
     */
    
    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);

        portletDescriptionResourceName = portletConfig.getInitParameter("portletDescription");
        
        captions = new HashMap();
        linkLists = new HashMap();

        try {
        	ConfigService cs = Portal.getFactory().getConfigService();
            InputSource is = new InputSource(cs.loadConfigFile(portletDescriptionResourceName));
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document portletDescriptionDoc = documentBuilder.parse(is);
            XPathEvaluator xpath = new XPathEvaluatorImpl();
            
            String orientation = ((XPathResult) xpath.evaluate("/links-portlet/@orientation", portletDescriptionDoc, null, XPathResult.STRING_TYPE, null)).getStringValue();

//            System.out.println("LinksPortlet.init: orientation=" + orientation);
            
            horisontalOrientation = HORISONTAL_ATTRIBUTE_VALUE.equals(orientation);

//            System.out.println("LinksPortlet.init: horisontalOrientation=" + horisontalOrientation);

            XPathResult captionIter = (XPathResult) xpath.evaluate("/links-portlet/caption", portletDescriptionDoc, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
            Element captionNode;
            
            while ((captionNode = (Element) captionIter.iterateNext()) != null) {
                //System.out.println("LinksPortlet.init: captionNode=" + captionNode);
                String lang = captionNode.getAttribute("lang");
//                System.out.println("LinksPortlet.init: captionNode.lang=" + lang);

                String caption = ((XPathResult) xpath.evaluate("text()", captionNode, null, XPathResult.STRING_TYPE, null)).getStringValue();
//                System.out.println("LinksPortlet.init: caption=" + caption);

                captions.put(lang.length() != 0 ? lang : null, caption);
            }
        
            XPathResult linkListIter = (XPathResult) xpath.evaluate("/links-portlet/link-list", portletDescriptionDoc, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
            Element linkListNode;
            
            while ((linkListNode = (Element) linkListIter.iterateNext()) != null) {
//                System.out.println("LinksPortlet.init: linkListNode=" + linkListNode);
                String lang = linkListNode.getAttribute("lang");
//                System.out.println("LinksPortlet.init: linkListNode.lang=" + lang);
                
                List linkList = new ArrayList();
                
                XPathResult linkIter = (XPathResult) xpath.evaluate("*", linkListNode, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
                Element linkNode;
                
                while ((linkNode = (Element) linkIter.iterateNext()) != null) {
//                    System.out.println("LinksPortlet.init: linkNode=" + linkNode);
                    String tagName = linkNode.getTagName();
//                    System.out.println("LinksPortlet.init: linkNode.tagName=" + tagName);
                    
                    AbstractLinkListItem linkListItem;
                    
                    if ("link".equals(tagName)) {
                        Link link = new Link();

                        link.setHref(linkNode.getAttribute("href"));
                        
                        setCommonAttributes(xpath, linkNode, link);
                        
                        linkListItem = link;
                    } else if ("pageLink".equals(tagName)) {
                        PageLink pageLink = new PageLink();

                        pageLink.setPageName(linkNode.getAttribute("pageName"));
                        pageLink.setPortletName(linkNode.getAttribute("portletName"));
                        pageLink.setParams(linkNode.getAttribute("params"));
                        
                        setCommonAttributes(xpath, linkNode, pageLink);
                        
                        linkListItem = pageLink;
                    } else if ("mailLink".equals(tagName)) {
                        MailLink mailLink = new MailLink();

                        mailLink.setHrefTemplate(linkNode.getAttribute("hrefTemplate"));
                        mailLink.setActionName(linkNode.getAttribute("action"));
                        mailLink.setSubject(linkNode.getAttribute("subject"));
                        
                        setCommonAttributes(xpath, linkNode, mailLink);
                        
                        linkListItem = mailLink;
                    } else if ("separator".equals(tagName)) {
                        linkListItem = new Separator();
                    } else {
                        throw new PortletException("Unknown tag: " + tagName);
                    }
                    
                    linkList.add(linkListItem);
                }
                
                linkLists.put(lang.length() != 0 ? lang : null, linkList);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace(System.out);
            throw new PortletException(e);
        } catch (FactoryConfigurationError e) {
            e.printStackTrace(System.out);
            throw new PortletException(e);
        } catch (SAXException e) {
            e.printStackTrace(System.out);
            throw new PortletException(e);
        } catch (IOException e) {
            e.printStackTrace(System.out);
            throw new PortletException(e);
        }
    }

    private void setCommonAttributes(XPathEvaluator xpath, Element linkNode, AbstractLink link) {
        link.setVisualClass(getAttribute(linkNode, "class"));
//        System.out.println("LinksPortlet.init: link.VisualClass=" + link.getVisualClass());
        link.setCheckAction(getAction(linkNode));
        
        String linkText = ((XPathResult) xpath.evaluate("text()", linkNode, null, XPathResult.STRING_TYPE, null)).getStringValue();
//                        System.out.println("LinksPortlet.init: linkText=" + linkText);
        link.setText(linkText);
        
        //�������� ����, ������� �������� ������ � ������������� �� � �� ��������.
        
        HashSet<String> roles = new HashSet<String>();
        
        String rolesAttribute = getAttribute(linkNode, "roles");
        String rolesString[] = linkNode.getAttribute("roles").split(",");
        
        if(rolesAttribute != null){
        	for(int i = 0; i < rolesString.length; i++) rolesString[i] = rolesString[i].trim();
        	UserTransaction tx = null;
	    	try{
	    		tx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
	    		tx.begin();
	    		//roles.addAll(roleModule.findRolesByNames(rolesString));
	    		for (Role r : (Set<Role>) roleModule.findRolesByNames(rolesString)) roles.add(r.getName());
	    		tx.commit();
	    	} catch (Exception e){
	    		try{e.printStackTrace(); tx.rollback();}
	    		catch (Exception e1){logger.error("Transaction rollback failure.");}
	    	}    	
	    }       
        link.setRoles(roles);
    }

    private Action getAction(Element linkNode) {
        String actionName = getAttribute(linkNode, "checkAction");
        if (actionName == null)
        	return null;
        Action action = null;
        try {
            action = (Action) Class.forName(actionName).newInstance();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return action;
    }

    private String getAttribute(Element element, String attributeName) {
        String attributeValue = element.getAttribute(attributeName);
        return attributeValue.length() > 0 ? attributeValue : null;
    }
    
    /**
     * Serve up the <code>view</code> mode.
     * 
     * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest,
     *      javax.portlet.RenderResponse)
     */
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
//        System.out.println("LinksPortlet.doView: request.locale=" + request.getLocale());
        ContextProvider.getContext().setLocale(request.getLocale());
        response.setContentType(request.getResponseContentType());

        LinksPortletSessionBean sessionBean = getSessionBean(request);
        if (sessionBean == null) {
            response.getWriter().println("<b>NO PORTLET SESSION YET</b>");
            return;
        }
        
        String lang = request.getLocale().getLanguage();
//        System.out.println("LinksPortlet.doView: request.locale.lang=" + lang);

        String caption = getCaption(lang);
        List linkList = getLinkList(lang);
        
        permissionCheck(linkList, request.getUserPrincipal().getName());
        
        sessionBean.setCaption(caption);
        sessionBean.setLinkList(linkList);

        PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(getJspFilePath(request, horisontalOrientation ? H_VIEW_JSP : VIEW_JSP));
        rd.include(request, response);
    }

    private List getLinkList(String lang) throws PortletException {
        List linkList = (List) linkLists.get(lang);
        if (linkList == null) {
            linkList = (List) linkLists.get(null);
            if (linkList == null) {
                throw new PortletException("No link list for language " + lang);
            }
        }
        
        ArrayList linkListClone = new ArrayList();
        linkListClone.addAll(linkList);
        return linkListClone;
    }

    private String getCaption(String lang) throws PortletException {
        String caption = (String) captions.get(lang);
        if (caption == null) {
            caption = (String) captions.get(null);
            if (caption == null) {
                throw new PortletException("No Caption for language " + lang);
            }
        }
        return caption;
    }

    /**
     * Process an action request.
     * 
     * @see javax.portlet.Portlet#processAction(javax.portlet.ActionRequest,
     *      javax.portlet.ActionResponse)
     */
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, java.io.IOException {
    }

    /**
     * Get SessionBean.
     * 
     * @param request
     *            PortletRequest
     * @return LinksPortletSessionBean
     */
    private static LinksPortletSessionBean getSessionBean(PortletRequest request) {
        PortletSession session = request.getPortletSession();
        if (session == null)
            return null;
        LinksPortletSessionBean sessionBean = (LinksPortletSessionBean) session.getAttribute(SESSION_BEAN);
        if (sessionBean == null) {
            sessionBean = new LinksPortletSessionBean();
            session.setAttribute(SESSION_BEAN, sessionBean);
        }
        return sessionBean;
    }

    /**
     * Returns JSP file path.
     * 
     * @param request
     *            Render request
     * @param jspFile
     *            JSP file name
     * @return JSP file path
     */
    private static String getJspFilePath(RenderRequest request, String jspFile) {
        String markup = request.getProperty("dbmi.markup");
        if (markup == null)
            markup = getMarkup(request.getResponseContentType());
        return JSP_FOLDER + markup + "/" + jspFile + "." + getJspExtension(markup);
    }

    /**
     * Convert MIME type to markup name.
     * 
     * @param contentType
     *            MIME type
     * @return Markup name
     */
    private static String getMarkup(String contentType) {
        if ("text/vnd.wap.wml".equals(contentType))
            return "wml";
        else
            return "html";
    }

    /**
     * Returns the file extension for the JSP file
     * 
     * @param markupName
     *            Markup name
     * @return JSP extension
     */
    private static String getJspExtension(String markupName) {
        return "jsp";
    }
    
  //������� ������, �� ������� � ������������ ��� ����.
    private void permissionCheck(List linkList, String userName){	
    	
    	UserTransaction tx = null;
    	HashSet<String> userRoles = new HashSet<String>();
    	
        try{
        	tx = (UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
	        tx.begin();
        	User user = userModule.findUserByUserName(userName);
	        //userRoles = membershipModule.getRoles(user);
        	for(Role r : (Set<Role>) membershipModule.getRoles(user)) userRoles.add(r.getName());
	        tx.commit();
        } catch(Exception e){
        	try{e.printStackTrace(); tx.rollback();}
        	catch(Exception e1){logger.error("Transaction rollback failure.");}
        }
        
        if(userRoles == null) return;
        
        for(Iterator i = linkList.iterator(); i.hasNext(); ){
        	Object o = i.next();
        	if (o instanceof AbstractLink){
        		Set<String> roles = ((AbstractLink) o).getRoles();
        		if(!roles.isEmpty() && Collections.disjoint(roles, userRoles))
        			i.remove();
	        	}
	        }
    }
    
    private static void initStaticMembers(){
    	try{
        	conf =  new Configuration();
        	conf.setProperty(
        			"hibernate.dialect", 
        			"org.hibernate.dialect.PostgreSQLDialect"
        	);
        	conf.setProperty(
        			"hibernate.transaction.factory_class",
        			"org.hibernate.transaction.JTATransactionFactory"
        	);
        	conf.setProperty(
        			"hibernate.transaction.manager_lookup_class", 
        			"org.hibernate.transaction.JBossTransactionManagerLookup"
        	);
        	identitySessionFactory = conf.buildSessionFactory();
        	userModule = (UserModule) new InitialContext().lookup("java:portal/UserModule");
	        roleModule = (RoleModule) new InitialContext().lookup("java:portal/RoleModule");
	        membershipModule = (MembershipModule) new InitialContext().lookup("java:portal/MembershipModule");       
        } catch(Exception e){
        	logger.error("Static class objects initialization failure."); 
        	e.printStackTrace();
        }
    }

}
