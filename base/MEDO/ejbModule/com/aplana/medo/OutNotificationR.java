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
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

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
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.medo.RecordOfFiles.OutcomeExportPutFiles;
import com.aplana.medo.cards.AdditionOutcomeNotificationXML;
import com.aplana.medo.cards.CardException;
import com.aplana.medo.cards.Department;
import com.aplana.medo.cards.Notification;
import com.aplana.medo.cards.Organization;
import com.aplana.medo.cards.PersonInternal;
import com.aplana.medo.cards.Sender;
import com.aplana.medo.cards.StateMedoCard;

/**
 * @author PPanichev
 * 
 */
public class OutNotificationR {

	final Log logger = LogFactory.getLog(getClass());

	private String outFolderExport = null;
	private Transformer transformerNotification = null;
	private String transformerPath = null;
	private Properties options = null;
	private final String report = "reportSent.report";
	private final String secretary = "executorAssigned.secretary";
	private final String manager = "executorAssigned.manager";
	private final String executor = "executorAssigned.executor";
	private boolean _double = false;

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
		this.options = options;
	}

	/**
	 * @param properties
	 *                the properties to set
	 */
	public void setProperties(Properties properties) {
		AdditionOutcomeNotificationXML.setProperties(properties);
	}

	public void setDouble(boolean doubleXML) {
		this._double = doubleXML;
	}

	public void process() throws CardException {
		Collection<Card> cards_id = Notification.findCards();

		// ���� �� ��������� ��������
		if ((cards_id != null) && (cards_id.size() != 0)) {
			for (Card card_itr : cards_id) {
				final List<String> addressees = new ArrayList<String>();
				Exception er_rec = null;
				Notification notifiCard = null;
				try {
					notifiCard = new Notification(card_itr.getId(), options);
				} catch (Exception eN) {
					throw new CardException("jbr.medo.OutNotificationR.notCreate.notifiCard.", eN);
				}
				final GregorianCalendar time_current = new GregorianCalendar();
				try {
					final List<String> files = new ArrayList<String>();
					Card card = null;
					card = notifiCard.getCard();
					final long itr_proc_L = 3600000 * Long.valueOf(notifiCard.getItrPeriod());

					if (time_current.getTimeInMillis() >= notifiCard.getLastDate().getTime() + itr_proc_L) 
					{
						final ObjectId senderId = notifiCard.getSenderId();
						if (senderId == null) 
								throw new CardException("jbr.medo.OutNotificationR.Sender.isNull");
						final Sender sender = new Sender(senderId);
						String addressMEDO = sender.getAddressMEDO();
						String uuid = sender.getUUID();
						//String overheadOrgName = null;
						Sender overheadOrg = null;
						//final Boolean mailIs = GenericValidator.isEmail(mail);
						if (addressMEDO == null || "".equals(addressMEDO.replaceAll(" ", "")) || uuid == null || "".equals(uuid.replaceAll(" ", ""))) {
						    ObjectId overheadOrgId = sender.getOverheadOrganization();
						    
						    Boolean isClMedo = false;
						    while (overheadOrgId != null && (!isClMedo || addressMEDO == null || "".equals(addressMEDO.replaceAll(" ", "")) || uuid == null || "".equals(uuid.replaceAll(" ", "")))) {
							try {
							    overheadOrg = new Sender(overheadOrgId);
							    addressMEDO = overheadOrg.getAddressMEDO();
							    uuid = overheadOrg.getUUID();
							    isClMedo = overheadOrg.getClientMEDO();
							    //overheadOrgName = overheadOrg.getFullName();
							    overheadOrgId = overheadOrg.getOverheadOrganization();
							} catch(Exception e) {
							    logger.error("jbr.medo.OutNotificationR 'while' for Overhead Organization", e);
							}
						    }
						    if (!isClMedo) 
							throw new MedoException("jbr.medo.exporter.incorrectDataOrganization No overhead senders with parameter: ClientMEDO = Yes");
						    if (addressMEDO == null || "".equals(addressMEDO.replaceAll(" ", ""))) 
							throw new MedoException("jbr.medo.exporter.incorrectAddressee No overhead senders with non-empty parameter: addressMEDO");
						    if (uuid == null || "".equals(uuid.replaceAll(" ", "")))
							throw new MedoException("jbr.medo.exporter.incorrectDataOrganization No overhead senders with non-empty parameter: UUID");
						}
						addressees.add(addressMEDO);
						final String notifiCardUID = notifiCard.getNotificationCardUID();
						Document xml = CardExchangeUtils.getCardXML(card); // ��������
						// ���
						// � �������
						// JBR
						// // ��������� ���� �������� /////
						final DateFormat suffix_date_notification 
								= new SimpleDateFormat("yyyy-MM-dd");
						final DateFormat suffix_time_notification 
								= new SimpleDateFormat( "HH:mm:ss");
						final String date_notification 
								= suffix_date_notification.format(time_current.getTime());
						final String time_notification 
								= suffix_time_notification .format(time_current.getTime());
						// /****/////
						final String date_time_notification 
								= date_notification + "T" + time_notification;
						xml = (new AdditionOutcomeNotificationXML(xml)).additionTimeToXml(date_time_notification);
						xml = (new AdditionOutcomeNotificationXML(xml)).additionHeaderUIDToXml();
						if (overheadOrg != null) xml = (new AdditionOutcomeNotificationXML(xml)).additionOverheadSenderToXml(overheadOrg.getFullName(), overheadOrg.getId());
						if (notifiCard.getTemplateId().equals(Notification.TEMPLATE_NOTIFICATION_ORDER_ACCEPTED_ID.getId().toString())) 
						{
							final ObjectId pis = notifiCard.getPersonInternalSecretaryId();
							xml = additionPersonInternalAccept(xml, pis, this.secretary);
							final ObjectId pim = notifiCard.getPersonInternalManagerId();
							xml = additionPersonInternalAccept(xml, pim, this.manager);
							final ObjectId pie = notifiCard.getPersonInternalExecutorId();
							xml = additionPersonInternalAccept(xml, pie, this.executor);

							/*ObjectId de = notifiCard.getDepartmentExecutorId();
							if (de != null) {
								Department departmentCard = null;
								departmentCard = new Department(de);
								xml = (new AdditionOutcomeNotificationXML(xml)).additionDepartmentExecutorToXml(departmentCard);
			    				} else {
								logger.warn("jbr.medo.OutNotificationR.DepartmentExecutorId is null.");
			    				}*/
							} else {
								if (notifiCard.getTemplateId().equals(Notification.TEMPLATE_NOTIFICATION_REPORT_SENT_ID.getId().toString())) {  

								ObjectId pir = notifiCard.getPersonInternalReportId();
								xml = additionPersonInternalSent(xml, pir);
							}
						}
						if (_double) {
							// ����������� xml
							OutcomeExportPutFiles pfOutcome_xml = new OutcomeExportPutFiles(
									outFolderExport + "/../doubleOUT/",
	        							notifiCardUID + "_xmlJBRNotifi");
							pfOutcome_xml.putFile(xml); // �����������
							// ����
							//***//
						}
						final Document xmlNotification = transformMEDO(xml); // �����������
						// � �������
						// MEDO
						final OutcomeExportPutFiles pfOutcome 
								= new OutcomeExportPutFiles(outFolderExport, notifiCardUID);
						final File PutFile = pfOutcome.putFile(xmlNotification); // �����������
						// ����
						if (_double) {
							// /********////// TEST �������� � ������������ �����,
							// ���� �� ���������� ������
							try {
								logger
								.warn("jbr.medo.OutNotificationR double out notification begin...");
								final OutcomeExportPutFiles pfOutcome_double 
									= new OutcomeExportPutFiles(outFolderExport + "/../doubleOUT/", notifiCardUID);
								pfOutcome_double.putFile( xmlNotification);
								logger.warn("jbr.medo.OutNotificationR double out notification end.");
							} catch (Exception exc_par) {
								logger
								.error(
										"jbr.medo.OutNotificationR. Put double file out notification failed !",
										exc_par);
							}
							// //******//////
						}
						// ���������� ��� ����������� (����������������/������������������)
						//int reg = 1;
						/*if ((notifiCard.getTemplateId()).equals("")) {
			    reg = 0;
			}*/
						files.add(PutFile.getName());
						String subject = "";

						try {
							subject = subject.concat(notifiCard.getTheme());
							subject = subject.concat(" � ");
							subject = subject.concat(notifiCard.getRegNumber());
							subject = subject.concat(" �� ");
							subject = subject.concat(date_notification);
						} catch (Exception e_attr) {
							logger.error(
									"jbr.medo.OutNotificationR.attributeLetter.notInitialized",
									e_attr);
						}

						String namePutFileParent = PutFile.getParentFile().getName();
						createLetter(addressees, files, subject, 
								new File(
										outFolderExport + "/" + namePutFileParent,
										namePutFileParent + ".ini"));
						if (_double) {
							// ** TEST �������� ������ � ������������ �����**/
							try {
								logger
								.warn("jbr.medo.OutNotificationR double letter out notification begin...");
								createLetter(addressees, files, subject, new File(
										outFolderExport + "/../doubleOUT/"
										+ namePutFileParent,
										namePutFileParent + ".ini"));
								logger
								.warn("jbr.medo.OutNotificationR double letter out notification end.");
							} catch (Exception exc_letter) {
								logger
								.error(
										"jbr.medo.OutNotificationR. Put double file letter out notification failed !",
										exc_letter);
							}
							// /****////
						}
					} else {
					    logger.warn("Current time < last date from: " + notifiCard.getId());
					    continue;
					}
				} catch (Exception ex_for) {
					logger.error(
							"jbr.medo.OutNotificationR.proccessout.errorcard_itr, card_id: "
							+ card_itr.getId() + "; ", ex_for);
					er_rec = ex_for;
					final StackTraceElement[] st = Thread.currentThread().getStackTrace();
					for (StackTraceElement ste : st) {
						// StackTraceElement ste = st[st.length-1];
						// StackTraceElement ste =
						// Thread.currentThread().getStackTrace()[1];
						logger.error("\r\n jbr.medo.OutNotificationR.ex_for.codePoint: "
								+ ste.getFileName()
								+ ":"
								+ ste.getClassName()
								+ ":"
								+ ste.getMethodName()
								+ ":"
								+ ste.getLineNumber() + "\r\n");
					}
				}
				/**/
				// ��������� ��������� ��������
				finally {
					try {
						notifiCard.setLastRepeat( notifiCard.getLastRepeat() + 1);
					} catch (Exception exLR) {
						logger.error(
								"jbr.medo.OutNotificationR.error.setLastRepeat",
								exLR);
					}
					try {
						notifiCard.setLastDate(time_current.getTime());
					} catch (Exception exLD) {
						logger.error(
								"jbr.medo.OutNotificationR.error.setLastDate",
								exLD);
					}
					if (er_rec == null) {
						try {
							notifiCard.byPrepareSent();
							logger.info("jbr.medo.OutNotificationR. ������� ������� ������ ��������: �������� = "
									+ notifiCard.getLastRepeat());
						} catch (Exception e) {
							logger.error(
									"jbr.medo.OutNotificationR. �� ������� �������� ������ ��� ��������: "
									+ notifiCard.getId(), e);
						}
						try {
							notifiCard.setSendDate(time_current.getTime());
						} catch (Exception exSD) {
							logger.error(
									"jbr.medo.OutNotificationR.error.setSendDate",
									exSD);
						}
					} else {
						logger.error("jbr.medo.OutNotificationR. ��������� ������� ������ ��������: �������� = "
								+ notifiCard.getLastRepeat());

						// / ������ ����� �������� "������ �������� �
						// ����", ��������� ����.
						try {
							final StateMedoCard smc = new StateMedoCard();
							smc.setIterator(notifiCard.getLastRepeat());
							smc.setLastTime(notifiCard.getLastDate());
							smc.setResultProcessing(er_rec.toString());
							final Long idMedoState = smc.createCard();
							notifiCard.addLinkedId(idMedoState);
						} catch (CardException ce) {
							logger
							.error(
									"jbr.medo.OutNotificationR.notCreateStateMedo",
									ce);
						} catch (Exception exc) {
							logger.error(
									"jbr.medo.OutNotificationR.createStateMedo.exception",
									exc);
						}
						// /****///

						if (notifiCard.getLastRepeat() == notifiCard.getCountRepeat()) {
							try {
								notifiCard.byPrepareNotSent();
							} catch (Exception ex) {
								logger.error(
										"jbr.medo.OutNotificationR.error.byPrepareNotSent",
										ex);
							}
						}
					}
				}
				/**/
			}
		}
	}

	private Document additionPersonInternalAccept(Document xml, ObjectId pi, String code) throws DataException, ServiceException {
		if (pi != null) {
			final PersonInternal personInternalCard = new PersonInternal(pi);
			xml = (new AdditionOutcomeNotificationXML(xml)).additionPersonInternalToXml(personInternalCard, code);
			xml = (new AdditionOutcomeNotificationXML(xml)).additionContactInfoPersonInternalToXml(personInternalCard, code);
			xml = additionOrganization(xml, personInternalCard.getOrganizationId(), code);
			xml = additionDepartment(xml, personInternalCard.getDepartmentId(), code);
		} else {
			logger.warn(String.format("jbr.medo.OutNotificationR.%s.PersonInternal is null.", code));
		}
		return xml;
	}

	private Document additionPersonInternalSent(Document xml, ObjectId pi) throws DataException, ServiceException {
		final String code = this.report;
		if (pi != null) {
			final PersonInternal personInternalCard = new PersonInternal(pi);
			xml = (new AdditionOutcomeNotificationXML(xml)).additionPersonInternalToXml(personInternalCard, code);
			xml = additionOrganizationWithDescription(xml, personInternalCard.getOrganizationId(), code);
			xml = additionDepartment(xml, personInternalCard.getDepartmentId(), code);
		} else {
			logger.warn(String.format("jbr.medo.OutNotificationR.%s.PersonInternal is null.", code));
		}
		return xml;
	}

	private Document additionOrganization(Document xml, ObjectId org, String code) throws DataException, ServiceException {
		if (org != null) {
			Organization organizationCard = null;
			organizationCard = new Organization(org);
			xml = (new AdditionOutcomeNotificationXML(xml)).additionOrganizationToXml(organizationCard, code);
		} else {
			logger.warn(String.format("jbr.medo.OutNotificationR.%s.Organization is null.", code));
		}
		return xml;
	}
	
	private Document additionOrganizationWithDescription(Document xml, ObjectId org, String code) throws DataException, ServiceException {
		if (org != null) {
			Organization organizationCard = null;
			organizationCard = new Organization(org);
			xml = (new AdditionOutcomeNotificationXML(xml)).additionOrganizationWithDescriptionToXml(organizationCard, code);
		} else {
			logger.warn(String.format("jbr.medo.OutNotificationR.%s.Organization is null.", code));
		}
		return xml;
	}

	private Document additionDepartment(Document xml, ObjectId dep, String code) throws DataException, ServiceException {
		if (dep != null) {
			Department departmentCard = new Department(dep);
			xml = (new AdditionOutcomeNotificationXML(xml)).additionDepartmentToXml(departmentCard, code);
		} else {
			logger.warn(String.format("jbr.medo.OutNotificationR.%s.Department is null.", code));
		}
		return xml;
	}

	private Document transformMEDO(Document XML) {
		TransformerFactory tFactory = TransformerFactory.newInstance();

		try {
			final String URI = Portal.getFactory().getConfigService().getConfigFileUrl(transformerPath).toURI().toString();
			transformerNotification = tFactory.newTransformer(new StreamSource(
					Portal.getFactory().getConfigService().loadConfigFile(
							transformerPath), URI));
		} catch (TransformerConfigurationException ex) {
			logger.error("jbr.medo.OutNotificationR. Error during transformer creation", ex);
		} catch (IOException ex) {
			logger.error("jbr.medo.OutNotificationR. Error during template file for transformer reading",
					ex);
		} catch (URISyntaxException ex) {
			logger.error("jbr.medo.OutNotificationR. Error during URI reading", ex);
		}
		final ByteArrayOutputStream source = XmlUtils.serialize(XML);
		final InputStream inputStream = new ByteArrayInputStream(source.toByteArray());
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			transformerNotification.transform( 
					new StreamSource(inputStream),
					new StreamResult(out));
		} catch (TransformerException ex) {
			logger.error("jbr.medo.OutNotificationR. Error during transformation of Document: "
					+ XML.toString());
			return null;
		}
		final Document MEDODocument = XmlUtils.createDOMDocument(out);
		return MEDODocument;
	}

	public void createLetter(List<String> addressees, List<String> files,
			String subject, File outFile)
		throws CardException 
	{
		final  TicketGenerator generator = new TicketGenerator();
		generator.setSent(false);
		generator.setRead(false);
		generator.setSignature(true);
		generator.setEncrypting(false);

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
			throw new CardException(
					"jbr.medo.OutNotificationR.notCreateLetter (letterFileStream)",
					ex);
		} finally {
			if (letterFileStream != null) {
				try {
					letterFileStream.close();
				} catch (IOException ex) {
					throw new CardException(
							"jbr.medo.OutNotificationR.notCreateLetter (letterFileStream.close)",
							ex);
				}
			}
		}
	}
}
