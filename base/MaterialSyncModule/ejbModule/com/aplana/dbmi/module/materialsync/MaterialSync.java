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
package com.aplana.dbmi.module.materialsync;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import java.io.IOException;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.ejb.support.AbstractStatelessSessionBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.SystemUser;

/*
 *  ��������� ����� ����������� � ����� Inbox � ���������.
 *  �������� ����� process, ������� ����������� �� ����������
 *  ����� ������� � ����� Inbox, ����������� � ����� Bad,
 *  ������������ ����� ������� �������� � ���������. ���� ���������
 *  ���� � �������� �������, �� ���� ��������� �� Bad, ����� ���� ��������.
 *  id �������� � ������� ����� ���������� ���� ������������ �� �������� �����,
 *  ������� ������� ����� ��������� ������ APO[cardID]_ddmmyyhh24misss.pdf, 
 *  ��� ������� "[" � "]" �����������
 *  
 *  ���������������� ���� CONFIG_FILE ������ �������������� ����� Inbox � Bad
 */
public class MaterialSync extends AbstractStatelessSessionBean implements SessionBean {

	static final long serialVersionUID = 1L;
	
	private final Log logger = LogFactory.getLog(getClass());

	private static final String CONFIG_FILE = "dbmi/materialsync/config.xml";
	private static final ObjectId TEMPLATE_FILE =
		ObjectId.predefined(Template.class, "jbr.file");
	private static final ObjectId ATTR_DOCLINKS = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.files");
	private static final ObjectId ATTR_MATERIAL_NAME = ObjectId.predefined(
			StringAttribute.class, "jbr.materialName");

	private AsyncDataServiceBean serviceBean;
	private List<Map<String, String>> folders; // �������� �������������� ���������� Inbox � Bad
	private String inFolder = null;
	private String badFolder = null;
	private List<String> nameFiles;

	
	public MaterialSync() {
		setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
		setBeanFactoryLocatorKey("businessBeanFactory");
	}
	
	// ������������� �������� ����� ������������ ����� �������� process
	protected void onEjbCreate() throws CreateException {
		// ��������� serviceBean
		serviceBean = createServiceBean();
		// �������� �������� �������������� ���������� Inbox � Bad
		try {
			readConfig();
		} catch (Exception e) { 
			logger.error("Error reading configuration file " + CONFIG_FILE, e);
			return;
		}
		// ���������� ����� �� ��������� �������������� ����������
		// Inbox � Bad ��� ��������
		for (Map<String, String> varian : folders) {
			File inDir = new File(varian.get("in"));
			logger.info(inDir);
			File badDir = new File(varian.get("bad"));
			logger.info(badDir);
			if (inDir.canRead() && badDir.canRead()) {
				inFolder = inDir.getPath();
				badFolder = badDir.getPath();
				break;
			}
		}
	}

	// �������� �����, ����������� �� ���������� � ����������� ��� ������
	public void process(Map<?,?> parameters) {
		refreshSessionId();
		logger.debug("Start task");
		if (inFolder == null || badFolder == null) {
			logger.error("Available path to the Inbox and Bad not specified");
			return;
		}
		//��������� ����� �� Inbox � Bad � ���������� ����� ������������ ������
		File inDir = new File(inFolder);
		File[] listFile = inDir.listFiles();
		nameFiles = new ArrayList<String>(listFile != null ? listFile.length : 0);
		for (File inFile : listFile) {
			String name = inFile.getName();
			File badFile = new File(badFolder, name);
			//boolean isSuccess = inFile.renameTo(badFile);
			try {
				FileUtils.moveFile(inFile, badFile);
				nameFiles.add(name);
			} catch (IOException ex) {
				// ������� ��������� � ��������� � ������������ �����
				logger.error(String.format("Unable to move the file %s into a directory %s", inFile, badFolder), ex);
			}
		}
		// �������� ������������ ����� �������� � ���������, ������ ������ �����������
		for (String name : nameFiles) {
			File file = new File(badFolder, name);
			// �������� �������� ���� � ��������
			boolean isSuccess = uploadFile(file);
			if (isSuccess) {
				// ������� ����������� � �������� ���� �� Bad
				deleteFile(file);
			} else {
				logger.error("Failed to add file "+file.getName()+" to target card ");
			}
		}
	}
	
