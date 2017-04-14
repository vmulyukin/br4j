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
package com.aplana.dbmi.archive.export;

import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.aplana.dbmi.action.GetActionLog;
import com.aplana.dbmi.action.GetCardVersionHistory;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;

/**
 * ���������� ������� � XML � �������� ����������
 * @author ppolushkin
 * 
 * TODO: ��������� ����������� �������� ������� ��������
 *
 */
public class CardXMLExporter implements CardExporter {
	
	protected static Log logger = LogFactory.getLog(CardXMLExporter.class);
	
	private List<ObjectId> nestedCardsToExport;
	
	private List<ObjectId> exportedCards;

	private DataServiceFacade service;

	public List<ObjectId> getNestedCardsToExport() {
		if(nestedCardsToExport == null) {
			nestedCardsToExport = new ArrayList<ObjectId>();
		}
		return nestedCardsToExport;
	}

	public void setNestedCardsToExport(List<ObjectId> nestedCardsToExport) {
		this.nestedCardsToExport = nestedCardsToExport;
	}
	
	public List<ObjectId> getExportedCards() {
		if(exportedCards == null) {
			exportedCards = new ArrayList<ObjectId>();
		}
		return exportedCards;
	}

	public void setExportedCards(List<ObjectId> exportedCards) {
		this.exportedCards = exportedCards;
	}
	
	public DataServiceFacade getService() {
		return service;
	}

	public void setService(DataServiceFacade service) {
		this.service = service;
	}
	
	private AttributeXMLExporterFactory getFactory() {
		return AttributeXMLExporterFactory.getFactory();
	}
	
	@Override
	public Document export(Card c) throws DOMException, ParserConfigurationException, DataException {
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db = f.newDocumentBuilder();
		Document doc = db.newDocument();
		Element root = doc.createElement("archiveCard");
		export(c, doc, root);
		doc.appendChild(root);
		return doc;
	} 
	
	public void export(Card c, Document doc, Element elem) throws DOMException, ParserConfigurationException, DataException  {
		if (c.getId() == null) {
		    logger.warn("Card is has not Id");
		    return;
		}

		    Element root = doc.createElement("card");
		    root.setAttribute("id", String.valueOf(c.getId().getId()));
		    root.setAttribute("template", String.valueOf(c.getTemplate().getId()));
		    /*final String templateName = c.getTemplateName();
		    if(templateName != null) {
		    	root.setAttribute("templateName", templateName);
		    }*/
		    root.setAttribute("state", String.valueOf(c.getState().getId()));
		    final String fileName = c.getFileName();
		    if(fileName != null) {
		    	root.setAttribute("fileName", fileName);
		    }
		    final ObjectId parentId = c.getParent();
		    if(parentId != null) {
		    	root.setAttribute("parent", String.valueOf(parentId.getId()));
		    }
		    final String url = c.getUrl();
		    if(url != null) {
		    	root.setAttribute("url", url);
		    }
		    elem.appendChild(root);
		    
		    Iterator i = c.getAttributes().iterator();
		    Element elemBlocks = doc.createElement("blocks");
			root.appendChild(elemBlocks);
		    while (i.hasNext()) {
			TemplateBlock block = (TemplateBlock) i.next();
			Element elemBlock = doc.createElement("block");			
			
			Element elemAttrs = doc.createElement("attributes");
			
			Iterator j = block.getAttributes().iterator();
			while (j.hasNext()) {
			    Attribute attr = (Attribute) j.next();
			    AttributeXMLExporter exp = getFactory().getAttributeXMLExporter(doc, c, attr);
			    if (exp == null) {
				logger.warn("Unsupported attribute type: "
					+ attr.getClass());
					continue;
			    }
			    exp.setCexp(this);
			    Object obj = exp.export();
			    if(obj == null) {
			    	continue;
			    }
			    if(obj instanceof Element) {
			    	elemAttrs.appendChild((Element) obj);
			    }
			    
			}
			if(elemAttrs.hasChildNodes()) {
				elemBlock.appendChild(elemAttrs);
			}
			if(elemBlock.hasChildNodes()) {
				final String blockCode = (String) block.getId().getId();
				elemBlock.setAttribute("code", blockCode);
				/*final String blockName = block.getName();
				if(blockName != null) {
					elemBlock.setAttribute("name", blockName);
				}*/
				elemBlocks.appendChild(elemBlock);
			}
		}
		exportActionLog(c, doc, root);
		exportHistory(c, doc, root);
	}
	
