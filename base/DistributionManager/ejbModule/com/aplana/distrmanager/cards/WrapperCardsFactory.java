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

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.action.ParametersAction;
import com.aplana.distrmanager.exceptions.FindMessageGostException;
import com.aplana.distrmanager.exceptions.StateCardException;
import com.aplana.distrmanager.util.IdStringPair;

public class WrapperCardsFactory {

	private final Log logger = LogFactory.getLog(getClass());

	private static final String ELM_ERROR = "jbr.DistributionManager.WrapperCardsFactory.errorCreateELM";
    private static final String STATE_CARD_ERROR = "jbr.DistributionManager.WrapperCardsFactory.errorCreateStateCard";
    private static final String FIND_MESSAGE_GOST_ERROR = "jbr.DistributionManager.WrapperCardsFactory.notFound.msgGost";
    private static final String MESSAGE_GOST_ERROR = "jbr.DistributionManager.WrapperCardsFactory.notFound.messageGOSTCard";
    private static final String NOTICE_GOST_ERROR = "jbr.DistributionManager.WrapperCardsFactory.errorCreateNoticeGOSTCard";

	private DataServiceFacade serviceBean;

	private WrapperCardsFactory() {
	}

	private void init(DataServiceFacade serviceBean) {
		this.serviceBean = serviceBean;
	}
	public static WrapperCardsFactory instance(DataServiceFacade serviceBean) {
		WrapperCardsFactory wf = new WrapperCardsFactory();
		wf.init(serviceBean);
		return wf;
	}

	public ElementListMailing createElmWrapper(Card elmCard) throws Exception {
		// Wrapper ���
		ElementListMailing elmCardWrap = null;
		try {
			elmCardWrap = new ElementListMailing(serviceBean);
			elmCardWrap.init(elmCard);
			return elmCardWrap;
		} catch (Exception se) {
			logError(elmCard, elmCardWrap, ELM_ERROR, se);
		    throw se;
		}
	}

	public StateCard createStateCardWrapper(ElementListMailing elmCardWrap) throws DataException {
		// ���� ��������� �����
		StateCard stateCardWrap = null;
		try {
			stateCardWrap = new StateCard(serviceBean);
			Card stateCard = elmCardWrap.findStateCardOnIntAttr(StateCard.ITERATION_NUMBER, elmCardWrap.getLastAttempt());
			stateCardWrap.init(stateCard);
			return stateCardWrap;
		} catch (Exception se) {
			Card card = null;
			if (null != elmCardWrap)
				card = elmCardWrap.getCard();
			logError(card, elmCardWrap, STATE_CARD_ERROR, se);
			throw
				new StateCardException(se);
		}
	}

	public MessageGOST createMessageGostWrapper(ElementListMailing elmCardWrap) throws Exception {
		// ������� ��������� ����
		MessageGOST msgGOST = null;
		try {
			ObjectId msgGostId = elmCardWrap.findCardMessageGOST(); // ���� �� �����
			//MessageGOST.findCard(elmCard.getUid(), serviceBean); // ���� �� uuid
			msgGOST = new MessageGOST(serviceBean);
			msgGOST.init(msgGostId);
			return msgGOST;
		} catch(FindMessageGostException fmsge) {
			Card card = null;
			if (null != elmCardWrap)
				card = elmCardWrap.getCard();
			logError(card, elmCardWrap, FIND_MESSAGE_GOST_ERROR, fmsge);
			throw fmsge;
		} catch(Exception exMsg) {
			Card card = null;
			if (null != elmCardWrap)
				card = elmCardWrap.getCard();
			logError(card, elmCardWrap, MESSAGE_GOST_ERROR, exMsg);
		    throw exMsg; // ���� �� ������� �� ������ ��������� ����, �������� ������� ��������� "�������� ������ ��������"
		}
	}

