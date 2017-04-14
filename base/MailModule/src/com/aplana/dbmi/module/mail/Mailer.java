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
package com.aplana.dbmi.module.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.aplana.dbmi.Portal;

public class Mailer implements ApplicationContextAware, InitializingBean {
    protected final Log logger = LogFactory.getLog(getClass());

    private String encoding = "UTF-8";

    private Session mailSession;

    private String defaultFromAddress;

    private String defaultFromName;

    private String defaultSubject;

    private String defaultCardLinkPrefix;

    private String configFile;

    private static ApplicationContext applicationContext;

    public static Mailer getMailer() {
        return (Mailer) applicationContext.getBean("mailer");
    }

    public boolean sendMail(String letter, String recipient) {
        return sendMail(letter, recipient, getDefaultSubject(), getDefaultFromAddress(), getDefaultFromName());
    }

    public boolean sendMail(String letter, String recipient, String subject) {
        return sendMail(letter, recipient, subject, getDefaultFromAddress(), getDefaultFromName());
    }

    public boolean sendMail(String letter, String recipient, String subject, String from, String fromName) {
        logger.info("Sending mail to " + recipient + "; subject: '" + subject + '\'');
        if (recipient == null || recipient.length() == 0) {
            logger.warn("[DataService] recipient is empty");
            return false;
        }
        try {
            MimeMessage msg = new MimeMessage(mailSession);

            if (from != null) {
                try {
                    msg.setFrom(new InternetAddress(from, fromName, getEncoding()));
                } catch (UnsupportedEncodingException e) {
                    logger.error("[DataService] Can't set encoded \"from\" in Mailer.");
                    msg.setFrom(new InternetAddress(from));
                }
            }
            /*InternetAddress[] parsed = InternetAddress.parse(recipient);
            logger.info("[DEBUG] Mailer: recipient=" + recipient +
            		"; address=" + parsed[0].getAddress() + ", personal=" + parsed[0].getPersonal());*/
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            msg.setSubject(subject, getEncoding());
            // msg.setContent(html, "text/html");
            msg.setDataHandler(new DataHandler(new EncodedSource(letter)));
            Transport.send(msg);
            logger.info("Mail sent");
        } catch (Exception e) {
            logger.error("Error sending mail", e);
            return false;
        }
        return true;
    }

	public boolean sendMail(final String letter, String recipient, String subject, Collection attachments) {
        logger.info("Sending mail to " + recipient + "; subject: '" + subject +
        		"\'; attachments: " + attachments.size());
		try {
			MimeMessage msg = new MimeMessage(mailSession);
			msg.setSentDate(new Date());
			msg.setFrom(new InternetAddress(getDefaultFromAddress(), getDefaultFromName(), getEncoding()));
			msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
			msg.setSubject(subject, getEncoding());

			MimeMultipart content = new MimeMultipart();
			MimeBodyPart body = new MimeBodyPart();
			body.setDataHandler(new DataHandler(new EncodedSource(letter)));
			content.addBodyPart(body);

			for (Iterator itr = attachments.iterator(); itr.hasNext(); ) {
				DataSource source = (DataSource) itr.next();
				MimeBodyPart attachment = new MimeBodyPart();
				attachment.setDataHandler(new DataHandler(source));
				attachment.setFileName(MimeUtility.encodeText(source.getName(), getEncoding(), null));
				content.addBodyPart(attachment);
			}

			msg.setContent(content);
			Transport.send(msg);
			return true;
		} catch (Exception e) {
			logger.error("Error sending message", e);
			return false;
		}
	}

    public Session getMailSession() {
        return mailSession;
    }

    public void setMailSession(Session mailSession) {
        this.mailSession = mailSession;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getDefaultFromAddress() {
        return defaultFromAddress;
    }

    public void setDefaultFromAddress(String defaultFromAddress) {
        this.defaultFromAddress = defaultFromAddress;
    }

    public String getDefaultFromName() {
        return defaultFromName;
    }

    public void setDefaultFromName(String defaultFromName) {
        this.defaultFromName = defaultFromName;
    }

    public String getDefaultSubject() {
        return defaultSubject;
    }

    public void setDefaultSubject(String defaultSubject) {
        this.defaultSubject = defaultSubject;
    }

    public String getDefaultCardLinkPrefix() {
        return defaultCardLinkPrefix;
    }

    public void setDefaultCardLinkPrefix(String defaultCardLinkPrefix) {
        this.defaultCardLinkPrefix = defaultCardLinkPrefix;
        try {
            /*Enumeration networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) networkInterfaces.nextElement();
                logger.info("networkInterface.displayName=" + networkInterface.getDisplayName());
                logger.info("networkInterface.name=" + networkInterface.getName());
                Enumeration inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) inetAddresses.nextElement();
                    logger.info("networkInterface.[" + networkInterface.getName() + "].inetAddress=" + inetAddress);
                }
                logger.info("-----------------------------------");
            }*/
            InetAddress localHost = InetAddress.getLocalHost();
            logger.info("localHost=" + localHost);
            logger.info("localHost.CanonicalHostName=" + localHost.getCanonicalHostName());
            logger.info("localHost.HostAddress=" + localHost.getHostAddress());
            logger.info("localHost.HostName=" + localHost.getHostName());
            this.defaultCardLinkPrefix = MessageFormat.format(defaultCardLinkPrefix,
            		new Object[] {localHost.getHostAddress()});
        } catch (UnknownHostException e) {
            logger.error("Error determining local host address:", e);
        } /*catch (SocketException e) {
            logger.error("Error determining local host address:", e);
        }*/
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void afterPropertiesSet() throws Exception {
        loadConfig();
    }

    private void loadConfig() throws IOException {
        // TODO implement locale-sensitive loading
        Properties config = new Properties();
        config.load(Portal.getFactory().getConfigService().loadConfigFile(configFile));

        BeanWrapperImpl thisWrapper = new BeanWrapperImpl(this);
        thisWrapper.setPropertyValues(config);
    }

    protected class EncodedSource implements DataSource {

    	private String source;

    	public EncodedSource(String source) {
    		this.source = source;
    	}

		public String getContentType() {
            return "text/html; charset=" + getEncoding();
		}

		public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(source.getBytes(getEncoding()));
		}

		public String getName() {
			return "Encoded source";
		}

		public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException();
		}

    }
}
