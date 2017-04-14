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
package com.aplana.dbmi.jbr.processors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.GetCardsTree;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.action.xml.SearchXmlHelper;
import com.aplana.dbmi.archive.export.CardXMLExporter;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.SaveQueryBase;

public class ExportToXMLCardProcessor extends ProcessCard implements Parametrized {
	
	private static final long serialVersionUID = 1L;
	
	// �������� �� ������� ��������� ��� �������� � ��������� ���������
	private String PARAM_NESTED_CARDS_TEMPLS = "exportableNestedCardTemplates";
	//�������� �� ������� cardlink\backlink, ������� ����� ��������������� � �������� ������������ ������
	private String INCLUDED_LINKS = "includedLinks";
	// ����������� XML ����� � �������� ����������� ��������
	private static final ObjectId ATTR_SAVED_CARD = ObjectId.predefined(CardLinkAttribute.class, "jbr.saved.card");
	private static final ObjectId TEMPLATE_FILE = ObjectId.predefined(Template.class, "jbr.file");
	
	private DataServiceFacade serviceFacade;
	private Set<ObjectId> exportableNestedCardTemplates;
	/**
	 * ������ cardlink\backlink, ������� ����� ��������������� � �������� ������������ ������
	 */
	private Set<ObjectId> includedLinks;

	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException {
		Card card = getCard();
		File file = null;
		try {

			final GetCardsTree getCardsTreeAction= new GetCardsTree();
			getCardsTreeAction.setCardId(card.getId());
			getCardsTreeAction.setTemplates(exportableNestedCardTemplates);
			getCardsTreeAction.setLinkAttrs(includedLinks);
			getCardsTreeAction.setReverse(GetCardsTree.Fields.LINKATTRS, false);
			getCardsTreeAction.setReverse(GetCardsTree.Fields.TEMPLATES, false);
			final List<ObjectId> exportableNestedCard = new ArrayList<ObjectId>();
			Collection<Long> cardIdTree =  execAction(getCardsTreeAction);
			for(Long id: cardIdTree){
				exportableNestedCard.add(new ObjectId(Card.class, id));
			}
			exportableNestedCard.remove(card.getId());
			
			CardXMLExporter ctXML = new CardXMLExporter();
			ctXML.setNestedCardsToExport(exportableNestedCard);
			ctXML.setService(getDataServiceBean());
			Document doc = ctXML.export(card);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer = transformerFactory.newTransformer();
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	        DOMSource source = new DOMSource(doc);
	        
			//������� ����
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
			String fileName = (card.getId() != null && card.getId().getId() != null
					? String.valueOf(card.getId().getId()) + "_"
					: "")
						+ sdf.format(new Date());
			file = File.createTempFile(fileName, ".xml");
			Result result = new StreamResult(file);
	        transformer.transform(source, result);
	        Card fileCard = createFileCard(fileName);
	        attachFileToFileCard(fileName + ".xml", fileCard, file);
	        SaveDoc(card.getId(), card, fileCard);
		} catch (Exception e) {
			throw new DataException(e);
		}finally {
			// delete temporary file
			if(file != null) {
				file.delete();
				if (logger.isDebugEnabled()) {
					logger.debug("temporary file " + file.getAbsolutePath() + " deleted");
				}
			}
		}
		return null;
	}
	
	private DataServiceFacade getDataServiceBean() throws DataException {
		if (this.serviceFacade == null) {
			serviceFacade = new DataServiceFacade();
			serviceFacade.setUser(getSystemUser());
			serviceFacade.setDatabase(getDatabase());
			serviceFacade.setQueryFactory(getQueryFactory());
		}
		return this.serviceFacade;
	}
	
	@Override
	public void setParameter(String name, String value) {

		if (name == null || value == null)
			return;
		if(PARAM_NESTED_CARDS_TEMPLS.equalsIgnoreCase(name)) {
			this.exportableNestedCardTemplates = new HashSet<ObjectId>();
			this.exportableNestedCardTemplates.addAll(ObjectIdUtils.commaDelimitedStringToNumericIds(value, Template.class));
		} else if (INCLUDED_LINKS.equalsIgnoreCase(name)) {
			
			final String[] sIds = value.trim().split("\\s*(?<!\\\\)[;,]\\s*");
			if (sIds.length > 0) {
				includedLinks = new HashSet<ObjectId>(sIds.length);
				for (String s : sIds) {
					if (s == null || s.length() == 0)
						continue;
					ObjectId res = SearchXmlHelper.safeMakeId(s);
					includedLinks.add(res);
					
				}
			}
		} 
		else
			return;
	}

	private Card createFileCard(String fileName) throws Exception {

		CreateCard createCardAction = new CreateCard();
		createCardAction.setTemplate(TEMPLATE_FILE);
		
		Card fileCard = (Card) execAction(createCardAction, getSystemUser());
			
		StringAttribute name = (StringAttribute)fileCard.getAttributeById(Attribute.ID_NAME);
		name.setValue(fileName);
		saveCard(fileCard, getSystemUser());
		try {
			execAction(new UnlockObject(fileCard.getId()));
		} catch (Exception ex) {
			logger.debug("Failed to unlock card: " + fileCard.getId() + "\n" + ex);
		}
		return fileCard;
	}
	
	private void attachFileToFileCard(String fileName, Card c, File file) throws FileNotFoundException, DataException, ServiceException {
		FileInputStream fis = new FileInputStream(file);
		try {
			UploadFile uploadAction = new UploadFile();
			uploadAction.setCardId(c.getId());
			uploadAction.setFileName(fileName);
			uploadAction.setData(fis);
			getDataServiceBean().doAction(uploadAction);
            c = loadCardById(c.getId());
			MaterialAttribute attr = (MaterialAttribute) c.getAttributeById(Attribute.ID_MATERIAL);
			attr.setMaterialName(fileName);
			attr.setMaterialType(MaterialAttribute.MATERIAL_FILE);

			StringAttribute name = (StringAttribute) c.getAttributeById(Attribute.ID_NAME);
			name.setValue(fileName);

			final SaveQueryBase query = getQueryFactory().getSaveQuery(c);
			query.setObject(c);
			getDatabase().executeQuery(getSystemUser(), query);
		}finally {
			IOUtils.closeQuietly(fis);
		}

	}
	
	public void SaveDoc(ObjectId docId, Card docCard, Card fileCard) throws DataException{
		Card targetCard = docCard;
		CardLinkAttribute docLinks = targetCard.getCardLinkAttributeById(ATTR_SAVED_CARD);
		docLinks.addLinkedId(fileCard.getId());
		updateAttribute(docId, docLinks);
	}
	
	/**
	 * ��������� ���������� �������� ��� �������� cardId.
	 * @param cardId
	 * @param attr
	 * @throws DataException 
	 */
	private void updateAttribute(ObjectId cardId, Attribute attr) 
		throws DataException 
	{
		final OverwriteCardAttributes action = new OverwriteCardAttributes();
		action.setCardId(cardId);
		action.setAttributes(Collections.singletonList(attr));
		action.setInsertOnly(true);
		execAction(new LockObject(cardId));
		try {
			execAction(action, getSystemUser());
		} finally {
			execAction(new UnlockObject(cardId));
		}
	}
	
}
