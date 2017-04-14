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
package com.aplana.dbmi.service.impl.access;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.aplana.dbmi.access.delegate.DelegatorBean;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardAccess;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.impl.query.AttributeTypes;
import com.aplana.dbmi.utils.StrUtils;

/**
 * ����� ������ ��� ������ � ��������� ������� � ������
 * @author RAbdullin
 */
public class CardAccessUtils { 

	/**
	 * �������� sql-��������� ��� ��������� ������ ����� ������������ 
	 * ��� ������� � ������� (��������� sql-����������, ����� ����� ���� 
	 * ������������ ���������� ������ ��� ����� ����� ������).
	 * 
	 * @param inlinePersonIds: ������-��������� ��� id-����������� ������,
	 * �������� "(:personId)", "?" ��� "143, 123, 10".
	 * 
	 * @param inlineOutList: �������� ������ sql-����� ��� ������ pr=person_role
	 * � prt=person_role_template, 
	 * ��������, "1" ��� "pr.role_code";
	 * 
	 * @param inlineTemplate: sql=��������� ��� ��������� � id �������, 
	 * 		��������, "c.template_id" ��� "ca.template_id";;
	 * 
	 * @param inlineCustomCond: ��� ������� ��� �������� (and, not, end. etc...)
	 * 	 ��������, ""(�����) ��� "and (pr.role_code = ca.role_code)";
	 * 
	 * @return sql-��������� ��� ��������� ������ ����� �� outListInline � 
	 * ������ ������������ ��� ������� � ������� templateInline.
	 * ������������ ������ ����:
	 * 
	 *  
	 */
	public static String buildSqlSelectRoles( 
			String inlinePersonIds,
			String inlineOutList, 
			String inlineTemplate, 
			String inlineCustomCond)
	{
		final String sql = "";

// � ������ ������ BR4J00036917 ������� ����������� ����-������
			
//				"SELECT " + inlineOutList + "\n" + 
//				"FROM person_role AS pr \n" +
//				"\t LEFT OUTER JOIN person_role_template AS prt \n" +
//
//				"\t\t  ON pr.prole_id = prt.prole_id \n" + 
//				"WHERE \n" +
//				"\t  pr.person_id in ("+ inlinePersonIds + ") \n" + // (:personId)
//
//				"\t  and coalesce(prt.template_id, "+ inlineTemplate+ " ) = " + inlineTemplate+ " \n"+
//				( (inlineCustomCond == null) ? "" : inlineCustomCond )
//				+ "\n"
//				;
		return sql;
	}

	/*
	 * Get all roles of the user for the card (directly assigned, inherited, 
	 * and delegated).
	 * (!) All roles return in upper case.
	 */
	public static Set /*of <ObjectId>*/ getPersonRoles4Card( ObjectId personId, 
			ObjectId cardId, JdbcTemplate jdbc)
	{
		return getAdvPersonRoles4Card( personId, cardId, jdbc);
	}


