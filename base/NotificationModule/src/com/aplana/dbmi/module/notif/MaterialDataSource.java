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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.MaterialUtil;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.ProcessorBase;

public class MaterialDataSource implements DataSource
{
	protected final Log logger = LogFactory.getLog(getClass());

	private static final FileTypeMap fileTypeMap = MimetypesFileTypeMap.getDefaultFileTypeMap();
	private final ProcessorBase caller;
	private final ObjectId cardId;
	private String name = null;

	public MaterialDataSource(ObjectId cardId, ProcessorBase caller, String name) {
		this.cardId = cardId;
		this.caller = caller;
		this.name = name;
	}

	public MaterialDataSource(ObjectId cardId, ProcessorBase caller) {
		this(cardId, caller, null);
	}

	public String getContentType() {
		return fileTypeMap.getContentType(getName());
	}

	public InputStream getInputStream() throws IOException {
		try {
			return MaterialUtil.getMaterial(cardId, caller);
		} catch (DataException e) {
			logger.error("Error loading material from card " + cardId.getId(), e);
			throw new IOException("Error loading material from card " + cardId.getId());
		}
	}

	public String getName() {
		if (name == null) {
			try {
				ObjectQueryBase query = caller.getQueryFactory().getFetchQuery(Card.class);
				query.setId(cardId);
				Card card = (Card) caller.getDatabase().executeQuery(caller.getUser(), query);
				MaterialAttribute material = (MaterialAttribute) card.getAttributeById(Attribute.ID_MATERIAL);
				if (material == null) {
					logger.error("Card " + cardId.getId() + " don't have material attribute");
					return null;
				}
				name = material.getMaterialName();
			} catch (DataException e) {
				logger.error("Error fetching card " + cardId.getId(), e);
			}
		}
		return name;
	}

	public OutputStream getOutputStream() throws IOException {
		throw new UnsupportedOperationException();
	}
}
