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

import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.aplana.dmsi.types.Author;
import com.aplana.dmsi.types.DocumentType;
import com.aplana.dmsi.types.ImportedIncomeDocumentType;
import com.aplana.dmsi.types.ImportedOGDocumentType;
import com.aplana.dmsi.types.PrivatePersonWithSign;
import com.aplana.dmsi.types.ImportedOGDocumentType.OgType;

public class DocumentTypeAdapter extends XmlAdapter<DocumentType, DocumentType> {

    private static final Byte COLLECTIVE_DOC_VALUE = 1;

    @Override
    public DocumentType marshal(DocumentType doc) throws Exception {
        return doc;
    }

    @Override
    public DocumentType unmarshal(DocumentType doc) throws Exception {
        DocumentSort docSort = resolveDocumentSort(doc);
        switch (docSort) {
        case ANONYMOUS_OG: {
            ImportedOGDocumentType resultDoc = new ImportedOGDocumentType(doc);
            resultDoc.setOgType(OgType.ANONYMOUS);
            return resultDoc;
        }
        case INDIVIDUAL_OG: {
            ImportedOGDocumentType resultDoc = new ImportedOGDocumentType(doc);
            resultDoc.setOgType(OgType.INDIVIDUAL);
            return resultDoc;
        }
        case COLLECTION_OG: {
            ImportedOGDocumentType resultDoc = new ImportedOGDocumentType(doc);
            resultDoc.setOgType(OgType.COLLECTIVE);
            return resultDoc;
        }
        case INCOME:
            return new ImportedIncomeDocumentType(doc);
        default:
            return new ImportedIncomeDocumentType(doc);
        }
    }

    private static enum DocumentSort {
        ANONYMOUS_OG, INDIVIDUAL_OG, COLLECTION_OG, INCOME
    }

    private DocumentSort resolveDocumentSort(DocumentType doc) {
        if (doc.getType() != 1) {
            return DocumentSort.INCOME;
        }

        List<Author> authors = doc.getAuthor();
        if (authors.isEmpty()) {
            return DocumentSort.ANONYMOUS_OG;
        }

        boolean isCollective = COLLECTIVE_DOC_VALUE.equals(doc.getCollection());
        int privatePersonsCount = 0;
        for (Author author : authors) {
            author.getContainedObject();
            Object authorObject = author.getContainedObject();
            if (authorObject instanceof PrivatePersonWithSign) {
                privatePersonsCount++;
            }
        }
        if (privatePersonsCount > 0) {
            if (isCollective)
                return DocumentSort.COLLECTION_OG;
            return DocumentSort.INDIVIDUAL_OG;
        }

        return DocumentSort.INCOME;

    }
}