	@SuppressWarnings("unchecked")
	private void exportActionLog(Card c, Document doc, Element elem) throws DataException {
		
		Element actionLog = doc.createElement("actionLog");
		GetActionLog action = new GetActionLog();
		action.setCardId(c.getId());
		List<ActionLogHist> list = (List<ActionLogHist>) getService().doAction(action);
		for(Iterator<ActionLogHist> it = list.iterator(); it.hasNext();) {
			ActionLogHist al = it.next();
			Element act = doc.createElement("action");
			String str = al.getActionCode();
		    if(str != null) {
		    	act.setAttribute("code", str);
		    }
		    Date d = al.getLogDate();
		    if(d != null) {
		    	act.setAttribute("logDate", String.valueOf(d.getTime()));
		    }
		    ObjectId obj = al.getActorId();
		    if(obj != null && obj.getId() != null) {
		    	act.setAttribute("actorId", String.valueOf(obj.getId()));
		    }
		    str = al.getIpAddress();
		    if(str != null) {
		    	act.setAttribute("ipAddress", str);
		    }
		    obj = al.getCardId();
		    if(obj != null && obj.getId() != null) {
		    	act.setAttribute("cardId", String.valueOf(obj.getId()));
		    }
		    obj = al.getTemplateId();
		    if(obj != null && obj.getId() != null) {
		    	act.setAttribute("templateId", String.valueOf(obj.getId()));
		    }
		    obj = al.getBlockCode();
		    if(obj != null && obj.getId() != null) {
		    	act.setAttribute("blockCode", String.valueOf(obj.getId()));
		    }
		    obj = al.getAttributeCode();
		    if(obj != null && obj.getId() != null) {
		    	act.setAttribute("attrCode", String.valueOf(obj.getId()));
		    }
		    obj = al.getPersonId();
		    if(obj != null && obj.getId() != null) {
		    	act.setAttribute("personId", String.valueOf(obj.getId()));
		    }
		    obj = al.getDelegateUserId();
		    if(obj != null && obj.getId() != null) {
		    	act.setAttribute("delegateId", String.valueOf(obj.getId()));
		    }
		    if(al.getActionLogId() != null) {
		    	act.setAttribute("action_log_id", al.getActionLogId().toString());
		    }
		    actionLog.appendChild(act);
		}
		elem.appendChild(actionLog);
	}
	
private void exportHistory(Card c, Document doc, Element elem) throws DataException {
		
		Element cardHistory = doc.createElement("cardHistory");
		GetCardVersionHistory action = new GetCardVersionHistory();
		action.setCardId(c.getId());
		List<CardVersionHist> list = (List<CardVersionHist>) getService().doAction(action);
		for(Iterator<CardVersionHist> it = list.iterator(); it.hasNext();) {
			CardVersionHist cv = it.next();
			Element ver = doc.createElement("cardVersion");
			Long l = cv.getVersionId();
		    if(l != null) {
		    	ver.setAttribute("versionId", String.valueOf(l));
		    }
		    Date d = cv.getVersionDate();
		    if(d != null) {
		    	ver.setAttribute("versionDate", String.valueOf(d.getTime()));
		    }
		    ObjectId obj = cv.getParentId();
		    if(obj != null && obj.getId() != null) {
		    	ver.setAttribute("parentCardId", String.valueOf(obj.getId()));
		    }
		    obj = cv.getState();
		    if(obj != null && obj.getId() != null) {
		    	ver.setAttribute("statusId", String.valueOf(obj.getId()));
		    }
			String str = cv.getFileName();
		    if(str != null) {
		    	ver.setAttribute("fileName", str);
		    }
		    str = cv.getUrl();
		    if(str != null) {
		    	ver.setAttribute("url", str);
		    }
			l = cv.getActionLogId();
		    if(l != null) {
		    	ver.setAttribute("action_log_id", l.toString());
		    }
		    
		    
		    for(Iterator<AttributeValueHist> it1 = cv.getAvh().iterator(); it1.hasNext();) {
		    	AttributeValueHist avh = it1.next();
		    	Element hist = doc.createElement("avh");
		    	obj = avh.getAttributeCode();
			    if(obj != null && obj.getId() != null) {
			    	hist.setAttribute("attr", String.valueOf(obj.getId()));
			    }
			    l = avh.getNumberValue();
			    if(l != null) {
			    	hist.setAttribute("numberValue", String.valueOf(l));
			    }
			    str = avh.getStringValue();
			    if(str != null) {
			    	hist.setAttribute("stringValue", str);
			    }
			    d = avh.getDateValue();
			    if(d != null) {
			    	hist.setAttribute("dateValue", String.valueOf(d.getTime()));
			    }
			    l = avh.getValueId();
			    if(l != null) {
			    	hist.setAttribute("valueId", String.valueOf(l));
			    }
			    str = avh.getAnotherValue();
			    if(str != null) {
			    	hist.setAttribute("anotherValue", str);
			    }
			    str = avh.getLongBinaryValue();
			    if(str != null) {
			    	hist.setAttribute("longBinaryValue", str);
			    }
			    
			    ver.appendChild(hist);
		    }
		    
		    cardHistory.appendChild(ver);
		}
		
		elem.appendChild(cardHistory);
	}

}
