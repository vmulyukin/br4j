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
/**
 *
 */
package com.aplana.ireferent.completion.cards;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.ireferent.card.FileCard;
import com.aplana.ireferent.types.WSOFile;
import com.aplana.ireferent.types.WSOMFile;

/**
 * @author PPanichev
 *
 */
@XmlTransient
public class CompletionFile extends WSOFile {

    /**
     *
     */
    private final transient Log logger = LogFactory.getLog(getClass());
    private static final transient long serialVersionUID = 3155485549466665419L;
    private transient FileCard fileCard = null;
    private transient byte[] body = {};

    public CompletionFile(FileCard fc, Boolean isMObject) {
	this.fileCard = fc;
	if (fileCard != null) {
	    GregorianCalendar calen = new GregorianCalendar();
	    XMLGregorianCalendar dfg = null;;
	    DatatypeFactory datatypeFactory;
	    try {
		datatypeFactory = DatatypeFactory.newInstance();
		dfg = datatypeFactory.newXMLGregorianCalendar(calen);
	    } catch (DatatypeConfigurationException ex) {
		logger.error("com.aplana.ireferent.completion.cards.CompletionFile.XMLGregorianCalendar", ex);
	    }
	    this.setDate(dfg);
	    this.setFieldName("");
	    this.setId(fileCard.getId());
	    this.setTitle(fileCard.getUrl());
	    this.setName(fileCard.getName());
	    this.setUser("");
	    this.setVersion(fileCard.getVersion());
	    if(!isMObject) {
		try {
		    body  = getMaterialBase64(fileCard.getData());
		} catch(IOException ioe) {
		    logger.error("com.aplana.ireferent.completion.cards.getMaterialBase64", ioe);
		}
		this.setType(WSOFile.CLASS_TYPE);
	    } else
		this.setType(WSOMFile.CLASS_TYPE);
	    this.setBody(body);
	}
    }

    /**
     * @return the material
     * @throws IOException
     */
    private byte[] getMaterialBase64(InputStream inStream) throws IOException {
	byte[] result = null;
	ByteArrayOutputStream outStream = null;
	try {
	    byte[] buffer = new byte[4 * 1024];
	    int read;
	    outStream = new ByteArrayOutputStream();
	    while ((read = inStream.read(buffer)) != -1) {
		outStream.write(buffer, 0, read);
	    }
	    outStream.flush();
	    byte[] ba = outStream.toByteArray();
	    result = ba;
	} finally {
	    if (outStream != null) {
		outStream.close();
	    }
	}
	return result;
    }
}
