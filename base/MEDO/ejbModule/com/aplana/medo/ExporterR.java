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
package com.aplana.medo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.cardexchange.xml.CardExchangeUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.medo.FormatsManager.Format;
import com.aplana.medo.RecordOfFiles.AttachedOutcomeExportFiles;
import com.aplana.medo.RecordOfFiles.OutcomeExportPutFiles;
import com.aplana.medo.cards.AdditionOutcomeExportXML;
import com.aplana.medo.cards.CardException;
import com.aplana.medo.cards.ElementListMailing;
import com.aplana.medo.cards.Imported;
import com.aplana.medo.cards.InBox;
import com.aplana.medo.cards.Organization;
import com.aplana.medo.cards.OrganizationCardHandler;
import com.aplana.medo.cards.OutcomeExportCardHandler;
import com.aplana.medo.cards.PersonInternal;
import com.aplana.medo.cards.RecipientExportCardHandler;
import com.aplana.medo.cards.StateMedoCard;

public class ExporterR {

	ObjectId SIGNATORY_ATTR_ID = ObjectId.predefined(PersonAttribute.class, "jbr.outcoming.signatory");

	Log logger = LogFactory.getLog(getClass());

	private Transformer transformerMEDO = null;
	private String outFolderExport = null;
	private String transformerPath = null;
	private boolean _double = false;
	private Properties prop = null; // Temp
	private boolean sendSourceAttachment;

	private DataServiceFacade serviceBean;

    /**
     * @param transformerPath
     *                the transformerPath to set
     */
    public void setTransformerPath(String transformerPath) {
	this.transformerPath = transformerPath;
    }

    /**
     * @param outFolderExport
     *                the outFolderExport to set
     */
    public void setOutFolderExport(String outFolderExport) {
	this.outFolderExport = outFolderExport;
    }

    /**
     * @param options
     *                the options to set
     */
    public void setOptions(Properties options) {
	ElementListMailing.setOptions(options);
    }

    /**
     * @param properties
     *                the properties to set
     */
    public void setProperties(Properties properties) {
	AdditionOutcomeExportXML.setProperties(properties);
	prop = properties; // Temp
    }

    public void setDouble(boolean doubleXML) {
	this._double = doubleXML;
    }

	public void setSendSourceAttachment(boolean sendSourceAttachment) {
		this.sendSourceAttachment = sendSourceAttachment;
	}

