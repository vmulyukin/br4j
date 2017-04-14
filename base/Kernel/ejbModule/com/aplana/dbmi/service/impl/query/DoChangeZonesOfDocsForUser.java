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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.action.ChangeZonesOfDocsForUser;

/**
 * @author ynikitin
 *
 */
public class DoChangeZonesOfDocsForUser extends ActionQueryBase implements WriteQuery{
	private static final long serialVersionUID = 1L;
	protected final Log logger = LogFactory.getLog(getClass());
	private List<ObjectId> lockedCards = new ArrayList<ObjectId>();
	
	private static final String DELETE_ZONE_DOW = "delete from attribute_value where card_id = ? and attribute_code = 'JBR_ZONE_DOW'";
	private static final String INSERT_ZONE_DOW = "insert into attribute_value(card_id, attribute_code, number_value, template_id)\n";
	private static final String SELECT_ZONE_DOW_FOR_INCOMING = 
		"select \n" + 
		"	av1.card_id, \n" +
		"	'JBR_ZONE_DOW' as attribute_code, \n" +
		"	av2.number_value, \n" +
		"	av1.template_id \n" +
		"from  \n" +
		"	attribute_value av1 \n" +
		"	join person p on p.person_id = av1.number_value \n" +
		"	join attribute_value av2 on av2.attribute_code = 'JBR_ZONE_DOW' and av2.card_id = p.card_id \n" +
		"	left JOIN attribute_value av3 on av3.card_id = av1.card_id and av3.attribute_code = 'REPLICATED_DOC_TYPE' \n" +
		"where \n" +
		"	(av1.attribute_code in ('JBR_INFD_RECEIVER', 'AUTHOR') \n" +
		"	and av1.template_id in (224)) \n" +
		"	and av1.card_id = ? \n" +
		"	and (av3.value_id is NULL or av3.value_id = 3550)";
	private static final String SELECT_ZONE_DOW_FOR_OUTCOMING = 
		"select \n" + 
		"	av1.card_id, \n" +
		"	'JBR_ZONE_DOW' as attribute_code, \n" +
		"	av2.number_value, \n" +
		"	av1.template_id \n" +
		"from  \n" +
		"	attribute_value av1 \n" +
		"	join person p on p.person_id = av1.number_value \n" +
		"	join attribute_value av2 on av2.attribute_code = 'JBR_ZONE_DOW' and av2.card_id = p.card_id \n" +
		"where \n" +
		"	(av1.attribute_code in ('JBR_INFD_SIGNATORY', 'AUTHOR', 'JBR_INFD_EXECUTOR') \n" +
		"	and av1.template_id in (364)) \n" +
		"	and av1.card_id = ? \n";
	private static final String SELECT_ZONE_DOW_FOR_ORD = 
		"select \n" + 
		"	av1.card_id, \n" +
		"	'JBR_ZONE_DOW' as attribute_code, \n" +
		"	av2.number_value, \n" +
		"	av1.template_id \n" +
		"from  \n" +
		"	attribute_value av1 \n" +
		"	join person p on p.person_id = av1.number_value \n" +
		"	join attribute_value av2 on av2.attribute_code = 'JBR_ZONE_DOW' and av2.card_id = p.card_id \n" +
		"where \n" +
		"	(av1.attribute_code in ('JBR_INFD_SIGNATORY', 'AUTHOR', 'JBR_INFD_EXECUTOR') \n" +
		"	and av1.template_id in (764)) \n" +
		"	and av1.card_id = ? \n";
	private static final String SELECT_ZONE_DOW_FOR_INNER = 
		"select \n" + 
		"	av1.card_id, \n" +
		"	'JBR_ZONE_DOW' as attribute_code, \n" +
		"	av2.number_value, \n" +
		"	av1.template_id \n" +
		"from  \n" +
		"	attribute_value av1 \n" +
		"	join person p on p.person_id = av1.number_value \n" +
		"	join attribute_value av2 on av2.attribute_code = 'JBR_ZONE_DOW' and av2.card_id = p.card_id \n" +
		"	join card c on c.card_id = av1.card_id \n" +
		"where \n" +
		"	(av1.attribute_code in ('JBR_INFD_SIGNATORY', 'AUTHOR', 'JBR_INFD_RECEIVER', 'JBR_INFD_EXECUTOR') \n" +
		"	and av1.template_id in (784)) \n" +
		"	and av1.card_id = ? \n" +
		"	and c.status_id in (101, 102, 103, 206, 48909, 104) \n" +
		"union all \n" +
		"select \n" + 
		"	av1.card_id, \n" +
		"	'JBR_ZONE_DOW' as attribute_code, \n" +
		"	av2.number_value, \n" +
		"	av1.template_id \n" +
		"from  \n" +
		"	attribute_value av1 \n" +
		"	join person p on p.person_id = av1.number_value \n" +
		"	join attribute_value av2 on av2.attribute_code = 'JBR_ZONE_DOW' and av2.card_id = p.card_id \n" +
		"	join card c on c.card_id = av1.card_id \n" +
		"where \n" +
		"	(av1.attribute_code in ('JBR_INFD_SIGNATORY', 'AUTHOR', 'JBR_INFD_EXECUTOR') \n" +
		"	and av1.template_id in (784)) \n" +
		"	and av1.card_id = ? \n" +
		"	and not(c.status_id in (101, 102, 103, 206, 48909, 104))";
	private static final String SELECT_ZONE_DOW_FOR_OG = 
		"select \n" + 
		"	av1.card_id, \n" +
		"	'JBR_ZONE_DOW' as attribute_code, \n" +
		"	av2.number_value, \n" +
		"	av1.template_id \n" +
		"from  \n" +
		"	attribute_value av1 \n" +
		"	join person p on p.person_id = av1.number_value \n" +
		"	join attribute_value av2 on av2.attribute_code = 'JBR_ZONE_DOW' and av2.card_id = p.card_id \n" +
		"	left JOIN attribute_value av3 on av3.card_id = av1.card_id and av3.attribute_code = 'REPLICATED_DOC_TYPE' \n" +
		"where \n" +
		"	(av1.attribute_code in ('AUTHOR') \n" +
		"	and av1.template_id in (864)) \n" +
		"	and av1.card_id = ? \n"+
		"	and (av3.value_id is NULL or av3.value_id = 3550) \n"+
		"union all \n" +
		"select \n" + 
		"	av1.card_id, \n" +
		"	'JBR_ZONE_DOW' as attribute_code, \n" +
		"	av2.number_value, \n" +
		"	av1.template_id \n" +
		"from  \n" +
		"	attribute_value av1 \n" +
		"	join attribute_value av2 on av2.attribute_code = 'JBR_ZONE_DOW' and av2.card_id = av1.number_value \n" +
		"	left JOIN attribute_value av3 on av3.card_id = av1.card_id and av3.attribute_code = 'REPLICATED_DOC_TYPE' \n" +
		"where \n" +
		"	(av1.attribute_code in ('JBR_RECEIVER') \n" +
		"	and av1.template_id in (864)) \n" +
		"	and av1.card_id = ? \n" +
		"	and (av3.value_id is NULL or av3.value_id = 3550)";

