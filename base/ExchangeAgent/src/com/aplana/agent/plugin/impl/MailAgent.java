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
package com.aplana.agent.plugin.impl;

import com.aplana.agent.conf.*;
import com.aplana.agent.conf.DeliveryConfiguration.DeliveryLogHandler;
import com.aplana.agent.conf.DocumentBodyReader.DocType;
import com.aplana.agent.conf.delivery.IdType;
import com.aplana.agent.conf.delivery.RecordType;
import com.aplana.agent.conf.routetable.Node;
import com.aplana.agent.conf.routetable.ResourceType;
import com.aplana.agent.conf.routetable.Resources.Resource;
import com.aplana.agent.plugin.*;
import com.aplana.agent.util.FileProperties;
import com.aplana.agent.util.FilePropsBuilder;
import com.aplana.agent.util.FileUtility;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.*;
import javax.mail.search.FlagTerm;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

public class MailAgent extends AbstractPlugin {
	private static final Logger logger = Logger.getLogger(MailAgent.class);

	private static final String PROPERTY_NAME_TO = "NAME_TO";
	private static final String PROPERTY_MAIL_TEXT = "MAIL_TEXT";
	private static final String PROPERTY_CONNECT_PASSWORD = "CONNECT_PASSWORD";
	private static final String PROPERTY_CONNECT_LOGIN = "CONNECT_LOGIN";
	private static final String PROPERTY_SUBJECT = "SUBJECT";
	private static final String PROPERTY_MAIL_PORT_NUMBER = "MAIL_PORT_NUMBER";
	private static final String PROPERTY_MAIL_HOST_NAME = "MAIL_HOST_NAME";
	private static final String PROPERTY_TRANSPORT_PROTOCOL_NAME = "TRANSPORT_PROTOCOL_NAME";
	private static final String PROPERTY_ENCODING = "ENCODING";
	private static final String PROPERTY_NAME_FROM = "NAME_FROM";
	private static final String PROPERTY_INTERNET_ADDRESEE_FROM = "INTERNET_ADDRESEE_FROM";
	private static final String PROPERTY_MIME_TYPE = "text/plain";
	private static final String PROPERTY_MULTIPART = "multipart/*";

	public final String JMAIL_TRANSPORT_PROTOCOL_PROPERTY = "mail.transport.protocol";
	public final String JMAIL_MAIL_HOST_PROPERTY = "mail.smtp.host";
	public final String JMAIL_MAIL_PORT_PROPERTY = "mail.smtp.port";

	private Properties smtpConfig = new Properties();

