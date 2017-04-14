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
package com.aplana.distrmanager.handlers;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Properties;

import javax.xml.datatype.DatatypeFactory;

import com.aplana.dbmi.action.ExportCardToXml;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TypeStandard;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.cards.ElementListMailing;
import com.aplana.distrmanager.cards.Organization;
import com.aplana.distrmanager.letter.types.Letter;
import com.aplana.distrmanager.letter.types.LetterType;
import com.aplana.distrmanager.letter.types.Letter.Addressee;
import com.aplana.distrmanager.letter.types.Letter.Attachments;
import com.aplana.distrmanager.letter.types.Letter.Sender;
import com.aplana.distrmanager.letter.types.Package;
import com.aplana.dmsi.Configuration;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
@Deprecated
public class GenerateOutXml {
	
	private static final Logger logger = Logger.getLogger(GenerateOutXml.class);
	
	private DataServiceFacade serviceBean = null;

	public GenerateOutXml(DataServiceFacade serviceBean) {
		this.serviceBean = serviceBean;
	}
	
	public LetterXML createLetterXML(Properties properties) {
		
		return new LetterXML(properties);
	}
	
	public ExportCardToXml.Result createXmlDocBase(ObjectId idDocBase, ObjectId idElm, TypeStandard typeStandard) throws DataException {
		// �������� ����� ExportCardToXml ��� ��������� ����������� xml-�� � ������
		ExportCardToXml exportCardToXml = new ExportCardToXml();
		exportCardToXml.setCardId(idDocBase);
		exportCardToXml.setRecipientId(idElm);
		exportCardToXml.setTypeStandard(typeStandard);
		ExportCardToXml.Result res;
		
		try{
			res = (ExportCardToXml.Result) serviceBean.doAction(exportCardToXml);
		} catch(Exception e) {
			throw new DataException(String.format("Error while saving export-xml for card {%d} and recipient {%d}:",
					idDocBase.getId(), idElm.getId()), e);
		}
		return res;
	}
	
	public class LetterXML {
		
		private String uuid;
		private ElementListMailing elm;
		private Map<ObjectId, String> attachments;
		private Properties propertiesLetter;
		
		private LetterXML(Properties properties) {
			this.propertiesLetter = properties;
		}
		
		public void init(String uuid, ElementListMailing elm, Map<ObjectId, String> attachments) throws Exception {
			Package.setPackage(propertiesLetter);
			this.uuid = uuid;
			this.elm = elm;
			this.attachments = attachments;
		}
		
		private Letter createXmlLetter() throws Exception {
			Organization recipient = getRecipient(elm.getRecipientId());
			Organization senderDefault = getDefaultOrganization();
			
			Letter letter = Letter.newInstance();
			letter.setId(uuid);
			letter.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
			letter.setType(LetterType.FOR_SEND); // �������� ��� ��������: FOR_SEND
			letter.setAddressee(
					Addressee.newInstance().
						init(
							recipient.getUUID(),
							recipient.getFullName(),
							null
						)
					);
			letter.setSender(
					Sender.newInstance().
						init(
							senderDefault.getUUID(),
							senderDefault.getFullName(),
							null
						)
					);
			letter.setAttachments(getAttachments());
			letter.setDescription(null);
			return letter;
		}
		
		private Attachments getAttachments() throws Exception {
			Letter.Attachments letterAttachments = Attachments.newInstance();
			for(String file : attachments.values()) {
				Attachments.Attachment attachment = Attachments.Attachment.newInstance();
				attachment.setName(file); // "DOCLINKS";"��������";"Files";"C";"JBR_FILES" �� ��������� ���� -> 
				attachment.setValue(null);
				letterAttachments.add(attachment);
			}
			return letterAttachments;
		}
		
		public ExportCardToXml.Result getXmlLetter() throws Exception {
			ExportCardToXml.Result res = null;							
			InputStream input = null;
			StringWriter sw = new StringWriter();
			try {
				try {
					//Marshal object into string.
					createXmlLetter().marshal(sw);
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
		
		private Organization getDefaultOrganization() throws Exception {
			ObjectId defaultOrgId = Configuration.instance().getDefaultOrganizationId();
			Organization sender = new Organization(serviceBean);
			sender.init(defaultOrgId);
			return sender;
	    }
		
		private Organization getRecipient(ObjectId recipientId) throws Exception {
			Organization recipient = new Organization(serviceBean);
			recipient.init(recipientId);
			return recipient;
		}
	}
}
