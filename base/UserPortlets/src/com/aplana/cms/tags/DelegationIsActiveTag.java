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
import com.aplana.dbmi.model.*;

import java.io.PrintWriter;

public class DelegationIsActiveTag implements TagProcessor
{

    private Delegation delegation;

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception {
		delegation = new Delegation();

        DateAttribute startDateAttribute =
                (DateAttribute) item.getAttributeById(ObjectId.predefined(DateAttribute.class, "jbr.delegation.date.start"));
        if(null != startDateAttribute) {
            delegation.setStartAt(startDateAttribute.getValue());
        }

        DateAttribute endDateAttribute =
                (DateAttribute) item.getAttributeById(ObjectId.predefined(DateAttribute.class, "jbr.delegation.date.end"));
        if(null != endDateAttribute) {
            delegation.setEndAt(endDateAttribute.getValue());
        }

		return true;
	}

	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms) {
        if(delegation.isActive()) {
            out.write("<span class='delegationActive'>��������</span>");
        } else {
            out.write("<span class='delegationInactive'>����������</span>");
        }
	}
}