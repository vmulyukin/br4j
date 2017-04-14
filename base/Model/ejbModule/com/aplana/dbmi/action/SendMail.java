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
package com.aplana.dbmi.action;

/**
 * {@link Action} implementation used to send email for given
 * recepient with defined subject and content.
 * <br>
 * Returns null as result.
 */
public class SendMail implements Action<Boolean> {

    private static final long serialVersionUID = 1L;
    
    private String recipient;
    private String subject;
    private String body;
    
    /**
     * Creates new action instance with given addressee, subject and body 
     * @param recipient recipient of the message. Should be a valid email address.
     * @param subject subject of the message to be send
     * @param body body of the message to be sent
     */
    public SendMail(String recipient, String subject, String body) {
        super();
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }

    /**
     * Gets body of the message to be sent
     * @return body of the message to be sent
     */
    public String getBody() {
        return body;
    }

    /**
     * Sets body of the message to be sent
     * @param body desired message text
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Gets recipient of the message 
     * @return email address of message recipient
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * Sets recipient of the message
     * @param recipient desired email address to sent email to
     */
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    /**
     * Gets message subject
     * @return message subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets message subject
     * @param subject desired value of message subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @see Action#getResultType()
     */
    public Class<?> getResultType() {
        return Boolean.class;
    }

}
