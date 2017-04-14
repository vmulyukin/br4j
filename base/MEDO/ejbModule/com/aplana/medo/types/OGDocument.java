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
package com.aplana.medo.types;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.datatype.XMLGregorianCalendar;

import com.aplana.dmsi.types.DMSIObject;
import com.aplana.dmsi.types.common.DeliveryMethod;
import com.aplana.dmsi.types.common.File;
import com.aplana.dmsi.types.common.Organization;
import com.aplana.dmsi.types.common.ReceiptSource;
import com.aplana.dmsi.types.common.RequestAuthor;

@XmlTransient
public class OGDocument extends DMSIObject {

    private RequestAuthor author;
    private ImportedDocument importedDocument;
    private DeliveryMethod deliveryMethod = DeliveryMethod.MEDO;
    private String annotation;
    private ReceiptSource receiptSource;
    private Organization correspondent;
    private String regNumber;
    private XMLGregorianCalendar regDate;
    private List<File> files;
    private List<com.aplana.dmsi.types.common.Rubric> rubric;
    private Organization receiver;

    public RequestAuthor getAuthor() {
	return this.author;
    }

    public void setAuthor(RequestAuthor author) {
	this.author = author;
    }

    public ImportedDocument getImportedDocument() {
	return this.importedDocument;
    }

    public void setImportedDocument(ImportedDocument importedDocument) {
	this.importedDocument = importedDocument;
    }

    public DeliveryMethod getDeliveryMethod() {
	return this.deliveryMethod;
    }

    public void setDeliveryMethod(DeliveryMethod deliveryMethod) {
	this.deliveryMethod = deliveryMethod;
    }

    public String getAnnotation() {
	return this.annotation;
    }

    public void setAnnotation(String annotation) {
	this.annotation = annotation;
    }

    public ReceiptSource getReceiptSource() {
	return this.receiptSource;
    }

    public void setReceiptSource(ReceiptSource receiptSource) {
	this.receiptSource = receiptSource;
    }

    public List<File> getFiles() {
	if (this.files == null) {
	    this.files = new ArrayList<File>();
	}
	return this.files;
    }

    public List<com.aplana.dmsi.types.common.Rubric> getRubric() {
	if (this.rubric == null) {
	    this.rubric = new ArrayList<com.aplana.dmsi.types.common.Rubric>();
	}
	return this.rubric;
    }

	public Organization getCorrespondent() {
		return this.correspondent;
	}

	public void setCorrespondent(Organization correspondent) {
		this.correspondent = correspondent;
	}

	public String getRegNumber() {
		return this.regNumber;
	}

	public void setRegNumber(String regNumber) {
		this.regNumber = regNumber;
	}

	public XMLGregorianCalendar getRegDate() {
		return this.regDate;
	}

	public void setRegDate(XMLGregorianCalendar regDate) {
		this.regDate = regDate;
	}

	public Organization getReceiver() {
		return receiver;
	}

	public void setReceiver(Organization receiver) {
		this.receiver = receiver;
	}

}
