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
/**
 * 
 */
package com.aplana.dbmi.jbr.processors.dbproc;

import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;

/**
 * @author RAbdullin
 * (2011/03/30, YNikitin) добавил записывание ID-ников найденных карточек дерева в целевой атрибут ("_ID")
 * (2011/08, RuSA) Разделение альтернативных по сути веток - работа с персонами, либо использование "_ID".
 */

// TODO: адаптировать также и для использования вместо TreeCopyPersonsFromDeepCards
public class ProcDynaRole extends ProcessCard {

	private static final long serialVersionUID = 1L;

	/**
	 * название исходного атрибута для сбора id-ников карточек дерева
	 */
	final static String ID_ATTRIBUTE = "_ID";

	/**
	 * Список атрибутов для сбора персон (U-, C-, E- атрибуты) внутри карточек.
	 */
	private Set<ObjectId> srcAttrIds;

	/**
	 * Список C-атрибутов для построения дерева карточек.
	 */
	private Set<ObjectId> nodeLinkAttrIds;

	/**
	 * Является ли текущий узел - корнем/началом дерева?
	 * 		true: является, false(по умол) нет и надо искать get_root_card
	 */
	private boolean rootIsCurrent = false;

	/**
	 * Список атрибутов для сбора персон (U-, C-, E- атрибуты) внутри карточек.
	 */
	// private List<ObjectId> dstCopyToAttrIds = new ArrayList<ObjectId>();

	/**
	 * Целевой список "шаблон + атрибут" виде:
	 * 		id-шаблона  ->  целевой элемент 
	 */
	private Map<ObjectId, Destination> destinations = new HashMap<ObjectId, Destination>(1);

	/**
	 * true = дополнять целевой атрибут 
	 * или false (по умол) = замещать;
	 */
	private boolean append = false;

	/**
	 * true = записывать результат только в текущую карточку;
	 * false = (по умол) во всё дерево;
	 */
	private boolean destIsCurrent = false;

	/**
	 * Разрешённые состояния для карточек узлов дерева, для выбора их person-значений
	 */
	private Set<ObjectId> nodeCardsStateIds;
	