	private static final String SELECT_ZONE_DOW_FOR_NPA = 
		"select \n" + 
		"	av1.card_id, \n" +
		"	'JBR_ZONE_DOW' as attribute_code, \n" +
		"	av2.number_value, \n" +
		"	av1.template_id \n" +
		"from  \n" +
		"	attribute_value av1 \n" +
		"	join person p on p.person_id = av1.number_value \n" +
		"	join attribute_value av2 on av2.attribute_code = 'JBR_ZONE_DOW' and av2.card_id = p.card_id \n" +
		"where \n" +
		"	(av1.attribute_code in ('JBR_INFD_SIGNATORY', 'AUTHOR', 'JBR_INFD_EXECUTOR') \n" +
		"	and av1.template_id in (1226)) \n" +
		"	and av1.card_id = ? \n";
	private static final String SELECT_ZONE_DOW_FOR_RASSM = 
			"select distinct \n" + 
			"	av1.card_id, \n" +
			"	'JBR_ZONE_DOW' as attribute_code, \n" +
			"	av2.number_value, \n" +
			"	av1.template_id \n" +
			"from  \n" +
			"	attribute_value av1 \n" +
			"	join person p on p.person_id = av1.number_value \n" +
			"	join attribute_value av2 on av2.attribute_code = 'JBR_ZONE_DOW' and av2.card_id = p.card_id \n" +
			"where \n" +
			"	(av1.attribute_code in ('JBR_RASSM_PERSON', 'AUTHOR') \n" +
			"	and av1.template_id in (504)) \n" +
			"	and av1.card_id = ? \n";
	private static final String SELECT_ZONE_DOW_FOR_RESOLUTION = 
		"select \n" + 
		"	av1.card_id, \n" +
		"	'JBR_ZONE_DOW' as attribute_code, \n" +
		"	av2.number_value, \n" +
		"	av1.template_id \n" +
		"from  \n" +
		"	attribute_value av1 \n" +
		"	join person p on p.person_id = av1.number_value \n" +
		"	join attribute_value av2 on av2.attribute_code = 'JBR_ZONE_DOW' and av2.card_id = p.card_id \n" +
		"where \n" +
		"	(av1.attribute_code in ('JBR_INFD_EXEC_LINK', 'ADMIN_255974', 'JBR_TCON_INSPECTOR') \n" +
		"	and av1.template_id in (324)) \n" +
		"	and av1.card_id = ? \n";
	private static final String SELECT_ZONE_DOW_FOR_VISA = 
		"select \n" + 
		"	av1.card_id, \n" +
		"	'JBR_ZONE_DOW' as attribute_code, \n" +
		"	av2.number_value, \n" +
		"	av1.template_id \n" +
		"from  \n" +
		"	attribute_value av1 \n" +
		"	join person p on p.person_id = av1.number_value \n" +
		"	join attribute_value av2 on av2.attribute_code = 'JBR_ZONE_DOW' and av2.card_id = p.card_id \n" +
		"where \n" +
		"	(av1.attribute_code in ('JBR_VISA_RESPONSIBLE') \n" +
		"	and av1.template_id in (348)) \n" +
		"	and av1.card_id = ? \n";
	private static final String SELECT_ZONE_DOW_FOR_SIGN = 
		"select \n" + 
		"	av1.card_id, \n" +
		"	'JBR_ZONE_DOW' as attribute_code, \n" +
		"	av2.number_value, \n" +
		"	av1.template_id \n" +
		"from  \n" +
		"	attribute_value av1 \n" +
		"	join person p on p.person_id = av1.number_value \n" +
		"	join attribute_value av2 on av2.attribute_code = 'JBR_ZONE_DOW' and av2.card_id = p.card_id \n" +
		"where \n" +
		"	(av1.attribute_code in ('JBR_SIGN_RESPONSIBLE') \n" +
		"	and av1.template_id in (365)) \n" +
		"	and av1.card_id = ? \n";
	private static final String SELECT_ZONE_DOW_FOR_EXAM = 
		"select \n" + 
		"	av1.card_id, \n" +
		"	'JBR_ZONE_DOW' as attribute_code, \n" +
		"	av2.number_value, \n" +
		"	av1.template_id \n" +
		"from  \n" +
		"	attribute_value av1 \n" +
		"	join person p on p.person_id = av1.number_value \n" +
		"	join attribute_value av2 on av2.attribute_code = 'JBR_ZONE_DOW' and av2.card_id = p.card_id \n" +
		"where \n" +
		"	(av1.attribute_code in ('JBR_FOR_INFORMATION') \n" +
		"	and av1.template_id in (524)) \n" +
		"	and av1.card_id = ? \n";

