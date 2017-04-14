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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.springframework.jdbc.core.RowMapper;
import com.aplana.dbmi.action.ImportAttribute;
import com.aplana.dbmi.action.ImportResult;
import com.aplana.dbmi.action.ImportObjects;
import com.aplana.dbmi.action.ImportAttribute.CUSTOM_ATTRIBUTE_CODES;
import com.aplana.dbmi.action.ImportResult.IMPORT_RESULT_TYPE;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.UserData;

/**
 * ������ (��������� ���������� ��� �������) ������������ �� csv ��������
 * @author PPanichev
 * 
 */

public abstract class DoImportObjects extends ActionQueryBase implements WriteQuery {

	private static final long serialVersionUID = 1L;
	private ImportObjects action;
	private String typeObject;
	private Class<? extends DataObject> clazz;

	public Object processQuery() throws DataException {
		action = (ImportObjects)getAction();
		//logger.debug(action);
		
		List<ImportAttribute> importAttributes = action.getImportAttributes();
		boolean checkDoublets = action.isCheckDoublets();
		boolean updateDoublets = action.isUpdateDoublets();
		int lineNumber = action.getLineNumber();
		
		if (importAttributes==null||importAttributes.isEmpty()){
			throw new DataException("card.import.attribute.codes.is.empty");
		}
		
		ImportAttribute attr = importAttributes.get(0);
		if (attr==null||attr.getValue()==null){
			throw new DataException("import.input.name.table.empty");
		}
		
		typeObject = action.getTypeImportObject().name();
		clazz = action.getClassImportObject();
		
		String codeObject = attr.getValue();
		ObjectId objectId = new ObjectId(clazz, codeObject);

		String objectName = MessageFormat.format(ContextProvider.getContext().getLocaleMessage("import.success.object.name.default"), new Object[]{codeObject});
		if (action.getCustomImportObjectName()!=null){
			objectName = action.getCustomImportObjectName();
		}
		ImportResult result = new ImportResult();
		result.setImportAttributes(importAttributes); 
		List<ObjectId> doubletObjectIds = new ArrayList<ObjectId>(); 
		try{
			if (logger.isDebugEnabled()){
				logger.debug(MessageFormat.format("Import " + typeObject + " {0} in line {1}", new Object[]{importAttributes, lineNumber}));
			}
			// ����� ������������ � �������������� �������� �� ��������� ������, ����������� ������ ���������� ���������
			List<ImportAttribute> notMandatoryAttributes = new ArrayList<ImportAttribute>();
			List<ImportAttribute> troubleAttributes = getTroubleAttributes(importAttributes);
			notMandatoryAttributes.addAll(importAttributes);
			List<ImportAttribute> mandatoryAttributes = getMandatoryForDoubletCheckAttributes(importAttributes);
			notMandatoryAttributes.removeAll(mandatoryAttributes);
			
			// �� ������ ����� ������� ������� ��� �������� ��������� �������� �������� ������� � ������ ���������� � ���������� ��� ���������� ���������
			if (troubleAttributes!=null&&!troubleAttributes.isEmpty()){
				throw new DataException(generateTroubleMessage(troubleAttributes));				
			}
			// ��������� �� ��������� ��� ������������� (������������� � ������ ������ - ��� ������� �������� �� ��������� ��� ������� ���������� ����������)
			if (checkDoublets||updateDoublets){
				doubletObjectIds = findDoubletsObjects(mandatoryAttributes);
				if (doubletObjectIds!=null&&doubletObjectIds.size()>0){
					for(ObjectId doubletObjectId :doubletObjectIds){
					// ���� �������� ������
						if (doubletObjectId!=null){
							if (logger.isDebugEnabled()){
								logger.debug(MessageFormat.format("Found doublet " + typeObject + " {0} for line {1}", new Object[]{doubletObjectId.getId().toString(), lineNumber}));
							}
							// ���� ����������, �� � ��������� ��������� �������������� ��������
							if (updateDoublets){
								updateObject(doubletObjectId, notMandatoryAttributes);
								if (logger.isDebugEnabled()){
									logger.debug(MessageFormat.format("Updated doublet " + typeObject + " {0} for line {1}", new Object[]{doubletObjectId.getId(), lineNumber}));
								}
							} 
						}
					}
					result.addAllImportObjectIds(doubletObjectIds);
					result.setResultType(IMPORT_RESULT_TYPE.DOUBLET);
					final String doublets = IdUtils.makeObjectIdStringLine(doubletObjectIds, ",");
					result.setResultMessage("\n\t"+MessageFormat.format(ContextProvider.getContext().getLocaleMessage("card.import.doublet.card"), new Object[]{lineNumber, doublets}));
					return result;
				}
			}
			
			// ������� ����� ������ ��������� �������� ���������� ��������
			//SystemRole importRole = saveRole(systemRoleId, getUser(), importAttributes, true);
			DataObject importObject = saveObject(objectId, getUser(), importAttributes, true);
			ObjectId importObjectId = importObject.getId();
			if (logger.isDebugEnabled()) {
				logger.debug(MessageFormat.format("Created new " + typeObject + " {0} for line {1}", new Object[]{importObjectId.getId(), lineNumber}));
			}
			
			List<String> successList = generateSuccessList(importObject);
			StringBuffer str = new StringBuffer();
			for(String s: successList){
				str.append("\n\t"+s);
			}
			result.addImportObjectId(importObjectId);
			result.setResultType(IMPORT_RESULT_TYPE.SUCCESS);
			result.setResultMessage(str.toString());
			return result;
		} catch (Exception e){
			if (logger.isErrorEnabled()){ 
				logger.error(e.getMessage());
			}
			// ��� ����� ��������� � ������� ��������, ���������� ����������
			throw new DataException(e.getMessage());
		}
	}
		
