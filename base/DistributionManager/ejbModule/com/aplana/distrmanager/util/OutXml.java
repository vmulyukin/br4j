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
package com.aplana.distrmanager.util;

import com.aplana.dbmi.action.ExportCardToXml;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TypeStandard;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;

public class OutXml {
	
	private DataServiceFacade serviceBean = null;
	public OutXml(DataServiceFacade serviceBean) {
		this.serviceBean = serviceBean;
	}
	public ExportCardToXml.Result createMessageXml(ObjectId idDocBase, ObjectId idElm, TypeStandard typeStandard) throws DataException {
		// �������� ����� ExportCardToXml ��� ��������� ����������� xml-�� � ������
		ExportCardToXml exportCardToXml = new ExportCardToXml();
		exportCardToXml.setCardId(idDocBase);
		exportCardToXml.setRecipientId(idElm);
		exportCardToXml.setTypeStandard(typeStandard);
		ExportCardToXml.Result res;
		
		try{
			res = (ExportCardToXml.Result) serviceBean.doAction(exportCardToXml);
		} catch(Exception e) {
			throw new DataException(String.format("Error while saving export-xml for card {%d} and recipient {%d}:",
					idDocBase.getId(), idElm.getId()), e);
		}
		return res;
	}
}
