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
package com.aplana.dbmi.ws;

import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(name = "CardImportService", targetNamespace = "http://aplana.com/dbmi/ws/CardImportService")
public interface CardImportService {
	@WebMethod
	@WebResult(name = "cardId", targetNamespace = "")
	@RequestWrapper(localName = "importCard", targetNamespace = "http://aplana.com/dbmi/ws/CardImportService", className = "com.aplana.dbmi.ws.cardimportservice.ImportCard")
	@ResponseWrapper(localName = "importCardResponse", targetNamespace = "http://aplana.com/dbmi/ws/CardImportService", className = "com.aplana.dbmi.ws.cardimportservice.ImportCardResponse")
	public long importCard(@WebParam(name = "card", targetNamespace = "")
	DataHandler card, @WebParam(name = "filename", targetNamespace = "")
	String filename, @WebParam(name = "file", targetNamespace = "")
	DataHandler file);

	@WebMethod
	@WebResult(name = "resString", targetNamespace = "")
	// @RequestWrapper(localName = "testService", targetNamespace =
	// "http://aplana.com/dbmi/ws/CardImportService", className =
	// "com.aplana.dbmi.ws.cardimportservice.TestCardImportServiceRequest")
	public String testService(
			@WebParam(name = "instring", targetNamespace = "")
			String instring,
			@WebParam(name = "instring1", targetNamespace = "")
			String instring1);

	@WebMethod
	@WebResult(name = "cardId", targetNamespace = "")
	public Long importMaterialCard(
			@WebParam(name = "attrName", targetNamespace = "")
			String attrName, @WebParam(name = "attrVal", targetNamespace = "")
			String attrVal);

	@WebMethod
	@WebResult(name = "cardId", targetNamespace = "")
	public Long importCardFromXML(
			@WebParam(name = "encoding", targetNamespace = "")
			String encoding,
			@WebParam(name = "xmlInString", targetNamespace = "")
			String xmlInString,
			@WebParam(name = "inFileName", targetNamespace = "")
			String inFileName,
			@WebParam(name = "inFileBase64", targetNamespace = "")
			String inFileBase64,
			@WebParam(name = "inAttachmentIds", targetNamespace = "")
			String inAttachmentIds,
			@WebParam(name = "inFileNames", targetNamespace = "")
			String inFileNames);

}
