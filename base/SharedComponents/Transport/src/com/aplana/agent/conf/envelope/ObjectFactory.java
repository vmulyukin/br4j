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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2.1.1-4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.10.29 at 03:30:39 PM EET 
//


package com.aplana.agent.conf.envelope;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.aplana.agent.conf.envelope package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.aplana.agent.conf.envelope
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Letter.Addressee }
     * 
     */
    public Letter.Addressee createLetterAddressee() {
        return new Letter.Addressee();
    }

    /**
     * Create an instance of {@link Letter.Attachments.Attachment }
     * 
     */
    public Letter.Attachments.Attachment createLetterAttachmentsAttachment() {
        return new Letter.Attachments.Attachment();
    }

    /**
     * Create an instance of {@link Letter.Attachments }
     * 
     */
    public Letter.Attachments createLetterAttachments() {
        return new Letter.Attachments();
    }

    /**
     * Create an instance of {@link Letter }
     * 
     */
    public Letter createLetter() {
        return new Letter();
    }

    /**
     * Create an instance of {@link Letter.Sender }
     * 
     */
    public Letter.Sender createLetterSender() {
        return new Letter.Sender();
    }

}