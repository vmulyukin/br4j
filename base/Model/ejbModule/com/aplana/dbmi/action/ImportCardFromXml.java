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

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import com.aplana.dbmi.model.ObjectId;

public class ImportCardFromXml implements Action<ImportCardFromXml.ImportCard> {

	private static final long serialVersionUID = 1L;
	private transient InputStream source;
	private transient InputStream letterSource;
	private String fileName = "defaultFileName.xml";

	public Class<?> getResultType() {
		return ImportCard.class;
	}

	public InputStream getSource() {
		return this.source;
	}

	public void setSource(InputStream source) {
		this.source = source;
	}

	public InputStream getLetterSource() {
		return this.letterSource;
	}

	public void setLetterSource(InputStream letterSource) {
		this.letterSource = letterSource;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public static abstract class ImportCard implements Action<ImportCard.Result> {
		private static final long serialVersionUID = 1L;
		
		private long packetCardId;
		private transient byte[] streamData;

		public Class<?> getResultType() {
			return Result.class;
		}

		public long getPacketCardId() {
			return this.packetCardId;
		}

		public void setPacketCardId(long packetCardId) {
			this.packetCardId = packetCardId;
		}

		public byte[] getStreamData() {
			return streamData;
		}

		public void setStreamData(byte[] streamData) {
			this.streamData = streamData;
		}

		public static class Result {
			private final ObjectId cardId;
			private final Map<ObjectId, String> paths;
			private StatusDescription statusDescription = new StatusDescription();

			public Result(ObjectId cardId, Map<ObjectId, String> paths) {
				this.cardId = cardId;
				if (paths == null) {
					this.paths = Collections.emptyMap();
				} else {
					this.paths = paths;
				}
			}

			public Result() {
				this(null, null);
			}

			public ObjectId getCardId() {
				return this.cardId;
			}

			public Map<ObjectId, String> getPaths() {
				return Collections.unmodifiableMap(this.paths);
			}

			public boolean isResultSuccessful() {
				return StatusDescription.OK_CODE.equals(statusDescription
						.getStatusCode());
			}

			public StatusDescription getStatusDescription() {
				return this.statusDescription;
			}

			public void setStatusDescription(StatusDescription errorMessage) {
				this.statusDescription = errorMessage;
			}
		}

		public static class StatusDescription {
			public static final Long OK_CODE = Long.valueOf(0);
			public static final Long DEFAULT_ERROR_CODE = Long.valueOf(-1);
			private String result;
			private Long statusCode = OK_CODE;

			public String getResult() {
				return this.result;
			}

			public void setResult(String result) {
				this.result = result;
			}

			public Long getStatusCode() {
				return this.statusCode;
			}

			public void setStatusCode(Long statusCode) {
				this.statusCode = statusCode;
			}

			public void setError(String message) {
				setResult(message);
				setStatusCode(DEFAULT_ERROR_CODE);
			}
		}
	}
}