	// запросы на извлечение карточек, удовлетворяющих профильным правилам со сверяемым атрибутом Доступ к Зонам ДОУ 
	public final static String SELECT_PROFILE_ACCESS_CARD_THIS =
		"SELECT \n" +
		"	c.card_id, \n" +
		"	c.template_id, \n" + 
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
		"		AND pr.profile_attr_code = 'JBR_ZONES_DOW_ACCESS'\n" +
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
		"		and (tv.number_value=pv.number_value \n" +
		"			OR tv.number_value in (select avh.number_value from attribute_value_hist avh where avh.card_id = p.card_id and avh.version_id = :version_id and avh.attribute_code = 'JBR_ZONES_DOW_ACCESS')) \n" +
		"WHERE \n" +
		"	p.card_id=pv.card_id \n" +
		"	AND p.card_id = :card_id";

	// Access by linked card's attribute (direct link)
	public final static String SELECT_PROFILE_ACCESS_CARD_LINKED =
		"SELECT \n" +
		"	c.card_id, \n" +
		"	c.template_id, \n" +
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
		"		AND pr.profile_attr_code = 'JBR_ZONES_DOW_ACCESS'\n" +
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
		"       AND (tv.number_value=pv.number_value \n" +
		"			OR tv.number_value in (select avh.number_value from attribute_value_hist avh where avh.card_id = p.card_id and avh.version_id = :version_id and avh.attribute_code = 'JBR_ZONES_DOW_ACCESS')) \n" +
		"WHERE \n" +
		"	p.card_id=pv.card_id \n" +
		"	AND pr.intermed_attr_code is NULL \n" +
		"	AND p.card_id = :card_id";

