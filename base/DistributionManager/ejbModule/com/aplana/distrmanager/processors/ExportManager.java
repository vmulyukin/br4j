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
package com.aplana.distrmanager.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.aplana.dbmi.action.ExportCardToXml.Result;
import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.jbr.util.IdUtils.IdPair;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.distrmanager.action.ParametersAction;
import com.aplana.distrmanager.cards.ElementListMailing;
import com.aplana.distrmanager.cards.MessageGOST;
import com.aplana.distrmanager.cards.NoticeGOST;
import com.aplana.distrmanager.cards.StateCard;
import com.aplana.distrmanager.cards.WrapperCardsFactory;
import com.aplana.distrmanager.exceptions.ChangeStateException;
import com.aplana.distrmanager.exceptions.DeleteMessageGostException;
import com.aplana.distrmanager.exceptions.DocBaseException;
import com.aplana.distrmanager.exceptions.FindMessageGostException;
import com.aplana.distrmanager.exceptions.SaveCardException;
import com.aplana.distrmanager.exceptions.StateCardException;
import com.aplana.distrmanager.exceptions.WriteResultException;
import com.aplana.distrmanager.handlers.CheckDocBase;
import com.aplana.distrmanager.handlers.CreateOutXml;
import com.aplana.distrmanager.handlers.InitExportBeanFactory;
import com.aplana.distrmanager.handlers.SaveAttachments;
import com.aplana.distrmanager.handlers.SetTimeInMessage;
import com.aplana.distrmanager.handlers.UpdateMessageGost;
import com.aplana.distrmanager.util.UtilsWorkingFiles;

public class ExportManager extends ProcessCard {

	/**
	 *
	 */
	private static final String UPDATE_GOST_BEAN_PARAM = "updateGostBean";

	/**
	 * ��������� - �������� �������� (��� �������� ��������� "��������� ����" - "����������� ����", �� �������� ��������).
	 * @author ppanichev
	 */

	private final Log logger = LogFactory.getLog(getClass());

	private static final long serialVersionUID = 1L;

    private static final String STATE_CARD_ERROR = "jbr.DistributionManager.ExportManager.errorCreateStateCard";
    private static final String BACKLINK_DOCBASE_ERROR = "jbr.DistributionManager.ExportManager.notFound.backLinkDocBase";
    private static final String NONCRITICAL_ERROR = "jbr.DistributionManager.ExportManager.noncriticalError";
    private static final String FIND_MESSAGE_GOST_ERROR = "jbr.DistributionManager.ExportManager.notFound.msgGost";
    private static final String SUCCESSFUL = "jbr.DistributionManager.ExportManager.SUCCESSFUL";
    private static final String DELETE_MESSAGE_GOST_ERROR_CRITICAL = "jbr.DistributionManager.ExportManager.DeleteMessageGost.Rollback";
    private static final String WRITE_RESULT_ERROR_CRITICAL = "jbr.DistributionManager.ExportManager.WriteResult.Rollback";
    private static final String SAVE_CARD_ERROR_CRITICAL = "jbr.DistributionManager.ExportManager.SaveCard.Rollback";
    private static final String CHANGE_STATE_ERROR_CRITICAL = "jbr.DistributionManager.ExportManager.ChangeStatus.Rollback";
    private static final String ERROR_CRITICAL = "jbr.DistributionManager.ExportManager.doProcess.CriticalError.Rollback";

	/**
	 * ������� �� ��������-���������.
	 */
	public static final String ID_DOC = "idDocumentBase";

    /**
     * �������� ��������� �������: ������������� �� ���� (������� - �������� ��������) � �������� -
     * ���� (������� - �������� ��������), ��������� � ��������� ���������� ��� ������� ��������.
     * ������ ���������: "�������1 ��������" = ��������1{;�������2 ��������" = ��������2}
     */
	private static final String CONDITIONS = "conditions";

	protected static final String REG_SEPARATOR = "[,;]";
	protected static final String SET_SEPARATOR = "[=]";

	private List<IdPair> setAttrPairsList = new ArrayList<IdPair>();
	private DataServiceFacade serviceBean = null;
	private ObjectId backLinkDocBase = null;
	private Card docBase = null;
	private ObjectId docBaseId = null;
	private ObjectId msgGostId = null;
	private ElementListMailing elmCardWrap = null;
	private StateCard stateCardWrap = null;
	private Card currentCard = null;
	private MessageGOST msgGOSTWrap = null;
	private Result xmlOutResult = null;
	private String noticeGostId = null;
	private Map<ObjectId, String> updateGostBeanNameMapping = new HashMap<ObjectId, String>();
	private String updateGostBeanName = "";

