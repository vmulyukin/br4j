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
package com.aplana.dbmi.model;

import java.text.MessageFormat;
import java.util.Date;

import com.aplana.dbmi.service.DataException;

/**
 * �������� ����������� ���������. 
 * @author RAbdullin
 *
 */
public class PermissionDelegate extends DataObject {

	private static final long serialVersionUID = 1L;

	// �������������� ���������� (����� ������������ ������ ������)
	PermissionSet permissions;
	ObjectId permissionSetId;

	// ���� "� �" "��"
	// final Search.DatePeriod period = new Search.DatePeriod( null, null);
	Date startAt;
	Date endAt;

	// ��� �����������
	ObjectId fromPersonId;

	// ���� ������������ 
	ObjectId toPersonId;

	boolean fromPersonHasAccessToo;
	boolean active = true;


	// @Override
	public void setId(long id) {
		super.setId(new ObjectId(PermissionDelegate.class, id));
	}


	/**
	 * @return ������-����� ������� �������� �������������.
	 */
	public Date getStartAt() {
		return this.startAt;
	}

	public void setStartAt(Date value) {
		this.startAt = value;
		updateActive();
	}

	/**
	**��������� ���������� ������������� � ����������� �� �������
	 */
	private void updateActive(){
		Date now = new Date();
		active = (now != null) && (this.isActive())	&&	( this.startAt == null || this.startAt.before(now) ) &&	( this.endAt == null || this.endAt.after(now) );
		
	}
	/**
	 * @return ����� ������� �������� �������������.
	 */
	public Date getEndAt() {
		return this.endAt;
	}

	public void setEndAt(Date value) {
		this.endAt = value;
		updateActive();
	}

	/**
	 * ���������� ���� ������ � ����� ���������� ������� �������������.
	 * @param dateFrom	������, ���� null, �� ������������;
	 * @param dateTo	������, ���� null, �� ������������;
	 * @throws DataException 
	 */
	public void setPeriod(Date dateFrom, Date dateTo) throws DataException {
		if ( dateFrom != null && dateTo != null && dateFrom.after(dateTo))
			throw new DataException("delegate.period.invalid2", new Object[] {
					dateFrom, dateTo
			} );

		this.startAt = dateFrom;
		this.endAt = dateTo;
		updateActive();
	}


	/**
	 * @return ������ ���������� (����������), ����������� � ������.
	 * (!) ����������� ��� ���������� ������� ���������� ����� �������� 
	 * ������������� ������� ��������� (� ���� ������ ������ ����������, 
	 * ������� ���� � �����)
	 */
	public PermissionSet getPermissions() {
		return permissions;
	}

	public void setPermissions(PermissionSet permSet) {
		this.permissions = permSet;
		if ( (permSet != null) && (permSet.getId() != null) )
			this.permissionSetId = permSet.getId();
	}

	public ObjectId getPermissonSetId() {

		if (this.permissionSetId != null)
			return permissionSetId;

		return (permissions != null) ? permissions.getId() : null;
	}

	public void setPermissonSetId(long id) 
		throws DataException 
	{
		setPermissonSetId( (id <= 0) ? null : new ObjectId(PermissionSet.class, id));
	}

	public void setPermissonSetId(ObjectId permSetId) 
		throws DataException 
	{
		boolean ok;
		if (permSetId == null)
			ok = (permissions == null) || (permissions.getId() == null);
		else 
			ok = (permissions == null) 
					|| (permissions.getId() == null)
					|| permSetId.equals( permissions.getId())
				;
		if (!ok) {
			// permissions.setId( permSetId);
			throw new DataException( "delegate.permissions.setting.invaid_id2", 
						new Object[] { permSetId, (permissions == null) ? null : permissions.getId() }
					);
		}
		this.permissionSetId = permSetId;
	}


	/**
	 * @return id ������ ������ ���������� �������.
	 */
	public ObjectId getFromPersonId() {
		return this.fromPersonId;
	}

	/**
	 * @param sourcePerson ����� �������� id ���������������� �������
	 */
	public void setFromPersonId(ObjectId sourcePerson) {
		this.fromPersonId = sourcePerson;
	}

	public void setFromPersonId(long id) {
		// setFromPersonId( new ObjectId( Person.class, id));
		setFromPersonId( (id <= 0) ? null : new ObjectId( Person.class, id));
	}


	/**
	 * @return id �������������� �������, ������� �������� ����� ("��")
	 */
	public ObjectId getToPersonId() {
		return toPersonId;
	}


	/**
	 * @param fromPersonId ����� �������� id �������������� �������
	 */
	public void setToPersonId(ObjectId trustedPersonId) {
		this.toPersonId = trustedPersonId;
	}
	
	public void setToPersonId(long id) {
		setToPersonId( (id <= 0) ? null : new ObjectId( Person.class, id));
	}


	/**
	 * @param date ����������� ����
	 * @return true, ���� ������ ������� ��������� �� ��������� ����. 
	 */
	public boolean isActiveAt(Date date)
	{
		return (date != null) && (this.isActive())
			&&	( this.startAt == null || this.startAt.before(date) )
			&&	( this.endAt == null || this.endAt.after(date) );
	}


	/**
	 * @return true (default), ���� �������� ������� (����) ��������� �� ����� 
	 * �������������� �����, false - ���� �� ����� ������������� ���������� ���� 
	 * � ��� �� �����.
	 */
	public boolean isFromPersonHasAccessToo() {
		return fromPersonHasAccessToo;
	}

	/**
	 * @param value �������� ��� ���������� �������� ������� �� ����� 
	 * ������������� �������� ����������� �������.
	 */
	public void setFromPersonHasAccessToo(boolean value) {
		this.fromPersonHasAccessToo = value;
	}


	public boolean isActive() {
		return this.active;
	}


	public void setActive(boolean active) {
		this.active = active;
	}


	public int hashCode() {
		return super.hashCode(); // ID
	}


	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof PermissionDelegate))
			return false;

		final PermissionDelegate other = (PermissionDelegate) obj;

		if (fromPersonHasAccessToo != other.fromPersonHasAccessToo)
			return false;

		if (startAt == null) {
			if (other.startAt != null)
				return false;
		} else if (!startAt.equals(other.startAt))
			return false;

		if (endAt == null) {
			if (other.endAt != null)
				return false;
		} else if (!endAt.equals(other.endAt))
			return false;

		if (fromPersonId == null) {
			if (other.fromPersonId != null)
				return false;
		} else if (!fromPersonId.equals(other.fromPersonId))
			return false;

		if (toPersonId == null) {
			if (other.toPersonId != null)
				return false;
		} else if (!toPersonId.equals(other.toPersonId))
			return false;

		if (permissions == null) {
			if (other.permissions != null)
				return false;
		} else if (!permissions.equals(other.permissions))
			return false;

		return true;
	}


	public String toString() {
		return MessageFormat.format(
				"\n {0}( id={1}, " +

				"\n\t id from person	''{2}'', "+
				"\n\t id to person 		''{3}'', "+

				"\n\t id start at 		{4}, "+
				"\n\t id end at 		{5}, "+

				"\n\t boss has access	{6}, "+
				"\n\t permSetId			{7}, "+
				"\n\t permissions		{8}"+
				"\n\t is active 		{9}, "+
				"\n)", 
				new Object[] { 
						this.getClass().getName(),
						this.permissionSetId, 

						this.getFromPersonId(),
						this.getToPersonId(),

						this.getStartAt(),
						this.getEndAt(),

						new Boolean( this.isFromPersonHasAccessToo()),
						this.getPermissonSetId(),
						this.getPermissions(),
						this.active
				});
	}


}
