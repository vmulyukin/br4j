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
package com.aplana.dmsi.processors;

import com.aplana.dbmi.action.ImportCardFromXml;
import com.aplana.dbmi.model.TypeStandard;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.ObjectFactory;
import com.aplana.dmsi.action.ExceptionOccurredAction;
import com.aplana.dmsi.action.ImportCardByDelo;
import com.aplana.dmsi.action.ImportCardByGOST;
import com.aplana.dmsi.card.CardHandler;
import com.aplana.dmsi.types.Header;
import com.aplana.dmsi.types.common.File;
import com.aplana.dmsi.types.common.Packet;

public class CreateCardForHeader extends ProcessorBase {

	private static final long serialVersionUID = 1L;
	private Long packetCardId = null;

	@Override
	public Object process() throws DataException {
		Exception exception = null;
		try {
			return doProcess();
		} catch (DMSIException ex) {
			exception = ex;
		} catch (RuntimeException ex) {
			exception = ex;
		}
		return exceptionOccurred(new DMSIException("header.loading", exception));
	}

	private Object doProcess() throws DMSIException {
		ImportCardFromXml action = (ImportCardFromXml) getAction();
		ImportCardFromXml.ImportCard importAction = (ImportCardFromXml.ImportCard) getResult();
		this.packetCardId = importAction.getPacketCardId();
		TypeStandard typeStandard = null;

		if (importAction instanceof ImportCardByDelo) {
			typeStandard = TypeStandard.DELO;
		} else if (importAction instanceof ImportCardByGOST) {
			typeStandard = TypeStandard.GOST;
		} else {
			return importAction;
		}

		ObjectFactory.startWork(typeStandard);
		Header header = ObjectFactory.createHeader();

		byte[] streamData = importAction.getStreamData();
		File sourceFile = new File();
		sourceFile.setImage(streamData);
		String sourceFileName = action.getFileName();
		sourceFile.setFileName(sourceFileName);

		header.setSourceFile(sourceFile);
		header.setName(sourceFileName);

		Packet packet = new Packet();
		packet.setId(Long.toString(this.packetCardId));
		packet.setHeader(header);
		ObjectFactory.finishWork();

		DataServiceFacade serviceBean = getDataServiceBean();
		CardHandler cardHandler = new CardHandler(serviceBean);
		cardHandler.updateCard(packet);

		if (importAction instanceof ImportCardByDelo) {
			((ImportCardByDelo) importAction).setImportedDocCardId(Long.valueOf(header.getId()));
		} else if (importAction instanceof ImportCardByGOST) {
			((ImportCardByGOST) importAction).setGostMessageCardId(Long.valueOf(header.getId()));
		}
		return importAction;
	}

	private ImportCardFromXml.ImportCard exceptionOccurred(DMSIException exception) {
		logger.error(exception.getMessage(), exception);
		ExceptionOccurredAction exceptionOccurredAction = new ExceptionOccurredAction();
		exceptionOccurredAction.setErrorMessage(exception.getMessage());
		exceptionOccurredAction.setPacketCardId(this.packetCardId);
		return exceptionOccurredAction;
	}

	private DataServiceFacade getDataServiceBean() {
		DataServiceFacade serviceBean = new DataServiceFacade();
		serviceBean.setUser(getUser());
		serviceBean.setDatabase(getDatabase());
		serviceBean.setQueryFactory(getQueryFactory());
		return serviceBean;
	}

}
