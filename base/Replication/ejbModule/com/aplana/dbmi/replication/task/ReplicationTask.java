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
package com.aplana.dbmi.replication.task;

import com.aplana.dbmi.action.*;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.replication.action.CreateReplicationPackage;
import com.aplana.dbmi.replication.action.HandlingPackage;
import com.aplana.dbmi.replication.packageconfig.PackageType;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Card.Attribute;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Card.Attribute.MaterialValue;
import com.aplana.dbmi.replication.processors.ReplicationConfiguration;
import com.aplana.dbmi.replication.tool.CardRelationUtils;
import com.aplana.dbmi.replication.tool.ReplicationNotificationHandler;
import com.aplana.dbmi.replication.tool.ReplicationRepeatLoad;
import com.aplana.dbmi.replication.tool.ReplicationUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.SystemUser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.ejb.support.AbstractStatelessSessionBean;

import javax.ejb.CreateException;
import javax.ejb.SessionContext;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Session Bean implementation class ReplicationTask
 */
public class ReplicationTask extends AbstractStatelessSessionBean {

	public static final int REPLICATION_FLAG_FALSE = 0;
	public static final int REPLICATION_FLAG_TRUE = 1;
	public static final String REPLICATION_PACKAGE_NAME = "replication-package.xml";

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ReplicationTask.class);
	private static final String LOCK_FILE = "folder.lock";
	private static final String LOG_FILE_SUFFIX = ".log";

	private File inFolder;
	private File failFolder;
	private DataServiceBean serviceBean;
	private static Boolean working = false;

	@Override
	public void setSessionContext(SessionContext sessionContext) {
		super.setSessionContext(sessionContext);
		setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
		setBeanFactoryLocatorKey("businessBeanFactory");
	}

	@Override
	protected void onEjbCreate() throws CreateException {
		logger.info("Create ReplicationTaskBean");
		serviceBean = createServiceBean();
		readConfig();
	}

	public void process(Map<?, ?> parameters) {
		synchronized (ReplicationTask.class) {
			if (working)
				return;
			working = true;
		}
		try {
			doProcess();
		} finally {
			synchronized (ReplicationTask.class) {
				working = false;
			}
		}
	}

	public void doProcess() {
		try {
			logger.info("Start ReplicationTask");

			if (inFolder == null || !inFolder.exists()) {
				logger.warn("Unable to find Incoming directory!");
				return;
			}
			//получаем директории без блокировки
			File[] subDirectories = inFolder.listFiles(new FileFilter() {
				public boolean accept(File file) {
					return file.isDirectory() && !isDirectoryLocked(file);
				}
			});
			logger.info("Found directory: " + subDirectories.length);
			//Сортировка
			Arrays.sort(subDirectories);

			//обработка доступных файлов в директориях
			for (File processingDirectory : subDirectories) {
				logger.info("Working with folder " + processingDirectory.getName());

				File[] files = processingDirectory.listFiles(new FilenameFilter() {
					public boolean accept(File directory, String name) {
						return isReplicationXMLFile(name);
					}
				});
				if (files == null || files.length == 0) {
					logger.warn("Not found any XML files in directory " + processingDirectory.getAbsolutePath());
					continue;
				}
				File folderLockFile = new File(processingDirectory, LOCK_FILE);
				folderLockFile.createNewFile();
				for (File file : files) {
					logger.info("Working with file " + file.getName());
					try {
						createObjectFromFile(file, processingDirectory);
					} catch (Exception error) {
						logger.error("An exception occurred while processing the file " + processingDirectory.getAbsolutePath() + File.separator + file.getName(), error);
						folderLockFile.delete();
						storePackage(processingDirectory, error);
					}
				}
				FileUtils.deleteDirectory(processingDirectory);
			}

			ReplicationRepeatLoad replicRepLOad = new ReplicationRepeatLoad(serviceBean);
			int repeat = replicRepLOad.repeatLoad();

			logger.info("Repeat Load: " + repeat);
			logger.info("Finish ReplicationTask");
		} catch (Exception ex) {
			logger.error("Error on ReplicationTask", ex);
		}
	}

	private DataServiceBean createServiceBean() {
		DataServiceBean serviceBean = new DataServiceBean();
		serviceBean.setUser(new SystemUser());
		serviceBean.setAddress("localhost");
		serviceBean.setSessionId(String.valueOf(Thread.currentThread().getId()));
		return serviceBean;
	}

	private void readConfig() {
		logger.info("Read config for ReplicationTask");
		try {
			inFolder = new File(ReplicationConfiguration.getReplicationNodeConfig().getIncomingFolder());
			failFolder = new File(ReplicationConfiguration.getReplicationNodeConfig().getFailureFolder());
		} catch (Exception ex) {
			logger.warn("Error read ReplicationTask config.", ex);
		}
	}

	private boolean isDirectoryLocked(File directory) {
		File lockFile = new File(directory, LOCK_FILE);
		return lockFile.exists();
	}

	private boolean isReplicationXMLFile(String name) {
		return REPLICATION_PACKAGE_NAME.equals(name);
	}

	private void createObjectFromFile(File file, File folder) throws Exception {
		ReplicationPackage replicationPackage;
		try {
			replicationPackage = ReplicationUtils.fileToPackage(file);
		} catch (Exception error) {
			throw new Exception("Error while unmarshal XML file into ReplicationPackage model", error);
		}

		if (!isPackageForThisSystem(replicationPackage.getAddressee())) {
			throw new Exception("Replication package is not for this system. Wrong <Addressee> UUID: " + replicationPackage.getAddressee());
		} else {
			//меняем местами получателя и отправителя
			String addresee = replicationPackage.getAddressee();
			replicationPackage.setAddressee(replicationPackage.getSender());
			replicationPackage.setSender(addresee);
		}

		if (!createMaterialsFromFile(replicationPackage, folder)) {
			throw new Exception("Cannot create materials from this replication package.");
		}

		if (replicationPackage.getPackageType().equals(PackageType.CARD)) {
			HandlingPackage handlingPackage = new HandlingPackage();
			handlingPackage.setPackageXml(replicationPackage);
			Card card = serviceBean.doAction(handlingPackage);
			if (!card.getState().equals(IdUtils.makeStateId("jbr.replication.notProcessed"))) {
				card = setCardState(card);
			}
		} else if (replicationPackage.getPackageType().equals(PackageType.REQUEST)) {
			for (String guid : replicationPackage.getIncompleteCards().getCardGuid()) {
				Search search = new Search();
				search.setByAttributes(true);
				search.addStringAttribute(CardRelationUtils.REPLICATION_UUID, guid);
				SearchResult sr = serviceBean.doAction(search);
				List<Card> cards = sr.getCards();
				if (cards.size() == 1) {
					CreateReplicationPackage createReplicationPackage = new CreateReplicationPackage();
					createReplicationPackage.setCard(serviceBean.<Card>getById(cards.get(0).getId()));
					createReplicationPackage.setAddressee(replicationPackage.getAddressee());
					createReplicationPackage.setPackageType(PackageType.CARD);
					createReplicationPackage.setUpdateVersion(false);
					serviceBean.doAction(createReplicationPackage);
				}
			}
		} else if (replicationPackage.getPackageType().equals(PackageType.RESPONSE)) {
			HandlingPackage handlingPackage = new HandlingPackage();
			handlingPackage.setPackageXml(replicationPackage);
			Card card = serviceBean.doAction(handlingPackage);

			if (!card.getState().equals(IdUtils.makeStateId("jbr.replication.notProcessed"))) {
				card = setCardState(card);
			}
		} else if (replicationPackage.getPackageType().equals(PackageType.COLLISION)) {// обработка коллизий
			HandlingPackage handlingPackage = new HandlingPackage();
			handlingPackage.setPackageXml(replicationPackage);
			Card card = serviceBean.doAction(handlingPackage);
			if (!card.getState().equals(IdUtils.makeStateId("jbr.replication.notProcessed"))) {
				card = setCardState(card);
			}
		} else if (replicationPackage.getPackageType().equals(PackageType.STATUS)) {// обработка пришедших уведомлений
			ReplicationNotificationHandler handler = new ReplicationNotificationHandler(serviceBean);
			handler.processingNotification(replicationPackage);
		}
	}

	protected boolean isPackageForThisSystem(String addressee) throws JAXBException, IOException {
		if (addressee.equals(ReplicationConfiguration.getReplicationNodeConfig().getServerGUID())) {
			return true;
		}
		if (ReplicationConfiguration.getReplicationNodeConfig().getOrganizations() != null) {
			for (String orgUid : ReplicationConfiguration.getReplicationNodeConfig().getOrganizations().getGUID()) {
				if (addressee.equals(orgUid)) {
					return true;
				}
			}
		}
		return false;
	}

	private Card setCardState(Card card) throws DataException, ServiceException {
		GetWorkflowMovesFromTargetState getWorkflowMovesAction = new GetWorkflowMovesFromTargetState();
		getWorkflowMovesAction.setCard(card);
		getWorkflowMovesAction.setToStateId(IdUtils.makeStateId("jbr.replication.notProcessed"));
		List<Long> moveIds = serviceBean.doAction(getWorkflowMovesAction);

		WorkflowMove wfMove = serviceBean.getById(new ObjectId(WorkflowMove.class, moveIds.get(0)));

		ChangeState changeState = new ChangeState();
		changeState.setCard(card);
		changeState.setWorkflowMove(wfMove);
		serviceBean.doAction(changeState);
		return card;
	}

	// загрузка материала
	private boolean createMaterialsFromFile(ReplicationPackage rpg, File folder) throws DataException, ServiceException, IOException {
		boolean result = true;
		PackageType type = rpg.getPackageType();
		if (type.equals(PackageType.CARD) || type.equals(PackageType.COLLISION) || type.equals(PackageType.RESPONSE)) {
			for (Attribute replicAttr : rpg.getCard().getAttribute()) {
				if (!replicAttr.getMaterialValue().isEmpty()) {
					List<MaterialValue> listMaterial = replicAttr.getMaterialValue();
					for (MaterialValue materialValue : listMaterial) {
						final String fileName = materialValue.getFile();
						File[] files = folder.listFiles(new FilenameFilter() {
							public boolean accept(File dir, String filename) {
								return filename.equals(fileName);
							}
						});
						if (files.length != 1) {
							logger.error("Material with " + fileName + " name  not found");
							result = false;
						} else {
							File material = files[0];
							InputStream is = new FileInputStream(material);
							try {
								CreateCard createCard = new CreateCard(ObjectId.template("jbr.file"));
								Card fileCard = serviceBean.doAction(createCard);
								fileCard.<StringAttribute>getAttributeById(ObjectId.predefined(StringAttribute.class, "jbr.materialName"))
										.setValue(fileName);
								ObjectId fileCardId = serviceBean.saveObject(fileCard);
								fileCard = serviceBean.getById(fileCardId);
								if (fileCard.isLocked()) {
									UnlockObject uo = new UnlockObject(fileCard);
									serviceBean.doAction(uo);
								}
								UploadFile uploadFile = new UploadFile();
								uploadFile.setCardId(fileCard.getId());
								uploadFile.setFileName(materialValue.getName());
								uploadFile.setData(is);
								serviceBean.doAction(uploadFile);
							} catch (Exception ex) {
								logger.error("Error on create material.", ex);
								result = false;
							} finally {
								is.close();
							}
						}
						if (!result) {
							break;
						}
					}
				}
				if (!result) {
					break;
				}
			}
		}
		return result;
	}

	private void storePackage(File currentFolder, Exception error) {
		PrintWriter output = null;
		try {
			File storeDirectory = new File(failFolder + File.separator + currentFolder.getName());
			FileUtils.copyDirectory(currentFolder, storeDirectory);
			logger.info("Unprocessed replication package saved into the folder " + storeDirectory.getAbsolutePath());
			File logFile = new File(failFolder, currentFolder.getName() + LOG_FILE_SUFFIX);
			logFile.createNewFile();
			output = new PrintWriter(logFile);
			error.printStackTrace(output);
		} catch (IOException exception) {
			logger.error("Cannot store unprocessed replication package", exception);
		} finally {
			IOUtils.closeQuietly(output);
		}
	}
}