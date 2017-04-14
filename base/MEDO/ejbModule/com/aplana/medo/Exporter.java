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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

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
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.cardexchange.xml.CardExchangeUtils;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.medo.cards.CardException;
import com.aplana.medo.cards.CardXMLBuilder;
import com.aplana.medo.cards.DistributionItemCardHandler;
import com.aplana.medo.cards.OrganizationCardHandler;
import com.aplana.medo.cards.StateMedoCard;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

// REFACTORING IS REQUIRED!!!
public class Exporter {

    public static final ObjectId PREPARE_DELIVERY = ObjectId.predefined(
	    CardState.class, "prepareDELIVERY");
    public static final ObjectId SUCCESSFULLY_DELIVERY = ObjectId.predefined(
	    CardState.class, "sent");
    public static final ObjectId PREPARE_SENT = ObjectId.predefined(
	    WorkflowMove.class, "prepare.sent");
    public static final ObjectId ERROR_DELIVERY = ObjectId.predefined(
	    CardState.class, "jbr.distributionItem.notSent");
    public static final ObjectId PREPARE_NOTSENT = ObjectId.predefined(
	    WorkflowMove.class, "prepare.notsent");
    public static final ObjectId COUNT_ATTEMPT_DELIVERY = ObjectId.predefined(
	    IntegerAttribute.class, "countAttemptDELIVERY");
    public static final ObjectId LAST_ATTEMPT_DELIVERY = ObjectId.predefined(
	    IntegerAttribute.class, "lastAttemptDELIVERY");
    public static final ObjectId MODE_DELIVERY = ObjectId.predefined(
	    ListAttribute.class, "jbr.distributionItem.method");
    public static final ObjectId LAST_TIME_DELIVERY = ObjectId.predefined(
	    DateAttribute.class, "lastTimeDELIVERY");
    public static final ObjectId MODE_MEDO = ObjectId.predefined(
	    ReferenceValue.class, "modeMEDO");
    public static final ObjectId CARD_LINK = ObjectId.predefined(
	    CardLinkAttribute.class, "jbr.distributionItem.processing");
    public static final ObjectId DATE_SIGNING = ObjectId.predefined(
	    DateAttribute.class, "jbr.outcoming.signdate");
    public static final ObjectId DATE_CREATED = ObjectId.predefined(
	    DateAttribute.class, "created");
    public static final ObjectId REG_NUMBER = ObjectId.predefined(
	    StringAttribute.class, "regnumber");
    public static final ObjectId SHORT_DESCRIPTION = ObjectId.predefined(
	    TextAttribute.class, "jbr.document.title");

    private static DateFormat SUFFIX_FILE_FORMAT = new SimpleDateFormat(
	    "yyyyMMddHHmmssSSS");
    
    private static DateFormat SUFFIX_DATE_SIGNING = new SimpleDateFormat(
    "dd.MM.yyyy");

    private QueryFactory qFactory = null;
    private Database db = null;

    Log logger = LogFactory.getLog(getClass());

    private Transformer transformerMEDO = null;

    private Properties properties;
    private Properties options;

    private int count;
    private String name_out_file;
    private String out_dir;

    private File exportingFile;
    private String outFolderExport;

    private String transformerPath;

    /**
     * @return the transformerPath
     */
    public String getTransformerPath() {
	return this.transformerPath;
    }

    /**
     * @param transformerPath
     *                the transformerPath to set
     */
    public void setTransformerPath(String transformerPath) {
	this.transformerPath = transformerPath;
    }

    /**
     * @return the outFolderExport
     */
    public String getOutFolderExport() {
	return this.outFolderExport;
    }

    /**
     * @param outFolderExport
     *                the outFolderExport to set
     */
    public void setOutFolderExport(String outFolderExport) {
	this.outFolderExport = outFolderExport;
    }

    /**
     * @return the options
     */
    public Properties getOptions() {
	return this.options;
    }

    /**
     * @param options
     *                the options to set
     */
    public void setOptions(Properties options) {
	this.options = options;
    }

    public String getFileName() {
	return name_out_file;
    }

