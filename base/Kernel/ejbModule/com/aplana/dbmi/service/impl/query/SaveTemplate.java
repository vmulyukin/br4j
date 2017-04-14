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

import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * {@link SaveQueryBase} descendant used to save instances of {@link Template} class.
 * Updates information in TEMPLATE, TEMPLATE_ATTRIBUTE, TEMPLATE_BLOCK, DEFAULT_ATTRIBUTE_VALUE,
 * ATTRIBUTE_VIEW_PARAM tables.
 */
public class SaveTemplate extends SaveQueryBase
{
	/**
	 * Identifier of 'New template created' log event
	 */
	public static final String EVENT_ID_CREATE = "NEW_TEMPL";
	/**
	 * Identifier of 'Template changed' log event
	 */
	public static final String EVENT_ID_CHANGE = "CHG_TEMPL";

	/**
	 * Checks validity of {@link Template} instance being saved.
	 */
	public void validate() throws DataException
	{
		Template template = (Template) getObject();
		if (template.getId() == null) {
			if (!template.isActive())
				throw new DataException("store.template.newinactive");
		}
		HashSet setBlocks = new HashSet();
		Iterator itr = template.getBlocks().iterator();
		while (itr.hasNext())
		{
			TemplateBlock block = (TemplateBlock) itr.next();
			if (block.getId() == null || block.getId().getId() == null)
				throw new DataException("store.template.noidblock");
			if (setBlocks.contains(block.getId()))
				throw new DataException("store.template.doubleblock",
						new Object[] { block.getNameRu(), block.getNameEn() });
			setBlocks.add(block.getId());
			if (AttributeBlock.ID_REMOVED.equals(block.getId()))
				throw new DataException("store.template.wrongblock",
						new Object[] { DataException.RESOURCE_PREFIX + "block." + AttributeBlock.ID_REMOVED.getId() });
			if (AttributeBlock.ID_REST.equals(block.getId()))
				throw new DataException("store.template.wrongblock",
						new Object[] { DataException.RESOURCE_PREFIX + "block." + AttributeBlock.ID_REST.getId() });
		}
		if (!setBlocks.contains(AttributeBlock.ID_COMMON))
			throw new DataException("store.template.noblock",
					new Object[] { DataException.RESOURCE_PREFIX + "block." + AttributeBlock.ID_COMMON.getId() });
		super.validate();
	}
	
	public String getEvent() {
		return isNew() ? EVENT_ID_CREATE : EVENT_ID_CHANGE;
	}
	
	protected ObjectId processNew() throws DataException
	{
		Template template = (Template) getObject();
		template.setId(generateId("seq_template_id"));
		getJdbcTemplate().update(
			"INSERT INTO template (" +
				"template_id" +
				", template_name_rus" +
				", template_name_eng" +
				", workflow_id" +				
				", locked_by" +
				", lock_time" +
				", show_in_createcard" +
				", show_in_search" +
			") VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
			new Object[] {
				template.getId().getId(),
				template.getNameRu(),
				template.getNameEn(),

				template.getWorkflow().getId(),				
				getUser().getPerson().getId().getId(),
				new Date(),

				template.isShowInCreateCard() ? new Long(1) : new Long(0),
				template.isShowInSearch() ? new Long(1) : new Long(0)
			},
			new int[] {
				Types.NUMERIC,
				Types.VARCHAR,
				Types.VARCHAR,

				Types.NUMERIC,
				Types.NUMERIC,
				Types.TIMESTAMP,

				Types.NUMERIC,
				Types.NUMERIC
			}
		);

		//1.
		insertTemplateBlockTemplateAttributes();

		//2.
//		saveCardAccess();

		return template.getId();
	}
	
	private Map getTemplateAttrIdMap(Long templateId) {
		final Map result = new HashMap();
		getJdbcTemplate().query(
			"select attribute_code, template_attr_id from template_attribute where template_id = ?",
			new Object[] {templateId},
			new int[] {Types.NUMERIC }, // (2010/03) POSTGRE OLD: Types.DECIMAL
			new RowCallbackHandler() {
				public void processRow(ResultSet rs) throws SQLException {
					result.put(rs.getString(1), new Long(rs.getLong(2)));
				}
			}
		);
		return result;
	}
	
