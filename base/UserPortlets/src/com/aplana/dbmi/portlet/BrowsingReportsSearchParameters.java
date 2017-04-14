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

import java.io.InputStream;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.Search.NumericIdList;
import com.aplana.dbmi.action.xml.SearchXmlHelper;
import com.aplana.dbmi.ajax.SearchCardServletParameters;
import com.aplana.dbmi.ajax.SearchResultLabelBuilder;
import com.aplana.dbmi.card.hierarchy.descriptor.LinkDescriptor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.search.ext.RouteSearchObjectId;
import com.aplana.dbmi.service.DataServiceBean;

public class BrowsingReportsSearchParameters extends SearchResultLabelBuilder implements SearchCardServletParameters  {
	public static final String CALLER = "BrowsingReportsPortlet";
	public static final String PARAM_NAMESPACE = "namespace";
	public static final String PARAM_TEMPLATE = "template";
	public static final String PARAM_QUERY = "query";
	public static final String PARAM_DEP_ATTR = "dep_attr";
	public static final String PARAM_DEP_VALUES = "dep_values";
	public static final String PARAM_SQLXML = "sqlxml";
	private static final String CONFIG_FILE_PREFIX = "dbmi/";
	private Search search;
	
	public ObjectId getLabelAttrId() {
		return Attribute.ID_NAME;
	}

	public Search getSearch() {
		return search;
	}

	public void initialize(HttpServletRequest request, DataServiceBean serviceBean) throws ServletException {
		try {
			String namespace = request.getParameter(PARAM_NAMESPACE);
			BrowsingReportsPortletSessionBean sessionBean = (BrowsingReportsPortletSessionBean)BrowsingReportsPortlet.getSessionBean(request, namespace);
			
			String templateId = request.getParameter(PARAM_TEMPLATE);
			String queryXml = request.getParameter(PARAM_QUERY);
			String depAttrXml = request.getParameter(PARAM_DEP_ATTR);
			String depValuesXml = request.getParameter(PARAM_DEP_VALUES);
			String sqlXml = request.getParameter(PARAM_SQLXML);

			
			if (queryXml != null && queryXml.length()>0){
				Search cardSearch = new Search();
	            InputStream inputStream = Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILE_PREFIX + queryXml);
				cardSearch.initFromXml(inputStream);
				// ���� ����� ��������� �������� � Search
				if(!"".equals(depAttrXml) && !"".equals(depValuesXml)){
					if(depAttrXml.indexOf(Attribute.LABEL_ATTR_PARTS_SEPARATOR) == -1){
						//���������� ����������� � ����� ������� �����
						ObjectId queryAttr = SearchXmlHelper.safeMakeId(depAttrXml);
						String[] attrValuesSplit = depValuesXml.split(",");
						ArrayList attrValues = new ArrayList(attrValuesSplit.length);
						if (queryAttr != null && ListAttribute.class.equals(queryAttr.getType())) {						
							for(String attrValueSplit : attrValuesSplit){
								ObjectId refValId = ObjectId.predefined(ReferenceValue.class, attrValueSplit);
								if (refValId == null)
									refValId = new ObjectId(ReferenceValue.class, Long.parseLong(attrValueSplit));
								attrValues.add(DataObject.createFromId(refValId));
							}
							cardSearch.addListAttribute(queryAttr, attrValues);
						} else if (queryAttr != null && CardLinkAttribute.class.equals(queryAttr.getType())) {
							for(String attrValueSplit : attrValuesSplit){
								ObjectId cardId = new ObjectId(Card.class, Long.parseLong(attrValueSplit));
								cardSearch.addCardLinkAttribute(queryAttr, cardId);
							}
						}
					} else {
						//����������� ����� ��������� ���������
						NumericIdList list = new NumericIdList();
						for(String value: depValuesXml.split(",")){
							list.addId(new ObjectId(Card.class, Long.parseLong(value)));
						}
						ObjectId objectId =  SearchXmlHelper.safeMakeId(depAttrXml.split(Attribute.LABEL_ATTR_PARTS_SEPARATOR)[0]);//����� ������ ID � �������� "��� �������"
						
						RouteSearchObjectId routePathId = new RouteSearchObjectId(objectId.getType(), objectId.getId(), depAttrXml);
						cardSearch.addAttribute(routePathId, list);
					}
				}
				this.search = cardSearch;
			} else if(sqlXml != null && sqlXml.length() > 0)
			{
				Search cardSearch = new Search();
				cardSearch.setSqlXmlName(sqlXml);
				this.search = cardSearch;
			}
			else if (templateId != null && templateId.length()>0){
				Search cardSearch = new Search();
				cardSearch.setByAttributes(true);
				ArrayList templates = new ArrayList();
				
				Template template;
					template = (Template)serviceBean.getById(ObjectId.predefined(Template.class, templateId));
				
				templates.add(template);
				cardSearch.setTemplates(templates);
				this.search = cardSearch;
			}else{
				this.search = sessionBean.getEmployeesSearch();
			}
		}catch(Exception ex){
			throw new ServletException("Can not init BrowsingReportsSearchParameters", ex); 
		}
	}

	public LinkDescriptor getList() {
		// TODO Auto-generated method stub
		return null;
	}	
}
