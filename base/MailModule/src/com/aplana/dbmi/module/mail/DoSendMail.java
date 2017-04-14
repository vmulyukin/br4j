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
package com.aplana.dbmi.module.mail;

import com.aplana.dbmi.action.SendMail;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoSendMail extends ActionQueryBase {
	private static final long serialVersionUID = 1L;

	public Object processQuery() throws DataException {
        SendMail sendMailAction = (SendMail) getAction();
        Mailer mailer = Mailer.getMailer();

        Person senderPerson = getUser().getPerson();
        
        boolean result;
        
        if (senderPerson.getEmail() != null && senderPerson.getEmail().length() > 0) {
            result = mailer.sendMail(sendMailAction.getBody(), sendMailAction.getRecipient(), sendMailAction.getSubject(), senderPerson.getEmail(), senderPerson.getFullName());
        } else {
            result = mailer.sendMail(sendMailAction.getBody(), sendMailAction.getRecipient(), sendMailAction.getSubject());
        }
        return new Boolean(result);
    }

}