	/**
	**Запрещенные состояния для карточек узлов дерева
	 */
	private Set<ObjectId> nodeCardsNotStateIds;

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#process()
	 */
	@Override
	public Object process()
			throws DataException
	{
		final long start_ms = System.currentTimeMillis();

		final ObjectId curNodeId = getCardId();

		// DONE Добавит проверку установки параметров !		
		logger.debug("processing card " + curNodeId + " into attributes " + destinations);
		if (curNodeId == null || curNodeId.getId() == null) {
			logger.debug(" current node (card) id is null -> exiting");
			return null;
		}
		if (this.destinations == null || this.destinations.isEmpty()) {
			logger.debug(" no destinations attributes -> exiting");
			return null;
		}

		if (this.srcAttrIds == null || this.srcAttrIds.isEmpty()) {
			logger.debug(" no source attributes -> exiting");
			return null;
		}

		// атрибуты-источники персон ...
		final String srcAttrCodes = IdUtils.makeIdCodesQuotedEnum(this.srcAttrIds);

		// запрос получения всего дерева карточек для данной обработки ...
		final String sqlGetTree_1 =  this.buildSelectGetTreeCards();

		/**
		 * получение id целевых карточек ...
		 */
		final String sDestCardIds_1 = (destIsCurrent) ? ":card_id" : sqlGetTree_1;

		/**
		 * Фильтр по целевым шаблонам вида:
		 * 		( avDest.attribute_code in (...) and avDest.template_id = T1)
		 * 		or ( avDest.attribute_code in (...) and avDest.template_id = T2)
		 */
		final StringBuffer destTemplateFilter = buildDestTemplateFilter("avDest");

		/**
		 * (1) Получение персон либо по Id-карточек...
		 */
		// TODO: возможно стоит убрать ID_ATTRIBUTE из списка исходных
		final boolean attrIDExist = srcAttrCodes.contains(ID_ATTRIBUTE);	// определяем, есть ли ID_ATTRIBUTE в списке исходных

		/*
		 * sql для выполнения действий очитски и вставки для двух случаев ...
		 */
		String sqlClearDest = null, sqlInsert = null;

		if (logger.isDebugEnabled())
			logger.debug( "Summarizing "+ (attrIDExist ? "card ids" : "persons")+" ["+ srcAttrCodes +"] of tree from card "+ curNodeId +" linked by ["+IdUtils.makeIdCodesQuotedEnum(this.nodeLinkAttrIds) +"] ... ");

		if (attrIDExist)
		{
			// если указан атрибут "_ID" -> он обязан быть единственным
			if (srcAttrIds.size() != 1) {
				logger.error("config problem: attribute '"+ ID_ATTRIBUTE + "' must be alone in the arguments list");
				throw new DataException("jbr.card.configfail");
			}

			// получение списка id-карточек дерева, если надо ...
			final List<Long> srcCardIds = loadSrcCardIds(curNodeId, srcAttrCodes, sqlGetTree_1);
			final String sSrcCardIds = ObjectIdUtils.numericIdsToCommaDelimitedString(srcCardIds);

			if (!append) {
				sqlClearDest = 
					"DELETE FROM attribute_value avDest \n"+
					"WHERE \n" +
					"		avDest.card_id IN ( " + sDestCardIds_1 + 
					"		) \n"+
					// "		and av.attribute_code in ( \n" + sDestAttrCodes + ") \n"+
					"		AND ( \n "+ destTemplateFilter +
					"\n		) -- /AND \n" +

					( (sSrcCardIds == null || "".equals(sSrcCardIds) ) 
					? "" :
					"		-- no such links inside inserting values ... \n" +
					"		AND avDest.number_value NOT IN ( \n" + sSrcCardIds
					+ ") -- /IN \n" )
				;
			}

			/* запрос вставки обычных CardLink-ов */
			if (sSrcCardIds != null)
				sqlInsert = 
					"INSERT INTO attribute_value (card_id, attribute_code, number_value) \n\n" +
					"		SELECT DISTINCT cDest.card_id, ta.attribute_code, srcLink.card_id as number_value \n" +
					"		FROM card cDest JOIN template_attribute ta \n" +
					"					ON ta.template_id = cDest.template_id \n" +
					"					AND ( \n" +
											buildDestTemplateFilter("ta") +
					"					) -- /AND \n" +
					"				CROSS JOIN card srcLink \n" +
					"		WHERE \n" +
					"			/* destination cards ... */ \n" +
					"			cDest.card_id in ("+ sDestCardIds_1+ 
					"			) \n\n" +

					"			/* card links to be inserted ... */ \n "+
					"			AND srcLink.card_id in ( "+ sSrcCardIds+
					"\n			) -- /AND srcLink.card_id in (...) \n\n" +

					"			/* self-link safety ... */ \n"+
					"			AND (cDest.card_id <> srcLink.card_id) \n\n"+

					"			/* anyway - perform dest-duplicate check ... */ \n" +
					"			AND NOT EXISTS ( \n" +
					"					select 1 from attribute_value avE \n" +
					"					where avE.card_id = cDest.card_id \n" +
					"						and avE.attribute_code = ta.attribute_code \n" +
					"						and avE.number_value = srcLink.card_id \n" +
					"			) -- /AND NOT EXISTS \n" 
				;

		} else 
		{ // отработка персон (U-атрибуты и С-арибуты на персональные карточки )

			// получение списка персон из исх атрибутов дерева ...
			final List<Long> srcPersonIds = loadSrcPersons(curNodeId, srcAttrCodes, sqlGetTree_1);
			if (logger.isDebugEnabled())
				logger.debug( "Summarized person list of attributes ["+ srcAttrCodes 
					+"]\n\t of cards [" + sqlGetTree_1 
					+"] \n\t has size " + (srcPersonIds != null ? srcPersonIds.size() : "0") );
			final String sSrcPersons = (srcPersonIds != null && !srcPersonIds.isEmpty()) 
					? ObjectIdUtils.numericIdsToCommaDelimitedString(srcPersonIds) 
					: "";

			// CardUtils.dropAttributes(getJdbcTemplate(), new Object[]{dstAttrId}, allNodes);
			if (!append) {
				sqlClearDest = 
						"DELETE FROM attribute_value avDest \n"+
						"WHERE \n" +
						"		avDest.card_id IN ( " + sDestCardIds_1 + 
						"		) \n"+
						"		AND ( \n "+ destTemplateFilter +
						"\n		) -- /AND \n" +

						/* >>> (2011/07/21, RuSA) Это работает до 20 минут для больших деревьев, 
						 * что многовато, малость подработаем вид проверки и время сделаем ~20 сек ...
						 * (2011/08/08, RuSA) проверка вида "person_id in (select distinct person...)"
						 * // (2011/07/18, RuSA) Для уменьшения нагрузки - удаление только "устаревших" данных ...
						 * "		AND (avDest.card_id, avDest.attribute_code, avDest.number_value) NOT IN ( \n" +
						 * 			sqlGetAllPersons_1 +
						 * " 		) --/AND NOT IN ... \n"
						 */
						( ("".equals(sSrcPersons) || "NULL".equals(sSrcPersons) ) ? "" :
						"		-- такой персоны нет среди вновь добавляемых ... \n" +
						"		AND NOT EXISTS (\n" +
						"				SELECT 1 FROM " +
						"					-- соединение удаляемой записи с внут персоной ... \n" +
						"					attribute aDest JOIN person pDest \n" + 
						"						ON aDest.data_type in ('C', 'U') \n" +
						"						AND aDest.attribute_code = avDest.attribute_code \n" + 
						// в зависимости от типа  атрибута выбираем U-код непосредственно 
						// или находим его по личной карточке ...
						"						AND( \n" +
						"								(aDest.data_type = 'U' and pDest.person_id = avDest.number_value)\n" + 
						"								OR (aDest.data_type = 'C' and pDest.card_id = avDest.number_value) \n" +
						"						) \n" +
						"				-- Далее просто проверяем что эта персона есть в новом наборе ... \n" +
						"				WHERE pDest.person_id IN ( \n"+
						"					" + sSrcPersons +
						"\n			) -- /pDest.person_id IN \n" +
						"		) -- /AND NOT EXISTS \n"
						/* <<< (2011/07/21, RuSA), (2011/08/08, RuSA)
						 */
					);
			}

			/*
			 * запрос вставки  id-персон или id-персональных карточек для mixed (U,C)-атрибутов ...
			 */
			
			/*			
				// (YNikitin, 2011/04/10) Оптимизация запроса в рамках задачи 2889/2902
					"					and avE.number_value = ( \n"+
					"						CASE WHEN 'U' = aMta.data_type \n" +
					"							THEN mta.person_id  \n"+
					"							ELSE mta.person_card_id \n"+
					"						END \n" +
					"					) -- /CASE \n"+
			 */
			if ( (sSrcPersons != null) && !"".equals(sSrcPersons) )
				sqlInsert = 
					"INSERT INTO attribute_value (card_id, attribute_code, number_value) \n\n" +
					"	SELECT DISTINCT cDest.card_id, ta.attribute_code \n" +
					"			/* depends on destination attribute type get person_id or personal card_id ... */ \n"+
					"			, (CASE WHEN 'U' = aDst.data_type \n" +
					"				THEN pSrc.person_id \n" +
					"				ELSE pSrc.card_id \n" +
					"			END) as number_value \n" +
					"	FROM card cDest JOIN template_attribute ta \n" +
					"					ON ta.template_id = cDest.template_id \n" +
					"					AND ( \n" +
											buildDestTemplateFilter("ta") +
					"					) -- /AND \n" +
					"			JOIN attribute aDst \n" +
					"					on aDst.attribute_code = ta.attribute_code \n" +
					"					and aDst.data_type in ('U', 'C') \n" +
					"			, person pSrc \n " +
					"	WHERE \n" +
					"			/* destination cards ... */ \n" +
					"			cDest.card_id in ("+ sDestCardIds_1+ 
					"			) \n\n" +

					"			/* person ids to be inserted ... */ \n "+
					"			AND pSrc.person_id in ( "+ sSrcPersons+
					"\n			) -- /AND srcLink.card_id in (...) \n\n" +

					"			/* anyway - perform dest-duplicate check ... */ \n" +
					"			AND NOT EXISTS ( \n" +
					"					select 1 from attribute_value avE \n" +
					"					where avE.card_id = cDest.card_id \n" +
					"						and avE.attribute_code = ta.attribute_code \n" +
					"						and ( \n" +
					"							( ('U' = aDst.data_type) and ( avE.number_value = pSrc.person_id) ) \n" +
					"							OR ( ('C' = aDst.data_type) and ( avE.number_value = pSrc.card_id) ) \n" +
					"						) -- and \n" +
					"			) -- /AND NOT EXISTS \n" 
				;
		} // else if (attrIDExist)

		/**
		 * (2) очистка целевых данных ...
		 */
		// CardUtils.dropAttributes(getJdbcTemplate(), new Object[]{dstAttrId}, allNodes);
		if (!append && sqlClearDest != null) {
			execDelete( curNodeId, sqlClearDest);
		}

		/**
		 * (3) Добавление новых данных ...
		 */
		// здесь INSERT уже проверкой уникальности вставляемых значений ...
		final boolean nothingToInsert = (sqlInsert == null) || (sqlInsert.length() == 0);
		if ( nothingToInsert) {
			logger.warn( "No persons to insert into the doc-tree containing card "+ curNodeId.getId() );
		} else 
		{
			final NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(getJdbcTemplate());
			final int countIns = jdbc.update( sqlInsert,
					new MapSqlParameterSource().addValue( "card_id", curNodeId.getId(), Types.NUMERIC)
			);

				// TODO:	markChangeDate(...);	// пометка времени изменения для целевых карточек ...

			if (logger.isDebugEnabled()) {
				logger.debug( "Insert "+ countIns 
						+ " attributes into the doc-tree containing card "+ curNodeId.getId()
						+ " by SQL: \n"+ sqlInsert+ ";\n");
			} else {
				logger.info( "Insert "+ countIns+ " attributes into the doc-tree containing card "+ curNodeId.getId());
			}
		}
		logTime("work time is ", start_ms);
		return null;
	}


