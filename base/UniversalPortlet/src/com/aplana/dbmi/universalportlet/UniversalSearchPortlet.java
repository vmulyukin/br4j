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
package com.aplana.dbmi.universalportlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.model.ContextProvider;

/**
 * Search portlet. Acts as a filter to UniversalPortlet
 * In order to provide an ability to filter data in UniversalPortlet you have to follow these steps:
 * 
 * - create search description file in ${conf.folder}/universalPortlet/search folder
 * 				with list of columns you need you data to be filtered by
 * 
 * - specify the parameter "universalSearchPortletDescription" in *-portal-object.xml, by adding your search description file as a value
 */
public class UniversalSearchPortlet extends GenericPortlet {
	
	public static final String SESSION_BEAN = "UniversalSearchPortletSessionBean";
	
	public static final String JSP_FOLDER = "/_UniversalPortlet/jsp/";
	
	public static final String VIEW_FILE = "UniversalSearchPortlet";
	
	
	private static final String UNIVERSAL_SEARCH_PORTLET_CONFIG_FILE_PREFIX = "dbmi/universalPortlet/search/";
	private static final String UNIVERSAL_SEARCH_PORTLET_DESCRIPTION = "universalSearchPortletDescription";
	
	public static final String UNIVERSAL_SEARCH_BEAN = "UNIVERSAL_SEARCH_BEAN";
	public static final String UNIVERSAL_SEARCH_FILTER = "UNIVERSAL_SEARCH_FILTER";
	public static final String CLEAR_UNIVERSAL_SEARCH_FILTER = "CLEAR_UNIVERSAL_SEARCH_FILTER";
	public static final String CLEAR_UNIVERSAL_SEARCH_FILTER_ACTION = "CLEAR_UNIVERSAL_SEARCH_FILTER_ACTION";
	
	public static final String ACTION = "ACTION";
	public static final String SEARCH_ACTION = "SEARCH";
	public static final String PARAM_SEARCH_WORD = "SEARCH_WORD";
	public static final String PARAM_IS_STRICT = "IS_STRICT";
	
	
	protected static Log logger = LogFactory.getLog(UniversalPortlet.class);
	
