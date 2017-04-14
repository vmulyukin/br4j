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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.ImportAttribute;
import com.aplana.dbmi.action.ImportCards;
import com.aplana.dbmi.action.ImportResult;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.ImportAttribute.CUSTOM_ATTRIBUTE_CODES;
import com.aplana.dbmi.action.ImportResult.IMPORT_RESULT_TYPE;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.LongAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.PortalUserLoginAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.UserRolesAndGroupsAttribute;
import com.aplana.dbmi.model.util.DateUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/*
 * ToDo:
 * 3. Локализовать сообщения об успехе и ошибках 
 */
public class DoImportCards extends ActionQueryBase implements WriteQuery {

	private static final long serialVersionUID = 1L;

	public Object processQuery() throws DataException {
		ImportCards action = (ImportCards)getAction();
		logger.debug(action);
		
		ObjectId templateId = (ObjectId)action.getTemplateId();
		List<ImportAttribute> importAttributes = action.getImportAttributes();
		boolean checkDoublets = action.isCheckDoublets();
		boolean updateDoublets = action.isUpdateDoublets();
		String templateName = action.getTemplateName();
		int lineNumber = action.getLineNumber();
		
		if (templateId==null||templateId.getId()==null){
			throw new DataException("card.import.input.template.empty");
		}
		if (importAttributes==null||importAttributes.isEmpty()){
			throw new DataException("card.import.attribute.codes.is.empty");
		}
		
		String objectName = MessageFormat.format(ContextProvider.getContext().getLocaleMessage("card.import.success.object.name.default"), new Object[]{templateName});
		if (action.getCustomImportObjectName()!=null){
			objectName = action.getCustomImportObjectName();
		}
		ImportResult result = new ImportResult();
		result.setImportAttributes(importAttributes);

		StringBuffer errorMessages = new StringBuffer(ContextProvider.getContext().getLocaleMessage("card.import.found.trouble.object")); 

		List<ObjectId> doubletCardIds = new ArrayList<ObjectId>(); 
		try{
			if (logger.isDebugEnabled()){
				logger.debug(MessageFormat.format("Import card {0} in line {1}", new Object[]{importAttributes, lineNumber}));
			}
			// делим обязательные и необязательные атрибуты на отдельные списки, определеяем список проблемных атрибутов
			List<ImportAttribute> notMandatoryCardAttributes = new ArrayList<ImportAttribute>();
			List<ImportAttribute> troubleCardAttributes = getTroubleAttributes(importAttributes);
			notMandatoryCardAttributes.addAll(importAttributes);
			List<ImportAttribute> mandatoryCardAttributes = getMandatoryForDoubletCheckAttributes(importAttributes);
			notMandatoryCardAttributes.removeAll(mandatoryCardAttributes);
			
			// на данном этапе наличие проблем при парсинге атрибутов означает внесение карточки в список проблемных и прерывание её дальнейшей обработки
			if (troubleCardAttributes!=null&&!troubleCardAttributes.isEmpty()){
				throw new DataException(generateTroubleMessage(troubleCardAttributes));				
			}
			// проверяем на дубликаты при необходимости (необходимость в данном случае - это условие проверки на дубликаты или условие обновления дубликатов)
			if (checkDoublets||updateDoublets){
				doubletCardIds = findCardOrPerson(templateId, mandatoryCardAttributes, false);
				if (doubletCardIds!=null&&doubletCardIds.size()>0){
					StringBuffer doubletMsg = new StringBuffer();
					for(ObjectId doubletCardId :doubletCardIds){
					// если дубликат найден
						if (doubletCardId!=null){
							if (logger.isDebugEnabled()){
								logger.debug(MessageFormat.format("Found doublet card {0} for line {1}", new Object[]{doubletCardId.getId().toString(), lineNumber}));
							}
							// если необходимо, то у дубликата обновляем необязательные атрибуты
							if (updateDoublets){
								updateCard(doubletCardId, notMandatoryCardAttributes);
								if (logger.isDebugEnabled()){
									logger.debug(MessageFormat.format("Updated doublet card {0} for line {1}", new Object[]{doubletCardId.getId(), lineNumber}));
								}
							} 
						}
					}
					result.addAllImportObjectIds(doubletCardIds);
					result.setResultType(IMPORT_RESULT_TYPE.DOUBLET);
					final String doublets = IdUtils.makeObjectIdStringLine(doubletCardIds, ",");
					result.setResultMessage("\n\t"+MessageFormat.format(ContextProvider.getContext().getLocaleMessage("card.import.doublet.card"), new Object[]{lineNumber, doublets}));
					return result;
				}
			}
			Card importCard = createNewCard(templateId, importAttributes);
			if (logger.isDebugEnabled()){
				logger.debug(MessageFormat.format("Created new card card {0} for line {1}", new Object[]{importCard.getId().getId(), lineNumber}));
			}
			
			List<String> successList = generateSuccessList(Collections.singletonList(importCard));
			StringBuffer str = new StringBuffer();
			for(String s: successList){
				str.append("\n\t"+s);
			}
			result.addImportObjectId(importCard.getId());
			result.setResultType(IMPORT_RESULT_TYPE.SUCCESS);
			result.setResultMessage(str.toString());
			return result;
		} catch (Exception e){
			if (logger.isErrorEnabled()){ 
				logger.error(e.getMessage());
			}
			// при любых проблемах в импорте карточки, выкидываем исключение
			throw new DataException(e.getMessage());
		}
	}