	@Override
	public Object process() throws DataException {
		try {
			try {
				final Card card = loadCard(getCardId());
				currentCard = card;
				// ��������� �������� �� conditions
				if (!checkConditions())
					return null;	// ���� �������� �������� �� ������ � conditions, �� ������������ ��������
				serviceBean = InitExportBeanFactory.instance().initServiceBean(getBeanFactory());
				docBase = CheckDocBase.instance(serviceBean).handle(card, backLinkDocBase);
				docBaseId = docBase.getId();
				elmCardWrap = WrapperCardsFactory.instance(serviceBean).createElmWrapper(card);
				stateCardWrap = WrapperCardsFactory.instance(serviceBean).createStateCardWrapper(elmCardWrap);
				msgGOSTWrap = WrapperCardsFactory.instance(serviceBean).createMessageGostWrapper(elmCardWrap);
				msgGostId = msgGOSTWrap.getCard().getId();
				// ���� �������� ����������, ��� ������ ��������� ����,
				// �.�. �������� �� ������ ��������� ����, �.�. ��� ��� ����������.
				// ������� ���������� ��. ����������.
				if (msgGOSTWrap.isExistAttachments())
					return null;

				calculateUpdateGostBeanName();
				UpdateMessageGost updateMessageGost = null;
				if (!updateGostBeanName.isEmpty() && getBeanFactory().containsBean(updateGostBeanName)) {
					updateMessageGost = (UpdateMessageGost) getBeanFactory().getBean(updateGostBeanName, UpdateMessageGost.class);
				} else {
					updateMessageGost = new UpdateMessageGost(serviceBean);
				}
				updateMessageGost.handle(msgGOSTWrap, elmCardWrap);
				xmlOutResult = CreateOutXml.instance(serviceBean).handle(card, docBaseId);
				SaveAttachments.instance().handle(msgGOSTWrap, xmlOutResult);
				SetTimeInMessage.instance(serviceBean).handle(elmCardWrap, getJdbcTemplate());
				// �����
				logError(card, SUCCESSFUL, null);
			} catch(DocBaseException dbe) {
				logError(currentCard, BACKLINK_DOCBASE_ERROR, dbe);
				throw
					new DataException(BACKLINK_DOCBASE_ERROR);
			} catch(StateCardException sce) {
				logError(currentCard, STATE_CARD_ERROR, sce);
				throw
					new DataException(STATE_CARD_ERROR);
			} catch(FindMessageGostException fmge) { // ������������� ������ (�� ����� ��������� ����)
				logError(currentCard, FIND_MESSAGE_GOST_ERROR, fmge);
				// ��������� ������� � ��������� �����
				stateCardWrap.setResultProcessing(fmge.toString());
				UtilsWorkingFiles.saveCard(stateCardWrap.getCard(), serviceBean);
				// ��������� ��� � "������������"
				elmCardWrap.byPrepareNotSent(getJdbcTemplate());
			} catch(Exception e) { // ������������� ������
				logError(currentCard, NONCRITICAL_ERROR, e);
				// ������� ��������� ���� � ����� �������� (� ��� �� ��� ��������, ������� ����� �����)
				final NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(
    					getJdbcTemplate());
				elmCardWrap.deleteMsgGost(msgGostId, jdbc, getJdbcTemplate());
				// ��������� ������� � ��������� �����
				stateCardWrap.setResultProcessing(e.getMessage());
				UtilsWorkingFiles.saveCard(stateCardWrap.getCard(), serviceBean);
				// ��������� ��� � "������������"
				elmCardWrap.byPrepareNotSent(getJdbcTemplate());
			}
		} catch(DeleteMessageGostException dmge) { // ����������� ������
			logError(currentCard, DELETE_MESSAGE_GOST_ERROR_CRITICAL, dmge);
			throw dmge;
		} catch(WriteResultException wre) { // ����������� ������
			logError(currentCard, WRITE_RESULT_ERROR_CRITICAL, wre);
			throw wre;
		} catch(SaveCardException sce) { // ����������� ������
			logError(currentCard, SAVE_CARD_ERROR_CRITICAL, sce);
			throw sce;
		} catch(ChangeStateException cse) { // ����������� ������
			logError(currentCard, CHANGE_STATE_ERROR_CRITICAL, cse);
			throw cse;
		} catch(Exception e) { // ����������� ������
			logError(currentCard, ERROR_CRITICAL, e);
			throw new DataException(ERROR_CRITICAL, e);
		}
		return null;
	}

	private void calculateUpdateGostBeanName() {
		if (getResult() instanceof ParametersAction.Result) {
			updateGostBeanName = ((ParametersAction.Result)getResult()).getParameter(UPDATE_GOST_BEAN_PARAM);
			return;
		}
		if (updateGostBeanNameMapping.containsKey(docBase.getTemplate())) {
			updateGostBeanName = updateGostBeanNameMapping.get(docBase.getTemplate());
		}
	}