	/**
	 * Get all roles of the user for the card (directly assigned and inherited) 
	 * (!) All roles return in upper case.
	 * @param personId
	 * @param cardId
	 * @param jdbc
	 * @return Set of {@link SystemRole} identifiers
	 */
	public static Set /*of <ObjectId>*/ getAdvPersonRoles4Card( ObjectId personId, 
			ObjectId cardId, JdbcTemplate jdbc)
	{
		/**
		 * �������� ������ ��������� ����� ��� ��������� �������� ������������.
		 * (!) ���������:
		 * 	 ��� ����������� sql-��������� (�����): cardId � personId;
		 * 	 ��� �����������: ( personId, cardId)
		 */
		final StringBuffer bufQuery_arg2 = new StringBuffer(
				"SELECT pr.role_code \n" +
				"FROM 	person_role pr \n" +
				"WHERE pr.person_id = ? \n");

		final Set regions = getCardRegions(cardId, jdbc);
		if (regions != null)
		{	// ���� ������ �������� - �������� ����� ������ ������ ��� ...

			//  "{0}"	java-�������� ��� �������� ������ ��������, 
			// 			�������� "(1=1)" ��� "( prr.value_id in (...) )";

// � ������ ������ BR4J00036917 ������� ����������� ����-������, ����-������
			
//			final String sCondition
//				= MessageFormat.format( 
//						"	AND ( \n" + 
//						" 		NOT EXISTS( \n" +
//						"				SELECT 1 FROM person_role_region prr \n" +
//						"				WHERE prr.prole_id = pr.prole_id \n" +
//						"		)\n" + 
//						" 		OR (EXISTS ( \n" +
//						"				SELECT 1 from person_role_region prr \n" +
//						"				WHERE	prr.prole_id = pr.prole_id \n" +
//						"					AND	prr.value_id in ({0}) \n" +
//						"			)) \n" + 
//						" 	)\n", 
//					new Object[] { StrUtils.getAsString( regions, ", ", "'") } 
//				);
//			bufQuery_arg2.append( sCondition);
		}
		// final Object[] args = new Object[] { personId.getId(), cardId.getId() };
		// final int[] argTypes = new int[] { Types.NUMERIC, Types.NUMERIC };
		final Object[] args = new Object[] { personId.getId() };
		final int[] argTypes = new int[] { Types.NUMERIC };

		// ���������� ��������� ����� � ������ answer � ���� ����� ...
		final Set answer = new HashSet();
		jdbc.query( 
				bufQuery_arg2.toString(), 
				args, 
				argTypes,
				new RowCallbackHandler() {
					public void processRow(ResultSet rs) throws SQLException 
					{
						final String s = rs.getString(1);
						if (s != null)
							answer.add( new ObjectId(SystemRole.class, s) );
					}}
			);
		
		return answer;
	}


	/**
	 * Returns set of {@link PersonAttribute} identifiers from given card
	 * which values includes given personId
	 * @param personId identifier of {@link Person} to be searched in card's attributes
	 * @param cardId identifier of {@link Card} 
	 * @param jdbc jdbcTemplate
	 * @return set of {@link PersonAttribute} identifiers, this method never returns null
	 */
	public static Set getUserPersonAttributesForCard( ObjectId personId, 
			ObjectId cardId, 
			JdbcTemplate jdbc) 
	{
		final Set result = new HashSet();
		RowCallbackHandler rh = new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				ObjectId id = new ObjectId(PersonAttribute.class, rs.getString(1));
				result.add(id);
			}
		};
		jdbc.query(
			"SELECT av.attribute_code \n" +
			"FROM attribute_value av \n" +
			"WHERE \n" +
			"		av.card_id = ? \n" +
			"		AND av.number_value = ? \n" +
			"		AND exists (\n" +
			"			SELECT 1 " +
			"			FROM attribute a " +
			"			WHERE a.attribute_code = av.attribute_code " +
			"				and a.data_type = ? \n" +
			"		 )",
			new Object[] {
				(Long)cardId.getId(),
				(Long)personId.getId(),
				AttributeTypes.PERSON
			},
			new int[] {
				Types.NUMERIC,
				Types.NUMERIC,
				Types.VARCHAR
			},
			rh
		);
		return result;
	}


	/**Check if the user has requested role for the card (directly or 
	 * via inheritance).
	 * PL/SQL prototype:
	 * function Has_Role(
	 * 		pPerson_ID in Person.Person_Id%type,
	 * 		pRole_Code in System_Role.Role_Code%type,
	 * 		pCard_ID in Card.Card_Id%type) return number
	 * 	);
	 * @param roleCode
	 * @param personId
	 * @param cardId
	 * @param jdbc
	 * @return true if the person has the role. 
	 */
