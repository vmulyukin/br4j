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
package com.aplana.dbmi.service.impl;

import com.aplana.dbmi.PortalUser;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jdbc.DbmiJdbcTemplate;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.MessageException;
import com.aplana.dbmi.service.impl.access.PerformWorkflowMove;
import com.aplana.dbmi.service.impl.query.GetCard;
import com.aplana.dbmi.utils.QueryInspector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.nativejdbc.JBossNativeJdbcExtractor;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * Data access object (DAO) used for often used database-related operations.<br>
 * Typically you should work with instances of this class only through
 * spring (@link Database transaction proxy} which is managed by spring
 * application context under {@link DataServiceBean#BEAN_DATABASE} key.
 */
public class DatabaseBean extends JdbcDaoSupport implements Database
{
	/**
	 * Constant defining maximal period of time (in seconds) that could pass since
	 * last synchronization of inactive user to be able to set this user back
	 * to active state.<br>
	 * Currently set to 5 days
	 */
	public static final long USER_RESTORE_PERIOD = 5L * 24 * 60 * 60 * 1000;

	protected final Log logger = LogFactory.getLog(getClass());

	private QueryFactory queryFactory;
	private static ThreadLocal<QueryBase> currentQuery = new ThreadLocal<QueryBase>();

	@Override
	protected void initTemplateConfig()
	{
		getJdbcTemplate().setNativeJdbcExtractor(new JBossNativeJdbcExtractor());
		//getJdbcTemplate().setExceptionTranslator(new DBNamesSQLExceptionTranslator());
	}
	private LogEventBean logEventBean;

	public void setLogEventBean(LogEventBean logEventBean)  throws DataException {
		this.logEventBean = logEventBean;
	}

	/**
	 * @see Database#executeQuery(UserData, QueryBase)
	 */
	@SuppressWarnings("unchecked")
	public <T> T executeQuery(UserData user, QueryBase query) throws DataException
	{
		ErrorMessage error = null;
		try {
			query.setParentQuery(currentQuery.get());
			query.setUser(user);
			currentQuery.set(query);
			if (logger.isDebugEnabled()) {
				String parentUid = query.getParentQuery() == null ? "null" : query.getParentQuery().getUid().toString(); 
				logger.debug("Start query ---> "+query.getUid() +", parent = " + parentUid + "; event object = " + query.getEventObject()
					+": user = " + user.getPerson().getId().toString() + " (" + user.getPerson().getFullName() + ")");
			}
			if (query.getParentQuery() == null) {
				query.setLevel(1l);;
			} else {
				query.setLevel(query.getParentQuery().getLevel() + 1);
			}

			query.prepare();
			recalculateAccessForNewCard(user, query);	// ����������� ����� ��� ����� ��������
			validate(user, query);
			query.preProcess();

			long start = System.currentTimeMillis();
			QueryInspector.start(query, "query", query.getLevel());
			Object result = query.processQuery();
			QueryInspector.end(System.currentTimeMillis() - start, query.getLevel());

			// (YNikitin, 2013/05/16) �.�. � ����-���� �������� ���������� �������� � ������������� � ������� ����� ������� ����� �������� �������������� ������ � �������� ����� ������ (�������� ��� ����� ��������), ��
			// ������ � ����� ������� ������ ����� ����� ���������� ��������, � �� � ������ ��������� ���������� �������� � ��� ����-����
			if (error == null) {
				// PPanichev 03.10.2014
				//if ( logger.isInfoEnabled()) { ???? 
				LogEntry logEntry = query.getLogEntry();
				if (null != logEventBean && null != logEntry) {
					//�� �������� GET_CARD �� �������
					if(!user.getPerson().getId().equals(Person.ID_SYSTEM) || 
							!query.getLogEntry().getEvent().equals(GetCard.EVENT_ID)){
						logEventBean.logEventExt(user, query.getLogEntry());
					}
				}
			}
			result = query.postProcess(result);
			query.recalculateAccessList();
			return (T)result;
		} catch (ExceptionEnvelope e) {
			logger.error(query.getClass().getName() + ": caught exception", e.getSealedException());
			DataException dataException = e.getSealedException();
			if (query.getLogEntry() != null)
				error = new ErrorMessage(e, dataException, query.getLogEntry());
			throw dataException;
		} catch (RuntimeException e) {
			logger.error(query.getClass().getName() + ": unexpected exception", e);
			final StringBuilder sb = new StringBuilder();
			final StackTraceElement[] st = e.getStackTrace();
			for (StackTraceElement ste : st) {
				sb.append("\tat").append(ste.toString()).append("\r\n");
			}
			logger.error(sb.toString());
			DataException dataException = new DataException(DataException.ID_GENERAL_RUNTIME,
					new Object[] { DataException.RESOURCE_PREFIX + query.getClass().getName(), e.getMessage() }, e);
			if (query.getLogEntry() != null)
				error = new ErrorMessage(e, dataException, query.getLogEntry());
			throw dataException;
		}
		catch (MessageException e) {
			throw e;
		}
		catch (DataException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error(query.getClass().getName() + ": unknown exception", e);
			logger.error(e.getStackTrace().toString());
			DataException dataException = new DataException(DataException.ID_GENERAL_RUNTIME,
					new Object[] { DataException.RESOURCE_PREFIX + query.getClass().getName(), e.getMessage() }, e);
			if (query.getLogEntry() != null)
				error = new ErrorMessage(e, dataException, query.getLogEntry());
			throw dataException;
		}
		finally {
			currentQuery.set(query.getParentQuery());
			query.setParentQuery(null);
			try {
				// (YNikitin, 2013/05/16) � finally ����� ������ ������, �� ������ ��� �������� ����
				if (error != null && logger.isErrorEnabled()) {
					error.isSucces(0L);
					logEventBean.logEventExt(user, error);
				}
			} catch (Exception e) {
				logger.error(getClass().getName() + ":executeQuery" + ": caught exception", e);
			}
		}
	}

	/**
	 * �������� �� �� ��� � ���������� card ������������� �� null, �� 0, � �� ""
	 * @param card
	 * @return
	 */
	private boolean isNullId(Card card) {
		boolean result = false;
		if (card == null){
			result = true;
		}else if(card.getId() == null){
			result = true;
		}else if(card.getId().getId() instanceof Long){
			Long id = (Long)card.getId().getId();
			result = id.longValue() == 0;
		}else if(card.getId().getId() instanceof String){
			String id = (String)card.getId().getId();
			result = id.equals("");
		}
		return result;
	}

	/**
	 * @see Database#checkAccess(UserData, AccessCheckerBase)
	 */
	public boolean checkAccess(UserData user, AccessCheckerBase accessChecker) throws DataException
	{
        if (Person.ID_SYSTEM.equals(user.getPerson().getId()))
            return true;

		try {
			accessChecker.setJdbcTemplate(getJdbcTemplate());
			accessChecker.setUser(user);
			return accessChecker.checkAccess();
		} catch (ExceptionEnvelope e) {
			logger.error(accessChecker.getClass().getName() + ": caught exception", e.getSealedException());
			throw e.getSealedException();
		} catch (RuntimeException e) {
			logger.error(accessChecker.getClass().getName() + ": unexpected exception", e);
			throw new DataException(DataException.ID_GENERAL_RUNTIME,
					new Object[] { DataException.RESOURCE_PREFIX + accessChecker.getClass().getName(), e.getMessage() });
		}
	}

	/**
	 * @see Database#resolveUser(String)
	 */
	public Person resolveUser(String name) throws DataException
	{
		try {
			Person person = (Person) getJdbcTemplate().queryForObject(
					"SELECT person_id, person_login, full_name, email, sync_date, is_active, card_id " +
					"FROM person WHERE person_login=? AND (is_active=1 OR person_id=0)",
					new Object[] { name },
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							final Person person = new Person();
							person.setId(rs.getLong(1));
							person.setLogin(rs.getString(2));
							person.setFullName(rs.getString(3));
							person.setEmail(rs.getString(4));
							person.setSyncDate(rs.getDate(5));
							person.setActive(rs.getBoolean(6));
                            if (rs.getObject(7) != null)
							person.setCardId(new ObjectId(Card.class, rs.getLong(7)));
							return person;
						}
					});
			//syncPersonCard((Long)person.getCardId().getId(), person, true);
			return person;
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
			//throw new DataException("session.user", new Object[] { name });
		} catch (RuntimeException e) {
			logger.error("Resolving user: unexpected exception", e);
			throw new DataException("session.init", new Object[] { e.getMessage() });
		}
	}

	/**
	 * @see Database#syncUser(PortalUser)
	 */
	public void syncUser(PortalUser person) throws DataException
	{
		IdPair id = null;
		List<?> ids = getJdbcTemplate().query(
				"SELECT person_id, card_id FROM person WHERE person_login=? AND is_active=1",
				new Object[] { person.getLogin() },
				new IdPairMapper());
		if (ids.size() != 1){
			getJdbcTemplate().update(
					"UPDATE person SET is_active=0 WHERE person_login=?",
					new Object[] { person.getLogin() });
			for (Iterator<?> i = ids.iterator(); i.hasNext(); ) // sync each Person Card state with user
				syncPersonCard(((IdPair)i.next()).cardId, (Person)null, false);

			logger.warn("WARNING! Fixed " + ids.size() + " active records for user " + person.getLogin());
			try {
				id = (IdPair) getJdbcTemplate().queryForObject(
						"SELECT p.person_id, p.card_id FROM person p WHERE p.person_login=? AND p.sync_date>? AND p.sync_date=" +
							"(SELECT MAX(pm.sync_date) FROM person pm WHERE pm.person_login=p.person_login)",
						new Object[] { person.getLogin(), new Date(System.currentTimeMillis() - USER_RESTORE_PERIOD) },
						new IdPairMapper());
			} catch (IncorrectResultSizeDataAccessException e1) {
				logger.warn("WARNING ! syncUser: No such user");
				// It's ok, there's no such user
			}
		} else   // try to get only user
			try {
				id = (IdPair)ids.get(0);
			} catch (Exception e2) {// It's ok: there is no any user with such login
			}

		try {
			if (id == null) {
				long personId = getJdbcTemplate().queryForLong( "SELECT nextval('seq_person_id')");
				getJdbcTemplate().update(
						"INSERT INTO person (person_id, person_login, full_name, " +
											"email, sync_date, is_active) " +
						"VALUES (?, ?, ?, ?, ?, 1)",
						new Object[] { personId, person.getLogin(), person.getFullName(),
									person.getEmail(), new Date() },
						new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR,
									Types.VARCHAR, Types.TIMESTAMP });
				createPersonCard(person, personId);
				if (logger.isInfoEnabled()) {
					logger.info("[DataService] User " + person.getLogin() + " added");
				}
			} else {
				getJdbcTemplate().update(
						"UPDATE person SET full_name=?, email=?, is_active=1, sync_date=? WHERE person_id=?",
						new Object[] { person.getFullName(), person.getEmail(), new Date(), id.userId.getId() },
						new int[] { Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP, Types.NUMERIC });
				syncPersonCard(id.cardId, person, true);
				if (logger.isInfoEnabled()) {
					logger.info("[DataService] User " + person.getLogin() + " and his Card are updated");
				}
			}
		} catch (Exception e) {
			logger.error("Synchronizing user: unexpected exception", e);
			throw new DataException("sync.user.update", new Object[] { e.getMessage(), person.getLogin() });
		}
	}

	/**
	 * @see Database#clearUsers(Date)
	 */
	public void clearUsers(Date threshold) throws DataException
	{
		try {
			logger.info("[DEBUG] Removing all records older than " + threshold);
			int removed = getJdbcTemplate().update(
					"UPDATE person SET is_active=0, sync_date=? WHERE is_active=1 AND sync_date<?",
					new Object[] { new Date(), threshold });
			logger.info("[DataService] " + removed + " user(s) removed from DBMI inventory");
		} catch (DataAccessException e) {
			logger.error("Clearing users: unexpected exception", e);
			throw new DataException("sync.user.clear", new Object[] { e.getMessage() });
		}
	}

	private void syncPersonCard(ObjectId user_id, PortalUser person, boolean setActive) throws DataException{
		String fullName = null;
		String eMail = null;
		try {
			fullName = person.getFullName();
			eMail = person.getEmail();
		} catch (Exception e) {}
		syncPersonCard(user_id, fullName, eMail, setActive);
	}

	private void syncPersonCard(ObjectId user_id, Person person, boolean setActive) throws DataException{
		String fullName = null;
		String eMail = null;
		try {
			fullName = person.getFullName();
			eMail = person.getEmail();
		} catch (Exception e) {}
		syncPersonCard(user_id, fullName, eMail, setActive);
	}

	private void syncPersonCard(ObjectId userId, String fullName, String eMail, boolean setActive) throws DataException{
		if (userId == null)
			return;

		Person sysPerson = (Person)DataObject.createFromId(Person.ID_SYSTEM);
		UserData sysData = new UserData();
		sysData.setPerson(sysPerson);

		Card personCard = null;
		if (userId != null){
			ObjectQueryBase getCardQuery =
				queryFactory.getFetchQuery(Card.class);
			getCardQuery.setId(userId);
			try {
				personCard = executeQuery(sysData, getCardQuery);
			} catch (DataException e) {
				if ( ((Long) userId.getId()).longValue() != 0)
					logger.warn("Can't find internal person Card for user user_id="+userId.getId());
				// It's ok, we assume there is no such card
				return;
			}
			if (personCard == null)
				return;

			// If there is no changes for this attributes (First Name, Last Name, E-mail) in person card
			// then no need to sync person card
			if (!checkForChanges(personCard, fullName, eMail))
				return;

//--------------- try to lock Card before move along Workflow -----------------
			try {
				LockObject lockCardAction = new LockObject(personCard.getId());
				ActionQueryBase lockCardQuery = queryFactory.getActionQuery(lockCardAction);
				lockCardQuery.setAction(lockCardAction);
				lockCardQuery.setSessionId((int)Thread.currentThread().getId());
				executeQuery(sysData, lockCardQuery);
			} catch (Exception e) {
				logger.error("Can't lock internal person Card, id="+personCard.getId().getId());
				throw new DataException("sync.user.card.lock",
						new Object[] { e.getMessage(), personCard.getId().getId() });
			}

//-***------------ Add Name and Email -----------------------------------
			if ((fullName != null)&&(eMail != null)){
				ArrayList<String> tok = new ArrayList<String>(2);
				StringTokenizer st = new StringTokenizer(fullName.trim());
				while (st.hasMoreTokens())
					tok.add(st.nextToken());
				StringAttribute att = personCard.getAttributeById(
						ObjectId.predefined(StringAttribute.class, "jbr.person.lastName"));
				if (!(att == null))
					try {
						att.setValue(tok.get(0));
					} catch (Exception e) {}

				att = personCard.getAttributeById(
						ObjectId.predefined(StringAttribute.class, "jbr.person.firstName"));
				if (!(att == null))
					try {
						att.setValue(tok.get(1));
					} catch (Exception e) {}

				att = personCard.getAttributeById(
						ObjectId.predefined(StringAttribute.class, "jbr.person.email"));
				if (!(att == null))
					att.setValue(eMail);

				SaveQueryBase saveCardQuery = queryFactory.getSaveQuery(personCard);
				saveCardQuery.setObject(personCard);
				saveCardQuery.setSessionId((int)Thread.currentThread().getId());
				executeQuery(sysData, saveCardQuery);
			}
//----If need to change card status (make person active or inactive) then calculate WFM and do workflow move
			if (personCard.getState().equals(ObjectId.predefined(CardState.class, "user.active")) != setActive) {

				//--------------- constructing required Workflow object -----------------------
				WorkflowMove wMove;
				ObjectQueryBase getWFM = queryFactory.getFetchQuery(WorkflowMove.class);
				if (setActive)
					getWFM.setId(ObjectId.predefined(WorkflowMove.class, "user.makeActive"));
				else
					getWFM.setId(ObjectId.predefined(WorkflowMove.class, "user.makeInactive"));
				try {
					wMove = (WorkflowMove)executeQuery(sysData, getWFM);
				} catch (DataException e) {
					logger.error("DatabaseBean.syncPersonCard: no workflowMove object for activating/disactivating person card ", e);
					throw new DataException("sync.user.card.workflow",
							new Object[] { e.getMessage(), personCard.getId().getId() });
				}

				//--------------- performing Worlflow action -----------------------------------
				try {
					ChangeState changeStateAction = new ChangeState();
					changeStateAction.setCard(personCard);
					changeStateAction.setWorkflowMove(wMove);
					ActionQueryBase changeStateCardQuery =
							queryFactory.getActionQuery(changeStateAction);
					changeStateCardQuery.setAction(changeStateAction);
					changeStateCardQuery.setSessionId((int)Thread.currentThread().getId());
					executeQuery(sysData, changeStateCardQuery);
				} catch (Exception e) {
					logger.error("DatabaseBean.syncPersonCard: no workflowMove object for activating/disactivating person card ", e);
					throw new DataException("sync.user.card.workflow.move",
							new Object[] { e.getMessage(), personCard.getId().getId() });
				}
			}

//--------------- try to unlock Card after move along Workflow -----------------
			try {
				UnlockObject unlockCardAction = new UnlockObject(personCard.getId());
				ActionQueryBase unlockCardQuery = queryFactory.getActionQuery(unlockCardAction);
				unlockCardQuery.setAction(unlockCardAction);
				unlockCardQuery.setSessionId((int)Thread.currentThread().getId());
				executeQuery(sysData, unlockCardQuery);
			} catch (Exception e) {
				logger.error("Can't unlock internal person Card, id="+personCard.getId().getId());
				throw new DataException("sync.user.card.unlock",
						new Object[] { e.getMessage(), personCard.getId().getId() });
			}
		}
	}

	/**
	 * Creates new Card type internal person that corresponds given person parameter
	 * @param person
	 * @return {@link com.aplana.dbmi.action.Search} of new or existing internal person card that
	 * corresponds given person parameter
	 * @throws DataException
	 */
	private ObjectId createPersonCard(PortalUser person, long personId) throws DataException{
		if (person == null)
			return null;

		Person sysPerson = (Person)DataObject.createFromId(Person.ID_SYSTEM);
		UserData sysData = new UserData();
		sysData.setPerson(sysPerson);

		Card personCard = null;

		try {
			CreateCard createCardAction = new CreateCard(ObjectId.predefined(Template.class, "jbr.internalPerson"));
			ActionQueryBase newCardQuery =
				queryFactory.getActionQuery(createCardAction);
			newCardQuery.setAction(createCardAction);
			newCardQuery.setSessionId((int)Thread.currentThread().getId());
			personCard = executeQuery(sysData, newCardQuery);

			PersonAttribute owner = personCard.getAttributeById(
					ObjectId.predefined(PersonAttribute.class, "jbr.person.owner"));
			owner.setPerson(new ObjectId(Person.class, personId));

			ArrayList<String> tok = new ArrayList<String>(2);
			StringTokenizer st = new StringTokenizer(person.getFullName().trim());
			while (st.hasMoreTokens())
				tok.add(st.nextToken());

			StringAttribute att = personCard.getAttributeById(
					ObjectId.predefined(StringAttribute.class, "jbr.person.lastName"));
			if (!(att == null))
				try {
					att.setValue(tok.get(0));
				} catch (Exception e) {}

			att = personCard.getAttributeById(
					ObjectId.predefined(StringAttribute.class, "jbr.person.firstName"));
			if (!(att == null))
				try {
					att.setValue(tok.get(1));
				} catch (Exception e) {}

			att = personCard.getAttributeById(
					ObjectId.predefined(StringAttribute.class, "jbr.person.email"));
			if (!(att == null))
				att.setValue(person.getEmail());

			SaveQueryBase saveCardQuery = queryFactory.getSaveQuery(personCard);
			saveCardQuery.setObject(personCard);
			saveCardQuery.setSessionId((int)Thread.currentThread().getId());
			executeQuery(sysData, saveCardQuery);

			UnlockObject unlockCardAction = new UnlockObject(personCard.getId());
			ActionQueryBase unlockCardQuery = queryFactory.getActionQuery(unlockCardAction);
			unlockCardQuery.setAction(unlockCardAction);
			unlockCardQuery.setSessionId((int)Thread.currentThread().getId());
			executeQuery(sysData, unlockCardQuery);
			// ��� ���� ������� ���������� � �������� ���������� �������
			executeQuery(sysData, unlockCardQuery);

			return personCard.getId();
		} catch (Exception e) {
			logger.error("Can't create internal person card with sys user !");
			throw new DataException("sync.user.card.create", new Object[] { e.getMessage() });
		}
	}

	/**
	 * Checking person card for changes in specified attributes.
	 * If at least one of them has changed, then we need to sync person card.
	 *
	 * @param personCard
	 *            Person card which we will check for changes
	 * @param fullName
	 *            First name and Last name of Portal User
	 * @param eMail
	 *            e-mail E-Mail address of Portal User
	 * @return Is there need to sync person card. True - yes, need to sync. False - no.
	 */
	private boolean checkForChanges(Card personCard, String fullName, String eMail) {
		if (fullName != null && eMail != null) {
			ArrayList<String> tok = new ArrayList<String>(2);
			StringTokenizer st = new StringTokenizer(fullName.trim());
			while (st.hasMoreTokens())
				tok.add(st.nextToken());
			StringAttribute att = personCard.getAttributeById(ObjectId.predefined(StringAttribute.class, "jbr.person.lastName"));
			if (att != null && (att.getValue() == null || !att.getValue().equals(tok.get(0)))) {
				return true;
			}

			att = personCard.getAttributeById(ObjectId.predefined(StringAttribute.class, "jbr.person.firstName"));
			if (att != null && (att.getValue() == null || !att.getValue().equals(tok.get(1)))) {
				return true;
			}

			att = personCard.getAttributeById(ObjectId.predefined(StringAttribute.class, "jbr.person.email"));
			if (att != null && (att.getValue() == null || !att.getValue().equals(eMail))) {
				return true;
			}
		}
		return false;
	}

	public QueryFactory getQueryFactory() {
		return queryFactory;
	}

	public void setQueryFactory(QueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

    private class IdPairMapper implements RowMapper{

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new IdPair(rs.getLong(1), rs.getLong(2));
		}
	}

    private class IdPair{
        public ObjectId userId;
        public ObjectId cardId;

        public IdPair(long userId, long cardId){
            this.userId = new ObjectId(Person.class, userId);
            this.cardId = new ObjectId(Card.class, cardId);
		}
	}

	@Override
	protected JdbcTemplate createJdbcTemplate(DataSource dataSource) {
		return new DbmiJdbcTemplate(dataSource);
	}

	public void validate(UserData user, QueryBase query) throws DataException {
		query.setJdbcTemplate(getJdbcTemplate());
		query.setUser(user);
		if (query.getSessionId() == null) {
			query.setSessionId(query.getParentQuery() == null ? null : query.getParentQuery().getSessionId());
		}
		if (!query.checkAccess()) {
			// ��������� ����� �������� ����������
			AccessCheckerBase accessChecker = query.getAccessChecker();

			String[] errorParam = new String[5];

			errorParam[0] = accessChecker.getClass().getSimpleName();
			if (accessChecker.getClass().equals(PerformWorkflowMove.class)) {
				ChangeState action = accessChecker.getAction();
				if (action.getWorkflowMove().getName() != null) {
					errorParam[0] += " ["
							+ action.getWorkflowMove().getName().getValueRu()
							+ "]";
				}
			}

			if (accessChecker.getObject() != null) {
				String templateName = "";
				String status = "";
				if (accessChecker.getObject() instanceof Card) {
					Card card = (Card) accessChecker.getObject();
					// �������� �� �� ��� id �� (null ��� 0 ��� "") ����� ��
					// ������ ������ ��������
					if (!isNullId(card)) {
						if (card.getTemplate() == null) {
							ObjectQueryBase getCardQuery = queryFactory
									.getFetchQuery(Card.class);
							getCardQuery.setId(card.getId());
							getCardQuery.setAccessChecker(null);
							try {
								card = executeQuery(user, getCardQuery);
							} catch (DataException e) {
								// ���������� ������. � ���� ������ � ����������
								// � ���������� ���� ����� ������ ����� �������
								// ������ ������
							}
						}
						templateName = card.getTemplateNameRu();
						status = card.getStateName().getValueRu();
					}
				}
				errorParam[1] = templateName;
				errorParam[2] = status;
				errorParam[3] = accessChecker.getObject().getId().getId()
						.toString();
				errorParam[4] = user.getPerson().getFullName();
			}

			throw new DataException(DataException.ID_GENERAL_ACCESS, errorParam);
		}
		query.validate();
	}

	/**
	 * ����������� ����� ��� ����� ��������, ���� � ����� ���������� � � ��������� ���������� �������� � �� �� ����� �������
	 * @param query
	 * @throws DataException
	 */
	private void recalculateAccessForNewCard(UserData user, QueryBase query) throws DataException{
		if (!(query instanceof GetCard)||user==null||user.getPerson().getId().equals(Person.ID_SYSTEM)){
			return;
		}
		ObjectId cardId = ((GetCard)query).getId();
		query.getPrimaryQuery().recalculateAccessList(cardId);
	}
}