	// Access by linked user card's attribute (direct user link)
	public final static String SELECT_PROFILE_ACCESS_CARD_USER_LINKED =
		"SELECT \n" +
		"	c.card_id, \n" +
		"	c.template_id, \n" +
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
		"		AND pr.profile_attr_code = 'JBR_ZONES_DOW_ACCESS'\n" +
		"	LEFT JOIN person_role sr \n" +
		"		ON pr.role_code=sr.role_code \n" +
		"	JOIN person p \n" +
		"		ON (pr.role_code IS NULL OR p.person_id=sr.person_id) \n" +
		"	JOIN attribute_value lv \n" +
		"		ON c.card_id=lv.card_id \n" +
		"		AND pr.link_attr_code=lv.attribute_code \n" +
		"	JOIN person pL \n" +
		"		ON pL.person_id=lv.number_value \n" +
		"	JOIN card lc \n" +
		"		ON lc.card_id = pL.card_id \n" +
		"		AND (pr.linked_status_id=lc.status_id OR pr.linked_status_id IS NULL) \n" +
		"	JOIN attribute_value pv \n" +
		"		ON pr.profile_attr_code=pv.attribute_code \n" +
		"	JOIN attribute_value tv \n" +
		"		ON lv.number_value=tv.card_id \n" +
		"		AND pr.target_attr_code=tv.attribute_code \n" +
		"       AND (tv.number_value=pv.number_value \n" +
		"			OR tv.number_value in (select avh.number_value from attribute_value_hist avh where avh.card_id = p.card_id and avh.version_id = :version_id and avh.attribute_code = 'JBR_ZONES_DOW_ACCESS')) \n" +
		"WHERE \n" +
		"	p.card_id=pv.card_id \n" +
		"	AND pr.intermed_attr_code is NULL \n" +
		"	AND p.card_id = :card_id";
	