	/**
	 * Сформировать select-запрос для получения id из дерева карточек.
	 * sNodeLinkCodes список атрибутов связи
	 * @return строка с sql-select запросом.
	 */
	private String buildSelectGetTreeCards() 
	{
		// атрибуты, образующие дерево ...
		final String sNodeLinkCodes = IdUtils.makeIdCodesQuotedEnum(this.nodeLinkAttrIds);

		final String rootGetter = (this.rootIsCurrent) ? "(:card_id)" : "get_root_card(:card_id)";
		final boolean hasEnabledStates = (this.nodeCardsStateIds != null) && !this.nodeCardsStateIds.isEmpty();
		final boolean hasNotEnabledStates = (this.nodeCardsNotStateIds != null) && !this.nodeCardsNotStateIds.isEmpty();

		// если выполняется получение атрибутов из текущей карточки (фактически копирование) ...
		final boolean treeIsCurrent =
			this.rootIsCurrent && (this.nodeLinkAttrIds == null || this.nodeLinkAttrIds.isEmpty());

		// условие проверки статуса ...
		final String stateCond = 
			( (!hasEnabledStates && !hasNotEnabledStates)
					? ""
					: "						JOIN card cc on cc.card_id = t.card_id \n"
					+ ( hasEnabledStates 
							? "							AND cc.status_id in ("+ ObjectIdUtils.numericIdsToCommaDelimitedString(this.nodeCardsStateIds) + ") \n"
							: "" )
					+ ( hasNotEnabledStates 
							? "							AND NOT cc.status_id in ("+ ObjectIdUtils.numericIdsToCommaDelimitedString(this.nodeCardsNotStateIds) + ") \n"
							: "" )
			);

		/* запрос получения card_id узлов дерева ...
		 * параметр (1) :card_id
		 */
		final String sqlGetTree_1 =
			(treeIsCurrent) 
			?	"					SELECT t.card_id \n" +
				"					FROM card t \n "+
				stateCond +
				" 					WHERE t.card_id = :card_id \n"
			:
				"					SELECT t.card_id \n"+
				"					FROM connectby( 'card', 'card_id', 'parent_card_id', CAST ("+ rootGetter + " AS character varying), 0) \n" +
				"							as t(card_id numeric, parent_card_id numeric, level int) \n" +
				"						-- (!) LEFT-JOIN нужен т.к. в карточке может не быть атрибутов, но дереве она должна быть - ибо записть в неё будет ... \n" +
				"						LEFT JOIN attribute_value parentNode \n"+
				// TODO: по-идее, второе условие (t.card_id=pNode.num_va) излишнее
				"							ON (t.parent_card_id = parentNode.card_id AND t.card_id = parentNode.number_value) \n" +

				// учёт сконфигурированных статусов карточек ...
				//
				stateCond +
				"					WHERE ( \n" +
				"							parentNode.attribute_code IN ("+ sNodeLinkCodes+ ") \n" +
				"							OR (t.parent_card_id IS NULL) \n" +
				"					) -- /WHERE \n"
			;
			return sqlGetTree_1;
	}


