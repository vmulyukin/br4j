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

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;

import java.util.Collections;
import java.util.List;

/**
 * ���������, ����������� ������� � ������� ���� �� ����� ��������,
 * � ������� �������� ������� ����� templateId, � �������� ��������
 * NAME ����� �������� ��������� value.
 * ���� �� ����� �������� �� �������, �� ������������ ����� ��������
 * ��������� � ������ errorMsg
 */
public class CheckCardExistence extends ProcessCard {

    protected static final String TEMPLATE_PARAM = "templateId";
    protected static final String DEST_VALUE_PARAM = "value";
    protected static final String ERROR_MSG_PARAM = "errorMsg";

    private ObjectId templateId = null;
    private String value = null;
    private String errorMsg;

    @Override
    public Object process() throws DataException {
        Search search = new Search();
        search.setByAttributes(true);
        search.setTemplates(Collections.singletonList(DataObject.createFromId(templateId)));
        search.addStringAttribute(IdUtils.smartMakeAttrId("NAME", StringAttribute.class), value);
        search.setSearchLimit(1);
        final List<Card> cards = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser());
        if (null == cards) {
            throw new DataException(errorMsg);
        }
        return null;
    }

    @Override
    public void setParameter(String name, String value) {
        if (TEMPLATE_PARAM.equalsIgnoreCase(name)) {
            this.templateId = IdUtils.smartMakeAttrId(value, Template.class);
        } else if (DEST_VALUE_PARAM.equalsIgnoreCase(name)) {
            this.value = value;
        } else if (ERROR_MSG_PARAM.equalsIgnoreCase(name)) {
            this.errorMsg = value;
        } else {
            super.setParameter(name, value);
        }
    }
}
