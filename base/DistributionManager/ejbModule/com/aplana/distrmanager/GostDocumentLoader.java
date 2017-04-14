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
package com.aplana.distrmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ImportCardFromXml;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.ImportCardFromXml.ImportCard.Result;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.LoadDoc.DocumentLoader;
import com.aplana.distrmanager.exceptions.DistributionManagerException;
import com.aplana.distrmanager.exceptions.LoadingException;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.card.CardHandler;
import com.aplana.dmsi.types.common.Packet;
import com.aplana.dmsi.util.ServiceUtils;

class GostDocumentLoader implements DocumentLoader {

	private DataServiceFacade serviceBean;
	private File workingDirectory;
	private static final WorkflowMove unsuccessfulWorkflowMove = WorkflowMove.createFromId(ObjectId
			.workflowMove("jbr.importedDoc.loadFailed"));

	public void setServiceBean(DataServiceFacade serviceBean) {
		this.serviceBean = serviceBean;
	}
	
	public ObjectId load(File passportFile, File letterFile) throws DataException, FileNotFoundException, LoadingException {
		workingDirectory = passportFile.getParentFile();
		ImportCardFromXml.ImportCard importAction;
		importAction = loadPassportIntoSystem(passportFile, letterFile);
		try {
			Result importResult = serviceBean.doAction(importAction);
			if (!importResult.isResultSuccessful()) {
				throw new LoadingException(importResult.getStatusDescription().getResult());
			}
			uploadFiles(importResult.getPaths());
			return importResult.getCardId();
		} catch (DistributionManagerException ex) {
			//��� ����������� ����������� ������ �� ����� �������� �����������
			updateProcessingResult(importAction.getPacketCardId(), ex.getLocalizedMessage());
			throw new LoadingException(ex.toString(), ex);
		} catch (DataException de) {
			//����� ��� ��� ������ ��������� ����������� ����������� �������� �� ����� IN � discarded 
			//��������� ��������� � �������
			updateProcessingResult(importAction.getPacketCardId(), de.getLocalizedMessage());
			throw de;
		}
	}
	
	private ImportCardFromXml.ImportCard loadPassportIntoSystem(File passportFile, File letterFile)
			throws FileNotFoundException, DataException {
		InputStream passportStream = null;
		InputStream letterStream = null;
		try {
			ImportCardFromXml action = new ImportCardFromXml();
			passportStream = new FileInputStream(passportFile);
			action.setSource(passportStream);
			if (letterFile != null) {
				letterStream = new FileInputStream(letterFile);
				action.setLetterSource(letterStream);
			}
			action.setFileName(passportFile.getName());
			return serviceBean.doAction(action);
		} finally {
			IOUtils.closeQuietly(passportStream);
			IOUtils.closeQuietly(letterStream);
		}
	}

	private void uploadFiles(Map<ObjectId, String> files) throws FileNotFoundException {
		for (Entry<ObjectId, String> cardFile : files.entrySet()) {
			ObjectId cardId = cardFile.getKey();
			String fileName = cardFile.getValue();
			File sourceFile = new File(fileName);
			if (!sourceFile.isAbsolute()) {
				sourceFile = new File(workingDirectory, fileName);
			}
			uploadFile(cardId, sourceFile);
		}
	}

	private void uploadFile(ObjectId cardId, File sourceFile) throws FileNotFoundException {
		InputStream fileStream = null;
		try {
			Material material = new Material();
			material.setName(sourceFile.getName());
			fileStream = new FileInputStream(sourceFile);
			material.setData(fileStream);
			ServiceUtils.uploadMaterial(serviceBean, cardId, material);
		} finally {
			IOUtils.closeQuietly(fileStream);
		}
	}

	private void changeState(long cardId, WorkflowMove workflowMove) throws DataException {
		DataServiceFacade dataService = serviceBean;
		ObjectId cardObjectId =  new ObjectId(Card.class, cardId);
		Card card = DataObject.createFromId(cardObjectId);
		LockObject lock = new LockObject(card);
		dataService.doAction(lock);
		try {
			ChangeState changeStateAction = new ChangeState();
			changeStateAction.setCard(card);
			changeStateAction.setWorkflowMove(workflowMove);
			dataService.doAction(changeStateAction);
		} finally {
			UnlockObject unlock = new UnlockObject(card);
			dataService.doAction(unlock);
		}
	}
	
	/*
	 * ��������� ��������� ��������� ��������� ���������:
	 * ���������� ��������� �� ������ � ��������� � ������ ���������� (� �������)
	 */
	private void updateProcessingResult(long packetCardId, String result) throws DataException {
		try {
			Packet packet = new Packet();
			packet.setId(String.valueOf(packetCardId));
			packet.setProcessingResult(result);
			CardHandler cardHandler = new CardHandler( serviceBean );
			cardHandler.updateCard(packet);
		} catch (DMSIException e) {
			throw new DataException("distribution.resultProcessing", new Object[] {packetCardId}, e);
		}

		changeState(packetCardId, unsuccessfulWorkflowMove);
	}
}