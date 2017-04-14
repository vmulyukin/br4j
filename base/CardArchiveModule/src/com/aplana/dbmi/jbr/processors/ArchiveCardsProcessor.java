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

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.aplana.dbmi.action.CreateArchReports;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.RebindFileCards;
import com.aplana.dbmi.action.RemoveCard;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;


/**
 * <b> ��������� ��������� ������������� ����� � � ��������.</b>
 * <p>
 * ������������� ����������� � ��������� ������:<br>
 * 1. ��������� �������<br>
 * 2. <i>�����������</i> ������������ ����������� ���� (��-��������� -
 * ��������) � �������� ����� <br>
 * 3. �������� ������ ����<br>
 * </p>
 * ����������� ���������: <br>
 * 1. <b>������������:</b><br>
 * <code>{@link #PARAM_REBIND_ATTR rebindAttr}</code><br>
 * <code>{@link #PARAM_TREE_REBIND_ATTRS linksToRebind}</code><br>
 * <code>{@link #PARAM_REVERSE_ATTR_REBIND reverseRebindAttr}</code><br>
 * <code>{@link #PARAM_TREE_REBIND_TEMPLATES rebindTemplate}</code><br>
 * 2. <b>��������:</b><br>
 * <code>{@link #PARAM_SAVE_PARRENT_ATTRS saveAttrs}</code><br>
 * <code>{@link #PARAM_TREE_REMOVE_ATTRS linksToRemove}</code><br>
 * <code>{@link #PARAM_REVERSE_ATTR_REMOVE reverseRemoveAttr}</code><br>
 * <code>{@link #PARAM_SAVE_CARD_IDS saveCards}</code><br>
 * <code>{@link #PARAM_TREE_REMOVE_TEMPLATES templatesToRemove}</code><br>
 * <code>{@link #PARAM_REVERSE_TEMPLATE_REMOVE reverseRemoveTemplates}</code><br>
 * <i>������:</i>
 * <pre>  
 * {@code
 * <pre-process class="ArchiveCardsProcessor">
 * 		<parameter name="rebindTemplates" value="jbr.file"/>
 * 		<parameter name="rebindAttr" value="jbr.files"/>
 *		<parameter name="reverseRebindAttr" value="true"/>
 *		<parameter name="templatesToRemove" value="jbr.internalPerson;street;jbr.journal;jbr.index;jbr.numerator;jbr.formatNum;
 *						jbr.externalPerson;jbr.department;boss.settings;jbr.foiv;jbr.RefRegion;jbr.RefCountry;
 *						jbr.RefCity;jbr.RefDistrict;jbr.RefThemeOfAddress;jbr.authtypeitem;jbr.distributionItem.template;"/>
 *		<parameter name="reverseRemoveTemplates" value="true"/>
 *</pre-process>}
 * </pre>
 * ������������ action-�:<br> {@link com.aplana.dbmi.action.RebindFileCards
 * ������������}<br> {@link com.aplana.dbmi.action.RemoveCard ��������}
 */
public class ArchiveCardsProcessor extends ProcessCard {