	protected void processUpdate() throws DataException
	{
		checkLock();
		Template template = (Template) getObject();
		Long templateId = (Long)template.getId().getId();
		getJdbcTemplate().update(
			"UPDATE template SET " +
				"template_name_rus=?" +
				", template_name_eng=?" +
				", is_active=?" +
				", workflow_id = ? " +
				", show_in_createcard = ? " +
				", show_in_search = ? " +
			"WHERE template_id=?",
				new Object[] {
					template.getNameRu(),
					template.getNameEn(),
					//Boolean.valueOf(template.isActive()),
					new Integer(template.isActive() ? 1 : 0),
					
					template.getWorkflow().getId(),
					new Long(template.isShowInCreateCard() ? 1 : 0),
					new Long(template.isShowInSearch() ? 1 : 0),
					
					templateId
				},
				new int[] { // (2010/03, POSTGRE)
					Types.VARCHAR,
					Types.VARCHAR,
					Types.NUMERIC,
					
					Types.NUMERIC,
					Types.NUMERIC,
					Types.NUMERIC,
					
					Types.NUMERIC
				}
		);

		ChildrenQueryBase q = getQueryFactory().getChildrenQuery(Template.class, AttributeViewParamDetail.class);
		q.setParent(template.getId());
		Collection oldAttributeViewParams = (Collection)getDatabase().executeQuery(getUser(), q);
		
		q = getQueryFactory().getChildrenQuery(Template.class, DefaultAttributeValue.class);
		q.setParent(template.getId());
		Collection defaultAttributeValues = (Collection)getDatabase().executeQuery(getUser(), q);

		
		q = getQueryFactory().getChildrenQuery(Template.class, BlockViewParam.class);
		q.setParent(template.getId());
		Collection oldBlockViewParams = (Collection)getDatabase().executeQuery(getUser(), q);
		
		
		getJdbcTemplate().update(
			"DELETE FROM attribute_view_param avp WHERE exists " +
			"(select 1 from template_attribute ta where ta.template_attr_id = avp.template_attr_id" +
			" and ta.template_id = ?)",
			new Object[] { templateId },
			new int[] { Types.NUMERIC } // (2010/03, POSTGRE)
		);
		
		getJdbcTemplate().update(
				"DELETE FROM block_view_param WHERE template_id = ?",
				new Object[] { templateId },
				new int[] { Types.NUMERIC } // (2010/03, POSTGRE)
				);
		
		getJdbcTemplate().update(
			"DELETE FROM default_attribute_value dav WHERE exists " +
			"(select 1 from template_attribute ta where ta.template_attr_id = dav.template_attr_id" +
			" and ta.template_id = ?)",
			new Object[] { templateId },
			new int[] { Types.NUMERIC } // (2010/03, POSTGRE)
			
		);

		getJdbcTemplate().update(
				"delete from workflow_move_required_field w where EXISTS " +
				"(select 1 from template_attribute ta WHERE w.template_attr_id= ta.template_attr_id and ta.template_id=?)",
				new Object[] { templateId },
				new int[] { Types.NUMERIC } // (2010/03, POSTGRE)
		);
		
		getJdbcTemplate().update(
				"DELETE FROM template_attribute WHERE template_id = ?",
				new Object[] { templateId },
				new int[] { Types.NUMERIC } // (2010/03, POSTGRE)
				);
		getJdbcTemplate().update(
				"DELETE FROM template_block WHERE template_id = ?",
				new Object[] { templateId },
				new int[] { Types.NUMERIC } // (2010/03, POSTGRE)
				);
		
		/*getJdbcTemplate().update(
				"DELETE FROM card_access WHERE template_id = ?",
				new Object[] { templateId },
				new int[] { Types.NUMERIC } // (2010/03, POSTGRE)
				);*/

		
		//1.
		insertTemplateBlockTemplateAttributes();

		//2.
//		saveCardAccess();

		//3. utility
		Map	newAttributeCodes = getTemplateAttrIdMap(templateId);

		//4.
		saveAttributeViewParams(oldAttributeViewParams, newAttributeCodes);
		
		saveBlockViewParams(oldBlockViewParams);
		
		//5.
		if(!defaultAttributeValues.isEmpty()){
			saveDefaultAttributeValues(defaultAttributeValues, newAttributeCodes);			
		}
		
		//6. save workflow required fields.
		prepareWorkflowRequiredFields(newAttributeCodes);
		saveWorkflowRequiredFields();
	}


