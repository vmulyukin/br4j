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
package com.aplana.dbmi.card.download.actionhandler;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.aplana.dbmi.action.ImportCardFromXml;
import com.aplana.dbmi.action.ImportCardFromXml.ImportCard.Result;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * implementation of the FileActionHandler that upload card from file
 */
public class CardUploadAction extends UploadAction {

    @Override
    protected String processFile(InputStream sourceStream, String fileName, HttpServletRequest request)
	    throws DataException {
	try {
	    DataServiceBean dataServiceBean = getServiceBean();
	    ImportCardFromXml action = new ImportCardFromXml();
	    action.setSource(sourceStream);
	    action.setFileName(fileName);
	    ImportCardFromXml.ImportCard importAction = (ImportCardFromXml.ImportCard) dataServiceBean
		    .doAction(action);
	    Result importResult = (Result) dataServiceBean
		    .doAction(importAction);
	    return formatImportResult(importResult);
	} catch (ServiceException ex) {
	    throw new DataException(ex);
	}
    }

    private String formatImportResult(Result result) {
	StringBuilder pathsBuilder = new StringBuilder();
	Map<ObjectId, String> paths = result.getPaths();
	if (paths != null && !paths.isEmpty()) {
	    for (Entry<ObjectId, String> file : paths.entrySet()) {
		if (pathsBuilder.length() > 0) {
		    pathsBuilder.append("&");
		}
		try {
		    ObjectId cardId = file.getKey();
		    String path = file.getValue();
		    pathsBuilder.append(String.format("%s=%s", cardId.getId(),
			    URLEncoder.encode(path, "UTF-8")));
		} catch (UnsupportedEncodingException ex) {
		    throw new IllegalStateException(ex);
		}
	    }
	}
	return String.format("%s?%s", result.getCardId().getId(), pathsBuilder
		.toString());
    }
}
