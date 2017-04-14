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
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.domapi.XPathEvaluatorImpl;
import org.displaytag.util.SortingState;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.xpath.XPathEvaluator;
import org.w3c.dom.xpath.XPathResult;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.action.SQLQueryAction;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.util.DateUtils;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;

/**
 * A mega-universal portlet that can execute any SQL request and display its result via DisplayTag.
 * Reads configuration from file in the dbmi/universalPortlet subdirectory of the server config directory.
 * Config file name can be specified in "portletDescription" portlet init parameter or
 * in "universalPortletDescription" page property.
 */
public class UniversalPortlet extends GenericPortlet {
	
	protected static Log logger = LogFactory.getLog(UniversalPortlet.class);

    private static final String UNIVERSAL_PORTLET_CONFIG_FILE_PREFIX = "dbmi/universalPortlet/";

    private static final String UNIVERSAL_PORTLET_DESCRIPTION_INIT_PARAMETER_NAME = "portletDescription";

    private static final String UNIVERSAL_PORTLET_DESCRIPTION_PAGE_PROPERTY_NAME = "universalPortletDescription";
    
    private static final String CAN_BE_EXPORTED_TO_EXCEL_PROPERTY_NAME = "canBeExportedToExcel";
    
    private static final String CAN_BE_PRINTED_PROPERTY_NAME = "canBePrinted";
    
    private static final String SUBMIT_ON_LOAD_PARAM = "universalPortletSubmitOnLoad";
    
    public static final String DOWNLOAD_IMPORT_TEMPLATE = "downloadImportTemplate";
    
    public static final String SHOW_REFRESH_BUTTON = "showBtnRefresh";

	public static final String EDIT_ACCESS_ROLES = "editAccessRoles";

	public static final String JSP_FOLDER = "/_UniversalPortlet/jsp/"; // JSP folder name

    public static final String VIEW_JSP = "UniversalPortletView"; // JSP file name to be rendered on the view mode

    public static final String SESSION_BEAN = "UniversalPortletSessionBean"; // Bean name for the portlet session

    public static final String FORM_SUBMIT = "UniversalPortletFormSubmit"; // Action name for submit form

    public static final String ACTION_FIELD                 = "MI_ACTION_FIELD";
    public static final String PRINT_ACTION                 = "MI_PRINT_ACTION";
    
    public static final String REFRESH_ACTION				= "MI_REFRESH_ACTION";
    
    public static final String BACK_TO_REQUEST_ACTION		= "BACK_TO_REQUEST_ACTION";

    public static final String TABLE_ID = "dataItem";
    
	public static final String CREATE_ACCESS_ROLES = "createAccessRoles";
	public static final String PREF_SHOW_BTN_CREATE = "showBtnCreate"; 	// ������� ���������� ������� ������ ������� ��������
	public static final String CUSTOM_IMPORT_TITLE = "customImportTitle"; // ��������� ��������� �� ������� ���������� ��������
	public static final String CAN_IMPORT = "canImport";
	
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat dateFormatInput = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String portletDescriptionResourceName;
	private String jspView;
    private String jspFolder;

    /**
     * @see javax.portlet.Portlet#init()
     */
    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);

        portletDescriptionResourceName = portletConfig.getInitParameter(UNIVERSAL_PORTLET_DESCRIPTION_INIT_PARAMETER_NAME);
        setJspView(VIEW_JSP);
        setJspFolder(JSP_FOLDER);