    public void proccessOUT() throws DataException, CardException {

	Collection<Card> cards_id = ElementListMailing.findCards();

	// ���� �� ��������� ��������
	if ((cards_id != null) && (cards_id.size() != 0)) {

	    for (Card card_itr : cards_id) {
		Exception er_rec = null;
		Boolean f_suc = true;
		try {
		List<String> addressees = new ArrayList<String>();
		List<String> files = new ArrayList<String>();
		Card card = null;
		ElementListMailing elmCard = new ElementListMailing(card_itr.getId());
		card = elmCard.getCard();

		GregorianCalendar time_current = new GregorianCalendar();
		Long itr_proc_L = 3600000 * Long.valueOf(elmCard.getItrPeriod());

		if (time_current.getTimeInMillis() >= elmCard.getLastDate().getTime()
			+ itr_proc_L) {

		    String id_card_s = elmCard.getId(); // ID
			try {
	
				ObjectId[] recipientIds = elmCard.getRecipientAttribute().getIdsArray();
				if (recipientIds == null) {
					throw new CardException("jbr.medo.exporter.incorrectAddressee recipientIds is null");
				}
	
				//for (ObjectId recipientId : recipientIds) {
				final ObjectId recipientId = recipientIds[0];
			    	if (recipientId == null) {
					throw new CardException(
					    "jbr.medo.exporter.incorrectAddressee recipientId is null");
				}
				final RecipientExportCardHandler recipient = new RecipientExportCardHandler(recipientId);
				final Card recipientCard = recipient.getCard();
				if (!recipientCard.getTemplate().equals(
				    OrganizationCardHandler.TEMPLATE_ID)) {
					throw new MedoException("jbr.medo.exporter.incorrectAddressee");
				}
	
				String addressMEDO = recipient.getAddressMEDO();
				String uuid = recipient.getUUID();
	
				if (uuid == null || "".equals(uuid.trim())) {
					// ������ ����� �������� "������ �������� � ����", ��������� ����.
					String errorMessage = new MedoException("jbr.medo.exporter.missingRecipientUUID").getMessage();
					logger.error(errorMessage);
					createStateCard(errorMessage, elmCard);
					elmCard.byPrepareNotSent();
					return;
				}
	
				if (!recipient.getClientMEDO()) {
					throw new CardException("jbr.medo.exporter.incorrectclientMEDO recipient not client MEDO");
				}
	
				//final Boolean mailIs = GenericValidator.isEmail(mail);
				if (addressMEDO == null || "".equals(addressMEDO.replaceAll(" ", ""))) {
				    ObjectId overheadOrgId = recipient.getOverheadOrganization();
				    RecipientExportCardHandler overheadOrg = null;
				    Boolean isClMedo = false;
				    while (overheadOrgId != null && (!isClMedo || addressMEDO == null || "".equals(addressMEDO.replaceAll(" ", "")) || uuid == null || "".equals(uuid.replaceAll(" ", "")))) {
					try {
					    overheadOrg = new RecipientExportCardHandler(overheadOrgId);
					    addressMEDO = overheadOrg.getAddressMEDO();
					    uuid = overheadOrg.getUUID();
					    isClMedo = overheadOrg.getClientMEDO();
					    overheadOrgId = overheadOrg.getOverheadOrganization();
					} catch(Exception e) {
					    logger.error("jbr.medo.ExporterR 'while' for Overhead Organization", e);
					}
				    }
				    if (!isClMedo)
					throw new MedoException("jbr.medo.exporter.incorrectDataOrganization No overhead recipients with parameter: ClientMEDO = Yes");
				    if (addressMEDO == null || "".equals(addressMEDO.replaceAll(" ", "")))
					throw new MedoException("jbr.medo.exporter.incorrectAddressee No overhead recipients with non-empty parameter: addressMEDO");
				    if (uuid == null || "".equals(uuid.replaceAll(" ", "")))
					throw new MedoException("jbr.medo.exporter.incorrectDataOrganization No overhead recipients with non-empty parameter: UUID");
				}
	
				addressees.add(addressMEDO);
				final String fullNameRecipient = recipient.getRecipientFullNameAttribute();
	
				Card outcomeCard = null;
				try {
				    outcomeCard = OutcomeExportCardHandler.
					findCard(elmCard.getFoundationDocAttribute().getLinkSource(), card.getId());
				} catch(Exception ex_oc) {
				    logger.error("jbr.medo.exporter.notFound.outcomeCard. Card Element list mailing: " + id_card_s, ex_oc);
				    er_rec = ex_oc;
				    f_suc = false;
				    continue; // ���� �� ������� �� ������ ��������� ���������, �������� ������� ��������� "�������� ������ ��������"
				}
	
				Format medoFormat = FormatsManager.instance().resolve(outcomeCard);
	
				Document xmlMEDO = null;
	            final OutcomeExportCardHandler oechCard = new OutcomeExportCardHandler(outcomeCard.getId(), sendSourceAttachment);
	            AttachedOutcomeExportFiles downloadFiles = new AttachedOutcomeExportFiles(OutcomeExportPutFiles.NAME_FILE);
				switch (medoFormat) {
				case MEDO:
					if (oechCard.isDsp()) {
						createStateCard(getLocaleMessage("medo.export.dsp", "Document was not sent because it has DSP flag"), elmCard);
						elmCard.byPrepareNotSent();
						continue;
					}
	
					Document xml = CardExchangeUtils.getCardXML(outcomeCard); // �������� ��� � ������� JBR
					xml = (new AdditionOutcomeExportXML(xml)).additionXml(elmCard.getUid()); //��������� uid'�
					xml = (new AdditionOutcomeExportXML(xml)).addOrganizationToXml(fullNameRecipient);
	
					try {
						//���� ����������� ���������� ��� �� �������� �� ���� xml
						PersonAttribute personAttr =  outcomeCard.getAttributeById(SIGNATORY_ATTR_ID);
						if (personAttr != null && personAttr.getPerson() != null) {
							PersonInternal person = new PersonInternal(personAttr.getPerson().getCardId());
							Organization org = new Organization(person.getOrganizationId());
							if (org != null) {
								xml = (new AdditionOutcomeExportXML(xml)).addSignatoryOrgToXml(org.getFullName());
							}
						}
					} catch (Exception ex) {
						logger.warn("Could not retrieve signatory's organization", ex);
					}
	
					// ��������� ���������� �� ���������������� ��������� � ���������, ������: ��������������� �������� - �������� - ���������;
					try {
						Long id_inbox = null;
						TypedCardLinkAttribute rel_doc = oechCard.getRelatedDoc();
						if (rel_doc != null) {
							//rel_doc.getIdsArray();
							HashMap<Long, Long> types = (HashMap<Long, Long>)rel_doc.getTypes();
							if (types != null && !types.isEmpty()) {
								Set<Long> keySetCard = types.keySet();
								Long valueTypeLink = null;
								for(Long keyCard : keySetCard) {
									valueTypeLink = types.get(keyCard);
									logger.info("List typelink: \r\n");
									if (
											valueTypeLink != null &&
											(
												valueTypeLink.equals(OutcomeExportCardHandler.TYPE_LINK_INRESP.getId()) ||
												valueTypeLink.equals(OutcomeExportCardHandler.TYPE_LINK_RESP.getId()) ||
												valueTypeLink.equals(OutcomeExportCardHandler.TYPE_LINK_EXEC.getId()) ||
												valueTypeLink.equals(OutcomeExportCardHandler.TYPE_LINK_LINKED.getId()) ||
												valueTypeLink.equals(OutcomeExportCardHandler.TYPE_LINK_INLIKED.getId()) ||
												valueTypeLink.equals(OutcomeExportCardHandler.TYPE_LINK_INEXEC.getId())
											)
										)
									{
										id_inbox = keyCard;
										logger.info("Find inbox document: " + id_inbox + "; Id typelink: " + valueTypeLink);
										break;
									} else
										logger.error("id typelink -> " + valueTypeLink + " not found !;\r\n");
									logger.info("List typelink end. \r\n");
								}
								if (id_inbox != null) {
									logger.debug("Inbox not null.");
									ObjectId card_id_ib = new ObjectId(Card.class, id_inbox);
									InBox ibCard = new InBox(card_id_ib); // ������ ���������
									Card card_inbox = ibCard.getCard();
									if (card_inbox.getTemplate().getId().equals(InBox.TEMPLATE_ID.getId())) { // ���� ��� ��������
										///������� ��������������� �������� (��������) � ����������� UID
										logger.debug("Search imported document ...");
										CardLinkAttribute import_doc = ibCard.getSourcesAttribute();
										ObjectId[] sourcesIds = null;
										if (import_doc != null) {
											logger.debug("Imported document was found.");
												sourcesIds = import_doc.getIdsArray();
										} else
											logger.error("jbr.medo.Exporter.inbox.imported.isNull");
										if (sourcesIds == null || sourcesIds.length == 0) {
												logger.error("jbr.medo.Exporter.original(imported document).isNull");
										} else {
												ObjectId sourcesId = sourcesIds[0];
												logger.debug("Imported document [0]: " + sourcesId);
												Imported imported = new Imported(sourcesId);
												String importUID = imported.getDocUID().getValue();
												logger.debug("Imported document UID: " + importUID);
												if (importUID != null && !"".equals(importUID)) {
													logger.info("phase: begin additionImportedDocUIDToXml");
													xml = (new AdditionOutcomeExportXML(xml)).additionImportedDocUIDToXml(importUID);
													logger.info("phase: end additionImportedDocUIDToXml");
	
													String reg_number_inbox = ibCard.getDocNumber()!= null?ibCard.getDocNumber().getValue():"";
													Date reg_date_inbox = ibCard.getDateRegistration() != null ?ibCard.getDateRegistration().getValue():null;
													logger.debug("Reg number inbox: " + reg_number_inbox + "; Reg date inbox: " + reg_date_inbox);
													if (reg_date_inbox != null && !"".equals(reg_number_inbox) && reg_number_inbox != null) {
														logger.info("phase: begin additionTypeLink");
														xml = (new AdditionOutcomeExportXML(xml)).additionTypeLink(valueTypeLink.toString());
														logger.info("phase: end additionTypeLink");
	
														DateFormat suffix_date_notification = new SimpleDateFormat("yyyy-MM-dd");
														String reg_date_inbox_fmt = suffix_date_notification.format(reg_date_inbox);
														logger.info("phase: begin additionRegDateInBox");
														xml = (new AdditionOutcomeExportXML(xml)).additionRegDateInBox(reg_date_inbox_fmt);
														logger.info("phase: end additionRegDateInBox");
	
														logger.info("phase: begin additionRegNumberInBox");
														xml = (new AdditionOutcomeExportXML(xml)).additionRegNumberInBox(reg_number_inbox);
														logger.info("phase: end additionRegNumberInBox");
													} else
														logger.error("jbr.medo.Exporter.inbox: regdata or regnumber isNull");
												} else
													logger.error("jbr.medo.Exporter.original(imported document).UID.isNull");
										}
									} else {
										logger.error("jbr.medo.Exporter.inbox: not related inbox card !");
									}
								} else
									logger.error("jbr.medo.Exporter.inbox.isNull");
	
							} else
								logger.error("jbr.medo.Exporter.notTypeLink");
						} else
							logger.error("jbr.medo.Exporter.outbox: no related inbox document !");
					} catch(Exception e) {
						logger.error("jbr.medo.Exporter. Error adding attributes from the imported document.", e);
					}
	
					// TEMP
					try
					{
						ObjectId[] linkedFile = oechCard.getLinkedFileCards();
						// �������� ��� ��� ��������
						for(ObjectId idAttach : linkedFile)
						{
							if (idAttach != null)
							{
								String eds = AttachedOutcomeExportFiles.getEds(idAttach);
								AttachedOutcomeExportFiles.addEdsAttribute(eds, prop, xml, idAttach.getId().toString());
							}
						}
					}
					catch(Exception e)
					{
						logger.error("Error getting EDS.", e);
					}
					//
					downloadFiles.setLinkedFileCards(oechCard.getLinkedFileCards());
					downloadFiles.updateDoclinks(xml);
	
					if (_double) {
					// ����������� xml
						try {
							logger.warn("jbr.medo.Exporter Double card JBR begin...");
	        			OutcomeExportPutFiles pfOutcome_xmlJBR = new OutcomeExportPutFiles(
	        				outFolderExport + "/../doubleOUT/", elmCard.getUid()+ "_xmlJBRExport");
	        			pfOutcome_xmlJBR.putFile(xml); // �����������
	        							 // ����
							logger.warn("jbr.medo.Exporter Double card JBR end.");
						} catch(Exception exc_par) {
							logger.error("jbr.medo.Exporter. Put double file JBR failed !", exc_par);
						}
					}
	
					xmlMEDO = transformMEDO(xml); 	// ����������� � ������� MEDO
					if (_double) {
						///********////// TEST �������� � ������������ �����, ���� �� ���������� ������
						try {
							logger.warn("jbr.medo.Exporter Double card begin...");
					    OutcomeExportPutFiles pfOutcome_double = new OutcomeExportPutFiles(outFolderExport + "/../doubleOUT/", elmCard.getUid());
					    pfOutcome_double.putFile(xmlMEDO);
							logger.warn("jbr.medo.Exporter Double card end.");
						} catch(Exception exc_par) {
							logger.error("jbr.medo.Exporter. Put double file failed !", exc_par);
						}
					////******//////
					}
					break;
				case MEDO_OG:
					OGExporter ogExporter = new OGExporter(outcomeCard);
					ogExporter.setServiceBean(serviceBean);
					xmlMEDO = ogExporter.export();
					break;
				case UNDEFINED:
					throw new MedoException("jbr.medo.export.unknownFormat", new Object[] { card.getId() });
				}
	
				OutcomeExportPutFiles pfOutcome = new OutcomeExportPutFiles(outFolderExport, elmCard.getUid()); //������ ��� �������������� �����
	
				if (Format.MEDO.equals(medoFormat)) {
					final String outDir = outFolderExport + "/" + elmCard.getUid();
					///////////// Download attached files ////////////////
	
					downloadFiles.setOutDir(outDir);
					files = downloadFiles.putFiles();
	
				}
				// /////////////////////////////////////////////////////
	
	            File PutFile = pfOutcome.putFile(xmlMEDO); // ����������� ����
				String namePutFileParent = PutFile.getParentFile().getName();
	
				String subject = "";
				try {
				    	DateFormat suffix_date_signing = new SimpleDateFormat("dd.MM.yyyy");
				    	subject = subject.concat(oechCard.getTheme());
				    	subject =  subject.concat(" � ");
				    	subject =  subject.concat(oechCard.getRegNumber());
				    	subject =  subject.concat(" �� ");
					String date_signing = suffix_date_signing.format(oechCard.getDateSigning());
					subject =  subject.concat(date_signing);
				    } catch(Exception e_attr) {
				    logger.error("jbr.medo.attributeLetter.notInitialized", e_attr);
				}
				createLetter(addressees, files, subject, new File(
					outFolderExport + "/" + namePutFileParent, namePutFileParent + ".ini"));
				if (_double) {
					/** TEST �������� ������ � ������������ �����**/
					try {
					    logger.warn("jbr.medo.Exporter double letter begin...");
					    createLetter(addressees, files, subject, new File(
							outFolderExport + "/../doubleOUT/" + namePutFileParent, namePutFileParent + ".ini"));
					    logger.warn("jbr.medo.Exporter double letter end.");
					} catch(Exception exc_letter) {
					    logger.error("jbr.medo.Exporter. Put double file letter failed !", exc_letter);
					}
					///****////
				}

				//��������� �����
				createStateCard(getLocaleMessage("medo.export.successful"), elmCard);
		    } catch (Exception er) {
				logger.error("jbr.medo.er.Exporter: ", er);
				er_rec = er;
				f_suc = false;
				StackTraceElement ste0 = Thread.currentThread().getStackTrace()[2];
				logger.error("\r\n jbr.medo.er.codePoint: " +  ste0.getFileName() + ":" +
				           ste0.getClassName() + ":" +
				           ste0.getMethodName() + ":" +
				           ste0.getLineNumber() + "\r\n");
		    }
		    // ��������� ��������� ��������
		    finally {
//			elmCard.setLastRepeat(elmCard.getLastRepeat() + 1);
//			elmCard.setLastDate(time_current.getTime());
			elmCard.setLastRepeatAndLastDate(elmCard.getLastRepeat() + 1, time_current.getTime());

			if (f_suc) {
			    try {
				elmCard.byPrepareSent();
				logger
					.info("������� ������� ������ ��������: �������� = "
						+ elmCard.getLastRepeat()); // �������
			    } catch (Exception e) {
				logger.error(
					"�� ������� �������� ������ ��� ��������: "
						+ id_card_s, e);
			    }
			} else {
			    logger
				    .error("��������� ������� ������ ��������: �������� = "
					    + elmCard.getLastRepeat()); // �������
			    // / ������ ����� �������� "������ �������� � ����", ��������� ����.
			    String errorMessage = er_rec == null ? "" : er_rec.getMessage();
			    createStateCard(errorMessage, elmCard);
			    if (elmCard.getLastRepeat() == elmCard.getCountRepeat()) { // ����� ���������� ����� == � ���������: -128 �� 127
			    	elmCard.byPrepareNotSent();
			    }
			}
		    }
		} else {
		    logger.warn("Current time < last date from: " + elmCard.getId());
		    continue;
		}
	    	} catch(Exception ex_for) {
	    	logger.error("jbr.medo.proccessout.errorcard_itr, card_id: "+ card_itr.getId() + "; ", ex_for);
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		logger.error("\r\n jbr.medo.ex_for.codePoint: " +  ste.getFileName() + ":" +
		           ste.getClassName() + ":" +
		           ste.getMethodName() + ":" +
		           ste.getLineNumber() + "\r\n");
		continue;
	      }
	    }
	}
    }

