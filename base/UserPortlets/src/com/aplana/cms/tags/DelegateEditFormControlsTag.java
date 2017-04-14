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
package com.aplana.cms.tags;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.delegate.DelegateEditBean;
import com.aplana.dbmi.delegate.DelegateHelper;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.util.JspUtils;

import java.io.PrintWriter;


public class DelegateEditFormControlsTag implements TagProcessor
{

    private DelegateEditBean bean;
    private boolean isFromPersonSelectable;
    private boolean isDelegationEditable = true;

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception {
		bean = new DelegateEditBean();

        DateAttribute startDateAttribute =
                (DateAttribute) item.getAttributeById(ObjectId.predefined(DateAttribute.class, "jbr.delegation.date.start"));
        if(null != startDateAttribute) {
            bean.setFrom_date(JspUtils.Date2Str(startDateAttribute.getValue()));
        } else {
            bean.setFrom_date("");
        }

        DateAttribute endDateAttribute =
                (DateAttribute) item.getAttributeById(ObjectId.predefined(DateAttribute.class, "jbr.delegation.date.end"));
        if(null != endDateAttribute) {
            bean.setTo_date(JspUtils.Date2Str(endDateAttribute.getValue()));
        } else {
            bean.setTo_date("");
        }

        PersonAttribute fromAttribute =
                (PersonAttribute) item.getAttributeById(ObjectId.predefined(PersonAttribute.class, "jbr.delegation.from"));
        if(null != fromAttribute && null != fromAttribute.getValue()) {
            bean.setUser_from(JspUtils.convertId2Str(fromAttribute.getValue()));
        }

        PersonAttribute toAttribute =
                (PersonAttribute) item.getAttributeById(ObjectId.predefined(PersonAttribute.class, "jbr.delegation.to"));
        if(null != toAttribute && null != toAttribute.getValue()) {
            bean.setUser_to(JspUtils.convertId2Str(toAttribute.getValue()));
        }

        bean.setCurrentUserId(JspUtils.convertId2Str(cms.getService().getPerson().getId()));
        isFromPersonSelectable = DelegateHelper.isFromPersonSelectable(cms.getService());
        
        LongAttribute delegationId =
        		(LongAttribute) item.getAttributeById(ObjectId.predefined(LongAttribute.class, "jbr.delegation.id"));
        
        isDelegationEditable = DelegateHelper.isDelegationEditable(cms.getService(), delegationId);

		return true;
	}

	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms) {
		if(bean.getFrom_date() != null && !bean.getFrom_date().equals(""))
			out.write("<input type=\"hidden\" id=\"delegationEditForm_fromDateValue\" value=\"" + bean.getFrom_date() + "\"/>");
        if(bean.getTo_date() != null && !bean.getTo_date().equals(""))
        	out.write("<input type=\"hidden\" id=\"delegationEditForm_toDateValue\" value=\"" + bean.getTo_date() + "\"/>");
        out.write("<input type=\"hidden\" id=\"delegationEditForm_userFromValue\" value=\"" + bean.getUser_from() + "\"/>");
        out.write("<input type=\"hidden\" id=\"delegationEditForm_userToValue\" value=\"" + bean.getUser_to() + "\"/>");
        out.write("<input type=\"hidden\" id=\"delegationEditForm_isFromPersonSelectableValue\" value=\"" + isFromPersonSelectable + "\"/>");
        out.write("<input type=\"hidden\" id=\"delegationEditForm_isDelegationEditable\" value=\"" + isDelegationEditable + "\"/>");
	}

}