	// Access by linked card's attribute (reverse link)
	public final static String SELECT_PROFILE_ACCESS_CARD_BACKLINK =
		"SELECT \n" +
		"	c.card_id, \n" +
		"	c.template_id, \n" +
		"	c.rule_id, \n" +
		"	p.person_id, \n" +
		"	tv.attr_value_id \n" +
		"FROM \n" +
		"	( \n" +
		"		SELECT \n" + 
		"			c.card_id, \n" +
		"			c.template_id, \n" + 
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
		"					ON 	(c.template_id=r.template_id OR r.template_id IS NULL) \n" + 
		"					AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +
		"			JOIN profile_access_rule pr \n" +
		"					ON 	r.rule_id=pr.rule_id \n" + 
		"					AND pr.profile_attr_code = 'JBR_ZONES_DOW_ACCESS'\n" +
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
		"			c.template_id, \n" + 
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
		"					ON 	(c.template_id=r.template_id OR r.template_id IS NULL) \n" + 
		"					AND (c.status_id=r.status_id OR r.status_id IS NULL) \n" +
		"			JOIN profile_access_rule pr \n" +
		"					ON 	r.rule_id=pr.rule_id \n" + 
		"					AND pr.profile_attr_code = 'JBR_ZONES_DOW_ACCESS'\n" +
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
		"	JOIN person p \n" +
		"		ON p.card_id=pv.card_id \n" +
		"	JOIN attribute_value tv \n" +
		"		ON lc.card_id=tv.card_id \n" +
		"		AND c.target_attr_code=tv.attribute_code \n" +
		"       AND (tv.number_value=pv.number_value \n" +
		"			OR tv.number_value in (select avh.number_value from attribute_value_hist avh where avh.card_id = p.card_id and avh.version_id = :version_id and avh.attribute_code = 'JBR_ZONES_DOW_ACCESS')) \n" +
		"WHERE \n" +
		"	(c.role_code IS NULL OR p.person_id=c.person_id) \n" +
		"	AND c.intermed_attr_code is NULL \n" +
		"	AND p.card_id = :card_id";
	
	// запрос на извлечение всех карточек, для которых применяются профильные правила с атрибутом профиля Доступ к Зонам ДОУ, причем учитываются только те документы, для которых сверка идёт с профилем входного пользователя
	// при сравнении атрибута профиля и атрибута карточки учитывается и текущее значение в профиле и значение в определенной версии
	private static final String SELECT_CARDS_FOR_ZONE_ACCESS = 
		"select distinct card_id, template_id from (\n" +
		SELECT_PROFILE_ACCESS_CARD_THIS +
		"\n union \n" +
		SELECT_PROFILE_ACCESS_CARD_LINKED+
		"\n union \n" +
		SELECT_PROFILE_ACCESS_CARD_USER_LINKED+
		"\n union \n" +
		SELECT_PROFILE_ACCESS_CARD_BACKLINK+
		") as c1";
	
