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

package com.aplana.crypto.cryptoserviceproxy;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.aplana.crypto.cryptoserviceproxy package. 
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

    private final static QName _CheckStringContentSignature_QNAME = new QName("http://crypto.aplana.com/", "checkStringContentSignature");
    private final static QName _CheckStringContentSignatureResponse_QNAME = new QName("http://crypto.aplana.com/", "checkStringContentSignatureResponse");
    private final static QName _VerifyPKCS7_QNAME = new QName("http://crypto.aplana.com/", "verifyPKCS7");
    private final static QName _VerifyPKCS7Response_QNAME = new QName("http://crypto.aplana.com/", "verifyPKCS7Response");
    private final static QName _CryptoServiceException_QNAME = new QName("http://crypto.aplana.com/", "CryptoServiceException");
    private final static QName _GetByteArrayDigest_QNAME = new QName("http://crypto.aplana.com/", "getByteArrayDigest");
    private final static QName _GetByteArrayDigestResponse_QNAME = new QName("http://crypto.aplana.com/", "getByteArrayDigestResponse");
    private final static QName _GetByteArrayDigestArg0_QNAME = new QName("", "arg0");
    private final static QName _GetByteArrayDigestResponseReturn_QNAME = new QName("", "return");
    private final static QName _VerifyPKCS7Arg1_QNAME = new QName("", "arg1");
    private final static QName _CheckCertificate_QNAME = new QName("http://crypto.aplana.com/", "checkCertificate");
    private final static QName _CheckCertificateResponse_QNAME = new QName("http://crypto.aplana.com/", "checkCertificateResponse");
    private final static QName _CheckCertificateArg0_QNAME = new QName("", "arg0");
    private final static QName _CheckCertificateResponseReturn_QNAME = new QName("", "return");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.aplana.crypto.cryptoserviceproxy
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CheckStringContentSignature }
     * 
     */
    public CheckStringContentSignature createCheckStringContentSignature() {
        return new CheckStringContentSignature();
    }

    /**
     * Create an instance of {@link VerifyPKCS7Response }
     * 
     */
    public VerifyPKCS7Response createVerifyPKCS7Response() {
        return new VerifyPKCS7Response();
    }

    /**
     * Create an instance of {@link WsPkcs7Result }
     * 
     */
    public WsPkcs7Result createWsPkcs7Result() {
        return new WsPkcs7Result();
    }

    /**
     * Create an instance of {@link GetByteArrayDigest }
     * 
     */
    public GetByteArrayDigest createGetByteArrayDigest() {
        return new GetByteArrayDigest();
    }

    /**
     * Create an instance of {@link GetByteArrayDigestResponse }
     * 
     */
    public GetByteArrayDigestResponse createGetByteArrayDigestResponse() {
        return new GetByteArrayDigestResponse();
    }

    /**
     * Create an instance of {@link CheckStringContentSignatureResponse }
     * 
     */
    public CheckStringContentSignatureResponse createCheckStringContentSignatureResponse() {
        return new CheckStringContentSignatureResponse();
    }

    /**
     * Create an instance of {@link CryptoServiceException }
     * 
     */
    public CryptoServiceException createCryptoServiceException() {
        return new CryptoServiceException();
    }

    /**
     * Create an instance of {@link VerifyPKCS7 }
     * 
     */
    public VerifyPKCS7 createVerifyPKCS7() {
        return new VerifyPKCS7();
    }
    
    public CheckCertificate createCheckCertificate() {
        return new CheckCertificate();
    }

    public CheckCertificateResponse createCheckCertificateResponse() {
        return new CheckCertificateResponse();
    }


    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CheckStringContentSignature }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://crypto.aplana.com/", name = "checkStringContentSignature")
    public JAXBElement<CheckStringContentSignature> createCheckStringContentSignature(CheckStringContentSignature value) {
        return new JAXBElement<CheckStringContentSignature>(_CheckStringContentSignature_QNAME, CheckStringContentSignature.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CheckStringContentSignatureResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://crypto.aplana.com/", name = "checkStringContentSignatureResponse")
    public JAXBElement<CheckStringContentSignatureResponse> createCheckStringContentSignatureResponse(CheckStringContentSignatureResponse value) {
        return new JAXBElement<CheckStringContentSignatureResponse>(_CheckStringContentSignatureResponse_QNAME, CheckStringContentSignatureResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VerifyPKCS7 }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://crypto.aplana.com/", name = "verifyPKCS7")
    public JAXBElement<VerifyPKCS7> createVerifyPKCS7(VerifyPKCS7 value) {
        return new JAXBElement<VerifyPKCS7>(_VerifyPKCS7_QNAME, VerifyPKCS7 .class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VerifyPKCS7Response }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://crypto.aplana.com/", name = "verifyPKCS7Response")
    public JAXBElement<VerifyPKCS7Response> createVerifyPKCS7Response(VerifyPKCS7Response value) {
        return new JAXBElement<VerifyPKCS7Response>(_VerifyPKCS7Response_QNAME, VerifyPKCS7Response.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CryptoServiceException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://crypto.aplana.com/", name = "CryptoServiceException")
    public JAXBElement<CryptoServiceException> createCryptoServiceException(CryptoServiceException value) {
        return new JAXBElement<CryptoServiceException>(_CryptoServiceException_QNAME, CryptoServiceException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetByteArrayDigest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://crypto.aplana.com/", name = "getByteArrayDigest")
    public JAXBElement<GetByteArrayDigest> createGetByteArrayDigest(GetByteArrayDigest value) {
        return new JAXBElement<GetByteArrayDigest>(_GetByteArrayDigest_QNAME, GetByteArrayDigest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetByteArrayDigestResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://crypto.aplana.com/", name = "getByteArrayDigestResponse")
    public JAXBElement<GetByteArrayDigestResponse> createGetByteArrayDigestResponse(GetByteArrayDigestResponse value) {
        return new JAXBElement<GetByteArrayDigestResponse>(_GetByteArrayDigestResponse_QNAME, GetByteArrayDigestResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "arg0", scope = GetByteArrayDigest.class)
    public JAXBElement<byte[]> createGetByteArrayDigestArg0(byte[] value) {
        return new JAXBElement<byte[]>(_GetByteArrayDigestArg0_QNAME, byte[].class, GetByteArrayDigest.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "return", scope = GetByteArrayDigestResponse.class)
    public JAXBElement<byte[]> createGetByteArrayDigestResponseReturn(byte[] value) {
        return new JAXBElement<byte[]>(_GetByteArrayDigestResponseReturn_QNAME, byte[].class, GetByteArrayDigestResponse.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "arg1", scope = VerifyPKCS7 .class)
    public JAXBElement<byte[]> createVerifyPKCS7Arg1(byte[] value) {
        return new JAXBElement<byte[]>(_VerifyPKCS7Arg1_QNAME, byte[].class, VerifyPKCS7 .class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "arg0", scope = VerifyPKCS7 .class)
    public JAXBElement<byte[]> createVerifyPKCS7Arg0(byte[] value) {
        return new JAXBElement<byte[]>(_GetByteArrayDigestArg0_QNAME, byte[].class, VerifyPKCS7 .class, ((byte[]) value));
    }
    
    @XmlElementDecl(namespace = "http://crypto.aplana.com/", name = "CheckCertificate")
    public JAXBElement<CheckCertificate> createCheckCertificate(CheckCertificate value) {
        return new JAXBElement<CheckCertificate>(_CheckCertificate_QNAME, CheckCertificate.class, null, value);
    }

    @XmlElementDecl(namespace = "http://crypto.aplana.com/", name = "CheckCertificateResponse")
    public JAXBElement<CheckCertificateResponse> createCheckCertificateResponse(CheckCertificateResponse value) {
        return new JAXBElement<CheckCertificateResponse>(_CheckCertificateResponse_QNAME, CheckCertificateResponse.class, null, value);
    }

    @XmlElementDecl(namespace = "", name = "arg0", scope = CheckCertificate.class)
    public JAXBElement<byte[]> createCheckCertificateArg0(byte[] value) {
        return new JAXBElement<byte[]>(_CheckCertificateArg0_QNAME, byte[].class, CheckCertificate.class, ((byte[]) value));
    }


    @XmlElementDecl(namespace = "", name = "return", scope = CheckCertificateResponse.class)
    public JAXBElement<byte[]> createCheckCertificateResponseReturn(byte[] value) {
        return new JAXBElement<byte[]>(_CheckCertificateResponseReturn_QNAME, byte[].class, CheckCertificateResponse.class, ((byte[]) value));
    }

}