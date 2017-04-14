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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.agent.conf.envelope.FormatType;
import com.aplana.agent.conf.envelope.Letter;
import com.aplana.dbmi.action.ImportCardFromXml;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TypeStandard;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dmsi.action.ExceptionOccurredAction;
import com.aplana.dmsi.action.ImportCardByDelo;
import com.aplana.dmsi.action.ImportCardByGOST;
import com.aplana.dmsi.card.CardHandler;
import com.aplana.dmsi.types.common.File;
import com.aplana.dmsi.types.common.Organization;
import com.aplana.dmsi.types.common.Packet;

public class DoImportCardFromXml extends ActionQueryBase implements WriteQuery {

	private static final long serialVersionUID = 1L;

	private static final String ENVELOPE_PACKAGE = "com.aplana.agent.conf.envelope";

	private static final String LETTER_DEFAULT_NAME = "Letter.xml";

    /**
     * Identifier of 'DoImportCardFromXml' action to be used in system log
     */
    public static final String EVENT_ID = "IMPORT_CARD_FROM_XML";

    private Log logger = LogFactory.getLog(getClass());
	private Long packetCardId = null;

	private static Map<FormatType, TypeStandard> formatToTypeStandard;

	static {
		formatToTypeStandard = new HashMap<FormatType, TypeStandard>();
		formatToTypeStandard.put(FormatType.DELO, TypeStandard.DELO);
		formatToTypeStandard.put(FormatType.GOST, TypeStandard.GOST);
	}

    @Override
    public String getEvent() {
	return EVENT_ID;
    }

    @Override
    public Object processQuery() throws DataException {
		packetCardId = null;
	ImportCardFromXml action = (ImportCardFromXml) getAction();
		InputStream packetStream = action.getLetterSource();
	InputStream sourceStream = action.getSource();

		DataServiceFacade serviceBean = getDataServiceBean();
		CardHandler cardHandler = new CardHandler(serviceBean);
		ObjectId cardId = null;
		Packet packet = new Packet();
		try {
			cardId = cardHandler.createCard(packet);
			packet.setId(cardId.getId().toString());
		} catch (DMSIException ex) {
			throw new DataException("distribution.packetLoading.system", ex);
		}
		this.packetCardId = (Long) cardId.getId();

		Exception exception = null;
		try {
			fillPacket(packet, packetStream);
			cardHandler.updateCard(packet);
		} catch (DMSIException ex) {
			exception = ex;
		} catch (RuntimeException ex) {
			exception = ex;
		} catch (JAXBException ex) {
			exception = ex;
		}

		if (exception != null) {
			return exceptionOccurred(new DMSIException("packet.filling", exception));
		}

	byte[] streamData = new byte[0];
	try {
	    streamData = IOUtils.toByteArray(sourceStream);
	} catch (IOException e) {
			exception = e;
		} catch (RuntimeException e) {
			exception = e;
	}
		if (exception != null) {
			return exceptionOccurred(new DMSIException("source.reading", exception));
		}
	if (streamData.length == 0) {
			return exceptionOccurred(new DMSIException("source.empty"));
	}

		ImportCardFromXml.ImportCard importAction = resolveImportAction(packet.getType());
		importAction.setPacketCardId(this.packetCardId);
	    importAction.setStreamData(streamData);
	    return importAction;
	}

	private ImportCardFromXml.ImportCard exceptionOccurred(DMSIException exception) {
		logger.error(exception.getMessage(), exception);
		ExceptionOccurredAction exceptionOccurredAction = new ExceptionOccurredAction();
		exceptionOccurredAction.setErrorMessage(exception.getMessage());
		exceptionOccurredAction.setPacketCardId(this.packetCardId);
		return exceptionOccurredAction;
    }

	private void fillPacket(Packet packet, InputStream packetStream) throws JAXBException, DMSIException {
		if (packetStream == null) {
			fillPacketByDefaultValues(packet);
		} else {
			fillPacketByData(packet, packetStream);
    }
	}

	private Packet fillPacketByDefaultValues(Packet packet) {
		packet.setPacketUid(UUID.randomUUID().toString());
		packet.setType(TypeStandard.GOST);
		return packet;
    }

	private void fillPacketByData(Packet packet, InputStream packetStream) throws JAXBException, DMSIException {
		File packetData = readPacketSource(packetStream);
		byte[] data = packetData.getImage();
		JAXBContext context = JAXBContext.newInstance(ENVELOPE_PACKAGE);
		Unmarshaller um = context.createUnmarshaller();
		Letter letter = (Letter) um.unmarshal(new ByteArrayInputStream(data));
		packet.setPacketData(packetData);
		packet.setDate(letter.getDate());
		packet.setPacketUid(letter.getId());
		packet.setMessageUid(letter.getMessageId());
		packet.setType(convertFormatToStandard(letter.getFormat()));

		Letter.Sender letterSender = letter.getSender();
		if (letterSender != null) {
			Organization sender = new Organization();
			sender.setUuid(letterSender.getGuid());
			sender.setFullName(letterSender.getName());
			packet.setSender(sender);
		}
	}

	private File readPacketSource(InputStream packetStream) throws DMSIException {
		byte[] packetData = new byte[0];
		try {
			packetData = IOUtils.toByteArray(packetStream);
		} catch (IOException e) {
			throw new DMSIException("The error during packet letter reading is occurred", e);
		}
		if (packetData.length == 0) {
			throw new DMSIException("The error during packet letter uploading is occurred. There is no data in letter.");
		}

		File packetFile = new File();
		packetFile.setFileName(LETTER_DEFAULT_NAME);
		packetFile.setImage(packetData);
		return packetFile;
	}

	private TypeStandard convertFormatToStandard(FormatType type) {
		return formatToTypeStandard.get(type);
	}

	private ImportCardFromXml.ImportCard resolveImportAction(TypeStandard typeStandard) {
		ImportCardFromXml.ImportCard result = null;

		if (typeStandard == null) {
			result = new ImportCardByGOST();
		} else if (typeStandard.equals(TypeStandard.DELO)) {
			result = new ImportCardByDelo();
		} else if (typeStandard.equals(TypeStandard.GOST)) {
			result = new ImportCardByGOST();
		}
		return result;
	}

	private DataServiceFacade getDataServiceBean() {
		DataServiceFacade serviceBean = new DataServiceFacade();
		serviceBean.setUser(getUser());
		serviceBean.setDatabase(getDatabase());
		serviceBean.setQueryFactory(getQueryFactory());
	return serviceBean;
    }
}
