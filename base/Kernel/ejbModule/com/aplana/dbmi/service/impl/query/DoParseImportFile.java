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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.aplana.dbmi.action.ImportAttribute;
import com.aplana.dbmi.action.ParseImportFile;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/**
 * ���������� ������� ����, ��������� ������� ������� ��������� �� ������� �������
 * @author PPanichev
 *
 */
 
public class DoParseImportFile extends ActionQueryBase {

	private static final long serialVersionUID = 1L;

	public Object processQuery() throws DataException {
		ParseImportFile action = (ParseImportFile)getAction();
		String typeImport = action.getTypeImportObject().name();
		//logger.debug(action);
		List<List<ImportAttribute>> importAttributes= new ArrayList<List<ImportAttribute>>();
		try{
			InputStream file = action.getFile();
			List<String> fileLines = getStringList(file);
			
			// ������ ������� - ��� ��� �������/�������
			final String[] sysObject = fileLines.get(0).split(
					ParseImportFile.FILE_ATTRIBUTE_DELIMETER);
			if (sysObject == null || sysObject[0] == null
					|| sysObject[0].isEmpty() || sysObject == null) {
				throw new DataException("import.input.name.table.empty");
			}
			if ((!typeImport.equals(sysObject[0]))) {
				throw new DataException("import.inbound.table.name.invalid",
						new Object[] { sysObject[0], typeImport });
			}

			// 2� ������� - ��� ������ ��������� ��������� (���������������) - �����
			final String attrCodes = fileLines.get(1);
			if (attrCodes==null||attrCodes.isEmpty()){
				throw new DataException("card.import.second.line.incorrect", new Object[]{});
			}
			// ������ ����� � ��������� ������ ��������� ��� ������� ��� ��������
			List<ImportAttribute> headCardAttributes = parseTableHead(attrCodes);
			for(int valueLineNumber = 2; valueLineNumber<fileLines.size(); valueLineNumber++ ){
				String line = fileLines.get(valueLineNumber);
				// ������ ������ �����, ���������� � � ����� ��������� ��������� ��� ������� � ������������ ����������
				importAttributes.add(parseTableLine(line, headCardAttributes, valueLineNumber+1));
			}
			return importAttributes;
		} catch (Exception e){
			throw new DataException(e.getMessage());
		}
	}
	
	private List<String> getStringList(InputStream file) throws Exception{
		// ��������� ��������� ������� ����
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(file));
		String line;
		List<String> lines = new ArrayList<String>();
		try{
			while ((line = reader.readLine()) != null) {
		        lines.add(line);
			}
		} finally {
			reader.close();
		}
		return lines;
	}
	
	/**
	 * ������� ����� csv-����� 
	 * @param line - ����� � ��������� ��������� �����
	 * ����� ����� ����� ��������� ���:
	 * !%@code%!;!%@name_rus%!;!%@name_eng%!;@some_code
	 * %% - ������� �������������� ��������
	 * ! - ������� �������� �� �����
	 * ����� �������� ������ ��������� ��������.
	 * @return ������ ��������� �����
	 * throws DataException
	 */
	private List<ImportAttribute> parseTableHead(String line) throws DataException{
		String[] attrs = line.split(ParseImportFile.FILE_ATTRIBUTE_DELIMETER);
		List<ImportAttribute> list = new ArrayList<ImportAttribute>();
		StringBuffer errors = new StringBuffer(); 
		for(String attr : attrs){
			try{
				ImportAttribute nextAttr = new ImportAttribute();
				// ���� ������� ��������� � ������ ����������, �� ������� �������� ������ ���� ����� ��������� ��������������
				if(attr.trim().startsWith(ParseImportFile.HEAD_DOUBLET_CHECK_FLAG)&&attr.trim().endsWith(ParseImportFile.HEAD_DOUBLET_CHECK_FLAG)){
					nextAttr.setDoubletCheck(true);
					attr = attr.trim().substring(1, attr.length()-1);
				}
				if(attr.trim().startsWith(ParseImportFile.HEAD_MANDATORY_FLAG)&&attr.trim().endsWith(ParseImportFile.HEAD_MANDATORY_FLAG)){
					nextAttr.setMandatory(true);
					attr = attr.trim().substring(1, attr.length()-1);
				}
				//Attribute 
				if (attr.startsWith(ParseImportFile.HEAD_CUSTOM_ATTRIBUTE_CODE_FLAG)){
					String customAttributeCode = attr.substring(1);
					try{
						nextAttr.setCustomPrimaryCodeId(ImportAttribute.CUSTOM_ATTRIBUTE_CODES.valueOf(customAttributeCode.toUpperCase()));
					} catch (Exception ex){
						if (logger.isErrorEnabled()){
							logger.error(ex.getMessage());
						}
						throw new DataException("card.import.custom.attribute.code.incorrect", new Object[]{customAttributeCode});
					}
				} 
				list.add(nextAttr);
			} catch (Exception e){
				// ������ �������� ������� �������� ���������� � �������� ���������
				errors.append("\n"+e.getMessage());
			}
		}
		if (errors.length()>0){
			// �������� ������ �������� ������� � �������� ������ � ����� �������
			throw new DataException("card.import.head.error", new Object[]{errors.toString()});
		}
		return list;
	}

	/**
	 * ������� ������� �� ���������� ������������� ��������� 
	 * @param line - ����� �� ���������� ��������� �� �����
	 * @param headAttributes - ������ ��������� �� ����� ��� ����, ����� �������� �� ����� ����������� � ��������� ���������� 
	 * @return ������ ��������� �� ����������
	 * throws DataException
	 */
	private List<ImportAttribute> parseTableLine(String line, List<ImportAttribute> headAttributes, int lineNumber) throws DataException{
		try {
			String[] attrValues = line.split(ParseImportFile.FILE_ATTRIBUTE_DELIMETER, -1);
			if (attrValues.length!=headAttributes.size()){
				throw new DataException("card.import.attribute.value.incorrect.number", new Object[]{attrValues.length, headAttributes.size()});
			}
			final List<ImportAttribute> list = new ArrayList<ImportAttribute>();
			for(int i=0; i<attrValues.length; i++){
				final ImportAttribute nextAttribute = ((ImportAttribute)headAttributes.get(i)).clone();
				// ��� ����� ��� �������� � ��������� �������������� �������, ������� ��������� �� � ������ ��������, �������� �������� ����� ������������� ��� ��� ������� 
				try{
					String attrCode = (nextAttribute.getPrimaryCodeId()!=null)?nextAttribute.getPrimaryCodeId().getId().toString():nextAttribute.getCustomPrimaryCodeId().name(); 
					if (nextAttribute.isMandatory()&&(attrValues[i]==null||attrValues[i].isEmpty())){
						throw new DataException("card.import.mandatory.attribute.empty", new Object[]{attrCode});
					}
					if (nextAttribute.isDoubletCheck()&&(attrValues[i]==null||attrValues[i].isEmpty())){
						throw new DataException("card.import.doubletCheck.attribute.empty", new Object[]{attrCode});
					}
					nextAttribute.setValue(attrValues[i]);
				} catch (DataException e){
					nextAttribute.setTroubleMessage(e.getMessage());
				}
				list.add(nextAttribute);
			}
			return list;
		} catch (Exception e){
			// ����� ����������� ��� ����������� ������� ������ �������� ������� � �������� ������ � ������ �
			throw new DataException("card.import.card.parsing.error", new Object[]{lineNumber, e.getMessage()});
		}
	}
}