	@Override
	public void setParameter(String name, String value) {
		if (ID_DOC.equalsIgnoreCase(name)) {
			this.backLinkDocBase = ObjectIdUtils.getObjectId(BackLinkAttribute.class, value, false);
		} else if (UPDATE_GOST_BEAN_PARAM.equalsIgnoreCase(name)) {
			String[] mapping = value.trim().split("\\s*;\\s*");
			for (String mappingPart : mapping) {
				String[] mappingPair = mappingPart.split("\\s*:\\s*");
				if (mappingPair.length != 2) {
					throw new IllegalArgumentException("Illegal config: [" + mappingPart + "]. It should be in format <template>|default:<name>");
				}
				if (mappingPair[0].equals("default")) {
					updateGostBeanName = mappingPair[1];
				} else {
					ObjectId templateId = ObjectIdUtils.getObjectId(Template.class, mappingPair[0], true);
					updateGostBeanNameMapping.put(templateId, mappingPair[1]);
				}
			}
		} else if (CONDITIONS.equals(name)){ // ���������� �������� �������
			String[] list = value.trim().split(REG_SEPARATOR); // ��������� ������ �� ������ ������� ��������
				for (String setRule : list){
					String[] pairString = setRule.split(SET_SEPARATOR); // ��������� ������ �������� �� ����: �������, ��������.
					if (pairString.length != 2){ // ���� ��� ���� ��� ��� �����������
						logger.warn("Broken conditions rule: "+setRule+" Skipping.");
						continue;
					}
					final IdPair pair = new IdPair();
					pair.dest.setId( IdUtils.smartMakeAttrId( pairString[0].trim(),
							ListAttribute.class, false));
					if (pair.destId() == null){
						logger.warn("Broken conditions rule: "+setRule+" Can not determine " +
								"destination attribute: "+pairString[0]+" Skipping.");
						continue;
					}
					if (!"NULL".equalsIgnoreCase(pairString[1])){
						pair.source.setId( IdUtils.smartMakeAttrId(pairString[1].trim(),
								ReferenceValue.class));
						if (pair.sourceId() == null){
							logger.warn("Broken conditions rule: "+setRule+" Can not determine " +
									"source attribute/value: "+ pairString[1]+" Skipping.");
							continue;
						}
					}
					setAttrPairsList.add(pair);
				}
			} else {
				super.setParameter(name, value);
			}
	}

	private boolean checkConditions() { // ������ - ������� ���������. ���� �������� ��� � �������� ��� �� == null - false.
		boolean result = false;
		if (setAttrPairsList.isEmpty()) {
			return true;
		}
		for (IdPair entry : setAttrPairsList) {
			try {
				// entry.sourceId() - �������� �������� ��� ListAttribute
				ListAttribute attrList = (ListAttribute)currentCard.getAttributeById(entry.destId()); // �������� ������� ListAttribute
				if (attrList != null && attrList.getValue() != null) {
					// ���������� �������� ListAttribute �� �������� � �������� ��������� ListAttribute
					// true - � �������� ������������ ��������, �������� � �������.
					// false - � �������� ����������� ��������, �������� � �������.
					if(attrList.getValue().getId().equals(entry.sourceId())) {
						result = true;
					} else {
						logger.warn("The card does not contain the value specified in the configuration file. Attribute "
								+ entry.dest + " from " + entry.source);
						result = false;
						break; // false * any = false
					}
				} else {
					logger.warn("The card does not contain the attribute specified in the configuration file. Attribute: "
							+ entry.dest + " from " + entry.source);
					result = false;
					break; // false * any = false;
				}
			} catch (Exception e) {
				logger.warn("Error setting attribute " + entry.dest +
						" from " + entry.source, e);
			}
		}
		return result;
	}

	protected Card loadCard(ObjectId id) throws DataException
	{
		final ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
		query.setId(id);
		return (Card) getDatabase().executeQuery(getSystemUser(), query);
	}

	private void logError(Card card, String msgError, Exception e) {
		String error = String.
			format("{%s} docBaseId: {%s}; elmId: {%s}; elmUUID: {%s}; msgGostId: {%s}; noticeGostId: {%s};",
					(null == msgError)?"null":msgError,
					(null == docBaseId)?"null":docBaseId.getId(),
					(null == card)?"null":card.getId().getId(),
					(null == elmCardWrap)?"null":elmCardWrap.getUid(),
					(null == msgGostId)?"null":msgGostId.getId(),
					(null == noticeGostId)?"null":noticeGostId
			);
		if (null == e) {
			logger.error(error);

		} else {
			logger.error(error, e);
		}
	}
}
