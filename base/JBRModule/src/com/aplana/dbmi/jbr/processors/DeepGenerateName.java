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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.jbr.util.PathAttributeDescriptior;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.processors.CheckStringAttrLength;

/**
 *
 * @author user68 ������:
 *
 *         <pre-process class="DeepGenerateName">
 * 					<!-- ���������� ��� � ������� "��������������� �����" "���� �����������" "��� ������ ���������" "���������� ����� ������ ���������" "����� ������ ���������"-->
 * 					<!-- "regnumber" ('JBR_REGD_REGNUM') "regdate" ('JBR_REGD_DATEREG') "jbr.InfOGSecondName" ('ADMIN_274992') +"jbr.appauthor.name" ('ADMIN_281034')+
 * 						 "jbr.appauthor.patronymic" ('ADMIN_281035') "jbr.InfAboutDocCity" ('ADMIN_277245') "jbr.appauthor.address" ('ADMIN_277248')-->
 * 					<parameter name="format" value="��������� ������� {0} {1} {2} {3} {4} {5} {6} {7}"/>
 * 					<parameter name="dstAttrId" value="NAME"/>
 * 					<parameter name="srcAttrIds" value="string: regnumber; 
 * 					date: regdate;
 * 					link: jbr.AppealAuthor@jbr.InfOGSecondName;
 * 					link: jbr.AppealAuthor@jbr.appauthor.name;
 * 					link: jbr.AppealAuthor@jbr.appauthor.patronymic;
 * 					link: jbr.AppealAuthor@jbr.InfAboutDocCity@NAME#12; -- ������ 12 �������� � �����
 * 					link: jbr.AppealAuthor@jbr.appauthor.address
 * 					;
 * 					link: jbr.ThemeOfQuery@NAME#delim='; '
 * 					"/>
 * </pre-process>
 * (2011/02/10) �������� ����� ������� ��������: forceSaveMode
 * (BR4J00038754) �������� ����� ����������:
 * 					link: jbr.person.organization@jbr.organization.shortName@jbr.organization.fullName;
 * 					����� "@" ����������� �������� ��������� ��������,
 * 					������: ���� �������� ��������, ����� ���������� ��������� �� ������� ����������� �������� ����� �������
 *
 */
public class DeepGenerateName extends ProcessCard {
	private static final long serialVersionUID = 1L;

	public static final String PROCESSORTAG = "GenerateName";
	public static final String PARAM_ATTR_FORMAT = "format";
	/*
	 * ������ �������� ��������� � GenerateName.java �������������, ��� �������
	 * �������� ����� ������� ������������ ����� ����� # �� �����
	 */
	public static final String PARAM_SRC_ATTR_IDS = "srcAttrIds";
	public static final String PARAM_DST_ATTR_ID = "dstAttrId";
	public static final String PARAM_IGNORED_STATES = "ignoredStates";
	/*
	 * ����������� �� ����� ������ ������� �������� ��������� (0<= - ���
	 * �����������, default=60) ������: �����
	 */
	public static final String PARAM_MAXARGSTRLEN = "maxArgStrLen";

	protected Set<ObjectId> sourceIgnoredStateIds;

	// ��������� ���������� ��������� (default = false)
	public static final String PARAM_FORCE_SAVE = "forceSave";

	/**
	 * ����� ������� ������� - ����� ���������� ������� �������� ����� �������� � ��� �������:
	 * 1. CARD - ��� �������� (�� ���������)
	 * 2. ATTRIBUTE - ������ ������� �������
	 * 3. ��� ��������� - �� ��������� ��������
	 */
	public static final String PARAM_FORCE_SAVE_MODE = "forceSaveMode";

	public static final ObjectId DefaultAttrId = Attribute.ID_NAME;
	public static final int DEFAULT_ARGSTRLEN = 60;

	private String fmt = "{0}";
	private final List<PathAttributeDescriptior> attrIdList = new ArrayList<PathAttributeDescriptior>();

	private PathAttributeDescriptior destAttrId = null;

	private int maxAttrValueLen = DEFAULT_ARGSTRLEN;

	private final String TAG_TEMPLATE_NAME_MAINDOC = "%MAINDOC.TEMPLATE.NAME%";
	private final String TAG_TEMPLATE_NAME_SELF = "%SELF.TEMPLATE.NAME%";
	final Map<String, String> tag_macros = new HashMap<String, String>(2);

	@Override
	public Object process() throws DataException {
		prepareParameters();
		processSrcAttributes();
		return null;
	}

