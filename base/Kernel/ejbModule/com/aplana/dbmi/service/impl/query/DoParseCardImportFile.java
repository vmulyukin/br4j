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
import java.util.Map;

import org.jaxen.util.SingletonList;

import com.aplana.dbmi.action.GetAttributeCodeByName;
import com.aplana.dbmi.action.ImportAttribute;
import com.aplana.dbmi.action.ParseCardImportFile;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.ImportAttribute.CUSTOM_ATTRIBUTE_CODES;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/**
 * Распарсить входной файл, проверить наличие входных атрибутов во входном шаблоне
 * @author ynikitin
 *
 */
 
public class DoParseCardImportFile extends ActionQueryBase {

	public Object processQuery() throws DataException {
		ParseCardImportFile action = (ParseCardImportFile)getAction();
		//logger.debug(action);
		List<List<ImportAttribute>> importAttributes= new ArrayList();
		try{
			ObjectId templateId = action.getTemplateId();
			InputStream file = action.getFile();
			List<String> fileLines = getStringList(file);
			
			// первая строчка - это шаблон
			final String[] sTemplateId = fileLines.get(0).split(ParseCardImportFile.FILE_ATTRIBUTE_DELIMETER);
			if (sTemplateId==null||sTemplateId[0]==null||sTemplateId[0].isEmpty()||templateId==null){
				throw new DataException("card.import.input.template.empty");
			}
			if ((!templateId.getId().equals(Long.parseLong(sTemplateId[0])))){
				throw new DataException("card.import.input.template.invalid", new Object[]{sTemplateId[0], templateId.getId()});
			}
			
			final String attrCodes = fileLines.get(1);
			if (attrCodes==null||attrCodes.isEmpty()){
				throw new DataException("card.import.second.line.incorrect", new Object[]{});
			}
			// парсим шапку и формируем список атрибутов для импорта без значений
			List<ImportAttribute> headCardAttributes = parseTableHead(attrCodes, (Long)templateId.getId());
			if(action.isGetHead()){
				importAttributes.add(headCardAttributes);
				return importAttributes;
			}
			for(int valueLineNumber = 2; valueLineNumber<fileLines.size(); valueLineNumber++ ){
				String line = fileLines.get(valueLineNumber);
				// парсим каждую линию, преваращая её в набор атрибутов для импорта с заполненными значениями
				importAttributes.add(parseTableLine(line, headCardAttributes, valueLineNumber+1));
			}
			return importAttributes;
		} catch (Exception e){
			throw new DataException(e.getMessage());
		}
	}
	