	/**
	 * Сформировать из исходного списка импортируемых атрибутов список обязательных для поиска дубликатов атрибутов
	 * @param ImportAttributes - исходный список импортируемых атрибутов
	 * @return список только обязательных для поиска атрибутов
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
	 * Сформировать из исходного списка импортируемых атрибутов список проблемных атрибутов (тех, для которых выявлены ошибки при парсинге)
	 * @param ImportAttributes - исходный список импортируемых атрибутов
	 * @return список только проблемных атрибутов
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
	 * Найти в БД карточку определенного шаблона по списку заполненных атрибутов
	 * или персону по списку заполненных атрибутов в её карточке  
     * @param templateId - входной шаблон
	 * @param cardAttributes - список заполненных атрибутов
	 * @return id карточки или id персоны
	 * @throws DataException 
	 * @throws ParseException 
	 */
	private List<ObjectId> findCardOrPerson(ObjectId templateId, List<ImportAttribute> cardAttributes, boolean isPerson) throws DataException, ParseException{
		if (logger.isDebugEnabled()){
			if (isPerson){
				logger.debug(MessageFormat.format("Try find person with attributes {0}", new Object[]{cardAttributes}));
			} else {
				logger.debug(MessageFormat.format("Try find card with template {0} and attributes {1}", new Object[]{templateId.getId(), cardAttributes}));
			}
		}
		
		// искать будем через обычный sql-запрос
		StringBuffer sqlBuf = (isPerson)?new StringBuffer("select distinct c.person_id from person c"):new StringBuffer("select distinct c.card_id from card c");
		StringBuffer whereSqlBuf = new StringBuffer();
		int i = 100;
		for(ImportAttribute cardImportAttribute : cardAttributes){
			if (logger.isDebugEnabled()){
				logger.debug(MessageFormat.format("Genereate sql-part for attribute {0}", new Object[]{cardImportAttribute}));
			}
			final String alias = "av_" + i++; 
			final String linkedAlias = "av_" + i++;// очередной синоним таблицы
			final String linkedAlias2 = "av_" + i++;// ещё один синоним таблицы
			ObjectId attributeId = cardImportAttribute.getPrimaryCodeId();
			final Class attributeClass = (attributeId!=null)?attributeId.getType():null;
			Attribute attr = null;
			if (attributeId!=null){
				attr = (Attribute) DataObject.createFromId(attributeId);
			} 
			if (StringAttribute.class.equals(attributeClass)||TextAttribute.class.equals(attributeClass)){
				sqlBuf.append( "\n\t\t JOIN attribute_value as "+ alias +" ON ( \n" ); // (*)
				sqlBuf.append( "\t\t\t 		("+ alias +".card_id = c.card_id) AND \n" ); 
				sqlBuf.append( MessageFormat.format( 
						"\t\t\t\t\t ({0}.attribute_code=''{1}'') AND \n",
						new Object[] {	alias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getPrimaryCodeId().getId()) }
						
				));		
				sqlBuf.append( MessageFormat.format( 
						"\t\t\t\t\t ({0}.string_value=''{1}'') \n",
						new Object[] {	alias, StringEscapeUtils.escapeSql(String.valueOf(cardImportAttribute.getValue())) }
				));
				sqlBuf.append( "\n\t\t )" ); // (*)
			} else if (ListAttribute.class.equals(attributeClass)&&cardImportAttribute.isReference()){
				// атрибут-ссылка на текстовое значение справочника 
				sqlBuf.append( "\n\t\t JOIN attribute_value as "+ alias +" ON ( \n" ); // (*)
				sqlBuf.append( "\t\t\t 		("+ alias +".card_id = c.card_id) AND \n" ); 
				sqlBuf.append( MessageFormat.format( 
						"\t\t\t\t\t ({0}.attribute_code=''{1}'') \n",
						new Object[] {	alias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getPrimaryCodeId().getId()) }
						
				));	
				sqlBuf.append( "\n\t\t )" ); // (*)

				sqlBuf.append( "\n\t\t JOIN values_list as "+ linkedAlias +" ON ( \n" ); // (*)
				sqlBuf.append( "\t\t\t 		("+ linkedAlias +".value_id = "+alias+".value_id) AND \n" ); 
				sqlBuf.append( MessageFormat.format( 
						"\t\t\t\t\t ({0}.value_rus = ''{1}'')\n",
						new Object[] {	linkedAlias, StringEscapeUtils.escapeSql(String.valueOf(cardImportAttribute.getValue())) }
						
				));
				sqlBuf.append( "\n\t\t )" ); // (*)
				
				// если задано название справочника, то ищем среди его значений 
				if (cardImportAttribute.getReferenceName()!=null&&!cardImportAttribute.getReferenceName().isEmpty()){
					sqlBuf.append( "\n\t\t JOIN reference_list as "+ linkedAlias2 +" ON ( \n" ); // (*)
					sqlBuf.append( "\t\t\t 		("+ linkedAlias2 +".ref_code = "+linkedAlias+".ref_code) AND \n" ); 
					sqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t ({0}.ref_code = ''{1}'')\n",
							new Object[] {	linkedAlias2, StringEscapeUtils.escapeSql(cardImportAttribute.getReferenceName()) }
							
					));
				} else {
					// иначе ищем в справочнике конкретного атрибута
					sqlBuf.append( "\n\t\t JOIN attribute_option as "+ linkedAlias2 +" ON ( \n" ); // (*)
					sqlBuf.append( "\t\t\t 		("+ linkedAlias2 +".option_value = "+linkedAlias+".ref_code) AND \n" ); 
					sqlBuf.append( "\t\t\t\t 	("+ linkedAlias2 +".option_code = 'REFERENCE') AND \n" ); 
					sqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t ({0}.attribute_code=''{1}'')\n",
							new Object[] {	linkedAlias2, StringEscapeUtils.escapeSql(attributeId.getId().toString()) }
							
					));
				}
				sqlBuf.append( "\n\t\t )" ); // (*)
			} else if (ListAttribute.class.equals(attributeClass)&&!cardImportAttribute.isReference()){
				// атрибут-ссылка на id справочника 
				sqlBuf.append( "\n\t\t JOIN attribute_value as "+ alias +" ON ( \n" ); // (*)
				sqlBuf.append( "\t\t\t 		("+ alias +".card_id = c.card_id) AND \n" ); 
				sqlBuf.append( MessageFormat.format( 
						"\t\t\t\t\t ({0}.attribute_code=''{1}'') AND \n",
						new Object[] {	alias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getPrimaryCodeId().getId()) }
						
				));	
				sqlBuf.append( MessageFormat.format( 
						"\t\t\t\t\t ({0}.value_id = {1})\n",
						new Object[] {	alias, StringEscapeUtils.escapeSql(String.valueOf(cardImportAttribute.getValue())) }));
				sqlBuf.append( "\n\t\t )" ); // (*)
			} else if (DateAttribute.class.equals(attributeClass)){
				sqlBuf.append( "\n\t\t JOIN attribute_value as "+ alias +" ON ( \n" ); // (*)
				sqlBuf.append( "\t\t\t 		("+ alias +".card_id = c.card_id) AND \n" ); 
				sqlBuf.append( MessageFormat.format( 
						"\t\t\t\t\t ({0}.attribute_code=''{1}'') AND \n",
						new Object[] {	alias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getPrimaryCodeId().getId()) }
						
				));	
				SimpleDateFormat formatter=new SimpleDateFormat("dd.MM.yyyy");
				Date dateValue = formatter.parse(cardImportAttribute.getValue());
				dateValue = DateUtils.toUTC(dateValue);
				sqlBuf.append( MessageFormat.format( 
						"\t\t\t\t\t ({0}.date_value=''{1}'')\n",
						new Object[] {	alias, StringEscapeUtils.escapeSql(dateValue.toString()) }));
				sqlBuf.append( "\n\t\t )" ); // (*)
			} else if (IntegerAttribute.class.equals(attributeClass)||LongAttribute.class.equals(attributeClass)){
				sqlBuf.append( "\n\t\t JOIN attribute_value as "+ alias +" ON ( \n" ); // (*)
				sqlBuf.append( "\t\t\t 		("+ alias +".card_id = c.card_id) AND \n" ); 
				sqlBuf.append( MessageFormat.format( 
						"\t\t\t\t\t ({0}.attribute_code=''{1}'') AND \n",
						new Object[] {	alias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getPrimaryCodeId().getId()) }
						
				));		
				sqlBuf.append( MessageFormat.format( 
						"\t\t\t\t\t ({0}.number_value = {1}) \n",
						new Object[] {	alias, StringEscapeUtils.escapeSql(String.valueOf(cardImportAttribute.getValue())) }
				));
				sqlBuf.append( "\n\t\t )" ); // (*)
			} else if (CardLinkAttribute.class.equals(attributeClass)){
				// при поиске карточек (дубликатов или справочных) учитываем только те, где есть полное совпадение атрибутов и входных значений, причем лишние записи отсутсвуют
				// сначала добавляем ограничение на отсутствие лишних значений
				if (cardImportAttribute.getCustomLinkCodeId()==null&&cardImportAttribute.getLinkCodeId()==null){
					whereSqlBuf.append("\n\t AND not exists(select 1 from attribute_value as "+ alias +" WHERE ( \n" ); // (*)
					whereSqlBuf.append( "\t\t\t	("+ alias +".card_id = c.card_id) AND \n" ); 
					whereSqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t ({0}.attribute_code=''{1}'') \n",
							new Object[] {	alias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getPrimaryCodeId().getId()) }
							
					));
					whereSqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t AND not({0}.number_value in ({1})) \n",
							new Object[] {	alias, generateValuesList(StringEscapeUtils.escapeSql((String)cardImportAttribute.getValue()), ",", null) }
					));
					whereSqlBuf.append("\n\t\t)");
					whereSqlBuf.append("\n\t)");
					// далее отрабатываем все значения
					String[] values = cardImportAttribute.getValue().split("]>");
					for(String tempValue: values){ 
						String value = tempValue.replaceAll("<\\[", "").replaceAll("]>", "").trim();
						if (value==null||value.isEmpty()){
							continue;
						}
						final String inner_alias = "av_" + i++; 
						sqlBuf.append( "\n\t\t JOIN attribute_value as "+ inner_alias +" ON ( \n" ); // (*)
						sqlBuf.append( "\t\t\t	("+ inner_alias +".card_id = c.card_id) AND \n" ); 
						sqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t ({0}.attribute_code=''{1}'') \n",
								new Object[] {	inner_alias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getPrimaryCodeId().getId()) }
								
						));
						sqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t AND ({0}.number_value = {1}) \n",
								new Object[] {	inner_alias, value }
						));
						sqlBuf.append("\n\t\t)");
					}
					continue;
				}