	/**
	 * @throws DataException
	 */
	private void prepareParameters() {
		final StringBuilder buf = new StringBuilder("\n Processor preferences: \n");

		// ������������ ��������� - � ������� �� ���� ������������ ��� ��������
		sourceIgnoredStateIds = IdUtils.makeStateIdsList(super.getParameter(PARAM_IGNORED_STATES, ""));

		// ������...
		fmt = getParameter(PARAM_ATTR_FORMAT, "{0}");
		buf.append("\t format      : '").append(fmt).append("'\n");

		// ����������� ����� �������� ������� �������� ��������...
		try {
			maxAttrValueLen = Integer.parseInt(getParameter(PARAM_MAXARGSTRLEN, String.valueOf(DEFAULT_ARGSTRLEN)));
		} catch (Exception e) {
			maxAttrValueLen = DEFAULT_ARGSTRLEN;
		}
		buf.append("\t max attr len: ").append(maxAttrValueLen).append("\n");

		// id �������� �������� ...
		//
		destAttrId = makeObjectId(getParameter(PARAM_DST_ATTR_ID, "NAME"));
		buf.append("\t dest attr   : ").append(destAttrId).append("\n");

		// ID ����������...
		//
		buf.append("\t <source args> \n");
		final String[] sIds = getParameter(PARAM_SRC_ATTR_IDS, "name").trim().split("\\s*(?<!\\\\)[;,]\\s*");// "\\s*[^\\\\][;,]\\s*"
		if (sIds.length > 0) {
			attrIdList.clear();
			int i = 0;
			for (String s : sIds) {
				if (s == null || s.isEmpty())
					continue;
				final PathAttributeDescriptior id = makeObjectId(s);
				attrIdList.add(id);

				i++;
				buf.append(MessageFormat.format("\t\t [{0}] \t {1} \t->\t {2}\n", i, s, id));
			}
		}
		buf.append("\t </source args> \n");

		// if (this.attrIdList.size() < 1) attrIdList.add( DefaultAttrId);

		if (logger.isDebugEnabled())
			logger.debug(buf.toString());
	}

	/**
	 * @param s
	 *            ������ ���� "{���:} id {@id2}", {x} = �������������� ����� x.
	 *            ��������, "string: jbr.organization.shortName"
	 *            "back: jbr.sender: fullname" ���� ��� ������, �� ������
	 *            ���������������� �������� � ����� ������ � �����-���� �����,
	 *            ���� ��� ������, �� ����������� �� "string".
	 * @return
	 * @throws DataException
	 */
	protected static PathAttributeDescriptior makeObjectId(final String s) {
		return (s == null) ? null : new PathAttributeDescriptior(s);
	}

