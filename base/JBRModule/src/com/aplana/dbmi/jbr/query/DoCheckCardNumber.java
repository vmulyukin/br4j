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
package com.aplana.dbmi.jbr.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.aplana.dbmi.jbr.action.CheckCardNumber;
import com.aplana.dbmi.jbr.action.CheckCardNumber.CheckCardNumberResult;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;

public class DoCheckCardNumber extends ActionQueryBase implements Parametrized {
	/**
	 * ��� ��������� �� query.xml � ����� � ����� ��� ����������� ��������
	 * ���������� ��������.
	 */
	private static final String PARAM_CONFIG = "config"; 
	
	/**
	 * ������ ����������� ��������
	 */
	final private static String TEMPLATES_LIST = "224";
	
	/**
	 * ������ ���������, ������� �� �������� ��������.
	 * �.�. �������� � ����� �� ����� ��������� ����� ����� ���������� ��������
	 * ����������� ��������� (�.�. ������ �� ������������ ���� �����).
	 */
	final private static String STATES_UNCHEKCED_LIST  = "1,301,302,303990"; // 1=��������, 301=������ �� ����, 302="�������� � �����������", 303990=�������
	
	/**
	 * ������ ���������, ������� ���� �������� ��� ���������� ��������.
	 *    "��������������� �����";"JBR_REGD_REGNUM"
	 *    "���� �����������";"JBR_REGD_DATEREG"
	 *    "������� ����������";"JBR_INFD_SHORTDESC"
	 *    "�����������"("����������");"JBR_INFD_SENDER"
	 */
	final static String[] PROBLEM_CARD_INFO_ATTRLIST = { 
		"'JBR_REGD_REGNUM'",		"'JBR_REGD_DATEREG'",
		"'JBR_INFD_SHORTDESC'", 	"'JBR_INFD_SENDER'"
	};

	/**
	 * ��������� ��� ��������� ���������� ��������.
	 */
	final public static CheckCardNumberResult RESULT_CARDS_ABSENT = null; 

	// private String config;

	@Override
	public Object processQuery() throws DataException {

		final CheckCardNumber action = (CheckCardNumber)getAction();

		final String number = action.getNumber();
		if (!action.isNumberCanBeNullOrEmpty() 
				&& (number == null || number.trim().length() == 0)) {
			throw new DataException("jbr.card.check.incoming.nonumber");
		}

		final Date date = action.getDate();
		if ( !action.isDateCanBeNull() && date == null)
			throw new DataException("jbr.card.check.incoming.nodate");

		final ObjectId organization = action.getOrganization();
		if ( !action.isOrganizationCanBeNull() && organization == null)
			throw new DataException("jbr.card.check.incoming.noorganization");

		final ObjectId zoneDOW = action.getZoneDOW();
		if ( !action.isZoneDOWCanBeNull() && zoneDOW == null)
			throw new DataException("Incomin card without zoneDOW");
		
		final ObjectId cardObjId = action.getCardId();
		final Long cardId = (cardObjId == null) ? null : (Long)cardObjId.getId();

		// �������� Id-�� ���������� �������� ...
		
		// �������� ������������ ������ ��� ������������� ���� 3-� �����
		// ToDo: ���������� �� ������ ��������� (� �������, ���� �����������)
		if (	date != null 
				&& organization != null 
				&& !(number == null || number.trim().length() == 0)
			)
		{
			final List<Long> /* of id card */ problemCardIds = 
				// this.getSameNumberAndDateCardsIds( attrnum.getValue(), attrdate.getValue() );
				this.getSameNumberAndDateCardsIds(cardId, number, date, organization,zoneDOW);
	
//			return (problemCardIds == null || problemCardIds.isEmpty())
//							? RESULT_CARDS_ABSENT
//							: makeProblemsCardInfo(problemCardIds, "-1");
			return makeResult(problemCardIds);
		} 
		return RESULT_CARDS_ABSENT;
	}

	/**
	 * @param problemCardIds
	 * @return
	 */
	private CheckCardNumberResult makeResult(List<Long> problemCardIds) {
		if (problemCardIds == null || problemCardIds.isEmpty())
				return null;
		final List<ObjectId> list = new ArrayList<ObjectId>(problemCardIds.size());
		for (Long id : problemCardIds) {
			list.add( new ObjectId( Card.class, id));
		}
		return new CheckCardNumberResult(list);
	}
	
	/**
	 * @param cardId: id ����������� ��������
	 * @param date
	 * @param number
	 * @param organization: ����������� (�����������) ����������� null;
	 * @param zoneDOW: ���� ��� ����������� null;
	 * @return ������ ��������� ���������� ��������
	 */
	@SuppressWarnings({ "unchecked", "cast" })
	private List<Long> getSameNumberAndDateCardsIds(Long cardId, String number, 
			Date date, ObjectId orgId) {
		return this.getSameNumberAndDateCardsIds(cardId, number, date, orgId, null);
	}

