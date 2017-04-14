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
package com.aplana.dbmi.card.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import com.aplana.dbmi.model.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.cms.NavigationPortlet;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.card.FilesAndCommentsUtils.RoundData;
import com.aplana.dbmi.card.FilesAndCommentsUtils.RoundDataFiles;
import com.aplana.dbmi.card.FilesAndCommentsUtils.RoundVisaData;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.web.tag.util.StringUtils;

/**
 * Utility class used to perform commonly used actions within ARM.
 * 
 * @author EStatkevich
 */
public class ARMUtils {

	public static final ObjectId ATTR_ARM_MANAGER = ObjectId.predefined(PersonAttribute.class, "jbr.arm.manager");
	public static final ObjectId ATTR_FIRST_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.firstName");
	public static final ObjectId ATTR_LAST_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.lastName");
	public static final ObjectId ATTR_MIDDLE_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.middleName");
	public static final ObjectId ATTR_PERSON_ORG = ObjectId.predefined(CardLinkAttribute.class, "jbr.person.organization");
	public static final ObjectId ATTR_ORG_SHORTNAME = ObjectId.predefined(StringAttribute.class, "jbr.organization.shortName");
	
	public static final ObjectId ROLE_MINISTR = ObjectId.predefined(SystemRole.class, "jbr.minister");
	public static final ObjectId ATTR_NAME = ObjectId.predefined(StringAttribute.class, "name");
	public static final ObjectId ATTR_MATERIAL_NAME = ObjectId.predefined(StringAttribute.class, "jbr.materialName");
	public static final ObjectId CARD_STATE_PUBLISHED = ObjectId.predefined(CardState.class, "published");
	public static final ObjectId CARD_STATE_ACTIVE = ObjectId.predefined(CardState.class, "active");
	
	public static final String INCOMING_NAVIGATOR = "navigator";
	public static final String CONTROL_NAVIGATOR = "navigator2";
	
	private final static Log logger = LogFactory.getLog(ARMUtils.class);

	/**
	 * Retrieves ARM settings for current user.
	 * 
	 * @param serviceBean
	 * @return Card
	 */
	public static Card getArmSettings(DataServiceBean serviceBean) {
		Card armCard = null;
		Search search = new Search();
		search.setByAttributes(true);
		search.addPersonAttribute(ATTR_ARM_MANAGER, Person.ID_CURRENT);
		final List<DataObject> templates = new ArrayList<DataObject>(1);
		templates.add(DataObject.createFromId(new ObjectId(Template.class, 544)));
		search.setTemplates(templates);
		search.setStates(Collections.singleton(CARD_STATE_PUBLISHED));

		/*
		 * ��������� �� ��������. � �������� �������� JBR_ARM_COMPANY �������
		 * PersonAttribute ���������� Person � ������� cardId = null. List
		 * columns = new ArrayList(); SearchResult.Column col = new
		 * SearchResult.Column(); col.setAttributeId(new
		 * ObjectId(PersonAttribute.class, "JBR_ARM_COMPANY"));
		 * columns.add(col); search.setColumns(columns);
		 */
		try {
			final Collection<Card> armCards = SearchUtils.execSearchCards(search, serviceBean);
			if (armCards == null || armCards.size() == 0) {
				throw new Exception("For the current user is not given a card 'Settings ARM Manager'");
			}
			if (armCards.size() > 1) {
				logger.info("For the current user is given more than one card 'Settings ARM Manager'");
			}
			armCard = armCards.iterator().next();
			// ���� ��������� �������� �������� �������� JBR_ARM_COMPANY � search,
			// �� � �������� �������� ������� PersonAttribute ���������� Person � ������� cardId = null.
			// ����� �������� cardId � ������ ��������� ��� ��������.
			armCard = (Card) serviceBean.getById(armCard.getId());
		} catch (Exception e) {
			logger.error("Error retrieving cards 'settings for ARM Manager':", e);
		}
		return armCard;
	}
	
	/**
	 * Returns correspondence between the person's card number and full person name using system user credentials
	 * 
	 * @param ids - person cards ids collection
	 * @return Map Long -> Name
	 */
	public static Map<Long, String> getNameByCardIds(Collection<Long> ids) {
		DataServiceBean serviceBean = new DataServiceBean();
		serviceBean.setAddress("localhost");
		serviceBean.setUser(new SystemUser());
		return getNameByCardIds(serviceBean, ids);
	}

	public static Map<Long, String> getNameByCardIds(DataServiceBean serviceBean, Collection<Long> ids){
		return getNameByCardIds(serviceBean, ids, CardAccess.NO_VERIFYING);
	}

