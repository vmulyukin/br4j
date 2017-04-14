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

import java.text.MessageFormat;
import java.util.List;

import com.aplana.dbmi.action.ParentReportAction;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
/*
 * Query ���������� id ������� �� ���������� ��� ������������ ���������
 * (�� ���� �������� id �������� ���������)
 */
public class DoParentReportAction extends ActionQueryBase {

	private static final long serialVersionUID = 1L;

	private static final ObjectId regularResTemplate = ObjectId.predefined(Template.class, "jbr.resolution");
	private static final ObjectId indepResTemplate = ObjectId.predefined(Template.class, "jbr.independent.resolution");
	
	@SuppressWarnings("unchecked")
	@Override
	public Object processQuery() throws DataException {
		ParentReportAction parentReportAction = (ParentReportAction) getAction();
		
		String ids = ObjectIdUtils.numericIdsToCommaDelimitedString(parentReportAction.getChildResolutionCardIds());

		List<Long> reportIds = getJdbcTemplate().queryForList(MessageFormat.format(getResultSql(), ids), Long.class);

		return reportIds;
	}
	
	private String getResultSql(){
		StringBuilder sqlBuf = new StringBuilder();
		sqlBuf.append(getParentReportSql("JBR_RIMP_PARASSIG", regularResTemplate)); // Regular parent resolution reports
		sqlBuf.append("UNION \n");
		sqlBuf.append(getParentReportSql("JBR_MAINDOC", indepResTemplate)); // Independent parent resolution reports
		return sqlBuf.toString();
	}

	private String getParentReportSql(String parentLinkAttr, ObjectId parentTemplateId){
		StringBuilder sqlBuf = new StringBuilder();
		sqlBuf.append("select distinct av_parent_report.card_id \n");
		sqlBuf.append("from card c \n");
		sqlBuf.append("		join attribute_value av_sign on c.card_id=av_sign.card_id AND av_sign.attribute_code=''JBR_INFD_SGNEX_LINK'' \n");
		sqlBuf.append("		join attribute_value av on c.card_id = av.card_id and av.attribute_code = ''" + parentLinkAttr + "'' \n");
		sqlBuf.append("		join card parend_card on parend_card.card_id = av.number_value and parend_card.template_id in (" + parentTemplateId.getId() + ")  \n");
		sqlBuf.append("		join attribute_value av_parent_report on av_parent_report.number_value = parend_card.card_id and av_parent_report.attribute_code=''ADMIN_702311''  \n");
		sqlBuf.append("		join attribute_value av_exec on av_parent_report.card_id = av_exec.card_id and av_exec.attribute_code=''ADMIN_702335''    \n");
		sqlBuf.append("WHERE c.card_id in ({0}) and c.template_id in (" + regularResTemplate.getId() + ") and av_sign.number_value = av_exec.number_value \n");
		return sqlBuf.toString();
	}
}
