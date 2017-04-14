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

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.ObjectId;

/** Экшен, который меняет Зону доу у документов-оснований: ОГ, Входящие, Внутренние, исходящие, в которых текущий пользователь или пользователи текущего департамента являются Автором, Подписантом или Адресатом
 * На входе card_id пользователя или департамента (флаг isDepartment говорит о том, какая карточка - Персона или Департамент) 
 * @author ynikitin
 */
public class ChangeZonesOfDocsForUser implements Action {
	private static final long serialVersionUID = 1L;
	private ObjectId cardId;
	private boolean isDepartment = false;
	private Long versionId = null;
	private boolean isZoneAccess = false;
	
	public ObjectId getCardId() {
		return cardId;
	}

	public void setCardId(ObjectId cardId) {
		this.cardId = cardId;
	}

	public boolean isDepartment() {
		return isDepartment;
	}

	public void setDepartment(boolean isDepartment) {
		this.isDepartment = isDepartment;
	}

	public Long getVersionId() {
		return versionId;
	}

	public void setVersionId(Long versionId) {
		this.versionId = versionId;
	}

	public boolean isZoneAccess() {
		return isZoneAccess;
	}

	public void setZoneAccess(boolean isZoneAccess) {
		this.isZoneAccess = isZoneAccess;
	}

	public Class getResultType() {
		// TODO Auto-generated method stub
		return Long.class;
	}
	
	public boolean equals(Object obj)
	{
		if(obj == this)
			return true;
	
	     /* obj ссылается на null */
		if(obj == null)
			return false;
	
		/* Удостоверимся, что ссылки имеют тот же самый тип */
	    if(!(getClass() == obj.getClass()))
	    	return false;
	    else
	    {
	    	ChangeZonesOfDocsForUser tmp = (ChangeZonesOfDocsForUser)obj;
	    	if(tmp.cardId == this.cardId&&tmp.versionId == this.versionId&&tmp.isDepartment == this.isDepartment&&tmp.isZoneAccess == this.isZoneAccess)
	    		return true;
	    	else
	    		return false;
	    }
	}
}