	/**
	 * Returns correspondence between the person's card number and full person name.  
	 * 
	 * @param serviceBean
	 * @param ids - person cards ids collection
	 * @return Map Long -> Name
	 */
	public static Map<Long, String> getNameByCardIds(DataServiceBean serviceBean, Collection<Long> ids, Long permType) {
		final Map<Long, String> map = new LinkedHashMap<Long, String>();

		// Search-�� ��������� ���������� � �����, �������, �������� � ����������� ������
		final Search search = new Search();
		search.setColumns(getFullNameColumns());
		search.getFilter().setCurrentUserRestrict(permType);
		search.setByCode(true);
		final StringBuffer words = new StringBuffer();
		for (final Iterator<Long> i = ids.iterator(); i.hasNext();) {
			words.append(i.next());
			if (i.hasNext()) {
				words.append(",");
			}
		}
		search.setWords(words.toString());
		try {
			final List<Card> cards = SearchUtils.execSearchCards(search, serviceBean);
			// SearchResult res = (SearchResult) serviceBean.doAction(search);
			// final Iterator<Card> i = ((Collection<Card>) res.getCards()).iterator();
			if (cards != null) {
				for (Card c : cards) {
					final StringBuffer name = new StringBuffer();
					Attribute attr = c.getAttributeById(ATTR_LAST_NAME);
					if (attr != null && attr.getStringValue() != null && attr.getStringValue().length() > 0) {
						name.append(attr.getStringValue());
						
						attr = c.getAttributeById(ATTR_FIRST_NAME);
						if (attr != null && attr.getStringValue() != null && attr.getStringValue().length() > 0) {
							name.append(' ').append(attr.getStringValue().substring(0, 1)).append('.');
						}
						attr = c.getAttributeById(ATTR_MIDDLE_NAME);
						if (attr != null && attr.getStringValue() != null && attr.getStringValue().length() > 0) {
							name.append(' ').append(attr.getStringValue().substring(0, 1)).append('.');
						}
						attr = c.getAttributeById(ATTR_PERSON_ORG);
						if (attr != null && !((CardLinkAttribute)attr).getIdsLinked().isEmpty()) {
							ObjectId orgId = ((CardLinkAttribute)attr).getIdsLinked().get(0);
							Card orgCard = (Card) serviceBean.getById(orgId);
							attr = orgCard.getAttributeById(ATTR_ORG_SHORTNAME);
							if (attr != null && attr.getStringValue() != null && attr.getStringValue().length() > 0) {
								name.append(", ").append(attr.getStringValue());
							}
						}
						
					} else {
						attr = c.getAttributeById(ATTR_NAME);
						if (attr != null && attr.getStringValue() != null && attr.getStringValue().length() > 0) {
							name.append(attr.getStringValue().trim());
						}
					}

					final Long id = (Long) c.getId().getId();
					map.put(id, name.toString());
				}
			}
		} catch (Exception e) {
			logger.error("Error search of cards:", e);
		}
		return map;
	}
	
	/**
	 * Prepares columns for {@link Search} to retrieve full person name.
	 * 
	 * @return List<SearchResult.Column>
	 */
	public static List<SearchResult.Column> getFullNameColumns() {
		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		SearchUtils.addColumns(columns, new ObjectId[] { ATTR_FIRST_NAME, ATTR_MIDDLE_NAME, ATTR_LAST_NAME, ATTR_PERSON_ORG, Attribute.ID_NAME });
		return columns;
	}
	
	/**
	 * Checks user role and returns appropriate "switch navigator" button for non-minister users.
	 * 
	 * @param request
	 * @param serviceBean
	 * @return String
	 */
	public static String retrieveSwitchNavigatorButton(PortletRequest request, DataServiceBean serviceBean) {
		if (isNotMinister(serviceBean)) {
			final String controlButton = "<a href=\"/portal/auth/portal/boss/folder/LeftMenu?action=1&navigatorName=navigator2\" class=\"button shield\"></a>";
			final String incomingButton = "<a href=\"/portal/auth/portal/boss/folder/LeftMenu?action=1&navigatorName=navigator\" class=\"button incoming\"></a>";

			String currentNavigator = (String) request.getPortletSession().
                    getAttribute(NavigationPortlet.ATTR_CURRENT_NAVIGATOR_NAME, PortletSession.APPLICATION_SCOPE);
			String newNavigator = (String) request.getPortletSession().
                    getAttribute(NavigationPortlet.ATTR_NEW_NAVIGATOR_NAME, PortletSession.APPLICATION_SCOPE);

			if (null == currentNavigator) {
				return controlButton;
			}

			if (INCOMING_NAVIGATOR.equals(newNavigator) && !newNavigator.equals(currentNavigator)) {
				return controlButton;
			}

			if (CONTROL_NAVIGATOR.equals(newNavigator) && !newNavigator.equals(currentNavigator)) {
				return incomingButton;
			}

			if (INCOMING_NAVIGATOR.equals(currentNavigator)) {
				return controlButton;
			} else {
				return incomingButton;
			}
		}
		return null;
	}
	