	@Override
	public Object processQuery() throws DataException {
		ChangeZonesOfDocsForUser action = (ChangeZonesOfDocsForUser)this.getAction();
		ObjectId cardId = action.getCardId();
		boolean isDepartment = action.isDepartment();
		Long versionId = action.getVersionId();
		boolean isZoneAccess = action.isZoneAccess();
		// получить список карточек, где входные пользователи являются Авторами, Подписантами или Адресатами (в разных шаблонах по-разному)
		final NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(getJdbcTemplate());
		final MapSqlParameterSource args = new MapSqlParameterSource();
		String sql = null;
		if (isZoneAccess){
			args.addValue("card_id", cardId.getId(), Types.NUMERIC);
			if (versionId==null){
				versionId = getLastCardVersion((Long)cardId.getId());
			}
			if (versionId==null){
				versionId = new Long(0);
			}
			args.addValue("version_id", versionId, Types.NUMERIC);
			sql = SELECT_CARDS_FOR_ZONE_ACCESS;
		} else {
			sql = 
		//		"select distinct c1.* , av.attribute_code as ZoneCode, av.number_value as ZoneValue \n" +
				"select distinct c1.card_id, c1.template_id \n" +
				"from \n" +
				"	(select \n" + 
				"		av1.card_id, \n" +
				"		av1.template_id, \n" +
				"		av1.attribute_code as UserCode, \n" +
				"		av1.number_value as UserValue \n" +
				"	from  \n" +
				"		attribute_value av1 \n" +
				"		join card c on c.card_id = av1.card_id \n" +
				"		left JOIN attribute_value av3 on av3.card_id = av1.card_id and av3.attribute_code = 'REPLICATED_DOC_TYPE' \n" +
				"	where \n" +
				"		(av1.attribute_code in ('JBR_INFD_RECEIVER', 'AUTHOR') \n" +
				"		and av1.template_id in (224) \n" +
				"		and (av3.value_id is NULL or av3.value_id = 3550)) \n" +
				// Для внутренних набор атрибутов зависит от статуса карточки
				"		or \n" +
				"		(av1.attribute_code in ('JBR_INFD_SIGNATORY', 'AUTHOR', 'JBR_INFD_RECEIVER', 'JBR_INFD_EXECUTOR') \n" +
				"		and av1.template_id in (784) \n" +
				"		and c.status_id in (101, 102, 103, 206, 48909, 104)) \n" +
				"		or \n" +
				"		(av1.attribute_code in ('JBR_INFD_SIGNATORY', 'AUTHOR', 'JBR_INFD_EXECUTOR') \n" +
				"		and av1.template_id in (784) \n" +
				"		and not(c.status_id in (101, 102, 103, 206, 48909, 104))) \n" +
				"		or \n" +
				"		(av1.attribute_code in ('JBR_INFD_SIGNATORY', 'AUTHOR', 'JBR_INFD_EXECUTOR') \n" +
				"		and av1.template_id in (364)) \n" +
				"		or \n" +
				"		(av1.attribute_code in ('JBR_INFD_SIGNATORY', 'AUTHOR', 'JBR_INFD_EXECUTOR') \n" +
				"		and av1.template_id in (764)) \n" +
				"		or \n" +
				"		(av1.attribute_code in ('JBR_INFD_SIGNATORY', 'AUTHOR', 'JBR_INFD_EXECUTOR') \n" +
				"		and av1.template_id in (1226)) \n" +
				"		or \n" +
				"		(av1.attribute_code in ('AUTHOR') \n" +
				"		and av1.template_id in (864) \n" +
				"		and (av3.value_id is NULL or av3.value_id = 3550)) \n" +
				"		or \n" +
				"		(av1.attribute_code in ('AUTHOR', 'JBR_RASSM_PERSON') \n" +
				"		and av1.template_id in (504)) \n" +
				"		or \n" +
				"		(av1.attribute_code in ('JBR_INFD_EXEC_LINK', 'ADMIN_255974', 'JBR_TCON_INSPECTOR') \n" +
				"		and av1.template_id in (324) \n" +
				"		and c.status_id in (103, 206)) \n" +
				"		or \n" +
				"		(av1.attribute_code in ('JBR_VISA_RESPONSIBLE') \n" +
				"		and av1.template_id in (348) \n" +
				"		and c.status_id in (107, 201, 41466, 73992, 6092498, 6833780, 202)) \n" +
				"		or \n" +
				"		(av1.attribute_code in ('JBR_SIGN_RESPONSIBLE') \n" +
				"		and av1.template_id in (365) \n" +
				"		and c.status_id in (41466, 108, 73992, 204, 477934, 205)) \n" +
				"		or \n" +
				"		(av1.attribute_code in ('JBR_FOR_INFORMATION') \n" +
				"		and av1.template_id in (524) \n" +
				"		and c.status_id in (52086, 67424, 73992, 67425)) \n" +
				"	union all \n" +
				"	select \n" +
				"		av1.card_id, \n" +
				"		av1.template_id, \n" +
				"		av1.attribute_code as UserCode, \n" +
				"		p1.person_id as UserValue \n" +
				"	from \n" +
				"		attribute_value av1 \n" +
				"		join person p1 on p1.card_id = av1.number_value \n" +
				"		join card c on c.card_id = av1.card_id \n" +
				"		left JOIN attribute_value av3 on av3.card_id = av1.card_id and av3.attribute_code = 'REPLICATED_DOC_TYPE' \n" +
				"	where \n" +
				"		(av1.attribute_code in ('JBR_RECEIVER') \n" +
				"		and av1.template_id in (864) \n" +
				"		and (av3.value_id is NULL or av3.value_id = 3550)) \n" +
				"	) as c1 \n" +
				"	join attribute_value av on av.attribute_code = 'JBR_ZONE_DOW' and av.card_id = c1.card_id \n" +
				"where c1.UserValue in "+(!isDepartment?
											"(select person_id from person p left join attribute_value av on p.card_id = av.card_id and av.attribute_code = 'JBR_ZONE_DOW_RECALC' where p.card_id = :card_id and coalesce(av.value_id, 1433) = 1432) \n": 
											"(select person_id from person p join attribute_value avPerson on avPerson.attribute_code = 'JBR_PERS_DEPT_LINK' and avPerson.card_id = p.card_id and avPerson.number_value = :card_id left join attribute_value av on p.card_id = av.card_id and av.attribute_code = 'JBR_ZONE_DOW_RECALC' where coalesce(av.value_id, 1433) = 1432) \n") +
				"order by c1.template_id, c1.card_id";
			args.addValue("card_id", cardId.getId(), Types.NUMERIC);
		}
		List<Card> cList = namedParameterJdbcTemplate.query(sql, 
				args,
				new RowMapper(){
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						ObjectId id = new ObjectId(Card.class, rs.getInt(1));
						ObjectId template = new ObjectId(Template.class, rs.getInt(2));
						
						Card card = new Card();
						card.setId(id);
						card.setTemplate(template);
						return card;
					}
		});
		/* обрабатываем каждый документ по очереди
		 * 1. Блокируем его
		 * 2. Очищаем права
		 * 3. Обновляем атрибуты
		 * 4. Помещаем карточку в список на пересчёт
		 * 5. Разблокируем карточку
		 */
		try {
			for (Card document : cList){
				lockCard(document.getId());
				cleanAccessList(document.getId());
				// если только пересчёт прав, то копировать Зоны не надо
				if (!isZoneAccess){
					updateZoneDowForCard(document);
				}
				// поскольку все карточки так или иначе независимы друг от друга, то пересчёт прав на каждую делаем сразу после обновления её атрибутов        
				this.getPrimaryQuery().putCardIdInRecalculateAL(document.getId());					
				this.getPrimaryQuery().recalculateAccessList(document.getId());	
			}
		} finally {
			for(ObjectId cId : lockedCards){
				unlockCard(cId);
			}
		}
		
