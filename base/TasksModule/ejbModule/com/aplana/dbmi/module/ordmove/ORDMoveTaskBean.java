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
package com.aplana.dbmi.module.ordmove;

import com.aplana.dbmi.action.*;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.*;
import com.aplana.dbmi.task.AbstractTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class ORDMoveTaskBean extends AbstractTask {

	private static final long serialVersionUID = 1L;

    private static final ObjectId TEMLATE_ORD = ObjectId.predefined(Template.class, "jbr.ord");
    private static final ObjectId STATE_EXECUTION = ObjectId.predefined(CardState.class, "execution");
    private static final ObjectId WFM_EXECUTION_DONE = ObjectId.predefined(WorkflowMove.class, "jbr.incoming.execution.done");

    private static final ObjectId DOCUMENT_COMMISSION_CARDLINK_ID = ObjectId.predefined(BackLinkAttribute.class, "jbr.resolutions");
    private static final ObjectId DOCUMENT_INFORMATION_CARDLINK_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.inform.list");
    private static final ObjectId DOCUMENT_REG_NUMBER_ID = ObjectId.predefined(StringAttribute.class, "jbr.maindoc.regnum");
    private static final ObjectId DOCUMENT_EXECSEND_DATEATTR_ID = new ObjectId(DateAttribute.class, "JBR_DATE_ON_EXECUTE");

    private static final ObjectId DONE_STATE_ID = ObjectId.predefined(CardState.class, "done");
    private static final ObjectId COMMISSION_CANCELLED_STATE_ID = ObjectId.predefined(CardState.class, "poruchcancelled");
    private static final ObjectId COMMISSION_RECYCLED_STATE_ID = ObjectId.predefined(CardState.class, "trash");
    private static final ObjectId ACQUAINTANCE_DONE_STATE_ID = ObjectId.predefined(CardState.class, "competent");

    private static final ObjectId INFORMATION_ACQUAINTANCE_DATEATTR_ID = ObjectId.predefined(DateAttribute.class, "jbr.acquaintance");
    private static final ObjectId INFORMATION_DISPATH_DATEATTR_ID = ObjectId.predefined(DateAttribute.class, "jbr.information.dispathDate");

    protected final Log logger = LogFactory.getLog(getClass());

    private static Boolean working = false;


    /**
     * 
     * @param parameters
     */
	public void process(Map<?, ?> parameters) {
        synchronized (ORDMoveTaskBean.class) {
            if (working) {
                logger.warn("Process is already working. Skipping.");
                return;
            }
            working = true;
        }
        logger.info("TASK started");
        try {
            processMoving(serviceBean);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            synchronized (ORDMoveTaskBean.class) {
                working = false;
                logger.info("TASK finished");
            }
        }
    }

    private void processMoving(DataServiceBean service) throws DataException, ServiceException {
        List<Card> cardList = fetchOrdInExecutionStatus(service);
        if (cardList != null) {
            boolean locked = false;
            for (Card card : cardList) {
                try {
                    locked = lockCard(card, service);
                    if (locked) {
                        ORDMoveCondition condition = validForMove(card, service);
                        if (condition.isPassed()) {
                            logger.info(customToString(card) + " to be moved to Done status.\n" + condition);
                            moveStatus(card, service);
                        } else {
                            logger.info(customToString(card) + " doesn't meet conditions to be moved to Done status.\n" + condition);
                        }
                    }
                    // even if exception occurred at one card, process other
                } catch (DataException e) {
                    logger.error(e.getMessage(), e);
                } catch (ServiceException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    if (locked) 
                    	unlockCard(card, service);
                }
            }
        }
    }

    private void moveStatus(Card card, DataServiceBean service) throws DataException, ServiceException {
        // ���������� ������� ��� �������� ��� � ��������� "��������"

        final WorkflowMove wfm = (WorkflowMove) DataObject.createFromId(WFM_EXECUTION_DONE);

        ChangeState changeState = new ChangeState();
        changeState.setCard(card);
        changeState.setWorkflowMove(wfm);

        service.doAction(changeState);
        logger.info(customToString(card) + " has moved to DONE state.");
    }


    private List<Card> fetchOrdInExecutionStatus(DataServiceBean service) throws DataException, ServiceException {
        Search search = new Search();
        search.setByAttributes(true);

        // search ORD
        List<DataObject> templates = new ArrayList<DataObject>(1);
        templates.add(DataObject.createFromId(TEMLATE_ORD));
        search.setTemplates(templates);
        // state ���������� 
        List<DataObject> states = new ArrayList<DataObject>(1);
        states.add(DataObject.createFromId(STATE_EXECUTION));
        search.setStates(states);
        defineColumnsTopLevelSearch(search);

        final SearchResult searchResult = (SearchResult) service.doAction(search);
        if (searchResult == null) {
            logger.info("No ORD in Execution state found. searchResult == null");
            return null;
        }

        final List<Card> cards = getCardsList(searchResult);
        if (cards != null) {
            logger.info(cards.size() + " ORD(s) found in Execution state. ");
            return cards;
        }
        logger.info("No ORD in Execution state found. searchResult.getCards() == null");
        return null;
    }

    private void defineColumnsTopLevelSearch(Search search) {
        List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
        addColumnToList(columns, DOCUMENT_REG_NUMBER_ID);
        addColumnToList(columns, DOCUMENT_COMMISSION_CARDLINK_ID);
        addColumnToList(columns, DOCUMENT_INFORMATION_CARDLINK_ID);
        addColumnToList(columns, DOCUMENT_EXECSEND_DATEATTR_ID);
        search.setColumns(columns);
    }

    private void addColumnToList(List<SearchResult.Column> columns, ObjectId objectId) {
        SearchResult.Column col = new SearchResult.Column();
        col.setAttributeId(objectId);
        columns.add(col);
    }

    private ORDMoveCondition validForMove(Card card, DataServiceBean service) throws DataException, ServiceException {
        // �������� ���������� � �������� ��������� �������� ������
        /* �������� 1-�� �������:
         * ��� �� ��������� ������� ������, � ����� �� ������������ ��������� � �������� �������� (����������� ��� �� ������������, ��������, �������, ������� ��� �� ���������)
         */
    	final BackLinkAttribute commissionsAttr = (BackLinkAttribute) card.getAttributeById(DOCUMENT_COMMISSION_CARDLINK_ID);
        final CardLinkAttribute infoAttribute = (CardLinkAttribute) card.getAttributeById(DOCUMENT_INFORMATION_CARDLINK_ID);
        if (commissionsAttr != null && !commissionsAttr.isEmpty()) {
            return areCommissionsAndAcquaintancesInFinalState(card.getId(), commissionsAttr, infoAttribute, service);
        }
        
        // ��� ���������
        // �������� ������������
        /* �������� 2-�� �������:
         * �� ��������� ��� �� ��������� � ��� �� ������������ ��������� � �������� �������� (�����������). ����� ���������� ������������ ������ 30 ����.
         */
        if (infoAttribute != null && !infoAttribute.isEmpty()) {
            Date lastAcquaintanceDate = obtainLastAcquintanceDate(infoAttribute, service);
            if (lastAcquaintanceDate == null) return ORDMoveCondition.INFO_NOT_EXPIRED_NO_COMMISSION;
            return checkInterval(lastAcquaintanceDate) ?
                    ORDMoveCondition.INFO_EXPIRED_NO_COMMISSION : ORDMoveCondition.INFO_NOT_EXPIRED_NO_COMMISSION;
        }

        // ��� ��� ������������
        /* �������� 3-�� �������:
         * �� ��������� ��� �� ��������� � ��� �� ������������ ������ 30 ���� ����� �������� �� ����������;
         */
        // �������� ���� �������� �� ����������
        Date execSendDate = readDateValue(card, DOCUMENT_EXECSEND_DATEATTR_ID);
        if (execSendDate == null) {
            logger.warn(customToString(card) + " - JBR_INCOMEDATE is not filled.");
            return ORDMoveCondition.EXEC_NOT_EXPIRED_NO_COMMISSION_NO_INFO;
        }

        return checkInterval(execSendDate) ?
                ORDMoveCondition.EXEC_EXPIRED_NO_COMMISSION_NO_INFO : ORDMoveCondition.EXEC_NOT_EXPIRED_NO_COMMISSION_NO_INFO;
    }


    /**
     * ��������� ���� ������������ � �� �������
     */
    private Date obtainLastAcquintanceDate(CardLinkAttribute infoAttribute, DataServiceBean service) throws DataException, ServiceException {
        final Search search = new Search();
        search.setByCode(true);
        search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(infoAttribute.getIdsLinked()));
        final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
        // ���� ������������
        columns.add(createColumn(INFORMATION_ACQUAINTANCE_DATEATTR_ID));
        // ���� � ����� �������� �� ������������
        columns.add(createColumn(INFORMATION_DISPATH_DATEATTR_ID));
        // ������ ������������
        columns.add(createColumn(Card.ATTR_STATE));
        search.setColumns(columns);

        SearchResult searchResult = (SearchResult) service.doAction(search);
        // ������� ���� ���������� ������������ � ����������
        Date lastAcquaintanceDate = null;
        if (searchResult != null && searchResult.getCards() != null) {
            final List<Card> cards = getCardsList( searchResult );
            if (cards != null) {
            	for (Card info : cards) {
            		final ObjectId stateId = info.getState();
            		// ���� ������ ������������ �� ��������, �� ������ ������� �� ��������� (����������� null - ������������� ��������� ��������)
            		if (!ACQUAINTANCE_DONE_STATE_ID.equals(stateId)
            				) {
            			logger.debug("Found not finished acquaintance: " + info.getId().getId() + ". Exiting.");
            			return null;
            		}
            		final Date acquaintanceDate = readDateValue(info, INFORMATION_ACQUAINTANCE_DATEATTR_ID);
            		if (acquaintanceDate != null) {
            			if (lastAcquaintanceDate == null || acquaintanceDate.after(lastAcquaintanceDate)) {
            				lastAcquaintanceDate = acquaintanceDate;
            			}
            		} else {
            			// ���� ���� ������������ �� ���������, ���������� ���� �������� ��� ������������
            			final Date dispathDate = readDateValue(info, INFORMATION_DISPATH_DATEATTR_ID);
            			if ((dispathDate != null) && (lastAcquaintanceDate == null || dispathDate.after(lastAcquaintanceDate))) {
            				lastAcquaintanceDate = dispathDate;
            			}
            		}
            	}
            }
        }
        logger.debug("Last Acquaintance Date is: " + (lastAcquaintanceDate == null ? null : lastAcquaintanceDate.toString()));
        return lastAcquaintanceDate;
    }

    private boolean checkInterval(Date lastAcquaintanceDate) {
        long intervalMs = System.currentTimeMillis() - lastAcquaintanceDate.getTime();
        long days = intervalMs / (1000L * 60L * 60L * 24L);
        return (days > ORDMoveCondition.DAYS_BEFORE_MOVE);
    }


    /**
     * ��������� ��� ��� ��������� ������� ������ � ��� ������������ (��������� ������������ - ��� ���� true) � �������� ��������
     */
    private ORDMoveCondition areCommissionsAndAcquaintancesInFinalState(ObjectId cardId, BackLinkAttribute commissionsAttr, CardLinkAttribute acquaintanceAttr, DataServiceBean service) throws DataException, ServiceException {
    	final ListProject action = new ListProject();
		action.setCard( cardId);
		action.setAttribute( commissionsAttr.getId());
        final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
        columns.add(createColumn(Card.ATTR_STATE));
		action.setColumns(columns);

        SearchResult searchResult = (SearchResult) service.doAction(action);
        if (searchResult != null && searchResult.getCards() != null) {
            final List<Card> cards = getCardsList(searchResult);
            if (cards != null) {
            	for (Card commission : cards) {
            		final ObjectId stateId = commission.getState();
            		if (!COMMISSION_CANCELLED_STATE_ID.equals(stateId)
            				&& !DONE_STATE_ID.equals(stateId)
            				&& !COMMISSION_RECYCLED_STATE_ID.equals(stateId)
            		) {
            			logger.debug("Found not finished top-level commission: " + commission.getId().getId() + ". Exiting.");
            			return ORDMoveCondition.NOT_ALL_ORD_IN_FINAL_STATE;
            		}
            	}
            }
        }
        logger.debug("All top-level commissions are in final state.");
        
        // �������� ������������
        final Search search = new Search();
        search.setByCode(true);
        search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(acquaintanceAttr.getIdsLinked()));
        search.setColumns(columns);

        searchResult = (SearchResult) service.doAction(search);
        if (searchResult != null && searchResult.getCards() != null) {
            final List<Card> cards = getCardsList(searchResult);
            if (cards != null) {
            	for (Card acquaintance : cards) {
            		final ObjectId stateId = acquaintance.getState();
            		if (!ACQUAINTANCE_DONE_STATE_ID.equals(stateId)
            				) {
            			logger.debug("Found not finished acquaintance: " + acquaintance.getId().getId() + ". Exiting.");
            			return ORDMoveCondition.NOT_ALL_ORD_IN_FINAL_STATE;
            		}
            	}
            }
        }
        logger.debug("All acquaintances are in final state.");
        
        return ORDMoveCondition.ALL_ORD_IN_FINAL_STATE;
    }

    private boolean lockCard(Card card, DataServiceBean service) throws DataException, ServiceException {
        if (card == null) throw new IllegalArgumentException("Card is null");
        try {
            LockObject lock = new LockObject(card.getId());
            service.doAction(lock);
        } catch (ObjectLockedException e) {
            logger.warn(customToString(card) + " is locked by " + e.getLocker().getFullName());
            return false;
        }
        logger.debug(customToString(card) + " has locked");
        return true;
    }

    private void unlockCard(Card card, DataServiceBean service) {
        if (card == null) throw new IllegalArgumentException("Card is null");
        try {
            UnlockObject unlock = new UnlockObject(card.getId());
            service.doAction(unlock);
            logger.debug("Parent document unlocked");
        } catch (DataException e) {
            logger.error("Failed to unlock parent document object", e);
        } catch (ServiceException e) {
            logger.error("Failed to unlock parent document object", e);
        }
    }

    private String customToString(Card card) {
        if (card == null) return null;
        StringAttribute sa = (StringAttribute) card.getAttributeById(DOCUMENT_REG_NUMBER_ID);
        return new StringBuilder().append("Card '").append(sa == null ? null : sa.getValue())
                .append("' Id (").append(
                        (card.getId() == null || card.getId().getId() == null) ? null : card.getId().getId())
                .append(")").toString();
    }

    private Date readDateValue(Card card, ObjectId attr) {
        final DateAttribute date = (DateAttribute) card.getAttributeById(attr);
        return (date != null && !date.isEmpty()) 
        			? date.getValue()
        			: null;
    }

	/**
	 * ���������� ��� �������� ������� ��� ���������.
	 * @param attrId �������
	 * @return ������� � ���� ���������.
	 */
	public static SearchResult.Column createColumn(ObjectId attrId) 
	{
		final SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(attrId);
		return col;
	}

	/**
	 * �������� ������ ������� {@link SearchResult.Column}[] ��� ���������� 
	 * ������ ���������.
	 * @param attrIds id ���������, ��� ������� ��������� �������� ��������� �������.
	 * @return ����� ������ �������.
	 */
	public static List<SearchResult.Column> createColumns(ObjectId... attrIds)
	{
		return (attrIds == null)
					? null
					: addColumns( new ArrayList<SearchResult.Column>(attrIds.length), attrIds);
	}

	/**
	 * �������� ��������� ������� ��������� ��������� � ������ �������. 
	 * @param dest ������� ������, � ������� ��������.
	 * @param attrIds id ���������, ��� ������� ��������� �������� ��������� �������.
	 * @return dest.
	 */
	public static List<SearchResult.Column> addColumns(List<SearchResult.Column> dest, ObjectId... attrIds)
	{
		if (dest != null && attrIds != null) {
			for (ObjectId id : attrIds) {
				if (id != null)
					dest.add(createColumn(id));
			}
		}
		return dest;
	}

	/**
	 * �������� ������ �������� �� ���������� ����������.
	 * @param sr
	 * @return �������� ������ �������� ��� null.
	 */
	@SuppressWarnings("unchecked")
	public static List<Card> getCardsList( final SearchResult sr)
	{
		if (sr == null)
			return null;
		final List<Card> list = sr.getCards();
		return (list == null || list.isEmpty()) ? null : list;
	}

}
