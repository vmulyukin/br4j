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
package com.aplana.dbmi.module.notif;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CardPassportDataSource implements DataSource {
    protected final Log logger = LogFactory.getLog(getClass());

    private static final FileTypeMap fileTypeMap = FileTypeMap
	    .getDefaultFileTypeMap();

    private transient byte[] data;
    private String fileName;

    public CardPassportDataSource(byte[] data) {
	this.data = data;
	this.fileName = calculateName(new ByteArrayInputStream(data));
    }

    public String getContentType() {
	return fileTypeMap.getContentType(getName());
    }

    public InputStream getInputStream() {
	if (data == null) {
	    data = new byte[0];
	}
	return new ByteArrayInputStream(data);
    }

    public String getName() {
	return fileName;
    }

    public OutputStream getOutputStream() throws IOException {
	throw new UnsupportedOperationException();
    }

    private String calculateName(InputStream dataStream) {
	try {
	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    SAXParser parser = factory.newSAXParser();
	    RootElementNameResolver handler = new RootElementNameResolver();
	    parser.parse(dataStream, handler);
	    return handler.getName() + ".xml";
	} catch (ParserConfigurationException ex) {
	    logger.error("It is impossible to resolve fileName of XML", ex);
	} catch (SAXException ex) {
	    logger.error("It is impossible to resolve fileName of XML", ex);
	} catch (IOException ex) {
	    logger.error("It is impossible to resolve fileName of XML", ex);
	}
	return "passport.xml";
    }

    private static class RootElementNameResolver extends DefaultHandler {
	private String name;

	public RootElementNameResolver() {
	}

	public void startElement(String uri, String localName, String qName,
		Attributes attributes) throws SAXException {
	    if (name == null) {
		name = localName;
		if ("".equals(name)) {
		    name = qName;
		}
	    }
	}

	public String getName() {
	    return this.name;
	}
    }
}
