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
package com.aplana.dbmi.filestorage.converters.strategy;

import java.io.InputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Strategy of converting
 * @author desu
 *
 */
public interface ConvertStrategy {
	
	/**
	 * Convert input data (presented by InputStream) into PDF
	 * @param document {@link Document} to store pdf
	 * @param inputStream Stream of image
	 * @param writer {@link PdfWriter} to write content of PDF
	 * @return true if convert was success
	 */
	public boolean convert(Document document, InputStream inputStream, PdfWriter writer);
	
}
