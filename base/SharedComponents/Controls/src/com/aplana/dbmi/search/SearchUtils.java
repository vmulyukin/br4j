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
package com.aplana.dbmi.search;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.Search.DatePeriod;
import com.aplana.dbmi.action.Search.Interval;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.web.AbstractControl;
import com.aplana.dbmi.model.web.CheckboxControl;
import com.aplana.dbmi.model.web.ControlUtils;
import com.aplana.dbmi.model.web.WebBlock;
import com.aplana.dbmi.model.web.WebSearchBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class SearchUtils {

    public static void loadTemplates(DataServiceBean dataServiceBean, WebSearchBean searchBean) throws DataException, ServiceException {
        Collection templates = dataServiceBean.listAll(Template.class);
        Collections.sort((List) templates, new Comparator() {
			public int compare(Object obj1, Object obj2) {
				if (obj1 == null)
					return obj2 == null ? 0 : -1;
				if (obj2 == null)
					return 1;
				return ((Template) obj1).getName().compareTo(((Template) obj2).getName());
			}
        });
        searchBean.setDbTemplates(templates);
        List controlTemplates = new ArrayList();
        for (Iterator it = templates.iterator(); it.hasNext();) {
            Template template = (Template) it.next();
            CheckboxControl control = new CheckboxControl(template.getId().getId().toString(), template.getName(), template.getId().getId().toString());
            control.setLabelEn(template.getNameEn());
            control.setLabelRu(template.getNameRu());
            controlTemplates.add(control);
        }
        searchBean.setViewTemplates(controlTemplates);
    }

    public static void loadMainAttributes(DataServiceBean dataServiceBean, WebSearchBean searchBean) throws DataException, ServiceException, RemoteException {
        List mainAttributes = new ArrayList();
        AttributeBlock attributeBlock = (AttributeBlock) dataServiceBean.getById(AttributeBlock.ID_COMMON);
        searchBean.setDbAttributes(attributeBlock.getAttributes());
        for (Iterator it = attributeBlock.getAttributes().iterator(); it.hasNext();) {
            Attribute attribute = (Attribute) it.next();
            AbstractControl control = ControlUtils.initializeControl(attribute, dataServiceBean);
            if (control != null) {
                mainAttributes.add(control);
            }
            if (attribute instanceof StringAttribute || attribute instanceof TextAttribute) {
                searchBean.getAttributes().put(attribute.getId().getId(), attribute.getId().getId());
            }
        }

        WebBlock mainBlock = new WebBlock();
        mainBlock.setName(attributeBlock.getName());
        mainBlock.setNameEn(attributeBlock.getNameEn());
        mainBlock.setNameRu(attributeBlock.getNameRu());
        mainBlock.setId(attributeBlock.getId());
        mainBlock.setAttributes(mainAttributes);
        searchBean.setViewMainBlock(mainBlock);
    }

    public static Search getSearch(DataServiceBean dataServiceBean, WebSearchBean searchBean) throws DataException, ServiceException, RemoteException, ParseException {
        Search search = new Search();
        search.setByCode(searchBean.getNumber().booleanValue());
        search.setByAttributes(searchBean.getProperty().booleanValue());
        search.setByMaterial(searchBean.getFullText().booleanValue());
        search.setWords(searchBean.getSearchText());
        search.setTemplates(new ArrayList());
        for (Iterator it = searchBean.getDbTemplates().iterator(); it.hasNext();) {
            Template template = (Template) it.next();
            if (searchBean.getTemplates().containsKey(template.getId().getId().toString()) && searchBean.getTemplates().get(template.getId().getId().toString()) != null
                    && !"".equals(searchBean.getTemplates().get(template.getId().getId().toString()))) {
                search.getTemplates().add(template);
            }
        }

        List dbAttributes = new ArrayList();
        dbAttributes.addAll(searchBean.getDbAttributes());
        Map webAttMap = new HashMap();
        webAttMap.putAll(searchBean.getAttributes());
        for (Iterator it = dbAttributes.iterator(); it.hasNext();) {
            Attribute attribute = (Attribute) it.next();
            String attrIdStr = attribute.getId().getId().toString();
            if ((!webAttMap.containsKey(attribute.getId().getId().toString()) || webAttMap.get(attribute.getId().getId().toString()) == null
                    || "".equals(webAttMap.get(attribute.getId().getId().toString())) || "-1".equals(webAttMap.get(attribute.getId().getId().toString())))
                    && !(attribute instanceof TreeAttribute) && !(attribute instanceof ListAttribute) && !(attribute instanceof IntegerAttribute)) {
                continue;
            }

            if (attribute instanceof TextAttribute) {
                search.addStringAttribute(attribute.getId());
            } else if (attribute instanceof StringAttribute) {
                search.addStringAttribute(attribute.getId());
            } else if (attribute instanceof IntegerAttribute) {
                String tmpIntFrom = (String) webAttMap.get(attrIdStr + "_from");
                String tmpIntTo = (String) webAttMap.get(attrIdStr + "_to");

                Integer intFromO;
                int intFrom;
                if (tmpIntFrom != null && tmpIntFrom.length() > 0) {
                    intFromO = Integer.valueOf(tmpIntFrom);
                    intFrom = intFromO.intValue();
                } else {
                    intFromO = null;
                    intFrom = Integer.MIN_VALUE;
                }

                Integer intToO;
                int intTo;
                if (tmpIntTo != null && tmpIntTo.length() > 0) {
                    intToO = Integer.valueOf(tmpIntTo);
                    intTo = intToO.intValue();
                } else {
                    intToO = null;
                    intTo = Integer.MAX_VALUE;
                }

                if (intFromO != null || intToO != null) {
                    search.addIntegerAttribute(attribute.getId(), intFrom, intTo);
                }
            } else if (attribute instanceof DateAttribute) {
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                String tmpDateFrom = webAttMap.get(attribute.getId().getId()).toString();
                String tmpDateTo = webAttMap.get(attribute.getId().getId().toString() + "_new").toString();
                Date dateFrom;
                if (tmpDateFrom != null && !"".equals(tmpDateFrom)) {
                    try {
                    	dateFrom = dateFormat.parse(tmpDateFrom);
                    }
                    catch (ParseException e){
                    	dateFrom = new SimpleDateFormat("yyyy-MM-dd").parse(tmpDateFrom);
                    }
                } else {
                    dateFrom = null;
                }
                Date dateTo;
                if (tmpDateTo != null && !"".equals(tmpDateTo)) {
                	try {
                		dateTo = dateFormat.parse(tmpDateTo);
                	}
                	catch (ParseException e) {
                		dateTo = new SimpleDateFormat("yyyy-MM-dd").parse(tmpDateTo);
                	}
                } else {
                    dateTo = null;
                }
                search.addDateAttribute(attribute.getId(), dateFrom, dateTo);
            } else if (attribute instanceof TreeAttribute) {
                TreeAttribute treeAttribute = (TreeAttribute) attribute;
                Collection rootValues = dataServiceBean.listChildren(treeAttribute.getReference(), ReferenceValue.class);
                List referenceValues = new ArrayList();
                ControlUtils.getTreeReferenceValues(referenceValues, rootValues, webAttMap);
                if (referenceValues.size() > 0) {
                    search.addListAttribute(treeAttribute.getId(), referenceValues);
                }
            } else if (attribute instanceof ListAttribute) {
                ListAttribute listAttribute = (ListAttribute) attribute;
                Collection rootValues = dataServiceBean.listChildren(listAttribute.getReference(), ReferenceValue.class);
                List referenceValues = new ArrayList();
                ControlUtils.getListReferenceValues(referenceValues, rootValues, webAttMap);
                if (referenceValues.size() > 0) {
                    search.addListAttribute(listAttribute.getId(), referenceValues);
                }
            }
        }
        return search;

    }

    public static void loadAttributes(DataServiceBean dataServiceBean, WebSearchBean searchBean) throws DataException, ServiceException, RemoteException {
        Map searchTemplates = searchBean.getTemplates();
        String templateId = null;
        for (Iterator it = searchTemplates.entrySet().iterator(); it.hasNext();) {
            Entry entry = (Entry) it.next();
            String value = entry.getValue() == null ? null : entry.getValue().toString();
            if (value != null && value.length() > 0) {
                templateId = value;
                break;
            }
        }
        Collection blocks = null;
        if (templateId != null) {
            blocks = dataServiceBean.listChildren(new ObjectId(Template.class, templateId), TemplateBlock.class);
        } else {
            blocks = new ArrayList();
        }
        List webBlocks1 = new ArrayList();
        List webBlocks2 = new ArrayList();
        List dbAttributes = new ArrayList();
        for (Iterator itBlocks = blocks.iterator(); itBlocks.hasNext();) {
            TemplateBlock templateBlock = (TemplateBlock) itBlocks.next();
            /*
             * if(AttributeBlock.ID_COMMON.equals(templateBlock.getId())){
             * continue; }
             */
            WebBlock webBlock = new WebBlock();
            webBlock.setName(templateBlock.getName());
            webBlock.setNameEn(templateBlock.getNameEn());
            webBlock.setNameRu(templateBlock.getNameRu());
            webBlock.setId(templateBlock.getId());
            List webAttributes = new ArrayList();
            for (Iterator itAttr = templateBlock.getAttributes().iterator(); itAttr.hasNext();) {
                Attribute attribute = (Attribute) itAttr.next();
                dbAttributes.add(attribute);
                AbstractControl abstractControl = ControlUtils.initializeControl(attribute, dataServiceBean);
                if (abstractControl != null) {
                    webAttributes.add(abstractControl);
                }
                if (attribute instanceof StringAttribute || attribute instanceof TextAttribute) {
                    searchBean.getAttributes().put(attribute.getId().getId(), attribute.getId().getId());
                }
            }
            webBlock.setAttributes(webAttributes);
            webBlocks1.add(webBlock);
//TODO ���-�� ������� � ����������� Ace            
/*            if (templateBlock.getColumn() == 0) {
                webBlocks1.add(webBlock);
            } else {
                webBlocks2.add(webBlock);
            }
*/        }
        searchBean.setViewBlocks1(webBlocks1);
        searchBean.setViewBlocks2(webBlocks2);
        searchBean.setDbAttributes(dbAttributes);
    }

    public static WebBlock findBlock(WebSearchBean searchBean, ObjectId id) {
        List blocks = new ArrayList();
        blocks.addAll(searchBean.getViewBlocks1());
        blocks.addAll(searchBean.getViewBlocks2());
        for (Iterator it = blocks.iterator(); it.hasNext();) {
            WebBlock block = (WebBlock) it.next();
            if (id.getId().equals(block.getId().getId())) {
                return block;
            }

        }
        return null;

    }

    public static void initializeFromSearch(Search search, WebSearchBean searchBean) {
        searchBean.setSearchText(search.getWords());
        searchBean.setFullText(Boolean.valueOf((search.isByMaterial())));
        searchBean.setNumber(Boolean.valueOf(search.isByCode()));
        searchBean.setProperty(Boolean.valueOf(search.isByAttributes()));
        searchBean.setIsAllTemplates(Boolean.TRUE);
        if (search.getTemplates() != null && !search.getTemplates().isEmpty()) {
            searchBean.setIsAllTemplates(Boolean.FALSE);
            for (Iterator it = search.getTemplates().iterator(); it.hasNext();) {
                Template template = (Template) it.next();
                searchBean.getTemplates().put(template.getId().getId().toString(), template.getId().getId().toString());
            }
        }
        if (searchBean.getDbAttributes() != null) {
            searchBean.getAttributes().clear();
            for (Iterator it = searchBean.getDbAttributes().iterator(); it.hasNext();) {
                Attribute attribute = (Attribute) it.next();
                if (!search.hasAttribute(attribute.getId())) {
                    continue;
                }
                if (attribute instanceof StringAttribute || attribute instanceof ListAttribute) {
                    searchBean.getAttributes().put(attribute.getId().getId().toString(), attribute.getId().getId().toString());
                } else if (attribute instanceof DateAttribute) {
                    DatePeriod datePeriod = search.getDateAttributePeriod(attribute.getId());
                    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    searchBean.getAttributes().put(attribute.getId().getId().toString(), dateFormat.format(datePeriod.start));
                    searchBean.getAttributes().put(attribute.getId().getId().toString() + "_new", dateFormat.format(datePeriod.end));
                } else if (attribute instanceof IntegerAttribute) {
                    ObjectId attributeId = attribute.getId();
                    Interval integerAttributeInterval = search.getIntegerAttributeInterval(attributeId);
                    searchBean.getAttributes().put(attributeId.getId().toString() + "_from", Long.toString(integerAttributeInterval.min));
                    searchBean.getAttributes().put(attributeId.getId().toString() + "_to", Long.toString(integerAttributeInterval.max));
                } else if (attribute instanceof ListAttribute || attribute instanceof TreeAttribute) {
                    Collection referenceValues = search.getListAttributeValues(attribute.getId());
                    for (Iterator itRef = referenceValues.iterator(); itRef.hasNext();) {
                        ReferenceValue referenceValue = (ReferenceValue) itRef.next();
                        searchBean.getAttributes().put(referenceValue.getId().getId().toString(), referenceValue.getId().getId().toString());
                    }
                }
            }
        }

    }

    public static void moveToRenderParameters(ActionRequest request, ActionResponse response) {

        Set parameterEntries = request.getParameterMap().entrySet();
        for (Iterator it = parameterEntries.iterator(); it.hasNext();) {
            Entry entry = (Entry) it.next();
            if (entry.getKey() != null && entry.getKey().toString().indexOf("my") >= 0 && entry.getValue() != null && ((String[]) entry.getValue()).length > 0
                    && !"".equals(((String[]) entry.getValue())[0])

            ) {
                response.setRenderParameter(entry.getKey().toString().substring(2), ((String[]) entry.getValue())[0]);
            }
        }
    }

}