/*				sqlBuf.append( "\n\t\t JOIN attribute_value as "+ alias +" ON ( \n" ); // (*)
				sqlBuf.append( "\t\t\t	("+ alias +".card_id = c.card_id) AND \n" ); 
				sqlBuf.append( MessageFormat.format( 
						"\t\t\t\t\t ({0}.attribute_code=''{1}'') \n",
						new Object[] {	alias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getPrimaryCodeId().getId()) }
						
				));*/
				// в случае если по логину
				if (cardImportAttribute.getCustomLinkCodeId()==CUSTOM_ATTRIBUTE_CODES.LOGIN){
					whereSqlBuf.append("\n\t AND not exists(select 1 from attribute_value as "+ alias +" join person as "+ linkedAlias +" on ( \n" );
					whereSqlBuf.append( "\t\t\t	("+ alias +".card_id = c.card_id) AND \n" ); 
					whereSqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t ({0}.attribute_code=''{1}'') \n",
							new Object[] {	alias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getPrimaryCodeId().getId()) }
							
					));
					whereSqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t AND not({0}.number_value in ({1})) \n",
							new Object[] {	alias, generateValuesList(StringEscapeUtils.escapeSql((String)cardImportAttribute.getValue()), ",", null) }
					));
					whereSqlBuf.append("\n\t\t)");
					whereSqlBuf.append("\n\t WHERE ( \n" ); // (*)" +
					whereSqlBuf.append( "\t\t\t 		("+ linkedAlias +".card_id = "+alias+".number_value) AND \n" ); 
					whereSqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t not({0}.person_login in ({1})) \n",
							new Object[] {	linkedAlias, generateValuesList(StringEscapeUtils.escapeSql((String)cardImportAttribute.getValue()), ",", "'") }
					));
					whereSqlBuf.append( "\n\t\t\t ))" ); // (*)
					
				// иначе ищем отсутсвие ссылочных ссылочных карточек
				} else if (cardImportAttribute.getLinkCodeId()!=null){
					whereSqlBuf.append("\n\t AND not exists(select 1 from attribute_value as "+ alias +" join attribute_value as "+ linkedAlias +" on ( \n" );
//					whereSqlBuf.append( "\n\t\t and not exists(select 1 from attribute_value as "+ linkedAlias +" where ( \n" ); // (*)
					whereSqlBuf.append( "\t\t\t 		("+ linkedAlias +".card_id = "+alias+".number_value) AND \n" ); 
					whereSqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t ({0}.attribute_code=''{1}'') AND \n",
							new Object[] {	linkedAlias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getLinkCodeId().getId()) }
							
					));		
					whereSqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t ({0}.template_id={1}) AND \n",
							new Object[] {	linkedAlias, StringEscapeUtils.escapeSql(cardImportAttribute.getLinkTemplateId().getId().toString()) }
							
					));		
					ObjectId linkAttributeId = cardImportAttribute.getLinkCodeId();
					final Class linkAttributeClass = linkAttributeId.getType();
					if (linkAttributeClass.equals(StringAttribute.class)||linkAttributeClass.equals(TextAttribute.class)){
						whereSqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t not({0}.string_value in ({1})) \n",
								new Object[] {	linkedAlias, generateValuesList(StringEscapeUtils.escapeSql((String)cardImportAttribute.getValue()), ",", "'") }
								
						));
					} else if (linkAttributeClass.equals(ListAttribute.class)){
						whereSqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t not({0}.value_id in({1})) \n",
								new Object[] {	linkedAlias, generateValuesList(StringEscapeUtils.escapeSql((String)cardImportAttribute.getValue()), ",", null) }
								
						));
					} else if (linkAttributeClass.equals(CardLinkAttribute.class)||attributeClass.equals(IntegerAttribute.class)||attributeClass.equals(LongAttribute.class)||attributeClass.equals(PersonAttribute.class)){
						whereSqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t not({0}.number_value in ({1})) \n",
								new Object[] {	linkedAlias, generateValuesList(StringEscapeUtils.escapeSql((String)cardImportAttribute.getValue()), ",", null) }
								
						));
					} else if (linkAttributeClass.equals(DateAttribute.class)){
						SimpleDateFormat formatter=new SimpleDateFormat("dd.MM.yyyy");
						Date dateValue = formatter.parse(cardImportAttribute.getValue());
						dateValue = DateUtils.toUTC(dateValue);
						whereSqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t not({0}.date_value in ({1}))\n",
								new Object[] {	linkedAlias, generateValuesList(StringEscapeUtils.escapeSql((String)cardImportAttribute.getValue()), ",", "'") }));
					} else {
						throw new DataException("card.import.incorrect.type.or.format", new Object[]{linkAttributeId.getId(), StringEscapeUtils.escapeSql(cardImportAttribute.getLinkTemplateId().getId().toString())});
					}
					whereSqlBuf.append( "\n\t\t )" ); // (*)
					whereSqlBuf.append( "\n\t\t\t	WHERE("+ alias +".card_id = c.card_id) AND \n" ); 
					whereSqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t ({0}.attribute_code=''{1}'') \n",
							new Object[] {	alias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getPrimaryCodeId().getId()) }
							
					));
					whereSqlBuf.append( "\n\t\t )" ); // (*)
				} else {
					throw new DataException("card.import.incorrect.type.or.format", new Object[]{cardImportAttribute.getCustomLinkCodeId(), StringEscapeUtils.escapeSql(cardImportAttribute.getLinkTemplateId().getId().toString())});
				}
