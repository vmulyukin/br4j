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
package com.aplana.dbmi.portlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.aplana.dbmi.action.GetPersonByLogin;
import com.aplana.dbmi.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.card.hierarchy.Messages;
import com.aplana.dbmi.card.hierarchy.descriptor.MessagesReader;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.util.DateUtils;

public class BrowsingReportsPortletConfReader {
    static final String CONFIG_FOLDER = "dbmi/jasperReports/confPortlet/";

    private final Document doc;
    private final XPath xpath;
    private final DataServiceBean dataService;
    
    private static final String PARAM_CURRENT_YEAR_SELECTED = "currentYearSelected";
    private static final String PARAM_CURRENT_MONTH_SELECTED = "currentMonthSelected";
    private static final String PARAM_CURRENT_QUARTER_SELECTED = "currentQuarterSelected";
	private static final String PARAM_START_YEAR = "startYear";
	private static final String PARAM_ALL_VALUES = "all";
	private static final String VALUE_ALL_VALUES = "-1";
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	private XPathExpression reportsExpression = null;
	private XPathExpression kitsExpression = null;
	private XPathExpression parameterExpression = null;
	private XPathExpression optionExpression = null;
	private XPathExpression dependingValues = null;
	private Messages messages = null;

    BrowsingReportsPortletConfReader(String confFile, DataServiceBean dataService) throws IOException, SAXException,
            ParserConfigurationException {
        InputStream stream = Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FOLDER + confFile);
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);

        XPathFactory factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
        this.dataService = dataService;
    }

    /* ������ ����������� �������� JSON
    {
    	reports: [ // �������� ����������� ������ ������ ������ ��� ������
    		{	id: "reportChartAD",
    			label: "������� �� ������������� ������������ � ���������� ���������, ����������� �� �������� � ��"
    		},
    		...
    	],
    	controls: { // �������� ��������� ����� ����������
    		reportChartAD : { // id-� ������
    			root: "rootSet", // �������� ����� ����������, ������������ ��� ������ ����-�� ������
    			kits : { // �������� ������ ���������. ����� ��������� ������������ ���������. 
    					//����� ��������� ����� ���� ��������� ��� �������. ��������� ����������� ����� ���. ����������. 
    					//����� ���������� ����� ���� ������ � ������ ����� ����������
    				"rootSet": [ // ������ ���������
    					{	name: "dateReg",
    						label: "���� �����������",
    						type: "Date",
    						require: true
    					},
    					{	name: "choice1",
    						label: "����� �������������� ���������� ������",
    						type: "SelectAdditionalControls",
    						require: false
    						options: [
    							{	ref: "SelectOne",
    								name: "������ ����� �������������� ����������"
    							},
    							{	ref: "SelectTwo",
    								name: "������ ����� �������������� ����������"
    							}
    						]
    					}
    				],
    				"SelectOne": [
    					{	name: "input1",
    						label: "������ ���� ��� �����",
    						type: "String",
    						require: true
    					},
    					{	name: "ch1",
    						label: "����� ��������� �� �����������",
    						type: "ValuesRef",
    						require: false,
    						values: [
    							{	id:	"id1",
    								label: "�������� 1"
    							},
    							{	id: "id2",
    								label: "�������� 2"
    							}
    						]
    					}
    				],
    				...
    			}
    		},
    		...
    	}
    }
    */
    String getJSONConfReports() throws IOException, SAXException, ParserConfigurationException,
            XPathExpressionException, JSONException, DataException, ServiceException {
        reportsExpression = xpath.compile("/reports/report");
        kitsExpression = xpath.compile("./kits/kit");
        parameterExpression = xpath.compile("./parameter");
        optionExpression = xpath.compile("./option");
        dependingValues = xpath.compile("./depending/dependence");

        GetPersonByLogin getPersonByLogin = new GetPersonByLogin(dataService.getUserName());
        Person currentUser = dataService.doAction(getPersonByLogin);

        MessagesReader messagesReader = new MessagesReader(xpath);
        messages = messagesReader
                .read((Element) xpath.evaluate("/reports/messages", doc, XPathConstants.NODE));

        JSONObject json = new JSONObject();

        JSONObject reports = new JSONObject();
        JSONObject controls = new JSONObject();
        NodeList reportNodes = (NodeList) reportsExpression.evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < reportNodes.getLength(); i++) {
            Element reportEl = (Element) reportNodes.item(i);

            // set reports
            JSONObject reportJson = new JSONObject();
            reportJson.put("id", reportEl.getAttribute("id"));
            reportJson.put("exportType", reportEl.hasAttribute("exportType") ? reportEl.getAttribute("exportType") : getExportType());
            if(reportEl.hasAttribute("fileName")) {
            	reportJson.put("fileName", reportEl.getAttribute("fileName"));
            }
            if (messages.getMessage(reportEl.getAttribute("title")) != null) {
                reportJson.put("label", messages.getMessage(reportEl.getAttribute("title")).getValue());
            } else {
                reportJson.put("label", reportEl.getAttribute("title"));
            }
            reports.put(reportEl.getAttribute("id"), reportJson);

            // set controls
            JSONObject reportControls = new JSONObject();
            reportControls.put("root", reportEl.getAttribute("rootKit"));

            JSONObject kits = new JSONObject();
            NodeList kitNodes = (NodeList) kitsExpression.evaluate(reportEl, XPathConstants.NODESET);
            for (int j = 0; j < kitNodes.getLength(); j++) {
                Element kitEl = (Element) kitNodes.item(j);

                JSONArray parameters = new JSONArray();
                NodeList parameterNodes = (NodeList) parameterExpression.evaluate(kitEl, XPathConstants.NODESET);
                for (int k = 0; k < parameterNodes.getLength(); k++) {
                    Element parameterEl = (Element) parameterNodes.item(k);

                    JSONObject parameterJson = new JSONObject();
                    parameterJson.put("name", parameterEl.getAttribute("name"));
                    String type = parameterEl.getAttribute("type");
                    parameterJson.put("type", type);
                    parameterJson.put("label", messages.getMessage(parameterEl.getAttribute("title")).getValue());
                    String require = parameterEl.getAttribute("require");
                    if (require != null) {
                        parameterJson.put("require", Boolean.parseBoolean(require));
                    }
                    if (type.equals("Boolean")){
                    	parameterJson.put("value", Boolean.parseBoolean(parameterEl.getAttribute("value")));
                    }
                    
                    if (type.equals("ValuesRef") || type.equals("ValuesRefList")) {
                        JSONArray allValues = getValuesRef(parameterEl.getAttribute("idRef"));
                        parameterJson.put("values", allValues);
                        JSONArray defValues = new JSONArray();
                        String allDiffersFromEmpty = parameterEl.getAttribute("allDiffersFromEmpty");
                        /*if(allDiffersFromEmpty != null){
                        	parameterJson.put("allDiffersFromEmpty", Boolean.parseBoolean(allDiffersFromEmpty));
                        }*/
                        parameterJson.put("allDiffersFromEmpty", parameterEl.getAttribute("allDiffersFromEmpty"));
                        
                        String s = parameterEl.getAttribute("defValues");
                        String sa[] = s.split(",");
                        for (int l = 0; l < allValues.length(); l++) {
                            JSONObject cur = (JSONObject) allValues.get(l);
                            for (int m = 0; m < sa.length; m++) {
                                if (sa[m].equals(cur.get("id"))) {
                                    defValues.put(cur);
                                }
                            }
                        }
                        // ��������� ���������� �� ��������� �������� "���"
                        if(PARAM_ALL_VALUES.equalsIgnoreCase(s)) {
                        	defValues = new JSONArray();
                        	JSONObject cur = new JSONObject();
                        	cur.put("id", VALUE_ALL_VALUES);
                            defValues.put(cur);
                        }
                        parameterJson.put("defvalues", defValues);
                        String allGroups = parameterEl.getAttribute("allGroups");
                        if (allGroups != null && !allGroups.equals("")) {
                        	parameterJson.put("allGroups", messages.getMessage(allGroups).getValue());
                        }
                    } else if(type.equals("Radio")){
                        String defValue = parameterEl.getAttribute("defValue");
                        parameterJson.put("defValue", defValue);
                        JSONArray options = new JSONArray();
                        NodeList optionNodes = (NodeList) optionExpression
                                .evaluate(parameterEl, XPathConstants.NODESET);
                        for (int m = 0; m < optionNodes.getLength(); m++) {
                            Element optionEl = (Element) optionNodes.item(m);

                            JSONObject optionJson = new JSONObject();
                            optionJson.put("id", optionEl.getAttribute("id"));
                            optionJson.put("name", messages.getMessage(optionEl.getAttribute("title")).getValue());

                            options.put(optionJson);
                        }
                        parameterJson.put("options", options);                   	
                    	
                    }else if (type.equals("SelectKit") ) {
                    	
                    	getMultiValued(parameterEl, parameterJson, messages);
                    	
                        String defValue = parameterEl.getAttribute("defValue");
                        String currentMouth = parameterEl.getAttribute(PARAM_CURRENT_MONTH_SELECTED);
                        String currentQuarter = parameterEl.getAttribute(PARAM_CURRENT_QUARTER_SELECTED);
                        if(isChangeParameter(currentMouth)){
                        	defValue = String.valueOf(DateUtils.getMonth() + 1);
                        } else if(isChangeParameter(currentQuarter)){
                        	defValue = String.valueOf(DateUtils.getMonth() / 3 + 1);
                        }
                        parameterJson.put("defValue", defValue);
                        JSONArray options = new JSONArray();
                        NodeList optionNodes = (NodeList) optionExpression
                                .evaluate(parameterEl, XPathConstants.NODESET);
                        for (int m = 0; m < optionNodes.getLength(); m++) {
                            Element optionEl = (Element) optionNodes.item(m);

                            JSONObject optionJson = new JSONObject();
                            optionJson.put("ref", optionEl.getAttribute("ref"));
                            optionJson.put("dep_value", optionEl.getAttribute("dep_value"));
                            optionJson.put("name", messages.getMessage(optionEl.getAttribute("title")).getValue());

                            options.put(optionJson);
                        }
                        parameterJson.put("options", options);
                        parameterJson.put("dep_control", parameterEl.getAttribute("dep_control"));
                    } else if(type.equals("YearPeriod")) {
            			
                    	getMultiValued(parameterEl, parameterJson, messages);
                    	
                    	boolean curYearSelected = false;
                    	String currentYearSelected = parameterEl.getAttribute(PARAM_CURRENT_YEAR_SELECTED);
            			String startYear = parameterEl.getAttribute(PARAM_START_YEAR);

            			if(currentYearSelected!=null && !currentYearSelected.equals(""))
            				curYearSelected = Boolean.parseBoolean(currentYearSelected);
            			
            			getYears(curYearSelected, startYear, parameterJson);            			
            			
            		} else if (type.equals("Card")) {
                        parameterJson.put("template", parameterEl.getAttribute("template"));
                        parameterJson.put("query", parameterEl.getAttribute("query"));
                        parameterJson.put("sqlxml", parameterEl.getAttribute("sqlxml"));
                    } else if (type.equals("Cards")) {
                        parameterJson.put("template", parameterEl.getAttribute("template"));
                        parameterJson.put("query", parameterEl.getAttribute("query"));
                        parameterJson.put("dep_control", parameterEl.getAttribute("dep_control"));
                        parameterJson.put("dep_attr", parameterEl.getAttribute("dep_attr"));
                        parameterJson.put("sqlxml", parameterEl.getAttribute("sqlxml"));
                        parameterJson.put("group", parameterEl.getAttribute("group"));
                        /*String allDiffersFromEmpty = parameterEl.getAttribute("allDiffersFromEmpty");
                        if(allDiffersFromEmpty != null){
                        	parameterJson.put("allDiffersFromEmpty", Boolean.parseBoolean(allDiffersFromEmpty));
                        }*/
                        parameterJson.put("allDiffersFromEmpty", parameterEl.getAttribute("allDiffersFromEmpty"));
                        String allSelected = parameterEl.getAttribute("allSelected");
                        if(allSelected != null && !"".equals(allSelected.trim()))
                        	parameterJson.put("allSelected", messages.getMessage(parameterEl.getAttribute("allSelected")).getValue());
                        String buttons = parameterEl.getAttribute("buttons");
                        if (buttons != null) {
                            parameterJson.put("buttons", Boolean.parseBoolean(buttons));
	                        String allGroups = parameterEl.getAttribute("allGroups");
	                        if (allGroups != null && !allGroups.equals("")) {
	                        	parameterJson.put("allGroups", messages.getMessage(allGroups).getValue());
	                        }
                        }
                        String defValue = parameterEl.getAttribute("defValue");
                        if ("current".equals(defValue)) {
                            defValue = dataService.getPerson().getCardId().getId().toString();
                        } else if("userOrg".equals(defValue)){
                            Card userCard = dataService.getById(currentUser.getCardId());
                            if(userCard.getCardLinkAttributeById(BrowsingReportsPortlet.ORGANIZATION_ID).isEmpty()){
                                defValue = null;
                            } else {
                                defValue = userCard.getCardLinkAttributeById(BrowsingReportsPortlet.ORGANIZATION_ID)
                                            .getSingleLinkedId().getId().toString();
                            }
                        }
                        if(defValue!=null){
                            JSONObject obj = getJSONPerson(defValue);
                            parameterJson.put("defValue", obj);
                        }
                    }else if(type.equals("DatePeriod")){
                    	createDatePeriod(parameterEl, parameterJson);
                    }

                    parameters.put(parameterJson);
                }

                String kitId = kitEl.getAttribute("id");
                kits.put(kitId, parameters);
            }

            reportControls.put("kits", kits);

            String reportId = reportEl.getAttribute("id");
            controls.put(reportId, reportControls);
        }
        json.put("reports", reports);
        json.put("controls", controls);

        return json.toString();
    }
    
    private void createDatePeriod(Element parameterEl, JSONObject jsonObject) throws JSONException, XPathExpressionException {

	    jsonObject.put("options", parseOptionNodes(parameterEl));

	    jsonObject.put("depending", parseDependingNodes(parameterEl));	   

	}
    
    private JSONArray parseDependingNodes(Element parameterEl) throws XPathExpressionException, JSONException{
	    JSONArray depending = new JSONArray();
	    NodeList dependingNodes = (NodeList) dependingValues.evaluate(parameterEl, XPathConstants.NODESET);
	    for (int i = 0; i < dependingNodes.getLength(); i++) {
	        Element optionEl = (Element) dependingNodes.item(i);
	        JSONObject optionJson = new JSONObject();
	        optionJson.put("id", optionEl.getAttribute("id"));
	        optionJson.put("condition", optionEl.getAttribute("condition"));
	        optionJson.put("action", optionEl.getAttribute("action"));	        
	        depending.put(optionJson);
		}
	    return depending;
    }
    
    /**
     * ��� ��������� ���� <option> ������ ����� ������������ ���� �����
     * @param parameterEl
     * @return
     * @throws XPathExpressionException
     * @throws JSONException
     */
    private JSONArray parseOptionNodes(Element parameterEl) throws XPathExpressionException, JSONException{
	    JSONArray options = new JSONArray();
	    NodeList optionNodes = (NodeList) optionExpression
	            .evaluate(parameterEl, XPathConstants.NODESET);
	    for (int i = 0; i < optionNodes.getLength(); i++) {
	        Element optionEl = (Element) optionNodes.item(i);
	        JSONObject optionJson = new JSONObject();
	        optionJson.put("id", optionEl.getAttribute("id"));
	        optionJson.put("name", messages.getMessage(optionEl.getAttribute("title")).getValue());
	        options.put(optionJson);
		}
	    return options;
    }
    

	private boolean isChangeParameter(String strBool){
		boolean bool = false;
		
    	if(strBool!=null && !strBool.equals("")){
    		bool = Boolean.parseBoolean(strBool);
    	}
    	return bool;    	
    }
    
    private void getYears(boolean currentYearSelected, String startYearParam, JSONObject parameterJson) throws JSONException, DataException, ServiceException {
		Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
		
		Integer startYear = null;
		if(startYearParam!=null && !startYearParam.equals("")){
			try {
				
				int parsedValue = Integer.parseInt(startYearParam);
				
				if(parsedValue < 0)
					startYear = currentYear + parsedValue;
				else
					startYear = parsedValue;
					
			} catch(NumberFormatException nfe){
				logger.error("�������� �������� ��������� yearsPeriod = " + startYearParam);
			}		
		}
		
		if(startYear == null || startYear > currentYear || startYear < 0)
			startYear = currentYear;
		
		JSONArray options = new JSONArray();
		for (int i = startYear; i <= currentYear; i++) {
			JSONObject obj = new JSONObject();
			obj.put("ref", "" + i);
			obj.put("name", "" + i);
			if(currentYearSelected && i==currentYear){
				parameterJson.put("defValue", "" + i);
			}
			options.put(obj);
		}
		parameterJson.put("options", options);
	}
    
    private void getMultiValued(Element parameterEl, JSONObject parameterJson, Messages messages) throws JSONException{
    	String multiValued = parameterEl.getAttribute("multiValued");
        if (multiValued != null) {
            parameterJson.put("multiValued", Boolean.parseBoolean(multiValued));
            String allGroups = parameterEl.getAttribute("allGroups");
            if (allGroups != null && !allGroups.equals("")) {
            	parameterJson.put("allGroups", messages.getMessage(allGroups).getValue());
            }
            
        }
    }

    String getExportType() throws XPathExpressionException {
        XPathExpression exportExpression = xpath.compile("/reports/exportType");
        Element elExport = (Element) exportExpression.evaluate(doc, XPathConstants.NODE);
        return elExport.getTextContent();
    }

    // ��������� �������� �����������: [{id: "", label: ""}, ...]
    JSONArray getValuesRef(String idRefs) throws JSONException, DataException, ServiceException {
        JSONArray result = new JSONArray();
        for(String idRef: idRefs.split(",")){
	        ObjectId objIdRef = new ObjectId(Reference.class, idRef);
	        Collection<ReferenceValue> refValues = dataService.listChildren(objIdRef, ReferenceValue.class);
	        Iterator<ReferenceValue> iterRefVal = refValues.iterator();
	        while (iterRefVal.hasNext()) {
	            ReferenceValue refVal = iterRefVal.next();
	            JSONObject obj = new JSONObject();
	            obj.put("id", refVal.getId().getId().toString());
	            if (refVal.getValueRu() != null) {
	                obj.put("label", refVal.getValueRu());
	            } else {
	                obj.put("label", "");
	            }
	            result.put(obj);
	        }
        }
        return result;
    }
    
    /*
     * ���������� json-������ ������������: {cardId: "", name: ""}
     */
    JSONObject getJSONPerson(String persCardId) throws DataException, ServiceException, JSONException {
        if (persCardId == null || persCardId.length() == 0) return null;
        Long id = Long.parseLong(persCardId);
        
        JSONObject result = new JSONObject();
        ObjectId objPersCard = new ObjectId(Card.class, id);
        
        Card persCard = (Card) dataService.getById(objPersCard);
        String name = persCard.getAttributeById(new ObjectId(StringAttribute.class, "NAME")).getStringValue();
        result.put("cardId", id);
        result.put("name", name);
        return result;
    }
}