	/** 
	 * Построить фильтр по целевым шаблонам вида:
	 * 			( avDest.attribute_code in (...) and avDest.template_id = T1)
	 * 			or ( avDest.attribute_code in (...) and avDest.template_id = T2)
	 */
	private StringBuffer buildDestTemplateFilter(final String avAlias) 
	{
		final StringBuffer destTemplateFilter = new StringBuffer();
		int i = 0;
		for (Iterator<Destination> iter = destinations.values().iterator(); iter.hasNext(); ) 
		{
			final Destination dstItem = iter.next();
			if (i++ > 0)
				destTemplateFilter.append("		OR \n");
			destTemplateFilter.append("		( \n");
			destTemplateFilter.append( "			").append(avAlias).append(".attribute_code IN ( ")
					.append( IdUtils.makeIdCodesQuotedEnum(dstItem.getAttrIds()))
					.append(" ) --/IN \n");
			if (dstItem.getTemplateId() != null)
				destTemplateFilter.append("			AND ").append(avAlias).append(".template_id=")
						.append(dstItem.getTemplateId().getId())
						.append(" \n");
			destTemplateFilter.append("		) \n");
		}
		return destTemplateFilter;
	}


	/**
	 * Получить всех персон по списку исходных атрибутов из дерева карточек.
	 * @param curNodeId
	 * @param srcAttrCodes
	 * @param sqlSelectGetTree_1
	 * @return
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	private List<Long> loadSrcPersons(final ObjectId curNodeId,
			String srcAttrCodes, final String sqlSelectGetTree_1)
			throws DataAccessException 
	{
		if ( (curNodeId == null) || curNodeId.getId() == null 
			|| (srcAttrCodes == null) || srcAttrCodes.trim().length() == 0
			|| sqlSelectGetTree_1 == null
			)
			return null;

		final long ms_start = System.currentTimeMillis();

		final String sqlPersons =
				"SELECT DISTINCT pdr.person_id \n"+
				"FROM attribute_value avdr \n"+
				"			JOIN attribute a \n" +
				"					ON  a.data_type in ('C', 'U') \n" +
				"					AND a.attribute_code = avdr.attribute_code \n" +
				// в зависимости от типа  атрибута выбираем U-код непосредственно 
				// или находим его по личной карточке ...
				"			JOIN person pdr \n" +
				"					ON (a.data_type = 'U' and pdr.person_id = avdr.number_value) \n" +
				"					OR (a.data_type = 'C' and pdr.card_id = avdr.number_value) \n" +
				"WHERE avdr.attribute_code in ("+ srcAttrCodes +") \n"+
				"		AND avdr.card_id IN ( \n" + sqlSelectGetTree_1 + "\n" + 
				"		) -- /AND avdr.card_id IN(...) \n "
			;
		final NamedParameterJdbcTemplate jdbcSelPerons = new NamedParameterJdbcTemplate(getJdbcTemplate());
		final List<Long> srcPersonIds = jdbcSelPerons.queryForList( sqlPersons, 
				new MapSqlParameterSource().addValue( "card_id", curNodeId.getId(), Types.NUMERIC), 
				Long.class);

		logTime( "\t find person records "+ (srcPersonIds == null ? "null" : srcPersonIds.size()), ms_start);

		return srcPersonIds;
	}


	/**
	 * Получить список всех id карточек дерева.
	 * @throws DataAccessException
	 * @param curNodeId
	 * @param srcAttrCodes
	 * @param sqlSelectGetTree_1
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Long> loadSrcCardIds(ObjectId curNodeId, String srcAttrCodes,
			String sqlSelectGetTree_1) 
	{
		if ( (curNodeId == null) || curNodeId.getId() == null 
				|| (srcAttrCodes == null) || srcAttrCodes.trim().length() == 0
				|| sqlSelectGetTree_1 == null
				)
				return null;

		final long ms_start = System.currentTimeMillis();

		final String sql = 
				"SELECT DISTINCT ccDR.card_id FROM card ccDR \n" +
				"WHERE ccDR.card_id IN ( \n" + 
									sqlSelectGetTree_1 +
				"\n			) -- /WHERE IN(...) \n "
				;
		final NamedParameterJdbcTemplate jdbcSelPerons = new NamedParameterJdbcTemplate(getJdbcTemplate());
		final List<Long> srcCardIds = jdbcSelPerons.queryForList( sql, 
				new MapSqlParameterSource().addValue( "card_id", curNodeId.getId(), Types.NUMERIC), 
				Long.class);
		logTime( "\t find _ID cards records "+ (srcCardIds == null ? "null" : srcCardIds.size()), ms_start);

		return srcCardIds;

	}


	/**
	 * Выполнить sql-запрос.
	 * @param sqlClearDest
	 * @return кол-во удалённых записей
	 */
	private int execDelete( ObjectId curNodeId, String sqlClearDest) {
		final long delStart_ms = System.currentTimeMillis();
		// TODO:	markChangeDate(...);	// пометка времени изменения
		final NamedParameterJdbcTemplate jdbcDel = new NamedParameterJdbcTemplate(getJdbcTemplate());
		final int countDel = jdbcDel.update( sqlClearDest, 
				new MapSqlParameterSource().addValue( "card_id", curNodeId.getId(), Types.NUMERIC) 
			);
		final long msDuration = System.currentTimeMillis() - delStart_ms;
		if (logger.isDebugEnabled()) {
			logger.debug( "Dropped "+ countDel
					+" older attributes in "+ msDuration+
					" msec of the doc-tree containing card " + curNodeId.getId()
					+" by SQL: \n"+ sqlClearDest + ";");
		} else {
			logger.info( "Dropped "+ countDel 
					+" older attributes in "+ msDuration
					+" msec of the doc-tree containing card " + curNodeId.getId());
		}
		logTime( "\t delete records "+ countDel, delStart_ms);
		return countDel;
	}


	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.processors.ProcessCard#setParameter(java.lang.String, java.lang.String)
	 */
	@Override
	public void setParameter(String name, String value) 
	{
		if (name == null || "".equals(name)) return;

		if ("srcAttrIds".equalsIgnoreCase(name)) {
			this.srcAttrIds = parseAttrIds(value);
		} else if ("nodeLinkAttrIds".equalsIgnoreCase(name)) {
			this.nodeLinkAttrIds = parseAttrIds(value);
		}
		else if ("copyToDestinations".equalsIgnoreCase(name)) {
			this.destinations = parseDestinations(value);
		} else if ("writeOperation".equalsIgnoreCase(name)) {
			this.append = "append".equalsIgnoreCase(value.trim());
		} else if ("destIsCurrent".equalsIgnoreCase(name)) {
			this.destIsCurrent = Boolean.parseBoolean(value.trim());
		} else if ("nodeCardsStateIds".equalsIgnoreCase(name)) {
			this.nodeCardsStateIds = IdUtils.makeStateIdsList(value);
		} else if ("nodeCardsNotStateIds".equalsIgnoreCase(name)) {
			this.nodeCardsNotStateIds = IdUtils.makeStateIdsList(value);
		} else if ("rootIsCurrent".equalsIgnoreCase(name)) {
			this.rootIsCurrent = Boolean.parseBoolean(value.trim());
		} else if ("destAttrId".equalsIgnoreCase(name)) {
			regDestAttr( this.destinations, null, IdUtils.smartMakeAttrId(value, PersonAttribute.class));
		} else {
			super.setParameter(name, value);
			// logger.warn( "May be unknown parameter: '"+ name + "'='"+ value + "' -> ignored");
		}
	}

