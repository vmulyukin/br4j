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
import java.util.HashSet;
import java.util.Set;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.model.Attribute;

public class OptionsSupportingStringAttributeEditor extends StringAttributeEditor
{
	public static final String PARAM_OPTIONS = "options";
	private Set<String> options = new HashSet<String>();
	public OptionsSupportingStringAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/StringWithOptions.jsp");
	}
	
	public void setParameter(String name, String value){
		if(name.equalsIgnoreCase("config")) readConfig(value);
		else super.setParameter(name, value);
	}
	
	public void writeEditorCode(RenderRequest request, RenderResponse response, Attribute attr) throws IOException, PortletException{
		request.setAttribute(PARAM_OPTIONS, options);
		super.writeEditorCode(request, response, attr);
	}
	
	private void readConfig(String filename){
		try {
			final InputStream stream = Portal.getFactory().getConfigService().loadConfigFile(AttributeEditorFactory.CONFIG_FOLDER + filename);
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
		    Element root = doc.getDocumentElement();
		    XPath xpath = XPathFactory.newInstance().newXPath();
		    NodeList values = (NodeList) xpath.evaluate("/options/option/text()", root, XPathConstants.NODESET);
		    for(int i = 0; i < values.getLength(); i++){
		    	options.add(values.item(i).getTextContent());
		    }
		} catch (Exception e) {
			logger.error("Error while reading from configuration file (" + filename + ")");
			e.printStackTrace();
		}
	}
}