	private void insertTemplateBlockTemplateAttributes() throws DataException {
		
		final Template template = (Template) getObject();
		
		final Long templateId = (Long)template.getId().getId();
		
		getJdbcTemplate().execute(new ConnectionCallback() {
			public Object doInConnection(Connection conn) throws SQLException, DataAccessException {
				
				PreparedStatement blockInsert = conn.prepareStatement(
						"INSERT INTO template_block (template_id, block_code) VALUES (?, ?)");
				PreparedStatement attrInsert = conn.prepareStatement(
						"INSERT INTO template_attribute (template_id, block_code, attribute_code, is_mandatory, is_hidden, is_readonly, order_in_list, column_width) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
/*				int column = 0;
				int row = 0;
*/				Iterator iBlock = template.getBlocks().iterator();
				while (iBlock.hasNext())
				{
					TemplateBlock block = (TemplateBlock) iBlock.next();
					// TODO ������ ��� Ace					
/*					if (block.getColumn() < column)
						System.err.println("[DataService] Warning! Invalid column order: " +
								block.getColumn() + " follows " + column + " - block " + block.getId());
					if (block.getColumn() > column) {
						column = block.getColumn();
						row = 0;
					}
					int layout = (column + 1) * 100 + row++;
*/					blockInsert.setObject(1, templateId);
					blockInsert.setObject(2, block.getId().getId());
//	TODO ������ ��� Ace				blockInsert.setInt(3, layout);
					blockInsert.execute();
					
					Iterator iAttr = block.getAttributes().iterator();
					while (iAttr.hasNext())
					{
						Attribute attr = (Attribute) iAttr.next();
						attrInsert.setObject(1, template.getId().getId());
						attrInsert.setObject(2, block.getId().getId());
						attrInsert.setObject(3, attr.getId().getId());
						attrInsert.setInt(4, attr.isMandatory() ? 1 : 0);
						attrInsert.setInt(5, attr.isHidden() ? 1 : 0);
						attrInsert.setInt(6, attr.isReadOnly() ? 1 : 0);
						if (attr.isSearchShow())
							attrInsert.setInt(7, attr.getSearchOrder());
						else
							attrInsert.setNull(7, Types.NUMERIC);
						attrInsert.setInt(8, attr.getColumnWidth());
						attrInsert.execute();
					}
				}
				return null;
			}
		});
		
		
	}
	
	private void saveAttributeViewParams(final Collection oldAttributeViewParams, final Map newAttributeCodes) {
		
		final Collection avpdCollection = oldAttributeViewParams;//getAttributeViewParamsForUpdate(template, oldAttributeViewParams);
		
		if(avpdCollection!=null){
			Iterator i = avpdCollection.iterator();
			while (i.hasNext()) {
				AttributeViewParamDetail avpd = (AttributeViewParamDetail)i.next();
				if (!newAttributeCodes.containsKey(avpd.getAttributeCode())) {
					i.remove();
				}
			}
			
			String sql = "insert into attribute_view_param (rec_id, template_attr_id," +
				" status_id, role_code, person_attribute_code, is_mandatory, is_hidden, is_readonly)" +
				" values (?, ?, ?, ?, ?, ?, ?, ?)";
			BatchPreparedStatementSetter pss = new BatchPreparedStatementSetter() {
				Iterator iter = avpdCollection.iterator();
				public int getBatchSize() {
					return avpdCollection.size();
				}
	
				public void setValues(PreparedStatement stmt, int index)
				throws SQLException {
					AttributeViewParamDetail avpd = (AttributeViewParamDetail)iter.next();
	
					if(avpd.getId()==null || avpd.getId().getId()==null){
						//if new definitions added
						stmt.setLong(1, generateId("seq_attribute_view_param"));
					}else{
						//if old definitions used
						stmt.setLong(1, ((Long)avpd.getId().getId()).longValue());
					}
					stmt.setLong(2, ( (Long)newAttributeCodes.get(avpd.getAttributeCode())).longValue());
					stmt.setLong(3, avpd.getStateId());
					stmt.setString(4, avpd.getRoleCode());
					stmt.setString(5, avpd.getPersonAttributeId() == null ? null : (String)avpd.getPersonAttributeId().getId());
					stmt.setInt(6, avpd.isMandatory() ? 1 : 0);
					stmt.setInt(7, avpd.isHidden() ? 1 : 0);
					stmt.setInt(8, avpd.isReadOnly() ? 1 : 0);
				}
			};		
			getJdbcTemplate().batchUpdate(sql, pss);
		}
	}

	/*	
	private Collection getAttributeViewParamsForUpdate(Template template, Collection oldAttributeViewParams) {

		Collection avpdCollection = template.getAttributesViewParamDetails();
		
		if(avpdCollection!=null) {
			return avpdCollection;
		}
		else if(oldAttributeViewParams!=null){
			return oldAttributeViewParams;
		}

		return null;
	}
	*/
	
	private void saveBlockViewParams(Collection oldBlockViewParams) {
		final Collection newBlockViewParams = oldBlockViewParams;
		
		final Template template = (Template) getObject();
		
		Iterator iterBlocks = template.getBlocks().iterator();
		Collection blocksId = new ArrayList();
		while(iterBlocks.hasNext()) {
			blocksId.add(((TemplateBlock)iterBlocks.next()).getId());
		}
		
		Iterator iterBvp = newBlockViewParams.iterator();
		while(iterBvp.hasNext()) {
			if (!blocksId.contains(((BlockViewParam)iterBvp.next()).getBlock()))
				iterBvp.remove();
		}
		
		String sql = "insert into block_view_param (rec_id, template_id, block_code, status_id, state_block) "+ 
					 "values (?, ?, ?, ?, ?)";
		BatchPreparedStatementSetter pss = new BatchPreparedStatementSetter() {
			Iterator iter = newBlockViewParams.iterator();
			public int getBatchSize() {
				return newBlockViewParams.size();
			}
	
			public void setValues(PreparedStatement stmt, int index)
			throws SQLException {
				BlockViewParam viewParam = (BlockViewParam)iter.next();
	
				stmt.setLong(1, ((Long)viewParam.getId().getId()).longValue());
				stmt.setLong(2, ((Long)viewParam.getTemplate().getId()).longValue());
				stmt.setString(3, (String)viewParam.getBlock().getId());
				stmt.setLong(4, ((Long)viewParam.getCardStatus().getId()).longValue());
				stmt.setLong(5, viewParam.getStateBlock());
			}
		};		
		getJdbcTemplate().batchUpdate(sql, pss);
	}
	
	//1. remove all references to not existing attributes in this template and set template_attr_ids
	private void prepareWorkflowRequiredFields(Map newAttributeCodes) {
		
		final Template template = (Template) getObject();

		final Set attributeCodes = newAttributeCodes.keySet();
		
		//1. remove all references to not existing attributes in this template and set template_attr_ids
		final List toRemove = new ArrayList();
		
		final Collection workflowMoveRequiredFields 
			= template.getWorkflowMoveRequiredFields();
		for (Iterator iterator = workflowMoveRequiredFields.iterator(); iterator.hasNext();) {
			
			final WorkflowMoveRequiredField w 
				= (WorkflowMoveRequiredField) iterator.next();
			
			// if( !w.isRequired()  || ! attributeCodes.contains(w.getAttributeCode()))
			if ( !attributeCodes.contains( w.getAttributeCode() ) )
			{
				toRemove.add(w);
			}else{
				w.setTemplateAttributeId((Long)newAttributeCodes.get(w.getAttributeCode()));
			}
		}
		workflowMoveRequiredFields.removeAll(toRemove);
	}

	//2. save 
	private void saveWorkflowRequiredFields() {
		final Template template = (Template) getObject();
		final Collection workflowMoveRequiredFields = template.getWorkflowMoveRequiredFields();
		
		final String sql = "insert into workflow_move_required_field (wfm_id, template_attr_id, must_be_set) values(?, ?, ?)";
		
		BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
			final Iterator iter = workflowMoveRequiredFields.iterator();
			public int getBatchSize() {
				return workflowMoveRequiredFields.size();
			}
			public void setValues(PreparedStatement stmt, int index) throws SQLException {
				final WorkflowMoveRequiredField w 
					= (WorkflowMoveRequiredField) iter.next();

				stmt.setLong(1, w.getWorkflowMoveId().longValue());
				stmt.setLong(2, w.getTemplateAttributeId().longValue());
				stmt.setLong(3, w.getMustBeSetCode() );
			}
		};		
		getJdbcTemplate().batchUpdate(sql, bpss);
	}