	/**
	 * зарегистрировать список целевых атрибут для указанного шаблона 
	 * @param templateId: целевой шаблон, если null - то любой.
	 * @param attrIds 
	 */
	private static Destination regDestAttrs( Map<ObjectId, Destination> destinations, 
			ObjectId templateId, Collection<ObjectId> attrIds) 
	{
		Destination result = destinations.get( templateId);
		if (attrIds != null) {
			if (result == null) {
				result = new Destination(templateId, attrIds);
				destinations.put(templateId, result);
			} else {
				result.getAttrIds().addAll( attrIds);
			} 
		}
		return result;
	}

	/**
	 * зарегистрировать целевой атрибут для указанного шаблона 
	 * @param templateId: целевой шаблон, если null - то любой.
	 * @param attrId 
	 */
	private static Destination regDestAttr( Map<ObjectId, Destination> destinations, 
			 ObjectId templateId, ObjectId attrId) 
	{
		final Collection<ObjectId> col = new ArrayList<ObjectId>(1);
		col.add(attrId);
		return regDestAttrs( destinations, templateId, col);
	}

	private static Set<ObjectId> parseAttrIds(String value)
	{
		final List<ObjectId> list = IdUtils.stringToAttrIds(value, PersonAttribute.class); //  CardLinkAttribute.class 
		return (list == null || list.isEmpty()) 
					? null 
					: new HashSet<ObjectId>(list);
	}