//	public static boolean hasRoleForCard( String roleCode, ObjectId personId, 
//			ObjectId cardId, JdbcTemplate jdbc)
//	{
//		return (roleCode != null) 
//			&&	getPersonRoles4Card( personId, cardId, jdbc)
//					.contains( new ObjectId( SystemRole.class, roleCode ));
//	}


	/**
	 * ������ chkCardAccess(arg1..arg4) , �� ���� �������� ������ ���� 
	 * ������������ �� �������� (������� ����� �������� ����� hasRole4Card).
	 * @param permission
	 * @param objectId
	 * @param personId 
	 * @param cardId �������� ������ ��� �������� ���������������� ������ card_access.
	 * @param roles4card ������ ����� ������������, ������� ������� ��� ������ ��������
	 * @param jdbc
	 * @return
	 */
//	public static boolean chkCardAccessByRoles( 
//			Set roles4card,	
//			Long permission, 
//			ObjectId objectId,
//			ObjectId personId, 
//			ObjectId cardId,
//			JdbcTemplate jdbc)
//	{
//		try {
//			// �������: ������ �� ���� � ������ ����� ������������...
//			final String sCondition;
//			if (roles4card == null || roles4card.isEmpty()) {
//				sCondition = "ca.role_code is null";
//			} else {
//				sCondition = "ca.role_code is null OR ca.role_code in (" 
//					+ SimpleDBUtils.stringIdentifiersToCommaSeparatedSqlString(roles4card) 
//					+ ")";
//			}
//
//			final String Query_ChkCardAccess_arg4_fmt1	=	
//				"\n  select ca.role_code \n"+  
//				"  from	card_access ca \n"+
//				"  		, card c \n"+
//				"  where \n"+
//				"  		c.card_id = (?) \n"+ // (:cardId)
//				"  		and c.template_id = ca.template_id \n"+
//				"  		and ca.permission_type = (?) \n"+ // (:permissionType)
//				"  		and ca.object_id = (?) \n"+ // (:objectId) 
//				"  		and ( {0} ) \n"+ // {:roles}
//				"  		and ( \n"+
//				"  			ca.person_attribute_code is null \n"+
//				"  			or exists ( \n"+
//				"  				select null \n" +
//				"  				from attribute_value av \n"+
//				"  				where \n"+
//				"  					av.attribute_code = ca.person_attribute_code \n"+
//				"  					and av.number_value = (?) \n"+ // (:personId)
//				"  					and av.card_id = c.card_id \n"+ // (:cardId) 
//				"  			) \n"+
//				"  		)";
//			
//			final String sQuery_arg4
//				= MessageFormat.format( Query_ChkCardAccess_arg4_fmt1, 
//										new Object[] { sCondition } );
//
//			// ���� �����������: ( cardId, permissionType, objectId, personId, cardId )
//			final Object[] args	= new Object[] {
//				(Long)cardId.getId(),
//				permission,
//				(Long)objectId.getId(),
//				(Long)personId.getId()
//			};
//			final int[] argTypes = new int[] {
//				Types.NUMERIC,
//				Types.NUMERIC,
//				Types.NUMERIC,
//				Types.NUMERIC 
//			};
//
//			final SqlRowSet result = jdbc.queryForRowSet( sQuery_arg4, args, argTypes);
//			// ���� ���� ���� �� ���� ������ - ������ ����������� ...
//			return (result != null) && result.first();
//
//		} catch (IncorrectResultSizeDataAccessException ex) {
//			return false;
//		}
//	}

	/**
	 * Check if the user has requested permission for accessing the card+object.
	 * function Check_Access(
	 * 	1	pPerson_ID in Person.Person_Id%type,
	 * 	2	pCard_ID in Card.Card_Id%type,
	 * 	3	pPermission_Type in card_access.permission_type%type,
	 * 	4	pObject_ID in card_access.object_id%type
	 * 	); 
	 * @param permission:
	 * @param objectId: is used only if permission is NOT in (CardAccess.EDIT_CARD, CardAccess.READ_CARD) 
	 * @param personId
	 * @param cardId
	 * @param jdbc
	 * @return true if the use has the requested permission.
	 */
	public static boolean chkCardAccess( Long permission, ObjectId objectId, 
		ObjectId personId, ObjectId cardId, JdbcTemplate jdbc)
	{
		//final Set userEffRoles = getPersonRoles4Card(personId, cardId, jdbc);
		//return chkCardAccessByRoles( userEffRoles, permission, objectId, personId, cardId, jdbc);

		final boolean useObjectId = 
			(permission != CardAccess.READ_CARD) && 
			(permission != CardAccess.EDIT_CARD);

		final NamedParameterJdbcTemplate jdbcNamed = new NamedParameterJdbcTemplate(jdbc);
		final String sqlText = 
				DelegatorBean.makeSqlSelectCAWithDelegations( 
						"count(*)", personId.getId().toString(), 
						"ca.permission_type = (:permType)",
						// ������� cardCond:
						(permission == CardAccess.CREATE_CARD)
							? null
							: (useObjectId) 
								?	"c.card_id = (:cardId) and ca.object_id=(:objectId)"
								:	"c.card_id = (:cardId) and ca.object_id=c.status_id"
						, null // inlineTemplateCond
					);

		final MapSqlParameterSource mapArgs = new MapSqlParameterSource()
					.addValue("permType", permission, Types.NUMERIC)
					.addValue("cardId", cardId.getId(), Types.NUMERIC);
		if (useObjectId)
			mapArgs.addValue("objectId", objectId.getId(), Types.NUMERIC);
		final long acount = jdbcNamed.queryForLong( sqlText, mapArgs);

//		DEBUG OUTPUT:
//		System.out.append( "chkCardAccess:" +
//				"\n\t permission: "+ permission.longValue() + 
//				"\n\t objectId: "+ objectId + 
//				"\n\t personId: "+ personId + 
//				"\n\t cardId  : "+ cardId + 
//				"\n\t returns : " + acount + " ca-record(s)"+
//				"\n\t using check card access query: \n"+
//				SimpleDBUtils.getSqlQueryInfo( sqlText, 
//							new Object[] { 
//									permission, 
//									(cardId != null && cardId.getId() != null) 
//											? String.valueOf( ((Long) cardId.getId()).longValue() ) 
//											: null 
//							}, 
//							new int[] { Types.NUMERIC, Types.NUMERIC }
//						)
//		);

		return acount > 0;
	}


	/**
	 * ��������� ��� ��������� �������� ������ ���� ��������, 
	 * ����������� � ������� (�������� ������� �������� � ���� ������������).
	 * @return ������ �������� (Long) ��� null, ���� 
	 * ������ �� ����� ��� �������� ����.
	 */
	public static Set /* <Long> */ getCardRegions(ObjectId cardId, JdbcTemplate jdbc)
	{
		Long regionId;
		try {
			/* �������� ������ ��� �������� */
			regionId = new Long( jdbc.queryForLong( 
				"SELECT av.value_id FROM attribute_value av WHERE av.attribute_code = ? AND av.card_id = ?", 
				new Object[]{ (String)Attribute.ID_REGION.getId(), (Long)cardId.getId() },
				new int[] {Types.VARCHAR, Types.NUMERIC }
			));
		} catch (EmptyResultDataAccessException ex) {
			return null; // ��� �������� �������
		}
		
		final Set result = new HashSet();
		try {
			// �������� ������ �� ���� � ���� ��� ������������ �������� ...
			// (!) NULL ������ ������������ ��� ����;
			// (!) ���������� ������ �� ��������� ������.
			while ( (regionId.longValue() != 0) && (!result.contains(regionId))) {
				result.add( regionId);
				regionId = new Long( jdbc.queryForLong( 
					"SELECT vl.parent_value_id FROM values_list vl WHERE vl.value_id = ?",
					new Object[]{ regionId } 
				));
			};
		} catch (IncorrectResultSizeDataAccessException ex) {
			// ����� �� ������ �������� ��������, ���� ������ ��������� ����...
		}
		return result;
	}

}