	/**
	 * @param cardId: id ����������� ��������
	 * @param date
	 * @param number
	 * @param organization: ����������� (�����������) ����������� null;
	 * @return ������ ��������� ���������� ��������
	 */
	// (String number, Date date)
	@SuppressWarnings({ "unchecked", "cast" })
	private List<Long> getSameNumberAndDateCardsIds(Long cardId, String number, 
			Date date, ObjectId orgId, ObjectId zoneDOW) 
			// throws DataException 
	{
		final boolean useOrganization = (orgId != null);
		final boolean useDocNumber = (number != null);
		final boolean useDocDate = (date != null);
		final boolean useZoneDOW = (zoneDOW != null);

		// ���������...
		final MapSqlParameterSource args = new MapSqlParameterSource();
		args.addValue("cardId", cardId, Types.NUMERIC);

		if (orgId != null)
			args.addValue("orgId", (Long) orgId.getId(), Types.NUMERIC); 
		if (useDocNumber)
			args.addValue("docNum", number, Types.VARCHAR);
		// (YNikitin, 2011/03/17) ������� �������������� �������� �� ������� ����� SQL (������� �� �������� � ����)
		/*if (useDocDate){
			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			calendar.setTime(date);
			args.addValue("docDate", calendar.getTime(), Types.DATE);
		}*/
		if (useZoneDOW){
			args.addValue("zoneDOW",  (Long) zoneDOW.getId(), Types.NUMERIC);
		}

		// ��� SQL ������...
		final String sql = MessageFormat.format(
			"select c.card_id, av2.date_value\n" +
			"from  card c\n" + 

			// -- ����� ���������
			( (useDocNumber)
					? "\t  join attribute_value av1 on (av1.card_id=c.card_id and av1.attribute_code=''JBR_REGD_NUMOUT'' \n"
							+"\t\t\t  and upper( av1.string_value ) = upper( :docNum ) ) \n" 
					: ""
			) + 

			// (YNikitin, 2011/03/17) ������� �������������� �������� �� ������� ����� SQL (������ ��������� ��� ����)
			// -- ����
/*			( (useDocDate)
					? "\t  left join attribute_value av2 on " 
						+ "\t\t ( av2.card_id = c.card_id and av2.attribute_code = ''JBR_REGD_DATEOUT'' \n"
						+ "\t\t\t and date_trunc(''day'', av2.date_value) = :docDate ) \n"
					: ""
			)+*/
			// 
					"\t  left join attribute_value av2 on " 
					+ "\t\t ( av2.card_id = c.card_id and av2.attribute_code = ''JBR_REGD_DATEOUT'') \n"
					+

			// -- �����������
			( (useOrganization)
					? "\t  join attribute_value avSender on (avSender.card_id=c.card_id and avSender.attribute_code=''JBR_INFD_SENDER'' \n" + 
							"\t\t\t  and avSender.number_value=:orgId ) \n"
					: ""
			)+
			//���� ���
			( (useZoneDOW)
					? "\t  join attribute_value avDOW on (avDOW.card_id=c.card_id and avDOW.attribute_code=''JBR_ZONE_DOW'' \n" + 
							"\t\t\t  and avDOW.number_value=:zoneDOW ) \n"
					: ""
			)+

			"where\n" + 
			// -- (:UncheckedStates) -- ������������� ���������
			"\t\t c.status_id not in ({0})\n" + 

			// -- (:CheckedTemplates) -- ����������� �������
			"\t\t  and c.template_id in ({1})\n" + 

			"\t\t  and c.card_id <> coalesce(:cardId, -1)" 
			, STATES_UNCHEKCED_LIST, TEMPLATES_LIST
			);

		final ObjectId attrId= IdUtils.smartMakeAttrId("JBR_REGD_DATEOUT", DateAttribute.class);

		final NamedParameterJdbcTemplate jdbcQ = new NamedParameterJdbcTemplate(getJdbcTemplate());
		final List<Card> result = jdbcQ.query( sql, args, new RowMapper(){
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				final Card card = new Card();
				card.setId(rs.getInt(1));

				final DateAttribute attr = new DateAttribute();
				attr.setId(attrId);
				attr.setValueWithTZ(rs.getTimestamp(2));

				card.setAttributes(new ArrayList());
				card.getAttributes().add(attr);
				return card;
			}
		});

		if (result == null || result.isEmpty()){ 
			return null;
		}

		// ����� ��������� �� �������� � ����� ����������� ��������� ������ id-�����, ��������������� ������� �������� �� ����  
		final List<Long> answer = new ArrayList();
		final Calendar cBase = getDateTruncByDay(date);
		for(int i=0; i < result.size(); i++) {
			final Card c = result.get(i);
			boolean hasProblem = true;
			if (useDocDate) {
				// �������� ���������� �� ���� ...
				final Date cardDate = ((DateAttribute)c.getAttributeById(attrId)).getValue();
				// � ���� �� ���� � ������� ���� ������� ����� �����
				final Calendar cCard = getDateTruncByDay(cardDate);
				hasProblem = (cardDate != null && cBase.compareTo(cCard) == 0);
			}
			if(hasProblem)
				answer.add( (Long) c.getId().getId() );
		}
		return answer;
	}

	static final Calendar getDateTruncByDay(Date dat) {
		final Calendar result = Calendar.getInstance();
		result.setTime(dat);
		result.set(Calendar.HOUR, 0);
		result.set(Calendar.HOUR_OF_DAY, 0);
		result.set(Calendar.MINUTE, 0);
		result.set(Calendar.SECOND, 0);
		result.set(Calendar.MILLISECOND, 0);
		return result;
	}

	/**
	 * �������� ���� � ���������� ���������.
	 * @return
	 */