	protected void createStateCard(String message, ElementListMailing elmCard) {
		try {
		    StateMedoCard smc = new StateMedoCard();
		    smc.setIterator(elmCard.getLastRepeat());
		    smc.setLastTime(elmCard.getLastDate());
			smc.setResultProcessing(message);
		    Long idMedoState = smc.createCard();
		    elmCard.addLinkedId(idMedoState);
		} catch (CardException ce) {
		    logger.error("jbr.medo.notCreateStateMedo", ce);
		} catch (Exception exc) {
		    logger.error("jbr.medo.createStateMedo.exception", exc);
		}
	}

	private Document transformMEDO(Document XML) {
	TransformerFactory tFactory = TransformerFactory.newInstance();

	try {
	    String URI = Portal.getFactory().getConfigService().getConfigFileUrl(transformerPath).toURI().toString();
	    transformerMEDO = tFactory.newTransformer(new StreamSource(Portal
		    .getFactory().getConfigService().loadConfigFile(
			    transformerPath), URI));
	} catch (TransformerConfigurationException ex) {
	    logger.error("Error during transformer creation", ex);
	} catch (IOException ex) {
	    logger.error("Error during template file for transformer reading",
		    ex);
	} catch (URISyntaxException ex) {
	    logger.error("Error during URI reading", ex);
	}
	ByteArrayOutputStream source = XmlUtils.serialize(XML);
	InputStream inputStream = new ByteArrayInputStream(source.toByteArray());
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	try {
	    transformerMEDO.transform(new StreamSource(inputStream),
		    new StreamResult(out));
	} catch (TransformerException ex) {
	    logger.error("Error during transformation of Document: "
		    + XML.toString());
	    return null;
	}
	Document MEDODocument = XmlUtils.createDOMDocument(out);
	return MEDODocument;
    }

