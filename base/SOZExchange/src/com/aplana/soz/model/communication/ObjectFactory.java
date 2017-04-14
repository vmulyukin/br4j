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

package com.aplana.soz.model.communication;

import java.math.BigInteger;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.aplana.soz.model.communication package. 
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

    private final static QName _DocumentNumberDate_QNAME = new QName("http://www.infpres.com/IEDMS", "date");
    private final static QName _DictionaryClientsEntryComment_QNAME = new QName("http://www.infpres.com/IEDMS", "comment");
    private final static QName _DictionaryClientsEntryStartDate_QNAME = new QName("http://www.infpres.com/IEDMS", "startDate");
    private final static QName _DocumentClauseDesignation_QNAME = new QName("http://www.infpres.com/IEDMS", "designation");
    private final static QName _DeliveryDestinationFiles_QNAME = new QName("http://www.infpres.com/IEDMS", "files");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.aplana.soz.model.communication
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Communication.Files }
     * 
     */
    public Communication.Files createCommunicationFiles() {
        return new Communication.Files();
    }

    /**
     * Create an instance of {@link Dictionary.Clients }
     * 
     */
    public Dictionary.Clients createDictionaryClients() {
        return new Dictionary.Clients();
    }

    /**
     * Create an instance of {@link LinkedDocument.Document.Links }
     * 
     */
    public LinkedDocument.Document.Links createLinkedDocumentDocumentLinks() {
        return new LinkedDocument.Document.Links();
    }

    /**
     * Create an instance of {@link DocumentClause }
     * 
     */
    public DocumentClause createDocumentClause() {
        return new DocumentClause();
    }

    /**
     * Create an instance of {@link MessageNotification }
     * 
     */
    public MessageNotification createMessageNotification() {
        return new MessageNotification();
    }

    /**
     * Create an instance of {@link LinkedDocument.Document }
     * 
     */
    public LinkedDocument.Document createLinkedDocumentDocument() {
        return new LinkedDocument.Document();
    }

    /**
     * Create an instance of {@link AddresseeList }
     * 
     */
    public AddresseeList createAddresseeList() {
        return new AddresseeList();
    }

    /**
     * Create an instance of {@link MessageNotification.ReportPrepared }
     * 
     */
    public MessageNotification.ReportPrepared createMessageNotificationReportPrepared() {
        return new MessageNotification.ReportPrepared();
    }

    /**
     * Create an instance of {@link AssociatedFile }
     * 
     */
    public AssociatedFile createAssociatedFile() {
        return new AssociatedFile();
    }

    /**
     * Create an instance of {@link Notification }
     * 
     */
    public Notification createNotification() {
        return new Notification();
    }

    /**
     * Create an instance of {@link MessageNotification.DocumentRefused }
     * 
     */
    public MessageNotification.DocumentRefused createMessageNotificationDocumentRefused() {
        return new MessageNotification.DocumentRefused();
    }

    /**
     * Create an instance of {@link com.aplana.soz.model.communication.Document.Addressees }
     * 
     */
    public com.aplana.soz.model.communication.Document.Addressees createDocumentAddressees() {
        return new com.aplana.soz.model.communication.Document.Addressees();
    }

    /**
     * Create an instance of {@link AddresseeList.Contents }
     * 
     */
    public AddresseeList.Contents createAddresseeListContents() {
        return new AddresseeList.Contents();
    }

    /**
     * Create an instance of {@link com.aplana.soz.model.communication.Document.Clauses }
     * 
     */
    public com.aplana.soz.model.communication.Document.Clauses createDocumentClauses() {
        return new com.aplana.soz.model.communication.Document.Clauses();
    }

    /**
     * Create an instance of {@link MessageAcknowledgment }
     * 
     */
    public MessageAcknowledgment createMessageAcknowledgment() {
        return new MessageAcknowledgment();
    }

    /**
     * Create an instance of {@link MessageNotification.ExecutorAssigned }
     * 
     */
    public MessageNotification.ExecutorAssigned createMessageNotificationExecutorAssigned() {
        return new MessageNotification.ExecutorAssigned();
    }

    /**
     * Create an instance of {@link LinkedDocument }
     * 
     */
    public LinkedDocument createLinkedDocument() {
        return new LinkedDocument();
    }

    /**
     * Create an instance of {@link MessageNotification.ReportSent }
     * 
     */
    public MessageNotification.ReportSent createMessageNotificationReportSent() {
        return new MessageNotification.ReportSent();
    }

    /**
     * Create an instance of {@link Dictionary }
     * 
     */
    public Dictionary createDictionary() {
        return new Dictionary();
    }

    /**
     * Create an instance of {@link Dictionary.Clients.Entry }
     * 
     */
    public Dictionary.Clients.Entry createDictionaryClientsEntry() {
        return new Dictionary.Clients.Entry();
    }

    /**
     * Create an instance of {@link com.aplana.soz.model.communication.Document }
     * 
     */
    public com.aplana.soz.model.communication.Document createDocument() {
        return new com.aplana.soz.model.communication.Document();
    }

    /**
     * Create an instance of {@link Anyone }
     * 
     */
    public Anyone createAnyone() {
        return new Anyone();
    }

    /**
     * Create an instance of {@link LinkedDocument.Document.Signatories }
     * 
     */
    public LinkedDocument.Document.Signatories createLinkedDocumentDocumentSignatories() {
        return new LinkedDocument.Document.Signatories();
    }

    /**
     * Create an instance of {@link LinkedDocument.Document.Enclosures }
     * 
     */
    public LinkedDocument.Document.Enclosures createLinkedDocumentDocumentEnclosures() {
        return new LinkedDocument.Document.Enclosures();
    }

    /**
     * Create an instance of {@link MessageNotification.CourseChanged }
     * 
     */
    public MessageNotification.CourseChanged createMessageNotificationCourseChanged() {
        return new MessageNotification.CourseChanged();
    }

    /**
     * Create an instance of {@link com.aplana.soz.model.communication.Document.Signatories }
     * 
     */
    public com.aplana.soz.model.communication.Document.Signatories createDocumentSignatories() {
        return new com.aplana.soz.model.communication.Document.Signatories();
    }

    /**
     * Create an instance of {@link DocumentNumber }
     * 
     */
    public DocumentNumber createDocumentNumber() {
        return new DocumentNumber();
    }

    /**
     * Create an instance of {@link Communication.DeliveryIndex }
     * 
     */
    public Communication.DeliveryIndex createCommunicationDeliveryIndex() {
        return new Communication.DeliveryIndex();
    }

    /**
     * Create an instance of {@link com.aplana.soz.model.communication.Document.Enclosures }
     * 
     */
    public com.aplana.soz.model.communication.Document.Enclosures createDocumentEnclosures() {
        return new com.aplana.soz.model.communication.Document.Enclosures();
    }

    /**
     * Create an instance of {@link CommunicationPartner }
     * 
     */
    public CommunicationPartner createCommunicationPartner() {
        return new CommunicationPartner();
    }

    /**
     * Create an instance of {@link DeliveryDestination }
     * 
     */
    public DeliveryDestination createDeliveryDestination() {
        return new DeliveryDestination();
    }

    /**
     * Create an instance of {@link Signatory }
     * 
     */
    public Signatory createSignatory() {
        return new Signatory();
    }

    /**
     * Create an instance of {@link Addressee }
     * 
     */
    public Addressee createAddressee() {
        return new Addressee();
    }

    /**
     * Create an instance of {@link MessageAcknowledgment.HashCodes }
     * 
     */
    public MessageAcknowledgment.HashCodes createMessageAcknowledgmentHashCodes() {
        return new MessageAcknowledgment.HashCodes();
    }

    /**
     * Create an instance of {@link Enclosure }
     * 
     */
    public Enclosure createEnclosure() {
        return new Enclosure();
    }

    /**
     * Create an instance of {@link MessageNotification.DocumentAccepted }
     * 
     */
    public MessageNotification.DocumentAccepted createMessageNotificationDocumentAccepted() {
        return new MessageNotification.DocumentAccepted();
    }

    /**
     * Create an instance of {@link DocumentReference }
     * 
     */
    public DocumentReference createDocumentReference() {
        return new DocumentReference();
    }

    /**
     * Create an instance of {@link com.aplana.soz.model.communication.Document.Links }
     * 
     */
    public com.aplana.soz.model.communication.Document.Links createDocumentLinks() {
        return new com.aplana.soz.model.communication.Document.Links();
    }

    /**
     * Create an instance of {@link com.aplana.soz.model.communication.Document.Correspondents }
     * 
     */
    public com.aplana.soz.model.communication.Document.Correspondents createDocumentCorrespondents() {
        return new com.aplana.soz.model.communication.Document.Correspondents();
    }

    /**
     * Create an instance of {@link Communication }
     * 
     */
    public Communication createCommunication() {
        return new Communication();
    }

    /**
     * Create an instance of {@link LinkedDocument.Document.Addressees }
     * 
     */
    public LinkedDocument.Document.Addressees createLinkedDocumentDocumentAddressees() {
        return new LinkedDocument.Document.Addressees();
    }

    /**
     * Create an instance of {@link LinkedDocument.Document.Clauses }
     * 
     */
    public LinkedDocument.Document.Clauses createLinkedDocumentDocumentClauses() {
        return new LinkedDocument.Document.Clauses();
    }

    /**
     * Create an instance of {@link DocumentClause.Parcipants }
     * 
     */
    public DocumentClause.Parcipants createDocumentClauseParcipants() {
        return new DocumentClause.Parcipants();
    }

    /**
     * Create an instance of {@link LinkedDocument.Document.Correspondents }
     * 
     */
    public LinkedDocument.Document.Correspondents createLinkedDocumentDocumentCorrespondents() {
        return new LinkedDocument.Document.Correspondents();
    }

    /**
     * Create an instance of {@link Communication.Header }
     * 
     */
    public Communication.Header createCommunicationHeader() {
        return new Communication.Header();
    }

    /**
     * Create an instance of {@link Correspondent }
     * 
     */
    public Correspondent createCorrespondent() {
        return new Correspondent();
    }

    /**
     * Create an instance of {@link QualifiedValue }
     * 
     */
    public QualifiedValue createQualifiedValue() {
        return new QualifiedValue();
    }

    /**
     * Create an instance of {@link MessageAcknowledgment.HashCodes.Item }
     * 
     */
    public MessageAcknowledgment.HashCodes.Item createMessageAcknowledgmentHashCodesItem() {
        return new MessageAcknowledgment.HashCodes.Item();
    }

    /**
     * Create an instance of {@link Dictionary.Clients.Entry.Organization }
     * 
     */
    public Dictionary.Clients.Entry.Organization createDictionaryClientsEntryOrganization() {
        return new Dictionary.Clients.Entry.Organization();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.infpres.com/IEDMS", name = "date", scope = DocumentNumber.class)
    public JAXBElement<XMLGregorianCalendar> createDocumentNumberDate(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_DocumentNumberDate_QNAME, XMLGregorianCalendar.class, DocumentNumber.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.infpres.com/IEDMS", name = "comment", scope = Dictionary.Clients.Entry.class)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    public JAXBElement<String> createDictionaryClientsEntryComment(String value) {
        return new JAXBElement<String>(_DictionaryClientsEntryComment_QNAME, String.class, Dictionary.Clients.Entry.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.infpres.com/IEDMS", name = "startDate", scope = Dictionary.Clients.Entry.class)
    public JAXBElement<XMLGregorianCalendar> createDictionaryClientsEntryStartDate(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_DictionaryClientsEntryStartDate_QNAME, XMLGregorianCalendar.class, Dictionary.Clients.Entry.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.infpres.com/IEDMS", name = "designation", scope = DocumentClause.class)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    public JAXBElement<String> createDocumentClauseDesignation(String value) {
        return new JAXBElement<String>(_DocumentClauseDesignation_QNAME, String.class, DocumentClause.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link BigInteger }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.infpres.com/IEDMS", name = "files", scope = DeliveryDestination.class)
    public JAXBElement<List<BigInteger>> createDeliveryDestinationFiles(List<BigInteger> value) {
        return new JAXBElement<List<BigInteger>>(_DeliveryDestinationFiles_QNAME, ((Class) List.class), DeliveryDestination.class, ((List<BigInteger> ) value));
    }

}