	/**
	 * @param systemRole - ����������� ������
	 * @return ����������� ������
	 * @throws DataException
	 */
	protected abstract DataObject saveObject(ObjectId objId, UserData user,
			List<ImportAttribute> importAttributes, boolean isNew) throws DataException;
	
	private List<ObjectId> findDoubletsObjects(List<ImportAttribute> mandatoryAttributes) {
		
		if (logger.isDebugEnabled()){
			logger.debug(MessageFormat.format("Try find doublets " + typeObject + " with attributes {0}", new Object[]{mandatoryAttributes}));
		}
		if (mandatoryAttributes.isEmpty())
			return null;
		StringBuffer sqlBuf = new StringBuffer(action.getBaseDoubletsSqlForObject());
		sqlBuf.append("where \n");
		
		for (int i=0; i<mandatoryAttributes.size(); i++) {
			ImportAttribute customAttributeId = mandatoryAttributes.get(i);
			sqlBuf.append(MessageFormat.format("sr.{0} = ''{1}'' \n", new Object[]{
					customAttributeId.getCustomPrimaryCodeId().getColumnName(), 
					customAttributeId.getValue()
					}));
			if (i != mandatoryAttributes.size() - 1) {
				sqlBuf.append(" OR \n");
			}
		}
		
		@SuppressWarnings("unchecked")
		List<ObjectId> doubletsObjectsId = getJdbcTemplate().query(sqlBuf.toString(),
				new Object[] { },
				new int[] {  },
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				            return new ObjectId(clazz, rs.getString(1));
						}
					});	
		if (doubletsObjectsId == null || doubletsObjectsId.isEmpty())
			return null;
		if (doubletsObjectsId.size()>1){
			if (logger.isWarnEnabled()){
				logger.warn(MessageFormat.format("Found {0} doublets " + typeObject + " whith card attributes {1}.", new Object[]{doubletsObjectsId.size(), mandatoryAttributes}));
			}
		}
		return doubletsObjectsId;
	}

	/**
	 * ������������ �� ��������� ������ ������������� ��������� ������ ������������ ��� ������ ���������� ���������
	 * @param ImportAttributes - �������� ������ ������������� ���������
	 * @return ������ ������ ������������ ��� ������ ���������
	 */
	private List<ImportAttribute> getMandatoryForDoubletCheckAttributes(List<ImportAttribute> ImportAttributes){
		List<ImportAttribute> resultList = new ArrayList<ImportAttribute>();
		for(ImportAttribute attr: ImportAttributes){
			if (attr.isDoubletCheck()){
				resultList.add(attr);
			}
		}
		return resultList;
	}
	
	/**
	 * ������������ �� ��������� ������ ������������� ��������� ������ ���������� ��������� (���, ��� ������� �������� ������ ��� ��������)
	 * @param ImportAttributes - �������� ������ ������������� ���������
	 * @return ������ ������ ���������� ���������
	 */
	private List<ImportAttribute> getTroubleAttributes(List<ImportAttribute> ImportAttributes){
		List<ImportAttribute> resultList = new ArrayList<ImportAttribute>();
		for(ImportAttribute attr: ImportAttributes){
			if (attr.isTrouble()){
				resultList.add(attr);
			}
		}
		return resultList;
	}
	
	/**
	 * �������� ������ ������� ��������� �� ������� �������
	 * @param doubletObjectId - ������� ������
	 * @param attributes - ������ ����������� ��������� ��������� �� ����������
	 * @throws DataException 
	 */
	private void updateObject(ObjectId doubletObjectId, List<ImportAttribute> attributes) throws DataException {
		if (doubletObjectId==null)
			return;
		if (logger.isDebugEnabled()){
			logger.debug(MessageFormat.format("Update " + typeObject + " {0} by attributes {1}", new Object[]{doubletObjectId.getId(), attributes}));
		}
		try {
			// �������������� ��� ��������, ������� ���������� ������������
			saveObject(doubletObjectId, getUser(), attributes, false);
		} catch (Exception e){
			throw new DataException("import.update.error", new Object[]{doubletObjectId.getId(), e.getMessage()});
		}
	}
	
	/**
	 * ���������� ��������� ������ ��������, ����������� � ������ <[...]> � ������ ���� �� ��������, 
	 * ����������� ������������ ������� �������� (�������� �������) � ��� ������������� �������������� ������� �������� (� ������� ')   
	 * @param value - ������� ������
	 * @param delimChar - ������ ����������
	 * @param escapeChar - ������������ ������
	 * @return
	 */
	private String generateValuesList(String inputValue, String delimChar, String escapeChar){
		String[] values = inputValue.split("]>");
		StringBuffer result = new StringBuffer();
		for(int i=0; i<values.length; i++){ 
			String value = values[i].replaceAll("<\\[", "").replaceAll("]>", "").trim();
			if (value==null||value.isEmpty())
				continue;
			if (i>0){
				result.append(delimChar);
			}
			if (escapeChar!=null&&escapeChar.length()>0){
				result.append(escapeChar);
			}
			result.append(value);
			if (escapeChar!=null&&escapeChar.length()>0){
				result.append(escapeChar);
			}
		}
		return result.toString();
	}
	
	private String generateTroubleMessage(List<ImportAttribute> importAttributes){
		StringBuffer resultMsg = new StringBuffer();
		for (Iterator<ImportAttribute> itr = importAttributes.iterator(); itr.hasNext();) {
			ImportAttribute object = (ImportAttribute)itr.next();
			resultMsg.append(object.getTroubleMessage());
			if (itr.hasNext()){
				resultMsg.append(", ");
			}
		}
		return resultMsg.toString();
	}

	private ArrayList<String> generateSuccessList(DataObject importObject) {
		
		ArrayList<String> result = new ArrayList<String>();
		if(getName() == null) {
			result.add(importObject.getId().toString());
		} else {
			result.add(getName());
		}
		return result;
	}
	
	protected abstract String getName();
	
	private String findGroupByValue(String value, CUSTOM_ATTRIBUTE_CODES customAttributeId) throws DataException{
		if (logger.isDebugEnabled()) {
			logger.debug(MessageFormat.format("Try find system group with {0} equal {1}", new Object[]{customAttributeId.name(), value}));
		}
		
		// ������ ����� ����� ������� sql-������
		StringBuffer sqlBuf = new StringBuffer("select distinct group_code from system_group sg \n");
		sqlBuf.append("where \n");
		sqlBuf.append(MessageFormat.format("sg.{0} = ''{1}''", new Object[]{customAttributeId.name(), value}));
		ArrayList<String> groupCodes = (ArrayList<String>)getJdbcTemplate().query(sqlBuf.toString(), new RowMapper() {
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException
            {
                String group_code = rs.getString(1);
                return group_code;
            }
        });
		if (groupCodes.isEmpty()){
			throw new DataException("card.import.system.group.not.found", new Object[]{customAttributeId.name(), value});
		} else if(groupCodes.size()>1){
			throw new DataException("card.import.system.group.found.more.one", new Object[]{customAttributeId.name(), value});
		}
		return groupCodes.get(0);
	}

	private String findRoleByValue(String value, CUSTOM_ATTRIBUTE_CODES customAttributeId) throws DataException{
		if (logger.isDebugEnabled()){
			logger.debug(MessageFormat.format("Try find system role with {0} equal {1}", new Object[]{customAttributeId.name(), value}));
		}
		
		// ������ ����� ����� ������� sql-������
		StringBuffer sqlBuf = new StringBuffer("select distinct role_code from system_role sr \n");
		sqlBuf.append("where \n");
		sqlBuf.append(MessageFormat.format("sr.{0} = ''{1}''", new Object[]{customAttributeId.name(), value}));
		ArrayList<String> roleCodes = (ArrayList<String>)getJdbcTemplate().query(sqlBuf.toString(), new RowMapper() {
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException
            {
                String role_code = rs.getString(1);
                return role_code;
            }
        });
		if (roleCodes.isEmpty()){
			throw new DataException("card.import.system.role.not.found", new Object[]{customAttributeId.name(), value});
		} else if(roleCodes.size()>1){
			throw new DataException("card.import.system.role.found.more.one", new Object[]{customAttributeId.name(), value});
		}
		return roleCodes.get(0);
	}

	/**
	 * ����� ���������� ��������� ����� �� ������� ������ (����� ����������� ��� ������� ����� ��� �����)
	 */
	private Set<String> findRolesByGroup(String value) throws DataException{
		if (logger.isDebugEnabled()){
			logger.debug(MessageFormat.format("Try find system roles for group {0}", new Object[]{value}));
		}
		if (value == null || value.isEmpty()){
			if (logger.isDebugEnabled()){
				logger.debug(MessageFormat.format("Input group is null, searching break", new Object[]{}));
			}
			return null;
		}
		
		// ������ ����� ����� �������� �����
		ObjectId groupId =  new ObjectId( SystemGroup.class, value);
		ChildrenQueryBase srQuery = getQueryFactory().getChildrenQuery(SystemGroup.class, SystemRole.class);;
		srQuery.setParent(groupId);
		final Collection<SystemRole> roleCodes = (Collection<SystemRole>)getDatabase().executeQuery(getUser(), srQuery);
		
		if (roleCodes.isEmpty()){
			if (logger.isDebugEnabled()){
				logger.debug(MessageFormat.format("Role List for group {0} is empty", new Object[]{value}));
			}
			return null;
		}
		Set<String> result = new HashSet<String>();
		for (SystemRole sr: roleCodes){
			result.add(sr.getId().getId().toString());
		}
		return result;
	}
	
	/**
	 * ����� ���������� ��������� ����� �� ������� ������
	 */
	private Collection<SystemRole> searchRolesByGroup(String value) throws DataException{
		if (logger.isDebugEnabled()){
			logger.debug(MessageFormat.format("Try find system roles for group {0}", new Object[]{value}));
		}
		if (value == null || value.isEmpty()){
			if (logger.isDebugEnabled()){
				logger.debug(MessageFormat.format("Input group is null, searching break", new Object[]{}));
			}
			return null;
		}
		
		// ������ ����� ����� �������� �����
		ObjectId groupId =  new ObjectId( SystemGroup.class, value);
		ChildrenQueryBase srQuery = getQueryFactory().getChildrenQuery(SystemGroup.class, SystemRole.class);;
		srQuery.setParent(groupId);
		final Collection<SystemRole> roleCodes = (Collection<SystemRole>)getDatabase().executeQuery(getUser(), srQuery);
		
		if (roleCodes.isEmpty()){
			if (logger.isDebugEnabled()){
				logger.debug(MessageFormat.format("Role List for group {0} is empty", new Object[]{value}));
			}
			return null;
		}
		return roleCodes;
	}
	
	/**
	 * ����� ���������� ��������� ����� �� ������� ����
	 */
	private Collection<SystemGroup> searchGroupsByRole(String value) throws DataException{
		if (logger.isDebugEnabled()){
			logger.debug(MessageFormat.format("Try find system groups for role {0}", new Object[]{value}));
		}
		if (value == null || value.isEmpty()){
			if (logger.isDebugEnabled()){
				logger.debug(MessageFormat.format("Input role is null, searching break", new Object[]{}));
			}
			return null;
		}
		
		// ������ ����� ����� �������� �����
		ObjectId roleId =  new ObjectId( SystemRole.class, value);
		ChildrenQueryBase srQuery = getQueryFactory().getChildrenQuery(SystemRole.class, SystemGroup.class);;
		srQuery.setParent(roleId);
		final Collection<SystemGroup> groupCodes = (Collection<SystemGroup>)getDatabase().executeQuery(getUser(), srQuery);
		
		if (groupCodes.isEmpty()){
			if (logger.isDebugEnabled()){
				logger.debug(MessageFormat.format("Group List for role {0} is empty", new Object[]{value}));
			}
			return null;
		}
		return groupCodes;
	}
}