	private String processSrcAttributes() throws DataException {
		final Card card = getCard();
		if (sourceIgnoredStateIds.contains(card.getState())) {
			if (logger.isDebugEnabled())
				logger.debug("Card " + card.getId().getId()+" is in ignored state and won't be processed");
			return null;
		}
		final String tag = "(card=" + card.getId() + ")";
		if (logger.isDebugEnabled())
			logger.debug("preparing arguments values for " + tag + "...");

		if (attrIdList.size() < 1) {
			if (logger.isDebugEnabled())
				logger.debug(tag + ": args list is empty -> exiting");
			return null;
		}

		final StringBuilder buf = new StringBuilder("\n \t [argument] \t\t\t [card attribute] \t\t\t value \n");

		/*
		 * ��������� �������� ������� ����� �������� ...
		 */
		final String selfTemplName = card.getTemplateName();
		if (selfTemplName != null)
			tag_macros.put( TAG_TEMPLATE_NAME_SELF, selfTemplName);

		// ������������ ������ ���������� ��������������...
		final ArrayList<String> args = new ArrayList<String>(attrIdList.size());
		for (PathAttributeDescriptior itemPathDesc : this.attrIdList) {
			final List<Attribute> finalAttrs = new ArrayList<Attribute>();
					
			/*
			 * ���� �������� ������� ���� cardLink ��� backLink, �� �����
			 * ������� �������� ��������� NAME ������ �������� �� ������� ��
			 * ���������, ��� ����� � ���� ������ � attrIds, ��������� �������
			 * NAME
			 */
			final List<ObjectId> attrIds = itemPathDesc.getAttrIds();
			final ObjectId lastAttrId = attrIds.get(attrIds.size() - 1);
			final Class<?> attrClass = lastAttrId.getType();
			if (attrClass.isAssignableFrom(CardLinkAttribute.class)
					|| attrClass.isAssignableFrom(BackLinkAttribute.class)) {
				attrIds.add(DefaultAttrId);
			}

			final Collection<Card> activeCards = new ArrayList<Card>();
			activeCards.add(card);

			//������ ������� �����
			//finalAttrs.add(activeCards.iterator().next().getAttributeById(itemPathDesc.getAttrIds().get(0)));

			for (ObjectId attrId : attrIds) {
				final Collection<Card> newCards = new HashSet<Card>();
				for (Card c : activeCards) {
					Attribute attr = c.getAttributeById(attrId);
					if (attr == null) {
						logger.warn(MessageFormat.format(MSG_CARD_0_HAS_NO_ATTRIBUTE_1, c.getId(), attrId));
						continue;
					}
					//��������� �������������� ��������, ��� �������������� ������� �������� PersonAttribute � ��� ����
					//�� ��������� � ������ ��������� (������� �� ����: BR4J00040140)
					if (attr instanceof LinkAttribute || (attr instanceof PersonAttribute && attrIds.indexOf(attrId) != attrIds.size() - 1)) {
						// ����������� ��������� �������� �� ���������
						final List<Card> list = loadAllLinkedCardsByAttr(c.getId(), attr);
						if (list == null || list.isEmpty()) {
							logger.warn(MessageFormat.format(MSG_CARD_0_HAS_EMPTY_ATTR_1, c.getId(), attrId));
							continue;
						}
						newCards.addAll(list);
					} else {
						final String stringValue = attr.getStringValue();
						// ����� ���������, ������ ���� ������ �������� �������
						if (Card.ATTR_TEMPLATE.equals(attrId)) {
							final StringAttribute strAttr = new StringAttribute();
							strAttr.setId((String) Card.ATTR_TEMPLATE.getId());
							strAttr.setValue(c.getTemplateName());
							finalAttrs.add(strAttr);
						} else if (attr instanceof DateAttribute && stringValue != null && !stringValue.isEmpty()) {
							// ��� ��������� ���� ���� "�������" ����� �����...
							final DateAttribute dateAttr = new DateAttribute();
							dateAttr.setId(attr.getId());
							dateAttr.setValue(((DateAttribute) attr).getValue());
							//dateAttr.setShowTime(false);
							dateAttr.setTimePattern(DateAttribute.defaultTimePattern);
							finalAttrs.add(dateAttr);
						} else if (stringValue != null && !stringValue.isEmpty()) {
							finalAttrs.add(attr);
						} else {
							// ������� ����, ���� � ���� �������� ��������� �������� �������
							newCards.add(c);
						}
					} // else
				} // for
				/*
				 * ��������� ������� �������� �������� ...
				 * ��������, "��������� �������� (����������, ���, �������� � �.�.)"
				 */
				final Iterator<Card> itr = newCards.iterator();
				if (itr.hasNext()) {
					final Card currCard = itr.next();
					final String mainDocTemplName = currCard.getTemplateName();
					if (mainDocTemplName != null) {
						if (logger.isDebugEnabled()) {
							logger.debug(MessageFormat.format("{0} = ''{1}'': ru = ''{2}'' and en = ''{3}'', locale=''{4}''",
									TAG_TEMPLATE_NAME_MAINDOC, mainDocTemplName, currCard.getTemplateNameRu(), currCard.getTemplateNameEn(),
									ContextProvider.getContext().getLocale().toString()));
						}
						tag_macros.put(TAG_TEMPLATE_NAME_MAINDOC, mainDocTemplName);
					}
				}

				// ����� �������� ������� -> ��������� ����
				if (!finalAttrs.isEmpty()) {
					break;
				}

				activeCards.clear();
				activeCards.addAll(newCards);
			} // for

			final StringBuilder argBuf = new StringBuilder();
			if (!finalAttrs.isEmpty()) {
				for (Iterator<Attribute> iterator = finalAttrs.iterator(); iterator.hasNext();) {
					final Attribute attr = iterator.next();
					argBuf.append(attr.getStringValue());
					if (iterator.hasNext())
						argBuf.append(itemPathDesc.getDelimiter());
				}
			}

			String argValue = argBuf.toString();
			Integer length = null;
			if (itemPathDesc.getMaxLength() != null) {
				length = itemPathDesc.getMaxLength();
			} else if (maxAttrValueLen > 0) {
				length = maxAttrValueLen;
			}

			if (length != null && argValue.length() > length) {
				argValue = argValue.substring(0, length);
			}

			buf.append("\t value='").append(argValue).append("' \n");
			args.add(argValue);
		} // for

		/* 
		 * �����-����������� ...
		 */
		fmt = applyTagMacros(fmt, tag_macros);

		/* 
		 * (!) ������������ ���������� ...
		 */
		String result = MessageFormat.format(fmt, args.toArray());
		if(result.length() > CheckStringAttrLength.MAX_TEXT_LENGTH) {
			result = result.substring(CheckStringAttrLength.MAX_TEXT_LENGTH);
		}
		/*
		 * ���������� ...
		 */
		final Attribute destAttr = smartSetAttributeValue(card, destAttrId, result);

		if (getBooleanParameter(PARAM_FORCE_SAVE, false)) {
			final String forceSaveMode = getParameter(PARAM_FORCE_SAVE_MODE, "CARD").toUpperCase();
			if (forceSaveMode.equalsIgnoreCase("CARD")){
				super.saveCard(card, getSystemUser());
				reloadCard();
			} else if (forceSaveMode.equalsIgnoreCase("ATTRIBUTE")){
				updateAttribute(card.getId(), destAttr);
			} else
				logger.warn(MessageFormat.format("Unknown mode ''{0}''=''{1}'' was ignored",
						PARAM_FORCE_SAVE_MODE, forceSaveMode));
		}
		return result;
	}

