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
package com.aplana.dbmi.jbr.processors.docgraph;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.entdb.ManagerTempTables;

/**
 * @author RAbdullin
 * �������� ����������� ��� ��������� ������� ����������.
 * ��������� ����������� ������� ��������� � �����.
 * ��������, ��� ������������ ���� ���������, ����� ���� �� ��������� ����������, 
 * ����� ������� ���������, ���������� ��� ������� ��������� ����������.
 * �������� copyToDestinations ��������� ������ �������� � �������, � ������� 
 * ��� ������������. � ������ ������� �� ���� ������� ����� ����������� ����������,
 * ����������� ��� �������� ������. ������ ������� ��������� copyToDestinations:
 * <br>"ID_�������1:ID_��������1, ID_��������2; ID_�������2:ID_�������1, ID_�������3"</br>
 */
public class DynaRoles extends GraphProcessorBase {

	private static final long serialVersionUID = 1L;

	/**
	 * ������ ��������� ��� ����� ������ (U-, C-, E- ��������) ������ ��������.
	 */
	private Set<ObjectId> srcAttrIds;

	/**
	 * ������� ��� ���������� ������������� ������ ������.
	 */
	private ObjectId dstAttrId;

	/**
	 * ������ ��������� ��� ����� ������ (U-, C-, E- ��������) ������ ��������.
	 */
	private List<ObjectId> dstCopyToAttrIds = new ArrayList<ObjectId>();
	
	private List<Destination> destinations;