	/**
	 * Разбор строки со списком шаблонов и атрибутов в этом шаблоне. 
	 * @param destinationsString имеет вид списка с разделителем точка с запятой,
	 * в котором каждый элемент имеет вид
	 * 		"{шаблон:}атрибут1 {, атрибут2 {, атрибут3 ... }}"
	 *  отсутствие шаблона или символ "*" в качестве шаблона означают "любой шаблон".
	 * пример "*:jbr.resolution.dynamic_role.hidden"  => "атрибут jbr.resolution.dynamic_role.hidden в любом шаблоне"
	 * или более сложный: 
	 * 				"*:jbr.resolution.dynamic_role.hidden;
	 * 				jbr.report.internal:jbr.report.dynamic_role.hidden;
	 * 				jbr.report.external:jbr.report.dynamic_role.hidden;
	 * 				jbr.adnotamCommission:jbr.adnotamCommission.dynamic_role.hidden"
	 * с заданием атрибутов как для всех шаблонов (первый элемент в списке).
	 * так и для конкретных шаблонов (остальные).
	 * .
	 * @return формированую карту "шаблон-описание атрибутов для шаблона"
	 * 
	 */
	private Map<ObjectId, Destination> parseDestinations(final String destinationsString)
	{
		final Map<ObjectId, Destination> destByTemplates = new HashMap<ObjectId, Destination>();
		final String[] dests = destinationsString.split("\\s*[;]\\s*");
		for (String dest : dests) {
			final String[] destComps = dest.split(":");
			if (destComps.length < 1) {
				logger.warn("Invalid format '" + dest+ "' -> skipped ");
				continue;
			}
			String sTemplate = null, sAttribute = null;
			boolean isTemplateAny = false;
			if (destComps.length == 1) {
				// принимаем что если нет двоеточия - то указан только атрибут (в произвольном шаблоне)
				// sTemplate = '*';
				isTemplateAny = true;
				sAttribute = destComps[0].trim();
			} else {
				sTemplate = destComps[0].trim();
				sAttribute = destComps[1].trim();
				isTemplateAny = (sTemplate.length() == 0) || sTemplate.startsWith("*");
			}

			// если шаблон не задан, или он пустой или "*" -> пригодно для любого шаблона
			final ObjectId templateId = (isTemplateAny) 
						? null
						: IdUtils.tryFindPredefinedObjectId(sTemplate, Template.class);
			if ( !isTemplateAny && templateId == null)
			{
				logger.warn( "Unknown template skipped '" + sTemplate+ "' from arg '"+ dest + "'");
				continue;
			}
			if (sAttribute.length() < 1) {
				logger.warn("No attribute configured for template inside '" + dest+ "' -> skipped");
				continue;
			}

			final List<ObjectId> attrIdsList = 
						IdUtils.stringToAttrIds(sAttribute, PersonAttribute.class);
			// DONE: если есть уже Dest с таким шаблоном -> надо просто добавить в список атрибут
			regDestAttrs(destByTemplates, templateId, attrIdsList);
		}
		return destByTemplates;
	}

