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
package com.aplana.dbmi.cardexchange.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
//import java.util.Properties;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.cardexchange.service.CardExchangeException;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataService;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.DataServiceHome;
import com.aplana.dbmi.service.SystemUser;

public class CardExchangeUtils {
	private final static String NS = "http://aplana.com/dbmi/exchange/model/Card";
	private final static Log logger = LogFactory.getLog(CardExchangeUtils.class);
	public static final String TEMPLATE_PERSON = "jbr.internalPerson";
	public static final String PERSON_NAME = "name";
	
	public static ObjectId getObjectId(Class clazz, String code, boolean isNumeric) {
		ObjectId result = ObjectId.predefined(clazz, code);
		if (result == null) {
			if (isNumeric) {
				result = new ObjectId(clazz, Long.parseLong(code));
			} else {
				result = new ObjectId(clazz, code);
			}
		}
		return result;
	}
	
	public static Document getCardXML(Card c) throws DataException {
		if (c.getId() == null) {
		    throw new CardExchangeException("cardExport.error.newCard");
		}
		try {
		    DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		    DocumentBuilder db = f.newDocumentBuilder();
		    Document doc = db.newDocument();
		    Element root = doc.createElementNS(NS, "card");
		    root.setAttribute("id", String.valueOf(c.getId().getId()));
		    root.setAttribute("templateId", String.valueOf(c.getTemplate()
			    .getId()));
		    doc.appendChild(root);
		    Iterator i = c.getAttributes().iterator();
		    // Properties aliases = new Properties();
		    // aliases.load(CardExchangeUtils.class.getResourceAsStream("/attribute_aliases.properties"));
		    while (i.hasNext()) {
			TemplateBlock block = (TemplateBlock) i.next();
			Iterator j = block.getAttributes().iterator();
			while (j.hasNext()) {
			    Attribute attr = (Attribute) j.next();
			    AttributeXMLHandler xmlType = AttributeXMLHandler
				    .getXmlType(attr.getClass());
			    if (xmlType == null) {
				logger.warn("Unsupported attribute type: "
					+ attr.getClass());
				continue;
			    }
			    Element attrElem = doc.createElementNS(NS, "attribute");
			    String attrCode = (String) attr.getId().getId();
			    if (attrCode == null) {
				attrCode = (String) attr.getId().getId();
			    }
			    attrElem.setAttribute("code", attrCode);
			    attrElem.setAttribute("type", xmlType.getXmlType());
			    if (attr.isMultiValued()) {
				attrElem.setAttribute("multiValued", "true");
			    }
			    attrElem.setAttribute("name", attr.getName());
			    AttributeXMLValue[] values = xmlType.getValue(attr);
			    if (values != null) {
				for (int k = 0; k < values.length; ++k) {
				    AttributeXMLValue val = values[k];
				    if (val != null) {
					Element valElem = doc.createElementNS(NS, "value");
					valElem.setTextContent(val.getValue());
					if (val.getDescription() != null) {
					    valElem.setAttribute("description", val
						    .getDescription());
					}
					attrElem.appendChild(valElem);
				    }
				}
			    }
				root.appendChild(attrElem);
			}
		    }
		    return doc;
		} catch (Exception e) {
		    logger.error("An error occured while exporting card to XML", e);
		    throw new CardExchangeException("cardExport.failed", new Object[] {
			    c.getId().getId(), e.getMessage() }, e);
		}
	    }
	
	public static ObjectId getPersonIdByName(String personName) {
		ObjectId retVal = null;
		Card personCard = null;
		DataServiceBean serviceBean = null;
		DataService service = null;
		try {
			System.out.println("Person string is: " + personName);
			InitialContext context = new InitialContext();
			DataServiceHome home = (DataServiceHome) PortableRemoteObject
					.narrow(context.lookup("ejb/dbmi"), DataServiceHome.class);
			service = home.create();
			serviceBean = new DataServiceBean();
			serviceBean.setService(service, service.authUser(new SystemUser(),
					"127.0.0.1"));

			Search search = new Search();
			search.setWords(personName);
			search.setByAttributes(true);
			search.addStringAttribute(ObjectId.predefined(
					StringAttribute.class, PERSON_NAME));
			List templates = new ArrayList(1);
			templates.add(DataObject.createFromId(ObjectId.predefined(
					Template.class, TEMPLATE_PERSON)));
			search.setTemplates(templates);

			Collection cards = ((SearchResult) serviceBean.doAction(search))
					.getCards();

			if (cards.size() == 0) {
				throw new Exception("Could't find person by name!");
			}
			if (cards.size() > 1) {
				System.out.println("More than one persons found!");
			}
			Iterator iter = cards.iterator();
			personCard = (Card) iter.next();
			personCard = (Card) serviceBean.getById(personCard.getId());
			retVal = personCard.getId();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}

	}
}
