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
package com.aplana.dmsi;

import com.aplana.dbmi.action.ExportCardToXml;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TypeStandard;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dmsi.action.ExportCard;
import com.aplana.dmsi.action.ExportCardByDelo;
import com.aplana.dmsi.action.ExportCardByGOST;

public class DoExportCardToXml extends ActionQueryBase implements WriteQuery {

    private static final long serialVersionUID = 1L;

    @Override
    public Object processQuery() throws DataException {
	ExportCardToXml action = (ExportCardToXml) getAction();
	ExportCard exportAction = resolveExportAction(action.getTypeStandard(), action.getRecipientId());
	exportAction.setCardId(action.getCardId());
	exportAction.setRecipientId(action.getRecipientId());
	Database database = getDatabase();
	ActionQueryBase actionQuery= getQueryFactory().getActionQuery(exportAction);
	actionQuery.setAction(exportAction);
	return database.executeQuery(getUser(), actionQuery);
	}

    private ExportCard resolveExportAction(TypeStandard typeStandard, @SuppressWarnings("unused")ObjectId recipientId) {
	ExportCard result = null;
    if (typeStandard == null){
    	result = new ExportCardByDelo();
    }else if (typeStandard.equals(TypeStandard.DELO)) {
    	result = new ExportCardByDelo();
    }else if (typeStandard.equals(TypeStandard.GOST)){
    	result = new ExportCardByGOST();
    }
	return result;
    }
}
