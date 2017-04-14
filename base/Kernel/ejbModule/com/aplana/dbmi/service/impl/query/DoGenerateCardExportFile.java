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

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.GenerateCardExportFile.ExportType;
import com.aplana.dbmi.action.ImportAttribute;
import com.aplana.dbmi.action.GenerateCardExportFile;
import com.aplana.dbmi.action.ImportAttribute.CUSTOM_ATTRIBUTE_CODES;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.LongAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/**
 * Csv export file generation query
 * 
 */
public class DoGenerateCardExportFile extends ActionQueryBase {

	private static final long serialVersionUID = 1L;
	
	private ExportType exportType;

	@Override
	public Object processQuery() throws DataException {
		GenerateCardExportFile exportAction = getAction();
		
		final Search search = exportAction.getSearch();
		List<ImportAttribute> importAttributes = exportAction.getImportAttributes();
		exportType = exportAction.getExportType();
		if(exportType == null)
			throw new DataException("Export type must be provided");
		String sqlFilter = exportAction.getSqlFilter();
		
		List resultlines = null;
		
		final List<AbstractColumnDesc> descriptions = buildColumnDescriptions(importAttributes);
		String sql = buildSql(search, sqlFilter, descriptions);
			
		resultlines = getJdbcTemplate().query(sql, new RowMapper(){
	
			@Override
			public String mapRow(ResultSet rs, int index) throws SQLException {
				StringBuilder sb = new StringBuilder();
				for(int i=0; i<descriptions.size(); i++) {
					AbstractColumnDesc desc = descriptions.get(i);
					if(!desc.isMultiValued()) {
						String resCol = rs.getString("res_"+desc.getAlias());
						if(resCol != null && !resCol.equalsIgnoreCase("null"))
							sb.append(resCol);
					} else {
						Array resColumnArray = rs.getArray("res_"+desc.getAlias());
						if(resColumnArray != null) {
							String[] resColumn = (String[])resColumnArray.getArray();
							if(resColumn != null) {
								if(resColumn.length > 1) {
									for(String s : resColumn) {
										sb.append(GenerateCardExportFile.MULTIATTR_START);
										sb.append(s);
										sb.append(GenerateCardExportFile.MULTIATTR_END);
									}
								} else if(resColumn.length == 1
										&& resColumn[0] != null
										&& !resColumn[0].equalsIgnoreCase("null"))
									sb.append(resColumn[0]);
							}
						}
					}
					sb.append(GenerateCardExportFile.FILE_ATTRIBUTE_DELIMETER);
				}
				return sb.toString();
			}
			
		});
		return resultlines;
	}
	
	/**
	 * Builds column descriptions by provided import attributes
	 * @param attrs import attributes
	 * @return
	 */
	private List<AbstractColumnDesc> buildColumnDescriptions(List<ImportAttribute> attrs) {
		final List<AbstractColumnDesc> columnDescs = new ArrayList<AbstractColumnDesc>();
		for(int i=0; i<attrs.size(); i++) {
			ImportAttribute attr = attrs.get(i);
			String alias = "value_"+i;
			if(attr.getPrimaryCodeId() != null) {
				AttrColumnDesc desc = new AttrColumnDesc(attr);
				ObjectId attrId = attr.getPrimaryCodeId();
				desc.setPrimaryAttr(attrId.getId().toString());
				if(attr.getLinkCodeId() != null) {
					desc.setColumnName(getColumnNameByAttrId(attr.getLinkCodeId()));
					desc.setLinkAttr(attr.getLinkCodeId().getId().toString());
				} else if(attr.getCustomLinkCodeId() != null) {
					desc.setColumnName(attr.getCustomLinkCodeId().getColumnName());
				} else desc.setColumnName(getColumnNameByAttrId(attrId));
				desc.setAlias(alias);
				desc.setImportAttr(attr);
				desc.setMultiValued(isAttrMultiValued(attrId));
				desc.setSql(createGetValueSql(desc));
				columnDescs.add(desc);
			} else if(attr.getCustomPrimaryCodeId() != null) {
				CustomColumnDesc desc = new CustomColumnDesc(attr);
				final CUSTOM_ATTRIBUTE_CODES code = attr.getCustomPrimaryCodeId();
				desc.setAlias(alias);
				desc.setTableName(code.getTableName());
				desc.setImportAttr(attr);
				desc.setSql(createGetValueSql(desc));
				columnDescs.add(desc);
			}
		}
		return columnDescs;
	}
	
	private boolean isAttrMultiValued(ObjectId attrId) {
		Class type = attrId.getType();
		if(LinkAttribute.class.isAssignableFrom(type)
				|| PersonAttribute.class.equals(type))
			return true;
		return false;
	}
	