	/**
	 * Вспомогательный класс для хранения шаблонов и атрибутов, связанных с шаблоном.
	 * Шаблон NULL означает что атрибуты применимы для любого шаблона. 
	 * @param destinationsString
	 * @return
	 */
	static class Destination{
		protected ObjectId templateId;
		protected Set<ObjectId> attrIds;

		Destination(ObjectId templateId, Collection<ObjectId> attrIds) {
			this.templateId = templateId;
			this.attrIds = new HashSet<ObjectId>( attrIds);
		}

		public ObjectId getTemplateId() {
			return templateId;
		}
		public Set<ObjectId> getAttrIds() {
			return attrIds;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			final StringBuffer buf = new StringBuffer();
			if (attrIds == null || attrIds.isEmpty()) {
				buf.append("empty");
			} else {
				for (ObjectId id: attrIds) {
					buf.append(id).append(";");
				}
			}
			return MessageFormat.format("(template={0}, attrIds=[{1}] )", 
					(templateId == null ? "*" : templateId.toString()),
					buf.toString());
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((this.attrIds == null) ? 0 : this.attrIds.hashCode());
			result = prime
					* result
					+ ((this.templateId == null) ? 0 : this.templateId
							.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			
			if (getClass() != obj.getClass())
				return false;

			final Destination other = (Destination) obj;

			if (this.templateId == null) {
				if (other.templateId != null)
					return false;
			} else if (!this.templateId.equals(other.templateId))
				return false;

			if (this.attrIds == null) {
				if (other.attrIds != null)
					return false;
			} else if (!this.attrIds.equals(other.attrIds))
				return false;

			return true;
		}

	}
}