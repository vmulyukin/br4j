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
package com.aplana.dmsi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.aplana.dbmi.action.ImportCardFromXml.ImportCard.Result;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TypeStandard;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dmsi.GOSTImporter.Processor;
import com.aplana.dmsi.action.ImportCardByDelo;
import com.aplana.dmsi.card.CardHandler;
import com.aplana.dmsi.card.handling.CardFacade;
import com.aplana.dmsi.types.AckResult;
import com.aplana.dmsi.types.AcknowledgementType;
import com.aplana.dmsi.types.DocTransfer;
import com.aplana.dmsi.types.DocumentType;
import com.aplana.dmsi.types.File;
import com.aplana.dmsi.types.FileEnumType;
import com.aplana.dmsi.types.Header;
import com.aplana.dmsi.types.HeaderMessageEnumType;
import com.aplana.dmsi.types.ImportedOGDocumentType;
import com.aplana.dmsi.types.LinkDocTransfer;
import com.aplana.dmsi.types.Rubric;
import com.aplana.dmsi.types.SourceDocument;
import com.aplana.dmsi.types.SourceDocumentTwo;
import com.aplana.dmsi.types.SubRubric;
import com.aplana.dmsi.types.delo.ExpansionType;
import com.aplana.dmsi.util.RubricUtils;
import com.aplana.dmsi.util.ServiceUtils;
import com.aplana.dmsi.util.XMLException;
import com.aplana.dmsi.util.XmlUtils;

public class DoImportCardByDelo extends ActionQueryBase implements WriteQuery {

    private static final long serialVersionUID = 1L;
	/**
	 * Identifier of 'DoImportCardByDelo' action to be used in system log
	 */
	public static final String EVENT_ID = "IMPORT_CARD_BY_DELO";

	@Override
	public String getEvent() {
		return EVENT_ID;
	}

