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
package com.aplana.dbmi.module.docflow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.QueryBase;

public class EndEnclosedProcessor extends ProcessorBase implements Parametrized {
	private static final long serialVersionUID = 1L;
	
	static final ObjectId enclosedCompleteDateId = ObjectId.predefined(DateAttribute.class,
            "jbr.visa.enclosedCompleteDate");
    public static final String PARAM_CONFIG_BEAN = "configBean";
    private VisaConfiguration config;

    public void setConfig(VisaConfiguration config) {
        this.config = config;
    }

    public void setParameter(String name, String value) {
        if (PARAM_CONFIG_BEAN.equalsIgnoreCase(name)) {
            config = (VisaConfiguration) getBeanFactory().getBean(value);
        } else {
            throw new IllegalArgumentException("Unknown parameter: " + name);
        }
    }

    @Override
    public Object process() throws DataException {
        // �������� ��������
        ChangeState move = (ChangeState) getAction();
        // �������� ����
        Card visa = move.getCard();
        returnVisaFromEnclosed(visa);
        return visa;
    }

    protected Object doAction(Action action) throws DataException {
        ActionQueryBase query = getQueryFactory().getActionQuery(action);
        query.setAccessChecker(null);
        query.setAction(action);
        return getDatabase().executeQuery(getSystemUser(), query);
    }

    protected ObjectId getObjectIdByTemplate(Class<?> type, String key, ObjectId template) throws DataException {
        Map<Object, ObjectId> specific = config.getObjectIdMap(key + VisaConfiguration.INFIX_TEMPLATE, Template.class, type);
        if (specific.containsKey(template)) {
            return specific.get(template);
        }
        return config.getObjectId(type, key);
    }

    protected ObjectId getPreviousCardStateFromHistory(final ObjectId cardId, final ObjectId historyAttrId) {
        try {
            return (ObjectId) this.getDatabase().executeQuery(getSystemUser(), new QueryBase() {
				private static final long serialVersionUID = 1L;
				@Override
                public Object processQuery() throws DataException {
                    final String sql = "select wfm.from_status_id from card c"
                            + " join attribute_value a on a.card_id = c.card_id and attribute_code = " + "'"
                            + historyAttrId.getId() + "'" + " join workflow_move wfm on wfm.name_rus = "
                            + " xmlserialize(content (xpath(cast('//part[last()]/@action' as varchar),"
                            + " cast(convert_from(a.long_binary_value, 'UTF8') as xml)))[1] as varchar(100))"
                            + " where c.card_id = " + cardId.getId();
                    RowMapper rowMapper = new RowMapper() {
                        public Object mapRow(ResultSet rs, int index) throws SQLException {
                            return new ObjectId(CardState.class, rs.getLong(1));
                        }
                    };
                    return getJdbcTemplate().queryForObject(sql, rowMapper);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    final protected boolean isCardAlreadyAtWFMState(final ObjectId cardId, final ObjectId wfmId) throws DataException {
        final Boolean cardAlreadyAtState = (Boolean) this.getDatabase().executeQuery(getSystemUser(), new QueryBase() {
			private static final long serialVersionUID = 1L;
			@Override
            public Object processQuery() throws DataException {
                final int result = getJdbcTemplate().queryForInt(
                        "select count(*) \n" + "from card c \n"
                                + "       join template t on t.template_id = c.template_id \n"
                                + "       join workflow_move wfm on wfm.workflow_id = t.workflow_id \n" + "where \n"
                                + "       c.status_id=wfm.to_status_id \n" + "   and c.card_id=? \n"
                                + "   and wfm.wfm_id=? \n", new Object[] { cardId.getId(), wfmId.getId() },
                        new int[] { Types.NUMERIC, Types.NUMERIC });
                return new Boolean(result > 0);
            }
        });
        return cardAlreadyAtState.booleanValue();
    }

    protected void returnVisaFromEnclosed(final Card visa) throws DataException {
        ObjectId historyId = null;
        WorkflowMove wfm = null;
        try {
            historyId = getObjectIdByTemplate(HtmlAttribute.class, VisaConfiguration.ATTR_DECISION_HISTORY, visa
                    .getTemplate());
        } catch (DataException e) {
        }
        if (historyId != null) {
            ObjectId returnState = getPreviousCardStateFromHistory(visa.getId(), historyId);
            wfm = CardUtils.findWorkFlowMove(visa.getId(), returnState, this.getQueryFactory(), this.getDatabase(),
                    getSystemUser());
        }
        if (wfm == null) {
            final ObjectId wfmId = getObjectIdByTemplate(WorkflowMove.class,
                    VisaConfiguration.MOVE_RETURN_FROM_ENCLOSED, visa.getTemplate());
            wfm = (WorkflowMove) DataObject.createFromId(wfmId);
        }
        // ���� �������� ��� � ������ ��������� - ������ ������ ����� ��
        // ����...
        if (isCardAlreadyAtWFMState(visa.getId(), wfm.getId())) {
            // ��� � ������ ���������...
            return;
        }
        final ChangeState move = new ChangeState();
        move.setWorkflowMove(wfm);
        move.setCard(visa);
        doAction(move);
    }
}