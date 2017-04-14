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
package com.aplana.dbmi.service.impl.access;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.aplana.dbmi.model.AccessRule;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ByCardAccessRule;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAccessRule;
import com.aplana.dbmi.model.PersonProfileAccessRule;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.impl.query.AttributeOptions;
import com.aplana.dbmi.utils.SetMap;

/**
 * Основной класс для работы с правами. Умеет:
 * 1. Чистить права в матрице прав по различным признакам
 * 		1.1. Права на входную карточку
 * 		1.2. Права согласно атрибутам входной карточки
 * 		1.3. Права, данные согласно входному правилу
 * 		1.4. Права, данные пользователям с входной ролью
 * 2. Раздавать права на карточки
 * 		2.1. Анализируя все правила, применяемые ко входной карточке
 * 		2.2. Анализируя правила, применяемые к связанным с текущей карточкой карточкам
 * 		2.3. Анализируя правила и пользователей, которые относятся ко входной Роли
 * 		2.3. Анализируя входное правило
 * 
 * Основные сокращения далее в комментариях класса:
 *  АС - Атрибут связи
 *  АС-КЛ - Атрибут связи кардлинк
 *  АС-БЛ - Атрибут связи беклинк
 *  АС-ПЛ - Атрибут связи персон-линк (персон-атрибут)
 *  АПС - Атрибут промежуточной связи
 *  АПС-КЛ - Атрибут промежуточной связи кардлинк
 *  АПС-БЛ - Атрибут промежуточной связи беклинк
 *  АПС-ПЛ - Атрибут промежуточной персон-линк (персон-атрибут)
 */
public class AccessRuleManager {
	
	public static final int BUCKET_SIZE = 150;

	protected final Log logger = LogFactory.getLog(getClass());
	private JdbcTemplate jdbc;
	