//	private String makeProblemsCardInfo( List<Long> list, String noId)
//		throws DataException
//	{
//		if (list == null) return noId;
//
//		// ��������� ������...
//		final Search search = makeSearchObj( getIntsAsCommaList(list));
//
//		// ���������� ������� ...
//		final ActionQueryBase action = super.getQueryFactory().getActionQuery(search);
//		action.setAction(search);
//
//		final SearchResult cards 
//			= (SearchResult) getDatabase().executeQuery( getUser(), action);
//		
//		return getCardsInfoCommaList(cards);
//	}
	
//	/**
//	 * @param cards ������ ��������
//	 * @return ������ ��������� �������� � ���� ������
//	 */
//	@SuppressWarnings("unchecked")
//	private String getCardsInfoCommaList(SearchResult cards) {
//
//		// TODO: ����� ����� �������� ������ ������, ������������� ���, � ���� ��������� ����� ���...
//		
//		if (cards == null || cards.getCards() == null) 
//			return "NULL";
//		
//		final StringBuffer result = new StringBuffer();
//		result.append( "\n <br/> \n <ol style=\"font-size: 1em; margin: 3mm; margin-left:10mm;\">" );
//		for (Iterator iterator = cards.getCards().iterator(); iterator.hasNext();) {
//			final Card card = (Card) iterator.next(); 
//			result	.append( "\n <li> ")
//					.append( StringEscapeUtils.escapeHtml( getCardInfo(card) ))
//					.append( " </li>");
//		}
//		result.append("\n </ol> \n");
//		return result.toString();
//	}

//	/**
//	 * @param card
//	 * @return ���������� �� ���� ��������� � ��������
//	 */
//	@SuppressWarnings("unchecked")
//	private String getCardInfo(Card card) {
//
//		if (card == null || card.getAttributes() == null) 
//			return "";
//		
//		final StringBuffer result = new StringBuffer();
//		for (Iterator iterator = card.getAttributes().iterator(); iterator.hasNext();) {
//			final Attribute attr = (Attribute) iterator.next();
//			result.append(attr.getStringValue());
//			if (iterator.hasNext()) result.append(", ");
//		}
//		return result.toString();
//	}

// static private ObjectId[] attrIds;
//	private static ObjectId[] getAttributesIds()
//	{
//		if (attrIds == null) {
//			attrIds = new ObjectId[PROBLEM_CARD_INFO_ATTRLIST.length];
//			for (int i = 0; i < PROBLEM_CARD_INFO_ATTRLIST.length; i++)
//				attrIds[i] = new ObjectId( Attribute.class, PROBLEM_CARD_INFO_ATTRLIST[i]);
//		}
//		return attrIds;
//	}

//	/**
//	 * �������� ������ �� ������ ��������.
//	 * @param idsStr ������ ��������� ��������
//	 * @return ������ ��� ������.
//	 * @throws DataException
//	 */
//	private Search makeSearchObj( final String idsStr ) throws DataException
//	{
//		Search srchResult = null;
//
//		// ��������� ����� ����� � �������� �� ������������...
//		if (config == null || "".equals(config)) {
//			// �� ������ ���� - defaultSearch
//			// �������� ������� ��������� ������ ...
//			// srchResult.setByCode(true);
//			// srchResult.setByAttributes(false);
//			srchResult = CardUtils.getFetchAction( idsStr, getAttributesIds());
//		} else	{
//			// �������� ��������� ������ �� �����...
//			try {
//				srchResult = new Search();
//
//				// �������� �� XML-�����
//				srchResult.initFromXml( Portal.getFactory().getConfigService().loadConfigFile(config));
//
//				// ������� ������ Id's ��������...
//				srchResult.setWords( idsStr );
//				
//			} catch (IOException ex) {
//				ex.printStackTrace();
//				throw new DataException("jbr.card.configfail", ex);
//			}
//		}
//
//		
//		// ���������� �������� �������...
//		
//		return srchResult;
//	}

//	final static String getIntsAsCommaList(List<Long> list)
//	{
//		final StringBuffer idsBuf = new StringBuffer();
//		if (list == null) return null;
//		
//		for (int i = 0; i < list.size(); ++i) {
//			idsBuf.append(String.valueOf(list.get(i)));
//			if (i < list.size() - 1) idsBuf.append(","); 
//		}
//		return idsBuf.toString();
//	}

	public void setParameter(String name, String value) {
		if (PARAM_CONFIG.equals(name)) {
			// this.config = value;
			logger.warn( "Deprecated parameter '"+ name+ "' ignored");
		} else {
			throw new IllegalArgumentException("Unknown parameter: '" + name + "'");
		}
	}
}
