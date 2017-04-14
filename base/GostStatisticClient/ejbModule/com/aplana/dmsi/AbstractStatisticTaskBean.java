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

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.util.ServiceUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.ejb.support.AbstractStatelessSessionBean;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public abstract class AbstractStatisticTaskBean extends AbstractStatelessSessionBean implements SessionBean {

	private static final long serialVersionUID = 1L;
	
	private static final Log logger = LogFactory.getLog(AbstractStatisticTaskBean.class);
	
	protected static final String USERNAME_PROPERTY = "username";
	protected static final String PASSWORD_PROPERTY = "password";
	protected static final String SERVER_PROPERTY = "server";
	
	public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy");
	public static final String REPORT_EXTENSION = ".xls";
	
	public static final ObjectId TEMPLATE_FILE = ObjectId.predefined(Template.class, "jbr.file");
	public static final ObjectId STRING_MATERIAL_NAME = ObjectId.predefined(StringAttribute.class, "jbr.materialName");
	public static final ObjectId TEMPLATE_STATISTICS = ObjectId.predefined(Template.class, "jbr.statistics");
	public static final ObjectId CARDLINK_ATTACHMENT = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	public static final ObjectId TEXTATTRIBUTE_DESCRIPTION = ObjectId.predefined(TextAttribute.class, "descr");
	public static final ObjectId STATISTICS_TYPE = ObjectId.predefined(ListAttribute.class, "jbr.statistics.type");
	
	private Set<String> addresses;
	private String username;
	private String password;
	
	private ObjectId statisticsType;
	
	private static DataServiceFacade serviceBean;
	
	@Override
    public void setSessionContext(SessionContext sessionContext) {
		super.setSessionContext(sessionContext);
		setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
		setBeanFactoryLocatorKey("businessBeanFactory");
    }

	@Override
    protected void onEjbCreate() throws CreateException {
		setServiceBean((DataServiceFacade) getBeanFactory().getBean("systemDataServiceFacade"));
    }
	
	protected void readProperties(String fileName) throws IllegalStateException {
		Properties options;
		try {
			URL configUrl = Portal.getFactory().getConfigService().getConfigFileUrl(fileName);
			options = PropertiesLoaderUtils.loadProperties(new UrlResource(configUrl));
			setParameters(options);
		} catch (IOException ex) {
			throw new IllegalStateException("It is impossible to read properties from " + fileName, ex);
		}
	}
	
	protected void setParameters(Properties options) {
		setUsername(options.getProperty(USERNAME_PROPERTY));
		setPassword(options.getProperty(PASSWORD_PROPERTY));

		setAddresses(new HashSet<String>());
		for (String prop : options.stringPropertyNames()) {
			if (prop.startsWith(SERVER_PROPERTY)) {
				String address = options.getProperty(prop);
				getAddresses().add(address);
			}
		}
	}
	
	protected void createStatisticCard(Card materialCard, Exception reason) throws Exception{
		CreateCard createCardAction = new CreateCard(TEMPLATE_STATISTICS);
		Card gostStatCard = getServiceBean().doAction(createCardAction);
		TextAttribute descriptionAttr = gostStatCard.getAttributeById(TEXTATTRIBUTE_DESCRIPTION);
		
		ListAttribute type = gostStatCard.getAttributeById(STATISTICS_TYPE);
		ReferenceValue value = new ReferenceValue();
		value.setId(getStatisticsType());
		type.setValue(value);

		CardLinkAttribute file = gostStatCard.getAttributeById(CARDLINK_ATTACHMENT);
		file.setLinkedCardLabelText(materialCard.getId(), "report");

		if (reason != null) {
			descriptionAttr.setValue("Processed with exception " + reason);
		} else {
			descriptionAttr.setValue("Completed successfully");
		}

		try {
			getServiceBean().saveObject(gostStatCard);
		} catch (DataException e) {
			logger.error("Cannot create GostStatisticCard due to following exception: ", e);
		}

		String cardId = "";
		try {
			cardId = materialCard.getId() != null ? materialCard.getId().getId().toString() : "null";
			releaseCardLock(materialCard);
			
			cardId = gostStatCard.getId() != null ? materialCard.getId().getId().toString() : "null";
			releaseCardLock(gostStatCard);
		} catch(DataException e) {
			logger.error("An error has occured while trying to unlock card: "+cardId+" due to the following exception: ", e);
		}
		
	}
	
	private void releaseCardLock(Card card) throws DataException {
		UnlockObject unlockAction = new UnlockObject();
		unlockAction.setId(card.getId());
		getServiceBean().doAction(unlockAction);
	}
	
	protected Card createNewMaterialCard(InputStream reportStream, String reportName) throws Exception {
		try {
			CreateCard createCardAction = new CreateCard(TEMPLATE_FILE);
			Card newCard = getServiceBean().doAction(createCardAction);
			StringAttribute materialNameAttr = newCard.getAttributeById(STRING_MATERIAL_NAME);
			materialNameAttr.setValue(reportName);
			ObjectId cardId = getServiceBean().saveObject(newCard);
			newCard.setId(cardId);
			Material material = new Material();
			material.setName(reportName);
			material.setData(reportStream);
			ServiceUtils.uploadMaterial(getServiceBean(), cardId, material);
			return newCard;
		} finally {
			IOUtils.closeQuietly(reportStream);
		}
	}

	public abstract void process(Map<?, ?> parameters);
	
	public Set<String> getAddresses() {
		return addresses;
	}

	public void setAddresses(Set<String> addresses) {
		this.addresses = addresses;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public DataServiceFacade getServiceBean() {
		return serviceBean;
	}

	public void setServiceBean(DataServiceFacade serviceBean) {
		AbstractStatisticTaskBean.serviceBean = serviceBean;
	}

	public ObjectId getStatisticsType() {
		return statisticsType;
	}

	public void setStatisticsType(ObjectId statisticsType) {
		this.statisticsType = statisticsType;
	}
	
}