	/* ��������� ��������� ���� � ��������. �������� � ������� ����� �������� ����
	 * ������������ �� ����� �����
	 */
	private boolean uploadFile(File file) {
			// �������� id ������� ��������
			ObjectId targetId;
			try {
				targetId = new ObjectId(Card.class, Long.valueOf(parse(checkHeader(file.getName()), PartOfFileName.CARD_ID)));
			} catch (Exception e) {
				logger.error("Unable to determine the id cards from name file "+ file.getName(), e);
				return false;
			}
			
			// ��������� ������� ��������
			Card targetCard;
			try {
				targetCard = (Card) serviceBean.getById(targetId);
			} catch (Exception e) {
				logger.error("Failed to load the card with the id "+ targetId.getId(), e);
				return false;
			}
			
			// ������� �������� ��������
			Card fileCard;
			try {
				fileCard = createFileCard();
			} catch (Exception e) {
				logger.error("Could not create card Material", e);
				return false;
			}
			
			// ��������� � �������� �������� ����
			try {
				attachFileToFileCard(fileCard, file);
			} catch (Exception e) {
				logger.error("Unable to download the file "+file.getName()+" to the card material", e);
				return false;
			}
			
			// ������������ �������� �������� �������� � ������ ������� ��������
			CardLinkAttribute docLinks = targetCard
					.getCardLinkAttributeById(ATTR_DOCLINKS);
			if (docLinks == null) {
				logger.error("Attribute "+ATTR_DOCLINKS+" in the target card "+targetId.getId()+" was not found");
				return false;
			}
			docLinks.addLabelLinkedCard(fileCard);
			
			// ��������� ������� ��������
			try {
				serviceBean.doAction(new LockObject(targetCard.getId()));
			} catch (Exception e) {
				logger.error("Unable to lock the card "+targetId.getId()+", to add material", e);
				return false;
			} 
			boolean isSuccess = true;
			try {
				serviceBean.saveObject(targetCard);
			} catch (Exception e) {
				logger.error("Failed to save target card "+targetId.getId(), e);
				isSuccess = false;
			} finally {
				try {
					serviceBean.doAction(new UnlockObject(targetCard.getId()));
				} catch (Exception e) {
					logger.error("Unable to unlock the card "+targetId.getId(), e);
				} 
			}
			return isSuccess;
	}
	
	private void deleteFile(File file) {
		file.delete();
	}

	private AsyncDataServiceBean createServiceBean() {
		AsyncDataServiceBean serviceBean = new AsyncDataServiceBean();
		serviceBean.setUser(new SystemUser());
		serviceBean.setAddress("localhost");
		return serviceBean;
	}

	// ����������� �� ������. ����� �������������� ����� Inbox � Bad
	private void readConfig() throws Exception {
		folders = new LinkedList<Map<String,String>>();
		InputStream input = Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILE);
		
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
		Element root = doc.getDocumentElement();
		NodeList folderList = root.getElementsByTagName("folder");
		for (int i=0; i < folderList.getLength(); i++) {
			Element folderEl = (Element)folderList.item(i);
			Map<String, String> map = new HashMap<String, String>();
			map.put("in", folderEl.getAttribute("in"));
			map.put("bad", folderEl.getAttribute("bad"));
			folders.add(map);
		}
	}
	
	// �������� ������� ��������
	private Card createFileCard() throws Exception {
		CreateCard createCardAction = new CreateCard();
		createCardAction.setTemplate(TEMPLATE_FILE);
		Card fileCard =  (Card)serviceBean.doAction(createCardAction);

		StringAttribute name = (StringAttribute)fileCard.getAttributeById(Attribute.ID_NAME);
		name.setValue("file");

		ObjectId fileCardId = serviceBean.saveObject(fileCard);
		fileCard.setId((Long)fileCardId.getId());
		serviceBean.doAction(new UnlockObject(fileCard));

		return fileCard;
	}
	
	// ���������� ����� file � �������� �������� �������� card 
	private void attachFileToFileCard(Card card, File file) throws FileNotFoundException, DataException, ServiceException {
		UploadFile uploadAction = new UploadFile();
		uploadAction.setCardId(card.getId());
		String fileName = file.getName();
		uploadAction.setFileName(fileName);
		uploadAction.setData(new BufferedInputStream(new FileInputStream(file)));
		serviceBean.doAction(uploadAction);
		
		MaterialAttribute attr = (MaterialAttribute) card.getAttributeById(Attribute.ID_MATERIAL);
		attr.setMaterialName(fileName);
		attr.setMaterialType(MaterialAttribute.MATERIAL_FILE);
		
		StringAttribute name = (StringAttribute) card.getAttributeById(Attribute.ID_NAME);
		name.setValue(fileName);
		
		StringAttribute materialNameAttr = (StringAttribute) card.getAttributeById(ATTR_MATERIAL_NAME);
		String materialName = parse(checkHeader(fileName), PartOfFileName.REAL_NAME);
		materialNameAttr.setValue(materialName);
		
		serviceBean.doAction(new LockObject(card));
		try {
			serviceBean.saveObject(card);
		} finally {
			serviceBean.doAction(new UnlockObject(card));
		}
	}
	
	
	private String checkHeader(String str) throws DataException {
		String header = str.substring(0, 3);
		if ("APO".equals(header)) {
			return str.substring(3);
		} else {
			throw new DataException();
		}
	}
	
	// ���������� id �������� �� ����� �����
	// str ���� APO[cardID]_ddmmyyhh24misss.pdf, ��� ������� "[" � "]" �����������
	private String parse(String str, PartOfFileName pofn) throws DataException {
		int under_index = str.indexOf("_");
		String strCardId;
		if(pofn == PartOfFileName.CARD_ID) {
			strCardId = str.substring(0, under_index);
		} else if(pofn == PartOfFileName.REAL_NAME) {
			strCardId = str.substring(under_index + 1, str.length());
		} else
			throw new DataException();
		return strCardId;		
	}
	
	private void refreshSessionId() {
		this.serviceBean.setSessionId(String.valueOf(Thread.currentThread().getId()));
	}
	
	enum PartOfFileName {
		CARD_ID, REAL_NAME
	}
}
