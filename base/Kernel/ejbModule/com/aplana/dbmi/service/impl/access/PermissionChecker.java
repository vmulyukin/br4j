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
package com.aplana.dbmi.service.impl.access;
import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.AccessCheckerBase;
import com.aplana.dbmi.service.impl.UserData;

/**
 * @author RAbdullin
 * (portation of PL/SQL stored procs at "PKG_PERMISSION::Has_Role/Check_Access")
 * This utility class realize security logic of user accessing 
 * to any cards via actions.
 * 
 */
public class PermissionChecker extends AccessCheckerBase {

	private Long needPermission;
	private Object needObjectId;

	public PermissionChecker() { 
		super(); 
	}
	
	public PermissionChecker(JdbcTemplate jdbc) {
		super();
		setJdbcTemplate(jdbc);
	}

	public PermissionChecker(Long perm, Object needObjId) {
		this( perm, needObjId, null);
	}
	
	public PermissionChecker(Long permission, Object needObjId, JdbcTemplate jdbc) {
		super();
		setJdbcTemplate(jdbc);
		this.needPermission = permission;
		this.needObjectId = needObjId;
	}

	/**Example: CardAccess.READ_CARD
	 * @return assigned value
	 */
	public Long getNeedPermission()
	{
		return this.needPermission;
	}

	/**
	 * Example: CardAccess.READ_CARD
	 */
	public Long setNeedPermission(Long permission)
	{
		/*
		if ( 	(permission == this.needPermission) 
			||	(permission != null && permission.equals(this.needPermission))
			) // no changes
			return this.needPermission;
		... before changing value ...
		*/
		
		// assign new value
		return this.needPermission = permission;
	}
	

	public Object getNeedObjectId()
	{
		return this.needObjectId;
	}
	
	public Object setNeedObjectId(Object objId)
	{
		/*
		if ( 		(objId == this.needObjectId) 
				||	(objId != null && objId.equals(this.needObjectId))
				) // no changes
				return this.needObjectId;
		... before changing value ...
		 */

		// new value...
		return this.needObjectId = objId;
	}
 
	public boolean chkCardRWAccess( Long permission, ObjectId personId, ObjectId cardId)
	{
		if (personId == null || cardId == null)
			return false;

		return CardAccessUtils.chkCardAccess(permission, cardId, personId, cardId, this.getJdbcTemplate());

//		try {
//			/**
//			 * (CardAccess) �������� ������ ����������� ����� �� �������� � ���������.
//			 * (!) ���������:
//			 * 	 	(1) "{0}"	java-�������� ��� �������� ������� ������ ����,
//			 * ��������, "1=1" ��� "ca.role_code is null or (ca.role_code in (...))";
//			 * 		(2) 3 ����������� sql-��������� (�����):  cardId, personId, permissionType.
//			 * 			4 �����������: ( cardId, permissionType, personId, cardId )
//			 */
//			final String sql_arg4_fmt1 = 
//					"select ca.role_code \n"+  
//					"from	card_access ca \n"+
//					"		join card c on c.template_id = ca.template_id \n" +
//					"			and c.status_id = ca.object_id \n" + // -(:objectId)
//					// "\t	, template t \n"+
//					"where \n"+
//					"	c.card_id = (?) \n"+ // (:cardId)
//					// "\t and c.template_id = t.template_id \n"+
//					// "\t and t.template_id = ca.template_id \n"+
//					"	and ca.permission_type = (?) \n"+ // (:permissionType)
//					"	and ( {0} ) \n"+ // userEffRoles (sCondition)
//
//					"	and ( \n"+
//					"		ca.person_attribute_code is null \n"+
//					"		or exists ( \n"+
//					"			select null \n" +
//					"			from attribute_value av \n"+
//					"			where \n"+
//					"				av.attribute_code = ca.person_attribute_code \n"+
//					"				and av.number_value = (?) \n"+ // (:personId)
//					"				and av.card_id = (?) \n"+ // (:cardId) 
//					"		) \n"+
//					"	) \n";
//			
//			
//			final Set userEffRoles 
//				= CardAccessUtils.getUserRoles4Card( personId, cardId, getJdbcTemplate());
//			
//			/* >>> (2009/12/09, DSultanbekob, RuSA)
//			 * OLD:
//			if (userEffRoles == null || userEffRoles.isEmpty())
//				// � ������������ ��� ������ ������� ���� (�� �� ���)...
//				return false;
//			 */
//
//			// �������: ������ �� ��������� ���� � ������ ����� ������������...
//			String sCondition;
//			if ( (userEffRoles == null) || userEffRoles.isEmpty() )
//				sCondition = "ca.role_code is null";
//			else
//				sCondition = MessageFormat.format( "(ca.role_code is null) OR (ca.role_code in ({0}))", 
//						new Object[] { SimpleDBUtils.stringIdentifiersToCommaSeparatedSqlString(userEffRoles)} );
//			// <<< (2009/12/09)
//
//			final String sQuery_arg4
//				= MessageFormat.format( sql_arg4_fmt1, new Object[] { sCondition});
//
//			// ������ �����������: ( cardId, permissionType, personId, cardId )
//			final Object[] args 
//				= new Object[] {	cardId.getId(), permission,  
//									personId.getId(), cardId.getId() };
//			final int[] argTypes
//				= new int[] {	Types.NUMERIC, Types.NUMERIC, 
//								Types.NUMERIC, Types.NUMERIC };
//
//			final SqlRowSet result 
//				= getJdbcTemplate().queryForRowSet( sQuery_arg4, args, argTypes);
//
//			// ������ ����������� - ���� ���� ���� �� ���� ������ (��� ��������
//			// "� ������������ ���� ����, ������� �������� ��������� ������
//			// � ��������" ...
//			return (result != null) && (result.first());
//
//		} catch (IncorrectResultSizeDataAccessException ex) {
//			return false;
//		}
		
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.service.impl.AccessCheckerBase#checkAccess()
	 */
	// @Override
	public boolean checkAccess() throws DataException 
	{

		final Card card = (Card) getObject();
		final UserData user = getUser();
		
		if (card == null || card.getId() == null) 
			return false;
		
		if (user == null || user.getPerson() == null) 
			return false;
		
		return chkCardRWAccess( this.getNeedPermission(),  
					getUser().getPerson().getId(), 
					card.getId()
					);
	}


}