	@Override
	public Object processQuery() throws DataException {
		ObjectFactory.startWork(TypeStandard.DELO);
		try {
			ImportCardByDelo action = (ImportCardByDelo) getAction();
			byte[] streamData = action.getStreamData();
			InputStream transformerStream;
			ServiceUtils.clearEventLog();
			try {
				transformerStream = ServiceUtils.readConfig("delo_import_template.xsl");
				Transformer transformer = XmlUtils.createTransformer(transformerStream);
				ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
				transformer.transform(new StreamSource(new ByteArrayInputStream(streamData)), new StreamResult(resultStream));
				streamData = resultStream.toByteArray();

				GOSTImporter importer = new GOSTImporter();
				final DataServiceFacade serviceBean = getDataServiceBean();
				importer.setDataServiceBean(serviceBean);
				importer.setProcessor(new GOSTImporter.Processor() {
					public void preProcess(Header header) {
						DocumentType doc = header.getDocument();
						if (doc == null) {
							return;
						}

						List<DocTransfer> docTransfers = doc.getDocTransfer();
						ExpansionType expansion = (ExpansionType) header.getExpansion();
						List<File> files = expansion.getFiles();
						for (File file : files) {
							if (FileEnumType.PASSPORT.equals(file.getType())) {
								continue;
							}
							DocTransfer docTransfer = new LinkDocTransfer();
							docTransfer.setDescription(file.getDescription());
							docTransfer.setValue(file.getName().getBytes());
							docTransfer.getEds().addAll(file.getEds());
							docTransfers.add(docTransfer);
						}
						if (doc instanceof ImportedOGDocumentType) {
							List<Rubric> rubrics = doc.getRubric();
							for (Rubric expansionRubric : expansion.getRubric()) {
								String rubricCode = expansionRubric.getCode();
								Rubric rubric;

								if (RubricUtils.isRubric(rubricCode)) {
									rubric = new Rubric();
									rubric.setNumber(rubricCode);
								} else if (RubricUtils.isOldStyleRubric(rubricCode)) {
									rubric = new Rubric();
									rubric.setCode(rubricCode);
								} else if (RubricUtils.isSubRubric(rubricCode)) {
									rubric = new SubRubric();
									rubric.setNumber(rubricCode);
								} else {
									//In case of wrong format of Rubric just skip it.
									continue;
								}
								rubric.setValue(expansionRubric.getValue());
								rubrics.add(rubric);
							}
						}
						doc.setTaskList(header.getTaskList());
					}

					public void postProcess(ObjectId cardId) {
					}


				});
				importer.addProcessor(HeaderMessageEnumType.ACKNOWLEDGEMENT, new Processor() {
					private String uuid;

					public void preProcess(Header header) {
						AcknowledgementType ack = header.getAcknowledgement();
						uuid = ack.getMsgId();
						ack.setMsgId(header.getMsgId());
						ack.setAckResultDescription(formatAckResultDescription(ack.getAckResult()));
					}

					private String formatAckResultDescription(List<AckResult> ackResults) {
						StringBuilder descr = new StringBuilder();
						for (AckResult ackResult : ackResults) {
							if (descr.length() > 0) {
								descr.append("\n");
							}
							descr.append(ackResult.getErrorcode() + ": " + ackResult.getValue());
						}
						return descr.toString();
					}

					public void postProcess(ObjectId ackId) throws DMSIException {
						SourceDocument doc = getSourceDocument(uuid);
						if (doc == null) {
							throw new DMSIException(
									"Source document for acknowledgement is not found or several cards have same UUID");
						}
						AcknowledgementType ack = new AcknowledgementType();
						ack.setId(ackId.getId().toString());
						doc.setAcknowledgement(ack);
						CardHandler cardHandler = new CardHandler(serviceBean);
						cardHandler.updateCard(doc);
					}

					private SourceDocument getSourceDocument(String uid) throws DMSIException{
						ObjectId cardId = null;
						SourceDocument document = null;
						cardId = ServiceUtils.getCardIdByUUID(getDataServiceBean(), uid, ObjectId.predefined(StringAttribute.class, "jbr.distributionItem.uuid"));
						if (cardId != null) {
							document = new SourceDocument();
							document.setId(cardId.getId().toString());
							return document;
						}
						cardId = ServiceUtils.getCardIdByUUID(getDataServiceBean(), uid, ObjectId.predefined(TextAttribute.class, "uid"));
						if (cardId!=null) {
							document = new SourceDocumentTwo();
							document.setId(cardId.getId().toString());
							return document;
						}
						return document;
					}
				});

				final long importedDocCardId = action.getImportedDocCardId();
				importer.addProcessor(HeaderMessageEnumType.DOCUMENT, new Processor() {
					public void preProcess(Header header) {
					}

					public void postProcess(ObjectId cardId) throws DMSIException {
						CardFacade cardFacade = new CardFacade(serviceBean, cardId);
						cardFacade.addAttributeValue(ObjectId
								.predefined(CardLinkAttribute.class, "jbr.original.source"), importedDocCardId);
						cardFacade.updateCard();
					}
				});
				importer.importCard(importedDocCardId, streamData);
				Result result = new Result(importer.getImportedCardId(), importer.getPaths());
				ServiceUtils.setLogIntoDatabase(getBeanFactory(), importedDocCardId, getUser(), EVENT_ID);
				return result;
			} catch (IOException ex) {
				throw new DataException(ex);
			} catch (XMLException ex) {
				throw new DataException(ex);
			} catch (TransformerException ex) {
				throw new DataException(ex);
			} catch (GOSTException ex) {
				throw new DataException(ex);
			}
		} finally {
			ObjectFactory.finishWork();
		}
	}

	protected DataServiceFacade getDataServiceBean() {
		DataServiceFacade serviceBean = new DataServiceFacade();
		serviceBean.setUser(getUser());
		serviceBean.setDatabase(getDatabase());
		serviceBean.setQueryFactory(getQueryFactory());
		return serviceBean;
}
}