	public AccessRuleManager(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	/**
	 * Determines whether the user has specified access to specified card.
	 * This method checks both role access rules and resolved person access list.
	 * 
	 * @param cardId ID of card to be accessed
	 * @param operation Access operation code, see {@link com.aplana.dbmi.model.AccessCard#setOperation(String)}
	 * @param personId ID of person trying to access card
     * @param doCheckDelegationRules true if delegation check is needed, helps avoid endless loop
	 * @return true if the person has permissions to access the card
	 */	
	public boolean isCardOperationAllowed(ObjectId cardId, String operation, ObjectId personId, boolean doCheckDelegationRules) {

		if (cardId == null || !Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card ID");
		if (personId == null || !Person.class.equals(personId.getType()))
			throw new IllegalArgumentException("Not a person ID");

		final Set<ObjectId> checkedCards = new HashSet<ObjectId>();

		return isCardOperationAllowed(cardId, operation, personId,  doCheckDelegationRules, checkedCards);
	}

	private boolean isCardOperationAllowed(ObjectId cardId, String operation, ObjectId personId, boolean doCheckDelegationRules, 
			final Set<ObjectId> checkedCards) {

		long started = System.currentTimeMillis();

		//skip access checking if current user is a system user
		if (Person.ID_SYSTEM.equals(personId)) {
			return true;
		}

		//avoid endless loop
		if (doCheckDelegationRules) {
			if (!checkedCards.contains(cardId)) {
				checkedCards.add(cardId);
			} else {
				return false;
			}
		}

		long permissions =
            // Access for all users
            jdbc.queryForLong(
				"SELECT COUNT(r.rule_id) FROM role_access_rule rr \n" +
					"JOIN access_rule r ON r.rule_id=rr.rule_id \n" +
					"JOIN access_card_rule cr ON cr.rule_id=r.rule_id, \n" +
					"card c \n" +
				"WHERE c.card_id=? \n" +
				"AND c.is_active=1 \n" +
                "AND rr.role_code is NULL \n" +
				"AND (r.template_id IS NULL OR r.template_id=c.template_id) \n" +
				"AND (r.status_id IS NULL OR r.status_id=c.status_id) \n" +
				"AND cr.operation_code=? \n" +
				"limit 1",
				new Object[] { cardId.getId(), operation});
		
		if (permissions == 0) {
			permissions =
			// Access by role
			jdbc.queryForLong(
				"SELECT COUNT(r.rule_id) FROM role_access_rule rr \n" +
					"JOIN access_rule r ON r.rule_id=rr.rule_id \n" +
					"JOIN access_card_rule cr ON cr.rule_id=r.rule_id \n" +
					"JOIN person_role pr ON pr.role_code=rr.role_code, \n" +
					"card c \n" +
				"WHERE c.card_id=? \n" +
				"AND (c.is_active=1 or pr.role_code='A') \n" +
				"AND (r.template_id IS NULL OR r.template_id=c.template_id) \n" +
				"AND (r.status_id IS NULL OR r.status_id=c.status_id) \n" +
				"AND cr.operation_code=? \n" +
				"AND pr.person_id=? \n" +
				"limit 1",
				new Object[] { cardId.getId(), operation, personId.getId() });
		}
		
		if (permissions == 0 && isCardActive(cardId)) {
			permissions =
			// Access by attribute
			jdbc.queryForLong(
				"SELECT COUNT(a.person_id) FROM access_list a \n" +
					"JOIN access_card_rule cr ON a.rule_id=cr.rule_id \n" +
				"WHERE a.card_id=? \n" +
				"AND cr.operation_code=? \n" +
				"AND a.person_id=? \n" +
				"limit 1",
				new Object[] { cardId.getId(), operation, personId.getId() });
		}
		logger.info("Card " + cardId.getId() + ", operation " + operation + ", user " +
				personId.getId() + ": " + permissions + " permission(s) found");
        boolean ret=false;
		if (permissions > 0) {
            ret = true;
        } else {
            if (doCheckDelegationRules) ret = checkDelegation(cardId, operation, personId, checkedCards);
        }
        long delay = System.currentTimeMillis() - started;
        logger.info("Card " + cardId.getId() + ", operation " + operation + ", user " +
				personId.getId() + " check worked : " + delay + " ms");
        return ret;
    }

    /**
     * Access by delegation
     * @param cardId
     * @param operation
     * @return
     */
    private boolean checkDelegation(ObjectId cardId, String operation, ObjectId personId, 
          final Set<ObjectId> checkedCards) {
        List delegationRules = jdbc.query
                ("SELECT dr.link_attr_code, ao.option_code, ao.option_value FROM delegation_access_rule dr \n" +
                        " JOIN access_rule r ON r.rule_id=dr.rule_id \n" +
                        " JOIN access_card_rule cr ON cr.rule_id=r.rule_id \n" +
                        " LEFT OUTER JOIN attribute_option ao ON ao.attribute_code = dr.link_attr_code \n" +
                        ", card c  \n" +
                        "WHERE c.card_id=? \n" +
                        " AND (r.template_id = c.template_id) \n" +
                        " AND (r.status_id=c.status_id) \n" +
                        " AND cr.operation_code=? "
                , new Object[]{cardId.getId(), operation}
                , new RowMapper() {
                    public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                        return new Object[]{resultSet.getString(1), resultSet.getString(2), resultSet.getString(3)};
                    }
                });
        if (delegationRules == null || delegationRules.isEmpty()) {
            logger.debug("There is no delegation rule for card " + cardId.getId());
            return false;
        }

        // separate attributes by option UPLINK
        List<Object[]> delegationRulesSimple = new ArrayList<Object[]>(delegationRules.size());
        List<Object[]> delegationRulesUplink = new ArrayList<Object[]>(delegationRules.size());
        for (Object delR : delegationRules) {
            Object[] delegationRule= (Object[]) delR;
            if ("UPLINK".equals(delegationRule[1])) {
                delegationRulesUplink.add(delegationRule);
            } else {
                delegationRulesSimple.add(delegationRule);
            }
        }

        // get cards by backlinks with UPLINK
        // the LINK option for each such attribute should be in delegationRulesSimple
        if (delegationRulesUplink.size() > 0) {
            for (Object[] delegationRuleUplink : delegationRulesUplink) {
                String intemedCardlink = (String) delegationRuleUplink[2]; //option value
                // find origin cardlink in "simple" options and remove there
                String originCardlink = null;
                for (Iterator<Object[]> iterator = delegationRulesSimple.iterator(); iterator.hasNext();) {
                    Object[] next = iterator.next();
                    if (next[0].equals(delegationRuleUplink[0]) && "LINK".equals(next[1])){ // by attr code
                        originCardlink = (String) next[2];
                        iterator.remove();
                        break;
                    }
                }
                if (originCardlink == null) {
                    throw new IllegalStateException("Backlink with UPLINK option has no LINK option");
                }

                long cardByUpBacklink = jdbc.queryForLong("SELECT functionbacklink( ?, ?, ?)"
                        , new Object[]{cardId.getId(), intemedCardlink, originCardlink});

                logger.info("Recursive access check for user" + personId.getId() + " to Card " + cardId + " by linked card " + cardByUpBacklink);
                if (isCardOperationAllowed(new ObjectId(Card.class, cardByUpBacklink), operation, personId, true, checkedCards)) {
                    logger.info("Delegation rule passed. Card " + cardId.getId() + ", operation " + operation + ", user " +
                            personId.getId() + ": " + " by UPLINK backlink: " + delegationRuleUplink[0] + " , linked card " + cardByUpBacklink);
                    return true;
                }

            }
        }
        // separate rules by cardlink and backlink
        if (delegationRulesSimple.size() > 0) {
            List cardLinks = new ArrayList(delegationRulesSimple.size());
            List cardLinksByBacklinks = new ArrayList(delegationRulesSimple.size());
            for (Object[] delegationRuleSimple : delegationRulesSimple) {
                if ("LINK".equals(delegationRuleSimple[1])) {
                    cardLinksByBacklinks.add( delegationRuleSimple[2]);
                } else {
                    cardLinks.add( delegationRuleSimple[0]);
                }
            }

            // check for backlink
            if (cardLinksByBacklinks.size() > 0) {
                cardLinksByBacklinks.add(cardId.getId());
                List resultList = jdbc.queryForList(
                        "SELECT card_id FROM attribute_value \n" +
                                " WHERE \n" +
                                " attribute_code IN \n" +
                                "  (" + populateParameters(cardLinksByBacklinks.size() - 1) + " ) AND number_value =?"
                        , cardLinksByBacklinks.toArray());
                if (resultList != null && !resultList.isEmpty()) {
                	for (Object aResult : resultList) {
                        Map cardsByBacklink = (Map) aResult;
	                    if (callRecursivePermissionCheck(cardId, operation, personId, cardsByBacklink, checkedCards)) {
	                        return true;
	                    }
                	}
                }
            }

            // check for cardlink
            if (cardLinks.size() > 0) {
                cardLinks.add(cardId.getId());
                List resultList = jdbc.queryForList(
                        " SELECT number_value FROM attribute_value WHERE attribute_code IN \n" +
                                "  (" + populateParameters(cardLinks.size() - 1) + " ) AND card_id = ?"
                        , cardLinks.toArray());
                if (resultList != null && !resultList.isEmpty()) {
                    for (Object aResult : resultList) {
                        Map cardsByCardlink = (Map) aResult;
                        if (callRecursivePermissionCheck(cardId, operation, personId, cardsByCardlink, checkedCards)) {
                            return true;
                        }
                    }
                }
            }

        }
        logger.debug("Card " + cardId.getId() + ", operation " + operation + ", user " +
                personId.getId() + ": " + " Nothing found by " + delegationRules.size() + " delegation rules ");
        return false;
    }

    private boolean callRecursivePermissionCheck(ObjectId cardId, String operation, ObjectId personId, 
          Map linkedCards, final Set<ObjectId> checkedCards) {
        if (linkedCards != null && !linkedCards.isEmpty()) {
            for (Object o : linkedCards.values()) {
                Long linkedCardId = ((BigDecimal) o).longValue();
                logger.info("Recursive access check for user " + personId.getId() + " to Card " + cardId + " by linked card " + linkedCardId);
                if (isCardOperationAllowed(new ObjectId(Card.class, linkedCardId), operation, personId, true, checkedCards)) {
                    logger.info("Delegation rule passed. Card " + cardId.getId() + ", operation " + operation + ", user " +
                            personId.getId() + ": " + " by linked card " + linkedCardId);
                    return true;
                }
            }
        }
        return false;
    }

    /**
	 * Determines whether the user has specified access to specified template.
	 * Because this kind of access doesn't depend on any card, the method checks only
	 * role access rules.
	 * 
	 * @param templateId ID of template to be accessed
	 * @param operation Access operation code, see {@link com.aplana.dbmi.model.AccessTemplate#setOperation(String)}
	 * @param personId ID of person trying to access template
	 * @return true if the person has permissions to access the template
	 */
	public boolean isTemplateOperationAllowed(ObjectId templateId, String operation, ObjectId personId) {
		if (templateId == null || !Template.class.equals(templateId.getType()))
			throw new IllegalArgumentException("Not a template ID");
		if (personId == null || !Person.class.equals(personId.getType()))
			throw new IllegalArgumentException("Not a person ID");
		//skip access checking if current user is a system user
		if (Person.ID_SYSTEM.equals(personId)) {
			return true;
		}

		long permissions =
			jdbc.queryForLong(
					"SELECT COUNT(*) FROM role_access_rule rr \n" +
					"JOIN access_rule r ON r.rule_id=rr.rule_id \n" +
					"JOIN access_template_rule tr ON tr.rule_id=r.rule_id \n" +
					"JOIN person_role pr ON pr.role_code=rr.role_code \n" +
				"WHERE r.template_id=? \n" +
				"AND tr.operation_code=? \n" +
				"AND pr.person_id=? ",
				new Object[] { templateId.getId(), operation, personId.getId() })
				+
				jdbc.queryForLong(
					"SELECT COUNT(*) FROM role_access_rule rr \n" +
					"JOIN access_rule r ON r.rule_id=rr.rule_id \n" +
					" JOIN access_template_rule tr ON tr.rule_id=r.rule_id \n" +
					" WHERE r.template_id=? \n" +
					" AND tr.operation_code=? \n" +
					"AND rr.role_code is NULL " ,
					new Object[] { templateId.getId(), operation });
		
		logger.info("Template " + templateId.getId() + ", operation " + operation + ", user " +
				personId.getId() + ": " + permissions + " permission(s) found");
		return permissions > 0;
	}
	
	/**
	 * Determines whether the user is allowed to make the change of the state of specific card.
	 * This method checks both role access rules and resolved person access list.
	 * 
	 * @param cardId ID of card to be modified
	 * @param moveId ID of move to be made
	 * @param personId ID of person trying to make an operation
	 * @return true if the person has permissions to make the change
	 */
	public boolean isWorkflowMoveAllowed(ObjectId cardId, ObjectId moveId, ObjectId personId) {
		if (cardId == null || !Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card ID");
		if (moveId == null || !WorkflowMove.class.equals(moveId.getType()))
			throw new IllegalArgumentException("Not a workflow move ID");
		if (personId == null || !Person.class.equals(personId.getType()))
			throw new IllegalArgumentException("Not a person ID");
		//skip access checking if current user is a system user
		if (Person.ID_SYSTEM.equals(personId)) {
			return true;
		}

		long permissions =
			// Access by role
			jdbc.queryForLong(
				"SELECT COUNT(rr.rule_id) FROM role_access_rule rr \n" +
					"JOIN access_rule r ON r.rule_id=rr.rule_id \n" +
					"JOIN access_move_rule mr ON mr.rule_id=r.rule_id \n" +
					"JOIN person_role pr ON pr.role_code=rr.role_code, \n" +
					"card c \n" +
				"WHERE c.card_id=? \n" +
				"AND (r.template_id IS NULL OR r.template_id=c.template_id) \n" +
				"AND (r.status_id IS NULL OR r.status_id=c.status_id) \n" +
				"AND mr.wfm_id=? \n" +
				"AND pr.person_id=? ",
				new Object[] { cardId.getId(), moveId.getId(), personId.getId() }) +
			// Access by attribute
			jdbc.queryForLong(
				"SELECT COUNT(mr.rule_id) FROM access_list a \n" +
				"JOIN access_move_rule mr ON mr.rule_id=a.rule_id \n" +
				"WHERE a.card_id=? \n" +
				"AND mr.wfm_id=? \n" +
				"AND a.person_id=?",
				new Object[] { cardId.getId(), moveId.getId(), personId.getId() });
		logger.info("Card " + cardId.getId() + ", move " + moveId.getId() + ", user " +
				personId.getId() + ": " + permissions + " permission(s) found");
		return permissions > 0;
	}
	
	public boolean isAttributeOperationAllowed(ObjectId cardId, ObjectId attributeId, String operation, ObjectId personId) {
		if (cardId == null || !Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card ID");
		if (attributeId == null || !Attribute.class.isAssignableFrom(attributeId.getType()))
			throw new IllegalArgumentException("Not an attribute ID");
		if (personId == null || !Person.class.equals(personId.getType()))
			throw new IllegalArgumentException("Not a person ID");
		//skip access checking if current user is a system user
		if (Person.ID_SYSTEM.equals(personId)) {
			return true;
		}

		long permissions =
			// Access by role
			jdbc.queryForLong(
				"SELECT COUNT(r.rule_id) FROM role_access_rule rr \n" +
					"JOIN access_rule r ON r.rule_id=rr.rule_id \n" +
					"JOIN access_attr_rule ar ON cr.rule_id=r.rule_id \n" +
					"JOIN person_role pr ON pr.role_code=rr.role_code, \n" +
					"card c \n" +
				"WHERE c.card_id=? \n" +
				"AND (r.template_id IS NULL OR r.template_id=c.template_id) \n" +
				"AND (r.status_id IS NULL OR r.status_id=c.status_id) \n" +
				"AND ar.attribute_code=? \n" +
				"AND ar.operation_code=? \n" +
				"AND pr.person_id=? ",
				new Object[] { cardId.getId(), attributeId.getId(), operation, personId.getId() }) +
			// Access by attribute
			jdbc.queryForLong(
				"SELECT COUNT(a.person_id) FROM access_list a \n" +
					"JOIN access_attr_rule cr ON a.rule_id=cr.rule_id \n" +
				"WHERE a.card_id=? \n" +
				"AND ar.attribute_code=? \n" +
				"AND ar.operation_code=? \n" +
				"AND a.person_id=?",
				new Object[] { cardId.getId(), attributeId.getId(), operation, personId.getId() });
		logger.info("Card " + cardId.getId() + ", attribute " + attributeId.getId() + ", operation " +
				operation + ", user " + personId.getId() + ": " + permissions + " permission(s) found");
		return permissions > 0;
	}
	
	public String getAttributeOperationSubquery(String cardTableAlias, String attributeId, String operation, String personId) {
		String query =
			"SELECT COUNT(r.rule_id) FROM role_access_rule rr \n" +
				"JOIN access_rule r ON r.rule_id=rr.rule_id \n" +
				"JOIN access_attr_rule ar ON ar.rule_id=r.rule_id \n" +
				"JOIN person_role pr ON pr.role_code=rr.role_code \n" +
			"WHERE (r.template_id IS NULL OR r.template_id=" + cardTableAlias + ".template_id)\n" +
			" AND (r.status_id IS NULL OR r.status_id=" + cardTableAlias + ".status_id)\n" +
			" AND ar.attribute_code=" + attributeId +
			" AND ar.operation_code=" + operation +
			" AND pr.person_id=" + personId;
		return query;
	}
	
	/**
	 * Removes all persmissions to specific card.
	 * Should be called before changing card's status.
	 * 
	 * @param cardId ID of the card
	 */
	public void cleanAccessListByCard(ObjectId cardId) {
		if (cardId == null || !Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card ID");
		long start = System.currentTimeMillis();
		long rows = jdbc.update(
				"DELETE FROM access_list WHERE card_id=?",
				new Object[] { cardId.getId() });
		long duration = System.currentTimeMillis() - start;
		logger.info(rows + " permission(s) removed (" + duration + "ms) by card "+cardId.getId());
	}
	
	/**
	 * Removes all permissions granted by card's attribute values.
	 * Should be called before changing the card.
	 * 
	 * @param cardId ID of the card
	 */
	public void cleanAccessListBySourceCard(ObjectId cardId) {
		if (cardId == null || !Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card ID");
		long start = System.currentTimeMillis();
		long rows = jdbc.update(
				"DELETE FROM access_list WHERE source_value_id IN (\n" +
				"SELECT attr_value_id FROM attribute_value WHERE card_id=?)",
				new Object[] { cardId.getId() });
		long duration = System.currentTimeMillis() - start;
		logger.info(rows + " permission(s) removed (" + duration + "ms) by attributes of card "+cardId.getId());
	}
	
	/**
	 * Removes all persmissions to specific card and by card's attribute values.
	 * Should be called before changing card's status.
	 * 
	 * @param cardId ID of the card
	 */
	public void cleanAccessListByCardAndSourceAttrs(ObjectId cardId) {
		if (cardId == null || !Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card ID");
		long start = System.currentTimeMillis();
		long rows = jdbc.update(
				"DELETE FROM access_list WHERE al_id in ( " +
				"   select al_id from access_list where card_id = ? " +
				"   UNION " +
				"   select al_id from access_list where " +
				"	source_value_id IN (\n" +
				"	SELECT attr_value_id FROM attribute_value WHERE card_id=?)" +
				" )",
				new Object[] { cardId.getId(), cardId.getId() });
		long duration = System.currentTimeMillis() - start;
		logger.info(rows + " permission(s) removed (" + duration + "ms) by card's id and card's attributes "+cardId.getId());
	}
	
	/**
	 * Removes all permissions granted to user by person & profile rules with specific role.
	 * Should be called before revoking of role from user.
	 * 
	 * @param roleId Revoked role ID
	 */
	public void cleanAccessListByRole(ObjectId roleId) {
		if (roleId == null || !Role.class.equals(roleId.getType()))
			throw new IllegalArgumentException("Not a role ID");
		long start = System.currentTimeMillis();
		long rows = jdbc.update(
				"DELETE FROM access_list a \n" +
				"USING person_access_rule par, person_role pr \n" +
				"WHERE a.rule_id=par.rule_id AND a.person_id=pr.person_id \n" +
					"AND par.role_code=pr.role_code AND pr.prole_id=?",
				new Object[] { roleId.getId() });
		rows += jdbc.update(
				"DELETE FROM access_list a \n" +
				"USING profile_access_rule par, person_role pr \n" +
				"WHERE a.rule_id=par.rule_id AND a.person_id=pr.person_id \n" +
					"AND par.role_code=pr.role_code AND pr.prole_id=?",
				new Object[] { roleId.getId() });
		long duration = System.currentTimeMillis() - start;
		logger.info(rows + " permission(s) removed (" + duration + "ms) by role "+roleId.getId());
	}
	
	/**
	 * Removes all permissions granted by attribute value of card.
	 * Should be called before deleting attribute value of card.
	 * 
	 * @param roleId Revoked role ID
	 */
	public void cleanAccessListByAttributeOfCard(ObjectId attrId, ObjectId cardId) {
		if (attrId == null)
			throw new IllegalArgumentException("Not a attribute code");
		if (cardId == null || !Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card ID");
		long start = System.currentTimeMillis();
		long rows = jdbc.update(
			"DELETE FROM access_list ac " +
			"WHERE source_value_id in (SELECT attr_value_id FROM attribute_value "+
			"WHERE attribute_code=? AND card_id=?)",
			new Object[] { attrId.getId(), cardId.getId() },
			new int[] { Types.VARCHAR, Types.NUMERIC }
		);
		long duration = System.currentTimeMillis() - start;
		logger.info(rows + " permission(s) removed (" + duration + "ms) by attribute "+attrId.getId() + " of card "+cardId.getId());
	}
	
	/**
	 * Removes all permissions granted by attribute value of card.
	 * Should be called before deleting attribute value of card.
	 * 
	 * @param roleId Revoked role ID
	 */
	public void cleanAccessListByLinkToCard(ObjectId cardId, String attrCodes, String templateIds) {
		if (cardId == null || !Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card ID");
		long start = System.currentTimeMillis();
		long rows = jdbc.update(
				"DELETE FROM access_list ac " +
				"WHERE source_value_id in (SELECT attr_value_id FROM attribute_value av \n"+
				"WHERE "+(attrCodes!=null&&!attrCodes.isEmpty()?"av.attribute_code in ("+ attrCodes + ") \n":"(1=1) \n")+
				"AND (av.number_value = ?) \n"+
				("".equals(templateIds)?")":"\t\t and exists(select 1 from card c where c.card_id = av.card_id and c.template_id in ("+templateIds+")) )\n"),
				new Object[] { cardId.getId() },
				new int[] { Types.NUMERIC }
			);
		long duration = System.currentTimeMillis() - start;
		logger.info(rows + " permission(s) removed (" + duration + "ms) by linked cards to "+cardId.getId()+" by attributes "+attrCodes + " with templates "+templateIds);
	}
	
	/**
	 * Removes all permissions granted be specific rule.
	 * Should be called before removing the rule.
	 * 
	 * @param ruleId ID of the rule
	 */
	public void cleanAccessListByRule(ObjectId ruleId) {
		if (ruleId == null || !AccessRule.class.isAssignableFrom(ruleId.getType()))
			throw new IllegalArgumentException("Not an access rule ID");
		long rows = jdbc.update(
				"DELETE FROM access_list WHERE rule_id=?",
				new Object[] { ruleId.getId() });
		logger.info(rows + " permission(s) removed by ruleId "+ruleId.getId());
	}
	

	/* Новые запросы */ 
	// I. =============== Person rules ===============
	// Access to the card by its own attributes
	public final static String SELECT_PERSON_ACCESS_CARD_THIS = 
		"SELECT \n" +
		"	c.card_id, \n" +
		"	r.rule_id, \n" +
		"	pv.number_value, \n" +
		"	pv.attr_value_id \n" +
		"FROM \n" +
		"	card c \n" +
		"	JOIN access_rule r \n" +
		//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*		"		ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
		"		AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
		"		ON (c.template_id=r.template_id) \n" +
		"		AND (c.status_id=r.status_id) \n" +
		"	JOIN person_access_rule pr \n" +
		"		ON r.rule_id=pr.rule_id \n" +
		"	LEFT JOIN person_role sr \n" +
		"		ON pr.role_code=sr.role_code \n" +
		"	JOIN attribute_value pv \n" +
		"		ON c.card_id=pv.card_id \n" +
		"		AND pr.person_attr_code=pv.attribute_code \n" +
		"WHERE \n" +
		"	(pr.role_code IS NULL OR pv.number_value=sr.person_id) \n" +
		"	AND pr.link_attr_code is NULL \n" +
		"	AND c.card_id in (:curCardIds) \n";

	// Access to the card by linked cards' attributes (direct link)
	public final static String SELECT_PERSON_ACCESS_CARD_LINKED =
		"SELECT \n" +
		"	c.card_id, \n" +
		"	r.rule_id, \n" +
		"	pv.number_value, \n" +
		"	pv.attr_value_id \n" +
		"FROM \n" +
		"	card c \n" +
		"	JOIN access_rule r \n" +
		//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
		/*		"		ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
				"		AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
				"		ON (c.template_id=r.template_id) \n" +
				"		AND (c.status_id=r.status_id) \n" +
		"	JOIN person_access_rule pr \n" +
		"		ON r.rule_id=pr.rule_id \n" +
		"	LEFT JOIN person_role sr \n" +
		"		ON pr.role_code=sr.role_code \n" +
		"	JOIN attribute_value lv \n" +
		"		ON c.card_id=lv.card_id \n" +
		"		AND pr.link_attr_code=lv.attribute_code \n" +
		"	JOIN card lc \n" +
		"		ON lv.number_value=lc.card_id \n" +
		"		AND (pr.linked_status_id=lc.status_id OR pr.linked_status_id IS NULL) \n" +
		"	JOIN attribute_value pv \n" +
		"		ON lv.number_value=pv.card_id \n" +
		"		AND pr.person_attr_code=pv.attribute_code \n" +
		"WHERE \n" +
		"	(pr.role_code IS NULL OR pv.number_value=sr.person_id) \n" +
		"	AND pr.intermed_attr_code IS NULL \n" +
		"	AND c.card_id=:curCardId \n";

	// Access to the card by linked person cards' attributes (direct user link)
	// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
/*	public final static String SELECT_PERSON_ACCESS_CARD_USER_LINKED =
		"SELECT \n" +
		"	c.card_id, \n" +
		"	r.rule_id, \n" +
		"	pv.number_value, \n" +
		"	pv.attr_value_id \n" +
		"FROM \n" +
		"	card c \n" +
		"	JOIN access_rule r \n" +
		"		ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
		"		AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +
		"	JOIN person_access_rule pr \n" +
		"		ON r.rule_id=pr.rule_id \n" +
		"	LEFT JOIN person_role sr \n" +
		"		ON pr.role_code=sr.role_code \n" +
		"	JOIN attribute_value lv \n" +
		"		ON c.card_id=lv.card_id \n" +
		"		AND pr.link_attr_code=lv.attribute_code \n" +
		"	JOIN person pL \n" +
		"		ON pL.person_id = lv.number_value \n" +
		"	JOIN card lc \n" +
		"		ON lc.card_id = pL.card_id \n" +
		"		AND (pr.linked_status_id=lc.status_id OR pr.linked_status_id IS NULL) \n" +
		"	JOIN attribute_value pv \n" +
		"		ON lv.number_value=pv.card_id \n" +
		"		AND pr.person_attr_code=pv.attribute_code \n" +
		"WHERE \n" +
		"	(pr.role_code IS NULL OR pv.number_value=sr.person_id) \n" +
		"	AND pr.intermed_attr_code IS NULL \n" +
		"	AND c.card_id=:curCardId \n";
*/
	// Access by linked card's attribute (reverse link)
	// подправили запрос, чтобы для беклинков-атрибутов связи искался самые верхние родители
	public final static String SELECT_PERSON_ACCESS_CARD_BACKLINK =
		"SELECT \n" +
		"	c.card_id, \n" +
		"	c.rule_id, \n" +
		"	pv.number_value, \n" +
		"	pv.attr_value_id \n" +
		"FROM \n" +
		"	( \n" +
		"		SELECT \n" + 
		"			c.card_id, \n" + 
		"			r.rule_id, \n" + 
		"			functionbacklink(c.card_id, aoU.option_value, aoL.option_value) as link_card_id, \n" + 
		"			pr.linked_status_id, \n" +
		"			sr.person_id, \n" +
		"			pr.role_code, \n" +
		"			pr.intermed_attr_code, \n" +
		"			pr.person_attr_code \n" +
		"		FROM card c \n" +
		"			JOIN access_rule r \n" + 	
		//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
		/*		"		ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
				"		AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
		"					ON (c.template_id=r.template_id) \n" +
		"					AND (c.status_id=r.status_id) \n" +
		"			JOIN person_access_rule pr \n" +
		"					ON 	r.rule_id=pr.rule_id \n" + 
		"			LEFT JOIN person_role sr \n" +
		"					ON pr.role_code=sr.role_code \n" + 
		"			JOIN attribute_option aoL \n" + 
		"					ON pr.link_attr_code=aoL.attribute_code \n" + 
		"					AND aoL.option_code='" +AttributeOptions.LINK+"' \n" +
		"			JOIN attribute_option aoU \n" + 
		"					ON pr.link_attr_code=aoU.attribute_code \n" + 
		"					AND aoU.option_code='" +AttributeOptions.UPLINK+"' \n" +
		"		UNION ALL \n" +
		"		SELECT \n" + 
		"			c.card_id, \n" + 
		"			r.rule_id, \n" + 
		"			av.card_id as link_card_id, \n" + 
		"			pr.linked_status_id, \n" +
		"			sr.person_id, \n" +
		"			pr.role_code, \n" +
		"			pr.intermed_attr_code, \n" +
		"			pr.person_attr_code \n" +
		"		FROM card c \n" +
		"			JOIN access_rule r \n" + 	
		//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
		/*		"		ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
				"		AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
		"					ON (c.template_id=r.template_id) \n" +
		"					AND (c.status_id=r.status_id) \n" +
		"			JOIN person_access_rule pr \n" +
		"					ON 	r.rule_id=pr.rule_id \n" + 
		"			LEFT JOIN person_role sr \n" +
		"					ON pr.role_code=sr.role_code \n" + 
		"			JOIN attribute_option aoL \n" + 
		"					ON pr.link_attr_code=aoL.attribute_code \n" + 
		"					AND aoL.option_code='" +AttributeOptions.LINK+"' \n" +
		"			LEFT JOIN attribute_option aoU \n" + 
		"					ON pr.link_attr_code=aoU.attribute_code \n" + 
		"					AND aoU.option_code='" +AttributeOptions.UPLINK+"' \n" +
		"			JOIN attribute_value av \n" +
		"					ON av.attribute_code = aoL.option_value \n" +
		"					AND av.number_value = c.card_id \n" +
		"		WHERE \n" +
		"			aoU.option_value is NULL \n" +
		"	) as c \n" + 
		"	JOIN card lc \n" +
		"		ON c.link_card_id=lc.card_id \n" +
		"		AND (c.linked_status_id=lc.status_id OR c.linked_status_id IS NULL) \n" +
		"	JOIN attribute_value pv \n" +
		"		ON lc.card_id=pv.card_id \n" +
		"		AND c.person_attr_code=pv.attribute_code \n" +
		"WHERE \n" +
		"	(c.role_code IS NULL OR pv.number_value=c.person_id) \n" +
		"	AND c.intermed_attr_code IS NULL \n" +
		"	AND c.card_id = :curCardId \n";

	// Access to other cards by this card's attributes (direct link)
	public final static String SELECT_PERSON_ACCESS_LINKED_CARD =
		"SELECT \n" +
		"	lc.card_id, \n" +
		"	r.rule_id, \n" +
		"	pv.number_value, \n" +
		"	pv.attr_value_id \n" +
		"FROM \n" +
		"	attribute_value pv \n" +
		"	JOIN person_access_rule pr \n" +
		"		ON pv.attribute_code=pr.person_attr_code \n" +
		"	LEFT JOIN person_role sr \n" +
		"		ON pr.role_code=sr.role_code \n" +
		"	JOIN access_rule r \n" +
		"		ON pr.rule_id=r.rule_id \n" +
		"	JOIN card c \n" +
		"		ON pv.card_id=c.card_id \n" +
		"		AND (pr.linked_status_id=c.status_id OR pr.linked_status_id IS NULL) \n" +
		"	JOIN attribute_value lv \n" +
		"		ON pv.card_id=lv.number_value \n" +
		"		AND lv.attribute_code=pr.link_attr_code \n" +
		"	JOIN card lc \n" +
		"		ON lv.card_id=lc.card_id \n" +
		//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
		/*"		AND (lc.template_id=r.template_id OR r.template_id is null) \n" +
		"		AND (lc.status_id=r.status_id OR r.status_id is null) \n" +*/
		"		AND (lc.template_id=r.template_id) \n" +
		"		AND (lc.status_id=r.status_id) \n" +
		"WHERE \n" +
		"	(pr.role_code IS NULL OR pv.number_value=sr.person_id) \n" +
		"	AND pr.intermed_attr_code IS NULL \n" +
		"	AND pv.card_id=:curCardId \n" +
		"	AND lc.card_id not in (:otherCards) \n";

	// Access to other cards by this card's attributes (reverse link)
	// подправили запрос, чтобы для беклинков-атрибутов связи текущая карточка являлась самым верхним родителем
	public final static String SELECT_PERSON_ACCESS_BACKLINK_CARD =
		"SELECT \n" +
		"	ct.card_id, \n" +
		"	ct.rule_id, \n" +
		"	ct.person_id as number_value, \n" +
		"	ct.attr_value_id \n" +
		"FROM \n" +
		"	( \n" +
		"		WITH RECURSIVE card_tree(card_id, rule_id, template_id, status_id, link_card_id, person_id, attr_value_id, uplinkAttrCode) AS ( \n" +
		"		SELECT \n" + 
		"			lc.card_id, \n" + 
		"			r.rule_id, \n" + 
		"			lc.template_id, \n" +     
		"			lc.status_id, \n" +
		"			c.card_id as link_card_id, \n" +
		"			pv.number_value as person_id, \n" +
		"			pv.attr_value_id, \n" +
		"			aoU.option_value as uplinkAttrCode \n" +
		"		FROM \n" +
		"			card c \n" +
		"			JOIN attribute_value av \n" +
		"					ON av.card_id=c.card_id \n" +
		"			JOIN card lc \n" +
		"					ON lc.card_id = av.number_value \n" +
		"			JOIN person_access_rule pr \n" +
		"					ON (pr.linked_status_id=c.status_id OR pr.linked_status_id IS NULL) \n" +
		"			JOIN access_rule r \n" + 	
		"					ON r.rule_id=pr.rule_id \n" + 
		"			LEFT JOIN person_role sr \n" +
		"					ON pr.role_code=sr.role_code \n" + 
		"			JOIN attribute_option aoL \n" + 
		"					ON pr.link_attr_code=aoL.attribute_code \n" + 
		"					AND aoL.option_code='" +AttributeOptions.LINK+"' \n" +
		"			LEFT JOIN attribute_option aoU \n" + 
		"					ON pr.link_attr_code=aoU.attribute_code \n" + 
		"					AND aoU.option_code='" +AttributeOptions.UPLINK+"' \n" +
		"			JOIN attribute_value pv \n" +
		"					ON pv.card_id=c.card_id \n" +
		"					AND pr.person_attr_code = pv.attribute_code \n" +
		"		WHERE \n" +
		"			(sr.role_code IS NULL OR pv.number_value=sr.person_id) \n" +
		"			AND av.attribute_code = aoL.option_value \n" +
		"			AND pr.intermed_attr_code IS NULL \n" +
		"			AND pv.card_id=:curCardId \n" +
		"		UNION \n" +
		"		SELECT \n" + 
		"			lc.card_id, \n" + 
		"			c.rule_id, \n" + 
		"			lc.template_id, \n" +     
		"			lc.status_id, \n" +
		"			c.link_card_id, \n" +
		"			c.person_id, \n" +
		"			c.attr_value_id, \n" +
		"			c.uplinkAttrCode \n" +
		"		FROM \n" +
		"			card_tree c \n" +
		"			JOIN attribute_value av \n" +
		"					ON av.card_id=c.card_id \n" +
		"					AND av.attribute_code = c.uplinkAttrCode \n" +
		"			JOIN card lc \n" +
		"					ON lc.card_id = av.number_value \n" +
		"	) \n" +
		"	SELECT \n" + 
		"		c.card_id, \n" + 
		"		c.rule_id, \n" + 
		"		c.person_id, \n" +
		"		c.attr_value_id \n" +
		"	FROM \n" +
		"		card_tree c \n" +
		"		join access_rule r \n" +
		"				ON r.rule_id = c.rule_id \n" +
		"				AND r.template_id = c.template_id \n" +
		"				and r.status_id = c.status_id \n" +
		"	WHERE c.card_id not in (:otherCards)\n" +
		") as ct \n";

	
	// II. =============== Profile rules ===============
	// Access by card's own attributes
	public final static String SELECT_PROFILE_ACCESS_CARD_THIS =
		"SELECT \n" +
		"	c.card_id, \n" +
		"	r.rule_id, \n" +
		"	p.person_id, \n" +
		"	tv.attr_value_id \n" +
		"FROM \n" +
		"	card c \n" +
		"	JOIN access_rule r \n" +
		//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
		/*"		ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
		"		AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
		"		ON (c.template_id=r.template_id) \n" +
		"		AND (c.status_id=r.status_id) \n" +
		"		AND c.card_id in (:curCardIds) \n" +	
		"	JOIN profile_access_rule pr \n" +
		"		ON r.rule_id=pr.rule_id \n" +
		"		AND pr.link_attr_code is NULL \n" +
		"	LEFT JOIN person_role sr \n" +
		"		ON pr.role_code=sr.role_code \n" +
		"	JOIN person p \n" +
		"		ON (pr.role_code IS NULL OR p.person_id=sr.person_id) \n" +
		"	JOIN attribute_value pv \n" +
		"		ON pr.profile_attr_code=pv.attribute_code \n" +
		"	JOIN attribute_value tv \n" +
		"		ON c.card_id=tv.card_id \n" +
		"		AND pr.target_attr_code=tv.attribute_code \n" +
		"		and tv.number_value=pv.number_value \n" +
		"WHERE \n" +
		"	p.card_id=pv.card_id";

	// Access by linked card's attribute (direct link)
	public final static String SELECT_PROFILE_ACCESS_CARD_LINKED =
		"SELECT \n" +
		"	c.card_id, \n" +
		"	r.rule_id, \n" +
		"	p.person_id, \n" +
		"	tv.attr_value_id \n" +
		"FROM \n" +
		"	card c \n" +
		"	JOIN access_rule r \n" +
		//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
		/*"		ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
		"		AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
		"		ON (c.template_id=r.template_id) \n" +
		"		AND (c.status_id=r.status_id) \n" +
		"	JOIN profile_access_rule pr \n" +
		"		ON r.rule_id=pr.rule_id \n" +
		"	LEFT JOIN person_role sr \n" +
		"		ON pr.role_code=sr.role_code \n" +
		"	JOIN person p \n" +
		"		ON (pr.role_code IS NULL OR p.person_id=sr.person_id) \n" +
		"	JOIN attribute_value lv \n" +
		"		ON c.card_id=lv.card_id \n" +
		"		AND pr.link_attr_code=lv.attribute_code \n" +
		"	JOIN card lc \n" +
		"		ON lv.number_value=lc.card_id \n" +
		"		AND (pr.linked_status_id=lc.status_id OR pr.linked_status_id IS NULL) \n" +
		"	JOIN attribute_value pv \n" +
		"		ON pr.profile_attr_code=pv.attribute_code \n" +
		"	JOIN attribute_value tv \n" +
		"		ON lv.number_value=tv.card_id \n" +
		"		AND pr.target_attr_code=tv.attribute_code \n" +
		"       AND tv.number_value=pv.number_value \n" +
		"WHERE \n" +
		"	p.card_id=pv.card_id \n" +
		"	AND pr.intermed_attr_code is NULL \n" +
		"	AND c.card_id=:curCardId \n";

	// Access by linked user card's attribute (direct user link)
	// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
/*	public final static String SELECT_PROFILE_ACCESS_CARD_USER_LINKED =
		"SELECT \n" +
		"	c.card_id, \n" +
		"	r.rule_id, \n" +
		"	p.person_id, \n" +
		"	tv.attr_value_id \n" +
		"FROM \n" +
		"	card c \n" +
		"	JOIN access_rule r \n" +
		"		ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
		"		AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +
		"	JOIN profile_access_rule pr \n" +
		"		ON r.rule_id=pr.rule_id \n" +
		"	LEFT JOIN person_role sr \n" +
		"		ON pr.role_code=sr.role_code \n" +
		"	JOIN person p \n" +
		"		ON (pr.role_code IS NULL OR p.person_id=sr.person_id) \n" +
		"	JOIN attribute_value lv \n" +
		"		ON c.card_id=lv.card_id \n" +
		"		AND pr.link_attr_code=lv.attribute_code \n" +
		"	JOIN person pL \n" +
		"		ON pL.person_id = lv.number_value \n" +
		"	JOIN card lc \n" +
		"		ON lc.card_id = pL.card_id \n" +
		"		AND (pr.linked_status_id=lc.status_id OR pr.linked_status_id IS NULL) \n" +
		"	JOIN attribute_value pv \n" +
		"		ON pr.profile_attr_code=pv.attribute_code \n" +
		"	JOIN attribute_value tv \n" +
		"		ON lv.number_value=tv.card_id \n" +
		"		AND pr.target_attr_code=tv.attribute_code \n" +
		"       AND tv.number_value=pv.number_value \n" +
		"WHERE \n" +
		"	p.card_id=pv.card_id \n" +
		"	AND pr.intermed_attr_code is NULL \n" +
		"	AND c.card_id=:curCardId \n";
	*/
	// Access by linked card's attribute (reverse link)
	// подправили запрос, чтобы для беклинков-атрибутов связи искался самые верхние родители
	public final static String SELECT_PROFILE_ACCESS_CARD_BACKLINK =
		"SELECT \n" +
		"	c.card_id, \n" +
		"	c.rule_id, \n" +
		"	p.person_id, \n" +
		"	tv.attr_value_id \n" +
		"FROM \n" +
		"	( \n" +
		"		SELECT \n" + 
		"			c.card_id, \n" + 
		"			r.rule_id, \n" + 
		"			functionbacklink(c.card_id, aoU.option_value, aoL.option_value) as link_card_id, \n" +     
		"			pr.linked_status_id, \n" +
		"			sr.person_id, \n" +
		"			pr.role_code, \n" +
		"			pr.intermed_attr_code, \n" +		
		"			pr.target_attr_code, \n" +
		"			pr.profile_attr_code \n" +
		"		FROM card c \n" +
		"			JOIN access_rule r \n" + 	
		//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
		/*"					ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
		"					AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
		"					ON 	(c.template_id=r.template_id) \n" + 
		"					AND (c.status_id=r.status_id) \n" +
		"			JOIN profile_access_rule pr \n" +
		"					ON 	r.rule_id=pr.rule_id \n" + 
		"			LEFT JOIN person_role sr \n" +
		"					ON pr.role_code=sr.role_code \n" + 
		"			JOIN attribute_option aoL \n" + 
		"					ON pr.link_attr_code=aoL.attribute_code \n" + 
		"					AND aoL.option_code='" +AttributeOptions.LINK+"' \n" +
		"			JOIN attribute_option aoU \n" + 
		"					ON pr.link_attr_code=aoU.attribute_code \n" + 
		"					AND aoU.option_code='" +AttributeOptions.UPLINK+"' \n" +
		"		UNION ALL \n" +
		"		SELECT \n" + 
		"			c.card_id, \n" + 
		"			r.rule_id, \n" + 
		"			av.card_id as link_card_id, \n" + 
		"			pr.linked_status_id, \n" +
		"			sr.person_id, \n" +
		"			pr.role_code, \n" +
		"			pr.intermed_attr_code, \n" +		
		"			pr.target_attr_code, \n" +
		"			pr.profile_attr_code \n" +
		"		FROM card c \n" +
		"			JOIN access_rule r \n" + 	
		//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
		/*"					ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
		"					AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
		"					ON 	(c.template_id=r.template_id) \n" + 
		"					AND (c.status_id=r.status_id) \n" +
		"			JOIN profile_access_rule pr \n" +
		"					ON 	r.rule_id=pr.rule_id \n" + 
		"			LEFT JOIN person_role sr \n" +
		"					ON pr.role_code=sr.role_code \n" + 
		"			JOIN attribute_option aoL \n" + 
		"					ON pr.link_attr_code=aoL.attribute_code \n" + 
		"					AND aoL.option_code='" +AttributeOptions.LINK+"' \n" +
		"			LEFT JOIN attribute_option aoU \n" + 
		"					ON pr.link_attr_code=aoU.attribute_code \n" + 
		"					AND aoU.option_code='" +AttributeOptions.UPLINK+"' \n" +
		"			JOIN attribute_value av \n" +
		"					ON av.attribute_code = aoL.option_value \n" +
		"					AND av.number_value = c.card_id \n" +
		"		WHERE \n" +
		"			aoU.option_value is NULL \n" +
		"		" +
		"	) as c \n" + 
		"	JOIN card lc \n" +
		"		ON c.link_card_id=lc.card_id \n" +
		"		AND (c.linked_status_id=lc.status_id OR c.linked_status_id IS NULL) \n" +
		"	JOIN attribute_value pv \n" +
		"		ON c.profile_attr_code=pv.attribute_code \n" +
		"	JOIN attribute_value tv \n" +
		"		ON lc.card_id=tv.card_id \n" +
		"		AND c.target_attr_code=tv.attribute_code \n" +
		"		AND tv.number_value=pv.number_value \n" +
		"	JOIN person p \n" +
		"		ON p.card_id=pv.card_id \n" +
		"WHERE \n" +
		"	(c.role_code IS NULL OR p.person_id=c.person_id) \n" +
		"	AND c.intermed_attr_code is NULL \n" +
		"	AND c.card_id = :curCardId \n";

	// Access to other cards by this card's attributes (direct link)
	public final static String SELECT_PROFILE_ACCESS_LINKED_CARD =
		"(with linked_cards as ( --находим связанные карточки попадающие под условия существующих правил \n" +
		"	select pr.role_code, tv.attr_value_id, tv.number_value, pr.profile_attr_code, lc.card_id, r.rule_id \n" +
		"	from card c \n" +
		"	join attribute_value tv \n" +
		"		on  tv.card_id = c.card_id \n" +
		"		and tv.card_id = :curCardId \n" +
		"	join profile_access_rule pr \n" + 
		"		on  tv.attribute_code=pr.target_attr_code \n" +
		"		and (pr.linked_status_id=c.status_id or pr.linked_status_id is null) \n" +
		"		and pr.intermed_attr_code is null \n" +
		"	join access_rule r \n" + 
		"		on pr.rule_id=r.rule_id \n" + 
		"	join attribute_value lv \n" + 
		"		on  tv.card_id=lv.number_value \n" + 
		"		and lv.attribute_code=pr.link_attr_code \n" +  
		"	join card lc \n" +
		"		on  lc.card_id = lv.card_id and lc.card_id not in (:otherCards) \n" +
		//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
		/*"		and (lc.template_id=r.template_id or r.template_id is null) \n" + 
		"		and (lc.status_id=r.status_id or r.status_id is null) \n" +*/ 
		"		and (lc.template_id=r.template_id) \n" + 
		"		and (lc.status_id=r.status_id) \n" + 
		"), persons as ( --находим карточки пользователей, для которых будем давать права \n" +
		"	select li.card_id, p.card_id as person_card, li.number_value, li.profile_attr_code, li.rule_id, li.attr_value_id, p.person_id \n" +
		"	from linked_cards li \n" +
		"	left join person_role sr \n" + 
		"		on li.role_code=sr.role_code \n" + 
		"	join person p \n" + 
		"		on (li.role_code is null or p.person_id=sr.person_id) \n" + 
		"), result as ( --отбираем тех пользователей, кто указан в связанной карточке в заданном правилом атрибуте \n" +
		"	select pp.card_id, pp.rule_id, pp.person_id, pp.attr_value_id \n" +
		"	from persons pp \n" +
		"	join attribute_value pv \n" + 
		"		on pv.card_id = pp.person_card \n" +
		"		and pp.number_value=pv.number_value \n" +
		"		and pp.profile_attr_code=pv.attribute_code \n" +
		") \n" +
		"select \n" + 
		"	card_id, \n" + 
		"	rule_id, \n" + 
		"	person_id, \n" + 
		"	attr_value_id \n" + 
		"from result) \n";

	// Access to other cards by this card's attributes (reverse link)
	// подправили запрос, чтобы для бекликнков-атрибутов связи текущая карточка являлась самым верхним родителем
	public final static String SELECT_PROFILE_ACCESS_BACKLINK_CARD =
		"SELECT \n" +
		"	ct.card_id, \n" +
		"	ct.rule_id, \n" +
		"	ct.person_id, \n" +
		"	ct.attr_value_id \n" +
		"FROM \n" +
		"	( \n" +
		"		WITH RECURSIVE card_tree(card_id, rule_id, template_id, status_id, link_card_id, person_id, attr_value_id, uplinkAttrCode) AS ( \n" +
		"		SELECT \n" + 
		"			lc.card_id, \n" + 
		"			r.rule_id, \n" + 
		"			lc.template_id, \n" +     
		"			lc.status_id, \n" +
		"			c.card_id as link_card_id, \n" +
		"			p.person_id, \n" +
		"			tv.attr_value_id, \n" +
		"			aoU.option_value as uplinkAttrCode \n" +
		"		FROM \n" +
		"			card c \n" +
		"			JOIN attribute_value av \n" +
		"					ON av.card_id=c.card_id \n" +
		"			JOIN card lc \n" +
		"					ON lc.card_id = av.number_value \n" +
		"			JOIN profile_access_rule pr \n" +
		"					ON (pr.linked_status_id=c.status_id OR pr.linked_status_id IS NULL) \n" +
		"			JOIN access_rule r \n" + 	
		"					ON r.rule_id=pr.rule_id \n" + 
		"			LEFT JOIN person_role sr \n" +
		"					ON pr.role_code=sr.role_code \n" + 
		"			JOIN person p \n" +
		"					ON (sr.role_code IS NULL OR p.person_id=sr.person_id) \n" +		
		"			JOIN attribute_option aoL \n" + 
		"					ON pr.link_attr_code=aoL.attribute_code \n" + 
		"					AND aoL.option_code='" +AttributeOptions.LINK+"' \n" +
		"			LEFT JOIN attribute_option aoU \n" + 
		"					ON pr.link_attr_code=aoU.attribute_code \n" + 
		"					AND aoU.option_code='" +AttributeOptions.UPLINK+"' \n" +
		"			JOIN attribute_value pv \n" +
		"					ON pr.profile_attr_code=pv.attribute_code \n" +
		"			JOIN attribute_value tv \n" +
		"					ON tv.attribute_code=pr.target_attr_code \n" +
		"					AND tv.card_id=c.card_id \n" +
		"					AND tv.number_value=pv.number_value \n" +
		"		WHERE \n" +
		"			pv.card_id=p.card_id \n" +
		"			AND av.attribute_code = aoL.option_value \n" +
		"			AND pr.intermed_attr_code IS NULL \n" +
		"			AND tv.card_id=:curCardId \n" +
		"		UNION \n" +
		"		SELECT \n" + 
		"			lc.card_id, \n" + 
		"			c.rule_id, \n" + 
		"			lc.template_id, \n" +     
		"			lc.status_id, \n" +
		"			c.link_card_id, \n" +
		"			c.person_id, \n" +
		"			c.attr_value_id, \n" +
		"			c.uplinkAttrCode \n" +
		"		FROM \n" +
		"			card_tree c \n" +
		"			JOIN attribute_value av \n" +
		"					ON av.card_id=c.card_id \n" +
		"					AND av.attribute_code = c.uplinkAttrCode \n" +
		"			JOIN card lc \n" +
		"					ON lc.card_id = av.number_value \n" +
		"	) \n" +
		"	SELECT \n" + 
		"		c.card_id, \n" + 
		"		c.rule_id, \n" + 
		"		c.person_id, \n" +
		"		c.attr_value_id \n" +
		"	FROM \n" +
		"		card_tree c \n" +
		"		join access_rule r \n" +
		"				ON r.rule_id = c.rule_id \n" +
		"				AND r.template_id = c.template_id \n" +
		"				and r.status_id = c.status_id \n" +
		"	WHERE c.card_id not in (:otherCards)\n" +
		") as ct \n";
	
	public static final String INSERT_INTO_TT_PERSON_PROFILES =
					"insert into person_profiles \n" +
					"	select \n" +
					"		p.person_id, \n" +
					"		av.number_value, \n" +
					"		av.attribute_code \n" +
					"	from \n" +
					"		person p \n" +
					"		join attribute_value av \n" +
					"			on p.card_id = av.card_id \n" +
					"			and av.attribute_code in (select distinct profile_attr_code from profile_access_rule ) \n";

	//все связанные карточки, ссылающиеся на данную по какому-либо UPLINK
	public static final String INSERT_INTO_TT_LINKS = "insert into tt_links \n" +
			"with recursive uplinks as ( \n" +
			"	select \n" +
			"		av.number_value as base, av.card_id as linked, av.attribute_code as aoU, av_aoU.attribute_code as attr \n" +
			"	from \n" +
			"		attribute_value av \n" +
			"		join attribute_option av_aoU \n" +
			"			on av.attribute_code = av_aoU.option_value \n" +
			"			and av_aoU.option_code = 'UPLINK' \n" +
			"	where \n" +
			"		av.number_value in (:curCardIds) \n" +
			"	union \n" +
			"	select \n" +
			"		b.base, av.card_id, av.attribute_code, b.attr \n" +
			"	from \n" +
			"		uplinks b \n" +
			"		join attribute_value av \n" +
			"			on av.number_value = b.linked \n" +
			"			and av.attribute_code = b.aoU \n" +
			"), final_linked_to_current as (  --все связанные с текущей карточки \n" +
			"	select \n" +
			"		up.base, av.card_id as linked, up.attr --связанные по сложному бэклинку \n" +
			"	from \n" +
			"		uplinks up \n" +
			"		join attribute_option av_aoL \n" +
			"			on up.attr = av_aoL.attribute_code \n" +
			"			and av_aoL.option_code = 'LINK' \n" +
			"		join attribute_value av \n" +
			"			on av.number_value = up.linked \n" +
			"			and av.attribute_code = av_aoL.option_value \n" +
			"	union \n" +
			"	select \n" +
			"		av.number_value, av.card_id, av_aoL.attribute_code \n" +
			"	from \n" +
			"		attribute_value av  --связанные по простому бэклинку \n" +
			"		join attribute_option av_aoL \n" +
			"			on av.attribute_code = av_aoL.option_value \n" +
			"			and av_aoL.option_code = 'LINK' \n" +
			"		left join attribute_option av_aoU \n" +
			"			on av_aoL.attribute_code = av_aoU.attribute_code \n" +
			"			and av_aoU.option_code = 'UPLINK' \n" +
			"	where \n" +
			"		av.number_value in (:curCardIds) \n" +
			"		and av_aoU.attribute_code is NULL \n" +
			"	UNION \n" +
			"	select \n" +
			"		av.card_id as base, av.number_value as linked, av.attribute_code as attr --связанные по кардлинку \n" +
			"	from \n" +
			"		attribute_value av \n" +
			"		join attribute a \n" +
			"			on av.attribute_code = a.attribute_code \n" +
			"			and a.data_type in ('C','E') \n" +
			"	where \n" +
			"		av.card_id in (:curCardIds) \n" +
			") \n" +
			"select * from final_linked_to_current \n"; 
	
	//все связанные карточки, которые ссылаются на текущие карточки по какому-либо Бэклинку
	public static final String INSERT_INTO_TT_LINKS_2 = "insert into tt_links2 \n" +
			"WITH RECURSIVE dirty_backlinked as( \n" +
			"	select \n" +
			"		c.card_id, c.status_id, av.number_value, ao2.option_value, ao.attribute_code, 1 as lvl \n" +
			"	from \n" +
			"		card c \n" +
			"		join attribute_value av \n" +
			"			on c.card_id = av.card_id \n" +
			"			and av.number_value is not null \n" +
			"		join attribute a \n" +
			"			on a.attribute_code = av.attribute_code \n" +
			"			and a.data_type in ('C','E') \n" +
			"		join attribute_option ao \n" +
			"			on av.attribute_code = ao.option_value \n" +
			"			and ao.option_code = 'LINK' \n" +
			"		left join attribute_option ao2 \n" +
			"			on ao.attribute_code = ao2.attribute_code \n" +
			"			and ao2.option_code = 'UPLINK' \n" +
			"	where \n" +
			"		c.card_id in (:curCardIds) \n" +
			"	UNION \n" +
			"	select \n" +
			"		t.card_id, t.status_id, av.number_value,t.option_value, t.attribute_code, t.lvl+1 \n" +
			"	from \n" +
			"		dirty_backlinked t \n" +
			"		join attribute_value av \n" +
			"			on t.number_value = av.card_id \n" +
			"			and av.attribute_code = t.option_value \n" +
			"), \n" +
			"final_linked as ( \n" +
			"	select \n" +
			"		t.card_id as this_card, t.status_id as this_status, t.number_value as linked, t.attribute_code as attr  --фильтруем карточки, найденные по бэклинку \n" +
			"	from \n" +
			"		dirty_backlinked t \n" +
			"	where \n" +
			"		(lvl=1 and option_value is null) \n" +
			"		or lvl > 1 \n" +
			"	UNION ALL \n" +
			"	select \n" +
			"		c.card_id as this_card, c.status_id as this_status, av.card_id as linked, av.attribute_code as attr --добавляем карточки, которые ссылаются на текущую по кардлинку \n" +
			"	from \n" +
			"		card c \n" +
			"		join attribute_value av \n" +
			"			on c.card_id = av.number_value \n" +
			"		join attribute a \n" +
			"			on av.attribute_code = a.attribute_code \n" +
			"			and a.data_type in ('C','E') \n" +
			"	where \n" +
			"		av.number_value in (:curCardIds) and c.card_id in (:curCardIds) \n" +
			") \n" +
			"select * from final_linked \n";
	
	public final static String SELECT_NEW_PROFILE_ACCESS_CARD_THIS =
			"SELECT  distinct \n" +
					"	c.card_id,  \n" +
					"	r.rule_id,  \n" +
					"	pp.person_id,  \n" +
					"	av.attr_value_id  \n" +
					"FROM  \n" +
					"	card c  \n" +
					"	JOIN access_rule r  \n" +
					"		ON (c.template_id=r.template_id)  \n" +
					"		AND (c.status_id=r.status_id)  \n" +
					"		AND c.card_id in (:curCardIds)  \n" +
					"	JOIN profile_access_rule pr  \n" +
					"		ON r.rule_id=pr.rule_id  \n" +
					"		AND pr.link_attr_code is NULL  \n" +
					"	JOIN attribute_value av \n" +
					"		ON av.card_id = c.card_id and \n" +
					"		pr.target_attr_code=av.attribute_code  \n" +
					"	join person_profiles pp  \n" +
					"		on pp.number_value = av.number_value  \n" +
					"		and pr.profile_attr_code = pp.attribute_code \n" +
					"	left join person_role sr  \n" +
					"		on pp.person_id = sr.person_id   \n" +
					"where \n" +
					"	pr.role_code=sr.role_code or pr.role_code is null \n";
	
	public static final String SELECT_PERSON_ACCESS_BY_LINKED_CARDS = 
			"select \n" +
					"	distinct c.card_id, r.rule_id, p.person_id, av.attr_value_id \n" +
					"from \n" +
					"	tt_links b \n" +
					"	join card c \n" +
					"		on c.card_id = b.base \n" +
					"	join card lc \n" +
					"		on lc.card_id = b.linked \n" +
					"	join access_rule r \n" +
					"		on c.template_id = r.template_id \n" +
					"		and c.status_id = r.status_id \n" +
					"	join person_access_rule pr \n" +
					"		on r.rule_id = pr.rule_id \n" +
					"		and pr.intermed_attr_code IS NULL \n" +
					"		and pr.link_attr_code = b.attr \n" +
					"		and (pr.linked_status_id is NULL or pr.linked_status_id = lc.status_id) \n" +
					"	join attribute_value av \n" +
					"		on lc.card_id = av.card_id and av.attribute_code = pr.person_attr_code \n" +
					"	join person p \n" +
					"		on av.number_value = p.person_id \n" +
					"	left join person_role sr \n" +
					"		on p.person_id = sr.person_id \n" +
					"where \n" +
					"	(pr.role_code=sr.role_code or pr.role_code is null) \n" +
					"	and c.card_id not in (:otherCards) \n";
	
	public static final String SELECT_PERSON_ACCESS_TO_LINKED_CARDS =
			"select \n" +
					"	distinct t2.linked, r.rule_id, av.number_value, av.attr_value_id \n" +
					"from \n" +
					"	tt_links2 t2 \n" +
					"	join card lc \n" +
					"		on lc.card_id = t2.linked \n" +
					"	join access_rule r \n" +
					"		on lc.status_id = r.status_id \n" +
					"		and lc.template_id = r.template_id \n" +
					"	join person_access_rule pr \n" +
					"		on r.rule_id = pr.rule_id \n" +
					"		and pr.intermed_attr_code IS NULL \n" +
					"		and pr.link_attr_code = t2.attr \n" +
					"		and (pr.linked_status_id is NULL or pr.linked_status_id = t2.this_status) \n" +
					"	join attribute_value av \n" +
					"		on t2.this_card = av.card_id \n" +
					"		and av.attribute_code = pr.person_attr_code \n" +
					"	join person p \n" +
					"		on av.number_value = p.person_id \n" +
					"	left join person_role sr \n" +
					"		on p.person_id = sr.person_id \n" +
					"where \n" +
					"	(pr.role_code=sr.role_code or pr.role_code is null) \n" +
					"	and t2.linked not in (:otherCards) \n";
	
	public static final String SELECT_PROFILE_ACCESS_BY_LINKED_CARDS =
			"select \n" +
					"	distinct c.card_id, r.rule_id, pp.person_id, av.attr_value_id \n" +
					"from \n" +
					"	tt_links b \n" +
					"	join card c \n" +
					"		on c.card_id = b.base \n" +
					"	join card lc \n" +
					"		on lc.card_id = b.linked \n" +
					"	join access_rule r \n" +
					"		on c.template_id = r.template_id \n" +
					"		and c.status_id = r.status_id \n" +
					"	join profile_access_rule pr \n" +
					"		on r.rule_id = pr.rule_id \n" +
					"		and pr.intermed_attr_code IS NULL \n" +
					"		and pr.link_attr_code = b.attr \n" +
					"		and (pr.linked_status_id is NULL or pr.linked_status_id = lc.status_id) \n" +
					"	join attribute_value av \n" +
					"		on lc.card_id = av.card_id \n" +
					"		and av.attribute_code = pr.target_attr_code \n" +
					"	join person_profiles pp \n" +
					"		on pp.number_value = av.number_value \n" +
					"		and pr.profile_attr_code = pp.attribute_code \n" +
					"	left join person_role sr \n" +
					"		on pp.person_id = sr.person_id \n" +
					"where \n" +
					"	c.card_id not in (:otherCards) \n" +
					"	and (pr.role_code=sr.role_code or pr.role_code is null) \n";
	
	public static final String SELECT_PROFILE_ACCESS_TO_LINKED_CARDS =
			"select \n" +
					"	distinct t2.linked, r.rule_id, pp.person_id, av.attr_value_id \n" +
					"from \n" +
					"	tt_links2 t2 \n" +
					"	join card lc \n" +
					"		on lc.card_id = t2.linked \n" +
					"	join access_rule r \n" +
					"		on lc.status_id = r.status_id \n" +
					"		and lc.template_id = r.template_id \n" +
					"	join profile_access_rule pr \n" +
					"		on r.rule_id = pr.rule_id \n" +
					"		and pr.intermed_attr_code IS NULL \n" +
					"		and pr.link_attr_code = t2.attr \n" +
					"		and (pr.linked_status_id is NULL or pr.linked_status_id = t2.this_status ) \n" +
					"	join attribute_value av \n" +
					"		on t2.this_card = av.card_id \n" +
					"		and av.attribute_code = pr.target_attr_code \n" +
					"	join person_profiles pp \n" +
					"		on pp.number_value = av.number_value \n" +
					"		and pr.profile_attr_code = pp.attribute_code \n" +
					"	left join person_role sr \n" +
					"		on pp.person_id = sr.person_id \n" +
					"where \n" +
					"	t2.linked not in (:otherCards) \n" +
					"	and (pr.role_code=sr.role_code or pr.role_code is null) \n";
	
			
	public boolean isCardActive(ObjectId cardId) {
		int is_active = jdbc.queryForInt("select count(is_active) from card where card_id=? and is_active = 1", 
				new Object[] { cardId.getId() }, 
				new int[] { Types.NUMERIC });
		return is_active == 1;
	}
	
	public List<Long> removeInactiveCards(List<ObjectId> cardIds){
		return jdbc.queryForList("select distinct card_id from card where card_id in (" + ObjectIdUtils.numericIdsToCommaDelimitedString(cardIds) + ") and is_active = 1",
				Long.class);
		
	}
	
	/**
	 * Recalculates all permissions granted by card's attributes.
	 * Should be called after changing the card.
	 * 
	 * @param cardId ID of the card
	 */
	public void updateAccessByCard(final ObjectId cardId, List<Long> otherCards) {
		if (cardId == null || !Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card ID");
		//для неактивных карточек ничего не считаем
		if (!isCardActive(cardId)) {
			logger.info("Inactive card: "+cardId.getId() + ". Skipping update access_list.");
			return;
		}
		long start = System.currentTimeMillis();
		if(otherCards == null){
			otherCards = new ArrayList<Long>();
		}
		otherCards.add(-1L);
		final NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbc);
		String str = "INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) " +
				// Person access rules
				SELECT_PERSON_ACCESS_CARD_THIS +
				" UNION ALL \n" + SELECT_PERSON_ACCESS_CARD_LINKED +
// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующую строчку необходимо раскомментарить
//				" UNION ALL \n" + SELECT_PERSON_ACCESS_CARD_USER_LINKED +
				//" UNION ALL " + SELECT_PERSON_ACCESS_CARD_BACKLINK +		// Not needed - nothing changed for the card
				" UNION ALL \n" + SELECT_PERSON_ACCESS_LINKED_CARD +
				" UNION ALL \n" + SELECT_PERSON_ACCESS_BACKLINK_CARD +
				// Profile access rules
				" UNION ALL \n" + SELECT_PROFILE_ACCESS_CARD_THIS +
				" UNION ALL \n" + SELECT_PROFILE_ACCESS_CARD_LINKED +
// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующую строчку необходимо раскомментарить
//				" UNION ALL \n" + SELECT_PROFILE_ACCESS_CARD_USER_LINKED +
				//" UNION ALL " + SELECT_PROFILE_ACCESS_CARD_BACKLINK +		// Not needed - nothing changed for the card
				" UNION ALL \n" + SELECT_PROFILE_ACCESS_LINKED_CARD +
				" UNION ALL \n" + SELECT_PROFILE_ACCESS_BACKLINK_CARD; 
		final MapSqlParameterSource args = new MapSqlParameterSource();
		args.addValue("curCardId", cardId.getId(), Types.NUMERIC);
		args.addValue("otherCards", otherCards, Types.NUMERIC);
		
		long rows = namedParameterJdbcTemplate.update(str, args);
		logger.info("Direct rules resolved: " + (System.currentTimeMillis() - start) + "ms by " + rows + " rows by card "+cardId.getId());
		
		rows += updateAccessWithIndirectLinks(Collections.singletonList((Long)cardId.getId()), true, otherCards);
		
		long duration = System.currentTimeMillis() - start;
		logger.info(rows + " permission(s) added (" + duration + "ms) by card "+cardId.getId());
	}
	
	public void updateAccessByCard(final ObjectId cardId) {
		updateAccessByCard(cardId, null);
	}

	/**
	 * Recalculates all permissions to specific card.
	 * Should be called after changing of card's state or template.
	 * 
	 * @param cardId ID of the card
	 */
	public void updateAccessToCard(List<ObjectId> cardIds, List<Long> otherCards) {
		for(ObjectId cardId: cardIds){
		if (cardId == null || !Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card ID");
		}
		long start = System.currentTimeMillis();
		//для неактивных карточек ничего не считаем
		List<Long> activeCardIds = removeInactiveCards(cardIds);
		if (activeCardIds.isEmpty()) {
			logger.info("all cards are inactive: " + ObjectIdUtils.numericIdsToCommaDelimitedString(cardIds));
			return;
		}
		if(otherCards == null){
			otherCards = new ArrayList<Long>();
		}
		if(otherCards.isEmpty()){
			otherCards.add(-1L);
		}
		final NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbc);
		String str = "INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) " +
				// Person access rules
				SELECT_PERSON_ACCESS_CARD_THIS +
				" UNION ALL \n" + SELECT_PERSON_ACCESS_BY_LINKED_CARDS +
				" UNION ALL \n" + SELECT_PERSON_ACCESS_TO_LINKED_CARDS +
				// Profile access rules
				" UNION ALL \n" + SELECT_NEW_PROFILE_ACCESS_CARD_THIS +
				" UNION ALL \n" + SELECT_PROFILE_ACCESS_BY_LINKED_CARDS +
				" UNION \n" + SELECT_PROFILE_ACCESS_TO_LINKED_CARDS;
		
		final MapSqlParameterSource args = new MapSqlParameterSource();
		args.addValue("curCardIds", activeCardIds);
		args.addValue("otherCards", otherCards, Types.NUMERIC);
		
		
		Boolean exists = (Boolean) jdbc.queryForObject("SELECT EXISTS ( SELECT 1 FROM information_schema.tables WHERE table_name = 'tt_links')", Boolean.class);
		if(!exists){
			jdbc.execute("create temporary table IF NOT EXISTS tt_links (base numeric, linked numeric, attr character varying) ON COMMIT drop");
			jdbc.execute("create temporary table IF NOT EXISTS tt_links2 (this_card numeric, this_status numeric, linked numeric, attr character varying) ON COMMIT drop");
			jdbc.execute("create temporary table IF NOT EXISTS person_profiles (person_id numeric, number_value numeric, attribute_code character varying) ON COMMIT drop");
	
			jdbc.execute("CREATE INDEX ON tt_links using btree (base)");
			jdbc.execute("CREATE INDEX ON tt_links2 using btree (this_card)");
			jdbc.execute("CREATE INDEX ON person_profiles using btree(number_value)");
			
			namedParameterJdbcTemplate.update(INSERT_INTO_TT_PERSON_PROFILES, args);
		}
		namedParameterJdbcTemplate.update(INSERT_INTO_TT_LINKS, args);
		namedParameterJdbcTemplate.update(INSERT_INTO_TT_LINKS_2, args);
		
		 
		long rows = namedParameterJdbcTemplate.update(str, args);
		
		jdbc.execute("truncate table tt_links");
		jdbc.execute("truncate table tt_links2");
		
		 
		logger.info("Direct rules resolved: " + (System.currentTimeMillis() - start) + "ms by " + rows + " rows to cards " + ObjectIdUtils.numericIdsToCommaDelimitedString(activeCardIds));
		
		rows += updateAccessWithIndirectLinks(activeCardIds, true/*false*/, otherCards);
		
		long duration = System.currentTimeMillis() - start;
	}
	
	public void updateAccessToCard(ObjectId cardId) {
		updateAccessToCard(Collections.singletonList(cardId), null);
	}
	
	private final static Integer CARD_STARTING = new Integer(1);
	private final static Integer CARD_INTERMEDIATE = new Integer(2);
	private final static Integer CARD_ENDING = new Integer(3);

	private final static Integer LINK_FWD = new Integer(11);
	private final static Integer LINK_BACK = new Integer(12);

	private final static Integer LINK_FWD_FWD = new Integer(11);
	private final static Integer LINK_FWD_BACK = new Integer(12);
	private final static Integer LINK_BACK_FWD = new Integer(13);
	private final static Integer LINK_BACK_BACK = new Integer(14);
	
	private final static Integer LINK_USR_USR = new Integer(15);
	private final static Integer LINK_USR_FWD = new Integer(16);
	private final static Integer LINK_USR_BACK = new Integer(17);
	private final static Integer LINK_FWD_USR = new Integer(18);
	private final static Integer LINK_BACK_USR = new Integer(19);

	private static final Integer RULE_PERSON = new Integer(21);
	private static final Integer RULE_PROFILE = new Integer(22);
	
	// Query returning all the complex links defined in different access rules, along with rule type constant
	private static final String LINK_RULE_TABLE =
		"(SELECT r.link_attr_code, r.intermed_attr_code, " + RULE_PERSON + " as rule_type \n" +
	    "FROM person_access_rule r \n" +
	    "UNION \n" +
		"SELECT r.link_attr_code, r.intermed_attr_code, " + RULE_PROFILE + " as rule_type \n" +
	    "FROM profile_access_rule r)";
	
	@Deprecated
	private int updateAccessWithIndirectLinksOld(ObjectId cardId, boolean affiliated) {
		int rows = 0;
		
		/* TODO
		 * When we're updating access to the requested card only (affiliated=false),
		 * we can filter out the rules right away by card's state and template.
		 * But this requires to make modifications in several points of single query,
		 * so conditional constructions will seem too complex, and maybe it will be
		 * better to have separate versions of queries for this case.
		 */
		
		// Gathering linked cards
		final SetMap mapTypes = new SetMap();
		
		class LinkBuilder implements RowCallbackHandler {
			private Integer linkType;
			
			public LinkBuilder(Integer linkType) {
				this.linkType = linkType;
			}
			
			public void processRow(ResultSet rs) throws SQLException {
				ComplexLink link = new ComplexLink(rs.getString(1), rs.getString(2));
				mapTypes.put(link, new Integer(rs.getInt(3)));		// rule type - RULE_XXX values
				mapTypes.put(link, new Integer(rs.getInt(4)));		// link type - LINK_XXX values
				mapTypes.put(link, linkType);
			}
		}
		
		// Finding complex links in which the card is starting (поиск комплексных ссылок для текущей карточки в качестве начальной)
		jdbc.query(
				"SELECT pr.link_attr_code, pr.intermed_attr_code, pr.rule_type, " + LINK_FWD + " \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_value v ON pr.link_attr_code=v.attribute_code \n" +
				"WHERE v.card_id=? \n" +
				"UNION \n" +		// Replaces DISTINCT in both queries
				"SELECT ol.option_value, ou.option_value, pr.rule_type, " + LINK_BACK + " \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_option ol ON pr.link_attr_code=ol.attribute_code \n" +
					"AND ol.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option ou ON pr.link_attr_code=ou.attribute_code \n" +
					"AND ou.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"JOIN attribute_value v ON ol.option_value=v.attribute_code \n" +
				"WHERE v.card_id=?",
				new Object[] { cardId.getId(), cardId.getId() },
				new LinkBuilder(CARD_STARTING));
		// Finding complex links in which the card is intermediate (поиск комплексных ссылок, в которых текущая карточка промежуточная)
		jdbc.query(
				"SELECT pr.link_attr_code, pr.intermed_attr_code, pr.rule_type, " + LINK_FWD + " \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_value v ON pr.intermed_attr_code=v.attribute_code \n" +
				"WHERE v.card_id=? \n" +
				"UNION \n" +
				"SELECT ol.option_value, ou.option_value, pr.rule_type, " + LINK_BACK + " \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_option ol ON pr.link_attr_code=ol.attribute_code \n" +
					"AND ol.option_code='" + AttributeOptions.LINK + "' \n" +
				"JOIN attribute_option ou ON pr.link_attr_code=ou.attribute_code \n" +
					"AND ou.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"JOIN attribute_value v ON ou.option_value=v.attribute_code \n" +
				"WHERE v.card_id=?",
				new Object[] { cardId.getId(), cardId.getId() },
				new LinkBuilder(CARD_INTERMEDIATE));

		// Finding complex links in which the card is ending (поиск комплексных ссылок, в которых текущая карточка является последней)
		jdbc.query(
				"SELECT pr.link_attr_code, pr.intermed_attr_code, pr.rule_type, " + LINK_FWD + " \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_value v ON (pr.link_attr_code=v.attribute_code OR pr.intermed_attr_code=v.attribute_code) \n" +
				"WHERE pr.intermed_attr_code IS NOT NULL AND v.number_value=? \n" +
				"UNION \n" +
				"SELECT ol.option_value, ou.option_value, pr.rule_type, " + LINK_BACK + " \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_option ol ON pr.link_attr_code=ol.attribute_code \n" +
					"AND ol.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option ou ON pr.link_attr_code=ou.attribute_code \n" +
					"AND ou.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"JOIN attribute_value v ON (ol.option_value=v.attribute_code OR ou.option_value=v.attribute_code) \n" +
				"WHERE v.number_value=?",
				new Object[] { cardId.getId(), cardId.getId() },
				new LinkBuilder(CARD_ENDING));
		
		// Resolving rules with indirect links
		for (Iterator itr = mapTypes.keySet().iterator(); itr.hasNext(); ) {
			ComplexLink link = (ComplexLink) itr.next();
			HashSet types = mapTypes.get(link);
			
			HashSet links = new HashSet();
			if (types.contains(CARD_STARTING) || types.contains(CARD_INTERMEDIATE))
				links = collectLinksDownOld(cardId, link, types.contains(CARD_STARTING));
			if (types.contains(CARD_ENDING))
				links.add(cardId.getId());
			if (links.size() == 0)
				continue;
			
			HashSet sources = new HashSet();
			if (types.contains(CARD_ENDING))
				sources = collectLinksUpOld(cardId, link);
			if (types.contains(CARD_STARTING))
				sources.add(cardId.getId());
			if (sources.size() == 0)
				continue;
			
			if (types.contains(LINK_FWD) && types.contains(RULE_PERSON)) {
				ArrayList params = new ArrayList(links.size() + sources.size() + 2);
				if (affiliated)
					params.addAll(sources);
				else
					params.add(cardId.getId());
				params.addAll(links);
				params.add(link.linkAttrId);
				params.add(link.intermedAttrId);
				rows += jdbc.update(
						"INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) " +
						// Person access rules with forward links
						"SELECT c.card_id, r.rule_id, v.number_value, v.attr_value_id " +
						"FROM person_access_rule pr " +
						"LEFT JOIN person_role sr ON pr.role_code=sr.role_code " +
						"JOIN access_rule r ON pr.rule_id=r.rule_id " +
						"JOIN card c ON (c.template_id=r.template_id OR r.template_id IS NULL) " +
							"AND (c.status_id=r.status_id OR r.status_id IS NULL) " +
						"JOIN attribute_value v ON pr.person_attr_code=v.attribute_code " +
						"JOIN card cl ON v.card_id=cl.card_id " +
							"AND (pr.linked_status_id IS NULL OR pr.linked_status_id=cl.status_id)" +
						"WHERE (pr.role_code IS NULL OR v.number_value=sr.person_id) " +
							"AND c.card_id" + (affiliated ? " IN (" + populateParameters(sources.size()) + ") " : "=? ") +
							"AND v.card_id IN (" + populateParameters(links.size()) + ") " +
							"AND pr.link_attr_code=? " +
							"AND pr.intermed_attr_code=?",
						params.toArray());
			}
			if (types.contains(LINK_BACK) && types.contains(RULE_PERSON)) {
				ArrayList params = new ArrayList(links.size() + sources.size() + 2);
				params.addAll(sources);
				if (affiliated)
					params.addAll(links);
				else
					params.add(cardId.getId());
				params.add(link.linkAttrId);
				params.add(link.intermedAttrId);
				rows += jdbc.update(
						"INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) " +
						// Person access rules with backward links
						"SELECT c.card_id, r.rule_id, v.number_value, v.attr_value_id " +
						"FROM person_access_rule pr " +
						"LEFT JOIN person_role sr ON pr.role_code=sr.role_code " +
						"JOIN access_rule r ON pr.rule_id=r.rule_id " +
						"JOIN card c ON (c.template_id=r.template_id OR r.template_id IS NULL) " +
							"AND (c.status_id=r.status_id OR r.status_id IS NULL) " +
						"JOIN attribute_option ol ON pr.link_attr_code=ol.attribute_code " +
							"AND ol.option_code='" + AttributeOptions.LINK + "' " +
						"LEFT JOIN attribute_option ou ON pr.link_attr_code=ou.attribute_code " +
							"AND ou.option_code='" + AttributeOptions.UPLINK + "' " +
						"JOIN attribute_value v ON pr.person_attr_code=v.attribute_code " +
						"JOIN card cl ON v.card_id=cl.card_id " +
							"AND (pr.linked_status_id IS NULL OR pr.linked_status_id=cl.status_id) " +
						"WHERE (pr.role_code IS NULL OR v.number_value=sr.person_id) " +
							"AND v.card_id IN (" + populateParameters(sources.size()) + ") " +
							"AND c.card_id" + (affiliated ? " IN (" + populateParameters(links.size()) + ") " : "=? ") +
							"AND ol.option_value=? " +
							"AND coalesce(ou.option_value, '') = coalesce(?, '')",
						params.toArray());
			}
			if (types.contains(LINK_FWD) && types.contains(RULE_PROFILE)) {
				ArrayList params = new ArrayList(links.size() + sources.size() + 2);
				if (affiliated)
					params.addAll(sources);
				else
					params.add(cardId.getId());
				params.addAll(links);
				params.add(link.linkAttrId);
				params.add(link.intermedAttrId);
				rows += jdbc.update(
						"INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) " +
						// Profile access rules with forward links
						"SELECT c.card_id, r.rule_id, p.person_id, v.attr_value_id " +
						"FROM profile_access_rule pr " +
						"LEFT JOIN person_role sr ON pr.role_code=sr.role_code " +
						"JOIN access_rule r ON pr.rule_id=r.rule_id " +
						"JOIN card c ON (c.template_id=r.template_id OR r.template_id IS NULL) " +
							"AND (c.status_id=r.status_id OR r.status_id IS NULL) " +
						"JOIN attribute_value v ON pr.target_attr_code=v.attribute_code " +
						"JOIN attribute_value pv ON v.number_value=pv.number_value " +
							"AND pv.attribute_code=pr.profile_attr_code " +
						"JOIN person p ON pv.card_id=p.card_id " +
						"WHERE (pr.role_code IS NULL OR p.person_id=sr.person_id) " +
							"AND c.card_id" + (affiliated ? " IN (" + populateParameters(sources.size()) + ") " : "=? ") +
							"AND v.card_id IN (" + populateParameters(links.size()) + ") " +
							"AND pr.link_attr_code=? " +
							"AND pr.intermed_attr_code=?",
						params.toArray());
			}
			if (types.contains(LINK_BACK) && types.contains(RULE_PROFILE)) {
				ArrayList params = new ArrayList(links.size() + sources.size() + 2);
				params.addAll(sources);
				if (affiliated)
					params.addAll(links);
				else
					params.add(cardId.getId());
				params.add(link.linkAttrId);
				params.add(link.intermedAttrId);
				rows += jdbc.update(
						"INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) " +
						// Profile access rules with backward links
						"SELECT c.card_id, r.rule_id, p.person_id, v.attr_value_id " +
						"FROM profile_access_rule pr " +
						"LEFT JOIN person_role sr ON pr.role_code=sr.role_code " +
						"JOIN access_rule r ON pr.rule_id=r.rule_id " +
						"JOIN card c ON (c.template_id=r.template_id OR r.template_id IS NULL) " +
							"AND (c.status_id=r.status_id OR r.status_id IS NULL) " +
						"JOIN attribute_option ol ON pr.link_attr_code=ol.attribute_code " +
							"AND ol.option_code='" + AttributeOptions.LINK + "' " +
						"LEFT JOIN attribute_option ou ON pr.link_attr_code=ou.attribute_code " +
							"AND ou.option_code='" + AttributeOptions.UPLINK + "' " +
						"JOIN attribute_value v ON pr.target_attr_code=v.attribute_code " +
						"JOIN attribute_value pv ON v.number_value=pv.number_value " +
							"AND pv.attribute_code=pr.profile_attr_code " +
						"JOIN person p ON pv.card_id=p.card_id " +
						"WHERE (pr.role_code IS NULL OR p.person_id=sr.person_id) " +
							"AND v.card_id IN (" + populateParameters(sources.size()) + ") " +
							"AND c.card_id" + (affiliated ? " IN (" + populateParameters(links.size()) + ") " : "=? ") +
							"AND ol.option_value=? " +
							"AND coalesce(ou.option_value, '')=coalesce(?, '')",
						params.toArray());
			}
		}
		
		return rows;
	}

	/**
	 * Новый метод обновления прав на карточки за счет связанных по атрибуту промежуточной связи карточек с учетом того, 
	 * что и атрибут связи, и атрибут промежуточной связи могут быть как кардлинками, так и беклинками
	 * @param cardId - текущая карточка, для которой прописываем права
	 * @param affiliated: true - значит обновлять права для связанных с текущей карточtr, false - только для текущей 
	 * @return
	 */
	private int updateAccessWithIndirectLinks(List<Long> activeCardIds, boolean affiliated, List<Long> otherCards) {
		int rows = 0;
		
		/* TODO
		 * When we're updating access to the requested card only (affiliated=false),
		 * we can filter out the rules right away by card's state and template.
		 * But this requires to make modifications in several points of single query,
		 * so conditional constructions will seem too complex, and maybe it will be
		 * better to have separate versions of queries for this case.
		 */
		
		// Gathering linked cards
		final SetMap mapTypes = new SetMap();
		
		class LinkBuilder implements RowCallbackHandler {
			private Integer linkType;
			private Long cardId;
			
			public LinkBuilder(Integer linkType) {
				this.linkType = linkType;
			}
			
			public void processRow(ResultSet rs) throws SQLException {
				ComplexLink link = new ComplexLink(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getLong(7), linkType);
				mapTypes.put(link, new Integer(rs.getInt(5)));		// rule type - RULE_XXX values
				mapTypes.put(link, new Integer(rs.getInt(6)));		// link type - LINK_XXX values
				mapTypes.put(link, linkType);
			}
		}
		
		// Finding complex links in which the card is starting (поиск комплексных ссылок для текущей карточки в качестве начальной)
		// добавляем обработку LINK-ов и UPLINK-ов для атрибута промежуточной связи
		
		final NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbc);
		final MapSqlParameterSource args = new MapSqlParameterSource();
		args.addValue("curCardIds", activeCardIds);
		
		namedParameterJdbcTemplate.query(
				// атрибут связи и атрибут промежуточной связи - кардлинки
				"SELECT pr.link_attr_code, null, pr.intermed_attr_code, null, pr.rule_type, " + LINK_FWD_FWD + ", v.card_id \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_value v ON pr.link_attr_code=v.attribute_code \n" +
				"JOIN attribute al ON pr.link_attr_code=al.attribute_code and al.data_type in ('C', 'E') \n" +
				"JOIN attribute ai ON pr.intermed_attr_code=ai.attribute_code and ai.data_type in ('C', 'E') \n" +
				"WHERE v.card_id in (:curCardIds) \n" +
				"UNION \n" +		
				// атрибут связи - кардлинк, а атрибут промежуточной связи - беклинк
				"SELECT pr.link_attr_code, null, oil.option_value, COALESCE(oiu.option_value, ''), pr.rule_type, " + LINK_FWD_BACK + ", vl.card_id \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_value vl ON pr.link_attr_code=vl.attribute_code \n" +
				"JOIN attribute al ON pr.link_attr_code=al.attribute_code and al.data_type in ('C', 'E') \n" +
				"JOIN attribute_option oil ON pr.intermed_attr_code=oil.attribute_code \n" +
					"AND oil.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option oiu ON pr.intermed_attr_code=oiu.attribute_code \n" +
					"AND oiu.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"WHERE vl.card_id in (:curCardIds) \n" +
				"UNION \n" +
				// атрибут связи - беклинк, атрибут промежуточной связи - кардлинк
				"SELECT oll.option_value, COALESCE(olu.option_value, ''), pr.intermed_attr_code, null, pr.rule_type, " + LINK_BACK_FWD + ", vi.number_value \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_option oll ON pr.link_attr_code=oll.attribute_code \n" +
					"AND oll.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option olu ON pr.link_attr_code=olu.attribute_code \n" +
					"AND olu.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"JOIN attribute_value vi ON pr.intermed_attr_code=vi.attribute_code \n" +
				"JOIN attribute ai ON pr.intermed_attr_code=ai.attribute_code and ai.data_type in ('C', 'E') \n" +
				"WHERE vi.number_value in (:curCardIds) \n" +
				"UNION \n" +
				// атрибут связи и атрибут промежуточной связи - беклинки
				"SELECT oll.option_value, COALESCE(olu.option_value, ''), oil.option_value, COALESCE(oiu.option_value, ''), pr.rule_type, " + LINK_BACK_BACK + ", vi.card_id \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_option oll ON pr.link_attr_code=oll.attribute_code \n" +
					"AND oll.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option olu ON pr.link_attr_code=olu.attribute_code \n" +
					"AND olu.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"JOIN attribute_option oil ON pr.intermed_attr_code=oil.attribute_code \n" +
					"AND oil.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option oiu ON pr.intermed_attr_code=oiu.attribute_code \n" +
					"AND oiu.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"JOIN attribute_value vi ON (oil.option_value=vi.attribute_code) \n" +
				"WHERE vi.card_id in (:curCardIds) \n"/* +
				// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
				"UNION \n" +
				// АС и АПС - ПЛ
				"SELECT pr.link_attr_code, null, pr.intermed_attr_code, null, pr.rule_type, " + LINK_USR_USR + " \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_value v ON pr.link_attr_code=v.attribute_code \n" +
				"JOIN attribute al ON pr.link_attr_code=al.attribute_code and al.data_type = 'U' \n" +
				"JOIN attribute ai ON pr.intermed_attr_code=ai.attribute_code and ai.data_type = 'U' \n" +
				"WHERE v.card_id=? \n" +
				"UNION \n" +		
				// АС-ПЛ, АПС-КЛ
				"SELECT pr.link_attr_code, null, pr.intermed_attr_code, null, pr.rule_type, " + LINK_USR_FWD + " \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_value v ON pr.link_attr_code=v.attribute_code \n" +
				"JOIN attribute al ON pr.link_attr_code=al.attribute_code and al.data_type = 'U' \n" +
				"JOIN attribute ai ON pr.intermed_attr_code=ai.attribute_code and ai.data_type in ('C', 'E') \n" +
				"WHERE v.card_id=? \n" +
				"UNION \n" +		
				// АС-ПЛ, АПС-БЛ
				"SELECT pr.link_attr_code, null, oil.option_value, COALESCE(oiu.option_value, ''), pr.rule_type, " + LINK_USR_BACK + " \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_value vl ON pr.link_attr_code=vl.attribute_code \n" +
				"JOIN attribute al ON pr.link_attr_code=al.attribute_code and al.data_type = 'U' \n" +
				"JOIN attribute_option oil ON pr.intermed_attr_code=oil.attribute_code \n" +
					"AND oil.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option oiu ON pr.intermed_attr_code=oiu.attribute_code \n" +
					"AND oiu.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"WHERE vl.card_id=? \n" +
				"UNION \n" +
				// АС-КЛ, АПС-ПЛ
				"SELECT pr.link_attr_code, null, pr.intermed_attr_code, null, pr.rule_type, " + LINK_FWD_USR + " \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_value v ON pr.link_attr_code=v.attribute_code \n" +
				"JOIN attribute al ON pr.link_attr_code=al.attribute_code and al.data_type in ('C', 'E') \n" +
				"JOIN attribute ai ON pr.intermed_attr_code=ai.attribute_code and ai.data_type = 'U' \n" +
				"WHERE v.card_id=?"*/,
				args,
				new LinkBuilder(CARD_STARTING));
		// Finding complex links in which the card is intermediate (поиск комплексных ссылок, в которых текущая карточка промежуточная)
		namedParameterJdbcTemplate.query(
				// атрибут связи и атрибут промежуточной связи - кардлинки
				"SELECT pr.link_attr_code, null, pr.intermed_attr_code, null, pr.rule_type, " + LINK_FWD_FWD + ", vi.card_id \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute al ON pr.link_attr_code=al.attribute_code and al.data_type in ('C', 'E') \n" +
				"JOIN attribute_value vi ON pr.intermed_attr_code=vi.attribute_code \n" +
				"JOIN attribute ai ON pr.intermed_attr_code=ai.attribute_code and ai.data_type in ('C', 'E') \n" +
				"WHERE vi.card_id in (:curCardIds) \n" +
				"UNION \n" +
				// атрибут связи - кардлинк, а атрибут промежуточной связи - беклинк
				"SELECT pr.link_attr_code, null, oil.option_value, COALESCE(oiu.option_value, ''), pr.rule_type, " + LINK_FWD_BACK + ", vi.number_value \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_option oil ON pr.intermed_attr_code=oil.attribute_code \n" +
					"AND oil.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option oiu ON pr.intermed_attr_code=oiu.attribute_code \n" +
					"AND oiu.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"JOIN attribute_value vi ON (oil.option_code=vi.attribute_code) \n" +
				"JOIN attribute ai ON pr.intermed_attr_code=ai.attribute_code and ai.data_type in ('C', 'E') \n" +
				"WHERE vi.number_value in (:curCardIds) \n" +
				"UNION \n" +
				// атрибут связи - беклинк, атрибут промежуточной связи - кардлинк
				"SELECT oll.option_value, COALESCE(olu.option_value, ''), pr.intermed_attr_code, null, pr.rule_type, " + LINK_BACK_FWD + ", vi.card_id \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_option oll ON pr.link_attr_code=oll.attribute_code \n" +
					"AND oll.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option olu ON pr.link_attr_code=olu.attribute_code \n" +
					"AND olu.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"JOIN attribute_value vi ON pr.intermed_attr_code=vi.attribute_code \n" +
				"WHERE vi.card_id in (:curCardIds) \n" +
				"UNION \n" +
				// атрибут связи и атрибут промежуточной связи - беклинки
				"SELECT oll.option_value, COALESCE(olu.option_value, ''), oil.option_value, COALESCE(oiu.option_value, ''), pr.rule_type, " + LINK_BACK_BACK + ", vi.number_value \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_option oll ON pr.link_attr_code=oll.attribute_code \n" +
					"AND oll.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option olu ON pr.link_attr_code=olu.attribute_code \n" +
					"AND olu.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"JOIN attribute_option oil ON pr.intermed_attr_code=oil.attribute_code \n" +
					"AND oil.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option oiu ON pr.intermed_attr_code=oiu.attribute_code \n" +
					"AND oiu.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"JOIN attribute_value vi ON (oil.option_value=vi.attribute_code) \n" +
				"WHERE vi.number_value in (:curCardIds) \n"/*+
				// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
				"UNION \n" +
				// АС-КЛ, АПС-ПЛ
				"SELECT pr.link_attr_code, null, pr.intermed_attr_code, null, pr.rule_type, " + LINK_FWD_USR + " \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute al ON pr.link_attr_code=al.attribute_code and al.data_type in ('C', 'E') \n" +
				"JOIN attribute ai ON pr.intermed_attr_code=ai.attribute_code and ai.data_type = 'U' \n" +
				"JOIN attribute_value vi ON pr.intermed_attr_code=vi.attribute_code \n" +
				"WHERE vi.card_id=? \n" +
				"UNION \n" +
				// АС-БЛ, АПС-КЛ
				"SELECT oll.option_value, COALESCE(olu.option_value, ''), pr.intermed_attr_code, null, pr.rule_type, " + LINK_BACK_USR + " \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_option oll ON pr.link_attr_code=oll.attribute_code \n" +
					"AND oll.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option olu ON pr.link_attr_code=olu.attribute_code \n" +
					"AND olu.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"JOIN attribute_value vi ON pr.intermed_attr_code=vi.attribute_code \n" +
				"JOIN attribute ai ON pr.intermed_attr_code=ai.attribute_code and ai.data_type = 'U' \n" +
				"WHERE vi.card_id=?"*/,
				args,
				new LinkBuilder(CARD_INTERMEDIATE));

		// Finding complex links in which the card is ending (поиск комплексных ссылок, в которых текущая карточка является последней)
		namedParameterJdbcTemplate.query(
				// атрибут связи и атрибут промежуточной связи - кардлинки
				"SELECT pr.link_attr_code, null, pr.intermed_attr_code, null, pr.rule_type, " + LINK_FWD_FWD + ", v.number_value \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_value v ON (pr.intermed_attr_code=v.attribute_code) \n" +
				"JOIN attribute al ON pr.link_attr_code=al.attribute_code and al.data_type in ('C', 'E') \n" +
				"JOIN attribute ai ON pr.intermed_attr_code=ai.attribute_code and ai.data_type in ('C', 'E') \n" +
				"WHERE v.number_value in (:curCardIds) \n" +
				"UNION \n" +
				// атрибут связи - кардлинк, а атрибут промежуточной связи - беклинк
				"SELECT pr.link_attr_code, null, oil.option_value, COALESCE(oiu.option_value, ''), pr.rule_type, " + LINK_FWD_BACK + ", vi.card_id \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute al ON pr.link_attr_code=al.attribute_code and al.data_type in ('C', 'E') \n" +
				"JOIN attribute_option oil ON pr.intermed_attr_code=oil.attribute_code \n" +
					"AND oil.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option oiu ON pr.intermed_attr_code=oiu.attribute_code \n" +
					"AND oiu.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"JOIN attribute_value vi ON (oil.option_value=vi.attribute_code) \n" +
				"WHERE vi.card_id in (:curCardIds) \n" +
				"UNION \n" +
				// атрибут связи - беклинк, атрибут промежуточной связи - кардлинк
				"SELECT oll.option_value, COALESCE(olu.option_value, ''), pr.intermed_attr_code, null, pr.rule_type, " + LINK_BACK_FWD + ", vl.number_value \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute ai ON pr.intermed_attr_code=ai.attribute_code and ai.data_type in ('C', 'E') \n" +
				"JOIN attribute_option oll ON pr.link_attr_code=oll.attribute_code \n" +
					"AND oll.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option olu ON pr.link_attr_code=olu.attribute_code \n" +
					"AND olu.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"JOIN attribute_value vl ON (oll.option_value=vl.attribute_code OR olu.option_value=vl.attribute_code) \n" +
				"WHERE vl.number_value in (:curCardIds) \n" +
				"UNION \n" +
				// атрибут связи и атрибут промежуточной связи - беклинки
				"SELECT oll.option_value, COALESCE(olu.option_value, ''), oil.option_value, COALESCE(oiu.option_value, ''), pr.rule_type, " + LINK_BACK_BACK + ", vl.number_value \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_option oll ON pr.link_attr_code=oll.attribute_code \n" +
					"AND oll.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option olu ON pr.link_attr_code=olu.attribute_code \n" +
					"AND olu.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"JOIN attribute_option oil ON pr.intermed_attr_code=oil.attribute_code \n" +
					"AND oil.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option oiu ON pr.intermed_attr_code=oiu.attribute_code \n" +
					"AND oiu.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"JOIN attribute_value vl ON (oll.option_value=vl.attribute_code OR olu.option_value=vl.attribute_code) \n" +
				"WHERE vl.number_value in (:curCardIds) \n"/* +
				// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
				"UNION \n" +
				// АС-ПЛ, АПС-КЛ
				"SELECT pr.link_attr_code, null, pr.intermed_attr_code, null, pr.rule_type, " + LINK_USR_FWD + " \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute_value v ON (pr.intermed_attr_code=v.attribute_code) \n" +
				"JOIN attribute al ON pr.link_attr_code=al.attribute_code and al.data_type = 'U' \n" +
				"JOIN attribute ai ON pr.intermed_attr_code=ai.attribute_code and ai.data_type in ('C', 'E') \n" +
				"WHERE v.number_value=? \n" +
				"UNION \n" +
				// АС-ПЛ, АПС-БЛ
				"SELECT pr.link_attr_code, null, oil.option_value, COALESCE(oiu.option_value, ''), pr.rule_type, " + LINK_USR_BACK + " \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute al ON pr.link_attr_code=al.attribute_code and al.data_type = 'U' \n" +
				"JOIN attribute_option oil ON pr.intermed_attr_code=oil.attribute_code \n" +
					"AND oil.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option oiu ON pr.intermed_attr_code=oiu.attribute_code \n" +
					"AND oiu.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"JOIN attribute_value vi ON (oil.option_value=vi.attribute_code) \n" +
				"WHERE vi.card_id=? \n" +
				"UNION \n" +
				// АС-БЛ, АПС-ПЛ
				"SELECT oll.option_value, COALESCE(olu.option_value, ''), pr.intermed_attr_code, null, pr.rule_type, " + LINK_BACK_USR + " \n" +
				"FROM " + LINK_RULE_TABLE + " pr \n" +
				"JOIN attribute ai ON pr.intermed_attr_code=ai.attribute_code and ai.data_type = 'U' \n" +
				"JOIN attribute_option oll ON pr.link_attr_code=oll.attribute_code \n" +
					"AND oll.option_code='" + AttributeOptions.LINK + "' \n" +
				"LEFT JOIN attribute_option olu ON pr.link_attr_code=olu.attribute_code \n" +
					"AND olu.option_code='" + AttributeOptions.UPLINK + "' \n" +
				"JOIN attribute_value vl ON (oll.option_value=vl.attribute_code OR olu.option_value=vl.attribute_code) \n" +
				"WHERE vl.number_value=?"*/,
				args,
				new LinkBuilder(CARD_ENDING));
		
		// Resolving rules with indirect links
		for (Iterator itr = mapTypes.keySet().iterator(); itr.hasNext(); ) {
			ComplexLink link = (ComplexLink) itr.next();
			HashSet types = mapTypes.get(link);
			
			HashSet links = new HashSet();
			if (types.contains(CARD_STARTING) || types.contains(CARD_INTERMEDIATE))
				links = collectLinksDown(link.getCardObjectId(), link, types.contains(CARD_STARTING), types);	// сбор связанных карточек по атрибутам связи и атрибутам промежуточной связи
			if (types.contains(CARD_ENDING))
				links.add(link.getCardId());		// либо же текущая карточка и является связанной, т.к. она в конце 
			if (links.size() == 0)
				continue;
			
			HashSet sources = new HashSet();
			if (types.contains(CARD_ENDING))
				sources = collectLinksUp(link.getCardObjectId(), link, types);
			
			sources.removeAll(activeCardIds);
			sources.removeAll(otherCards);
			
			if (types.contains(CARD_STARTING))
				sources.add(link.getCardId());
			
			if (sources.size() == 0)
				continue;
			
			// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
			if ((types.contains(LINK_FWD_FWD)/*||types.contains(LINK_FWD_USR)||types.contains(LINK_USR_FWD)||types.contains(LINK_USR_USR)*/) && types.contains(RULE_PERSON)) {
				ArrayList params = new ArrayList(links.size() + sources.size() + 2);
				if (affiliated)
					params.addAll(sources);
				else
					params.add(link.getCardId());
				params.addAll(links);
				params.add(link.linkAttrId);
				params.add(link.intermedAttrId);
				rows += jdbc.update(
						"INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) \n" +
						// Person access rules with forward links
						"SELECT c.card_id, r.rule_id, v.number_value, v.attr_value_id \n" +
						"FROM person_access_rule pr \n" +
						"LEFT JOIN person_role sr ON pr.role_code=sr.role_code \n" +
						"JOIN access_rule r ON pr.rule_id=r.rule_id \n" +
						"JOIN card c \n" +
						//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*						"	ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
						"	AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
						"	ON (c.template_id=r.template_id) \n" +
						"	AND (c.status_id=r.status_id) \n" +
						"JOIN attribute_value v ON pr.person_attr_code=v.attribute_code \n" +
						"JOIN card cl ON v.card_id=cl.card_id \n" +
							"AND (pr.linked_status_id IS NULL OR pr.linked_status_id=cl.status_id) \n" +
						"WHERE (pr.role_code IS NULL OR v.number_value=sr.person_id) \n" +
							"AND c.card_id" + (affiliated ? " IN (" + populateParameters(sources.size()) + ") \n" : "=? \n") +
							"AND v.card_id IN (" + populateParameters(links.size()) + ") \n" +
							"AND pr.link_attr_code=? \n" +
							"AND pr.intermed_attr_code=?\n" + 
						"FOR UPDATE OF v",
						params.toArray());
			}
			// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
			if ((types.contains(LINK_FWD_BACK)/*||types.contains(LINK_USR_BACK)*/) && types.contains(RULE_PERSON)) {
				ArrayList params = new ArrayList(links.size() + sources.size() + 3);
				if (affiliated)
					params.addAll(sources);
				else
					params.add(link.getCardId());
				params.addAll(links);
				params.add(link.linkAttrId);
				params.add(link.intermedAttrId);
				params.add(link.intermedAttrIdU);
				rows += jdbc.update(
						"INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) \n" +
						// Person access rules with forward links
						"SELECT c.card_id, r.rule_id, v.number_value, v.attr_value_id \n" +
						"FROM person_access_rule pr \n" +
						"LEFT JOIN person_role sr ON pr.role_code=sr.role_code \n" +
						"JOIN access_rule r ON pr.rule_id=r.rule_id \n" +
						"JOIN card c \n" +
						//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*						"	ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
						"	AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
						"	ON (c.template_id=r.template_id) \n" +
						"	AND (c.status_id=r.status_id) \n" +
						"JOIN attribute_value v ON pr.person_attr_code=v.attribute_code \n" +
						"JOIN card cl ON v.card_id=cl.card_id \n" +
							"AND (pr.linked_status_id IS NULL OR pr.linked_status_id=cl.status_id) \n" +
						"JOIN attribute_option ol ON pr.intermed_attr_code=ol.attribute_code \n" +
							"AND ol.option_code='" + AttributeOptions.LINK + "' \n" +
						"LEFT JOIN attribute_option ou ON pr.intermed_attr_code=ou.attribute_code \n" +
							"AND ou.option_code='" + AttributeOptions.UPLINK + "' \n" +
						"WHERE (pr.role_code IS NULL OR v.number_value=sr.person_id) \n" +
							"AND c.card_id" + (affiliated ? " IN (" + populateParameters(sources.size()) + ") \n" : "=? \n") +
							"AND v.card_id IN (" + populateParameters(links.size()) + ") \n" +
							"AND pr.link_attr_code=? \n" +
							"AND ol.option_value=? \n" +
							"AND coalesce(ou.option_value, '')=coalesce(?, '')\n" +
						"FOR UPDATE OF v",
						params.toArray());
			}
			// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
			if ((types.contains(LINK_BACK_FWD)/*||types.contains(LINK_BACK_USR)*/) && types.contains(RULE_PERSON)) {
				ArrayList params = new ArrayList(links.size() + sources.size() + 3);
				params.addAll(sources);
				if (affiliated)
					params.addAll(links);
				else
					params.add(link.getCardId());
				params.add(link.linkAttrId);
				params.add(link.linkAttrIdU);
				params.add(link.intermedAttrId);
				rows += jdbc.update(
						"INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) \n" +
						// Person access rules with backward links
						"SELECT c.card_id, r.rule_id, v.number_value, v.attr_value_id \n" +
						"FROM person_access_rule pr \n" +
						"LEFT JOIN person_role sr ON pr.role_code=sr.role_code \n" +
						"JOIN access_rule r ON pr.rule_id=r.rule_id \n" +
						"JOIN card c \n" +
						//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
						/*"	ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
						"	AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
						"	ON (c.template_id=r.template_id) \n" +
						"	AND (c.status_id=r.status_id) \n" +
						"JOIN attribute_option ol ON pr.link_attr_code=ol.attribute_code \n" +
							"AND ol.option_code='" + AttributeOptions.LINK + "' \n" +
						"LEFT JOIN attribute_option ou ON pr.link_attr_code=ou.attribute_code \n" +
							"AND ou.option_code='" + AttributeOptions.UPLINK + "' \n" +
						"JOIN attribute_value v ON pr.person_attr_code=v.attribute_code \n" +
						"JOIN card cl ON v.card_id=cl.card_id \n" +
							"AND (pr.linked_status_id IS NULL OR pr.linked_status_id=cl.status_id) \n" +
						"WHERE (pr.role_code IS NULL OR v.number_value=sr.person_id) \n" +
							"AND v.card_id IN (" + populateParameters(sources.size()) + ") \n" +
							"AND c.card_id" + (affiliated ? " IN (" + populateParameters(links.size()) + ") \n" : "=? \n") +
							"AND ol.option_value=? \n" +
							"AND coalesce(ou.option_value, '')=coalesce(?, '') \n" +
							"AND pr.intermed_attr_code=? \n" +
						"FOR UPDATE OF v",
						params.toArray());
			}
			if (types.contains(LINK_BACK_BACK) && types.contains(RULE_PERSON)) {
				ArrayList params = new ArrayList(links.size() + sources.size() + 4);
				params.addAll(sources);
				if (affiliated)
					params.addAll(links);
				else
					params.add(link.getCardId());
				params.add(link.linkAttrId);
				params.add(link.linkAttrIdU);
				params.add(link.intermedAttrId);
				params.add(link.intermedAttrIdU);
				rows += jdbc.update(
						"INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) \n" +
						// Person access rules with backward links
						"SELECT c.card_id, r.rule_id, v.number_value, v.attr_value_id \n" +
						"FROM person_access_rule pr \n" +
						"LEFT JOIN person_role sr ON pr.role_code=sr.role_code \n" +
						"JOIN access_rule r ON pr.rule_id=r.rule_id \n" +
						"JOIN card c \n" +
						//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*						"	ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
						"	AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
						"	ON (c.template_id=r.template_id) \n" +
						"	AND (c.status_id=r.status_id) \n" +
						"JOIN attribute_option ol ON pr.link_attr_code=ol.attribute_code \n" +
							"AND ol.option_code='" + AttributeOptions.LINK + "' \n" +
						"LEFT JOIN attribute_option ou ON pr.link_attr_code=ou.attribute_code \n" +
							"AND ou.option_code='" + AttributeOptions.UPLINK + "' \n" +
						"JOIN attribute_option oil ON pr.intermed_attr_code=oil.attribute_code \n" +
							"AND oil.option_code='" + AttributeOptions.LINK + "' \n" +
						"LEFT JOIN attribute_option oiu ON pr.intermed_attr_code=oiu.attribute_code \n" +
							"AND oiu.option_code='" + AttributeOptions.UPLINK + "' \n" +
						"JOIN attribute_value v ON pr.person_attr_code=v.attribute_code \n" +
						"JOIN card cl ON v.card_id=cl.card_id \n" +
							"AND (pr.linked_status_id IS NULL OR pr.linked_status_id=cl.status_id) \n" +
						"WHERE (pr.role_code IS NULL OR v.number_value=sr.person_id) \n" +
							"AND v.card_id IN (" + populateParameters(sources.size()) + ") \n" +
							"AND c.card_id" + (affiliated ? " IN (" + populateParameters(links.size()) + ") \n" : "=? \n") +
							"AND ol.option_value=? \n" +
							"AND coalesce(ou.option_value, '')=coalesce(?, '') \n" +
							"AND oil.option_value=? \n" +
							"AND coalesce(oiu.option_value, '')=coalesce(?, '')\n" +
						"FOR UPDATE OF v",
						params.toArray());
			}

			// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
			if ((types.contains(LINK_FWD_FWD)/*||types.contains(LINK_FWD_USR)||types.contains(LINK_USR_FWD)*/) && types.contains(RULE_PROFILE)) {
				ArrayList params = new ArrayList(links.size() + sources.size() + 2);
				if (affiliated)
					params.addAll(sources);
				else
					params.add(link.getCardId());
				params.addAll(links);
				params.add(link.linkAttrId);
				params.add(link.intermedAttrId);
				rows += jdbc.update(
						"INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) \n" +
						// Person access rules with forward links
						"SELECT c.card_id, r.rule_id, p.person_id, v.attr_value_id \n" +
						"FROM profile_access_rule pr \n" +
						"LEFT JOIN person_role sr ON pr.role_code=sr.role_code \n" +
						"JOIN access_rule r ON pr.rule_id=r.rule_id \n" +
						"JOIN card c \n" +
						//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
						/*"	ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
						"	AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
						"	ON (c.template_id=r.template_id) \n" +
						"	AND (c.status_id=r.status_id) \n" +
						"JOIN attribute_value v ON pr.target_attr_code=v.attribute_code \n" +
						"JOIN attribute_value pv ON v.number_value=pv.number_value \n" +
							"AND pv.attribute_code=pr.profile_attr_code \n" +
						"JOIN person p ON pv.card_id=p.card_id \n" +
						"JOIN card cl ON v.card_id=cl.card_id \n" +
							"AND (pr.linked_status_id IS NULL OR pr.linked_status_id=cl.status_id) \n" +
						"WHERE (pr.role_code IS NULL OR p.person_id=sr.person_id) \n" +
							"AND c.card_id" + (affiliated ? " IN (" + populateParameters(sources.size()) + ") \n" : "=? \n") +
							"AND v.card_id IN (" + populateParameters(links.size()) + ") \n" +
							"AND pr.link_attr_code=? \n" +
							"AND pr.intermed_attr_code=?\n" +
						"FOR UPDATE OF v",
						params.toArray());
			}
			// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
			if ((types.contains(LINK_FWD_BACK)/*||types.contains(LINK_USR_BACK)*/) && types.contains(RULE_PROFILE)) {
				ArrayList params = new ArrayList(links.size() + sources.size() + 3);
				if (affiliated)
					params.addAll(sources);
				else
					params.add(link.getCardId());
				params.addAll(links);
				params.add(link.linkAttrId);
				params.add(link.intermedAttrId);
				params.add(link.intermedAttrIdU);
				rows += jdbc.update(
						"INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) \n" +
						// Person access rules with forward links
						"SELECT c.card_id, r.rule_id, p.person_id, v.attr_value_id \n" +
						"FROM profile_access_rule pr \n" +
						"LEFT JOIN person_role sr ON pr.role_code=sr.role_code \n" +
						"JOIN access_rule r ON pr.rule_id=r.rule_id \n" +
						"JOIN card c \n" +
						//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
						/*"	ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
						"	AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
						"	ON (c.template_id=r.template_id) \n" +
						"	AND (c.status_id=r.status_id) \n" +
						"JOIN attribute_option ol ON pr.intermed_attr_code=ol.attribute_code \n" +
							"AND ol.option_code='" + AttributeOptions.LINK + "' \n" +
						"LEFT JOIN attribute_option ou ON pr.intermed_attr_code=ou.attribute_code \n" +
							"AND ou.option_code='" + AttributeOptions.UPLINK + "' \n" +
						"JOIN attribute_value v ON pr.target_attr_code=v.attribute_code \n" +
						"JOIN attribute_value pv ON v.number_value=pv.number_value \n" +
							"AND pv.attribute_code=pr.profile_attr_code \n" +
						"JOIN person p ON pv.card_id=p.card_id \n" +
						"JOIN card cl ON v.card_id=cl.card_id \n" +
							"AND (pr.linked_status_id IS NULL OR pr.linked_status_id=cl.status_id) \n" +
						"WHERE (pr.role_code IS NULL OR p.person_id=sr.person_id) \n" +
							"AND c.card_id" + (affiliated ? " IN (" + populateParameters(sources.size()) + ") \n" : "=? \n") +
							"AND v.card_id IN (" + populateParameters(links.size()) + ") \n" +
							"AND pr.link_attr_code=? \n" +
							"AND ol.option_value=? \n" +
							"AND coalesce(ou.option_value, '')=coalesce(?, '')\n" +
						"FOR UPDATE OF v",
						params.toArray());
			}
			// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
			if ((types.contains(LINK_BACK_FWD)/*||types.contains(LINK_BACK_USR)*/) && types.contains(RULE_PROFILE)) {
				ArrayList params = new ArrayList(links.size() + sources.size() + 3);
				params.addAll(sources);
				if (affiliated)
					params.addAll(links);
				else
					params.add(link.getCardId());
				params.add(link.linkAttrId);
				params.add(link.linkAttrIdU);
				params.add(link.intermedAttrId);
				rows += jdbc.update(
						"INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) \n" +
						// Person access rules with backward links
						"SELECT c.card_id, r.rule_id, p.person_id, v.attr_value_id \n" +
						"FROM profile_access_rule pr \n" +
						"LEFT JOIN person_role sr ON pr.role_code=sr.role_code \n" +
						"JOIN access_rule r ON pr.rule_id=r.rule_id \n" +
						"JOIN card c \n" +
						//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*						"	ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
						"	AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
						"	ON (c.template_id=r.template_id) \n" +
						"	AND (c.status_id=r.status_id) \n" +
						"JOIN attribute_option ol ON pr.link_attr_code=ol.attribute_code \n" +
							"AND ol.option_code='" + AttributeOptions.LINK + "' \n" +
						"LEFT JOIN attribute_option ou ON pr.link_attr_code=ou.attribute_code \n" +
							"AND ou.option_code='" + AttributeOptions.UPLINK + "' \n" +
						"JOIN attribute_value v ON pr.target_attr_code=v.attribute_code \n" +
						"JOIN attribute_value pv ON v.number_value=pv.number_value \n" +
							"AND pv.attribute_code=pr.profile_attr_code \n" +
						"JOIN person p ON pv.card_id=p.card_id \n" +
						"JOIN card cl ON v.card_id=cl.card_id \n" +
							"AND (pr.linked_status_id IS NULL OR pr.linked_status_id=cl.status_id) \n" +
						"WHERE (pr.role_code IS NULL OR p.person_id=sr.person_id) \n" +
							"AND v.card_id IN (" + populateParameters(sources.size()) + ") \n" +
							"AND c.card_id" + (affiliated ? " IN (" + populateParameters(links.size()) + ") \n" : "=? \n") +
							"AND ol.option_value=? \n" +
							"AND coalesce(ou.option_value, '')=coalesce(?, '') \n" +
							"AND pr.intermed_attr_code=? \n" +
						"FOR UPDATE OF v",
						params.toArray());
			}
			// Здесь надо править
			if (types.contains(LINK_BACK_BACK) && types.contains(RULE_PROFILE)) {
				ArrayList params = new ArrayList(links.size() + sources.size() + 4);
				params.addAll(sources);
				if (affiliated)
					params.addAll(links);
				else
					params.add(link.getCardId());
				params.add(link.linkAttrId);
				params.add(link.linkAttrIdU);
				params.add(link.intermedAttrId);
				params.add(link.intermedAttrIdU);
				rows += jdbc.update(
						"INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) \n" +
						// Person access rules with backward links
						"SELECT c.card_id, r.rule_id, p.person_id, v.attr_value_id \n" +
						"FROM profile_access_rule pr \n" +
						"LEFT JOIN person_role sr ON pr.role_code=sr.role_code \n" +
						"JOIN access_rule r ON pr.rule_id=r.rule_id \n" +
						"JOIN card c \n" +
						//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*						"	ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
						"	AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
						"	ON (c.template_id=r.template_id) \n" +
						"	AND (c.status_id=r.status_id) \n" +
						"JOIN attribute_option ol ON pr.link_attr_code=ol.attribute_code \n" +
							"AND ol.option_code='" + AttributeOptions.LINK + "' \n" +
						"LEFT JOIN attribute_option ou ON pr.link_attr_code=ou.attribute_code \n" +
							"AND ou.option_code='" + AttributeOptions.UPLINK + "' \n" +
						"JOIN attribute_option oil ON pr.intermed_attr_code=oil.attribute_code \n" +
							"AND oil.option_code='" + AttributeOptions.LINK + "' \n" +
						"LEFT JOIN attribute_option oiu ON pr.intermed_attr_code=oiu.attribute_code \n" +
							"AND oiu.option_code='" + AttributeOptions.UPLINK + "' \n" +
						"JOIN attribute_value v ON pr.target_attr_code=v.attribute_code \n" +
						"JOIN attribute_value pv ON v.number_value=pv.number_value \n" +
							"AND pv.attribute_code=pr.profile_attr_code \n" +
						"JOIN person p ON pv.card_id=p.card_id \n" +
						"JOIN card cl ON v.card_id=cl.card_id \n" +
							"AND (pr.linked_status_id IS NULL OR pr.linked_status_id=cl.status_id) \n" +
						"WHERE (pr.role_code IS NULL OR p.person_id=sr.person_id) \n" +
							"AND v.card_id IN (" + populateParameters(sources.size()) + ") \n" +
							"AND c.card_id" + (affiliated ? " IN (" + populateParameters(links.size()) + ") \n" : "=? \n") +
							"AND ol.option_value=? \n" +
							"AND coalesce(ou.option_value, '')=coalesce(?, '') \n" +
							"AND oil.option_value=? \n" +
							"AND coalesce(oiu.option_value, '')=coalesce(?, '') \n" +
						"FOR UPDATE OF v",
						params.toArray());
			}			
		}
		
		return rows;
	}	
	/**
	 * Recalculates all permissions granted to a user by person & profile access rules,
	 * specifying a role.
	 * Should be called after granting of new role to user or modifying one.
	 * 
	 * @param roleId ID of the role
	 */
	/* ToDo: сделать пересчёт с учётом того, что АПС тоже может быть заполнен, а для АС-БЛ анализируются не только LINK-свойство, но и UPLINK)
	Наше счастье, что сейчас все правила, где указаны статические роли не используют АС и АПС*/
	public Long updateAccessByRole(ObjectId roleId) {
		if (roleId == null || !Role.class.equals(roleId.getType()))
			throw new IllegalArgumentException("Not a role ID");
		long start = System.currentTimeMillis();
		
		long rows = jdbc.update(
			"INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) " +
				// Person access rules without links
				"SELECT c.card_id, r.rule_id, pv.number_value, pv.attr_value_id " +
				"FROM card c " +
				"JOIN access_rule r \n" +
				//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*				"	ON (c.template_id=r.template_id OR r.template_id IS NULL) " +
				"	AND (c.status_id=r.status_id OR r.status_id IS NULL) " +*/
				"	ON (c.template_id=r.template_id) " +
				"	AND (c.status_id=r.status_id) " +
				"	AND (c.is_active=1) " +
				"JOIN person_access_rule pr ON r.rule_id=pr.rule_id " +
				"JOIN attribute_value pv ON c.card_id=pv.card_id AND pr.person_attr_code=pv.attribute_code " +
				"JOIN person_role sr ON pr.role_code=sr.role_code AND pv.number_value=sr.person_id " +
				"WHERE pr.link_attr_code IS NULL AND sr.prole_id=? " +

			"UNION ALL " +
				// Person access rules with forward links
				"SELECT c.card_id, r.rule_id, pv.number_value, pv.attr_value_id " +
				"FROM card c " +
				"JOIN access_rule r \n" +
				//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*				"	ON (c.template_id=r.template_id OR r.template_id IS NULL) " +
				"	AND (c.status_id=r.status_id OR r.status_id IS NULL) " +*/
				"	ON (c.template_id=r.template_id) " +
				"	AND (c.status_id=r.status_id) " +
				"	AND (c.is_active=1) " +
				"JOIN person_access_rule pr ON r.rule_id=pr.rule_id " +
				"JOIN attribute_value lv ON c.card_id=lv.card_id AND pr.link_attr_code=lv.attribute_code " +
				"JOIN attribute a ON pr.link_attr_code = a.attribute_code AND a.data_type in ('C', 'E') " +
				"JOIN card lc ON lv.number_value=lc.card_id " +
					"AND (pr.linked_status_id=lc.status_id OR pr.linked_status_id IS NULL) " +
				"JOIN attribute_value pv ON lv.number_value=pv.card_id AND pr.person_attr_code=pv.attribute_code " +
				"JOIN person_role sr ON pr.role_code=sr.role_code AND pv.number_value=sr.person_id " +
				"WHERE pr.intermed_attr_code is NULL and sr.prole_id=? " +
				
				// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
			/*"UNION ALL " +
				// Person access rules with forward person-links
				"SELECT c.card_id, r.rule_id, pv.number_value, pv.attr_value_id " +
				"FROM card c " +
				"JOIN access_rule r ON (c.template_id=r.template_id OR r.template_id IS NULL) " +
					"AND (c.status_id=r.status_id OR r.status_id IS NULL) " +
				"JOIN person_access_rule pr ON r.rule_id=pr.rule_id " +
				"JOIN attribute_value lv ON c.card_id=lv.card_id AND pr.link_attr_code=lv.attribute_code " +
				"JOIN attribute a ON pr.link_attr_code = a.attribute_code AND a.data_type = 'U' " +
				"JOIN card lc ON lc.card_id = (select p.card_id from person p where p.person_id = lv.number_value) " +
					"AND (pr.linked_status_id=lc.status_id OR pr.linked_status_id IS NULL) " +
				"JOIN attribute_value pv ON lv.number_value=pv.card_id AND pr.person_attr_code=pv.attribute_code " +
				"JOIN person_role sr ON pr.role_code=sr.role_code AND pv.number_value=sr.person_id " +
				"WHERE pr.intermed_attr_code is NULL and sr.prole_id=? " +
*/
			"UNION ALL " +
				// Person access rules with backward links
				"SELECT c.card_id, r.rule_id, pv.number_value, pv.attr_value_id " +
				"FROM card c " +
				"JOIN access_rule r \n" +
				//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*				"	ON (c.template_id=r.template_id OR r.template_id IS NULL) " +
				"	AND (c.status_id=r.status_id OR r.status_id IS NULL) " +*/
				"	ON (c.template_id=r.template_id) " +
				"	AND (c.status_id=r.status_id) " +
				"	AND (c.is_active=1) " +
				"JOIN person_access_rule pr ON r.rule_id=pr.rule_id " +
				"JOIN attribute_option o ON pr.link_attr_code=o.attribute_code " +
					"AND o.option_code='" + AttributeOptions.LINK + "' " +
				"JOIN attribute_value lv ON c.card_id=lv.number_value AND o.option_value=lv.attribute_code " +
				"JOIN card lc ON lv.card_id=lc.card_id " +
					"AND (pr.linked_status_id=lc.status_id OR pr.linked_status_id IS NULL) " +
				"JOIN attribute_value pv ON lv.card_id=pv.card_id AND pr.person_attr_code=pv.attribute_code " +
				"JOIN person_role sr ON pr.role_code=sr.role_code AND pv.number_value=sr.person_id " +
				"WHERE pr.intermed_attr_code is NULL and sr.prole_id=? " +

			"UNION ALL " +
				// Profile access rules without links
				"SELECT c.card_id, r.rule_id, p.person_id, tv.attr_value_id " +
				"FROM card c " +
				"JOIN access_rule r \n" +
				//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*				"	ON (c.template_id=r.template_id OR r.template_id IS NULL) " +
				"	AND (c.status_id=r.status_id OR r.status_id IS NULL) " +*/
				"	ON (c.template_id=r.template_id) " +
				"	AND (c.status_id=r.status_id) " +
				"	AND (c.is_active=1) " +
				"JOIN profile_access_rule pr ON r.rule_id=pr.rule_id " +
				"JOIN attribute_value tv ON c.card_id=tv.card_id AND pr.target_attr_code=tv.attribute_code " +
				"JOIN attribute_value pv ON tv.number_value=pv.number_value AND pr.profile_attr_code=pv.attribute_code " +
				"JOIN person p ON p.card_id=pv.card_id " +
				"JOIN person_role sr ON pr.role_code=sr.role_code AND p.person_id=sr.person_id " +
				"WHERE pr.link_attr_code IS NULL AND sr.prole_id=? " +
			
			"UNION ALL " +
				// Profile access rules with forward links
				"SELECT c.card_id, r.rule_id, p.person_id, tv.attr_value_id " +
				"FROM person_role sr " +
				"JOIN person p ON sr.person_id=p.person_id " +
				"JOIN profile_access_rule pr ON sr.role_code=pr.role_code " +
				"JOIN attribute_value pv ON p.card_id=pv.card_id AND pr.profile_attr_code=pv.attribute_code " +
				"JOIN attribute a ON pr.link_attr_code = a.attribute_code AND a.data_type in ('C', 'E') " +
				"JOIN access_rule r ON pr.rule_id=r.rule_id " +
				"JOIN card c \n" +
				//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
				/*"	ON (r.template_id=c.template_id OR r.template_id IS NULL) " +
				"	AND (r.status_id=c.status_id OR r.status_id IS NULL) " +*/
				"	ON (r.template_id=c.template_id) " +
				"	AND (r.status_id=c.status_id) " +
				"	AND (c.is_active=1) " +
				"JOIN attribute_value lv ON c.card_id=lv.card_id AND pr.link_attr_code=lv.attribute_code " +
				"JOIN attribute_value tv ON lv.number_value=tv.card_id AND pr.target_attr_code=tv.attribute_code " +
					"AND pv.number_value=tv.number_value " +
				"JOIN card lc ON tv.card_id=lc.card_id " +
					"AND (pr.linked_status_id=lc.status_id OR pr.linked_status_id IS NULL) " +
				"WHERE pr.intermed_attr_code is NULL and sr.prole_id=? " +
			
				// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
			/*"UNION ALL " +
				// Profile access rules with forward person-links
				"SELECT c.card_id, r.rule_id, p.person_id, tv.attr_value_id " +
				"FROM person_role sr " +
				"JOIN person p ON sr.person_id=p.person_id " +
				"JOIN profile_access_rule pr ON sr.role_code=pr.role_code " +
				"JOIN attribute_value pv ON p.card_id=pv.card_id AND pr.profile_attr_code=pv.attribute_code " +
				"JOIN attribute a ON pr.link_attr_code = a.attribute_code AND a.data_type = 'U' " +
				"JOIN access_rule r ON pr.rule_id=r.rule_id " +
				"JOIN card c ON (r.template_id=c.template_id OR r.template_id IS NULL) " +
					"AND (r.status_id=c.status_id OR r.status_id IS NULL) " +
				"JOIN attribute_value lv ON c.card_id=lv.card_id AND pr.link_attr_code=lv.attribute_code " +
				"JOIN attribute_value tv ON tv.card_id = (select p.card_id from person p where p.person_id = lv.number_value) " +
					"AND pr.target_attr_code=tv.attribute_code " +
					"AND pv.number_value=tv.number_value " +
				"JOIN card lc ON tv.card_id=lc.card_id " +
					"AND (pr.linked_status_id=lc.status_id OR pr.linked_status_id IS NULL) " +
				"WHERE pr.intermed_attr_code is NULL and sr.prole_id=? " +
*/
			"UNION ALL " +
				// Profile access rules with backward links
				"SELECT c.card_id, r.rule_id, p.person_id, tv.attr_value_id " +
				"FROM person_role sr " +
				"JOIN person p ON sr.person_id=p.person_id " +
				"JOIN profile_access_rule pr ON sr.role_code=pr.role_code " +
				"JOIN attribute_value pv ON p.card_id=pv.card_id AND pr.profile_attr_code=pv.attribute_code " +
				"JOIN access_rule r ON pr.rule_id=r.rule_id " +
				"JOIN card c \n" +
				//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
				/*"	ON (r.template_id=c.template_id OR r.template_id IS NULL) " +
				"	AND (r.status_id=c.status_id OR r.status_id IS NULL) " +*/
				"	ON (r.template_id=c.template_id) " +
				"	AND (r.status_id=c.status_id) " +
				"	AND (c.is_active=1) " +
				"JOIN attribute_option o ON pr.link_attr_code=o.attribute_code " +
					"AND o.option_code='" + AttributeOptions.LINK + "' " +
				"JOIN attribute_value lv ON c.card_id=lv.number_value AND o.option_value=lv.attribute_code " +
				"JOIN attribute_value tv ON lv.card_id=tv.card_id AND pr.target_attr_code=tv.attribute_code " +
					"AND pv.number_value=tv.number_value " +
				"JOIN card lc ON tv.card_id=lc.card_id " +
					"AND (pr.linked_status_id=lc.status_id OR pr.linked_status_id IS NULL) " +
				"WHERE pr.intermed_attr_code is NULL and sr.prole_id=?",
				
				new Object[] {
					roleId.getId(), roleId.getId(), /*roleId.getId(), roleId.getId(),*/
					roleId.getId(), roleId.getId(), roleId.getId(), roleId.getId() });
		
		long duration = System.currentTimeMillis() - start;
		logger.info(rows + " permission(s) added (" + duration + "ms) for Role "+roleId.getId());
		return rows;
	}
	
	/**
	 * Recalculates all permissions granted by specific rule.
	 * Should be called after changing or creating of new rule.
	 * 
	 * @param rule A rule. Must have a correct ID
	 */
	public long applyNewRule(AccessRule rule) {
		if (rule.getId() == null)
			throw new IllegalArgumentException("Rule must be stored before applying");
		ObjectId roleId = null;
		
		if (rule instanceof ByCardAccessRule) {
			roleId = ((ByCardAccessRule)rule).getRoleId();
		}

		if (!checkPersonWithRoleExists(roleId)){
			logger.debug("Persons with role '" + roleId.getId().toString() + "' not founded in system, recalculating access list break.");
			return 0;
		}
		
		String query = getSqlQueryForAccessRules(rule);
		
		// Resolving rules...
		logger.debug("Applying access rule: " + rule.getId().getId() + ", sql: " + query);
		long rows = 0;

		//ByCardAccessRule is parent for PersoAccessRule and PersonProfileAccessRule classes only
		if ((rule instanceof ByCardAccessRule) && (((ByCardAccessRule) rule).getLinkAttribute() != null) && (Attribute.TYPE_BACK_LINK.equals(((ByCardAccessRule) rule).getLinkAttribute().getType()))){
			if (rule instanceof PersonProfileAccessRule){
				rows = jdbc.update(
						"INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) " + query,
						new Object[] { rule.getId().getId(), rule.getId().getId(), rule.getId().getId() });
			} else {
				rows = jdbc.update(
						"INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) " + query,
						new Object[] { rule.getId().getId(), rule.getId().getId() });
			}
		} else {
			if (rule instanceof PersonProfileAccessRule){
				rows = jdbc.update(
						"INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) " + query,
						new Object[] { rule.getId().getId(), rule.getId().getId() });
			} else {
				rows = jdbc.update(
						"INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) " + query,
						new Object[] { rule.getId().getId() });
			}
		}
		logger.info(rows + " permission(s) added for rule "+rule.getId());
		return rows;
	}
	
	/**
	 * Recalculates all permissions granted by specific rules.
	 * Should be called after changing or creating of new rule.
	 * 
	 * @param rules {@link List} of {@link AccessRule}.
	 */
	public void applyNewRules(List<AccessRule> rules) {
		for(AccessRule rule : rules) {
			applyNewRule(rule);
		}
	}
	
	// запрос на извлечение основных атрибутов для таблицы access_list из полученного дерева карточек
	private static String SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PERSON_RULES =
		"select distinct \n" +
		"	ct.card_id, \n" +
		"	ct.rule_id, \n" +
		"	pv.number_value as person_id, \n" + 
		"	pv.attr_value_id \n" +
		"from card_tree ct \n" +
		"{0}" + 
		"JOIN attribute_value pv ON ct.link_card_id=pv.card_id \n" + 
		"     AND ct.person_attr_code=pv.attribute_code \n" + 
		"{1} ;"; 
	
	private static String SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PROFILE_RULES =
			"select distinct \n" +
			"	ct.card_id, \n" +
			"	ct.rule_id, \n" +
			"	pv.person_id as person_id, \n" + 
			"	tv.attr_value_id \n" +
			"from card_tree ct \n" +
			"{0}" + 
			"JOIN attribute_value tv \n" +
			"	ON ct.link_card_id=tv.card_id \n" +
			"	AND ct.target_attr_code=tv.attribute_code \n" +
			"JOIN person_all_profiles pv \n" +
			"	ON tv.number_value=pv.number_value \n" +
			"	AND ct.profile_attr_code=pv.attribute_code \n";

	////////////////////////Персональные правила/////////////////////////////////////
	// Запрос для извлечения самых верхних родителей на основе backlink-атрибута link_attr_code во входном персональном правиле
	// для backlink-атрибута link_attr_code
	private static String SELECT_UPPER_PARENT_CARD_FOR_PERSON_RULE =
		"			( \n" +
		"				SELECT \n" + 
		"					c.card_id, \n" + 
		"					r.rule_id, \n" + 
		"					functionbacklink(c.card_id, aoU.option_value, aoL.option_value) as link_card_id, \n" +     
		"					pr.linked_status_id, \n" +
		"					pr.role_code, \n" +
		"					pr.intermed_attr_code, \n" +
		"					pr.person_attr_code \n" +
		"				FROM card c \n" +
		"					JOIN access_rule r \n" + 	
		//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*		"							ON 	(c.template_id=r.template_id OR r.template_id IS NULL) \n" + 
		"							AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
		"							ON 	(c.template_id=r.template_id) \n" + 
		"							AND (c.status_id=r.status_id) \n" +
		"					JOIN person_access_rule pr \n" +
		"							ON 	r.rule_id=pr.rule_id \n" + 
		"					JOIN attribute_option aoL \n" + 
		"							ON pr.link_attr_code=aoL.attribute_code \n" + 
		"							AND aoL.option_code='" +AttributeOptions.LINK+"' \n" +
		"					JOIN attribute_option aoU \n" + 
		"							ON pr.link_attr_code=aoU.attribute_code \n" + 
		"							AND aoU.option_code='" +AttributeOptions.UPLINK+"' \n" +
		"				WHERE r.rule_id = ? \n" +
		"				UNION ALL \n" +
		"				SELECT \n" + 
		"					c.card_id, \n" + 
		"					r.rule_id, \n" + 
		"					av.card_id as link_card_id, \n" +     
		"					pr.linked_status_id, \n" +
		"					pr.role_code, \n" +
		"					pr.intermed_attr_code, \n" +
		"					pr.person_attr_code \n" +
		"				FROM card c \n" +
		"					JOIN access_rule r \n" + 	
		//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*		"							ON 	(c.template_id=r.template_id OR r.template_id IS NULL) \n" + 
		"							AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
		"							ON 	(c.template_id=r.template_id) \n" + 
		"							AND (c.status_id=r.status_id) \n" +
		"					JOIN person_access_rule pr \n" +
		"							ON 	r.rule_id=pr.rule_id \n" + 
		"					JOIN attribute_option aoL \n" + 
		"							ON pr.link_attr_code=aoL.attribute_code \n" + 
		"							AND aoL.option_code='" +AttributeOptions.LINK+"' \n" +
		"					LEFT JOIN attribute_option aoU \n" + 
		"							ON pr.link_attr_code=aoU.attribute_code \n" + 
		"							AND aoU.option_code='" +AttributeOptions.UPLINK+"' \n" +
		"					JOIN attribute_value av \n" +
// необходимо ввести ограничение в утилите, чтобы нельзя было использовать беклинки такого типа
//		"							ON av.attribute_code in (select unnest(string_to_array(aoL.option_value, '';''))) \n" +
		"							ON av.attribute_code = aoL.option_value \n" +
		"							and av.number_value = c.card_id \n" +
		"				WHERE r.rule_id = ? \n" +
		"					and aoU.option_value is NULL \n"+
		"			) as c \n"; 
	
	// запрос на извлечение детей первого уровня на основе cardlink-атрибута link_attr_code во входном персональном правиле
	// для cardlink-атрибута link_attr_code
	private static String SELECT_FIRST_CHILD_CARD_FOR_PERSON_RULE =
		"			( \n" +
		"				SELECT \n" + 
		"					c.card_id, \n" + 
		"					r.rule_id, \n" + 
		"					lv.number_value as link_card_id, \n" +     
		"					pr.linked_status_id, \n" +
		"					pr.role_code, \n" +
		"					pr.intermed_attr_code, \n" +
		"					pr.person_attr_code \n" +
		"				FROM card c \n" +
		"					JOIN access_rule r 	\n" +
		//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*		"							ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
		"							AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
		"							ON (c.template_id=r.template_id) \n" +
		"							AND (c.status_id=r.status_id) \n" +
		"					JOIN person_access_rule pr \n" +
		"							ON r.rule_id=pr.rule_id \n" +
		"					JOIN attribute_value lv \n" +
		"							ON c.card_id=lv.card_id \n" +
		"							AND pr.link_attr_code=lv.attribute_code \n" +
		"				WHERE  \n" +
		"					r.rule_id=? \n"+
		"			) as c \n"; 
	
	// запрос на извлечение детей первого уровня на основе personlink-атрибута link_attr_code во входном персональном правиле
	// для personlink-атрибута link_attr_code
	// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
	/*private static String SELECT_FIRST_CHILD_USER_CARD_FOR_PERSON_RULE =
		"			( \n" +
		"				SELECT \n" + 
		"					c.card_id, \n" + 
		"					r.rule_id, \n" + 
		"					p1.card_id as link_card_id, \n" +     
		"					pr.linked_status_id, \n" +
		"					pr.role_code, \n" +
		"					sr.person_id, " +
		"					pr.intermed_attr_code, \n" +
		"					pr.person_attr_code \n" +
		"				FROM card c \n" +
		"					JOIN access_rule r 	\n" +
		"							ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
		"							AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +
		"					JOIN person_access_rule pr \n" +
		"							ON r.rule_id=pr.rule_id \n" +
		"					LEFT JOIN person_role sr \n" +
		"							ON pr.role_code=sr.role_code \n" + 
		"					JOIN attribute_value lv \n" +
		"							ON c.card_id=lv.card_id \n" +
		"							AND pr.link_attr_code=lv.attribute_code \n" +
		"					JOIN person p1 \n" +
		"							ON p1.person_id=lv.number_value \n" +
		"				WHERE  \n" +
		"					r.rule_id=? \n"+
		"			) as c \n"; 
	*/
	// список колонок, которые надо извлечь из предыдущих карточек, чтобы изспользовать полученный список в качестве первых родительских
	// для backlink-атрибута intermed_attr_code
	private static String COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE =
		"			c.card_id, \n" +
		"			c.rule_id, \n" +
		"			lv.card_id as link_card_id, \n" +
		"			c.linked_status_id, \n" +
		"			c.role_code, \n" +
		"			c.person_attr_code, \n" +
		"			o.option_value as link_attr_code \n";

	// список колонок, которые надо извлечь из предыдущих карточек, чтобы изспользовать полученный список в качестве первых дочерних
	// для cardlink-атрибута intermed_attr_code
	private static String COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE =
		"			c.card_id, \n" +
		"			c.rule_id, \n" +
		"			lv.number_value as link_card_id, \n" +
		"			c.linked_status_id, \n" +
		"			c.role_code, \n" +
		"			c.person_attr_code, \n" +
		"			c.intermed_attr_code as link_attr_code \n";

	// запрос на извлечение родителей первого уровня в персональных правилах для вычесленных ввиде дерева карточек  
	// для backlink-атрибута intermed_attr_code
	private static String SELECT_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE =
		"	select \n" +
		"		ct.card_id, \n" +
		"		ct.rule_id, \n" +
		"		lv.card_id as link_card_id, \n" +
		"		ct.linked_status_id, \n" +
		"		ct.role_code, \n" +
		"		ct.person_attr_code, \n" +
		"		ct.link_attr_code \n" +
		"	from \n" +
		"		card_tree ct \n" +
		"		JOIN attribute_value lv " +
		"				ON ct.link_card_id=lv.number_value \n" +
// необходимо ввести ограничение в утилите, чтобы нельзя было использовать беклинки такого типа
//		"				AND lv.attribute_code in (select unnest(string_to_array(ct.link_attr_code, '';''))) \n" +
		"				AND lv.attribute_code = ct.link_attr_code \n";

	// запрос на извлечение детей первого уровня в персональных правилах для вычисленных в виде дерева карточек  
	// для cardlink-атрибута intermed_attr_code
	private static String SELECT_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE =
		"	select \n" +
		"		ct.card_id, \n" +
		"		ct.rule_id, \n" +
		"		lv.number_value as link_card_id, \n" +
		"		ct.linked_status_id, \n" +
		"		ct.role_code, \n" +
		"		ct.person_attr_code, \n" +
		"		ct.link_attr_code \n" +
		"	from \n" +
		"		card_tree ct \n" +
		"		JOIN attribute_value lv " +
		"				ON ct.link_card_id=lv.card_id \n" +
		"				AND lv.attribute_code = ct.link_attr_code \n";

	// запрос на извлечение детей первого уровня в персональных правилах для вычесленных ввиде дерева карточек  
	// для personlink-атрибута intermed_attr_code
	// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
	/*private static String SELECT_FIRST_CHILD_USER_CARD_OF_PREV_CARD_FOR_PERSON_RULE =
		"	select \n" +
		"		ct.card_id, \n" +
		"		ct.rule_id, \n" +
		"		lc.card_id as link_card_id, \n" +
		"		pv.number_value as person_id, \n" + 
		"		pv.attr_value_id, \n" +
		"		ct.linked_status_id, \n" +
		"		ct.role_code, \n" +
		"		ct.person_attr_code, \n" +
		"		ct.link_attr_code \n" +
		"	from \n" +
		"		card_tree ct \n" +
		"		JOIN attribute_value lv " +
		"				ON ct.link_card_id=lv.card_id \n" +
		"				AND ct.link_attr_code=lv.attribute_code \n" +
		"		JOIN card lc 	" +
		"				ON lc.card_id = (select p.card_id from person p where p.person_id = lv.number_value)\n" +
		"				AND (ct.linked_status_id=lc.status_id OR ct.linked_status_id IS NULL) \n" +
		"		JOIN attribute_value pv " +
		"				ON lc.card_id=pv.card_id \n" +
		"				AND ct.person_attr_code=pv.attribute_code \n" +
		"	WHERE 	" +
		"		(ct.role_code IS NULL OR pv.number_value=ct.person_id) \n";
	*/
	// подзапросы для вычисления первых родительских карточек для найденных ранее карточек, используется совместно с COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE
	// для backlink-атрибута intermed_attr_code
	private static String JOIN_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE =
		"			JOIN attribute_option o \n" +
		"					ON c.intermed_attr_code=o.attribute_code \n" +
		"					AND o.option_code='" + AttributeOptions.LINK + "' \n" +
		"			JOIN attribute_value lv \n" +
		"					ON c.link_card_id=lv.number_value \n" +
		"					AND o.option_value=lv.attribute_code \n";
		
	// подзапросы для вычисления первых дочерних карточек для найденных ранее карточек, используется совместно с COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE
	private static String JOIN_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE =
		"			JOIN attribute_value lv \n" +
		"					ON c.link_card_id=lv.card_id \n" +
		"					AND c.intermed_attr_code=lv.attribute_code \n";
	
	// подзапросы для вычисления первых дочерних карточек для найденных ранее карточек, используется совместно с COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE
	// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
	/*private static String JOIN_FIRST_CHILD_USER_CARD_OF_PREV_CARD_FOR_PERSON_RULE =
		"			JOIN attribute_value lv \n" +
		"					ON c.link_card_id=lv.card_id \n" +
		"					AND c.intermed_attr_code=lv.attribute_code \n" +
		"			JOIN card lc \n" +
		"					ON lc.card_id = (select p.person_id from person p where p.person_id = lv.number_value)\n" +
		"					AND (c.linked_status_id=lc.status_id OR c.linked_status_id IS NULL) \n" +
		"			JOIN attribute_value pv \n" +
		"					ON lc.card_id=pv.card_id \n" +
		"					AND c.person_attr_code=pv.attribute_code \n";
	*/
	// where-часть всех запросов - проверка на совпадение пользователя, прописанного в целевой карточке, с пользователем, обладающим ролью, прописанной в правиле
	private static String WHERE_PERSON_IN_CARD_EQUALS_PERSON_WITH_ROLE_IN_PERSON_RULE = 
		"			{0} \n";
	////////////////////////Профильные правила/////////////////////////////////////
	// запрос для извлечения профильных атрибутов из профиля пользователей, удовлетворяющих входному правилу
	private static String PERSON_ALL_PROFILES = 
		"person_all_profiles as( \n" +
		"			select \n"+
		"				p.person_id, \n"+ 
		"				av.number_value, \n"+ 
		"				av.attribute_code, \n"+
		"				par.rule_id, \n"+
		"				par.link_attr_code, \n"+
		"				par.target_attr_code \n"+
		"			from \n"+ 
		"				profile_access_rule par \n"+ 
		"				{0} \n"+ 
		"				join person p \n"+  
		"					on {1} \n"+ 
		"				join attribute_value av \n"+  
		"					on av.attribute_code = par.profile_attr_code \n"+ 
		"					and av.template_id = 10 \n"+ 
		"					and p.card_id = av.card_id \n"+ 
		"			where \n"+ 
		"				par.rule_id = ? \n"+ 
		"		)";
	// Запрос для извлечения самых верхних родителей на основе backlink-атрибута link_attr_code во входном профильном правиле
	// для backlink-атрибута link_attr_code
	private static String SELECT_UPPER_PARENT_CARD_FOR_PROFILE_RULE =
		"			( \n" +
		"				SELECT \n" + 
		"					c.card_id, \n" + 
		"					r.rule_id, \n" + 
		"					functionbacklink(c.card_id, aoU.option_value, aoL.option_value) as link_card_id, \n" +     
		"					pr.linked_status_id, \n" +
		"					{0} as person_id, \n" +
		"					pr.role_code, \n" +
		"					pr.intermed_attr_code, \n" +
		"					pr.profile_attr_code, \n" +
		"					pr.target_attr_code \n" +
		"				FROM card c \n" +
		"					JOIN access_rule r \n" + 	
		//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*		"							ON 	(c.template_id=r.template_id OR r.template_id IS NULL) \n" + 
		"							AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
		"							ON 	(c.template_id=r.template_id) \n" + 
		"							AND (c.status_id=r.status_id) \n" +
		"					JOIN profile_access_rule pr \n" +
		"							ON 	r.rule_id=pr.rule_id \n" + 
		"					{1} \n" + 
		"					JOIN attribute_option aoL \n" + 
		"							ON pr.link_attr_code=aoL.attribute_code \n" + 
		"							AND aoL.option_code=''" +AttributeOptions.LINK+"'' \n" +
		"					JOIN attribute_option aoU \n" + 
		"							ON pr.link_attr_code=aoU.attribute_code \n" + 
		"							AND aoU.option_code=''" +AttributeOptions.UPLINK+"'' \n" +
		"				WHERE r.rule_id = ? \n"+
		"				UNION ALL \n" +
		"				SELECT \n" + 
		"					c.card_id, \n" + 
		"					r.rule_id, \n" + 
		"					av.card_id as link_card_id, \n" +     
		"					pr.linked_status_id, \n" +
		"					{0} as person_id, \n" +
		"					pr.role_code, \n" +
		"					pr.intermed_attr_code, \n" +
		"					pr.profile_attr_code, \n" +
		"					pr.target_attr_code \n" +
		"				FROM card c \n" +
		"					JOIN access_rule r \n" + 	
		//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*		"							ON 	(c.template_id=r.template_id OR r.template_id IS NULL) \n" + 
		"							AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
		"							ON 	(c.template_id=r.template_id) \n" + 
		"							AND (c.status_id=r.status_id) \n" +
		"					JOIN profile_access_rule pr \n" +
		"							ON 	r.rule_id=pr.rule_id \n" + 
		"					{1} \n" + 
		"					JOIN attribute_option aoL \n" + 
		"							ON pr.link_attr_code=aoL.attribute_code \n" + 
		"							AND aoL.option_code=''" +AttributeOptions.LINK+"'' \n" +
		"					LEFT JOIN attribute_option aoU \n" + 
		"							ON pr.link_attr_code=aoU.attribute_code \n" + 
		"							AND aoU.option_code=''" +AttributeOptions.UPLINK+"'' \n" +
		"					JOIN attribute_value av \n" +
// необходимо ввести ограничение в утилите, чтобы нельзя было использовать беклинки такого типа
//		"							ON av.attribute_code in (select unnest(string_to_array(aoL.option_value, '';''))) \n" +
"							ON av.attribute_code = aoL.option_value \n" +
		"							and av.number_value = c.card_id \n" +
		"				WHERE r.rule_id = ? \n" +
		"					and aoU.option_value is NULL \n"+
		"			) as c \n"; 
	
	// запрос на извлечение детей первого уровня на основе cardlink-атрибута link_attr_code во входном профильном правиле
	// для cardlink-атрибута link_attr_code
	private static String SELECT_FIRST_CHILD_CARD_FOR_PROFILE_RULE =
		"			( \n" +
		"				SELECT \n" + 
		"					c.card_id, \n" + 
		"					r.rule_id, \n" + 
		"					lv.number_value as link_card_id, \n" +     
		"					{0} as person_id, " +
		"					pr.linked_status_id, \n" +
		"					pr.role_code, \n" +
		"					pr.profile_attr_code, \n" +
		"					pr.target_attr_code, \n" +
		"					pr.intermed_attr_code \n" +
		"				FROM 	\n" +
		"					card c \n" +
		"					JOIN access_rule r \n" +
		//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*		"							ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
		"							AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
		"							ON (c.template_id=r.template_id) \n" +
		"							AND (c.status_id=r.status_id) \n" +
		"					JOIN profile_access_rule pr \n" +
		"							ON r.rule_id=pr.rule_id \n" +
		"					{1} " +
		"					JOIN attribute_value lv \n" +
		"							ON c.card_id=lv.card_id \n" +
		"							AND pr.link_attr_code=lv.attribute_code \n" +
		"				WHERE  \n" +
		"					r.rule_id=?\n" +
		"			) as c \n"; 
	
	// запрос на извлечение детей первого уровня на основе personlink-атрибута link_attr_code во входном профильном правиле
	// для personlink-атрибута link_attr_code
	// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
	/*private static String SELECT_FIRST_CHILD_USER_CARD_FOR_PROFILE_RULE =
		"			( \n" +
		"				SELECT \n" + 
		"					c.card_id, \n" + 
		"					r.rule_id, \n" + 
		"					p1.card_id  as link_card_id, \n" +     
		"					sr.person_id, " +
		"					pr.linked_status_id, \n" +
		"					pr.role_code, \n" +
		"					pr.profile_attr_code, \n" +
		"					pr.target_attr_code, \n" +
		"					pr.intermed_attr_code \n" +
		"				FROM 	\n" +
		"					card c \n" +
		"					JOIN access_rule r \n" +
		"							ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
		"							AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +
		"					JOIN profile_access_rule pr \n" +
		"							ON r.rule_id=pr.rule_id \n" +
		"					LEFT JOIN person_role sr \n" +
		"							ON pr.role_code=sr.role_code \n" +
		"					JOIN attribute_value lv \n" +
		"							ON c.card_id=lv.card_id \n" +
		"							AND pr.link_attr_code=lv.attribute_code \n" +
		"					JOIN person p1 " +
		"							ON p1.person_id = lv.number_value" +
		"				WHERE  \n" +
		"					r.rule_id=?\n" +
		"			) as c \n"; 
	*/
	// список колонок, которые надо извлечь из предыдущих карточек, чтобы использовать полученный список в качестве первых родительских
	// для backlink-атрибута intermed_attr_code
	private static String COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE =
		"		c.card_id, \n" +
		"		c.rule_id, \n" +
		"		lv.card_id as link_card_id, \n" +
		"		c.linked_status_id, \n" +
		"		NULL as role_code, \n" +
		"		c.profile_attr_code, \n" +
		"		c.target_attr_code, \n" +
		"		o.option_value as link_attr_code \n";

	// список колонок, которые надо извлечь из предыдущих карточек, чтобы изспользовать полученный список в качестве первых дочерних
	// для cardlink-атрибута intermed_attr_code
	private static String COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE =
		"			c.card_id, \n" +
		"			c.rule_id, \n" +
		"			lv.number_value as link_card_id, \n" +
		"			c.linked_status_id, \n" +
		"			NULL as role_code, \n" +
		"			c.profile_attr_code, \n" +
		"			c.target_attr_code, \n" +
		"			c.intermed_attr_code as link_attr_code \n";

	// запрос на извлечение родителей первого уровня в профильных правилах для вычесленных ввиде дерева карточек  
	// для backlink-атрибута intermed_attr_code
	private static String SELECT_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE =
		"	select \n" +
		"		ct.card_id, \n" +
		"		ct.rule_id, \n" +
		"		lv.card_id as link_card_id, \n" +
		"		ct.linked_status_id, \n" +
		"		ct.role_code, \n" +
		"		ct.profile_attr_code, \n" +
		"		ct.target_attr_code, \n" +
		"		ct.link_attr_code \n" +
		"	from \n" +
		"		card_tree ct \n" +
		"		JOIN attribute_value lv \n" +
		"				ON ct.link_card_id=lv.number_value \n" +
		"				AND lv.attribute_code = ct.link_attr_code \n";
	// запрос на извлечение детей первого уровня в профильных правилах для вычисленных в виде дерева карточек  
	// для cardlink-атрибута intermed_attr_code
	private static String SELECT_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE =
		"	select \n" +
		"		ct.card_id, \n" +
		"		ct.rule_id, \n" +
		"		lv.number_value as link_card_id, \n" +
		"		ct.linked_status_id, \n" +
		"		ct.role_code, \n" +
		"		ct.profile_attr_code, \n" +
		"		ct.target_attr_code, \n" +
		"		ct.link_attr_code \n" +
		"	from \n" +
		"		card_tree ct \n" +
		"		JOIN attribute_value lv \n" +
		"				ON ct.link_card_id=lv.card_id \n" +
		"				AND ct.link_attr_code=lv.attribute_code \n";
	// запрос на извлечение детей первого уровня в профильных правилах для вычисленных в виде дерева карточек  
	// для personlink-атрибута intermed_attr_code
	// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
	/*private static String SELECT_FIRST_CHILD_USER_CARD_OF_PREV_CARD_FOR_PROFILE_RULE =
		"	select \n" +
		"		ct.card_id, \n" +
		"		ct.rule_id, \n" +
		"		lc.card_id as link_card_id, \n" +
		"		p.person_id, \n" + 
		"		tv.attr_value_id, \n" +
		"		ct.linked_status_id, \n" +
		"		ct.role_code, \n" +
		"		ct.profile_attr_code, \n" +
		"		ct.target_attr_code, \n" +
		"		ct.link_attr_code \n" +
		"	from \n" +
		"		card_tree ct \n" +
		"		JOIN attribute_value lv \n" +
		"				ON ct.link_card_id=lv.card_id \n" +
		"				AND ct.link_attr_code=lv.attribute_code \n" +
		"		JOIN card lc 	\n" +
		"				ON lc.card_id = (select p1.card_id from person p1 where p1.person_id = lv.number_value) \n" +
		"				AND (ct.linked_status_id=lc.status_id OR ct.linked_status_id IS NULL) \n" +
		"		JOIN attribute_value tv \n" +
		"				ON lc.card_id=tv.card_id \n" +
		"				AND ct.target_attr_code=tv.attribute_code \n" +
		"		JOIN attribute_value pv \n" +
		"				ON tv.number_value=pv.number_value \n" +
		"				AND ct.profile_attr_code=pv.attribute_code \n" +
		"		JOIN person p \n" +
		"				ON p.card_id=pv.card_id \n" +
		"		WHERE \n" +
		"			(ct.role_code IS NULL OR p.person_id=ct.person_id) \n";
	*/
	// подзапросы для вычисления первых родительских карточек для найденных ранее карточек, используется совместно с COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE
	// для backlink-атрибута intermed_attr_code
	private static String JOIN_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE =
		"			JOIN attribute_option o \n" +
		"					ON c.intermed_attr_code=o.attribute_code \n" +
		"					AND o.option_code='" + AttributeOptions.LINK + "' \n" +
		"			JOIN attribute_value lv \n" +
		"					ON c.link_card_id=lv.number_value \n" +
		"					AND o.option_value=lv.attribute_code \n";
		
	// подзапросы для вычисления первых дочерних карточек для найденных ранее карточек, используется совместно с COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE
	// для cardlink-атрибута intermed_attr_code
	private static String JOIN_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE =
		"			JOIN attribute_value lv \n" +
		"					ON c.link_card_id=lv.card_id \n" +		// ??? перепроверить
		"					AND c.intermed_attr_code=lv.attribute_code \n";

	// подзапросы для вычисления первых дочерних карточек для найденных ранее карточек, используется совместно с COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE
	// для personlink-атрибута intermed_attr_code
	// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
	/*private static String JOIN_FIRST_CHILD_USER_CARD_OF_PREV_CARD_FOR_PROFILE_RULE =
		"			JOIN attribute_value lv \n" +
		"					ON c.link_card_id=lv.card_id \n" +
		"					AND c.intermed_attr_code=lv.attribute_code \n" +
		"			JOIN card lc 	\n" +
		"					ON lc.card_id = (select p1.card_id from person p1 where p1.person_id = lv.number_value) \n" +
		"					AND (c.linked_status_id=lc.status_id OR c.linked_status_id IS NULL) \n" +
		"			JOIN attribute_value tv \n" +
		"					ON lc.card_id=tv.card_id \n" +
		"					AND c.target_attr_code=tv.attribute_code \n" +
		"			JOIN attribute_value pv \n" +
		"					ON tv.number_value=pv.number_value \n" +
		"					AND c.profile_attr_code=pv.attribute_code \n" +
		"			JOIN person p \n" +
		"					ON p.card_id=pv.card_id \n";
	*/
	// where-часть всех запросов - проверка на совпадение пользователя, прописанного в целевой карточке, с пользователем, обладающим ролью, прописанной в правиле
	private static String WHERE_PERSON_IN_CARD_EQUALS_PERSON_WITH_ROLE_IN_PROFILE_RULE = 
		"		{0} \n";
	/**
	 * Сложная функция, выдающая запрос для извлечения карточек, которые удовлетворяют входному правилу.
	 * Для атрибутов связи и атрибутов промежуточной связи карточки строятся по следующему алгоритму:
	 * 1. для атрибутов связи ищутся связанные карточки первого уровня:
	 * 		1.1 Для Кардлинков - дети первого уровня
	 * 		1.2 Для Бэклинков - самые верхние родители (т.е. с анализом UPLINK-ов)
	 * 2. для атрибутов промежуточной связи ищутся дерево связанных карточек 
	 * 		2.1 Для Кардлинков - дети первого и ниже уровня относительно карточек, найденных по атрибуту связи  
	 * 		2.2 Для Бэклинков - родители первого и выше уровней относительно карточек, найденных по атрибуту связи
	 * @param
	 * rule - входное правило
	 * @return
	 * Запрос на извлечение искомого списка карточек
	 **/
	private String getSqlQueryForAccessRules(AccessRule rule) {
		//TODO в утилите при пересчете прав на карточки надо исключить из пересчета
		//неактивные карточки (у которых is_active=0 в таблице card)
		String query = null;
		String role_check_string;
		String role_check_string2;
		if (rule instanceof PersonAccessRule) {
			Attribute linkAttr = ((PersonAccessRule) rule).getLinkAttribute();
			Attribute intermediateLinkAttr = ((PersonAccessRule) rule).getIntermediateLinkAttribute();
			ObjectId stateId = ((PersonAccessRule) rule).getLinkedStateId();
			ObjectId roleCodeId = ((PersonAccessRule) rule).getRoleId();
			logger.info("Linked state id = "+((stateId!=null)?stateId.getId():""));
			if (linkAttr == null) {
				query =		// Access by card's own attributes
					"	SELECT \n" +
					"		c.card_id, \n" +
					"		r.rule_id, \n" +
					"		pv.number_value, \n" +
					"		pv.attr_value_id \n" +
					"	FROM \n" +
					"		card c \n" +
					"		JOIN access_rule r \n" +
					//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*					"			ON (c.template_id=r.template_id OR r.template_id IS NULL) \n" +
					"			AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +*/
					"			ON (c.template_id=r.template_id) \n" +
					"			AND (c.status_id=r.status_id) \n" +
					"		JOIN person_access_rule pr \n" +
					"			ON r.rule_id=pr.rule_id \n";
				// если роль задана, то тобавляем фильтрацию
				role_check_string2 = "		AND (pr.role_code is NULL) \n";
				if (roleCodeId!=null) {
					query = query +
							"		JOIN person_role sr \n"+ 
							"			ON pr.role_code=sr.role_code ";
					role_check_string2 = "		AND (pv.number_value=sr.person_id) \n";
				} 
				query = query +	
					"		JOIN attribute_value pv \n" +
					"			ON c.card_id=pv.card_id \n" +
					"			AND pr.person_attr_code=pv.attribute_code \n" +
					"	WHERE \n" +
					"		(1=1) \n" +
					role_check_string2+
					"		AND r.rule_id=?";
				// добавляем обработку кардлинков и бэклинков в промежуточной связи 
				// Возможна комбинация BackLink/BackLink, BackLink/CardLink, CardLink/BackLink, CardLink/CardLink для атрибута связи и промежуточной связи  
			} else if (Attribute.TYPE_BACK_LINK.equals(linkAttr.getType())){
					// вариант BackLink в качестве атрибута промежуточной связи - при этом для атрибута промежуточной связи используется только LINK-часть, иначе запрос становится слишком тяжелым, т.к. строится дерево связанных карточек (реккурсивно ищутся родительские карточки) 
				if (intermediateLinkAttr!=null&&Attribute.TYPE_BACK_LINK.equals(intermediateLinkAttr.getType())){
					query =		
						"WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, linked_status_id, role_code, person_attr_code, link_attr_code) AS (\n" +
						"		SELECT \n" +
									COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						"		FROM \n" +
						SELECT_UPPER_PARENT_CARD_FOR_PERSON_RULE +
						JOIN_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE +

						"	UNION ALL \n" +
						SELECT_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE + " )" + 

						MessageFormat.format(SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PERSON_RULES, 
								(stateId!=null&&stateId.getId()!=null) ? "join card lc ON ct.link_card_id=lc.card_id and (ct.linked_status_id=lc.status_id) \n": "", 
								roleCodeId != null ? "join person_role pr on pv.number_value=pr.person_id and pr.role_code = ct.role_code \n" : "");
				} else if (intermediateLinkAttr!=null && (Attribute.TYPE_CARD_LINK.equals(intermediateLinkAttr.getType()) || Attribute.TYPE_TYPED_CARD_LINK.equals(intermediateLinkAttr.getType()))){
					// вариант CardLink в качестве атрибута промежуточной связи, строится дерево связанных карточек (реккурсивно ищутся дочерние карточки для родителей, найденных по атрибуту связи) 
					query =		
						"WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, linked_status_id, role_code, person_attr_code, link_attr_code) AS (\n" +
						"		SELECT \n" +
									COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						"		FROM \n" +
						SELECT_UPPER_PARENT_CARD_FOR_PERSON_RULE +
						JOIN_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE + 
						"	UNION ALL \n" +
						SELECT_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						") \n" +
						MessageFormat.format(SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PERSON_RULES, 
								(stateId!=null&&stateId.getId()!=null) ? "join card lc ON ct.link_card_id=lc.card_id and (ct.linked_status_id=lc.status_id)": "",
								roleCodeId != null ? "join person_role pr on pv.number_value=pr.person_id and pr.role_code = ct.role_code \n" : "");
					// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
				/*} else if (intermediateLinkAttr!=null && (Attribute.TYPE_PERSON.equals(intermediateLinkAttr.getType()))){
					// вариант PersonLink в качестве атрибута промежуточной связи, строится дерево связанных карточек (реккурсивно ищутся дочерние карточки для родителей, найденных по атрибуту связи) 
					query =		
						"WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, person_id, attr_value_id, linked_status_id, role_code, person_attr_code, link_attr_code) AS (\n" +
						"		SELECT \n" +
									COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						"		FROM \n" +
									SELECT_UPPER_PARENT_CARD_FOR_PERSON_RULE +
									JOIN_FIRST_CHILD_USER_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						"		WHERE \n" +
									WHERE_PERSON_IN_CARD_EQUALS_PERSON_WITH_ROLE_IN_PERSON_RULE +
						"	UNION ALL \n" +
							SELECT_FIRST_CHILD_USER_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						") \n" +
						SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE; */
				} else {
					// случай, когда берутся только родители верхнего уровня
 
					query =		// Access by linked card's attribute (reverse link)
						"		select \n" + 
						"			c.card_id, \n" + 
						"			c.rule_id, \n" +
						"			pv.number_value as person_id, \n" + 
						"			pv.attr_value_id \n" +
						"		FROM \n" +
						SELECT_UPPER_PARENT_CARD_FOR_PERSON_RULE +
						"			JOIN card lc \n" + 
						"					ON lc.card_id = c.link_card_id \n" +
						((stateId!=null&&stateId.getId()!=null)?
						"					AND (c.linked_status_id=lc.status_id) \n":"") + 
						"			JOIN attribute_value pv \n" +
						"					ON lc.card_id=pv.card_id \n" + 
						"					AND c.person_attr_code=pv.attribute_code \n" + 
						(roleCodeId != null ? "join person_role pr on pv.number_value=pr.person_id and pr.role_code = c.role_code \n" : "") +
						";";
				}
			} else if (Attribute.TYPE_CARD_LINK.equals(linkAttr.getType()) || Attribute.TYPE_TYPED_CARD_LINK.equals(linkAttr.getType()) ||
					Attribute.TYPE_DATED_TYPED_CARD_LINK.equals(linkAttr.getType())) {
				// вариант BackLink в качестве атрибута промежуточной связи - при этом для атрибута промежуточной связи используется только LINK-часть, иначе запрос становится слишком тяжелым, т.к. строится дерево связанных карточек (реккурсивно ищутся родительские карточки для детей первого уровня, найденных по атрибуту связи) 
				if (intermediateLinkAttr!=null&&Attribute.TYPE_BACK_LINK.equals(intermediateLinkAttr.getType())){
					query =		
						"WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, linked_status_id, role_code, person_attr_code, link_attr_code) AS (\n" +
						"	SELECT \n" +
								COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						"	FROM \n" +
						SELECT_FIRST_CHILD_CARD_FOR_PERSON_RULE +
						JOIN_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE + 
						"	UNION ALL \n" +
						SELECT_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE + 
						") \n" +
						MessageFormat.format(SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PERSON_RULES,
							(stateId!=null&&stateId.getId()!=null) ? "join card lc ON ct.link_card_id=lc.card_id and (ct.linked_status_id=lc.status_id)": "",
							roleCodeId != null ? "join person_role pr on pv.number_value=pr.person_id and pr.role_code = ct.role_code \n" : "");
				} else if (intermediateLinkAttr!=null && (Attribute.TYPE_CARD_LINK.equals(intermediateLinkAttr.getType()) || Attribute.TYPE_TYPED_CARD_LINK.equals(intermediateLinkAttr.getType()))){
					// вариант CardLink в качестве атрибута промежуточной связи, строится дерево связанных карточек (реккурсивно ищутся дочерние карточки для родителей, найденных по атрибуту связи) 
					query =		
						"WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, linked_status_id, role_code, person_attr_code, link_attr_code) AS (\n" +
						"	SELECT \n" +
								COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						"	FROM \n" +
						SELECT_FIRST_CHILD_CARD_FOR_PERSON_RULE +
						JOIN_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						"	UNION ALL \n" +
						SELECT_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						") \n" +
						MessageFormat.format(SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PERSON_RULES,
								(stateId!=null&&stateId.getId()!=null) ? "join card lc ON ct.link_card_id=lc.card_id and (ct.linked_status_id=lc.status_id)": "",
								roleCodeId != null ? "join person_role pr on pv.number_value=pr.person_id and pr.role_code = ct.role_code \n" : "");
					// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
				} /*else if (intermediateLinkAttr!=null && (Attribute.TYPE_PERSON.equals(intermediateLinkAttr.getType()))){
					// вариант PersonLink в качестве атрибута промежуточной связи, строится дерево связанных карточек (реккурсивно ищутся дочерние карточки для родителей, найденных по атрибуту связи) 
					query =		
						"WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, person_id, attr_value_id, linked_status_id, role_code, person_attr_code, link_attr_code) AS (\n" +
						"	SELECT \n" +
								COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						"	FROM \n" +
								SELECT_FIRST_CHILD_CARD_FOR_PERSON_RULE +
								JOIN_FIRST_CHILD_USER_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						"	WHERE \n" +
								WHERE_PERSON_IN_CARD_EQUALS_PERSON_WITH_ROLE_IN_PERSON_RULE +
						"	UNION ALL \n" +
							SELECT_FIRST_CHILD_USER_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						") \n" +
						SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE; 
				} */else {
					query =		// Access by linked card's attribute (forward link)
						"	SELECT \n" +
						"		c.card_id, \n" +
						"		c.rule_id, \n" +
						"		pv.number_value, \n" +
						"		pv.attr_value_id \n" +
						"	FROM \n" +
						SELECT_FIRST_CHILD_CARD_FOR_PERSON_RULE +
						"		JOIN card lc \n" +
						"			ON c.link_card_id=lc.card_id \n" +
						((stateId!=null&&stateId.getId()!=null)?
						"			AND (c.linked_status_id=lc.status_id) \n":"") + 
						"		JOIN attribute_value pv \n" +
						"			ON lc.card_id=pv.card_id \n" +
						"			AND c.person_attr_code=pv.attribute_code \n" +
						(roleCodeId != null ? "join person_role pr on pv.number_value=pr.person_id and pr.role_code = c.role_code \n" : "") +
						";";
				}
				// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
			} /*else if (Attribute.TYPE_PERSON.equals(linkAttr.getType())) {
				// вариант BackLink в качестве атрибута промежуточной связи - при этом для атрибута промежуточной связи используется только LINK-часть, иначе запрос становится слишком тяжелым, т.к. строится дерево связанных карточек (реккурсивно ищутся родительские карточки для детей первого уровня, найденных по атрибуту связи) 
				if (intermediateLinkAttr!=null&&Attribute.TYPE_BACK_LINK.equals(intermediateLinkAttr.getType())){
					query =		
						"WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, person_id, attr_value_id, linked_status_id, role_code, person_attr_code, link_attr_code) AS (\n" +
						"	SELECT \n" +
								COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						"	FROM \n" +
								SELECT_FIRST_CHILD_USER_CARD_FOR_PERSON_RULE +
								JOIN_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						"	WHERE \n" +
								WHERE_PERSON_IN_CARD_EQUALS_PERSON_WITH_ROLE_IN_PERSON_RULE +
						"	UNION ALL \n" +
								SELECT_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						") \n" +
						SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE; 
				} else if (intermediateLinkAttr!=null && (Attribute.TYPE_CARD_LINK.equals(intermediateLinkAttr.getType()) || Attribute.TYPE_TYPED_CARD_LINK.equals(intermediateLinkAttr.getType()))){
					// вариант CardLink в качестве атрибута промежуточной связи, строится дерево связанных карточек (реккурсивно ищутся дочерние карточки для родителей, найденных по атрибуту связи) 
					query =		
						"WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, person_id, attr_value_id, linked_status_id, role_code, person_attr_code, link_attr_code) AS (\n" +
						"	SELECT \n" +
								COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						"	FROM \n" +
								SELECT_FIRST_CHILD_USER_CARD_FOR_PERSON_RULE +
								JOIN_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						"	WHERE \n" +
								WHERE_PERSON_IN_CARD_EQUALS_PERSON_WITH_ROLE_IN_PERSON_RULE +
						"	UNION ALL \n" +
							SELECT_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						") \n" +
						SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE; 
				} else if (intermediateLinkAttr!=null && (Attribute.TYPE_PERSON.equals(intermediateLinkAttr.getType()))){
					// вариант PersonLink в качестве атрибута промежуточной связи, строится дерево связанных карточек (реккурсивно ищутся дочерние карточки для родителей, найденных по атрибуту связи) 
					query =		
						"WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, person_id, attr_value_id, linked_status_id, role_code, person_attr_code, link_attr_code) AS (\n" +
						"	SELECT \n" +
								COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						"	FROM \n" +
								SELECT_FIRST_CHILD_USER_CARD_FOR_PERSON_RULE +
								JOIN_FIRST_CHILD_USER_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						"	WHERE \n" +
								WHERE_PERSON_IN_CARD_EQUALS_PERSON_WITH_ROLE_IN_PERSON_RULE +
						"	UNION ALL \n" +
							SELECT_FIRST_CHILD_USER_CARD_OF_PREV_CARD_FOR_PERSON_RULE +
						") \n" +
						SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE; 
				} else {
					query =		// Access by linked card's attribute (forward link)
						"	SELECT \n" +
						"		c.card_id, \n" +
						"		c.rule_id, \n" +
						"		pv.number_value, \n" +
						"		pv.attr_value_id \n" +
						"	FROM \n" +
								SELECT_FIRST_CHILD_USER_CARD_FOR_PERSON_RULE +
						"		JOIN card lc \n" +
						"			ON c.link_card_id=lc.card_id \n" +
						"			AND (c.linked_status_id=lc.status_id OR c.linked_status_id IS NULL) \n" +
						"		JOIN attribute_value pv \n" +
						"			ON lc.card_id=pv.card_id \n" +
						"			AND c.person_attr_code=pv.attribute_code \n" +
						"	WHERE \n" +
								WHERE_PERSON_IN_CARD_EQUALS_PERSON_WITH_ROLE_IN_PERSON_RULE +
						";";
				}
			}*/
		} else if (rule instanceof PersonProfileAccessRule) {
			Attribute linkAttr = ((PersonProfileAccessRule) rule).getLinkAttribute();
			Attribute intermediateLinkAttr = ((PersonProfileAccessRule) rule).getIntermediateLinkAttribute();
			ObjectId stateId = ((PersonProfileAccessRule) rule).getLinkedStateId();
			ObjectId roleCodeId = ((PersonProfileAccessRule) rule).getRoleId();
			logger.info("Linked state id = "+((stateId!=null)?stateId.getId():""));
			if (linkAttr == null){
				query =		
						"WITH ";
				if (roleCodeId != null) {
					query = query + 
							MessageFormat.format(PERSON_ALL_PROFILES,   
										"					JOIN person_role sr \n"+
										"							ON par.role_code=sr.role_code \n", "(p.person_id=sr.person_id)");
				} else {
					query = query +
							MessageFormat.format(PERSON_ALL_PROFILES, "", "(par.role_code IS NULL)");
				}
				query = query +		
					", rules as ( \n" +
					"  select a1.template_id, \n" + 
					"    a1.status_id, \n" +
					"    a1.rule_id, \n" +
					"    a2.role_code, \n" +
					"    a2.target_attr_code, \n" + 
					"    a2.profile_attr_code, \n" +
					"    a3.card_id \n" +
					"  from access_rule a1 \n" +
					"    join profile_access_rule a2 \n" + 
					"      on a1.rule_id = ? and a1.rule_id=a2.rule_id \n" + 
					"    join card a3 \n" +
					//(BR4J00037800, YNikitin, 2014/10/14) для профильных и персональных правил шаблон и статусы не могут быть пустыми		
/*					"      on (a3.template_id=a1.template_id OR a1.template_id IS NULL) \n" + 
					"     and (a3.status_id=a1.status_id OR a1.status_id IS NULL) \n" +*/
					"      on (a3.template_id=a1.template_id) \n" + 
					"     and (a3.status_id=a1.status_id) \n" +
					") " +
					"	SELECT \n" +
					"		r.card_id, \n" +
					"		r.rule_id, \n" +
					"		pv.person_id, \n" +
					"		tv.attr_value_id \n" +
					"	FROM \n" +
					"		rules r \n";
				query = query +	
					"		JOIN attribute_value tv \n" +
					"				ON r.card_id=tv.card_id \n" +
					"				AND r.target_attr_code=tv.attribute_code \n" +
					"		JOIN person_all_profiles pv \n" +
					"				ON tv.number_value=pv.number_value \n" +
					"				AND r.profile_attr_code=pv.attribute_code \n" +
					"	WHERE (1=1)\n";
			}
			// добавляем обработку кардлинков и бэклинков в промежуточной связи 
			// Возможна комбинация BackLink/BackLink, BackLink/CardLink, CardLink/BackLink, CardLink/CardLink для атрибута связи и промежуточной связи  
			else if (Attribute.TYPE_BACK_LINK.equals(linkAttr.getType())){
				// вариант BackLink в качестве атрибута промежуточной связи - при этом для атрибута промежуточной связи используется только LINK-часть, иначе запрос становится слишком тяжелым, т.к. строится дерево связанных карточек (реккурсивно ищутся родительские карточки) 
				if (intermediateLinkAttr!=null&&Attribute.TYPE_BACK_LINK.equals(intermediateLinkAttr.getType())){

					query =		
							"WITH RECURSIVE ";
					if (roleCodeId != null) {
						query = query + 
								MessageFormat.format(PERSON_ALL_PROFILES,   
											"					JOIN person_role sr \n"+
											"							ON par.role_code=sr.role_code \n", "(p.person_id=sr.person_id)");
					} else {
						query = query +
								MessageFormat.format(PERSON_ALL_PROFILES, "", "(par.role_code IS NULL)");
					}
					query = query +		
							", card_tree(card_id, rule_id, link_card_id, linked_status_id, role_code, profile_attr_code, target_attr_code, link_attr_code) AS (\n"+
							"		SELECT \n" +
										COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE +
							"		FROM \n" +
							MessageFormat.format(SELECT_UPPER_PARENT_CARD_FOR_PROFILE_RULE, "NULL", "");
	
					query = query +	
						JOIN_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE + 
						"	UNION ALL \n" +
						SELECT_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE +
						") \n" +
					MessageFormat.format(SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PROFILE_RULES,
							(stateId!=null&&stateId.getId()!=null) ? "join card lc ON ct.link_card_id=lc.card_id and (ct.linked_status_id=lc.status_id) \n": "");
								
				} else if (intermediateLinkAttr!=null && (Attribute.TYPE_CARD_LINK.equals(intermediateLinkAttr.getType()) || Attribute.TYPE_TYPED_CARD_LINK.equals(intermediateLinkAttr.getType()))){
					// вариант CardLink в качестве атрибута промежуточной связи, строится дерево связанных карточек (реккурсивно ищутся дочерние карточки для родителей, найденных по атрибуту связи) 
					query =		
							"WITH RECURSIVE ";
					if (roleCodeId != null) {
						query = query + 
								MessageFormat.format(PERSON_ALL_PROFILES,   
											"					JOIN person_role sr \n"+
											"							ON par.role_code=sr.role_code \n", "(p.person_id=sr.person_id)");
					} else {
						query = query +
								MessageFormat.format(PERSON_ALL_PROFILES, "", "(par.role_code IS NULL)");
					}
					query = query +		
							", card_tree(card_id, rule_id, link_card_id, linked_status_id, role_code, profile_attr_code, target_attr_code, link_attr_code) AS (\n"+
							"	SELECT \n" +
							COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE +
							"	FROM \n" +
							MessageFormat.format(SELECT_UPPER_PARENT_CARD_FOR_PROFILE_RULE, "NULL", "");
					query = query +		
							JOIN_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE + 
							"	UNION ALL \n" +
							SELECT_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE +
							") \n" +
							MessageFormat.format(SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PROFILE_RULES,
									(stateId!=null&&stateId.getId()!=null) ? "join card lc ON ct.link_card_id=lc.card_id and (ct.linked_status_id=lc.status_id) \n": ""); 
					// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
				} /*else if (intermediateLinkAttr!=null && (Attribute.TYPE_PERSON.equals(intermediateLinkAttr.getType()))){
					// вариант CardLink в качестве атрибута промежуточной связи, строится дерево связанных карточек (реккурсивно ищутся дочерние карточки для родителей, найденных по атрибуту связи) 
					query =		
						"WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, person_id, attr_value_id, linked_status_id, role_code, profile_attr_code, target_attr_code, link_attr_code) AS (\n" +
						"	SELECT \n" +
									COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE +
						"	FROM \n" +
									SELECT_UPPER_PARENT_CARD_FOR_PROFILE_RULE +	
									JOIN_FIRST_CHILD_USER_CARD_OF_PREV_CARD_FOR_PROFILE_RULE +
						"	WHERE \n" +
								WHERE_PERSON_IN_CARD_EQUALS_PERSON_WITH_ROLE_IN_PROFILE_RULE +
						"	UNION ALL \n" +
							SELECT_FIRST_CHILD_USER_CARD_OF_PREV_CARD_FOR_PROFILE_RULE +
						") \n" +
						SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE; 
				} */else {
					// случай, когда берутся только верхние родители
					query =		
							"WITH  ";
					if (roleCodeId != null) {
						query = query + 
								MessageFormat.format(PERSON_ALL_PROFILES,   
											"					JOIN person_role sr \n"+
											"							ON par.role_code=sr.role_code \n", "(p.person_id=sr.person_id)");
					} else {
						query = query +
								MessageFormat.format(PERSON_ALL_PROFILES, "", "(par.role_code IS NULL)");
					}
					query = query +		
							"		select \n" + 
							"			c.card_id, \n" + 
							"			c.rule_id, \n" +
							"			pv.person_id, \n" + 
							"			tv.attr_value_id \n" +
							"		FROM \n" +
							MessageFormat.format(SELECT_UPPER_PARENT_CARD_FOR_PROFILE_RULE, "NULL", "") +	
							"			JOIN card lc \n" + 
							"					ON lc.card_id = c.link_card_id \n" +
							((stateId!=null&&stateId.getId()!=null)?
							"					AND (c.linked_status_id=lc.status_id) \n":"") + 
							"			JOIN attribute_value tv \n" +
							"					ON lc.card_id=tv.card_id \n" +
							"					AND c.target_attr_code=tv.attribute_code \n" +
							"			JOIN person_all_profiles pv \n" +
							"					ON tv.number_value=pv.number_value \n" +
							"					AND c.profile_attr_code=pv.attribute_code \n" +
							";";
				}
			} else if (Attribute.TYPE_CARD_LINK.equals(linkAttr.getType()) || Attribute.TYPE_TYPED_CARD_LINK.equals(linkAttr.getType())) {
				if (intermediateLinkAttr!=null&&Attribute.TYPE_BACK_LINK.equals(intermediateLinkAttr.getType())){

					query =		
							"WITH RECURSIVE ";
					if (roleCodeId != null) {
						query = query + 
								MessageFormat.format(PERSON_ALL_PROFILES,   
											"					JOIN person_role sr \n"+
											"							ON par.role_code=sr.role_code \n", "(p.person_id=sr.person_id)");
					} else {
						query = query +
								MessageFormat.format(PERSON_ALL_PROFILES, "", "(par.role_code IS NULL)");
					}
					query = query +		
							", card_tree(card_id, rule_id, link_card_id, linked_status_id, role_code, profile_attr_code, target_attr_code, link_attr_code) AS (\n" +
							"	select \n" +
									COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE	+
							"	from \n" +
							MessageFormat.format(SELECT_FIRST_CHILD_CARD_FOR_PROFILE_RULE, "NULL", "");
					query = query +		
							JOIN_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE + 
							"	UNION ALL \n" +
							SELECT_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE + 
							") \n" +
							MessageFormat.format(SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PROFILE_RULES,
									(stateId!=null&&stateId.getId()!=null) ? "join card lc ON ct.link_card_id=lc.card_id and (ct.linked_status_id=lc.status_id) \n": "");  
				} else if (intermediateLinkAttr!=null && (Attribute.TYPE_CARD_LINK.equals(intermediateLinkAttr.getType()) || Attribute.TYPE_TYPED_CARD_LINK.equals(intermediateLinkAttr.getType()))){
					// вариант CardLink в качестве атрибута промежуточной связи, строится дерево связанных карточек (реккурсивно ищутся дочерние карточки для родителей, найденных по атрибуту связи) 
					query =		
							"WITH RECURSIVE ";
					if (roleCodeId != null) {
						query = query + 
								MessageFormat.format(PERSON_ALL_PROFILES,   
											"					JOIN person_role sr \n"+
											"							ON par.role_code=sr.role_code \n", "(p.person_id=sr.person_id)");
					} else {
						query = query +
								MessageFormat.format(PERSON_ALL_PROFILES, "", "(par.role_code IS NULL)");
					}
					query = query +		
							", card_tree(card_id, rule_id, link_card_id, linked_status_id, role_code, profile_attr_code, target_attr_code, link_attr_code) AS (\n" +
							"	select \n" +
									COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE	+
							"	from \n" +
							MessageFormat.format(SELECT_FIRST_CHILD_CARD_FOR_PROFILE_RULE, "NULL", "");
					query = query +		
							JOIN_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE + 
							"	UNION ALL \n" +
							SELECT_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE + 
							") \n" +
							MessageFormat.format(SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PROFILE_RULES,
									(stateId!=null&&stateId.getId()!=null) ? "join card lc ON ct.link_card_id=lc.card_id and (ct.linked_status_id=lc.status_id) \n": "");; 
					// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
				} /*else if (intermediateLinkAttr!=null && (Attribute.TYPE_PERSON.equals(intermediateLinkAttr.getType()))){
					// вариант PersonLink в качестве атрибута промежуточной связи, строится дерево связанных карточек (реккурсивно ищутся дочерние карточки для родителей, найденных по атрибуту связи) 
					query =		
						"WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, person_id, attr_value_id, linked_status_id, role_code, profile_attr_code, target_attr_code, link_attr_code) AS (\n" +
						"	SELECT \n" +
									COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE +
						"   FROM \n" +
									SELECT_FIRST_CHILD_CARD_FOR_PROFILE_RULE +
									JOIN_FIRST_CHILD_USER_CARD_OF_PREV_CARD_FOR_PROFILE_RULE+
						"	WHERE \n" +
								WHERE_PERSON_IN_CARD_EQUALS_PERSON_WITH_ROLE_IN_PROFILE_RULE +
						"	UNION ALL \n" +
							SELECT_FIRST_CHILD_USER_CARD_OF_PREV_CARD_FOR_PROFILE_RULE +
						") \n" +
						SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE; 
				} */else {

					query =		
							"WITH  ";
					if (roleCodeId != null) {
						query = query + 
								MessageFormat.format(PERSON_ALL_PROFILES,   
											"					JOIN person_role sr \n"+
											"							ON par.role_code=sr.role_code \n", "(p.person_id=sr.person_id)");
					} else {
						query = query +
								MessageFormat.format(PERSON_ALL_PROFILES, "", "(par.role_code IS NULL)");
					}
					query = query +		
						"	SELECT \n" +
						"		c.card_id, \n" +
						"		c.rule_id, \n" +
						"		pv.person_id, \n" +
						"		tv.attr_value_id \n" +
						"	FROM \n" +
						MessageFormat.format(SELECT_FIRST_CHILD_CARD_FOR_PROFILE_RULE, "NULL", "") +
						"		JOIN card lc \n" +
						"				ON c.link_card_id=lc.card_id \n" +
						((stateId!=null&&stateId.getId()!=null)?
						"				AND (c.linked_status_id=lc.status_id) \n":"") + 
						"		JOIN attribute_value tv \n" +
						"				ON lc.card_id=tv.card_id \n" +
						"				AND c.target_attr_code=tv.attribute_code \n" +
						"		JOIN person_all_profiles pv \n" +
						"				ON tv.number_value=pv.number_value \n" +
						"				AND c.profile_attr_code=pv.attribute_code \n" +
						";";
				}
				
				// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
			} /*else if (Attribute.TYPE_PERSON.equals(linkAttr.getType())) {
				if (intermediateLinkAttr!=null&&Attribute.TYPE_BACK_LINK.equals(intermediateLinkAttr.getType())){
					query =		
						"WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, person_id, attr_value_id, linked_status_id, role_code, profile_attr_code, target_attr_code, link_attr_code) AS (\n" +
						"	select \n" +
									COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE	+
						"	from \n" +
									SELECT_FIRST_CHILD_USER_CARD_FOR_PROFILE_RULE +
									JOIN_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE +
						"	WHERE \n" +
								WHERE_PERSON_IN_CARD_EQUALS_PERSON_WITH_ROLE_IN_PROFILE_RULE +
						"	UNION ALL \n" +
							SELECT_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE +
						") \n" +
						SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE; 
				} else if (intermediateLinkAttr!=null && (Attribute.TYPE_CARD_LINK.equals(intermediateLinkAttr.getType()) || Attribute.TYPE_TYPED_CARD_LINK.equals(intermediateLinkAttr.getType()))){
					// вариант CardLink в качестве атрибута промежуточной связи, строится дерево связанных карточек (реккурсивно ищутся дочерние карточки для родителей, найденных по атрибуту связи) 
					query =		
						"WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, person_id, attr_value_id, linked_status_id, role_code, profile_attr_code, target_attr_code, link_attr_code) AS (\n" +
						"	SELECT \n" +
									COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE +
						"   FROM \n" +
									SELECT_FIRST_CHILD_USER_CARD_FOR_PROFILE_RULE +
									JOIN_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE+
						"	WHERE \n" +
								WHERE_PERSON_IN_CARD_EQUALS_PERSON_WITH_ROLE_IN_PROFILE_RULE +
						"	UNION ALL \n" +
							SELECT_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE +
						") \n" +
						SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE; 
				} else if (intermediateLinkAttr!=null && (Attribute.TYPE_PERSON.equals(intermediateLinkAttr.getType()))){
					// вариант PersonLink в качестве атрибута промежуточной связи, строится дерево связанных карточек (реккурсивно ищутся дочерние карточки для родителей, найденных по атрибуту связи) 
					query =		
						"WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, person_id, attr_value_id, linked_status_id, role_code, profile_attr_code, target_attr_code, link_attr_code) AS (\n" +
						"	SELECT \n" +
									COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE +
						"   FROM \n" +
									SELECT_FIRST_CHILD_USER_CARD_FOR_PROFILE_RULE +
									JOIN_FIRST_CHILD_USER_CARD_OF_PREV_CARD_FOR_PROFILE_RULE+
						"	WHERE \n" +
								WHERE_PERSON_IN_CARD_EQUALS_PERSON_WITH_ROLE_IN_PROFILE_RULE +
						"	UNION ALL \n" +
							SELECT_FIRST_CHILD_USER_CARD_OF_PREV_CARD_FOR_PROFILE_RULE +
						") \n" +
						SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE; 
				} else {
					query =		// Access by linked card's attribute (forward link)
						"	SELECT \n" +
						"		c.card_id, \n" +
						"		c.rule_id, \n" +
						"		p.person_id, \n" +
						"		tv.attr_value_id \n" +
						"	FROM \n" +
								SELECT_FIRST_CHILD_USER_CARD_FOR_PROFILE_RULE +
						"		JOIN card lc \n" +
						"				ON c.link_card_id=lc.card_id \n" +
						"				AND (c.linked_status_id=lc.status_id OR c.linked_status_id IS NULL) \n" +
						"		JOIN attribute_value tv \n" +
						"				ON lc.card_id=tv.card_id \n" +
						"				AND c.target_attr_code=tv.attribute_code \n" +
						"		JOIN attribute_value pv \n" +
						"				ON tv.number_value=pv.number_value \n" +
						"				AND c.profile_attr_code=pv.attribute_code \n" +
						"		JOIN person p \n" +
						"				ON p.card_id=pv.card_id \n" +
						"	WHERE \n" +
								WHERE_PERSON_IN_CARD_EQUALS_PERSON_WITH_ROLE_IN_PROFILE_RULE +
						";";
				}
				
			}*/
		} else {	// Shall never be executed
			return null;		// No update needed
		}
		return query;
	}
	
	/**
	 * Проверка наличия в системе пользователей с входной ролью
	 * @param roleId - код роли
	 * @return true/false - существует/не существует пользователи с ролью
	 */
	private boolean checkPersonWithRoleExists(ObjectId roleId){
		// если роль не задана, то проверка успешная
		if (roleId==null)
			return true;
		int personCount = jdbc.queryForInt(
				"select count(*) from person_role pr where pr.role_code = ?",
				new Object[] { roleId.getId() }, 
				new int[] { Types.VARCHAR });
		
		return (personCount>0);
	}
	
	/**
	 * Класс Комплексная ссылка - содержит в себе код атрибута связи и код атрибута промежуточной связи
	 * @author MrPie
	 * Change by YNikitin:
	 * добавил разделение на LINK и UPLINK коды - используются для беклинков атрибутов связи и атрибутов промежуточной связи 
	 * (для кардлинков пустые) 
	 */
	private class ComplexLink {
		public String linkAttrId;
		public String intermedAttrId;

		public String linkAttrIdU;
		public String intermedAttrIdU;
		public Long cardId;
		public Integer linkType;
		
		public ObjectId getCardObjectId() {
			return new ObjectId(Card.class, cardId);
		}

		public Long getCardId() {
			return cardId;
		}

		public ComplexLink(String linkAttrId, String intermedAttrId) {
			this.linkAttrId = linkAttrId;
			this.linkAttrIdU = null;
			this.intermedAttrId = intermedAttrId;
			this.intermedAttrIdU = null;
		}

		public ComplexLink(String linkAttrIdL, String linkAttrIdU, String intermedAttrIdL, String intermedAttrIdU, Long cardId, Integer linkType) {
			this.linkAttrId = linkAttrIdL;
			this.linkAttrIdU = linkAttrIdU;
			this.intermedAttrId = intermedAttrIdL;
			this.intermedAttrIdU = intermedAttrIdU;
			this.cardId = cardId;
			this.linkType = linkType;
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || !(obj instanceof ComplexLink))
				return false;
			ComplexLink other = (ComplexLink) obj;
			return 
				(linkAttrId == null ? other.linkAttrId == null : linkAttrId.equals(other.linkAttrId)) &&
				(intermedAttrIdU == null ? other.intermedAttrIdU == null : intermedAttrIdU.equals(other.intermedAttrIdU))&&
				(linkAttrIdU == null ? other.linkAttrIdU == null : linkAttrIdU.equals(other.linkAttrIdU))&&
				(intermedAttrIdU == null ? other.intermedAttrIdU == null : intermedAttrIdU.equals(other.intermedAttrIdU))&&
				(cardId == null ? other.cardId == null : cardId.equals(other.cardId))&&
				(linkType == null ? other.linkType == null : linkType.equals(other.linkType));
		}

		public int hashCode() {
			return 
				(linkAttrId == null ? 0 : linkAttrId.hashCode()) ^
				(intermedAttrId == null ? 0 : intermedAttrId.hashCode()) ^
				(linkAttrIdU == null ? 0 : linkAttrIdU.hashCode()) ^
				(intermedAttrIdU == null ? 0 : intermedAttrIdU.hashCode()) ^ 
				(cardId == null ? 0 : cardId.hashCode()) ^ 
				(linkType == null ? 0 : linkType.hashCode());
		}
		
		@Override
		public String toString(){
			String result = 
			"(" + (cardId == null?"null":cardId.toString()) + "){"+
				((linkAttrId == null)?"null":linkAttrId)+"/"+
			 	((linkAttrIdU == null)?"null":linkAttrIdU)+"}:{"+
			 	((intermedAttrId == null)?"null":intermedAttrId)+"/"+
			 	((intermedAttrIdU == null)?"null":intermedAttrIdU)+"/"+
			 	((linkType == null)?"null":linkType)+"}";
			return result;
		}
	}
	
	@Deprecated
	private HashSet collectLinksUpOld(ObjectId cardId, ComplexLink link) {
		logger.info("Collecting links up by \n" + link.linkAttrId + "/\n" + link.intermedAttrId);
		long start = System.currentTimeMillis();
		HashSet ids = new HashSet();
		List linked = Collections.singletonList(cardId.getId());
		do {
			ArrayList params = new ArrayList(linked.size() + 1);
			params.add(link.linkAttrId);
			params.addAll(linked);
			ids.addAll(jdbc.queryForList(
					"SELECT v.card_id FROM attribute_value v \n" +
					"WHERE v.attribute_code=? \n" +
						"AND v.number_value IN (\n" + populateParameters(linked.size()) + ")",
					params.toArray(),
					Long.class));
			params = new ArrayList(linked.size() + 1);
			params.add(link.intermedAttrId);
			params.addAll(linked);
			linked = jdbc.queryForList(
					"SELECT v.card_id FROM attribute_value v \n" +
					"WHERE v.attribute_code=? \n" +
						"AND v.number_value IN (\n" + populateParameters(linked.size()) + ")",
					params.toArray(),
					Long.class);
		} while (linked.size() > 0);
		long duration = System.currentTimeMillis() - start;
		logger.info(ids.size() + " links collected (" + duration + "ms)");
		return ids;
	}
	
	private HashSet collectLinksUp(ObjectId cardId, ComplexLink link, HashSet types) {
		logger.info("Collecting links up by \n" + link);
		long start = System.currentTimeMillis();
		HashSet ids = new HashSet();
		List linked;
		if (types.contains(LINK_FWD_FWD)||types.contains(LINK_FWD_BACK)||types.contains(LINK_USR_FWD)||types.contains(LINK_USR_BACK)){
			linked = Collections.singletonList(cardId.getId());
			do {
				ArrayList params = new ArrayList(linked.size() + 1);
				params.add(link.linkAttrId);
				params.addAll(linked);
				if (types.contains(LINK_FWD_FWD)||types.contains(LINK_FWD_BACK)){
					ids.addAll(jdbc.queryForList(
							"SELECT v.card_id FROM attribute_value v \n" +
							"WHERE v.attribute_code=? \n" +
								"AND v.number_value IN (\n" + populateParameters(linked.size()) + ")",
							params.toArray(),
							Long.class));
				} else {
					ids.addAll(jdbc.queryForList(
							"SELECT v.card_id FROM attribute_value v \n" +
							"WHERE v.attribute_code=? \n" +
								"AND v.number_value IN (\n" +
								"select p.person_id from person p where p.card_id in ("+ populateParameters(linked.size()) + "))",
							params.toArray(),
							Long.class));
				}
				params = new ArrayList(linked.size() + 1);
				params.add(link.intermedAttrId);
				params.addAll(linked);
				if (types.contains(LINK_FWD_FWD)||types.contains(LINK_USR_FWD)){
					linked = jdbc.queryForList(
						"SELECT v.card_id FROM attribute_value v \n" +
						"WHERE v.attribute_code=? \n" +
							"AND v.number_value IN (\n" + populateParameters(linked.size()) + ")",
						params.toArray(),
						Long.class);
				}
				if (types.contains(LINK_FWD_BACK)||types.contains(LINK_USR_BACK)){
					linked = jdbc.queryForList(
							"SELECT v.number_value FROM attribute_value v \n" +
							"WHERE v.attribute_code=? \n" +
								"AND v.card_id IN (\n" + populateParameters(linked.size()) + ")",
							params.toArray(),
							Long.class);
				}
			} while (linked.size() > 0);
		}
		if (types.contains(LINK_BACK_FWD)||types.contains(LINK_BACK_BACK)||types.contains(LINK_BACK_USR)){
			ArrayList params = new ArrayList(2 + 1);
			if (link.linkAttrIdU!=null&&link.linkAttrIdU.length()!=0){
				params.add(link.linkAttrIdU);
				params.add(link.linkAttrId);
				params.add(cardId.getId());
				
				linked = jdbc.queryForList(
						"SELECT functionbacklink(c.card_id, ?, ?) as link_card_id FROM card c \n" +
						"WHERE c.card_id = ?",
						params.toArray(),
						Long.class);
			} else {
				params.add(link.linkAttrId);
				params.add(cardId.getId());
				
				linked = jdbc.queryForList(
						"SELECT av.card_id as link_card_id FROM attribute_value av \n" +
						"WHERE av.attribute_code = ? and av.number_value = ?",
						params.toArray(),
						Long.class);
				
			}
			do {
				params = new ArrayList(linked.size() + 1);
				params.add(link.intermedAttrId);
				params.addAll(linked);
				if (types.contains(LINK_BACK_FWD)){
					linked = jdbc.queryForList(
						"SELECT v.number_value FROM attribute_value v \n" +
						"WHERE v.attribute_code=? \n" +
							"AND v.card_id IN (\n" + populateParameters(linked.size()) + ")",
						params.toArray(),
						Long.class);
				}
				if (types.contains(LINK_BACK_BACK)){
					linked = jdbc.queryForList(
							"SELECT v.card_id FROM attribute_value v \n" +
							"WHERE v.attribute_code=? \n" +
								"AND v.number_value IN (\n" + populateParameters(linked.size()) + ")",
							params.toArray(),
							Long.class);
				}
				if (types.contains(LINK_BACK_USR)){
					linked = jdbc.queryForList(
						"SELECT p.card_id FROM attribute_value v \n" +
						"join person p on p.person_id = v.number_value \n" +
						"WHERE v.attribute_code=? \n" +
							"AND v.card_id IN (\n" + populateParameters(linked.size()) + ")",
						params.toArray(),
						Long.class);
				}
				ids.addAll(linked);
			} while (linked.size() > 0);
		}
		long duration = System.currentTimeMillis() - start;
		logger.info(ids.size() + " links collected (" + duration + "ms)");
		return ids;
	}
	/**
	 * Сбор всех дочерних карточек 
	 * @param cardId - входная карточка
	 * @param link - комплексная ссылка
	 * @param isInitial - признак того, что входная карточка - самая начальная 
	 * @return
	 */
	@Deprecated
	private HashSet collectLinksDownOld(ObjectId cardId, ComplexLink link, boolean isInitial) {
		logger.info("Collecting links down by " + link.linkAttrId + "/" + link.intermedAttrId);
		long start = System.currentTimeMillis();
		int step = 0;
		HashSet ids = new HashSet();
		List linked;
		if (isInitial) {
			linked = jdbc.queryForList(
					"SELECT v.number_value FROM attribute_value v \n" +
					"WHERE v.attribute_code=? AND v.card_id=?",
					new Object[] { link.linkAttrId, cardId.getId() },
					Long.class);
		} else {
			linked = new ArrayList();
			linked.add(cardId.getId());
		}
		while (linked.size() > 0) {
			logger.info(linked.size() + " card(s) found on step " + ++step);
			ids.addAll(linked);
			ArrayList params = new ArrayList(linked.size() + 1);
			params.add(link.intermedAttrId);
			params.addAll(linked);
			linked = jdbc.queryForList(
					"SELECT v.number_value FROM attribute_value v \n" +
					"WHERE v.attribute_code=? \n" +
						"AND v.card_id IN (\n" + populateParameters(linked.size()) + ")",
					params.toArray(),
					Long.class);
		}
		long duration = System.currentTimeMillis() - start;
		logger.info(ids.size() + " links collected (" + duration + "ms)");
		return ids;
	}
	
	/**
	 * Сбор всех дочерних карточек (только те, что стоят в самом конце) 
	 * @param cardId - входная карточка
	 * @param link - комплексная ссылка
	 * @param isInitial - признак того, что входная карточка - самая верхняя для дерева связанных 
	 * @return
	 */
	private HashSet collectLinksDown(ObjectId cardId, ComplexLink link, boolean isInitial, HashSet types) {
		logger.info("Collecting links down by " + link);
		long start = System.currentTimeMillis();
		int step = 0;
		HashSet ids = new HashSet();
		List linked = new ArrayList();
		List tempLinked = new ArrayList();
		// если текущая карточка самая верхняя, то ищем первую ссылочную на оcнове связей
		if (isInitial) {
			if (types.contains(LINK_FWD_FWD)||types.contains(LINK_FWD_BACK)/*||types.contains(LINK_FWD_USR)*/){
				linked = jdbc.queryForList(
					"SELECT v.number_value FROM attribute_value v \n" +
					"WHERE v.attribute_code=? AND v.card_id=?",
					new Object[] { link.linkAttrId, cardId.getId() },
					Long.class);
			}
			// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
			/*if (types.contains(LINK_USR_FWD)||types.contains(LINK_USR_USR)||types.contains(LINK_USR_BACK)){
				linked = jdbc.queryForList(
					"SELECT p.card_id FROM attribute_value v \n" +
					"JOIN person p on p.person_id = v.number_value \n" +
					"WHERE v.attribute_code=? AND v.card_id=?",
					new Object[] { link.linkAttrId, cardId.getId() },
					Long.class);
			}*/
			if (types.contains(LINK_BACK_FWD)){
				linked = jdbc.queryForList(
					"SELECT v.card_id FROM attribute_value v \n" +
					"WHERE v.attribute_code=? AND v.number_value=?",
					new Object[] { link.intermedAttrId, cardId.getId() },
					Long.class);
			}
			if (types.contains(LINK_BACK_BACK)){
				linked = jdbc.queryForList(
					"SELECT v.number_value FROM attribute_value v \n" +
					"WHERE v.attribute_code=? AND v.card_id=?",
					new Object[] { link.intermedAttrId, cardId.getId() },
					Long.class);
			}
		} else {	// иначе текущая карточка и есть ссылочная (для промежуточных карточек)
			linked = new ArrayList();
			linked.add(cardId.getId());
		}
		// если используется схема, когда атрибут связи - беклинк 
		// надо перед поиском связанных карточек найти сначала родителя по LINK/UPLINK атрибута связи для самой нижней карточки,
		// а потом первые связанные с ней по LINK-части атрибута связи, от них и будем плясать в поиске дочерних карточек 
		if (types.contains(LINK_BACK_FWD)){
			while (linked.size() > 0) {
				tempLinked  = linked;
				logger.info(linked.size() + " intermediate card(s) found on step " + ++step);
				ArrayList params;
				params = new ArrayList(linked.size() + 1);
				params.add(link.intermedAttrId);
				params.addAll(linked);
				linked = jdbc.queryForList(
						"SELECT v.card_id FROM attribute_value v \n" +
						"WHERE v.attribute_code=? AND v.number_value in (\n" + populateParameters(linked.size()) + ")",
						params.toArray(),
						Long.class);
			}
			ArrayList params;
			params = new ArrayList(tempLinked.size() + 1);
			params.add(link.linkAttrId);
			params.addAll(tempLinked);
			linked = jdbc.queryForList(
					"SELECT v.number_value FROM attribute_value v \n" +
					"WHERE v.attribute_code=? \n" +
						"AND v.card_id IN (\n" + populateParameters(tempLinked.size()) + ")",
					params.toArray(),
					Long.class);
			ids.addAll(linked);
			
		}
		// если используется схема, когда атрибут связи - беклинк, и атрибут промежуточной связи - беклинк, 
		// надо перед поиском связанных карточек найти сначала родителя по LINK/UPLINK атрибута связи для самой нижней карточки,
		// а потом первые связанные с ней по LINK-части атрибута связи, от них и будем плясать в поиске дочерних карточек 
		if (types.contains(LINK_BACK_BACK)){
			while (linked.size() > 0) {
				tempLinked  = linked;
				logger.info(linked.size() + " intermediate card(s) found on step " + ++step);
				ArrayList params;
				params = new ArrayList(linked.size() + 1);
				params.add(link.intermedAttrId);
				params.addAll(linked);
				linked = jdbc.queryForList(
						"SELECT v.number_value FROM attribute_value v \n" +
						"WHERE v.attribute_code=? AND v.card_id IN (\n" + populateParameters(linked.size()) + ")",
						params.toArray(),
						Long.class);
			}
			ArrayList params;
			params = new ArrayList(tempLinked.size() + 1);
			params.add(link.linkAttrId);
			params.addAll(tempLinked);
			linked = jdbc.queryForList(
					"SELECT v.number_value FROM attribute_value v \n" +
					"WHERE v.attribute_code=? \n" +
						"AND v.card_id IN (\n" + populateParameters(tempLinked.size()) + ")",
					params.toArray(),
					Long.class);
			ids.addAll(linked);
			
		}

		// далее по атрибутам связи или атрибутам промежуточной связи находим все дерево детей
		while (linked.size() > 0) {
			logger.info(linked.size() + " card(s) found on step " + ++step);
			ArrayList params;
			if (types.contains(LINK_FWD_FWD)/*||types.contains(LINK_USR_FWD)*/){
				params = new ArrayList(linked.size() + 1);
				params.add(link.intermedAttrId);
				params.addAll(linked);
				linked = jdbc.queryForList(
						"SELECT v.number_value FROM attribute_value v \n" +
						"WHERE v.attribute_code=? \n" +
							"AND v.card_id IN (\n" + populateParameters(linked.size()) + ")",
						params.toArray(),
						Long.class);
				ids.addAll(linked);
			}
			// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующие строчки необходимо раскомментарить
			/*if (types.contains(LINK_FWD_USR)||types.contains(LINK_USR_USR)){
				params = new ArrayList(linked.size() + 1);
				params.add(link.intermedAttrId);
				params.addAll(linked);
				linked = jdbc.queryForList(
						"SELECT p.card_id FROM attribute_value v \n" +
						"JOIN person p on p.person_id = v.number_value \n" +
						"WHERE v.attribute_code=? \n" +
							"AND v.card_id IN (\n" + populateParameters(linked.size()) + ")",
						params.toArray(),
						Long.class);
				ids.addAll(linked);
			}*/
			if (types.contains(LINK_FWD_BACK)/*||types.contains(LINK_USR_BACK)*/){
				params = new ArrayList(linked.size() + 1);
				params.add(link.intermedAttrId);
				params.addAll(linked);
				linked = jdbc.queryForList(
						"SELECT v.card_id FROM attribute_value v \n" +
						"WHERE v.attribute_code=? \n" +
							"AND v.number_value IN (\n" + populateParameters(linked.size()) + ")",
						params.toArray(),
						Long.class);
				ids.addAll(linked);
			}
			if (types.contains(LINK_BACK_FWD)||types.contains(LINK_BACK_BACK)/*||types.contains(LINK_BACK_USR)*/){
				params = new ArrayList(linked.size() + 1);
				params.add(link.linkAttrIdU);
				params.addAll(linked);
				linked = jdbc.queryForList(
						"SELECT v.number_value FROM attribute_value v \n" +
						"WHERE v.attribute_code=? \n" +
							"AND v.card_id IN (\n" + populateParameters(linked.size()) + ")",
						params.toArray(),
						Long.class);
				ids.addAll(linked);
			}
		}
		long duration = System.currentTimeMillis() - start;
		logger.info(ids.size() + " links collected (" + duration + "ms)");
		return ids;
	}

	private String populateParameters(int num) {
		StringBuffer buf = new StringBuffer(num * 2);
		if (num < 1)
			return "NULL";
		buf.append("?");
		for (int i = 1; i < num; i++)
			buf.append(",?");
		return buf.toString();
	}
}