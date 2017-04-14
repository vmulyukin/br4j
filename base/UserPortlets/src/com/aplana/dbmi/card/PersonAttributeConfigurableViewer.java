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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.portlet.PortletRequest;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.xml.SearchXmlHelper;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.DataException;

public class PersonAttributeConfigurableViewer extends CardLinkAttributeViewer {
	
	public static final String PARAM_CONFIG = "config";
	private String config = null;
	protected Collection<SearchResult.Column> columns;
	
	@Override
	public void initEditor(PortletRequest request, Attribute attr){ 
		try{
			initColumnsFromXml();
			super.initEditor(request, attr);		
		} catch(Exception e){logger.error("Exception during initialization: " + e.getMessage()); e.printStackTrace();}
	}
	@Override
	public void loadAttributeValues(Attribute attr, PortletRequest request) {
		LinkedCardUtils.reloadLinks(request, (PersonAttribute) attr, columns);
	}
	
	@Override
	public void setParameter(String name, String value) {
		if(name.equals(PARAM_CONFIG)) config = value;
		else super.setParameter(name, value);
	}
	
	private void initColumnsFromXml() throws DataException{
		if(config != null){
			try {
				final Search search = new Search();
				final InputStream xml = Portal.getFactory().getConfigService().loadConfigFile(AttributeEditorFactory.CONFIG_FOLDER + config);
				try {
					SearchXmlHelper.initFromXml(search, xml);
				} finally {
					xml.close();
				}
				final Collection<SearchResult.Column> columnsLocal = search.getColumns();
				if(columnsLocal != null && !columnsLocal.isEmpty())
					this.columns = columnsLocal;
			} catch (IOException e) {
				logger.error("Couldn't open hierarchy descriptor file: " + config, e);
			}
		}
	}
}