	private static final long serialVersionUID = 1L;
	/**
	 * <i>�������������� ��������</i><br>
	 * CardLink-������� �������� �����, � ������� �������� ��������� �����.<br>
	 * ���� �������� �� �����, {@link com.aplana.dbmi.action.RebindFileCards
	 * ������} �� �����������
	 */
	public static String PARAM_REBIND_ATTR = "rebindAttr";
	/**
	 * <i>�������������� ��������</i><br>
	 * �������� �������� �����, ������� ����� ���������.<br>
	 * ���� �������� �� �����, ����� ������� ��� ��������.
	 */
	public static String PARAM_SAVE_PARRENT_ATTRS = "saveAttrs";
	/**
	 * <i>�������������� ��������</i><br>
	 * �������� ����� ����� CardLink-��������� �������� ����� �����/�� �����
	 * ��������� � {@link #PARAM_REBIND_ATTR "bindAttr"}.<br>
	 * ���� �������� <b>�� �����</b> � ������� <b>���������������</b> � ������
	 * (�.�. {@link #PARAM_REVERSE_ATTR_REBIND "reverseRebindAttr"} == true), ��
	 * ������������ ��� ��������� CardLink-�������� �������� �����.<br>
	 * ���� �������� <b>�����</b> � ������� <b>���������������</b> � ������
	 * (�.�. {@link #PARAM_REVERSE_ATTR_REBIND "reverseRebindAttr"} == true), ��
	 * ������������ ��� ��������� CardLink-�������� �������� �����, �����
	 * ��������.<br>
	 * ���� �������� <b>�����</b> � ������� <b>�������������</b> � ������ (�.�.
	 * {@link #PARAM_REVERSE_ATTR_REBIND "reverseRebindAttr"} == false), �����
	 * �������������� ������ �������� CardLink-�������� �������� �����.<br>
	 * �������, ����� �������� <b>�� �����</b> � ������� <b>�������������</b> �
	 * ������ (�.�. {@link #PARAM_REVERSE_ATTR_REBIND "reverseRebindAttr"} ==
	 * false), <b>����������</b>
	 */
	public static String PARAM_TREE_REBIND_ATTRS = "linksToRebind";
	/**
	 * <i>�������������� ��������</i><br>
	 * ����� ����� Template-�� ����� ��������� � {@link #PARAM_REBIND_ATTR}. <br>
	 * <b>�� ���������</b> - � {@link com.aplana.dbmi.action.RebindFileCards
	 * RebindFileCards} ������������ 284-� template ("File")
	 */
	public static String PARAM_TREE_REBIND_TEMPLATES = "rebindTemplates";
	/**
	 * <i>�������������� ��������</i><br>
	 * �������� ����� ����� CardLink-��������� �������� ����� �����/�� �����
	 * �������. <br>
	 * <B>WARNING!!!</B> - ������� ������, ��� ����������� ����� ������
	 * ��������� �� ����������� �������� �������� �����, ��� ��� �
	 * {@link com.aplana.dbmi.action.RemoveCard RemoveCard} ������ �������
	 * �������� �� ��, ����� �� ��������� ������ ����� �� ��������.<br>
	 * ���� �������� <b>�� �����</b> � ������� <b>���������������</b> � ������
	 * (�.�. {@link #PARAM_REVERSE_ATTR_REMOVE "reverseRemoveAttr"} == true), ��
	 * ������������ ��� ��������� CardLink-�������� �������� �����.<br>
	 * ���� �������� <b>�����</b> � ������� <b>���������������</b> � ������
	 * (�.�. {@link #PARAM_REVERSE_ATTR_REMOVE "reverseRemoveAttr"} == true), ��
	 * ������������ ��� ��������� CardLink-�������� �������� �����, �����
	 * ��������.<br>
	 * ���� �������� <b>�����</b> � ������� <b>�������������</b> � ������ (�.�.
	 * {@link #PARAM_REVERSE_ATTR_REMOVE "reverseRemoveAttr"} == false), �����
	 * �������������� ������ �������� CardLink-�������� �������� �����.<br>
	 * �������, ����� �������� <b>�� �����</b> � ������� <b>�������������</b> �
	 * ������ (�.�. {@link #PARAM_REVERSE_ATTR_REMOVE "reverseRemoveAttr"} ==
	 * false), <b>����������</b>
	 */
	public static String PARAM_TREE_REMOVE_ATTRS = "linksToRemove";
	/**
	 * <i>�������������� ��������</i><br>
	 * �������������, ��� ������������ ��������, ��������� � ���������
	 * {@link #PARAM_TREE_REBIND_ATTRS "linksToBind"}, ��� ���������� ������
	 * ������ ���� ��� �������� � �������� �����: <br>
	 * <code>true</code> - ������������ ��� ���������, ����� ��������� <br>
	 * <code>false</code> - ������������ ������ ��������� <br>
	 * <b>�� ���������</b> - <code>true</code>.
	 */
	public static String PARAM_REVERSE_ATTR_REBIND = "reverseRebindAttr";
	/**
	 * <i>�������������� ��������</i><br>
	 * �������������, ��� ������������ ��������, ��������� �
	 * {@link #PARAM_TREE_REMOVE_ATTRS "linksToRemove"}, ��� ���������� ������
	 * ��������� ����: <br>
	 * <code>true</code> - ������������ ��� ���������, ����� ��������� <br>
	 * <code>false</code> - ������������ ������ ��������� <br>
	 * <b>�� ���������</b> - <code>true</code>.
	 */
	public static String PARAM_REVERSE_ATTR_REMOVE = "reverseRemoveAttr";
	/**
	 * <i>�������������� ��������</i><br>
	 * ��������� ������ � ����� ���� ID ����, ������� �� ����� �������.
	 */
	public static String PARAM_SAVE_CARD_IDS = "saveCards";
	/**
	 * �������������, ��� ������������ Template-�, ��������� �
	 * {@link #PARAM_TREE_REMOVE_TEMPLATES "templatesToRemove"}, ��� ����������
	 * ������ ��������� ����: <br>
	 * <code>true</code> - ������������ ��� ���������, ����� ��������� <br>
	 * <code>false</code> - ������������ ������ ��������� <br>
	 * <b>�� ���������</b> - <code>true</code>.
	 */
	public static String PARAM_REVERSE_TEMPLATE_REMOVE = "reverseRemoveTemplates";
	/**
	 * <i>��������������, �� ����� ����������� ��������, ��� ��� � ���������
	 * ������ ����� ������� ���������� ������</i><br>
	 * �������� ����� ����� Template-�� �����/�� ����� �������.
	 */
	public static String PARAM_TREE_REMOVE_TEMPLATES = "templatesToRemove";
	/**
	 * <i>�������������� ��������</i><br>
	 * ���������, ������� �� ������� �������� ��������
	 * <code>true</code> - ������� ������� <br>
	 * <code>false</code> - �� ������� <br>
	 * <b>�� ���������</b> - <code>false</code>.
	 */
	public static String PARAM_REMOVE_ROOT_CARD_HISTORY = "removeRootCardHistory";
	/**
	 * <i>�������������� ��������</i><br>
	 * ���������, ����� ������� ����� ������������� � ������ �������� ��� �������� ��������
	 */	
	public static String PARAM_REBIND_TREE_�ARD_TEMPLATES = "rebindCardTreeTemplates";
	/**
	 * <i>�������������� ��������</i><br>
	 * ���������, ��� ������������ ������ �������� �� rebindCardTreeTemplates
	 * <code>true</code> - ��������� ������� ��������� �� ������ <br>
	 * <code>false</code> - � ������ �������� ������ ��������� �������� <br>
	 * <b>�� ���������</b> - <code>true</code>.
	 */
	public static String PARAM_REVERSE_REBIND_CARD_TREE_TEMPLATES = "reverseCardTreeTemplates";
	
