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
package com.aplana.dmsi.types.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.aplana.dmsi.types.Econtact;
import com.aplana.dmsi.types.EmailEcontact;
import com.aplana.dmsi.types.FaxEcontact;
import com.aplana.dmsi.types.WorkPhoneEcontact;

public class EcontactAdapter extends XmlAdapter<Econtact, Econtact> {

    @Override
    public Econtact marshal(Econtact contact) throws Exception {
	return contact;
    }

    @Override
    public Econtact unmarshal(Econtact contact) throws Exception {
	switch (contact.getType()) {
	case EMAIL:
	    return new EmailEcontact(contact);
	case WORK_PHONE:
	    return new WorkPhoneEcontact(contact);
	case FAX:
	    return new FaxEcontact(contact);
	default:
	    return contact;
	}
    }
}