    public void setFileName(String fn) {
	name_out_file = fn;
    }

    public String getOutDir() {
	return out_dir;
    }

    public void setOutDir(String od) {
	out_dir = od;
    }

    /**
     * @return the properties
     */
    public Properties getProperties() {
	return this.properties;
    }

    /**
     * @param properties
     *                the properties to set
     */
    public void setProperties(Properties properties) {
	this.properties = properties;
    }

    /**
     * @return the qFactory
     */
    public QueryFactory getQFactory() {
	return this.qFactory;
    }

    /**
     * @param factory
     *                the qFactory to set
     */
    public void setQFactory(QueryFactory factory) {
	this.qFactory = factory;
    }

    /**
     * @return the db
     */
    public Database getDb() {
	return this.db;
    }

    /**
     * @param db
     *                the db to set
     */
    public void setDb(Database db) {
	this.db = db;
    }

    /**
     * @return the transformerMEDO
     */
    public Transformer getTransformerMEDO() {
	return this.transformerMEDO;
    }

    /**
     * @param transformerMEDO
     *                the transformerMEDO to set
     */
    public void setTransformerMEDO(Transformer transformerMEDO) {
	this.transformerMEDO = transformerMEDO;
    }

    /**
     * @return the name_out_file
     */
    public String getName_out_file() {
	return this.name_out_file;
    }

    /**
     * @param name_out_file
     *                the name_out_file to set
     */
    public void setName_out_file(String name_out_file) {
	this.name_out_file = name_out_file;
    }

    /**
     * @return the out_dir
     */
    public String getOut_dir() {
	return this.out_dir;
    }

    /**
     * @param out_dir
     *                the out_dir to set
     */
    public void setOut_dir(String out_dir) {
	this.out_dir = out_dir;
    }

