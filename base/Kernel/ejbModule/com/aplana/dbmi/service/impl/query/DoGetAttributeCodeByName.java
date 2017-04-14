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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.GetAttributeCodeByName;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoGetAttributeCodeByName extends ActionQueryBase {

	public Object processQuery() throws DataException {
		GetAttributeCodeByName action = (GetAttributeCodeByName)getAction();
		logger.debug(action);
		
		ObjectId templateId = (ObjectId)action.getTemplateId();
		if (templateId==null||templateId.getId()==null){
			throw new DataException("card.import.input.template.empty");
		}
		List attrNames = action.getAttrNames();
		if (attrNames==null||attrNames.isEmpty()){
			throw new DataException("card.import.attribute.names.is.empty", new Object[]{templateId.getId()});
		}
		boolean isRusLang = action.isRusLang();
		boolean isCodes = action.isCodes();
		long manyAttributesType = action.getManyAttributesType();
		
		final String defaultSql = 
			"select \n"+
			"	a.attribute_code \n" +
			"from \n" +
			"	attribute a \n" +
			"where " +
			(isCodes?
					"	a.attribute_code=? \n":
					"	"+((isRusLang==true)?"a.attr_name_rus":"a.attr_name_eng")+"=? \n") +
			"order by a.attribute_code asc";

		final String sql = 
			"select \n"+
			"	ta.attribute_code, \n" +
			"	a.data_type \n" +
			"from \n" +
			"	template_attribute ta \n" +
			"	join attribute a \n" +
			"		on a.attribute_code = ta.attribute_code \n" +
			"where " +
			"	ta.template_id = ? \n" +
			(isCodes?
					"	and a.attribute_code=? \n":
					"	and "+((isRusLang==true)?"a.attr_name_rus":"a.attr_name_eng")+"=? \n") +
			"order by ta.template_attr_id asc";
		List<ObjectId> attributeCodes = new ArrayList<ObjectId>();
		for( Iterator i = attrNames.iterator(); i.hasNext(); ) {
			String attrName = (String)i.next();
			// сначала проверяем, чтобы атрибуты вообще существовали в системе 
			List<String> sAttributes = getJdbcTemplate().query(defaultSql,
					new Object[] { attrName},
					new int[] { Types.VARCHAR },
						new RowMapper() {
							public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
								String attrItem = rs.getString(1);
								return attrItem;
							}
						});
			if (sAttributes==null||sAttributes.isEmpty()){
				throw new DataException("card.import.atribute.not.exist", new Object[] {attrName});
			}
			// потом, чтобы они были привязаны к шаблону
			List<ObjectId> attributes = getJdbcTemplate().query(sql,
					new Object[] { templateId.getId(), attrName},
					new int[] { Types.NUMERIC, Types.VARCHAR },
						new RowMapper() {
							public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
								Attribute attrItem = AttributeTypes.createAttribute(rs.getString(2), rs.getString(1));
								return attrItem.getId();
							}
						});
			if (attributes==null||attributes.isEmpty()){
				throw new DataException("card.import.atribute.not.in.template", new Object[] { 
						attrName, templateId.getId()
				});
			}
			if (attributes.size()>1&&manyAttributesType==GetAttributeCodeByName.MANY_ATTRIBUTE_CODE_ERROR){
				throw new DataException("card.import.many.count.attribute.error", new Object[] { 
						attrName, templateId.getId()
				});
			}
			if (attributes.size()>1&&manyAttributesType==GetAttributeCodeByName.MANY_ATTRIBUTE_CODE_GET_FIRST){
				logger.warn(MessageFormat.format("Attribute with name ''{0}'' exists in template {1} more 1 count. Take first of them.", new Object[] { 
						attrName, templateId.getId()
				}));
			}
			
			attributeCodes.add(attributes.get(0));
		}
		return attributeCodes;
	}

}