	private final static String SQL_GET_ALL_REMOVE_ATTRIBUTES = "-- ������� ��� �������� CardLink-� �������� ����� \n"
		+ "SELECT DISTINCT av.attr_value_id FROM attribute_value av \n"
		+ "INNER JOIN attribute a ON a.attribute_code = av.attribute_code \n"
		+ "WHERE av.number_value IN (:CARD_IDS) AND a.data_type IN ('C', 'E') AND card_id = :ROOT_ID;";

	private final static String SQL_REMOVE_ROOT_ATTRIBUTES = "-- ������� �������� �������� �������� \n"
		+ "DELETE FROM attribute_value \n"
		+ "WHERE attr_value_id IN (:LINK)";

	private ObjectId rebindAttrId;
	private Set<ObjectId> rebindTemplates = new HashSet<ObjectId>();
	private Set<ObjectId> rebindLinks = new HashSet<ObjectId>();
	private boolean rebindReverseLinks = true;
	private Set<ObjectId> saveLinks = new HashSet<ObjectId>();
	private Set<ObjectId> removeLinks = new HashSet<ObjectId>();
	private boolean removeReverseLinks = true;
	private Set<ObjectId> saveIds = new HashSet<ObjectId>();
	private Set<ObjectId> removeTemplates = new HashSet<ObjectId>();
	private Set<ObjectId> rebindCardTreeTemplates = new HashSet<ObjectId>();
	private boolean removeReverseTemplates = true;
	private boolean removeRootCardHistosy = false;
	private boolean reverseCardTreeTemplates = true;

	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException {
		Card card = getCard();
		if ((removeLinks == null && !removeReverseLinks)
				|| (rebindLinks == null && !rebindReverseLinks)) {
			throw new DataException("Incorrect reverse parameters");
		}
		if (removeLinks.remove(null) || saveLinks.remove(null)
				|| rebindLinks.remove(null) || rebindTemplates.remove(null)
				|| saveIds.remove(null))
			throw new DataException(
					"Invalid parameter in queries.xml. ObjectId.predefined() return NULL");

		// ���������� �����
		execAction(new LockObject(card.getId()));
		Set<Long> total = Collections.emptySet();
		try {
			// ��������� �������
			CreateArchReports report = new CreateArchReports();
			report.setCardId(card.getId());
			if (card.getTemplate().equals(ObjectId.predefined(Template.class, "jbr.ord")))
				report.setReportName("ArchiveORD");
			else if (card.getTemplate().equals(ObjectId.predefined(Template.class, "jbr.incoming")))
				report.setReportName("ArchiveIncoming");
			else if (card.getTemplate().equals(ObjectId.predefined(Template.class, "jbr.outcoming")))
				report.setReportName("ArchiveOutgoing");
			else if (card.getTemplate().equals(ObjectId.predefined(Template.class, "jbr.interndoc")))
				report.setReportName("ArchiveInside");
			else if (card.getTemplate().equals(ObjectId.predefined(Template.class, "jbr.npa")))
				report.setReportName("ArchiveNPA");
			else if (card.getTemplate().equals(ObjectId.predefined(Template.class, "jbr.incomingpeople")))
				report.setReportName("ArchiveOG");
			final ActionQueryBase query3 = getQueryFactory().getActionQuery(
				report);
			query3.setAction(report);
			getDatabase().executeQuery(getSystemUser(), query3);
		  
			// 	������
			if (rebindAttrId != null) {
				RebindFileCards rebind = new RebindFileCards();
				rebind.setDestCardId(card.getId());
				rebind.setDestAttrid(rebindAttrId);
				rebind.setExcludeLinkAttrIds(rebindReverseLinks);
				rebind.setExcludeTemplateIds(reverseCardTreeTemplates);
				rebind.setLinkAttrIds(rebindLinks);
				if (rebindTemplates != null && !rebindTemplates.isEmpty())
					rebind.setTargetTemplateIds(rebindTemplates);
				if (rebindCardTreeTemplates != null && !rebindCardTreeTemplates.isEmpty())
					rebind.setCardTreeTemplates(rebindCardTreeTemplates);
				final ActionQueryBase query = getQueryFactory().getActionQuery(
						rebind);
				query.setAction(rebind);
				getDatabase().executeQuery(getSystemUser(), query);
			}

			// ��������� ������� � ��� ������������� ��������		
			if (removeReverseLinks)
				if(rebindAttrId != null)
					removeLinks.add(rebindAttrId);
			else
				removeLinks.remove(rebindAttrId);

			// ��������
			RemoveCard remove = new RemoveCard();
			remove.setCardId(card.getId());
			remove.setSavedAttrIds(saveLinks);
			remove.setLinkAttrIds(removeLinks);
			remove.setIgnoredCardIds(saveIds);
			remove.setExcludeLinkAttrIds(removeReverseLinks);
			remove.setTemplateIds(removeTemplates);
			remove.setExcludeTemplateIds(removeReverseTemplates);
			remove.setRemoveRootCardHistosy(removeRootCardHistosy);
			
			// � �������� ����� ��������� ��� ��������, ����� ��������� �������� CardLink-�
			remove.setRemoveRootCard(false);
			final ActionQueryBase query2 = getQueryFactory().getActionQuery(remove);
			query2.setAction(remove);
			total = (Set<Long>) getDatabase().executeQuery(getSystemUser(), query2);

			// ����� ���� ����������� ��������� ������ �������� �� ��
			final ObjectQueryBase subQuery = getQueryFactory().getFetchQuery(Card.class);
			subQuery.setId(card.getId());
			// �������� �������� ���������
			card = (Card) getDatabase().executeQuery(getUser(), subQuery);
		} finally {
			// ��������� �������� CardLink-�, �� ������� ����� �� ���������
			List<String> removeAttrIds = new ArrayList<String>();
			final MapSqlParameterSource args = new MapSqlParameterSource();
			final NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(
				getJdbcTemplate());
			args.addValue("CARD_IDS", total, Types.NUMERIC).addValue("ROOT_ID", Long.valueOf(card.getId().getId().toString()));
			try {
				if (!total.isEmpty())
					removeAttrIds = jdbc.queryForList(SQL_GET_ALL_REMOVE_ATTRIBUTES, args, String.class);
				args.addValue("LINK", removeAttrIds, Types.BIGINT);
				if (!removeAttrIds.isEmpty())
					jdbc.update(SQL_REMOVE_ROOT_ATTRIBUTES, args);
			} catch (Exception ex) {
				logger.error("Error while deleting root attributes.");
				throw new DataException(
					"Error while deleting root attributes. ", ex);
			} finally {
				// ������������� �����
				execAction(new UnlockObject(card.getId()));
			}
		}
		return null;
	}