	/**
	 * Checks whether the user has the role of Minister.
	 * 
	 * @param serviceBean
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	private static boolean isNotMinister(DataServiceBean serviceBean) {
		try {
			final Person curPerson = (Person) serviceBean.getById(Person.ID_CURRENT);
			for (Role role : (Collection<Role>) curPerson.getRoles()) {
				if (ROLE_MINISTR.equals(role.getSystemRole().getId())) {
					return false;
				}
			}
		} catch (DataException e) {
			logger.error("Error checking minister role:", e);
			return false;
		} catch (ServiceException e) {
			logger.error("Error checking minister role:", e);
			return false;
		}
		return true;
	}
	
	public static JSONArray getAttachmentsJSONData(RoundDataFiles roundDataFiles) throws DataException, ServiceException{
		JSONArray attachments = new JSONArray();
		RoundData[] roundDatas = roundDataFiles.getRoundDatas();
		try {
			for (RoundData roundData : roundDatas) {
				JSONObject roundAttachment = new JSONObject();				
				roundAttachment.put("parrent", getAttachmentsJSONData(roundData.fileCardList));
				roundAttachment.put("child", getVisaSignAttachmentsJSONData(roundData.visaDataList));
				attachments.put(roundAttachment);
			}
		} catch (JSONException e) {
			logger.error("JSON error", e);
			throw new DataException(e);
		} 
		return attachments;
	}
	
	public static JSONArray getVisaSignAttachmentsJSONData(List<RoundVisaData> visaDataList) throws DataException, ServiceException {
		JSONArray attachments = new JSONArray();
		if (visaDataList != null && visaDataList.size() > 0) {
			try {				
				for (RoundVisaData roundVisaData : visaDataList) {
					if(roundVisaData.fileCardList.size()<1){
						continue;
					}
					JSONObject roundVisa = new JSONObject();
					roundVisa.put("author", roundVisaData.userName);
					roundVisa.put("comment", roundVisaData.comment);
					roundVisa.put("files", getAttachmentsJSONData(roundVisaData.fileCardList));
					attachments.put(roundVisa);
				}
			} catch (JSONException e) {
				logger.error("JSON error", e);
				throw new DataException(e);
			}
		}
		return attachments;
	} 
	

	
	public static JSONArray getAttachmentsJSONData(List<Card> attachmentsCardList) throws DataException, ServiceException {
		JSONArray attachments = new JSONArray();
		if (attachmentsCardList != null && attachmentsCardList.size() > 0) {
			try {
				for (Card attachmentCard : attachmentsCardList) {
					JSONObject attachment = new JSONObject();
					StringAttribute materialNameAttr = (StringAttribute) attachmentCard.getAttributeById(ATTR_MATERIAL_NAME);
					if (materialNameAttr != null && StringUtils.hasLength(materialNameAttr.getValue())) {
						attachment.put("name", materialNameAttr.getValue());
					} else {
						StringAttribute nameAttr = (StringAttribute) attachmentCard.getAttributeById(ATTR_NAME);
						attachment.put("name", nameAttr.getValue());
					}
					attachment.put("cardId", attachmentCard.getId().getId().toString());
					attachments.put(attachment);
				}
			} catch (JSONException e) {
				logger.error("JSON error", e);
				throw new DataException(e);
			}
		}
		return attachments;
	}
	
	/**
	 * �������� ����� �������� � ���� <�� ��������, �������� ���������>, �� �������� ���������� �� ���� � ���� ����-�������� attr
	 * @param service
	 * @param attr
	 * @return ����� ��������
	 */
	public static Map<ObjectId, String> getAttachedFilesMap (DataServiceBean service, LinkAttribute attr) {
		List<Card> attachedFiles = CardUtils.loadCardsByCode(service, 
														ObjectIdUtils.numericIdsToCommaDelimitedString(attr.getIdsLinked()), 
														Card.ATTR_ID, Card.ATTR_STATE, Attribute.ID_MATERIAL);
		if(CollectionUtils.isEmpty(attachedFiles)) {
			return new LinkedHashMap<ObjectId, String>();
		}
		Iterator<Card> iter = attachedFiles.iterator();
		Card card;
		while(iter.hasNext()) {
			card = iter.next();
			if(!CARD_STATE_ACTIVE.equals(card.getState())) {
				iter.remove();
			}
		}
		Map<ObjectId, String> attachedFilesMap = new LinkedHashMap<ObjectId, String>();
		for(Card attachedCard : attachedFiles) {
	        MaterialAttribute mAttr = (MaterialAttribute) attachedCard.getAttributeById(Attribute.ID_MATERIAL);
	        if (!mAttr.isEmpty()) {
	        	attachedFilesMap.put(attachedCard.getId(), mAttr.getMaterialName());
	        }
		}
		return attachedFilesMap;
	}
}