	/**
	 * Creates sql part that retrieves the value for a column description
	 * @param colDesc column description
	 * @return
	 */
	private String createGetValueSql(AbstractColumnDesc colDesc) {
		final StringBuilder sql = new StringBuilder();
		ImportAttribute attr = colDesc.getImportAttr();
		if(colDesc instanceof AttrColumnDesc) {
			AttrColumnDesc desc = (AttrColumnDesc)colDesc;
			sql.append(" SELECT ");
			if(desc.getLinkAttr() != null) {
				sql.append(" link_av.card_id");
			} else sql.append(" av.card_id");
			sql.append(",");
			if(attr.isReference()) {
				sql.append("vl.").append(desc.getColumnName()).append("::varchar");
			} else if(attr.getCustomLinkCodeId() != null) {
				if(desc.isMultiValued()) {
					sql.append("array_agg(custom_val.").append(desc.getColumnName()).append(")::varchar[]");
				} else sql.append("custom_val.").append(desc.getColumnName()).append("::varchar");
			} else {
				if(desc.isMultiValued()) {
					sql.append("array_agg(av.").append(desc.getColumnName()).append(")::varchar[]");
				} else sql.append("av.").append(desc.getColumnName()).append("::varchar");
			}
			sql.append(" as value");
			
			sql.append("\n FROM attribute_value ");
			if(attr.isReference()) {
				sql.append(" av JOIN card_ids cids on cids.card_id = av.card_id")
					.append(" JOIN values_list vl ON vl.value_id = av.value_id ");
				sql.append(" WHERE av.attribute_code = '")
					.append(desc.getPrimaryAttr()).append("'");
			} else if(desc.getLinkAttr() != null) {
				sql.append(" link_av \n LEFT JOIN attribute_value av ON av.card_id = link_av.number_value AND av.attribute_code = '")
					.append(desc.getLinkAttr()).append("' JOIN card_ids cids on cids.card_id = link_av.card_id WHERE link_av.attribute_code = '")
					.append(desc.getPrimaryAttr()).append("'");
				if(desc.isMultiValued())
					sql.append(" GROUP BY link_av.card_id ");
			} else {
				sql.append(" av JOIN card_ids cids on cids.card_id = av.card_id ");
				if(attr.getCustomLinkCodeId() != null) {
					CUSTOM_ATTRIBUTE_CODES code = attr.getCustomLinkCodeId();
					if(code == CUSTOM_ATTRIBUTE_CODES.LOGIN) {
						sql.append(" LEFT JOIN ").append(code.getTableName()).append(" custom_val ON custom_val.person_id = av.number_value");
					} else if(code == CUSTOM_ATTRIBUTE_CODES.GROUP_CODE
								|| code == CUSTOM_ATTRIBUTE_CODES.GROUP_NAME_RUS
								|| code == CUSTOM_ATTRIBUTE_CODES.GROUP_NAME_ENG) {
						sql.append(" LEFT JOIN person p ON p.person_id = av.number_value");
						sql.append(" LEFT JOIN person_role_group prg ON prg.person_id = p.person_id\n");
						sql.append(" LEFT JOIN ").append(code.getTableName()).append(" custom_val ON custom_val.").append(code.getColumnName()).append(" = prg.").append(code.getColumnName());
					} else if(code == CUSTOM_ATTRIBUTE_CODES.ROLE_CODE
								|| code == CUSTOM_ATTRIBUTE_CODES.ROLE_NAME_RUS
								|| code == CUSTOM_ATTRIBUTE_CODES.ROLE_NAME_ENG) {
						sql.append(" LEFT JOIN person p ON p.person_id = av.number_value");
						sql.append(" LEFT JOIN person_role pr ON pr.person_id = p.person_id");
						sql.append(" LEFT JOIN ").append(code.getTableName()).append(" custom_val ON custom_val.role_code = pr.role_code");
					}
				}
				sql.append(" WHERE av.attribute_code = '").append(desc.getPrimaryAttr()).append("'");
				if(desc.isMultiValued()) {
					sql.append(" GROUP BY av.card_id ");
				}
			}
		} else if(colDesc instanceof CustomColumnDesc) {
			CustomColumnDesc desc = (CustomColumnDesc)colDesc;
			CUSTOM_ATTRIBUTE_CODES code = attr.getCustomPrimaryCodeId();
			sql.append(" SELECT ");
			if(exportType == ExportType.CARDS) {
				sql.append(" p.card_id");
				if(code == CUSTOM_ATTRIBUTE_CODES.LOGIN) {
					sql.append(", p.").append(code.getColumnName()).append(" as value FROM ").append(code.getTableName()).append(" p ");
				} else if(code == CUSTOM_ATTRIBUTE_CODES.GROUP_CODE
						|| code == CUSTOM_ATTRIBUTE_CODES.GROUP_NAME_RUS
						|| code == CUSTOM_ATTRIBUTE_CODES.GROUP_NAME_ENG) {
					desc.setMultiValued(true);
					sql.append(", array_agg(prg.").append(code.getColumnName()).append(") as value FROM person p ");
					sql.append(" LEFT JOIN person_role_group prg ON prg.person_id = p.person_id\n");
					sql.append(" LEFT JOIN ").append(code.getTableName()).append(" sg ON sg.").append(code.getColumnName()).append(" = prg.").append(code.getColumnName());
				} else if(code == CUSTOM_ATTRIBUTE_CODES.ROLE_CODE
						|| code == CUSTOM_ATTRIBUTE_CODES.ROLE_NAME_RUS
						|| code == CUSTOM_ATTRIBUTE_CODES.ROLE_NAME_ENG) {
					desc.setMultiValued(true);
					sql.append(", array_agg(r.").append(code.getColumnName()).append(") as value FROM person p ");
					sql.append(" LEFT JOIN person_role pr ON pr.person_id = p.person_id");
					sql.append(" LEFT JOIN ").append(code.getTableName()).append(" r ON r.role_code = pr.role_code");
				}
				sql.append("\n JOIN card_ids cids ON cids.card_id = p.card_id");
				if(desc.isMultiValued()) {
					 sql.append(" GROUP BY p.card_id");
				}
			}
		}
		return sql.toString();
	}
	
