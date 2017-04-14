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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.medo.RecordOfFiles.OutcomeExportPutFiles;
import com.aplana.medo.cards.CardException;
import com.aplana.medo.cards.Imported;
import com.aplana.medo.cards.InBox;
import com.aplana.medo.cards.Sender;

/**
 * @author PPanichev
 *
 */
public class OutNotification {
    
    Log logger = LogFactory.getLog(getClass());
    
    private String notificationUID = null;
    private String notificationID = null;
    private String outFolderExport = null;
    
    public OutNotification(String outFolderExp) throws CardException {
	this.outFolderExport = outFolderExp;
	process();
    }
    
    public void process() throws CardException {
	Collection<Card> cards_id = InBox.findCards();
	
	// ���� �� ��������� ��������
	if ((cards_id != null) && (cards_id.size() != 0)) {
	    for (Card card_itr : cards_id) {
		try {
		    List<String> addressees = new ArrayList<String>();
		    List<String> files = new ArrayList<String>();
		    Card card = null;
		    InBox ibCard = new InBox(card_itr.getId());
		    card = ibCard.getCard();
		    // /������� �������� ����������� � ����������� ��������
			String sender_name = "";
			
			ObjectId[] senderIds = ibCard.getSender().getIdsArray();
			if (senderIds == null) {
			    logger
				    .warn("jbr.medo.outnotification.sender.isNull");
			} else {
			    ObjectId senderId = senderIds[0];
			    Sender sender = new Sender(senderId);
			    sender_name = sender.getFullName();
			}
			    // /***///
				/* ������� mail �����������, �� ������� ����� */
			    String mail = ClientsOfIEDMS.instance().findByName(sender_name)
				    .getMail();
			    if ("".equals(mail))
				throw new CardException(
					"jbr.medo.outnotification.incorrectAddressee");
			    addressees.add(mail);
			    /* *** */
		    
		    ///������� ��������������� �������� (��������) � ����������� UID � ID
		    ObjectId[] sourcesIds = ibCard.getSourcesAttribute()
			.getIdsArray();
		    if (sourcesIds == null) {
			throw new CardException(
			    "jbr.medo.outnotification.original(imported document).isNull");
		    }
		    ObjectId sourcesId = sourcesIds[0];
		    
		    Imported imported = new Imported(sourcesId);
		    /** ������� ����������� UID**/
		    String notificationUID_catalog = UUID.randomUUID().toString();
		    notificationUID = imported.getDocUID().getValue();
		    notificationID = imported.getDocID().getValue();
		    ///***///
		    String state = card.getState().getId().toString();
		    String xmlNotification = new String("");

		    int reg = -1;
		    DateFormat suffix_date_notification = new SimpleDateFormat("yyyy-MM-dd");
		    DateFormat suffix_time_notification = new SimpleDateFormat("HH:mm:ss");
		    DateFormat suffix_date_num = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    GregorianCalendar gc = new GregorianCalendar();
		    String date_notification = suffix_date_notification.format(gc.getTime());
		    String time_notification = suffix_time_notification.format(gc.getTime());
		    String date_time_notification = date_notification + "T" + time_notification;
		    if (state.equals(InBox.REGISTRATION.getId().toString())) { // ��������
			xmlNotification = "<?xml version=\"1.0\" encoding=\"WINDOWS-1251\"?>\r\n" +
			"<xdms:communication xdms:version=\"2.0\" xmlns:xdms=\"http://www.infpres.com/IEDMS\">\r\n" +
			"<xdms:header xdms:type=\"�����������\">\r\n" +
			"<xdms:source xdms:uid=\"1D04CA3E-DF1A-0CB4-C325-6EF4003CA2AB\">\r\n" +
			"<xdms:organization>������������ ����� � �������� ������������ ���������� ���������</xdms:organization>\r\n" +
			"</xdms:source>\r\n" +
			"</xdms:header> \r\n" +
			"<xdms:notification xdms:type=\"���������������\" xdms:uid=\"" + notificationUID + "\" xdms:id=\"" + notificationID + "\">\r\n" +
			"<xdms:documentAccepted>\r\n" +
			"<xdms:time>" + date_time_notification + "</xdms:time>\r\n" +
			"<xdms:foundation>\r\n" +
			"<xdms:num>\r\n" +
			"<xdms:number>" + ibCard.getDocNumber().getValue() + "</xdms:number>\r\n" +
			"<xdms:date>" + suffix_date_num.format(ibCard.getDocDate().getValue()) + "</xdms:date>\r\n" +
			"</xdms:num>\r\n" +
			"</xdms:foundation>\r\n" +
			"<xdms:num>\r\n" +
			"<xdms:number>" + ibCard.getRegNumber().getValue() + "</xdms:number>\r\n" +
			"<xdms:date>" + suffix_date_num.format(ibCard.getDateRegistration().getValue()) + "</xdms:date>\r\n" +
			"</xdms:num>\r\n" +
			"</xdms:documentAccepted>\r\n" +
			"</xdms:notification>\r\n" +
			"</xdms:communication>";
			reg = 1;
		    } else if (state.equals(InBox.TRASH.getId().toString())) { // ��������
			xmlNotification = "<?xml version=\"1.0\" encoding=\"WINDOWS-1251\"?>\r\n" +
			"<xdms:communication xdms:version=\"2.0\" xmlns:xdms=\"http://www.infpres.com/IEDMS\">\r\n" +
			"<xdms:header xdms:uid='" + UUID.randomUUID().toString() + "' xdms:type='�����������' xdms:created='" + date_time_notification + "'>\r\n" +
			"<xdms:source xdms:uid='1D04CA3E-DF1A-0CB4-C325-6EF4003CA2AB'>\r\n" +
			"<xdms:organization>������������ ����� � �������� ������������ ���������� ���������</xdms:organization>\r\n" +
			"</xdms:source>\r\n" +
			"</xdms:header>\r\n" +
			"<xdms:notification xdms:uid='" + notificationUID + "' xdms:type='�������� � �����������'" + " xdms:id=\"" + notificationID + "\">\r\n" +
			"<xdms:documentRefused>\r\n" +
			"<xdms:time>" + date_time_notification + "</xdms:time>\r\n" +
			"<xdms:foundation>\r\n" +
			"<xdms:organization>" + sender_name + "</xdms:organization>\r\n" +
			"<xdms:num>\r\n" +
			"<xdms:number>" + ibCard.getDocNumber().getValue() + "</xdms:number>\r\n" +
			"<xdms:date>" + suffix_date_num.format(ibCard.getDocDate().getValue()) + "</xdms:date>\r\n" +
			"</xdms:num>\r\n" +
			"</xdms:foundation>\r\n" +
			"<xdms:reason>�� �������� �����������</xdms:reason>\r\n" +
			"</xdms:documentRefused>\r\n" +
			"</xdms:notification>\r\n" +
			"</xdms:communication>\r\n";
			reg = 0;
		    } 
		    
		    byte[] notify = xmlNotification.getBytes("Cp1251");
		    InputStream inputStream = new ByteArrayInputStream(notify);
		    DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		    
		    DocumentBuilder db = f.newDocumentBuilder();
		    Document doc = db.parse(inputStream);
		    
		    OutcomeExportPutFiles pfOutcome = new OutcomeExportPutFiles(outFolderExport, notificationUID_catalog);
		    File PutFile = pfOutcome.putFile(doc); // �����������
		    								    // ����
		  ///********////// TEST �������� � ������������ �����, ���� �� ���������� ������
			try {
			    logger.warn("jbr.medo.Exporter double out notification begin...");
			    OutcomeExportPutFiles pfOutcome_double = new OutcomeExportPutFiles(outFolderExport + "/../doubleOUT/", notificationUID_catalog);
			    pfOutcome_double.putFile(doc);
			    logger.warn("jbr.medo.Exporter double out notification end.");
			} catch(Exception exc_par) {
			    logger.error("jbr.medo.Exporter. Put double file out notification failed !", exc_par);
			}
			////******//////
		    files.add(PutFile.getName());
		    String subject = "";
		    
		   try {
		    	subject = subject.concat(ibCard.getTheme().getValue());
		    	subject =  subject.concat(" � ");
		    	subject =  subject.concat(ibCard.getRegNumber()
				.getValue());
		    	subject =  subject.concat(" �� ");
			subject =  subject.concat(date_notification);
		    } catch(Exception e_attr) {
			logger.error("jbr.medo.outnotification.attributeLetter.notInitialized", e_attr);
		    }
		    
		    String namePutFileParent = PutFile.getParentFile().getName();
		    createLetter(addressees, files, subject, new File(
				outFolderExport + "/" + namePutFileParent, namePutFileParent + ".ini"), reg);
		    /** TEST �������� ������ � ������������ �����**/
			try {
			    logger.warn("jbr.medo.Exporter double letter out notification begin...");
			    createLetter(addressees, files, subject, new File(
					outFolderExport + "/../doubleOUT/" + namePutFileParent, namePutFileParent + ".ini"), reg);
			    logger.warn("jbr.medo.Exporter double letter out notification end.");
			} catch(Exception exc_letter) {
			    logger.error("jbr.medo.Exporter. Put double file letter out notification failed !", exc_letter);
			}
			///****////
		    StringAttribute medo_type = (StringAttribute) card
		    .getAttributeById(InBox.MEDO_TYPE);
		    if (reg == 0) {
			ibCard.setAttributeCard(medo_type, "��������");
		    } else if (reg == 1) {
			ibCard.setAttributeCard(medo_type, "���������������");
		    }
		} catch(Exception ex_for) {
		    	logger.error("jbr.medo.proccessout.errorcard_itr, card_id: "+ card_itr.getId() + "; ", ex_for);
			StackTraceElement ste = Thread.currentThread().getStackTrace()[1];
			logger.error("\r\n jbr.medo.ex_for.codePoint: " +  ste.getFileName() + ":" +
			           ste.getClassName() + ":" +
			           ste.getMethodName() + ":" + 
			           ste.getLineNumber() + "\r\n");
			//continue;
		      }
	    }
	}
    }
    
    public void createLetter(List<String> addressees, List<String> files, String subject,
	    File outFile, int registration) throws CardException {
	TicketGenerator generator = new TicketGenerator();
	if (registration == 0) {
	    generator.setSent(false);
	    generator.setRead(false);
	} else if (registration == 1) {
	    generator.setSent(true);
	    generator.setRead(true);   
	}
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
	    throw new CardException("jbr.medo.OutNotification.notCreateLetter (letterFileStream)",ex);
	} finally {
	    if (letterFileStream != null) {
		try {
		    letterFileStream.close();
		} catch (IOException ex) {
		    throw new CardException("jbr.medo.OutNotification.notCreateLetter (letterFileStream.close)",ex);
		}
	    }
	}
    }
}