		return new Long(cList.size());
	}
	
	private void lockCard(ObjectId docId) throws DataException{
		LockObject lockAction = new LockObject(docId);
		ActionQueryBase aqb = getQueryFactory().getActionQuery(lockAction);
		aqb.setAccessChecker(null);
		aqb.setAction(lockAction);
		aqb.setUser(getSystemUser());
		aqb.setSessionId(getSessionId());
		getDatabase().executeQuery(getSystemUser(), aqb);
		lockedCards.add(docId);
	}

	private void unlockCard(ObjectId docId) throws DataException{
		UnlockObject lockAction = new UnlockObject(docId);
		ActionQueryBase aqb = getQueryFactory().getActionQuery(lockAction);
		aqb.setAccessChecker(null);
		aqb.setAction(lockAction);
		aqb.setUser(getSystemUser());
		aqb.setSessionId(getSessionId());
		getDatabase().executeQuery(getSystemUser(), aqb);
		//lockedCards.remove(docId);
	}

	/**
	 * Метод обновления Зоны ДОУ в документе, в котором входной пользователь (или один из пользователей входного департамента) является ключевым
	 * @param card - карточка (её id и шаблон для определения алгоритма пересчёта)
	 */
	private void updateZoneDowForCard(Card card){
		ObjectId cardId = card.getId();
		ObjectId templateId = card.getTemplate();
		if (cardId == null || templateId ==null){
			logger.warn("Id or template for card "+card+" is null, updating ZONE_DOW is break");
			return;
		}
		// чистим Зоны ДОУ перед вставкой корректных Зон ДОУ
		long deleteCount = getJdbcTemplate().update(DELETE_ZONE_DOW, new Object[]{card.getId().getId()},
				new int[] { Types.NUMERIC });
		logger.info("Delete "+ deleteCount + " rows of attribute 'JBR_ZONE_DOW' in card " + card.getId().getId().toString()+ " with template " + templateId.getId());
		long insertCount = 0;
		// теперь выполняем копирование Зон ДОУ из карточек пользователей - для каждого шаблона из рахных пользователей
		if (templateId.equals(new ObjectId(Template.class, 224))){
			insertCount = getJdbcTemplate().update(INSERT_ZONE_DOW + SELECT_ZONE_DOW_FOR_INCOMING, new Object[]{card.getId().getId()},
					new int[] { Types.NUMERIC });
		} else if (templateId.equals(new ObjectId(Template.class, 364))){
			insertCount = getJdbcTemplate().update(INSERT_ZONE_DOW + SELECT_ZONE_DOW_FOR_OUTCOMING, new Object[]{card.getId().getId()},
					new int[] { Types.NUMERIC });
		} else if (templateId.equals(new ObjectId(Template.class, 764))){
			insertCount = getJdbcTemplate().update(INSERT_ZONE_DOW + SELECT_ZONE_DOW_FOR_ORD, new Object[]{card.getId().getId()},
					new int[] { Types.NUMERIC });
		} else if (templateId.equals(new ObjectId(Template.class, 784))){
			insertCount = getJdbcTemplate().update(INSERT_ZONE_DOW + SELECT_ZONE_DOW_FOR_INNER, new Object[]{card.getId().getId(), card.getId().getId()},
					new int[] { Types.NUMERIC, Types.NUMERIC });
		} else if (templateId.equals(new ObjectId(Template.class, 864))){
			insertCount = getJdbcTemplate().update(INSERT_ZONE_DOW + SELECT_ZONE_DOW_FOR_OG, new Object[]{card.getId().getId(), card.getId().getId()},
					new int[] { Types.NUMERIC, Types.NUMERIC });
		} else if (templateId.equals(new ObjectId(Template.class, 1226))){
			insertCount = getJdbcTemplate().update(INSERT_ZONE_DOW + SELECT_ZONE_DOW_FOR_NPA, new Object[]{card.getId().getId()},
					new int[] { Types.NUMERIC });
		} else if (templateId.equals(new ObjectId(Template.class, 504))){
			insertCount = getJdbcTemplate().update(INSERT_ZONE_DOW + SELECT_ZONE_DOW_FOR_RASSM, new Object[]{card.getId().getId()},
					new int[] { Types.NUMERIC });
		} else if (templateId.equals(new ObjectId(Template.class, 324))){
			insertCount = getJdbcTemplate().update(INSERT_ZONE_DOW + SELECT_ZONE_DOW_FOR_RESOLUTION, new Object[]{card.getId().getId()},
					new int[] { Types.NUMERIC });
		} else if (templateId.equals(new ObjectId(Template.class, 348))){
			insertCount = getJdbcTemplate().update(INSERT_ZONE_DOW + SELECT_ZONE_DOW_FOR_VISA, new Object[]{card.getId().getId()},
					new int[] { Types.NUMERIC });
		} else if (templateId.equals(new ObjectId(Template.class, 365))){
			insertCount = getJdbcTemplate().update(INSERT_ZONE_DOW + SELECT_ZONE_DOW_FOR_SIGN, new Object[]{card.getId().getId()},
					new int[] { Types.NUMERIC });
		} else if (templateId.equals(new ObjectId(Template.class, 524))){
			insertCount = getJdbcTemplate().update(INSERT_ZONE_DOW + SELECT_ZONE_DOW_FOR_EXAM, new Object[]{card.getId().getId()},
					new int[] { Types.NUMERIC });
		}
		logger.info("Insert "+ insertCount + " rows for attribute 'JBR_ZONE_DOW' in card " + card.getId().getId()+ " with template " + templateId.getId());
		return;
	}
	
	private Long getLastCardVersion(Long cardId){
		final String sql = 
			"select version_id from card_version cv \n" +
			"where cv.card_id = ?"; 
		Long versionId = null;
		try{
			versionId = getJdbcTemplate().queryForLong(sql, new Object[]{cardId},
					new int[] { Types.NUMERIC });
		} catch (Exception e){
			versionId = null;
		}
		return versionId;
	}
}
