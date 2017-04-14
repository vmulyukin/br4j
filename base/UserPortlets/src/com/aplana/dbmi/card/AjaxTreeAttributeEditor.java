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
package com.aplana.dbmi.card;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.servlet.ServletException;

import org.json.JSONWriter;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.service.DataException;

public class AjaxTreeAttributeEditor extends JspAttributeEditor {

    private String PARAM_COLLAPSED_BY_DEF = "collapsedByDefault";
    private String PARAM_SELECT_LEAVES_ONLY = "selectLeavesOnly";
    
    private boolean isCollapsed;
    private boolean isLeavesOnly;

    public boolean isValueCollapsable() {
        return true;
    }

    public AjaxTreeAttributeEditor() {
        setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/AjaxTree.jsp");
        setParameter(PARAM_INIT_JSP,
                "/WEB-INF/jsp/html/attr/AjaxTreeInclude.jsp");
    }

    public boolean gatherData(ActionRequest request, Attribute attr)
            throws DataException {
        TreeAttribute treeAttr = (TreeAttribute) attr;
        String param = request
                .getParameter(getAttrHtmlId(treeAttr) + "_values");
        if (param == null) {
            return false;
        } else {
            String[] idVals = param.split(", ");
            List values = new ArrayList();
            for (int i = 0; i < idVals.length; i++) {
                if (!idVals[i].equals("")) {
                    values.add(DataObject.createFromId(new ObjectId(
                            ReferenceValue.class, Long.parseLong(idVals[i]))));
                }
            }
            // ��������� ������� ������ �������� ���������� �������������
            String new_check = request.getParameter(getAttrHtmlId(treeAttr)
                    + "_new_check");
            if (new_check != null) {
                String new_value = request.getParameter(getAttrHtmlId(treeAttr)
                        + "_new_value");
                if (new_value != null && new_value.length() > 0) {
                    values.add(ReferenceValue.newAnotherValue(new_value));
                }
            }
            treeAttr.setValues(values);
            return true;
        }
    }

    public static String getJSONValues(TreeAttribute attr)
            throws ServletException {
        StringWriter w = new StringWriter();
        JSONWriter jw = new JSONWriter(w);
        try {
            jw.array();
            if (attr != null && attr.getValues() != null) {
                Iterator iterVal = attr.getValues().iterator();
                while (iterVal.hasNext()) {
                    ReferenceValue refVal = (ReferenceValue) iterVal.next();
                    jw.object();
                    jw.key("id").value(refVal.getId().getId().toString());
                    jw.endObject();
                }
            }
            jw.endArray();
            return w.toString();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void setParameter(String name, String value) {
        if (PARAM_COLLAPSED_BY_DEF.equals(name)) {
            isCollapsed = Boolean.parseBoolean(value);
        } else if (PARAM_SELECT_LEAVES_ONLY.equals(name)) {
        	isLeavesOnly = Boolean.parseBoolean(value);
        } else {
            super.setParameter(name, value);
        }
    }

    public boolean isCollapsedByDefault() {
        return isCollapsed;
    }
    
    public boolean isLeavesOnly() {
    	return isLeavesOnly;
    }
}