	@SuppressWarnings("static-access")
	public NoticeGOST createNoticeGost(ElementListMailing elmCardWrap, MessageGOST msgGOST, Card docBase, Action action) throws Exception {
		NoticeGOST noticeGost = null;
		try {
			// ���� ��� ������������� ��������� == null, �� ������������
			if (null != elmCardWrap.getMsgTypeValue()) {
				Long typeMsg = (Long)elmCardWrap.getMsgTypeValue().getId().getId();
				Long msgNotice =  (Long)ElementListMailing.TYPE_MESSAGE_NOTICE.getId();
				// ���� � ��� �������� �������� "��� ������������� ���������" ����� "�����������"
				// c����� ��� "����������� ����"
				if (msgNotice.equals(typeMsg)) {
					Card cardNoticeGost = NoticeGOST.create(serviceBean);
					long cardNoticeGostId = ((Long) cardNoticeGost.getId().getId()).longValue();
					// ��������� ������������ �������� �����������
					noticeGost = NoticeGOST.newInstance(serviceBean);
					noticeGost.init(cardNoticeGost);
					//noticeGost.setName(noticeGost.getUid()); // ��� �������� = uid
					if (action.getResultType().equals(ParametersAction.Result.class)) {
						ParametersAction paramAction = (ParametersAction)action;
						ParametersAction.Result res = paramAction.getResult();
						// ��������� ����������� � ��������
						ReferenceValue refVal = res.getRefValue();
						Delivery delivery = Delivery.newInstance(serviceBean);
						delivery.init(docBase);
						noticeGost.setUid(delivery.getSourceUuid());
						if (NoticeGOST.DEST_VALUE.equals(refVal)) {
							noticeGost.setNoticeType(NoticeGOST.DEST_VALUE);
							noticeGost.setErrorCode(delivery.getErrorCode());
							noticeGost.setErrorDescr(delivery.getErrorDescr());
						}
						// ��������� ����������� � �����������
						if (NoticeGOST.REG_VALUE.equals(refVal)) {
							// �������� �� ��������� ��� ���� � �������� ������
							String errorCode = "0";
							String errorDescr = "";
							noticeGost.setNoticeType(NoticeGOST.REG_VALUE);
							noticeGost.setErrorCode(errorCode);
							noticeGost.setErrorDescr(errorDescr);
							for (IdStringPair pair : res.getPair()) {
								if (null != pair) {
									if (pair.destId().equals(NoticeGOST.ERROR_CODE_ID))
										noticeGost.setErrorCode(pair.getVal());
									if (pair.destId().equals(NoticeGOST.ERROR_DESCR_ID))
										noticeGost.setErrorDescr(pair.getVal());
								}
							}
							for(Attribute attr : res.getListParameters()) {
								if (null != attr) {
									if (attr instanceof DateAttribute && attr.getId().equals(noticeGost.DATEREG_INBOX_DOC_ID)) {
										DateAttribute dateAttr = (DateAttribute)attr;
										noticeGost.setDateReg(dateAttr.getValue());
									}
									if (attr instanceof StringAttribute && attr.getId().equals(noticeGost.REGNUM_INBOX_DOC_ID)) {
										StringAttribute strAttr = (StringAttribute)attr;
										noticeGost.setRegNum(strAttr.getValue());
									}
									if (attr instanceof StringAttribute && attr.getId().equals(noticeGost.REFUSE_REASON_DOC_ID)) {
										StringAttribute strAttr = (StringAttribute) attr;
										noticeGost.setErrorDescr(strAttr.getValue());
										noticeGost.setErrorCode("-1");
									}
								}
							}
						}
					}
					noticeGost.saveCard();
					msgGOST.addNoticeGost(cardNoticeGostId);
					if (!msgGOST.isExistNoticeGost())
						throw
							new Exception(NOTICE_GOST_ERROR);

				}
			}
			return noticeGost;
		} catch(Exception ne) {
			Card card = null;
			if (null != elmCardWrap)
				card = elmCardWrap.getCard();
			logError(card, elmCardWrap, NOTICE_GOST_ERROR, ne);
			throw ne;
		}
	}

	private void logError(Card card, ElementListMailing elmWrap, String msgError, Exception e) {
		String error = String.
			format("{%s}; elmId: {%s}; elmUUID: {%s};",
					(null == msgError)?"null":msgError,
					(null == card)?"null":card.getId().getId(),
					(null == elmWrap)?"null":elmWrap.getUid()
			);
		if (null == e) {
			logger.error(error);

		} else {
			logger.error(error, e);
		}
	}
}
