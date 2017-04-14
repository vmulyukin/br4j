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
package com.aplana.dbmi.cardexchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;

import org.w3c.dom.Document;

import ru.it.fsfr.services.dbmi.DBMIStartReglamentService;
import ru.it.fsfr.services.dbmi.File;
import ru.it.fsfr.services.dbmi.IDBMIStartReglamentService;

import com.aplana.dbmi.cardexchange.xml.CardExchangeUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.MaterialUtil;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.ProcessorBase;

public class ExportCardProcessor extends ProcessorBase implements Parametrized {
	private final static String PARAM_SERVICE_URL = "serviceURL";
	private String serviceURL;
	private class FileExportDataSource implements DataSource {
		InputStream stream;
		FileExportDataSource(InputStream stream) {
			this.stream = stream;
		}

		public String getContentType() {
			return "application/octet-stream";
		}

		public InputStream getInputStream() throws IOException {
			return stream;
		}

		public String getName() {
			throw new UnsupportedOperationException();
		}

		public OutputStream getOutputStream() throws IOException {
			throw new UnsupportedOperationException();
		}
	}
	
	public Object process() throws DataException {
		Card card = (Card)getObject(); 
		Document doc = CardExchangeUtils.getCardXML(card);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		try {
	        Transformer transformer = transformerFactory.newTransformer();
	        DOMSource source = new DOMSource(doc);
	        StringWriter writer = new StringWriter();
	        Result result =  new StreamResult(writer);
	        transformer.transform(source, result);
			String stCard = writer.getBuffer().toString(); 
			writer.close();
			System.out.println(stCard);
			DBMIStartReglamentService service = new DBMIStartReglamentService();
			IDBMIStartReglamentService proxy = service.getDBMIStartReglamentServicePort();
			if (serviceURL != null) {
				((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
	              serviceURL);
			}
			File f = new File();
			if (card.getMaterialType() == card.MATERIAL_FILE) {
				f.setName(card.getFileName());
				InputStream stream = MaterialUtil.getMaterial(card.getId(), this);
				if (stream != null) {
					f.setHref(new DataHandler(new FileExportDataSource(stream)));
				}
			}
	        proxy.startReglamentByCard(stCard, f);
		} catch (Exception e) {
			logger.error("An error occured while trying to send card xml to FSFR system", e);
			throw new DataException(e);
		}
		return null;
	}

	public void setParameter(String name, String value) {
		if (PARAM_SERVICE_URL.equals(name)) {
			serviceURL = value;
		}
	}
}