//				sqlBuf.append( "\n\t\t )" ); // (*)
				
				// далее проверяем, чтобы были ссылочные карточки с атрибутами, заполненными всеми входными значениями
				String[] values = cardImportAttribute.getValue().split("]>");
				for(String tempValue: values){ 
					String value = tempValue.replaceAll("<\\[", "").replaceAll("]>", "").trim(); 
					if (value==null||value.isEmpty()){
						continue;
					}
					final String inner_alias = "av_" + i++; 
					final String inner_linkedAlias = "av_" + i++;// очередной синоним таблицы
					final String inner_linkedAlias2 = "av_" + i++;// ещё один синоним таблицы
					sqlBuf.append( "\n\t\t JOIN attribute_value as "+ inner_alias +" ON ( \n" ); // (*)
					sqlBuf.append( "\t\t\t	("+ inner_alias +".card_id = c.card_id) AND \n" ); 
					sqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t ({0}.attribute_code=''{1}'') \n",
							new Object[] {	inner_alias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getPrimaryCodeId().getId()) }
							
					));
					sqlBuf.append( "\n\t\t )" ); // (*)
					// если ссылочный атрибут - логин пользователя, то ищем пользователя по логину
					if (cardImportAttribute.getCustomLinkCodeId()==CUSTOM_ATTRIBUTE_CODES.LOGIN){
						sqlBuf.append( "\n\t\t JOIN person as "+ inner_linkedAlias +" ON ( \n" ); // (*)
						sqlBuf.append( "\t\t\t 		("+ inner_linkedAlias +".card_id = "+inner_alias+".number_value) AND \n" ); 
						sqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t ({0}.person_login = ''{1}'') \n",
								new Object[] {	inner_linkedAlias, value }
						));
						sqlBuf.append( "\n\t\t )" ); // (*)

					// иначе ищем ссылочную карточку
					} else if (cardImportAttribute.getLinkCodeId()!=null){
						sqlBuf.append( "\n\t\t JOIN attribute_value as "+ inner_linkedAlias +" ON ( \n" ); // (*)
						sqlBuf.append( "\t\t\t 		("+ inner_linkedAlias +".card_id = "+inner_alias+".number_value) AND \n" ); 
						sqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t ({0}.attribute_code=''{1}'') AND \n",
								new Object[] {	inner_linkedAlias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getLinkCodeId().getId()) }
								
						));		
						sqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t ({0}.template_id={1}) AND \n",
								new Object[] {	inner_linkedAlias, StringEscapeUtils.escapeSql(cardImportAttribute.getLinkTemplateId().getId().toString()) }
								
						));		
						ObjectId linkAttributeId = cardImportAttribute.getLinkCodeId();
						final Class linkAttributeClass = linkAttributeId.getType();
						if (linkAttributeClass.equals(StringAttribute.class)||linkAttributeClass.equals(TextAttribute.class)){
							sqlBuf.append( MessageFormat.format( 
									"\t\t\t\t\t ({0}.string_value = ''{1}'') \n",
									new Object[] {	inner_linkedAlias, value }
									
							));
						} else if (linkAttributeClass.equals(ListAttribute.class)){
							sqlBuf.append( MessageFormat.format( 
									"\t\t\t\t\t ({0}.value_id ={1}) \n",
									new Object[] {	inner_linkedAlias, value }
									
							));
						} else if (linkAttributeClass.equals(CardLinkAttribute.class)||attributeClass.equals(IntegerAttribute.class)||attributeClass.equals(LongAttribute.class)||attributeClass.equals(PersonAttribute.class)){
							sqlBuf.append( MessageFormat.format( 
									"\t\t\t\t\t ({0}.number_value = {1}) \n",
									new Object[] {	inner_linkedAlias, value }
									
							));
						} else if (linkAttributeClass.equals(DateAttribute.class)){
							SimpleDateFormat formatter=new SimpleDateFormat("dd.MM.yyyy");
							Date dateValue = formatter.parse(cardImportAttribute.getValue());
							dateValue = DateUtils.toUTC(dateValue);
							sqlBuf.append( MessageFormat.format( 
									"\t\t\t\t\t ({0}.date_value = ''{1}'')\n",
									new Object[] {	inner_linkedAlias, value }));
						} else {
							throw new DataException("card.import.incorrect.type.or.format", new Object[]{linkAttributeId.getId(), StringEscapeUtils.escapeSql(cardImportAttribute.getLinkTemplateId().getId().toString())});
						}
						sqlBuf.append( "\n\t\t )" ); // (*)
					} else {
						throw new DataException("card.import.incorrect.type.or.format", new Object[]{cardImportAttribute.getCustomLinkCodeId(), StringEscapeUtils.escapeSql(cardImportAttribute.getLinkTemplateId().getId().toString())});
					}

				}
				//sqlBuf.append( "\n\t\t )" ); // (*)
			} else if (PersonAttribute.class.equals(attributeClass)){
				// при поиске карточек (дубликатов или справочных) учитываем только те, где есть полное совпадение атрибутов и входных значений, причем лишние записи отсутсвуют
				// сначала добавляем ограничение на отсутствие лишних значений
				if (cardImportAttribute.getCustomLinkCodeId()==null&&cardImportAttribute.getLinkCodeId()==null){
					whereSqlBuf.append("\n\t AND not exists(select 1 from attribute_value as "+ alias +" WHERE ( \n" ); // (*)
					whereSqlBuf.append( "\t\t\t	("+ alias +".card_id = c.card_id) AND \n" ); 
					whereSqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t ({0}.attribute_code=''{1}'') \n",
							new Object[] {	alias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getPrimaryCodeId().getId()) }
							
					));
					whereSqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t AND not({0}.number_value in ({1})) \n",
							new Object[] {	alias, generateValuesList(StringEscapeUtils.escapeSql((String)cardImportAttribute.getValue()), ",", null) }
					));
					whereSqlBuf.append("\n\t\t)");
					whereSqlBuf.append("\n\t)");
					// далее отрабатываем все значения
					String[] values = cardImportAttribute.getValue().split("]>");
					for(String tempValue: values){ 
						String value = tempValue.replaceAll("<\\[", "").replaceAll("]>", "").trim();
						if (value==null||value.isEmpty()){
							continue;
						}
						final String inner_alias = "av_" + i++; 
						sqlBuf.append( "\n\t\t JOIN attribute_value as "+ inner_alias +" ON ( \n" ); // (*)
						sqlBuf.append( "\t\t\t	("+ inner_alias +".card_id = c.card_id) AND \n" ); 
						sqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t ({0}.attribute_code=''{1}'') \n",
								new Object[] {	inner_alias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getPrimaryCodeId().getId()) }
								
						));
						sqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t AND ({0}.number_value = {1}) \n",
								new Object[] {	inner_alias, value }
						));
						sqlBuf.append("\n\t\t)");
					}
					continue;
				}
				// в случае если по логину
				if (cardImportAttribute.getCustomLinkCodeId()==CUSTOM_ATTRIBUTE_CODES.LOGIN){
					whereSqlBuf.append( "\n\t\t\t and not exists(select 1 from attribute_value as "+ alias +" join person as "+ linkedAlias +" on ( \n" ); // (*)
					whereSqlBuf.append( "\t\t\t 		("+ linkedAlias +".person_id = "+alias+".number_value) AND \n" ); 
					whereSqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t not({0}.person_login in ({1})) \n",
							new Object[] {	linkedAlias, generateValuesList(StringEscapeUtils.escapeSql((String)cardImportAttribute.getValue()), ",", "'") }
					));
					whereSqlBuf.append( "\n\t\t\t )" ); // (*)
					whereSqlBuf.append( "\t\t\t WHERE		("+ alias +".card_id = c.card_id) AND \n" ); 
					whereSqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t ({0}.attribute_code=''{1}'') \n",
							new Object[] {	alias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getPrimaryCodeId().getId()) }
							
					));		
					whereSqlBuf.append( "\n\t\t\t )" ); // (*)
					
				// иначе ищем отсутсвие ссылочных карточек
				} else if (cardImportAttribute.getLinkCodeId()!=null){

					whereSqlBuf.append( "\n\t\t\t and not exists(select 1 from attribute_value as "+ alias +" JOIN person as "+ linkedAlias +" ON ( \n" ); // (*)
					whereSqlBuf.append( "\t\t\t 		("+ linkedAlias +".person_id = "+alias+".number_value)  \n" ); 
					whereSqlBuf.append( "\n\t\t and exists(select 1 from attribute_value as "+ linkedAlias2 +" where ( \n" ); // (*)
					whereSqlBuf.append( "\t\t\t 		("+ linkedAlias2 +".card_id = "+linkedAlias+".card_id) AND \n" ); 
					whereSqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t ({0}.attribute_code=''{1}'') AND \n",
							new Object[] {	linkedAlias2, StringEscapeUtils.escapeSql((String)cardImportAttribute.getLinkCodeId().getId()) }
							
					));		
					whereSqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t ({0}.template_id={1}) AND \n",
							new Object[] {	linkedAlias2, StringEscapeUtils.escapeSql(cardImportAttribute.getLinkTemplateId().getId().toString()) }
							
					));		
					ObjectId linkAttributeId = cardImportAttribute.getLinkCodeId();
					final Class linkAttributeClass = linkAttributeId.getType();
					if (linkAttributeClass.equals(StringAttribute.class)||linkAttributeClass.equals(TextAttribute.class)){
						whereSqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t not({0}.string_value in ({1})) \n",
								new Object[] {	linkedAlias2, generateValuesList(StringEscapeUtils.escapeSql((String)cardImportAttribute.getValue()), ",", "'") }
								
						));
					} else if (linkAttributeClass.equals(ListAttribute.class)){
						whereSqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t not({0}.value_id in({1})) \n",
								new Object[] {	linkedAlias2, generateValuesList(StringEscapeUtils.escapeSql((String)cardImportAttribute.getValue()), ",", null) }
								
						));
					} else if (linkAttributeClass.equals(CardLinkAttribute.class)||attributeClass.equals(IntegerAttribute.class)||attributeClass.equals(LongAttribute.class)||attributeClass.equals(PersonAttribute.class)){
						whereSqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t not({0}.number_value in ({1})) \n",
								new Object[] {	linkedAlias2, generateValuesList(StringEscapeUtils.escapeSql((String)cardImportAttribute.getValue()), ",", null) }
								
						));
					} else if (linkAttributeClass.equals(DateAttribute.class)){
						SimpleDateFormat formatter=new SimpleDateFormat("dd.MM.yyyy");
						Date dateValue = formatter.parse(cardImportAttribute.getValue());
						dateValue = DateUtils.toUTC(dateValue);
						whereSqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t not({0}.date_value in ({1}))\n",
								new Object[] {	linkedAlias2, generateValuesList(StringEscapeUtils.escapeSql((String)cardImportAttribute.getValue()), ",", "'") }));
					} else {
						throw new DataException("card.import.incorrect.type.or.format", new Object[]{linkAttributeId.getId(), StringEscapeUtils.escapeSql(cardImportAttribute.getLinkTemplateId().getId().toString())});
					}
					whereSqlBuf.append( "\n\t\t ))" ); // (*)
					whereSqlBuf.append( "\t\t\t) WHERE		("+ alias +".card_id = c.card_id) AND \n" ); 
					whereSqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t ({0}.attribute_code=''{1}'') \n",
							new Object[] {	alias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getPrimaryCodeId().getId()) }
							
					));		
					whereSqlBuf.append( "\n\t\t\t )" ); // (*)
				} else {
					throw new DataException("card.import.incorrect.type.or.format", new Object[]{cardImportAttribute.getCustomLinkCodeId(), StringEscapeUtils.escapeSql(cardImportAttribute.getLinkTemplateId().getId().toString())});
				}
				
				// далее проверяем, чтобы были ссылочные карточки с атрибутами, заполненными всеми входными значениями
				String[] values = cardImportAttribute.getValue().split("]>");
				for(String tempValue: values){ 
					String value = tempValue.replaceAll("<\\[", "").replaceAll("]>", "").trim(); 
					if (value==null||value.isEmpty()){
						continue;
					}
					final String inner_alias = "av_" + i++; 
					final String inner_linkedAlias = "av_" + i++;// очередной синоним таблицы
					final String inner_linkedAlias2 = "av_" + i++;// ещё один синоним таблицы
					// если ссылочный атрибут - логин пользователя, то ищем пользователя по логину
					sqlBuf.append( "\n\t\t JOIN attribute_value as "+ inner_alias +" ON ( \n" ); // (*)
					sqlBuf.append( "\t\t\t	("+ inner_alias +".card_id = c.card_id) AND \n" ); 
					sqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t ({0}.attribute_code=''{1}'') \n",
							new Object[] {	inner_alias, StringEscapeUtils.escapeSql((String)cardImportAttribute.getPrimaryCodeId().getId()) }
							
					));
					if (cardImportAttribute.getCustomLinkCodeId()==CUSTOM_ATTRIBUTE_CODES.LOGIN){
						sqlBuf.append( "\n\t\t )" ); // (*)
						sqlBuf.append( "\n\t\t JOIN person as "+ inner_linkedAlias +" ON ( \n" ); // (*)
						sqlBuf.append( "\t\t\t 		("+ inner_linkedAlias +".person_id = "+inner_alias+".number_value) AND \n" ); 
						sqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t ({0}.person_login = ''{1}'') \n",
								new Object[] {	inner_linkedAlias, value }
								
						));
					} else if (cardImportAttribute.getLinkCodeId()!=null){
						sqlBuf.append( "\n\t\t )" ); // (*)
	
						sqlBuf.append( "\n\t\t JOIN person as "+ inner_linkedAlias +" ON ( \n" ); // (*)
						sqlBuf.append( "\t\t\t 		("+ inner_linkedAlias +".person_id = "+inner_alias+".number_value)  \n" ); 
						sqlBuf.append( "\n\t\t )" ); // (*)
	
						sqlBuf.append( "\n\t\t JOIN attribute_value as "+ inner_linkedAlias2 +" ON ( \n" ); // (*)
						sqlBuf.append( "\t\t\t 		("+ inner_linkedAlias2 +".card_id = "+inner_linkedAlias+".card_id) AND \n" ); 
						sqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t ({0}.attribute_code=''{1}'') AND \n",
								new Object[] {	inner_linkedAlias2, StringEscapeUtils.escapeSql((String)cardImportAttribute.getLinkCodeId().getId()) }
								
						));		
						ObjectId linkAttributeId = cardImportAttribute.getLinkCodeId();
						final Class linkAttributeClass = linkAttributeId.getType();
						if (linkAttributeClass.equals(StringAttribute.class)||linkAttributeClass.equals(TextAttribute.class)){
							sqlBuf.append( MessageFormat.format( 
									"\t\t\t\t\t ({0}.string_value=''{1}'') \n",
									new Object[] {	inner_linkedAlias2, value }
									
							));
						} else if (linkAttributeClass.equals(ListAttribute.class)){
							sqlBuf.append( MessageFormat.format( 
									"\t\t\t\t\t ({0}.value_id = {1}) \n",
									new Object[] {	inner_linkedAlias2, value }
									
							));
						} else if (linkAttributeClass.equals(DateAttribute.class)){
							SimpleDateFormat formatter=new SimpleDateFormat("dd.MM.yyyy");
							Date dateValue = formatter.parse(cardImportAttribute.getValue());
							dateValue = DateUtils.toUTC(dateValue);
							sqlBuf.append( MessageFormat.format( 
									"\t\t\t\t\t ({0}.date_value=''{1}'')\n",
									new Object[] {	inner_linkedAlias2, value }));
							sqlBuf.append( "\n\t\t )" ); // (*)
						} else if (linkAttributeClass.equals(CardLinkAttribute.class)||linkAttributeClass.equals(IntegerAttribute.class)||linkAttributeClass.equals(LongAttribute.class)||linkAttributeClass.equals(PersonAttribute.class)){
							sqlBuf.append( MessageFormat.format( 
									"\t\t\t\t\t ({0}.number_value={1}) \n",
									new Object[] {	inner_linkedAlias2, value }
									
							));
						} else {
							throw new DataException("card.import.incorrect.type.or.format", new Object[]{linkAttributeId.getId(), StringEscapeUtils.escapeSql(cardImportAttribute.getLinkTemplateId().getId().toString())});
						}
					} else {
						throw new DataException("card.import.incorrect.type.or.format", new Object[]{cardImportAttribute.getCustomLinkCodeId(), StringEscapeUtils.escapeSql(cardImportAttribute.getLinkTemplateId().getId().toString())});
					}
					sqlBuf.append( "\n\t\t )" ); // (*)
				}
				//sqlBuf.append( "\n\t\t )" ); // (*)

			// ищем карточку или персону по логину
			} else if (attributeId==null&&cardImportAttribute.getCustomPrimaryCodeId()==CUSTOM_ATTRIBUTE_CODES.LOGIN){
				sqlBuf.append( "\n\t\t JOIN person as "+ alias +" ON ( \n" ); // (*)
				sqlBuf.append( "\t\t\t 		("+ alias +".card_id = c.card_id) AND \n" ); 
				sqlBuf.append( MessageFormat.format( 
						"\t\t\t\t\t ({0}.person_login in ({1})))  \n",
						new Object[] {	alias, generateValuesList(StringEscapeUtils.escapeSql((String)cardImportAttribute.getValue()), ",", "'") }
						
				));		
			} else {
				throw new DataException("card.import.incorrect.type.or.format", new Object[]{cardImportAttribute, StringEscapeUtils.escapeSql(templateId.getId().toString())});
			}
		}
		if (!isPerson){
			sqlBuf.append( MessageFormat.format( 
					"\nwhere (c.template_id={0}) ",
					new Object[] {	StringEscapeUtils.escapeSql(templateId.getId().toString()) }
			));
			sqlBuf.append( MessageFormat.format( 
					"\n and not(c.status_id in ({0})) ",
					new Object[] { ImportCards.EXCLUDE_SEARCH_STATES}
			));
			sqlBuf.append(whereSqlBuf);
			sqlBuf.append( "\n order by c.card_id desc " ); // (*)
			
			List<ObjectId> cardIds = getJdbcTemplate().query(sqlBuf.toString(),
					new Object[] { },
					new int[] {  },
						new RowMapper() {
							public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					            ObjectId cardId = new ObjectId(Card.class, rs.getLong(1));
					            return cardId;
							}
						});	
			// после того, как нашли, загрузим её из БД
			if (cardIds == null || cardIds.isEmpty())
				return null;
			if (cardIds.size()>1){
				if (logger.isWarnEnabled()){
					logger.warn(MessageFormat.format("Found {0} cards for template {1} whith card attributes {2}.", new Object[]{cardIds.size(), templateId.getId(), cardAttributes.toString()}));
				}
			}
			return cardIds;
		} else {
			sqlBuf.append( "\nwhere (1=1) ");
			sqlBuf.append( MessageFormat.format( 
					"\n and not exists(select 1 from card c11 where c11.status_id in ({0}) and c11.card_id = c.card_id) ",
					new Object[] { ImportCards.EXCLUDE_SEARCH_STATES}
			));
			sqlBuf.append(whereSqlBuf);
			sqlBuf.append( "\n order by c.person_id desc " ); // (*)
			
			List<ObjectId> personIds = getJdbcTemplate().query(sqlBuf.toString(),
					new Object[] { },
					new int[] {  },
						new RowMapper() {
							public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					            return new ObjectId(Person.class, rs.getLong(1));
							}
						});	
			// после того, как нашли, загрузим её из БД
			if (personIds == null || personIds.isEmpty())
				return null;
			if (personIds.size()>1){
				if (logger.isWarnEnabled()){
					logger.warn(MessageFormat.format("Found {0} persons whith card attributes {1}.", new Object[]{personIds.size(), cardAttributes.toString()}));
				}
			}
			return personIds;
		}
	}

	/**
	 * Найти в БД id справочного значения по его имени и по справочнику для входного атрибута
	 * @param value - справочное значение
	 * @param attrCode - входной справочный атрибут
	 * @return id справочного значения в БД (если несколько, то берем первый)
	 * throws DataException 
	 */
	private ReferenceValue findReferenceValue(String value, ObjectId attrCode) throws DataException{
		if (logger.isDebugEnabled()){
			logger.debug(MessageFormat.format("Try find reference value for value {0} in attribute {1}", new Object[]{value, attrCode.getId()}));
		}
		// искать будем через обычный sql-запрос
		final String sql = 	"select value_id from values_list vl \n" +
		"	join attribute_option ao \n" +
		"		on vl.ref_code = ao.option_value \n" +
		"WHERE \n" +
		"	value_rus = ? \n" +
		"	and ao.attribute_code = ? \n" +
		"	and ao.option_code = 'REFERENCE'\n" +
		"order by value_id asc";
		
		List<ReferenceValue> referenceCodes = getJdbcTemplate().query(sql,
					new Object[] { value, attrCode.getId()},
					new int[] { Types.VARCHAR, Types.VARCHAR },
						new RowMapper() {
							public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					            ReferenceValue referenceValue = new ReferenceValue();
					            referenceValue.setId(new ObjectId(ReferenceValue.class, rs.getLong(1)));
					            return referenceValue;
							}
						});	
		if (referenceCodes==null||referenceCodes.isEmpty()){
			throw new DataException("card.import.reference.value.not.found", new Object[]{value, attrCode.getId()});
		}
		if (referenceCodes.size()>1){
			logger.warn("More than one reverence code for value "+value+" in attribute "+attrCode.getId()+", get first.");
		}
		return referenceCodes.get(0);
	}

	/**
	 * Найти в БД id справочного значения по его имени и имени справочника  
	 * @param value - справочное значение
	 * @return id справочного значения в БД (если несколько, то берем первый)
	 * throws DataException 
	 */
	private ReferenceValue findReferenceValue(String value, String refName) throws DataException{
		if (logger.isDebugEnabled()){
			logger.debug(MessageFormat.format("Try find reference value for value {0} and reference name {1}", new Object[]{value, refName}));
		}
		// искать будем через обычный sql-запрос
		final String sql = 	"select value_id from values_list vl \n" +
							"	join reference_list rl \n" +
							"		on rl.ref_code = vl.ref_code \n" +
							"WHERE \n" +
							"	vl.value_rus = ? \n" +
							"	and rl.ref_code = ? \n" +
							"order by vl.value_id asc";
		
		List<ReferenceValue> referenceCodes = getJdbcTemplate().query(sql,
					new Object[] { value, refName},
					new int[] { Types.VARCHAR, Types.VARCHAR },
						new RowMapper() {
							public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					            ReferenceValue referenceValue = new ReferenceValue();
					            referenceValue.setId(new ObjectId(ReferenceValue.class, rs.getLong(1)));
					            return referenceValue;
							}
						});	
		if (referenceCodes==null||referenceCodes.isEmpty()){
			throw new DataException("card.import.reference.value.not.found2", new Object[]{value, refName});
		}
		if (referenceCodes.size()>1){
			logger.warn("More than one reverence code for value "+value+" in reference "+refName+", get first.");
		}
		return referenceCodes.get(0);
	}
	/**
	 * Обновить список входных атрибутов во входной карточке  
	 * @param card - входная карточка
	 * @param cardAttributes - список обновляемых атрибутов со значениями
	 * @return true/false
	 * @throws DataException 
	 */
	private Card updateCard(ObjectId cardId, List<ImportAttribute> cardAttributes) throws DataException{
		if (cardId==null)
			return null;
		if (logger.isDebugEnabled()){
			logger.debug(MessageFormat.format("Update card {0} by attributes {1}", new Object[]{cardId.getId(), cardAttributes}));
		}
		boolean locked = false;
		Card card = null;
		try {
			// сначала загружаем карточку из БД
			final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
			// TODO: чтобы избежать проверки прав доступа это в общем-то хак, нужно придумать что-то другое
			cardQuery.setAccessChecker(null);
			cardQuery.setId(cardId);
			cardQuery.setSessionId(getSessionId());
			card = (Card) getDatabase().executeQuery( getUser(), cardQuery);
			// потом блокируем
			lockCard(card);
			locked = true; 
			// формируем список заполняемых атрибутов (при необходимости находим ссылки на другие карточки или справочники)
			List<Attribute> attributes = new ArrayList<Attribute>();
			for (ImportAttribute importAttribute : cardAttributes){
				// заполняем атрибуты карточки 
				Attribute nextAttribute = generateAttribute(importAttribute, card);
				//attributes.add(nextAttribute);
			}
			// сохраняем карточку в БД
			//card.getAttributes().addAll(attributes);
			saveCard(card);
			return card;
		} catch (Exception e){
			throw new DataException("card.import.update.error", new Object[]{card.getId().getId(), e.getMessage()});
		} finally {
			if (locked&&card!=null)
				// в конце разблокируем
				unLockCard(card);
		}
	}
	
	/**
	 * Создать в БД новую карточку определенного шаблона и заполнить в ней список входных атрибутов  
	 * @param templateId - входной шаблон
	 * @param cardAttributes - список заполненных атрибутов
	 * @return Card
	 * @throws DataException 
	 */
	private Card createNewCard(ObjectId templateId, List<ImportAttribute> cardAttributes) throws DataException{
		if (templateId==null)
			return null;
		if (logger.isDebugEnabled()){
			logger.debug(MessageFormat.format("Try create new card with template {0} and attributes {1}", new Object[]{templateId.getId(), cardAttributes}));
		}
		// создаем карточку
		CreateCard action = new CreateCard(templateId);
		
		ActionQueryBase aqb = getQueryFactory().getActionQuery(action);
		aqb.setAction(action);
		aqb.setAsync(false);
		aqb.setSessionId(getSessionId());
		Card card = (Card)getDatabase().executeQuery(getUser(), aqb);
		
		// формируем список заполняемых атрибутов (при необходимости находим ссылки на другие карточки или справочники)
		List<Attribute> attributes = new ArrayList<Attribute>();
		for (ImportAttribute importAttribute : cardAttributes){
			// заполняем атрибуты карточки
			Attribute cardAttribute = generateAttribute(importAttribute, card);
		}
		// сохраняем карточку в БД
		saveCard(card);
		if (card!=null){
			// в конце разблокируем (дважды - особенность блокировок)
			unLockCard(card);
			unLockCard(card);
		}
		return card;
	}
	
	/**
	 * Сохранение входной карточки
	 * @param card - входная карточка
	 * @throws DataException
	 */
	private void saveCard(Card card) throws DataException{
		try{
			if (logger.isDebugEnabled()){
				logger.debug(MessageFormat.format("Save card {0}", new Object[]{(card.getId()==null?"new":card.getId().getId())}));
			}
			SaveQueryBase aqb = getQueryFactory().getSaveQuery(card);
			aqb.setObject(card);
			//aqb.setAsync(false);
			aqb.setSessionId(getSessionId());
			final ObjectId id = (ObjectId)getDatabase().executeQuery(getUser(), aqb);
			card.setId(Long.parseLong("" + id.getId()));
		} catch (Exception e){
			throw new DataException("card.import.save.error", new Object[]{(card.getId()==null?"":card.getId().getId()), e.getMessage()});
		}
	}

	/**
	 * Блокировка входной карточки
	 * @param card - входная карточка
	 * @throws DataException
	 */
	private void lockCard(Card card) throws DataException{
		if (logger.isDebugEnabled()){
			logger.debug(MessageFormat.format("Lock card {0}", new Object[]{card.getId().getId()}));
		}
		LockObject action = new LockObject(card.getId());
		
		ActionQueryBase aqb = getQueryFactory().getActionQuery(action);
		aqb.setAction(action);
		aqb.setAsync(false);
		aqb.setSessionId(getSessionId());
		getDatabase().executeQuery(getUser(), aqb);
	}

	/**
	 * Разблокировка входной карточки
	 * @param card - входная карточка
	 * @throws DataException
	 */
	private void unLockCard(Card card) throws DataException{
		if (logger.isDebugEnabled()){
			logger.debug(MessageFormat.format("Unlock card {0}", new Object[]{card.getId().getId()}));
		}
		UnlockObject action = new UnlockObject(card.getId());
		
		ActionQueryBase aqb = getQueryFactory().getActionQuery(action);
		aqb.setAction(action);
		aqb.setAsync(false);
		aqb.setSessionId(getSessionId());
		getDatabase().executeQuery(getUser(), aqb);
	}
	/**
	 * На основе информации об импортируемом атрибуте формируем стандартный атрибут для карточки с заполненным значением
	 * @param cardImportAttribute
	 * @return объект класса Attribute
	 * @throws DataException
	 */
	private Attribute generateAttribute(ImportAttribute cardImportAttribute, Card card) throws DataException{
		if (logger.isDebugEnabled()){
			logger.debug(MessageFormat.format("Fill in card {0} attribute by {1}", new Object[]{(card.getId()==null?card:card.getId().getId()), cardImportAttribute}));
		}
		// создаём атрибуты основных типов, у которых могут быть проверяемые значения, иначе выкидываем исключение
		try{
			ObjectId attributeId = cardImportAttribute.getPrimaryCodeId();
			CUSTOM_ATTRIBUTE_CODES customAttributeId = cardImportAttribute.getCustomPrimaryCodeId();

			Attribute attr = null;
			if(attributeId!=null) {
				attr = card.getAttributeById(attributeId);
			} else if (customAttributeId!=null){
				if (customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.LOGIN)) {
					attr = card.getAttributeById(ImportCards.USER_LOGIN_CARD_ATTRIBUTE);
				} else if (customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.GROUP_CODE)
						||customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.GROUP_NAME_RUS)
						||customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.GROUP_NAME_ENG)
						||customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.ROLE_CODE)
						||customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.ROLE_NAME_RUS)
						||customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.ROLE_NAME_ENG)){
					attr = card.getAttributeById(ImportCards.USER_ROLES_CARD_ATTRIBUTE);
				}
			} 
			// если атрибута в исходной карточке нет, то значит пришли некорректные данные
			if (attr==null){
				throw new DataException("card.import.atribute.not.in.card", new Object[]{cardImportAttribute, (card.getId()==null?card:card.getId().getId())});
			}
			final Class attributeClass = attr.getClass();
			if (attributeClass.equals(StringAttribute.class)){
				((StringAttribute) attr).setValue(cardImportAttribute.getValue());
			} else if (attributeClass.equals(TextAttribute.class)){
				((TextAttribute) attr).setValue(cardImportAttribute.getValue());
			} else if (attributeClass.equals(CardLinkAttribute.class)){
				boolean isMultiValued = ((CardLinkAttribute)attr).isMultiValued();
				String[] values = cardImportAttribute.getValue().split("]>");
				if (!isMultiValued&&values.length>1){
					throw new DataException(MessageFormat.format("card.import.single.value.attribute.error", new Object[]{attributeId.getId()}));
				}
				((CardLinkAttribute)attr).clear();
				for(String tempValue: values){ 
					String value = tempValue.replaceAll("<\\[", "").replaceAll("]>", "").trim(); 
					if (value==null||value.isEmpty()){
						continue;
					}
					final Card linkedCard = new Card(); 
					// для кардлников учитываем, что возможны ссылки на значения в других карточках
					if (cardImportAttribute.getLinkCodeId()!=null||cardImportAttribute.getCustomLinkCodeId()==CUSTOM_ATTRIBUTE_CODES.LOGIN){
						List<ImportAttribute> linkCardAttributes = new ArrayList<ImportAttribute>(); 
						ImportAttribute linkCardAttribute = new ImportAttribute();
						linkCardAttribute.setPrimaryCodeId(cardImportAttribute.getLinkCodeId());
						linkCardAttribute.setCustomPrimaryCodeId(cardImportAttribute.getCustomLinkCodeId());
						linkCardAttribute.setValue(value);
						linkCardAttributes.add(linkCardAttribute);
						final List<ObjectId> cards = findCardOrPerson(cardImportAttribute.getLinkTemplateId(), linkCardAttributes, false);
						final ObjectId cardId = (cards==null)?null:cards.get(0);	// в случае поиска карточки или персоны для заполнения, береётся только первая найденная карточка или персона
						if (cardId==null||cardId.getId()==null){
							if (cardImportAttribute.getLinkCodeId()!=null){
								throw new DataException("card.import.card.not.found", new Object[]{cardImportAttribute.getLinkTemplateId().getId(), cardImportAttribute.getLinkCodeId().getId(), value});
							} else if (cardImportAttribute.getCustomLinkCodeId()==CUSTOM_ATTRIBUTE_CODES.LOGIN){
								throw new DataException("card.import.card.person.not.found", new Object[]{value});
							} else {
								throw new DataException("card.import.unknown.error", new Object[]{cardImportAttribute.getLinkTemplateId().getId(), cardImportAttribute.getLinkCodeId().getId()});
							}
						}
						linkedCard.setId(cardId);
					} else{
						linkedCard.setId(new Long(value));
					}
					final CardLinkAttribute linksAttr = (CardLinkAttribute)attr;
					// аккуратно задаем id для отображения значения атрибута...
					linksAttr.addLabelLinkedCard(linkedCard);
				}
			} else if (attributeClass.equals(ListAttribute.class)&&cardImportAttribute.isReference()){
				if (cardImportAttribute.getValue()==null||cardImportAttribute.getValue().isEmpty()){
					if (logger.isWarnEnabled()){
						logger.warn(MessageFormat.format("Value for attribute {0} in card {1} is undefined. Clear attribute value.", new Object[]{attr.getId().toString(), (card.getId()==null?card:card.getId().getId())} ));
					}
					attr.clear();
					return attr;
				}
				ReferenceValue refValue = (cardImportAttribute.getReferenceName()!=null&&!(cardImportAttribute.getReferenceName().isEmpty()))?findReferenceValue(cardImportAttribute.getValue(), cardImportAttribute.getReferenceName()):findReferenceValue(cardImportAttribute.getValue(), attributeId);
				((ListAttribute) attr).setValue(refValue);
			} else if (attributeClass.equals(ListAttribute.class)&&!cardImportAttribute.isReference()){
				if (cardImportAttribute.getValue()==null||cardImportAttribute.getValue().isEmpty()){
					if (logger.isWarnEnabled()){
						logger.warn(MessageFormat.format("Value for attribute {0} in card {1} is undefined. Clear attribute value.", new Object[]{attr.getId().toString(), (card.getId()==null?card:card.getId().getId())} ));
					}
					attr.clear();
					return attr;
				}
				ReferenceValue refValue = new ReferenceValue();
				refValue.setId(new Long(cardImportAttribute.getValue()));
				((ListAttribute) attr).setValue(refValue);
			} else if (attributeClass.equals(DateAttribute.class)){
				if (cardImportAttribute.getValue()==null||cardImportAttribute.getValue().isEmpty()){
					if (logger.isWarnEnabled()){
						logger.warn(MessageFormat.format("Value for attribute {0} in card {1} is undefined. Clear attribute value.", new Object[]{attr.getId().toString(), (card.getId()==null?card:card.getId().getId())} ));
					}
					attr.clear();
					return attr;
				}
				SimpleDateFormat formatter=new SimpleDateFormat("dd.MM.yyyy");
				Date dateValue = formatter.parse(cardImportAttribute.getValue());
				((DateAttribute) attr).setValue(dateValue);
			} else if (attributeClass.equals(IntegerAttribute.class)){
				if (cardImportAttribute.getValue()==null||cardImportAttribute.getValue().isEmpty()){
					if (logger.isWarnEnabled()){
						logger.warn(MessageFormat.format("Value for attribute {0} in card {1} is undefined. Clear attribute value.", new Object[]{attr.getId().toString(), (card.getId()==null?card:card.getId().getId())} ));
					}
					attr.clear();
					return attr;
				}
				((IntegerAttribute) attr).setValue((new Long(cardImportAttribute.getValue())).intValue());
			} else if (attributeClass.equals(LongAttribute.class)){
				if (cardImportAttribute.getValue()==null||cardImportAttribute.getValue().isEmpty()){
					if (logger.isWarnEnabled()){
						logger.warn(MessageFormat.format("Value for attribute {0} in card {1} is undefined. Clear attribute value.", new Object[]{attr.getId().toString(), (card.getId()==null?card:card.getId().getId())} ));
					}
					attr.clear();
					return attr;
				}
				((LongAttribute) attr).setValue(new Long(cardImportAttribute.getValue()));
			} else if (attributeClass.equals(PersonAttribute.class)){
				if (cardImportAttribute.getValue()==null||cardImportAttribute.getValue().isEmpty()){
					if (logger.isWarnEnabled()){
						logger.warn(MessageFormat.format("Value for attribute {0} in card {1} is undefined. Clear attribute value.", new Object[]{attr.getId().toString(), (card.getId()==null?card:card.getId().getId())} ));
					}
					attr.clear();
					return attr;
				}
				boolean isMultiValued = ((PersonAttribute)attr).isMultiValued();
				String[] values = cardImportAttribute.getValue().split("]>");
				if (!isMultiValued&&values.length>1){
					throw new DataException(MessageFormat.format("card.import.single.value.attribute.error", new Object[]{attributeId.getId()}));
				}
				((PersonAttribute)attr).clear();
				for(String tempValue: values){ 
					String value = tempValue.replaceAll("<\\[", "").replaceAll("]>", "").trim(); 
					if (value==null||value.isEmpty()){
						continue;
					}
					final Person person = new Person();
					if (cardImportAttribute.getLinkCodeId()!=null||cardImportAttribute.getCustomLinkCodeId()==CUSTOM_ATTRIBUTE_CODES.LOGIN){
						List<ImportAttribute> linkCardAttributes = new ArrayList<ImportAttribute>(); 
						ImportAttribute linkCardAttribute = new ImportAttribute();
						linkCardAttribute.setPrimaryCodeId(cardImportAttribute.getLinkCodeId());
						linkCardAttribute.setCustomPrimaryCodeId(cardImportAttribute.getCustomLinkCodeId());
						linkCardAttribute.setValue(value);
						linkCardAttributes.add(linkCardAttribute);
						final List<ObjectId> persons = findCardOrPerson(cardImportAttribute.getLinkTemplateId(), linkCardAttributes, true);
						final ObjectId personId = (persons==null)?null:persons.get(0);	// в случае поиска карточки или персоны для заполнения, береётся только первая найденная карточка или персона
						if (personId==null||personId.getId()==null){
							if (cardImportAttribute.getLinkCodeId()!=null){
								throw new DataException("card.import.person.card.not.found", new Object[]{cardImportAttribute.getLinkCodeId().getId(), value});
							} else if (cardImportAttribute.getCustomLinkCodeId()==CUSTOM_ATTRIBUTE_CODES.LOGIN){
								throw new DataException("card.import.person.not.found", new Object[]{value});
							} else {
								throw new DataException("card.import.unknown.error", new Object[]{cardImportAttribute.getLinkTemplateId().getId(), cardImportAttribute.getLinkCodeId().getId()});
							}
						}
						person.setId(personId);
					} else {
						person.setId(new Long(value));
					}
					final PersonAttribute pa = (PersonAttribute)attr;
					if (pa.getValues() == null) {
						pa.setValues(new ArrayList());
					}
					pa.getValues().add(person);
				}
			} else if (attributeClass.equals(PortalUserLoginAttribute.class)) {
				((PortalUserLoginAttribute) attr).setValue(cardImportAttribute.getValue());
			} else if (attributeClass.equals(UserRolesAndGroupsAttribute.class)) {
				ArrayList<String> groupRolesValues = new ArrayList(); 
				Set<String> groupRolesCodes = new HashSet<String>(); 
				Set<String> rolesCodes = new HashSet<String>(); 
				UserRolesAndGroupsAttribute attrGroupRoles = (UserRolesAndGroupsAttribute)attr;
				// задаем пустые списки ролей и групп, если этих значений ещё не задано
				if (attrGroupRoles.getAssignedGroups() == null){
					attrGroupRoles.setAssignedGroups(groupRolesCodes);
				}
				if (attrGroupRoles.getAssignedRoles() == null){
					attrGroupRoles.setAssignedRoles(rolesCodes);
				}
				((UserRolesAndGroupsAttribute)attr).setExcludedGroupRoleCodes(new HashMap<String, Set<String>>());
				// генерируем массив входных значений, если он вообще не пустой
				String[] values = cardImportAttribute.getValue().split("]>");
					for(String tempValue: values){ 
						String value = tempValue.replaceAll("<\\[", "").replaceAll("]>", "").trim();
						if (value!=null&&!value.isEmpty()){
							groupRolesValues.add(value);
						}
					}
					
				if(customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.GROUP_CODE)
						||customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.GROUP_NAME_RUS)
						||customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.GROUP_NAME_ENG)){
					// пройдемся по каждому значению из списка и проверим на наличие объекта в БД
					StringBuffer errorMsg = new StringBuffer(); 
					for(String value:groupRolesValues){
						try{
							String groupCode = findGroupByValue(value, customAttributeId);
							groupRolesCodes.add(groupCode);
							//Set<String> roles = findRolesByGroup(groupCode);
							//rolesCodes.addAll(roles);
						} catch (Exception e){
							if (errorMsg.length()>0){
								errorMsg.append(", ");
							}
							errorMsg.append(e.getMessage());
							continue;
						}
					}
					if (errorMsg.length()>0){
						throw new DataException(errorMsg.toString());
					}
					attrGroupRoles.setAssignedGroups(groupRolesCodes);
				}
				
				if(customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.ROLE_CODE)
						||customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.ROLE_NAME_RUS)
						||customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.ROLE_NAME_ENG)){
					// пройдемся по каждому значению из списка и проверим на наличие объекта в БД
					StringBuffer errorMsg = new StringBuffer(); 
					for(String value:groupRolesValues){
						try{
							String groupCode = findRoleByValue(value, customAttributeId);
							rolesCodes.add(groupCode);
						} catch (Exception e){
							if (errorMsg.length()>0){
								errorMsg.append(", ");
							}
							errorMsg.append(e.getMessage());
							continue;
						}
					}
					if (errorMsg.length()>0){
						throw new DataException(errorMsg.toString());
					}
					attrGroupRoles.setAssignedRoles(rolesCodes);
				}
			} else {
				throw new DataException("card.import.incorrect.type.or.format.for.import.attribute", new Object[]{attr.getId().getId()});
			}
			return attr;
		} catch (Exception e){
			String attributeCode = (cardImportAttribute.getPrimaryCodeId()!=null?cardImportAttribute.getPrimaryCodeId().getId().toString():((cardImportAttribute.getCustomPrimaryCodeId()!=null)?cardImportAttribute.getCustomPrimaryCodeId().name():cardImportAttribute.toString()));
			throw new DataException("card.import.fill.attribute.error", new Object[]{attributeCode, (card.getId()==null?"":card.getId().getId()), e.getMessage()});
		}
	}
	
	/**
	 * Превращаем текстовый список значений, заключенных в скобки <[...]> в список этих же значений, 
	 * разделенных определенным входным символом (например запятой) и при необходимости экранированных входным символом (к примеру ')   
	 * @param value - входной список
	 * @param delimChar - символ разделения
	 * @param escapeChar - экранирующий символ
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
		for (Iterator itr = importAttributes.iterator(); itr.hasNext();){
			ImportAttribute object = (ImportAttribute)itr.next();
			resultMsg.append(object.getTroubleMessage());
			if (itr.hasNext()){
				resultMsg.append(", ");
			}
		}
		return resultMsg.toString();
	}

	private ArrayList<String> generateSuccessList(Collection<Card> successCards){
		ArrayList<String> result = new ArrayList<String>();
		for (Iterator itr = successCards.iterator(); itr.hasNext();){
			Card object = (Card)itr.next();
			Attribute attr = object.getAttributeById(ImportCards.SUCCESS_CARD_SHOW_ATTRIBUTE);
			if (attr==null){
				result.add(object.getId().getId().toString());
			} else {
				result.add(attr.getStringValue());
			}
		}
		// здесь по идее ещё надо отсортировать по возрастанию
		if (result!=null&&!result.isEmpty()){
			String[] resultArray = new String[result.size()];
			result.toArray(resultArray); 
			Arrays.sort(resultArray);
			result.clear();
			Collections.addAll(result, resultArray);
		}
		return result;
	}
	
	private String findGroupByValue(String value, CUSTOM_ATTRIBUTE_CODES customAttributeId) throws DataException{
		if (logger.isDebugEnabled()){
			logger.debug(MessageFormat.format("Try find system group with {0} equal {1}", new Object[]{customAttributeId.name(), value}));
		}
		
		// искать будем через обычный sql-запрос
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
		
		// искать будем через обычный sql-запрос
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
	 * Метод возвращает множество ролей из входной группы (может пригодиться при импорте ролей или групп)
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
		
		// искать будем через дочернее квери
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
}