	/**
	 * ��������� ���������������� �� �������.
	 * @param s
	 * @param macros
	 * @return ������, ����� ���������� �����.
	 */
	private static String applyTagMacros(String s, Map<String, String> macros) {
		String result = s;
		if (s != null && macros != null) {
			for (Map.Entry<String, String> entry : macros.entrySet()) {
				if (entry != null && entry.getValue() != null && result.contains(entry.getKey())) {
					result = result.replaceAll(entry.getKey(), entry.getValue());
				}
			}
		}
		return result;
	}

	/**
	 * �� ����������� ��������� ��������� �������� ��� ���������� ���������
	 * ��������.
	 *
	 * @param card
	 * @param attrId
	 * @param value
	 * @return
	 * @throws DataException
	 */
	public static Attribute smartSetAttributeValue(final Card card,
			PathAttributeDescriptior loc, String value) throws DataException {
		if (loc.getAttrIds().size() == 0) {
			throw new DataException("general.unique",
					new Object[] { PROCESSORTAG + ":: not set dstAttrId" });
		}
		if (loc.getAttrIds().size() > 1) {
			throw new DataException(
					"general.unique",
					new Object[] { PROCESSORTAG
							+ ":: destination linked attribute is not supported" });
		}

		final ObjectId attrId = loc.getAttrIds().get(0);
		final Attribute destAttr = card.getAttributeById(attrId);

		if (destAttr == null)
			// ������ ��� ������ �������� -> ��� � ������� ?
			throw new DataException("jbr.processor.nodestattr_2", new Object[] {
					PROCESSORTAG, attrId.getId() });

		// ���� ����� �������, ������� ��������� ��� ������ ...
		java.lang.reflect.Method setter = null;
		try { // �������� �������� setter setValue(String) ...
			setter = destAttr.getClass().getMethod("setValue", String.class);
		} catch (Exception ex) { // SecurityException, NoSuchMethodException
		}

		if (setter == null)
			try { // �������� �������� setter setValue(Object) ...
				setter = destAttr.getClass()
						.getMethod("setValue", Object.class);
			} catch (Exception ex) { // SecurityException, NoSuchMethodException
			}

		boolean wasAssigned = false;
		if (setter != null) {
			try {
				setter.invoke(destAttr, value); // (!) ����������
				wasAssigned = true;
			} catch (Exception ex) { // IllegalArgumentException,
										// IllegalAccessException,
										// InvocationTargetException
				throw new DataException("jbr.processor.desterr_3",
						new Object[] { PROCESSORTAG, value, attrId.getId() },
						ex);
			}
		}

		if (!wasAssigned) { // ���� ��� (��� �� ����������) �����
							// setValue(string), ��
							// �������� ���� ������� ������ ����� ��������
							// ������������ ����...
			if (destAttr instanceof StringAttribute)
				((StringAttribute) destAttr).setValue(value);

			else if (destAttr instanceof IntegerAttribute) {
				final int ival = (value != null) ? Integer.parseInt(value
						.trim()) : 0;
				((IntegerAttribute) destAttr).setValue(ival);
			} else
				throw new DataException("jbr.processor.destsetterfail_2",
						new Object[] { PROCESSORTAG, attrId.getId() });
		}

		return destAttr;
	}

	/**
	 * ��������� ���������� �������� ��� �������� cardId.
	 * @param cardId
	 * @param attr
	 * @throws DataException
	 */
	private void updateAttribute(ObjectId cardId, Attribute attr)
		throws DataException
	{
		final OverwriteCardAttributes action = new OverwriteCardAttributes();
		action.setCardId(cardId);
		action.setAttributes(Collections.singletonList(attr));
		action.setInsertOnly(false);
		execAction(new LockObject(cardId));
		try {
			execAction(action, getSystemUser());
		} finally {
			execAction(new UnlockObject(cardId));
		}
	}
}