//        System.out.println("UniversalPortlet.init portletDescriptionResourceName=" + portletDescriptionResourceName);
    }

    /**
     * @param sessionBean
     * @param portletRequest
     * @param portletResponse
     * @throws PortletException
     */
    private void initDescription(UniversalPortletSessionBean sessionBean, PortletRequest portletRequest, PortletResponse portletResponse) throws PortletException {
//        System.out.println("UniversalPortlet.initDescription start");
        HashMap<String, QueryDescription> queryDescriptions = new HashMap<String, QueryDescription>();
        sessionBean.setQueryDescriptions(queryDescriptions);

        HashMap<String, TableDescription> tableDescriptions = new HashMap<String, TableDescription>();
        sessionBean.setTableDescriptions(tableDescriptions);

        try {
            String portletDescriptionName = portletDescriptionResourceName;
            if (portletDescriptionName == null) {
//                System.out.println("UniversalPortlet.initDescription portletDescriptionResourceName is null, trying get page property.");
                portletDescriptionName = Portal.getFactory().getPortletService().getPageProperty(UNIVERSAL_PORTLET_DESCRIPTION_PAGE_PROPERTY_NAME, portletRequest, portletResponse);
//                System.out.println("UniversalPortlet.initDescription portletDescriptionName=" + portletDescriptionName);
                if (portletDescriptionName == null) {
                    throw new PortletException("No Universal Portlet description found");
                }
            }

            InputStream inputStream = Portal.getFactory().getConfigService().loadConfigFile(UNIVERSAL_PORTLET_CONFIG_FILE_PREFIX + portletDescriptionName);
            InputSource is = new InputSource(inputStream);
//            InputSource is = new InputSource(getClass().getResourceAsStream('/' + portletDescriptionResourceName));
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document portletDescriptionDoc = documentBuilder.parse(is);
            XPathEvaluator xpath = new XPathEvaluatorImpl();

            XPathResult externalParametersXPathResult = (XPathResult) xpath.evaluate("/universal-portlet/@external-parameters", portletDescriptionDoc, null, XPathResult.STRING_TYPE, null);
            boolean externalParameters = Boolean.parseBoolean(externalParametersXPathResult.getStringValue());
            sessionBean.setExternalParameters(externalParameters);

            XPathResult queryIter = (XPathResult) xpath.evaluate("/universal-portlet/query", portletDescriptionDoc, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
            Element queryNode;

            while ((queryNode = (Element) queryIter.iterateNext()) != null) {
//                System.out.println("UniversalPortlet.init: queryNode=" + queryNode);
                QueryDescription queryDescription = new QueryDescription();
                String lang = queryNode.getAttribute("lang");
//                System.out.println("UniversalPortlet.init: queryNode.lang=" + lang);

                String sql = ((XPathResult) xpath.evaluate("sql/text()", queryNode, null, XPathResult.STRING_TYPE, null)).getStringValue();
//                System.out.println("UniversalPortlet.init: sql=" + sql);
                queryDescription.setSql(sql);

                List<ParameterDescription> parametersMetaData = new ArrayList<ParameterDescription>();
                XPathResult paramsIter = (XPathResult) xpath.evaluate("parameters/param", queryNode, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
                Element paramNode;
                while ((paramNode = (Element) paramsIter.iterateNext()) != null) {
                    ParameterDescription parameterDescription = new ParameterDescription();
                    parameterDescription.setName(paramNode.getAttribute("name"));
                    parameterDescription.setDisplayName(paramNode.getAttribute("dispaly-name"));
                    String typeStr = paramNode.getAttribute("type");
                    if ("string".equals(typeStr)) {
                        parameterDescription.setType(ParameterDescription.STRING_TYPE);
                    } else if ("calendar".equals(typeStr)) {
                        parameterDescription.setType(ParameterDescription.CALENDAR_TYPE);
                    }
                    parametersMetaData.add(parameterDescription);
                }

                queryDescription.setParametersMetaData(parametersMetaData);

                queryDescriptions.put(lang.length() != 0 ? lang : null, queryDescription);
            }

            XPathResult tableIter = (XPathResult) xpath.evaluate("/universal-portlet/table", portletDescriptionDoc, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
            Element tableNode;


            while ((tableNode = (Element) tableIter.iterateNext()) != null) {
//                System.out.println("UniversalPortlet.init: tableNode=" + tableNode);
                TableDescription tableDescription = new TableDescription();
                String lang = tableNode.getAttribute("lang");
//                System.out.println("UniversalPortlet.init: tableNode.lang=" + lang);


                String title = ((XPathResult) xpath.evaluate("@title", tableNode, null, XPathResult.STRING_TYPE, null)).getStringValue();
                tableDescription.setTitle(title);

                String pageSizeStr = ((XPathResult) xpath.evaluate("@pagesize", tableNode, null, XPathResult.STRING_TYPE, null))
                        .getStringValue();
                int pageSize = Integer.parseInt(pageSizeStr);
                tableDescription.setPageSize(pageSize);

                List<ColumnDescription> columnsMetaData = new ArrayList<ColumnDescription>();
                XPathResult columnsIter = (XPathResult) xpath.evaluate("column-description", tableNode, null,
                        XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
                Element columnNode;
                while ((columnNode = (Element) columnsIter.iterateNext()) != null) {
                    ColumnDescription columnDescription = new ColumnDescription();
                    columnDescription.setName(columnNode.getAttribute("name"));
                    columnDescription.setDisplayName(columnNode.getAttribute("display-name"));
                    columnDescription.setSortable(Boolean.valueOf(columnNode.getAttribute("is-sortable")).booleanValue());
                    columnDescription.setHidden(Boolean.valueOf(columnNode.getAttribute("is-hidden")).booleanValue());
                    final String colWidth = columnNode.getAttribute("width");
                    if(colWidth != null && !colWidth.equals(""))
                    	columnDescription.setWidth(Integer.parseInt(colWidth));
                    String linkAttribule = columnNode.getAttribute("link");
//                    System.out.println("UniversalPortlet.init: linkAttribule='" + linkAttribule + '\'');
                    if (linkAttribule != null && linkAttribule.length() > 0) {
                        MessageFormat linkFormat = new MessageFormat(linkAttribule);
                        columnDescription.setLink(linkFormat);
                        String linkColumnAttribute = columnNode.getAttribute("linkColumn");
//                        System.out.println("UniversalPortlet.init: linkColumnAttribute='" + linkColumnAttribute + '\'');
                        if (linkColumnAttribute != null && linkColumnAttribute.length() > 0) {
                            columnDescription.setLinkColumn(linkColumnAttribute);
                        }
                    }

                    columnsMetaData.add(columnDescription);
                }

                // link columns

                for (Iterator<ColumnDescription> cmdIterator = columnsMetaData.iterator(); cmdIterator.hasNext();) {
                    ColumnDescription columnDescription = cmdIterator.next();
                    if (columnDescription.getLink() != null && columnDescription.getLinkColumn() != null) {
                        String linkColumn = columnDescription.getLinkColumn();
                        int linkColumnIndex = 0;
                        for (Iterator<ColumnDescription> icmdIterator = columnsMetaData.iterator(); icmdIterator.hasNext(); linkColumnIndex++) {
                            ColumnDescription iColumnDescription = icmdIterator.next();
                            if (linkColumn.equals(iColumnDescription.getName())) {
                                columnDescription.setLinkColumnIndex(linkColumnIndex);
                                break;
                            }
                        }
                    }
                }

                tableDescription.setColumnsMetaData(columnsMetaData);

                tableDescriptions.put(lang.length() != 0 ? lang : null, tableDescription);
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
        } catch (NumberFormatException e) {
            e.printStackTrace(System.out);
            throw new PortletException(e);
        }
    }

    /**
     * Serve up the <code>view</code> mode.
     *
     * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest,
     *      javax.portlet.RenderResponse)
     */
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
//        System.out.println("UniversalPortlet.doView: request.locale=" + request.getLocale());
        Locale requestLocale = request.getLocale();

        ContextProvider.getContext().setLocale(requestLocale);

        // Set the MIME type for the render response
        response.setContentType(request.getResponseContentType());

        // Check if portlet session exists
        UniversalPortletSessionBean sessionBean = getSessionBean(request, response);
        if (sessionBean == null) {
            response.getWriter().println("<b>NO PORTLET SESSION YET</b>");
            return;
        }
        
        if(sessionBean.isSubmitOnLoad())
        	processSubmitForm(request, response, requestLocale, sessionBean);
        
        String lang = request.getLocale().getLanguage();
//        System.out.println("UniversalPortlet.doView: request.locale.lang=" + lang);

        QueryDescription queryDescription = getQueryDescription(lang, sessionBean);

        TableDescription tableDescription = getTableDescription(lang, sessionBean);

        List<ParameterDescription> parametersMetaData = queryDescription.getParametersMetaData();
        sessionBean.setParametersMetaData(parametersMetaData);
        sessionBean.setColumnsMetaData(tableDescription.getColumnsMetaData());

        MapSqlParameterSource params = null;
        if (!sessionBean.isExternalParameters()) {
            params = sessionBean.getParametersValues();
        } else {
            String[] paramStr = extractParametersFromURL(request, parametersMetaData);
            if (paramStr != null) {
                params = parseParameters(paramStr, parametersMetaData);
            }
        }

        if (params != null) {
            final AsyncDataServiceBean ds = PortletUtil.createService(request);
            final String sql = queryDescription.getSql();
            final List<?> result = loadData(sql, params, ds, sessionBean);
            final List<List<String>> dataList = convertResult(result, tableDescription);

            sessionBean.setData(dataList);
        } else {
            sessionBean.setData(new ArrayList<List<String>>(0));
        }

        sessionBean.setTitle(tableDescription.getTitle());
        sessionBean.setPageSize(tableDescription.getPageSize());

//        System.out.println("UniversalPortlet.doView: namespace=" + response.getNamespace());

        SortingState.configureSortingState(request, TABLE_ID);
        
        if(sessionBean.getFilter() != null)
        	request.getPortletSession().removeAttribute(UniversalSearchPortlet.UNIVERSAL_SEARCH_FILTER, PortletSession.APPLICATION_SCOPE);

        // Invoke the JSP to render
        PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(getJspFilePath(request, getJspView()));
        rd.include(request, response);
    }

    private QueryDescription getQueryDescription(String lang, UniversalPortletSessionBean sessionBean) throws PortletException {
        HashMap<String, QueryDescription> queryDescriptions = sessionBean.getQueryDescriptions();
        QueryDescription queryDescription = (QueryDescription) queryDescriptions.get(lang);
        if (queryDescription == null) {
            queryDescription = (QueryDescription) queryDescriptions.get(null);
            if (queryDescription == null) {
                throw new PortletException("No QueryDescription for language " + lang);
            }
        }
        return queryDescription;
    }

    private TableDescription getTableDescription(String lang, UniversalPortletSessionBean sessionBean) throws PortletException {
        HashMap<String, TableDescription> tableDescriptions = sessionBean.getTableDescriptions();
        TableDescription tableDescription = (TableDescription) tableDescriptions.get(lang);
        if (tableDescription == null) {
            tableDescription = (TableDescription) tableDescriptions.get(null);
            if (tableDescription == null) {
                throw new PortletException("No TableDescription for language " + lang);
            }
        }
        return tableDescription;
    }

    /**
     * Process an action request.
     *
     * @see javax.portlet.Portlet#processAction(javax.portlet.ActionRequest,
     *      javax.portlet.ActionResponse)
     */
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, java.io.IOException {
        Locale requestLocale = request.getLocale();
        ContextProvider.getContext().setLocale(requestLocale);
        UniversalPortletSessionBean sessionBean = getSessionBean(request, response);
        if(sessionBean != null) {
        	final String formAction = request.getParameter(ACTION_FIELD);
        	if (PRINT_ACTION.equals(formAction)) {
                sessionBean.setPrintMode(true);
        	}
        	final String formSubmit = request.getParameter(FORM_SUBMIT);
            if (formSubmit != null) {
            	processSubmitForm(request, response, requestLocale, sessionBean);
            }
            if(BACK_TO_REQUEST_ACTION.equals(formAction)) {
				// reload
            	clearSearch(request, sessionBean);
            	SortingState.saveSortingStateToSession(null, TABLE_ID, request.getPortletSession());
				processSubmitForm(request, response, requestLocale, sessionBean);
			} else if(REFRESH_ACTION.equals(formAction)) {
				processSubmitForm(request, response, requestLocale, sessionBean);
			}
        }
    }
    
    private void clearSearch(PortletRequest request, UniversalPortletSessionBean sessionBean) {
    	sessionBean.setFilter(null);
    	request.getPortletSession().setAttribute(UniversalSearchPortlet.CLEAR_UNIVERSAL_SEARCH_FILTER, UniversalSearchPortlet.CLEAR_UNIVERSAL_SEARCH_FILTER_ACTION, PortletSession.APPLICATION_SCOPE);
    }
    
    private void processSubmitForm(PortletRequest request, PortletResponse response, Locale locale, UniversalPortletSessionBean sessionBean) throws PortletException, java.io.IOException {
    	QueryDescription queryDescription = getQueryDescription(locale.getLanguage(), sessionBean);
        List<ParameterDescription> parametersMetaData = queryDescription.getParametersMetaData();

        String[] initialParams = extractParametersFromRequest(request, parametersMetaData);
        MapSqlParameterSource params = parseParameters(initialParams, parametersMetaData);

        sessionBean.setInitialParameters(initialParams);
        sessionBean.setParametersValues(params);
    }

    private String[] extractParametersFromRequest(PortletRequest request, List<ParameterDescription> parametersMetaData) throws PortletException {
        String paramValueStr;
        String[] params = new String[parametersMetaData.size()];
        int i = 0;
        for (Iterator<ParameterDescription> paramIter = parametersMetaData.iterator(); paramIter.hasNext(); i++) {
            ParameterDescription parameterDescription = paramIter.next();
            String paramName = parameterDescription.getName();
            paramValueStr = request.getParameter(paramName);
            params[i] = paramValueStr;
        }
        return params;
    }

    private MapSqlParameterSource parseParameters(String[] paramsStr, List<ParameterDescription> parametersMetaData) throws PortletException {
        String paramValueStr;
        final MapSqlParameterSource params = new MapSqlParameterSource();
        int i = 0;
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (Iterator<ParameterDescription> paramIter = parametersMetaData.iterator(); paramIter.hasNext(); i++) {
            ParameterDescription parameterDescription = paramIter.next();
            String paramName = parameterDescription.getName();

            switch (parameterDescription.getType()) {
            case ParameterDescription.CALENDAR_TYPE://
                paramValueStr = paramsStr[i];
	            try {
		            if(paramValueStr!= null && paramValueStr.length() > 0) {
			            Date paramDate = dateFormat.parse(paramValueStr);
			            params.addValue(paramName, paramDate.getTime() / 1000, SQLQueryAction.PARAM_NUMBER);
		            } else {
			            params.addValue(paramName, null, SQLQueryAction.PARAM_NUMBER);
		            }
	            } catch (ParseException e) {
		            logger.error("Can't parse date parameter: " + paramValueStr);
		            return null;
	            }
                break;
            case ParameterDescription.STRING_TYPE:
                paramValueStr = paramsStr[i];
                params.addValue(paramName, paramValueStr, SQLQueryAction.PARAM_STRING);
                break;
            default:
                throw new PortletException("Unknown parameter type: " + parameterDescription.getType());
            }
        }
        return params;
    }

    private List<List<String>> convertResult(List<?> result, TableDescription tableDescription) 
    {
    	final List<ColumnDescription> columnsMetaData = tableDescription.getColumnsMetaData();
    	final List<List<String>> resultDataList = new ArrayList<List<String>>(result.size());

    	// ������� ����������� �� �������� ����� ������������:
    	// ��� � �������� � ������� �������� 	<=> 	��� � ��
    	final HashMap<String, String> sequensor = new HashMap<String, String>();
    	boolean isFirst = true;

    	for (Iterator<?> resultIter = result.iterator(); resultIter.hasNext();) {

    		final Map<?,?> resultRow = (Map<?,?>) resultIter.next();
    		
    		// �������� ������������� ���� �������
    		if (isFirst)
    		{
    			isFirst = false;
    			for (Iterator<?> iterator = resultRow.keySet().iterator(); iterator.hasNext();) 
    			{
    				String colName = (String) iterator.next();
    				if (colName == null) colName = "";
    				sequensor.put(colName.toUpperCase(), colName);
    			}
			
    		}

    		final List<String> dataListRow = new ArrayList<String>(columnsMetaData.size());

    		for (Iterator<ColumnDescription> columnIter = columnsMetaData.iterator(); columnIter.hasNext();) {
    			final ColumnDescription columnDescription = columnIter.next();
    			// dataListRow.add(resultRow.get(columnDescription.getName()));
    			final String resultColName 
    				= (String) sequensor.get(columnDescription.getName().toUpperCase() ); 
    			final Object obj = resultRow.get(resultColName);
    			if(obj == null) {
    				dataListRow.add(null);
    				continue;
    			}
    			if(obj instanceof String) {
    				dataListRow.add((String) obj);
    			} else if(obj instanceof Number) {
    				dataListRow.add(((Number) obj).toString());
    			} else if(obj instanceof Date) {
    				Date d = DateUtils.setValueWithTZ((Date)obj);
    				dataListRow.add(dateFormatInput.format(d));
    			} else {
    				String errorStr = "Invalid data from ACTION_LOG db table".intern();
    				logger.error(errorStr);
    				dataListRow.add(errorStr);
    			}
    			
    		}

    		resultDataList.add(dataListRow);
    	}
    	return resultDataList;
    }
    
    /**
     * filters data by given filter
     */
    private String filterSQL(String sql, UniversalSearchFilter filter) {
    	if(filter == null
    			|| filter.getColumnNames() == null
    			|| filter.getColumnNames().isEmpty())
    		return sql;
    	StringBuilder sb = new StringBuilder();
    	sb.append("SELECT * FROM ( ")
    		.append(sql)
    		.append(" ) AS result WHERE ");
    	
    	boolean first = true;
		for(String column : filter.getColumnNames()) {
			if(!first) {
				sb.append(" OR ");
			} else first = false;
			
			sb.append(" result.").append(column); // search column name
			
			if(filter.isStrict()) {
				sb.append(" = '")
					.append(filter.getColumnValue())
					.append("'");
			} else {
				sb.append(" LIKE ('%")
					.append(filter.getColumnValue())
					.append("%') ");
			}
		}
    	return sb.toString();
    }

    private List<?> loadData(String sql, MapSqlParameterSource params, AsyncDataServiceBean ds, UniversalPortletSessionBean sessionBean) throws PortletException {
        sql = filterSQL(sql, sessionBean.getFilter());
        SQLQueryAction action = new SQLQueryAction();
        action.setSql(sql);
        action.setParams(params);
        List<?> result = null;
        try {
            result = (List<?>) ds.doAction(action, ExecuteOption.SYNC);
        } catch (DataException e) {
            e.printStackTrace(System.out);
            throw new PortletException(e);
        } catch (ServiceException e) {
            e.printStackTrace(System.out);
            throw new PortletException(e);
        }
        return result;
    }

    /**
     * Get SessionBean.
     *
     * @param request
     *            PortletRequest
     * @param response
     * @return UniversalPortletSessionBean
     * @throws PortletException
     */
    protected UniversalPortletSessionBean getSessionBean(PortletRequest request, PortletResponse response) throws PortletException {
        PortletSession session = request.getPortletSession();
        if (session == null)
            return null;
        UniversalPortletSessionBean sessionBean = (UniversalPortletSessionBean) session.getAttribute(SESSION_BEAN);
        if (sessionBean == null) {
            sessionBean = new UniversalPortletSessionBean();
            
            final PortletService psrvc = Portal.getFactory().getPortletService();

    		final String submitOnLoadStr = psrvc.getPageProperty(
    				SUBMIT_ON_LOAD_PARAM, request, null);
    		if(submitOnLoadStr != null && !submitOnLoadStr.equals("")) {
    			sessionBean.setSubmitOnLoad(Boolean.parseBoolean(submitOnLoadStr));
    		}
    		
    		final String downloadImportTemplate = psrvc.getPageProperty(DOWNLOAD_IMPORT_TEMPLATE, request, response);
    		if (downloadImportTemplate != null && !downloadImportTemplate.equals(""))
    			sessionBean.setDownloadImportTemplate(downloadImportTemplate.trim());
    		
    		final String showRefreshBtn = psrvc.getPageProperty(SHOW_REFRESH_BUTTON, request, response);
    		if (showRefreshBtn != null && !showRefreshBtn.equals(""))
    			sessionBean.setShowRefreshButton(Boolean.parseBoolean(showRefreshBtn.trim()));

            initDescription(sessionBean, request, response);

            session.setAttribute(SESSION_BEAN, sessionBean);
            if (response instanceof RenderResponse) {
                RenderResponse renderResponse = (RenderResponse) response;
                String namespace = renderResponse.getNamespace();
                session.setAttribute(SESSION_BEAN + '.' + namespace, sessionBean, PortletSession.APPLICATION_SCOPE);
            }
            final String exportFlagStr = Portal.getFactory().getPortletService().getPageProperty(CAN_BE_EXPORTED_TO_EXCEL_PROPERTY_NAME, request, response);
            if(exportFlagStr != null && !exportFlagStr.equals(""))
            	sessionBean.setCanBeExportedToExcel(Boolean.parseBoolean(exportFlagStr));
            final String printFlagStr = Portal.getFactory().getPortletService().getPageProperty(CAN_BE_PRINTED_PROPERTY_NAME, request, response);
            if(printFlagStr != null && !printFlagStr.equals(""))
            	sessionBean.setCanBePrinted(Boolean.parseBoolean(printFlagStr));
    		String editAccessRoles = psrvc.getPageProperty(EDIT_ACCESS_ROLES, request, response);
    		if (editAccessRoles != null&&!editAccessRoles.isEmpty()){
    			sessionBean.setEditAccessRoles(editAccessRoles);
    		}
    		String createAccessRoles = psrvc.getPageProperty(CREATE_ACCESS_ROLES, request, response);
    		if (createAccessRoles != null&&!createAccessRoles.isEmpty()){
    			sessionBean.setCreateAccessRoles(createAccessRoles);
    		}
	        String canCreateCards = psrvc.getPageProperty(PREF_SHOW_BTN_CREATE, request, response);
			if (canCreateCards != null) {
				sessionBean.setShowCreate(Boolean.parseBoolean(canCreateCards));
			}
			String customImportTitle = psrvc.getPageProperty(CUSTOM_IMPORT_TITLE, request, response);
			if (customImportTitle!=null&&!customImportTitle.isEmpty())
				sessionBean.setCustomImportTitle(customImportTitle);
			
			String canImport = psrvc.getPageProperty(CAN_IMPORT, request, response);
			if (canImport == null) canImport = "false"; 	//default is false
			sessionBean.setCanImport(Boolean.parseBoolean(canImport));
			
			sessionBean.getServiceBean(request);
        }
        UniversalSearchFilter searchFilter = (UniversalSearchFilter) session.getAttribute(UniversalSearchPortlet.UNIVERSAL_SEARCH_FILTER, PortletSession.APPLICATION_SCOPE);
        if(sessionBean != null && searchFilter != null)
        	sessionBean.setFilter(searchFilter);
        return sessionBean;
    }

    private String[] extractParametersFromURL(PortletRequest request, List<ParameterDescription> parametersMetaData) throws PortletException {
        String paramValueStr;
        String[] params = new String[parametersMetaData.size()];

        PortletService portletService = Portal.getFactory().getPortletService();

        boolean allNulls = true;
        int i = 0;
        for (Iterator<ParameterDescription> paramIter = parametersMetaData.iterator(); paramIter.hasNext(); i++) {
            ParameterDescription parameterDescription = paramIter.next();
            String paramName = parameterDescription.getName();

            paramValueStr = portletService.getUrlParameter(request, paramName);
            params[i] = paramValueStr;
            allNulls &= paramValueStr == null;
        }
        return allNulls ? null : params;
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
	private String getJspFilePath(RenderRequest request, String jspFile) {
		String markup = request.getProperty("wps.markup");
		if (markup == null)
			markup = getMarkup(request.getResponseContentType());
		return getJspFolder() + markup + "/" + jspFile + "." + getJspExtension(markup);
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

	protected String getJspView() {
		return jspView;
	}

	protected void setJspView(String jspView) {
		this.jspView = jspView;
	}

	protected String getJspFolder() {
		return jspFolder;
	}

	protected void setJspFolder(String jspFolder) {
		this.jspFolder = jspFolder;
	}
}