	public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
    }
	
	public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

      Locale requestLocale = request.getLocale();

      ContextProvider.getContext().setLocale(requestLocale);

      response.setContentType(request.getResponseContentType());
      
      UniversalSearchPortletSessionBean sessionBean = getSessionBean(request, response);
      
      clearSearchIfNeed(request, sessionBean);
      
      PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(getJspFilePath(request, VIEW_FILE));
      rd.include(request, response);
	}
	
	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, java.io.IOException {
        Locale requestLocale = request.getLocale();
        ContextProvider.getContext().setLocale(requestLocale);
        UniversalSearchPortletSessionBean sessionBean = getSessionBean(request, response);
        if(sessionBean != null) {
        	final String action = request.getParameter(ACTION);
        	if(SEARCH_ACTION.equals(action)) {
        		handleSearch(request, response, sessionBean);
        	}
        }
    }
	
	private void clearSearchIfNeed(PortletRequest request, UniversalSearchPortletSessionBean sessionBean) {
		if(request.getPortletSession().getAttribute(UniversalSearchPortlet.CLEAR_UNIVERSAL_SEARCH_FILTER, PortletSession.APPLICATION_SCOPE) != null) {
			sessionBean.setFilter(null);
			request.getPortletSession().removeAttribute(UniversalSearchPortlet.CLEAR_UNIVERSAL_SEARCH_FILTER, PortletSession.APPLICATION_SCOPE);
		}
	}
	
	/**
	 * Handles search action
	 * @param request
	 * @param response
	 * @param sessionBean
	 */
	private void handleSearch(PortletRequest request, PortletResponse response, UniversalSearchPortletSessionBean sessionBean) {
		
		logger.info("Performing search action");
		
		UniversalSearchFilter filter = new UniversalSearchFilter();
		
		// the search text that has been inserted in a search text field
		final String searchWord = request.getParameter(PARAM_SEARCH_WORD);
		if(searchWord != null && !searchWord.equals("")) {
			// the flag that represents whether strict search is needed
			final String strictParam = request.getParameter(PARAM_IS_STRICT);
			filter.setStrict(("on").equals(strictParam));
			filter.setColumnValue(searchWord);
			try {
				initSearchFilterColumns(request, response, filter);
			} catch (Exception e) {
				logger.error("An error has occured while trying to read search description file!", e);
			}
			sessionBean.setFilter(filter);
			request.getPortletSession().setAttribute(UNIVERSAL_SEARCH_FILTER, filter, PortletSession.APPLICATION_SCOPE);
		}
	}
	
	/**
	 * Initializes search filter column names from search description file
	 * @param request
	 * @param response
	 * @param filter filter object
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private void initSearchFilterColumns(PortletRequest request, PortletResponse response, UniversalSearchFilter filter) throws IOException, ParserConfigurationException, SAXException {
		
		String portletSearcFileName = Portal.getFactory().getPortletService().getPageProperty(UNIVERSAL_SEARCH_PORTLET_DESCRIPTION, request, response);
		
		if(portletSearcFileName == null || portletSearcFileName.equals(""))
			throw new IllegalStateException("No search description file is present in portlet description! You need to specify a parameter that will represent a search description file name for portlet: "+
														this.getClass().getSimpleName()+" in order to have search filter abilities");
		
		logger.info("A search file name parameter has been found: "+portletSearcFileName);
		InputStream inputStream = null;
		try {
			inputStream = Portal.getFactory().getConfigService().loadConfigFile(UNIVERSAL_SEARCH_PORTLET_CONFIG_FILE_PREFIX + portletSearcFileName);
			InputSource is = new InputSource(inputStream);
	        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	        Document searchDescriptionDoc = documentBuilder.parse(is);
	        XPathEvaluator xpath = new XPathEvaluatorImpl();
	        
	        logger.info("Parsing search description file");
	        
	        XPathResult columnsXpath = (XPathResult) xpath.evaluate("/universal-search/column", searchDescriptionDoc, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
	        Element columnNode;
	        
	        final List<String> columnNames = new ArrayList<String>();
	        
	        while ((columnNode = (Element) columnsXpath.iterateNext()) != null) {
	        	columnNames.add(columnNode.getAttribute("name"));
	        }
	        filter.setColumnNames(columnNames);
		} finally {
			if(inputStream != null)
				inputStream.close();
		}
	}
	
	private UniversalSearchPortletSessionBean getSessionBean(PortletRequest request, PortletResponse response) throws PortletException {
        PortletSession session = request.getPortletSession();
        if (session == null)
            return null;
        UniversalSearchPortletSessionBean sessionBean = (UniversalSearchPortletSessionBean) session.getAttribute(SESSION_BEAN);
        if (sessionBean == null) {
            sessionBean = new UniversalSearchPortletSessionBean();

            session.setAttribute(SESSION_BEAN, sessionBean);
            if (response instanceof RenderResponse) {
                RenderResponse renderResponse = (RenderResponse) response;
                String namespace = renderResponse.getNamespace();
                session.setAttribute(SESSION_BEAN + '.' + namespace, sessionBean, PortletSession.APPLICATION_SCOPE);
            }
        }
        return sessionBean;
    }
	
	/**
     * Returns JSP file path.
     *
     * @param request
     *                Render request
     * @param jspFile
     *                JSP file name
     * @return JSP file path
     */
    private static String getJspFilePath(RenderRequest request, String jspFile) {
        String markup = request.getProperty("wps.markup");
        if (markup == null)
            markup = getMarkup(request.getResponseContentType());
        return JSP_FOLDER + markup + "/" + jspFile + "." + getJspExtension(markup);
    }
    
    /**
     * Converts MIME type to markup name.
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
}