    // TODO: (N.Zhegalin) Dirty hardcode. Refactoring is required
    @SuppressWarnings( { "null", "unchecked" })
    public void proccessOUT() throws DataException, CardException,
	    ServiceException {
	// ������ �������� �� �������� �������� ����
	final Search search_state = new Search();
	final List<String> states = new ArrayList<String>(1);
	final ReferenceValue refVal = (ReferenceValue) DataObject
		.createFromId(MODE_MEDO);

	List<String> addressees = new ArrayList<String>();
	List<String> files = new ArrayList<String>();

	count = 0;
	states.add(PREPARE_DELIVERY.getId().toString());
	search_state.setStates(states);
	Collection<ReferenceValue> medoValues = Collections
		.singletonList(refVal);
	search_state.addListAttribute(MODE_DELIVERY, medoValues);
	search_state.setByAttributes(true);

	final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
	search_state.setColumns(columns);
	// 1� ������� - LAST_ATTEMPT_DELIVERY
	final SearchResult.Column colAttempt = new SearchResult.Column();
	columns.add(colAttempt);
	colAttempt.setAttributeId(LAST_ATTEMPT_DELIVERY);
	// ***//
	// 2� ������� - COUNT_ATTEMPT_DELIVERY
	final SearchResult.Column colCount = new SearchResult.Column();
	columns.add(colCount);
	colCount.setAttributeId(COUNT_ATTEMPT_DELIVERY);
	// ***//
	// 3� ������� - LAST_TIME_DELIVERY
	final SearchResult.Column colLastTime = new SearchResult.Column();
	columns.add(colLastTime);
	colLastTime.setAttributeId(LAST_TIME_DELIVERY);
	// ***//

	// ���� �� ��������� ��������

	SearchResult sr = null;
	ActionQueryBase query = qFactory.getActionQuery(search_state); // ������
	query.setAction(search_state); // ������
	try {
	    sr = (SearchResult) db.executeQuery(getSystemUser(), query); // ���������
	} catch (Exception ex) {
	    logger.error(ex);
	}

	if ((sr != null) && (sr.getCards().size() != 0)) {
	    Collection<Card> cards_list = sr.getCards();
	    for (Card card_itr : cards_list) {
	     try {
		com.aplana.dbmi.service.DataServiceBean serviceBean = ServicesProvider
			.serviceBeanInstance();
		Card card;
		try {
		    card = (Card) serviceBean.getById(card_itr.getId());
		} catch (ServiceException ex) {
		    throw new CardException(ex);
		}

		// Generate UID ////////////////////////////////

		StringAttribute uidAttribute = (StringAttribute) card
			.getAttributeById(DistributionItemCardHandler.UUID_ATTRIBUTE_ID);
		String uid = uidAttribute.getValue();
		if (uid == null || "".equals(uid)) {
		    uid = UUID.randomUUID().toString();
		    uidAttribute.setValue(uid);
		}
		saveCardMEDO(card, card.getId(), serviceBean);
		// ///////////////////////////////////////////////

		String last_interval = options
			.getProperty("iteratorLAST", "24"); // �� ���������,
		// ���������
		// �������� ��
		// ��������� (24
		// ���� ��� itr = 5)
		GregorianCalendar time_current = new GregorianCalendar();
		Integer countRepeat = null; // ���������� �������� (5 ��������
		// �����. � ��������) - ��������
		Integer lastRepeat = null;
		try {
		    lastRepeat = ((IntegerAttribute) card
			    .getAttributeById(LAST_ATTEMPT_DELIVERY))
			    .getValue(); // ��������
		    // �����
		    // ��������
		    // ���
		    // ��������
		} finally { // (�������)
		    if (lastRepeat == null)
			lastRepeat = 0;
		}
		try {
		    countRepeat = ((IntegerAttribute) card
			    .getAttributeById(COUNT_ATTEMPT_DELIVERY))
			    .getValue();
		} finally {
		    if (countRepeat == null)
			countRepeat = 0;
		}
		Date date_last = null;
		try {
		    date_last = ((DateAttribute) card
			    .getAttributeById(LAST_TIME_DELIVERY)).getValue(); // ��������
		} finally {
		    if (date_last == null) { // ����
			date_last = time_current.getTime(); // ���������
		    } // ���������
		} // (�������)
		String itr_proc = options.getProperty("iterator" + lastRepeat,
			last_interval); // �� ��������� ��������� �������� ��
		// ���������
		Long itr_proc_L = 3600000 * Long.valueOf(itr_proc);

		if (time_current.getTimeInMillis() >= date_last.getTime()
			+ itr_proc_L) {
		    Exception er_rec = null;
		    Boolean f_suc = true;
		    ObjectId id_card = card.getId();
		    String id_card_s = id_card.getId().toString(); // ID
		    try {
			CardLinkAttribute recipientAttribute = (CardLinkAttribute) card
				.getAttributeById(DistributionItemCardHandler.RECIPIENT_ATTRIBUTE_ID);
			ObjectId[] recipientIds = recipientAttribute
				.getIdsArray();
			if (recipientIds == null) {
			    throw new MedoException(
				    "jbr.medo.exporter.incorrectAddressee");
			}

			for (ObjectId recipientId : recipientIds) {
			    Card recipientCard = (Card) serviceBean
				    .getById(recipientId);
			    if (!recipientCard.getTemplate().equals(
				    OrganizationCardHandler.TEMPLATE_ID)) {
				throw new MedoException(
					"jbr.medo.exporter.incorrectAddressee");
			    }
			    StringAttribute recipientFullNameAttribute = (StringAttribute) recipientCard
				    .getAttributeById(OrganizationCardHandler.FULL_NAME_ATTRIBUTE_ID);

			    String mail = ClientsOfIEDMS.instance().findByName(
				    recipientFullNameAttribute.getValue())
				    .getMail();
			    if ("".equals(mail))
				throw new MedoException(
					"jbr.medo.exporter.incorrectAddressee");
			    addressees.add(mail);
			}

			BackLinkAttribute foundationDocAttribute = (BackLinkAttribute) card
				.getAttributeById(ObjectId.predefined(
					BackLinkAttribute.class,
					"jbr.distributionItem.foundationDoc"));
			CardLinkAttribute a = (CardLinkAttribute) serviceBean
				.getById(foundationDocAttribute.getLinkSource());
			Search search = new Search();
			search.addCardLinkAttribute(a.getId(), card.getId());
			search.setByAttributes(true);
			List<Card> res = ((SearchResult) serviceBean
				.doAction(search)).getCards();
			if (res.size() > 1) {
			    logger
				    .error("Should be found only one outcome card ! cardId: " + card_itr.getId() + "; ", new MedoException());
			    //throw new MedoException();
			}
			
			if (res.size() == 0) {
			    logger
				    .error("Not found outcome card ! Iteration is interrupted, cardId: " + card_itr.getId() + "; ", new RuntimeException());
			    continue; // ���� �� ������� �� ������ ��������� ���������, �������� ������� ��������� "�������� ������ ��������" 
			}
			 
			
			Card outcomeCard = res.get(0);
			outcomeCard = (Card) serviceBean.getById(outcomeCard
				.getId());

			Document xml = CardExchangeUtils
				.getCardXML(outcomeCard); // ��������
			// ���
			// � �������
			// JBR

			String documentUidCode = properties
				.getProperty("code.document.uid");
			if (documentUidCode == null) {
			    logger
				    .error("code.document.uid property should be set");
			    throw new MedoException();
			}
			String headerUidCode = properties
				.getProperty("code.header.uid");
			if (documentUidCode == null) {
			    logger
				    .error("code.header.uid property should be set");
			    throw new MedoException();
			}

			xml.getDocumentElement().appendChild(
				CardXMLBuilder.createAttribute(xml,
					documentUidCode,
					CardXMLBuilder.STRING_TYPE, uid));
			xml.getDocumentElement().appendChild(
				CardXMLBuilder.createAttribute(xml,
					headerUidCode,
					CardXMLBuilder.STRING_TYPE, UUID
						.randomUUID().toString()));

			Document xmlMEDO = transformMEDO(xml); // �����������
			// � �������
			// MEDO
			Boolean sf = putFile(xmlMEDO, id_card_s); // �����������
			// ����
			files.add(exportingFile.getAbsolutePath());

			// /////////// Download attached files ////////////////
			CardLinkAttribute doclinks = (CardLinkAttribute) outcomeCard
				.getAttributeById(ObjectId.predefined(
					CardLinkAttribute.class, "jbr.files"));
			ObjectId[] linkedFileCards = doclinks.getIdsArray();
			if (linkedFileCards != null) {
			    for (ObjectId cardId : linkedFileCards) {
				DownloadFile downloadFile = new DownloadFile();
				downloadFile.setCardId(cardId);
				Material material = (Material) serviceBean
					.doAction(downloadFile);
				FileOutputStream outStream = null;
				try {
				    File docLink = new File(getOutDir(),
					    material.getName());
				    outStream = new FileOutputStream(docLink);
				    InputStream inStream = material.getData();

				    byte[] buffer = new byte[4 * 1024];
				    int read;
				    while ((read = inStream.read(buffer)) != -1) {
					outStream.write(buffer, 0, read);
				    }
				    outStream.flush();
				    files.add(docLink.getAbsolutePath());
				} finally {
				    if (outStream != null) {
					outStream.close();
				    }

				}
			    }
			}
			// /////////////////////////////////////////////////////
			String subject = "";
			try {
				TextAttribute theme = null;
				 theme = (TextAttribute) outcomeCard
				    .getAttributeById(SHORT_DESCRIPTION);
				if ( (theme.getValue()) == null) {
				    theme.setValue("");
				}
				StringAttribute regnumber = (StringAttribute) outcomeCard
					.getAttributeById(REG_NUMBER);
				if (regnumber.getValue() == null) {
				    regnumber.setValue("0");
				}
				DateAttribute date_signing = null;
				date_signing = (DateAttribute) outcomeCard
				    .getAttributeById(DATE_SIGNING);
				if (date_signing.getValue() == null) {
				    date_signing = (DateAttribute) outcomeCard
				    .getAttributeById(DATE_CREATED);
				} 
				String date_signing_format = SUFFIX_DATE_SIGNING.format(date_signing.getValue());
				subject = subject.concat(theme.getValue());
				subject = subject.concat(" � ");
				subject = subject.concat(regnumber.getValue());
				subject = subject.concat(" �� ");
				subject = subject.concat(date_signing_format);
			    } catch(Exception e_attr) {
			    logger.error("jbr.medo.attributeLetter.notInitialized", e_attr);
			}
			createLetter(addressees, files, subject, new File(
				outFolderExport + "/" + getFileName(), getFileName() + ".ini"));

		    } catch (Exception er) {
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
			lastRepeat = lastRepeat++;
			if (f_suc) {
			    try {
				LockObject lock = new LockObject(id_card);
				serviceBean.doAction(lock);
				try {
				    final ChangeState move = new ChangeState(); // ��������
				    // ��������-��������
				    // ���
				    // ��������
				    serviceBean.saveObject(card);
				    move.setCard(card); // ���������� ��������,
				    // ������� ����
				    // ����������.
				    move
					    .setWorkflowMove((WorkflowMove) DataObject
						    .createFromId(PREPARE_SENT));
				    serviceBean.doAction(move);
				} finally {
				    UnlockObject unlock = new UnlockObject(
					    id_card);
				    serviceBean.doAction(unlock);
				}
				logger
					.info("������� ������� ������ ��������: �������� = "
						+ lastRepeat); // �������
			    } catch (Exception e) {
				logger.error(
					"�� ������� �������� ������ ��� ��������: "
						+ card, e);
			    }
			} else {
			    logger
				    .error("��������� ������� ������ ��������: �������� = "
					    + lastRepeat); // �������

			    IntegerAttribute attr_itr = (IntegerAttribute) card
				    .getAttributeById(LAST_ATTEMPT_DELIVERY);
			    attr_itr.setValue(lastRepeat);
			    DateAttribute attr_date = (DateAttribute) card
				    .getAttributeById(LAST_TIME_DELIVERY);
			    attr_date.setValue(time_current.getTime());
			    // / ������ ����� �������� "������ �������� �
				// ����", ��������� ����.
				try {
				    CardLinkAttribute card_link = card
					    .getCardLinkAttributeById(CARD_LINK);
				    StateMedoCard smc = new StateMedoCard();
				    smc.setIterator(lastRepeat);
				    smc.setLastTime(time_current.getTime());
				    if (er_rec == null) {
					smc
						.setResultProcessing("null");
				    } else {
					smc
						.setResultProcessing(er_rec.toString());
				    }
				    Long idMedoState = smc.createCard();
				    card_link.addLinkedId(idMedoState);
				    saveCardMEDO(card, id_card, serviceBean);
				} catch(CardException ce) {
				    logger.error("jbr.medo.notCreateStateMedo",
					    ce);
				}
				catch(Exception exc) {
				    logger.error("jbr.medo.createStateMedo.exception", exc);
				}
				// /****///

			    if (lastRepeat == countRepeat--) {
				LockObject lock = new LockObject(id_card);
				serviceBean.doAction(lock);
				try {
				    final ChangeState move = new ChangeState(); // ��������
				    // ��������-��������
				    // ���
				    // ��������
				    serviceBean.saveObject(card);
				    move.setCard(card); // ���������� ��������,
				    // ������� ����
				    // ����������.
				    move
					    .setWorkflowMove((WorkflowMove) DataObject
						    .createFromId(PREPARE_NOTSENT));
				    serviceBean.doAction(move);
				} finally {
				    UnlockObject unlock = new UnlockObject(
					    id_card);
				    serviceBean.doAction(unlock);
				}
			    }
			}
		    }
		}
	     } 
	      catch(Exception ex_for) {
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

    private void createLetter(List<String> addressees, List<String> files, String subject,
	    File outFile) throws MedoException {
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
	    generator.serialize(letterFileStream);
	} catch (IOException ex) {
	    throw new MedoException("jbr.medo.notCreateLetter (letterFileStream)", ex);
	} finally {
	    if (letterFileStream != null) {
		try {
		    letterFileStream.close();
		} catch (IOException ex) {
		    throw new MedoException("jbr.medo.notCreateLetter (letterFileStream.close)",ex);
		}
	    }
	}
    }

    private UserData getSystemUser() throws DataException {
	UserData user = new UserData();
	user.setPerson(db.resolveUser(Database.SYSTEM_USER));
	user.setAddress("internal");
	return user;
    }

    private Document transformMEDO(Document XML) {
	TransformerFactory tFactory = TransformerFactory.newInstance();

	try {
	    transformerMEDO = tFactory.newTransformer(new StreamSource(Portal
		    .getFactory().getConfigService().loadConfigFile(
			    transformerPath)));
	} catch (TransformerConfigurationException ex) {
	    logger.error("Error during transformer creation", ex);
	} catch (IOException ex) {
	    logger.error("Error during template file for transformer reading",
		    ex);
	}
	// DOMSource source = new DOMSource(XML);
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

    private Boolean putFile(Document XML, String cardID) {
	Boolean result = true; // ������� �� ����������� ������ ������
	File file = null;

	try {
	    String name = generateFileName(cardID); // �������
	    // ���
	    // �����
	    // (���������
	    // ����������)
	    setFileName(name);
	    File workingDir = new File(outFolderExport + "/" + name);
	    workingDir.mkdir();
	    setOutDir(workingDir.getAbsolutePath());
	    // ���������� � �������� ����, � ������� ����� ������������� ������
	    file = setFile(getFileName() + ".xml", out_dir);

	    synchronized (file) { // �������������� ������ � �����
		if (!file.exists()) { // ���� ���� �� ����������
		    // C������� ����
		    File dr = new File(out_dir); // ������� ������� ���
		    // ������
		    synchronized (dr) { // �������������� ������ � ��������
			if (dr.exists()) // ��������� ������� ��
			    // �������������
			    file.createNewFile(); // ���� ����������, ������
			// ���� � ��������
			else {
			    dr.mkdirs(); // ���� ������� �� ����������,
			    // ������ ���
			    file.createNewFile(); // ������ � �������� ����
			}
		    }
		}

		if (!file.canWrite()) { // ���� ���� ����������, ���������
		    // ����������� ������ � ����
		    logger.error("������ � ���� : " + file
			    + " ����������!"); // ���������� ��� ���
		    // ������������� ������ � ����
		    logger
			    .error("����: " + file
				    + " - �� �������� ��� ������.");
		    file = null;
		} else { // ���� ������ � ���� ��������
		    // ���������� � ����
		    try {
			FileOutputStream out = new FileOutputStream(file);
			OutputFormat format = new OutputFormat("XML", "UTF-8",
				false);
			XMLSerializer serializer = new XMLSerializer(out,
				format);
			serializer.serialize(XML.getDocumentElement());
		    } catch (IOException ex) {
			logger
				.error(
					"Error during updated by links xml serialization",
					ex);
			result = false;
		    }
		}
	    }
	} catch (Exception IO) { // ������� ��������� �� ������
	    result = false;
	    logger.error(IO.toString(), IO);
	} finally {
	    if (result) {
		logger.info("������ �����: " + file + " ���������.");
	    } else
		logger.error("������ �����: " + file + " �� ���������!");
	    file = null;
	}
	return result;
    }

    private File setFile(String name, String dir) {
	File file = new File(dir, name);
	exportingFile = file;
	return file;
    }

    private String generateFileName(String cardId) {
	return String.format("%s_%s", cardId, SUFFIX_FILE_FORMAT
		.format(new Date()));
    }

    private void saveCardMEDO(Card card, ObjectId id_card,
	    com.aplana.dbmi.service.DataServiceBean serviceBean)
	    throws DataException, ServiceException {
	LockObject lock = new LockObject(id_card);
	serviceBean.doAction(lock);
	try {
	    serviceBean.saveObject(card);
	} finally {
	    UnlockObject unlock = new UnlockObject(id_card);
	    serviceBean.doAction(unlock);
	}
    }

}