	private final static String TMPTABLEID_ATTR_CARD = "gtemp_attr_card_";
	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#process()
	 */
	@SuppressWarnings("unused")
	@Override
	public boolean processNode() throws DataException {

//TODO ������� �������� ��������� ���������� !		
		if (this.getCurNodeId() == null) {
			logger.debug(" current node (card) id is null -> exiting ");
			return true;
		}
		if (this.dstAttrId == null) {
			logger.debug(" destination attribute is null -> exiting ");
			return true;
		}
		logger.debug("processing card " + getCurNodeId().getId() 
				+ " into attribute " + dstAttrId );

		// ������� "������������" ���� ������������ � �������� �����...

		final Set<ObjectId> allNodes = new HashSet<ObjectId>();
		// allNodes.clear();

		// �������� � ���� ������ - ����, ���� ����� � ���������...
		//ObjectIdUtils.fillObjectIdSetFromCollection(allNodes, this.getCurNodeId());
		if (this.getCurNodeId() != null)
			allNodes.add(this.getCurNodeId());
		if (this.getAllChildNodes() != null)
			allNodes.addAll(this.getAllChildNodes());
		if (this.getAllParentNodes() != null)
			allNodes.addAll(this.getAllParentNodes());

		// ����� ��� ����������...
		final String sAllCardIds = ObjectIdUtils.numericIdsToCommaDelimitedString(allNodes); // IdUtils.makeIdCodesEnum(allNodes);
		final String sAttrCodes = IdUtils.makeIdCodesQuotedEnum(this.srcAttrIds);
		
		ManagerTempTables managerTempTables = new ManagerTempTables(getJdbcTemplate());
		String tableName = (TMPTABLEID_ATTR_CARD+getUUID()).toUpperCase();
		try{
			managerTempTables.regTmpTable(tableName, getSqlCreateTable(tableName));	
			logger.info("Reg temp table: "+tableName);
			managerTempTables.start(new String[]{tableName});		

			logger.trace( "Summarizing persons ["+ sAttrCodes +"] on cards ["+ sAllCardIds +"] ... ");

			// ������� id-������ ��� id-������������ �������� ��� mixed (U,C)-���������
			String sql = "SELECT DISTINCT \n"
				+"	src.dest_card_id, \n"
				+"	src.dest_attr_code, \n"
				// � ����������� �� ���� �������� �������� �������� ��� ������: 
				// person_id ��� card_id �������...
				+"	(CASE \n"
				+"		WHEN 'U'=(select a.data_type from attribute a where a.attribute_code=src.dest_attr_code) \n" 
				+"			THEN src.person_id \n"
				+"			ELSE src.person_card_id \n"
				+"	END) as dst_number_value \n"
				+"FROM ( \n"
				+"	SELECT \n"	
				+ "		c.card_id dest_card_id,"
				+ "		CAST( ? as VARCHAR) as dest_attr_code, -- (:dst_attr_code) \n" 
				+ "		-- person_id \n"
				+"		( CASE \n" 
				+"			WHEN a.data_type = 'U' THEN av.number_value \n"
				+"			ELSE (select p.person_id from person p where p.card_id = av.number_value) \n"
				+"		  END ) as person_id, \n"
				+ "\n"
				+"		-- card_id \n"
				+"		( CASE \n"
				+"			WHEN a.data_type = 'U' THEN (select p.card_id from person p where p.person_id = av.number_value) \n"
				+"			ELSE av.number_value \n"
				+"		  END ) as person_card_id \n"
				+"	FROM attribute_value av \n"
				+"		JOIN attribute a on a.attribute_code=av.attribute_code \n"
				+"				and a.data_type in ('U', 'C') \n "

				// 		id �������� �������� (:card_ids) 
				+"				and  av.card_id in ("+ sAllCardIds +") \n"

				// 		�������� �������� ((:attr_codes))...
				+"				and av.attribute_code in ("+ sAttrCodes+") \n" 
				+"		JOIN card c ON c.card_id IN ("+sAllCardIds+") \n"

				+") src \n";
		
			StringBuffer destinationsSql = new StringBuffer();
			Set<ObjectId> lockedCards = new HashSet<ObjectId>(allNodes.size());
			try {
				for (ObjectId objId : allNodes) {
					execAction(new LockObject(objId));
					lockedCards.add(objId);
				}
				int count_temp = getJdbcTemplate().update( 
					"INSERT INTO "+tableName +" (card_id, attribute_code, number_value) \n" +
				sql, 
				new Object[] { this.dstAttrId.getId()}, 
				new int[] { Types.VARCHAR } );
		
				CardUtils.dropAttributes(getJdbcTemplate(), new Object[]{dstAttrId}, allNodes);
		
				int count_result = getJdbcTemplate().update( 
					"INSERT INTO attribute_value (card_id, attribute_code, number_value) \n" +
					"SELECT DISTINCT t.card_id, t.attribute_code, t.number_value FROM "+tableName+" as t");
		
				logger.debug( "Summarized "+ count_result +" items of attributes ["+ sAttrCodes +"] under cards ["+ sAllCardIds +"]");
		
				destinationsSql = new StringBuffer();
				for (Iterator<Destination> i = destinations.iterator(); i.hasNext(); ) {
					Destination destination = i.next();
					destinationsSql.append("( ta.attribute_code in ( ");
					destinationsSql.append(IdUtils.makeIdCodesQuotedEnum(destination.getAttrIds()));
					destinationsSql.append(" ) AND ta.template_id=");
					destinationsSql.append(destination.getTemplateId().getId());
					destinationsSql.append(" )");
					if (i.hasNext())
						destinationsSql.append(" OR ");
					dstCopyToAttrIds.addAll(destination.getAttrIds());
				}
				if (getCopyToNodes().isEmpty() || dstCopyToAttrIds.isEmpty()){			
					return true;
				}
			} finally {
				for (ObjectId objId : lockedCards) {
					execAction(new UnlockObject(objId));
				}
			}
	
		
			final int clear_count = getJdbcTemplate().update("DELETE FROM "+tableName); 
		
			final String cardIds = ObjectIdUtils.numericIdsToCommaDelimitedString(getCopyToNodes());
			lockedCards.clear();
			try {
				for (ObjectId objId : getCopyToNodes()) {
					execAction(new LockObject(objId));
					lockedCards.add(objId);
				}
				sql = "INSERT INTO "+ tableName +" (card_id, attribute_code, number_value) \n" +
				"( " +
				"	SELECT c.card_id, ta.attribute_code, av.number_value \n" +
				"	FROM card c \n" +
				"		JOIN template_attribute ta ON ta.template_id=c.template_id \n" +
				"		JOIN attribute_value av ON  av.card_id=? AND av.attribute_code=? \n" +
				"	WHERE c.card_id IN ("+ cardIds + ") \n" +
				"		  AND ( \n" + destinationsSql +	"\n      )\n" +
				")"; 
				int count_temp = getJdbcTemplate().update(sql, 
				new Object[] { 
						this.getCurNodeId().getId(), 
						this.dstAttrId.getId() 
					}, 
				new int[] { Types.NUMERIC, Types.VARCHAR } );
		
				CardUtils.dropAttributes(getJdbcTemplate(), dstCopyToAttrIds.toArray(), getCopyToNodes());
		
				int count_result = getJdbcTemplate().update( 
					"INSERT INTO attribute_value (card_id, attribute_code, number_value) \n" +
					"SELECT DISTINCT t.card_id, t.attribute_code, t.number_value FROM "+tableName+" as t");
		
		
				logger.debug(count_result + " values of attribute '" + this.dstAttrId.getId() 
					+ "' from card "+ this.getCurNodeId().getId()
					+ " inserted into the cards list " + cardIds);

				return true; // ���������� ��������� �����
			} finally {
				for (ObjectId objId : lockedCards) {
					execAction(new UnlockObject(objId));
				}
			}
		} catch (DataException e) {
			logger.error("Error copying attributes processor: "+getClass().getName()+" processor !", e);
			throw e;
		} catch (Exception e) {	
			logger.error("Error copying attributes processor: "+getClass().getName()+" processor !", e);
			throw new DataException(e);
		} finally {
			if (managerTempTables.isStarted()){
				managerTempTables.close();
				logger.info("Drop temp table: "+tableName);
			}
		} 
	}
	