	@Override
	public void setConfiguration(File config) {
		InputStream fis = null;
		try {
			fis = ConfigService.loadConfigFile(config.getName());
			Properties props = new Properties();
			props.load(fis);
			smtpConfig.putAll(props);
		} catch (FileNotFoundException e) {
			String msg = "Config '" + config.getAbsolutePath() + "' does not exists";
			logger.error(msg, e);
		} catch (IOException e) {
			String msg = "Can't open config '" + config.getAbsolutePath()+ "'.";
			logger.error(msg, e);
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}

	@Override
	public boolean getMail(Node node) throws PluginException {
		File dest;
		String resource = null;
		try {
			resource = findOutputResource(node, ResourceType.COLLECTOR);
			dest = new File(new URL(resource).getFile());
		} catch (MalformedURLException e) {
			logger.error("Can't process this URL:" + resource, e);
			throw new PluginException("Bad collector's URL");
		}
		for (Resource res : node.getResources().getResource()) {
			if (res.getType() == ResourceType.DOCUMENT || res.getType() == ResourceType.TICKET) {
				try {
					Properties pop3Props = new Properties();
			        pop3Props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			        pop3Props.setProperty("mail.pop3.socketFactory.fallback", "false");
			        
			        // protocol://username:password@host/foldername
			        final URLName url = new URLName(res.getUrl());
			        
			        class MailAuthenticator extends Authenticator {
			        	public MailAuthenticator() {}
			        	public PasswordAuthentication getPasswordAuthentication() {
			        		return new PasswordAuthentication(url.getUsername(), url.getPassword());
			        	}
			        }
			        Session session = Session.getInstance(pop3Props, new MailAuthenticator());
			        
			        Store store = session.getStore(url);
			        store.connect();
			        Folder defFolder = store.getDefaultFolder();
			        Folder inbox = defFolder.getFolder("INBOX");
			        inbox.open(Folder.READ_WRITE);

			        Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
					for (Message inMessage : messages) {
						if (inMessage.getSubject() != null 
								&& inMessage.getSubject().equals(smtpConfig.getProperty(PROPERTY_SUBJECT))) {
							if (inMessage.isMimeType(PROPERTY_MIME_TYPE)) {
								// TODO Handle messages without attachment
								logger.debug("e-mail (" + inMessage.getSubject() + ") without attachment... skipping...");
							} else if (inMessage.isMimeType(PROPERTY_MULTIPART)) {
								File destDir = new File(dest, UUID.randomUUID().toString());
								Properties props = new FilePropsBuilder()
									.set(FileProperties.TEXT, env.getProperty(AGENT_ID) + "-receive")
									.set(FileProperties.PARTIAL, true)
									.build();
								FileUtility.lockFolder(destDir, props, true);
								Multipart multipart = (Multipart) inMessage.getContent();
								for (int i = 0; i < multipart.getCount(); i++) {
									Part part = multipart.getBodyPart(i);
									if (part.getFileName() != null
										&& !"".equals(part.getFileName())
										&& part.getDisposition() != null
										&& part.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {

										String fileName = MimeUtility.decodeText(part.getFileName());
										File attachment = new File(destDir, fileName);
										IOUtils.copy(part.getInputStream(), new FileOutputStream(attachment));
									}
								}
								DeliveryLogHandler logHandler = getDeliveryLogHandler(destDir);

								LetterDetectionResult ldr;
								try {
									ldr = DocumentBodyReader.detectMainDocument(destDir);
								} catch (LetterTypeDetectException e2) {
									FileUtility.deleteDir(destDir);
									throw new SendMailException("Unknown letter type in " + destDir, e2);
								}
								if (ldr.getType().equals(DocType.UNKNOWN)){
									FileUtility.deleteDir(destDir);
									throw new SendMailException("Unknown letter type in " + destDir);
								}
								File mainDoc = (ldr.getEnvelopeFile() == null) ?
										ldr.getDocFile() :			// ���� ����� �������� ���� (?) �� ���� �������� ��������
										ldr.getEnvelopeFile();
								DocumentBodyReader reader = getDocumentBodyReader(mainDoc);
								RecordType record = RecordTypeProxy.getInstance()
										.setDate(new Date())
										.setTransportAgentName(env.getProperty(Plugin.TRANSPORT_AGENT_NAME))
										.setTransportAgentUuid(env.getProperty(Plugin.TRANSPORT_AGENT_UUID))
										.setAgentName(env.getProperty(Plugin.AGENT_ID))
										.setActionType(IdType.RECEIVE)
										.setFromNodeName(node.getName())
										.setFromURL(res.getUrl())
										.setToNodeName("COLLECTOR")
										.setToURL(dest)
										.setPacketName(destDir.getName())
										.setPacketId((reader == null) ? "unknown" : reader.getId())
										.setLetterContent(destDir)
										.getRecord();
								try {
									logHandler.addRecord(record);
								} catch (IncorrectMD5Exception e) {
									String msg = "Failed check by MD5";
									logger.error(msg, e);
									FileUtility.silentDeleteDir(destDir);
									throw new PluginException(msg, e);
								}
								logHandler.save();
								FileUtility.unlockFolder(destDir);
							}
							inMessage.setFlag(Flags.Flag.SEEN, true);
							inMessage.setFlag(Flags.Flag.DELETED, true);
						}
					}

					inbox.close(true);
					store.close();
				} catch (NoSuchProviderException e) {
					logger.error("Can't process this URL:" + res.getUrl(), e);
					return false;
				} catch (IOException e) {
					String message = "Can't move directory to:" + dest.getAbsolutePath();
					logger.error(message, e);
					throw new GetMailException(message);
				} catch (MessagingException e) {
					String message = "Can't send email:" + dest.getAbsolutePath();
					logger.error(message, e);
					throw new PluginException(message);
				} catch (JAXBException e) {
					String message = "Error while process deliveryLog";
					logger.error(message, e);
					throw new PluginException(message);
				} catch (DatatypeConfigurationException e) {
					String message = "No DataTypeFactory configuration found. Please check it.";
					logger.error(message, e);
					throw new PluginException(message);
				}
			}
		}
		return true;
	}

	@Override
	public boolean sendMail(URL letter, Node destinationNode) throws PluginException {       
		File srcDir = FileUtils.toFile(letter);
		LetterDetectionResult ldr;
		try {
			ldr = DocumentBodyReader.detectMainDocument(srcDir);
		} catch (LetterTypeDetectException e2) {
			throw new SendMailException("Unknown letter type in " + letter, e2);
		}
		if (ldr.getType().equals(DocType.UNKNOWN)){
			throw new SendMailException("Unknown letter type in " + letter);
		}

		File mainDoc = (ldr.getEnvelopeFile() == null) ?
				ldr.getDocFile() :			// ���� ����� �������� ���� (?) �� ���� �������� ��������
				ldr.getEnvelopeFile();

		DocType mainDocType = ldr.getType();
		String toEmailURL = findOutputResource(destinationNode, mainDocType, letter);
		if (toEmailURL == null) {
			//���� ����� �� ������� ���� ���������� (???), ������� ��������� ��������� �����
			return false;
		}
		URLName urlName = new URLName(toEmailURL);
		DocumentBodyReader reader            = getDocumentBodyReader(mainDoc);
		DeliveryLogHandler logHandlerSuccess = getDeliveryLogHandler(srcDir);
		DeliveryLogHandler logHandlerFail    = getDeliveryLogHandler(srcDir);
		
		try {
			if (reader == null || !reader.isValid(srcDir)) {
				String msg = "Error while reading " + srcDir.getAbsolutePath();
				logger.error(msg);
				RecordType record = RecordTypeProxy.getInstance()
						.setDate(new Date())
						.setTransportAgentName(env.getProperty(Plugin.TRANSPORT_AGENT_NAME))
						.setTransportAgentUuid(env.getProperty(Plugin.TRANSPORT_AGENT_UUID))
						.setAgentName(env.getProperty(Plugin.AGENT_ID))
						.setActionType(IdType.REFUSE)
						.setFromNodeName("COLLECTOR")
						.setFromURL(srcDir.getParentFile())
						.setToNodeName(destinationNode.getName())
						.setToURL(toEmailURL)
						.setPacketName(srcDir.getName())
						.setPacketId(reader==null ? "unknown" : reader.getId())
						.setLetterContent(srcDir)
						.getRecord();
				try {
					logHandlerFail.addRecord(record);
					logHandlerFail.save();
				} catch (IncorrectMD5Exception e) {
					logger.error(e.getMessage(), e);
				}
				handleInvalidMessage(srcDir, destinationNode);
				return true;
			}
		
			InternetAddress fromAddress = new InternetAddress(
					smtpConfig.getProperty(PROPERTY_INTERNET_ADDRESEE_FROM),
					smtpConfig.getProperty(PROPERTY_NAME_FROM),
					smtpConfig.getProperty(PROPERTY_ENCODING));

			InternetAddress toAddress = new InternetAddress(
					urlName.getFile(),
					smtpConfig.getProperty(PROPERTY_NAME_TO),
					smtpConfig.getProperty(PROPERTY_ENCODING));

			Properties smtpServerProperties = getSMTPServerProperties();

			Session session = Session.getDefaultInstance(smtpServerProperties);
			Transport transport = session.getTransport();
			MimeMessage mailMessage = new MimeMessage(session);
			mailMessage.setFrom(fromAddress);
			mailMessage.setRecipient(Message.RecipientType.TO, toAddress);
			mailMessage.setSubject(smtpConfig.getProperty(PROPERTY_SUBJECT),
					smtpConfig.getProperty(PROPERTY_ENCODING));

			BodyPart bodyPart = new MimeBodyPart();
			bodyPart.setContent(smtpConfig.getProperty(PROPERTY_MAIL_TEXT),
					"text/plain; charset=" + smtpConfig.getProperty(PROPERTY_ENCODING));

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(bodyPart);

			logHandlerSuccess.addRecord(RecordTypeProxy.getInstance()
					.setDate(new Date())
					.setTransportAgentName(env.getProperty(Plugin.TRANSPORT_AGENT_NAME))
					.setTransportAgentUuid(env.getProperty(Plugin.TRANSPORT_AGENT_UUID))
					.setAgentName(env.getProperty(Plugin.AGENT_ID))
					.setActionType(IdType.SEND)
					.setFromNodeName("COLLECTOR")
					.setFromURL(srcDir.getParentFile())
					.setToNodeName(destinationNode.getName())
					.setToURL(toEmailURL)
					.setPacketName(srcDir.getName())
					.setPacketId(reader.getId())
					.setLetterContent(srcDir)
					.getRecord()
			);
			logHandlerSuccess.save();
			
			for(File file : srcDir.listFiles()) {
				if (!FileUtility.isSpecialFile(file)) {
					addAttachment(file, file.getName(), multipart);
				}
			}
			mailMessage.setContent(multipart);
			transport.addTransportListener(new SendTransportListener());
			transport.connect(
					smtpConfig.getProperty(PROPERTY_CONNECT_LOGIN),
					smtpConfig.getProperty(PROPERTY_CONNECT_PASSWORD));
			transport.sendMessage(mailMessage, new Address[] {toAddress});
			transport.close();
			processCopy(srcDir, destinationNode); // ����������� ������� ������������� ���������
			FileUtility.markPartial(srcDir);
			FileUtility.silentDeleteDir(srcDir);
		} catch (DatatypeConfigurationException e) {
			String message = "No DataTypeFactory configuration found. Please check it.";
			logger.error(message, e);
			handleErrorMail(logHandlerFail, srcDir, destinationNode, toEmailURL, reader);
			throw new PluginException(message);
		} catch (Exception e) {
			logger.error("UnsupportedEncodingException" + e.getMessage());
			handleErrorMail(logHandlerFail, srcDir, destinationNode, toEmailURL, reader);
			throw new PluginException(e.getMessage(), e);
		}
		return true;
	}
	
	private void handleErrorMail(DeliveryLogHandler failHandler, File srcDir, Node destinationNode, String email, DocumentBodyReader reader) {
		try {
			failHandler.addRecord(RecordTypeProxy.getInstance()
					.setDate(new Date())
					.setTransportAgentName(env.getProperty(Plugin.TRANSPORT_AGENT_NAME))
					.setTransportAgentUuid(env.getProperty(Plugin.TRANSPORT_AGENT_UUID))
					.setAgentName(env.getProperty(Plugin.AGENT_ID))
					.setActionType(IdType.REFUSE)
					.setFromNodeName("COLLECTOR")
					.setFromURL(srcDir.getParentFile())
					.setToNodeName(destinationNode.getName())
					.setToURL(email)
					.setPacketName(srcDir.getName())
					.setPacketId(reader.getId())
					.setLetterContent(srcDir)
					.getRecord()
			);
			failHandler.save();
		} catch (Exception e) {
			String msg = "Error while adding fail record into " + DeliveryConfiguration.DELIVERY_LOG_FILENAME;
			logger.error(msg, e);
		}
	}
	
	private Properties getSMTPServerProperties() {
		Properties smtpServerProperties = new Properties();
		smtpServerProperties.put(JMAIL_TRANSPORT_PROTOCOL_PROPERTY, smtpConfig.getProperty(PROPERTY_TRANSPORT_PROTOCOL_NAME));
		smtpServerProperties.put(JMAIL_MAIL_HOST_PROPERTY, smtpConfig.getProperty(PROPERTY_MAIL_HOST_NAME));
		smtpServerProperties.put(JMAIL_MAIL_PORT_PROPERTY, smtpConfig.getProperty(PROPERTY_MAIL_PORT_NUMBER));
		return smtpServerProperties;
	}

	/**
	 * Adds attachemnt to the mail message.
	 * 
	 * @param attachment
	 * @param fileName
	 * @param multipart
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	private void addAttachment(File attachment, String fileName,
			Multipart multipart) throws MessagingException,
			UnsupportedEncodingException {
		MimeBodyPart documentPart = new MimeBodyPart();
		final DataSource fileDataSource = new FileDataSource(attachment);
		documentPart.setDataHandler(new DataHandler(fileDataSource));
		documentPart.setFileName(MimeUtility.encodeText(fileName));
		multipart.addBodyPart(documentPart);
	}

	/**
	 * Notifies about the results of message delievery.
	 * 
	 * @author atsvetkov
	 * 
	 */
	class SendTransportListener implements TransportListener {
		public void messageDelivered(TransportEvent arg0) {
			logger.info("Message sent suuccessfull: " + arg0.getMessage());
		}

		public void messageNotDelivered(TransportEvent arg0) {
			logger.error("Message was not delivered: " + arg0.getMessage());
		}

		public void messagePartiallyDelivered(TransportEvent arg0) {
			logger.error("Message was delivered partially: " + arg0.getMessage());
		}
	}

	@Override
	protected boolean handleInvalidMessage(File messageDir, Node node) throws PluginException {
		super.handleInvalidMessage(messageDir, node);
		FileUtility.silentDeleteDir(messageDir);
		return true;
	}
	
	@Override
	public void cleanResources(Node node) throws PluginException {
		//do nothing yet
	}
}
