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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringUtils;

import com.aplana.dbmi.action.ExportCardToXml.Result;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.expansion.ExpansionProcessor;
import com.aplana.dmsi.object.DMSIObjectFactory;
import com.aplana.dmsi.types.AckResult;
import com.aplana.dmsi.types.AcknowledgementType;
import com.aplana.dmsi.types.ExportedDocumentType;
import com.aplana.dmsi.types.Header;
import com.aplana.dmsi.types.HeaderMessageEnumType;

public class GOSTExporter {

	private Processor processor;
	private DataServiceFacade dataServiceBean;
	private Map<HeaderMessageEnumType, List<Processor>> processorsByType = new HashMap<HeaderMessageEnumType, List<Processor>>();
	private ObjectId cardId;
	private ObjectId headerCardId;
	private ExpansionProcessor expansionProcessor;

	public DataServiceFacade getDataServiceBean() {
		return this.dataServiceBean;
	}

	public void setDataServiceBean(DataServiceFacade serviceBean) {
		this.dataServiceBean = serviceBean;
	}

	public ObjectId getCardId() {
		return this.cardId;
	}

	public void setCardId(ObjectId cardId) {
		this.cardId = cardId;
	}

	public ObjectId getHeaderCardId() {
		return this.headerCardId;
	}

	public void setHeaderCardId(ObjectId headerCardId) {
		this.headerCardId = headerCardId;
	}

	public ExpansionProcessor getExpansionProcessor() {
		return this.expansionProcessor;
	}

	public void setExpansionProcessor(ExpansionProcessor expansionProcessor) {
		this.expansionProcessor = expansionProcessor;
	}

	public static interface Processor {
		Map<ObjectId, String> collectFiles(Header header);

		void postProcess(Header header) throws DMSIException;

		Map<String, Object> getAdditions(Header header);
	}

	public Processor getProcessor() {
		return this.processor;
	}

	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	public List<Processor> getProcessors(HeaderMessageEnumType type) {
		List<Processor> typeProcessors = processorsByType.get(type);
		if (typeProcessors == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(typeProcessors);
	}

	public void addProcessor(HeaderMessageEnumType type, Processor typeProcessor) {
		if (type == null) {
			return;
		}
		List<Processor> typeProcessors = processorsByType.get(type);
		if (typeProcessors == null) {
			typeProcessors = new ArrayList<Processor>();
			processorsByType.put(type, typeProcessors);
		}
		typeProcessors.add(typeProcessor);
	}

	public Result exportCard() throws DataException {
		checkState();
		try {
			Configuration config = Configuration.instance();
			JAXBContext context = config.getJAXBContext();
			Marshaller m = context.createMarshaller();
			Header header = createHeader();
			if (expansionProcessor != null) {
				expansionProcessor.fillExpansion(dataServiceBean, header, cardId);
			}
			Map<ObjectId, String> files = Collections.emptyMap();
			if (processor != null) {
				postProcess(header);
				files = processor.collectFiles(header);
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(header, os);
			InputStream is = new ByteArrayInputStream(os.toByteArray());
			os.close();
			Result result = new Result(is, files);
			if (processor != null) {
				Map<String, Object> infos = processor.getAdditions(header);
				for (Entry<String, Object> infoEntry : infos.entrySet()) {
					result.addInfo(infoEntry.getKey(), infoEntry.getValue());
				}
			}
			return result;
		} catch (JAXBException ex) {
			throw new DataException(ex);
		} catch (IOException ex) {
			throw new DataException(ex);
		} catch (DMSIException ex) {
			throw new DataException(ex);
		}
	}

	private void checkState() {
		if (this.dataServiceBean == null) {
			throw new IllegalStateException("Data service should be defined before using");
		}
		if (this.cardId == null) {
			throw new IllegalStateException("Card id for export should be defined before using");
		}
	}

	protected Header createHeader() throws DMSIException {
		Header header = exportHeader();
		HeaderMessageEnumType messageType = header.getMsgType();
		switch (messageType) {
		case DOCUMENT:
			ExportedDocumentType doc = createDocument();
			header.setDocument(doc);
			break;
		case ACKNOWLEDGEMENT:
			AcknowledgementType ack = createAcknowledgement();
			header.setAcknowledgement(ack);
			break;
		default:
			throw new UnsupportedOperationException(messageType + " message type is not supported now");
		}
		return header;
	}

	protected Header exportHeader() throws DMSIException {
		if (this.headerCardId == null) {
			throw new IllegalStateException("Header card id should be defined before using");
		}
		DMSIObjectFactory headerFactory = DMSIObjectFactory.newInstance(getDataServiceBean(), "Header");
		return (Header) headerFactory.newDMSIObject(this.headerCardId);
	}

	protected void postProcess(Header header) throws DMSIException {
		List<Processor> typeProcessors = getProcessors(header.getMsgType());
		for (Processor typeProcessor : typeProcessors) {
			typeProcessor.postProcess(header);
		}
		if (processor != null) {
			processor.postProcess(header);
		}
    }

	private ExportedDocumentType createDocument() throws DMSIException {
		DataServiceFacade serviceBean = getDataServiceBean();
		DMSIObjectFactory objectFactory = DMSIObjectFactory.newInstance(serviceBean, "Document");
		return (ExportedDocumentType) objectFactory.newDMSIObject(cardId);
    }

	private AcknowledgementType createAcknowledgement() throws DMSIException {
		DataServiceFacade serviceBean = getDataServiceBean();
		DMSIObjectFactory objectFactory = DMSIObjectFactory.newInstance(serviceBean, "Acknowledgement");
		AcknowledgementType ack = (AcknowledgementType) objectFactory.newDMSIObject(cardId);

		String errorDescription = ack.getAckResultDescription();
		Long errorCode = ack.getAckResultErrorCode();

		AckResult ackResult = new AckResult();
		ack.getAckResult().add(ackResult);
		if (!StringUtils.isEmpty(errorDescription)) {
			ackResult.setValue(errorDescription);
		}
		if (errorCode != null) {
			ackResult.setErrorcode(BigInteger.valueOf(errorCode));
		}
		return ack;
	}

	public static ObjectId getCardId(String id) {
		try {
			return new ObjectId(Card.class, Long.parseLong(id));
		} catch (NumberFormatException ex) {
			throw new IllegalStateException("Id of card is invalid " + id);
		}
	}

}
