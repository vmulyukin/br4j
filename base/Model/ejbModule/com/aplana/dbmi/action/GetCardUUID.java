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

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TextAttribute;

/**
 * �����, ������������ �������� �������� UUID �������� ��� ������� ��������
 * ���� ������� �� �����, �� ������������ ����� UUID, ������������ � �������� � ������������ 
 * @author ynikitin
 * return String
 *
 */
public class GetCardUUID implements Action {
	private static final long serialVersionUID = 1L;

	private ObjectId cardId;
    
    public static final ObjectId JBR_UUID = new ObjectId(TextAttribute.class, "JBR_UUID");

    public ObjectId getCardId() {
    	return this.cardId;
    }

    public void setCardId(ObjectId cardId) {
    	this.cardId = cardId;
    }

    public Class<?> getResultType() {
    	return String.class;
    }
}