	private List<String> getStringList(InputStream file) throws Exception{
		// построчно считываем входной файл
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
	 * Парсинг шапки csv-файла 
	 * @param line - линия с описанием атрибутов шапки
	 * шапка может иметь следующий вид:
	 * %DESCR%;%JBR_REGLOG_TYPE_J~v%;%JBR_JREG_NUMFORM%;%ADMIN_1091614~t.484->JBR_DEPT_FULLNAME%, где
	 * DESCR, JBR_REGLOG_TYPE_J - коды атрибутов в БД
	 * %% - признак обязательности атрибута (обязательные атрибуты участвуют в проверке на дубликаты)
	 * ~v - признак того, что атрибут является ссылкой на справочное значение
	 * ~t.484 - признак того, что атрибут ссылается на карточки шаблона 484, а атрибут, указанный после ->, говорит какой атрибут в ссылочной карточке надо сравнивать со значением
	 * @return список атрибутов шапки
	 * throws DataException
	 */
	private List<ImportAttribute> parseTableHead(String line, Long templateId) throws DataException{
		String[] attrs = line.split(ParseCardImportFile.FILE_ATTRIBUTE_DELIMETER);
		List list = new ArrayList();
		StringBuffer errors = new StringBuffer(); 
		for(String attr : attrs){
			try{
				ImportAttribute nextAttr = new ImportAttribute();
				// если атрибут участвует в поиске дубликатов, то признак проверки должен идти перед признаком обязательности
				if(attr.trim().startsWith(ParseCardImportFile.HEAD_DOUBLET_CHECK_FLAG)&&attr.trim().endsWith(ParseCardImportFile.HEAD_DOUBLET_CHECK_FLAG)){
					nextAttr.setDoubletCheck(true);
					attr = attr.trim().substring(1, attr.length()-1);
				}
				if(attr.trim().startsWith(ParseCardImportFile.HEAD_MANDATORY_FLAG)&&attr.trim().endsWith(ParseCardImportFile.HEAD_MANDATORY_FLAG)){
					nextAttr.setMandatory(true);
					attr = attr.trim().substring(1, attr.length()-1);
				}
				String[] subAttrs = attr.split(ParseCardImportFile.HEAD_LINK_DELIMETER);
				//Attribute 
				if (subAttrs[0].startsWith(ParseCardImportFile.HEAD_CUSTOM_ATTRIBUTE_CODE_FLAG)){
					String customAttributeCode = subAttrs[0].substring(1);
					try{
						nextAttr.setCustomPrimaryCodeId(ImportAttribute.CUSTOM_ATTRIBUTE_CODES.valueOf(customAttributeCode.toUpperCase()));
						// для кастомного кода атрибута login добавляем насильную обязательность атрибута и признак поиска по нему дубликатов
						if (ImportAttribute.CUSTOM_ATTRIBUTE_CODES.valueOf(customAttributeCode.toUpperCase()).equals(CUSTOM_ATTRIBUTE_CODES.LOGIN)){
							nextAttr.setMandatory(true);
							nextAttr.setDoubletCheck(true);
						}
					} catch (Exception ex){
						if (logger.isErrorEnabled()){
							logger.error(ex.getMessage());
						}
						throw new DataException("card.import.custom.attribute.code.incorrect", new Object[]{customAttributeCode});
					}
				} else {
					nextAttr.setPrimaryCodeId(getAttrIdByAttrCode(subAttrs[0], templateId));
				}
				if(subAttrs.length >= 2){
					// первый атрибут-ссылка на карточку определенного шаблона
					if(subAttrs[1].startsWith(ParseCardImportFile.HEAD_TEMPLATE_FLAG)){
						String[] linkInfo = subAttrs[1].split(ParseCardImportFile.HEAD_TEMPLATE_DEST_ATTRIBUTE_DELIMETER);
						ObjectId linkTemplate = new ObjectId(Template.class, Long.parseLong(linkInfo[0].substring(2)));
						nextAttr.setLinkTemplateId(linkTemplate);
						checkLinkTemplate(nextAttr, templateId);
						// если ссылочный атрибут - логин пользователя
						if (linkInfo[1].startsWith(ParseCardImportFile.HEAD_CUSTOM_ATTRIBUTE_CODE_FLAG)){
							String customAttributeCode = linkInfo[1].substring(1);
							try{
								nextAttr.setCustomLinkCodeId(ImportAttribute.CUSTOM_ATTRIBUTE_CODES.valueOf(customAttributeCode.toUpperCase()));
							} catch (Exception ex){
								if (logger.isErrorEnabled()){
									logger.error(ex.getMessage());
								}
								throw new DataException("card.import.custom.attribute.code.incorrect", new Object[]{customAttributeCode});
							}
								
							
						} else {
							nextAttr.setLinkCodeId(getAttrIdByAttrCode(linkInfo[1], (Long)linkTemplate.getId()));
						}
					// первый атрибут - ссылка на справочник
					} else if(subAttrs[1].startsWith(ParseCardImportFile.HEAD_REFERENCE_FLAG)){
						nextAttr.setReference(true);
						if (subAttrs[1].length()>1){
							String refName = subAttrs[1].substring(2);
							if (refName!=null&&!refName.isEmpty()){
								nextAttr.setReferenceName(refName);
							}
						}
					// первый атрибут-ссылка на карточки произвольного шаблона (т.е. нет приставки "t.<id шаблона>->")
					} else {
						throw new DataException("card.import.link.attribute.without.template", new Object[]{subAttrs[1]}); 
					}
				}
				list.add(nextAttr);
			} catch (Exception e){
				// ошибки парсинга каждого атрибута складируем в итоговое сообщение
				errors.append("\n"+e.getMessage());
			}
		}
		if (errors.length()>0){
			// итоговую ошибку парсинга выводим с пометкой Ошибка в шапке импорта
			throw new DataException("card.import.head.error", new Object[]{errors.toString()});
		}
		return list;
	}

	/**
	 * Парсинг строчек со значениями импортируемых атрибутов 
	 * @param line - линия со значениями атрибутов из шапки
	 * @param headAttributes - список атрибутов из шапки для того, чтобы атрибуты из шапки клонировать и дополнить значениями 
	 * @return список атрибутов со значениями
	 * throws DataException
	 */
	private List<ImportAttribute> parseTableLine(String line, List<ImportAttribute> headAttributes, int lineNumber) throws DataException{
		try {
			String[] attrValues = line.split(ParseCardImportFile.FILE_ATTRIBUTE_DELIMETER, -2);
			if (attrValues.length!=headAttributes.size()){
				throw new DataException("card.import.attribute.value.incorrect.number", new Object[]{attrValues.length, headAttributes.size()});
			}
			final List list = new ArrayList();
			for(int i=0; i<attrValues.length; i++){
				final ImportAttribute nextAttribute = ((ImportAttribute)headAttributes.get(i)).clone();
				// нам нужны все проблемы в атрибутах импортируемой карточки, поэтому сохраняем их в каждом атрибуте, характер проблемы будем анализировать уже при импорте 
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
			// любую критическую для продолжения импорта ошибку парсинга выводим с пометкой Ошибка в карточке №
			throw new DataException("card.import.card.parsing.error", new Object[]{lineNumber, e.getMessage()});
		}
	}
	
	/**
	 * Проверить наличие входного атрибута во входном шаблоне
	 * @param attrCode - строковый код атрибута
	 * @param templateId - id шаблона
	 * @return возвращаем атрибут в виде ObjectId
	 * throws DataException 
	 */
	private ObjectId getAttrIdByAttrCode(String attrCode, Long templateId) throws DataException{
		// для анализа используем самописный экшен GetAttributeCodeByName
		GetAttributeCodeByName action = new GetAttributeCodeByName();
		action.setTemplateId(new ObjectId(Template.class, templateId));
		action.setAttrNames(new SingletonList(attrCode));
		ActionQueryBase aqb = getQueryFactory().getActionQuery(action);
		aqb.setAccessChecker(null);
		aqb.setAction(action);
		List<ObjectId> attributes = (List<ObjectId>)getDatabase().executeQuery(getUser(), aqb);
		if (attributes==null||attributes.isEmpty()){
			throw new DataException("card.import.atribute.not.in.template", new Object[]{attrCode, templateId});
		}
		return attributes.get(0);
	}
	
	/**
	 * Проверить вхождение ссылочного шаблона во множество доступных для основного атрибута
	 * @param nextAttr - импортируемый атрибут с заполненным основным атрибутом и ссылочным шаблоном  
	 * @param templateId - id входного шаблона
	 * throws DataException если не входит 
	 */
	private void checkLinkTemplate(ImportAttribute nextAttr, Long templateId) throws DataException{
		// необходимо подгрузить свойства основного атриубута (из xml_data или editors.xml) и проверить список отображаемых шаблонов
		if (nextAttr.getPrimaryCodeId()!=null){
			ObjectId attrCode = nextAttr.getPrimaryCodeId();
			if (attrCode.getType().equals(CardLinkAttribute.class)||attrCode.getType().equals(TypedCardLinkAttribute.class)||attrCode.getType().equals(DatedTypedCardLinkAttribute.class)){
				ParseCardImportFile action = (ParseCardImportFile)getAction();
				Map<ObjectId, Search> cardLinkAttributeSearch = action.getCardLinkAttributeSearchMap();
				// если входной мап пустой или в нём нет анализируемого кардлинка, то значит он может ссылаться на любой шаблон, включая входной
				if (cardLinkAttributeSearch!=null&&cardLinkAttributeSearch.get(attrCode)!=null){
					List<Template> templates = (ArrayList<Template>)cardLinkAttributeSearch.get(attrCode).getTemplates();
					// если для сёрча анализиуемого кардлинка список возможных шаблонов пустой, то  значит он может ссылаться на любой шаблон, включая входной
					if (templates!=null&&!templates.isEmpty()){
						for (Template template:templates){
							if (template.getId().equals(nextAttr.getLinkTemplateId())){
								return;
							}
						}
						throw new DataException("card.import.attribute.incorrect.link", new Object[]{attrCode, templateId, nextAttr.getLinkTemplateId().getId()});
					}
				}
			} else if (attrCode.getType().equals(PersonAttribute.class)) {
				// персон атрибуты могут ссылаться только на 10-й шаблон
				if (!nextAttr.getLinkTemplateId().equals(ObjectId.predefined(Template.class, "jbr.internalPerson"))){
					throw new DataException("card.import.attribute.incorrect.link", new Object[]{attrCode, templateId, nextAttr.getLinkTemplateId().getId()});
				}
			} else {
				throw new DataException("card.import.attribute.not.linked", new Object[]{attrCode, templateId});
			}
		} else {
			throw new DataException("card.import.attribute.not.linked", new Object[]{nextAttr.getCustomPrimaryCodeId().name(), templateId});
		}
	}
}
