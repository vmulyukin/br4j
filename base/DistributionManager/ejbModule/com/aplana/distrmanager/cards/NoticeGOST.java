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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.exceptions.SaveCardException;
import com.aplana.distrmanager.util.UtilsWorkingFiles;

public class NoticeGOST {

	protected Log logger = LogFactory.getLog(getClass());

    public static final ObjectId TEMPLATE_NOTICE_GOST_ID = ObjectId.predefined(
    	    Template.class, "jbr.gost.ack");
    public static final ObjectId UUID_ATTRIBUTE_ID = ObjectId.predefined(
		    TextAttribute.class, "uid");
    public static final ObjectId NAME_NOTICE_ID = ObjectId.predefined(
    		StringAttribute.class, "name");
    public static final ObjectId REGNUM_DOC_ID = ObjectId.predefined(
    		StringAttribute.class, "jbr.gost.ack.regNumber");
    public static final ObjectId REGNUM_INBOX_DOC_ID = ObjectId.predefined(
    		StringAttribute.class, "regnumber");
    public static final ObjectId DATEREG_DOC_ID = ObjectId.predefined(
    		DateAttribute.class, "jbr.gost.ack.regDate");
    public static final ObjectId DATEREG_INBOX_DOC_ID = ObjectId.predefined(
    		DateAttribute.class, "regdate");
    public static final ObjectId REFUSE_REASON_DOC_ID = ObjectId.predefined(
    		StringAttribute.class, "medo.registration.refuse.reason");
    public static final ObjectId NOTICE_TYPE_ID = ObjectId.predefined(
    		ListAttribute.class, "jbr.gost.ack.type");
    public static final ObjectId ERROR_CODE_ID = ObjectId.predefined(
    		StringAttribute.class, "jbr.gost.ack.errorCode");
    public static final ObjectId ERROR_DESCR_ID = ObjectId.predefined(
    		TextAttribute.class, "jbr.gost.ack.errorDescr");
    public static final ObjectId MODE_DEST = ObjectId.predefined(
    	    ReferenceValue.class, "jbr.gost.ack.type.recieved");
    public static final ObjectId MODE_REG = ObjectId.predefined(
    	    ReferenceValue.class, "jbr.gost.ack.type.registered");

    public static final ReferenceValue REG_VALUE = (ReferenceValue) DataObject.createFromId(MODE_REG);
    public static final ReferenceValue DEST_VALUE = (ReferenceValue) DataObject.createFromId(MODE_DEST);

	private DataServiceFacade serviceBean = null;
	private Card card = null;
	private String id = null;
	private String uid = null;
	private String name = null;
	private String errorCode = null;
	private String errorDescr = null;
	private String regNum = null;
	private Date dateReg = null;
	private ReferenceValue noticeType = null;
	private StringAttribute nameAttr = null;
	private StringAttribute errorDescrAttr = null;
	private StringAttribute errorCodeAttr = null;
	private StringAttribute regNumAttr = null;
	private DateAttribute dateRegAttr = null;
	private ListAttribute noticeTypeAttr = null;
	private TextAttribute uidAttribute = null;

	private NoticeGOST() {
	}

	private NoticeGOST(DataServiceFacade serviceBean) {
		this.serviceBean = serviceBean;
	}

	public static NoticeGOST newInstance(DataServiceFacade serviceBean) {
		return new NoticeGOST(serviceBean);
	}

	public void init(Card cardNotice) throws DataException {
		card = cardNotice;
		if (card != null) {
			id = card.getId().getId().toString();
			nameAttr = (StringAttribute) card
					.getAttributeById(NAME_NOTICE_ID);
			if (null != nameAttr)
				name = nameAttr.getValue();
			else
				throw
					new DataException("jbr.DistributionManager.cards.NoticeGOST.notNameAttr");

			uidAttribute = (TextAttribute) card.getAttributeById(UUID_ATTRIBUTE_ID);
		    if (uidAttribute != null)
		    	uid = uidAttribute.getValue();
		    else {
		    	throw
					new DataException("jbr.DistributionManager.cards.NoticeGOST.notUidAttribute");
		    }

			errorCodeAttr = (StringAttribute) card
					.getAttributeById(ERROR_CODE_ID);
			if (null != errorCodeAttr)
				errorCode = errorCodeAttr.getValue();
			else
				throw
					new DataException("jbr.DistributionManager.cards.NoticeGOST.notErrorCodeAttr");

			errorDescrAttr = (StringAttribute) card
					.getAttributeById(ERROR_DESCR_ID);
			if (null != errorDescrAttr)
				errorDescr = errorDescrAttr.getValue();
			else
				throw
					new DataException("jbr.DistributionManager.cards.NoticeGOST.notErrorDescrAttr");

			regNumAttr = (StringAttribute) card
					.getAttributeById(REGNUM_DOC_ID);
			if (null != regNumAttr)
				regNumAttr.getValue();
			else
				logger.warn("jbr.DistributionManager.cards.NoticeGOST.notRegNumAttr");

			dateRegAttr = (DateAttribute) card
					.getAttributeById(DATEREG_DOC_ID);
			if (null != dateRegAttr)
				dateRegAttr.getValue();
			else
				logger.warn("jbr.DistributionManager.cards.NoticeGOST.notDateRegAttr");

			noticeTypeAttr = (ListAttribute) card
					.getAttributeById(NOTICE_TYPE_ID);
			if (null != noticeTypeAttr)
				noticeType = noticeTypeAttr.getValue();
			else
				throw
					new DataException("jbr.DistributionManager.cards.NoticeGOST.notNoticeTypeAttr");
		} else
		    throw
		    	new DataException("jbr.DistributionManager.cards.NoticeGOST.notFound");

		logger.info("Create object NoticeGOST with current parameters: "
			+ getParameterValuesLog());
	}

	protected String getParameterValuesLog() {
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(String.format("UUID='%s', ", uid));
		logBuilder.append(String.format("id='%s', ", id));
		return logBuilder.toString();
	}

	public void saveCard() throws SaveCardException {
		UtilsWorkingFiles.saveCard(card, serviceBean);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		nameAttr.setValue(name);
		this.name = name;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorDescr() {
		return errorDescr;
	}

	public void setErrorDescr(String errorDescr) {
		errorDescrAttr.setValue(errorDescr);
		this.errorDescr = errorDescr;
	}

	public void setErrorCode(String errorCode) {
		errorCodeAttr.setValue(errorCode);
		this.errorCode = errorCode;
	}

	public String getRegNum() {
		return regNum;
	}

	public void setRegNum(String regNum) {
		regNumAttr.setValue(regNum);
		this.regNum = regNum;
	}

	public Date getDateReg() {
		return dateReg;
	}

	public void setDateReg(Date dateReg) {
		dateRegAttr.setValue(dateReg);
		this.dateReg = dateReg;
	}

	public ReferenceValue getNoticeType() {
		return noticeType;
	}

	public void setNoticeType(ReferenceValue noticeType) {
		noticeTypeAttr.setValue(noticeType);
		this.noticeType = noticeType;
	}

	public String getId() {
		return id;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		uidAttribute.setValue(uid);
		this.uid = uid;
	}

	public static Card create(DataServiceFacade serviceBean) throws DataException {
		CreateCard create = new CreateCard(TEMPLATE_NOTICE_GOST_ID);
		Card noticeCard = (Card)serviceBean.doAction(create);
		if (noticeCard == null) {
			throw new DataException(
					"jbr.DistributionManager.cards.NoticeGOST.notCreated");
		}
		UtilsWorkingFiles.saveCardCreated(noticeCard, serviceBean);
		return noticeCard;
	}
}