	private void saveDefaultAttributeValues(final Collection defaultAttributeValues, final Map newAttributeCodes) {
		final List records = new ArrayList();
		final List templateAttributeIds = new ArrayList();
		Iterator i = defaultAttributeValues.iterator();
		while (i.hasNext()) {
			DefaultAttributeValue dav = (DefaultAttributeValue)i.next();
			if (!newAttributeCodes.containsKey(dav.getAttributeId().getId())) {
				i.remove();
			} else if (dav.getValue() instanceof Collection) {
				Iterator j = ((Collection)dav.getValue()).iterator();
				while (j.hasNext()) {
					records.add(j.next());
					templateAttributeIds.add(dav.getAttributeId());
				}
			} else {
				records.add(dav.getValue());
				templateAttributeIds.add(dav.getAttributeId());
			}
		}		
		
		// (2010/03) POSGRE
		/* OLD:
		String sql = "insert into default_attribute_value (" +
			"default_attribute_value_id" +
			", template_attr_id" +
			", number_value" +
			", string_value" +
			", date_value" +
			", value_id" +
			", long_binary_value" +
		") values (" +
			"seq_system_id.nextval" +
			", ?" +	// 1	template_attr_id
			", ?" + // 2	number_value			
			", ?" + // 3	string_value
			", ?" + // 4	date_value
			", ?" + // 5 	value_id
			", ?" + // 6	long_binary_value
		")";
		*/
		String sql = "insert into default_attribute_value ( \n" +
			"  default_attribute_value_id \n" +
			"  , template_attr_id \n" +
			"  , number_value \n" +
			"  , string_value \n" +
			"  , date_value \n" +
			"  , value_id \n" +
			"  , long_binary_value \n" +
		") values ( \n" +
			"  nextval('seq_system_id') \n" +
			"  , ? \n" +	// 1	template_attr_id
			"  , ? \n" + // 2	number_value			
			"  , ? \n" + // 3	string_value
			"  , ? \n" + // 4	date_value
			"  , ? \n" + // 5 	value_id
			"  , ? \n" + // 6	long_binary_value
			")"
		;
		
		BatchPreparedStatementSetter pss = new BatchPreparedStatementSetter() {
			public int getBatchSize() {
				return records.size();
			}

			public void setValues(PreparedStatement stmt, int index) throws SQLException {
				Object rec = records.get(index);
				ObjectId attributeId = (ObjectId)templateAttributeIds.get(index);
				Class clazz = attributeId.getType();
				stmt.setLong(1, ((Long)newAttributeCodes.get(attributeId.getId())).longValue());
				
				if (clazz.isAssignableFrom(DateAttribute.class)) {
					java.sql.Date dt = new java.sql.Date(((Date)rec).getTime());
					stmt.setNull(2, Types.DECIMAL);
					stmt.setNull(3, Types.VARCHAR);
					stmt.setDate(4, dt);
					stmt.setNull(5, Types.DECIMAL);
					stmt.setNull(6, Types.BINARY);
				} else if (clazz.isAssignableFrom(StringAttribute.class) || clazz.isAssignableFrom(TextAttribute.class)) {
					stmt.setNull(2, Types.DECIMAL);
					stmt.setString(3, (String)rec);
					stmt.setNull(4, Types.DATE);
					stmt.setNull(5, Types.DECIMAL);
					stmt.setNull(6, Types.BINARY);					
				} else if (clazz.isAssignableFrom(IntegerAttribute.class)) {
					stmt.setInt(2, ((Integer)rec).intValue());
					stmt.setNull(3, Types.VARCHAR);
					stmt.setNull(4, Types.DATE);
					stmt.setNull(5, Types.DECIMAL);
					stmt.setNull(6, Types.BINARY);
				} else if (clazz.isAssignableFrom(CardLinkAttribute.class)) {
					// Card c = (Card)rec;
					ObjectId id = (ObjectId) rec; 
					// stmt.setLong(2, ((Long)c.getId().getId()).longValue());
					stmt.setLong(2, ((Long)id.getId()).longValue());
					stmt.setNull(3, Types.VARCHAR);
					stmt.setNull(4, Types.DATE);
					stmt.setNull(5, Types.DECIMAL);
					stmt.setNull(6, Types.BINARY);
				} else if (clazz.isAssignableFrom(TreeAttribute.class) || clazz.isAssignableFrom(ListAttribute.class)) {
					ReferenceValue ref = (ReferenceValue)rec;
					stmt.setNull(2, Types.DECIMAL);
					stmt.setNull(3, Types.VARCHAR);
					stmt.setNull(4, Types.DATE);
					stmt.setLong(5, ((Long)ref.getId().getId()).longValue());
					stmt.setNull(6, Types.BINARY);
				} else if (clazz.isAssignableFrom(HtmlAttribute.class)) {
					stmt.setNull(2, Types.DECIMAL);
					stmt.setNull(3, Types.VARCHAR);
					stmt.setNull(4, Types.DATE);
					stmt.setNull(5, Types.DECIMAL);
					try {
						stmt.setBytes(6, ((String)rec).getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						logger.error(e);
					}
				} else if (clazz.isAssignableFrom(PersonAttribute.class)) {
					Person p = (Person)rec;
					stmt.setLong(2, ((Long)p.getId().getId()).longValue());
					stmt.setNull(3, Types.VARCHAR);
					stmt.setNull(4, Types.DATE);
					stmt.setNull(5, Types.DECIMAL);
					stmt.setNull(6, Types.BINARY);
				} else if (clazz.isAssignableFrom(SecurityAttribute.class)) {
					AccessListItem item = (AccessListItem)rec;
					if (item.getPerson() != null) {
						stmt.setLong(2, ((Long)item.getPerson().getId().getId()).longValue());
					} else {
						stmt.setNull(2, Types.DECIMAL);	
					}
					if (item.getRoleType() != null) {
						stmt.setString(3, item.getRoleType());
					} else {
						stmt.setNull(3, Types.VARCHAR);	
					}
					stmt.setNull(4, Types.DATE);
					if (item.getDepartment() != null) {
						stmt.setLong(5, ((Long)item.getDepartment().getId().getId()).longValue());
					} else {
						stmt.setNull(5, Types.DECIMAL);
					}
					stmt.setNull(6, Types.BINARY);
				}
			}
		};
		getJdbcTemplate().batchUpdate(sql, pss);
	}
	
    //TODO Move all such checks to administrative portlet
    /*
	public void validateCardAccess(CardAccess ca) throws DataException {
		Class clazz = null;
		
		if (CardAccess.WORKFLOW_MOVE.equals(ca.getPermissionType())) {
			clazz = WorkflowMove.class;
		} else if (CardAccess.EDIT_CARD.equals(ca.getPermissionType()) || CardAccess.READ_CARD.equals(ca.getPermissionType())) {
			clazz = CardState.class;
		} else if (CardAccess.CREATE_CARD.equals(ca.getPermissionType())) {
			if (ca.getObjectId() != null) {
				throw new DataException("store.cardaccess.wrong.objectid", 
					new Object[] {
						ca.getObjectId(),
						ca.getPermissionType()
					});
			}
			clazz = null;
		} else {
			throw new DataException("store.cardaccess.wrong.permissiontype", new Object[] { ca.getPermissionType() });				
		}
		
		if (clazz != null) {
			if (ca.getObjectId() == null) {
				throw new DataException("store.cardaccess.wrong.objectid", 
					new Object[] {
						"null",
						ca.getPermissionType()
					});				
			}
			checkClass(clazz, ca.getObjectId().getType());
			
		}
		
		checkClass(Template.class, ca.getTemplateId().getType());
		
		if (ca.getRoleId() != null) {
			checkClass(SystemRole.class, ca.getRoleId().getType());
		}
		
		if (ca.getPersonAttributeId() != null) {
			checkClass(PersonAttribute.class, ca.getPersonAttributeId().getType());
		}
	}
	
	private void checkClass(Class expected, Class found) throws DataException {
		if (!expected.equals(found)) {
			throw new DataException(
				"store.cardaccess.wrong.class",
				new Object[] {
					expected.getCanonicalName(),
					found.getCanonicalName()
				}
			);
		}
	} */
}