	private String buildSql(Search search, String sqlFilter, List<AbstractColumnDesc> columnDescs) {
		
		final StringBuilder sql = new StringBuilder();
		final String templatesStr = ObjectIdUtils.numericIdsToCommaDelimitedString(search.getTemplates());
		final String statesStr = ObjectIdUtils.numericIdsToCommaDelimitedString(search.getStates());
		
		if(exportType == ExportType.CARDS) {
			sql.append(" WITH card_ids as (").append("\nSELECT c.card_id from card c");
			if(templatesStr != null && !templatesStr.equals("")) {
				sql.append(" WHERE c.template_id in (").append(templatesStr).append(") ");
				if(statesStr != null && !statesStr.equals(""))
					sql.append(" AND c.status_id in (").append(statesStr).append(") ");
			} else if(statesStr != null && !statesStr.equals("")) {
				sql.append(" WHERE c.status_id in (").append(statesStr).append(") ");
			}
			sql.append(")\n");
			
			sql.append(" SELECT ");
			for(int i=0; i < columnDescs.size(); i++) {
				AbstractColumnDesc desc = columnDescs.get(i);
				if(i > 0)
					sql.append("\n,");
				sql.append(desc.getAlias()).append(".value as res_").append(desc.getAlias());
			}
			sql.append("\n");
			sql.append(" FROM card_ids c ");
			for(int i=0; i<columnDescs.size(); i++) {
				AbstractColumnDesc desc = columnDescs.get(i);
				sql.append("\nLEFT JOIN (");
					sql.append(desc.getSql());
				sql.append(") as ").append(desc.getAlias())
					.append(" ON c.card_id = ").append(desc.getAlias()).append(".card_id ");
			}
		}
		
		return sql.toString();
	}
	
	private String getColumnNameByAttrId(ObjectId attrId) {
		final Class attrType = attrId.getType();
		if(StringAttribute.class.isAssignableFrom(attrType)) {
			return "string_value";
		} else if(DateAttribute.class.equals(attrType)) {
			return "date_value";
		} else if(LinkAttribute.class.isAssignableFrom(attrType)
				|| IntegerAttribute.class.equals(attrType)
				|| LongAttribute.class.equals(attrType)
				|| PersonAttribute.class.equals(attrType)) {
			return "number_value";
		} else if(ReferenceAttribute.class.isAssignableFrom(attrType)) {
			return "value_rus";
		}
		return null;
	}
	
	/**
	 * Column description for custom tables
	 */
	private static class CustomColumnDesc extends AbstractColumnDesc {
		
		private String tableName;
		
		public CustomColumnDesc(ImportAttribute importAttr) {
			setImportAttr(importAttr);
		}
		
		public String getTableName() {
			return tableName;
		}

		public void setTableName(String tableName) {
			this.tableName = tableName;
		}
	}
	
	/**
	 * Abstract column description
	 */
	private static abstract class AbstractColumnDesc {
		private String columnName;
		private String alias;
		private String sql;
		private ImportAttribute importAttr;
		private boolean multiValued;
		
		public String getColumnName() {
			return columnName;
		}
		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}
		public String getAlias() {
			return alias;
		}
		public void setAlias(String alias) {
			this.alias = alias;
		}
		public String getSql() {
			return sql;
		}

		public void setSql(String sql) {
			this.sql = sql;
		}
		public ImportAttribute getImportAttr() {
			return importAttr;
		}
		public void setImportAttr(ImportAttribute importAttr) {
			this.importAttr = importAttr;
		}
		public boolean isMultiValued() {
			return multiValued;
		}
		public void setMultiValued(boolean multiValued) {
			this.multiValued = multiValued;
		}
	}
	
	/**
	 * A column description for attribute value
	 */
	private static class AttrColumnDesc extends AbstractColumnDesc {
		
		private String primaryAttr;
		private String linkAttr;
		
		public AttrColumnDesc(ImportAttribute importAttr) {
			setImportAttr(importAttr);
		}
		
		public String getPrimaryAttr() {
			return primaryAttr;
		}
		public void setPrimaryAttr(String primaryAttr) {
			this.primaryAttr = primaryAttr;
		}
		public String getLinkAttr() {
			return linkAttr;
		}
		public void setLinkAttr(String linkAttr) {
			this.linkAttr = linkAttr;
		}

	}
	
}