    public void createLetter(List<String> addressees, List<String> files, String subject,
	    File outFile) throws CardException {
	TicketGenerator generator = new TicketGenerator();
	generator.setAutosend(true);
	generator.setEncrypting(false);
	generator.setSignature(true);
	generator.setSent(true);
	generator.setRead(false);
	String subj = "��� ���� ";
	subj = subj.concat(subject);
	generator.setSubject(subj);
	for (String addressee : addressees) {
	    generator.addAddressee(addressee);
	}
	for (String file : files) {
	    generator.addFile(file);
	}

	FileOutputStream letterFileStream = null;
	try {
	    letterFileStream = new FileOutputStream(outFile);
	    letterFileStream.write(new String("").getBytes());
	    generator.serializeWin1251(letterFileStream);
	} catch (IOException ex) {
	    throw new CardException("jbr.medo.notCreateLetter (letterFileStream)",ex);
	} finally {
	    if (letterFileStream != null) {
		try {
		    letterFileStream.close();
		} catch (IOException ex) {
		    throw new CardException("jbr.medo.notCreateLetter (letterFileStream.close)",ex);
		}
	    }
	}
    }

    public void setServiceBean(DataServiceFacade serviceBean) {
    	this.serviceBean = serviceBean;
    }

    private static String getLocaleMessage(String key) {
    	return getLocaleMessage(key, "");
    }

    private static String getLocaleMessage(String key, String defaultMessage) {
    	String localizedMessage = defaultMessage;
		try {
			localizedMessage = ContextProvider.getContext().getLocaleMessage(key);
		} catch (MissingResourceException ex) {
		}
		return localizedMessage;
    }
    

}