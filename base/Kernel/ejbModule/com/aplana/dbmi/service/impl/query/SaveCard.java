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
package com.aplana.dbmi.service.impl.query;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.ProcessPseudoCardAttributes;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.AttributeViewParam;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.Group;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.LongAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.PortalUserLoginAttribute;
import com.aplana.dbmi.model.PseudoAttribute;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.UserRolesAndGroupsAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.SecurityAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.SmartSaveConfig;
import com.aplana.dbmi.utils.AttributeSqlBuilder;
import com.aplana.dbmi.utils.AttributeSqlBuilder.InsertAttributesSQLBuilder;
import com.aplana.dbmi.utils.SimpleDBUtils;

/**
 * {@link SaveQueryBase} descendant used to save instances of {@link Card} class
 */
public class SaveCard extends SaveQueryBase {
	private static final long serialVersionUID = 1L;

	protected final Log logger = LogFactory.getLog(getClass());

	// public static boolean EnableSmartAttributesSave = true;

	/**
	 * Identifier of 'New card created' log event
	 */
	public static final String EVENT_ID_CREATE = "NEW_CARD";
	/**
	 * Identifier of 'Card changed' log event
	 */
	public static final String EVENT_ID_CHANGE = "CHG_CARD";

	/**
	 * Checks validity of {@link Card} instance being saved
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void validate() throws DataException
	{
		final Card card = (Card) getObject();

		Boolean activeTemplate = null;
		try {
			activeTemplate = (Boolean) getJdbcTemplate().queryForObject(
					"SELECT is_active FROM template WHERE template_id=?",
					new Object[] { card.getTemplate().getId() },
					new int[] { Types.NUMERIC }, // (2010/03) POSTGRE
					Boolean.class);
		} catch (IncorrectResultSizeDataAccessException e) {
			throw new DataException("store.card.template");
		}
		if (!activeTemplate.booleanValue())
			throw new DataException("store.card.templateold");

		Card storedCard = null;
		if (card.getId() != null) {
			ObjectQueryBase subQuery = getQueryFactory().getFetchQuery(Card.class);
			subQuery.setId(card.getId());
			storedCard = (Card) getDatabase().executeQuery(getUser(), subQuery);
		} else {
			ActionQueryBase subQuery = getQueryFactory().getActionQuery(CreateCard.class);
			subQuery.setAction(new CreateCard((Template) DataObject.createFromId(card.getTemplate())));
			storedCard = (Card) getDatabase().executeQuery(getUser(), subQuery);
		}

		if (card.getId() == null) {
			long initialState = getJdbcTemplate().queryForLong(
					"select w.initial_status_id from workflow w where exists (select 1 from template t where t.template_id = ? and t.workflow_id = w.workflow_id)",
					new Object[] {card.getTemplate().getId()},
					new int[] {Types.NUMERIC}
			);
			if (!card.getState().getId().equals(new Long(initialState))) {
				throw new DataException("store.card.initialstate");
			}
		}

		final Map<Object, AttributeViewParam> avpMap = getAttrViewParamMap(card);
		for( TemplateBlock block : card.<TemplateBlock>getAttributes() )
		{
			for( Iterator<Attribute> attrItr = ((Collection<Attribute>) block.getAttributes()).iterator();
				attrItr.hasNext(); )
			{
				final Attribute attr = attrItr.next();
				final Attribute storedAttr = storedCard.getAttributeById(attr.getId());
				if (storedAttr == null)
					throw new DataException("store.card.attribute", new Object[] {
									attr.getNameRu(), attr.getNameEn()
								});

				final AttributeViewParam avp = avpMap.get(attr.getId().getId());
				final boolean isReadonly;
				if(Person.ID_SYSTEM.equals(getUser().getPerson().getId())){//"�������" ����� ������ ��� ��������
					isReadonly = false;
				} else {
					isReadonly = (avp == null) ? storedAttr.isReadOnly() : avp.isReadOnly();					
				}
				if (!storedAttr.isActive() ||
						//AttributeBlock.ID_REST.equals(storedAttr.getBlockId()) ||
						AttributeBlock.ID_REMOVED.equals(storedAttr.getBlockId()))
				{
					/*if (!attr.equalValue(storedAttr))
						throw new DataException("store.card.attributeold",
								new Object[] { attr.getNameRu(), attr.getNameEn() });*/
					logger.info("[DataService] Removing inactive attribute " + attr.getName() + " [" + attr.getId().getId() + "] \n"
								+ "\t stored value: '" + storedAttr.getStringValue() + "' \n"
								+ "\t new value   : '" + attr.getStringValue() + "' \n"
						);
					attrItr.remove();
				} else if (isReadonly) {
					if (!attr.equalValue(storedAttr)) {
						logger.warn("[DataService] (!) Changing value of read-only attribute " + attr.getName()
								+ " [" + attr.getId().getId() + "] REFUSED: \n"
								+ "\t stored value: '" + storedAttr.getStringValue() + "' \n"
								+ "\t dropped new : '" + attr.getStringValue() + "' \n"
							);
						copyValue(attr, storedAttr); // (!) replace the new value by stored readonly value
						// attrItr.remove();
						// block.getAttributes().add(storedAttr);
					}
				} else if (attr instanceof TreeAttribute) {
					final TreeAttribute treeAttr = (TreeAttribute) attr;
					if (treeAttr.getValues() == null)
						continue;
					for( Iterator<ReferenceValue> valItr = treeAttr.getValues().iterator();
						valItr.hasNext(); )
					{
						final ReferenceValue value = valItr.next();
						if (ReferenceValue.ID_ANOTHER.equals(value.getId()) && (value.getValueRu() == null || value.getValueRu().length() == 0)
								&& (value.getValueEn() == null || value.getValueEn().length() == 0))
							throw new DataException("store.card.attribute.othervalue", new Object[] {
									attr.getNameRu(), attr.getNameEn()
							});
					}
				}
			}
		}
		super.validate();
	}

	/**
	 * ��������� ��� �������� ������ view-���������.
	 * @param card
	 * @return ��������� <Object, AttributeViewParam>
	 * @throws DataException
	 */
	@SuppressWarnings("unchecked")
	private Map<Object, AttributeViewParam> getAttrViewParamMap(Card card) throws DataException {
		ChildrenQueryBase avpQuery;
		if (card.getId() != null) {
			avpQuery = getQueryFactory().getChildrenQuery(Card.class, AttributeViewParam.class);
			avpQuery.setParent(card.getId());
		} else {
			avpQuery = getQueryFactory().getChildrenQuery(Template.class, AttributeViewParam.class);
			avpQuery.setParent(card.getTemplate());
		}
		final Collection<AttributeViewParam> attrViewParamCollection =
				(Collection<AttributeViewParam>)getDatabase().executeQuery(getUser(), avpQuery);
		final Map<Object, AttributeViewParam> result = new HashMap<Object, AttributeViewParam>();
		for ( AttributeViewParam avp: attrViewParamCollection) {
			result.put(avp.getId().getId(), avp);
		}
		return result;
	}

	@Override
	public String getEvent() {
		return isNew() ? EVENT_ID_CREATE : EVENT_ID_CHANGE;
	}

	protected final PersonAttribute markAuthorByCurrentUser(Card card) {
		if (card == null) return null;
		final PersonAttribute author = (PersonAttribute) card.getAttributeById(Attribute.ID_AUTHOR);
		if (author != null)
			author.setPerson(getUser().getPerson());
		return author;
	}

	protected final DateAttribute markDateByNow(Card card, ObjectId attrId) {
		if (card == null) return null;
		final DateAttribute dateAttr = (DateAttribute) card.getAttributeById(attrId);
		if (dateAttr != null)
			dateAttr.setValue(new Date());
		return dateAttr;
	}

	@Override
	protected ObjectId processNew() throws DataException {
		final Card card = (Card) getObject();

		markAuthorByCurrentUser(card);
		markDateByNow(card, Attribute.ID_CREATE_DATE);
		markDateByNow(card, Attribute.ID_CHANGE_DATE);

		// (2010/03) POSGRE
		// OLD: card.setId(getJdbcTemplate().queryForLong("SELECT seq_card_id.nextval FROM dual"));
		//��� ���������� ���������� ����������������� �������������
		if (card.getReserveId() == 0){
			card.setId(getJdbcTemplate().queryForLong("SELECT nextval('seq_card_id')"));
		}else{
			card.setId(card.getReserveId());
		}

		getJdbcTemplate().update(
				"INSERT INTO card (card_id, template_id, is_active," /*parent_card_id, "*/ +
				/* "file_name, external_path, */"status_id) " +
				"VALUES (?, ?, ?, ?)",
				new Object[] { card.getId().getId(), card.getTemplate().getId(), card.isActive(), card.getState().getId() },
				new int[] {    Types.NUMERIC,        Types.NUMERIC,              Types.NUMERIC,   Types.NUMERIC  }
		);
		ActionQueryBase subQuery = getQueryFactory().getActionQuery(LockObject.class);
		subQuery.setAction(new LockObject(card.getId()));
		subQuery.setSessionId(this.getSessionId());
		getDatabase().executeQuery(getUser(), subQuery); //���������� ������� ��������� ��� �������� ������ ����� ���
		getDatabase().executeQuery(getUser(), subQuery); //��� ��� �� ���������� �������� �������������� ���������� �� �������������

		insertAttributes(card);
		logger.info("new card saved: id= "+ card.getId());
		// � ����� ����������� ��� ��������� ������� ���� ����� ����������� ����� ���������� ���� ����-����������� ������������� �����
		this.getPrimaryQuery().putCardIdInRecalculateAL(card.getId());

		return card.getId();
	}

	final static private int MAX_UPDATE_RETRIES = 2;

	@Override
	protected void processUpdate() throws DataException
	{
		// �������� ���������� �������� � DoOverwriteCardAttributes,
		// ������� ���������� � insertAttributes
		// checkLock("card", "card_id");
		final Card card = (Card) getObject();
		final Object cardId = card.getId().getId();

		cleanAccessList(card.getId());

		markDateByNow(card, Attribute.ID_CHANGE_DATE);
		boolean retryUpdate;
		int retryCount = 0;
		do {
			retryUpdate = false;
			++retryCount;

			final SmartSaveConfig ssCfg = getSmartSaveConfig();
			boolean enSmartSave = false;
			boolean enCheckWriteByRead = false;
			String hardSaveAttributes = null;
			SmartSaveConfig.ActionOnErrorCheckWriteByRead act =  SmartSaveConfig.ActionOnErrorCheckWriteByRead.actIgnore;
			if (ssCfg != null) {
				try {
					enSmartSave =  ssCfg.isEnSmartSaveMode();
					enCheckWriteByRead = ssCfg.isEnChkWriteByRead();
					act = ssCfg.getActionOnErrorCheckWriteByRead();
					hardSaveAttributes = ssCfg.getHardSaveAttributes();
				} catch (RemoteException ex) {
					logger.error(ex);
				}
			}
			logger.debug("smart save is " + (enSmartSave ?"ENABLED" :"DISABLED")
					+ ", check write by read is " + (enCheckWriteByRead ?"ENABLED" :"DISABLED")
					+ ", on error action is " + act
			);

			final InsertAttributesSQLBuilder builder;
			if (hardSaveAttributes!=null && hardSaveAttributes!="")
				builder = new InsertAttributesSQLBuilder(hardSaveAttributes);
			else builder = new InsertAttributesSQLBuilder();

			{
				int countDel;
				if (enSmartSave) {
					// �������� ������ ���������� ��������� ...
					countDel = smartDeleteAttributes(card, builder);
				} else {
					// ������� ����� ������, ����� ��� ������� �������� ������ ��������� ...
					countDel = getJdbcTemplate().update(
							"DELETE FROM attribute_value avDest WHERE avDest.card_id=?",
							new Object[] { cardId },
							new int[] { Types.NUMERIC }
					);
				}
				if (logger.isDebugEnabled())
					logger.debug( "removed "+ countDel+ " attributes from 'attribute_value' for card "
							+ cardId + " using smartSave="+ enSmartSave);
			}

			/*
			 * ����������� ACL ...
			 */
			{
				final int aclDel = getJdbcTemplate().update(
						"DELETE FROM access_control_list WHERE card_id=?",
						new Object[] { cardId },
						new int[] { Types.NUMERIC }
				);
				if (logger.isDebugEnabled())
					logger.debug( "removed "+ aclDel+ " records from 'access_control_list' for card "+ cardId);
			}

			/* ���������� ��������� �������� ... */
			insertAttributes(card);

			//update access_list
			// � ����� ����������� ��� ��������� ������� ���� ����� ����������� ����� ���������� ���� ����-����������� ������������ �����
			this.getPrimaryQuery().putCardIdInRecalculateAL(card.getId());
			
			/*
			 * �������� ������ ������� ...
			 */
			if (enSmartSave && enCheckWriteByRead)
			{
				logger.debug( "checking write by read for card " + cardId);
				int curDbCount = getJdbcTemplate().queryForInt(
						"select count(*) from attribute_value av where av.card_id = ?",
						new Object[]{ cardId }, new int[]{ Types.NUMERIC } );

				final int saveCount = builder.getEmmitedAttrs().size();
				if ( saveCount == curDbCount) {
					logger.debug("'check write by read' is OK for card "+ cardId+ ", attribute counter is "+ curDbCount);
				} else {
					logger.error("PROBLEM DETECTED by 'check write by read' for card "+ cardId
							+ ": attr counter is "+ saveCount
							+ " <> current db attr counter "+ curDbCount
							+ "; 'on error action' configured as " + act);
					if (logger.isTraceEnabled()) {
						traceBuilderAttr( builder);
						traceSimpleDbAttr( cardId );
					}
					switch (act) {
						case actRaise:
							throw new DataException("jbr.card.check.write.byread_1", new Object[] {cardId} );

						case actSwitchToSimpleMode:
							logger.warn("switching to SimpeWriteMode and retying ...");
							try {
								if (ssCfg != null) ssCfg.setEnSmartSaveMode(false);
							} catch (RemoteException ex) {
								throw new DataException(ex);
							}
							retryUpdate = true;
							break;

						case actIgnore:
						default:
							// �������������� (�� ����) � ������ ������ ...
							break;
					}
				}
			}
			if (retryCount > MAX_UPDATE_RETRIES) {
				logger.warn("updateCard fail on all of "+ MAX_UPDATE_RETRIES+ " retries  -> raise exception");
				throw new DataException("jbr.card.check.write.byread_1", new Object[] {cardId} );
			}
		} while ( retryUpdate );

		logger.info( "exist card saved: id= "+ card.getId());
	}

	/**
	 * @param builder
	 */
	private void traceBuilderAttr(InsertAttributesSQLBuilder builder) {
		final StringBuffer sbuf = new StringBuffer();
		sbuf.append("\nCurrent saving data:");
		sbuf.append("\n\tNN \t attr_code");
		int i = 0;
		for(Attribute attr: builder.getEmmitedAttrs()) {
			i++;
			sbuf.append(MessageFormat.format("\n\t[{0}] \t {1}", i, attr.getId()));
		}
		logger.trace(sbuf);
	}

	/**
	 * @param cardId
	 */
	private void traceSimpleDbAttr(Object cardId) {
		final StringBuffer dstBuf = new StringBuffer();
		getJdbcTemplate().query(
				"select * from attribute_value av where av.card_id = ? order by av.attribute_code, av.number_value",
				new Object[]{ cardId }, new int[]{ Types.NUMERIC },
				new ResultSetExtractor(){

					public Object extractData(ResultSet rs)
							throws SQLException, DataAccessException
					{
						SimpleDBUtils.makeInfoColumns(rs.getMetaData(), dstBuf);
						SimpleDBUtils.makeInfoDataSet(rs, dstBuf, 100);
						return rs;
					}}
			);
		logger.trace(dstBuf);
	}

	private SmartSaveConfig smartSaveCfg = null;
	public SmartSaveConfig getSmartSaveConfig()
	{
		if (smartSaveCfg == null)
			smartSaveCfg = (SmartSaveConfig) getBeanFactory().getBean( SmartSaveConfig.BEAN_SSMGRCFG, SmartSaveConfig.class);
		return smartSaveCfg;
	}


	/**
	 * �������� �� �������� ������ ���������� ���������, �.�. ����� ������� ���
	 * ����� ����� ������������ � ��������.
	 * @param card ������������ ��������.
	 * @return ���-�� �������� ���������.
	 * @throws DataException
	 */
	private int smartDeleteAttributes(Card card, InsertAttributesSQLBuilder builder)
		throws DataException
	{
		if (card == null || card.getId() == null)
			return -1;

		final Long cardId = (Long) card.getId().getId();

		final List<Attribute> allAttributes = collectAllAttributes(card);

		// ������������ ������ ��������� � ���� "select A1 union select A2 ..."
		// true, ���� ���� ��������� �����-���� ������
		final boolean hasNewData = builder.emmitSelectAttributes( allAttributes, "\t\t\t");

		// �������� ������ ���� ���� ��� ...
		final StringBuffer sqlDelOld = AttributeSqlBuilder.makeSqlDeleteOldByCardId(
				builder, "avDelOld", null);
		builder.insertArg( 0, cardId, Types.NUMERIC );

		/* �������������� ������ ������� �� �������� � ��� ���������� ... */
		if (logger.isTraceEnabled()) {
			logger.trace( "delete SQL is "
					+ com.aplana.dbmi.utils.SimpleDBUtils.getSqlQueryInfo( sqlDelOld.toString(), builder.args(), builder.types()));

			/* �������������� ��������� ������ ... */
			tracePredelete(builder, hasNewData);
		}

		/* (!) ��������... */
		final int countDel = getJdbcTemplate().update( sqlDelOld.toString(), builder.getPreparedStatementSetter() );
		return countDel;
	}

	boolean enTraceDelete = true; // TODO

	/**
	 * @param builder
	 * @param hasNewData
	 * @throws DataAccessException
	 */
	private void tracePredelete(InsertAttributesSQLBuilder builder,
			final boolean hasNewData) throws DataAccessException
	{
		if (!enTraceDelete)
			return;

		final StringBuffer sqlPreviewDel = AttributeSqlBuilder.makeSqlSelectOldByCardId(
				builder, "avDelOld", null);
		final StringBuffer dstBuf = new StringBuffer("\n preview deleteing records:>>>");
		getJdbcTemplate().query( sqlPreviewDel.toString(),
				builder.getPreparedStatementSetter(),
				new ResultSetExtractor() {
					public Object extractData(ResultSet rs)
							throws SQLException, DataAccessException
					{
						SimpleDBUtils.makeInfoColumns(rs.getMetaData(), dstBuf);
						SimpleDBUtils.makeInfoDataSet(rs, dstBuf, 50);
						return rs;
					}});
		dstBuf.append("<<<\n");
		logger.trace(dstBuf);
	}

	/**
	 * �������� �������� �������� � ������� attribute_value � access_control_list.
	 * @param card
	 * @throws DataException
	 */
	private void insertAttributes(final Card card)
		throws DataException
	{
		long cardId = (Long) card.getId().getId();
		if (card.getMaterialType() == Card.MATERIAL_URL) {
			final int count = getJdbcTemplate().update(
				"UPDATE card SET file_storage1=NULL" +
				", file_name = NULL" +
				", external_path = ?" +
				" WHERE card_id=?",
				new Object[] { card.getUrl(), cardId},
				new int[] { Types.VARCHAR, Types.NUMERIC });
			if (logger.isDebugEnabled())
				logger.debug( "updated "+ count+ " file_storage, file_name, external_path in 'card' by id "+ cardId);
		} else {
			final int count = getJdbcTemplate().update(
				"UPDATE card SET external_path = null" +
				" WHERE card_id=?",
				new Object[] { cardId},
				new int[] { Types.NUMERIC });
			if (logger.isDebugEnabled())
				logger.debug( "updated "+ count+ " external_path in 'card' by id "+ cardId);
		}

		final List<Attribute> allAttributes = collectAllAttributes(card);
		
		final OverwriteCardAttributes action = new OverwriteCardAttributes();
		action.setCardId(card.getId());
		action.setAttributes(allAttributes);
		action.setInsertOnly( true);

		ActionQueryBase q = getQueryFactory().getActionQuery(action);
		q.setAction(action);
		getDatabase().executeQuery(getUser(), q);
		
		final List<PseudoAttribute> pseudoAttributes = collectPseudoAttributes(allAttributes);
		if (!pseudoAttributes.isEmpty()) {
			final ProcessPseudoCardAttributes pAction = new ProcessPseudoCardAttributes();
			pAction.setCardId(card.getId());
			pAction.setAttributes(pseudoAttributes);
			q = getQueryFactory().getActionQuery(pAction);
			q.setAction(pAction);
			getDatabase().executeQuery(getUser(), q);
		}
	}

	/**
	 * @param card
	 * @return �������� ����� ���� ��������� ��������.
	 */
	private List<Attribute> collectAllAttributes(final Card card) {
		final HashMap<ObjectId, Attribute> allAttributesMap = new HashMap<ObjectId, Attribute>();
		for (AttributeBlock block : card.<AttributeBlock>getAttributes()) {
			for( Attribute attr : block.getAttributes()) {
				allAttributesMap.put(attr.getId(), attr);
			}
		}
		ArrayList<Attribute> allAttributes = new ArrayList<Attribute>();
		allAttributes.addAll(allAttributesMap.values());
		return allAttributes;
	}
	
	/**
	 * @param card
	 * @return ����� ������-��������� ��������.
	 */
	private List<PseudoAttribute> collectPseudoAttributes(final List<Attribute> attrList) {
		ArrayList<PseudoAttribute> pseudoAttributes = new ArrayList<PseudoAttribute>();
		for( Attribute attr : attrList) {
			if (attr instanceof PseudoAttribute) {
				pseudoAttributes.add((PseudoAttribute)attr);
			}
		}
		return pseudoAttributes;
	}

	// TODO: ������� � ����������� ����� ������ � ����������
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void copyValue(Attribute dest, Attribute src) {
		if (!dest.getClass().isAssignableFrom(src.getClass()))
			throw new IllegalArgumentException("Attribute type mismatch");
		else if (Attribute.TYPE_STRING.equals(dest.getType()))
			((StringAttribute) dest).setValue(((StringAttribute) src).getValue());
		else if (Attribute.TYPE_TEXT.equals(dest.getType()))
			((TextAttribute) dest).setValue(((TextAttribute) src).getValue());
		else if (Attribute.TYPE_INTEGER.equals(dest.getType()))
			((IntegerAttribute) dest).setValue(((IntegerAttribute) src).getValue());
        else if (Attribute.TYPE_LONG.equals(dest.getType()))
			((LongAttribute) dest).setValue(((LongAttribute) src).getValue());
		else if (Attribute.TYPE_DATE.equals(dest.getType()))
			((DateAttribute) dest).setValue(((DateAttribute) src).getValue());
		else if (Attribute.TYPE_LIST.equals(dest.getType()))
			((ListAttribute) dest).setValue(((ListAttribute) src).getValue());
		else if (Attribute.TYPE_TREE.equals(dest.getType()))
			((TreeAttribute) dest).setValues(((TreeAttribute) src).getValues());
		else if (Attribute.TYPE_PERSON.equals(dest.getType()))
			((PersonAttribute) dest).setValues(((PersonAttribute) src).getValues());
		else if (Attribute.TYPE_SECURITY.equals(dest.getType()))
			((SecurityAttribute) dest).setAccessList(((SecurityAttribute) src).getAccessList());
		else if (Attribute.TYPE_CARD_LINK.equals(dest.getType())) {
			// (2010/02, RuSA) OLD: ((CardLinkAttribute) dest).setValues(((CardLinkAttribute) src).getValues());
			// ((CardLinkAttribute) dest).setIdsLinked( ((CardLinkAttribute) src).getIdsLinked() );
			// ((CardLinkAttribute) dest).setLabelLinkedCards(((CardLinkAttribute) src).getLabelLinkedCards());
			final Map destMap = ((CardLinkAttribute) dest).getLabelLinkedMap();
			destMap.clear();
			destMap.putAll( ((CardLinkAttribute) src).getLabelLinkedMap());
		} else if (Attribute.TYPE_TYPED_CARD_LINK.equals(dest.getType())) {
			// (2010/02, RuSA) OLD: ((TypedCardLinkAttribute) dest).setValues(((TypedCardLinkAttribute) src).getValues());
			// ((TypedCardLinkAttribute) dest).setIdsLinked(((TypedCardLinkAttribute) src).getIdsLinked());
			// ((TypedCardLinkAttribute) dest).setLabelLinkedCards(((TypedCardLinkAttribute) src).getLabelLinkedCards());
			final Map destMap = ((CardLinkAttribute) dest).getLabelLinkedMap();
			destMap.clear();
			destMap.putAll( ((CardLinkAttribute) src).getLabelLinkedMap());
			((TypedCardLinkAttribute) dest).setTypes(((TypedCardLinkAttribute) src).getTypes());
		} else if (Attribute.TYPE_DATED_TYPED_CARD_LINK.equals(dest.getType())) {
			final Map destMap = ((CardLinkAttribute) dest).getLabelLinkedMap();
			destMap.clear();
			destMap.putAll( ((CardLinkAttribute) src).getLabelLinkedMap());
			((DatedTypedCardLinkAttribute) dest).setTypes(((DatedTypedCardLinkAttribute) src).getTypes());
			((DatedTypedCardLinkAttribute) dest).setDates(((DatedTypedCardLinkAttribute) src).getDates());
		}
		else if (Attribute.TYPE_HTML.equals(dest.getType()))
			((HtmlAttribute) dest).setValue(((HtmlAttribute) src).getValue());
		else if (Attribute.TYPE_MATERIAL.equals(dest.getType()) ||
				Attribute.TYPE_BACK_LINK.equals(dest.getType()))
			return;		// no need to copy
		else if (dest instanceof PseudoAttribute)
			return;
		else
			throw new IllegalArgumentException("Unknown attribute type");
	}
	
	@Override
	public String toString() {
		DataObject obj = getObject();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String startDate = sdf.format(getQueryContainer().getCreationTime());
		
		StringBuilder sb = new StringBuilder();
		sb.append("� ").append(startDate);
		sb.append(" ������������ ").append(getUser().getPerson().getFullName());
		sb.append(" ��������(�) \"����������\" ");
		if (obj instanceof Card) {
			sb.append("�������� \"");
			sb.append(((Card)obj).getAttributeById(ObjectId.predefined(StringAttribute.class, "name")).getStringValue());
		} else {
			sb.append("������� \"").append(obj.getClass().getSimpleName());
		}
		sb.append("\" (��� = ").append(obj.getId().getId()).append(")");
		return sb.toString();
	}
}