	private String getUUID(){
		return java.util.UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	private String getSqlCreateTable(String tableName){		  
		StringBuilder sql =	new StringBuilder();
		sql.append("CREATE TEMPORARY TABLE "+ tableName +" ( \n");
		sql.append("	card_id numeric(9), \n");
		sql.append("	attribute_code character varying(20), \n");
		sql.append("	number_value numeric, \n");
		sql.append("	ordnum serial NOT NULL PRIMARY KEY \n");
		sql.append(") ON COMMIT DROP;\n");		
		return sql.toString();
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.processors.ProcessCard#setParameter(java.lang.String, java.lang.String)
	 */
	@Override
	public void setParameter(String name, String value) {
		if (name == null || "".equals(name)) return;
		super.setParameter(name, value);

		if ("srcAttrIds".equalsIgnoreCase(name)) {
			final List<ObjectId> list = IdUtils.stringToAttrIds(value, PersonAttribute.class); //  CardLinkAttribute.class 
			this.srcAttrIds = (list == null || list.isEmpty()) 
						? null 
						: new HashSet<ObjectId>(list);
		} else if ("dstAttrId".equalsIgnoreCase(name)) {
			this.dstAttrId = (value == null || "".equals(value)) 
						? null 
						: IdUtils.tryFindPredefinedObjectId(value, PersonAttribute.class); 
		} else if ("copyToDestinations".equalsIgnoreCase(name)) {
			destinations = parseDestinations(value);
		} else {
			logger.warn( "Unknown parameter: '"+ name + "'='"+ value + "' -> ignored");
		}
	}
	
	private List<Destination> parseDestinations(final String destinationsString){
		List<Destination> result = new ArrayList<Destination>();
		String[] dests = destinationsString.split(";");
		for (String dest : dests) {
			String[] destComps = dest.split(":");
			if (destComps.length != 2)
				continue;
			final ObjectId templateId = 
				IdUtils.tryFindPredefinedObjectId(destComps[0].trim(), Template.class);
			final List<ObjectId> attrIdsList = 
				IdUtils.stringToAttrIds(destComps[1].trim(), PersonAttribute.class);
			result.add(new Destination(templateId, attrIdsList));
		}
		return result;
	}

	class Destination{
		protected ObjectId templateId;
		protected List<ObjectId> AttrIds;

		Destination(ObjectId templateId, List<ObjectId> attrIds) {
			this.templateId = templateId;
			AttrIds = attrIds;
		}

		public ObjectId getTemplateId() {
			return templateId;
		}
		public List<ObjectId> getAttrIds() {
			return AttrIds;
		}
	}
}