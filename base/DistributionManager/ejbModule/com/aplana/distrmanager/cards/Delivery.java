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
package com.aplana.distrmanager.cards;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.exceptions.SaveCardException;
import com.aplana.distrmanager.util.UtilsWorkingFiles;

public class Delivery {
	
	protected Log logger = LogFactory.getLog(getClass());
	
	public static final ObjectId UUID_ATTRIBUTE_ID = ObjectId.predefined(
		    TextAttribute.class, "uid");
	public static final ObjectId ERROR_CODE_ID = ObjectId.predefined(
	    		StringAttribute.class, "jbr.distr.errorCode");
	public static final ObjectId ERROR_DESCR_ID = ObjectId.predefined(
	    		TextAttribute.class, "jbr.distr.processingResult");
	public static final ObjectId SOURCE_UUID_ID = ObjectId.predefined(
    		StringAttribute.class, "jbr.distr.sourceUuid");
	
	
	private DataServiceFacade serviceBean = null;
	private Card card = null;
	private String id = null;
	private String uid = null;
	private String errorCode = null;
	private String errorDescr = null;
	private String sourceUuid = null;
	private StringAttribute sourceUuidAttr = null;
	
	private Delivery() {
	}
	
	private Delivery(DataServiceFacade serviceBean) {
		this.serviceBean = serviceBean;
	}

	public static Delivery newInstance(DataServiceFacade serviceBean) {
		return new Delivery(serviceBean);
	}
	
	public void init(Card cardDelivery) throws DataException {
		card = cardDelivery;
		if (card != null) {	
			id = card.getId().getId().toString();
			TextAttribute uidAttribute = (TextAttribute) card
			    	.getAttributeById(UUID_ATTRIBUTE_ID);
		    if (uidAttribute != null)
		    	uid = uidAttribute.getValue();
		    else
		    	throw new DataException("jbr.DistributionManager.cards.Delivery.notUUIDAttr");
		    
		    StringAttribute errorCodeAttr = (StringAttribute) card
					.getAttributeById(ERROR_CODE_ID);
			if (null != errorCodeAttr)
				errorCode = errorCodeAttr.getValue();
			else 
				throw 
					new DataException("jbr.DistributionManager.cards.Delivery.notErrorCodeAttr");
			
			StringAttribute errorDescrAttr = (StringAttribute) card
					.getAttributeById(ERROR_DESCR_ID);
			if (null != errorDescrAttr)
				errorDescr = errorDescrAttr.getValue();
			else 
				throw 
					new DataException("jbr.DistributionManager.cards.Delivery.notErrorDescrAttr");
			
			sourceUuidAttr = (StringAttribute) card
					.getAttributeById(SOURCE_UUID_ID);
			if (null != sourceUuidAttr)
				sourceUuid = sourceUuidAttr.getValue();
			else 
				throw 
					new DataException("jbr.DistributionManager.cards.Delivery.notSourceUuidAttr");
		} else
		    throw 
	    		new DataException("jbr.DistributionManager.cards.Delivery.notFound");
	
	logger.info("Create object Delivery with current parameters: "
		+ getParameterValuesLog());
	}
	
	public void saveCard() throws SaveCardException {
		UtilsWorkingFiles.saveCard(card, serviceBean);
	}
	
	protected String getParameterValuesLog() {
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(String.format("UUID='%s', ", uid));
		logBuilder.append(String.format("id='%s', ", id));
		return logBuilder.toString();
	}

	public String getId() {
		return id;
	}

	public String getUid() {
		return uid;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorDescr() {
		return errorDescr;
	}

	public String getSourceUuid() {
		return sourceUuid;
	}

	public void setSourceUuid(String sourceUuid) {
		sourceUuidAttr.setValue(sourceUuid);
		this.sourceUuid = sourceUuid;
	}
}
