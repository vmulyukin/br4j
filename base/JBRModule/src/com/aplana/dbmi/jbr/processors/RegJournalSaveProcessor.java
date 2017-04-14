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
package com.aplana.dbmi.jbr.processors;

import java.util.Collections;
import java.util.List;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;

import org.springframework.jdbc.core.JdbcTemplate;

@SuppressWarnings("serial")
public class RegJournalSaveProcessor extends ProcessCard
{
	/**
	 * ��������, ����������� id �������� � �������� ������� ������������, �������
	 * ����� ��������� �� ������� :����� �����������
	 */
	private final String PARAM_OWNER_REGJOURNAL_ATTR_ID = "owner_regjournal_attr_id";
	/**
	 * ��� �������� "������� ������������" � ������� "������ �����������" ������ ����������� �����
	 * ��������� � �������� �������
	 */
	private final String PARAM_INDEXES_ATTR_ID = "indexes_attr_id";
	/**
	 * �������� ��������� id ������� ������� �����������
	 */
	private final String PARAM_INDEX_TEMPLATE_ID = "index_template_id";
	
	private ObjectId owner_regjournal_attr_id = null;
	private ObjectId indexes_attr_id = null;
	private ObjectId index_template_id = null;

	
	@Override
	public Object process() throws DataException
	{
		Card cur_card = getCard();
		if( cur_card == null )
		{
			logger.error("No active card?!");
			return null;
		}
		ObjectId cur_card_id = cur_card.getId();
		if( cur_card_id == null )
		{
			logger.error("Card not saved? cannot work!");
			return null;
		}
		if( owner_regjournal_attr_id == null || indexes_attr_id == null )
		{
			logger.error( String.format("Required parameters for processor (%s; %s) are not set!",
				PARAM_INDEXES_ATTR_ID, PARAM_OWNER_REGJOURNAL_ATTR_ID) );
			return null;
		}
		
		CardLinkAttribute links_attr = (CardLinkAttribute)cur_card.getAttributeById( indexes_attr_id );
		if( links_attr == null )
		{
			logger.warn( "No linked nomenclature indexes?" );
			return null;
		}
		
		List<ObjectId> linked_card_ids = links_attr.getIdsLinked();
		if( linked_card_ids != null )
		{
			for( ObjectId linked_card_id: linked_card_ids )
			{
				logger.warn( String.format( "   =>> Linked to %d", linked_card_id.getId() ) );
				Card child = loadCardById( linked_card_id ); //...instead of getQueryFactory().getFetchQuery( Card.class );
				linkChildCardToMe( child, cur_card_id );
			}
		}
		
		removeHangingLinks( cur_card_id, linked_card_ids );
		
		return null;
	}
	
	
	@Override
	public void setParameter( String name, String value )
	{
		if( PARAM_OWNER_REGJOURNAL_ATTR_ID.equalsIgnoreCase( name ) )
		{
			owner_regjournal_attr_id = IdUtils.smartMakeAttrId( value, CardLinkAttribute.class );
		}
		else if( PARAM_INDEXES_ATTR_ID.equalsIgnoreCase( name ) )
		{
			indexes_attr_id = IdUtils.smartMakeAttrId( value, CardLinkAttribute.class );
		}
		else if( PARAM_INDEX_TEMPLATE_ID.equalsIgnoreCase( name ) )
		{
			index_template_id = IdUtils.smartMakeAttrId( value, Template.class );
		}
		else
			super.setParameter( name, value );
	}
	
	
	protected void linkChildCardToMe( Card child, ObjectId cur_card_id )
	{
		if( child == null || cur_card_id == null ) return;
		try
		{
			CardLinkAttribute child_link = (CardLinkAttribute)child.getAttributeById( owner_regjournal_attr_id );
			//if( child_link == null )
			//{
				// this should never happen
			//}
			//child_link.setIdsLinked( link_value );
			child_link.addLinkedId( cur_card_id );
			// lock linked card as system user
			LockObject lock_action = new LockObject( child );
			execAction( lock_action, getSystemUser() );
			// saveCard( child, getSystemUser() ); // no, only overwrite attributes
			// overwrite child card attributes
			try {
				OverwriteCardAttributes overwrite_action = new OverwriteCardAttributes();
				overwrite_action.setCardId( child.getId() );
				overwrite_action.setAttributes( Collections.singletonList(child_link) );
				overwrite_action.setInsertOnly( false );
				execAction( overwrite_action, getSystemUser() );
			} finally {
				// unlock
				UnlockObject unlock_action = new UnlockObject( child );
				execAction( unlock_action );
				// TODO: remove next logger line?
				logger.info( String.format("Successfully linked child [%d] to me", cur_card_id.getId() ) );
			}
		}
		catch( DataException e )
		{
			logger.warn( "DataException while linking child card to current card!", e );
		}
	}
	
	
	protected void removeHangingLinks( ObjectId cur_card_id, List<ObjectId> linked_card_ids )
	{
		// ������� ��� ������ �� ������� ������ ����������� �� ���� �������� �����������, �������
		//  ��������� �� ������� ������, �� ��� ���� ��� ������ �� ��� ������� �� ���������
		//  � ������ �������� ����� ������->������ �������� ������������� �����
		//  ������->������, ��� �-�� � ���� ����� & �������
		
		String sql = null;
		
		if( linked_card_ids == null || linked_card_ids.isEmpty() )
		{
			// ���� ��� ����������� �������� � ����� ������� -
			// ������� ��� ������ �� ���� �� ���� �������� �����������
			sql = String.format( "DELETE FROM attribute_value WHERE attribute_code='%s' " +
				"    AND  template_id=%d  AND  number_value=%d",
				owner_regjournal_attr_id.getId().toString(),
				index_template_id.getId(),
				cur_card_id.getId() );
		}
		else
		{
			StringBuffer buf = new StringBuffer( "DELETE FROM attribute_value where attr_value_id in ( \n" );
			buf.append( "    SELECT attr_value_id FROM attribute_value av WHERE \n" );
			buf.append( "        av.attribute_code='" );
			buf.append( owner_regjournal_attr_id.getId().toString() );
			buf.append( "' \n" );
			buf.append( "        AND av.number_value=" );
			buf.append( cur_card_id.getId() );
			buf.append( " \n" );
			buf.append( "        AND av.template_id=" );
			buf.append( index_template_id.getId().toString() );
			buf.append( " \n" );
			buf.append( "        AND av.card_id NOT IN (" );
			buf.append( IdUtils.makeIdCodesEnum( linked_card_ids, "," ) );
			buf.append( ")\n" );
			buf.append( "    )\n" );
			
			sql = buf.toString();
		}
		
		JdbcTemplate tmpl = getJdbcTemplate();
		if( sql != null && tmpl != null )
		{
			int modified = tmpl.update( sql );
			logger.warn( String.format( "%d hanging owner-links to this reg journal were removed from DB", modified ) );
		}
	}

}