	@Override
	public void setParameter(String name, String value) {
		if (name.equalsIgnoreCase(PARAM_REBIND_ATTR)) {
			this.rebindAttrId = ObjectId.predefined(CardLinkAttribute.class,
					value);
		} else if (name.equalsIgnoreCase(PARAM_TREE_REBIND_ATTRS)
				|| name.equalsIgnoreCase(PARAM_TREE_REMOVE_ATTRS)) {
			String[] stringAttrs = value.trim().split(";");
			Set<ObjectId> links = new HashSet<ObjectId>();
			if (name.equalsIgnoreCase(PARAM_TREE_REBIND_ATTRS))
				links = rebindLinks;
			else
				links = removeLinks;
			for (String attr : stringAttrs) {
				ObjectId attrId = ObjectId.predefined(CardLinkAttribute.class, attr.trim());
				if (attrId == null) {
					attrId = ObjectId.predefined(TypedCardLinkAttribute.class, attr.trim());
				}
				links.add(attrId);
			}
		} else if (name.equalsIgnoreCase(PARAM_TREE_REBIND_TEMPLATES)
				|| name.equalsIgnoreCase(PARAM_TREE_REMOVE_TEMPLATES)
				|| name.equalsIgnoreCase(PARAM_REBIND_TREE_�ARD_TEMPLATES)) {
			String[] stringTemplates = value.trim().split(";");
			Set<ObjectId> templates = new HashSet<ObjectId>();
			if (name.equalsIgnoreCase(PARAM_TREE_REBIND_TEMPLATES))
				templates = rebindTemplates;
			else if (name.equalsIgnoreCase(PARAM_TREE_REMOVE_TEMPLATES))
				templates = removeTemplates;
			else if (name.equalsIgnoreCase(PARAM_REBIND_TREE_�ARD_TEMPLATES))
				templates = rebindCardTreeTemplates;
			for (String template : stringTemplates) {
				templates.add(ObjectId.predefined(Template.class,
						template.trim()));
			}
		} else if (name.equalsIgnoreCase(PARAM_REVERSE_ATTR_REMOVE)) {
			this.removeReverseLinks = Boolean.parseBoolean(value);
		} else if (name.equalsIgnoreCase(PARAM_REVERSE_ATTR_REBIND)) {
			this.rebindReverseLinks = Boolean.parseBoolean(value);
		} else if (name.equalsIgnoreCase(PARAM_REVERSE_TEMPLATE_REMOVE)) {
			this.removeReverseTemplates = Boolean.parseBoolean(value);
		} else if (name.equalsIgnoreCase(PARAM_SAVE_CARD_IDS)) {
			String[] stringIds = value.trim().split(";");
			for (String id : stringIds) {
				saveIds.add(new ObjectId(Card.class, new Long(id.trim())));
			}
		} else if (name.equalsIgnoreCase(PARAM_SAVE_PARRENT_ATTRS)) { //TODO: ��� ��������
			String[] stringAttrs = value.trim().split(";");
			for (String attr : stringAttrs) {
				saveLinks.add(new ObjectId(Attribute.class, attr.trim()));
			}
		} else if (name.equalsIgnoreCase(PARAM_REMOVE_ROOT_CARD_HISTORY)){
			this.removeRootCardHistosy = Boolean.parseBoolean(value);
		} else if (name.equalsIgnoreCase(PARAM_REVERSE_REBIND_CARD_TREE_TEMPLATES)){
			this.reverseCardTreeTemplates = Boolean.parseBoolean(value);
		}else
			super.setParameter(name, value);

	}
}
