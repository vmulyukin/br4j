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
package com.aplana.distrmanager.letter;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.aplana.agent.conf.envelope.FormatType;
import com.aplana.agent.conf.envelope.Letter;
import com.aplana.agent.conf.envelope.LetterType;
import com.aplana.agent.conf.envelope.Letter.Addressee;
import com.aplana.agent.conf.envelope.Letter.Attachments;
import com.aplana.agent.conf.envelope.Letter.Sender;
import com.aplana.dbmi.action.ExportCardToXml;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.cards.DefaultSender;
import com.aplana.distrmanager.cards.ElementListMailing;
import com.aplana.distrmanager.cards.Organization;
import com.aplana.dmsi.Configuration;

public class OperationsOnLetter {

	private static final Logger logger = Logger.getLogger(OperationsOnLetter.class);
	private static final String PACKAGE_LETTER = "com.aplana.agent.conf.envelope";
	public static final String RESULT_FILE_NAME = "DistributionLetter.xml";
	
	private DataServiceFacade serviceBean = null;
	private String uuid;
	private ElementListMailing elm;
	private Map<ObjectId, String> attachments;
	
	public OperationsOnLetter(DataServiceFacade serviceBean) {
		this.serviceBean = serviceBean;
	}
	
	public void init(String uuid, ElementListMailing elm, Map<ObjectId, String> attachments) throws Exception {
		this.uuid = uuid;
		this.elm = elm;
		this.attachments = attachments;
	}
	
	private void marshal(Writer writer) throws Exception {
		JAXBContext jc = JAXBContext.newInstance(PACKAGE_LETTER);
		//Create marshaller
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		//Marshal object into writer.
		marshaller.marshal(createLetter(), writer);
	}
	
	private Letter unmarshal(InputStream is) throws Exception {
		JAXBContext jc = JAXBContext.newInstance(PACKAGE_LETTER);
		//Create unmarshaller
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		Object jaxbLetter = unmarshaller.unmarshal(is);
		return (Letter)jaxbLetter;
	}
	
	private Letter createLetter() throws Exception {
		Organization recipient = getRecipient(elm.getRecipientId());
		Organization senderDefault = DefaultSender.getDefaultOrganization(serviceBean);
		
		Letter letter = new Letter();
		letter.setId(uuid);
		letter.setMessageId(elm.getUid());
		letter.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
		letter.setType(LetterType.FOR_SEND); // �������� ��� ��������: FOR_SEND
		letter.setFormat(modeDelivery(elm)); 
		Addressee addressee = new Addressee();
		addressee.setGuid(recipient.getUUID());
		addressee.setName(recipient.getFullName());
		addressee.setValue(null);
		letter.setAddressee(addressee);
		
		Sender sender = new Sender();
		sender.setGuid(senderDefault.getUUID());
		sender.setName(senderDefault.getFullName());
		sender.setValue(null);
		letter.setSender(sender);
		
		letter.setAttachments(getAttachments());
		letter.setDescription(null);
		return letter;
	}
	
	private Attachments getAttachments() throws Exception {
		Letter.Attachments letterAttachments = new Attachments();
		for(String file : attachments.values()) {
			Attachments.Attachment attachment = new Attachments.Attachment();
			attachment.setName(file); // "DOCLINKS";"��������";"Files";"C";"JBR_FILES" �� ��������� ���� -> 
			attachment.setValue(null);
			letterAttachments.getAttachment().add(attachment);
		}
		return letterAttachments;
	}
	
	private FormatType modeDelivery(ElementListMailing elm) {
		FormatType modeDelivery = null;
		Long modeDeliveryElm = (Long)elm.getDeliveryValue().getId().getId();
		Long modeMedo = (Long)elm.MODE_MEDO.getId();
		Long modeDelo = (Long)elm.MODE_DELO.getId();
		Long modeGost = (Long)elm.MODE_GOST.getId();
		if (modeMedo.equals(modeDeliveryElm))
			modeDelivery = FormatType.IEDMS;
		if (modeDelo.equals(modeDeliveryElm)) 
			modeDelivery = FormatType.DELO;
		if (modeGost.equals(modeDeliveryElm)) 
			modeDelivery = FormatType.GOST;
		return modeDelivery;
	}
	
	public ExportCardToXml.Result getLetter() throws Exception {
		ExportCardToXml.Result res = null;							
		InputStream input = null;
		StringWriter sw = new StringWriter();
		try {
			try {
				//Marshal object into string.
				marshal(sw);
				input = IOUtils.toInputStream(sw.getBuffer().toString(), "UTF-8");
				res = new ExportCardToXml.Result(input, null);
			} catch (Exception e) {
				logger.error("jbr.letter.marshaller.error", e);
				throw new Exception(e); // ������������
			}
		} finally {
			if(null != sw) {
				sw.flush();
				sw.close();
			}
		}
		return res;
	}
	
	private Organization getRecipient(ObjectId recipientId) throws Exception {
		Organization recipient = new Organization(serviceBean);
		recipient.init(recipientId);
		return recipient;
	}
}
